package index.alchemy.potion;

import index.alchemy.api.IFieldAccess;
import index.alchemy.api.IRegister;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.SuppressFBWarnings;
import index.alchemy.client.render.HUDManager;
import index.alchemy.core.AlchemyResourceLocation;
import index.project.version.annotation.Omega;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Field.Provider
public class AlchemyPotion extends Potion implements IRegister {
	
	@SuppressFBWarnings("NP_ALWAYS_NULL")
	public static final IFieldAccess<PotionEffect, NBTTagCompound> extraNbt = null;
	
	public static final ResourceLocation DEAFULT_ICON = new AlchemyResourceLocation("potion");
	
	public static final int NOT_FLASHING_TIME = 20 * 13 - 1;
	
	private static int current_id = -1;
	
	private int id, interval;
	
	@Override
	public boolean isReady(int tick, int level) {
		return interval < 1 || tick % interval == 0;
	}
	
	@Override
	public boolean isInstant() {
		return interval < 1;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		HUDManager.bind(getStatusIcon());
		return id;
	}
	
	public ResourceLocation getStatusIcon() {
		return DEAFULT_ICON;
	}
	
	@Override
	public void affectEntity(Entity source, Entity indirect, EntityLivingBase living, int level, double health) {
		performEffect(living, level);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isBeneficial() {
		return !isBadEffect();
	}
	
	public AlchemyPotion(String name, boolean isbad, int color) {
		this(name, isbad, color, 1);
	}
	
	public AlchemyPotion(String name, boolean isbad, int color, int interval) {
		super(isbad, color);
		this.interval = interval;
		if (!isbad)
			setBeneficial();
		if (getStatusIcon() != DEAFULT_ICON)
			id = ++current_id;
		if (name != null) {
			setPotionName("effect." + name);
			setRegistryName("potion_" + name);
		}
		register();
	}
	
	public AlchemyPotion setRegistryName(String name) {
        if (getRegistryName() != null)
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + getRegistryName());
        int index = name.lastIndexOf(':');
        String oldPrefix = index == -1 ? "" : name.substring(0, index);
        name = index == -1 ? name : name.substring(index + 1);
        registryName = new ResourceLocation(oldPrefix, name);
        return this;
    }
	
}
