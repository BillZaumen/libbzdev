import org.bzdev.geom.*;
import org.bzdev.math.Adder;

public class SurfaceIntTest {

    // initialize in main for convenience in case anything goes wrong
    static SurfaceIntegral siA;
    static SurfaceIntegral siV;

    static SurfaceIntegral.Batched bA;
    static SurfaceIntegral.Batched bV;

    static void TriangleCase() throws Exception {
	System.out.println("Triangle test for area");

	Surface3D surface1 = new Surface3D.Double();
	double[] coords = new double[48];
	coords[0] = 100.0;
	coords[1] = 100.0*2;
	coords[2] = 50.0;
	coords[3] = 400.0;
	coords[4] = 400.0*2;
	coords[5] = 50.0;
	coords[6] = 400.0;
	coords[7] = 100.0*2;
	coords[8] = 50.0;
	surface1.addPlanarTriangle(coords);

	double[] utangent = new double[3];
	double[] vtangent = new double[3];
	Surface3D.uTangent(utangent, SurfaceIterator.PLANAR_TRIANGLE,
			   coords, 0.0, 0.0);
	Surface3D.vTangent(vtangent, SurfaceIterator.PLANAR_TRIANGLE,
			   coords, 0.0, 0.0);
	System.out.format("u tangent (planar T) at (0,0): (%g, %g, %g)\n",
			  utangent[0], utangent[1], utangent[2]);
	System.out.format("v tangent (planar T) at (0,0): (%g, %g, %g)\n",
			  vtangent[0], vtangent[1], vtangent[2]);

	Surface3D surface2= new Surface3D.Double();
	double[] pcoords = new double[12];
	pcoords[0] = 100.0;
	pcoords[1] = 100.0*2;
	pcoords[2] = 50.0;
	pcoords[3] = 200.0;
	pcoords[4] = 200.0*2;
	pcoords[5] = 50.0;
	pcoords[6] = 300.0;
	pcoords[7] = 300.0*2;
	pcoords[8] = 50.0;
	pcoords[9] = 400.0;
	pcoords[10] = 400.0*2;
	pcoords[11] = 50.0;
	Surface3D.setupU0ForTriangle(pcoords, coords, false);
	pcoords[4] = 100.0*2;
	pcoords[7] = 100.0*2;
	pcoords[10] = 100.0*2;
	Surface3D.setupV0ForTriangle(pcoords, coords, false);
	pcoords[0] = 400.0;
	pcoords[1] = 100.0*2;
	pcoords[3] = 400.0;
	pcoords[4] = 200.0*2;
	pcoords[6] = 400.0;
	pcoords[7] = 300.0*2;
	pcoords[9] = 400.0;
	pcoords[10] = 400.0*2;
	Surface3D.setupW0ForTriangle(pcoords, coords, false);
	Surface3D.setupPlanarCP111ForTriangle(coords);
	surface2.addCubicTriangle(coords);

	Surface3D.uTangent(utangent, SurfaceIterator.CUBIC_TRIANGLE,
			   coords, 0.0, 0.0);
	Surface3D.vTangent(vtangent, SurfaceIterator.CUBIC_TRIANGLE,
			   coords, 0.0, 0.0);
	System.out.format("u tangent (cubic T) at (0,0): (%g, %g, %g)\n",
			  utangent[0], utangent[1], utangent[2]);
	System.out.format("v tangent (cubic T) at (0,0): (%g, %g, %g)\n",
			  vtangent[0], vtangent[1], vtangent[2]);


	System.out.println("surface1.area() = " + surface1.area());
	System.out.println("surface2.area() = " + surface2.area());
	
	System.out.println("surface1.area() by siA = "
			   + siA.integrate(surface1));
	System.out.println("surface2.area() by siA= "
			   + siA.integrate(surface2));
	System.out.println("expected area = " + (300.0*600.0/2));

	System.out.println("vector field, surface1: "
			   + siV.integrate(surface1));
	System.out.println("vector field, surface2: "
			   + siV.integrate(surface2));

	System.out.println("surface1 area repeated:");
	for (double area: bA.integrate(surface1)) {
	    System.out.println("    " + area);
	}

	System.out.println("surface1 vector field repeated:");
	for (double v: bV.integrate(surface1)) {
	    System.out.println("    " + v);
	}

	System.out.println("sequential versus parallel:");
	System.out.println("siA surface1: "
			   + siA.integrate(surface1, false)
			   + " " + siA.integrate(surface1, true));
	System.out.println("siV surface1: "
			   + siV.integrate(surface1, false)
			   + " " + siV.integrate(surface1, true));
	System.out.println("siA surface2: "
			   + siA.integrate(surface2, false)
			   + " " + siA.integrate(surface2, true));
	System.out.println("siV surface2: "
			   + siV.integrate(surface2, false)
			   + " " + siV.integrate(surface2, true));
	double[] as1 = bA.integrate(surface1, false);
	double[] as2 = bA.integrate(surface1, true);
	if (as1.length != as2.length) throw new Exception();
	System.out.println("bA for surface1:");
	for (int i = 0; i < as1.length; i++) {
	    System.out.println("    " + as1[i] + " " + as2[i]);
	}
	double[] vs1 = bV.integrate(surface1, false);
	double[] vs2 = bV.integrate(surface1, true);
	if (vs1.length != vs2.length) throw new Exception();
	System.out.println("bV for surface1:");
	for (int i = 0; i < vs1.length; i++) {
	    System.out.println("    " + vs1[i] + " " + vs2[i]);
	}
    }

