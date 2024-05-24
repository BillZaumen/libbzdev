package org.bzdev.util;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.net.URL;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Template Processor.
 * A template processor is initialized with a list of keymaps, searched
 * last to first so that later entries override previous one. A template
 * file contains text and directives.  The directives are denoted by an
 * initial '$' followed by a delimiter, the default being '('. The directive
 * consists of all the text up to but not including the closing delimiter,
 * whose default is ')'.  The sequence "$$" is replaced with a single '$'
 * and any following character is treated as an ordinary character.
 * <p>
 * The directives are treated as follows:
 * <ul>
 * <li> Directives starting with '!' are treated as comments.
 * <li> A directive consisting of letters, digits, underscores ('_')
 *       or periods ('.') indicate that text is to be replaced. The
 *       directive is treated as a string used to look up a
 *       replacement string.  If the lookup fails, the replacement is
 *       an empty string.
 * <li> A directive consisting of two subdirectives (sequences of letters,
 *       digits, underscores, or periods), with the two sequences separated by a
 *       colon (":") indicate repetition.  The first subdirective is
 *       treated as a string used to look up an array of KeyMap
 *       objects.  The second subdirective indicates the name of a
 *       directive terminating the part of the template where
 *       iteration applies. For this region, each KeyMap is put at the
 *       top of a KeyMap stack in turn and that region of the template
 *       is processed until the KeyMap array is exhausted.  Iterations can
 *       be nested, but the names of the second subdirective of different
 *       iteration directives must differ if the iteration directives are
 *       nested, and the names of the first subdirective of an iteration
 *       directive must not collide with the name of a string-replacement
 *       directive (otherwise only one will be visible).
 * <li> A directive consisting of a '+' or '-' followed by two
 *       subdirectives (sequences of letters, digits, underscores, or
 *       periods), with the two sequences separated by a colon (":") indicate
 *       repetition conditionally:
 *       <UL>
 *          <li> When the first subdirective is prefaced by a '+' and
 *               there is no key matching the subdirective, no iterations will
 *               be performed. If there is a key, the default number of
 *               iterations is 1, but that can be changed by using the
 *               method
 *               {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)}.
 *          <li> When the first subdirective is prefaced by a '-' and
 *               there is a key matching the subdirective, no iterations will
 *               be performed. If there is not a key, the default number of
 *               iterations is 1, but that can be changed by using the
 *               method
 *               {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)}.
 *       </UL>
 *       Directives with a '+' or '-' can be used to create conditional
 *       statements without having to explicitly add new directives whose
 *       only use is to indicate status.
 * </ul>
 * <p>
 *  KeyMap keys must match directives.  For a simple replacement,
 *  the value must be a string.  For iteration, the KeyMap key
 *  corresponding to the first subdirective must be an array of KeyMap
 *  instances or an instance of TemplateProcessor.KeyMapIterable,
 *  determining the mappings to be used in each iteration.  As a special
 *  case, if the KeyMap value is another KeyMap, the iteration
 *  includes only that KeyMap.  The class TemplateProcessor.KeyMapList
 *  provides a list implementation of the KeyMapIterable interface.
 */

public class TemplateProcessor {

    static String errorMsg(String key, Object... args) {
	return UtilErrorMsg.errorMsg(key, args);
    }

    private static ThreadLocal<TemplateProcessor.KeyMap> root
	= new ThreadLocal<>();

    /**
     * Interface for objects that provide an iterator to generate
     * a new KeyMap for each iteration.
     */
    public interface KeyMapIterable extends Iterable<KeyMap> {}

    /**
     * KeyMap list for iteration.
     * The list will be traversed to find a new KeyMap for each
     * iteration.
     */
    public static class KeyMapList extends LinkedList<KeyMap>
    implements KeyMapIterable {}

