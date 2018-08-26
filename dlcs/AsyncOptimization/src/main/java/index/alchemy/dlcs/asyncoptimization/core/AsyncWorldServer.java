package index.alchemy.dlcs.asyncoptimization.core;

import java.io.File;
import java.net.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import index.alchemy.api.IFieldContainer;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Patch;
import index.alchemy.core.AlchemyThreadManager;
import index.alchemy.dlcs.asyncoptimization.api.IAsyncThreadListener;
import index.alchemy.dlcs.asyncoptimization.api.IExtendedNetworkManager;
import index.alchemy.util.FieldContainer;
import index.alchemy.util.SideHelper;
import index.alchemy.util.cache.ICache;
import index.alchemy.util.cache.StdCache;
import index.alchemy.util.observer.ObserverWrapperFieldContainer;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.DemoPlayerInteractionManager;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListIPBans;
import net.minecraft.server.management.UserListOps;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

@Listener
@Hook.Provider
@Patch("net.minecraft.world.WorldServer")
public class AsyncWorldServer extends WorldServer implements IAsyncThreadListener {
	
	public static class InnerConfig {
		
		@Config(category = AsyncOptimization.DLC_NAME, comment = "Minimum unloading time after loading in the world.")
		public static int ticks_waited = 20 * 10;
		
	}
	
	@Patch("io.netty.channel.ChannelOutboundBuffer")
	public static class Patch$ChannelOutboundBuffer {
		
		public void addMessage(Object msg, int size, ChannelPromise promise) {
			synchronized (this) { addMessage(msg, size, promise); }
		}
		
		public void addFlush() {
			synchronized (this) { addFlush(); }
		}
		
//		private void removeEntry(ChannelOutboundBuffer.Entry e) {
//			synchronized (this) { removeEntry(e); }
//		}
		
	}
	
	@Patch("net.minecraft.server.MinecraftServer")
	public static abstract class Patch$MinecraftServer extends MinecraftServer {

		@Patch.Exception
		public Patch$MinecraftServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn,
				YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn,
				GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
			super(anvilFileIn, proxyIn, dataFixerIn, authServiceIn, sessionServiceIn, profileRepoIn, profileCacheIn);
		}
		
