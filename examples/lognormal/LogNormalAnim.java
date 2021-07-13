import org.bzdev.anim2d.*;
import org.bzdev.math.rv.*;
import org.bzdev.graphs.*;
import org.bzdev.geom.*;

import java.io.File;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

public class LogNormalAnim {
    static Graph.Axis xAxis;
    static Graph.Axis yAxis;
    
    static public void main(String[] argv) {
	Animation2D animation = new Animation2D(800, 600, 1000.0, 40);

	final double maxX = 50.0;
	final double maxY = 1.0;

	// set up the animation's graph.
	animation.setOffsets(100, 20, 100, 20);
	animation.setRanges(0.0, maxX, 0.0, maxY);
	animation.setBackgroundColor(Color.WHITE);

	// configure the axes
	xAxis = new Graph.Axis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
			       maxX, 0.0, 1.0, false);
	xAxis.setWidth(2.0);
	xAxis.setColor(Color.BLACK);
	xAxis.setLabelOffset(10.0);
	xAxis.setLabel("Quantity Being Measured");

	Graph.TickSpec xTickSpec1 = new Graph.TickSpec(5.0, 1.0, 1);
	Graph.TickSpec xTickSpec2 = new Graph.TickSpec(7.5, 2.0, 5);
	Graph.TickSpec xTickSpec3 = new Graph.TickSpec(10.0, 2.0, 10,
						       "%2.0f", 5);
	xAxis.addTick(xTickSpec1);
	xAxis.addTick(xTickSpec2);
	xAxis.addTick(xTickSpec3);
	
	yAxis = new Graph.Axis(0.0, 0.0, Graph.Axis.Dir.VERTICAL_INCREASING,
			       maxY, 0.0, 0.1, true);
	yAxis.setWidth(2.0);
	yAxis.setColor(Color.BLACK);
	yAxis.setLabelOffset(10.0);
	yAxis.setLabel("Probability Density Function");
	
	Graph.TickSpec yTickSpec1 = new Graph.TickSpec(5.0, 1.0, 1);
	Graph.TickSpec yTickSpec2 = new Graph.TickSpec(7.5, 2.0, 5);
	Graph.TickSpec yTickSpec3 = new Graph.TickSpec(10.0, 2.0, 10,
						       "%3.1f", 5);
	yAxis.addTick(yTickSpec1);
	yAxis.addTick(yTickSpec2);
	yAxis.addTick(yTickSpec3);

	// layer to draw axes on the graph with a z-order such that the
	// axes are drawn before anything is plotted.
	AnimationLayer2D axes = new AnimationLayer2D(animation, "axes", true) {
		@Override
		public void addTo(Graph g, Graphics2D g2d, Graphics2D g2dGCS) {
		    super.addTo(g, g2d, g2dGCS);
		    g.draw(xAxis);
		    g.draw(yAxis);
		}
	    };
	axes.setZorder(0, true);

	// model-specific parameters
	final double tmax = 15.0;
	final double imean = 10.0;
	final double ivar =  0.01;
	final double dm = 0.005;
	final double ds = 0.001;

	// Object for the plot.
	AnimationObject2D plot =
	    new AnimationObject2D(animation, "plot", true) {
		
		double mu = Math.log(imean * imean /
				     Math.sqrt(ivar + imean * imean));
		double sigma = Math.sqrt(Math.log(1 + ivar / (imean * imean))); 
		double sigma2 = sigma * sigma;		     

		SplinePath2D distr = new SplinePath2D();
		
		// method to draw the object when needed
		public void addTo(Graph graph,
				  Graphics2D g2d, Graphics2D g2dGCS)
		{
		    Stroke savedStroke = g2d.getStroke();
		    Color savedColor = g2d.getColor();
		    g2d.setStroke(new BasicStroke(1.5f));
		    g2d.setColor(Color.BLACK);
		    graph.draw(g2d, distr);
		    g2d.setStroke(savedStroke);
		    g2d.setColor(savedColor);
		}

		double[] xs = new double [500];
		double[] ys = new double [500];

		// method to update the object's state to what it should
		// be for the current animation time.
		protected void update(double t, long simtime) {
		    mu +=  dm;
		    sigma2 +=  ds;
		    sigma = Math.sqrt(sigma2);
		    double maxy = 0.0;
		    for (int i = 0; i < 500; i++) {
			double x = 0.1 + (double)i / 10;
			double z = Math.log(x) - mu;
			double y = (1.0 /(x * sigma * Math.sqrt(2 * Math.PI)))
			    * Math.exp(-(z*z)/(2.0 * sigma * sigma));
			xs[i] = x;
			ys[i] = y;
			if (y > maxy) maxy = y;
		    }
		    System.out.println("t = " + t
				       + ", mu  = " + mu
				       + ", sigma = " + sigma
				       + ", max-y = " + maxy);
		    distr = new SplinePath2D(xs, ys, false);
		}
	    };
	//set the z-order to a number higher than for the animation layer.
	plot.setZorder(1, true);

	int maxframes = animation.estimateFrameCount(tmax);
	try {
	    // put the images into a directory and run the animation to
	    // create the images.
	    File dir = new File("atmp");
	    dir.mkdirs();

	    animation.initFrames(maxframes, "atmp/img-", "png");
	    animation.scheduleFrames(0, maxframes);

	    animation.run();
	    
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
