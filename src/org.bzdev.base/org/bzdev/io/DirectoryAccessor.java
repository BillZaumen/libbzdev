package org.bzdev.io;
import java.io.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.CopyOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.LinkOption;
import java.security.*;
import java.util.ArrayList;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Class to provide access to a directory in the presence of a security
 * manager. The constructors allow access to a directory based on the
 * security policy when the constructor is called.  The methods allow
 * access to files and directories based on the permissions granted to
 * the DirectoryAccessor class itself.
 */
public class DirectoryAccessor {

    File dir;
    private boolean readable = true;
    private boolean writable = true;

    /**
     * Determine if this directory accessor allows files to be read or
     * existing subdirectories to be accessed with a directory accessor
     * that allows reading.
     * <P>
     * Note: while the caller cannot explicitly configure a directory
     * accessor to prevent reading, a security manager can.
     * @return true if the directory accessor allows files to be written
     *         or created; false otherwise
     */
    public boolean allowsReading() {return readable;}

    /**
     * Determine if this directory accessor allows files in its directory
     * to be written or new subdirectories to be created with that property.
     * <P>
     * Note: a directory accessor will not allow writing if it was created
     * in read-only mode or if a security manager forbids writing.
     * @return true if the directory accessor allows files to be written
     *         or created; false otherwise
     */
    public boolean allowsWriting() {return writable;}

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    /**
     * Constructor given a directory name.
     * @param dirname the directory name
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     */
    public DirectoryAccessor(String dirname) throws IOException {
 	this((dirname == null)? (File)null: new File(dirname));
   }

