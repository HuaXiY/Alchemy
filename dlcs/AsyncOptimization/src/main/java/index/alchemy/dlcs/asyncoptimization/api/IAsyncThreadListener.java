package index.alchemy.dlcs.asyncoptimization.api;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import index.alchemy.api.IFieldContainer;
import index.alchemy.dlcs.asyncoptimization.core.AsyncWorldServer;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.util.IThreadListener;

public interface IAsyncThreadListener extends IThreadListener {
	
	Logger LOGGER = LogManager.getLogger(AsyncWorldServer.class);
	
	void startLoop();
	
	IFieldContainer<Boolean> running();
	
	BlockingQueue<Runnable> runnables();
	
	Thread asyncThread();
	
	default ListenableFuture<Object> addScheduledTask(Runnable runnable) {
		final Runnable srcRunnable = runnable;
		runnable = () -> {
			try {
				srcRunnable.run();
			} catch (Throwable t) {
				if (!(t instanceof ThreadQuickExitException))
					LOGGER.catching(t);
			}
		};
		Callable<Object> callable = Executors.callable(runnable);
		try {
			if (!isCallingFromMinecraftThread()) {
				ListenableFutureTask<Object> futureTask = ListenableFutureTask.create(callable);
				runnables().add(futureTask::run);
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
		try {
			addScheduledTask(runnable).get();
		} catch (Exception e) { throw new RuntimeException(e); }
	}

}
