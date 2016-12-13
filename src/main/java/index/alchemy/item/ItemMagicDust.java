package index.alchemy.item;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicDust extends AlchemyItemColor {
	
	protected int metadata;
	protected Item material;
	
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack item, EntityPlayer player, List<String> tooltip, boolean advanced) {
		//tooltip.add("");
	}
	
	@Override
	public boolean canUseItemStack(EntityLivingBase living, ItemStack item) {
		return false;
	}
	
	public ItemMagicDust(String name, int color, Item material) {
		this(name, color, material, 0);
	}
	
	public ItemMagicDust(String name, int color, Item material, int metadata) {
		super(name, "powder", color);
		this.material = material;
	}

}
