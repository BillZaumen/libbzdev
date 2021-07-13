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

public class DisjointSetsUnion<E> implements Set<E> {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    LinkedList<Set<E>> setOfSets = null; 

    /**
     * Constructor.
     */
    public DisjointSetsUnion() {
	setOfSets = new LinkedList<Set<E>>();
    }

    /**
     * Constructor for two sets.
     * This is provided to avoid having to add sets for a common case.
     * @param set1 the first set
     * @param set2 the second set
     */
    public DisjointSetsUnion(Set<E> set1, Set<E>set2) {
	this();
	addSet(set1);
	addSet(set2);
    }


    /**
     * Add a set to the union.
     * This set must be disjoint from any other set in the union, and the
     * union is partially backed by this set.
     * @param set the set to add
     */
    public void addSet(Set<E>set) {
	setOfSets.add(set);
    }

    /**
     * Remove a set to the union.
     * A set that can be successfully removed must be one that was
     * added to the union, either by calling addSet or by a constructor.
     * @param set the set to remove
     */
    public void removeSet(Set<E>set) {
	int len = setOfSets.size();
	for (int i = 0; i < len; i++) {
	    if (setOfSets.get(i) == set) {
		setOfSets.remove(i);
		return;
	    }
	}
    }

    /**
     * Create a view of this object as an unmodifiable set, with
     * DisjointSetUnion-specific methods hidden.
     * Logically equivalent to using Collections.unmodifiableSet but
     * with some performance advantages when using the iterator.
     * @return an unmodifiable set backed by a DisjointSetsUnion
     */
    public Set<E> setView() {
	return new Set<E>() {
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
		return DisjointSetsUnion.this.equals(o);
	    }

	    public boolean contains(Object o) {
		return DisjointSetsUnion.this.contains(o);
	    }

	    public boolean containsAll(Collection<?> c) {
		return DisjointSetsUnion.this.containsAll(c);
	    }

	    public int hashCode() {
		return DisjointSetsUnion.this.hashCode();
	    }

	    public boolean isEmpty() {
		return DisjointSetsUnion.this.isEmpty();
	    }

	    public Iterator<E> iterator() {
		return DisjointSetsUnion.this.iterator();
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
		
	    public int size() {return DisjointSetsUnion.this.size();}

	    public Object[] toArray() {
		return DisjointSetsUnion.this.toArray();
	    }

	    public <T> T[] toArray(T[] a) 
		throws ArrayStoreException, NullPointerException 
	    {
		return DisjointSetsUnion.this.toArray(a);
	    }
	};
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
	int sz = 0; int hc = 0;
	for(Set<E> s: setOfSets) {
	    sz += s.size();
	    hc += s.hashCode();
	}
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
	for(Set<E> set: setOfSets) {
	    if (set.contains(o)) {
		return true;
	    }
	}
	return false;
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
	int result = 0;
	for (Set<E> set: setOfSets) {
	    result += set.hashCode();
	}
	return result;
    }

    public boolean isEmpty() {
	for (Set<E> set: setOfSets) {
	    if (!set.isEmpty()) return false;
	}
	return true;
    }

    class EmptyIterator<T> implements Iterator<T> {
	public T next() {return null;}
	public boolean  hasNext() {return false;}
	public void remove() {}
    }
    EmptyIterator<E> emptyIterator = new EmptyIterator<E>();

    class OurIterator implements Iterator<E> {
	Iterator<Set<E>> sit = setOfSets.iterator();
	Iterator<E> it = sit.hasNext()? sit.next().iterator(): emptyIterator;

	public boolean hasNext() {
	    boolean result = false;
	    while ((result = it.hasNext()) == false && sit.hasNext()) {
		it = sit.next().iterator();
	    }
	    return result;
	}

	public E next() {
	    while (it.hasNext() == false && sit.hasNext()) {
		it = sit.next().iterator();
	    }
	    return it.next();
	}

	public void remove() throws UnsupportedOperationException {
	    throw new UnsupportedOperationException(errorMsg("unsupported"));
	}
    }


    public Iterator<E> iterator() {
	return new OurIterator();
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
	int result = 0;
	for (Set<E> set: setOfSets) {
	    result += set.size();
	}
	return result;
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

//  LocalWords:  exbundle hashCode addSet unmodifiable
//  LocalWords:  DisjointSetUnion unmodifiableSet DisjointSetsUnion
