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

public class YTest0 {

    static final Charset UTF8 = Charset.forName("UTF-8");

    public static void main(String argv[]) throws Exception {
	/*
 	String sctest = "%YAML 1.2\n---\n trying foo bar\n...\n";
	Object obj =  JSUtilities.YAML.parse(sctest);
	System.out.print("found the " + obj.getClass() + ": ");
	System.out.println(obj);
	*/
	JSArray list = (JSArray)
	    JSUtilities.YAML.parse(new FileReader("strings.yaml", UTF8));
	for (Object o: list) {
	    System.out.println(o.toString());
	    System.out.println("---");
	}
   }
}
