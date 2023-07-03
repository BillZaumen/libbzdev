package org.bzdev.util;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

//@exbundle org.bzdev.util.lpack.Util

// From https://www.geeksforgeeks.org/aho-corasick-algorithm-pattern-searching/
// Java program for implementation of
// Aho Corasick algorithm for String
// matching
// This code is contributed by Princi Singh

 
/**
 * This class provides an implementation of the Aho Corasick string-matching
 * algorithm.
 * <P>
 * If there are a number of predetermined patterns (substrings) to search for,
 * {@link ACMatcher} will outperform the same search using a
 * {@link SuffixArray}. If multiple searches are performed on the same
 * string, with the pattern or substring not known in advance, a
 * {@link SuffixArray} is a better choice.
 * <P>
 * The implementation is based on one contributed to the Geeks for
 * Geeks website by Princi Singh as an example of
 * <A HREF="https://www.geeksforgeeks.org/aho-corasick-algorithm-pattern-searching/">
 * an Aho Corasick algorithm implementation</A>.  The modifications included
 * using a {@link BitSet} instead of an integer (which restricted the number
 * of patterns to less than 32), a dynamically configured alphabet, and
 * methods to support iterators and streams.  The implementation in this
 * package is case sensitive.
 */
public class ACMatcher  {

    static String
	errorMsg(String key, Object... args)
    {
	return UtilErrorMsg.errorMsg(key, args);
    }


    /**
     * Match result holder.
     */
    public static class MatchResult {
	private int index;
	private int start;
	private int end;

	/**
	 * Get the index into the of strings array passsed to the constutctor
	 * of {@link ACMatcher} for this match.
	 * @return the index
	 */
	public int getIndex() {
	    return index;
	}
	
	/**
	 * Get the index into the text being matched for the start of a match
	 * @return the index 
	 */
	public int getStart() {
	    return start;
	}

	/**
	 * Get the index into the text being matched for the end of a match.
	 * This index is one higher than the last character in the matched
	 * text.
	 * @return the index 
	 */
	public int getEnd() {
	    return end;
	}

	MatchResult(int index, int start, int end) {
	    this.index = index;
	    this.start = start;
	    this.end = end;
	}
    }
    
 
    HashMap<Character,Integer> alphabet;
    int[] alphabetArray = null;
    int asize = 0;

    String[] patterns = null;

    private int getChar(char ch) {
	if (alphabetArray != null) {
	    if (ch < alphabetArray.length) {
		return alphabetArray[ch];
	    } else {
		return asize;
	    }
	} else {
	    Integer c = alphabet.get(ch);
	    if (c == null) return asize;
	    else return (int)c;
	}
    }

    // Max number of states in the matching
    // machine. Should be equal to the sum
    // of the length of all keywords.
    private int MAXS;
 
    // Maximum number of characters
    // in input alphabet
    private int MAXC;
 
    // OUTPUT FUNCTION IS IMPLEMENTED USING out[]
    // Bit i in this mask is one if the word with
    // index i appears when the machine enters
    // this state.
    private BitSet []out;
 
    // FAILURE FUNCTION IS IMPLEMENTED USING f[]
    private int []f;
 
    // GOTO FUNCTION (OR TRIE) IS
    // IMPLEMENTED USING g[][]
    private int [][]g;
 
    private void buildMatchingMachine(String arr[])
    {
	int k = arr.length;
	
	for(int i = 0; i < MAXS; i++) {
	    out[i] = new BitSet(k);
	    Arrays.fill(g[i], -1);
	}
	int states = 1;
 
	for(int i = 0; i < k; ++i) {
	    String word = arr[i];
	    int currentState = 0;
 
	    for(int j = 0; j < word.length(); ++j) {
		int ch = getChar(word.charAt(j));
		if (g[currentState][ch] == -1)
		    g[currentState][ch] = states++;
 
		currentState = g[currentState][ch];
	    }
	    out[currentState].set(i);
	}
 
	for(int ch = 0; ch < MAXC; ++ch) {
	    if (g[0][ch] == -1)
		g[0][ch] = 0;
	}

	Arrays.fill(f, -1);
	Queue<Integer> q = new LinkedList<>();

	for(int ch = 0; ch < MAXC; ++ch) {
	    if (g[0][ch] != 0) {
		f[g[0][ch]] = 0;
		q.add(g[0][ch]);
	    }
	}
 
	while (!q.isEmpty()) {
	    int state = q.peek();
	    q.remove();

	    for(int ch = 0; ch < MAXC; ++ch) {
		if (g[state][ch] != -1) {
		    int failure = f[state];
		    while (g[failure][ch] == -1) {
			failure = f[failure];
		    }
		    failure = g[failure][ch];
		    f[g[state][ch]] = failure;
		    out[g[state][ch]].or(out[failure]);
		    q.add(g[state][ch]);
		}
	    }
	}
    }
 
    private int findNextState(int currentState, char nextInput)
    {
	int answer = currentState;
	int ch = getChar(nextInput);

	while (g[answer][ch] == -1) {
	    answer = f[answer];
	}
 
	return g[answer][ch];
    }
 
    int[] kmap;

