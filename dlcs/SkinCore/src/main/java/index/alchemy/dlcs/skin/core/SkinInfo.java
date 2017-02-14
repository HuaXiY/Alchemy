package index.alchemy.dlcs.skin.core;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import index.project.version.annotation.Omega;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@Omega
public class SkinInfo implements ICapabilityProvider, ICapabilitySerializable {
	
	public byte skin_data[];
	
	public String skin_type;
	
	@Nullable
	public transient final ISkinEntity entity;
	
	public transient final Map<MinecraftProfileTexture.Type, ResourceLocation>
			skin_mapping = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
	
	public SkinInfo(ISkinEntity entity) {
		this.entity = entity;
	}
	
	public SkinInfo(byte skin_data[], String skin_type) {
		this.skin_data = skin_data;
		this.skin_type = skin_type;
		this.entity = null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SkinCore.skin_info;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return hasCapability(capability, facing) ? (T) this : null;
	}
	
	public void copy(EntityLivingBase living) {
		living.getCapability(SkinCore.skin_info, null).deserializeNBT(serializeNBT());
	}

	@Override
	public NBTBase serializeNBT() {
		return SkinCore.skin_info.writeNBT(this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		SkinCore.skin_info.readNBT(this, null, nbt);
	}
	
}
