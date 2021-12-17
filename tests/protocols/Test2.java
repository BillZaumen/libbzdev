import java.net.*;
import java.io.*;
import org.bzdev.protocols.Handlers;
import java.util.*;
import java.util.regex.*;

public class Test2 {

    // copy of 'normalize' from org.bzdev.protocols.resource.Handler
    // so we can test it independently.

    static String pathsep = "|";

    public static void main(String argv[]) throws Exception {
	String path = "$classpath|" + argv[0];

	System.setProperty("org.bzdev.protocols.resource.path", path);
	Handlers.enable();

	URL url = new URL("resource:hello.txt");
	int val;
	InputStream is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
	is.close();
	url = new URL("resource:goodbye.txt");
	is = url.openStream();
	while((val = is.read()) != -1) {
	    System.out.print((char)val);
	}
    }
}
