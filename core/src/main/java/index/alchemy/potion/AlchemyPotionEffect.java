package index.alchemy.potion;

import index.alchemy.api.annotation.Patch;
import index.alchemy.api.annotation.SuppressFBWarnings;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import net.minecraftforge.common.util.Constants.NBT;

@Patch("net.minecraft.potion.PotionEffect")
public class AlchemyPotionEffect extends PotionEffect {
    
    protected static final String EXTRA_NBT_KEY = "ExtraNbt";
    
    @Patch.Exception
    public AlchemyPotionEffect(Potion potionIn, int durationIn, int amplifierIn, boolean ambientIn,
                               boolean showParticlesIn) {
        super(potionIn, durationIn, amplifierIn, ambientIn, showParticlesIn);
    }
    
    @Override
    @SuppressFBWarnings({"IL_INFINITE_RECURSIVE_LOOP", "NP_ALWAYS_NULL"})
    public NBTTagCompound writeCustomPotionEffectToNBT(NBTTagCompound nbt) {
        nbt = writeCustomPotionEffectToNBT(nbt);
        NBTTagCompound extraNbt = AlchemyPotion.extraNbt.get(this);
        if (extraNbt != null)
            nbt.setTag(EXTRA_NBT_KEY, extraNbt);
        return nbt;
    }
    
    @SuppressFBWarnings({"IL_INFINITE_RECURSIVE_LOOP", "NP_ALWAYS_NULL"})
    public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound nbt) {
        PotionEffect effect = readCustomPotionEffectFromNBT(nbt);
        if (nbt.hasKey(EXTRA_NBT_KEY, NBT.TAG_COMPOUND))
            AlchemyPotion.extraNbt.set(effect, nbt.getCompoundTag(EXTRA_NBT_KEY));
        return effect;
    }
    
}
