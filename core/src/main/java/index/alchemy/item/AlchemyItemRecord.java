package index.alchemy.item;

import index.alchemy.api.IColorItem;
import index.alchemy.api.IOreDictionary;
import index.alchemy.api.IRegister;
import index.alchemy.api.IResourceLocation;
import index.alchemy.core.AlchemyResourceLocation;
import index.project.version.annotation.Omega;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
public class AlchemyItemRecord extends ItemRecord implements IRegister, IResourceLocation, IColorItem, IOreDictionary {
    
    public static final ResourceLocation ICON = new AlchemyResourceLocation("record");
    
    protected int color;
    
    @Override
    public ResourceLocation getResourceLocation() {
        return ICON;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColor() {
        return new IItemColor() {
            @Override
            public int colorMultiplier(ItemStack item, int index) {
                return index == 0 ? color : -1;
            }
        };
    }
    
    public AlchemyItemRecord(String name, SoundEvent sound, int color) {
        super(name, sound);
        this.color = color;
        setTranslationKey("record");
        setRegistryName("record_" + name);
        register();
    }
    
    @Override
    public String getNameInOreDictionary() {
        return "record";
    }
    
}
