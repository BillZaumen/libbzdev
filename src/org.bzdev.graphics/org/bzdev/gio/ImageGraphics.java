package org.bzdev.gio;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.Point2D;
import java.io.*;
import javax.imageio.*;
import java.security.*;
import java.util.HashSet;

//@exbundle org.bzdev.gio.lpack.Gio

class ImageGraphics extends OutputStreamGraphics implements GraphicsCreator {

    OutputStream os;
    BufferedImage image;

    Graphics2D g2d;

    String type;

    static String errorMsg(String key, Object... args) {
	return OutputStreamGraphics.errorMsg(key, args);
    }

    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    /**
     * Get the default width.
     * @param orientation the image orientation
     * @return the width in units of points
     */
    public static Integer getDefaultWidth(ImageOrientation orientation) {
	return DEFAULT_WIDTH;
    }

    /**
     * Get the default height.
     * @param orientation the image orientation
     * @return the height in units of points
     */
    public static Integer getDefaultHeight(ImageOrientation orientation) {
	return DEFAULT_HEIGHT;
    }


    static HashSet<String> hasAlpha = new HashSet<>();
    static {
	hasAlpha.add("image/png");
    }

    /**
     * Constructor.
     * @param os an output stream to which an image file will be
     *        written.
     * @param width the image width
     * @param height the image height
     * @param orientation the image orientation
     * @param type the image type
     * @param preferAlpha true if an alpha channel is desired;
     *        false otherwise
     */
    public ImageGraphics(OutputStream os, int width, int height,
			 ImageOrientation orientation,
			 String type, boolean preferAlpha)
    {
	super(os, width, height, orientation, type, preferAlpha);
	this.os = os;
	if (preferAlpha) {
	    String mediaType =
		OutputStreamGraphics.getMediaTypeForImageType(type);
	    if (mediaType == null || !hasAlpha.contains(mediaType)) {
		// only allow alpha channels for output formats
		// known to support it.
		cancelRequestedAlpha();
		preferAlpha = false;
	    }
	}
	switch(orientation) {
	case NORMAL:
	    image = new
		BufferedImage(width, height,
			      (preferAlpha? BufferedImage.TYPE_INT_ARGB_PRE:
			       BufferedImage.TYPE_INT_RGB));
	    break;
	default:
	    image = new
		BufferedImage(height, width,
			      (preferAlpha? BufferedImage.TYPE_INT_ARGB_PRE:
			       BufferedImage.TYPE_INT_RGB));
	    break;
	}
	g2d = image.createGraphics();
	setupGraphicsForImages(g2d);
	applyInitialTransform(g2d);
        g2d.setRenderingHint
            (RenderingHints.KEY_INTERPOLATION,
             RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    }

    public ColorModel getColorModel() {
	return image.getColorModel();
    }

    public Graphics2D createGraphics() throws UnsupportedOperationException {
	Graphics g = g2d.create();
	if (g instanceof Graphics2D) {
	    return (Graphics2D) g;
	} else {
	    throw new UnsupportedOperationException
		(errorMsg("cannotCreateG2D"));
	}
    }

     private static AccessControlContext context = null;

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

     // list of paths for which we want to allow file access.
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

    boolean done = false;

    public void imageComplete() throws IOException {
	// ImageIO.write needs to be in a doPrivileged block because
	// it needs permissions to open a temporary file, the name of
	// which is not documented.

	if (done) throw new IOException(errorMsg("imageComplete"));
	done = true;
	try {
	    initACC();
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
		    public Void run() throws IOException {
			if (!ImageIO.write(image, getType(),  os)) {
			    throw new IOException
				(errorMsg("cannotWriteIT", getType()));
			}
			return null;
		    }
		}, context);
	} catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
    }
}

//  LocalWords:  exbundle png os preferAlpha cannotCreateG ImageIO
//  LocalWords:  doPrivileged imageComplete cannotWriteIT
