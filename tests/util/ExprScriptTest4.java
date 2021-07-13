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

public class ExprScriptTest4 {

    public void vatest(int ix, double... vals) {
	System.out.print("ix = + + ix, values =");
	for (double v: vals) {
	    System.out.print(" " + v);
	}
	System.out.println();
    }

    public int foo() { return 10;}
    public int bar() {return 20;}

    public static void main(String argv[]) throws Exception {
	String s; Object value;
	ExpressionParser parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	try {
	    s = Files.readString(new File("test4mE.esp").toPath());
	    parser.parse(s, "test4mE.esp");
	} catch (ObjectParser.Exception e) {
	    System.out.print(e.getFileName() + ", offset " + e.getOffset()
			     + ": " + e.getMessage());
	    System.out.println(" (EXCEPTION EXPECTED)");
	}

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	s = Files.readString(new File("test4m.esp").toPath());
	parser.parse(s);

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	double[] v1 = {
	    1.0, 2.0, 3.0
	};
	double[] v2 = {
	    4.0, 5.0, 6.0
	};
	parser.set("v1", v1);
	parser.set("v2", v2);
	double[][] matrix = {{10.0,  7.0, 3.0},
			     {7.0 , 20.0, 4.0},
			     {3.0 ,  4.0, 30.0}};
	parser.set("matrix", matrix);
	double [] y = {5.0, 10.0, 20.0};
	parser.set("y", y);

	s = Files.readString(new File("test4L.esp").toPath());
	parser.parse(s);

	double[] v1p = (double[]) parser.get("v1p");
	if (VectorOps.norm(VectorOps.sub(v1, v1p)) > 1.e-14) {
	    throw new Exception();
	}
	double[][] matrixp = (double[][])parser.get("matrixp");
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (Math.abs(matrix[i][j] - matrixp[i][j]) > 1.e-14) {
		    throw new Exception();
		}
	    }
	}
	double dp = (Double) parser.get("dp");
	System.out.println("dp = " + dp + ", expecting " +
			   VectorOps.dotProduct(v1, v2));
	if (Math.abs(dp - VectorOps.dotProduct(v1, v2)) > 1.e-14)
	    throw new Exception();
	double[] cp = (double [])parser.get("cp");
	double[] ecp = VectorOps.crossProduct(v1, v2);
	double norm = VectorOps.norm(VectorOps.sub(cp, ecp));
	System.out.println ("norm(cp - ecp) = " + norm);
	if (norm > 1.e-14) throw new Exception();
	double[] x =(double[]) parser.get("x");

	LUDecomp lud = new LUDecomp(matrix);
	double[] xx = lud.solve(y);
	norm = VectorOps.norm(VectorOps.sub(x, xx));
	System.out.println("norm(x -xx) = " + norm);
	if (norm > 1.e-14) throw new Exception();
	double det = (Double) parser.get("det");
	double det2 = lud.det();
	System.out.println("det = " + det);
	if (Math.abs(det - det2) > 1.e-14) throw new Exception();
	double[][] inverse = (double[][]) parser.get("inverse");
	double[][] ident = MatrixOps.multiply(matrix, inverse);
	for  (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.format("%3.2f ", ident[i][j]);
		if (i == j) {
		    if (Math.abs(ident[i][j] - 1.0) > 1.e-14) {
			throw new Exception();
		    }
		} else {
		    if (Math.abs(ident[i][j]) > 1.e-14) {
			throw new Exception();
		    }
		}
	    }
	    System.out.println();
	}
	System.out.println("--------");
	double[] a1 = (double[])parser.get("a1");
	String[] s1 = (String[])parser.get("s1");
	Object[] s2 = (Object[])parser.get("s2");
	int[] iarray = (int[])parser.get("iarray");
	long[] larray =(long[])parser.get("larray");
	for (int i = 0; i < 3; i++) {
	    System.out.format("%g, %s, %s, %d, %d\n",
			      a1[i], s1[i], s2[i], iarray[i], larray[i]);
	}
	double[][] m1 = (double[][])parser.get("m1");
	Object[][] m2 = (Object[][])parser.get("m2");
	String[][] sm1 = (String[][])parser.get("sm1");
	int[][] imatrix = (int[][])parser.get("imatrix");
	long[][] lmatrix = (long[][])parser.get("lmatrix");
	System.out.println("--------");
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(" " + m1[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("--------");
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(" " + m2[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("--------");
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(" " + sm1[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("--------");
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(" " + imatrix[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("--------");
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(" " + lmatrix[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("--------");
	System.out.println();

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test4k.esp").toPath());
	parser.parse(s);

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test4j.esp").toPath());
	parser.parse(s);

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test4i.esp").toPath());
	parser.parse(s);

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));

	s = Files.readString(new File("test4h.esp").toPath());
	parser.parse(s, "test4h.esp");
	System.out.println("x = " + parser.get("x"));
	System.out.println("y = " + parser.get("y"));
	JSArray array = (JSArray) parser.get("array");
	System.out.println("array[0] = " + array.get(0));

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));

	s = Files.readString(new File("test4g1.esp").toPath());
	try {
	    parser.parse(s, "test4g1.esp");
	    s = Files.readString(new File("test4g2.esp").toPath());
	    long t0 = System.nanoTime();
	    parser.parse(s, "test4g2.esp");
	    long t1 = System.nanoTime();
	    BasicStats stats = new BasicStats.Population();
	    (new GaussianRV(0.0, 1.0)).stream(50000000).forEach(stats::add);
	    long t2 = System.nanoTime();
	    System.out.println("ratio [running times] = " +
			       (((double)(t1 - t0))/ ((double)(t2 - t1))));
	    System.out.format("Java: mean = %g, sdev = %g\n",
			      stats.getMean(), stats.getSDev());
	    stats = (BasicStats) parser.get("stats");
	    System.out.format("ESP: mean = %g, sdev = %g\n",
			      stats.getMean(), stats.getSDev());
	} catch (ObjectParser.Exception e) {
	    System.out.print(e.getFileName() + ", offset " + e.getOffset()
			     + ": ");
	    if (e.getCause() != null) {
		Throwable t = e;
		while ((t = t.getCause()) != null) {
		    if (t instanceof ObjectParser.Exception) {
			e = (ObjectParser.Exception) t;
			System.out.print(e.getFileName() + ", offset "
					 + e.getOffset()
					 + ": ");
			e.printStackTrace();
		    } else {
			t.printStackTrace();
		    }
		}
	    } else {
		System.out.println(e.getMessage());
		e.printStackTrace();
	    }
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test4f.esp").toPath());
	try {
	    parser.set("obj", new ExprScriptTest4());
	    parser.parse(s, "test4f.esp");
	    System.out.println("x = " + parser.get("x"));
	    System.out.println("y = " + parser.get("y"));
	    System.out.println("z = " + parser.get("z"));
	    System.exit(1);
	} catch (ObjectParser.Exception e) {
	    System.out.println(e.getMessage() + " (ERROR EXPECTED)");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("a2d", null);
	parser.set("epts", null);
	parser.set("userdist", 10.0);
	parser.set("gcsdist", 10.0);
	parser.set("frameWidth", 1920);
	parser.set("frameHeight", 1080);
	s = Files.readString(new File("test4e.esp").toPath());
	try {
	    parser.parse(s, "tests4e.esp");
	    System.out.println("script returned; frameWidth = "
			       + parser.get("frameWidth"));
	    System.out.println("x = " + parser.get("x"));
	    System.out.println("y = " + parser.get("y"));
	} catch (ObjectParser.Exception e) {
	    System.out.print(e.getFileName() + ", offset " + e.getOffset()
			     + ": ");
	    if (e.getCause() != null) {
		Throwable t = e;
		while ((t = t.getCause()) != null) {
		    if (t instanceof ObjectParser.Exception) {
			e = (ObjectParser.Exception) t;
			System.out.print(e.getFileName() + ", offset "
					 + e.getOffset()
					 + ": ");
			e.printStackTrace();
		    } else {
			t.printStackTrace();
		    }
		}
	    } else {
		System.out.println(e.getMessage());
		e.printStackTrace();
	    }
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	parser = new ExpressionParser(Math.class);
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	s = Files.readString(new File("test4d.esp").toPath());
	try {
	    parser.parse(s, "test4d.esp");
	    System.out.println("script returned; frameWidth = "
			       + parser.get("frameWidth"));
	} catch (ObjectParser.Exception e) {
	    System.out.print(e.getFileName() + ", offset " + e.getOffset()
			     + ": ");
	    if (e.getCause() != null) {
		e.getCause().printStackTrace();
	    } else {
		System.out.println(e.getMessage());
		e.printStackTrace();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("out", new PrintWriter(System.out));
	s = Files.readString(new File("test4c.esp").toPath());
	parser.parse(s, "tests4c.esp");

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("out", new PrintWriter(System.out));
	s = Files.readString(new File("test4b.esp").toPath());
	parser.parse(s, "tests4b.esp");

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("out", new PrintWriter(System.out));
	s = Files.readString(new File("test4.esp").toPath());
	try  {
	    value = parser.parse(s);
	    System.out.println("no exception seen");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.println("exception expected: " + e.getMessage());
	}

	System.out.println("subspacing = " + parser.get("subspacing"));
	System.out.println("r = " + parser.get("r"));

	System.out.println("s2 = " + parser.get("s2"));
	System.out.println("s3 = " + parser.get("s3"));
	System.out.println("s4 = " + parser.get("s4"));

	System.out.println("t1 = " + parser.get("t1"));
	System.out.println("t2 = " + parser.get("t2"));
	System.out.println("t3 = " + parser.get("t3"));

	parser = new ExpressionParser();
	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.setWriter(new PrintWriter(System.out));
	parser.set("out", new PrintWriter(System.out));
	try {
	    s = Files.readString(new File("test4a.esp").toPath());
	    value = parser.parse(s, "test4a.esp");
	    throw new Exception("this test should have failed");
	} catch (ObjectParser.Exception e) {
	    System.out.print(e.getFileName() + ", offset " + e.getOffset()
			     + ": ");
	    if (e.getCause() != null) {
		e.getCause().printStackTrace();
	    } else {
		System.out.println(e.getMessage()
				   + " (ERROR EXPECTED)");
	    }
	} catch (java.lang.Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	String[] fnames = {"test4m.esp", "test4L.esp", "test4k.esp",
			   "test4j.esp", "test4i.esp", "test4h.esp",
			   "test4g.esp",  "test4e.esp",
			   "test4d.esp", "test4c.esp", "test4b.esp"};
	System.out.println("Try a series of files with"
			   + " a security manager installed");
	ExpressionParser[] parsers = new ExpressionParser[fnames.length];
	String[] srcs = new String[fnames.length];
	int i = 0;
	for (String fn: fnames) {
	    if (fn.equals("test4i.esp") || fn.equals("test4h.esp")
		|| fn.equals("test4g.esp") || fn.equals("test4f.esp")
		|| fn.equals("test4e.esp") || fn.equals("test4d.esp")) {
		parsers[i] = new ExpressionParser(Math.class);
	    } else {
		parsers[i] = new ExpressionParser();
	    }
	    parsers[i].setScriptingMode();
	    parsers[i].setImportMode();
	    parsers[i].setScriptImportMode();
	    parsers[i].setGlobalMode();
	    parsers[i].setWriter(new PrintWriter(Writer.nullWriter()));
	    parsers[i].setErrorWriter(new PrintWriter(Writer.nullWriter()));
	    srcs[i] = Files.readString(new File(fn).toPath());
	    parser = parsers[i];
	    if (fn.equals("test4L.esp")) {
		parser.set("v1", v1);
		parser.set("v2", v2);
		parser.set("matrix", matrix);
		parser.set("y", y);
	    } else if (fn.equals("test4f")) {
		parser.set("obj", new ExprScriptTest4());
	    } else if (fn.equals("test4e.esp")) {
		parser.set("a2d", null);
		parser.set("epts", null);
		parser.set("userdist", 10.0);
		parser.set("gcsdist", 10.0);
		parser.set("frameWidth", 1920);
		parser.set("frameHeight", 1080);
	    } else if (fn.equals("test4b.esp")) {
		parser.set("out", new PrintWriter(Writer.nullWriter()));
	    } else if (fn.equals("test4.esp")) {
		parser.set("out", new PrintWriter(Writer.nullWriter()));
	    }
	    i++;
	}
	System.setSecurityManager(new SecurityManager());
	i = -1;
	for (String fn: fnames) {
	    System.out.println("... " + fn);
	    try {
		i++;
		parsers[i].parse(srcs[i], fn);
	    }  catch (ObjectParser.Exception e) {
		System.out.print(e.getFileName() + ", offset " + e.getOffset()
				 + ": ");
		if (e.getCause() != null) {
		    Throwable t = e;
		    while ((t = t.getCause()) != null) {
			if (t instanceof ObjectParser.Exception) {
			    e = (ObjectParser.Exception) t;
			    System.out.print(e.getFileName() + ", offset "
					     + e.getOffset()
					     + ": ");
			    e.printStackTrace();
			} else {
			    t.printStackTrace();
			}
		    }
		} else {
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	System.exit(0);
    }
}
