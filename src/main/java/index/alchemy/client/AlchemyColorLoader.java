package index.alchemy.client;

import index.alchemy.api.Alway;
import index.alchemy.core.Init;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Init(state = ModState.POSTINITIALIZED)
public class AlchemyColorLoader {
	
	private static final List item_color = new LinkedList(), block_color = new LinkedList();
	
	public static <T extends Item & IColorItem> void addItemColor(T t) {
		item_color.add(t);
	}
	
	public static <T extends Block & IColorBlock> void addBlockColor(T t) {
		block_color.add(t);
	}
	
	public static <T extends Item & IColorItem> void registerItemColor() {
		ItemColors colors = Minecraft.getMinecraft().getItemColors();
		for (T t : (List<T>) item_color) {
			colors.registerItemColorHandler(t.getItemColor(), t);
		}
	}
	
	public static <T extends Block & IColorBlock> void registerBlockColor() {
		BlockColors colors = Minecraft.getMinecraft().getBlockColors();
		for (T t : (List<T>) block_color) {
			colors.registerBlockColorHandler(t.getBlockColor(), t);
		}
	}
	
	public static void init() {
		registerItemColor();
		registerBlockColor();
	}

}
