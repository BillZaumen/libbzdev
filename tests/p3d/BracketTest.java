import org.bzdev.p3d.*;
import org.bzdev.p3d.P3d.Rectangle;
import org.bzdev.math.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/*
 * This reproduced a bug in which the embedded test failed.  The
 * failure generated the following:
 * ------------------
 * not an embedded manifold
 * Triangle (66.1803,104.021,15.0000)-(66.5114,103.910,8.00000)
 *         -(66.5114,103.910,15.0000)
 * StackTrace for triangle creation:
 *   java.lang.Thread.getStackTrace(Thread.java:1589)
 *   org.bzdev.p3d.Model3D$1.run(Model3D.java:2938)
 *   org.bzdev.p3d.Model3D$1.run(Model3D.java:2936)
 *   java.security.AccessController.doPrivileged(Native Method)
 *   org.bzdev.p3d.Model3D.addTriangle(Model3D.java:2935)
 *   org.bzdev.p3d.Model3D.addTriangle(Model3D.java:2974)
 *   org.bzdev.p3d.P3d$Rectangle.addFlippedV(P3d.java:74)
 *   BracketTest.addAnnulus(BracketTest.java:274)
 *   BracketTest.main(BracketTest.java:337)
 * Triangle (64.3412,109.620,8.00000)-(63.1287,104.754,8.00000)
 *          -(63.9109,109.692,8.00000)
 * StackTrace for triangle creation:
 *   java.lang.Thread.getStackTrace(Thread.java:1589)
 *   org.bzdev.p3d.Model3D$1.run(Model3D.java:2938)
 *   org.bzdev.p3d.Model3D$1.run(Model3D.java:2936)
 *   java.security.AccessController.doPrivileged(Native Method)
 *   org.bzdev.p3d.Model3D.addTriangle(Model3D.java:2935)
 *   org.bzdev.p3d.Model3D.addTriangle(Model3D.java:2974)
 *   BracketTest.addAnnulus(BracketTest.java:269)
 * ------------------
 * The only point of the first triangle that could possibly intersect
 * the second triangle is (66.5114,103.910,8.00000) - all the other points
 * have higher z values but the second triangle's verticies have a z value
 * of 8.00000. Meanwhile, the x values for the second triangl are all
 * less than 66 but the x value of the point from the first triangle is
 * 66.5114. So they must be disjoint.
 * 
 */

public class BracketTest {

    static double h1 = 40.0;
    static double w1 = 7.5;
    static double dtab = 15.0;
    static double d1 = 10.0;
    static double atab = 7.0;

    static final double r3o2 = Math.sqrt(3.0)/2.0;


    static void addHexagon(Model3D m3d, double sideLength,
			   double xc, double yc, double z)
    {
	double rInner = r3o2*sideLength;
	double halfSideLength = sideLength/2.0;
	
	m3d.addTriangle(xc, yc, z,
			xc-halfSideLength, yc-rInner, z,
			xc+halfSideLength, yc-rInner, z);
	m3d.addTriangle(xc, yc, z,
			xc+halfSideLength, yc-rInner, z,
			xc+sideLength, yc, z);
	m3d.addTriangle (xc, yc, z,
			 xc+sideLength, yc, z,
			 xc+halfSideLength, yc+rInner, z);
	m3d.addTriangle (xc, yc, z,
			 xc+halfSideLength, yc+rInner, z,
			 xc-halfSideLength, yc+rInner, z);
	m3d.addTriangle (xc, yc, z,
			 xc-halfSideLength, yc+rInner, z,
			 xc - sideLength, yc, z);
	m3d.addTriangle (xc, yc, z,
			 xc - sideLength, yc, z,
			 xc-halfSideLength, yc-rInner, z);
    }

    static double[] xarray = new double[361];
    static double[] yarray = new double[361];

    static double[] xiarray = new double[361];
    static double[] yiarray = new double[361];

