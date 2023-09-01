import org.bzdev.math.*;
import org.bzdev.lang.MathOps;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FunctionsTest {

    static class ChgParams {
	double a;
	double b;
	double z;
	double actualValue;
	ChgParams(double a,double b, double z,double actualValue) {
	    this.a = a;
	    this.b = b;
	    this.z = z;
	    this.actualValue = actualValue;
	}
    }

    static final double w2[] = {1.0, 1.0};
    static final double w3[] = {
	0.555555555555556,
	0.888888888888889,
	0.555555555555556
    };
    static final double w4[] = {
	0.347854845137454,
	0.652145154862546,
	0.652145154862546,
	0.347854845137454
    };
    static final double w5[] = {
	0.236926885056189,
	0.478628670499366,
	0.568888888888889,
	0.478628670499366,
	0.236926885056189
    };
    static final double w6[] = {
	0.171324492379170,
	0.360761573048139,
	0.467913934572691,
	0.467913934572691,
	0.360761573048139,
	0.171324492379170
    };

    static final double[] ws[] = {null, null, w2, w3, w4, w5, w6};

    static double j2(double x) {
	double f1 = (3.0/(x*x) - 1.0)/x;
	double f2 = 3.0/(x*x);
	return f1*Math.sin(x) - f2*Math.cos(x);
    }

    static double j3(double x) {
	double f1 = (15.0/(x*x*x) - 6.0/x)/x;
	double f2 = (15.0/(x*x) - 1.0)/x;
	return f1*Math.sin(x) - f2 * Math.cos(x);
    }

    static double laguerre0(double x) {return 1.0;}
    static double laguerre1(double x) {return 1.0-x;}
    static double laguerre2(double x) {return 0.5*(x*x-4.0*x+2);}
    static double laguerre3(double x) {return (-x*x*x + 9*x*x - 18 * x + 6)/6;}
    static double laguerre4(double x) {
	return (x*x*x*x - 16*x*x*x + 72*x*x - 96*x + 24)/24;
    }

    static double laguerre0(double alpha, double x) {return 1.0;}
    static double laguerre1(double alpha, double x) {return -x + alpha + 1.0;}
    static double laguerre2(double alpha, double x) {
	return x*x/2.0 - (alpha + 2)*x + (alpha + 2)*(alpha + 1) / 2.0;
    }
    static double laguerre3(double alpha, double x) {
	return -x*x*x/6 + (alpha+3)*x*x/2
	    - (alpha+2)*(alpha+3)*x/2.0 + (alpha+1)*(alpha+2)*(alpha+3)/6;
    }


    static final BigDecimal rootPI = new
	BigDecimal("1.772453850905516027298167483341145182"
		   + "79754945612238712821380778985291");
    static final BigDecimal sf =
	(new BigDecimal(2)).divide(rootPI, 200, RoundingMode.HALF_EVEN);

    static double hrErf(double arg) {
	BigDecimal x = new BigDecimal(arg);
	x.setScale(200);
	BigDecimal xsq = x.multiply(x);
	xsq.setScale(200, RoundingMode.HALF_EVEN);
	BigDecimal neg1 = BigDecimal.ONE.negate();

	double limit = 1.0e-50;

	BigDecimal term = new BigDecimal(arg);
	term = term.setScale(200);
	BigDecimal sum = BigDecimal.ONE.multiply(term);
	sum.setScale(200);
	int n = 1;
	do {
	    term = term.multiply(neg1);
	    term = term.multiply(xsq);
	    term = term.divide(new BigDecimal(n), 200,
			       RoundingMode.HALF_EVEN);
	    sum = sum.add(term.divide(new BigDecimal(2*n+1), 200,
				      RoundingMode.HALF_EVEN));

	    n++;
	} while (Math.abs(term.doubleValue()) > limit);
	sum = sum.multiply(sf);
	return sum.doubleValue();
    }

    static double hrErfc(double arg) {
	BigDecimal x = new BigDecimal(arg);
	x.setScale(200);
	BigDecimal xsq = x.multiply(x);
	xsq.setScale(200, RoundingMode.HALF_EVEN);
	BigDecimal neg1 = BigDecimal.ONE.negate();

	double limit = 1.0e-50;

	BigDecimal term = new BigDecimal(arg);
	term = term.setScale(200);
	BigDecimal sum = BigDecimal.ONE.multiply(term);
	sum.setScale(200);
	int n = 1;
	do {
	    term = term.multiply(neg1);
	    term = term.multiply(xsq);
	    term = term.divide(new BigDecimal(n), 200,
			       RoundingMode.HALF_EVEN);
	    sum = sum.add(term.divide(new BigDecimal(2*n+1), 200,
				      RoundingMode.HALF_EVEN));

	    n++;
	} while (Math.abs(term.doubleValue()) > limit);
	sum = sum.multiply(sf);
	BigDecimal result = BigDecimal.ONE.subtract(sum);
	return result.doubleValue();
    }

    static double ourPoch(double x, int n) {
	if (n == 0) return 1.0;
	double prod = x;
	for (int i = 1; i < n; i++) {
	    prod *= (x + i);
	}
	return prod;
    }

    static double ourHgF(double a, double b, double c, double z) {
	double sum = 0;
	double term;
	int n = 0;
	do {
	    term = Functions.poch(a, n) * Functions.poch(b, n)
		* MathOps.pow(z, n)
		/ (Functions.poch(c, n) * Functions.factorial(n));
	    sum += term;
	    n++;
	} while (Math.abs(term) > 1.e-15);
	return sum;
    }

    static void  ellipticIntegralTest() throws Exception {
	double angles[] = {
	    Math.PI/8,
	    Math.PI/4,
	    3*Math.PI/8,
	    Math.PI/2,
	    5*Math.PI/8,
	    3*Math.PI/4,
	    7*Math.PI/8,
	    Math.PI,
	    Math.PI + Math.PI/8,
	    Math.PI + Math.PI/4,
	    Math.PI + 3*Math.PI/8,
	    Math.PI + Math.PI/2,
	    Math.PI + 5*Math.PI/8,
	    Math.PI + 3*Math.PI/4,
	    Math.PI + 7*Math.PI/8,
	    2*Math.PI
	};
	double firstKind03[] = {
	    0.3935851732,
	    0.7919586905,
	    1.197396134,
	    1.60804862,
	    2*1.60804862 - 1.197396134,
	    2*1.60804862 - 0.7919586905,
	    2*1.60804862 - 0.3935851732,
	    2*1.60804862,
	    2*1.60804862 + 0.3935851732,
	    2*1.60804862 + 0.7919586905,
	    2*1.60804862 + 1.197396134,
	    2*1.60804862 + 1.60804862,
	    4*1.60804862 - 1.197396134,
	    4*1.60804862 - 0.7919586905,
	    4*1.60804862 - 0.3935851732,
	    4*1.60804862,
	};
	double secondKind03[] = {
	    0.3918165381,
	    0.7789308656,
	    1.159301124,
	    1.534833465,
	    2*1.534833465 - 1.159301124 ,
	    2*1.534833465 - 0.7789308656,
	    2*1.534833465 - 0.3918165381,
	    2*1.534833465,
	    2*1.534833465 + 0.3918165381,
	    2*1.534833465 + 0.7789308656,
	    2*1.534833465 + 1.159301124,
	    2*1.534833465 + 1.534833465,
	    4*1.534833465 - 1.159301124,
	    4*1.534833465 - 0.7789308656,
	    4*1.534833465 - 0.3918165381,
	    4*1.534833465,
	};

	double thirdKind0307[] = {
	    0.408264060218680,
	    0.923378136566370,
	    1.719788570167700,
	    2.956212354991400,
	    2*2.956212354991400 - 1.719788570167700,
	    2*2.956212354991400 - 0.923378136566370,
	    2*2.956212354991400 - 0.408264060218680,
	    2*2.956212354991400,
	    2*2.956212354991400 + 0.408264060218680,
	    2*2.956212354991400 + 0.923378136566370,
	    2*2.956212354991400 + 1.719788570167700,
	    2*2.956212354991400 + 2.956212354991400,
	    4*2.956212354991400 - 1.719788570167700,
	    4*2.956212354991400 - 0.923378136566370,
	    4*2.956212354991400 - 0.408264060218680,
	    4*2.956212354991400,
	};

	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.eF(angles[i], 0.3);
	    if (Math.abs(value - firstKind03[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + firstKind03[i]);
	    }
	}
	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.eF(-angles[i], 0.3);
	    if (Math.abs(value + firstKind03[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + -firstKind03[i]);
	    }
	}
	// test of RF, RD, RC, RJ using homogeneity properties
	// & duplication theorems.  This is a consistency test,
	// but can cover a lot of combinations
	double vals[] = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

	for (double kappa: vals) {
	    for(double x: vals) {
		for (double y: vals) {
		    for (double z: vals) {
			double value1 = Functions.RF(kappa*x,
						     kappa*y,
						     kappa*z);
			double value2 = Functions.RF(x, y, z)
			    / Math.sqrt(kappa);
			if (Math.abs(value1 - value2) > 1.e-10) {
			    throw new Exception(value1 + " != " + value2);
			}
		    }
		}
	    }
	}
	for(double x: vals) {
	    for (double y: vals) {
		for (double z: vals) {
		    double lambda = Math.sqrt(x)*Math.sqrt(y)
			+ Math.sqrt(y)*Math.sqrt(z)
			+ Math.sqrt(z)*Math.sqrt(x);
		    double value1 = Functions.RF(x, y, z);
		    double value2 = Functions.RF((x+lambda)/4,
						 (y+lambda)/4,
						 (z+lambda)/4);
		    if (Math.abs(value1 - value2) > 1.e-10) {
			throw new Exception(value1 + " != " + value2);
		    }
		}
	    }
	}

	for(double x: vals) {
	    for (double y: vals) {
		    double value1 = Functions.RC(x, y);
		    double value2 = Functions.RF(x, y, y);
		    if (Math.abs(value1 - value2) > 1.e-10) {
			throw new Exception(value1 + " != " + value2);
		    }
	    }
	}

	for(double x: vals) {
	    for (double y: vals) {
		for (double z: vals) {
		    double value1 = Functions.RD(x, y, z);
		    double value2 = Functions.RJ(x, y, z, z);
		    if (Math.abs(value1 - value2) > 1.e-10) {
			throw new Exception(value1 + " != " + value2);
		    }
		}
	    }
	}

	for (double p: vals) {
	    for (double kappa: vals) {
		for(double x: vals) {
		    for (double y: vals) {
			for (double z: vals) {
			    double value1 = Functions.RJ(kappa*x,
							 kappa*y,
							 kappa*z,
							 kappa*p);
			    double value2 = Functions.RJ(x, y, z, p)
				/(kappa*Math.sqrt(kappa));
			    if (Math.abs(value1 - value2) > 1.e-10) {
				throw new Exception(value1 + " != " + value2);
			    }
			}
		    }
		}
	    }
	}
	for (double p: vals) {
	    for(double x: vals) {
		for (double y: vals) {
		    for (double z: vals) {
			double lambda = Math.sqrt(x)*Math.sqrt(y)
			    + Math.sqrt(y)*Math.sqrt(z)
			    + Math.sqrt(z)*Math.sqrt(x);
			double d = (Math.sqrt(p) + Math.sqrt(x))
			    * (Math.sqrt(p) + Math.sqrt(y))
			    * (Math.sqrt(p) + Math.sqrt(z));

			double value1 = Functions.RJ(x, y, z, p);
			double value2 = Functions.RJ((x+lambda)/4,
					      (y+lambda)/4,
					      (z+lambda)/4,
					      (p+lambda)/4) / 4
			    + 6*Functions.RC(d*d, d*d + (p-x)*(p-y)*(p-z));
			if (Math.abs(value1 - value2) > 1.e-10) {
			    throw new Exception(value1 + " != " + value2);
			}
		    }
		}
	    }
	}

	// test incomplete elliptic integral of the second kind.
	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.eE(angles[i], 0.3);
	    if (Math.abs(value - secondKind03[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + secondKind03[i]);
	    }
	}
	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.eE(-angles[i], 0.3);
	    if (Math.abs(value + secondKind03[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + -secondKind03[i]);
	    }
	}

	// test incomplete elliptic integral of the third kind.eE
	if (Math.abs(Functions.ePI(0.7, Math.PI/2, 0.3)
		     - Functions.ePI(0.7, 0.3)) > 1.e-11) {
	    System.out.println(Functions.ePI(0.7, Math.PI/2, 0.3)
			       + " != "
			       + Functions.ePI(0.7, 0.3));
	    throw new Exception ("ePI Error - complete versus incomplete");
	}

	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.ePI(0.7, angles[i], 0.3);
	    if (Math.abs(value - thirdKind0307[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + thirdKind0307[i]);
	    }
	}
	for (int i = 0; i < angles.length; i++) {
	    double value = Functions.ePI(0.7, -angles[i], 0.3);
	    if (Math.abs(value + thirdKind0307[i]) > 1.e-8) {
		throw new Exception("angle index = " + i
				    + ", saw " + value
				    + ", expected " + -thirdKind0307[i]);
	    }
	}

	double x = .3;
	double y = .4;
	double z = .5;
	double p = .6;


	// from https://calcresource.com/eval-elliptic-carlson.html
	// ... to check against somone else's software.
	double rf = 1.59129;
	double rc = 1.65576;
	double rd = 3.49443;
	double rj = 3.11271;
	if (Math.abs(rf - Functions.RF(x,y,z)) > 1.e-4)	{
	    throw new Exception("RF error");
	}
	if (Math.abs(rc - Functions.RC(x,y)) > 1.e-4)	{
	    throw new Exception("RC error");
	}
	if (Math.abs(rd - Functions.RD(x,y,z)) > 1.e-4)	{
	    throw new Exception("RD error");
	}
	if (Math.abs(rj - Functions.RJ(x,y,z,p)) > 1.e-4)	{
	    throw new Exception("RJ error");
	}
    }

    static void initialCarlsonTest() throws Exception {
	// An attempt to integrate the square root of a quartic
	// 9 + 6x + 6x^2 + 2x^3 + x^4 disagrees with GL quadrature.
	// expected values calculated using
	// https://calcresource.com/eval-elliptic-carlson.html
	// as an independent source of values.


	double[] args1 = {
	    605.8143191089465 ,
	    610.886115878671 ,
	    624.742522339222
	};
	double v1 = Functions.RD(args1[0], args1[1], args1[2]);
	double ev1 = 0.0000650673;

	double[] args2 = {
	    605.8143191089465 ,
	    610.886115878671 ,
	    624.742522339222
	};
	double v2 = Functions.RF(args2[0], args2[1], args2[2]);
	double ev2 = 0.0403638;

	double[] args3 = {
	    605.8143191089465 ,
	    610.886115878671 ,
	    624.742522339222 ,
	    613.8143191089465
	};

	double v3 = Functions.RJ(args3[0], args3[1], args3[2], args3[3]);
	double ev3 = 0.000065761;

	double[] args4 = {
	    5.786029061433749E7 ,
	    5.786003461433749E7
	};
	double v4 = Functions.RC(args4[0], args4[1]);
	double ev4 = 0.000131465;

	double[] args5 = {
	    1418.521301712814 ,
	    1341.765819933007
	};
	double v5 = Functions.RC(args5[0], args5[1]);
	double ev5 = 0.0270461;

	if (Math.abs (v1 - ev1) > 1.0e-6) {
	    System.out.println("v1 = " + v1 + "ev1 = " + ev1);
	    throw new Exception();
	}
	if (Math.abs (v2 - ev2) > 1.0e-6) {
	    System.out.println("v2 = " + v2 + "ev2 = " + ev2);
	    throw new Exception();
	}
	if (Math.abs (v3 - ev3) > 1.0e-8) {
	    System.out.println("v3 = " + v3 + "ev3 = " + ev3);
	    throw new Exception();
	}
	if (Math.abs (v4 - ev4) > 1.0e-10) {
	    System.out.println("v4 = " + v4 + "ev4 = " + ev4);
	    throw new Exception();
	}
	if (Math.abs (v5 - ev5) > 1.0e-7) {
	    System.out.println("v5 = " + v5 + "ev5 = " + ev5);
	    throw new Exception();
	}
    }

    public static void main(String argv[]) throws Exception {

	initialCarlsonTest();

	double delta = 0.00001;
	double limit = 0.0001;
	double n = 17;
	int m = -1;
	if (argv.length > 0) {
	    n = Integer.parseInt(argv[0]);
	}
	if (argv.length > 1) {
	    m = Integer.parseInt(argv[1]);
	}
	int errcount = 0;

	System.out.println("Elliptic Integral Test");
	ellipticIntegralTest();
	System.out.println("root test:");
	for (int i = 1; i < 30; i++) {
	    double x  = 97.0;

	    double root = MathOps.root(i, x);
	    double prod = 1.0;
	    for (int j = 0; j < i; j++) {
		prod *= root;
	    }
	    if (Math.abs(prod - 97.0) > 1.e-12) {
		System.out.println("i = " + i + ", root = " + root
				   + ", prod = " + prod);
		throw new Exception("root failed");
	    }
	}

	for (int i = 1; i < 30; i++) {
	    if (i%2 == 0) continue;
	    double x  = - 97.0;

	    double root = MathOps.root(i, x);
	    double prod = 1.0;
	    for (int j = 0; j < i; j++) {
		prod *= root;
	    }
	    if (Math.abs(prod + 97.0) > 1.e-12) {
		System.out.println("i = " + i + ", root = " + root
				   + ", prod = " + prod);
		throw new Exception("root failed");
	    }
	}

	System.out.println("power test:");
	double prod = 1.0;
	long lprod = 1;
	long nlprod = 1;
	for (int i = 0; i < 30; i++) {
	    if (MathOps.pow(2.0, i) != prod) {
		System.out.format("pow(2.0, %d) = %g ?\n",
				  MathOps.pow(2.0, i));
		errcount++;
	    }
	    if (MathOps.lPow(2, i) != lprod) {
		System.out.format("lPow(2, %d) = %d ?\n",
				  MathOps.lPow(2, i));
		errcount++;
	    }
	    if (MathOps.lPow(-2, i) != lprod) {
		System.out.format("lPow(-2, %d) = %d ?\n",
				  MathOps.lPow(-2, i));
		errcount++;
	    }
	    prod *= 2.0;
	    lprod *= 2;
	    nlprod *= -2;
	}

	if (errcount > 0) {
	    System.out.println("power test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("factorial test:");
	long fvalue = 1;
	for (int i = 0; i < 15; i++) {
	    if (i > 1) fvalue *= i;
	    if (Functions.factorial(i) != fvalue) {
		System.out.format("%d! = %g ? (should be %d)\n",
				  i, Functions.factorial(i), fvalue);
		errcount++;
	    }
	}

	for (int i = 0; i < 21; i++) {
	    long value = Functions.longFactorial(i);
	    double expected = Functions.factorial(i);
	    if (Math.abs((value - expected)/expected) > 1.e-10) {
		System.out.format("longFactorial(%d) = %d, expected %14.12g\n",
				  i, value, expected);
		errcount++;
	    }
	}

	for (int i = 0; i < 30; i++) {
	    double value = Functions.exactFactorial(i).doubleValue();
	    double expected = Functions.factorial(i);
	    if (Math.abs((value - expected)/expected) > 1.e-10) {
		System.out.format("exactFactorial(%d) = %14.12g, "
				  + "expected %14.12g\n",
				  i, value, expected);
		errcount++;
	    }
	}

	if (Long.MAX_VALUE/Functions.longFactorial(20) >= 21) {
	    System.out.println("array size too small");
	    errcount++;
	}

	if (errcount > 0) {
	    System.out.println("factorial test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Test Legendre Polynomial derivatives");
	if (m < 0) {
	    limit = 0.0001;
	    double dlimit = 0.001;
	    double ddelta = 0.000001;
	    for (int i = 0; i < 17; i++) {
		limit *= 1.5;
		for (double x = -1.0; x <= 1.0; x += 0.1) {
		    if (Math.abs(x)< 1.e-10) x = 0.0;
		    if (Math.abs(x + 0.5) < 1.e-10) x = - 0.5;
		    double deriv = (Functions.P(i,x+delta) - Functions.P(i,x))
			/ delta;
		    if (Math.abs(deriv - Functions.dPdx(i,x)) > limit) {
			System.out.println(i + " " + x + ": "
					   + Functions.P(i,x) + " "
					   + Functions.dPdx(i,x)
					   + " " + deriv);
			errcount++;
		    }
		    if (x+delta >= 1.0) continue;
		    if (x-delta <= -1.0) continue;
		    if (i < 0) continue;
		    // if (i > 5) continue;
		    for (int k = -i; k <=i; k++) {
			if (k < 0) continue;
			if (x != 1.0 && x != -1.0 && k < i && k > -i) {
			    deriv = (i*x*Functions.P(i,k,x)
				     -(i+k)*Functions.P(i-1,k,x))/(x*x-1);
			} else {
			    deriv =
				(Functions.P(i,k,x+ddelta)
				 - Functions.P(i,k,x-ddelta))
				/ (2.0*ddelta);
			}
			double value = Functions.dPdx(i,k,x);
			double max = Math.max(Math.abs(deriv), Math.abs(value));
			if (Math.abs((deriv-value)/max) > dlimit) {
			    System.out.format("dPdx(%d,%d,%g) = %14.12g, "
					      + "expected %14.12g\n",
					      i, k, x,
					      value,
					      deriv);
			    errcount++;
			}
		    }
		}
	    }
	}
	if (errcount > 0) {
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}
	System.out.println("Legendre polynomial test");
	for (int i = 1; i <= n; i++) {
	    if (m >= 0 && i != n) continue;
	    double[] roots = new double[i];
	    int nroots = (m == -1)?
		Functions.LegendrePolynomial.roots(i, roots):
		Functions.LegendrePolynomial.roots(i, roots, m);
	    if (nroots != i) {
		System.out.println("number of roots for P_" + i +" =  "
				   + nroots);
		errcount++;
	    }
	    for (int j = 0; j < roots.length; j++) {
		double deriv = Functions.dPdx(i, roots[j]);
		double weight = 2.0 / ((1.0 - roots[j]*roots[j])*deriv*deriv);
		boolean failed = false;
		if(i > 1 && i < ws.length &&
		   Math.abs(weight - ws[i][j]) > 1.e-12) {
		    failed = true;
		}
		if (Math.abs(Functions.P(i, roots[j])) > 1.e-12) failed = true;
		if (failed) {
		    System.out.println("    " + roots[j] +
				       ", function value = "
				       + Functions.P(i, roots[j])
				       + ", weight = " + weight);
		    errcount++;
		}
	    }
	    Polynomial lp = Functions.LegendrePolynomial.asPolynomial(i);
	    BezierPolynomial blp =
		Functions.LegendrePolynomial.asBezierPolynomial(i);
	    for (int k = 0; k <= 10; k++) {
		double t = k/10.0;
		if (t < 0.0) t = 0.0;
		if (t > 1.0) t = 1.0;
		double val1 = lp.valueAt(t);
		double val2 = blp.valueAt(t);
		double val = Functions.P(i, t);
		if (Math.abs(val - val1) > 1.e-10) {
		    throw new Exception();
		}
		if (Math.abs(val - val2) > 1.e-10) {
		    throw new Exception();
		}
	    }
	}
	if (errcount > 0) {
	    System.out.println ("roots or weights wrong");
	    System.exit (1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("check associated Legendre functions");
	for (int i = 0; i <= 10; i += 2) {
	    double x = i /10.0;
	    int nP = 1;
	    int mP = 1;
	    double value = Functions.P(nP,mP,x);
	    double expected = - Math.sqrt(1-x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
	    }
	    nP = 2; mP = 1;
	    value = Functions.P(nP,mP,x);
	    expected = -3*x*Math.sqrt(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    mP = 2;
	    value = Functions.P(nP,mP,x);
	    expected = 3.0*(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 3; mP = 1;
	    value = Functions.P(nP,mP,x);
	    expected = -(3.0/2.0)*(5.0*x*x-1)*Math.sqrt(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 3; mP = 2;
	    value = Functions.P(nP,mP,x);
	    expected = 15.0*x*(1.0-x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 3; mP = 3;
	    value = Functions.P(nP,mP,x);
	    expected = -15.0*(1-x*x)*Math.sqrt(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 4; mP = 1;
	    value = Functions.P(nP,mP,x);
	    expected = -(5.0/2.0)*(7.0*x*x*x-3.0*x)*Math.sqrt(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 4; mP = 2;
	    value = Functions.P(nP,mP,x);
	    expected = (15.0/2.0)*(7.0*x*x - 1.0)*(1.0-x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 4; mP = 3;
	    value = Functions.P(nP,mP,x);
	    expected = -105.0*x*(1-x*x)*Math.sqrt(1 - x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	    nP = 4; mP = 4;
	    value = Functions.P(nP,mP,x);
	    expected = 105*(1.0-x*x)*(1.0-x*x);
	    if (Math.abs (value - expected) > 1.e-10) {
		System.out.format(" P(%d,%d,%g) = %14.12g "
				  + "(should be %14.12g)\n",
				  nP, mP, x, value, expected);
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	/*
	  for (int i = 0; i < 50; i++) {
	  double x = i/10.0;
	  System.out.format("\u03b6(%g) = %g\t", x,
	  Functions.zeta(x));
	  if (i % 5 == 4) System.out.println();
	  }
	*/
	System.out.println("Zeta function test:");
	for (int i = 2; i < 10; i++) {
	    double x = (double) i;
	    double xx = x + 0.000000001;
	    // check consistency of small integer values - a special case
	    // hard coded - with the full computation.
	    if (Math.abs(Functions.zeta(x) - Functions.zeta(xx)) > 1.e-6) {
		System.out
		    .format("\u03b6(%g) = %14.12g, \u03b6(%12.10g) = %14.12g\n",
			    x, Functions.zeta(x),
			    xx, Functions.zeta(xx));
		errcount++;
	    }
	}

	for (int i = 0; i < 10; i++) {
	    double x = -(double) i;
	    double xx = x + 0.000000001;
	    if (Math.abs(Functions.zeta(x) - Functions.zeta(xx)) > 1.e-6) {
		System.out
		    .format("\u03b6(%g) = %14.12g, \u03b6(%12.10g) = %14.12g\n",
			    x, Functions.zeta(x),
			    xx, Functions.zeta(xx));
		errcount++;
	    }
	}
	if ( errcount > 0) {
	    System.out.println("zeta-function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("log Gamma function test:");
	System.out.println("(for integers, compares log \u0393(n) to log n!");
	System.out.println(" and thus also checks the logFactorial function)");
	if (Math.abs(Functions.logGamma(0.5) - Math.log(Math.sqrt(Math.PI)))
	    > 1.e-9) {
	    System.out.format("log \u0393(%g) = %14.12g, should be %14.12g\n",
			      0.5, Functions.logGamma(0.5),
			      Math.log(Math.sqrt(Math.PI)));
	    errcount++;
	}
	if (Math.abs(Functions.logGamma(1.5)-Math.log(0.5 * Math.sqrt(Math.PI)))
	    > 1.e-9) {
	    System.out.format("log \u0393(%g) = %14.12g, should be %14.12g\n",
			      1.5, Functions.logGamma(1.5),
			      Math.log(0.5 * Math.sqrt(Math.PI)));
	    errcount++;
	}
	if (Math.abs(Functions.logGamma(3.5)
		     - Math.log((15.0/8.0) * Math.sqrt(Math.PI))) > 1.e-9) {
	    System.out.format("log \u0393(%g) = %14.12g, should be %14.12g\n",
			      3.5, Functions.logGamma(3.5),
			      Math.log((15.0/8.0) * Math.sqrt(Math.PI)));
	    errcount++;
	}
	for (int i = 1; i < 15; i++) {
	    double x = (double) i;
	    if (Math.abs(Functions.logGamma(x)-Functions.logFactorial(i-1))
		> 1.e-9) {
		System.out.format("log \u0393(%g) = %14.12g, "
				  + "should be %14.12g\n",
				  x, Functions.logGamma(x),
				  Functions.logFactorial(i-1));
		errcount++;
	    }
	}

	for (int i = 40; i < 50; i++) {
	    double x = (double) i;
	    if (Math.abs(Functions.logGamma(x)-Functions.logFactorial(i-1))
		> 1.e-9) {
		System.out.format("log \u0393(%g) = %14.12g, "
				  + "should be %14.12g\n",
				  x, Functions.logGamma(x),
				  Functions.logFactorial(i-1));
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println("log \u0393 test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Gamma-function test:");

	if (Math.abs(Functions.Gamma(0.6) - 1.48919224881) > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      0.6, Functions.Gamma(0.6),
			      1.48919224881);
	    errcount++;
	}

	if (Math.abs(Functions.Gamma(0.5)-(Math.sqrt(Math.PI))) > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      0.5, Functions.Gamma(0.5),
			      (Math.sqrt(Math.PI)));
	    errcount++;
	}
	if (Math.abs(Functions.Gamma(1.5) - (0.5 * Math.sqrt(Math.PI)))
	    > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      1.5, Functions.Gamma(1.5),
			      (0.5 * Math.sqrt(Math.PI)));
	    errcount++;
	}
	if (Math.abs(Functions.Gamma(3.5) - ((15.0/8.0) * Math.sqrt(Math.PI)))
	    > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      3.5, Functions.Gamma(3.5),
			      ((15.0/8.0) * Math.sqrt(Math.PI)));
	    errcount++;
	}

	if (Math.abs(Functions.Gamma(-0.5) - (-2 * Math.sqrt(Math.PI)))
	    > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      -0.5, Functions.Gamma(-0.5),
			      (-2 * Math.sqrt(Math.PI)));
	    errcount++;
	}

	if (Math.abs(Functions.Gamma(-1.5) - ((4.0/3.0) * Math.sqrt(Math.PI)))
	    > 1.e-10) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      -1.5, Functions.Gamma(-1.5),
			      ((4.0/3.0) * Math.sqrt(Math.PI)));
	}

	if (Functions.Gamma(-1.0) != Double.NEGATIVE_INFINITY) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      -1.0, Functions.Gamma(-1.0),
			      Double.NEGATIVE_INFINITY);
	    errcount++;
	}

	if (Functions.Gamma(-2.0) != Double.POSITIVE_INFINITY) {
	    System.out.format("\u0393(%g) = %14.12g, should be %14.12g\n",
			      -2.0, Functions.Gamma(-2.0),
			      Double.POSITIVE_INFINITY);
	    errcount++;
	}

	for (int i = 1; i < 15; i++) {
	    double x = (double) i;
	    if (Functions.Gamma(x) != Functions.factorial(i-1)) {
		System.out.format("\u0393(%g) = %14.12g, "
				  + "should be %14.12g\n",
				  x, Functions.Gamma(x),
				  Functions.factorial(i-1));
		errcount++;
	    }
	}
	for (int i = 40; i < 50; i++) {
	    double x = (double) i;
	    if (Functions.Gamma(x) != Functions.factorial(i-1)) {
		System.out.format("\u0393(%g) = %14.12g, "
				  + "should be %14.12g\n",
				  x, Functions.Gamma(x),
				  Functions.factorial(i-1));
		errcount++;
	    }
	}

	for (int i = 1; i < 20; i++) {
	    double gammaNPH = Functions.Gamma(i + 0.5);
	    double expecting = Math.sqrt(Math.PI)
		* Functions.oddFactorial(2*i-1) / MathOps.pow(2.0,i);
	    if (Math.abs((gammaNPH - expecting)/expecting) > 1.e-10) {
		System.out.format("\u0393(%d+1/2) = %14.12g, "
				  + "expecting %14.12g\n",
				  i, gammaNPH, expecting);
		errcount++;
	    }

	}

	if (errcount > 0) {
	    System.out.println("Gamma-function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Beta-function tests:");

	for (int i = 10; i < 20; i++) {
	    final double x = i/10.0;
	    for (int j = 10; j < 20; j++) {
		final double y = j / 10.0;
		double value = Functions.Beta(x,y);
		GLQuadrature glq = new GLQuadrature(16) {
			protected double function(double theta) {
			    return
				Math.exp(Math.log(Math.sin(theta))*(2*x-1)
					 + Math.log(Math.cos(theta))*(2*y-1));
			}
		    };
		double expected = 2.0 * glq.integrate(0.0,
						      Math.PI/2.0,
						      128);
		if (Math.abs(value - expected) > 1.e-9) {
		    System.out.format("B(%g,%g) = %14.12g (expected %14.12g)\n",
				      x, y, value, expected);
		    System.exit(1);
		}
		if (Math.abs(Functions.Beta(y,x) - value) > 1.e-12) {
		    System.out.println("Beta function is symmmetric but "
				       + value + " != "
				       + Functions.Beta(y,x));
		    throw new Exception("Beta function not symmetric");
		}
	    }
	}
	for (int i = -9; i < 10; i++) {
	    if (i == 0) continue;
	    final double x = i/10.0;
	    for (int j = -9; j < 10; j++) {
		if (j == 0) continue;
		if (i + j == 0) continue;
		final double y = j / 10.0;
		double value = Functions.Beta(x,y);
		double expected = (Functions.Gamma(x)
				   / Functions.Gamma(x+y))
		    * Functions.Gamma(y);
		if (Math.abs(value - expected) > 1.e-9) {
		    System.out.format("B(%g,%g) = %14.12g (expected %14.12g)\n",
				      x, y, value, expected);
		    System.exit(1);
		}
		if (Math.abs(Functions.Beta(y,x) - value) > 1.e-12) {
		    System.out.println("Beta function is symmmetric but "
				       + value + " != "
				       + Functions.Beta(y,x));
		    throw new Exception("Beta function not symmetric");
		}
	    }
	}
	// values checked using Wolfram Research beta-function calculator.
	for (int i = 1; i <= 10; i++) {
	    double y = i/10.0;
	    double x = - y;
	    if (i != 10 && Functions.Beta(x,y) != 0.0) {
		System.out.format("B(%g,%g) = %14.12g ?\n", x, y,
				  Functions.Beta(x,y));
		System.exit(1);
	    }
	    if (i == 10 && Functions.Beta(x,y) != -1.0) {
		System.out.format("B(%g,%g) = %14.12g ?\n", x, y,
				  Functions.Beta(x,y));
		System.exit(1);
	    }
	    if (Math.abs(Functions.Beta(y,x) -
			 Functions.Beta(x,y)) > 1.e-12) {
		System.out.println("Beta function is symmmetric but "
				   + Functions.Beta(x,y)
				   + " != "
				   + Functions.Beta(y,x));
		throw new Exception("Beta function not symmetric");
	    }
	}

	if (Functions.Beta(1.0, -2.0)
	    != Functions.Beta(-2.0, 1.0)) {
	    throw new Exception("Beta function not symmetric");
	}
	if (Functions.Beta(1.0, -3.0)
	    != Functions.Beta(-3.0, 1.0)) {
	    throw new Exception("Beta function not symmetric");
	}
	if (Functions.Beta(2.0, -3.0)
	    != Functions.Beta(-3.0, 2.0)) {
	    throw new Exception("Beta function not symmetric");
	}

	if (Math.abs(Functions.Beta(1.0, -2.0) + 0.5) > 1.e-10) {
	    System.out.format("B(%g,%g) = %14.12g \n", 1.0, -2.0,
			      Functions.Beta(1.0, -2.0));
	    System.exit(1);
	}
	if (Math.abs(Functions.Beta(1.0, -3.0) + 1.0/3.0) > 1.e-10) {
	    System.out.format("B(%g,%g) = %14.12g ?\n", 1.0, -3.0,
			      Functions.Beta(1.0, -3.0));
	    System.exit(1);
	}
	if (Math.abs(Functions.Beta(2.0, -3.0) - 1.0/6.0) > 1.e-10) {
	    System.out.format("B(%g,%g) = %14.12g ?\n", 1.0, -3.0,
			      Functions.Beta(2.0, -3.0));
	    System.exit(1);
	}
	// http://keisan.casio.com/exec/system/1180573396 for values for
	// incomplete beta functions.

	if (Math.abs(Functions.Beta(0.4, 1.1, 3.1)
		     - 0.2073263984686077198742) > 1.e-10) {
	    System.out.println("Beta(0.4, 1.1., 3.1) failed");
	    System.exit(1);
	}
	if (Math.abs(Functions.BetaI(0.4, 1.1, 3.1)
		     - 0.76919718230480629196) > 1.e-10) {
	    System.out.println("BetaI(0.4, 1.1., 3.1) failed");
	    System.exit(1);
	}

	if (Math.abs(Functions.BetaI(0.4, 10.0, 12.0)
		     - 0.30855818788527407104) > 1.e-10) {
	    System.out.println(Functions.Beta(0.4, 10.0, 12.0));
	    System.out.println(Functions.BetaI(0.4, 10.0, 12.0));
	    System.out.println("BetaI(0.4, 10.0., 12.0) failed");
	    System.exit(1);
	}

	if (Math.abs(Functions.BetaI(0.4, 51.0, 52.0)
		     - 0.0258100429476447865167) > 1.e-8) {
	    System.out.println(Functions.BetaI(0.4, 51.0, 52.0));
	    System.out.println("diff = " + (Functions.BetaI(0.4, 51.0, 52.0)
					    - 0.0258100429476447865167));
	    System.out.println("BetaI(0.4, 51.0., 52.0) failed");
	    System.exit(1);
	}
	
	if (Math.abs(Functions.BetaI(0.999, 100.0, 1.0)
		     -0.9047921471137090420322) > 1.e-10) {
	    System.out.println(Functions.BetaI(0.999, 100.0, 1.0));
	    System.out.println("BetaI(0.01, 3.0, 40.0) failed");
	    System.exit(1);
	}


	double betaDeriv = (Functions.Beta(0.4+delta, 1.1, 3.1)
			    - Functions.Beta(0.4, 1.1, 3.1))/delta;
	if (Math.abs(Functions.dBetadx(0.4, 1.1, 3.1) - betaDeriv)
	    > 10*delta) {
	    System.out.format("dBetadx: %s != %s\n",
			      Functions.dBetadx(0.4, 1.1, 3.1), betaDeriv);
	    System.exit(1);
	}

	betaDeriv = (Functions.BetaI(0.4+delta, 1.1, 3.1)
			    - Functions.BetaI(0.4, 1.1, 3.1))/delta;
	if (Math.abs(Functions.dBetaIdx(0.4, 1.1, 3.1) - betaDeriv)
	    > 10*delta) {
	    System.out.format("dBetaIdx: %s != %s\n",
			      Functions.dBetaIdx(0.4, 1.1, 3.1), betaDeriv);
	    System.exit(1);
	}

	betaDeriv = (Functions.dBetadx(0.4+delta, 1.1, 3.1)
			    - Functions.dBetadx(0.4, 1.1, 3.1))/delta;
	if (Math.abs(Functions.d2Betadx2(0.4, 1.1, 3.1) - betaDeriv) > delta) {
	    System.out.format("d2Betadx2: %s != %s\n",
			      Functions.d2Betadx2(0.4, 1.1, 3.1), betaDeriv);
	    System.exit(1);
	}

	betaDeriv = (Functions.dBetaIdx(0.4+delta, 1.1, 3.1)
			    - Functions.dBetaIdx(0.4, 1.1, 3.1))/delta;
	if (Math.abs(Functions.d2BetaIdx2(0.4, 1.1, 3.1) - betaDeriv)
	    > 10*delta) {
	    System.out.format("d2BetaIdx2: %s != %s\n",
			      Functions.d2BetaIdx2(0.4, 1.1, 3.1), betaDeriv);
	    System.exit(1);
	}


	System.out.println(" ... OK");

	System.out.println("digamma function tests:");

	if (Math.abs(Functions.digamma(1.0)  + Constants.EULERS_CONSTANT)
	    > 1.e-10) {
	    System.out.format("\u03a8(%g) = %14.12g (should be %12.10g)\n",
			      1.0, Functions.digamma(1.0),
			      -Constants.EULERS_CONSTANT);
	    errcount++;
	}
	if (Math.abs(Functions.digamma(0.5) -
		     (-2.0*Math.log(2.0) -Constants.EULERS_CONSTANT))
	    > 1.e-10) {
	    System.out.format("\u03a8(%g) = %14.12g (should be %12.10g)\n",
			      0.5, Functions.digamma(0.5),
			      -2.0*Math.log(2.0) -Constants.EULERS_CONSTANT);
	    errcount++;
	}
	if (Math.abs(Functions.digamma(0.25) -
		     (-Math.PI/2.0 - 3.0*Math.log(2.0)
		      -Constants.EULERS_CONSTANT)) > 1.e-10) {
	    System.out.format("\u03a8(%g) = %14.12g (should be %12.10g)\n",
			      0.25, Functions.digamma(0.25),
			      -Math.PI/2.0 - 3.0*Math.log(2.0)
			      -Constants.EULERS_CONSTANT);
	    errcount++;
	}
	if (Math.abs(Functions.digamma(2.0) -
		     (-Constants.EULERS_CONSTANT + 1.0)) > 1.e-10) {
	    System.out.format("\u03a8(%g) = %14.12g (should be %12.10g)\n",
			      2.0, Functions.digamma(2.0),
			      -Constants.EULERS_CONSTANT + 1.0);
	    errcount++;
	}
	if (Math.abs(Functions.digamma(3.0) -
		     (-Constants.EULERS_CONSTANT + 1.0 + 1.0/2.0)) > 1.e-10) {
	    System.out.format("\u03a8(%g) = %14.12g (should be %12.10g)\n",
			      3.0, Functions.digamma(3.0),
			      -Constants.EULERS_CONSTANT
			      + 1.0 + 1.0/2.0);
	    errcount++;
	}
	for (int i = 1; i < 10; i++) {
	    double x = (double) i;
	    double y = x + 0.00001;
	    double dx = Functions.digamma(x);
	    double di = Functions.digamma(i);
	    double dy = Functions.digamma(y);
	    if (Math.abs(dx - di) > 1.e-10) {
		System.out.format("\u03a8(%g) = %14.12g "
				  + "(int version = %14.12g)\n",
				  x, Functions.digamma(x),
				  Functions.digamma(i));
		errcount++;
	    }
	    if (Math.abs(dx - di) > 1.e-6) {
		System.out.format("... \u03a8(%g) = %14.12g\n",
				  y, Functions.digamma(y));
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println(" ... digamma test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("odd factorial test:");
	double oddfacts[] = {1.0, 1.0, 1.0, 3.0, 3.0, 15.0, 15.0,
			     105.0, 105.0, 945.0, 945.0, 10395.0,
			     10395.0, 135135.0, 135135.0};

	for (int i = 0; i < oddfacts.length; i++) {
	    if (Functions.oddFactorial(i) != oddfacts[i]) {
		System.out.format("oddFactorial(%d) = %g\n", i,
				  Functions.oddFactorial(i));
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println("odd factorial test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Bessel function tests:");

	if (Math.abs(Functions.J(0, 1.0) - 0.7651976865579665514497) > 1.e-12) {
	    System.out.format("J(%d,%g) = %14.12g  (expected %14.12g)\n",
			      0, 1.0, Functions.J(0, 1.0),
			      0.7651976865579665514497);
	    errcount++;
	}
	if (Math.abs(Functions.Y(0, 1.0) - 0.08825696421567695798293)
	    > 1.e-12) {
	    System.out.format("Y(%d,%g) = %14.12g  (expected %14.12g)\n",
			      0, 1.0, Functions.Y(0, 1.0),
			      0.08825696421567695798293);
	}
	if (Math.abs(Functions.J(1, 1.0) - 0.4400505857449335159597)
	    > 1.e-12) {
	    System.out.format("J(%d,%g) = %14.12g  (expected %14.12g)\n",
			      1, 1.0, Functions.J(1, 1.0),
			      0.4400505857449335159597);
	    errcount++;
	}
	if (Math.abs(Functions.Y(1, 1.0) + 0.7812128213002887165472) > 1.e-12) {
	    System.out.format("Y(%d,%g) = %14.12g  (expected %14.12g)\n",
			      1, 1.0, Functions.Y(1, 1.0),
			      -0.7812128213002887165472);
	    errcount++;
	}
	if (Math.abs( Functions.J(2, 1.0) - 0.1149034849319004804696)
	    > 1.e-12) {
	    System.out.format("J(%d,%g) = %14.12g  (expected %14.12g)\n",
			      2, 1.0, Functions.J(2, 1.0),
			      0.1149034849319004804696);
	    errcount++;
	}
	if (Math.abs(Functions.Y(2, 1.0) + 1.650682606816254391077) > 1.e-12) {
	    System.out.format("Y(%d,%g) = %14.12g  (expected %14.12g)\n",
			      2, 1.0, Functions.Y(2, 1.0),
			      -1.650682606816254391077);
	    errcount++;
	}
	if (Math.abs(Functions.J(-1, 1.0) + 0.4400505857449335159597)
	    > 1.e-12) {
	    System.out.format("J(%d,%g) = %14.12g  (expected %14.12g)\n",
			      -1, 1.0, Functions.J(-1, 1.0),
			      -0.4400505857449335159597);
	    errcount++;
	}
	if (Math.abs(Functions.Y(-1, 1.0) - 0.7812128213002887165472)
	    > 1.e-12) {
	    System.out.format("Y(%d,%g) = %14.12g  (expected %14.12g)\n",
			      -1, 1.0, Functions.Y(-1, 1.0),
			      0.7812128213002887165472);
	    errcount++;
	}
	if (Math.abs(Functions.J(-2, 1.0) - 0.1149034849319004804696)
	    > 1.e-12){
	    System.out.format("J(%d,%g) = %14.12g  (expected %14.12g)\n",
			      -2, 1.0, Functions.J(-2, 1.0),
			      0.1149034849319004804696);
	    errcount++;
	}
	if (Math.abs(Functions.Y(-2, 1.0) + 1.650682606816254391077) > 1.e-12) {
	    System.out.format("Y(%d,%g) = %14.12g  (expected %14.12g)\n",
			      -2, 1.0, Functions.Y(-2, 1.0),
			      -1.650682606816254391077);
	    errcount++;
	}
	if (Math.abs(Functions.J(0.5, 1.0) - 0.671396707141803090416)
	    > 1.e-12) {
	    System.out.format("J(%g,%g) = %14.12g  (expected %14.12g)\n",
			      0.5, 1.0, Functions.J(0.5, 1.0),
			      0.671396707141803090416);
	    errcount++;
	}
	if (Math.abs(Functions.Y(0.5, 1.0) + 0.431098868018376079521)
	    > 1.e-12) {
	    System.out.format("Y(%g,%g) = %14.12g  (expected %14.12g)\n",
			      0.5, 1.0, Functions.Y(0.5, 1.0),
			      -0.431098868018376079521);
	    errcount++;
	}
	if (Math.abs(Functions.J(-0.5, 1.0) - 0.431098868018376079521)
	    > 1.e-12) {
	    System.out.format("J(%g,%g) = %14.12g  (expected %14.12g)\n",
			      -0.5, 1.0, Functions.J(-0.5, 1.0),
			      0.431098868018376079521);
	    errcount++;
	}
	if (Math.abs(Functions.Y(-0.5, 1.0) - 0.671396707141803090416)
	    > 1.e-12) {
	    System.out.format("Y(%g,%g) = %14.12g  (expected %14.12g)\n",
			      -0.5, 1.0, Functions.Y(-0.5, 1.0),
			      0.671396707141803090416);
	    errcount++;
	}

	if (Math.abs(Functions.J(0.0, 2.0) - 0.2238907791412356680518)
	    > 1.e-12) {
	    System.out.format("J(%g,%g) = %14.12g  (expected %14.12g)\n",
			      -0.5, 1.0, Functions.J(-0.5, 1.0),
			      0.2238907791412356680518);
	    errcount++;
	}
	if (Math.abs(Functions.Y(0.0, 2.0) - 0.5103756726497451195966)
	    > 1.e-12) {
	    System.out.format("Y(%g,%g) = %14.12g  (expected %14.12g)\n",
			      -0.5, 1.0, Functions.Y(-0.5, 1.0),
			      0.5103756726497451195966);
	    errcount++;
	}


	if (Math.abs(Functions.J(0.5, 2.0) - 0.5130161365618277516657)
	    > 1.e-12) {
	    System.out.format("J(%g,%g) = %14.12g  (expected %14.12g)\n",
			      0.5, 2.0, Functions.J(0.5, 2.0),
			      0.5130161365618277516657);
	    errcount++;
	}
	if (Math.abs(Functions.Y(0.5, 2.0) - 0.234785710406248469174)
	    > 1.e-12) {
	    System.out.format("Y(%g,%g) = %14.12g  (expected %14.12g)\n",
			      0.5, 2.0, Functions.Y(0.5, 2.0),
			      0.234785710406248469174);
	    errcount++;
	}

	if (Math.abs(Functions.J(1.0, 2.0) - 0.5767248077568733872024)
	    > 1.e-12) {
	    System.out.format("J(%g,%g) = %14.12g  (expected %14.12g)\n",
			      -1.0, 2.0, Functions.J(1.0, 2.0),
			      0.5767248077568733872024);
	    errcount++;
	}
	if (Math.abs(Functions.Y(1.0, 2.0) + 0.107032431540937546888)
	    > 1.e-12) {
	    System.out.format("Y(%g,%g) = %14.12g  (expected %14.12g)\n",
			      1.0, 2.0, Functions.Y(1.0, 2.0),
			      -0.107032431540937546888);
	    errcount++;
	}

	for (int i = -4; i < 5; i++) {
	    if (Functions.J(i, 0.0) != ((i==0)? 1.0: 0.0)) {
		System.out.format("J(%d, 0.0) = %14.12g ?\n",
				  i, Functions.J(i, 0.0));
		errcount++;
	    }
	}


	limit = 0.0001;
	delta = .00000001;
	for (int i = -5; i < 5; i++) {
	    for (double x = 0.0; x <= 2.0; x += 0.1) {
		double deriv = (Functions.J(i,x+delta) - Functions.J(i,x))
		    / delta;
		double ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dJdx(i,x)) > ourlimit) {
		    System.out.format("d[J(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.dJdx(i,x), deriv);
		    errcount++;
		}
		if (x == 0.0) continue;
		deriv =  (Functions.Y(i,x+delta) - Functions.Y(i,x))
		    / delta;
		ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dYdx(i,x)) > ourlimit) {
		    System.out.format("d[Y(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.dYdx(i,x), deriv);
		    errcount++;
		}
	    }
	}

	for (int i = -5; i < 5; i++) {
	    double nu = i + 0.5;
	    for (double x = 0.1; x <= 2.0; x += 0.1) {
		double deriv = (Functions.J(nu,x+delta) - Functions.J(nu,x))
		    / delta;
		double ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dJdx(nu,x)) > ourlimit) {
		    System.out.format("d[J(%g,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      nu, x, Functions.dJdx(nu,x), deriv);
		    System.out.format ("     (J(%g,%g) = %g, J(%g,%g) = %g)\n",
				       nu, x+delta, Functions.J(nu,x+delta),
				       nu, x, Functions.J(nu, x));
		    errcount++;
		}
		deriv =  (Functions.Y(nu,x+delta) - Functions.Y(nu,x))
		    / delta;
		ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dYdx(nu,x)) > ourlimit) {
		    System.out.format("d[Y(%g,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      nu, x, Functions.dYdx(nu,x), deriv);
		    errcount++;
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("Bessel-function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Spherical-Bessel-function test:");

	if (Math.abs(Functions.j(0, .00001) - 0.9999999999833333333334)
	    > 1.e-10) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      0, .00001, Functions.j(0, .00001),
			      0.9999999999833333333334);
	    errcount++;
	}
	if (Math.abs(Functions.y(0, .00001) + 99999.99999500000000004)
	    > 1.e-5) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      0, .00001, Functions.y(0, .00001),
			      -99999.99999500000000004);
	    errcount++;
	}
	if (Math.abs(Functions.j(0, 1.0) - 0.841470984807896506653) > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      0, 1.0, Functions.j(0, 1.0),
			      0.841470984807896506653);
	    errcount++;
	}
	if (Math.abs(Functions.y(0, 1.0) + 0.540302305868139717401) > 1.e-12) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      0, 1.0, Functions.y(0, 1.0),
			      -0.540302305868139717401);
	    errcount++;
	}

	if (Math.abs(Functions.j(1, .00001) - 3.3333333333E-6) > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      1, .00001, Functions.j(1, .00001),
			      3.3333333333E-6);
	    errcount++;
	}
	if (Math.abs(Functions.y(1, .00001) + 10000000000.49999999999)
	    > 10.0) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      1, .00001, Functions.y(1, .00001),
			      -10000000000.49999999999);
	    errcount++;
	}
	if (Math.abs(Functions.j(1, 1.0) - 0.301168678939756789252) > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      1, 1.0, Functions.j(1, 1.0),
			      0.301168678939756789252);
	    errcount++;
	}
	if (Math.abs(Functions.y(1, 1.0)  + 1.381773290676036224053) > 1.e-12) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      1, 1.0, Functions.y(1, 1.0),
			      -1.381773290676036224053);
	    errcount++;
	}


	if (Math.abs(Functions.j(2, .00001) -
		     6.666666666619047619048E-12) > 1.e-22) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      2, .00001, Functions.j(2, .00001),
			      6.666666666619047619048E-12);
	    errcount++;
	}
	if (Math.abs(Functions.y(2, .00001) + 3000000000050000.000001)
	    > 100000.0) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      2, .00001, Functions.y(2, .00001),
			      -3000000000050000.000001);
	    errcount++;
	}
	if (Math.abs(Functions.j(2, 1.0) - 0.0620350520113738611022) > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      2, 1.0, Functions.j(2, 1.0),
			      0.0620350520113738611022);
	    errcount++;
	}
	if (Math.abs(Functions.y(2, 1.0) + 3.60501756615996895476) > 1.e-12) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      2, 1.0, Functions.y(2, 1.0),
			      -3.60501756615996895476);
	    errcount++;
	}

	if (Math.abs(Functions.j(4, 2.0) - 0.01407939276291532129121) > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      4, 2.0, Functions.j(4, 2.0),
			      0.01407939276291532129121);
	    errcount++;
	}
	if (Math.abs(Functions.y(4, 2.0) + 4.461291526363125663573) > 1.e-12) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      4, 2.0, Functions.y(4, 2.0),
			      -4.461291526363125663573);
	    errcount++;
	}

	if (Math.abs(Functions.j(5, 2.0) - 0.002635169770244117349047)
	    > 1.e-12) {
	    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
			      5, 2.0, Functions.j(5, 2.0),
			      0.002635169770244117349047
			      );
	    errcount++;
	}
	if (Math.abs(Functions.y(5, 2.0) + 18.59144531119098556222) > 1.e-12) {
	    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
			      5, 2.0, Functions.y(5, 2.0),
			      -18.59144531119098556222);
	    errcount++;
	}

	for (int i = 0; i < 6; i++) {
	    for (double x = 0.1; x < 1.0; x += 0.1) {
		double value = Math.sqrt((Math.PI/2.0)/x)
		    * Functions.J(i+0.5, x);
		if (Math.abs(Functions.j(i,x) - value) > 1.e-8) {
		    System.out.format("j(%d,%g) = %14.12g (expected %14.12g)\n",
				      i, x, Functions.j(i,x), value);
		    errcount++;
		}
		value = Math.sqrt((Math.PI/2.0)/x)
		    * Functions.Y(i+0.5, x);
		if (Math.abs((Functions.y(i,x) - value)/value) > 1.e-8) {
		    System.out.format("y(%d,%g) = %14.12g (expected %14.12g)\n",
				      i, x, Functions.y(i,x), value);
		    errcount++;
		}
	    }
	}

	limit = 0.0001;
	delta = .0000001;
	for (int i = -5; i <= 5; i++) {
	    for (double x = 0.0; x <= 2.0; x += 0.1) {
		double deriv = (x == 0.0)?
		    (Functions.j(i, x+delta) - Functions.j(i, x)) / delta:
		    (Functions.j(i,x+delta) - Functions.j(i,x-delta))
		    / (2.0*delta);
		double ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.djdx(i,x)) > ourlimit) {
		    System.out.format("d[j(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.djdx(i,x), deriv);
		    System.out.format(" (j(%d,%g) = %14.12g, j(%d,%g) = %14.12g)"
				      + "\n",
				      i-1, x, Functions.j(i-1, x),
				      i+1, x, Functions.j(i+1, x));
		    System.out.format(" (j(%d,%10.8g) = %14.12g, j(%d,%g) = %14.12g)"
				      + "\n",
				      i, x+delta, Functions.j(i, x+delta),
				      i, x, Functions.j(i, x));
		    errcount++;
		}
		if (x == 0.0) continue;
		deriv =  (Functions.y(i,x+delta) - Functions.y(i,x))
		    / delta;
		ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dydx(i,x)) > ourlimit) {
		    System.out.format("d[y(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.dydx(i,x), deriv);
		    errcount++;
		}
	    }
	}


	if (errcount > 0) {
	    System.out.println("Spherical-Bessel-function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Modified Bessel function tests:");

	double nu = 0.0;
	double x = 1.0;
	double expected = 1.266065877752008335598;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.4210244382407083333356;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 0.5;
	x =  1.0;
	expected = 0.937674888245487646717;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.46106850444789455844;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}


	nu = 1.0;
	x = 1.0;
	expected = 0.5651591039924850272077;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.6019072301972345747375;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.5;
	x = 1.0;
	expected = 0.2935253263474797997886;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.922137008895789116879;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 2.0;
	x = 1.0;
	expected = 0.1357476697670382811829;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 1.624838898635177482811;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 3.0;
	x = 1.0;
	expected = 0.02216842492433190247629;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 7.10126282473794450598;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	}

	nu = 4.0;
	x = 1.0;
	expected = 0.002737120221046866325138;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 44.23241584706284451869;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 0.0;
	x = 0.0;
	expected = 1.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 0.5;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.5;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.0;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.5;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 2.0;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 2.5;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 3.0;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 3.5;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 4.0;
	x = 0.0;
	expected = 0.0;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = Double.POSITIVE_INFINITY;
	if (Functions.K(nu,x) != expected) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 0.0;
	x =  2.0;
	expected = 2.27958530233606726744;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);

	}
	expected = 0.1138938727495334356527;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	}

	nu = 0.5;
	x = 2.0;
	expected = 2.046236863089055036605;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.119937771968061447368;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.0;
	x = 2.0;
	expected = 1.590636854637329063382;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.1398658818165224272846;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 1.5;
	x = 2.0;
	expected = 1.099473188633109675514;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.1799066579520921710521;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 2.0;
	x =  2.0;
	expected = 0.688948447698738204055;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.253759754566055862937;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 3.0;
	x = 2.0;
	expected = 0.2127399592398526552724;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 0.647385390948634153159;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	nu = 4.0;
	x = 2.0;
	expected = 0.0507285699791802382379;
	if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	    System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.I(nu,x), expected);
	    errcount++;
	}
	expected = 2.19591592741195832242;
	if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	    System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
			      nu, x, Functions.K(nu,x), expected);
	    errcount++;
	}

	limit = 0.0001;
	delta = .00000001;
	for (int i = -5; i < 5; i++) {
	    for (x = 0.0; x <= 2.0; x += 0.1) {
		double deriv = (Functions.I(i,x+delta) - Functions.I(i,x))
		    / delta;
		double ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dIdx(i,x)) > ourlimit) {
		    System.out.format("d[I(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.dIdx(i,x), deriv);
		    errcount++;
		}
		if (x == 0.0) continue;
		deriv =  (Functions.K(i,x+delta) - Functions.K(i,x))
		    / delta;
		ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dKdx(i,x)) > ourlimit) {
		    System.out.format("d[K(%d,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      i, x, Functions.dKdx(i,x), deriv);
		    errcount++;
		}
	    }
	}

	for (int i = -5; i < 5; i++) {
	    nu = i + 0.5;
	    for (x = 0.1; x <= 2.0; x += 0.1) {
		double deriv = (Functions.I(nu,x+delta) - Functions.I(nu,x))
		    / delta;
		double ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dIdx(nu,x)) > ourlimit) {
		    System.out.format("d[I(%g,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      nu, x, Functions.dIdx(nu,x), deriv);
		    System.out.format ("     (I(%g,%g) = %g, I(%g,%g) = %g)\n",
				       nu, x+delta, Functions.I(nu,x+delta),
				       nu, x, Functions.I(nu, x));
		    errcount++;
		}
		deriv =  (Functions.K(nu,x+delta) - Functions.K(nu,x))
		    / delta;
		ourlimit = Math.max(limit, Math.abs(deriv*limit));
		if (Math.abs(deriv - Functions.dKdx(nu,x)) > ourlimit) {
		    System.out.format("d[K(%g,%g)]/dx = %14.12g "
				      + "(should be %14.12g)\n",
				      nu, x, Functions.dKdx(nu,x), deriv);
		    errcount++;
		}
	    }
	}


	if (errcount > 0) {
	    System.out.println("Modified Bessel Function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	/*
	  nu = ;
	  x = ;
	  expected =
	  if (Math.abs(Functions.I(nu,x) - expected ) > 1.e-12) {
	  System.out.format("I(%g,%g) = %14.12g (expected %14.12g)\n",
	  nu, x, Functions.I(nu,x), expected);

	  }
	  expected =
	  if (Math.abs(Functions.K(nu,x) - expected) > 1.e-12) {
	  System.out.format("K(%g,%g) = %14.12g (expected %14.12g)\n",
	  nu, x, Functions.K(nu,x), expected);
	  }
	*/

	System.out.println("Airy function test:");

	x = 0.0;
	expected = 0.35502805388781723926;
	if (Math.abs(Functions.Ai(x) - expected ) > 1.e-12) {
	    System.out.format("Ai(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Ai(x), expected);
	    errcount++;
	}
	expected = 0.614926627446000735151;
	if (Math.abs(Functions.Bi(x) - expected) > 1.e-12) {
	    System.out.format("Bi(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Bi(x), expected);
	    errcount++;
	}

	x = -2.0;
	expected = 0.2274074282016855759919;
	if (Math.abs(Functions.Ai(x) - expected ) > 1.e-12) {
	    System.out.format("Ai(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Ai(x), expected);
	    errcount++;
	}
	expected = -0.412302587956398488083;
	if (Math.abs(Functions.Bi(x) - expected) > 1.e-12) {
	    System.out.format("Bi(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Bi(x), expected);
	    errcount++;
	}


	x = 2.0;
	expected = 0.034924130423274379135;
	if (Math.abs(Functions.Ai(x) - expected ) > 1.e-12) {
	    System.out.format("Ai(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Ai(x), expected);

	    errcount++;
	}
	expected = 3.29809499997821471028;
	if (Math.abs(Functions.Bi(x) - expected) > 1.e-12) {
	    System.out.format("Bi(%g) = %14.12g (expected %14.12g)\n",
			      x, Functions.Bi(x), expected);
	    errcount++;
	}

	delta = 0.000001;
	for (int i = -20; i <= 20; i++) {
	    x = i/10.0;
	    double deriv = (Functions.Ai(x + delta) - Functions.Ai(x))/delta;
	    if (Math.abs (Functions.dAidx(x) - deriv) > 1.e-4) {
		System.out.format("dAidx(%g) = %14.12g, expected %g\n",
				  x, Functions.dAidx(x), deriv);
		errcount++;
	    }
	    deriv = (Functions.Bi(x + delta) - Functions.Bi(x))/delta;
	    if (Math.abs (Functions.dBidx(x) - deriv) > 1.e-4) {
		System.out.format("dBidx(%g) = %14.12g, expected %g\n",
				  x, Functions.dBidx(x), deriv);
		errcount++;
	    }
	}

	if (errcount > 0) {
	    System.out.println("Airy function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Inverse hyperbolic function tests:");
	for (x = -10.0; x <= 10.0; x += 0.1) {
	    if (Math.abs(Math.sinh(Functions.asinh(x)) - x) > 1.0e-10) {
		System.out.println("asinh failed for x = " + x);
		errcount++;
	    }
	    if (x >= 1.0) {
		if (Math.abs(Math.cosh(Functions.acosh(x)) - x) > 1.0e-10) {
		    System.out.println("acosh failed for x = " + x);
		errcount++;
		}
	    }
	    if (x > -1.0 && x < 1.0) {
		if (Math.abs(Math.tanh(Functions.atanh(x)) - x) > 1.0e-10) {
		    System.out.println("atanh failed for x = " + x);
		    errcount++;
		}
	    }
	}
	if (Functions.atanh(1.0) != Double.POSITIVE_INFINITY) {
	    System.out.println("atanh failed for x = 1.0");
	    errcount++;
	}
	if (Functions.atanh(-1.0) != Double.NEGATIVE_INFINITY) {
	    System.out.println("atanh failed for x = -1.0");
	    errcount++;
	}

	if (errcount > 0) {
	    System.out.println("Inverse hyperbolic function test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}


	System.out.println("Laguerre polynomial test:");
	delta = 0.0001;
	for (int i = 0; i < 5; i++) {
	    for (x = -10.0; x <= 10.0; x += 0.1) {
		double lvalue = Functions.L(i, x);
		double evalue;
		switch (i) {
		case 0: evalue = laguerre0(x); break;
		case 1: evalue = laguerre1(x); break;
		case 2: evalue = laguerre2(x); break;
		case 3: evalue = laguerre3(x); break;
		case 4: evalue = laguerre4(x); break;
		default: evalue = Double.NaN;
		}
		double max = Math.max(1.0, Math.abs(evalue));
		if (Math.abs((evalue-lvalue)/max) > 1.e-10) {
		    System.out.format("L(%d,%g) = %14.12g, expected %14.12g\n",
				      i, x, lvalue, evalue);
		    errcount++;
		}
		double deriv = (Functions.L(i,x+delta) - Functions.L(i,x-delta))
		    / (2.0*delta);
		lvalue = Functions.dLdx(i, x);
		max = Math.max(1.0, Math.abs(evalue));
		if (Math.abs((deriv-lvalue)/max) > 1.e-5) {
		    System.out.format("d[L(%d,%g)]/dx = %14.12g, "
				      + "expected %14.12g\n",
				      i, x, lvalue, deriv);
		    errcount++;
		}
	    }
	}
	double alpha = 1.3;
	for (int i = 0; i < 4; i++) {
	    for (x = -10.0; x <= 10.0; x += 0.1) {
		double lvalue = Functions.L(i, alpha, x);
		double evalue;
		switch (i) {
		case 0: evalue = laguerre0(alpha, x); break;
		case 1: evalue = laguerre1(alpha, x); break;
		case 2: evalue = laguerre2(alpha, x); break;
		case 3: evalue = laguerre3(alpha, x); break;
		default: evalue = Double.NaN;
		}
		double max = Math.max(1.0, Math.abs(evalue));
		if (Math.abs((evalue-lvalue)/max) > 1.e-10) {
		    System.out.format("L(%d, 1.3, %g) = %14.12g, "
				      + "expected %14.12g\n",
				      i, x, lvalue, evalue);
		    errcount++;
		}
		double deriv = (Functions.L(i, alpha, x+delta)
				- Functions.L(i, alpha, x-delta))
		    / (2.0*delta);
		lvalue = Functions.dLdx(i, alpha, x);
		max = Math.max(1.0, Math.abs(evalue));
		if (Math.abs((deriv-lvalue)/max) > 1.e-5) {
		    System.out.format("d[L(%d, 1.3, %g)]/dx = %14.12g, "
				      + "expected %14.12g\n",
				      i, x, lvalue, deriv);
		    errcount++;
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("Laguerre polynomial test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Bernstein polynomial test:");

	double xs[] = { 0.0, 0.25, 0.5, 0.75, 1.0};
	for (double u: xs) {
	    x = u;
	    double b[] = {
		MathOps.pow(1-x, 3),
		3*x*MathOps.pow(1-x,2),
		3*x*x*(1-x),
		x*x*x
	    };
	    for(int i = 0; i < 4; i++) {
		if (Math.abs(b[i] - Functions.B(i,3,x)) > 1.e-10) {
		    errcount++;
		    System.out.format("b[%d] = %g but B(%d,3,x) = %g\n",
				      i, b[i], i, Functions.B(i,3,x));
		}
		double estderiv =
		    (Functions.B(i,3,x+delta) - Functions.B(i,3,x)) / delta;
		double deriv = Functions.dBdx(i,3,x);
		if (Math.abs(estderiv - deriv) > delta*10) {
		    errcount++;
		    System.out.format
			("for B(%d,3)(%g), estderiv = %g, deriv = %g\n",
			 i, x, estderiv, deriv);
		}
		double estderiv2 =
		    (Functions.dBdx(i,3,x+delta) - Functions.dBdx(i,3,x))/delta;
		double deriv2 = Functions.d2Bdx2(i,3,x);
		if (Math.abs(estderiv2 - deriv2) > delta*10) {
		    errcount++;
		    System.out.format
			("for B(%d,3)(%g), estderiv2 = %g, deriv2 = %g\n",
			 i, x, estderiv2, deriv2);
		}
	    }
	}
	if (errcount > 0) {
	    System.out.println("Bernstein polynomial test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}
	System.out.println("Try De Casteljau's algorithm");
	double beta[] = {1.0, 5.0, 3.0, 4.0, 2.0};
	double testx[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5,
			  0.6, 0.7, 0.8, 0.9, 1.0};
	double[] result2 = new double[2];
	double[] result3 = new double[2];
	double[] result22 = new double[4];
	double betaTwo[] = {
	    1.0, 2.0,
	    5.0, 6.0,
	    3.0, 4.0,
	    4.0, 5.0,
	    2.0, 3.0};
	double betaThree[] = new double[2*betaTwo.length];
	System.arraycopy(betaTwo, 0, betaThree, betaTwo.length, betaTwo.length);

	for (double xx: testx) {
	    double val1 = Functions.Bernstein.sumB(beta, 4, xx);
	    double val2 = 0.0;
	    for (int i = 0; i <= 4; i++) {
		val2 += beta[i] * Functions.B(i, 4, xx);
	    }
	    if (Math.abs(val1-val2) > 1.e-10) {
		errcount++;
		System.out.format("sum: x = %g, %g != %g\n", xx, val1, val2);
	    }
	    Functions.Bernstein.sumB(result2, betaTwo, 4, xx);
	    Functions.Bernstein.sumB(result3, betaThree, 5, 4, xx);
	    Functions.Bernstein.sumB(result22, 0, 2, betaThree, 5, 4, xx);
	    Functions.Bernstein.sumB(result22, 2, 2, betaThree, 5, 4, xx);

	    if (Math.abs(result2[0] - val1) > 1.e-10) {
		errcount++;
		System.out.format("sumB with result array produced %g "
				  +" instead of %g for %g\n",
				  result2[0], val1, xx);
	    }

	    for (int i = 0; i < 2; i++) {
		if (Math.abs(result2[i] - result3[i]) > 1.e-10) {
		    System.out.format("result2[%d] = %g but result3[%d] = %g "
				      + "for xx = %g\n",
				      i, result2[i], i, result3[i], xx);
		    errcount++;
		}
		if (Math.abs(result2[i] - result22[i]) > 1.e-10) {
		    System.out.format("result2[%d] = %g but result22[%d] = %g "
				      + "for xx = %g\n",
				      i, result2[i], i, result22[i], xx);
		    errcount++;
		}
		if (Math.abs(result2[i] - result22[i+2]) > 1.e-10) {
		    System.out.format("result2[%d] = %g but result22[%d] = %g "
				      + "for xx = %g\n",
				      i, result2[i], i+2, result22[i+2], xx);
		    errcount++;
		}
	    }

	    double val3 = Functions.Bernstein.dsumBdx(beta, 4, xx);
	    double val4 = 0.0;
	    for (int i = 0; i <= 4; i++) {
		val4 += beta[i] * Functions.dBdx(i, 4, xx);
	    }
	    if (Math.abs(val3-val4) > 1.e-10) {
		errcount++;
		System.out.format("deriv of sum: x = %g, %g != %g\n",
				  xx, val3, val4);
	    }
	    double val3a = Functions.Bernstein.dIsumBdxI(1, beta, 4, xx);
	    if (val3a != val3) {
		errcount++;
		System.out.format("dsumBdx and dIsumBdxI do not agree, I = 1");
	    }

	    double val5 = Functions.Bernstein.d2sumBdx2(beta, 4, xx);
	    double val6 = 0.0;
	    for (int i = 0; i <= 4; i++) {
		val6 += beta[i] * Functions.d2Bdx2(i, 4, xx);
	    }
	    if (Math.abs(val5-val6) > 1.e-10) {
		errcount++;
		System.out.format("2nd deriv of sum: x = %g, %g != %g\n",
				  xx, val5, val6);
	    }
	    double val5a = Functions.Bernstein.dIsumBdxI(2, beta, 4, xx);
	}

	double beta0[] = {1.0, 5.0, 10.0, 11.0};
	double beta0r3[] = new double[beta0.length+3];

	int nr3 = Functions.Bernstein.raiseBy(beta0r3, beta0, 3, 3);
	for (int i = 0; i <= 10; i++){
	    double rbx = i/10.0;
	    if (Math.abs(Functions.Bernstein.sumB(beta0, 3, rbx)
			 - Functions.Bernstein.sumB(beta0r3, nr3, rbx))
		> 1.e-14) {
		System.out.println("x = " + rbx + ": initial = "
				   + Functions.Bernstein.sumB(beta0, 3, rbx)
				   + ", raised = "
				   + Functions.Bernstein.sumB(beta0r3, 6, rbx));
		throw new Exception("raiseBy");
	    }
	}

	double beta2d[] = {1.0, 2.0,
			   5.0, 6.0,
			   10.0, 11.0,
			   11.0, 12.0,
			   15.0, 16.0,
			   20.0, 21.0,
			   21.0, 22.0};

	Functions.Bernstein.sumB(result2, beta2d, 0, 3, 0.4);
	Functions.Bernstein.sumB(result3, beta2d, 3, 3, 0.4);
	double vx = Functions.Bernstein.sumB(beta0, 3, 0.4);
	double vvx = beta0[0]*Functions.B(0,3,0.4) +
	    beta0[1]*Functions.B(1,3,0.4) + beta0[2]*Functions.B(2,3,0.4)
	    + beta0[3]*Functions.B(3,3,0.4);
	if (Math.abs(vx- vvx) > 1.e-14) {
	    System.out.println("vx != vvx");
	    errcount++;
	}
	if (Math.abs(result2[0] - vx) > 1.e-14) {
	    System.out.println("vx = " + vx);
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result2[0] != vx for beta0, beta2d");
	    errcount++;
	}
	if (Math.abs(result2[0] + 1.0 - result2[1]) > 1.e-14) {
	    System.out.println("result2[0]+1 != result2[1] for beta2d");
	    errcount++;
	}
	if (Math.abs(result2[0] + 10.0 - result3[0]) > 1.e-14) {
	    System.out.println("result2[0]+10 != result3[0] for beta2d");
	    errcount++;
	}
	double dvxdx = Functions.Bernstein.dsumBdx(beta0, 3, 0.4);
	Functions.Bernstein.dsumBdx(result2, beta2d, 0, 3, 0.4);
	Functions.Bernstein.dsumBdx(result3, beta2d, 3, 3, 0.4);
	Functions.Bernstein.dsumBdx(result22, 0, 2, beta2d, 0, 3, 0.4);
	Functions.Bernstein.dsumBdx(result22, 2, 2, beta2d, 0, 3, 0.4);
	if (Math.abs(result2[0] - dvxdx) > 1.e-14) {
	    System.out.println("dvxdx = " + dvxdx);
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result2[0] != vx for beta0, beta2d");
	    errcount++;
	}
	if (Math.abs(result2[0] - result2[1]) > 1.e-14) {
	    System.out.println("result2[0] != result2[1] for beta2d (deriv)");
	    errcount++;
	}
	if (Math.abs(result2[0]- result3[0]) > 1.e-14) {
	    System.out.println("result2[0] != result3[0] for beta2d (deriv)");
	    errcount++;
	}
	for (int i = 0; i < 2; i++) {
	    if (Math.abs(result2[i] - result22[i]) > 1.e-10) {
		System.out.format("result2[%d] = %g but result22[%d] = %g "
				  + "for xx = %g\n",
				  i, result2[i], i, result22[i], 0.4);
		errcount++;
	    }
	    if (Math.abs(result2[i] - result22[i+2]) > 1.e-10) {
		System.out.format("result2[%d] = %g but result22[%d] = %g "
				  + "for xx = %g\n",
				  i, result2[i], i+2, result22[i+2], 0.4);
		errcount++;
	    }
	}

	double d2vxdx2 = Functions.Bernstein.d2sumBdx2(beta0, 3, 0.4);
	Functions.Bernstein.d2sumBdx2(result2, beta2d, 0, 3, 0.4);
	Functions.Bernstein.d2sumBdx2(result3, beta2d, 3, 3, 0.4);
	if (Math.abs(result2[0] - d2vxdx2) > 1.e-14) {
	    System.out.println("d2vxdx2 = " + d2vxdx2);
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result2[0] != d2vxdx2 for beta0, beta2d "
			       + "(2nd deriv)");
	    errcount++;
	}
	if (Math.abs(result2[0] - result2[1]) > 1.e-14) {
	    System.out.println("result2[0] != result2[1] for beta2d "
			       + "(2nd deriv)");
	    errcount++;
	}
	if (Math.abs(result2[0]- result3[0]) > 1.e-13) {
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result3[0] = " + result3[0]);
	    System.out.println("result2[0] != result3[0] for beta2d "
			       + "(2nd deriv)");
	    errcount++;
	}

	Functions.Bernstein.dIsumBdxI(result2, beta2d, 0, 3, 0.4, 2);
	Functions.Bernstein.dIsumBdxI(result3, beta2d, 3, 3, 0.4, 2);
	if (Math.abs(result2[0] - d2vxdx2) > 1.e-14) {
	    System.out.println("d2vxdx2 = " + d2vxdx2);
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result2[0] != d2vxdx2 for beta0, beta2d "
			       + "(I=2 deriv");
	    errcount++;
	}
	if (Math.abs(result2[0] - result2[1]) > 1.e-14) {
	    System.out.println("result2[0] != result2[1] for beta2d "
			       + "(I=2 deriv)");
	    errcount++;
	}
	if (Math.abs(result2[0]- result3[0]) > 1.e-13) {
	    System.out.println("result2[0] = " + result2[0]);
	    System.out.println("result3[0] = " + result3[0]);
	    System.out.println("result2[0] != result3[0] for beta2d "
			       + "(I=2 deriv)");
	    errcount++;
	}


	if (errcount > 0) {
	    System.out.println("Bernstein polynomial test(s) failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Bernstein polynomials for barycentric coord test");
	int[][] lambdas = {{0, 0, 3},
			   {0, 1, 2},
			   {0, 2, 1},
			   {0, 3, 0},
			   {1, 0, 2},
			   {1, 1, 1},
			   {1, 2, 0},
			   {2, 0, 1},
			   {2, 1, 0},
			   {3, 0, 0}};
	for (double u: xs) {
	    for (double v: xs) {
		double w = 1.0 - (u + v);
		if (w < 0.0) continue;
		for (int[] lambda: lambdas) {
		    int lambda0 = lambda[0];
		    int lambda1 = lambda[1];
		    int lambda2 = lambda[2];
		    double actual =
			(Functions.factorial(3)/(Functions.factorial(lambda0)
						 *Functions.factorial(lambda1)
						 *Functions.factorial(lambda2)))
			* MathOps.pow(u, lambda0)
			* MathOps.pow(v, lambda1)
			* MathOps.pow(w, lambda2);
		    double computed = Functions.B(3, lambda, u, v, w);
		    if (Math.abs(computed -actual) > 1.e-10) {
			errcount++;
			System.out.format("Barycntric test: B(3,{%d, %d, %d},"
					  +"%g, %g, %g) = %g, expected %g\n",
					  lambda0, lambda1, lambda2, u, v, w,
					  computed, actual);
		    }
		    delta = 0.0001;
		    double ds = 10.0;
		    for (int i = 0; i < 3; i++) {
			double deriv = Functions.dBdx(i, 3,lambda, u, v, w);
			double est;
			switch (i) {
			case 0:
			    est = (Functions.B(3,lambda, u + delta, v, w)
				   - Functions.B(3,lambda, u, v, w)) / delta;
			    break;
			case 1:
			    est = (Functions.B(3,lambda, u, v + delta, w)
				   - Functions.B(3,lambda, u, v, w)) / delta;
			    break;
			case 2:
			    est = (Functions.B(3,lambda, u, v, w + delta)
				   - Functions.B(3,lambda, u, v, w)) / delta;
			    break;
			default:
			    est = 0.0; // never happens
			}
			if (Math.abs(deriv - est) > ds*delta) {
			    errcount++;
			    System.out.format("dBdx(%d,3,%g,%g,%g) = %g"
					      + ", estimated as %g\n",
					      i, u, v, w, deriv, est);
			}

			for (int j = 0; j < 3; j++) {
			    double deriv2 =
				Functions.d2Bdxdy(i, j, 3, lambda, u, v, w);
			    double est2;
			    switch(j) {
			    case 0:
				est2 = (Functions.dBdx(i, 3,lambda,
						       u + delta, v, w)
					- Functions.dBdx(i,3,lambda,
							 u, v, w))
				    / delta;
				break;
			    case 1:
				est2 = (Functions.dBdx(i, 3,lambda,
						       u, v + delta, w)
					- Functions.dBdx(i,3,lambda,
							 u, v, w))
				    / delta;
				break;
			    case 2:
				est2 = (Functions.dBdx(i, 3,lambda,
						       u, v, w + delta)
					- Functions.dBdx(i,3,lambda,
							 u, v, w))
				    / delta;
				break;
			    default:
				est2 = 0.0; // neve happens
			    }
			    if (Math.abs(deriv2 - est2) > ds*delta) {
				errcount++;
				System.out.format("d2Bdxdy(%d,%d,3,%g,%g,%g)"
						  + " = %g"
						  + ", estimated as %g\n",
						  i, j, u, v, w,
						  deriv2, est2);
			    }
			}
		    }
		}
	    }
	}

	for (int i = 0; i <= 2; i++) {
	    for (int j = 0; j <= 2; j++) {
		for (int k = 0; k <= 2; k++) {
		    int index = Functions.Bernstein.indexForLambdas(2, i, j, k);
		    int lambda[] =
			Functions.Bernstein.lambdasForIndex(2, 3, index);
		    if (lambda[0] != i || lambda[1] != j || lambda[2] != k) {
			System.out.format("indexForLambda failure: "
					  + "i = %d, j = %d, k = %d, index = %d"
					  + ", lambda[0] = %d, "
					  + "lambda[1] = %d, "
					  + "lambda[2] = %d\n",
					  i, j, k, index,
					  lambda[0], lambda[1], lambda[2]);
			errcount++;
		    }
		}
	    }
	}
	for (int index: Functions.Bernstein.generateIndices(2, 3)) {
	    int lambda[] = Functions.Bernstein.lambdasForIndex(2, 3, index);
	    if (lambda[0] + lambda[1] + lambda[2] != 2) {
		System.out.format("lambdas for index %d: "
				  + "lambda[0] = %d, lambda[1] = %d, "
				  + "lambda[2] = %d\n",
				  index, lambda[0], lambda[1], lambda[2]);
		errcount++;
	    }
	}

	for (int index: Functions.Bernstein.generateIndices(2, 1, 3)) {
	    int lambda[] = Functions.Bernstein.lambdasForIndex(2, 3, index);
	    if (lambda[0] + lambda[1] + lambda[2] != 1) {
		System.out.format("lambdas for index %d: "
				  + "lambda[0] = %d, lambda[1] = %d, "
				  + "lambda[2] = %d\n",
				  index, lambda[0], lambda[1], lambda[2]);
		errcount++;
	    }
	}

	for (int index1:  Functions.Bernstein.generateIndices(2, 3, 3)) {
	    for (int index2:  Functions.Bernstein.generateIndices(2, 2, 3)) {
		for (int index3:  Functions.Bernstein.generateIndices(2,1,3)) {
		    if (index1 == index2 || index1 == index3
			|| index2 == index3) {
			System.out.format("indices 1 2 3: %d %d %d "
					  + "(should not be equal pairwise)\n",
					  index1, index2, index3);
		    }
		}
	    }
	}

	double bbeta[] = {2.0, 6.0, 7.0, 4.0, 8.0, 9.0, 10.0, 3.0, 1.0, 5.0};
	double bbeta2[] = {2.0, 3.0,
			   6.0, 7.0,
			   7.0, 8.0,
			   4.0, 5.0,
			   8.0, 9.0,
			   9.0, 10.0,
			   10.0, 11.0,
			   3.0, 4.0,
			   1.0, 2.0,
			   5.0, 6.0};

	double[] bbeta2a = new double[3*bbeta2.length];
	int offset2a = bbeta2.length;
	System.arraycopy(bbeta2, 0, bbeta2a, bbeta2.length, bbeta2.length);

	for (double u: xs) {
	    for (double v: xs) {
		double w = 1.0 - (u + v);
		if (w < 0.0) continue;
		double computed = Functions.Bernstein.sumB(bbeta, 3, u, v, w);
		double actual = 0.0;
		int[] indices = Functions.Bernstein.generateIndices(3, 3);
		for (int i = 0; i < bbeta.length; i++) {
		    int lambda[] =
			Functions.Bernstein.lambdasForIndex(3, 3, indices[i]);
		    actual += bbeta[i]*Functions.B(3, lambda, u, v, w);
		}
		if (Math.abs(computed - actual) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed = %g, actual = %g\n",
				      u, v, w, computed, actual);
		    errcount++;
		}
		computed = Functions.Bernstein.dsumBdx(0, bbeta, 3, u, v, w);
		double ds = 10.0;
		delta = .0001;
		double estimated =
		    (Functions.Bernstein.sumB(bbeta, 3, u+delta, v, w)
		     - Functions.Bernstein.sumB(bbeta, 3, u, v, w)) / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed deriv u = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v, w);
		estimated =
		    (Functions.Bernstein.sumB(bbeta, 3, u, v+delta, w)
		     - Functions.Bernstein.sumB(bbeta, 3, u, v, w)) / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed deriv v = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.dsumBdx(2, bbeta, 3, u, v, w);
		estimated =
		    (Functions.Bernstein.sumB(bbeta, 3, u, v, w+delta)
		     - Functions.Bernstein.sumB(bbeta, 3, u, v, w)) / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed deriv w = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.d2sumBdxdy(0, 0, bbeta, 3,
							  u, v, w);
		estimated =
		    (Functions.Bernstein.dsumBdx(0, bbeta, 3, u+delta, v, w)
		     - Functions.Bernstein.dsumBdx(0, bbeta, 3, u, v, w))
		    / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed second deriv u u = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.d2sumBdxdy(0, 1, bbeta, 3,
							  u, v, w);
		estimated =
		    (Functions.Bernstein.dsumBdx(0, bbeta, 3, u, v+delta, w)
		     - Functions.Bernstein.dsumBdx(0, bbeta, 3, u, v, w))
		    / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed second deriv u v = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.d2sumBdxdy(1, 1, bbeta, 3,
							  u, v, w);
		estimated =
		    (Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v+delta, w)
		     - Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v, w))
		    / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed second deriv v v = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		computed = Functions.Bernstein.d2sumBdxdy(1, 2, bbeta, 3,
							  u, v, w);
		estimated =
		    (Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v, w+delta)
		     - Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v, w))
		    / delta;
		if (Math.abs((computed - estimated)/estimated) > ds*delta) {
		    System.out.format("u = %g, v = %g, w = %g: "
				      + "computed second deriv u v = %g, "
				      + "estimated = %g\n",
				      u, v, w, computed, estimated);
		    errcount++;
		}
		double[] result = new double[2];
		double[] result2a = new double[2];
		Functions.Bernstein.sumB(result, bbeta2, 3, u, v, w);
		Functions.Bernstein.sumB(result2a, bbeta2a, offset2a, 3,
					 u, v, w);
		for (int i = 0; i < 2; i++) {
		    if (Math.abs(result[i] - result2a[i]) > 1.e-10) {
			throw new Exception("result and result2a differ");
		    }
		}

		actual = Functions.Bernstein.sumB(bbeta, 3, u, v, w);
		if (Math.abs(result[0] - actual) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (sum): "
				      + "result[0] = %g, not %g\n",
				      u, v, w, result[0], actual);
		    errcount++;
		}
		if (Math.abs(result[1]-result[0] - 1.0) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (sum): "
				      + "result[0] = %g, result[1] = %g\n",
				      u, v, w, result[0], result[1]);
		    errcount++;
		}
		Functions.Bernstein.dsumBdx(0, result, bbeta2, 3, u, v, w);
		actual = Functions.Bernstein.dsumBdx(0, bbeta, 3, u, v, w);
		if (Math.abs(result[0] - actual) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (deriv 0): "
				      + "result[0] = %g, not %g\n",
				      u, v, w, result[0], actual);
		    errcount++;
		}
		if (Math.abs(result[1]-result[0] - 3.0) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (deriv 0): "
				      + "result[0] = %g, result[1] = %g\n",
				      u, v, w, result[0], result[1]);
		    errcount++;
		}
		Functions.Bernstein.dsumBdx(1, result, bbeta2, 3, u, v, w);
		actual = Functions.Bernstein.dsumBdx(1, bbeta, 3, u, v, w);
		if (Math.abs(result[0] - actual) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (deriv 1): "
				      + "result[0] = %g, not %g\n",
				      u, v, w, result[0], actual);
		    errcount++;
		}
		if (Math.abs(result[1]-result[0] - 3.0) > 1.e-10) {
		    System.out.format("u = %g, v = %g, w = %g (deriv 1): "
				      + "result[0] = %g, result[1] = %g\n",
				      u, v, w, result[0], result[1]);
		    errcount++;
		}
		for (int i = 0; i < 2; i++) {
		    for (int j = 0; j < 2; j++) {
			Functions.Bernstein.d2sumBdxdy(i, j, result, bbeta2,
						       3, u, v, w);
			actual = Functions.Bernstein.d2sumBdxdy(i, j, bbeta,
								3, u, v, w);
			if (Math.abs(result[0] - actual) > 1.e-10) {
			    System.out.format
				("u = %g, v = %g, w = %g (deriv %d %d): "
				 + "result[0] = %g, not %g\n",
				 u, v, w, i, j, result[0], actual);
			    errcount++;
			}
			if (Math.abs(result[1]-result[0] - 6.0) > 1.e-10) {
			    System.out.format
				("u = %g, v = %g, w = %g (deriv %d %d): "
				 + "result[0] = %g, result[1] = %g\n",
				 u, v, w, i, j, result[0], result[1]);
			    errcount++;
			}
		    }
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("Barycentric Bernstein polynomial test(s) "
			       + "failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("GCD Test...");

	int[] ints = { 3, 5, 1,
		       6, 8, 2,
		       15, 18, 3,
		       13*7*3, 13*3*2, 13*3,
		       3*5*2*2, 3*5*2*7*13, 3*5*2};
	for (int i = 0; i < ints.length; i+= 3) {
	    int i1 = ints[i];
	    int i2 = ints[i+1];
	    int gcd = ints[i+2];
	    if (gcd != MathOps.gcd(i1, i2)) {
		System.out.format("gcd test failed: MathOps.gcd(%d,%d) != %d\n",
				  i1, i2, gcd);
		errcount++;
	    }
	}

	for (int i = 0; i < ints.length; i+= 3) {
	    int i1 = -ints[i];
	    int i2 = ints[i+1];
	    int gcd = ints[i+2];
	    if (gcd != MathOps.gcd(i1, i2)) {
		System.out.format("gcd test failed: MathOps.gcd(%d,%d) != %d\n",
				  i1, i2, gcd);
		errcount++;
	    }
	}

	for (int i = 0; i < ints.length; i+= 3) {
	    int i1 = ints[i];
	    int i2 = -ints[i+1];
	    int gcd = ints[i+2];
	    if (gcd != MathOps.gcd(i1, i2)) {
		System.out.format("gcd test failed: MathOps.gcd(%d,%d) != %d\n",
				  i1, i2, gcd);
		errcount++;
	    }
	}

	for (int i = 0; i < ints.length; i+= 3) {
	    int i1 = -ints[i];
	    int i2 = -ints[i+1];
	    int gcd = ints[i+2];
	    if (gcd != MathOps.gcd(i1, i2)) {
		System.out.format("gcd test failed: MathOps.gcd(%d,%d) != %d\n",
				  i1, i2, gcd);
		errcount++;
	    }
	}


	for (int i = 0; i < ints.length; i+= 3) {
	    long i1 = ints[i];
	    long i2 = ints[i+1];
	    long gcd = ints[i+2];
	    if (gcd != MathOps.gcd(i1, i2)) {
		System.out.format("(long) gcd test failed: MathOps.gcd(%d,%d) != %d\n",
				  i1, i2, gcd);
		errcount++;
	    }
	}

	if (errcount > 0) {
	    System.out.println("GCD test(s) "
			       + "failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}
	System.out.println("test gcd exception ...");
	try {
	    MathOps.gcd(0,1);
	} catch(Exception e) {
	    System.out.println(e.getMessage() + " ... EXPECTED");
	}

	System.out.println("modular inverse test...");
	int[] miarray = new int[2];
	for (int i = -2000; i < 2000; i++) {
	    for (int j = 2; j < 2000; j++) {
		if (i == 0 && j == 0) continue;
		if (MathOps.gcd(i, j) != 1) continue;
		if (i % j == 0) continue;
		int inv = Functions.modInverse(i, j);
		int test = (i*inv) % j;
		if (test < 0) test += j;
		if (test != 1) {
		    System.out.format("modInverse(%d,%d) failed\n", i, j);
		    System.out.println("... inv = " + inv);
		    int gcd = Bezout.gcd(i, j, miarray);
		    System.out.format(" ... gcd = %d = %d*%d + %d*%d\n",
				      gcd, miarray[0], i, miarray[1], j);
		    System.exit(1);
		}
	    }
	}



	System.out.println("log2 tests ...");

	for (int i = 1; i < 1000; i++) {
	    double log2 = MathOps.log2(i);
	    double expecting = Math.log((double)i)/Math.log(2.0);
	    if (Math.abs(log2 - expecting) > 1.e-10) {
		errcount++;
		System.out.format("log2(%d) = %g, expecting %g\n",
				  i, log2, expecting);
	    }
	    if (log2 != MathOps.log2((long)i)) {
		errcount++;
		System.out.format("log2(%d) != log2((long)%d)\n", i, i);
	    }
	    if (log2 != MathOps.log2(i, 1.e-15)) {
		errcount++;
		System.out.format("log2(%d) != log2(%d, 1.e-15)\n", i, i);
	    }
	    if (log2 != MathOps.log2(i, 1.e-15)) {
		errcount++;
		System.out.format("log2(%d) != log2((long)%d, 1.e-15)\n", i, i);
	    }
	}

	if (errcount > 0) {
	    System.out.println("log2(s) "
			       + "failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	// test values computed using
	// http://keisan.casio.com/exec/system/1180573449
	// which claims to have a high-accuracy calculator.
	double[][] erfdata = {{0.0, 0.0, 1.0},
			      {0.5, 0.5204998778130465376827,
			       0.4795001221869534623173},
			      {1.0, 0.8427007929497148693412,
			       0.1572992070502851306588},
			      {2.0, 0.9953222650189527341621,
			       0.004677734981047265837931},
			      {2.9999, 0.9999778955735178391204,
			       2.21044264821608796383E-5},
			      {3.0001, 0.9999779234241298066712,
			       2.207657587019332879175E-5},
			      {3.4999, 0.9999992563614959586093,
			       7.43638504041390714601E-7},
			      {3.5001, 0.9999992574413813986773,
			       7.42558618601322736944E-7},
			      {3.9999, 0.9999999845700387844418,
			       1.54299612155581506077E-8},
			      {4.0001, 0.9999999845954352564099,
			       1.540456474359011558829E-8},
			      {4.1999, 0.9999999971420415814819,
			       2.857958418518096190004E-9},
			      {4.2001, 0.9999999971469679902422,
			       2.853032009757835240026E-9},
			      {4.4999, 0.9999999998032027437235,
			       1.967972562764988886172E-10},
			      {4.5001, 0.9999999998035650049504,
			       1.964349950496194270166E-10},
			      {4.9999, 0.9999999999984609723351,
			       1.539027664880482010329E-12},
			      {5.0001, 0.9999999999984641065089,
			       1.535893491062363546906E-12},
			      {10.0, 1.0, 2.088487583762544757001E-45}};

	System.out.println("erf tests ...");
	for (int i = 0; i < erfdata.length; i++) {
	    x = erfdata[i][0];
	    double erf = hrErf(x);
	    double erfc = hrErfc(x);
	    double erflim = i < (erfdata.length - 1)? 1.e-10: 1.e-9;
	    if (Math.abs(erf - erfdata[i][1]) > 1.e-10
		|| (Math.abs((erfc - erfdata[i][2])/erfdata[i][2])) > erflim) {
		System.out.format("hrErf(%g) = %s, hrErfc(%g) = %s"
				  + ", expecting %s, %s\n",
				  x, erf, x, erfc,
				  erfdata[i][1], erfdata[i][2]);
		errcount++;
	    }
	}
	for (int i = 0; i < erfdata.length; i++) {
	    x = erfdata[i][0];
	    double erf = Functions.erf(x);
	    double erfc = Functions.erfc(x);
	    if (Math.abs(erf - erfdata[i][1]) > 1.e-10
		|| (Math.abs((erfc - erfdata[i][2])/erfdata[i][2])) > 1.0e-10) {
		System.out.format("erf(%g) = %s, erfc(%g) = %s, expecting "
				  + "%s, %s\n",
				  x, erf, x, erfc,
				  erfdata[i][1], erfdata[i][2]);
		errcount++;
	    }
	}
	for (int i = 0; i < 150; i++) {
	    x = i / 10.0;
	    if (Functions.erf(x) != - Functions.erf(-x)) {
		System.out.format("erf(%g) = %g, erf(%g) = %g\n",
				  x, Functions.erf(x),
				  -x, Functions.erf(-x));
		errcount++;
	    }
	    if (Math.abs(Functions.erfc(x) - (1.0 - Functions.erf(x)))
		> 3.e-9) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, Functions.erfc(x));
		errcount++;
	    }
	    if (Math.abs(Functions.erfc(-x) - (1.0 - Functions.erf(-x)))
		> 1.e-9) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  -x, Functions.erf(-x),
				  -x, Functions.erfc(-x));
		errcount++;
	    }
	    if (Math.abs(Functions.erf(x) - hrErf(x)) > 1.e-10) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }
	    double elim = 1.e-10;
	    if (Math.abs(Functions.erfc(x) - hrErfc(x)) > elim) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }
	}

	for (int i = 0; i < 2000; i++) {
	    x = i / 400.0;
	    if (Functions.erf(x) != - Functions.erf(-x)) {
		System.out.format("erf(%g) = %g, erf(%g) = %g\n",
				  x, Functions.erf(x),
				  -x, Functions.erf(-x));
		errcount++;
	    }
	    if (Math.abs(Functions.erfc(x) - (1.0 - Functions.erf(x)))
		> 3.e-9) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, Functions.erfc(x));
		errcount++;
	    }
	    if (Math.abs(Functions.erfc(-x) - (1.0 - Functions.erf(-x)))
		> 1.e-9) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  -x, Functions.erf(-x),
				  -x, Functions.erfc(-x));
		errcount++;
	    }
	    if (Math.abs(Functions.erf(x) - hrErf(x)) > 1.e-10) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }
	    double elim = 1.e-10;
	    if (Math.abs(Functions.erfc(x) - hrErfc(x)) > elim) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }

	    if (Math.abs(Functions.erf(x, 100) - hrErf(x)) > 1.e-10) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }
	    if (Math.abs(Functions.erfc(x, 100) - hrErfc(x)) > elim) {
		System.out.format("erf(%g) = %g, erfc(%g) = %g\n",
				  x, Functions.erf(x),
				  x, hrErf(x));
		errcount++;
	    }
	}

	if (errcount > 0) {
	    System.out.println("erf and/or erfc failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Pochhammer's symbol tests ...");

	for (int i = 0; i < 1000; i++) {
	    double xp = i / 10.0;
	    for (int np = 0; np < 100; np++) {
		double y = Functions.poch(xp, np);
		double ey = ourPoch(xp, np);
		if (Math.abs((y - ey)/((ey == 0.0)? 1.0: ey)) > 1.e-10) {
		    System.out.format("(%g)%d : %s != %s\n", xp, np, y, ey);
		    errcount++;
		}
	    }
	}

	for (int i = 1; i < 1000; i++) {
	    double xp = -i / 10.0;
	    for (int np = 0; np < 100; np++) {
		double y = Functions.poch(xp, np);
		double ey = ourPoch(xp, np);
		if (Math.abs((y - ey)/((ey == 0.0)? 1.0: ey)) > 1.e-10) {
		    System.out.format("(%g)%d : %s != %s\n", xp, np, y, ey);
		    errcount++;
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("Pochhammer's symbol test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("hypergeometric function test ...");
	for (int i = 1; i < 5; i++) {
	    double z = i/10.0;
	    double value = ourHgF(0.5, 1.0, 1.5, -(z*z));
	    expected = Math.atan(z) / z;
	    if (Math.abs(value - expected) > 1.e-10) {
		System.out.format("at z = %g, ourHgF = %s, expected = %s\n",
				  z, value, expected);
		errcount++;
	    }
	}

	if (Math.abs(Functions.hgF(0.5, 1.0, 1.5, 0.0) - 1.0) > 1.e-10) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  0.0, Functions.hgF(0.5, 1.0, 1.5, 0.0), 1.0);
		errcount++;
	}

	System.out.println("hgF(0.5, 1.0, 1.5, z) test");
	for (int i = 1; i < 1000; i++) {
	    double z = i/10.0;
	    double value = Functions.hgF(0.5, 1.0, 1.5, -(z*z));
	    expected = Math.atan(z) / z;
	    if (Math.abs(value - expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		errcount++;
	    }
	}

	for (int i = 1; i < 1000; i++) {
	    double z = i/10.0;
	    double value = Functions.hgF(1, 2, 1.0, 1.5, -(z*z));
	    expected = Math.atan(z) / z;
	    if (Math.abs(value - expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		errcount++;
	    }
	}

	for (int i = 1; i < 1000; i++) {
	    double z = i/10.0;
	    double value = Functions.hgF(1, 2, 1, 1, 1.5, -(z*z));
	    expected = Math.atan(z) / z;
	    if (Math.abs(value - expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		errcount++;
	    }
	}

	double hgparms[][] = {{1.0, 2.0, 1.0},
			      {1.0, 2.0, 2.0},
			      {2.0, 1.0, 1.0},
			      {2.0, 1.0, 2.0}};

	System.out.println("hgF(a, b, c, z) test with a=b or b=c");
	for (double[] parms: hgparms) {
	    double a = parms[0];
	    double b = parms[1];
	    double c = parms[2];
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(a, b, c, z);
		expected = MathOps.pow(1-z, ((c==2.0)? -1: -2));
		if (Math.abs(value-expected) > 1.e-14) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%g,%g,%g,%g)\n",
				      a, b, c, z);
		    errcount++;
		}
	    }
	}

	for (double[] parms: hgparms) {
	    double a = parms[0];
	    int ia = (int)Math.round(a);
	    double b = parms[1];
	    double c = parms[2];
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(ia,1, b, c, z);
		expected = MathOps.pow(1-z, ((c==2.0)? -1: -2));
		if (Math.abs(value-expected) > 1.e-14) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%g,%g,%g,%g)\n",
				      a, b, c, z);
		    errcount++;
		}
	    }
	}

	for (double[] parms: hgparms) {
	    double a = parms[0];
	    int ia = (int)Math.round(a);
	    double b = parms[1];
	    int ib = (int)Math.round(b);
	    double c = parms[2];
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(ia,1, ib,1, c, z);
		expected = MathOps.pow(1-z, ((c==2.0)? -1: -2));
		if (Math.abs(value-expected) > 1.e-14) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%g,%g,%g,%g)\n",
				      a, b, c, z);
		    errcount++;
		}
	    }
	}


	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 3, 1.0, 1.0, z);
	    expected = ((1-z)<0?-1:1) * Math.pow(Math.abs(1-z),-1.0/3.0);
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d, %d, %d,%g,%g)\n",
				  1,3, 1,1, 1.0, z);
		errcount++;
	    }
	}

	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 3, 1, 1, 1.0, z);
	    expected = ((1-z)<0?-1:1) * Math.pow(Math.abs(1-z),-1.0/3.0);
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d, %d, %d,%g,%g)\n",
				  1,3, 1,1, 1.0, z);
		errcount++;
	    }
	}

	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 1, 1, 3, 1.0, z);
	    expected = ((1-z)<0?-1:1) * Math.pow(Math.abs(1-z),-1.0/3.0);
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d, %d, %d,%g,%g)\n",
				  1,3, 1,1, 1.0, z);
		errcount++;
	    }
	}

	if (false) {
	    System.out.println("Calling problem case "
			       + "F(2.0, -1.0, 3.0-1.0/3.0, 1.0/1.1)");
	    System.out.println(Functions.hgF(2.0, -1.0, 3.0-1.0/3.0, 1.0/1.1));
	}

	double aa = 1.0/3.0;
	double bb = 2.0;
	double cc = 4.0;
	double f1 =
	    (Functions.Gamma(cc)*Functions.Gamma(bb-aa))
	    / (Functions.Gamma(bb)*Functions.Gamma(cc-aa));
	double f2 =
	    (Functions.Gamma(cc)*Functions.Gamma(aa-bb))
	    / (Functions.Gamma(aa)*Functions.Gamma(cc-bb));

	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 3, 2, 1, 4.0, z);
	    // System.out.println("z = " + z);
	    if (z <= 1.0) expected = Functions.hgF(1.0/3.0, 2.0, 4.0, z);
	    else {
		/*
		System.out.format("F(%g,%g,%g,%g) and F(%g,%g,%g,%g)\n",
				  aa, 1-cc+aa, 1-bb+aa, 1/z,
				  bb, 1-cc+bb, 1-aa+bb, 1/z);
		*/
		expected = -f1*Math.pow(z,-aa)
		     *Functions.hgF(aa, 1-cc+aa, 1-bb+aa, 1/z)
		     + f2*Math.pow(z,-bb)
		     *Functions.hgF(bb,1-cc+bb,1-aa+bb, 1/z);
	    }
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d, %d,%d, %g,%g)\n",
				  1,3, 2,1, 1.0, z);
		errcount++;
	    }
	}


	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 3, 1.0, 1.0, z);
	    expected = ((1-z)<0?-1:1) * Math.pow(Math.abs(1-z),-1.0/3.0);
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d,%g,%g,%g)\n",
				  1,3, 1.0, 1.0, z);
		errcount++;
	    }
	}

	for (int i = 1; i < 40; i++) {
	    double z = (i-20)/10.0;
	    double value = Functions.hgF(1, 3, 2.0, 4.0, z);
	    if (z <= 1.0) expected = Functions.hgF(1.0/3.0, 2.0, 4.0, z);
	    else expected = -f1*Math.pow(z,-aa)
		     *Functions.hgF(aa, 1-cc+aa, 1-bb+aa, 1/z)
		     + f2*Math.pow(z,-bb)
		     *Functions.hgF(bb,1-cc+bb,1-aa+bb, 1/z);
	    if (Math.abs(value-expected) > 1.e-14) {
		System.out.format("at z = %g, hgF = %s, expected = %s\n",
				  z, value, expected);
		System.out.format("had called hgF(%d,%d, %d,%d, %g,%g)\n",
				  1,3, 2,1, 1.0, z);
		errcount++;
	    }
	}

	System.out.println("hypergeometric function, "
			   + "a or b a negative integer");
	for (int nn = 0; nn < 10; nn++) {
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF((double)(-nn), 3.0, 4.0, z);
		expected = ourHgF((double)(-nn), 3.0, 4.0, z);
		if (Math.abs((value-expected)/expected) > 1.e-8) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%d, %g, %g, %g)\n",
				      -nn, 3.0, 4.0, z);
		    errcount++;
		}
	    }
	}

	for (int nn = 0; nn < 10; nn++) {
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(-nn, 1, 3.0, 4.0, z);
		expected = ourHgF((double)(-nn), 3.0, 4.0, z);
		if (Math.abs((value-expected)/expected) > 1.e-8) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%d, %d, %g, %g, %g)\n",
				      -nn, 1, 3.0, 4.0, z);
		    errcount++;
		}
	    }
	}

	for (int nn = 0; nn < 10; nn++) {
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(-nn, 1, 3, 1, 4.0, z);
		expected = ourHgF((double)(-nn), 3.0, 4.0, z);
		if (Math.abs((value-expected)/expected) > 1.e-8) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%d,%d, %d,%d, %g, %g)\n",
				      -nn,1, 3,1, 4.0, z);
		    errcount++;
		}
	    }
	}

	// Use a set of arguments that will provide a value that
	// can be computed independently using Legendre polynomials.
	for (int nn = 0; nn < 10; nn++) {
	    for (int i = 1; i < 40; i++) {
		double z = (i-20)/10.0;
		double value = Functions.hgF(-nn, 1, nn+1, 1, 1.0, z);
		expected = Functions.P(nn, 1.0 - 2.0*z);
		if (Math.abs((value-expected)/expected) > 1.e-10) {
		    System.out.format("at z = %g, hgF = %s, expected = %s\n",
				      z, value, expected);
		    System.out.format("had called hgF(%d,%d, %d,%d, %g, %g)\n",
				      -nn,1, nn+1,1, 1.0, z);
		    errcount++;
		}
	    }
	}

	double hgSpecialCases[][] = {{2.5, 1.5, 2.0},
				     {2.5, 1.5, 3.0},
				     {2.5, 1.5, 4.0},
				     {2.5, 1.5, 5.0},
				     {2.5, 1.5, 6.0}
	};
	System.out.println("test some special values of a, b c");
	for (double[] parms: hgSpecialCases) {
	    double a = parms[0];
	    double b = parms[1];
	    double c = parms[2];
	    double z = 0.7;
	    double value = Functions.hgF(a, b, c, z);
	    expected = ourHgF(a, b, c, z);
	    if (Math.abs(value - expected) > 1.e-10) {
		System.out.format("F(%g, %g; %g; %g) = %s, expected %s\n",
				  a, b, c, z, value, expected);
		errcount++;
	    }
	    // check the two other variants
	    int an = (int) Math.round(2*a);
	    int ad = 2;
	    double value2 = Functions.hgF(an, ad, b, c, z);
	    if (Math.abs(value2 - value) > 1.e-10) {
		System.out.format("hgF(%d,$d, b, c z) = %s, not %s\n",
				  an, ad, b, c, z, value2, value);
	    }
	    int bn = (int) Math.round(2*b);
	    int bd = 2;
	    value2 = Functions.hgF(an, ad, bn, bd, c, z);
	    if (Math.abs(value2 - value) > 1.e-10) {
		System.out.format("hgF(%d,$d, %d,%d, b, c z) = %s, not %s\n",
				  an,ad, bn,bd, c, z, value2, value);
	    }
	}


	double hgParmsDeriv[][] = {{0.5, 2.0, 3.1},
			       {1.0, 2.1, 4.1},
			       {-0.5, -2.54, 1.1},
			       {-3.1, 4.1, 7.5}};

	System.out.println("test derivatives of F(a,b;c;z) with respect to z");
	for (double[] parms: hgParmsDeriv) {
	    double a = parms[0];
	    double b = parms[1];
	    double c = parms[2];
	    for (int i = 1; i < 20; i++) {
		double z = (i-20)/10.0;
		try {
		    double deriv = Functions.dhgFdx(a, b, c, z);
		    expected = (Functions.hgF(a, b, c, z+delta)
				- Functions.hgF(a, b, c, z))/delta;
		    if (Math.abs(deriv - expected) > 3.e-4) {
			System.out.format ("dhgF/dx: %s != %s, a=%g, b=%g, "
					   + "c=%g, z=%g\n",
					   deriv, expected, a, b, c, z);
			errcount++;
		    }
		} catch (IllegalArgumentException e) {
		    System.out.format("a=%g, b=%g, c=%g, z=%g\n",
				      a, b, c, z);
		    e.printStackTrace(System.out);
		    errcount++;
		    System.exit(1);
		}
		try {
		    double deriv = Functions.d2hgFdx2(a, b, c, z);
		    expected = (Functions.dhgFdx(a, b, c, z+delta)
				- Functions.dhgFdx(a, b, c, z))/delta;
		    if (Math.abs(deriv - expected) > 3.e-4) {
			System.out.format ("d2hgF/dx2: %s != %s, a=%g, b=%g, "
					   + "c=%g, z=%g\n",
					   deriv, expected, a, b, c, z);
			errcount++;
		    }
		} catch (IllegalArgumentException e) {
		    System.out.format("a=%g, b=%g, c=%g, z=%g\n",
				      a, b, c, z);
		    e.printStackTrace(System.out);
		    System.exit(1);
		}
	    }
	}

	if(Math.abs(Functions.hgF(0.5, 4.5, 1.5, -200.0)
		    - Functions.hgF(1,2, 9,2, 1.5, -200.0)) > 1.e-10) {
	    System.out.println("Functions.hgF(0.5, 4.5, 1.5, -200.0) != "
			       + "Functions.hgF(1,2, 9,2, 1.5, -200.0)");
	    errcount++;
	}

	if (Math.abs(Functions.hgF(0.5, 4.5, 1.5, -200.0)
		     - 0.03232488142024478024848) > 1.e-10) {
	    System.out.println("Functions.hgF(0.5, 4.5, 1.5, -200.0) = "
			       + Functions.hgF(0.5, 4.5, 1.5, -200.0)
			       + ", expecting "
			       + 0.03232488142024478024848);
	    System.out.println("using Equation 15.3.5:");
	    System.out.println(" = (1/sqrt(201))*F(0.5,1.5-4.5,1.5, 200/201) = "
			       + (Functions.hgF(0.5,-3.0, 1.5, 200.0/201.0)
				  / Math.sqrt(201.0)));
	    errcount++;
	}

	{
	    double t = 0.4;
	    int tnu = 3;
	    double value = Functions.hgF(1,2, tnu+1,2, 1.5, -t*t/tnu);
	    double expecting = ourHgF(0.5, (tnu+1)/2.0, 1.5, -t*t/tnu);
	    if (Math.abs(value-expecting)> 2e-10) {
		System.out.format("ttest case: %g, %d: %s != %s\n",
				  t, tnu, value, expecting);
		errcount++;
	    }
	}

	if (errcount > 0) {
	    System.out.println("hypergeometric-function test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("Confluent-hypergeometric-function test ...");

	// used http://keisan.casio.com/exec/system/1349143651 to get
	// test values.

	ChgParams[] chgParams = {
	    new ChgParams(1.1, 2.3, 4.2,13.76188617622117186948),
	    new ChgParams(2, 2.3, 4.1, 42.74831839659512019249 ),
	    new ChgParams(2, 3, 4.3, 26.41528601499904054179),
	    new ChgParams(-3.0, -4.0, 4.5,13.234375)
	};

	if (Math.abs(Functions.M(2, 2.3, 4.1)
		     - Functions.M(2.0, 2.3, 4.1)) > 1.e-14) {
	    throw new Exception("M mismatch");
	}

	if (Math.abs(Functions.M(2, 3, 4.3)
		     - Functions.M(2.0, 3.0, 4.3)) > 1.e-14) {
	    throw new Exception("M mismatch");
	}
	delta /= 100.0;
	for (ChgParams params: chgParams) {
	    double a = params.a;
	    double b = params.b;
	    double z = params.z;
	    double expecting = params.actualValue;
	    double value = Functions.M(a,b,z);
	    if (Math.abs(value - expecting) > 1.e-3) {
		System.out.format("M(%g,%g,%g) = %s, expecting %s\n",
				  a, b, z, value, expecting);
	    }
	    double deriv = Functions.dMdx(a,b,z);
	    expecting = (Functions.M(a,b,z+delta) - value)/delta;
	    if (Math.abs(deriv - expecting) > 1.e-3) {
		System.out.format("dMdx(%g,%g,%g) = %s, expecting %s\n",
				  a, b, z, deriv, expecting);
	    }
	    double deriv2 = Functions.d2Mdx2(a,b,z);
	    expecting = (Functions.dMdx(a,b,z+delta) - deriv)/delta;
	    if (Math.abs(deriv2 - expecting) > 1.e-3) {
		System.out.format("d2Mdx2(%g,%g,%g) = %s, expecting %s\n",
				  a, b, z, deriv, expecting);
	    }
	}

	if (errcount > 0) {
	    System.out.println("confluent hypergeometric-function test failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("timing test for Legendre Polynomials:");
	Polynomial p60 = Functions.LegendrePolynomial.asPolynomial(65);
	BezierPolynomial bp60 =
	    Functions.LegendrePolynomial.asBezierPolynomial(65);

	double sum = 0.0;
	int ilimit = 1000000;
	Runtime.getRuntime().gc();
	long t0 = System.nanoTime();
	for (int i = 0; i < ilimit; i++) {
	    sum += Functions.P(60, 0.5);
	}
	long t1 = System.nanoTime();
	for (int i = 0; i < ilimit; i++) {
	    sum += p60.valueAt(0.5);
	}
	long t2 = System.nanoTime();
	for (int i = 0; i < ilimit; i++) {
	    sum += bp60.valueAt(0.5);
	}
	long t3 = System.nanoTime();
	System.out.format("%d %d %d\n", (t1-t0)/1000L, (t2-t1)/1000L,
			  (t3-t2)/1000L);

	 t1 = System.nanoTime();
	for (int i = 0; i < ilimit; i++) {
	    double[] array1 = new double[61];
	    double[] array2 = new double[61];
	}
	t2 = System.nanoTime();
	System.out.format("array allocation time: %d\n",
			  (t2-t1)/1000L);

	System.exit(0);
    }
}
