import org.bzdev.anim2d.*;
import org.bzdev.math.RungeKuttaMV;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;

public class Ball extends AnimationObject2D {
    double x;
    RungeKuttaMV<HParms> rk;
    double spt;
    
    public Ball(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	spt = 1.0 / animation.getTicksPerSecond();
    }
    public void init(double x, double p, double m, double k) {
	double[] y0 = {x, p};
	HParms parameters = new HParms();
	parameters.m = m;
	parameters.k = k;
	rk = new RungeKuttaMV<HParms>(2, 0.0, y0) {
	    protected void applyFunction(double t, double[] y,
					 double[] yderivative)
	    {
		HParms parameters = getParameters();
		double m = parameters.m;
		double k = parameters.k;
		yderivative[0] = y[1] / m;
		yderivative[1] = -k * y[0];
	    }
	};
	rk.setParameters(parameters);
    }

    Graph.UserDrawable ball = new Graph.UserDrawable() {
	    Ellipse2D circle =
		new Ellipse2D.Double(-15.0, -15.0, 30.0 , 30.0);

	    public Shape toShape(boolean xAxisPointsRight,
				 boolean yAxisPointsDown) {
		return circle;
	    }
	};
		
    public void addTo(Graph graph, Graphics2D g2d,
			  Graphics2D g2dGCS)
    {
	Stroke savedStroke = g2d.getStroke();
	Color savedColor = g2d.getColor();
	g2d.setStroke(new BasicStroke(1.0f));
	g2d.setColor(Color.RED);
	graph.draw(g2d, ball, x, 0.0);
	graph.fill(g2d, ball, x, 0.0);
	g2d.setStroke(savedStroke);
	g2d.setColor(savedColor);
    }

    long last = 0;
    protected void update(double t, long simtime) {
	for (long i = last; i < simtime; i++) {
	    rk.update(spt);
	}
	x = rk.getValue(0);
	last = simtime;
    }
}