    private static String[] getStringArray(String first, String[] rest) {
	String strings[] = new String[1 + rest.length];
	strings[0] = first;
	System.arraycopy(rest, 0, strings, 1, rest.length);
	return strings;
    }

    /**
     * Constructor using a variable number of arguments.
     * @param first the first search string
     * @param rest the remaining search strings
     */
    public ACMatcher(String first, String... rest) {
	this(getStringArray(first, rest));
    }

    /**
     * Get the number of patterns for this matcher.
     * @return the number of patterns
     */
    public int size() {
	return patterns.length;
    }

    /**
     * Get the patterns (search strings) for this matcher.
     * @return the patterns
     */
    public String[] getPatterns() {
	String[] array = new String[patterns.length];
	System.arraycopy(patterns, 0, array, 0, patterns.length);
	return array;
    }

    /**
     * Constructor.
     * @param strings the strings to match
     */
    public ACMatcher(String[] strings) {
	MAXS = 1;
	int alen = 128;
	int k = 0;
	int kk = 0;
	kmap = new int[strings.length];
	for (String s: strings) {
	    if (s == null || s.length() == 0) {
		kk++;
		continue;
	    }
	    MAXS += s.length();
	    alen += (int)Math.round(Math.log(s.length()));
	    kmap[k] = kk;
	    k++; kk++;
	}
	patterns = new String[k];
	k = 0;
	for (String s: strings) {
	    if (s == null || s.length() == 0) continue;
	    patterns[k] = s;
	    k++;
	}

	alphabet = new HashMap<Character,Integer>(alen);

	MAXC = 1;
	
	char maxChar = 0;
	for (String s: patterns) {
	    if (s == null) continue;
	    int len = s.length();
	    if (len == 0) continue;
	    for (int i = 0; i < len; i++) {
		char ch = s.charAt(i);
		if (ch > maxChar) maxChar = ch;
		if (alphabet.containsKey(ch)) {
		    continue;
		}
		alphabet.put(ch, asize++);
		MAXC++;
	    }
	}
	if (maxChar < 1024) {
	    alphabetArray = new int[maxChar+1];
	    Arrays.fill(alphabetArray, asize);
	    for (Map.Entry<Character,Integer> entry: alphabet.entrySet()) {
		alphabetArray[entry.getKey()] = entry.getValue();
	    }
	}

	out = new BitSet[MAXS];
	f = new int[MAXS];
	g = new int[MAXS][MAXC];
	
	buildMatchingMachine(patterns);
    }


    /**
     * Get an {@link Iterable} that can search the given text for
     * the patterns used to configure this matcher.
     * <P>
     * This is a convenience method, added to allow an instance of
     * this class to be used in a 'for' loop:
     * <BLOCKQUOTE><PRE><CODE>
     * String patterns[] = {...};
     * ACMatcher matcher = new ACMatcher(patterns);
     * String text = "...";
     * for (ACMatcher.MatchResult mr: matcher.iterableOver(text)) {
     *    ...
     * }
     * </CODE></PRE></BLOCKQUOTE>
     * @return the iterable
     */
    public Iterable<MatchResult> iterableOver(String text) {
	return new Iterable<MatchResult>() {
	    public Iterator<MatchResult> iterator() {
		return ACMatcher.this.iterator(text);
	    }
	};
    }

    /**
     * Get a stream of matches for the given text.
     * @param text the text to scan
     * @return the stream
     */
    public Stream<MatchResult> stream(String text) {
	return StreamSupport
	    .stream(iterableOver(text).spliterator(), false);
    }

    /**
     * Get an iterator that will scan some given text and match that
     * text against the patterns used to configure this matcher.
     * @param text the text to scan
     * @return an iterator that will enumerate the matches
     * @see ACMatcher.MatchResult
     */
    public Iterator<MatchResult> iterator(String text) {
	final int k = (patterns == null)? 0: patterns.length;
	if (k == 0) {
	    return new Iterator<MatchResult>() {
		public boolean hasNext() {return false;}
		public MatchResult next() throws NoSuchElementException {
		    throw new NoSuchElementException(errorMsg("noNextElem"));
		}
	    };
	}
	return new Iterator<MatchResult>() {
	    int currentState = 0;
	    int index = -1;
	    int lenm1 = text.length() - 1;
	    boolean done = false;
	    Queue<MatchResult> queue = new LinkedList<MatchResult>();

	    private void update() {
		if (queue.isEmpty()) {
		    while (index < lenm1) {
			index++;
			currentState = findNextState(currentState,
						     text.charAt(index));
			if (!out[currentState].isEmpty()) {
			    for (int j = 0; j < k; j++) {
				if (out[currentState].get(j)) {
				    int end = index + 1;
				    int start = end - patterns[j].length();
				    MatchResult mr = new MatchResult(kmap[j],
								     start,
								     end);
				    queue.offer(mr);
				}
			    }
			    break;
			}
		    }
		    done = queue.isEmpty();
		}
	    }

	    @Override
	    public boolean hasNext() {
		update();
		return !done;
	    }
	    
	    @Override
	    public MatchResult next() throws NoSuchElementException {
		update();
		MatchResult mr = queue.poll();
		if (mr == null) {
		    throw new NoSuchElementException(errorMsg("noNextElem"));
		}
		return mr;
	    }
	};
    }
}

