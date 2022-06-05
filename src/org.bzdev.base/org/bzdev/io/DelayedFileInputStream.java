package org.bzdev.io;

import java.io.*;
import java.security.*;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Class to provide delayed opening of a file input stream.
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
public class DelayedFileInputStream {
    boolean opened = false;
    private File file;

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor given a file name.
     * @param name the name of the file to open for reading
     */
    public DelayedFileInputStream(String name) throws FileNotFoundException {
	this (name != null ? new File(name): null);
    }
    

    /**
     * Constructor given a file.
     * @param file the file to open for reading.
     */
    public DelayedFileInputStream(File file) throws FileNotFoundException {
	String name = (file != null? file.getPath(): null);
	/*
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkRead(name);
	}
	*/
	if (name == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	try {
	    this.file = file.getCanonicalFile();
	} catch (FileNotFoundException efnf) {
	    throw efnf;
	} catch (IOException eio) {
	    throw new FileNotFoundException
		(errorMsg("noCanonical", file.getName()));
	}
    }
    
    /**
     * Open the file specified by a constructor.
     * Security tests for permission to open the file were performed when the
     * constructor was called.  This method can be called at most one time.
     * The file actually opened is the file with the same canonical path name
     * as the file passed to the constructor.
     * @return the input stream for reading the file
     */
    public synchronized InputStream open() throws FileNotFoundException {
	    if (opened)
		throw new IllegalStateException(errorMsg("alreadyOpened"));
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<InputStream>() {
		    public InputStream run() throws FileNotFoundException {
			InputStream is = new FileInputStream(file);
			opened = true;
			return is;
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

//  LocalWords:  exbundle nullArg noCanonical alreadyOpened
