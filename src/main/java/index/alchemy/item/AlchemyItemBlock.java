package index.alchemy.item;

import index.alchemy.api.IColorItem;
import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.util.Always;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AlchemyItemBlock extends ItemBlock implements IColorItem, IResourceLocation, IRegister {
	
	@SideOnly(Side.CLIENT)
	protected IItemColor color;
	
	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return new IItemColor() {
			
			@Override
			public int getColorFromItemstack(ItemStack item, int index) {
				return color == null ? -1 : color.getColorFromItemstack(item, index);
			}
			
		};
	}
	
	@Override
	public ResourceLocation getResourceLocation() {
		return block instanceof IResourceLocation ? ((IResourceLocation) block).getResourceLocation() : getRegistryName();
	}

	public AlchemyItemBlock(Block block) {
		super(block);
		if (Always.runOnClient() && block instanceof IColorItem)
			color = ((IColorItem) block).getItemColor();
	}

}
