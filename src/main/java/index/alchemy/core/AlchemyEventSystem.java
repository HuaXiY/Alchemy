package index.alchemy.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import index.alchemy.animation.StdCycle;
import index.alchemy.api.AlchemyRegistry;
import index.alchemy.api.IAlchemyRecipe;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.ICycle;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IFieldAccess;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IMaterialConsumer;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.ITileEntity;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Render;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.entity.control.SingleProjection;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.util.Always;
import index.alchemy.util.Counter;
import index.alchemy.util.ReflectionHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Loading
@Listener
@ThreadSafe
@Hook.Provider
@Field.Provider
@Init(state = ModState.CONSTRUCTED)
public enum AlchemyEventSystem implements IGuiHandler, IInputHandle {
	
	INSTANCE;
	
	public static AlchemyEventSystem instance() { return INSTANCE; }
	
	public static enum EventType {
		
		EVENT_BUS(MinecraftForge.EVENT_BUS),
		TERRAIN_GEN_BUS(MinecraftForge.TERRAIN_GEN_BUS),
		ORE_GEN_BUS(MinecraftForge.ORE_GEN_BUS);
		
		private EventBus bus;
		
		EventType(EventBus bus) { this.bus = bus; }
		
		public EventBus bus() { return bus; }
		
	}
	
	@SideOnly(Side.CLIENT)
	private static final class KeyBindingHandle {
		
		private final KeyBinding binding;
		private final IInputHandle target;
		private final Method method;
		private final boolean ignoreHook;
		private final Function<KeyBinding, Void> handler;
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle target, Method method, boolean ignoreHook) {
			this.binding = binding;
			this.target = target;
			this.method = method;
			this.ignoreHook = ignoreHook;
			this.handler = AlchemyEngine.getASMClassLoader().createWrapper(method, target);
		}
		
		public KeyBinding getBinding() { return binding; }
		
		public IInputHandle getTarget() { return target; }
		
		public Method getMethod() { return method; }
		
		public boolean isIgnoreHook() { return ignoreHook; }
		
