import org.bzdev.drama.*;
import org.bzdev.devqsim.*;
import org.bzdev.util.units.MKS;

/**
 * Condition representing a temperature.
 */
public class TemperatureCond extends DoubleCondition {

    DramaSimulation sim;

    long tstep;
    double ticksPerUnitTime;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object will be interned in the
     *        simulation's tables; false otherwise
     */
    public TemperatureCond(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
	ticksPerUnitTime = sim.getTicksPerUnitTime();
	tstep = Math.round(ticksPerUnitTime * MKS.minutes(1.0));
    }

    double lastNotifiedTemperature;

    double minTemp;
    double maxTemp;

    boolean configured = false;

    /**
     * Start a sequence of condition changes.
     */
    public void start() throws InterruptedException {
	configured = true;
	scheduleDefaultTask();
    }

    long maxtime;
    long risetime;
    
    double rate = 0.0;

    private void setRate() {
	double deltaTemp = (maxTemp - minTemp);
	double deltaTime = (double)risetime / ticksPerUnitTime;
	
	if (deltaTime != 0.0) {
	    rate = deltaTemp / deltaTime;
	}
    }


    /**
     * Set the maximum time in simulation ticks for temperature changes.
     * @param maxtime the maximum time in simulation ticks
     * @param risetime the time in ticks for the temperature to rise from its
     *        minimum value to its maximum value
     */
    public void setTimes(long maxtime, long risetime) {


	if (configured) throw new IllegalStateException("cannot configure");
	this.maxtime = maxtime;
	this.risetime = risetime;
	setRate();
    }

    /**
     * Set the maximum time in real-valued time units for temperature changes.
     * @param maxtime the maximum time in seconds
     * @param risetime the time in seconds for the temperature to rise from its
     *        minimum value to its maximum value
     */
    public void setTimes(double maxtime, double risetime) {
	if (configured) throw new IllegalStateException("cannot configure");
	this.maxtime = Math.round(ticksPerUnitTime * maxtime);
	this.risetime = Math.round(ticksPerUnitTime * risetime);
	setRate();
    }


    /**
     * Set the temperature range.
     * @param min the minimum temperature (Kelvin)
     * @param max the maximum temperature (Kelvin)
     */
    public void setTemperatureRange(double min, double max) {
	if (configured) throw new IllegalStateException("cannot configure");
	minTemp = min;
	maxTemp = max;
	setRate();
	setValue(min);
    }

    private double oldTemp;

    private void updateTemperature(long oldtime, long newtime, double incr) {
	double temperature = getValue();
	if (newtime < risetime) {
	    temperature += rate * incr;
	} else if (oldtime > maxtime - risetime) {
	    temperature -= rate * incr;
	}
	setValue(temperature);
	if (Math.abs(oldTemp - temperature) > 0.5) {
	    oldTemp = temperature;
	    notifyObservers();
	    completeNotification();
	}
    }

    /**
     * The default task.
     */
    protected void defaultTask() {
	long time = sim.currentTicks();

	while (time < maxtime) {
	    long ctime = sim.currentTicks();
	    /*
	    updateTemperature(time, ctime,
			      (ctime - time)/ticksPerUnitTime);
	    */
	    updateTemperature(time, ctime,
			      sim.currentTimeFrom(time));
	    if (time + tstep >= maxtime) break;
	    TaskThread.pause(tstep);
	    time = ctime;
	}
    }
}
