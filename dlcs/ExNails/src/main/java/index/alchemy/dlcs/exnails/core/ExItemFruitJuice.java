package index.alchemy.dlcs.exnails.core;

import java.util.List;

import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Patch;
import index.alchemy.util.CraftingHelper;
import index.alchemy.util.EnumHelper;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState.ModState;
import toughasnails.item.ItemFruitJuice;

@Omega
@Init(state = ModState.POSTINITIALIZED)
@Patch("toughasnails.item.ItemFruitJuice")
public class ExItemFruitJuice extends ItemFruitJuice {
	
	// Marks the implicit constructor
	@Patch.Exception
	private ExItemFruitJuice() { }
	
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) { }
	
	@Override
	public boolean hasEffect(ItemStack stack) { return false; }
	
	@Patch.Exception
	public static void init() {
		CraftingHelper.remove(ItemFruitJuice.class);
		Tool.load(ItemFruitJuice.JuiceType.class);
		EnumHelper.setValues(ItemFruitJuice.JuiceType.class, EnumHelper.addEnum(ItemFruitJuice.JuiceType.class, "DEFAULT",
				new Class[]{ int.class, float.class }, 8, 0.5F));
	}

}
