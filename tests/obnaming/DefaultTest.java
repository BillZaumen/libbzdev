import org.bzdev.obnaming.*;

abstract class OurNamedObject extends DefaultNamedObject<OurNamedObject>
{
    protected OurNamedObject(OurObjectNamer namer,
			     String name,
			     boolean intern)
    {
	super(namer, name, intern);
    }
}

class OurNamedObject1 extends OurNamedObject {
    int x = 10;
    public OurNamedObject1(OurObjectNamer namer,
			   String name,
			   boolean intern)
    {
	super(namer, name, intern);
    }
}

class OurObjectNamer
    extends DefaultObjectNamer<OurNamedObject>
{
    public OurObjectNamer() {
	super(OurNamedObject.class);
    }
}
			     
abstract class OurObjectFactory<OBJ extends OurNamedObject>
    extends DefaultNOFactory<OurObjectNamer, OurNamedObject, OBJ>
{
    protected OurObjectFactory(OurObjectNamer namer) {
	super(namer);
    }
}

class OurObjectFactory1 extends OurObjectFactory<OurNamedObject1> {
    public OurObjectFactory1(OurObjectNamer namer) {
	super(namer);
    }
    protected OurNamedObject1 newObject(String name) {
	return new OurNamedObject1(getObjectNamer(), name, willIntern());
    }
}

public class DefaultTest {
    static public void main(String argv[]) throws Exception {
	OurObjectNamer namer = new OurObjectNamer();
	OurObjectFactory1 factory = new OurObjectFactory1(namer);

	OurNamedObject1 object = factory.createObject("object");
	System.out.println("object name = " + object.getName());
	System.out.println("object.x = " + object.x);
	if (object != namer.getObject("object")) {
	    System.out.println("object not found");
	    System.exit(1);
	}
	System.exit(0);
    }
}