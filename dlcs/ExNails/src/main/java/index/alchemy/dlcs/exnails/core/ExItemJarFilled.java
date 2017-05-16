package index.alchemy.dlcs.exnails.core;

import biomesoplenty.api.item.BOPItems;
import biomesoplenty.common.item.ItemJarFilled;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Patch;
import index.project.version.annotation.Omega;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@Omega
@Hook.Provider
@Patch("biomesoplenty.common.item.ItemJarFilled")
public class ExItemJarFilled extends ItemJarFilled {
	
	@Patch.Exception
	@Hook("biomesoplenty.common.item.ItemJarFilled#func_77659_a")
	public static Hook.Result onItemRightClick(ItemJarFilled item, ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (item.getContentsType(stack) == JarContents.HONEY) {
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
	@Hook("biomesoplenty.common.item.ItemJarFilled#func_77661_b")
	public static Hook.Result getItemUseAction(ItemJarFilled item, ItemStack stack) {
		return item.getContentsType(stack) == JarContents.HONEY ? new Hook.Result(EnumAction.DRINK) : Hook.Result.VOID;
	}
	
	@Override
	@Patch.Spare
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase living) {
		return super.onItemUseFinish(stack, worldIn, living);
	}
	
	@Patch.Exception
	@Hook("biomesoplenty.common.item.ItemJarFilled#func_77654_b")
	public static Hook.Result onItemUseFinish(ItemJarFilled item, ItemStack stack, World world, EntityLivingBase living) {
		if (item.getContentsType(stack) == JarContents.HONEY) {
			stack.stackSize--;
			if (living instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) living;
				player.getFoodStats().addStats(4, 1F);
			}
			living.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20 * 6));
			ItemStack jar = new ItemStack(BOPItems.jar_empty);
			if (stack.stackSize < 1)
				return new Hook.Result(jar);
			if (living instanceof EntityPlayer) {
				if (!((EntityPlayer) living).inventory.addItemStackToInventory(jar))
					((EntityPlayer) living).dropItem(jar, false);
			} else
				living.entityDropItem(jar, 1);
			return new Hook.Result(stack);
		} else
			return Hook.Result.VOID;
	}
	
	@Override
	@Patch.Spare
	public int getMaxItemUseDuration(ItemStack stack) {
		return super.getMaxItemUseDuration(stack);
	}
	
	@Patch.Exception
	@Hook("biomesoplenty.common.item.ItemJarFilled#func_77626_a")
	public static Hook.Result getMaxItemUseDuration(ItemJarFilled item, ItemStack stack) {
		return item.getContentsType(stack) == JarContents.HONEY ? new Hook.Result(32) : Hook.Result.VOID;
	}

}
