import org.bzdev.geom.*;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;
import java.util.Random;

public class BasicPath3DTest {
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
	graph.setRanges(0.0, 20, -100.0, 100.0);
	Graphics2D g2d = graph.createGraphics();

	double xc[] = new double[18];
	double yc[] = new double[18];
	double zc[] = new double[18];
	double ucc[] = new double[18+1];
	double xcc[] = new double[18+1];
	double ycc[] = new double[18+1];
	double zcc[] = new double[18+1];
	double zero[] = new double[18];
	double maxX = Double.NEGATIVE_INFINITY;
	double maxY = Double.NEGATIVE_INFINITY;
	double maxZ = Double.NEGATIVE_INFINITY;
	double minX = Double.POSITIVE_INFINITY;
	double minY = Double.POSITIVE_INFINITY;
	double minZ = Double.POSITIVE_INFINITY;
       	for (int i = 0; i < 18; i++) {
	    double theta = Math.toRadians((double)20.0 * i);
	    double r = 100.0;
	    double u = i;
	    double x  = r * Math.sin(theta) * 2.0;
	    double y = r * Math.sin(theta);
	    double z = y / 2.0;
	    xc[i] = x;
	    yc[i] = y;
	    zc[i] = z;
	    if (minX > x) minX = x;
	    if (minY > y) minY = y;
	    if (minZ > z) minZ = z;
	    if (maxX < x) maxX = x;
	    if (maxY < y) maxY = y;
	    if (maxZ < z) maxZ = z;
	    ucc[i] = u;
	    xcc[i] = x;
	    ycc[i] = y;
	    zcc[i] = z;
	    g2d.setColor(Color.RED);
	    graph.draw(g2d, symbol, u, x);
	    g2d.setColor(Color.GREEN);
	    graph.draw(g2d, symbol, u, y);
	    g2d.setColor(Color.BLUE);
	    graph.draw(g2d, symbol, u, z);
	}
	ucc[18] = 18.0;
	xcc[18] = xcc[0];
	ycc[18] = ycc[0];
	zcc[18] = zcc[0];

	BasicSplinePath3D path = new BasicSplinePath3D(xc, yc, zc, true);
	BasicSplinePath2D xpath = new BasicSplinePath2D(ucc, xcc, false);
	BasicSplinePath2D ypath = new BasicSplinePath2D(ucc, ycc, false);
	BasicSplinePath2D zpath = new BasicSplinePath2D(ucc, zcc, false);

	Rectangle3D br = path.getBounds();
	if (br.getMinX() > minX
	    || br.getMinY() > minY
	    || br.getMinZ() > minZ
	    || br.getMaxX() < maxX
	    || br.getMaxY() < maxY
	    || br.getMaxZ() < maxZ) {
	    System.out.println("... bad bounds");
	    System.out.format("    actual minX=%g, minY=%g, minZ=%g, "
			      + "maxX=%g, maxY=%g, maxZ=%g\n",
			      minX, minY, minZ, maxX, maxY, maxZ);
	    System.out.println ("    MinX = " + br.getMinX());
	    System.out.println ("    MinY = " + br.getMinY());
	    System.out.println ("    MinZ = " + br.getMinZ());
	    System.out.println ("    MaxX = " + br.getMaxX());
	    System.out.println ("    MaxY = " + br.getMaxY());
	    System.out.println ("    MaxZ = " + br.getMaxZ());
	    System.exit(1);
	}

	g2d.setColor(Color.RED);
	graph.draw(g2d, xpath);
	g2d.setColor(Color.GREEN);
	graph.draw(g2d, ypath);
	g2d.setColor(Color.BLUE);
	graph.draw(g2d, zpath);
	graph.write("png", "sp3dtest.png");

