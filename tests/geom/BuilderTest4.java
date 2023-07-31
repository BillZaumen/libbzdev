import org.bzdev.geom.*;

public class BuilderTest4 {

    public static void main(String argv[]) throws Exception {
	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	SplinePathBuilder.CPoint[] cpoints1= new SplinePathBuilder.CPoint[5];

	cpoints1[0] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0);
	cpoints1[1] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 1.0, 0.0);
	cpoints1[2] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 1.0, 1.0);
	cpoints1[3] = new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 0.0, 1.0);
	cpoints1[4] = new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE);
	// apath.append(cpoints);
	spb.initPath(cpoints1);
	BasicSplinePath2D path = spb.getPath();
	System.out.println("isClosed = " + path.isClosed());
	path.printTable();
	System.out.println(".... segments ...");
	Path2DInfo.printSegments(path);
	System.out.println("maxParameter = " + path.getMaxParameter());
	System.out.println("path length = " + path.getPathLength());
	System.out.println("path length [0, 4] = "
			   + path.getPathLength(0.0, 4.0));
	System.out.println("path length [0, 5] = "
			   + path.getPathLength(0.0, 5.0));
	System.out.println("path length [0, 8] = "
			   + path.getPathLength(0.0, 8.0));
	System.exit(0);
    }
}
