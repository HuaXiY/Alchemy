package index.alchemy.item;

import index.alchemy.util.AABBHelper;
import index.project.version.annotation.Gamma;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Gamma
public class ItemScrollBOOM extends ItemScroll {
	public ItemScrollBOOM() {
		super("scroll_boom", 30, 64, true, 1);
	}
	
	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		for (EntityLivingBase entity : player.world.getEntitiesWithinAABB(EntityLivingBase.class,  AABBHelper.getAABBFromEntity(player, 300D))) {
            if(!(entity instanceof EntityPlayer))
                for (int i = 0; i < 3; i++) {
                	player.world.createExplosion(player, entity.posX, entity.posY, entity.posZ, 10F, true);
                }
        }
	}
}
