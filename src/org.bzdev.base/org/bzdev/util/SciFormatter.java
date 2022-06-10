package org.bzdev.util;
import org.bzdev.lang.MathOps;
import java.lang.reflect.Array;
import java.util.regex.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.Formattable;
import static java.util.FormattableFlags.*;
import java.util.Locale;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;
import java.io.Closeable;
import java.io.Flushable;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * An interpreter for printf-style format strings that can use standard
 * scientific notation for real numbers.
 * This class is nearly identical to the class
 * {@link java.util.Formatter java.util.Formatter}. The main difference is
 * that the 'e', 'E', 'g', and 'G' conversions will accept the '#' flag
 * indicating an alternative representation that replaces 'e' or 'E' with
 * a multiplication sign, followed by 10, followed by the exponent displayed
 * using superscripts. For the exponent, a leading '+' and leading '0'
 * characters are not shown.
 * <P>
 * When the format is 'E' (as opposed to 'e') and the "#' flag is
 * used, the number before the multiplication sign and the
 * multiplication sign are dropped when when the value is 10 raised to
 * an integer exponent. There are a few exceptions: the number -10, 1,
 * 0, and 10 are shown as integers.  The rationale is that some
 * journals have standards for graphs where the multiplication sign is
 * not allowed for such numbers when they are used for labeling an
 * axis on a graph.
 * <P>
 * For example, 
 * <blockquote><code>
 * format("%3.3g %#3.3g"  20000.0, 20000.0)
 * </code></blockquote>
 * will produce the output
 * <blockquote> <code>
 *  2.000e+04  2.000&times;10<sup>4</sup>
 * </code></blockquote>
 * <P>
 * The class Formatter (as tested using Java 1.7) behaves erratically when
 * only some of the conversions specify an argument position. Thus
 * <blockquote><code>
 * formatter = new Formatter();
 * formatter.format("1 2 3 = %3$d %2$d %1$d", 3, 2, 1);
 * System.out.println(formatter.toString());
 * formatter = new Formatter();
 * formatter.format("1 2 3 = %3$d %d %1$d", 3, 2, 1);
 * System.out.println(formatter.toString());
 * </code></blockquote>
 * will produce
 * <blockquote><code>
 * 1 2 3 = 1 2 3
 * 1 2 3 = 1 3 3
 * </code></blockquote>
 * even though the %d argument is the second conversion.  SciFormatter
 * handles both cases identically:
 * <blockquote><code>
 * SciFormatter formatter = new SciFormatter();
 * formatter.format("1 2 3 = %3$d %2$d %1$d", 3, 2, 1);
 * System.out.println(formatter.toString());
 * formatter = new SciFormatter();
 * formatter.format("1 2 3 = %3$d %d %1$d", 3, 2, 1);
 * System.out.println(formatter.toString());
 * </code></blockquote>
 * will produce
 * <blockquote><code>
 * 1 2 3 = 1 2 3
 * 1 2 3 = 1 2 3
 * </code></blockquote>
 * <P>
 * The class SciFormatter does not extend Formatter because Formatter is
 * a final class.  Aside from the use of '#' in the 'g', 'G', 'e', and 'E'
 * conversions, the format string is identical to that used by Formatter
 * and the constructors and methods of SciFormatter have the same signatures
 * as those for Formatter.
 * <P>
 * The method and constructor documentation has been copied verbatim from
 * the Open JDK 1.7 documentation as we cannot use inheritance to find it.
 * A series of methods named <code>format</code> have been added to explicitly
 * handle up to 11 arguments. These methods are provided for use with
 * scripting languages that do not recognize Java methods that use a
 * variable number of arguments.
 * <P>
 * For the 'e', 'E', 'g', and 'G' formats with the '#' flag, the
 * Unicode characters used in addition to the ones in the ASCII
 * character set are the following:
 * <UL>
 *   <LI> '\u2070' - 0 as a superscript.
 *   <LI> '\u00B9' - 1 as a superscript.
 *   <LI> '\u00B2' - 2 as a superscript.
 *   <LI> '\u00B3' - 3 as a superscript.
 *   <LI> '\u2074' - 4 as a superscript.
 *   <LI> '\u2075' - 5 as a superscript.
 *   <LI> '\u2076' - 6 as a superscript.
 *   <LI> '\u2077' - 7 as a superscript.
 *   <LI> '\u2078' - 8 as a superscript.
 *   <LI> '\u2079' - 9 as a superscript.
 *   <LI> '\u207B' - a minus sign as a superscript
 *   <LI> '\u00D7' - a multiplication sign
 * </UL>
 * <P>
 * Note: for Java 7, Formatter has a bug that prevents formats
 * such as "%3.1g" or "%3.1e" from working when formatting the
 * number 0.0 - an exception is
 * thrown.  This bug seems to have been fixed in Java 8. SciFormatter
 * has a work around that handles this specific case for Java 7.
 * If System.getProperties("java.version") does not return a string
 * starting with "1.7", the work around will not be used.
 * @see java.util.Formatter
 */
