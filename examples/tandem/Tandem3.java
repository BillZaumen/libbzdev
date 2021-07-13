import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.math.rv.InterarrivalTimeRV;
import org.bzdev.math.rv.PoissonIATimeRV;
import org.bzdev.math.StaticRandom;

import org.bzdev.anim2d.*;
import org.bzdev.graphs.Graph;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

public class Tandem3 {
    
    static Simulation  sim = new Simulation ();
    static FifoTaskQueue cq1 = new FifoTaskQueue(sim, "cq1", false);
    static FifoTaskQueue cq2 = new FifoTaskQueue(sim, "cq2", false);

    static int count[] = new int[10];

    public static void main(String argv[]) {

	StaticRandom.setSeed(1758935837430L);
	StaticRandom.maximizeQuality();
	
	// Create a simulation of two tandem queues with
	// Poission traffic.

	final InterarrivalTimeRV rv1 = new PoissonIATimeRV(10.0);
	final InterarrivalTimeRV rv2 = new PoissonIATimeRV(15.0);

	final Runnable program = new Runnable() {
		public void run() {
		    long start = sim.currentTicks();
		    cq1.addCurrentTask(rv1.next());
		    cq2.addCurrentTask(rv1.next());
		    long end = sim.currentTicks();

		    long ind = (end - start)/10;
		    if (0 <= ind && ind < 10) count[(int)ind]++;
		}
	    };

	sim.scheduleTask(new Runnable() {
		public void run() {
		    for (int i = 0; i < 10000; i++) {
			TaskThread.pause(rv2.next());
			sim.scheduleTask(program, 0);
		    }
		}
	    });

	// Add an animation with trivial graphics: each queue
	// is shown as a rectangle whose width is proportional
	// to the queue length.

	Animation2D animation = new Animation2D(sim, 800, 450,
						25.0, 1);
	animation.setRanges(0.0, 100.0, 0.0, 40.0);
	animation.setBackgroundColor(Color.BLACK);
	AnimationObject2D object =
	    new AnimationObject2D(animation, "queueDisplay", true)
	    {
		public void addTo(Graph graph, 
				  Graphics2D g2d,
				  Graphics2D g2dGCS)
		{
		    double len1 = 10.0 * cq1.size();
		    double len2 = 10.0 * cq2.size();
		    Rectangle2D r1 =
			new Rectangle2D.Double(0.0, 5.0, len1, 10.0);
		    Rectangle2D r2 =
			new Rectangle2D.Double(0.0, 25.0, len2, 10.0);
		    Color savedColor = g2d.getColor();
		    Stroke savedStroke = g2d.getStroke();
		    g2d.setStroke(new BasicStroke(1.0F));
		    g2d.setColor(Color.YELLOW);
		    graph.draw(g2d, r1);
		    graph.fill(g2d, r1);
		    graph.draw(g2d, r2);
		    graph.fill(g2d, r2);
		    g2d.setStroke(savedStroke);
		    g2d.setColor(savedColor);
		}
	    };
	object.setZorder(0, true);
	File dir = new File("ttmp");
	dir.mkdirs();
	int maxframes = animation.estimateFrameCount(30.0);
	try {
	    animation.initFrames(maxframes, "ttmp/img-", "png");
	    animation.scheduleFrames(0, maxframes);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	// run the simulation

	sim.run();

	// print out a table showing the distribution for
	// the total time spent in the queues. The counts
	// include events whose time fit in a series of bins.

	for (int i = 0; i < 10; i++) {
	    System.out.println("count[" +(i*10)
			       +".." +((i+1)*10 - 1)
			       +"] = " +count[i]);
	}

    }
}
