package org.bzdev.lang;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Permission;
import java.io.Flushable;
//@exbundle org.bzdev.lang.lpack.Lang


/**
 * Null security manager for tracing security-manager methods.
 * This security manager implements a "null" security policy and is
 * equivalent to setting the security manager to null, but can be
 * configured using the method
 * {@link #setOutput(Appendable) setOutput}
 * to trace the calls made to the security manager's public methods.
 * <P>
 * Deprecated because the SecurityManager class is being removed from
 * Java.
 */
@Deprecated
public class  NullSecurityManager extends SecurityManager {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }

    Appendable out;
    int depth = 0;
    boolean print = true;

    /**
     * Set the output used to trace calls to security-manager public methods.
     * The argument is an Appendable, an interface inherited by both
     * {@link java.io.PrintStream PrintStream} and
     * {@link java.io.PrintWriter PrintWriter}, among other classes.
     * @param out the output objected used to log tracing messages; null to
     *        stop tracing
     */
    public void setOutput(Appendable out) {
	print = (out != null);;
	if (out == null || this.out != out) {
	    if (this.out != null) {
		try {
		    if (this.out instanceof Flushable) {
			((Flushable)this.out).flush();
		    }
		} catch (IOException eio) {}
	    }
	}
	this.out = out;
    }

    /**
     * Get the output object.
     * @return the output object; null if none is set
     */
    public Appendable getOutput() {
	return out;
    }

    /**
     * Constructor.
     */
    public NullSecurityManager() {
	super();
    }

    @Override
    public void checkAccept(String host, int port) {
	try {
	    if (print) out.append("checkAccept(\"" + host + "\", " 
				  + port +")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	   throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkAccess(Thread t) {
	try {
	    if (print) out.append("checkAccess(" + t.toString() + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkAccess(ThreadGroup g) {

	try {
	    if (print) out.append("checkAccess(" + g.toString() + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    /*
    @Deprecated
    @Override
    public void checkAwtEventQueueAccess() {
	try {
	    if (print) out.append("checkAWTEventQueueAccess()");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }
    */

    @Override
    public void checkConnect(String host, int port) {
	try {
	    if (print) out.append("checkConnect(\"" + host + "\", "
				  + port +")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
	try {
	    if (print) out.append("checkConnect(\"" + host + "\", " + port + ","
		       + context.toString() +")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkCreateClassLoader() {
	try {
	    if (print) out.append("checkCreateClassLoader()\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkDelete(String file) {
	try {
	    if (print) out.append("checkDelete(\"" + file + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkExec(String cmd) {
	try {
	    if (print) out.append("checkExec(\"" + cmd + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkExit(int status) {
	try {
	    if (print) out.append("checkExit(" + status + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkLink(String lib) {
	try {
	    if (print) out.append("checkLink(\"" + lib + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkListen(int port) {
	try {
	    if (print) out.append("checkListen(" + port + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    /*
    @Deprecated
    @Override
    public void checkMemberAccess(Class<?>clazz, int which) {
	try {
	    if (print) out.append("check(" + clazz + ", " + which + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }
    */

    @Override
    public void checkMulticast(InetAddress maddr) {
	try {
	    if (print) out.append("checkMulticast(" + maddr.toString() + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    @Deprecated public void checkMulticast(InetAddress maddr, byte ttl) {
	try {
	    if (print) out.append("check("
				  + maddr.toString() + ", " + (int)ttl + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPackageAccess(String pkg) {
	try {
	    if (print) out.append("checkPackageAccess(\"" + pkg +  "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPackageDefinition(String pkg) {
	try {
	    if (print) out.append("checkPackageDefinition(\"" + pkg +"\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPermission(Permission perm) {
	try {
	    if (print) out.append("checkPermission(" + perm + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
	try {
	    if (print) out.append("checkPermision(" + perm
				  + ", " + context +")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPrintJobAccess() {
	try {
	    if (print) out.append("checkPrintJobAccess()\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPropertiesAccess() {
	try {
	    if (print) out.append("checkPropertiesAccess()\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkPropertyAccess(String key) {
	try {
	    if (print) out.append("checkPropertyAccess(\"" + key + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkRead(FileDescriptor fd) {
	try {
	    if (print) out.append("checkRead(" + fd + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkRead(String file) {
	try {
	    if (print) out.append("checkRead(\"" + file + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkRead(String file, Object context) {
	try {
	    if (print) out.append("checkRead(\"" + file
				  + "\", " + context + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkSecurityAccess(String target) {
	try {
	    if (print) out.append("checkSecurityAccess(\"" + target + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkSetFactory() {
	try {
	    if (print) out.append("checkSetFactory()\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    /*
    @Deprecated
    @Override
    public void checkSystemClipboardAccess() {
	try {
	    if (print) out.append("checkSystemClipboardAccess()\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Deprecated
    @Override
    public boolean checkTopLevelWindow(Object window) {
	try {
	    if (print) out.append("checkTopLevelWindow(" + window + ")\n");
	    return true;
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }
    */

    @Override
    public void checkWrite(FileDescriptor fd) {
	try {
	    if (print) out.append("checkWrite(" + fd + ")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }

    @Override
    public void checkWrite(String file) {
	try {
	    if (print) out.append("checkWrite(\"" + file + "\")\n");
	} catch(IOException eio) {
	    String msg = errorMsg("debugOutputFailed");
	    throw new RuntimeException(msg, eio);
	}
    }
}

//  LocalWords:  exbundle setOutput Appendable PrintStream eio msg
//  LocalWords:  PrintWriter checkAccept debugOutputFailed errorMsg
//  LocalWords:  checkAccess checkAwtEventQueueAccess IOException
//  LocalWords:  checkAWTEventQueueAccess RuntimeException checkExec
//  LocalWords:  checkConnect checkCreateClassLoader checkDelete
//  LocalWords:  checkExit checkLink checkListen checkMemberAccess
//  LocalWords:  clazz checkMulticast checkPackageAccess checkRead
//  LocalWords:  checkPackageDefinition checkPermission boolean
//  LocalWords:  checkPermision checkPrintJobAccess checkSetFactory
//  LocalWords:  checkPropertiesAccess checkPropertyAccess checkWrite
//  LocalWords:  checkSecurityAccess checkSystemClipboardAccess
//  LocalWords:  checkTopLevelWindow
