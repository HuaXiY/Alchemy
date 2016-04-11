package index.alchemy.core;

import index.alchemy.client.AlchemyKeyBindLoader;
import index.alchemy.container.ContainerItemInventory;
import index.alchemy.gui.GUIID;
import index.alchemy.item.AlchemyItemLoader;
import index.alchemy.item.IItemInventory;
import index.alchemy.item.ItemInventory;
import index.alchemy.network.MessageOpenGui;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageSpaceRingPickUp;
import index.alchemy.potion.AlchemyPotionLoader;
import index.alchemy.potion.PotionEternal;
import index.alchemy.potion.PotionMultipleXP;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyEventSystem implements IGuiHandler {
	
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
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void handleKeyInput(KeyInputEvent event) {
		if (AlchemyKeyBindLoader.key_space_ring.isPressed()) {
			if (AlchemyItemLoader.ring_space.isEquipmented(Minecraft.getMinecraft().thePlayer))
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageOpenGui(GUIID.SPACE_RING));
		} else if (AlchemyKeyBindLoader.key_space_ring_pickup.isPressed()) {
			if (AlchemyItemLoader.ring_space.isEquipmented(Minecraft.getMinecraft().thePlayer) &&
					ClientProxy.ring_space_pickup_last_time - Minecraft.getMinecraft().theWorld.getWorldTime() > 20 * 3) {
				AlchemyNetworkHandler.networkWrapper.sendToServer(new MessageSpaceRingPickUp());
				ClientProxy.ring_space_pickup_last_time = Minecraft.getMinecraft().theWorld.getWorldTime();
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event) {
		for (IPlayerTickable tickable : event.side.isServer() ? SERVER_TICKABLE : CLIENT_TICKABLE)
			tickable.onTick(event.player);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event) {
		((PotionEternal) AlchemyPotionLoader.eternal).onLivingHurt(event);
	}
	
	@SubscribeEvent
	public void onPlayerPickupXP(PlayerPickupXpEvent event) {
		((PotionMultipleXP) AlchemyPotionLoader.multiple_xp).onPlayerPickupXP(event);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case GUIID.SPACE_RING:
				return new ContainerChest(player.inventory, AlchemyItemLoader.ring_space.getItemInventory(player), player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
			case GUIID.SPACE_RING:
				return new GuiChest(player.inventory, AlchemyItemLoader.ring_space.getItemInventory(player));
		}
		return null;
	}
	
	public AlchemyEventSystem(Object mod) {
		MinecraftForge.EVENT_BUS.register(this);
		NetworkRegistry.INSTANCE.registerGuiHandler(mod, this);
	}

}