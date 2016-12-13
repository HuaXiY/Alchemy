package index.alchemy.item;

import index.alchemy.block.AlchemyBlockLoader;
import index.alchemy.interacting.Elemix;
import index.alchemy.util.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemScrollIceScreen extends ItemScroll {
	public ItemScrollIceScreen() {
		super("scroll_ice_screen", 0, 64, false, 1);
	}

	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		EntityLivingBase living = player;
		int posX = (int) living.posX, posY = (int) living.posY, posZ = (int) living.posZ;
		if (Always.isServer()) {
			for (int x = -1; x < 2; x++) 
				for (int y = -1; y < 3; y++) 
					for (int z = -1; z < 2; z++) {
						if ((!(x == 0 && z == 0) || (y == -1 || y == 2)) &&
								Elemix.blockCanToIce(world, new BlockPos(posX + x, posY + y, posZ + z))) {
							world.setBlockState(new BlockPos(posX + x, posY + y, posZ + z), AlchemyBlockLoader.ice_temp.getDefaultState());
						}
					}
		}
		living.fallDistance = 0;
		living.setPosition(posX + 0.5, living.posY > posY ? posY + 1 : posY, posZ + 0.5);
		living.motionX = living.motionZ = living.motionY = 0;
	}
}
