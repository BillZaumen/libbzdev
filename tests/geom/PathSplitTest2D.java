import org.bzdev.geom.*;
import org.bzdev.math.*;
import org.bzdev.graphs.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.PrintWriter;

public class PathSplitTest2D {

    // Based on equations in https://pomax.github.io/bezierinfo/#splitting
    // These give the results in closed form for a cubic Bezier curve, so
    // we can use this as a cross check for the Path2DInfo method.
    static Point2D split1(double[] result, double z, double x, double y,
			  double[] coords)
    {
	result[0] = z*coords[0] - (z-1)*x;
	result[1] = z*coords[1] - (z-1)*y;
	result[2] = z*z*coords[2]-2*z*(z-1)*coords[0] + (z-1)*(z-1)*x;
	result[3] = z*z*coords[3]-2*z*(z-1)*coords[1] + (z-1)*(z-1)*y;
	result[4] = z*z*z*coords[4] - 3*z*z*(z-1)*coords[2]
	    + 3*z*(z-1)*(z-1)*coords[0] - (z-1)*(z-1)*(z-1)*x;
	result[5] = z*z*z*coords[5] - 3*z*z*(z-1)*coords[3]
	    + 3*z*(z-1)*(z-1)*coords[1] - (z-1)*(z-1)*(z-1)*y;

	return new Point2D.Double(x, y);
    }

    static Point2D split2(double[] result, double z, double x, double y,
			  double[] coords)
    {
	result[0] = z*z*coords[4]-2*z*(z-1)*coords[2] + (z-1)*(z-1)*coords[0];
	result[1] = z*z*coords[5]-2*z*(z-1)*coords[3] + (z-1)*(z-1)*coords[1];
	result[2] = z*coords[4] - (z-1)*coords[2];
	result[3] = z*coords[5] - (z-1)*coords[3];
	result[4] = coords[4];
	result[5] = coords[5];
	return new Point2D.Double(z*z*z*coords[4] - 3*z*z*(z-1)*coords[2]
				  + 3*z*(z-1)*(z-1)*coords[0]
				  - (z-1)*(z-1)*(z-1)*x,
				  z*z*z*coords[5] - 3*z*z*(z-1)*coords[3]
				  + 3*z*(z-1)*(z-1)*coords[1]
				  - (z-1)*(z-1)*(z-1)*y);
    }

    public static void main(String argv[]) throws Exception {
	Path2D path = new Path2D.Double();
	path.moveTo(-8.0, 0.0);
	path.lineTo(-8.0, -4.0);
	path.lineTo(0.0, -4.0);
	path.quadTo(8.0, -4.0, 8.0, 0.0);
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0);
	path.closePath();
	path.lineTo(-7.0, 0.0);
	path.lineTo(-7.0, 1.0);
	path.closePath();
	path.moveTo(10.0, 10.0);
	path.lineTo(11.0, 11.0);
	path.moveTo(20.0, 20.0);
	path.lineTo(30.0, 40.0);
	path.lineTo(40.0, 40.0);
	path.closePath();

	Path2D[] subpaths = PathSplitter.split(path);

