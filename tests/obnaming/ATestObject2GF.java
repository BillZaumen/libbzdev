import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import java.util.Set;
import java.util.HashSet;

@FactoryParmManager("ATestObject2GParmManager")
public class ATestObject2GF<T extends Number, S extends Set<T>>
    extends NamedObjectFactory
	    <ATestObject2GF<T,S>, ATestNamer, ATestObject, ATestObject2>
{
    @PrimitiveParm("value1")
	int value1 = 0;

    @PrimitiveParm(value="value2", rvmode=true)
	IntegerRandomVariable value2 = new UniformIntegerRV(0, 10);

    @PrimitiveParm(value="value3", rvmode = true)
	IntegerRandomVariableRV<? extends IntegerRandomVariable> value3 =
	new FixedIntegerRVRV(new UniformIntegerRV(0, 10));


    ATestNamer namer;
    ATestObject2GParmManager<T,S> pm;

    // don't need to use these - just see if the compiler is happy.
    T number = null;
    S ourSet = null;

    public ATestObject2GF(ATestNamer namer) {
	super(namer);
	this.namer = namer;
	// addDefault(ATestObject2Factory.class, "defaults");
	pm = new  ATestObject2GParmManager<T,S>(this);
	initParms(pm, ATestObject2GF.class);
	// pm.setDefaults(this);
    }

    public void clear() {
	pm.setDefaults(this);
	super.clear();
    }

    protected ATestObject2 newObject(String name) {
	return new ATestObject2(getObjectNamer(), name, willIntern());
    }

    protected void initObject(ATestObject2 object) {
	object.setValue1(value1);
	object.setValue2(value2.next());
	object.setValue3(value3.next());
    }
}
