package org.bzdev.net.calendar;

import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.ChronoUnit;
import java.time.format.*;
import java.util.*;

import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;
import org.bzdev.util.TemplateProcessor.KeyMapList;

//@exbundle org.bzdev.net.calendar.lpack.Calendar

/**
 * Builder for an iCalendar file.  The iCalendar format is described
 * in <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>.
 * Users of this class should be familiar with this RFC. This class
 * uses several additional classes as shown in the following diagram:
 * <P style="text-align: center">
 * <img src="doc-files/ICalBuilder.png" class="imgBackground" alt="Class Diagram">
 * <P>
 * Users of this class will first create an instance of it and also
 * instances of inner classes representing iCalendar components. Then
 * these components will be added to the iCalendar instance by
 * calling {@link ICalBuilder#add(ICalBuilder.Component)} or
 * {@link ICalBuilder#add(ICalBuilder.Common)}.
 * <P>
 * The most general way of creating components is to create an instance of
 * {@link ICalBuilder.Component} by providing the name of the component
 * in the constructor.  The methods
 * {@link ICalBuilder.Base#addProperty(String,String,boolean, String...)}
 * and/or
 * {@link ICalBuilder.Base#addProperty(String,String[],boolean, String...)}
 * can be used to add explicitly properties to the component. A more
 * convenient alternative is to use several inner classes:
 * <UL>
 *  <LI>{@link ICalBuilder.Event}. This class represents an iCalendar event
 *      component.
 *  <LI>{@link ICalBuilder.ToDo}. This class represents an iCalendar to-do
 *      component.
 *  <LI>{@link ICalBuilder.Journal}. This class represents an iCalendar
 *      journal component.
 *  <LI>{@link ICalBuilder.FreeBusy}. This class represents an iCalendar
 *      free-busy component.
 *  <LI>{@link ICalBuilder.TimeZone}. This class represents a time-zone
 *      component.
 *  <LI>{@link ICalBuilder.Alarm}. This class represents an iCalendar alarm
 *      component. Alarms are allowed within iCalendar event and to-do
 *      components, and an alarm's constructor determines the component
 *      containing that alarm.
 * </UL>
 * Each of these inner classes contains methods that will set their
 * properties, covering the most common cases. Additional properties can
 * be added using
 * {@link ICalBuilder.Base#addProperty(String,String,boolean, String...)}
 * and/or
 * {@link ICalBuilder.Base#addProperty(String,String[],boolean, String...)}
 * in which case the user must ensure that a property is allowed by
 * RFC 5545. These methods can also be used for cases where the methods
 * of an inner class do not provide parameters (e.g., parameters not
 * explicitly defined in RFC 5545).
 * <P>
 * The classes {@link ICalBuilder.FreeBusy} and {@link ICalBuilder.TimeZone}
 * can also be used, but these are trivial implementations of the
 * {@link ICalBuilder.Component} class, providing little more than
 * the component name  used in the "BEGIN" and "END" lines.
 * <P>
 * Note: while RFC 5545 allows a mix of component types in an
 * iCalendar object,
 * <A HREF="https://tools.ietf.org/html/rfc6047">RFC 6047</A>,
 * which defines the iMIP (iCalendar Message-Based Interoperability Protocol)
 * requires that all component in the same object have the
 * same component type.  Since different transport protocols can differ
 * in what constraints are used, the  user of this class must ensure that
 * an object created is appropriate for a given transport protocol.
 * <P>
 * This class and most of its inner classes have methods for which one or
 * more arguemnts are declared with the type
 * {@link java.time.temporal.TemporalAccessor}. The
 * argument must be one of the following classes (which implement
 * {@link java.time.temporal.TemporalAccessor}):
 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
 * {@link java.time.ZonedDateTime}, {@link java.time.Instant},
 * The classes{@link java.time.LocalDate} and {@link java.time.LocalDateTime}
 * will be treated as a "floating time" (see RFC 5545) and both
 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
 * have their times represented using UTC.
 */
public class ICalBuilder {

    /**
     * Constructor.
     */
    public ICalBuilder() {
    }

    static String errorMsg(String key, Object... args) {
	return CalendarErrorMsg.errorMsg(key, args);
    }

    /**
     * The media type for an iCalendar object
     */
    public static final String MEDIA_TYPE = "text/calendar";

    /**
     * Methods defined by iTIP (iCalendar Transport-Independent
     * Interoperability Protocol). Please see
     * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
     * Section 1.4 of RFC 5546</A>.
     * @see setMethod(ITIPMethod)
     */
    public static enum ITIPMethod {
	/**
	 * Used to publish an iCalendar object to one or more
	 * "Calendar Users".  There is no interactivity between the
	 * publisher and any other "Calendar User".  An example might
	 * include a baseball team publishing its schedule to the
	 * public.
	 * <P>
	 * Note: The description of each constant  was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	PUBLISH,
	/**
	 * Used to schedule an iCalendar object with other
	 * "Calendar Users".  Requests are interactive in
	 * that they require the receiver to respond using
	 * the reply methods.  Meeting requests, busy-time
	 * requests, and the assignment of tasks to other
	 * "Calendar Users" are all examples.  Requests are
	 * also used by the Organizer to update the status
	 * of an iCalendar object.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	REQUEST,
	/**
	 * A reply is used in response to a request to
	 * convey Attendee status to the Organizer.
	 * Replies are commonly used to respond to meeting
	 * and task requests.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	REPLY,
	/**
	 * Add one or more new instances to an existing
	 * recurring iCalendar object.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	ADD,
	/**
	 * Cancel one or more instances of an existing
	 * iCalendar object.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	CANCEL,
	/**
	 * Used by an Attendee to request the latest
	 * version of an iCalendar object.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	REFRESH,
	/**
	 * Used by an Attendee to negotiate a change in an
	 * iCalendar object.  Examples include the request
	 * to change a proposed event time or change the
	 * due date for a task.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	COUNTER,
	/**
	 * Used by the Organizer to decline the proposed
	 * counter proposal.
	 * <P>
	 * Note: The description was copied from
	 * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
	 * Section 1.4 of RFC 5546</A>.
	 */
	DECLINECOUNTER
    }

    String method = null;
    /**
     * Set the method for an iCalendar object.
     * The method field is defined in
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.7.2">
     * Section 3.7.2 of RFC 5545</A>. 
     * <P>
     * This Java method is provided for cases where the desired method
     * is not one defined by
     * <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
     * RFC 5546</A>. Otherwise one should use {@link #setMethod(ITIPMethod)},
     * which is type safe&mdash;a mistyped method name will result in
     * a compile-time error.
     * @param method the method to use; null if no method is to be
     *        provided
     */
    public void setMethod(String method) {
	this.method = method;
    }

    /**
     * Set the method for an iCalendar object using RFC 5456 methods.
     * The method field is defined in
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.7.2">
     * Section 3.7.2 of RFC 5545</A>.
     * The enumeration {@link ITIPMethod} provides the methods defined
     * in <A HREF="https://tools.ietf.org/html/rfc5546#section-1.4">
     * Section 1.4 of RFC 5546</A>.
     * @param method the method; null if no method is to be provided
     */
    public void setMethod(ITIPMethod method) {
	if (method == null) {
	    this.method = null;
	} else {
	    switch(method) {
	    case PUBLISH:
		setMethod("PUBLISH");
		break;
	    case REQUEST:
		setMethod("REQUEST");
		break;
	    case REPLY:
		setMethod("REPLY");
		break;
	    case ADD:
		setMethod("ADD");
		break;
	    case CANCEL:
		setMethod("CANCEL");
		break;
	    case REFRESH:
		setMethod("REFRESH");
		break;
	    case COUNTER:
		setMethod("COUNTER");
		break;
	    case DECLINECOUNTER:
		setMethod("DECLINECOUNTER");
		break;
	    }
	}
    }

    /**
     * Base class for iCalendar components.
     */
    public static abstract class Base {

	int duration;
	Units units;
	LinkedList<String> attachList = new LinkedList<>();
	LinkedList<String> lines = new LinkedList<>();


	/**
	 * Lines of text created for the iCalendar format, but without
	 * the EOL sequence and/or line folding. Subclasses will use
	 * this field to record the lines they create.
	 */
	protected LinkedList<String> contentLines = new LinkedList<>();

	/**
	 * Constructor.
	 */
	protected Base() {}

	/**
	 * Set the duration for an iCalendar component.
	 * Durations are define in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.5">
	 * Section 3.8.2.5 of RFC 5545</A>
	 * Some iCalendar components do not have a 'duration' property,
	 * and the subclasses for those components will throw an exception
	 * if this method is called (this behavior should be documented
	 * by all subclasses that do not support this method).
	 * @param duration the duration, either a positive integer
	 *        or 0 if the duration should be ignored
	 * @param units the units for the duration
	 *        ({@link Units#SECONDS}, {@link Units#MINUTES},
	 *        {@link Units#HOURS}, {@link Units#DAYS}, or
	 *        {@link Units#WEEKS}); null only if the duration is 0
	 * @exception UnsupportedOperationException if an iCalendar
	 *            component does not have a duration.
	 * @exception IllegalArgumentException an argument had an illegal value
	 */
	public void setDuration(int duration, Units units)
	    throws UnsupportedOperationException
	{
	    if (duration != 0 && units == null) {
		throw new IllegalArgumentException(errorMsg("nullUnits"));
	    }
	    if (duration < 0) {
		throw new IllegalArgumentException
		    (errorMsg("negativeDuration", duration));
	    }
	    this.duration = duration;
	    this.units = units;
	}


