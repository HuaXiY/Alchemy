package index.alchemy.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import index.alchemy.annotation.Init;
import index.alchemy.annotation.Message;
import index.alchemy.api.INetworkMessage;
import index.alchemy.core.AlchemyInitHook;
import index.alchemy.core.AlchemyModLoader;
import index.alchemy.core.Constants;
import index.alchemy.core.debug.AlchemyRuntimeExcption;
import index.alchemy.util.Tool;
import net.minecraft.entity.player.EntityPlayerMP;
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

@Init(state = ModState.PREINITIALIZED)
public class AlchemyNetworkHandler {
	
	public static final SimpleNetworkWrapper networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.MOD_ID);
	
	private static final Map<Class<?>, Side> message_mapping = new LinkedHashMap<Class<?>, Side>();
	
	private static int id = -1;
	
	private static synchronized int next() {
		return ++id;
	}

	private static <T extends IMessage & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz, Side side) {
		networkWrapper.registerMessage(clazz, clazz, next(), side);
		AlchemyInitHook.push_event(clazz);
	}
	
	public static void registerMessage(INetworkMessage<IMessage> handle) {
		networkWrapper.registerMessage(handle, handle.getMessageClass(), next(), handle.getMessageSide());
	}
	
	@SideOnly(Side.CLIENT)
	public static void openGui(int id) {
		networkWrapper.sendToServer(new MessageOpenGui(id));
	}
	
	public static void spawnParticle(EnumParticleTypes particle, AxisAlignedBB aabb, World world, List<SDouble6Package> d6ps) {
		SDouble6Package d6p[] = Tool.toArray(d6ps, SDouble6Package.class);
		for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb))
			networkWrapper.sendTo(new MessageParticle(particle.getParticleID(), d6p), player);
	}
	
	public static void playSound(SoundEvent sound, SoundCategory category, AxisAlignedBB aabb, World world, List<SDouble3Float2Package> d3f2ps) {
		SDouble3Float2Package d3f2p[] = Tool.toArray(d3f2ps, SDouble3Float2Package.class);
		for (EntityPlayerMP player : world.getEntitiesWithinAABB(EntityPlayerMP.class, aabb))
			networkWrapper.sendTo(new MessageSound(sound.getRegistryName().toString(), category.getName(), d3f2p), player);
	}
	
	public static <T extends IMessage & IMessageHandler<T, IMessage>> void init() {
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
				throw new AlchemyRuntimeExcption(new RuntimeException(new NullPointerException(clazz + " -> @Message.value()")));
	}
	
}