		public void handle() {
			if (handler != null)
				try {
					handler.apply(binding);
				} catch (Exception e) { AlchemyRuntimeException.onException(e); }
		}
		
	}
	
	public static final EventType[]
			EVENT_BUS = new EventType[]{ EventType.EVENT_BUS },
			TERRAIN_GEN_BUS = new EventType[]{ EventType.TERRAIN_GEN_BUS },
			ORE_GEN_BUS = new EventType[]{ EventType.ORE_GEN_BUS };
	
	private static final List<IPlayerTickable> 
			server_tickable = Lists.newArrayList(),
			client_tickable = Lists.newArrayList();
	
	private static final Map<Side, List<IContinuedRunnable>>
			runnable_mapping = makeSyncRunnableMapping(),
			runnable_mapping_buffer = makeSyncRunnableMapping();
	
	private static final Map<Side, Phase>
			phase_mapping = Arrays.stream(Side.values()).collect(() -> Maps.newEnumMap(Side.class),
					(m, s) -> m.put(s, Phase.START), Map::putAll);
	
	private static final List<IGuiHandle> gui_handle = Lists.newArrayList();
	
	private static final Set<Object> hook_input = Sets.newHashSet();
	
	private static volatile boolean hookInputState = false;
	
	private static final Set<String> texture_set = Sets.newHashSet();
	
	private static final List<KeyBindingHandle> key_handle = Lists.newArrayList();
	
	public static Map<Side, List<IContinuedRunnable>> makeSyncRunnableMapping() {
		return Arrays.stream(Side.values()).collect(() -> Maps.newEnumMap(Side.class),
				(m, s) -> m.put(s, Collections.synchronizedList(Lists.newLinkedList())), Map::putAll);
	}
	
	public static void registerPlayerTickable(IPlayerTickable tickable) {
		AlchemyModLoader.checkState();
		if (tickable.getSide() != null)
			(tickable.getSide() == Side.SERVER ? server_tickable : client_tickable).add(tickable);
		else {
			server_tickable.add(tickable);
			client_tickable.add(tickable);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlayerTick(PlayerTickEvent event) {
		String flag = "135";
//		if (!System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
//			AlchemyRegistry.registerAlchemyRecipe(new IAlchemyRecipe() {
//				
//				@Override
//				public int getAlchemyTime() {
//					return 20 * 10;
//				}
//				
//				@Override
//				public ItemStack getAlchemyResult(World world, BlockPos pos) {
//					return new ItemStack(AlchemyItemLoader.amulet_heal);
//				}
//				
//				@Override
//				public ResourceLocation getAlchemyName() {
//					return new AlchemyResourceLocation("Test");
//				}
//				
//				@Override
//				public List<IMaterialConsumer> getAlchemyMaterials() {
//					return Lists.newArrayList(Always.generateMaterialConsumer(new ItemStack(Items.DIAMOND)));
//				}
//				
//				@Override
//				public Fluid getAlchemyFluid() {
//					return FluidRegistry.WATER;
//				}
//				
//				@Override
//				public int getAlchemyColor() {
//					return 0xFFFFFF;
//				}
//			});
//			System.setProperty("index.alchemy.runtime.debug.player", flag);
//		}
		if (Always.isClient() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			// runtime do some thing
			{
//				System.out.println(event.player.isWearing(EnumPlayerModelParts.HAT));
				//EntityLivingBase entity = IFollower.follower.get(event.player);
				//SingleProjection.cutoverState();
//				System.out.println(entity);
				//SingleProjection.projection(entity);
//				for (EntityLivingBase living : event.player.worldObj.getEntitiesWithinAABB(EntityMagicPixie.class, AABBHelper.getAABBFromEntity(event.player, 16))) {
//					SingleProjection.projectionFollower(living);
//				}
				//registerEventHandle(new RTest());
				//System.out.println(((ItemRingSpace)AlchemyItemLoader.ring_space).getInventory(AlchemyItemLoader.ring_space.getFormLiving(Minecraft.getMinecraft().thePlayer)));;
				//Minecraft.getMinecraft().thePlayer.playSound(AlchemySoundLoader.record_re_awake, 1, 1);
				//BiomesOPlenty.proxy.spawnParticle(BOPParticleTypes.PLAYER_TRAIL, player.posX + offsetX, ((int)player.posY) + groundYOffset + 0.01, player.posZ + offsetZ, "dev_trail");
				//GL11.glPushMatrix();
				//GL11.glTranslated(player.posX, player.posY, player.posZ);
				//RenderHelper.Draw2D.drawRound(3, 0, 360, 1, 0xFF66CCFF, false);
				//GL11.glPopMatrix();
				//Minecraft.getMinecraft().effectRenderer.addEffect(new FXWisp(player.worldObj, player.posX
				//		+ offsetX, player.posY + offsetY, player.posZ + offsetZ));
				
				/*ItemStack item = Minecraft.getMinecraft().thePlayer.getHeldItemMainhand();
				if (item != null) {
					Block block = Block.getBlockFromItem(item.getItem());
					if (block != null)
						System.out.println(Block.getIdFromBlock(block));
				}*/
				//System.out.println(Tool.get(Names.Name.class, 1, Tool.get(Names.class, 3)));
			}
			System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		if (Always.isServer() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
//			EntityPlayer player = event.player;
//			player.changeDimension(10);
//			System.out.println(player.worldObj.provider.getDimensionType());
////			Item item = player.getHeldItemMainhand().getItem();
//			if (item instanceof IItemThirst) {
//				((IItemThirst) item).setThirst(2);
//				((IItemThirst) item).setHydration(4);
//			}
//			for (int i = 0; i < 200; i++) {
//				EntityChicken chicken = new EntityChicken(player.worldObj);
//				chicken.setPosition(player.posX, player.posY, player.posZ);
//				player.worldObj.spawnEntityInWorld(chicken);
//			}
			
//			player.worldObj.getEntitiesWithinAABB(EntityChicken.class, AABBHelper.getAABBFromEntity(player, 128))
//				.forEach(EntityLivingBase::onKillCommand);
			//System.out.println(Tool.<List<String>>$(FMLCommonHandler.instance(), "brandingsNoMC"));
				
			//player.changeDimension(10);
//			WorldServer world = DimensionManager.getWorld(1);
//			System.out.println(world);
//			if (world != null)
//				world.getDefaultTeleporter().placeInExistingPortal(player, player.rotationYaw);
//			player.changeDimension(1);
//			Entity entity = Tool.$("Lcom.brandon3055.draconicevolution.entity.EntityChaosGuardian", "new", player.worldObj);
//			entity.posX = player.posX;
//			entity.posY = player.posY - 8;
//			entity.posZ = player.posZ;
//			player.worldObj.spawnEntityInWorld(entity);
			
//			List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>();
//			int update[] = FXUpdateHelper.getIntArrayByArgs(TileEntityCauldron.FX_KEY_GATHER, 360, 300);
//			for (int i = 0; i < 1; i++)
//				d6iaps.add(new Double6IntArrayPackage(
//						player.posX + 6 - player.worldObj.rand.nextFloat() * 12,
//						player.posY + 6 - player.worldObj.rand.nextFloat() * 12,
//						player.posZ + 6 - player.worldObj.rand.nextFloat() * 12, 0, 0, 0, update));
//			AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
//					AABBHelper.getAABBFromEntity(player, AlchemyNetworkHandler.getParticleRange()), player.worldObj, d6iaps);
			//System.out.println(Arrays.toString(new AttachCapabilitiesEvent(null).getListenerList().getListeners(0)));
			//event.player.worldObj.setBlockState(event.player.getPosition(), AlchemyBlockLoader.silver_ore.getDefaultState());
			//System.out.println(DimensionManager.getWorld(10));ItemFlintAndSteel BlockFire
			//System.out.println(DimensionManager.getWorld(10).getDefaultTeleporter()); 
			//event.player.changeDimension(0);
			//System.out.println(DimensionManager.getWorld(1).getDefaultTeleporter().placeInExistingPortal(event.player, event.player.rotationYaw));
			//new MagicTeleportDirectional(1).apply(null, event.player, 1);
			//System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		for (IPlayerTickable tickable : event.side.isServer() ? server_tickable : client_tickable)
			tickable.onTick(event.player, event.phase);
	}
	
	public static void addDelayedRunnable(IPhaseRunnable runnable, int tick) {
		addDelayedRunnable(Always.getSide(), runnable, tick);
	}
	
	public static void addDelayedRunnable(Side side, IPhaseRunnable runnable, int tick) {
		addContinuedRunnable(side, new IContinuedRunnable() {
			
			int c_tick = tick;
			
			@Override
			public boolean run(Phase phase) {
				if (c_tick < 1 || phase == Phase.START && --c_tick < 1) {
					runnable.run(phase);
					return true;
				}
				return false;
			}
			
		});
	}
	
	public static void addCounterRunnable(IPhaseRunnable runnable, Counter counter, int total) {
		addCounterRunnable(Always.getSide(), runnable, counter, total);
	}
	
	public static void addCounterRunnable(Side side, IPhaseRunnable runnable, Counter counter, int total) {
		addContinuedRunnable(side, new IContinuedRunnable() {
			
			int count = -1;
			
			@Override
			public boolean run(Phase phase) {
				if (counter.getAsBoolean() && ++count < total) {
					runnable.run(phase);
					return false;
				}
				return count >= total;
			}
			
		});
	}
	
	public static void addContinuedRunnable(IIndexRunnable runnable, int tick) {
		addContinuedRunnable(Always.getSide(), runnable, tick);
	}
	
	public static void addContinuedRunnable(Side side, IIndexRunnable runnable, int tick) {
		addContinuedRunnable(side, new IContinuedRunnable() {
			
			int c_tick = tick;
			
			@Override
			public boolean run(Phase phase) {
				runnable.run(tick - c_tick, phase);
				return c_tick < 1 || phase == Phase.START && --c_tick < 1;
			}
			
		});
	}
	
	public static void addContinuedRunnable(IContinuedRunnable runnable) {
		addContinuedRunnable(Always.getSide(), runnable);
	}
	
	public static void addContinuedRunnable(Side side, IContinuedRunnable runnable) {
		runnable_mapping_buffer.get(side).add(runnable);
	}
	
	public static void onRunnableTick(Side side, Phase phase) {
		updatePhase(side, phase);
		List<IContinuedRunnable> list = runnable_mapping.get(side), buffer = runnable_mapping_buffer.get(side);
		synchronized (buffer) {
			list.addAll(buffer);
			buffer.clear();
		}
		for (Iterator<IContinuedRunnable> iterator = list.iterator(); iterator.hasNext();)
			if (iterator.next().run(phase))
				iterator.remove();
	}
	
	public static void updatePhase(Side side, Phase phase) {
		phase_mapping.put(side, phase);
	}
	
	public static Phase getPhase() {
		return phase_mapping.get(Always.getSide());
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onServerTick(ServerTickEvent event) {
		String flag = "20";
		if (!System.getProperty("index.alchemy.runtime.debug.server", "").equals(flag)) {
			// runtime do some thing
			{
				//System.out.println(EventHelper.getAllHandler(PlayerDropsEvent.class, 0));
			}
			System.setProperty("index.alchemy.runtime.debug.server", flag);
		}
		onRunnableTick(event.side, event.phase);
	}
	
	@SideOnly(Side.CLIENT)
	private static long lastTickTime = -1;
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onClientTick(ClientTickEvent event) {
		String flag = "43";
		if (!System.getProperty("index.alchemy.runtime.debug.client", "").equals(flag)) {
			// runtime do some thing
			{
//				ICycle cycle = new StdCycle().setCycles(2).setLenght(10).setLoop(true);
//				System.out.println(cycle);
//				System.out.println(ReflectionHelper.shallowCopy(cycle));
//				Block.getBlockFromName(name)
//				List<EntityMagicPixie> result = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityMagicPixie.class,
//						AABBHelper.getAABBFromEntity(Minecraft.getMinecraft().thePlayer, 15));
//				if (result.size() > 0)
//					Minecraft.getMinecraft().setRenderViewEntity(result.get(0));
				
//				Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().thePlayer);
			}
			System.setProperty("index.alchemy.runtime.debug.client", flag);
		}
//		Object object = Minecraft.getMinecraft().objectMouseOver.hitInfo;
//		System.out.println(object);
//		if (object instanceof Block)
//			;
//		{
//			if (System.currentTimeMillis() - lastTickTime > 3000)
//				Minecraft.getMinecraft().effectRenderer.clearEffects(Minecraft.getMinecraft().theWorld);
//			lastTickTime = System.currentTimeMillis();
//		}
		onRunnableTick(event.side, event.phase);
	}
	
	@SubscribeEvent
	public static void onInitHook(InitHookEvent event) {
		AlchemyModLoader.info(event.init.getClass(), event.init);
	}
	
	public static synchronized void registerGuiHandle(IGuiHandle handle) {
		AlchemyModLoader.checkState();
		gui_handle.add(handle);
	}
	
	public static int getGuiIdByGuiHandle(IGuiHandle handle) {
		return gui_handle.indexOf(handle);
	}
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return gui_handle.get(id).getServerGuiElement(player, world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return gui_handle.get(id).getClientGuiElement(player, world, x, y, z);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isHookInput() {
		return hookInputState;
	}
	
	@SideOnly(Side.CLIENT)
	public static void addInputHook(Object obj) {
		if (hook_input.isEmpty())
			hookInputState = true;
		hook_input.add(obj);
		KeyBinding.unPressAllKeys();
	}
	
	@SideOnly(Side.CLIENT)
	public static void removeInputHook(Object obj) {
		hook_input.remove(obj);
		if (hook_input.isEmpty())
			hookInputState = false;
		KeyBinding.updateKeyBindState();
	}
	
	@SideOnly(Side.CLIENT)
	public static void clearInputHook() {
		hook_input.clear();
		hookInputState = false;
		KeyBinding.updateKeyBindState();
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerInputHandle(IInputHandle handle) {
		Class<?> clazz = handle.getClass();
		for (KeyBinding binding : handle.getKeyBindings()) {
			if (!ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, binding))
				ClientRegistry.registerKeyBinding(binding);
			String description = binding.getKeyDescription();
			for (Method method : clazz.getMethods()) {
				KeyEvent event = method.getAnnotation(KeyEvent.class);
				if (event != null)
					if (event.value() != null)
						if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == KeyBinding.class) {
							for (String value : event.value())
								if (value.equals(description))
									key_handle.add(new KeyBindingHandle(binding, handle, method, event.ignoreHook()));
						}
						else
							AlchemyRuntimeException.onException(new IllegalArgumentException(
											clazz + "#" + method.getName() + "() -> args != " + KeyBinding.class.getName()));
					else
						AlchemyRuntimeException.onException(new NullPointerException(
										clazz + "#" + method.getName() + "() -> @KeyEvent.value()"));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onKeyInput(KeyInputEvent event) {
		if (isHookInput())
			KeyBinding.unPressAllKeys();
		for (KeyBindingHandle handle : key_handle)
				if (isKeyHandleActive(handle))
					handle.handle();
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isKeyHandleActive(KeyBindingHandle handle) {
		return (!isHookInput() || handle.isIgnoreHook()) && handle.getBinding().getKeyConflictContext().isActive() &&
				Keyboard.isKeyDown(handle.getBinding().getKeyCode());
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isKeyBindingActive(KeyBinding binding) {
		return !isHookInput() && binding.getKeyConflictContext().isActive() && Keyboard.isKeyDown(binding.getKeyCode());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onMouseInput(MouseEvent event) {
		if (isHookInput())
			markEventCanceled(event);
	}
	
	public static final String
			KEY_DEBUG_DISABLE_HOOK = "key.debug_disable_hook",
			KEY_DEBUG_CLEAR_EFFECTS = "key.debug_clear_effects";
	
	@Override
	@SideOnly(Side.CLIENT)
	public KeyBinding[] getKeyBindings() {
		return new KeyBinding[]{
				new AlchemyKeyBinding(KEY_DEBUG_DISABLE_HOOK, Keyboard.KEY_F9),
				new AlchemyKeyBinding(KEY_DEBUG_CLEAR_EFFECTS, Keyboard.KEY_MINUS)
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(value = KEY_DEBUG_DISABLE_HOOK, ignoreHook = true)
	public void onKeyDebugDisableHookPressed(KeyBinding binding) {
		if (GuiScreen.isAltKeyDown())
			clearInputHook();
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(KEY_DEBUG_CLEAR_EFFECTS)
	public void onKeyDebugClearEffects(KeyBinding binding) {
		if (GuiScreen.isAltKeyDown())
			Minecraft.getMinecraft().effectRenderer.clearEffects(Minecraft.getMinecraft().theWorld);
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.Minecraft#func_184124_aB", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result runTickMouse(Minecraft minecraft) {
		if (isHookInput())
			return Hook.Result.NULL;
		else
			return Hook.Result.VOID;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.util.MouseHelper#func_74374_c", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result mouseXYChange(MouseHelper helper) {
		if (isHookInput()) {
			Mouse.getDX();
			Mouse.getDY();
			return Hook.Result.NULL;
		} else
			return Hook.Result.VOID;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void renderBar(RenderGameOverlayEvent.Pre event) {
		if (SingleProjection.isProjectionState()) {
			markEventCanceled(event);
			HUDManager.setupOverlayRendering();
		} else if (event.getType() == ElementType.ALL)
			HUDManager.render();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onTextureStitch_Pre(TextureStitchEvent.Pre event) {
		texture_set.stream().map(ResourceLocation::new).forEach(event.getMap()::registerSprite);
	}
	
	public static void registerEventHandle(IEventHandle handle) {
		Arrays.stream(handle.getEventTypes()).map(EventType::bus).forEach(bus -> bus.register(handle));
	}
	
	public static void registerTileEntity(ITileEntity tile) {
		GameRegistry.registerTileEntity(tile.getTileEntityClass(), tile.getTileEntityName());
	}
	
	public static final IFieldAccess<Event, Boolean> markIgnore = null;
	
	@Hook("net.minecraftforge.fml.common.eventhandler.ASMEventHandler#invoke")
	public static Hook.Result invoke(ASMEventHandler handler, Event event) {
		return Tool.isNullOr(markIgnore.get(event), false) ? Hook.Result.NULL : Hook.Result.VOID;
	}
	
	public static void markEventIgnore(Event event) {
		markIgnore.set(event, true);
	}
	
	public static void markEventIgnore(Event event, Event.Result result) {
		event.setResult(result);
		markIgnore.set(event, true);
	}
	
	public static void markEventCanceled(Event event) {
		event.setCanceled(true);
		markIgnore.set(event, true);
	}
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		AlchemyInitHook.init(instance());
		NetworkRegistry.INSTANCE.registerGuiHandler(AlchemyConstants.MOD_ID, instance());
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Listener listener = clazz.getAnnotation(Listener.class);
		if (listener != null)
			for (Listener.Type type : listener.value())
				type.getEventBus().register(clazz);
		if (Always.isClient()) {
			Texture texture = clazz.getAnnotation(Texture.class);
			if (texture != null)
				if (texture.value() != null)
					for (String res : texture.value())
						texture_set.add(res);
				else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Texture.value()"));
			Render render = clazz.getAnnotation(Render.class);
			if (render != null)
				if (render.value() != null) {
					if (Tool.isSubclass(TileEntity.class, render.value()) && Tool.isSubclass(TileEntitySpecialRenderer.class, clazz))
						try {
							ClientRegistry.bindTileEntitySpecialRenderer(render.value(), (TileEntitySpecialRenderer) clazz.newInstance());
						} catch (Exception e) { AlchemyRuntimeException.onException(e); }
					else
						AlchemyRuntimeException.onException(new RuntimeException(
								"Can't bind Render: " + render.value().getName() + " -> " + clazz.getName()));
				} else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Render.value()"));
		}
	}
	
}