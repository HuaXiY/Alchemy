package index.alchemy.item;

import index.alchemy.api.IColorItem;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyItemColor extends AlchemyItem implements IColorItem {
	
	protected int color;
	
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
		super(name, formatting, icon_name);
		this.color = color;
	}
	
}
