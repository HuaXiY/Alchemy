package index.alchemy.dlcs.asyncoptimization.api;

import java.util.function.Consumer;

public class RunnableRunner implements Consumer<Runnable> {
    @Override
    public void accept(Runnable runnable) {
        runnable.run();
    }
}
