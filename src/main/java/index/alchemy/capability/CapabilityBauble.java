package index.alchemy.capability;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IPlayerTickable;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.Always;
import index.alchemy.util.InventoryHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityBauble extends AlchemyCapability<InventoryBauble> implements IPlayerTickable, IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("bauble");
	public static final String KEY_INVENTORY = "key.inventory";
	
	@Override
	public Class<InventoryBauble> getDataClass() {
		return InventoryBauble.class;
	}
	
	@Override
	public Side getSide() {
		return Side.SERVER;
	}

	@Override
	public void onTick(EntityPlayer player, Phase phase) {
		if (phase == Phase.END) {
			InventoryBauble inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
			if (inventory != null)
				while (inventory.update(false));
		}
	}
	
	@SubscribeEvent
	public void onAttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer || event.getObject() instanceof EntityZombie)
			event.addCapability(RESOURCE, new InventoryBauble((EntityLivingBase) event.getObject()));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingUpdate(LivingUpdateEvent event) {
		IInventory inventory = event.getEntityLiving().getCapability(AlchemyCapabilityLoader.bauble, null);
		if (inventory != null)
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null && item.getItem() instanceof IBauble)
					((IBauble) item.getItem()).onWornTick(item, event.getEntityLiving());
			}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDrops(LivingDropsEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (Always.isServer() && !(living instanceof EntityPlayer)) {
			IInventory inventory = living.getCapability(AlchemyCapabilityLoader.bauble, null);
			if (inventory == null)
				return;
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.removeStackFromSlot(i);
				if (item != null)
					event.getDrops().add(InventoryHelper.getEntityItem(living, item));
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDrops(PlayerDropsEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (Always.isServer() && !player.worldObj.getGameRules().getBoolean("keepInventory")) {
			IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
			if (inventory != null)
				for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
					ItemStack item = inventory.removeStackFromSlot(i);
					if (item != null)
						event.getDrops().add(InventoryHelper.getEntityItem(player, item));
				}
		}
	}
	
	@SubscribeEvent
	public void onPlayer_Clone(PlayerEvent.Clone event) {
		event.getOriginal().getCapability(AlchemyCapabilityLoader.bauble, null).copy(event.getEntityPlayer());
	}
	
	@SubscribeEvent
	public void onPlayer_StartTracking(PlayerEvent.StartTracking event) {
		InventoryBauble inventory = event.getTarget().getCapability(AlchemyCapabilityLoader.bauble, null);
		if (inventory != null && inventory.hasItem())
			inventory.updatePlayer((EntityPlayerMP) event.getEntityPlayer(), inventory.serializeNBT());
	}
	
	@SubscribeEvent
	public void tooltipEvent(ItemTooltipEvent event) {
		if (event.getItemStack()!=null && event.getItemStack().getItem() instanceof IBauble) {
			BaubleType type = ((IBauble) event.getItemStack().getItem()).getBaubleType(event.getItemStack());
			event.getToolTip().add(TextFormatting.GOLD + I18n.translateToLocal("name." + type.name().toLowerCase()));
		}
	}

	// TODO old data

}