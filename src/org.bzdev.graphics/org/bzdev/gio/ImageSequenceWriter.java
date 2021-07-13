package org.bzdev.gio;
import org.bzdev.io.ZipDocWriter;
import java.io.*;
import org.bzdev.imageio.ImageMimeInfo;
import java.util.Locale;

//@exbundle org.bzdev.gio.lpack.Gio

/**
 * Writer for a simplified video format consisting of a sequence
 * of images of the same type and dimensions.
 * The writer produces a file or output stream in ZipDoc format
 * (see {@link org.bzdev.io.ZipDocWriter} ZipDocWriter) with an
 * embedded media type of image/vnd.bzdev.image-sequence+zip.  This
 * format is a ZIP file with the following entries:
 *  <ul>
 *   <li>META-INF/ is the first entry in the ZIP file and represents
 *       a directory named META-INF. The length for the data it contains
 *       is zero, and it has an extra header field whose length is 34,
 *       whose type is 0xFACE (in little-endian byte order), and whose
 *       value is "image/vnd.bzdev.image-sequence+zip".
 *   <li> META-INF/metadata contains several lines, each starting
 *        with a name, followed immediately by a colon and a space
 *        (": "), and then followed immediately by a value:
 *        <ul>
 *          <li> Frame-Width: WIDTH
 *          <li> Frame-Height: HEIGHT
 *          <li> Frame-Media-Type: MEDIA_TYPE
 *          <li> Frame-Rate: RATE
 *          <li> Entry-Format: FORMAT
 *        </ul>
 *        where WIDTH is an integer specifying the width of each image
 *        in pixels, HEIGHT is an integer specifying the height of
 *        each image in pixels, MEDIA_TYPE is the media type of the
 *        images (historically, the media type), and RATE is a
 *        double-precision value giving the frame rate in units of
 *        frames per second. In addition, FORMAT is a string suitable
 *        for the C function printf for an integer argument to produce
 *        the name of the ZIP-file entry for an image, but without the
 *        initial "images/" string.  The field Frame-Rate is optional;
 *        the other fields are required.
 *   <li> images/NameN.EXT is a series of images, added to the ZIP file
 *        in the order in which they should be shown, where Name is the
 *        initial portion of an image-file name, N is a 0-padded
 *        fixed-width integer-valued sequence number starting at 1, and
 *        EXT is the file extension corresponding to the mime type. For
 *        example, images/img001.png is a suitable name for the first image
 *        in a sequence of no more than 999 PNG images.
 *  </ul>
 *  The file will contain the following bytes at the specified offsets:
 * <blockquote>
 * <pre>
 * Bytes 0 to 3:   50 4B 03 04
 * Bytes 8 to 9:   00 00
 * Bytes 14 to 25: 00 00 00 00 00 00 00 00 00 00 00 00
 * Bytes 26 to 27: 09 00
 * Bytes 28 to 29: (2 + the length of the media type)
 *                 in little-endian byte order; a larger value if
 *                 other information is included
 * Bytes 30 to 38: the characters "META-INF/"  (in UTF-8 encoding)
 * Bytes 39 to 40: CE FA  (0xFACE in little-endian byte order)
 * Bytes 41 to 42: 02 02 (the number 34 in little-endian byte order)
 * Bytes 43 to 76: image/vnd.bzdev.image-sequence+zip
 *                 (the characters making up the media type encoded
 *                 using UTF-8, without a terminating null character)
 * </pre>
 * </blockquote>
 * <P>
 * The method
 * {@link #addMetadata(int,int,String,String) addMetadata(int, int, String,String)} or
 * {@link #addMetadata(int,int,String,double,String) addMetadata(int, int, String,double,String)}
 * must be called before the call to {@link #close() close()}, and must be
 * called before any images are added. The method
 * {@link org.bzdev.io.ZipDocWriter#nextOutputStream(String,boolean,int)},
 * {@link org.bzdev.io.ZipDocWriter#nextOutputStream(String,boolean,int,int)},
 * {@link #nextOutputStream(String,boolean,int,byte[])},
 * {@link #nextOutputStream(String,boolean,int,int,byte[])},
 * {@link #nextOutputStream(boolean,int)},
 * {@link #nextOutputStream(boolean,int,byte[])},
 * {@link #nextOutputStream(boolean,int,int)},
 * {@link #nextOutputStream(boolean,int,int,byte[])},
 * {@link #nextOutputStreamGraphics()},
 * {@link #nextOutputStreamGraphics(String)},
 * {@link #nextOutputStreamGraphics(String, int)},
 * {@link #nextOutputStreamGraphics(boolean,int)},
 * {@link #nextOutputStreamGraphics(boolean,int,byte[])},
 * {@link #nextOutputStreamGraphics(int)},
 * {@link #nextOutputStreamGraphics(boolean,int,int)}, or
 * {@link #nextOutputStreamGraphics(boolean,int,int,byte[])}
 * must be used to create each of the images - these methods return
 * either an output stream that is used to store the image or an
 * instance of OutputStreamGraphics that allows the image to be drawn.
 * Only the first eight of these methods returns an output stream
 * directly.  The stream or OSGraphicsOps object that is returned
 * should be explicitly closed when the image is completely written,
 * and only one image can be written at a time.
 * <P>
 * The methods
 * {@link org.bzdev.io.ZipDocWriter#nextOutputStream(String,boolean,int,int)}
 * and
 * {@link org.bzdev.io.ZipDocWriter#nextOutputStream(String,boolean,int,int,byte[])}
 * allows a count indicating that <code>count</code> entry
 * names will appear in the output stream and share the same image. In these
 * cases (ones in which the method's first argument is an entry name),
 * {@link #repeatFile(String)} must be called
 * <code>count-1</code> times before the next output stream is created.
 * For the other methods named <code>nextOutputStream</code> and for the methods
 * named <code>nextOutputStreamGraphics</code>, {@link #repeatFile(String)}
 * must not be called as its use is implicit.
 * When the last image is complete
 * {@link org.bzdev.io.ZipDocWriter#close() close} must be called.
 * <P>
 * <B>Note:</B> Due to a change in IETF nomenclature, the term "media type"
 * is now used for what was previously called a MIME (Multipurpose Internet
 * Mail Extensions) type.
 */
