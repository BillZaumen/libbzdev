import org.bzdev.drama.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import org.bzdev.devqsim.*;
import org.bzdev.drama.common.ConditionMode;
import org.bzdev.math.RootFinder;
import org.bzdev.math.MathException;
import org.bzdev.util.units.MKS;
// for testing
import org.bzdev.math.RungeKutta;
import org.bzdev.math.GLQuadrature;

/**
 * Actor representing a home.
 */
@DMethodContexts({
	@DMethodContext(helper = "org.bzdev.drama.DoReceive",
			localHelper = "DoReceiveHome"),
	@DMethodContext(helper = "org.bzdev.drama.OnConditionChange",
			localHelper = "OnConditionChangeHome")
	    })
public class Home extends Actor {
    DramaSimulation sim;

    boolean debug = false;
    boolean waiting  = false;
    boolean setWaitingCalled = false;

    /**
     * Wait for a specified time before determining when the air conditioner
     * should be turned on.
     * @param time the time to wait in seconds
     */
    public void  setWaiting(double time) {
	if (setWaitingCalled)
	    throw new IllegalStateException("setWaiting called more than once");
	setWaitingCalled = true;
	if (time > 0.0) {
	    waiting = true;
	    sim.scheduleCall(new Callable() {
		    public void call() {
			waiting = false;
			update();
			scheduleNextEvent();
		    }
		},
		Math.round(time*ticksPerUnitTime));
	}
    }

    // standard code for use of dynamic methods
    static {
	DoReceiveHome.register();
	OnConditionChangeHome.register();
    }

    private double ticksPerUnitTime;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of the object
     * @param intern true if the object will be interned in the
     *        simulation's tables; false otherwise
     */
    public Home(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	ticksPerUnitTime = sim.getTicksPerUnitTime();
	this.sim = sim;
	lastTime = sim.currentTicks();
    }

    boolean started = false;

    /**
     * Start this actor.
     * This must be called after initialization.
     */
    public void start() {
	if (started) return;
	setConstants();		// just to make sure.
	started = true;
	acOn = false;
	scheduleNextEvent();
    }

    Utility utility = null;

    /**
     * Set the utility.
     * This method can only be called once.
     * @param utility the utility
     */
    public void setUtility(Utility utility) {
	if (utility == null)
	    throw new IllegalArgumentException("null argument");
	if (this.utility != null)
	    throw new IllegalStateException("setUtility already called");
	utility.register(this);
	this.utility = utility;
    }

    private double desiredLowTemperature;
    private double desiredHighTemperature;

    /**
     * Set the desired high and low temperatures.
     * It is assumed that there is an air conditioner that
     * will turn on if the temperature is at or below the low
     * value and turn off it is at or above the high value.
     * @param low the low temperature (Kelvin)
     * @param the high temperature (Kelvin)
     */
    public void setLowHighTemperatures(double low, double high) {
	desiredLowTemperature = low;
	desiredHighTemperature = high;
	adjustRequestedTemperatures();
    }

    private double requestedLowTemperature;
    private double requestedHighTemperature;

    double k1;
    double k2;
    double k3;
    double acPower;

    /**
     * Set the power level for when the AC is on.
     */
    public void setACPower(double value) {
	acPower = value;
    }

    private double fraction = 1.0;
    private void adjustRequestedTemperatures() {
	requestedHighTemperature = outsideTemperature
	    - fraction * (outsideTemperature - desiredHighTemperature);
	requestedLowTemperature = outsideTemperature
	    - fraction *(outsideTemperature - desiredLowTemperature);
	if (requestedHighTemperature < desiredHighTemperature)
	    requestedHighTemperature = desiredHighTemperature;
	if (requestedLowTemperature < desiredLowTemperature)
	    requestedLowTemperature = desiredLowTemperature;
    }

    /**
     * Receive a message.
     * This implements a dynamic method.
     * @param msg the message
     * @param src the soruce of the message
     * @param wereQueued true if the message had been queued; false otherwise
     * @see org.bzdev.drama#doRecieve(Object,org.bzdev.drama.Actor,boolean)
     */
    @DMethodImpl("org.bzdev.drama.DoReceive")
    void doReceive1(ReductionMessage msg, Actor src, boolean wereQueued) {
	 update();
	 fraction = msg.getFraction();
	 adjustRequestedTemperatures();
	 scheduleNextEvent();
    }

    double a, b, c, q, qroot;
    boolean acOn = false;
    boolean qneg = false;
    double Tmin;

