import org.bzdev.util.*;
import org.bzdev.graphs.Graph;
import org.bzdev.lang.MathOps;
import org.bzdev.scripting.*;
import org.bzdev.lang.UnexpectedExceptionError;

import java.io.*;

import java.net.URL;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.function.*;


// Use to test an issue with the ESP provider, where it worked
// with an extended scripting context but not a default scripting
// ocntext.
public class ExprScriptTestE {

    static String errormsg(String name, Object...args) {
	return name;
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
    

    public static void main(String argv[]) throws Exception {
	// ExpressionParser parser = new ExpressionParser();
	ExpressionParser parser;
	try {
	    parser = argv.length == 0?
		new ExpressionParser():
		new ExpressionParser(classArray1, classArray2, null,
				     classArray4, null);
	} catch (IllegalAccessException e) {
	    throw new UnexpectedExceptionError(e);
	}
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	
	String s = "import(java.awt.Color); var black = java.awt.Color.BLACK;";
	System.out.println("var black...  returns " + parser.parse(s));

	System.exit(0);
    }
}
