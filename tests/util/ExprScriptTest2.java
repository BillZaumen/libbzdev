import java.io.File;
import java.nio.file.Files;
import org.bzdev.lang.MathOps;
import org.bzdev.util.*;

public class ExprScriptTest2 {


    public static void main(String[] argv) throws Exception {

	final ExpressionParser parser = new ExpressionParser(MathOps.class);

	parser.setScriptingMode();
	parser.setImportMode();
	parser.setScriptImportMode();
	parser.setGlobalMode();
	parser.addClasses(Math.class);
	parser.set("temp1", 0);
	final String sa = Files.readString(new File("test2a.esp").toPath());
	parser.parse(sa);
	final String sb = Files.readString(new File("test2b.esp").toPath());
	Object v = parser.parse(sb);

	final int M = 10000;

	Runnable r = () -> {
		for (int i = 0; i < M; i++) {
		    Object vv = parser.parse(sb);
		    if (!v.equals(vv)) {
			System.out.println("v != vv");
			System.exit(1);
		    }
		}
	};

	int N = 100;

	Thread[] threads = new Thread[N];

	System.out.println("first test...");
	for (int i = 0; i < N; i++) {
	    threads[i] = new Thread(r);
	}
	for (int i = 0; i < N; i++) {
	    threads[i].start();
	}
	for (int i = 0; i < N; i++) {
	    threads[i].join();
	}

	final String s2 = "incr()";
	r = () -> {
	    for (int i = 0; i < M; i++) {
		Object vv = parser.parse(s2);
		int value = (Integer) vv;
		if (value != 1) {
		    System.out.println("incr() failed: value = " + value);
		    System.exit(1);
		}
	    }
	};

	threads = new Thread[N];

	System.out.println("second test...");
	for (int i = 0; i < N; i++) {
	    threads[i] = new Thread(r);
	}
	for (int i = 0; i < N; i++) {
	    threads[i].start();
	}
	for (int i = 0; i < N; i++) {
	    threads[i].join();
	}

    }
}
