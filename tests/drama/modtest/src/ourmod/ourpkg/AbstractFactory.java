package ourpkg;
import org.bzdev.drama.*;
import org.bzdev.math.rv.*;
import org.bzdev.obnaming.annotations.*;

@FactoryParmManager(value = "FactoryPM",
		    stdFactory = "Factory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstractFactory<Obj extends TestActor>
    extends AbstractActorFactory<Obj>
{
    @PrimitiveParm("value")
    int value = 0;

    @PrimitiveParm("rv")
    DoubleRandomVariable rv = new FixedDoubleRV(0.0);

    FactoryPM<Obj> pm;

    public AbstractFactory(DramaSimulation sim) {
	super(sim);
	pm = new FactoryPM<Obj>(this);
	initParms(pm, AbstractFactory.class);
    }

    @Override
    public void clear() {
	pm.setDefaults(this);
	super.clear();
    }

    @Override
    protected void initObject(TestActor object) {
	object.setValue(value);
	object.setRV(rv);
    }

}