	PathIterator pit = path.getPathIterator(null);
	double[] coords = new double[6];
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		break;
	    }
	    pit.next();
	}

	for (int j = 0; j < subpaths.length; j++) {
	    System.out.println("SUBPATH " + j + ":");
	    pit = subpaths[j].getPathIterator(null);
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		switch (type) {
		case PathIterator.SEG_MOVETO:
		    System.out.println("MOVETO (" + coords[0]
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_LINETO:
		    System.out.println("LINETO (" + coords[0]
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_QUADTO:
		    System.out.println("QUADTO:");
		    for (int i = 0; i < 4; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.out.println("CUBICTO:");
		    for (int i = 0; i < 6; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CLOSE:
		    System.out.println("CLOSE PATH");
		    break;
		}
		pit.next();
	    }
	}

	System.out.println("------ Path2D.Float case ----------");

	path = new Path2D.Float();
	path.moveTo(-8.0, 0.0);
	path.lineTo(-8.0, -4.0);
	path.lineTo(0.0, -4.0);
	path.quadTo(8.0, -4.0, 8.0, 0.0);
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0);
	path.closePath();
	path.lineTo(-7.0, 0.0);
	path.lineTo(-7.0, 1.0);
	path.closePath();
	path.moveTo(10.0, 10.0);
	path.lineTo(11.0, 11.0);
	path.moveTo(20.0, 20.0);
	path.lineTo(30.0, 40.0);
	path.lineTo(40.0, 40.0);
	path.closePath();

	subpaths = PathSplitter.split(path);

	pit = path.getPathIterator(null);
	coords = new double[6];
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		break;
	    }
	    pit.next();
	}

	for (int j = 0; j < subpaths.length; j++) {
	    System.out.println("SUBPATH " + j + ":");
	    pit = subpaths[j].getPathIterator(null);
	    while (!pit.isDone()) {
		int type = pit.currentSegment(coords);
		switch (type) {
		case PathIterator.SEG_MOVETO:
		    System.out.println("MOVETO (" + coords[0]
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_LINETO:
		    System.out.println("LINETO (" + coords[0]
				       + ", " + coords[1] + ")");
		    break;
		case PathIterator.SEG_QUADTO:
		    System.out.println("QUADTO:");
		    for (int i = 0; i < 4; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CUBICTO:
		    System.out.println("CUBICTO:");
		    for (int i = 0; i < 6; i += 2)
			System.out.println("    ... control point ("
					   + coords[i] + ", " + coords[i+1]
					   + ")");
		    break;
		case PathIterator.SEG_CLOSE:
		    System.out.println("CLOSE PATH");
		    break;
		}
		pit.next();
	    }
	}

	double startX = 44.0;
	double startY = 55.0;
	coords[0] = 50.0;
	coords[1] = 60.0;
	coords[2] = 90.0;
	coords[3] = 100.0;
	coords[4] = 105.0;
	coords[5] = 115.0;
	double[] scoords = new double[12];

	PathSplitter.split(PathIterator.SEG_QUADTO,
			   startX, startY, coords, 0, scoords, 0, 0.4);

	double u = 0.25;

	double ex = Path2DInfo.getX(u, startX, startY,
				    PathIterator.SEG_QUADTO, coords);
	double ey = Path2DInfo.getY(u, startX, startY,
				    PathIterator.SEG_QUADTO, coords);

	u = 0.625;

	double x = Path2DInfo.getX(u, startX, startY,
				   PathIterator.SEG_QUADTO, scoords);
	double y = Path2DInfo.getY(u, startX, startY,
				   PathIterator.SEG_QUADTO, scoords);

	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	double sx = scoords[2];
	double sy = scoords[3];
	System.arraycopy(scoords, 4, scoords, 0, 4);

	u = 0.75;

	ex = Path2DInfo.getX(u, startX, startY,
			     PathIterator.SEG_QUADTO, coords);
	ey = Path2DInfo.getY(u, startX, startY,
			     PathIterator.SEG_QUADTO, coords);

	u = (0.75-0.4)/0.6;

	x = Path2DInfo.getX(u, sx, sy, PathIterator.SEG_QUADTO, scoords);
	y = Path2DInfo.getY(u, sx, sy, PathIterator.SEG_QUADTO, scoords);

	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	PathSplitter.split(PathIterator.SEG_CUBICTO,
			   startX, startY, coords, 0, scoords, 0, 0.4);

	u = 0.25;

	ex = Path2DInfo.getX(u, startX, startY,
			     PathIterator.SEG_CUBICTO, coords);
	ey = Path2DInfo.getY(u, startX, startY,
			     PathIterator.SEG_CUBICTO, coords);

	u = 0.625;

	x = Path2DInfo.getX(u, startX, startY,
			    PathIterator.SEG_CUBICTO, scoords);
	y = Path2DInfo.getY(u, startX, startY,
			    PathIterator.SEG_CUBICTO, scoords);

	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	sx = scoords[4];
	sy = scoords[5];
	System.arraycopy(scoords, 6, scoords, 0, 6);

	u = 0.75;

	ex = Path2DInfo.getX(u, startX, startY,
			     PathIterator.SEG_CUBICTO, coords);
	ey = Path2DInfo.getY(u, startX, startY,
			     PathIterator.SEG_CUBICTO, coords);

	u = (0.75-0.4)/0.6;

	x = Path2DInfo.getX(u, sx, sy, PathIterator.SEG_CUBICTO, scoords);
	y = Path2DInfo.getY(u, sx, sy, PathIterator.SEG_CUBICTO, scoords);

	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	PathSplitter.split(PathIterator.SEG_LINETO,
			   startX, startY, coords, 0, scoords, 0, 0.4);

	u = 0.25;

	ex = Path2DInfo.getX(u, startX, startY,
			     PathIterator.SEG_LINETO, coords);
	ey = Path2DInfo.getY(u, startX, startY,
			     PathIterator.SEG_LINETO, coords);
	u = 0.625;

	x = Path2DInfo.getX(u, startX, startY,
			    PathIterator.SEG_LINETO, scoords);
	y = Path2DInfo.getY(u, startX, startY,
			    PathIterator.SEG_LINETO, scoords);
	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	sx = scoords[0];
	sy = scoords[1];
	System.arraycopy(scoords, 2, scoords, 0, 2);

	u = 0.75;

	ex = Path2DInfo.getX(u, startX, startY,
			     PathIterator.SEG_LINETO, coords);
	ey = Path2DInfo.getY(u, startX, startY,
			     PathIterator.SEG_LINETO, coords);

	u = (0.75-0.4)/0.6;

	x = Path2DInfo.getX(u, sx, sy, PathIterator.SEG_LINETO, scoords);
	y = Path2DInfo.getY(u, sx, sy, PathIterator.SEG_LINETO, scoords);

	if (Math.abs(ex - x) > 1.e-10 || Math.abs(ey - y) > 1.e-10) {
	    throw new Exception("split path does not match original path");
	}

	System.out.println(" --- test additional split methods ---");

	path = new Path2D.Double();
	path.moveTo(-8.0, 0.0); // u = 0
	path.lineTo(-8.0, -4.0); // u in (0,1]
	path.lineTo(0.0, -4.0); // u in (1,2]
	path.quadTo(8.0, -4.0, 8.0, 0.0); // u in (2, 3]
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0); // u in (3, 4]
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0); // u in (4, 5]
	path.closePath();			       // u in (5, 6]
	path.lineTo(-7.0, 0.0);
	path.lineTo(-7.0, 1.0);
	path.closePath();
	
	Graph graph = new Graph(600, 600);
	graph.setOffsets(50, 50);
	graph.setRanges(-10.0, 10.0, -10.0, 10.0);
	Graphics2D g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.setColor(Color.RED);
	graph.draw(g2d, path);

	int count = 0;
	Path2D fpath = new Path2D.Double();
	Path2D cpath;
	
	// Set to true to offset the green and red paths so we can
	// see of they would overlap each other.
	boolean shift = (argv.length > 1);
	AffineTransform ot = g2d.getTransform();


	g2d.setStroke(new BasicStroke(3.0F, BasicStroke.CAP_BUTT,
				      BasicStroke.JOIN_BEVEL));
	g2d.setColor(Color.BLACK);
	for (u = 0.0; u < 6.0; u += 0.2) {
	    System.out.format("u = %s\n", u);
	    cpath = PathSplitter.subpath(path, u, u+0.2);
	    graph.draw(g2d, cpath);
	    count++;
	    if (count%2 == 1) {
		g2d.setColor(Color.GREEN);
		if (shift) g2d.translate(5,15);
	    } else {
		g2d.setColor(Color.BLACK);
		if (shift) g2d.setTransform(ot);
	    }
	}
	graph.write("png", "pstest2d.png");


	graph = new Graph(600, 600);
	graph.setOffsets(50, 50);
	graph.setRanges(-10.0, 10.0, -10.0, 10.0);
	g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.setColor(Color.RED);
	graph.draw(g2d, path);

	// reference line 
	Path2D line = new Path2D.Double();
	line.moveTo(-9.0, 2.0);
	line.lineTo(-7.0, 2.0);
	graph.draw(g2d, line);
	    


	count = 0;

	ot = g2d.getTransform();


	g2d.setStroke(new BasicStroke(3.0F, BasicStroke.CAP_BUTT,
				      BasicStroke.JOIN_BEVEL));
	g2d.setColor(Color.BLACK);
	for (u = 0.0; u < 5.0; u += 0.8) {
	    System.out.format("u = %s\n", u);
	    double u2 = u + 0.8;
	    if (u2 > 5.0) u2 = 6.0;
	    cpath = PathSplitter.subpath(path, u, u2);
	    graph.draw(g2d, cpath);
	    count++;
	    if (count%2 == 1) {
		g2d.setColor(Color.GREEN);
		if (shift) g2d.translate(5,15);
	    } else {
		g2d.setColor(Color.BLACK);
		if (shift) g2d.setTransform(ot);
	    }
	}
	graph.write("png", "pstest2d1.png");


	System.out.println("--- randomly generated tests --- ");

	IntegerRandomVariable rv = new UniformIntegerRV(1,4);
	for (int k = 0; k < 100; k++) {
	    BasicSplinePath2D bpath = new BasicSplinePath2D();
	    bpath.moveTo(0.0, 0.0);
	    int cnt = 0;
	    for (int i = 1; i < 120; i++) {
		double r = i;
		double theta = Math.PI/10 * i;
		double r1, r2, theta1, theta2;
		switch (rv.next()) {
		case PathIterator.SEG_LINETO:
		    bpath.lineTo(r*Math.cos(theta), r*Math.sin(theta));
		    break;
		case PathIterator.SEG_QUADTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    bpath.quadTo(r*Math.cos(theta), r*Math.sin(theta),
				 r1*Math.cos(theta1), r1*Math.sin(theta1));
		case PathIterator.SEG_CUBICTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    i++;
		    r2 = i;
		    theta2 = Math.PI/10 * i;
		    bpath.curveTo(r*Math.cos(theta), r*Math.sin(theta),
				  r1*Math.cos(theta1), r1*Math.sin(theta1),
				  r2*Math.cos(theta2), r2*Math.sin(theta2));
		}
	    }
	    double umax = bpath.getMaxParameter();
	    int max = (int)(Math.round(umax) * 10);
	    IntegerRandomVariable urv = new UniformIntegerRV(0,true, max,true);
	    for (int i = 0; i < 1000; i++) {
		int ifactor = urv.next();
		double value1;
		if (ifactor % 10 == 0) {
		    value1 = (double)(ifactor/10);
		} else {
		    value1 = (ifactor / 10.0);
		}
		double value2;
		do {
		    ifactor = urv.next();
		    if (ifactor % 10 == 0) {
			value2 = (double)(ifactor/10);
		    } else {
			value2 = (ifactor / 10.0);
		    }
		} while (value1 == value2);
		double u1, u2;
		if (value1 < value2) {
		    u1 = value1;
		    u2 = value2;
		} else {
		    u1 = value2;
		    u2 = value1;
		}
		Path2D p = PathSplitter.subpath(bpath, u1, u2);
		Path2D pstart = (u1 > 0)? PathSplitter.subpath(bpath, 0.0, u1):
		    new Path2D.Double();
		Path2D pend = (u2 < umax)?
		    PathSplitter.subpath(bpath, u2, umax): new Path2D.Double();
		double olen = bpath.getPathLength(u1, u2);
		double plen = Path2DInfo.pathLength(p);
		double flen = Path2DInfo.pathLength(bpath);
		double slen = Path2DInfo.pathLength(pstart);
		double elen = Path2DInfo.pathLength(pend);
		double r = Math.abs((flen - (slen + plen + elen))/flen);
		if (r > 1.e-5) {
		    System.out.format("u1=%g, u2=%g, slen=%s, plen=%s, elen=%s,"
				      + "  flen = %s\n",
				      u1, u2, slen, plen, elen, flen);
		    throw new Exception(" r = " + r);
		}

		if (Math.abs((olen - plen)/olen) > 2.e-4) {
		    PrintWriter out = new PrintWriter("pstest.log", "UTF-8");
		    out.format("u1 = %s, u2 = %s\n", u1, u2);
		    out.format("X at u1 = %s\n", bpath.getX(u1));
		    out.format("Y at u1 = %s\n", bpath.getY(u1));
		    out.format("X at u2 = %s\n", bpath.getX(u2));
		    out.format("Y at u2 = %s\n", bpath.getY(u2));
		    out.println("PATH:");
		    Path2DInfo.printSegments("    ", out, bpath);
		    out.println("SPLIT PATH:");
		    Path2DInfo.printSegments("    ", out, p);
		    out.format("split path length = %s, path lenth = %s\n",
			       plen, olen);
		    out.flush();
		    out.close();
		    String msg = String.format("lengths: %s %s: %g\n",
					       plen, olen,
					       Math.abs((plen - olen)/olen));
		    throw new Exception(msg);
		}
	    }
	}

	System.out.println("--- check alternate method");

	sx = 5.0;
	sy = 6.0;
	double newarray[] = {
	    25.0, 30.0,
	    44.0, 50.0,
	    10.0, 15.0
	};
	scoords = newarray;

	double[] first = new double[6];
	double[] second = new double[6];
	double[] firstSplit = new double[6];
	double[] secondSplit = new double[6];
	double[] singleSplit = new double[6];

	for (int i = 1; i < 10; i++) {
	    double z = i/10.0;
	    for (int k = 0; k < 6;  k++) {
		// so we can see if the variable was actually set.
		first[k] = -1000.0;
		firstSplit[k] = -1000.0;
		second[k] = -1000.0;
		secondSplit[k] = -1000.0;
	    }

	    Point2D p = PathSplitter.split(first, second, z, 3,
					   sx, sy, scoords);
	    Point2D firstPoint = split1(firstSplit, z, sx, sy, scoords);
	    Point2D secondPoint = split2(secondSplit, z, sx, sy, scoords);

	    Point2D sp = PathSplitter.split(singleSplit, null, z, 3,
						    sx, sy, scoords);
	    for (int k = 0; k < 6; k++) {
		if (singleSplit[k] != first[k]) throw new Exception();
	    }
	    if (sp != null) throw new Exception();
	    sp = PathSplitter.split(null, singleSplit, z, 3,
				    sx, sy, scoords);
	    for (int k = 0; k < 6; k++) {
		if (singleSplit[k] != second[k]) throw new Exception();
	    }
	    if (!sp.equals(p)) throw new Exception();

	    if (Math.abs(sx-firstPoint.getX()) > 1.e-10
		|| Math.abs(sy - firstPoint.getY()) > 1.e-10
		|| Math.abs(first[4]-secondPoint.getX()) > 1.e-10
		|| Math.abs(first[5]-secondPoint.getY()) > 1.e-10) {
		System.out.println("z = " + z);
		System.out.format("first[4] = %g, secondPoint.getX() = %g\n",
				  first[4], secondPoint.getX());
		System.out.format("first[5] = %g, secondPoint.getY() = %g\n",
				  first[5], secondPoint.getY());
		throw new Exception("split failed");
	    }
	    for (int k = 0; k < 6; k++) {
		if (Math.abs(first[k] - firstSplit[k]) > 1.e-10
		    || Math.abs(second[k] - secondSplit[k]) > 1.e-10) {
		    System.out.println("z = " + z);
		    for (int kk = 0; kk < 6; kk++) {
			System.out.format
			    ("first[%d] = %g, firstSplit[%d] = %g\n",
			     kk, first[kk], kk, firstSplit[kk]);
		    }
		    for (int kk = 0; kk < 6; kk++) {
			System.out.format
			    ("second[%d] = %g, secondSplit[%d] = %g\n",
			     kk, second[kk], kk, secondSplit[kk]);
		    }
		    throw new Exception("split failed");
		}
	    }
	}

	System.out.println("--- randomly generated tests with flattening ---");

	for (int k = 0; k < 100; k++) {
	    BasicSplinePath2D bpath = new BasicSplinePath2D();
	    bpath.moveTo(0.0, 0.0);
	    int cnt = 0;
	    for (int i = 1; i < 120; i++) {
		double r = i;
		double theta = Math.PI/10 * i;
		double r1, r2, theta1, theta2;
		switch (rv.next()) {
		case PathIterator.SEG_LINETO:
		    bpath.lineTo(r*Math.cos(theta), r*Math.sin(theta));
		    break;
		case PathIterator.SEG_QUADTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    bpath.quadTo(r*Math.cos(theta), r*Math.sin(theta),
				 r1*Math.cos(theta1), r1*Math.sin(theta1));
		case PathIterator.SEG_CUBICTO:
		    i++;
		    r1 = i;
		    theta1 = Math.PI/10 * i;
		    i++;
		    r2 = i;
		    theta2 = Math.PI/10 * i;
		    bpath.curveTo(r*Math.cos(theta), r*Math.sin(theta),
				  r1*Math.cos(theta1), r1*Math.sin(theta1),
				  r2*Math.cos(theta2), r2*Math.sin(theta2));
		}
	    }
	    PathIterator fpit = new
		FlatteningPathIterator2D(bpath.getPathIterator(null), 1.0);
	    bpath = new BasicSplinePath2D();
	    bpath.append(fpit, false);	    
	    double umax = bpath.getMaxParameter();
	    int max = (int)(Math.round(umax) * 10);
	    IntegerRandomVariable urv = new UniformIntegerRV(0,true, max,true);
	    for (int i = 0; i < 1000; i++) {
		int ifactor = urv.next();
		double value1;
		if (ifactor % 10 == 0) {
		    value1 = (double)(ifactor/10);
		} else {
		    value1 = (ifactor / 10.0);
		}
		double value2;
		do {
		    ifactor = urv.next();
		    if (ifactor % 10 == 0) {
			value2 = (double)(ifactor/10);
		    } else {
			value2 = (ifactor / 10.0);
		    }
		} while (value1 == value2);
		double u1, u2;
		if (value1 < value2) {
		    u1 = value1;
		    u2 = value2;
		} else {
		    u1 = value2;
		    u2 = value1;
		}
		Path2D p = PathSplitter.subpath(bpath, u1, u2);
		double olen = bpath.getPathLength(u1, u2);
		double plen = Path2DInfo.pathLength(p);
		if (Math.abs((olen - plen)/olen) > 1.0e-5) {
		    PrintWriter out = new PrintWriter("pstest.log", "UTF-8");
		    out.format("u1 = %s, u2 = %s\n", u1, u2);
		    out.format("X at u1 = %s\n", bpath.getX(u1));
		    out.format("Y at u1 = %s\n", bpath.getY(u1));
		    out.format("X at u2 = %s\n", bpath.getX(u2));
		    out.format("Y at u2 = %s\n", bpath.getY(u2));
		    out.println("PATH:");
		    Path2DInfo.printSegments("    ", out, bpath);
		    out.println("SPLIT PATH:");
		    Path2DInfo.printSegments("    ", out, p);
		    out.format("split path length = %s, path lenth = %s\n",
			       plen, olen);
		    out.flush();
		    out.close();
		    String msg = String.format("lengths: %s %s: %g\n",
					       plen, olen,
					       Math.abs((plen - olen)/olen));
		    throw new Exception(msg);
		}
	    }
	}


	System.exit(0);
    }
}
