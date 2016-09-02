package index.alchemy.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import index.alchemy.api.Always;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Render;
import index.alchemy.api.annotation.Texture;
import index.alchemy.client.LocalTexture;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.development.DMain;
import index.alchemy.util.Tool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.Event;
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

@Loading
@Init(state = ModState.CONSTRUCTED)
public class AlchemyEventSystem implements IGuiHandler {
	
	private static AlchemyEventSystem instance;
	
	public static AlchemyEventSystem getInstance() {
		return instance;
	}
	
	public static enum EventType {
		EVENT_BUS,
		TERRAIN_GEN_BUS,
		ORE_GEN_BUS
	}
	
	@SideOnly(Side.CLIENT)
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
			server_tickable = new ArrayList<IPlayerTickable>(),
			client_tickable = new ArrayList<IPlayerTickable>();
	
	private static final Map<Side, List<IContinuedRunnable>> 
			runnable_mapping = new HashMap<Side, List<IContinuedRunnable>>();
	static {
		for (Side side : Side.values())
			runnable_mapping.put(side, Collections.synchronizedList(new LinkedList<IContinuedRunnable>()));
	}
	
	private static final List<IGuiHandle> gui_handle = new ArrayList<IGuiHandle>();
	
	private static final Set<Object> hook_input = new HashSet<Object>();
	
	private static boolean hookInputState = false;
	
	private static final Set<String> texture_set = new HashSet<String>();
	
	private static final List<KeyBindingHandle> key_handle = new ArrayList<KeyBindingHandle>();
	
