package org.bzdev.net.calendar;
import java.util.List;

/**
 * The iCalendar component interface. 
 * This interface is used for parsing iCalendar objects.
 * <P>
 * Each component is characterized by a collection of properties
 * and a collection of iCalendar components that are parts of this
 * component (for example, the event and to-do components contain
 * alarm components). The relation between the classes presenting
 * components, properties, and parameters is shown in the following
 * UML diagram:
 * <P style="text-align: center">
 * <img src="doc-files/ICalParser.png">
 * <P>
 * Starting from the top-level component (an instance of{@link
 * ICalParser}), calling {@link ICalComponent#getComponents()} will
 * return a list of the components with the same parent.  For any
 * level, the method {@link ICalComponent#getProperties()} will return
 * a list of {@link ICalProperty} objects containing the properties of
 * that component.
 */
public interface ICalComponent {
    /**
     * Get the name of an iCalendar component.
     * the name "VCALENDAR" is the name of the top-level component.
     * Other component names defined by
     * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>
     * are "VEVENT", "VTODO", "VJOURNAL", and "VALARM".
     * @return the name
     */
    public String getName();

    /**
     * Get the properties for an iCalendar component.
     * @return the component's properties
     */
    public List<ICalProperty> getProperties();

    /**
     * Get the components nested within this component.
     * Suitable components defined in RFC 5545
     * consist of
     * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.6">
     * alarms</A>.
     * @return a list of the nested components
     */
    public List<ICalComponent> getComponents();
}

//  LocalWords:  iCalendar UML img src ICalParser ICalComponent HREF
//  LocalWords:  getComponents getProperties IcalProperty VCALENDAR
//  LocalWords:  VEVENT VTODO VJOURNAL VALARM