	/**
	 * Add an attachment referenced by a URI.
	 * Attachments are defined in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.1">
	 * Section 3.8.1.1 of RFC 5545</A>.
	 * The media type is defined in 
	 * <A HREF="https://tools.ietf.org/html/rfc4288#section-4.2">Section 4.2 of RFC 4288</A>.
	 * Some iCalendar components do not have an 'attachment' property,
	 * and the subclasses for those components will throw an exception
	 * if this method is called (this behavior should be documented
	 * by all subclasses that do not support this method).
	 * @param formatType the media type of an attachment
	 * @param uri the URI referencing the attachment
	 * @exception UnsupportedOperationException a subclass does not
	 *            allow attachments
	 */
	public void addAttachment(String formatType, String uri)
	    throws UnsupportedOperationException
	{
	    if (formatType != null) {
		attachList.add("ATTACH;FMTTYPE="
				+ "\"" + formatType + "\":"
				+ uri);
	    } else {
		attachList.add("ATTACH:" + uri);
	    }
	}

	/**
	 * Add an attachment provided as binary data.
	 * The media type is defined in 
	 * <A HREF="https://tools.ietf.org/html/rfc4288#section-4.2">Section 4.2 of RFC 4288</A>.
	 * Attachments are defined in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.1">
	 * Section 3.8.1.1 of RFC 5545</A>.
	 * Some iCalendar components do not have an 'attachment' property,
	 * and the subclasses for those components will throw an exception
	 * if this method is called (this behavior should be documented
	 * by all subclasses that do not support this method).
	 * @param formatType the media type of an attachment
	 * @param data an array of bytes containing the attachment
	 * @exception UnsupportedOperationException a subclass does not
	 *            allow attachments
	 */
	public void addAttachment(String formatType, byte[] data)
	    throws UnsupportedOperationException
	{
	    StringBuilder sb = new StringBuilder();
	    sb.append("ATTACH;ENCODING=BASE64;VALUE=BINARY");

	    if (formatType != null) {
		sb.append(";FMTTYPE=\"");
		sb.append(formatType);
		sb.append("\"");
	    }
	    sb.append(":");
	    Base64.Encoder encoder = Base64.getEncoder();
	    sb.append(encoder.encodeToString(data));
	    attachList.add(sb.toString());
	}
    
	private static String[] emptyParam = new String[0];

	/**
	 * Add a property with no parameters to this iCalendar component.
	 * RFC 5544 provides a sizable number of properties, and allows
	 * experimental ones to be added. This method allows a property
	 * to be explicitly added.
	 * <P>
	 * If the values are URIs, the third argument (escapeValues)
	 * should be set to false; for text, it should be set to true.
	 * The property name must be one allowed by RFC 5545 for the
	 * current iCalendar component. None of the arguments should
	 * be folded.
	 * <P>
	 * As a general rule, the caller of this method should set the
	 * 'escapeValues' argument so that the user can provide values
	 * that have not been escaped: typically only text values need
	 * to be escaped.
	 * @param property the name of the property
	 * @param escapeValues true if the the values should be escaped
	 *        by this method; false otherwise
	 * @param values one or more values for the property
	 */
	public void addProperty(String property, boolean escapeValues,
				String... values)
	{
	    lines.add(ICalBuilder.icalProp(property, emptyParam,
					   escapeValues, values));
	}


	/**
	 * Add a property with a single parameter to this iCalendar component.
	 * RFC 5544 provides a sizable number of properties, and allows
	 * experimental ones to be added. This method allows any property
	 * to be explicitly added.
	 * <P>
	 * If the values are URIs, the third argument (escapeValues)
	 * should be set to false; for text, it should be set to true.
	 * The property name must be one allowed by RFC 5545 for the
	 * current iCalendar component. A parameter should not be
	 * escaped, and none of the arguments should be folded.
	 * <P>
	 * As a general rule, the caller of this method should set the
	 * 'escapeValues' argument so that the user can provide values
	 * that have not been escaped: typically only text values need
	 * to be escaped.
	 * @param property the name of the property
	 * @param param the property's parameter; an empty string if there
	 *        are no parameters
	 * @param escapeValues true if the the values should be escaped
	 *        by this method; false otherwise
	 * @param values one or more values for the property
	 */
	public void addProperty(String property, String param,
				boolean escapeValues, String... values)
	{
	    lines.add(ICalBuilder.icalProp(property, param,
					   escapeValues, values));
	}

	/**
	 * Add a property with multiple parameters to this iCalendar component.
	 * RFC 5544 provides a sizable number of properties, and allows
	 * experimental ones to be added. This method allows any property
	 * to be explicitly added.
	 * <P>
	 * If the values are URIs, the third argument (escapeValues)
	 * should be set to false; for text, it should be set to true.
	 * The property name must be one allowed by RFC 5545 for the
	 * current iCalendar component. A parameter should not be
	 * escaped, and none of the arguments should be folded.
	 * <P>
	 * As a general rule, the caller of this method should set the
	 * 'escapeValues' argument so that the user can provide values
	 * that have not been escaped: typically only text values need
	 * to be escaped.
	 * @param property the name of the property
	 * @param params the property's parameters; a zero-length array
	 *        indicates that there are no parameters.
	 * @param escapeValues true if the the values should be escaped
	 *        by this method; false otherwise
	 * @param values one or more values for the property
	 */
	public void addProperty(String property, String[] params,
				boolean escapeValues, String... values)
	{
	    lines.add(ICalBuilder.icalProp(property, params,
					   escapeValues, values));
	}

	/**
	 * Generate content lines.
	 * Subclasses that override this method should call
	 * <CODE>super.createContentLines()</CODE>.
	 */
	protected void createContentLines() {
	    if (duration != 0) {
		String u = null;
		switch (units) {
		case WEEKS:
		    u = "W";
		    break;
		case DAYS:
		    u = "D";
		    break;
		case HOURS:
		    u = "H";
		    break;
		case MINUTES:
		    u = "M";
		    break;
		case SECONDS:
		    u = "S";
		    break;
		}
		String string = null;
		if (duration < 0) {
		    string = "-PT" + Math.abs(duration) + u;
		} else {
		    string = "PT" + duration + u;
		}
		contentLines.add("DURATION:" + string);
	    }
	    for (String line: lines) {
		contentLines.add(line);
	    }
	    for (String line: attachList) {
		contentLines.add(line);
	    }
	}

	/**
	 * Clear the content lines.
	 * Subclasses that override this method must
	 * call super.clearContentLines().
	 */
	protected void clearContentLines() {
	    contentLines.clear();
	}
    }


    /**
     * Arbitrary top-level iCalendar component.
     * The ICalBuilder class contains inner classes that can
     * create event and to-do iCalendar objects.  Other iCalendar
     * objects can be created using this class.
     * 
     */
    public static class Component extends Base {
	String type;
	String[] initialContents = null;
	
	/**
	 * Constructor.
	 * Please see 
	 * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>
	 * for a description of component names. The standard ones are
	 * VEVENT, VTODO, VJOURNAL, VFREEBUSY, and VTIMEZONE. Experimental
	 * component names are of the form X-&lt;VENDOR-ID &lt;NAME&gt;
	 * where NAME is a string containing letters (alpha characters),
	 * digits, and "-".
	 * The component's properties will be delimited by the lines
	 * <BLOCKQUOTE><PRE><CODE>
	 *    BEGIN:&lt;X-NAME&gt;
	 * </CODE></PRE></BLOCKQUOTE>
	 * and
	 * <BLOCKQUOTE><PRE><CODE>
	 *    END:&lt;X-NAME&gt;
	 * </CODE></PRE></BLOCKQUOTE>
	 * Each property should be added using the methods
	 * {@link ICalBuilder.Base#addProperty(String,String,boolean,String...)}
	 * or
	 * {@link ICalBuilder.Base#addProperty(String,String[],boolean,String...)}.
	 * @param componentName the name of a component (e.g., its type)
	 */
	public Component(String componentName) {
	    this.type = componentName;
	}

	/**
	 * Constructor with contents.
	 * Please see 
	 * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>
	 * for a description of component names. The standard ones are
	 * VEVENT, VTODO, VJOURNAL, VFREEBUSY, and VTIMEZONE. Experimental
	 * component names are of the form X-&lt;VENDOR-ID &lt;NAME&gt;
	 * where NAME is a string containing letters (alpha characters),
	 * digits, and "-".
	 * The component's properties will be delimited by the lines
	 * <BLOCKQUOTE><PRE><CODE>
	 *    BEGIN:&lt;X-NAME&gt;
	 * </CODE></PRE></BLOCKQUOTE>
	 * and
	 * <BLOCKQUOTE><PRE><CODE>
	 *    END:&lt;X-NAME&gt;
	 * </CODE></PRE></BLOCKQUOTE>
	 * <P>
	 * The initial properties are provided by the second argument.
	 * Each element of this array represents a line that starts
	 * with a property name.  The content lines are presumed to
	 * have special characters escaped when necessary.  The
	 * content lines must not be 'folded'.
	 * @param componentName the name of a component
	 * @param contents the (initial) contents for this component.
	 */
	public Component(String componentName, String[] contents) {
	    this(componentName);
	    initialContents = contents.clone();
	}

