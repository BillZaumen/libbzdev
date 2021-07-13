 import org.bzdev.geom.*;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class CondTest2D {
    public static void main(String argv[]) throws Exception {
	Path2D path1 = new Path2D.Double();

	path1.moveTo(10.0, 20.0);
	path1.lineTo(20.0, 30.0);
	path1.quadTo (40.0, 45.0, 50.0, 60.0);
	path1.curveTo(70.0, 80.0, 90.0, 100.0, 110.0, 120.0);
	path1.closePath();

	ConditionalPathIterator2D pit =
	    new ConditionalPathIterator2D
	    (path1.getPathIterator(null),
	     (double[] coords) -> {
		for (int i = 0; i < coords.length; i++) {
		    if (Math.round(coords[i]) != coords[i]) return false;
		}
		return true;
	     });

	double[] coords = new double[6];
	while (!pit.isDone()) {
	    boolean next = false;
	    switch(pit.currentSegment(coords)) {
	    case PathIterator.SEG_CLOSE:
	    case PathIterator.SEG_MOVETO:
	    case PathIterator.SEG_LINETO:
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 4; i++) {
		    if (Math.round(coords[i]) != coords[i]) {
			next = true;
			break;
		    }
		}
		if (next) break;
		throw new Exception();
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 6; i++) {
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
