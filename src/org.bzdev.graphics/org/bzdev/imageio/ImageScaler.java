package org.bzdev.imageio;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.net.*;
import java.security.*;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.imageio.lpack.ImageIO

/**
 * Class for scaling images.
 * The class supports various ways of specifying IO.
 */
public class ImageScaler {
    /**
     * Constructor.
     */
    public ImageScaler() {}

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.imageio.lpack.ImageIO");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    /**
     * Create a scaled image file given file names for the input and output
     * images.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param inFileName the name of the input file
     * @param outFileName the name of the output file
     * @param type the image type to create:
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */ 
    public void scaleImage(int maxScaledWidth, int maxScaledHeight,
			   String inFileName, String outFileName,
			   String type)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	if (outFileName == null || inFileName == null) 
	    throw new NullPointerException(errorMsg("nullFileName"));
	File outputFile = new File (outFileName);
	scaleImage(maxScaledWidth, maxScaledHeight, inFileName, outputFile,
			type);
    }

    int lastImageWidth = 0;
    int lastImageHeight = 0;

    /**
     * Get the width of the last image that was scaled.
     * @return the image width in pixels
     */
    public int getLastImageWidth() {
	return lastImageWidth;
    }

    /**
     * Get the height of the last image that was scaled.
     * @return the image height in pixels
     */
    public int getLastImageHeight() {
	return lastImageHeight;
    }


    /**
     * Create a scaled buffered image file from an image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param image the image to scale
     * @return a scaled image
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */ 
    public BufferedImage scaleImage(int maxScaledWidth,
				    int maxScaledHeight,
				    Image image)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	int imageWidth = image.getWidth(null);
	int imageHeight = image.getHeight(null);
	boolean useHeightAlone = false;
	boolean useWidthAlone = false;
	if (maxScaledWidth == -1 && maxScaledHeight == -1) {
	    maxScaledWidth = 0;
	    maxScaledHeight = 0;
	}
	if (maxScaledWidth == 0) {
	    maxScaledWidth = imageWidth;
	} else if (maxScaledWidth == -1) {
	    useHeightAlone = true;
	    maxScaledWidth = imageWidth;
	}
	if (maxScaledHeight == 0) {
	    maxScaledHeight = imageHeight;
	} else if (maxScaledHeight == -1) {
	    useWidthAlone = true;
	    maxScaledHeight = imageHeight;
	}
	double scaledAspectRatio = 
	    (double)maxScaledWidth / (double)maxScaledHeight;
	double aspectRatio = ((double)imageWidth) / ((double)imageHeight);
	int scaledImageWidth;
	int scaledImageHeight;
	if (useHeightAlone) {
	    scaledImageHeight = maxScaledHeight;
	    scaledImageWidth = (int)
		(imageWidth *((double)scaledImageHeight/(double)imageHeight));
	} else if (useWidthAlone) {
	    scaledImageWidth = maxScaledWidth;
	    scaledImageHeight = (int)
		(imageHeight * ((double)scaledImageWidth/(double)imageWidth));
	} else if (scaledAspectRatio < aspectRatio) {
	    scaledImageWidth = maxScaledWidth;
	    scaledImageHeight = (int)(maxScaledWidth / aspectRatio);
	} else {
	    scaledImageWidth = (int)(maxScaledHeight * aspectRatio);
	    scaledImageHeight = maxScaledHeight;
	}
	BufferedImage scaledImage = new 
	    BufferedImage(scaledImageWidth, scaledImageHeight,
			  BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics2D = scaledImage.createGraphics();
	graphics2D.setRenderingHint
	    (RenderingHints.KEY_INTERPOLATION,
	     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	graphics2D.drawImage(image, 0, 0, 
			     scaledImageWidth, scaledImageHeight,
			     null);
	lastImageWidth = scaledImageWidth;
	lastImageHeight = scaledImageHeight;
	return scaledImage;
    }

    /**
     * Create a scaled buffered Image file given a file name for the input
     * image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param inputFileName the name of the input file.
     * @return a scaled image
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */ 
    public BufferedImage scaleImage(int maxScaledWidth,
				    int maxScaledHeight, 
				    String inputFileName)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	Image image = 
	    Toolkit.getDefaultToolkit().getImage(inputFileName);
	MediaTracker mediaTracker = new MediaTracker(new Container());
	mediaTracker.addImage(image, 0);
	mediaTracker.waitForID(0);
	if (mediaTracker.isErrorID(0)) {
	    throw new IOException(errorMsg("readImageFailed"));
	}
	return scaleImage(maxScaledWidth, maxScaledHeight, image);
    }

    /**
     * Create a scaled buffered Image file given a URL for the input image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param url the URL of the input image
     * @return a scaled image
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     *
     */
    public BufferedImage scaleImage
	(int maxScaledWidth, int maxScaledHeight, URL url)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	Image image = 
	    Toolkit.getDefaultToolkit().getImage(url);
	MediaTracker mediaTracker = new MediaTracker(new Container());
	mediaTracker.addImage(image, 0);
	mediaTracker.waitForID(0);
	if (mediaTracker.isErrorID(0)) {
	    throw new IOException(errorMsg("readImageFailed"));
	}
	return scaleImage(maxScaledWidth, maxScaledHeight, image);
    }

     private static AccessControlContext context = null;
     // list of paths for which we want to allow file access.

    // Need privileges to read the java.io.tmpdir property
    private static String getTempDirPattern() {
	return AccessController.doPrivileged
	    (new PrivilegedAction<String>() {
		public String run() {
		    return System.getProperty("java.io.tmpdir")
			+ System.getProperty("file.separator") + "-";
		}
	    });
    }

     // Used by 'write' methods where ImageIO tries to create
     // temporary files.
     private static String[] filePatterns = {
	 getTempDirPattern()
     };

     private static synchronized void initACC() {
	 if (context == null) {
	     PermissionCollection permissions = new Permissions();
	     for (String target: filePatterns) {
		 permissions.add(new FilePermission(target,
						    "read,write,delete"));
	     }
	     ProtectionDomain domain = new ProtectionDomain(null, permissions);
	     context = new AccessControlContext(new ProtectionDomain[]{domain});
	 }
     }

    /**
     * Create a scaled image file given a file name for the input image and
     * a file for the output image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param inputFileName the name of the input file 
     * @param outputFile the output file
     * @param type the image type to create:
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */
    public void scaleImage(int maxScaledWidth, int maxScaledHeight,
			   String inputFileName, final File outputFile,
			   final String type)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	if (outputFile == null || inputFileName == null) 
	    throw new NullPointerException(errorMsg("nullFileName"));

	 String name = (outputFile != null? outputFile.getPath(): null);
	 /*
	 SecurityManager sm = System.getSecurityManager();
	 if (sm != null && name != null) {
	     sm.checkWrite(name);
	 }
	 */
	if (outputFile.exists() && !outputFile.canWrite())
	    throw new IllegalArgumentException(errorMsg("cannotWrite", name));
	final BufferedImage scaledImage =
	    scaleImage(maxScaledWidth, maxScaledHeight, inputFileName);
	 // since the security manager (if any) allowed the file to be
	 // written, we can use a doPrivileged block, which is needed because
	 // ImageIO.write creates a temporary file and there is no default
	 // permission allowing that.
	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(scaledImage, type, outputFile)) {
			     throw new IOException
				 (errorMsg("cannotWriteImg", type));
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
    }


    /**
     * Create a scaled image file given a URL for the input image and a
     * file for the output image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param url the URL for the image source 
     * @param outputFile the output file
     * @param type the image type to create:
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */
    public void scaleImage(int maxScaledWidth, int maxScaledHeight,
				       URL url, final File outputFile,
				       final String type)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	if (outputFile == null || url == null) 
	    throw new NullPointerException(errorMsg("nullArgument"));
	 String name = (outputFile != null? outputFile.getPath(): null);
	 /*
	 SecurityManager sm = System.getSecurityManager();
	 if (sm != null && name != null) {
	     sm.checkWrite(name);
	 }
	 */
	if (outputFile.exists() && !outputFile.canWrite())
	    throw new IllegalArgumentException(errorMsg("cannotWrite", name));
	final BufferedImage scaledImage =
	    scaleImage(maxScaledWidth, maxScaledHeight, url);
	 // since the security manager (if any) allowed the file to be
	 // written, we can use a doPrivileged block, which is needed because
	 // ImageIO.write creates a temporary file and there is no default
	 // permission allowing that.
	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(scaledImage, type, outputFile)) {
			     throw new IOException
				 ("cannot create a file an image type of "
				  + type);
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
    }

    /**
     * Create a scaled image file given a file name for the input image
     * and an output stream for the output image.
     * The supported input file formats are those supported by Java.
     * The image will not be distorted and will be as large as possible
     * while fitting into a rectangle whose height and width are given
     * by the arguments maxScaledWidth and maxScaledHeight respectively.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param inputFileName the name of the input file 
     * @param os the output stream
     * @param type the image type to create:
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */

    public void scaleImage(int maxScaledWidth, int maxScaledHeight,
			   String inputFileName, final OutputStream os,
			   final String type)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	if (os == null || inputFileName == null || type == null)
	    throw new NullPointerException(errorMsg("nullArgument"));
	final BufferedImage scaledImage =
	    scaleImage(maxScaledWidth, maxScaledHeight, inputFileName);

	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(scaledImage, type, os)) {
			     throw new IOException
				 ("cannot create a file an image type of "
				  + type);
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
    }

    /**
     * Create a scaled image file given a URL for the input image and
     * an output stream for the output image.
     * The supported input file formats are those supported by Java.
     * <P>
     * If both maxScaledWdith and MaxScaledHeight are -1, they are set to 0.
     * @param maxScaledWidth the maximum width of a scaled image in pixels;
     *        0 for the image width and -1 if the width is not constrained
     * @param maxScaledHeight the maximum height of a scaled image in pixels;
     *        0 for the image height and -1 if the height is not constrained
     * @param url the URL for the image source
     * @param os the output stream
     * @param type the image type to create:
     * @exception IllegalArgumentException  generally an illegal input or
     *            output file name
     * @exception IOException read or write operations failed
     * @exception InterruptedException  the current thread was interrupted
     */

    public void scaleImage(int maxScaledWidth, int maxScaledHeight,
				       URL url, final OutputStream os,
				       final String type)
	throws IllegalArgumentException, IOException, InterruptedException
    {
	if (os == null) {
	    throw new NullPointerException(errorMsg("nullOS"));
	}
	if (url == null) {
	    throw new NullPointerException(errorMsg("nullURL"));
	}
	final BufferedImage scaledImage =
	    scaleImage(maxScaledWidth, maxScaledHeight, url);
	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(scaledImage, type, os)) {
			     throw new IOException
				 (errorMsg("cannotWriteImg", type));
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
    }
}

//  LocalWords:  exbundle maxScaledWidth maxScaledHeight inFileName
//  LocalWords:  maxScaledWdith outFileName IllegalArgumentException
//  LocalWords:  IOException InterruptedException nullFileName url os
//  LocalWords:  inputFileName readImageFailed ImageIO outputFile
//  LocalWords:  cannotWrite doPrivileged cannotWriteImg nullArgument
//  LocalWords:  nullOS nullURL
