package org.bzdev.scripting;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.security.Permission;

import java.util.*;

/**
 * Security manager for scripting contexts.
 * A scripting context can be in sandboxed mode or normal mode.
 * In normal mode, no security checks are performed.  In sandboxed
 * mode, the security policy provided by
 * {@link java.lang.SecurityManager SecurityManager} is used.  If a
 * ScriptingContext is created by the statement
 * <pre><code>
 *     ScriptingContext sc = new ScriptingContext(true);
 * </code></pre>
 * the scripting context created will never switch modes.  If a
 * scripting context creates a thread, that thread inherits the
 * current mode.
 */
public class ScriptingSecurityManager extends SecurityManager {

    static {
	// Calling this method causes ScriptinContext to be loaded. We
	// need this to paper over a bug in Java-11.0.1 where the
	// a BootstrapMethodError (bootstrap method initialization exception)
	// is thrown and caught by the JVM, not the application, causing a
	// crash when a ScriptingSecurityManger is installed before a
	// ScriptingContext is created.
	ScriptingContext.register();
    }

    @Override
    public void checkPermission(Permission perm) {
	if (ScriptingContext.inSandbox()) {
	    super.checkPermission(perm);
	}

    }

    @Override
    public void checkPermission(Permission perm, Object context) {
	if (ScriptingContext.inSandbox()) {
	    super.checkPermission(perm, context);
	}
    }

    /*
    @Deprecated
    @Override
    public boolean checkTopLevelWindow(Object window) {
	if (ScriptingContext.inSandbox()) {
	    return super.checkTopLevelWindow(window);
	} else {
	    return true;
	}
    }
    */
}

//  LocalWords:  sandboxed SecurityManager ScriptingContext pre sc
//  LocalWords:  ScriptinContext BootstrapMethodError JVM boolean
//  LocalWords:  ScriptingSecurityManger checkTopLevelWindow
//  LocalWords:  inSandbox