    static void addFlippedHexagon(Model3D m3d, double sideLength,
				  double xc, double yc, double z,
				  double ri, double r)
    {
	double rInner = r3o2*sideLength;
	double halfSideLength = sideLength/2.0;

	for (int i = 0; i < 360; i++) {
	    double angle = (Math.PI/180.0)*i;
	    double cosAngle = Math.cos(angle);
	    double sinAngle = Math.sin(angle);
	    double x = r * cosAngle + xc;
	    double y = r * sinAngle + yc;
	    xarray[i] = x;
	    yarray[i] = y;
	    x = ri * cosAngle + xc;
	    y = ri * sinAngle + yc;
	    xiarray[i] = x;
	    yiarray[i] = y;
	}
	xarray[360] = xarray[0];
	yarray[360] = yarray[0];
	xiarray[360] = xiarray[0];
	yiarray[360] = yiarray[0];

	double hx[] = new double[7];
	double hy[] = new double[7];
	int aindex[] = {30, 90, 150, 210, 270, 330};

	hx[0] = xc + sideLength;
	hy[0] = yc;
	hx[1] = xc + halfSideLength;
	hy[1] = yc + rInner;
	hx[2] = xc - halfSideLength;
	hy[2] = yc + rInner;
	hx[3] = xc - sideLength;
	hy[3] = yc;
	hx[4] = xc - halfSideLength;
	hy[4] = yc - rInner;
	hx[5] = xc + halfSideLength;
	hy[5] = yc - rInner;
	hx[6] = hx[0];
	hy[6] = hy[0];

	for (int i = 0; i < 6; i++) {
	    int ip1 = i + 1;
	    m3d.addTriangle(hx[i], hy[i], z,
			    xarray[aindex[i]], yarray[aindex[i]], z,
			    hx[ip1], hy[ip1], z);
	}

	for (int k = 0; k < 6; k++) {
	    for (int i = 0; i < 30; i++) {
		int kk = k * 60;
		int kki = kk + i;
		int kkip1 = kki + 1;
		m3d.addTriangle(hx[k], hy[k], z,
				xarray[kki], yarray[kki], z,
				xarray[kkip1], yarray[kkip1], z);
	    }
	    for (int i = 0; i < 30; i++) {
		int kp1 = k + 1;
		int kk = k * 60 + 30;
		int kki = kk + i;
		int kkip1 = kki + 1;
		m3d.addTriangle(xarray[kki], yarray[kki], z,
				xarray[kkip1], yarray[kkip1], z,
				hx[kp1], hy[kp1], z);
	    }
	}
    }

    static void addHexagonSides(Model3D m3d, double sideLength,
				double xc, double yc, double z1, double z2)
    {
	double rInner = r3o2*sideLength;
	double halfSideLength = sideLength/2.0;
	
	Rectangle.addV(m3d, xc - halfSideLength, yc - rInner, z1,
		       xc + halfSideLength, yc - rInner, z2);
	Rectangle.addFlippedV(m3d, xc - halfSideLength, yc + rInner, z1,
			      xc + halfSideLength, yc + rInner, z2);
	
    }


