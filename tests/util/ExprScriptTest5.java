import org.bzdev.lang.MathOps;
import org.bzdev.util.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Population;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.LUDecomp;
import org.bzdev.math.MatrixOps;
import org.bzdev.math.VectorOps;

public class ExprScriptTest5 {

    public void vatest(int ix, double... vals) {
	System.out.print("ix = " + ix + ", values =");
	for (double v: vals) {
	    System.out.print(" " + v);
	}
	System.out.println();
    }

    public static void main(String argv[]) throws Exception {
	String s; Object value;
	ExpressionParser parser;

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test5.esp").toPath());
	try {
	    parser.set("obj", new ExprScriptTest5());
	    parser.parse(s, "test5.esp");
	} catch (Exception e) {
	    String name = e.getClass().getName();
	    name = name.replace('$', '.');
	    System.out.println(name + ": "
			       + e.getMessage());
	    for (StackTraceElement ste: e.getStackTrace()) {
		String cn = ste.getClassName();
		cn.replace('$', '.');
		int ind = cn.lastIndexOf('.');
		if (ind >= 0) {
		    cn = cn.substring(ind+1);
		}
		System.out.format("... at %s.%s (line %d)\n",
				  cn,
				  ste.getMethodName(),
				  ste.getLineNumber());
	    }
	    Throwable ee = e;
	    while ( (ee = ee.getCause()) != null) {
		System.out.println("---------");
		name = ee.getClass().getName();
		name = name.replace('$', '.');
		System.out.println(name + ": "
				   + ee.getMessage());
		for (StackTraceElement ste: ee.getStackTrace()) {
		    String cn = ste.getClassName();
		    cn = cn.replace('$', '.');
		    int ind = cn.lastIndexOf('.');
		    if (ind >= 0) {
			cn = cn.substring(ind+1);
		    }
		    System.out.format("... at %s.%s (line %d)\n",
				      cn,
				      ste.getMethodName(),
				      ste.getLineNumber());
		}
	    }
	    System.exit(1);
	}

	System.exit(0);
    }
}
