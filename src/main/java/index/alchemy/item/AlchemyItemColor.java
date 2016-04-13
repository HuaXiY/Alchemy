package index.alchemy.item;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.client.IColorItem;

public class AlchemyItemColor extends AlchemyItem implements IColorItem {
	
	protected static ResourceLocation icon_name;
	
	protected int color;
	
	@Override
	public ResourceLocation getResourceLocation() {
		return icon_name;
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
	
	public AlchemyItemColor(String name, String icon_name, int color) {
		this(name, icon_name, color, null);
	}
	
	public AlchemyItemColor(String name, String icon_name, int color, TextFormatting formatting) {
		super(name, formatting);
		this.icon_name = new AlchemyResourceLocation(icon_name);
		this.color = color;
	}

}
