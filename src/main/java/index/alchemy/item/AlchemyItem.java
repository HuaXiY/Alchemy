package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.client.AlchemyColorLoader;
import index.alchemy.client.IColorItem;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.core.Constants;
import index.alchemy.core.IEventHandle;
import index.alchemy.core.IOreDictionary;
import index.alchemy.core.IPlayerTickable;
import index.alchemy.core.IRegister;
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

public class AlchemyItem extends Item implements IResourceLocation, IRegister {
	
	public static final CreativeTabs CREATIVE_TABS = new CreativeTabs(Constants.MOD_ID) {
		@Override
		public Item getTabIconItem() {
			return AlchemyItemLoader.solvent_lapis_lazuli;
		}
	};
	
	protected String name_color;
	
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
		register();
	}
	
	@Override
	public void register() {
		AlchemyInitHook.init_impl(this);
	}
	
}
