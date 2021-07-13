import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import java.util.*;

@FactoryParmManager(value = "LTestParmManager",
		    tipResourceBundle = "LTestTips",
		    labelResourceBundle = "LTestLabels",
		    docResourceBundle = "LTestDocs")
public class LTestFactory
    extends NamedObjectFactory
	    <LTestFactory, ATestNamer, ATestObject, LTestObject>
{
    @PrimitiveParm("value1")
    int value1 = 0;

    ATestNamer namer;
    LTestParmManager pm;

    public LTestFactory() {
	this(null);
    }

    public LTestFactory(ATestNamer namer) {
	super(namer);
	this.namer = namer;
	pm = new LTestParmManager(this);
	initParms(pm, LTestFactory.class);
    }

    public void clear() {
	pm.setDefaults(this);
	super.clear();
    }

    protected LTestObject newObject(String name) {
	return new LTestObject(getObjectNamer(), name, willIntern());
    }

    protected void initObject(LTestObject object) {
	object.setValue1(value1);
    }
}
