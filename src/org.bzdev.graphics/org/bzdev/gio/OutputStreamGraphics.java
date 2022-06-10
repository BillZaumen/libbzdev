package org.bzdev.gio;
import org.bzdev.gio.spi.OSGProvider;

import java.io.*;
import java.awt.*;
import java.awt.image.ColorModel;
import javax.imageio.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Formatter;
import java.util.ServiceLoader;
import java.util.ResourceBundle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import java.security.*;

//@exbundle org.bzdev.gio.lpack.Gio

/**
 * Output some graphics to an output stream in a standard format.
 * Various image formats are supported (the ones supported by Java).
 * In addition, postscript is supported directly, and various other
 * image formats may be supported via an SPI (Service Provider Interface).
 * The image-format types one can expect to have are
 * <UL>
 *    <LI> png - provided by the Java imageio library.
 *    <LI> jpeg - provided by the Java imageio library.
 *    <LI> gif - provided by the Java imageio library.
 *    <LI> bmp - provided by the Java imageio library.
 *    <LI> wbmp - provided by the Java imageio library
 *    <li> ps - provided by the org.bzdev.gio package and
 *              the java.print package.
 *    <li> svg - this format will be activated if the Apache Batik
 *         package org.apache.batik and its subpackages are
 *         included on the class path.  Batik-1.8 has some bugs
 *         that cause problems if an image is drawn. The symptom
 *         is a null pointer exception when a particular Batik
 *         method is used. As a work-around, the provider
 *         in org.bzdev.providers.osg for batik uses an anonymous
 *         class to override three methods where this bug may
 *         appear. The modification catches a null pointer exception
 *         and quietly continues.  The result is that the image
 *         will not be drawn but the application will continue
 *         running.  SVG providers should return instances of
 *         OutputStreamGraphics that implement the {@link SvgOps}
 *         interface.
 * </UL>
 *<P>
 * The method named <code>newInstance</code> are used to create a new
 * instance of this class.  These newInstance methods use various
 * subsets of the following arguments:
 * <ul>
 *  <li> os - the input stream.
 *  <li> width - the width of the image when created in its normal orientation.
 *  <li> height - the height of the image when created in its normal
 *       orientation.
 *  <li> scaleFactor -a scale factor to change the image size.  The scaling
 *       is the same in the X and Y directions, with (0.0, 0.0) always placed
 *       in the same location.
 *  <li> xtranslation - the translation in the X direction to apply after
 *       scaling. This can be used to center an image.
 *  <li> ytranslation - the translation in the Y direction to apply after
 *       scaling. This can be used to center an image.
 *  <li> orientation - either ImageOrientation.NORMAL (where the width and
 *       height have its normal meanings), ImageOrientation.CLOCKWISE90
 *       (where the image is rotated clockwise 90 degrees and where the
 *       width and height are swapped), or ImageOrientation.COUNTERCLOCKWISE90
 *       (where the image is rotated counterclockwise 90 degrees and where the
 *       width and height are swapped).

 *  <li> type - the type of the image format ("ps", "png", "jpeg", etc.).
 *       The static method {@link
 *       OutputStreamGraphics#getImageTypes()} returns an array
 *       containing the valid image-format type names. The methods
 *       {@link OutputStreamGraphics#getImageTypeForFile(String)} and
 *       {@link OutputStreamGraphics#getImageTypeForFile(File)} can be
 *       used to look up the type given a file or file
 *       name. Similarly,
 *       {@link OutputStreamGraphics#getImageTypeForSuffix(String)} can be
 *       used to look up an image-format type given a file-name extension.
 *  <li> preferAlpha - This is a boolean value. When <code>true</code>,
 *       it indicates that an image with an alpha channel is requested.
 *       Otherwise the value is <code>false</code>. Whether or not the
 *       request can be granted is dependent on the type of the image. Some
 *       image formats do not support alpha channels.
 * </ul>
 * <P> After obtaining a new instance of OutputStreamGraphics,  the user
 * should then call {@link #createGraphics() createGraphics()} to
 * create a graphics context for drawing.  To finish creating the
 * output stream, the user must call {@link #imageComplete() imageComplete()}.
 * As many graphics contexts as needed can be created.
 * <P>
 * Subclasses of OutputStreamGraphics are referenced by output-stream-graphics
 * service providers. Subclasses used for this purpose must implement
 * methods whose signature and modifiers are
 * <UL>
 *    <LI><code>public static Integer getDefaultWidth(ImageOrientation)</code>.
 *    <LI><code>public static Integer getDefaultHeight(ImageOrientation)</code>.
 * </UL>
 * These methods return the default height and width.  In most cases the
 * image orientation is ignored. One exception is for postscript, due to
 * the use of a printer-based software that has dependencies are the sizes
 * of various types of paper. The image orientation indicates how the image
 * is intended to be viewed, and the height and width indicates the
 * height and width when viewed from the desired orientation.
 */