    /**
     * Set the constants k1 and k2.
     * With t denoting time and T denoting temperature, when the air
     * consition is on, the rate of change of inside temperature is given by
     * dT/dt = -k<sub>1</sub>T /(T<sub>0</sub> - T) + k<sub>2</sub>(T<sub>0</sub>-T)
     * and when the air conditioner is off,
     * dT/dt = k<sub>2</sub>(T<sub>0</sub>-T).
     * If the inside temperature is larger than the outside temperature,
     * then dT/dt = -k<sub>3</sub>.
     * 
     * @param k1 the value of k1 (must be positive)
     * @param k2 the value of k2 (must be positive)
     * @param k3 the value of k3 (must be positive)
     */
    public void setKs(double k1, double k2, double k3) {
	if (k1 <= 0.0 || k2 <= 0.0) throw new IllegalArgumentException();
	this.k1 = k1;
	this.k2 = k2;
	this.k3 = k3;
	setConstants();
    }

    // set constants a, b, c, q, and qroot based on the outside temperature and
    // the constants k1 and k2.  These are used by the methods f and timediff
    // and are set by this function call as they change only when k1, k2, or
    // the outside temperature change - it is more efficient to call them once
    // instead of multiple times.
    private void setConstants() {
	a = k2 * outsideTemperature * outsideTemperature;
	b = - (2.0 * k2 * outsideTemperature + k1);
	c = k2;
	q = 4.0 * a * c - b * b;
	qroot = Math.sqrt((q >= 0.0)? q: -q);
	Tmin = outsideTemperature + (k1 - qroot)/(2.0*k2);
    }

    private double f(double x) {
	double logarg = (2.0*c*x + b - qroot) / (2.0*c*x + b + qroot);
	if (logarg == 0.0)
	    throw new MathException("zero argument for logarithm");
	if (logarg < 0) logarg = -logarg;
	return (1.0/qroot)*Math.log(logarg);
    }

    double timediff(double T1, double T2) {
	if (acOn) {
	    if (T2 < Tmin && T1 > T2) {
		// will never reach T2 because it is below the minimum
		// temperature the AC can maintain.
		throw new IllegalArgumentException("T2 too small");
	    }
	    return (outsideTemperature + b/(2.0*c))*(f(T2)-f(T1))
		- Math.log((a + b * T2 + c * T2 * T2)
			   /(a + b * T1 + c * T1 * T1))
		/(2.0 * c);
	} else {
	    if (T1 > outsideTemperature
		&& outsideTemperature > requestedLowTemperature) {
		if (T2 < outsideTemperature) {
		    throw new MathException("T1 > T0 but T2 < T0");
		} else {
		    return k3*(T1-T2)/(T1-outsideTemperature);
		}
	    } else {
		return Math.log((outsideTemperature - T1)
				/ (outsideTemperature - T2))
		    / k2;
	    }
	}
    }

    /**
     * Calculate a new temperature.
     * @param tdelta the time interval (seconds)
     * @param lastT the temperature (Kelvin) at the start of the interval
     * @return the temperature (Kelvin) at the end of the interval
     */
    double tempAfterInterval(double tdelta, final double lastT) {
	if (acOn) {
	    RootFinder.Brent rfb = new RootFinder.Brent() {
		    public double function(double T) {
			return timediff(lastT, T);
		    }
		};
	    RootFinder.Newton rfn = new RootFinder.Newton() {
		    public double function(double T) {
			return timediff(lastT, T);
		    }
		    public double firstDerivative(double T) {
			double denom = k2*T*T
			    - (2.0*k2*outsideTemperature + k1) * T
			    + k2*outsideTemperature*outsideTemperature;
			if (denom == 0.0) {
			    throw new MathException
				("division by zero in Newton's method");
			} else {
			    return (outsideTemperature - T) / denom;
			}
		    }
		};
	    // if Newton's method would try a temperature below Tmin,
	    // timediff will return NaN, so we should use Brent's
	    // method in that case.
	    if (tdelta / rfn.firstDerivative(lastT) + lastT > Tmin) {
		try {
		    return rfn.solve(tdelta, lastT);
		} catch (Exception e) {
		    // revert to Brent's method if Netwon's method
		    // fails.
		    try {
			double x1 = rfb.function(lastT)-tdelta;
			double x2 = (lastT + Tmin) / 2.0;
			double x3 = rfb.function(x2)-tdelta;
			if (x1 == 0.0) return lastT;
			if (x3 == 0.0) return x2;
			while (Math.copySign(1.0, x1)
			       == Math.copySign(1.0, x3)) {
			    x2 = (x2 + Tmin) / 2.0;
			    x3 = rfb.function(x2)-tdelta;
			    if (x3 == 0.0) return x2;
			}
			return rfb.solve(tdelta, lastT, x2);
		    } catch (Exception ee) {
			System.out.println("tdelta = " + tdelta
					   + ", lastT = " + lastT
					   + ", Tmin = " + Tmin
					   + ", acOn = " + acOn
					   + ", requestedLowTemp = "
					   + requestedLowTemperature);
			System.out.println("outsideTemperature = "
					   + outsideTemperature
					   + ", requestedHighTemperature = "
					   + requestedHighTemperature);
			System.out.println("rfb.function(lastT) = "
					   + rfb.function(lastT)
					   + ", rfb.function(Tmin) = "
					   + rfb.function(Tmin));

			ee.printStackTrace();
			System.exit(1);
			return 0.0;
		    }
		}
	    } else {
		double x1 = rfb.function(lastT)-tdelta;
		double x2 = (lastT + Tmin) / 2.0;
		double x3 = rfb.function(x2)-tdelta;
		if (x1 == 0.0) return lastT;
		if (x3 == 0.0) return x2;
		while (Math.copySign(1.0, x1) == Math.copySign(1.0, x3)) {
		    x2 = (x2 + Tmin) / 2.0;
		    x3 = rfb.function(x2)-tdelta;
		    if (x3 == 0.0) return x2;
		}
		return rfb.solve(tdelta, lastT, x2);
	    }
	} else {
	    if (lastT > outsideTemperature &&
		outsideTemperature > requestedLowTemperature) {
		double newT = lastT - (lastT - outsideTemperature)*tdelta/k3;
		if (newT < outsideTemperature) {
		    throw new MathException
			("(lastT - k3 * deltat) < outsideTemperature");
		}
		return newT;
	    } else {
		return outsideTemperature
		    - (outsideTemperature - lastT) * Math.exp(-k2 * tdelta);
	    }
	}
    }

