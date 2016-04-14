package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.client.AlchemyColorLoader;
import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.client.IColorItem;
import index.alchemy.core.Constants;
import index.alchemy.core.IOreDictionary;
import index.alchemy.core.IResourceLocation;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.oredict.OreDictionary;

public class AlchemyItem extends Item implements IResourceLocation {
	
	public static final CreativeTabs CREATIVE_TABS = new CreativeTabs(Constants.MODID) {
		@Override
		public Item getTabIconItem() {
			return AlchemyItemLoader.solvent_lapis_lazuli;
		}
	};
	
	private String name_color;
	
	protected ResourceLocation icon_name;
	
	@Override
	public String getItemStackDisplayName(ItemStack item) {
        return name_color + super.getItemStackDisplayName(item);
    }

	@Override
	public ResourceLocation getResourceLocation() {
		return icon_name == null ? getRegistryName(): icon_name;
	}
	
	public boolean canUseItemStack(EntityLivingBase living, ItemStack item) {
		return living instanceof EntityPlayer;
	}
	
	public AlchemyItem(String name) {
		this(name, null, null);
	}
	
	public AlchemyItem(String name, TextFormatting formatting) {
		this(name, formatting, null);
	}
	
	public AlchemyItem(String name, String icon) {
		this(name, null, icon);
	}
	
	public AlchemyItem(String name, TextFormatting formatting, String icon) {
		name_color = formatting == null ? "" : formatting.toString();
		if (icon != null)
			icon_name = new AlchemyResourceLocation(icon);
		setCreativeTab(CREATIVE_TABS);
		setUnlocalizedName(name);
		setRegistryName(name);
		registerItem();
	}
	
	public <T extends Item & IColorItem> void registerItem() {
		GameRegistry.register(this);
		AlchemyItemLoader.ALL_ITEM.add(this);
		
		if (Alway.isClient()) {
			ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(
					getResourceLocation(), "inventory"));
			if (this instanceof IColorItem)
				AlchemyColorLoader.addItemColor((T) this);
		}
		
		if (this instanceof IOreDictionary)
			OreDictionary.registerOre(((IOreDictionary) this).getNameInOreDictionary(), new ItemStack(this));
		
		if (this instanceof IBrewingRecipe)
			BrewingRecipeRegistry.addRecipe((IBrewingRecipe) this);
	}
	
}
