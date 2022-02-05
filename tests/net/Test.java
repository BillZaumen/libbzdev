import org.bzdev.net.*;
import java.net.URL;
import java.net.HttpCookie;
import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

public class Test {

    private static String ourCodebaseDir;
    static {
	try {
	    ourCodebaseDir =
		(new File(URLPathParser.class.getProtectionDomain()
			  .getCodeSource().getLocation()
			  .toURI())).getParentFile().getCanonicalPath();
	} catch (Exception e) {
	    System.err.println("Could not find our own codebase");
	    System.exit(1);
	}
    }


    public static void main(String argv[]) throws Exception {
	String string = "abc\"<'>\"def";

	System.out.println(string + " ---> " + WebEncoder.htmlEncode(string));
	System.out.println("decoded: " +
			   WebDecoder.htmlDecode(WebEncoder.htmlEncode
						 (string)));

	string = "ab\\\'\"\n\t\r\bcd";
	System.out.println(string + " ---> " + WebEncoder.quoteEncode(string));
	System.out.println(WebEncoder.quoteEncode(string) + " ---> "
			   + WebDecoder.quoteDecode(WebEncoder.quoteEncode
						    (string)));

	Map<String,String> map = new LinkedHashMap<>();
	map.put("x1", "10");
	map.put("x2", "10=10");
	map.put("x3", "20 > 30");
	map.put("x4", "10&");
	map.put("R0f", "2.0 + t / 360.0");;
	String query1 = WebEncoder.formEncode(map);
	String query2 = WebEncoder.formEncode(map, true);
	String query3 = "R0=2.0%20%2B%20t%20%2F%20360";
	Map<String,String>map3 = WebDecoder.formDecode(query3);
	System.out.println("R0 = " + map3.get("R0"));

	System.out.println("query1 = " + query1);
	System.out.println("query2 = " + query2);
	Map<String,String> map2 = WebDecoder.formDecode(query1);
	Map<String,String[]> map2mv = WebDecoder.formDecodeMV(query1);
	if (map2.size() != map.size()) throw new Exception("size");
	for (Map.Entry<String,String> entry: map2.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue();
	    if (!map.containsKey(key)) throw new Exception("key");
	    if (!value.equals(map.get(key))) throw new Exception("value");
	}
	if (map2mv.size() != map.size()) throw new Exception("size");
	for (Map.Entry<String,String[]> entry: map2mv.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue()[0];
	    if (!map.containsKey(key)) throw new Exception("key");
	    if (!value.equals(map.get(key))) throw new Exception("value");
	}
	map2 = WebDecoder.formDecode(query2, true);
	map2mv = WebDecoder.formDecodeMV(query2, true);
	if (map2.size() != map.size()) throw new Exception("size");
	for (Map.Entry<String,String> entry: map2.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue();
	    if (!map.containsKey(key)) throw new Exception("key");
	    if (!value.equals(map.get(key))) throw new Exception("value");
	}
	if (map2mv.size() != map.size()) throw new Exception("size");
	for (Map.Entry<String,String[]> entry: map2mv.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue()[0];
	    if (!map.containsKey(key)) throw new Exception("key");
	    if (!value.equals(map.get(key))) throw new Exception("value");
	}
	map2mv = WebDecoder.formDecodeMV("foo=a&foo=b&bar=aa&bar=bb");
	if (map2mv.get("foo").length != 2) throw new Exception("multiple");
	if (!map2mv.get("foo")[0].equals("a")) throw new Exception("multiple");
	if (!map2mv.get("foo")[1].equals("b")) throw new Exception("multiple");
	if (!map2mv.get("bar")[0].equals("aa")) throw new Exception("multiple");
	if (!map2mv.get("bar")[1].equals("bb")) throw new Exception("multiple");
	string = "http://a.com/b.jar|foo|for-||||bar|||xyz||";
	
	System.out.println("trying " + string);
	URL[] urls  = URLPathParser.getURLs(string);
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting " + string);
	for (String s: URLPathParser.split(string)) {
	    System.out.println(s);
	}

	string = "classes|ltest.jar";
	System.out.println("trying " + string);
	urls = URLPathParser.getURLs(string);
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting " + string);
	for (String s: URLPathParser.split(string)) {
	    System.out.println(s);
	}
	string = ".../foo.jar|classes|ltest.jar|~/ltest.jar|~~ltest.jar";
	System.out.println("trying " + string);
	urls = URLPathParser.getURLs(null,string, "/usr/bar");
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting " + string);
	for (String s: URLPathParser.split(string)) {
	    System.out.println(s);
	}

	string = ".../foo.jar";
	System.out.println("trying " + string);
	urls = URLPathParser.getURLs(null,string, "/usr/bar");
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting " + string);
	for (String s: URLPathParser.split(string)) {
	    System.out.println(s);
	}

	System.out.println("trying empty string case");
	urls = URLPathParser.getURLs(null,"", "/usr/bar");
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting an empty string");
	for (String s: URLPathParser.split("")) {
	    System.out.println(s);
	}


	string = ".../foo.jar";
	System.out.println("trying " + string);
	urls = URLPathParser.getURLs(null,string, ourCodebaseDir);
	for (URL url: urls) {
	    System.out.println(url);
	}
	System.out.println("splitting " + string);
	for (String s: URLPathParser.split(string)) {
	    System.out.println(s);
	}

	urls = URLPathParser.getURLs(null, string, ourCodebaseDir,
				     System.out);
	System.out.println("(File error message expected)");


	HeaderOps headers = HeaderOps.newInstance();

	headers.set("content-type", "text/plain; charset=\"utf-8\"");

	headers.add("ourhdr", "foo");
	headers.add("ourhdr", "bar");

	System.out.println("content-type: " + headers.getFirst("content-type"));
	System.out.print("ourhdr:");
	for (String value: headers.get("ourhdr")) {
	    System.out.print(" " + value);
	}
	System.out.println();
	map = headers.parseFirst("content-type", false);
	for (Map.Entry<String,String> entry: map.entrySet()) {
	    System.out.println(entry.getKey() + " = " + entry.getValue());
	}

	System.out.println();

	headers.set("parms", "parm1 = 2; parm2 = 3; httponly; parm3=4");
	headers.set("parms2", "parm1 = 2; parm2 = 3; httponly; parm3=4;");
	headers.set("ctest", "foo/bar; x = foo (comment)bar;"
		    + "y = bar (\"comment)  foo; z = bar (comment) bar;"
		    + "(last comment)");
	headers.set("qtest", "foo1/bar1; x = foo\"\\\"quote\\\"\"bar;"
		    + "y = bar \"quote\"foo; z = bar\"quote\" bar");

	headers.set("qtest", "foo1/bar1; x = foo\"\\\"quote\\\"\"bar;"
		    + "y = bar \"quote\"foo; z = bar\"quote\" bar");
	headers.set("commas", "text/plain; charset=uft-8, text/html; "
		    + "charset2=utf-8");

	String names[] = {"parms", "parms2", "ctest", "qtest", "commas"};

	for (String name: names) {
	    System.out.println("*** name = " + name +":");
	    map = headers.parseFirst(name, false);
	    if (map == null) {
		System.out.println("no header for " + name);
	    }
	    for (Map.Entry<String,String> entry: map.entrySet()) {
		System.out.println(entry.getKey() + " = " + entry.getValue());
	    }
	}
	System.out.println("now try commas with acceptComma set to true");
	map = headers.parseFirst("commas", true);
	if (map == null) {
	    System.out.println("no header for commas");
	}
	for (Map.Entry<String,String> entry: map.entrySet()) {
	    System.out.println(entry.getKey() + " = " + entry.getValue());
	}
	System.out.println("now try commas as a multiple-valued header:");
	for (Map<String,String> m:  headers.parseAll("commas", false)) {
	    for (Map.Entry<String,String> entry: m.entrySet()) {
		System.out.println(entry.getKey() + " = " + entry.getValue());
	    }
	}

	System.out.println
	    ("now try commas as a multiple-valued header (acceptCommas=true):");
	headers.set("commas", "text/plain; charset=uft-8, text/html; "
		    + "charset2=utf-8");
	headers.add("commas", "text/html; charset=uft-8, text/plain; "
		    + "charset2=utf-8");


	for (Map<String,String> m:  headers.parseAll("commas", true)) {
	    for (Map.Entry<String,String> entry: m.entrySet()) {
		System.out.println(entry.getKey() + " = " + entry.getValue());
	    }
	}
	System.out.println("------ HttpCookie Test---------");
	HttpCookie hc = new HttpCookie("foo", "foo bar");
	hc.setMaxAge(100);
	System.out.println("hc = " + hc.toString());
	hc = new HttpCookie("foo", "foo\"bar");
	System.out.println("hc = " + hc.toString());

	System.out.println("------ ServerCookie test--------");
	ServerCookie sc = ServerCookie.newInstance("name1", "foo");
	sc.setMaxAge(600);
	sc.setDomain("foo-bar.com");
	sc.setPath("foo/bar");
	System.out.format("sc: name = %s, value = %s\n",
			  sc.getName(), sc.getValue());
	String svalue = "hello";
	sc.setComment("hello");
	if (!sc.getComment().equals(svalue)) throw new Exception();
	sc.addToHeaders(headers);
	svalue = "foo bar";
	sc.setValue(svalue);
	if (!sc.getValue().equals(svalue)) throw new Exception();
	sc.setMaxAge(-1);
	sc.setPath(null);
	sc.setDomain(null);
	svalue = "hello there";
	sc.setComment(svalue);
	if (!sc.getComment().equals(svalue)) throw new Exception();

	sc.addToHeaders(headers);
	sc.setComment(null);
	sc.setValue("hello");
	sc.addToHeaders(headers);

	for (String s: headers.get("Set-Cookie")) {
	    System.out.println(s);
	}
	for (int i = 0; i < 6; i++) {
	    try {
		switch(i) {
		case 0:
		    sc.setValue("a\"b");
		    break;
		case 1:
		    sc.setPath("foo/bar;");
		    break;
		case 2:
		    sc.setComment("abd\"def");
		    break;
		case 3:
		    sc.setDomain("a..b");
		    break;
		case 4:
		    sc.setDomain("-ab");
		    break;
		case 5:
		    sc.setDomain("ab.");
		    break;
		}
		throw new Exception("checks failed, case " + i);
	    } catch (IllegalArgumentException e) {
		System.out.println("case " + i + ", exception expected");
	    }
	}
	System.exit(0);
    }
}
