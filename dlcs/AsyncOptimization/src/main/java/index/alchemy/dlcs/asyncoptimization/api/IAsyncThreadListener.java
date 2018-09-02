package index.alchemy.dlcs.asyncoptimization.api;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import index.alchemy.api.IFieldContainer;
import index.alchemy.dlcs.asyncoptimization.core.AsyncWorldServer;
import index.alchemy.dlcs.asyncoptimization.core.WaitingRunnable;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.util.IThreadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;

public interface IAsyncThreadListener extends IThreadListener {
	
	Logger LOGGER = LogManager.getLogger(AsyncWorldServer.class);
	
	void startLoop();
	
	IFieldContainer<Boolean> running();
	
	BlockingDeque<Runnable> runnables();
	
	Thread asyncThread();

	@Nonnull
	default ListenableFuture<Object> addScheduledTask(@Nonnull Runnable runnable) {
		return addScheduledTask(runnable, false);
	}

    @Nonnull
	default ListenableFuture<Object> addScheduledTask(@Nonnull  Runnable runnable, boolean isSync) {
		Callable<Object> callable = () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                if (!(t instanceof ThreadQuickExitException))
                    LOGGER.catching(t);
            }
            return null;
        };
		try {
			if (!isCallingFromMinecraftThread() || asyncThread().isInterrupted()) {
				ListenableFutureTask<Object> futureTask = ListenableFutureTask.create(callable);
				if (isSync)
					runnables().add(futureTask);
				else {
					// Thread.currentThread is the caller thread, not the queue thread!
					asyncThread().interrupt(); // Notify the thread that another thread is waiting for it.
					runnables().add(new WaitingRunnable(Thread.currentThread(), runnable));
				}
				return futureTask;
			} else
				try {
					return Futures.immediateFuture(callable.call());
				} catch (Exception e) {
					return Futures.immediateFailedCheckedFuture(e);
				}
		} catch (Exception e) {
			LOGGER.error(asyncThread() + " - " + running() + " - " + running().get());
			throw new RuntimeException(e);
		}
	}
	
	default void syncCall(Runnable runnable) {
		// Calling from the caller thread, not the queueing thread.
		if (Thread.interrupted()) { // If a thread is waiting the caller thread, the caller thread should immediately process the request from that thread *before* waiting for next task.
			for (Runnable r : runnables()) {
				if (r instanceof WaitingRunnable && ((WaitingRunnable) r).getSourceThread() == Thread.currentThread()) {
					r.run();
					break; // Only one task possible because we know the source thread is already blocked, so only one task can be submitted
				}
			}
		}
		try {
			addScheduledTask(runnable, true).get();
		} catch (Exception e) { throw new RuntimeException(e); }
	}

}