		@Override
		public void updateTimeLightAndEntities() {
			profiler.startSection("jobs");
			synchronized (futureTaskQueue) {
				while (!futureTaskQueue.isEmpty())
					try {
						Util.runTask(futureTaskQueue.poll(), LOGGER);
					} catch (ThreadQuickExitException e) { }
			}
			profiler.endStartSection("levels");
			net.minecraftforge.common.chunkio.ChunkIOExecutor.tick();
			for (int id : net.minecraftforge.common.DimensionManager.getIDs(tickCounter % 200 == 0)) {
				long nano = System.nanoTime();
				if (id == 0 || getAllowNether()) {
					WorldServer world = net.minecraftforge.common.DimensionManager.getWorld(id);
					world.addScheduledTask(() -> {
						if (tickCounter % 20 == 0)
							playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(world.getTotalWorldTime(), world.getWorldTime(),
									world.getGameRules().getBoolean("doDaylightCycle")), world.provider.getDimension());
						net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreWorldTick(world);
						try {
							world.tick();
							world.updateEntities();
						} catch (Throwable t) {
							CrashReport report = CrashReport.makeCrashReport(t, "Exception ticking world");
							world.addWorldInfoToCrashReport(report);
							throw new ReportedException(report);
						}
						net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostWorldTick(world);
						world.getEntityTracker().tick();
						worldTickTimes.get(id)[tickCounter % 100] = System.nanoTime() - nano;
					});
				}
			}
			profiler.endStartSection("dim_unloading");
			net.minecraftforge.common.DimensionManager.unloadWorlds(worldTickTimes);
			profiler.endStartSection("connection");
			getNetworkSystem().networkTick();
			profiler.endStartSection("players");
			playerList.onTick();
			profiler.endStartSection("tickables");
			tickables.forEach(ITickable::update);
			profiler.endSection();
		}
		
	}
	
	@Patch("net.minecraft.server.management.PlayerList")
	public static class Patch$PlayerList extends PlayerList {
		
		@Patch.Replace
		public Patch$PlayerList(MinecraftServer server) {
			super(server);
			bannedPlayers = new UserListBans(FILE_PLAYERBANS);
			bannedIPs = new UserListIPBans(FILE_IPBANS);
			ops = new UserListOps(FILE_OPS);
			whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
			playerStatFiles = Collections.synchronizedMap(Maps.newHashMap());
			uuidToPlayerMap = Collections.synchronizedMap(Maps.newHashMap());
			playerEntityList= Collections.synchronizedList(Lists.newArrayList());
			advancements = Collections.synchronizedMap(Maps.newHashMap());
			this.server = server;
			bannedPlayers.setLanServer(false);
			bannedIPs.setLanServer(false);
			maxPlayers = 8;
		}
		
		@Override
		public void transferPlayerToDimension(EntityPlayerMP player, int targetDimension, net.minecraftforge.common.util.ITeleporter teleporter) {
			int dimension = player.dimension;
			WorldServer oldWorld = server.getWorld(player.dimension);
			player.dimension = targetDimension;
			WorldServer newWorld = server.getWorld(player.dimension);
			IAsyncThreadListener.class.cast(oldWorld).syncCall(() -> {
				player.connection.sendPacket(new SPacketRespawn(player.dimension, newWorld.getDifficulty(),
						newWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
				updatePermissionLevel(player);
				oldWorld.removeEntityDangerously(player);
				player.isDead = false;
				transferEntityToWorld(player, dimension, oldWorld, newWorld, teleporter);
			});
			IAsyncThreadListener.class.cast(newWorld).syncCall(() -> {
				preparePlayer(player, oldWorld);
				player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
				player.interactionManager.setWorld(newWorld);
				player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
				updateTimeAndWeatherForPlayer(player, newWorld);
				syncPlayerInventory(player);
				for (PotionEffect effect : player.getActivePotionEffects())
					player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), effect));
				// Fix MC-88179: on non-death SPacketRespawn, also resend attributes
				AttributeMap attributemap = (AttributeMap) player.getAttributeMap();
				Collection<net.minecraft.entity.ai.attributes.IAttributeInstance> watchedAttribs = attributemap.getWatchedAttributes();
				if (!watchedAttribs.isEmpty())
					player.connection.sendPacket(new SPacketEntityProperties(player.getEntityId(), watchedAttribs));
				net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, dimension, targetDimension);
			});
		}


		@Override
		public void transferEntityToWorld(Entity entity, int lastDimension, WorldServer oldWorld, WorldServer newWorld, ITeleporter teleporter) {
			WorldProvider pOld = oldWorld.provider, pNew = newWorld.provider;
			double moveFactor = pOld.getMovementFactor() / pNew.getMovementFactor();
			double dx = entity.posX * moveFactor;
			double dz = entity.posZ * moveFactor;
			float yaw = entity.rotationYaw;
			if (entity.dimension == 1) {
				BlockPos blockpos;
				if (lastDimension == 1)
					blockpos = newWorld.getSpawnPoint();
				else
					blockpos = newWorld.getSpawnCoordinate();
				dx = blockpos.getX();
				entity.posY = blockpos.getY();
				dz = blockpos.getZ();
				double fdx = dx, fdz = dz;
				IAsyncThreadListener.class.cast(oldWorld).syncCall(() -> {
					entity.setLocationAndAngles(fdx, entity.posY, fdz, 90.0F, 0.0F);
					if (entity.isEntityAlive())
						oldWorld.updateEntityWithOptionalForce(entity, false);
				});
			}
			if (lastDimension != 1) {
				dx = MathHelper.clamp((int) dx, -29999872, 29999872);
				dz = MathHelper.clamp((int) dz, -29999872, 29999872);
				double fdx = dx, fdz = dz;
				IAsyncThreadListener.class.cast(newWorld).syncCall(() -> {
					if (entity.isEntityAlive()) {
						entity.setLocationAndAngles(fdx, entity.posY, fdz, entity.rotationYaw, entity.rotationPitch);
						teleporter.placeEntity(newWorld, entity, yaw);
						newWorld.spawnEntity(entity);
						newWorld.updateEntityWithOptionalForce(entity, false);
						entity.setWorld(newWorld);
					}
				});
			}
		}
		
		@Override
		public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP oldPlayer, int targetDimension, boolean conqueredEnd) {
			EntityPlayerMP playerMP[] = { null };
			WorldServer oldWorld = server.getWorld(targetDimension);
			if (oldWorld == null)
				targetDimension = oldPlayer.getSpawnDimension();
			else if (!oldWorld.provider.canRespawnHere())
				targetDimension = oldWorld.provider.getRespawnDimension(oldPlayer);
			if (server.getWorld(targetDimension) == null)
				targetDimension = 0;
			int dimension = targetDimension;
			IAsyncThreadListener.class.cast(oldPlayer.getServerWorld()).syncCall(() -> {
				oldPlayer.getServerWorld().getEntityTracker().removePlayerFromTrackers(oldPlayer);
				oldPlayer.getServerWorld().getEntityTracker().untrack(oldPlayer);
				oldPlayer.getServerWorld().getPlayerChunkMap().removePlayer(oldPlayer);
				playerEntityList.remove(oldPlayer);
				server.getWorld(oldPlayer.dimension).removeEntityDangerously(oldPlayer);
				oldPlayer.dimension = dimension;
			});
			WorldServer newWorld = server.getWorld(dimension);
			IAsyncThreadListener.class.cast(newWorld).syncCall(() -> {
				BlockPos bedPos = oldPlayer.getBedLocation(dimension);
				boolean flag = oldPlayer.isSpawnForced(dimension);
				PlayerInteractionManager interactionManager;
				if (server.isDemo())
					interactionManager = new DemoPlayerInteractionManager(newWorld);
				else
					interactionManager = new PlayerInteractionManager(newWorld);
				EntityPlayerMP newPlayer = new EntityPlayerMP(server, newWorld, oldPlayer.getGameProfile(), interactionManager);
				newPlayer.connection = oldPlayer.connection;
				newPlayer.copyFrom(oldPlayer, conqueredEnd);
				newPlayer.dimension = dimension;
				newPlayer.setEntityId(oldPlayer.getEntityId());
				newPlayer.setCommandStats(oldPlayer);
				newPlayer.setPrimaryHand(oldPlayer.getPrimaryHand());
				oldPlayer.getTags().forEach(newPlayer::addTag);
				playerEntityList.add(newPlayer);
				uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
				setPlayerGameTypeBasedOnOther(newPlayer, oldPlayer, newWorld);
				if (bedPos != null) {
					BlockPos spawnPos = EntityPlayer.getBedSpawnLocation(newWorld, bedPos, flag);
					if (spawnPos != null) {
						newPlayer.setLocationAndAngles(spawnPos.getX() + 0.5F, spawnPos.getY() + 0.1F, spawnPos.getZ() + 0.5F, 0.0F, 0.0F);
						newPlayer.setSpawnPoint(bedPos, flag);
					} else
						newPlayer.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
				}
				newWorld.getChunkProvider().provideChunk((int) newPlayer.posX >> 4, (int) newPlayer.posZ >> 4);
				while (!newWorld.getCollisionBoxes(newPlayer, newPlayer.getEntityBoundingBox()).isEmpty() && newPlayer.posY < 256.0D)
					newPlayer.setPosition(newPlayer.posX, newPlayer.posY + 1.0D, newPlayer.posZ);
				newPlayer.connection.sendPacket(new SPacketRespawn(newPlayer.dimension, newPlayer.world.getDifficulty(),
						newPlayer.world.getWorldInfo().getTerrainType(), newPlayer.interactionManager.getGameType()));
				newPlayer.connection.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ, newPlayer.rotationYaw, newPlayer.rotationPitch);
				newPlayer.connection.sendPacket(new SPacketSpawnPosition(newWorld.getSpawnPoint()));
				newPlayer.connection.sendPacket(new SPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));
				updateTimeAndWeatherForPlayer(newPlayer, newWorld);
				updatePermissionLevel(newPlayer);
				newWorld.getPlayerChunkMap().addPlayer(newPlayer);
				newWorld.spawnEntity(newPlayer);
				newPlayer.addSelfToInternalCraftingInventory();
				newPlayer.setHealth(newPlayer.getHealth());
				net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(newPlayer, conqueredEnd);
				playerMP[0] = newPlayer;
			});
			return playerMP[0];
		}
		
		@Override
		protected void writePlayerData(EntityPlayerMP player) {
			IAsyncThreadListener listener = IAsyncThreadListener.class.cast(player.getServerWorld());
			if (listener.isCallingFromMinecraftThread())
				writePlayerData(player);
			else
				listener.addScheduledTask(() -> writePlayerData(player));
		}
		
		@Override
		public void setViewDistance(int distance) {
			viewDistance = distance;
			if (server.worlds != null)
				for (WorldServer world : server.worlds)
					if (world != null)
						IAsyncThreadListener.class.cast(world).addScheduledTask(() -> {
							world.getPlayerChunkMap().setPlayerViewRadius(distance);
							world.getEntityTracker().setViewDistance(distance);
						});
		}
		
	}
	
	@Patch("net.minecraft.network.NetworkSystem")
	public static class Patch$NetworkSystem extends NetworkSystem {

		@Patch.Exception
		public Patch$NetworkSystem(MinecraftServer server) {
			super(server);
		}
		
		@Override
		public void networkTick() {
			synchronized (networkManagers) {
				Iterator<NetworkManager> iterator = networkManagers.iterator();
				ICache<IThreadListener, List<NetworkManager>> cache = new StdCache<IThreadListener, List<NetworkManager>>().setOnMissGet(Lists::newLinkedList);
				while (iterator.hasNext()) {
					NetworkManager manager = iterator.next();
					IThreadListener listener = IExtendedNetworkManager.class.cast(manager).getThreadListener();
					if (listener != null)
						cache.get(listener).add(manager);
					else
						doNetworkManagerTick(manager);
				}
				cache.getCacheMap().forEach((listener, list) -> listener.addScheduledTask(() -> list.forEach(this::doNetworkManagerTick)));
				networkManagers.removeIf(manager -> !manager.hasNoChannel() && !manager.isChannelOpen());
			}
		}
		
		@SuppressWarnings("unchecked")
		public void doNetworkManagerTick(NetworkManager manager) {
			if (!manager.hasNoChannel()) {
				if (manager.isChannelOpen()) {
					try {
						manager.processReceivedPackets();
					} catch (Exception exception) {
						if (manager.isLocalChannel()) {
							CrashReport report = CrashReport.makeCrashReport(exception, "Ticking memory connection");
							CrashReportCategory category = report.makeCategory("Ticking connection");
							category.addDetail("Connection", manager::toString);
							throw new ReportedException(report);
						}
						LOGGER.warn("Failed to handle packet for {}", new Object[] {manager.getRemoteAddress(), exception});
						TextComponentString component = new TextComponentString("Internal server error");
						manager.sendPacket(new SPacketDisconnect(component), future -> manager.closeChannel(component));
						manager.disableAutoRead();
					}
				} else
					manager.handleDisconnection();
			}
		}
		
	}
	
	@Patch("net.minecraftforge.common.chunkio.ChunkIOProvider")
	public static class Patch$ChunkIOProvider {
		
		private Chunk chunk;
		
		public void syncCallback() {
			if (chunk == null) {
				syncCallback();
				return;
			}
			IThreadListener listener = IThreadListener.class.cast(chunk.getWorld());
			if (listener.isCallingFromMinecraftThread())
				syncCallback();
			else
				listener.addScheduledTask(this::syncCallback);
		}
		
	}
	
	@Patch("net.minecraftforge.common.DimensionManager")
	public static class Patch$DimensionManager extends DimensionManager {
		
		public static void unloadWorlds(Hashtable<Integer, long[]> worldTickTimes) {
			IntIterator queueIterator = unloadQueue.iterator();
			while (queueIterator.hasNext()) {
				int id = queueIterator.nextInt();
				Dimension dimension = dimensions.get(id);
				if (dimension == null || dimension.ticksWaited < ForgeModContainer.dimensionUnloadQueueDelay + InnerConfig.ticks_waited) {
					dimension.ticksWaited++;
					continue;
				}
				WorldServer world = worlds.get(id);
				queueIterator.remove();
				dimension.ticksWaited = 0;
				if (world != null)
					world.addScheduledTask(() -> {
						// Don't unload the world if the status changed
						if (world == null || !canUnloadWorld(world)) {
							FMLLog.log.debug("Aborting unload for dimension {} as status changed", id);
							return;
						}
						try {
							world.saveAllChunks(true, null);
						} catch (MinecraftException e) {
							FMLLog.log.error("Caught an exception while saving all chunks:", e);
						} finally {
							MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));
							world.flush();
							setWorld(id, null, world.getMinecraftServer());
						}
					});
			}
		}
		
	}
	
	@Override
	public void saveAllChunks(boolean all, IProgressUpdate progressCallback) throws MinecraftException {
		if (isCallingFromMinecraftThread()) {
			saveAllChunks(all, progressCallback);
			return;
		}
		MinecraftException pointer[] = { null };
		syncCall(() -> {
			try {
				saveAllChunks(all, progressCallback);
			} catch (MinecraftException e) { pointer[0] = e; }
		});
		if (pointer[0] != null)
			throw pointer[0];
	}
	
