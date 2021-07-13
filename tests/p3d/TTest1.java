import org.bzdev.p3d.*;
import java.util.*;

public class TTest1 {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);

	m3d.addTriangle(6.0, 2.0, 11.0,
			6.0, 3.0, 12.0,
			6.0, 3.0, 11.0);
	m3d.addTriangle(5.0, 5.0, 16.0,
			6.0, 3.0, 11.0,
			7.0, 3.0, 11.0);

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