    /**
     * Map from TemplateProcessor keys to their corresponding values.
     * The keys are correspond to the first token in a TemplateProcessor
     * directive.  The corresponding value is typically a string, but
     * may be one of the following:
     * <UL>
     *   <LI> An instance of a TemplateProcessor iteration class or interface
     *        ({@link TemplateProcessor.KeyMapIterable},
     *         {@link TemplateProcessor.KeyMapList}, an array of
     *         {@link TemplateProcessor.KeyMap}, or [as a special case
     *         that provides a single iteration]
     *         {@link TemplateProcessor.KeyMap}).
     *   <LI> Any other object (but typically a string) whose
     *        toString() method will produce the desired text.
     * </UL>
     * There is a one additional method that a {@link Map} does not
     * provide:
     * {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)}.
     * This method is used to define custom iterations depending on
     * whether a value exists or not.
     */
    public static class KeyMap extends HashMap<String,Object> {
	/**
	 * Get the value associated with a key.
	 * @param key the key
	 * @return the corresponding value
	 */
	@Override
	public Object get(Object key) {
	    if (key != null && !(key instanceof String)) {
		throw new IllegalArgumentException(errorMsg("keyNotString"));
	    }
	    String k = (String)key;
	    boolean plus = (k == null)? false: k.startsWith("+");
	    boolean minus = (k == null)? false: k.startsWith("-");
	    String ourkey  = (plus || minus)? k.substring(1): k;
	    if (plus) {
		if (super.containsKey(ourkey)) {
		    if (super.containsKey(key)) {
			return super.get(key);
		    } else {
			return TemplateProcessor.emptyKeyMap;
		    }
		} else {
		    return null;
		}
	    } else if (minus) {
		if (!super.containsKey(ourkey)) {

		    if (super.containsKey(key)) {
			return super.get(key);
		    } else {
			if (this == root.get()) {
			    return TemplateProcessor.emptyKeyMap;
			} else {
			    return null;
			}
		    }
		} else {
		    // return null;
		    return TemplateProcessor.emptyKeyMapArray;
		}
	    } else {
		return super.get(key);
	    }
	}

	/**
	 * Set the value for a key.
	 * The key must not start with the characters '+' or '-'.
	 * A key containing white space will not be recognized by a
	 * template processor.
	 * @param key the key
	 * @param value the value corresponding to the key (either a
	 *        {@link java.lang.String} or an instance of a class
	 *        implementing {@link TemplateProcessor.KeyMapIterable},
	 *        with a null value trated as an empty string)
	 * @return the previous object; null if there was none
	 */
	@Override
	public Object put(String key, Object value)
	    throws IllegalArgumentException
	{
	    if (key.startsWith("-") || key.startsWith("+")) {
		throw new IllegalArgumentException(errorMsg("negposkey"));
	    }
	    Object result = super.put(key, value);
	    return result;
	}

	/**
	 * Set a key-map entry for both a key and its '+' or '-'
	 * conditional directives.
	 * <P>
	 * A value, if not null, must be either a {@link java.lang.String}
	 * or an instance of a class implementing
	 * {@link TemplateProcessor.KeyMapIterable}.
	 * @param key the key
	 * @param value the value for the key (if null, the key will
	 *        not be entered, but KeyMap entries for the hasList
	 *        and hasntList will be entered if non null)
	 * @param hasList an instance of {@link TemplateProcessor.KeyMap},
	 *        {@link TemplateProcessor.KeyMapList},
	 *        {@link TemplateProcessor.KeyMapIterable},
	 *        or an array of {@link TemplateProcessor.KeyMap}
	 *        that provides the map or list to use for iteration given
	 *         an initial '+'
	 *        in the key when the key has a non-null value
	 * @param hasntList an instance of {@link TemplateProcessor.KeyMap},
	 *        {@link TemplateProcessor.KeyMapList},
	 *        {@link TemplateProcessor.KeyMapIterable},
	 *        or an array of {@link TemplateProcessor.KeyMap}
	 *        that provides the map or list to use for iteration
	 *        given an initial '-' in the key when the key has a
	 *        null value
	 */
	public void put(String key, Object value,
			Object hasList, Object hasntList)
	    throws IllegalArgumentException
	{
	    if (key.startsWith("-") || key.startsWith("+")) {
		throw new IllegalArgumentException(errorMsg("negposkey"));
	    }
	    if ((hasList != null && !(hasList instanceof KeyMap
				      || hasList instanceof KeyMapList
				      || hasList instanceof KeyMapIterable
				      || hasList instanceof KeyMap[]))
		|| (hasntList != null
		    && !(hasntList instanceof KeyMap
			 || hasntList instanceof KeyMapList
			 || hasntList instanceof KeyMapIterable
			 || hasntList instanceof KeyMap[]))) {
		throw new IllegalArgumentException(errorMsg("keymapList"));
	    }
	    if (value != null) {
		super.put(key, value);
	    }
	    if (hasList != null) {
		super.put("+" + key, hasList);
	    }
	    if (hasntList != null) {
		super.put("-" + key, hasntList);
	    }
	}

