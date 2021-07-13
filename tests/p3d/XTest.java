import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.io.File;

public class XTest {

    public static void main(String argv[]) throws Exception {

	double[] sarray =
	    {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
	double[] tarray = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};

	final int N = sarray.length;
	final int M = tarray.length;

	BezierGrid grid1 = new BezierGrid
	    (sarray, tarray, (s,t) -> {return 10*s;}, (s,t) -> {return 10*t;},
	     (s,t) -> {
		 double w = - (s*s/25 + t*t/6.25);
		 return 10*Math.exp(w);
	     });
	grid1.createSplines();

	BezierGrid grid2 = new BezierGrid
	    (sarray, tarray, (s,t) -> {return 10*s;}, (s,t) -> {return 10*t;},
	     (s,t) -> {return 0.0;});
	grid2.createSplines();


	grid2.reverseOrientation(true);

	double[] coords = new double[48];
	double[] coords1 = new double[12];
	double[] coords2 = new double[12];

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);

	for (int i = 0; i < N-1; i++) {
	    grid1.getFullSplineU(i,0,coords1);
	    grid2.getFullSplineU(i,0,coords2);
	    Surface3D.setupV0ForPatch(coords2, coords, false);
	    Surface3D.setupV1ForPatch(coords1, coords, false);
	    Point3D p1 = grid1.getPoint(i, 0);
	    Point3D p2 = grid2.getPoint(i, 0);
	    Surface3D.setupU0ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    p1 = grid1.getPoint(i+1, 0);
	    p2 = grid2.getPoint(i+1, 0);
	    Surface3D.setupU1ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    Surface3D.setupRestForPatch(coords);
	    surface.addCubicPatch(coords);

	    grid1.getFullSplineU(i,M-1,coords1);
	    grid2.getFullSplineU(i,M-1, coords2);
	    Surface3D.setupV0ForPatch(coords2, coords, false);
	    Surface3D.setupV1ForPatch(coords1, coords, false);
	    p1 = grid1.getPoint(i, M-1);
	    p2 = grid2.getPoint(i, M-1);
	    Surface3D.setupU0ForPatch(Path3D.setupCubic(p2, p1),
				      coords, false);
	    p1 = grid1.getPoint(i+1, M-1);
	    p2 = grid2.getPoint(i+1, M-1);
	    Surface3D.setupU1ForPatch(Path3D.setupCubic(p2, p1),
				      coords, false);
	    Surface3D.setupRestForPatch(coords);
	    surface.addFlippedCubicPatch(coords);

	}
	for (int j = 0; j < M-1; j++) {
	    grid1.getFullSplineV(0,j,coords1);
	    grid2.getFullSplineV(0,j, coords2);
	    Surface3D.setupV0ForPatch(coords2, coords, true);
	    Surface3D.setupV1ForPatch(coords1, coords, true);
	    Point3D p1 = grid1.getPoint(0, j);
	    Point3D p2 = grid2.getPoint(0, j);
	    Surface3D.setupU1ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    p1 = grid1.getPoint(0, j+1);
	    p2 = grid2.getPoint(0, j+1);
	    Surface3D.setupU0ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    Surface3D.setupRestForPatch(coords);
	    surface.addCubicPatch(coords);

	    grid1.getFullSplineV(N-1, j, coords1);
	    grid2.getFullSplineV(N-1, j, coords2);
	    Surface3D.setupV0ForPatch(coords2, coords, true);
	    Surface3D.setupV1ForPatch(coords1, coords, true);
	    p1 = grid1.getPoint(N-1, j);
	    p2 = grid2.getPoint(N-1, j);
	    Surface3D.setupU1ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    p1 = grid1.getPoint(N-1, j+1);
	    p2 = grid2.getPoint(N-1, j+1);
	    Surface3D.setupU0ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    Surface3D.setupRestForPatch(coords);
	    surface.addFlippedCubicPatch(coords);
	}

	Model3D m3d = new Model3D();
	m3d.append(surface);
	m3d.setTessellationLevel(2);
	m3d.setStackTraceMode(true);

	if (m3d.notPrintable(System.out)) {
	    System.out.println("m3d not printable");
	    System.exit(1);
	}


	m3d.writeX3D("XTest", "test of X3D format generation", null,
		     "xtest.x3d");

	m3d.writeX3D("XTest", "test of X3D format generation", null,
		     "xtest.x3dz");

	m3d.writeX3D("XTest", "test of X3D format generation", null,
		     false, new File("xtest2.x3d"));

	m3d.writeX3D("XTest", "test of X3D format generation", null,
		     false, new File ("xtest2.x3dz"));

	m3d.writeSTL("XTest - test using STL format", "xtest.stl");

	System.exit(0);
    }
}
