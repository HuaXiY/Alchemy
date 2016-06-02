package index.alchemy.capability;

import baubles.api.IBauble;
import index.alchemy.annotation.InitInstance;
import index.alchemy.annotation.KeyEvent;
import index.alchemy.api.Alway;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IGuiHandle;
import index.alchemy.api.IInputHandle;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyEventSystem.EventType;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.InventoryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityBauble extends AlchemyCapability<InventoryBauble> implements IEventHandle, IInputHandle, IGuiHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("bauble");
	public static final String KEY_INVENTORY = "key.inventory";
	
	@Override
	public Class<InventoryBauble> getDataClass() {
		return InventoryBauble.class;
	}

	@Override
	public EventType[] getEventType() {
		return AlchemyEventSystem.EVENT_BUS;
	}
	
	@SubscribeEvent
	public void onAttachCapabilities_Entity(AttachCapabilitiesEvent.Entity event) {
		if (event.getEntity() instanceof EntityLivingBase) {
			event.addCapability(RESOURCE, new InventoryBauble((EntityLivingBase) event.getEntity()));
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		IInventory inventory = event.getEntityLiving().getCapability(AlchemyCapabilityLoader.bauble, null);
		for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null && item.getItem() instanceof IBauble)
				((IBauble) item.getItem()).onWornTick(item, event.getEntityLiving());
		}
	}
	
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (Alway.isServer() && !(living instanceof EntityPlayer)) {
			IInventory inventory = living.getCapability(AlchemyCapabilityLoader.bauble, null);
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null)
					event.getDrops().add(InventoryHelper.getEntityItem(living, item));
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerDrops(PlayerDropsEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (Alway.isServer() && !player.worldObj.getGameRules().getBoolean("keepInventory")) {
			IInventory inventory = player.getCapability(AlchemyCapabilityLoader.bauble, null);
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null)
					event.getDrops().add(InventoryHelper.getEntityItem(player, item));
			}
		}
	}

	@Override
	public KeyBinding[] getKeyBindings() {
		AlchemyModLoader.checkState();
		return new KeyBinding[] {
				Minecraft.getMinecraft().gameSettings.keyBindInventory
		};
	}
	
	@SideOnly(Side.CLIENT)
	@KeyEvent(KEY_INVENTORY)
	public void onInventory(KeyBinding binding) {
		
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		return null;
	}

}