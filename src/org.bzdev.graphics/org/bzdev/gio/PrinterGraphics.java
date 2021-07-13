package org.bzdev.gio;
import org.bzdev.util.SafeFormatter;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.print.*;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

//@exbundle org.bzdev.gio.lpack.Gio

/**
 * Output-Stream graphics implementation for printing.
 * This class allows the OSGraphics interface to be used for
 * printing. Aside from some printer-specific arguments in
 * the constructors, one will typically specify an image
 * height and width used for drawing.  The image will then
 * be scaled to fit on a page.
 * <P>
 * As an example, one can print a graph to the default printer
 * as follows:
 * <blockquote><code><pre>
 * import org.bzdev.graph.*;
 * import org.bzdev.gio.*;
 * ...
 *     int width = ...;
 *     int height = ...;
 *     PrinterGraphics pg = new PrinterGraphics(width, height);
 *     Graph graph = new Graph(pg);
 *     ...
 *     graph.write();
 * </pre></code></blockquote>
 * The implementation of <code>graph.write()</code> calls
 * <code>pg.imageComplete()</code>, which initiates the actual printing.
 * <P>
 * To allow the user to choose a printer, one can call
 * <blockquote><code><pre>
 *      ...
 *	PrinterJob pjob = PrinterJob.getPrinterJob();
 *      if (pjob.printDialog()) {
 *          PrinterGraphics pg = new PrinterGraphics(pjob, null, width, height);
 *          ...
 *      }
 *   PrinterJob job = 
 * </pre></code></blockquote>
 * In this case, the orientation will be chosen to maximize the size of
 * the printed image.
 * <P>
 * To allow the user to choose a printer, orientation, and other
 * properties, use
 * <blockquote><code><pre>
 * import bzdev.gio.PrinterGraphics;
 * import java.awt.print.*;
 * import javax.print.attribute.*;
 * import javax.print.attribute.standard.*;
 *      ...
 *      PrintRequestAttributeSet aset = new PrintRequestAttributeSet();
 *	PrinterJob pjob = PrinterJob.getPrinterJob();
 *      if (pjob.printDialog(aset)) {
 *          PrinterGraphics pg = new PrinterGraphics(pjob, aset, width, height);
 *          ...
 *      }
 * </pre></code></blockquote>
 */
public class PrinterGraphics implements OSGraphicsOps {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.gio.lpack.Gio");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    boolean useScaling = true;
    int width;
    int height;
    double dwidth;
    double dheight;
    Graphics2D sg2d;
    Graphics2DRecorder recorder;
    PrinterJob pjob;
    PrintRequestAttributeSet aset;

    private static PrintRequestAttributeSet
	asetDefaults(int width, int height)
    {
	if (width <= height) {
	    return new HashPrintRequestAttributeSet
		(OrientationRequested.PORTRAIT);
	} else {
	    return new HashPrintRequestAttributeSet
		(OrientationRequested.LANDSCAPE);
	}
    }

    /**
     * Set the print-request attribute for the image orientation if missing.
     * This method adds an OrientationRequested attribute to a
     * PrintRequestAttributeSet if one is not already present. The
     * orientation is based on the width and height arguments: if
     * the width is larger than the height, a landscape orientation
     * is chosen; otherwise a portrait orientation is 
     * This should be called before a print dialog is shown to
     * preselect the orientation.  A print dialog
     * can override this selection, if desired.
     * @param aset the print-request attribute set to modify
     * @param width the width of an image to be printed
     * @param height the height of an image to be printed
     * @exception NullPointerException the attribute set argument was
     *            null
     */
    public static void setAttributes(PrintRequestAttributeSet aset,
				     int width, int height)
	throws NullPointerException
				     
    {
	if (aset == null) {
	    throw new NullPointerException(errorMsg("nullAttributeSet"));
	}
	if (!aset.containsKey(OrientationRequested.class)) {
	    if (width <= height) {
		aset.add(OrientationRequested.PORTRAIT);
	    } else {
		aset.add(OrientationRequested.LANDSCAPE);
	    }
	}
    }