	@Override
	protected void createContentLines() {
	    contentLines.add("BEGIN:" + type);
	    if (initialContents != null) {
		for (String line: initialContents) {
		    contentLines.add(line);
		}
	    }
	    super.createContentLines();
	    contentLines.add("END:" + type);
	}	
    }


    /**
     * The iCalendar free/busy component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.5">
     * Section 3.6.4 of RFC 545</A>
     * for a description of what properties are allowed in this component
     * and the order in which they can occur. The properties must be
     * added by providing them to a constructor and/or by using the
     * methods {@link Base#addProperty(String,String,boolean,String...)} and
     * {@link Base#addProperty(String,String[],boolean,String...)}.
     */
    public static class FreeBusy extends Component {
	
	/**
	 * Constructor.
	 */
	public FreeBusy() {
	    super("VFREEBUSY");
	}

	/**
	 * Constructor with contents.
	 * Each element of the content array represents a line that
	 * starts with a property name.  The lines are presumed to
	 * have special characters escaped when necessary.  The
	 * content lines must not be 'folded'.
	 * @param contents the sequence of properties for this component
	 */
	public FreeBusy(String[] contents) {
	    super("VFREEBUSY", contents);
	}

	/**
	 * Set the duration for an iCalendar component.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param duration (ignored)
	 * @param units (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void setDuration(int duration, Units units)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}

	/**
	 * Add an attachment referenced by a URI.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param formatType (ignored)
	 * @param uri (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void addAttachment(String formatType, String uri)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}

	/**
	 * Try to add an attachment provided as binary data.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param formatType (ignored)
	 * @param data (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void addAttachment(String formatType, byte[] data)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}
    }


    /**
     * The iCalendar timezone component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.5">
     * Section 3.6.5 of RFC 5545</A>
     * for a description of what properties are allowed in this component
     * and the order in which they can occur. The properties must be
     * added by providing them to a constructor and/or by using the
     * methods {@link Base#addProperty(String,String,boolean,String...)} and
     * {@link Base#addProperty(String,String[],boolean,String...)}.
     */
    public static class TimeZone extends Component {
	
	/**
	 * Constructor.
	 */
	public TimeZone() {
	    super("VTIMEZONE");
	}

	/**
	 * Constructor with contents.
	 * Each element of the content array represents a line that
	 * starts with a property name.  The lines are presumed to
	 * have special characters escaped when necessary.  The
	 * content lines must not be not 'folded'.
	 * @param contents the sequence of properties for this component
	 */
	public TimeZone(String[] contents) {
	    super("VTIMEZONE", contents);
	}

	/**
	 * Set the duration for an iCalendar component.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param duration (ignored)
	 * @param units (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void setDuration(int duration, Units units)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}

	/**
	 * Add an attachment referenced by a URI.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param formatType (ignored)
	 * @param uri (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void addAttachment(String formatType, String uri)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}

	/**
	 * Try to add an attachment provided as binary data.
	 * This operation is not supported and calling this
	 * method will throw an exception.
	 * @param formatType (ignored)
	 * @param data (ignored)
	 * @exception UnsupportedOperationException this class does not
	 *            allow attachments
	 */
	public final void addAttachment(String formatType, byte[] data)
	    throws UnsupportedOperationException
	{
	    throw new UnsupportedOperationException
		(errorMsg("unsupportedMethod"));
	}
    }

    /**
     * Superclass for those ICalendar components have a unique ID.
     */
    public static abstract class Common extends Base {
	String uid = null;
	int sequence = -1;
	TemporalAccessor created;
	TemporalAccessor lastModified;
	TemporalAccessor startTime = null;
	String[] startTimeParms = null;
	String summary = null;
	String[] summaryParams = null;
	String description = null;
	String[] descriptionParams = null;
	String location = null;
	String[] locationParams = null;
	String clasz = null;
	String[] categories = null;
	Status status = null;
	int priority = 0;
	String url;
	String geo = null;
	String resources = null;
	LinkedList<String> relatedToList = new LinkedList<>();
	
	ArrayList<Alarm> alarms = new ArrayList<>();

	/**
	 * Constructor.
	 * The first argument (uid) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.7">
	 * Section 3.8.4.7 of RFC 5545</A>.
	 * The second argument (sequence) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.4">
	 * Section 3.8.7.4 of RFC 5545</A>.
	 * The third argument (created) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.1">
	 * Section 3.8.7.1 of RFC 5545</A>.
	 * The fourth argument (lastModified) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.3">
	 * Section of 3.8.7.3 RFC 5545</A>.
	 * <P>
	 * The types of the {@link java.time.temporal.TemporalAccessor}
	 * arguments must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param uid the globally unique identifier for this iCalendar
	 *        component
	 * @param sequence a sequence number labeling changes to this
	 *        iCalendar component
	 * @param created the time at which this iCalendar component was created
	 * @param lastModified the last time at which this iCalendar component
	 *         was modified
	 * @exception IllegalArgumentException an argument was null or
	 *            the sequence number was negative, the uid argument
	 *            was null, or an unrecoginzed time format was used
	 *            (wrong subclass of TemporalAccessor)
	 */
	protected Common(String uid, int sequence,
			 TemporalAccessor created,
			 TemporalAccessor lastModified)
	    throws IllegalArgumentException
	{
	    if (uid == null) {
		throw new IllegalArgumentException(errorMsg("nullUID"));
	    }
	    if (sequence < 0) {
		throw new IllegalArgumentException
		    (errorMsg("negativeSequence"));
	    }
	    checkTemporalAccessor(created);
	    checkTemporalAccessor(lastModified);
	    this.uid = uid;
	    this.sequence = sequence;
	    this.created = created;
	    this.lastModified = lastModified;
	}

	/**
	 * Set the start time for this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.4">
	 * Section 3.8.2.4 of RFC 5545</A>
	 * for a description of the start-time property.
	 * Parameters should not be used unless the run-time type of
	 * the first argument is {@link java.time.LocalDate} or
	 * {@link java.time.LocalDateTime}. The parameters defined
	 * by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param startTime the time at which the activity represented
	 *        by this  iCalendar component starts
	 * @param parameters the parameters for the start time, if any
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	protected void setStartTime(TemporalAccessor startTime,
				    String... parameters)
	    throws IllegalArgumentException
	{
	    checkTemporalAccessor(startTime);
	    this.startTime = startTime;
	    startTimeParms = parameters.clone();
	}

	/**
	 * Set the summary property for this event.
	 * The first argument is typically a short string describing an
	 * event and is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.12">
	 * Section 3.8.1.12 of RFC 5545</A>.  
	 * Parameters defined by RFC 5545 for a summary are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param summary a summary describing this event; null if there is
	 *        no summary
	 * @param parameters the parameters for this property
	 */
	protected void setSummary(String summary, String... parameters)
	{
	    this.summary = summary;
	    summaryParams = parameters.clone();
	}

	/**
	 * Set the description for this event.
	 * The description of an iCalendar component and is
	 * described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.5">
	 * Section 3.8.1.5 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a description are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param description a full description of this event; null
	 *        if there is no description
	 * @param parameters the parameters for this property
	 */
	protected void setDescription(String description, String... parameters)
	{
	    this.description = description;
	    descriptionParams = parameters.clone();
	}

	/**
	 * Set the location for this iCalendar component.
	 * The location property describes the location of an event and
	 * is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.7">
	 * Section 3.8.1.7 of RFC 5545</A>.
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param location the location for this event; null if no location is
	 *        specified
	 * @param parameters the parameters for this property
	 */
	protected void setLocation(String location, String... parameters)
	{
	    this.location = location;
	    locationParams = parameters.clone();
	}
	
	/**
	 * Add a comment to attach to this iCalendar component
	 * Comments are described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.4">
	 * Section 3.8.1.4 of RFC 5545</A>.
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param comment the comment.
	 * @param parameters the parameters for this property
	 */
	public void addComment(String comment, String... parameters) {
	    addProperty("COMMENT", parameters, true, comment);
	}

	/**
	 * Set the classification of this iCalendar component.
	 * Allowed values are PUBLIC, PRIVATE, CONFIDENTIAL,
	 * an IANA token (any iCalendar identifier registered
	 * with IANA), or a token of the form 
	 * X-&lt;vendorid&gt;-&lt;word&gt; where  -&lt;word&gt;
	 * is a non-empty sequence of letters (alpha characters), digits,
	 * and "-". The classification property is describe in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.3">
	 * Section 8.3.1.3 of RFC 5545</A>.
	 * 
	 * <P>
	 * As of December 23, 2019, the only classifications registered
	 * with IANA are PUBLIC, PRIVATE, and CONFIDENTIAL.
	 * @param classification the classification.
	 */
	public void setClassification(String classification) {
	    this.clasz = classification;
	}

