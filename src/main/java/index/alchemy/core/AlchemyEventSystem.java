package index.alchemy.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Predicate;

import index.alchemy.api.Always;
import index.alchemy.api.ICache;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IInputHandle;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.KeyEvent;
import index.alchemy.api.annotation.Listener;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Render;
import index.alchemy.api.annotation.Texture;
import index.alchemy.api.annotation.Unsafe;
import index.alchemy.client.fx.FXWisp;
import index.alchemy.client.fx.update.FXUpdateHelper;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.development.DMain;
import index.alchemy.item.ItemRingTime;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.Double6IntArrayPackage;
import index.alchemy.util.AABBHelper;
import index.alchemy.util.Tool;
import index.alchemy.util.cache.StdCache;
import net.minecraft.client.Minecraft;
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

import static org.objectweb.asm.Opcodes.*;

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
		
		private static final String HANDLER_DESC = Type.getInternalName(Predicate.class);
		private static final String HANDLER_FUNC_NAME = Predicate.class.getDeclaredMethods()[1].getName();
		private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(Predicate.class.getDeclaredMethods()[1]);
		
		private static final ICache<Method, Predicate<KeyBinding>> cache = new StdCache<Method, Predicate<KeyBinding>>();
		
		private static int id = -1;
		public static synchronized int nextId() {
			return ++id;
		}
		
		private final KeyBinding binding;
		private final IInputHandle target;
		private final Method method;
		private final Predicate<KeyBinding> handler;
		
		public KeyBindingHandle(KeyBinding binding, IInputHandle target, Method method) {
			this.binding = binding;
			this.target = target;
			this.method = method;
			this.handler = createWrapper(method, target);
		}
		
		public KeyBinding getBinding() {
			return binding;
		}
		
		public IInputHandle getTarget() {
			return target;
		}
		
		public Method getMethod() {
			return method;
		}
		
		public void handle() {
			if (handler != null)
				try {
					handler.apply(binding);
				} catch (Exception e) {
					AlchemyRuntimeException.onException(e);
				}
		}
		
		private String getUniqueName(Method callback) {
			return String.format("%s_%d_%s_%s_%s", getClass().getName(), nextId(),
					callback.getDeclaringClass().getSimpleName(),
					callback.getName(),
					callback.getParameterTypes()[0].getSimpleName());
		}
		
		@Nullable
		@Unsafe(Unsafe.ASM_API)
		private Predicate<KeyBinding> createWrapper(Method callback, Object target) {
			Predicate<KeyBinding> result = cache.get(callback);
			if (result != null)
				return result;
			
			ClassWriter cw = new ClassWriter(0);
			MethodVisitor mv;

			boolean isStatic = Modifier.isStatic(callback.getModifiers());
			String name = getUniqueName(callback);
			String desc = name.replace('.',  '/');
			String instType = Type.getInternalName(callback.getDeclaringClass());
			String callType = Type.getInternalName(callback.getParameterTypes()[0]);
			String handleName = callback.getName();
			String handleDesc = Type.getMethodDescriptor(callback);

			cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, desc, null, "java/lang/Object", new String[]{ HANDLER_DESC });
			cw.visitSource("AlchemyEventSystem.java:147", "invoke: " + instType + handleName + handleDesc);
			
			{
				if (!isStatic)
					cw.visitField(ACC_PUBLIC | ACC_SYNTHETIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", isStatic ? "()V" : "(Ljava/lang/Object;)V", null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
				if (!isStatic) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
				}
				mv.visitInsn(RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			{
				mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, HANDLER_FUNC_NAME, HANDLER_FUNC_DESC, null, null);
				mv.visitCode();
				mv.visitVarInsn(ALOAD, 0);
				if (!isStatic) {
					mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
					mv.visitTypeInsn(CHECKCAST, instType);
				}
				mv.visitVarInsn(ALOAD, 1);
				mv.visitTypeInsn(CHECKCAST, callType);
				mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, instType, handleName, handleDesc, false);
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IRETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
			}
			cw.visitEnd();
			try {
				Class<?> ret = AlchemyModLoader.asm_loader.define(name, cw.toByteArray());
				AlchemyModLoader.info("Define", name);
				if (isStatic)
					result = (Predicate<KeyBinding>) ret.newInstance();
				else
					result = (Predicate<KeyBinding>) ret.getConstructor(Object.class).newInstance(target);
				if (result != null)
					cache.add(callback, result);
			} catch(Exception e) {
				AlchemyRuntimeException.onException(e);
			}
			return result;
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
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerTick(PlayerTickEvent event) {
		String flag = "48";
		if (Always.isClient() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			// runtime do some thing
			{
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
			//System.setProperty("index.alchemy.runtime.debug.player", flag);
		}
		if (Always.isServer() && !System.getProperty("index.alchemy.runtime.debug.player", "").equals(flag)) {
			EntityPlayer player = event.player;
			List<Double6IntArrayPackage> d6iaps = new LinkedList<Double6IntArrayPackage>();
			int update[] = FXUpdateHelper.getIntArrayByArgs(ItemRingTime.FX_KEY_GATHER, 120, 300);
			for (int i = 0; i < 1; i++)
				d6iaps.add(new Double6IntArrayPackage(
						player.posX + 6 - player.worldObj.rand.nextFloat() * 12,
						player.posY + 6 - player.worldObj.rand.nextFloat() * 12,
						player.posZ + 6 - player.worldObj.rand.nextFloat() * 12, 0, 0, 0, update));
			AlchemyNetworkHandler.spawnParticle(FXWisp.Info.type,
					AABBHelper.getAABBFromEntity(player, AlchemyNetworkHandler.getParticleRange()), player.worldObj, d6iaps);
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
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onServerTick(ServerTickEvent event) {
		String flag = "6";
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
	public void onClientTick(ClientTickEvent event) {
		String flag = "12";
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
		AlchemyModLoader.info(event.init.getClass(), event.init);
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
						if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == KeyBinding.class) {
							for (String value : event.value())
								if (value.equals(description))
									key_handle.add(new KeyBindingHandle(binding, handle, method));
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
	public void onKeyInput(KeyInputEvent event) {
		if (hookInputState)
			KeyBinding.unPressAllKeys();
		else 
			for (KeyBindingHandle handle : key_handle)
				if (Keyboard.isKeyDown(handle.getBinding().getKeyCode()))
					handle.handle();
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
	
	@SubscribeEvent
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
			Listener listener = clazz.getAnnotation(Listener.class);
			if (listener != null)
				MinecraftForge.EVENT_BUS.register(clazz);
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