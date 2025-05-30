import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import org.bzdev.math.RealValuedFunction;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;


public class BuilderTest {

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

    static RealValuedFunction fx100 = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 100.0 * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction fy100 = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 100.0 * Math.sin(Math.toRadians(t));
	    }
	};

    static RealValuedFunction fx40 = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 40.0 * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction fy40 = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 40.0 * Math.sin(Math.toRadians(t));
	    }
	};


    static RealValuedFunction linf = new RealValuedFunction() {
	    public double valueAt(double t) {
		return t;
	    }
	};

    public static void main(String argv[]) throws Exception {
	SplinePathBuilder spb = new SplinePathBuilder();
	SplinePathBuilder spbc =  new SplinePathBuilder();
	SplinePathBuilder spbr = new SplinePathBuilder();
	SplinePathBuilder spb1 = new SplinePathBuilder();
	SplinePathBuilder spb2 = new SplinePathBuilder();

	System.out.println("check some methods");
	System.out.println("spb.constantWIND_NON_ZERO() = "
			   + spb.constantWIND_NON_ZERO());
	System.out.println("spb.constantWIND_EVEN_ODD() = "
			   + spb.constantWIND_EVEN_ODD());
	System.out.println(".... case 1 ....");

	SplinePathBuilder.CPoint cpoints1[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 360.0, 36),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	AffineTransform af = AffineTransform.getRotateInstance(Math.PI/4);
	af.translate(25.0, 25.0);
	AffineTransform af2 = AffineTransform.getTranslateInstance(0.0, -25.0);
	AffineTransform af3 = AffineTransform.getTranslateInstance(15.0, -25.0);
	SplinePathBuilder.CPoint cpoints1c[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 360.0, 36, af),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	BasicSplinePathBuilder bspb = new BasicSplinePathBuilder();
	bspb.append(cpoints1);

	BasicSplinePath2D bpath  = bspb.getPath();
	double[] vector = new double[4];
	double kappa1 = bpath.curvature(0.0);
	double kappa2 = bpath.curvature(0.5);
	System.out.format("kappa1 = %g, kappa2 = %g\n", kappa1, kappa2);
	bpath.getTangent(0.0, vector);
	bpath.getTangent(0.0, vector, 2);
	System.out.format("(%g, %g), (%g, %g)\n",
			  vector[0], vector[1],
			  vector[2], vector[3]);
	bpath.getNormal(0.0, vector);
	bpath.getNormal(0.0, vector, 2);
	System.out.format("(%g, %g), (%g, %g)\n",
			  vector[0], vector[1],
			  vector[2], vector[3]);

	bpath.getTangent(4.5, vector);
	bpath.getTangent(4.5, vector, 2);
	System.out.format("(%g, %g), (%g, %g)\n",
			  vector[0], vector[1],
			  vector[2], vector[3]);
	bpath.getNormal(4.5, vector);
	bpath.getNormal(4.5, vector, 2);
	System.out.format("(%g, %g), (%g, %g)\n",
			  vector[0], vector[1],
			  vector[2], vector[3]);

	spb.append(cpoints1);
	spbc.append(cpoints1c);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	SplinePath2D path = spb.getPath();
	SplinePath2D pathc = spbc.getPath();
	SplinePath2D pathr = spbr.getPath();
	SplinePath2D path2 = spb2.getPath();
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

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLUE);
	graph.draw(g2d, pathc);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);

	graph.write("png", new File("bldtest1.png"));

	System.out.println(".... case 2 ....");
	
	SplinePathBuilder.CPoint cpoints2[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(linf, fx, -90.0, 90.0, 18),
	    new
	    SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END_PREV)
	};

	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints2);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();
	
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

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest2.png"));

	System.out.println(".... case 3 ....");

	SplinePathBuilder.CPoint cpoints3[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, -90.0, 0.0),
	    new SplinePathBuilder.CPoint(linf, fx, -80.0, 80.0, 16),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 90.0, 0.0)
	};

	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints3);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

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


	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest3.png"));

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
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints4);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

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


	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest4.png"));

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
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints5);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

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

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest5.png"));

	
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
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints6);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();
	
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

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest6.png"));

	System.out.println(".... case 7 ....");

	SplinePathBuilder.CPoint cpoints7[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 80.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 90.0, 170.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(180.0), fy.valueAt(180.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(190.0), fy.valueAt(190.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(200.0), fy.valueAt(200.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(210.0), fy.valueAt(210.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(220.0), fy.valueAt(220.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(230.0), fy.valueAt(230.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(240.0), fy.valueAt(240.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(250.0), fy.valueAt(250.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(260.0), fy.valueAt(260.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 270.0, 350.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints7);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	System.out.println("spb1.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb1.getCPoints(false, null)) {
	    System.out.println("    " + cpt.type);
	}
	spb2.append(spb1.getCPoints(true, null));
	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest7.png"));

	System.out.println(".... case 8 ....");

	SplinePathBuilder.CPoint cpoints8[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 80.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 0.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints8);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    System.out.println("    " + cpt.type);
	}
	path = spb.getPath();
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest8.png"));

	System.out.println(".... case 9 ....");

	SplinePathBuilder.CPoint cpoints9[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 80.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 0.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints9);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch(cpt.type) {
	    case SPLINE:
	    case CONTROL:
	    case SEG_END:
		System.out.println("    " + cpt.type + ", x = " + cpt.x
				   + ", y = " + cpt.y);
		break;
	    case SPLINE_FUNCTION:
		System.out.println("    " + cpt.type + ", t1 = " + cpt.t1
				   + ", t2 = " + cpt.t2);
		break;
	    default:
		System.out.println("    " + cpt.type);
		break;
	    }
	}
	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch(cpt.type) {
	    case SPLINE:
	    case CONTROL:
	    case SEG_END:
		System.out.println("    " + cpt.type + ", x = " + cpt.x
				   + ", y = " + cpt.y);
		break;
	    case SPLINE_FUNCTION:
		System.out.println("    " + cpt.type + ", t1 = " + cpt.t1
				   + ", t2 = " + cpt.t2);
		break;
	    default:
		System.out.println("    " + cpt.type);
		break;
	    }
	}


	path = spb.getPath();
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

	count = 0;
	System.out.println("path entries:");
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
	count = 0;
	System.out.println("pathr entries:");
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(pathr)) {
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

	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest9.png"));

	System.out.println(".... case 10 ....");

	SplinePathBuilder.CPoint cpoints10[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 50.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 43.3, 25.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 25.0, 43.3),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 0.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints10);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	path = spb.getPath();
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest10.png"));

	System.out.println(".... case 11 ....");

	SplinePathBuilder.CPoint cpoints11[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 50.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 43.3, 25.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, 25.0, 43.3),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, 0.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints11);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("creating path");
	path = spb.getPath();
	System.out.println("creating pathr");
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

	count = 0;
	System.out.println("path:");
	Path2DInfo.printSegments(path);
	/*
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
	*/
	System.out.println("pathr:");
	Path2DInfo.printSegments(pathr);
	/*
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(pathr)) {
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
	*/

	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest11.png"));

	System.out.println(".... case 12 ....");

	SplinePathBuilder.CPoint cpoints12[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 90.0, 3),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints12);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("creating path");
	path = spb.getPath();
	System.out.println("creating pathr");
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

	count = 0;
	System.out.println("path:");
	Path2DInfo.printSegments(path);
	/*
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
	*/
	System.out.println("pathr:");
	Path2DInfo.printSegments(pathr);
	/*
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(pathr)) {
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
	*/

	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest12.png"));


	System.out.println(".... case 7a ....");

	SplinePathBuilder.CPoint cpoints7a[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 80.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 90.0, 170.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(180.0), fy.valueAt(180.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(190.0), fy.valueAt(190.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(200.0), fy.valueAt(200.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(210.0), fy.valueAt(210.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(220.0), fy.valueAt(220.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 1.3*fx.valueAt(220.0),
					 1.3*fy.valueAt(220.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(230.0), fy.valueAt(230.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(240.0), fy.valueAt(240.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(250.0), fy.valueAt(250.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(260.0), fy.valueAt(260.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 270.0, 350.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints7a);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	System.out.println("spb.getCPoints(false, af2):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, af2)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("spb.getCPoints(true, af3):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, af3)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	spb2.append(spb1.getCPoints(false, null));
	spb2.append(spb1.getCPoints(true, null));
	System.out.println("spb1.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb1.getCPoints(true, af2)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest7a.png"));


	System.out.println(".... case 7b ....");

	SplinePathBuilder.CPoint cpoints7b[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 0.0, 0.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 80.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 90.0, 170.0, 8),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(180.0), fy.valueAt(180.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(190.0), fy.valueAt(190.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(200.0), fy.valueAt(200.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(210.0), fy.valueAt(210.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(220.0), fy.valueAt(220.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 1.3*fx.valueAt(220.0),
					 1.3*fy.valueAt(220.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(230.0), fy.valueAt(230.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(240.0), fy.valueAt(240.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(250.0), fy.valueAt(250.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(260.0), fy.valueAt(260.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 270.0, 350.0, 8),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb1 = new SplinePathBuilder();
	spb2 = new SplinePathBuilder();
	spb.append(cpoints7b);
	spbr.append(spb.getCPoints(true, null));
	spb1.append(spb.getCPoints(false, af2));
	spb1.append(spb.getCPoints(true, af3));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	System.out.println("spb.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(true, null)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	System.out.println("spb.getCPoints(false, af2):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, af2)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}
	System.out.println("spb.getCPoints(true, af3):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, af3)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	spb2.append(spb1.getCPoints(false, null));
	spb2.append(spb1.getCPoints(true, null));
	System.out.println("spb1.getCPoints(true, null):");
	for (SplinePathBuilder.CPoint cpt: spb1.getCPoints(true, af2)) {
	    switch (cpt.type) {
	    case CLOSE:
	    case SEG_END_NEXT:
	    case SEG_END_PREV:
	    case MOVE_TO_NEXT:
		System.out.println("    " + cpt.type);
		break;
	    default:
		System.out.println("    " + cpt.type +", x = " + cpt.x
				   + ", y = " + cpt.y);
	    }
	}

	path = spb.getPath();
	pathr = spbr.getPath();
	path2 = spb2.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path2);
	graph.write("png", new File("bldtest7b.png"));

	System.out.println(".... case 13 ....");

	SplinePathBuilder.CPoint cpoints13[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 0.0, 40.0, 4),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 50, 90.0, 4),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(100.0), fy.valueAt(100.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(110.0), fy.valueAt(110.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(120.0), fy.valueAt(120.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(130.0), fy.valueAt(130.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(140.0), fy.valueAt(140.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(150.0), fy.valueAt(150.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(160.0), fy.valueAt(160.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(170.0), fy.valueAt(170.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(fx, fy, 180, 240.0, 2),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(250.0), fy.valueAt(250.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(260.0), fy.valueAt(260.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(270.0), fy.valueAt(270.0))
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints13);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    System.out.println("    " + cpt.type);
	}
	path = spb.getPath();
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest13.png"));


	System.out.println(".... case 14 ....");

	SplinePathBuilder.CPoint cpoints14[] = {
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.MOVE_TO,
					 fx.valueAt(0.0), fy.valueAt(0.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(10.0), fy.valueAt(10.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(20.0), fy.valueAt(20.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(30.0), fy.valueAt(30.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(40.0), fy.valueAt(40.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_NEXT),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(50.0), fy.valueAt(50.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(60.0), fy.valueAt(60.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(70.0), fy.valueAt(70.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(80.0), fy.valueAt(80.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt( 90.0), fy.valueAt(90.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(100.0), fy.valueAt(100.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(110.0), fy.valueAt(110.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(120.0), fy.valueAt(120.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 fx.valueAt(130.0), fy.valueAt(130.0)),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END_PREV),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(140.0), fy.valueAt(140.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(150.0), fy.valueAt(150.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(160.0), fy.valueAt(160.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CONTROL,
					 fx.valueAt(170.0), fy.valueAt(170.0)),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 fx.valueAt(180.0), fy.valueAt(180.0)),
	};
	spb = new SplinePathBuilder();
	spbr = new SplinePathBuilder();
	spb.append(cpoints14);
	spbr.append(spb.getCPoints(true, null));
	System.out.println("spb.getCPoints(false, null):");
	for (SplinePathBuilder.CPoint cpt: spb.getCPoints(false, null)) {
	    System.out.println("    " + cpt.type);
	}
	path = spb.getPath();
	pathr = spbr.getPath();

	graph = new Graph(700,700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

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
	g2d = graph.createGraphics();

	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(4.5F));
	graph.draw(g2d, pathr);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(3.0F));
	graph.draw(g2d, path);
	graph.write("png", new File("bldtest14.png"));

	boolean dir = true;

	SplinePathBuilder.CPoint[] array1 = {new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.MOVE_TO,
					 100.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 0.0, 100.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 -100.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 0.0, -100.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	SplinePathBuilder.modifyCPoints(array1, dir, null);
	System.out.println("array1 (reversed):");
	for (SplinePathBuilder.CPoint cpoint: array1) {
	    if (cpoint.type == SplinePathBuilder.CPointType.SPLINE_FUNCTION) {
		System.out.format("    type %s, t1 = %g, t2 = %g, n = %d\n",
				  cpoint.type, cpoint.t1, cpoint.t2, cpoint.n);
	    } else if (cpoint.type == SplinePathBuilder.CPointType.CLOSE
		       || cpoint.type
		       == SplinePathBuilder.CPointType.MOVE_TO_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_PREV) {
		System.out.format("    type %s\n", cpoint.type);
	    } else {
		System.out.format("    type %s, point (%g, %g)\n",
				  cpoint.type, cpoint.x, cpoint.y);
	    }
	}

	SplinePathBuilder.CPoint[] array2 = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx100, fy100, 0.0, 270.0, 3),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	SplinePathBuilder.modifyCPoints(array2, dir, null);
	System.out.println("array2 (reversed):");
	for (SplinePathBuilder.CPoint cpoint: array2) {
	    if (cpoint.type == SplinePathBuilder.CPointType.SPLINE_FUNCTION) {
		System.out.format("    type %s, t1 = %g, t2 = %g, n = %d\n",
				  cpoint.type, cpoint.t1, cpoint.t2, cpoint.n);
	    } else if (cpoint.type == SplinePathBuilder.CPointType.CLOSE
		       || cpoint.type
		       == SplinePathBuilder.CPointType.MOVE_TO_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_PREV) {
		System.out.format("    type %s\n", cpoint.type);
	    } else {
		System.out.format("    type %s, point (%g, %g)\n",
				  cpoint.type, cpoint.x, cpoint.y);
	    }
	}
	SplinePathBuilder.CPoint[] array3 = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(fx100, fy100, 0.0, 180.0, 2),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 0.0, -100.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE),
	};

	SplinePathBuilder.modifyCPoints(array3, dir, null);
	System.out.println("array3 (reversed):");
	for (SplinePathBuilder.CPoint cpoint: array3) {
	    if (cpoint.type == SplinePathBuilder.CPointType.SPLINE_FUNCTION) {
		System.out.format("    type %s, t1 = %g, t2 = %g, n = %d\n",
				  cpoint.type, cpoint.t1, cpoint.t2, cpoint.n);
	    } else if (cpoint.type == SplinePathBuilder.CPointType.CLOSE
		       || cpoint.type
		       == SplinePathBuilder.CPointType.MOVE_TO_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_PREV) {
		System.out.format("    type %s\n", cpoint.type);
	    } else {
		System.out.format("    type %s, point (%g, %g)\n",
				  cpoint.type, cpoint.x, cpoint.y);
	    }
	}

	SplinePathBuilder.CPoint[] array4 = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 50.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 0.0, 50.0),
	    new SplinePathBuilder.CPoint(fx, fy, 180.0, 270.0, 1),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE),
	};

	spb = new SplinePathBuilder();
	spb.append(array4);
	Path2D path4 = spb.getPath();

	SplinePathBuilder.modifyCPoints(array4, dir, null);

	System.out.println("array4 (reversed):");
	for (SplinePathBuilder.CPoint cpoint: array4) {
	    if (cpoint.type == SplinePathBuilder.CPointType.SPLINE_FUNCTION) {
		System.out.format("    type %s, t1 = %g, t2 = %g, n = %d\n",
				  cpoint.type, cpoint.t1, cpoint.t2, cpoint.n);
	    } else if (cpoint.type == SplinePathBuilder.CPointType.CLOSE
		       || cpoint.type
		       == SplinePathBuilder.CPointType.MOVE_TO_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_PREV) {
		System.out.format("    type %s\n", cpoint.type);
	    } else {
		System.out.format("    type %s, point (%g, %g)\n",
				  cpoint.type, cpoint.x, cpoint.y);
	    }
	}

	spb = new SplinePathBuilder();
	spb.append(array4);
	Path2D path4r = spb.getPath();

	spb = new SplinePathBuilder();
	spb.append(array1);
	Path2D path1 = spb.getPath();

	spb = new SplinePathBuilder();
	spb.append(array2);
	path2 = spb.getPath();

	spb = new SplinePathBuilder();
	spb.append(array3);
	Path2D path3 = spb.getPath();


	SplinePathBuilder.CPoint[] array5 = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 40.0, 0.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SPLINE,
					 0.0, 40.0),
	    new SplinePathBuilder.CPoint(fx40, fy40, 180.0, 270.0, 1),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.SEG_END,
					 0.0, 0.0)
	};

	spb = new SplinePathBuilder();
	spb.append(array5);
	Path2D path5 = spb.getPath();
	try {
	    SplinePathBuilder.modifyCPoints(array5, dir, null);
	} catch (IllegalArgumentException iae) {
	    System.out.println("EXCEPTION EXPECTED: " + iae.getMessage());
	}

	System.out.println("array5 (restored):");
	for (SplinePathBuilder.CPoint cpoint: array5) {
	    if (cpoint.type == SplinePathBuilder.CPointType.SPLINE_FUNCTION) {
		System.out.format("    type %s, t1 = %g, t2 = %g, n = %d\n",
				  cpoint.type, cpoint.t1, cpoint.t2, cpoint.n);
	    } else if (cpoint.type == SplinePathBuilder.CPointType.CLOSE
		       || cpoint.type
		       == SplinePathBuilder.CPointType.MOVE_TO_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_NEXT
		       || cpoint.type
		       == SplinePathBuilder.CPointType.SEG_END_PREV) {
		System.out.format("    type %s\n", cpoint.type);
	    } else {
		System.out.format("    type %s, point (%g, %g)\n",
				  cpoint.type, cpoint.x, cpoint.y);
	    }
	}

	graph = new Graph(700, 700);
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);

	g2d = graph.createGraphics();

	g2d.setColor(Color.BLUE);
	g2d.setStroke(new BasicStroke(12.0F));
	graph.draw(g2d, path1);
	graph.draw(g2d, path5);
	g2d.setColor(Color.GREEN);
	g2d.setStroke(new BasicStroke(6.0F));
	graph.draw(g2d, path2);
	graph.draw(g2d, path4);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(2.0F));
	graph.draw(g2d, path3);
	graph.draw(g2d, path4r);

	graph.write("png", new File("bldtest15.png"));

	System.out.println(".... case 15 ....");

	SplinePathBuilder.CPoint cpoints15[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 10.0, 20.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 5.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	spb = new SplinePathBuilder();
	spb.append(cpoints15);
	path = bspb.getPath();
	Path2DInfo.printSegments(path);

	// check termination for BasicSplinePathBuilder with CLOSE
	// for cases with control points near the end

	System.out.println("---- cpoints 15 (basic)----");
	bspb = new BasicSplinePathBuilder();
	bspb.append(cpoints15);
	path = bspb.getPath();
	Path2DInfo.printSegments(path);

	System.out.println("---- cpoints 16 (basic) ----");
	SplinePathBuilder.CPoint cpoints16[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 10.0, 20.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SEG_END, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 5.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	bspb = new BasicSplinePathBuilder();
	bspb.append(cpoints16);
	path = bspb.getPath();
	Path2DInfo.printSegments(path);

	System.out.println("---- cpoints 17 (basic) ----");
	SplinePathBuilder.CPoint cpoints17[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 10.0, 20.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 5.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	bspb = new BasicSplinePathBuilder();
	bspb.append(cpoints17);
	path = bspb.getPath();
	Path2DInfo.printSegments(path);

	System.out.println("---- cpoints 18a (basic) ----");
	SplinePathBuilder.CPoint cpoints18[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO, 10.0, 20.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -30.0, 10.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.SPLINE, -50.0, 50.0),
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.CONTROL, -50.0, 5.0),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};
	bspb = new BasicSplinePathBuilder();
	bspb.append(cpoints18);
	path = bspb.getPath();
	Path2DInfo.printSegments(path);


	System.exit(0);
    }
}
