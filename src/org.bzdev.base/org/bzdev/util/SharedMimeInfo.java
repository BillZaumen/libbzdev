package org.bzdev.util;
import java.io.Closeable;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

//@exbundle org.bzdev.util.lpack.Util

/**
 * Utility class for generating a freedesktop.org shared-mime-info file.
 * These files are used to specify how the media type (MIME type) of a
 * file can be determined. The BZDev class library provides support for
 * file formats that are stylized ZIP files or stylized Java property
 * files, both with the media type encoded in the files.  This class
 * provides an easy way to generate shared-mime-info entries.
 * <P>
 * The constructors determine the output stream. After a series of 'add'
 * methods, the method {@link SharedMimeInfo#close()} must be called
 * (either directly or via a Java try-with-resources statement) once all
 * the entries have been added.
 * <P>
 * It is easy use the ESP scripting language. The following program is
 * a good example:
 * <BLOCKQUOTE><PRE></CODE>
 * #!/usr/bin/scrunner -sEUN:true
 * import org.bzdev.util.SharedMimeInfo;
 *
 * new SharedMimeInfo(global.getWriter())
 *    .addConfigPropertyType(90, "application/foo", "foo",
 *                           "foo type")
 *    .addZipDocType(80, "application/bar+zip", "bar",
 *                   "bar type")
 *    .close();
 * </CODE></PRE></BLOCKQUOTE>
 * When run with no arguments, this script will print a shared-mime-info
 * file to standard output.  To redirect it to a file, add
 * the <CODE>scrunner</CODE> argument <CODE>-o FILE</CODE> when the
 * script is run: if the script is named createMime, then one can run
 * the command
 * <BLOCKQUOTE><PRE></CODE>
 *   ./createMime -o MimeInfo.xml
 * </CODE></PRE></BLOCKQUOTE>
 */
public class SharedMimeInfo implements Closeable {

    // We need fully qualified names because this class contains
    // inner classes with similar names.
    static String errorMsg(java.lang.String key, java.lang.Object... args)
    {
	return UtilErrorMsg.errorMsg(key, args);
    }


    private static Charset UTF8 = Charset.forName("UTF-8");

    private boolean starting = true;
    private boolean shouldCloseWriter = false;
    private Writer w;

    /**
     * Constructor given a file.
     * @param file a file used for output
     * @throws IOException if an IO error occurred
     */
    public SharedMimeInfo(File file) throws IOException {
	w = new FileWriter(file, UTF8);
	shouldCloseWriter = true;
    }

    /**
     * Constructor to write to standard output.
     * @throws IOException if an IO error occurred
     */
    public SharedMimeInfo() throws IOException {
	    w = new OutputStreamWriter(System.out, UTF8);
    }

    /**
     * Constructor given a writer.
     * @param writer a {@link Writer} used for output
     */
    public SharedMimeInfo(Writer writer) {
	w = writer;
    }


    private void start() throws IOException {
	CopyUtilities.copyResource("org/bzdev/util/SharedMimeInfoStart.xml",
				   w, UTF8);
	starting = false;
    }

    /**
     * Close this {@link SharedMimeInfo}.
     * If the constructor's argument was not a file, the file being
     * written will be closed, but not otherwise. In all cases, the
     * output will be flushed.
     */
    @Override
    public void close() throws IOException {
	if (starting) {
	    start();
	}
	CopyUtilities.copyResource("org/bzdev/util/SharedMimeInfoEnd.xml",
				   w, UTF8);
	w.flush();
	if (shouldCloseWriter) {
	    w.close();
	}
    }

    /**
     * Add an entry to a share-mime-info file for a media type readable
     * by {@link org.bzdev.swing.ConfigPropertyEditor}.
     * @param priority the priority as an integer in the range [0,100]
     * @param mediaType the media type (MIME type)
     * @param suffix the file-name suffix for this type (null if there
     *        isn't one)
     * @param comment a comment describing the media type; null if
     *        there is none
     * @return this object
     * @throws IllegalArgumentException for an illegal argument
     * @throws IOException if an IO error occurs
     */
    public SharedMimeInfo addConfigPropertyType(int priority,
						String mediaType,
						String suffix,
						String comment)
	throws IOException, IllegalArgumentException
    {
	if (priority < 0 || priority > 100) {
	    String msg = errorMsg("illegalPriority", priority);
	    throw new IllegalArgumentException(msg);
	}
	if (mediaType == null) {
	    throw new IllegalArgumentException(errorMsg("nullMediaType"));
	}
	if (starting) {
	    start();
	}
	TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	kmap.put("priority", "" + priority);
	kmap.put("mediaType", mediaType);
	if (suffix != null) {
	    if (suffix.startsWith(".")) {
		suffix = suffix.substring(1);
	    }
	    kmap.put("suffix", suffix);
	}
	if (comment != null) {
	    kmap.put("comment", comment.trim());
	}
	TemplateProcessor tp = new TemplateProcessor(kmap);
	String resource = "org/bzdev/util/SharedMimeInfoCPE.xml";
	tp.processSystemResource(resource, "UTF-8", w);
	return this;
    }

    /**
     * Add an entry to a share-mime-info file for a media type readable
     * by {@link org.bzdev.io.ZipDocFile}.
     * <P>
     * This format is a stylized ZIP format, so the media type should
     * end with the string "+zip".
     * @param priority the priority as an integer in the range [0,100]
     * @param mediaType the media type (MIME type)
     * @param suffix the file-name suffix for this type (null if there
     *        isn't one)
     * @param comment a comment describing the media type; null if
     *        there is none
     * @return this object
     * @throws IllegalArgumentException for an illegal argument
     * @throws IOException if an IO error occurs
     */
    public SharedMimeInfo addZipDocType(int priority,
					String mediaType,
					String suffix,
					String comment)
	throws IOException, IllegalArgumentException
    {
	if (priority < 0 || priority > 100) {
	    String msg = errorMsg("illegalPriority", priority);
	    throw new IllegalArgumentException(msg);
	}
	if (mediaType == null) {
	    throw new IllegalArgumentException(errorMsg("nullMediaType"));
	}
	if (starting) {
	    start();
	}
	TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
	kmap.put("priority", "" + priority);
	kmap.put("mediaType", mediaType);
	if (suffix != null) {
	    if (suffix.startsWith(".")) {
		suffix = suffix.substring(1);
	    }
	    kmap.put("suffix", suffix);
	}
	if (comment != null) {
	    kmap.put("comment", comment.trim());
	}
	TemplateProcessor tp = new TemplateProcessor(kmap);
	String resource = "org/bzdev/util/SharedMimeInfoZipDoc.xml";
	tp.processSystemResource(resource, "UTF-8", w);
	return this;
    }
}
