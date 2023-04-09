package org.bzdev.net.calendar;
import java.io.*;
import java.util.*;
import org.bzdev.util.CopyUtilities;


//@exbundle org.bzdev.net.calendar.lpack.Calendar

/**
 * Parser for iCalendar objects.
 * An iCalendar parser in this implementation is also an
 * implementation of the interface {@link ICalComponent} because this
 * class represents an iCalendar's component (a VCALENDAR) has
 * properties associated with it and contains other iCalendar
 * components.  Aside from the constructors, there are no public
 * methods other than those defined by {@link ICalComponent}.
 * <P>
 * The documentation for {@link ICalComponent} describes the parse
 * tree created when this method's constructor is called.
 */
public class ICalParser implements ICalComponent {

    static String errorMsg(String key, Object... args) {
	return CalendarErrorMsg.errorMsg(key, args);
    }

    ArrayList<ICalComponent> nodelist = null;
    ArrayList<ICalProperty> properties = null;

    private static String textProperties[] = {
	"ACTION",
	"CALSCALE",
	"CATEGORIES",
	"CLASSIFICATION",
	"COMMENT",
	"CONTACT",
	"DESCRIPTION",
	"LOCATION",
	"METHOD",
	"PRODID",
	"RELATED-TO",
	"REQUEST-STATUS",
	"RESOURCES",
	"STATUS",
	"SUMMARY",
	"TRANSP",
	"TZID",
	"TZNAME",
	"UID",
	"VERSION"
    };

    private Set<String> hasText =
	new HashSet<String>(2*textProperties.length);

    private ICalParser() {
	for (String name: textProperties) {
	    hasText.add(name);
	}
    }

    /**
     * Constructor.
     * The byte array contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>.
     * @param data the byte array containing an iCalendar
     * @throws IOException an IO error occurred
     */
    public ICalParser(byte[] data) throws IOException {
	this();
	init(data);
    }

    /**
     * Constructor adding text-property names.
     * The byte array contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>.
     * <P>
     * The second argument is a list of the names of additional properties
     * (these must not be ones explicitly defined in RFC 5545) whose values
     * are text.  RFC 5545 treats text differently from other property
     * values. In particular, a ',' or ';' can values for fields containing
     * multiple values, with a ',' or ';' escaped to denote text.
     * @param data the byte array containing an iCalendar
     * @param names a list of additional property names whose values
     *        are text fields
     * @throws IOException an IO error occurred
     */
    public ICalParser(byte[] data, String[] names) throws IOException {
	this();
	for (String name: names) {
	    hasText.add(name);
	}
	init(data);
    }

    /**
     * Constructor given a file.
     * The file contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * <A HREF="https://tools.ietf.org/html/rfc5545">RFC 5545</A>.
     * @param f the file to read
     * @exception IOException an error occurred while reading the input
     */
    public ICalParser(File f) throws IOException {
	this(new FileInputStream(f));
    }

    /**
     * Constructor given an input stream.
     * The input stream contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * @param is the input stream to read
     * @exception IOException an error occurred while reading the input
     */
    public ICalParser(InputStream is) throws IOException {
	this(readInput(is));
    }

    /**
     * Constructor given a file and adding text-property names.
     * The file contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * <P>
     * The second argument is a list of the names of additional properties
     * (these must not be ones explicitly defined in RFC 5545) whose values
     * are text.  RFC 5545 treats text differently from other property
     * values. In particular, a ',' or ';' can values for fields containing
     * multiple values, with a ',' or ';' escaped to denote text.
     * @param f the file to read
     * @param names a list of additional property names whose values
     *        are text fields
     * @exception IOException an error occurred while reading the input
     */
    public ICalParser(File f, String[] names) throws IOException {
	this(new FileInputStream(f), names);
    }

    /**
     * Constructor given and input stream and adding text-property names.
     * The input stream contains UTF-8 encoded text with CRLF end
     * of lines as described in
     * <P>
     * The second argument is a list of the names of additional properties
     * (these must not be ones explicitly defined in RFC 5545) whose values
     * are text.  RFC 5545 treats text differently from other property
     * values. In particular, a ',' or ';' can values for fields containing
     * multiple values, with a ',' or ';' escaped to denote text.
     * @param is the input stream to read
     * @param names a list of additional property names whose values
     *        are text fields
     * @exception IOException an error occurred while reading the input
     */
    public ICalParser(InputStream is, String[] names) throws IOException {
	this(readInput(is), names);
    }


