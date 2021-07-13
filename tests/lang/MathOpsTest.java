import org.bzdev.lang.MathOps;

// test the fround method in isolation (it is private in the
// p3d package

public class MathOpsTest {

    private static float fround(double val) {
	float v1 = (float)val;
	/*
	float v2  = Math.nextAfter(v1, val);
	return (Math.abs(v2-val) < Math.abs(v1-val))? v2: v1;
	*/
	return v1;
    }

    public static void main(String argv[]) throws Exception {
	
	double base = 2.0;
	double top = Math.nextUp((float) base);

	for (double x = base; x <= top; x = Math.nextUp(x)) {
	    double val = (float)(x);
	    if (val != base && val != top) {
		throw new Exception ("val does not have a float value");
	    }
	    if (Math.abs(x - val) > (top - base)/2.0) {
		throw new Exception("x - val = " + (x - val)
				    + ", (top-base)/2.0 = " 
				    + ((top - base)/2.0));
	    }
	    val = MathOps.fceil(x);
	    if (val < x) {
		throw new Exception("MathOps.fceil failed: x = " + x
				    +", val = " + val);
	    }
	    val = MathOps.ffloor(x);
	    if (val > x) {
		throw new Exception("MathOps.fceil failed: x = " + x
				    +", val = " + val);
	    }

	    val = MathOps.froundTowardZero(x);
	    if (Math.abs(val) > Math.abs(x)) {
		throw new Exception("MathOps.froundTowardZero failed: x = " + x
				    +", val = " + val);
	    }

	    val = MathOps.froundTowardInf(x);
	    if (Math.abs(val) < Math.abs(x)) {
		throw new Exception("MathOps.froundTowardInf failed: x = " + x
				    +", val = " + val);
	    }
	}

	base = -2.0;
	top = Math.nextUp((float) base);
	System.out.println("base = " + base);
	System.out.println("top = " + top);
	
	for (double x = base; x <= top; x = Math.nextUp(x)) {
	    double val = (float)(x);
	    if (val != base && val != top) {
		throw new Exception ("val does not have a float value");
	    }
	    if (Math.abs(x - val) > (top - base)/2.0) {
		throw new Exception("x - val = " + (x - val)
				    + ", (top-base)/2.0 = " 
				    + ((top - base)/2.0));
	    }
	    val = MathOps.fceil(x);
	    if (val < x) {
		throw new Exception("MathOps.fceil failed: x = " + x
				    +", val = " + val);
	    }
	    val = MathOps.ffloor(x);
	    if (val > x) {
		throw new Exception("MathOps.fceil failed: x = " + x
				    +", val = " + val);
	    }

	    val = MathOps.froundTowardZero(x);
	    if (Math.abs(val) > Math.abs(x)) {
		throw new Exception("MathOps.froundTowardZero failed: x = " + x
				    +", val = " + val);
	    }

	    val = MathOps.froundTowardInf(x);
	    if (Math.abs(val) < Math.abs(x)) {
		throw new Exception("MathOps.froundTowardInf failed: x = " + x
				    +", val = " + val);
	    }
	}
	System.exit(0);
    }
}
