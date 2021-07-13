import org.bzdev.io.ZipDocWriter;
import org.bzdev.io.ZipDocFile;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

public class ZipDocWriterTest {
    public static void main(String argv[]) {
	try {
	    FileOutputStream fos = new FileOutputStream("test.dat");
	    ZipDocWriter zdw = new ZipDocWriter(fos, "application/test");
	    OutputStream os = zdw.nextOutputStream("entry1", false, 0);
	    byte[] data = new byte[1024];
	    for (int i = 0; i < 1024; i++) {
		int val = i % 256;
		if (val > 127) val -= 256;
		data[i] = (byte) val;
	    }
	    os.write(data);
	    os.close();
	    os = zdw.nextOutputStream("entry2", true, 9);
	    os.write(data);
	    os.close();
	    os = zdw.nextOutputStream("entry3", false, 0);
	    os.write(data);
	    os.close();

	    os = zdw.nextOutputStream("entry4", true, 9, 5);
	    os.write(data);
	    os.close();
	    zdw.repeatFile("entry4-1");
	    zdw.repeatFile("entry4-2");
	    zdw.repeatFile("entry4-3");
	    zdw.repeatFile("entry4-4");

	    os = zdw.nextOutputStream("entry5", false, 0, 5);
	    os.write(data);
	    os.close();
	    zdw.repeatFile("entry5-1");
	    zdw.repeatFile("entry5-2");
	    zdw.repeatFile("entry5-3");
	    zdw.repeatFile("entry5-4");

	    os = zdw.nextOutputStream("entry6", true, 9);
	    os.write(data);
	    os.close();
	    zdw.close();
	    ZipDocFile zdf = new ZipDocFile("test.dat");
	    System.out.println("Test ZipDocFile");
	    System.out.println("MIME type = " + zdf.getMimeType());
	    System.out.format(" entry count: %d, %d\n",
			      zdf.getRequestedEntryCount(),
			      zdf.getActualEntryCount());

	    String names[] = {"entry3", "entry4",
			      "entry4-1", "entry4-2", "entry4-3", "entry4-4",
			      "entry5",
			      "entry5-1", "entry5-2", "entry5-3", "entry5-4",
			      "entry6"};
	    for (String name: names) {
		ZipEntry entry = zdf.getEntry(name);
		System.out.format("rep count for %s = %d\n",
				  name,
				  ZipDocFile.getRepetitionCount(entry));
	    }

	    System.out.println("Test zdf.entries():");
	    Enumeration<? extends ZipEntry> entries = zdf.entries();
	    while (entries.hasMoreElements()) {
		ZipEntry entry = entries.nextElement();
		System.out.format("%s --> %s: %b %d\n",
				  entry.getName(),
				  ZipDocFile.getActualName(entry),
				  ZipDocFile.isActualEntry(entry),
				  ZipDocFile.getRepetitionCount(entry));
	    }
	    System.out.println("Test failure for a non-existent entry:");
	    ZipEntry badEntry = zdf.getEntry("foo");
	    System.out.println("badEntry = " + badEntry);
	    try {
		InputStream bis = zdf.getInputStream(badEntry);
	    } catch (Exception ee) {
		ee.printStackTrace();
		System.out.println("[exception expected]");
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}


	System.exit(0);
    }
}
