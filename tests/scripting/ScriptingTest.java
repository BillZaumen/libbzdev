import org.bzdev.scripting.*;
import org.bzdev.lang.Callable;
import javax.script.*;
import java.security.*;
import java.io.*;
// import java.util.*;
// import javax.security.auth.PrivateCredentialPermission;
// import javax.management.MBeanPermission;
// import javax.security.auth.kerberos.ServicePermission;
// import java.util.regex.*;

import java.util.Properties;


// for graphics test
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;


public class ScriptingTest {
    static boolean inSandbox = false;
    // static SecurityManager sm = null;
    static ThreadGroup globaltg;

    public static class ScriptRunner extends DefaultScriptingContext {
	public ScriptRunner() {super();}
	public ScriptRunner(String languageName) {
	    super(languageName);
	}
	public void run(String filename) throws Exception {
	    // ScriptEngine engine = getScriptEngine();
	    // boolean oldInSandbox = inSandbox;
	    // inSandbox = false;
	    // inSandbox = oldInSandbox;
	    FileReader reader = new FileReader(filename);
	    /*
	    if (sm != null) {
		try {
		    System.setSecurityManager(sm);
		} catch (UnsupportedOperationException eu) {
		    System.exit(0);
		}
	    }
	    */
	    try {
		FileReader fr = new FileReader("Makefile");
		int ch = fr.read();
		fr.close();
	    } catch (Exception e1) {
		System.out.println("read outside of scripting-context "
				   + "sandbox failed");
	    }
	    try {
		Thread thread = new Thread(globaltg, (Thread)null) {
			public void run() {}
		    };
	    } catch (Exception e2) {
		System.out.println("could not create thread for globaltg"
				   + " outside of scripting-context sandbox");
	    }
	    // onScriptStarting();
	    Callable testCall = new Callable() {
		    public void call() {
			try {
			    imageTest();
			} catch (Exception ei) {
			    System.out.println("imageTest failed");
			    ei.printStackTrace(System.out);
			}
			try {
			    FileReader fr = new FileReader("Makefile");
			    int ch = fr.read();
			    fr.close();
			} catch (Exception e1) {
			    System.out.println
				("read inside of scripting-context "
				 + "sandbox failed");
			}
			try {
			    Thread thread = new Thread(globaltg, (Thread)null) {
				    public void run() {}
				};
			} catch (Exception e2) {
			    System.out.println
				("could not create thread for globaltg"
				 + " inside of scripting-context sandbox");
			}
		    }
		};
	    // doScriptSandboxed(testCall);
	    testCall.call();
	    // onScriptEnding();
	    // engine.put(ScriptEngine.FILENAME, filename);
	    // engine.eval(reader);
	    evalScript(filename, reader);
	}

	public void testPrivate(Properties props) {
	    try {
		Object result =
		    invokePrivateFunction(props,
					  // ScriptingContext.PFMode.PRIVILEGED,
					  "test",
					  Double.valueOf(10.0),
					  Double.valueOf(20.0));
		putScriptObject("result", result);
		evalScript("java.lang.System.out.println(\"result = \" "
			   + "+ result);");
	    } catch (Exception e) {
		System.out.println("testPrivate failed: "
				   +e.getMessage());
	    }
	}

	public void testPrivate(Bindings bindings, Properties props) {
	    try {
		Object result =
		    invokePrivateFunction(bindings, props,
					  // ScriptingContext.PFMode.PRIVILEGED,
					  "test",
					  Double.valueOf(10.0),
					  Double.valueOf(20.0));
		putScriptObject("result", result);
		evalScript("java.lang.System.out.println(\"result = \" "
			   + "+ result);");
	    } catch (Exception e) {
		System.out.println("testPrivate failed: "
				   +e.getMessage());
	    }
	}


	public void testPrivate2(Properties props, Object object) {
	    try {
		invokePrivateFunction(props,
				      // ScriptingContext.PFMode.PRIVILEGED,
				      "printit", object);
	    } catch (Exception e) {
		System.out.println("testPrivate2 failed");
	    }
	}

	public void testPrivate2(Bindings bindings,
				 Properties props,
				 Object object)
	{
	    try {
		invokePrivateFunction(bindings, props,
				      // ScriptingContext.PFMode.PRIVILEGED,
				      "printit", object);
	    } catch (Exception e) {
		System.out.println("testPrivate2 failed");
	    }
	}

    }