	/**
	 * Take the key-value pairs provided by a map and add these to
	 * this map.
	 * @param m a map associating keys with values
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
	    for (Map.Entry<? extends String, ? extends Object>entry:
		     m.entrySet()) {
		String key = entry.getKey();
		Object value = entry.getValue();
		if (key.startsWith("-") || key.startsWith("+")) {
		    // if not a string it is a key map or a key map list
		    if (value instanceof String) {
			throw new IllegalArgumentException
			    (errorMsg("negposkey"));
		    }
		}
	    }
	    super.putAll(m);
	}

	/**
	 * Print this KeyMap to System.out using the UTF-8 charset.
	 * <P>
	 * The output format uses indentation to indicate nesting.
	 * When a key's value is another keymap or one of the keymap
	 * iteration classes, the type of iteration used is shown in
	 * square brackets immediately after the key, followed by
	 * indented descriptions of the keys provided by each iteration.
	 * When a key was constructed using the method
	 * {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)},
	 * the iteration for the '+' and '-' variants will be flagged
	 * with the phrase "ignored" or "in use" depending on whether or
	 * not the the key has a non-null or null value respectively.
	 */
	public void print() {
	    print(System.out, "UTF-8");
	}

	/**
	 * Print this KeyMap, specifying an output stream and character
	 * set.
	 * <P>
	 * The output format uses indentation to indicate nesting.
	 * When a key's value is another keymap or one of the keymap
	 * iteration classes, the type of iteration used is shown in
	 * square brackets immediately after the key, followed by
	 * indented descriptions of the keys provided by each iteration.
	 * When a key was constructed using the method
	 * {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)},
	 * the iteration for the '+' and '-' variants will be flagged
	 * with the phrase "ignored" or "in use" depending on whether or
	 * not the the key has a non-null or null value respectively.
	 * @param os the output stream
	 * @param csn the name of the character set to use
	 */
	public void print(OutputStream os, String csn) {
	    print(new PrintWriter(os, true, Charset.forName(csn)));
	}

	/**
	 * Print this KeyMap using PrintWriter.
	 * <P>
	 * The output format uses indentation to indicate nesting.
	 * When a key's value is another keymap or one of the keymap
	 * iteration classes, the type of iteration used is shown in
	 * square brackets immediately after the key, followed by
	 * indented descriptions of the keys provided by each iteration.
	 * When a key was constructed using the method
	 * {@link TemplateProcessor.KeyMap#put(String,Object,Object,Object)},
	 * the iteration for the '+' and '-' variants will be flagged
	 * with the phrase "ignored" or "in use" depending on whether or
	 * not the the key has a non-null or null value respectively.
	 * @param out a print writer
	 */
	public void print(PrintWriter out) {
	    printKeymap("", this, out);
	}

	/*
	private static void printKeymap(String prefix,
					TemplateProcessor.KeyMapIterable kmlist,
					PrintWriter out)
	{
	    int i = 0;
	    for (KeyMap km: kmlist) {
		i++;
		out.println(prefix + "keymap list entry " + i +":");
		printKeymap(prefix + "  ", km, out);
	    }
	}

	private static void printKeymap(String prefix,
					TemplateProcessor.KeyMap[] kmlist,
					PrintWriter out)
	{
	    int i = 0;
	    for (KeyMap km: kmlist) {
		i++;
		out.println(prefix + "keymap list entry " + i +":");
		printKeymap(prefix + "  ", km, out);
	    }
	}
	*/

	private static void printKeymap(String prefix,
					TemplateProcessor.KeyMap keymap,
					PrintWriter out)
	{
	    if (keymap == null) {
		out.println(prefix + "  [keymap is null");
		return;
	    }
	    for (Map.Entry<String,Object> entry: keymap.entrySet()) {
		String key = entry.getKey();
		String usage = "";
		if (key.startsWith("+")){
		    if (keymap.containsKey(key.substring(1))) {
			usage = ", in use";
		    } else {
			usage = ", ignored";
		    }
		} else if (key.startsWith("-")) {
		    if (keymap.containsKey(key.substring(1))) {
			usage = ", ignored";
		    } else {
			usage = ", in use";
		    }
		}
		Object value = entry.getValue();
		if (value instanceof String) {
		    out.println(prefix + "key " + key + ": value = \""
				+ (String) value + "\"");
		} else if (value instanceof TemplateProcessor.KeyMap) {
		    TemplateProcessor.KeyMap submap =
			(TemplateProcessor.KeyMap) value;
		    if (submap.size() == 0) {
			out.println(prefix + "key " + key
				    + ": [empty single iteration"
				    + usage + "]");
		    } else {
			out.println(prefix + "key " + key
				    + ": [single iteration" + usage +"]");
			printKeymap(prefix + "  ", submap, out);
		    }
		} else if (value instanceof TemplateProcessor.KeyMap[]) {
		    TemplateProcessor.KeyMap[] submap =
			(TemplateProcessor.KeyMap[]) value;
		    if (submap.length == 0) {
			out.println(prefix + "key " + key
				    + ": [no iterations" + usage + "]");
		    } else if (submap.length == 1) {
			out.format("%s%s: [single iteration]\n", prefix, key);
		    } else {
			int count = 0;
			out.format("%s%s: [%d iterations%s]\n",
				   prefix, key, submap.length, usage);
			for (TemplateProcessor.KeyMap kmap: submap) {
			    out.format("%s*** iteration %d\n", prefix + "  ",
				       ++count);
			    printKeymap(prefix + "    ", kmap, out);
			}
		    }
		    /*
		    printKeymap(prefix + "    ",
				(TemplateProcessor.KeyMap[]) value,
				out);
		    */
		} else if (value instanceof TemplateProcessor.KeyMapIterable) {
		    out.println(prefix + "key " + key +": [iteration(s)"
				+ usage + "]");
		    TemplateProcessor.KeyMapIterable kmaps =
			(TemplateProcessor.KeyMapIterable) value;
		    int count = 0;
		    for (TemplateProcessor.KeyMap kmap: kmaps) {
			out.format("%s*** iteration %d\n", prefix + "  ",
				   ++count);
			printKeymap(prefix + "    ", kmap, out);
		    }
		} else {
		    if (value == null) {
			out.println(prefix + "key " + key +": [NO VALUE]");
		    } else {
			Class<?> c = value.getClass();
			if (c == null) {
			    out.println(prefix + "key "  + key
					+ ": \"" + value + "\"");
			} else {
			    out.println(prefix  + "key "  + key +": ("
					+ c.getSimpleName()
					+ ")" + "\"" +value.toString() + "\"");
			}
		    }
		}
	    }
	}

