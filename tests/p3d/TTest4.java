import org.bzdev.p3d.*;
import java.util.*;

// The values were obtained from another example that failed,
// so now incorporated in the test suite.
public class TTest4 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);

	m3d.addTriangle(0.0, 50.0, 0.0,
			0.0, 0.0, 0.0,
			25.0, 25.0, 50.0);
	m3d.addTriangle(0.0, 100.0, 0.0,
			0.0,  50.0, 0.0,
			25.0, 75.0, 50.0);


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