	System.out.println("Test values (cyclic case)");
	int errcount = 0;
	for (int i = 0; i < 19; i++) {
	    if (i < 18) {
		for (double j = 0; j < 10; j++) {
		    double uu = (double)i + ((double)j)/10.0;
		    double u = xpath.getX(uu);
		    if (Math.abs(path.getX(u) - xpath.getY(uu)) > 1.e-10) {
			System.out.format("    uu=%g, u=%g: x=%g, should be %g\n",
					  uu, u, path.getX(u), xpath.getY(uu));
			errcount++;
		    }
		    if (Math.abs(path.getY(u) - ypath.getY(uu)) > 1.e-10) {
			System.out.format("    uu=%g, u=%g: y=%g, should be %g\n",
					  uu, u, path.getY(u), ypath.getY(uu));
			errcount++;
		    }
		    if (Math.abs(path.getZ(u) - zpath.getY(uu)) > 1.e-10) {
			System.out.format("    uu=%g, u=%g: z=%g, should be %g\n",
					  uu, u, path.getZ(u), zpath.getY(uu));
			errcount++;
		    }
		}
	    }
	}
	if (errcount > 0) {
	    double[] xcpoints1 = new double[19*3];
	    double[] xcpoints2 = new double[19*3];
	    int[] type1 = new int[20];
	    int[] type2 = new int[20];
	    double[] coords1 = new double[9];
	    double[] coords2 = new double[6];
	    PathIterator3D pi1 = path.getPathIterator(null);
	    PathIterator pi2 = xpath.getPathIterator(null);
	    int index1 = 0;
	    int index2 = 0;
	    int cindex1 = 0;
	    int cindex2 = 0;
	    boolean done1 = false;
	    boolean done2 = false;
	    while (!(done1 && done2)) {
		if (!pi1.isDone()) {
		    int type = pi1.currentSegment(coords1);
		    type1[index1++] = type;
		    if (type == PathIterator3D.SEG_MOVETO
			|| type == PathIterator3D.SEG_LINETO) {
			xcpoints1[cindex1++] = coords1[0];
		    } else if (type == PathIterator3D.SEG_QUADTO) {
			xcpoints1[cindex1++] = coords1[0];
			xcpoints1[cindex1++] = coords1[3];
		    } else if (type == PathIterator3D.SEG_CUBICTO) {
			xcpoints1[cindex1++] = coords1[0];
			xcpoints1[cindex1++] = coords1[3];
			xcpoints1[cindex1++] = coords1[6];
		    } else if (type == PathIterator3D.SEG_CLOSE) {
		    } else {
			System.out.println("    bad type1");
		    }
		    pi1.next();
		} else {
		    done1 = true;
		}
		if (!pi2.isDone()) {
		    int type = pi2.currentSegment(coords2);
		    type2[index2++] = type;
		    if (type == PathIterator.SEG_MOVETO
			|| type == PathIterator.SEG_LINETO) {
			xcpoints2[cindex2++] = coords2[1];
		    } else if (type == PathIterator.SEG_QUADTO) {
			xcpoints2[cindex2++] = coords2[1];
			xcpoints2[cindex2++] = coords2[3];
		    } else if (type == PathIterator.SEG_CUBICTO) {
			xcpoints2[cindex2++] = coords2[1];
			xcpoints2[cindex2++] = coords2[3];
			xcpoints2[cindex2++] = coords2[5];
		    } else if (type == PathIterator.SEG_CLOSE) {
		    } else {
			System.out.println("    bad type2");
		    }
		    pi2.next();
		} else {
		    done2 = true;
		}
	    }
	    if (cindex1 != cindex2) {
		System.out.format("    cindex1 and cindex2 differ: %d != %d\n",
				  cindex1, cindex2);
	    }
	    int cindex = 0;
	    for (int i = 0; i < 19; i++) {
		if (type1[i] != type2[i]) {
		    System.out.format("    i = %d: type1 = %d, type2 = %d\n",
				      i, type1[i], type2[i]);
		}
		int type = type1[i];
		int m = 0;
		if (type == PathIterator.SEG_MOVETO
		    || type == PathIterator.SEG_LINETO) {
		    m = 1;
		} else if (type == PathIterator.SEG_QUADTO) {
		    m = 2;
		} else if (type == PathIterator.SEG_CUBICTO) {
		    m = 3;
		} else if (type == PathIterator.SEG_CLOSE) {
		    m = 0;
		}
		for (int j = 0; j < m; j++) {
		    if (Math.abs(xcpoints1[cindex+j] 
				 - xcpoints2[cindex+j])> 1.e-10) {
			System.out.format("    type=%d, i=%d, j=%d: %g != %g\n",
					  type, i, j,
					  xcpoints1[cindex+j],
					  xcpoints2[cindex+j]);
		    }
		}
		cindex += m;
	    }
	}

	if (errcount > 0) throw new Exception("... failed");

	System.out.println("Test lengths (cyclic case)");

