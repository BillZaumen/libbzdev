package org.bzdev.providers.esp;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.security.*;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.ResourceBundle;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import javax.script.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.scripting.ExtendedScriptingContext;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ErrorMessage;
import org.bzdev.util.ObjectParser;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.providers.esp.lpack.ESP

public class ESPEngine extends AbstractScriptEngine
    implements ScriptEngine, Invocable
{

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.providers.esp.lpack.ESP");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    ExpressionParser parser;
    // ScriptContext context;
    Bindings gbindings = null;
    Bindings bindings = null;
    Reader reader = null;
    Writer writer  = null;
    PrintWriter printWriter = null;
    Writer errorWriter = null;
    PrintWriter printErrorWriter = null;

    static final String linesepStr = System.getProperty("line.separator");
    static final char[] linesep = linesepStr.toCharArray();
    static final char finalLineSep = linesep[linesep.length-1];

    static ScriptException getScriptException(ObjectParser.Exception e) {
	int[] loc = ErrorMessage.getLineAndColumn(e.getInput(), e.getOffset());
	String filename = e.getFileName();
	int lineNumber = loc[0];
	int columnNumber = loc[1];
	e.setPrefix("### ");
	String msg = e.getMessage();
	ScriptException se = new ScriptException(msg, filename,
						 lineNumber, columnNumber+1);
	se.initCause(e.getCause());
	se.setStackTrace(e.getStackTrace());
	Throwable t = e.getCause();
	while (t != null) {
	    if (t instanceof ObjectParser.Exception) {
		ObjectParser.Exception ope = (ObjectParser.Exception) t;
		ope.setPrefix("  ... ### ");
		ope.showLocation(true);
	    }
	    t = t.getCause();
	}
	return se;
    }

    private static final Class<?> classArray1[] = {
	Reader.class, PrintWriter.class, Writer.class, CharBuffer.class,
	Set.class, Object.class

    };

    private static final Class<?> classArray2[] = {
	Object.class, Class.class,
	ScriptingContext.class, ExtendedScriptingContext.class,
	Reader.class, PrintWriter.class, CharBuffer.class
    };

    private static final Class<?> classArray4[] = classArray2;

    /**
     * Constructor.
     */
    public ESPEngine() {
	try {
	    parser = new ExpressionParser(classArray1, classArray2, null,
					  classArray4, null);
	} catch (IllegalAccessException e) {
	    throw new UnexpectedExceptionError(e);
	}
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	/*
	  parser.addClasses(Reader.class, Writer.class, CharBuffer.class,
	  PrintWriter.class);
	*/
	context = new SimpleScriptContext();
    }

    private synchronized void doContext() {
	Reader cr = context.getReader();
	if (cr != reader) {
	    reader = cr;
	    parser.setReader(cr);
	    parser.setReader(cr);
	}
	Writer cw = context.getWriter();
	if (cw != writer) {
	    writer = cw;
	    if (writer instanceof PrintWriter) {
		printWriter = (PrintWriter) writer;
	    } else {
		printWriter = new PrintWriter(writer);
	    }
	    parser.setWriter(printWriter);
	}
	Writer cew = context.getErrorWriter();
	if (cew != errorWriter) {
	    errorWriter = cew;
	    if (errorWriter instanceof PrintWriter) {
		printErrorWriter = (PrintWriter) errorWriter;
	    } else {
		printErrorWriter = new PrintWriter(errorWriter);
	    }
	    parser.setErrorWriter(printErrorWriter);
	}
	Bindings gb = context.getBindings(ScriptContext.GLOBAL_SCOPE);
	Bindings cb = context.getBindings(ScriptContext.ENGINE_SCOPE);
	if (gb != gbindings) {
	    gbindings = gb;
	    parser.setGlobalBindings(gbindings);
	}
	if (cb != bindings) {
	    bindings  = cb;
	    parser.setBindings(bindings);
	}
    }


    @Override
    public Bindings createBindings() {
	Map<String,Object> map = Collections.synchronizedMap
	    (new HashMap<String,Object>());
	return new SimpleBindings(map);
    }

    private String readAll(Reader reader) throws ScriptException {
	char[] cbuf = new char[1024];
	StringBuilder sb = new StringBuilder(1024);
	int len;
	try {
	    while ((len = reader.read(cbuf)) != -1) {
		sb.append(cbuf, 0, len);
	    }
	    return sb.toString();
	} catch (IOException eio) {
	    throw new ScriptException(eio);
	}
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
	return eval(readAll(reader));
    }

    @Override
    public Object eval(Reader reader, Bindings n)  throws ScriptException {
	return eval(readAll(reader), n);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context)
	     throws ScriptException
    {
	return eval(readAll(reader), context);
    }

    @Override
    public Object eval(String script)  throws ScriptException {
	doContext();
	String filename = (String)get(ScriptEngine.FILENAME);
	try {
	    return (filename == null)? parser.parse(script):
		parser.parse(script, filename);
	} catch (ObjectParser.Exception e) {
	    throw getScriptException(e);
	} catch(Exception e) {
	    throw new ScriptException(e);
	}
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
	doContext();
	String filename = (String) get(ScriptEngine.FILENAME);
	try {
	    return (filename == null)? parser.parse(script, n):
		parser.parse(script, filename, n);
	} catch (ObjectParser.Exception e) {
	    throw getScriptException(e);
	} catch(Exception e) {
	    throw new ScriptException(e);
	}
    }

    @Override
    public Object eval(String script, ScriptContext context)
	throws ScriptException
    {
	try {
	    parser.setReaderTL(context.getReader());
	    Writer w = context.getWriter();
	    Writer ew = context.getErrorWriter();
	    parser.setWriterTL((w==null)? null:
			       (w instanceof PrintWriter)?
			       (PrintWriter) w: new PrintWriter(w));
	    parser.setErrorWriterTL((ew==null)? null:
				    (ew instanceof PrintWriter)?
				    (PrintWriter) ew: new PrintWriter(ew));

	    String filename = (String)get(ScriptEngine.FILENAME);
	    parser.setGlobalBindings(context
				     .getBindings(ScriptContext.GLOBAL_SCOPE));
	    try {
		return (filename == null)?
		    parser.parse(script,
				 context.getBindings(ScriptContext
						     .ENGINE_SCOPE)):
		    parser.parse(script, filename,
				 context.getBindings(ScriptContext
						     .ENGINE_SCOPE));
	    } catch (ObjectParser.Exception e) {
		throw getScriptException(e);
	    } catch(Exception e) {
		throw new ScriptException(e);
	    }
	} finally {
	    parser.setReaderTL(null);
	    parser.setWriterTL(null);
	    parser.setErrorWriterTL(null);
	    doContext();
	}
    }

    @Override
    public ScriptEngineFactory getFactory() {
	return new ESPFactory();
    }

    // Invocable

    @Override
    public <T> T getInterface(Class<T> clasz) {
	InvocationHandler handler = new InvocationHandler() {
		public Object invoke(Object proxy, Method m, Object[] args)
		    throws Throwable
		{
		    String method = m.getName();
		    return invokeFunction(method, args);
		}
	    };
	try {
	    Object result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Object>() {
			public Object run()
			    throws IllegalArgumentException,
				   NullPointerException,
				   SecurityException,
				   ObjectParser.Exception
			{
			    return Proxy.newProxyInstance
				(clasz.getClassLoader(), new Class<?>[]{clasz},
				 handler);
			}
		    });
	    return clasz.cast(result);
	}  catch (PrivilegedActionException ep) {
	    java.lang.Exception e = ep.getException();
	    if (e instanceof RuntimeException) {
		throw (RuntimeException) e;
	    } else {
		throw new UnexpectedExceptionError(e);
	    }
	}
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz)
	throws IllegalArgumentException
    {
	if (thiz == null) {
	    throw new IllegalArgumentException(errorMsg("nullFirstArgument"));
	} else if (thiz instanceof ExpressionParser.ESPObject) {
	    ExpressionParser.ESPObject object  =
		(ExpressionParser.ESPObject)thiz;
	    return object.convert(clasz);
	} else {
	    String msg = errorMsg("nonObjectFirstArgument");
	    throw new IllegalArgumentException(msg);
	}
	
   }

    @Override
    public Object invokeFunction(String name, Object... args)
	throws ScriptException, NoSuchMethodException, NullPointerException
    {
	if (name == null) {
	    throw new NullPointerException(errorMsg("nullFirstArgument"));
	}
	Object obj = parser.get(name);
	if (obj == null) {
	    throw new NoSuchMethodException(errorMsg("noMethod", name));
	}
	if (obj instanceof ExpressionParser.ESPFunction) {
	    ExpressionParser.ESPFunction f = (ExpressionParser.ESPFunction) obj;
	    try {
		return f.invoke(args);
	    } catch (ObjectParser.Exception e) {
		throw getScriptException(e);
	    } catch (Exception e) {
		throw new ScriptException(e);
	    }
	}
	throw new NoSuchMethodException(errorMsg("noMethod", name));
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args)
	throws ScriptException, NoSuchMethodException, NullPointerException
    {
	if (thiz == null) {
	    throw new IllegalArgumentException(errorMsg("nullFirstArgument"));
	}
	if (name == null) {
	    throw new NullPointerException(errorMsg("nullSecondArgument"));
	}
	if (thiz instanceof ExpressionParser.ESPObject) {
	    Object obj = ((ExpressionParser.ESPObject)thiz).get(name);
	    if (obj == null) {
		throw new NoSuchMethodException(errorMsg("noMethod", name));
	    } else if (obj instanceof ExpressionParser.ESPFunction) {
		ExpressionParser.ESPFunction f =
		    (ExpressionParser.ESPFunction) obj;
		int nf = f.numberOfArguments();
		if (nf != args.length) {
		    String msg =
			errorMsg("argsForMethod", name, nf, args.length);
		    throw new NoSuchMethodException(msg);
		}
		try {
		    return f.invoke(args);
		} catch (ObjectParser.Exception e) {
		    throw getScriptException(e);
		} catch (Exception ee) {
		    throw new ScriptException(ee);
		}
	    } else {
		throw new NoSuchMethodException(errorMsg("noMethod", name));
	    }
	} else {
	    throw new IllegalArgumentException(errorMsg("notObject1"));
	}
    }
}
