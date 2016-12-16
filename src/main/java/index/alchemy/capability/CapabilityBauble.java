package index.alchemy.capability;

import static java.lang.Math.min;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.AlchemyBaubles;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.inventory.InventoryBauble;
import index.alchemy.util.Always;
import index.alchemy.util.InventoryHelper;
import index.project.version.annotation.Beta;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
@Hook.Provider
@InitInstance(AlchemyCapabilityLoader.TYPE)
public class CapabilityBauble extends AlchemyCapability<InventoryBauble> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new AlchemyResourceLocation("bauble");
	public static final String KEY_INVENTORY = "key.inventory";
	
	@Override
	public Class<InventoryBauble> getDataClass() {
		return InventoryBauble.class;
	}
	
	@SubscribeEvent
	public void onAttachCapabilities_Entity(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer || event.getObject() instanceof EntityZombie)
			event.addCapability(RESOURCE, new InventoryBauble((EntityLivingBase) event.getObject()));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingUpdate(LivingUpdateEvent event) {
		InventoryBauble inventory = event.getEntityLiving().getCapability(AlchemyCapabilityLoader.bauble, null);
		if (inventory != null) {
			for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
				ItemStack item = inventory.getStackInSlot(i);
				if (item != null && item.getItem() instanceof IBauble)
					((IBauble) item.getItem()).onWornTick(item, event.getEntityLiving());
			}
			if (Always.isServer()) {
				if (event.getEntityLiving().ticksExisted % 10 == 0)
					for (int i = 0, len = inventory.getSizeInventory(); i < len; i++) {
						ItemStack item = inventory.getStackInSlot(i);
						if (item != null && item.getItem() instanceof IBauble &&
								((IBauble) item.getItem()).willAutoSync(item, event.getEntityLiving()) &&
								inventory.getCache().equals(i, item.getTagCompound()))
							inventory.setChanged(i, true);
					}
				inventory.updateAll();
			}
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void tooltipEvent(ItemTooltipEvent event) {
		if (event.getItemStack() != null && event.getItemStack().getItem() instanceof IBauble) {
			BaubleType type = ((IBauble) event.getItemStack().getItem()).getBaubleType(event.getItemStack());
			event.getToolTip().add(min(event.getToolTip().size(), 1), TextFormatting.GOLD + type.toString());
		}
	}

	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.gui.inventory.GuiInventory#func_146976_a", type = Hook.Type.TAIL)
	public static final void drawGuiContainerBackgroundLayer(GuiInventory gui, float partialTicks, int mouseX, int mouseY) {
		HUDManager.bind(GuiContainer.INVENTORY_BACKGROUND);
		gui.drawTexturedModalRect(gui.guiLeft + 76, gui.guiTop + 7, 7 + 17, 7, 1, 18 * 4);
		gui.drawTexturedModalRect(gui.guiLeft + 76 + 1, gui.guiTop + 7, 7 + 1, 7, 18 - 2, 18 * 4);
		gui.drawTexturedModalRect(gui.guiLeft + 76 + 17, gui.guiTop + 7, 7, 7, 1, 18 * 4);
		for (int i = 0; i < 4; i++)
			gui.drawTexturedModalRect(gui.guiLeft + 97 + i * 18, gui.guiTop + 61, 76, 61, 18, 18);
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.gui.inventory.GuiContainerCreative#func_146976_a", type = Hook.Type.TAIL)
	public static final void drawGuiContainerBackgroundLayer(GuiContainerCreative gui, float partialTicks, int mouseX, int mouseY) {
		if (gui.getSelectedTabIndex() == CreativeTabs.INVENTORY.getTabIndex()) {
			HUDManager.bind(GuiContainer.INVENTORY_BACKGROUND);
			for (int i = 0; i < 4; i++)
				gui.drawTexturedModalRect(gui.guiLeft + 126 + i / 2 * 18, gui.guiTop + 10 + i % 2 * 18, 76, 61, 18, 18);
			for (int i = 0; i < 4; i++)
				gui.drawTexturedModalRect(gui.guiLeft + 16 + i / 2 * 18, gui.guiTop + 10 + i % 2 * 18, 76, 61, 18, 18);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.gui.inventory.GuiContainerCreative#func_147050_b", type = Hook.Type.TAIL)
	public static final void setCurrentCreativeTab(GuiContainerCreative gui, CreativeTabs tab) {
		if (tab == CreativeTabs.INVENTORY)
			for (int len = 8, start = gui.inventorySlots.inventorySlots.size() - len - 1, i = 0; i < len; i++) {
				Slot slot = gui.inventorySlots.getSlot(start + (i == 4 ? 5 : i == 5 ? 4 : i < 4 ? i == 0 ? 3 : i - 1 : i));
				slot.xDisplayPosition = (i > 3 ? 91 : 17) + i / 2 * 18;
				slot.yDisplayPosition = 11 + i % 2 * 18;
			}
	}
	
	@Hook(value = "net.minecraft.inventory.ContainerPlayer#<init>", type = Hook.Type.TAIL)
	public static final void init_ContainerPlayer(ContainerPlayer container, InventoryPlayer inventory, boolean localWorld, EntityPlayer player) {
		InventoryBauble baubles = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.HEAD,   4, 77, 8 + 0 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.BODY,   5, 77, 8 + 1 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.CHARM,  6, 77, 8 + 2 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.AMULET, 0, 80 + 18 * 1, 8 + 3 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.RING,   1, 80 + 18 * 2, 8 + 3 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.RING,   2, 80 + 18 * 3, 8 + 3 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.BELT,   3, 80 + 18 * 4, 8 + 3 * 18));
	}
	
	@Hook("net.minecraft.inventory.ContainerPlayer#func_82846_b")
	public static final Hook.Result transferStackInSlot(ContainerPlayer container, EntityPlayer player, int index) {
		Slot slot = container.inventorySlots.get(index);
		int id = container.inventorySlots.size();
		if (slot != null && slot.getHasStack()) {
			ItemStack item = slot.getStack();
			if (item.getItem() instanceof IBauble && container.mergeItemStack(item, id - AlchemyBaubles.getBaublesSize(), id, false)) {
				if (item.stackSize == 0)
					slot.putStack(null);
				else
					slot.onSlotChanged();
				return Hook.Result.NULL;
			}
		}
		return Hook.Result.VOID;
	}

}