import java.awt.*;
import java.awt.geom.*;
import org.bzdev.geom.*;
import org.bzdev.graphs.*;
import org.bzdev.math.*;
import org.bzdev.p3d.*;


public class Carafe {

    static double minR = 12.0;
    static double x1 = 15.0;
    static double x2 = 30.0;
    static double x3 = 25.0;

    static RealValuedFunction xf = new RealValuedFunction() {
	    @Override
	    public double valueAt(double y) {
		return minR * Math.cosh(y/minR);
	    }
	    @Override
	    public double derivAt(double y) {
		return Math.sinh(y/minR);
	    }
	    @Override
	    public double secondDerivAt(double y) {
		return Math.sinh(y/minR) / minR;
	    }
	};

    static RealValuedFunction yf = RealValuedFunction.xFunction;


    public static void main(String argv[]) throws Exception {

	double y1 = minR*Functions.acosh(x1/minR);
	double y2 = - minR*Functions.acosh(x2/minR);
	System.out.format("y1 = %g, y2 = %g\n", y1, y2);

	Path2D p1 = new SplinePath2D(xf, yf, y1, y2, 20, false);
	Point2D cp = p1.getCurrentPoint();
	double tangent1[] = {0.0, -1.0};
	double tangent2[] = {-1.0, 0.0};
	Path2D p2b = Paths2D.createArc(cp.getX(), cp.getY(),
				       Path2DInfo.lastTangent(p1), 0,
				       x3, cp.getY() - 30.0,
				       tangent2, 0);
	p1.append(p2b, true);
	cp = p1.getCurrentPoint();
	
	Path2D p = Paths2D.offsetBy(p1, 3.0, 3.0, false);
	p = PathSplitter.split(p)[0];

	double y3 = p1.getCurrentPoint().getY();

	p = Paths2D.reverse(p);
	
	Path2D p2c = Paths2D.createArc(p, p1, Math.PI/8);
	Path2DInfo.printSegments(p2c);
	

	p.append(p2c, true);
	p.append(p1, true);
	// p = Paths2D.pruneShortLineSegments(p);


	BezierGrid grid = new BezierGrid(p, (i, pt, type, bounds) -> {
		double theta = i*2*Math.PI/36;
		double r = pt.getX();
		double x = r*Math.cos(theta);
		double y = r*Math.sin(theta);
		double z = pt.getY();
		return new Point3D.Double(x, y, z);
	}, 36, true);

	System.out.println("n drawable segs = "
			   + Path2DInfo.numberOfDrawableSegments(p));
	System.out.println("n drawable knots = "
			   + Path2DInfo.numberOfDrawableKnots(p));
	System.out.format("grid is %d by %d (nU by NV)\n",
			  grid.getUArrayLength(),
			  grid.getVArrayLength());
	// grid.print();
	
	Path3D bp = grid.getBoundary();

	System.out.println(bp);

	Graph graph = new Graph(800, 600);
	double ydist = y1 - y3;
	double xdist = 2*(Math.max(x1, x2) + 12.0);
	double sf1 = 800/xdist;
	double sf2 = 600/ydist;
	double sf = (sf1 < sf2)? sf1: sf2;
	graph.setRanges(-xdist/2, y3, 0.0, 0.0, sf, sf);
	graph.setOffsets(50, 50);
	Graphics2D g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(2.0F));
	graph.draw(g2d, new Line2D.Double(0.0, -10.0, 0.0, 10.0));
	graph.draw(g2d, p);
	graph.write("png", "carefPath.png");
    }
}
