package index.alchemy.util;

import index.project.version.annotation.Omega;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

@Omega
public class FakeChunkPrimer extends ChunkPrimer {
	
	protected World world;
	protected ChunkPrimer chunkPrimer;
	protected int cx, cz;
	
	public FakeChunkPrimer(World world, ChunkPrimer chunkPrimer, int x, int z) {
		this.world = world;
		this.chunkPrimer = chunkPrimer;
		cx = x >> 4;
		cz = z >> 4;
	}
	
	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		int chunkX = x >> 4, chunkZ = z >> 4;
		if (chunkX == cx && chunkZ == cz)
			return chunkPrimer.getBlockState(x & 15, y, z & 15);
		return world.getBlockState(new BlockPos(x, y, z));
	}
	
	@Override
	public void setBlockState(int x, int y, int z, IBlockState state) {
		int chunkX = x >> 4, chunkZ = z >> 4;
		if (chunkX == cx && chunkZ == cz)
			chunkPrimer.setBlockState(x & 15, y, z & 15, state);
		else
			world.setBlockState(new BlockPos(x, y, z), state, 2);
	}
	
}
