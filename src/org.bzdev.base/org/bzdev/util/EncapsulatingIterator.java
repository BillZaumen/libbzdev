package org.bzdev.util;
import java.util.*;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Encapsulating iterator class.
 * An encapsulating iterator uses an iterator passed to its
 * constructor to generate a sequence of objects of type T.
 * Instead of returning an object of type T when next() is called, an object
 * of a different type E is returned.
 * The method next() is responsible for constructing an object of type E
 * from the corresponding object of type T.
 * <P>
 * One use is in conjunction with a template processor, where objects of
 * type Iterator&lt;TemplateProcessor.KeyMap&gt; can be used for iteration.
 */
public abstract class EncapsulatingIterator<E,T> implements Iterator<E> {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    private Iterator<T> it;
    public EncapsulatingIterator(Iterator<T> it) {
	this.it = it;
    }

    public boolean hasNext() {return it.hasNext();}

    /**
     * Get the next element using the iterator passed to the constructor.
     * This method must be called once and only once by
     * {@link #next() next()} for each call to {@link #next() next()}.
     * @return the next element from the encapsulated iterator.
     */
    protected T encapsulatedNext() {return it.next();}

    /**
     * Return the next element in the iteration.
     * Typically, this method will create a new element of type E and
     * then configure it based on the value returned by a single call
     * to encapsulatedNext().
     * @return the next element in the iteration
     */
    public abstract E next();

    public void remove() {
	throw new UnsupportedOperationException(errorMsg("unsupported"));
    }
}

//  LocalWords:  exbundle lt TemplateProcessor KeyMap
//  LocalWords:  encapsulatedNext
