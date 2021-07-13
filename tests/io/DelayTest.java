import org.bzdev.io.*;
import java.io.*;

public class DelayTest {

    public static void main(String argv[]) {
	try {
	    DelayedFileInputStream dis1 =
		new DelayedFileInputStream("DelayTest.java");
	    
	    DelayedFileOutputStream dos2 =
		new DelayedFileOutputStream("test1.dat", false);

	    DelayedRandomAccessFile draf =
		new DelayedRandomAccessFile("test1.dat", "rw");

	    DelayedFileInputStream dis3 =
		new DelayedFileInputStream("test1.dat");

	    FileAccessor fa = new FileAccessor("test1.dat", "rw");

	    DirectoryAccessor da = new DirectoryAccessor("testdir");

	    DirectoryAccessor daro = new DirectoryAccessor("testdir", true);


	    System.setSecurityManager(new SecurityManager());

	    InputStream is1 = dis1.open();
	    OutputStream os2 = dos2.open();
	    System.out.println("first char of DelayTest.java = "
			       + (char)is1.read());
	    is1.close();
	    os2.write(1);
	    os2.flush();
	    os2.close();
	    RandomAccessFile raf = draf.open();
	    System.out.println("first byte of test.dat = "
			       + raf.readByte());
	    raf.close();
	    
	    InputStream is3 = dis3.open();
	    is3.close();

	    System.out.println("test FileAccessor");
	    InputStream is = fa.getInputStream();
	    System.out.println("first byte of test1.dat = "
			       + is.read());
	    is.close();
	    OutputStream os = fa.getOutputStream();
	    os.write(2);
	    os.close();
	    
	    raf = fa.getRandomAccessFile();
	    System.out.println("first byte of test1.dat = " + raf.readByte());
	    raf.seek(0L);
	    raf.writeByte(3);
	    raf.seek(0L);
	    System.out.println("first byte of test1.dat = " + raf.readByte());
	    raf.close();
	    
	    FileAccessor fa2 = da.createFileAccessor("t.dat");
	    DirectoryAccessor da2 = da.addDirectory("nested");


	    FileAccessor fa3 = da2.createFileAccessor("nt.dat");
	    os = fa2.getOutputStream();
	    os.write(4);
	    os.close();
	    is = fa2.getInputStream();
	    System.out.println("first byte of t.dat = " + is.read());
	    is.close();

	    os = fa3.getOutputStream();
	    os.write(5);
	    os.close();
	    is = fa3.getInputStream();
	    System.out.println("first byte of nt.dat = " + is.read());
	    is.close();
	    
	    os = da.getOutputStream("t.dat");
	    os.write(6);
	    os.close();
	    is = da.getInputStream("t.dat");
	    System.out.println("first byte of t.dat = " + is.read());
	    is.close();

	    raf = fa2.getRandomAccessFile();
	    System.out.println("first byte of t.dat = " + raf.readByte());
	    raf.close();
	    System.out.println("fa2.length() = " + fa2.length());
	    System.out.println("fa2.lastModified() = " + fa2.lastModified());

	    is = daro.getInputStream("t.dat");
	    System.out.println("first byte of t.dat (using daro) = "
			       + is.read());
	    is.close();

	    System.out.println("entries in directory associated with da");
	    for (String name: da.list()) {
		System.out.println("    " + name
				   + ": readable = " + da.canRead(name)
				   + ", writeable = " + da.canWrite(name)
				   + ", exists = " + da.exists(name)
				   + ", isDirectory = " + da.isDirectory(name));
	    }


	    System.out.println("entries in directory associated with daro");
	    for (String name: daro.list()) {
		System.out.println("    " + name
				   + ": readable = " + daro.canRead(name)
				   + ", writeable = " + daro.canWrite(name)
				   + ", exists = " + daro.exists(name)
				   + ", isDirectory = "
				   + daro.isDirectory(name));
	    }

	    try {
		os = daro.getOutputStream("tt.dat");
		System.out.println("should not have been able to open t.dat");
		System.exit(1);
	    } catch (Exception e) {
		System.out.println("expected error message: "
				   + e.getMessage());
	    }

	    try {
		fa = daro.createFileAccessor("t.dat", "rw");
		System.out.println("new file accessor should have failed "
				   + "(wrong mode)");
		System.exit(1);
	    } catch (Exception e) {
		System.out.println("expected error message: "
				   + e.getMessage());
	    }

	    try {
		raf = daro.getRandomAccessFile("t.dat", "rw");
		System.out.println("getRandomAccessFile should have failed "
				   + "(wrong mode)");
		System.exit(1);
	    } catch (Exception e) {
		System.out.println("expected error message: "
				   + e.getMessage());
	    }

	    da.move("t.dat", "t1.dat");
	    da.copy("t1.dat", "t2.dat");

	    System.out.println("Deleting t1.dat: " +da.delete("t1.dat"));
	    System.out.println("da contains");
	    for (String s: da.list()) {
		System.out.println("    " + s);
	    }
	    fa = da.createFileAccessor("nested");
	    if (fa.isDirectory()) {
		DirectoryAccessor nda = fa.getDirectoryAccessor();
		System.out.println("nested:");
		for (String s: nda.list()) {
		    System.out.println("    " + s);
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
