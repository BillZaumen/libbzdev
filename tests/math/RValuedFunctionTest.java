import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.DefaultScriptingContext;
import org.bzdev.scripting.ExtendedScriptingContext;
import java.io.*;
import java.util.function.DoubleUnaryOperator;

public class RValuedFunctionTest {

    public static void main(String argv[]) throws Exception {

	RealValuedFunction trigfunct1 = new RealValuedFunction() {
		public double valueAt(double x) {
		    return Math.sin(x);
		}
		public double derivAt(double x) {
		    return Math.cos(x);
		}
		public double secondDerivAt(double x) {
		    return -Math.sin(x);
		}
	    };

	if (trigfunct1.valueAt(1.0) != trigfunct1.applyAsDouble(1.0)) {
	    throw new Exception("applyAsDouble");
	}

	RealValuedFunctOps f2 = (t) -> {return t*t;};
	RealValuedFunctOps f3 = trigfunct1.compose(f2);
	if (f3.valueAt(0.5) != trigfunct1.valueAt(f2.valueAt(0.5))) {
	    throw new Exception("compose");
	}
	RealValuedFunctOps f4 = trigfunct1.andThen(f2);
	if (f4.valueAt(0.5) != f2.valueAt(trigfunct1.valueAt(0.5))) {
	    throw new Exception("andThen");
	}

	DoubleUnaryOperator op2  = (t) -> {return t*t;};
	DoubleUnaryOperator f3a = trigfunct1.compose(op2);
	if (f3a.applyAsDouble(0.5) != f3.valueAt(0.5)) {
	    throw new Exception("compose DoubleUnaryOps");
	}

	DoubleUnaryOperator f4a = trigfunct1.andThen(op2);
	if (f4a.applyAsDouble(0.5) != f4.valueAt(0.5)) {
	    throw new Exception("andThen DoubleUnaryOps");
	}

	DoubleUnaryOperator op3 = op2.compose(trigfunct1);
	if (op3.applyAsDouble(0.5)
	    != op2.applyAsDouble(trigfunct1.valueAt(0.5))) {
	    throw new Exception("compose RealValuedFunctOps");
	}

	if (op3.applyAsDouble(0.5)
	    != op2.applyAsDouble(trigfunct1.applyAsDouble(0.5))) {
	    throw new Exception("compose RealValuedFunctOps");
	}

	if (trigfunct1.deriv(0).valueAt(1.0) != trigfunct1.valueAt(1.0)) {
	    throw new Exception("deriv(1)");
	}

	if (trigfunct1.deriv().valueAt(1.0) != trigfunct1.derivAt(1.0)) {
	    throw new Exception("deriv()");
	}

	if (trigfunct1.deriv(1).valueAt(1.0) != trigfunct1.derivAt(1.0)) {
	    throw new Exception("deriv(1)");
	}

	if (trigfunct1.secondDeriv().valueAt(1.0)
	    != trigfunct1.secondDerivAt(1.0)) {
	    throw new Exception("deriv()");
	}

	if (trigfunct1.deriv(2).valueAt(1.0) != trigfunct1.secondDerivAt(1.0)) {
	    throw new Exception("deriv(2)");
	}

	// check lambda expresion
	RealValuedFunctOps p3 = (t) -> {return t*t*t;};
	if (Math.abs(p3.valueAt(2.0) - 8) > 1.e-12) {
	    throw new Exception("p3 failed");
	}

	RealValuedFunction trigfunct2 = null;

	RealValuedFunction trigfunct3 = new
	    RealValuedFunction(null, trigfunct1);

	String language = (argv.length > 0 && argv[0].equals("esp"))?
	    "ESP": "ECMAScript";

	String filename = (argv.length > 0 && argv[0].equals("esp"))?
	    "rvftest.esp": "rvftest.js";



	ScriptingContext scripting = new DefaultScriptingContext(language);
	scripting = new ExtendedScriptingContext(scripting);
	scripting.putScriptObject("scripting", scripting);

	Reader reader = new InputStreamReader
	    (new FileInputStream(filename), "UTF-8");

	Object result = scripting.evalScript(reader);
	if (result instanceof RealValuedFunction) {
	    trigfunct2 = (RealValuedFunction)result;
	} else {
	    throw new Exception
		("rvftest.js did not produce a RealValuedFunction");
	}

	RealValuedFunction trigfunct4 = new
	    RealValuedFunction(scripting, "f", "fp", "fpp");

	Object s = scripting.evalScript("\"foo\"");
	if (!(s instanceof String)) {
	    System.out.println("typeof s = " + s.getClass());
	}


	if ((trigfunct1.getDomainMin(0) != trigfunct1.getDomainMin())
	    || (trigfunct1.getDomainMax(0) != trigfunct1.getDomainMax())
	    || (trigfunct1.domainMinClosed(0) != trigfunct1.domainMinClosed())
	    || (trigfunct1.domainMaxClosed(0) != trigfunct1.domainMaxClosed())){
	    throw new Exception("domain min/max failure");
	}

	for (int i = 0; i < 360; i++) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    double array[] = {theta};

	    if (trigfunct1.isInDomain(theta)
		!= trigfunct1.isInDomain(array)) {
		throw new Exception("isInDomain mismatch");
	    }
	    if ((trigfunct1.valueAt(theta) != trigfunct1.valueAt(array))
		|| (trigfunct1.derivAt(theta) != trigfunct1.derivAt(0,theta))
		|| (trigfunct1.secondDerivAt(theta)
		    != trigfunct1.secondDerivAt(0,0,theta))) {
		throw new Exception("valueAt/derivAt/secondDerivAt failure");
	    }
	}

