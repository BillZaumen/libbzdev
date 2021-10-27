import java.awt.*;
import java.awt.geom.*;
import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.p3d.*;

public class CylinderTest {
    public static void main(String argv[]) throws Exception {

	Path2D triangle = new Path2D.Double();
	triangle.moveTo(0.0, 0.0);
	triangle.lineTo(1.0, 0.0);
	triangle.lineTo(0.0, 1.0);
	triangle.closePath();
	System.out.println("n drawable segs for triangle = "
			   + Path2DInfo.numberOfDrawableSegments(triangle));
	System.out.println("n drawable knots for triangle = "
			   + Path2DInfo.numberOfDrawableKnots(triangle));

	

	Path2D line = new Path2D.Double();
	line.moveTo(100.0, 100.0);
	line.lineTo(100.0, 0.0);
	line.lineTo(100.0, -100.0);

	System.out.println("n drawable segs = "
			   + Path2DInfo.numberOfDrawableSegments(line));
	System.out.println("n drawable knots = "
			   + Path2DInfo.numberOfDrawableKnots(line));
	BezierGrid grid = new BezierGrid(line, (i, pt, type, bounds) -> {
		double theta = (i*2*Math.PI/8);
		double r = pt.getX();
		double x = r*Math.cos(theta);
		double y = r*Math.sin(theta);
		double z = pt.getY();
		return new Point3D.Double(x, y, z);
	}, 8, true);
	
	System.out.println("nU = " +grid.getUArrayLength());
	System.out.println("nV = " +grid.getVArrayLength());
	grid.print();

	Path3D bp = grid.getBoundary();
	Path3DInfo.printSegments(bp);
	System.out.println(bp);
	int nV = grid.getVArrayLength();
	System.out.println("grid.getBoundary(0,0):");
	Path3DInfo.printSegments(grid.getBoundary(0,0));
	System.out.println("grid.getBoundary(0,2):");
	Path3DInfo.printSegments(grid.getBoundary(0,2));
    }
}
