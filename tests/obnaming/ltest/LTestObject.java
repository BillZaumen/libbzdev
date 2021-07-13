
public class LTestObject extends ATestObject2 {

    // test class to make sure we can instantiate other classes
    // stored in the jar file besides what we actually create
    // even though the factory pretty much does this anyway.
    static class Other {
	double value = 20.0;
    }

    Other other;

    public LTestObject(ATestNamer namer, String name, boolean intern) {
	super(namer, name, intern);
	other = new Other();
    }

}