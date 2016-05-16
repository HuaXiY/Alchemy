package index.alchemy.client;

import java.util.LinkedList;
import java.util.List;

import index.alchemy.annotation.Init;
import index.alchemy.api.IColorBlock;
import index.alchemy.api.IColorItem;
import index.alchemy.core.AlchemyInitHook;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
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
			AlchemyInitHook.push_event(t);
		}
	}
	
	public static <T extends Block & IColorBlock> void registerBlockColor() {
		BlockColors colors = Minecraft.getMinecraft().getBlockColors();
		for (T t : (List<T>) block_color) {
			colors.registerBlockColorHandler(t.getBlockColor(), t);
			AlchemyInitHook.push_event(t);
		}
	}
	
	public static void init() {
		registerItemColor();
		registerBlockColor();
	}

}
