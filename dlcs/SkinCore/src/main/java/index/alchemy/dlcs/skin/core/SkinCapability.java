package index.alchemy.dlcs.skin.core;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.capability.AlchemyCapability;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.dlcs.skin.core.BlockWardrobe.EnumPartType;
import index.alchemy.dlcs.skin.core.SkinCore.UpdateSkinClient;
import index.alchemy.network.AlchemyNetworkHandler;
import index.project.version.annotation.Beta;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
@Hook.Provider
@InitInstance(AlchemyCapabilityLoader.TYPE)
public class SkinCapability extends AlchemyCapability<SkinInfo> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new ResourceLocation("skin:info");
	public static final String NBT_KEY_SKIN_DATA = "skin_data", NBT_KEY_SKIN_TYPE = "skin_type";
	
	public static final String SKIN_TYPES[] = { "default", "slim" };
	
	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<? extends EntityPlayer> event) {
		if (event.getObject() instanceof EntityPlayer)
		event.addCapability(RESOURCE, new SkinInfo(event.getObject()));
	}
	
	@SubscribeEvent
	public void onPlayer_Clone(PlayerEvent.Clone event) {
		event.getOriginal().getCapability(SkinCore.skin_info, null).copy(event.getEntityPlayer());
	}
	
	@SubscribeEvent
	public void onPlayer_StartTracking(PlayerEvent.StartTracking event) {
		SkinInfo info = event.getTarget().getCapability(SkinCore.skin_info, null);
		if (info != null && info.skin_data != null && info.skin_type != null)
			AlchemyNetworkHandler.network_wrapper.sendTo(new UpdateSkinClient(event.getTarget().getEntityId(),
					info.skin_type, info.skin_data), (EntityPlayerMP) event.getEntityPlayer());
	}
	
	@Override
	public NBTBase writeNBT(Capability capability, SkinInfo instance, EnumFacing side) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (instance.skin_data != null)
			nbt.setByteArray(NBT_KEY_SKIN_DATA, instance.skin_data);
		if (instance.skin_type != null)
			nbt.setString(NBT_KEY_SKIN_TYPE, instance.skin_type);
		return nbt;
	}
	
	@Override
	public void readNBT(Capability capability, SkinInfo instance, EnumFacing side, NBTBase base) {
		if (base instanceof NBTTagCompound) {
			NBTTagCompound nbt = (NBTTagCompound) base;
			if (nbt.hasKey(NBT_KEY_SKIN_DATA))
				instance.skin_data = nbt.getByteArray(NBT_KEY_SKIN_DATA);
			if (nbt.hasKey(NBT_KEY_SKIN_TYPE))
				instance.skin_type = nbt.getString(NBT_KEY_SKIN_TYPE);
			if (instance.player instanceof EntityPlayerMP && !(instance.player instanceof FakePlayer))
				AlchemyEventSystem.addDelayedRunnable(p -> AlchemyNetworkHandler.network_wrapper.sendTo(new SkinCore.UpdateSkinClient(
						instance.player.getEntityId(), instance.skin_type, instance.skin_data), (EntityPlayerMP) instance.player), 0);
		}
	}
	
	@Override
	public Class<SkinInfo> getDataClass() {
		return SkinInfo.class;
	}
	
	@Override
	public SkinInfo call() throws Exception {
		return new SkinInfo(null);
	}
	
	@SideOnly(Side.CLIENT)
	@Hook("net.minecraft.client.network.NetworkPlayerInfo#func_178837_g")
	public static Hook.Result getLocationSkin(NetworkPlayerInfo playerInfo) {
		if (playerInfo.getGameProfile() == null)
			return Hook.Result.VOID;
		EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(playerInfo.getGameProfile().getId());
		if (player == null)
			return Hook.Result.VOID;
		ResourceLocation skin = player.getCapability(SkinCore.skin_info, null).skin_mapping.get(Type.SKIN);
		return skin == null ? Hook.Result.VOID : new Hook.Result(skin);
	}
	
	@SideOnly(Side.CLIENT)
	@Hook("net.minecraft.client.network.NetworkPlayerInfo#func_178851_f")
	public static Hook.Result getSkinType(NetworkPlayerInfo playerInfo) {
		if (playerInfo.getGameProfile() == null)
			return Hook.Result.VOID;
		EntityPlayer player = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(playerInfo.getGameProfile().getId());
		if (player == null)
			return Hook.Result.VOID;
		String type = player.getCapability(SkinCore.skin_info, null).skin_type;
		return type == null || type.isEmpty() ? Hook.Result.VOID : new Hook.Result(type);
	}
	
	private static ThreadLocal<IBlockAccess> cacheWorld = new ThreadLocal<>();
	private static ThreadLocal<BlockPos> cachePos = new ThreadLocal<>();
	private static ThreadLocal<IBlockState> cacheState = new ThreadLocal<>();
	
	public static void updateCache(IBlockAccess world, BlockPos pos, IBlockState state) {
		cacheWorld.set(world);
		cachePos.set(pos);
		cacheState.set(state);
	}
	
	public static void clearCache() {
		updateCache(null, null, null);
	}
	
	@Hook("net.minecraft.world.World#func_180495_p")
	public static Hook.Result getBlockState(World world, BlockPos pos) {
		if (cacheWorld.get() == world && pos.equals(cachePos.get()))
			return new Hook.Result(cacheState.get());
		return Hook.Result.VOID;
	}
	
	@Hook("net.minecraft.world.World#func_180501_a")
	public static Hook.Result setBlockState(World world, BlockPos pos, IBlockState newState, int flags) {
		if ((flags & (1 << 6)) != 0)
			return Hook.Result.VOID;
		if (newState.getBlock() instanceof BlockWardrobe) {
			if (newState.getValue(BlockWardrobe.PART) == EnumPartType.FOOT)
				if (newState.getBlock().canPlaceBlockAt(world, pos))
					world.setBlockState(pos.up(), newState.withProperty(BlockWardrobe.PART, BlockWardrobe.EnumPartType.HEAD), flags);
				else
					return Hook.Result.FALSE;
		} else {
			IBlockState oldState = world.getBlockState(pos);
			if (oldState.getBlock() instanceof BlockWardrobe)
				world.setBlockState(oldState.getValue(BlockWardrobe.PART) == EnumPartType.HEAD ? pos.down() : pos.up(),
						Blocks.AIR.getDefaultState(), 1 << 6 | flags);
		}
		return Hook.Result.VOID;
	}

}