    static public void imageTest() {
	BufferedImage image = new BufferedImage(200,200,
						BufferedImage.TYPE_INT_BGR);

	Graphics2D g2d = image.createGraphics();
	Font font = g2d.getFont();
	FontMetrics fm = g2d.getFontMetrics(font);
	FontRenderContext frc = g2d.getFontRenderContext();
	LineMetrics lm = font.getLineMetrics("hello", frc);
	lm = fm.getLineMetrics("hello", g2d);
	lm = fm.getLineMetrics("hello", 0, 2, g2d);
	g2d.dispose();
    }

    public static void main(String argv[]) throws Exception {

	int ind = 0;
	boolean useScriptSandbox = false;
	boolean multiple = false;


	System.out.println("Scripting Language Names:");
	for (String name: Scripting.getLanguageNameSet()) {
	    System.out.println("    " + name);
	}
	
	System.out.println("Scripting Language Aliases:");
	for (String name: Scripting.getAliasSet()) {
	    System.out.println("    " + name);
	}

	System.out.println("Scripting File extensions:");
	for (String name: Scripting.getExtensionSet()) {
	    System.out.println("    " + name);
	}

	System.out.println("language corresponding to rhino: "
			   + Scripting.getLanguageNameByAlias("rhino"));

	System.out.println("language corresponding to .js: "
			   + Scripting.getLanguageNameByExtension("js"));

	System.out.println("language corresponding to .es: "
			   + Scripting.getLanguageNameByExtension("es"));

	System.out.println("extensions corresponding to ECMAScript:");
	for (String name: Scripting.getExtensionsByLanguageName("ECMAScript"))
	    System.out.println("    " + name);

	System.out.println("extensions corresponding to .js:");
	for (String name: Scripting.getExtensionsByExtension("js"))
	    System.out.println("    " + name);

	System.out.println("extensions corresponding to .es:");
	for (String name: Scripting.getExtensionsByExtension("es"))
	    System.out.println("    " + name);

	/*
	System.out.println("extensions corresponding to rhino:");
	for (String name: Scripting.getExtensionsByAlias("rhino"))
	    System.out.println("    " + name);
	*/

	System.out.println("aliases corresponding to ECMAScript:");
	for (String name: Scripting.getAliasesByLanguageName("ECMAScript"))
	    System.out.println("    " + name);

	System.out.println("aliases corresponding to .js:");
	for (String name: Scripting.getAliasesByExtension("js"))
	    System.out.println("    " + name);

	System.out.println("aliases corresponding to .es:");
	for (String name: Scripting.getAliasesByExtension("es"))
	    System.out.println("    " + name);


	/*

	System.out.println("aliases corresponding to rhino:");
	for (String name: Scripting.getAliasesByAlias("rhino"))
	    System.out.println("    " + name);
	*/

	globaltg = new ThreadGroup(Thread.currentThread().getThreadGroup()
				   .getParent(), "foo");

	while (ind < argv.length && argv[ind].startsWith("-")) {
	    /*
	    if (argv[ind].equals("--sandbox")) {
		sm = new SecurityManager();
	    } else if (argv[ind].equals("--scriptSandbox")) {
		useScriptSandbox = true;
	    }
	    */
	    ind++;
	}


	/*
	ScriptEngineManager manager = new ScriptEngineManager();

	final ScriptEngine engine = manager.getEngineByName("javascript");
	ScriptEngineFactory factory = engine.getFactory();
	final String languageName = factory.getLanguageName();
	final Bindings defaultBindings =
	    engine.getBindings(ScriptContext.ENGINE_SCOPE);
	*/

	if (argv.length == ind) {
	    /*
	    if (sm != null) {
		try {
		    System.setSecurityManager(sm);
		} catch (UnsupportedOperationException eu) {
		    System.exit(0);
		}
		try {
		    sm.checkPermission(new
				       RuntimePermission("setSecurityManager"));
		    System.out.println("can change security manager");
		} catch (SecurityException esec) {
		    System.out.println("Cannot change security manager");
		}
	    }
	    */
	    System.exit(0);
	}


	int index = argv[ind].lastIndexOf('.');
	String languageName =  null;
	if (index > -1) {
	    String ext = argv[ind].substring(index+1);
	    languageName = Scripting.getLanguageNameByExtension(ext);
	    System.out.println("language = " + languageName
			       + " for file-name extension " + ext);
	}

	// due to Nashorn being deprecated with a replacement in limbo.
	boolean hasECMAScript = Scripting.supportsLanguage("ECMAScript");
	if (languageName.equals("ECMAScript") && hasECMAScript == false) {
	    // quietly skip this test.
	    System.exit(0);
	}

	ScriptingContext sc = new DefaultScriptingContext(languageName);
	FileReader reader = null;
	ScriptRunner sr = new ScriptRunner(languageName);

	try {
	    reader = new FileReader(argv[ind]);
	} catch (FileNotFoundException fnfe) {
	    System.out.println("file not found");
	    System.exit(1);
	}

	if (useScriptSandbox) {
	    // sm = new ScriptingSecurityManager();
	    System.exit(0);
	}
	try {
	    sc.evalScript(argv[ind], reader);
	    sr.run(argv[ind]);
	    Properties bprops = new Properties();
	    Bindings bindings = sc.createBindings();
	    bprops.setProperty("ECMAScript",
			       "({test: function(x,y) {return x+y}})");
	    bprops.setProperty("ESP",
			       "({test: function(x,y) {x+y}})");
	    sr.testPrivate(null, bprops);
	    sr.testPrivate(bindings, bprops);
	    sr.testPrivate(null, bprops);
	    sr.testPrivate(bindings, bprops);

	    bprops = new Properties();
	    bprops.setProperty("ECMAScript",
			       "({printit: function(x) "
			       + "{java.lang.System.out.println(x);}})");
	    bprops.setProperty("ESP",
			       "({printit: function(x) "
			       + "{global.getWriter().println(x);}})");
	    sr.testPrivate2(null, bprops, sc.evalScript("10 + 20"));
	    sr.testPrivate2(bindings, bprops, sc.evalScript("10 + 20"));

	    bprops = new Properties();
	    Object obj = sr.evalScript("({foo: 10, bar: 20})");
	    bprops.setProperty("ECMAScript",
			      "({printit: function(x) "
			      + "{java.lang.System.out.println"
			      + "(\"{foo: \" + x.foo "
                               + "+ \", bar: \" + x.bar + \"}\");}})");
	    bprops.setProperty("ESP",
			      "({printit: function(x) "
			      + "{global.getWriter().println"
			      + "(\"{foo: \" + x.foo "
                               + "+ \", bar: \" + x.bar + \"}\");}})");
	    sr.testPrivate2(null, bprops, obj);
	    sr.testPrivate2(bindings, bprops, obj);

	    System.out.println("Tests without explicit bindings ...");
	    Properties props = new Properties();
	    /*
	    System.out.println("engine assumes scripting langage is "
			       + engine.get(ScriptEngine.LANGUAGE));
	    System.out.println("engine assumes scripting langage name is "
			       + engine.get(ScriptEngine.NAME));
	    */
	    props.setProperty("ECMAScript",
			      "({test: function(x,y) {return x + y}})");
	    props.setProperty("ESP",
			      "({test: function(x,y) {return x + y}})");
	    sr.testPrivate(props);
	    System.out.println("now try a test that will print an expression");
	    props.setProperty("ECMAScript",
			      "({printit: function(x) "
			      + "{java.lang.System.out.println(x);}})");
	    props.setProperty("ESP",
			      "({printit: function(x) "
			      + "{global.getWriter().println(x);}})");
	    sr.testPrivate2(props, sr.evalScript("10 + 20"));
	    
	    props = new Properties();
	    props.setProperty("ECMAScript",
			      "({printit:function(x) {print (typeof(x));}})");
	    props.setProperty("ESP",
			      "({printit:function(x) {"
			      + "global.getWriter.println(global.typeof(x))"
			      + ";}})");


	    if (languageName.equals("ESP")) {
		sr.evalScript("function valueOf(x){x}");
	    } else {
		sr.evalScript("function valueOf(x){return x;}");
	    }
	    Object object = sr.evalScript("valueOf({foo: 10, bar: 20})");
	    sr.testPrivate2(props, object);
	    sr.putScriptObject("foobar", object);
	    if (languageName.equals("ESP")) {
		sr.evalScript("global.getWriter().println("
			      +"\"foobar.foo = \" + foobar[\"foo\"])");
	    } else {
		sr.evalScript("print(\"foobar.foo = \" + foobar.foo)");
	    }
	    System.out.println("calling sr.testPrivate2(props, object)");
	    props = new Properties();
	    props.setProperty("ECMAScript",
			      "({printit:function(x) "
			      + "{java.lang.System.out.println"
			      + "(\"{foo: \" + x.foo "
                               + "+ \", bar: \" + x.bar + \"}\");}})");
	    props.setProperty("ESP",
			      "{printit:function(x) "
			      + "{global.getWriter().println"
			      + "(\"{foo: \" + x[\"foo\"]"
                               + "+ \", bar: \" + x[\"bar\"] + \"}\");}}");
	    sr.testPrivate2(props, object);
	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	System.exit(0);
    }
}
