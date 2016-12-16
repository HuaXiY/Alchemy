package index.alchemy.dlcs.skin.core;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import index.project.version.annotation.Omega;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

@Omega
public class SkinInfo implements ICapabilityProvider, ICapabilitySerializable {
	
	public final Map<MinecraftProfileTexture.Type, ResourceLocation> skin_mapping = Maps.newEnumMap(MinecraftProfileTexture.Type.class);
	
	public byte skin_data[];
	
	public String skin_type;
	
	public final EntityPlayer player;
	
	public SkinInfo(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == SkinCore.skin_info;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return hasCapability(capability, facing) ? (T) this : null;
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
