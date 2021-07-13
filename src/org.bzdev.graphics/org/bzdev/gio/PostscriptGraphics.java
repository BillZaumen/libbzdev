package org.bzdev.gio;
import org.bzdev.lang.ExceptionedCallable;
import java.io.*;
import java.awt.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

// Needed due to openjdk-8 trying to install assistive technology
// which is not needed in this case as we are only writing to an
// output stream specifically to create a postscript file.

import java.security.*;


//@exbundle org.bzdev.gio.lpack.Gio


class PostscriptGraphics extends ThreadedOSGraphics implements GraphicsCreator
{

    static String errorMsg(String key, Object... args) {
	return OutputStreamGraphics.errorMsg(key, args);
    }

    static SizePrinter defaultSize = new SizePrinter();

    // 6.5" wide, 9" high, so it fits on a page.
    private static final int DEFAULT_PAGEWIDTH = 6*72 + 36;
    private static final int DEFAULT_PAGEHEIGHT = 9*72;

    private static Integer getDefaultPageWidth() {
	int result = defaultSize.getWidth();
	return (result < 0)? DEFAULT_PAGEWIDTH: result;
    }

    private static Integer getDefaultPageHeight() {
	int result = defaultSize.getHeight();
	return (result < 0)? DEFAULT_PAGEHEIGHT: result;
    }

    /**
     * Get the default width.
     * @param orientation the image orientation
     * @return the width in units of points
     */
    public static Integer getDefaultWidth(ImageOrientation orientation) {
	
	switch (orientation) {
	case NORMAL:
	    return PostscriptGraphics.getDefaultPageWidth();
	default:
	    return PostscriptGraphics.getDefaultPageHeight();
	}
    }

    /**
     * Get the default height.
     * @param orientation the image orientation
     * @return the height in units of points
     */
    public static Integer getDefaultHeight(ImageOrientation orientation) {
	switch (orientation) {
	case NORMAL:
	    return PostscriptGraphics.getDefaultPageHeight();
	default:
	    return PostscriptGraphics.getDefaultPageWidth();
	}
    }

    // print to a byte array output stream that we can ignore so we
    // can get the image size.
    static class SizePrinter implements Printable {

	static String errorMsg(String key, Object... args) {
	    return OutputStreamGraphics.errorMsg(key, args);
	}

	SizePrinter() {
	    ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
	    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	    String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
	    StreamPrintServiceFactory[] factories =
		StreamPrintServiceFactory.lookupStreamPrintServiceFactories
		(flavor, psMimeType);
	    if (factories.length == 0) {
		throw new RuntimeException(errorMsg("noPostscript"));
	    }
	    StreamPrintService sps = factories[0].getPrintService(os);
	    DocPrintJob pj = sps.createPrintJob();
	    PrintRequestAttributeSet aset = 
		new HashPrintRequestAttributeSet();
	    Doc doc = new SimpleDoc(this, flavor, null);
	    try {
		pj.print(doc, aset);
	    } catch (Exception e) {
	    }
	    os = null; factories = null; sps = null; doc = null; aset = null;
	}
	boolean firstTime = true;
	double width = -1.0;
	double height = -1.0;
	public int print(Graphics g, PageFormat pf, int pageIndex) {
	    if (pageIndex == 0) {
		if (firstTime) {
		    firstTime = false;
		    width = pf.getImageableWidth();
		    height = pf.getImageableHeight();
		}
		return Printable.PAGE_EXISTS;
	    } else {
		return Printable.NO_SUCH_PAGE;
	    }
	}
	int getWidth() {return (int)Math.round(width);}
	int getHeight() {return (int)Math.round(height);}
    }

    protected double getImplScaleFactor(ImageOrientation orientation) {
	double w = (double) getWidth();
	double h = (double) getHeight();
	double scaleX = PostscriptGraphics.getDefaultWidth(orientation) / w;
	double scaleY = PostscriptGraphics.getDefaultHeight(orientation) / h;
	double scale = (scaleX < scaleY)? scaleX: scaleY;
	return (scale < 1.0)? scale: 1.0;
    }


