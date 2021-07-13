import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;

public class BGPathTest2 {
    public static void main(String argv[]) throws Exception {
	double h1 = 4.0;	// disk thickness.
	double h2 = 30;		// base to underside of the disk
	double r1 = 15.0;	// inner tube radius
	double r2 = 20.0;	// outer tube radius
	double r3 = 30.0;	// disk radius
	double depth = 2.0;	// 1/2 the gap between parts.
	double rw = 1.25;		// Wire radius

	ArrayList<Point3D> wirelist = new ArrayList<>();
	
	double r = r3 - 2*rw;
	double h = h2 + h1 + 4*rw;
	double theta = 0.0;
	double deltaTheta = Math.PI/20;
	int i = 0;
	while (r > r1 + depth - 3*rw) {
	    if (i++ == 10) break;
	    wirelist.add(new Point3D.Double
			 (r*Math.cos(theta), r*Math.sin(theta), h));
	    theta += deltaTheta;
	    r -= 5*rw / 40;
	}

	Point3D[] wirePoints = new Point3D[wirelist.size()];
	wirelist.toArray(wirePoints);

	SplinePath3D wirePath = new SplinePath3D(wirePoints, false);

	Path2D template = Paths2D.createArc(0.0, 0.0, rw, 0.0,
					    2*Math.PI, Math.PI/4);
	template.closePath();

	double inormal[] = {1.0, 0.0, 0.0};
 
	BezierGrid.Mapper mapper = BezierGrid.getMapper(wirePath, inormal);

	BezierGrid wire = new BezierGrid(template, mapper);

	if (!wire.isWellFormed(System.out)) {
	    System.out.println("... not well formed");

	}

	Path3D bd1 = wire.getBoundary(0,0);

	Point3D wstart = wirePath.getStart();
	Point3D wend = wirePath.getEnd();
	System.out.println("wstart = " + wstart);
	System.out.println("wirePoints[0] = " + wirePoints[0]);
	System.out.println("wend = " + wend);
	System.out.println("wirePoints[<LAST>] = "
			   + wirePoints[wirePoints.length - 1]);

	Path3D bd1a = wire.getBoundary(wstart, null, true);
	Path3D bd1b = wire.getBoundary(null, (offset, array) -> {
		return array[offset] > 15.0;
	    }, true);

	Path3D bd2 = wire.getBoundary(wend, null, true);

	Path3DInfo.printSegments(bd1);
	Path3DInfo.printSegments(bd1a);
	Path3DInfo.printSegments(bd1b);

	BezierCap bc = new BezierCap(bd1, 0.0, true);

    }   
}
