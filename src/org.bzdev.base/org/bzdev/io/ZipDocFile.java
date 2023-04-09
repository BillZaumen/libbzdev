package org.bzdev.io;
import java.net.URLDecoder;
import java.util.zip.*;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.nio.charset.Charset;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.util.IteratorEnumeration;
import org.bzdev.util.EncapsulatingIterator;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Class to read entries in a zip-based file format with an
 * embedded media type. These files are typically created by
 * using {@link ZipDocWriter} or one of its subclasses.
 * A number of file formats are based on the Zip archive format
 * including Java JAR files and Open Document files. The file
 * format supported by this class is one in which documents based
 * on Zip archives are self-labeled with their Media types.
 * <P>
 * The Zip Document format is a Zip archive whose initial entry is a
 * directory name, "META-INF/", with a size of 0, and with an extra
 * field for that initial entry denoting a media type.  By convention,
 * the first element in the extra field for the first entry should be
 * the one providing the media type, with a two-byte header ID of
 * 0xFACE (stored in little-endian order). The media type uses UTF-8
 * encoding but legal characters in a media type make this compatible
 * with U.S.  ASCII. The media type is not null-terminated. This
 * results in the following byte sequences for the first portion of a
 * file:
 * <blockquote>
 * <pre>
 * Bytes 0 to 3: 50 4B 03 04
 * Bytes 8 to 9:  00 00
 * Bytes 14 to 25: 00 00 00 00 00 00 00 00 00 00 00 00
 * Bytes 26 to 27: 09 00
 * Bytes 28 to 29: (4 + the length of the media type)
 *                 in little-endian byte order; a larger value if
 *                 other information is included
 * Bytes 30 to 38: the characters "META-INF/"  (in UTF-8 encoding)
 * Bytes 39 to 40: CE FA  (0xFACE in little-endian byte order)
 * Bytes 41 to 42: the length of the media type in little-endian order
 * Bytes 43 to (43 + mtlen):  the characters making up the media type
 *                 encoded using UTF-8, where mtlen is the number of
 *                 characters in the media type
 * </pre>
 * </blockquote>
 * <P>
 * The file can be read by any software that can process ZIP files.
 * Applications using this file format can store data in the
 * META-INF directory, typically meta data.  The rationale for this
 * format is to make it easy for classing engines or similar software
 * to determine the a document type.
 * <P>
 * In a few cases (e.g., the files generated by
 * {@link org.bzdev.gio.ImageSequenceWriter}), the zip file
 * represent a sequence of objects, and some subsequences may consist
 * of the same object repeated multiple times. To store these
 * efficiently, the ZIP entry for the first can be tagged with a
 * repetition count, provided in the method
 * {@link ZipDocWriter#nextOutputStream(String,boolean,int,int)}.
 * The names chosen
 * for the entry should normally be such that the missing items can be
 * filled in without risk of a name conflict. The tag is a ZIP-file
 * extra header whose ID is 0xFCDA, whose length is 4, and whose value
 * is a 32-bit positive integer, with all three fields stored in
 * little-endian byte order, the normal convention for ZIP files.
 * <P>
 * When creating a ZipDocFile, several entry names are reserved. These
 * are "META-INF/", "META-INF/counters", and "META-INF/repetitionMap".
 * The entry META-INF/counters contains two 32-bit two's complement
 * integers in little-endian byte order.  The first of these two
 * integers contains the actual number of ZIP entries in the ZIP file,
 * excluding those whose names start with "META-INF/".  The second
 * contains the number of entries, excluding those whose names start with
 * "META-INF/", and including repetitions.
 * <P>
 * The reserved entry "META-INF/repetitionMap" is a US-ASCII file using
 * CRLF as a newline separator.  The line contains two values: an
 * entry name and the actual entry name, separated by a space. Each
 * of these names is URL encoded with the unencoded names using a
 * UTF-8 character set.  The repetitionMap entry might not be present
 * if the repetition count is 1 for all entries.  A repetition count of
 * 1 is the default value - the count includes the original entry.
 * Entries for which the repetition count is 1 are not present in a
 * repetitionMap entry.
 */
public class ZipDocFile extends ZipFile {

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    /**
     * Open a ZipDocFile for reading given the specified File object.
     * @param file - the file to be opened for reading
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     */
    public ZipDocFile(File file) throws ZipException, IOException {
	super(file);
    }

    /**
     * Open a ZipDocFile for reading given the specified File object and
     * charset.
     * @param file - the file to be opened for reading
     * @param charset - the charset used to decode the Zip entry names and
     *        comments (ignored if the language-encoding bit of the ZIP
     *        entry's general-purpose flag is set)
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     */
    public ZipDocFile(File file, Charset charset)
	throws ZipException, IOException
    {
	super(file, charset);
    }

    /**
     * Open a ZipDocFile for reading given the specified File object and a
     * specified mode.
     * @param file - the file to be opened for reading
     * @param mode - the mode in which the file is to be opened
     *        ({@link ZipFile#OPEN_READ} or 
     *        {@link ZipFile#OPEN_READ}|{@link ZipFile#OPEN_DELETE})
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     * @exception IllegalArgumentException - if the mode argument is invalid
     */
    public ZipDocFile(File file, int mode) throws ZipException, IOException {
	super(file, mode);
    }

    /**
     * Open a ZipDocFile for reading given the specified File object and a
     * specified mode.
     * @param file - the file to be opened for reading
     * @param mode - the mode in which the file is to be opened
     *        ({@link ZipFile#OPEN_READ} or 
     *        {@link ZipFile#OPEN_READ}|{@link ZipFile#OPEN_DELETE})
     * @param charset - the charset used to decode the Zip entry names and
     *        comments (ignored if the language-encoding bit of the ZIP
     *        entry's general-purpose flag is set)
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     * @exception IllegalArgumentException - if the mode argument is invalid
     */
    public ZipDocFile(File file, int mode, Charset charset)
	throws ZipException, IOException
    {
	super(file, mode, charset);
    }

    /**
     * Open a ZipDocFile for reading given the name of a file.
     * @param name - the name of the file to be opened for reading
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     */
    public ZipDocFile(String name) throws ZipException, IOException {
	super(name);
    }

    /**
     * Open a ZipDocFile for reading given the name of a file and a
     * charset.
     * @param name - the name of a file to be opened for reading
     * @param charset - the charset used to decode the Zip entry names and
     *        comments (ignored if the language-encoding bit of the ZIP
     *        entry's general-purpose flag is set)
     * @exception ZipException - if a ZIP format error has occurred
     * @exception IOException - if an I/O error has occurred
     */
    public ZipDocFile(String name, Charset charset)
	throws ZipException, IOException
    {
	super(name, charset);
    }

    /**
     * Get this ZipDocFile's media type.
     * The value is the media type embedded in this file.
     * @return the media type (MIME type) specified when the file was created
     */
    public String getMimeType() {
	ZipEntry entry = getEntry("META-INF/");
	if (entry == null) return null;
	byte[] extraHeaders = entry.getExtra();
	if (extraHeaders == null) return null;
	int index = 0;
	int limit = extraHeaders.length - 4;
	while (index < limit &&
	       !(extraHeaders[index] == (byte)0xce
		 && extraHeaders[index+1] == (byte)0xfa)) {
	    int length = ((int)(extraHeaders[index+2]) & 0xff)
		| ((((int)extraHeaders[index+3]) << 8) & 0xff00);
	    index += length + 4;
	}
	if (index < limit) {
	    int length = ((int)(extraHeaders[index+2]) & 0xff)
		| ((((int)extraHeaders[index+3]) << 8) & 0xff00);
	    try {
		return new String(extraHeaders, 4, length, "US-ASCII");
	    } catch (UnsupportedEncodingException e) {
		throw new UnexpectedExceptionError(e);
	    }
	} else {
	    return null;
	}
    }

    boolean needUpdate = true;
    int noRepeatEntryCount = 0;
    int entryCount = 0;
    LinkedHashMap<String,String> fileNameMap = null;

    static class ZipDocFileEntry extends ZipEntry {
	ZipEntry entry;
	private String name;
	ZipDocFileEntry(String name, ZipEntry entry) {
	    super(entry);
	    this.name = name;
	    this.entry = entry;
	}

	@Override
	public Object clone() {
	    Object obj = super.clone();
	    ZipDocFileEntry object = (ZipDocFileEntry) obj;
	    object.entry = (ZipEntry)(entry.clone());
	    return object;
	}

	@Override
	public String getName() {
	    return name;
	}
	ZipEntry getEntry() {return entry;}
    }

    @Override
    public InputStream getInputStream(ZipEntry entry) throws IOException {
	if (entry instanceof ZipDocFileEntry) {
	    ZipDocFileEntry zentry = (ZipDocFileEntry) entry;
	    entry = zentry.getEntry();
	}
	return super.getInputStream(entry);
    }

    @Override
    public ZipEntry getEntry(String name) throws IllegalStateException {
	ZipEntry entry = super.getEntry(name);
	if (entry == null) {
	    try {
		setup();
	    } catch(IOException e) {
		String msg = errorMsg("ZipDocFileSetup");
		throw new IllegalStateException(msg, e);
	    }
	    if (fileNameMap != null) {
		String nm = fileNameMap.get(name);
		if (nm == null) return null;
		entry = super.getEntry(nm);
		if (entry == null) return null;
		entry = new ZipDocFileEntry(name, entry);
	    }
	}
	return entry;
    }


    @Override
    public Enumeration<? extends ZipEntry> entries()
	throws IllegalStateException
    {
	try {
	    setup();
	} catch(IOException e) {
	    String msg = errorMsg("ZipDocFileSetup");
	    throw new IllegalStateException(msg, e);
	}
	if (fileNameMap == null) {
	    return super.entries();
	}
	Iterator<ZipEntry> it =
	     new EncapsulatingIterator<ZipEntry,String>(fileNameMap.keySet()
							.iterator()) {
	    public ZipEntry next() {
		String name = encapsulatedNext();
		return getEntry(name);
	    }
	};
	return new IteratorEnumeration<ZipEntry>(it);
    }

    private void setup() throws IOException {
	if (needUpdate) {
	    Enumeration<? extends ZipEntry>entries = super.entries();
	    while (entries.hasMoreElements()) {
		ZipEntry ent = entries.nextElement();
		String name = ent.getName();
		if (!name.startsWith("META-INF/")) {
		    noRepeatEntryCount++;
		    entryCount += getRepetitionCount(ent);
		}
	    }
	    ZipEntry entry = super.getEntry("META-INF/repetitionMap");
	    if (entry != null) {
		int n = entryCount - noRepeatEntryCount;
		int size = Math.round(n * 1.5F);
		if (size < 16) size = 16;
		HashMap<String,LinkedList<String>> map =
		    new HashMap<>(size);
		InputStream is = getInputStream(entry);
		Reader rd = new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(rd);
		String line = null;
		while ((line = reader.readLine()) != null) {
		    String[] tokens = line.split(" +");
		    String newName =
			URLDecoder.decode(tokens[0].trim(), "UTF-8");
		    String origName =
			URLDecoder.decode(tokens[1].trim(), "UTF-8");
		    LinkedList<String> list = map.get(origName);
		    if (list == null) {
			list = new LinkedList<String>();
			map.put(origName,list);
		    }
		    list.add(newName);
		}
		is.close();
		entries = super.entries();
		size = Math.round(entryCount*1.5F);
		if (size < 16) size = 16;
		fileNameMap = new LinkedHashMap<>(size);
		while (entries.hasMoreElements()) {
		    ZipEntry ent = entries.nextElement();
		    String origName = ent.getName();
		    fileNameMap.put(origName, origName);
		    LinkedList<String> list = map.get(origName);
		    int rcount = getRepetitionCount(ent);
		    if (list != null) {
			int sz = list.size();
			if (rcount != sz+1) {
			    String msg =
				errorMsg("repCount", origName, sz, rcount);
			    throw new IOException(msg);
			}
			for (String newName: list) {
			    fileNameMap.put(newName, origName);
			}
		    } else {
			if (rcount != 1) {
			    String msg = errorMsg("missingRepEntry", origName);
			    throw new IOException(msg);
			}
		    }
		}
	    }
	    needUpdate = false;
	}
    }

    /**
     * Get the number of entries in this file including repetitions.
     * The count excludes entries whose names begin with "META-INF/".
     * Those entries, meeting the criteria given above, that
     * have a repetition count of n contribute a value of n to the
     * count returned.
     * @return the number of entries
     * @throws IOException if an IO error occurred
     */
    public int getRequestedEntryCount() throws IOException {
	setup();
	return entryCount;
    }

    /**
     * Get the number of entries stored in this file.
     * The count excludes entries whose names begin with "META-INF/".
     * The repetition count associated with each of these entries is
     * ignored.
     * @return the number of entries
     * @throws IOException if an IO error occurred
     */
    public int getActualEntryCount() throws IOException {
	setup();
	return noRepeatEntryCount;
    }

    /**
     * Test if an entry is an actual entry in the ZIP file.
     * The entry must be one associated with an instance of a ZipDocFile.
     * The argument must not be null.
     * @param entry a ZIP-file entry
     * @return true if the entry is one that exists in the ZIP file; false
     *         if it corresponds to an alternate name for an existing entry
     */
    public static boolean isActualEntry(ZipEntry entry) {
	return !(entry instanceof ZipDocFileEntry);
    }

    /**
     * Get the actual name of a ZIP entry.
     * If an entry was created by calling the {@link ZipDocWriter}
     * method {@link ZipDocWriter#repeatFile(String)}, this method
     * will return the name of the entry repeated, not the name
     * passed to {@link ZipDocWriter#repeatFile(String)}.
     * @param entry a ZIP-file entry
     * @return the name of corresponding entry that actually exists
     *         in the ZIP file
     */
    public static String getActualName(ZipEntry entry) {
	if (entry instanceof ZipDocFileEntry) {
	    entry = ((ZipDocFileEntry)entry).getEntry();
	}
	return entry.getName();
    }

    /**
     * Get the repetition count for a specific zip-file entry.
     * Repetition counts default to 1, but alternative values
     * may be specified by calls to the {@link ZipDocWriter} method
     * {@link ZipDocWriter#nextOutputStream(String,boolean,int,int)}.
     * @param entry a zip entry
     * @return the repetition count for the actual entry corresponding to
     *         this entry
     */
    public static int getRepetitionCount(ZipEntry entry) {
	byte[] extraHeaders = entry.getExtra();
	if (extraHeaders == null) {
	    return 1;
	}
	int index = 0;
	int limit = extraHeaders.length - 4;
	while (index < limit &&
	       !(extraHeaders[index] == (byte)0xda
		 && extraHeaders[index+1] == (byte)0xfc
		 && extraHeaders[index+2] == (byte)4
		 && extraHeaders[index+3] == (byte)0)) {
	    int length = ((int)(extraHeaders[index+2]) & 0xff)
		| ((((int)extraHeaders[index+3]) << 8) & 0xff00);
	    index += length + 4;
	}
	if (index <= limit-4) {
	    int mask = 0xff;
	    int count = ((int)(extraHeaders[index+4])) & mask;
	    count |= (((int)(extraHeaders[index+5])) & mask) << 8;
	    count |= (((int)(extraHeaders[index+6])) & mask) << 16;
	    count |= (((int)(extraHeaders[index+7])) & mask) << 24;
	    return (count > 1)? count: 1;
	} else {
	    return 1;
	}
    }
}
//  LocalWords:  ZipDocWriter xFACE endian UTF blockquote pre mtlen
//  LocalWords:  subsequences nextOutputStream boolean xFCDA CRLF
//  LocalWords:  ZipDocFile repetitionMap unencoded ZipException
//  LocalWords:  IOException charset ZipFile SecurityException
//  LocalWords:  checkRead checkDelete IllegalArgumentException
//  LocalWords:  ZipDocFileSetup repeatFile
