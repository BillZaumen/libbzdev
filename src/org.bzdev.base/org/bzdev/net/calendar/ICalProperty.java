package org.bzdev.net.calendar;
import java.util.*;

//@exbundle org.bzdev.net.calendar.lpack.Calendar

/**
 * The iCalendar-property class.
 * This interface is used for parsing iCalendar objects.
 * <P>
 * An iCalendar property is characterized by a name, an
 * optional set of parameters, and one or more values.
 */
public class ICalProperty {
    String name;
    ArrayList<ICalParameter> parameters = null;
    String value = null;
    ArrayList<String> values = null;
    ArrayList<Character> delims = null;

    /**
     * Constructor.
     */
    protected ICalProperty() {}

    /**
     * Get the name of this property
     * @return the property name
     */
    public String getName() {
	return name;
    }

    private static List<ICalParameter> EMPTY_ICAL_LIST
	= Collections.unmodifiableList(new LinkedList<ICalParameter>());

    private static List<String> EMPTY_STRING_LIST
	= Collections.unmodifiableList(new LinkedList<String>());

    private static List<Character> EMPTY_CHAR_LIST
	= Collections.unmodifiableList(new LinkedList<Character>());

    /**
     * Get the parameters defined for this property
     * @return the parameters
     */
    public List<ICalParameter> getParameters() {
	if (parameters == null) {
	    return EMPTY_ICAL_LIST;
	} else {
	    return Collections.unmodifiableList(parameters);
	}
    }

    /**
     * Determine if this property has multiple values
     * @return true if this property has multiple values;
     *         false otherwise
     */
    public boolean isMultiValued() {
	return values != null;
    }

    /**
     * Get the value for this property
     * @return the value
     */
    public String getValue() {
	if (values == null) {
	    return value;
	} else if (values.size() > 0) {
	    return values.get(0);
	} else {
	    return null;
	}
    }

    /**
     * Get the values for this property
     * @return the values
     */
    public List<String> getValues() {
	if (values == null) {
	    if (value != null) {
		return Collections.singletonList(value);
	    } else {
		return EMPTY_STRING_LIST;
	    }
	} else {
	    return Collections.unmodifiableList(values);
	}
    }

    public List<Character> getDelims() {
	if (values == null) return EMPTY_CHAR_LIST;
	return Collections.unmodifiableList(delims);
    }
}

//  LocalWords:  exbundle iCalendar
