package index.alchemy.dlcs.asyncoptimization.core;

public class WaitingRunnable implements Runnable {
    private Thread sourceThread;
    private Runnable wrappedRunnable;

    public WaitingRunnable(Thread sourceThread, Runnable wrappedRunnable) {
        this.sourceThread = sourceThread;
        this.wrappedRunnable = wrappedRunnable;
    }

    public Thread getSourceThread() {
        return sourceThread;
    }

    @Override
    public void run() {
        wrappedRunnable.run();
    }
}