	/**
	 * Constructor.
	 */
	public KeyMap() {super();}
	/**
	 * Constructor given an initial capacity.
	 * @param initialCapacity the initial capacity of the map
	 */
	public KeyMap(int initialCapacity) {super(initialCapacity);}
	/**
	 * Constructor given an initial capacity and load factor.
	 * @param initialCapacity the initial capacity of the map
	 * @param loadFactor the load factor for the map
	 */
	public KeyMap(int initialCapacity, float loadFactor) {
	    super(initialCapacity, loadFactor);
	}
	/**
	 * Constructor given an existing map.
	 * @param map an existing map that will be copied
	 */
	public KeyMap(Map<String, Object> map) {
	    super(map);
	}
    }

    /**
     * Exception class for TemplateProcessor instances.
     */

    public static class Exception extends RuntimeException {
	/**
	 * Constructor.
	 * @param message the message for the exception
	 */
	public Exception(String message) {super(message);}
    }

    /*
    LinkedList<HashMap<String,Object>> tables = 
	new LinkedList<HashMap<String,Object>>();
    ********/

    LinkedList<KeyMap> origTables = new LinkedList<KeyMap>();
    LinkedList<KeyMap> tables = new LinkedList<KeyMap>();

    char openDelim = '(';
    char closeDelim = ')';


    /**
     * Set the open-delimiter character.
     * The matching close-delimiter character is implied.
     * The default delimiter is '('.
     * @param delim an open delimiter, either '(', '{', '[', or '&lt;'.
     */
    public void setDelimiter(char delim) {
	switch (delim) {
	case '(':
	    break;
	case '{':
	    openDelim = '{';
	    closeDelim = '}';
	    break;
	case '[':
	    openDelim = '[';
	    closeDelim = ']';
	    break;
	case '<':
	    openDelim = '<';
	    closeDelim = '>';
	default:
	    throw new IllegalArgumentException
		(errorMsg("badDelimiter", "" + delim));
	}
    }


    /**
     * Constructor.
     * @param tbls a list of KeyMap tables, searched last to first for a
     *        value matching a key.
     */
    public TemplateProcessor(KeyMap... tbls) {
	for (KeyMap tbl: tbls) {
	    origTables.addFirst(tbl);
	}
    }

    String replacement(String token) {
	for (KeyMap tbl: tables) {
	    Object entry = tbl.get(token);
	    if (entry != null) {
		if (entry instanceof String) {
		    return (String) entry;
		} else {
		    String cn = entry.getClass().getName();
		    throw new TemplateProcessor.Exception
			(errorMsg("badReplacement", token, cn, "String"));
		}
	    }
	}
	return "";
    }

    // methods that modify this map are overridden to make this keymap
    // unmodifiable
    private static KeyMap emptyKeyMap = new KeyMap() {
	    @Override
	    public Object put(String k, Object v) {
		throw new UnsupportedOperationException("unmodifiableKeyMap");
	    }
	    @Override
	    public void put(String k, Object v, Object h, Object hn) {
		throw new UnsupportedOperationException("unmodifiableKeyMap");
	    }
	    @Override
	    public void putAll(Map<? extends String, ? extends Object> m) {
		throw new UnsupportedOperationException("unmodifiableKeyMap");
	    }
	};
    private static KeyMap[] emptyKeyMapArray = new KeyMap[0];

