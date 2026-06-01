package org.bzdev.ejws;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.SyncFailedException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.ResourceBundle;
import org.bzdev.io.LineReader;
import org.bzdev.io.CSVReader;
import org.bzdev.io.CSVWriter;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.ejws.lpack.SBLStore

/**
 * Persistent storage for SBL data and passwords. The constructor
 * will be given a file.  For a file <I>fname</I> in some directory,
 * files names #<I>fname</I> and <I>fname</I>~ may also be created
 * and are used to recover if the system goes down unexpectedly.
 */
public class SBLStore implements AutoCloseable {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.SBLStore");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    File file;
    FileDescriptor fd;
    CSVWriter w;
    static final int MAX_COUNTER = 10;
    static final long TIMEOUT = 10000;

    AtomicInteger counter = new AtomicInteger(0);

    // If we get an abnormally large number of new account requests in
    // rapid succession, we should back off on the number of file system
    // syncs.
    Thread monitor = new Thread(() -> {
	    try {
		for (;;) {
		    if (Thread.currentThread().isInterrupted()) {
			return;
		    }
		    Thread.sleep(TIMEOUT);
		    if (counter.getAndSet(0) >= MAX_COUNTER) {
			try {
			    fd.sync();
			} catch (SyncFailedException sfe){}
		    }
		}
	    } catch (InterruptedException ei) {
		return;
	    }
    });

    /**
     * Data stored for a user.
     */
    public static class Entry {
	public boolean pwmode;
	public String data;
	public boolean isActive;

	Entry(boolean pwmode, String data, boolean isActive) {
	    this.pwmode = pwmode;
	    this.data = data;
	    this.isActive = isActive;
	}

	/**
	 * Determine if the data field contains a password or
	 * SBL data.
	 * @return true for a password; false for SBL data
	 */
	public boolean getPWMode() {
	    return pwmode;
	}

	/**
	 * Get a user's data stored in this object
	 * @return either a password or {@link java.lang.String}-encoded
	 * SBL data.
	 */
	public String getData() {
	    return data;
	}

	/**
	 * Determine if a user is active.
	 * @return true if the user is active; false otherise
	 */
	public boolean isActive() {
	    return isActive;
	}

    }

    TreeMap<String,Entry> map = new TreeMap<>();
    TreeSet<String> set = new TreeSet<>();

    /**
     * Constructor.
     * for a file named "store", files name "#store" and
     * "store~" may be created temporarily.
     * @param file the file used for persistent storage
     */
    public SBLStore(File file) throws IOException {
	this.file = file;
	File parent = file.getParentFile();
	String fname = file.getName();
	File tmp = new File(parent, "#" + fname);
	File prev = new File(parent, fname + "~");
	Path fpath = file.toPath();
	Path tpath = tmp.toPath();
	Path ppath = prev.toPath();

	if (prev.exists() && prev.canRead()) {
	    try {
		Files.move(ppath, fpath, StandardCopyOption.ATOMIC_MOVE);
	    } catch (AtomicMoveNotSupportedException ea) {
		Files.move(ppath, fpath);
	    }
	}
	if (file.canRead()) {
	    CSVReader r = new CSVReader(new FileReader(file, UTF8),
					false,
					LineReader.Delimiter.CRLF);
	    String[] row;
	    while ((row = r.nextRow()) != null) {

		String name = row[0];
		if (row[1].length() == 0 && row[2].length() == 0
		    && row[3].length() == 0) {
		    map.remove(name);
		    set.remove(name);
		} else {
		    boolean pwmode = (row[1].length() == 0)? false:
			"true".equals(row[1]);
		    String data = row[2];
		    boolean isActive= (row[3].length() == 0)? false:
			"true".equals(row[3]);
		    Entry entry = map.get(name);
		    if (entry == null) {
			entry = new Entry(pwmode, data, isActive);
			map.put(name, entry);
			set.add(name);
		    } else {
			if (row[1].length() > 0) {
			    entry.pwmode = pwmode;
			    entry.data = data;
			}
			if (row[3].length() > 0) {
			    entry.isActive = isActive;
			}
		    }
		}
	    }
	    // now write it out to the tmp file.
	    w = new CSVWriter(new FileWriter(tmp, UTF8), 4, false,
			      LineReader.Delimiter.CRLF);
	    for (Map.Entry<String,Entry> entry: map.entrySet()) {
		String nm = entry.getKey();
		Entry ent = entry.getValue();
		w.writeRow(nm, ""+ent.pwmode, ent.data, ""+ent.isActive);
	    }
	    w.flush();
	    w.close();
	    try {
		Files.move(fpath, ppath, StandardCopyOption.ATOMIC_MOVE);
		Files.move(tpath, fpath, StandardCopyOption.ATOMIC_MOVE);
	    } catch (AtomicMoveNotSupportedException ea) {
		Files.move(fpath, ppath);
		Files.move(tpath, fpath);
	    }
	    prev.delete();
	    FileOutputStream os = new FileOutputStream(file, true);
	    fd = os.getFD();
	    w = new CSVWriter(new OutputStreamWriter(os, UTF8),
			      4, false, LineReader.Delimiter.CRLF);
	} else {
	    FileOutputStream os = new FileOutputStream(file, true);
	    fd = os.getFD();
	    w = new CSVWriter(new OutputStreamWriter(os, UTF8),
			      4, false, LineReader.Delimiter.CRLF);
	}
	// make this a daemon so it will not stop the VM from shutting
	// down.
	monitor.setDaemon(true);
	monitor.start();
    }