    double outsideTemperature = MKS.degC(20.0);

    /**
     * Dynamic method implementation for temperature conditions.
     * @param condition sending a notification
     * @param mode the type of notification
     * @param source the object sending the notification
     * @see org.bzdev.drama.Actor#onConditionChange(org.bzdev.drama.Condition,org.bzdev.drama.generic.ConditionMode,org.bzdev.devqsim.SimObject)
     */
    @DMethodImpl("org.bzdev.drama.OnConditionChange")
	void onConditionChange1(TemperatureCond condition,
				ConditionMode mode,
				SimObject source)
    {
	update();
	outsideTemperature = condition.getValue();
	setConstants();
	if (started) scheduleNextEvent();
    }

    double insideTemperature;
    double nextTemp;
    long lastTime = 0;

    /**
     * Set the initial value of the inside temperature
     * @param temp the temperature (Kelvin)
     */
    public void setInsideTemperature(double temp) {
	insideTemperature = temp;
	nextTemp = temp;
    }

    SimulationEvent sev = null;

    protected void update(double time, long simtime) {
	if (started && (lastTime == simtime)) return;
	if (sev != null && sev.isPending()) {
	    sev.cancel();
	    double tdelta = sim.currentTimeFrom(lastTime);
	    insideTemperature = tempAfterInterval(tdelta, insideTemperature);
	} else {
	    insideTemperature = nextTemp;
	}
	lastTime = simtime;
    }

    /**
     * Schedule the next simulation event. and store the next
     * value of the temperature.
     */
    void scheduleNextEvent() {
	lastTime = sim.currentTicks();
	if (waiting) {
	    acOn = false;
	    utility.adjustPower(this, 0.0);
	    nextTemp = outsideTemperature;
	} else if (insideTemperature > outsideTemperature) {
	    acOn = false;
	    utility.adjustPower(this, 0.0);
	    nextTemp = outsideTemperature;
	} else if (insideTemperature >= requestedHighTemperature) {
	    acOn = true;
	    utility.adjustPower(this, acPower);
	    nextTemp = requestedLowTemperature;
	} else if (insideTemperature <= requestedLowTemperature) {
	    acOn = false;
	    utility.adjustPower(this, 0.0);
	    if (requestedHighTemperature <= outsideTemperature) {
		nextTemp = requestedHighTemperature;
	    } else {
		nextTemp = outsideTemperature;
	    }
	} else {
	    if (acOn) {
		nextTemp = requestedLowTemperature;
	    } else {
		if (requestedHighTemperature <= outsideTemperature) {
		    nextTemp = requestedHighTemperature;
		} else {
		    nextTemp = outsideTemperature;
		}
	    }
	}
	if (insideTemperature != outsideTemperature
	    && nextTemp != outsideTemperature) {
	    double tInterval = timediff(insideTemperature, nextTemp);
	    long interval = Math.round(Math.ceil(tInterval * ticksPerUnitTime));
	    sev = sim.scheduleCall(new Callable() {
		    public void call() {
			update();
			scheduleNextEvent();
		    }
		}, interval);
	}
    }
}
