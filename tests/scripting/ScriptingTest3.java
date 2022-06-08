import org.bzdev.scripting.*;
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


public class ScriptingTest3 {

    static class OurScriptingContext extends ScriptingContext {
	Properties properties;
	OurScriptingContext(Properties properties, ScriptingContext parent) {
	    super(parent);
	    this.properties = properties;
	}
	public Object test(Object x, Object y) throws ScriptException {
	    return invokePrivateFunction(properties,
					 "test",
					 x, y);
	    /*
	    return invokePrivateFunction(properties,
					 ScriptingContext.PFMode.PRIVILEGED,
					 "test",
					 x, y);
	    */
	}
    }

    public static void main(String argv[]) {
	try {
	    Properties props = new Properties();
	    props.setProperty("ECMAScript",
			      "({test: function(x,y) {return x + y}})");
	    props.setProperty("ESP",
			      "{test: function(x,y) {return x + y}}");
	    // ScriptingContext dfsc=new DefaultScriptingContext("ECMAScript");
	    ScriptingContext dfsc = new DefaultScriptingContext("ESP");
	    ExtendedScriptingContext efsc = new ExtendedScriptingContext(dfsc);
	    dfsc.putScriptObject("scripting", dfsc);
	    dfsc.evalScript("import(org.bzdev.drama.DramaSimulation)");
	    dfsc.evalScript("import(org.bzdev.math.rv.GaussianRV)");
	    dfsc.evalScript("import(org.bzdev.math.rv.FixedIntegerRV)");
	    dfsc.evalScript("import(org.bzdev.math.rv.FixedLongRV)");
	    dfsc.evalScript("import(org.bzdev.math.rv.FixedBooleanRV)");
	    if (dfsc != dfsc.getScriptObject("scripting")) {
		System.out.println("problem with get/put for script objects");
		System.exit(1);
	    }
	    // dfsc.evalScript("global.getWriter().println(scripting.getClass().getName())");
	    dfsc.evalScript("global.getWriter().println(global.typeof(scripting))");
	    ScriptingContext dsc = (ScriptingContext) dfsc.evalScript
		("new org.bzdev.drama.DramaSimulation(scripting)");
	    System.out.println(dsc.getClass().getName());
	    try {
		OurScriptingContext sc = new OurScriptingContext(props, dsc);
		sc.putScriptObject("result", sc.test(10.0, 20.0));
		sc.evalScript("global.getWriter().println(result)");
	    } catch (Exception e) {
		e.printStackTrace(System.out);
	    }
	    System.out.println("---- now try efsc -----");
	    efsc.putScriptObject("scripting", efsc);
	    Object dsim = efsc.evalScript
		("scripting.create(\"org.bzdev.drama.DramaSimulation\", "
		 + "scripting)");
	    dsc = (ScriptingContext) dsim;
	    System.out.println(dsim.getClass().getName());
	    try {
		OurScriptingContext sc = new OurScriptingContext(props, dsc);
		sc.putScriptObject("result", sc.test(10.0, 20.0));
		sc.evalScript("global.getWriter().println(result)");
	    } catch (Exception e) {
		e.printStackTrace(System.out);
	    }
	    org.bzdev.math.rv.GaussianRV grv = (org.bzdev.math.rv.GaussianRV)
		efsc.evalScript("new org.bzdev.math.rv.GaussianRV(5.0, 2.0)");
	    System.out.println("grv mean = " + grv.getMean()
			       + ", sdev = " + grv.getSDev());
	    grv = (org.bzdev.math.rv.GaussianRV) efsc.evalScript
		("scripting.create(\"org.bzdev.math.rv.GaussianRV\", 5.0, 2.0)");
	    System.out.println("grv mean = " + grv.getMean()
			       + ", sdev = " + grv.getSDev());
	    org.bzdev.math.rv.FixedIntegerRV firv =
		(org.bzdev.math.rv.FixedIntegerRV) efsc.evalScript
		("scripting.create(\"org.bzdev.math.rv.FixedIntegerRV\", 10)");
	    System.out.println("firv.next() = " + firv.next());
	    org.bzdev.math.rv.FixedLongRV flrv =
		(org.bzdev.math.rv.FixedLongRV) efsc.evalScript
		("scripting.create(\"org.bzdev.math.rv.FixedLongRV\", 20)");
	    System.out.println("flrv.next() = " + flrv.next());
	    org.bzdev.math.rv.FixedBooleanRV fbrv =
		(org.bzdev.math.rv.FixedBooleanRV) efsc.evalScript
		("scripting.create(\"org.bzdev.math.rv.FixedBooleanRV\", true)");
	    System.out.println("fbrv.next() = " + fbrv.next());

	    System.out.println("----- test createArray -----");
	    Object object = efsc.evalScript
		("scripting.createArray(java.lang.Double.class, 5)");
	    System.out.println("created " + object.getClass());
	    /*
	    object = efsc.evalScript
		("scripting.createArray(java.lang.Double.TYPE, 5)");
	    System.out.println("created " + object.getClass());
	    */
	    object = efsc.evalScript
		("scripting.createArray(\"java.lang.Double\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"double\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"float\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"int\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(java.lang.Long.class, 5)");
	    System.out.println("created " + object.getClass());
	    /*
	    object = efsc.evalScript
		("scripting.createArray(java.lang.Long.TYPE, 5)");
	    System.out.println("created " + object.getClass());
	    */
	    object = efsc.evalScript
		("scripting.createArray(\"long\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"short\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"char\", 5)");
	    System.out.println("created " + object.getClass());
	    object = efsc.evalScript
		("scripting.createArray(\"byte\", 5)");
	    System.out.println("created " + object.getClass());

	    System.out.println("----- test createAndInitArray -----");

	    Double[] dcaObject = (Double[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Double.class, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("dcaObject[0] = " + dcaObject[0]);
	    /*
	    double[] daObject = (double[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Double.TYPE, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("daObject[0] = " + daObject[0]);
	    */
	    dcaObject = (Double[])efsc.evalScript
		("scripting.createAndInitArray(\"java.lang.Double\","
		 + "1.0, 2.0, 3.0)");
	    System.out.println("dcaObject[0] = " + dcaObject[0]);
	    double[] daObject = (double[])efsc.evalScript
		("scripting.createAndInitArray(\"double\", 1.0, 2.0, 3.0)");
	    System.out.println("daObject[0] = " + daObject[0]);

	    Integer[] icaObject = (Integer[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Integer.class, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("icaObject[0] = " + icaObject[0]);
	    /*
	    int[] iaObject = (int[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Integer.TYPE, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("iaObject[0] = " + iaObject[0]);
	    */
	    icaObject = (Integer[])efsc.evalScript
		("scripting.createAndInitArray(\"java.lang.Integer\","
		 + "1.0, 2.0, 3.0)");
	    System.out.println("icaObject[0] = " + icaObject[0]);
	    int[] iaObject = (int[])efsc.evalScript
		("scripting.createAndInitArray(\"int\", 1.0, 2.0, 3.0)");
	    System.out.println("iaObject[0] = " + iaObject[0]);

	    Long[] lcaObject = (Long[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Long.class, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("lcaObject[0] = " + lcaObject[0]);
	    /*
	    long[] laObject = (long[])efsc.evalScript
		("scripting.createAndInitArray(java.lang.Long.TYPE, "
		 + "1.0, 2.0, 3.0)");
	    System.out.println("laObject[0] = " + laObject[0]);
	    */
	    lcaObject = (Long[])efsc.evalScript
		("scripting.createAndInitArray(\"java.lang.Long\","
		 + "1.0, 2.0, 3.0)");
	    System.out.println("lcaObject[0] = " + lcaObject[0]);
	    long[] laObject = (long[])efsc.evalScript
		("scripting.createAndInitArray(\"long\", 1.0, 2.0, 3.0)");
	    System.out.println("laObject[0] = " + laObject[0]);

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.exit(1);
	}
	System.exit(0);
    }
}
