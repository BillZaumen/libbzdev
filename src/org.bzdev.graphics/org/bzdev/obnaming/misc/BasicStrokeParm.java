package org.bzdev.obnaming.misc;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import java.awt.BasicStroke;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

/**
 * Obnaming factory support class for java.awt.BasicStroke.
 * <P>
 * Parameter names will be compound names, typically using a period as
 * a separator. When this class is used, multiple parameter-name
 * components will be defined, and the last component of the parameter
 * name will be the following:
 * <ul>
 *   <li> "cap" - an enumeration constant specified by
 *        the enumeration
 *   {@link org.bzdev.obnaming.misc.BasicStrokeParm.Cap BasicStrokeParm.Cap}
 *        whose values are <CODE>BUTT</CODE>, <CODE>ROUND</CODE>, and
 *        <CODE>SQUARE</CODE>
 *   <li> "join" - an enumeration constant specified by
 *        the enumeration
 *   {@link org.bzdev.obnaming.misc.BasicStrokeParm.Join BasicStrokeParm.Join}
 *        whose values are <CODE>BEVEL</CODE>, <CODE>MITER</CODE>, and
 *        <CODE>ROUND</CODE>.
 *   <li> "width" - the line width.
 *   <li> "miterLimit" - the limit such that a line join is trimmed
 *        when the ratio of miter length to stroke width is
 *        greater than this value. The miter length is the
 *        diagonal length of the miter, which is the distance
 *        between the inside corner and the outside corner of
 *        the intersection. The smaller the angle formed by two
 *        line segments, the longer the miter length and the
 *        sharper the angle of intersection. The default
 *        miterlimit value of 10.0 causes all angles less than
 *        11 degrees to be trimmed. Trimming miters converts
 *        the decoration of the line join to a bevel. This value
 *        applies only to a line join that has a MITER join
 *        decoration and must be larger than or equal to 1.0
 *   <li> "dashPhase" - the offset to start the dashing pattern.
 *   <li> "dashIncrement" - the length
 *        corresponding to a space ' ' or minus sign '-' in a dash
 *        pattern.
 *   <li> "dashPattern" - the dash pattern, represented by a
 *        string. If the string has a length of zero, the line
 *        is solid. Otherwise the pattern should be represented
 *        by a sequence of "-" or " " giving the length of
 *        dashes or empty space respectively as multiples of
 *        the value of dashIncrement.  Each "-" and each " "
 *        represents a component of a segment whose length is
 *        dashIncrement.  Thus, if dashIncrement is 10.0, then
 *        the pattern "-- - " will consist of a dash of length
 *        20.0, a space of length 10.0, a dash of length 10.0,
 *        and a space of length 10.0, with the pattern
 *        repeating as needed. Unless the string is empty, it must
 *        start with a "-". Note that a dash phase can be used to
 *        make it look like the pattern started with a blank: spaces
 *        may appear at the end of the string.
 *   <li> "gcsMode" - a boolean that, when true, indicates that
 *        the width, dashPhase, and dashIncrement are provided in
 *        graph coordinate space, not user space. The default is
 *        'false'.
 * </ul>
 */
@CompoundParmType(tipResourceBundle = "*.lpack.BasicStrokeParmTips",
		  labelResourceBundle = "*.lpack.BasicStrokeParmLabels",
		 docResourceBundle = "*.lpack.BasicStrokeParmDocs")
