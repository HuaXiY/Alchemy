package index.alchemy.dlcs.skin.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Message;
import index.alchemy.client.MemoryTexture;
import index.alchemy.core.AlchemyEngine;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.interacting.WoodType;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.Always;
import index.alchemy.util.FileMap;
import index.alchemy.util.NBTHelper;
import index.alchemy.util.Tool;
import index.alchemy.util.cache.AsyncLocalFileCache;
import index.project.version.annotation.Alpha;
import io.netty.buffer.ByteBuf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.lambda.Unchecked;

import static index.alchemy.dlcs.skin.core.SkinCore.*;

@Alpha
@Init(state = ModState.INITIALIZED)
@DLC(id = DLC_ID, name = DLC_NAME, version = DLC_VERSION, mcVersion = "[1.10.2]")
public class SkinCore {
	
	public static final String
			DLC_ID = "skin",
			DLC_NAME = "Skin",
			DLC_VERSION = "0.0.1-dev";
	
	@Message(Side.SERVER)
	public static class UpdateSkinServer implements IMessage, IMessageHandler<UpdateSkinServer, IMessage> {
		
		public String type;
		public byte[] data;
		
		public UpdateSkinServer() { }
		
		public UpdateSkinServer(String type, byte data[]) {
			this.type = type;
			this.data = data;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			type = ByteBufUtils.readUTF8String(buf);
			data = new byte[buf.readInt()];
			buf.readBytes(data);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			ByteBufUtils.writeUTF8String(buf, type);
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}

		@Override
		public IMessage onMessage(UpdateSkinServer message, MessageContext ctx) {
			SkinInfo info = ctx.getServerHandler().playerEntity.getCapability(SkinCore.skin_info, null);
			info.skin_type = message.type;
			info.skin_data = message.data;
			updatePlayerSkin(ctx.getServerHandler().playerEntity);
			return null;
		}
		
	}
	
	@Message(Side.CLIENT)
	public static class UpdateSkinClient implements IMessage, IMessageHandler<UpdateSkinClient, IMessage> {
		
		public int id;
		public String name, type;
		public byte[] data;
		
		public UpdateSkinClient() { }
		
		public UpdateSkinClient(int id, String name, String type, byte data[]) {
			this.id = id;
			this.name = name;
			this.type = Tool.isEmptyOr(type, "");
			this.data = Tool.isNullOr(data, () -> new byte[0]);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readInt();
			name = ByteBufUtils.readUTF8String(buf);
			type = ByteBufUtils.readUTF8String(buf);
			data = new byte[buf.readInt()];
			buf.readBytes(data);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(id);
			ByteBufUtils.writeUTF8String(buf, name);
			ByteBufUtils.writeUTF8String(buf, type);
			buf.writeInt(data.length);
			buf.writeBytes(data);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(UpdateSkinClient message, MessageContext ctx) {
			if (name != null)
				onSkinCallback(this);
			if (id != -233)
				AlchemyEventSystem.addDelayedRunnable(p -> {
					Entity entity = message.id < 0 ? GuiWardrobe.player : Always.findEntityFormClientWorld(message.id);
					if (entity != null) {
						SkinInfo info = entity.getCapability(SkinCore.skin_info, null);
						if (info != null) {
							ResourceLocation skin = new ResourceLocation("skin:" + entity.getName());
							Minecraft.getMinecraft().getTextureManager().deleteTexture(skin);
							if (message.data.length > 0) {
								Minecraft.getMinecraft().getTextureManager().loadTexture(skin, new MemoryTexture(message.data));
								info.skin_mapping.put(Type.SKIN, skin);
								info.skin_type = message.type;
							} else {
								info.skin_mapping.put(Type.SKIN, null);
								info.skin_type = null;
							}
						}
					}
				}, 0);
			return null;
		}
		
		public UpdateSkinServer toUpdateServerMessage() {
			return new UpdateSkinServer(type, data);
		}
		
	}
	
	@Message(Side.SERVER)
	public static class RequestSkinData implements IMessage, IMessageHandler<RequestSkinData, UpdateSkinClient> {
		
		public String name;
		
		public RequestSkinData() { }
		
		public RequestSkinData(String name) {
			this.name = name;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			name = ByteBufUtils.readUTF8String(buf);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			ByteBufUtils.writeUTF8String(buf, name);
		}
		
		@Override
		public UpdateSkinClient onMessage(RequestSkinData message, MessageContext ctx) {
			SkinInfo info = getSkinInfoFormPlayerName(message.name);
			return info != null ? new UpdateSkinClient(-233, message.name, info.skin_type, info.skin_data) : null;
		}
		
	}
	
	public static final File CACHE_PATH = new File(AlchemyEngine.getMinecraftDir(), "skin/cache");
	
	private static final Logger logger = LogManager.getLogger(SkinCore.class.getSimpleName());
	
	public static final Optional<AsyncLocalFileCache> async_cache = Optional.ofNullable(newAsyncLocalFileCache(CACHE_PATH));
	
