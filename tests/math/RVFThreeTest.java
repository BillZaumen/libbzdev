import org.bzdev.math.RealValuedFunctionThree;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.DefaultScriptingContext;
import org.bzdev.scripting.ExtendedScriptingContext;
import java.io.*;

public class RVFThreeTest {

    public static void main(String argv[]) throws Exception {

	RealValuedFunctionThree trigfunct1 = new RealValuedFunctionThree() {
		public double valueAt(double x, double y, double z) {
		    return Math.sin(x) * Math.cos(y) * z;
		}
		public double deriv1At(double x, double y, double z) {
		    return Math.cos(x) * Math.cos(y) * z;
		}
		public double deriv2At(double x, double y, double z) {
		    return -Math.sin(x) * Math.sin(y) * z;
		}
		public double deriv3At(double x, double y, double z) {
		    return Math.sin(x) * Math.cos(y);
		}
		public double deriv11At(double x, double y, double z) {
		    return -Math.sin(x) * Math.cos(y) * z;
		}
		public double deriv12At(double x, double y, double z) {
		    return -Math.cos(x) * Math.sin(y) * z;
		}
		public double deriv13At(double x, double y, double z) {
		    return Math.cos(x) * Math.cos(y);
		}
		public double deriv21At(double x, double y, double z) {
		    return -Math.cos(x) * Math.sin(y) * z;
		}
		public double deriv22At(double x, double y, double z) {
		    return -Math.sin(x) * Math.cos(y) * z;
		}
		public double deriv23At(double x, double y, double z) {
		    return -Math.sin(x) * Math.sin(y);
		}
		public double deriv31At(double x, double y, double z) {
		    return Math.cos(x) * Math.cos(y);
		}
		public double deriv32At(double x, double y, double z) {
		    return -Math.sin(x) * Math.sin(y);
		}
		public double deriv33At(double x, double y, double z) {
		    return 0.0;
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

	if ((trigfunct1.getDomainMin(2) != trigfunct1.getDomainMin3())
	    || (trigfunct1.getDomainMax(2) != trigfunct1.getDomainMax3())
	    || (trigfunct1.domainMinClosed(2) != trigfunct1.domainMin3Closed())
	    || (trigfunct1.domainMaxClosed(2)
		!= trigfunct1.domainMax3Closed())){
	    throw new Exception("domain min/max failure");
	}

	double z = 1.5;
	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);
		double array[] = {theta, phi, z};
		if (trigfunct1.isInDomain(theta, phi, z) !=
		    trigfunct1.isInDomain(array)) {
		    throw new Exception("isInDomain mismatch");
		}
		if ((trigfunct1.valueAt(theta,phi,z)
		     != trigfunct1.valueAt(array))
		    || (trigfunct1.derivAt(0,theta,phi,z) !=
			trigfunct1.deriv1At(theta,phi,z))
		    || (trigfunct1.derivAt(1,theta,phi,z) !=
			trigfunct1.deriv2At(theta,phi,z))
		    || (trigfunct1.derivAt(2,theta,phi,z) !=
			trigfunct1.deriv3At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(0,0,theta,phi,z) !=
			trigfunct1.deriv11At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(0,1,theta,phi,z) !=
			trigfunct1.deriv12At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(0,2,theta,phi,z) !=
			trigfunct1.deriv13At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(1,0,theta,phi,z) !=
			trigfunct1.deriv21At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(1,1,theta,phi,z) !=
			trigfunct1.deriv22At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(1,2,theta,phi,z) !=
			trigfunct1.deriv23At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(2,0,theta,phi,z) !=
			trigfunct1.deriv31At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(2,1,theta,phi,z) !=
			trigfunct1.deriv32At(theta,phi,z))
		    || (trigfunct1.secondDerivAt(2,2,theta,phi,z) !=
			trigfunct1.deriv33At(theta,phi,z))) {
		    throw new Exception("value/deriv mismatch");
		}
	    }
	}

	if (trigfunct1.deriv(0).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv1At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv(1).valueAt(1.0, 2.0, 3.0) !=
	    trigfunct1.deriv2At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv(2).valueAt(1.0, 2.0, 3.0) !=
	    trigfunct1.deriv3At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}


