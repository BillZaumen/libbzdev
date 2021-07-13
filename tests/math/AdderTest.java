import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import java.util.LinkedList;

public class AdderTest {

    public static void main(String argv[]) throws Exception {

	Adder adder = new Adder.Kahan();
	Adder adderp = new Adder.Pairwise();
	for (int i = 1; i <= 100; i++) {
	    adder.add((double)i);
	    adderp.add((double)i);
	}
	double sum = adder.getSum();
	double sump = adderp.getSum();
	if (sum != 5050.0) {
	    throw new Exception("sum = " + sum +  ", expected 5050.0");
	}
	if (sump != 5050.0) {
	    throw new Exception("sump = " + sump +  ", expected 5050.0");
	}

	Adder.Kahan adderk = new Adder.Kahan();
	Adder.Kahan.State state = adderk.getState();
	for (int i = 1; i <= 100; i++) {
	    double term = (double) i;
	    double y = term - state.c;
	    double t = state.total + y;
	    state.c = (t - state.total) - y;
	    state.total = t;
	}
	sum = adderk.getSum();
	if (sum != 5050.0) {
	    throw new Exception("(adderk) sum = " + sum +  ", expected 5050.0");
	}

	adder.reset();
	adderp.reset();

	// iterate long enough to be fairly sure that the JIT compiler
	// will optimize the add method.
	for (long i = 0; i < 1000000000; i++) {
	    adder.add(30.0 + i);
	}
	adder.reset();
	double isum = 0.0;
	for (int i = 0; i < 1000000; i++) {
	    double x = 20.0 + .0000001;
	    isum += x;
	    adder.add(x);
	    adderp.add(x);
	}

	for (int i = 0; i < 1000000; i++) {
	    double x = -20.0 + .0000001;
	    isum += x;
	    adder.add(x);
	    adderp.add(x);
	}
	sum = adder.getSum();
	sump = adderp.getSum();
	System.out.println("sum = " + sum + ", simple addition yields " + isum);
	System.out.println("sump = " + sump
			   + ", simple addition yields " + isum);
	if (Math.abs(sum - .2) > 3.e-9) {
	    throw new Exception("sum = " + sum + ", actual value is 0.2");
	}
	if (Math.abs(sump - .2) > 7.e-9) {
	    throw new Exception("sump = " + sump + ", actual value is 0.2");
	}

	for (int j = 0; j < 1000; j++ ) {
	    double total = 0.0;
	    double c = 0.0;
	    for (int i = 0; i < 2000000; i++) {
		double x = ((i%2 == 0)? 20.0: -20.0) + .0000001;
		double y = x - c;
		double t = total + y;
		c = (t - total) - y;
		total = t;
	    }
	    if (j == 999) {
		System.out.println("total = " + total);
	    }
	}

	double[] array = new double[100];
	LinkedList<Double> list  = new LinkedList<Double>();
	for (int i = 0; i < array.length; i++) {
	    array[i] = i + 1;
	    list.add(array[i]);
	}
	adder.reset();
	adderp.reset();
	adder.add(array);
	adderp.add(array);
	sum = adder.getSum();
	sump = adderp.getSum();
	double sumpp = Adder.Pairwise.getSum(array);
	if (sum != 5050.0) {
	    throw new Exception("sum = " + sum +", expecting 5050.0");
	}	
	if (sump != 5050.0) {
	    throw new Exception("sump = " + sump +", expecting 5050.0");
	}
	if (sumpp != 5050.0) {
	    throw new Exception("sumpp = " + sumpp +", expecting 5050.0");
	}

	adder.add(1.0);
	adderp.add(1.0);
	sum = adder.getSum();
	sump = adderp.getSum();

	if (sum != 5051.0) {
	    throw new Exception("sum = " + sum +", expecting 5050.0");
	}
	if (sump != 5051.0) {
	    throw new Exception("sump = " + sump +", expecting 5050.0");
	}

	adder.reset();
	adderp.reset();
	adder.add(list);
	adderp.add(list);
	sum = adder.getSum();
	sump = adderp.getSum();
	if (sum != 5050.0) {
	    throw new Exception("sum = " + sum +", expecting 5050.0");
	}
	if (sump != 5050.0) {
	    throw new Exception("sump = " + sump +", expecting 5050.0");
	}

	adder.reset();
	adderp.reset();
	adder.add(array, 50, 53);
	adderp.add(array, 50, 53);
	sum = adder.getSum();
	sump = adderp.getSum();
	if (Math.abs(sum - (array[50]+array[51]+array[52])) - 1.e-10 > 0) {
	    throw new Exception("sum = " + sum + " should have been "
				+ (array[50]+array[51]+array[52]));
	}
	if (Math.abs(sump - (array[50]+array[51]+array[52])) - 1.e-10 > 0) {
	    throw new Exception("sump = " + sump + " should have been "
				+ (array[50]+array[51]+array[52]));
	}

	adder.add(2.0);
	adderp.add(2.0);
	if (Math.abs(adder.getSum() - (sum+2.0)) > 1.e-10) {
	    throw new Exception("sum = " + adder.getSum()
				+ " should have been " + (sum + 2.0));
	}
	if (Math.abs(adderp.getSum() - (sum+2.0)) > 1.e-10) {
	    throw new Exception("sump = " + adderp.getSum()
				+ " should have been " + (sump + 2.0));
	}
	System.out.println("test 'add' for each array length between "
			   + "0 and 4095");

	for (int k = 0; k < 64; k++) {
	    for (int j = 0; j < 64; j++) {
		int n = k*64 + j;
		array = new double[n];
		for (int i = 0; i < array.length; i++) {
		    array[i] = i + 1;
		}
		adder.reset();
		adderp.reset();
		adder.add(array);
		adderp.add(array);
		if (Math.abs(adder.getSum() - adderp.getSum()) > 1.e-10) {
		    throw new Exception(adder.getSum() + " != "
					+ adderp.getSum());
		}
	    }
	}
	for (int k = 0; k < 64; k++) {
	    for (int j = 0; j < 64; j++) {
		int n = k*64 + j;
		array = new double[n];
		for (int i = 0; i < array.length; i++) {
		    array[i] = i + 1;
		}
		adder.add(array);
		adderp.add(array);
		adder.add(20.0);
		adderp.add(20.0);
	    }
	}
	if (Math.abs(adder.getSum() - adderp.getSum()) > 1.e-10) {
	    throw new Exception(adder.getSum() + " != "
				+ adderp.getSum());
	}

	System.out.println("Random-sequence test");

	IntegerRandomVariable irv = new UniformIntegerRV(1, 1024);
	IntegerRandomVariable crv = new UniformIntegerRV(0, 4);
	double value;
	int start;
	int end;
	int tmp;
	array = new double[1024];
	for (int i = 0; i < 1024; i++) {
	    array[i] = (double)(i+1);
	}
	for (int i = 0; i < 1000; i++) {
	    adder.reset();
	    adderp.reset();
	    for (int j = 0 ; j < 10000; j++) {
		switch(crv.next()) {
		case 0:
		    value = 2.5;
		    adderp.add(value);
		    adderp.add(value);
		    break;
		case 1:
		    start = irv.next();
		    end = irv.next();
		    if (start > end) {
			tmp = start;
			start = end;
			end = tmp;
		    }
		    adder.add(array, start, end);
		    adderp.add(array, start, end);
		    break;
		case 2:
		    adder.add(array);
		    adderp.add(array);
		    break;
		case 3:
		    list.clear();
		    int n = irv.next();
		    for (int k = 0; k < n; k++) {
			list.add(Double.valueOf((double)(k+1)));
		    }
		    adder.add(list);
		    adderp.add(list);
		default:
		    break;
		}
	    }
	    sum = adder.getSum();
	    sump = adderp.getSum();
	    if (Math.abs((sum - sump)/sum) > 1.e-4) {
		throw new Exception("sum = " + sum + ", sump = " + sump);
	    }
	}
	System.out.println("... OK");
	System.exit(0);
    }
}
