package org.bzdev.util;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.lang.reflect.*;
import java.nio.charset.Charset;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.OptionalInt;
import java.util.OptionalDouble;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.URLPathParser;
import org.bzdev.util.SuffixArray;
import org.bzdev.util.TemplateProcessor;
import java.security.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;
import java.util.function.*;

//@exbundle org.bzdev.util.lpack.ObjectParser

/**
 * Parser for expressions that produce numbers of type double.
 * While most instance of {@link ObjectParser} provided in this package
 * just implement the {@link ObjectParser} interface,
 * {@link ExpressionParser} provides more capabilities. An
 * {@link ExpressionParser} can remember previous values, can be
 * configured to return values of various types, to accept various
 * constants (enumeration constants or integer-valued fields) as
 * arguments, and can evaluate functions and methods.
 * <P>
 * ExpressionParser also implements a scripting language named ESP.
 * For the full syntax and capabilities, please see
 * <STRONG><A HREF="doc-files/esp.html">ExpressionParser and the ESP scripting language</A></STRONG>.
 * <P>
 * {@link ExpressionParser} provides two constructors.  The first,
 * {@link ExpressionParser#ExpressionParser(Class...)} creates an
 * instance that provides functions that are implemented by public,
 * static methods of the classes listed in the argument list. These
 * are restricted to ones whose arguments and returned types are
 * {@link String}, int, long, double, and boolean, {@link Number},
 * {@link Double}, {@link Long}, {@link Integer}, {@link Boolean},
 * and stream-related classes.
 * The default behavior can be extended to allow more types and additional
 * capabilities by using the second constructor
 * {@link ExpressionParser#ExpressionParser(Class[],Class[],Class[],Class[],Class[])}.
 * The arguments to this constructor are
 * <OL>
 *    <LI> An array for classes providing the types that can be
 *         returned by methods or functions.
 *    <LI> An array of classes providing the types allowed
 *         as arguments for functions and methods.
 *    <LI> An array of classes providing those classes whose static methods
 *         will be used as functions (the effect is similar to using Java's
 *         static import statement).
 *    <LI> An array of classes providing those classes whose instance methods
 *         will be available.
 *    <LI> An array of classes provides those classes whose public, static,
 *         and final fields that are int-valued or enumeration constants will
 *         be available.
 * </OL>
 * The methods and functions are filtered to include only those
 * <UL>
 *   <LI> whose return values are int, double, or any of the classes
 *        listed in the first argument to the
 *        {@link ExpressionParser#ExpressionParser(Class[],Class[],Class[],Class[],Class[]) constructor}.
 *   <LI> whose arguments are int, double, or any of the classes listed in
 *        the second argument to the {
 *        {@link ExpressionParser#ExpressionParser(Class[],Class[],Class[],Class[],Class[]) constructor}.
 * </UL>
 */
public class ExpressionParser implements ObjectParser<Object>
{

    // resource bundle for messages used by exceptions and errors
    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.util.lpack.ObjectParser");

    private static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    private static enum Operator {
	FUNCTION_KEYWORD,
	BACKQUOTE,		// turn a block into a lambda expression
	NEW,
	VAR,
	QVAR,			// var IDENT ?= ... ;
	QQVAR,			// var IDENT ??= ...;
	PLUS,
	UNARY_MINUS,
	BINARY_MINUS,
	TIMES,
	DIVIDEBY,
	MOD,
	MATH_MOD,
	DOT,
	LSHIFT,			// << operator
	RSHIFT,			// >> operator
	URSHIFT,		// >>> operator
	NOT,
	AND,
	OR,
	XOR,
	EQ,
	NE,
	GT,
	LT,
	GE,
	LE,
	INSTANCEOF,
	LNOT,	       // logical operator '!'
	LOR,	       // logical OR  '||'
	LAND,	       // logical AND '&&'
	QMARK,	       // conditional
	COLON,	       // conditional
	ASSIGN1,       // assign to a variable
	ASSIGN2,       // assign to an object property or array element
	SWAP,	       // swap two variables
	THROW,
	// Following are not actually operators but this
	// is a convenient spot to denote them - it simplifies
	// the implementation of the Token class (see below)
	OBRACE,
	CBRACE,
	OBJOPENBRACE,
	OBJCLOSEBRACE,
	OBRACKET,
	CBRACKET,
	SEMICOLON,
	MINUS,
	NULL,
	VOID,
	OPAREN,
	CPAREN,
	COMMA,
	ACTIVE_COMMA,	// for object and list constructs
	NUMBER,
	CLASS,		// to represent types
	TYPED_NULL,	// the value null with a specified type
	METHOD_REF,	// Method reference
	CONSTANT,	// enum constant or int- public, static, final
	BOOLEAN,
	STRING,
	CONSTRUCTOR,
	FUNCTION,
	IMPORT,
	FINISH_IMPORT,
	METHOD,
	VARIABLE,
	VARIABLE_NAME,	// The name of a variable, not its value
	LEAVE_ON_STACK,	// change a closing paren so we keep an index
	EXIST_TEST,
	PARAMETER,	// Lambda-expression formal parameter
	UNARY_PLUS,	// no-op
	PEQ,		// Prefix equal (must be changed)
	NOOP,           // indicate a token should be skipped.
	// Next operators are used for debugging
	START_TOKEN_TRACING,
	STOP_TOKEN_TRACING,
	START_STACK_TRACING,
	STOP_STACK_TRACING,
    }

    // used by Token to record the current file name, is one is set.
    private static ThreadLocal<String> filenameTL = new ThreadLocal<>();


    private Reader reader = new InputStreamReader(System.in, UTF8);;
    private PrintWriter writer = new PrintWriter(System.out, true);
    private PrintWriter errorWriter = new PrintWriter(System.err, true);

    private ThreadLocal<Reader> tlReader = new ThreadLocal<>();
    private ThreadLocal<PrintWriter> tlWriter = new ThreadLocal<>();
    private ThreadLocal<PrintWriter> tlErrorWriter = new ThreadLocal<>();

    /**
     * Set the reader.
     * A reader can be used by a script for obtaining input.
     * The reader can be overridden on a per-thread basis by calling
     * {@link #setReaderTL(java.io.Reader)}.
     * <P>
     * A null argument does not restore the initial value.
     * @param reader the reader.
     */
    public void setReader(Reader reader) {this.reader = reader;}

    /**
     * Set the writer.
     * A writer can be used by a script for normal output.
     * The writer can be overridden on a per-thread basis by calling
     * {@link #setWriterTL(java.io.PrintWriter)}.
     * <P>
     * A null argument does not restore the initial value.
     * @param writer the writer.
     */
    public void setWriter(PrintWriter writer) {this.writer = writer;}

    /**
     * Set the error writer.
     * A writer can be used by a script for error output.
     * The error writer can be overridden on a per-thread basis by calling
     * {@link #setErrorWriterTL(java.io.PrintWriter)}.
     * <P>
     * A null argument does not restore the initial value.
     * @param errorWriter the writer.
     */
    public void setErrorWriter(PrintWriter errorWriter) {
	this.errorWriter = errorWriter;
    }

    /**
     * Set the thread-specific reader.
     * A reader can be used by a script for obtaining input.
     * A thread-specific reader, if not null, will be chosen instead of
     * the reader specified by {@link #setReader(java.io.Reader)}.
     * @param reader the reader.
     */
    public void setReaderTL(Reader reader) {this.tlReader.set(reader);}

    /**
     * Set the thread-specific writer.
     * A writer can be used by a script for normal output.
     * A thread-specific writer, if not null, will be chosen instead of
     * the writer specified by {@link #setWriter(java.io.PrintWriter)}.
     * @param writer the writer.
     */
    public void setWriterTL(PrintWriter writer) {this.tlWriter.set(writer);}

    /**
     * Set the thread-specific error writer.
     * A writer can be used by a script for error output.
     * A thread-specific error writer, if not null, will be chosen
     * instead of the error writer specified by
     * {@link #setErrorWriter(java.io.PrintWriter)}.
     * @param errorWriter the writer.
     */
    public void setErrorWriterTL(PrintWriter errorWriter) {
	this.tlErrorWriter.set(errorWriter);
    }

    private static final EnumSet<Operator> relationalOps
	= EnumSet.of(Operator.GT, Operator.GE, Operator.LT, Operator.LE);

    private static final EnumSet<Operator> arithOps
	= EnumSet.of(Operator.PLUS, Operator.UNARY_MINUS, Operator.BINARY_MINUS,
		     Operator.TIMES, Operator.DIVIDEBY, Operator.DOT);

    private static final EnumSet<Operator> logicalOps
	= EnumSet.of(Operator.NOT, Operator.AND, Operator.OR, Operator.XOR);


    private static final EnumSet<Operator> notBeforeVars
	= EnumSet.of(Operator.INSTANCEOF, Operator.THROW, Operator.CBRACE,
		     Operator.OBJCLOSEBRACE, Operator.CBRACKET,Operator.NULL,
		     Operator.VOID, Operator.CPAREN, Operator.NUMBER,
		     Operator.CLASS, Operator.METHOD_REF,Operator.CONSTANT,
		     Operator.BOOLEAN, Operator.STRING, Operator.CONSTRUCTOR,
		     Operator.FUNCTION, Operator.METHOD, Operator.VARIABLE,
		     Operator.VARIABLE_NAME, Operator.EXIST_TEST,
		     Operator.PARAMETER);

    private static final EnumSet<Operator> binaryOps
	= EnumSet.of(Operator.PLUS, Operator.BINARY_MINUS, Operator.TIMES,
		     Operator.DIVIDEBY, Operator.MOD, Operator.MATH_MOD,
		     Operator.DOT,
		     Operator.LSHIFT, Operator.RSHIFT, Operator.URSHIFT,
		     Operator.AND, Operator.OR, Operator.XOR,
		     Operator.EQ, Operator.NE, Operator.GT,
		     Operator.LT, Operator.GE, Operator.LE,
		     Operator.LOR, Operator.LAND, Operator.QMARK,
		     Operator.COLON, Operator.SWAP);

    // operators other than binaryOps that cannot appear before a
    // method reference
    private static final EnumSet<Operator> notBeforeMethodRef
	= EnumSet.of(Operator.UNARY_MINUS, Operator.NOT, Operator.LNOT,
		     Operator.NEW, Operator.OBRACE, Operator.CBRACE,
		     Operator.OBRACKET, Operator.CBRACKET,
		     Operator.OPAREN, Operator.OBJOPENBRACE,
		     Operator.OBJCLOSEBRACE, Operator.NULL, Operator.VOID,
		     Operator.COMMA, Operator.ACTIVE_COMMA,
		     Operator.NUMBER, Operator.STRING, Operator.INSTANCEOF,
		     Operator.CLASS, Operator.BOOLEAN, Operator.METHOD,
		     Operator.LEAVE_ON_STACK, Operator.PARAMETER,
		     Operator.THROW, Operator.ASSIGN1, Operator.ASSIGN2,
		     Operator.CONSTRUCTOR, Operator.SWAP);

    private static final EnumSet<Operator> notBeforeString
	= EnumSet.of(Operator.FUNCTION_KEYWORD, Operator.NEW,
		     Operator.DOT, Operator.INSTANCEOF,
		     Operator.CBRACE, Operator.CBRACKET, Operator.NUMBER,
		     Operator.NULL, Operator.VOID, Operator.STRING,
		     Operator.CPAREN, Operator.CLASS, Operator.METHOD_REF,
		     Operator.CONSTANT, Operator.BOOLEAN, Operator.METHOD,
		     Operator.VARIABLE, Operator.VARIABLE_NAME,
		     Operator.EXIST_TEST, Operator.PARAMETER, Operator.SWAP);

    private static final EnumSet<Operator> notBeforeOBRACKET
	= EnumSet.of(Operator.DOT, Operator.INSTANCEOF,
		     Operator.CLASS, Operator.SWAP);

    private static final EnumSet<Operator> notBeforeOBRACE
	= EnumSet.of(Operator.DOT, Operator.INSTANCEOF,
		     Operator.CLASS, Operator.SWAP, Operator.CBRACE,
		     Operator.CBRACKET, Operator.OBJCLOSEBRACE,
		     Operator.NEW, Operator.THROW, Operator.VARIABLE_NAME,
		     Operator.EXIST_TEST);


    // for these tokens, when tracing, display the value as well as the name
    private static final EnumSet<Operator> traceValueSet
	= EnumSet.of(Operator.FUNCTION_KEYWORD, Operator.VAR);

    Thread startingThread = Thread.currentThread();
    AtomicReference<Thread> importThreadRef = new AtomicReference<>();
    AtomicLong threadCount = new AtomicLong();
    volatile boolean noImport = false;

    Set<Class<?>> allowedValues = new HashSet<>();
    Set<Class<?>> allowedValuesCache =
	Collections.synchronizedSet(new HashSet<>());
    Set<Class<?>> allowedArgs = new HashSet<>();
    Set<Class<?>> allowedArgsCache =
	Collections.synchronizedSet(new HashSet<>());

    Set<String> simpleClassNames = new HashSet<String>();
    boolean usesMethods = false;

    char[] fullClassNames = null;
    Map<String,Class<?>> classMap = new HashMap<>();
    SuffixArray.Char fcnsa = null;

    void clearFCN() {
	fullClassNames = null;
	fcnsa = null;
    }

    void initFullClassNames() {
	if (fullClassNames == null) {
	    if (fullClassNames != null) return;
	    int sz = 32*(allowedValues.size() + allowedArgs.size());
	    StringBuilder sb = new StringBuilder(sz);
	    sb.append("|");
	    for (Class<?> c: allowedValues) {
		String name = c.getName().replace('$', '.');
		sb.append(name);
		if (!classMap.containsKey(name)) {
		    classMap.put(name, c);
		}
		sb.append("|");
	    }
	    for (Class<?> c: allowedArgs) {
		if (allowedValues.contains(c)) continue;
		String name = c.getName().replace('$', '.');
		sb.append(name);
		if (!classMap.containsKey(name)) {
		    classMap.put(name, c);
		}
		sb.append("|");
	    }
	    char[] fcns = sb.toString().toCharArray();
	    sb.setLength(0);
	    int n = -1;
	    for (char ch: fcns) {
		if (n < ch) n = ch;
	    }
	    n++;
	    if (n > 0) {
		fcnsa = new SuffixArray.Char(fcns, n);
	    }
	    fullClassNames = fcns;
	}
    }

    Class<?> findMethodRefClass(Class<?> clasz, String method, Method fim) {
	if (allowedValues.contains(clasz) || allowedArgs.contains(clasz)
	    || allowedValuesCache.contains(clasz)) {
	    try {
		Method m =
		    clasz.getDeclaredMethod(method, fim.getParameterTypes());
		if (Modifier.isPublic(m.getModifiers())) {
		    return clasz;
		}
	    } catch (NoSuchMethodException e) {
		// fall through
	    }
	}
	if (!clasz.isInterface()) {
	    Class<?> sc = clasz.getSuperclass();
	    if (sc != null) {
		Class<?> c = findMethodRefClass(sc, method, fim);
		if (c != null) {
		    return c;
		}
	    }
	}
	for (Class<?> ic: clasz.getInterfaces()) {
	    Class<?> c = findMethodRefClass(ic, method, fim);
	    if (c != null) {
		return c;
	    }
	}
	return null;
    }

    synchronized Class<?> findClass(String name)
	throws IllegalArgumentException
    {
	initFullClassNames();
	SuffixArray.Range range = fcnsa.findRange((name + "|").toCharArray());
	int rsz = range.size();
	if (rsz == 1) {
	    int index = range.subsequenceIndex(0);
	    char ch = fullClassNames[--index];
	    int end = index + range.subsequenceLength();
	    if (ch != '|' && ch != '.') {
		throw new IllegalArgumentException(errorMsg("noClass", name));
	    }
	    while (ch != '|') {
		ch = fullClassNames[--index];
	    }
	    index++;
	    String fname = new String(fullClassNames, index, end - index);
	    return classMap.get(fname);
	} else if (rsz > 1) {
	    String fname = null;
	    for (int i = 0; i < rsz; i++) {
		int index = range.subsequenceIndex(i);
		char ch = fullClassNames[--index];
		int end = index + range.subsequenceLength();
		if (ch != '|' && ch != '.') {
		    continue;
		} else if (fname != null) {
		    // ambiguous
		    throw new IllegalArgumentException
			(errorMsg("ambiguousClass", name));
		}
		while (ch != '|') {
		    ch = fullClassNames[--index];
		}
		index++;
		fname = new String(fullClassNames, index, end - index);
	    }
	    if (fname == null) {
		// no match.
		throw new IllegalArgumentException(errorMsg("noClass", name));
	    } else {
		return classMap.get(fname);
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("noClass", name));
	}
    }



    // Uses this as a proxy for null so we have something on the stack
    private static final Object NULL = new Object();

    // Use this for a void return value, indicating that it cannot
    // be used as an argument.
    private static final Object VOID = new Object();

    /**
     * Determine if a value is void.
     * Instances of ESPFunction can return a designated value to
     * indicate that no value is actually returned. This method
     * checks an object to see if it is this designated value.
     * @param object the object to test
     * @return true if the object is void; false otherwise
     * @see ESPFunction#isVoid()
     */
    public static final boolean isVoid(Object object) {
	return object == VOID;
    }

    static final Comparator<Class<?>> classComparator =
	new Comparator<>() {
	    public int compare(Class<?> c1, Class<?> c2) {
		String s1 = c1.getName().replace('$', '.');
		String s2 = c2.getName().replace('$', '.');
		return s1.compareTo(s2);
	    }
	};

    private ThreadLocal<UniTreeNode<Map<String,Object>>> argTreeTL
	= new ThreadLocal<>();

    /**
     * Class representing an expression-parser lambda expression
     * The lambda expression is evaluated by calling the method
     * {@link ESPFunction#invoke(Object...)} with the number of
     * arguments specified by {@link ESPFunction#numberOfArguments()}.
     */
    public class ESPFunction {
	String[] args; 		// formal parameters (names)
	String orig;
	ArrayList<LinkedList<Token>> tokenQueues;
	boolean isMethod = false;
	ESPObject thisObject = null;
	boolean willSync = false;
	void willSync() {
	    willSync = true;
	}


	boolean isMethod() {return isMethod;}

	void setAsMethod() {
	    isMethod = true;
	}

	boolean isVoid = false;
	void setAsVoid() {isVoid = true;}

	/**
	 * Determine if the value returned by this function should be ignored.
	 * @return true if the value returned by this function should be
	 *         ignored; false otherwise
	 */
	public boolean isVoid() {return isVoid;}



	void setThisObject(ESPObject object) {
	    thisObject = object;
	}

	ESPObject getThisObject() {return thisObject;}

	UniTreeNode<Map<String,Object>> argTree = argTreeTL.get();

	ESPFunction(String[] args,
		  String orig,
		  ArrayList<LinkedList<Token>> tokenQueues) {
	    this.args = args;
	    this.orig = orig;
	    this.tokenQueues = tokenQueues;
	}

	/**
	 * Get the number of arguments for this lambda expression.
	 * @return the number of arguments that may be used with
	 *         {@link #invoke(Object...)}, excluding an internally
	 *         provided argument that refers to the current object
	 *         when the function implements a method.
	 */
	public int numberOfArguments() {
	    return args.length;
	}

	/**
	 * Call this function.
	 * @param fargs this function's arguments
	 * @return the result of invoking this function
	 * @exception IllegalArgumentException an argument was not appropriate
	 */
	public Object invoke(Object... fargs) throws IllegalArgumentException {
	    int nargs = (fargs == null)? 0: fargs.length;
	    if (nargs != (args.length)) {
		// for methods ignore the first "this" argument that is
		// automatically added. [no longer needed]
		// int nargsm1 = nargs - 1;
		String msg;
		if (isMethod) {
		    msg = errorMsg("wrongNumbArgsM", nargs, args.length);
		} else {
		    msg = errorMsg("wrongNumbArgsF", nargs, args.length);
		}
		throw new IllegalArgumentException(msg);
	    }
	    HashMap<String,Object> argmap = new HashMap<>();

	    if (isMethod) {
		argmap.put("this", thisObject);
	    }
	    for (int i = 0; i < nargs; i++) {
		argmap.put(args[i], fargs[i]);
	    }

	    // Setup global if necessary for the case were a function
	    // was invoked outside of parsing a script.
	    Object ourGlobal = vmap.get().get("global");
	    if (processor.epSingleton != null
		&& ourGlobal != processor.epSingleton) {
		vmap.get().put("global", processor.epSingleton);
	    }
	    try {
		if (willSync) {
		    Object syncObject =  ExpressionParser.this;
		    synchronized (syncObject) {
			return invokeAux(argmap);
		    }
		} else {
		    return invokeAux(argmap);
		}
	    } finally {
		if (processor.epSingleton != null
		    && ourGlobal != processor.epSingleton) {
		    vmap.get().put("global", ourGlobal);
		}
	    }
	}

	private Object invokeAux(Map<String,Object>argmap)
	    throws IllegalArgumentException
	{
	    UniTreeNode<Map<String,Object>> savedArgTree = argTreeTL.get();
	    Thread currentThread = Thread.currentThread();
	    Thread lastThread = null;
	    try {
		UniTreeNode<Map<String,Object>> newArgTree =
		    UniTreeNode.addTo(argmap, argTree);
		argTreeTL.set(newArgTree);

		lastThread = importThreadRef
		    .compareAndExchangeAcquire(null, currentThread);
		threadCount.addAndGet(1);
		if (noImport == false
		    && lastThread != currentThread
		    && lastThread != null) {
		    synchronized (ExpressionParser.this) {
			noImport = true;
		    }
		}
		// processor.pushArgMap(argmap);
		processor.pushArgMap(newArgTree);
		processor.pushBases();
		for (LinkedList<Token> tokenQueue: tokenQueues) {
		    // if a previous statement has left something
		    // on the stack, ignore it.
		    while(processor.hasValue()) {
			// processor.valueStack.pop();
			processor.popValue();
		    }
		    parseExpression(tokenQueue, orig);
		}
		processor.eval(orig);
		if (processor.valueStackEmpty()) {
		    return null;
		} else {
		    // return processor.valueStack.pop();
		    return processor.popValue();
		}
	    } finally {
		//just in case, we'll empty the remaining entries
		while(processor.hasValue()) {
		    processor.popValue();
		}
		processor.popBases();
		processor.popArgMap();
		if (threadCount.addAndGet(-1) == 0) {
		    importThreadRef
			.compareAndExchangeRelease(currentThread, null);
		} else {
		    synchronized (ExpressionParser.this) {
			if (noImport) {
			    // no longer need it.
			    importThreadRef.setRelease(null);
			}
		    }
		}
		argTreeTL.set(savedArgTree);
	    }
	}

	// fi must be a functional interface so there will be only a
	// single method that can be called.
	/**
	 * Convert this function into an implementation of a
	 * functional interface.
	 * @param<T> the type of the functional interface's class.
	 * @param fi the functional interface's class
	 * @return an instance of the functional interface that
	 *         will call this function.
	 */
	public <T> T convert(Class<T>fi) {
	    InvocationHandler handler = new InvocationHandler() {
		    public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable
		    {
			Object result = ESPFunction.this.invoke(args);
			if (result == VOID) {
			    return null;
			} else {
			    return result;
			}
		    }
		};
	    try {
		Object result = AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Object>() {
			    public Object run()
				throws IllegalArgumentException,
				       NullPointerException,
				       ObjectParser.Exception
			    {
				return Proxy.newProxyInstance
				    (fi.getClassLoader(), new Class<?>[] {fi},
				     handler);
			    }
			});
		return fi.cast(result);
	    } catch (PrivilegedActionException ep) {
		java.lang.Exception e = ep.getException();
		if (e instanceof RuntimeException) {
		    throw (RuntimeException) e;
		} else {
		    throw new UnexpectedExceptionError(e);
		}
	    }
	}
    }


    /**
     * ExpressionParser/ESP Object.
     */
    public class ESPObject extends JSObject implements JSOps {

	ESPObject() {
	    super();
	}

	// fi must be an interface.
	/**
	 * Convert this object to one with a specified class
	 * @param<T> the type for the class <CODE>fi</CODE>
	 * @param fi the class to which this object is to be converted
	 * @return an object of type T
	 */
	public <T> T convert(Class<T>fi) {
	    InvocationHandler handler = new InvocationHandler() {
		    public Object invoke(Object proxy, Method m, Object[] args)
			throws Throwable
		    {
			String method = m.getName();
			Object object = ESPObject.this.get(method);
			if (object == null) {
			    String msg = errorMsg("missingMethod", method);
			    throw new IllegalArgumentException(msg);
			}
			if (object instanceof ESPFunction) {
			    ESPFunction f = (ESPFunction) object ;
			    int n1 = f.numberOfArguments();
			    int n2 = m.getParameterCount();
			    if (n1 != n2) {
				String msg =
				    errorMsg("wrongArgCntM", method, n1, n2);
				throw new IllegalArgumentException(msg);
			    }
			    return f.invoke(args);
			    // return f.invoke(args);
			} else {
			    String msg = errorMsg("missingFunct", method);
			    throw new IllegalArgumentException(msg);
			}
		    }
		};
	    return fi.cast(AccessController.doPrivileged
			   (new PrivilegedAction<Object>() {
				   public Object run() {
				       return Proxy.newProxyInstance
					   (fi.getClassLoader(),
					    new Class<?>[] {fi},
					    handler);
				   }
			       }));
	}
    }

    /**
     * ExpressionParser/ESP array.
     */
    public class ESPArray extends JSArray {
	/**
	 * Constructor.
	 */
	ESPArray() {
	    super();
	}

	// ndims is 1 for an array and 2 for a matrix
	ESPArray(Object matrix, int ndims) {
	    super();
	    if (ndims == 1) {
		int n = Array.getLength(matrix);
		for (int i = 0; i < n; i++) {
		    addObject(Array.get(matrix, i));
		}
	    } else if (ndims == 2)  {
		int n = Array.getLength(matrix);
		int m = (n == 0)? 0: Array.getLength(Array.get(matrix, 0));
		for (int i = 0; i < n; i++) {
		    Object row = Array.get(matrix, i);
		    if (row == null) {
			String msg = errorMsg("nullRow", i);
			throw new IllegalArgumentException(msg);
		    } else if (Array.getLength(row) != m) {
			int rlen = Array.getLength(row);
			String msg = errorMsg("wrongRowLen", i, rlen, m);
			throw new IllegalArgumentException(msg);
		    }
		    ESPArray ourRow = new ESPArray();
		    for (int j = 0; j < m; j++) {
			ourRow.addObject(Array.get(row, j));
		    }
		    addObject(ourRow);
		}
	    }
	}

	/**
	 * Convert this object to a Java array.
	 * <P>
	 * Note: this method is used internally by {@link ExpressionParser}.
	 * While it can be called directly, in most cases the methods
	 * {@link JSArray#toArray(Class)}, {@link JSArray#toDoubleArray()},
	 * {@link JSArray#toLongArray()}, {@link JSArray#toIntArray()},
	 * are {@link JSArray#toBooleanArray()} are better choices as these
	 * methods' return types are arrays of the appropriate type.
	 * @param clasz the component type of the array (the primitive
	 *        types double, long, int, and boolean are recognized
	 *        in addition to non-primitive class names)
	 * @return the array
	 */
	public Object toJavaArray(Class<?> clasz) {
	    if (clasz.equals(double.class)) {
		return toDoubleArray();
	    } else if (clasz.equals(long.class)) {
		return toLongArray();
	    } else if (clasz.equals(int.class)) {
		return toIntArray();
	    } else if (clasz.equals(boolean.class)) {
		return toBooleanArray();
	    } else {
		return toArray(clasz);
	    }
	}

	/**
	 *  Create a {@link Stream stream} of a specified type
	 *  backed by this object.
	 *  The argument determines the type of the stream created:
	 *  for
	 *  <UL>
	 *    <LI><STRONG>int.class</STRONG>, the stream is an
	 *      {@link IntStream IntStream}.
	 *    <LI><STRONG>long.class</STRONG>,
	 *      {@link LongStream LongStream}.
	 *    <LI><STRONG>double.class</STRONG>,
	 *      {@link DoubleStream DoubleStream}.
	 *  </UL>
	 *  @param clasz either int.class, long.class, or double.class
	 * @return the stream
	 *  @exception IllegalArgumentException if the argument is not
	 *             a recognized class
	 *  @exception NullPointerException if the argument is null
	 */

	public Object toStream(Class<?> clasz)
	    throws IllegalArgumentException
	{
	    if (clasz == null) {
		throw new NullPointerException(errorMsg("nullArg", 1));
	    } else if (clasz.equals(double.class)) {
		return DoubleStream.of(toDoubleArray());
	    } else if (clasz.equals(long.class)) {
		return LongStream.of(toLongArray());
	    } else if (clasz.equals(int.class)) {
		return IntStream.of(toIntArray());
	    } else {
		throw new IllegalArgumentException(errorMsg("toStreamErr"));
	    }

	}

	/**
	 * Convert this object to a Java  matrix (an n by m Java array).
	 * <P>
	 * Note: this method is used internally by {@link ExpressionParser}.
	 * While it can be called directly, in most cases the methods
	 * {@link JSArray#toMatrix(Class)}, {@link JSArray#toDoubleMatrix()},
	 * {@link JSArray#toLongMatrix()}, {@link JSArray#toIntMatrix()},
	 * and {@link JSArray#toBooleanMatrix()} are better choices as these
	 * methods' return types are matrices (e.g., arrays of arrays)
	 * of the appropriate type.
	 * @param clasz the component type of the array (the primitive
	 *        types double, long, int, and boolean are recognized
	 *        in addition to non-primitive class names)
	 * @return the matrix
	 */
	public Object toJavaMatrix(Class<?> clasz) {
	    if (clasz.equals(double.class)) {
		return toDoubleMatrix();
	    } else if (clasz.equals(long.class)) {
		return toLongMatrix();
	    } else if (clasz.equals(int.class)) {
		return toIntMatrix();
	    } else if (clasz.equals(boolean.class)) {
		return toBooleanMatrix();
	    } else {
		return toMatrix(clasz);
	    }
	}
    }

    private static final class ObjMethodDescriptor {
	String name;
	int nargs;
	ObjMethodDescriptor(String n, int sz) {
	    name = n;
	    nargs = sz;
	}
	@Override
	public boolean equals(Object o) {
	    if (o instanceof ObjMethodDescriptor) {
		ObjMethodDescriptor o1 = (ObjMethodDescriptor)o;
		return o1.name.equals(name) && (o1.nargs == nargs);
	    }
	    return false;
	}
	@Override
	public int hashCode() {
	    return name.hashCode();
	}
    }

    volatile Map<String,Object> gmap = null;

    /**
     * Set Global bindings.
     * Global bindings are provided because of the API used by
     * the package {@link javax.script}.
     * API.
     * @param gmap the global bindings; null to remove them
     */
    public void setGlobalBindings(Map<String,Object> gmap) {
	this.gmap = gmap;
    }

    volatile boolean importsFrozen = false;
    volatile HashSet<Constructor<?>> blockedConstructors = null;
    volatile HashSet<Method> blockedMethods = null;

    // The global object is an instance of this class.
    private class ESP {
	/**
	 * Determine if an object is an ESP array.
	 * @return true if the object is an ESP array, false otherwise
	 */
	public boolean isESPArray(Object obj) {
	    if (obj == null) return false;
	    return obj instanceof ESPArray;
	}

	/**
	 * Determine if an object is a Java array.
	 * @return true if the object is an ESP array, false otherwise
	 */
	public boolean isJavaArray(Object obj) {
	    if (obj == null) return false;
	    return obj.getClass().isArray();
	}

	public boolean isArray(Object obj) {
	    if (obj == null) return false;
	    return (obj instanceof ESPArray) || obj.getClass().isArray();
	}

	/**
	 * Get the type of a Java array whose elements are of a specified
	 * class.
	 * @param clasz the class of an array element
	 * @return the type of an array whose element type is clasz
	 * @exception NullPointerException if clasz is null
	 * @exception IllegalArgumentException if clasz is Void.TYPE
	 */
	public Class<?> typeForArrayOf(Class<?> clasz) {
	    return java.lang.reflect.Array.newInstance(clasz, 0).getClass();
	}

	/**
	 * Determine if an object is an ESP object.
	 * @return true if the object is an ESP array, false otherwise
	 */
	public boolean isObject(Object obj) {
	    if (obj == null) return false;
	    return obj instanceof ESPObject;
	}

	/**
	 * Get the type of an object.
	 * @param obj the object
	 * @return the type of the object
	 */
	public Class<?> typeof(Object obj) {
	    if (obj == null) return null;
	    return obj.getClass();
	}

	/**
	 * Get the type of an ESP object.
	 * @return the type that represents an ESP object.
	 */
	public Class<?> ESPObjectType() {
	    return ESPObject.class;
	}

	/**
	 * Get the type of an ESP array.
	 * @return the type that represents an ESP array.
	 */
	public Class<?> ESPArrayType() {
	    return ESPArray.class;
	}

	/**
	 * Set the value stored in an object given a key.
	 * @param object the name of an object (an ESP array or object
	 *        or a Java array)
	 * @param key the key (an integer for an array)
	 * @param value the value.
	 */
	public Object set(String object, Object key, Object value)
	    throws IllegalArgumentException
	{
	    Object obj = vmap.get().get(object);
	    if (obj == null) {
		throw new
		    IllegalArgumentException(errorMsg("notArrayOrObject"));
	    } else if (key.equals("global")) {
		throw new IllegalArgumentException("modGlobal");
	    } else if (key.equals("this")) {
		throw new IllegalArgumentException("modThis");
	    } else if (obj instanceof ESPArray) {
		return ((ESPArray)obj).set((int)(Integer)key, value);
	    } else if (obj instanceof ESPObject) {
		return ((ESPObject)obj).putObject((String)key, value);
	    } else if (obj.getClass().isArray()) {
		int  ind = (int)(Integer)key;
		Object old = Array.get(obj, ind);
		Array.set(obj, ind, value);
		return old;
	    } else {
		throw new
		    IllegalArgumentException(errorMsg("notArrayOrObject"));
	    }
	}

	/**
	 * Get the value stored in an object given a key.
	 * @param object the name of anobject (an ESP array or object or a
	 *         Java array)
	 * @param key the key (an integer for an array)
	 * @return the value corresponding to the key
	 */
	public Object get(String object, Object key) {
	    Object obj = vmap.get().get(object);
	    if (obj instanceof ESPArray) {
		return ((ESPArray)obj).get((int)(Integer)key);
	    } else if (obj instanceof ESPObject) {
		return ((ESPObject)obj).get((String)key);
	    } else if (obj.getClass().isArray()) {
		    return Array.get(obj, (int)(Integer)key);
	    } else {
		throw new
		    IllegalArgumentException(errorMsg("notArrayOrObject"));
	    }
	}

	/**
	 * Get the size of a ESP array, an ESP object, or an array
	 * @param object  the object
	 * @return the size (or array length)
	 */
	public int size(Object object) {
	    if (object instanceof ESPArray) {
		return ((ESPArray)object).size();
	    } else if (object instanceof ESPObject) {
		return ((ESPObject)object).size();

	    } else if (object.getClass().isArray()) {
		return Array.getLength(object);
	    } else {
		throw new
		    IllegalArgumentException(errorMsg("notArrayOrObject"));
	    }
	}

	/**
	 * Get an object from the global name space.
	 * @param key the name of the object
	 * @return the object corresponding to the key provided by this
	 *         method's argument; null if there is no such object
	 */
	public Object get(String  key) {
	    return vmap.get().get(key);
	}

	/**
	 * Determine if an object exists in the global name space.
	 * @param key the name of the object
	 * @return true if such an object exists; false otherwise
	 */
	public boolean exists(String key) {
	    return vmap.get().containsKey(key)
		|| functNamesThreadLocal.get().contains(key);
	}

	/**
	 * Set an object in the global name space.
	 * @param key the name of the object
	 * @param value the new object
	 * @return the previous object corresponding to the key
	 *         provided by this method's first argument; null if there
	 *         is no such object
	 */
	public void set(String key, Object value) {
	    vmap.get().put(key, value);
	}

	/**
	 * Get the reader for this parser.
	 * This method will first try to find a thread-specific reader
	 * and if there is none, it will use the reader set by the last
	 * call to {@link #setReader(Reader)}, or, if
	 * {@link #setReader(Reader)} has not been called, a reader
	 * configured to read from standard input with the UTF-8
	 * character set.
	 * @return the reader; null if there is none
	 */
	public Reader getReader() {
	    Reader r = tlReader.get();
	    if (r == null) {
		r = reader;
	    }
	    return r;
	}

	/**
	 * Get the writer for this parser.
	 * This method will first try to find a thread-specific writer
	 * and if there is none, it will use a default writer.
	 * <P>
	 * The default writer is the one set by the last call to
	 * {@link #setWriter(Writer)}, or, if {@link #setWriter(Writer)}
	 * has not been called, a {@link PrintWriter} configured to
	 * autoflush, to direct its output to standard output, and to
	 * use the UTF-8 character set.
	 * @return the writer; null if there is none
	 */
	public PrintWriter getWriter() {
	    PrintWriter w = tlWriter.get();
	    if (w == null) {
		w = writer;
	    }
	    return w;
	}

	/**
	 * Get the error writer for this parser.
	 * This method will first try to find a thread-specific writer
	 * and if there is none, it will use a default writer.
	 * <P>
	 * The default errro writer is the one set by the last call to
	 * {@link #setErrorWriter(Writer)} or, if
	 * {@link #setErrorWriter(Writer)} has not been called, a
	 * {@link PrintWriter} configured to autoflush, to direct its
	 * output to standard output, and to use the UTF-8 character
	 * set.
	 * @return the error writer; null if there is none
	 */
	public PrintWriter getErrorWriter() {
	    PrintWriter w = tlErrorWriter.get();
	    if (w == null) {
		w = errorWriter;
	    }
	    return w;
	}

	/**
	 * Create a set of the current global variable/parameter names.
	 * @return a set of the current global variables
	 */
	public Set<String> globals() {
	    Map<String,Object> map = gmap;
	    return (map == null)? Collections.emptySet():
		Collections.unmodifiableSet(gmap.keySet());
	}

	/**
	 * Get the value associated with a specified global variable or
	 * parameter.
	 * @param key the global variable/parameter
	 * @return the value of for the global variable specified by key;
	 *         null if there is no value in the tables or if no table
	 *         was configured
	 */
	public Object getGlobal(String key) {
	    Map<String,Object> map = gmap;
	    return (map == null)? null: map.get(key);
	}

	/**
	 * Get the value associated with a specified global variable or
	 * parameter.
	 * @param key the global variable/parameter
	 * @param defaultObject the object to return if there there is
	 *        no value associated with the specified key
	 * @return the value of for the global variable specified by key;
	 *         defaultObject if there is no value in the tables
	 */
	public Object getGlobal(String key, Object defaultObject) {
	    Map<String,Object> map = gmap;
	    return (map == null)? defaultObject:
		(map.containsKey(key)? map.get(key): defaultObject);
	}

	private volatile int importCount = 0;

	/**
	 * Import classes.
	 * This is equivalent to the ESP import statement and is
	 * provided to allow
	 * {@link org.bzdev.scripting.ExtendedScriptingContext#importClass}
	 * and
	 * {@link org.bzdev.scripting.ExtendedScriptingContext#importClasses}
	 * to be used to import classes.
	 * <P>
	 * After a sequence of calls to this method, the method
	 * {@link #importFinished()} must be called.
	 * @param pkg the package name; null or an empty string
	 *        for an unnamed package
	 * @param spec either a string providing a class name
	 *        name or an ESP array of strings, each a class name
	 * @return the value null
	 * @exception IllegalArgumentException there was a syntax error
	 *            in the arguments
	 * @exception IllegalStateException adding additional classes is
	 *            not allowed given the current state of the
	 *            expression parser
	 */
	public synchronized Object importClasses(String pkg, Object spec)
	    throws IllegalArgumentException, IllegalAccessException,
		   IllegalStateException
	{
	    if (!(processor.scriptingMode && processor.importMode
		  && scriptImportAllowed) || importsFrozen) {
		throw new IllegalStateException(errorMsg("noImport"));
	    }
	    int prevImportCount = importCount;
	    Thread currentThread = Thread.currentThread();
	    Thread lastThread = null;
	    try {
		lastThread = importThreadRef
		    .compareAndExchangeAcquire(null, currentThread);
		threadCount.addAndGet(1);
		if (noImport == false &&
		    lastThread != currentThread
		    && lastThread != null) {
		    synchronized (ExpressionParser.this) {
			noImport = true;
		    }
		}
		if (noImport) {
		    throw new IllegalStateException(errorMsg("noImport"));
		}
		ExpressionParser.this.importClasses(pkg, spec);
		importCount++;
		return null;
	    } finally  {
		if (importCount == prevImportCount) {
		    if (threadCount.addAndGet(-(prevImportCount+1)) == 0) {
			importThreadRef
			    .compareAndExchangeRelease(currentThread, null);
		    } else {
			synchronized (ExpressionParser.this) {
			    if (noImport) {
				// no longer need it.
				importThreadRef.setRelease(null);
			    }
			}
		    }
		}
	    }
	}

	/**
	 * Finish a sequence of imports.
	 * This method must be called after a sequence of one or moe
	 * calls to {@link #importClasses(String,Object)}. It causes
	 * a sequence of {@link #importClasses(String,Object)} calls
	 * to be processed as a group, so that all the classes are
	 * allowed as arguments and return values when method tables are
	 * built.
	 * @return the value null
	 * @exception IllegalAccessException a requested class, method, or
	 *            field could not be accessed
	 * @exception IllegalStateException adding additional classes is
	 *            not allowed given the current state of the
	 *            expression parser
	 */
	public synchronized Object finishImport()
	    throws IllegalArgumentException, IllegalAccessException,
		   IllegalStateException
	{
	    if (importCount == 0) return null;
	    int prevImportCount = importCount;
	    Thread currentThread = Thread.currentThread();
	    Thread lastThread = null;
	    try {
		lastThread = importThreadRef
		    .compareAndExchangeAcquire(null, currentThread);
		threadCount.addAndGet(1);
		if (noImport == false &&
		    lastThread != currentThread && lastThread != null) {
		    synchronized (ExpressionParser.this) {
			noImport = true;
		    }
		}
		if (noImport) {
		    throw new IllegalStateException(errorMsg("noImport"));
		}
		ExpressionParser.this.finishImport();
		importCount = 0;
		return null;
	    } finally {
		if (importCount == 0) {
		    if (threadCount.addAndGet(-(prevImportCount+1)) == 0) {
			importThreadRef
			    .compareAndExchangeRelease(currentThread, null);
		    } else {
			synchronized (ExpressionParser.this) {
			    if (noImport) {
				// no longer need it.
				importThreadRef.setRelease(null);
			    }
			}
		    }
		}
	    }
	}

	/**
	 * Create a new Java array.
	 * The maximum number of dimensions is 255 (a limit set by java).
	 * @param clasz the class of the array's components
	 * @param dimensions the dimensions of the array
	 */
	public Object newJavaArray(Class<?> clasz, int... dimensions) {
	    return Array.newInstance(clasz, dimensions);
	}

	private TemplateProcessor.KeyMapList
	    getClassList(TemplateProcessor.KeyMapList list)
	{
	    TemplateProcessor.KeyMapList clist = new
		TemplateProcessor.KeyMapList();
	    String cname = null;
	    for (TemplateProcessor.KeyMap kmap: list) {
		if (!kmap.get("class").equals(cname)) {
		    cname = (String)kmap.get("class");
		    kmap.put("title", cname);
		    TemplateProcessor.KeyMap kmap2 =
			new TemplateProcessor.KeyMap();
		    kmap2.put("class", cname);
		    clist.add(kmap2);
		}
	    }
	    return clist;
	}

	/**
	 * Generate documentation for imported classes and methods.
	 * The first argument will typically be created on the command
	 * line when scrunner is used to run this script.  The second will
	 * usually be an ESP array, all of whose elements are strings. These
	 * strings will be URLs.  For example,
	 * <BLOCkQUOTE><PRE><CODE>
	 *  global.generateDocs(w, ["/usr/share/doc/libbzdev-doc/api",
                                    "/usr/share/doc/librdanim-doc/api"]);
	 * </CODE></PRE></BLOCKQUOTE>
	 * <P>
	 * The 'docs' arguments is a list of strings.  Each string
	 * can contain multiple URLS or file names, with '|' used
	 * as a separator for URLs or filenames.  A sequence of n "|"
	 * characters will be replaced by floor(n/2) "|" characters
	 * and if n is odd, the final "|" is treated as the separator.
	 * As a result, except when it is the first component, a file
	 * name cannot start with "|".  If it is really necessary to
	 * have a "|" at the start of a file name, use a "file" URL
	 * (in which case the "|" should be encoded as "%7C".)  If a
	 * path component's initial characters, up to and including
	 * the first ":" are syntactically valid as part of a URL, the
	 * component is interpreted as a URL, not a file name.
	 *  <P>
	 * In addition, a leading "~", followed by either the standard
	 * file-separator character or "/", is replaced with the
	 * user's home directory. The sequence "~~" at the start of a
	 * component will be replaced by a single "~".  All subsequent
	 * "~" in that component are left as is.
	 * @param w a writer to record the documentation
	 * @param docs a  string giving the URLs for each API's
	 *        documentation, with '|' separating URLs and '~' used to
	 *        indicate home directories
	 * @exception IllegalArgumentException an element of the second
	 *            argument is not a string
	 * @exception IOException an IO error occurred
	 * @exception MalformedURLException a URL was malformed
	 * @see org.bzdev.net.URLPathParser
	 */
	public Object generateDocs(PrintWriter w, JSArray docs)
	    throws IllegalArgumentException, IOException,
		   MalformedURLException
	{
	    LinkedList<URL> apis = new LinkedList<URL>();
	    for (Object o: docs) {
		if (o instanceof String) {
		    URL[] apiURLs = null;
		    try {
			apiURLs = AccessController.doPrivileged
			    (new PrivilegedExceptionAction<URL[]>() {
				    public URL[] run()
					throws MalformedURLException
				    {
					return URLPathParser.getURLs(null,
								     (String) o,
								     null,
								     null);
				    }
				});
		    } catch (PrivilegedActionException ep) {
			java.lang.Exception e = ep.getException();
			if (e instanceof MalformedURLException) {
			    throw (MalformedURLException) e;
			}
		    }
		    if (apiURLs == null) {
			apiURLs = new URL[0];
		    }
		    for (URL url: apiURLs) {
			apis.add(url);
		    }
		} else {
		    String msg = errorMsg("expectedString2A");
		    throw new IllegalArgumentException(msg);
		}
	    }
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			    public Void run() throws IOException {
				createAPIMap(apis);
				return (Void) null;
			    }
			});
	    } catch (PrivilegedActionException ep1) {
		java.lang.Exception e = ep1.getException();
		if (e instanceof IOException) {
		    throw (IOException) e;
		}
	    }
	    TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	    kmap.put("retList", keylistForReturnClasses());
	    kmap.put("argList", keylistForArgumentClasses());
	    TemplateProcessor.KeyMapList
		list = keylistForConstructors();
	    kmap.put("constrClasses", getClassList(list));
	    kmap.put("constrList", list);
	    list = keylistForFunctions();
	    kmap.put("functClasses", getClassList(list));
	    kmap.put("functList", list);
	    list = keylistForMethods();
	    kmap.put("methodClasses", getClassList(list));
	    kmap.put("methodList", list);
	    kmap.put("constList", keylistForConstants());
	    String resource = errorMsg("docResource");
	    TemplateProcessor tp = new TemplateProcessor(kmap);
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			    public Void run() throws IOException {
				tp.processSystemResource(resource, "UTF-8", w);
				return (Void)null;
			    }
			});
	    } catch (PrivilegedActionException e) {
		Throwable t = e.getCause();
		if (t instanceof IOException) {
		    throw (IOException) t;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, t);
		}
	    }
	    return VOID;
	}

	public Object blockImports() {
	    importsFrozen = true;
	    return null;
	}

	public synchronized Object blockConstructor(Class<?> clasz,
						    Class<?>... argumentClasses)
	    throws IllegalStateException, NullPointerException,
		   NoSuchMethodException
	{
	    if (importsFrozen == true) {
		throw new IllegalStateException(errorMsg("noImport"));
	    }
	    if (clasz == null) {
		throw new NullPointerException(errorMsg("nullArg", 1));
	    }
	    for (int i = 0; i < argumentClasses.length; i++) {
		if (argumentClasses[i] == null) {
		    throw new NullPointerException(errorMsg("nullArg", i+2));
		}
	    }
	    Constructor<?> c = clasz.getConstructor(argumentClasses);
	    if (blockedConstructors == null) {
		blockedConstructors = new HashSet<Constructor<?>>();
	    }
	    blockedConstructors.add(c);
	    return null;
	}

	public synchronized Object blockMethod(Class<?> clasz, String name,
					       Class<?>... argumentClasses)
	    throws IllegalStateException, NullPointerException,
		   NoSuchMethodException

	{
	    if (importsFrozen == true) {
		throw new IllegalStateException(errorMsg("noImport"));
	    }
	    if (clasz == null) {
		throw new NullPointerException(errorMsg("nullArg", 1));
	    }
	    if (name == null) {
		throw new NullPointerException(errorMsg("nullArg", 2));
	    }
	    for (int i = 0; i < argumentClasses.length; i++) {
		if (argumentClasses[i] == null) {
		    throw new NullPointerException(errorMsg("nullArg", i+3));
		}
	    }
	    Method m = clasz.getMethod(name, argumentClasses);
	    if (blockedMethods == null) {
		blockedMethods = new HashSet<Method>();
	    }
	    blockedMethods.add(m);
	    return null;
	}

	/*
	public synchronized Object blockScriptingCreate() {
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(ExtendedScriptingContext.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class);
	    blockMethod(String.class,
			"create", Class.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class,
			Object.class, Object.class, Object.class);
	    return null;
	}
	*/
    }

    private static final HashMap<ObjMethodDescriptor, Method> epobjMethods
	= new HashMap<>();
    static {
	try {
	    epobjMethods.put(new ObjMethodDescriptor("hasProperty", 1),
			     ESPObject.class.getMethod("containsKey",
						      String.class));
	    epobjMethods.put(new ObjMethodDescriptor("get", 1),
			     ESPObject.class.getMethod("get", String.class));
	    epobjMethods.put(new ObjMethodDescriptor("propertyNames", 0),
			     ESPObject.class.getMethod("keySet"));
	    epobjMethods.put(new ObjMethodDescriptor("properties", 0),
			     ESPObject.class.getMethod("entrySet"));
	    epobjMethods.put(new ObjMethodDescriptor("put", 2),
			     ESPObject.class.getMethod("put",
						      String.class,
						      Object.class));
	} catch (NoSuchMethodException nsme) {
	    throw new UnexpectedExceptionError(nsme);
	}
    }

    private static final HashMap<ObjMethodDescriptor,Method> eparrayMethods
	= new HashMap<>();
    static {
	try {
	    eparrayMethods.put(new ObjMethodDescriptor("get", 1),
			       ESPArray.class.getMethod("get", int.class));
	    eparrayMethods.put(new ObjMethodDescriptor("set", 2),
			       ESPArray.class.getMethod("setObject",
						       int.class,
						       Object.class));
	    eparrayMethods.put(new ObjMethodDescriptor("add", 1),
			       ESPArray.class.getMethod("addObject",
						       Object.class));
	    eparrayMethods.put(new ObjMethodDescriptor("size", 0),
			       ESPArray.class.getMethod("size"));
	    eparrayMethods.put(new ObjMethodDescriptor("stream", 0),
			       ESPArray.class.getMethod("stream"));
	    eparrayMethods.put(new ObjMethodDescriptor("parallelStream", 0),
			       ESPArray.class.getMethod("parallelStream"));
	    eparrayMethods.put(new ObjMethodDescriptor("forEach", 1),
			       ESPArray.class.getMethod("forEach",
						       Consumer.class));
	    eparrayMethods.put(new ObjMethodDescriptor("toArray", 0),
			       ESPArray.class.getMethod("toArray"));
	    eparrayMethods.put(new ObjMethodDescriptor("toArray", 1),
			       ESPArray.class.getMethod("toJavaArray",
							 Class.class));
	    eparrayMethods.put(new ObjMethodDescriptor("toStream", 1),
			       ESPArray.class.getMethod("toStream",
							 Class.class));
	    /*
	    eparrayMethods.put(new ObjMethodDescriptor("toDoubleArray", 0),
			       ESPArray.class.getMethod("toDoubleArray"));
	    eparrayMethods.put(new ObjMethodDescriptor("toLongArray", 0),
			       ESPArray.class.getMethod("toLongArray"));
	    eparrayMethods.put(new ObjMethodDescriptor("toIntArray", 0),
			       ESPArray.class.getMethod("toIntArray"));
	    */
	    eparrayMethods.put(new ObjMethodDescriptor("toMatrix", 0),
			       ESPArray.class.getMethod("toMatrix"));
	    eparrayMethods.put(new ObjMethodDescriptor("toMatrix", 1),
			       ESPArray.class.getMethod("toJavaMatrix",
							 Class.class));
	    /*
	    eparrayMethods.put(new ObjMethodDescriptor("toDoubleMatrix", 0),
			       ESPArray.class.getMethod("toDoubleMatrix"));
	    eparrayMethods.put(new ObjMethodDescriptor("toLongMatrix", 0),
			       ESPArray.class.getMethod("toLongMatrix"));
	    eparrayMethods.put(new ObjMethodDescriptor("toIntMatrix", 0),
			       ESPArray.class.getMethod("toIntMatrix"));
	    */
	} catch (NoSuchMethodException nsme) {
	    throw new UnexpectedExceptionError(nsme);
	}
    }


    static Class<?> streamClasses[] = {
	DoubleStream.class,
	IntStream.class,
	LongStream.class,
	Stream.class,
    };

    static Class<?> streamSupportClasses[] = {
	Collector.class,
	Consumer.class,
	BinaryOperator.class,
	BiConsumer.class,
	Predicate.class,
	Supplier.class,
	Function.class,
	UnaryOperator.class,

	DoubleBinaryOperator.class,
	DoubleConsumer.class,
	DoublePredicate.class,
	DoubleSupplier.class,
	DoubleFunction.class,
	DoubleToIntFunction.class,
	DoubleToLongFunction.class,
	DoubleUnaryOperator.class,

	IntBinaryOperator.class,
	IntConsumer.class,
	IntPredicate.class,
	IntSupplier.class,
	IntToDoubleFunction.class,
	IntFunction.class,
	IntToLongFunction.class,
	IntUnaryOperator.class,

	LongBinaryOperator.class,
	LongConsumer.class,
	LongFunction.class,
	LongPredicate.class,
	LongSupplier.class,
	LongToIntFunction.class,
	LongToDoubleFunction.class,
	LongUnaryOperator.class,

	ToDoubleBiFunction.class,
	ToDoubleFunction.class,
	ToIntBiFunction.class,
	ToIntFunction.class,
	ToLongBiFunction.class,
	ToLongFunction.class,

	Collectors.class,
	Optional.class,
	OptionalDouble.class
    };

    static HashMap<Class<?>,HashMap<String,Method>> streamTable
	= new HashMap<>();

    private void initStreamTable(ExpressionProcessor processor)
	throws IllegalAccessException
    {
	for (Class<?> clasz: streamClasses) {
	    HashMap<String,Method> hmap = new HashMap<>();
	    for (Method m: clasz.getMethods()) {
		if (m.isVarArgs()) continue;
		if (m.isSynthetic()) continue;
		String key = m.getName() + ":" + m.getParameterCount();
		hmap.put(key, m);
		for (Class<?> c: m.getParameterTypes()) {
		    allowedArgs.add(c);
		    allowedValues.add(c);
		    simpleClassNames.add(c.getSimpleName());
		}
	    }
	    streamTable.put(clasz, hmap);
	    simpleClassNames.add(clasz.getSimpleName());
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			    public Void run() throws IllegalAccessException {
				processor.addMethods(clasz, true);
				return (Void) null;
			    }
			});
	    } catch (PrivilegedActionException ep) {
		java.lang.Exception e = ep.getException();
		if (e instanceof IllegalAccessException) {
		    throw (IllegalAccessException) e;
		} else if (e instanceof RuntimeException) {
		    throw (RuntimeException) e;
		}
	    }
	}
	/*
	for (Class<?> clasz: streamSupportClasses) {
	    addClass(processor, clasz);
	}
	*/
	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
			public Void run() throws IllegalAccessException {
			    addClasses(processor, streamSupportClasses);
			    return (Void) null;
			}
		    });
	} catch (PrivilegedActionException ep) {
	    java.lang.Exception e = ep.getException();
	    if (e instanceof IllegalAccessException) {
		throw (IllegalAccessException) e;
	    } else if (e instanceof RuntimeException) {
		throw (RuntimeException) e;
	    }
	}
    }

    private Method findStreamMethod(String mname, Object target,
				    Object[] args)
    {
	Class<?> clasz = target.getClass();
	HashMap<String,Method> hmap = null;
	String key = mname + ":" + args.length;
	if (target instanceof DoubleStream) {
	    hmap = streamTable.get(DoubleStream.class);
	} else if (target instanceof IntStream) {
	    hmap = streamTable.get(IntStream.class);
	} else if (target instanceof LongStream) {
	    hmap = streamTable.get(LongStream.class);
	} else if (target instanceof Stream) {
	    hmap = streamTable.get(Stream.class);
	} else {
	    return null;
	}
	return hmap.get(key);
    }

    private HashMap<Integer,HashMap<Class<?>,Class<?>>> camap =	new HashMap<>();

    private Class<?> getArrayClass(Class<?>c) {
	HashMap<Class<?>,Class<?>> map = camap.get(0);
	if (map == null) {
	    map = new HashMap<Class<?>,Class<?>>();
	    camap.put(0, map);
	}
	Class<?> result = map.get(c);
	if (result == null) {
	    result = Array.newInstance(c, 0).getClass();
	    map.put(c, result);
	}
	return result;
    }

    private Class<?> getArrayClass(Class<?>c, int depth) {
	HashMap<Class<?>,Class<?>> map = camap.get(depth);
	if (map == null) {
	    map = new HashMap<Class<?>,Class<?>>();
	    camap.put(depth, map);
	}
	Class<?> result = map.get(c);
	if (result == null) {
	    result = c;
	    for (int i = 0; i < depth; i++) {
		result = Array.newInstance(result, 0).getClass();
	    }
	    map.put(c, result);
	}
	return result;
    }

    private int getArrayDepth(Class<?> c) {
	int cnt = 0;
	if (c.isArray()) {
	    cnt++;
	    Class<?> cc = c.getComponentType();
	    while (cc.isArray()) {
		cc = c.getComponentType();
		cnt++;
	    }
	}
	return cnt;
    }

    private Class<?> getLastComponentType(Class<?> c) {
	while (c.isArray()) {
	    c = c.getComponentType();
	}
	return c;
    }

    private boolean allowedArrayComponent(Class<?>c, boolean useArgs) {
	if (c.isArray()) {
	    c = getLastComponentType(c);
	}
	int cmod = c.getModifiers();
	boolean isEnum = c.isEnum()
	    && Modifier.isPublic(cmod)
	    && (Modifier.isStatic(cmod)
		|| c.getEnclosingClass() == null);
        return (c.equals(int.class)
		|| c.equals(long.class)
		|| c.equals(double.class)
		|| c.equals(boolean.class)
		|| c.equals(Integer.class)
		|| c.equals(Long.class)
		|| c.equals(Double.class)
		|| c.equals(Boolean.class)
		|| c.equals(String.class)
		|| isEnum
		|| (useArgs? allowedArgs.contains(c):
		    allowedValues.contains(c)));
    }

    private Class<?> explicitClasses[] = {
	String.class,
	Number.class,
	Double.class,
	Integer.class,
	Long.class,
	Boolean.class,
	Reader.class,
	PrintWriter.class,
	// Sets, lists, etc. use Collection to create streams
	Collection.class,
	// need this because we have to handle sets of this type for
	// iteration
	Map.Entry.class,
    };

    /**
     * Interface implemented by ExpressionParser objects that represent
     * a method reference.
     */
    public interface ESPMethodReference {
	/**
	 * Call this method reference's method.
	 * A method reference is represented by ExpressionParser as an
	 * object, The invoke method calls the referenced method:
	 * <UL>
	 *   <LI> For the case CLASSNAME::METHOD where the method is static,
	 *        that static method is called with all of the arguments
	 *   <LI> For the case CLASSNAME::METHOD where the method is an
	 *        instance method, the first argument is treated as the
	 *        object to which the method will be applied, with the
	 *        remaining arguments as the method's arguments.
	 *   <LI> For the case OBJECT::METHOD, the method must be an instance
	 *        method for that object, and this method will be called with
	 *        the specified arguments.
	 * </UL>
	 * @param fargs this function's arguments
	 * @return the value produced by invoking this function.
	 * @exception IllegalArgumentException an argument was not appropriate
	 * @exception IllegalAccessException a class or method was not
	 *            accessible
	 * @exception NoSuchMethodException the method could not be found
	 * @exception InvocationTargetException the operation failed when
	 *            the method or constructor was being executed
	 * @exception InstantiationException a constructor failed
	 */
	Object invoke(Object... fargs) throws IllegalArgumentException,
					      NoSuchMethodException,
					      InvocationTargetException,
					      IllegalAccessException,
					      InstantiationException;

	/**
	 * Convert this object to a functional interface.
	 * @param <T> the type for the class <CODE>functionalInterface</CODE>
	 * @param functionalInterface the class for the functional interface
	 * @return an object that implements the functional interface
	 */
	<T> T convert(Class<T> functionalInterface);
    }

    private static Method methodMatch(Class<?> c, Method m) {
	try {
	    if (!Modifier.isPublic(c.getModifiers())) return null;
	    Method mm = c.getMethod(m.getName(), m.getParameterTypes());
	    if (Modifier.isPublic(mm.getModifiers())) {
		return mm;
	    } else {
		return null;
	    }
	} catch (NoSuchMethodException e) {
	    return null;
	}
    }

    // Needed because you can't invoke a method when its
    // declaring class is not public (even if the method itself
    // is declared to be public).  Curiously, you can call the
    // method from compiled Java code and that works.
    private static Method findBestMethod(Method m)
	throws NoSuchMethodException
    {
	Class<?> c = m.getDeclaringClass();
	Method mm = findBestMethod(c, m);
	if (mm == null) {
	    String cname = c.getName();
	    String mname = m.getName();
	    String msg = errorMsg("noUseableMethod", cname, mname);
	    throw new NoSuchMethodException(msg);
	}
	return mm;
    }

    private static Method findBestMethod(Class<?> c, Method m) {
	Method mm;
	if ((mm = methodMatch(c, m)) != null) {
	    return mm;
	}
	if (!c.isInterface()) {
	    Class<?> sc = c.getSuperclass();
	    if (sc != null) {
		mm = findBestMethod(sc, m);
		if (mm != null) return mm;
	    }
	}
	for (Class<?> ic: c.getInterfaces()) {
	    mm = findBestMethod(ic, m);
	    if (mm != null) {
		return mm;
	    }
	}
	return null;
    }

    static class TypedNull {
	Class<?> clasz;
	TypedNull(Class<?> c) {clasz = c;}
	Class<?> getType() {return clasz;}
	Object getValue() {return null;}
    }


    private class ExpressionProcessor {

	ExpressionProcessor(Class<?>[] rclasses, Class<?>[] fclasses,
			    Class<?>[] mclasses, Class<?>[] fieldClasses)
	    throws IllegalAccessException
	{
	    initStreamTable(this);
	    addConstructors(String.class);
	    for (Class<?> c: explicitClasses) {
		allowedValues.add(c);
		allowedArgs.add(c);
		// no inner classes to worry about
		simpleClassNames.add(c.getName());
	    }
	    // so these 3 types can be used in constructors, and
	    // as arguments to functions and methods.
	    allowedArgs.add(ESPObject.class);
	    allowedArgs.add(ESPArray.class);
	    allowedArgs.add(ESPFunction.class);
	    if (rclasses != null) {
		for (Class<?> c: rclasses) {
		    if (c.isArray()) {
			String msg = errorMsg("noArray", c.getName());
			throw new IllegalAccessException(msg);
		    }
		    addConstructors(c);
		}
	    }
	    for (Class<?> c: explicitClasses) {
		addMethods(c, true);
	    }
	    if (fclasses != null) {
		for (Class<?> c: fclasses) {
		    if (c.isArray()) {
			String msg = errorMsg("noArray", c.getName());
			throw new IllegalAccessException(msg);
		    }
		    addMethods(c, true);
		}
	    }
	    // addMethods(String.class, false);
	    // addMethods(Reader.class, false);
	    // addMethods(PrintWriter.class, false);
	    for (Class<?> c: explicitClasses) {
		addMethods(c, false);
	    }

	    if (mclasses != null) {
		for (Class<?> c: mclasses) {
		    if (c.isArray()) {
			String msg = errorMsg("noArray", c.getName());
			throw new IllegalAccessException(msg);
		    }
		    addMethods(c, false);
		}
	    }

	    if (fieldClasses != null) {
		for (Class<?> c: fieldClasses) {
		    if (c.isArray()) {
			String msg = errorMsg("noArray", c.getName());
			throw new IllegalAccessException(msg);
		    }
		    addFields(c);
		}
	    }
	    processEnumsAndFields();
	}

	boolean scriptingMode = false;

	boolean importMode = false;
	boolean frozen = false;


	/**
	 * Set scripting mode.
	 * If scripting mode is not set, each call to 'parse' must
	 * contain a single statement that can start with the
	 * keywords 'var' or 'function', or the operator '='.
	 * This allows an inexpensive test to determine that a
	 * string should not be parsed.
	 * If scripting mode is set, the previous constraints do
	 * not apply: multiple statements can appear in the same
	 * string, separated by semicolons. Setting scripting mode
	 * allows {@link ExpressionParser this class} to be used
	 * to help implement the ESP scripting language.
	 * @exception IllegalStateException this method was called
	 *            after expression-parsing started.
	 */
	public void setScriptingMode() throws IllegalStateException {
	    /*
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new ExpressionParserPermission
				   ("org.bzdev.util.ExpressionParser"));
	    }
	    */
	    if (frozen) throw new IllegalStateException
			    (errorMsg("scriptingModeTooLate"));
	    scriptingMode = true;
	}


	/**
	 * Set Import mode.
	 * If import mode is turned on, classes can be added after
	 * expressions are evaluated.
	 * @exception IllegalStateException this method was called
	 *            after expression-parsing started.
	 */
	public void setImportMode() throws IllegalStateException {
	    /*
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new ExpressionParserPermission
				   ("org.bzdev.util.ExpressionParser"));
	    }
	    */
	    if (frozen) throw new IllegalStateException
			    (errorMsg("importModeTooLate"));
	    importMode = true;
	}

	ESP epSingleton = null;

	/**
	 * Set the global-object mode.
	 * A variable named "global" will be added automatically and
	 * cannot be changed.
	 * @exception IllegalStateException this method was called
	 *            after expression-parsing started.
	 */
	public void setGlobalMode()  throws IllegalStateException {
	    /*
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new ExpressionParserPermission
				   ("org.bzdev.util.ExpressionParser"));
	    }
	    */
	    if (frozen) throw new IllegalStateException
			    (errorMsg("globalModeTooLate"));
	    if (epSingleton == null) {
		epSingleton = new ESP();
		vmap.get().put("global", epSingleton);
	    }
	}

	public ESPMethodRef createMethodRef(Class<?> clasz, String method) {
	    return new ESPMethodRef(clasz, method);
	}

	public ESPMethodRef createMethodRef2(Object object, String method)
	{
	    Class<?> clasz = object.getClass();
	    return new ESPMethodRef(clasz, method, object);
	}

	class ESPMethodRef implements ESPMethodReference {
	    private Class<?> clasz;
	    private String method;
	    private Object object = null;
	    int nargs = -2;

	    public int numberOfArguments() {
		if (nargs < -1) {
		    synchronized(ExpressionParser.this) {
			AccessController.doPrivileged
			    (new PrivilegedAction<Void>() {
				    public Void run() {
					Method[] methods =
					    clasz.getMethods();
					for (Method meth: methods) {
					    if (meth.getName().equals(method)) {
						nargs =
						    meth.getParameterCount();
						break;
					    }
					}
					return (Void) null;
				    }
				});
		    }
		}
		return nargs;
	    }

	    ESPMethodRef(Class<?> clasz, String method) {
		this.clasz = clasz;
		this.method = method;
	    }

	    ESPMethodRef(Class<?> clasz, String method, Object object) {
		this(clasz, method);
		this.object = object;
	    }

	    private boolean methodRefMatch1(Class<?> c) {
		if (c.equals(double.class)) {
		    c = Number.class;
		} else if (c.equals(long.class)) {
		    c = Long.class;
		    if (clasz.equals(Integer.class)) return true;
		} else if (c == int.class) {
		    c = Integer.class;
		}
		return c.isAssignableFrom(clasz);
	    }

	    private Constructor
		constrRefMatch(MethodInfo minfo, boolean isVarArgs,
			       ClassArraySorter.Key fkey,
			       ClassArraySorter.Key key)
	    {
		if (/*fkey.isAssignableFrom(key)*/key.isAssignableFrom(fkey)) {
		    Constructor c =  minfo.getConstructor(clasz, key);
		    return c;
		} else {
		    return null;
		}
	    }

	    private void modifyCarray(Class<?>[] carray) {
		for (int i = 0; i < carray.length; i++) {
		    if (carray[i] == null) {
			continue;
		    } else if (carray[i].equals(Double.class)) {
			carray[i] = Number.class;
		    }
		}
	    }

	    @Override
	    public Object invoke(Object... fargs)
		throws IllegalAccessException, IllegalArgumentException,
		       InvocationTargetException, NoSuchMethodException,
		       InstantiationException
	    {
		if (method.equals("new")) {
		    return doConstr(clasz.getName(), fargs);
		}
		Method m = null;
		if (object != null) {
		    m = doCall(method, fargs, object);
		    return doCall(method, m, fargs, object);
		}
		try {
		    m = doCall(clasz.getName() +"." + method, fargs, null);
		    return doCall(method, m, fargs, null);
		} catch (InvocationTargetException ite) {
		    throw ite;
		} catch(java.lang.Exception e) {
		    if (fargs.length == 0) throw e;
		    Object[] args = new Object[fargs.length - 1];
		    System.arraycopy(fargs, 1, args, 0, args.length);
		    m = doCall(method, args, fargs[0]);
		    return doCall(method, m, args, fargs[0]);
		}
	    }

	    // fi must be a functional interface so there will be only a
	    // single method that can be called.
	    @Override
	    public <T> T convert(Class<T>fi) {
		Method m = null;
		Constructor c = null;
		boolean isStat = true;
		if (fi.getDeclaredAnnotation(FunctionalInterface.class)
		    != null) {
		    Method fim = null;
		    for (Method mm: fi.getMethods()) {
			if (mm.isDefault()) continue;
			if (mm.isSynthetic()) continue;
			int mods = mm.getModifiers();
			if ((mods & Modifier.STATIC) == 0
			    && (mods & Modifier.PUBLIC) != 0) {
			    fim = mm;
			    break;
			}
		    }
		    if (fim == null) {
			String msg =
			    errorMsg("noSuitableFIMethod", fi.getName());
			throw new IllegalArgumentException(msg);
		    }
		    if (object != null) {
			// no need to synchronize because this is case
			// occurs in the same thread that created this
			// method reference
			/*
			System.out.println("++++ clasz = " + clasz);
			System.out.println("++++ method = " + method);
			System.out.println("++++ fim = " + fim);
			*/
			Class<?> nclasz =
			    findMethodRefClass(clasz, method, fim);
			if (nclasz == null) {
			    StringBuilder sb = new StringBuilder();
			    boolean ft = true;
			    for (Class<?> pc: fim.getParameterTypes()) {
				if (ft) {
				    ft = false;
				} else  {
				    sb.append(",");
				}
				sb.append(pc.getName());
			    }
			    if (fim.isVarArgs()) sb.append("...");
			    String cn = clasz.getName().replace('$', '.');
			    String parms = sb.toString();
			    String msg =errorMsg("noMRClass",cn,method,parms);
			    throw new IllegalStateException(msg);
			}
			// If we switch to nclasz, we need nclasz impoorted,
			// which makes method references work differenttly
			// then lambda expressions doing the same thing.
			// clasz = nclasz;
		    }
		    boolean isVarArgs = fim.isVarArgs();
		    Class<?>[] carray = fim.getParameterTypes();
		    modifyCarray(carray);
		    /*
		    System.out.print("carray: ");
		    for (int iii = 0; iii < carray.length; iii++) {
			System.out.print(carray[iii]);
		    }
		    System.out.println();
		    */
		    ClassArraySorter.Key fkey =
			new ClassArraySorter.Key(carray);
		    Class<?> rtype = fim.getReturnType();
		    // System.out.println("++++ rtype = " + rtype);
		    if (method.equals("new")) {
			if (!rtype.isAssignableFrom(clasz)) {
			    String rtName = rtype.getName();
			    String name = clasz.getName() + "::" + method;
			    String fimnm = fi.getName();
			    String msg =
				errorMsg("notCompatible", name, rtName, fimnm);
			    throw new IllegalArgumentException(msg);
			}
			String sname = clasz.getSimpleName();
			MethodInfo minfo =
			    constrMap.get(sname + ":" + carray.length);
			if (minfo == null) {
			    minfo = constrMap.get(sname);
			    if (minfo != null) isVarArgs = true;
			}
			if (minfo == null) {
			    String cname = clasz.getName();
			    throw new IllegalStateException
				(errorMsg("noMethodC", cname, carray.length));
			} else {
			    LinkedList<ClassArraySorter.Key> sorter =
				minfo.getSorted(clasz);
			    // Collections.reverse(sorter);
			    for (ClassArraySorter.Key key: sorter) {
				c = constrRefMatch(minfo, isVarArgs, fkey, key);
				if (c != null) break;
			    }
			}
		    } else {
			MethodInfo minfo =
			    staticMethodMap.get(isVarArgs? method:
						method + ":" +carray.length);
			if (minfo != null) {
			    LinkedList<ClassArraySorter.Key> sorter =
				minfo.getSorted(clasz);
			    if (sorter != null) {
				// Collections.reverse(sorter);
				fkey.promotePrimitives();
				/*
				System.out.println(fkey);
				for (ClassArraySorter.Key key: sorter) {
				    System.out.println("has key " + key);
				}
				*/
				for (ClassArraySorter.Key key: sorter) {
				    boolean tst = // fkey.isAssignableFrom(key);
					key.isAssignableFrom(fkey);
				    m = tst? minfo.getMethod(clasz, key): null;
				    /*
				    m = methodRefMatch(minfo, isVarArgs,
						       rtype, fkey, key);
				    */
				    if (m != null) break;
				}
			    }
			}
			if (m == null
			    && (object != null ||carray.length > 0)) {
			    minfo = (object == null)?
				methodMap.get(fim.isVarArgs()? method:
					      method + ":"
					      + (carray.length-1)):
				methodMap.get(fim.isVarArgs()? method:
					      method + ":"
					      + (carray.length));
			    isStat = false;
			    if (object == null && minfo != null) {
				LinkedList<ClassArraySorter.Key> sorter =
				    minfo.getSorted(clasz);
				if (sorter != null) {
				    // Collections.reverse(sorter);
				    Class<?>[] ncarray =
					new Class<?>[carray.length-1];
				    System.arraycopy(carray, 1, ncarray, 0,
						     ncarray.length);
				    if(!methodRefMatch1(carray[0])) {
					String s1 = clasz.getName();
					String s2 = carray[0].getName();
					String s3 = fi.getName();
					String msg =
					    errorMsg("mRM1Failed", s1, s2, s3);
					throw new IllegalArgumentException(msg);
				    }
				    fkey = new ClassArraySorter.Key(ncarray);
				    for (ClassArraySorter.Key key: sorter) {
					boolean tst =
					    // fkey.isAssignableFrom(key);
					    key.isAssignableFrom(fkey);
					m = tst? minfo.getMethod(clasz, key):
					    null;
					/*
					m = methodRefMatch(minfo, isVarArgs,
							   rtype, fkey, key);
					*/
					if (m != null) break;
				    }
				}
			    } else if (minfo != null) {
				LinkedList<ClassArraySorter.Key> sorter =
				    minfo.getSorted(clasz);
				if (sorter != null) {
				    // Collections.reverse(sorter);
				    fkey = new ClassArraySorter.Key(carray);
				    fkey.promotePrimitives();
				    for (ClassArraySorter.Key key: sorter) {
					// m = minfo.getMethod(clasz, key);
					boolean tst =
					    // fkey.isAssignableFrom(key);
					    key.isAssignableFrom(fkey);
					m = tst? minfo.getMethod(clasz, key):
					    null;
					/*
					m = methodRefMatch(minfo, isVarArgs,
							   rtype, fkey, key);
					*/
					if (m != null) break;
				    }
				}
			    }
			}
		    }
		    if (m == null && c == null) {
			String s1 = clasz.getName() + "::" + method;
			String s2 = fi.getName();
			String msg = errorMsg("noMatching", s1, s2);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String s1 = clasz.getName() + "::" + method;
		    String s2 = fi.getName();
		    String msg = errorMsg("notFI", s1, s2);
		    throw new IllegalArgumentException(msg);
		}
		final Method ourmethod = m;
		final Constructor ourconstr = c;
		final boolean isStatic = isStat;
		/*
		System.out.println("+++ ourmethod = " + ourmethod);
		System.out.println("+++ ourconstr = " + ourconstr);
		System.out.println("+++ isStatic = " + isStatic);
		*/
		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object proxy, Method m,
					     Object[] args)
			    throws Throwable
			{
			    if (ourconstr != null) {
				return ourconstr.newInstance(args);
			    } else if (isStatic) {
				return ourmethod.invoke(null, args);
			    } else if (object != null) {
				return ourmethod.invoke(object, args);
			    } else {
				Object[] newargs = new Object[args.length-1];
				System.arraycopy(args, 1, newargs, 0,
						 newargs.length);
				return ourmethod.invoke(args[0], newargs);
			    }
			}
		    };
		return fi.cast(AccessController.doPrivileged
			       (new PrivilegedAction<Object>() {
				       public Object run() {
					   return Proxy.newProxyInstance
					       (fi.getClassLoader(),
						new Class<?>[] {fi},
						handler);
				       }
				   }));

	    }
	}

	// private boolean stackTracing = false;
	private ThreadLocal<Boolean> stackTracing =
	    new ThreadLocal<Boolean>() {
		@Override protected Boolean initialValue() {
		    return Boolean.FALSE;
		}
	    };

	private class MethodInfo {
	    boolean addsCompleted = false;
	    String name;
	    HashMap<Class<?>, Object> map = new HashMap<>();
	    HashMap<Class<?>,ClassArraySorter> map2 = null;
	    MethodInfo(String name) {
		this.name = name;
	    }
	    ClassArraySorter getSorter(Class<?> clasz) {
		if (addsCompleted) {
		    throw new IllegalStateException(errorMsg("addsComplete"));
		}
		Object obj = map.get(clasz);
		if (obj == null) {
		    ClassArraySorter sorter = new ClassArraySorter();
		    map.put(clasz, sorter);
		    return sorter;
		} else if (obj instanceof LinkedList) {
		    ClassArraySorter sorter = map2.get(clasz);
		    map.put(clasz, sorter);
		    return sorter;
		} else if (obj instanceof ClassArraySorter) {
		    ClassArraySorter result = (ClassArraySorter) obj;
		    return result;
		}
		return null;
	    }

	    HashMap<Class<?>,HashMap<ClassArraySorter.Key,Method>> mmap
		= new HashMap<>();

	    Method getMethod(Class<?> clasz, ClassArraySorter.Key key) {
		HashMap<ClassArraySorter.Key,Method> kmap = mmap.get(clasz);
		if (kmap == null) return null;
		return kmap.get(key);
	    }

	    void putMethod(Class<?> clasz, ClassArraySorter.Key key, Method m)
		throws IllegalAccessException
	    {
		HashMap<ClassArraySorter.Key,Method> kmap;
		if (!mmap.containsKey(clasz)) {
		    kmap = new HashMap<ClassArraySorter.Key,Method>();
		    mmap.put(clasz, kmap);
		} else {
		    kmap = mmap.get(clasz);
		}
		Class<?> dc = m.getDeclaringClass();
		if (!Modifier.isPublic(dc.getModifiers())
		    && !allowedValues.contains(dc)
		    && !allowedArgs.contains(dc)) {
		    try {
			m = findBestMethod(m);
		    } catch (NoSuchMethodException e) {
			throw new IllegalAccessException(e.getMessage());
		    }
		}
		kmap.put(key, m);
	    }

	    HashMap<Class<?>,HashMap<ClassArraySorter.Key,Constructor>> cmap
		= new HashMap<>();

	    Constructor getConstructor(Class<?> clasz,
				       ClassArraySorter.Key key)
	    {
		HashMap<ClassArraySorter.Key,Constructor>
		    kmap = cmap.get(clasz);
		if (kmap == null) return null;
		return kmap.get(key);
	    }

	    void putConstructor(Class<?> clasz, ClassArraySorter.Key key,
			   Constructor c)
	    {
		HashMap<ClassArraySorter.Key,Constructor> kmap;
		if (!cmap.containsKey(clasz)) {
		    kmap = new HashMap<ClassArraySorter.Key,Constructor>();
		    cmap.put(clasz, kmap);
		} else {
		    kmap = cmap.get(clasz);
		}
		kmap.put(key, c);
	    }

	    @SuppressWarnings("unchecked")
	    synchronized
	    LinkedList<ClassArraySorter.Key> getSorted(Class<?> clasz) {
		Object obj = map.get(clasz);
		if (obj instanceof ClassArraySorter) {
		    if (importMode) {
			if (map2 == null) {
			    map2 = new HashMap<Class<?>,ClassArraySorter>();
			}
			map2.put(clasz, (ClassArraySorter)obj);
		    }
		    obj = ((ClassArraySorter)obj).createList(importMode);
		    if (importMode == false) {
			addsCompleted = true;
			frozen = true;
		    };
		    map.put(clasz, obj);
		}
		if (obj instanceof LinkedList) {
		    return (LinkedList<ClassArraySorter.Key>) obj;
		}
		return null;
	    }

	    Set<Class<?>> getClasses() {
		return map.keySet();
	    }
	}

	private HashMap<String,MethodInfo> constrMap = new HashMap<>();
	private HashSet<Class<?>> constrSet = new HashSet<>();
	private HashSet<Class<?>> enumSet = new HashSet<>();

	public void addConstructors(Class<?> clasz)
	    throws IllegalAccessException
	{
	    if (constrSet.contains(clasz)) return;
	    simpleClassNames.add(clasz.getSimpleName());

	    constrSet.add(clasz);
	    for (Constructor<?> constr: clasz.getDeclaredConstructors()) {
		int mod = constr.getModifiers();
		if (Modifier.isPublic(mod)) {
		    Class<?>[] parameters = constr.getParameterTypes();
		    boolean skip = false;
		    int plen = parameters.length;
		    int lst = plen - 1;
		    for (int i = 0; i < plen; i++) {
			Class<?> c = parameters[i];
			if (c.equals(double.class)) {
			    c = Number.class;
			} else if (c.equals(int.class)) {
			    c = Integer.class;
			} else if (c.equals(long.class)) {
			    c = Long.class;
			} else if (c.equals(boolean.class)) {
			    c = Boolean.class;
			}
			int cmod = c.getModifiers();
			boolean isEnum = c.isEnum()
			    && Modifier.isPublic(cmod)
			    && (Modifier.isStatic(cmod)
				|| c.getEnclosingClass() == null);
			if (!c.equals(Number.class)
			    && !c.equals(Integer.class)
			    && !c.equals(Long.class)
			    && !c.equals(Boolean.class)
			    && !c.equals(String.class)
			    && !isEnum
			    && !allowedArgs.contains(c)) {
			    if (constr.isVarArgs() && i == lst && c.isArray()) {
				Class<?>cc = c.getComponentType();
				if (cc.equals(double.class)) {
				    cc = Number.class;
				    c = getArrayClass(cc);
				} else if (cc.equals(int.class)) {
				    cc = Integer.class;
				    c = getArrayClass(cc);
				} else if (cc.equals(long.class)) {
				    cc = Long.class;
				    c = getArrayClass(cc);
				} else if (cc.equals(boolean.class)) {
				    cc = Boolean.class;
				    c = getArrayClass(cc);
				} else if (cc.isArray()
					   && !allowedArrayComponent(cc,
								     true)) {
				    skip = true;
				    break;
				} else if (!clasz.equals(String.class)
				    && !allowedArgs.contains(cc)) {
				    skip = true;
				    break;
				}
			    } else if (c.isArray()) {
				Class<?>cc = getLastComponentType(c);
				if (!allowedArrayComponent(cc, true)) {
				    skip = true;
				    break;
				}
			    } else {
				skip = true;
				break;
			    }
			}
			parameters[i] = c;
			if (!c.isPrimitive()) {
			    if (constr.isVarArgs() && i == lst) {
				Class<?> cc = c.getComponentType();
				if (cc.isArray()) {
				    cc = getLastComponentType(cc);
				}
				simpleClassNames.add(cc.getSimpleName());
			    } else if (c.isArray()) {
				Class<?>cc = getLastComponentType(c);
				simpleClassNames.add(cc.getSimpleName());
			    } else {
				simpleClassNames.add(c.getSimpleName());
			    }
			}
			if (isEnum && !enumSet.contains(c)) {
			    enumSet.add(c);
			    allowedArgs.add(c);
			    allowedValues.add(c);
			    addEnumConstants(c);
			}
		    }
		    if (skip) continue;
		    // String key = clasz.getSimpleName() + ":" + plen;
		    String key = (constr.isVarArgs())? clasz.getSimpleName():
			clasz.getSimpleName() + ":" + plen;
		    MethodInfo map = constrMap.get(key);
		    if (map == null) {
			map = new MethodInfo(key);
			constrMap.put(key, map);
		    }
		    // Class<?> mclass = constr.getDeclaringClass();
		    ClassArraySorter sorter = map.getSorter(clasz);
		    ClassArraySorter.Key skey =
			new ClassArraySorter.Key(parameters);
		    if (constr.isVarArgs()) skey.varargsMode();
		    sorter.addKey(skey);
		    map.putConstructor(clasz, skey, constr);
		}
	    }
	}

	public void addEnumConstants(Class<?> c)
	    throws IllegalAccessException
	{
	    String cname = c.getName();
	    if (c.isEnum()) {
		for (Field f: c.getDeclaredFields()) {
		    if (f.isEnumConstant()) {
			String key = cname + "." + f.getName();
			Object value = f.get(null);
			key = key.replace('$', '.');
			cmap.put(key, value);
		    }
		}
	    }
	}

	private HashSet<Class<?>> staticMethodCSet = new HashSet<>();
	private HashSet<Class<?>> methodCSet = new HashSet<>();
	private HashMap<String,MethodInfo> methodMap = new HashMap<>();
	private HashMap<String,MethodInfo> staticMethodMap = new HashMap<>();

	private void addMethods(Class<?> clasz, boolean isStatic)
	    throws IllegalAccessException
	{
	    simpleClassNames.add(clasz.getSimpleName());
	    if (isStatic) {
		if (staticMethodCSet.contains(clasz)) return;
		staticMethodCSet.add(clasz);
	    } else {
		if (methodCSet.contains(clasz)) return;
		methodCSet.add(clasz);
	    }
	    for (Method m: (isStatic? clasz.getDeclaredMethods():
			    clasz.getMethods())) {
		int mod = m.getModifiers();
		if (Modifier.isStatic(mod) == isStatic
		    && Modifier.isPublic(mod)) {
		    Class<?> retclass = m.getReturnType();
		    if (retclass.equals(double.class)
			|| retclass.equals(void.class)
			|| retclass.equals(int.class)
			|| retclass.equals(long.class)
			|| retclass.equals(boolean.class)
			|| retclass.equals(String.class)
			|| allowedValues.contains(retclass)
			|| (retclass.isArray() &&
			    allowedArrayComponent(retclass, false))) {
			Class<?>[] parameters = m.getParameterTypes();
			boolean skip = false;
			int plen = parameters.length;
			int lst = plen - 1;
			for (int i = 0; i < plen; i++) {
			    Class<?> c = parameters[i];
			    if (c.equals(double.class)) {
				c = Number.class;
			    } else if (c.equals(int.class)) {
				c = Integer.class;
			    } else if (c.equals(long.class)) {
				c = Long.class;
			    } else if (c.equals(boolean.class)) {
				c = Boolean.class;
			    }
			    int cmod = c.getModifiers();
			    boolean isEnum = c.isEnum()
				&& Modifier.isPublic(cmod)
				&& (Modifier.isStatic(cmod)
				    || c.getEnclosingClass() == null);
			    if (!c.equals(Number.class)
				&& !c.equals(Integer.class)
				&& !c.equals(Long.class)
				&& !c.equals(Boolean.class)
				&& !c.equals(String.class)
				&& !isEnum
				&& !allowedArgs.contains(c)) {
				if (m.isVarArgs() && i == lst && c.isArray()) {
				    Class<?>cc = c.getComponentType();
				    if (cc.equals(double.class)) {
					cc = Number.class;
					c = getArrayClass(cc);
				    } else if (c.equals(int.class)) {
					cc = Integer.class;
					c = getArrayClass(cc);
				    } else if (c.equals(long.class)) {
					cc = Long.class;
					c = getArrayClass(cc);
				    } else if (c.equals(boolean.class)) {
					cc = Boolean.class;
					c = getArrayClass(cc);
				    } else if (c.isArray()) {
					cc = getLastComponentType(c);
				    }
				    if (!clasz.equals(String.class)
					&& !allowedArgs.contains(cc)) {
					skip = true;
					break;
				    }
				} else {
				    skip = true;
				    break;
				}
			    }
			    parameters[i] = c;
			    if (!c.isPrimitive()) {
				if (m.isVarArgs() && i == lst) {
				    Class<?> cc = c.getComponentType();
				    if (cc.isArray()) {
					cc = getLastComponentType(cc);
				    }
				    simpleClassNames.add(cc.getSimpleName());
				} else if (c.isArray()) {
				    Class<?> cc = getLastComponentType(c);
				    simpleClassNames.add(cc.getSimpleName());
				} else {
				    simpleClassNames.add(c.getSimpleName());
				}
			    }
			    if (isEnum && !enumSet.contains(c)) {
				enumSet.add(c);
				allowedArgs.add(c);
				allowedValues.add(c);
				addEnumConstants(c);
			    }
			}
			if (skip) {
			    /*
			    System.out.println("skipping "
					       + m.getName() + ":" + plen
					       + "for " + clasz);
			    */
			    continue;
			}
			String key = (m.isVarArgs())? m.getName():
			    m.getName() + ":" + plen;
			// System.out.println("key " + key + " for " + clasz);
			MethodInfo map = isStatic? staticMethodMap.get(key):
			    methodMap.get(key);
			if (map == null) {
			    map = new MethodInfo(key);
			    if (isStatic) {
				staticMethodMap.put(key, map);
			    } else {
				methodMap.put(key, map);
			    }
			}
			Class<?> dclass = m.getDeclaringClass();
			if (dclass.equals(Object.class)) {
			    if (plen != 0
				|| !(m.getName().equals("toString"))) {
				continue;
			    }
			}
			ClassArraySorter sorter = map.getSorter(clasz);
			ClassArraySorter.Key skey =
			    new ClassArraySorter.Key(parameters);
			if (m.isVarArgs()) skey.varargsMode();
			sorter.addKey(skey);
			map.putMethod(clasz, skey, m);
		    } else {
			/*
			System.out.println("skipping " + m.getName()
					   + " for " + clasz
					   + " because retclass = "
					   + m.getReturnType());
			*/
		    }
		}
	    }
	}
	StringBuilder sb = new StringBuilder();
	HashSet<Class<?>> fieldClassSet = new HashSet<>();
	List<Field> fieldList= new LinkedList<>(); // only used for docs

	private void createFieldList() {
	    fieldList.clear();
	    sb.setLength(0);
	    sb.append("|");
	    for (Class<?> clasz: fieldClassSet) {
		if (enumSet.contains(clasz)) continue;
		for (Field f: clasz.getDeclaredFields()) {
		    int mods = f.getModifiers();
		    if (!Modifier.isStatic(mods)
			|| !Modifier.isPublic(mods)
			|| !Modifier.isFinal(mods)) {
			continue;
		    }
		    Class<?> c = f.getType();
		    if (c.isEnum() || c.equals(int.class)
			|| c.equals(boolean.class)
			|| c.equals(double.class)
			|| c.equals(long.class)
			|| c.equals(String.class)
			|| allowedValues.contains(c)
			|| allowedArgs.contains(c)) {
			String name = clasz.getName() + "."
			    + f.getName() + "|";
			name = name.replace('$', '.');
			fieldList.add(f);
			sb.append(name);
		    }
		}
	    }
	}

	private void addFields(Class<?> clasz)
	    throws IllegalAccessException
	{
	    sa = null;
	    // sb.setLength(0);
	    // sb.append("|");
	    fieldClassSet.add(clasz);
	    simpleClassNames.add(clasz.getSimpleName());
	    for (Field f: clasz.getDeclaredFields()) {
		int mods = f.getModifiers();
		if (!Modifier.isStatic(mods)
		    || !Modifier.isPublic(mods)
		    || !Modifier.isFinal(mods)) {
		    continue;
		}
		Class<?> c = f.getType();
		if (!c.isPrimitive()) {
		    simpleClassNames.add(c.getSimpleName());
		}
		if (c.isEnum()) {
		    if (!enumSet.contains(c)) {
			enumSet.add(c);
			allowedArgs.add(c);
			allowedValues.add(c);
			addEnumConstants(c);
		    }
		} else if (c.equals(int.class)
			   || c.equals(boolean.class)
			   || c.equals(double.class)
			   || c.equals(long.class)
			   || c.equals(String.class)
			   || allowedValues.contains(c)
			   || allowedArgs.contains(c)) {
		    String key = clasz.getName() + "." + f.getName();
		    key = key.replace('$', '.');
		    cmap.put(key, f.get(null));
		} else if (scriptingMode) {
		    allowedArgs.add(c);
		    allowedValues.add(c);
		    String key = clasz.getName() + "." + f.getName();
		    key = key.replace('$', '.');
		    cmap.put(key, f.get(null));
		} else {
		    continue;
		}
	    }
	}

	char[] enumsAndFields = null;
	SuffixArray.Char sa = null;
	private void processEnumsAndFields() {
	    createFieldList();
	    for (Class<?> clasz: enumSet) {
		for (Field f: clasz.getDeclaredFields()) {
		    if (f.isEnumConstant()) {
			String name = clasz.getName() + "."
			    + f.getName() + "|";
			name = name.replace('$', '.');
			sb.append(name);
		    }
		}
	    }
	    enumsAndFields = sb.toString().toCharArray();
	    sb.setLength(0);
	    int n = -1;
	    for (char ch: enumsAndFields) {
		if (n < ch) n = ch;
	    }
	    n++;
	    if (n > 0) {
		sa =  new SuffixArray.Char(enumsAndFields, n);
	    }
	}

	// used to list name conflicts.
	// ArrayList<String> ambiguousNames = null;
	ThreadLocal<ArrayList<String>> ambiguousNames = new ThreadLocal<>();

	public String findField(String name) {
	    if (sa == null) {
		if (importMode) {
		    if (enumSet.size() == 0 && fieldClassSet.size() == 0) {
			return null;
		    } else {
			AccessController.doPrivileged
			    (new PrivilegedAction<Void>() {
				    public Void run() {
					processEnumsAndFields();
					return (Void) null;
				    }
				});
		    }
		} else {
		    return null;
		}
	    }
	    SuffixArray.Range range = sa.findRange((name + "|").toCharArray());
	    int rsz = range.size();
	    if (rsz == 1) {
		int index = range.subsequenceIndex(0);
		char ch = enumsAndFields[--index];
		int end = index + range.subsequenceLength();
		if (ch != '|' && ch != '.') return null;
		while (ch != '|') {
		    ch = enumsAndFields[--index];
		}
		index++;
		return new String(enumsAndFields, index, end - index);
	    } else if (rsz > 1) {
		String fname = null;
		ArrayList<String> ambigNames = null;
		for (int i = 0; i < rsz; i++) {
		    int start = range.subsequenceIndex(i);
		    int index = start;
		    char ch = enumsAndFields[--index];
		    int end = index + range.subsequenceLength();
		    if (ch == '|') {
			ambigNames = null;
			return new String(enumsAndFields, start, end - start);
		    }
		    if (ch != '|' && ch != '.') {
			continue;
		    } else if (fname != null) {
			if (ambigNames == null) {
			    ambigNames = new ArrayList<String>();
			    ambigNames.add(fname);
			}
			while (ch != '|') {
			    ch = enumsAndFields[--index];
			}
			index++;
			String nfn = new String(enumsAndFields,
						index, end - index);
			ambigNames.add(nfn);
			continue;
		    }
		    while (ch != '|') {
			ch = enumsAndFields[--index];
		    }
		    index++;
		    fname = new String(enumsAndFields, index, end - index);
		}
		if (ambigNames != null) {
		    ambiguousNames.set(ambigNames);
		}
		return (ambigNames == null)? fname: null;
	    } else {
		return null;
	    }
	}

	private class Bases {
	    int valueStackBase;
	    int opStackBase;
	    Bases(int valueStackBase, int opStackBase) {
		this.valueStackBase = valueStackBase;
		this.opStackBase = opStackBase;
	    }
	    int getValueStackBase() {return valueStackBase;}
	    int getOptStackBase() {return opStackBase;}
	}

	/*
	private Stack<Object> valueStack = new Stack<Object>();
	private int valueStackBase = 0; // function-call start
	private Stack<Token> opStack = new Stack<Token>();
	private int opStackBase = 0;
	Stack<Bases> baseStack = new Stack<>();

	private Stack<Map<String,Object>>amapStack = new Stack<>();
	private Map<String,Object> amap = null;
	*/

	class CallContext {
	    Stack<Object> valueStack = new Stack<Object>();
	    int valueStackBase = 0; // function-call start
	    Stack<Token> opStack = new Stack<Token>();
	    int opStackBase = 0;
	    Stack<Bases> baseStack = new Stack<>();
	    /*
	    Stack<Map<String,Object>>amapStack = new Stack<>();
	    Map<String,Object> amap = null;
	    */
	    Stack<UniTreeNode<Map<String,Object>>> amapTreeStack
		= new Stack<>();
	    UniTreeNode<Map<String,Object>> amapTree = null;
	};

	private ThreadLocal<CallContext> callContext =
	    new ThreadLocal<CallContext>() {
		@Override protected CallContext initialValue() {
		    return new CallContext();
		}
	    };

	private ThreadLocal<Stack<CallContext>> callContextStack =
	    new ThreadLocal<>() {
		@Override protected Stack<CallContext> initialValue() {
		    return new Stack<CallContext>();
		}
	    };

	// So we can have a separate call context if there is a
	// recursive call to the ExpressionParser 'parse' methods.
	private void pushCallContext() {
	    CallContext cc = callContext.get();
	    Stack<CallContext> stack = callContextStack.get();
	    stack.push(cc);
	    cc = new CallContext();
	    callContext.set(cc);
	}


	private void popCallContext() {
	    Stack<CallContext> stack = callContextStack.get();
	    if (stack.size() == 0) {
		throw new IllegalStateException(errorMsg("emptyCCStack"));
	    }
	    CallContext cc = stack.pop();
	    callContext.set(cc);
	}


	/*
	private void pushArgMap(Map<String,Object> map) {
	    CallContext cc = callContext.get();
	    if (cc.amap != null) {
		cc.amapStack.push(cc.amap);
	    }
	    cc.amap = map;
	}

	private void popArgMap() {
	    CallContext cc = callContext.get();
	    if (cc.amapStack.size() > 0) {
		cc.amap = cc.amapStack.pop();
	    } else {
		cc.amap = null;
	    }
	}

	boolean hasAmap() {
	    CallContext cc = callContext.get();
	    return cc.amap != null;
	}

	void putInAmap(String name, Object object) {
	    CallContext cc = callContext.get();
	    cc.amap.put(name, object);
	}

	boolean amapContains(String name) {
	    CallContext cc = callContext.get();
	    return cc.amap.containsKey(name);
	}

	Object getFromAmap(String name) {
	    CallContext cc = callContext.get();
	    return cc.amap.get(name);
	}
	*/

	private void pushArgMap(UniTreeNode<Map<String,Object>> mapTree) {
	    CallContext cc = callContext.get();
	    if (cc.amapTree != null) {
		cc.amapTreeStack.push(cc.amapTree);
	    }
	    cc.amapTree = mapTree;
	}

	private void popArgMap() {
	    CallContext cc = callContext.get();
	    if (cc.amapTreeStack.size() > 0) {
		cc.amapTree = cc.amapTreeStack.pop();
	    } else {
		cc.amapTree = null;
	    }
	}

	boolean hasAmap() {
	    CallContext cc = callContext.get();
	    return cc.amapTree != null;
	}

	void putInAmap
	    (String name, Object object) {
	    CallContext cc = callContext.get();
	    boolean needAdd = true;
	    for (Map<String,Object>map: cc.amapTree) {
		if (map.containsKey(name)) {
		    map.put(name, object);
		    needAdd = false;
		    break;
		}
	    }
	    if (needAdd) {
		cc.amapTree.getElement().put(name, object);
	    }
	}

	boolean amapContains(String name) {
	    CallContext cc = callContext.get();
	    for (Map<String,Object>map: cc.amapTree) {
		if (map.containsKey(name)) {
		    return true;
		}
	    }
	    return false;
	}

	Object getFromAmap(String name) {
	    CallContext cc = callContext.get();
	    for (Map<String,Object>map: cc.amapTree) {
		if (map.containsKey(name)) {
		    return map.get(name);
		}
	    }
	    return null;
	}

	int getOpStackBase() {
	    CallContext cc = callContext.get();
	    return cc.opStackBase;
	}

	int getValueStackBase() {
	    CallContext cc = callContext.get();
	    return cc.valueStackBase;
	}

	void pushBases() {
	    CallContext cc = callContext.get();
	    cc.baseStack.push(new Bases(cc.valueStackBase, cc.opStackBase));
	    cc.valueStackBase = cc.valueStack.size();
	    cc.opStackBase = cc.opStack.size();
	}

	void popBases() {
	    CallContext cc = callContext.get();
	    if (cc.baseStack.size() == 0) {
		System.err.println
		    ("ExpressionParser warning: base stack error");
		cc.valueStackBase = 0;
		cc.opStackBase = 0;
	    }
	    Bases bases = cc.baseStack.pop();
	    cc.valueStackBase = bases.valueStackBase;
	    cc.opStackBase = bases.opStackBase;
	}

	public void pushValue(Object value) {
	    if (stackTracing.get()) {
		System.err.format("        PUSH %s\n", value);
	    }
	    if (value == null) value = NULL;
	    CallContext cc = callContext.get();
	    cc.valueStack.push(value);
	}

	public Object popValue() {
	    CallContext cc = callContext.get();
	    if (cc.valueStack.size() <= cc.valueStackBase) {
		throw new IllegalStateException(errorMsg("valueStackEmpty"));
	    }
	    Object value =  cc.valueStack.pop();
	    if (value == NULL) value = null;
	    if (stackTracing.get()) {
		System.err.format("        ... POPPING %s\n", value);
	    }
	    return value;
	}

	public Object peekValue() {
	    CallContext cc = callContext.get();
	    if (cc.valueStack.size() <= cc.valueStackBase) {
		throw new IllegalStateException(errorMsg("valueStackEmpty"));
	    }
	    Object value = cc.valueStack.peek();
	    if (value == NULL) value = null;
	    return value;
	}

	public boolean hasValue() {
	    CallContext cc = callContext.get();
	    return cc.valueStack.size() > cc.valueStackBase;
	}

	public boolean valueStackEmpty() {
	    CallContext cc = callContext.get();
	    return cc.valueStack.size() == cc.valueStackBase;
	}

	public Object getResult() {
	    CallContext cc = callContext.get();
	    if (cc.valueStack.size() == cc.valueStackBase) return null;
	    Object results = cc.valueStack.pop();
	    if (results == NULL || results == VOID) return null;
	    return results;
	}

	public void clear() {
	    CallContext cc = callContext.get();
	    cc.valueStack.clear();
	    cc.opStack.clear();
	}

	public void pushOp(Token opToken) {
	    CallContext cc = callContext.get();
	    cc.opStack.push(opToken);
	}

	public Token peekOp() {
	    CallContext cc = callContext.get();
	    return cc.opStack.peek();
	}

	public boolean hasOps() {
	    CallContext cc = callContext.get();
	    return cc.opStack.size() > cc.opStackBase;
	}


	public Token popOp() {
	    CallContext cc = callContext.get();
	    if (cc.opStack.size() <= cc.opStackBase) {
		throw new IllegalStateException(errorMsg("opStackEmpty"));
	    }
	    return cc.opStack.pop();
	}


	// push and eval if necessary.
	public void pushOp(Token opToken, String orig) {
	    if (stackTracing.get()) {
		if (traceValueSet.contains(opToken.getType())
		    && opToken.getValue() != null) {
		    System.err.format("    PUSH %s (\"%s %s\"): level=%d\n",
				      opToken.getType(), opToken.getName(),
				      opToken.getValue(),
				      opToken.getLevel());
		} else {
		    System.err.format("    PUSH %s (\"%s\"): level=%d\n",
				      opToken.getType(), opToken.getName(),
				      opToken.getLevel());
		}
	    }
	    while (hasOps()) {
		Token op = peekOp();
		if (op.getLevel() >= opToken.getLevel()) {
		    evalOnce(orig);
		} else {
		    break;
		}
	    }
	    pushOp(opToken);
	    switch(opToken.getType()) {
	    case ACTIVE_COMMA:
	    case CBRACKET:
	    case OBJCLOSEBRACE:
	    case CPAREN:
	    case QMARK:
	    case FINISH_IMPORT:
		evalOnce(orig);
		break;
	    case SEMICOLON:
		evalOnce(orig);
		boolean firstTime = true;
		while (hasValue()) {
		    if (stackTracing.get() && firstTime) {
			System.err.println("        ... "
					   + "(clearing current stack)");
			firstTime = false;
		    }
		    popValue();
		}
		break;
	    default:
		break;
	    }
	}

	private Number add(Number n1, Number n2) {
	    if (n1 instanceof Double || n2 instanceof Double) {
		double d1 = n1.doubleValue();
		double d2 = n2.doubleValue();
		return Double.valueOf(d1+d2);
	    } else {
		long l1 = n1.longValue();
		long l2 = n2.longValue();
		long result = l1 + l2;
		if (n1 instanceof Integer && n2 instanceof Integer) {
		    if (result <= Integer.MAX_VALUE
			&& result >= Integer.MIN_VALUE) {
			return Integer.valueOf((int)result);
		    } else {
			return Long.valueOf(result);
		    }
		} else {
		    if ((l1 >= 0 && l2 >= 0 && result < 0)
			|| (l1 <= 0 && l2 <= 0 && result > 0)) {
			double d1 = n1.doubleValue();
			double d2 = n2.doubleValue();
			return Double.valueOf(d1+d2);
		    } else {
			return Long.valueOf(result);
		    }
		}
	    }
	}
	
	private Number changeSign(Number n) {
	    if (n instanceof Integer) {
		int iv = n.intValue();
		return Integer.valueOf(-iv);
	    } else if (n instanceof Long) {
		long iv = n.longValue();
		return Long.valueOf(-iv);
	    } else {
		double d = n.doubleValue();
		return Double.valueOf(-d);
	    }
	}

	private Number not(Number n) {
	    if (n instanceof Integer) {
		int iv = n.intValue();
		return Integer.valueOf(~iv);
	    } else if (n instanceof Long) {
		String msg = errorMsg("needInt", n);
		throw new IllegalArgumentException(msg);
	    } else if (n instanceof Double) {
		double d = n.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			int iv = (int) lv;
			return Integer.valueOf(~iv);
		    } else {
			String msg = errorMsg("intRange", n);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n);
		throw new IllegalArgumentException(msg);
	    }
	}

	private Number and(Number n1, Number n2) {
	    int i1; int i2;
	    if (n1 instanceof Integer) {
		i1  = n1.intValue();
	    } else if (n1 instanceof Long) {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    } else if (n1 instanceof Double) {
		double d = n1.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i1 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n1);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n1);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    }
	    if (n2 instanceof Integer) {
		i2  = n2.intValue();
	    } else if (n2 instanceof Long) {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    } else if (n2 instanceof Double) {
		double d = n2.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i2 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n2);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n2);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    }
	    return Integer.valueOf(i1 & i2);
	}

	private Number or(Number n1, Number n2) {
	    int i1; int i2;
	    if (n1 instanceof Integer) {
		i1  = n1.intValue();
	    } else if (n1 instanceof Long) {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    } else if (n1 instanceof Double) {
		double d = n1.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i1 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n1);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n1);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    }
	    if (n2 instanceof Integer) {
		i2  = n2.intValue();
	    } else if (n2 instanceof Long) {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    } else if (n2 instanceof Double) {
		double d = n2.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i2 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n2);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n2);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    }
	    return Integer.valueOf(i1 | i2);
	}

	private Number xor(Number n1, Number n2) {
	    int i1; int i2;
	    if (n1 instanceof Integer) {
		i1  = n1.intValue();
	    } else if (n1 instanceof Long) {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    } else if (n1 instanceof Double) {
		double d = n1.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i1 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n1);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n1);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n1);
		throw new IllegalArgumentException(msg);
	    }
	    if (n2 instanceof Integer) {
		i2  = n2.intValue();
	    } else if (n2 instanceof Long) {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    } else if (n2 instanceof Double) {
		double d = n2.doubleValue();
		if (Math.rint(d) == d) {
		    long lv = Math.round(d);
		    if (lv <= Integer.MAX_VALUE && lv >= Integer.MIN_VALUE) {
			i2 = (int) lv;
		    } else {
			String msg = errorMsg("intRange", n2);
			throw new IllegalArgumentException(msg);
		    }
		} else {
		    String msg = errorMsg("needInt", n2);
		    throw new IllegalArgumentException(msg);
		}
	    } else {
		String msg = errorMsg("needInt", n2);
		throw new IllegalArgumentException(msg);
	    }
	    return Integer.valueOf(i1 ^ i2);
	}

	private Number sub(Number n1, Number n2) {
	    if (n1 instanceof Double || n2 instanceof Double) {
		double d1 = n1.doubleValue();
		double d2 = n2.doubleValue();
		return Double.valueOf(d1-d2);
	    } else {
		long l1 = n1.longValue();
		long l2 = n2.longValue();
		long result = l1 - l2;
		if (n1 instanceof Integer && n2 instanceof Integer) {
		    if (result <= Integer.MAX_VALUE
			&& result >= Integer.MIN_VALUE) {
			return Integer.valueOf((int)result);
		    } else {
			return Long.valueOf(result);
		    }
		} else {
		    if ((l1 >= 0 && l2 <= 0 && result < 0)
			|| (l1 <= 0 && l2 >= 0 && result > 0)) {
			double d1 = n1.doubleValue();
			double d2 = n2.doubleValue();
			return Double.valueOf(d1-d2);
		    } else {
			return Long.valueOf(result);
		    }
		}
	    }
	}

	private Number mult(Number n1, Number n2) {
	    if (n1 instanceof Double || n2 instanceof Double) {
		double d1 = n1.doubleValue();
		double d2 = n2.doubleValue();
		return Double.valueOf(d1 * d2);
	    } else {
		if (n1 instanceof Integer && n2 instanceof Integer) {
		    long l1 = n1.longValue();
		    long l2 = n2.longValue();
		    long result = l1 * l2;
		    if (result <= Integer.MAX_VALUE
			&& result >= Integer.MIN_VALUE) {
			return Integer.valueOf((int)result);
		    } else {
			return Long.valueOf(result);
		    }
		} else {
		    double d1 = n1.doubleValue();
		    double d2 = n2.doubleValue();
		    double dresult = d1*d2;
		    long lresult = (long) dresult;
		    if (dresult == (double)lresult) {
			return Long.valueOf(lresult);
		    } else {
			return Double.valueOf(dresult);
		    }
		}
	    }
	}

	private Number div(Number n1, Number n2) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    double result = d1/d2;
	    if (n1 instanceof Double || n2 instanceof Double) {
		return Double.valueOf(result);
	    } else {
		long lresult = (long)result;
		if ((double) lresult == result) {
		    if (n1 instanceof Integer && n2 instanceof Integer
			&& lresult <= Integer.MAX_VALUE
			&& lresult >= Integer.MIN_VALUE) {
			return Integer.valueOf((int)lresult);
		    } else {
			return lresult;
		    }
		} else {
		    return Double.valueOf(result);
		}
	    }
	}

	private Number mod(Number n1, Number n2) {
	    if (n1 instanceof Integer) {
		int in1 = n1.intValue();
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    return Integer.valueOf(in1 % in2);
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    return Integer.valueOf((int)(in1 % ln2));
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    long ln2 = Math.round(d2);
		    if ((double)ln2 == d2) {
			return Long.valueOf(in1 % ln2);
		    } else {
			return Double.valueOf(in1 % d2);
		    }
		}
	    } else if (n1 instanceof Long) {
		long ln1 = n1.longValue();
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    return Integer.valueOf((int)(ln1 % in2));
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    return Long.valueOf(ln1 % ln2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    long ln2 = Math.round(d2);
		    if ((double)ln2 == d2) {
			return Long.valueOf(ln1 % ln2);
		    } else {
			return Double.valueOf(ln2 % d2);
		    }
		}
	    } else if (n1 instanceof Double) {
		double d1 = n1.doubleValue();
		long ln1 = Math.round(d1);
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    if ((double)ln1 == d1) {
			return Long.valueOf(ln1 % in2);
		    } else {
			return Double.valueOf(d1 % in2);
		    }
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    if ((double)ln1 == d1) {
			return Long.valueOf(ln1 % ln2);
		    } else {
			return Double.valueOf(d1 % ln2);
		    }
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    long ln2 = Math.round(d2);
		    if ((double)ln1 == d1 && (double)ln2 == d2) {
			return Long.valueOf(ln1 % ln2);
		    } else {
			return Double.valueOf(d1 % d2);
		    }
		}
	    }
	    String msg = errorMsg("badMod", n1, n2);
	    throw new IllegalArgumentException(msg);
	}

	private Number mathmod(Number n1, Number n2) {
	    if (n1 instanceof Integer) {
		int in1 = n1.intValue();
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    if (in2 <= 0) {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		    int val = in1 % in2;
		    if (val < 0) val += in2;
		    return Integer.valueOf(val);
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    if (ln2 <= 0) {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		    long val = in1 % ln2;
		    return (val < 0)?
			Long.valueOf(ln2 + val):
			Integer.valueOf((int)val);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    long ln2 = Math.round(d2);
		    if ((double)ln2 == d2) {
			if (ln2 <= 0) {
			    String msg = errorMsg("badMathMod", n1, n2);
			    throw new IllegalArgumentException(msg);
			}
			long val = in1 & ln2;
			if (val < 0) {
			    return Long.valueOf(ln2 + val);
			} else {
			    return Integer.valueOf((int)val);
			}
		    } else {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		}
	    } else if (n1 instanceof Long) {
		long ln1 = n1.longValue();
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    if (in2 <= 0) {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		    int val = (int)(ln1 % in2);
		    if (val < 0) val += in2;
		    return Integer.valueOf(val);
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    if (ln2 <= 0) {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		    long val = ln1 % ln2;
		    if (val < 0) val += ln2;
		    return Long.valueOf(val);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (d2 < 0) {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		    long ln2 = Math.round(d2);
		    if ((double)ln2 == d2) {
			long val = ln1  % ln2;
			if (val < 0) val += ln2;
			return Long.valueOf(val);
		    } else {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		}
	    } else if (n1 instanceof Double) {
		double d1 = n1.doubleValue();
		long ln1 = Math.round(d1);
		if (n2 instanceof Integer) {
		    int in2 = n2.intValue();
		    if ((double)ln1 == d1) {
			int val = (int)(ln1 % in2);
			if (val < 0) val += in2;
			return Integer.valueOf(val);
		    } else {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		} else if (n2 instanceof Long) {
		    long ln2 = n2.longValue();
		    if ((double)ln1 == d1) {
			long val = ln1 % ln2;
			if (val < 0) val += ln2;
			return Long.valueOf(val);
		    } else {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    long ln2 = Math.round(d2);
		    if ((double)ln1 == d1 && (double)ln2 == d2) {
			long val = ln1 % ln2;
			if (val < 0) val += ln2;
			return Long.valueOf(val);
		    } else {
			String msg = errorMsg("badMathMod", n1, n2);
			throw new IllegalArgumentException(msg);
		    }
		}
	    }
	    String msg = errorMsg("badMod", n1, n2);
	    throw new IllegalArgumentException(msg);
	}

	private Number lshift(Number n1, Number n2) {
	    if (n1 instanceof Integer) {
		int i1 = n1.intValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Integer.valueOf(i1 << i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Integer.valueOf(i1 << l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Integer.valueOf(i1 << l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Long) {
		long l1 = n1.longValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 << i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 << l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 << l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Double) {
		double d1 = n1.doubleValue();
		if (Math.rint(d1) != d1) {
		    throw new IllegalArgumentException
			(errorMsg("nonIntLongN1", n1));
		}
		long l1 = Math.round(d1);
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 << i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 << l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 << l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("nonIntegralN1", n1));

	    }
	}

	private Number rshift(Number n1, Number n2) {
	    if (n1 instanceof Integer) {
		int i1 = n1.intValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Integer.valueOf(i1 >> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Integer.valueOf(i1 >> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Integer.valueOf(i1 >> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Long) {
		long l1 = n1.longValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 >> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 >> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 >> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Double) {
		double d1 = n1.doubleValue();
		if (Math.rint(d1) != d1) {
		    throw new IllegalArgumentException
			(errorMsg("nonIntLongN1", n1));
		}
		long l1 = Math.round(d1);
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 >> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 >> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 >> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("nonIntegralN1", n1));

	    }
	}

	private Number urshift(Number n1, Number n2) {
	    if (n1 instanceof Integer) {
		int i1 = n1.intValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Integer.valueOf(i1 >>> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Integer.valueOf(i1 >>> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Integer.valueOf(i1 >>> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Long) {
		long l1 = n1.longValue();
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 >>> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 >>> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 >>> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else if (n1 instanceof Double) {
		double d1 = n1.doubleValue();
		if (Math.rint(d1) != d1) {
		    throw new IllegalArgumentException
			(errorMsg("nonIntLongN1", n1));
		}
		long l1 = Math.round(d1);
		if (n2 instanceof Integer) {
		    int i2 = n2.intValue();
		    return Long.valueOf(l1 >>> i2);
		} else if (n2 instanceof Long) {
		    long l2 = n2.longValue();
		    return Long.valueOf(l1 >>> l2);
		} else if (n2 instanceof Double) {
		    double d2 = n2.doubleValue();
		    if (Math.rint(d2) != d2) {
			throw new IllegalArgumentException
			    (errorMsg("nonIntLongN2", n2));
		    }
		    long l2 = Math.round(d2);
		    return Long.valueOf(l1 >>> l2);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("nonIntegralN2", n2));
		}
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("nonIntegralN1", n1));

	    }
	}


	private Boolean gt(Number n1, Number n2) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    return Boolean.valueOf(d1 > d2);
	}

	private Boolean ge(Number n1, Number n2) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    return Boolean.valueOf(d1 >= d2);
	}

	private Boolean lt(Number n1, Number n2) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    return Boolean.valueOf(d1 < d2);
	}

	private Boolean le(Number n1, Number n2) {
	    double d1 = n1.doubleValue();
	    double d2 = n2.doubleValue();
	    return Boolean.valueOf(d1 <= d2);
	}

	private Boolean eq(Object n1, Object n2) {
	    if (n1 == null && n2 == null) return true;
	    else if (n1 == null) return false;
	    else if (n2 == null) return false;
	    else if (n1 instanceof Number && n2 instanceof Number) {
		if (n1 instanceof Double || n2 instanceof Double) {
		    double d1 = ((Number)n1).doubleValue();
		    double d2 = ((Number)n2).doubleValue();
		    return d1 == d2;
		} else if (n1 instanceof Long || n1 instanceof Integer) {
		    if (n2 instanceof Double) {
			double d1 = ((Number)n1).doubleValue();
			double d2 = ((Number)n2).doubleValue();
			return d1 == d2;
		    } else {
			long i1 = ((Number)n1).longValue();
			long i2 = ((Number)n2).longValue();
			return i1 == i2;
		    }
		} else {
		    double d1 = ((Number)n1).doubleValue();
		    double d2 = ((Number)n2).doubleValue();
		    return d1 == d2;
		}
	    } else {
		return n1.equals(n2);
	    }
	}

	private Boolean neq(Object n1, Object n2) {
	    boolean result = eq(n1, n2);
	    return !result;
	}

	private Boolean lnot(Boolean b1) {
	    boolean result = b1;
	    return !b1;
	}

	private Boolean land(Boolean b1, Boolean b2) {
	    return b1 && b2;
	}

	private Boolean lor(Boolean b1, Boolean b2) {
	    return b1 || b2;
	}

	public Method findRVFMethod(final String fname)
	    throws IllegalStateException, NoSuchMethodException,
		   IllegalArgumentException
	{
	    Class<?>[] argclasses = new Class<?>[1];
	    int i = 0;
	    argclasses[0] = Double.class;
	    Method method = findMethod(fname, argclasses, null, null, null);
	    if (method == null) {
		String msg = errorMsg("notRVF", fname);
		throw new IllegalStateException(msg);
	    }
	    Class<?> retclass = method.getReturnType();
	    if (!(retclass.equals(Double.class)
		  || retclass.equals(double.class))) {
		String msg = errorMsg("notRVF", fname);
		throw new IllegalStateException(msg);
	    }
	    return method;
	}

	// used for a method cache
	private class MethodKey {
	    Class<?> clasz;
	    Method method;
	    MethodKey(Class<?> c, Method m) {
		clasz = c;
		method = m;
	    }
	    public boolean equals(Object o) {
		if (o instanceof MethodKey) {
		    MethodKey mk = (MethodKey) o;
		    // m is looked up from a MethodInfo so we can use '=='
		    return (mk.clasz.equals(clasz) && mk.method == method);
		} else {
		    return false;
		}
	    }
	    public int hashCode() {
		int hash = (clasz == null)? 0: clasz.hashCode();
		return 31*hash + ((method == null)? 0: method.hashCode());
	    }
	}

	Map<Class<?>,Class<?>> classCache = Collections
	    .synchronizedMap(new HashMap<Class<?>,Class<?>>());

	private int classDist(Class<?> start, Class<?> end) {
	    if (start.equals(end)) return 0;
	    if (!end.isAssignableFrom(start)) return Integer.MAX_VALUE;
	    int dist = classDist(start.getSuperclass(), end);
	    if (dist != Integer.MAX_VALUE) {
		return (dist + 1);
	    }
	    Class<?>[] interfaces = start.getInterfaces();
	    for (Class<?> c: interfaces) {
		int d = classDist(c, end);
		if (d < dist) {
		    dist = d;
		}
	    }
	    return (dist == Integer.MAX_VALUE)? dist: (dist + 1);
	}

	private  Class<?> findBestClass( Class<?> target, Class<?>[]carray) {
	    if (classCache.containsKey(target)) {
		return classCache.get(target);
	    }
	    Class<?> result = null;
	    try {
		ArrayList<Class<?>> calist = new ArrayList<>(carray.length);
		ArrayList<Class<?>> ialist = new ArrayList<>(carray.length);
		for (Class<?> c: carray) {
		    if (c.equals(target)) {
			result = c;
			return result;
		    }
		    if (c.isAssignableFrom(target)) {
			if (c.isInterface()) {
			    ialist.add(c);
			} else {
			    calist.add(c);
			}
		    }
		}
		int sz = calist.size() + ialist.size();
		carray = new Class<?>[sz];
		int i = 0;
		for (Class<?> c: calist) {
		    carray[i++] = c;
		}
		for (Class<?> c: ialist) {
		    carray[i++] = c;
		}
		int dist = Integer.MAX_VALUE;
		for (Class<?> c: carray) {
		    int d = classDist(target, c);
		    if (d < dist) {
			dist = d;
			result = c;
		    }
		}
		return result;
	    } finally {
		classCache.put(target, result);
	    }
	}

	private Method findMethod(String fname, Class<?>[] argclasses,
				  int[] argcount,
				  ClassArraySorter.ArgCountMap[] objectMaps,
				  Class<?> targetClass)
	    throws IllegalStateException, NoSuchMethodException,
		   IllegalArgumentException
	{
	    /*
	    System.out.format("findMethod: fname=%s, targetClass=%s\n",
			      fname, targetClass.getName());
	    if (argcount == null) {
		System.out.println("... argcount = " + argcount);
	    } else {
		System.out.print("... argcount = [");
		for (int i = 0; i < argcount.length; i++) {
		    if (i != 0) System.out.print(", ");
		    System.out.print(argcount[i]);
		}
		System.out.println("]");
	    }
	    */

	    for (int i = 0;  i < argclasses.length; i++) {
		if (argclasses[i] == null) {
		    continue;
		} else if (argclasses[i].equals(Double.class)) {
		    argclasses[i] = Number.class;
		}
	    }
	    int index = fname.lastIndexOf('.');
	    if (index >= 0 && targetClass != null) {
		String msg = errorMsg("notMethodName", fname);
		throw new IllegalArgumentException(msg);
	    }
	    String cname = (targetClass != null)? targetClass.getName():
		((index < 0)? null: fname.substring(0, index));
	    String mname = (index < 0)? fname: fname.substring(index+1);
	    boolean varargs = false;
	    MethodInfo minfo = (targetClass == null)?
		staticMethodMap.get(mname + ":" + argclasses.length):
		methodMap.get(mname + ":" + argclasses.length);
	    if (minfo == null) {
		minfo = (targetClass == null)?
		    staticMethodMap.get(mname): methodMap.get(mname);
		if (minfo != null) varargs = true;
	    }
	    if (minfo == null) {
		if (targetClass == null) {
		    throw new NoSuchMethodException
			(errorMsg("noMethodF", fname, argclasses.length));
		} else {
		    throw new NoSuchMethodException
			(errorMsg("noMethodM", fname, argclasses.length));
		}
	    }
	    Set<Class<?>> classSet = minfo.getClasses();
	    int len = classSet.size();
	    if (len == 0) {
		throw new IllegalStateException
		    (errorMsg("noClasses", fname, argclasses.length));
	    }
	    Class<?>[] carray = new Class<?>[len];
	    Class<?> clasz = null;
	    LinkedList<ClassArraySorter.Key> sorter = null;
	    if (len == 1) {
		classSet.toArray(carray);
		clasz = carray[0];
		sorter = minfo.getSorted(clasz);
	    } else {
		if (cname == null) {
		    throw new IllegalStateException
			(errorMsg("noClassName", fname, argclasses.length));
		}
		classSet.toArray(carray);
		if (targetClass == null) {
		    ArrayList<String> ambiguousNames = null;
		    for (Class<?> c: carray) {
			String name = c.getName().replace('$','.');
			// int index1 = name.lastIndexOf(cname);
			int index1 = name.endsWith(cname)?
			    name.length() - cname.length(): -1;
			if (index1 != -1) {
			    if (index1 == 0 || name.charAt(index1-1) == '.') {
				if (clasz != null) {
				    if (ambiguousNames == null) {
					ambiguousNames =
					    new ArrayList<String>();
					ambiguousNames
					    .add(clasz.getName()
						 .replace('$','.'));
				    }
				    ambiguousNames.add(name);
				}
				clasz = c;
				sorter = minfo.getSorted(clasz);
				if (index1 == 0) {
				    // if we have an exact match & it appears
				    // to be ambiguous, we've matched a class
				    // in the unnamed package
				    ambiguousNames = null;
				    break;
				}
			    }
			}
		    }
		    if (ambiguousNames != null) {
			String msg = errorMsg("multMethF", fname);
			for (String nm: ambiguousNames) {
			    msg += LINE_SEP + "--- " + nm;
			}
			throw new IllegalStateException(msg);
		    }
		} else {
		    clasz = findBestClass(targetClass, carray);
		    sorter = minfo.getSorted(clasz);
		}
	    }
	    if (sorter == null) {
		if (targetClass == null) {
		    throw new IllegalStateException
			(errorMsg("noSorterF", fname, argclasses.length));
		} else {
		    throw new IllegalStateException
			(errorMsg("noSorterM", fname, argclasses.length));
		}
	    }

	    ClassArraySorter.Key ourkey =
		new ClassArraySorter.Key(argclasses, true,
					 argcount, objectMaps);
	    if (varargs) {
		for (ClassArraySorter.Key key: sorter) {
		    if (key.isAssignableFrom(ourkey, true)) {
			return minfo.getMethod(clasz, key);
		    }
		}
	    } else {
		for (ClassArraySorter.Key key: sorter) {
		    if (key.isAssignableFrom(ourkey, true)) {
			return minfo.getMethod(clasz, key);
		    }
		}
	    }
	    if (targetClass == null) {
		throw new IllegalStateException
		    (errorMsg("noSorterResultF", fname, argclasses.length));
	    } else {
		throw new IllegalStateException
		    (errorMsg("noSorterResultM", fname, argclasses.length));
	    }
	}

	private Constructor<?> findConstr(String cname, Class<?>[] argclasses,
					  int[] argcount,
					  ClassArraySorter.ArgCountMap[]
					  objectMaps)
	    throws IllegalStateException, NoSuchMethodException
	{
	    for (int i = 0; i < argclasses.length; i++) {
		if (argclasses[i].equals(Double.class)) {
		    argclasses[i] = Number.class;
		}
	    }
	    int index = cname.lastIndexOf('.');
	    String sname = (index == -1)? cname: cname.substring(index+1);
	    boolean varargs = false;
	    MethodInfo minfo = constrMap.get(sname + ":" + argclasses.length);
	    if (minfo == null) {
		minfo = constrMap.get(sname);
		if (minfo != null) varargs = true;
	    }
	    if (minfo == null) {
		throw new IllegalStateException
		    (errorMsg("noMethodC", cname, argclasses.length));
	    }
	    Set<Class<?>> classSet = minfo.getClasses();
	    int len = classSet.size();
	    if (len == 0) {
		throw new IllegalStateException
		    (errorMsg("noClasses", cname, argclasses.length));
	    }
	    Class<?>[] carray = new Class<?>[len];
	    Class<?> clasz = null;
	    LinkedList<ClassArraySorter.Key> sorter = null;
	    if (len == 1) {
		classSet.toArray(carray);
		clasz = carray[0];
		sorter = minfo.getSorted(clasz);
	    } else {
		if (cname == null) {
		    throw new IllegalStateException
			(errorMsg("noClassName", cname, argclasses.length));
		}
		classSet.toArray(carray);
		ArrayList<String> ambiguousNames = null;
		for (Class<?> c: carray) {
		    String name = c.getName().replace('$', '.');
		    // int index1 = name.lastIndexOf(cname);
		    int index1 = name.endsWith(cname)?
			name.length() - cname.length(): -1;
		    if (index1 != -1) {
			if (index1 == 0 || name.charAt(index1-1) == '.') {
			    if (clasz != null) {
				// we have multiple matches.
				if (ambiguousNames == null) {
				    ambiguousNames = new ArrayList<String>();
				    ambiguousNames
					.add(clasz.getName().replace('$','.'));
				}
				ambiguousNames.add(name);
				continue;
			    }
			    clasz = c;
			    sorter = minfo.getSorted(clasz);
			    if (index1 == 0) {
				// if we have an exact match & it appears
				// to be ambiguous, we've matched a class
				// in the unnamed package
				ambiguousNames = null;
				break;
			    }
			}
		    }
		}
		if (ambiguousNames != null) {
		    String msg = errorMsg("multConstr", cname);
		    for (String nm: ambiguousNames) {
			msg += LINE_SEP + "--- " + nm;
		    }
		    throw new IllegalStateException(msg);
		}
	    }
	    if (sorter == null) {
		throw new IllegalStateException
		    (errorMsg("noSorterC", cname, argclasses.length));
	    }
	    ClassArraySorter.Key ourkey =
		new ClassArraySorter.Key(argclasses, true,
					 argcount, objectMaps);
	    if (varargs) {
		for (ClassArraySorter.Key key: sorter) {
		    if (key.isAssignableFrom(ourkey)) {
			return minfo.getConstructor(clasz, key);
		    }
		}
	    } else {
		for (ClassArraySorter.Key key: sorter) {
		    if (key.isAssignableFrom(ourkey, true)) {
			return minfo.getConstructor(clasz, key);
		    }
		}
	    }
	    throw new IllegalStateException
		(errorMsg("noSorterResultC", cname, argclasses.length));
	}

	private Method doCall(String fname, Object[] args, Object target)
	    throws IllegalAccessException,
		   IllegalArgumentException,
		   InvocationTargetException,
		   NoSuchMethodException
	{
	    Class<?>[] argclasses = new Class<?>[args.length];
	    int i = 0;
	    boolean sawf = false;
	    boolean needObjMaps = false;
	    for (Object obj: args) {
		if (obj instanceof ESPFunction) sawf = true;
		else if (obj instanceof ESPMethodRef) sawf = true;
		else if (obj instanceof ESPObject) {
		    sawf = true;
		    needObjMaps = true;
		}
		if (obj == null) {
		    argclasses[i++] = null;
		} else if (obj instanceof TypedNull) {
		    argclasses[i++] = ((TypedNull)obj).getType();
		} else {
		    argclasses[i++] = obj.getClass();
		}
	    }
	    int[] argcount;
	    ClassArraySorter.ArgCountMap[] objectMaps = null;
	    if (sawf) {
		argcount = new int[args.length];
		if (needObjMaps) {
		    objectMaps = new ClassArraySorter.ArgCountMap[args.length];
		}
		for (i = 0; i < args.length; i++) {
		    if (argclasses[i].equals(ESPFunction.class)) {
			ESPFunction f = (ESPFunction)args[i];
			argcount[i] = f.numberOfArguments();
		    } else if (argclasses[i].equals(ESPMethodRef.class)) {
			ESPMethodRef mr = (ESPMethodRef)args[i];
			argcount[i] = mr.numberOfArguments();
		    } else if (argclasses[i].equals(ESPObject.class)) {
			argcount[i] = ClassArraySorter.INTERFACE_TEST;
		        ClassArraySorter.ArgCountMap omap =
			    new ClassArraySorter.ArgCountMap();
			for (Map.Entry<String,Object> entry:
				 ((ESPObject)args[i]).entrySet()) {
			    String name = entry.getKey();
			    Object value = entry.getValue();
			    if (value instanceof ESPFunction) {
				ESPFunction f = (ESPFunction) value;
				omap.put(name, f.numberOfArguments());
			    }
			}
			objectMaps[i] = omap;
		    } else {
			argcount[i] = ClassArraySorter.NO_ARGCOUNT_TEST;
		    }
		}
	    } else {
		argcount = null;
	    }

	    Method m = findMethod(fname, argclasses, argcount, objectMaps,
				  ((target == null)? null: target.getClass()));
	    if (blockedMethods != null &&
		blockedMethods.contains(m)) {
		throw new IllegalStateException(errorMsg("blockedMethod"));
	    }
	    return m;
	}

	private Object doCall(String fname, Method m,
			      Object[] args, Object target)
	    throws IllegalAccessException,
		   IllegalArgumentException,
		   InvocationTargetException,
		   NoSuchMethodException
	{
	    int  i = 0;
	    Class<?>[] types = m.getParameterTypes();
	    Object[] oargs = new Object[types.length];
	    boolean varargs = m.isVarArgs();
	    int jmax = types.length - 1;
	    Object varray = null;
	    boolean needCopy = true;
	    if (varargs) {
		types[jmax] = types[jmax].getComponentType();
		if (args.length == types.length) {
		    Object lastarg = args[jmax];
		    if (lastarg != null) {
			Class lc = lastarg.getClass();
			if (lc.isArray()) {
			    lc = lc.getComponentType();
			    if (types[jmax].isAssignableFrom(lc)) {
				int len = Array.getLength(lastarg);
				varray = Array.newInstance(types[jmax], len);
				System.arraycopy(lastarg, 0, varray, 0, len);
				needCopy = false;
			    }
			}
		    }
		}
		varray = (varray != null)? varray:
		    Array.newInstance(types[jmax], args.length-jmax);
		oargs[jmax] = varray;
	    }
	    Object targ = oargs;
	    for (Object obj: args) {
		if (needCopy && varargs && targ == oargs &&  i == jmax) {
		    i = 0;
		    targ = varray;
		}
		int j = (targ == varray)? jmax: i;
		if (obj instanceof Number) {
		    Number n = (Number) obj;
		    if (types[j].equals(double.class)
			|| types[j].equals(Double.class)) {
			if (n instanceof Integer) {
			    int ix = n.intValue();
			    n = Double.valueOf((double)ix);
			} else if (n instanceof Long) {
			    long lx = n.longValue();
			    n = Double.valueOf((double)lx);
			}
		    } else if (types[j].equals(long.class)
			       || types[j].equals(Long.class)) {
			 if (n instanceof Double) {
			     double x = n.doubleValue();
			     if (x == Math.rint(x)) {
				 long ix = Math.round(x);
				 n = Long.valueOf(ix);
			     } else {
				 throw new IllegalArgumentException
				     (errorMsg("toLongFromDouble", n));
			     }
			 } else if (n instanceof Integer) {
			     int ix = ((Integer)n).intValue();
			     n = Integer.valueOf(ix);
			 }
		    } else if (types[j].equals(int.class)
			       || types[j].equals(Integer.class)) {
			if (n instanceof Double) {
			    double x = n.doubleValue();
			    if (x == Math.rint(x)) {
				long ix = Math.round(x);
				if (ix >= Integer.MIN_VALUE && ix
				    <= Integer.MAX_VALUE) {
				    int iv = (int) ix;
				    n = Integer.valueOf(iv);
				} else {
				    throw new IllegalArgumentException
					(errorMsg("toIntSize", n));
				}
			    } else {
				throw new IllegalArgumentException
				    (errorMsg("toIntFromDouble", n));
			    }
			} else if (n instanceof Long) {
			    long lv = n.longValue();
			    if (lv >= Integer.MIN_VALUE
				&& lv <= Integer.MAX_VALUE) {
				int iv = (int) lv;
				n = Integer.valueOf(iv);
			    } else {
				throw new IllegalArgumentException
				    (errorMsg("toIntSize", n));
			    }
			}
		    }
		    if (targ == oargs) {
			oargs[i++] = n;
		    } else {
			Array.set(varray, i++, n);
		    }
		} else if (obj instanceof ESPFunction
			   && types[j]
			   .getDeclaredAnnotation(FunctionalInterface.class)
			   != null) {
		    Object o = ((ESPFunction) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof ESPMethodRef
			   && types[j]
			   .getDeclaredAnnotation(FunctionalInterface.class)
			   != null) {
		    Object o = ((ESPMethodRef) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof ESPObject
			   && types[j].isInterface()) {
		    Object o = ((ESPObject) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof TypedNull) {
		    if (targ == oargs) {
			oargs[i++] = null;
		    } else {
			Array.set(varray, i++, null);
		    }
		} else {
		    if (targ == oargs) {
			oargs[i++] = obj;
		    } else {
			Array.set(varray, i++, obj);
		    }
		}
	    }
	    Class<?> dc = m.getDeclaringClass();

	    Object result = m.invoke(target, oargs);
	    if (result instanceof String || result instanceof Boolean
		|| result instanceof Number) {
		return  result;
	    } else if (allowedValues.size() > 0) {
		if (result != null) {
		    Class<?> rclass = result.getClass();
		    if (allowedValues.contains(rclass)) {
			return result;
		    } else if (allowedValuesCache.contains(rclass)) {
			return result;
		    } else {
			for (Class<?> c: allowedValues) {
			    if (c.isAssignableFrom(rclass)) {
				allowedValuesCache.add(rclass);
				return result;
			    }
			}
		    }
		    throw new RuntimeException
			(errorMsg("notAllowedReturnType", fname, rclass));
		}
		// return null;
		return new TypedNull(m.getReturnType());
	    }
	    throw new RuntimeException
		(errorMsg("nonNumberReturned", fname));
	}

	private Object doConstr(String cname, Object[] args)
	    throws IllegalAccessException,
		   IllegalArgumentException,
		   InvocationTargetException,
		   NoSuchMethodException,
		   InstantiationException
	{
	    Class<?>[] argclasses = new Class<?>[args.length];
	    int i = 0;
	    boolean sawf = false;
	    boolean needObjMaps = false;
	    for (Object obj: args) {
		if (obj instanceof ESPFunction) sawf = true;
		else if (obj instanceof ESPMethodRef) sawf = true;
		else if (obj instanceof ESPObject) {
		    sawf = true;
		    needObjMaps = true;
		}
		if (obj == null) {
		    argclasses[i++] = null;
		} else if (obj instanceof TypedNull) {
		    argclasses[i++] = ((TypedNull)obj).getType();
		} else {
		    argclasses[i++] = obj.getClass();
		}
	    }
	    int[] argcount;
	    ClassArraySorter.ArgCountMap[] objectMaps = null;
	    if (sawf) {
		argcount = new int[args.length];
		if (needObjMaps) {
		    objectMaps = new ClassArraySorter.ArgCountMap[args.length];
		}
		for (i = 0 ; i < args.length; i++) {
		    if (argclasses[i].equals(ESPFunction.class)) {
			ESPFunction f = (ESPFunction)args[i];
			argcount[i] = f.numberOfArguments();
		    } else if (argclasses[i].equals(ESPMethodRef.class)) {
			ESPMethodRef mr = (ESPMethodRef)args[i];
			argcount[i] = mr.numberOfArguments();
		    } else if (argclasses[i].equals(ESPObject.class)) {
			argcount[i] = ClassArraySorter.INTERFACE_TEST;
		        ClassArraySorter.ArgCountMap omap =
			    new ClassArraySorter.ArgCountMap();
			for (Map.Entry<String,Object> entry:
				 ((ESPObject)args[i]).entrySet()) {
			    String name = entry.getKey();
			    Object value = entry.getValue();
			    if (value instanceof ESPFunction) {
				ESPFunction f = (ESPFunction) value;
				omap.put(name, f.numberOfArguments());
			    }
			}
			objectMaps[i] = omap;
		    } else {
			argcount[i] = ClassArraySorter.NO_ARGCOUNT_TEST;
		    }
		}
	    } else {
		argcount = null;
	    }

	    Constructor<?>constr = findConstr(cname, argclasses,
					      argcount, objectMaps);
	    if (blockedConstructors != null &&
		blockedConstructors.contains(constr)) {
		throw new IllegalStateException(errorMsg("blockedConstructor"));
	    }
	    i = 0;
	    Class<?>[] types = constr.getParameterTypes();
	    Object[] oargs = new Object[types.length];
	    boolean varargs = constr.isVarArgs();
	    int jmax = types.length - 1;
	    Object varray = null;
	    if (varargs) {
		types[jmax] = types[jmax].getComponentType();
		varray = Array.newInstance(types[jmax], args.length-jmax);
		oargs[jmax] = varray;
	    }
	    Object targ = oargs;
	    for (Object obj: args) {
		if (varargs && targ == oargs && i == jmax) {
		    i = 0;
		    targ = varray;
		}
		int j = (targ == varray)? jmax: i;
		if (obj instanceof Number) {
		    Number n = (Number) obj;
		    if (types[j].equals(double.class)
			|| types[j].equals(Double.class)) {
			if (n instanceof Integer) {
			    int ix = n.intValue();
			    n = Double.valueOf((double)ix);
			} else if (n instanceof Long) {
			    long lx = n.longValue();
			    n = Double.valueOf((double)lx);
			}
		    } else if (types[j].equals(long.class)
			       || types[j].equals(Long.class)) {
			 if (n instanceof Double) {
			     double x = n.doubleValue();
			     if (x == Math.rint(x)) {
				 long ix = Math.round(x);
				 n = Long.valueOf(ix);
			     } else {
				 throw new IllegalArgumentException
				     (errorMsg("toLongFromDouble", n));
			     }
			 } else if (n instanceof Integer) {
			     int ix = ((Integer)n).intValue();
			     n = Integer.valueOf(ix);
			 }
		    } else if (types[j].equals(int.class)
			       || types[j].equals(Integer.class)) {
			if (n instanceof Double) {
			    double x = n.doubleValue();
			    if (x == Math.rint(x)) {
				long ix = Math.round(x);
				if (ix >= Integer.MIN_VALUE && ix
				    <= Integer.MAX_VALUE) {
				    int iv = (int) ix;
				    n = Integer.valueOf(iv);
				} else {
				    throw new IllegalArgumentException
					(errorMsg("toIntSize", n));
				}
			    } else {
				throw new IllegalArgumentException
				    (errorMsg("toIntFromDouble", n));
			    }
			} else if (n instanceof Long) {
			    long lv = n.longValue();
			    if (lv >= Integer.MIN_VALUE
				&& lv <= Integer.MAX_VALUE) {
				int iv = (int) lv;
				n = Integer.valueOf(iv);
			    } else {
				throw new IllegalArgumentException
				    (errorMsg("toIntSize", n));
			    }
			}
		    }
		    if (targ == oargs) {
			oargs[i++] = n;
		    } else {
			Array.set(varray, i++, n);
		    }
		} else if (obj instanceof ESPFunction
			   && types[j]
			   .getDeclaredAnnotation(FunctionalInterface.class)
			   != null) {
		    Object o = ((ESPFunction) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof ESPMethodRef
			   && types[j]
			   .getDeclaredAnnotation(FunctionalInterface.class)
			   != null) {
		    Object o = ((ESPMethodRef) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof ESPObject
			   && types[i].isInterface()) {
		    Object o = ((ESPObject) obj).convert(types[i]);
		    if (targ == oargs) {
			oargs[i++] = o;
		    } else {
			Array.set(varray, i++, o);
		    }
		} else if (obj instanceof TypedNull) {
		    if (targ == oargs) {
			oargs[i++] = null;
		    } else {
			Array.set(varray, i++, null);
		    }
		} else {
		    if (targ == oargs) {
			oargs[i++] = obj;
		    } else {
			Array.set(varray, i++, obj);
		    }
		}
	    }
	    Class<?> cclass = constr.getDeclaringClass();
	    if (Number.class.isAssignableFrom(cclass)
		|| cclass.equals(Boolean.class)
		|| cclass.equals(String.class)) {
		return constr.newInstance(oargs);
	    } else if (allowedValues.size() > 0) {
		if (allowedValues.contains(cclass)) {
		    return constr.newInstance(oargs);
		} else if (allowedValuesCache.contains(cclass)) {
		    return constr.newInstance(oargs);
		} else {
		    for (Class<?> c: allowedValues) {
			if (c.isAssignableFrom(cclass)) {
			    allowedValuesCache.add(cclass);
			    return constr.newInstance(oargs);
			}
		    }
		}
		throw new RuntimeException
		    (errorMsg("notAllowedClass", cname));
	    }
	    throw new RuntimeException
		(errorMsg("notAllowedClass", cname));
	}

	public void eval(String orig) throws ObjectParser.Exception {
	    if (stackTracing.get()) {
		System.err.println("    CALLING eval");
	    }
	    Token op;
	    while (hasOps()) {
		evalOnce(orig);
	    }
	}



	@SuppressWarnings("unchecked")
	private void unsafeAccept(Consumer c, Object o) {
	    c.accept(o);
	}


	private void evalOnce(String orig) throws ObjectParser.Exception {
	    Token opToken = popOp();
	    Operator op = opToken.getType();
	    if (stackTracing.get()) {
		if (op == Operator.QVAR
		    || op == Operator.QQVAR
		    || op == Operator.VAR) {
		    System.err.format("        EVAL %s (\"%s\")\n",
				      op, opToken.getValue());
		    System.err.format("        ... ASSIGNING %s\n",
				      peekValue());
		} else {
		    System.err.format("        EVAL %s (\"%s\")\n",
				      op, opToken.getName());
		}
	    }
	    String prop = null;
	    String varname;
	    Object object;
	    Object target;
	    switch (op)  {
	    case QVAR:
	    case QQVAR:
	    case VAR:
		varname = (String)opToken.getValue();
		object = peekValue();
		if (hasAmap()) {
		    putInAmap(varname, object);
		} else {
		    vmap.get().put(varname, object);
		}
		break;
	    case SWAP:
		{
		    String name2 = (String) popValue();
		    String name1 = (String) popValue();
		    boolean hasAmap = hasAmap();
		    Object o1;
		    Object o2;
		    Map<String,Object> vm = vmap.get();
		    if (hasAmap && amapContains(name1)) {
			o1 = getFromAmap(name1);
		    } else if (vm.containsKey(name1)) {
			o1 = vm.get(name1);
		    } else {
			String msg = errorMsg("noValue", name1);
			throw new ObjectParser.Exception
			    (msg, opToken.getFileName(), orig,
			     opToken.getIndex());
		    }
		    if (hasAmap && amapContains(name2)) {
			o2 = getFromAmap(name2);
		    } else if (vm.containsKey(name2)) {
			o2 = vm.get(name2);
		    } else {
			String msg = errorMsg("noValue", name2);
			throw new ObjectParser.Exception
			    (msg,opToken.getFileName(), orig,
			     opToken.getIndex());
		    }
		    if (hasAmap && amapContains(name1)) {
			putInAmap(name1, o2);
		    } else {
			vm.put(name1, o2);
		    }
		    if (hasAmap && amapContains(name2)) {
			putInAmap(name2, o1);
		    } else {
			vm.put(name2, o1);
		    }
		    pushValue(Boolean.TRUE);
		}
		break;
	    case ASSIGN1:
		object = popValue();
		varname = (String)(popValue());
		if (hasAmap() && amapContains(varname)) {
		    putInAmap(varname, object);
		} else if (opToken.containsKeyInMap(varname)) {
		    opToken.putInMap(varname, object);
		} else {
		    String msg = errorMsg("noValue", varname);
		    throw new ObjectParser.Exception(msg,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		pushValue(object);
		break;
	    case ASSIGN2:
		object = popValue();
		Object oindex = popValue();
		target = popValue();
		try {
		    if (target instanceof ESPObject) {
			((ESPObject)target).putObject((String)oindex, object);
		    } else if (target instanceof ESPArray) {
			((ESPArray)target).setObject((int)(Integer)oindex,
						    object);
		    } else if (target != null && target.getClass().isArray()) {
			Array.set(target, (int)(Integer)oindex, object);
		    } else {
			String msg = errorMsg("notIndexed");
			throw new IllegalArgumentException(msg);
		    }
		    pushValue(object);
		} catch (java.lang.Exception e) {
		    String msg = errorMsg("indexAssigned");
		    throw new ObjectParser.Exception(msg, e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case METHOD_REF:
		// This call sets up a method reference for later
		// use.
		try {
		    String methodName = (String)popValue();
		    target = popValue();
		    if (target == null) {
			throw new IllegalStateException
			    (errorMsg("nullMethodRefTarget"));
		    }
		    pushValue(createMethodRef2(target, methodName));
		} catch (java.lang.Exception e) {
		    String msg = errorMsg("methodRefFailed");
		    throw new ObjectParser.Exception(msg, e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case QMARK:
		break;
	    case CBRACKET:
	    case OBJCLOSEBRACE:
	    case ACTIVE_COMMA:
		try {
		    prop = (String) opToken.getValue();
		    if (prop != null) {
			Object val = popValue();
			ESPObject obj = (ESPObject)popValue();
			obj.putObject(prop, val);
			if (val instanceof ESPFunction) {
			    ESPFunction f = (ESPFunction) val;
			    if (f.isMethod()) {
				f.setThisObject(obj);
			    }
			}
			pushValue(obj);
		    } else {
			Object val = popValue();
			ESPArray array = (ESPArray)popValue();
			array.addObject(val);
			pushValue(array);
		    }
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case PLUS:
		try {
		    Object o2 = popValue();
		    Object o1 = popValue();
		    if (o1 instanceof String || o2 instanceof String) {
			String s1 = (o1 == null)? "null": o1.toString();
			String s2 = (o2 == null)? "null": o2.toString();
			pushValue(s1 + s2);
		    } else {
			Number n2 = (Number) o1;
			Number n1 = (Number) o2;
			pushValue(add(n1, n2));
		    }
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case UNARY_MINUS:
		try {
		    pushValue(changeSign((Number)popValue()));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case BINARY_MINUS:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(sub(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case TIMES:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(mult(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case DIVIDEBY:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(div(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case MOD:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(mod(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case MATH_MOD:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(mathmod(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LSHIFT:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(lshift(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case RSHIFT:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(rshift(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case URSHIFT:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(urshift(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case NOT:
		try {
		    Number n = (Number) popValue();
		    pushValue(not(n));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LNOT:
		try {
		    Boolean b = (Boolean) popValue();
		    pushValue(lnot(b));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case THROW:
		{
		    String msg;
		    try {
			msg = (String) popValue();
		    } catch (java.lang.Exception e) {
			throw new ObjectParser.Exception(e.getMessage(), e,
							 opToken.getFileName(),
							 orig,
							 opToken.getIndex());
		    }
		    throw new ObjectParser.Exception(msg, opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
	    case AND:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(and(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case OR:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(or(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case XOR:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(xor(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LAND:
		try {
		    Boolean b2 = (Boolean) popValue();
		    Boolean b1 = (Boolean) popValue();
		    pushValue(land(b1, b2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LOR:
		try {
		    Boolean b2 = (Boolean) popValue();
		    Boolean b1 = (Boolean) popValue();
		    pushValue(lor(b1, b2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case GT:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(gt(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case GE:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(ge(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LT:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(lt(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case LE:
		try {
		    Number n2 = (Number) popValue();
		    Number n1 = (Number) popValue();
		    pushValue(le(n1, n2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case INSTANCEOF:
		try {
		    String className = (String)popValue();
		    if (className.equals("boolean")) className = "Boolean";
		    else if (className.equals("double")) className = "Double";
		    else if (className.equals("int")) className = "Integer";
		    else if (className.equals("long")) className = "Long";
		    Object obj = popValue();
		    Class<?> clasz = findClass(className);
		    if (obj == null) {
			pushValue(clasz.equals(Object.class)? Boolean.TRUE:
				  Boolean.FALSE);
		    } else if (obj instanceof TypedNull) {
			TypedNull tn = (TypedNull) obj;
			pushValue(clasz.isAssignableFrom(tn.getType()));
		    } else {
			pushValue(clasz.isAssignableFrom(obj.getClass()));
		    }
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case EQ:
		try {
		    Object o2 = popValue();
		    Object o1 = popValue();
		    if (o1 instanceof TypedNull) o1 = null;
		    if (o2 instanceof TypedNull) o2 = null;
		    pushValue(eq(o1, o2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case NE:
		try {
		    Object o2 = popValue();
		    Object o1 = popValue();
		    if (o1 instanceof TypedNull) o1 = null;
		    if (o2 instanceof TypedNull) o2 = null;
		    pushValue(neq(o1, o2));
		} catch (java.lang.Exception e) {
		    throw new ObjectParser.Exception(e.getMessage(), e,
						     opToken.getFileName(),
						     orig,
						     opToken.getIndex());
		}
		break;
	    case CPAREN:
		{
		    Token ftoken = opToken.getFunct();
		    Operator oper = ftoken.getType();
		    String fname = ftoken.getName();
		    Object value = ftoken.getValueTL();
		    int nargs = opToken.getArgCount();
		    Object[] args = null;

		    if (oper /*ftoken.getType()*/ == Operator.IMPORT) {
			int argcount = opToken.getArgCount();
			if (argcount == 0 || argcount > 2) {
			    String msg = errorMsg("badImportArgCount");
			    throw new
				ObjectParser.Exception
				(msg, ftoken.getFileName(), orig,
				 ftoken.getIndex());
			}
			Object o2 = popValue();
			Object o1 = (argcount == 1)? null: popValue();
			if (o1 == null || o1 instanceof String) {
			    try {
				importClasses((String)o1, o2);
				// pushValue(null);
			    } catch (java.lang.Exception e) {
				String pn = (String) o1;
				String msg = errorMsg("badImport", pn);
				throw new
				    ObjectParser.Exception
				    (msg, e, ftoken.getFileName(), orig,
				     ftoken.getIndex());
			    }
			} else {
			    String msg = errorMsg("badImportArg1");
			    throw new ObjectParser.Exception
				(msg, ftoken.getFileName(), orig,
				 ftoken.getIndex());
			}
			return;
		    } else if (value instanceof ESPFunction) {
			ESPFunction ef = (ESPFunction)value;
			int index = nargs -1;
			String[] parms = ef.args;
			HashMap<String,Object> argmap = new HashMap<>();
			boolean efIsMethod = ef.isMethod();
			if (efIsMethod) {
			    argmap.put("this", ef.getThisObject());
			}
			for (int i = 0; i < nargs; i++) {
			    Object val = popValue();
			    if (val == VOID) {
				String msg = errorMsg("voidArg");
				String filename = opToken.getFileName();
				int ind = opToken.getIndex();
				throw new ObjectParser.Exception(msg, filename,
								 orig, ind);
			    }
			    argmap.put(parms[index--],
				       ((val == NULL)? null: val));
			}
			if (ef.willSync) {
			    Object syncObject = ExpressionParser.this;
			    synchronized(syncObject) {
				UniTreeNode<Map<String,Object>> savedArgTree
				    = argTreeTL.get();
				try {
				    UniTreeNode<Map<String,Object>> newArgTree =
					UniTreeNode.addTo(argmap, ef.argTree);
				    argTreeTL.set(newArgTree);
				    // pushArgMap(argmap);
				    pushArgMap(newArgTree);
				    pushBases();
				    for (LinkedList<Token> tokenQueue:
					     ef.tokenQueues) {
					// if a previous statement has left
					//  something on the stack, ignore it.
					while(hasValue()) {
					    popValue();
					}
					parseExpression(tokenQueue, ef.orig);
				    }
				    eval(ef.orig);
				    return;
				} finally {
				    popBases();
				    popArgMap();
				    argTreeTL.set(savedArgTree);
				}

			    }
			} else {
			    UniTreeNode<Map<String,Object>> savedArgTree
				= argTreeTL.get();
			    try {
				UniTreeNode<Map<String,Object>> newArgTree =
				    UniTreeNode.addTo(argmap, ef.argTree);
				argTreeTL.set(newArgTree);
				// pushArgMap(argmap);
				pushArgMap(newArgTree);
				pushBases();
				for (LinkedList<Token> tokenQueue:
					 ef.tokenQueues) {
				    // if a previous statement has left
				    //  something on the stack, ignore it.
				    while(hasValue()) {
					popValue();
				    }
				    parseExpression(tokenQueue, ef.orig);
				}
				eval(ef.orig);
				return;
			    } finally {
				popBases();
				popArgMap();
				argTreeTL.set(savedArgTree);
			    }
			}
		    } else {
			args = new Object[nargs];
			int index = nargs -1;
			for (int i = 0; i < nargs; i++) {
			    Object val = popValue();
			    if (val == VOID) {
				String msg = errorMsg("voidArg");
				String filename = opToken.getFileName();
				int ind = opToken.getIndex();
				throw new ObjectParser.Exception(msg, filename,
								 orig, ind);
			    }
			    args[index--] = ((val == NULL)? null: val);
			}
		    }
		    try {
			if (oper == Operator.METHOD) {
			    target = popValue();
			    if (target instanceof ESPFunction
				&& fname.equals("invoke")) {
				ESPFunction f = (ESPFunction)target;
				pushValue(f.invoke(args));
				break;
			    } else if (target instanceof ESP) {
				ESP ep = (ESP) target;
				Object results = null;
				if (fname.equals("isESPArray") &&
				    args.length == 1) {
				    results = ep.isESPArray(args[0]);
				} else if (fname.equals("isArray") &&
				    args.length == 1) {
				    results = ep.isArray(args[0]);
				} else if (fname.equals("isJavaArray")
					   && args.length == 1) {
				    results = ep.isJavaArray(args[0]);
				} else if (fname.equals("isObject")
					   && args.length == 1) {
				    results = ep.isObject(args[0]);
				} else if (fname.equals("typeForArrayOf")
					   && args.length == 1) {
				    Class<?> clz = (Class<?>)args[0];
				    results = ep.typeForArrayOf(clz);
				} else if (fname.equals("typeof")
					   && args.length == 1) {
				    results = ep.typeof(args[0]);
				} else if (fname.equals("ESPObjectType")
					   && args.length == 0) {
				    results = ep.ESPObjectType();
				} else if (fname.equals("ESPArrayType")
					   && args.length == 0) {
				    results = ep.ESPArrayType();
				} else if (fname.equals("set")
					   && args.length == 2) {
				    ep.set((String)args[0], args[1]);
				} else if (fname.equals("set")
					   && args.length == 3) {
				    ep.set((String)args[0], args[1], args[2]);
				} else if (fname.equals("get")
					   && args.length == 2) {
				    results = ep.get((String)args[0], args[1]);
				} else if (fname.equals("get")
					   && args.length == 1) {
				    results = ep.get((String)args[0]);
				} else if (fname.equals("size")
					   && args.length == 1) {
				    results = ep.size(args[0]);
				} else if (fname.equals("exists")
					   && args.length == 1) {
				    results = ep.exists((String)args[0]);
				} else if (fname.equals("globals")
					   && args.length == 0) {
				    ESPArray resultArray = new ESPArray();
				    for (String key: ep.globals()) {
					resultArray.add(key);
				    }
				    results = resultArray;
				} else if (fname.equals("getGlobal")
					   && args.length == 1) {
				    results = ep.getGlobal((String)args[0]);
				} else if (fname.equals("getGlobal")
					   && args.length == 2) {
				    results = ep.getGlobal((String)args[0],
							   args[1]);
				} else if (fname.equals("getReader")
					   && args.length == 0) {
				    results = ep.getReader();
				} else if (fname.equals("getWriter")
					   && args.length == 0) {
				    results = ep.getWriter();
				} else if (fname.equals("getErrorWriter")
					   && args.length == 0) {
				    results = ep.getErrorWriter();
				} else if (fname.equals("importClasses")
					   && args.length == 2) {
				    results = ep.importClasses((String)args[0],
							       args[1]);
				} else if (fname.equals("finishImport")
					   && args.length == 0) {
				    results = ep.finishImport();
				} else if (fname.equals("newJavaArray")
					   && args.length > 1
					   && args[0] instanceof Class) {
				    Class<?> c = (Class<?>)args[0];
				    int[] arguments = new int[args.length-1];
				    int i = 0;
				    int j = 1;
				    while (j < args.length) {
					if (!(args[j] instanceof Integer)) {
					    String msg =
						errorMsg("notIntElement");
					    throw new ObjectParser.Exception
						(msg, opToken.getFileName(),
						 orig, opToken.getIndex());
					}
					arguments[i] = ((Integer) args[j])
					    .intValue();
					if (arguments[i] < 0) {
					    String msg =
						errorMsg("notPosElement");
					    throw new ObjectParser.Exception
						(msg, opToken.getFileName(),
						 orig, opToken.getIndex());
					}
					i++; j++;
				    }
				    results = Array.newInstance(c, arguments);
				} else if (fname.equals("blockImports")
					   && args.length == 0) {
				    results = ep.blockImports();
				} else if (fname.equals("blockConstructor")
					   && args.length >= 1) {
				    Class<?> clz = (Class<?>) args[0];
				    Class<?>[] arguments =
					new Class<?>[args.length - 1];
				    for (int i = 1; i < args.length; i++) {
					arguments[i-1] = (Class<?>)args[i];
				    }
				    results = ep.blockConstructor(clz,
								  arguments);
				} else if (fname.equals("blockMethod")
					   && args.length >= 2) {
				    Class<?> clz = (Class<?>) args[0];
				    String mname = (String) args[1];
				    Class<?>[] arguments =
					new Class<?>[args.length - 2];
				    for (int i = 2; i < args.length; i++) {
					arguments[i-2] = (Class<?>)args[i];
				    }
				    results = ep.blockMethod(clz, mname,
							     arguments);
				    /*
				} else if (fname.equals("blockScriptingCreate")
					   && args.length == 0) {
				    results = ep.blockScriptingCreate();
				    */
				} else if (fname.equals("generateDocs")
					   && args.length == 2
					   && (args[0] instanceof PrintWriter
					       || args[0] instanceof
					       OutputStream)
					   && args[1] instanceof JSArray) {
				    PrintWriter w = 
					args[0] instanceof PrintWriter?
					(PrintWriter) args[0]:
					new PrintWriter((OutputStream)args[0],
							true, UTF8);
				    JSArray docs = (JSArray) args[1];
				    results = ep.generateDocs(w, docs);
				} else {
				    int len = args.length;
				    String msg =
					errorMsg("noMethodF", fname, len);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				}
				pushValue(results);
				break;
			    }
			    Method m;
			    if (target instanceof BaseStream) {
				// special case.
				m = findStreamMethod(fname, target, args);
				if (m == null) {
				    String msg =
					errorMsg("NoStreamMethod", fname, args);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				}

			    } else if (target instanceof ESPArray) {
				ObjMethodDescriptor d =
				    new ObjMethodDescriptor(fname, args.length);
				m = eparrayMethods.get(d);
				if (m == null) {
				    int alen = args.length;
				    String msg =
					errorMsg("noMethodM", fname, alen);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				} else {
				    if (fname.equals("forEach")) {
					Object ourArg = args[0];
					if (ourArg instanceof ESPFunction) {
					    ourArg = ((ESPFunction)ourArg)
						.convert(Consumer.class);
					} else if (ourArg
						   instanceof ESPMethodRef) {
					    ourArg = ((ESPMethodRef)ourArg)
						.convert(Consumer.class);
					} else {
					    // wrong argument type
					String msg =
					    errorMsg("notForEachConsumer");
					throw new ObjectParser.Exception
					    (msg, opToken.getFileName(), orig,
					     opToken.getIndex());
					}
					pushValue(m.invoke(target,ourArg));
				    } else {
					pushValue(m.invoke(target, args));
				    }
				}
				break;
			    } else if (target instanceof ESPObject) {
				ObjMethodDescriptor d =
				    new ObjMethodDescriptor(fname, args.length);
				m = epobjMethods.get(d);
				if (m == null) {
				    ESPObject o = (ESPObject) target;
				    Object fobj = o.get(fname);
				    if (fobj instanceof ESPFunction) {
					ESPFunction f = (ESPFunction) fobj;
					    pushValue(f.invoke(args));
					// pushValue(f.invoke(args));
					// terminate this case
					break;
				    } else {
					int len = args.length;
					String msg =
					    errorMsg("noMethodM",fname,len);
					throw new ObjectParser.Exception
					    (msg, opToken.getFileName(), orig,
					     opToken.getIndex());
				    }
				}
			    } else if (target != null
				       && target.getClass().isArray()) {
				if (fname.equals("get")
				    && args.length == 1) {
				    int index = (int)(Integer)args[0];
				    pushValue(Array.get(target, index));
				} else if (fname.equals("set")
				    && args.length == 2) {
				    int index = (int)(Integer)args[0];
				    Object val = args[1];
				    Array.set(target, index, val);
				    pushValue(val);
				} else if (fname.equals("size") &&
					   args.length == 0) {
				    pushValue(Array.getLength(target));
				} else if (fname.equals("stream")
					   && args.length == 0) {
				    int len = Array.getLength(target);
				    Stream<?> stream = IntStream
					.range(0, len)
					.mapToObj((index) -> {
						return Array.get(target, index);
					    });
				    pushValue(stream);
				} else if (fname.equals("toStream") &&
					   args.length == 1) {
				    int len = Array.getLength(target);
				    Class<?> tsclass = (Class<?>)args[0];
				    if (tsclass == null) {
					String msg = errorMsg("nullArg", 1);
					throw new ObjectParser.Exception
					    (msg, opToken.getFileName(), orig,
					     opToken.getIndex());
				    }
				    if (tsclass.equals(int.class)) {
					IntStream stream = IntStream
					    .range(0, len)
					    .map((index) -> {
						    return Array.getInt
							(target, index);
						});
					pushValue(stream);
				    } else if (tsclass.equals(long.class)) {
					LongStream stream = IntStream
					    .range(0, len)
					    .mapToLong((index) -> {
						    return Array.getLong
							(target, index);
						});
					pushValue(stream);
				    } else if (tsclass.equals(double.class)) {
					DoubleStream stream = IntStream
					    .range(0, len)
					    .mapToDouble((index) -> {
						    return Array.getDouble
							(target, index);
						});
					pushValue(stream);
				    } else {
					String msg = errorMsg("toStreamErr");
					throw new ObjectParser.Exception
					    (msg, opToken.getFileName(), orig,
					     opToken.getIndex());
				    }
				} else if (fname.equals("parallelStream")
					   && args.length == 0) {
				    int len = Array.getLength(target);
				    Stream<?> stream = IntStream
					.range(0, len)
					.mapToObj((index) -> {
						return Array.get(target, index);
					    })
					.parallel();
				    pushValue(stream);
				} else if (fname.equals("forEach")
					   && args.length == 1) {
				    Object obj = args[0];
				    if (obj instanceof ESPFunction) {
					int len = Array.getLength(target);
					ESPFunction f = (ESPFunction) obj;
					if (f.numberOfArguments() != 1) {
					    String msg =
						errorMsg("notConsumer");
					    throw new ObjectParser.Exception
						(msg, opToken.getFileName(),
						 orig, opToken.getIndex());
					}
					for (int i = 0; i < len; i++) {
					    f.invoke(Array.get(target, i));
					}
					pushValue(null);
				    } else if (obj instanceof ESPMethodRef) {
					int len = Array.getLength(target);
					/*
					Stream<?> stream = IntStream
					    .range(0, len)
					    .mapToObj((index) -> {
						    return Array.get(target,
								     index);
						});
					stream.forEach
					    (((ESPMethodRef) obj)
					     .convert(Consumer.class));
					*/
					Consumer c = ((ESPMethodRef)obj)
					    .convert(Consumer.class);
					for (int i = 0; i < len; i++) {
					    // c.accept(Array.get(target, i));
					    unsafeAccept(c,
							 Array.get(target, i));
					}
					pushValue(null);
				    } else {
					String msg =
					    errorMsg("notConsumer");
					throw new ObjectParser.Exception
					    (msg, opToken.getFileName(),
					     orig, opToken.getIndex());
				    }
				} else if (fname.equals("toESPArray") &&
					   args.length == 0) {
				    pushValue(new ESPArray(target, 1));
				} else if (fname.equals("toESPMatrix") &&
					   args.length == 0) {
				    pushValue(new ESPArray(target, 2));
				} else {
				    int len = args.length;
				    String msg =
					errorMsg("noMethodM",fname,len);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				}
				break;
			    } else if (target instanceof ESPMethodRef) {
				pushValue(((ESPMethodRef)target).invoke(args));
				break;
			    } else {
				m = doCall(fname, args, target);
			    }
			    if (m.getReturnType().equals(void.class)) {
				if (!valueStackEmpty()) {
				    String msg = errorMsg("nestedCallM", fname);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				}
				doCall(fname, m, args, target);
			    } else {
				pushValue(doCall(fname, m, args, target));
			    }
			} else if (oper == Operator.FUNCTION) {
			    Method m = doCall(fname, args, null);
			    if (m.getReturnType().equals(void.class)) {
				if (!valueStackEmpty()) {
				    String msg = errorMsg("nestedCallP", fname);
				    throw new ObjectParser.Exception
					(msg, opToken.getFileName(), orig,
					 opToken.getIndex());
				}
				doCall(fname, m, args, null);
			    } else {
				pushValue(doCall(fname, m, args, null));
			    }
			} else if (oper == Operator.CONSTRUCTOR) {
			    pushValue(doConstr(fname, args));
			}
		    } catch (java.lang.Exception e) {
			String msg =
			    errorMsg("fcallFailed", fname, args.length);
			throw new ObjectParser.Exception
			    (msg, e, opToken.getFileName(), orig,
			     opToken.getIndex());
		    }
		}
		break;
	    case FINISH_IMPORT:
		try {
		    finishImport();
		} catch (IllegalAccessException ea) {
		    String msg = errorMsg("finishImportFailed");
		    throw new ObjectParser.Exception
			(msg, ea, opToken.getFileName(), orig,
			 opToken.getIndex());
		}
		break;
	    }
	}

	Comparator<Field> fieldComparator = new Comparator<>() {
		// sort lexically on class names and then on field
		// names within a given class
		public int compare(Field f1, Field f2) {
		    String cname1 = f1.getDeclaringClass().getName();
		    String cname2 = f2.getDeclaringClass().getName();
		    int status = cname1.compareTo(cname2);
		    if (status == 0) {
			String fname1 = f1.getName();
			String fname2 = f2.getName();
			return fname1.compareTo(fname2);
		    } else {
			return status;
		    }
		}
	    };

	private boolean fieldsSorted = false;

	public TemplateProcessor.KeyMapList keylistForConstants() {
	    if (!fieldsSorted) {
		Collections.sort(fieldList, fieldComparator);
		fieldsSorted = true;
	    }
	    TemplateProcessor.KeyMapList list =
		new TemplateProcessor.KeyMapList();
	    for (Field f: fieldList) {
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		Class<?> clasz = f.getDeclaringClass();
		String fn = f.getName();
		try {
		    URL url = findDocURL(clasz, fn);
		    if (url != null) {
			kmap.put("href", url.toString());
			// String cn = clasz.getName().replace('$','.');
			String cn = clasz.getCanonicalName();
			kmap.put("item", cn + "." + fn);
			list.add(kmap);
		    }
		} catch (Exception e) {
		}
	    }
	    return list;
	}


	public List<String> getConstants() {
	    int[] array = sa.findRange("|").toArray();
	    Arrays.sort(array);
	    ArrayList<String> list = new ArrayList<>(array.length-1);
	    for (int i = 0; i < array.length-1; i++) {
		int start = array[i]+1;
		int end = array[i+1];
		String s = new String(enumsAndFields, start, end-start);
		list.add(s);
	    }
	    return list;
	}

	public TemplateProcessor.KeyMapList keylistForReturnClasses() {
	    TemplateProcessor.KeyMapList list =
		new TemplateProcessor.KeyMapList();
	    for (Class<?> clasz: getReturnClasses()) {
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		Class<?> c = clasz;
		while (c.isArray()) {
		    c = c.getComponentType();
		}
		URL url = findDocURL(c);
		if (url != null) {
		    kmap.put("href", url.toString());
		    // kmap.put("item", clasz.getName().replace('$','.'));
		    kmap.put("item", clasz.getCanonicalName());
		    list.add(kmap);
		}
	    }
	    return list;
	}

	public ArrayList<Class<?>> getReturnClasses() {
	    ArrayList<Class<?>> list = new ArrayList<>(allowedValues);
	    Collections.sort(list, classComparator);
	    return list;
	}

	public TemplateProcessor.KeyMapList keylistForArgumentClasses() {
	    TemplateProcessor.KeyMapList list =
		new TemplateProcessor.KeyMapList();
	    for (Class<?> clasz: getArgumentClasses()) {
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		Class<?> c = clasz;
		while (c.isArray()) {
		    c = c.getComponentType();
		}
		URL url = findDocURL(c);
		if (url != null) {
		    kmap.put("href", url.toString());
		    // kmap.put("item", clasz.getName().replace('$','.'));
		    kmap.put("item", clasz.getCanonicalName());
		    list.add(kmap);
		}
	    }
	    return list;
	}

	public ArrayList<Class<?>> getArgumentClasses() {
	    ArrayList<Class<?>> list = new ArrayList<>(allowedArgs);
	    Collections.sort(list, classComparator);
	    return list;
	}

	private List<String> getFromMethodMap(Map<String,MethodInfo> map,
					      boolean showMethod)
	{
	    Collection<MethodInfo> collection = map.values();
	    ArrayList<String> list = new ArrayList<>(collection.size());
	    StringBuilder sb = new StringBuilder();
	    for (Map.Entry<String,MethodInfo> entry: map.entrySet()) {
		String method = entry.getKey();
		int index = method.indexOf (':');
		if (index >= 0) {
		    method = method.substring(0, index);
		}
		MethodInfo mi = entry.getValue();
		for (Class<?> clasz: mi.getClasses()) {
		    for (ClassArraySorter.Key key: mi.getSorted(clasz)) {
			sb.setLength(0);
			sb.append(clasz.getName().replace('$', '.'));
			if (showMethod) {
			    sb.append(": ");
			    sb.append(method);
			}
			sb.append('(');
			boolean first = true;
			for (Class<?> c: key.toArray()) {
			    if (first) {
				first = false;
			    } else {
				sb.append(',');
			    }
			    if (c.equals(Integer.class)) {
				sb.append("int");
			    } else if (c.equals(Long.class)) {
				sb.append("long");
			    } else if (c.equals(Number.class)) {
				sb.append("double");
			    } else {
				sb.append(c.getName().replace('$', '.'));
			    }
			}
			sb.append(')');
			list.add(sb.toString());
		    }
		}
	    }
	    return list;
	}

	private Comparator<TemplateProcessor.KeyMap> methodComparator = new
	    Comparator<>() {
		public int compare(TemplateProcessor.KeyMap kmap1,
				   TemplateProcessor.KeyMap kmap2)
		{
		    String cname1 = (String)kmap1.get("class");
		    String cname2 = (String)kmap2.get("class");
		    int status = cname1.compareTo(cname2);
		    if (status == 0) {
			String item1 = (String)kmap1.get("item");
			String item2 = (String)kmap2.get("item");
			return item1.compareTo(item2);
		    } else {
			return status;
		    }
		}
	    };


	private TemplateProcessor.KeyMapList
	    keylistFromMethodMap(Map<String,MethodInfo> map,
				 boolean showMethod, boolean hideString)
	{
	    Collection<MethodInfo> collection = map.values();
	    StringBuilder sb = new StringBuilder();
	    TemplateProcessor.KeyMapList list =
		new TemplateProcessor.KeyMapList();
	    for (Map.Entry<String,MethodInfo> entry: map.entrySet()) {
		String method = entry.getKey();
		int index = method.indexOf (':');
		if (index >= 0) {
		    method = method.substring(0, index);
		}
		MethodInfo mi = entry.getValue();
		for (Class<?> clasz: mi.getClasses()) {
		    for (ClassArraySorter.Key key: mi.getSorted(clasz)) {
			if (hideString && clasz.equals(String.class)) continue;
			if (showMethod) {
			    if (blockedMethods != null) {
				Method m = mi.getMethod(clasz, key);
				if (m != null && blockedMethods.contains(m)) {
				    continue;
				}
			    }
			} else {
			    if (blockedConstructors != null) {
				Constructor c = mi.getConstructor(clasz, key);
				if (c != null
				    && blockedConstructors.contains(c)) {
				    continue;
				}
			    }
			}
			TemplateProcessor.KeyMap kmap =
			    new TemplateProcessor.KeyMap();
			sb.setLength(0);
			String classname = clasz.getName().replace('$', '.');
			kmap.put("class", classname);
			if (showMethod) {
			    sb.append(method);
			} else {
			    // sb.append(clasz.getSimpleName());
			    // Constructor case: the name is "<init>"
			    // but we should use character references
			    // because this is going into an HTML page
			    sb.append("&lt;init&gt;");
			}
			sb.append('(');
			boolean first = true;
			for (Class<?> c: key.toArray()) {
			    if (first) {
				first = false;
			    } else {
				sb.append(',');
			    }
			    if (c.equals(Integer.class)) {
				sb.append("int");
			    } else if (c.equals(Long.class)) {
				sb.append("long");
			    } else if (c.equals(Number.class)) {
				sb.append("double");
			    } else if (c.equals(Boolean.class)) {
				sb.append("boolean");
			    } else {
				// sb.append(c.getName().replace('$', '.'));
				sb.append(c.getCanonicalName());
			    }
			}
			sb.append(')');
			String rest = sb.toString();
			if (showMethod) {
			    kmap.put("method", method);
			}
			URL url = findDocURL(clasz, rest);
			if (url != null) {
			    kmap.put("item", rest);
			    kmap.put("href", url.toString());
			    int ind = rest.indexOf('(');
			    if (ind > -1) {
				kmap.put("arguments",
					 rest.substring(ind));
			    }
			    list.add(kmap);
			}
		    }
		}
	    }
	    Collections.sort(list, methodComparator);
	    return list;
	}

	public TemplateProcessor.KeyMapList keylistFromConstructors() {
	    return keylistFromMethodMap(constrMap, false, false);
	}

	public List<String> getConstructors() {
	    return getFromMethodMap(constrMap, false);
	}

	public TemplateProcessor.KeyMapList keylistFromFunctions() {
	    return keylistFromMethodMap(staticMethodMap, true, false);
	}

	public List<String> getFunctions() {
	    return getFromMethodMap(staticMethodMap, true);
	}

	public TemplateProcessor.KeyMapList
	    keylistFromMethods(boolean hideString)
	{
	    return keylistFromMethodMap(methodMap, true, hideString);
	}

	public List<String> getMethods() {
	    return getFromMethodMap(methodMap, true);
	}

    }

    ExpressionProcessor processor;

    /**
     * Constructor.
     * The arguments are classes that will be scanned for
     * public, static methods that return a 'double' value
     * and whose argument have the type
     * <UL>
     *    <LI> int. An argument of this type must be a literal
     *         or an expression guaranteed to return an int.
     *    <LI> long. An argument of this type must be a literal
     *         or an expression guaranteed to return a long.
     *    <LI> boolean. An argument of this type must be a literal
     *         (true or false) or an expression guaranteed to return
     *         a boolean.
     *    <LI> String. An argument of this type must be a literal
     *         or a function guaranteed to return a string.
     *    <LI> double. An argument of this type may be an expression,
     *         a literal, or a function call.
     * </UL>
     * @param classes the classes whose static methods
     *        will be used as functions
     * @exception IllegalAccessException a requested class, method, or
     *            field could not be accessed
     */
    public ExpressionParser(Class<?>... classes)
	    throws IllegalAccessException
    {
	/*
	if (classes.length > 0) {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new ExpressionParserPermission
				   ("org.bzdev.util.ExpressionParser"));
	    }
	}
	*/
	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>()  {
			public Void run() throws IllegalAccessException {
			    processor = new ExpressionProcessor
				(null, classes, null, null);
			    return (Void) null;
			}
		    });
	} catch (PrivilegedActionException ep) {
	    java.lang.Exception e = ep.getException();
	    if (e instanceof IllegalAccessException) {
		throw (IllegalAccessException) e;
	    } else if (e instanceof RuntimeException) {
		throw (RuntimeException) e;
	    }
	}
    }

    /**
     * Constructor for extended expressions.
     * @param returnTypes the types that may be returned by function or
     *        method, or the type of an object whose constructors may be used
     * @param argumentTypes the types that can be used as function or method
     *        arguments
     * @param functionClasses the classes whose static methods will be
     *        used as functions.
     * @param methodClasses the classes whose public instance methods may
     *        be used.
     * @param fieldClasses the classes whose public, static, final fields
     *        may be used.
     * @exception IllegalAccessException a requested class, method, or
     *            field could not be accessed
     */
    public ExpressionParser(Class<?>[] returnTypes,
			    Class<?>[] argumentTypes,
			    Class<?>[] functionClasses,
			    Class<?>[] methodClasses,
			    Class<?>[] fieldClasses)
	    throws IllegalAccessException
    {
	/*
	if ((returnTypes != null && returnTypes.length > 0)
	    || (argumentTypes != null && argumentTypes.length > 0)
	    || (functionClasses != null && functionClasses.length > 0)
	    || (methodClasses != null && methodClasses.length > 0)
	    || (fieldClasses != null && fieldClasses.length > 0)) {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		sm.checkPermission(new ExpressionParserPermission
				   ("org.bzdev.util.ExpressionParser"));
	    }
	}
	*/

	if (returnTypes != null) {
	    for (Class<?> clasz: returnTypes) {
		if (clasz.isPrimitive()) continue;
		allowedValues.add(clasz);
		allowedArgs.add(clasz);
		simpleClassNames.add(clasz.getSimpleName());
	    }
	}
	if (methodClasses != null) {
	    usesMethods = true;
	}
	if (argumentTypes != null) {
	    for (Class<?> clasz: argumentTypes) {
		if (clasz.isPrimitive()) continue;
		allowedArgs.add(clasz);
		simpleClassNames.add(clasz.getSimpleName());
	    }
	}

	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
			public Void run() throws IllegalAccessException {
			    processor = new ExpressionProcessor
				(returnTypes, functionClasses,
				 methodClasses, fieldClasses);
			    return (Void) null;
			}
		    });
	} catch (PrivilegedActionException ep) {
	    java.lang.Exception e = ep.getException();
	    if (e instanceof IllegalAccessException) {
		throw (IllegalAccessException) e;
	    } else if (e instanceof RuntimeException) {
		throw (RuntimeException) e;
	    }
	}
    }

    /**
     * Turn on scripting mode.
     * When this method is called, this expression processor will
     * accept lines that contain multiple expressions, separated by
     * semicolon, and will not require an "=" at the start of a top-level
     * expression. This method should be called before an expression is
     * parsed if it is called at all.
     */
     public void setScriptingMode() throws IllegalStateException {
	 if (Thread.currentThread() == startingThread) {
	     processor.setScriptingMode();
	 } else {
	     String msg = errorMsg("scriptingModeThread");
	     throw new IllegalStateException(msg);
	 }
     }

    /**
     * Turn on import mode.
     * When this method is called, this expression processor will
     * allow new classes to be added. It must be called before a
     * script calls a function or method. It should be called before an
     * expression is parsed if it is called at all.
     */
     public void setImportMode() throws IllegalStateException {
	 if (Thread.currentThread() == startingThread) {
	     processor.setImportMode();
	 } else {
	     String msg = errorMsg("importModeThread");
	     throw new IllegalStateException(msg);
	 }
     }

    boolean scriptImportAllowed = false;

    /**
     * Set Script-Import mode.
     * If script-import mode is turned on, a script can import classes.
     */
    public void setScriptImportMode() {
	if (Thread.currentThread() == startingThread) {
	    scriptImportAllowed = true;
	} else {
	    String msg = errorMsg("scriptImportModeThread");
	    throw new IllegalStateException(msg);
	}
    }

    /**
     * Turn on global mode.
     * When this method is called, this expression processor will
     * allow a variable named global to be accessed.  This variable
     * provides various methods.
     */
    public void setGlobalMode() throws IllegalStateException {
	processor.setGlobalMode();
    }

    /**
     * Add a class to all appropriate tables.
     * This is equivalent to including the class in each of the
     * array arguments for the constructor
     * {@link #ExpressionParser(Class[],Class[],Class[],Class[],Class[])}.
     * Primitive classes are ignored.
     * @param classes the classes to add
     * @exception IllegalAccessException a class or method is not accessible
     */
    public void addClasses(Class<?>... classes) throws IllegalAccessException {
	Thread currentThread = Thread.currentThread();
	Thread lastThread = null;
	try {
	    lastThread = importThreadRef
		.compareAndExchangeAcquire(null, currentThread);
	    threadCount.addAndGet(1);
	    if (lastThread != currentThread && lastThread != null) {
		String msg = errorMsg("addClassThreads");
		throw new IllegalStateException(msg);
	    }
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			    public Void run() throws IllegalAccessException {
				addClasses(processor, classes);
				return (Void) null;
			    }
			});
	    } catch (PrivilegedActionException e) {
		java.lang.Exception ee = e.getException();
		if (ee instanceof IllegalAccessException) {
		    throw (IllegalAccessException) ee;
		}
	    }
	    // addClasses(processor, classes);
	} finally {
	    if (threadCount.addAndGet(-1) == 0) {
		importThreadRef
		    .compareAndExchangeRelease(currentThread, null);
	    } else {
		synchronized (ExpressionParser.this) {
		    if (noImport) {
			// no longer need it.
			importThreadRef.setRelease(null);
		    }
		}
	    }
	}
    }

    private synchronized void addClasses(ExpressionProcessor processor,
				       Class<?>... classes)
	    throws IllegalAccessException
    {
	if (noImport || importsFrozen) {
	    throw new IllegalStateException(errorMsg("noImport"));
	}
	clearFCN();
	for (Class<?> clasz: classes) {
	    if (clasz.isPrimitive()) continue;
	    if (scriptImportAllowed) {
		// add this class, superclasses, interfaces, classes
		// for the values returned by methods, and the classes
		// used as method parameters.
		addClassesFrom(clasz);
	    } else {
		allowedValues.add(clasz);
		allowedArgs.add(clasz);
		simpleClassNames.add(clasz.getSimpleName());
	    }
	}
	usesMethods = true;
	for (Class<?> clasz: classes) {
	    if (clasz.isPrimitive()) continue;
	    if (!clasz.isInterface()) {
		processor.addConstructors(clasz);
		processor.addMethods(clasz, true);
	    }
	    processor.addMethods(clasz, false);
	    processor.addFields(clasz);
	}
    }

    private void addClassesFrom(Class<?> clasz) throws IllegalAccessException {
	if (clasz == null) return;
	if (clasz.isPrimitive()) return;
	if (allowedValues.contains(clasz)) return;
	if (allowedArgs.contains(clasz)) return;
	if (!Modifier.isPublic(clasz.getModifiers())) return;
	addClassesFrom(clasz.getSuperclass());
	for (Class<?> c: clasz.getInterfaces()) {
	    addClassesFrom(c);
	}
	if (clasz.isArray()) {
	    allowedValues.add(clasz);
	    allowedArgs.add(clasz);
	    addClassesFrom(clasz.getComponentType());
	    return;
	}
	allowedValues.add(clasz);
	allowedArgs.add(clasz);
	if (clasz.isEnum()) {
	    processor.enumSet.add(clasz);
	    processor.addEnumConstants(clasz);
	}
	simpleClassNames.add(clasz.getSimpleName());
	for (Method method: clasz.getMethods()) {
	    addClassesFrom(method.getReturnType());
	    for (Class<?> c: method.getParameterTypes()) {
		addClassesFrom(c);
	    }
	}
    }

    void importClasses(String pkg, Object classNames)
	throws IllegalArgumentException, IllegalAccessException
    {
	if (pkg != null) {
	    pkg = pkg.trim();
	    if (pkg.equals("null")) pkg = null;
	}
	if (classNames instanceof String) {
	    importClassesAux(pkg, (String)classNames);
	} else if (classNames instanceof ESPArray) {
	    for (Object obj: (ESPArray)classNames) {
		if (obj instanceof String) {
		    String cn = (String)obj;
		    importClassesAux(pkg, cn);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("importSyntaxL"));
		}
	    }
	} else {
		throw new IllegalArgumentException(errorMsg("importSyntax2"));
	}
    }


    ArrayList<Class<?>> importList = null;

    private void importClassesAux(String pkg, String className)
	throws IllegalArgumentException, IllegalAccessException
    {
	if (className == null) {
	    throw new IllegalArgumentException(errorMsg("noClassName1"));
	}
	String cn;
	if (pkg == null) {
	    cn = className;
	    /*
	    cn  = (className.indexOf('.') != -1)?
		className.replace('.', '$'): className;
	    */
	} else {
	    String pkg1 = (pkg == null || pkg.length() == 0)? "": pkg + ".";
	    cn = (className.indexOf('.') != -1)?
		pkg1 + className.replace('.', '$'): pkg1 + className;
	}
	final String cname = cn;
	try {
	    Class<?> clasz = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Class<?>>() {
			public Class<?> run()
			    throws ClassNotFoundException
			{
			    if (pkg == null) {
				ClassLoader cl = ClassLoader
				    .getSystemClassLoader();
				String name = cname;
				int len = name.length();
				for (;;) {
				    try {
					return cl.loadClass(name);
				    } catch (ClassNotFoundException e) {
					int ind = name.lastIndexOf('.');
					if (ind == -1) {
					    throw e;
					} else {
					    String s1 = name.substring(0, ind);
					    String s2 = name.substring(ind+1,
								       len);
					    name = s1  + "$" + s2;
					}
				    }
				}
			    } else {
				return ClassLoader.getSystemClassLoader()
				    .loadClass(cname);
			    }
			}});
	    if (importList == null) importList = new ArrayList<Class<?>>();
	    importList.add(clasz);
	    // addClasses(processor, clasz);
	} catch (PrivilegedActionException ep) {
	    java.lang.Exception e = ep.getException();
	    String msg = errorMsg("invalidPkgOrClassName", pkg, cn);
	    throw new IllegalArgumentException(msg, e);
	}
    }

    void finishImport() throws IllegalAccessException {
	Class<?>[] classes = new Class<?>[importList.size()];
	importList.toArray(classes);
	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
			public Void run() throws IllegalAccessException {
			    addClasses(processor, classes);
			    return (Void) null;
			}
		    });
	} catch (PrivilegedActionException e) {
	    java.lang.Exception ee = e.getException();
	    if (ee instanceof IllegalAccessException) {
		throw (IllegalAccessException) ee;
	    }
	}
    }


    /**
     * Find the method implementing a function.
     * The argument is the name of a static method with one
     * double-precision argument that returns a double-precision value,
     * and must include enough of the class name to be unique given the
     * classes used to define functions.
     * @param fname the name of the function
     * @return the method
     * @exception IllegalStateException classes are missing or fname does
     *            not name a method that is recognized
     * @exception NoSuchMethodException a method does not exist
     * @exception IllegalArgumentException fname is not syntactically
     *            the name of a method
     */
    public Method findRVFMethod (String fname)
	throws IllegalStateException, NoSuchMethodException,
	       IllegalArgumentException
    {
	return processor.findRVFMethod(fname);
    }

    /**
     * Add a named function, specifying a file name for its input file.
     * @param name the name of the function
     * @param args the formal parameters for the function
     * @param expressions the expressions the represent the body of
     *        a function
     * @param filename the name of the file containing the function's
     *        expressions
     * @param synchronizedFunction true if this function is synchronized
     *        on this expression parser, false otherwise
     */
    public void addFunction(String name, String[] args,
			    String expressions, String filename,
			    boolean synchronizedFunction)
    {
	String prevFileName = filenameTL.get();
	try {
	    filenameTL.set(filename);
	    addFunction(name, args, expressions, synchronizedFunction);
	} finally {
	    filenameTL.set(prevFileName);
	}
    }

    /**
     * Add a named function.
     * @param name the name of the function
     * @param args the formal parameters for the function
     * @param expressions the expressions the represent the body of
     *        a function
     * @param synchronizedFunction true if this function is synchronized
     *        on this expression parser, false otherwise
     */
    public void addFunction(String name, String[] args,
			    String expressions, boolean synchronizedFunction)
    {
	ArrayList<LinkedList<Token>> tokenQueues = new ArrayList<>();
	    tokenize(expressions, 0, tokenQueues);
	    addFunction(name, args, expressions, tokenQueues,
			synchronizedFunction);
    }


    /**
     * Get the fully qualified names for  enumeration constants and fields
     * provided by this expression parser.
     * <P>
     * This method is provided for applications that can list the available
     * constants as part of a 'help' or documentation subsystem.
     * @return the constants
     */
    public List<String> getConstants() {
	return processor.getConstants();
    }

    /**
     * Get the classes for objects that can be returned by this parser.
     * The values returned do not include primitive types.
     * <P>
     * This method is provided for applications that can list the
     * types of objects that can be returned as part of a 'help' or
     * documentation subsystem.
     * @return the classes
     */
    public ArrayList<Class<?>> getReturnClasses() {
	return processor.getArgumentClasses();
    }

    /**
     * Get the classes for objects that can be uses as arguments for
     * functions or methods provided by this expression parser.
     * The values returned do not include primitive types.
     * <P>
     * This method is provided for applications that can list the
     * types of objects that can be used as arguments as part of a
     * 'help' or documentation subsystem.
     * @return the classes
     */
    public ArrayList<Class<?>> getArgumentClasses() {
	return processor.getArgumentClasses();
    }

    /**
     * Get the constructors supported by this expression parser.
     * Each element in the returned list consists of a fully qualified
     * class name followed by an open parenthesis, a comma-separated list
     * of the class names for the arguments, and a closing parenthesis.
     * <P>
     * This method is provided for applications that can list the
     * constructors and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the constructors
     */
    public List<String> getConstructors() {
	return processor.getConstructors();
    }

    /**
     * Get the functions supported by this expression parser.
     * Each element in the returned list consists of a fully qualified
     * class name followed the function name that is in turn followed
     * by an open parenthesis, a comma-separated list of the class
     * names for the arguments, and a closing parenthesis.
     * <P>
     * This method is provided for applications that can list
     * functions and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the functions
     */
    public List<String> getFunctions() {
	return processor.getFunctions();
    }

    /**
     * Get the methods  supported by this expression parser.
     * Each element in the returned list consists of a fully qualified
     * class name followed by a colon, a space, and the method name.
     * The method  name is in turn followed by an open parenthesis, a
     * comma-separated list of the class names for the arguments, and
     * a closing parenthesis.
     * <P>
     * This method is provided for applications that can list
     * methods and their arguments as part of a 'help' or
     * documentation subsystem.
     * @return a list of the methods
     */
    public List<String> getMethods() {
	return processor.getMethods();
    }

    /**
     * Get a key list for generating a table of constants.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified name of a constant
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForConstants() {
	return processor.keylistForConstants();
    }

    /**
     * Get a key list for generating a table of classes, instances of
     * which can be returned by an expression.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified class name of the class
     *         an expression can return
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForReturnClasses() {
	return processor.keylistForReturnClasses();
    }

    /**
     * Get a key list for generating a table of .
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>item</B> - the fully qualified class name of a class for
     *        an argument to a constructor, function or method recognized
     *        by this expression parser
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForArgumentClasses() {
	return processor.keylistForArgumentClasses();
    }

    /**
     * Get a key list for generating a table of constructors.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the object
     *        a constructor will create.
     *   <LI> <B>item</B> - the simple class name followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        constructor's arguments, followed by a closing parenthesis.
     *   <LI> <B>arguments</B> - an opening parenthesis, followed by a
     *        comma-separated list of argument class names, followed by
     *        a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForConstructors() {
	return processor.keylistFromConstructors();
    }

    /**
     * Get a key list for generating a table of functions.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for a class,
     *        one of whose static methods implements the function.
     *   <LI> <B>item</B> - the name of the static method representing
     *        the function followed by an open parenthesis, a
     *        comma-separated list of the types of the function's
     *        arguments, followed by a closing parenthesis.
     *   <LI> <B>method</B> - the simple name of the function.
     *   <LI> <B>arguments</B> - an opening parenthesis, followed by a
     *        comma-separated list of argument class names, followed by
     *        a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForFunctions() {
	return processor.keylistFromFunctions();
    }

    /**
     * Get a key list for generating a table of instance methods,
     * excluding methods for {@link String}.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the
     *         class declaring a method.
     *   <LI> <B>item</B> - the name of the method followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        method's arguments, followed by a closing parenthesis.
     *   <LI> <B>method</B> - the simple name of the function.
     *   <LI> <B>arguments</B> - an opening parenthesis, followed by a
     *        comma-separated list of argument class names, followed by
     *        a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForMethods() {
	return processor.keylistFromMethods(true);
    }

    /**
     * Get a key list for generating a table of instance methods.
     * The key list will contain a sequence of key maps with the
     * following keys:
     * <UL>
     *   <LI> <B>href</B> - The URL provided by the HREF attribute of an
     *         an &lt;A&gt;element, not including the delimiting quotes.
     *   <LI> <B>class</B> - the fully qualified class name for the
     *         class declaring a method.
     *   <LI> <B>method</B> - the simple name of the function.
     *   <LI> <B>item</B> - the name of the method followed by an open
     *        parenthesis, a comma-separated list of the types of the
     *        method's arguments, followed by a closing parenthesis.
     *   <LI> <B>arguments</B> - an opening parenthesis, followed by a
     *        comma-separated list of argument class names, followed by
     *        a closing parenthesis.
     * </UL>
     * This method is provided for generating HTML pages that can point
     * to API documentation.  The method {@link #createAPIMap(List)}
     * must be called before this method is used.
     * @param hideString true if methods of {@link String} should not be
     *        shown; false otherwise
     * @return the key list
     */
    public TemplateProcessor.KeyMapList keylistForMethods(boolean hideString) {
	return processor.keylistFromMethods(hideString);
    }

    static Pattern MATCHING_PATTERN = Pattern.compile
	("[\\p{Space}]*(=|(var\\p{Space}+[_$\\p{IsAlphabetic}]"
	 + "[_$\\p{IsAlphabetic}\\p{Digit}]*[\\p{Space}]*)"
	 + "|(function\\p{Space}+)|"
	 + "(synchronized\\p{Space}+function\\p{Space}+))");

    @Override
    public boolean appliesTo(String string) {
	if  (string == null) return false;
	return MATCHING_PATTERN.matcher(string).lookingAt();
    }

    private class Token {
	Operator type;
	String name;
	int index; // index into the string being parsed
	int level;
	int argcount = 0;
	Token condPeer = null;
	Token bracePeer = null;
	Token bracketPeer = null;
	int qmarkIndex = -1;	// an index for COLON, max for 1st QMARK
	int getQmarkIndex() {return qmarkIndex;}
	void setQmarkIndex(int value) {qmarkIndex = value;}
	Operator getType() {return type;}
	String getName() {return name;}
	int getLevel() {return level;}
	int getIndex() {return index;}
	String getFileName() {return filename;}

	String filename;

	void changeType(Operator type) {
	    this.type = type;
	}

	boolean forMethod = false;
	void setForMethod() {forMethod = true;}
	boolean forMethod() {return forMethod;}

	void modLevel(int incr) {
	    level += incr;
	}

	void setCondPeer(Token condPeer) {
	    this.condPeer = condPeer;
	}
	Token getCondPeer() {return condPeer;}

	void setBracePeer(Token bracePeer) {
	    this.bracePeer = bracePeer;
	}
	Token getBracePeer() {return bracePeer;}

	void setBracketPeer(Token bracketPeer) {
	    this.bracketPeer = bracketPeer;
	}
	Token getBracketPeer() {return bracketPeer;}

	Token funct = null;

	void setFunct(Token f) {
	    funct = f;
	}
	Token getFunct() {return funct;}


	Object value;
	void setValue(Object value) {
	    this.value = value;
	}
	Object getValue() {return value;}

	ThreadLocal<Object> valueTL = new ThreadLocal<>();

	void setValueTL(Object value) {
	    // this.value = value;
	    this.valueTL.set(value);
	}

	void setName(String name) {
	    this.name = name;
	}

	Object getValueTL() {return valueTL.get();}

	void incrArgCount() {
	    argcount++;
	}

	int getArgCount() {return argcount;}
	void setArgCount(int count) {
	    argcount = count;
	}

	boolean willSync = false;
	boolean willSync() {return willSync;}
	void sync() {willSync = true;}

	Map<String,Object> map;

	boolean containsKey(String name) {
	    if (!processor.hasAmap()) {
		return map.containsKey(name);
	    } else {
		if (processor.amapContains(name)) {
		    return true;
		} else {
		    return map.containsKey(name);
		}
	    }
	}

	Object get(String name) {
	    // return getFromMap(map, name);
	    if (!processor.hasAmap()) {
		return map.get(name);
	    } else {
		if (processor.amapContains(name)) {
		    return processor.getFromAmap(name);
		} else {
		    return map.get(name);
		}
	    }
	}

	Object get() {
	    return get((String)value);
	}

	void putInMap(String name, Object object) {
	    map.put(name, object);
	}

	boolean containsKeyInMap(String name) {
	    return map.containsKey(name);
	}

	// used by var.name
	boolean variableExists() {
	    //	    return map.containsKey(name);
	    if (!processor.hasAmap()) {
		return map.containsKey(name);
	    } else {
		if (processor.amapContains(name)) {
		    return true;
		} else {
		    return map.containsKey(name);
		}
	    }
	}

	// used with QVAR
	boolean variableExistsQ() {
	    if (!processor.hasAmap()) {
		return map.containsKey((String)value);
	    } else {
		if (processor.amapContains((String)value)) {
		    return true;
		} else {
		    return map.containsKey((String)value);
		}
	    }
	}

	// used with QQVAR
	boolean variableExistsQQ() {
	    if (!processor.hasAmap()) {
		boolean result = map.containsKey((String)value);
		if (result) {
		    result = (map.get((String)value) != null);
		}
		return result;
	    } else {
		if (processor.amapContains((String)value)) {
		    return (processor.getFromAmap((String)value) != null);
		} else {
		    boolean result =  map.containsKey((String)value);
		    if (result) {
			result = (map.get((String)value) != null);
		    }
		    return result;
		}
	    }
	}

	boolean incrementedLevel = false;
	boolean incrementedLevel() {return incrementedLevel;}
	void  incrementingLevel() {incrementedLevel = true;}


	boolean backquoted = false;
	void setBackquoted() {backquoted = true;}
	boolean backquoted() {return backquoted;}

	Token(Operator type, String s, int index, int level) {
	    this.type = type;
	    name = s;
	    this.index = index;
	    this.level = level;
	    map = vmap.get();
	    filename = filenameTL.get();
	}
    }

    private Map<String,Object> cmap =
	Collections.synchronizedMap(new TreeMap<String,Object>());

    // We can override the binding temporarily, but otherwise we
    // just use the default provided by vmapBinding.
    private ThreadLocal<Map<String,Object>> vmap =
	new ThreadLocal<>() {
	    public Map<String,Object> get() {
		Map<String,Object> map = super.get();
		if (map == null) {
		    return vmapBinding.get();
		} else {
		    return map;
		}
	    }

	    @Override protected Map<String,Object> initialValue() {
		return null;
	    }
	};

    private class VMapBinding {
	Map<String,Object> map =
	    Collections.synchronizedMap(new HashMap<String,Object>());
	public Map<String,Object> get() {return map;}

	public void set (Map<String,Object> map) {
	    this.map = map;
	}
    }

    VMapBinding vmapBinding = new VMapBinding();


    /*
    private Map<String,Object> vmap =
	Collections.synchronizedMap(new HashMap<String,Object>());
    */

    private ThreadLocal<Set<String>> vsetThreadLocal = new ThreadLocal<>() {
	    @Override protected Set<String> initialValue() {
		return new HashSet<String>();
	    }
	};
    /*
    private Set<String> vset =
	Collections.synchronizedSet(new HashSet<String>());
    */

    // Function names.  These must be defined at the top level of
    // scripts and a name cannot be overridden once defined.
    private ThreadLocal<Set<String>> functNamesThreadLocal =
	new ThreadLocal<>() {
	    @Override protected Set<String> initialValue() {
		return new HashSet<String>();
	    }
	};


    private void addFunction(String name,
			     String[] args,
			     String orig,
			     ArrayList<LinkedList<Token>> tokenQueues,
			     boolean synchronizedFunction)
	throws ObjectParser.Exception
    {
	ESPFunction f = new ESPFunction(args, orig, tokenQueues);
	int sz = tokenQueues.size();
	if (sz > 0) {
	    LinkedList<Token> lastList = tokenQueues.get(sz-1);
	    Token last = lastList.getLast();
	    if (last.getType() == Operator.VOID) {
		f.setAsVoid();
	    }
	}
	if (synchronizedFunction) {
	    f.willSync();
	}
	vmap.get().put(name, f);
    }

    /**
     * Get the value of a variable or function.
     * @param name the name of the variable or function
     * @return the value of the variable or the lambda expression
     *        (an instance of {@link ESPFunction}) implementing the
     *        function
     */
    public Object get(String name) {
	Object result = getInternal(name);
	return (result instanceof TypedNull)? null: result;
    }

    // Internally, we want to keep instances of TypedNull
    Object getInternal(String name) {
	if (!processor.hasAmap()) {
	    return vmap.get().get(name);
	} else {
	    if (processor.amapContains(name)) {
		return processor.getFromAmap(name);
	    } else {
		return vmap.get().get(name);
	    }
	}
    }

    // used by Token so we can change bindings. In a function or method,
    // the lookup for a variable's value occurs after the function or method
    // was defined.
    /*
    Object getFromMap(Map<String,Object> map, String name) {
	if (!processor.hasAmap()) {
	    return map.get(name);
	} else {
	    if (processor.amapContains(name)) {
		return processor.getFromAmap(name);
	    } else {
		return map.get(name);
	    }
	}
    }
    */

    /**
     * Determine if a variable or function exists.
     * @param name the name of the variable
     * @return true if the variable exists; false otherwise
     */
    public boolean exists(String name) {
	return vmap.get().containsKey(name)
		|| functNamesThreadLocal.get().contains(name);
    }

    /**
     * Set the value of a variable.
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void set(String name, Object value) {
	 vmap.get().put(name, value);
    }

    /**
     * Remove a variable.
     * @param name the name of the variable
     * @return the value of the variable being removed
     */
    public Object remove(String name) {
	return vmap.get().remove(name);
    }

    /**
     * Get the variables for this expression parser.
     * @return a set containing the names of the variables
     */
    public Set<String> variables() {
	return vmap.get().keySet();
    }

    /**
     * Remove all variables from this expression parser and
     * reset it.
     */
    public void clear() {
	processor.clear();
	vmap.get().clear();
    }

    private synchronized String findField(String name) {
	return processor.findField(name);
    }

    private static final Pattern STRING_PATTERN
	= Pattern.compile("\"((\\\\.|[^\"\\\\])*)\"");
    
    private static final Pattern
	INTEGER_PATTERN = Pattern.compile("[-+]?[0-9]+");

    private static final Pattern DOUBLE_PATTERN
	= Pattern.compile("[-+]?([0-9]+([.][0-9]*)?|[.][0-9]+)([eE]-?[0-9]+)?");

    /*
    public void testit(String s) throws IllegalArgumentException {
	LinkedList<Token> tokens = tokenize(s, 0);
	Token t = null;
	while ((t = tokens.poll()) != null) {
	    if (t.getType() == Operator.CPAREN && t.getFunct() != null) {
		System.out.println (t.getType() + ": "
				    + t.name + ", " + t.level
				    + "; funct = " + t.getFunct().getName());
	    } else if (t.getType() == Operator.NUMBER) {
		System.out.println (t.getType() + ": "
				    + t.name + ", " + t.level
				    + "; value = " + t.getValue()
				    + ", type = " + t.getValue().getClass());
		
	    } else {
		System.out.println (t.getType() + ": "
				    + t.name + ", " + t.level);
	    }
	}
    }
    */


    private int nextIdentIndex(String s, int i, int len) {
	int start = i;
	char ch = s.charAt(i);
	char sch = ch;
	i++;
	if (i >= len) return len;
	i = skipWhitespace(s, i, len, false);
	if (i ==len) return (start+1);
	ch = s.charAt(i);

	if (sch == '.') {
	    if (ch == '.') return (start + 1);
	    if (Character.isJavaIdentifierStart(ch)) {
		return i;
	    }
	} else {
	    if (ch == '.') return i;
	    if (ch == ':' && i < (len-1) && s.charAt(i+1) == ':') {
		return i;
	    }
	}
	return (start+1);
    }

    // Assumption: there will never be two unary operators in a few
    // (so if we see that, we'll fix it up).  The reason is that operations
    // on the stack are evaluated when the next operation arrives, as long
    // as those have an equal or higher level. Two unary operators in a
    // row would prevent that from working properly.
    //
    // * Implied level offest of 0 for mult/div/mod, with level decremented
    //   afterwards.
    // * Implied level offset of -1 for addition, with level decremented
    //   afterwards.
    // * Implied level offset of +1 for a variable, with level incremented
    //   afterwards.

    private static final int LEVEL_OFFSET = 22+2;
    private static final int SHIFT_OFFSET = -2;
    private static final int BITWISE_OFFSET = -4;
    private static final int RELEQ_OFFSET = -6;
    private static final int EQ_OFFSET = -8;
    private static final int LOGICAL_OFFSET = -8; // from BITWISE
    private static final int COND_OFFSET = -14;	  // conditional: (?:)
    private static final int VAR_OFFSET = -15;	  // var statement
    private static final int DOUBLE_COLON_OFFSET = -19;
    private static final int SEMICOLON_OFFSET = -20;
    private static final int ASSIGN_OFFSET = -10; // to be added to EQ_OFFSET

    private boolean isParam(String name, HashSet<String>params,
			    Stack<HashSet<String>> paramsStack)
    {
	if (params == null) return false;
	if (params.contains(name)) return true;
	return false;
    }


    static final String LINE_SEP = System.getProperty("line.separator");

    static final char[] linesep = LINE_SEP.toCharArray();
    static final char SEP_END = (linesep.length == 0)? '\n':
	linesep[linesep.length-1];

    static final String COMMENT_RE =
	"(//.*" + SEP_END + "|/[*][^*]*[*]/)";

    // Used only by skipWhitespace in order to skip over comments.
    // While we just check the last character of the line separator,
    // this works in practice: currently line separator sequences are
    // \r\n or \n.  The original MacOS system used \r.  Testing \n works
    // in the last case.  Testing \r will end a line ending with \r\n at
    // the \n, but that is a whitespace character and will be skipped anyway.
    // It is not likely that a different sequence will be introduced given
    // the use of \r\n as an EOL sequence in various Internet formats, and
    // any new sequence would most likely use some existing whitespace
    // characters in any case.
    private static int skipOverEOL(String s, int i) {
	int len = s.length();
	if (i == len) return i;
	char ch = s.charAt(i);
	while (ch != SEP_END) {
	    i++;
	    if (i == len) return i;
	    ch = s.charAt(i);
	}
	i++;
	return i;
    }
    private static int skipToStarSlash(String s, int i)
	throws ObjectParser.Exception
    {
	int len = s.length();
	if (i == len) {
	    String msg = errorMsg("noCommentEnd");
	    throw new ObjectParser.Exception(msg, s,
					     filenameTL.get(),
					     len-1);
	}
	char ch = s.charAt(i);
	for (;;) {
	    if (ch == '*') {
		i++;
		if (i == len) {
		    String msg = errorMsg("noCommentEnd");
		    throw new ObjectParser.Exception(msg, filenameTL.get(), s,
						     len-1);
		}
		ch = s.charAt(i);
		if (ch == '/') {
		    return i+1;
		}
	    }
	    i++;
	    if (i == len) {
		String msg = errorMsg("noCommentEnd");
		throw new ObjectParser.Exception(msg, filenameTL.get(),
						 s, len-1);
	    }
	    ch = s.charAt(i);
	}
    }


    // returns the new index.
    private static int skipWhitespace(String s, int i, boolean noterm)
	throws ObjectParser.Exception
    {
	return skipWhitespace(s, i, s.length(), noterm);
    }

    private static int skipWhitespace(String s, int i, int len, boolean noterm)
	throws ObjectParser.Exception
    {
	if (i == len) return i;
	char ch = s.charAt(i);
	while (Character.isWhitespace(ch) || ch == '/') {
	    if (ch == '/') {
		i++;
		if (i < len && s.charAt(i) == '/') {
		    i++;
		    i = skipOverEOL(s, i);
		} else if (i < len && s.charAt(i) == '*') {
		    i++;
		    i = skipToStarSlash(s, i);
		} else {
		    i--;
		    return i;
		}
	    } else {
		i++;
	    }
	    if (i == len) {
		if (noterm) {
		    //input terminated unexpectedly
		    String msg = errorMsg("unterminatedVar");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		} else {
		    return i;
		}
	    }
	    ch = s.charAt(i);
	}
	return i;
    }

    private static boolean maybeCall(String s, int index, int len) {
	int i = skipWhitespace(s, index, len, false);
	return (i < len)? s.charAt(i) == '(': false;
    }

    private static boolean searchVsets(Stack<Set<String>> stack
				       , String key) {
	for (Set<String> set: stack) {
	    if (set.contains(key)) return true;
	}
	return false;
    }

    // for debugging
    private static void printVsets(Stack<Set<String>> stack) {
	int i = 0;
	System.out.println("... vsetStack:");
	for (Set<String> set: stack) {
	    System.out.println("        stack " + i + ":");
	    for (String name: set) {
		System.out.println("            " + name);
	    }
	    i++;
	}
    }


    private static class QMarkPair {
	Token firstQMark;
	int qmarkIndex;
	QMarkPair(Token token, int ind) {
	    firstQMark = token;
	    qmarkIndex = ind;
	}
    }

    // Provided because the String substring(int) method
    // seems to make a full copy from the index to the end of the string
    // instead of sharing a buffer (which is immutable).
    private static String getLineTail(String s, int i) {
	// Get the tail up to an EOL.

	int len = s.length();
	if (i >= len) return "";
        char ch = s.charAt(i);
	if (ch == '\r' || ch == '\n') return "";
	int end = i + 1;
	while (end < len) {
	    ch = s.charAt(end);
	    if (ch == '\r' || ch == '\n') break;
	    end++;
	}
	return s.substring(i, end);
    }


    private LinkedList<Token>
	tokenize(String s, int offset, ArrayList<LinkedList<Token>> tarray)
	throws ObjectParser.Exception
    {
	int len = s.length();
	LinkedList<Token> tokens = new LinkedList<Token>();
	Stack<Token> parenPeers = new Stack<>();
	Token parenPeer = null;
	Stack<Token> bracePeers = new Stack<>();
	Token bracePeer = null;
	Stack<Token> condPeers = new Stack<>();
	Token condPeer = null;
	Stack<Token> bracketPeers = new Stack<>();
	Token bracketPeer = null;
	Stack<HashSet<String>> paramsStack = new Stack<>();
	HashSet<String>params = null;
	Stack<Token> varPeers = new Stack<>();
	Token varPeer = null;

	Stack<String>objectProps = new Stack<>();
	boolean sawObjectProp = false;
	int level = 0;
	int oldlevel; 		// tmp variable
	Token prev = null;
	Operator prevType = null;
	Token next = null;
	Token funct = null;
	boolean semicolonSeen = false;
	boolean functkwSeen = false;
	boolean functParams = false;
	boolean methodStartSeen = false;
	String functName = null;
	boolean expectingOBrace = false;
	Token epObjectStart = null;
	boolean epObjectStarted = false;
	boolean colonExpected = false;
	Token peqToken = null;
	boolean importStarted = false;
	boolean finishNeeded = false;

	Stack<QMarkPair> firstQMarkStack = new Stack<>();
	Token firstQMark = null;
	int qmarkIndex = 0;
	boolean mustSync = false;

	Set<String> vset = vsetThreadLocal.get();
	Set<String> functNames = functNamesThreadLocal.get();

	// vset.clear();  clear when parse is called
	Stack<Set<String>> vsetStack = new Stack<>();
	vsetStack.push(vset);

	Token nonNullPrev = null;
	Token ltPrevToken = null;

	// Used to determine if we should internally add a
	// missing parentheses for import statements.
	boolean importParen = false;
	boolean needCloseParen = false;

	for (int i = 0; i < len; i++) {
	    char ch = s.charAt(i);
	    if (colonExpected) {
		i = skipWhitespace(s, i, len, false);
		if (i == len) {
		    i--;	// for i++ in 'for' loop
		    continue;
		}
		ch = s.charAt(i);
		if (ch != ':' && ch != '(') {
		    String chstr = s.substring(i,i+1);
		    String msg = errorMsg("colonExpected", chstr);
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
	    } else if (epObjectStarted) {
		i = skipWhitespace(s, i, len, false);
		if (i == len) {
		    i--;	// for i++ in 'for' loop
		    continue;
		}
		ch = s.charAt(i);
		if (ch == '}' && (prev == null
				  || prev.getType() == Operator.OBJOPENBRACE)) {
		    // special case - we have an empty object. When the
		    // open brace is processed, a JSObject is pushed onto
		    // the value stack, but if the object definition is
		    // empty, there is nothing further to do.
		    prev = new Token(Operator.OBJCLOSEBRACE, "}", offset+i,
				     level);
		    level -= (1 + LEVEL_OFFSET);
		    parenPeer = (parenPeers.size() == 0)? null:
			parenPeers.pop();
		    bracePeer = (bracePeers.size() == 0)? null:
			bracePeers.pop();
		    continue;
		}
		if (ch != '"' && !Character.isJavaIdentifierStart(ch)) {
		    String badname =s.substring(i,i+1);
		    String msg = errorMsg("notPropName", badname);
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
	    }
	    Operator ptype = (prev == null)? null: prev.getType();
	    if (ch == '=') {
		if (ptype == Operator.PEQ) {
		    prev.changeType(Operator.EQ);
		    prev.setName("==");
		    continue;
		} if (ptype == Operator.LNOT) {
		    prev.changeType(Operator.NE);
		    prev.setName("!=");
		    prev.modLevel(-3 + EQ_OFFSET);
		    continue;
		} else if (ptype == Operator.GT) {
		    prev.changeType(Operator.GE);
		    prev.setName(">=");
		    continue;
		} else if (ptype == Operator.LT) {
		    prev.changeType(Operator.LE);
		    prev.setName("<=");
		    continue;
		}
	    } else if (ch == '&') {
		if (ptype == Operator.AND) {
		    prev.changeType(Operator.LAND);
		    prev.setName("&&");
		    prev.modLevel(LOGICAL_OFFSET);
		    continue;
		}
	    } else if (ch == '|') {
		if (ptype == Operator.OR) {
		    prev.changeType(Operator.LOR);
		    prev.setName("||");
		    prev.modLevel(LOGICAL_OFFSET);
		    continue;
		}
	    } else if (ch == '>') {
		if (ptype == Operator.LE) {
		    if (ltPrevToken != null) {
			ltPrevToken.changeType(Operator.VARIABLE_NAME);
			prev.changeType(Operator.SWAP);
			prev.setName("<=>");
			ltPrevToken = null;
			continue;
		    } else {
			// <=> must have a variable as its previous token
			String msg = errorMsg("varExpectedBefore");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		}
	    } else {
		int iprev = i;
		i = skipWhitespace(s, i, len, false);
		if (i == len) {
		    i--;	// for i++ in 'for' loop
		    continue;
		}
		if (i != iprev) {
		    ch = s.charAt(i);
		}
	    }
	    if (ptype == Operator.OBRACKET && ch == ']') {
		// special case - an empty list. There is nothing to do:
		// we don't add the closing bracket to the queue because
		// there is no element to add to the list, but we do set
		// the previous token for syntax-checking reasons.
		prev = new Token(Operator.OBRACKET, "]", offset+i, level);
		level -= (1 + LEVEL_OFFSET);
		parenPeer = (parenPeers.size() == 0)? null: parenPeers.pop();
		bracketPeer = (bracketPeers.size() == 0)? null:
		    bracketPeers.pop();
		ltPrevToken = null;
		continue;
	    }
	    if (ptype == Operator.PEQ) {
		if (processor.scriptingMode && peqToken != null) {
		    Operator peqTokenType = peqToken.getType();
		    if (peqTokenType == Operator.VARIABLE) {
			// VARIABLE = EXPRESSION CASE.
			prev.changeType(Operator.ASSIGN1);
			peqToken.changeType(Operator.VARIABLE_NAME);
		    } else if (peqTokenType == Operator.CPAREN
			  && peqToken.getName().equals("]")) {
			prev.changeType(Operator.ASSIGN2);
			peqToken.changeType(Operator.LEAVE_ON_STACK);
		    } else {
			// Saw an '=' that is not part of a multi-character
			// operator and that is not part of a legal assignment
			// statement.
			String msg = errorMsg("misplacedEq");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    prev.modLevel(ASSIGN_OFFSET);
		    ptype = prev.getType();
		    peqToken = null;
		} else {
		    // Saw an '=' that is not part of a multi-character
		    // operator and that is not part of a legal assignment
		    // statement: there was no previous token.
		    String msg = errorMsg("misplacedEq");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
	    }
	    if (processor.scriptingMode && ptype == Operator.CBRACE
		&& parenPeer == null && ch != ';' && ch != '(') {
		// make it look like we have a semicolon after a '}' that
		// ends a top-level function or lambda expression
		i--;
		ch = ';';
	    }
	    if (ch != '(' && (ptype == Operator.METHOD
			      || ptype == Operator.CONSTRUCTOR
			      || ptype == Operator.FUNCTION)) {
		String msg = errorMsg("notCalled", prev.getName());
		throw new ObjectParser.Exception(msg, filenameTL.get(),
						 s, i);
	    }
	    if (semicolonSeen) {
		String msg = errorMsg("semicolon");
		throw new ObjectParser.Exception(msg, filenameTL.get(), s, i);
	    }
	    ltPrevToken = null;

	    if (importParen) {
		i = skipWhitespace(s, i, len, false);
		if (i == len) {
		    i--;	// for i++ in 'for' loop
		    continue;
		}
		ch = s.charAt(i);
		if (ch == '"' || Character.isJavaIdentifierStart(ch)) {
		    needCloseParen = true;
		    ch = '(';
		    i--;
		}
		importParen = false;
	    } else if (needCloseParen) {
		// the 'else' is needed so we don't run this
		// when we just set ch to '(' and decremented i.
		i = skipWhitespace(s, i, len, false);
		if (i == len) {
		    i--;	// for i++ in 'for' loop
		    continue;
		}
		ch = s.charAt(i);
		if (ch == ';') {
		    needCloseParen = false;
		    i--;
		    ch = ')';
		}
	    }

	    // Operator ptype = (prev == null)? null: prev.getType();
	    switch(ch) {
	    case '#':
		if (processor.scriptingMode) {
		    if (i == 0 && i+1 < len && s.charAt(i+1) == '!') {
			// Treat as a comment to allow '#!' scripts.
			i += 2;
			while (i < len && s.charAt(i) != SEP_END) i++;
			continue;
		    } else if (i + 2 < len) {
			int isv = i;
			if (s.charAt(i+1) == '+' && s.charAt(i+2) == 'T') {
			    tokens.add(new Token(Operator.START_TOKEN_TRACING,
						 "#+T", isv+2, level));
			    i += 2;
			    continue;
			} else if (s.charAt(i+1)=='-' && s.charAt(i+2)=='T') {
			    tokens.add(new Token(Operator.STOP_TOKEN_TRACING,
						 "#+T", isv+2, level));
			    i += 2;
			    continue;
			} else if (s.charAt(i+1)=='+' && s.charAt(i+2)=='S') {
			    tokens.add(new Token(Operator.START_STACK_TRACING,
						 "#+S", isv+2, level));
			    i += 2;
			    continue;
			} else if (s.charAt(i+1)=='-' && s.charAt(i+2)=='S') {
			    tokens.add(new Token(Operator.STOP_STACK_TRACING,
						 "#-S", isv+2, level));
			    i += 2;
			    continue;
			} else if (s.charAt(i+1)=='#' && s.charAt(i+2)=='#') {
			    i += 3;
			    i = skipWhitespace(s, i, len, false);
			    if (i == len) {
				i--;
				continue;
			    } else {
				// Make sure we are at the top level.
				if (parenPeer != null || bracePeer != null
				    || bracketPeer != null || params != null
				    || varPeer != null || condPeer != null) {
				    String msg = errorMsg("misplaced3Hashes");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				// Behave as if we were done, but then
				// restart where we left off.
				if (importStarted) {
				    next = new Token(Operator.FINISH_IMPORT,
						     prev.getName(),
						     isv+2,
						     prev.getLevel());
				    tokens.add(next);
				    importStarted = false;
				    finishNeeded = false;
				}
				if (firstQMark != null) {
				    firstQMark.setQmarkIndex(qmarkIndex);
				    firstQMark = null;
				}
				qmarkIndex = 0;
				// vset = vsetThreadLocal.get();
				Set<String> savedVset = new
				    HashSet<String>(vsetThreadLocal.get());
				/*
				vsetStack = new Stack<Set<String>>();
				vsetStack.push(vset);
				*/
				if (tarray != null) {
				    tarray.add(tokens);
				    tokens = null;
				    return tokens;
				} else {
				    // process the current token queue
				    // as in parseExpression
				    Token first = tokens.peek();
				    if (first != null) {
					if (first.getType() == Operator.PLUS) {
					    tokens.poll();
					}
				    }
				    parseExpression(tokens, s);
				    processor.eval(s);
				    processor.getResult();
				    processor.clear();
				}
				// reinitialize and continue
				vsetStack = new Stack<Set<String>>();
				vset = savedVset;
				vsetThreadLocal.set(savedVset);
				vsetStack.push(savedVset);
				tokens = new LinkedList<Token>();
				i--;
				continue;
			    }
			} else {
			    String msg = errorMsg("syntaxError");
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
		    } else {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, i);
		    }
		} else {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, i);
		}
	    case ';':
		mustSync = false; // Just in case.
		if (bracePeer != null) {
		    if (parenPeer.getType() == Operator.OBRACE) {
			// We are inside a lambda expression's body so
			// a semicolon is OK - used to delimit statements.
			level = bracePeer.getLevel();
			if (ptype != null && binaryOps.contains(ptype)) {
			    String msg = errorMsg("syntaxError");
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
			next = new Token(Operator.SEMICOLON, ";", offset+i,
					 level + SEMICOLON_OFFSET);
			tokens.add(next);
			if (varPeer != null
			    && varPeer.getBracePeer() == bracePeer) {
			    params.add((String)varPeer.getValue());
			    vset.add((String)varPeer.getValue());
			}
		    } else {
			// If we are directly inside an object but not
			// inside a lambda expression or method, a ';' is
			// not legal.
			String msg = errorMsg("semicolon");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		} else {
		    if (tarray != null || processor.scriptingMode) {
			if (condPeer != null) {
			    String msg = errorMsg("missingColon");
			    throw new ObjectParser.Exception(msg,
							     filenameTL.get(),
							     s, i);
			} else if (parenPeer != null) {
			    String msg = errorMsg("unbalancedParens");
			    throw new ObjectParser.Exception(msg,
							     filenameTL.get(),
							     s, i);
			}
			if (varPeer != null || processor.scriptingMode) {
			    level = 0;
			    if (ptype != null && binaryOps.contains(ptype)) {
				String msg = errorMsg("syntaxError");
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, i);
			    }
			    next = new Token(Operator.SEMICOLON, ";",
					     offset+i,
					     level + SEMICOLON_OFFSET);
			    tokens.add(next);
			    if (varPeer != null) {
				Token vbracePeer = varPeer.getBracePeer();
				if (bracePeer != null
				    && vbracePeer == bracePeer) {
				    params.add((String)varPeer.getValue());
				    vset.add((String)varPeer.getValue());
				} else if (vbracePeer == null) {
				    vset.add((String)varPeer.getValue());
				}
			    }
			    if (firstQMark != null) {
				// No braces, so this is a top-level
				// semicolon.
				firstQMark.setQmarkIndex(qmarkIndex);
				qmarkIndex = 0;
				firstQMark = null;
			    }
			} else if (!processor.scriptingMode) {
			    if (firstQMark != null) {
				firstQMark.setQmarkIndex(qmarkIndex);
				qmarkIndex = 0;
				firstQMark = null;
			    }
			    if (tokens.size() > 0) {
				tarray.add(tokens);
				tokens = new LinkedList<Token>();
			    }
			}
		    } else {
			// to indicate that nothing should follow a semicolon
			// in a top-level expression without scripting mode.
			if (processor.scriptingMode == false) {
			    semicolonSeen = true;
			}
		    }
		}
		if (varPeer != null && varPeer.getBracePeer() == parenPeer) {
		    // The  semicolon is at top level or part of a function
		    // or method definition, so we should pop it at this point.
		    varPeer = (varPeers.size() == 0)? null: varPeers.pop();
		}
		if (importStarted) {
		    int ii = nextIdentIndex(s, i+1, len);
		    if (ii < len) {
			char iich = s.charAt(ii);
			if (!(iich == 'i' && ii < len-6
			      && s.charAt(ii+1) == 'm'
			      && s.charAt(ii+2) == 'p'
			      && s.charAt(ii+3) == 'o'
			      && s.charAt(ii+4) == 'r'
			      && s.charAt(ii+5) == 't'
			      && !Character.isJavaIdentifierPart
			      (s.charAt(ii+6)))) {
			    // next is not an import statement
			    prev = next;
			    next = new Token(Operator.FINISH_IMPORT, ";",
					     offset+i, prev.getLevel());
			    finishNeeded = false;
			    tokens.add(next);
			    parseExpression(tokens, s);
			    tokens.clear();
			}
		    }
		}
		// make it look like we are starting from scratch
		if (ptype != Operator.OBRACE
		    && next != null && next.getType() == Operator.SEMICOLON) {
		    nonNullPrev = next;
		}
		prev = null;
		importStarted = false;
		sawObjectProp = false;
		continue;
	    case '-':
		// change to UNARY_MINUS or BINARY_MINUS while parsing.
		if (prev == null || ptype == Operator.OPAREN
		    || ptype == Operator.OBRACE
		    || ptype == Operator.OBJOPENBRACE
		    || ptype == Operator.OBRACKET
		    || ptype == Operator.VAR
		    || ptype == Operator.ASSIGN1
		    || ptype == Operator.ASSIGN2
		    || ptype == Operator.COMMA
		    || ptype == Operator.ACTIVE_COMMA
		    || binaryOps.contains(ptype) /*
		    || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY*/) {
		    next = new Token(Operator.UNARY_MINUS, "-",
				     offset+i, level+2);
		} else if (ptype == Operator.UNARY_MINUS) {
		    prev.type = Operator.UNARY_PLUS;
		    nonNullPrev = null;
		    sawObjectProp = false;
		    continue;
		} else if (ptype == Operator.UNARY_PLUS) {
		    prev.type = Operator.UNARY_MINUS;
		    nonNullPrev = null;
		    sawObjectProp = false;
		    continue;
		} else if (ptype == Operator.NOT) {
		    String msg = errorMsg("adjacentOps", "~", "-");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		} else {
		    if (ptype == Operator.PLUS) {
			prev.type = Operator.BINARY_MINUS;
			nonNullPrev = null;
			continue;
		    } else if (ptype == Operator.BINARY_MINUS) {
			prev.type = Operator.PLUS;
			nonNullPrev = null;
			continue;
		    }
		    level--;
		    if (ptype == null || binaryOps.contains(ptype)) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    next = new Token(Operator.BINARY_MINUS, "-",
				     offset+i, level);
		}
		tokens.add(next);
		break;
	    case '^':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(),
						     i);
		}
		*/
		if (ptype == Operator.OPAREN
		    || ptype == Operator.COMMA
		    || ptype == Operator.ACTIVE_COMMA) {
		    /*
		    next = new Token(Operator.XOR, "^",
				     offset+i, level);
		    */
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		} else {
		    /*
		    if (ptype == Operator.PLUS || ptype == Operator.TIMES
			|| ptype == Operator.DIVIDEBY || ptype == Operator.NOT
			|| ptype == Operator.OR || ptype == Operator.XOR
			|| ptype == Operator.MOD) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, s,
							 filenameTL.get(),
							 i);
		    }
		    */
		    level--;
		    next = new Token(Operator.XOR, "^",
				     offset+i, level + BITWISE_OFFSET);
		}
		tokens.add (next);
		break;
	    case '|':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		if (ptype == Operator.OPAREN
		    || ptype == Operator.COMMA
		    || ptype == Operator.ACTIVE_COMMA) {
		    /*
		    next = new Token(Operator.OR, "|",
				     offset+i, level);
		    */
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		} else {
		    /*
		    if (ptype == Operator.PLUS || ptype == Operator.TIMES
			|| ptype == Operator.DIVIDEBY || ptype == Operator.NOT
			|| ptype == Operator.OR || ptype == Operator.XOR
			|| ptype == Operator.MOD
			|| ptype == Operator.STRING) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, s,
							 filenameTL.get(), i);
		    }
		    */
		    level--;
		    next = new Token(Operator.OR, "|",
				     offset+i, level - 1 + BITWISE_OFFSET );
		}
		tokens.add (next);
		break;
	    case '?':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (ptype == Operator.OPAREN) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		level--;
		next = new Token(Operator.QMARK, "?",
				 offset+i, level + COND_OFFSET);
		if (firstQMark == null) {
		    firstQMark = next;
		}
		// next.setQmarkDepth(++qmarkdepth);
		if (condPeer != null) {
		    condPeers.push(condPeer);
		}
		condPeer = next;
		next.setBracePeer(bracePeer);
		tokens.add(next);
		break;
	    case ':':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (i < len-1 && s.charAt(i+1) == ':') {
		    if (notBeforeMethodRef.contains(ptype)) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    // start of a method reference following an
		    // expression, not a class name.
		    int ii = i + 2;
		    if (ii >= len) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i+1);
		    }
		    ii = skipWhitespace(s, ii, true);
		    char mrch = s.charAt(ii);
		    if (Character.isJavaIdentifierStart(mrch)) {
			int start = ii;
			while (Character.isJavaIdentifierPart(mrch)) {
			    ii++;
			    if (ii == len) break;
			    mrch = s.charAt(ii);
			}
			String mname = s.substring(start, ii);
			ii--;
			next = new Token(Operator.METHOD_REF,
					 mname, offset + ii,
					 level - DOUBLE_COLON_OFFSET);
			next.setValue(mname);
			tokens.add(next);
			i = ii;
			break;
		    } else {
			// error: identifier must follow "::".
		    }
		}
		if (epObjectStarted == false) {
		    if (condPeer == null) {
			String msg = errorMsg("badCondEnd");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    if (condPeer.getBracePeer() != bracePeer) {
			String msg = errorMsg("badCondNest");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		}
		if (ptype == Operator.OPAREN) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (epObjectStarted) {
		    epObjectStarted = false;
		    colonExpected = false;
		    level = bracePeer.getLevel();
		    // next = new Token(Operator.COLON, ":", offset+i, level);
		    nonNullPrev = null;
		    continue;	// don't want to change prev and next
		} else {
		    level--;
		    next = new Token(Operator.COLON, ":",
				     offset+i, condPeer.getLevel());
		    next.setQmarkIndex(qmarkIndex++);
		    if (firstQMark == null) {
			// This should not happen: something should have
			// complained about a syntax error to prevent
			// this error from occuring.
			String msg = errorMsg("firstQMarkNull");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    condPeer.setCondPeer(next);
		    condPeer = (condPeers.size() == 0)? null: condPeers.pop();
		}
		// next.setQmarkDepth(qmarkdepth--);
		tokens.add(next);
		break;
	    case '+':
		if (prev == null || ptype == Operator.OPAREN
		    || ptype == Operator.OBRACE
		    || ptype == Operator.OBJOPENBRACE
		    || ptype == Operator.OBRACKET
		    || ptype == Operator.VAR
		    || ptype == Operator.ASSIGN1
		    || ptype == Operator.ASSIGN2
		    || ptype == Operator.COMMA
		    || ptype == Operator.ACTIVE_COMMA) {
		    next = new Token(Operator.UNARY_PLUS, "+",
				     offset+i, level);
		} else {
		    if (ptype == null || binaryOps.contains(ptype)) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    if (ptype == Operator.PLUS || ptype == Operator.TIMES
			|| ptype == Operator.DIVIDEBY || ptype == Operator.NOT
			|| ptype == Operator.OR || ptype == Operator.XOR
			|| ptype == Operator.MATH_MOD
			|| ptype == Operator.MOD) {
			String msg = errorMsg("syntaxError");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    level--;
		    next = new Token(Operator.PLUS, "+",
				     offset+i, level);
		}
		tokens.add (next);
		break;
	    case '~':
		if (prev != null) {
		    if (ptype == Operator.UNARY_MINUS) {
			String msg = errorMsg("adjacentOps", "-", "~");
			throw new Exception(msg, s, i);
		    } else if (ptype == Operator.NOT) {
			prev.type = Operator.UNARY_PLUS;
			nonNullPrev = null;
			sawObjectProp = false;
			continue;
		    } else if (ptype == Operator.UNARY_PLUS) {
			prev.type = Operator.NOT;
			nonNullPrev = null;
			sawObjectProp = false;
			continue;
		    }
		}
		level += 2;
		next = new Token(Operator.NOT, "~", offset+i, level);
		level -= 3;
		tokens.add(next);
		break;
	    case '!':
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MATH_MOD
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		level += 2;
		next = new Token(Operator.LNOT, "!", offset+i, level);
		level -= 3;
		tokens.add(next);
		break;
	    case '&':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		next = new Token(Operator.AND, "&", offset+i,
				 level + BITWISE_OFFSET);
		level--;
		tokens.add(next);
		break;
	    case '*':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		next = new Token(Operator.TIMES, "*", offset+i, level);
		level--;
		tokens.add(next);
		break;
	    case '<':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (i < len-1 && s.charAt(i+1) == '<') {
		    i++;
		    next = new Token(Operator.LSHIFT, "<<", offset+i,
				     level + SHIFT_OFFSET);
		} else {
		    if (ptype == Operator.VARIABLE) {
			ltPrevToken = prev;
		    }
		    next = new Token(Operator.LT, "<", offset+i,
				     level + RELEQ_OFFSET);
		}
		level--;
		tokens.add(next);
		break;
	    case '>':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (i < len - 1 && s.charAt(i+1) == '>') {
		    i++;
		    if (i < len -1 && s.charAt(i+1) == '>') {
			i++;
			next = new Token(Operator.URSHIFT, ">>>", offset+i,
					 level + SHIFT_OFFSET);
		    } else {
			i++;
			next = new Token(Operator.RSHIFT, ">>", offset+i,
					 level + SHIFT_OFFSET);
		    }
		} else {
		    next = new Token(Operator.GT, ">", offset+i,
				     level + RELEQ_OFFSET);
		}
		level--;
		tokens.add(next);
		break;
	    case '=':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		level--;
		next = new Token(Operator.PEQ, "=", offset+i,
				 level + EQ_OFFSET);
		tokens.add(next);
		peqToken = prev;
		break;
	    case '/':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		next = new Token(Operator.DIVIDEBY, "/", offset+i, level);
		level--;
		tokens.add(next);
		break;
	    case '%':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		/*
		if (prev == null) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		if (ptype == Operator.PLUS || ptype == Operator.TIMES
		    || ptype == Operator.DIVIDEBY || ptype == Operator.NOT
		    || ptype == Operator.OR || ptype == Operator.XOR
		    || ptype == Operator.MOD) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, s,
						     filenameTL.get(), i);
		}
		*/
		int inext = i+1;
		if (inext < len && s.charAt(inext) == '%') {
		    next = new Token(Operator.MATH_MOD, "%%", offset+i, level);
		    i++;
		} else {
		    next = new Token(Operator.MOD, "%", offset+i, level);
		}
		level--;
		tokens.add(next);
		break;
	    case ',':
		if (ptype == null || binaryOps.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (parenPeer == null) {
		    String msg = errorMsg("badComma");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		level = parenPeer.getLevel();
		if (parenPeer == bracketPeer) {
		    if (bracketPeer.getFunct() != null) {
			String msg = errorMsg("activeComma");
			throw new ObjectParser.Exception(msg, filenameTL.get(),
							 s, i);
		    }
		    next = new Token(Operator.ACTIVE_COMMA, ",",
				     offset+i, level);
		    next.setBracketPeer(bracketPeer);
		} else if (parenPeer == bracePeer) {
		    next = new Token(Operator.ACTIVE_COMMA, ",",
				     offset+i, level);
		    next.setValue(objectProps.pop());
		    next.setBracePeer(bracePeer);
		    epObjectStarted = true;
		} else {
		    next = new Token(Operator.COMMA, ",", offset+i, level);
		}
		parenPeer.incrArgCount();
		tokens.add(next);
		if (parenPeer.incrementedLevel()) {
		    // Add because we set level to that for
		    // parenPeer a few lines earlier.
		    level += (1 + LEVEL_OFFSET);
		}
		break;
	    case '[':
		boolean btest = (prev == null);
		if (!btest) {
		    switch (ptype) {
		    case CPAREN:
		    case VARIABLE:
		    case OBJCLOSEBRACE:
		    case CBRACKET:
			btest = false;
			break;
		    default:
			if (notBeforeOBRACKET.contains(ptype)) {
			    String msg = errorMsg("badOBracket");
			    throw new ObjectParser.Exception(msg,
							     filenameTL.get(),
							     s, i);
			}
			btest = true;
		    }
		}
		oldlevel = level;
		level += 1 + LEVEL_OFFSET;
		next = new Token(Operator.OBRACKET, "[", offset+i, level);
		if (btest == false) {
		    next.setFunct(new Token(Operator.METHOD, "get",
					    offset+i, oldlevel));
		}
		if (bracketPeer != null) {
		    bracketPeers.push(bracketPeer);
		}
		    bracketPeer = next;
		if (parenPeer != null) {
			parenPeers.push(parenPeer);
		}
		parenPeer = next;
		tokens.add(next);
		break;
	    case ']':
		if (prev == null) {
		    String msg = errorMsg("badCBracket");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (bracketPeer == null) {
		    String msg = errorMsg("unbalancedBrackets");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		level = bracketPeer.getLevel();
		funct = bracketPeer.getFunct();
		if (funct != null) {
		    // treat as a method
		    bracketPeer.changeType(Operator.OPAREN);
		    next = new Token(Operator.CPAREN, "]", offset+i, level);
		    next.setFunct(funct);
		    next.setArgCount(1);
		} else {
		    next = new Token(Operator.CBRACKET, "]", offset+i, level);
		}
		level -= (1 + LEVEL_OFFSET);
		next.setBracketPeer(bracketPeer);
		if (bracketPeer == parenPeer) {
		    parenPeer = (parenPeers.size() == 0)? null:
			parenPeers.pop();
		}
		bracketPeer = (bracketPeers.size() == 0)? null:
		    bracketPeers.pop();
		tokens.add(next);
		break;
	    case '`':
		level++; // so we have to decr after each operator
		next = new Token(Operator.BACKQUOTE, "function",
				 offset+i, level);
		vset = new HashSet<String>();
		vsetStack.push(vset);
		tokens.add(next);
		break;
	    case '{':
		if (notBeforeOBRACE.contains(ptype)) {
		    String  msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, i);
		}
		if (ptype == Operator.BACKQUOTE) {
		    level += 1 + LEVEL_OFFSET;
		    next = new Token(Operator.OPAREN, "(", offset+i, level);
		    tokens.add(next);
		    next = new Token(Operator.CPAREN, ")", offset+i, level);
		    next.setArgCount(0);
		    if (params != null) paramsStack.push(params);
		    params = new HashSet<String>();
		    // "this" is a reserved keyword used by
		    // methods. We don't allow it as the name of a parameter.
		    params.add("this");
		    tokens.add(next);
		    level -= 1 + LEVEL_OFFSET;
		    expectingOBrace = true;
		}
		if (expectingOBrace) {
		    // expecting a '{' in a function def / lambda expression
		    expectingOBrace = false;
		} else {
		    epObjectStarted = true;
		    // String msg = errorMsg("syntaxError");
		    // throw new ObjectParser.Exception(msg, s, i);
		}
		level += 1 + LEVEL_OFFSET;
		next = new Token
		    ((epObjectStarted? Operator.OBJOPENBRACE: Operator.OBRACE),
		     "{", offset+i, level);
		if (ptype == Operator.BACKQUOTE) {
		    prev.changeType(Operator.FUNCTION_KEYWORD);
		    next.setBackquoted();
		}
		if (bracePeer != null) {
		    if (bracePeer.getType() == Operator.OBJOPENBRACE
			&& epObjectStarted == false && methodStartSeen) {
			// So we can use bracePeer to tell a function is
			// actually a method so that "this" is allowed.
			next.setForMethod();
		    }
		    bracePeers.push(bracePeer);
		}
		bracePeer = next;
		if (parenPeer != null) {
		    parenPeers.push(parenPeer);
		}
		parenPeer = next;
		if (!epObjectStarted) {
		    // function definition
		    firstQMarkStack.push(new QMarkPair(firstQMark, qmarkIndex));
		    firstQMark = null;
		    qmarkIndex = 0;
		}
		tokens.add(next);
		methodStartSeen = false;
		break;
	    case '}':
		if (prev == null && nonNullPrev == null) {
		    String msg = errorMsg("badCBrace");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (nonNullPrev != null) {
		    nonNullPrev.changeType(Operator.NOOP);
		}
		if (bracePeer == null) {
		    String msg = errorMsg("unbalancedBraces");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		Operator bptype = bracePeer.getType();
		level = bracePeer.getLevel();
		if (bptype == Operator.OBRACE
		    && bracePeer.backquoted()) {
		    // Need to insert '; true'
		    tokens.add(new Token(Operator.SEMICOLON, ";", offset+i,
					 level + SEMICOLON_OFFSET));
		    next = new Token(Operator.BOOLEAN, "true", offset+i,
				    level+1);
		    next.setValue(Boolean.TRUE);
		    tokens.add(next);
		}

		next = new Token
		    (((bptype == Operator.OBJOPENBRACE)? Operator.OBJCLOSEBRACE:
		      Operator.CBRACE), "}", offset+i, level);
		level -= (1 + LEVEL_OFFSET);
		next.setBracePeer(bracePeer);
		if (bptype == Operator.OBJOPENBRACE) {
		    next.setValue(objectProps.pop());
		    next.setBracePeer(bracePeer);
		} else {
		    // function or lambda expression definition
		    if (vsetStack.size() > 1) {
			vsetStack.pop();
			vset = vsetStack.peek();
		    }
		    params = (paramsStack.size() == 0)? null:
			paramsStack.pop();
		}
		if (varPeer != null && varPeer.getBracePeer() == parenPeer) {
		    varPeer = (varPeers.size() == 0)? null: varPeers.pop();
		}
		parenPeer = (parenPeers.size() == 0)? null:
		    parenPeers.pop();
		bracePeer = (bracePeers.size() == 0)? null: bracePeers.pop();
		if (bptype == Operator.OBRACE) {
		    if (firstQMark != null) {
			firstQMark.setQmarkIndex(qmarkIndex);
		    }
		    QMarkPair qmpair = firstQMarkStack.pop();
		    if (qmpair != null) {
			firstQMark = qmpair.firstQMark;
			qmarkIndex = qmpair.qmarkIndex;
		    }
		}
		tokens.add(next);
		if (bptype == Operator.OBRACE &&
		    next.getBracePeer().backquoted) {
		    // act as if '()' follows.
		    next = new Token(Operator.METHOD, "invoke",
				     offset+i, level);
		    funct = next;
		    level += 1 + LEVEL_OFFSET;
		    next = new Token(Operator.OPAREN, "(", offset+i, level);
		    if (funct != null) {
			next.setFunct(funct);
		    }
		    tokens.add(next);
		    next = new Token(Operator.CPAREN, ")", offset+i, level);
		    next.setFunct(funct);
		    next.setArgCount(0);
		    tokens.add(next);
		    level -= (1 + LEVEL_OFFSET);
		}
		break;
	    case '(':
		if (epObjectStarted && colonExpected) {
		    // allow standard Java method declarations
		    // by inserting the required tokens.
		    epObjectStarted  = false;
		    colonExpected = false;
		    level = bracePeer.getLevel();
		    // we don't add a ':' token for an object's
		    // method or property def.
		    // next we need a FUNCTION_KEYWORD
		    level++;
		    prev = new Token(Operator.FUNCTION_KEYWORD, "function",
				     offset+i, level);
		    vset = new HashSet<String>();
		    vsetStack.push(vset);
		    vset.add("this");
		    prev.setForMethod();
		    if (mustSync) {
			prev.sync();
			mustSync = false;
		    }
		    methodStartSeen = true;
		    functkwSeen = true;
		    tokens.add(prev);
		    // This puts us in the same state as if we had seen
		    // ": function (" after the method name with '(' being
		    // processed.
		}
		funct = null;
		if (prev != null && !functkwSeen) {
		    switch (ptype) {
		    case CONSTRUCTOR:
			funct = prev;
			level += 1 + LEVEL_OFFSET;
			break;
		    case METHOD:
			funct = prev;
			level += 1 + LEVEL_OFFSET;
			break;
		    case VARIABLE:
			prev.changeType(Operator.FUNCTION);
			// fall through
		    case IMPORT: // import is handled as a function
			funct = prev;
			level += 1 + LEVEL_OFFSET;
			break;
		    case OBRACE:
		    case OBRACKET:
		    case VAR:
		    case QVAR:
		    case QQVAR:
		    case ASSIGN1:
		    case ASSIGN2:
		    case UNARY_MINUS:
		    case BINARY_MINUS:
		    case PLUS:
		    case TIMES:
		    case DIVIDEBY:
		    case MOD:
		    case MATH_MOD:
		    case LSHIFT:
		    case RSHIFT:
		    case URSHIFT:
		    case NOT:
		    case AND:
		    case OR:
		    case XOR:
		    case LNOT:
		    case LAND:
		    case LOR:
		    case EQ:
		    case NE:
		    case GT:
		    case LT:
		    case GE:
		    case LE:
		    case QMARK:
		    case COLON:
		    case COMMA:
		    case OPAREN:
			level += 2 + LEVEL_OFFSET;
			break;
		    case CBRACE:
			// insert a method call
			prev = new Token(Operator.METHOD, "invoke",
					 offset+i, level);
			funct = prev;
			tokens.add(prev);
			level += 1 + LEVEL_OFFSET;
			break;
		    default:
			if (sawObjectProp == false) {
			    // error
			    String msg = errorMsg("badOParen");
			    throw new ObjectParser.Exception(msg,
							     filenameTL.get(),
							     s, i);
			} else {
			    level += 1 + LEVEL_OFFSET;
			}
			break;
		    }
		} else {
		    level += 1 + LEVEL_OFFSET;
		}
		next = new Token(Operator.OPAREN, "(", offset+i, level);
		if (funct != null) {
		    next.setFunct(funct);
		}
		if (parenPeer != null) {
		    parenPeers.push(parenPeer);
		}
		parenPeer = next;
		if (functkwSeen) {
		    functParams = true;
		    if (params != null) paramsStack.push(params);
		    params = new HashSet<String>();
		    // "this" is a reserved keyword used by
		    // methods. We don't allow it as the name of a parameter.
		    params.add("this");
		}
		tokens.add(next);
		// Fix up for method calls with some operators (.e.g, ==)
		if (prev != null) {
		    Operator ptType = prev.getType();
		    if (ptType == Operator.METHOD
			|| ptType == Operator.CONSTRUCTOR
			|| ptType == Operator.FUNCTION) {
			next.incrementingLevel();
			level += (1 + LEVEL_OFFSET);
			// System.out.println("would increment level");
		    }
		}
		break;
	    case ')':
		if (prev == null) {
		    String msg = errorMsg("badCParen");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		boolean decr = false;
		if (parenPeer == null) {
		    String msg = errorMsg("unbalancedParens");
		    throw new ObjectParser.Exception(msg, filenameTL.get(),
						     s, i);
		}
		if (parenPeer.incrementedLevel()) {
		    // System.out.println("would decrement level");
		    level -= (1 + LEVEL_OFFSET);

		}
		if (parenPeer != prev) {
		    parenPeer.incrArgCount();
		}
		level = parenPeer.getLevel();
		next = new Token(Operator.CPAREN, ")", offset+i, level);
		level -= (1 + LEVEL_OFFSET);
		funct = parenPeer.getFunct();
		if (funct != null) {
		    next.setFunct(funct);
		    next.setArgCount(parenPeer.getArgCount());
		}
		tokens.add(next);
		parenPeer = (parenPeers.size() ==0)? null: parenPeers.pop();
		if (functkwSeen) {
		    expectingOBrace = true;
		}
		functkwSeen = false;
		functParams = false;
		break;
	    case '"':
		if (ptype != null && notBeforeString.contains(ptype)) {
		    String msg = errorMsg("syntaxError");
		    throw new ObjectParser.Exception (msg, filenameTL.get(),
						      s, i);
		}
		// String stail = s.substring(i);
		String stail = getLineTail(s, i);
		Matcher smatcher = STRING_PATTERN.matcher(stail);
		if (smatcher.lookingAt()) {
		    int start = i + smatcher.start(1);
		    int end = i + smatcher.end(1);
		    String svalue = s.substring(start, end);
		    int ind = svalue.indexOf('\\');
		    if (ind != -1) {
			StringBuilder sb = new StringBuilder();
			while (ind != -1 && ind < svalue.length()) {
			    sb.append(svalue.substring(0, ind));
			    ind++;
			    svalue = svalue.substring(ind);
			    switch(svalue.charAt(0)) {
			    case 'b':
				sb.append("\b");
				svalue = svalue.substring(1);
				break;
			    case 'f':
				sb.append("\f");
				svalue = svalue.substring(1);
				break;
			    case 'n':
				sb.append("\n");
				svalue = svalue.substring(1);
				break;
			    case 'r':
				sb.append("\r");
				svalue = svalue.substring(1);
				break;
			    case 't':
				sb.append("\t");
				svalue = svalue.substring(1);
				break;
			    case '\\':
				sb.append("\\");
				svalue = svalue.substring(1);
				break;
			    case 'u':
				int codepoint = 0;
				for (int j = 1; j < 5; j++) {
				    switch(svalue.charAt(j)) {
				    case '0':
					codepoint = (codepoint << 4) | 0;
					break;
				    case '1':
					codepoint = (codepoint << 4) | 1;
					break;
				    case '2':
					codepoint = (codepoint << 4) | 2;
					break;
				    case '3':
					codepoint = (codepoint << 4) | 3;
					break;
				    case '4':
					codepoint = (codepoint << 4) | 4;
					break;
				    case '5':
					codepoint = (codepoint << 4) | 5;
					break;
				    case '6':
					codepoint = (codepoint << 4) | 6;
					break;
				    case '7':
					codepoint = (codepoint << 4) | 7;
					break;
				    case '8':
					codepoint = (codepoint << 4) | 8;
					break;
				    case '9':
					codepoint = (codepoint << 4) | 9;
					break;
				    case 'a':
				    case 'A':
					codepoint = (codepoint << 4) | 10;
					break;
				    case 'b':
				    case 'B':
					codepoint = (codepoint << 4) | 11;
					break;
				    case 'c':
				    case 'C':
					codepoint = (codepoint << 4) | 12;
					break;
				    case 'd':
				    case 'D':
					codepoint = (codepoint << 4) | 13;
					break;
				    case 'e':
				    case 'E':
					codepoint = (codepoint << 4) | 14;
					break;
				    case 'f':
				    case 'F':
					codepoint = (codepoint << 4) | 15;
					break;
				    default:
					String msg = errorMsg("badUnicode");
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				}
				sb.append((char)codepoint);
				svalue = svalue.substring(5);
				break;
			    default:
				break;
			    }
			    ind = svalue.indexOf('\\');
			}
			sb.append(svalue);
			svalue = sb.toString();
		    }

		    if (epObjectStarted) {
			objectProps.push(svalue);
			sawObjectProp = true;
			colonExpected = true;
			nonNullPrev = null;
			i = end;
			continue;
		    } else {
			level++;
			next = new Token(Operator.STRING, svalue, offset+i,
					 level);
			next.setValue(svalue);
			tokens.add(next);
		    }
		    i = end; // the for loop will skip the terminating quote
		} else {
		    String msg = errorMsg("badString");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, i);
		}
		break;
	    case '.': case '0': case '1': case '2': case '3': case '4':
	    case '5': case '6': case '7': case '8': case '9':
		// String tail = s.substring(i);
		String tail = getLineTail(s, i);
		Matcher matcher = DOUBLE_PATTERN.matcher(tail);
		if (matcher.lookingAt()) {
		    if (ptype != null) {
			if (ptype == Operator.NUMBER
			    || ptype == Operator.CLASS
			    || ptype == Operator.STRING
			    || ptype == Operator.VARIABLE
			    // || ptype == Operator.CONSTRUCTOR
			    // || ptype == Operator.METHOD
			    || ptype == Operator.CONSTANT
			    // || ptype == Operator.FUNCTION
			    || ptype == Operator.IMPORT
			    || ptype == Operator.BOOLEAN) {
			    String msg = errorMsg("syntaxError");
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
		    }
		    int end = i + matcher.end();
		    String number = s.substring(i, end);
		    level++; // so we have to decr after each operator
		    next = new Token(Operator.NUMBER, number, offset+i, level);
		    tokens.add(next);
		    i = end - 1; // subtract 1 because the for loop adds 1
		    try {
			if (INTEGER_PATTERN.matcher(number).matches()) {
			    if (prev != null
				&& prev.getType() == Operator.UNARY_MINUS
				&& end < len && s.charAt(end) == 'L'
				&& number.equals("9223372036854775808")) {
				// Ignore the unary minus and use a
				// negative number: -Long.MAX_VALUE is
				// larger than -Long.MIN_VALUE.
				prev.changeType(Operator.NOOP);
				number = "-" + number;
			    }
			    long lval = Long.parseLong(number);
			    if (lval > Integer.MAX_VALUE) {
				next.setValue(Long.valueOf(lval));
				if (end < len && s.charAt(end) == 'L') {
				    i++;
				} else if (lval == (1L + Integer.MAX_VALUE)
					   && prev != null
					   && prev.getType()
					   == Operator.UNARY_MINUS) {
				    // Ignore the unary minus and set the
				    // value to the smallest integer because
				    // Integer.MAX_VALUE = 2147483647 while
				    // Integer.MIN_VALUE = -2147483648
				    prev.changeType(Operator.NOOP);
				    next.setValue(Integer.MIN_VALUE);
				}
			    } else {
				// explicit long case for values that
				// could be an int.
				if (end < len && s.charAt(end) == 'L') {
				    // We found an explicit long integer
				    next.setValue(Long.valueOf(lval));
				    i++;
				} else {
				    next.setValue(Integer.valueOf((int)lval));
				}
			    }
			} else {
			    next.setValue(Double.valueOf(number));
			}
		    } catch (Exception e) {
			String msg = errorMsg("badNumber");
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, i);
		    }
		} else if (ch == '.') {
		    if (ptype == Operator.NULL
			|| ptype == Operator.VOID
			|| (ptype != Operator.VARIABLE
			    // && ptype != Operator.METHOD
			    // && ptype != Operator.FUNCTION
			    // && ptype != Operator.CONSTRUCTOR
			    && ptype != Operator.CPAREN
			    && ptype != Operator.CBRACKET
			    && ptype != Operator.CBRACE
			    && ptype != Operator.CLASS
			    && ptype != Operator.STRING)) {
			// System.out.println(ptype);
			String msg = errorMsg("misplacedDot");
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, i);
		    }
		    next = new Token(Operator.DOT, ".", offset+i, level);
		} else {
		    String msg = errorMsg("badNumber");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, i);
		}
		break;
	    default:
		if (epObjectStarted) {
		    // special case: must be a Java identifier
		    // and will simply be put on a stack.
		    if (Character.isJavaIdentifierStart(ch)) {
			int start = i;
			while (Character.isJavaIdentifierPart(ch)) {
			    i++;
			    if (i == len) break;
			    ch = s.charAt(i);
			}
			objectProps.push(s.substring(start, i));
			sawObjectProp = true;
			i--;	// because of 'for' loop
			colonExpected = true;
		    } else {
			// error
			String tmp = s.substring(i, i+1);
			String msg = errorMsg("notPropName", tmp);
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, i);
		    }
		    // we don't want to update next and prev.
		    nonNullPrev = null;
		    continue;
		}
		if (Character.isJavaIdentifierStart(ch)) {
		    int start = i;
		    boolean dotseen = false;
		    char lastchar = ch;
		    int firstdot = -1;
		    int lastdot = -1;
		    i = nextIdentIndex(s, i, len);
		    boolean sawMethodRef = false;
		    int methodRefInd = -1;
		    if (i != len) {
			ch = s.charAt(i);
			if (ch == ':' && i < len-1 /*&& s.charAt(i) == ':'*/) {
			    methodRefInd = i;
			    i += 2;
			    i = nextIdentIndex(s, i, len);
			    if (i == len) {
				// error
			    }
			    ch = s.charAt(i);
			    if (!Character.isJavaIdentifierStart(ch)) {
				// error
			    }
			    while (Character.isJavaIdentifierPart(ch)) {
				i++;
				if (i == len) break;
				ch = s.charAt(i);
			    }
			    sawMethodRef = true;
			} else {
			    while ((ch == '.' && lastchar != '.')
				   || (lastchar == '.'
				       && Character.isJavaIdentifierStart(ch))
				   || (lastchar != '.'
				       && Character.isJavaIdentifierPart(ch))) {
				if (ch == '.') {
				    if (firstdot == -1) {
					firstdot = i;
				    }
				    lastdot = i;
				}
				lastchar = ch;
				i = nextIdentIndex(s, i, len);
				if (i == len) break;
				ch = s.charAt(i);
				if (ch == ':' && i < len-1
				    && s.charAt(i+1) == ':') {
				    methodRefInd = i;
				    i += 2;
				    i = nextIdentIndex(s, i, len);
				    if (i == len) {
					// error
					String msg =errorMsg("unexpectedEnd");
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				    ch = s.charAt(i);
				    if (!Character.isJavaIdentifierStart(ch)) {
					String msg =errorMsg("notIdentifier");
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);

				    }
				    while (Character.isJavaIdentifierPart(ch)) {
					i++;
					if (i == len) break;
					ch = s.charAt(i);
				    }
				    sawMethodRef = true;
				}
				if (i == len) break;
				ch = s.charAt(i);
			    }
			}
		    }
		    if (importStarted) {
			// change qualified names to strings
			String str = s.substring(start, i).trim()
			    .replaceAll(COMMENT_RE, "").replaceAll("\\s", "");
			level++;
			if (ptype != null && notBeforeString.contains(ptype)) {
			    String msg = errorMsg("syntaxError");
			    throw new ObjectParser.Exception (msg,
							      filenameTL.get(),
							      s, i);
			}
			next = new Token(Operator.STRING, str, offset+i, level);
			next.setValue(str);
			tokens.add(next);
			i--;
			break;
		    }
		    int ii = skipWhitespace(s, i, len, false);
		    char nch = (ii == len)? '\0': s.charAt(ii);
		    String mrc = sawMethodRef? s.substring(start, methodRefInd)
			    .replaceAll(COMMENT_RE, "")
			    .replaceAll("\\s", ""):
			null;
		    Class<?> mrclass = null;
		    if (sawMethodRef) {
			try {
			    mrclass = findClass(mrc);
			} catch (IllegalArgumentException eia) {
			    // Thrown by findClass if there is no class.
			    // In this case, we reset to just before
			    // processing the first colon in the double colon.
			    i = methodRefInd;
			    mrclass = null;
			}
		    }
		    if (sawMethodRef && mrclass != null) {
			// Method ref starting with a class name or a
			// typed null
			String methodName = s.substring(methodRefInd+2, i);
			methodName = methodName.replaceAll(COMMENT_RE, "")
			    .replaceAll("\\s", "");
			try {
			    // Class<?> clasz = findClass(c);
			    // mrclass = findClass(mrc);
			    level++;
			    boolean typedNull = methodName.equals("null");
			    next = new Token ((typedNull? Operator.TYPED_NULL:
					       Operator.METHOD_REF),
					      mrc + "::" + methodName,
					      offset+i, level);
			    if (typedNull) {
				next.setValue(new TypedNull(mrclass));
			    } else {
				next.setValue(processor
					      .createMethodRef(mrclass,
							       methodName));
			    }
			    tokens.add(next);
			    i--;
			    break;
			} catch (java.lang.Exception e) {
			    String msg = e.getMessage();
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
		    } else if (bracePeer != null && bracePeer.forMethod()
			       && firstdot != -1
			       && firstdot == lastdot && i != lastdot
			       && s.substring(start,firstdot).trim()
			       .equals("this")
			       && nch != '(') {
			//System.out.println("got here, nch = " + nch);
			// in a method body and saw this.VARIABLE,
			// which is treated as this["VARIABLE"].
			level++;
			prev = new Token(Operator.VARIABLE, "this",
					 offset + firstdot -1, level);
			tokens.add(prev);
			oldlevel = level;
			level += 1 + LEVEL_OFFSET;
			prev = new Token(Operator.OPAREN, "[", offset + i,
					 level);
			next = new Token(Operator.METHOD, "get", offset+i,
					 oldlevel);
			prev.setFunct(next);
			tokens.add(prev);
			String svalue = s.substring(lastdot+1, i).trim();
			prev = new Token(Operator.STRING, svalue,
					 offset+i, level+1);
			prev.setValue(svalue);
			tokens.add(prev);
			prev = new Token(Operator.CPAREN, "]", offset+i, level);
			prev.setFunct(next);
			prev.setArgCount(1);
			level -= (1 + LEVEL_OFFSET);
			tokens.add(prev);
			next = prev;
			i--;
			break;
		    } else if (firstdot != -1 && i != lastdot
			       && s.substring(lastdot+1, i).trim().
			       equals("class")) {
			String c = s.substring(start, lastdot);
			c = c.replaceAll(COMMENT_RE, "").replaceAll("\\s", "");
			int arrayDepth = 0;
			int j = skipWhitespace(s, i , len, false);
			while (j < len && s.charAt(j) == '[') {
			    j++;
			    j = skipWhitespace(s, j, len, true);
			    if (s.charAt(j) == ']') {
				arrayDepth++;
				j++;
				i = j;
				j = skipWhitespace(s, j, len, false);
			    }
			}
			try {
			    Class<?> clasz =
				(c.equals("double")? double.class:
				 c.equals("long")? long.class:
				 c.equals("int")? int.class:
				 c.equals("boolean")? boolean.class:
				 findClass(c));
			    if (arrayDepth > 0) {
				clasz = getArrayClass(clasz, arrayDepth);
			    }
			    level++;
			    next = new Token(Operator.CLASS,
					     c, offset+i, level);
			    next.setValue(clasz);
			    tokens.add(next);
			    i--; // because if the i++ in the 'for' loop
			} catch (Exception e) {
			    String msg = e.getMessage();
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
			break;
		    } else if (firstdot != -1 && lastdot == firstdot
			&& i != lastdot) {
			String v = s.substring(start, firstdot)
			    .replaceAll(COMMENT_RE, "").stripTrailing();
			if (v.equals("var")) {
			    // special case - test if a variable exists.
			    String tvar = s.substring(firstdot+1, i)
				.replaceAll(COMMENT_RE, "").trim();
			    level++;
			    next = new Token(Operator.EXIST_TEST, tvar,
					     offset+firstdot, level);
			    tokens.add(next);
			    i--; // because if the i++ in the 'for' loop
			    break;
			}
			boolean test1 =
			    usesMethods && !simpleClassNames.contains(v);
			boolean test2 =  exists(v) || /*vset.contains(v)*/
			    searchVsets(vsetStack, v)
			    || isParam(v, params, paramsStack);
			if (test1 || test2) {
			    if (test2) {
				// we found an object, a '.', and a method name.
				level++; // so we have to decr after each op
				if (prev != null) {
				    if (/*ptype == Operator.VARIABLE
					// || ptype == Operator.METHOD
					// || ptype == Operator.FUNCTION
					// || ptype == Operator.CONSTRUCTOR
					|| ptype == Operator.CPAREN*/
					notBeforeVars.contains(ptype)) {
					String  msg = errorMsg("syntaxError");
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, start);
				    }
				}
				next = new Token(Operator.VARIABLE, v,
						 offset + start,
						 level);
				tokens.add(next);
				prev = new Token(Operator.DOT, ".",
						 offset + firstdot, level);
				String m = s.substring(firstdot+1, i)
				    .replaceAll(COMMENT_RE,"").trim();
				next = new Token(Operator.METHOD, m,
						 offset+firstdot+1,
						 level);
				tokens.add(next);
				i--;
				break;
			    } else {
				String fname = findField(v);
				if (fname != null) {
				    // v was actually a constant. Some
				    // constants have methods, so we
				    // duplicate what we do with variables
				    // except for picking a constant instead
				    // to get the object.
				    level++;
				    next = new Token(Operator.CONSTANT, v,
						     offset+start, level);
				    next.setValue(cmap.get(fname));
				    tokens.add(next);
				    prev = new Token(Operator.DOT, ".",
						     offset + firstdot, level);
				    String m = s.substring(firstdot+1, i)
					.replaceAll(COMMENT_RE,"").trim();
				    next = new Token(Operator.METHOD, m,
						     offset+firstdot+1,
						     level);
				    tokens.add(next);
				    i--;
				    break;
				} else {
				    String msg = errorMsg("noValue", v);
				    int ind = (firstdot < 0)? i: firstdot;
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, ind);
				}
			    }
			}
		    }
		    String variable = s.substring(start, i)
			.replaceAll(COMMENT_RE, "").replaceAll("\\s", "");
		    if (prev != null && !variable.equals("instanceof")) {
			if (ptype == Operator.VARIABLE
			    // || ptype == Operator.METHOD
			    // || ptype == Operator.FUNCTION
			    // || ptype == Operator.CONSTRUCTOR
			    || ptype == Operator.CPAREN) {
			    String  msg = errorMsg("syntaxError");
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
		    }
		    if (variable.equals("function")) {
			level++; // so we have to decr after each operator
			next = new Token(Operator.FUNCTION_KEYWORD, variable,
					 offset+start, level);
			vset = new HashSet<String>();
			vsetStack.push(vset);
			functkwSeen = true;
			if (mustSync) {
			    next.sync();
			    mustSync = false;
			}
			if (processor.scriptingMode && bracePeer == null
			    && parenPeer == null && prev == null) {
			    // we found a top-level function definition.
			    i = skipWhitespace(s, i, len, true);
			    ch = s.charAt(i);
			    if (Character.isJavaIdentifierStart(ch)) {
				// function definition, not lambda expression.
				if (Character.isJavaIdentifierStart(ch)) {
				    int start2 = i;
				    while (Character.isJavaIdentifierPart(ch)) {
					i++;
					if (i == len) break;
					ch = s.charAt(i);
				    }
				    String fName = s.substring(start2,i);
				    if (functNames.contains(fName)) {
					String msg =
					    errorMsg("fnameInUse", fName);
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				    functNames.add(fName);
				    next.setValue(fName);
				} else {
				    // error
				    String tmp = s.substring(i, i+1);
				    String msg = errorMsg("notFunctName", tmp);
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
			    }
			}
			tokens.add(next);
		    } else if (variable.equals("new")) {
			next = new Token(Operator.NEW, variable,
					 offset+start, level);
		    } else if (variable.equals("var")) {
			int lasti = i;
			i = skipWhitespace(s, i, len, true);
			ch = s.charAt(i);
			if (processor.scriptingMode) {
			    if (ptype == null
				|| ptype == Operator.OBRACE
				|| ptype == Operator.SEMICOLON) {
				level = (bracePeer != null)?
				    bracePeer.getLevel(): 0;
				next = new Token(Operator.VAR, variable,
						 offset+start,
						 level + VAR_OFFSET);
				if (parenPeer != null &&
				    parenPeer.getType() != Operator.OBRACE) {
				    String msg = errorMsg("misplacedVar");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, lasti);
				}
				next.setBracePeer(bracePeer);
				if (varPeer != null) {
				    varPeers.push(varPeer);
				}
				varPeer = next;
				// i = skipWhitespace(s, i, len, true);
				// ch = s.charAt(i);
				boolean isdecl = false;
				boolean lastiter = false;
				boolean iscond = false;
				boolean incri = false;
				for (;;) {
				    if (Character.isJavaIdentifierStart(ch)) {
					start = i;
					while (Character
					       .isJavaIdentifierPart(ch)) {
					    i++;
					    if (i == len) {
						//input terminated unexpectedly
						String msg = errorMsg("varEQ");
						throw new ObjectParser.Exception
						    (msg, filenameTL.get(),
						     s, i);
					    }
					    ch = s.charAt(i);
					}
					String v = s.substring(start, i);
					if (v.equals("global")
					    || v.equals("this")) {
					    String msg =
						errorMsg("reservedIdent", v);
					    throw new ObjectParser.Exception
						(msg, filenameTL.get(), s,  i);
					}
					next.setValue(v);
					int j = skipWhitespace(s, i,
							       len, false);
					if (j < len) {
					    char nchar = s.charAt(j);
					    if (nchar == ';') {
						// last variable declaration
						isdecl = true;
						lastiter = true;
					    } else if (nchar == ',') {
						// variable delcaration
						isdecl = true;
					    } else if (isdecl == false
						       && (j < len - 3)
						       && (nchar == '?')
						       && s.charAt(j+1) == '?'
						       && s.charAt(j+2)=='=') {
						iscond = true;
						incri = true;
						next.changeType(Operator.QQVAR);
					    } else if (isdecl == false
						       && (j < len - 2)
						       && (nchar == '?')
						       && s.charAt(j+1)=='=') {
						iscond = true;
						next.changeType(Operator.QVAR);
					    }
					} else {
					    isdecl = true;
					    lastiter = true;
					}
					if (isdecl == false &&
					    iscond == false &&
					    (vset.contains(v)
					     || (vsetStack.size() == 1
						 && exists(v)))) {
					    // variable already defined.
					    String msg =
						errorMsg("alreadyDefined", v);
					    throw new ObjectParser.Exception
						(msg, filenameTL.get(), s, i);
					}
					vset.add(v);

					if (iscond) {
					    break;
					} else if (isdecl) {
					    i = j+1;
					    if (lastiter || i == len) break;
					    i = skipWhitespace(s, i,
							       len, false);
					    if (i < len) {
						ch = s.charAt(i);
					    } else {
						break;
					    }
					} else {
					    break;
					}
				    } else {
					String msg =
					    errorMsg("noIdentAfterVar");
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				}
				if (isdecl) {
				    // unwind - we were just adding entries
				    // to vset
				    if (varPeers.size() > 0) {
					varPeer = varPeers.pop();
				    } else {
					varPeer = null;
				    }
				    next = null;
				    nonNullPrev = null;
				    sawObjectProp = false;
				    continue;
				}
				i = skipWhitespace(s, i, len, true);
				ch = s.charAt(i);
				if (iscond) {
				    if (incri) i++;
				    i++;
				} else if (ch != '=') {
				    // an '=' was expected.
				    String msg = errorMsg("varEQ");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				i++;
				if (i == len) {
				    //input terminated unexpectedly
				    String msg = errorMsg("unterminatedVar");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				ch = s.charAt(i);
				switch(ch) {
				case '=':
				case '<':
				case '>':
				case '*':
				case ':':
				case '?':
				case '&':
				case '|':
				case '^':
				case '}':
				case ']':
				case ')':
				    String msg = errorMsg("varEQOP");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
			    } else {
				String msg = errorMsg("badIdent");
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, i);
			    }
			} else {
			    if (ptype == null
				|| ptype == Operator.OBRACE
				|| (ptype == Operator.SEMICOLON
				    && bracePeer != null)) {
				level = (bracePeer == null)? 0:
				    bracePeer.getLevel();
				next = new Token(Operator.VAR, variable,
						 offset+start,
						 level + VAR_OFFSET);
				if (parenPeer != null &&
				    parenPeer.getType() != Operator.OBRACE) {
				    String msg = errorMsg("misplacedVar");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, lasti);
				}
				next.setBracePeer(bracePeer);
				if (varPeer != null) {
				    varPeers.push(varPeer);
				}
				varPeer = next;
				// i = skipWhitespace(s, i, len, true);
				// ch = s.charAt(i);
				if (Character.isJavaIdentifierStart(ch)) {
				    start = i;
				    while (Character.isJavaIdentifierPart(ch)) {
					i++;
					if (i == len) {
					    //input terminated unexpectedly
					    String msg = errorMsg("varEQ");
					    throw new ObjectParser.Exception
						(msg, filenameTL.get(), s, i);
					}
					ch = s.charAt(i);
				    }
				    // next.setValue(s.substring(start, i));
				    String v = s.substring(start, i);
				    if (v.equals("global")
					|| v.equals("this")) {
					String msg =
					    errorMsg("reservedIdent", v);
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				    next.setValue(v);
				} else {
				    String msg = errorMsg("noIdentAfterVar");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				i = skipWhitespace(s, i, len, true);
				ch = s.charAt(i);
				if (ch != '=') {
				    // an '=' was expected.
				    String msg = errorMsg("varEQ");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				i++;
				if (i == len) {
				    //input terminated unexpectedly
				    String msg = errorMsg("unterminatedVar");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
				ch = s.charAt(i);
				switch(ch) {
				case '=':
				case '<':
				case '>':
				case '*':
				case ':':
				case '?':
				case '&':
				case '|':
				case '^':
				case '}':
				case ']':
				case ')':
				    String msg = errorMsg("varEQOP");
				    throw new ObjectParser.Exception
					(msg, filenameTL.get(), s, i);
				}
			    } else {
				String msg = errorMsg("badIdent");
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, i);
			    }
			}
			tokens.add(next);
		    } else if (variable.equals("null")) {
			level++; // so we have to decr after each operator
			next = new Token(Operator.NULL, "null",
					 offset+start, level);
			next.setValue(NULL);
			tokens.add(next);
		    } else if (variable.equals("void")) {
			level++; // so we have to decr after each operator
			next = new Token(Operator.VOID, "void",
					 offset+start, level);
			next.setValue(VOID);
			tokens.add(next);
		    } else if (variable.equals("instanceof")) {
			prev = new Token(Operator.INSTANCEOF, variable,
					 offset+start,
					 level+RELEQ_OFFSET);
			level--; // check this - copied from "<=" case
			tokens.add(prev);
			StringBuffer sb = new StringBuffer();
			i = skipWhitespace(s, i, len, true);
			ch = s.charAt(i);
			int lasti = i;
			while (Character.isJavaIdentifierStart(ch)) {
			    int strt = i;
			    lasti = i;
			    i++;
			    if (i == len) break;
			    ch  = s.charAt(i);
			    while (i < len &&
				   Character.isJavaIdentifierPart(ch)) {
				lasti = i;
				i++;
				if (i == len) break;
				ch = s.charAt(i);
			    }
			    sb.append(s.substring(strt, i));
			    i = skipWhitespace(s, i, len, false);
			    if (i == len) break;
			    ch = s.charAt(i);
			    if (ch == '.') {
				sb.append('.');
				lasti = i;
				i++;
				if (i == len) break;
				i = skipWhitespace(s, i, len, false);
				if (i == len) break;
				ch = s.charAt(i);
			    }
			}
			if (s.charAt(lasti) == '.') {
			    String cn = sb.toString();
			    String msg = errorMsg("illformedClassName", cn);
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, lasti);
			}
			if (sb.length() > 0) {
			    String className = sb.toString();
			    level++;
			    next = new Token(Operator.STRING, className,
					     offset + i, level);
			    next.setValue(className);
			    tokens.add(next);
			} else {
			    String msg = errorMsg("missingClassName");
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, i);
			}
		    } else if (variable.equals("true")) {
			level++; // so we have to decr after each operator
			next = new Token(Operator.BOOLEAN, "true",
					 offset+start, level);
			next.setValue(Boolean.TRUE);
			tokens.add(next);
		    } else if (variable.equals("false")) {
			level++; // so we have to decr after each operator
			next = new Token(Operator.BOOLEAN, "false",
					 offset+start, level);
			next.setValue(Boolean.FALSE);
			tokens.add(next);
		    } else if (variable.equals("throw")) {
			level--;
			// we want this operator to have a precedence
			// just below '+'
			if (prev != null) {
			    switch(ptype) {
			    case SEMICOLON:
			    case OPAREN:
			    case OBRACE:
			    case QMARK:
			    case QVAR:
			    case QQVAR:
			    case COLON:
			    case LOR:
			    case LAND:
				break;
			    default:
				String msg = errorMsg("misplacedThrow");
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, i);
			    }
			}
			next = new Token(Operator.THROW, "throw",
					 offset+start, level-1);
			tokens.add(next);
			break;
		    } else if (level == 0 && prev == null
			       && processor.scriptingMode
			       && scriptImportAllowed
			       && importsFrozen == false
			       && variable.equals("import")) {
			level++;
			next = new Token(Operator.IMPORT, variable,
					 offset+start, level);
			tokens.add(next);
			importStarted = true;
			finishNeeded = true;
			// Setting importParen to true results in code
			// immediately before the 'switch' statement above
			// being executed, and that code checks for the
			// opening parenthesis.
			importParen = true;

		    } else if (variable.equals("import")) {
			// imports not allowed / 'import' is a reserved word.
			String msg;
			if (importsFrozen) {
			    msg = errorMsg("importBlocked");
			} else {
			    msg = errorMsg("importReserved");
			}
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, i);
		    } else if (variable.equals("synchronized")) {
			mustSync = true;
			i--;	  // due to 'for' loop
			nonNullPrev = null;
			sawObjectProp = false;
			continue; // special case - we just set a flag
		    } else {
			boolean varExists = exists(variable)
			    || searchVsets(vsetStack,variable);
			boolean maybeCall = maybeCall(s, i, len);
			String fname = (varExists || maybeCall)? null:
			    findField(variable);
			if (!varExists && maybeCall) {
			    //check for calling a constant's method.
			    int index = variable.lastIndexOf('.');
			    if (index != -1) {
				String constant = variable.substring(0, index);
				if (constant.endsWith(".class")) {
				    String c = constant.substring
					(0, constant.length() - 6);
				    String method = variable.substring(index+1)
					.replaceAll(COMMENT_RE,"").trim();
				    /*
				    System.out.println(constant);
				    System.out.println(method);
				    */
				    try {
					Class<?> clasz =
					    (c.equals("double")? double.class:
					     c.equals("long")? long.class:
					     c.equals("int")? int.class:
					     c.equals("boolean")? boolean.class:
					     findClass(c));
					level++;
					next = new Token(Operator.CLASS,
							 c, offset+i, level);
					next.setValue(clasz);
					tokens.add(next);
					prev = new Token(Operator.DOT, ".",
							 offset+start+index,
							 level);
					next = new Token(Operator.METHOD,
							 method,
							 offset+start+index+1,
							 level);
					tokens.add(next);
					i--;
					break;
				    } catch (Exception e) {
					String msg = e.getMessage();
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				}
				fname = findField(constant);
				if (fname != null) {
				    String method = variable.substring(index+1)
					.replaceAll(COMMENT_RE,"").trim();
				    if (method.length() == 0) method = null;
				    if (method != null) {
					level++;
					next = new Token(Operator.CONSTANT,
							 constant,
							 offset+start, level);
					next.setValue(cmap.get(fname));
					tokens.add(next);
					prev = new Token(Operator.DOT, ".",
							 offset+start+index,
							 level);
					next = new Token(Operator.METHOD,
							 method,
							 offset+start+index+1,
							 level);
					tokens.add(next);
					i--;
					break;
				    }
				}
			    }
			}
			if (fname != null) {
			    level++; // so we have to decr after each operator
			    next = new Token(Operator.CONSTANT, variable,
					     offset+start, level);
			    next.setValue(cmap.get(fname));
			} else if (prev != null
				   && ptype == Operator.DOT) {
			    next = new Token(Operator.METHOD, variable,
					     offset+start, level);
			} else if (prev != null
				   && ptype == Operator.NEW) {
			    level++; // so we have to decr after each operator
			    next = new Token(Operator.CONSTRUCTOR, variable,
					     offset+start, level);
			} else {
			    if (ptype!=null && notBeforeVars.contains(ptype)) {
				String msg = errorMsg("syntaxError");
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, i);
			    }
			    level++; // so we have to decr after each operator
			    next = new Token((functParams? Operator.PARAMETER:
					      Operator.VARIABLE),
					     variable, offset+start, level);
			    if (ptype == Operator.SWAP) {
				next.changeType(Operator.VARIABLE_NAME);
			    }
			    if (functkwSeen) {
				params.add(variable);
				vset.add(variable);
			    } else if (!maybeCall) {
				if (!varExists) {
				    if (processor.ambiguousNames.get()
					== null) {
					String msg =
					    errorMsg("noValue", variable);
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    } else {
					String msg =
					    errorMsg("noValue2", variable);
					for (String nm:
						 processor.ambiguousNames
						 .get()) {
					    msg += LINE_SEP + "--- "
						+ nm;
					}
					throw new ObjectParser.Exception
					    (msg, filenameTL.get(), s, i);
				    }
				}
			    }
			}
			tokens.add(next);
		    }
		    i--;	// because of for loop
		} else {
		    // Illegal character in input.
		    String msg = errorMsg("badIdent");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, i);
		}
		break;
	    }
	    sawObjectProp = false;
	    nonNullPrev = null;
	    prev = next;
	    next = null;
	}
	if (condPeer != null) {
	    String msg = errorMsg("missingColon");
	    throw new ObjectParser.Exception(msg, filenameTL.get(), s, len-1);
	} else if (bracketPeer != null) {
	    String msg = errorMsg("unbalancedBrackets");
	    throw new ObjectParser.Exception(msg, filenameTL.get(), s, len-1);
	} else if (bracePeer != null) {
	    String msg = errorMsg("unbalancedBraces");
	    throw new ObjectParser.Exception(msg, filenameTL.get(), s, len-1);
	} else if (parenPeer != null) {
	    String msg = errorMsg("unbalancedParens");
	    throw new ObjectParser.Exception(msg, filenameTL.get(), s, len-1);
	}
	if (finishNeeded) {
	    next = new Token(Operator.FINISH_IMPORT,
			     ((prev == null)? "<EOF>": prev.getName()),
			     offset+len-1,
			     ((prev == null)? level: prev.getLevel()));
	    tokens.add(next);
	}
	if (firstQMark != null) {
	    firstQMark.setQmarkIndex(qmarkIndex);
	}
	if (tarray != null) {
	    tarray.add(tokens);
	    tokens = null;
	}
	return tokens;
    }

    private Object parseExpression(String s, int offset, String orig,
				   boolean parseOnly)
    {
	// processor.clear();	// in case of a previous failure.
	processor.ambiguousNames.set(null);
	tokenTracing.set(false);
	processor.stackTracing.set(false);
	if (parseOnly) {
	    tokenize(s, offset, null);
	    // The return value will be ignored: matches will return
	    // true if there is no exception, false otherwise
	    return null;
	}
	try {
	    processor.pushCallContext();
	    LinkedList<Token> tokens = tokenize(s, offset, null);
	    Token first = tokens.peek();
	    if (first != null) {
		if (first.getType() == Operator.PLUS) {
		    // a leading '+' is ignored.  We don't generate an
		    // error because YAML allows a leading '+' in a number.
		    tokens.poll();
		}
	    }
	    parseExpression(tokens, orig);
	    processor.eval(orig);
	    return processor.getResult();
	} finally {
	    processor.popCallContext();
	}
    }

    private static void removeAfterColon(ListIterator<Token> iter,
					 int ourlevel) {
	int parenCnt = 0;
	boolean first = true;
	boolean done = false;
	while (iter.hasNext()) {
	    // sequence equivalent to peek
	    Token token = iter.next();
	    iter.previous();
	    switch(token.getType()) {
	    case OPAREN:
		parenCnt++;
		break;
	    case CPAREN:
		parenCnt--;
		if (parenCnt < 0) {
		    done = true;
		}
		break;
	    case COMMA:
		if (parenCnt == 0) {
		    done = true;
		}
	    }
	    if (done) break;
	    if (first) {
		first = false;
	    } else if (token.getLevel() < ourlevel) {
		break;
	    }
	    token = iter.next();
	}
    }

    static void skipFunction(ListIterator<Token> iter) {
	// started while iter points to a token with type
	// Operator.FUNCTION_KEYWORD
	int braceCount = 0;
	Token bpeer = null;
	while (iter.hasNext()) {
	    Token token = iter.next();
	    if (bpeer == null)  {
		if (token.getType() == Operator.OBRACE) {
		    bpeer = token;
		}
	    } else {
		if (token.getType() == Operator.CBRACE
		    && token.getBracePeer() == bpeer) {
		    break;
		}
	    }
	}
    }

    // used with some directives to selectively print part of the
    // token stream onto standard error for debugging.

    private ThreadLocal<Boolean>tokenTracing =
	new ThreadLocal<Boolean>() {
	    @Override protected Boolean initialValue() {
		return Boolean.FALSE;
	    }
	};

    private void  parseExpression(LinkedList<Token> tokens, String s)
    {
	Token token;
	ListIterator<Token> iter = tokens.listIterator(0);
	Operator skipType = null;
	int skipLevel = 0;	// used only when skipType is not null.
	boolean[] qmarkFence = null;
	Token finalToken = null;
	java.util.Iterator<Token> reverseIter =tokens.descendingIterator();
	boolean tokTracing = tokenTracing.get();
	while (reverseIter.hasNext()) {
	    // to eliminate any trailing semicolons as
	    // processing these results in a null value being returned
	    // and with Java programming, it is easy to be in the habit
	    // of typing one.
	    Token t = reverseIter.next();
	    if (t.getType() == Operator.SEMICOLON) {
		finalToken = t;
	    } else {
		break;
	    }
	}
	while (iter.hasNext()) {
	    token = iter.next();
	    if (token == finalToken) break;
	    int level = token.getLevel();
	    if (skipType != null) {
		// for  '&&' and '||', which won't evaluate the right-hand
		// term if the left-hand term is false or true respectively.
		// we have ">=" instead of ">" because of the order of
		// evaluation when precedents are the same.
		if (skipLevel <= level) {
		    if (false || tokTracing) {
			if (tokTracing) {
			    System.err
				.format("... skipping TOKEN %s (\"%s\"): "
					+ "level=%s\n",
					token.getType(), token.getName(),
					level);
			} else {
			    System.out.println("... skipping token "
					       + token.getType() +
					       ", level = " + level
					       + ", name = " + token.getName()
					       + ", value = "
					       + token.getValue());
			}
		    }
		    continue;
		} else {
		    skipType = null;
		}
	    }
	    Operator ttype = token.getType();
	    if (false || tokTracing) {
		if (tokTracing) {
		    if (traceValueSet.contains(ttype)
			&& token.getValue() != null) {
			System.err.format("TOKEN %s (\"%s %s\"): level=%s\n",
					  token.getType(),
					  token.getName(), token.getValue(),
					  level);
		    } else {
			System.err.format("TOKEN %s (\"%s\"): level=%s\n",
					  token.getType(), token.getName(),
					  level);
		    }
		} else {
		    System.out.println("... token " + token.getType() +
				   ", level = " + level
				   + ", name = " + token.getName()
				   + ", value = " + token.getValue()
				   + ", funct-type = "
				   + (token.getFunct() == null? null:
				      token.getFunct().getType())
				   + ", funct-name = "
				   + (token.getFunct() == null? null:
				      token.getFunct().getName())
				   + ", forMethod = " + token.forMethod()
				   );
		}
	    }
	    switch (ttype) {
	    case START_TOKEN_TRACING:
		if (tokTracing == false) {
		    System.err.format("TOKEN %s (\"%s\"): level=%s\n",
				      ttype, token.getName(), level);
		}
		tokTracing = true;
		tokenTracing.set(tokTracing);
		continue;
	    case STOP_TOKEN_TRACING:
		tokTracing = false;
		tokenTracing.set(tokTracing);
		continue;
	    case START_STACK_TRACING:
		processor.stackTracing.set(true);
		continue;
	    case STOP_STACK_TRACING:
		processor.stackTracing.set(false);
		continue;
	    case NOOP:
		continue;
	    case FUNCTION_KEYWORD:
		if (iter.hasNext()) {
		    boolean forMethod = token.forMethod();
		    String functName = (String)token.getValue();
		    boolean willSync = token.willSync();
		    token = iter.next();
		    if (token.getType() != Operator.OPAREN) {
			int ind = token.getIndex();
			String msg =
			    errorMsg("expectingOpenParen", token.getName());
			throw new ObjectParser.Exception
			    (msg, token.getFileName(), s, ind);

		    }
		    int acnt = token.getArgCount();
		    String[] formalParameters = new String[acnt];
		    for (int i = 0; i < formalParameters.length; i++) {
			if (!iter.hasNext()) {
			    String msg = errorMsg("missingLambdaParams");
			    throw new ObjectParser.Exception
				(msg, token.getFileName(), s, token.getIndex());
			}
			token = iter.next();
			formalParameters[i] = token.getName();
			if (!iter.hasNext()) {
			    String msg = errorMsg("missingLambdaParams");
			    throw new ObjectParser.Exception
				(msg, token.getFileName(), s, token.getIndex());
			}
			token = iter.next();
		    }
		    if (acnt == 0 && iter.hasNext()) {
			token = iter.next();
		    }
		    if (!iter.hasNext()) {
			int ind = token.getIndex();
			String msg = errorMsg("missingLambda");
			throw new ObjectParser.Exception
			    (msg, token.getFileName(), s, ind);
		    }
		    Token bpeer = iter.next();
		    ArrayList<LinkedList<Token>> tarray = new ArrayList<>();
		    LinkedList<Token> lst = new LinkedList<>();
		    for (;;) {
			if (!iter.hasNext()) {
			    // not terminated
			    int ind = token.getIndex();
			    String msg = errorMsg("missingCBrace");
			    throw new ObjectParser.Exception
				(msg, token.getFileName(), s, ind);
			}
			token = iter.next();
			if (token.getType() == Operator.SEMICOLON
			    && token.getBracePeer() == bpeer) {
			    if (lst.size() > 0) {
				tarray.add(lst);
				lst = new LinkedList<Token>();
			    }
			} else if (token.getType() == Operator.CBRACE
				   && token.getBracePeer() == bpeer) {
			    if (lst.size() > 0) {
				tarray.add(lst);
				lst = new LinkedList<>();
			    }
			    break;
			} else {
			    lst.add(token);
			}
		    }
		    ESPFunction lambda = new
			ESPFunction(formalParameters, s, tarray);
		    if (willSync) {
			lambda.willSync();
		    }
		    int sz = tarray.size();
		    if (sz > 0) {
			LinkedList<Token> lastList = tarray.get(sz-1);
			Token last = lastList.getLast();
			if (last.getType() == Operator.VOID) {
			    lambda.setAsVoid();
			}
		    }

		    if (functName != null) {
			// top-level function name - so just insert the
			// function into the table.
			vmap.get().put(functName, lambda);
		    } else {
			if (forMethod) lambda.setAsMethod();
			processor.pushValue(lambda);
		    }
		} else {
		    String msg = errorMsg("missingLambda");
		    throw new ObjectParser.Exception
			(msg, token.getFileName(), s, s.length()-1);
		}
		break;
	    case OBRACKET:
		processor.pushValue(new ESPArray());
		break;
	    case OBJOPENBRACE:
		processor.pushValue(new ESPObject());
		break;
	    case EXIST_TEST:
		processor.pushValue(token.variableExists());
		break;
	    case SEMICOLON:
	    case VAR:
	    case UNARY_MINUS:
	    case BINARY_MINUS:
	    case PLUS:
	    case TIMES:
	    case DIVIDEBY:
	    case MOD:
	    case MATH_MOD:
	    case LSHIFT:
	    case RSHIFT:
	    case URSHIFT:
	    case NOT:
	    case AND:
	    case OR:
	    case XOR:
	    case GT:
	    case GE:
	    case LT:
	    case LE:
	    case INSTANCEOF:
	    case EQ:
	    case NE:
	    case LNOT:
	    case THROW:
	    case ASSIGN1:
	    case ASSIGN2:
	    case SWAP:
		processor.pushOp(token, s);
		break;
	    case LOR:
	    case LAND:
		processor.pushOp(token, s);
		if (processor.valueStackEmpty()) {
		    String tname = token.getName();
		    String msg = errorMsg("noValueBeforeOp", tname);
		    throw new ObjectParser.Exception
			(msg, token.getFileName(), s, token.getIndex());
		} else {
		    Object o = processor.peekValue();
		    boolean toskipVal = (ttype == Operator.LOR);
		    if (o != null && o instanceof Boolean
			&& ((Boolean) o) == toskipVal) {
			// the value on the stack is false so we
			// can skip the next term - tokens whose
			// levels are larger than this token's level.
			processor.popOp();
			skipType = token.getType();
			skipLevel = token.getLevel();
		    }
		}

		break;
	    case OPAREN:
		{
		}
		break;
	    case CPAREN:
		{
		    Token ftoken = token.getFunct();
		    if (ftoken != null) {
			if (ftoken.getType() != Operator.IMPORT) {
			    Object value = getInternal(ftoken.getName());
			    if (value instanceof ESPFunction) {
				ESPFunction fn = (ESPFunction) value;
				int n1 = fn.args.length;
				int n2 = token.getArgCount();
				if (n1 != n2) {
				    int ind = token.getIndex();
				    String msg =
					errorMsg("wrongArgCnt", fn, n1, n2);
				    throw new ObjectParser.Exception
					(msg, token.getFileName(), s, ind);
				}
				ftoken.setValueTL(value);
			    } else {
				ftoken.setValueTL(null);
			    }
			}
			processor.pushOp(token, s);
		    }
		}
		break;
	    case FINISH_IMPORT:
		processor.pushOp(token, s);
		break;
	    case QVAR:
		if (token.variableExistsQ()) {
		    // we have to remove the tokens after a QVAR if the
		    // variable exists.  This is basically the same code as
		    // for the COLON case.
		    int ourlevel = token.getLevel();
		    removeAfterColon(iter, ourlevel);
		    processor.pushValue(token.get());
		} else {
		    processor.pushOp(token, s);
		}
		break;
	    case QQVAR:
		if (token.variableExistsQQ()) {
		    // we have to remove the tokens after a QQVAR if the
		    // variable exists or is null./ This is basically the
		    // same code as for the COLON case.
		    int ourlevel = token.getLevel();
		    removeAfterColon(iter, ourlevel);
		    processor.pushValue(token.get());
		} else {
		    processor.pushOp(token, s);
		}
		break;
	    case QMARK:
		{
		    if (qmarkFence == null) {
			if (token.getQmarkIndex() == -1) {
			    String msg = errorMsg("qmarkIndex");
			    int ind = token.getIndex();
			    throw new ObjectParser.Exception
				(msg, token.getFileName(), s, ind);
			}
			qmarkFence = new boolean[token.getQmarkIndex()];
		    } else {
			int qmi = token.getQmarkIndex();
			if (0 <= qmi) {
			    if (qmarkFence.length < qmi) {
				qmarkFence = new boolean[qmi];
			    } else {
				Arrays.fill(qmarkFence, 0, qmi, false);
			    }
			}
		    }
		    processor.pushOp(token, s);
		    Object val = processor.popValue();
		    Token colonToken = token.getCondPeer();
		    // colonToken.clearQmarkFence();
		    if (val instanceof Boolean) {
			Boolean bval = (Boolean) val;
			if (bval.booleanValue() == false) {
			    int ourlevel = token.getLevel();
			    Token ourToken = token;
			    while (iter.hasNext()) {
				token = iter.next();
				if (token == colonToken) {
				    break;
				} else if (token.getType()
					   == Operator.FUNCTION_KEYWORD) {
				    skipFunction(iter);
				} else if (token.getLevel() < ourlevel) {
				    int ind = token.getIndex();
				    String msg = errorMsg("missingColon");
				    throw new ObjectParser.Exception
					(msg, token.getFileName(), s, ind);
				}
			    }
			    if (tokens.size() == 0) {
				// missing 'else'
				String msg = errorMsg("noColon");
				throw new ObjectParser.Exception
				    (msg, token.getFileName(), s,
				     token.getIndex());
			    }
			} else {
			    // indicates we should delete.
			    // colonToken.setQmarkFence();
			    qmarkFence[colonToken.getQmarkIndex()] = true;
			}
		    } else {
			int ind = token.getIndex();
			String msg = errorMsg("notBooleanBeforeQM");
			throw new ObjectParser.Exception
			    (msg, token.getFileName(), s, ind);
		    }
		}
		break;
	    case COLON:
		if (qmarkFence[token.getQmarkIndex()]) {
		    // If we see this, the QMARK case didn't remove it so
		    // the value must have been 'true' and we have to remove
		    // tokens after the colon.
		    int ourlevel = token.getLevel();
		    removeAfterColon(iter, ourlevel);
		    /*
		    int parenCnt = 0;
		    boolean first = true;
		    boolean done = false;
		    while (iter.hasNext()) {
			// sequence equivalent to peek
			token = iter.next();
			iter.previous();
			switch(token.getType()) {
			case OPAREN:
			    parenCnt++;
			    break;
			case CPAREN:
			    parenCnt--;
			    if (parenCnt < 0) {
				done = true;
			    }
			    break;
			case COMMA:
			    if (parenCnt == 0) {
				done = true;
			    }
			}
			if (done) break;
			if (first) {
			    first = false;
			} else if (token.getLevel() < ourlevel) {
			    break;
			}
			token = iter.next();
		    }
		    */
		} else {
		    int ind = token.getIndex();
		    String msg = errorMsg("badColon");
		    throw new ObjectParser.Exception
			(msg, token.getFileName(), s, ind);
		}
		break;
	    case CBRACKET:
	    case OBJCLOSEBRACE:
	    case COMMA:
	    case ACTIVE_COMMA:
		{
		    processor.pushOp(token, s);
		}
		break;
	    case UNARY_PLUS:
	    case METHOD:
	    case NEW:
	    case CONSTRUCTOR:
	    case DOT:
	    case LEAVE_ON_STACK:
		break;
	    case FUNCTION:
		break;
	    case VARIABLE_NAME:
		processor.pushValue(token.getName());
		break;
	    case VARIABLE:
		{
		    String name = token.getName();
		    Object value = token.get(name);
		    if (value == null && !token.containsKey(name)) {
			int ind = token.getIndex();
			String msg = errorMsg("noValue", token.getName());
			throw new ObjectParser.Exception
			    (msg, token.getFileName(), s, ind);
		    }
		    processor.pushValue(value);
		}
		break;
	    case CONSTANT:
	    case STRING:
	    case BOOLEAN:
	    case NUMBER:
	    case TYPED_NULL:
	    case CLASS:
		{
		    processor.pushValue(token.getValue());
		}
		break;
	    case METHOD_REF:
		{
		    Object obj = token.getValue();
		    if (obj instanceof String) {
			// When the object is a string, an ESPMethodRef
			// has not yet been created. Pushing the operation
			// at this point will cause enough preceding operations
			// to be performed so that a target object will
			// appear on the stack, followed by the method name
			// that is pushed after this operation. Then, when
			// the METHOD_REF token is evaluated, we can create
			// the actual method reference.
			processor.pushOp(token, s);
		    }
		    processor.pushValue(obj);
		}
		break;
	    case VOID:
		processor.pushValue(VOID);
		break;
	    case NULL:
		{
		    processor.pushValue(NULL);
		}
		break;
	    default:
		// unexpected token - should not occur
	    }
	}
	return;
    }

    private static String LINE_SEP_STR = "" + SEP_END;

    private static Pattern WS_PATTERN = Pattern.compile
	("(\\p{Space}|(//.*" + Pattern.quote(LINE_SEP_STR) +"))+");
    private static Pattern VAR_PATTERN = Pattern.compile
	("var(\\p{Space}|(//.*" + Pattern.quote(LINE_SEP_STR) +"))+");
    private static Pattern SYNCHRONIZED_PATTERN = Pattern.compile
	("synchronized(\\p{Space}|(//.*" + Pattern.quote(LINE_SEP_STR) +"))+");
    private static Pattern FUNCTION_PATTERN = Pattern.compile
	("function(\\p{Space}|(//.*" + Pattern.quote(LINE_SEP_STR) +"))+");
    private static Pattern ARG_PATTERN = Pattern.compile
	("[_$\\p{IsAlphabetic}][_$\\p{IsAlphabetic}\\p{Digit}]*");
    
    @Override
    public boolean matches(String s) {
	Object ourGlobal = vmap.get().get("global");
	if (processor.epSingleton != null
	    && ourGlobal != processor.epSingleton) {
	    vmap.get().put("global", processor.epSingleton);
	}
	try {
	    parse(s, true);
	    return true;
	} catch (Exception e) {
	    return false;
	} finally {
	    if (processor.epSingleton != null
		&& ourGlobal != processor.epSingleton) {
		vmap.get().put("global", ourGlobal);
	    }
	}
    }

    /**
     * Set the bindings for this parser.
     * Bindings are maps that assign objects to variable names
     * for variables defined outside a function or object.
     * The map should be a synchronized map if an application is
     * multithreaded.
     * @param bindings the binding
     */
    public void setBindings(Map<String,Object>bindings) {
	vmapBinding.set(bindings);
    }

    /**
     * Set the bindings for this parser.
     * Bindings are maps that assign objects to variable names.
     * @return the bindings
     */
    public Map<String,Object> getBindings() {
	return vmapBinding.get();
    }

    @Override
    public Object parse(String s) throws Exception {
	Object ourGlobal = vmap.get().get("global");
	if (processor.epSingleton != null
	    && ourGlobal != processor.epSingleton) {
	    vmap.get().put("global", processor.epSingleton);
	}
	try {
	    return parse(s, false);
	} finally {
	    if (processor.epSingleton != null
		&& ourGlobal != processor.epSingleton) {
		vmap.get().put("global", ourGlobal);
	    }
	}
    }

    /**
     * Parse a string and return the corresponding object.
     * The filename argument is simply a string that will be used as
     * a label to denote the source of the first argument, and will
     * nearly always be a file name.
     * @param s the string
     * @param filename  a name to label the first argument
     * @return the corresponding object
     * @exception ObjectParser.Exception if the string could not
     *            be successfully parsed or the object could not be created
     */
    public Object parse(String s, String filename) {
	String prevFileName = filenameTL.get();
	try {
	    filenameTL.set(filename);
	    return parse(s);
	} finally {
	    filenameTL.set(prevFileName);
	}
    }

    /**
     * Parse a string and return the corresponding object.
     * The filename argument is simply a string that will be used as
     * a label to denote the source of the first argument, and will
     * nearly always be a file name.
     * @param s the string
     * @param filename  a name to label the first argument
     * @param bindings a map assigning objects to variable names
     * @return the corresponding object
     * @exception Exception if the string could not be successfully
     *            parsed or the object could not be created
     */
    public Object parse(String s, String filename, Map<String,Object> bindings)
    {
	String prevFileName = filenameTL.get();
	try {
	    filenameTL.set(filename);
	    return parse(s, bindings);
	} finally {
	    filenameTL.set(prevFileName);
	}
    }


    /**
     * Parse a string and return the corresponding object
     * @param s the string
     * @param bindings a map assigning objects to variable names
     * @return the corresponding object
     * @exception Exception if the string could not be successfully
     *            parsed or the object could not be created
     */
    public Object parse(String s, Map<String,Object> bindings)
	throws Exception
    {
	if (bindings == null) {
	    throw new NullPointerException(errorMsg("noBindings"));
	}
	Object global = null;
	boolean hasGlobal = false;
	try {
	    vmap.set(bindings);
	    if (processor.epSingleton != null) {
		hasGlobal = bindings.containsKey("global");
		global = bindings.put("global", processor.epSingleton);
	    }
	    return parse(s);
	} finally {
	    vmap.set(null);
	    if (processor.epSingleton != null) {
		if (hasGlobal) {
		    bindings.put("global", global);
		} else {
		    bindings.remove("global");
		}
	    }
	}
    }

    boolean noPrefixMode = true;

    /**
     * Set prefix mode.
     * Prefix mode requires that a script start with an '=' or the
     * keywords "var" "function", or "synchronized" followed by "function".
     * This mode does not have to be set unless scripting mode is enabled.
     */
    public void setPrefixMode() {
	noPrefixMode = false;
    }

    private Object parse(String s, boolean parseOnly) throws Exception {
	vsetThreadLocal.get().clear();
	functNamesThreadLocal.get().clear();
	Thread currentThread = Thread.currentThread();
	Thread lastThread = null;
	if (processor.scriptingMode && noPrefixMode) {
	    // in scripting mode, there is no special processing.
	    try {
		lastThread = importThreadRef
		    .compareAndExchangeAcquire(null, currentThread);
		threadCount.addAndGet(1);
		if (noImport == false &&
		    lastThread != currentThread
		    && lastThread != null) {
		    synchronized (ExpressionParser.this) {
			noImport = true;
		    }
		}
		Object result = parseExpression(s, 0, s, parseOnly);
		return (result instanceof TypedNull)? null: result;
	    } finally {
		if (threadCount.addAndGet(-1) == 0) {
		    importThreadRef
			.compareAndExchangeRelease(currentThread, null);
		} else {
		    synchronized (ExpressionParser.this) {
			if (noImport) {
			    // no longer need it.
			    importThreadRef.setRelease(null);
			}
		    }
		}
	    }
	}
	int offset = 0;
	String orig = s;

	Matcher matcher = WS_PATTERN.matcher(s);
	if (matcher.lookingAt()) {
	    offset += matcher.end();
	    s = s.substring(offset);
	}
	matcher = VAR_PATTERN.matcher(s);
        if (matcher.lookingAt()) {
            int incr = matcher.end();
            offset += incr;
            s = s.substring(incr);
            int index = 0;
            if (Character.isJavaIdentifierStart(s.charAt(index))) {
                index++;
                int len = s.length();
                while (index < len
                       && Character.isJavaIdentifierPart(s.charAt(index))) {
                    index++;
                }
                String vname = s.substring(0,index);
                if (simpleClassNames.contains(vname)) {
                    String msg = errorMsg("vnameWasClassname", vname);
                    throw new IllegalArgumentException(msg);
                }
		String savedS = s;
		int savedIndex = index;
                offset += index;
                s = s.substring(index);
                matcher = WS_PATTERN.matcher(s);
                if (matcher.lookingAt()) {
                    incr = matcher.end();
                    offset += incr;
                    s = s.substring(incr);
                }
		boolean iscond2 = (s.length() > 2 &&
				   s.charAt(0) == '?' && s.charAt(1) == '?'
				   && s.charAt(2) == '=');
		boolean iscond = (s.length() > 1 &&
				  s.charAt(0) == '?' && s.charAt(1) == '=');
                if (s.length() == 0 || (s.charAt(0) != '='
					&& !(iscond || iscond2))) {
                    String msg = errorMsg("varEq");
                    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, 0);
                }
		if (iscond2) {
		    if (exists(vname)) {
			Object val = get(vname);
			if (val != null) {
			    return val;
			}
		    }
		} else if (iscond) {
		    // if we are not in scripting mode, nothing can follow
		    // the first top-level semicolon. As a result, we
		    // can simply ignore the rest of the expression and
		    // return the current value.
		    if (exists(vname)) {
			return get(vname);
		    }
		} else {
		    // We delayed this test so we could check for the
		    // '?=' case.
		    if (exists(vname)) {
			String msg = errorMsg("alreadyDefined", vname);
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), savedS, savedIndex);
		    }
		    savedS = null;
		}
                s = s.substring(iscond2? 3: iscond? 2: 1);
                offset++;
                Object value = parseExpression(s, offset, orig, parseOnly);
                vmap.get().put(vname, value);
		return (value instanceof TypedNull)? null: value;
            } else {
                String msg = errorMsg("notVarIdent");
                throw new ObjectParser.Exception
		    (msg, filenameTL.get(), s, 0);
            }
        }
	boolean synchronizedFunction = false;
	matcher = SYNCHRONIZED_PATTERN.matcher(s);
	if (matcher.lookingAt()) {
	    int incr = matcher.end();
	    offset = incr;
	    s = s.substring(incr);
	    synchronizedFunction = true;
	}
	matcher = FUNCTION_PATTERN.matcher(s);
	if (matcher.lookingAt()) {
	    int incr = matcher.end();
	    offset += incr;
	    s = s.substring(incr);
	    int index = 0;
	    matcher = WS_PATTERN.matcher(s);
	    if (matcher.lookingAt()) {
		incr = matcher.end();
		offset += incr;
		s = s.substring(incr);
	    }
	    if (Character.isJavaIdentifierStart(s.charAt(index))) {
		index++;
		int len = s.length();
		while (index < len
		       && Character.isJavaIdentifierPart(s.charAt(index))) {
		    index++;
		}
		String vname = s.substring(0,index);
		if (simpleClassNames.contains(vname)) {
		    String msg = errorMsg("fnameWasClassname", vname);
		    throw new IllegalArgumentException(msg);
		}
		if (exists(vname)) {
		    String msg = errorMsg("alreadyDefined", vname);
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, index);
		}
		offset += index;
		s = s.substring(index);
		matcher = WS_PATTERN.matcher(s);
		if (matcher.lookingAt()) {
		    incr = matcher.end();
		    offset += incr;
		    s = s.substring(incr);
		}
		if (s.length() == 0 || s.charAt(0) != '(') {
		    String msg = errorMsg("noArgList", vname);
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, 0);
		}
		s = s.substring(1);
		offset++;
		matcher = WS_PATTERN.matcher(s);
		if (matcher.lookingAt()) {
		    incr = matcher.end();
		    offset += incr;
		    s = s.substring(incr);
		}
		ArrayList<String> argsList = new ArrayList<>();
		if (s.length() > 0) {
		    Set<String> vset = vsetThreadLocal.get();
		    while (s.charAt(0) != ')') {
			matcher = ARG_PATTERN.matcher(s);
			if (matcher.lookingAt()) {
			    incr = matcher.end();
			    String arg = s.substring(0, incr);
			    if (arg.equals("global") || arg.equals("this")) {
				String msg = errorMsg("reservedIdent", arg);
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, 0);
			    }
			    if (vset.contains(arg)) {
				String msg = errorMsg("alreadyDefinedP", arg);
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, 0);
			    }
			    vset.add(arg);
			    argsList.add(arg);
			    offset += incr;
			    s = s.substring(incr);
			    if (s.length() == 0) {
				String msg = errorMsg("argListTerm", vname);
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, 0);
			    }
			}
			matcher = WS_PATTERN.matcher(s);
			if (matcher.lookingAt()) {
			    incr = matcher.end();
			    offset += incr;
			    s = s.substring(incr);
			    if (s.length() == 0) {
				String msg = errorMsg("argListTerm", vname);
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, 0);
			    }
			}
			if (s.length() > 0) {
			    char ch = s.charAt(0);
			    if (ch == ',') {
				s = s.substring(1);
				offset++;
				matcher = WS_PATTERN.matcher(s);
				if (matcher.lookingAt()) {
				    incr = matcher.end();
				    offset += incr;
				    s = s.substring(incr);
				}
			    } else if (s.charAt(0) == ')') {
				continue;
			    } else {
				String msg = errorMsg("argListChar", vname, ch);
				throw new ObjectParser.Exception
				    (msg, filenameTL.get(), s, 0);
			    }
			} else {
			    String msg = errorMsg("argListTerm", vname);
			    throw new ObjectParser.Exception
				(msg, filenameTL.get(), s, 0);
			}
		    }
		    s = s.substring(1);
		    offset++;
		    matcher = WS_PATTERN.matcher(s);
		    if (matcher.lookingAt()) {
			incr = matcher.end();
			offset += incr;
			s = s.substring(incr);
		    }
		} else {
		    String msg = errorMsg("argListTerm", vname);
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, 0);
		}
		if (s.length() == 0) {
		    String msg = errorMsg("functBrace");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, 0);
		}
		char ch = s.charAt(0);
		if (ch == '{') {
		    s = s.substring(1);
		    offset++;
		    s = s.stripTrailing();
		    len = s.length();
		    if (len == 0) {
			String msg = errorMsg("functBrace");
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, 0);
		    }
		    s = s.substring(0, len-1);
		    len--;
		    if (len == 0) {
			String msg = errorMsg("functBrace");
			throw new ObjectParser.Exception
			    (msg, filenameTL.get(), s, 0);
		    }
		} else {
		    String msg = errorMsg("functBrace");
		    throw new ObjectParser.Exception
			(msg, filenameTL.get(), s, 0);
		}
		String[] args = new String[argsList.size()];
		args = argsList.toArray(args);
		addFunction(vname, args, s, synchronizedFunction);
		return null;
	    } else {
		String msg = errorMsg("notFunctionDef");
		throw new ObjectParser.Exception
		    (msg, filenameTL.get(), s, 0);
	    }
	}
	if (s.charAt(0) != '=') {
	    String msg = errorMsg("noStartingEq");
	    throw new ObjectParser.Exception
		(msg, filenameTL.get(), s, 0);
	}
	offset++;
	s = s.substring(1);
	Object res = parseExpression(s, offset, orig, parseOnly);
	return (res instanceof TypedNull)? null: res;
    }

    Map<String,URL> baseMap = new HashMap<>();
    static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Find the URL for a javadoc file for the given class.
     * @param clasz the class
     * @return the URL
     */
    public URL findDocURL(Class<?> clasz) {
	// return findDocURL(clasz.getName().replace('$', '.'));
	return findDocURL(clasz.getCanonicalName());
    }

    /**
     * Find the URL for a javadoc file for the given class name.
     * @param classname the fully qualified class name
     * @return the URL
     */
    public URL findDocURL(String classname) {
	String pname = classname;
	int index;
	while ((index = pname.lastIndexOf('.')) != -1) {
	    pname = pname.substring(0, index);
	    if (baseMap.containsKey(pname)) {
		break;
	    }
	}
	if (index == -1) {
	    return null;
	}
	String cname = classname.substring(pname.length() + 1);
	URL base = baseMap.get(pname);
	try {
	    return new URL(base, cname + ".html");
	} catch (MalformedURLException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Find the URL for a javadoc file for the given class and field, method
     * or constructor within that class.
     * @param clasz the class
     * @param rest the remainder of the method, field, or constructor
     *        specification
     * @return the URL
     */
    public URL findDocURL(Class<?> clasz, String rest) {
	// return findDocURL(clasz.getName().replace('$', '.'), rest);
	return findDocURL(clasz.getCanonicalName(), rest);
    }

    /**
     * Find the URL for a javadoc file for the given class name and
     * field, method or constructor within that class.
     * @param classname the name of the class
     * @param rest the remainder of the method, field, or constructor
     *        specification
     * @return the URL
     */
    public URL findDocURL(String classname, String rest) {
	String pname = classname;
	int index;
	while ((index = pname.lastIndexOf('.')) != -1) {
	    pname = pname.substring(0, index);
	    if (baseMap.containsKey(pname)) {
		break;
	    }
	}
	if (index == -1) {
	    return null;
	}
	String cname = classname.substring(pname.length() + 1);
	URL base = baseMap.get(pname);
	try {
	    return new URL(base, cname + ".html#" + rest);
	} catch (MalformedURLException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Create a table mapping package names to the location containing
     * the HTML files for that package's documentation.
     * For the javadoc directories provided by the JDK, the name of the
     * final component in the path is "api" and that directory will
     * contain a file named either element-list or package-list.
     * @param apis a list of the URLS for javadoc directories
     * @exception IOException an IO error occurred
     */
    public void createAPIMap(List<URL> apis) throws IOException {
	for (URL url: apis) {
	    URL eurl = new URL(url, "element-list");
	    InputStream is = null;
	    try {
		is = eurl.openStream();
	    } catch (IOException eio) {
		URL purl = new URL(url, "package-list");
		is = purl.openStream();
	    }
	    LineNumberReader r = new
		LineNumberReader(new InputStreamReader(is, UTF8));
	    String line;
	    String currentModule = "";
	    String pkg = null;
	    while ((line = r.readLine()) != null) {
		if (line.startsWith("module:")) {
		    currentModule = line.substring(7) + "/";
		} else {
		    String rurl = currentModule + line.replace('.', '/')
			+ "/";
		    baseMap.put(line, new URL(url, rurl));
		}
	    }
	}
    }
}

//  LocalWords:  BLOCKQUOTE PRE OL YAML IsAlphabetic testit tokenize
//  LocalWords:  IllegalArgumentException getType CPAREN getFunct lt
//  LocalWords:  funct getValue UNARY exbundle MyClass noMethod varEq
//  LocalWords:  noClasses noClassName  enum ObjectParser parser's fi
//  LocalWords:  errorMsg argclasses toIntSize ExpressionParser's UTF
//  LocalWords:  toIntFromDouble nonNumberReturned fcallFailed cname
//  LocalWords:  badComma badOParen badCParen emptyParens badString
//  LocalWords:  badNumber badIdent noValue notVarIdent noStartingEq
//  LocalWords:  ExpressionParser needInt intRange notMethodName prev
//  LocalWords:  substring mname instanceof oargs findMethod unary ESP
//  LocalWords:  notAllowedReturnType returnTypes argumentTypes href
//  LocalWords:  functionClasses methodClasses fieldClasses toString
//  LocalWords:  noMethodF noMethodM noSorterF noSorterM noMethodC sb
//  LocalWords:  noSorterResultF noSorterResultM noSorterC prevType
//  LocalWords:  noSorterResultC toLongFromDouble adjacentOps notRVF
//  LocalWords:  syntaxError vnameWasClassname lexically ArrayList EQ
//  LocalWords:  fname createAPIMap hideString BITWISE misplacedEq
//  LocalWords:  ptype decr javadoc clasz classname html JDK api apis
//  LocalWords:  badCondEnd missingColon notBooleanBeforeQM QMARK EOL
//  LocalWords:  badColon ESPFunction numberOfArguments fargs args msg
//  LocalWords:  getClass notAllowedClass nestedCallM nestedCallP len
//  LocalWords:  qmarkdepth setQmarkDepth getQmarkDepth LinkedList
//  LocalWords:  unbalancedParens badCondNest badCBrace misplacedDot
//  LocalWords:  unbalancedBraces vmap containsKey expectingOpenParen
//  LocalWords:  missingLambda missingCBrace wrongArgCnt getLevel tmp
//  LocalWords:  ourlevel noColon fnameWasClassname noArgList noClass
//  LocalWords:  argListTerm argListChar functBrace notFunctionDef
//  LocalWords:  missingMethod wrongArgCntM missingFunct JSObject
//  LocalWords:  colonExpected notPropName badCBracket ambiguousClass
//  LocalWords:  unbalancedBrackets notArrayOrObject hasProperty plen
//  LocalWords:  propertyNames keySet entrySet setObject addObject
//  LocalWords:  parallelStream getSimpleName mclass constr getName
//  LocalWords:  getDeclaringClass retclass getReturnType setLength
//  LocalWords:  isArray isJavaArray isObject skipWhitespace MacOS
//  LocalWords:  whitespace unterminatedVar tvar notFunctName charAt
//  LocalWords:  misplacedVar varEQ noIdentAfterVar varEQOP skipType
//  LocalWords:  illformedClassName missingClassName prcedents paren
//  LocalWords:  skipLevel modGlobal modThis anobject notIndexed gmap
//  LocalWords:  IllegalStateException indexAssigned isESPArray typeof
//  LocalWords:  ESPObjectType ESPArrayType activeComma reservedIdent
//  LocalWords:  boolea boolean IDENT setReaderTL setWriterTL isVoid
//  LocalWords:  setErrorWriterTL errorWriter setReader setWriter
//  LocalWords:  setErrorWriter binaryOps wrongNumbArgsM pushArgMap
//  LocalWords:  wrongNumbArgsF argmap javax defaultObject
//  LocalWords:  importClass importClasses importFinished