    Object replacements(String token) {
	for (KeyMap tbl: tables) {
	    Object entry = tbl.get(token);
	    if (entry != null) {
		if (entry instanceof KeyMapIterable) {
		    return (KeyMapIterable) entry;
		}
		if (entry instanceof KeyMap[]) {
		    return (KeyMap[]) entry;
		} else if (entry instanceof KeyMap) {
		    return (KeyMap) entry;
		} else {
		    // System.out.println("found " + entry.getClass());
		    String cn = entry.getClass().getName();
		    String s1 = "TemplateProcessor.KeyMap[]";
		    String s2 = " List<TemplateProcessor.KeyMap>";
		    throw new 
			TemplateProcessor.Exception
			(errorMsg("badReplacement2", token, cn, s1, s2));
		}
	    }
	}
	return emptyKeyMapArray;
    }
    

    private void processTemplate(char[] template, int start, int length,
				 Writer writer)
	throws IOException
    {
	int origlen = length;
	int origstart = start;
	int index = start;
	//  System.out.println("processing Template");
	for (;;) {
	    while (index < start + length && template[index] != '$' ) {
		index++;
	    }
	    if (index >= start + length) break;
	    if (template[index] == '$') {
		// System.out.println("found first $");
		// System.out.println(start +" " +(index - start));
		writer.write(template, start, index - start);
		length -= (1 + index - start);
		start = index + 1;
		index = start;
		if ((start - origstart + length) != origlen) {
		    throw new RuntimeException((start-origstart+length) 
					       + " != " + origlen);
		}
		if (index < start + length) {
		    /*
		    System.out.println("scanning after $, openDelim = "
				       +openDelim);
		    */
		    if (template[index] == '$') {
			// System.out.println(index +" 1");
			writer.write(template, index, 1);
			start = index + 1;
			length--;
			index = start;
		    } else if (template[index] == openDelim) {
			// System.out.println("found openDelim");
			start = index + 1;
			length--;
			index = start;
			if (template[index] == '!') {
			    while (length > 0 
				   && template[index] != closeDelim) {
				start++; index++; length--;
			    }
			    if (length == 0) continue;
			    if (template[index] == closeDelim) {
				start++; index++; length--;
				continue;
			    }
			}
			int svindex = index;
			if (template[index] == '-'
			    || template[index] == '+') {
			    index++;
			}
			while (Character.isLetterOrDigit(template[index])
			       || template[index] == '_'
			       || template[index] == '.') {
			    index++;
			}
			if (svindex == index) {
			    int lineno = 1;
			    for (int k = 0; k < svindex; k++) {
				if (template[k] == '\n') lineno++;
			    }
			    String msg = errorMsg("openingDelim", lineno);
			    throw new IOException(msg);
			}
			String token = 
			    new String(template, start, (index - start));
			// System.out.println("found token \"" +token +"\"");
			length -=(index - start);
			start = index;
			if ((start-origstart + length) != origlen) {
			    throw new 
				RuntimeException((start-origstart+length)
						 + " != " + origlen);
			}
			// index = start;
			if (template[index] == ':') {
			    start++;
			    index++;
			    length--;
			    int mark1 = index;
			    while (Character.isLetterOrDigit(template[index])
				   || template[index] == '_'
				   || template[index] == '.') {
				index++;
			    }
			    if (template[index] == closeDelim) {
				int mark2 = index;
				boolean endDirectiveFound = false;
				index++;
				length -= (index - start);
				start = index;
				if ((start-origstart +length) != origlen) {
				    throw new 
					RuntimeException((start-origstart+length)
							 + " != " 
							 + origlen);
				}

				/*
				String end = new String(template, mark1,
							(mark2 - mark1));
				System.out.println("start = " + start
						   +", mark1 = " +mark1
						   +", mark2 = " +mark2
						   +" \"" + end +"\"");
				System.out.println("start = " +start
						   +", length = " +length
						   +", char before start = "
						   +template[start-1]);
				*/
				while (index < start + length) {
				    if (template[index] == '$' &&
					index+1 < start + length &&
					template[index + 1] == '$') {
					index += 2;
				    } else if (template[index] == '$' &&
					       index+1 < start + length &&
					       template[index + 1]
					       == openDelim) {
					boolean test = true;
					for (int i = 0; i < mark2-mark1; i++) {
					    if (index+2+i >= start + length) {
						// the delimiter is missing!
						int lineno = 1;
						for (int k = 0;
						     k < mark1; k++) {
						    if (template[k] == '\n')
							lineno++;
						}
						int thisline = lineno;
						for (int k = mark1; k <= index;
						     k++) {
						    if (template[k] == '\n')
							thisline++;
						}
						String et = String.valueOf
						    (template, mark1,
						     (mark2 - mark1));
						throw new IOException
			    (errorMsg("delimNest",thisline,token,et,lineno));
						// test = false;
						// break;
					    }
					    if (template[index+2+i] !=
						template[mark1 + i]) {
						test = false;
						// System.out.println("!pattern");
						break;
					    }
					}
					if (test && 
					    template[index+2+(mark2-mark1)]
					    != closeDelim) {
					    test = false;
					}
					if (test) {
					    endDirectiveFound = true;
					    /*
					    System.out.println
						("test succeeded");
					    */
					    Object sequence =
						replacements(token);
					    if (sequence instanceof
						KeyMapIterable) {
						for (KeyMap tbl:
							 (KeyMapIterable)
							 sequence) {
						    tables.addFirst(tbl);
						    /*
						      System.out.println
						      ("start recursion");
						    */
						    processTemplate
							(template,
							 start,
							 (index-start),
							 writer);
						    /*
						      System.out.println
						      ("end recursion");
						    */
						    tables.removeFirst();
						}
					    } else if(sequence instanceof
						      KeyMap[]) {
						for (KeyMap tbl: (KeyMap[])
							 sequence) {
						    tables.addFirst(tbl);
						    /*
						      System.out.println
						      ("start recursion");
						    */
						    processTemplate
							(template,
							 start,
							 (index-start),
							 writer);
						    /*
						      System.out.println
						      ("end recursion");
						    */
						    tables.removeFirst();
						}
					    } else if (sequence instanceof
						       KeyMap) {
						KeyMap tbl = (KeyMap)sequence;
						tables.addFirst(tbl);
						processTemplate(template,
								start,
								(index-start),
								writer);
						tables.removeFirst();
					    }
					    length -= (index - start);
					    length -= 3 + (mark2-mark1);
					    start = index + 3 + (mark2-mark1);
					    index = start;
					    if ((start-origstart +length) 
						!= origlen) {
						throw new 
						    RuntimeException
						    ((start-origstart+length)
						     + " != " + origlen);
					    }
					    break;
					} else {
					    index++;
					    // System.out.println("test failed");
					}
				    } else {
					index++;
				    }
				}
				if (!endDirectiveFound
				    && index >= start+length) {
				    // the delimiter is missing!
				    int lineno = 1;
				    for (int k = 0; k < mark1; k++) {
					if (template[k] == '\n') lineno++;
				    }
				    int thisline = lineno;
				    if (index > start+length) {
					index = start+length;
				    }
				    for (int k = mark1; k < index; k++) {
					if (template[k] == '\n')
					    thisline++;
				    }
				    if(index > 0 && template[index-1] == '\n') {
					thisline--;
				    }
				    String et = String.valueOf
					(template, mark1, (mark2 - mark1));
				    throw new IOException
			     (errorMsg("delimNest",thisline,token,et,lineno));
				}
			    } else {
				int lineno = 1;
				for (int k = 0; k < index; k++) {
				    if (template[k] == '\n') {
					lineno++;
				    }
				}
				String msg =
				    errorMsg("closingDelim",lineno,token);
				throw new IOException(msg);
			    }
			} else if (template[index] == closeDelim) {
			    /*
			    System.out.println(token +" --> "
					       +replacement(token));
			    */
			    if (token != null && token.length() > 0) {
				writer.write(replacement(token));
			    }
			    length -= (1 + index - start);
			    start = index + 1;
			    index = start;
			    /*
			    System.out.println("start = " +start +
					       ", length = " + length);
			    */
			    if ((start-origstart +length) != origlen) {
				throw new RuntimeException
				    ((start-origstart + length) + " != " + origlen);
			    }
			} else {
			    // not terminated properly, so skip to closing
			    // delimiter.
			    int lineno = 1;
			    for (int k = 0; k < index; k++) {
				if (template[k] == '\n') {
				    lineno++;
				}
			    }
			    String msg =
				errorMsg("closingDelim", lineno, token);
			    throw new IOException(msg);
			    /*
			    while (template[index] != closeDelim) {
				index++;
			    }
			    length -= (1 + index - start);
			    start = index + 1;
			    index = start;
			    */
			}
		    }
		}
	    }
	}
	if (length > 0) {
	    // System.out.println(start +" " + length);
	    writer.write(template, start, length);
	    // System.out.println("\"" + new String(template, start, length) + "\"");
	}
    }