	BasicSplinePath2D loop = new BasicSplinePath2D(xc, yc, true);
	BasicSplinePath3D loop3D1 =
	    new BasicSplinePath3D(xc, yc, zero, true);
	BasicSplinePath3D loop3D2 =
	    new BasicSplinePath3D(xc, zero, yc, true);
				  
	double upairs[][] = {
	    {2.0, 3.0},
	    {12.5, 12.6},
	    {5.5, 7.5},
	    {10.0, 11.0},
	    {10.0, 12.0},
	    {0.0, 18.0},
	    {-0.5, 18.5},
	    {0.0, 36.0},
	    {0.5, 35.5},
	    {0.5, 53.5}
	};
	for (double[] upair: upairs) {
	    double u1 = upair[0];
	    double u2 = upair[1];
	    double len = loop.getPathLength(u1, u2);
	    loop.setAccuracyMode(true);
	    double len3 = loop.getPathLength(u1, u2);
	    loop.setAccuracyMode(false);
	    System.out.println("len = " + len + ", len3 = " + len3);
	    if (Math.abs(len-len3)/Math.abs(len) > 1.e-10) {
		throw new Exception();
	    }
	    double len1 = loop3D1.getPathLength(u1, u2);
	    double len2 = loop3D2.getPathLength(u1, u2);
	    if (Math.abs(len-len1) > 1.e-10 || Math.abs(len-len2) > 1.e-10) {
		System.out.format("    u1=%g, u2=%g: len=%g, len1=%g, len2=%g\n",
				  u1, u2, len, len1, len2);
		errcount++;
	    }
	}
	if (errcount > 0) throw new Exception("... failed");

	double tlen = loop.getPathLength();
	double tlen1 = loop3D1.getPathLength();
	double tlen2 = loop3D2.getPathLength();
	if (Math.abs(tlen-tlen1) > 1.e-10 || Math.abs(tlen-tlen2) > 1.e-10) {
	    System.out.format("    tlen=%g, tlen1=%g, tlen2=%g\n",
			      tlen, tlen1, tlen2);
	    errcount++;
	}
	if (errcount > 0) throw new Exception("failed");

	System.out.println("Check distance function (cyclic case)");
	
	for (int i = 0; i < 10000; i++) {
	    double u = (i-5000)/100.0;
	    double s = loop.s(u);
	    double s1 = loop3D1.s(u);
	    double s2 = loop3D2.s(u);
	    if (Math.abs(s1 - s) > 1.e-10 || Math.abs(s2 - s) > 1.e-10) {
		System.out.format("    u=%g: s = %g, s1 = %g, s2 = %g\n",
				  u, s, s1, s2);
		errcount++;
	    }
	    double uu = loop.u(s);
	    double uu1 = loop3D1.u(s1);
	    double uu2 = loop3D2.u(s2);
		
	    if (Math.abs(uu1 - uu) > 1.e-6 || Math.abs(uu2 - uu) > 1.e-6) {
		System.out.format("    u=%g: s = %g, uu = %g, uu1 = %g, uu2 = %g\n",
				  u, s, uu, uu1, uu2);
		System.out.format("    ... errors: %g, %g\n",
				  Math.abs(uu1-uu), Math.abs(uu2 - uu));
		errcount++;
	    }
	}
	if (errcount > 0) throw new Exception("... failed");

	// see if the s and u methods are consistent.
	for (int i = -5400; i <= 5400; i++) {
	    double u = i/100.0;
	    double lim = Math.abs(u);
	    if (lim < 1.0) lim = 1.0;
	    lim *= 0.015;
	    double s = loop.s(u);
	    double delta = Math.abs(u - loop.u(s));
	    if (delta > lim) {
		System.out.println("    i = " + i + ", delta = " + delta
				   + ", lim = " + lim
				   + ", s = " + s
				   + ", loop.u(s) = " + loop.u(s));
		errcount++;
	    }
	    loop.setAccuracyMode(true);
	    double s2 = loop.s(u);
	    if (Math.abs(s-s2)/s > 1.e-8) {
		System.out.println("s = " +s);
		System.out.println("s2 = " +s2);
		throw new Exception();
	    }
	    double delta2 = Math.abs(u - loop.u(s2))/Math.max(1.0, Math.abs(u));
	    if (delta2 > 1.e-7) {
		System.out.println("s = " + s);
		System.out.println("s2 = " + s2);
		System.out.println("u = " + u);
		System.out.println("loop.u(s2) = " + loop.u(s2));
		System.out.println("delta = " + delta);
		System.out.println("delta2 = " + delta2);
		throw new Exception();
	    }
	    loop.setAccuracyMode(false);
	}
	if (errcount > 0) throw new Exception("... failed");


