import org.bzdev.util.*;
import java.io.*;

public class SMITest {

    public static void main(String argv[]) throws Exception {
	new SharedMimeInfo(new File("minfo.xml"))
	    .addConfigPropertyType(80, "application/foo", "foo",
				   "foo type")
	    .addZipDocType(80, "application/bar+zip", "bar",
			   "bar type")
	    .close();
	
    }
}
