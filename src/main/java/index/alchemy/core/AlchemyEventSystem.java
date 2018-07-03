package index.alchemy.core;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IFieldAccess;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.ITileEntity;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Patch;
import index.alchemy.api.annotation.RenderBinding;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.AlchemyKeyBinding;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.entity.control.SingleProjection;
import index.alchemy.util.$;
import index.alchemy.util.Always;
import index.alchemy.util.Counter;
import index.alchemy.util.FunctionHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
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

import static index.alchemy.util.$.$;

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
	
	public static final EventBus ALCHEMY_EVENT_BUS = new EventBus();
	
	public static enum EventType {
		
		EVENT_BUS(MinecraftForge.EVENT_BUS),
		TERRAIN_GEN_BUS(MinecraftForge.TERRAIN_GEN_BUS),
		ORE_GEN_BUS(MinecraftForge.ORE_GEN_BUS),
		ALCHEMY_BUS(AlchemyEventSystem.ALCHEMY_EVENT_BUS);
		
		public static class Name {
			
			public static final String
					EVENT_BUS = "EVENT_BUS",
					TERRAIN_GEN_BUS = "TERRAIN_GEN_BUS",
					ORE_GEN_BUS = "ORE_GEN_BUS",
					ALCHEMY_BUS = "ALCHEMY_BUS";
			
		}
		
		private final EventBus bus;
		
		EventType(EventBus bus) { this.bus = bus; }
		
		public EventBus bus() { return bus; }
		
	}
	
	public static class SystemEvent extends Event {
		
		@Cancelable
		@Patch("java.lang.System")
		public static class GC extends SystemEvent {
			
			@Patch.Exception
			public GC() { }
			
			public static void gc() {
				try {
					if ((boolean) ((ClassLoader) ClassLoader.getSystemClassLoader().loadClass("net.minecraft.launchwrapper.Launch")
							.getField("classLoader").get(null)).loadClass("index.alchemy.core.AlchemyEventSystem").getMethod("gc").invoke(null))
						return;
				} catch (Exception e) {
					throw new InternalError(e);
				}
				Runtime.getRuntime().gc();
			}
			
		}
		
	}
	
	public static boolean gc() {
		return MinecraftForge.EVENT_BUS.post(new SystemEvent.GC());
	}
	
	@SubscribeEvent
	public static void onSystem_GC(SystemEvent.GC event) {
		event.setCanceled(true);
	}
	
	@SideOnly(Side.CLIENT)
	public static final class KeyBindingHandle {
		
		private final KeyBinding binding;
		private final IInputHandle target;
		private final boolean ignoreHook;
		private final Consumer<KeyBinding> handler;
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle target, Method method, boolean ignoreHook) {
			this(binding, target, ignoreHook, AlchemyEngine.getASMClassLoader().createWrapper(method, target));
		}
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle target, boolean ignoreHook, Consumer<KeyBinding> handler) {
			this.binding = binding;
			this.target = target;
			this.ignoreHook = ignoreHook;
			this.handler = handler;
		}
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle target, boolean ignoreHook, Function<KeyBinding, Void> handler) {
			this.binding = binding;
			this.target = target;
			this.ignoreHook = ignoreHook;
			this.handler = keyBinding -> handler.apply(keyBinding);
		}
		
		public KeyBinding getBinding() { return binding; }
		
		public IInputHandle getTarget() { return target; }
		
		public boolean isIgnoreHook() { return ignoreHook; }
		
		public void handle() {
			if (handler != null)
				try {
					handler.accept(binding);
				} catch (Exception e) { AlchemyRuntimeException.onException(e); }
		}
		
	}
	
	public static final EventType[]
			EVENT_BUS = new EventType[]{ EventType.EVENT_BUS },
			TERRAIN_GEN_BUS = new EventType[]{ EventType.TERRAIN_GEN_BUS },
			ORE_GEN_BUS = new EventType[]{ EventType.ORE_GEN_BUS },
			ALCHEMY_BUS = new EventType[]{ EventType.ALCHEMY_BUS };
	
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
	
	private static final Map<Object, Integer> hook_input = Maps.newHashMap();
	
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
		String flag = "0";
		if (Always.isClient() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			// runtime do some thing
			{
				
			}
			System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		if (Always.isServer() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			// runtime do some thing
			{
				
			}
			System.setProperty("index.alchemy.runtime.debug.player", flag);
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
		String flag = "1";
		if (!System.getProperty("index.alchemy.runtime.debug.server", "").equals(flag)) { 
			// runtime do some thing
			{
			}
			System.setProperty("index.alchemy.runtime.debug.server", flag);
		}
		onRunnableTick(event.side, event.phase);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onClientTick(ClientTickEvent event) {
		String flag = "181";
		if (!System.getProperty("index.alchemy.runtime.debug.client", "").equals(flag)) {
			// runtime do some thing
			{
			}
			System.setProperty("index.alchemy.runtime.debug.client", flag);
		}
		onInputHookTick();
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
	protected static void onInputHookTick() {
		hook_input.replaceAll((obj, tick) -> tick > 0 ? tick - 1 : tick);
		hook_input.values()
			.removeIf(Integer.valueOf(0)::equals);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isHookInput() {
		return hookInputState;
	}
	
	@SideOnly(Side.CLIENT)
	public static void addInputHook(Object obj) {
		addInputHook(obj, -1);
	}
	
	@SideOnly(Side.CLIENT)
	public static void addInputHook(Object obj, int tick) {
		if (hook_input.isEmpty())
			hookInputState = true;
		hook_input.put(obj, tick);
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
	public static void addKeyBindingHandle(KeyBindingHandle handle) {
		key_handle.add(handle);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.TOP)
	public static void onKeyInput(KeyInputEvent event) {
		if (isHookInput())
			KeyBinding.unPressAllKeys();
		for (KeyBindingHandle handle : key_handle)
				if (isKeyHandleActive(handle))
					handle.handle();
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isKeyHandleActive(KeyBindingHandle handle) {
		return (!isHookInput() || handle.isIgnoreHook()) && isKeyBindingActive(handle.getBinding());
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isKeyBindingActive(KeyBinding binding) {
		return binding.getKeyConflictContext().isActive() && Keyboard.isKeyDown(binding.getKeyCode());
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.TOP)
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
			Minecraft.getMinecraft().effectRenderer.clearEffects(Minecraft.getMinecraft().world);
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
	@SubscribeEvent(priority = EventPriority.BOTTOM)
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
	
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Listener listener = clazz.getAnnotation(Listener.class);
		if (listener != null) {
			for (String type : listener.value())
				Optional.ofNullable(EventType.valueOf(type)).map(EventType::bus).ifPresent(bus -> bus.register(clazz));
			Object instance = null;
			for (Method method : clazz.getMethods()) {
				Listener.EventHandler handler = method.getAnnotation(Listener.EventHandler.class);
				if (handler != null) {
					boolean isStaic = Modifier.isStatic(method.getModifiers());
					Class<?> args[] = method.getParameterTypes();
					if (args.length == 1 && $.isInstance(FMLEvent.class, args[0]))
						try {
							MethodHandle handle = AlchemyEngine.lookup().unreflect(method);
							if (!isStaic)
								handle.bindTo(instance == null ? instance = $(clazz, "new") : instance);
							AlchemyModLoader.addFMLEventCallback((Class<FMLEvent>) args[0],
									FunctionHelper.onThrowableConsumer(e -> handle.invoke(e), AlchemyRuntimeException::onException));
						} catch (IllegalAccessException e) { }
				}
			}	
		}
		if (Always.isClient()) {
			Texture texture = clazz.getAnnotation(Texture.class);
			if (texture != null)
				if (texture.value() != null)
					for (String res : texture.value())
						texture_set.add(res);
				else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Texture.value()"));
			RenderBinding render = clazz.getAnnotation(RenderBinding.class);
			if (render != null) {
				if (render.value() != null) {
					if ($.isSubclass(TileEntity.class, render.value()) && $.isSubclass(TileEntitySpecialRenderer.class, clazz))
						ClientRegistry.bindTileEntitySpecialRenderer((Class<T>) render.value(),
								(TileEntitySpecialRenderer<? super T>) $(clazz, "new"));
					else if ($.isSubclass(Entity.class, render.value()) && $.isSubclass(
							net.minecraft.client.renderer.entity.Render.class, clazz))
						RenderingRegistry.registerEntityRenderingHandler((Class<Entity>) render.value(),
								manager -> $(clazz, "new", manager));
					else
						AlchemyRuntimeException.onException(new RuntimeException(
								"Can't bind Render: " + render.value().getName() + " -> " + clazz.getName()));
				} else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Render.value()"));
			}
		}
	}
	
}