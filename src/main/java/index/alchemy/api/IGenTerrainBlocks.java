package index.alchemy.api;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

@FunctionalInterface
public interface IGenTerrainBlocks {
	
	void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int cx, int cz, double noiseVal);
	
	abstract class InnerBuilder<T extends InnerBuilder<T, G>, G extends IGenTerrainBlocks> {
		
		protected float amountPerChunk;
		
		protected T self() { return (T) this; }
		
		public T amountPerChunk(float a) { amountPerChunk = a; return self(); }
		
		public abstract G create();
		
	}
	
	interface IGenTerrainBuilder<T extends IGenTerrainBlocks> {
		
        T create();
        
    }

}
