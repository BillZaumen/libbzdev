import org.bzdev.lang.*;

public class CallTest {
    public static void main(String argv[]) {
	try {
	    Callable c = new Callable() {
		    public void call() {
			System.out.println("hello");
		    }
		};
	    c.call();
	    c = () -> {System.out.println("hello");};
	    c.call();
	    CallableArgs<String> ca = new CallableArgs<String>() {
		    public void call(String... args) {
			System.out.println("hello"

					   + ((args.length == 0)? "":
					      args[0]));
		    }
		};
	    ca.call(", goodbye");
	    CallableArgsReturns<Integer,Integer>
		cr = new CallableArgsReturns<Integer,Integer>() {
		    public Integer call(Integer... args) {
			int j = (args.length == 0)? 0: args[0];
			return new Integer(10 + j);
		    }
		};
	    System.out.println(cr.call());
	    System.out.println(cr.call(Integer.valueOf(20)));
	    cr = (args) -> {int j = (args.length == 0)? 0: args[0];
			    return Integer.valueOf(10+j);};
	    System.out.println(cr.call());
	    System.out.println(cr.call(Integer.valueOf(20)));
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	try {
	    ExceptionedCallable ec = new ExceptionedCallable() {
		    public void call() throws Exception {
			throw new Exception();
		    }
		};
	    ec.call();
	    System.out.println("no exception 1");
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    ExceptionedCallable ec = () -> {
		throw new Exception();
	    };
	    ec.call();
	    System.out.println("no exception 2");
	    System.exit(1);
	} catch (Exception e) {
	}


	try {
	    ExceptionedCallableReturns<Integer>
		ec = new ExceptionedCallableReturns<Integer>() {
		    public Integer call() throws Exception {
			throw new Exception();
		    }
		};
	    int j = ec.call();
	    System.out.println("no exception 3");
	    System.exit(1);
	} catch (Exception e) {
	}

	try {
	    ExceptionedCallableReturns<Integer> ec = () ->
		{
		    throw new Exception();
		};
	    int j = ec.call();
	    System.out.println("no exception 4, j = " + j);
	    System.exit(1);
	} catch (Exception e) {
	}
	try {
	    ExceptionedCallableReturns<Integer> ec = () ->
		{
		    return 40;
		};
	    int j = ec.call();
	    System.out.println("j = " + j);
	} catch (Exception e) {
	    System.out.println("exception not expected");
	}

	System.exit(0);
    }
}
