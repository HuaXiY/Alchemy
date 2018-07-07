package index.alchemy.dlcs.asyncoptimization.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import index.alchemy.api.annotation.Patch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderWorker;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator.Status;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Patch("net.minecraft.client.renderer.chunk.ChunkRenderWorker")
public class Patch$ChunkRenderWorker extends ChunkRenderWorker {
	
	private static final Logger LOGGER = LogManager.getLogger();

	@Patch.Exception
	public Patch$ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcherIn) {
		super(chunkRenderDispatcherIn);
	}
	
	@Override
	public void processTask(ChunkCompileTaskGenerator generator) throws InterruptedException {
		if (generator == null)
			return;
		generator.getLock().lock();
		try {
			if (generator.getStatus() != ChunkCompileTaskGenerator.Status.PENDING) {
				if (!generator.isFinished())
					LOGGER.warn("Chunk render task was {} when I expected it to be pending; ignoring task", generator.getStatus());
				return;
			}
			BlockPos chunkPos = generator.getRenderChunk().getPosition();
			ChunkCompileTaskGenerator.Status status = !isChunkExisting(chunkPos, generator.getRenderChunk().getWorld()) ?
					ChunkCompileTaskGenerator.Status.DONE : ChunkCompileTaskGenerator.Status.COMPILING;
			generator.setStatus(status);
			if (status == Status.DONE)
				return;
		} finally { generator.getLock().unlock(); }
		Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
		if (viewEntity == null)
			generator.finish();
		else {
			generator.setRegionRenderCacheBuilder(getRegionRenderCacheBuilder());
			float x = (float) viewEntity.posX;
			float y = (float) viewEntity.posY + viewEntity.getEyeHeight();
			float z = (float) viewEntity.posZ;
			ChunkCompileTaskGenerator.Type type = generator.getType();
			if (type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK)
				generator.getRenderChunk().rebuildChunk(x, y, z, generator);
			else if (type == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY)
				generator.getRenderChunk().resortTransparency(x, y, z, generator);
			generator.getLock().lock();
			try {
				if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING) {
					if (!generator.isFinished())
						LOGGER.warn("Chunk render task was {} when I expected it to be compiling; aborting task", generator.getStatus());
					freeRenderBuilder(generator);
					return;
				}
				generator.setStatus(ChunkCompileTaskGenerator.Status.UPLOADING);
			} finally {
				generator.getLock().unlock();
			}
			final CompiledChunk compiledChunk = generator.getCompiledChunk();
			ArrayList<ListenableFuture<Object>> list = Lists.newArrayList();
			if (type == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK)
				for (BlockRenderLayer blockrenderlayer : BlockRenderLayer.values())
					if (compiledChunk.isLayerStarted(blockrenderlayer))
						list.add(chunkRenderDispatcher.uploadChunk(blockrenderlayer, generator.getRegionRenderCacheBuilder()
								.getWorldRendererByLayer(blockrenderlayer), generator.getRenderChunk(), compiledChunk, generator.getDistanceSq()));
			else if (type == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY)
				list.add(chunkRenderDispatcher.uploadChunk(BlockRenderLayer.TRANSLUCENT, generator.getRegionRenderCacheBuilder()
							.getWorldRendererByLayer(BlockRenderLayer.TRANSLUCENT), generator.getRenderChunk(), compiledChunk, generator.getDistanceSq()));
			final ListenableFuture<List<Object>> future = Futures.allAsList(list);
			generator.addFinishRunnable(() -> future.cancel(false));
			Futures.addCallback(future, new GeneratorFutureCallback(this, generator, compiledChunk));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class GeneratorFutureCallback implements FutureCallback<List<Object>> {
		
		protected final ChunkRenderWorker worker;
		protected final ChunkCompileTaskGenerator generator;
		protected final CompiledChunk compiledChunk;
		
		public GeneratorFutureCallback(ChunkRenderWorker worker, ChunkCompileTaskGenerator generator, CompiledChunk compiledChunk) {
			this.worker = worker;
			this.generator = generator;
			this.compiledChunk = compiledChunk;
		}
		
		public void onSuccess(@Nullable List<Object> list) {
			worker.freeRenderBuilder(generator);
			generator.getLock().lock();
			try {
				if (generator.getStatus() == ChunkCompileTaskGenerator.Status.UPLOADING) {
					generator.setStatus(ChunkCompileTaskGenerator.Status.DONE);
				}
			} finally {
				generator.getLock().unlock();
			}
			generator.getRenderChunk().setCompiledChunk(compiledChunk);
		}
		
		public void onFailure(Throwable throwable) {
			worker.freeRenderBuilder(generator);
			if (!(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException))
				Minecraft.getMinecraft().crashed(CrashReport.makeCrashReport(throwable, "Rendering chunk"));
		}
		
	}
	
}