/**
 * The calendar package supports the iCalendar format defined in
 * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>.
 * This package contains classes that can create and parse a
 * file or similar object that uses the iCalendar format.
 * The class
 * <UL>
 *  <LI>{@link org.bzdev.net.calendar.ICalBuilder} is the class used
 *      to generate an iCalendar object, either as a byte array, a file,
 *      or by writing to an output stream.
 *  <LI> {@link org.bzdev.net.calendar.ICalParser} is the class used
 *      to parse an iCalendar object stored a a byte array, a file, or
 *      obtained by reading from an input stream.
 * </UL>
 * An iCalendar file consists of a sequence of lines using CRLF to
 * indicate the end of a line. Lines longer than 75 characters are
 * folded (broken up into multiple lines with each continuation line
 * starting with a space).  The ICalBuilder and ICalParser classes
 * handle line folding automatically. It also handles cases in which
 * various characters must be replaced with escape sequences.
 * <P>
 * An iCalendar component starts with a line beginning with "BEGIN:"
 * followed by the component name, and is terminated by a line
 * beginning with "END:" followed by the same component name. The
 * intermediate lines specify various properties.  Each property
 * begins with a property name, followed by either a semicolon or a
 * colon. A semicolon separates the property name, or a previous
 * parameter, from a parameter.  A colon must follow the parameters or
 * the property name (when there are no parameters). The properties
 * that are acceptable for a given component are specified in the
 * RFC.
 */
package org.bzdev.net.calendar;

//  LocalWords:  iCalendar HREF CRLF ICalBuilder ICalParser
