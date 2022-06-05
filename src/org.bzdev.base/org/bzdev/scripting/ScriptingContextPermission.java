package org.bzdev.scripting;
import java.security.*;

//@exbundle org.bzdev.scripting.lpack.Scripting

/**
 * Permission to allow the construction of a ScriptingContext or to call
 * the ScriptingContext methods setReader or setWriter,
 * setErrorWriter, or createBindingSwapper to be called.
 * This permission allows specific subclasses of ScriptingContext to
 * be constructed even if a security manager has been installed. It is
 * intended for users of the BZDev library. The actions string should
 * be either empty or comma-separated tokens whose values can be
 * <ul>
 *   <li> "trusted", which implies the ability to construct a trusted
 *        scripting context.  When trusted, scripts will be evaluated
 *        with no additional access control; otherwise they are evaluated
 *        with no permissions.
 *   <li> "swapbindings", which implies that the ScriptingContext method
 *        createBindingSwapper to be used.
 *   <li> "privileged", which implies that the additional privileges are
 *        available for the ScriptingContext methods
 *        evalScriptPrivileged,
 *        doScriptPrivileged, doScriptPrivilegedReturns, and
 *        invokePrivateFunction when its second argument is
 *        ScriptingContext.PFMode.PRIVILEGED. These additional privileges
 *        are the ones granted to the target class' code base. Without
 *        this permission, no privileges are granted to these methods.
 * </ul>
 * An actions string (other than an empty string) must not be used if
 * the permission is for the targets java.io.Reader (to allow the
 * ScriptingContext setReader method to be used) or
 * java.io.Writer (to allow the ScriptingContext setErrorWriter or
 * setWriter methods to be used).
 * <P>
 * In order to create a subclass of ScriptingContext, both the
 * BZDev library code base and the code base of the scripting context
 * subclass must have a ScriptingContextPermission for the subclass.
 * @deprecated The security manager is being removed from Java
 */
@Deprecated
public final class ScriptingContextPermission extends BasicPermission {

    static String errorMsg(String key, Object... args) {
	return Scripting.errorMsg(key, args);
    }


    /**
     * Constructor.
     * @param name the fully qualified class name of the subclass of
     *        ScriptingContext that will be constructed, or the
     *        class names java.io.Reader or java.io.Writer
     */
    public ScriptingContextPermission(String name) {
	super(name);
    }

    private String actions = "";

    /**
     * Constructor with actions.
     * When the actions string has the value "trusted", that indicates
     * that a subclass of ScriptingContext may be instantiated with
     * a constructor that sets a 'trusted' argument to true.
     * @param name the fully qualified class name of the subclass of
     *        ScriptingContext that will be constructed, or the
     *        class names java.io.Reader or java.io.Writer
     * @param actions either an empty string, mandatory when name is
     *        java.io.Reader or java.io.Writer, or a string made up
     *        of any of the comma-separated tokens "trusted",
     *        "swapbindings", or "privileged"
     * @exception IllegalArgumentException the actions argument had an
     *            illegal value
     */
    public ScriptingContextPermission(String name, String actions) {
	super(name, actions);
	if (actions.length() != 0) {
	    boolean swapbindings = false;
	    boolean trusted = false;
	    boolean privileged = false;
	    for (String token: actions.split(",")) {
		token = token.trim();
		if (token.equals("trusted")) {
		    trusted = true;
		} else if (token.equals("swapbindings")) {
		    swapbindings = true;
		} else if (token.equals("privileged")) {
		    privileged = true;
		} else {
		    throw new IllegalArgumentException
			(errorMsg("actionNotRecognized", token));
		}
	    }
	    // Rewrite the actions so they are listed in a predicable
	    // order.
	    if (trusted) {
		if (swapbindings) {
		    this.actions = (privileged?
				    "trusted,swapbindings,privileged":
				    "trusted,swapbindings");
		} else {
		    this.actions = (privileged? "trusted,privileged":
				    "trusted");
		}
	    } else if (swapbindings) {
		this.actions = (privileged? "swapbindings,privileged":
				"swapbindings");
	    } else if (privileged) {
		this.actions = "privileged";
	    }
	}
    }

    @Override
    public String getActions() {
	return actions;
    }

    @Override
    public boolean implies(Permission p) {
	boolean result = super.implies(p);
	if (result) {
	    String pactions = p.getActions();
	    if (pactions.equals("trusted")) {
		result = (actions.equals("trusted")
			  || actions.equals("trusted,swapbindings")
			  || actions.equals("trusted,privileged")
			  || actions.equals("trusted,swapbindings,privileged"));
	    } else if (pactions.equals("trusted,swapbindings")) {
		result = (actions.equals("trusted,swapbindings")
			  || actions.equals("trusted,swapbindings,privileged"));
	    } else if(pactions.equals("trusted,privileged")) {
		result = (actions.equals("trusted,privileged")
			  || actions.equals("trusted,swapbindings,privileged"));
	    } else if (pactions.equals("trusted,swapbindings,privileged")) {
		result = actions.equals("trusted,swapbindings,privileged");
	    } else if (pactions.equals("swapbindings")) {
		result = (actions.equals("swapbindings")
			  || actions.equals("swapbindings,privileged")
			  || actions.equals("trusted,swapbindings")
			  || actions.equals("trusted,swapbindings,privileged"));
	    } else if (pactions.equals("swapbindings,privileged")) {
		result = (actions.equals("swapbindings,privileged")
			  || actions.equals("trusted,swapbindings,privileged"));
	    } else if (pactions.equals("privileged")) {
		result = (actions.equals("privileged")
			  || actions.equals("trusted,privileged")
			  || actions.equals("swapbindings,privileged")
			  || actions.equals("trusted,swapbindings,privileged"));
	    }
	}
	return result;
    }
}

//  LocalWords:  exbundle ScriptingContext setReader setWriter BZDev
//  LocalWords:  setErrorWriter createBindingSwapper subclasses ul li
//  LocalWords:  swapbindings evalScriptPrivileged doScriptPrivileged
//  LocalWords:  doScriptPrivilegedReturns invokePrivateFunction
//  LocalWords:  ScriptingContextPermission IllegalArgumentException
//  LocalWords:  actionNotRecognized
