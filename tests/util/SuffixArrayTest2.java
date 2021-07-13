import org.bzdev.util.*;

public class SuffixArrayTest2 {
    static public void main(String argv[]) {

	int ifsequence2[] = {7, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 5,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4,
			   1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 8, 0};

	int ifsubsequence2[] = {1, 2, 3, 4};
	SuffixArray.Integer ifsa2 = new SuffixArray.Integer(ifsequence2, 11);
	int[] ifA2 = ifsa2.getArray();
	int if2 = ifsa2.findSubsequence(ifsubsequence2);
	System.out.println("ifsa2 test: arbitrary index = " + if2);

	int if21 = ifsa2.findSubsequence(ifsubsequence2, false);
	int if22 = ifsa2.findSubsequence(ifsubsequence2, true);
	System.out.format("ifsa2 test: first index = %d, last index = %d\n",
			  if21, if22);
	System.out.format("ifsequence2: first index = %d, last index = %d\n",
			  ifA2[if21], ifA2[if22]);
	System.out.format("ifsa2: any index = %d\n",
			  ifsa2.findSubsequence(ifsubsequence2));
	SuffixArray.Range range2 = ifsa2.findRange(ifsubsequence2);

	System.out.println("range2.size() = " + range2.size());
	for (int ind2: range2) {
	    for (int i = 0; i < ifsubsequence2.length; i++) {
		if (ifsubsequence2[i] != ifsequence2[ind2+i]) {
		    System.out.println("found wrong subsequence");
		    System.exit(1);
		}
	    }
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	}

	System.out.println("Now iterate in sequence order");
	int lastind = -1;
	for (int ind2: range2.sequenceOrder()) {
	    if (lastind > ind2) {
		System.out.println("sequenceOrder() failed");
		System.exit(1);
	    }
	    System.out.println("using iterator, subsequence2 starting at "
			       + ind2);
	    lastind = ind2;
	}
	int[] rangeArray = new int[range2.size()*2];

	range2.toArray(rangeArray, 0);
	range2.toArray(rangeArray, range2.size());
	for (int i = 0; i < range2.size(); i++) {
	    if (rangeArray[i] != rangeArray[i+range2.size()]) {
		System.out.println("toArray(Range,int[]) failed");
		System.exit(1);

	    }
	}
	
	System.exit(0);
    }
}
