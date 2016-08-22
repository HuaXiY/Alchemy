package index.alchemy.magic;

import index.alchemy.api.Always;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class MagicTeleportDirectional extends AlchemyMagic {
	
	public static final String NBT_KEY_DIM = "dim", NBT_KEY_X = "x", NBT_KEY_Y = "y", NBT_KEY_Z = "z";
	
	protected int dim = -1;
	protected double x, y, z;
	
	public MagicTeleportDirectional() {}
	
	public MagicTeleportDirectional(int dim) {
		this.dim = dim;
	}

	@Override
	public void apply(EntityLivingBase src, EntityLivingBase living, float amplify) {
		if (Always.isServer()) {
			if (dim != -1) {
				WorldServer world = DimensionManager.getWorld(dim);
				if (world != null)
					world.getDefaultTeleporter().placeInExistingPortal(living, living.rotationYaw);
			}
			living.setPositionAndUpdate(x, y, z);
		}
	}
	
	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger(NBT_KEY_DIM, dim);
		nbt.setDouble(NBT_KEY_X, x);
		nbt.setDouble(NBT_KEY_Y, y);
		nbt.setDouble(NBT_KEY_Z, z);
		return nbt;
	}
	
	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		dim = nbt.getInteger(NBT_KEY_DIM);
		x = nbt.getDouble(NBT_KEY_X);
		y = nbt.getDouble(NBT_KEY_Y);
		z = nbt.getDouble(NBT_KEY_Z);
	}

}
