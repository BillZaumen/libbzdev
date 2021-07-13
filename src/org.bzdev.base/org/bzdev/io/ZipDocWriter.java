package org.bzdev.io;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;
import java.util.zip.*;
import java.nio.charset.Charset;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.URLEncoder;

//@exbundle org.bzdev.io.lpack.IO

/**
 * Writer for documents in ZipDoc format.
 * A number of file formats are based on the Zip archive format
 * including Java JAR files and Open Document files. The file
 * format supported by this class is one in which documents based
 * on Zip archives are self-labeled with their media types.
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
 * Bytes 43 to (42 + mtlen):  the characters making up the media type
 *                 encoded using UTF-8, where mtlen is the number of
 *                 characters in the media type (only single-byte UTF-8
 *                 characters can occur in a media type.
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
 * {@link #nextOutputStream(String,boolean,int,int)}. The names chosen
 * for the entry should normally be such that the missing items can be
 * filled in without risk of a name conflict. The tag is a ZIP-file
 * extra header whose ID is 0xFCDA, whose length is 4, and whose value
 * is a 32-bit positive integer, with all three fields stored in
 * little-endian byte order, the normal convention for ZIP files.
 * <P>
 * Several entry names are reserved. These are "META-INF/" and
 * "META-INF/repetitionMap" The reserved entry
 * "META-INF/repetitionMap" is a US-ASCII file using CRLF as a newline
 * separator.  The line contains two values: an entry name and the
 * actual entry name, separated by a space. Each of these names is URL
 * encoded with the unencoded names using a UTF-8 character set.  The
 * repetitionMap entry might not be present if the repetition count is
 * 1 for all entries.  A repetition count of 1 is the default value -
 * the count includes the original entry.  Entries for which the
 * repetition count is 1 are not present in a repetitionMap entry.
 * <P>
 * Subclasses and other users of this class may add entries whose names
 * start with "META-INF" but must not add an entry whose name
 * matches a reserved name.
 */
public class ZipDocWriter implements AutoCloseable  {

    static String errorMsg(String key, Object... args) {
	return IoErrorMsg.errorMsg(key, args);
    }

    static class ZipDocOutputStream extends OutputStream {

	static String errorMsg(String key, Object... args) {
	    return IoErrorMsg.errorMsg(key, args);
	}

	ZipDocWriter writer;
	ZipOutputStream zos;
	boolean closed = false;
	ZipDocOutputStream(ZipOutputStream zos, ZipDocWriter writer) {
	    this.zos = zos;
	    this.writer = writer;
	}

