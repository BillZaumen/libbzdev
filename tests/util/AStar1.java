import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.bzdev.util.*;

public class AStar1 {

    // First is node and rest in list are neighbors
    static int table[][] = {
	{0},
	{1, 2, 3},
	{2, 1, 4, 5},
	{3, 1, 4},
	{4, 2, 3, 5, 6},
	{5, 2, 4, 6, 8},
	{6, 4, 5, 7},
	{7, 6, 8},
	{8, 5, 7}
    };

    /*
    public static Supplier<Integer> getNeighbors(final Integer i) {
	final int ind[] = {0};
	return new Supplier<Integer> () {
	    public Integer get() {
		ind[0]++;
		if (ind[0] >= table[i].length) return null;
		return table[i][ind[0]];
	    }
	};
    }
    */
    
    public static Stream<Integer> getNeighbors(final Integer i) {
 	if (i >= 0 && i < table.length) {
	    if (table[i][0] == i) {
		int current[] = table[i];
		return IntStream.of(current).skip(1).boxed();
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
   }

    public static void main(String argv[]) throws Exception {
	// to make sure getNeighbors works.
	for (int i = 1; i < 9; i++) {
	    Stream<Integer> s = getNeighbors(i);
	    System.out.print(i + ": ");
	    
	    s.forEach((nd) -> {System.out.print(nd + " ");});
	    System.out.println();
	}
	
	AStarSearch<Integer> as = new AStarSearch<Integer>
	    // ((node) -> {return getNeighbors(node);},
	    (AStar1::getNeighbors,
	     (n1, n2) -> {return 1.0;},
	     null);

	List<Integer> result = as.search(1, 8);
	if (result == null) {
	    System.out.println("<no path found>");
	} else {
	    System.out.println("path:");
	    for (Integer ival: result) {
		System.out.println(ival);
	    }
	}
    }
}
