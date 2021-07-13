import javax.swing.text.*;
import java.io.*;
import org.bzdev.swing.*;
import org.bzdev.swing.io.*;
import org.bzdev.io.*;

public class DRTest {
    static public void main(String argv[]) {
	PlainDocument doc = new PlainDocument();
	int i;
	String string = "hello world ";
	for (i = 0; i < 1000; i++) {
	    try {
		doc.insertString(doc.getLength(), string +i +"\n", null);
	    } catch (Exception e) {}
	}
	DocumentReader reader = new DocumentReader(doc);
	BufferedReader breader = new BufferedReader(reader);
	String line;
	// System.out.println("ready to read");
	try {
	    i = 0;
	    line = breader.readLine();
	    if (!line.equals(string + i)) {
		System.err.println("\"" + line + "\" != \"" 
				   + string + i + "\"");
		System.exit(1);
	    } else {
		i++;
	    }
	    //  System.out.println(line);
	    
	    while ((line = breader.readLine()) != null) {
		if (!line.equals(string + i)) {
		    System.err.println("\"" + line + "\" != \"" 
				       + string + i + "\"");
		    System.exit(1);
		} else {
		    i++;
		}
	    }
	    breader.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
