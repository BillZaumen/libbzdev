import org.bzdev.math.*;

public class ConstTest {

    static double expected[] = {
	1.0, 0.5,
	1.0/6, 0,
	-1.0/30, 0,
	1.0/42, 0,
	-1.0/30, 0,
	5.0/66, 0,
	-691.0/2730, 0,
	7.0/6, 0,
	-3617.0/510, 0,
	43867.0/798, 0,
	-174611.0/330, 0
    };

    public static void main(String argv[]) throws Exception {
	System.out.println("Euler's constant = "
			   + Constants.EULERS_CONSTANT);

	int errcount = 0;

	System.out.println("Bernoulli number test:");

	if (Constants.BernoulliNumber1(1) != -Constants.BernoulliNumber2(1)) {
	    System.out.format(" for index 1, the first and second "
			      + "Bernoulli numbers do not have opposite signs");
	    errcount++;
	}


	System.out.println("Bernoulli numbers (first and second)");
	double limit = 1.e-12;
	for (int i = 0; i < expected.length; i++) {
	    double value = Constants.BernoulliNumber2(i);
	    if (i == 14) limit *= 100.0;
	    if (i == 16) limit *= 10.0;
	    if (i == 18) limit *= 10.0;
	    if (i == 20) limit *= 100.0;
	    if (Math.abs(expected[i]- value) > limit) {
		System.out.format("%d: %14.12g should be %14.12g\n",
				  i, value, expected[i]);
		errcount++;
	    }
	}
	if (errcount > 0) {
	    System.out.println("Bernoulli number test failed.");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	/*
	for (int i = 0; i <= 30; i++) {
	    System.out.format("    %g\t%g\n",
			      Constants.BernoulliNumber1(i),
			      Constants.BernoulliNumber2(i));
	}
	*/
	System.exit(0);
    }
}
