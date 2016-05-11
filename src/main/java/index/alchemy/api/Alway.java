package index.alchemy.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class Alway {
	
	public static final int SEA_LEVEL = 62;
	
	public static final Map<Thread, Side> SIDE_MAPPING = new HashMap<Thread, Side>();
	
	public static Side getSide() {
		Side side = SIDE_MAPPING.get(Thread.currentThread());
		if (side == null)
			SIDE_MAPPING.put(Thread.currentThread(), side = FMLCommonHandler.instance().getEffectiveSide());
		return side;
	}
	
	public static boolean isServer() {
		return getSide().isServer();
	}
	
	public static boolean isClient() {
		return getSide().isClient();
	}
	
	public static BiomeGenBase getCurrentBiome(EntityPlayer player) {
		return player.worldObj.getBiomeGenForCoords(new BlockPos((int) player.posX, 0, (int) player.posZ));
	}
	
	public static BiomeGenBase getCurrentBiome(World world, int x, int z) {
		return world.getBiomeGenForCoords(new BlockPos(x, 0, z));
	}
	
}