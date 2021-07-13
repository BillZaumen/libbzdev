import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class CondTest3D {
    public static void main(String argv[]) throws Exception {
	Path3D path1 = new Path3D.Double();

	path1.moveTo(10.0, 20.0, 30.0);
	path1.lineTo(20.0, 30.0, 40.0);
	path1.quadTo (40.0, 45.0, 47.0, 50.0, 60.0, 65.0);
	path1.curveTo(70.0, 80.0, 85.0, 90.0, 100.0, 105.0,
		      110.0, 120.0, 125.0);
	path1.closePath();

	ConditionalPathIterator3D pit =
	    new ConditionalPathIterator3D
	    (path1.getPathIterator(null),
	     (double[] coords) -> {
		for (int i = 0; i < coords.length; i++) {
		    if (Math.round(coords[i]) != coords[i]) return false;
		}
		return true;
	     });

	double[] coords = new double[9];
	while (!pit.isDone()) {
	    boolean next = false;
	    switch(pit.currentSegment(coords)) {
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 6; i++) {
		    if (Math.round(coords[i]) != coords[i]) {
			next = true;
			break;
		    }
		}
		if (next) break;
		throw new Exception();
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 9; i++) {
		    if (Math.round(coords[i]) != coords[i]) {
			next = true;
			break;
		    }
		}
		if (next) break;
		throw new Exception();
	    }
	    pit.next();
	}
	System.exit(0);
    }
}
