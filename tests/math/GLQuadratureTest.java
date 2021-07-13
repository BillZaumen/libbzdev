import org.bzdev.math.*;

public class GLQuadratureTest {
    static class Parameters {
	double value;
    }

    // test function
    public static void main(String argv[]) throws Exception {
	int max = 16;
	int lower = 2;
	if (argv.length == 1) {
	    max = Integer.parseInt(argv[0]);
	} else if (argv.length == 2) {
	    max = Integer.parseInt(argv[0]);
	    lower = Integer.parseInt(argv[1]);
	    if (lower < 2) lower = 2;
	}

	for (int i = lower; i <= max; i++) {
	    GLQuadrature quad = new GLQuadrature(i) {
		    protected double function(double x) {
			return 1/x;
		    }
		};
	    GLQuadrature<Parameters> quadp = new GLQuadrature<Parameters>(i) {
		protected double function(double x, Parameters p) {
		    return 1/(x + ((p == null)? 0.0: p.value));
		}
	    };
	    Parameters p = new Parameters();
	    p.value = 5.0;
	    quadp.setParameters(p);

	    System.out.println("n = " + quad.getNumberOfPoints() 
			       + ", integral = "
			       + quad.integrate(2.0, 5.0)
			       +", " + quad.integrate(2.0, 5.0, 2)
			       +", " + quad.integrate(2.0, 5.0, 3));
	    double[] args = quad.getArguments(2.0,  5.0);
	    System.out.println(" ... using getArguments variant, integral = "
			       + quad.integrate(args));
	    System.out.println("n = " + quadp.getNumberOfPoints()
			       + ", integral (p.value = 5.0) = "
			       + quadp.integrate(2.0, 5.0)
			       +", " + quadp.integrate(2.0, 5.0, 2)
			       +", " + quadp.integrate(2.0, 5.0, 3));
	    System.out.println("n = " + quadp.getNumberOfPoints()
			       + ", integral (explicit p.value = 5.0) = "
			       + quadp.integrateWithP(2.0, 5.0, p)
			       +", " + quadp.integrateWithP(2.0, 5.0, 2, p)
			       +", " + quadp.integrateWithP(2.0, 5.0, 3, p));
	}

	System.out.println("exact: " + (Math.log(5.0) - Math.log(2.0)));
	System.out.println("exact with p.value = 5.0: "
			   + (Math.log(5.0+5.0) - Math.log(2.0 + 5.0)));

	Adder adder = new Adder.Kahan();
	Adder adderp = new Adder.Pairwise();
	
	GLQuadrature q = new GLQuadrature(8) {
		protected double function(double x) {
		    return 1/x;
		}
	    };
	GLQuadrature<Parameters> qp = new GLQuadrature<Parameters>(8) {
	    protected double function(double x, Parameters p) {
		return 1/(x + ((p == null)? 0.0: p.value));
	    }
	};
	Parameters pp = new Parameters();
	pp.value = 5.0;
	qp.setParameters(pp);
	double v = q.integrate(2.0, 5.0);
	q.integrate(adder, 2.0, 5.0);
	q.integrate(adderp, 2.0, 5.0);
	System.out.println("adder test: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());

	adder.reset(); adderp.reset();
	v = q.integrate(2.0, 5.0, 3);
	q.integrate(adder, 2.0, 5.0, 3);
	q.integrate(adderp, 2.0, 5.0, 3);
	System.out.println("adder test: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());
	
	adder.reset(); adderp.reset();
	v = qp.integrate(2.0, 5.0);
	qp.integrate(adder, 2.0, 5.0);
	qp.integrate(adderp, 2.0, 5.0);
	System.out.println("adder test with param: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());

	adder.reset(); adderp.reset();
	v = qp.integrate(2.0, 5.0, 3);
	qp.integrate(adder, 2.0, 5.0, 3);
	qp.integrate(adderp, 2.0, 5.0, 3);
	System.out.println("adder test with param: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());

	pp.value = 0.0;
	qp.setParameters(pp);
	pp.value = 5.0;

	adder.reset(); adderp.reset();
	v = qp.integrateWithP(2.0, 5.0, pp);
	qp.integrateWithP(adder, 2.0, 5.0, pp);
	qp.integrateWithP(adderp, 2.0, 5.0, pp);
	System.out.println("adder test with param: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());

	adder.reset(); adderp.reset();
	v = qp.integrateWithP(2.0, 5.0, 3, pp);
	qp.integrateWithP(adder, 2.0, 5.0, 3, pp);
	qp.integrateWithP(adderp, 2.0, 5.0, 3, pp);
	System.out.println("adder test with param: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());

	adder.reset(); adderp.reset();
	double[] arguments = qp.getArguments(2.0,  5.0);

	v = qp.integrateWithP(arguments, pp);
	qp.integrateWithP(adder, arguments, pp);
	qp.integrateWithP(adderp,arguments, pp);
	System.out.println("adder test with param: " +v + ", " + adder.getSum()
			   + ", " + adderp.getSum());


	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double t) {return 1.0/t;}
	    };

	GLQuadrature quadf = GLQuadrature.newInstance(f, 16);
	System.out.println("For newInstance case, integral = "
			   + quadf.integrate(2.0, 5.0));

	quadf = GLQuadrature.newInstance((t) -> 1.0/t, 16);
	System.out.println("For newInstance case, integral = "
			   + quadf.integrate(2.0, 5.0));

	System.out.println("Test getWeights()");

	arguments = GLQuadrature.getArguments(1.0, 10.0, 5);
	double[] weights = GLQuadrature.getWeights(1.0, 10.0, 5);
	GLQuadrature glq8 = new GLQuadrature(5) {
		public double function(double x) {
		    double y = x*x;
		    y = y*y;
		    y = y*y;
		    return y;
		}
	    };

	adder = new Adder.Kahan();
	for (int i = 0; i < 5; i++) {
	    double x = arguments[i];
	    double y = x*x;
	    y = y*y;
	    y = y*y;
	    adder.add(y*weights[i]);
	}
	// the integral of x^8 is (x^9)/9
	double exact = 10.0*10.0;
	exact = exact*exact;
	exact = exact*exact;
	exact *= 10.0;
	exact -= 1.0;
	exact /= 9.0;

	double glqValue = glq8.integrate(1.0, 10.0);

	if (Math.abs(adder.getSum() - exact)/exact > 1.e-10) {
	    System.out.println("integral = " + adder.getSum()
			       + ", expected " + exact
			       + ", numeric integration yields "
			       + glqValue);
	}

	System.exit(0);
    }
}
