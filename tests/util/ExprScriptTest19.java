import java.io.*;

import org.bzdev.scripting.*;
import org.bzdev.util.*;

public class ExprScriptTest19 {
    public static void main(String[] argv) throws Exception {
	DefaultScriptingContext dsc = new
	    DefaultScriptingContext();
	ExtendedScriptingContext sc = new ExtendedScriptingContext(dsc);
	PrintWriter out = new PrintWriter(System.out);
	sc.setWriter(out);
	sc.putScriptObject("scripting", sc);

	// String index problem occurred with the semicolon but not
	// without it
	sc.evalScript("import (\"org.bzdev.lang.MathOps\");");
	sc.evalScript("var ilong = asLong(10)");
	Object obj = sc.getScriptObject("ilong");
	System.out.println("ilong = " + obj + ", type = "
			   + obj.getClass());

    }
}
