package org.bzdev.lang;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.security.*;

import java.util.*;

//@exbundle org.bzdev.lang.lpack.Lang

/**
 * Security manager that allows a thread to temporarily run without
 * most security checks, subject to a permission check.  If the
 * code running in a thread has the permission
 * {@link org.bzdev.lang.DesignatedThreadsPermission}, it can use
 * a callable to run code with most permission checks ignored. The
 * permissions that will always be checked are:
 * <UL>
 *   <IT> java.lang.RuntimePermission when the permission's name
 *         is "setSecurityManager".
 *   <IT> java.security.SecurityPermission for all names.
 * </UL>
 * All permissions that are checked are checked by the security manager
 * used as the first argument in this class's constructor. That
 * security manager will typically be an instance of
 * {@link java.lang.SecurityManager}.
 * <P>
 * Use cases include a program whose code can be trusted but that runs
 * untrusted code (e.g., scripts) in separate threads, with
 * particular threads allowed to run with no security restrictions. For
 * example:
 * <blockquote><code><pre>
 *     final DesignatedThreadsSM dtsm =
 *         new DesignatedThreadsSM(new SecurityManager());
 *     Thread fpt = dtsm.fullPrivilegedThread(new Runnable() {
 *             public void run() {
 *                 ...
 *             }
 *         });
 *    System.setSecurityManager(dtsm);
 *    fpt.start();
 *    // untrusted code runs here.
 *;   ...
 * </pre></code></blockquote>
 * <P>
 * Deprecated because the SecurityManager class is being removed
 * from Java.
 */
