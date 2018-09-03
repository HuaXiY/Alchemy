package index.alchemy.dlcs.asyncoptimization.api;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class WaitableHashMap<K, V> extends HashMap<K, V> {
    private ReentrantLock lock = new ReentrantLock(true);
    private Condition emptyCond = lock.newCondition();

    public void waitAndPut(K key, V value) {
        lock.lock();
        try {
            while (containsKey(key)) {
                try {
                    emptyCond.await();
                } catch (InterruptedException e) {
                    // IGNORED
                }
            }

            put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public void executeAndClearIfPresentAndRun(K key, Consumer<V> func, Runnable runnable) {
        lock.lock();
        V v;
        try {
            v = get(key);
            if (v != null) func.accept(v);
            remove(key);
            runnable.run();
            emptyCond.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void executeAndClearIfPresent(K key, Consumer<V> func) {
        lock.lock();
        try {
            var v = get(key);
            if (v != null) func.accept(v);
            remove(key);
            emptyCond.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