	public void close() throws IOException {
	    if (closed) return;
	    zos.flush();
	    zos.closeEntry();
	    closed = true;
	    if (writer.lastos == this) writer.lastos = null;
	}
	public void flush() throws IOException{
	    if (closed) return;
	    zos.flush();
	}
	public void write(byte[] b) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    zos.write(b);
	}
	public void write(byte[] b, int off, int len) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    zos.write(b, off, len);
	}
	public void write(int b) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    zos.write(b);
	}
    }

    static class ZipDocByteArrayOutputStream extends OutputStream {
	ZipDocWriter writer;
	ZipOutputStream zos;
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	boolean closed = false;
	ZipEntry entry;

	static String errorMsg(String key, Object... args) {
	    return IoErrorMsg.errorMsg(key, args);
	}

	ZipDocByteArrayOutputStream(ZipOutputStream zos, ZipEntry entry,
				    ZipDocWriter writer) {
	    this.zos = zos;
	    this.entry = entry;
	    this.writer = writer;
	}

	public void close() throws IOException {
	    if (closed) return;
	    bos.flush();
	    int sz = bos.size();
	    byte[] array = bos.toByteArray();
	    CRC32 crc  = new CRC32();
	    crc.update(array);
	    entry.setSize(sz);
	    entry.setCrc(crc.getValue());
	    zos.putNextEntry(entry);
	    zos.write(array);
	    array = null; bos.close(); bos = null;
	    zos.closeEntry();
	    closed = true;
	    if (writer.lastos == this) writer.lastos = null;
	}
	public void flush() throws IOException {
	    if (closed) return;
	    bos.flush();
	}
	public void write(byte[] b) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    bos.write(b);
	}
	public void write(byte[] b, int off, int len) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    bos.write(b, off, len);
	}
	public void write(int b) throws IOException {
	    if (closed) throw new IOException(errorMsg("streamClosed"));
	    bos.write(b);
	}
    }

    ZipOutputStream zos;

    static byte[] getExtra(String mimeType, byte[] extras) {
	int elen = (extras == null)? 0: extras.length;
	int mlen = 4 + mimeType.length();
	byte[] extra = new byte[mlen + elen];
	if (elen > 0) {
	    System.arraycopy(extras, 0, extra, mlen, elen);
	}
	extra[0] = (byte)0xce;
	extra[1] = (byte)0xfa;
	int len = mimeType.length();
	extra[2] = (byte)(len % 256);
	extra[3] = (byte)(len / 256);

	for (int i = 0; i < mimeType.length(); i++) {
	    char ch = mimeType.charAt(i);
	    if ((int)ch > 256)
		throw new IllegalArgumentException
		    (errorMsg("MIMETypeChars"));
	    extra[i+4] = (byte) mimeType.charAt(i);
	}
	return extra;
    }

    static byte[] getExtra(int count, byte[] extras) {
	int elen = (extras == null)? 0: extras.length;
	byte[] extra = new byte[8 + elen];
	if (elen > 0) {
	    System.arraycopy(extras, 0, extra, 8, elen);
	}
	extra[0] = (byte) 0xda;
	extra[1] = (byte)0xfc;
	extra[2] = 4;
	extra[3] = 0;
	extra[4] = (byte)(count & 0xff);
	count = count >> 8;
	extra[5] = (byte)(count & 0xff);
	count = count >> 8;
	extra[6] = (byte)(count & 0xff);
	count = count >> 8;
	extra[7] = (byte)(count & 0xff);
	return extra;
    }

    static final long CRC_ZERO = 0;

    /**
     * Constructor.
     * @param os the output stream on which a zip-document file will be written
     * @param mimeType the file's media type
     * @exception IOException an error occurred during processing
     */
    public ZipDocWriter(OutputStream os, String mimeType) throws IOException {
	zos = new ZipOutputStream(os, Charset.forName("UTF-8"));
	zos.setMethod(ZipOutputStream.STORED);
	zos.setLevel(0);
	ZipEntry entry = new ZipEntry("META-INF/");
	entry.setSize(0);
	entry.setCrc(CRC_ZERO);
	if (mimeType != null) {
	    mimeType = mimeType.trim();
	    if (mimeType.length() > 0) {
		entry.setExtra(getExtra(mimeType, null));
	    }
	}
	zos.putNextEntry(entry);
	zos.closeEntry();
    }

   /**
     * Constructor with extra field.
     * The extra header field should not include the media-type data
     * that this class (and its superclass) will provide.
     * @param os the output stream on which a zip-document file will be written
     * @param mimeType the file's media type
     * @param extras the extra field, excluding the header and value
     *        describing the file's media type
     * @exception IOException an error occurred during processing
     */
    public ZipDocWriter(OutputStream os, String mimeType,
			byte[] extras) throws IOException
    {
	zos = new ZipOutputStream(os, Charset.forName("UTF-8"));
	zos.setMethod(ZipOutputStream.STORED);
	zos.setLevel(0);
	ZipEntry entry = new ZipEntry("META-INF/");
	entry.setSize(0);
	entry.setCrc(CRC_ZERO);
	if (mimeType != null) {
	    mimeType = mimeType.trim();
	    if (mimeType.length() > 0) {
		entry.setExtra(getExtra(mimeType, extras));
	    }
	}
	zos.putNextEntry(entry);
	zos.closeEntry();
    }

    OutputStream lastos = null;
    int entryCount = 0;
    int noRepeatEntryCount = 0;

    int currentEntryCount = 0;
    String currentName = null;

    LinkedHashMap<String,String> fileNameMap = null;

    /**
     * Provide the name for a repeated entry and create that entry.
     * When an entry is created with a repetition count whose value is
     * <code>count</code> in a call to
     * {@link #nextOutputStream(String,boolean,int,int)} or
     * {@link #nextOutputStream(String,boolean,int,int,byte[])}, then
     * repeatFiles method must be called <code>count-1</code> times before a
     * another call to nextOutputStream or a call to {@link #close()}.
     * @param nextName the name of a repeated entry
     * @exception IllegalStateException if repeatFile cannot be called at
     *        this point
     * @exception IllegalArgumentException if the name of the stream is
     *        a reserved name or null, or if a subclass restricts the name
     *        in some way
     */
    public void repeatFile(String nextName)
	throws IllegalArgumentException, IllegalStateException
    {
	if (fileNameMap == null) {
	    throw new IllegalStateException("noRepetitionMode");
	}
	if (currentEntryCount == 0) {
	    throw new IllegalStateException
		(errorMsg("entryCountExhausted"));
	}
	if (fileNameMap.containsKey(nextName)) {
	    throw new IllegalArgumentException(errorMsg("nameExists", nextName));
	}
	currentEntryCount--;
	fileNameMap.put(nextName, currentName);
    }

    /**
     * Get an output stream for the next entry.
     * <P>
     * The caller must finish writing all the data for an entry and
     * must close this output stream before calling nextOutputStream to get a
     * new stream.
     * <P>
     * This method is provided for convenience: it merely calls
     * {@link #nextOutputStream(String,boolean,int,byte[])} with its
     * last argument set to null. Subclasses that need to override the
     * the nextOutputStream methods will typically override
     * {@link #nextOutputStream(String,boolean,int,byte[])} and
     * {@link #nextOutputStream(String,boolean,int,int,byte[])}.
     * @param name the name of the entry
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the name of the stream is
     *        a reserved name or null, or if a subclass restricts the name
     *        in some way
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	return nextOutputStream(name, compressed, level, null);
    }

    /**
     * Get an output stream for the next entry and set an extra field.
     * <P>
     * The caller must finish writing all the data for an entry and
     * must close this output stream before
     * calling nextOutputStream to get a new stream.
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * <P>
     * Subclasses that need to override the
     * the nextOutputStream methods will typically override
     * this method and {@link #nextOutputStream(String,boolean,int,int,byte[])}.
     * @param name the name of the entry
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param extra the extra field for a ZIP-file entry
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the name of the stream is
     *        a reserved name or null, or if a subclass restricts the name
     *        in some way
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level, byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	if (currentEntryCount != 0) {
	    throw new IllegalStateException
		(errorMsg("entryCountExists", currentEntryCount));
	}
	if (name == null) {
	    throw new IllegalArgumentException(errorMsg("nullName"));
	}
	if (name.equals("META-INF/") || name.equals("META_INF/repetitionMap")) {
	    throw new IllegalArgumentException
		(errorMsg("reservedZipDocEntry", name));
	}
	ZipEntry zipEntry = new ZipEntry(name);
	if (extra != null) {
	    zipEntry.setExtra(extra);
	}
	if (lastos != null) {
	    lastos.close();
	    lastos = null;	// in case of an exception in the following code
	}
	if (!name.startsWith("META-INF/")) {
	    entryCount++;
	    noRepeatEntryCount++;
	}
	if (compressed) {
	    zos.setMethod(ZipOutputStream.DEFLATED);
	    zos.setLevel(level);
	    zos.putNextEntry(zipEntry);
	    lastos =  new ZipDocOutputStream(zos, this);
	} else {
	    zos.setMethod(ZipOutputStream.STORED);
	    zos.setLevel(0);
	    // don't call putNextEntry until we've written to the stream  and
	    // computed this entry's size and CRC.
	    lastos = new ZipDocByteArrayOutputStream(zos, zipEntry, this);
	}
	return lastos;
    }

    /**
     * Get an output stream with a repetition count for the next entry.
     * A repetition count indicates that the entry represents a sequence
     * of identical entries.  It is indicated in the ZIP file being created by
     * the presence of an extra header whose 16-bit length-field contains the
     * value 4 and whose 16-bit ID is 0xFCDA, and whose value field is 32
     * bits long and contains the repetition count.  Each field in this header
     * is in little-endian order in order to match standard ZIP-file
     * conventions.
     * <P>
     * The caller must finish writing all the data for an entry and must
     * close this output stream  before calling nextOutputStream to get
     * a new stream.
     * <P>
     * This method is provided for convenience: it merely calls
     * {@link #nextOutputStream(String,boolean,int,int,byte[])} with its
     * last argument set to null.  Subclasses that need to override the
     * the nextOutputStream methods will  typically override
     * {@link #nextOutputStream(String,boolean,int,byte[])} and
     * {@link #nextOutputStream(String,boolean,int,int,byte[])}.
     * @param name the name of the entry
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param count the repetition count for this entry; ignored if the
     *        value is less than 1
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the name of the stream is
     *        a reserved name or null, or if a subclass restricts the name
     *        in some way
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level, int count)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	return nextOutputStream(name, compressed, level, count, null);
    }

    /**
     * Get an output stream with a repetition count for the next entry,
     * specifying an extra field.
     * A repetition count indicates that the entry represents a sequence
     * of identical entries.  It is indicated in the ZIP file being created by
     * the presence of an extra header whose 16-bit length-field contains the
     * value 4 and whose 16-bit ID is 0xFCDA, and whose value field is 32
     * bits long and contains the repetition count.  Each field in this header
     * is in little-endian order in order to match standard ZIP-file
     * conventions.
     * <P>
     * The caller must finish writing all the data for an entry and must
     * close this output stream before calling nextOutputStream to get a
     * new stream.
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * <P>
     * Subclasses that need to override the the nextOutputStream
     * methods will typically override this method and
     * {@link #nextOutputStream(String,boolean,int,byte[])}.
     * @param name the name of the entry
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param count the repetition count for this entry; ignored if the
     *        value is less than 1
     * @param extra the extra field for the next entry
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the name of the stream is
     *        a reserved name or null, or if a subclass restricts the name
     *        in some way
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level, int count, byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	if (currentEntryCount != 0) {
	    throw new IllegalStateException
		(errorMsg("entryCountExists", currentEntryCount));
	}
	if (name == null) {
	    throw new IllegalArgumentException(errorMsg("nullName"));
	}
	if (name.equals("META-INF/") || name.equals("META-INF/repetitionMap")) {
	    throw new IllegalArgumentException
		(errorMsg("reservedZipDocEntry", name));
	}
	ZipEntry zipEntry = new ZipEntry(name);
	if (lastos != null) lastos.close();
	if (count > 1) {
	    if (fileNameMap == null) {
		int size = (noRepeatEntryCount == 0)? 1024:
		    (1024/noRepeatEntryCount);
		if (size <= 32) size = 32;
		size *= count;
		if (size <= 0) size = 1024*64;
		if (size > 1024*512) size = 1024*512;
		fileNameMap = new LinkedHashMap<String,String>(size);
	    }
	    if (!name.startsWith("META-INF/")) {
		zipEntry.setExtra(getExtra(count, extra));
		entryCount += count;
		noRepeatEntryCount++;
		currentEntryCount = count - 1;
		currentName = name;
	    } else {
		throw new IllegalArgumentException(errorMsg("countMustBeOne"));
	    }
	} else {
	    zipEntry.setExtra(extra);
	    if (!name.startsWith("META-INF/")) {
		entryCount++;
		noRepeatEntryCount++;
	    }
	    currentName = name;
	}
	if (compressed) {
	    zos.setMethod(ZipOutputStream.DEFLATED);
	    zos.setLevel(level);
	    zos.putNextEntry(zipEntry);
	    lastos =  new ZipDocOutputStream(zos, this);
	} else {
	    zos.setMethod(ZipOutputStream.STORED);
	    zos.setLevel(0);
	    // don't call putNextEntry until we've written to the stream  and
	    // computed this entry's size and CRC.
	    lastos = new ZipDocByteArrayOutputStream(zos, zipEntry, this);
	}
	return lastos;
    }

    /**
     * Close the document.
     * This method adds one additional entry with the name
     * META-INF/repetitionMap, whose syntax is described above.
     *
     * @exception IllegalStateException if {@link #repeatFile(String)}
     *            was not called the required number of times
     * @exception IOException if an error occurred while writing the
     *            ZIP file.
     */
    public void close() throws IllegalStateException, IOException {
	if (currentEntryCount != 0) {
	    throw new IllegalStateException
		(errorMsg("entryCountExists", currentEntryCount));
	}
	if (lastos != null) {
	    lastos.close();
	    lastos = null;
	}
	if (fileNameMap != null && fileNameMap.size() > 0) {
	    ZipEntry entry = new ZipEntry("META-INF/repetitionMap");
	    zos.setMethod(ZipOutputStream.DEFLATED);
	    zos.setLevel(9);
	    zos.putNextEntry(entry);
	    OutputStream trailer = new ZipDocOutputStream(zos, this);
	    PrintStream out = new PrintStream(trailer, false, "UTF-8");
	    for (Map.Entry<String,String> ent: fileNameMap.entrySet()) {
		String newName = URLEncoder.encode(ent.getKey(), "UTF-8");
		String origName = URLEncoder.encode(ent.getValue(), "UTF-8");
		out.print(newName);
		out.print(" ");
		out.print(origName);
		// always end lines with CRLF.
		out.print("\r\n");
	    }
	    out.flush();
	    out.close();
	    trailer.close();
	    lastos = null;
	}
	zos.flush();
	zos.close();
    }
}

//  LocalWords:  exbundle ZipDoc xFACE endian UTF blockquote pre os
//  LocalWords:  mtlen subsequences nextOutputStream boolean xFCDA
//  LocalWords:  streamClosed MIMETypeChars mimeType IOException CRC
//  LocalWords:  ZipDocFile repeatFile NoRepConfigFailed nextName
//  LocalWords:  noRepetitionMode entryCountExhausted nameExists CRLF
//  LocalWords:  Deflator IllegalArgumentException entryCountExists
//  LocalWords:  nullName repetitionMap reservedZipDocEntry unencoded
//  LocalWords:  putNextEntry countMustBeOne Subclasses superclass
//  LocalWords:  repeatFiles IllegalStateException
