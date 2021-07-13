import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.geom.*;
import java.awt.*;

import org.bzdev.lang.Callable;
import org.bzdev.devqsim.*;
import org.bzdev.math.StaticRandom;
import org.bzdev.util.units.MKS;
import org.bzdev.math.rv.*;

import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.imageio.ImageMimeInfo;
import org.bzdev.graphs.*;
import org.bzdev.geom.*;

public class StephanTime {
    static Simulation sim = new Simulation(1000.0);
    static final long ticksPerDay = sim.getTicks(MKS.days(1.0));
    static final long taskAddEnd = sim.getTicks(MKS.hours(6.0));
    static final long noon = sim.getTicks(MKS.hours(3.0));
    static final FifoTaskQueue stack = new FifoTaskQueue(sim, "stack", true);

    static int count;

    // The stack emptied before noon, so we need to schedule
    // a test at noon in case nothing else is put on the stack in
    // the meantime.
    static void checkLater(long ct) {
	sim.scheduleCall(new Callable() {
		public void call() {
		    if (stack.size() > 0 || stack.isBusy()) return;
		    goClimbing();
		}
	    }, noon - ct);
    }

    static ArrayList<Double> alist;

    // This records the delay and makes sure that Stephan
    // climbs only once per day.
    static boolean goneClimbing = false;
    static void goClimbing() {
	if (goneClimbing) return;
	long ct = sim.currentTicks() % ticksPerDay;
	double delay = (ct - noon) / sim.getTicksPerUnitTime();
	double delayMinutes = delay/60.0;
	alist.add(delayMinutes);
	goneClimbing = true;
    }

    // every 24 hours, we set the boolean goneClimbing to false.
    static Callable clearFlag = null;
    static {
	clearFlag = new Callable() {
		public void call() {
		    goneClimbing = false;
		    if (count > 0) {
			count--;
			sim.scheduleCall(clearFlag, ticksPerDay);
		    }
		}
	    };
    }