    /**
     * Constructor.
     * The default printer will be used.
     * The orientation (portrait or landscape) will be chosen so that
     * the printed image will be as large as possible
     * @param width the width in pixels for the area to be printed
     * @param height the height in pixel for the area to be printed
     * @exception PrinterException a suitable printer was not available
     * @exception IllegalArgumentException the width or height was not
     *            a positive integer
     */
    public PrinterGraphics(int width, int height)
	throws PrinterException, IllegalArgumentException
    {
	this(PrinterJob.getPrinterJob(), asetDefaults(width, height),
	     width, height);
    }


    /**
     * Constructor specifying a print job, a print-request attribute set,
     * a width, and a height.
     * Unlike the other constructor, the orientation (portrait or
     * landscape) is the one explicitly provided in the attribute set,
     * provided that the attribute set is not null.  If the attribute
     * set is null, the orientation is set to make the printed image
     * as large as possible.
     * @param pjob the print job
     * @param aset the print job's attributes; null for defaults
     * @param width the width in pixels for the area to be printed
     * @param height the height in pixel for the area to be printed
     * @exception PrinterException a suitable printer was not available
     * @exception IllegalArgumentException the width or height was not
     *            a positive integer
     */
    public PrinterGraphics(PrinterJob pjob, PrintRequestAttributeSet aset,
			   int width, int height)
	throws PrinterException, IllegalArgumentException
    {
	if (pjob == null || pjob.getPrintService() == null) {
	    throw new PrinterException(errorMsg("noPrintersAvailable"));
	}
	if (aset == null) {
	    aset = asetDefaults(width, height);
	}
	if (width <= 0 || height <= 0) {
	    throw new IllegalArgumentException
		(errorMsg("badWidthHeight", width, height));
	}
	this.pjob = pjob;
	this.aset = aset;
	this.width = width;
	this.height = height;
	dwidth = (double)width;
	dheight = (double)height;
	sg2d = new SurrogateGraphics2D(width,height, false);
	recorder = new Graphics2DRecorder(sg2d);
    }

    /**
     * Constructor specifying an orientation.
     * The default printer will be used and the image will not be
     * scaled.
     * @param landscape true if a landscape orientation should be used;
     *        false if a portrait orientation should be used
     */
    public PrinterGraphics(boolean landscape)
	throws PrinterException
    {
	this(PrinterJob.getPrinterJob(), null, landscape);
    }

    /**
     * Constructor specifying a print job, a print-request attribute set,
     * and an explicit image orientation.
     * @param pjob the print job
     * @param aset the print job's attributes; null for defaults
     * @param landscape true if a landscape orientation should be used;
     *        false if a portrait orientation should be used
     */
    public PrinterGraphics(PrinterJob pjob, PrintRequestAttributeSet aset,
			   boolean landscape)
	throws PrinterException
    {
	if (pjob == null || pjob.getPrintService() == null) {
	    throw new PrinterException(errorMsg("noPrintersAvailable"));
	}
	if (aset == null) {
	    aset = new HashPrintRequestAttributeSet
		(landscape? OrientationRequested.LANDSCAPE:
		 OrientationRequested.PORTRAIT);
	} else {
	    aset.add(landscape? OrientationRequested.LANDSCAPE:
		 OrientationRequested.PORTRAIT);
	}
	this.pjob = pjob;
	this.aset = aset;
	useScaling = false;
	PageFormat pf = pjob.getPageFormat(aset);
	double ix = pf.getImageableX();
	double iy = pf.getImageableY();
	// A test showed that pf.getWidth() and pf.getHeight() return
	// the actual page width and height, not the imageable area.
	// double pw = pf.getWidth() - 2.0 * ix;
	// double ph = pf.getHeight() - 2.0 * iy;
	dwidth = pf.getImageableWidth();
	dheight = pf.getImageableHeight();
	// the page width and height is supposed to reflect the
	// image orientation.
	width = (int)(Math.ceil(dwidth));
	height = (int)(Math.ceil(dheight));
	sg2d = new SurrogateGraphics2D(width,height, false);
	recorder = new Graphics2DRecorder(sg2d);
    }

