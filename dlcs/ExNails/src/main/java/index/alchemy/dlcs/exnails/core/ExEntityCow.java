package index.alchemy.dlcs.exnails.core;

import javax.annotation.Nullable;

import index.alchemy.api.Time;
import index.alchemy.api.annotation.Patch;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@Patch("net.minecraft.entity.passive.EntityCow")
public class ExEntityCow extends EntityCow {
	
	public static final String NBT_KEY_INTERACT = "interact";
	
	@Patch.Exception
	private ExEntityCow(World world) { super(world); }
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
		if (stack != null && stack.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !isChild() &&
				worldObj.getTotalWorldTime() - getEntityData().getLong(NBT_KEY_INTERACT) > Time.DAY * 3) {
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
			if (--stack.stackSize == 0)
				player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
			else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET)))
				player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
			getEntityData().setLong(NBT_KEY_INTERACT, worldObj.getTotalWorldTime());
			return true;
		}
		else
			return super.processInteract(player, hand, stack);
	}

}
