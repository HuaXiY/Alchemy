package index.alchemy.item;

import index.alchemy.util.AABBHelper;
import index.project.version.annotation.Gamma;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Gamma
public class ItemScrollLightning extends ItemScroll {
	public ItemScrollLightning() {
		super("scroll_lightning", 30, 64, true, 1);
	}
	
	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		for (EntityLivingBase entity : player.world.getEntitiesWithinAABB(EntityLivingBase.class, AABBHelper.getAABBFromEntity(player, 30D))) {
            if(!(entity instanceof EntityPlayer))
            	world.addWeatherEffect(new EntityLightningBolt(world, entity.posX, entity.posY, entity.posZ, false));
        }
	}
}