    /**
     * Process a template.
     * @param reader the Reader used to read a template
     * @param writer the Writer used to output the replacement text
     * @exception IOException an IO error occurred during processing
     */
    public void processTemplate(Reader reader,
				Writer writer) 
	throws IOException
    {
	processTemplate(reader, 1024, writer);
    }


    /**
     * Process a template given a size hint for buffering.
     * @param reader the Reader used to read a template
     * @param sizeHint the buffer space to allocate for copying.
     * @param writer the Writer used to output the replacement text
     * @exception IOException an IO error occurred during processing
     */
    public void processTemplate(Reader reader,
				int sizeHint,
				Writer writer) 
	throws IOException
    {
	if (reader == null || writer == null) {
	    throw new NullPointerException(errorMsg("nullArgument"));
	}
	CharArrayWriter caw = new CharArrayWriter(sizeHint);
	char[] cbuf = new char[sizeHint];
	int len;
	while ((len = reader.read(cbuf, 0, cbuf.length)) != -1) {
	    caw.write(cbuf, 0, len);
	}
	char[] template = caw.toCharArray();
	try {
	    boolean isEmpty = origTables.isEmpty();
	    tables = isEmpty? new LinkedList<KeyMap>():
		new LinkedList<KeyMap>(origTables);
	    root.set(isEmpty? null: tables.getLast());
	    processTemplate(template, 0, template.length, writer);
	    writer.flush();
	} finally {
	    tables = null;
	    root.set(null);
	}
    }

