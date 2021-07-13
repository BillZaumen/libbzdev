import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import org.bzdev.util.units.MKS;
import org.bzdev.math.RootFinder;
import org.bzdev.math.MathException;

/**
 * Factory for instances of Home.
 * <P>
 * Parameters:
 * <ul>
 *   <li> <code>domainMember</code>. Used to configure an instance of
 *        DomainMember for handle domain membership. 
 *   <li> <code>domain</code>. Used to specify a domain that can added
 *        or removed from the actor's domain set. For this parameter,
 *        the key is the domain and the value is a boolean
 *        that is true if conditions for that domain should be
 *        tracked; false otherwise. If a specified domain was already
 *        joined by a shared domain member, an explicit request to
 *        join that domain will be ignored when an actor is created.
 *   <li> <code>utility</code>. The actor representing a utility.
 *   <li><code>referenceTemperature</code>. A temperature, roughly the
 *             peak outside temperature, used in computing the constants
 *             k1, k2, and k3 given the parameters warmingTime, coolingTime,
 *             fanCoolingTime, and some other parameters.
 *   <li><code>warmingTime</code>. The time it takes for the temperature to
 *             rise from the value of the parameter desiredLowTemperature
 *             to the value of the parameter desiredHighTemperature when the
 *             air conditioner is off and the outside temperature is 
 *             the reference temperature.
 *   <li><code>coolingTime</code>. The time it takes for the temperatue to
 *             fall from the value of the parameter desiredHighTemperature to
 *             the value of the parameter desiredLowTemperature when the air
 *             condition is on and the outside temperature is the reference
 *             temperature.
 *   <li><code>fanCoolingTime</code>. The time it takes for the temperature
 *             to fall from a higher value to the outside temperature when
 *             the cooling is provided by a fan moving cooler outside air inside.
 *   <li> <code>desiredLowTemperature</code>. The lowest value of the range of
 *        temperatures that an air conditioner maintains (Kelvin).
 *   <li> <code>desiredHighTemperature</code>. The highest value of the range of
 *        temperatures that an air conditioner maintains (Kelvin).
 *   <li> <code>acPower</code>. The power the air conditioner uses when cooling
 *        (Watts).
 *   <li> <code>initialTemperature</code>. the initial temperature inside
 *        the home (Kelvin).
 *   <li><code>waitTime</code>. The time to wait from the start of the
 *        simulation before the air conditioner can turn on.
 * </ul>
 */
@FactoryParmManager(value = "HomeFactoryParmManager",
		    labelResourceBundle = "HomeFactoryLabels",
		    tipResourceBundle = "HomeFactoryTips")
public class HomeFactory extends AbstractActorFactory<Home> {
    
    @PrimitiveParm(value="utility")
    Utility utility = null;

    /*
    @PrimitiveParm(value="k1", rvmode = true, lowerBound="0.0")
    DoubleRandomVariable k1 = new FixedDoubleRV(0.0);

    @PrimitiveParm(value="k2", rvmode = true, lowerBound="0.0")
    DoubleRandomVariable k2 = new FixedDoubleRV(0.0);
    */