	/**
	 * Add the categories for an iCalendar component.
	 * Each category is a user-specified string that can be
	 * used for search purposes. Each category will typically
	 * be a keyword useful for searching for particular types
	 * of calendar components. Categories are described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.2">
	 * Section 3.8.1.2 of RFC 5545</A>. Language values are described
	 * in <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * Section 3.2.10 of RFC 5545</A> and
	 * <A HREF="https://tools.ietf.org/html/rfc5646"> RFC 5646</A>.
	 * @param lang the language for this component
	 * @param categories the categories
	 */
	public void addCategories(String lang, String... categories) {
	    String param = (lang == null)? null: "LANGUAGE="+lang;
	    addProperty("CATEGORIES", param, true, categories);
	}

	/**
	 * Set the status of this iCalendar component.
	 * The status property is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.11">
	 * Section 3.8.1.11 of RFC 5545</A>.
	 * @param status the status; null to clear the status
	 * @exception IllegalArgumentException the status is not allowed for
	 *            this iCalendar component
	 */
	protected void setStatus(Status status)
	    throws IllegalArgumentException
	{
	    this.status = status;
	}

	/**
	 * Set the priority of this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.9">
	 * Section 3.8.1.9 of RFC 5545</A> for a description of the
	 * priority property.
	 * @param priority the priority, a value in the
	 *        range [0, 9]
	 */
	protected void setPriority(int priority) {
	    if (priority < 0 || priority > 9) {
		throw new IllegalArgumentException
		    (errorMsg("illegalPriority", priority));
	    }
	    this.priority = priority;
	}

	/**
	 * Set the URL property for this iCalendar component.
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.6">
	 * Section 3.8.4.6 of RFC 5545</A>.
	 * @param uri the URL's URI; null if there is none
	 */
	public void setURL(String uri) {
	    url = uri;
	}

	/**
	 * Add a property indicating that another iCalendar component is
	 * related to this one.
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.5">
	 * Section 3.8.4.5 of RFC 5545</A>.
	 * @param reltype the type of relation (PARENT, CHILD,
	 *        SIBLING, or a token starting with "X-"); null if there
	 *        should be no RELTYPE parameter
	 * @param id  the unique identifier of the other component
	 */
	public void addRelatedTo(String reltype, String id)
	    throws IllegalArgumentException
	{
	    if (reltype == null) {
		addProperty("RELATED-TO", true, id);
	    }
	    if(!(reltype.equals("PARENT") || reltype.equals("CHILD")
		 || reltype.equals("SIBLING") || reltype.startsWith("X-"))) {
		throw new IllegalArgumentException
		    (errorMsg("reltype", reltype));
	    }
	    addProperty("RELATED-TO", "RELTYPE="+reltype, true, id);
	}

	/**
	 * Specify a latitude and longitude for an iCalendar component.
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.6">
	 * Section 3.8.1.6 of RFC 5545</A>.
	 * @param latitude the latitude in degrees and between
	 *        -90.0 and 9.0 inclusive
	 * @param longitude the longitude in degrees and between
	 *        -180.0 inclusive and 180.0 exclusive
	 */
	public void setGeo(double latitude, double longitude) {
	    if (latitude < -90.0 || latitude > 90.0) {
		throw new IllegalArgumentException
		    (errorMsg("latitude", latitude));
	    }
	    if (longitude < -180.0 || longitude > 180.0) {
		throw new IllegalArgumentException
		    (errorMsg("longitude", longitude));
	    }
	    geo = String.format("%f;%f", latitude, longitude);
	}

	/**
	 * Add a contact.
	 * A contact is a textual field as describe in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.2">
	 * Section 3.8.4.2 of RFC 5545</A>.
	 * <P>
	 * Parameters defined by RFC 5545 for a contact are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param contact the contact
	 * @param parameters  the parameters as defined in RFC 5545
	 * @exception IllegalArgumentException the argument was null
	 */

	public void addContact(String contact, String... parameters) {
	    if (contact == null) {
		throw new IllegalArgumentException("nullContact");
	    }
	    addProperty("CONTACT", parameters, true, contact);
	}
	
	/**
	 * Add an attendee.
	 * The first argument is a URI denoting the attendee (for example,
	 * a "mailto" URI).
	 * The attendee property is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.1">
	 * Section 3.8.4.1 of RFC 5545</A>. This section also
	 * describes the parameters for this property. Two
	 * particularly useful parameters are the "CN" property, which
	 * provides a common name for the attendee, and the "CUTYPE"
	 * parameter, whose values can be INDIVIDUAL, GROUP, RESOURCE,
	 * ROOM, UNKNOWN, or an experimental type starting with "X-"
	 * or an IANA-registered type not listed above.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.2">
	 * Section 3.2.2 of RFC 5545</A> and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.3">
	 * Section 3.2.3 of RFC 5545</A> respectively for these two
	 * parameters.  The language parameter
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A> may be useful as well.
	 * @param uri a URI denoting an attendee
	 * @param params additional parameters as defined in RFC 5545
	 */
	public void addAttendee(String uri, String... params) {
	    if (uri == null) {
		throw new IllegalArgumentException(errorMsg("nullURI"));
	    }
	    addProperty("ATTENDEE", params, false, uri);
	}

	/**
	 * Create the content for this event (a sequence of lines)
	 * that will make up part of an iCalendar file).
	 */
	protected void createContentLines() throws IllegalStateException {
	    contentLines.add("UID:" + ICalBuilder.escape(uid));
	    contentLines.add("DTSTAMP:"
			     + getTime(Instant.now()
				       .truncatedTo(ChronoUnit.SECONDS)));
	    if (sequence >= 0) {
		contentLines.add("SEQUENCE:" + sequence);
	    }
	    if (created != null) {
		contentLines.add("CREATED:" + ICalBuilder.getTime(created));
	    }
	    if (lastModified != null) {
		contentLines.add("LAST-MODIFIED:"
				 + ICalBuilder.getTime(lastModified));
	    }
	    if (startTime != null) {
		contentLines.add(ICalBuilder.icalProp
				 ("DTSTART", startTimeParms,
				  false, ICalBuilder.getTime(startTime)));
	    }
	    if (summary != null) {
		contentLines.add(ICalBuilder.icalProp("SUMMARY",
						      summaryParams,
						      true,
						      summary));
	    }
	    if (description != null) {
		contentLines.add(ICalBuilder.icalProp("DESCRIPTION",
						      descriptionParams,
						      true, description));
	    }
	    if (location != null) {
		contentLines.add(ICalBuilder.icalProp("LOCATION",
						      locationParams,
						      true,
						      location));
	    }
	    if (clasz != null) {
		contentLines.add("CLASS:" + ICalBuilder.escape(clasz));
	    }
	    if (status != null) {
		String string = null;
		switch(status) {
		case TENTATIVE:
		    string = "TENTATIVE";
		    break;
		case CONFIRMED:
		    string =  "CONFIRMED";
		    break;
		case CANCELLED:
		    string =  "CANCELLED";
		    break;
		case NEEDS_ACTION:
		    string = "NEEDS-ACTION";
		    break;
		case COMPLETED:
		    string = "COMPLETED";
		    break;
		case IN_PROCESS:
		    string = "IN_PROCESS";
		    break;
		case DRAFT:
		    string = "DRAFT";
		case FINAL:
		    string = "FINAL";
		}
		contentLines.add("STATUS:" + string);
	    }
	    if (priority > 0) {
		contentLines.add("PRIORITY:" + priority);
	    }
	    if (url != null) {
		contentLines.add("URL:" + url);
	    }
	    if (geo != null) {
		contentLines.add("GEO:" + geo);
	    }
	    super.createContentLines();
	}

	@Override
	protected void clearContentLines() {
	    super.clearContentLines();
	    for (Alarm alarm: alarms) {
		alarm.clearContentLines();
	    }
	}
    }

    /**
     * Superclass for those ICalendar components that
     * can contain alarms.
     */
    public static abstract class CommonWithAlarm extends Common {

	ArrayList<Alarm> alarms = new ArrayList<>();

	/**
	 * Constructor.
	 * The first argument (uid) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.7">
	 * Section 3.8.4.7 of RFC 5545</A>.
	 * The second argument (sequence) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.4">
	 * Section 3.8.7.4 of RFC 5545</A>.
	 * The third argument (created) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.1">
	 * Section 3.8.7.1 of RFC 5545</A>.
	 * The fourth argument (lastModified) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.3">
	 * Section of 3.8.7.3 RFC 5545</A>.
	 * <P>
	 * The types of the {@link java.time.temporal.TemporalAccessor}
	 * arguments must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param uid the globally unique identifier for this iCalendar
	 *        component
	 * @param sequence a sequence number labeling changes to this
	 *        iCalendar component
	 * @param created the time at which this iCalendar component was created
	 * @param lastModified the last time at which this iCalendar component
	 *         was modified
	 * @exception IllegalArgumentException an argument was null or
	 *            the sequence number was negative or a temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	protected CommonWithAlarm(String uid, int sequence,
			 TemporalAccessor created,
			 TemporalAccessor lastModified)
	    throws IllegalArgumentException
	{
	    super(uid, sequence, created, lastModified);
	}


	/**
	 * Add an alarm to this iCalendar component.
	 * <P>
	 * This method should not be called directly because it is
	 * called by an alarm's constructor.
	 * @param alarm the alarm
	 * @see ICalBuilder.Alarm
	 */
	final void add(Alarm alarm) {
	    if (alarm == null) {
		throw new IllegalArgumentException(errorMsg("nullAlarm"));
	    }
	    alarms.add(alarm);
	}


