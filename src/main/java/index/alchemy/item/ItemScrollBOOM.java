package index.alchemy.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemScrollBOOM extends ItemScroll {
	public ItemScrollBOOM() {
		super("scroll_boom", 30, 64, true, 1);
	}
	
	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		for (EntityLivingBase entity : player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, 
				new AxisAlignedBB(player.posX - 30D, player.posY - 30D, player.posZ - 30D,
						player.posX + 30D, player.posY + 30D, player.posZ + 30D))) {
            if(!(entity instanceof EntityPlayer))
                for (int i = 0; i < 3; i++) {
                	player.worldObj.createExplosion(player, entity.posX, entity.posY, entity.posZ, 1F, true);
                }
        }
	}
}
