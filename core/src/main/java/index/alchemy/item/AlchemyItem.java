package index.alchemy.item;

import java.util.Random;

import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.core.AlchemyResourceLocation;
import index.project.version.annotation.Omega;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

@Omega
public abstract class AlchemyItem extends Item implements IResourceLocation, IRegister {
    
    protected static final Random random = new Random();
    
    public static abstract class AlchemyCreativeTabs extends CreativeTabs implements IRegister {
        
        public AlchemyCreativeTabs(String label) {
            super(label);
            register();
        }
        
    }
    
    protected String name_color;
    
    protected ResourceLocation icon_name;
    
    @Override
    public String getItemStackDisplayName(ItemStack item) {
        return name_color + super.getItemStackDisplayName(item);
    }
    
    @Override
    public ResourceLocation getResourceLocation() {
        return icon_name == null ? getRegistryName() : icon_name;
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
        if (name != null) {
            setTranslationKey(name);
            setRegistryName(name);
        }
        register();
    }
    
    public AlchemyItem setRegistryName(String name) {
        if (getRegistryName() != null)
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + getRegistryName());
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index);
        name = index == -1 ? name : name.substring(index + 1);
        registryName = new ResourceLocation(oldPrefix, name);
        return this;
    }
    
}