    /**
     * Process a template given a reader, specifying the output as a file.
     * @param reader the Reader used to read a template
     * @param encoding the character encoding for the output file
     * @param outputFile a file in which the output is stored
     * @exception IOException an IO error occurred during processing
     */
    public void processTemplate(Reader reader, String encoding, File outputFile)
	throws IOException
    {
	OutputStream os = new FileOutputStream(outputFile);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(reader, wr);
    }

    /**
     * Process a template given a reader and output stream.
     * @param reader the Reader used to read a template
     * @param encoding the character encoding for the output stream
     * @param os the output stream
     * @exception IOException an IO error occurred during processing
     */
    public void processTemplate(Reader reader, String encoding, OutputStream os)
	throws IOException
    {
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(reader, wr);
    }

    /**
     * Process a template stored as a system resource, specifying the output
     * as a File.
     * This can be problematic with Java modules unless the module
     * containing the resource is an open module.
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param outputFile a file in which the output is stored
     * @exception IOException an IO error occurred during processing
     */
    public void processSystemResource(String resource,
				      String encoding,
				      File outputFile)
	throws IOException
    {
	InputStream is =  null;
	try {
	    is = ClassLoader.getSystemResourceAsStream(resource);
	} catch (Exception e) {}
	if (is == null) {
	    if (!resource.startsWith("/")) {
		resource = "/" + resource;
	    }
	    is = TemplateProcessor.class.getResourceAsStream(resource);
	}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStream os = new FileOutputStream(outputFile);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
	wr.close();

    }

    /**
     * Process a template stored as a system resource.
     * This can be problematic with Java modules unless the module
     * containing the resource is an open module.
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param writer the Writer used to output the replacement text
     * @exception IOException an IO error occurred during processing
     */
    public void processSystemResource(String resource,
				      String encoding,
				      Writer writer)
	throws IOException
    {
	InputStream is = null;
	try {
	    is = ClassLoader.getSystemResourceAsStream(resource);
	} catch (Exception e) {}
	if (is == null) {
	    if (!resource.startsWith("/")) {
		resource = "/" + resource;
	    }
	    is = TemplateProcessor.class.getResourceAsStream(resource);
	}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	processTemplate(rd,writer);
	is.close();
    }

    /**
     * Process a template stored as a system resource, specifying the output
     * as an output stream.
     * This can be problematic with Java modules unless the module
     * containing the resource is an open module.
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param os an output stream for the processed template
     * @exception IOException an IO error occurred during processing
     */
    public void processSystemResource(String resource, String encoding,
				      OutputStream os) 
	throws IOException
    {
	InputStream is = null;
	try {
	    is = ClassLoader.getSystemResourceAsStream (resource);
	} catch (Exception e) {}
	if (is == null) {
	    if (!resource.startsWith("/")) {
		resource = "/" + resource;
	    }
	    is = TemplateProcessor.class.getResourceAsStream(resource);
	}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
    }

