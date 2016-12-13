package index.alchemy.item;

import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.core.AlchemyResourceLocation;
import index.alchemy.core.AlchemyConstants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

public class AlchemyItem extends Item implements IResourceLocation, IRegister {
	
	public static abstract class AlchemyCreativeTabs extends CreativeTabs implements IRegister {
		
		public AlchemyCreativeTabs(String label) {
			super(label);
			register();
		}
		
	}
	
	public static final CreativeTabs CREATIVE_TABS = new AlchemyCreativeTabs(AlchemyConstants.MOD_ID) {

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
	
	@Override
	public CreativeTabs getCreativeTab() {
		return CREATIVE_TABS;
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
		setCreativeTab(getCreativeTab());
		setUnlocalizedName(name);
		setRegistryName(name);
		register();
	}
	
}