	public static void registerPlayerTickable(IPlayerTickable tickable) {
		AlchemyModLoader.checkState();
		if (tickable.getSide() != null)
			(tickable.getSide() == Side.SERVER ? server_tickable : client_tickable).add(tickable);
		else {
			server_tickable.add(tickable);
			client_tickable.add(tickable);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerTick(PlayerTickEvent event) {
		String flag = "47";
		if (Always.isClient() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			// runtime do some thing
			{
				//EntityPlayer player = event.player;
				//float groundYOffset = 0.015625F;
				//double offsetX = 0.3 - player.worldObj.rand.nextFloat() * 0.6;
				//double offsetZ = 0.3 - player.worldObj.rand.nextFloat() * 0.6;
				//BiomesOPlenty.proxy.spawnParticle(BOPParticleTypes.PLAYER_TRAIL, player.posX + offsetX, ((int)player.posY) + groundYOffset + 0.01, player.posZ + offsetZ, "dev_trail");
				//GL11.glPushMatrix();
				//GL11.glTranslated(player.posX, player.posY, player.posZ);
				//RenderHelper.Draw2D.drawRound(3, 0, 360, 1, 0xFF66CCFF, false);
				//GL11.glPopMatrix();
				//Minecraft.getMinecraft().effectRenderer.addEffect(new FXWisp(player.worldObj, player.posX
				//		+ offsetX, ((int)player.posY) + 2, player.posZ + offsetZ));
			}
			//System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		if (Always.isServer() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			//System.out.println(DimensionManager.getWorld(10));ItemFlintAndSteel BlockFire
			//System.out.println(DimensionManager.getWorld(10).getDefaultTeleporter()); 
			//event.player.changeDimension(0);
			//System.out.println(DimensionManager.getWorld(1).getDefaultTeleporter().placeInExistingPortal(event.player, event.player.rotationYaw));
			//new MagicTeleportDirectional(1).apply(null, event.player, 1);
			System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		for (IPlayerTickable tickable : event.side.isServer() ? server_tickable : client_tickable)
			tickable.onTick(event.player, event.phase);
	}
		
	public static void addDelayedRunnable(final IPhaseRunnable runnable, final int tick) {
		addContinuedRunnable(new IContinuedRunnable() {
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
	
	public static void addContinuedRunnable(final IIndexRunnable runnable, final int tick) {
		addContinuedRunnable(new IContinuedRunnable() {
			int c_tick = tick;
			@Override
			public boolean run(Phase phase) {
				runnable.run(tick - c_tick, phase);
				return c_tick < 1 || phase == Phase.START && --c_tick < 1;
			}
		});
	}
	
	public static void addContinuedRunnable(IContinuedRunnable runnable) {
		runnable_mapping.get(Always.getSide()).add(runnable);
	}
	
	public static void onRunnableTick(Side side, Phase phase) {
		List<IContinuedRunnable> list = runnable_mapping.get(side);
		synchronized (list) {
			Iterator<IContinuedRunnable> iterator = list.iterator();
			while (iterator.hasNext())
				if (iterator.next().run(phase))
					iterator.remove();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onServerTick(ServerTickEvent event) {
		String flag = "4";
		if (!System.getProperty("index.alchemy.runtime.debug.server", "").equals(flag)) {
			// runtime do some thing
			{
			}
			System.setProperty("index.alchemy.runtime.debug.server", flag);
		}
		onRunnableTick(event.side, event.phase);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClientTick(ClientTickEvent event) {
		String flag = "9";
		if (!System.getProperty("index.alchemy.runtime.debug.client", "").equals(flag)) {
			// runtime do some thing
			{
				//System.out.println(Minecraft.getMinecraft().thePlayer.getEntityData());
			}
			System.setProperty("index.alchemy.runtime.debug.client", flag);
		}
		onRunnableTick(event.side, event.phase);
	}
	
	@SubscribeEvent
	public void onInitHook(InitHookEvent event) {
		AlchemyModLoader.logger.info("    init: <" + event.init.getClass().getName() + "> " + event.init);
		if (AlchemyModLoader.enable_dmain)
			DMain.init(event.init);
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
	public static void registerInputHandle(final IInputHandle handle) {
		Class<?> clazz = handle.getClass();
		for (KeyBinding binding : handle.getKeyBindings()) {
			if (!ArrayUtils.contains(Minecraft.getMinecraft().gameSettings.keyBindings, binding))
				ClientRegistry.registerKeyBinding(binding);
			String description = binding.getKeyDescription();
			for (Method method : clazz.getMethods()) {
				KeyEvent event = method.getAnnotation(KeyEvent.class);
				if (event != null)
					if (event.value() != null)
						if (!Modifier.isStatic(method.getModifiers()))
							if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == KeyBinding.class) {
								for (String value : event.value())
									if (value.equals(description))
										key_handle.add(new KeyBindingHandle(binding, handle, method));
							}
							else
								AlchemyRuntimeException.onException(new IllegalArgumentException(
												clazz + "#" + method.getName() + "() -> args != " + KeyBinding.class.getName()));
						else
							AlchemyRuntimeException.onException(new IllegalAccessException(
											clazz + "#" + method.getName() + "() -> is static"));
					else
						AlchemyRuntimeException.onException(new NullPointerException(
										clazz + "#" + method.getName() + "() -> @KeyEvent.value()"));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onKeyInput(KeyInputEvent event) {
		if (hookInputState)
			KeyBinding.unPressAllKeys();
		else 
			for (KeyBindingHandle handle : key_handle)
				if (Keyboard.isKeyDown(handle.getBinding().getKeyCode()))
					try {
						handle.getMethod().invoke(handle.getHandle(), handle.getBinding());
					} catch (Exception e) {
						AlchemyRuntimeException.onException(e);
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
	
	@SubscribeEvent()
	@SideOnly(Side.CLIENT)
	public void onTextureStitch_Pre(TextureStitchEvent.Pre event) {
		for (String res : texture_set)
			event.getMap().registerSprite(new ResourceLocation(res));
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
	
	public static void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		instance = new AlchemyEventSystem();
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
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
						} catch (Exception e) {
							AlchemyRuntimeException.onException(e);
						}
					else
						AlchemyRuntimeException.onException(new RuntimeException(
								"Can't bind Render: " + render.value().getName() + " -> " + clazz.getName()));
				} else
					AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Render.value()"));
		}
	}
	
	private AlchemyEventSystem() {
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(AlchemyConstants.MOD_ID, this);
	}

}