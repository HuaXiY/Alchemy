package index.alchemy.util.observer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import index.alchemy.api.ISubElement;
import index.alchemy.api.IViewSupport;
import index.alchemy.api.IWrapper;

public class ObserverWrapperList<T, P> implements List<T>, IWrapper<List<T>>, ISubElement<P>, IViewSupport {
	
	public static interface IChangeObserver<T, P> {
		
		void accept(ObserverWrapperList<T, P> list, T old, T _new);
		
	}
	
	public class ListenerWrapperListIterator implements ListIterator<T>, IWrapper<ListIterator<T>>, ISubElement<ObserverWrapperList<T, P>> {
		
		protected ListIterator<T> wrapper;
		protected ObserverWrapperList<T, P> parent;
		protected T element;
		
		public ListenerWrapperListIterator(ListIterator<T> wrapper, ObserverWrapperList<T, P> parent) {
			this.wrapper = wrapper;
			this.parent = parent;
		}

		@Override
		public boolean hasNext() { return wrapper.hasNext(); }

		@Override
		public T next() { return element = wrapper.next(); }

		@Override
		public boolean hasPrevious() { return wrapper.hasPrevious(); }

		@Override
		public T previous() { return wrapper.previous(); }

		@Override
		public int nextIndex() { return wrapper.nextIndex(); }

		@Override
		public int previousIndex() { return wrapper.previousIndex(); }

		@Override
		public void remove() {
			remove.ifPresent(remove -> remove.accept(parent, element));
			wrapper.remove();
		}

		@Override
		public void set(T e) {
			set.ifPresent(set -> set.accept(parent, element, e));
			wrapper.set(e);
		}

		@Override
		public void add(T e) {
			add.ifPresent(add -> add.accept(parent, element));
			wrapper.add(e);
		}
		
		@Override
		public ListIterator<T> getWrapper() { return wrapper; }

		@Override
		public void setWrapper(ListIterator<T> wrapper) { this.wrapper = wrapper; }
		
		@Override
		public ObserverWrapperList<T, P> getParent() { return parent; }

		@Override
		public void setParent(ObserverWrapperList<T, P> parent) { this.parent = parent; }
		
	}
	
	@Nonnull
	protected List<T> wrapper;
	protected Optional<BiConsumer<ObserverWrapperList<T, P>, T>> add;
	protected Optional<IChangeObserver<T, P>> set;
	protected Optional<BiConsumer<ObserverWrapperList<T, P>, T>> remove;
	protected P parent;
	protected String viewName;
	
	public ObserverWrapperList(@Nonnull List<T> wrapper,
			BiConsumer<ObserverWrapperList<T, P>, T> add,
			IChangeObserver<T, P> set,
			BiConsumer<ObserverWrapperList<T, P>, T> remove, P parent, String viewName) {
		this.wrapper = wrapper;
		this.add = Optional.ofNullable(add);
		this.set = Optional.ofNullable(set);
		this.remove = Optional.ofNullable(remove);
		this.parent = parent;
		this.viewName = viewName;
	}
	
	public ObserverWrapperList(@Nonnull List<T> wrapper,
			BiConsumer<ObserverWrapperList<T, P>, T> add,
			BiConsumer<ObserverWrapperList<T, P>, T> remove, P parent, String viewName) {
		this(wrapper, add, (p, a, b) -> {
			if (remove != null)
				remove.accept(p, a);
			if (add != null)
				add.accept(p, b);
		}, remove, parent, viewName);
	}

	@Override
	public int size() { return wrapper.size(); }

	@Override
	public boolean isEmpty() { return wrapper.isEmpty(); }

	@Override
	public boolean contains(Object o) { return wrapper.contains(o); }

	@Override
	public Iterator<T> iterator() { return listIterator(); }

	@Override
	public Object[] toArray() { return wrapper.toArray(); }

	@Override
	public <A> A[] toArray(A[] a) { return wrapper.toArray(a); }

	@Override
	public boolean add(T e) {
		add.ifPresent(add -> add.accept(this, e));
		return wrapper.add(e);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		boolean result = wrapper.remove(o);
		if (result)
			remove.ifPresent(remove -> remove.accept(this, (T) o));
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) { return wrapper.containsAll(c); }

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return wrapper.addAll(size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		add.ifPresent(add -> c.forEach(e -> add.accept(this, e)));
		return wrapper.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for (Object o : c)
			if (remove(o))
				result = true;
		return result;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean result = false;
		for (Object o : Lists.newLinkedList(this))
			if (!c.contains(o) && remove(o))
				result = true;
		return result;
	}

	@Override
	public void clear() {
		for (Object o : Lists.newLinkedList(this))
			remove(o);
	}

	@Override
	public T get(int index) { return wrapper.get(index); }

	@Override
	public T set(int index, T element) {
		T e = get(index);
		set.ifPresent(set -> set.accept(this, e, element));
		return wrapper.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		add.ifPresent(add -> add.accept(this, element));
		wrapper.add(index, element);
	}

	@Override
	public T remove(int index) {
		T e = get(index);
		return remove(e) ? e : null;
	}

	@Override
	public int indexOf(Object o) { return wrapper.indexOf(o); }

	@Override
	public int lastIndexOf(Object o) { return wrapper.lastIndexOf(o); }

	@Override
	public ListIterator<T> listIterator() {
		return new ListenerWrapperListIterator(wrapper.listIterator(), this);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new ListenerWrapperListIterator(wrapper.listIterator(index), this);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return new ObserverWrapperList<T, P>(wrapper.subList(fromIndex, toIndex),
			add.orElse(null), set.orElse(null), remove.orElse(null), parent, viewName);
	}

	@Override
	public List<T> getWrapper() { return wrapper; }

	@Override
	public void setWrapper(List<T> wrapper) { this.wrapper = wrapper; }
	
	@Override
	public P getParent() { return parent; }

	@Override
	public void setParent(P parent) { this.parent = parent; }

	@Override
	public String getViewName() { return viewName; }
	
	@Override
	public void setViewName(String viewName) { this.viewName = viewName; }

}