    class PostscriptGraphicsPrinter
	implements Printable, ThreadedOSGraphics.GraphicsWriter
    {
	OutputStream os;
	PostscriptGraphics pg;

	private String errorMsg(String key, Object... args) {
	    return OutputStreamGraphics.errorMsg(key, args);
	}

	PostscriptGraphicsPrinter(PostscriptGraphics pg, OutputStream os) {
	    this.pg = pg;
	    this.os = os;
	}

	private void printDocument() throws PrintException, IOException {
	    DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
	    String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
	    StreamPrintServiceFactory[] factories =
		StreamPrintServiceFactory.lookupStreamPrintServiceFactories
		(flavor, psMimeType);
	    if (factories.length == 0) {
		throw new RuntimeException(errorMsg("noPostscript"));
	    }
	    try {
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
				public Void run() throws Exception {
				    ProtectionDomain d = factories[0].getClass()
					.getProtectionDomain();
				    if (d != null) {
					if (d.getCodeSource() != null) {
					    throw new SecurityException
						(errorMsg("notPrimordial"));
					}
				    }
				    return (Void) null;
				}
			});
	    } catch (PrivilegedActionException e) {
		Exception ee = e.getException();
		setWriteException(ee);
	    } catch (Exception e) {
		setWriteException(e);
	    }
	    StreamPrintService sps = factories[0].getPrintService(os);
	    DocPrintJob pj = sps.createPrintJob();
	    PrintRequestAttributeSet aset = 
		new HashPrintRequestAttributeSet();
	    Doc doc = new SimpleDoc(this, flavor, null);
	    pj.print(doc, aset);
	    os.flush();
	}

	public int print(Graphics g, PageFormat pf, int pageIndex) {
	    if (pageIndex == 0) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.translate(pf.getImageableX(), pf.getImageableY()); 
		double isf;
		switch(pg.getOrientation()) {
		case COUNTERCLOCKWISE90:
		    isf = getImplScaleFactor(getOrientation());
		    g2d.translate(0.0, isf*pg.getWidth());
		    g2d.rotate(-Math.PI/2.0);
		    break;
		case CLOCKWISE90:
		    isf = getImplScaleFactor(getOrientation());
		    g2d.translate(isf*pg.getHeight(), 0.0);
		    g2d.rotate(Math.PI/2.0);
		    break;
		}
		try {
		    // Need a doPrivileged block because the paint code
		    // tries to use GNOME assistive technologies for some
		    // strange reason. While we are running this in a
		    // doPrivileged block, we previously checked that the
		    // print service factory that is in use was loaded by
		    // the primordial class loader so that a third party's
		    // jar file cannot provide a malicious print service
		    // that exploits the use of this doPrivileged block.
		    AccessController.doPrivileged
			(new PrivilegedExceptionAction<Void>() {
				public Void run() throws Exception {
				    pg.paint(g2d);
				    return (Void) null;
				}
			    });
		} catch (PrivilegedActionException e) {
		    Exception ee = e.getException();
		    setWriteException(ee);
		} catch (Exception e) {
		    setWriteException(e);
		}
		return Printable.PAGE_EXISTS;
	    } else {
		return Printable.NO_SUCH_PAGE;
	    }
	}

	public void writeGraphics() throws Exception {
	    printDocument();
	}
    }

    protected ThreadedOSGraphics.GraphicsWriter newGraphicsWriter() {
	return new PostscriptGraphicsPrinter(this, getOutputStream());
    }

    /**
     * Constructor requesting an alpha channel.
     * The width, height, and type parameters may be recovered using
     * access methods, but their values are not used by this class.
     * The alpha-channel request is ignored, but the constructor is
     * necessary.
     * @param os an output stream to which a postscript file will be
     *        written.
     * @param width the image width
     * @param height the image height
     * @param orientation the image orientation
     * @param type the string "ps"
     * @param preferAlpha ignored
     */
    public PostscriptGraphics(OutputStream os, int width, int height,
			      ImageOrientation orientation, String type,
			      boolean preferAlpha)
    {
	super(os, width, height, orientation, type, false);
    }

}

//  LocalWords:  openjdk exbundle noPostscript notPrimordial os ps
//  LocalWords:  doPrivileged preferAlpha
