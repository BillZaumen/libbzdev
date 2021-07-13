import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.units.*;

/**
 * Factory for instances of TemperatureCondition.
 * <P>
 * Parameters:
 * <ul>
 *   <li><code>maxtime</code>. The maximum time in seconds for a
 *       temperature change.
 *   <li><code>risetime</code>. The time it takes for the temperature to
 *       transition from the minimum value to the maximum value and
 *       vice verse.
 *   <li><code>minTemperature</code>. The minimum temperature (units = Kelvin).
 *   <li><code>maxTemperature</code>. The maximum temperature (units = Kelvin).
 * </ul>
 */
@FactoryParmManager(value = "TempCondFactoryParmManager",
		    labelResourceBundle = "TempCondLabels",
		    tipResourceBundle = "TempCondTips")
public class TemperatureCondFactory
    extends AbstractDoubleCondFactory<TemperatureCond>
{
    @PrimitiveParm(value="maxtime", lowerBound="0.0", lowerBoundClosed = false)
    double maxtime = MKS.hours(16.0);

    @PrimitiveParm(value="risetime", lowerBound="0.0", lowerBoundClosed = false)
    double risetime = MKS.hours(3.0);

    @PrimitiveParm(value="minTemperature",
		   lowerBound="0.0",
		   lowerBoundClosed = false)
    double minTemperature = MKS.degC(18.0);

    @PrimitiveParm(value="maxTemperature",
		   lowerBound="0.0",
		   lowerBoundClosed = false)
    double maxTemperature = MKS.degC(30.0);

    /**
     * Constructor for service provider.
     */
    public TemperatureCondFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public TemperatureCondFactory(DramaSimulation sim) {
	super(sim);
	TempCondFactoryParmManager pm = new TempCondFactoryParmManager(this);
	initParms(pm, TemperatureCondFactory.class);
    }


    @Override
    protected TemperatureCond newObject(String name) {
	return new TemperatureCond(getSimulation(), name, willIntern());
    }

    @Override
    protected void initObject(TemperatureCond obj) {
	obj.setTimes(maxtime, risetime);
	obj.setTemperatureRange(minTemperature, maxTemperature);
	try {
	    obj.start();
	} catch (InterruptedException ie) {
	    throw new RuntimeException("TemperatureCond " + obj.getName()
				       +" not started", ie);
	}
    }
}
