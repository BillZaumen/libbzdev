package org.bzdev.util;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.io.*;
import java.text.MessageFormat;
import java.security.AccessController;
import java.security.PrivilegedAction;

//@exbundle org.bzdev.util.lpack.ErrorMsg


/**
 * Error-Message Handling with GUI support.
 * This class handles the display of error messages. It is intended
 * for applications that may generate multiple errors or warnings
 * and that might run without a GUI. For some GUI-related operations,
 * one should use the class {@link org.bzdev.swing.SwingErrorMessage}.
 * Both this class and {@link org.bzdev.swing.SwingErrorMessage} provide
 * static methods, but are designed to be used interchangably for methods
 * common to both. They were originally a single class, but due to
 * the introduction of Java modules, it was desirable to split these into
 * two classes in order to allow some applications to be run with fewer
 * JAR files.
 * <P>
 * If nothing is configured, the messages by default are printed on
 * System.err. Where the messages are sent can be modified by calling
 * {@link ErrorMessage#setAppendable(Appendable)}.
 * <P>
 * For methods that cause a message to be displayed the order of arguments
 * are:
 * <OL>
 *   <LI> a {@link String} giving a title for a dialog box. A dialog box
 *        will be provided only if the method
 *        {@link org.bzdev.swing.SwingErrorMessage#setComponent(Component)}
 *        has been called, including with a null argument.
 *   <LI> a {@link Locale} giving the locale to use (null implies the
 *        default locale.
 *   <LI> a {@link String} containing format directives or the messsage itself
 *   <LI> a variable number of {@link Object}s providing arguments specified by
 *        a format string.
 * </OL> 
 * Depending on the method, some of these may be missing.  The order is
 * different from that used by {@link javax.swing.JOptionPane}. The
 * rationale for a different order is to make any format string used
 * appear directly before its arguments and to keep the order of
 * arguments consistent among methods defined by this class.
 * <P>
 * The method {@link ErrorMessage#setStackTrace(boolean)} can be used
 * to programatically turn the display of stack traces on and off for
 * those "display" methods that take a Throwable as an argument,
 * Error messages will sometimes be directed to a console. The documentation
 * for {@link org.bzdev.swing.SimpleConsole} explains how do do this.
 * @see org.bzdev.swing.SimpleConsole
 * @see org.bzdev.swing.SwingErrorMessage
 */
public class ErrorMessage {
    private static final String resourceBundleName = 
	"org.bzdev.util.lpack.ErrorMsg";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    private static ErrorMsgOps ops = new ErrorMsgOps() {
	    @Override
	    public void addSeparatorIfNeeded() {
		ErrorMessage.addSeparatorIfNeededAux();
	    }

	    @Override
	    public void addSeparator() {
		ErrorMessage.addSeparatorAux();
	    }

	    @Override
	    public void setStackTrace(boolean value) {
		ErrorMessage.setStackTraceAux(value);
	    }

	    @Override
	    public boolean stackTraceEnabled() {
		return ErrorMessage.stackTraceEnabledAux();
	    }

	    @Override
	    public void setAppendable(Appendable out) {
		setAppendableAux(out);
	    }
	    

	    @Override
	    public void display(String fn, int lineno, String msg) {
		ErrorMessage.displayAux(fn, lineno, msg);
	    }

	    @Override
	    public void displayConsoleIfNeeded() {
		return;
	    }

	    @Override
	    public void display(String prefix, Throwable t) {
		ErrorMessage.displayAux(prefix, t);
	    }

	    @Override
	    public void display(Throwable t) {
		ErrorMessage.displayAux(t);
	    }

	    @Override
	    public void display(String msg) {
		ErrorMessage.displayAux(msg);
	    }

	    @Override
	    public void format(String format, Object... args) {
		ErrorMessage.formatAux(format, args);
	    }

	    @Override
	    public void format(Locale locale, String format, Object... args) {
		ErrorMessage.formatAux(locale, format, args);
	    }

	    @Override
	    public void displayFormat(String title, String format,
				      Object... args)
	    {
		ErrorMessage.displayFormatAux(title, format, args);
	    }

	    @Override
	    public void displayFormat(String title, Locale locale,
				      String format,
				      Object... args)
	    {
		ErrorMessage.displayFormatAux(title, locale, format, args);
	    }

	    @Override
	    public void display(String title, String msg) {
		ErrorMessage.displayAux(title, msg);
	    }

	    @Override
	    public void display(String fn, String input, int index,
				Exception e, boolean verbose,
				boolean showLocation)
	    {
		ErrorMessage.displayAux(fn, input, index, e, verbose,
					showLocation);
	    }
	};

