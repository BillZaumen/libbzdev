package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Iterator that filters out elements.
 */
public class FilteringIterator<T> implements Iterator<T> {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    Set<T> filter = new HashSet<T>();
    Iterator<T> it;

    /**
     * Constructor.
     * @param it the iterator to filter
     */
    public FilteringIterator(Iterator<T>it) {
	this.it = it;
    }

    /**
     * Add an element to filter.
     * The iterator will return a sequence of elements.  Those elements that
     * match ones added to the filter will be ignored.
     * An element o matches an element e in the filter if either o and e are
     * both null or if o.equals(e) returns true.
     * @param element the element to filter
     */

    public void addToFilter(T element) {
	filter.add(element);
    }

    /**
     * Add elements to filter.
     * The iterator will return a sequence of elements.  Those elements that
     * match ones added to the filter will be ignored.
     * An element o matches an element e in the filter if either o and e are
     * both null or if o.equals(e) returns true.
     * @param elements the elements to filter
     */
    public void addToFilter(Collection<T> elements) {
	for (T element: elements) addToFilter(element);
    }

    
    // Following methods are defined by Iterator

    T next = null;
    public boolean hasNext() {
	if (next != null) {
	    return true;
	} else {
	    while (it.hasNext()) {
		next = it.next();
		if (!filter.contains(next)) {
		    return true;
		}
	    }
	    return false;
	}
    }

    public T next() throws NoSuchElementException {
	while (next == null && it.hasNext()) {
	    next = it.next();
	    if (filter.contains(next)) {
		next = null;
	    }
	}
	if (next == null) throw new NoSuchElementException(errorMsg("noNext"));
	else {
	    T result = next;
	    next = null;
	    return result;
	}
    }

    public void remove() 
	throws UnsupportedOperationException, IllegalStateException 
    {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }    
}



//  LocalWords:  exbundle noNext
