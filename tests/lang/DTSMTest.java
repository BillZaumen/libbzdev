import org.bzdev.lang.*;
import java.io.*;

public class DTSMTest {
    public static void main(String argv[]) throws Exception {

	final DesignatedThreadsSM sm =
	    new DesignatedThreadsSM(new SecurityManager());

	System.out.println("Before security manager was installed ...");
	sm.call(new ExceptionedCallable() {
		public void call() {
		    System.out.println("called [1]");
		}
	    });

	System.out.println(sm.call(new ExceptionedCallableReturns<String>() {
		public String call() {
		    return "called [2]";
		}
	    }));

	sm.call(new ExceptionedCallableArgs<String>() {
		public void call(String... args) throws Exception {
		    for (String s: args) {
			System.out.println(s);
		    }
		    sm.call(new ExceptionedCallable() {
			    public void call() throws Exception {
				System.out.println("hello-goodbye[3]");
			    }
			});
		}
	    }, "hello[3]", "goodbye[3]");


	System.out.print(sm.call
			 (new ExceptionedCallableArgsReturns<String,String>() {
				 public String call(String... args)
				     throws Exception
				 {
				     String result = "";
				     for (Object obj: args) {
					 result = result + obj + "\n";
				     }
				     return result;
				 }
			     }, "hello[4]", "goodbye[4]"));

	Runnable runnable = new Runnable() {
		public void run() {
		    try {
			FileInputStream is =
			    new FileInputStream("DTSMTest.java");
			is.close();
			System.out.println("thread successfully opened "
					   + "and closed a file");
		    } catch (Exception e) {
			System.out.println("failed to open the file");
			System.exit(1);
		    }
		    
		}
	    };

	Thread thread = sm.fullPrivilegedThread(runnable);

	Thread thread1 = sm.fullPrivilegedThread(new ThreadGroup("foo"),
						 runnable);
	Thread thread2 = sm.fullPrivilegedThread(runnable, "thread2");

	Thread thread3 = sm.fullPrivilegedThread(new ThreadGroup("bar"),
						 runnable, "thread2");

	Thread thread4 = sm.fullPrivilegedThread(new ThreadGroup("bar"),
						 runnable, "thread3",
						 20000L);


	System.setSecurityManager(sm);
	System.out.println("After security manager was installed ...");

	ExceptionedCallable callable = new ExceptionedCallable() {
		public void call() throws Exception {
			FileInputStream is =
			    new FileInputStream("DTSMTest.java");
			is.close();
			System.out.println("successfully opened and closed "
					   + "a file");
		}
	    };
	int errcount = 0;

	try {
	    callable.call();
	    System.out.println("exception expected but was missing");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.println("callable.call() threw "
			       + "an exception as expected");
	}

	try {
	    sm.call(callable);
	} catch (Exception e) {
	    System.out.println("sm.call(callable) failed\n");
	    errcount++;
	}

	System.out.println("starting threads we previously created ...");
	thread.start();
	thread.join();
	System.out.println("... thread1");
	thread1.start();
	thread1.join();
	System.out.println("... thread2");
	thread2.start();
	thread2.join();
	System.out.println("... thread3");
	thread3.start();
	thread3.join();
	System.out.println("... thread4");
	thread4.start();
	thread4.join();
	System.out.println("... threads ran to completion");
	try {
	sm.call(new ExceptionedCallable() {
		public void call() throws Exception {
		    System.out.println("called [1]");
		}
	    });
	} catch (Exception e) {
	    System.out.println("sm.call failed\n");
	    errcount++;
	}

	try {
	    System.out.println(sm.call(new
				       ExceptionedCallableReturns<String>()
				       {
					   public String call() {
					       return "called [2]";
					   }
		}));
	} catch (Exception e) {
	    System.out.println("sm.call failed\n");
	    errcount++;
	}

	try {
	    sm.call(new ExceptionedCallableArgs<String>() {
		    public void call(String... args) throws Exception {
			for (String s: args) {
			    System.out.println(s);
			}
			sm.call(new ExceptionedCallable() {
				public void call() throws Exception {
				    System.out.println("hello-goodbye[3]");
				}
			    });
		    }
		}, "hello[3]", "goodbye[3]");
	} catch (Exception e) {
	    System.out.println("sm.call failed\n");
	    errcount++;
	}


	try {
	    System.out.print(sm.call
			     (new ExceptionedCallableArgsReturns<String,String>
			      () {
				     public String call(String... args)
					 throws Exception
				     {
					 String result = "";
					 for (Object obj: args) {
					     result = result + obj + "\n";
					 }
					 return result;
				     }
				 }, "hello[4]", "goodbye[4]"));
	} catch (Exception e) {
	    System.out.println("sm.call failed\n");
	    errcount++;
	}
	if (errcount > 0) {
	    System.out.println("errcount = " + errcount);
	    System.exit(1);
	}
	System.exit(0);
    }
}
