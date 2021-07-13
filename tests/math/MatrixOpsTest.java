import org.bzdev.math.MatrixOps;


public class MatrixOpsTest {

    public static void vectorReflectTest(double[][] rm1,
					 double[][] rm2,
					 boolean colOrder)
	throws Exception
    {
	if (false) {
	    System.out.println("r1:");
	    for (int i = 0; i < rm1.length; i++) {
		for  (int  j = 0; j < rm1[0].length; j++) {
		    System.out.print("  " + rm1[i][j]);
		}
		System.out.println();
	    }
	}

	double[] rm1f = MatrixOps.flatten(rm1, colOrder);
	double vm1[] = new double[3*rm1f.length];
	for (int i = 0; i < rm1f.length; i++) {
	    int i3 = 3*i;
	    vm1[i3] = rm1f[i];
	    vm1[i3+1] = rm1f[i]+0.1;
	    vm1[i3+2] = rm1f[i]+0.2;
	}
	double[] vm1a = new double[vm1.length];
	MatrixOps.reflect(vm1a, 0, vm1, 0, rm1.length, rm1[0].length, colOrder,
			  0, 3, 0, 3);
	MatrixOps.reflect(vm1a, 0,vm1, 0, rm1.length, rm1[0].length, colOrder,
			  1, 3, 1, 3);
	MatrixOps.reflect(vm1a, 0, vm1, 0, rm1.length, rm1[0].length, colOrder,
			  2, 3, 2, 3);
	if (false) {
	    System.out.print("vm1a:");
	    for (int i = 0; i < vm1a.length; i++) {
		System.out.print(" " + vm1a[i]);
	    }
	    System.out.println();
	}

	double[] vm1b = new double[vm1.length];
	double[] vm1btmp = new double[vm1.length];
	MatrixOps.reflectRows(vm1btmp, 0, vm1, 0,
			      rm1.length, rm1[0].length, colOrder,
			      0, 3, 0, 3);
	MatrixOps.reflectRows(vm1btmp, 0, vm1, 0,
			      rm1.length, rm1[0].length, colOrder,
			      1, 3, 1, 3);
	MatrixOps.reflectRows(vm1btmp, 0, vm1,
			      0, rm1.length, rm1[0].length, colOrder,
			      2, 3, 2, 3);
	MatrixOps.reflectColumns(vm1b, 0, vm1btmp, 0,
				 rm1.length, rm1[0].length, colOrder,
				 0, 3, 0, 3);
	MatrixOps.reflectColumns(vm1b, 0, vm1btmp, 0,
				 rm1.length, rm1[0].length, colOrder,
				 1, 3, 1, 3);
	MatrixOps.reflectColumns(vm1b, 0, vm1btmp,
				 0, rm1.length, rm1[0].length, colOrder,
				 2, 3, 2, 3);
	if (false) {
	    System.out.print("vm1b:");
	    for (int i = 0; i < vm1b.length; i++) {
		System.out.print(" " + vm1b[i]);
	    }
	    System.out.println();
	}
	for (int i = 0; i < vm1.length; i++) {
	    if (vm1a[i] != vm1b[i]) {
		throw new Exception("i = " + i + ", " + vm1a[i]
				    + " != " + vm1b[i]);
	    }
	}

	double[] rm2f = MatrixOps.flatten(rm2, colOrder);
	double vm2[] = new double[3*rm2f.length];
	for (int i = 0; i < rm2f.length; i++) {
	    int i3 = 3*i;
	    vm2[i3] = rm2f[i];
	    vm2[i3+1] = rm2f[i]+0.1;
	    vm2[i3+2] = rm2f[i]+0.2;
	}
	double[] vm2a = new double[vm2.length];
	MatrixOps.reflect(vm2a, 0, vm2, 0, rm2.length, rm2[0].length, colOrder,
			  0, 3, 0, 3);
	MatrixOps.reflect(vm2a, 0,vm2, 0, rm2.length, rm2[0].length, colOrder,
			  1, 3, 1, 3);
	MatrixOps.reflect(vm2a, 0, vm2, 0, rm2.length, rm2[0].length, colOrder,
			  2, 3, 2, 3);

	double[] vm2b = new double[vm2.length];
	double[] vm2btmp = new double[vm2.length];
	MatrixOps.reflectRows(vm2btmp, 0, vm2, 0,
			      rm2.length, rm2[0].length, colOrder,
			      0, 3, 0, 3);
	MatrixOps.reflectRows(vm2btmp, 0, vm2, 0,
			      rm2.length, rm2[0].length, colOrder,
			      1, 3, 1, 3);
	MatrixOps.reflectRows(vm2btmp, 0, vm2,
			      0, rm2.length, rm2[0].length, colOrder,
			      2, 3, 2, 3);
	MatrixOps.reflectColumns(vm2b, 0, vm2btmp, 0,
				 rm2.length, rm2[0].length, colOrder,
				 0, 3, 0, 3);
	MatrixOps.reflectColumns(vm2b, 0, vm2btmp, 0,
				 rm2.length, rm2[0].length, colOrder,
				 1, 3, 1, 3);
	MatrixOps.reflectColumns(vm2b, 0, vm2btmp,
				 0, rm2.length, rm2[0].length, colOrder,
				 2, 3, 2, 3);

	for (int i = 0; i < vm2.length; i++) {
	    if (vm2a[i] != vm2b[i]) {
		throw new Exception();
	    }
	}
    }


