package index.alchemy.core;

import org.lwjgl.input.Mouse;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.client.render.HUDManager;
import index.alchemy.container.ContainerInventoryBauble;
import index.alchemy.inventory.InventoryBauble;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
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
						return new Hook.Result(Boolean.TRUE);
					}
				}
		pos.release();
		return new Hook.Result();
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.gui.inventory.GuiInventory#func_146976_a", type = Hook.Type.TAIL)
	public static final void drawGuiContainerBackgroundLayer(GuiInventory gui, float partialTicks, int mouseX, int mouseY) {
		HUDManager.bind(GuiContainer.INVENTORY_BACKGROUND);
		gui.drawTexturedModalRect(gui.guiLeft + 76, gui.guiTop + 7, 7, 7, 18, 18 * 4);
		gui.drawTexturedModalRect(gui.guiLeft + 96, gui.guiTop + 61, 76, 61, 18, 18);
	}
	
	@Hook(value = "net.minecraft.inventory.ContainerPlayer#<init>", type = Hook.Type.TAIL)
	public static final void init_ContainerPlayer(ContainerPlayer container, InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player) {
		Slot shield = container.inventorySlots.get(container.inventorySlots.size() - 1);
		shield.xDisplayPosition += 20;
		InventoryBauble baubles = player.getCapability(AlchemyCapabilityLoader.bauble, null);
		container.addSlotToContainer(new ContainerInventoryBauble.SlotBauble(player, baubles, BaubleType.AMULET, 0, 77, 8 ));
		container.addSlotToContainer(new ContainerInventoryBauble.SlotBauble(player, baubles, BaubleType.RING, 1, 77, 8 + 1 * 18));
		container.addSlotToContainer(new ContainerInventoryBauble.SlotBauble(player, baubles, BaubleType.RING, 2, 77, 8 + 2 * 18));
		container.addSlotToContainer(new ContainerInventoryBauble.SlotBauble(player, baubles, BaubleType.BELT, 3, 77, 8 + 3 * 18));
	}
	
	@Hook("net.minecraft.inventory.ContainerPlayer#func_82846_b")
	public static final Hook.Result transferStackInSlot(ContainerPlayer container, EntityPlayer player, int index) {
		Slot slot = container.inventorySlots.get(index);
		int id = container.inventorySlots.size();
		if (slot != null && slot.getHasStack()) {
			ItemStack item = slot.getStack();
			if (item.getItem() instanceof IBauble && container.mergeItemStack(item, id - 4, id, false)) {
				if (item.stackSize == 0)
					slot.putStack(null);
				else
					slot.onSlotChanged();
				return new Hook.Result(null);
			}
		}
		return new Hook.Result();
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.client.Minecraft#func_184124_aB", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result runTickMouse(Minecraft minecraft) {
		if (AlchemyEventSystem.isHookInput())
			return new Hook.Result(null);
		else
			return new Hook.Result();
	}
	
	@SideOnly(Side.CLIENT)
	@Hook(value = "net.minecraft.util.MouseHelper#func_74374_c", disable = "index.alchemy.asm.hook.disable_mouse_hook")
	public static final Hook.Result mouseXYChange(MouseHelper helper) {
		if (AlchemyEventSystem.isHookInput()) {
			Mouse.getDX();
			Mouse.getDY();
			return new Hook.Result(null);
		} else
			return new Hook.Result();
	}
	
}
