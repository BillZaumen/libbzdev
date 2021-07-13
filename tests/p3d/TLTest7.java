import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.util.Iterator;
import java.io.FileInputStream;

public class TLTest7 {
    public static void main(String argv[]) throws Exception {
	int N = 4;
	int M = 4;
	Point3D[][] array1 = new Point3D[N][M];
	Point3D[][] array2 = new Point3D[N][M];
       
	double delta = 10.0;

	for (int i = 0; i < N; i++) {
	    double x = delta * i;
	    for (int j = 0; j < M; j++) {
		double y = delta * j;
		array1[i][j] = new Point3D.Double(x, y, 30.0);
		if ((i == 1 || i == 2) && (j == 1 || j == 2)) {
		    array2[i][j] = new Point3D.Double(x, y, 0.0);
		} else {
		    array2[i][j] = new Point3D.Double(x, y, 20.0);
		}
	    }
	}
	BezierGrid grid1 = new BezierGrid(array1);
	BezierGrid grid2 = new BezierGrid(array2);
	for (int i = 1; i < 3; i++) {
	    for (int j = 1; j < 3; j++) {
		grid2.setRegion(i, j, 2);
	    }
	}
	grid2.reverseOrientation(true);

	Surface3D surface = new Surface3D.Double();
	surface.append(grid1);
	surface.append(grid2);
	
	double[] coords = new double[48];
	double[] coords1 = new double[12];
	double[] coords2 = new double[12];

	for (int i = 0; i < N-1; i++) {
	    grid1.getFullSplineU(i,0,coords1);
	    grid2.getFullSplineU(i,0, coords2);
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
	    Surface3D.setupU0ForPatch(Path3D.setupCubic(p2, p1), coords, false);
	    p1 = grid1.getPoint(i+1, M-1);
	    p2 = grid2.getPoint(i+1, M-1);
	    Surface3D.setupU1ForPatch(Path3D.setupCubic(p2, p1), coords, false);
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
	if (!surface.isWellFormed(System.out)) {
	    System.out.println("Surface is not well formed");
	}
	System.out.println("model size = " + m3d.size());

	m3d.setTessellationLevel(1);

	Iterator<Model3D.Triangle> it = m3d.tessellate();

	while (it.hasNext()) {
	    Model3D.Triangle triangle = it.next();
	    if (triangle.getZ1() == 0.0
		&& triangle.getZ2() == 0.0
		&& triangle.getZ3() == 0.0) {
		System.out.format
		    ("(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
		     triangle.getX1(), triangle.getY1(), triangle.getZ1(),
		     triangle.getX2(), triangle.getY2(), triangle.getZ2(),
		     triangle.getX3(), triangle.getY3(), triangle.getZ3());
	    }
	}
	m3d.writeSTL("TLTest7", "tltest7.stl");

	Model3D tm3d =new Model3D();
	BinarySTLParser bp =
	    new BinarySTLParser(new FileInputStream("tltest7.stl"));
	bp.addToModel(tm3d);
	double base = tm3d.getMinZ();
	System.out.println("after reading model back in");

	for (Model3D.Triangle triangle: tm3d.triangles()) {
	    if (triangle.getZ1() == base
		&& triangle.getZ2() == base
		&& triangle.getZ3() == base) {
		System.out.format
		    ("(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
		     triangle.getX1(), triangle.getY1(), triangle.getZ1(),
		     triangle.getX2(), triangle.getY2(), triangle.getZ2(),
		     triangle.getX3(), triangle.getY3(), triangle.getZ3());
	    }
	}
	

    }
}