package org.bzdev.lang;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Flushable;
import java.net.InetAddress;
import java.security.Permission;

/**
 * Security manager for tracing calls to java.lang.SecurityManager.
 * This security manager adds the ability to trace the
 * calls to its superclass.
 * <P>
 * Deprecated becasue the SecurityManager class is being removed
 * from Java.
 */
@Deprecated
public  class TracingSecurityManager extends SecurityManager {

    Appendable out = null;
    int depth = 0;
    boolean print = false;

    
    /**
     * Set the output used to trace calls to security-manager public methods.
     * The argument is an Appendable, an interface inherited by both
     * {@link java.io.PrintStream PrintStream} and
     * {@link java.io.PrintWriter PrintWriter}, among other classes.
     * @param out the output objected used to log tracing messages; null to
     *        stop tracing
     */
    public void setOutput(Appendable out) {
	print = false;
	if (out == null || this.out != out) {
	    if (this.out != null) {
		try {
		    if (called) this.out.append('\n');
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

    private int nestingLimit = Integer.MAX_VALUE;

    /**
     * Set the nesting limit.
     * The nesting limit restricts the number of recursive calls
     * to security-manager methods that will be traced.  A value of
     * 1 restricts the output to the top-level calls; a value of 2
     * restricts the output to the calls made by methods called at
     * the top level; etc.  A value of 2 is useful if you want to
     * see the top level and the first call to checkPermission.
     * <P>
     * Note: a nesting limit equal to Integer.MAX_VALUE is equivalent
     * to passing a value of 0.
     * @param limit the nesting limit; 0 or negative if there is none
     */
    public void setNestingLimit(int limit) {
	if (limit <= 0) nestingLimit = Integer.MAX_VALUE;
	else nestingLimit = limit;
    }

    /**
     * Get the nesting limit.
     * @return the nesting limit, with 0 denoting no limit
     */
    public int getNestingLimit() {
	return ((nestingLimit == Integer.MAX_VALUE)? 0: nestingLimit);
    }


    /**
     * Constructor.
     */
    public TracingSecurityManager() {
	super();
    }

    boolean called = false;
    private void printCall(String call) {
	if (!print && depth == 0 && out != null) {
	    // activate printing for new messages.
	    called = false;
	    print = true;
	}
	depth++;
	if (depth > nestingLimit) return;
	if (print) {
	    try {
		if (called) out.append('\n');
		for (int i = 1; i < depth; i++) out.append("  ");
		out.append(call);
		called = true;
	    } catch (IOException e) {}
	} 
    }

    private void printResponse(String response) {
	depth--;
	if (depth >= nestingLimit) return;
	if (print) {
	    try {
		if (called) {
		    out.append(" ... ");
		    out.append(response);
		    out.append('\n');
		    if (out instanceof Flushable) ((Flushable)out).flush();
		} else {
		    for (int i = 0; i < depth; i++) out.append("  ");
		    out.append("... ");
		    out.append(response);
		    out.append('\n');
		}
	    } catch(IOException e) {
	    } finally {
		called = false;
	    }
	}
    }

    @Override
    public void checkAccept(String host, int port) {
	try {
	    printCall("checkAccept(\"" + host + "\", " + port +")");
	    super.checkAccept(host, port);
	    printResponse("ok");
	} catch (RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkAccess(Thread t) {
	try {
	    printCall("checkAccess("
		      + ((t == null)? "null": t.toString())
		      + ")");
	    super.checkAccess(t);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkAccess(ThreadGroup g) {
	try {
	    printCall("checkAccess(" 
		      + ((g == null)? "null": g.toString())
		      + ")");
	    super.checkAccess(g);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    /*
    @Deprecated
    @Override
    public void checkAwtEventQueueAccess() {
	try {
	    printCall("checkAWTEventQueueAccess()");
	    super.checkAwtEventQueueAccess();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }
    */

    @Override
    public void checkConnect(String host, int port) {
	try {
	    printCall("checkConnect(\"" + host + "\", " + port +")");
	    super.checkConnect(host, port);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
	try {
	    printCall("checkConnect(\"" + host 
		      + "\", " + port + ", " + context.toString() +")");
	    super.checkConnect(host, port, context);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkCreateClassLoader() {
	try {
	    printCall("checkCreateClassLoader()\n");
	    super.checkCreateClassLoader();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkDelete(String file) {
	try {
	    printCall("checkDelete(\"" + file + "\")\n");
	    super.checkDelete(file);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkExec(String cmd) {
	try {
	    printCall("checkExec(\"" + cmd + ")\n");
	    super.checkExec(cmd);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkExit(int status) {
	try {
	    printCall("checkExit(" + status + ")");
	    super.checkExit(status);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkLink(String lib) {
	try {
	    printCall("checkLink(\"" + lib + "\")");
	    super.checkLink(lib);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkListen(int port) {
	try {
	    printCall("checkListen(" + port + ")");
	    super.checkListen(port);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }


    /*
      // Skip this one as the default depends on the stack depth.
      public void checkMemberAccess(Class<?>clazz, int which) {
	  try {
	      printCall("checkMemberAccess(" + clazz 
			+ ", " + which + ")");
	      super.checkMemberAccess(clazz, which);
	      printResponse("ok");
	  } catch(RuntimeException e) {
	      printResponse("failed");
	      throw (e);
	  }
      }
    */

    @Override
    public void checkMulticast(InetAddress maddr) {
	try {
	    printCall("checkMulticast(" + maddr.toString() 
		      + ")");
	    super.checkMulticast(maddr);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    @Deprecated public void checkMulticast(InetAddress maddr, byte ttl) {
	try {
	    printCall("checkMulticast("
		      + maddr.toString()
		      + ", " + (int)ttl + ")");
	    super.checkMulticast(maddr, ttl);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }


    @Override
    public void checkPackageAccess(String pkg) {
	try {
	    printCall("checkPackageAccess(\"" + pkg +  "\")");
	    super.checkPackageAccess(pkg);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkPackageDefinition(String pkg) {
	try {
	    printCall("checkPackageDefinition(\"" + pkg +"\")");
	    super.checkPackageDefinition(pkg);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkPermission(Permission perm) {
	try {
	    printCall("checkPermission(" + perm + ")");
	    super.checkPermission(perm);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
	try {
	    printCall("checkPermission(" + perm
		      + ", " + context +")");
	    super.checkPermission(perm, context);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }


    @Override
    public void checkPrintJobAccess() {
	try {
	    printCall("checkPrintJobAccess()");
	    super.checkPrintJobAccess();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkPropertiesAccess() {
	try {
	    printCall("checkPropertiesAccess()");
	    super.checkPropertiesAccess();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkPropertyAccess(String key) {
	try {
	    printCall("checkPropertyAccess(\"" + key + "\")");
	    super.checkPropertyAccess(key);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkRead(FileDescriptor fd) {
	try {
	    printCall("checkRead(" + fd + ")");
	    super.checkRead(fd);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkRead(String file) {
	try {
	    printCall("checkRead(\"" + file + "\")");
	    super.checkRead(file);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkRead(String file, Object context) {
	try {
	    printCall("checkRead(\"" + file
		      + "\", " + context + ")");
	    super.checkRead(file, context);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkSecurityAccess(String target) {
	try {
	    printCall("checkSecurityAccess(\""
		      + target + "\")");
	    super.checkSecurityAccess(target);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkSetFactory() {
	try {
	    printCall("checkSetFactory()");
	    super.checkSetFactory();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    /*
    @Deprecated
    @Override
    public void checkSystemClipboardAccess() {
	try {
	    printCall("checkSystemClipboardAccess()");
	    super.checkSystemClipboardAccess();
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Deprecated
    @Override
    public boolean checkTopLevelWindow(Object window) {
	try {
	    printCall("checkTopLevelWindow(" + window + ")");
	    boolean result = super.checkTopLevelWindow(window);
	    printResponse(result? "true": "false");
	    return result;
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }
    */


    @Override
    public void checkWrite(FileDescriptor fd) {
	try {
	    printCall("checkWrite(" + fd + ")");
	    super.checkWrite(fd);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }

    @Override
    public void checkWrite(String file) {
	try {
	    printCall("checkWrite(\"" + file + "\")");
	    super.checkWrite(file);
	    printResponse("ok");
	} catch(RuntimeException e) {
	    printResponse("failed");
	    throw (e);
	}
    }
}

//  LocalWords:  superclass Appendable PrintStream PrintWriter ok
//  LocalWords:  checkPermission checkAccept checkAccess printCall
//  LocalWords:  checkAwtEventQueueAccess checkAWTEventQueueAccess
//  LocalWords:  printResponse RuntimeException checkConnect clazz
//  LocalWords:  checkCreateClassLoader checkDelete checkExec boolean
//  LocalWords:  checkExit checkLink checkListen checkMemberAccess
//  LocalWords:  checkMulticast checkPackageAccess checkRead
//  LocalWords:  checkPackageDefinition checkPrintJobAccess
//  LocalWords:  checkPropertiesAccess checkPropertyAccess checkWrite
//  LocalWords:  checkSecurityAccess checkSetFactory
//  LocalWords:  checkSystemClipboardAccess checkTopLevelWindow