    static void BGCycloid() throws Exception {
	// coped from BGCycliodTest

	System.out.println("BGCycliod test");
	int N = 21;

	Point3D[][] array1 = new Point3D[N][N];
	Point3D[][] array2 = new Point3D[N-1][N-1];

	double r = 20.0;
	
	for (int i = 1; i < N-1; i++) {
	   for (int j = 1; j < N-1; j++) {

	       double thetaX = 2 * Math.PI * (i - 1) / (N-3);
	       double thetaY = 2 * Math.PI * (j - 1) / (N-3);

	       double x = r * (thetaX - Math.sin(thetaX));
	       double y = r * (thetaY - Math.sin(thetaY));
	       if (Math.abs(x) < 1.e-10) x = 0;
	       if (Math.abs(y) < 1.e-10) y = 0;
	       double z = r * (1 - Math.cos(thetaX)) * (1 - Math.cos(thetaY))
		   + 10.0;
	       array1[i][j] = new Point3D.Double(x, y, z);
	       array2[i-1][j-1] = new Point3D.Double(x, y, 0.0);
	   }
	}
	for (int i = 1 ; i < N-1 ; i++) {
	    array1[0][i] = new Point3D.Double(array1[1][i].getX(),
					     array1[1][i].getY(),
					     0.0);
	    array1[i][0] = new Point3D.Double(array1[i][1].getX(),
					     array1[i][1].getY(),
					     0.0);
	    array1[N-1][i] = new Point3D.Double(array1[N-2][i].getX(),
					     array1[N-2][i].getY(),
					     0.0);
	    array1[i][N-1] = new Point3D.Double(array1[i][N-2].getX(),
					     array1[i][N-2].getY(),
					     0.0);
	}

	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);

	for (int i = 1; i < N-1; i++) {
	    grid1.setRegion(0, i, 1);
	    grid1.setRegion(N-1, i, 1);
	    grid1.setRegion(i, 0, 1);
	    grid1.setRegion(i, N-1, 1);
	}

	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	Path3D boundary = surface.getBoundary();

	double a = surface.area();
	double v = surface.volume();
	
	System.out.println("a = " + a + ", v = " + v);

	System.out.println("area via surface integral = "
			   + siA.integrate(surface));


	System.out.println("volume via surface integral = "
			   + (siV.integrate(surface)/3));

	System.out.println("surface area repeated:");
	for (double area: bA.integrate(surface)) {
	    System.out.println("    " + area);
	}

	System.out.println("surface vector field repeated:");
	for (double vv: bV.integrate(surface)) {
	    System.out.println("    " + (vv/3));
	}

