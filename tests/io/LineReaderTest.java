import org.bzdev.io.LineReader;
import java.io.*;

public class LineReaderTest {

    public static void main(String argv[]) throws Exception {

	String string = "abc\r\nxyz\r\r\r\nuvw\n\r\n";

	StringReader sr = new StringReader(string);
	BufferedReader br = new BufferedReader(sr);
	String s;
	System.out.println("buffered reader:");
	
	while ((s = br.readLine()) != null) {
	    s = s.replace("\r", "\\r");
	    s = s.replace("\n", "\\n");
	    System.out.println("\"" + s + "\"");
	}

	System.out.println("line reader (null):");

	sr = new StringReader(string);
	LineReader lr = new LineReader(sr);

	while ((s = lr.readLine()) != null) {
	    s = s.replace("\r", "\\r");
	    s = s.replace("\n", "\\n");
	    System.out.println("\"" + s + "\"");
	}

	System.out.println("line reader (LF):");

	sr = new StringReader(string);
	lr = new LineReader(sr, LineReader.Delimiter.LF);

	while ((s = lr.readLine()) != null) {
	    s = s.replace("\r", "\\r");
	    s = s.replace("\n", "\\n");
	    System.out.println("\"" + s + "\"");
	}

	System.out.println("line reader (CR):");
	sr = new StringReader(string);
	lr = new LineReader(sr, LineReader.Delimiter.CR);

	while ((s = lr.readLine()) != null) {
	    s = s.replace("\r", "\\r");
	    s = s.replace("\n", "\\n");
	    System.out.println("\"" + s + "\"");
	}

    }
}
