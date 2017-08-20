package index.alchemy.item;

import index.project.version.annotation.Gamma;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

@Gamma
public class ItemScrollTP extends ItemScroll {
	public static final String SCROLL_TP = "scroll_tp";
	public ItemScrollTP() {
		super(SCROLL_TP, 100, 1, true, 10);
	}
	
	@Override
	public void useScroll(ItemStack item, World world, EntityPlayer player, int type) {
		if (item.hasTagCompound()) {
			NBTTagCompound nbt = item.getTagCompound().getCompoundTag(SCROLL_TP);
			if (nbt == null) {
				item.getTagCompound().setTag(SCROLL_TP, nbt);
			}
			if (player instanceof EntityPlayerMP) {
				int id;
				if (player.world.provider.getDimension() != (id = nbt.getInteger("id")))
					DimensionManager.getWorld(id).getDefaultTeleporter().placeInExistingPortal(player, player.rotationYaw);
                player.setPositionAndUpdate(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
			}
		} else {
			NBTTagCompound up = new NBTTagCompound(), nbt = new NBTTagCompound();
			item.setTagCompound(up);
			nbt.setString("name", player.world.getWorldInfo().getWorldName());
			nbt.setInteger("id", player.world.provider.getDimension());
			nbt.setDouble("x", player.posX);
			nbt.setDouble("y", player.posY);
			nbt.setDouble("z", player.posZ);
			up.setTag(SCROLL_TP, nbt);
			world.playSound(player, player.posX, player.posY, player.posZ, 
					SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.experience_orb.pickup")),
					SoundCategory.NEUTRAL, 1F, world.rand.nextFloat() * 0.1F + 0.9F);
		}
	}
}
