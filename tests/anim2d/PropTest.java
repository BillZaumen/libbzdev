import org.bzdev.anim2d.*;
import org.bzdev.obnaming.*;
import org.bzdev.graphs.Graph;
import java.awt.Graphics2D;

class RefPointTest extends DirectedObject2D {
    public RefPointTest(Animation2D a2d, String name, boolean intern) {
	super(a2d, name, intern);
    }
    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS) {
	// don't do anything - we are just testing a factory.
    }
}

class RefPointTestFactory
    extends DirectedObject2DFactory<RefPointTest>
{
    public RefPointTestFactory(Animation2D a2d) {
	super(a2d);
    }

    @Override public RefPointTest newObject(String name) {
	return new RefPointTest(getAnimation(), name, willIntern());
    }
}


public class PropTest {

    
    public static void main(String argv[]) {

	try {
	    Animation2D a2d = new Animation2D();

	    AnimationObject2DFactory<?>[] array = {
		new GraphViewFactory(a2d),
		new AnimationPath2DFactory(a2d),
		new AnimationLayer2DFactory(a2d),
		new RefPointTestFactory(a2d)
	    };

	    for (AnimationObject2DFactory<?> factory: array) {
		System.out.format("------- FACTORY %s ----------\n",
				  factory.getClass().getName());
		for (String name: factory.parmNameSet()) {
		    System.out.format("%s: %s - %s\n",
				      name,
				      factory.getLabel(name),
				      factory.getTip(name));
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}