public final class SciFormatter implements Closeable, Flushable {
    Formatter formatter;
    Object[] emptyArray = new Object[0];

    boolean nonstrict = true;

    /**
     * Set strict mode.
     *  When strict mode is turned on (it is off by default), an error
     *  will occur when a format directive expects an integer but the
     *  value's type is Double.  When strict mode is turned off, an
     *  error will occur when a format directive expects an integer
     *  and is given a Double whose value is not one that one expects
     *  for an integer.
     * @param value true if strict mode is to be turned on; false to turn
     *        it off.
     */
    public void setStrictMode(boolean value) {
	nonstrict = !value;
    }

    /**
     * Get strict mode.
     *  When strict mode is turned on (it is off by default), an error
     *  will occur when a format directive expects an integer but the
     *  value's type is Double.  When strict mode is turned off, an
     *  error will occur when a format directive expects an integer
     *  and is given a Double whose value is not one that one expects
     *  for an integer.
     * @return true if strict mode is on; false if it is off.
     */
    public boolean getStrictMode() {return !nonstrict;}



    /**
     * Constructs a new formatter.
     *
     * <p> The destination of the formatted output is a {@link StringBuilder}
     * which may be retrieved by invoking {@link #out out()} and whose
     * current content may be converted into a string by invoking {@link
     * #toString toString()}.  The locale used is the {@linkplain
     * java.util.Locale#getDefault() default locale} for this instance of the Java
     * virtual machine.
     */
    public SciFormatter() {
	formatter = new Formatter();
    }
   /**
     * Constructs a new formatter with the specified destination.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault() default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  a
     *         Destination for the formatted output.  If {@code a} is
     *         {@code null} then a {@link StringBuilder} will be created.
     */
    public SciFormatter(Appendable a) {
	formatter = new Formatter(a);
    }
    /**
     * Constructs a new formatter with the specified destination and locale.
     *
     * @param  a
     *         Destination for the formatted output.  If {@code a} is
     *         {@code null} then a {@link StringBuilder} will be created.
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     */
    public SciFormatter(Appendable a, Locale l) {
	formatter = new Formatter(a, l);
    }

    /**
     * Constructs a new formatter with the specified file.
     *
     * <p> The charset used is the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault() default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  file
     *         The file to use as the destination of this formatter.  If the
     *         file exists then it will be truncated to zero size; otherwise,
     *         a new file will be created.  The output will be written to the
     *         file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     */
    public SciFormatter(File file)  throws FileNotFoundException {
	formatter = new Formatter(file);
    }

    /**
     * Constructs a new formatter with the specified file and charset.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  file
     *         The file to use as the destination of this formatter.  If the
     *         file exists then it will be truncated to zero size; otherwise,
     *         a new file will be created.  The output will be written to the
     *         file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(File file, String csn)
	throws FileNotFoundException, UnsupportedEncodingException
    {
	formatter = new Formatter(file, csn);
    }

    /**
     * Constructs a new formatter with the specified file, charset, and
     * locale.
     *
     * @param  file
     *         The file to use as the destination of this formatter.  If the
     *         file exists then it will be truncated to zero size; otherwise,
     *         a new file will be created.  The output will be written to the
     *         file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(File file, String csn, Locale l)
	throws FileNotFoundException, UnsupportedEncodingException
    {
	formatter = new Formatter(file, csn, l);
    }

    /**
     * Constructs a new formatter with the specified locale.
     *
     * <p> The destination of the formatted output is a {@link StringBuilder}
     * which may be retrieved by invoking {@link #out out()} and whose current
     * content may be converted into a string by invoking {@link #toString
     * toString()}.
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     */
    public SciFormatter(Locale l) {
	formatter = new Formatter(l);
    }

    /**
     * Constructs a new formatter with the specified output stream.
     *
     * <p> The charset used is the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault() default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  os
     *         The output stream to use as the destination of this formatter.
     *         The output will be buffered.
     */
    public SciFormatter(OutputStream os) {
	formatter = new Formatter(os);
    }