    /**
     * Get a stream of entry-set elements whose keys are user names
     * and whose values are the corresponding instance of
     * {@link SBLStore.Entry}.
     * @return the stream
     */
    public Stream<Map.Entry<String,SBLStore.Entry>> mapStream() {
	return map.entrySet().stream();
    }

    /**
     * Get users.
     * @param active true if only active users should be included; false if
     *        only non-active users should be included
     * @return a set of users
     */
    public synchronized Set<String>
	getUsers(Map<String,? extends EjwsAuthenticator.Entry> emap,
		 boolean active)
    {
	TreeSet<String> result = new TreeSet<>();
	// try {
	    for (String name: set) {
		if (emap.containsKey(name)
		    && emap.get(name).isActive() == active) {
		    result.add(name);
		}
	    }
	    /*
	    fd.sync();
	    CSVReader r = new CSVReader(new FileReader(file, UTF8),
					false,
					LineReader.Delimiter.CRLF);
	    String[] row;
	    while ((row = r.nextRow()) != null) {
		String name = row[0];
		boolean isActive= (row[3].length() == 0)? false:
		    "true".equals(row[3]);
		if (isActive == active) {
		    result.add(name);
		}
	    }
	    r.close();
	} catch (IOException eio) {
	    return null;
	}
	    */
	return result;
    }


    /**
     * Clean up the map so it will only contain new entries
     */
    public synchronized void clearMap() {
	map.clear();
    }

    public synchronized boolean containsUser(String name) {
	return set.contains(name);
    }

    /**
     * Set all fields for a user.
     * @param name the user name
     * @param pwmode the password mode (true if the data parameter is
     *               an basic-authentication password; false if it is
     *               SBL data encoded as a string
     * @param data the data
     * @param isActive true the user is active; false otherwise
     */
    public synchronized void append(String name, boolean pwmode,
		       String data, boolean isActive)
	throws IOException
    {
	if (set.contains(name)) {
	    throw new IOException(errorMsg("entryExists", name));
	}

	Entry entry = new Entry(pwmode, data, isActive);
	map.put(name, entry);
	set.add(name);

	w.writeRow(name, ""+pwmode, data, ""+isActive);
	w.flush();
	if (counter.getAndIncrement() < MAX_COUNTER) {
	    fd.sync();
	}
    }

    /**
     * Set the data field for a user.
     * @param name the user name
     * @param pwmode the password mode (true if the data parameter is
     *               an basic-authentication password; false if it is
     *               SBL data encoded as a string
     * @param data the data
     */
    public synchronized void append(String name, boolean pwmode, String data)
	throws IOException
    {
	if (set.contains(name)) {
	    throw new IOException(errorMsg("entryExists", name));
	}
	Entry entry = new Entry(pwmode, data, false);
	map.put(name, entry);
	set.add(name);
	
	w.writeRow(name, ""+pwmode, data, "");
	w.flush();
	if (counter.getAndIncrement() < MAX_COUNTER) {
	    fd.sync();
	}
    }

    /**
     * Make a user active.
     * @param name the user name
     */
    public synchronized void makeActive(String name)
	throws IOException
    {
	if (set.contains(name)) {
	    w.writeRow(name, "", "", "true");
	    w.flush();
	    if (counter.getAndIncrement() < MAX_COUNTER) {
		fd.sync();
	    }
	} else {
	    throw new IOException(errorMsg("noEntry", name));
	}
    }

    /**
     * Remove a user.
     * @param name the user name
     */
    public synchronized void removeUser(String name)
	throws IOException
    {
	if (set.contains(name)) {
	    w.writeRow(name, "", "", "");
	    w.flush();
	    if (counter.getAndIncrement() < MAX_COUNTER) {
		fd.sync();
	    }
	} else {
	    throw new IOException(errorMsg("noEntry", name));
	}
    }


    boolean closed = false;

    public synchronized String getSBLData(String name) {
	Entry entry = map.get(name);
	if (entry != null) {
	    if (entry.getPWMode() == false) {
		return entry.getData();
	    }
	}
	return null;
    }

    public synchronized String getPWData(String name) {
	Entry entry = map.get(name);
	if (entry != null) {
	    if (entry.getPWMode() == true) {
		return entry.getData();
	    }
	}
	return null;
    }

    /**
     * Close all output streams and terminate subprocesses.
     */
    public synchronized void close() throws IOException {
	try {
	    if (!closed) {
		try {
		    monitor.interrupt();
		} catch (Exception te) {}
		w.flush();
		fd.sync();
		w.close();
		w = null;
		fd = null;
		closed = true;
	    }
	} catch(Exception e) {
	} finally {
	    closed = true;
	}
    }
}