    /**
     * Process a template stored as a system resource, specifying the output
     * as a File and provided a class in the same package as the resource.
     * Typically, the first argument will be <CODE>this.getClass()</CODE>
     * or just <CODE>getClass()</CODE>.  The first argument is needed
     * in most cases due to the restrictions modules place on the visibility
     * of resources.
     * @param clasz a class in the same package as the resource
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param outputFile a file in which the output is stored
     * @exception IOException an IO error occurred during processing
     */
public void processSystemResource(Class clasz,
				  String resource,
				  String encoding,
				  File outputFile)
	throws IOException
    {
	if (!resource.startsWith("/")) {
	    resource = "/" + resource;
	}
	InputStream is = null;
	try {
	    is = clasz.getResourceAsStream(resource);
	} catch (Exception e) {}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStream os = new FileOutputStream(outputFile);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
	wr.close();
    }

    /**
     * Process a template stored as a system resource and provided a
     * class in the same package as the resource.
     * Typically, the first argument will be <CODE>this.getClass()</CODE>
     * or just <CODE>getClass()</CODE>.  The first argument is needed
     * in most cases due to the restrictions modules place on the visibility
     * of resources.
     * @param clasz a class in the same package as the resource
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param writer the Writer used to output the replacement text
     * @exception IOException an IO error occurred during processing
     */
    public void processSystemResource(Class clasz,
				      String resource,
				      String encoding,
				      Writer writer)
	throws IOException
    {
	if (!resource.startsWith("/")) {
	    resource = "/" + resource;
	}
	InputStream is = null;
	try {
	    is = clasz.getResourceAsStream(resource);
	} catch (Exception e) {}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	processTemplate(rd,writer);
	is.close();
    }

    /**
     * Process a template stored as a system resource, specifying the
     * output as an output stream and provided a class in the same
     * package as the resource.
     * Typically, the first argument will be <CODE>this.getClass()</CODE>
     * or just <CODE>getClass()</CODE>.  The first argument is needed
     * in most cases due to the restrictions modules place on the visibility
     * of resources.
     * @param clasz a class in the same package as the resource
     * @param resource the name of the resource containing the template
     * @param encoding the character encoding for the resource
     * @param os an output stream for the processed template
     * @exception IOException an IO error occurred during processing
     */
    public void processSystemResource(Class clasz,
				      String resource,
				      String encoding,
				      OutputStream os)
	throws IOException
    {
	if (!resource.startsWith("/")) {
	    resource = "/" + resource;
	}
	InputStream is = null;
	try {
	    is = clasz.getResourceAsStream(resource);
	} catch (Exception e) {}
	if (is == null) {
	    throw new IOException(errorMsg("missingResource", resource));
	}
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
    }


    /**
     * Process a template located via a URL.
     * @param url the url locating the template
     * @param encoding the character encoding for the resource
     * @param writer the Writer used to create the output
     * @exception IOException an IO error occurred during processing
     */
    public void processURL(URL url, String encoding, Writer writer)
	throws IOException
    {
	InputStream is = url.openStream();
	InputStreamReader rd = new InputStreamReader(is, encoding);
	processTemplate(rd,writer);
	is.close();
    }

    /**
     * Process a template located via a URL, specifying the output
     * as a File.
     * @param url the url locating the template
     * @param encoding the character encoding for the resource
     * @param outputFile a file in which the output is stored
     * @exception IOException an IO error occurred during processing
     */
    public void processURL(URL url, String encoding, File outputFile) 
	throws IOException
    {
	InputStream is = url.openStream();
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStream os = new FileOutputStream(outputFile);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
	wr.close();

    }

    /**
     * Process a template located via a URL, specifying the output
     * as an output stream.
     * @param url the url locating the template
     * @param encoding the character encoding for the resource
     * @param os an output stream for the processed template
     * @exception IOException an IO error occurred during processing
     */
    public void processURL(URL url, String encoding, OutputStream os) 
	throws IOException
    {
	InputStream is = url.openStream();
	InputStreamReader rd = new InputStreamReader(is, encoding);
	OutputStreamWriter wr = new OutputStreamWriter(os, encoding);
	processTemplate(rd,wr);
	is.close();
    }
}

//  LocalWords:  exbundle keymaps ul li subdirectives subdirective lt
//  LocalWords:  KeyMap TemplateProcessor KeyMapIterable KeyMapList
//  LocalWords:  HashMap initialCapacity loadFactor LinkedList delim
//  LocalWords:  badDelimiter tbls badReplacement getClass openDelim
//  LocalWords:  IOException sizeHint nullArgument os url toString
//  LocalWords:  outputFile keyNotString negposkey hasList hasntList
//  LocalWords:  keymapList UTF charset csn keymap subtable delimNest
//  LocalWords:  openingDelim closingDelim closeDelim