    static void connectTab(Model3D m3d, double sideLength,
			   double xc, double yc, double z1, double z2,
			   int angle, double x, double y)
    {
	double rInner = r3o2*sideLength;
	double halfSideLength = sideLength/2.0;
	switch(angle) {
	case 30:
	    m3d.addTriangle(xc+sideLength, yc, z2,
			    x, y, dtab+d1,
			    x, y+h1, dtab+d1);
	    m3d.addTriangle(xc+sideLength, yc, z2,
			    x, y+h1, dtab+d1,
			    xc+halfSideLength, yc+rInner, z2);
	    m3d.addFlippedTriangle(xc+sideLength, yc, z1,
				   x, y, dtab,
				   x, y+h1, dtab);
	    m3d.addFlippedTriangle(xc+sideLength, yc, z1,
			    x, y+h1, dtab,
			    xc+halfSideLength, yc+rInner, z1);
	    Rectangle.addV(m3d, xc + sideLength, yc, z1, x, y, dtab + d1);
	    Rectangle.addV(m3d, xc + halfSideLength, yc + rInner, z2,
			   x, y+h1, dtab);
		   
	    break;
	case 150:
	    m3d.addFlippedTriangle(x, y+h1, dtab+d1,
			    xc - halfSideLength, yc + rInner, z2,
			    xc - sideLength, yc, z2);
	    m3d.addTriangle(x, y, dtab+d1,
			    xc - sideLength, yc, z2,
			    x, y+h1, z2);
	    m3d.addTriangle(x, y+h1, dtab,
			    xc - halfSideLength, yc + rInner, z1,
			    xc - sideLength, yc, z1);
	    m3d.addFlippedTriangle(x, y, dtab,
			    xc - sideLength, yc, z1,
			    x, y+h1, z1);
	    Rectangle.addV(m3d, xc - halfSideLength, yc + rInner, z1,
			   x, y + h1, dtab + d1);
	    Rectangle.addV(m3d, xc - sideLength, yc, z2,
			   x, y, dtab);
	    break;
	case 210:
	    m3d.addTriangle(x, y, dtab+d1,
			    xc - halfSideLength, yc - rInner, z2,
			    xc - sideLength, yc, z2);
	    m3d.addTriangle(x, y, dtab+d1,
			    xc - sideLength, yc, z2,
			    x, y+h1, z2);
	    m3d.addFlippedTriangle(x, y, dtab,
			    xc - halfSideLength, yc - rInner, z1,
			    xc - sideLength, yc, z1);
	    m3d.addFlippedTriangle(x, y, dtab,
			    xc - sideLength, yc, z1,
			    x, y+h1, z1);
	    Rectangle.addV(m3d, xc - sideLength, yc, z1,
			   x, y+h1, z2);
	    Rectangle.addV(m3d, xc - halfSideLength, yc - rInner, z2,
			   x, y, dtab);
	    break;
	case 330:
	    m3d.addTriangle(x, y, dtab+d1,
			    xc + sideLength, yc, z2,
			    xc + halfSideLength, yc-rInner, z2);
	    m3d.addTriangle(x, y+h1, dtab+d1,
			    xc + sideLength, yc, z2,
			    x, y, dtab+d1);
	    m3d.addFlippedTriangle(x, y, dtab,
			    xc + sideLength, yc, z1,
			    xc + halfSideLength, yc-rInner, z1);
	    m3d.addFlippedTriangle(x, y+h1, dtab,
			    xc + sideLength, yc, z1,
			    x, y, dtab);
	    Rectangle.addV(m3d, xc + halfSideLength, yc - rInner, z1,
			   x, y, dtab + d1);
	    Rectangle.addV(m3d, xc + sideLength, yc, z2,
			   x, y+h1, dtab);
	    break;
	default:
	    throw new RuntimeException("illegal switch: " + angle);
	}
    }
			   
    static void addTab(Model3D m3d, double x, double y, boolean invert) {
	if (invert) {
	    Rectangle.addFlippedV(m3d, x, y, 0.0, x, y+h1, dtab);
	    Rectangle.addV(m3d, x+w1, y, 0.0, x+w1, y+h1, dtab);
	    Rectangle.addV(m3d, x+w1, y, dtab, x+w1, y+h1, dtab + d1);
	    Rectangle.addH(m3d, 0.0, x+w1, y, x, y+h1);
	    Rectangle.addFlippedH(m3d, dtab+d1, x+w1, y, x, y+h1);
	    Rectangle.addFlippedV(m3d, x+w1, y, 0.0, x, y, dtab);
	    Rectangle.addFlippedV(m3d, x+w1, y, dtab, x, y, dtab + d1);
	    Rectangle.addV(m3d, x+w1, y+h1, 0.0, x, y+h1, dtab);
	    Rectangle.addV(m3d, x+w1, y+h1, dtab, x, y+h1, dtab + d1);
	} else {
	    Rectangle.addV(m3d, x, y, 0.0, x, y+h1, dtab);
	    Rectangle.addFlippedV(m3d, x-w1, y, 0.0, x-w1, y+h1, dtab);
	    Rectangle.addFlippedV(m3d, x-w1, y, dtab, x-w1, y+h1, dtab + d1);
	    Rectangle.addFlippedH(m3d, 0.0, x-w1, y, x, y+h1);
	    Rectangle.addH(m3d, dtab+d1, x-w1, y, x, y+h1);
	    Rectangle.addV(m3d, x-w1, y, 0.0, x, y, dtab);
	    Rectangle.addV(m3d, x-w1, y, dtab, x, y, dtab + d1);
	    Rectangle.addFlippedV(m3d, x-w1, y+h1, 0.0, x, y+h1, dtab);
	    Rectangle.addFlippedV(m3d, x-w1, y+h1, dtab, x, y+h1, dtab + d1);
	}
    }