	@Override
	protected void clearContentLines() {
	    super.clearContentLines();
	    for (Alarm alarm: alarms) {
		alarm.clearContentLines();
	    }
	}
	
	/**
	 * Create the content for this event (a sequence of lines)
	 * that will make up part of an iCalendar file).
	 */
	protected void createContentLines() {
	    super.createContentLines();
	    for (Alarm alarm: alarms) {
		alarm.createContentLines();
		for (String s: alarm.contentLines) {
		    contentLines.add(s);
		}
	    }
	}
    }

    /**
     * The iCalendar event component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.1">
     * Section 3.6.1 of RFC 5545</A> for a description of an
     * iCalendar event component.
     */
    public static class Event extends CommonWithAlarm {
	TemporalAccessor endTime = null;
	String[] endTimeParams = null;
	boolean transparent = false;

	/**
	 * Constructor.
	 * The first argument (uid) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.7">
	 * Section 3.8.4.7 of RFC 5545</A>.
	 * The second argument (sequence) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.4">
	 * Section 3.8.7.4 of RFC 5545</A>.
	 * The third argument (created) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.1">
	 * Section 3.8.7.1 of RFC 5545</A>.
	 * The fourth argument (lastModified) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.3">
	 * Section of 3.8.7.3 RFC 5545</A>.
	 * <P>
	 * The types of the {@link java.time.temporal.TemporalAccessor}
	 * arguments must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param uid a unique identifier labeling this event
	 * @param sequence the sequence number for updates for this
	 *        event
	 * @param created the time at which this event was created
	 * @param lastModified the last time at which this event was
	 *        modified
	 * @exception IllegalArgumentException an argument was null or
	 *            the sequence number was negative or a temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public Event(String uid, int sequence,
		     TemporalAccessor created, TemporalAccessor lastModified)
	    throws IllegalArgumentException
	{
	    super(uid, sequence, created, lastModified);
	}

	/**
	 * Set the starting time for this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.4">
	 * Section 3.8.2.4 of RFC 5545</A>
	 * for a description of the start-time property
	 * Parameters should not be used unless the run-time type of
	 * the first argument is {@link java.time.LocalDate} or
	 * {@link java.time.LocalDateTime}. The parameters defined
	 * by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param startTime the time at which this item starts; null if there
	 *        is no start time
	 * @param params the parameters for the starting time, if any
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public void setStartTime(TemporalAccessor startTime, String... params) {
	    super.setStartTime(startTime, params);
	}

	/**
	 * Set the ending time for this iCalendar component.
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.2">
	 * Section 3.8.2.2 of RFC 5545</A>
	 *  for a description of the end-time property.
	 * Parameters should not be used unless the run-time type of
	 * the first argument is {@link java.time.LocalDate} or
	 * {@link java.time.LocalDateTime}.  The parameters defined
	 * by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>

	 * @param endTime the time at which this item ends; null if there is
	 *        no end time
	 * @param params the parameters for the starting time, if any
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public void setEndTime(TemporalAccessor endTime,
			     String ... params)
	{
	    checkTemporalAccessor(endTime);
	    this.endTime = endTime;
	    endTimeParams = params.clone();
	}

	/**
	 * Set the summary property for this event.
	 * The first argument is typically a short string describing an
	 * event and is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.12">
	 * Section 3.8.1.12 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param summary a summary describing this event; null if there is
	 *        no summary
	 * @param parameters the parameters for this property
	 */
	public void setSummary(String summary, String... parameters)
	{
	    super.setSummary(summary, parameters);
	}

	/**
	 * Set the description for this event.
	 * The description of an iCalendar component and is
	 * described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.5">
	 * Section 3.8.1.5 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param description a full description of this event; null
	 *        if there is no description
	 * @param parameters the parameters for this property
	 */
	public void setDescription(String description, String... parameters)
	{
	    super.setDescription(description, parameters);
	}

	/**
	 * Set the location for this iCalendar component.
	 * The location property describes the location of an event and
	 * is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.7">
	 * Section 3.8.1.7 of RFC 5545</A>.
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param location the location for this event; null if no location is
	 *        specified
	 * @param parameters the parameters for this property
	 */
	public void setLocation(String location, String... parameters)
	{
	    super.setLocation(location, parameters);
	}


	/**
	 * Set the status for this iCalendar component
	 * The status property is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.11">
	 * Section 3.8.1.11 of RFC 5545</A>.
	 * @param status the status ({@link Status#TENTATIVE},
	 *        {@link Status#CONFIRMED}, or {@link Status#CANCELLED}); null
	 *        to indicate that their is no status.
	 * @exception IllegalArgumentException the status is not allowed for
	 *            this iCalendar component
	 */
	public void setStatus(Status status) throws IllegalArgumentException {
	    if (status != null) {
		switch (status) {
		case TENTATIVE:
		case CONFIRMED:
		case CANCELLED:
		    break;
		default:
		    throw new IllegalArgumentException
			(errorMsg("statusNotAllowed", status));
		}
	    }
	    super.setStatus(status);
	}

	/**
	 * Set the priority of this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.9">
	 * Section 3.8.1.9 of RFC 5545</A> for a description of the
	 * priority property.
	 * @param priority the priority, a value in the
	 *        range [0, 9]
	 */
	public void setPriority(int priority) {
	    super.setPriority(priority);
	}

	/**
	 * Indicate that this component is transparent (that is, it 
	 * will not 'hide' other calendar components or prevent them
	 * from being present over the same time interval).
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.7">
	 * Section 3.8.2.7 of RFC 5545</A>.
	 */
	public void setTransparent() {
	    transparent = true;
	}


	@Override
	protected void createContentLines() {
	    contentLines.add("BEGIN:VEVENT");
	    if (endTime != null) {
		addProperty("DTEND", endTimeParams, false,
			    ICalBuilder.getTime(endTime));
	    }
	    if (transparent) {
		contentLines.add("TRANSP:TRANSPARENT");
	    }
	    super.createContentLines();
	    contentLines.add("END:VEVENT");
	}
    }

    /**
     * Status values.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.11">
     * Section 3.8.1.11 of RFC 5545</A> for a detailed description of
     * these values.
     * 
     */
    public enum Status {
	/**
	 * Indicates that an event is tentative.
	 */
	TENTATIVE,
	/**
	 * Indicates that an event is definite.
	 */
	CONFIRMED,
	/**
	 * Indicates that an event, to-do component, or a journal
	 * component was canceled.
	 */
	CANCELLED,
	/**
	 * Indicates a to-do component requires some sort of action.
	 */
	NEEDS_ACTION,
	/**
	 * Indicates that a to-do component was completed.
	 */
	COMPLETED,
	/**
	 * Indicates that a to-do component is in process.
	 */
	IN_PROCESS,
	/**
	 * Indicates that a journal component is a draft version
	 */
	DRAFT,
	/**
	 * Indicates that a journal component is the final version.
	 */
	FINAL
    }

    /**
     * Duration units.
     * These units are described in
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.3.6">
     * Section 3.3.6 of RFC 5545</A>.
     */
    public enum Units {
	/**
	 * Units of seconds.
	 */
	SECONDS,
	/**
	 * Units of minutes.
	 */
	MINUTES,
	/**
	 * Units of hours.
	 */
	HOURS,
	/**
	 * Units of days.
	 */
	DAYS,
	/**
	 * Units of weeks.
	 */
	WEEKS
    }

    /**
     * The iCalendar to-do component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.2">
     * Section 3.6.2 of RFC 5545</A> for a description of the
     * iCalendar to-do component.
     */
    public static class ToDo extends CommonWithAlarm {
	TemporalAccessor due;
	String[] dueParams = null;
	int percentCompleted = -1;
	TemporalAccessor completed;
	ArrayList<Alarm> alarms = new ArrayList<>();