@Deprecated
public class DesignatedThreadsSM extends SecurityManager {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }

    // Maintains a counter in case a 'call' method is called
    // recursively.  We don't want to remove the current thread
    // until the last nested call has ended.
    HashMap<Thread,Integer> designatedThreads = new HashMap<>();
    SecurityManager sm;

    /**
     * Remove the current thread from the table of designated
     * threads.
     * For this method to succeed when a security manager
     * has been installed, the current thread must have the permission
     * org.bzdev.lang.DesignatedThreadsPermission.
     * @exception SecurityException permission to add a thread was
     *            not granted
     */
    private void addCurrentThread() throws SecurityException {
	SecurityManager sm2 = System.getSecurityManager();
	if (sm2 != null) {
	    sm2.checkPermission(new DesignatedThreadsPermission());
	}
	Thread t = Thread.currentThread();
	synchronized(this) {
	    Integer depth = designatedThreads.get(t);
	    if (depth == null) {
		depth = 0;
	    } else {
		depth = depth + 1;
	    }
	    designatedThreads.put(t, depth);
	}
    }

    /**
     * Remove the current thread from the table of designated
     * threads.
     */
    private void removeCurrentThread() {
	Thread t = Thread.currentThread();
	synchronized(this) {
	    Integer depth = designatedThreads.get(t);
	    if (depth > 0) {
		depth = depth - 1;
		designatedThreads.put(t, depth);
	    } else {
		designatedThreads.remove(t);
	    }
	}
    }

    /**
     * Execute code with no security restrictions, returning a value.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission      .
     * @param callable the code to execute
     * @return the value returned by calling the callable
     * @exception SecurityException permission to call this method was
     *            not granted
     */
    public <T> T call(CallableReturns<T> callable)
	throws SecurityException
    {
	addCurrentThread();
	try {
	    return callable.call();
	} finally {
	    removeCurrentThread();
	}
    }

    /**
     * Execute code with arguments and with no security restrictions,
     * returning a value.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @param callable the code to execute
     * @param args the arguments for the callable
     * @return the value returned by calling the callable
     * @exception SecurityException permission to call this method was
     *            not granted
     * @exception an exception was thrown
     */
    @SafeVarargs
    public final <T,TA> T call(CallableArgsReturns<T,TA> callable,
			       TA... args)
	throws SecurityException
    {
	addCurrentThread();
	try {
	    return callable.call(args);
	} finally {
	    removeCurrentThread();
	}
    }


    /**
     * Execute code with no security restrictions, without returning a value.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @exception SecurityException permission to call this method was
     *            not granted
     */
    public void call(Callable callable) throws SecurityException {
	addCurrentThread();
	try {
	    callable.call();
	} finally {
	    removeCurrentThread();
	}
    }

    /**
     * Execute code with arguments and with no security restrictions,
     * without returning a value.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @param callable the code to execute
     * @param args the arguments for the callable
     * @exception SecurityException permission to call this method was
     *            not granted
     */
    @SafeVarargs
    public final <T> void call(CallableArgs<T> callable, T... args)
    {
	addCurrentThread();
	try {
	    callable.call(args);
	} finally {
	    removeCurrentThread();
	}
    }


    /**
     * Execute code with no security restrictions, returning a value or
     * throwing an exception.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission      .
     * @param callable the code to execute
     * @return the value returned by calling the callable
     * @exception SecurityException permission to call this method was
     *            not granted
     * @exception an exception was thrown
     */
    public <T> T call(ExceptionedCallableReturns<T> callable)
	throws SecurityException, Exception
    {
	addCurrentThread();
	try {
	    return callable.call();
	} finally {
	    removeCurrentThread();
	}
    }

    /**
     * Execute code with arguments and with no security restrictions,
     * returning a value or throwing an exception.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @param callable the code to execute
     * @param args the arguments for the callable
     * @return the value returned by calling the callable
     * @exception SecurityException permission to call this method was
     *            not granted
     * @exception an exception was thrown
     */
    @SafeVarargs
    public final <T,TA> T call(ExceptionedCallableArgsReturns<T,TA> callable,
			       TA... args)
	throws SecurityException, Exception
    {
	addCurrentThread();
	try {
	    return callable.call(args);
	} finally {
	    removeCurrentThread();
	}
    }

    /**
     * Execute code with no security restrictions, without returning a value
     * but possibly throwing an exception.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @param callable the code to execute
     * @exception SecurityException permission to call this method was
     *            not granted
     * @exception an exception was thrown
     */
    public void call(ExceptionedCallable callable) throws Exception {
	addCurrentThread();
	try {
	    callable.call();
	} finally {
	    removeCurrentThread();
	}
    }

    /**
     * Execute code with arguments and with no security restrictions,
     * without returning a value but possibly throwing an exception.
     * The code base used by the current thread must have the
     * permission org.bzdev.lang.DesignatedThreadsPermission.
     * @param callable the code to execute
     * @exception SecurityException permission to call this method was
     *            not granted
     * @exception an exception was thrown
     */
    @SafeVarargs
    public final <T> void call(ExceptionedCallableArgs<T> callable, T... args)
	throws Exception
    {
	addCurrentThread();
	try {
	    callable.call(args);
	} finally {
	    removeCurrentThread();
	}
    }


    /**
     * Constructor.
     * For all threads except the ones excluded by calling
     * {@link #addCurrentThread()}, security checks will be delegated
     * to the security manager passed as a constructor.
     * @param sm the security manager that will determine the
     *        permissions for threads other than the ones designated
     *        via calls to {@link #addCurrentThread()}
     * @exception NullPointerException the argument was null
     */
    public DesignatedThreadsSM(SecurityManager sm)
	throws NullPointerException
    {
	if (sm == null) {
	    throw new NullPointerException(errorMsg("nullArgument"));
	}
	this.sm = sm;
    }

    synchronized boolean mustTest() {
	return !designatedThreads.containsKey(Thread.currentThread());
    }

    @Override
    public void checkPermission(Permission perm) {
	if (mustTest()) {
	    if (sm != null)sm.checkPermission(perm);
	} else {
	    if (perm instanceof SecurityPermission) {
		sm.checkPermission(perm);
	    } else if (perm instanceof RuntimePermission) {
		if (perm.getName().equals("setSecurityManager")) {
		    sm.checkPermission(perm);
		}
	    }
	}

    }

    @Override
    public void checkPermission(Permission perm, Object context) {
	if (mustTest()) {
	    if(sm != null) sm.checkPermission(perm, context);
	} else {
	    if (perm instanceof SecurityPermission) {
		sm.checkPermission(perm, context);
	    } else if (perm instanceof RuntimePermission) {
		if (perm.getName().equals("setSecurityManager")) {
		    sm.checkPermission(perm, context);
		}
	    }
	}
    }

    /*
    @Deprecated
    @Override
    public boolean checkTopLevelWindow(Object window) {
	if (mustTest()) {
	    if(sm != null) return sm.checkTopLevelWindow(window);
	}
	return true;
    }
    */

    /**
     * Create a thread with the privileges granted to the BZDev
     * class library.
     * This method must be called before a security manager
     * is installed but after it was created. The thread will technically
     * start but the runnable passed as an argument will not execute
     * until the thread's {@link Thread#start()} method is called.
     * Typically, if a security exeception occurs, it will not be thrown
     * by this method but instead by the thread that this method starts.
     * The thread will be in the WAITING state until {@link Thread#start()}
     * is explicitly called, unless a security exception has occurred in
     * which case the thread will have immediately terminated.
     * @param r the Runnable for the thread that will be created
     */
    public Thread fullPrivilegedThread(Runnable r) {
	return fullPrivilegedThread(null, (Runnable)r);
    }

    /**
     * Create a thread with the privileges granted to the BZDev
     * class library, providing a  name.
     * This method must be called before a security manager
     * is installed but after it was created. The thread will technically
     * start but the runnable passed as an argument will not execute
     * until the thread's {@link Thread#start()} method is called.
     * Typically, if a security exeception occurs, it will not be thrown
     * by this method but instead by the thread that this method starts.
     * The thread will be in the WAITING state until {@link Thread#start()}
     * is explicitly called, unless a security exception has occurred in
     * which case the thread will have immediately terminated.
     * @param r the Runnable for the thread that will be created
     * @param name the name to assign to the new thread; null if
     *        there is none
     */
    public Thread fullPrivilegedThread(Runnable r, String name) {
	return fullPrivilegedThread(null, r, name);
    }
    
    /**
     * Create a thread with the privileges granted to the BZDev
     * class library, providing a thread group.
     * This method must be called before a security manager
     * is installed but after it was created. The thread will technically
     * start but the runnable passed as an argument will not execute
     * until the thread's {@link Thread#start()} method is called.
     * Typically, if a security exeception occurs, it will not be thrown
     * by this method but instead by the thread that this method starts.
     * The thread will be in the WAITING state until {@link Thread#start()}
     * is explicitly called, unless a security exception has occurred in
     * which case the thread will have immediately terminated.
     * @param group the thread group; null if there is none
     * @param r the Runnable for the thread that will be created
     */
    public Thread fullPrivilegedThread(ThreadGroup group, Runnable r) {
	Thread thread = new Thread(group, (Runnable)null) {
		volatile boolean mustWait = true;
		Thread thisThread = this;
		public void run() {
		    DesignatedThreadsSM.this.call(new Callable() {
			    public void call() {
				synchronized(thisThread) {
				    while (mustWait) {
					try {
					    thisThread.wait();
					} catch(InterruptedException e) {
					    return;
					}
				    }
				}
				r.run();
			    }
			});
		}
		boolean notStarted = true;
		public void start() {
		    if (notStarted) {
			super.start();
			notStarted = false;
			return;
		    }
		    synchronized(thisThread) {
			mustWait = false;
			thisThread.notifyAll();
		    }
		}
	};
	// OK to call this twice because only the first call
	// actually starts the thread. The second
	// call to start is provided by the caller of this method
	// and allows the runnable passed as an argument to be
	// executed.
	thread.start();
	return thread;
    }

    /**
     * Create a thread with the privileges granted to the BZDev
     * class library, providing a thread group and name.
     * This method must be called before a security manager
     * is installed but after it was created. The thread will technically
     * start but the runnable passed as an argument will not execute
     * until the thread's {@link Thread#start()} method is called.
     * Typically, if a security exeception occurs, it will not be thrown
     * by this method but instead by the thread that this method starts.
     * The thread will be in the WAITING state until {@link Thread#start()}
     * is explicitly called, unless a security exception has occurred in
     * which case the thread will have immediately terminated.
     * @param group the thread group; null if there is none
     * @param r the Runnable for the thread that will be created
     * @param name the name to assign to the new thread; null if
     *        there is none
     */
    public Thread fullPrivilegedThread(ThreadGroup group, Runnable r,
				       String name)
    {
	Thread thread = new Thread(group, null, name) {
		boolean mustWait = true;
		Thread thisThread = this;
		public void run() {
		    DesignatedThreadsSM.this.call(new Callable() {
			    public void call() {
				synchronized(thisThread) {
				    while (mustWait) {
					try {
					    thisThread.wait();
					} catch(InterruptedException e) {
					    return;
					}
				    }
				}
				r.run();
			    }
			});
		}
		boolean notStarted = true;
		public void start() {
		    if (notStarted) {
			super.start();
			notStarted = false;
			return;
		    }
		    synchronized(thisThread) {
			mustWait = false;
			thisThread.notifyAll();
		    }
		}
	};
	// OK to call this twice because only the first call
	// actually starts the thread. The second
	// call to start is provided by the caller of this method
	// and allows the runnable passed as an argument to be
	// executed.
	thread.start();
	return thread;
    }

    /**
     * Create a thread with the privileges granted to the BZDev
     * class library, providing a thread group, name and stacksize.
     * See {@link Thread#Thread(ThreadGroup,Runnable,String,long)} for
     * an explanation of the stackSize argument.
     * This method must be called before a security manager
     * is installed but after it was created. The thread will technically
     * start but the runnable passed as an argument will not execute
     * until the thread's {@link Thread#start()} method is called.
     * Typically, if a security exeception occurs, it will not be thrown
     * by this method but instead by the thread that this method starts.
     * The thread will be in the WAITING state until {@link Thread#start()}
     * is explicitly called, unless a security exception has occurred in
     * which case the thread will have immediately terminated.
     * @param group the thread group; null if there is none
     * @param r the Runnable for the thread that will be created
     * @param name the name to assign to the new thread; null if
     *        there is none
     * @param stackSize the desired stack size for the new thread,
     *        or zero to indicate that this parameter is to be ignored.
     */
    public Thread fullPrivilegedThread(ThreadGroup group, Runnable r,
				       String name, long stackSize)
    {
	Thread thread = new Thread(group, null, name, stackSize) {
		boolean mustWait = true;
		Thread thisThread = this;
		public void run() {
		    DesignatedThreadsSM.this.call(new Callable() {
			    public void call() {
				synchronized(thisThread) {
				    while (mustWait) {
					try {
					    thisThread.wait();
					} catch(InterruptedException e) {
					    return;
					}
				    }
				}
				r.run();
			    }
			});
		}
		boolean notStarted = true;
		public void start() {
		    if (notStarted) {
			super.start();
			notStarted = false;
			return;
		    }
		    synchronized(thisThread) {
			mustWait = false;
			thisThread.notifyAll();
		    }
		}
	};
	// OK to call this twice because only the first call
	// actually starts the thread. The second
	// call to start is provided by the caller of this method
	// and allows the runnable passed as an argument to be
	// executed.
	thread.start();
	return thread;
    }
}

//  LocalWords:  exbundle setSecurityManager untrusted blockquote pre
//  LocalWords:  DesignatedThreadsSM dtsm SecurityManager fpt args sm
//  LocalWords:  fullPrivilegedThread Runnable SecurityException
//  LocalWords:  addCurrentThread NullPointerException nullArgument
//  LocalWords:  BZDev stacksize ThreadGroup stackSize runnable
