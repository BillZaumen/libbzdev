import org.bzdev.drama.*;
import org.bzdev.util.units.MKS;
import org.bzdev.math.rv.GaussianRV;
import org.bzdev.math.rv.UniformDoubleRV;
import org.bzdev.lang.Callable;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graph.Axis;
import org.bzdev.graphs.Graph.Axis.Dir;
import org.bzdev.graphs.Graph.TickSpec;
import org.bzdev.gio.OutputStreamGraphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;

/**
 * Simulation of the use of smart meters.
 * This uses an oversimplified model - the purpose was to illustrate
 * how to write a simulation using the bzdev library.
 * <P>
 * The model assumes that homes use electricity to run air conditioners.
 * An air condition is set up so that it turns on when the temperature
 * exceeds a value T<sub>dh</sub> and then turns off when the temperature
 * falls to a value of T<sub>dl</sub>. A utility can send a message requesting
 * that the air conditioner temporarily change these values to
 * T<sub>rh</sub> and T<sub>rl</sub> respectively. When the temperature
 * in a home exceeds both T<sub>rh</sub> and the outside temperature
 * T<sub>0</sub>, the rate of change of temperature is given by
 * dT/dt = -k<sub>3</sub> (one can simply pump the cooler outside air using
 * a fan whose energy usage is negligible).  Otherwise, when the air
 * conditioner is on, the rate of change of temperature is given by
 * <blockquote>
 * dT/dt = -k<sub>1</sub>T/(T<sub>0</sub>-T) + k<sub>2</sub>(T<sub>0</sub>-T)
 * </blockquote>
 * When the air conditioner is off, the rate of change of the temperature
 * inside a home is given by
 * <blockquote>
 * dT/dt = k<sub>2</sub>(T<sub>0</sub>-T)
 * </blockquote>
 * The temperature T<sub>0</sub> varies with time. As a simplification, it
 * is varied in small steps to avoid sending continual temperature updates.
 * <P>
 * The command-line flags are
 * <ul>
 *   <li><code>--type TYPE</code> - TYPE is the name of an image type. E.g.,
 *       jpeg, png, or ps.  The default is png.
 *   <li><code>--usageReduction</code> - between a time of 6.4 and 9.6 hours
 *               (the starting time is 0), all homes in the simulation receive
 *               a request to set T<sub>rl</sub> to
 *               T<sub>0</sub>-0.7(T<sub>0</sub>-T<sub>dl</sub>) and
 *               T<sub>rh</sub> to
 *               T<sub>0</sub>-0.7(T<sub>0</sub>-T<sub>dh</sub>)
 *   <li><code>--useWait WAIT_TIME</code> - configure homes with a varying
 *               time at which air conditioning may be turned on.
 *   <li><code>--phaseInReduction</code> - phase in the usage reduction over
 *               a 30 minute interval.
 * </ul>
 */
public class SmartmeterSim {

