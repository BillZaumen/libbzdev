import org.bzdev.p3d.*;
import java.util.*;

public class TTest {
    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D();

	m3d.addTriangle(0.0, 0.0, 0.0,
			0.0, 100.0, 0.0,
			100.0, 100.0, 0.0,
			null, "t1");
	m3d.addFlippedTriangle(0.0, 0.0, 0.0,
			       0.0, 100.0, 0.0,
			       100.0, 100.0, 0.0,
			       null, "t2");

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    P3d.printTriangleErrors(System.out, tlist);
	    java.util.Iterator<Model3D.Triangle> it = tlist.iterator();
	    while (it.hasNext()) {
		Model3D.Triangle t1 = it.next();
		Model3D.Triangle t2 = it.next();
		System.out.println(t1.toString() + " intersects "
				   + t2.toString());
	    }
	    System.out.println("(error message expected)");
	} else {
	    System.out.println("(error statement missing)");
	    System.exit(1);
	}
	System.out.println("--- non-planar intersecting ------");

	m3d = new Model3D();
	m3d.addTriangle(0.0, 0.0, 50.0,
			100.0, 0.0, 50.0,
			100.0, 100.0, 50.0);
	m3d.addTriangle(25.0, 5.0, 100.0,
			57.0, 5.0, 100.0,
			50.0, 5.0, 0.0);

	tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    java.util.Iterator<Model3D.Triangle> it = tlist.iterator();
	    while (it.hasNext()) {
		Model3D.Triangle t1 = it.next();
		Model3D.Triangle t2 = it.next();
		System.out.println(t1.toString() + " intersects "
				   + t2.toString());
	    }
	    System.out.println("(error message expected)");
	} else {
	    System.out.println("(error statement missing)");
	    System.exit(1);
	}
	System.out.println("--- planar overlapping ------");
	m3d = new Model3D();
	m3d.addTriangle(40.0, 0.0, 0.0,
			60.0, 0.0, 0.0,
			50.0,  100.0, 0.0);
	m3d.addTriangle(0.0, 40.0, 0.0,
			0.0, 60.0, 0.0,
			100.0, 50.0, 0.0);
	tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    java.util.Iterator<Model3D.Triangle> it = tlist.iterator();
	    while (it.hasNext()) {
		Model3D.Triangle t1 = it.next();
		Model3D.Triangle t2 = it.next();
		System.out.println(t1.toString() + " intersects "
				   + t2.toString());
	    }
	    System.out.println("(error message expected)");
	} else {
	    System.out.println("(error statement missing)");
	    System.exit(1);
	}

	m3d = new Model3D(false);

	System.out.println("try case in which two non-planar triangles ");
	System.out.println("touch at a vertex");

	m3d.addTriangle(6.0, 2.0, 11.0,
			6.0, 3.0, 12.0,
			6.0, 3.0, 11.0);
	m3d.addTriangle(5.0, 5.0, 16.0,
			6.0, 3.0, 11.0,
			7.0, 3.0, 11.0);

	tlist = m3d.verifyEmbedded2DManifold();
	if (tlist != null) {
	    java.util.Iterator<Model3D.Triangle> it = tlist.iterator();
	    while (it.hasNext()) {
		Model3D.Triangle t1 = it.next();
		Model3D.Triangle t2 = it.next();
		System.out.println(t1.toString() + " intersects "
				   + t2.toString());
	    }
	    System.out.println("(error message unexpected)");
	    System.exit(1);
	} else {
	    System.out.println("(error statement missing as expected)");
	}


	System.exit(0);
   }
}