public class BasicStrokeParm {

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return a stroke whose width is zero.
     */
    public static final class ZERO extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public ZERO() {
	    super(0.0);
	}
    }

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return null (for a zero-width stroke).
     */
    public static final class NULL_WHEN_ZERO extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public NULL_WHEN_ZERO() {
	    super(true);
	}
    }

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return a stroke whose width is 1.0. (1.0 is the
     * default width for {@link BasicStrokeParm} itself, but
     * creating a subclass improves code readability.
     */
    public static final class ONE extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public ONE() {
	    super(1.0);
	}
    }

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return a stroke whose width is 2.0.
     */
    public static final class TWO extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public TWO() {
	    super(2.0);
	}
    }

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return a stroke whose width is 3.0.
     */
    public static final class THREE extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public THREE() {
	    super(3.0);
	}
    }

    /**
     * A BasicStrokeParm configured so that, unless the stroke
     * is modified, {@link BasicStrokeParm#createBasicStroke()}
     * will return a stroke whose width is 4.0.
     */
    public static final class FOUR extends BasicStrokeParm {
	/**
	 * Constructor.
	 */
	public FOUR() {
	    super(3.0);
	}
    }

    private boolean nullFlag = false;

    /**
     * Constructor.
     * The default stroke width is 1.0.
     */
    public BasicStrokeParm(){}

    /**
     * Construct specifying handling of zero width strokes.
     * @param zeroWidth true if the width should be zero and if
     *        {@link #createBasicStroke()} should return null for
     *        zero-width strokes; false otherwise, in which case
     *        the width will have an initial value of 1.0
     */
    public BasicStrokeParm(boolean zeroWidth) {
	this();
	nullFlag = zeroWidth;
	if (zeroWidth) {
	    this.width = 0.0;
	}
    }


    /**
     * Constructor given a line width.
     * @param width the line width
     */
    public BasicStrokeParm(double width) {
	this.width = width;
    }

    /**
     * End cap class.
     * This provides an enum corresponding to integer-valued
     * constants defined by java.awt.BasicStroke. The constants
     * define the type of decoration at the end of a line.
     */
    public static enum Cap {
	/**
	 * Ends enclosed subpaths and dash segments with no added decoration.
	 */
	BUTT(BasicStroke.CAP_BUTT),

	/**
	 * Dash segments and enclosed subpaths end with a round
	 * decoration that has a radius equal to half the width of the pen.
	 */
	ROUND(BasicStroke.CAP_ROUND),

	/**
	 * Ends enclosed subpaths and dash segments with a square
	 * projection that extends beyond the end of the segment by a
	 * distance equal to half of the line width.
	 */
	SQUARE(BasicStroke.CAP_SQUARE);

	private int value;
	private Cap(int value) {this.value = value;}
	/**
	 * Get the integer constant matching a Cap enum constant.
	 * @return the integer constant defined by
	 * {@link java.awt.BasicStroke BasicStroke}
	 */
	public int getValue() {
	    return value;
	}
    }

    /**
     * Specification for how lines or line segments are capped.
     * @see org.bzdev.obnaming.misc.BasicStrokeParm.Cap
     */
    @PrimitiveParm("cap")
    public Cap cap = Cap.SQUARE;

    /**
     * Class specifying enumation constants defining how lines are joined.
     */
    public static enum Join {
	/**
	 * Joins path segments by connecting the outer corners of their
	 * wide outlines with a straight segment.
	 */
	BEVEL(BasicStroke.JOIN_BEVEL),

	/**
	 * Joins path segments by extending their outside edges until they meet.
	 */
	MITER(BasicStroke.JOIN_MITER),

	/**
	 * Joins path segments by rounding off the corner at a radius of
	 * half the line width.
	 */
	ROUND(BasicStroke.JOIN_ROUND);

	private int value;
	private Join(int value) {this.value = value;}
	/**
	 * Get the integer constant matching a Join enum constant.
	 * @return the integer constant defined by
	 * {@link java.awt.BasicStroke BasicStroke}
	 */
	public int getValue() {
	    return value;
	}
    }
    
    /**
     * The line width.
     */
    @PrimitiveParm("width")
    public double width = 1.0;

    /**
     * Specification for how lines or line segments are joined.
     * @see org.bzdev.obnaming.misc.BasicStrokeParm.Join
     */
    @PrimitiveParm("join")
    public Join join = Join.MITER;

    /**
     * The limit so that line joins are trimmed when the ratio of miter
     * length to stroke width is greater than this
     * value. The miter length is the diagonal length of the miter,
     * which is the distance between the inside corner and the outside
     * corner of the intersection. The smaller the angle formed by two
     * line segments, the longer the miter length and the sharper the
     * angle of intersection. The default miterlimit value of 10.0
     * causes all angles less than 11 degrees to be trimmed. Trimming
     * miters converts the decoration of the line join to a bevel. This
     * values applies only to a line join that has a MITER join decoration.
     * The miter limit, if provided, must have a value no smaller than or
     * equal to 1.0.
     * @see java.awt.BasicStroke
     */
    @PrimitiveParm(value="miterLimit", lowerBound="1.0", lowerBoundClosed=true)
    public double miterLimit = 10.0;

    /**
     * The offset to start the dashing pattern.
     */
    @PrimitiveParm("dashPhase")
    public double dashPhase = 0.0;
    
    /**
     *The length corresponding to a "-" or " " in a dashPattern.
     */
    @PrimitiveParm(value="dashIncrement",lowerBound="0.0",
		   lowerBoundClosed=false)
    public double dashIncrement = 10.0;

    /**
     * The dash pattern.
     * If null or of zero length, the line is solid. Otherwise the
     * pattern should be represented by a sequence of "-" or " " giving
     * the length of dashes or empty space respectively as multiples of
     * the value of dashIncrement.  Each "<code>-</code>" and each
     * "<code>&nbsp;</code>" represents a
     * component of a segment whose length is dashIncrement. Unless
     * the pattern is empty or all dashes, the pattern must start with
     * a dash and end with a space.
     * Thus, if dashIncrement is 10.0, then the pattern
     * "<code>--&nbsp;&nbsp;-&nbsp;</code>" will
     * consist of a dash of length 20.0, a space of length 20.0, a dash
     * of length 10.0, and a space of length 10.0, with the pattern
     * repeating as needed.
     */
    @PrimitiveParm("dashPattern")
    public String dashPattern = "";

    /**
     * Flag to indicate if widths or lengths are provided in
     * graph-coordinate-space units.
     * If true, the stroke created should be used for a graphic context
     * that includes a transform from graph coordinate space to user
     * space.
     */
    @PrimitiveParm("gcsMode")
    public boolean gcsMode = false;

    /**
     * Indicate if widths or lengths are provided in
     * graph-coordinate-space units.
     * If true, the stroke created should be used for a graphic context
     * that includes a transform from graph coordinate space to user
     * space.
     * @return true if the basic stroke created is intended for graph
     *         coordinate space; false if it is intended for user space
     */
    public boolean getGcsMode() {return gcsMode;}

    static boolean dashOnly(String pattern) {
	int len = pattern.length();
	for (int i = 0; i < len; i++) {
	    if (pattern.charAt(i) != '-') {
		return false;
	    }
	}
	return true;
    }

    /**
     * Get an array that represents a dash pattern.
     * A dash pattern consists of a string whose characters are either
     * a minus sign ("-") or a space ("&nbsp;"), starting with a "-".
     * An empty string, a string of zero length, or a string containing
     * only "-" characters is ignored and a null value will be returned.
     * Otherwise a dash pattern must start with a '-' and end with a
     * space. A pattern ending with a '-' is not necessary as the
     * dashPhase parameter can be used to create the same effect.
     * A sequence of "-" characters represents a dash whose length is
     * the number of characters in that sequence multiplied by the
     * dash increment.  Similarly, a sequence of "&nbsp;" characters
     * represents a gap whose length is the number of characters in
     * that sequence multiplied by the dash increment.  The array
     * returned provides the pattern of alternating opaque and
     * transparent segments used by classes such as
     * {@link java.awt.BasicStroke}.
     * @param dashPattern the dash pattern
     * @param dashIncrement the length corresponding to a character in
     *        a dash pattern
     * @exception IllegalArgumentException the dashInrement argument was
     *        not positive or the dashPattern argument was illegal
     */
    public static float[] getDashArray(String dashPattern, double dashIncrement)
	throws IllegalArgumentException
    {
	if (dashIncrement < 0) {
	    throw new IllegalArgumentException("dashIncrement negative");
	}
	if (dashPattern == null || dashPattern.length() == 0) {
	    return null;
	} else if (dashOnly(dashPattern)) {
	    return null;
	}
	Pattern p = Pattern.compile("(-+)|( +)");
	Matcher m = p.matcher(dashPattern);
	int index = 0;
	ArrayList<Integer> al = new ArrayList<>();
	while (m.find()) {
	    int start = m.start();
	    int len = m.end() - start;
	    if (dashPattern.charAt(start) == '-' && index%2 == 1) {
		throw new IllegalArgumentException
		    ("tried to create a BasicStroke using an illegal "
		     + "dash pattern");
	    }
	    al.add(len);
	    index++;
	}
	int n = al.size();
	if (n%2 != 0) {
	    throw new IllegalArgumentException
		("tried to create a BasicStroke using an illegal "
		 + "dash pattern");
	}
	float[] dash = new float[n];
	index = 0;
	for (Integer d: al) {
	    int dval = d;
	    dash[index++] = (float)(dval * dashIncrement);
	}
	return dash;
    }


    /**
     * Create a basic stroke.
     * The basic stroke will use the parameters defined by the
     * fields {@link #width width}, {@link #cap cap}, {@link #join join},
     * {@link #miterLimit miterLimit}, {@link #dashPhase dashPhase},
     * {@link #dashIncrement dashIncrement}, and
     * {@link #dashPattern dashPattern}.
     * @return the stroke; null if the width is 0.0 and this class is
     *         configured to return null in that case
     */
    public BasicStroke createBasicStroke() {
	if (nullFlag && width == 0.0) return null;
	if (dashPattern == null || dashPattern.length() == 0) {
	    return new BasicStroke((float)width, join.getValue(),
				   cap.getValue(), (float)miterLimit);
	} else if (dashOnly(dashPattern)) {
	    return new BasicStroke((float)width, join.getValue(),
				   cap.getValue(), (float)miterLimit);
	} else {
	    Pattern p = Pattern.compile("(-+)|( +)");
	    Matcher m = p.matcher(dashPattern);
	    int index = 0;
	    ArrayList<Integer> al = new ArrayList<>();
	    while (m.find()) {
		int start = m.start();
		int len = m.end() - start;
		if (dashPattern.charAt(start) == '-' && index%2 == 1) {
		    throw new IllegalStateException
			("tried to create a BasicStroke using an illegal "
			 + "dash pattern");
		}
		al.add(len);
		index++;
	    }
	    int n = al.size();
	    if (n%2 != 0) {
		    throw new IllegalStateException
			("tried to create a BasicStroke using an illegal "
			 + "dash pattern");
	    }
	    float[] dash = new float[n];
	    index = 0;
	    for (Integer d: al) {
		int dval = d;
		dash[index++] = (float)(dval * dashIncrement);
	    }
	    return new BasicStroke((float)width, join.getValue(),
				   cap.getValue(), (float)miterLimit,
				   dash, (float)dashPhase);
	}
    }
}

//  LocalWords:  Obnaming ul li BasicStrokeParm miterLimit miterlimit
//  LocalWords:  dashPhase dashIncrement dashPattern gcsMode boolean
//  LocalWords:  createBasicStroke zeroWidth enum subpaths enumation
//  LocalWords:  BasicStroke nbsp IllegalArgumentException
//  LocalWords:  dashInrement
