import java.net.*;
import java.io.*;
import org.bzdev.protocols.Handlers;
import java.util.*;
import java.util.regex.*;

public class Test {

    // copy of 'normalize' from org.bzdev.protocols.resource.Handler
    // so we can test it independently.

    static String pathsep = "|";
    static String RE =
	"([-[a-zA-Z0-9.+~!_$&'()*+,;=]|]|%[0-9A-Za-z][0-9A-Za-z])+";
    static Pattern compiledRE = Pattern.compile(RE);

    static String errorMessage(String... args) {
	String result = "";
	boolean first = true;
	for (String arg: args) {
	    if (first) first = false;
	    else result = result + " ";
	    result = result + arg;
	}
	return result;
    }

    static String normalize(String path) throws IOException {
	if (path.endsWith("/")) {
	    throw new IOException(errorMessage("slashAtEnd", path));
	}
	String[] components = path.split("/");
	ArrayList<String> alist = new ArrayList<>(components.length);
	int index = 0;
	try {
	    for (String component: components) {
		if (component.length() == 0) {
		    throw new IOException(errorMessage("nullComponent", path));
		}
		if (!compiledRE.matcher(component).matches()) {
		    throw new IOException
			(errorMessage("badPath",component, path));
		}
		if (component.equals(".")
		    || component.equals("%2E")) {
		    continue;
		} else if (component.equals("..")
			   || component.equals("%2E.")
			   || component.equals(".%2E")
			   || component.equals("%2E%2E")) {
		    index--;
		    alist.remove(index);
		} else {
		    alist.add(index++, component);
		}
	    }
	    StringBuilder sb = new StringBuilder(path.length());
	    boolean first = true;
	    for (String component: alist) {
		if (first) {
		    first = false;
		} else {
		    sb.append("/");
		}
		sb.append(component);
	    }
	    return sb.toString();
	} catch (IndexOutOfBoundsException e) {
	    throw new IOException(errorMessage("pathDotDot", path));
	}
    }

    static String badpaths[] = {
	"../foo",
	"foo/../..",
	"a/b/../c/../../../d",
	"a/",
	"a/",
	"./..",
	"a/b/foo/",
	"a\\b\\c",
	"a/b#foo"
    };

    static String goodpaths[] = {
	"a/b.png",
	"x_b.z",
	"x+b.z",
	"x%20y",
	"x%5Cz",
	"x%5cz"
    };


    public static void main(String argv[]) throws Exception {

	for (String p: goodpaths) {
	    System.out.println(p + " " + normalize(p));
	}
	for (String p: badpaths) {
	    try {
		normalize(p);
		throw new Exception("\"" + p +"\" unexpectedly worked");
	    } catch (IOException e){}
	}

	String path ="./testdir|$classpath"
	    + "|jar:file:../../BUILD/libbzdev-base.jar!/org/bzdev/lang/lpack";

	System.setProperty("org.bzdev.protocols.resource.path", path);
	System.setSecurityManager(new SecurityManager());
	Handlers.enable();

	URL url = new URL("sresource:hello.txt");
	InputStream is = url.openStream();
	int val;
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	is.close();
	url = new URL("resource:hello.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	is.close();
	url = new URL("resource:goodbye.txt");
	URL url2 = url.openConnection().getURL();
	System.out.println("url = " + url);
	System.out.println("url2 = " + url2);
	System.out.println(Handlers.sameFile(url, url2));
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:foo/%2E./goodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:foo/.%2E/goodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:foo/%2E%2E/goodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:foo/../goodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:%67oodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	url = new URL("resource:Lang.properties");
	is = url.openStream();
	int count = 0;
	while((val = is.read()) != -1) {
	    count++;
	}
	System.out.println("count = " + count);

	try {
	    url = new URL("resource:foo%2F../goodbye.txt");
	    is = url.openStream();
	    is.close();
	    System.out.println("reading resource:foo%2F../goodbye.txt "
			       + "should have failed");
	} catch (Exception e) {
	    System.out.print("exception thrown (as expected):");
	    System.out.println(e.getMessage());
	}
	try {
	    url = new URL("resource:../Test.java");
	    is = url.openStream();
	    is.close();
	    System.out.println("reading Test.java should have failed");
	} catch (Exception e) {
	    System.out.print("exception thrown (as expected):");
	    System.out.println(e.getMessage());
	}
    }
}