    public static void main(String argv[]) throws Exception {

	// Make the random number generate produce as good a
	// random number sequence as possible.
	StaticRandom.maximizeQuality();

	// command line arguments
	double meanIATime = MKS.minutes(Double.parseDouble(argv[0]));
	double serviceTime = MKS.minutes(Double.parseDouble(argv[1]));
	count = Integer.parseInt(argv[2]);
	String filename = null;
	String formatType = "ps";

	// set up the output stream for creating a graph.
	// The default image type is "ps" but if one provides a
	// filename extension, that determines the output image
	// format
	if (argv.length > 3) {
	    filename = argv[3];
	    int ind = filename.lastIndexOf('.');
	    if (ind == -1) filename = filename + ".ps";
	    ind = filename.lastIndexOf('.');
	    String suffix = filename.substring(ind+1);
	    if (suffix.contains(System.getProperty("file.separator"))) {
		ind = filename.lastIndexOf('.');
		filename = filename + ".ps";
		suffix = "ps";
	    }
	    if (!suffix.equals("ps")) {
		String mimeType = ImageMimeInfo.getMIMETypeForSuffix(suffix);
		formatType = ImageMimeInfo.getFormatNameForMimeType(mimeType);
	    }
	}
	OutputStream os = (filename == null)? null:
	    new FileOutputStream(filename);
	OutputStreamGraphics osg = (filename == null)? null:
	    OutputStreamGraphics.newInstance(os, 600, 600, formatType);

	// list to hold the delays.
	alist = new ArrayList<Double>(count+100);

	// Random variables
	final ExpDistrRV iarv =
	    new ExpDistrRV(meanIATime);
	final ExpDistrRV strv = new ExpDistrRV(serviceTime);

	// Task to add entries to the stack. The call when the
	// entry leaves the stack checks the stack size. If it
	// is zero, it checks the time and either calls goClimbing
	// or checkLater.  If it is not zero, there is nothing to do.
	Runnable taskgen = new Runnable() {
		public void run() {
		    while (count > 0) {
			TaskThread.pause(sim.getTicks(iarv.next()));
			long ct = sim.currentTicks() % ticksPerDay;
			if (ct < taskAddEnd) {
			    stack.add(new Callable() {
				    public void call() {
					long ct =
					    sim.currentTicks() % ticksPerDay;
					if (stack.size() == 0) {
					    if (ct >= noon) {
						goClimbing();
					    } else  {
						checkLater(ct);
					    }
					}
				    }
				}, sim.getTicks(strv.next()));
			} else {
			    TaskThread.pause(ticksPerDay - ct);
			}
		    }
		}
	    };

	// start everything.
	sim.scheduleTask(taskgen, 0);
	sim.scheduleCall(clearFlag, 0);
	sim.run();

	/*
	 * Data analysis phase
	 */
	int n = alist.size();
	int m = n;
	if (n > 100) {
	    m = n / 100;
	}
	double[] times = new double[11];
	double[] prob = new double[11];
	for (int i = 0; i < 11; i++) {
	    times[i] = i * 10.0;
	}

	// Compute cumulative probabilities
	Double[] array = new Double[n];
	array = alist.toArray(array);
	Arrays.sort(array);
	count = 0;
	int index = 0;
	double mean = 0.0;
	double sumsq = 0.0;
	for (double x: array) {
	    if (index == 11) break;
	    mean += x;
	    sumsq += x*x;
	    if (x > times[index]) {
		prob[index++] = ((double)count) / n;
	    }
	    count++;
	}
	mean /= n;
	double sdev = Math.sqrt((sumsq/n) - (mean*mean));
	// Print out the results
	for (int i = 0; i < 11; i++) {
	    System.out.format("P(t <= %g) = %g\n", times[i], prob[i]);
	}
	System.out.println("mean = " + mean
			   +", sdev = " + sdev);

	if (osg != null) {
	    // Create an output graph.
	    Graph graph = new Graph(osg);
	    graph.setOffsets(75, 75);
	    graph.setRanges(0.0, 100.0, 0.0, 1.0);

	    Graph.Axis xAxis =
		new Graph.Axis(0.0, 0.0,
			       Graph.Axis.Dir.HORIZONTAL_INCREASING,
			       100.2, 0.0, 10.0, false);
	    Graph.TickSpec xts1 =
		new Graph.TickSpec(4.0, 1.0, 1);
	    Graph.TickSpec xts2 =
		new Graph.TickSpec(8.0, 1.0, 5, "%3.1f", 10.0);
	    xAxis.addTick(xts1);
	    xAxis.addTick(xts2);
	    xAxis.setLabelOffset(10.0);
	    xAxis.setLabel("Delay in minutes");
	    
	    Graph.Axis yAxis =
		new Graph.Axis(0.0, 0.0,
			       Graph.Axis.Dir.VERTICAL_INCREASING,
			       1.002, 0.0, 0.1, true);

	    Graph.TickSpec yts1 =
		new Graph.TickSpec(4.0, 1.0, 1);
	    Graph.TickSpec yts2 =
		new Graph.TickSpec(8.0, 1.0, 5, "%3.1f", 10.0);
	    yAxis.addTick(yts1);
	    yAxis.addTick(yts2);

	    yAxis.setLabelOffset(10.0);
	    yAxis.setLabel("Cumulative Probability");

	    graph.draw(xAxis);
	    graph.draw(yAxis);

	    Path2D curve = new SplinePath2D(times, prob, false);
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setStroke(new BasicStroke(2.0F));
	    g2d.setColor(Color.BLACK);
	    graph.draw(g2d, curve);

	    Font font = new Font("SansSerif", Font.BOLD, 24);
	    graph.setFont(font);
	    graph.setFontJustification(Graph.Just.CENTER);
	    graph.setFontColor(Color.BLACK);
	    graph.drawString("Stephan Standard Time", 50.0, 0.9);
	    g2d.dispose();
	    
	    graph.write();
	}
    }
}
