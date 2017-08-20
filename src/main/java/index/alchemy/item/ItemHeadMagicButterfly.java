package index.alchemy.item;

import biomesoplenty.api.item.BOPItems;
import biomesoplenty.common.item.ItemJarFilled;
//import index.alchemy.entity.EntityMagicButterfly;
import index.alchemy.interacting.ModItems;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class ItemHeadMagicButterfly extends ItemHeadFollower {
	
	@Override
	public EntityLiving createFollower(ItemStack item, EntityLivingBase owner) {
		return null;//new EntityMagicButterfly(owner.world);
	}

	public ItemHeadMagicButterfly() {
		super("head_magic_butterfly", -1);
		alchemyTime = 20 * 60;
		alchemyColor = 0xC95324;
//		alchemyMaterials.addAll(Always.generateMaterialConsumers(
//				new ItemStack(BOPItems.jar_filled, 1, ItemJarFilled.JarContents.BUTTERFLY.ordinal()),
//				ModItems.bop$flower_pink_hibiscus,
//				ModItems.bop$flower_blue_hydrangea,
//				ModItems.bop$gem_tanzanite,
//				ModItems.bop$gem_sapphire,
//				Items.ENDER_EYE,
//				AlchemyItemLoader.dush_witchcraft,
//				Items.MAGMA_CREAM
//		));
	}

}
