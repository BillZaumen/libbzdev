package org.bzdev.util;
import java.util.regex.*;

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

/**
 * An interpreter for printf-style format strings with support for
 * use by scripting languages that do not support variable numbers of
 * arguments.
 * <P>
 * The behavior class is  identical to the class
 * {@link java.util.Formatter java.util.Formatter}. 
 * <P>
 * The method and constructor documentation has been copied verbatim from
 * the Open JDK 1.7 documentation as we cannot use inheritance to find it.
 * A series of methods named <code>format</code> have been added to explicitly
 * handle up to 11 arguments. These methods are provided for use with
 * scripting languages that do not recognize Java methods that use a
 * variable number of arguments.
 * <P>
 * Note: The Nashorn ECMAScript implementation does not need to use
 * this class as it handles format methods properly - the way Java does.
 * Furthermore, ECMAScript numbers are turned into the appropriate Java
 * types.  By contrast Rhino (at least, the version used when this
 * class was tested), does not understand methods with a variable number
 * of arguments.  VarArgsFormatter will work with it, but when the
 * format string expects an integer, a Double will not be converted to
 * an integer type unless rounding it produces the same value.
 * <P>
 * Also note that one may have to explicitly flush the output stream.
 * @see java.util.Formatter
 */
public final class VarArgsFormatter implements Closeable, Flushable {
    Formatter formatter;
    Object[] emptyArray = new Object[0];

    private boolean strict = false;

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
	strict = value;
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
    public boolean getStrictMode() {return strict;}

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
    public VarArgsFormatter() {
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
    public VarArgsFormatter(Appendable a) {
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
    public VarArgsFormatter(Appendable a, Locale l) {
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *          write access to the file
     *
     * @throws  FileNotFoundException
     *          If the given file object does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     */
    public VarArgsFormatter(File file)  throws FileNotFoundException {
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *          write access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public VarArgsFormatter(File file, String csn)
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(file.getPath())} denies
     *          write access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public VarArgsFormatter(File file, String csn, Locale l)
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
    public VarArgsFormatter(Locale l) {
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
    public VarArgsFormatter(OutputStream os) {
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
    public VarArgsFormatter(OutputStream os, String csn)
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
    public VarArgsFormatter(OutputStream os, String csn, Locale l)
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
    public VarArgsFormatter(PrintStream ps) {
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @throws  FileNotFoundException
     *          If the given file name does not denote an existing, writable
     *          regular file and a new regular file of that name cannot be
     *          created, or if some other error occurs while opening or
     *          creating the file
     */
    public VarArgsFormatter(String fileName) throws FileNotFoundException {
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public VarArgsFormatter(String fileName, String csn)
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
     * @throws  SecurityException
     *          If a security manager is present and {@link
     *          SecurityManager#checkWrite checkWrite(fileName)} denies write
     *          access to the file
     *
     * @throws  UnsupportedEncodingException
     *          If the named charset is not supported
     */
    public VarArgsFormatter(String fileName, String csn, Locale l)
	throws FileNotFoundException, UnsupportedEncodingException
    {
	formatter = new Formatter(fileName, csn, l);
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
    public VarArgsFormatter format(Locale l, String format, Object... args)
	throws IllegalFormatException, FormatterClosedException
    {
	if (strict) {
	    formatter.format(l, format, args);
	    return this;
	}
	Matcher matcher = p.matcher(format);
	StringBuffer sb = new StringBuffer();
	int[] index = new int[args.length];
	int indexCount =  0;
	for (int i = 0; i < args.length; i++) {
	    if (args[i] instanceof Double) {
		index[i] = args.length + (indexCount++);
	    } else {
		index[i] = i;
	    }
	}
	Object[] newargs = new Object[args.length + indexCount];
	System.arraycopy(args, 0, newargs, 0, args.length);

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
		    // System.out.println("continuing");
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
		switch(conversion) {
		case 'd':
		case 'o':
		case 'x':
		case 'X':
		    if (args[argIndex] instanceof Double) {
			Double x = (Double) args[argIndex];
			// Require that rounding the integer produces the
			// same number when it is converted back to a
			// double.
			Long ix = Long.valueOf(Math.round(x));
			newargs[index[argIndex]] = ix;
			if (x == ix.doubleValue()) {
			    s3 = (index[argIndex] + 1) + "$";
			} else {
			    s3 = (argIndex + 1) + "$";
			}
		    } else {
			s3 = (argIndex + 1) + "$";
		    }
		    break;
		default:
		    s3 = (argIndex + 1) + "$";
		    break;
		}
		String replacement = s1 + s2 + s3 + s4 + s5 + s6;
		matcher.appendReplacement
		    (sb, Matcher.quoteReplacement(replacement));
	    }
	    ind++;
	}
	matcher.appendTail(sb);
	format = sb.toString();
	args = newargs;
	formatter.format(l, format, args);
	return this;
    }

    /**
     * Writes a formatted string to this object's destination using the
     * specified format string and arguments.  The locale used is the one
     * defined during the construction of this formatter.
     * @param  format
     *         A format string as described in {@link java.util.Formatter Formatter}
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The maximum number of arguments is
     *         limited by the maximum dimension of a Java array as defined by
     *         <cite>The Java&trade; Virtual Machine Specification</cite>.
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
     * @return  this formatter
     */
    public VarArgsFormatter format(String format, Object... args)
	throws IllegalFormatException, FormatterClosedException
    {
	if (strict) {
	    formatter.format(format, args);
	    return this;
	} else {
	    return format(formatter.locale(), format, args);
	}
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
    public VarArgsFormatter format(String format)
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
    public VarArgsFormatter format(String format, Object arg1)
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2)
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(String format, Object arg1, Object arg2,
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
    public VarArgsFormatter format(Locale l, String format)
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
    public VarArgsFormatter format(Locale l, String format, Object arg1)
	throws IllegalFormatException, FormatterClosedException
    {
	Object[] args = {arg1};
	return format(l, format, args);
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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
    public VarArgsFormatter format(Locale l, String format,
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

//  LocalWords:  printf JDK Nashorn VarArgsFormatter formatter csn os
//  LocalWords:  StringBuilder toString getDefault charset checkWrite
//  LocalWords:  defaultCharset SecurityException SecurityManager ps
//  LocalWords:  getPath FileNotFoundException PrintStream fileName
//  LocalWords:  UnsupportedEncodingException ioException zA args arg
//  LocalWords:  FormatterClosedException IllegalFormatException pre
//  LocalWords:  IOException formatter's Appendable blockquote tc
//  LocalWords:  lastRebootDate
