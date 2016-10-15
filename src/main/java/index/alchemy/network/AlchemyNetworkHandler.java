package index.alchemy.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import index.alchemy.api.IGuiHandle;
import index.alchemy.api.INetworkMessage;
import index.alchemy.api.annotation.Config;
import index.alchemy.api.annotation.Init;
import index.alchemy.api.annotation.Loading;
import index.alchemy.api.annotation.Message;
import index.alchemy.core.AlchemyConstants;
import index.alchemy.core.AlchemyEventSystem;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.debug.AlchemyRuntimeException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Loading
@Init(state = ModState.PREINITIALIZED)
public class AlchemyNetworkHandler {
	
	public static final String CATEGORY_NETWORK ="network";
	
	@Config(category = CATEGORY_NETWORK, comment = "Can hear the sound of the range.")
	private static int sound_range = 32;
	
	public static int getSoundRange() {
		return sound_range;
	}
	
	@Config(category = CATEGORY_NETWORK, comment = "Can see the particle of the range.")
	private static int particle_range = 32;
	
	public static int getParticleRange() {
		return particle_range;
	}
	
	@Config(category = CATEGORY_NETWORK, comment = "Can receive the change of the tileentity of the range.")
	private static int tileentity_update_range = 128;
	
	public static int getTileEntityUpdateRange() {
		return tileentity_update_range;
	}
	
	public static final SimpleNetworkWrapper network_wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(AlchemyConstants.MOD_ID);
	
	private static final Map<Class<?>, Side> message_mapping = new LinkedHashMap<Class<?>, Side>();
	
	private static int id = -1;
	
	private static synchronized int next() {
		return ++id;
	}

	private static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz, Side side) {
		network_wrapper.registerMessage(clazz, clazz, next(), side);
		AlchemyInitHook.push_event(clazz);
	}
	
	public static void registerMessage(INetworkMessage<IMessage> handle) {
		if (handle instanceof INetworkMessage.Client)
			registerMessage((INetworkMessage.Client<IMessage>) handle);
		if (handle instanceof INetworkMessage.Server)
			registerMessage((INetworkMessage.Server<IMessage>) handle);
	}
	
	public static void registerMessage(INetworkMessage.Client<IMessage> handle) {
		network_wrapper.registerMessage(handle, handle.getClientMessageClass(), next(), Side.CLIENT);
	}
	
	public static void registerMessage(INetworkMessage.Server<IMessage> handle) {
		network_wrapper.registerMessage(handle, handle.getServerMessageClass(), next(), Side.SERVER);
	}
	
	@SideOnly(Side.CLIENT)
	public static void openGui(IGuiHandle handle) {
		network_wrapper.sendToServer(new MessageOpenGui(AlchemyEventSystem.getGuiIdByGuiHandle(handle)));
	}
	
	public static void spawnParticle(EnumParticleTypes particle, AxisAlignedBB aabb, World world, List<Double6IntArrayPackage> d6iaps) {
		Double6IntArrayPackage d6iap[] = d6iaps.toArray(new Double6IntArrayPackage[d6iaps.size()]);
		for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb))
			network_wrapper.sendTo(new MessageParticle(particle.getParticleID(), d6iap), player);
	}
	
	public static void playSound(SoundEvent sound, SoundCategory category, AxisAlignedBB aabb, World world, List<Double3Float2Package> d3f2ps) {
		Double3Float2Package d3f2p[] = d3f2ps.toArray(new Double3Float2Package[d3f2ps.size()]);
		for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb))
			network_wrapper.sendTo(new MessageSound(sound.getRegistryName().toString(), category.getName(), d3f2p), player);
	}
	
	public static void updateEntityNBT(MessageNBTUpdate.Type type, int id, NBTTagCompound data, EntityPlayerMP player) {
		network_wrapper.sendTo(new MessageNBTUpdate(type, id, data), player);
	}
	
	public static <T extends IMessage & IMessageHandler<T, IMessage>> void init() {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		for (Entry<Class<?>, Side> entry : message_mapping.entrySet())
			registerMessage((Class<T>) entry.getKey(), entry.getValue());
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		Message message = clazz.getAnnotation(Message.class);
		if (message != null)
			if (message.value() != null)
				message_mapping.put(clazz, message.value());
			else
				AlchemyRuntimeException.onException(new NullPointerException(clazz + " -> @Message.value()"));
	}
	
}