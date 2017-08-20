package index.alchemy.easteregg;

import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import index.project.version.annotation.Beta;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@Beta
@Hook.Provider
@Patch("net.minecraft.item.ItemBucket")
public class DrinkingLava extends ItemBucket {
	
	// No use, just to compile
	@Patch.Exception
	private DrinkingLava(Block containedBlockIn) {
		super(containedBlockIn);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemBucket#func_77659_a")
	public static Hook.Result onItemRightClick(ItemBucket item, ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (item == Items.LAVA_BUCKET) {
			RayTraceResult rayTrace = item.rayTrace(world, player, false);
			if (rayTrace == null) {
				player.setActiveHand(hand);
				return new Hook.Result(new ActionResult(EnumActionResult.SUCCESS, stack));
			}
		}
		return Hook.Result.VOID;
	}
	
	@Override
	@Patch.Spare
	public EnumAction getItemUseAction(ItemStack stack) {
		return super.getItemUseAction(stack);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemBucket#func_77661_b")
	public static Hook.Result getItemUseAction(ItemBucket item, ItemStack stack) {
		return item == Items.LAVA_BUCKET ? new Hook.Result(EnumAction.DRINK) : Hook.Result.VOID;
	}
	
	@Override
	@Patch.Spare
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase living) {
		return super.onItemUseFinish(stack, worldIn, living);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemBucket#func_77654_b")
	public static Hook.Result onItemUseFinish(ItemBucket item, ItemStack stack, World world, EntityLivingBase living) {
		if (item == Items.LAVA_BUCKET) {
			living.attackEntityFrom(DamageSource.LAVA, 10);
			living.setFire(30);
//			if (living instanceof EntityPlayer) {
//				EntityPlayer player = (EntityPlayer) living;
//			}
			return new Hook.Result(new ItemStack(Items.BUCKET));
		} else
			return Hook.Result.VOID;
	}
	
	@Override
	@Patch.Spare
	public int getMaxItemUseDuration(ItemStack stack) {
		return super.getMaxItemUseDuration(stack);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemBucket#func_77626_a")
	public static Hook.Result getMaxItemUseDuration(ItemBucket item, ItemStack stack) {
		return item == Items.LAVA_BUCKET ? new Hook.Result(32) : Hook.Result.VOID;
	}

}
