import org.bzdev.math.*;

public class VectorOpsTest {

    private static void specialCases() {
	double T[] = {
	    -0.9003489528419437,0.0,-0.43516866054027287
	};
	double N[] = {
	    -0.4287599754793279,0.17098806134192665,0.8870896044399887
	};
	double[] xp1 = VectorOps.crossProduct(T,N);
	double[] xp2 = new double[3];
	VectorOps.crossProduct(xp2, T, N);
	double[] xp3 = new double[3];
	VectorOps.crossProduct(xp3, 0, T, 0, N, 0);
	double[] xp4 = new double[3];
	System.arraycopy(T, 0, xp4, 0, 3);
	VectorOps.crossProduct(xp4, 0, xp4, 0, N, 0);

	System.out.format("xp1 = (%g, %g, %g)\n", xp1[0], xp1[1], xp1[2]);
	System.out.format("xp2 = (%g, %g, %g)\n", xp2[0], xp2[1], xp2[2]);
	System.out.format("xp3 = (%g, %g, %g)\n", xp3[0], xp3[1], xp3[2]);
	System.out.format("xp4 = (%g, %g, %g)\n", xp4[0], xp4[1], xp4[2]);

    }

    public static void main(String argv[]) throws Exception {

	specialCases();

	double[] v0 = {2.0, 7.0, -3.0};
	double[] v1 = {1.0, 3.0, 5.0};
	double[] v2 = {10.0, 11.0, 12.0};
	double[] result = new double[3];
	double[] array = new double[100];
	double[] array1 = new double[100];
	double[] array2 = new double[100];
	double[] v;
	double w;

	double[] cp = {3.0*12.0 - 11.0*5.0,
		       10.0*5.0 - 1.0*12.0,
		       11.0-30.0};

	double dotcp = v0[0]*cp[0] + v0[1]*cp[1] + v0[2] * cp[2];

	double norm = Math.sqrt(1.0 + 9.0 + 25.0);

	System.arraycopy(v1, 0, array1, 10, 3);
	System.arraycopy(v2, 0, array2, 20, 3);

	v = VectorOps.add(v1, v2);
	if (v[0] != 11.0 || v[1] != 14.0 || v[2] != 17.0) {
	    throw new Exception("add failed");
	}
	
	v = null;
	v = VectorOps.add(result, v1, v2);
	if (v[0] != 11.0 || v[1] != 14.0 || v[2] != 17.0) {
	    throw new Exception("add failed");
	}
	
	VectorOps.add(array, 30, array1, 10, array2, 20, 3);
	System.arraycopy(array, 30, result, 0, 3);
	v = result;
	if (v[0] != 11.0 || v[1] != 14.0 || v[2] != 17.0) {
	    throw new Exception("add failed");
	}

	v = VectorOps.add(null, 30, array1, 10, array2, 20, 3);
	System.arraycopy(v, 30, result, 0, 3);
	v = result;
	if (v[0] != 11.0 || v[1] != 14.0 || v[2] != 17.0) {
	    throw new Exception("add failed");
	}

	v = VectorOps.sub(v1, v2);
	if (v[0] != -9.0 || v[1] != -8.0 || v[2] != -7.0) {
	    System.out.format("%g, %g, %g\n", v[0], v[1], v[2]);
	    throw new Exception("sub failed");
	}

	v = null;
	v = VectorOps.sub(result, v1, v2);
	if (v[0] != -9.0 || v[1] != -8.0 || v[2] != -7.0) {
	    throw new Exception("sub failed");
	}

	VectorOps.sub(array, 30, array1, 10, array2, 20, 3);
	System.arraycopy(array, 30, result, 0, 3);
	v = result;
	if (v[0] != -9.0 || v[1] != -8.0 || v[2] != -7.0) {
	    throw new Exception("sub failed");
	}

	v = VectorOps.sub(null, 30, array1, 10, array2, 20, 3);
	System.arraycopy(v, 30, result, 0, 3);
	v = result;
	if (v[0] != -9.0 || v[1] != -8.0 || v[2] != -7.0) {
	    throw new Exception("sub failed");
	}

	v = VectorOps.multiply(2.0, v1);
	if (v[0] != 2.0 || v[1] != 6.0 || v[2] != 10.0) {
	    throw new Exception("multiply failed");
	}
	v = null;
	v = VectorOps.multiply(result, 2.0, v1);
	if (v[0] != 2.0 || v[1] != 6.0 || v[2] != 10.0) {
	    throw new Exception("multiply failed");
	}
	VectorOps.multiply(array, 40, 2.0, array1, 10, 3);
	System.arraycopy(array, 40, result, 0, 3);
	v = result;
	if (v[0] != 2.0 || v[1] != 6.0 || v[2] != 10.0) {
	    throw new Exception("multiply failed");
	}

	v = VectorOps.multiply(null, 40, 2.0, array1, 10, 3);
	System.arraycopy(v, 40, result, 0, 3);
	v = result;
	if (v[0] != 2.0 || v[1] != 6.0 || v[2] != 10.0) {
	    throw new Exception("multiply failed");
	}


	w = VectorOps.dotProduct(v1, v2);
	if (w != (10.0+33.0+60.0)) {
	    throw new Exception("dotProduct failed");
	}

	w = VectorOps.dotProduct(array1, 10, array2, 20, 3);
	if (w != (10.0+33.0+60.0)) {
	    throw new Exception("dotProduct failed");
	}

	v = VectorOps.crossProduct(v1, v2);
	if (v[0] != cp[0] || v[1] != cp[1] || v[2] != cp[2]) {

	    System.out.print("v =");
	    for (int i = 0; i < 3; i++) System.out.print(" " + v[i]);
	    System.out.println();
	    System.out.println("cp =");
	    for (int i = 0; i < 3; i++) System.out.print(" " + cp[i]);
	    System.out.println();
	    throw new Exception("crossProduct failed");
	}

	v = null;
	v = VectorOps.crossProduct(result, v1, v2);
	if (v[0] != cp[0] || v[1] != cp[1] || v[2] != cp[2]) {
	    throw new Exception("crossProduct failed");
	}

	
	VectorOps.crossProduct(array, 50, array1, 10, array2, 20);
	System.arraycopy(array, 50, result, 0, 3);
	v = result;
	if (v[0] != cp[0] || v[1] != cp[1] || v[2] != cp[2]) {
	    System.out.format("(%g,%g,%g) != (%g,%g,%g)\n",
			      v[0],v[1],v[2], cp[0], cp[1], cp[2]);
	    throw new Exception("crossProduct failed");
	}

	w = VectorOps.norm(v1);
	if (w != norm) {
	    throw new Exception("norm failed");
	}

	w = VectorOps.dotCrossProduct(v0, v1, v2);
	if (w != dotcp) {
	    System.out.println("w = " + w + ", dotcp = " + dotcp);
	    throw new Exception("dotCrossProduct failed");
	}

	w = VectorOps.dotCrossProduct(v0, 0, v1, 0, v2, 0);
	if (w != dotcp) {
	    throw new Exception("dotCrossProduct failed");
	}

	w = VectorOps.dotCrossProduct(v0, 0, array1, 10, array2, 20);
	if (w != dotcp) {
	    throw new Exception("dotCrossProduct failed");
	}


	w = VectorOps.norm(array1, 10, 3);
	if (w != norm) {
	    throw new Exception("norm failed");
	}

	w = VectorOps.norm(v2);
	VectorOps.normalize(v2);
	if (VectorOps.norm(v2) != 1.0) {
	    throw new Exception("normalize failed");
	}
	if (v2[0] != 10.0/w || v2[1] != 11.0/w || v2[2] != 12.0/w) {
	    throw new Exception("normalize failed");
	}

	VectorOps.normalize(array2, 20, 3);
	if (array2[20] != v2[0] || array2[21] != v2[1]
	    || array2[22] != v2[2]) {
	    throw new Exception("normalize failed");
	}

	w = VectorOps.norm(v1);
	v = VectorOps.unitVector(v1);
	if (Math.abs(v[0] - v1[0]/w) > 1.e-10
	    || Math.abs(v[1] - v1[1]/w) > 1.e-10
	    || Math.abs(v[2] - v1[2]/w) > 1.e-10) {
	    throw new Exception("unitVector failed");
	}

	v = null;
	v = VectorOps.unitVector(result, v1);
	if (Math.abs(v[0] - v1[0]/w) > 1.e-10
	    || Math.abs(v[1] - v1[1]/w) > 1.e-10
	    || Math.abs(v[2] - v1[2]/w) > 1.e-10) {
	    throw new Exception("unitVector failed");
	}

	VectorOps.unitVector(array, 60, array1, 10, 3);
	System.arraycopy(array, 60, result, 0, 3);
	v = result;
	if (Math.abs(v[0] - v1[0]/w) > 1.e-10
	    || Math.abs(v[1] - v1[1]/w) > 1.e-10
	    || Math.abs(v[2] - v1[2]/w) > 1.e-10) {
	    throw new Exception("unitVector failed");
	}

	v = VectorOps.unitVector(null, 60, array1, 10, 3);
	System.arraycopy(v, 60, result, 0, 3);
	v = result;
	if (Math.abs(v[0] - v1[0]/w) > 1.e-10
	    || Math.abs(v[1] - v1[1]/w) > 1.e-10
	    || Math.abs(v[2] - v1[2]/w) > 1.e-10) {
	    throw new Exception("unitVector failed");
	}
    }
}
