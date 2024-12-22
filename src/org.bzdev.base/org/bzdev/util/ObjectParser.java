package org.bzdev.util;
import java.util.regex.*;
import java.util.ResourceBundle;
import java.util.ArrayList;

//@exbundle org.bzdev.util.lpack.ObjectParser

class ObjectParserRB {
    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.util.lpack.ObjectParser");
}

/**
 * Parser that turns a string into an object.
 * Several static fields provide default parsers for various
 * types (strings, booleans, integers, and doubles), and the
 * value <CODE>null</CODE>.
 * <P>
 * In one use case, the caller will maintain a list of parsers
 * and call {@link ObjectParser#appliesTo(String)} to determine
 * if a parser in the list should be used. If so, the caller
 * will then call {@link ObjectParser#matches(String)} to check for
 * syntax errors and then call {@link ObjectParser#parse(String)}
 * to obtain the value. {@link ObjectParser#parse(String)} may
 * throw an exception of the string cannot be parsed (for example,
 * when the return value is an {@link java.lang.Integer Integer} whose
 * value is below {@link java.lang.Integer#MIN_VALUE} or above
 * {@link java.lang.Integer#MAX_VALUE}).
 *  <P>
 * This interface was created to support the class
 * {@link JSUtilities.YAML} and is public because it may have other
 * uses. 
 */
public interface ObjectParser<T> {

    private static String errorMsg(String key, Object... args) {
	return (new SafeFormatter())
	    .format(ObjectParserRB.exbundle.getString(key), args).toString();
    }

    /**
     * Exception class for ObjectParser errors.
     * This class provides access methods to recover the string that
     * was being parsed and the offset into the string that is associated
     * with the error.
     */
    public static class Exception extends IllegalArgumentException {
	private String input;
	private String filename = null;
	private int offset;

	private ArrayList<String> inputList = new ArrayList<>();;
	private ArrayList<String> filenameList = new ArrayList<>();;
	private ArrayList<Integer> offsetList = new ArrayList<>();

	/**
	 * Constructor.
	 * @param msg a message describing this exception
	 * @param input the input string
	 * @param offset an offset into the string indicating
	 *        where an error occurred; -1 if there is no specific offset
	 */
	public Exception(String msg, String input, int offset) {
	    super(msg);
	    this.input = input;
	    this.offset = offset;
	}

	/**
	 * Constructor with a file name.
	 * @param msg a message describing this exception
	 * @param filename the name of the input
	 * @param input the input string
	 * @param offset an offset into the string indicating
	 *        where an error occurred; -1 if there is no specific offset
	 */
	public Exception(String msg, String filename,
			 String input, int offset) {
	    super(msg);
	    this.filename = filename;
	    this.input = input;
	    this.offset = offset;
	}

	/**
	 * Constructor with a cause.
	 * @param msg a message describing this exception
	 * @param cause the Trowable that caused this exception
	 * @param input the input string
	 * @param offset an offset into the string indicating
	 *        where an error occurred; -1 if there is no specific offset
	 */
	public Exception(String msg, Throwable cause,
			 String input, int offset) {
	    super(msg, cause);
	    this.input = input;
	    this.offset = offset;
	}

	/**
	 * Constructor with a file name and  cause.
	 * @param msg a message describing this exception
	 * @param cause the Trowable that caused this exception
	 * @param filename the name of the input
	 * @param input the input string
	 * @param offset an offset into the string indicating
	 *        where an error occurred; -1 if there is no specific offset
	 */
	public Exception(String msg, Throwable cause,
			 String filename, String input, int offset) {
	    super(msg, cause);
	    this.filename = filename;
	    this.input = input;
	    this.offset = offset;
	}

	/**
	 * Add additional elements for a stack trace.
	 * @param filename the name of the input
	 * @param input the input string
	 * @param offset an offset into the string indicating
	 *        where an error occurred; -1 if there is no specific offset
	 */
	public void addTrace(String filename, String input, int offset) {
	    filenameList.add(filename);
	    inputList.add(input);
	    offsetList.add(offset);
	}

	/**
	 * Get the input string for which the error occurred.
	 * @return the string
	 */
	public String getInput() {return input;}

	/**
	 * Get the file name.
	 * @return the file name; null if there is none
	 */
	public String getFileName() {return filename;}

	/**
	 * Get the offset into the input string for the location at
	 * which an error occurred.
	 * @return the offset; -1 if none is known
	 */
	public int getOffset() {return offset;}

	/**
	 * Get the message as it was provided to this exception's constructor.
	 * @return the message.
	 */
	public String getPlainMessage() {
	    return super.getMessage();
	}

	String prefix = "";

	/**
	 *  Set the text that should precede each line of an error message.
	 *  The method assumes that the message used to create this exception
	 *  is on a single line.
	 *  @param prefix the prefix
	 */
	public void setPrefix(String prefix) {
	    this.prefix = prefix;
	}