    /**
     * Constructs a new formatter with the specified output stream and
     * charset.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  os
     *         The output stream to use as the destination of this formatter.
     *         The output will be buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(OutputStream os, String csn)
	throws UnsupportedEncodingException
    {
	formatter = new Formatter(os, csn);
    }

    /**
     * Constructs a new formatter with the specified output stream, charset,
     * and locale.
     *
     * @param  os
     *         The output stream to use as the destination of this formatter.
     *         The output will be buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(OutputStream os, String csn, Locale l)
	throws UnsupportedEncodingException
    {
	formatter = new Formatter(os, csn, l);
    }

    /**
     * Constructs a new formatter with the specified print stream.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault() default
     * locale} for this instance of the Java virtual machine.
     *
     * <p> Characters are written to the given {@link java.io.PrintStream
     * PrintStream} object and are therefore encoded using that object's
     * charset.
     *
     * @param  ps
     *         The stream to use as the destination of this formatter.
     */
    public SciFormatter(PrintStream ps) {
	formatter = new Formatter(ps);
    }

    /**
     * Constructs a new formatter with the specified file name.
     *
     * <p> The charset used is the {@linkplain
     * java.nio.charset.Charset#defaultCharset() default charset} for this
     * instance of the Java virtual machine.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault() default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this
     *         formatter.  If the file exists then it will be truncated to
     *         zero size; otherwise, a new file will be created.  The output
     *         will be written to the file and is buffered.
     *
     * @throws  FileNotFoundException
     *          If the given file name does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     */
    public SciFormatter(String fileName) throws FileNotFoundException {
	formatter = new Formatter(fileName);
    }

