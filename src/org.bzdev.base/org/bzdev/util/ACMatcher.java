package org.bzdev.util;
import org.bzdev.lang.MathOps;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.function.Function;

//@exbundle org.bzdev.util.lpack.Util

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
 * using a {@link BitSet} instead of an integer (which restricted the
 * number of patterns to  32), a dynamically configured
 * alphabet, and methods to provide iterators and streams. The
 * implementation in this package can be configured to be either case
 * sensitive or case insensitive.  Finally, the sequence being
 * searched can be represented as either a string or an array of
 * characters, and the search can be restricted to a subsequence of
 * either.
 * <P>
 * The patterns to search for are passed to a constructor. The methods
 * used to search are named
 * <UL>
 *   <LI><STRONG>iterator</STRONG>. This provides an {@link Iterator}.
 *   <LI><STRONG>iterableOver</STRONG>. This provides an {@link Iterable}
 *     for use in a 'for' loop.
 *   <LI><STRONG>stream</STRONG>. This provides a {@link Stream}.
 * <UL>
 * These are overloaded so that the text being searched can be either a
 * string or a character array, and so that all the text can be searched
 * or just a subsequence of the text.
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
	 * Get the index into the array of strings passed to the constructor
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

    boolean caseSensitive = true;

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
	int ch = (caseSensitive)? getChar(nextInput):
	    getChar(Character.toLowerCase(nextInput));

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
     * The search is case sensitive.
     * @param first the first search string
     * @param rest the remaining search strings
     */
    public ACMatcher(String first, String... rest) {
	this(false, getStringArray(first, rest));
    }

    /**
     * Constructor using a variable number of arguments and indicating
     * if the search is case sensitive or case insensitive.
     * @param ignoreCase true if the search is case insensitive; false
     *        if the search is case sensitive
     * @param first the first search string
     * @param rest the remaining search strings
     */
    public ACMatcher(boolean ignoreCase, String first, String... rest) {
	this(ignoreCase, getStringArray(first, rest));
    }

    /**
     * Get the number of patterns for this matcher.
     * @return the number of patterns
     */
    public int size() {
	return patterns.length;
    }

    private static <T> String[]
	createPatterns(Function<T,String> f, T[] specs)
    {
	String[] patterns = new String[specs.length];
	for (int i = 0; i < specs.length; i++) {
	    patterns[i] = f.apply(specs[i]);
	}
	return patterns;
    }


    /**
     * Constructor using an array of pattern specifications.
     * A function maps each pattern specification to the corresponding
     * pattern. This can be used to associate each pattern with a
     * enumeration, which can make the use of a <CODE>switch</CODE>
     * statement more reliable when new cases are added.  For
     * example,
     * <BLOCKQUOTE><PRE><CODE>
     * static enum SpecType {
     *      TYPE1,
     *      TYPE2,
     *      ...
     * }
     * static class Spec {
     *   SpecType type
     *   String pattern;
     *   public Spec(specType type, string pattern) {
     *       this.type = type;
     *       this.pattern = pattern;
     * }
     * ...
     *   Spec specs[] = {
     *       new Spec(SpecType.TYPE1, "foo"),
     *       new Spec(SpecType.TYPE2, "bar"),
     *       ...
     *   };
     *   ACMatcher matcher = new
     *     ACMatcher((spec) -&gt; {return spec.pattern;}, specs);
     *   for (ACMatcher.MatchResult mr: matcher.interatorOver(text)) {
     *       int index = mr.getIndex();
     *       switch (spec[index].type) {
     *       case TYPE1:
     *          ...
     *       }
     *   }
     * </CODE></PRE></BLOCKQUOTE>
     * @param f a function that maps a pattern specification to a pattern
     * @param specs an array containing pattern specifications
     * @param <T> the type of the objects used as a pattern specifications
     */

    public <T> ACMatcher(Function<T,String> f, T[] specs) {
	this(false, createPatterns(f, specs));
    }

    /**
     * Constructor using an array of pattern specifications and specifying
     * if the matcher is case sensitive.
     * A function maps each pattern specification to the corresponding
     * pattern. This can be used to associate each pattern with a
     * enumeration, which can make the use of a <CODE>switch</CODE>
     * statement more reliable when new cases are added.  For
     * example,
     * <BLOCKQUOTE><PRE><CODE>
     * static enum SpecType {
     *      TYPE1,
     *      TYPE2,
     *      ...
     * }
     * static class Spec {
     *   SpecType type
     *   String pattern;
     *   public Spec(specType type, string pattern) {
     *       this.type = type;
     *       this.pattern = pattern;
     * }
     * ...
     *   Spec specs[] = {
     *       new Spec(SpecType.TYPE1, "foo"),
     *       new Spec(SpecType.TYPE2, "bar"),
     *       ...
     *   };
     *   ACMatcher matcher = new
     *     ACMatcher((spec) -&gt; {return spec.pattern;}, specs);
     *   for (ACMatcher.MatchResult mr: matcher.interatorOver(text)) {
     *       int index = mr.getIndex();
     *       switch (spec[index].type) {
     *       case TYPE1:
     *          ...
     *       }
     *   }
     * </CODE></PRE></BLOCKQUOTE>
     * @param ignoreCase true if the search is case insensitive; false
     *        if the search is case sensitive
     * @param f a function that maps a pattern specification to a pattern
     * @param specs an array containing pattern specifications
     * @param <T> the type of the objects used as a pattern specifications
     */

    public <T> ACMatcher(boolean ignoreCase,
			 Function<T,String> f, T[] specs)
    {
	this(ignoreCase, createPatterns(f, specs));
    }


    /**
     * Get the patterns (search strings) for this matcher.
     * If the search is case insensitive, the patterns will use
     * lower case, regardless of the cases used in a constructor.
     * @return the patterns
     */
    public String[] getPatterns() {
	String[] array = new String[patterns.length];
	System.arraycopy(patterns, 0, array, 0, patterns.length);
	return array;
    }

    /**
     * Constructor.
     * The search is case sensitive.
     * @param strings the strings to match
     */
    public ACMatcher(String[] strings) {
	this(false, strings);
    }

    /**
     * Constructor specifying if the matcher is case sensitive or case
     * insensitive.
     * @param ignoreCase true if the search is case insensitive; false
     *        if the search is case sensitive
     * @param strings the strings to match
     */
    public ACMatcher(boolean ignoreCase, String[] strings) {
	this.caseSensitive = !ignoreCase;
	if (caseSensitive == false) {
	    String nstrings[] = new String[strings.length];
	    for (int i = 0; i < strings.length; i++) {
		String s = strings[i];
		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int j = 0; j < len; j++) {
		    sb.append(Character.toLowerCase(s.charAt(j)));
		}
		nstrings[i] = sb.toString();
	    }
	    strings = nstrings;
	}

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
	    // Increase alen by a value proportional to the logarithm
	    // of the length of each pattern.  The value of alen is used
	    // as an estimate of an appropriate initial's hash-table size.
	    alen += 4 * (int)Math.round(MathOps.log2(s.length(), 1.0));
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
     * Get an {@link Iterable} that searches the given text, provided
     * as a string, for the patterns used to configure this matcher.
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
     * @param text the text to search
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
     * Get an {@link Iterable} that starch's a portion of the given
     * text, provided as a string, for the patterns used to configure
     * this matcher.
     * <P>
     * This is a convenience method, added to allow an instance of
     * this class to be used in a 'for' loop:
     * <BLOCKQUOTE><PRE><CODE>
     * String patterns[] = {...};
     * ACMatcher matcher = new ACMatcher(patterns);
     * String text = "...";
     * int start = ...;
     * int end = ...;
     * for (ACMatcher.MatchResult mr: matcher.iterableOver(text, start, end)) {
     *    ...
     * }
     * </CODE></PRE></BLOCKQUOTE>
     * @param text the text to search
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return the iterable
     */
    public Iterable<MatchResult> iterableOver(String text, int start, int end) {
	return new Iterable<MatchResult>() {
	    public Iterator<MatchResult> iterator() {
		return ACMatcher.this.iterator(text, start, end);
	    }
	};
    }

    /**
     * Get an {@link Iterable} that searcher the given text, provided
     * as a character array, for the patterns used to configure this
     * matcher.
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
     * @param text the text to search
     * @return the iterable
     */
    public Iterable<MatchResult> iterableOver(char[] text) {
	return new Iterable<MatchResult>() {
	    public Iterator<MatchResult> iterator() {
		return ACMatcher.this.iterator(text);
	    }
	};
    }

    /**
     * Get an {@link Iterable} that searcher's a portion of the given
     * text, provided as a character array, for the patterns used to
     * configure this matcher.
     * <P>
     * This is a convenience method, added to allow an instance of
     * this class to be used in a 'for' loop:
     * <BLOCKQUOTE><PRE><CODE>
     * String patterns[] = {...};
     * ACMatcher matcher = new ACMatcher(patterns);
     * String text = "...";
     * int start = ...;
     * int end = ...;
     * for (ACMatcher.MatchResult mr: matcher.iterableOver(text, start, end)) {
     *    ...
     * }
     * </CODE></PRE></BLOCKQUOTE>
     * @param text the text to search
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return the iterable
     */
    public Iterable<MatchResult> iterableOver(char[] text, int start, int end) {
	return new Iterable<MatchResult>() {
	    public Iterator<MatchResult> iterator() {
		return ACMatcher.this.iterator(text, start, end);
	    }
	};
    }

    /**
     * Get a stream of matches for the given text, provided as a string.
     * @param text the text to scan
     * @return the stream
     */
    public Stream<MatchResult> stream(String text) {
	return StreamSupport
	    .stream(iterableOver(text).spliterator(), false);
    }

    /**
     * Get a stream of matches for a portion of the given text,
     * provided as a string.
     * @param text the text to scan
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return the stream
     */
    public Stream<MatchResult> stream(String text, int start, int end) {
	return StreamSupport
	    .stream(iterableOver(text, start, end).spliterator(), false);
    }

    /**
     * Get a stream of matches for the given text, provided as a character
     * array.
     * @param text the text to scan
     * @return the stream
     */
    public Stream<MatchResult> stream(char[] text) {
	return StreamSupport
	    .stream(iterableOver(text).spliterator(), false);
    }

    /**
     * Get a stream of matches for a portion the given text, provided
     * as a character array.
     * @param text the text to scan
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return the stream
     */
    public Stream<MatchResult> stream(char[] text, int start, int end) {
	return StreamSupport
	    .stream(iterableOver(text, start, end).spliterator(), false);
    }

    /**
     * Get an iterator that will scan a given string and match that
     * string against the patterns used to configure this matcher.
     * @param text the string to scan
     * @return an iterator that will enumerate the matches
     * @see ACMatcher.MatchResult
     */
    public Iterator<MatchResult> iterator(String text) {
	return iterator(text, 0, text.length());
    }
    /**
     * Get an iterator that will scan a portion of a given string and
     * match that string against the patterns used to configure this
     * matcher.
     * @param text the string to scan
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return an iterator that will enumerate the matches
     * @see ACMatcher.MatchResult
     */
    public Iterator<MatchResult> iterator(String text, int start, int end) {

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
	    int index = start - 1;
	    int lenm1 = end - 1;
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

    /**
     * Get an iterator that will scan a given character array and match that
     * sequence against the patterns used to configure this matcher.
     * @param text the character array to scan
     * @return an iterator that will enumerate the matches
     * @see ACMatcher.MatchResult
     */
    public Iterator<MatchResult> iterator(char[] text) {
	return iterator(text, 0, text.length);
    }
    /**
     * Get an iterator that will scan a portion of a given character
     * array and match that sequence against the patterns used to
     * configure this matcher.
     * @param text the character array to scan
     * @param start the starting index (inclusive) for the text being scanned
     * @param end the ending index (exclusive) for the text being scanned
     * @return an iterator that will enumerate the matches
     * @see ACMatcher.MatchResult
     */
    public Iterator<MatchResult> iterator(char[] text, int start, int end) {
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
	    int index = start - 1;
	    int lenm1 = end - 1;
	    boolean done = false;
	    Queue<MatchResult> queue = new LinkedList<MatchResult>();

	    private void update() {
		if (queue.isEmpty()) {
		    while (index < lenm1) {
			index++;
			currentState = findNextState(currentState,
						     text[index]);
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

//  LocalWords:  exbundle Aho Corasick substrings ACMatcher substring
//  LocalWords:  SuffixArray Princi HREF BitSet subsequence Iterable
//  LocalWords:  iterableOver GOTO TRIE matcher BLOCKQUOTE PRE enum
//  LocalWords:  SpecType specType MatchResult mr interatorOver alen
//  LocalWords:  getIndex iterable noNextElem ignoreCase