public class ImageSequenceWriter extends ZipDocWriter implements ISWriterOps {
    static final String MEDIATYPE = "image/vnd.bzdev.image-sequence+zip";

    static String errorMsg(String key, Object... args) {
	return OutputStreamGraphics.errorMsg(key, args);
    }

    /**
     * Constructor given a file.
     * @param f the output file
     */
    public ImageSequenceWriter(File f)
	throws IOException, FileNotFoundException
    {
	super(new FileOutputStream(f), MEDIATYPE);
    }

    /**
     * Constructor given a file name.
     * @param fileName the output file name
     */
    public ImageSequenceWriter(String fileName)
	throws IOException, FileNotFoundException
    {
	super(new FileOutputStream(fileName), MEDIATYPE);
    }

    /**
     * Constructor given an output stream.
     * @param os the output stream
     */
    public ImageSequenceWriter(OutputStream os) throws IOException {
	super(os, MEDIATYPE);
    }

    /**
     * Add the metadata entry without a frame rate and format.
     * A default format will be provided.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames
     * @exception IOException and IO exception occurred
     * @exception IllegalStateException metadata  was already added
     */
    public void addMetadata(int frameWidth, int frameHeight,
			    String frameMimeType)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, frameMimeType, -1.0, null);
    }

    /**
     * Add the metadata entry without a frame rate.
     * The format must exclude the initial "images" component
     * of a path name.  For example, the format "image%02d.png"
     * will be used to generate entities whose names are
     * "images/image01.png", "images/image02.png", etc. The
     * caller is expected to provide the filename extension as
     * shown in the example above.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames
     * @param format the printf string that will produce the name
     *        of a ZIP entry containing an image given an integer
     * @exception IOException and IO exception occurred
     * @exception IllegalStateException metadata  was already added
     */
    public void addMetadata(int frameWidth, int frameHeight,
			    String frameMimeType, String format)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, frameMimeType, -1.0, format);
    }

    /**
     * Add the metadata entry without a media type and without a format string.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameRate the number of frames per second
     * @exception IOException and IO exception occurred
     * @exception IllegalStateException metadata  was already added
     */
    public void addMetadata(int frameWidth, int frameHeight, double frameRate)
	throws IOException, IllegalStateException
    {
	addMetadata(frameWidth, frameHeight, null, frameRate, null);
    }


    boolean metadataExists = false;

    /**
     * Test if metadata was added.
     * @return true if an addMetadata method was previously called; false
     *         otherwise
     */
    public boolean metadataAdded() {return metadataExists;}

    private static final String sep = "\r\n";

    private int frameWidth = 0;
    private int frameHeight = 0;
    private String frameMimeType = null;
    private String frameImageType = null;
    private String format = null;


    @Override
    public int getFrameWidth() {return frameWidth;}

    @Override
    public int getFrameHeight() {return frameHeight;}

    /**
     * Add the metadata entry in full.
     * The format must exclude the initial "images" component
     * of a path name.  For example, the format "image%02d.png"
     * will be used to generate entities whose names are
     * "images/image01.png", "images/image02.png", etc. The
     * caller is expected to provide the filename extension as
     * shown in the example above.
     * @param frameWidth the frame width in pixels
     * @param frameHeight the frame height in pixels
     * @param frameMimeType the media type of the frames
     * @param frameRate the number of frames per second
     * @param format the printf string that will produce the name
     *        of a ZIP entry containing an image given an integer; null
     *        if this data is not provided
     * @exception IOException an IO error occurred
     * @exception IllegalStateException metadata was already added
     */
    public void addMetadata(int frameWidth, int frameHeight,
			    String frameMimeType,
			    double frameRate,
			    String format)
	throws IOException, IllegalStateException
    {
	if (metadataExists)
	    throw new IllegalStateException(errorMsg("metadataAdded"));

	OutputStream os = nextOutputStream("META-INF/metadata", true, 9);
	PrintStream ps = new PrintStream(os, false, "UTF-8");

	ps.print("Frame-Width: " + frameWidth + sep);
	ps.print("Frame-Height: " + frameHeight + sep);
	if (frameMimeType == null) frameMimeType = "image/png";
	ps.print("Frame-MIME-Type: " + frameMimeType + sep);
	if (frameRate > 0.0) {
	    ps.print("Frame-Rate: " + frameRate + sep);
	}
	if (format == null) {
	    String extension =
		ImageMimeInfo.getExtensionForMimeType(frameMimeType);
	    if (extension == null) {
		throw new IllegalArgumentException
		    (errorMsg("noExtensionForMT", frameMimeType));
	    }
	    format = "img%07d." + extension;
	}
	ps.print("Entry-Format: " + format + sep);
	ps.flush();
	ps.close();
	this.format = "images/" + format;
	this.frameWidth = frameWidth;
	this.frameHeight = frameHeight;
	this.frameMimeType = frameMimeType;
	frameImageType = ImageMimeInfo.getFormatNameForMimeType(frameMimeType);
	metadataExists = true;
    }

    /**
     * {@inheritDoc}
     * This class ignores the value provided by this method.
     */
    @Override
    public void setEstimatedFrameCount(int frameCount) {
    }

    /**
     * {@inheritDoc}
     * This class ignores the value provided by this method.
     */
    @Override
    public void setEstimatedFrameCount(long frameCount) {
    }

    private int counter = 1;

    @Override
    public void repeatFile(String nextName)
	throws IllegalArgumentException, IllegalStateException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	super.repeatFile(nextName);
	if (nextName.startsWith("images/")) counter++;
    }

    @Override
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level, byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	if (!name.startsWith("META-INF/")) {
	    if (!metadataExists) {
		throw new IllegalStateException("metadataMissing");
	    }
	} else if (name.equals("META-INF/metadata") && metadataExists) {
	    // need the metadataExists test because this has the same
	    // signature as ZipDocFile method used to create the metadata
	    // file.
	    throw new IllegalArgumentException
		(errorMsg("reservedName", name));
	}
	OutputStream result = super.nextOutputStream(name, compressed, level,
						     extra);
	if (name.startsWith("images/")) counter++;
	return result;
    }

    /**
     * Get an output stream for the next entry with an automatically
     * generated name.
     * <P>
     * The caller must finish writing all the data for an entry and
     * must close this output stream before calling nextOutputStream to get a
     * new stream.
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the format string provided when
     *            addMetadata was called generated an illegal name or null
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(boolean compressed, int level)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	return nextOutputStream(String.format((Locale)null, format, counter),
				compressed, level);
    }

    /**
     * Get an output stream for the next entry with an automatically
     * generated name and set an extra field.
     * <P>
     * The caller must finish writing all the data for an entry and
     * must close this output stream before
     * calling nextOutputStream to get a new stream.
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param extra the extra field for a ZIP-file entry; null if there is none
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the format string provided when
     *            addMetadata was called generated an illegal name or null
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(boolean compressed, int level,
					 byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	return nextOutputStream(String.format((Locale)null, format, counter),
				compressed, level, extra);
    }

    @Override
    public OutputStream nextOutputStream(String name, boolean compressed,
					 int level, int count, byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	if (!name.startsWith("META-INF/")) {
	    if (!metadataExists) {
		throw new IllegalStateException("metadataMissing");
	    }
	} else if (name.equals("META-INF/metadata")) {
	    throw new IllegalArgumentException
		(errorMsg("reservedName", name));
	}
	OutputStream results =
	    super.nextOutputStream(name, compressed, level, count, extra);
	if (name.startsWith("images/")) counter++;
	return results;
    }

    /**
     * Get an output stream with a repetition count for the next
     * entry, providing an automatically generated entry name.
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
     * The method {@link #repeatFile(String)} must not be called explicitly
     * when this method is used as it will provided the required number of
     * calls to {@link #repeatFile(String)}.
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param count the repetition count for this entry; ignored if the
     *        value is less than 1
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the format string provided when
     *            addMetadata was called generated an illegal name or null
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(boolean compressed, int level,
					 int count)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	OutputStream result =
	    nextOutputStream(String.format((Locale)null, format, counter),
			     compressed, level, count);
	for (int i = 1; i < count; i++) {
	    repeatFile(String.format((Locale)null, format, counter));
	}
	return result;
    }

    /**
     * Get an output stream with a repetition count for the next entry,
     * specifying an extra field and providing an automatically generated
     * entry name.
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
     * <P>
     * The method {@link #repeatFile(String)} must not be called explicitly
     * when this method is used as it will provided the required number of
     * calls to {@link #repeatFile(String)}.
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * @param compressed true if the entry is compressed; false otherwise
     * @param level the compression level when the entry is compressed
     *         (0 to 9 or the constants Deflator.BEST_COMPRESSION,
     *          Deflator.BEST_SPEED, or Deflator.DEFAULT_COMPRESSION, where
     *          Deflator is a class in the package java.util.zip)
     * @param count the repetition count for this entry; ignored if the
     *        value is less than 1
     * @param extra the extra field for the next entry; null if there is
     *        none
     * @return the output stream to use for creating the next entry
     * @exception IOException if an error occurred while writing the file
     * @exception IllegalArgumentException if the format string provided when
     *            addMetadata was called generated an illegal name or null
     * @exception IllegalStateException if a repetition count has been
     *            previously specified and {@link #repeatFile(String)}
     *            has not been called the required number of times
     */
    public OutputStream nextOutputStream(boolean compressed, int level,
					 int count, byte[] extra)
	throws IOException, IllegalArgumentException, IllegalStateException
    {
	OutputStream result =
	    nextOutputStream(String.format((Locale)null, format, counter),
			     compressed, level, count, extra);
	for (int i = 1; i < count; i++) {
	    repeatFile(String.format((Locale)null, format, counter));
	}
	return result;
    }

    /**
     * Create a named graphics output stream for drawing an image.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OutputStreamGraphics osg = isw.nextOutputStreamGraphics();
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * @return name the name of the output stream
     * @return a graphics output stream for drawing an image
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics(String name)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(name,
								 false,
								 0),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a graphics output stream for drawing an image.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OutputStreamGraphics osg = isw.nextOutputStreamGraphics();
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * Images will be stored without any compression and will be given
     * a standard name.
     * @return a graphics output stream for drawing an image
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics()
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(false, 0),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a graphics output stream for drawing an image, specifying
     * how the image should be stored.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * @param compressed true if the image should be compressed; false if
     *        it should be stored
     * @param level the compression level (0 =&gt; no compression,
     *        9 =&gt; maximum compression)
     * @return a graphics output stream for drawing an image
     */
    public OSGraphicsOps nextOutputStreamGraphics(boolean compressed,
							 int level)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(compressed,
								 level),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a graphics output stream for drawing an image, specifying
     * how the image should be stored and providing an extra field.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * @param compressed true if the image should be compressed; false if
     *        it should be stored
     * @param level the compression level (0 =&gt; no compression,
     *        9 =&gt; maximum compression)
     * @param extra a byte sequence to add to the extra field; null if there is
     *        none
     * @return a graphics output stream for drawing an image
     */
    public OSGraphicsOps nextOutputStreamGraphics(boolean compressed,
							 int level,
							 byte[] extra)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(compressed,
								 level,
								 extra),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a named graphics output stream for drawing an image, specifying a
     * repetition count.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * <P>
     * The method {@link #repeatFile(String)} should be called explicitly
     * when this method is used as it will not provide the required number of
     * calls to {@link #repeatFile(String)}.
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @return a graphics output stream for drawing an image
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics(String name, int count)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(name,
								 false,
								 0,
								 count),
						frameWidth, frameHeight,
						frameImageType);
    }
    /**
     * Create a graphics output stream for drawing an image, specifying a
     * repetition count.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * Images will be stored without any compression and will be given
     * a standard name.
     * <P>
     * The method {@link #repeatFile(String)} should not be called explicitly
     * when this method is used as it will provide the required number of
     * calls to {@link #repeatFile(String)}.
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @return a graphics output stream for drawing an image
     */
    @Override
    public OSGraphicsOps nextOutputStreamGraphics(int count)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(false, 0,
								 count),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a graphics output stream for drawing an image, specifying
     * a repetition count and specifying how the image should be stored.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceWriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * <P>
     * The method {@link #repeatFile(String)} should not be called explicitly
     * when this method is used as it will provided the required number of
     * calls to {@link #repeatFile(String)}.
     * @param compressed true if the image should be compressed; false if
     *        it should be stored
     * @param level the compression level (0 =&gt; no compression,
     *        9 =&gt; maximum compression)
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @return a graphics output stream for drawing an image
     */
    public OSGraphicsOps nextOutputStreamGraphics(boolean compressed,
							 int level, int count)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(compressed,
								 level, count),
						frameWidth, frameHeight,
						frameImageType);
    }

    /**
     * Create a graphics output stream for drawing an image,
     * specifying a repetition count, specifying how the image
     * should be stored, and providing an extra field.
     * To use this method, one will typically use the following
     * design pattern:
     * <blockquote><pre>
     *    ImageSequenceWriter isw = new ImageSequenceriter(...);
     *    OSGraphicsOps osg = isw.nextOutputStreamGraphics(...);
     *    Graphics2D g2d = osg.createGraphics();
     *    // drawing operation using g2d
     *    ...
     *    g2d.displose();
     *    osg.imageComplete();
     *    osg.close();
     * </pre></blockquote>
     * <P>
     * The method {@link #repeatFile(String)} should not be called explicitly
     * when this method is used as it will provided the required number of
     * calls to {@link #repeatFile(String)}.
     * The extra header field should not include the repetition-count data
     * that this class (and its superclass) will provide.
     * @param compressed true if the image should be compressed; false if
     *        it should be stored
     * @param level the compression level (0 =&gt; no compression,
     *        9 =&gt; maximum compression)
     * @param count the repetition count for the entry corresponding to
     *        the graphics output stream that will be returned
     * @param extra a byte sequence to add to the extra field; null if
     *        there is none
     * @return a graphics output stream for drawing an image
     */
    public OSGraphicsOps nextOutputStreamGraphics(boolean compressed,
							 int level, int count,
							 byte[] extra)
	throws IllegalStateException, IOException
    {
	if (!metadataExists) {
	    throw new IllegalStateException("metadataMissing");
	}
	return OutputStreamGraphics.newInstance(nextOutputStream(compressed,
								 level, count,
								 extra),
						frameWidth, frameHeight,
						frameImageType);
    }


    /**
     * Close the writer.
     * @exception IllegalStateException if {@link #repeatFile(String)}
     *            was not called the required number of times or
     *            {@link #addMetadata(int,int,String)},
     *            {@link #addMetadata(int,int,String,String)}, or
     *            {@link #addMetadata(int,int,String,double,String)},
     *            was not called
     * @exception IOException if an error occurred while writing the
     *            ZIP file.
     */
    @Override
    public void close() throws IllegalStateException, IOException {
	if (!metadataExists)
	    throw new IllegalStateException
		(errorMsg("metadataMissing"));
	super.close();
    }
}

//  LocalWords:  exbundle ZipDoc ZipDocWriter ul li xFACE endian PNG
//  LocalWords:  printf blockquote pre UTF addMetadata boolean IETF
//  LocalWords:  nextOutputStream nextOutputStreamGraphics repeatFile
//  LocalWords:  OutputStreamGraphics OSGraphicsOps fileName os png
//  LocalWords:  frameWidth frameHeight frameMimeType IOException img
//  LocalWords:  IllegalStateException frameRate metadataAdded xFCDA
//  LocalWords:  noExtensionForMT metadataMissing metadataExists isw
//  LocalWords:  ZipDocFile reservedName Deflator superclass osg
//  LocalWords:  IllegalArgumentException ImageSequenceWriter
//  LocalWords:  createGraphics displose imageComplete
//  LocalWords:  ImageSequenceriter