    public static void main(String argv[]) throws Exception {

	double[][] matrix1 = {{1.0, 2.0, 3.0},
			      {4.0, 5.0, 6.0}};


	double[] matrix1RO = MatrixOps.flatten(matrix1, false);

	double [] roExpected =  {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
	double[] coExpected = {1.0, 4.0, 2.0, 5.0, 3.0, 6.0};

	System.out.println("Testing flatten");
	// System.out.println("row major order for matrix1:");
	for (int i = 0; i < coExpected.length; i++) {
	    if (matrix1RO[i] != roExpected[i]) {
		throw new Exception("flatten failed (row-major ordering)");
	    }
	    // System.out.println("    " + matrix1RO[i]);
	}

	// System.out.println("column major order for matrix1:");
	double[] matrix1CO = MatrixOps.flatten(matrix1, true);
	for (int i = 0; i < coExpected.length; i++) {
	    if (matrix1CO[i] != coExpected[i]) {
		throw new Exception("flatten failed (column-major ordering)");
	    }
	    // System.out.println("    " + matrix1CO[i]);
	}

	System.out.println("Testing unflatten");
	double[][] tmp = MatrixOps.unflatten(matrix1RO, 2, 3, false);
	if (tmp.length != matrix1.length ||
	    tmp[0].length != matrix1[0].length) {
	    throw new Exception("unflattened failed");
	}
	for (int i = 0; i < matrix1.length; i++) {
	    for (int j = 0; j < matrix1[0].length; j++) {
		if (Math.abs(matrix1[i][j] -tmp[i][j]) > 1.e-10) {
		    throw new Exception("unflatten failed");
		}
	    }
	}

	tmp = MatrixOps.unflatten(matrix1CO, 2, 3, true);
	if (tmp.length != matrix1.length ||
	    tmp[0].length != matrix1[0].length) {
	    throw new Exception("unflattened failed");
	}
	for (int i = 0; i < matrix1.length; i++) {
	    for (int j = 0; j < matrix1[0].length; j++) {
		if (Math.abs(matrix1[i][j] -tmp[i][j]) > 1.e-10) {
		    throw new Exception("unflatten failed");
		}
	    }
	}

	System.out.println("Testing addition");

	double[][] matrix1a = {{10.0, 20.0, 30.0},
			     {40.0, 50.0, 60.0}};

	double[][] added = MatrixOps.add(matrix1, matrix1a);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		if (Math.abs(added[i][j] - (matrix1[i][j] + matrix1a[i][j]))
		    > 1.e-10) {
		    throw new Exception("addition failed");
		}
	    }
	}
	double[] fadded1 = MatrixOps.flatten(added, true);
	double[] fadded2 = MatrixOps.flatten(added, false);

	double[] added1 = MatrixOps.add(MatrixOps.flatten(matrix1, true),
					MatrixOps.flatten(matrix1a, true));
	double[] added2 =  MatrixOps.add(MatrixOps.flatten(matrix1, false),
					MatrixOps.flatten(matrix1a, false));

	if (fadded1.length != added1.length)
	    throw new Exception("length");
	for (int i = 0; i < fadded1.length; i++) {
	    if (Math.abs(fadded1[i] - added1[i]) > 1.e-10)
		throw new Exception("addition failure");
	}
	if (fadded2.length != added2.length)
	    throw new Exception("length");
	for (int i = 0; i < fadded2.length; i++) {
	    if (Math.abs(fadded2[i] - added2[i]) > 1.e-10)
		throw new Exception("addition failure");
	}

	MatrixOps.add(added1, added1, added1);
	for (int i = 0; i < fadded1.length; i++) {
	    if (Math.abs(2.0*fadded1[i] - added1[i]) > 1.e-10)
		throw new Exception("addition failure");
	}

	MatrixOps.add(added2, added2, added2);
	for (int i = 0; i < fadded2.length; i++) {
	    if (Math.abs(2.0*fadded2[i] - added2[i]) > 1.e-10)
		throw new Exception("addition failure");
	}


