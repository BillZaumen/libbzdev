package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Class for an Iterator whose implementation uses an Enumeration.
 * The Iterator class was introduced in Java 1.2 as an improvement
 * over the Enumeration class.  Unfortunately, some APIs require
 * an Enumeration and others require an Iterator. This class provides
 * a bridge between the two APIs.
 */
public class EnumerationIterator<E> implements Iterator<E> {
    Enumeration<E> enumeration;

    /**
     * Constructor.
     * The Enumeration passed to the constructor will be modified
     * as this class is used.
     * @param enumeration an instance of Enumeration
     */
    public EnumerationIterator(Enumeration<E> enumeration) {
	this.enumeration = enumeration;
    }

    @Override
    public E next() throws NoSuchElementException {
	return enumeration.nextElement();
    }

    @Override
    public boolean hasNext() {
	return enumeration.hasMoreElements();
    }

    @Override
    public void remove()
	throws UnsupportedOperationException, IllegalStateException
    {
	throw new UnsupportedOperationException();
    }

    /**
     * Generate an Iterable based on an Enumeration so that a foreach
     * clause can be used.
     * @param enumeration the Enumeration
     * @return an Iterable that will allow the enumeration to be used
     *         by a foreach clause
     */
    public static <E> Iterable<E> iterable(final Enumeration<E> enumeration) {
	return new Iterable<E>() {
	    public Iterator<E> iterator() {
		return new EnumerationIterator<E>(enumeration);
	    }
	};
    }
}
//  LocalWords:  exbundle APIs Iterable

