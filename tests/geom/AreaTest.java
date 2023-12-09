import org.bzdev.geom.*;
import java.awt.*;
import java.awt.geom.*;


public class AreaTest {
    public static void main(String argv[]) throws Exception {
	SplinePath2D cpath0 = new SplinePath2D();
	SplinePath2D cpath1 = new SplinePath2D();
	SplinePath2D cpath2 = new SplinePath2D();
	SplinePath2D cpath3 = new SplinePath2D();
	double xs[] = new double[36];
	double ys[] = new double[36];
	double xsInner2[] = new double[36];
	double ysInner2[] = new double[36];
	double xsInner3[] = new double[36];
	double ysInner3[] = new double[36];
	double r = 10.0;
	for (int i = 0; i < 36; i++) {
	    double theta = Math.toRadians(i * 10.0);
	    xs[i] = r * Math.cos(theta);
	    ys[i] = r * Math.sin(theta);
	    xsInner2[i] = -xs[i]/2.0;
	    ysInner2[i] = ys[i]/2.0;
	    xsInner3[i] = xs[i]/2.0;
	    ysInner3[i] = ys[i]/2.0;
	}
	cpath1.addCycle(xs, ys, 36);
	cpath2.addCycle(xs, ys, 36);
	cpath2.addCycle(xsInner2, ysInner2, 36);
	cpath3.addCycle(xs, ys, 36);
	cpath3.addCycle(xsInner3, ysInner3, 36);

	System.out.println("capth0 circumference = "
			   + Path2DInfo.circumferenceOf(cpath0)
			   + ", should be 0 because the path is empty");

	System.out.println("capth0 area = " + Path2DInfo.areaOf(cpath0)
			   + ", should be 0 because the path is empty");

	System.out.println("cpath1 circumference = "
			   + Path2DInfo.circumferenceOf(cpath1)
			   + ", should be " + (2 * Math.PI * r));

	System.out.println("cpath1 area = " + Path2DInfo.areaOf(cpath1)
			   + ", should be " + (Math.PI * r * r));

	System.out.println("cpath2 circumference = "
			   + Path2DInfo.circumferenceOf(cpath2)
			   + ", should be " + (2 * Math.PI * 1.5 * r));

	System.out.println("cpath2 area = " + Path2DInfo.areaOf(cpath2)
			   + ", should be " + (Math.PI * r * r *(1-1.0/4)));


	System.out.println("cpath3 circumference = "
			   + Path2DInfo.circumferenceOf(cpath3)
			   + ", should be " + (2 * Math.PI * r));

	System.out.println("cpath3 area = " + Path2DInfo.areaOf(cpath3)
			   + ", should be " + (Math.PI * r * r ));

	Path2D path4 = new Path2D.Double();
	path4.moveTo(0.0, 10.0);
	path4.lineTo(0.0, -10.0);
	path4.lineTo(10.0, -10.0);
	path4.lineTo(10.0, 10.0);
	path4.lineTo(0.0, 10.0);
	path4.closePath();
	path4.moveTo(0.0, 10.0);
	path4.lineTo(-10.0, 10.0);
	path4.lineTo(-10.0, -10.0);
	path4.lineTo(0.0, -10.0);
	path4.lineTo(0.0, 10.0);
	path4.closePath();

	System.out.println("path4 circumference = "
			   + Path2DInfo.circumferenceOf(path4)
			   + ", should be " + 80.0);

	System.out.println("path4 area = " + Path2DInfo.areaOf(path4)
			   + ", should be " + 400.0);

	Path2D path5 = new Path2D.Double();
	path5.moveTo(0.0, 0.0);
	path5.lineTo(10.0, 10.0);
	path5.lineTo(10.0, -10.0);
	path5.lineTo(0.0, 0.0);
	path5.closePath();
	path5.lineTo(-10.0, 10.0);
	path5.lineTo(-10.0, -10.0);
	path5.lineTo(0.0, 0.0);
	path5.closePath();

	System.out.println("path5 circumference = "
			   + Path2DInfo.circumferenceOf(path5)
			   + ", should be "
			   +(40.0 + 40.0 * Math.sqrt(2.0)));

	System.out.println("path5 area = " + Path2DInfo.areaOf(path5)
			   + ", should be " + 200.0);

	Path2D path6 = new Path2D.Double();
	path6.moveTo(0.0, 10.0);
	path6.lineTo(10.0, 10.0);
	path6.lineTo(10.0, -10.0);
	path6.lineTo(0.0, -10.0);
	path6.lineTo(0.0, 10.0);
	path6.closePath();
	path6.moveTo(5.0, 5.0);
	path6.lineTo(-5.0, 5.0);
	path6.lineTo(-5.0, -5.0);
	path6.lineTo(5.0, -5.0);
	path6.lineTo(5.0, 5.0);
	path6.closePath();
	
	System.out.println("path6 circumference = "
			   + Path2DInfo.circumferenceOf(path6)
			   + ", should be " + (70.0 + 30.0));

	System.out.println("path6 area = " + Path2DInfo.areaOf(path6)
			   + ", should be " + 200.0);
	

	Path2D path7 = new Path2D.Double();
	double x = 0.0;
	double y = 0.0;
	path7.moveTo(x-5.0, y+5.0);
	path7.lineTo(x+5.0, y+5.0);
	path7.lineTo(x+5.0, y-5.0);
	path7.lineTo(x-5.0, y-5.0);
	path7.lineTo(x-5.0, y+5.0);
	path7.closePath();
	x = 0.0; y = 10.0;
	path7.moveTo(x-5.0, y+5.0);
	path7.lineTo(x+5.0, y+5.0);
	path7.lineTo(x+5.0, y-5.0);
	path7.lineTo(x-5.0, y-5.0);
	path7.lineTo(x-5.0, y+5.0);
	path7.closePath();
	x = 0.0; y = -10.0;
	path7.moveTo(x-5.0, y+5.0);
	path7.lineTo(x+5.0, y+5.0);
	path7.lineTo(x+5.0, y-5.0);
	path7.lineTo(x-5.0, y-5.0);
	path7.lineTo(x-5.0, y+5.0);
	path7.closePath();
	x = -10.0; y = 0.0;
	path7.moveTo(x-5.0, y+5.0);
	path7.lineTo(x+5.0, y+5.0);
	path7.lineTo(x+5.0, y-5.0);
	path7.lineTo(x-5.0, y-5.0);
	path7.lineTo(x-5.0, y+5.0);
	path7.closePath();
	x = 10.0; y = 0.0;
	path7.moveTo(x-5.0, y+5.0);
	path7.lineTo(x+5.0, y+5.0);
	path7.lineTo(x+5.0, y-5.0);
	path7.lineTo(x-5.0, y-5.0);
	path7.lineTo(x-5.0, y+5.0);
	path7.closePath();
	path7.moveTo(-5.0, 5.0);
	path7.lineTo(-5.0, -5.0);
	path7.lineTo(5.0, -5.0);
	path7.lineTo(5.0, 5.0);
	path7.lineTo(-5.0, 5.0);
	path7.closePath();

	System.out.println("path7 circumference = "
			   + Path2DInfo.circumferenceOf(path7)
			   + ", should be " + 160.0);

	System.out.println("path7 length = " + Path2DInfo.pathLength(path7));

	System.out.println("path7 area = " + Path2DInfo.areaOf(path7)
			   + ", should be " + 400.0);

	Path2D path8 = new Path2D.Double();
	x = 0.0;
	y = 0.0;
	path8.moveTo(x-5.0, y+5.0);
	path8.lineTo(x+5.0, y+5.0);
	path8.lineTo(x+5.0, y-5.0);
	path8.lineTo(x-5.0, y-5.0);
	path8.lineTo(x-5.0, y+5.0);
	path8.closePath();
	x = 0.0; y = 10.0;
	path8.moveTo(x-5.0, y+5.0);
	path8.lineTo(x+5.0, y+5.0);
	path8.lineTo(x+5.0, y-5.0);
	path8.lineTo(x-5.0, y-5.0);
	path8.lineTo(x-5.0, y+5.0);
	path8.closePath();
	x = 0.0; y = -10.0;
	path8.moveTo(x-5.0, y+5.0);
	path8.lineTo(x+5.0, y+5.0);
	path8.lineTo(x+5.0, y-5.0);
	path8.lineTo(x-5.0, y-5.0);
	path8.lineTo(x-5.0, y+5.0);
	path8.closePath();
	x = -10.0; y = 0.0;
	path8.moveTo(x-5.0, y+5.0);
	path8.lineTo(x+5.0, y+5.0);
	path8.lineTo(x+5.0, y-5.0);
	path8.lineTo(x-5.0, y-5.0);
	path8.lineTo(x-5.0, y+5.0);
	path8.closePath();
	x = 10.0; y = 0.0;
	path8.moveTo(x-5.0, y+5.0);
	path8.lineTo(x+5.0, y+5.0);
	path8.lineTo(x+5.0, y-5.0);
	path8.lineTo(x-5.0, y-5.0);
	path8.lineTo(x-5.0, y+5.0);
	path8.closePath();

	System.out.println("path8 circumference = "
			   + Path2DInfo.circumferenceOf(path8)
			   + ", should be " + 120.0);

	System.out.println("path8 length = " + Path2DInfo.pathLength(path8));

	System.out.println("path8 area = " + Path2DInfo.areaOf(path8)
			   + ", should be " + 500.0);

	Path2D path9 = new Path2D.Double();
	path9.moveTo(-10.0, +10.0);
	path9.lineTo(10.0, +10.0);
	path9.lineTo(10.0, -10.0);
	path9.lineTo(-10.0, -10.0);
	path9.lineTo(-10.0, +10.0);
	path9.closePath();
	path9.moveTo(-20.0, 5.0);
	path9.lineTo(-10.0, 5.0);
	path9.lineTo(-10.0, -5.0);
	path9.lineTo(-20.0, -5.0);
	path9.lineTo(-20.0, 5.0);
	path9.closePath();
	
	System.out.println("path9 circumference = "
			   + Path2DInfo.circumferenceOf(path9)
			   + ", should be " + 100.0);

	System.out.println("path9 length = " + Path2DInfo.pathLength(path9));

	System.out.println("path9 area = " + Path2DInfo.areaOf(path9)
			   + ", should be " + 500.0);

	Path2D path10 = new Path2D.Double();
	/*
	x = 0.0;
	y = 0.0;
	path10.moveTo(x-5.0, y+5.0);
	path10.lineTo(x+5.0, y+5.0);
	path10.lineTo(x+5.0, y-5.0);
	path10.lineTo(x-5.0, y-5.0);
	path10.lineTo(x-5.0, y+5.0);
	path10.closePath();
	*/
	x = 0.0; y = 10.0;
	path10.moveTo(x-5.0, y+5.0);
	path10.lineTo(x+5.0, y+5.0);
	path10.lineTo(x+5.0, y-5.0);
	path10.lineTo(x-5.0, y-5.0);
	path10.lineTo(x-5.0, y+5.0);
	path10.closePath();
	x = 0.0; y = -10.0;
	path10.moveTo(x-5.0, y+5.0);
	path10.lineTo(x+5.0, y+5.0);
	path10.lineTo(x+5.0, y-5.0);
	path10.lineTo(x-5.0, y-5.0);
	path10.lineTo(x-5.0, y+5.0);
	path10.closePath();
	x = -10.0; y = 0.0;
	path10.moveTo(x-5.0, y+5.0);
	path10.lineTo(x+5.0, y+5.0);
	path10.lineTo(x+5.0, y-5.0);
	path10.lineTo(x-5.0, y-5.0);
	path10.lineTo(x-5.0, y+5.0);
	path10.closePath();
	x = 10.0; y = 0.0;
	path10.moveTo(x-5.0, y+5.0);
	path10.lineTo(x+5.0, y+5.0);
	path10.lineTo(x+5.0, y-5.0);
	path10.lineTo(x-5.0, y-5.0);
	path10.lineTo(x-5.0, y+5.0);
	path10.closePath();
	x = 0.0; y = 0.0;
	double epsilon = 0.000000001;
	path10.moveTo(x-5.0+epsilon, y+5.0-epsilon);
	path10.lineTo(x+5.0-epsilon, y+5.0-epsilon);
	path10.lineTo(x+5.0-epsilon, y-5.0+epsilon);
	path10.lineTo(x-5.0+epsilon, y-5.0+epsilon);
	path10.lineTo(x-5.0+epsilon, y+5.0-epsilon);
	path10.closePath();

	System.out.println("path10 circumference = "
			   + Path2DInfo.circumferenceOf(path10)
			   + ", should be " + (160.0 + 40 *(1-2*epsilon)));

	System.out.println("path10 length = " + Path2DInfo.pathLength(path10));

	double side = 10.0- 2.0 * epsilon;
	System.out.println("path10 area = " + Path2DInfo.areaOf(path10)
			   + ", should be " + (400.0 + side*side));

	Path2D outline = new Path2D.Float(new Area(path10));
	
	Path2DInfo.printSegments(outline);

	System.out.println();

	Ellipse2D circle = new Ellipse2D.Double(0.0, 0.0, 2.0, 2.0);
	System.out.println("For a circle of unit radius:");
	System.out.println("    circumference = "
			   + Path2DInfo.circumferenceOf(circle)
			   + " (ideal value = " + (2.0 * Math.PI) + ")");
	System.out.println("    area = " + Path2DInfo.areaOf(circle)
			   + " (ideal value = " + Math.PI + ")");

	SplinePath2D hrcircle = new SplinePath2D();
	double[] xshr = new double[360];
	double[] yshr = new double[360];
	for (int i = 0; i < 360; i++) {
	    double theta = Math.toRadians(i * 1.0);
	    xshr[i] = r * Math.cos(theta);
	    yshr[i] = r * Math.sin(theta);
	}
	hrcircle.addCycle(xshr, yshr, 360);
	System.out.println("high-res circle circumference = "
			   + Path2DInfo.circumferenceOf(hrcircle)
			   + ", expecting " + (Math.PI*2.0*r)
			   + " for perfect circle");
	System.out.println("high-res circle area = "
			   + Path2DInfo.areaOf(hrcircle)
			   + ", expecting " + (Math.PI * r * r)
			   + " for perfect circle");

	System.out.println("high-res circle area (using xdy-ydx) = "
			   + Path2DInfo.areaOf(hrcircle.getPathIterator(null)));
	System.out.println("high-res circle area (using xdy) = "
			   + Path2DInfo.areaOf(hrcircle.getPathIterator(null),
					       true));
	System.out.println("high-res circle area (using -ydx) = "
			   + Path2DInfo.areaOf(hrcircle.getPathIterator(null),
					       true));

	SplinePath2D crossedLoop = new SplinePath2D();
	for (int i = 0; i < 360; i++) {
	    double theta = Math.toRadians(i * 1.0);
	    xshr[i] = r * Math.cos(theta);
	    yshr[i] = r * Math.sin(2*theta);
	}
	crossedLoop.addCycle(xshr, yshr, 360);
	double crossedLoopArea = Path2DInfo.areaOf(crossedLoop);
	System.out.println("loop area = " + crossedLoopArea);
	if (Math.abs(crossedLoopArea) < 1.e-5) {
	    throw new Exception();
	}

	double cla2 = Path2DInfo.areaOf(crossedLoop.getPathIterator(null));
	if (Math.abs(cla2) > 1.e-5) {
	    throw new Exception();
	}

	Path2D narrowRect = new Path2D.Double();

	narrowRect.moveTo(1.0, 0.0);
	narrowRect.lineTo(11.0, 0.0);
	narrowRect.lineTo(11.0, 1.e-20); 
	narrowRect.lineTo(1.0, 1.e-20);
	narrowRect.lineTo(1.0, 0.0);
	narrowRect.closePath();
	System.out.println("narrowRect area (xdy-ydx) = " +
			   Path2DInfo.areaOf(narrowRect.getPathIterator(null)));
	System.out.println("narrowRect area (xdy) = " +
			   Path2DInfo.areaOf(narrowRect.getPathIterator(null),
					     true));
	System.out.println("narrowRect area (-ydx) = " +
			   Path2DInfo.areaOf(narrowRect.getPathIterator(null),
					     false));
	Path2D genpath = new Path2D.Double();

	genpath.moveTo(1.0, 1.0);
	genpath.lineTo(2.0, 3.0);
	genpath.quadTo(4.0, 6.0, 7.0, 8.0);
	genpath.curveTo(7.0, 9.0, 4.0, 8.5, 2.0, 5.0);
	genpath.closePath();
	System.out.println("genpath area (xdy-ydx) = " +
			   Path2DInfo.areaOf(genpath.getPathIterator(null)));
	System.out.println("genpath area (xdy) = " +
			   Path2DInfo.areaOf(genpath.getPathIterator(null),
					     true));
	System.out.println("genpath area (-ydx) = " +
			   Path2DInfo.areaOf(genpath.getPathIterator(null),
					     false));


	System.exit(0);
    }
}
