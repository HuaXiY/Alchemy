package index.alchemy.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.SideHelper;
import index.project.version.annotation.Omega;

import net.minecraftforge.fml.relauncher.Side;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Omega
@ThreadSafe
public final class AlchemyThreadManager {
    
    private static final Logger logger = LogManager.getLogger(AlchemyThreadManager.class.getSimpleName());
    
    public static class OtherThreadThrowable extends Throwable {
        
        private static final long serialVersionUID = -6650562972188195047L;
        
        public final Thread thread = Thread.currentThread();
        
        public OtherThreadThrowable(Throwable e) {
            super(e);
        }
        
    }
    
    public AlchemyThreadManager(int min, int max, int listAddThreshold, int skipFlag, int recyclingCount, int shortSleepTime, int longSleepTime, int warningThreshold) {
        this.skipFlag = skipFlag;
        this.listAddThreshold = listAddThreshold;
        this.recyclingCount = recyclingCount;
        this.shortSleepTime = shortSleepTime;
        this.longSleepTime = longSleepTime;
        this.warningThreshold = warningThreshold;
        this.max = max;
        this.min = Math.max(Math.min(max, min), 1);
        for (int i = 0; i < this.min; i++)
            addThread();
    }
    
    protected static int id;
    protected int index = -1, size = -1, min, max, listAddThreshold, warning, num, skipFlag, recyclingCount, shortSleepTime, longSleepTime, warningThreshold;
    protected List<WorkThread> works = Lists.newArrayList();
    protected WriteLock lock = new ReentrantReadWriteLock().writeLock();
    
    protected static int nextId() { return id++; }
    
    protected final class WorkThread extends Thread {
        
        {
            setName(toString() + "-" + toString());
            start();
        }
        
        private int skip;
        private boolean running = true;
        private LinkedList<Runnable> list = Lists.newLinkedList();
        
        @Override
        public void run() {
            while (running) {
                if (list.size() > listAddThreshold)
                    if (list.size() > skipFlag)
                        list.clear();
                    else
                        addThread();
                lock.lock();
                Runnable run;
                try {
                    run = list.poll();
                } finally {
                    lock.unlock();
                }
                if (run != null) {
                    try {
                        run.run();
                    } catch (Throwable e) {
                        AlchemyModLoader.logger.error("[ThreadManager]Catch a Throwable in runtime loop: ", e);
                        AlchemyRuntimeException.onException(new OtherThreadThrowable(e));
                    }
                    skip = 0;
                }
                else
                    try {
                        if (++skip > recyclingCount) {
                            if (size > min) {
                                break;
                            }
                            else
                                Thread.sleep(longSleepTime);
                        }
                        else
                            Thread.sleep(shortSleepTime);
                    } catch (Exception e) {}
            }
            deleltThread(WorkThread.this);
        }
        
    }
    
    protected void deleltThread(WorkThread t) {
        lock.lock();
        try {
            works.remove(t);
            size--;
        } finally {
            lock.unlock();
        }
    }
    
    public void addThread() {
        if (size >= max) {
            if (warningThreshold > 0 && ++warning > warningThreshold)
                logger.error("Warning: ThreadManager can't meet the list needs.(" + ++num + ")");
            return;
        }
        lock.lock();
        try {
            works.add(new WorkThread());
            size++;
        } finally {
            lock.unlock();
        }
    }
    
    public void add(Runnable runnable) {
        lock.lock();
        try {
            works.get(++index > size ? (index = 0) : index).list.add(runnable);
        } finally {
            lock.unlock();
        }
    }
    
    public static Thread runOnNewThread(Runnable runnable) {
        return runOnNewThread(runnable, null, SideHelper.side());
    }
    
    public static Thread runOnNewThread(Runnable runnable, String name) {
        return runOnNewThread(runnable, name, SideHelper.side());
    }
    
    public static Thread runOnNewThread(Runnable runnable, @Nullable String name, Side side) {
        return new Thread(SideHelper.mapSideThreadGroup(side), runnable) {
            
            {
                setName(Optional.ofNullable(name).orElse(AlchemyConstants.MOD_NAME) + "-" + nextId());
                start();
            }
            
        };
    }
    
}