    @Override
    public boolean requestsAlpha() {
	return false;
    }

    @Override
    public int getWidth() {
	return width;
    }

    @Override
    public int getHeight() {
	return height;
    }

    /**
     * Get the image width as a double-precision value.
     * For constructors that explicitly provide an image width,
     * this value is the same as that returned by {@link #getWidth()}.
     * For ones that do not, the value returned is the one provided by
     * the print job's page format (i.e., the value returned may not
     * be an integer).
     * @return the image width
     */
    public double getWidthAsDouble() {
	return dwidth;
    }

    /**
     * Get the image height as a double-precision value.
     * For constructors that explicitly provide an image height,
     * this value is the same as that returned by {@link #getHeight()}.
     * For ones that do not, the value returned is the one provided by
     * the print job's page format (i.e., the value returned may not
     * be an integer).
     * @return the image height
     */
    public double getHeightAsDouble() {
	return dheight;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public ColorModel getColorModel() {
	GraphicsConfiguration gconfig = sg2d.getDeviceConfiguration();
	return (gconfig == null)? null: gconfig.getColorModel();
    }

    @Override
    public Graphics2D createGraphics() {
	return recorder.createGraphics();
    }

    @Override
    public boolean canReset() {return false;}

    @Override
    public void reset() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }

    boolean done = false;
    @Override
    public void imageComplete() throws IOException {
	if  (done) throw new IOException("printingCompete");
	done = true;
	final double pgw = (double)getWidth();
	final double pgh = (double)getHeight();
	pjob.setPrintable(new Printable() {
		public int print(Graphics graphics,
				 PageFormat pf,
				 int page)
		    throws PrinterException
		{
		    double ix = pf.getImageableX();
		    double iy = pf.getImageableY();
		    // A test showed that pf.getWidth() and
		    // pf.getHeight() return the actual page width and
		    // height, not the imageable area.
		    // double pw = pf.getWidth() - 2.0 * ix;
		    // double ph = pf.getHeight() - 2.0 * iy;
		    // These methods should get what we want directly.
		    double pw = pf.getImageableWidth();
		    double ph = pf.getImageableHeight();
		    if (page > 0) {
			return Printable.NO_SUCH_PAGE;
		    }
		    Graphics2D g2d = (Graphics2D) graphics;
		    g2d.translate(ix, iy);
		    g2d = (Graphics2D) (graphics.create());
		    double scale, scalex, scaley;
		    double cx, cy, tx, ty;
		    if (useScaling) {
			scalex = pw / pgw;
			scaley = ph / pgh;
			scale = (scalex < scaley)? scalex: scaley;
			// don't scale if the image already fits.
			if (scale > 1.0) scale = 1.0;
		    } else {
			scale = 1.0;
		    }
		    cx = (scale * pgw);
		    cy = (scale *pgh);
		    tx = (pw - cx) / 2.0;
		    ty = (ph - cy) / 2.0;
		    if (scale > 0.0) {
			if (tx != 0.0 || ty != 0.0) {
			    g2d.translate(tx, ty);
			}
			if (scale != 1.0) {
			    g2d.scale(scale,scale);
			}
		    }
		    try {
			recorder.playback(g2d);
		    } finally {
			g2d.dispose();
		    }
		    return Printable.PAGE_EXISTS;
		}
	    });
	try {
	    pjob.print(aset);
	} catch (PrinterException pe) {
	    String msg = errorMsg("printingFailed", pe.getMessage());
	    throw new IOException(msg, pe);
	}
    }
}

//  LocalWords:  exbundle OSGraphics blockquote PrinterGraphics pjob
//  LocalWords:  PrinterJob getPrinterJob printDialog aset preselect
//  LocalWords:  PrintRequestAttributeSet PrinterException getWidth
//  LocalWords:  IllegalArgumentException noPrintersAvailable pre pw
//  LocalWords:  basWidthHeight printingCompete getHeight imageable
//  LocalWords:  printingFailed OrientationRequested nullAttributeSet
//  LocalWords:  NullPointerException badWidthHeight ph iy
