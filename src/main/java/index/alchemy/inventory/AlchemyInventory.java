package index.alchemy.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class AlchemyInventory implements IInventory {
	
	protected boolean flag;
	
	protected boolean shouldUpdate() {
		return flag;
	}
	
	@Override
	public void markDirty() {
		flag = true;
	}
	
	public IItemHandler getItemHandler() {
		return new InvWrapper(this);
	}
	
	@Override
	public void openInventory(EntityPlayer living) {}

	@Override
	public void closeInventory(EntityPlayer living) {
		if (shouldUpdate())
			updateNBT();
		living = null;
	}
	
	public abstract void updateNBT();
	
	@Override
	public String getName() {
		return "";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}
	
	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}
	
}
