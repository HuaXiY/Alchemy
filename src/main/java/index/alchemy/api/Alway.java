package index.alchemy.api;

import java.util.HashMap;
import java.util.Map;

import index.alchemy.core.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Alway {
	
	public static final int SEA_LEVEL = 62;
	
	public static final Map<Thread, Side> SIDE_MAPPING = new HashMap<Thread, Side>();
	
	public static boolean isAlchemyModLoaded() {
		return Loader.isModLoaded(Constants.MOD_ID);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean isPlaying() {
		return Minecraft.getMinecraft().thePlayer != null;
	}
	
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
	
	public static Biome getCurrentBiome(EntityPlayer player) {
		return getCurrentBiome(player.worldObj, (int) player.posX, (int) player.posZ);
	}
	
	public static Biome getCurrentBiome(World world, int x, int z) {
		return world.getBiomeForCoordsBody(new BlockPos(x, 0, z));
	}
	
}