	System.out.println("Check radius of curvature");
	for (int i = 0; i <= 180; i++) {
	    double xk = loop.curvature(i/10.0);
	    if (xk < 0) xk = - xk;
	    // 3D curvatures are unsigned.
	    double xk1 = loop3D1.curvature(i/10.0);
	    double xk2 = loop3D2.curvature(i/10.0);
	    if (Math.abs(xk1 - xk) > 1.e-10 || Math.abs(xk2 - xk) > 1.e-10) {
		System.out.format("    u = %g: xk = %g, xk1 = %g, xk2 = %g\n",
				  i/10.0, xk, xk1, xk2);
		errcount++;
	    }

	}
	if (errcount > 0) throw new Exception("... failed");

	System.out.println("TRY NON-CYCLIC CASE");
	
	// graph = new Graph();
	// graph.setOffsets(25,25);
	// graph.setRanges(-100.0, 100.0, -100.0, 100.0);
	// g2d = graph.createGraphics();
	// g2d.setColor(Color.RED);
	xc = new double[10];
	yc = new double[10];
	zero = new double[10];
	for (int i = 0; i < 10; i++) {
	    double theta = Math.toRadians((double)20.0 * i);
	    double r = 100.0;
	    double x  = r * Math.cos(theta);
	    double y = r * Math.sin(theta);
	    xc[i] = x;
	    yc[i] = y;
	}
	BasicSplinePath3D path1 = new BasicSplinePath3D(xc, yc, zero, false);
	BasicSplinePath3D path2 = new BasicSplinePath3D(xc,zero, yc, false);

	BasicSplinePath2D path2D = new BasicSplinePath2D(xc, yc, false);

	AffineTransform3D af =
	    AffineTransform3D.getRotateInstance(0.0, Math.PI/6.0, 0.0);
	// the transformed path rotates the path1 about the x axis so that
	// the new path varies in both X, Y, and Z.  The result is such that
	// s as a function of u is the same for the two paths, but all
	// coordinates are in use, so we'll test more of the code.
	path = new BasicSplinePath3D(path1, af);

	System.out.println("Check coordinates and distance function "
			   + "(noncyclic case)");

	for (int i = 0; i < 10; i++) {
	    if (i < 9) {
		for (int j = 0; j < 10; j++) {
		    double u = i + j/10.0;
		    double x = path2D.getX(u);
		    double y = path2D.getY(u);
		    double x1 = path1.getX(u);
		    double y1 = path1.getY(u);
		    double z1 = path1.getZ(u);
		    double x2 = path2.getX(u);
		    double y2 = path2.getY(u);
		    double z2 = path2.getZ(u);
		    
		    if (Math.abs(x - x1) > 1.e-10
			|| Math.abs(y - y1) > 1.e-10
			|| Math.abs(z1) > 1.e-10) {
			System.out.format("    u=%g: "
					  + "x=%g, y=%g, x1=%g, y1=%g, z1=%g\n",
					  x, y, x1, y1, z1);
			errcount++;
		    }
		    if (Math.abs(x - x2) > 1.e-10
			|| Math.abs(y - z2) > 1.e-10
			|| Math.abs(y2) > 1.e-10) {
			System.out.format("    u=%g: "
					  + "x=%g, y=%g, x2=%g, y2=%g, z2=%g\n",
					  x, y, x2, y2, z2);
			errcount++;
		    }
		    double s2d = path2D.s(u);
		    double s3d = path.s(u);
		    if (Math.abs(s3d-s2d) > 1.e-6) {
			System.out.format("    u=%g: s3d = %g, s2d = %g"
					  + " (|s3d - s2d| = %g)\n",
					  u, s3d, s2d, Math.abs(s3d-s2d));
			errcount++;
		    }
		    double x3 = path.getX(u);
		    double y3 = path.getY(u);
		    double z3 = path.getZ(u);
		    double r2dsq = x*x + y*y;
		    double r3sq = x3*x3 + y3*y3 + z3*z3;
		    if (Math.abs(r2dsq - r3sq) > 1.e-10) {
			System.out.format("    u=%g: r3sq = %g, r2dsq = %g\n",
					   u, r3sq, r2dsq);
			errcount++;
		    }
		}
	    }
	}
	if (errcount > 0) throw new Exception("... failed");

