import org.bzdev.obnaming.NamedObjectFactory;
import org.bzdev.obnaming.ParmKeyType;
import java.util.*;
import org.bzdev.anim2d.Animation2D;

/**
 * Test listing of factory data.
 */
public class ListTest {
    public static void main(String argv[]) {
	Animation2D a2d = new Animation2D();
	try {
	    for (String className: argv) {
		System.out.println("trying " + className);
		Class factoryClass = Class.forName(className);
		Object object = a2d.createFactory(factoryClass);
	        // Object object =  factoryClass.newInstance();
		if (object instanceof NamedObjectFactory) {
		    NamedObjectFactory factory = (NamedObjectFactory) object;
		    System.out.println(className +":");
		    NamedObjectFactory.ParmNameIterator parmNames =
			factory.parmNames();
		    while (parmNames.hasNext()) {
			String name = parmNames.next();
			String label;
			try {
			    label = " (" + factory.getLabel(name) + ")";
			    if (label.equals(" ()")) label = "";
			} catch(MissingResourceException e) {
			    label = "";
			}
			
			System.out.println("    " + name + label + ":");
			Class<?> clasz = factory.getFactoryClass(name);
			if (clasz != null) {
			    System.out.println("        defining "
					       + "factory class: "
					       + clasz.getName());
			}
			clasz = factory.getType(name);
			if (clasz != null) {
			    System.out.println("        type: "
					       + clasz.getName());
			}
			System.out.println("        rvmode: "
					   + factory.getRVMode(name));
			clasz = factory.keyType(name);
			if (clasz != null) {
			    if (ParmKeyType.class.isAssignableFrom(clasz)) {
				ParmKeyType qn = factory.getParmKeyType(name);
				System.out.println("        key type: "
						   + (qn == null? "<unknown>":
						      qn.description()));
				System.out.println("        addable = "
						   + qn.isAddable());
			    } else {
				System.out.println("        key type: " 
						   + clasz.getName());
			    }
			}
			Number min = factory.getGLB(name);
			boolean minInRange = factory.glbInRange(name);
			Number max = factory.getLUB(name);
			boolean maxInRange = factory.lubInRange(name);
			if (min != null || max != null) {
			    String range = minInRange? "[": "(";
			    if (min == null) {
				range = range + "-\u221e, ";
			    } else {
				range = range + min.toString() +", ";
			    }
			    if (max == null) {
				range = range + "\u221e";
			    } else {
				range = range + max.toString();
			    }
			    range = range + (maxInRange? "]": ")");
			    System.out.println("        range: " +range);
			}
			System.out.println("        description: "
					   + factory.getTip(name));
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