    public static void addAnnulus(Model3D m3d, double z1, double z2)
    {
	for (int i = 0; i < 360; i++) {
	    double x1 = xarray[i];
	    double y1 = yarray[i];
	    double xi1 = xiarray[i];
	    double yi1 = yiarray[i];
	    int ip1 = i + 1;

	    double x2 = xarray[ip1];
	    double y2 = yarray[ip1];
	    double xi2 = xiarray[ip1];
	    double yi2 = yiarray[ip1];
	    
	    m3d.addTriangle(x1, y1, z1,
			    xi1, yi1, z1,
			    xi2, yi2, z1);
	    m3d.addTriangle(x1, y1, z1,
			    xi2, yi2, z1,
			    x2, y2, z1);
	    Rectangle.addV(m3d, x1, y1, z1,
			   x2, y2, z2);
	    Rectangle.addFlippedV(m3d, xi1, yi1, z1,
				  xi2, yi2, z2);

	}
    }

    public static void addDisk(Model3D m3d, double xc, double yc, double z) {
	for (int i = 0; i < 360; i++) {
	    int ip1 = i+1;
	    m3d.addTriangle(xc, yc, z,
			    xiarray[ip1], yiarray[ip1], z,
			    xiarray[i], yiarray[i], z);
	}
    }

    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D();
	m3d.setStackTraceMode(true);

	double wireWidth = 120.0;
	double wireLength = 170.0;

	double sideLength = 30.0;
	
	double r1 = 20;
	double r2 = 25;

	double tab1x = 0.0;
	double tab2x = wireWidth;
	double tab1y = 0.0;
	double tab2y = wireLength - h1;

	addTab(m3d, tab1x, tab1y, false);
	addTab(m3d, tab1x, tab2y, false);
	addTab(m3d, tab2x, tab1y, true);
	addTab(m3d, tab2x, tab2y, true);

	double xCenter = wireWidth/2.0;
	double yCenter = wireLength/2.0;

	System.out.println("xCenter = " + xCenter);
	System.out.println("yCenter = " + yCenter);

	addHexagon(m3d, sideLength, xCenter, yCenter, dtab+d1);

	addHexagonSides(m3d, sideLength, xCenter, yCenter, dtab, dtab+d1);


	connectTab(m3d, sideLength, xCenter, yCenter, dtab, dtab+d1,
		   30, tab2x, tab2y);

	connectTab(m3d, sideLength, xCenter, yCenter, dtab, dtab+d1,
		   210, tab1x, tab1y);

	connectTab(m3d, sideLength, xCenter, yCenter, dtab, dtab+d1,
		   150, tab1x, tab2y);

	connectTab(m3d, sideLength, xCenter, yCenter, dtab, dtab+d1,
		   330, tab2x, tab1y);

	addFlippedHexagon(m3d, sideLength, xCenter, yCenter, dtab, r1, r2);

	addAnnulus(m3d, dtab - atab, dtab);

	addDisk(m3d, xCenter, yCenter, dtab);

	List<Model3D.Edge> elist = m3d.verifyClosed2DManifold();
	if (elist != null) {
	    System.err.println("not a closed manifold");
	    P3d.printEdgeErrors(System.out, elist);
	    System.exit(1);
	}

	List<Model3D.Triangle> tlist = m3d.verifyEmbedded2DManifold();
	if (elist == null && tlist != null) {
	    System.err.println("not an embedded manifold");
	    P3d.printTriangleErrors(System.out, tlist);
	    System.exit(1);
	}
    }
}