    /**
     * Constructs a new formatter with the specified file name and charset.
     *
     * <p> The locale used is the {@linkplain java.util.Locale#getDefault default
     * locale} for this instance of the Java virtual machine.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this
     *         formatter.  If the file exists then it will be truncated to
     *         zero size; otherwise, a new file will be created.  The output
     *         will be written to the file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @throws  FileNotFoundException
     *          If the given file name does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(String fileName, String csn)
	throws FileNotFoundException, UnsupportedEncodingException
    {
	formatter = new Formatter(fileName, csn);
    }

    /**
     * Constructs a new formatter with the specified file name, charset, and
     * locale.
     *
     * @param  fileName
     *         The name of the file to use as the destination of this
     *         formatter.  If the file exists then it will be truncated to
     *         zero size; otherwise, a new file will be created.  The output
     *         will be written to the file and is buffered.
     *
     * @param  csn
     *         The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.
     *
     * @throws  FileNotFoundException
     *          If the given file name does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public SciFormatter(String fileName, String csn, Locale l)
	throws FileNotFoundException, UnsupportedEncodingException
    {
	formatter = new Formatter(fileName, csn, l);
    }

    // Used to handle a case for Java 7 where Formatter fails.
    // The number formatted will be numerically equal to zero.
    // Unfortunately, if formatter is an instance of java.util.Formatter,
    // formatter.format(locale, "%5.3g", 0.0) will throw an exception
    // due to an ArrayIndexOutOfBoundsException. The format string
    // "%5.3f" works as expected.
    static class OurZeroDouble implements Formattable {
	boolean gformat;
	Locale locale;
	OurZeroDouble(Locale locale, boolean gformat) {
	    this.locale = locale;
	    this.gformat = gformat;
	}

	public void formatTo(Formatter fmt, int f, int width, int precision) {
	    boolean upperCase = (f & UPPERCASE) == UPPERCASE;
	    boolean left = (f & LEFT_JUSTIFY) == LEFT_JUSTIFY;

	    String format = (precision == -1)? "%f":
		String.format((Locale)null, "%%.%df", precision);
	    String string = String.format(format, 0.0);
	    if (gformat == false) {
		string = string + (upperCase? "E+00": "e+00");
	    }
	    if (width == -1) {
		format = "%s";
	    } else if (left) {
		format = String.format((Locale)null, "%%-%ds", width);
	    } else {
		format = String.format((Locale)null, "%%%ds", width);
	    }
	    fmt.format(locale, format, string);
	    return;
	}
    }


    // We can't subclass Double to create a Formattable version
    // because Double is a final class. All instances of this are
    // additional arguments tagged onto the end of the call to
    // format.
    static class OurDouble implements Formattable {
	Double x;
	Locale locale;
	OurDouble(Locale l, double x) {
	    this.locale = l;
	    this.x = Double.valueOf(x);
	}

	static final char table[] = {
	    '\u2070', // 0
	    '\u00B9', // 1
	    '\u00B2', // 2
	    '\u00B3', // 3
	    '\u2074', // 4
	    '\u2075', // 5
	    '\u2076', // 6
	    '\u2077', // 7
	    '\u2078', // 8
	    '\u2079'  // 9
	};

	static double[] powersOfTen = null;
	static {
	    ArrayList<Double> list = new ArrayList<>(2*310 + 4);
	    BigDecimal x = BigDecimal.TEN.negate();
	    list.add(0.0);
	    list.add(-1.0);
	    for (;;) {
		double value = x.doubleValue();
		if (Double.isInfinite(value)) {
		    break;
		} else {
		    list.add(value);
		}
		x = x.multiply(BigDecimal.TEN);
	    }
	    Collections.reverse(list);
	    list.add(1.0);
	    x = BigDecimal.TEN;
	    for (;;) {
		double value = x.doubleValue();
		if (Double.isInfinite(value)) {
		    break;
		} else {
		    list.add(value);
		}
		x = x.multiply(BigDecimal.TEN);
	    }
	    powersOfTen = new double[list.size()];
	    int i = 0;
	    for (Double value: list) {
		powersOfTen[i++] = value;
	    }
	    list = null;
	}

	public void formatTo(Formatter fmt, int f, int width, int precision) {
	    boolean useG = (f & ALTERNATE) == ALTERNATE;
	    // this uses a trick. This method is used only when the original
	    // format string used a '#' so we use that flag to encode whether
	    // we are processing  a 'g' or e 'e' directive.
	    boolean upperCase = (f & UPPERCASE) == UPPERCASE;
	    boolean left = (f & LEFT_JUSTIFY) == LEFT_JUSTIFY;
	    String sformat = (width == -1)? "%s":
		String.format((Locale) null,
			      "%%%s%ds", (left?"-":""), width);
	    if (upperCase && !useG) {
		double limit = MathOps.pow(10.0, -precision);
		if (limit > 1.e-9) limit = 1.e-9;
		// the formatting directive was 'E'.  For this case, a
		// few cases are handled specially: the numbers
		// -10, 1, 0, and 10 are not formatted and any number whose
		// absolute value is a power of 10 provided as simply a
		// power of ten with a sign.
		if (Math.abs((x + 10.0)/10.0) < limit) {
		    fmt.format(sformat, "-10");
		    return;
		} else if (Math.abs(x  + 1.0) < limit) {
		    fmt.format(sformat, "-1");
		    return;
		} else if (Math.abs(x) < limit) {
		    fmt.format(sformat, "0");
		    return;
		} else if (Math.abs(x - 1.0) < limit) {
		    fmt.format(sformat, "1");
		    return;
		} else if (Math.abs((x - 10.0)/10.0) < limit) {
		    fmt.format(sformat, "10");
		    return;
		}
		int index = Arrays.binarySearch(powersOfTen, x);
		if (index < 0) {
		    int index2 = -index - 1;
		    int index1 = index2 - 1;
		    boolean ok = false;
		    if ((index1 >= 0)
			&& (Math.abs((x - powersOfTen[index1])/x) < limit)) {
			ok = true;
			index = index1;
		    } else if ((index2 >= 0)
			       && (Math.abs((x-powersOfTen[index2])/x)<limit)) {
			ok = true;
			index = index2;
		    }
		    if (ok) {
			String result = (x > 0)? "10": "-10";
			int exponent = Math.abs(index)
			    - ((powersOfTen.length - 1) / 2);
			String exponentString = "" + exponent;
			for (char ch: exponentString.toCharArray()) {
			    if (ch == '-') {
				result = result + "\u207B";
			    } else {
				int d = Character.digit(ch, 10);
				result = result + table[d];
			    }
			}
			fmt.format(sformat, result);
			return;
		    }
		} else {
		    String result = (x > 0)? "10": "-10";
		    int exponent = Math.abs(index)
			- ((powersOfTen.length - 1) / 2);
		    String exponentString = "" + exponent;
		    for (char ch: exponentString.toCharArray()) {
			if (ch == '-') {
			    result = result + "\u207B";
			} else {
			    int d = Character.digit(ch, 10);
			    result = result + table[d];
			}
		    }
		    fmt.format(sformat, result);
		    return;
		}
	    }

	    Formatter formatter = new Formatter();
	    String format;
	    if (SciFormatter.USING_JAVA7 && x == 0.0
		&& (width != -1 || precision != -1)) {
		// %e or %g does not work when the argument is 0
		// when there is a field or precision.  We use
		// the matching %f format instead and fix it up as
		// needed.
		int newwidth;
		if (useG) {
		    newwidth = width;
		} else {
		    if (width == -1) newwidth = -1;
		    if (width > 4) newwidth = width - 4;
		    else newwidth = 1;
		}
		format = "%" + ((precision == -1)? "": "." + precision)
		    + (useG? "f": "fe+00");
	    } else {
		format = "%" + ((precision == -1)? "": "." + precision)
		    + (useG? "g": "e");
	    }
	    String result = formatter.format(locale, format, x).toString();

	    StringBuilder sb = new StringBuilder(result);
	    int start = result.indexOf("e");
	    if (start == -1) {
		start = result.indexOf("E");
	    }
	    if (start > -1) {
		sb.deleteCharAt(start);
		sb.insert(start, "\u00D710");
		int index = start + 3;
		int len = sb.length();
		boolean initialzeros = true;
		while ( index < len) {
		    char ch = sb.charAt(index);
		    if (ch == '+') {
			sb.deleteCharAt(index);
			index--;
			len--;
		    } else if (ch == '-') {
			sb.setCharAt(index, '\u207B');
		    } else if (Character.isDigit(ch)) {
			int value = Character.digit(ch, 10);
			if (initialzeros && value == 0) {
			    sb.deleteCharAt(index);
			    index--;
			    len--;
			} else {
			    sb.setCharAt(index, table[value]);
			    initialzeros = false;
			}
		    }
		    index++;
		}
		if (initialzeros) {
		    sb.deleteCharAt(start);
		    sb.deleteCharAt(start);
		    sb.deleteCharAt(start);
		}
	    }
	    // we know this is safe because no '%' characters will appear
	    // in a formatted number.
	    fmt.format(sformat, sb.toString());
	}
    }

    /**
     * Closes this formatter.  If the destination implements the {@link
     * java.io.Closeable} interface, its {@code close} method will be invoked.
     *
     * <p> Closing a formatter allows it to release resources it may be holding
     * (such as open files).  If the formatter is already closed, then invoking
     * this method has no effect.
     *
     * <p> Attempting to invoke any methods except {@link #ioException()} in
     * this formatter after it has been closed will result in a {@link
     * java.util.FormatterClosedException}.
     */
    public void close() {
	formatter.close();
    }

