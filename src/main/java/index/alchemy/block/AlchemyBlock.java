package index.alchemy.block;

import index.alchemy.api.Alway;
import index.alchemy.client.AlchemyColorLoader;
import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.client.IColorBlock;
import index.alchemy.core.Constants;
import index.alchemy.core.IOreDictionary;
import index.alchemy.core.IResourceLocation;
import index.alchemy.core.ITileEntity;
import index.alchemy.item.AlchemyItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class AlchemyBlock extends Block implements IResourceLocation {
	
	protected ResourceLocation icon_name;
	
	@Override
	public ResourceLocation getResourceLocation() {
		return icon_name == null ? getRegistryName(): icon_name;
	}
	
	public AlchemyBlock(String name, Material material) {
		this(name, material, null);
	}
	
	public AlchemyBlock(String name, Material material, String icon) {
		super(material);
		if (icon != null)
			icon_name = new AlchemyResourceLocation(icon);
		if (hasCreativeTab())
			setCreativeTab(AlchemyItem.CREATIVE_TABS);
		setUnlocalizedName(name);
		setRegistryName(name);
		registerBlock();
	}
	
	public boolean hasCreativeTab() {
		return true;
	}
	
	public <T extends Block & IColorBlock> void registerBlock() {
		Item item = new ItemBlock(this).setRegistryName(getRegistryName());
		GameRegistry.register(this);
		GameRegistry.register(item);
		AlchemyBlockLoader.ALL_BLOCK.add(this);
		
		if (Alway.isClient()) {
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(
					getResourceLocation(), "inventory"));
			if (this instanceof IColorBlock)
				AlchemyColorLoader.addBlockColor((T) this);
			item.setFull3D();
		}
		
		if (this instanceof ITileEntity)
			GameRegistry.registerTileEntity(((ITileEntity) this).getTileEntityClass(), getUnlocalizedName());
		
		if (this instanceof IOreDictionary)
			OreDictionary.registerOre(((IOreDictionary) this).getNameInOreDictionary(), new ItemStack(this));
	}

	
}
