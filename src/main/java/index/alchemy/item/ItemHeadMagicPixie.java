package index.alchemy.item;

import biomesoplenty.api.item.BOPItems;
import biomesoplenty.common.item.ItemJarFilled;
import index.alchemy.entity.EntityMagicPixie;
import index.alchemy.interacting.ModItems;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ItemHeadMagicPixie extends ItemHeadFollower {
	
	@Override
	public EntityLiving createFollower(ItemStack item, EntityLivingBase owner) {
		return new EntityMagicPixie(owner.worldObj);
	}

	public ItemHeadMagicPixie() {
		super("head_magic_pixie", -1);
		alchemyTime = 20 * 60;
		alchemyColor = 0xFD94E6;
		alchemyMaterials.addAll(Always.generateMaterialConsumers(
				new ItemStack(BOPItems.jar_filled, 1, ItemJarFilled.JarContents.PIXIE.ordinal()),
				ModItems.bop$flower_swampflower,
				ModItems.bop$flower_glowflower,
				Items.DIAMOND,
				Items.EMERALD,
				Items.ENDER_EYE,
				AlchemyItemLoader.dush_witchcraft,
				Items.MAGMA_CREAM
		));
	}

}