    /**
     * Main program.
     * @param argv the command-line arguments
     */
    public static void main(String argv[]) {

	final DramaSimulation sim = new DramaSimulation(1000.0);

	HomeFactory hf = new HomeFactory(sim);
	UtilityFactory uf = new UtilityFactory(sim);
	TemperatureCondFactory tf = new TemperatureCondFactory(sim);
	DomainFactory df = new DomainFactory(sim);

	double maxtime =  MKS.hours(16.0);
	double risetime = MKS.hours(3.0);

	boolean usageReduction = false;
	boolean phaseInReduction = false;
	boolean useWait = false;
	String type = "png";

	int ind = 0;
	try {
	    while (ind < argv.length && argv[ind].startsWith("-")) {
		if (argv[ind].equals("--usageReduction")) {
		    usageReduction = true;
		} else if (argv[ind].equals("--useWait")) {
		    useWait = true;
		} else if (argv[ind].equals("--type")) {
		    if ((++ind) < argv.length) {
			type = argv[ind];
		    } else {
			throw new Exception("type missing");
		    }
		} else if (argv[ind].equals("--phaseInReduction")) {
		    phaseInReduction = true;
		}
		ind++;
	}

	    Graph graph = null;
	    Graphics2D g2d = null;
	    if (ind < argv.length) {
		File file = new File(argv[ind]);
		OutputStream os = new FileOutputStream(file);
		OutputStreamGraphics osg =
		    OutputStreamGraphics.newInstance(os, 800, 600,
						     type);
		graph = new Graph(osg);
		graph.setOffsets(75, 75);
		graph.setRanges(0.0, MKS.hours(16.0),
				0.0, 4.0E7);
		/*
		Graph.Axis xaxis =
		    new Graph.Axis(0.0, 0.0,
				   Graph.Axis.Dir.HORIZONTAL_INCREASING,
				   MKS.hours(16.0),
				   0.0, MKS.minutes(1.0),
				   false) {
			public double axisValue(long ind) {
			    return (double)(ind/60);
			}
		    };
		*/
		Graph.Axis xaxis =
		    new TimeAxis(0.0, 0.0, Graph.Axis.Dir.HORIZONTAL_INCREASING,
				   MKS.hours(16.0), 0.0, MKS.minutes(1.0),
				 false);
		xaxis.setLabel("Time in hours");
		xaxis.setLabelOffset(10.0);
		xaxis.setWidth(2.0);
		xaxis.addTick(new Graph.TickSpec(3.0, 1.0, 10));
		xaxis.addTick(new Graph.TickSpec(5.0, 1.5, 60,
						 "%2.0f", 5.0));
		
		Graph.Axis yaxis =
		    new Graph.Axis(0.0, 0.0,
				   Graph.Axis.Dir.VERTICAL_INCREASING,
				   4.0E7,
				   0.0, 1.0E6,
				   true);
		yaxis.setLabel("Power in Watts");
		yaxis.setLabelOffset(10.0);
		yaxis.setWidth(2.0);
		yaxis.addTick(new Graph.TickSpec(3.0, 1.0, 1));
		yaxis.addTick(new Graph.TickSpec(5.0, 1.6, 10,
						 "%#2.0g", 5.0));
		graph.draw(xaxis);
		graph.draw(yaxis);
		g2d = graph.createGraphics();
	    }

	    tf.set("maxtime", maxtime);
	    tf.set("risetime", risetime);
	    tf.set("minTemperature", MKS.degC(18.0));
	    tf.set("maxTemperature", MKS.degC(30.0));
	    TemperatureCond outsideTempCond =
		tf.createObject("outsideTemperature");

	    df.add("condition", outsideTempCond);

	    Domain area = df.createObject("area");

	    final Utility utility = uf.createObject("utility");

	    utility.setupGraphics(graph, g2d);
	    utility.recordAveragePower(1.0);

	    hf.set("domain", area, true);
	    hf.set("utility", utility);
	    hf.set("desiredLowTemperature",
		   new GaussianRV(MKS.degC(20.0), 0.5));
	    hf.set("desiredHighTemperature",
		   new GaussianRV(MKS.degC(22.0), 0.5));
	    hf.set("referenceTemperature", MKS.degC(30.0));
	    hf.set("warmingTime", new GaussianRV(MKS.minutes(30.0),
						 MKS.minutes(10.0)));
	    hf.set("coolingTime",new GaussianRV(MKS.minutes(5.0), 180.0));
	    hf.set("fanCoolingTime", new GaussianRV(MKS.minutes(5.0), 60.0));

	    hf.set("initialTemperature", new GaussianRV(MKS.degC(18.0), 1.0));
	    hf.set("acPower", new GaussianRV(3500.0, 100.0));
	    if (useWait) {
		hf.set("waitTime",
		       new UniformDoubleRV(MKS.hours(1.0), true,
					   MKS.hours(2.0), true));
	    }

	    int n = 10000;
	    Home[] homes = hf.createObjects(new Home[n], "home", n);

	    long time1 = Math.round(sim.getTicksPerUnitTime() * maxtime *0.4);
	    long time2 = Math.round(sim.getTicksPerUnitTime() * maxtime *0.6);

	    System.out.println("time1 for reduction fraction = " + time1);
	    System.out.println("time2 for reduction fraction = " + time2);
	    if (usageReduction) {
		if (phaseInReduction) {
		    double tm = 30.0 * sim.getTicksPerUnitTime();
		    for (int i = 1; i <= 120.0; i++) {
			final double f = 1.0 - 0.3 * (i / 120.0);
			sim.scheduleCall(new Callable() {
				public void call() {
				    System.out.println
					("reduction fraction set to " + f
					 + " at " + sim.currentTime());
				    utility.setReductionFraction(f);
				}
			    }, Math.round(time1 + i * tm));

		    }
		} else {
		    sim.scheduleCall(new Callable() {
			    public void call() {
				System.out.println
				    ("reduction fraction set to 0.7 at "
				     +sim.currentTime());
				utility.setReductionFraction(0.7);
			    }
			}, time1);
		}
		if (phaseInReduction) {
		    double tm = 30.0 * sim.getTicksPerUnitTime();
		    for (int i = 1; i <= 120.0; i++) {
			final double f = 0.7 + 0.3 * (i / 120.0);
			sim.scheduleCall(new Callable() {
				public void call() {
				    System.out.println
					("reduction fraction set to " + f
					 + " at " + sim.currentTime());
				    utility.setReductionFraction(f);
				}
			    }, Math.round(time2 - (120-i) * tm));

		    }
		} else {
		    sim.scheduleCall(new Callable() {
			    public void call() {
				System.out.println
				    ("reduction fraction set to 1.0 at "
				     + sim.currentTime());
				utility.setReductionFraction(1.0);
			    }
			}, time2);
		}
	    }
	    sim.run(Math.round(sim.getTicksPerUnitTime() * maxtime));
	    if (g2d != null) g2d.dispose();
	    if (graph != null) {
		graph.write();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} 
    }
}
