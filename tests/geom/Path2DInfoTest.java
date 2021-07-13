import org.bzdev.geom.*;
import org.bzdev.math.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.Arrays;
import org.bzdev.util.PrimArrays;
import org.bzdev.util.IntComparator;

public class Path2DInfoTest {

    static void centerOfMassTest() {

	Path2D line1 = new Path2D.Double();
	line1.moveTo(193.53515548309275, 699.4515230632213);
	line1.lineTo(213.3553390593274, 745.3553390593272);
	line1.lineTo(233.17552263556203, 791.2591550554332);

	Point2D lcm1 = Path2DInfo.centerOfMassOf(line1);

	Path2D line2 = new Path2D.Double();
	line2.moveTo(193.53515548309275, 699.4515230632213);
	line2.lineTo(213.3553390593274, 745.3553390593272);
	line2.lineTo(233.17552263556203, 791.2591550554332);
	line2.closePath();

	Point2D lcm2 = Path2DInfo.centerOfMassOf(line2);
	System.out.println("lcm1 = " + lcm1);
	System.out.println("lcm2 = " + lcm2);

	Rectangle2D line1bb = line1.getBounds2D();
	System.out.println("area of line1 = " + Path2DInfo.areaOf(line2));
	System.out.println("area of line2 = " + Path2DInfo.areaOf(line2));
	System.out.println("bb area = "
			   + (line1bb.getWidth() * line1bb.getHeight()));
	System.out.println("expecting bad cm for line1/line2 because the ");
	System.out.println("area for these curves when closed is much less ");
	System.out.println("than the area for the curve's bounding box: ");
	System.out.println("the area for these curves should be zero if ");
	System.out.println("there were no floating-point errors");

	Path2D rect = new Path2D.Double();

	rect.moveTo(100.0, 200.0);
	rect.lineTo(500.0, 200.0);
	rect.lineTo(500.0, 700.0);
	rect.lineTo(100.0, 700.0);
	rect.closePath();

	// same as rect, but using cubic Bezier Curves.
	Path2D rect2 = new Path2D.Double();
	rect2.moveTo(100.0, 200.0);
	double[] coords = new double[6];
	double[] tmp = new double[6];
	coords[0] = 500.0; coords[1] = 200.0;
	Path2DInfo.elevateDegree(1, tmp, 100.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 100.0, 200.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 200.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 100.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect2.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	rect2.closePath();

	Path2D rect4 = new Path2D.Double();
	rect4.moveTo(100.0, 200.0);
	rect4.lineTo(500.0, 200.0);
	rect4.lineTo(500.0, 700.0);
	rect4.lineTo(900.0, 700.0);
	rect4.lineTo(900.0, 1200.0);
	rect4.lineTo(500.0, 1200.0);
	rect4.lineTo(500.0, 700.0);
	rect4.lineTo(100.0, 700.0);
	rect4.closePath();

	Path2D rect5 = new Path2D.Double();
	rect5.moveTo(100.0, 200.0);
	coords[0] = 500.0; coords[1] = 200.0;
	Path2DInfo.elevateDegree(1, tmp, 100.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 100.0, 200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 900.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 900.0; coords[1] = 1200.0;
	Path2DInfo.elevateDegree(1, tmp, 900.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 900.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 1200.0;
	Path2DInfo.elevateDegree(1, tmp, 900.0, 1200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 900.0, 1200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 500.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 1200.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 1200.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	coords[0] = 100.0; coords[1] = 700.0;
	Path2DInfo.elevateDegree(1, tmp, 500.0, 700.0, coords);
	Path2DInfo.elevateDegree(2, coords, 500.0, 700.0, tmp);
	rect5.curveTo(coords[0], coords[1], coords[2], coords[3],
		      coords[4], coords[5]);
	rect5.closePath();


	Path2D circ = Paths2D.createArc(250.0, 350.0, 250.0, 300.0,
					2*Math.PI, Math.PI/18);
	circ.closePath();
	Path2D path = new Path2D.Double(Path2D.WIND_EVEN_ODD);
	path.append(rect, false);
	path.append(circ, false);
	Point2D p1 = Path2DInfo.centerOfMassOf(rect);
	Point2D p12 = Path2DInfo.centerOfMassOf(rect2);
	Point2D p2 = Path2DInfo.centerOfMassOf(circ);
	Point2D p3 = Path2DInfo.centerOfMassOf(path);
	System.out.println("computing p4");
	Point2D p4 = Path2DInfo.centerOfMassOf(rect4);
	System.out.println("computing p5");
	Point2D p5 = Path2DInfo.centerOfMassOf(rect5);

	if (Math.abs(p1.getX() - p12.getX()) > 1.e-10
	    || Math.abs(p1.getY() - p12.getY()) > 1.e-10) {
	    System.out.println("p1 and p12 differ");
	    System.exit(1);
	}
	System.out.println("circumference of rect = "
			   + Path2DInfo.circumferenceOf(rect2));
	System.out.println("circumference of rect2 = "
			   + Path2DInfo.circumferenceOf(rect2));
	System.out.println("area of rect = " + Path2DInfo.areaOf(rect));
	System.out.println("area of rect2 = " + Path2DInfo.areaOf(rect2));

	System.out.println("circumference of rect4 = "
			   + Path2DInfo.circumferenceOf(rect4));
	System.out.println("circumference of rect5 = "
			   + Path2DInfo.circumferenceOf(rect5));

	System.out.println("area of rect4 = " + Path2DInfo.areaOf(rect4));
	System.out.println("area of rect5 = " + Path2DInfo.areaOf(rect5));

	if (Math.abs(p4.getX() - p5.getX()) > 1.e-10
	    || Math.abs(p4.getY() - p5.getY()) > 1.e-10) {
	    System.out.println("p4 and p5 differ");
	    System.out.format("p4 = (%g, %g)\n", p4.getX(), p4.getY());
	    System.out.format("p5 = (%g, %g)\n", p5.getX(), p5.getY());
	    System.exit(1);
	}


	double x1 = p1.getX();
	double y1 = p1.getY();
	double x2 = p2.getX();
	double y2 = p2.getY();
	double x3 = p3.getX();
	double y3 = p3.getY();
	double x4 = p4.getX();
	double y4 = p4.getY();

	System.out.format("rect: (%g, %g)\n", x1, y1);
	System.out.format("circ: (%g, %g)\n", x2, y2);
	System.out.format("combo: (%g, %g)\n", x3, y3);

	System.out.format("rect4: (%g, %g)\n", x4, y4);


	// Rotate 30 degrees so none of the line segments line up with
	// the X and Y axis.
	AffineTransform af = AffineTransform.getRotateInstance(Math.PI/6);

	Point2D p1r = Path2DInfo.centerOfMassOf(rect, af);
	Point2D p2r = Path2DInfo.centerOfMassOf(circ, af);
	Point2D p3r = Path2DInfo.centerOfMassOf(path, af);

	Point2D ep1r = af.transform(p1, null);
	Point2D ep2r = af.transform(p2, null);
	Point2D ep3r = af.transform(p3, null);

	// use known values.
	x1 = (500.0 + 100) / 2;
	y1 = (200.0 + 700) / 2;
	x2 = 250.0;
	y2 = 350.0;

	if (Math.abs(x1 - p1.getX()) > 1.e-10
	    || Math.abs(y1 - p1.getY()) > 1.e-10) {
	    System.out.format("expected rect: (%s, %s)\n", x1, y1);
	    System.out.format("found rect: (%s, %s)\n", p1.getX(), p1.getY());
	    System.exit(1);
	}
	if (Math.abs(x2 - p2.getX()) > 1.e-10
	    || Math.abs(y2 - p2.getY()) > 1.e-10) {
	    System.out.format("expected circ: (%s, %s)\n", x2, y2);
	    System.out.format("found circ: (%s, %s)\n", p2.getX(), p2.getY());
	    System.exit(1);
	}

	double ra = 400.0 * 500.0;
	double ca = Math.PI*50.0*50.0;
	double x = (x1*ra - x2*ca)/ (ra - ca);
	double y = (y1*ra - y2*ca)/ (ra - ca);
	if (Math.abs(x - p3.getX()) > 1.e-8
	    || Math.abs(y - p3.getY()) > 1.e-8) {
	    System.out.format("expected combo: (%s, %s)\n", x, y);
	    System.out.format("found combo: (%s, %s)\n", p3.getX(), p3.getY());
	    System.exit(1);
	}

	if ((Math.abs(p1r.getX() - ep1r.getX()) > 1.e-10)
	    || (Math.abs(p1r.getY() - ep1r.getY()) > 1.e-10)) {
	    System.out.println("rotated rectangle failed");
	}

	if ((Math.abs(p2r.getX() - ep2r.getX()) > 1.e-10)
	    || (Math.abs(p2r.getY() - ep2r.getY()) > 1.e-10)) {
	    System.out.println("rotated circle failed");
	}
	if ((Math.abs(p1r.getX() - ep1r.getX()) > 1.e-10)
	    || (Math.abs(p1r.getY() - ep1r.getY()) > 1.e-10)) {
	    System.out.println("rotated rectangle failed");
	}
	if ((Math.abs(p3r.getX() - ep3r.getX()) > 1.e-10)
	    || (Math.abs(p3r.getY() - ep3r.getY()) > 1.e-10)) {
	    System.out.println("rotated combo failed");
	}

	Path2DInfo.printSegments(rect2);
	System.out.println("Computing rmatrix");
	double[][] rmatrix = Path2DInfo.momentsOf(rect);
	System.out.println("Computing rmatrix2");
	double[][] rmatrix2 = Path2DInfo.momentsOf(rect2);
	boolean err = false;
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix[i][j] - rmatrix2[i][j]) > 0.1) {
		    System.out.println("rmatrix != rmatrix2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, rmatrix[i][j], rmatrix2[i][j]);
		    err = true;
		}
	    }
	}

	// Try with a reference point set to be the center of mass.
	rmatrix2 = Path2DInfo.momentsOf(p1, rect);
	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix[i][j] - rmatrix2[i][j]) > 0.1) {
		    System.out.println("rmatrix != rmatrix2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, rmatrix[i][j], rmatrix2[i][j]);
		    err = true;
		}
	    }
	}

	AffineTransform rot = AffineTransform.getRotateInstance(Math.PI/6);

	Path2D rectr = new Path2D.Double(rect, rot);
	Point2D ref = new Point2D.Double(100.0, 200.0);
	Point2D refr = rot.transform(ref, null);

	// check three-argument momentsOf
	double[][] moments1 = Path2DInfo.momentsOf(refr, rectr);
	double[][] moments2 = Path2DInfo.momentsOf(ref, rect, rot);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(moments1[i][j] - moments2[i][j]) > 0.1) {
		    System.out.println("moments1 != moments2");
		    System.out.format("at [%d][%d]: %s != %s\n",
				      i, j, moments1[i][j], moments2[i][j]);
		    err = true;
		}
	    }
	}

	if (err) System.exit(1);

	double[][] cmatrix = Path2DInfo.momentsOf(circ);
	double[][] pmatrix = Path2DInfo.momentsOf(path);
	double[][] rmatrix4 = Path2DInfo.momentsOf(rect4);
	double[][] rmatrix5 = Path2DInfo.momentsOf(rect5);

	for (int i = 0; i < 2; i++) {
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(rmatrix4[i][j] - rmatrix5[i][j]) > 0.1) {
		    System.out.println("rmatrix4 != rmatrix5");
		    System.exit(1);
		}
	    }
	}

	// expecting (400^2/12) = 13,333.3333 for Iy and
	// 500^2/12  = 20833.3333 for Ix; 0 for Ixy
	// See https://wp.optics.arizona.edu/optomech/wp-content/uploads/sites/53/2016/10/OPTI_222_W61.pdf

	System.out.println("rmatrix: ");
	System.out.format("    | %8g  %8g |\n", rmatrix[0][0], rmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", rmatrix[1][0], rmatrix[1][1]);
	System.out.println();
	if (Math.abs(rmatrix[0][0] - 13333.33333333) > 0.1
	    || Math.abs(rmatrix[1][1] - 20833.3333333) > 0.1
	    || Math.abs(rmatrix[0][1]) > 1.e-10) {
	    System.out.println("bad rmatrix");
	    System.exit(1);
	}

	// Expecting 1/4 radius squared
	// for diagonal elements (625) for a perfect
	// circle); 0 for off-diagonal elements. Our circle is a close
	// approximation.
	// See http://hyperphysics.phy-astr.gsu.edu/hbase/tdisc.html
	System.out.println("cmatrix: ");
	System.out.format("    | %8g  %8g |\n", cmatrix[0][0], cmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", cmatrix[1][0], cmatrix[1][1]);
	System.out.println();

	if (Math.abs(cmatrix[0][0] - 625.0) > 1.e-2
	    || Math.abs(cmatrix[0][0] - cmatrix[1][1]) > 1.e-3
	    || Math.abs(cmatrix[0][1]) > 1.e-8) {
	    System.out.println("bad cmatrix");
	    System.out.println("cmatrix[0][0] - 625.0 = "
			       + (cmatrix[0][0] - 625.0));
	    System.exit(1);
	}


	System.out.println("pmatrix: ");
	System.out.format("    | %8g  %8g |\n", pmatrix[0][0], pmatrix[0][1]);
	System.out.format("    | %8g  %8g |\n", pmatrix[1][0], pmatrix[1][1]);
	System.out.println();

	// diagonals should be 4 times higher than the ones for rect.
	// xy moment should be ((400)(500))/2.

	System.out.println("rmatrix4: ");
	System.out.format("    | %8g  %8g |\n", rmatrix4[0][0], rmatrix4[0][1]);
	System.out.format("    | %8g  %8g |\n", rmatrix4[1][0], rmatrix4[1][1]);
	System.out.println();

	if (Math.abs(rmatrix4[0][0] - 4*rmatrix[0][0]) > 0.1
	    || Math.abs(rmatrix4[1][1] - 4*rmatrix[1][1]) > 0.1
	    || Math.abs(rmatrix4[0][1] - 50000) > 0.1) {
	    System.out.println("bad rmatrix4");
	    System.exit(1);
	}

	// Test eigenvalues

	double[] eigenvalues = Path2DInfo.principalMoments(rmatrix);
	System.out.format("eigenvalues = %g and %g\n",
			  eigenvalues[0], eigenvalues[1]);
	double[][]eigenvectors = Path2DInfo.principalAxes(rmatrix);
	System.out.format("eigenvectors: (%g, %g) and (%g, %g)\n",
			  eigenvectors[0][0], eigenvectors[0][1],
			  eigenvectors[1][0], eigenvectors[1][1]);

	AffineTransform af45 = AffineTransform.getRotateInstance(Math.PI/4);
	Point2D tpt = new Point2D.Double(0.0, 1.0);
	System.out.println(tpt + " -> " + af45.transform(tpt, null));
	double[][] rmatrix45 = Path2DInfo.momentsOf(rect, af45);
	System.out.println("rmatrix45: ");
	System.out.format("    | %8g  %8g |\n",
			  rmatrix45[0][0], rmatrix45[0][1]);
	System.out.format("    | %8g  %8g |\n",
			  rmatrix45[1][0], rmatrix45[1][1]);
	System.out.println();

	double[] eigenvalues45 = Path2DInfo.principalMoments(rmatrix45);
	System.out.format("eigenvalues45 = %g and %g\n",
			  eigenvalues45[0], eigenvalues45[1]);
	double[][]eigenvectors45 = Path2DInfo.principalAxes(rmatrix45);
	System.out.format("eigenvectors45: (%g, %g) and (%g, %g)\n",
			  eigenvectors45[0][0], eigenvectors45[0][1],
			  eigenvectors45[1][0], eigenvectors45[1][1]);

	for (int i = 0; i < 2; i++) {
	    double[] tmp1 = MatrixOps.multiply(rmatrix45, eigenvectors45[i]);
	    double[] tmp2 = VectorOps.multiply(eigenvalues45[i],
					       eigenvectors45[i]);
	    for (int j = 0; j < 2; j++) {
		if (Math.abs(tmp1[j]-tmp2[j]) > 1.e-10) {
		    System.out.println("i = " + i);
		    System.out.format("tmp1 = (%s, %s)\n", tmp1[0], tmp1[1]);
		    System.out.format("tmp2 = (%s, %s)\n", tmp2[0], tmp2[1]);
		    System.out.format("ratio = (%s, %s)\n", tmp1[0]/tmp2[0],
				      tmp1[1]/tmp2[1]);
		    System.out.println("eigenvalue/eigenvector inconsistent");
		    System.exit(1);
		}
	    }
	}

    }

    static RealValuedFunction ifx = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.cos(Math.toRadians(t));
	    }
	};

    static RealValuedFunction ify = new RealValuedFunction() {
	    public double valueAt(double t) {
		return 50.0 * Math.sin(Math.toRadians(t));
	    }
	};

    // copied from Path2DInfo.java with a name change.  This is the
    // one we tested for small n, and can be used for larger n for
    // verification purposes.
    public static Path2D convexHullGW(double x, double y, double[] coords,
				      int n)
    {
	// psuedocode: https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	if (n < 0) {
	    throw new IllegalArgumentException(("fourthArgNeg"));
	}
	if (coords.length < 2*n) {
	    throw new IllegalArgumentException(("argarray"));
	}
	//Gift-wrapping algorithm
	double sx = x;
	double sy = y;
	int pointOnHullInd = -1;
	for (int i = 0; i < n; i++) {
	    int ind = 2*i;
	    if (coords[ind] < sx) {
		pointOnHullInd = i;
		sx = coords[ind];
		sy = coords[ind+1];
	    } else if (coords[ind] == sx && coords[ind+1] < sy) {
		pointOnHullInd = i;
		sy = coords[ind+1];
	    }
	}
	int startInd = pointOnHullInd;
	int endPointInd;
	int k = 0;
	Path2D path = new Path2D.Double();
	if (startInd == -1) {
	    path.moveTo(x, y);
	} else {
	    int ind2 = startInd*2;
	    path.moveTo(coords[ind2], coords[ind2+1]);
	}

	do {
	    if (pointOnHullInd != startInd) {
		if (pointOnHullInd == -1) {
		    path.lineTo(x, y);
		} else {
		    int ind2 = 2*pointOnHullInd;
		    path.lineTo(coords[ind2], coords[ind2+1]);
		}
	    }
	    endPointInd = -1;
	    double hx = (pointOnHullInd == -1)? x: coords[2*pointOnHullInd];
	    double hy = (pointOnHullInd == -1)? y: coords[2*pointOnHullInd + 1];
	    for (int j = -1; j < n; j++) {
		boolean test = (endPointInd == pointOnHullInd);
		if (test == false && (endPointInd != j)) {
		    double ex = (endPointInd == -1)? x: coords[2*endPointInd];
		    double ey = (endPointInd == -1)? y: coords[2*endPointInd+1];
		    double v1x = ex - hx;
		    double v1y = ey - hy;
		    double v2x = ((j == -1)? x: coords[2*j]) - hx;
		    double v2y = ((j == -1)? y: coords[2*j+1]) - hy;
		    double cprod = (j == pointOnHullInd)? 1.0:
			(v1x*v2y - v2x*v1y);
		    // allow for floating-point errors
		    double ulpcp = Math.abs(v1x*Math.ulp(v2y))
			+ Math.abs(v2y*Math.ulp(v1x))
			+ Math.abs(v2x*Math.ulp(v1y))
			+ Math.abs(v1y*Math.ulp(v2x));
		    ulpcp *= 16.0;
		    if (cprod < -ulpcp) {
			// want counterclockise hull
			test = true;
		    } else if (Math.abs(cprod) <= ulpcp) {
			// the two vectors are parallel, so replace
			// if the new one is longer
			double distsq1 = v1x*v1x + v1y*v1y;
			double distsq2 = v2x*v2x + v2y*v2y;
			if (distsq1 < distsq2) {
			    test = true;
			}
		    }
		} else {
		}
		if (test) {
		    endPointInd = j;
		}
	    }
	    k++;
	    pointOnHullInd = endPointInd;
	} while (endPointInd != startInd);
	path.closePath();
	return path;
    }

    public static Path2D convexHullAndrew(double x, double y,
					  double[] coords, int n)
    {
	// psuedocode: https://en.wikipedia.org/wiki/Gift_wrapping_algorithm
	if (n < 0) {
	    throw new IllegalArgumentException(("fourthArgNeg"));
	}
	if (coords.length < 2*n) {
	    throw new IllegalArgumentException(("argarray"));
	}
	// See
	// https://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain
	// for Andrew's monotone chain convex hull algorithm

	// use Andrew's monotone chain convex hull algorithm
	int[] points = new int[n+1];
	for (int i = 0; i < points.length; i++) {
	    points[i] = i-1;
	}
	PrimArrays.sort(points, new IntComparator() {
		public int compare(int ind1, int ind2) {
		    if (ind1 == -1) {
			if (ind2 == -1) {
			    return 0;
			} else {
			    int ind22 = ind2*2;
			    double x2 = coords[ind22];
			    if (x == x2) {
				double y2 = coords[ind22+1];
				if (y == y2) {
				    return 0;
				} else if (y < y2) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x < x2) {
				return -1;
			    } else {
				return 1;
			    }
			}
		    } else {
			int ind12 = ind1*2;
			double x1 = coords[ind12];
			if (ind2 == -1) {
			    if (x1 == x) {
				double y1 = coords[ind12+1];
				if (y1 == y) {
				    return 0;
				} else if (y1 < y) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x1 < x) {
				return -1;
			    } else {
				return 1;
			    }
			} else {
			    int ind22 = 2*ind2;
			    double x2 = coords[ind22];
			    if (x1 == x2)  {
				double y1 = coords[ind12+1];
				double y2 = coords[ind22+1];
				if (y1 == y2) {
				    return 0;
				} else if (y1 < y2) {
				    return -1;
				} else {
				    return 1;
				}
			    } else if (x1 < x2) {
				return -1;
			    } else {
				return 1;
			    }
			}
		    }
		}
		public boolean equals(Object obj) {
		    return this == obj;
		}
	    });
	/*
	  for (int i = 0; i < points.length; i++) {
	  int ind = points[i];
	  int ind2 = ind*2;
	  double x4 = (ind == -1)? x: coords[ind2];
	  double y4 = (ind == -1)? y: coords[ind2+1];
	  }
	*/
	int[] upper = new int[points.length];
	int[] lower = new int[points.length];
	int topUpper = 0;
	int topLower = 0;
	for (int i = 0; i < points.length; i++) {
	    while (topLower > 1) {
		int ind1 = lower[topLower-2];
		int ind2 = lower[topLower-1];
		int ind12 = ind1*2;
		int ind22 = ind2*2;
		int ii = points[i];
		int ii2 = ii*2;
		double x0 = (ind1 == -1)? x: coords[ind12];
		double y0 = (ind1 == -1)? y: coords[ind12+1];
		double x1 = (ind2 == -1)? x: coords[ind22];
		double y1 = (ind2 == -1)? y: coords[ind22+1];
		double x2 = (ii == -1)? x: coords[ii2];
		double y2 = (ii == -1)? y: coords[ii2+1];
		double v1x = x1-x0;
		double v1y = y1-y0;
		double v2x = x2-x0;
		double v2y = y2-y0;
		double cprod = v1x*v2y - v2x*v1y;
		double ulpcp = Math.abs(v1x*Math.ulp(v2y))
		    + Math.abs(v2y*Math.ulp(v1x))
		    + Math.abs(v2x*Math.ulp(v1y))
		    + Math.abs(v1y*Math.ulp(v2x));
		ulpcp *= 16.0;
		if (cprod < ulpcp) {
		    topLower--;
		} else {
		    break;
		}
	    }
	    lower[topLower++] = points[i];
	}
	for (int i = points.length-1; i >= 0; i--) {
	    while (topUpper > 1) {
		int ind1 = upper[topUpper-2];
		int ind2 = upper[topUpper-1];
		int ind12 = ind1*2;
		int ind22 = ind2*2;
		int ii = points[i];
		int ii2 = ii*2;
		double x0 = (ind1 == -1)? x: coords[ind12];
		double y0 = (ind1 == -1)? y: coords[ind12+1];
		double x1 = (ind2 == -1)? x: coords[ind22];
		double y1 = (ind2 == -1)? y: coords[ind22+1];
		double x2 = (ii == -1)? x: coords[ii2];
		double y2 = (ii == -1)? y: coords[ii2+1];
		double v1x = x1-x0;
		double v1y = y1-y0;
		double v2x = x2-x0;
		double v2y = y2-y0;
		double cprod = v1x*v2y - v2x*v1y;
		double ulpcp = Math.abs(v1x*Math.ulp(v2y))
		    + Math.abs(v2y*Math.ulp(v1x))
		    + Math.abs(v2x*Math.ulp(v1y))
		    + Math.abs(v1y*Math.ulp(v2x));
		ulpcp *= 16.0;
		if (cprod < ulpcp) {
		    topUpper--;
		} else {
		    break;
		}
	    }
	    upper[topUpper++] = points[i];
	}
	Path2D result = new Path2D.Double();
	boolean firstTime = true;
	topLower--;
	topUpper--;
	for (int i = 0; i < topLower; i++) {
	    int ind = lower[i];
	    int ind2 = ind*2;
	    double x3 = (ind == -1)? x: coords[ind2];
	    double y3 = (ind == -1)? y: coords[ind2+1];
	    if (firstTime) {
		result.moveTo(x3, y3);
		firstTime = false;
	    } else {
		result.lineTo(x3, y3);
	    }
	}
	for (int i = 0; i < topUpper; i++) {
	    int ind = upper[i];
	    int ind2 = ind*2;
	    double x3 = (ind == -1)? x: coords[ind2];
	    double y3 = (ind == -1)? y: coords[ind2+1];
	    if (firstTime) {
		result.moveTo(x3, y3);
		firstTime = false;
	    } else {
		result.lineTo(x3, y3);
	    }
	}
	result.closePath();
	return result;
    }

    // used by the Aki-Toussaint heuristic, but we need a stand-alone
    // test so this code was copied.
    private static boolean insideHull(double x0, double y0,
				      double[]coords, int aclen,
				      double x, double y)
    {
	double cx, cy, v1x, v1y, v2x, v2y, cprod, ulpcp;
	double fx = x0;
	double fy = y0;
	for (int i = 0; i < aclen; i += 2) {
	    cx = coords[i];
	    cy = coords[i+1];
	    v1x = cx - x0;
	    v1y = cy - y0;
	    v2x = x - x0;
	    v2y = y - y0;
	    cprod = v1x*v2y - v2x*v1y;
	    ulpcp = Math.abs(v1x*Math.ulp(v2y))
		+ Math.abs(v2y*Math.ulp(v1x))
		+ Math.abs(v2x*Math.ulp(v1y))
		+ Math.abs(v1y*Math.ulp(v2x));
	    ulpcp *= 16.0;
	    if (!(cprod > ulpcp)) {
		return false;
	    }
	    x0 = cx;
	    y0 = cy;
	}
	v1x = fx - x0;
	v1y = fy - y0;
	v2x = x - x0;
	v2y = y - y0;
	cprod = v1x*v2y - v2x*v1y;
	ulpcp = Math.abs(v1x*Math.ulp(v2y))
	    + Math.abs(v2y*Math.ulp(v1x))
	    + Math.abs(v2x*Math.ulp(v1y))
	    + Math.abs(v1y*Math.ulp(v2x));
	ulpcp *= 16.0;
	if (!(cprod > ulpcp)) {
	    return false;
	}
	return true;
    }

    static void perfTest(BasicSplinePath2D bpath,
			 java.util.List<Path2DInfo.Entry> entries)
	throws Exception
    {
	Path2DInfo.Entry entry = entries.get(3);
	int type = entry.getType();
	double x0 = entry.getStart().getX();
	double y0 = entry.getStart().getY();
	double[] coords = entry.getCoords();
	Path2DInfo.SegmentData sd = entry.getData();
	double sum = 0.0;
	double u = 0.5;
	int N = 1000000;
	long stime = 0;
	long etime = 0;

	for (int i = 0; i < 10; i++) {
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.curvature(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.curvature ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    Path2DInfo.SegmentData data =
		new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().curvature(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.curvature ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.getX(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.getX ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().getX(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.getX ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.dxDu(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.dxDu ran for "
				   + (etime - stime));
	    }
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().dxDu(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.dxDu ran for "
				   + (etime - stime));
	    }

	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    for (int j = 0; j < N; j++) {
		sum += Path2DInfo.d2xDu2(u, x0, y0, type, coords);
		if (u > 10.0) u += 1.0;
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("PathInfo2D.d2xDu2 ran for "
				   + (etime - stime));
	    }
	    if (i == 9) {
		stime = System.nanoTime();
	    }
	    data = new Path2DInfo.SegmentData(type, x0, y0, coords, null);
	    for (int j = 0; j < N; j++) {
		Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
		sum += entry.getData().d2xDu2(uv);
	       // to make sure Java doesn't optimize the code by only
	       // computing uv once.
		if (u > 10.0) {
		    u += 1.0;
		    entry = entries.get(j%3 + 1);
		}
	    }
	    if (i == 9) {
		etime = System.nanoTime();
		System.out.println("segdata.d2xDu2 ran for "
				   + (etime - stime));
	    }
	}
	System.out.println("sum = " + sum);
	Thread.sleep(5000);
    }

    public static void distTest() throws Exception {
	double x0 = 10.0;
	double y0 = 20.0;
	double coords2[] = {50.0, 60.0, 90.0, 100.0};
	double pcoords2[] = {
	    5.0, 20.0,
	    11.0, 21.0,
	    50.0, 65.0,
	    90.0, 105.0,
	    110.0, 100.0};

	for (int i = 0; i < pcoords2.length; i += 2) {
	    Point2D p = new Point2D.Double(pcoords2[i], pcoords2[i+1]);
	    double u = Path2DInfo.getMinDistBezierParm(p, x0, y0, coords2, 2);
	    Point2D cp =
		new Point2D.Double(Path2DInfo.getX(u, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2),
				   Path2DInfo.getY(u, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2));
	    double dist = p.distance(cp);
	    System.out.println("u = " + u + ", p = " + p + ", cp = " + cp
			       + ", dist = " + dist);
	    for (int j = 0; j <= 100; j++ ) {
		double v = i / 100.0;
		Point2D tp =
		    new Point2D.Double(Path2DInfo.getX(v, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2),
				   Path2DInfo.getY(v, x0, y0,
						   PathIterator.SEG_QUADTO,
						   coords2));
		if ((p.distance(tp) - dist) < -1.e-10) {
		    System.out.println("min = " + p.distance(tp)
				       +" for v = " + v
				       +", found " + dist);
		    throw new Exception("dist failed");
		}
	    }
	}

	double coords3[] = {40.0, 50.0, 70.0, 80.0, 90.0, 100.0};
	double pcoords3[] = {
	    5.0, 20.0,
	    11.0, 21.0,
	    40.0, 55.0,
	    70.0, 85.0,
	    90.0, 105.0,
	    100.0, 100.0
	};

	for (int i = 0; i < pcoords3.length; i += 2) {
	    Point2D p = new Point2D.Double(pcoords3[i], pcoords3[i+1]);
	    double u = Path2DInfo.getMinDistBezierParm(p, x0, y0, coords3, 3);
	    Point2D cp =
		new Point2D.Double(Path2DInfo.getX(u, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3),
				   Path2DInfo.getY(u, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3));
	    double dist = p.distance(cp);
	    System.out.println("u = " + u + ", p = " + p + ", cp = " + cp
			       + ", dist = " + dist);
	    for (int j = 0; j <= 100; j++ ) {
		double v = i / 100.0;
		Point2D tp =
		    new Point2D.Double(Path2DInfo.getX(v, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3),
				   Path2DInfo.getY(v, x0, y0,
						   PathIterator.SEG_CUBICTO,
						   coords3));
		if ((p.distance(tp) - dist) < -1.e-10) {
		    System.out.println("min = " + p.distance(tp)
				       +" for v = " + v
				       +", found " + dist);
		    throw new Exception("dist failed");
		}
	    }
	}

    }

    public static void intersectionTest() throws Exception {
	System.out.println("intersection test ...");

	// case that seems to behave wierdly
	double startx1 = 489.8278750335096;
	double starty1 = 818.1573721008735;
	double tcoords1[] = {
	    544.5419202414199,
	    872.8714173087839,
	    606.5591327339994,
	    869.0811134079981,
	    708.9442719099992,
	    817.8885438199983
	};
	double startx2 = 700.0;
	double starty2 = 820.0;
	double tcoords2[] = {
	    800.0,
	    820.0,
	};

	for (int i = 0; i < 10; i++) {
	    double u = 1.0 - i/100.0;
	    System.out.format("... u = %g, (%g, %g)\n",
			      u,
			      Path2DInfo.getX(u, startx1, starty1,
					      PathIterator.SEG_CUBICTO,
					      tcoords1),
			      Path2DInfo.getY(u, startx1, starty1,
					      PathIterator.SEG_CUBICTO,
					      tcoords1));
	}

	double[] ourxy
	    = Path2DInfo.getSegmentIntersectionUVXY
	    (PathIterator.SEG_CUBICTO, startx1, starty1, tcoords1,
	     PathIterator.SEG_LINETO, startx2, starty2, tcoords2);
	for (int i = 0; i < ourxy.length; i++) {
	    System.out.format("ourxy[%d] = %g\n", i, ourxy[i]);
	}


	double x1 = 10.0;
	double y1 = 20.0;
	double x2 = 30.0;
	double y2 = -20.0;

	double xv = 50.0;
	double yv = 5.0;
	double coords1V[] = {50.0, 120.0};

	double xh = 40.0;
	double yh = 10.0;
	double coords1H[] = {130.0, 50.0};

	double coords1L[] = {100.0, 110.0};
	double coords2L[] = {100.0, 120.0};

	double coords1Q[] = {50.0, 60.0, 100.0, 110.0};
	double coords2Q[] = {50.0, 30.0, 100.0, 120.0};

	double coords1C[] = {40.0, 50.0, 60.0, 75.0, 100.0, 110.0};
	double coords2C[] = {40.0, 25.0, 60.0, 65.0, 100.0, 120.0};

	double xm = 140;
	double ym = -50;
	double coordsML[] = {150.0, 200.0};
	double coordsMQ[] = {160.0, 10.0, 150.0, 200.0};
	double coordsMC[] = {160.0, -20.0, 170.0, 90.0, 150.0, 200.0};

	double coordsHQ[] = {50.0, 150.0, 80.0, 20.0};
	double coordsHC[] = {50.0, 150.0, 50.0, 140.0,  80.0, 20.0};

	// linear/linear
	double[] uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv == null) throw new Exception();
	System.out.println("u = " + uv[0] + ", v = " + uv[1]);
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_LINETO, coords2L))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_LINETO, coords2L))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_LINETO,
					      coords2L),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_LINETO,
					      coords2L));

	    throw new Exception();
	}

	/*
	for (int i = 0; i <= 10; i++) {
	    double v = i/10.0;
	    System.out.format("v = %g, x = %g, y = %g\n", v,
			      Path2DInfo.getX(v, x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(v, x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	}
	*/

	// linear/quadratic
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xh, yh, coords1H,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], xh, yh,
					  PathIterator.SEG_LINETO, coords1H)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], xh, yh,
					PathIterator.SEG_LINETO, coords1H)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xv, yv, coords1V,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], xv, yv,
					  PathIterator.SEG_LINETO, coords1V)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], xv, yv,
					PathIterator.SEG_LINETO, coords1V)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getY(uv[0], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	// quadratic/linear
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xh, yh, coords1H);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], xh, yh,
					  PathIterator.SEG_LINETO, coords1H)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], xh, yh,
					PathIterator.SEG_LINETO, coords1H)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_LINETO,
					      coords1H));

	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xv, yv, coords1V);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], xv, yv,
					  PathIterator.SEG_LINETO, coords1V)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], xv, yv,
					PathIterator.SEG_LINETO, coords1V)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V),
			      Path2DInfo.getY(uv[1], xv, yv,
					      PathIterator.SEG_LINETO,
					      coords1V));

	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, x1, y1, coords1L);
	if (uv == null) throw new Exception();
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));

	    throw new Exception();
	}

	// quadratic/quadratic

	System.out.println("... quadratic/quadratic case");


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_QUADTO, coords2Q))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_QUADTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... quadratic/cubic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... cubic/quadratic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[0], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));

	    throw new Exception();
	}

	System.out.println("... cubic/cubic");
	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv == null) {
	    System.out.println("no intersection");
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], x2, y2,
				       PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], x2, y2,
					  PathIterator.SEG_CUBICTO, coords2C))
	    > 1.e-10) {
	    System.out.format("u = %g, v = %g, (%g, %g) != (%g, %g)\n",
			      uv[0], uv[1],
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q),
			      Path2DInfo.getY(uv[1], x2, y2,
					      PathIterator.SEG_CUBICTO,
					      coords2Q));

	    throw new Exception();
	}

	System.out.println("... no-intersections test");

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, xm, ym, coordsML,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_LINETO, xm, ym, coordsML);
	if (uv != null) {
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    System.out.println("uv[0] = " + uv[0] + ", uv[1] = " + uv[1]);
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMQ,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMQ);
	if (uv != null) {
	    System.out.println("uv[0] = " + uv[0] + ", uv[1] = " + uv[1]);
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xm, ym, coordsMC,
	     PathIterator.SEG_LINETO, x2, y2, coords2L);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x2, y2, coords2L,
	     PathIterator.SEG_CUBICTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMC,
	     PathIterator.SEG_QUADTO, x2, y2, coords2Q);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x2, y2, coords2Q,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xm, ym, coordsMC,
	     PathIterator.SEG_CUBICTO, x2, y2, coords2C);
	if (uv != null) {
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x2, y2, coords2C,
	     PathIterator.SEG_QUADTO, xm, ym, coordsMC);
	if (uv != null) {
	    throw new Exception();
	}

	System.out.println("... multiple intersection test");

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_LINETO, x1, y1, coords1L	     );
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_LINETO, x1, y1, coords1L,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_LINETO, x1, y1, coords1L);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_LINETO, coords1L)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_LINETO, coords1L)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_LINETO,
					      coords1L));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, x1, y1, coords1Q,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_QUADTO, x1, y1, coords1Q);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_QUADTO, coords1Q)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_QUADTO, coords1Q)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_QUADTO,
					      coords1Q));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_QUADTO, xh, yh, coordsHQ);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_QUADTO, xh, yh, coordsHQ,
	     PathIterator.SEG_CUBICTO, x1, y1, coords1C	     );
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_QUADTO, coordsHQ))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_QUADTO,
					      coordsHQ),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    throw new Exception();
	}


	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, x1, y1, coords1C,
	     PathIterator.SEG_CUBICTO, xh, yh, coordsHC);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[0], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[1], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[0], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[1], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[2], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[3], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[2], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[3], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[0], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[1], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[2], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getX(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[3], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC));
	    throw new Exception();
	}

	uv = Path2DInfo.getSegmentIntersectionUV
	    (PathIterator.SEG_CUBICTO, xh, yh, coordsHC,
	     PathIterator.SEG_CUBICTO, x1, y1, coords1C);
	if (uv == null) throw new Exception();
	if (uv.length != 4) {
	    System.out.println("uv.length = " + uv.length);
	    throw new Exception();
	}
	if (Math.abs(Path2DInfo.getX(uv[1], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[0], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[1], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[0], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getX(uv[3], x1, y1,
					  PathIterator.SEG_CUBICTO, coords1C)
		     - Path2DInfo.getX(uv[2], xh, yh,
				       PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10
	    || Math.abs(Path2DInfo.getY(uv[3], x1, y1,
					PathIterator.SEG_CUBICTO, coords1C)
			- Path2DInfo.getY(uv[2], xh, yh,
					  PathIterator.SEG_CUBICTO, coordsHC))
	    > 1.e-10) {
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[0], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[1], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    System.out.format("(%g, %g) and (%g, %g)\n",
			      Path2DInfo.getX(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getY(uv[2], xh, yh,
					      PathIterator.SEG_CUBICTO,
					      coordsHC),
			      Path2DInfo.getX(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C),
			      Path2DInfo.getY(uv[3], x1, y1,
					      PathIterator.SEG_CUBICTO,
					      coords1C));
	    throw new Exception();
	}

	System.out.println("... intersection test done");
    }

    public static void main(String argv[]) throws Exception {

	centerOfMassTest();

	distTest();
	intersectionTest();

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

	int j = 0;
	PathIterator pit = path.getPathIterator(null);
	double[] coords = new double[6];
	double x = 0.0; double y = 0.0;
	double x0 = 0.0; double y0 = 0.0;
	double lastX = 0.0; double lastY = 0.0;
	while (!pit.isDone()) {
	    int type = pit.currentSegment(coords);
	    switch (type) {
	    case PathIterator.SEG_MOVETO:
		System.out.println("MOVETO (" + coords[0]
				   + ", " + coords[1] + ")");
		x = coords[0];
		y = coords[1];
		lastX = x;
		lastY = y;
		if (x != Path2DInfo.getX(0.5, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path2DInfo.getX failed");
		}
		if (y != Path2DInfo.getY(0.5, 0.0, 0.0, type, coords)) {
		    throw new Exception("Path2DInfo.getY failed");
		}
		break;
	    case PathIterator.SEG_LINETO:
		System.out.println("LINETO (" + coords[0]
				   + ", " + coords[1] + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y,
						 type, coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y,
						 type, coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-5) {
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-5) {
			throw new Exception("X derivative failed");
		    }
		    if (xpp != 0.0 || ypp != 0.0) {
			throw new Exception
			    ("non-zero second derivative");
		    }
		    if (xppp != 0.0 || yppp != 0.0) {
			throw new Exception
			    ("non-zero third derivative");
		    }
		}
		x = coords[0];
		y = coords[1];
		break;
	    case PathIterator.SEG_QUADTO:
		System.out.println("QUADTO:");
		for (int i = 0; i < 4; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y, type,
						 coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y, type,
						 coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);
		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " +(Math.abs(xp - (xv1-xv)/0.00001)));
			System.out.println("x = " + x + ", y = " + y);
			System.out.println("coords[0] = " + coords[0]
					   + ", coords[1] = " + coords[1]);
			System.out.println("coords[2] = " + coords[2]
					   + ", coords[3] = " + coords[3]);
			System.out.println( "xv = " + xv
					    + ", xv1 = " + xv1);
			System.out.println("xp = " + xp
					   +", (xv1-xv)/0.00001 = "
					   +((xv1-xv)/0.00001));

			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 1.e-4) {
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 1.e-4) {
			throw new Exception("Y second derivative failed");
		    }
		    if (xppp != 0.0 || yppp != 0.0) {
			throw new Exception
			    ("non-zero third derivative");
		    }
		}
		x = coords[2];
		y = coords[3];
		break;
	    case PathIterator.SEG_CUBICTO:
		System.out.println("CUBICTO:");
		for (int i = 0; i < 6; i += 2)
		    System.out.println("    ... control point ("
				       + coords[i] + ", " + coords[i+1]
				       + ")");
		{
		    double xv = Path2DInfo.getX(0.4, x, y, type, coords);
		    double yv = Path2DInfo.getY(0.4, x, y, type, coords);
		    double xv1 = Path2DInfo.getX(0.40001, x, y,
						 type, coords);
		    double yv1 = Path2DInfo.getY(0.40001, x, y,
						 type, coords);
		    double xp = Path2DInfo.dxDu(0.4, x, y, type, coords);
		    double yp = Path2DInfo.dyDu(0.4, x, y, type, coords);
		    double xp1 = Path2DInfo.dxDu(0.40001, x, y,
						 type, coords);
		    double yp1 = Path2DInfo.dyDu(0.40001, x, y,
						 type, coords);
		    double xpp = Path2DInfo.d2xDu2(0.4, x, y, type, coords);
		    double ypp = Path2DInfo.d2yDu2(0.4, x, y, type, coords);
		    double xppv1 = Path2DInfo.d2xDu2(0.40001, x, y, type,
						     coords);
		    double yppv1 = Path2DInfo.d2yDu2(0.40001, x, y, type,
						     coords);
		    double xppp = Path2DInfo.d3xDu3(0.4, x, y, type, coords);
		    double yppp = Path2DInfo.d3yDu3(0.4, x, y, type, coords);

		    if (Math.abs(xp - (xv1-xv)/0.00001) > 1.e-4) {
			System.out.println
			    ("error = " + Math.abs(xp - (xv1-xv)/0.00001));
			throw new Exception("X derivative failed");
		    }
		    if (Math.abs(yp - (yv1-yv)/0.00001) > 1.e-4) {
			throw new Exception("Y derivative failed");
		    }
		    if (Math.abs(xpp - (xp1-xp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(xpp - (xp1-xp)/0.00001));
			throw new Exception("X second derivative failed");
		    }
		    if (Math.abs(ypp - (yp1-yp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(ypp - (yp1-yp)/0.00001));
			throw new Exception("Y second derivative failed");
		    }
		    if (Math.abs(xppp - (xppv1-xpp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(xppp - (xppv1-xpp)/0.00001));
			throw new Exception("X third derivative failed");
		    }
		    if (Math.abs(yppp - (yppv1-ypp)/0.00001) > 5.0e-4) {
			System.out.println
			    ("error = " + Math.abs(yppp - (yppv1-ypp)/0.00001));
			throw new Exception("Y third derivative failed");
		    }
		}
		x = coords[4];
		y = coords[5];
		break;
	    case PathIterator.SEG_CLOSE:
		System.out.println("CLOSE PATH");
		coords[0] = lastX;
		coords[1] = lastY;
		x = coords[0];
		y = coords[1];
		break;
	    }
	    System.out.println("     ... static-method segment length = "
			       + Path2DInfo.segmentLength(type,
							  x0, y0,
							  coords));
	    System.out.println("     ... segment length["
			       + j
			       +"] = " +
			       Path2DInfo.segmentLength(path, j++));
	    x0 = x;
	    y0 = y;
	    pit.next();
	}

	Path2DInfo.SegmentData sd = null;
	for(Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    Point2D start = entry.getStart();
	    x0 = (start == null)? 0.0: start.getX();
	    y0 = (start == null)? 0.0: start.getY();
	    double[] coordinates = entry.getCoords();
	    int st = entry.getType();
	    if (st == PathIterator.SEG_MOVETO) continue;
	    double u = 0.4;
	    Path2DInfo.UValues uv = new Path2DInfo.UValues(u);
	    sd = new Path2DInfo.SegmentData(st, x0, y0, coordinates, sd);
	    double x1 = Path2DInfo.getX(u, x0,  y0, st, coordinates);
	    double y1 = Path2DInfo.getY(u, x0,  y0, st, coordinates);
	    double dxDu1 = Path2DInfo.dxDu(u, x0,  y0, st, coordinates);
	    double dyDu1 = Path2DInfo.dyDu(u, x0,  y0, st, coordinates);
	    double d2xDu21 = Path2DInfo.d2xDu2(u, x0,  y0, st, coordinates);
	    double d2yDu21 = Path2DInfo.d2yDu2(u, x0,  y0, st, coordinates);
	    double d3xDu31 = Path2DInfo.d2xDu2(u, x0,  y0, st, coordinates);
	    double d3yDu31 = Path2DInfo.d2yDu2(u, x0,  y0, st, coordinates);

	    double dsDu1 = Path2DInfo.dsDu(u, x0,  y0, st, coordinates);
	    double d2sDu21 = Path2DInfo.dsDu(u, x0,  y0, st, coordinates);
	    double curv1 = Path2DInfo.curvature(u, x0,  y0, st, coordinates);
	    double x2 = sd.getX(uv);
	    double y2 = sd.getY(uv);
	    double dxDu2 = sd.dxDu(uv);
	    double dyDu2 = sd.dyDu(uv);
	    double d2xDu22 = sd.d2xDu2(uv);
	    double d2yDu22 = sd.d2yDu2(uv);
	    double d3xDu32 = sd.d2xDu2(uv);
	    double d3yDu32 = sd.d2yDu2(uv);
	    double dsDu2 = sd.dsDu(uv);
	    double d2sDu22 = sd.dsDu(uv);
	    double curv2 = sd.curvature(uv);
	    if (Math.abs(x1 - x2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + x1 + " != " + x2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(y1 - y2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + y1 + " != " + y2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dxDu1 - dxDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dxDu1 + " != " + dxDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dyDu1 - dyDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dyDu1 + " != " + dyDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2xDu21 - d2xDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2yDu21 - d2yDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d3xDu31 - d3xDu32) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2xDu21 + " != " + d2xDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d3yDu31 - d3yDu32) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2yDu21 + " != " + d2yDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(dsDu1 - dsDu2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + dsDu1 + " != " + dsDu2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(d2sDu21 - d2sDu22) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + d2sDu21 + " != " + d2sDu22 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	    if (Math.abs(curv1 - curv2) > 1.e-10) {
		throw new Exception
		    ("SegmentData call and normal call disagree: "
		     + curv1 + " != " + curv2 + " ... st = "
		     + Path2DInfo.getTypeString(st));
	    }
	}

	int segStart = 2;
	int segEnd = 6;
	double total = 0.0;
	double segtotal = 0.0;
	for(Path2DInfo.Entry entry: Path2DInfo.getEntries(path)) {
	    System.out.println("index = " + entry.getIndex()
			       + ", type = " + entry.getTypeString()
			       + ", start = " + entry.getStart()
			       + ", end = " + entry.getEnd()
			       + ", length = " +entry.getSegmentLength());
	    total += entry.getSegmentLength();
	    int ind = entry.getIndex();
	    if (ind >= segStart && ind < segEnd) {
		segtotal += entry.getSegmentLength();
	    }
	    double[] c = entry.getCoords();
	    System.out.println("coords[0] = " + c[0]);
	}


	System.out.println("path length = " + Path2DInfo.pathLength(path));
	System.out.println("path length for segments ["
			   + segStart + ", " + segEnd +") = "
			   + Path2DInfo.pathLength(path, segStart, segEnd));
	if (Math.abs(total - Path2DInfo.pathLength(path)) > 1.e-8) {
	    System.out.println("wrong path length");
	    System.exit(1);
	}
	if (Math.abs(segtotal - Path2DInfo.pathLength
		     (path, segStart, segEnd)) > 1.e-8) {
	    System.out.println("wrong segment-range length");
	    System.exit(1);
	}

	Path2D path1 = new Path2D.Double();
	Path2D path2 = new Path2D.Double();

	path1.moveTo(0.0, 0.0);
	path2.moveTo(0.0, 0.0);
	path1.curveTo(100.0, 0.0, 100.0, 0.0, 100.0, 100.0);
	path2.quadTo(100.0, 0.0, 100.0, 100.0);
	
	System.out.println("path1 length: "
			   + Path2DInfo.segmentLength(path1, 1));
	System.out.println("path2 length: "
			   + Path2DInfo.segmentLength(path2, 1));
	coords = new double[6];
	coords[0] = 165.2016; coords[1] = 3.9624;
	coords[2] = 165.2016; coords[3] = 15.8596;
		
	System.out.println("canned length = "
			   + Path2DInfo.segmentLength
			   (PathIterator.SEG_QUADTO, 152.4, 3.9624,
			    coords));

	RealValuedFunction fx = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
		public double derivAt(double theta) {
		    return -100.0 * Math.sin(theta);
		}
	    };
	RealValuedFunction fy = new RealValuedFunction() {
		public double valueAt(double theta) {
		    return 100.0 * Math.sin(theta);
		}
		public double derivAt(double theta) {
		    return 100.0 * Math.cos(theta);
		}
	    };
	CubicSpline xspline = new CubicSpline1(fx, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	CubicSpline yspline = new CubicSpline1(fy, 0.0, Math.PI, 19*5,
					       CubicSpline.Mode.CLAMPED);
	pit = Path2DInfo.getPathIterator(null, xspline, yspline);
	path = new Path2D.Double();
	path.append(pit, false);

	if (Math.abs(Path2DInfo.pathLength(path) - Math.PI*100.0) > 1.e-3) {
	    System.out.println("path length for semicircle = "
			       + Path2DInfo.pathLength(path)
			       + ", expecting " + Math.PI*100.0);
	    System.exit(1);
	}

	System.out.println("first random path test");
			       
	path1 = new Path2D.Double();
	path1.moveTo(-50.0, 3.061616997868383E-14);
	path1.curveTo(-46.601769298462514, 15.141832724372453,
		      -48.50388233105287, -15.759866713122202,
		      -42.068883707497285, -30.564833119208576);
	path1.lineTo(-31.152618371501028, -42.87790070187225);
	path2 = new Path2D.Double();
	path2.moveTo(-45.038529126015305, -20.247367433334002);
	path2.curveTo(-44.309398690673675, -23.99897723551772,
		      -43.3558834322084, -27.603839837991313,
		      -42.068883707497285, -30.564833119208576);
	path2.lineTo(-40.97725717389764, -31.79613987747496);

	double u1 = 0.8;
	double u2 = 1.1;
	double path1Length = Path2DInfo.pathLength(path1);
	Path2D path1a = new Path2D.Double();
	path1a.append(new FlatteningPathIterator2D
		      (path1.getPathIterator(null), 1.0), false);
	BasicSplinePath2D path1b = new BasicSplinePath2D(path1);
	// path1b.setIntervalNumber(128);
	double path1aLength = Path2DInfo.pathLength(path1a);
	System.out.println("path1Length = " + path1Length);
	System.out.println("path1aLength = " + path1aLength);
	System.out.println("path1bLength = "
			   + Path2DInfo.pathLength(path1b));
	System.out.println("path1bLength = " + path1b.getPathLength());
	System.out.println("path1bLength (u1 to u2) = " 
			   + path1b.getPathLength(u1, u2));
	System.out.println("path2Length = " + Path2DInfo.pathLength(path2));

	System.out.println("second random path test");

	u1 = 0.1; u2 = 0.2;
	path1 = new Path2D.Double();
	path1.moveTo(-32.755801403744655, 100.8119907272862);
	path1.curveTo(-5.150015853103061E-14, 105.0,
		      -62.893021995294355, 86.56481839811957,
		      -87.37383539249424, 63.48080724758722);
	path2 = new Path2D.Double();
	path2.moveTo(-25.664464652595242, 101.40767214418844);
	path2.curveTo(-24.16779994820443, 101.39198361529388,
		      -23.493562445806894, 101.16803753897932,
		      -23.507691113405496, 100.7538082765707);

	path1Length = Path2DInfo.pathLength(path1);
	path1a = new Path2D.Double();
	path1a.append(new FlatteningPathIterator2D
		      (path1.getPathIterator(null), 1.0), false);
	path1b = new BasicSplinePath2D(path1);
	// path1b.setIntervalNumber(16);
	path1aLength = Path2DInfo.pathLength(path1a);
	System.out.println("path1Length = " + path1Length);
	System.out.println("path1aLength = " + path1aLength);
	System.out.println("path1bLength = "
			   + Path2DInfo.pathLength(path1b));
	System.out.println("path1bLength = " + path1b.getPathLength());
	System.out.println("path1bLength (u1 to u2) = " 
			   + path1b.getPathLength(u1, u2));
	System.out.println("path2Length = " + Path2DInfo.pathLength(path2));

	double ocoords[] = {1.0, 2.0, 6.0, 7.0, 10.0, 20.0};
	double ocoords1[] = {6.0, 7.0, 10.0, 20.0};
	double[] ncoords = new double[8];
	double[] ncoords1 = new double[6];

	Path2DInfo.elevateDegree(2, ncoords, 0, ocoords, 0);
	Path2DInfo.elevateDegree(2, ncoords1, 1.0, 2.0, ocoords1);

	if (ncoords[0] != ocoords[0] || ncoords[1] != ocoords[1]
	    || ncoords[6] != ocoords[4] || ncoords[7] != ocoords[5]) {
	    throw new Exception("ncoords error");
	}

	for (int i = 0; i < 6; i++) {
	    if (Math.abs(ncoords1[i] - ncoords[i+2]) > 1.e-10) {
	        System.out.format("ncoords1[%d] = %g while ncoords[%d] = %g\n",
				  i, ncoords1[i], i+2, ncoords[i+2]);
		throw new Exception("ncoords error");
	    }
	}

	for (int i = 0; i <= 20; i++) {
	    double u = i/20.0;
	    if (u < 0) u = 0.0;
	    if (u > 1) u = 1.0;
	    double v1 = Path2DInfo.getX(u, 1.0, 2.0,
					PathIterator.SEG_QUADTO,
					ocoords1);
	    double v2 =  Path2DInfo.getX(u, 1.0, 2.0,
					 PathIterator.SEG_CUBICTO,
					 ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getX(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }
	    v1 = Path2DInfo.getY(u, 1.0, 2.0,
				 PathIterator.SEG_QUADTO,
				 ocoords1);
	    v2 =  Path2DInfo.getY(u, 1.0, 2.0,
				  PathIterator.SEG_CUBICTO,
				  ncoords1);
	    if (Math.abs(v1 - v2) > 1.e-10) {
		System.out.format("getY(%g): quad = %g, cubic = %g\n",
				  u, v1, v2);
		throw new Exception("ncoords error");
	    }

	}

	System.out.println("counterclockwise test");

	path = new Path2D.Double();

	path.moveTo(0.0, 0.0);
	path.lineTo(1.0, 0.0);
	path.quadTo(2.0, 0.5, 1.0, 1.0);
	path.curveTo(0.66, 1.5, 0.33, 1.5, 0.0, 1.0);
	path.closePath();

	if (!Path2DInfo.isCounterclockwise(path)) {
	    System.out.println("counterclockwise = "
			       + Path2DInfo.isCounterclockwise(path));
	    System.exit(1);
	}
	if (Path2DInfo.isClockwise(path)) {
	    System.out.println("clockwise = "
			       + Path2DInfo.isClockwise(path));
	    System.exit(1);
	}


	System.out.println("lineSegmentsIntersect test");
	{
	    double xx1 = 0.0, yy1 = 0.0,
		xx2 = 25.0,  yy2 = 55.90169943749474,
		lxx1 = -50.0, lyy1 = 0.0,
		lxx2 = -25.0, lyy2 = 55.90169943749474;

	    if (Path2DInfo.lineSegmentsIntersect(xx1, yy1, xx2, yy2,
						 lxx1, lyy1, lxx2, lyy2)) {
		System.out.println("bad intersection [0]");
		System.exit(1);
	    }

	    double x1 = 38.428887674834904;
	    double y1 = 0.0;
	    double x2 = 37.66652173679645;
	    double y2 = 3.0305813264980603;
	    double x3 = 39.95361955091182;
	    double y3 = -6.06116265299612;
	    double x4 = 39.19125361287336;
	    double y4 = -3.0305813264980594;


	    if (Path2DInfo.lineSegmentsIntersect(x1, y1, x2, y2,
						 x3, y3, x4, y4)) {
		System.out.println("bad intersection [1]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, 1.0)) {
		System.out.println("bad intersection [2]");
		System.exit(1);
	    }
	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, 1.0)) {
		System.out.println("bad intersection [3]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [4]");
		System.exit(1);
	    }

	    if (Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 1.0, 0.0, 2.0, 0.0, 2.0, -1.0)) {
		System.out.println("bad intersection [5]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, 1.0, -1.0, 1.0, 1.0)) {
		System.out.println("bad intersection [6]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, 1.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [7]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, -1.0, 0.0, 1.0, 0.0)) {
		System.out.println("bad intersection [8]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 3.0, 0.0, 1.0, 0.0, 2.0, 0.0)) {
		System.out.println("bad intersection [9]");
		System.exit(1);
	    }

	    if (!Path2DInfo.lineSegmentsIntersect
		(0.0, 0.0, 2.0, 0.0, -1.0, 0.0, 3.0, 0.0)) {
		System.out.println("bad intersection [9]");
		System.exit(1);
	    }

	    x1 = 0.30470092700496826;
	    y1 = 9.883712979468449;
	    x2 = 7.375104376190603;
	    y2 = 4.817645302894979;
	    x3 = 5.272251521993861;
	    y3 = 9.875247930124923;
	    x4 = 1.6339912769768958;
	    y4 = 5.947394907277731;
	       
	    if (!Path2DInfo.lineSegmentsIntersect
		(x1, y1, x2, y2, x3, y3, x4, y4)) {
		System.out.println("bad intersection [10]");
		System.exit(1);
	    }

	    DoubleRandomVariable rv = new
		UniformDoubleRV(0.0, true, 10.0, true);
	    int cnt = 0;
	    for (int i = 0; i < 1000000; i++) {
		x1 = rv.next();
		y1 = rv.next();
		x2 = rv.next();
		y2 = rv.next();
		x3 = rv.next();
		y3 = rv.next();
		x4 = rv.next();
		y4 = rv.next();

		if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)
		    != Path2DInfo.lineSegmentsIntersect(x1, y1, x2, y2,
							x3, y3, x4, y4)) {
		    cnt++;
		    if (cnt == 1) {
			System.out.println("*** intersect methods differ");
			System.out.format("x1 = %s\n", x1);
			System.out.format("y1 = %s\n", y1);
			System.out.format("x2 = %s\n", x2);
			System.out.format("y2 = %s\n", y2);
			System.out.format("x3 = %s\n", x3);
			System.out.format("y3 = %s\n", y3);
			System.out.format("x4 = %s\n", x4);
			System.out.format("y4 = %s\n", y4);
			System.out.println("Path2DInfo: "
					   + Path2DInfo.lineSegmentsIntersect
					   (x1, y1, x2, y2, x3, y3, x4, y4)
					   + "; Line2D: "
					   + Line2D.linesIntersect
					   (x1, y1, x2, y2, x3, y3, x4, y4));
		    }
		}
	    }
	    System.out.println("Path2D and Line2D methods differ "
			       + cnt + " times out of 1000000 tries");
	    System.out.println("... OK");
	}

	SplinePathBuilder.CPoint cpoints1c[] = {
	    new SplinePathBuilder.CPoint
	    (SplinePathBuilder.CPointType.MOVE_TO_NEXT),
	    new SplinePathBuilder.CPoint(ifx, ify, 0.0, 360.0, 36),
	    new SplinePathBuilder.CPoint(SplinePathBuilder.CPointType.CLOSE)
	};

	BasicSplinePathBuilder spb = new BasicSplinePathBuilder();
	spb.append(cpoints1c);
	BasicSplinePath2D bpath = spb.getPath();
	java.util.List<Path2DInfo.Entry> entries = Path2DInfo.getEntries(bpath);
	perfTest(bpath, entries);

	double[] vector1 = new double[4];
	double[] vector2 = new double[4];
	double[] vector3 = new double[2];
	double[] vector4 = new double[2];
	Path2DInfo.UValues uv = new Path2DInfo.UValues(0.5);
	Path2DInfo.UValues uv0 = new Path2DInfo.UValues(0.0);
	Path2DInfo.UValues uv1 = new Path2DInfo.UValues(1.0);
	int iu = 0;
	for (Path2DInfo.Entry entry: entries) {
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    double u = iu + 0.5;
	    iu++;
	    int type = entry.getType();
	    coords = entry.getCoords();
	    Path2DInfo.SegmentData data = entry.getData();
	    double kappa1 = Path2DInfo.curvature(0.5,
						 start.getX(), start.getY(),
						 type, coords);
	    double kappa2 = data.curvature(uv);
	    double kappa3 = bpath.curvature(u);
	    bpath.getTangent(u, vector3);

	    System.out.format("kappa1 = %g, kappa2 = %g, kappa3 = %g \n",
			      kappa1, kappa2, kappa3);
	    System.out.format("tangent ... (%g, %g)\n", vector3[0], vector3[1]);
	    Path2DInfo.getTangent(0.5, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.5, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv, vector2, 0);
	    data.getTangent(uv, vector2, 2);
	    System.out.format("(%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (Math.abs(vector1[0]-vector2[0]) > 1.e-10
		|| Math.abs(vector1[1]-vector2[1]) > 1.e-10
		|| Math.abs(vector1[2]-vector2[2]) > 1.e-10
		|| Math.abs(vector1[3]-vector2[3]) > 1.e-10
		|| Math.abs(vector1[0]-vector1[2]) > 1.e-10
		|| Math.abs(vector1[1]-vector1[3]) > 1.e-10
		|| Math.abs(vector2[0]-vector2[2]) > 1.e-10
		|| Math.abs(vector2[1]-vector2[3]) > 1.e-10) {
		throw new Exception ("tangents not consistent");
	    }

	    Path2DInfo.getTangent(0.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv0, vector2, 0);
	    data.getTangent(uv0, vector2, 2);
	    System.out.format("u = 0.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    Path2DInfo.getTangent(1.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(1.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv1, vector2, 0);
	    data.getTangent(uv1, vector2, 2);
	    System.out.format("u = 1.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	}

	bpath = new BasicSplinePath2D();
	bpath.moveTo(10.0, 20.0);
	path.lineTo(20.0, 30.0);
	path.quadTo(25.0, 35.0, 30.0, 40.0);
	path.quadTo(30.0, 40.0, 35.0, 45.0);
	path.quadTo(40.0, 50.0, 40.0, 50.0);
	path.curveTo(45.0, 55.0, 50.0, 60.0, 55.0, 65.0);
	path.curveTo(55.0, 65.0, 60.0, 70.0, 65.0, 75.0);
	path.curveTo(70.0, 80.0, 75.0, 85.0, 75.0, 85.0);
	path.curveTo(75.0, 85.0, 80.0, 90.0, 80.0, 90.0);
	path.curveTo(85.0, 95.0, 85.0, 95.0, 90.0, 100.0);

	entries =  Path2DInfo.getEntries(bpath);

	for (Path2DInfo.Entry entry: entries) {
	    Point2D start = entry.getStart();
	    if (start == null) continue;
	    double u = iu + 0.5;
	    iu++;
	    int type = entry.getType();
	    coords = entry.getCoords();
	    Path2DInfo.SegmentData data = entry.getData();
	    double kappa1 = Path2DInfo.curvature(0.5,
						 start.getX(), start.getY(),
						 type, coords);
	    double kappa2 = data.curvature(uv);
	    double kappa3 = bpath.curvature(u);
	    bpath.getTangent(u, vector3);

	    System.out.format("kappa1 = %g, kappa2 = %g, kappa3 = %g \n",
			      kappa1, kappa2, kappa3);
	    System.out.format("tangent ... (%g, %g)\n", vector3[0], vector3[1]);
	    Path2DInfo.getTangent(0.5, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.5, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv, vector2, 0);
	    data.getTangent(uv, vector2, 2);
	    System.out.format("(%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (Math.abs(vector1[0]-vector2[0]) > 1.e-10
		|| Math.abs(vector1[1]-vector2[1]) > 1.e-10
		|| Math.abs(vector1[2]-vector2[2]) > 1.e-10
		|| Math.abs(vector1[3]-vector2[3]) > 1.e-10
		|| Math.abs(vector1[0]-vector1[2]) > 1.e-10
		|| Math.abs(vector1[1]-vector1[3]) > 1.e-10
		|| Math.abs(vector2[0]-vector2[2]) > 1.e-10
		|| Math.abs(vector2[1]-vector2[3]) > 1.e-10) {
		throw new Exception ("tangents not consistent");
	    }

	    Path2DInfo.getTangent(0.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(0.000001, vector3, 0,
				  start.getX(), start.getY(),
				  type, coords);
	    if (Math.abs(VectorOps.dotProduct(vector1, 0, vector3, 0, 2)
			 - 1.0) > 1.e-10) {
		throw new Exception("bad value, u=0.0");
	    }
	    Path2D spath = new Path2D.Double();
	    spath.moveTo(start.getX(), start.getY());
	    switch(type) {
	    case PathIterator.SEG_LINETO:
		spath.lineTo(coords[0], coords[1]);
		break;
	    case PathIterator.SEG_QUADTO:
		spath.quadTo(coords[0], coords[1], coords[2], coords[3]);
		break;
	    case PathIterator.SEG_CUBICTO:
		spath.curveTo(coords[0], coords[1], coords[2], coords[3],
			      coords[4], coords[5]);
		break;
	    default:
		spath = null;
	    }
	    data.getTangent(uv0, vector2, 0);
	    data.getTangent(uv0, vector2, 2);
	    System.out.format("u = 0.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (spath != null) {
		Path2DInfo.getTangent(spath, Path2DInfo.Location.START,
				      vector4, 0);
		if (Math.abs(vector1[0]-vector4[0]) > 1.e-10
		    || Math.abs(vector1[1]-vector4[1]) > 1.e-10) {
		    throw new Exception("getTangent with location");
		}
	    }
	    Path2DInfo.getTangent(1.0, vector1, 0,  start.getX(), start.getY(),
				  type, coords);
	    Path2DInfo.getTangent(1.0, vector1, 2,  start.getX(), start.getY(),
				  type, coords);
	    data.getTangent(uv1, vector2, 0);
	    data.getTangent(uv1, vector2, 2);
	    Path2DInfo.getTangent(0.999999,
				  vector3, 0,  start.getX(), start.getY(),
				  type, coords);

	    if (Math.abs(VectorOps.dotProduct(vector1, 0, vector3, 0, 2)
			 - 1.0) > 1.e-10) {
		throw new Exception("bad value, u=1.0");
	    }
	    System.out.format("u = 1.0: (%g,%g), (%g,%g), (%g,%g), (%g,%g)\n",
			      vector1[0], vector1[1],
			      vector2[0], vector2[1],
			      vector1[2], vector1[3],
			      vector2[2], vector2[3]);
	    if (spath != null) {
		Path2DInfo.getTangent(spath, Path2DInfo.Location.END,
				       vector4, 0);
		if (Math.abs(vector1[0]-vector4[0]) > 1.e-10
		    || Math.abs(vector1[1]-vector4[1]) > 1.e-10) {
		    throw new Exception("getTangent with location");
		}
	    }
	}

	{
	    double x1 = 10.0, y1 = 30.0;
	    double x2 = 70.0, y2 = 80.0;
	    double x3 = 50.0, y3 = -10.0;
	    double x4 = 40.0, y4 = 100.0;
	    double[] array = new double[8];

	    if (Path2DInfo.getLineIntersectionUV(x1, y1, x2, y2,
						 x3, y3, x4, y4,
						 array, 1)) {
		double u = array[1];
		double v = array[2];
		double oneMu = 1 - u;
		double oneMv = 1 - v;
		array[6] = (x1*oneMu + x2*u);
		array[7] = (y1*oneMu + y2*u);
		if (Math.abs(array[6] - (x3*oneMv + x4*v)) > 1.e-10
		    || Math.abs(array[7] - (y3*oneMv + y4*v)) > 1.e-10) {
		    throw new Exception("intersection points differ");
		}
		System.out.println("intersection: x = " +array[6]
				   + ", y = " + array[7]);
		System.out.println("intersection: u = " +array[1]
				   + ", v = " + array[2]);

	    } else {
		throw new Exception("getLineIntersectionUV returned false");
	    }
	    if (Path2DInfo.getLineIntersectionXY(x1, y1, x2, y2,
						 x3, y3, x4, y4,
						 array, 1)) {
		if (Math.abs(array[6] - array[1]) > 1.e-10
		    || Math.abs(array[7] - array[2]) > 1.e-10) {
		    throw new Exception("intersection point incorrect");
		}
		System.out.println("intersection: x = " +array[1]
				   + ", y = " + array[2]);
	    } else {
		throw new Exception("getLineIntersection returned false");
	    }
	}

	{
	    double[] array = new double[18+3];
	    Path2D tpath1 = new Path2D.Double();
	    Path2D tpath2 = new Path2D.Double();
	    Path2D tpath3 = new Path2D.Double();
	    tpath1.moveTo(10.0, 20.0);
	    tpath1.lineTo(11.0, 22.0);
	    tpath2.moveTo(10.0, 20.0);
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.moveTo(10.0, 20.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);
	    Point2D point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    Path2D[] paths = {tpath1, tpath2, tpath3};
	    for (Path2D tpath: paths) {
		Point2D start = Path2DInfo.getStart(tpath);
		if (start.getX() != 10.0 || start.getY() != 20.0) {
		    throw new Exception("wrong starting point");
		}
	    }


	    array[0] =  1.0 / Math.sqrt(5.0);
	    array[1] = 2.0 / Math.sqrt(5.0);
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.START, array, 2);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.START, array, 4);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.START, array, 6);
	    for (int i = 2; i < 8; i++) {
		if (Math.abs(array[i] - array[i%2]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);
	    double etangents[] = {
		1.0/Math.sqrt(5.0), 2.0/Math.sqrt(5.0),
		2.0/Math.sqrt(4.0+25.0), 5.0/Math.sqrt(4.0+25.0),
		5.0/Math.sqrt(25.0+49.0), 7.0/Math.sqrt(25.0+49.0)
	    };
	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }
	    tpath1.closePath();
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 10.0 && point.getY() != 20.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.closePath();
	    tpath3.closePath();
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    double ctangents[] = {
		-1.0 / Math.sqrt(5.0), -2.0 / Math.sqrt(5.0),
		-3.0 / Math.sqrt(9.0 + 49.0), -7.0 / Math.sqrt(9.0 + 49.0),
		-8.0 / Math.sqrt(64.0 + 196.0), -14.0 / Math.sqrt(64 + 196.0)
	    };

	    for (int i = 0; i < 6; i++) {
		if (Math.abs(array[i] - ctangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }

	    tpath1.lineTo(11.0, 22.0);
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);

	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }

	    tpath1.moveTo(10.0, 20.0);
	    tpath1.lineTo(11.0, 22.0);
	    point = tpath1.getCurrentPoint();
	    if (point.getX() != 11.0 && point.getY() != 22.0) {
		throw new Exception("currentPoint");
	    }
	    tpath2.moveTo(10.0, 20.0);
	    tpath2.quadTo(11.0, 22.0, 13.0, 27.0);
	    tpath3.moveTo(10.0, 20.0);
	    tpath3.curveTo(11.0, 22.0, 13.0, 27.0, 18.0, 34.0);

	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.END, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.END, array, 2);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.END, array, 4);

	    for (int i = 0 ; i < 6; i++) {
		if (Math.abs(array[i] - etangents[i]) > 1.e-10) {
		    throw new Exception("wrong tangent");
		}
	    }


	    Arrays.fill(array, 0.0);
	    Path2DInfo.getTangent(tpath1, Path2DInfo.Location.START, array, 0);
	    Path2DInfo.getTangent(tpath2, Path2DInfo.Location.START, array, 3);
	    Path2DInfo.getTangent(tpath3, Path2DInfo.Location.START, array, 6);
	    Path2DInfo.getNormal(tpath1, Path2DInfo.Location.START, array, 9);
	    Path2DInfo.getNormal(tpath2, Path2DInfo.Location.START, array, 12);
	    Path2DInfo.getNormal(tpath3, Path2DInfo.Location.START, array, 15);
	    for (int i = 0; i < 3; i++) {
		int offset1 = i*3;
		int offset2 = offset1 + 9;
		VectorOps.crossProduct(array, 18,
				       array, offset1,
				       array, offset2);
		if (Math.abs(array[18]) > 1.e-10 ||
		    Math.abs(array[19]) > 1.e-10 ||
		    Math.abs(array[20] - 1.0) > 1.e-10) {
		    System.out.format
			("(%g, %g, %g) X (%g, %g, %g) = (%g, %g, %g)\n",
			 array[i], array[i+1], array[i+2],
			 array[i+9], array[i+1+9], array[i+2+9],
			 array[18], array[19], array[20]);
		    throw new Exception("bad normal");
		}
	    }
	}

	System.out.println("check shift closed path ...");

	path = new Path2D.Double();
	path.moveTo(-8.0, 0.0);
	path.lineTo(-8.0, -4.0);
	path.lineTo(0.0, -4.0);
	path.quadTo(8.0, -4.0, 8.0, 0.0);
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0);

	path2 = Path2DInfo.shiftClosedPath(path, 8.0, 0.0);

	if (path2 != null) throw new Exception("null path expected");
	
	path.closePath();	

	path2 = Path2DInfo.shiftClosedPath(path, 8.0, 0.0);
	double[][] expecting = {
	    {8.0, 0.0},
	    {8.0, 2.0, 2.0, 4.0, 0.0, 4.0},
	    {-2.0, 4.0, -8.0, 4.0, -8.0, 2.0},
	    {-8.0, 0.0},
	    {-8.0, -4.0},
	    {0.0, -4.0},
	    {8.0, -4.0, 8.0, 0.0},
	    {}
	};

	PathIterator pi = path2.getPathIterator(null);
	int cnt = 0;
	double[] pcoords = new double[6];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 2; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 2; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 4; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}
	// Path2DInfo.printSegments(path2);

	path.lineTo(-7.0, 0.0);
	path.lineTo(-7.0, 1.0);
	Path2D path3 = Path2DInfo.shiftClosedPath(path, 8.0, 0.0);

	double[] coords2 = new double[6];
	double[] coords3 = new double[6];

	PathIterator pi2 = path2.getPathIterator(null);
	PathIterator pi3 = path3.getPathIterator(null);
	while (!pi2.isDone() && !pi3.isDone()) {
	    int type2 = pi2.currentSegment(coords2);
	    int type3 = pi3.currentSegment(coords3);
	    if (type2 != type3) throw new Exception("types");
	    switch(type2) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 2; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 2; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 4; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 6; i++) {
		    if (coords2[i] != coords3[i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		break;
	    }
	    pi2.next();
	    pi3.next();
	}
	if (pi2.isDone() != pi3.isDone()) {
	    throw new Exception("path segments");
	}

	path.reset();

	path.moveTo(10.0, 20.0);
	path.lineTo(30.0, 40.0);
	path.moveTo(40.0, 40.0);
	path.lineTo(50.0, 40.0);
	path.lineTo(50.0, 50.0);
	path.moveTo(40.0, 40.0);
	path.closePath();
	path.moveTo(-8.0, 0.0);
	path.lineTo(-8.0, -4.0);
	path.lineTo(0.0, -4.0);
	path.quadTo(8.0, -4.0, 8.0, 0.0);
	path.curveTo(8.0, 2.0, 2.0, 4.0, 0.0, 4.0);
	path.curveTo(-2.0, 4.0, -8.0, 4.0, -8.0, 2.0);
	path.closePath();	

	path2 = Path2DInfo.shiftClosedPath(path, 8.0, 0.0);

	pi = path2.getPathIterator(null);
	cnt = 0;
	pcoords = new double[6];

	while (!pi.isDone()) {
	    switch(pi.currentSegment(pcoords)) {
	    case PathIterator.SEG_MOVETO:
		for (int i = 0; i < 2; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_LINETO:
		for (int i = 0; i < 2; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_QUADTO:
		for (int i = 0; i < 4; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CUBICTO:
		for (int i = 0; i < 6; i++) {
		    if (pcoords[i] != expecting[cnt][i]) {
			throw new Exception("coords");
		    }
		}
		break;
	    case PathIterator.SEG_CLOSE:
		if (expecting[cnt].length != 0) {
		    throw new Exception("coords");
		}
		break;
	    }
	    pi.next();
	    cnt++;
	}

	System.out.println("... OK");
	System.out.println("Convex Hull");
	double hx = 10.0, hy = 20.0;
	double hcoords1a[] = {30.0, 40.0};
	double hcoords1b[] = {-10.0, 40.0};
	double hcoords1c[] = {-10.0, -40.0};
	double hcoords1d[] = {30.0, -40.0};

	double hexpecting1a[] = {10.0, 20.0, 30.0, 40.0};
	double hexpecting1b[] = {-10.0, 40.0, 10.0, 20.0};
	double hexpecting1c[] = {-10.0, -40.0, 10.0, 20.0};
	double hexpecting1d[] = {10.0, 20.0, 30.0, -40.0};
	Path2D hpath = Path2DInfo.convexHull(hx, hy, hcoords1a, 1);

	int hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1b, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1c, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords1d, 1);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords1d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting1d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting1d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	double hcoords2a[] = {21.0, 22.0, 30.0, 40.0};
	double hcoords2b[] = {-10.0, 40.0, 21.0, 22.0};
	double hcoords2c[] = {-10.0, -40.0, 21.0, 22.0};
	double hcoords2d[] = {30.0, -40.0, 21.0, 22.0};

	double hexpecting2a[] = {10.0, 20.0,  21.0, 22.0, 30.0, 40.0};
	double hexpecting2b[] = {-10.0, 40.0, 10.0, 20.0, 21.0, 22.0};
	double hexpecting2c[] = {-10.0, -40.0, 21.0, 22.0, 10.0, 20.0};
	double hexpecting2d[] = {10.0, 20.0, 30.0, -40.0, 21.0, 22.0};
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2a, 2);

	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2b, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2c, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords2d, 2);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords2d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting2d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting2d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}

	double atx = -100.0;
	double aty = -100.0;
	double atcoords[] = {100.0, -100.0, 100.0, 100.0, -100.0, 100.0};
	if (insideHull(atx, aty, atcoords, 6, 0.0, 0.0) == false) {
	    throw new Exception("insideHull");
	}

	double hcoords3a[] = {30.0, 20.1, 40.0, 22.2, 50.0, 22.3};
	double hcoords3b[] = {30.0, 20.1, 40.0, -22.2, 50.0, 22.3};
	double hcoords3c[] = {30.0, -20.1, 40.0, 22.2, 50.0, 22.3};
	double hcoords3d[] = {20.0, 40.0, 30.0, 60.0, 60.0, 120.0};
	double hcoords3e[] = {30.0, 20.4, 40.0, 22.2, 50.0, 22.3};
	double hcoords3f[] = {30.0, 20.4, 40.0, -22.2, 50.0, 22.3};
	double hcoords3g[] = {30.0, -20.4, 40.0, 22.2, 50.0, 22.3};
	double hcoords3h[] = {30.0, 20.1, 40.0, 20.2, 50.0, 20.0};

	double hexpecting3a[] = {10.0, 20.0, 30.0, 20.1, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3b[] = {10.0, 20.0, 40.0, -22.2, 50.0, 22.3};
	double hexpecting3c[] = {10.0, 20.0, 30.0, -20.1, 50.0, 22.3,



				 40.0, 22.2};
	double hexpecting3d[] = {10.0, 20.0, 60.0, 120.0};
	double hexpecting3e[] = {10.0, 20.0, 30.0, 20.4, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3f[] = {10.0, 20.0, 40.0, -22.2, 50.0, 22.3};
	double hexpecting3g[] = {10.0, 20.0, 30.0, -20.4, 50.0, 22.3,
				 40.0, 22.2};
	double hexpecting3h[] = {10.0, 20.0, 50.0, 20.0, 40.0, 20.2};

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3a, 3);

	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3a: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3a[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3a[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3b, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3b: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3b[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3b[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3b.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3c, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3c: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3c[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3c[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3c.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3d, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3d: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3d[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3d[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3d.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3e, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3e: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3e[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3e[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3e.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3f, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3f: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3f[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3f[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3f.length) {
	    throw new Exception("hull failed: path too short");
	}

	hpath = Path2DInfo.convexHull(hx, hy, hcoords3g, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3g: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3g[hi++] != hp.getX()) {
		    System.out.format("hi = %d, hp.getX() = %g, expected %g\n",
				      (hi-1), hp.getX(), hexpecting3g[(hi-1)]);
		    throw new Exception("hull failed");
		}
		if (hexpecting3g[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3g.length) {
	    throw new Exception("hull failed: path too short");
	}
	hpath = Path2DInfo.convexHull(hx, hy, hcoords3h, 3);
	hi = 0;
	for (Path2DInfo.Entry entry: Path2DInfo.getEntries(hpath)) {
	    if (entry.getType() != PathIterator.SEG_CLOSE) {
		Point2D hp = entry.getEnd();
		System.out.format("hcoords3h: (%g, %g)\n",
				  hp.getX(), hp.getY());
		if (hexpecting3h[hi++] != hp.getX()) {
		    throw new Exception("hull failed");
		}
		if (hexpecting3h[hi++] != hp.getY()) {
		    throw new Exception("hull failed");
		}
	    }
	}
	if (hi != hexpecting3h.length) {
	    throw new Exception("hull failed: path too short");
	}

	DoubleRandomVariable rv = new
	    UniformDoubleRV(-100.0, true, 100.0, true);
	for (int m = 10; m < 1000; m++) {
	    double[] hcoords = new double[2*m];
	    for (int k = 0; k < 2*m; k += 2) {
		hcoords[k] = rv.next();
		hcoords[k+1] = rv.next();
	    }
	    Path2D p1 = convexHullGW(hx, hy, hcoords, m);
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hcoords, m);
	    PathIterator pit1 = p1.getPathIterator(null);
	    PathIterator pit2 = p2.getPathIterator(null);
	    double[] crd1 = new double[2];
	    double[] crd2 = new double[2];
	    while (!pit1.isDone() && !pit2.isDone()) {
		crd1[0] = 0.0;
		crd1[1] = 0.0;
		crd2[0] = 0.0;
		crd2[1] = 0.0;
		if (pit1.currentSegment(crd1) != pit2.currentSegment(crd2)) {
		    throw new Exception("pit1 & pit2 differ in type");
		}
		for (int k = 0; k < 2; k++) {
		    if (Math.abs(crd1[k] - crd2[k]) > 1.e-10) {
			throw new Exception("pit1 & pit2 coords differ");
		    }
		}
		pit1.next();
		pit2.next();
	    }
	    if (pit1.isDone() != pit2.isDone()) {
		throw new Exception("pit1 and pit2 differ in "
				    + "number of segments");
	    }
	}

	int limit = 3;
	double[] hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	long t0 = System.nanoTime();
	for (int m = 0; m < 1000000; m++) {
	    Path2D p1 = convexHullGW(hx, hy, hhcoords, limit);
	}
	long t1 = System.nanoTime();

	for (int m = 0; m < 1000000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	long t2 = System.nanoTime();
	System.out.println("Test with limit set to 3");
	System.out.format("GW t1-t0 = %d, Andrew t2-t1=%d\n",
			  t1-t0, t2-t1);
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 1000000; m++) {
	    Path2D p1 = convexHullGW(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 1000000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("GW t1-t0 = %d, Andrew t2-t1=%d\n",
			  t1-t0, t2-t1);

	limit = 385;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=385): t1-t0 = %d, "
			  + "t2-t1=%d\n", t1-t0, t2-t1);

	limit = 512;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=512): t1-t0 = %d, "
			  + "t2-t1=%d\n",
			  t1-t0, t2-t1);

	limit = 385;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=385): t1-t0 = %d, "
			  + "t2-t1=%d\n", t1-t0, t2-t1);

	limit = 512;
	hhcoords = new double[2*limit];
	for (int m = 0; m < hhcoords.length; m++) {
	    hhcoords[m] = rv.next();
	}

	t0 = System.nanoTime();
	for (int m = 0; m < 100000; m++) {
	    Path2D p1 = convexHullAndrew(hx, hy, hhcoords, limit);
	}
	t1 = System.nanoTime();

	for (int m = 0; m < 100000; m++) {
	    Path2D p2 = Path2DInfo.convexHull(hx, hy, hhcoords, limit);
	}
	t2 = System.nanoTime();
	System.out.format("Aki-Toussaint Test (limit=512): t1-t0 = %d, "
			  + "t2-t1=%d\n",
			  t1-t0, t2-t1);

	System.out.println("... OK");
	System.exit(0);
    }
}