    private static byte[] readInput(InputStream is) throws IOException {
	ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
	// CopyUtilities.copyStream(is, os);
	is.transferTo(os);
	return os.toByteArray();
    }

    private void init(byte[] data) throws IOException {
	ByteArrayInputStream is = new ByteArrayInputStream(data);
	BufferedReader r = new BufferedReader(new InputStreamReader(is));
	StringBuilder sb = new StringBuilder();
	ArrayList<String> lines = new ArrayList<>(256);
	String iline = r.readLine();
	if (iline == null) {
	    return;
	}
	sb.append(iline);
	iline = r.readLine();
	// unfold lines
	while (iline != null) {
	    if (iline.length() != 0) {
		if (iline.startsWith(" ")) {
		    sb.append(iline.substring(1));
		} else {
		    lines.add(sb.toString());
		    sb.setLength(0);
		    sb.append(iline);
		}
	    }
	    iline = r.readLine();
	}
	if (sb.length() > 0) {
	    lines.add(sb.toString());
	    sb.setLength(0);	// in case this helps with garbage collection
	}
	Node parent = null;
	ArrayList<ICalProperty> properties = null;
	for (String line: lines) {
	    // System.out.println("line = " + line);
	    int len = line.length();
	    int ind = line.indexOf(':');
	    if (ind < 0) {
		throw new IOException(errorMsg("emptyLine"));
	    }
	    String start = line.substring(0, ind).toUpperCase(Locale.US);
	    if(start.equals("BEGIN")) {
		String name = line.substring(ind+1).trim().toUpperCase();
		if (name.length() == 0) {
		    throw new IOException(errorMsg("emptyName"));
		}
		Node node = new Node();
		node.name = name;
		node.parent = parent;
		parent = node;
		properties = new ArrayList<ICalProperty>(64);
	    } else if (start.equals("END")) {
		String name = line.substring(ind+1).trim().toUpperCase();
		if (parent == null) {
		    throw new IOException("noParent");
		}
		if (!parent.name.equals(name)) {
		    String msg =
			errorMsg("parentNameMismatch", parent.name, name);
		    throw new IOException(msg);
		}
		if (parent.parent == null) {
		    nodelist = parent.children;
		    this.properties = parent.properties;
		} else {
		    parent.parent.children.add(parent);
		}
		parent = parent.parent;
	    } else {
		int cind = line.indexOf(';');
		boolean hasparams = false;
		if (cind >= 0 && cind < ind) {
		    ind = cind;
		    hasparams = true;
		}
		ICalProperty property = new ICalProperty();
		property.name = line.substring(0, ind).trim().toUpperCase();
		parent.properties.add(property);
		int startInd = ind+1;
		if (hasparams) {
		    ArrayList<ICalParameter> parameters = new ArrayList<>();
		    while (hasparams) {
			if (startInd == line.length()) {
			    throw new IOException(errorMsg("emptyLine"));
			}
			line = line.substring(startInd);
			ind = line.indexOf("=");
			if (ind < 0) {
			    throw new IOException(errorMsg("missingEq"));
			}
			ICalParameter p = new ICalParameter();
			p.name = line.substring(0, ind).trim().toUpperCase();
			if (line.length() == ind) {
			    throw new IOException(errorMsg("missingTail"));
			}
			line = line.substring(ind+1);
			if (line.length() == 0) {
			    throw new IOException(errorMsg("missingTail"));
			}
			p.quoted = (line.charAt(0) == '"');
			if (p.quoted) {
			    line = line.substring(1);
			    if (line.length() == 0) {
				throw new IOException(errorMsg("badString"));
			    }
			    ind = line.indexOf('"');
			} else {
			    ind = -1;
			}
			if (ind < 0) {
			    ind = line.indexOf(':');
			    if (ind < 0) {
				throw new IOException(errorMsg("noColon"));
			    }
			    cind = line.indexOf(';');
			    ind = (cind < 0)? ind:
				((cind < ind)? cind: ind);
			    if (ind < 0) {
				String msg = errorMsg("noOrMisplacedSemicolon");
				throw new IOException(msg);
			    }
			    p.value = line.substring(0, ind).toUpperCase();
			    hasparams = !(cind < 0);
			    if (ind == line.length()) {
				String msg = errorMsg("noOrMisplacedSemicolon");
				throw new IOException(msg);
			    }
			    line = line.substring(ind+1);
			    startInd = 0;
			} else {
			    p.value = line.substring(0, ind);
			    if (ind == line.length()) {
				throw new IOException(errorMsg("badLineParse"));
			    }
			    line = line.substring(ind+1);
			    line = line.stripLeading();
			    if (line.charAt(0) == ':') {
				hasparams = false;
				line = line.substring(1);
			    } else {
				startInd = 0;
			    }
			}
			parameters.add(p);
		    }
		    property.parameters = parameters;
		} else {
		    // no parameters
		    int npind = line.indexOf(":");
		    if (npind < 0 ) {
			throw new IOException(errorMsg("noColon"));
		    }
		    npind++;
		    if (npind == line.length()) {
			String msg = errorMsg("noParameters");
			throw new IOException(msg);
		    }
		    line = line.substring(npind);
		}
		if (hasText.contains(property.name)) {
		    // these might contain multiple values
		    String[] strings1 = line.split("[,;]", -1);
		    String[] strings2 = line.split("(\\\\)*[,;]", -1);
		    ArrayList<String> list = new ArrayList<>();
		    ArrayList<Character> dlist = new ArrayList<>();
		    sb.setLength(0);
		    int lenm1 = strings1.length - 1;
		    int index = 0;
		    for (int i = 0; i < strings1.length; i++) {
			int len1 = strings1[i].length();
			int len2 = strings2[i].length();
			index+= len1;
			if ((len2 - len1) % 2 == 0) {
			    sb.append(strings1[i]);
			    list.add(sb.toString());
			    if (index < line.length()) {
				dlist.add(line.charAt(index));
			    }
			    sb.setLength(0);
			} else {
			    String substring =
				strings1[i].substring(0,len1-1);
			    sb.append(substring);
			    if (i < lenm1) {
				sb.append(line.charAt(index));
			    }
			}
			index++;
		    }
		    if (sb.length() > 0) {
			list.add(sb.toString());
			sb.setLength(0);
		    }
		    if (list.size() == 1) {
			property.value = list.get(0);
		    } else {
			property.values = list;
			property.delims = dlist;
		    }
		} else {
		    property.value = line;
		}
	    }
	}
    }

