import org.bzdev.math.*;

public class SimpsonsRuleTest {
    static class Parameters {
	double value;
    }
    public static void main(String argv[]) {
	try {
	    SimpsonsRule simp = new SimpsonsRule() {
		    protected double function(double t) {
			return 1.0/t ;
		    }
		};
	    SimpsonsRule<Parameters> simpp = new SimpsonsRule<Parameters>() {
		protected double function(double t, Parameters p) {
		    return 1/(t + ((p == null)? 0.0: p.value));
		}
	    };
	    Parameters parameters = new Parameters();
	    parameters.value = 5.0;
	    simpp.setParameters(parameters);

	    RealValuedFunction f = new RealValuedFunction() {
		    public double valueAt(double t) {return 1.0/t;}
		};
	    SimpsonsRule simpf = SimpsonsRule.newInstance(f);

	    double sum = 0.0;
	    int i, j;
	    for (i = 1, j = 1; i < 10; i++, j++) {
		if (j == 2) i--;
		sum += simp.integrate((double)i, (double)(j), 10);
		System.out.println("t = " + (j) + ", value = " + sum
				   + ", expected " + Math.log((double)(j)));
	    }

	    System.out.println("integral (parameter.value=5.0) = "
			       + simpp.integrate(1.0, 2.0, 100)
			       + ", expected "
			       + (Math.log(2.0+5.0) - Math.log(5.0 + 1.0)));
	    System.out.println("integral (explicit parameter.value=5.0) = "
			       + simpp.integrateWithP(1.0, 2.0, 100, parameters)
			       + ", expected "
			       + (Math.log(2.0+5.0) - Math.log(5.0 + 1.0)));

	    double[] args = simp.getArguments(2.0,  5.0, 100);
	    System.out.println("simp.integrate(2,5,100) = "
			       + simp.integrate(2.0, 5.0, 100)
			       + "\nsimpf.integrate(2,5,100) = "
			       + simpf.integrate(2.0, 5.0, 100)
			       + "\nsimp.integrate(args) = "
			       + simp.integrate(args));


	    System.out.println("-------------");
	    System.out.println(simp.integrate(2.0, 2.1)
			       + " " + simp.integrate(2.0, 2.1, 1));
			       
	    System.out.println("-------------");
	    double values[] = new double[11];
	    for (i = 0; i < 11; i++) {
		values[i] = 1.0/(1.0 + (i)/10.0);
	    }
	    System.out.println(SimpsonsRule.integrate(1.0, 2.0, values, 11)
			       + " " + Math.log (2.0));
	    
	    System.out.println("-------------");
	     values = new double[10];
	    for (i = 0; i < 10; i++) {
		values[i] = 1.0/(1.0 + (i)/9.0);
	    }
	    System.out.println(SimpsonsRule.integrate(1.0, 2.0, values, 10)
			       + " " + Math.log (2.0));

	} catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