	/**
	 * Get the prefix.
	 * @return the prefix; null if it is not defined or if it is
	 *         an empty string
	 */
	public String getPrefix() {
	    return (prefix == null)? prefix:
		(prefix.length() == 0)? null:
		prefix;
	}


	boolean showLoc = false;
	/**
	 * Indicate if a line showing the location of an error message
	 * should be added.
	 * @param mode true if a location should be added; false otherwise
	 */
	public void showLocation(boolean mode) {
	    showLoc = mode;
	}

	/**
	 * Get the message for this exception.
	 * The message will be annotated by adding a prefix, which
	 * can be set by calling {@link #setPrefix(String)}, and
	 * appending lines, each starting with the prefix, that contain
	 * a line from an input file or string, and a pointer indicating
	 * where in the line the error was detected. The first line will
	 * include a filename (if provided) and a line number.
	 * @return the annotated message
	 */
	@Override
	public String getMessage() {
	    return ErrorMessage.getMultilineString(prefix,
						   filename, input, offset,
						   filenameList,
						   inputList,
						   offsetList,
						   this, false, showLoc);
	}

	/**
	 * Get the error message with an explicitly provided prefix.
	 * The prefix will start each line of the message.
	 * @param prefix the prefix
	 * @param verbose true if fully-qualified class names should be
	 *        shown; false for just the final component of the class name
	 * @param showLocation true to show the location for an error message;
	 *        false otherwise
	 * @return the error message
	 */
	public String getMessage(String prefix,
				 boolean verbose,
				 boolean showLocation)
	{
	    return ErrorMessage.getMultilineString(prefix, filename,
						   input, offset,
						   this, verbose, showLocation);
	}

	/**
	 * Determine if the exception was due to an explicit throw.
	 * ESP and ExpressionParser have 'throw' statement that will
	 * generate an exception.
	 * @return true if the exception was due to a throw statement,
	 *         false otherwise
	 */
	public boolean wasThrown() {return false;}
    }

    /**
     * Object to encapsulate ESP source code.
     * This is used by {@link org.bzdev.obnaming.ObjectNamerLauncher}
     * to distinguish ESP expressions from strings that may be
     * similar.
     * @see SourceParser
     */
    public static class Source {
	String s;
	volatile boolean isEvaluated = false;
	ObjectParser op;
	Object evaluated = null;

	/**
	 * Evaluate the encapsulated source if not yet evaluated
	 * and return the results of the evaluation.
	 * The source is evaluated only once in case of side effects.
	 * @return the object resulting from evaluating the source
	 *      a single time
	 */
	synchronized public Object evaluate() throws ObjectParser.Exception {
	    if (!isEvaluated) {
		evaluated = op.parse(s);
		isEvaluated = true;
	    }
	    return evaluated;
	}

	/**
	 * Return the string encapsulated by this object
	 * @return the string
	 */
	@Override
	public String toString() {
	    return s;
	}

	/**
	 * Constructor.
	 * @param op the object parser to use
	 * @param s the string containing ESP code
	 */
	Source(ObjectParser op, String s) {
	    this.op = op;
	    this.s = s;
	}
    }

    /**
     * Object parser that tags a string as being one that is likely to
     * be an ESP expression or statement.
     * <P>
     * This class is used by {@link org.bzdev.obnaming.ObjectNamerLauncher}
     * to distinguish strings that must be evaluated from strings that
     * should not be evaluated.  For example, when using ObjectNamerLauncher
     * with YAML,
     * <BLOCKQUOTE><PRE><CODE>
     *     - = 10 + 20
     * </CODE></PRE></BLOCKQUOTE>
     * would be treated as an expression whose value is 30. By
     * contrast,
     * <BLOCKQUOTE><PRE><CODE>
     *     - !!str = 10 + 20
     * </CODE></PRE></BLOCKQUOTE>
     * would be interpretted as the string "= 10 + 20".  This is
     * actually handled in two passes. During the first, sequences that
     * could be ESP expressions or statements are replaced with objects
     * whose type is {@link Source} containing the expression in string
     * form.  In the second pass, each {@link Source} object is evaluated
     * as an ESP expression or statement.  This prevents the second case,
     * where "= 10 + 20" was meant to be a string, from being replaced
     * with the value 30.
     */
    public static class SourceParser implements ObjectParser<Source> {
	ExpressionParser ep;

	@Override
	public boolean matches(String string) {
	    if  (string == null) return false;
	    return
		ExpressionParser.MATCHING_PATTERN.matcher(string).lookingAt();
	}
	@Override
	public Source parse(String s) throws ObjectParser.Exception {
	    return new Source(ep, s);
	}

