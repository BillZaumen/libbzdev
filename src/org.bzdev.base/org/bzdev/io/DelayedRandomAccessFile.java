package org.bzdev.io;

import java.io.*;
import java.security.*;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Class to provide delayed opening of a file output stream.
 * This class is intended to allow classes that have permission to
 * open a file for reading to delay opening the file until a later
 * time.  The file may be opened by a class that has weaker permissions.
 * <P>
 * An example of when this class may be useful is when an application
 * has to open a long list of files, perhaps passed to the application
 * on the command line, and these files have to be processed with a
 * security manager installed. Each java virtual machine has a limit on the
 * number of files that can be simultaneously opened, so opening all the
 * files (if there are enough of them) and then setting the security
 * manager will not work.  This class avoids that issue by allowing permissions
 * to be checked when the constructor is called and then actually opening
 * the file when it is needed (at a later time).
 */
public class DelayedRandomAccessFile {
    boolean opened = false;
    String mode;
    private File file;

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor given a file name.
     * The <code>mode</code> argument has the same values and meaning
     * as the second argument to the constructors for {@link
     * java.io.RandomAccessFile RandomAccessFile}.
     * @param name the name of the file to open
     * @param mode the access mode
     * @see java.io.RandomAccessFile
     */
    public DelayedRandomAccessFile(String name, String mode)
	throws FileNotFoundException
    {
	this ((name != null ? new File(name): null), mode);
    }
    

    /**
     * Constructor given a file
     * The <code>mode</code> argument has the same values and meaning
     * as the second argument to the constructors for {@link
     * java.io.RandomAccessFile RandomAccessFile}.
     * {@link java.io.RandomAccessFile RandomAccessFile}.
     * @param file the file to open
     * @param mode the access mode
     */
    public DelayedRandomAccessFile(File file, String mode)
	throws FileNotFoundException
    {
	String name = (file != null? file.getPath(): null);
	if (name == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	SecurityManager sm = System.getSecurityManager();
	boolean rw = (mode != null) && mode.startsWith("rw");
	if (sm != null) {
	    sm.checkRead(name);
	    if (rw) {
		sm.checkWrite(name);
	    }
	}
	try {
	    this.file = file.getCanonicalFile();
	    this.mode = mode;
	} catch (FileNotFoundException efnf) {
	    throw efnf;
	} catch (IOException eio) {
	    throw new FileNotFoundException
		(errorMsg("noCanonical", name));
	}
    }
    
    /**
     * Open the file specified by a constructor.
     * Security tests for permission to open the file were performed when the
     * constructor was called.  This method can be called at most one time.
     * The file actually opened is the file with the same canonical path name
     * as the file passed to the constructor.
     * @return the output stream for reading the file
     */
    public synchronized RandomAccessFile open() throws FileNotFoundException {
	if (opened)
	    throw new IllegalStateException(errorMsg("alreadyOpened"));
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<RandomAccessFile>() {
		    public RandomAccessFile run() throws FileNotFoundException {
			RandomAccessFile raf =
			    new RandomAccessFile(file, mode);
			opened = true;
			return raf;
		    }
		});
	} catch (PrivilegedActionException e) {
	    Exception ee = e.getException();
	    if (ee instanceof RuntimeException) {
		throw (RuntimeException) ee;
	    } else if (ee instanceof FileNotFoundException) {
		throw (FileNotFoundException) ee;
	    } else {
		String msg = errorMsg("unexpected");
		throw new RuntimeException(msg, ee);
	    }
	}
    }
}

//  LocalWords:  exbundle RandomAccessFile nullArg rw noCanonical
//  LocalWords:  alreadyOpened