	/**
	 * Constructor.
	 * The first argument (uid) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.7">
	 * Section 3.8.4.7 of RFC 5545</A>.
	 * The second argument (sequence) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.4">
	 * Section 3.8.7.4 of RFC 5545</A>.
	 * The third argument (created) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.1">
	 * Section 3.8.7.1 of RFC 5545</A>.
	 * The fourth argument (lastModified) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.3">
	 * Section of 3.8.7.3 RFC 5545</A>.
	 * <P>
	 * The types of the {@link java.time.temporal.TemporalAccessor}
	 * arguments must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param uid a unique identifier labeling this event
	 * @param sequence the sequence number for updates for this
	 *        event
	 * @param created the time at which this event was created
	 * @param lastModified the last time at which this event was
	 *        modified
	 * @exception IllegalArgumentException an argument was null or
	 *            the sequence number was negative or a temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public ToDo(String uid, int sequence,
		    TemporalAccessor created, TemporalAccessor lastModified)
	    throws IllegalArgumentException
	{
	    super(uid, sequence, created, lastModified);
	}
	
	/**
	 * Set the starting time for this component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.4">
	 * Section 3.8.2.4 of RFC 5545</A>
	 * for a description of the start-time property.
	 * Parameters should not be used unless the run-time type of
	 * the first argument is {@link java.time.LocalDate} or
	 * {@link java.time.LocalDateTime}. The parameters defined
	 * by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param startTime the time at which this component starts;
	 *        null if no start time is specified
	 * @param params the parameters for the start time, if any
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public void setStartTime(TemporalAccessor startTime,
				 String ... params)
	{
	    super.setStartTime(startTime);
	}

	/**
	 * Set the  date-time due time for this component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.3">
	 * Section 3.8.2.3 of RFC 5545</A>
	 *  for a description of the date-time due property.
	 * Parameters should not be used unless the run-time type of
	 * the first argument is {@link java.time.LocalDate} or
	 * {@link java.time.LocalDateTime}.  The parameters defined
	 * by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param due the time at which this component is due; null if such
	 *        a time is not specified
	 * @param params the parameters for due, if any
	 * @exception IllegalStateException two methods used to set the
	 *            due time
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public void setDueTime(TemporalAccessor due, String... params)
	{
	    if (duration != 0 && due != null) {
		throw new IllegalStateException(errorMsg("dateDuration"));
	    }
	    checkTemporalAccessor(due);
	    this.due = due;
	    dueParams = params.clone();
	}


	/**
	 * Set the summary property for this event.
	 * The first argument is typically a short string describing an
	 * event and is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.12">
	 * Section 3.8.1.12 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param summary a summary describing this event; null if there is
	 *        no summary
	 * @param parameters the parameters for this property
	 */
	public void setSummary(String summary, String... parameters)
	{
	    super.setSummary(summary, parameters);
	}

	/**
	 * Set the description for this event.
	 * The description of an iCalendar component and is
	 * described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.5">
	 * Section 3.8.1.5 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param description a full description of this event; null
	 *        if there is no description
	 * @param parameters the parameters for this property
	 */
	public void setDescription(String description, String... parameters)
	{
	    super.setDescription(description, parameters);
	}

	/**
	 * Set the location for this iCalendar component.
	 * The location property describes the location of an event and
	 * is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.7">
	 * Section 3.8.1.7 of RFC 5545</A>.
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param location the location for this event; null if no location is
	 *        specified
	 * @param parameters the parameters for this property
	 */
	public void setLocation(String location, String... parameters)
	{
	    super.setLocation(location, parameters);
	}


	/**
	 * Set the duration for an iCalendar component.
	 * Durations are define in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.5">
	 * Section 3.8.2.5 of RFC 5545</A>
	 * @param duration the duration, either a positive integer
	 *        or 0 if the duration should be ignored
	 * @param units the units for the duration
	 *        ({@link Units#SECONDS}, {@link Units#MINUTES},
	 *        {@link Units#HOURS}, {@link Units#DAYS}, or
	 *        {@link Units#WEEKS}); null only if the duration is 0
	 * @exception UnsupportedOperationException if an iCalendar
	 *            component does not have a duration.
	 */
	@Override
	public void setDuration(int duration, Units units) {
	    if (due != null) {
		throw new IllegalStateException(errorMsg("dateDuration"));
	    }
	    super.setDuration(duration,  units);
	}
 
	/**
	 * Set the status for this iCalendar component.
	 * The status property is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.11">
	 * Section 3.8.1.11 of RFC 5545</A>.
	 * @param status the status ({@link Status#NEEDS_ACTION},
	 *        {@link Status#COMPLETED}, {@link Status#IN_PROCESS},
	 *         or {@link Status#CANCELLED}).
	 * @exception IllegalArgumentException the status is not
	 *            appropriate for this component
	 */
	public void setStatus(Status status)
	    throws IllegalArgumentException
	{ 
	    if (status != null) {
		switch (status) {
		case NEEDS_ACTION:
		case COMPLETED:
		case IN_PROCESS:
		case CANCELLED:
		    break;
		default:
		    throw new IllegalArgumentException
			(errorMsg("statusNotAllowed", status));
		}
	    }
	    super.setStatus(status);
	}

	/**
	 * Set the priority of this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.9">
	 * Section 3.8.1.9 of RFC 5545</A> for a description of the
	 * priority property.
	 * @param priority the priority, a value in the
	 *        range [0, 9]
	 */
	public void setPriority(int priority) {
	    super.setPriority(priority);
	}

	/**
	 * Set the percent-completed field for this iCalendar component
	 * @param percent the percent completed, where 0 indicates that
	 *        the "to-do" has not yet started and 100 indicates that
	 *        it has been completed
	 * @exception IllegalArgumentException the argument is less than
	 *            zero or larger than 100
	 */
	public void setPercentCompleted(int percent) {
	    if (percent < 0 || percent > 100) {
		throw new IllegalArgumentException
		    (errorMsg("percent", percent));
	    }
	    this.percentCompleted = percent;
	}

	/**
	 * Set the time indicating that this iCalendar component has
	 * been completed.
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param completed the time at which this component was
	 *        completed
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public void setCompleted(TemporalAccessor completed) {
	    checkTemporalAccessor(completed);
	    this.completed = completed;
	}

	@Override
	protected void createContentLines() {
	    contentLines.add("BEGIN:VTODO");
	    if (completed != null) {
		contentLines.add("COMPLETED:"
				 + ICalBuilder.getTime(completed));
	    }
	    if (percentCompleted >= 0) {
		contentLines.add("PERCENT-COMPLETE:" + percentCompleted);
	    }
	    if (due != null) {
		addProperty("DUE", dueParams, false, ICalBuilder.getTime(due));
	    }
	    super.createContentLines();
	    contentLines.add("END:VTODO");
	}
    }

    /**
     * The iCalendar journal component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.3">
     * Section 3.6.3 of RFC 5545</A> for a description of an
     * iCalendar journal component.
     * 
     */
    public static class Journal extends Common {

	/**
	 * Constructor.
	 * The first argument (uid) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.4.7">
	 * Section 3.8.4.7 of RFC 5545</A>.
	 * The second argument (sequence) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.4">
	 * Section 3.8.7.4 of RFC 5545</A>.
	 * The third argument (created) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.1">
	 * Section 3.8.7.1 of RFC 5545</A>.
	 * The fourth argument (lastModified) is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.7.3">
	 * Section of 3.8.7.3 RFC 5545</A>.
	 * <P>
	 * The types of the {@link java.time.temporal.TemporalAccessor}
	 * arguments must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param uid the globally unique identifier for this iCalendar
	 *        component
	 * @param sequence a sequence number labeling changes to this
	 *        iCalendar component
	 * @param created the time at which this iCalendar component was created
	 * @param lastModified the last time at which this iCalendar component
	 *         was modified
	 * @exception IllegalArgumentException an argument was null or
	 *            the sequence number was negative or a temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	public Journal(String uid, int sequence,
			 TemporalAccessor created,
			 TemporalAccessor lastModified)
	    throws IllegalArgumentException
	{
	    super(uid, sequence, created, lastModified);
	}

	/**
	 * Set the start time for this iCalendar component.
	 * Please see
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.4">
	 * Section 3.8.2.4 of RFC 5545</A>
	 * for a description of the start-time property.  The parameters
	 * defined by RFC 5545 are
	 * <UL>
	 *   <LI>VALUE. Values (which follow an "=" character) can be
	 *       DATE-TIME or DATE.
	 *   <LI>TZID. Please see
	 *       <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.19">
	 *       Section 3.2.19 of RFC 5545</A>.
	 * </UL>
	 * <P>
	 * The type of the {@link java.time.temporal.TemporalAccessor}
	 * argument must be either
	 * {@link java.time.LocalDate}, {@link java.time.LocalDateTime},
	 * {@link java.time.ZonedDateTime}, or {@link java.time.Instant},
	 * The classes{@link java.time.LocalDate} and
	 * {@link java.time.LocalDateTime}
	 * will be treated as a "floating time" (see RFC 5545) and both
	 * {@link java.time.ZonedDateTime} and {@link java.time.Instant} will
	 * have their times represented using UTC. To convert a LocalDate
	 * value to a LocalDateTime value, use  methods such as
	 * {@link java.time.LocalDate#atStartOfDay()}. To convert a
	 * LocalDateTime value to a ZonedDateTime object, use the
	 * method {@link java.time.LocalDateTime#atZone(java.time.ZoneId)}.
	 * To get the default time zone for the Java virtual machine, use
	 * the method {@link java.time.ZoneId#systemDefault()}.
	 * @param startTime the time at which the activity represented
	 *        by this  iCalendar component starts
	 * @param parameters the parameters
	 * @exception IllegalArgumentException the temporal accessor
	 *            argument's type is not a recognized subclass of
	 *            TemporalAccesssor
	 */
	@Override
	public void setStartTime(TemporalAccessor startTime,
				 String... parameters)
	    throws IllegalArgumentException
	{
	    super.setStartTime(startTime, parameters);
	}

	/**
	 * Set the summary property for this event.
	 * The first argument is typically a short string describing an
	 * event and is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.12">
	 * Section 3.8.1.12 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param summary a summary describing this event; null if there is
	 *        no summary
	 * @param parameters the parameters for this property
	 */
	public void setSummary(String summary, String... parameters)
	{
	    super.setSummary(summary, parameters);
	}

	/**
	 * Set the description for this event.
	 * The description of an iCalendar component and is
	 * described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.5">
	 * Section 3.8.1.5 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param description a full description of this event; null
	 *        if there is no description
	 * @param parameters the parameters for this property
	 */
	public void addDescription(String description, String... parameters)
	{
	    addProperty("DESCRIPTION", parameters, true, description);
	}

