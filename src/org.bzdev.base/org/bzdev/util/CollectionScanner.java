 package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * An Iterable for iterating over multiple collections.
 */
public class CollectionScanner<E> implements Iterable<E> {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    LinkedList<Collection<? extends E>> list = new
	LinkedList<Collection<? extends E>>();

    /**
     * Add a collection.
     * The elements of this collection are added to the sequence of
     * elements generated by an iterator.  If the same collection is added
     * twice, its elements appear twice.
     * @param collection the collection to add.
     * @see #iterator()
     */
    public void add(Collection<? extends E> collection) {
	list.add(collection);
    }

    /**
     * Get an iterator that will iterate over the elements of multiple
     * collections.
     *@return an iterator
     */
    public Iterator<E> iterator() {
	return new Iterator<E>() {
	    Iterator<Collection<? extends E>> cit = list.iterator();
	    Iterator<? extends E> it = null;
	    public boolean hasNext() {
		boolean result = false;
		if (it == null) {
		    while (cit.hasNext()) {
			it = cit.next().iterator();
			if (it.hasNext()) return true;
		    }
		    return false;
		} else {
		    if (it.hasNext()) {
			return true;
		    } else {
			while (cit.hasNext()) {
			    it = cit.next().iterator();
			    if (it.hasNext()) return true;
			}
			return false;
		    }
		}
	    }

	    public E next() {
		if (it == null) {
		    if (hasNext()) {
			return it.next();
		    } else {
			throw new NoSuchElementException(errorMsg("noNext"));
		    }
		} else if (it.hasNext()) {
		    return it.next();
		} else if (hasNext()) {
		    return it.next();
		} else {
		    throw new NoSuchElementException(errorMsg("noNext"));
		}
	    }

	    public void remove() {
		if (it == null) {
		    throw new IllegalStateException
			(errorMsg("noMoreIterations"));
		}
		it.remove();
	    }
	};
    }
}

//  LocalWords:  exbundle Iterable noNext noMoreIterations
