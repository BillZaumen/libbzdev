package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Class for an Enumeration whose implementation uses an Iterator.
 * The Iterator class was introduced in Java 1.2 as an improvement
 * over the Enumeration class.  Unfortunately, some APIs require
 * an Enumeration and others require an Iterator. This class provides
 * a bridge between the two APIs.
 */
public class IteratorEnumeration<E> implements Enumeration<E> {

    Iterator<E> iterator;

    /**
     * Constructor.
     * The Iterator passed to the constructor will be modified as
     * this class is used.
     * @param iterator the iterator
     */
    public IteratorEnumeration(Iterator<E> iterator) {
	this.iterator = iterator;
    }

    @Override
    public E nextElement() throws NoSuchElementException {
	return iterator.next();
    }

    @Override
    public boolean hasMoreElements() {
	return iterator.hasNext();
    }

}
//  LocalWords:  exbundle APIs
