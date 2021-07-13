import java.awt.*;
import java.awt.geom.*;
import java.io.File;

import org.bzdev.anim2d.*;
import org.bzdev.math.RungeKuttaMV;
import org.bzdev.util.units.MKS;
import org.bzdev.graphs.Graph;

public class Pendulum {
    static double m = 100.0;	// 100 kg for mass
    static double r = 5.0;	// 5 meter radius
    static double rcm = r;	//  center of mass at a radius of r.
    static double I = 100.0*r*r; // Moment of inertia.
    static double theta0 = 0.0;
    static double omega0 = 7.0/r; // 7 meters per second at a radius of r.
    static double g = MKS.gFract(1.0);
    static double hwidth = 0.5;	// half width blade.
    static double height = 0.2;	// height of blade;
    static double hc = 0.1;	// half-width of wire


    static class SimplePendulum extends PlacedAnimationObject2D {
	double[] init = {theta0, I*omega0};

	RungeKuttaMV rk = new RungeKuttaMV(2, 0.0, init) {
		public void applyFunction(double t,
					  double[] values,
					  double[] results) {
		    double theta = values[0];
		    double ptheta = values[1];
		    results[0] = ptheta/I;
		    results[1] = -m*g*rcm*Math.sin(theta);
		}
	    };

	protected void update(double t, long simtime) {
	    rk.updateTo(t, 0.0002);
	    setAngle(rk.getValue(0));
	}

	Shape shape1 = new Rectangle2D.Double(-hc, -r, 2*hc, r);
	Shape shape2 = new Rectangle2D.Double(-hwidth, -r, 2*hwidth, height);

	SimplePendulum(Animation2D a2d, String name, boolean intern) {
	    super(a2d, name, intern);
	    rk.setTolerance(1.0e-8);
	    setRefPointBounds(-hwidth, hwidth, -r, 0.0);
	    setPosition(0.0, 0.0, 0.0);
	}

	public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	    Color savedColor = g2d.getColor();
	    try {
		AffineTransform af = getAddToTransform();
		g2d.setColor(Color.RED);
		Shape s1 = new Path2D.Double(shape1, af);
		Shape s2 = new Path2D.Double(shape2, af);
		graph.draw(g2d, s1);
		graph.fill(g2d, s1);
		graph.draw(g2d, s2);
		graph.fill(g2d, s2);
	    } finally {
		g2d.setColor(savedColor);
	    }
	}
    }

    public static void main(String argv[]) throws Exception {

	Animation2D a2d = new Animation2D(1920,1080, 10000, 200);
	SimplePendulum pendulum = new SimplePendulum(a2d, "pendulum", true);
	double scale = 700.0 / 6.0;
	a2d.setRanges(0.0, 0.0, 0.5, .9,  scale, scale);
	pendulum.setZorder(1, true);
	a2d.setBackgroundColor(Color.BLACK);

	File tf = new File ("pndltmp");
	if (!tf.exists()) {
	    tf.mkdir();
	} else if (tf.isDirectory()) {
	    for (File f: tf.listFiles()) {
		f.delete();
	    }
	} else {
	    throw new Exception("tmp exists and is not a directory");
	}
	
	int maxFrames = a2d.estimateFrameCount(20.0);
	a2d.initFrames(maxFrames, "pndltmp/img", "jpg");
	a2d.scheduleFrames(0, maxFrames);
	a2d.run();
    }
}
