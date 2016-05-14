package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import index.alchemy.api.Alway;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemAmulet;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.SDouble6Package;
import index.alchemy.util.AABBHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;

public class ItemAmuletPurify extends AlchemyItemAmulet {
	
	public static final int INTERVAL = 20 * 30;
	public static final Random RANDOM = new Random();
	
	@Override
	public void onWornTick(ItemStack item, EntityLivingBase living) {
		if (living.ticksExisted % INTERVAL == 0) {
			if (Alway.isServer()) {
				boolean flag = false;
				for (PotionEffect effect : living.getActivePotionEffects())
					if (effect.getPotion().isBadEffect()) {
						living.removePotionEffect(effect.getPotion());
						flag = true;
					}
				if (flag) {
					List<SDouble6Package> d6p = new LinkedList<SDouble6Package>();
					for (int i = 0; i < 9; i++)
						d6p.add(new SDouble6Package(living.posX - 1 + RANDOM.nextDouble() * 2, living.posY + 1, living.posZ - 1 + RANDOM.nextDouble() * 2, 0D, 0D, 0D));
					AlchemyNetworkHandler.spawnParticle(EnumParticleTypes.WATER_SPLASH, AABBHelper.getAABBFromEntity(living, 16D), living.worldObj, d6p);
				}
				if (living.isInWater())
					living.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, INTERVAL));
			}
		}
	}

	public ItemAmuletPurify() {
		super("amulet_purify", 0x66CCFF);
	}

}
