package index.alchemy.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.client.IColorItem;

public class ItemIngot extends AlchemyItem implements IColorItem {
	
	public static final ResourceLocation INGOT = new AlchemyResourceLocation("ingot");
	
	protected int color;
	
	@Override
	public ResourceLocation getResourceLocation() {
		return INGOT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack item, int index) {
				return index == 0 ? color : -1;
			}
		};
	}

	public ItemIngot(String name) {
		super(name);
	}

}
