import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctionVA;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.scripting.DefaultScriptingContext;
import org.bzdev.scripting.ExtendedScriptingContext;
import java.io.*;

public class RVFVATest {

    static RealValuedFunction f1 = new RealValuedFunction() {
	    public double getDomainMin() {return -1000.0;}
	    public double getDomainMax() {return 1000.0;}
	    public boolean domainMinClosed() {return false;}
	    public boolean domainMaxClosed() {return false;}
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

    static RealValuedFunction f2 = new RealValuedFunction() {
	    public double getDomainMin() {return -100.0;}
	    public double getDomainMax() {return 100.0;}
	    public boolean domainMinClosed() {return true;}
	    public boolean domainMaxClosed() {return false;}

	    public double valueAt(double x) {
		return Math.sin(2*x);
	    }

	    public double derivAt(double x) {
		return 2*Math.cos(2*x);
	    }

	    public double secondDerivAt(double x) {
		return -4*Math.sin(2*x);
	    }
	};

    static RealValuedFunction f3 = new RealValuedFunction() {
	    public double getDomainMin() {return -100.0;}
	    public double getDomainMax() {return 100.0;}
	    public boolean domainMinClosed() {return false;}
	    public boolean domainMaxClosed() {return true;}

	    public double valueAt(double x) {
		return Math.sin(3*x);
	    }

	    public double derivAt(double x) {
		return 3*Math.cos(3*x);
	    }

	    public double secondDerivAt(double x) {
		return -9*Math.sin(3*x);
	    }
	};

    static double f(double x, double a, double b, double c) {
	return a*Math.sin(x) + b*Math.sin(2*x) + c*Math.sin(3*x);
    }
    static double fp(double x, double a, double b, double c) {
	return a*Math.cos(x) + 2*b*Math.cos(2*x) + 3*c*Math.cos(3*x);
    }
    static double fpp(double x, double a, double b, double c) {
	return -a*Math.sin(x) - 4*b*Math.sin(2*x) - 9*c*Math.sin(3*x);
    }

    static RealValuedFunctionVA fva = new RealValuedFunctionVA.Linear(f1,f2,f3);


    public static void main(String argv[]) throws Exception {

	RealValuedFunctionVA trigfunct1 = new RealValuedFunctionVA(2,2) {
		@Override
		public double valueAt(double... args) {
		    double x = args[0];
		    double y = args[1];
		    return Math.sin(x) * Math.cos(y);
		}
		@Override
		public double derivAt(int i, double... args) {
		    double x = args[0];
		    double y = args[1];
		    if (i == 0) {
			return Math.cos(x) * Math.cos(y);
		    } else if (i == 1) {
			return -Math.sin(x) * Math.cos(y);
		    }
		    throw new IllegalArgumentException("i out of range");
		}
		@Override
		public double secondDerivAt(int i, int j, double... args) {
		    double x = args[0];
		    double y = args[1];
		    if (i == 0 && j == 0) {
			return -Math.sin(x) * Math.cos(y);
		    } else if (i == 0 && j == 1) {
			return -Math.cos(x) * Math.sin(y);
		    } else if (i == 1 && j == 0) {
			return -Math.cos(x) * Math.sin(y);
		    } else if (i == 1 && j == 1) {
			return -Math.sin(x) * Math.cos(y);
		    }
		    throw new IllegalArgumentException("i or j out of range");
		}
	    };
	RealValuedFunctionVA trigfunct2 = null;

	RealValuedFunctionVA trigfunct3 = new
	    RealValuedFunctionVA(2,2, null, trigfunct1);

	String language = (argv.length > 0 && argv[0].equals("esp"))?
	    "ESP": "ECMAScript";

	String filename = (argv.length > 0 && argv[0].equals("esp"))?
	    "rvfvatest.esp": "rvfvatest.js";


	ScriptingContext scripting = new DefaultScriptingContext(language);
	scripting = new ExtendedScriptingContext(scripting);
	scripting.putScriptObject("scripting", scripting);

	Reader reader = new InputStreamReader
	    (new FileInputStream(filename), "UTF-8");

	Object result = scripting.evalScript(reader);

	if (result instanceof RealValuedFunctionVA) {
	    trigfunct2 = (RealValuedFunctionVA)result;
	} else {
	    throw new Exception
		("rvfvatest.js did not produce a RealValuedFunctionVA");
	}

	RealValuedFunctionVA trigfunct4 = new
	    RealValuedFunctionVA(2,2, scripting, "f", "fp", "fpp");

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
		if (Math.abs(trigfunct1.derivAt(0,theta, phi)
			     - trigfunct2.derivAt(0,theta, phi )) >= 1.e-12) {
		    throw new Exception("derivAt (0): "
					+ trigfunct1.derivAt(0, theta, phi)
					+ " \u2260 "
					+ trigfunct2.derivAt(0, theta, phi));
		}
		if (Math.abs(trigfunct1.derivAt(1,theta, phi)
			     - trigfunct2.derivAt(1,theta, phi )) >= 1.e-12) {
		    throw new Exception("derivAt (1): "
					+ trigfunct1.derivAt(1, theta, phi)
					+ " \u2260 "
					+ trigfunct2.derivAt(1, theta, phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(0,0, theta, phi)
			     - trigfunct2.secondDerivAt(0,0,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("derivAt (00): "
					+ trigfunct1.secondDerivAt(0,0,
								   theta, phi)
					+ " \u2260 "
					+ trigfunct2.secondDerivAt(0,0,
								   theta, phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(0,1, theta, phi)
			     - trigfunct2.secondDerivAt(0,1,theta,phi))
		    >= 1.e-12){
		    throw new Exception("secondDerivAt (0,1): "
					+ trigfunct1.secondDerivAt(0,1,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct2.secondDerivAt(0,1,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,0, theta,phi)
			     - trigfunct2.secondDerivAt(1,0,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt:(1,0) "
					+ trigfunct1.secondDerivAt(1,0,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct2.secondDerivAt(1,0,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,1,theta, phi)
			     - trigfunct2.secondDerivAt(1,1,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (1,1): "
					+ trigfunct1.secondDerivAt(1,1,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct2.secondDerivAt(1,1,
								   theta,phi));
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
		if (Math.abs(trigfunct1.derivAt(0,theta, phi)
			     - trigfunct3.derivAt(0,theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv1At: "
					+ trigfunct1.derivAt(0,theta, phi)
					+ " \u2260 "
					+ trigfunct3.derivAt(0,theta, phi));
		}
		if (Math.abs(trigfunct1.derivAt(1,theta, phi)
			     - trigfunct3.derivAt(1,theta, phi )) >= 1.e-12) {
		    throw new Exception("deriv2At: "
					+ trigfunct1.derivAt(1,theta, phi)
					+ " \u2260 "
					+ trigfunct3.derivAt(1,theta, phi));
		}
		if (Math.abs(trigfunct1.derivAt(0,0,theta, phi)
			     - trigfunct3.derivAt(0,0,theta,phi)) >= 1.e-12) {
		    throw new Exception("secondDerivAt (0,0): "
					+ trigfunct1.derivAt(0,0, theta,phi)
					+ " \u2260 "
					+ trigfunct3.derivAt(0,0,theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(0,1,theta, phi)
			     - trigfunct3.secondDerivAt(0,1,
							  theta, phi ))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (0,1): "
					+ trigfunct1.secondDerivAt(0,1,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct3.secondDerivAt(0,1,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,0,theta, phi)
			     - trigfunct3.secondDerivAt(1,0, theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (1,0): "
					+ trigfunct1.secondDerivAt(1,0,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct3.secondDerivAt(1,0,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,1,theta, phi)
			     - trigfunct3.secondDerivAt(1,1,
						    theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (1,1): "
					+ trigfunct1.secondDerivAt(1,1, theta, phi)
					+ " \u2260 "
					+ trigfunct3.secondDerivAt(1,1, theta, phi));
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
		if (Math.abs(trigfunct1.derivAt(0,theta, phi)
			     - trigfunct4.derivAt(0,theta, phi )) >= 1.e-12) {
		    throw new Exception("derivAt: (0)"
					+ trigfunct1.derivAt(0,theta, phi)
					+ " \u2260 "
					+ trigfunct4.derivAt(0,theta, phi));
		}
		if (Math.abs(trigfunct1.derivAt(1,theta, phi)
			     - trigfunct4.derivAt(1,theta, phi )) >= 1.e-12) {
		    throw new Exception("derivAt: (1)"
					+ trigfunct1.derivAt(1,theta, phi)
					+ " \u2260 "
					+ trigfunct4.derivAt(1,theta, phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(0,0, theta, phi)
			     - trigfunct4.secondDerivAt(0,0,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt  (0,0): "
					+ trigfunct1.secondDerivAt(0,0,theta,
								   phi)
					+ " \u2260 "
					+ trigfunct4.secondDerivAt(0,0,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(0,1,theta, phi)
			     - trigfunct4.secondDerivAt(0,1,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (0,1): "
					+ trigfunct1.secondDerivAt(0,1,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct4.secondDerivAt(0,1,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,0,theta, phi)
			     - trigfunct4.secondDerivAt(1,0,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (1,0): "
					+ trigfunct1.secondDerivAt(1,0,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct4.secondDerivAt(1,0,
								   theta,phi));
		}
		if (Math.abs(trigfunct1.secondDerivAt(1,1,theta, phi)
			     - trigfunct4.secondDerivAt(1,1,theta,phi))
		    >= 1.e-12) {
		    throw new Exception("secondDerivAt (1,1): "
					+ trigfunct1.secondDerivAt(1,1,
								   theta,phi)
					+ " \u2260 "
					+ trigfunct4.secondDerivAt(1,1,
								   theta,phi));
		}
	    }
	}

	// test domain min/max, etc.
	RealValuedFunctionVA trigfunct5 = new RealValuedFunctionVA(2,2) {

		public double valueAt(double... args) {
		    double x = args[0];
		    double y = args[1];
		    return Math.tan(x) * Math.tan(y);
		}
		public double getDomainMin(int i){return -Math.PI/2.0;}
		public boolean domainMinClosed(int i){return false;}

		public double getDomainMax(int i){return Math.PI/2.0;}
		public boolean domainMaxClosed(int i){return false;}
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
	    dresult = false;
	}

	if (fva.getDomainMin(0) != -100.0) {
	    throw new Exception ("fva.getDomainMin(0) did not return -100.0");
	}
	if (fva.getDomainMax(0) != 100.0) {
	    throw new Exception ("fva.getDomainMax(0) did not return 100.0");
	}
	if (fva.domainMinClosed(0) != false) {
	    throw new Exception ("fva.domainMinClosed(0) did not return false");
	}
	if (fva.domainMaxClosed(0) != false) {
	    throw new Exception ("fva.domainMaxClosed(0) did not return false");
	}

	if (fva.getDomainMin(1) != -Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMin(1) did not return -MAX_VALUE");
	}
	if (fva.getDomainMax(1) != Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMax(1) did not return MAX_VALUE");
	}
	if (fva.domainMinClosed(1) != true) {
	    throw new Exception("fva.domainMinClosed(1) did not return true");
	}
	if (fva.domainMaxClosed(1) != true) {
	    throw new Exception("fva.domainMaxClosed(1) did not return true");
	}

	if (fva.getDomainMin(2) != -Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMin(2) did not return -MAX_VALUE");
	}
	if (fva.getDomainMax(2) != Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMax(2) did not return MAX_VALUE");
	}
	if (fva.domainMinClosed(2) != true) {
	    throw new Exception("fva.domainMinClosed(2) did not return true");
	}
	if (fva.domainMaxClosed(2) != true) {
	    throw new Exception("fva.domainMaxClosed(2) did not return true");
	}

	if (fva.getDomainMin(3) != -Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMin(3) did not return -MAX_VALUE");
	}
	if (fva.getDomainMax(3) != Double.MAX_VALUE) {
	    throw new Exception("fva.getDomainMax(3) did not return MAX_VALUE");
	}
	if (fva.domainMinClosed(3) != true) {
	    throw new Exception("fva.domainMinClosed(3) did not return true");
	}
	if (fva.domainMaxClosed(3) != true) {
	    throw new Exception("fva.domainMaxClosed(3) did not return true");
	}

	double testvals[] = {1.5, 7.3, 2.0, 9.8};

	for (double a: testvals) {
	    for (double b: testvals) {
		for (double c: testvals) {
		    for (int k = 0; k < 360.0; k++) {
			double x = Math.toRadians((double)k);
			double y1 = fva.valueAt(x, a, b, c);
			double y2 = f(x, a, b, c);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}
			double[] J = fva.jacobian(x, a, b, c);
			double[] J1 = fva.jacobian(1, x, a, b, c);
			for (int i = 0; i < 3; i++) {
			    if (Math.abs(J[i+1] - J1[i]) >= 1.e-10) {
				throw new Exception("tail of J != J1");
			    }
			}
			y1 = fva.derivAt(0,x,a,b,c);
			y2 = fp(x, a, b, c);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}
			if (Math.abs(y1 - J[0]) > 1.e-10) {
			    throw new Exception ("y1 != J[0]");
			}
			y1 = fva.derivAt(1,x,a,b,c);
			y2 = f1.valueAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}
			if (Math.abs(y1 - J[1]) > 1.e-10) {
			    throw new Exception ("y1 != J[1]");
			}
			y1 = fva.derivAt(2,x,a,b,c);
			y2 = f2.valueAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}
			if (Math.abs(y1 - J[2]) > 1.e-10) {
			    throw new Exception ("y1 != J[2]");
			}
			y1 = fva.derivAt(3,x,a,b,c);
			y2 = f3.valueAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}
			if (Math.abs(y1 - J[3]) > 1.e-10) {
			    throw new Exception ("y1 != J[3]");
			}

			y1 = fva.secondDerivAt(0,0,x,a,b,c);
			y2 = fpp(x, a, b, c);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}

			y1 = fva.secondDerivAt(0,1,x,a,b,c);
			y2 = f1.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    System.out.println(y1 + " != "+ y2
					       + " (x = " + x + ")");
			    throw new Exception ("y1 != y2");
			}
			y1 = fva.secondDerivAt(1,0,x,a,b,c);
			y2 = f1.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}

			y1 = fva.secondDerivAt(0,2,x,a,b,c);
			y2 = f2.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    System.out.println(y1 + " != "+ y2
					       + " (x = " + x + ")");
			    throw new Exception ("y1 != y2");
			}
			y1 = fva.secondDerivAt(2,0,x,a,b,c);
			y2 = f2.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}

			y1 = fva.secondDerivAt(0,3,x,a,b,c);
			y2 = f3.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    System.out.println(y1 + " != "+ y2
					       + " (x = " + x + ")");
			    throw new Exception ("y1 != y2");
			}
			y1 = fva.secondDerivAt(3,0,x,a,b,c);
			y2 = f3.derivAt(x);
			if (Math.abs(y1 - y2) > 1.e-10) {
			    throw new Exception ("y1 != y2");
			}

			y2 = 0.0;
			for (int i = 1; i < 4; i++) {
			    for (int j = 1; j < 4; j++) {
				y1 = fva.secondDerivAt(i,j,x,a,b,c);
				if (Math.abs(y1 - y2) > 1.e-10) {
				    throw new Exception ("y1 != y2");
				}
			    }
			}
		    }
		}
	    }
	}
	System.exit(0);
    }
}
