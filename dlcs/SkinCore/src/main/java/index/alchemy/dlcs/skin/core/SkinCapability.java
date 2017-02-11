package index.alchemy.dlcs.skin.core;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import index.alchemy.api.ICache;
import index.alchemy.api.IEventHandle;
import index.alchemy.api.annotation.Field;
import index.alchemy.api.annotation.Hook;
import index.alchemy.api.annotation.InitInstance;
import index.alchemy.capability.AlchemyCapability;
import index.alchemy.capability.AlchemyCapabilityLoader;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.dlcs.skin.core.BlockWardrobe.EnumPartType;
import index.alchemy.dlcs.skin.core.SkinCore.UpdateSkinClient;
import index.alchemy.network.AlchemyNetworkHandler;
import index.alchemy.util.cache.ThreadContextCache;
import index.project.version.annotation.Beta;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
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
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Beta
@Hook.Provider
@Field.Provider
@InitInstance(AlchemyCapabilityLoader.TYPE)
public class SkinCapability extends AlchemyCapability<SkinInfo> implements IEventHandle {
	
	public static final ResourceLocation RESOURCE = new ResourceLocation("skin:info");
	public static final String NBT_KEY_SKIN_DATA = "skin_data", NBT_KEY_SKIN_TYPE = "skin_type";
	
	public static final String SKIN_TYPES[] = { "default", "slim" };
	
	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<? extends ISkinEntity> event) {
		if (event.getObject() instanceof ISkinEntity)
			event.addCapability(RESOURCE, new SkinInfo(event.getObject()));
	}
	
	@SubscribeEvent
	public void onPlayer_Clone(PlayerEvent.Clone event) {
		event.getOriginal().getCapability(SkinCore.skin_info, null).copy(event.getEntityPlayer());
		SkinCore.updatePlayerItselfSkin(event.getEntityPlayer());
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		SkinCore.updatePlayerItselfSkin(event.player);
	}
	
	@SubscribeEvent
	public void onPlayerChangedDimensionEvent(PlayerChangedDimensionEvent event) {
		SkinCore.updatePlayerItselfSkin(event.player);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		SkinCore.updatePlayerItselfSkin(event.player);
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
			if (instance.entity instanceof EntityPlayerMP && !(instance.entity instanceof FakePlayer))
				AlchemyEventSystem.addDelayedRunnable(p -> AlchemyNetworkHandler.network_wrapper.sendTo(new SkinCore.UpdateSkinClient(
						((Entity) instance.entity).getEntityId(), instance.skin_type, instance.skin_data), (EntityPlayerMP) instance.entity), 0);
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
	@Hook("net.minecraft.network.NetworkManager#func_179292_f")
	public static Hook.Result isEncrypted(NetworkManager network) {
		return Hook.Result.TRUE;
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
	
	private static ICache.ContextCache<Thread, Tuple3<IBlockAccess, BlockPos, IBlockState>> cache = new ThreadContextCache<>();
	
	public static void updateCache(IBlockAccess world, BlockPos pos, IBlockState state) {
		cache.add(Tuple.tuple(world, pos, state));
	}
	
	public static void clearCache() {
		cache.del();
	}
	
	@Hook("net.minecraft.world.World#func_180495_p")
	public static Hook.Result getBlockState(World world, BlockPos pos) {
		Tuple3<IBlockAccess, BlockPos, IBlockState> tuple3 = cache.get();
		if (tuple3 != null && tuple3.v1() == world && pos.equals(tuple3.v2()))
			return new Hook.Result(tuple3.v3());
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