	System.out.println("sequential versus parallel:");
	System.out.println("siA surface: "
			   + siA.integrate(surface, false)
			   + " " + siA.integrate(surface, true));
	System.out.println("siV surface: "
			   + siV.integrate(surface, false)
			   + " " + siV.integrate(surface, true));
	double[] as1 = bA.integrate(surface, false);
	double[] as2 = bA.integrate(surface, true);
	if (as1.length != as2.length) throw new Exception();
	System.out.println("bA for surface:");
	for (int i = 0; i < as1.length; i++) {
	    System.out.println("    " + as1[i] + " " + as2[i]);
	}
	double[] vs1 = bV.integrate(surface, false);
	double[] vs2 = bV.integrate(surface, true);
	if (vs1.length != vs2.length) throw new Exception();
	System.out.println("bV for surface:");
	for (int i = 0; i < vs1.length; i++) {
	    System.out.println("    " + vs1[i] + " " + vs2[i]);
	}
    }

    public static void ExtensionWithCap() throws Exception  {
	int N = 4;
	int M = 2;

	Point3D[][] array = {
	    { new Point3D.Double(10.0, 0.0, 10.0),
	      new Point3D.Double(5.0, 0.0, 10.0)},
	    { new Point3D.Double(0.0, 10.0, 10.0),
	      new Point3D.Double(0.0, 5.0, 10.0)},
	    { new Point3D.Double(-10.0, 0.0, 10.0),
	      new Point3D.Double(-5.0, 0.0, 10.0)},
	    { new Point3D.Double(0.0, -10.0, 10.0),
	      new Point3D.Double(0.0, -5.0, 10.0)}
	};


	BezierGrid grid1 = new BezierGrid(array, true, false);

	Surface3D surface = new Surface3D.Double();
	grid1.reverseOrientation(true);
	surface.append(grid1);
	BezierCap topCap = new BezierCap(grid1.getBoundary(0,0),
					 3.0, true);
	
	surface.append(topCap);

	int nbase = 4;
	double rbase = 1.0;
	double zbase = 0.0;

	BezierGrid extension =
	    grid1.createExtensionGrid((index, point, type, ends) -> {
		    double tt = (1.0*index)/(nbase-1);
		    double t = tt*tt;
		    double z = point.getZ()*(1-t) - zbase*tt;
		    double startR = Math.sqrt(point.getX()*point.getX()
					      + point.getY()*point.getY());
		    double rfactor = startR*(1-t) + rbase*t;
		    return new Point3D.Double(rfactor*point.getX()/startR,
					      rfactor*point.getY()/startR,
					      z);
		}, null, nbase, 0, 1);


	surface.append(extension);
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}


	Path3D boundary = surface.getBoundary();


	BezierCap cap = new BezierCap(boundary, 0.0, true);

	surface.append(cap);

	if (!surface.isWellFormed(System.out)) {
	    System.out.println("surface not well formed");
	    System.exit(1);
	}

	System.out.println();

	System.out.println("extension with cap: area = "
			   + surface.area()
			   + ", volume = "
			   + surface.volume());

	/*
	Surface3D.printit = true;
	Rectangle3D bb = surface.getBounds();
	Point3D refPoint = new Point3D.Double(bb.getCenterX(),
					      bb.getCenterY(),
					      bb.getCenterZ());

	Adder adder = new Adder.Kahan();
	System.out.println("Surface3D area without refPoint");
	SurfaceOps.addVolumeToAdder(adder, surface.getSurfaceIterator(null),
				    null);
	System.out.println("Surface3D area with refPoint");
	SurfaceOps.addVolumeToAdder(adder, surface.getSurfaceIterator(null),
				    refPoint);
	*/
	System.out.println("area via surface integral = "
			   + siA.integrate(surface));

	System.out.println("volume via surface integral = "
			   + (siV.integrate(surface)/3));

	System.out.println("surface area repeated:");
	for (double area: bA.integrate(surface)) {
	    System.out.println("    " + area);
	}

	System.out.println("surface vector field repeated:");
	for (double v: bV.integrate(surface)) {
	    System.out.println("    " + (v/3));
	}


	System.out.println("sequential versus parallel:");
	System.out.println("siA surface: "
			   + siA.integrate(surface, false)
			   + " " + siA.integrate(surface, true));
	System.out.println("siV surface: "
			   + siV.integrate(surface, false)
			   + " " + siV.integrate(surface, true));
	double[] as1 = bA.integrate(surface, false);
	double[] as2 = bA.integrate(surface, true);
	if (as1.length != as2.length) throw new Exception();
	System.out.println("bA for surface:");
	for (int i = 0; i < as1.length; i++) {
	    System.out.println("    " + as1[i] + " " + as2[i]);
	}
	double[] vs1 = bV.integrate(surface, false);
	double[] vs2 = bV.integrate(surface, true);
	if (vs1.length != vs2.length) throw new Exception();
	System.out.println("bV for surface:");
	for (int i = 0; i < vs1.length; i++) {
	    System.out.println("    " + vs1[i] + " " + vs2[i]);
	}
	/*
	final double rpx = refPoint.getX();
	final double rpy = refPoint.getY();
	final double rpz = refPoint.getZ();
	SurfaceIntegral siVa =
	    new SurfaceIntegral(0, (x,y,z) -> {return x -rpx;},
				(x,y,z) -> {return y - rpy;},
				(x,y,z) -> {return z - rpz;});
	System.out.println("volume via surface integral with refpoint = "
			   + (siVa.integrate(surface)/3));
	*/
    }

    static void RectTest() throws Exception {
	Rectangle3D r3d = new Rectangle3D.Double(1.0, 2.0, 3.0,
						 10.0, 20.0, 30.0);
	System.out.println("RectTest");
	System.out.println("area = " + siA.integrate(r3d)
			   + ", expecting "
			   + (2*30.0*10.0 + 2*20.0*10.0 + 2*20.0*30.0));
	System.out.println("volume = " + (siV.integrate(r3d)/3)
			   + ", expecting "
			   + (10.0*20.0*30.0));

	Surface3D surface = new Surface3D.Double(r3d);
	System.out.println("As Surface3D, area = " + surface.area()
			   + ", volume = " + surface.volume());
	System.out.println("integrating over Surface3D, volume = "
			   + (siV.integrate(surface)/3));

	System.out.println("surface area repeated:");
	for (double area: bA.integrate(surface)) {
	    System.out.println("    " + area);
	}

	System.out.println("surface vector field repeated:");
	for (double v: bV.integrate(surface)) {
	    System.out.println("    " + (v/3));
	}
	System.out.println("sequential versus parallel:");
	System.out.println("siA surface: "
			   + siA.integrate(surface, false)
			   + " " + siA.integrate(surface, true));
	System.out.println("siV surface: "
			   + siV.integrate(surface, false)
			   + " " + siV.integrate(surface, true));
	double[] as1 = bA.integrate(surface, false);
	double[] as2 = bA.integrate(surface, true);
	if (as1.length != as2.length) throw new Exception();
	System.out.println("bA for surface:");
	for (int i = 0; i < as1.length; i++) {
	    System.out.println("    " + as1[i] + " " + as2[i]);
	}
	double[] vs1 = bV.integrate(surface, false);
	double[] vs2 = bV.integrate(surface, true);
	if (vs1.length != vs2.length) throw new Exception();
	System.out.println("bV for surface:");
	for (int i = 0; i < vs1.length; i++) {
	    System.out.println("    " + vs1[i] + " " + vs2[i]);
	}

    }

    public static void main(String argv[]) throws Exception {

	siA = new SurfaceIntegral(0, (x,y,z) -> {return 1.0;});

	siV = new SurfaceIntegral(0, (x,y,z) -> {return x;},
				  (x,y,z) -> {return y;},
				  (x,y,z) -> {return z;});

	bA = new SurfaceIntegral.Batched(siA, siA, siA);
	bV = new SurfaceIntegral.Batched(siV, siV, siV);


	TriangleCase();

	BGCycloid();
	ExtensionWithCap();
	RectTest();




    }
}
