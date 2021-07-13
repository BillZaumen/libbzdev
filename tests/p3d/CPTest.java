import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.p3d.*;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.List;
import org.bzdev.math.VectorOps;


// Test cubic patch that is not tessellating correctly. The surface
// segment is stored in a binary file patch.dat and was
// obtained from a much larger test case that had failed.
// After some effort, that test case was reduced to one
// segment for testing.

public class CPTest {

    public static void main(String argv[]) throws Exception {
	
	DataInputStream din = new DataInputStream
	    (new FileInputStream("patch.dat"));
	Surface3D surface = new Surface3D.Double();
	Model3D m3d = new Model3D(false);
	double[] coords = new double[48];

	for (int i = 0; i < 48; i++) {
	    coords[i] = din.readDouble();
	}
	din.close();
	surface.addCubicPatch(coords);
	m3d.append(surface);
	m3d.setTessellationLevel(3);

	System.out.println("patch:");

	for (int i = 36; i >= 0; i -= 12) {
	    for (int j = 0; j < 12; j += 3) {
		System.out.format(" (%g, %g, %g)",
				  coords[i+j], coords[i+j+1], coords[i+j+2]);
	    }
	    System.out.println();
	}


	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	Model3D.Triangle triangle1 = null;
	Model3D.Triangle triangle2 = null;
	if (tlist != null && !tlist.isEmpty()) {
	    System.out.println("m3d not embedded");
	    for (Model3D.Triangle triangle: tlist) {
		System.out
		    .format("(%s, %s, %s)---(%s, %s, %s)---(%s, %s, %s)\n",
			    triangle.getX1(),
			    triangle.getY1(),
			    triangle.getZ1(),
			    triangle.getX2(),
			    triangle.getY2(),
			    triangle.getZ2(),
			    triangle.getX3(),
			    triangle.getY3(),
			    triangle.getZ3());
		System.out.format("    normal = (%s, %s, %s)\n",
				  triangle.getNormX(),
				  triangle.getNormY(),
				  triangle.getNormZ());
		if (triangle1 == null) triangle1 = triangle;
		else if (triangle2 == null) triangle2 = triangle;
	    }
	    double vector[] = {triangle1.getNormX(), triangle1.getNormY(),
			     triangle1.getNormZ()};
	    VectorOps.normalize(vector);
	    int i = 0;
	    double max = 0.0;
	    for (int j = 0; j < 3; j++) {
		double a = Math.abs(vector[j]);
		if (a > max) {
		    i = j;
		    max = a;
		}
	    }
	    double[] xdir = null;
	    double[] ydir = null;
	    switch (i) {
	    case 0:
		xdir = new double[] {0.0, 1.0, 0.0};
		ydir = new double[] {0.0, 0.0, 1.0};
		break;
	    case 1:
		xdir = new double[] {0.0, 0.0, 1.0};
		ydir = new double[] {1.0, 0.0, 0.0};
		break;
	    case 2:
		xdir = new double[] {1.0, 0.0, 0.0};
		ydir = new double[] {0.0, 1.0, 0.0};
		break;
	    }
	    double[] xaxis = VectorOps.crossProduct(ydir, vector);
	    VectorOps.normalize(xaxis);
	    double[] yaxis = VectorOps.crossProduct(vector, xaxis);
	    VectorOps.normalize(yaxis);
	    double[] zaxis = vector;
	    double pcoords[] = {triangle2.getX1() - triangle1.getX1(),
				triangle2.getY1() - triangle1.getY1(),
				triangle2.getZ1() - triangle1.getZ1()};
	    Point3D p1b =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));
	    pcoords = new double[] {triangle2.getX2() - triangle1.getX1(),
				    triangle2.getY2() - triangle1.getY1(),
				    triangle2.getZ2() - triangle1.getZ1()};
	    Point3D p2b =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));
	    
	    pcoords = new double[] {triangle2.getX3() - triangle1.getX1(),
				    triangle2.getY3() - triangle1.getY1(),
				    triangle2.getZ3() - triangle1.getZ1()};
	    Point3D p3b =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));


	    pcoords = new double[] {triangle1.getX1() - triangle1.getX1(),
				triangle1.getY1() - triangle1.getY1(),
				triangle1.getZ1() - triangle1.getZ1()};
	    Point3D p1a =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));

	    pcoords = new double[] {triangle1.getX2() - triangle1.getX1(),
				triangle1.getY2() - triangle1.getY1(),
				triangle1.getZ2() - triangle1.getZ1()};
	    Point3D p2a =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));

	    pcoords = new double[] {triangle1.getX3() - triangle1.getX1(),
				triangle1.getY3() - triangle1.getY1(),
				triangle1.getZ3() - triangle1.getZ1()};
	    Point3D p3a =
		new Point3D.Double(VectorOps.dotProduct(xaxis,pcoords),
				   VectorOps.dotProduct(yaxis, pcoords),
				   VectorOps.dotProduct(zaxis, pcoords));

	    System.out.println("Math.ulp(35.0F) = " + Math.ulp(35.0F));
	    System.out.println("p1a = " + p1a);
	    System.out.println("p2a = " + p2a);
	    System.out.println("p3a = " + p3a);


	    System.out.println("p1b = " + p1b);
	    System.out.println("p2b = " + p2b);
	    System.out.println("p3b = " + p3b);
	}
	m3d.createImageSequence(new FileOutputStream("cptest.isq"),
				 "png",
				 8, 6, 0.0, 0.5, 0.0, true);
	System.exit(0);
    }

}
