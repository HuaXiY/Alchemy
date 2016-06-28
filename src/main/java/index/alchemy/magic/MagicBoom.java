package index.alchemy.magic;

import index.alchemy.api.Alway;
import index.alchemy.api.IEntityLivingAction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class MagicBoom extends AlchemyMagic {
	
	public static final String NBT_KEY_STRENGTH = "strength";
	
	protected float strength;
	
	public MagicBoom() {}
	
	public MagicBoom(float strength) {
		this.strength = strength;
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Alway.isServer())
			living.worldObj.createExplosion(living, living.posX, living.posY, living.posZ, strength * amplify, true);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat(NBT_KEY_STRENGTH, strength);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		strength = nbt.getFloat(NBT_KEY_STRENGTH);
	}

}