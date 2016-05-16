package index.alchemy.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import index.alchemy.annotation.Init;
import index.alchemy.api.IContinuedRunnable;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IIndexRunnable;
import index.alchemy.api.IPhaseRunnable;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.development.DMain;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemLoader;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
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
	
	public static final EventType[]
			EVENT_BUS = new EventType[]{ EventType.EVENT_BUS },
			TERRAIN_GEN_BUS = new EventType[]{ EventType.TERRAIN_GEN_BUS },
			ORE_GEN_BUS = new EventType[]{ EventType.ORE_GEN_BUS };
	
	public static final List<IPlayerTickable> 
			SERVER_TICKABLE = new LinkedList<IPlayerTickable>(),
			CLIENT_TICKABLE = new LinkedList<IPlayerTickable>();
	
	public static final List<IContinuedRunnable>
			SERVER_RUNNABLE = new LinkedList<IContinuedRunnable>(),
			SERVER_TEMP = new LinkedList<IContinuedRunnable>(),
			CLIENT_RUNNABLE = new LinkedList<IContinuedRunnable>(),
			CLIENT_TEMP = new LinkedList<IContinuedRunnable>();
	
	public static final Set<Object> HOOK_INPUT = new HashSet<Object>();
	private static boolean hookInputState = false;
	
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
				return (c_tick < 0 || phase == Phase.START && --c_tick < 0) && runnable.run(phase);
			}
		}, side);
	}
	
	public static void addContinuedRunnable(final IIndexRunnable runnable, final int tick, Side side) {
		addContinuedRunnable(new IContinuedRunnable() {
			int c_tick = tick;
			@Override
			public boolean run(Phase phase) {
				boolean flag = runnable.run(tick - c_tick, phase);
				return (c_tick < 1 || phase == Phase.START && --c_tick < 1) && flag;
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
		if (AlchemyModLoader.is_modding)
			DMain.init(event.init);
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case GUIID.SPACE_RING:
				return new ContainerChest(player.inventory, AlchemyItemLoader.ring_space.getItemInventory(player, 
						AlchemyItemLoader.ring_space.getFormPlayer(player)), player);
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case GUIID.SPACE_RING:
				return new GuiChest(player.inventory, AlchemyItemLoader.ring_space.getItemInventory(player,
						AlchemyItemLoader.ring_space.getFormPlayer(player)));
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	public static void addInputHook(Object obj) {
		if (HOOK_INPUT.isEmpty()) {
			hookInputState = true;
			/*addContinuedRunnable(new IContinuedRunnable() {
				@Override
				public boolean run(Phase phase) {
					if (phase == Phase.START)
						KeyBinding.unPressAllKeys();
					return !hookInputState;
				}
			}, Side.CLIENT);*/
		}
		HOOK_INPUT.add(obj);
	}
	
	@SideOnly(Side.CLIENT)
	public static void removeInputHook(Object obj) {
		HOOK_INPUT.remove(obj);
		if (HOOK_INPUT.isEmpty())
			hookInputState = false;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void hookKeyInput(KeyInputEvent event) {
		if (hookInputState)
			KeyBinding.unPressAllKeys();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderBar(RenderGameOverlayEvent event) {
		if (event.getType() == ElementType.ALL)
			HUDManager.render();
	}
	
	public static void registerEventHandle(IEventHandle handle) {
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