package org.bzdev.gio.spi;
import org.bzdev.gio.OutputStreamGraphics;

/**
 * Service provider interface for OutputStreamGraphics.
 * The names of the classes implementing this interface
 * and appearing in a jar file should be placed in a file named
 * <blockquote>
 * META-INF/services/org.bzdev.gio.spi.OSGProvider
 * </blockquote>
 * and that file should be included in the jar file.
 * For a modular jar file, the module-info.jar file's module
 * declaration should contain
 * <BLOCKQUOTE><CODE><PRE>
 *     uses org.bzdev.graphs.spi.SymbolProvider;
 *     provides org.bzdev.graphs.spi.SymbolProvider with ...;
 * </PRE></CODE></BLOCKQUOTE>
 * where "<CODE>...</CODE>" is a comma-separated list of the
 * fully-qualified class names of the SymbolProvider providers that
 * the JAR file contains.
 * <P>
 * The image types that a provider supports will generally consist of
 * a list of standard names, followed by ones that indicate this
 * particular package.  For example, a provider for SVG (Scalable
 * Vector Graphics) images might use the types "svg", "SVG", and
 * "svg-NAME" where NAME refers to a specif implementation. An example
 * is "svg-batik" for the Apache Batik SVG implementation. A useful
 * convention is to use a '-' in a name only to separate a generic
 * name for an image type from a name referring to a particular
 * implementation.
 * <P>
 * If two providers use the same image-type names, duplicates will be
 * ignored. Providers are loaded by the class
 * {@link java.util.ServiceLoader} and the documentation for this class
 * describes how service providers are used.
 */
public interface OSGProvider {
    /**
     * Get the image types this OSGProvider supports.
     * The image type is name for the image format such as
     * "ps", or "jpeg", not a media type (MIME type), and
     * is intended for use within a java application, not
     * elsewhere.
     * 
     * @return the image types this OSGProvider supports
     */
    String[] getTypes();

    /**
     * Get the media type (MIME type) for an image
     * type supported by this provider.
     * @return the media type; null if the image type is
     * not supported by this provider.
     */
    String getMediaType(String imageType);

    /**
     * Get the file-name suffixes for an image type supported by
     * this provider.
     * Suffixes are the same as file-name extensions.
     * @return a list of file name suffixes; null if
     *         this provider does not support an image type.
     */
    String[] getSuffixes(String imageType);


    /**
     * Get the subclass of OutputStreamGraphics for a provider.
     * The subclass of OutputStreamGraphics returned must include
     * a public constructor with five arguments:
     * <ul>
     *   <li> the output stream (java.io.OutputStream).
     *   <li> the image width (int) in user-space coordinates.
     *   <li> the image height (int) in user-space coordinates.
     *   <li> the image type (String).
     *   <li> the preferAlpha flag (boolean). When <code>true</code>,
     *        an alpha channel is requested; otherwise the value is
     *        false. Whether or not an alpha channel can be used depends
     *        on the image type - some image formats do not support
     *        alpha channels.
     * </ul>
     * This constructor must call its superclass' constructor with
     * the first four arguments that match those listed above.
     * Each subclass of OutputStreamGraphics must also provide two
     * public static methods
     * <ul>
     *  <li> public static Integer getDefaultWidth(ImageOrientation)
     *  <li> public static Integer getDefaultHeight(ImageOrientation)
     * </ul>
     * that return the default width and height of an image in units
     * of points.  The value may or may not be dependent on the image
     * orientation. For many image formats (e.g., PNG and JPEG) the
     * default sizes are not dependent on an orientation. For Postscript,
     * they are because Java handles Postscript as a printing function
     * in which the values that can be printed are constrained to fit on
     * a page of a specific size.
     * @return the subclass of OutputStreamGraphics for this provider
     */
    Class<? extends OutputStreamGraphics> getOsgClass();
}

//  LocalWords:  OutputStreamGraphics blockquote PRE SymbolProvider
//  LocalWords:  SVG Scalable svg OSGProvider ps jpeg ul li boolean
//  LocalWords:  preferAlpha superclass getDefaultWidth PNG
//  LocalWords:  ImageOrientation getDefaultHeight