    /**
     * Flushes this formatter.  If the destination implements the {@link
     * java.io.Flushable} interface, its {@code flush} method will be invoked.
     *
     * <p> Flushing a formatter writes any buffered output in the destination
     * to the underlying stream.
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     */
    public void flush() {
	formatter.flush();
    }
    
    /**
     * Returns the locale set by the construction of this formatter.
     *
     * <p> The {@link #format(java.util.Locale,String,Object...) format} method
     * for this object which has a locale argument does not change this value.
     *
     * @return  {@code null} if no localization is applied, otherwise a
     *          locale
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     */
    public Locale locale() {
	return formatter.locale();
    }

    static boolean USING_JAVA7 = false;
    static {
	String property;
	try {
	    property =
		AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
			    return System.getProperty("java.version");
			}
		    });
	} catch (Exception e) {
	    // if we can't read the property, pick one that will
	    // cause USING_JAVA7 to be false. The USING_JAVA7 field
	    // is there for a workaround due to a Java 7 bug in
	    // the class java.util.Formatter.
	    property = "1.8";
	}
	int index = property.indexOf(".");
	if (index < 0) {
	    // unknown
	    USING_JAVA7 = false;
	} else {
	    property = property.substring(index+1);
	}
	index = 0;
	for(;;) {
	    char ch = property.charAt(index);
	    if (ch < '0' || ch > '9') break;
	    index++;
	}
	String version = property.substring(0,index);
	if (version.equals("7")) {
	    USING_JAVA7 = true;
	}
    }


    static Pattern p = 
	Pattern.compile("((?:%%)*)(%)((?:\\d+[$])?)([-#+0,( <]*)"
			+ "(\\d*(?:[.]\\d*)?)([a-zA-Z])");

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string, and arguments.
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.  This does not change this object's locale that was
     *         set during construction.
     *
     * @param  format
     *         A format string as described in 
     *         {@link java.util.Formatter Formatter}
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     *
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format, Object... args)
	throws IllegalFormatException, FormatterClosedException
    {
	Matcher matcher = p.matcher(format);
	StringBuffer sb = new StringBuffer();
	int[] index = new int[args.length];  // modified objects
	int[] oindex = new int[args.length]; // original objects.
	int[] findex = new int[args.length]; // java 7 fix
	int indexCount =  0;
	Arrays.fill(index, -1);
	Arrays.fill(oindex, -1);
	Arrays.fill(findex, -1);
	ArrayList<Object> alist = new ArrayList<>(2*args.length);
	int tail = 0;

	int argIndex = 0;
	boolean needMoreArgs = false;
	int ind = 0;
	while (matcher.find()) {
	    int itest = matcher.start() - 1;
	    if (itest >= 0) {
		if (format.charAt(itest) == '%') {
		    // The pattern matches an odd number of % characters
		    // in a row followed by the rest of the pattern.
		    // If there is a '%' directly before the
		    // match, the total number of '%' characters in a row
		    // is even, which indicates a string of '%' characters
		    // in the output instead of a formatting directive.
		    continue;
		}
	    }
	    int n = matcher.groupCount();
	    if (n == 6) {
		String s1 = matcher.group(1);
		String s2 = matcher.group(2);
		String s3 = matcher.group(3);
		String s4 = matcher.group(4);
		String s5 = matcher.group(5);
		String s6 = matcher.group(6);
		char conversion = s6.charAt(0);
		if (s4.indexOf('<') == -1) {
		    if(s3.length() != 0) {
			argIndex = Integer.parseInt
			    (s3.substring(0, s3.length()-1))-1;
		    } else {
			argIndex = ind;
		    }
		} else if (s3.length() == 0) {
		    s3 = (argIndex + 1) + "$";
		}
		s4 = s4.replace("<", "");
		if (s4.indexOf('#') != -1) {
		    // s4 = s4.replace("#", "");
		    switch(conversion) {
		    case 'e':
		    case 'E':
			s4 = s4.replace("#", "");
			// fall through
		    case 'g':
		    case 'G':
			if (args[argIndex] instanceof Number) {
			    if (index[argIndex] == -1) {
				index[argIndex] = tail;
				alist.add(new
					  OurDouble(l,
						    ((Number) args[argIndex])
						    .doubleValue()));
				tail++;
			    }
			    /*
			    newargs[index[argIndex]] =
				new OurDouble(l, (Double) args[argIndex]);
			    */
			    s3 = (index[argIndex] + 1) + "$";
			    s6 = Character.isUpperCase(conversion)? "S": "s";
			} else {
			    if (oindex[argIndex] == -1) {
				oindex[argIndex] = tail;
				alist.add(args[argIndex]);
				tail++;
			    }
			    s3 = (oindex[argIndex] + 1) + "$";
			    // s3 = (argIndex + 1) + "$";
			    // newargs[index[argIndex]] = args[argIndex];
			}
			break;
		    case 'd':
		    case 'o':
		    case 'x':
		    case 'X':
			if (nonstrict && args[argIndex] instanceof Double) {
			    Double x = (Double) args[argIndex];
			    // Require that rounding the integer produces the
			    // same number when it is converted back to a
			    // double.
			    Long ix = Long.valueOf(Math.round(x));
			    // newargs[index[argIndex]] = ix;
			    if (x == ix.doubleValue()) {
				// s3 = (index[argIndex] + 1) + "$";
				if (index[argIndex] == -1) {
				    index[argIndex] = tail;
				    alist.add(ix);
				    tail++;
				}
				s3 = (index[argIndex] + 1) + "$";
			    } else {
				if (oindex[argIndex] == -1) {
				    oindex[argIndex] = tail;
				    alist.add(args[argIndex]);
				    tail++;
				}
				s3 = (oindex[argIndex] + 1) + "$";
			    }
			} else {
			    if (oindex[argIndex] == -1) {
				oindex[argIndex] = tail;
				alist.add(args[argIndex]);
				tail++;
			    }
			    s3 = (oindex[argIndex] + 1) + "$";
			    // s3 = (argIndex + 1) + "$";
			}
			break;
		    default:
			if (oindex[argIndex] == -1) {
			    oindex[argIndex] = tail;
			    alist.add(args[argIndex]);
			    tail++;
			}
			s3 = (oindex[argIndex] + 1) + "$";
			// s3 = (argIndex + 1) + "$";
			break;
		    }
		} else {
		    boolean gformat = true;
		    switch(conversion) {
		    case 'd':
		    case 'o':
		    case 'x':
		    case 'X':
			if (nonstrict && args[argIndex] instanceof Double) {
			    Double x = (Double) args[argIndex];
			    // Require that rounding the integer produces the
			    // same number when it is converted back to a
			    // double.
			    Long ix = Long.valueOf(Math.round(x));
			    // newargs[index[argIndex]] = ix;
			    if (x == ix.doubleValue()) {
				//s3 = (index[argIndex] + 1) + "$";
				if (index[argIndex] == -1) {
				    index[argIndex] = tail;
				    alist.add(ix);
				    tail++;
				}
				s3 = (index[argIndex] + 1) + "$";
			    } else {
				if (oindex[argIndex] == -1) {
				    oindex[argIndex] = tail;
				    alist.add(args[argIndex]);
				    tail++;
				}
				s3 = (oindex[argIndex] + 1) + "$";
			    }
			} else {
			    if (oindex[argIndex] == -1) {
				oindex[argIndex] = tail;
				alist.add(args[argIndex]);
				tail++;
			    }
			    s3 = (oindex[argIndex] + 1) + "$";
				// s3 = (argIndex + 1) + "$";
			}
			break;
		    case 'e':
		    case 'E':
			gformat = false;
			// fall through
		    case 'g':
		    case 'G':
			if (args[argIndex] instanceof Number) {
			    double val = ((Number) args[argIndex])
				.doubleValue();
			    if (USING_JAVA7 && val == 0.0) {
				if (findex[argIndex] == -1) {
				    findex[argIndex] = tail;
				    alist.add(new OurZeroDouble(l, gformat));
				    tail++;
				}
				s3 = (findex[argIndex] + 1) + "$";
				s6 = Character.isUpperCase(conversion)? "S":
				    "s";
			    } else if (args[argIndex] instanceof Double) {
				if (oindex[argIndex] == -1) {
				    oindex[argIndex] = tail;
				    alist.add(args[argIndex]);
				    tail++;
				}
				s3 = (oindex[argIndex] + 1) + "$";
			    } else {
				if (index[argIndex] == -1) {
				    index[argIndex] = tail;
				    alist.add(Double.valueOf(val));
				    tail++;
				}
				s3 = (index[argIndex] + 1) + "$";
			    }

			} else {
			    if (oindex[argIndex] == -1) {
				oindex[argIndex] = tail;
				alist.add(args[argIndex]);
				tail++;
			    }
			    s3 = (oindex[argIndex] + 1) + "$";
			}
			break;
		    default:
			if (oindex[argIndex] == -1) {
			    oindex[argIndex] = tail;
			    alist.add(args[argIndex]);
			    tail++;
			}
			s3 = (oindex[argIndex] + 1) + "$";
			// s3 = (argIndex + 1) + "$";
			break;
		    }
		}
		String replacement = s1 + s2 + s3 + s4 + s5 + s6;
		matcher.appendReplacement
		    (sb, Matcher.quoteReplacement(replacement));
	    }
	    ind++;
	}
	matcher.appendTail(sb);
	format = sb.toString();
	// args = newargs;
	args = alist.toArray();
	formatter.format(l, format, args);
	return this;
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and arguments.  The locale used is the one
     * defined during the construction of this formatter.
     *
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its
     *          {@link #close()} method
     *
     * @return  This formatter
     */
    public SciFormatter format(String format, Object... args)
	throws IllegalFormatException, FormatterClosedException
    {
	return format(formatter.locale(), format, args);
    }


    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and no arguments.  The locale used is the one
     * defined during the construction of this formatter.
     *
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     *
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     *
     * @return  This formatter
     */
    public SciFormatter format(String format)
	throws IllegalFormatException, FormatterClosedException
    {
	return format(formatter.locale(), format, emptyArray);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and one argument.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1)
	throws IllegalFormatException, FormatterClosedException
    {
	if (arg1.getClass().isArray()) {
	    return format(formatter.locale(), format, (Object[])arg1);
	}
	Object[] args = {arg1};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and two arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and three arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and four arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and five arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4, arg5};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and six arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4, arg5, arg6};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and seven arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7,
	};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and eight arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8
	};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and nine arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9
	};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and ten arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @param  arg10 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9, Object arg10)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9, arg10
	};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and eleven arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * If there are more arguments than format specifiers, the extra
     * arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @param  arg10 the first argument used by the format string
     * @param  arg11 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(String format, Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9, Object arg10, Object arg11)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9, arg10, arg11
	};
	return format(formatter.locale(), format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale and format string with no arguments.
     *
     * @param  l
     *         The {@linkplain java.util.Locale locale} to apply during
     *         formatting.  If {@code l} is {@code null} then no localization
     *         is applied.  This does not change this object's locale that was
     *         set during construction.
     *
     * @param  format
     *         A format string as described in 
     *         {@link java.util.Formatter Formatter}
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     *
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format)
	throws IllegalFormatException, FormatterClosedException
    {
	return format(l, format, emptyArray);
    }
    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and one argument.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format, Object arg1)
	throws IllegalFormatException, FormatterClosedException
    {
	if (arg1.getClass().isArray()) {
	    return format(l, format, (Object[]) arg1);
	} else {
	    Object[] args = {arg1};
	    return format(l, format, args);
	}
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and two arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and three arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2, Object arg3)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and four arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and five arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4, arg5};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and six arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1, arg2, arg3, arg4, arg5, arg6};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and seven arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7,
	};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and eight arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8
	};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and nine arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9
	};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and ten arguments.
     * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @param  arg10 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9, Object arg10)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9, arg10
	};
	return format(l, format, args);
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified locale, format string and eleven arguments.
     * * The locale used is the one defined during the construction of
     * this formatter.  If there are more arguments than format
     * specifiers, the extra arguments are ignored.
     * <P>
     * This method duplicates the behavior of the method 
     * {@link #format(String,Object...)} and is provided because some
     * scripting languages do not recognize Java methods that have a
     * variable number of arguments.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  arg1 the first argument used by the format string
     * @param  arg2 the first argument used by the format string
     * @param  arg3 the first argument used by the format string
     * @param  arg4 the first argument used by the format string
     * @param  arg5 the first argument used by the format string
     * @param  arg6 the first argument used by the format string
     * @param  arg7 the first argument used by the format string
     * @param  arg8 the first argument used by the format string
     * @param  arg9 the first argument used by the format string
     * @param  arg10 the first argument used by the format string
     * @param  arg11 the first argument used by the format string
     * @throws  IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.  For specification of all possible
     *          formatting errors, see the {@link java.util.Formatter Formatter}
     *          section of the formatter class specification.
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     * @return  This formatter
     */
    public SciFormatter format(Locale l, String format,
			       Object arg1, Object arg2,
			       Object arg3, Object arg4, Object arg5,
			       Object arg6, Object arg7, Object arg8,
			       Object arg9, Object arg10, Object arg11)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {
	    arg1, arg2, arg3, arg4, arg5, arg6,
	    arg7, arg8, arg9, arg10, arg11
	};
	return format(l, format, args);
    }

    /**
     * Returns the {@code IOException} last thrown by this formatter's {@link
     * Appendable}.
     *
     * <p> If the destination's {@code append()} method never throws
     * {@code IOException}, then this method will always return {@code null}.
     *
     * @return  The last exception thrown by the Appendable or {@code null} if
     *          no such exception exists.
     */
    public IOException ioException() {
	return formatter.ioException();
    }

    /**
     * Returns the destination for the output.
     *
     * @return  The destination for the output
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     */
    public Appendable out() {
	return formatter.out();
    }


    /**
     * Returns the result of invoking {@code toString()} on the destination
     * for the output.  For example, the following code formats text into a
     * {@link StringBuilder} (created by Formatter's zero-argument
     * constructor) and then retrieves the resultant string:
     *
     * <blockquote><pre>
     *   Formatter f = new Formatter();
     *   f.format("Last reboot at %tc", lastRebootDate);
     *   String s = f.toString();
     *   // -&gt; s == "Last reboot at Sat Jan 01 00:00:00 PST 2000"
     * </pre></blockquote>
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * invocation
     *
     * <pre>
     *     out().toString() </pre>
     *
     * <p> Depending on the specification of {@code toString} for the {@link
     * Appendable}, the returned string may or may not contain the characters
     * written to the destination.  For instance, buffers typically return
     * their contents in {@code toString()}, but streams cannot since the
     * data is discarded.
     *
     * @return  The result of invoking {@code toString()} on the destination
     *          for the output
     *
     * @throws  FormatterClosedException
     *          If this formatter has been closed by invoking its {@link
     *          #close()} method
     */
    public String toString() {
	return formatter.toString();
    }
}

//  LocalWords:  printf blockquote Formatter formatter toString JDK
//  LocalWords:  SciFormatter getProperties StringBuilder getDefault
//  LocalWords:  charset defaultCharset SecurityException checkWrite
//  LocalWords:  SecurityManager getPath FileNotFoundException csn os
//  LocalWords:  UnsupportedEncodingException PrintStream ps fileName
//  LocalWords:  ArrayIndexOutOfBoundsException df Formattable ds fe
//  LocalWords:  ioException FormatterClosedException zA args newargs
//  LocalWords:  IllegalFormatException argIndex OurDouble arg pre tc
//  LocalWords:  IOException formatter's Appendable lastRebootDate
