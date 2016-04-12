package index.alchemy.item;

import index.alchemy.client.AlchemyResourceLocation;
import index.alchemy.client.IColorItem;
import index.alchemy.core.AlchemyModLoader;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMagicPowder extends AlchemyItem implements IColorItem {
	
	public static final ResourceLocation POWDER = new AlchemyResourceLocation("powder");
	
	protected int color, metadata;
	protected Item material;
	
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack item, EntityPlayer player, List<String> tooltip, boolean advanced) {
		tooltip.add("");
	}
	
	@Override
	public boolean canUseItemStack(EntityLivingBase living, ItemStack item) {
		return false;
	}
	
	@Override
	public ResourceLocation getResourceLocation() {
		return POWDER;
	}
	
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
	
	public ItemMagicPowder(String name, int color, Item material) {
		this(name, color, material, 0);
	}
	
	public ItemMagicPowder(String name, int color, Item material, int metadata) {
		super("powder_" + name);
		this.color = color;
		this.material = material;
	}

}
