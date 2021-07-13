import org.bzdev.geom.*;
import java.awt.geom.*;
import org.bzdev.math.*;

public class BezierVertexTest {
    public static void main(String argv[]) throws Exception {

	Path2D circle = Paths2D.createArc(51.0, 51.0, 51.0, 1.0,
					  2*Math.PI, Math.PI/2);
	circle.closePath();

	System.out.println("created 2D circle");

	Path3D circle3 = new Path3D.Double(circle, (nn, p, t, bbb) -> {
		return p;}, 0);

	BezierVertex bv1 = new BezierVertex(circle3, 50.0);

	System.out.println("bv1 bounds: " + bv1.getBounds());
	if (bv1.isWellFormed(System.out)) {
	    System.out.println("bv1 is well formed");
	} else {
	    System.out.println("bv1 is not well formed");
	}

	Path3D boundary = bv1.getBoundary();
	double blen = Path3DInfo.pathLength(boundary);
	System.out.format("bz1 boundary length = %g expecting %g for "
			  + " a perfect circle\n", blen, 100*Math.PI);


	BezierVertex bv2 = new BezierVertex(circle3, -50.0);
	bv2.flip();
	System.out.println("bv2 bounds: " + bv2.getBounds());
	if (bv2.isWellFormed(System.out)) {
	    System.out.println("bv2 is well formed");
	} else {
	    System.out.println("bv2 is not well formed");
	}

	Surface3D surface = new Surface3D.Double();
	surface.append(bv1);
	surface.append(bv2);
	
	System.out.println("surface.size() = " + surface.size());
	System.out.format("surface volume = %g, expecting %g "
			  + "for perfect cones\n",
			  surface.volume(), Math.PI*50.0*50.0*100.0/3.0);

	Point3D cm = Surface3D.centerOfMassOf(surface);
	System.out.println("cm = " + cm);
	double[][] moments = Surface3D.momentsOf(surface,
						 new Point3D.Double(51.0, 51.0,
								    0.0));
	double[][] I = SurfaceOps.toMomentsOfInertia(moments);
	double expecting[][] = {
	    {625.0, 0.0, 0.0},
	    {0.0, 625.0, 0.0},
	    {0.0, 0.0, 750.0}
	};
	System.out.println();
	for (int i = 0; i < 3; i++) {
	    if (i == 1) {
		System.out.print("I = |");
	    } else {
		System.out.print("    |");
	    }
	    for (int j = 0; j < 3; j++) {
		double val = I[i][j];
		if (val < 1.e-13) val = 0.0;
		System.out.format("%5.3g", val);
	    }
	    if (i == 1) {
		System.out.print(" |, expecting |");
	    } else {
		System.out.print(" |            |");
	    }
	    for (int j = 0; j < 3; j++) {
		double val = expecting[i][j];
		if (val < 1.e-13) val = 0.0;
		System.out.format("%5.3g", val);
	    }
	    System.out.println(" |");
	}
	System.out.println();

    }
}
