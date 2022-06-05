package org.bzdev.lang;
import java.security.*;

/**
 * Permission to allow modifying the stack-trace mode for a
 * Simulation (or one of its subclasses) or a Model3D. Used in
 * their methods named setStackTraceMode.
 * @deprecated The security manager is being removed from Java
 */
@Deprecated
public final class StackTraceModePermission extends BasicPermission {
    /**
     * Constructor.
     * For classes defined in the BZDev class library, the name must
     * be either org.bzdev.devqsim.Simulation or org.bzdev.p3d.Model3D
     * @param name the fully-qualified name of the class implementing
     *        a setStackTraceMode method
     */
    public StackTraceModePermission(String name) {
	super(name);
    }

    /**
     * Constructor with actions.
     * @param name the fully-qualified name of the class implementing
     *        a setStackTraceMode method
     * @param actions a parameter that is ignored in this case although
     *        the constructor is needed
     */
    public StackTraceModePermission(String name, String actions) {
	super(name, actions);
    }
}

//  LocalWords:  subclasses setStackTraceMode bzdev BZDev
