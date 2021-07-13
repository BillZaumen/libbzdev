import org.bzdev.obnaming.*;

public class TestObject extends DefaultNamedObject<TestObject> {
    TestObject(TestNamer namer, String name, boolean intern) {
	super(namer, name, intern);
    }
}