	protected void createContentLines() {
	    contentLines.add("BEGIN:VJOURNAL");
	    super.createContentLines();
	    contentLines.add("END:VJOURNAL");
	}

    }

    /**
     * Enumeration describing the types of actions associated with
     * alarms as described in
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.6.1">
     * Section 3.8.6.1 of RFC 5545</A>.
     */
    public enum AlarmType {
	/**
	 * The alarm is an audio alarm.
	 */
	AUDIO,
	/**
	 * The alarm consists of displayed text.
	 */
	DISPLAY,
	/**
	 * The alarm consists of email that should be sent. 
	 */
	EMAIL
    }

    /**
     * The iCalendar alarm component class.
     * Please see
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.6">
     * Section 3.6.6 of RFC 5545</A> for a description of the iCalendar
     * alarm component.
     */
    public static class Alarm extends Base {
	ICalBuilder.Common parent = null;
	int min;
	boolean fromStart;
	String summary = null;
	String[] summaryParams = null;
	String description = null;
	String[] descriptionParams = null;
	boolean audio;
	boolean display;
	boolean email;
	int repeat = 0;
	int duration = 0;
	/**
	 * Constructor.
	 * @param parent the iCalendar component to which this alarm is
	 *        attached
	 * @param offset the offset from the starting or ending time
	 *        of an event in units of minutes.
	 * @param type the type of the alarm
	 *        ({@link ICalBuilder.AlarmType#AUDIO},
	 *         {@link ICalBuilder.AlarmType#DISPLAY},
	 *          or {@link ICalBuilder.AlarmType#EMAIL}).
	 * @param fromStart true if the alarm is offset from the
	 *        start of the event; false if the alarm is offset from
	 *        the end of the event;
	 */
	public Alarm(CommonWithAlarm parent,
		     int offset, AlarmType type, boolean fromStart)
	    throws IllegalArgumentException
	{
	    if (type == null) {
		throw new IllegalArgumentException(errorMsg("nullAlarmType"));
	    }
	    switch (type) {
	    case AUDIO:
		audio = true;
		break;
	    case DISPLAY:
		display = true;
		break;
	    case EMAIL:
		email = true;
		break;
	    }
	    min = offset;
	    this.fromStart = fromStart;
	    parent.add(this);
	    this.parent = parent;
	}

	/**
	 * Set the summary field for this alarm.
	 * The use of this method is optional: defaults will be provided
	 * if this method is not used, provided that a summary was
	 * provided in the enclosing component.
	 * The mandatory argument is typically a short string describing an
	 * event and is described in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.12">
	 * Section 3.8.1.12 of RFC 5545</A>.
	 * For an alarm whose action is "DISPLAY", the mandatory
	 * DESCRIPTION property is created by choosing the first non-null
	 * value from this instance's summary field, this instance's
	 * description field, the enclosing component's summary field, and
	 * finally the enclosing component's description field in that
	 * order.
	 * For an email alarm, both the summary and the description are
	 * required.  If any of these is null, the corresponding fields in
	 * the enclosing iCalendar component will be used instead.
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param summary a summary describing this event; null if there is
	 *        no summary
	 * @param parameters the parameters for this property
	 */
	public void setSummary(String summary, String... parameters) {
	    this.summary = summary;
	    this.summaryParams = parameters.clone();
	}

	/**
	 * Set the description for this alarm.
	 * The use of this method is optional: defaults will be provided
	 * if this method is not used, provided that a summary was
	 * provided in the enclosing component.
	 * The mandatory argument is a string describing the alarm in
	 * more detail than what would appear in a summary.
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.1.5">
	 * Section 3.8.1.5 of RFC 5545</A>.  
	 * <P>
	 * Parameters defined by RFC 5545 for a comment are
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.10">
	 * LANGUAGE</A>
	 * and
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.2.1">
	 * ALTREP</A>.
	 * @param description a full describing this event; null if there is
	 *        no description
	 * @param parameters the parameters for this property
	 */
	public void setDescription(String description, String... parameters) {
	    this.description = description;
	    this.descriptionParams = parameters.clone();
	}


	/**
	 * Set the repeat count and the corresponding duration.
	 * The first argument refers to the REPEAT property specified in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.6.2">
	 * Section 3.8.6.2 of RFC 5545</A>, and the second argument
	 * refers to the DURATION property specified in
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.8.2.5">
	 * Section 3.8.2.5  of RFC 5545</A>.
	 * @param repeat the repetition count
	 * @param duration the duration in minutes
	 */
	public void repeat(int repeat, int duration)
	    throws IllegalArgumentException
	{
	    if (repeat < 0) throw new IllegalArgumentException
				(errorMsg("negativeRepeat", repeat));
	    if (duration < 0) throw new IllegalArgumentException
				  (errorMsg("negativeDuration", duration));
	    if (repeat == 0 || duration == 0) {
		repeat = 0;
		duration = 0;
	    }
	    this.repeat = repeat;
	    this.duration = duration;
	}
	
	@Override
	protected void createContentLines() {
	    contentLines.add("BEGIN:VALARM");
	    if (audio) {
		contentLines.add("ACTION:AUDIO");
	    } else if (display) {
		contentLines.add("ACTION:DISPLAY");

	    } else if (email) {
		contentLines.add("ACTION:EMAIL");
		
	    }
	    if (fromStart) {
		if (min < 0) {
		    contentLines.add("TRIGGER;RELATED=START:-PT"
				     + Math.abs(min) + "M");
		} else {
		    contentLines.add("TRIGGER;RELATED=START:PT"
				     + min + "M");
		}
	    } else {
		if (min < 0) {
		    contentLines.add("TRIGGER;RELATED=END:-PT"
				     + Math.abs(min) + "M");
		} else {
		    contentLines.add("TRIGGER;RELATED=END:PT"
				     + min + "M");
		}
	    }
	    if (duration > 0) {
		contentLines.add("DURATION:PT" + duration + "M");
		contentLines.add("REPEAT:" + repeat);
	    }
	    if (display) {
		String string = null;
		String[] params = null;
		if (summary != null) {
		    string = summary;
		    params = summaryParams;
		} else if (description != null) {
		    string = description;
		    params = descriptionParams;
		} else if (parent.summary != null) {
		    string = parent.summary;
		    params = parent.summaryParams;
		} else if (parent.description != null) {
		    string = parent.description;
		    params = parent.descriptionParams;
		} else {
		    throw new IllegalStateException(errorMsg("illegalDisplay"));
		}
		contentLines.add(ICalBuilder.icalProp("DESCRIPTION", params,
						      true, string));
		/*
		contentLines.add("DESCRIPTION:"
				  + ICalBuilder.escape(string));
		*/
	    } else if (email) {
		String s1 = (summary != null)? summary: parent.summary;
		String s2 = (description != null)? description:
		    parent.description;
		if (s1 == null) {
		    throw new IllegalStateException(errorMsg("noSummary"));
		}
		if (s2 == null) {
		    throw new IllegalStateException(errorMsg("noDescr"));
		}
		String[] p1 = (summary != null)? summaryParams:
		    parent.summaryParams;
		String[] p2 = (description != null)? descriptionParams:
		    parent.descriptionParams;
		contentLines.add(ICalBuilder.icalProp("SUMMARY", p1, true, s1));
		contentLines.add(ICalBuilder.icalProp("DESCRIPTION",
						      p2, true, s2));
		/*
		contentLines.add("SUMMARY:"
				  + ICalBuilder.escape(s1));
		contentLines.add("DESCRIPTION:"
				  + ICalBuilder.escape(s2));
		*/

	    }
	    super.createContentLines();
	    contentLines.add("END:VALARM");
	}
    }

    ArrayList<Base> items = new ArrayList<>();

    // ArrayList<Event> events = new ArrayList<>();

    /**
     * Add an iCalendar component to this iCalendar builder.
     * @param component the component to add.
     */
    public void add(Common component) {
	items.add(component);
    }

    /**
     * Add a user-specified iCalendar component to this iCalendar builder.
     * @param component the component to add.
     */
    public void add(Component component) {
	items.add(component);
    }

    static void checkTemporalAccessor(TemporalAccessor zdt) {
	if (zdt == null) return;
	if ((zdt instanceof LocalDate)
	    || (zdt instanceof LocalDateTime)
	    || (zdt instanceof ZonedDateTime)
	    || (zdt instanceof Instant)) {
	    return;
	}
	throw new IllegalArgumentException("unrecognized time/date format");
    }

    private static String getTime(TemporalAccessor zdt)
	throws IllegalStateException
    {
	String text = null;
	if (zdt == null) {
	    throw new IllegalStateException
		("TemporalAccessor argument was null");
	}
	if (zdt instanceof LocalDate) {
	    zdt = ((LocalDate)zdt).atStartOfDay();
	}
	if (zdt instanceof LocalDateTime) {
	    text = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(zdt);
	} else {
	    if (zdt instanceof ZonedDateTime) {
		zdt = ((ZonedDateTime)zdt).toInstant();
	    }
	    if (zdt instanceof Instant)  {
		text = DateTimeFormatter.ISO_INSTANT.format(zdt);
	    } else {
		throw new IllegalStateException
		    ("unrecognized time representation");
	    }
	}
	return text.replace("-","").replace(":","")
	    .replaceFirst("[.][0-9]*","");
    }


