package org.bzdev.io;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Class to provide access to a file in the presence of a security
 * manager.  Permission to access the file as specified by a mode
 * argument must be granted when a constructor is called. An input
 * stream, output stream, or a RandomAccessFile can then be obtained
 * using the permission granted to this class.
 * The modes are combinations of the following letters:
 * <ul>
 *   <li> "r" - the file accessor can open an input stream or a
 *              random-access file with read access.
 *   <li> "w" - the file accessor can open an output stream or a
 *              random-access file with write access.
 *   <li> "a" - if the file accessor opens an output stream, the
 *              data written to that output stream will be appended
 *              to the file.
 * </ul>
 * All combinations of these three letters are allowed.  The permissions
 * implied by "r" and "w" apply to capabilities of an instance of
 * FileAccessor, not to file-system permissions.
 * <P>
 * The caller must have the necessary permissions to create or modify
 * the specified file when the constructor is called.  The methods
 * {@link #getInputStream() getInputStream},
 * {@link #getOutputStream() getOutputStream}, and 
 * {@link #getRandomAccessFile() getRandomAccessFile} provide
 * access to an input stream, output stream, and RandomAccessFile
 * regardless of the permissions when these methods are called.
 * <P>
 * For example, a program using a command-line interface might
 * parse the command line and create a FileAccessor for each
 * file that an application was going to open, either for reading
 * or writing. Then the program could install a security manager,
 * after which the application could use the  file accessors to open
 * input or output streams as needed, ensuring that the maximum number
 * of open files for the JVM is not exceeded: when a FileAccessor is
 * created, the corresponding file is not opened immediately.
 *
 */
public class FileAccessor {

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    File file;
    boolean readable = true;
    boolean writable = true;
    boolean appendable = false;


    /**
     * Get the name of the file corresponding to this file accessor.
     * @return the last component of the file name
     */
    public String getName() {
	return file.getName();
    }

    /**
     * Test if the file associated with this file accessor is readable
     * and this file accessor allows reading.
     * @return true if the file is readable; false if not
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean isReadable() {
	if (readable) {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Boolean>() {
		    public Boolean run() {
			return file.canRead();
		    }
		});
	} else {
	    return false;
	}
    }

    /**
     * Test if the file associated with this file accessor is writable
     * and this file accessor allow writing.
     * @return true if the file is writable; false if not
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean isWritable() {
	if (writable) {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Boolean>() {
		    public Boolean run() {
			return file.canWrite();
		    }
		});
	} else {
	    return false;
	}
    }

    /**
     * Test if the file associated with this file accessor is writable
     * and this file accessor allow writing.
     * @return true if the file is writable; false if not
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean exists() {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Boolean>() {
		    public Boolean run() {
			return file.exists();
		    }
		});
    }

    /**
     * Returns the time that the file referenced by this file accessor was
     * last modified.
     * The value returned may be negative, indicating the number of milliseconds
     * before the epoch.
     * @return the time the file was last modified in milliseconds since
     *     the epoch (00:00:00: GMT, January 1, 1970); 0L if an IO error
     *     occurs
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see File#lastModified()
     */
    public long lastModified()
    {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Long>() {
		    public Long run() {
			return file.lastModified();
		    }
		});

    }

    /**
     * Returns the length of the file referenced by this file accessor.
     * @return the length of the file in bytes
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see File#lastModified()
     */
    public long length()
    {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Long>() {
		    public Long run() {
			return file.length();
		    }
		});

    }



    /**
     * Test if data must be appended to the file associated with this
     * file accessor.  A separate test is needed to determine if the
     * file is writable.
     * @return true if data must be appended; false otherwise
     */
    public boolean isAppendOnly() {
	return appendable;
    }

    /**
     * Determine if the file referenced by this file accessor is a directory.
     * @return true if it is a directory; false otherwise
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean isDirectory() {
	return AccessController.doPrivileged
	    (new PrivilegedAction<Boolean>() {
		    public Boolean run() {
			return file.isDirectory();
		    }
		});
    }

    /**
     * Constructor given a file name.
     * @param filename the name of the file to access
     * @exception IOException the file, most likely a portion of
     *            its path cannot be found or is not accessible
     * @exception SecurityException the caller does not have the permissions
     *            needed to access a file
     * @exception NullPointerException the argument was null
     */
    public FileAccessor(String filename)
	throws IOException, SecurityException, NullPointerException
    {
	this(filename, null);
    }

    /**
     * Constructor given a file name and mode.
     * The modes are strings containing the following letters:
     * <ul>
     *   <li> "r" - the file accessor can open an input stream.
     *   <li> "w" - the file accessor can open an output stream.
     *   <li> "a" - if the file accessor opens an output stream, the
     *              data written to that output stream will be appended
     *              to the file.
     * </ul>
     * @param filename the name of the file to access
     * @param mode the file-accessor mode (a combination of 'r', 'w',
     *        and 'a')
     * @exception IOException the file, most likely a portion of
     *            its path, cannot be found or is not accessible
     * @exception SecurityException the caller does not have the permissions
     *            needed to access a file
     * @exception NullPointerException the first argument was null
     */
    public FileAccessor(String filename, String mode) 
	throws IOException, SecurityException, NullPointerException
    {
	this(((filename == null)? null: new File(filename)), mode);
    }

    /**
     * Constructor given a File.
     * @param file the file to access
     * @exception IOException the file, most likely a portion of
     *            its path cannot be found or is not accessible
     * @exception SecurityException the caller does not have the permissions
     *            needed to access a file
     * @exception NullPointerException the argument was null
     */
    public FileAccessor(File file)
	throws IOException, SecurityException, NullPointerException
    {
	this(file, null);
    }

    /**
     * Constructor given a File and mode.
     * The modes are strings containing the following letters:
     * <ul>
     *   <li> "r" - the file accessor can open an input stream.
     *   <li> "w" - the file accessor can open an output stream.
     *   <li> "a" - if the file accessor opens an output stream, the
     *              data written to that output stream will be appended
     *              to the file. The file may not be opened as a random
     *              access file for writing
     * </ul>
     * @param file the file to access
     * @param mode the file-accessor mode (a combination of 'r', 'w',
     *        and 'a')
     * @exception IOException the file, most likely a portion of
     *            its path cannot be found or is not accessible
     * @exception SecurityException the caller does not have the permissions
     *            needed to access a file
     * @exception NullPointerException the first argument was null
     */
     public FileAccessor(File file, String mode)
	 throws IOException, SecurityException, NullPointerException
    {
	if (file == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (mode != null) {
	    if (mode.equals("a")) {
		appendable = true;
	    } else {
		if (!mode.contains("r")) readable = false;
		if (!mode.contains("w")) writable = false;
		if (mode.contains("a")) appendable = true;
	    }
	}
	/*
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    String name = (file != null? file.getPath(): null);
	    if (readable) {
		sm.checkRead(name);
	    }
	    if (writable) {
		sm.checkWrite(name);
	    }
	}
	*/
	try {
	    this.file = file.getCanonicalFile();
	    if (this.file.exists() && writable) {
		if (!this.file.isFile()  &!this.file.isDirectory()) {
		    throw new FileNotFoundException
			("could not find an ordinary file named \""
			 + file.getName() +"\"");
		}
	    }
	    if (readable && !writable) {
		if (!this.file.exists() || !this.file.isFile()) {
		    throw new FileNotFoundException
			("could not find an ordinary file named \""
			 + file.getName() +"\"");
		}
	    }
	} catch (FileNotFoundException efnf) {
	    throw efnf;
	} catch (IOException eio) {
	    String msg = errorMsg("canonNotFound", file.getName());
	    throw new FileNotFoundException(msg);
	}
    }

    /**
     * Get an input stream for reading from the file.
     * @return an input stream for reading from the file specified by
     *         a constructor
     * @exception IOException an IO error occurred or this file accessor
     *         does not allow a file to be read
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public InputStream getInputStream() throws IOException {
	if (readable) {
	    try {
		return AccessController.doPrivileged
		    (new PrivilegedExceptionAction<InputStream>() {
			public InputStream run() throws IOException {
			    if (!file.isFile()) {
				String path = file.getCanonicalPath();
				throw new IOException
				    (errorMsg("notAFile", path));
			    }
			    InputStream is = new FileInputStream(file);
			    return is;
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (IOException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	}
	throw new IOException(errorMsg("fileAccess"));
    }

    /**
     * Get an output stream for writing to the file.
     * @return an output stream for writing to the file specified by
     *         a constructor
     * @exception IOException an IO error occurred or this file accessor
     *         does not allow a file to be written
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public OutputStream getOutputStream() throws IOException {
	if (writable) {
	    try {
		return AccessController.doPrivileged
		    (new PrivilegedExceptionAction<FileOutputStream>() {
			public FileOutputStream run()
			    throws IOException
			{
			    if (file.exists() && !file.isFile()) {
				String path = file.getCanonicalPath();
				throw new IOException
				    (errorMsg("notAFile", path));
			    }
			    FileOutputStream is =
				new FileOutputStream(file, appendable);
			    return is;
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (IOException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	}
	throw new IOException(errorMsg("fileAccess"));
    }

    /**
     * Open the file for random access.
     * @return an instance of RandomAccessFile, which provides random
     *         access to the file specified in a constructor
     * @exception IOException an IO error occurred or this file accessor
     *         does not allow a file to be read or written
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public RandomAccessFile getRandomAccessFile() throws IOException {
	if (!readable && appendable) {
	    throw new IOException(errorMsg("appendNotRead"));
	}
	if (!readable && !writable)
	    throw new IOException(errorMsg("notReadOrWrite"));
	String mode = (readable? "r": "");
	mode =  mode + (writable? "w": "");
	if (mode.length() == 0) {
	    throw new IOException(errorMsg("fileAccess"));
	}
	final String theMode = mode;
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<RandomAccessFile>() {
		    public RandomAccessFile run() throws IOException {
			if (file.exists() && !file.isFile()) {
			    throw new IOException
				(errorMsg("notAFile", file.getCanonicalPath()));
			}
			RandomAccessFile raf =
			    new RandomAccessFile(file, theMode);
			return raf;
		    }
		});
	} catch (PrivilegedActionException e) {
	    Exception ee = e.getException();
	    if (ee instanceof RuntimeException) {
		throw (RuntimeException) ee;
	    } else if (ee instanceof IOException) {
		throw (IOException) ee;
	    } else {
		String msg = errorMsg("unexpected");
		throw new RuntimeException(msg, ee);
	    }
	}
    }

    /**
     * Get a directory accessor when this file accessor refers to
     * a directory.
     * @return a directory accessor; null if the file is not a directory
     *         or if this accessor is not readable.
     * @exception IOException an IO error occurred
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public DirectoryAccessor getDirectoryAccessor()
	throws IOException
    {
	return getDirectoryAccessor(false);
    }
    /**
     * Get a directory accessor when this file accessor refers to
     * a directory, specifying if the directory accessor is read only.
     * @param readOnly true if this directory accessor is read only;
     *        false otherwise
     * @return a directory accessor; null if the file is not a directory
     *         or if this accessor is not readable.
     * @exception IOException an IO error occurred
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public DirectoryAccessor getDirectoryAccessor(boolean readOnly)
	throws IOException
    {
	if (readable) {
	    try {
		return AccessController.doPrivileged
		    (new PrivilegedExceptionAction<DirectoryAccessor>() {
			public DirectoryAccessor run() throws IOException {
			    if (!file.isDirectory()) {
				return null;
			    }
			    boolean rd = readable && file.canRead();
			    boolean wrt = writable && file.canWrite();
			    return new DirectoryAccessor
				(file, (wrt == false) || readOnly);
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (FileNotFoundException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	}
	return null;
    }

    /**
     * Delete the file corresponding to this file accessor.
     * The directory access must allow a file to be written, as
     * must the corresponding file.
     * @return true if the deletion was successful; false if not
     * @exception IOException an IO error occurred
     * @exception IllegalStateException this file accessor is not writable
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean delete() throws IOException, IllegalStateException  {
	if (writable) {
	    try {
		return AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Boolean>() {
			public Boolean run()
			    throws IOException
			{
			    if (file.exists()) {
				if (!file.isFile()
				    && !file.isDirectory()
				    && !file.canWrite()) {
				    String path = file.getCanonicalPath();
				    throw new IOException
					(errorMsg("notAFile", path));
				}
				return file.delete();

			    } else {
				return false;
			    }
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (IOException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	} else {
	    throw new IllegalStateException(errorMsg("deleteForbiddenFA"));
	}
    }

    /**
     * Move the file associated with this file accessor to the file specified
     * by another file accessor.
     * @param fa the file accessor for the target file
     * @param options copy options, each of which is either
     *        {@link StandardCopyOption#ATOMIC_MOVE}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced because the REPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see StandardCopyOption
     * @see Files
     * @see CopyOption
     */
    public void move(FileAccessor fa, CopyOption... options )
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
    {
	if (writable && fa.writable) {
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			public Void run()
			    throws IOException
			{
			    if (file.exists()) {
				if (!file.isFile()
				    && !file.isDirectory()
				    && !file.canWrite()) {
				    String path = file.getCanonicalPath();
				    throw new IOException
					(errorMsg("notAFile", path));
				}
				Path start = file.toPath();
				Path target = fa.file.toPath();
				Files.move(start, target, options);
				return (Void)null;
			    } else {
				throw new IOException(errorMsg("noFile", file));
			    }
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (IOException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	} else {
	    throw new IllegalStateException(errorMsg("moveForbiddenFA"));
	}
    }

    /**
     * Copy the file associated with this file accessor to the file specified
     * by another file accessor.
     * @param fa the file accessor for the target file
     * @param options copy options, each of which is either
     *        {@link LinkOption#NOFOLLOW_LINKS}
     *        {@link StandardCopyOption#COPY_ATTRIBUTES}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced because the REPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see Files
     * @see CopyOption
     * @see StandardCopyOption
     */
    public void copy(FileAccessor fa, CopyOption... options )
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
    {
	if (readable && fa.writable) {
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			public Void run()
			    throws IOException
			{
			    if (file.exists()) {
				if (!file.isFile()
				    && !file.isDirectory()
				    && !file.canWrite()) {
				    String path = file.getCanonicalPath();
				    throw new IOException
					(errorMsg("notAFile", path));
				}
				Path start = file.toPath();
				Path target = fa.file.toPath();
				Files.copy(start, target, options);
				return (Void)null;
			    } else {
				throw new IOException(errorMsg("noFile", file));
			    }
			}
		    });
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		if (ee instanceof RuntimeException) {
		    throw (RuntimeException) ee;
		} else if (ee instanceof IOException) {
		    throw (IOException) ee;
		} else {
		    String msg = errorMsg("unexpected");
		    throw new RuntimeException(msg, ee);
		}
	    }
	} else {
	    throw new IllegalStateException(errorMsg("copyForbiddenFA"));
	}
    }
}

//  LocalWords:  exbundle RandomAccessFile ul li accessor accessors
//  LocalWords:  FileAccessor getInputStream getOutputStream JVM
//  LocalWords:  getRandomAccessFile SecurityException FileAccessor's
//  LocalWords:  codebase lastModified IOException nullArg notAFile
//  LocalWords:  NullPointerException appendable readOnly CopyOption
//  LocalWords:  IllegalStateException deleteForbiddenFA noFile
//  LocalWords:  StandardCopyOption UnsupportedOperationException
//  LocalWords:  FileAlreadyExistsException moveForbiddenFA NOFOLLOW
//  LocalWords:  DirectoryNotEmptyException LinkOption
//  LocalWords:  copyForbiddenFA
