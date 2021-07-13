import org.bzdev.geom.BasicSplinePathBuilder;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.CPoint;


public class BuilderTest2 {
    public static void main(String argv[]) throws Exception {
	double r = 8.0;
	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	SplinePathBuilder.CPoint[] cpoints= new SplinePathBuilder.CPoint[37];
	cpoints[0] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, r, 0.0);
	for (int i = 1; i < 36; i++) {
	    double theta = Math.toRadians(i * 10.0);
	    cpoints[i] =
		new SplinePathBuilder.CPoint
		(SplinePathBuilder.CPointType.SPLINE,
		 r * Math.cos(theta), r * Math.sin(theta));
	}
	cpoints[36] =
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE);
	// apath.append(cpoints);
	spb.initPath(cpoints);
	BasicSplinePath2D path = spb.getPath();
	    System.out.format("dyDu(0.0) = %g, dxDu(0.0) = %g\n",
			      path.dyDu(0.0), path.dxDu(0.0));

	    double u = 36.0;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    
	    u = 35.0;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    
	    u = 0.0;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));


	    u = 0.0001;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    u = -u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	    
	    u = 35.0 + u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));

	    u = 1.0 + u;
	    System.out.format("u = %g: x = %g, y = %g\n",
			      u, path.getX(u), path.getY(u));
	System.exit(0);
    }
}
