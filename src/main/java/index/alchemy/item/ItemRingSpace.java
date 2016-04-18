package index.alchemy.item;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.item.AlchemyItemBauble.AlchemyItemRing;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.network.MessageParticle;
import index.alchemy.network.SDouble6Packect;
import index.alchemy.util.Cache;

public class ItemRingSpace extends AlchemyItemRing implements IItemInventory {
	
	protected int size;
	
	@Override
	public ItemInventory getItemInventory(EntityPlayer player) {
		ItemStack content = getFormPlayer(player);
		if (content == null)
			return null;
		return new ItemInventory(player, content, size, 
				I18n.translateToLocal("inventory." + getUnlocalizedName().substring(Constants.ITEM)));
	}
	
	public ItemRingSpace() {
		this("ring_space", 9 * 6);
	}
	
	public ItemRingSpace(String name, int size) {
		super(name, 0x6600CC);
		this.size = size;
	}
	
	public void pickUp(EntityPlayer player) {
		ItemStack content = AlchemyItemLoader.ring_space.getFormPlayer(player);
		if (content == null)
			return;
		ItemInventory inventory = ((IItemInventory) AlchemyItemLoader.ring_space).getItemInventory(player);
		if (inventory == null)
			return;
		List<EntityItem> list = player.worldObj.getEntitiesWithinAABB(EntityItem.class, 
				new AxisAlignedBB(player.posX - 5D, player.posY - 5D, player.posZ - 5D,
						player.posX + 5D, player.posY + 5D, player.posZ + 5D));
		List<SDouble6Packect> d6p = new LinkedList<SDouble6Packect>(); 
		for (EntityItem entity : list) {
			inventory.mergeItemStack(entity.getEntityItem());
			if (entity.getEntityItem().stackSize < 1) {
				for (int i = 0; i < 3; i++)
					d6p.add(new SDouble6Packect(entity.posX, entity.posY, entity.posZ, 1, 1, 1));
				entity.setDead();
			}
		}
		if (list.size() > 0) {
			inventory.updateNBT();
			AlchemyNetworkHandler.networkWrapper.sendTo(new MessageParticle(EnumParticleTypes.PORTAL.getParticleID(),
					d6p.toArray(new SDouble6Packect[d6p.size()])), (EntityPlayerMP) player);
		}
	}

}