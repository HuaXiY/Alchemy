package index.alchemy.util;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import index.project.version.annotation.Omega;

@Omega
public interface FunctionHelper {
	
	static <T> void always(Consumer<T> consumer, T... ts) {
		Arrays.stream(ts).forEach(consumer);
	}
	
	static <A, B> void alwaysA(BiConsumer<A, B> consumer, A a, B... bs) {
		Arrays.stream(bs).forEach(b -> consumer.accept(a, b));
	}
	
	static <A, B> void alwaysB(BiConsumer<A, B> consumer, B b, A... as) {
		Arrays.stream(as).forEach(a -> consumer.accept(a, b));
	}
	
	static <A, B> BiConsumer<B, A> exchange(BiConsumer<A, B> consumer) {
		return (b, a) -> consumer.accept(a, b);
	}
	
	static Runnable link(Runnable... runnables) {
		return () -> {
			for (Runnable runnable : runnables)
				runnable.run();
		};
	}
	
	static <T> Consumer<T> link(Consumer<T>... consumers) {
		return t -> {
			for (Consumer<T> consumer : consumers)
				consumer.accept(t);
		};
	}
	
	static <A> Runnable link(Supplier<A> supplier, Consumer<A> consumer) {
		return () -> consumer.accept(supplier.get());
	}
	
	static <A, B> Consumer<A> link1(Function<A, B> function, Consumer<B> consumer) {
		return a -> consumer.accept(function.apply(a));
	}
	
	static <A, B, C> BiConsumer<A, C> link2(Function<A, B> function, BiConsumer<B, C> consumer) {
		return (a, c) -> consumer.accept(function.apply(a), c);
	}
	
	static <A, B> Consumer<B> link2(Supplier<A> supplier, BiConsumer<A, B> consumer) {
		return b -> consumer.accept(supplier.get(), b);
	}
	
	static <A, B> Supplier<B> link1(Supplier<A> supplier, Function<A, B> function) {
		return () -> function.apply(supplier.get());
	}
	
	@FunctionalInterface interface ExRunnable { void run() throws Throwable; }
	
	static Runnable onThrowable(ExRunnable runnable, Consumer<Throwable> handle) {
		return () -> { try { runnable.run(); } catch (Throwable t) { handle.accept(t); } };
	}
	
	@FunctionalInterface interface ExConsumer<T> { void accept(T t) throws Throwable; }
	
	static <T> Consumer<T> onThrowable(ExConsumer<T> consumer, Consumer<Throwable> handle) {
		return o -> { try { consumer.accept(o); } catch (Throwable t) { handle.accept(t); } };
	}
	
	@FunctionalInterface interface ExFunction<A, B> { B apply(A a) throws Throwable; }
	
	static <A, B> Function<A, B> onThrowable1(ExFunction<A, B> function, Function<Throwable, B> handle) {
		return o -> { try { return function.apply(o); } catch (Throwable t) { return handle.apply(t); } };
	}
	
}