//	@Patch.Exception
//	@Hook(value = "net.minecraft.world.chunk.Chunk#<init>", type = Hook.Type.TAIL)
//	public static void _init_Chunk(Chunk chunk, World world, int x, int z) {
//		if (SideHelper.isServer()) {
//			System.out.println(chunk + " - " + x + " - " + z);
//		}
//	}
	
//	@Patch.Exception
//	@Hook(value = "net.minecraftforge.common.DimensionManager$Dimension#<init>", type = Hook.Type.TAIL)
//	public static void _init_(DimensionManager.Dimension dimension, DimensionType type) {
//		dimension.ticksWaited = -InnerConfig.ticks_waited;
//	}
	
	@Patch.Exception
	@Hook("net.minecraftforge.fml.server.FMLServerHandler#getWorldThread")
	public static Hook.Result getWorldThread(FMLServerHandler handler, INetHandler netHandler) {
		return netHandler instanceof NetHandlerPlayServer ? new Hook.Result(((NetHandlerPlayServer) netHandler).player.world) : Hook.Result.VOID;
	}
	
	@Patch.Exception
	@Hook("net.minecraft.world.chunk.Chunk#logCascadingWorldGeneration")
	public static Hook.Result logCascadingWorldGeneration(Chunk chunk) {
		return Hook.Result.NULL;
	}
	
	protected LinkedBlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();
	protected Thread asyncThread;
	protected IFieldContainer<Boolean> running = new ObserverWrapperFieldContainer<>(new FieldContainer<>(Boolean.TRUE), (old, _new) -> {
		if (asyncThread != Thread.currentThread() && asyncThread != null && asyncThread.isAlive())
			if (old == Boolean.TRUE && _new == Boolean.FALSE)
				asyncThread.interrupt();
	}) ;
	
	{ startLoop(); }
	
	@Nullable
	public void startLoop() {
		running.set(Boolean.TRUE);
		Thread result = asyncThread != null && asyncThread.isAlive() ? null : AlchemyThreadManager.runOnNewThread(() -> {
			while (running.get() || runnables.size() > 0)
				try {
					runnables.take().run();
				} catch (InterruptedException e) { break; }
			asyncThread = null;
		}, "AsyncWorld-" + provider.getDimensionType().getName() + "-" + provider.getDimension());
		if (result != null)
			asyncThread = result;
	}
	
	public IFieldContainer<Boolean> running() { return running; }
	
	public BlockingQueue<Runnable> runnables() { return runnables; }

	public Thread asyncThread() { return asyncThread; }
	
	public AsyncWorldServer(MinecraftServer server, ISaveHandler saveHandler, WorldInfo info, int dimensionId, Profiler profilerIn) {
		super(server, saveHandler, info, dimensionId, profilerIn);	
	}
	
	@Override
	public boolean isCallingFromMinecraftThread() {
		return asyncThread == null || !asyncThread.isAlive() || !running().get() || Thread.currentThread() == asyncThread;
	}
	
	@Override
	public ListenableFuture<Object> addScheduledTask(Runnable runnable) {
		return IAsyncThreadListener.super.addScheduledTask(runnable);
	}
	