    private static final TemplateProcessor.KeyMap emptyMap
	= new TemplateProcessor.KeyMap();
    
    private static Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Apply the RFC 5545 encoding rules to a string.
     * @param text the string to encode
     * @return the encoded string
     */
    public static String escape(String text) {
	return text.replace("\\", "\\\\").replace(",", "\\,")
	    .replace(";", "\\;").replace("\r\n", "\n")
	    .replace('\r','\n').replace("\n","\\n");
    }

    // Some values (e.g., URIs) should not be escaped.
    // Except for URIs, escapeValues should be set to true.

    private static final String[] emptyParams = new String[0];


    /**
     * Create an unfolded iCalendar property line when there are no
     * parameters.
     * <P>
     * When there are multiple values, these will be comma-separated.
     * If a comma appears in a value, the escapeValues argument should
     * be set to true. The syntax for iCalendar properties is such that
     * the escapeValues argument will be 'true' only when the values are
     * text.
     * <P>
     * Note: this method is used internally, but is public in case it is
     * useful for other purposes.
     * @param property the name of the property
     * @param escapeValues add escape sequences for reserved characters
     * @param values the values for the property
     * @return an unfolded line representing an iCalendar property, and
     *         without a terminating EOL sequence
     */
    public static String icalProp(String property, boolean escapeValues,
			   String ... values) {
	return icalProp(property, emptyParams, escapeValues, values);
    }

    /**
     * Create an unfolded iCalendar property line when there is one
     * parameter.
     * <P>
     * When there are multiple values, these will be comma-separated.
     * If a comma appears in a value, the escapeValues argument should
     * be set to true. The syntax for iCalendar properties is such that
     * the escapeValues argument will be 'true' only when the values are
     * text.
     * <P>
     * The parameter consists of a keyword or a keyword followed by the
     * character '=" followed by a parameter value. If the parameter
     * contains reserved characters, that value will be quoted (unless
     * that value is already quoted).
     * <P>
     * Note: this method is used internally, but is public in case it is
     * useful for other purposes.
     * @param property the name of the property
     * @param parm a parameter
     * @param escapeValues add escape sequences for reserved characters
     * @param values the values for the property
     * @return an unfolded line representing an iCalendar property, and
     *         without a terminating EOL sequence
     */
    public static String icalProp(String property, String parm,
				  boolean escapeValues, String... values)
    {
	String[] parms = {parm};
	return icalProp(property, parms, escapeValues, values);
    }

    /**
     * Create an unfolded iCalendar property line when there are no
     * parameters.
     * <P>
     * When there are multiple values, these will be comma-separated.
     * If a comma appears in a value, the escapeValues argument should
     * be set to true. The syntax for iCalendar properties is such that
     * the escapeValues argument will be 'true' only when the values are
     * text.
     * <P>
     * Each parameter consists of a keyword or a keyword followed by the
     * character '=" followed by a parameter value. If a parameter
     * contains reserved characters, its value will be quoted (unless
     * its value is already quoted).
     * <P>
     * Note: this method is used internally, but is public in case it is
     * useful for other purposes.
     * @param property the name of the property
     * @param parms a array of  parameters
     * @param escapeValues add escape sequences for reserved characters
     * @param values the values for the property
     * @return an unfolded line representing an iCalendar property, and
     *         without a terminating EOL sequence
     */
    public static String icalProp(String property, String[] parms,
			   boolean escapeValues, String... values)
    {
	StringBuilder sb = new StringBuilder();
	sb.append(property.trim().toUpperCase());
	for (String p: parms) {
	    if (p == null) continue;
	    p = p.trim();
	    if (p.length() == 0) {
		continue;
	    }
	    String theParm = p;
	    if (p.indexOf(',') >= 0
		|| p.indexOf(';') >= 0
		|| p.indexOf(',') >= 0) {
		int ind = p.indexOf('=');
		if (ind >= 0) {
		    ind++;
		    String start = p.substring(0, ind);
		    String tail = p.substring(ind);
		    tail = tail.trim();
		    if (tail.length() > 0) {
			if (tail.charAt(0) == '"') {
			    p = start + tail;
			} else {
			    if (tail.indexOf('"') > 0) {
				throw new IllegalArgumentException
				    (errorMsg("badParm", theParm));
			    }
			    p = start + "\"" + tail + "\"";
			}
		    }
		}
	    }
	    sb.append(";");
	    sb.append(p);
	}
	sb.append(":");
	boolean notFirst = false;
	for (String value: values) {
	    if (escapeValues) {
		value = value.replace("\\", "\\\\").replace(",", "\\,")
		    .replace(";", "\\;").replace("\r\n", "\n")
		    .replace('\r','\n').replace("\n","\\n");
	    }
	    if (notFirst) {
		sb.append(",");
	    }
	    notFirst = true;
	    sb.append(value);
	}
	return (sb.toString());
    }

    /**
     * Apply RFC 5545 line-folding rules to a line of text
     * @param text a line of text
     * @return the line-folded text
     */
    public static String fold(String text) {
	byte[] bytes = text.getBytes(UTF8);
	int len = bytes.length;
	if (len > 64) {
	    int decr = 16;
	    int p = 64+decr;
	    String s1;
	    do {
		p -= decr;
		s1 = text.substring(0, p);
		len = s1.getBytes().length;
	    } while (len > 64);
	    String s2 = text.substring(p);
	    return s1 +"\r\n " + fold(s2);
	} else {
	    return text;
	}
    }

    /**
     * Return an iCalendar object.
     * @return a byte array containing an iCalendar object as UTF-8
     *         encoded text with CRLF end-of-lines
     */
    public byte[] toByteArray() {
	int size = 1024;		// allow ample space for min size
	try {
	    ByteArrayOutputStream os = new ByteArrayOutputStream(size);
	    write(os, true);
	    return os.toByteArray();
	} catch (IOException e) {}
	throw new UnexpectedExceptionError();
    }

    /**
     * Write an iCalendar object to a file.
     * @param file file the file
     * @exception IOException an error occurred while opening or writing
     *            the file
     */
    public void write(File file) throws IOException {
	write(new FileOutputStream(file),  true);
    }

    /**
     * Write an iCalendar object to an output stream().
     * @param os the output stream
     * @param close true if the writer constructed from the output
     *        stream should be closed when the iCalendar object is
     *        complete; false otherwise
     * @exception IOException an error occurred while writing to the
     *            output stream
     */
    public void write(OutputStream os, boolean close) throws IOException {
	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	if (method != null) {
	    map.put("hasMethod", emptyMap);
	    map.put("method", escape(method));
	}
	TemplateProcessor.KeyMapList list = new TemplateProcessor.KeyMapList();
	for (Base item: items) {
	    item.clearContentLines();
	    item.createContentLines();
	    for (String line: item.contentLines) {
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		String folded = fold(line);
		kmap.put("contentLine", folded);
		list.add(kmap);
	    }
	}
	map.put("contentLines", list);
	try {
	    InputStream is = getClass().getResourceAsStream("icalEvents.tpl");
	    Reader r = new InputStreamReader(is, UTF8);
	    TemplateProcessor tp = new TemplateProcessor(map);
	    OutputStreamWriter w = new OutputStreamWriter(os, UTF8);
	    tp.processTemplate(r, w);
	    w.flush();
	    if (close) w.close();
	} catch (IOException eio) {
	    // should not happen as we are not actually using IO
	    throw new RuntimeException
		(errorMsg("iCalendarGen", eio.toString()));
	}
    }
}

//  LocalWords:  iCalendar HREF CRLF ICalBuilder addProperty boolean
//  LocalWords:  ToDo FreeBusy iMIP iTIP setMethod ITIPMethod mdash
//  LocalWords:  DECLINECOUNTER EOL Subclasses Durations subclasses
//  LocalWords:  UnsupportedOperationException URI formatType uri lt
//  LocalWords:  FMTTYPE URIs escapeValues param params VEVENT VTODO
//  LocalWords:  VJOURNAL VFREEBUSY VTIMEZONE PRE AlarmType fromStart
//  LocalWords:  BLOCKQUOTE componentName Superclass uid lastModified
//  LocalWords:  IllegalArgumentException TZID startTime ALTREP IANA
//  LocalWords:  vendorid URL's reltype mailto CN CUTYPE DTSTAMP sb
//  LocalWords:  DTSTART contentLines StringBuilder CANCELLED endTime
//  LocalWords:  DTEND TRANSP VALARM ArrayList UTF hasMethod tpl args
//  LocalWords:  contentLine icalEvents ICalendarEvents errorMsg lang
//  LocalWords:  NetErrorMsg exbundle mistyped nullUnits nullUID parm
//  LocalWords:  negativeDuration clearContentlines unsupportedMethod
//  LocalWords:  negativeSequence illegalPriority nullContact nullURI
//  LocalWords:  nullAlarm statusNotAllowed dateDuration parms img os
//  LocalWords:  nullAlarmType negativeRepeat illegalDisplay badParm
//  LocalWords:  iCalendarGen TimeZone clearContentLines src
//  LocalWords:  IOException
