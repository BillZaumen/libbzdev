package org.bzdev.lang;
import java.lang.annotation.*;
import java.security.CodeSource;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.bzdev.lang.annotations.DMethodContext;
import org.bzdev.lang.annotations.DMethodContexts;

//@exbundle org.bzdev.lang.lpack.Lang

/**
 * Class loader supporting dynamic methods.
 * @deprecated
 * This class has been deprecated due to a change in OpenJDK.
 * For about 2 decades, this class would work with the standard
 * Java class loader. With the introduction of modules, the class loader
 * became much more restrictive. Test cases that used to work failed because
 * this class could no longer execute the register method on behalf of the
 * user.
 * <P>
 * The original documentation was:
 * <P>
 * With the default class loader, each local helper class has to
 * be loaded explicitly, typically by calling the helper's
 * <code>register()</code> static method from the corresponding
 * class implementing a dynamic method.  <code>DMClassLoader</code>
 * eliminates the need to do that by calling the <code>register()</code> 
 * methods automatically.
 * <p>
 * To use this class loader as the system class loader, set the system
 * property java.lang.class.loader to org.bzdev.lang.DMClassLoader when
 * the JVM is started.  One may also set the context class loader for
 * each thread.
 * <P>
 * This class loader delegates class loading in the same way as
 * {@link java.lang.ClassLoader java.lang.ClassLoader}
 * but adds the dynamic-method initialization described above. It also
 * verifies that a class implementing dynamic methods and its local helper
 * have the same code source and are in the same package.
 * <P>
 * If run when the default security manager is installed, the
 * runtime permission getProtectionDomain must be granted. This is used
 * for verifying that dynamic-method annotations are in the same protection
 * domain as this class loader, and that a class and its helper class are
 * similarly in the same protection domain in addition to being in the same
 * package.
 *
 * @see java.lang.Thread#setContextClassLoader(java.lang.ClassLoader)
 * @see java.lang.ClassLoader
 */
@Deprecated
public class DMClassLoader extends ClassLoader {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }

    private static void checkCodeSource() {
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
		public Void run() {
		    CodeSource clcs =
			DMClassLoader.class.getProtectionDomain().getCodeSource();
		    CodeSource mdccs =
			DMethodContext.class.getProtectionDomain().getCodeSource();
		    CodeSource mdcscs =
			DMethodContexts.class.getProtectionDomain().getCodeSource();
		    if (!clcs.equals(mdccs) || !clcs.equals(mdcscs)) {
			throw new SecurityException
			    (errorMsg("dmDiffer"));
		    }
		    return null;
		}
	    });
    }

    private static void checkCodeSource(final Class<?> cl1,
					final Class<?> cl2)
    {
	Package pack1 = cl1.getPackage();
	Package pack2 = cl2.getPackage();
	String p1 = (pack1 == null)? "": pack1.getName();
	String p2 = (pack2 == null)? "": pack2.getName();
	if (!p1.equals(p2)) {
	    throw new SecurityException
		(errorMsg("localHelperCheck2", cl1.getName(), cl2.getName()));
	}
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
		public Void run() {
		    CodeSource cl1cs =
			cl1.getProtectionDomain().getCodeSource();
		    CodeSource cl2cs =
			cl2.getProtectionDomain().getCodeSource();
		    if (cl1cs == null || cl2cs == null) {
			if (cl1cs != cl2cs) {
			    throw new SecurityException
				(errorMsg("codeBaseCheck", cl1.getName(), cl2.getName()));
			}
		    } else if (!cl1cs.equals(cl2cs)) {
			throw new SecurityException
				(errorMsg("codeBaseCheck", cl1.getName(), cl2.getName()));
		    }
		    return null;
		}
	    });
    }

    /**
     * Constructor.
     */
    public DMClassLoader() {
	super();
	checkCodeSource();
    }

    /**
     * Constructor.
     * @param parent the parent class loader
     */
    public DMClassLoader(ClassLoader parent) {
	super(parent);
	checkCodeSource();
    }

    private String getHelperClassName(Class<?> clazz,
				      DMethodContext annotation) 
	throws ClassNotFoundException
    {
	String name = annotation.localHelper();
	Package p = clazz.getPackage();
	String pName = (p == null)? "": p.getName() + ".";
	if (name.startsWith(pName)) {
	    return name;
	} else if (name.contains(".")) {
	    throw new ClassNotFoundException
		(errorMsg("localHelperCheck1", name));
	} else {
	    return pName + name;
	}
    }

    private void doLoadClasses(Class<?> clazz) 
	throws ClassNotFoundException
    {
	Class<?> superclass = clazz.getSuperclass();
	// Load the local-helper class using the same class loader
	// that the class we are processing used.
	ClassLoader cl = clazz.getClassLoader();
	if (superclass != null) {
	    // recursion necessary because, while loadClass will
	    // load the superclass, this is actually done by the
	    // parent class loader, which does not call the
	    // methods of this class loader.
	    // For safety (e.g., in case of an unexpected corner case),
	    // we load the helpers for our superclass first.
	    doLoadClasses(superclass);
	}
	DMethodContext a = clazz.getAnnotation(DMethodContext.class);
	if (a != null) {
	    String hname = getHelperClassName(clazz, a);
	    try {
		// check that the helper class and its DM class have
		// the same code source.
		Class<?>helperClass = cl.loadClass(hname);
		checkCodeSource(clazz, helperClass);
		// forces initialization & loads the helper class
		// using the same class loader as that used for clazz.
		Class.forName(hname, true, cl);
	    } catch (Exception e) {
		String msg = errorMsg("missingHelper", hname);
		throw new ClassNotFoundException(msg, e);
	    }
	}
	DMethodContexts aa = clazz.getAnnotation(DMethodContexts.class);
	if (aa != null) {
	    for (DMethodContext aaa: aa.value()) {
		String hname = getHelperClassName(clazz, aaa);
		try {
		    // check that the helper class and its DM class have
		    // the same code source.
		    Class<?>helperClass = cl.loadClass(hname);
		    checkCodeSource(clazz, helperClass);
		    // forces initialization & loads the helper class
		    // using the same class loader as that used for clazz.
		    Class.forName(hname, true, cl);
		} catch (Exception e) {
		    System.err.println(e.getMessage());
		    String msg = errorMsg("missingHelper", hname);
		    throw new ClassNotFoundException(msg, e);
		}
	    }
	}
    }

    /**
     * Load a class.
     * @param name the class name
     * @param resolveIt true to resolve it; false otherwise
     */
    protected Class<?> loadClass(String name, boolean resolveIt)
	throws ClassNotFoundException
    {
	Class<?> clazz = super.loadClass(name, false);
	doLoadClasses(clazz);
	if (resolveIt) {
	    resolveClass(clazz);
	}
	return clazz;
    }
}

//  LocalWords:  exbundle OpenJDK DMClassLoader JVM runtime dmDiffer
//  LocalWords:  getProtectionDomain setContextClassLoader loadClass
//  LocalWords:  localHelperCheck codeBaseCheck superclass DM clazz
//  LocalWords:  missingHelper