    /**
     * Constructor given a File.
     * @param dir the directory
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public DirectoryAccessor(File dir)
	throws IOException
    {
	if (dir == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	/*
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    String name = (dir != null? dir.getPath(): null);
	    try {
		sm.checkRead(name);
		readable = true;
	    } catch (Exception e) {
		readable = false;
	    }
	    try {
		sm.checkWrite(name);
		writable = true;
	    } catch (Exception e) {
		writable = false;
	    }
	    if (!readable && !writable) {
		throw new FileNotFoundException
		    (errorMsg("directoryAccess", name));
	    }
	}
	*/
	if (dir.exists() && !dir.isDirectory()) {
	    throw new FileNotFoundException
		(errorMsg("notDirectory", dir.getName()));
	}
	try {
	    this.dir = dir.getCanonicalFile();
	    this.dir.mkdirs();
	} catch (IOException ieo) {
	    throw new FileNotFoundException
		(errorMsg("canonicalForDir", dir.getName()));
	}
    }

    /**
     * Constructor given a directory name and mode.
     * @param dirname the directory name
     * @param readOnly true if the directory accessor does not permit
     *        new files to be added to the directory or existing files
     *        to be written
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     */
    public DirectoryAccessor(String dirname, boolean readOnly)
	throws IOException
    {
 	this(((dirname == null)? (File)null: new File(dirname)), readOnly);
   }


    /**
     * Constructor given a File and mode.
     * @param dir the directory
     * @param readOnly true if the directory accessor does not permit
     *        new files to be added to the directory or existing files
     *        to be written
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public DirectoryAccessor(File dir, boolean readOnly)
	throws IOException
    {
	if (dir == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	/*
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    String name = (dir != null? dir.getPath(): null);
	    try {
		sm.checkRead(name);
		readable = true;
	    } catch (Exception e) {
		readable = false;
	    }
	    try {
		sm.checkWrite(name);
		writable = true;
	    } catch (Exception e) {
		writable = false;
	    }
	}
	*/
	if (readOnly) writable = false;
	if (!readable && !writable) {
	    throw new FileNotFoundException
		(errorMsg("directoryAccess", dir.getName()));
	}
	if (dir.exists() && !dir.isDirectory()) {
	    throw new FileNotFoundException
		(errorMsg("notDirectory", dir.getName()));
	}
	try {
	    this.dir = dir.getCanonicalFile();
	    this.dir.mkdirs();
	} catch (IOException ieo) {
	    throw new FileNotFoundException
		(errorMsg("canonicalForDir", dir.getName()));
	}
    }

    /**
     * Create a new FileAccessor for a file in this instance's directory.
     * The file must be specified by a relative path name or relative file
     * and may not include directory components.
     * @param file the name of the file
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     */
    public FileAccessor createFileAccessor(String file) throws IOException
    {
	return createFileAccessor(((file == null)? null: new File(file)), null);
    }


    /**
     * Create a new FileAccessor for a file in this instance's directory
     * given a file-accessor mode.
     * The file must be specified by a relative path name or relative file
     * and may not include directory components.
     * @param file the name of the file
     * @param mode the file-accessor mode (a combination of "r", "w", and
     *        "a" as specified by constructors for
     *        {@link FileAccessor FileAccessor}
     */
    public FileAccessor createFileAccessor(String file, String mode)
	throws IOException
    {
	return createFileAccessor(((file == null)? null: new File(file)), mode);
    }

    /**
     * Create a new FileAccessor given a File for this instance's directory.
     * The file must be specified by a relative path name or relative file
     * and may not include directory components.
     * @param file the file to access
     */
    public FileAccessor createFileAccessor(final File file)
	throws IOException
    {
	String mode = (readable && writable)? "rw":
	    (readable? "r": (writable? "w": ""));
	return createFileAccessor(file, mode);
    }

    /**
     * Create a new FileAccessor given a File for this instance's directory
     * and given a file-accessor mode.
     * The file must be specified by a relative path name or relative file
     * and may not include directory components.
     * @param file the file to access
     * @param mode the requested file-accessor mode (a combination of
     *        "r", "w", and "a" as specified by constructors for
     *        {@link FileAccessor FileAccessor}
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception IllegalArgumentException the specified file has a directory
     *            component or is not absolute, or the mode included a
     *            "w" or "a" character when this directory accessor is
     *            read-only
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public FileAccessor createFileAccessor(final File file,
				final String mode)
	throws IOException, IllegalArgumentException
    {
	if (file == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (file.isAbsolute() || file.getParent() != null) {
	    throw new IllegalArgumentException
		(errorMsg("relativeNoParent", file.getName()));
	}
	if (!writable && mode != null) {
	    if (mode.contains("w") || mode.contains("a")) {
		throw new IllegalArgumentException
		    (errorMsg("illegalModeRO", file.getName()));
	    }
	}
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<FileAccessor>() {
		    public FileAccessor run() throws IOException {
			File ourfile = new File(dir, file.getName());
			if (!writable && !ourfile.exists()) {
			    throw new FileNotFoundException
				(errorMsg("noFile", file.getName()));
			}
			if (mode == null) {
			    String md = "";
			    if (readable && writable) {
				md = "rw";
			    } else if (readable) {
				md = "r";
			    } else if (writable) {
				md = "w";
			    }
			    return new FileAccessor(ourfile, md);
			} else {
			    return new FileAccessor(ourfile, mode);
			}
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
    

    /**
     * Get an input stream given a file name.
     * @param name the name of the file for which an input stream
     *        will be opened
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public InputStream getInputStream(String name) throws IOException {
	return getInputStream((name == null)? null: new File(name));
    }

    /**
     * Get an input stream given a File.
     * @param file the file for which an input stream will be opened
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public InputStream getInputStream(File file) throws IOException {
	return createFileAccessor(file).getInputStream();
    }

    /**
     * Get an output stream given a file name.
     * The file will be created if it does not already exist.
     * @param name the name of the file for which an output stream
     *        will be opened
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public OutputStream getOutputStream(String name) throws IOException {
	return getOutputStream((name == null)? null: new File(name));
    }

    /**
     * Get an output stream given a File.
     * The file will be created if it does not already exist.
     * @param file the file for which an output stream will be opened
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public OutputStream getOutputStream(File file) throws IOException {
	if (writable) {
	    return createFileAccessor(file).getOutputStream();
	} else {
	    throw new FileNotFoundException
		(errorMsg("noWriteRO", file.getName()));
	}
    }

    /**
     * Get a RandomAccessFile for a file specified by its name.
     * @param filename the file name
     * @param mode the mode ("r" or "w") defined by
     *        {@link FileAccessor FileAccessor}
     * @exception IOException the file could not be opened, possibly because
     *            a "r" or "a" character in the mode was used with a
     *            read-only directory accessor
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public RandomAccessFile getRandomAccessFile(String filename, String mode) 
	throws IOException
    {
	return getRandomAccessFile(((filename == null)? (File) null:
				    new File(filename)),
				   mode);
    }

    /**
     * Get a RandomAccessFile for a file.
     * @param file the file
     * @param mode the mode ("r" or "w") defined by
     *        {@link FileAccessor FileAccessor}
     * @exception IOException the file could not be opened, possibly because
     *            a "r" or "a" character in the mode was used with a
     *            read-only directory accessor
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public RandomAccessFile getRandomAccessFile(File file, String mode)
	throws IOException
    {
	if (!writable && mode != null &&
	    (mode.contains("w") || mode.contains("a"))) {
	    throw new IOException
		(errorMsg("writeAccessRO", file.getName()));
	}
	return new FileAccessor(file, mode).getRandomAccessFile();
    }

    /**
     * Add a new directory, specified by a relative path name.
     * The directory must be specified by a relative path name or
     * relative file and may not include multiple directory
     * components.
     * @param dir the name of the directory to create
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public DirectoryAccessor addDirectory(String dir)
	throws IOException, IllegalArgumentException,
	       RuntimeException, NullPointerException
    {
	return addDirectory((dir == null)? null: new File(dir));
    }


    /**
     * Add a new directory, specified by a relative File.
     * The directory must be specified by a relative path name or
     * relative file and may not include multiple directory
     * components. If the directory already exists, a directory
     * accessor for the existing directory will be returned.
     * This directory accessor must be writeable or an exception
     * will be thrown.
     * @param dir the directory to create
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception IllegalStateException this directory accessor does not
     *      allow directories to be added
     * @exception IllegalArgumentException the argument is a file
     *            with an absolute path name or has a parent directory
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     * @exception NullPointerException the argument was null
     */
    public DirectoryAccessor addDirectory(final File dir) 
	throws IOException, RuntimeException, IllegalArgumentException,
	       NullPointerException, IllegalStateException
    {
	if (dir == null) {
	    throw new NullPointerException(errorMsg("nullArg"));
	}
	if (dir.isAbsolute() || dir.getParent() != null) {
	    throw new IllegalArgumentException
		(errorMsg("relativeNoParent", dir.getName()));
	}
	if (!writable) {
	    throw new IllegalStateException(errorMsg("addForbidden"));
	}
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<DirectoryAccessor>() {
		    public DirectoryAccessor run()
			throws IOException
		    {
			File ourfile = new File(DirectoryAccessor.this.dir,
						dir.getName());
			File pf;
			try {
			    pf = ourfile.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			     FileNotFoundException nfe =
				 new FileNotFoundException
				 (errorMsg("noDirectory", dir.getName()));
			     nfe.initCause(e);
			     throw nfe;
			}
			if (!pf.equals(DirectoryAccessor.this.dir)) {
			    throw new FileNotFoundException
				(errorMsg("noDirectory", dir.getName()));
			}
			if (!writable && !ourfile.exists()) {
			    throw new FileNotFoundException
				(errorMsg("noDirectory", dir.getName()));
			}
			ourfile.mkdirs();
			return new DirectoryAccessor(ourfile);
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
     * Get a list of the names of files and subdirectories in the
     * directory associated with this directory accessor.
     * @return the names of the files and directories
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public String[] list() {
	    return AccessController.doPrivileged
		(new PrivilegedAction<String[]>() {
		    public String[] run() {
			return dir.list();
		    }
		});
    }

    /**
     * Generate a list of file accessors for the files in the directory
     * corresponding to this directory accesssor.
     * The file accessors included are ones for files that are readable
     * when this directory accessor allows reading, and for files that
     * are writeable when this directory allows writing.
     * @return the file accessors
     * @see #allowsReading()
     * @see #allowsWriting()
     * @exception IOException the directory corresponding to this
     *      directory accessor does not exist or is not readable
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public FileAccessor[] listFileAccessors() throws IOException {
	String mode = (readable && writable)? "rw":
	    (readable? "r": (writable? "w": ""));
	try {
	    return AccessController.doPrivileged
		(new PrivilegedExceptionAction<FileAccessor[]>() {
			public FileAccessor[] run() throws IOException {
			    File[] files = dir.listFiles();
			    ArrayList<FileAccessor> flist =
				new ArrayList<>(files.length);
			    for (int i = 0; i < files.length; i++) {
				File f = files[i];
				String ourmode = mode;
				if (!f.canRead()) {
				    ourmode = mode.replace("r","");
				}
				if (!f.canWrite()) {
				    ourmode = mode.replace("w","");
				}
				if (ourmode.length() > 0) {
				    flist.add(new FileAccessor(f, ourmode));
				}
			    }
			    FileAccessor[] fas = new FileAccessor[flist.size()];
			    return flist.toArray(fas);
			}
		    });
	} catch (PrivilegedActionException e) {
	    Exception ee = e.getException();
	    if (ee instanceof IOException) {
		throw (IOException) ee;
	    } else if (ee instanceof RuntimeException) {
		throw (RuntimeException) ee;
	    } else {
		String msg = errorMsg("unexpected");
		throw new RuntimeException(msg, ee);
	    }
	}
    }

    /**
     * Determine if a file in a directory provided by a directory accessor
     * is readable.
     * The name is the filename component, not the full path.
     * If this directory accessor does not allow files to be read,
     * this method will return false (unless an exception is thrown).
     * @param filename the name of a file or directory
     * @return true if the file or directory is readable; false otherwise
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean canRead(final String filename)
	throws IOException
    {
	Boolean result = Boolean.FALSE;
	try {
	    result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Boolean>() {
		    public Boolean run() throws FileNotFoundException {
			File file = new File(dir, filename);
			File pf;
			try {
			    pf = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			     FileNotFoundException nfe =
				 new FileNotFoundException
				 (errorMsg("noDirectory", file.getName()));
			     nfe.initCause(e);
			     throw nfe;
			}
			if (!pf.equals(dir)) {
			    String msg = errorMsg("unexpectedParent", pf, dir);
			    throw new FileNotFoundException(msg);
			}
			return readable && file.canRead();
		    }
		});
	    return result;
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

    /**
     * Determine if a file in a directory provided by a directory accessor
     * is writable.
     * The name is the filename component, not the full path.
     * If this directory accessor does not allow files to be written,
     * this method will return false (unless an exception is thrown).
     * @param filename the name of a file or directory
     * @return true if the file or directory is writable; false otherwise
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean canWrite(final String filename)
	throws IOException
    {
	Boolean result = Boolean.FALSE;
	try {
	    result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Boolean>() {
		    public Boolean run() throws FileNotFoundException {
			File file = new File(dir, filename);
			File pf;
			try {
			    pf = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			     FileNotFoundException nfe =
				 new FileNotFoundException
				 (errorMsg("noDirectory", file.getName()));
			     nfe.initCause(e);
			     throw nfe;
			}
			if (!pf.equals(dir)) {
			    String msg = errorMsg("unexpectedParent", pf, dir);
			    throw new FileNotFoundException(msg);
			}
			return writable && file.canWrite();
		    }
		});
	    return result;
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

    /**
     * Determine if a file name refers to a directory
     * The name is the filename component, not the full path.
     * @param filename the name of a directory or file name
     * @return true if the name refers to a directory; false otherwise
     * @exception IOException an IO error occurred, probably due to permissions
     *            or a missing directory component.
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean isDirectory(final String filename)
	throws IOException
    {
	Boolean result = Boolean.FALSE;
	try {
	    result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Boolean>() {
		    public Boolean run() throws FileNotFoundException {
			File file = new File(dir, filename);
			File pf;
			try {
			    pf = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			     FileNotFoundException nfe =
				 new FileNotFoundException
				 (errorMsg("noDirectory", file.getName()));
			     nfe.initCause(e);
			     throw nfe;
			}
			if (!pf.equals(dir)) {
			    String msg = errorMsg("unexpectedParent", pf, dir);
			    throw new FileNotFoundException(msg);
			}
			return file.isDirectory();
		    }
		});
	    return result;
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


    /**
     * Determine if a file name refers to a file or directory that exists
     * The name is the filename component, not the full path.
     * @param filename the name of a directory or file name
     * @return true if the name refers an existing file or  directory;
     *         false otherwise
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean exists(final String filename)
    {
	Boolean result = Boolean.FALSE;
	try {
	    result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Boolean>() {
		    public Boolean run() throws FileNotFoundException {
			File file = new File(dir, filename);
			File pf;
			try {
			    pf = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			     FileNotFoundException nfe =
				 new FileNotFoundException
				 (errorMsg("noDirectory", file.getName()));
			     nfe.initCause(e);
			     throw nfe;
			}
			if (!pf.equals(dir)) {
			    String msg = errorMsg("unexpectedParent", pf, dir);
			    throw new FileNotFoundException(msg);
			}
			return file.exists();
		    }
		});
	    return result;
	} catch (PrivilegedActionException e) {
	    Exception ee = e.getException();
	    if (ee instanceof RuntimeException) {
		throw (RuntimeException) ee;
	    } else if (ee instanceof FileNotFoundException) {
		return false;
	    } else {
		String msg = errorMsg("unexpected");
		throw new RuntimeException(msg, ee);
	    }
	}
    }

    /**
     * Delete a file or directory.
     * The name is the filename component, not the full path.
     * If this directory accessor does not allow files to be written,
     * this method will return false (unless an exception is thrown).
     * @param filename the name of a file or directory
     * @return true if the file or directory was deleted; false otherwise
     * @exception IllegalStateException this directory accessor does not
     *     allow files to be deleted
     * @exception SecurityException if DirectoryAccessor's codebase does not
     *      have the permissions needed for this method
     */
    public boolean delete(final String filename) throws IllegalStateException
    {
	if (!writable) {
	    throw new IllegalStateException(errorMsg("deleteForbidden"));
	}
	Boolean result = Boolean.FALSE;
	try {
	    result = AccessController.doPrivileged
		(new PrivilegedExceptionAction<Boolean>() {
		    public Boolean run() throws SecurityException {
			File file = new File(dir, filename);
			File pf;
			try {
			    pf = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
			    return false;
			}
			if (!pf.equals(dir)) {
			    return false;
			}
			if (!writable) {
			    return false;
			}
			return file.delete();
		    }
		});
	    return result;
	} catch (PrivilegedActionException e) {
	    Exception ee = e.getException();
	    if (ee instanceof SecurityException) {
		throw (SecurityException) ee;
	    } else if (ee instanceof RuntimeException) {
		throw (RuntimeException) ee;
	    } else {
		String msg = errorMsg("unexpected");
		throw new RuntimeException(msg, ee);
	    }
	}
    }

    /**
     * Move the file from the directory associated with this directory
     * accessor to another location in the same directory.
     * by another file accessor.
     * File names must not contain directory components.
     * @param f1 the name of the file to move
     * @param f2 the name of the new location.
     * @param options copy options, each of which is either
     *        {@link StandardCopyOption#ATOMIC_MOVE}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred, possibly due to permissions
     *      or a missing directory component
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced becuase the RPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see StandardCopyOption
     * @see Files
     * @see CopyOption
     */
    public void move(String f1, String f2, CopyOption... options)
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
    {
	FileAccessor fa1 = createFileAccessor(f1, "rw");
	FileAccessor fa2 = createFileAccessor(f2, "w");
	fa1.move(fa2, options);
    }

    /**
     * Copy a file from the directory associated with this directory accessor
     * to another location in the same directory.
     * File names must not contain directory components.
     * @param f1 the file to copy
     * @param f2 the name of the target file.
     * @param options copy options, each of which is either
     *        {@link LinkOption#NOFOLLOW_LINKS}
     *        {@link StandardCopyOption#COPY_ATTRIBUTES}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred, possibly due to permissions
     *      or a missing directory component
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced becuase the RPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see Files
     * @see CopyOption
     * @see StandardCopyOption
     */
    public void copy(String f1, String f2, CopyOption... options)
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
    {
	FileAccessor fa1 = createFileAccessor(f1, "r");
	FileAccessor fa2 = createFileAccessor(f2, "w");
	fa1.copy(fa2, options);
    }

    /**
     * Move the file from the directory associated with this directory
     * accessor to another location in a possibly differnt directory.
     * by another file accessor.
     * File names must not contain directory components.
     * @param f1 the name of the file to move
     * @param da the directory accessor for the target file's location
     * @param f2 the file name for the new location.
     * @param options copy options, each of which is either
     *        {@link StandardCopyOption#ATOMIC_MOVE}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred, possibly due to permissions
     *      or a missing directory component
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced becuase the RPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see StandardCopyOption
     * @see Files
     * @see CopyOption
     */
    public void move(String f1, DirectoryAccessor da, String f2,
		     CopyOption... options)
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
    {
	FileAccessor fa1 = createFileAccessor(f1, "rw");
	FileAccessor fa2 = da.createFileAccessor(f2, "w");
	fa1.move(fa2, options);
    }

    /**
     * Copy a file from the directory associated with this directory accessor
     * to a location in a possibly different directory.
     * File names must not contain directory components.
     * @param f1 the file to copy
     * @param f2 the name of the target file.
     * @param options copy options, each of which is either
     *        {@link LinkOption#NOFOLLOW_LINKS}
     *        {@link StandardCopyOption#COPY_ATTRIBUTES}
     *        or {@link StandardCopyOption#REPLACE_EXISTING}
     * @exception UnsupportedOperationException if an option is not recognized
     * @exception IOException an IO error occurred, possibly due to permissions
     *      or a missing directory component
     * @exception FileAlreadyExistsException if the target file exists but
     *      cannot be replaced becuase the RPLACE_EXISTING option was not
     *      specified
     * @exception DirectoryNotEmptyException if the target file exists but
     *       cannot be replaced because it is a non-empty directory
     * @exception SecurityException if FileAccessor's codebase does not
     *      have the permissions needed for this method
     * @see Files
     * @see CopyOption
     * @see StandardCopyOption
     */
    public void copy(String f1, DirectoryAccessor da, String f2,
		     CopyOption... options)
	throws IOException,
	       UnsupportedOperationException,
	       IllegalStateException
   {
	FileAccessor fa1 = createFileAccessor(f1, "r");
	FileAccessor fa2 = da.createFileAccessor(f2, "w");
	fa1.copy(fa2, options);
    }

}

//  LocalWords:  exbundle DirectoryAccessor dirname dir nullArg rw
//  LocalWords:  directoryAccess notDirectory canonicalForDir noFile
//  LocalWords:  readOnly accessor FileAccessor relativeNoParent
//  LocalWords:  IllegalArgumentException illegalModeRO noWriteRO
//  LocalWords:  RandomAccessFile filename IOException writeAccessRO
//  LocalWords:  noDirectory subdirectories FileNotFoundException
