package index.alchemy.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import index.alchemy.core.debug.AlchemyRuntimeException;

public final class AlchemyThreadManager {
	
	public static class OtherThreadThrowable extends Throwable {
		
		public final Thread thread = Thread.currentThread();

		public OtherThreadThrowable(Throwable e) {
			super(e);
		}
		
	}
	
	public AlchemyThreadManager() {
		this(1, 1, 10, 50);
	}
	
	public AlchemyThreadManager(int max, int skip) {
		this(1, max, 10, skip);
	}

	public AlchemyThreadManager(int max, int listAddThreshold, int skip) {
		this(1, max, listAddThreshold, skip);
	}

	public AlchemyThreadManager(int min, int max, int listAddThreshold, int skipFlag) {
		this.skipFlag = skipFlag;
		this.listAddThreshold = listAddThreshold;
		this.max = Math.max(Math.min(max, Runtime.getRuntime()
				.availableProcessors() - 1), 1);
		this.min = Math.max(Math.min(max, min), 1);
		for (int i = 0; i < this.min; i++)
			addThread();
	}
	
	private static int id;
	private int index = -1, size = -1, min, max, listAddThreshold, warning, num, skipFlag;
	private List<Threads> lt = new ArrayList<Threads>();
	private WriteLock lock = new ReentrantReadWriteLock().writeLock();

	private final class Threads extends Thread {
		Threads() {
			lock.lock();
			setName("ThreadManager-" + id++);
			lock.unlock();
			start();
		}

		private int skip;
		private boolean running = true;
		private List<Runnable> list = new LinkedList<Runnable>();

		@Override
		public void run() {
			while (running) {
				if (list.size() > listAddThreshold)
					if (list.size() > skipFlag)
						list.clear();
					else
						addThread();
				if (list.size() > 0) {
					lock.lock();
					Runnable run = list.get(0);
					lock.unlock();
					try {
						run.run();
					} catch (Throwable e) {
						AlchemyModLoader.logger.error("[ThreadManager]Catch a Throwable in runtime loop: ");
						AlchemyRuntimeException.onException(new OtherThreadThrowable(e));
					}
					lock.lock();
					list.remove(0);
					lock.unlock();
					skip = 0;
				} else
					try {
						if (++skip > 100) {
							if (size > 1) {
								break;
							} else
								Thread.sleep(1000);
						} else
							Thread.sleep(10);
					} catch (Exception e) {}
			}
			deleltThread(Threads.this);
		}
	}
	
	

	private void deleltThread(Threads t) {
		lock.lock();
		lt.remove(t);
		size--;
		lock.unlock();
	}

	public void addThread() {
		if (size > max && ++warning > 100) {
			AlchemyModLoader.logger.error("Warning: ThreadManager can't meet the list needs.(" + ++num + ")");
			return;
		}
		lock.lock();
		lt.add(new Threads());
		size++;
		lock.unlock();
	}

	public void add(Runnable r) {
		lock.lock();
		lt.get(++index > size ? 0 : index).list.add(r);
		lock.unlock();
	}
	
}
