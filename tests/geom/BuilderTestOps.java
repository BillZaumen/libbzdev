import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.math.RealValuedFunctOps;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;


public class BuilderTestOps {

    static RealValuedFunctOps fx = (t)->50.0*Math.cos(Math.toRadians(t));
    static RealValuedFunctOps fy = (t)->50.0*Math.sin(Math.toRadians(t));
    static RealValuedFunctOps linf = RealValuedFunctOps.identity();

    /*
    static RealValuedFunction fx = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction fy = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.sin(Math.toRadians(t));
	    }
	};

    static RealValuedFunction linf = new RealValuedFunction() {
	    public double valueAt(double t) {
		return t;
	    }
	};
    */
    public static void main(String argv[]) throws Exception {
	SplinePathBuilder spb = new SplinePathBuilder();

	System.out.println(".... case 1 ....");

	SplinePathBuilder.CPoint cpoints1[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 360.0, 36),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	spb.append(cpoints1);
	SplinePath2D path = spb.getPath();

	int count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, angle %g r=%g (at end)\n",
				  count,
				  entry.getTypeString(),
				  Math.toDegrees(Math.atan2(y, x)),
				  Math.sqrt(x*x + y*y));
	    }
	    count++;
	}

	Graph graph = new Graph(700, 700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	Graphics2D g2d = graph.createGraphics();

	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);

	graph.write("png", new File("bldtestOps1.png"));

	System.out.println(".... case 2 ....");

	
	SplinePathBuilder.CPoint cpoints2[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(linf, fx, -90.0, 90.0, 18),
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END_PREV)
	};

	spb = new SplinePathBuilder();
	spb.append(cpoints2);
	path = spb.getPath();
	
	graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	g2d = graph.createGraphics();

	count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, x =%g y=%g (at end)\n",
				  count,
				  entry.getTypeString(), x, y);
	    }
	    count++;
	}


	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("bldtestOps2.png"));

	System.out.println(".... case 3 ....");

	SplinePathBuilder.CPoint cpoints3[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, -90.0, 0.0),
	    new SplinePathBuilder.CPoint(linf, fx, -80.0, 80.0, 16),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 90.0, 0.0)
	};

	spb = new SplinePathBuilder();
	spb.append(cpoints3);
	path = spb.getPath();

	graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	g2d = graph.createGraphics();

	count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, x =%g y=%g (at end)\n",
				  count,
				  entry.getTypeString(), x, y);
	    }
	    count++;
	}


	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("bldtestOps3.png"));

	System.out.println(".... case 4 ....");


	SplinePathBuilder.CPoint cpoints4[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, -90.0, 0.0),
	    new SplinePathBuilder.CPoint(linf, fx, -80.0, 80.0, 16),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 90.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	spb = new SplinePathBuilder();
	spb.append(cpoints4);
	path = spb.getPath();

	graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	g2d = graph.createGraphics();

	count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, x =%g y=%g (at end)\n",
				  count,
				  entry.getTypeString(), x, y);
	    }
	    count++;
	}


	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("bldtestOps4.png"));

	System.out.println(".... case 5 ....");


	SplinePathBuilder.CPoint cpoints5[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(linf, fx, -90.0, 90.0, 18),
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END_PREV),
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	spb = new SplinePathBuilder();
	spb.append(cpoints5);
	path = spb.getPath();
	
	graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	g2d = graph.createGraphics();

	count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, x =%g y=%g (at end)\n",
				  count,
				  entry.getTypeString(), x, y);
	    }
	    count++;
	}

	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("bldtestOps5.png"));

	
	System.out.println(".... case 6 ....");

	SplinePathBuilder.CPoint cpoints6[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, -90.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, -80.0, 10.0),
	    
	    new SplinePathBuilder.CPoint(linf, fx, -70.0, 90.0, 16),
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	spb = new SplinePathBuilder();
	spb.append(cpoints6);
	path = spb.getPath();
	
	graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	g2d = graph.createGraphics();

	count = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D point = entry.getEnd();
	    if (point == null) {
		System.out.format("%d: type %s", count, entry.getType());
	    } else {
		double x = point.getX();
		double y = point.getY();
		System.out.format("%d: type %s, x =%g y=%g (at end)\n",
				  count,
				  entry.getTypeString(), x, y);
	    }
	    count++;
	}

	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("bldtestOps6.png"));
	System.exit(0);
    }
}
