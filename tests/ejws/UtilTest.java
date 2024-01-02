
import org.bzdev.ejws.EjwsUtilities;
import org.bzdev.ejws.WebMap;
import org.bzdev.ejws.WebMap.Info;
import org.bzdev.ejws.maps.DirWebMap;
import java.io.*;
import java.nio.charset.Charset;
import org.bzdev.util.CopyUtilities;

public class UtilTest {
    static public void main(String argv[]) {
	try {
	    DirWebMap webmap = new
		DirWebMap(new File(System.getProperty("user.dir")));

	    File file = new File(argv[0]);
	    if (file.isDirectory()) {
		EjwsUtilities.printHtmlDir(file,
					  "http://localhost:8082/our-tests/",
					  "UTF-8",
					  new FileOutputStream("junk"),
					  webmap);
		WebMap.Info info =
		    EjwsUtilities.printHtmlDir
		    (file, "http://localhost:8082/our-tests/", "UTF-8",
		     webmap);
		System.out.println("info.getMIMEType() = "
				   + info.getMIMEType());
		CopyUtilities.copyStream(info.getInputStream(), System.out,
					 Charset.forName("UTF-8"));
	    } else {
		EjwsUtilities.printHtmlDir(file,
					  "http://localhost:8082/our-tests/",
					  argv.length == 1? "":
					  argv[1],
					  "UTF-8",
					  new FileOutputStream("junk"),
					  webmap);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
