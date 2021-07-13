package org.bzdev.net.calendar;
import java.util.*;

//@exbundle org.bzdev.net.calendar.lpack.Calendar

/**
 * The iCalendar parameter class. 
 * This interface is used for parsing iCalendar objects.
 * <P>
 * An iCalendar parameter annotate an iCalendar property. Each
 * parameter has a name, and a value.  A boolean method determines
 * if the value had been quoted (such quotes are not provided in
 * the value itself).
 */
public class ICalParameter {
    String name;
    boolean quoted = false;
    String value;

    /**
     * Constructor.
     */
    protected ICalParameter() {}

    /**
     * Get the name of a parameter.
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * Determine if a value was quoted.
     * @return true if the value was quoted; false otherwise
     */
    public boolean wasQuoted() {
	return quoted;
    }

    /**
     * Get the value of a parameter.
     * @return the value (with any quotes removed)
     */
    public String getValue() {
	return value;
    }
}

//  LocalWords:  exbundle iCalendar boolean