	// check initialization code to make sure everything is
	// set up no matter which method we call first.
	System.out.println("testing inits - line from (0,0,0) to (10,10,10)");
			   
	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.curvature(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2sDu2(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2xDu2(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2yDu2(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dsDu(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dxDu(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dyDu(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getDistance(0.5, 0.7);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getMaxParameter();

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getPathLength();

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getPathLength(0.5, 0.7);


	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getPoint(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getX(0.5);
	
	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getY(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.u(0.5);

	path = new BasicSplinePath3D();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.s(0.5);

	// Now repeat, clearing the path instead of creating a new one.
	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.curvature(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2sDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2xDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.d2yDu2(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dsDu(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dxDu(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.dyDu(0.5);

	System.out.println("(will print entries for a few cases)");
	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(30.0, 40.0, 50.0);
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
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	System.out.println("max path parameter = " + path.getMaxParameter());

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	System.out.println("path length = " + path.getPathLength());

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getPathLength(0.5, 0.7);


	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getPoint(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getX(0.5);
	
	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.getY(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.u(0.5);

	path.clear();
	path.moveTo(0.0, 0.0, 0.0);
	path.lineTo(10.0, 10.0, 10.0);
	path.s(0.5);

	System.out.println("... OK");

	final double radius = 50;
	final double height = 2;
	BasicSplinePath3D helix = new BasicSplinePath3D
	    ((t) -> {return radius*Math.cos(t);},
	     (t) -> {return radius*Math.sin(t);},
	     (t) -> {return height*t;},
	     0.0, 10*Math.PI, 128, false);

	// See https://en.wikipedia.org/wiki/Frenet%E2%80%93Serret_formulas
	double torsion = height / (radius*radius + height*height);
	System.out.println("torsion = " + helix.torsion(5.0)
			   + ", expecting " + torsion);

	// We'd expect some variation in the torsion because a seris
	// of cubic Bezier curve segments cannot represent a helix exactly.
	// The limit is set to a 3 percent variation from the correct
	// value for a helix.
	for (int i = 40; i < 61; i++) {
	    double t = i / 10.0;
	    if (Math.abs(helix.torsion(t) - torsion) > torsion*0.03) {
		System.out.format("torsion at %g = %g, expecting %g\n",
				  t, helix.torsion(t), torsion);
		System.exit(1);
	    }
	}
	try {
	    System.out.println("Try a case that will generate an error");
	    SplinePath3D spath = new SplinePath3D();
	    Point3D[] points = {new Point3D.Double(0.0, 0.0, 0.0),
				new Point3D.Double(10.0, 10.0, 15.0),
				new Point3D.Double(30.0, 40.0, 60.0)};
	    spath.splineTo(points);
	} catch (Exception e) {
	    System.out.println("... " + e.getMessage());
	    System.out.println("... [Exception expected]\n");
	}

	BasicSplinePath3D bpath = new BasicSplinePath3D();
	// bpath.moveTo(-1.0, -1.0, -1.0);
	bpath.moveTo(0.0, 0.0, 0.0);
	bpath.lineTo(1.0, 0.0, 0.0);
	bpath.lineTo(1.0, 1.0, 0.0);
	bpath.lineTo(1.0, 1.0, 1.0);
	bpath.lineTo(1.0, 0.0, 1.0);
	bpath.lineTo(0.0, 0.0, 1.0);
	bpath.closePath();
	bpath.printTable();
	System.out.println("max parameter = " + bpath.getMaxParameter());
	System.out.println("path length = " + bpath.getPathLength());
	System.out.println("max parameter = " + bpath.getMaxParameter());
	System.out.println("max parameter = " + bpath.getMaxParameter());
	System.out.println("path length [0, 5] = "
			   + bpath.getPathLength(0.0, 5.0));
	System.out.println("path length [0, 6] = "
			   + bpath.getPathLength(0.0, 6.0));
	System.out.println("path length [0, 7] = "
			   + bpath.getPathLength(0.0, 7.0));
	System.out.println("path length [0, 12] = "
			   + bpath.getPathLength(0.0, 12.0));

	if (errcount > 0) {
	    System.out.println(" ... failed");
	    System.exit(1);
	}

	System.out.println("... OK");

	System.exit(0);


    }
}
