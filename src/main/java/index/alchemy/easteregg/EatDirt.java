package index.alchemy.easteregg;

import com.google.common.base.Function;

import index.alchemy.achievement.AlchemyAchievementLoader;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Patch;
import index.alchemy.util.Always;
import index.project.version.annotation.Beta;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.LoaderState.ModState;

@Beta
@Hook.Provider
@Init(state = ModState.POSTINITIALIZED)
@Patch("net.minecraft.item.ItemMultiTexture")
public class EatDirt extends ItemMultiTexture {
	
	// No use, just to compile
	@Patch.Exception
	private EatDirt(Block block, Block block2, Function<ItemStack, String> nameFunction) {
		super(block, block2, nameFunction);
	}
	
	@Patch.Exception
	private static Item dirt;
	
	@Patch.Exception
	public static void init() {
		dirt = Item.getItemFromBlock(Blocks.DIRT);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		return super.onItemRightClick(item, world, player, hand);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemMultiTexture#func_77659_a")
	public static Hook.Result onItemRightClick(ItemMultiTexture item, ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (item == dirt) {
			RayTraceResult rayTrace = item.rayTrace(world, player, false);
			if (player.canEat(true) && rayTrace == null) {
				player.setActiveHand(hand);
				return new Hook.Result(new ActionResult(EnumActionResult.SUCCESS, stack));
			}
		}
		return Hook.Result.VOID;
	}
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return super.getItemUseAction(stack);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemMultiTexture#func_77661_b")
	public static Hook.Result getItemUseAction(ItemMultiTexture item, ItemStack stack) {
		return item == dirt ? new Hook.Result(EnumAction.EAT) : Hook.Result.VOID;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase living) {
		return super.onItemUseFinish(stack, world, living);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemMultiTexture#func_77654_b")
	public static Hook.Result onItemUseFinish(ItemMultiTexture item, ItemStack stack, World world, EntityLivingBase living) {
		if (item == dirt) {
			--stack.stackSize;
			if (living instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) living;
				world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP,
					SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
				if (Always.isServer()) {
					player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 40, 150));
					player.addStat(StatList.getObjectUseStats(item));
					player.addStat(AlchemyAchievementLoader.delicious_dirt);
				}
			}
			return new Hook.Result(stack);
		} else
			return Hook.Result.VOID;
	}
	
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return super.getMaxItemUseDuration(stack);
	}
	
	@Patch.Exception
	@Hook("net.minecraft.item.ItemMultiTexture#func_77626_a")
	public static Hook.Result getMaxItemUseDuration(ItemMultiTexture item, ItemStack stack) {
		return item == dirt ? new Hook.Result(32) : Hook.Result.VOID;
	}

}
