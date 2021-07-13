import org.bzdev.math.*;
import org.bzdev.geom.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.*;
import org.bzdev.anim2d.*;
import org.bzdev.util.units.MKS;
import org.bzdev.util.CopyUtilities;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlackHole {

    public static class UCircle implements Graph.UserDrawable {
	static double radius = 14;
	public Shape toShape(boolean arg1, boolean arg2) {
	    return new Ellipse2D.Double(-radius, -radius, 2*radius, 2*radius); 
	}
    }

    public static class Disk extends AnimationObject2D {
	UCircle disk;
	OrbitParms op;
	CubicSpline rf;
	CubicSpline xf;
	CubicSpline yf;
	CubicSpline tf;
	double wavelength = 0.0;
	double t = 0.0;
	Stroke stroke = new BasicStroke(1.0f);
	public Disk(Animation2D a2d, String name, boolean intern) {
	    super(a2d, name, intern);
	    disk = new UCircle();
	}
	public void init(OrbitParms op, double wavelength,
			 CubicSpline tf, CubicSpline rf,
			 CubicSpline xf, CubicSpline yf)
	{
	    this.op = op;
	    this.wavelength = wavelength;
	    this.rf = rf;
	    this.xf = xf;
	    this.yf = yf;
	    this.tf = tf;
	}
	
	protected void update(double t, long simtime) {
	    this.t = t;
	}
	public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	    Stroke savedStroke = g2d.getStroke();
	    Color savedColor = g2d.getColor();
	    try {
		double tau = tf.inverseAt(t);
		double x = xf.valueAt(tau);
		double y = yf.valueAt(tau);
		double r = rf.valueAt(tau);
		g2d.setStroke(stroke);
		g2d.setColor(op.redshift(graph, wavelength, r));
		graph.fill(g2d, disk, x, y);
		graph.draw(g2d, disk, x, y);
	    } finally {
		g2d.setStroke(savedStroke);
		g2d.setColor(savedColor);
	    }
	}
    }

    public static class CoordCircle extends SplinePath2D {
	double r0;
	double r;

	public Paint getPaintGCS(Graph graph, double lambda, int m) {
	    float[] fractions = new float[m+2];
	    Color[] colors = new Color[m+2];
	    fractions[0] = 0.0F;
	    colors[0] = Color.BLACK;
	    fractions[1] = (float)(r0/r);
	    colors[1] = Color.BLACK;
	    for (int i = 0; i < m; i++) {
		double rr = r0 + ((i+1) *(r-r0))/(m);
		double factor = 1.0/Math.sqrt(1.0 - r0/rr);
		fractions[i+2] = (float)(rr/r);
		colors[i+2] =
		    Colors.getColorByWavelength(graph.getColorModel(),
						factor * lambda)
		    .darker();
	   } 
	    return new RadialGradientPaint(0.0F, 0.0F, (float)(r),
					   fractions, colors);
	}


	public Color findRedshiftedColor(Graphics2D g2d, double lambda) {
	    double factor = 1.0/Math.sqrt(1.0 - r0/r);
	    return Colors.getColorByWavelength
		(g2d.getDeviceConfiguration().getColorModel(),
		 factor * lambda);
	}

	private double[] getX(double r, double r0, int n) {
	    double result[] = new double[n];
	    for (int i = 0; i < n; i++) {
		double phi = (2.0 * Math.PI * i)/n;
		result[i] = r * Math.cos(phi);
	    }
	    return result;
	}
	private double[] getY(double r, double r0, int n) {
	    double result[] = new double[n];
	    for (int i = 0; i < n; i++) {
		double phi = (2.0 * Math.PI * i)/n;
		result[i] = r * Math.sin(phi);
	    }
	    return result;
	}

	public CoordCircle(double s, double r0, int n) {
	    super();
	    this.r0 = r0;
	    r = getRForDistance(s, r0);
	    addCycle(getX(r, r0, n), getY(r, r0, n));
	}
    }

    /*
    public static class CoordRing extends SplinePath2D {
	double r1;
	double r2;
	double r0;

	private Color findRedshiftedColor(Graph g, double lambda,
					  boolean useR1) {
	    double r = useR1? r1: r2;
	    double factor = 1.0/Math.sqrt(1.0 - r0/r);
	    return Colors.getColorByWavelength(g.getColorModel(),
					       factor * lambda);
	}

	public Paint getPaint(Graph g, double lambda) {
	    Color innerColor = findRedshiftedColor(g, lambda, true);
	    Color outerColor = findRedshiftedColor(g, lambda, false);
	    
	    double scale = g.getXScale();

	    double ur1 = r1*scale;
	    double ur2 = r2*scale;
	    float[] fractions = {0.0F, (float)(ur1/ur2), 1.0F};
	    Color[] colors = {Color.WHITE, innerColor, outerColor};

	    return new RadialGradientPaint(0.0F, 0.0F, (float)ur2,
					   fractions, colors);
	}

	public double getR1() {return r1;}
	public double getR2() {return r2;}

	private double[] getX(double r, double r0, int n) {
	    double result[] = new double[n];
	    for (int i = 0; i < n; i++) {
		double phi = (2.0 * Math.PI * i)/n;
		result[i] = r * Math.cos(phi);
	    }
	    return result;
	}
	private double[] getY(double r, double r0, int n) {
	    double result[] = new double[n];
	    for (int i = 0; i < n; i++) {
		double phi = (2.0 * Math.PI * i)/n;
		result[i] = r * Math.sin(phi);
	    }
	    return result;
	}
	public CoordRing(double s1, double s2, double r0, int n) {
	    super(Path2D.WIND_EVEN_ODD);
	    this.r0 = r0;
	    r1 = getRForDistance(s1, r0);
	    r2 = getRForDistance(s2, r0);
	    moveTo(r1, 0.0);
	    addCycle(getX(r1, r0, n), getY(r1, r0, n));
	    moveTo(r2, 0.0);
	    addCycle(getX(r2, r0, n), getY(r2, r0, n));
	}
    }
    */


    public static double radialDistance(double r1, double r2, double r0) {
	    double tmp1 = Math.sqrt(r1*r1 - r0*r1);
	    double tmp2 = Math.sqrt(r2*r2 - r0*r2);
	    double r0h = r0/2.0;
	    return (tmp2 - tmp1)
		+ (r0h)*Math.log((tmp2+r2-r0h)/(tmp1+r1-r0h));
    }

    private static RootFinder.Brent<Double> sFinderB =
	new RootFinder.Brent<Double>() {
	    public double function(double r) {
		double r0 = getParameters();
		return radialDistance(r0, r, r0);
	    }
    };

    public static double getRForDistance(double s, double r0) {
	if (s == 0.0) return r0;
	sFinderB.setParameters(r0);
	double lowerlim = s-5.0*r0;
	if (lowerlim < r0) lowerlim = r0;
	return sFinderB.solve(s, lowerlim, s + r0);
    }

    public static class OrbitParms {
	double E;
	double L;
	double r0;
	double minR = 0;
	double maxR = Double.MAX_VALUE;

	Color redshift(Graph graph, double wavelength, double r) {
	    return Colors.getColorByWavelength(graph.getColorModel(),
					       wavelength*E/(1.0 - r0/r));
	}

	public double initialR(double f) {
	    return minR + f * (maxR - minR);
	}

	public double initialV(double r, boolean out) {
	    double r2 = r*r;
	    double r3 = r2*r;
	    double L2 = L * L;
	    return (out? 1: -1) *
		Math.sqrt(E*E - 1 + r0/r - L2/r2 + r0*L2/r3);
	}

	static double getRc(double L, double r0) {
	    double L2 = L*L;
	    return (L2/r0) * (1 + Math.sqrt(1 - 3.0*r0*r0/L2));
	}

	static final double SQRT3 = Math.sqrt(3.0);

	static double getERc(double L, double r0) {
	    if (L < SQRT3*r0) 
		throw new IllegalArgumentException("L too small");
	    double rc = getRc(L, r0);
	    double rc2 = rc*rc;
	    double rc3 = rc2*rc;
	    double L2 = L*L;
	    return Math.sqrt(1 - r0/rc + L2/rc2 - r0*L2/rc3);
		
	}

	public OrbitParms(double E, double L, double r0) {
	    this.E = E;
	    this.L = L;
	    this.r0 = r0;
	    double L2 = L*L;

	    double[] coeff = {r0*L2, -L2, r0, E*E-1};
	    int n = CubicCurve2D.solveCubic(coeff);
	    int m = 0;
	    // System.out.println("n = " + n);
	    if (n > 0) {
		Arrays.sort(coeff, 0, n);
		for (int i = 0; i < n; i++) {
		    // System.out.format("coeff[%d] = %g\n", m, coeff[m]);
		    if (coeff[m] > r0) break;
		    m++;
		}
		n -= m;
	    }
	    // System.out.println("n = " + n + ", m = " + m);

	    switch(n) {
	    case 0:
		minR = r0;
		break;
	    case 1:
		minR = r0;
		maxR = coeff[m];
		break;
	    default:
		minR = coeff[n-2];
		maxR = coeff[n-1];
		break;
	    }
	    
	}
    }

    public static class LightParms {
	public double b;
	public double r0;
	public double rv;

	LightParms(double rp, double rv, double r0, boolean counterclockwise) {
	    this.r0 = r0;
	    this.rv = rv;
	    double rprv = rp/rv;
	    b = (counterclockwise? 1.0: -1.0)
		* rp / Math.sqrt(1.0 +rprv*rprv*(1 - r0/rv));
	}

	public double rInitial() {return rv;}
	
	public double uInitial() {
	    return -Math.sqrt(1-(b*b/(rv*rv))*(1-r0/rv));
	}
    }

    public static void main(String argv[]) throws Exception {

	final double r0 = 1.0;

	String type = "png";
	if (argv.length == 1) type = argv[0];

	// Test to make sure radialDistance produces the correct
	// result.  radialDistance returns the value of an integral,
	// using a closed-form expression for the value. Numerically
	// integrating it is a useful cross check.
	GLQuadrature glq = new GLQuadrature(16) {
		protected double function(double r) {
		    return 1.0/Math.sqrt(1.0 - r0/r);
		}
	    };
	System.out.println(glq.integrate(2.0, 5.0, 5) + " <--> "
			   + radialDistance(2.0, 5.0, r0));
	
	double max = 0.0;

	for (int i = 0; i < 100000; i++) {
	    double r = (double)i/1000.0 + r0;
	    double s = radialDistance(r0, r, r0);
	    double r1 = getRForDistance(s, r0);
	    double err = Math.abs(r - r1)/r;
	    if (err > max) max = err;
	}
	System.out.println("max = " + max);

	OutputStreamGraphics osg = OutputStreamGraphics.newInstance
	    (new FileOutputStream("bhgeom." + type), 800, 800, type);
	Graph graph = new Graph(osg);
	graph.setOffsets(20, 20);
	graph.setRanges(-10.0, 10.0, -10.0, 10.0);
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.fillRect(0,0, 800, 800);
	g2d.drawRect(0,0, 800, 800);

	double lambda = MKS.nm(420.0);

	// handle a filled circle using a GCS graphics context.
	Graphics2D g2dGCS = graph.createGraphicsGCS();
	CoordCircle outerCircle = new CoordCircle(11.0, r0, 36);
	g2dGCS.setPaint(outerCircle.getPaintGCS(graph, lambda, 64));
	g2dGCS.fill(outerCircle);

	g2d.setStroke(new BasicStroke(2.0F));
	g2d.setColor(Color.WHITE);
	// CoordCircle circle = new CoordCircle(0.0, r0, 36);
	// graph.draw(g2d,circle);
	for (int i = 0; i <= 11; i++) {
	    double s = i/1.0;
	    CoordCircle circle = new CoordCircle(s, r0, 36);
	    graph.draw(g2d, circle);
	}

	graph.write();


	type = "ps";
	osg = OutputStreamGraphics.newInstance
	    (new FileOutputStream("bhorbit." + type), 800, 800, type);
	graph = new Graph(osg);
	graph.setOffsets(20, 20);
	graph.setRanges(-10.0, 10.0, -10.0, 10.0);
	g2d = graph.createGraphics();
	g2d.setColor(Color.GRAY);
	g2d.fillRect(0,0, 800, 800);
	g2d.drawRect(0,0, 800, 800);

	for (int i = 0; i < 20; i++) {
	    double Lt = 1.8 + i/20.0;
	    System.out.format("L = %g: rc = %g, E = %g\n",
			      Lt, OrbitParms.getRc(Lt, r0),
			      OrbitParms.getERc(Lt, r0));
	}

	double E = 0.967;
	double L = 2.05;

	OrbitParms op = new OrbitParms(E, L, 1.0);
	System.out.format("minR = %g, maxR = %g\n", op.minR, op.maxR);

	double initialR = op.initialR(0.8);
	double[] xInit = {
	    0.0,		// initial t
	    initialR,		// initial r
	    op.initialV(initialR, true), // initial v
	    0.0				 // initial phi
	};
	double[] firstInit = xInit; // for use by the animation

	/*
	System.out.println("initial r = " + xInit[1]);
	System.out.println("initial dr/d\u03c4 = " + xInit[2]);
	*/

	RungeKuttaMV<OrbitParms> rk =
	    new RungeKuttaMV<OrbitParms>(4, 0.0, xInit)
	    {
		protected void applyFunction(double t,
					     double[] x,
					     double[] results)
		{
		    OrbitParms p = getParameters();
		    double E = p.E;
		    double L = p.L;
		    double r0 = p.r0;
		    double r = x[1];
		    double r2 = r*r;
		    double r3 = r2*r;
		    double r4 = r3*r;
		    double L2 = L*L;
		    results[0] = E/(1-r0/r);
		    results[1] = x[2];
		    results[2] = -r0/(2*r2) + L2/r3 - 3.0*r0*L2/(2.0*r4);
		    results[3] = L/r2;
		}
	    };
	rk.setParameters(op);
	double x[] = new double[4];

	ArrayList<Double> tList = new ArrayList<>();
	ArrayList<Double> rList = new ArrayList<>();
	ArrayList<Double> phiList = new ArrayList<>();

	int MAX = 460;
	double dtau = 1.0;

	rk.getValues(x);
	tList.add(x[0]);
	rList.add(x[1]);
	phiList.add(x[3]);

	/*
	System.out.format("tau = %g: t= %g,  r = %g, phi = %g\n",
			  0.0, x[0], x[1], x[3]);
	*/

	for (int i = 0; i < MAX; i++) {
	    rk.update(dtau, 32);
	    rk.getValues(x);
	    tList.add(x[0]);
	    rList.add(x[1]);
	    phiList.add(x[3]);
	    /*
	    if (i%10 == 9) {
		double deg = Math.toDegrees(x[3]);
		while (deg > 360.0) deg -= 360.0;
		System.out.format
		    ("tau = %g: t= %g,  r = %g, phi = %g (%g deg)\n",
		     dtau*(i+1), x[0], x[1], x[3], deg);
	    }
	    */
	}
	double[] rArray = CopyUtilities.toDoubleArray(rList);
	double[] phiArray = CopyUtilities.toDoubleArray(phiList);
	double[] xArray = new double[rArray.length];
	double[] yArray = new double[rArray.length];
	double[] tArray = CopyUtilities.toDoubleArray(tList);
	for (int i = 0; i < rArray.length; i++) {
	    xArray[i] = rArray[i]*Math.cos(phiArray[i]);
	    yArray[i] = rArray[i]*Math.sin(phiArray[i]);
	}
	Path2D orbit = new SplinePath2D(xArray, yArray, false);

	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(4.0F));
	graph.draw(g2d, orbit);

	CubicSpline tSpline = new CubicSpline1(tArray, 0.0, dtau);
	CubicSpline xSpline = new CubicSpline1(xArray, 0.0, dtau);
	CubicSpline ySpline = new CubicSpline1(yArray, 0.0, dtau);
	CubicSpline rSpline = new CubicSpline1(rArray, 0.0, dtau);

	int n = 33;
	double tmin = tArray[0];
	double tmax = tArray[tArray.length - 1];

	UCircle ucirc = new UCircle();

	g2d.setStroke(new BasicStroke(1.0F));
	for (int i = 0; i <= n; i++) {
	    double t = (tmin * (n-i) + tmax * i)/n;
	    double tau = tSpline.inverseAt(t);
	    double xx = xSpline.valueAt(tau);
	    double yy = ySpline.valueAt(tau);
	    double rr = rSpline.valueAt(tau);
	    g2d.setColor(op.redshift(graph, MKS.nm(470.0), rr));
	    /*
	    System.out.format("orbit @ %d: (%g, %g) for t = %g, tau=%g\n",
			      i, xx, yy, t, tau);
	    */
	    graph.draw(g2d, ucirc, xx, yy);
	    graph.fill(g2d, ucirc, xx, yy);
	}
	graph.write();

	RungeKuttaMV<LightParms> rkl =
	    new RungeKuttaMV<LightParms>(4)
	    {
		protected void applyFunction(double t,
					     double[] x,
					     double[] results)
		{
		    LightParms p = getParameters();
		    double r0 = p.r0;
		    double b = p.b;
		    double b2 = b*b;
		    double r = x[1];
		    double r2 = r*r;
		    double r3 = r2*r;
		    double r4 = r3*r;
		    results[0] = 1/(1-r0/r);
		    results[1] = x[2];
		    results[2] = b2*(1.0/r3 - 3.0*r0/(2.0*r4));
		    results[3] = b/r2;
		}
	    };
	ArrayList<Double> lambdaList = new ArrayList<>();

	// first image: 4.5 to 11.0
	// for second image, 2.8 to 3.0
	LightParms lp = null;

	ArrayList<Double> xrpList = new ArrayList<>();
	ArrayList<Double> xrList = new ArrayList<>();
	ArrayList<Double> xtList = new ArrayList<>();

	for (int j = 0; j < 60; j++) {
	    double rp = 4.5 + j/10.0;
	    if (rp > 11.0) break;
	    xrpList.add(rp);

	    lp = new LightParms(rp, 1000.0, 1.0, true);
	    double[] xI = {
		0.0,		// initial t
		lp.rInitial(),	// initial r
		lp.uInitial(),	// initial u
		0.0		// initial phi
	    };
	    xInit = xI;

	    CubicSpline phiSpline  = null;
	    double[] lambdaArray = null;

	    rkl.setParameters(lp);
	    rkl.setInitialValues(0.0, xInit);
	    rkl.getValues(x);
	    double llambda = 0.0;

	    int LMAX = 2000;
	    double dlambda = 1.0;
	    double lastPhi = x[3];
	    double phiLimit1 = Math.toRadians(80.0);
	    double phiLimit2 = Math.toRadians(100.0);

	    lambdaList.clear();
	    tList.clear();
	    rList.clear();
	    phiList.clear();

	    for (int i = 0; i < LMAX; i++) {
		rkl.update(dlambda, 32);
		llambda += dlambda;
		rkl.getValues(x);
		if (x[3] > phiLimit1) {
		    lambdaList.add(llambda);
		    tList.add(x[0]);
		    rList.add(x[1]);
		    phiList.add(x[3]);
		
		}
		if (x[3] > phiLimit2) break;
		if (x[3] - lastPhi > 0.01) {
		    dlambda /= 2.0;
		}
		lastPhi = x[3];
	    }
	    lambdaArray = CopyUtilities.toDoubleArray(lambdaList);
	    tArray = CopyUtilities.toDoubleArray(tList);
	    rArray = CopyUtilities.toDoubleArray(rList);
	    phiArray = CopyUtilities.toDoubleArray(phiList);
	    CubicSpline lptSpline = new CubicSpline2(lambdaArray, tArray);
	    CubicSpline lprSpline = new CubicSpline2(lambdaArray, rArray);
	    CubicSpline lpphiSpline = new CubicSpline2(lambdaArray, phiArray);
	    double xlambda = lpphiSpline.inverseAt(Math.PI/2);
	    double xt = lptSpline.valueAt(xlambda);
	    double xr = lprSpline.valueAt(xlambda);

	    xrList.add(xr);
	    xtList.add(xt);
	}
	double[] xrArray = CopyUtilities.toDoubleArray(xrList);
	double[] xrpArray = CopyUtilities.toDoubleArray(xrpList);
	double[] xtArray = CopyUtilities.toDoubleArray(xtList);
	for (int i = 0; i < xtArray.length; i++) {
	    xtArray[i] -= 1004.0;
	}
	RealValuedFunction apparentR1 = new CubicSpline2(xrArray, xrpArray);
	RealValuedFunction apparentT1 = new CubicSpline2(xrArray, xtArray);
	
	xrList.clear();
	xtList.clear();
	xrpList.clear();

	for (int j = 0; j < 60; j++) {
	    double rp = 2.8 + j/100.0;
	    if (rp > 3.0) break;
	    xrpList.add(rp);

	    lp = new LightParms(rp, 1000.0, 1.0, true);
	    double[] xI = {
		0.0,		// initial t
		lp.rInitial(),	// initial r
		lp.uInitial(),	// initial u
		0.0		// initial phi
	    };
	    xInit = xI;

	    CubicSpline phiSpline  = null;
	    double[] lambdaArray = null;

	    rkl.setParameters(lp);
	    rkl.setInitialValues(0.0, xInit);
	    rkl.getValues(x);
	    double llambda = 0.0;

	    int LMAX = 20000;
	    double dlambda = 1.0;
	    double lastPhi = x[3];
	    double phiLimit1 = Math.toRadians(255.0);
	    double phiLimit2 = Math.toRadians(285.0);

	    lambdaList.clear();
	    tList.clear();
	    rList.clear();
	    phiList.clear();

	    for (int i = 0; i < LMAX; i++) {
		rkl.update(dlambda, 32);
		llambda += dlambda;
		rkl.getValues(x);
		if (x[3] > phiLimit1) {
		    lambdaList.add(llambda);
		    tList.add(x[0]);
		    rList.add(x[1]);
		    phiList.add(x[3]);
		}
		if (x[3] > phiLimit2) {
		    break;
		}
		if (Math.abs(x[3] - lastPhi) > 0.01) {
		    dlambda /= 2.0;
		}
		if (Math.abs(x[3] - lastPhi) < 0.005) {
		    if (dlambda < 0.7)
			dlambda *= 1.5;
		}
		lastPhi = x[3];
	    }
	    lambdaArray = CopyUtilities.toDoubleArray(lambdaList);
	    tArray = CopyUtilities.toDoubleArray(tList);
	    rArray = CopyUtilities.toDoubleArray(rList);
	    phiArray = CopyUtilities.toDoubleArray(phiList);
	    CubicSpline lptSpline = new CubicSpline2(lambdaArray, tArray);
	    CubicSpline lprSpline = new CubicSpline2(lambdaArray, rArray);
	    CubicSpline lpphiSpline = new CubicSpline2(lambdaArray, phiArray);
	    double xlambda = lpphiSpline.inverseAt(3.0*Math.PI/2);
	    double xt = lptSpline.valueAt(xlambda);
	    double xr = lprSpline.valueAt(xlambda);
	    xrList.add(xr);
	    xtList.add(xt);
	}
	xrArray = CopyUtilities.toDoubleArray(xrList);
	xrpArray = CopyUtilities.toDoubleArray(xrpList);
	xtArray = CopyUtilities.toDoubleArray(xtList);
	for (int i = 0; i < xtArray.length; i++) {
	    xtArray[i] -= 1004.0;
	}
	RealValuedFunction apparentR2 = new CubicSpline2(xrArray, xrpArray);
	RealValuedFunction  apparentT2 = new CubicSpline2(xrArray, xtArray);
	RealValuedFunction rf = new RealValuedFunction() {
		public double valueAt(double r) {return r;}
		public double derivAt(double r) {return 1.0;}
		public double secondDerivAt(double r){ return 0.0;}
	    };

	Path2D rcurve1 = new SplinePath2D(rf, apparentR1, 4.5, 9.9, 55,
					  false); 
	Path2D rcurve2 = new SplinePath2D(rf, apparentR2, 4.5, 9.9, 55,
					  false); 
	type = "ps";
	osg = OutputStreamGraphics.newInstance
	    (new FileOutputStream("bhrmap." + type), 800, 800, type);
	graph = new Graph(osg);
	graph.setOffsets(100, 100);
	graph.setRanges(4.0, 11.0, 0.0, 11.0);
	Graph.Axis xAxis = new Graph.Axis(4.0, 0.0, 
					  Graph.Axis.Dir.HORIZONTAL_INCREASING,
					  10.0-4.0,
					  4.0, 0.5, false);
	xAxis.setWidth(2.0);
	xAxis.setLabelOffset(10.0);
	xAxis.setLabel("Schwarzschild radial coordinate");
	xAxis.addTick(new Graph.TickSpec(5.0, 1.5, 1));
	xAxis.addTick(new Graph.TickSpec(10.0, 2.0, 2, "%4.1f", 4.0));

	graph.draw(xAxis);

	Graph.Axis yAxis = new Graph.Axis(4.0, 0.0, 
					  Graph.Axis.Dir.VERTICAL_INCREASING,
					  11.0,
					  0.0, 0.5, true);
	yAxis.setLabel("Apparent radial coordinate");
	yAxis.addTick(new Graph.TickSpec(5.0, 1.5, 1));
	yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 2, "%4.1f", 4.0));
	graph.draw(yAxis);
    
	g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(2.0F));
	g2d.setColor(Color.BLACK);

	graph.draw(g2d, rcurve1);
	graph.draw(g2d, rcurve2);
	graph.write();

	Path2D tcurve1 = new SplinePath2D(rf, apparentT1, 4.5, 9.9, 55,
					  false); 
	Path2D tcurve2 = new SplinePath2D(rf, apparentT2, 4.5, 9.9, 55,
					  false);

	osg = OutputStreamGraphics.newInstance
	    (new FileOutputStream("bhtmap." + type), 800, 800, type);
	graph = new Graph(osg);
	graph.setOffsets(100, 100);
	graph.setRanges(4.0, 11.0, 0.0, 20.0);
	xAxis = new Graph.Axis(4.0, 0.0, 
			       Graph.Axis.Dir.HORIZONTAL_INCREASING,
			       10.0-4.0,
			       4.0, 0.5, false);
	xAxis.setWidth(2.0);
	xAxis.setLabelOffset(10.0);
	xAxis.setLabel("Schwarzschild radial coordinate");
	xAxis.addTick(new Graph.TickSpec(5.0, 1.5, 1));
	xAxis.addTick(new Graph.TickSpec(10.0, 2.0, 2, "%4.1f", 4.0));

	graph.draw(xAxis);

	yAxis = new Graph.Axis(4.0, 0.0, 
			       Graph.Axis.Dir.VERTICAL_INCREASING,
			       20.0,
			       0.0, 1.0, true);
	yAxis.setLabel("Time delay");
	yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 1));
	yAxis.addTick(new Graph.TickSpec(10.0, 2.0, 2, "%4.1f", 4.0));
	graph.draw(yAxis);
    
	g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(2.0F));
	g2d.setColor(Color.BLACK);

	graph.draw(g2d, tcurve1);
	graph.draw(g2d, tcurve2);
	graph.write();



	/*
	for (int i = 0; i < 55; i++) {
	    double r = 4.5 + i/10.0;
	    System.out.format("r = %g, r(img1) = %g, t = %g, r(img2) = %g, t = %g\n",
			      r, apparentR1.valueAt(r), apparentT1.valueAt(r),
			      apparentR2.valueAt(r), apparentT2.valueAt(r));
	    
	}
	*/

	rk.setParameters(op);
	rk.setInitialValues(0.0, firstInit);
	ArrayList<Double> tList1 = new ArrayList<>();
	ArrayList<Double> tList2 = new ArrayList<>();
	ArrayList<Double> rList1 = new ArrayList<>();
	ArrayList<Double> rList2 = new ArrayList<>();
	phiList.clear();
	rList.clear();
	MAX = 4600;
	dtau = 1.0;
	rk.getValues(x);
	System.out.println("x[0] = " + x[0]);
	tList1.add(x[0] + apparentT1.valueAt(x[1]));
	tList2.add(x[0] + apparentT2.valueAt(x[1]));
	rList1.add(apparentR1.valueAt(x[1]));
	rList2.add(apparentR2.valueAt(x[1]));
	rList.add(x[1]);
	phiList.add(x[3]);
	for (int i = 0; i < MAX; i++) {
	    rk.update(dtau, 32);
	    rk.getValues(x);
	    tList1.add(x[0] + apparentT1.valueAt(x[1]));
	    tList2.add(x[0] + apparentT2.valueAt(x[1]));
	    rList1.add(apparentR1.valueAt(x[1]));
	    rList2.add(apparentR2.valueAt(x[1]));
	    rList.add(x[1]);
	    phiList.add(x[3]);
	}
	rArray = CopyUtilities.toDoubleArray(rList);
        phiArray = CopyUtilities.toDoubleArray(phiList);
	double[] tArray1 = CopyUtilities.toDoubleArray(tList1);
	double[] tArray2 = CopyUtilities.toDoubleArray(tList2);
	double[] rArray1 =  CopyUtilities.toDoubleArray(rList1);
	double[] rArray2 =  CopyUtilities.toDoubleArray(rList2);
	double[] xArray1 = new double[rArray.length];
	double[] yArray1 = new double[rArray.length];
	double[] xArray2 = new double[rArray.length];
	double[] yArray2 = new double[rArray.length];
	for (int i = 0; i < rArray.length; i++) {
	    xArray1[i] = rArray1[i]*Math.cos(phiArray[i]);
	    yArray1[i] = rArray1[i]*Math.sin(phiArray[i]);
	    xArray2[i] = -rArray2[i]*Math.cos(phiArray[i]);
	    yArray2[i] = -rArray2[i]*Math.sin(phiArray[i]);
	}

	CubicSpline tSpline1 = new CubicSpline1(tArray1, 0.0, dtau);
	CubicSpline tSpline2 = new CubicSpline1(tArray2, 0.0, dtau);
	CubicSpline xSpline1 = new CubicSpline1(xArray1, 0.0, dtau);
	CubicSpline ySpline1 = new CubicSpline1(yArray1, 0.0, dtau);
	CubicSpline xSpline2 = new CubicSpline1(xArray2, 0.0, dtau);
	CubicSpline ySpline2 = new CubicSpline1(yArray2, 0.0, dtau);
	rSpline = new CubicSpline1(rArray, 0.0, dtau);

	Animation2D a2d = new Animation2D(1920, 1080, 3000.0, 1000);
	double scaleFactor = a2d.getHeight()/ 22.0;
	a2d.setRanges(-11.0, 11.0 + (1920-1080)/scaleFactor, -11.0, 11.0);
	a2d.setBackgroundColor(Color.BLACK);
	a2d.setFontColor(Color.WHITE);
	a2d.setFontJustification(Graph.Just.LEFT);
	a2d.setFont(new Font("Helvetica", Font.BOLD, 26));

	AnimationLayer2D layer = new AnimationLayer2D(a2d,"text", true);
	layer.initGraphicArray
	    (Arrays.asList
	     (new Graph.Graphic() {
		     Stroke stroke = new BasicStroke(2.0F);
		     CoordCircle circle = new CoordCircle(0.0, r0, 36);
		     public void addTo(Graph graph,
				       Graphics2D g2d, Graphics2D g2dGCS)
		     {
			 Stroke savedStroke = g2d.getStroke();
			 Color savedColor = g2d.getColor();
			 try {
			     g2d.setStroke(stroke);
			     g2d.setColor(Color.WHITE);
			     graph.draw(g2d, circle);
			 } finally {
			     g2d.setStroke(savedStroke);
			     g2d.setColor(savedColor);
			 }
			 double loc = 9.0;
			 double delta1 = 0.7;
			 double delta2 = 1.0;
			     
			 graph.drawString("White circle shows the event "
					  + "horizon", 12.0, loc);
			 loc -= delta1;
			 graph.drawString("(Schwarzschild radius = r\u2080)",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("wavelength = 760 nm before redshift",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("Inner location due to light bending",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("around the blackhole",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("Orbit viewed at r = 1000r\u2080, "
					  + "\u03b8 = 0",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("For the orbit shown, "
					  + "\u03b8 = \u03c0/2 ",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("Variations in light intensity not "
					  + "shown",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("The two locations for the light "
					  + "source",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("have different values of \u03d5 "
					  + "because", 12.0, loc);
			 loc -= delta1;
			 graph.drawString("of time delays when light circles a",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("black hole: both represent the same",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("object at different times",
					  12.0, loc);
			 loc -= delta2;
			 graph.drawString("Positions of the light source are",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("where they would be seen by the",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("observer, not at the light "
					  +"source's",
					  12.0, loc);
			 loc -= delta1;
			 graph.drawString("coordinates", 12.0, loc);

		     }
		 }));
	layer.setZorder(1, true);

	Disk disk1 = new Disk(a2d, "direct", true);
	Disk disk2 = new Disk(a2d, "indirect", true);
	double wavelength = MKS.nm(470.0);
	disk1.init(op, wavelength, tSpline1, rSpline, xSpline1, ySpline1);
	disk1.setZorder(2, true);
	disk2.init(op, wavelength, tSpline2, rSpline, xSpline2, ySpline2);
	disk2.setZorder(2, true);
	double t1 = tArray2[0];
	double t2 = tArray1[tArray1.length - 1];
	int maxframes = a2d.estimateFrameCount(t2-t1);
	System.out.println("maxframes = " + maxframes);
	File dir = new File("bhtmp");
	dir.mkdirs();
	for (File f: dir.listFiles()) {
	    f.delete();
	}
	a2d.initFrames(maxframes, "bhtmp/img-", "png");
	// run for 1/5 the value we computed to keep the
	// running time to a couple of minutes and to
	// keep the webm file size below 5 MBytes.
	a2d.scheduleFrames(a2d.getTicks(t1), maxframes/5);
	a2d.run();
	System.exit(0);
    }
}