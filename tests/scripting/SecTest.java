import org.bzdev.scripting.*;
import org.bzdev.lang.*;
import java.io.*;
import java.util.Properties;
import javax.script.*;


public class SecTest {

    static class OurScriptingContext extends DefaultScriptingContext {
	Properties properties;
	OurScriptingContext(Properties properties) {
	    super("ECMAScript");
	    this.properties = properties;
	}
	public Object testN(Object x) throws ScriptException {
	    return invokePrivateFunction(properties,
					 // ScriptingContext.PFMode.PRIVILEGED,
					 "test",
					 x);
	}
	public Object testS(Object x) throws ScriptException {
	    return invokePrivateFunction(properties,
					 // ScriptingContext.PFMode.SANDBOXED,
					 "test",
					 x);
	}

	public void testDSPE1() throws Exception {
	    /*
	    doScriptPrivileged(new ExceptionedCallable() {
		    public void call() throws Exception {
			System.out.println
			    ((new FileInputStream("test1.js")) != null);
		    }
		});
	    */
	    System.out.println((new FileInputStream("test1.js")) != null);
	}

	public void testDSP1() {
	    /*
	    doScriptPrivileged(new Callable() {
		    public void call() {
			try {
			    System.out.println
				((new FileInputStream("test1.js")) != null);
			} catch (Exception e) {
			    System.out.println
				("doScriptPrivileged(Callable) failed");
			}
		    }
		});
	    */
	}

	public InputStream testDSPER1() throws Exception {
	    /*
	    return doScriptPrivilegedReturns
		(new ExceptionedCallableReturns<InputStream>() {
		    public InputStream call() throws Exception{
			return (new FileInputStream("test1.js"));
		    }
		});
	    */
	    return new FileInputStream("test1.js");
	}

	public InputStream testDSPR1() {
	    try {
		return (new FileInputStream("test1.js"));
	    } catch (Exception e) {
		System.out.println
		    ("FileInputStream failed " + "(test1.js");
		return null;
	    }

	    /*
	    return doScriptPrivilegedReturns
		(new CallableReturns<InputStream>() {
		    public InputStream call() {
			try {
			    return (new FileInputStream("test1.js"));
			} catch (Exception e) {
			    System.out.println
				("doScriptPrivileged(Callable) failed "
				 + "(test1.js");
			    return null;
			}
		    }
		});
	    */
	}

	public void testDSPE2() throws Exception {
	    /*
	    doScriptPrivileged(new ExceptionedCallable() {
		    public void call() throws Exception {
			System.out.println
			    ((new FileInputStream("test2.js")) != null);
		    }
		});
	    */
	}

	public void testDSP2() {
	    /*
	    doScriptPrivileged(new Callable() {
		    public void call() {
			try {
			    System.out.println
				((new FileInputStream("test2.js")) != null);
			} catch (Exception e) {
			    System.out.println
				("doScriptPrivleged(Callable) failed "
				 + "(test2.js) as expected");
			}
		    }
		});
	    */
	}

	public InputStream testDSPER2() throws Exception {
	    /*
	    return doScriptPrivilegedReturns
		(new ExceptionedCallableReturns<InputStream>() {
		    public InputStream call() throws Exception {
			return (new FileInputStream("test2.js"));
		    }
		});
	    */
	    return new FileInputStream("test2.js");
	}

	public InputStream testDSPR2() {
	    /*
	    return doScriptPrivilegedReturns
		(new CallableReturns<InputStream>() {
		    public InputStream call() {
			try {
			    return (new FileInputStream("test2.js"));
			} catch (Exception e) {
			    System.out.println
				("doScriptPrivleged(Callable) failed "
				 + "(test2.js) as expected");
			    return null;
			}
		    }
		});
	    */
	    try {
		return new FileInputStream("test2.js");
	    } catch (Exception e) {
		return null;
	    }
	}
    }

