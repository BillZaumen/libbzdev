import org.bzdev.p3d.*;
import java.util.*;

public class TTest6 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);

	m3d.addTriangle(30.0, 85.0, 15.0,
			38.349365234375, 72.5, 15.0,
			45.0, 59.01923751831055, 15.0);
	m3d.addTriangle(37.72483825683594, 73.65023803710938, 8.0,
			37.92631149291992, 73.26321411132812, 15.0,
			37.72483825683594, 73.65023803710938, 15.0);

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
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