	/**
	 * Constructor.
	 * @param ep the expression parser that will be used to evaluate to
	 *        evaluate the source when {@link Source#evaluate()} is
	 *        called for the first time.
	 */
	public SourceParser(ExpressionParser ep) {
	    this.ep = ep;
	}
    }





    /**
     * Determine this parser applies to a given string.
     * <P>
     * The default implementation simply calls {@link #matches(String)} and
     * returns the results of calling that method. This method should be
     * overridden when less restrictive criteria can be used.
     * For example, a parser that provides a {@link java.awt.Color}
     * might accept strings that start with 'rgb(', 'rgba(', etc.
     * Then a syntax error can be reported if  {@link #matches(String)}
     * returns false.
     * @param s a string to parse
     * @return true if this string should be used with this parser;
     *         false otherwise
     */
    default boolean appliesTo(String s) {
	return matches(s);
    }


    /**
     * Determine if a string is syntactically valid.
     * @param s the string
     * @return true if the test succeeds; false if it fails
     */
    boolean matches(String s);

    /**
     * Parse a string and return the corresponding object.
     * @param s the string
     * @return the corresponding object
     * @exception Exception if the string could not be successfully
     *            parsed or the object could not be created
     */
    T parse(String s) throws Exception;

    /**
     * A parser for strings.
     * This merely returns the string.
     */
    static final ObjectParser<? super String> STRING =
	new ObjectParser<String>() {
	    @Override
	    public boolean matches(String s) {return true;}
	    @Override
	    public String parse(String s) throws Exception {return s;}
	};
 
    /**
     * A parser for 'null'.
     * The allowed value is just 'null', and the parsed value is null.
     */
    static final ObjectParser<Object> NULL =
	new ObjectParser<Object>() {
	    String last = null;	// last string with a successful match
	    @Override
	    public boolean matches(String s) {
		if (s == null) return false;
		if (s == last) return true;
		if (s.charAt(0) == 'n'
		    && s.charAt(1) == 'u'
		    && s.charAt(2) == 'l'
		    && s.charAt(3) == 'l') {
		    last = s;
		    return true;
		}
		return false;
	    }
	    @Override
	    public Object parse(String s) throws Exception {
		if (matches(s)) {
		    last = null;
		    return null;
		}
		last = null;
		String msg = errorMsg("notNull");
		throw new Exception(msg, s, 0);
	    }
	};

    /**
     * A parser for booleans.
     * The allowable values are <CODE>true</CODE> and
     * <CODE>false</CODE>, and are case sensitive.
     */
    static  final ObjectParser<? super Boolean> BOOLEAN =
	new ObjectParser<Boolean>() {
	    String last = null;	// last string with a successful match
	    @Override
	    public boolean matches(String s) {
		if (s == null) return false;
		if (s == last) return true; // quick test for the same string
		int len = s.length();
		if (len == 0) return false;
		char ch = s.charAt(0);
		if (ch == 't' && len == 4) {
		    if (s.charAt(1) == 'r'
			&& s.charAt(2) == 'u'
			&& s.charAt(3) == 'e') {
			last = s;
			return true;
		    }
		} else if (ch == 'f' && len == 5) {
		    if (s.charAt(1) == 'a'
			&& s.charAt(2) == 'l'
			&& s.charAt(3) == 's'
			&& s.charAt(4) == 'e') {
			last = s;
			return true;
		    }
		}
		return false;
	    }
	    @Override
	    public Boolean parse(String s) throws Exception {
		if (matches(s)) {
		    last = null;
		    return s.charAt(0) == 't';
		}
		last = null;
		String msg = errorMsg("notBoolean");
		throw new Exception(msg, s, 0);
	    }
	};

    /**
     * A parser for long integers.
     */
    static final ObjectParser<? super Long> LONG =
	new ObjectParser<Long>() {
	    private final Pattern LONG_PATTERN =
		Pattern.compile("[-+]?[1-9][0-9]*");
	    @Override
	    public boolean matches(String s) {
		if (s == null || s.length() == 0) return false;
		char ch = s.charAt(0);
		if (ch == '-' || ch == '+' || Character.isDigit(ch)) {
		    return LONG_PATTERN.matcher(s).matches();
		} else {
		    return false;
		}
	    }
	    @Override
	    public Long parse(String s) throws Exception {
		try {
		    if (s.charAt(0) == '+') s = s.substring(1);
		    return Long.valueOf(s);
		} catch (NumberFormatException e) {
		    String msg = errorMsg("notLong");
		    throw new Exception(msg, e, s, 0);
		}
	    }
	};

