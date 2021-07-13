import org.bzdev.util.*;
import org.bzdev.math.Functions;
import org.bzdev.math.PoissonTable;

public class PTableTest {
    public static void main(String argv[]) throws Exception {
	for (int i = 1; i < 1024; i++) {
	    double lambda = i;
	    int n = PoissonTable.estimateN(lambda);
	    if (n > 0) {
		System.out.println("lambda = " + lambda + ", n = " + n);
	    }
	}
	
	PoissonTable ptbl = PoissonTable.createTable(20.0);
	if (PoissonTable.getTable(20.0) == null) {
	    throw new Exception("cannot find created table");
	}
	System.gc();
	if (PoissonTable.getTable(20.0) == null) {
	    throw new Exception("cannot find created table after gc");
	}
	ptbl = null;
	System.gc();
	if (PoissonTable.getTable(20.0) != null) {
	    throw new Exception("found created table after gc when no ref");
	}

	PoissonTable.add(20.0);
	System.gc();
	if (PoissonTable.getTable(20.0) == null) {
	    throw new Exception("cannot find created table after gc");
	}
	PoissonTable.remove(20.0);
	System.gc();
	if (PoissonTable.getTable(20.0) != null) {
	    throw new Exception("found created table after gc when no ref");
	}
    }
}
