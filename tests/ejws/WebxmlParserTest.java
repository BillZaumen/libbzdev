import org.bzdev.ejws.*;
import org.bzdev.util.CopyUtilities;
import java.io.*;

public class WebxmlParserTest {
    static public void main(String[] argv) {
	try {
	    WebxmlParser parser = new WebxmlParser();

	    File root = new File (argv[0]);

	    WebMap map = WebMap.newInstance(root,
					    "org.bzdev.ejws.maps.DirWebMap");

	    WebMap.Info winfo = map.getWebxml();
	    parser.parse(winfo.getInputStream(), winfo.getLocation(), map);
	    FileOutputStream os = new FileOutputStream(new File(argv[1]));
	    // map.setRoot(root);
	    WebMap.Info info = map.getInfo("/index.html");
	    // System.out.println(info.getMIMEType());
	    // System.out.println(info.getLength());
	    CopyUtilities.copyStream(info.getInputStream(), os);
	    info.getInputStream().close();
	    os.close();
	    System.exit(0);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
