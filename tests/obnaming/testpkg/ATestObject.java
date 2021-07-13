package testpkg;

import org.bzdev.obnaming.NamedObjectOps;
import org.bzdev.obnaming.annotations.NamedObject;

@NamedObject(helperClass="OHelper", 
	     helperSuperclass = "ATestObjectSC",
	     helperSuperclassTypeParms = "<Integer,Double>",
	     helperSuperclassConstrTypes = {
		 @NamedObject.ConstrTypes,
		 @NamedObject.ConstrTypes(value={"Integer", "Double"},
					  exceptions={"Exception"})
	     },
	     namerHelperClass="NHelper",
	     namerClass="ATestNamer")
public class ATestObject extends OHelper implements NamedObjectOps {
    public ATestObject(ATestNamer namer, String name, boolean intern)
    {
	super(namer, name, intern);
    }
    public ATestObject(ATestNamer namer, String name, boolean intern,
		       Integer ind, Double x) 
	throws IllegalArgumentException, Exception
    {
	super(namer, name, intern, ind, x);
    }
}
