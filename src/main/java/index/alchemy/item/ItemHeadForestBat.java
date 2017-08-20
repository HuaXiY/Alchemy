package index.alchemy.item;

import biomesoplenty.api.item.BOPItems;
import biomesoplenty.common.item.ItemJarEmpty;
import biomesoplenty.common.item.ItemJarFilled;
import biomesoplenty.common.item.ItemJarFilled.JarContents;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.IFieldAccess;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.Hook.Type;
import index.alchemy.entity.EntityForestBat;
import index.alchemy.interacting.ModItems;
import index.alchemy.util.Always;
import index.alchemy.util.EnumHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static index.alchemy.util.Tool.$;

@Hook.Provider
@Field.Provider
public class ItemHeadForestBat extends ItemHeadFollower implements IEventHandle {
	
	public static final JarContents FOREST_BAT = null;
	
	@Hook(value = "biomesoplenty.common.item.ItemJarFilled$JarContents#<clinit>", isStatic = true, type = Type.TAIL)
	public static void clinit() {
		$(ItemHeadForestBat.class, "FOREST_BAT<<", EnumHelper.addEnum(JarContents.class, "FOREST_BAT", new Class[0]));
	}
	
	@Hook("biomesoplenty.common.item.ItemJarEmpty#func_111207_a")
	public static Hook.Result itemInteractionForEntity(ItemJarEmpty empty, ItemStack stack, EntityPlayer player, EntityLivingBase target,
			EnumHand hand) {
		if (Always.isServer() && target.getClass() == EntityBat.class) {
			target.setDead();
			stack.setCount(stack.getCount() - 1);
			ItemStack batJar = new ItemStack(BOPItems.jar_filled, 1, FOREST_BAT.ordinal());
			EntityItem batJarEntity = new EntityItem(player.world, player.posX, player.posY, player.posZ, batJar);
			player.world.spawnEntity(batJarEntity);
			if (!(player instanceof FakePlayer))
				batJarEntity.onCollideWithPlayer(player);
			return Hook.Result.TRUE;
		}
		return Hook.Result.VOID;
	}
	
	@Hook("biomesoplenty.common.item.ItemJarFilled#func_77659_a")
	public static Hook.Result onItemRightClick(ItemJarFilled item, ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		if (Always.isServer() && item.getContentsType(stack) == FOREST_BAT) {
			Vec3d releasePoint = getAirPositionInFrontOfPlayer(world, player, 0.8D);
			releaseBat(stack, world, player, releasePoint);
			emptyJar(stack, player, new ItemStack(BOPItems.jar_empty));
			return new Hook.Result(new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack));
		}
		return Hook.Result.VOID;
	}
	
	protected static ItemStack emptyJar(ItemStack stack, EntityPlayer player, ItemStack emptyJarStack) {
		if (!player.capabilities.isCreativeMode)
			stack.setCount(stack.getCount() - 1);
		player.addStat(StatList.getObjectUseStats(stack.getItem()));
		if (!player.inventory.addItemStackToInventory(emptyJarStack))
			player.dropItem(emptyJarStack, false);
		return stack;
	}
	
	protected static Vec3d getAirPositionInFrontOfPlayer(World world, EntityPlayer player, double targetDistance) {
		float cosYaw = MathHelper.cos(-player.rotationYaw * 0.017453292F - (float) Math.PI);
		float sinYaw = MathHelper.sin(-player.rotationYaw * 0.017453292F - (float) Math.PI);
		float cosPitch = -MathHelper.cos(-player.rotationPitch * 0.017453292F);
		float facingX = sinYaw * cosPitch;
		float facingY = MathHelper.sin(-player.rotationPitch * 0.017453292F);
		float facingZ = cosYaw * cosPitch;

		Vec3d playerEyePosition = new Vec3d(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ);
		Vec3d targetPosition = playerEyePosition.addVector((double) facingX * targetDistance, (double) facingY * targetDistance, (double) facingZ * targetDistance);
		
		// see if there's anything in the way
		RayTraceResult hit = world.rayTraceBlocks(playerEyePosition, targetPosition, true, false, false);
		if (hit == null)
			return targetPosition;
		else {
			// there's something in the way - return the point just before the collision point
			double distance = playerEyePosition.distanceTo(hit.hitVec) * 0.9;
			return playerEyePosition.addVector((double)facingX * distance, (double)facingY * distance, (double)facingZ * distance);			
		}
	}
	
	public static void releaseBat(ItemStack stack, World world, EntityPlayer player, Vec3d releasePoint) {
		EntityBat bat = new EntityBat(world);					
		bat.setLocationAndAngles(releasePoint.x, releasePoint.y, releasePoint.z,
				MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
		world.spawnEntity(bat);
		bat.playLivingSound();
		if (stack.hasDisplayName())
			bat.setCustomNameTag(stack.getDisplayName());
	}
	
	public static final IFieldAccess<EntityLivingBase, EntityLivingBase> lastAttackTarget = null;
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
		EntityLivingBase living = event.getEntityLiving();
		if (lastAttackTarget.get(living) != null) {
			lastAttackTarget.set(living, null);
			PotionEffect effect = living.getActivePotionEffect(MobEffects.GLOWING);
			if (effect == null || effect.getDuration() == 0)
				living.setGlowing(false);
		}
		EntityLivingBase target = (EntityLivingBase) event.getTarget();
		if (target != null && getFormLiving(target) != null) {
			lastAttackTarget.set(living, target);
			living.setGlowing(true);
		}
	}
	
	@Override
	public EntityLiving createFollower(ItemStack item, EntityLivingBase owner) {
		return new EntityForestBat(owner.world);
	}
	
	public ItemHeadForestBat() {
		super("head_forest_bat", -1);
		alchemyTime = 20 * 60;
		alchemyColor = 0x4D381E;
		alchemyMaterials.addAll(Always.generateMaterialConsumers(
				new ItemStack(BOPItems.jar_filled, 1, FOREST_BAT.ordinal()),
				ModItems.bop$flower_swampflower,
				ModItems.bop$flower_glowflower,
				ModItems.bop$gem_topaz,
				ModItems.bop$gem_amethyst,
				Items.ENDER_EYE,
				AlchemyItemLoader.dush_witchcraft,
				Items.MAGMA_CREAM
		));
	}

}