public abstract class OutputStreamGraphics
    implements OSGraphicsOps, GraphicsCreator
{

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.gio.lpack.Gio");

    static String errorMsg(String key, Object... args) {
	// We can user Formatter rather than SafeFormatter
	// because the format directives are all %s ones.
	return (new Formatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    private static Set<String> itSet = new LinkedHashSet<String>();

    private static  Map<String,Class<? extends OutputStreamGraphics>> map
	= new LinkedHashMap<String,Class<? extends OutputStreamGraphics>>();

    private static final String PS = "ps";

    // provider-specific info
    // media type from image type
    private static HashMap<String,String> mediaTypes = new HashMap<>();
    // image type from media type
    private static HashMap<String,String> imageTypes = new HashMap<>();
    // all image types from media type
    private static HashMap<String,LinkedHashSet<String>> aliasTypes =
	new HashMap<String,LinkedHashSet<String>>();

    // Image type from suffix
    private static HashMap<String,String> suffixMap = new HashMap<>();
    // suffix set from image type
    private static HashMap<String,HashSet<String>> suffixes =
	new HashMap<String,HashSet<String>>();
    // preferred suffix from image type
    private static HashMap<String,String> preferredSuffix = new HashMap<>();

    static {
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		public Void run() {
		    ServiceLoader<OSGProvider> loader =
			ServiceLoader.load(OSGProvider.class);
		    for (OSGProvider provider: loader) {
			Class<? extends OutputStreamGraphics> clazz
			    = provider.getOsgClass();
			for (String type: provider.getTypes()) {
			    if (!map.containsKey(type)) {
				map.put(type, clazz);
				String mediaType = provider.getMediaType(type);
				mediaTypes.put(type, mediaType);
				if (!imageTypes.containsKey(mediaType)) {
				    // the first match provides the
				    // media-type to image type mapping.
				    // Any others are just/ aliases.
				    imageTypes.put(mediaType, type);
				    LinkedHashSet<String> hset =
					new LinkedHashSet<>();
				    hset.add(type);
				    aliasTypes.put(mediaType, hset);
				    itSet.add(type);
				} else {
				    aliasTypes.get(mediaType).add(type);
				}
				String sarray[] = provider.getSuffixes(type);
				if (sarray.length > 0) {
				    if (!preferredSuffix.containsKey(type)) {
					preferredSuffix.put(type, sarray[0]);
				    }
				    LinkedHashSet<String> sset =
					new LinkedHashSet<>();
				    for (String suffix: sarray) {
					if (!suffixMap.containsKey(suffix)) {
					    suffixMap.put(suffix, type);
					}
					sset.add(suffix);
				    }
				    suffixes.put(type, sset);
				}
			    }
			}
		    }
		    return null;
		}
	    });
    }

    /**
     * Get all the file-name suffixes associated with graphics output streams.
     * These may be used as file-name extensions.
     * @return the suffixes
     */
    public static String[] getSuffixes() {
	Set<String> keySet = suffixMap.keySet();
	String[] array = new String[keySet.size()];
	return keySet.toArray(array);
    }

    /**
     * Get the suffixes appropriate for an image-format type.
     * These may be used as file-name extensions.
     * @param imageType the image-format type
     * @return the suffixes; null if there are none (e.g., if
     *         the image-format type is not recognized)
     */
    public static String[] getSuffixesForImageType(String imageType) {
	Set<String> sset = suffixes.get(imageType);
	if (sset == null) return null;
	String array[] = new String[sset.size()];
	return sset.toArray(array);
    }

    /**
     * Get the suffixes appropriate for a media type.
     * These may be used as file-name extensions.
     * @param mediaType the media type
     * @return the suffixes; null if there are none (e.g., if
     *         the image-format type is not recognized)
     */
    public static String[] getSuffixesForMediaType(String mediaType) {
	String imageType = imageTypes.get(mediaType);
	if (imageType == null) return null;
	return getSuffixesForImageType(imageType);
    }

    /**
     * Get the media type (MIME type) for an image-format type.
     * @param imageType the image-format type
     * @return the media type; null if there is none (e.g., if
     *         the image-format type is not recognized)
     */
    public static String getMediaTypeForImageType(String imageType) {
	return mediaTypes.get(imageType);
    }

    /**
     * Get the preferred suffix for an image-format type.
     * @param imageType the image-format type
     * @return the suffix; null if there is none (e.g., if
     *         the image-format type is not recognized)
     */
    public static String getSuffixForImageType(String imageType) {
	return preferredSuffix.get(imageType);
    }

    /**
     * Get the preferred image-format type for a media type.
     * Multiple image-format types can have the same media type. This
     * method picks a preferred one to use.
     * @param mediaType the media type
     * @return the corresponding image-format type; null if there is none
     *          (e.g., if the media type is not recognized)
     */
    public static String getImageTypeForMediaType(String mediaType) {
	return imageTypes.get(mediaType);
    }

    /**
     * Get all image-format types for a media type.
     * Multiple image-format types can have the same media type. This
     * method  lists them.  The first element of the array that
     * is returned will contain the preferred image-format type.
     * Other names are either aliases (e.g. "PS" for "ps") or
     * indicate an image-format type and a specific provider.
     * @param mediaType the media type
     * @return the corresponding image-format types; null if there is none
     *          (e.g., if the media type is not recognized)
     */
    public static String[] getAliasesForMediaType(String mediaType) {
	LinkedHashSet<String> hset = aliasTypes.get(mediaType);
	if (hset == null) {
	    return null;
	}
	return hset.toArray(new String[hset.size()]);
    }

    /**
     * Get all aliases for an image-format type.
     * Multiple image-format types can have the same media type. This
     * method  lists all that have the same media type.
     * The first element of the array that
     * is returned will contain the preferred image-format type.
     * Other names are  aliases (e.g. "PS" for "ps", or
     * indicate an image-format type and a specific provider).
     * @param imageType the image-format type
     * @return the corresponding image-format types; null if there is none
     *          (e.g., if the media type is not recognized)
     */
    public static String[] getAliasesForImageType(String imageType) {
	String mediaType = mediaTypes.get(imageType);
	if (mediaType == null) return null;
	return getAliasesForMediaType(mediaType);
    }

    /**
     * Get the preferred suffix for a media type.
     * Multiple suffixes can have the same media type. This
     * method picks a preferred one to use.
     * @param mediaType the media type
     * @return the corresponding image-format type; null if there is none
     *          (e.g., if the media type is not recognized)
     */
    public static String getSuffixForMediaType(String mediaType) {
	String imageType = imageTypes.get(mediaType);
	if (imageType == null) return null;
	return preferredSuffix.get(imageType);
    }

    /**
     * Get the supported image-format types.
     * These include the image-format types supported by Java (the format
     * names used in the javax.imageio package) plus
     * an image-format type named "ps" for images that use Postscript.
     * Aliases for the image-format types are not listed.
     * An SPI (Service-Provider Interface) allows additional ones
     * to be added.
     * @return an array of strings naming these types
     */
    public static String[] getImageTypes() {
	String[] result = new String[itSet.size()];
	return itSet.toArray(result);
    }

    /**
     * Get the supported image-format types, including aliases.
     * These include the image-format types supported by Java (the format
     * names used in the javax.imageio package) plus
     * image-format types named "ps" or "PS" for images that use Postscript.
     * An SPI (Service-Provider Interface) allows additional ones
     * to be added.
     * @return an array of all image-format type names.
     */
    public static String[] getAllImageTypes() {
	Set<String> keySet = map.keySet();
	return keySet.toArray(new String[keySet.size()]);
    }

    /**
     * Get the supported media types.
     * These include the media types supported by Java (the media
     * types used in the javax.imageio package) plus
     * "application/postscript".
     * An SPI (Service-Provider Interface) allows additional ones
     * to be added.
     * @return an array of strings naming these types.
     */
    public static String[] getMediaTypes() {
	Set<String> mtSet = imageTypes.keySet();
	String[] result = new String[mtSet.size()];
	return mtSet.toArray(result);
    }

    /**
     * Get the extension for a file.
     * @param file the file
     * @return the file extension; null if there is no extension
     */
    public static String getFilenameExtension(File file) {
	String filename = file.getName();
	int index = filename.lastIndexOf('.');
	return (index == -1 || index+1 == filename.length())? null:
	    filename.substring(index+1);
    }
    /**
     * Get the extension for a file name.
     * @param filename the filename
     * @return the file extension; null if there is no extension;
     *         an empty string if the file name ends in a period
     */
    public static String getFilenameExtension(String filename) {
	return getFilenameExtension(new File(filename));
    }


    /**
     * Get the image-format type given a file.
     * The image-format type is determined by the file's extension.
     * Image-format types are the format names used in the
     * javax.imageio package, extended to include "ps".
     * @param file the file
     * @return the format name for the file; null if unknown
     *         or if there is none
     */
    public static String getImageTypeForFile(File file) {
	String ext = getFilenameExtension(file);
	if (ext == null) return null;
	return suffixMap.get(ext);
    }

    /**
     * Get the image-format type given a file name.
     * The image-format type is determined by the file's extension.
     * Image-format types are the format names used in the
     * javax.imageio package, extended to include "ps".
     * @param filename the file name
     * @return the format name for the file name; null if unknown
     *         or if there is none
     */
    public static String getImageTypeForFile(String filename) {
	String ext = getFilenameExtension(filename);
	if (ext == null) return null;
	return suffixMap.get(ext);
    }

    /**
     * Get the image-format type given a file-name extension or suffix.
     * @param extension the suffix or file-name extension
     * @return the image-format type; null if there is none
     *         or if the image-format type is not recognized by Java
     */
    public static String getImageTypeForSuffix(String extension) {
	if (extension == null) return null;
	return suffixMap.get(extension);
    }

    private OutputStream os;
    private int width;
    private int height;
    private String type;

    private double scaleFactor = 1.0;

    ImageOrientation orientation = ImageOrientation.NORMAL;

    /**
     * Get the image orientation.
     * Logically, an image is first drawn and then possibly rotated
     * depending on the value of the image orientation.
     * @return the image orientation (either ImageOrientation.NORMAL,
     *         ImageOrientation.COUNTERCLOCKWISE90, or
     *         ImageOrientation.CLOCKWISE90)
     * @see ImageOrientation
     */
    public ImageOrientation getOrientation() {return orientation;}

    /**
     * Get the scale factor to fit in a bounding box of the default
     * size given an explicit image orientation.
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param width the width of the image in user-space coordinates
     * @param height the height of the image in user-space coordinates
     * @param orientation the image orientation
     */
    public static double getBBScaleFactor(String type, int width, int height,
					  ImageOrientation orientation) {
	double w = (double)getDefaultWidth(type, orientation);
	double h = (double)getDefaultHeight(type, orientation);
	double xsf = w/width;
	double ysf = h/height;
	return (xsf < ysf)? xsf: ysf;
    }

    /**
     * Get the scale factor.
     * A scale factor can be used to scale the image created. The scaling
     * occurs in user space and affects the size of everything drawn.
     * The default scale factor is 1.0.  Values above 1.0 may result in
     * clipping.
     * @return the scale factor
     */
    public double getScaleFactor() {return scaleFactor;}

    void setScaleFactor(double scaleFactor) {
	this.scaleFactor = scaleFactor;
    }

    private double xTranslation = 0.0;
    private double yTranslation = 0.0;

    /**
     * Get the user-space translation.
     * A user-space translation is set by calling
     * {@link org.bzdev.gio.OutputStreamGraphics#newInstance(java.io.OutputStream,int,int,double,double,double,String) newInstance}.
     * This translation applies to the point 0.0 after scaling.
     * @return the result of translating the point at the location (0.0, 0.0)
     */
    public Point2D getTranslation() {
	return new Point2D.Double(xTranslation, yTranslation);
    }

    void setTranslation(double x, double y) {
	xTranslation = x;
	yTranslation = y;
    }

    /**
     * Get the output stream.
     * @return the output stream
     */
    protected OutputStream getOutputStream() {
	return os;
    }

    /**
     * Close the underlying output stream.
     * This is equivalent to the expression
     * <code>getOutputStream().close()</code>.
     * Note: in some cases, close should not be called.  For example, when
     * the output stream is an instance of ZipOutputStream, <code>close()</code>
     * would close the whole stream. Instead one could use the expression
     * <code>((ZipOutputStream)getOutputStream()).closeEntry()</code>.
     * @exception IOException if an IO error occurred
     */
    @Override
    public void close() throws IOException {
	os.close();
    }

    /**
     * Get the image width parameter.
     * This is the value determined by the method
     * {@link #newInstance(java.io.OutputStream,int,int,String) newInstance}.
     * Unless a graphics context is modified, this value represents an
     * upper bound on the X coordinate of points that will appear in the
     * image in the coordinate system used by the graphics context independent
     * of the orientation
     * @return the width in user-space coordinates
     */
    @Override
    public int getWidth() {
	return width;
    }

    /**
     * Get the image height parameter.
     * This is the value determined by the method
     * {@link #newInstance(java.io.OutputStream,int,int,String) newInstance}.
     * Unless a graphics context is modified, this value represents an
     * upper bound on the Y coordinate of points that will appear in the
     * image in the coordinate system used by the graphics context independent
     * of the orientation.
     * @return the height in user-space coordinates
     */
    @Override
    public int getHeight() {
	return height;
    }

    /**
     * Get the image-format type parameter.
     * This is the value passed to the method
     * {@link #newInstance(java.io.OutputStream,int,int,String) newInstance}.
     * @return the image-format type
     */
    public String getType() {
	return type;
    }

    /**
     * Return the default width for a given type and image orientation.
     * For many image formats, the returned value is not dependent on the
     * orientation.  Some (e.g., Postscript) will be orientation dependent
     * as the default size is tied by the underlying Java implementation to
     * sizes of paper supported by printers.
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param orientation the image orientation
     * @return the default width
     */
    public static int getDefaultWidth(String type,
				      ImageOrientation orientation)
    {

	type = type.trim();
	Class<? extends OutputStreamGraphics> clazz = map.get(type);
	if (clazz == null) {
	    throw new IllegalArgumentException(errorMsg("osgType", type));
	} else {
	    try {
		String methodName = "getDefaultWidth";
		Method method = clazz.getDeclaredMethod(methodName,
							ImageOrientation.class);
		if (((method.getModifiers() & (Modifier.PUBLIC|Modifier.STATIC))
		     == (Modifier.PUBLIC|Modifier.STATIC))
		    && (method.getReturnType().equals(Integer.class))) {
		    Object obj = method.invoke(null, orientation);
		    if (obj instanceof Integer) {
			return (Integer)obj;
		    } else {
			String n1 = clazz.getName();
			String n3 = orientation.toString();
			throw new RuntimeException
			    (errorMsg("nonInteger", n1, methodName, n3));
		    }
		} else {
		    throw new NoSuchMethodException
			(errorMsg("noIntMethod", methodName, clazz.toString()));
		}
	    } catch (NoSuchMethodException e) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, e);
	    } catch (IllegalAccessException eee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eee);
	    } catch (InvocationTargetException eeee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eeee);
	    }
	}
    }

    /**
     * Return the default height for a given type and image orientation.
     * For many image formats, the returned value is not dependent on the
     * orientation.  Some (e.g., Postscript) will be orientation dependent
     * as the default size is tied by the underlying Java implementation to
     * sizes of paper supported by printers.
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param orientation the image orientation
     * @return the default height
     */
    public static int getDefaultHeight(String type,
				       ImageOrientation orientation)
    {

	type = type.trim();
	Class<? extends OutputStreamGraphics> clazz = map.get(type);
	if (clazz == null) {
	    throw new IllegalArgumentException(errorMsg("osgType", type));
	} else {
	    try {
		String methodName = "getDefaultHeight";
		Method method = clazz.getDeclaredMethod(methodName,
							ImageOrientation.class);
		if (((method.getModifiers() & (Modifier.PUBLIC|Modifier.STATIC))
		     == (Modifier.PUBLIC|Modifier.STATIC))
		    && (method.getReturnType().equals(Integer.class))) {
		    Object obj = method.invoke(null, orientation);
		    if (obj instanceof Integer) {
			return (Integer)obj;
		    } else {
			String n1 = clazz.getName();
			String n3 = orientation.toString();
			throw new RuntimeException
			    (errorMsg("nonInteger", n1, methodName, n3));
			    /*(clazz.toString() +"." + methodName
			     + "(ImageOrientation) did not "
			     + "return an integer value");*/
		    }
		} else {
		    throw new NoSuchMethodException
			(errorMsg("noIntMethod", methodName, clazz.toString()));
		        /*("no public static int " + methodName
			 + "(ImageOrientation) method for "
			 + clazz.toString());*/
		}
	    } catch (NoSuchMethodException e) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, e);
	    } catch (IllegalAccessException eee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eee);
	    } catch (InvocationTargetException eeee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eeee);
	    }
	}
    }

    /**
     * Create an instance of OutputStreamGraphics.
     * @param os the output stream
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, getDefaultWidth(type, ImageOrientation.NORMAL),
			   getDefaultHeight(type, ImageOrientation.NORMAL),
			   ImageOrientation.NORMAL,
			   type, false);
    }

    /**
     * Create an instance of OutputStreamGraphics, possibly with an alpha
     * channel.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, String type, boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, getDefaultWidth(type, ImageOrientation.NORMAL),
			   getDefaultHeight(type, ImageOrientation.NORMAL),
			   ImageOrientation.NORMAL,
			   type, preferAlpha);
    }

    /**
     * Create an instance of OutputStreamGraphics specifying an image
     * orientation.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * @param os the output stream
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, ImageOrientation orientation, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os,
			   getDefaultWidth(type, orientation),
			   getDefaultHeight(type, orientation),
			   orientation, type, false);
    }

    /**
     * Create an instance of OutputStreamGraphics specifying an image
     * orientation, possibly with an alpha channel.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, ImageOrientation orientation, String type,
		    boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os,
			   getDefaultWidth(type, orientation),
			   getDefaultHeight(type, orientation),
			   orientation, type, preferAlpha);
    }

    /**
     * Create an instance of OutputStreamGraphics given a height and width.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param height the image height in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics 
	newInstance(OutputStream os, int width, int height, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, ImageOrientation.NORMAL, type,
			   false);
    }

    /**
     * Create an instance of OutputStreamGraphics given a height and width,
     * preferably with an alpha channel.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param height the image height in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height, String type,
		    boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, ImageOrientation.NORMAL, type,
			   preferAlpha);
    }

    /**
     * Create an instance of OutputStreamGraphics given a height, width, and
     * image orientation.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param height the image height in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    ImageOrientation orientation, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, orientation, type, false);
    }
    /**
     * Create an instance of OutputStreamGraphics given a height, width, and
     * image orientation.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param height the image height in user-space coordinates
     *        (ignored for determining the size of a Postscript image)
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true the caller requests an alpha channel;
     *        false otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    ImageOrientation orientation, String type,
		    boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	type = type.trim();
	Class<? extends OutputStreamGraphics> clazz = map.get(type);
	if (clazz == null) {
	    throw new IllegalArgumentException(errorMsg("osgType", type));
	} else {
	    try {
		Constructor<? extends OutputStreamGraphics> constructor =
		clazz.getConstructor(OutputStream.class, int.class, int.class,
				     ImageOrientation.class,
				     String.class, boolean.class);
		return constructor.newInstance(os, width, height, orientation,
					       type, preferAlpha);
	    } catch (NoSuchMethodException e) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, e);
	    } catch (InstantiationException ee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, ee);
	    } catch (IllegalAccessException eee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eee);
	    } catch (InvocationTargetException eeee) {
		String msg = errorMsg("spError");
		throw new RuntimeException(msg, eeee);
	    }
	}
    }

    /**
     * Create an instance of OutputStreamGraphics with a scale factor.
     * @param os the output stream
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor;
     *        0.0 or negative values indicate that the scale factor should
     *        be computed to match any size constraints set by the
     *        image-format type
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, scaleFactor,
			   ImageOrientation.NORMAL, type, false);
    }

    /**
     * Create an instance of OutputStreamGraphics with a scale factor,
     * preferably with an alpha channel.
     * @param os the output stream
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor;
     *        0.0 or negative values indicate that the scale factor should
     *        be computed to match any size constraints set by the
     *        image-format type
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, String type, boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, scaleFactor,
			   ImageOrientation.NORMAL, type, preferAlpha);
    }

    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, and image orientation.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor;
     *        0.0 or negative values indicate that the scale factor should
     *        be computed to match any size constraints set by the
     *        image-format type
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, ImageOrientation orientation,
		    String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, scaleFactor, orientation,
			   type, false);
    }
    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, and image orientation, preferably with an alpha channel.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor;
     *        0.0 or negative values indicate that the scale factor should
     *        be computed to match any size constraints set by the
     *        image-format type
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, ImageOrientation orientation,
		    String type, boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	OutputStreamGraphics osg = newInstance(os, width, height, orientation,
					       type, preferAlpha);
	if (scaleFactor <= 0.0) {
	    scaleFactor = getBBScaleFactor(type, width, height, orientation);
	}
	osg.setScaleFactor(scaleFactor);
	return osg;
    }

    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, and translation.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor
     * @param xt the x translation in user space (the value of x that
     *           the point (0.0, 0.0) will be moved to)
     * @param yt the y translation in user space (the value of y that
     *           the point (0.0, 0.0) will be moved to)
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, double xt, double yt, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, scaleFactor, xt, yt, type, false);
    }

    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, and translation, preferably with an alpha channel.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor
     * @param xt the x translation in user space (the value of x that
     *           the point (0.0, 0.0) will be moved to)
     * @param yt the y translation in user space (the value of y that
     *           the point (0.0, 0.0) will be moved to)
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, double xt, double yt, String type,
		    boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	OutputStreamGraphics osg = newInstance(os, width, height,
					       ImageOrientation.NORMAL,
					       type, preferAlpha);
	osg.setScaleFactor(scaleFactor);
	osg.setTranslation(xt, yt);
	return osg;
    }

    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, translation, and image orientation.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor
     * @param xt the x translation in user space (the value of x that
     *           the point (0.0, 0.0) will be moved to)
     * @param yt the y translation in user space (the value of y that
     *           the point (0.0, 0.0) will be moved to)
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, double xt, double yt,
		    ImageOrientation orientation, String type)
	throws IllegalArgumentException, RuntimeException
    {
	return newInstance(os, width, height, scaleFactor, xt, yt,
			   orientation, type, false);
    }

    /**
     * Create an instance of OutputStreamGraphics specifying a height, width,
     * scale factor, translation, and image orientation,
     * preferably with an alpha channel.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * <P>
     * Note: while an alpha channel can be requested, so that the image
     * created will support transparency, it may not be possible to
     * grant this request, as some output formats, determined by the
     * <code>type</code> argument, do not support alpha channels.
     * @param os the output stream
     * @param width the image width in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param height the image height in user-space coordinates
     *        (used by Postscript only for determining an aspect ratio,
     *        as the image will be scaled to fit on a page)
     * @param scaleFactor a positive real number giving the scale factor
     * @param xt the x translation in user space (the value of x that
     *           the point (0.0, 0.0) will be moved to)
     * @param yt the y translation in user space (the value of y that
     *           the point (0.0, 0.0) will be moved to)
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     * @exception IllegalArgumentException an argument was illegal
     * @exception RuntimeException a runtime exception occurred, possibly
     *            due to an error in a service provider (check the cause
     *            for the exception)
     * @see #getImageTypes()
     * @see #getImageTypeForFile(File)
     * @see #getImageTypeForFile(String)
     * @see #getImageTypeForSuffix(String)
     */
    public static OutputStreamGraphics
	newInstance(OutputStream os, int width, int height,
		    double scaleFactor, double xt, double yt,
		    ImageOrientation orientation, String type,
		    boolean preferAlpha)
	throws IllegalArgumentException, RuntimeException
    {
	OutputStreamGraphics osg = newInstance(os, width, height,
					       orientation, type, preferAlpha);
	osg.setScaleFactor(scaleFactor);
	osg.setTranslation(xt, yt);
	return osg;
    }

    /**
     * Sets up graphics assuming a graphics context is a typical image.
     * When the orientation is COUNTERCLOCKWISE90 or CLOCKWISE90, the
     * graphics context will be translated by the width or height, and
     * then a rotation whose absolute value is 90 degrees will be
     * applied.  When the orientation is normal, there is no
     * translation or rotation.  Users can then draw objects as if
     * width was horizontal and height was vertical.  In addition, if
     * an alpha channel was not requested, the image will be filled with
     * a white background.
     * <P>
     * Note: some classes (e.g., postscript graphics) do not use
     * this method. It is not appropriate for all cases.
     * @param g2d the graphics context
     */
    protected void setupGraphicsForImages(Graphics2D g2d) {
	switch(orientation) {
	case COUNTERCLOCKWISE90:
	    g2d.translate(0.0, width);
	    g2d.rotate(-Math.PI/2.0);
	    break;
	case CLOCKWISE90:
	    g2d.translate(height, 0.0);
	    g2d.rotate(Math.PI/2.0);
	    break;
	default:
	    break;
	}
	if (!alpha) {
	    g2d.setColor(Color.white);
	    g2d.fillRect(0, 0, width, height);
	}
	g2d.setColor(Color.black);
    }


    /**
     * Apply the AffineTransform implied by a scale factor and translation
     * set by a call to newInstance.
     * <P>
     * This method should be used by subclasses when creating a
     * graphics context.  The main exception is postscript - the
     * postscript print drivers will scale an image to fit on a
     * page, so the use of this transform is pointless.
     * @param g2d the graphics context
     */
    protected void applyInitialTransform(Graphics2D g2d) {
	    double sf = getScaleFactor();
	    Point2D tp = getTranslation();
	    double xt = tp.getX();
	    double yt = tp.getY();
	    if (xt != 0.0 || yt != 0.0) {
		g2d.translate(xt, yt);
	    }
	    if (sf != 1.0) {
		g2d.scale(sf,sf);
	    }
    }

    private boolean alpha = false;

    /**
     * Determine if this instance is requesting an alpha channel.
     * The value may be changed from that provided in the constructor
     * due to the capabilities of a particular image format.
     * @return true if an alpha channel is requested; false otherwise
     */
    @Override
    public boolean requestsAlpha() {
	return alpha;
    }

    /**
     * Cancel a request for an alpha channel.
     * This method allows subclasses to cancel a request for an
     * alpha channel. It will typically be called in a constructor for
     * a subclass.
     */
    protected void cancelRequestedAlpha() {
	alpha = false;
    }

    /**
     * Constructor.
     * If the X value of a pixel that will be drawn is in the range
     * (0, width), it will be visible in the image created.  If the Y
     * value of a pixel that will be drawn is in the range (0,
     * height), it will be visible in the image created.
     * When an image orientation other than ImageOrientation.NORMAL is
     * specified, the operations are the same as if the image was
     * drawn with an image orientation of ImageOrientation.NORMAL, and
     * then (for other values of the image orientation) rotated by 90
     * degrees clockwise or counterclockwise, in which case the image
     * produced will have its height and width exchanged.
     * <P>
     * Constructors of subclasses must have these arguments.  The
     * constructor for subclasses will be called by one of the
     * <code>newInstance</code> methods.
     * @param os the output stream
     * @param width the image width (used by Postscript only for
     *        determining an aspect ratio, as the image will be scaled
     *        to fit on a page)
     * @param height the image height (used by Postscript only for
     *        determining an aspect ratio, as the image will be scaled
     *        to fit on a page)
     * @param orientation the image orientation
     * @param type a string naming an image-format type (valid types are
     *        discovered by calling {@link #getImageTypes() getImageTypes()}
     * @param preferAlpha true if an alpha channel is requested; false
     *        otherwise
     */
    protected OutputStreamGraphics(OutputStream os,
				   int width, int height,
				   ImageOrientation orientation,
				   String type,
				   boolean preferAlpha)
    {
	this.os = os;
	this.width = width;
	this.height = height;
	this.orientation = orientation;
	this.type = type.trim();
	alpha = preferAlpha;
    }

    /**
     * Get the color model for the image that will be produced.
     * @return the color model
     */
    @Override
    abstract public ColorModel getColorModel();

    /**
     * Get a graphics context for drawing.
     * The graphics context returned will implicitly handle any scaling or
     * translations specified in the call to newInstance.
     * @return a new graphics context.
     * @exception UnsupportedOperationException this operation is not
     *            supported, typically because the implementation can
     *            only create an instance of Graphics, not Graphics2D
     *            (something that would rarely, if ever, occur in
     *            practice)
     */
    @Override
    abstract public Graphics2D createGraphics()
	throws UnsupportedOperationException;

    /**
     * Flush the output.
     * The default implementation does not do anything. Subclasses
     * for which providing a partial image makes sense may choose
     * to implement this method. Such subclasses should be implemented
     * so that a call to {@link #flush()} is not needed after
     * a call to {#link #imageComplete()}.;
     *
     */
    @Override
    public void flush() throws IOException {}


    @Override
    public boolean canReset() {return false;}

    @Override
    public void reset() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    /**
     * Final processing for writing an image file to the output stream.
     * 
     * Does not close the stream, but will flush it.
     * @exception IOException IO failure or a PrintException (which will be
     *            provided as the cause of the IOException)
     */
    @Override
    public abstract void imageComplete() throws IOException;

    /**
     * This class represents a surrogate OSGraphicsOps.
     * It is is used as a placeholder for an instance of OSGraphicsOps
     * that will be provided at a later point in time, and its constructor
     * is expected to provide quantities that are compatible with
     * the instance of OSGraphicsOps that will replace this object.
     * <P>
     * One of its uses is in a constructor for {@link org.bzdev.graphs.Graph},
     * where an argument to the constructor is an instance of
     * {@link org.bzdev.gio.ISWriterOps}.
     */
    public static class Surrogate implements OSGraphicsOps {
	int width;
	int height;
	boolean requestAlpha;

	Graphics2D g2d;

	/**
	 * Constructor.
	 * @param width the width of the drawing area
	 * @param height the height of the drawing area
	 * @param requestAlpha true if the drawing area should be
	 *        configured with an alpha channel; false otherwise
	 */
	public Surrogate(int width, int height, boolean requestAlpha) {
	    g2d = new SurrogateGraphics2D(width, height, requestAlpha);
	    this.width = width;
	    this.height = height;
	    this.requestAlpha = requestAlpha;
	}

	@Override
	public boolean requestsAlpha() {return requestAlpha;}
	@Override
	public int getWidth() {return width;}
	@Override
	public int getHeight() {return height;}
	@Override
	public void close() {}
	@Override
	public void flush() {}
	@Override
	public ColorModel getColorModel() {
	    return g2d.getDeviceConfiguration().getColorModel();
	}
	@Override
	public Graphics2D createGraphics()
	    throws UnsupportedOperationException
	{
	    return (Graphics2D) g2d.create();
	}
	@Override
	public boolean canReset() {return false;}
	@Override
	public void reset() {}
	@Override
	public void imageComplete() {}
    }
}

//  LocalWords:  exbundle newInstance os ImageOrientation scaleFactor
//  LocalWords:  xtranslation ytranslation ul li ps png jpeg keySet
//  LocalWords:  OutputStreamGraphics getImageTypes createGraphics xt
//  LocalWords:  getImageTypeForFile getImageTypeForSuffix javax gif
//  LocalWords:  imageComplete suffixSet DisjointSetsUnion suffixMap
//  LocalWords:  ImageMimeInfo getSuffixSet imageType mediaType SPI
//  LocalWords:  imageio filename ZipOutputStream IOException spError
//  LocalWords:  getDefaultWidth nonInteger noIntMethod clazz runtime
//  LocalWords:  getDefaultHeight toString methodName osgType yt bmp
//  LocalWords:  IllegalArgumentException SecurityException wbmp svg
//  LocalWords:  RuntimeException UnsupportedOperationException
//  LocalWords:  PrintException subpackages preferAlpha boolean
