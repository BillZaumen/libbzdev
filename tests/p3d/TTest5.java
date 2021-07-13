import org.bzdev.p3d.*;
import java.util.*;

// The values were obtained from another example that failed,
// so now incorporated in the test suite.
public class TTest5 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);

	double x1 = 50.0, y1 = 16.66666603088379, z1 =33.33333206176758;
	double x2 = 43.75, y2 = 43.75, z2 = 87.5;
	double x3 = 40.625, y3 = 40.625,z3 = 81.25;
	m3d.addTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);

	x1 = 50.0; y1 = 16.66666603088379; z1 = 33.33333206176758;
	x2 = 25.0; y2 = 25.0; z2 = 50.0;
	x3 = 21.875; y3 = 21.875; z3 = 43.75;
	m3d.addTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);


	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    System.out.println("Intersecting Triangles");
	    for (Model3D.Triangle triangle: tlist) {
		System.out.format
		    ("(%s,%s,%s)-(%s,%s,%s)-(%s,%s,%s)\n",
		     triangle.getX1(),triangle.getY1(), triangle.getZ1(),
		     triangle.getX2(),triangle.getY2(), triangle.getZ2(),
		     triangle.getX3(), triangle.getY3(), triangle.getZ3());

	    }
	    System.out.println("(error message unexpected)");
	    System.exit(1);
	} else {
	    System.out.println("(error statement missing as expected)");
	    System.exit(0);
	}

    }
}
