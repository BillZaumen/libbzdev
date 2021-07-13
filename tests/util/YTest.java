import org.bzdev.util.*;

import org.bzdev.io.AppendableWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;

public class YTest {

    public static void main(String argv[]) throws Exception {


	try {
	    System.out.println("parsing an empty Yaml file");
	    String s = "%YAML 1.2\n---\n# Nothing to do!\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    System.out.println("object = " + object);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	try {
	    System.out.println("parsing an empty Yaml file");
	    String s = "%YAML 1.2\n---\n\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    System.out.println("object = " + object);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("parsing a  Yaml file containing --");
	    String s = "%YAML 1.2\n---\n--\n--\n\n--\n\n\n--\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    System.out.println("object = " + object);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	try {
	    System.out.println("parsing a  Yaml file with a string starting"
		+ " with '...'");
	    String s = "%YAML 1.2\n---\n...a\n...b\n\n...c\n"
		+ "\n\n...d\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof String) {
		String s2 = (String) object;
		System.out.println("object = \"" + s2 + "\"");
	    } else {
		System.out.println("object = " + object);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);

	}

	try {
	    System.out.println("[1] YAML with an object starting at col 0");
	    String s = "%YAML 1.2\n---\nf: hello\ns: goodbye\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof JSObject) {
		System.out.println("object = " + object);
		JSObject obj = (JSObject) object;
		for (Map.Entry<String,Object> entry: obj.entrySet()) {
		    System.out.println(entry.getKey() + ": "
				       + entry.getValue());
		}
	    } else {
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("YAML with an object containing a URL");
	    String s =
		"%YAML 1.2\n---\nurl: http://www.xyz.com/index.html\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof JSObject) {
		System.out.println("object = " + object);
		JSObject obj = (JSObject) object;
		if (obj.size() != 1) {
		    throw new Exception("size");
		}
		for (Map.Entry<String,Object> entry: obj.entrySet()) {
		    System.out.println(entry.getKey() + ": "
				       + entry.getValue());
		}
	    } else {
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("YAML with a key-like string starting at col 0");
	    String s = "%YAML 1.2\n---\nf:hello\nxxxx\n\n ...\n ...\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof String) {
		String s2 = (String)object;
		System.out.println("object = \"" + s2 + "\"");
	    } else {
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("[2] YAML with an object starting at col 0");
	    String s = "%YAML 1.2\n---\nf: hello\nt:\n- a\n- b\ns: goodbye\n"
		+ "...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof JSObject) {
		JSObject obj = (JSObject) object;
		for (Map.Entry<String,Object> entry: obj.entrySet()) {
		    if (entry.getValue() instanceof JSArray) {
			JSArray array = (JSArray) entry.getValue();
			String key = entry.getKey();
			if (key == null) key = "[null key]";
			System.out.println(key + ":");
			for (Object o: array) {
			    System.out.println("- " + o);
			}
		    } else {
			String key = entry.getKey();
			if (key == null) key = "[null key]";
			System.out.println(key + ": " + entry.getValue());
		    }
		}
	    } else {
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("[1] YAML with a string starting at col 0");
	    String s = "%YAML 1.2\n---\nabcd\ndefg\n\ng\n\n\n---h\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof String) {
		String s2 = (String) object;
		System.out.println("object = \"" + s2 + "\"");
	    } else {
		System.out.println("object = " + object);
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    System.out.println("[2] YAML with a string starting at col 0");
	    String s = "%YAML 1.2\n---\nabcd\ndefg\n\ng\n\n\n---h\n...\n";
	    Object object = JSUtilities.YAML.parse(s);
	    if (object instanceof String) {
		String s2 = (String) object;
		System.out.println("object = \"" + s2 + "\"");
	    } else {
		System.out.println("object = " + object);
		throw new Exception();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	try {
	    System.out.println("parsing YTest11.yaml");
	    Reader r = new FileReader("YTest11.yaml", Charset.forName("UTF-8"));
	    Object object = JSUtilities.YAML.parse(r);
	    if (object instanceof JSArray) {
		JSArray array = (JSArray) object;
		for (Object obj: array) {
		    if (obj instanceof JSObject) {
			JSObject o = (JSObject) obj;
			for(Map.Entry<String,Object> entry: o.entrySet()) {
			    System.out.println("found property "
					       + entry.getKey()
					       + ", value type = "
					       + entry.getValue().getClass());
			    if (entry.getValue() instanceof JSArray) {
				JSArray a = (JSArray) entry.getValue();
				for (Object s: a) {
				    if (s instanceof String) {
					System.out.println("    - " + s);
				    } else {
					throw new Exception("String expected");
				    }
				}
			    } else {
				throw new Exception("JSArray expected");
			    }
			}
		    } else {
			throw new Exception("JSObject expected");
		    }
		}
	    } else {
		throw new Exception("JSArray expected");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	ExpressionParser ep = new ExpressionParser(Math.class);

	try {
	    ObjectParser.SourceParser sp2 =
		new ObjectParser.SourceParser(ep);

	    Reader rr = new FileReader("YTest10.yaml",
				       Charset.forName("UTF-8"));
	    System.out.println("parsing YTest10.yaml");
	    JSUtilities.YAML.Parser yparser2 = new JSUtilities.YAML
		.Parser(rr,
			new JSUtilities.YAML.TagSpec("!bzdev!",
						     "tag:bzdev.org,2021:esp",
						     sp2));
	    Object results2 = yparser2.getResults();
	    if (results2 instanceof JSObject) {
		for (Map.Entry<String,Object> entry:
			 ((JSObject)results2).entrySet()) {
		    String key = entry.getKey();
		    Object val = entry.getValue();
		    if (val instanceof String) {
			System.out.println(key +": " + val);
		    } else if (val instanceof JSObject) {
			System.out.println(key + ": <JSObject, size = "
					   + ((JSObject) val).size()
					   + ">");
		    } else if (val instanceof JSArray) {
			if (((JSArray) val).size() == 0) {
			    System.out.println(key + ": <empty JSArray>");
			} else {
			    System.out.println(key + ":");
			    for (Object e: (JSArray)val) {
				if (e instanceof JSArray) {
				    System.out.println("    JSArray, size = "
						       + ((JSArray) e).size());
				} else if (e instanceof JSObject) {
				    JSObject ee = (JSObject) e;
				    System.out.println("    -");
				    for (Map.Entry<String,Object> ent:
					     ee.entrySet()) {
					System.out.print("\t" + ent.getKey()
							 + ": ");
					Object v = ent.getValue();
					if (v instanceof JSArray) {
					    System.out.println("<JSArray, size "
							       + " = "
							       + ((JSArray)v)
							       .size());
					} else {
					    System.out.println(v);
					}
				    }
				} else {
				    System.out.println("    " + e);
				}
			    }
			}
		    }
		}
	    }
	    System.out.println("YTest10.yaml complete");
	} catch (Exception e) {
	    // System.out.print(e.getMessage());
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    ObjectParser.SourceParser sp2 =
		new ObjectParser.SourceParser(ep);

	    Reader rr = new FileReader("YTest7.yaml", Charset.forName("UTF-8"));
	    JSUtilities.YAML.Parser yparser2 = new JSUtilities.YAML
		.Parser(rr,
			new JSUtilities.YAML.TagSpec("!bzdev!",
						     "tag:bzdev.org,2021:esp",
						     sp2));
	    Object results2 = yparser2.getResults();
	    System.out.println("NO EXCEPTION");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.print(e.getMessage());
	    System.out.println(" ... EXCEPTION EXPECTED");
	}

	try {
	    ObjectParser.SourceParser sp2 =
		new ObjectParser.SourceParser(ep);

	    Reader rr = new FileReader("YTest8.yaml", Charset.forName("UTF-8"));
	    JSUtilities.YAML.Parser yparser2 = new JSUtilities.YAML
		.Parser(rr,
			new JSUtilities.YAML.TagSpec("!bzdev!",
						     "tag:bzdev.org,2021:esp",
						     sp2));
	    Object results2 = yparser2.getResults();
	    System.out.println("NO EXCEPTION");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.print(e.getMessage());
	    System.out.println(" ... EXCEPTION EXPECTED");
	}

	try {
	    ObjectParser.SourceParser sp2 =
		new ObjectParser.SourceParser(ep);

	    Reader rr = new FileReader("YTest9.yaml", Charset.forName("UTF-8"));
	    JSUtilities.YAML.Parser yparser2 = new JSUtilities.YAML
		.Parser(rr,
			new JSUtilities.YAML.TagSpec("!bzdev!",
						     "tag:bzdev.org,2021:esp",
						     sp2));
	    Object results2 = yparser2.getResults();
	    System.out.println("NO EXCEPTION");
	    System.exit(1);
	} catch (Exception e) {
	    System.out.print(e.getMessage());
	    System.out.println(" ... EXCEPTION EXPECTED");
	}


	LinkedHashMap<String,String> map = new LinkedHashMap<>();
	map.put("\"hello\"", "hello");
	map.put("\"\\\\hello\\\\\"", "\\hello\\");
	map.put("\"\\\"hello\\\"\"", "\"hello\"");
	map.put("\"u-umlaut = \\u00fc\"", "u-umlaut = \u00fc");
	map.put("'First test\nfor single quotes:\n\n***\"''\" should be "
		+ "a single quote'",
		"First test for single quotes:\n***\"'\" should be "
		+ "a single quote");
	map.put("\"YAML-specific\ndouble-quote test:\n\ncheck new lines\"",
		"YAML-specific double-quote test:\ncheck new lines");
	map.put("|\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n");
	map.put("|-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye");
	map.put("|+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n\n");
	map.put("|\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\n\nGoodbye\n");
	map.put("|-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye");
	map.put("|+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye\n\n");
	map.put(">\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n");
	map.put(">-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye");
	map.put(">+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n\n");
	map.put(">\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\nGoodbye\n");
	map.put(">-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye");
	map.put(">+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye\n\n");
	map.put(">\n  Hello\n    there\n    there\n\n  Goodbye\n  Goodbye\n\n",
		"Hello\n  there\n  there\nGoodbye Goodbye\n");
	map.put(">-\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye");
	map.put(">+\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye\n\n");
	map.put("|2\n   Hi there\n\n", "  Hi there\n");
	map.put("|+2\n   Hi there\n\n", "  Hi there\n\n");
	map.put("|-2\n   Hi there\n\n", "  Hi there");
	map.put(">2\n Hi\n there\n\n", "Hi there\n");
	map.put(">+2\n Hi\n there\n\n", "Hi there\n\n");
	map.put(">-2\n Hi\n there\n\n", "Hi there");

	// add a leading '---' on the same line
	map.put("---\"hello\"", "hello");
	map.put("---\"\\\\hello\\\\\"", "\\hello\\");
	map.put("---\"\\\"hello\\\"\"", "\"hello\"");
	map.put("---\"u-umlaut = \\u00fc\"", "u-umlaut = \u00fc");
	map.put("---'First test\nfor single quotes:\n\n***\"''\" should be "
		+ "a single quote'",
		"First test for single quotes:\n***\"'\" should be "
		+ "a single quote");
	map.put("---\"YAML-specific\ndouble-quote test:\n\ncheck new lines\"",
		"YAML-specific double-quote test:\ncheck new lines");
	map.put("---|\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n");
	map.put("---|-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye");
	map.put("---|+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n\n");
	map.put("---|\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\n\nGoodbye\n");
	map.put("---|-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye");
	map.put("---|+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye\n\n");
	map.put("--->\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n");
	map.put("--->-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye");
	map.put("--->+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n\n");
	map.put("--->\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\nGoodbye\n");
	map.put("--->-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye");
	map.put("--->+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye\n\n");
	map.put
	    ("--->\n  Hello\n    there\n    there\n\n  Goodbye\n  Goodbye\n\n",
	     "Hello\n  there\n  there\nGoodbye Goodbye\n");
	map.put("--->-\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye");
	map.put("--->+\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye\n\n");
	map.put("---|2\n   Hi there\n\n", "  Hi there\n");
	map.put("---|+2\n   Hi there\n\n", "  Hi there\n\n");
	map.put("---|-2\n   Hi there\n\n", "  Hi there");
	map.put("--->2\n Hi\n there\n\n", "Hi there\n");
	map.put("--->+2\n Hi\n there\n\n", "Hi there\n\n");
	map.put("--->-2\n Hi\n there\n\n", "Hi there");

	map.put(" \"hello\"", "hello");
	map.put(" \"\\\\hello\\\\\"", "\\hello\\");
	map.put(" \"\\\"hello\\\"\"", "\"hello\"");
	map.put(" \"u-umlaut = \\u00fc\"", "u-umlaut = \u00fc");
	map.put(" 'First test\nfor single quotes:\n\n***\"''\" should be "
		+ "a single quote'",
		"First test for single quotes:\n***\"'\" should be "
		+ "a single quote");
	map.put(" \"YAML-specific\ndouble-quote test:\n\ncheck new lines\"",
		"YAML-specific double-quote test:\ncheck new lines");
	map.put(" |\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n");
	map.put(" |-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye");
	map.put(" |+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello\nthere\n\nGoodbye\n\n");
	map.put(" |\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\n\nGoodbye\n");
	map.put(" |-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye");
	map.put(" |+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\n\nGoodbye\n\n");
	map.put(" >\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n");
	map.put(" >-\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye");
	map.put(" >+\n  Hello\n  there\n\n  Goodbye\n\n",
		"Hello there\nGoodbye\n\n");
	map.put(" >\n  Hello\n    there\n\n  Goodbye\n\n",
		"Hello\n  there\nGoodbye\n");
	map.put(" >-\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye");
	map.put(" >+\n  Hello\n   there\n\n  Goodbye\n\n",
		"Hello\n there\nGoodbye\n\n");
	map.put(" >\n  Hello\n    there\n    there\n\n  Goodbye\n  Goodbye\n\n",
		"Hello\n  there\n  there\nGoodbye Goodbye\n");
	map.put(" >-\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye");
	map.put(" >+\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n",
		"Hello\n   there\n   there\nGoodbye Goodbye\n\n");
	map.put(" |2\n    Hi there\n\n", "  Hi there\n");
	map.put(" |+2\n    Hi there\n\n", "  Hi there\n\n");
	map.put(" |-2\n    Hi there\n\n", "  Hi there");
	map.put(" >2\n   Hi\n  there\n\n", " Hi there\n");
	map.put(" >+2\n   Hi\n  there\n\n", " Hi there\n\n");
	map.put(" >-2\n   Hi\n  there\n\n", " Hi there");

	map.put("-hello", "-hello");
	map.put("-hello # with a comment\n", "-hello");

	String s1 = "  list:\n    - sublist:\n"
	    + "      - a\n      - b\n      - c\n  foo: bar\n";
	System.out.println("testing s1");
	System.out.println(s1);
	JSUtilities.YAML.parse(s1);


	String pt2 = "%YAML 1.2\n---\n  tracks:\n    - tracknumber: 4\n"
	    + "      title: \"IV\"\n...\n";
	JSUtilities.YAML.parse(pt2);



	// String pt1 = "|+2\n   Hi there\n\n";
	// String pte1 = "  Hi there\n\n";

	String pt1 = " >+\n Hello\n    there\n    there\n\n Goodbye\n Goodbye\n\n";
	String pte1 =  "Hello\n   there\n   there\nGoodbye Goodbye\n\n";


	String res = (String)JSUtilities.YAML.parse(pt1);
	if (!res.equals(pte1)) {
	    System.out.println("RESULT:");
	    System.out.println(res);
	    System.out.println("EXPECTING:");
	    System.out.println(pte1);
	    System.exit(1);
	}


       String y0 = "- define:\n"
	   + "    - {withPrefix: \"fontParams\", config: [\n"
	   + "       {name: \"SANS_SARIF\"},\n"
	   + "       {withPrefix: \"color\",\n"
	   + "        red: 212, blue: 44, green:174}]}\n";
       System.out.println(y0);
       JSUtilities.YAML.parse(y0);


	/*
	System.out.println("testing y0");
	String y0 = "%YAML 1.2\n---\n  foo1: hello\n  foo2: goodbye\n"
	    + "  list:\n    - bar: 10\n      foo: Foo\n...\n";
	System.out.println(y0);
	JSUtilities.YAML.parse(y0);
	*/

	System.out.println("testing y1");
	String y1 = "%YAML 1.2\n---\n  foo1: hello\n  foo2: goodbye\n"
	    + "  list:\n\n    - bar: 10\n      foo: Foo\n...\n";

	JSUtilities.YAML.parse(y1);

	String s2 = "  bar: hello\n  list2:\n    - bar1: bar1\n    - sublist:\n"
	    +"      - a\n      - b\n  foobar: last line\n";
	System.out.println(s2);
	JSObject testobj = (JSObject) JSUtilities.YAML.parse(s2);
	System.out.println("testobj.get(foobar) = " + testobj.get("foobar"));

	try {
	    JSUtilities.YAML.parse(" >2\n Hi\n  there!\n\n");
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    System.out.println("[exception was expected]");
	}

	map.put("-10", "-10");
	System.out.println("testing parse(\"-10\")");

	if (!(JSUtilities.YAML.parse("-10") instanceof Integer)) {
	    System.out.println(JSUtilities.YAML.parse("-10").getClass());
	    throw new Exception("... did not produce an Integer for -10");
	}

	map.put("-1.25e-30", "-1.25e-30");
	if (!(JSUtilities.YAML.parse("-1.25e-20") instanceof Double)) {
	    System.out.println(JSUtilities.YAML.parse("-10").getClass());
	    throw new Exception("... did not produce an Integer for -10");
	}

	map.put("-1000000000000", "-1000000000000");
	if (!(JSUtilities.YAML.parse("-1000000000000") instanceof Long)) {
	    System.out.println(JSUtilities.YAML.parse("-1000000000000")
			       .getClass());
	    throw new Exception("... did not produce an Long for "
				+ "-1000000000000");
	}

	map.put("-1000000000000.0", "-1000000000000");
	if (!(JSUtilities.YAML.parse("-1000000000000") instanceof Long)) {
	    System.out.println(JSUtilities.YAML.parse("-1000000000000")
			       .getClass());
	    throw new Exception("... did not produce an Long for "
				+ "-1000000000000.0");
	}

	// Writer w = new AppendableWriter(System.out);
	int index = 1;
	for (Map.Entry<String,String> entry: map.entrySet()) {
	    String yaml = entry.getKey();
	    if (index == 87) System.out.println("**** yaml = " + yaml);
	    String expecting = entry.getValue();
	    System.out.println("**** CASE " + (index++));
	    Object object = null;
	    try {
		object = JSUtilities.YAML.parse(yaml);
	    } catch (Exception e) {
		System.out.println("... was processing " + yaml);
		throw e;
	    }
	    if (object instanceof String) {
		String s = (String) object;
		if (!s.equals(expecting)) {
		    System.out.println("YAML: " +yaml);
		    System.out.println("Value: <" + s + ">");
		    throw new Exception("error parsing " + yaml);
		}
	    } else if (object instanceof Integer) {
		Integer value = (Integer) object;
		Integer expected = Integer.valueOf(expecting);
		if (!value.equals(expected)) {
		    System.out.println("YAML: " +yaml);
		    System.out.println("value: " + value);
		    System.out.println("expected: " + expected);
		    throw new Exception("value did not match");
		}
	    } else if (object instanceof Long) {
		Long value = (Long) object;
		Long expected = Long.valueOf(expecting);
		if (!value.equals(expected)) {
		    System.out.println("YAML: " +yaml);
		    System.out.println("value: " + value);
		    System.out.println("expected: " + expected);
		    throw new Exception("value did not match");
		}
	    } else if (object instanceof Double) {
		Double value = (Double) object;
		Double expected = Double.valueOf(expecting);
		if (!value.equals(expected)) {
		    System.out.println("YAML: " +yaml);
		    System.out.println("value: " + value);
		    System.out.println("expected: " + expected);
		    throw new Exception("value did not match");
		}
	    } else if (object instanceof Boolean) {
		Boolean value = (Boolean) object;
		Boolean expected = Boolean.valueOf(expecting);
		if (!value.equals(expected)) {
		    System.out.println("YAML: " +yaml);
		    System.out.println("value: " + value);
		    System.out.println("expected: " + expected);
		    throw new Exception("value did not match");
		}
	    } else {
		System.out.println("YAML:" + yaml);
		System.out.println("unknown type " + object.getClass());
		throw new Exception("error parsing " + yaml);
		    
	    }
	    /*
	    JSUtilities.JSON.writeTo(w, object);
	    w.flush();
	    System.out.println();
	    */
	}
	System.out.println("... testing array 1");
	String atest1 = "- 'hello'\n- 'goodbye'\n";
	JSArray array = (JSArray) JSUtilities.YAML.parse(atest1);
	System.out.println("array.size() = " + array.size());
	for (Object obj: array) {
	    System.out.println("... " + obj);
	}

	System.out.println("... testing array 2");
	String atest2 = "-\n  - 'hello'\n  - 'goodbye'\n"
	    + "-\n  - 'Hello'\n  - 'Goodbye'\n";
	array = (JSArray) JSUtilities.YAML.parse(atest2);
	for (Object obj: array) {
	    if (obj instanceof String) {
		System.out.println("... " + obj);
	    } else if (obj instanceof JSArray) {
		for (Object obj2: (JSArray)obj) {
		    System.out.println("    ... " + obj2);
		}
	    }
	}

	System.out.println("... testing array 3");
	String atest3 = "---\n-\n  - 'hello'\n  - 'goodbye'\n"
	    + "-\n  - 'Hello'\n  - 'Goodbye'\n";
	array = (JSArray) JSUtilities.YAML.parse(atest3);
	for (Object obj: array) {
	    if (obj instanceof String) {
		System.out.println("... " + obj);
	    } else if (obj instanceof JSArray) {
		for (Object obj2: (JSArray)obj) {
		    System.out.println("    ... " + obj2);
		}
	    }
	}
	
	System.out.println("... testing array 4");
	String atest4 = "----\n     - 'hello'\n     - 'goodbye'\n"
	    + "   -\n     - 'Hello'\n     - 'Goodbye'\n";
	array = (JSArray) JSUtilities.YAML.parse(atest4);
	for (Object obj: array) {
	    if (obj instanceof String) {
		System.out.println("... " + obj);
	    } else if (obj instanceof JSArray) {
		for (Object obj2: (JSArray)obj) {
		    System.out.println("    ... " + obj2);
		}
	    }
	}


	System.out.println("... testing object 1");
	String otest1 = "foo: 10\nbar: 20";
	JSObject object = (JSObject) JSUtilities.YAML.parse(otest1);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    System.out.println(entry.getKey() +": " + entry.getValue());
	}


	System.out.println("... testing object 2");
	String otest2 = "foo: bar: 20";
	try {
	    JSUtilities.YAML.parse(otest2);
	    throw new Exception();
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	    System.out.println("[exception expected]");
	} catch (Exception e) {
	    System.out.println("exception test failed");
	}

	System.out.println("... testing object 3");
	String otest3 = "foo: - 20";
	try {
	    JSUtilities.YAML.parse(otest3);
	    throw new Exception();
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	    System.out.println("[exception expected]");
	} catch (Exception e) {
	    System.out.println("exception test failed");
	}

	System.out.println("... testing object 4");
	String otest4 = "foo:\n  foo1: 10\n  foo2: 20\nbar: '30'\n"
	    + "foobar: last item";
	object = (JSObject) JSUtilities.YAML.parse(otest4);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSObject) {
		System.out.println("... " + key + ":");
		for (Map.Entry<String,Object>entry1:
			 ((JSObject)value).entrySet()) {
		    String key1 = entry1.getKey();
		    Object value1 = entry1.getValue(); 
		    System.out.println("  ... " + key1 + ": " + value1);
		}
	    } else {
		System.out.println("... " + key + ": " + value);
	    }
	}

	System.out.println("... testing object 5");
	String otest5 = "foo:\n  foo1: 10\n  foo2: 20\nbar:\n"
	    + "  bar1: 30\n  bar2: 40\n";
	object = (JSObject) JSUtilities.YAML.parse(otest5);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSObject) {
		System.out.println("... " + key + ":");
		for (Map.Entry<String,Object>entry1:
			 ((JSObject)value).entrySet()) {
		    String key1 = entry1.getKey();
		    Object value1 = entry1.getValue(); 
		    System.out.println("  ... " + key1 + ": " + value1);
		}
	    } else {
		System.out.println("... " + key + ": " + value);
	    }
	}

	System.out.println("... testing object 6");
	String otest6 = "foo:\n  - 10\n  - 20\nbar:\n"
	    + "  - 30\n   0 40\nfoobar: 50\n";
	object = (JSObject) JSUtilities.YAML.parse(otest6);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSArray) {
		System.out.println("... " + key + ":");
		for (Object value1: ((JSArray)value)) {
		    System.out.println("  ... " + value1);
		}
	    } else {
		System.out.println("... " + key + ": " + value);
	    }
	}

	System.out.println("... testing object 7");
	String otest7 = "foo:\n  - 10\n  - 20\nbar:\n"
	    + "  - 30 # hi\n\n   0 40 # there\nfoobar: 50\n";
	object = (JSObject) JSUtilities.YAML.parse(otest7);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSArray) {
		System.out.println("... " + key + ":");
		for (Object value1: ((JSArray)value)) {
		    System.out.println("  ... " + value1);
		}
	    } else {
		System.out.println("... " + key + ": " + value);
	    }
	}

	System.out.println("... testing object 8");
	String otest8 = "foo:\n  - 10\n  - 20\nbar:\n"
	    + "  - 30 # hi\n\n  - 40 # there\nfoobar: 50\n";
	object = (JSObject) JSUtilities.YAML.parse(otest8);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSArray) {
		System.out.println("... " + key + ":");
		for (Object value1: ((JSArray)value)) {
		    System.out.println("  ... " + value1);
		}
	    } else {
		System.out.println("... " + key + ": " + value);
	    }
	}

	System.out.println();
	System.out.println();
	System.out.println();

	System.out.println("... testing object 9");
	String otest9 = "- foo:\n    - 10\n    - 20\n  bar:\n"
	    + "    - 30 # hi\n\n    - 40 # there\n- foobar: 50";
	array = (JSArray) JSUtilities.YAML.parse(otest9);
	for (Object item: array) {
	    System.out.println(" - [" + item + "]" );
	    for (Map.Entry<String,Object>entry: ((JSObject)item).entrySet()) {
		String key = entry.getKey();
		Object value = entry.getValue();
		if (value instanceof JSArray) {
		    System.out.println("  ... " + key + ":");
		    for (Object value1: ((JSArray)value)) {
			System.out.println("    ... " + value1);
		    }
		} else {
		    System.out.println("  ... " + key + ": " + value);
		}
	    }
	}

	System.out.println();
	System.out.println();
	System.out.println();

	System.out.println("... testing object 10");
	String otest10 = "- foo: &foo\n    - 10\n    - &XX 20\n  bar:\n"
	    + "    - 30 # hi\n\n    - *XX  # there\n- copyfoo: *foo";
	array = (JSArray) JSUtilities.YAML.parse(otest10);
	for (Object item: array) {
	    System.out.println(" - [" + item + "]" );
	    for (Map.Entry<String,Object>entry: ((JSObject)item).entrySet()) {
		String key = entry.getKey();
		Object value = entry.getValue();
		if (value instanceof JSArray) {
		    System.out.println("  ... " + key + ":");
		    for (Object value1: ((JSArray)value)) {
			System.out.println("    ... " + value1);
		    }
		} else {
		    System.out.println("  ... " + key + ": " + value);
		}
	    }
	}

	String ttest1 = "%YAML 1.2\n%TAG !foo! tag:localhost,2020\n---\n"
			 + " - !!str 1.0\n"
			 +"...\n";
	JSUtilities.YAML.parse(ttest1);

	System.out.println("testing tags 1");

	String ttest2 = "!!int \"10\"\n";
	Integer iobject = (Integer)JSUtilities.YAML.parse(ttest2);
	System.out.println("iobject = " + iobject);

	System.out.println("testing tags 2");

	String ttest3 = "  !!int \"10\"\n";
	iobject = (Integer)JSUtilities.YAML.parse(ttest3);
	System.out.println("iobject = " + iobject);

	String ttest4 = "!!str 10\n";
	Object obj = JSUtilities.YAML.parse(ttest4);
	System.out.println("object = " + obj
			   + ", class: " + obj.getClass());

	System.out.println("... testing tags 3");

	String ttest5 = "  !!str 10\n";
	obj = JSUtilities.YAML.parse(ttest5);
	System.out.println("object = " + obj
			   + ", class: " + obj.getClass());


	System.out.println("... testing tags 4");
	String ttest6A = "- \"20\" # comment";
	JSUtilities.YAML.parse(ttest6A);

	String ttest6a = "foo:\n  - 10\n  - \"20\"\nbar:\n"
	    + "  - 30 # hi\n\n  - \"40\" # there\n";
	String ttest6b = "foo:\n  - 10\n  - \"20\"\nbar:\n"
	    + "  - 30 # hi\n\n  - \"40\" # there\n"
	    + "foobar1: 50\n\nfoobar2: \"50\"\nfoobar3: 50\n"
	    + "\nfoobar4:  \"50\"\nfoobar5: 50\n";

	String ttest6 = "foo:\n  - !!str 10\n  - !!int \"20\"\nbar:\n"
	    + "  - 30 # hi\n\n  - \"40\" # there\n"
	    + "foobar1: 50\n\nfoobar2: \"50\"\nfoobar3: !!str 50\n"
	    + "\nfoobar4: !!int \"50\"\nfoobar5: !!float 50";
	object = (JSObject) JSUtilities.YAML.parse(ttest6);
	for (Map.Entry<String,Object>entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    if (value instanceof JSArray) {
		System.out.println("... " + key + ":");
		for (Object value1: ((JSArray)value)) {
		    System.out.println("  ... " + value1 + ", "
				       + value1.getClass());
		}
	    } else {
		System.out.println("... " + key + ": " + value
				   + ", " + value.getClass());
	    }
	}

	System.out.println();
	System.out.println("test multiple YAML documents");

	String mtest = "%YAML 1.2\n---\n- 10\n- 20\n---\n- 30\n- 40\n...\n";

	JSUtilities.YAML.Parser parser = new
	    JSUtilities.YAML.Parser(new StringReader(mtest));

	array = (JSArray) parser.getResults();
	for (Object o: array) {
	    System.out.println("... " + o);
	}
	System.out.println(".........");
	if (parser.hasNext()) {
	    array = (JSArray) parser.getResults();
	    for (Object o: array) {
		System.out.println("... " + o);
	    }
	}
	if (parser.hasNext()) {
	    System.out.println("did not terminate as it should have");
	}
	System.out.println();
	System.out.println();
	System.out.println();
	System.out.println();
	System.out.println();
	System.out.println();
	System.out.println("test conversion to key map");

	Reader r = new FileReader("YTest6.yaml", Charset.forName("UTF-8"));;
	array = (JSArray) JSUtilities.YAML.parse(r);

	r = new FileReader("YTest5.yaml", Charset.forName("UTF-8"));;
	array = (JSArray) JSUtilities.YAML.parse(r);

	r = new FileReader("YTest.yaml", Charset.forName("UTF-8"));
	object = (JSObject) JSUtilities.YAML.parse(r);

	System.out.println("foobar: " + object.get("foobar"));

	TemplateProcessor.KeyMap kmap = object.toKeyMap();
	kmap.print();
	
	r = new FileReader("YTest1.yaml", Charset.forName("UTF-8"));
	JSUtilities.YAML.parse(r);

	r = new FileReader("YTest2.yaml", Charset.forName("UTF-8"));
	JSUtilities.YAML.parse(r);


	System.out.println("Try YTest3.yaml");

	r = new FileReader("YTest3.yaml", Charset.forName("UTF-8"));
	JSUtilities.YAML.Parser yparser =
	    new JSUtilities.YAML
	    .Parser(r, new JSUtilities.YAML.TagSpec("!bzdev!",
						    "tag:bzdev.org,2021:esp",
						    ep));
	Object results = yparser.getResults();
	if (results instanceof JSArray) {
	    JSArray yarray = (JSArray)results;
	    for (Object yobject: yarray) {
		if (yobject instanceof JSObject) {
		    for (Map.Entry<String,Object> entry:
			     ((JSObject) yobject).entrySet()) {
			System.out.println(entry.getKey() + ": "
					   + entry.getValue());
		    }
		} else {
		    System.out.println(yobject);
		}
	    }
	}

	ObjectParser.SourceParser sp =
	    new ObjectParser.SourceParser(ep);

	System.out.println("Try YTest4.yaml");

	r = new FileReader("YTest4.yaml", Charset.forName("UTF-8"));
	yparser = new JSUtilities.YAML
	    .Parser(r, new JSUtilities.YAML.TagSpec("!bzdev!",
						    "tag:bzdev.org,2021:esp",
						    sp));
	results = yparser.getResults();

	if (results instanceof JSArray) {
	    JSArray yarray = (JSArray) results;
	    for (Object yobject: yarray) {
		if (yobject instanceof ObjectParser.Source) {
		    ObjectParser.Source src =
			(ObjectParser.Source) yobject;
		    System.out.println("... " + src.evaluate());
		} else {
		    System.out.println("... " + yobject);
		}
	    }
	}

	/*
	for (String yaml: firstTests) {
	    System.out.println("Parsing <" + yaml + "> ...");
	    Object object = JSUtilities.YAML.parse(yaml);
	    // System.out.println(object);
	    JSUtilities.JSON.writeTo(w, object);
	    w.flush();
	    System.out.println();
	}
	*/

    }
}
