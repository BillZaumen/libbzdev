import java.awt.geom.*;
import java.util.ArrayList;
import org.bzdev.geom.*;
import org.bzdev.math.*;

public class BuilderTest11 {

    public static void main(String argv[]) throws Exception {
	// Check symmetry 

	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	SplinePathBuilder.CPoint list[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, 0.0, 1.0/3.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 1.0, 1.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 2.0, 1.5),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 3.0, 1.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, 4.0, 1.0/3.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 4.0, 0.0),
	};

	double knots[] = { 0.0, 1.0, 1.5, 1.0, 0.0};

	spb.initPath();
	spb.append(list);

	BasicSplinePath2D path = spb.getPath();
	Path2DInfo.printSegments(path);
	double deriv1 = path.dyDu(0.0);
	double deriv2 = path.dyDu(4.0);
	System.out.format("deriv1 = %s, deriv2 = %s\n", deriv1, deriv2);
	CubicBezierSpline1 spline = new
	    CubicBezierSpline1(knots, 0.0, 1.0,
			       CubicSpline.Mode.CLAMPED,
			       1.0, -1.0);
	System.out.print("coeff: ");
	for (double c: spline.getBernsteinCoefficients()) {
	    System.out.format("%g ", c);
	}
	System.out.println();

	System.exit(0);
    }
}