    /**
     * A parser for integers.
     */
    static final ObjectParser<? super Integer> INTEGER =
	new ObjectParser<Integer>() {
	    private final Pattern INTEGER_PATTERN =
		Pattern.compile("[-+]?[1-9][0-9]*");
	    @Override
	    public boolean matches(String s) {
		if (s == null || s.length() == 0) return false;
		char ch = s.charAt(0);
		if (ch == '-' || ch == '+'|| Character.isDigit(ch)) {
		    return INTEGER_PATTERN.matcher(s).matches();
		} else {
		    return false;
		}
	    }
	    @Override
	    public Integer parse(String s) throws Exception {
		try {
		    if (s.charAt(0) == '+') s = s.substring(1);
		    return Integer.valueOf(s);
		} catch (NumberFormatException e) {
		    String msg = errorMsg("notInt");
		    throw new Exception(msg, e, s, 0);
		}
	    }
	};

    /**
     * A parser for integers or longs.
     * An Integer will be returned unless the value cannot be
     * represented as an Integer, in which case a Long will
     * be returned.
     */
    static final ObjectParser<? super Number> INTLONG =
	new ObjectParser<Number>() {
	    private final Pattern INTEGER_PATTERN =
		Pattern.compile("[-+]?[0-9]+");
	    @Override
	    public boolean matches(String s) {
		if (s == null || s.length() == 0) return false;
		char ch = s.charAt(0);
		if (ch == '-' || ch== '+' || Character.isDigit(ch)) {
		    return INTEGER_PATTERN.matcher(s).matches();
		} else {
		    return false;
		}
	    }
	    @Override
	    public Number parse(String s) throws Exception {
		if (s.charAt(0) == '+') s = s.substring(1);
		try {
		    long x = Long.parseLong(s);
		    if (x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE) {
			return Integer.valueOf((int)x);
		    } else {
			return Long.valueOf(x);
		    }
		} catch (NumberFormatException e) {
		    String msg = errorMsg("notIntLong");
		    throw new Exception(msg, e, s, 0);
		}
	    }
	};

    /**
     * A parser for double-precision numbers.
     */
    static final ObjectParser<? super Double> DOUBLE =
	new ObjectParser<Double>() {
	    private final Pattern DOUBLE_PATTERN
		= Pattern.compile
		("[-+]?([0-9]+([.][0-9]+)?|[.][0-9]+)([eE]-?[0-9]+)?");
	    @Override
	    public boolean matches(String s) {
		if (s == null || s.length() == 0) return false;
		char ch = s.charAt(0);
		if (ch == '-' || ch == '+' || ch == '.'
		    || Character.isDigit(ch)) {
		    return DOUBLE_PATTERN.matcher(s).matches();
		} else {
		    return false;
		}
	    }
	    @Override
	    public Double parse(String s) throws Exception {
		if (s.charAt(0) == '+') s = s.substring(1);
		try {
		    return Double.valueOf(s);
		} catch (NumberFormatException e) {
		    String msg = errorMsg("notDouble");
		    throw new Exception(msg, e, s, 0);
		}
	    }
	};

    /**
     * A parser for numbers.
     * The number that the {@link #parse(String)} method returns will
     * be a subclass of {@link java.lang.Number}: either an
     * {@link Integer}, a {@link Long}, or a {@link Double}, whichever
     * is the most specific class that will represent the number
     * provided.
     */
    static final ObjectParser<? super Number> NUMBER = 
	new ObjectParser<Number>() {
	    private final Pattern
		INTEGER_PATTERN = Pattern.compile("[-+]?[0-9]+");
	    private final Pattern DOUBLE_PATTERN
		= Pattern.compile
		("[-+]?([0-9]+([.][0-9]+)?|[.][0-9]+)([eE]-?[0-9]+)?");
	    @Override
	    public boolean matches(String s) {
		if (s == null || s.length() == 0) return false;
		char ch = s.charAt(0);
		if (ch == '-' || ch == '+'|| ch == '.'
		    || Character.isDigit(ch)) {
		    return DOUBLE_PATTERN.matcher(s).matches();
		} else {
		    return false;
		}
	    }
	    @Override
	    public Number parse(String s) throws Exception {
		if (s.charAt(0) == '+') s = s.substring(1);
		try {
		    if (INTEGER_PATTERN.matcher(s).matches()) {
			long x = Long.parseLong(s);
			if (x >= Integer.MIN_VALUE && x <= Integer.MAX_VALUE) {
			    return Integer.valueOf((int)x);
			} else {
			    return Long.valueOf(x);
			}
		    } else {
			return Double.valueOf(s);
		    }
		} catch (NumberFormatException e) {
		    String msg = errorMsg("notNumber");
		    throw new Exception(msg, e, s, 0);
		}
	    }
	};
}

//  LocalWords:  parsers booleans ObjectParser appliesTo JSUtilities
//  LocalWords:  YAML exbundle msg Trowable rgb rgba notNull notLong
//  LocalWords:  notBoolean notInt notIntLong notDouble notNumber
