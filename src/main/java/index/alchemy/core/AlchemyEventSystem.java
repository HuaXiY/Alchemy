package index.alchemy.core;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.core.AlchemyInitHook.InitHookEvent;
import index.alchemy.development.DMain;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemLoader;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
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
	
	public static void registerPlayerTickable(IPlayerTickable tickable) {
		if (tickable.getSide() != null)
			(tickable.getSide() == Side.SERVER ? SERVER_TICKABLE : CLIENT_TICKABLE).add(tickable);
		else {
			SERVER_TICKABLE.add(tickable);
			CLIENT_TICKABLE.add(tickable);
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		for (IPlayerTickable tickable : event.side.isServer() ? SERVER_TICKABLE : CLIENT_TICKABLE)
			tickable.onTick(event.player);
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