    public static void main(String argv[]) throws Exception {
	boolean checkPolicy =
	    (System.getProperty("java.security.policy") != null);

	Properties props = new Properties();
	props.setProperty("ECMAScript",
			  "({test: function(fname) "
			  + "{return new java.io.FileInputStream(fname)}})");
	OurScriptingContext osc = new OurScriptingContext(props);

	try {
	    System.out.println("-- Constructing scripting contexts before "
			       + "a security manager is installed --");
	    DefaultScriptingContext dsc =
		new DefaultScriptingContext("ECMAScript");
	    dsc.evalScript("is = new java.io.FileInputStream(\"test2.js\")");
	    DefaultScriptingContext tdsc =
		new DefaultScriptingContext("ECMAScript");
	    tdsc.evalScript("is = new java.io.FileInputStream(\"test2.js\")");
	    System.setSecurityManager(new SecurityManager());
	    try {
		dsc.setWriter(new OutputStreamWriter(System.out));
		System.out.println("writer was set unexpectedly");
	    } catch (Exception ee) {
		System.out.println(ee.getClass()
				   + " (security exception expected)");
	    }
		try {
		    dsc.evalScript
			("is = new java.io.FileInputStream(\"test1.js\")");
		    System.out.println("dsc evalScript unexpectedly succeeded "
				       + "for test1.js");
		} catch (Exception ee1) {
		    Throwable ec = ee1;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}
		try {
		    dsc.evalScript
			("is = new java.io.FileInputStream(\"test2.js\")");
		    System.out.println("dsc evalScript unexpectedly succeeded "
				       + "for test2.js");
		} catch (Exception ee3) {
		    Throwable ec = ee3;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}
		try {
		    InputStream is = new FileInputStream("test1.js");
		    is.close();
		    System.out.println("direct open worked");
		} catch (Exception de) {
		    System.out.println("direct open failed");
		    System.exit(1);
		}

		try {
		    tdsc.evalScript
			("is = new java.io.FileInputStream(\"test1.js\")");
		    System.out.println("file open succeeded as expected");
		} catch (Exception ee2) {
		    System.out.println("tsc file open unexpectedly failed");
		    System.out.println("(may be due to scripting engine)");
		    /*
		    if (ee2.getCause() != null) {
			ee2.getCause().printStackTrace(System.out);
		    } else {
			ee2.printStackTrace(System.out);
		    }
		    */
		}
		try {
		    tdsc.evalScript
			("is = new java.io.FileInputStream(\"test2.js\")");
		    System.out.println("tsc evalScript unexpectedly succeeded");
		} catch (Exception ee3) {
		    Throwable ec = ee3;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}

		try {
		    osc.testS("test1.js");
		    System.out.println
			("osc: file test1.js opened unexpectedly");
		} catch (Exception ee3) {
		    System.out.println
			("osc: testS(\"test1.js\") failed as expected");
		}
		try {
		    osc.testN("test1.js");
		    System.out.println("osc: testN(\"test1.js\") succeeded as "
				       +"expected");
		} catch (Exception ee3) {
		    System.out.println("osc: testN(\"test1.js\") failed "
				       + "to open unexpectedly");
		    System.out.println("(may be due to scripting engine)");
		    // ee3.printStackTrace(System.out);
		}
		try {
		    osc.testN("test2.js");
		    System.out.println
			("osc: file test2.js opened unexpectedly");
		} catch (Exception ee3) {
		    System.out.println
			("osc: testN(\"test2.js\") failed as expected");
		}
		/*
		try {
		    osc.testDSPE1();
		    osc.testDSP1();
		    osc.testDSPER1();
		    if (osc.testDSPR1() == null) {
			System.out.println("null return for testDSPR1 not "
					   + "expected");
		    }
		} catch (Exception e) {
		    System.out.println("doScriptPrivileged failure (test1.js)");
		}
		*/

		/*
		try {
		    osc.testDSPE2();
		    System.out.println("unexpected success: testDSPE2");
		} catch (Exception e) { }
		try {
		    osc.testDSP2();
		    System.out.println("expected success: testDSP2");
		} catch (Exception e) { }
		try {
		    osc.testDSPER2();
		    System.out.println("unexpected success: testDSPER2");
		} catch (Exception e) { }
		try {
		    if (osc.testDSPR2() != null)
		    System.out.println("unexpected success: testDSPR2");
		} catch (Exception e) { }
		*/
	    if (checkPolicy) {
		System.out.println("-- Constructing scripting contexts when "
				   + "a security manager is installed --");
		System.out.println("creating new dsc");
		dsc = new DefaultScriptingContext("ECMAScript");
		System.out.println("creating new tsc");
		tdsc = new DefaultScriptingContext("ECMAScript");
		System.out.println("creating new osc");
		osc = new OurScriptingContext(props);
		System.out.println("checking script evaluation ...");
		try {
		    dsc.evalScript
			("is = new java.io.FileInputStream(\"test1.js\")");
		    System.out.println("dsc evalScript unexpectedly succeeded "
				       + "for test1.js");
		} catch (Exception ee1) {
		    Throwable ec = ee1;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}
		try {
		    dsc.evalScript
			("is = new java.io.FileInputStream(\"test2.js\")");
		    System.out.println("dsc evalScript unexpectedly succeeded "
				       + "for test2.js");
		} catch (Exception ee3) {
		    Throwable ec = ee3;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}
		try {
		    tdsc.evalScript
			("is = new java.io.FileInputStream(\"test1.js\")");
		    System.out.println("file open succeeded as expected");
		} catch (Exception ee2) {
		    System.out.println("tsc file open unexpectedly failed");
		    System.out.println("(may be due to scripting engine)");
		    /*
		    if (ee2.getCause() != null) {
			ee2.getCause().printStackTrace(System.out);
		    } else {
			ee2.printStackTrace(System.out);
		    }
		    */
		}
		try {
		    tdsc.evalScript
			("is = new java.io.FileInputStream(\"test2.js\")");
		    System.out.println("tsc evalScript unexpectedly succeeded");
		} catch (Exception ee3) {
		    Throwable ec = ee3;
		    while (ec.getCause() != null) {ec = ec.getCause();}
		    System.out.println(ec.getClass()
				       + " - failed as expected");
		}

		try {
		    osc.testS("test1.js");
		    System.out.println
			("osc: file test1.js opened unexpectedly");
		} catch (Exception ee3) {
		    System.out.println
			("osc: testS(\"test1.js\") failed as expected");
		}
		try {
		    osc.testN("test1.js");
		    System.out.println("osc: testN(\"test1.js\") succeeded as "
				       +"expected");
		} catch (Exception ee3) {
		    System.out.println("osc: testN(\"test1.js\") failed "
				       + "to open unexpectedly");
		    System.out.println("(may be due to scripting engine)");
		    // ee3.printStackTrace(System.out);
		}
		try {
		    osc.testN("test2.js");
		    System.out.println
			("osc: file test2.js opened unexpectedly");
		} catch (Exception ee3) {
		    System.out.println
			("osc: testN(\"test2.js\") failed as expected");
		}
		try {
		    osc.testDSPE1();
		    osc.testDSP1();
		    osc.testDSPER1();
		    if (osc.testDSPR1() == null) {
			System.out.println("null return for testDSPR1 not "
					   + "expected");
		    }
		} catch (Exception e) {
		    System.out.println("doScriptPrivileged failure (test1.js)");
		}

		try {
		    osc.testDSPE2();
		    System.out.println("unexpected success: testDSPE2");
		} catch (Exception e) { }
		try {
		    osc.testDSP2();
		    System.out.println("expected success: testDSP2");
		} catch (Exception e) { }
		try {
		    osc.testDSPER2();
		    System.out.println("unexpected success: testDSPER2");
		} catch (Exception e) { }
		try {
		    if (osc.testDSPR2() != null)
		    System.out.println("unexpected success: testDSPR2");
		} catch (Exception e) { }
	    }

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	System.exit(0);
    }
}