	@Nullable
	private static final AsyncLocalFileCache newAsyncLocalFileCache(File path) {
		FileMap folderMap = FileMap.newFileMap(path);
		if (folderMap == null) {
			logger.warn("Invalid path: " + path);
			return null;
		}
		return new AsyncLocalFileCache(path, folderMap, SkinCore::onSkinMiss);
	}
	
	private static final Map<String, Long> time_mapping = Maps.newHashMap();
	
	private static File onSkinMiss(String name, AsyncLocalFileCache asyncCache) {
		Long lastTime = time_mapping.get(name);
		if (lastTime != null)
			if (System.currentTimeMillis() - lastTime < 1000 * 60 * 60)
				return null;
		time_mapping.put(name, System.currentTimeMillis());
		AlchemyNetworkHandler.network_wrapper.sendToServer(new RequestSkinData(name));
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	private static void onSkinCallback(UpdateSkinClient message) {
		if (async_cache.isPresent()) {
			AsyncLocalFileCache cache = async_cache.get();
			File cacheFile = new File(cache.getCachePath(), message.name + ".png");
			File resule = cache.add(message.name, cacheFile);
			if (cacheFile != resule)
				Unchecked.runnable(() -> CompressedStreamTools.writeCompressed((NBTTagCompound) new SkinInfo(message.data, message.type)
						.serializeNBT(), new FileOutputStream(cacheFile)), logger::warn).run();
			else
				logger.warn("Can't create new file: " + cacheFile);
		}
	}
	
	@Nullable
	public static SkinInfo getSkinInfoFormPlayerName(String name) {
		EntityPlayerMP playerMP = FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUsername(name);
		if (playerMP != null) {
			SkinInfo info = playerMP.getCapability(skin_info, null);
			return info != null ? info : null;
		}
		NBTTagCompound nbt = NBTHelper.getNBTFromPlayerName(name);
		if (nbt != null) {
			
		}
		return null;
	}
	
	@CapabilityInject(SkinCapability.class)
	public static final Capability<SkinInfo> skin_info = null;
	
	public static final CommandBase update_skin = new CommandUpdateSkin();
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		WoodType.stream().map(BlockWardrobe::new).forEach(b ->
				GameRegistry.addRecipe(new ItemStack(b), "BAB", "B B", "BAB", 'B', b.type.log, 'A', b.type.plank));
		if (Always.isClient())
			registerModelLoader();
	}
	
	@SideOnly(Side.CLIENT)
	private static void registerModelLoader() {
		ModelLoaderRegistry.registerLoader(new WardrobeModelLoader());
	}
	
	@EventHandler
	@SideOnly(Side.CLIENT)
	public void onFMLLoadComplete(FMLLoadCompleteEvent event) {
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.loadTexture(GuiWardrobe.BACKGROUND_TEXTURES, new SimpleTexture(GuiWardrobe.BACKGROUND_TEXTURES));
		textureManager.loadTexture(GuiWardrobe.BUTTON_TEXTURES, new SimpleTexture(GuiWardrobe.BUTTON_TEXTURES));
	}
	
	public static void updatePlayerItselfSkin(EntityPlayer player) {
		SkinInfo info = player.getCapability(skin_info, null);
		AlchemyNetworkHandler.network_wrapper.sendTo(new UpdateSkinClient(player.getEntityId(), player.getName(),
				info.skin_type, info.skin_data), (EntityPlayerMP) player);
	}
	
	public static void updatePlayerSkin(EntityPlayer player) {
		SkinInfo info = player.getCapability(skin_info, null);
		for (EntityPlayer other : ((WorldServer) player.worldObj).getEntityTracker().getTrackingPlayers(player))
			AlchemyNetworkHandler.network_wrapper.sendTo(new UpdateSkinClient(player.getEntityId(), player.getName(),
					info.skin_type, info.skin_data), (EntityPlayerMP) other);
		updatePlayerItselfSkin(player);
	}
	
	public static void updatePlayerSkinTracking(EntityPlayer player) {
		for (EntityPlayer other : ((WorldServer) player.worldObj).getEntityTracker().getTrackingPlayers(player)) {
			SkinInfo info = other.getCapability(skin_info, null);
			AlchemyNetworkHandler.network_wrapper.sendTo(new UpdateSkinClient(other.getEntityId(), other.getName(),
					info.skin_type, info.skin_data), (EntityPlayerMP) player);
		}
		updatePlayerSkin(player);
	}
	
	@SideOnly(Side.CLIENT)
	public static void updateSkin(String type, byte data[], boolean sendToServer) {
		UpdateSkinClient message = new UpdateSkinClient(Minecraft.getMinecraft().thePlayer.getEntityId(),
				Minecraft.getMinecraft().thePlayer.getName(), type, data);
		message.onMessage(message, null);
		if (sendToServer)
			AlchemyNetworkHandler.network_wrapper.sendToServer(message.toUpdateServerMessage());
	}

}
