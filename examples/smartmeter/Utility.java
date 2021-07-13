import org.bzdev.drama.*;
import org.bzdev.devqsim.SimulationEvent;
import java.util.*;
import org.bzdev.lang.Callable;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import org.bzdev.graphs.Graph;

/**
 * Class representing a utility.
 */
public class Utility extends Actor {

    DramaSimulation sim;

    /**
     * Group for distributing messages to homes.
     *
     */
    static class HomeGroup extends Group {
	/**
	 * Constructor.
	 * @param sim the simulation
	 * @param name the name of the group
	 * @param intern true if the object should be interned in the
	 *        simulation tables; false if not
	 */
	public HomeGroup(DramaSimulation sim, String name, boolean intern) {
	    super(sim, name, intern);
	}

	@Override
	public Iterator<Actor> actorRecipientIterator(Actor source) {
	    return getActorMembers().iterator();
	}

	@Override
	public Iterator<Group> groupRecipientIterator(Actor source) {
	    return getGroupMembers().iterator();
	}
    }
    HomeGroup homes;
    long ticksPerUnitTime;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object will be interned in the
     *        simulation's tables; false otherwise
     */
    public Utility(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
	homes = new HomeGroup(sim, "<Utility internal Group>", false);
	ticksPerUnitTime = Math.round(sim.getTicksPerUnitTime());
    }

    double reductionFraction = 1.0;

    /**
     * Set the reduction fraction.
     * @param value a fraction in the range [0.0, 1.0]
     */
    public void setReductionFraction(double value) {
	if (value < 0.0 || value > 1.0)
	    throw new IllegalArgumentException("argument out of range");
	reductionFraction = value;
	send(new ReductionMessage(reductionFraction), homes, 0L);
    }

    private double powerOutput = 0.0;

    /**
     * Get the power output.
     * @return the power output
     */
    public double getPowerOutput() {return powerOutput;}

    private void incrementPowerOutput(double incr) {
	powerOutput += incr;
	if (powerOutput < 0.0) powerOutput = 0.0;
    }

    Map<Home,Double> powerTable = new HashMap<Home,Double>(1<<17);

    SimulationEvent sev = null;
    long lastTime = 0;
    long lastAvTime = 0;
    double sum = 0.0;

    double averagePower = 0.0;
    Graph graph = null;
    Graphics2D g2d = null;
    double lastx = 0.0;
    double lasty = 0.0;
    Line2D line = new Line2D.Double();

    /**
     * Set up this object to draw a curve to show the power level
     * as a function of time.
     * @param graph the graph
     * @param g2d A Graphics2D created with the graph's createGraphics()
     *        method
     */
    public void setupGraphics(Graph graph, Graphics2D g2d) {
	this.graph = graph;
	this.g2d = g2d;
    }


    /**
     * Compute the average power over a time interval.
     */
    void computeAveragePower() {
	long time = sim.currentTicks();
	double xtime = sim.currentTime();
	sum += powerOutput *  (time - lastTime);
	averagePower = sum / (time - lastAvTime);
	System.out.println("averagePower at time "
			   + xtime + " = " + averagePower);
	lastAvTime = time;
	lastTime = time;
	if (graph != null && g2d != null) {
	    line.setLine(lastx, lasty, xtime, lasty);
	    graph.draw(g2d, line);
	    line.setLine(xtime, lasty, xtime, averagePower);
	    graph.draw(g2d, line);
	}
	lastx = xtime;
	lasty = averagePower;
	sum = 0.0;

    }

    /**
     * Record the average power over an interval.
     * @param interval the interval in seconds
     */
    public void recordAveragePower(final double interval) {
	sim.scheduleCall(new Callable() {
		public void call() {
		    computeAveragePower();
		    recordAveragePower(interval);
		}
	    },
	    Math.round(interval*ticksPerUnitTime), 2.0);
    }

    /**
     * Adjust power.
     * This is called by instances of Home to indicate their power usage.
     * @param home the user
     * @param power the power level for that user
     */
    public void adjustPower(Home home, double power) {
	Double value = powerTable.get(home);
	powerTable.put(home, power);
	double old = (value == null)? 0.0: value;
	double incr = power - old;
	powerOutput += incr;
	// in case of round-off errors
	if (powerOutput < 0.0) powerOutput = 0.0;
	long time = sim.currentTicks();
	sum += powerOutput * (time - lastTime);
	lastTime = time;

	if (sev == null) {
	    sev = sim.scheduleCall(new Callable() {
		    public void call() {
			System.out.println("at time " + sim.currentTime()
					   + ", power output = " + powerOutput);
			sev = null;
		    }
		},
		0, 1.0);
	}
    }

    /**
     * Register a home with this utility.
     * @home the home
     */
    public void register(Home home) {
	home.joinGroup(homes);
	powerTable.put(home, 0.0);
	send(new ReductionMessage(reductionFraction), home, 0L);
    }
}