    /**
     * Set the message operations object.
     * This class is used by {@link org.bzdev.swing.SwingErrorMessage}
     * to override the default behavior of this class.
     * @param newops a replacement for the object that handles error
     *        messages.
     */
    protected static void setOps(ErrorMsgOps newops) {
	ops = newops;
    }

    /*
     * Get an instance of ErrorMsgOps.
     * @return an instance of ErrorMsgOps that cn be used for reporting
     *         error messages or warnings.
    protected static ErrorMsgOps getErrorMsgOps() {
	
	return (ops != null)? ops2: ops;
    }
    */

    /**
     * A field indicating if a separator is needed.
     * This should be treated as a read-only value.
     */
    protected static boolean needSeparator = false;
    /**
     * Add separator if needed.
     */
    public static void addSeparatorIfNeeded() {
	ops.addSeparatorIfNeeded();
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     */
    protected static void addSeparatorIfNeededAux() {
	if (needSeparator) {
	    addSeparator();
	}
	needSeparator = false;
    }

    /**
     * Add a separator.
     */
    public static void addSeparator() {
	ops.addSeparator();
    }
    /**
     * Implemtation method for an instance of ErrorMsgOps.
     */
    protected static void addSeparatorAux() {
	try {
	    err.append("________________________________________________\n\n");
	} catch (IOException e) {}
	needSeparator = false;
    }
    static boolean recordStackTrace = false;

    /**
     * Set stack-trace recording.
     * Determines if a stack trace is shown when a Throwable is displayed.
     * @param value true if stack traces should be recorded; false otherwise
     * @see ErrorMessage#display(Throwable)
     */
    public static void setStackTrace(boolean value) {
	ops.setStackTrace(value);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param value an argument
     */
    protected static void setStackTraceAux(boolean value) {
	recordStackTrace = value;
    }

    /**
     * Check if stack traces are enabled.
     * @return true if stack traces are enabled; false otherwise
     * @see #setStackTrace(boolean)
     */
    public static boolean stackTraceEnabled() {
	return ops.stackTraceEnabled();
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @return status
     */
    protected static boolean stackTraceEnabledAux() {
	return recordStackTrace;
    }

    /**
     * The {@link Appendable} to which error messages will be written.
     * The default is {@link System#err}.
     */
    protected static Appendable err = System.err;

    /**
     * Set the Appendable recording error messages.
     * The default is System.err.  The method
     * {@link #displayConsoleIfNeeded()} will display a console
     * if it is passed to this method and if it is an instance of
     * {@link org.bzdev.swing.SimpleConsole}.  To use the Java console
     * for output (the TTY associated with a process on Unix/Linux),
     * try setAppendable(System.console().writer()); however, normally
     * one would just use System.err.
     * @param out the Appendable for error messages; null for the default.
     */
    public static void setAppendable(Appendable out) {
	ops.setAppendable(out);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param out an argument
     */
    protected static void setAppendableAux(Appendable out) {
	if (out == null) out = System.err;
	err = out;
    }

    /**
     * Display a message with a file name and line number.
     * @param fn the file name
     * @param lineno the line number
     * @param msg the message to display
     */
    public static void display(String fn, int lineno, String msg) {
	ops.display(fn, lineno, msg);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param fn an argument
     * @param lineno an argument
     * @param msg an argumnt
     */
    protected static void displayAux(String fn, int lineno, String msg) {
	if (msg == null) return;
	String newmsg = (fn == null)? 
	    MessageFormat.format(localeString("lineNoMsg"), lineno, msg):
	    MessageFormat.format(localeString("fnLineNoMsg"), 
				 fn, lineno, msg);
	try {
	    err.append(newmsg);
	    err.append('\n');
	} catch (IOException e) {
	}
    }

    /**
     * Display a console if there is one, if it is an instance of
     * {@link org.bzdev.swing.SimpleConsole}, and if there is new text to
     * display. For a {@link org.bzdev.swing.SimpleConsole} to be available,
     * it must have * been passed to {@link #setAppendable(Appendable)}.
     */
    public static void displayConsoleIfNeeded() {
	ops.displayConsoleIfNeeded();
    }


    /**
     * Display a Throwable with a prefix.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param prefix a prefix to show before a stack trace, typically
     *        consisting of spaces
     * @param t the Throwable
     * @see #setStackTrace
     */
    public static void display (String prefix, Throwable t) 
    {
	ops.display(prefix, t);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param prefix an argument
     * @param t an argument
     */
    protected static void displayAux(String prefix, Throwable t) 
    {
	try {
	    err.append(prefix);
	    String tmsg = t.getMessage();
	    if (tmsg == null) {
		err.append("caused by " + t.getClass().toString());
	    } else {
		err.append("caused by " + t.getClass().toString() +": " + tmsg);
	    }
	    err.append('\n');
	    if (recordStackTrace && t instanceof Exception) {
		for (StackTraceElement ste: t.getStackTrace()) {
		    err.append(prefix);
		    err.append("    ");
		    err.append(ste.toString());
		    err.append('\n');
		}
	    }
	} catch (IOException eio) {
	}
	needSeparator = true;
    }

    /**
     * Display a Throwable.
     * at a minimum, the Throwable's message will be displayed.
     * Optionally, a stack trace may be displayed as well.
     * @param t the Throwable
     * @see #setStackTrace
     */
    public static void display(Throwable t) {
	ops.display(t);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param t an argument
     */
    protected static void displayAux(Throwable t) {
	if (t == null) return;
	String msg = t.getMessage();
	if (msg == null) {
	    msg = t.getClass().toString();   
	} else if (recordStackTrace && t instanceof Exception) {
	    msg = t.getClass().toString() + ": " + msg;
	}
	try {
	    err.append(msg);
	    err.append('\n');
	    if (recordStackTrace && t instanceof Exception) {
		for (StackTraceElement ste: t.getStackTrace()) {
		    err.append("    ");
		    err.append(ste.toString());
		    err.append('\n');
		}
		Throwable tt = t.getCause();
		if (tt != null) {
		    display("  ", tt);
		}
	    }
	} catch (IOException ioe) {
	}
	needSeparator = true;
    }


    /**
     * Display  a message.
     * @param msg the message to display
     */
    public static void display(String msg) {
	ops.display(msg);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param msg an argument
     */
    protected static void displayAux(String msg) {
	if (msg == null) return;
	try {
	    err.append(msg);
	    err.append('\n');
	} catch (IOException e) {
	}
	needSeparator = true;
    }

    /**
     * Display formatted text using the default locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param format a format string
     * @param args the arguments to format
     */
    public static void format(String format, Object... args) {
	ops.format(format, args);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param format an argument
     * @param args arguments
     */
    protected static void formatAux(String format, Object... args) {
	displayAux(String.format(format, args));
    }

    /**
     * Display formatted text for a specific locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
     */
    public static void format(Locale locale, String format, Object... args) {
	ops.format(locale, format, args);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param locale an argument
     * @param format an argument
     * @param args arguments
     */
    protected static void formatAux(Locale locale, String format,
				  Object... args)
    {
	displayAux(String.format(locale, format, args));
    }

    /**
     * Display formatted text with a title using the default locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * <P>
     * The title argument is ignored. This method is provided for
     * compatibility with {@link org.bzdev.swing.SwingErrorMessage}.
     * @param title the title; null for the default title
     * @param format a format string
     * @param args the arguments to format
     *
     */
    public static void displayFormat(String title, String format,
					 Object... args)
    {
	ops.displayFormat(title, format, args);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param title an argument
     * @param format an argument
     * @param args arguments
     */
    protected static void displayFormatAux(String title, String format,
					   Object... args)
    {
	displayAux(title, String.format(format, args));
    }

    /**
     * Display formatted text with a title for a specific locale.
     * See {@link java.util.Formatter} for a description of the
     * format string.
     * @param title the title; null for the default title
     * @param locale the Locale, null for no localization
     * @param format a format string
     * @param args the arguments to format
     */
    public static void displayFormat(String title, Locale locale,
				     String format,
				     Object... args)
    {
	ops.displayFormat(title, locale, format, args);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param title an argument
     * @param locale an argument
     * @param format an argument
     * @param args arguments

     */
    protected static void displayFormatAux(String title, Locale locale,
					   String format,
					   Object... args)
    {
	displayAux(title, String.format(locale, format, args));
    }

    /**
     * Display  a message with a title.
     * The title argument is ignored. This method is provided for
     * compatibility with {@link org.bzdev.swing.SwingErrorMessage}.
     * @param msg the message to display
     * @param title the title; null for the default title
     * @see #setAppendable(Appendable)
     */
    public static void display(String title, String msg) {
	ops.display(title, msg);
    }

    /**
     * Implemtation method for an instance of ErrorMsgOps.
     * @param title an argument
     * @param msg an argument
     */
    protected static void displayAux(String title, String msg) {
	try {
	    err.append(msg);
	    err.append('\n');
	} catch (IOException e) {}
    }

    private static  String linesepStr;
    private static  char[] linesep;
    private static char finalLineSep;

    static {
	// In case this object is created after a security manager
	// is installed.
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		    public Void run() {
			linesepStr = System.getProperty("line.separator");
			linesep = linesepStr.toCharArray();
			finalLineSep = linesep[linesep.length-1];
			return (Void) null;
		    }
		});
    }

    // for reconstructing an error.
    /**
     * Get the line, column number, and end of line for a specified
     * offset into a string.
     * @param s the string
     * @param index the offset from the start of the string (the offset
     *        for the 1st character is 0)
     * @return an array whose first element is the line number, whose
     *         second element is the column number, and whose third
     *         element is the offset for the end of the line, excluding any
     *         trailing white space
     */
    public static int[] getLineAndColumn(String s, int index) {
	int line = 1;
	int last = 0;
	int count = 0;
	int j = 0;
	int len = s.length();
	if (index > len) {
	    System.err.println("error at index " + index
			       + " greater than string length:");
	    System.err.println(s);
	    index = len;
	}
	for (int i = 0; i < index; i++) {
	    char ch = s.charAt(i);
	    if (ch == finalLineSep) {
		// in case we end with just '\n' instead of '\r'\n'
		// when running on systems that use '\r'\n' as the
		// default line terminator.
		line++;
		j = 0;
		last = i+1;
	    } else {
		if (ch == linesep[j]) {
		    if (j == linesep.length - 1) {
			line++;
			j = 0;
			last = i+1;
		    } else {
			j++;
		    }
		} else {
		    j = 0;
		}
	    }
	}
	int end = index;
	j = 0;
	boolean eol = false;
	while (end < len) {
	    char ch = s.charAt(end);
	    if (ch == linesep[j]) {
		if (j == linesep.length-1) {
		    eol = true;
		    break;
		} else {
		    j++;
		}
	    } else {
		j = 0;
	    }
	    end++;
	}
	if (eol) {
	    end -= linesep.length;
	    if (!Character.isWhitespace(s.charAt(end))) end++;
	}
	return new int[] {
	    line, index - last, end
	};
    }

    private static Map<Class<? extends Throwable>,Function<Throwable,String>>
	msgmap = new HashMap<>();

    /**
     * Add a mapping from a subclass of {@link Throwable} to a function
     * that provides the message for that {@link Throwable} without any
     * modifications.
     * This method should be called at most once for each class:
     * subsequent calls for a given class will be ignored.
     * <P>
     * Note: a mapping is provided for the class
     * {@link ObjectParser.Exception} as it will annotate its message
     * with a description of the location where an error occurred.
     * {@link ObjectParser.Exception} provides a method
     * {@link ObjectParser.Exception#getPlainMessage()} that recovers the
     * unannotated message.
     * @param clasz the class of the {@link Throwable}
     * @param mapping a function that maps the {@link Throwable} to its
     *        message.
     */
    public static void addToMessageMap(Class<? extends Throwable> clasz,
				       Function<Throwable,String> mapping)
    {
	if (msgmap.containsKey(clasz)) return;
	msgmap.put(clasz, mapping);
    }

    /**
     * Get an unannotated message for a {@link Throwable}.
     * In most casses, this simply calls the throwable's
     * {@link Throwable#getMessage() getMessage()} method. That
     * behavior can be modified by calling
     * {@link #addToMessageMap(Class,Function)}.
     * <P>
     * This method is used by
     * {@link #getMultilineString(String,String,String,int,Exception,boolean,boolean)},
     * which in turn is used by {@link ObjectParser.Exception#getMessage()} to
     * prevent a strange loop (i.e., infinite recursion).
     * @param t the {@link Throwable}.a
     * @return the message
     */
    public static String getMessageForThrowable(Throwable t) {
	final Class<? extends Throwable> tc = t.getClass();
	Method m = AccessController
	    .doPrivileged(new PrivilegedAction<Method>() {
		public Method run() {
		    try {
			return tc.getMethod("getMessage");
		    } catch (Exception e) {
			return  null;
		    }
		}
	    });
	Class<?> dc = m.getDeclaringClass();
	if (msgmap.containsKey(dc)) {
	    return msgmap.get(dc).apply(t);
	} else {
	    return t.getMessage();
	}
    }

    static {
	addToMessageMap(ObjectParser.Exception.class,
			(e)->{
			    return ((ObjectParser.Exception)e)
				.getPlainMessage();
			});
    }

    /**
     * Generate a multi-line string for an error message, based on an
     * exception, with a line from some input.
     * The default format in English shows the filename in quotes
     * followed by the line number, the class for the exception, and
     * the corresponding message. The next line contains a portion of
     * the input, with a subsequent line including a caret indicating
     * the point where the error occurred, followed by some information
     * about the column number.
     * @param prefix a string that starts each line when verbose is true
     *        and each line other than the first line when verbose is false
     * @param fn a file name or other name for the input
     * @param input the input being processed
     * @param index a location in the input where an error occurred
     * @param e the exception that generated an error
     * @param verbose true if a long format should be used
     * @param useLocation true if the location should appended
     * @return a string spaning multiple lines
     */
    public static String getMultilineString(String prefix,
					    String fn, String input, int index,
					    Exception e,
					    boolean verbose,
					    boolean useLocation)
    {
	int[] loc = getLineAndColumn(input, index);
	int lineNumber = loc[0];
	int columnNumber = loc[1];
	int end = loc[2];
	String sourceLine = input.substring(index - columnNumber, end)
	    .stripTrailing().replace('\t', ' ');
	StringBuilder sb = new StringBuilder();
	// ObjectParser.Exception uses this method to generate its
	// messages, so we have to treat it specially to avoid a strange loop.
	String msg = getMessageForThrowable(e);
	if (verbose) {
	    sb.append(prefix);
	    sb.append(MessageFormat.format(localeString("fnLineNoEMsg"),
					   fn, lineNumber,
					   e.getClass().getName(),
					   msg));
	} else if (useLocation) {
	    sb.append(prefix);
	    sb.append(MessageFormat.format(localeString("fnLineNoEMsg"),
					   fn, lineNumber,
					   e.getClass().getSimpleName(),
					   msg));
	} else {
	    sb.append(MessageFormat.format(localeString("exceptionMsg"),
					   e.getClass().getSimpleName(),
					   msg));
	}
	sb.append(linesepStr);
	sb.append(prefix);
	sb.append(sourceLine);
	sb.append(linesepStr);
	sb.append(prefix);
	for (int i = 0; i < columnNumber; i++) {
	    sb.append(' ');
	}
	if (useLocation) {
	    sb.append("^ ");
	    if (verbose) {
		sb.append(MessageFormat.format(localeString("column"),
					       columnNumber+1));
	    } else {
		sb.append(MessageFormat.format(localeString("shortColumn"),
					       columnNumber+1));
	    }
	} else {
	    sb.append("^");
	}
	return sb.toString();
    }

    /**
     * Display an error message, based on an exception,
     * with a line from some input.
     * The default format in English shows the filename in quotes
     * followed by the line number, the class for the exception, and
     * the corresponding message. The next line contains a portion of
     * the input, with a subsequent line including a caret indicating
     * the point where the error occurred, followed by some information
     * about the column number.
     * @param fn a file name or other name for the input
     * @param input the input being processed
     * @param index a location in the input where an error occurred
     * @param e the exception that generated an error
     * @param verbose true if a long format should be used
     * @param showLocation true if line number and column numbers should be
     *        shown; false otherwise
     */
    public static void display(String fn, String input, int index,
			       Exception e, boolean verbose,
			       boolean showLocation)
    {
	ops.display(fn, input, index, e, verbose, showLocation);
    }

    /**
     * Implementation to display an error message, based on an exception,
     * with a line from some input.
     * The default format in English shows the filename in quotes
     * followed by the line number, the class for the exception, and
     * the corresponding message. The next line contains a portion of
     * the input, with a subsequent line including a caret indicating
     * the point where the error occurred, followed by some information
     * about the column number.
     * @param fn a file name or other name for the input
     * @param input the input being processed
     * @param index a location in the input where an error occurred
     * @param e the exception that generated an error
     * @param verbose true if a long format should be used
     * @param showLocation true if line number and column numbers should be
     *        shown; false otherwise
     */
    protected static void displayAux(String fn, String input, int index,
				     Exception e, boolean verbose,
				     boolean showLocation)
    {
	try {
	    err.append(getMultilineString("", fn, input, index, e,
					  verbose, showLocation));
	    err.append(linesepStr);
	} catch (IOException eio) {}
    }
}

//  LocalWords:  exbundle interchangably ErrorMessage setAppendable
//  LocalWords:  Appendable OL setComponent messsage setStackTrace cn
//  LocalWords:  boolean programatically Throwable ErrorMsgOps TTY fn
//  LocalWords:  getErrorMsgOps displayConsoleIfNeeded lineno msg
//  LocalWords:  lineNoMsg fnLineNoMsg Throwable's args fnLineNoEMsg
//  LocalWords:  exceptionMsg IOException
