import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class BasicPathTest {

    private static Graph.UserDrawable symbol = new Graph.UserDrawable() {
	    Ellipse2D circle =
		new Ellipse2D.Double(-10.0, -10.0, 20.0, 20.0);
	    public Shape toShape(boolean xAxisPointsRight,
				 boolean yAxisPointsDown)
	    {
		return circle;
	    }
	};

    public static void main(String argv[]) throws Exception {
	Graph graph = new Graph();
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0, -100.0, 100.0);
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.RED);

	double xc[] = new double[18];
	double yc[] = new double[18];
	double xc2[] = new double[18];
	double yc2[] = new double[18];
	for (int i = 0; i < 18; i++) {
	    double theta = Math.toRadians((double)20.0 * i);
	    double r = 100.0;
	    double x  = r * Math.cos(theta);
	    double y = r * Math.sin(theta);
	    xc[i] = x;
	    xc2[i] = x;
	    yc[i] = y;
	    yc2[i] = -y;
	    graph.draw(g2d, symbol, x, y);
	}
	BasicSplinePath2D circle = new BasicSplinePath2D(xc, yc, true);
	BasicSplinePath2D circle2 = new BasicSplinePath2D(xc2, yc2, true);
	    
	circle.printTable(System.out);

	g2d.setColor(Color.BLACK);
	graph.draw(g2d, circle);

	g2d.setColor(Color.RED);
	for (int i = 0; i < 18; i++) {
	    for (double delta = 0.0; delta < 0.99; delta += 0.5) {
		double theta = Math.toRadians((double)20.0 * i);
		double r = 100.0;
		double u = i + delta;
		double x = circle.getX(u);
		double y = circle.getY(u);
		Point2D point = circle.getPoint(u);
		if (point.getX() != x || point.getY() != y) {
		    throw new Exception
			("getPoint, getX, and getY don't agree");
		}
		double radius = Math.sqrt(x*x + y*y);
		double angle = Math.toDegrees(Math.atan2(y, x));
		if (angle < 0) angle = 360 + angle;

		/*
		  System.out.println("u = " + u + ", x = " + x
		  + ", y = " + y
		  + ", r = " + radius
		  + ", angle = " + angle);
		*/
		graph.draw(g2d, symbol, x, y);
		graph.fill(g2d, symbol, x, y);
	    }
	}
	graph.write("png", "sptest.png");
	System.out.println("Segment 2 length = "
			   + circle.getPathLength(2.0, 3.0));

	System.out.println("Length for u = 12.5 to 12.6: "
			   + circle.getPathLength(12.5, 12.6));

	System.out.println("Length for u = 5.5 to 7.5: "
			   + circle.getPathLength(5.5, 7.5));

	System.out.println("Segment 10 length = "
			   + circle.getPathLength(10.0, 11.0));

	System.out.println("Segment 10 + Segment 11 length = "
			   + circle.getPathLength(10.0, 12.0));

	System.out.println("Total length = " + circle.getPathLength());

	System.out.println("length for u = 0.0 to 18.0 = "
			   + circle.getPathLength(0.0, 18.0));
	System.out.println("length for u = -0.5 to 18.5 = "
			   + circle.getPathLength(-0.5, 18.5));
	System.out.println("length for u = 0.0 to 36.0 = "
			   + circle.getPathLength(0.0, 36.0));
	System.out.println("length for u = 0.5 to 35.5 = "
			   + circle.getPathLength(0.5, 35.5));
	System.out.println("length for u = 0.0 to 54.0 = "
			   + circle.getPathLength(0.0, 54.0));
	System.out.println("length for u = 0.5 to 53.5 = "
			   + circle.getPathLength(0.5, 53.5));
	// try a million random values of u1 and u2 and make sure
	// we are not too far off from what we'd get for an exact
	// circle.
	Random random = new Random();
	for (int i = 0; i < 1000000; i++) {
	    double u1 = 180 * random.nextGaussian();
	    double u2 = 180 * random.nextGaussian();
	    double len = circle.getDistance(u1, u2);
	    circle.setAccuracyMode(true);
	    double len2 = circle.getDistance(u1, u2);
	    circle.setAccuracyMode(false);

	    if (Math.abs(len-len2)/Math.max(1.0, Math.abs(len)) > 1.e-8) {
		throw new Exception();
	    }

	    double estimated = (Math.PI / 9.0) * (u2 - u1) * 100;
	    double limit = (estimated == 0.0)? 1.0e-3:
		Math.abs(estimated) * 1.0e-3;
	    if (Math.abs(len - estimated) >  limit) {
		System.out.println(len + " != " + estimated
				   + " for " + u2 + " - " + u1);
		System.exit(1);
	    }
	}
	
	{
	    double u = -1000/100.0;
	    double s = circle.s(u);
	    double uu = circle.u(s);
	    System.out.println("u = " + u + ", s = " + s +", uu = " + uu);


	    
	    u = -10.0001;
	    double s1 = circle.s(u);
	    uu = circle.u(s1);
	    System.out.println("u = " + u + ", s = " + s1 +", uu = " + uu);

	    u = -10.0002;
	    double s2 = circle.s(u);
	    uu = circle.u(s2);
	    System.out.println("u = " + u + ", s = " + s2 +", uu = " + uu);
	}
	// see if the s and u methods are consistent.
	for (int i = -5400; i <= 5400; i++) {
	    double u = i/100.0;
	    double s = circle.s(u);
	    double delta = Math.abs(u - circle.u(s));
	    if (delta > 1.e-7) {
		System.out.println("i = " + i + ", delta = " + delta
				   + ", s = " + s
				   + ", circle.u(s) = " + circle.u(s));
	    }
	    circle.setAccuracyMode(true);
	    double s2 = circle.s(u);
	    if (Math.abs(s-s2)/s > 1.e-8) {
		System.out.println("s = " +s);
		System.out.println("s2 = " +s2);
		throw new Exception();
	    }
	    double delta2 = Math.abs(u - circle.u(s2));
	    if (delta2 > 1.e-7) {
		System.out.println("s = " + s);
		System.out.println("s2 = " + s2);
		System.out.println("u = " + u);
		System.out.println("circle.u(s2) = " + circle.u(s2));
		System.out.println("delta = " + delta);
		throw new Exception();
	    }
	    circle.setAccuracyMode(false);
	}

	System.out.println("circle curvature = " + circle.curvature(0.0));
	System.out.println("circle2 curvature = " + circle2.curvature(0.0));
			   

	System.out.println("Check radius of curvature:");
	for (int i = 0; i <= 180; i++) {
	    double xk = circle.curvature(i/10.0);
	    if (Math.abs(xk - 0.01) > 2.0e-2) {
		System.out.println("wrong radius of curvature: " + (1.0/xk));
		throw new Exception
		    ("radius of curvature should be 100\u00b1\u03b4");
	    }
	}
	System.out.println("... OK");


	System.out.println("---- try non-cyclic case ----");
	
	graph = new Graph();
	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0, -100.0, 100.0);
	g2d = graph.createGraphics();
	g2d.setColor(Color.RED);
	xc = new double[10];
	yc = new double[10];
	for (int i = 0; i < 10; i++) {
	    double theta = Math.toRadians((double)20.0 * i);
	    double r = 100.0;
	    double x  = r * Math.cos(theta);
	    double y = r * Math.sin(theta);
	    xc[i] = x;
	    yc[i] = y;
	    graph.draw(g2d, symbol, x, y);
	}
	BasicSplinePath2D halfCircle = new BasicSplinePath2D(xc, yc, false);

	g2d.setColor(Color.BLACK);
	graph.draw(g2d, halfCircle);
	g2d.setColor(Color.RED);

	for (int i = 0; i < 10; i++) {
	    for (double delta = 0.0; delta < 0.99; delta += 0.5) {
		double theta = Math.toRadians((double)20.0 * i);
		double r = 100.0;
		double u = i + delta;
		if (u - 9.5 > -1.e-10)  continue;
		double x = halfCircle.getX(u);
		double y = halfCircle.getY(u);
		Point2D point = halfCircle.getPoint(u);
		if (point.getX() != x || point.getY() != y) {
		    throw new Exception
			("getPoint, getX, and getY don't agree");
		}
		double radius = Math.sqrt(x*x + y*y);
		double angle = Math.toDegrees(Math.atan2(y, x));
		if (angle < 0) angle = 360 + angle;

		/*
		  System.out.println("u = " + u + ", x = " + x
		  + ", y = " + y
		  + ", r = " + radius
		  + ", angle = " + angle);
		*/
		graph.draw(g2d, symbol, x, y);
		graph.fill(g2d, symbol, x, y);
	    }
	}
	graph.write("png", "sptest2.png");

	System.out.println("Segment 2 length = "
			   + halfCircle.getPathLength(2.0, 3.0));

	System.out.println("Length for u = 7.5 to 7.6: "
			   + halfCircle.getPathLength(7.5, 7.6));

	System.out.println("Length for u = 5.5 to 7.5: "
			   + halfCircle.getPathLength(5.5, 7.5));

	System.out.println("Segment 5 length = "
			   + halfCircle.getPathLength(5.0, 6.0));

	System.out.println("Segment 6 length = "
			   + halfCircle.getPathLength(6.0, 7.0));

	System.out.println("Segment 5 + Segment 6 length = "
			   + halfCircle.getPathLength(5.0, 7.0));

	System.out.println("Total length = " + halfCircle.getPathLength());
	System.out.println("length for u = 0.0 to 9.0 = "
			   + halfCircle.getPathLength(0.0, 9.0));

	// try a million random values of u1 and u2 and make sure
	// we are not too far off from what we'd get for an exact
	// circle.
	for (int i = 0; i < 1000000; i++) {
	    double u1 = 9.0 * random.nextDouble();
	    double u2 = 9.0 * random.nextDouble();
	    double len = halfCircle.getDistance(u1, u2);
	    double estimated = (Math.PI / 9.0) * (u2 - u1) * 100;
	    double limit = (estimated == 0.0)? 5.0e-2:
		Math.abs(estimated) * 5.0e-2;
	    if (Math.abs(len - estimated) >  limit) {
		System.out.println(len + " != " + estimated
				   + " for " + u2 + " - " + u1
				   + ", i = " + i);
		System.exit(1);
	    }
	}
	// check s and u methods
	for (int i = 0; i <= 900; i++) {
	    double u = i/100.0;
	    if (i == 900) u = 9.0;
	    double s = halfCircle.s(u);
	    try {
		double delta = Math.abs(u - halfCircle.u(s));
		if (delta > 1.0e-7) {
		    System.out.println("i = " + i + ", delta = " + delta
				       + ", s = " + s
				       + ", circle.u(s) = " + circle.u(s));
		}
	    } catch (Exception e) {
		System.out.println("exception when u = " + u +", i = " + i
				   + ", s = halfCircle.s(u) = " + s);
		throw e;
	    }
	}
	circle.clear();
	circle.addCycle(xc,yc);
	System.out.println("print table after circle reconstructed");
	circle.printTable(System.out);

	// try a million random values of u1 and u2 and make sure
	// we are not too far off from what we'd get for an exact
	// circle.
	for (int i = 0; i < 1000000; i++) {
	    double u1 = 9.0 * random.nextDouble();
	    double u2 = 9.0 * random.nextDouble();
	    double len = halfCircle.getDistance(u1, u2);
	    double estimated = (Math.PI / 9.0) * (u2 - u1) * 100;
	    double limit = (estimated == 0.0)? 5.0e-2:
		Math.abs(estimated) * 5.0e-2;
	    if (Math.abs(len - estimated) >  limit) {
		System.out.println(len + " != " + estimated
				   + " for " + u2 + " - " + u1
				   + ", i = " + i);
		System.exit(1);
	    }
	}
	// check s and u methods
	for (int i = 0; i <= 900; i++) {
	    double u = i/100.0;
	    double s = halfCircle.s(u);
	    double delta = Math.abs(u - halfCircle.u(s));
	    if (delta > 1.0e-7) {
		System.out.println("i = " + i + ", delta = " + delta
				   + ", s = " + s
				   + ", circle.u(s) = " + circle.u(s));
	    }
	}

	// check initialization code to make sure everything is
	// set up no matter which method we call first.
	System.out.println("testing inits - line from (0,0) to (10,10)");
			   
	BasicSplinePath2D path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.curvature(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2sDu2(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2xDu2(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2yDu2(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dsDu(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dxDu(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dyDu(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getDistance(0.5, 0.7);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getMaxParameter();

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getPathLength();

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getPathLength(0.5, 0.7);


	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getPoint(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getX(0.5);
	
	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getY(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.u(0.5);

	path = new BasicSplinePath2D();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.s(0.5);

	// Now repeat, clearing the path instead of creating a new one.
	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.curvature(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2sDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2xDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.d2yDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dsDu(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dxDu(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.dyDu(0.5);

	System.out.println("(will print entries for a few cases)");
	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(30.0, 40.0);
	System.out.println("*** configure a case for a line of length 50");
	System.out.println("distance from 0.5 to 0.7 = "
			   + path.getDistance(0.5, 0.7));
	System.out.println("... distance from 0.0 to 1.0 = "
			   + path.getDistance(0.0, 1.0));
	System.out.println("... path length from 0.0 to 1.0 = "
			   + path.getPathLength(0.0, 1.0));
	System.out.println("... total path length = " + path.getPathLength());
	if (path.getPathLength() == 0.0) {
	    System.out.println("...............");
	    path.printTable();
	    System.out.println("...............");
	}

	System.out.println("*** configure cases for a line (0,0)--->(10,10)");
	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	System.out.println("max path parameter = " + path.getMaxParameter());

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	System.out.println("path length = " + path.getPathLength());

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getPathLength(0.5, 0.7);


	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getPoint(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getX(0.5);
	
	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.getY(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.u(0.5);

	path.clear();
	path.moveTo(0.0, 0.0);
	path.lineTo(10.0, 10.0);
	path.s(0.5);

	System.exit(0);
    }
}
