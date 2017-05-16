package index.alchemy.dlcs.exnails.core;

import javax.annotation.Nullable;

import index.alchemy.api.Time;
import index.alchemy.api.annotation.Patch;
import index.alchemy.util.Always;
import index.project.version.annotation.Omega;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@Omega
@Patch("net.minecraft.entity.passive.EntityMooshroom")
public class ExEntityMooshroom extends EntityMooshroom {
	
	public static final String NBT_KEY_INTERACT = "interact";
	
	@Patch.Exception
	private ExEntityMooshroom(World world) { super(world); }
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, @Nullable ItemStack stack) {
		if (Always.isClient())
			return false;
		if (stack != null && stack.getItem() == Items.BOWL && getGrowingAge() >= 0 && !player.capabilities.isCreativeMode &&
				worldObj.getTotalWorldTime() - getEntityData().getLong(NBT_KEY_INTERACT) > Time.DAY) {
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
			if (--stack.stackSize == 0)
				player.setHeldItem(hand, new ItemStack(Items.MUSHROOM_STEW));
			else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MUSHROOM_STEW)))
				player.dropItem(new ItemStack(Items.MUSHROOM_STEW), false);
			getEntityData().setLong(NBT_KEY_INTERACT, worldObj.getTotalWorldTime());
			return true;
		}
		else
			return super.processInteract(player, hand, stack);
	}

}
