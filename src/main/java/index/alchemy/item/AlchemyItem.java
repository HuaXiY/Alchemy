package index.alchemy.item;

import index.alchemy.api.Alway;
import index.alchemy.client.AlchemyColorLoader;
import index.alchemy.client.IColorItem;
import index.alchemy.core.Constants;
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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public class AlchemyItem extends Item implements IResourceLocation {
	
	public static final CreativeTabs CREATIVE_TABS = new CreativeTabs(Constants.MODID) {
		@Override
		public Item getTabIconItem() {
			return AlchemyItemLoader.scroll_ice_screen;
		}
	};
	
	private String name_color;
	
	@Override
	public String getItemStackDisplayName(ItemStack item) {
        return name_color + super.getItemStackDisplayName(item);
    }

	@Override
	public ResourceLocation getResourceLocation() {
		return getRegistryName();
	}
	
	public boolean canUseItemStack(EntityLivingBase living, ItemStack item) {
		return living instanceof EntityPlayer;
	}
	
	public AlchemyItem(String name) {
		this(name, null);
	}
	
	public <T extends Item & IColorItem> AlchemyItem(String name, TextFormatting formatting) {
		name_color = formatting == null ? "" : formatting.toString();
		setCreativeTab(CREATIVE_TABS);
		setUnlocalizedName(name);
		setRegistryName(name);
		registerItem();
		if (Alway.isClient()) {
			if (this instanceof IColorItem)
				AlchemyColorLoader.addItemColor((T) this);
		}
	}
	
	public void registerItem() {
		GameRegistry.register(this);
		AlchemyItemLoader.ALL_ITEM.add(this);
		
		if (Alway.isClient())
			ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(
					getResourceLocation(), "inventory"));
	}
	
}
