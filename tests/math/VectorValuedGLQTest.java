import org.bzdev.math.*;

// Same as GLQuadratureTest, but also create VectorValuedGLQ
// objects whose integrals will be multiples of the corresponding
// GLQuadarture case.


public class VectorValuedGLQTest {
    static class Parameters {
	double value;
    }

    // test function
    public static void main(String argv[]) throws Exception {
	int max = 16;
	int lower = 2;
	int m = 5;
	if (argv.length == 1) {
	    max = Integer.parseInt(argv[0]);
	} else if (argv.length == 2) {
	    max = Integer.parseInt(argv[0]);
	    lower = Integer.parseInt(argv[1]);
	    if (lower < 2) lower = 2;
	}

	double val;
	double val2;
	double val3;
	double vals[] = new double[m];
	double vals2[] = new double[m];
	double vals3[] = new double[m];


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

	    VectorValuedGLQ vquad = new VectorValuedGLQ(i, m) {
		    protected void mapping(double[] results, int m, double x) {
			for (int i = 0; i < m; i++) {
			    results[i] = (i+1)*(1/x);
			}
		    }
		};

	    VectorValuedGLQ<Parameters> vquadp = new VectorValuedGLQ<>(i, m) {
		    @Override
		    protected void mapping(double[] results, int m, double x,
					   Parameters p)
		    {
			for (int i = 0; i < m; i++) {
			    results[i] =
				(i+1)*(1/(x + ((p == null)? 0.0: p.value)));
			}
		    }
		};

	    if (quad.getNumberOfPoints() != vquad.getNumberOfPoints())
		throw new Exception();

	    if (quadp.getNumberOfPoints() != vquadp.getNumberOfPoints())
		throw new Exception();

	    if (vquad.getResultLength() != m) throw new Exception();

	    Parameters p = new Parameters();
	    p.value = 5.0;
	    quadp.setParameters(p);
	    vquadp.setParameters(p);

	    val = quad.integrate(2.0, 5.0);
	    val2 = quad.integrate(2.0, 5.0, 2);
	    val3 = quad.integrate(2.0, 5.0, 3);
	    
	    vquad.integrate(vals, 2.0, 5.0);
	    vquad.integrate(vals2, 2.0, 5.0, 2);
	    vquad.integrate(vals3, 2.0, 5.0, 3);

	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		    throw new Exception();
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val2 - vals2[j]) > 1.e-10) {
		    System.out.format("j = %d, (j+1)*val2=%s, vals2[%d] = %s\n",
				      j, (j+1)*val2, j, vals2[j]);
		    throw new Exception();
		}
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val3 - vals3[j]) > 1.e-10)
		    throw new Exception();
	    }
	    
	    val = quadp.integrate(2.0, 5.0);
	    val2 = quadp.integrate(2.0, 5.0, 2);
	    val3 = quadp.integrate(2.0, 5.0, 3);

	    vquadp.integrate(vals,2.0, 5.0);
	    vquadp.integrate(vals2, 2.0, 5.0, 2);
	    vquadp.integrate(vals3, 2.0, 5.0, 3);
	    
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		    throw new Exception();
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val2 - vals2[j]) > 1.e-10)
		    throw new Exception();
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val3 - vals3[j]) > 1.e-10)
		    throw new Exception();
	    }

	    double[] args = quad.getArguments(2.0,  5.0);
	    double[] vargs = vquad.getArguments(2.0,  5.0);

	    val = quad.integrate(args);
	    vquad.integrate(vals, vargs);
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		    throw new Exception();
	    }

	    val = quadp.integrateWithP(2.0, 5.0, p);
	    val2 = quadp.integrateWithP(2.0, 5.0, 2, p);
	    val3 = quadp.integrateWithP(2.0, 5.0, 3, p);

	    vquadp.integrateWithP(vals, 2.0, 5.0, p);
	    vquadp.integrateWithP(vals2, 2.0, 5.0, 2, p);
	    vquadp.integrateWithP(vals3, 2.0, 5.0, 3, p);

	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		    throw new Exception();
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val2 - vals2[j]) > 1.e-10)
		    throw new Exception();
	    }
	    for (int j = 0; j < m; j++) {
		if (Math.abs((j+1)*val3 - vals3[j]) > 1.e-10)
		    throw new Exception();
	    }
	}

	GLQuadrature q = new GLQuadrature(8) {
		protected double function(double x) {
		    return 1/x;
		}
	    };
	VectorValuedGLQ vq = new VectorValuedGLQ(8, m) {
		protected void mapping(double[] results, int m, double x) {
		    for (int i = 0; i < m; i++) {
			results[i] = (i+1)*(1/x);
		    }
		}
	    };

	GLQuadrature<Parameters> qp = new GLQuadrature<Parameters>(8) {
	    protected double function(double x, Parameters p) {
		return 1/(x + ((p == null)? 0.0: p.value));
	    }
	};

	VectorValuedGLQ<Parameters> vqp =
	    new VectorValuedGLQ<Parameters>(8, m) {
		protected void mapping(double[] results, int m, double x,
				       Parameters p)
		{
		    for (int i = 0; i < m; i++) {
			results[i] =
			(i+1)*(1/(x + ((p == null)? 0.0: p.value)));
		    }
		}
	};

	Adder adder = new Adder.Kahan();
	Adder adderp = new Adder.Pairwise();

	Adder[] adders = new Adder[m];
	Adder[] adderps = new Adder[m];
	for (int j = 0; j < m; j++) {
	    adders[j] = new Adder.Kahan();
	    adderps[j] = new Adder.Pairwise();
	}


	Parameters pp = new Parameters();
	pp.value = 5.0;
	qp.setParameters(pp);
	vqp.setParameters(pp);
	double v = q.integrate(2.0, 5.0);

	q.integrate(adder, 2.0, 5.0);
	q.integrate(adderp, 2.0, 5.0);
	vq.integrate(adders, 2.0, 5.0);
	vq.integrate(adderps, 2.0, 5.0);
	
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}
	
	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}
	q.integrate(adder, 2.0, 5.0, 3);
	q.integrate(adderp, 2.0, 5.0, 3);
	vq.integrate(adders, 2.0, 5.0, 3);
	vq.integrate(adderps, 2.0, 5.0, 3);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}

	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}

	qp.integrate(adder, 2.0, 5.0);
	qp.integrate(adderp, 2.0, 5.0);
	vqp.integrate(adders, 2.0, 5.0);
	vqp.integrate(adderps, 2.0, 5.0);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}

	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}


	qp.integrate(adder, 2.0, 5.0, 3);
	qp.integrate(adderp, 2.0, 5.0, 3);
	vqp.integrate(adders, 2.0, 5.0, 3);
	vqp.integrate(adderps, 2.0, 5.0, 3);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}

	pp.value = 0.0;
	qp.setParameters(pp);
	vqp.setParameters(pp);
	pp.value = 5.0;

	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}

	qp.integrateWithP(adder, 2.0, 5.0, pp);
	qp.integrateWithP(adderp, 2.0, 5.0, pp);
	vqp.integrateWithP(adders, 2.0, 5.0, pp);
	vqp.integrateWithP(adderps, 2.0, 5.0, pp);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}

	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}

	qp.integrateWithP(adder, 2.0, 5.0, 3, pp);
	qp.integrateWithP(adderp, 2.0, 5.0, 3, pp);
	vqp.integrateWithP(adders, 2.0, 5.0, 3, pp);
	vqp.integrateWithP(adderps, 2.0, 5.0, 3, pp);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}

	adder.reset(); adderp.reset();
	for (int j = 0; j < m; j++) {
	    adders[j].reset();
	    adderps[j].reset();
	}

	double[] arguments = qp.getArguments(2.0,  5.0);

	qp.integrateWithP(adder, arguments, pp);
	qp.integrateWithP(adderp,arguments, pp);
	vqp.integrateWithP(adders, arguments, pp);
	vqp.integrateWithP(adderps,arguments, pp);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*adder.getSum() - adders[j].getSum()) > 1.e-10)
		throw new Exception();
	    if (Math.abs((j+1)*adderp.getSum() - adderps[j].getSum()) > 1.e-10)
		throw new Exception();
	}


	RealValuedFunction f = new RealValuedFunction() {
		public double valueAt(double t) {return 1.0/t;}
	    };

	RealToVectorMap map = new RealToVectorMap() {
		public void apply(double[] result, int m, double t) {
		    for (int i = 0; i < m; i++) {
			result[i] = (i+1)*(1.0/t);
		    }
		}
	    };

	GLQuadrature quadf = GLQuadrature.newInstance(f, 16);
	VectorValuedGLQ vquadf = VectorValuedGLQ.newInstance(map, 16, m);
	val = quadf.integrate(2.0, 5.0);
	vquadf.integrate(vals, 2.0, 5.0);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		throw new Exception();
	}

	quadf = GLQuadrature.newInstance((t) -> 1.0/t, 16);
	vquadf = VectorValuedGLQ.newInstance((results, mm, t) -> {
		for (int i = 0; i < mm; i++) {
		    results[i] = (i+1)*(1.0/t);
		}
	    }, 16, m);

	val = quadf.integrate(2.0, 5.0);
	vquadf.integrate(vals, 2.0, 5.0);
	for (int j = 0; j < m; j++) {
	    if (Math.abs((j+1)*val - vals[j]) > 1.e-10)
		throw new Exception();
	}

	System.exit(0);
    }
}
