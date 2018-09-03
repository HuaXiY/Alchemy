package index.alchemy.dlcs.asyncoptimization.api;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

import index.alchemy.api.IFieldContainer;

import index.alchemy.dlcs.asyncoptimization.core.AsyncWorldServer;
import index.alchemy.dlcs.asyncoptimization.core.WaitingRunnable;

import index.alchemy.util.cache.ThreadCache;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.util.IThreadListener;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface IAsyncThreadListener extends IThreadListener {
    WaitableHashMap<Thread, Runnable> WAITING_THREADS = new WaitableHashMap<>();

    Logger LOGGER = LogManager.getLogger(AsyncWorldServer.class);
    
    void startLoop();
    
    IFieldContainer<Boolean> running();
    
    BlockingDeque<Runnable> runnables();
    
    Thread asyncThread();
    
    @Nonnull
    default ListenableFuture<Object> addScheduledTask(@Nonnull Runnable runnable) {
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
            if (!isCallingFromMinecraftThread() || asyncThread() == null || asyncThread().isInterrupted()) {
                ListenableFutureTask<Object> futureTask = ListenableFutureTask.create(callable);
                runnables().add(futureTask);
                return futureTask;
            }
            else
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
        if (Thread.currentThread() == asyncThread()) {
            runnable.run();
            return;
        }
        var ft = ListenableFutureTask.create(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                if (!(t instanceof ThreadQuickExitException))
                    LOGGER.catching(t);
            }
            return null;
        });
        WAITING_THREADS.executeAndClearIfPresentAndRun(Thread.currentThread(), Runnable::run, () -> {
            WAITING_THREADS.waitAndPut(asyncThread(), ft);
            asyncThread().interrupt();
        });
        try {
            ft.get();
        } catch (InterruptedException | ExecutionException e) {
            // IGNORED
        }
    }
}
