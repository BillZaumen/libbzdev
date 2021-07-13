import org.bzdev.math.*;

public class PermTest {
    public static void main(String argv[]) throws Exception {
	int[] parray = {1, 2, 0};
	Permutation p = new Permutation(parray);

	double[] vector = {0.0, 1.0, 2.0};
	double[][] matrix = {{0.0, 1.0, 2.0},
			     {1.0, 1.0, 2.0},
			     {2.0, 1.0, 2.0}};

	double[] y1 = p.applyTo(vector);
	double[][] m = p.applyTo(matrix);

	double[][] mm = p.leftMultiplyBy(matrix);
	
	double[][] pm = p.getMatrix();
	for(int i = 0; i< 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(pm[i][j] + " ");
	    }
	    System.out.println();
	}

	double[][] pmm = p.inverse().getMatrix();
	System.out.println("-- inverse matrix");
	for(int i = 0; i< 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(pmm[i][j] + " ");
	    }
	    System.out.println();
	}
	System.out.println("-- m");
	for(int i = 0; i< 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(m[i][j] + " ");
	    }
	    System.out.println();
	}
	System.out.println("-- mm");
	for(int i = 0; i< 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(mm[i][j] + " ");
	    }
	    System.out.println();
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		double val = 0.0;
		for (int k = 0; k < 3; k++) {
		    val += matrix[i][k]*pm[k][j];
		}
		if (Math.abs(val - mm[i][j]) > 1.e-10) {
		    System.out.println("leftMultiply gave bad results");
		    System.exit(1);
		}
	    }
	}

	System.out.println("-- matrix");

	for(int i = 0; i< 3; i++) {
	    for (int j = 0; j < 3; j++) {
		System.out.print(matrix[i][j] + " ");
	    }
	    System.out.println();
	}
	System.out.println("-- y3");

	double[][] y3 = new double[3][3];

	try {
	    if (y1[0] != 1.0 && y1[1] != 2.0 &&  y1[2] != 0.0)
		throw new Exception("bad permutation");
	    if (m[1][0] != 1.0 && m[1][0] != 2.0 &&  m[2][0] != 0.0)
		throw new Exception("bad permutation");
	    
	    boolean flag = false;
	    for (int i = 0; i < 3; i++) {
		for (int j = 0; j < 3; j++) {
		    for (int k = 0; k < 3; k++) {
			y3[i][j] += pm[i][k]*matrix[k][j];
		    }
		    System.out.print(y3[i][j] + " ");
		    if (Math.abs(m[i][j] - y3[i][j]) > 1.e-10) {
			flag = true;
		    }
		}
		System.out.println();
	    }
	    if (flag)
		throw new Exception("getMatrix() gave bad results");
	    System.out.println("--");

	    Permutation inv = p.inverse();
	    double[] y2 = inv.applyTo(y1);
	    for (int i = 0; i < vector.length; i++) {
		if (y2[i] != vector[i]) {
		    throw new Exception("bad inverse permutation");
		}
	    }	    
	    Permutation p1 = new Permutation(3);
	    p1.swap(0,2);
	    p1.swap(0,1);
	    int[] p1vec = p1.getVector();
	    int[][] cycles = {{2, 1, 0}};
	    Permutation p2 = new Permutation(cycles, 3);
	    int[] p2vec = p2.getVector();
	    for (int i = 0; i < 3; i++) {
		if (parray[i] != p1vec[i] && parray[i] != p2vec[i]) {
		    throw new Exception("permutations differ");
		}
	    }
	    if (p.det() != p1.det() || p.det() != p2.det()) {
		System.out.println("p.det() = " + p.det());
		System.out.println("p1.det() = " + p1.det());
		System.out.println("p2.det() = " + p2.det());
		throw new Exception("permutation determinate  differ");
	    }
	    int[][] carray = {{2, 3, 4},
			      {5, 6, 7},
			      {0, 1}};
	    p = new Permutation(carray, 10);
	    System.out.println("Cycles for Pemutation(carray, 10):");
	    cycles = p.getCycles();
	    for (int i = 0; i < cycles.length; i++) {
		for (int j = 0; j < cycles[i].length; j++) {
		    System.out.print(cycles[i][j] + " ");
		}
		System.out.println();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.out.println("permutation test succeeded");
	System.exit(0);
    }
}
