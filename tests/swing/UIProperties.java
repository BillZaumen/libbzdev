import java.io.*;
import java.util.*;
import javax.swing.*;
import org.bzdev.io.CSVWriter;

public class UIProperties {

    public static void main(String argv[]) throws Exception {
	String lafname = (argv.length == 1 && argv[0].equals("system"))?
	    UIManager.getSystemLookAndFeelClassName():
	    UIManager.getCrossPlatformLookAndFeelClassName();

	UIManager.setLookAndFeel(lafname);

	UIDefaults defaults = UIManager.getLookAndFeelDefaults();

	TreeSet<String> set = new TreeSet<>();
	for (Object key: defaults.keySet()) {
	    if (key instanceof String) {
		set.add((String)key);
	    }
	}
	CSVWriter w = new
	    CSVWriter(new OutputStreamWriter(System.out, "UTF-8"), 3);
	for (String key: set) {
	    Object value = defaults.get(key);
	    w.writeRow(key,
		       (value == null)? "[unknown]":
		       value.getClass().getCanonicalName(),
		       "" + value);
	}
	w.flush();
	w.close();
	System.exit(0);
    }
}
