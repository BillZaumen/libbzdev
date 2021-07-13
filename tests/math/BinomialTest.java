import org.bzdev.math.*;
import java.math.BigInteger;

public class BinomialTest {
    public static void main(String argv[]) throws Exception {
	int errcount = 0;
	System.out.println("Binomial test:");
	for (int i = 0; i < 10; i++) {
	    for (int j = 0; j <= i; j++) {
		long expected =
		    Math.round(Functions.factorial(i)
			       /(Functions.factorial(j)
				 *Functions.factorial(i-j)));
		if (Binomial.C(i,j) != expected) {
		    System.out.format
			("Binomial.C(%d,%d) = %d (expected %d)\n",
			 i, j, Binomial.C(i,j), expected);
		    errcount++;
		}
	    }
	}

	for (int i = 0; i < 10; i++) {
	    long[] results = Binomial.coefficients(i);
	    for (int j = 0; j < results.length; j++) {
		long expected =
		    Math.round(Functions.factorial(i)
			       / (Functions.factorial(j)
				  * Functions.factorial(i-j)));
		if (results[j] != expected) {
		    System.out.format
			("Binomial.coefficients(%d)[%d] = %d (expected %d)\n",
			 i, j, results[j], expected);
		    errcount++;
		}
	    }
	}

	for (int i = 0; i < 20; i++) {
	    long[] coeffs = Binomial.coefficients(i);
	    int j = 0;
	    for (long val: coeffs) {
		System.out.print(val + " ");
		double v = Binomial.coefficient(i, j);
		if (Math.abs((v - val)/val) > 1.e-14) {
		    System.out.format("bad value for C(%d,%d)\n", i, j);
		    errcount++;

		}
		j++;
	    }
	    System.out.println();
	}
	for (int i = 0; i < 100; i++) {
	    for (int j = 0; j <= i; j++) {
		double value = Binomial.coefficient(i,j);
		double expected =
		    Math.rint(Functions.factorial(i)
			      / (Functions.factorial(j)
				 * Functions.factorial(i-j)));
		if (Math.abs((value - expected) / expected) > 1.e-10) {
		    System.out.format
			("Binomial.coefficiens(%d,%d) = %14.12g "
			 + "(expected %14.12g)\n",
			 i, j, value, expected);
		    errcount++;
		}
	    }
	}

	for (int i = 0; i < 50; i++) {
	    for (int j = 0; j <= i; j++) {
		double value = Binomial.coefficient(i,j);
		double expected = Math.rint(value);
		if (Math.abs((value - expected) / expected) > 1.e-14) {
		    System.out.format
			("Binomial.coefficiens(%d,%d) = %14.12g "
			 + "(should be clsoe to  %14.12g)\n",
			 i, j, value, expected);
		    errcount++;
		}
	    }
	}


	for (int i = 300; i < 700; i++) {
	    for (int j = 0; j <= i; j++) {
		double expected =
		    Math.rint(Math.exp(Functions.logFactorial(i)
				       -(Functions.logFactorial(j)
					 + Functions.logFactorial(i-j))));
		if (Math.abs((Binomial.coefficient(i,j)- expected)/expected)
		    > 1.e-10) {
		    System.out.format
			("Binomial.coefficient(%d,%d) = %14.12g "
			 + "(expected %14.12g)\n",
			 i, j, Binomial.coefficient(i,j), expected);
		    errcount++;
		}
	    }
	}

	if (errcount > 0) {
	    System.out.println("tests failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("test for n = 1000:");
	double previous = Binomial.coefficient(1000,0);
	if (previous != 1.0) {
	    System.out.println("C(1000,0) = " + previous);
	    errcount++;
	}
	for (int k = 1; k < 50; k++) {
	    double next = Binomial.coefficient(1000,k);
	    double val = ((1000+1-k)*previous/k);
	    if (Math.abs((next - val)/next) > 1.e-11) {
		System.out.format("C(1000,%d) = %18.16g, expecting %18.16g\n",
				  k, Binomial.coefficient(1000,k), val);
		errcount++;
	    }
	    previous = next;
	}
	if (errcount > 0) {
	    System.out.println("tests failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}

	System.out.println("exactC test:");

	BigInteger[] array1 = Binomial.exactC(0);
	if (array1.length != 1 && array1[0].longValue() != 1) {
	    System.out.println("n = 0, m = 0: " + array1[0].longValue() + " ");
	    errcount++;
	}
	array1 = Binomial.exactC(array1, 1);
	if (array1.length != 2 && array1[0].longValue() != 1
	    && array1[1].longValue() != 1) {
	    System.out.println("n = 1, m = 0: " + array1[0].longValue() + " ");
	    System.out.println("n = 1, m = 1: " + array1[1].longValue() + " ");
	    errcount++;
	}
	array1 = Binomial.exactC(1);
	if (array1.length != 2 && array1[0].longValue() != 1
	    && array1[1].longValue() != 1) {
	    System.out.println("n = 1, m = 0: " + array1[0].longValue() + " ");
	    System.out.println("n = 1, m = 1: " + array1[1].longValue() + " ");
	    errcount++;
	}
	array1 = Binomial.exactC(array1, 2);
	if (array1.length != 3 && array1[0].longValue() != 1
	    && array1[1].longValue() != 2 && array1[2].longValue() != 1) {
	    System.out.println("n = 1, m = 0: " + array1[0].longValue() + " ");
	    System.out.println("n = 1, m = 1: " + array1[1].longValue() + " ");
	    System.out.println("n = 1, m = 2: " + array1[2].longValue() + " ");
	    errcount++;
	}
	array1 = Binomial.exactC(2);
	for (int m  = 0; m < array1.length; m++) {
	    if (array1[m].longValue() != Binomial.C(2,m)) {
		System.out.format("exactC(%d,%d) = %d, expecting %d\n",
				  2, m, array1[m].longValue(), Binomial.C(2,m));
		    errcount++;
	    }
	}

	for (int n = 3; n < 67; n++) {
	    BigInteger[] array2 = Binomial.exactC(array1, n);
	    for (int m  = 0; m < array2.length; m++) {
		if (array2[m].longValue() != Binomial.C(n,m)) {
		    System.out.print(array2[m].longValue() + " ");
		    errcount++;
		}
	    }
	    array1 = array2;
	}

	for (int n = 67; n < 700; n++) {
	    BigInteger[] array2 = Binomial.exactC(array1, n);
	    for (int m  = 0; m < array2.length; m++) {
		double value = array2[m].doubleValue();
		double expected = Binomial.coefficient(n,m);
		if (Math.abs((value-expected)/expected) > 1.e-10) {
		    System.out.format("exactC(%d,%d) = %14.12g, "
				      + "expected %14.12g\n",
				      n, m, value, expected);
		}
	    }
	    array1 = array2;
	}

	if (errcount > 0) {
	    System.out.println("exactC tests failed");
	    System.exit(1);
	} else {
	    System.out.println(" ... OK");
	}
	System.exit(0);
    }
}
