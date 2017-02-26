package index.alchemy.core;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.jooq.lambda.tuple.Tuple;

import baubles.client.BaublesRenderLayer;
import biomesoplenty.api.biome.BOPBiomes;
import index.alchemy.api.IMaterialContainer;
import index.alchemy.api.annotation.Hook;
import index.alchemy.entity.ai.EntityAIEatMeat;
import index.alchemy.util.Tool;
import index.alchemy.world.biome.AlchemyBiomeLoader;
import index.project.version.annotation.Gamma;
import index.project.version.annotation.Omega;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Omega
@Hook.Provider
public class AlchemyHooks {
	
	// IO blocking
	@Hook(value = "biomesoplenty.common.remote.TrailManager#retrieveTrails", isStatic = true)
	public static final Hook.Result retrieveTrails() {
		return Hook.Result.NULL;
	}
	
//	@Hook("net.minecraft.world.World#getCollisionBoxes")
//	public static void getCollisionBoxes(World world, AxisAlignedBB bb) {
//		if (true)
//			return;
//		for (StackTraceElement element : Tool.getStackTrace())
//			if (element.getClassName().startsWith("net.gobbob.")) {
//				System.out.println(bb);
//				Tool.where();
//				return;
//			}
////		Tool.where();
////		System.out.println(storage.getYLocation());
////		System.out.println(Arrays.toString(Tool.<byte[]>$(newBlocklightArray, "data")));
//	}
	
//	@Hook("net.minecraft.pathfinding.WalkNodeProcessor#getPathNodeTypeRaw")
//	public static final Hook.Result getPathNodeTypeRaw(WalkNodeProcessor processor, IBlockAccess access, int x, int y, int z) {
//		IBlockState state = access.getBlockState(new BlockPos(x, y, z));
//		if (state.getMaterial() == Material.WOOD && state.getBlock() instanceof BlockFenceGate)
//			return new Hook.Result(state.getValue(BlockFenceGate.OPEN) ? PathNodeType.DOOR_OPEN : PathNodeType.DOOR_WOOD_CLOSED);
//		return Hook.Result.VOID;
//	}
	
	// Biome-Debug, will remove
	
//	@Gamma
//	public static Biome getBiome(int x, int y) {
//		return Tool.isNullOr(Biome.getBiome(x % 20 + y / 20), BOPBiomes.flower_island.get());
//	}
//	
	@Gamma
	public static Biome getBiome() {
		return /*BOPBiomes.flower_island.get();*/AlchemyBiomeLoader.dragon_island;
	}
	
	@Gamma
	static boolean debug_biome_flag;
	
	@Gamma
	@Hook("net.minecraft.world.biome.BiomeProvider#getBiomes")
	public static final Hook.Result getBiomes(BiomeProvider provider, @Nullable Biome[] oldBiomeList,
			int x, int z, int width, int depth, boolean cacheFlag) {
		if (debug_biome_flag)
			return Hook.Result.VOID;
		Biome result[] = new Biome[width * depth];
		Arrays.fill(result, getBiome());
		return new Hook.Result(result);
	}
	
	@Gamma
	@Hook("net.minecraft.world.biome.BiomeProvider#getBiomes")
	public static final Hook.Result getBiome(BiomeProvider provider, BlockPos pos, Biome defaultBiome) {
		if (debug_biome_flag)
			return Hook.Result.VOID;
		return new Hook.Result(getBiome());
	}
	
	@Gamma
	@Hook("net.minecraft.world.biome.BiomeProvider#getBiomesForGeneration")
	public static final Hook.Result getBiomesForGeneration(BiomeProvider provider, Biome[] biomes,
			int x, int z, int width, int height) {
		if (debug_biome_flag)
			return Hook.Result.VOID;
		Biome result[] = new Biome[width * height];
		Arrays.fill(result, getBiome());
		return new Hook.Result(result);
	}
	
	@Gamma
	@Hook("net.minecraft.world.biome.BiomeProvider#areBiomesViable")
	public static final Hook.Result areBiomesViable(BiomeProvider provider, int x, int z, int radius, List<Biome> allowed) {
		if (debug_biome_flag)
			return Hook.Result.VOID;
		Biome biome = getBiome();
		return allowed.stream().filter(b -> b != biome).count() > 0 ? Hook.Result.FALSE : Hook.Result.TRUE;
	}
	
}
