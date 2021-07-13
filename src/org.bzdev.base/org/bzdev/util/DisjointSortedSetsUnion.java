package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Set implementation backed by a collection of disjoint sets.  
 * The set is immutable in that elements cannot be added explicitly -
 * only the sets it backs can be modified .  These sets must be
 * disjoint for the iterator to not contain duplicate entries and for
 * size() and hashCode() to return the correct values.
 */

public class DisjointSortedSetsUnion<E> implements Set<E> {
    SortedSet<E> set1 = null;
    SortedSet<E> set2 = null;

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor.
     * When this constructor is used, additional sets cannot be added
     * and the iterator preserves order. The elements in each set must
     * implement the Comparable interface (checked at runtime) and the
     * set implementations must produce elements in ascending order.
     * <p>
     * Typically, each set will be a TreeSet&lt;E&gt;, although any other
     * Set implementation that provides the desired ordering can be used.
     * @param set1 the first set
     * @param set2 the second set
     */
    public DisjointSortedSetsUnion(SortedSet<E> set1, SortedSet<E>set2) {
	this.set1 = set1;
	this.set2 = set2;
    }

    public boolean add(E e) {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public boolean addAll(Collection<? extends E> c) {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public void clear() {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof Set)) return false;
	Set set = (Set) o;
	int sz = set1.size() + set2.size();
	int hc = set1.hashCode() + set2.hashCode();
	if (sz != set.size()) return false;
	if (hc != set.hashCode()) return false;
	Iterator<E> it = iterator();
	while (it.hasNext()) {
	    E elem = it.next();
	    if (!set.contains(elem)) return false;
	}
	return true;
    }

    public boolean contains(Object o) {
	return set1.contains(o) || set2.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
	for(Object o: c) {
	    if (!contains(o)) {
		return false;
	    }
	}
	return true;
    }

    public int hashCode() {
	return set1.hashCode() + set2.hashCode();
    }

    public boolean isEmpty() {
	return set1.isEmpty() && set2.isEmpty();
    }

    class EmptyIterator<T> implements Iterator<T> {
	public T next() {return null;}
	public boolean  hasNext() {return false;}
	public void remove() {}
    }
    EmptyIterator<E> emptyIterator = new EmptyIterator<E>();

    class OurOrderedSetIterator implements Iterator<E> {
	Iterator<E> it1 = set1.iterator();
	Iterator<E> it2 = set2.iterator();
	boolean has1 = it1.hasNext();
	boolean has2 = it1.hasNext();
	E e1 = has1? it1.next(): null;
	E e2 = has2? it2.next(): null;

	public boolean hasNext() {
	    return has1 || has2;
	}

	@SuppressWarnings("unchecked")
	public E next() {
	    if (has1 && has2) {
		int cmp = ((Comparable<E>)e1).compareTo(e2);
		if (cmp < 0) {
		    try {
			return e1;
		    } finally {
			has1 = it1.hasNext();
			if (has1) e1 = it1.next();
		    }
		} else {
		    try {
			return e2;
		    } finally {
			has2 = it2.hasNext();
			if (has2) e2 = it2.next();
		    }
		}
	    } else if (has1) {
		try {
		    return e1;
		} finally {
		    has1 = it1.hasNext();
		    if (has1) e1 = it1.next();
		}
	    } else if (has2) {
		try {
		    return e2;
		} finally {
		    has2 = it2.hasNext();
		    if (has2) e2 = it2.next();
		}
	    } else {
		throw new NoSuchElementException();
	    }
	}

	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException(errorMsg("unsupported"));
	}
    }

    public Iterator<E> iterator() {
	return  new OurOrderedSetIterator();
    }

    public boolean remove(Object o) {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }

    public int size() {
	return set1.size() + set2.size();
    }

    public Object[] toArray() {
	Object[] result = new Object[size()];
	int i = 0;
	for(E element: this) {
	    result[i++] = element;
	}
	return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) 
	throws ArrayStoreException, NullPointerException 
    {
	int sz = size();
	if (a.length < sz) {
	    a = (T[])java.lang.reflect.Array.newInstance
		    (a.getClass().getComponentType(), sz);
	}
	int i = 0;
	Iterator<E>it = iterator();
	for (i = 0; i < sz; i++) {
	    a[i] = (T)it.next();
	}
	for ( ; i < a.length; i++) {
	    a[i] = null;
	}
	return a;
    }
}

//  LocalWords:  exbundle hashCode runtime TreeSet lt