	for (int i = 0; i < 360; i++) {
	    double x = i;
	    double theta = Math.toRadians(x);

	    if (Math.abs(trigfunct1.valueAt(theta)
			 - trigfunct2.valueAt(theta)) >= 1.e-12) {
		throw new Exception("values: "
				   + trigfunct1.valueAt(theta)
				   + " \u2260 "
				   + trigfunct2.valueAt(theta));
	    }
	    if (Math.abs(trigfunct1.derivAt(theta)
			 - trigfunct2.derivAt(theta)) >= 1.e-12) {
		throw new Exception("derivatives: "
				   + trigfunct1.derivAt(theta)
				   + " \u2260 "
				   + trigfunct2.derivAt(theta));
	    }

	    if (Math.abs(trigfunct1.secondDerivAt(theta)
			 - trigfunct2.secondDerivAt(theta)) >= 1.e-12) {
		throw new Exception("second derivatives: "
				   + trigfunct1.secondDerivAt(theta)
				   + " \u2260 "
				   + trigfunct2.secondDerivAt(theta));
	    }
	}

	for (int i = 0; i < 360; i++) {
	    double x = i;
	    double theta = Math.toRadians(x);

	    if (Math.abs(trigfunct1.valueAt(theta)
			 - trigfunct3.valueAt(theta)) >= 1.e-12) {
		throw new Exception("values: "
				   + trigfunct1.valueAt(theta)
				   + " \u2260 "
				   + trigfunct3.valueAt(theta));
	    }
	    if (Math.abs(trigfunct1.derivAt(theta)
			 - trigfunct3.derivAt(theta)) >= 1.e-12) {
		throw new Exception("derivatives: "
				   + trigfunct1.derivAt(theta)
				   + " \u2260 "
				   + trigfunct3.derivAt(theta));
	    }

	    if (Math.abs(trigfunct1.secondDerivAt(theta)
			 - trigfunct3.secondDerivAt(theta)) >= 1.e-12) {
		throw new Exception("second derivatives: "
				   + trigfunct1.secondDerivAt(theta)
				   + " \u2260 "
				   + trigfunct3.secondDerivAt(theta));
	    }
	}

	for (int i = 0; i < 360; i++) {
	    double x = i;
	    double theta = Math.toRadians(x);

	    if (Math.abs(trigfunct1.valueAt(theta)
			 - trigfunct4.valueAt(theta)) >= 1.e-12) {
		throw new Exception("values: "
				   + trigfunct1.valueAt(theta)
				   + " \u2260 "
				   + trigfunct4.valueAt(theta));
	    }
	    if (Math.abs(trigfunct1.derivAt(theta)
			 - trigfunct4.derivAt(theta)) >= 1.e-12) {
		throw new Exception("derivatives: "
				   + trigfunct1.derivAt(theta)
				   + " \u2260 "
				   + trigfunct4.derivAt(theta));
	    }

	    if (Math.abs(trigfunct1.secondDerivAt(theta)
			 - trigfunct4.secondDerivAt(theta)) >= 1.e-12) {
		throw new Exception("second derivatives: "
				   + trigfunct1.secondDerivAt(theta)
				   + " \u2260 "
				   + trigfunct4.secondDerivAt(theta));
	    }
	}

	RealValuedFunction trigfunct5 = new RealValuedFunction() {
		public double valueAt(double x) {
		    return Math.tan(x);
		}
		public double getDomainMin(){return -Math.PI/2.0;}
		public double getDomainMax() {return Math.PI/2.0;}
		public boolean domainMinClosed() {return false;}
		public boolean domainMaxClosed() {return false;}
	    };

	double pi = Math.PI;
	double pi2 = pi/2.0;

	double[] args = {0.0, pi, -pi, pi2, -pi2};
	boolean dresult = true;
	for (double x: args) {
	    if (trigfunct5.isInDomain(x) != dresult) {
		throw new Exception("trigfunct5.isInDomain(" + x + ") failed");
	    }
	    double array[] = {x};
	    if (trigfunct5.isInDomain(array) != dresult) {
		throw new Exception("trigfunct5.isInDomain(" + x + ") failed");
	    }
	    dresult = false;
	}

	RealValuedFunction trigfunct6 =
	    new RealValuedFunction(x -> Math.sin(x),
				   x -> Math.cos(x),
				   x -> -Math.sin(x));

	RealValuedFunction trigfunct7 =
	    new RealValuedFunction(x -> Math.sin(x));

	for (int i = 0; i < 1000; i++) {
	    double x = i * Math.PI / 100.0;
	    if (Math.abs(trigfunct1.valueAt(x) - trigfunct6.valueAt(x))
		> 1.e-15) {
		throw new Exception("trigfunct1 and tirgfunct6 differ");
	    }
	    if (Math.abs(trigfunct1.valueAt(x) - trigfunct7.valueAt(x))
		> 1.e-15) {
		throw new Exception("trigfunct1 and tirgfunct6 differ");
	    }
	    if (Math.abs(trigfunct1.derivAt(x) - trigfunct6.derivAt(x))
		> 1.e-15) {
		throw new Exception("trigfunct1 and tirgfunct6 derivs differ");
	    }
	    if (Math.abs(trigfunct1.secondDerivAt(x)
			 - trigfunct6.secondDerivAt(x)) > 1.e-15) {
		throw new Exception
		    ("trigfunct1 and tirgfunct6 second derivs differ");
	    }

	}


	System.exit(0);
    }
}