//	@Overrides
//	@SuppressWarnings("deprecation")
//	protected void finalize() throws Throwable {
//		try { running().set(Boolean.FALSE); }
//		finally { super.finalize(); }
//	}
	
	@Patch.Exception
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(WorldEvent.Unload event) {
		if (SideHelper.isServer())
			IAsyncThreadListener.class.cast(event.getWorld()).running().set(Boolean.FALSE);
	}
	
	public void loadEntities(Collection<Entity> entityCollection)
	{
		for (Entity entity : Lists.newArrayList(entityCollection))
		{
			if (this.canAddEntity(entity) && !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, this)))
			{
				this.loadedEntityList.add(entity);
				this.onEntityAdded(entity);
			}
		}
	}

	public boolean canAddEntity(Entity entityIn)
	{
		if (entityIn.isDead)
		{
			WorldServer.LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityList.getKey(entityIn));
			return false;
		}
		else
		{
			UUID uuid = entityIn.getUniqueID();

			if (this.entitiesByUuid.containsKey(uuid))
			{
				Entity entity = this.entitiesByUuid.get(uuid);

				if (this.unloadedEntityList.contains(entity))
				{
					this.unloadedEntityList.remove(entity);
				}
				else
				{
//					Thread.dumpStack();
					System.out.println(entityIn + " - " + getChunk(entityIn.getPosition()));
					if (!(entityIn instanceof EntityPlayer))
					{
						WorldServer.LOGGER.warn("Keeping entity {} that already exists with UUID {}", EntityList.getKey(entity), uuid.toString());
						return false;
					}

					WorldServer.LOGGER.warn("Force-added player with duplicate UUID {}", (Object)uuid.toString());
				}

				this.removeEntityDangerously(entity);
			}

			return true;
		}
	}
	
}
