package org.bzdev.util;
import java.util.EventListener;
import java.lang.reflect.Array;

/**
 * Class implementing a list of event listeners .
 * This class has the same behavior as the class
 * {@link javax.swing.event.EventListenerList}. It is provided so that
 * several modules (e.g., org.bzdev.devqsim and org.bzdev.drama) will
 * not have a dependency on the module java.desktop.
 * @see javax.swing.event.EventListenerList
 */

public class EvntListenerList {
    static final Object[] EMPTYLIST = new Object[0];
    Object[] list = EMPTYLIST;

    /**
     * Constructor.
     */
    public EvntListenerList(){}

    /**
     * Add a listener to the list
     * The listener l's class or one of its superclasses must match the
     * class t.
     * The full list is comprised of a collection of sublists, each
     * distinguished by a class t that serves as a key naming a sublist.
     * @param t the class for the listener
     * @param l the listener, whose type or supertype must match the one
     *        provided by the argument t
     */
    public synchronized <T extends EventListener> void add(Class<T> t, T l) {
	Object[] list2 = new Object[list.length+2];
	System.arraycopy(list, 0, list2, 0, list.length);
	list2[list.length] = t;
	list2[list.length+1] = l;
	list = list2;
    }

    /**
     * Get the number of listeners
     * @return the number of listeners
     */
    public int getListenerCount() {
	return list.length/2;
    }

    
    /**
     * Get the number of listeners for a particular key
     * Each listener will have the key t as the listener's class or a
     * superclass.
     * The full list is comprised of a collection of sublists, each
     * distinguished by a class t that serves as a key naming a sublist.
     * @parem t the class or a superclass serving as a key for listeners
     * @return the number of listeners
     */
    public int getListenerCount(Class<?> t) {
	Object[] ourlist = list;
	int count = 0;
	for (int  i = 0; i < ourlist.length; i+=2) {
	    if (ourlist[i].equals(t)) {
		count++;
	    }
	}
	return count;
    }

    /**
     * Returns the internal list of alternating listener classes and
     * listeners.  The array returned is the internal array used by
     * this class.
     *<P>
     * Please see {@link javax.swing.event.EventListenerList} for additional
     * documentation.
     * @return an array whose even elements (starting from 0) contain
     *         the class for a listener and whose odd values contain the
     *         corresponding listener
     */
    public Object[] getListenerList() {
	return list;
    }

    /**
     * Get an array of listeners of a particular type.
     * The full list is comprised of a collection of sublists, each
     * distinguished by a class t that serves as a key naming a sublist.
     * @param t the class (or superclass) of a listener that is used
     *        as a key
     * @return a list of the listeners matching the key
     */
    @SuppressWarnings("unchecked")
    public <T extends EventListener> T[] getListeners(Class<T> t) {
	Object[] ourlist = list;
	int length = ourlist.length;

	int count = 0;
	for (int  i = 0; i < ourlist.length; i+=2) {
	    if (ourlist[i].equals(t)) {
		count++;
	    }
	}
	T[] array = (T[])Array.newInstance(t, count);
	int index = 0;
	for (int i = 0; i < ourlist.length; i += 2) {
	    if (ourlist[i].equals(t)) {
		array[index++] = (T) ourlist[i+1];
	    }
	}
	return array;
    }

    /**
     * Remove a listener from a listener list.
     * If the same listener was added twice, the most recently added
     * listener is the one that is removed.  The listener l's class or
     * one of its superclasses must match the class t.  The full list
     * is comprised of a collection of sublists, each distinguished by
     * a class t that serves as a key naming a sublist.
     * @param t the class for the listener
     * @param l the listener, whose type or supertype must match the one
     *        provided by the argument t
     */
    public synchronized <T extends EventListener> void remove(Class<T> t, T l) {
	Object[] ourlist = list;
	int ind = -1;
	for (int i = list.length-2; i >= 0; i -= 2) {
	    if (ourlist[i].equals(t) && ourlist[i+1].equals(l)) {
		ind = i;
		break;
	    }
	}
	if (ind != -1) {
	    if (ourlist.length == 2) {
		list = EMPTYLIST;
	    } else {
		Object[] newlist = new Object[ourlist.length-2];
		System.arraycopy(ourlist, 0, newlist, 0, ind);
		System.arraycopy(ourlist, ind+2, newlist, ind,
				 newlist.length - ind);
		list = newlist;
	    }
	}
    }
    
    /**
     * Get a string representation of this object.
     * @return the string
     */
    public String toString() {
	Object[] ourlist = list;
	StringBuilder sb = new StringBuilder();
	sb.append("[");
	boolean first = true;
	boolean isClass = true;
	for (Object o: ourlist) {
	    if (first) {
		first = false;
	    } else {
		sb.append(",");
	    }
	    if (isClass) {
		sb.append(((Class<?>)o).getName());
	    } else {
		sb.append(o.toString());
	    }
	    isClass = !isClass;
	}
	sb.append("]");
	return sb.toString();
    }

}

//  LocalWords:  l's superclasses sublists sublist supertype
//  LocalWords:  superclass
