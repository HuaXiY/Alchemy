package index.alchemy.item;

import javax.annotation.Nullable;

import index.alchemy.api.IAlchemyBrewingRecipe;
import index.alchemy.api.annotation.Lang;
import index.alchemy.util.Tool;
import index.project.version.annotation.Beta;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import static java.lang.Math.*;

import java.util.List;

@Beta
public class ItemMagicSolvent extends AlchemyItemColor implements IAlchemyBrewingRecipe {
	
	protected int metadata;
	protected Item material;
	protected Type type;
	
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> tooltip, boolean advanced) {
		super.addInformation(item, player, tooltip, advanced);
		Type type = Type.getType(item);
		if (type != null)
			tooltip.add(min(tooltip.size(), 1), type.getTextWithoutFormattingCodes());
	}
	
	@Override
	public boolean canUseItemStack(EntityLivingBase living, ItemStack item) {
		return false;
	}
	
	@Override
	public boolean isIngredient(ItemStack ingredient) {
		return ingredient.getItem() == material && ingredient.getMetadata() == metadata;
	}

	@Override
	public ItemStack getOutput() {
		ItemStack result = new ItemStack(this);
		result.setTagCompound(type.getNBT());
		return result;
	}
	
	@Lang
	public static enum Type {
		
		NULL(TextFormatting.WHITE),
		UNSTABLE(TextFormatting.GRAY),
		BENIGN(TextFormatting.GREEN),
		POWERFUL(TextFormatting.LIGHT_PURPLE),
		WONDERFUL(TextFormatting.GOLD),
		DANGEROUS(TextFormatting.RED);
		
		private TextFormatting formatting;
		
		Type(TextFormatting formatting) {
			this.formatting = formatting;
		}
		
		public static final String PREFIX = "solvent.type.";
		
		private static final String EFFECT = "solvent_effect";
		
		public boolean equals(Type type) {
			return type != null && ordinal() == type.ordinal();
		}
		
		public boolean has(NBTTagCompound nbt) {
			return equals(getType(nbt));
		}
		
		@Override
		public String toString() {
			return I18n.translateToLocal(PREFIX + name().toLowerCase());
		}
		
		public String getTextWithoutFormattingCodes() {
			return formatting + toString();
		}
		
		public NBTTagCompound getNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger(EFFECT, ordinal());
			return nbt;
		}
		
		public static Type random() {
			return values()[itemRand.nextInt(values().length)];
		}
		
		public static NBTTagCompound randomOutput() {
			return random().getNBT();
		}
		
		@Nullable
		public static Type getType(ItemStack item) {
			return getType(item.getTagCompound());
		}
		
		@Nullable
		public static Type getType(NBTTagCompound nbt) {
			return nbt != null && nbt.hasKey(EFFECT) ? Tool.getSafe(values(), nbt.getInteger(EFFECT), NULL) : null;
		}
		
	}
	
	public ItemMagicSolvent(String name, int color, Type type, Item material) {
		this(name, color, type, material, 0);
	}
	
	public ItemMagicSolvent(String name, int color, Type type, Item material, int metadata) {
		super(name, "solvent_bottle", color);
		this.material = material;
		this.metadata = metadata;
		setMaxStackSize(1);
	}
	
}
