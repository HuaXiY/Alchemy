package index.alchemy.item;

import index.alchemy.util.AABBHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemScrollBOOM extends ItemScroll {
	public ItemScrollBOOM() {
		super("scroll_boom", 30, 64, true, 1);
	}
	
	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		for (EntityLivingBase entity : player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,  AABBHelper.getAABBFromEntity(player, 30D))) {
            if(!(entity instanceof EntityPlayer))
                for (int i = 0; i < 3; i++) {
                	player.worldObj.createExplosion(player, entity.posX, entity.posY, entity.posZ, 1F, true);
                }
        }
	}
}
