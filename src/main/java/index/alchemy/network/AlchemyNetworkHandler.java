package index.alchemy.network;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;

import com.google.common.collect.Maps;

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
import index.alchemy.util.Always;
import index.alchemy.util.Tool;
import index.project.version.annotation.Omega;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.LoaderState.ModState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Loading
@Init(state = ModState.PREINITIALIZED)
public class AlchemyNetworkHandler {
	
	public static final String CATEGORY_NETWORK ="network";
	
	@Nonnegative
	@Config(category = CATEGORY_NETWORK, comment = "Can hear the sound of the range.", min = 0)
	private static int sound_range = 32;
	
	public static int getSoundRange() {
		return sound_range;
	}
	
	@Nonnegative
	@Config(category = CATEGORY_NETWORK, comment = "Can see the particle of the range.", min = 0)
	private static int particle_range = 32;
	
	public static int getParticleRange() {
		return particle_range;
	}
	
	@Nonnegative
	@Config(category = CATEGORY_NETWORK, comment = "Can receive the change of the tileentity of the range.", min = 0)
	private static int tileentity_update_range = 128;
	
	public static int getTileEntityUpdateRange() {
		return tileentity_update_range;
	}
	
	private static final SimpleNetworkWrapper network_wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(AlchemyConstants.MOD_ID);
	
	private static final Map<Class<?>, Side> message_mapping = Maps.newLinkedHashMap();
	
	private static int id = -1;
	
	private static synchronized int next() {
		return ++id;
	}
	
	public static <T extends IMessage & IMessageHandler<T, IMessage>> void addMessageMapping(Class<T> clazz, Side side) {
		message_mapping.put(clazz, side);
	}

	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz, Side side) {
		network_wrapper.registerMessage(clazz, clazz, next(), side);
		AlchemyInitHook.push_event(clazz);
	}
	
	public static void registerMessage(INetworkMessage<?> handle) {
		if (handle instanceof INetworkMessage.Client)
			registerMessage((INetworkMessage.Client<?>) handle);
		if (handle instanceof INetworkMessage.Server)
			registerMessage((INetworkMessage.Server<?>) handle);
	}
	
	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(INetworkMessage.Client<T> handle) {
		network_wrapper.registerMessage(Tool.instance(handle.getClientMessageClass()), handle.getClientMessageClass(), next(), Side.CLIENT);
	}
	
	public static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(INetworkMessage.Server<T> handle) {
		network_wrapper.registerMessage(Tool.instance(handle.getServerMessageClass()), handle.getServerMessageClass(), next(), Side.SERVER);
	}
	
	@SideOnly(Side.CLIENT)
	public static void openGui(IGuiHandle handle) {
		sendToServer(new MessageOpenGui(AlchemyEventSystem.getGuiIdByGuiHandle(handle)));
	}
	
	@SideOnly(Side.CLIENT)
	public static void sendToServer(IMessage message) {
		if (message != null)
			network_wrapper.sendToServer(message);
	}
	
	public static void spawnParticle(EnumParticleTypes particle, Stream<EntityPlayer> players, List<Double6IntArrayPackage> d6iaps) {
		sendToClient(players, new MessageParticle(particle.getParticleID(), d6iaps.toArray(new Double6IntArrayPackage[d6iaps.size()])));
	}
	
	public static void playSound(SoundEvent sound, SoundCategory category, Stream<EntityPlayer> players, List<Double3Float2Package> d3f2ps) {
		sendToClient(players, new MessageSound(sound.getRegistryName().toString(), category.getName(), d3f2ps.toArray(new Double3Float2Package[d3f2ps.size()])));
	}
	
	public static void sendToClient(Stream<EntityPlayer> players, IMessage message) {
		if (message != null)
			players.filter(EntityPlayerMP.class::isInstance).map(EntityPlayerMP.class::cast).forEach(player -> network_wrapper.sendTo(message, player));
	}
	
	public static void sendMessage(Stream<EntityPlayer> players, IMessage message) {
		if (Always.isClient())
			sendToServer(message);
		else
			sendToClient(players, message);
	}
	
	@Deprecated
	public static Stream<EntityPlayer> getPlayerFromContext(MessageContext context) {
		if (Always.isClient())
			return Stream.of(Minecraft.getMinecraft().player);
		else
			return Stream.of(context.getServerHandler().player);
	}
	
	@SuppressWarnings("unchecked")
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
