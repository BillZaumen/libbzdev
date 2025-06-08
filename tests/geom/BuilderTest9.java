import java.awt.geom.*;
import java.util.ArrayList;
import org.bzdev.geom.*;


public class BuilderTest9 {

    public static void main(String argv[]) throws Exception {
	// Check symmetry 

	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	SplinePathBuilder.CPoint list[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 2.0, 1.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 3.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, -2.0, 1.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb.initPath();
	spb.append(list);

	SplinePath2D path = spb.getPath();
	Path2DInfo.printSegments(path);
	System.exit(0);
    }
}