	if (trigfunct1.secondDeriv(0, 0).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv11At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(0, 1).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv12At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(0, 2).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv13At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(1, 0).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv21At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(1, 1).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv22At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(1, 2).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv23At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(2, 0).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv31At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(2, 1).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv32At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.secondDeriv(2, 2).valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv33At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}


	if (trigfunct1.deriv1().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv1At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv2().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv2At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv3().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv3At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	if (trigfunct1.deriv11().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv11At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv12().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv12At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv13().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv13At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv21().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv21At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv22().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv22At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv23().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv23At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv31().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv31At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv32().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv32At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}
	if (trigfunct1.deriv33().valueAt(1.0, 2.0, 3.0)
	    != trigfunct1.deriv33At(1.0, 2.0, 3.0)) {
	    throw new Exception();
	}

	RealValuedFunctionThree trigfunct2 = null;

	RealValuedFunctionThree trigfunct3 = new
	    RealValuedFunctionThree(null, trigfunct1);

	String language = (argv.length > 0 && argv[0].equals("esp"))?
	    "ESP": "ECMAScript";

	String filename = (argv.length > 0 && argv[0].equals("esp"))?
	    "rvfthreetest.esp": "rvfthreetest.js";



	ScriptingContext scripting = new DefaultScriptingContext(language);
	scripting = new ExtendedScriptingContext(scripting);
	scripting.putScriptObject("scripting", scripting);

	Reader reader = new InputStreamReader
	    (new FileInputStream(filename), "UTF-8");

	Object result = scripting.evalScript(reader);
	if (result instanceof RealValuedFunctionThree) {
	    trigfunct2 = (RealValuedFunctionThree)result;
	} else {
	    throw new Exception
		("rvftwotest.js did not produce a RealValuedFunctionThree");
	}

	RealValuedFunctionThree trigfunct4 = new
	    RealValuedFunctionThree(scripting, "f", "f1", "f2", "f3",
				    "f11", "f12", "f13",
				    "f21", "f22", "f23",
				    "f31", "f32", "f33");

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi, z)
			     - trigfunct2.valueAt(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.valueAt(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi, z)
			     - trigfunct2.deriv1At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv1At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi, z)
			     - trigfunct2.deriv2At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv2At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv3At(theta, phi, z)
			     - trigfunct2.deriv3At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv3At: "
					+ trigfunct1.deriv3At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv3At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi, z)
			     - trigfunct2.deriv11At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv11At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi, z)
			     - trigfunct2.deriv12At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv12At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv13At(theta, phi, z)
			     - trigfunct2.deriv13At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv13At: "
					+ trigfunct1.deriv13At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv13At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi, z)
			     - trigfunct2.deriv21At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv21At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi, z)
			     - trigfunct2.deriv22At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv22At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv23At(theta, phi, z)
			     - trigfunct2.deriv23At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv23At: "
					+ trigfunct1.deriv23At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv23At(theta, phi, z));
		}

		if (Math.abs(trigfunct1.deriv31At(theta, phi, z)
			     - trigfunct2.deriv31At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv31At: "
					+ trigfunct1.deriv31At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv31At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv32At(theta, phi, z)
			     - trigfunct2.deriv32At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv32At: "
					+ trigfunct1.deriv32At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv32At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv33At(theta, phi, z)
			     - trigfunct2.deriv33At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv33At: "
					+ trigfunct1.deriv33At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct2.deriv33At(theta, phi, z));
		}
	    }
	}

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi, z)
			     - trigfunct3.valueAt(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.valueAt(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi, z)
			     - trigfunct3.deriv1At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv1At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi, z)
			     - trigfunct3.deriv2At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv2At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv3At(theta, phi, z)
			     - trigfunct3.deriv3At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv3At: "
					+ trigfunct1.deriv3At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv3At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi, z)
			     - trigfunct3.deriv11At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv11At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi, z)
			     - trigfunct3.deriv12At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv12At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv13At(theta, phi, z)
			     - trigfunct3.deriv13At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv13At: "
					+ trigfunct1.deriv13At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv13At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi, z)
			     - trigfunct3.deriv21At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv21At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi, z)
			     - trigfunct3.deriv22At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv22At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv23At(theta, phi, z)
			     - trigfunct3.deriv23At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv23At: "
					+ trigfunct1.deriv23At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv23At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv31At(theta, phi, z)
			     - trigfunct3.deriv31At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv31At: "
					+ trigfunct1.deriv31At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv31At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv32At(theta, phi, z)
			     - trigfunct3.deriv32At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv32At: "
					+ trigfunct1.deriv32At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv32At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv33At(theta, phi, z)
			     - trigfunct3.deriv33At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv33At: "
					+ trigfunct1.deriv33At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct3.deriv33At(theta, phi, z));
		}
	    }
	}

	for (int i = 0; i < 360; i += 10) {
	    double x = i;
	    double theta = Math.toRadians(x);
	    for (int j = 0; j < 360; j += 10) {
		double y = i;
		double phi = Math.toRadians(y);

		if (Math.abs(trigfunct1.valueAt(theta, phi, z)
			     - trigfunct4.valueAt(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("values: "
					+ trigfunct1.valueAt(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.valueAt(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv1At(theta, phi, z)
			     - trigfunct4.deriv1At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.deriv1At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv1At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv2At(theta, phi, z)
			     - trigfunct4.deriv2At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.deriv2At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv2At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv3At(theta, phi, z)
			     - trigfunct4.deriv3At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv3At: "
					+ trigfunct1.deriv3At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv3At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv11At(theta, phi, z)
			     - trigfunct4.deriv11At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv11At: "
					+ trigfunct1.deriv11At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv11At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv12At(theta, phi, z)
			     - trigfunct4.deriv12At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv12At: "
					+ trigfunct1.deriv12At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv12At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv13At(theta, phi, z)
			     - trigfunct4.deriv13At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv13At: "
					+ trigfunct1.deriv13At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv13At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv21At(theta, phi, z)
			     - trigfunct4.deriv21At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv21At: "
					+ trigfunct1.deriv21At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv21At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv22At(theta, phi, z)
			     - trigfunct4.deriv22At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv22At: "
					+ trigfunct1.deriv22At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv22At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv23At(theta, phi, z)
			     - trigfunct4.deriv23At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv23At: "
					+ trigfunct1.deriv23At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv23At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv31At(theta, phi, z)
			     - trigfunct4.deriv31At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv31At: "
					+ trigfunct1.deriv31At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv31At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv32At(theta, phi, z)
			     - trigfunct4.deriv32At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv32At: "
					+ trigfunct1.deriv32At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv32At(theta, phi, z));
		}
		if (Math.abs(trigfunct1.deriv33At(theta, phi, z)
			     - trigfunct4.deriv33At(theta, phi, z)) >= 1.e-12) {
		    throw new Exception("deriv33At: "
					+ trigfunct1.deriv33At(theta, phi, z)
					+ " \u2260 "
					+ trigfunct4.deriv33At(theta, phi, z));
		}
	    }
	}

	// test domain min/max, etc.
	RealValuedFunctionThree trigfunct5 = new RealValuedFunctionThree() {
		public double valueAt(double x, double y, double z) {
		    return Math.tan(x) * Math.tan(y) * z;
		}
		public double getDomainMin1(){return -Math.PI/2.0;}
		public double getDomainMin2(){ return -Math.PI/2.0;}
		public double getDomainMin3(){ return 0.0;}
		public boolean domainMin1Closed(){return false;}
		public boolean domainMin2Closed(){return false;}
		public boolean domainMin3Closed(){return true;}

		public double getDomainMax1(){return Math.PI/2.0;}
		public double getDomainMax2(){ return Math.PI/2.0;}
		public double getDomainMax3(){ return Double.MAX_VALUE;}
		public boolean domainMax1Closed(){return false;}
		public boolean domainMax2Closed(){return false;}
		public boolean domainMax3Closed(){return true;}
	    };

	double pi = Math.PI;
	double pi2 = Math.PI/2;
	double tests[][] = {
	    {1.0, 0.0, 1.0},
	    {pi, 0.0, 1.0},
	    {0.0, pi, 1.0},
	    {pi2, 0.0, 0.0},
	    {0.0, pi2, 1.0},
	    {pi, pi, 1.0},
	    {1.0, 0.0, -1.0}
	};

	boolean dresult = true;

	for (double[] triplet: tests) {
	    if (trigfunct5.isInDomain(triplet[0],triplet[1], triplet[2])
		!= dresult) {
		System.out.format("inDomain failed on (%g, %g, %g)\n",
				  triplet[0], triplet[1], triplet[2]);
		System.exit(1);
	    }
	    if (trigfunct5.isInDomain(triplet) != dresult) {
		System.out.format("inDomain failed on array ({%g, %g, %g})\n",
				  triplet[0], triplet[1], triplet[2]);
		System.exit(1);
	    }
	    dresult = false;
	}

	RealValuedFunctionThree trigfunct6 =
	    new RealValuedFunctionThree((x,y,zz) -> Math.sin(x)*Math.cos(y)*zz);

	RealValuedFunctionThree trigfunct7 =
	    new RealValuedFunctionThree
	    ((x,y,zz) -> Math.sin(x)*Math.cos(y)*zz,
	     (x,y,zz) -> Math.cos(x)*Math.cos(y)*zz,
	     (x,y,zz) -> -Math.sin(x)*Math.sin(y)*zz,
	     (x,y,zz) -> Math.sin(x)*Math.cos(y));

	RealValuedFunctionThree trigfunct8 =
	    new RealValuedFunctionThree
	    ((x,y,zz) -> Math.sin(x)*Math.cos(y)*zz,
	     (x,y,zz) -> Math.cos(x)*Math.cos(y)*zz,
	     (x,y,zz) -> -Math.sin(x)*Math.sin(y)*zz,
	     (x,y,zz) -> Math.sin(x)*Math.cos(y),

	     (x,y,zz) -> -Math.sin(x)*Math.cos(y)*zz,
	     (x,y,zz) -> -Math.cos(x)*Math.sin(y)*zz,
	     (x,y,zz) -> Math.cos(x)*Math.cos(y),

	     (x,y,zz) -> -Math.cos(x)*Math.sin(y)*zz,
	     (x,y,zz) -> -Math.sin(x)*Math.cos(y)*zz,
	     (x,y,zz) -> -Math.sin(x)*Math.sin(y),

	     (x,y,zz) -> Math.cos(x)*Math.cos(y),
	     (x,y,zz) -> -Math.sin(x)*Math.sin(y),
	     (x,y,zz) -> 0.0);

	for (int i = 0; i < 100; i++) {
	    double x = (Math.PI/10)*i;
	    for (int j = 0; j < 100; j++) {
		double y = (Math.PI/10)*j;
		if (trigfunct1.valueAt(x,y,z) != trigfunct6.valueAt(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.valueAt(x,y,z) != trigfunct7.valueAt(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.valueAt(x,y,z) != trigfunct8.valueAt(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv1At(x,y,z) != trigfunct7.deriv1At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv2At(x,y,z) != trigfunct7.deriv2At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv3At(x,y,z) != trigfunct7.deriv3At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv1At(x,y,z) != trigfunct8.deriv1At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv2At(x,y,z) != trigfunct8.deriv2At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv3At(x,y,z) != trigfunct8.deriv3At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv11At(x,y,z)
		    != trigfunct8.deriv11At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv12At(x,y,z)
		    != trigfunct8.deriv12At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv13At(x,y,z)
		    != trigfunct8.deriv13At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv21At(x,y,z)
		    != trigfunct8.deriv21At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv22At(x,y,z)
		    != trigfunct8.deriv22At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv23At(x,y,z)
		    != trigfunct8.deriv23At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv31At(x,y,z)
		    != trigfunct8.deriv31At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv32At(x,y,z)
		    != trigfunct8.deriv32At(x,y,z)) {
		    throw new Exception("mismatch");
		}
		if (trigfunct1.deriv33At(x,y,z)
		    != trigfunct8.deriv33At(x,y,z)) {
		    throw new Exception("mismatch");
		}
	    }
	}
	System.exit(0);
    }
}
