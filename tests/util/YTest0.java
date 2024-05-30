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
	JSArray list = (JSArray)
	    JSUtilities.YAML.parse(new FileReader("strings.yaml", UTF8));
	for (Object o: list) {
	    System.out.println(o.toString());
	    System.out.println("---");
	}

	JSObject object = (JSObject)
	    JSUtilities.YAML.parse(new FileReader("ytest0.yaml", UTF8));
	for (Map.Entry<String,Object> entry: object.entrySet()) {
	    String key = entry.getKey();
	    Object value = entry.getValue();
	    System.out.format("%s: %s\n", key, value);
	    if (value instanceof JSObject) {
		object = (JSObject) value;
		for (Map.Entry<String,Object> entry2: object.entrySet()) {
		    key = entry2.getKey();
		    value = entry2.getValue();
		    System.out.format("... %s: %s\n", key, value);
		}
	    }
	}
   }
}