    @PrimitiveParm(value="warmingTime", rvmode=true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable warmingTime = new FixedDoubleRV(MKS.minutes(30.0));
	
    @PrimitiveParm(value="coolingTime", rvmode=true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable coolingTime = new FixedDoubleRV(MKS.minutes(5.0));

    
    @PrimitiveParm(value="fanCoolingTime", rvmode=true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable fanCoolingTime = new FixedDoubleRV(MKS.minutes(1.0));

    @PrimitiveParm(value="referenceTemperature", lowerBound="0.0",
		   lowerBoundClosed=false)
    double referenceTemperature = MKS.degC(30.0);


    @PrimitiveParm(value="desiredLowTemperature", rvmode = true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable desiredLowTemperature
	=  new FixedDoubleRV(MKS.degC(20.0));

    @PrimitiveParm(value="desiredHighTemperature", rvmode = true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable desiredHighTemperature
	=  new FixedDoubleRV(MKS.degC(22.0));

    @PrimitiveParm(value="acPower", rvmode = true,
		   lowerBound="0.0", lowerBoundClosed=true)
    DoubleRandomVariable acPower
	= new FixedDoubleRV(3500.0);

    @PrimitiveParm(value="initialTemperature", rvmode = true,
		   lowerBound="0.0", lowerBoundClosed=false)
    DoubleRandomVariable initialTemperature
	= new FixedDoubleRV(MKS.degC(18.0));

    @PrimitiveParm(value="waitTime", rvmode = true,
		   lowerBound="0.0", lowerBoundClosed=true)
    DoubleRandomVariable waitTime
	= new FixedDoubleRV(0.0);

    /**
     * Constructor for service provider.
     */
    public HomeFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public HomeFactory(DramaSimulation sim) {
	super(sim);
	HomeFactoryParmManager pm = new HomeFactoryParmManager(this);
	initParms(pm, HomeFactory.class);
    }

    @Override
    protected Home newObject(String name) {
	return new Home(getSimulation(), name, willIntern());
    }

    static class KParms {
	double k1;
	double k2;
	double k3;
    }

    private KParms getKs(final double dl, final double dh, double wt, 
			 final double ct, double fctime) {

	final double k2 = Math.log((referenceTemperature - dl)
				   / (referenceTemperature - dh))
	    / wt;

	RootFinder.Brent rf = new RootFinder.Brent() {
		double a; double b; double c;
		double q; double qroot; double Tmin;

		private double f(double x) {
		    double logarg = (2.0*c*x + b - qroot)
			/ (2.0*c*x + b + qroot);
		    if (logarg == 0.0)
			throw new MathException("zero argument for logarithm");
		    if (logarg < 0) logarg = -logarg;
		    return (1.0/qroot)*Math.log(logarg);
		}

		double timediff(double T1, double T2) {
		     if (T2 < Tmin && T1 > T2) {
			 // will never reach T2 because it is below the minimum
			 // temperature the AC can maintain.
			 throw new IllegalArgumentException("T2 too small");
		     }
		     return (referenceTemperature + b/(2.0*c))*(f(T2)-f(T1))
			 - Math.log((a + b * T2 + c * T2 * T2)
				    /(a + b * T1 + c * T1 * T1))
			 /(2.0 * c);
		}

		public double function(double k1) {
		    a = k2 * referenceTemperature * referenceTemperature;
		    b = - (2.0 * k2 * referenceTemperature + k1);
		    c = k2;
		    q = 4.0 * a * c - b * b;
		    qroot = Math.sqrt((q >= 0.0)? q: -q);
		    Tmin = referenceTemperature + (k1 - qroot)/(2.0*k2);
		    double result = timediff (dh, dl) - ct;
		    /*
		    System.out.println("trying k1 = " + k1
				       + ", timediff - ct = " + result);
		    */
		    return result;
		}
	    };
	double tmp = (referenceTemperature - dl);
	double lguess = (referenceTemperature * Math.log(dh/dl) - (dh-dl)) / ct;
	if (lguess < 0.0)
	    throw new IllegalArgumentException("bad choice of of parameters");
	int n = 2;
	double hguess = n * lguess;
	while (rf.function(hguess) > ct) hguess = lguess * (++n);
	// System.out.println("lguess = " + lguess + ", hguess = " + hguess);
	double k1 = rf.findRoot(lguess, hguess);
	// System.out.println("k1 = " + k1 + ", k2 = " + k2);
	KParms kp = new KParms();
	kp.k1 = k1;
	kp.k2 = k2;
	kp.k3 = fctime;
	return kp;
    }


    @Override
    protected void initObject(Home obj) {
	super.initObject(obj);
	obj.setUtility(utility);
	KParms ks;
	double dl, dh, wt, ct, fct;

	for (;;) {
	    dl = desiredLowTemperature.next();
	    dh = desiredHighTemperature.next();
	    wt = warmingTime.next();
	    ct = coolingTime.next();
	    fct = fanCoolingTime.next();
	    try {
	       ks = getKs(dl,dh, wt, ct, fct);
	       break;
	    } catch (Exception e) {
		continue;
	    }
	}
	double wtt = waitTime.next();

	obj.setLowHighTemperatures(dl, dh);
	obj.setKs(ks.k1, ks.k2, ks.k3);
	obj.setACPower(acPower.next());
	if (wtt != 0.0) obj.setWaiting(wtt);

	if (false) {
	  // turn on to test that we computed k1 and k2 correctly.
	    boolean acSave = obj.acOn;
	    double ot = obj.outsideTemperature;
	    obj.outsideTemperature = referenceTemperature;
	    obj.setKs(ks.k1, ks.k2, ks.k3);
	    obj.acOn = false;
	    /*
	    System.out.println("wt = " + wt + ", calculated as "
			       + obj.timediff(dl, dh));
	    */
	    obj.acOn = true;
	    /*
	    System.out.println("ct = " + ct + ", calculated as "
			       + obj.timediff(dh, dl));
	    */
	    obj.acOn = acSave;
	    obj.outsideTemperature = ot;
	}
	obj.setInsideTemperature(initialTemperature.next());
	obj.start();
    }
}
