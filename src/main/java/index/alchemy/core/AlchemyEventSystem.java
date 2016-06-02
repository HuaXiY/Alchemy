package index.alchemy.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import index.alchemy.annotation.Init;
import index.alchemy.annotation.KeyEvent;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.development.DMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Init(state = ModState.CONSTRUCTED)
public class AlchemyEventSystem implements IGuiHandler {
	
	public static final AlchemyEventSystem INSTANCE = new AlchemyEventSystem();
	
	public static enum EventType {
		EVENT_BUS,
		TERRAIN_GEN_BUS,
		ORE_GEN_BUS
	}
	
	private static class KeyBindingHandle {
		
		private final KeyBinding binding;
		private final IInputHandle handle;
		private final Method method;
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle handle, Method method) {
			this.binding = binding;
			this.handle = handle;
			this.method = method;
		}
		
		public KeyBinding getBinding() {
			return binding;
		}
		
		public IInputHandle getHandle() {
			return handle;
		}
		
		public Method getMethod() {
			return method;
		}
		
	}
	
	public static final EventType[]
			EVENT_BUS = new EventType[]{ EventType.EVENT_BUS },
			TERRAIN_GEN_BUS = new EventType[]{ EventType.TERRAIN_GEN_BUS },
			ORE_GEN_BUS = new EventType[]{ EventType.ORE_GEN_BUS };
	
	private static final List<IPlayerTickable> 
			SERVER_TICKABLE = new ArrayList<IPlayerTickable>(),
			CLIENT_TICKABLE = new ArrayList<IPlayerTickable>();
	
	private static final List<IContinuedRunnable>
			SERVER_RUNNABLE = new LinkedList<IContinuedRunnable>(),
			SERVER_TEMP = new LinkedList<IContinuedRunnable>(),
			CLIENT_RUNNABLE = new LinkedList<IContinuedRunnable>(),
			CLIENT_TEMP = new LinkedList<IContinuedRunnable>();
	
	private static final List<IGuiHandle> GUI_HANDLES = new ArrayList<IGuiHandle>();
	
	private static final Set<Object> HOOK_INPUT = new HashSet<Object>();
	
	private static boolean hookInputState = false;
	
	private static final List<KeyBindingHandle> KEY_HANDELS = new ArrayList<KeyBindingHandle>();
	
	public static void registerPlayerTickable(IPlayerTickable tickable) {
		AlchemyModLoader.checkState();
		if (tickable.getSide() != null)
			(tickable.getSide() == Side.SERVER ? SERVER_TICKABLE : CLIENT_TICKABLE).add(tickable);
		else {
			SERVER_TICKABLE.add(tickable);
			CLIENT_TICKABLE.add(tickable);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerTick(PlayerTickEvent event) {
		for (IPlayerTickable tickable : event.side.isServer() ? SERVER_TICKABLE : CLIENT_TICKABLE)
			tickable.onTick(event.player, event.phase);
	}
	
	public static void addDelayedRunnable(final IPhaseRunnable runnable, final int tick, Side side) {
		addContinuedRunnable(new IContinuedRunnable() {
			int c_tick = tick;
			@Override
			public boolean run(Phase phase) {
				runnable.run(phase);
				return c_tick < 1 || phase == Phase.START && --c_tick < 1;
			}
		}, side);
	}
	
	public static void addContinuedRunnable(final IIndexRunnable runnable, final int tick, Side side) {
		addContinuedRunnable(new IContinuedRunnable() {
			int c_tick = tick;
			@Override
			public boolean run(Phase phase) {
				runnable.run(tick - c_tick, phase);
				return c_tick < 1 || phase == Phase.START && --c_tick < 1;
			}
		}, side);
	}
	
	public static void addContinuedRunnable(IContinuedRunnable runnable, Side side) {
		if (side == null) {
			SERVER_RUNNABLE.add(runnable);
			CLIENT_RUNNABLE.add(runnable);
		} else
			(side.isServer() ? SERVER_RUNNABLE : CLIENT_RUNNABLE).add(runnable);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onServerTick(ServerTickEvent event) {
		String flag = "4";
		if (!System.getProperty("index.alchemy.runtime.debug", "").equals(flag)) {
			// runtime do some thing
			{
				System.out.println(AlchemyCapabilityLoader.time_leap);
				System.out.println(AlchemyCapabilityLoader.bauble);
			}
			System.setProperty("index.alchemy.runtime.debug", flag);
		}
		if (!SERVER_RUNNABLE.isEmpty()) {
			for (IContinuedRunnable runnable : SERVER_RUNNABLE)
				if (runnable.run(event.phase))
					SERVER_TEMP.add(runnable);
			SERVER_RUNNABLE.removeAll(SERVER_TEMP);
			SERVER_TEMP.clear();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClientTick(ClientTickEvent event) {
		if (!CLIENT_RUNNABLE.isEmpty()) {
			for (IContinuedRunnable runnable : CLIENT_RUNNABLE)
				if (runnable.run(event.phase))
					CLIENT_TEMP.add(runnable);
			CLIENT_RUNNABLE.removeAll(CLIENT_TEMP);
			CLIENT_TEMP.clear();
		}
	}
	
	@SubscribeEvent
	public void onInitHook(InitHookEvent event) {
		AlchemyModLoader.logger.info("    init: <" + event.init.getClass().getName() + "> " + event.init);
		if (AlchemyModLoader.use_dmain)
			DMain.init(event.init);
	}
	
	public static synchronized void registerGuiHandle(IGuiHandle handle) {
		AlchemyModLoader.checkState();
		GUI_HANDLES.add(handle);
	}
	
	public static int getGuiIdByGuiHandle(IGuiHandle handle) {
		return GUI_HANDLES.indexOf(handle);
	}
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return GUI_HANDLES.get(id).getServerGuiElement(player, world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		return GUI_HANDLES.get(id).getClientGuiElement(player, world, x, y, z);
	}
	
	@SideOnly(Side.CLIENT)
	public static void addInputHook(Object obj) {
		if (HOOK_INPUT.isEmpty())
			hookInputState = true;
		HOOK_INPUT.add(obj);
	}
	
	@SideOnly(Side.CLIENT)
	public static void removeInputHook(Object obj) {
		HOOK_INPUT.remove(obj);
		if (HOOK_INPUT.isEmpty())
			hookInputState = false;
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerInputHandle(final IInputHandle handle) {
		for (KeyBinding binding : handle.getKeyBindings()) {
			if (!ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, binding))
				ClientRegistry.registerKeyBinding(binding);
			String description = binding.getKeyDescription();
			for (final Method method : handle.getClass().getMethods()) {
				KeyEvent event = method.getAnnotation(KeyEvent.class);
				if (event != null)
					for (String value : event.value())
						if (value.equals(description))
							KEY_HANDELS.add(new KeyBindingHandle(binding, handle, method));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onKeyInput(KeyInputEvent event) {
		if (hookInputState)
			KeyBinding.unPressAllKeys();
		else 
			for (KeyBindingHandle handle : KEY_HANDELS)
				if (Keyboard.isKeyDown(handle.getBinding().getKeyCode()))
					try {
						handle.getMethod().invoke(handle.getHandle(), handle.getBinding());
					} catch (Exception e) {
						throw new AlchemyRuntimeExcption(e);
					}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onMouseInput(MouseEvent event) {
		if (hookInputState)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderBar(RenderGameOverlayEvent.Pre event) {
		if (event.getType() == ElementType.ALL)
			HUDManager.render();
	}
	
	public static void registerEventHandle(IEventHandle handle) {
		AlchemyModLoader.checkState();
		for (EventType type : handle.getEventType()) {
			if (type == EventType.EVENT_BUS)
				MinecraftForge.EVENT_BUS.register(handle);
			else if (type == EventType.TERRAIN_GEN_BUS)
				MinecraftForge.TERRAIN_GEN_BUS.register(handle);
			else if (type == EventType.ORE_GEN_BUS)
				MinecraftForge.ORE_GEN_BUS.register(handle);
		}
	}
	
	public static void init() {}
	
	public AlchemyEventSystem() {
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(AlchemyModLoader.instance(), this);
	}

}