	MatrixOps.add(added, added, added);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 3; j++) {
		if (Math.abs(added[i][j] - 2.0*(matrix1[i][j] + matrix1a[i][j]))
		    > 1.e-10) {
		    throw new Exception("addition failed");
		}
	    }
	}

	double[] added3 = new double[2*added1.length];
	double[] added4 = new double[2*added1.length];
	MatrixOps.add(2, 3, added3, 0, added3, 0, added1, 0,
		      0, 2, 0, 2, 0, 1);
	MatrixOps.add(2, 3, added3, 0, added3, 0, added1, 0,
		      1, 2, 1, 2, 0, 1);
	MatrixOps.add(2, 3, added3, 0, added3, 0, added3, 0,
		      1, 2, 1, 2, 1, 2);

	for (int i = 0; i < added1.length; i++) {
	    int i1 = 2*i;
	    int i2 = i1+1;
	    if (Math.abs(2*added3[i1] - added3[i2]) > 1.e-10) {
		throw new Exception("add failed");
	    }
	}


	System.out.println("Testing transpose");

	double[][] matrix1t = MatrixOps.transpose(matrix1);
	for (int i = 0; i < matrix1.length; i++) {
	    for (int j = 0; j < matrix1[0].length; j++) {
		if (matrix1t[j][i] != matrix1[i][j]) {
		    System.out.format("matrix1t[%d][%d] = %g, "
				      + "matrix1[%d]%d] = %g\n",
				      j, i, matrix1t[j][i],
				      i, j, matrix1[i][j]);
		    throw new Exception("transposed failed");
		}
	    }
	}


	double[][] matrix1ta = {
	    {1.0, 2.0, 3.0},
	    {4.0, 5.0, 6.0},
	    {7.0, 8.0, 9.0},
	    {10.0, 11.0, 12.0}
	};

	double[][] matrix1tb = MatrixOps.transpose(matrix1ta,
						   matrix1ta.length,
						   matrix1ta[0].length);

	for (int i = 0; i < matrix1ta.length; i++) {
	    for (int j = 0; j < matrix1ta[0].length; j++) {
		if (matrix1tb[j][i] != matrix1ta[i][j]) {
		    System.out.format("matrix1t[%d][%d] = %g, "
				      + "matrix1[%d]%d] = %g\n",
				      j, i, matrix1t[j][i],
				      i, j, matrix1[i][j]);
		    throw new Exception("transposed failed");
		}
	    }
	}

	double[] m1RO = MatrixOps.transpose(matrix1RO, 2, 3, false);
	for (int i = 0; i < roExpected.length; i++) {
	    if (coExpected[i] != m1RO[i]) {
		throw new Exception("transpose failed");
	    }
	}

	double[] m1CO = MatrixOps.transpose(matrix1CO, 2, 3, true);
	for (int i = 0; i < coExpected.length; i++) {
	    if (roExpected[i] != m1CO[i]) {
		throw new Exception("transpose failed");
	    }
	}


	double[][] tm1 = {{1.0, 2.0, 3.0},
			 {4.0, 5.0, 6.0},
			 {7.0, 8.0, 9.0}};

	double[][] tm2 = {{1.0, 2.0, 3.0},
			 {4.0, 5.0, 6.0},
			 {7.0, 8.0, 9.0}};
	MatrixOps.transpose(tm1, tm1);
	for (int i = 0; i < tm1.length; i++) {
	    for (int j = 0; j < tm1[0].length; j++) {
		if (tm2[j][i] != tm1[i][j]) {
		    System.out.format("tm1[%d][%d] = %g, "
				      + "tm2[%d]%d] = %g\n",
				      j, i, matrix1t[j][i],
				      i, j, matrix1[i][j]);
		    throw new Exception("transposed failed");
		}
	    }
	}

	double[] tm2RO = MatrixOps.flatten(tm2, false);
	double[] tm2CO = MatrixOps.flatten(tm2, true);
	MatrixOps.transpose(tm2RO, 3, 3, tm2RO, 3, 3, false);
	MatrixOps.transpose(tm2CO, 3, 3, tm2CO, 3, 3, true);
	tm2 = MatrixOps.unflatten(tm2RO, 3, 3, false);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (tm2[i][j] != tm1[i][j]) {
		    throw new Exception("transpose failed");
		}
	    }
	}

	tm2 = MatrixOps.unflatten(tm2CO, 3, 3, true);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 3; j++) {
		if (tm2[i][j] != tm1[i][j]) {
		    throw new Exception("transpose failed");
		}
	    }
	}
	

	double[] tm3 = {1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
			4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
			7.0, 70.0, 8.0, 80.0, 9.0, 90.0,
			7.5, 70.5, 8.5, 80.5, 9.5, 90.5};

	double[] tm3a = {1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
			4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
			7.0, 70.0, 8.0, 80.0, 9.0, 90.0};

	double[] tm3b = {1.0, 10.0, 4.0, 40.0, 7.0, 70.0, 7.5, 70.5,
			 2.0, 20.0, 5.0, 50.0, 8.0, 80.0, 8.5, 80.5,
			 3.0, 30.0, 6.0, 60.0, 9.0, 90.0, 9.5, 90.5};

	double[] tm3c = {1.0, 10.0, 4.0, 40.0, 7.0, 70.0,
			 2.0, 20.0, 5.0, 50.0, 8.0, 80.0,
			 3.0, 30.0, 6.0, 60.0, 9.0, 90.0};


	double[] expected3 = {1.0, 10.0, 4.0, 40.0, 7.0, 70.0, 7.5, 70.5,
			      2.0, 20.0, 5.0, 50.0, 8.0, 80.0, 8.5, 80.5,
			      3.0, 30.0, 6.0, 60.0, 9.0, 90.0, 9.5, 90.5};

	double[] expected3a = {1.0, 10.0, 4.0, 40.0, 7.0, 70.0,
			       2.0, 20.0, 5.0, 50.0, 8.0, 80.0,
			       3.0, 30.0, 6.0, 60.0, 9.0, 90.0};

	double[] expected3b = {1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
			       4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
			       7.0, 70.0, 8.0, 80.0, 9.0, 90.0,
			       7.5, 70.5, 8.5, 80.5, 9.5, 90.5};

	double[] expected3c = {1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
			       4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
			       7.0, 70.0, 8.0, 80.0, 9.0, 90.0};


	double[] tm4 =  new double[tm3.length];
	double[] tm4a =  new double[tm3a.length];
	MatrixOps.transpose(tm4, 3, 4, 0, tm3, 4, 3, 0, false,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4, 3, 4, 0, tm3, 4, 3, 0, false,
			    1, 2, 1, 2);
	for (int i = 0; i < tm4.length; i++) {
	    if (tm4[i] != expected3[i]) {
		throw new Exception("transpose failed");
	    }
	}

	MatrixOps.transpose(tm4a, 3, 3, 0, tm3a, 3, 3, 0, false,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm3a, 3, 3, 0, false,
			    1, 2, 1, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm4a, 3, 3, 0, false,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm4a, 3, 3, 0, false,
			    1, 2, 1, 2);

	for (int i = 0; i < tm4a.length; i++) {
	    if (tm4a[i] != tm3a[i]) {
		throw new Exception("transpose failed");
	    }
	}

	if (tm3b.length != tm3.length) {
	    throw new Exception("tm3b has the wrong length: "
				+ tm3b.length + " != " + tm3.length);
	}
	MatrixOps.transpose(tm4, 3, 4, 0, tm3b, 4, 3, 0, true,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4, 3, 4, 0, tm3b, 4, 3, 0, true,
			    1, 2, 1, 2);
	System.out.print("tm3b =");
	for (int i = 0; i < tm3b.length; i++) {
	    System.out.print(" " + tm3b[i]);
	}
	System.out.println();
	System.out.print("tm4 =");
	for (int i = 0; i < tm4.length; i++) {
	    System.out.print(" " + tm4[i]);
	}
	System.out.println();
	System.out.print("expected3b =");
	for (int i = 0; i < expected3b.length; i++) {
	    System.out.print(" " + expected3b[i]);
	}
	System.out.println();

	for (int i = 0; i < tm4.length; i++) {
	    if (tm4[i] != expected3b[i]) {
		throw new Exception("transpose failed");
	    }
	}

	MatrixOps.transpose(tm4a, 3, 3, 0, tm3c, 3, 3, 0, false,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm3c, 3, 3, 0, false,
			    1, 2, 1, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm4a, 3, 3, 0, false,
			    0, 2, 0, 2);
	MatrixOps.transpose(tm4a, 3, 3, 0, tm4a, 3, 3, 0, false,
			    1, 2, 1, 2);

	for (int i = 0; i < tm4a.length; i++) {
	    if (tm4a[i] != tm3c[i]) {
		throw new Exception("transpose failed");
	    }
	}



	System.out.println("Testing multipication");


	double[][] matrix2 = {{10.0, 20.0, 30.0, 40.0},
			      {50.0, 60.0, 70.0, 80.0},
			      {90.0, 100.0, 110.0, 120.0}};


	double[] matrix2RO = MatrixOps.flatten(matrix2, false);
	double[] matrix2CO = MatrixOps.flatten(matrix2, true);



	double[][] matrix3 = new double[2][4];

	double[][] expecting = {
	    {10.0+2*50+3*90, 20.0+2*60+3*100,
	     30.0+2*70+3*110, 40.0+2*80+3*120},
	    {40.0+5*50+6*90, 80.0+5*60+6*100,
	     120.0+5*70+6*110, 160.0+5*80+6*120}};

	System.out.println("Testing multiple - [][] case");
	MatrixOps.multiply(matrix3, matrix1, matrix2);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 4; j++) {
		if (Math.abs(matrix3[i][j] - expecting[i][j]) > 1.e-10) {
		    throw new Exception("matrix multiplication failed");
		}
	    }
	}

	System.out.println("Testing multiple- [] cases");

	double[] matrix3RO = MatrixOps.multiply(matrix1RO, 2, 3,
						matrix2RO, 3, 4,
						false);
	    
	double[] matrix3CO = MatrixOps.multiply(matrix1CO, 2, 3,
						matrix2CO, 3, 4,
						true);
	
	matrix3 = MatrixOps.unflatten(matrix3RO, 2, 4, false);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 4; j++) {
		if (Math.abs(matrix3[i][j] - expecting[i][j]) > 1.e-10) {
		    throw new Exception("matrix multiplication failed");
		}
	    }
	}

	matrix3 = MatrixOps.unflatten(matrix3CO, 2, 4, true);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 4; j++) {
		if (Math.abs(matrix3[i][j] - expecting[i][j]) > 1.e-10) {
		    throw new Exception("matrix multiplication failed");
		}
	    }
	}

	System.out.println("Testing vlen cases");
	
	double[] matrix1ROv3 = new double[matrix1RO.length*3];
	double[] matrix2ROv3 = new double[matrix2RO.length*3];
	double[] matrix3ROv3 = new double[matrix3RO.length*3];

	for (int i = 0; i < matrix1RO.length; i++) {
	    int ii = i*3;
	    matrix1ROv3[ii] = matrix1RO[i];
	    matrix1ROv3[ii+1] = 2*matrix1RO[i];
	    matrix1ROv3[ii+2] = 4*matrix1RO[i];
	}

	for (int i = 0; i < matrix2RO.length; i++) {
	    int ii = i*3;
	    matrix2ROv3[ii] = matrix2RO[i];
	    matrix2ROv3[ii+1] = 2*matrix2RO[i];
	    matrix2ROv3[ii+2] = 4*matrix2RO[i];
	}

	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(matrix3ROv3, 2, 4, 0,
			       matrix1ROv3,  2, 3, 0,
			       matrix2ROv3, 3, 4, 0,
			       false,
			       i, 3, i, 3, i, 3);
	}

	for (int i = 0; i < matrix3RO.length; i++) {
	    int ii = i * 3;
	    if (Math.abs(matrix3RO[i] - matrix3ROv3[ii]) > 1.e-10) {
		System.out.format("expected [%d] -> %g, saw [%d] -> %g\n",
				  i, matrix3RO[i], ii, matrix3ROv3[ii]);
		throw new Exception("matrix multipy failed");
	    }
	    if (Math.abs(matrix3RO[i]*4 - matrix3ROv3[ii+1]) > 1.e-10) {
		System.out.format("expected [%d] -> %g, saw [%d] -> %g\n",
				  i, 4*matrix3RO[i], ii+1, matrix3ROv3[ii+1]);
		throw new Exception("matrix multipy failed");
	    }
	    if (Math.abs(matrix3RO[i]*16 - matrix3ROv3[ii+2]) > 1.e-10) {
		throw new Exception("matrix multipy failed");
	    }
	}

	double[] matrixS = new double[matrix3ROv3.length];
	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(2, 4, matrixS, 0, 10.0, matrix3ROv3, 0,
			       i, 3, i, 3);
	}
	for (int i = 0; i < matrix3ROv3.length; i++) {
	    if (Math.abs(matrixS[i] - 10.0*matrix3ROv3[i]) > 1.e-10) {
		throw new Exception("multiplying a matrix by a scalar failed");
	    }
	}

	double[] matrix1COv3 = new double[matrix1CO.length*3];
	double[] matrix2COv3 = new double[matrix2CO.length*3];
	double[] matrix3COv3 = new double[matrix3CO.length*3];

	for (int i = 0; i < matrix1CO.length; i++) {
	    int ii = i*3;
	    matrix1COv3[ii] = matrix1CO[i];
	    matrix1COv3[ii+1] = 2*matrix1CO[i];
	    matrix1COv3[ii+2] = 4*matrix1CO[i];
	}

	for (int i = 0; i < matrix2CO.length; i++) {
	    int ii = i*3;
	    matrix2COv3[ii] = matrix2CO[i];
	    matrix2COv3[ii+1] = 2*matrix2CO[i];
	    matrix2COv3[ii+2] = 4*matrix2CO[i];
	}

	for (int i = 0; i < 3; i++) {
	    MatrixOps.multiply(matrix3COv3, 2, 4, 0,
			       matrix1COv3,  2, 3, 0,
			       matrix2COv3, 3, 4, 0,
			       true,
			       i, 3, i, 3, i, 3);
	}

	for (int i = 0; i < matrix3CO.length; i++) {
	    int ii = i * 3;
	    if (Math.abs(matrix3CO[i] - matrix3COv3[ii]) > 1.e-10) {
		System.out.format("expected [%d] -> %g, saw [%d] -> %g\n",
				  i, matrix3CO[i], ii, matrix3COv3[ii]);
		throw new Exception();
	    }
	    if (Math.abs(matrix3CO[i]*4 - matrix3COv3[ii+1]) > 1.e-10) {
		System.out.format("expected [%d] -> %g, saw [%d] -> %g\n",
				  i, 4*matrix3CO[i], ii+1, matrix3COv3[ii+1]);
		throw new Exception();
	    }
	    if (Math.abs(matrix3CO[i]*16 - matrix3COv3[ii+2]) > 1.e-10) {
		throw new Exception();
	    }
	}

	System.out.println("Testing reflectRows");
	double[][] matrix4 = MatrixOps.reflectRows(matrix2);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}
	System.out.format("dim(matrix2) = (%d, %d)\n", matrix2.length,
			  matrix2[0].length);
	double[][] matrix4a = MatrixOps.reflectRows(matrix2, 3, 4);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4a[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}

	double[][] matrix4b = new double[3][4];
	double[][] result = MatrixOps.reflectRows(matrix4b, matrix2);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4b[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}

	double[] fmatrix4c = MatrixOps.reflectRows(matrix2RO, 3, 4, false);
	for (int i = 0; i < 12; i++) {
	    System.out.print(" " + matrix2RO[i]);
	}
	System.out.println();
	for (int i = 0; i < 12; i++) {
	    System.out.print(" " + fmatrix4c[i]);
	}
	System.out.println();
	double[][] matrix4c = MatrixOps.unflatten(fmatrix4c, 3, 4, false);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		System.out.print(" " + matrix4c[i][j]);
	    }
	    System.out.println();
	}
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4c[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}

	double[] tm5 = {1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
			4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
			7.0, 70.0, 8.0, 80.0, 9.0, 90.0,
			7.5, 70.5, 8.5, 80.5, 9.5, 90.5};

	double[] tm5a = {1.0, 10.0, 4.0, 40.0, 7.0, 70.0, 7.5, 70.5,
			 2.0, 20.0, 5.0, 50.0, 8.0, 80.0, 8.5, 80.5,
			 3.0, 30.0, 6.0, 60.0, 9.0, 90.0, 9.5, 90.5};

	double[] expected5 = {3.0, 30.0, 2.0, 20.0, 1.0, 10.0,
			      6.0, 60.0, 5.0, 50.0, 4.0, 40.0,
			      9.0, 90.0, 8.0, 80.0, 7.0, 70.0,
			      9.5, 90.5, 8.5, 80.5, 7.5, 70.5};

	double[] expected5a = {3.0, 30.0, 6.0, 60.0, 9.0, 90.0, 9.5, 90.5,
			       2.0, 20.0, 5.0, 50.0, 8.0, 80.0, 8.5, 80.5,
			       1.0, 10.0, 4.0, 40.0, 7.0, 70.0, 7.5, 70.5};

	double tm6[] = new double[24];

	MatrixOps.reflectRows(tm6, 0, tm5, 0, 4, 3, false, 0, 2, 0, 2);
	MatrixOps.reflectRows(tm6, 0, tm5, 0, 4, 3, false, 1, 2, 1, 2);

	System.out.println("reflect-vector case");
	System.out.print("tm5 =");
	for (int i = 0; i < tm5.length; i++) {
	    System.out.print(" " + tm5[i]);
	}
	System.out.println();
	System.out.print("tm6 =");
	for (int i = 0; i < tm6.length; i++) {
	    System.out.print(" " + tm6[i]);
	}
	System.out.println();
	System.out.println("expected5 =");
	for (int i = 0; i < expected5.length; i++) {
	    System.out.print(" " + expected5[i]);
	}
	System.out.println();

	for (int i = 0; i < tm5.length; i++) {
	    if (tm6[i] != expected5[i]) {
		throw new Exception("reflectRows failed");
	    }
	}
	System.out.print("tm5a =");
	for (int i = 0; i < tm5.length; i++) {
	    System.out.print(" " + tm5[i]);
	}
	System.out.println();
	System.out.println();
	System.out.print("tm6 =");
	for (int i = 0; i < tm6.length; i++) {
	    System.out.print(" " + tm6[i]);
	}
	System.out.println();
	System.out.print("expected5a =");
	for (int i = 0; i < expected5.length; i++) {
	    System.out.print(" " + expected5[i]);
	}
	System.out.println();


	MatrixOps.reflectRows(tm6, 0, tm5a, 0, 4, 3, true, 0, 2, 0, 2);
	MatrixOps.reflectRows(tm6, 0, tm5a, 0, 4, 3, true, 1, 2, 1, 2);
	for (int i = 0; i < tm5a.length; i++) {
	    if (tm6[i] != expected5a[i]) {
		throw new Exception("reflectRows failed, i = " + i
				    + ", " + tm6[i] + " != " + expected5a[i]);
	    }
	}

	// transpose, which makes rows columns, then reflect the columns
	// and transpose back.
	matrix4 = MatrixOps
	    .transpose(MatrixOps
		       .reflectColumns(MatrixOps
				       .transpose(matrix2)));
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}
	matrix4b = new double[4][3];
	MatrixOps.reflectColumns(matrix4b, MatrixOps.transpose(matrix2));
	matrix4 = MatrixOps
	    .transpose(matrix4b);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}
	double[] m1 = MatrixOps.transpose(matrix2RO, 3, 4, false);
	double[] m2 = MatrixOps.reflectColumns(m1, 4, 3, false);
	double[] m3 = MatrixOps.transpose(m2, 4, 3, false);
	matrix4 = MatrixOps.unflatten(m3, 3, 4, false);
	for (int i = 0; i < 3; i++) {
	    for (int j = 0; j < 4; j++) {
		if (matrix4[i][j] != matrix2[i][3-j]) {
		    throw new Exception();
		}
	    }
	}

	double[] expected5rc = {
	    7.5, 70.5, 8.5, 80.5, 9.5, 90.5,
	    7.0, 70.0, 8.0, 80.0, 9.0, 90.0,
	    4.0, 40.0, 5.0, 50.0, 6.0, 60.0,
	    1.0, 10.0, 2.0, 20.0, 3.0, 30.0,
	};
	m1 = new double[24];
	MatrixOps.reflectColumns(m1, 0, tm5, 0, 4, 3, false, 0, 2, 0, 2);
	MatrixOps.reflectColumns(m1, 0, tm5, 0, 4, 3, false, 1, 2, 1, 2);
	for (int i = 0; i < tm5.length; i++) {
	    if (m1[i] != expected5rc[i]) {
		throw new Exception();
	    }
	}

	float[] tm5f = {1.0F, 10.0F, 2.0F, 20.0F, 3.0F, 30.0F,
			4.0F, 40.0F, 5.0F, 50.0F, 6.0F, 60.0F,
			7.0F, 70.0F, 8.0F, 80.0F, 9.0F, 90.0F,
			7.5F, 70.5F, 8.5F, 80.5F, 9.5F, 90.5F};

	float[] expected5rcf = {
	    7.5F, 70.5F, 8.5F, 80.5F, 9.5F, 90.5F,
	    7.0F, 70.0F, 8.0F, 80.0F, 9.0F, 90.0F,
	    4.0F, 40.0F, 5.0F, 50.0F, 6.0F, 60.0F,
	    1.0F, 10.0F, 2.0F, 20.0F, 3.0F, 30.0F,
	};
	float m1f[] = new float[24];
	MatrixOps.reflectColumns(m1f, 0, tm5f, 0, 4, 3, false, 0, 2, 0, 2);
	MatrixOps.reflectColumns(m1f, 0, tm5f, 0, 4, 3, false, 1, 2, 1, 2);
	for (int i = 0; i < tm5f.length; i++) {
	    if (m1f[i] != expected5rcf[i]) {
		throw new Exception();
	    }
	}


	double rm1[][] = {{1.0, 2.0, 3.0, 4.0, 5.0},
			{6.0, 7.0, 8.0, 9.0, 10.0},
			{11.0, 12.0, 13.0, 14.0, 15.0},
			{16.0, 17.0, 18.0, 19.0, 20.0},
			{21.0, 22.0, 23.0, 24.0, 25.0}};

	System.out.println("rm1:");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + rm1[i][j]);
	    }
	    System.out.println();
	}

	double rm2[][] = {{1.0, 2.0, 3.0, 4.0, 5.0, 5.5},
			{6.0, 7.0, 8.0, 9.0, 10.0, 10.5},
			{11.0, 12.0, 13.0, 14.0, 15.0, 15.5},
			{16.0, 17.0, 18.0, 19.0, 20.0, 20.5}};
	System.out.println("rm2:");
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		System.out.print(" " + rm2[i][j]);
	    }
	    System.out.println();
	}

	double[][] test1a;
	double[][] test2a;
	double[][] test1b = MatrixOps.reflectRows(rm1);
	double[][] test2b = MatrixOps.reflectRows(rm2);

	// Check reflectRows for flattened matrices, assuming it works
	// for unflattened ones.
	test1a = MatrixOps
	    .unflatten(MatrixOps.reflectRows(MatrixOps.flatten(rm1, true),
					     rm1.length, rm1[0].length, true),
		       rm1.length, rm1[0].length, true);
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test1a = MatrixOps
	    .unflatten(MatrixOps.reflectRows(MatrixOps.flatten(rm1, false),
					     rm1.length, rm1[0].length, false),
		       rm1.length, rm1[0].length, false);
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test2a = MatrixOps
	    .unflatten(MatrixOps.reflectRows(MatrixOps.flatten(rm2, true),
					     rm2.length, rm2[0].length, true),
		       rm2.length, rm2[0].length, true);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test2a = MatrixOps
	    .unflatten(MatrixOps.reflectRows(MatrixOps.flatten(rm2, false),
					     rm2.length, rm2[0].length, false),
		       rm2.length, rm2[0].length, false);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	// Check reflectColumns for flattened matrices, assuming it works
	// for unflattened ones.
	test1a = MatrixOps
	    .unflatten(MatrixOps
		       .reflectColumns(MatrixOps.flatten(rm1, true),
				       rm1.length, rm1[0].length, true),
		       rm1.length, rm1[0].length, true);
	test1b = MatrixOps.reflectColumns(rm1);
	test2b = MatrixOps.reflectColumns(rm2);
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test1a = MatrixOps
	    .unflatten(MatrixOps
		       .reflectColumns(MatrixOps.flatten(rm1, false),
				       rm1.length, rm1[0].length, false),
		       rm1.length, rm1[0].length, false);
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test2a = MatrixOps
	    .unflatten(MatrixOps
		       .reflectColumns(MatrixOps.flatten(rm2, true),
				       rm2.length, rm2[0].length, true),
		       rm2.length, rm2[0].length, true);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test2a = MatrixOps
	    .unflatten(MatrixOps
		       .reflectColumns(MatrixOps.flatten(rm2, false),
				       rm2.length, rm2[0].length, false),
		       rm2.length, rm2[0].length, false);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	// Test reflect(...) methods
	// use the fact that reflect() is equivalent to reflecting columns
	// and then reflecting rows - a method is provided because it is
	// more efficient to do it directly.
	test1a = MatrixOps.reflect(rm1);
	test1b = MatrixOps.reflectRows(MatrixOps.reflectColumns(rm1));
	System.out.println("test1a:");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + test1a[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("test1b:");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + test1b[i][j]);
	    }
	    System.out.println();
	}

	test2a = MatrixOps.reflect(rm2);
	test2b = MatrixOps.reflectRows(MatrixOps.reflectColumns(rm2));

	System.out.println("test2a:");
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		System.out.print(" " + test2a[i][j]);
	    }
	    System.out.println();
	}
	System.out.println("test2b:");
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		System.out.print(" " + test2b[i][j]);
	    }
	    System.out.println();
	}

	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	double[][] test2c = MatrixOps.reflect(rm2, 4, 5);
	double[][] test2d =
	    MatrixOps.reflectRows(MatrixOps.reflectColumns(rm1, 4, 5), 4, 5);
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 5; j++) {
		if (test2c[i][j] != test2d[i][j]) {
		    throw new Exception();
		}
	    }
	}

	System.out.println("rm1:");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + rm1[i][j]);
	    }
	    System.out.println();
	}
	test1a = MatrixOps
	    .unflatten(MatrixOps
		       .reflect(MatrixOps.flatten(rm1, false),
				rm1.length, rm1[0].length, false),
		       rm1.length, rm1[0].length, false);
	System.out.println("test1a (created using row major order):");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + test1a[i][j]);
	    }
	    System.out.println();
	}
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	// double check flattening:
	test1a = MatrixOps.unflatten(MatrixOps.flatten(rm1, true),
				     rm1.length, rm1[0].length, true);
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != rm1[i][j]) {
		    throw new Exception();
		}
	    }
	}

	test1a = MatrixOps
	    .unflatten(MatrixOps
		       .reflect(MatrixOps.flatten(rm1, true),
				rm1.length, rm1[0].length, true),
		       rm1.length, rm1[0].length, true);
	System.out.print("rm1 (CMO):");
	for (double val: MatrixOps.flatten(rm1, true)) {
	    System.out.print(" " + val);
	}
	System.out.println();

	System.out.println("test1a (created using column major order):");
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		System.out.print(" " + test1a[i][j]);
	    }
	    System.out.println();
	}
	for (int i = 0; i < rm1.length; i++) {
	    for (int j = 0; j < rm1[0].length; j++) {
		if (test1a[i][j] != test1b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	test2a = MatrixOps
	    .unflatten(MatrixOps
		       .reflect(MatrixOps.flatten(rm2, false),
				rm2.length, rm2[0].length, false),
		       rm2.length, rm2[0].length, false);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}
	test2a = MatrixOps
	    .unflatten(MatrixOps
		       .reflect(MatrixOps.flatten(rm2, true),
				rm2.length, rm2[0].length, true),
		       rm2.length, rm2[0].length, true);
	for (int i = 0; i < rm2.length; i++) {
	    for (int j = 0; j < rm2[0].length; j++) {
		if (test2a[i][j] != test2b[i][j]) {
		    throw new Exception();
		}
	    }
	}

	vectorReflectTest(rm1, rm2, true);
	vectorReflectTest(rm1, rm2, false);

	System.out.println("... OK");
	System.exit(0);
    }
}
