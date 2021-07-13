import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.FileOutputStream;
import java.util.Iterator;

public class TLTest2 {
    public static void main(String argv[]) throws Exception {

	int N = 32;
	int M = 18;
	// int N = 8;
	// int M = 4;

	Point3D[][] array = new Point3D[N][M];
	Point3D[] template = new Point3D[M];

	double r1 = 100.0;
	double r2 = 30.0;

	for (int i = 0; i < M; i++) {
	    double theta = (2 * Math.PI * i)/M;
	    double x = r1 + r2 * Math.cos(theta);
	    double y = 0.0;
	    double z = r2 * Math.sin(theta);
	    template[i] = new Point3D.Double(x, y, z);
	    array[0][i] = template[i];
	}

	for (int i = 1; i < N; i++) {
	    double phi = (2 * Math.PI * i)/N;
	    AffineTransform3D af =
		AffineTransform3D.getRotateInstance(phi, 0.0, 0.0);
	    for (int j = 0; j < M; j++) {
		array[i][j] = af.transform(template[j], null);
	    }
	}

	BezierGrid grid = new BezierGrid(array, true, true);

	System.out.println("grid bounding box: " + grid.getBounds());


	if (!grid.isWellFormed(System.out)) {
	    System.exit(1);
	}

	Model3D m3d = new Model3D();
	Model3D tm3d = new Model3D();

	m3d.append(grid);
	m3d.setTessellationLevel(2);

	/*
	int i = 0; 
	Iterator<Model3D.Triangle>it = m3d.tessellate();
	while (it.hasNext()) {
	    Model3D.Triangle triangle = it.next();
	    System.out.format("%d: "
			      + "(%g,%g,%g)-(%g,%g,%g)-(%g,%g,%g)\n",
			      ++i,
			      triangle.getX1(), triangle.getY1(),
			      triangle.getZ1(),
			      triangle.getX2(), triangle.getY2(),
			      triangle.getZ2(),
			      triangle.getX3(), triangle.getY3(),
			      triangle.getZ3());
	    tm3d.addTriangle(triangle.getX1(), triangle.getY1(),
			     triangle.getZ1(),
			     triangle.getX2(), triangle.getY2(),
			     triangle.getZ2(),
			     triangle.getX3(), triangle.getY3(),
			     triangle.getZ3());
	}
	*/

	if (m3d.notPrintable(System.out)) {
	    System.out.println("tm3d not printagble");
	    System.exit(1);
	}

	m3d.createImageSequence(new FileOutputStream("tltest2.isq"),
				 "png", 8, 4);

	System.out.println("m3d.size() = " + m3d.size());

	System.exit(0);
    }
}
