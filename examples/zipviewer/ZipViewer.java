import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Desktop;
import java.util.*;
import java.util.zip.*;

public class ZipViewer {

    static int port = 8082;
    static EmbeddedWebServer ews;
    static void addEntry(EmbeddedWebServer ews, String path, File f) 
	throws Exception
    {
	if (f.isDirectory()) {
	    File wx = new File(f, "WEB-INF" + File.separator + "web.xml");
	    boolean noWebxml = !wx.isFile() || !wx.canRead();
	    ews.add(path, DirWebMap.class, f,
		    null, noWebxml, true, false);
	} else {
	    String name = f.getName();
	    if (name.endsWith(".zip") || name.endsWith(".jar")) {
		boolean noWebxml = false;
		ZipFile zf = null;
		try {
		    zf = new ZipFile(f);
		    ZipEntry ze = zf.getEntry("WEB-INF/web.xml");
		    noWebxml = (ze == null);
		} catch (IOException e) {
		    noWebxml = true;
		    // System.out.println("IOException in checking ZIP files");
		} finally {
		    if (zf != null) zf.close();
		}
		ews.add(path, ZipWebMap.class, f,
			null, noWebxml, true, !noWebxml);
	    } else if (name.endsWith(".war")) {
		    ews.add(path, ZipWebMap.class, f,
			    null, false, true, true);
	    } else {
		throw new IllegalArgumentException
		    ("not a directory, zip, jar, or war file");
	    }
	}
    }
    public static void main(String argv[]) {
	try {
	    ews = new EmbeddedWebServer(port, null);
	    if (argv.length == 0) {
		System.exit(0);
	    } else if (argv.length == 1) {
		addEntry(ews, "/", new File(argv[0]));
	    } else {
		Set<String> names = new HashSet<String>();
		for (String arg: argv) {
		    File f = new File(arg);
		    String name = URLEncoder.encode(f.getName(), "UTF-8");
		    if (names.contains(name)) {
			for (int i = 1;; i++) {
			    String newName = name + i;
			    if (!names.contains(newName)) {
				name = newName;
				break;
			    }
			}
		    }
		    names.add(name);
		    addEntry(ews, "/" + name + "/", f);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	ews.start();

	try {
	    URI uri = new URI("http://localhost:" + port + "/");
	    Desktop.getDesktop().browse(uri);
	} catch (Exception e1) {
	    System.out.println("please start browser manually");
	}
    }
}
