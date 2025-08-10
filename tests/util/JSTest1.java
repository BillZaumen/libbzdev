import java.io.*;
import java.util.*;

import org.bzdev.util.*;


public class JSTest1 {
    public static void main(String argv[]) throws Exception {
	InputStream is = new FileInputStream("jsontest");
	JSObject obj = (JSObject) JSUtilities.JSON.parse(is, "UTF-8");
	JSArray array = (JSArray)obj.get("matches");
	for (Object ob: array) {
	    System.out.println ("****");
	    JSObject object = (JSObject) ob;
	    for (String key: object.keySet()) {
		System.out.println("key = " + key);
	    }
	}
	System.exit(0);
    }
}