    @Override
    public String getName() {return "VCALENDAR";}

    @Override
    public List<ICalComponent> getComponents() {
	return Collections.unmodifiableList(nodelist);
    }

    @Override
    public List<ICalProperty> getProperties() {
	return Collections.unmodifiableList(properties);
    }

    /**
     * The iCalendar-component class.
     */
    static class Node implements ICalComponent {
	String name;
	Node parent;
	ArrayList<ICalProperty> properties = new ArrayList<>();
	ArrayList<ICalComponent> children = new ArrayList<>();

	/**
	 * Constructor.
	 */
	protected Node() {}

	/**
	 * Get the name of an iCalendar component.
	 * @return the name
	 */
	public String getName() {
	    return name;
	}

	/**
	 * Get the properties for an iCalendar component.
	 * @return the component's properties
	 */
	public List<ICalProperty> getProperties() {
	    return Collections.unmodifiableList(properties);
	}

	/**
	 * Get the components nested within this component.
	 * Suitable components defined in RFC 5545
	 * consist of
	 * <A HREF="https://tools.ietf.org/html/rfc5545#section-3.6.6">
	 * alarms</A>.
	 * @return a list of the nested components
	 */
	public List<ICalComponent> getComponents() {
	    return Collections.unmodifiableList(children);
	}
    }
}

//  LocalWords:  exbundle iCalendars CALSCALE PRODID TRANSP TZID UID
//  LocalWords:  TZNAME UTF CRLF HREF iCalendar IOException
//  LocalWords:  iCalendar's VCALENDAR
