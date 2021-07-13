import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.DefaultScriptingContext;
import org.bzdev.scripting.ExtendedScriptingContext;
import java.io.*;

public class RVFTwoTest {

    public static void main(String argv[]) throws Exception {

	RealValuedFunctionTwo trigfunct1 = new RealValuedFunctionTwo() {
		public double valueAt(double x, double y) {
		    return Math.sin(x) * Math.cos(y);
		}
		public double deriv1At(double x, double y) {
		    return Math.cos(x) * Math.cos(y);
		}
		public double deriv2At(double x, double y) {
		    return -Math.sin(x) * Math.sin(y);
		}
		public double deriv11At(double x, double y) {
		    return -Math.sin(x) * Math.cos(y);
		}
		public double deriv12At(double x, double y) {
		    return -Math.cos(x) * Math.sin(y);
		}
		public double deriv21At(double x, double y) {
		    return -Math.cos(x) * Math.sin(y);
		}
		public double deriv22At(double x, double y) {
		    return -Math.sin(x) * Math.cos(y);
		}
	    };

	if ((trigfunct1.getDomainMin(0) != trigfunct1.getDomainMin1())
	    || (trigfunct1.getDomainMax(0) != trigfunct1.getDomainMax1())
	    || (trigfunct1.domainMinClosed(0) != trigfunct1.domainMin1Closed())
	    || (trigfunct1.domainMaxClosed(0)
		!= trigfunct1.domainMax1Closed())){
	    throw new Exception("domain min/max failure");
	}

	if ((trigfunct1.getDomainMin(1) != trigfunct1.getDomainMin2())
	    || (trigfunct1.getDomainMax(1) != trigfunct1.getDomainMax2())
	    || (trigfunct1.domainMinClosed(1) != trigfunct1.domainMin2Closed())
	    || (trigfunct1.domainMaxClosed(1)
		!= trigfunct1.domainMax2Closed())){
	    throw new Exception("domain min/max failure");
	}

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);
		double array[] = {theta, phi};
		if (trigfunct1.isInDomain(theta, phi) !=
		    trigfunct1.isInDomain(array)) {
		    throw new Exception("isInDomain mismatch");
		}
		if ((trigfunct1.valueAt(theta,phi) != trigfunct1.valueAt(array))
		    || (trigfunct1.derivAt(0,theta,phi) !=
			trigfunct1.deriv1At(theta,phi))
		    || (trigfunct1.derivAt(1,theta,phi) !=
			trigfunct1.deriv2At(theta,phi))
		    || (trigfunct1.secondDerivAt(0,0,theta,phi) !=
			trigfunct1.deriv11At(theta,phi))
		    || (trigfunct1.secondDerivAt(0,1,theta,phi) !=
			trigfunct1.deriv21At(theta,phi))
		    || (trigfunct1.secondDerivAt(1,0,theta,phi) !=
			trigfunct1.deriv21At(theta,phi))
		    || (trigfunct1.secondDerivAt(1,1,theta,phi) !=
			trigfunct1.deriv22At(theta,phi))) {
		    throw new Exception("value/deriv mismatch");
		}
	    }
	}


	if (trigfunct1.deriv(0).valueAt(1.0, 2.0)
	    != trigfunct1.deriv1At(1.0, 2.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv(1).valueAt(1.0, 2.0) !=
	    trigfunct1.deriv2At(1.0, 2.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(0, 0).valueAt(1.0, 2.0)
	    != trigfunct1.deriv11At(1.0, 2.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(0, 1).valueAt(1.0, 2.0)
	    != trigfunct1.deriv12At(1.0, 2.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(1, 0).valueAt(1.0, 2.0)
	    != trigfunct1.deriv21At(1.0, 2.0)) {
	    throw new Exception();
	}


	if (trigfunct1.secondDeriv(1, 1).valueAt(1.0, 2.0)
	    != trigfunct1.deriv22At(1.0, 2.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv1().valueAt(1.0, 2.0)
	    != trigfunct1.deriv1At(1.0, 2.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv2().valueAt(1.0, 2.0)
	    != trigfunct1.deriv2At(1.0, 2.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv11().valueAt(1.0, 2.0)
	    != trigfunct1.deriv11At(1.0, 2.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv12().valueAt(1.0, 2.0)
	    != trigfunct1.deriv12At(1.0, 2.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv21().valueAt(1.0, 2.0)
	    != trigfunct1.deriv21At(1.0, 2.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv22().valueAt(1.0, 2.0)
	    != trigfunct1.deriv11At(1.0, 2.0)) {
	    throw new Exception();
	}


	RealValuedFunctionTwo trigfunct2 = null;

	RealValuedFunctionTwo trigfunct3 = new
	    RealValuedFunctionTwo(null, trigfunct1);

	String language = (argv.length > 0 && argv[0].equals("esp"))?
	    "ESP": "ECMAScript";

	String filename = (argv.length > 0 && argv[0].equals("esp"))?
	    "rvftwotest.esp": "rvftwotest.js";

	ScriptingContext scripting = new DefaultScriptingContext(language);
	scripting = new ExtendedScriptingContext(scripting);
	scripting.putScriptObject("scripting", scripting);

	Reader reader = new InputStreamReader
	    (new FileInputStream(filename), "UTF-8");

	Object result = scripting.evalScript(reader);
	if (result instanceof RealValuedFunctionTwo) {
	    trigfunct2 = (RealValuedFunctionTwo)result;
	} else {
	    throw new Exception
		("rvftwotest.js did not produce a RealValuedFunctionTwo");
	}

	RealValuedFunctionTwo trigfunct4 = new
	    RealValuedFunctionTwo(scripting, "f", "f1", "f2",
			       "f11", "f12", "f21", "f22");

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi)
			     - trigfunct2.valueAt(theta, phi)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi)
					+ " \u2260 "
					+ trigfunct2.valueAt(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi)
			     - trigfunct2.deriv1At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv1At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi)
			     - trigfunct2.deriv2At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv2At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi)
			     - trigfunct2.deriv11At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv11At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi)
			     - trigfunct2.deriv12At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv12At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi)
			     - trigfunct2.deriv21At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv21At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi)
			     - trigfunct2.deriv22At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi)
					+ " \u2260 "
					+ trigfunct2.deriv22At(theta, phi));
		}
	    }
	}

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi)
			     - trigfunct3.valueAt(theta, phi)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi)
					+ " \u2260 "
					+ trigfunct3.valueAt(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi)
			     - trigfunct3.deriv1At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv1At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi)
			     - trigfunct3.deriv2At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv2At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi)
			     - trigfunct3.deriv11At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv11At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi)
			     - trigfunct3.deriv12At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv12At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi)
			     - trigfunct3.deriv21At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv21At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi)
			     - trigfunct3.deriv22At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi)
					+ " \u2260 "
					+ trigfunct3.deriv22At(theta, phi));
		}
	    }
	}

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi)
			     - trigfunct4.valueAt(theta, phi)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi)
					+ " \u2260 "
					+ trigfunct4.valueAt(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi)
			     - trigfunct4.deriv1At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv1At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi)
			     - trigfunct4.deriv2At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv2At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi)
			     - trigfunct4.deriv11At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv11At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi)
			     - trigfunct4.deriv12At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv12At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi)
			     - trigfunct4.deriv21At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv21At(theta, phi));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi)
			     - trigfunct4.deriv22At(theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi)
					+ " \u2260 "
					+ trigfunct4.deriv22At(theta, phi));
		}
	    }
	}

	// test domain min/max, etc.
	RealValuedFunctionTwo trigfunct5 = new RealValuedFunctionTwo() {
		public double valueAt(double x, double y) {
		    return Math.tan(x) * Math.tan(y);
		}
		public double getDomainMin1(){return -Math.PI/2.0;}
		public double getDomainMin2(){ return -Math.PI/2.0;}
		public boolean domainMin1Closed(){return false;}
		public boolean domainMin2Closed(){return false;}

		public double getDomainMax1(){return Math.PI/2.0;}
		public double getDomainMax2(){ return Math.PI/2.0;}
		public boolean domainMax1Closed(){return false;}
		public boolean domainMax2Closed(){return false;}
	    };

	double pi = Math.PI;
	double pi2 = Math.PI/2;
	double tests[][] = {
	    {1.0, 0.0},
	    {pi, 0.0},
	    {0.0, pi},
	    {pi2, 0.0},
	    {0.0, pi2},
	    {pi, pi}
	};

	boolean dresult = true;

	for (double[] pair: tests) {
	    if (trigfunct5.isInDomain(pair[0],pair[1]) != dresult) {
		System.out.format("inDomain failed on (%g, %g)\n",
				  pair[0], pair[1]);
		System.exit(1);
	    }
	    if (trigfunct5.isInDomain(pair) != dresult) {
		System.out.format("inDomain failed on array ({%g, %g})\n",
				  pair[0], pair[1]);
		System.exit(1);
	    }
	    dresult = false;
	}

	RealValuedFunctionTwo trigfunct6 =
	    new RealValuedFunctionTwo((x,y) -> Math.sin(x)*Math.cos(y));

	RealValuedFunctionTwo trigfunct7 =
	    new RealValuedFunctionTwo((x,y) -> Math.sin(x)*Math.cos(y),
				      (x,y) -> Math.cos(x)*Math.cos(y),
				      (x,y) -> -Math.sin(x)*Math.sin(y));

	RealValuedFunctionTwo trigfunct8 =
	    new RealValuedFunctionTwo
	    ((x,y) -> Math.sin(x)*Math.cos(y),
	     (x,y) -> Math.cos(x)*Math.cos(y),
	     (x,y) -> -Math.sin(x)*Math.sin(y),
	     (x,y)->-Math.sin(x)*Math.cos(y), (x,y)->-Math.cos(x)*Math.sin(y),
	     (x,y)->-Math.cos(x)*Math.sin(y), (x,y)->-Math.sin(x)*Math.cos(y));

	for (int i = 0; i < 100; i++) {
	    double x = (Math.PI/10)*i;
	    for (int j = 0; j < 100; j++) {
		double y = (Math.PI/10)*j;
		if (trigfunct1.valueAt(x,y) != trigfunct6.valueAt(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.valueAt(x,y) != trigfunct7.valueAt(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.valueAt(x,y) != trigfunct8.valueAt(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv1At(x,y) != trigfunct7.deriv1At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv2At(x,y) != trigfunct7.deriv2At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv1At(x,y) != trigfunct8.deriv1At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv2At(x,y) != trigfunct8.deriv2At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv11At(x,y) != trigfunct8.deriv11At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv12At(x,y) != trigfunct8.deriv12At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv21At(x,y) != trigfunct8.deriv21At(x,y)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv22At(x,y) != trigfunct8.deriv22At(x,y)) {
		    throw new Exception("mismatch");
		}
	    }
	}


	System.exit(0);
    }
}
