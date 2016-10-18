package index.alchemy.core;

import org.lwjgl.input.Mouse;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.client.BaublesRenderLayer;
import index.alchemy.api.AlchemyBaubles;
import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Hook.Type;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.client.render.HUDManager;
import index.alchemy.entity.ai.EntityAIEatMeat;
import index.alchemy.inventory.InventoryBauble;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyHooks {
	
	@Hook(value = "biomesoplenty.common.remote.TrailManager#retrieveTrails", isStatic = true)
	public static final Hook.Result retrieveTrails() {
		return Hook.Result.NULL;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.renderer.entity.RenderPlayer#<init>", type = Type.TAIL)
	public static final void init_RenderPlayer(RenderPlayer renderPlayer, RenderManager renderManager, boolean useSmallArms) {
		renderPlayer.addLayer(new BaublesRenderLayer());
	}
	
	@Hook("net.minecraft.world.World#func_72875_a")
	public static final Hook.Result isMaterialInBB(World world, AxisAlignedBB bb, Material material) {
		int minX = MathHelper.floor_double(bb.minX);
		int maxX = MathHelper.ceiling_double_int(bb.maxX);
		int minY = MathHelper.floor_double(bb.minY);
		int maxY = MathHelper.ceiling_double_int(bb.maxY);
		int minZ = MathHelper.floor_double(bb.minZ);
		int maxZ = MathHelper.ceiling_double_int(bb.maxZ);
		BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		for (int x = minX; x < maxX; x++)
			for (int y = minY; y < maxY; y++)
				for (int z = minZ; z < maxZ; z++) {
					Block block = world.getBlockState(pos.setPos(x, y, z)).getBlock();
					if (block instanceof IMaterialContainer && ((IMaterialContainer) block).isMaterialInBB(world, pos, material)) {
						pos.release();
						return Hook.Result.TRUE;
					}
				}
		pos.release();
		return Hook.Result.VOID;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.gui.inventory.GuiInventory#func_146976_a", type = Hook.Type.TAIL)
	public static final void drawGuiContainerBackgroundLayer(GuiInventory gui, float partialTicks, int mouseX, int mouseY) {
		HUDManager.bind(GuiContainer.INVENTORY_BACKGROUND);
		gui.drawTexturedModalRect(gui.guiLeft + 76, gui.guiTop + 7, 7, 7, 18, 18 * 4);
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
	public static final void init_ContainerPlayer(ContainerPlayer container, InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player) {
		InventoryBauble baubles = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.HEAD,  4, 77, 8 + 0 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.BODY,  5, 77, 8 + 1 * 18));
		container.addSlotToContainer(new InventoryBauble.SlotBauble(player, baubles, BaubleType.CHARM, 6, 77, 8 + 2 * 18));
		
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
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.Minecraft#func_184124_aB", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result runTickMouse(Minecraft minecraft) {
		if (AlchemyEventSystem.isHookInput())
			return Hook.Result.NULL;
		else
			return Hook.Result.VOID;
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.util.MouseHelper#func_74374_c", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result mouseXYChange(MouseHelper helper) {
		if (AlchemyEventSystem.isHookInput()) {
			Mouse.getDX();
			Mouse.getDY();
			return Hook.Result.NULL;
		} else
			return Hook.Result.VOID;
	}
	
	@Hook(value = "net.minecraft.entity.passive.EntityWolf#func_184651_r", type = Hook.Type.TAIL)
	public static final void initEntityAI(EntityWolf wolf) {
		wolf.tasks.addTask(3, new EntityAIEatMeat(wolf));
	}
	
}
