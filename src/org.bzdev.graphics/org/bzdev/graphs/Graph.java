package org.bzdev.graphs;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.awt.font.*;
import java.util.*;
import java.security.*;
import javax.imageio.ImageIO;

import org.bzdev.gio.OSGraphicsOps;
import org.bzdev.gio.ISWriterOps;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.gio.SurrogateGraphics2D;
import org.bzdev.io.FileAccessor;
import org.bzdev.util.SciFormatter;
import org.bzdev.graphs.spi.SymbolProvider;
import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.graphs.lpack.Graphs

/**
 * Class for representing and creating graphs.
 * Java's graphics use two coordinate systems:
 * <ul>
 *   <li> an integer-valued coordinate system in which each
 *        point corresponds to a pixel for the device rendering
 *        graphics.  This coordinate system is referred to as
 *        "device space" in Java documentation.
 *   <li> a real-valued coordinate system in units of points, where
 *        there are 72 points per inch. This coordinate system is
 *        referred to as "user space" in Java documentation.
 * </ul>
 * For some common cases (e.g., a BufferedImage)
 * a distance of length 1.0 in user space corresponds to a distance of
 * 1 in device space, but for hardware-based devices (e.g., printers), this
 * may not be the case.  Typical computer monitors have 72 pixels per inch,
 * so for this case device space and user space are identical.  Both spaces
 * use a convention where increasing x coordinates point right and increasing
 * y coordinates point down.
 * <p>
 * The Graph class introduces a third space called graph coordinate
 * space in which the coordinates are in the units being plotted, and
 * in which increasing values are oriented as specified by the user
 * (generally 'left' for the x direction 'up' for the 'y' direction).
 * (the normal convention when drawing graphs) rather than down. The
 * use of graph coordinate space simplifies drawing graphs as scaling
 * and translations are handled automatically.
 * <p>
 * A graph is assumed to be drawn on a rectangle with a height and
 * width specified in user space.
 * <blockquote><pre><code>
 *                            width       
 *        _|____________________________________________|_
 *         |                                            |
 *         |                                            | upper
 *         |        __________________________          |_y-offset
 *         |        |                         |         |
 *  height |        |                         |         |
 *         |        |                         |         |
 *         |        |                         |         |
 *         |        |                         |         |
 *         |        |                         |         |
 *         |        |_________________________|         |_
 *         |                                            | lower
 *        _|____________________________________________|_y-offset
 *         |  lower |                          | upper  |
 *          x-offset                            x-offset
 *     
 * </CODE></PRE></blockquote>
 * A portion of this rectangle&mdash;the inner rectangle in the
 * figure above&mdash;is area in which objects in the graph are
 * usually drawn. The axes, however, will typically be outside
 * this inner rectangle.  Various offsets determine how much space
 * is left for such axes, labels, and other decorations surrounding
 * the graph.  The method
 * {@link Graph#setOffsets(int,int,int,int) setOffsets} sets
 * these offsets.  The height, width,and four offsets are specified
 * in user space.  The method
 * {@link Graph#setRanges(double,double,double,double) setRanges} provides
 * the upper and lower x and y coordinates in graph coordinate space
 * of the inner rectangle.  When a graph is created, the constructor
 * will call set all the offsets to 0 and then call
 * <code>setRanges(0.0, getWidth(), 0.0, getHeight())</code>,
 * thus causing user space and graph coordinate space to have the same
 * scale, with the point (0,0) at the lower left corner of the figure
 * and with x and y increasing to reach other points within the graph.
 * <P>
 * The method {@link Graph#createGraphics() createGraphics} returns a graphics
 * context for user space and
 * {@link Graph#createGraphicsGCS() createGraphicsGCS} returns a graphics
 * context for graph coordinate space. While one can use the graphics context
 * returned by {@link Graph#createGraphicsGCS() createGraphicsGCS} directly
 * for drawing, strokes would also be scaled, making it difficult to
 * draw lines and other shapes with a desired outline width in user
 * space, the usual case when drawing curves on a graph. To handle the
 * usual case conveniently, the Graph class provides several drawing
 * methods that allow fonts and strokes to be specified in user space,
 * with shapes and some other objects that can be drawn specified in
 * graph-coordinate space. The other objects are instances of Drawable
 * and Graphic described below The drawing methods are
 * <ul>
 *  <li> <A ID="drawImg"></A>drawImage.  There are several variants,
 *       some of which allow the image to be rotated, scaled, or
 *       flipped.  Arguments common to all are a graphics context, an
 *       image, and the x and y coordinate in graph-coordinate
 *       space. Optional arguments (those that are present appear in the
 *       order shown below) include the following:
 *       <ul>
 *          <li> refpoint or refpointName - A Point2D in the image's
 *               coordinate system that represents a point on the
 *               image that will be placed at the location denoted by
 *               the common x and y arguments.  The image's coordinate
 *               system uses real-valued values in which 1.0
 *               corresponds to the size of a pixel, with increase x
 *               values moving right and increasing y values moving
 *               down from the upper-left corner of the image (i.e,
 *               the coordinates are standard Java user-space
 *               coordinates).  A reference point name is a symbolic
 *               name representing common cases (corners, the image's
 *               center, and the centers of image's edges).  The image's
 *               coordinate system is independent of the scaleX and scaleY
 *               parameters defined below.
 *          <li> angle - the angle by which the image should be rotated,
 *               measured counterclockwise from a right-pointing horizontal
 *               axis. The default is 0.0. For graph coordinate space,
 *               "right pointing" refers to the direction of increasing X
 *               and counterclockwise refers to a rotation towards the
 *               direction for increasing Y.
 *          <li> scaleX - the scaling factor in the X direction. The default
 *               is 1.0. Larger scaling factors make the image larger.
 *          <li> scaleY - the scaling factor in the Y direction. The default
 *               is 1.0. Larger scaling factors make the image larger.
 *          <li> flipX - true if the image should be reflected about the
 *               x axis. The default is <code>false</code>. A reflected
 *               image will overlay the original image, with the
 *               reflection applied before any other transformation. If
 *               an image is reflected, the reference point refers to the
 *               reflected image.
 *          <li> flipY - true if the image should be reflected about the
 *               y axis. The default is <code>false</code>.  A reflected
 *               image will overlay the original image, with the
 *               reflection applied before any other transformation. If
 *               an image is reflected, the reference point refers to the
 *               reflected image.
 *          <li> imageInGCS - true if, before any scaling, the width and
 *               height of a pixel corresponds to 1 unit length in graph
 *               coordinate space; false if the width and
  *               height of a pixel corresponds to 1 unit length in user
 *               space. The default is <code>true</code>.
 *       </ul>
 *       The default for <code>imageInGCS</code> of <code>true</code>
 *       is the most convenient value for the anim2d package, where
 *       images would tend to represent objects in an animation and
 *       should adjust size as a view of an animation is zoomed. By
 *       contrast, a value of <code>false</code> is appropriate when
 *       the image represents some sort of fixed-sized icon or label,
 *       but those are often positioned at fixed locations on the
 *       fame, in which case a Graphics2D for the frame would be used
 *       directly.
 *  <li> drawString. There are two variants. One uses font parameters
 *       associated with the graph and the other uses a set of font
 *       parameters configured independently. The class FontParms specifies
 *       the font color, the font to use, the position relative to the
 *       string of a reference point whose position on the graph is specified
 *       when the string is drawn, and the angle at which the string should
 *       be printed.
 *  <li> draw.  This method takes a graphics context as its first argument
 *       and either a Graph.Drawable or a Shape as its second argument. The
 *       outline of a shape will be drawn. For a Drawable, its toShape method
 *       will be called and the resulting shape will be drawn.
 *  <li> fill. This method takes a graphics context as its first argument
 *       and either a Graph.Drawable or a Shape as its second argument. The
 *       shape will be filled. For a Drawable, its toShape method
 *       will be called and the resulting shape will be filled.
 *  <li> add. This method takes as its argument an instance of the class
 *       Graph.Graphic and adds that object to the graph by calling the
 *       object's addTo method.
 * </ul>
 * <p>
 * The interface Graph.Drawable indicates that an object has an associated
 * shape that can be drawn or filled by a graph.  The interface
 * Graph.Graphic indicates that an object has a graphical representation
 * that can appear on a graph.  This representation is typically more
 * complex than ones associated with a Shape or a Drawable. The interfaces
 * Graph.UserDrawable and Graph.UserGraphic are similar, but represent
 * objects that should be drawn in user space and then placed at specified
 * locations in graph coordinate space. Graph.UserDrawable and
 * Graph.UserGraphic are useful for drawing symbols that should have a
 * fixed size on an image. Symbols to represent data points are a good
 * example of cases where these classes are useful.
 * <p>
 * Graph axis are described by the class Graph.Axis, with tick marks
 * along the axis specified by the class Graph.TickSpec.  Finally, for
 * some uses of the graph class (e.g., its use in animations provided by
 * the org.bzdev.anim2d package), it is useful to be able to draw lines
 * with a width specified in graph coordinate space.  In these cases,
 * the method {@link Graph#createGraphicsGCS() createGraphicsGCS} can
 * be used to obtain a graphics context where stroke widths are defined
 * in graph coordinate space units.
 *
 * Typically one will first create a graph. One can specify the image
 * dimensions explicitly or initialize the graph with an instance of
 * {@link org.bzdev.gio.OutputStreamGraphics}.  For example,
 * <blockquote><pre><code>
 *   Graph graph = new Graph(800, 600);
 * </CODE></PRE></blockquote>
 * or
 * <blockquote><pre><code>
 *   OutputStream os = new FileOutputStream("output.png");
 *   OutputStreamGraphics =
 *      OutputStreamGraphics.newInstance(800, 600, "png");
 *   Graph graph = new Graph(osg);
 * </CODE></PRE></blockquote>
 * The documentation for {@link org.bzdev.gio.PrinterGraphics} shows
 * how to set up a graph so that the image will go directly to a printer.
 * <P>
 * The next step is to set the offsets and ranges.
 * The method {@link Graph#setOffsets(int,int)} or
 * {@link Graph#setOffsets(int,int,int,int)} will reserve space around
 * the edges of a graph for labels and axes (specifically, the tick
 * marks). Values of 50 are reasonable, with a value of 75 or 100 useful
 * if there is a label for an axis.  If the offsets are too small, a label
 * or tick mark might not be visible. One then sets the ranges for the
 * graph (the range in the X direction and the range in the Y direction)
 * by calling {@link Graph#setRanges(double,double,double,double)}.
 * For example, if the X values on a graph vary from 0.0 to 1000.0 and
 * the Y values vary from 0.0 to 100.0, the following statements could
 * be used to configure the graph:
 * <blockquote><pre><code>
 *     graph.setOffsets(75, 50, 75, 50);
 *     graph.setRanges(0.0, 1000.0, 0.0, 100.0);
 * </CODE></PRE></blockquote>
 * An alternate method
 * {@link Graph#setRanges(double,double,double,double,double,double)}
 * is useful when the Graph class is used to create a 'canvas' on
 * which to draw objects.
 * <P>
 * Once the graph is configured, one can draw objects on it.  The
 * first step it to create a graphics context. Aside from setting the
 * drawing color, font size, strokes, etc., this graphics context will
 * not be used directly in most cases as the graphics context has
 * user-space coordinates.  Instead, the graphics context will be
 * passed to one of the graph's "draw" methods. For example,
 * <blockquote><pre><code>
 *     Graphics2D g2d = graph.createGraphics();
 *     double x1 = 10.0;
 *     double y1 = 1.0;
 *     double x2 = 900.0;
 *     double y1 = 90.0;
 *     Line2D line = new Line2D.Double(x1, y1, x2, y2);
 *     g2d.setColor(Color.BLUE);
 *     g2d.setStroke(new BasicStroke(1.5F));
 *     graph.draw(g2d, line);
 * </CODE></PRE></blockquote>
 * will draw a blue line whose end points in graph coordinate space
 * are (10.0, 1.0) and (900.0, 90.0) and whose width is 1.5 pts
 * (in user-space units).
 * <P>
 * To draw a symbol on the graph, one can create an instance of the
 * class {@link org.bzdev.graphs.Graph.SymbolFactory}, configure it,
 * and then create a symbol. A 'draw' method will then draw the symbol.
 * For example,
 * <blockquote><pre><code>
 *     Graph.SymbolFactory sf = new Graph.SymbolFactory();
 *     sf.setColor(Color.BLACK);
 *     Graph.Symbol circ = sf.newSymbol("SolidCircle");
 *     graph.draw(circ, 200.0, 30.0);
 *     graph.drawEY(circ, 400.0, 40.0, 25.0);
 * </CODE></PRE></blockquote>
 * will draw a symbol (a solid, black circle) at the point (200.0, 30.0)
 * in graph coordinate space. It will also draw the same symbol at
 * (400.0, 40.0) in graph coordinate space, but with error bars in the
 * Y direction.
 * <P>
 * The easiest way to add axes to a graph is to use one of the subclasses
 * of {@link org.bzdev.graphs.AxisBuilder} to create an axis. One can then
 * call a "draw" method to add the axis to the graph. For example
 * <blockquote><pre><code>
 *     AxisBuilder.Linear ab =
 *        new AxisBuilder.Linear(graph, 0.0, 0.0, 1000.0, true, "X Axis");
 *     ab.setMaximumExponent(3);
 *     ab.addTickSpec(0, 0, true, "%4.0f");
 *     ab.addTickSpec(2, 1, false, null);
 *     graph.draw(ab.createAxis());
 * </CODE></PRE></blockquote>
 * will draw an X axis. How the axis is configured is described in the
 * documentation for each of subclasses of {@link org.bzdev.graphs.AxisBuilder}.
 * <P>
 * To draw or output an image, call a 'write' method:
 * <blockquote><pre><code>
 *     graph.write();
 * </CODE></PRE></blockquote>
 * when the graph's constructor used an instance of
 * {@link org.bzdev.gio.OutputStreamGraphics} or other classes that implement
 * the {@link org.bzdev.gio.OSGraphicsOps} interface. Otherwise use
 * a 'write' method with multiple arguments:
 <blockquote><pre><code>
 *     graph.write("png", "output.png");
 * </CODE></PRE></blockquote>
 * @see org.bzdev.graphs.Graph.Axis
 * @see org.bzdev.graphs.Graph.TickSpec
 * @see org.bzdev.gio.OutputStreamGraphics
 * @see org.bzdev.gio.PrinterGraphics
 */
 public class Graph {

    private static ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.graphs.lpack.Graphs");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

     /**
      * The default width for a graph.
      */
     public static final int DEFAULT_WIDTH = 700;
     /**
      * The default height for a graph.
      */
     public static final int DEFAULT_HEIGHT = 700;

     BufferedImage image = null;
     OSGraphicsOps osg = null;
     ISWriterOps isw = null;
     int iwidth;
     int iheight;
     double dwidth;
     double dheight;

     /**
      * Get the image used to display a graph.
      * @return the image for this graph; null if there is none
      */
     public BufferedImage getImage() {
	 return image;
     }

     /**
      * Get the output-stream graphics or {@link OSGraphicsOps} used to
      * display a graph.
      * @return the output-stream graphics instance; null if there is none
      */
     public OSGraphicsOps getOutputStreamGraphics() {
	 return osg;
     }

     /**
      * Get the color model associated with the graph's image.
      * @return the color model
      */
     public ColorModel getColorModel() {
	 if (image != null) return image.getColorModel();
	 else if (osg != null) return osg.getColorModel();
	 else return null;
     }


     /**
      * Get the height of a graph as an integer.
      * @return the height
      */

     public int getHeightAsInt() {return iheight;}

     /**
      * Get the width of a graph as an integer.
      * @return the width
      */
     public int getWidthAsInt() {return iwidth;}

     /**
      * Get the height of a graph.
      * @return the height in user-space units
      */
     public double getHeight(){return dheight;}

     /**
      * Get the width of a graph.
      * @return the width in user-space units
      */
     public double getWidth(){return dwidth;}


    /**
     * Enumeration specifying the image type for a graph.
     * This enum provides a type-save value for the integer
     * constants defined by {@link java.awt.image.BufferedImage}.
     */
    public static enum ImageType {
	/**
	 * Specifies an image type of BufferedImage.TYPE_INT_RGB.
	 */
	INT_RGB(BufferedImage.TYPE_INT_RGB),
	/**
	 * Specifies an image type of BufferedImage.TYPE_INT_ARGB.
	 */
	INT_ARGB(BufferedImage.TYPE_INT_ARGB),
	/**
	 * Specifies an image type of BufferedImage.TYPE_INT_ARGB_PRE.
	 */
	INT_ARGB_PRE(BufferedImage.TYPE_INT_ARGB_PRE),
	/**
	 * Specifies an image type of BufferedImage.TYPE_INT_BGR.
	 */
	INT_BGR(BufferedImage.TYPE_INT_BGR),
	/**
	 * Specifies an image type of BufferedImage.TYPE_3BYTE_BGR.
	 */
	THREE_BYTE_BGR(BufferedImage.TYPE_3BYTE_BGR),
	/**
	 * Specifies an image type of BufferedImage.TYPE_4BYTE_ABGR.
	 */
	FOUR_BYTE_ABGR(BufferedImage.TYPE_4BYTE_ABGR),
	/**
	 * Specifies an image type of BufferedImage.TYPE_4BYTE_ABGR_PRE.
	 */
	FOUR_BYTE_ABGR_PRE(BufferedImage.TYPE_4BYTE_ABGR_PRE),
	/**
	 * Specifies an image type of BufferedImage.TYPE_USHORT_565_RGB.
	 */
	USHORT_565_RGB(BufferedImage.TYPE_USHORT_565_RGB),
	/**
	 * Specifies an image type of BufferedImage.TYPE_USHORT_555_RGB.
	 */
	USHORT_555_RGB(BufferedImage.TYPE_USHORT_555_RGB),
	/**
	 * Specifies an image type of BufferedImage.TYPE_BYTE_GRAY.
	 */
	BYTE_GRAY(BufferedImage.TYPE_BYTE_GRAY),
	/**
	 * Specifies an image type of BufferedImage.TYPE_USHORT_GRAY.
	 */
	USHORT_GRAY(BufferedImage.TYPE_USHORT_GRAY),
	/**
	 * Specifies an image type of BufferedImage.TYPE_BYTE_BINARY.
	 */
	BYTE_BINARY(BufferedImage.TYPE_BYTE_BINARY),
	/**
	 * Specifies an image type of BufferedImage.TYPE_BYTE_INDEXED.
	 */
	BYTE_INDEXED(BufferedImage.TYPE_BYTE_INDEXED);

	int bitype;

	/**
	 * Return the integer type code BufferedImage uses.
	 * @return the type of a buffered image
	 */
	public int getType() {return bitype;}

	private ImageType(int type) {
	    bitype = type;
	}

	/**
	 * Find an ImageType given the corresponding BufferedImage
	 * constant.
	 * @param bitype an image type defined by the class
	 *        {@link java.awt.image.BufferedImage}
	 * @return the corresponding ImageType enumeration constant; null
	 *         if there is none
	 */
	public static ImageType getImageType(int bitype) {
	    switch (bitype) {
	    case BufferedImage.TYPE_INT_RGB:
		return ImageType.INT_RGB;
	    case BufferedImage.TYPE_INT_ARGB:
		return ImageType.INT_ARGB;
	    case BufferedImage.TYPE_INT_ARGB_PRE:
		return ImageType.INT_ARGB_PRE;
	    case BufferedImage.TYPE_INT_BGR:
		return ImageType.INT_BGR;
	    case BufferedImage.TYPE_3BYTE_BGR:
		return ImageType.THREE_BYTE_BGR;
	    case BufferedImage.TYPE_4BYTE_ABGR:
		return ImageType.FOUR_BYTE_ABGR;
	    case BufferedImage.TYPE_4BYTE_ABGR_PRE:
		return ImageType.FOUR_BYTE_ABGR_PRE;
	    case BufferedImage.TYPE_USHORT_565_RGB:
		return ImageType.USHORT_565_RGB;
	    case BufferedImage.TYPE_USHORT_555_RGB:
		return ImageType.USHORT_555_RGB;
	    case BufferedImage.TYPE_BYTE_GRAY:
		return ImageType.BYTE_GRAY;
	    case BufferedImage.TYPE_USHORT_GRAY:
		return ImageType.USHORT_GRAY;
	    case BufferedImage.TYPE_BYTE_BINARY:
		return ImageType.BYTE_BINARY;
	    case BufferedImage.TYPE_BYTE_INDEXED:
		return ImageType.BYTE_INDEXED;
	    default:
		return null;
	    }
	}

    }

     ImageType imageType =  null;

     /**
      * Get the image type.
      * @return the image type; null there is no image.
      */
     public ImageType getImageType() {
	 return (image == null)? null: imageType;
     }

     /**
      * Constructor.
      * The image will be cleared.
      */
     public Graph() {
	 this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     }

     boolean shared = false;

     /**
      * Constructor based on another instance of Graph.
      * When shared, the two graphs share the same image or other
      * object on which a graph is to be drawn.  The original graph's
      * size, image type, offsets, ranges and background color are
      * copied. When not shared, a new image will be created with the
      * same image type and dimensions 1as the original image.  The
      * image will be cleared (so it will be filled with the
      * background color) if it is not shared.
      * <P>
      * A shared graph is particularly useful for graphs in which
      * the left and right vertical axes have different scales
      * (for example a graph showing two functions of temperature,
      * where one function's range is a pressure and the other
      * function's range is a volume).

      * @param graph a graph on which to base this instance
      * @param shared True if the graph is shared; false otherwise
      * @exception IllegalArgumentException the graph argument uses
      *            output stream graphics or an image sequence writer
      *            but the graph being constructed is  not shared
      * @see #clear()
      */
     public Graph(Graph graph, boolean shared)
	 throws IllegalArgumentException
     {
	 this.shared = shared;
	 if (shared) {
	     image = graph.image;
	     imageType = graph.getImageType();
	     isw  = graph.isw;
	     osg = graph.osg;
	 } else if (graph.image != null) {
	     int type = graph.image.getType();
	     ColorModel cm = graph.image.getColorModel();
	     imageType = graph.getImageType();
	     if ((type == BufferedImage.TYPE_BYTE_BINARY
		  || type == BufferedImage.TYPE_BYTE_INDEXED) &&
		 cm instanceof IndexColorModel) {
		 image = new BufferedImage(graph.getWidthAsInt(),
					   graph.getHeightAsInt(),
					   type, (IndexColorModel)cm);
	     } else {
		 image = new BufferedImage(graph.getWidthAsInt(),
					   graph.getHeightAsInt(),
					   type);
	     }
	 } else if (graph.osg != null) {
	     String cn = graph.osg.getClass().getName();
	     throw new IllegalArgumentException(errorMsg("osgNoCopy", cn));
	 } else if (graph.isw != null) {
	     String cn = graph.isw.getClass().getName();
	     throw new IllegalArgumentException(errorMsg("iswNoCopy", cn));
	 }
	 iwidth = graph.iwidth;
	 iheight = graph.iheight;
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 setOffsets(graph.xLowerOffset, graph.xUpperOffset,
		    graph.yLowerOffset, graph.yUpperOffset);
	 setRanges(graph.xLower, graph.xUpper, graph.yLower, graph.yUpper);
	 backgroundColor = graph.backgroundColor;
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 if (!shared) clear();
     }

     /**
      * Constructor specifying an image width and height.
      * The image will be cleared.
      * @param width the image width
      * @param height the image height
      * @see #clear()
      */
     public Graph(int width, int height) {
	 image = new BufferedImage
	     (width, height, BufferedImage.TYPE_INT_ARGB_PRE);
	 iwidth = width;
	 iheight = height;
	 imageType = ImageType.INT_ARGB_PRE;
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 setRanges(0.0, (double)width, 0.0, (double)height);
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 clear();
     }

     /**
      * Constructor specifying an image width, height, and type.
      * The image will be cleared.
      * @param width the image width
      * @param height the image height
      * @param type the image type (an enum constant corresponding to
      *        integer constants defined by
      *        {@link java.awt.image.BufferedImage BufferedImage} that
      *        must not be null)
      * @see #clear()
      */
     public Graph(int width, int height, ImageType type) {
	 imageType = type;
	 image = new BufferedImage(width, height, type.getType());
	 iwidth = width;
	 iheight = height;
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 setRanges(0.0, (double)width, 0.0, (double)height);
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 clear();
     }

     /**
      * Constructor specifying an image.
      * The image will not be cleared. The image may not be null.
      * @param image the image used to display the graph
      */
     public Graph(BufferedImage image) {
	 imageType = ImageType.getImageType(image.getType());
	 this.image = image;
	 iwidth = image.getWidth();
	 iheight = image.getHeight();
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 setRanges(0.0, dwidth, 0.0, dheight);
     }

     /**
      * Constructor specifying an output-stream-graphics object.
      * The graphics will not be cleared.
      * @param osg the instance of OSGraphicsOps to be used to create the graph
      */
     public Graph(OSGraphicsOps osg) {
	 this.osg = osg;
	 iwidth = osg.getWidth();
	 iheight = osg.getHeight();
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 setRanges(0.0, dwidth, 0.0, dheight);
     }

     /**
      * Constructor specifying an image-sequence-writer object.
      * The graphics will not be cleared.
      * @param width the width of the drawing area
      * @param height the height of the drawing area
      * @param requestAlpha true if the drawing area should be
      *        configured with an alpha channel; false otherwise
      * @param isw the instance of ISWriterOps used to create the graph
      */
     public Graph(int width, int height, boolean requestAlpha,
		  ISWriterOps isw)
     {
	 this.isw = isw;
	 osg = new OutputStreamGraphics.Surrogate(width, height, requestAlpha);
	 imageType = SurrogateGraphics2D.getGraphImageType(requestAlpha);
	 int  iswWidth = isw.getFrameWidth();
	 int  iswHeight = isw.getFrameHeight();
	 if (iswWidth != 0 || iswHeight != 0) {
	     if (iswWidth != width || iswHeight != height) {
		 throw new IllegalArgumentException(errorMsg("ISWSize"));
	     }
	 }
	 iwidth = width;
	 iheight = height;
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 setRanges(0.0, dwidth, 0.0, dheight);
     }

     /**
      * Set this graph's OutputStreamGraphics field.
      * This method is used only when a graph is configured to use
      * an image sequence writer. The argument should have been
      * created with one of that image sequence writer's
      * nextOutputStreamGraphics methods. If not, some methods
      * such as {@link #getImageType()} may not work as expected
      * (the image type returned by {@link #getImageType()} is not
      * changed by this method).
      * <P>
      * Offsets, ranges, and graph rotations are preserved, as are
      * fonts, font color, the background color, etc., but previously
      * created graphics contexts are no longer valid, and previous
      * drawing operations are not copied to the new output stream
      * graphics.
      * <P>
      * This method is used by {@link org.bzdev.anim2d.Animation2D}.
      * It should rarely be used directly as it is intended for use
      * by animation classes or other classes that manage sequences of
      * images.
      * @param osg the output stream graphics to use
      * @exception IllegalStateException this graph was not constructed with
      *           an output stream graphics object.
      * @exception IllegalArgumentException the width and height for the
      *           argument does not match the ones required for this graph
      * @see #Graph(int,int,boolean,ISWriterOps)
      */
     public void setOSGraphics(OSGraphicsOps osg) {
	 if (isw == null) {
	     throw new IllegalStateException(errorMsg("setOSGraphics"));
	 }
	 if ((osg.getWidth() != iwidth) || (osg.getHeight() != iheight)) {
	     throw new IllegalStateException(errorMsg("OSGSize"));
	 }
	 this.osg = osg;
     }


     /**
      * Constructor specifying an image width, height, type, and color model.
      * A color model should be provided only when the image type is
      * BufferedImage.TYPE_BYTE_BINARY or BufferedImage.TYPE_BYTE_INDEXED.
      * The image will be cleared.
      * @param width the image width
      * @param height the image height
      * @param type the image type (constants from
      *  {@link java.awt.image.BufferedImage BufferedImage}
      * @param cm the color model
      */
     public Graph(int width, int height, int type, IndexColorModel cm)
     {
	 image = new BufferedImage(width, height, type, cm);
	 iwidth = width;
	 iheight = height;
	 dwidth = (double)iwidth;
	 dheight = (double)iheight;
	 graphBoundingBoxUS = new
	     Rectangle2D.Double(0.0, 0.0, dwidth, dheight);
	 setRanges(0.0, (double)width, 0.0, (double)height);
	 clear();
     }

     private  static Color defaultBackgroundColor =
	 new Color(0, 0, 0, 0);

     /**
      * Set the default color to use when clearing a graph.
      * Unless set by the user, the default is black with an
      * alpha value of 0 making 'black' transparent. This is
      * the color that will be used initially when a graph is
      * created. Existing graphs will not have their background
      * color modified.
      * @param color the color
      */
    public static void setDefaultBackgroundColor(Color color) {
	 defaultBackgroundColor = color;
     }

     /**
      * Get the default color to use when clearing a graph.
      * @return the default color
      */
     public static Color getDefaultBackgroundColor() {
	 return defaultBackgroundColor;
     }

     private Color backgroundColor = defaultBackgroundColor;

     /**
      * Set the color to use when clearing a graph.
      * @param color the background color
      */
     public void setBackgroundColor(Color color) {
	 backgroundColor = color;
     }


     /**
      * Get the color to use when clearing a graph.
      * @return the color to use when clearing a graph
      */
     public Color getBackgroundColor() {return backgroundColor;}

     private boolean clearByFillMode = false;

     /**
      * Set whether or clearing a graph should add the background
      * color to each pixel in a graph or replace the pixels.
      * For an alpha of 255, the appearance does not depend on the
      * mode.  For other values of alpha, when the mode is true, the
      * background color, which is transparent or translucent, will
      * be added to each pixel; when the mode is false, each pixel will
      * be replaced with one having the background color.
      * @param mode true for adding; false for replacing
      */
     public void setClearByFillMode(boolean mode) {
	 clearByFillMode = mode;
     }

     /**
      * Get the mode that determines if clearing a graph should add
      * the background color to each pixel in a graph or replace the
      * pixels.
      * @return true for adding; false for replacing
      */
     public boolean getClearByFillMode() {return clearByFillMode;}

     /**
      * Clear an image.
      * The effect depends on the background color and whether
      * clear-by-fill mode is active.  In clear-by-fill mode, the
      * graph's image is filled with the background color. If the
      * background color has a value of alpha less than 255, the
      * previous image will be covered by a transparent or partially
      * transparent pixels.  If not in clear-by-fill mode, the
      * existing image will be replaced with the background color
      * (this is the default mode when a graph is created).
      * Clear-by-fill mode is useful for some types of animation,
      * where an indication of the previous positions of objects in
      * the animation is useful, for instance by creating a 'blurred
      * motion' effect.
      * @see #setClearByFillMode(boolean)
      * @see #setBackgroundColor(Color)
      * @see #getClearByFillMode()
      * @see #getBackgroundColor()
      */
     public void clear() {
	 Graphics2D g2d = (image != null)? image.createGraphics():
	     osg.createGraphics();
	 try {
	     if (clearByFillMode) {
		 g2d.setColor(backgroundColor);
		 g2d.fillRect(0,0, iwidth, iheight);
	     } else {
		 g2d.setBackground(backgroundColor);
		 g2d.clearRect(0,0, iwidth, iheight);
	     }
	 } finally {
	     g2d.dispose();
	 }
     }

     /**
      * Clear an image using a specified background
      * @param paint the background
      */
     public void clear(Paint paint) {
	 Graphics2D g2d = (image != null)? image.createGraphics():
	     osg.createGraphics();
	 // g2d.setColor(new Color(255,255,255,0));
	 try {
	     g2d.setPaint(paint);
	     g2d.fillRect(0,0, iwidth, iheight);
	 } finally {
	     g2d.dispose();
	 }
     }
    
     /**
      * Enumeration specifying text justification for strings.
      */
     public static enum Just {
	 /**
	  *  Left justify strings.
	  */
	 LEFT,
	     /**
	      * Center strings.
	      */
	     CENTER,
	     /**
	      * Right justify strings.
	      */
	     RIGHT};
     /**
      * Vertical text positioning for strings.
      */
     public static enum BLineP {
	 /**
	  * Top alignment.
	  */
	 TOP,
	     /**
	      * Center alignment.
	      */
	     CENTER,
	     /**
	      * Baseline alignment.
	      */
	     BASE,
	     /**
	      * Bottom alignment.
	      */
	     BOTTOM
	     }

     static class Observer implements ImageObserver {
	 boolean couldNotLoad = false;
	 boolean imageDone = false;
	 boolean needHeight, needWidth, needImage;
	 public Observer(boolean needWidth, boolean needHeight,
			 boolean needImage)
	 {
	     this.needWidth = needWidth;
	     this.needHeight = needHeight;
	     this.needImage = needImage;
	 }
	 public synchronized boolean imageUpdate(Image img, int infoflags,
						 int x, int y,
						 int width, int height)
	 {
	     // System.out.println("update image called: infoflags = " + infoflags);
	     int mask = 0;
	     if (needWidth) mask |= ImageObserver.WIDTH;
	     if (needHeight) mask |= ImageObserver.HEIGHT;
	     if (needImage) mask |= ImageObserver.ALLBITS;

	     int emask = ImageObserver.ABORT | ImageObserver.ERROR;
	     couldNotLoad = ((infoflags & emask) != 0);
	     imageDone =  ((infoflags & mask) == mask);
	     boolean result = (!imageDone) || couldNotLoad;
	     if (!result) notify();
	     return result;
	 }

	 public boolean done() throws IOException {
	     if (couldNotLoad) {
		 throw new IOException(errorMsg("cannotLoadImage"));
	     }
	     return imageDone;
	 }
     }

     static double getImageWidth(Image img) throws IOException {
	 Observer observer = new Observer(true, false, false);
	 int ival = img.getWidth(observer);
	 double iw = 0.0;
	 if (ival == -1) {
	     try {
		 synchronized(observer) {
		     while (!observer.done()) {
			 observer.wait();
		     }
		 }
		 iw = (double) img.getWidth(null);
	     } catch (InterruptedException e) {
		 String msg = errorMsg("cannotLoadImage");
		 throw new IOException(msg, e);
	     }
	 } else {
	     iw = (double)ival;
	 }
	 return iw;
     }

     static double getImageHeight(Image img) throws IOException {
	 Observer observer = new Observer(false, true, false);
	 int ival = img.getHeight(observer);
	 double ih;
	 if (ival == -1) {
	     try {
		 synchronized(observer) {
		     while (!observer.done()) {
			 observer.wait();
		     }
		 }
		 ih = (double) img.getHeight(null);
	     } catch (InterruptedException e) {
		 String msg = errorMsg("cannotLoadImage");
		 throw new IOException(msg, e);
	     }
	 } else {
	     ih = (double) ival;
	 }
	 return ih;
     }

     /**
      * Draw an image.
      * The image's reference point is at the image's upper left corner.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the image will take up one unit in graph coordinate
      * space.  The upper edge of the image (from left to right in the image's
      * coordinate system) will lie along a line passing through (x,
      * y) in graph coordinate space and parallel to the positive X
      * axis.  Note that if the positive X axis in graph coordinate
      * space points right to left in user space, the image will
      * appear to have been rotated 180 degrees relative to the
      * positive X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the x coordinate in graph coordinate space for the image's
      *        reference point
      * @param y the coordinate in graph coordinate space for the image's
      *        reference point
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, DEFAULT_REFPOINT,
		   0.0, 1.0, 1.0, false, false, true);

     }

     /**
      * Draw an image with each pixel representing a unit in either
      * user space or graph coordinate space.
      * The image's reference point is at the image's upper left corner.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the image
      * will take up 1 unit in graph coordinate space.  The upper edge
      * of the image (from left to right in the image's coordinate
      * system) will lie along a line passing through (x, y) in graph
      * coordinate space and parallel to the positive X axis.  Note
      * that if the positive X axis in graph coordinate space points
      * right to left in user space, the image will appear to have
      * been rotated 180 degrees relative to the positive X axis in
      * user space.
      * <P>
      * When the argument imageInCGS is false, a pixel in the image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to the X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the x coordinate in graph coordinate space for the image's
      *        reference point
      * @param y the coordinate in graph coordinate space for the image's
      *        reference point
      * @param imageInGCS true if the image pixel size is a unit in
      *        graph coordinate space; false if the image pixel size
      *        is a unit in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, DEFAULT_REFPOINT,
		   0.0, 1.0, 1.0, false, false, imageInGCS);

     }


     /**
      * Draw an image given a named reference point.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the image will take up one unit in graph coordinate
      * space. The upper edge of the image (from left to right in the
      * image's coordinate system) will be parallel to a line passing
      * through (x, y) in graph coordinate space and parallel to the
      * positive X axis.  The image will be placed so that its
      * reference point is at (x, y). Note that if the positive X axis
      * in graph coordinate space points right to left in user space,
      * the image will appear to have been rotated 180 degrees
      * relative to the positive X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, 0.0, 1.0, 1.0,
		   false, false, true);
     }

     /**
      * Draw an image given a named reference point with each pixel
      * representing a unit in either user space or graph coordinate space.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the image
      * will take up one unit in graph coordinate space. The upper
      * edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line passing through
      * (x, y) in graph coordinate space and parallel to the positive
      * X axis.  The image will be placed so that its reference point
      * is at (x, y). Note that if the positive X axis in graph
      * coordinate space points right to left in user space, the image
      * will appear to have been rotated 180 degrees relative to the
      * positive X axis in user space.
      * <P>
      * When the argument imageInCGS is false, a pixel in the image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to the X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param imageInGCS true if the image pixel size is a unit in
      *        graph coordinate space; false if the image pixel size
      *        is a unit in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, 0.0, 1.0, 1.0,
		   false, false, imageInGCS);

     }

     /**
      * Draw an image given a named reference point and an angle.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the image will take up one unit in graph coordinate
      * space. The lower edge of the image (from left to right in the
      * image's coordinate system) will be parallel to a line in user
      * space whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the
      *        image
      * @param angle an angle, in radians and in graph coordinate
      *        space, by which the image will be rotated in the
      *        counter clockwise direction from its normal orientation
      *        about the reference point
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, angle, 1.0, 1.0,
		   false, false, true);

     }

     /**
      * Draw an image given a named reference point and an angle
      * with each pixel representing a unit in either user space
      * or graph coordinate space.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the image
      * will take up 1 unit in graph coordinate space. The lower edge
      * of the image (from left to right in the image's coordinate
      * system) will be parallel to a line in user space whose
      * direction is set by its angle from the positive X direction
      * when rotated towards the positive Y direction, both in graph
      * coordinate space. The image will be placed so that the
      * reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to a line obtained by rotating
      * counter clockwise by the specified angle from the positive X
      * axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the
      *        image
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param imageInGCS true if the image pixel size is a unit in
      *        graph coordinate space; false if the image pixel size
      *        is a unit in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle, boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, angle, 1.0, 1.0,
		   false, false, imageInGCS);

     }

     /**
      * Draw an image given a named reference point, an angle, and scale
      * factors.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the scaled image will take up one unit in graph
      * coordinate space. The lower edge of the image (from left to
      * right in the image's coordinate system) will be parallel to a
      * line in user space whose direction is set by its angle from
      * the positive X direction when rotated towards the positive Y
      * direction, both in graph coordinate space. The image will be
      * placed so that the reference point is at the coordinates (x,
      * y) in graph coordinate space.  The mapping from
      * graph-coordinate-space angles to user-space angles is not
      * linear if the scale factors in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param angle an angle, in radians and in graph coordinate
      *        space, by which the image will be rotated in the
      *        counter clockwise direction from its normal
      *        orientation and about the reference point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle, double scaleX, double scaleY)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, angle, scaleX, scaleY,
		   false, false, true);
     }

     /**
      * Draw an image given a named reference point, an angle, and scale
      * factors, with a pixel's height and width before scaling being
      * a unit length in either user space or graph coordinate space.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the scaled
      * image will take up one unit in graph coordinate space. The
      * lower edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to a line obtained by rotating
      * counter clockwise by the specified angle from the positive X
      * axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle, double scaleX, double scaleY,
			   boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, angle, scaleX, scaleY,
		   false, false, imageInGCS);
     }

     /**
      * Draw an image given a named reference point, an angle,
      * scale factors, and reflection flags.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * When an image is flipped in either the X or Y direction (or both),
      * it is flipped about the center of the image, and the reference point
      * remains at the same location relative to the image's upper left corner.
      * That is, the flipped image in effect overlays the original one and
      * replaces it.  The angle is the angle in user space, but reversed from
      * the usual clockwise direction used in most Java graphics operations
      * (to be consistent with the common case for graph coordinate space
      * where increasing X moves right and increasing Y moves up).
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the scaled image will take up one unit in graph
      * coordinate space. The lower edge of the image (from left to
      * right in the image's coordinate system) will be parallel to a
      * line in user space whose direction is set by its angle from
      * the positive X direction when rotated towards the positive Y
      * direction, both in graph coordinate space. The image will be
      * placed so that the reference point is at the coordinates (x,
      * y) in graph coordinate space.  The mapping from
      * graph-coordinate-space angles to user-space angles is not
      * linear if the scale factors in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param flipX true of the image should be reflected about the Y axis
      *        in the image's coordinate system
      * @param  flipY true of the image should be reflected about the X axis
      *        in the image's coordinate system.
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle, double scaleX, double scaleY,
			   boolean flipX, boolean flipY)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpointName, angle, scaleX, scaleY,
		   flipX, flipY, true);
     }

     // the value returned is in image-pixel-based units, assuming a
     // refpoint of (0.0, 0.0) is the upper-left corner of an image,
     // with the image's x coordinate going from left to right and the
     // image's y coordinate going from top to bottom.
     private static Point2D getRefpoint(Image img, RefPointName name)
	 throws IOException
     {
	 Point2D refpoint;

	 double iw = getImageWidth(img);
	 double ih = getImageHeight(img);
	 switch (name) {
	 default:
	 case UPPER_LEFT:
	     refpoint = DEFAULT_REFPOINT; // (0.0, 0.0)
	     break;
	 case UPPER_CENTER:
	     refpoint = new Point2D.Double(iw/2.0, 0.0);
	     break;
	 case UPPER_RIGHT:
	     refpoint = new Point2D.Double((double)iw, 0.0);
	     break;
	 case CENTER_LEFT:
	     refpoint = new Point2D.Double(0.0, ih/2.0);
	     break;
	 case CENTER:
	     refpoint = new Point2D.Double(iw/2.0, ih/2.0);
	     break;
	 case CENTER_RIGHT:
	     refpoint = new Point2D.Double((double)iw, ih/2.0);
	     break;
	 case LOWER_LEFT:
	     refpoint = new Point2D.Double(0.0, (double)ih);
	     break;
	 case LOWER_CENTER:
	     refpoint = new Point2D.Double(iw/2.0,(double)ih);
	     break;
	 case LOWER_RIGHT:
	     refpoint = new Point2D.Double((double)iw, (double)ih);
	     break;
	 }
	 return refpoint;
     }

     /**
      * Draw an image given a named reference point, an angle, scale
      * factors, reflection flags, with each pixel representing a unit
      * in either user space or graph coordinate space.
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph.
      * When an image is flipped in either the X or Y direction (or both),
      * it is flipped about the center of the image, and the reference point
      * remains at the same location relative to the image's upper left corner.
      * That is, the flipped image in effect overlays the original one and
      * replaces it.  The angle is the angle in user space, but reversed from
      * the usual clockwise direction used in most Java graphics operations
      * (to be consistent with the common case for graph coordinate space
      * where increasing X moves right and increasing Y moves up).
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the scaled
      * image will take up one unit in graph coordinate space. The
      * lower edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to the X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param flipX true of the image should be reflected about the Y axis
      *        in the image's coordinate system
      * @param  flipY true of the image should be reflected about the X axis
      *        in the image's coordinate system.
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img,
			   double x, double y, RefPointName refpointName,
			   double angle, double scaleX, double scaleY,
			   boolean flipX, boolean flipY,
			   boolean imageInGCS)
	 throws IOException
     {
	 Point2D refpoint = getRefpoint(img, refpointName);
	 drawImage(g2d, img, x, y, refpoint, angle, scaleX, scaleY,
		   flipX, flipY, imageInGCS);
     }


     /**
      * Get the bounding box in graph coordinate space for an image
      * given a reference point name, angle, and scale factors, with a
      * pixel's height and width before scaling being a unit length in
      * either user space or graph coordinate space.
      * <P>
      * An image's reference point is the point in the image's coordinate
      * system that will be placed at the point (x, y) on the graph. For
      * this method, the reference point is determined by name, not by
      * its coordinates.
      * <P>
      * For images that are never translated or rotated, this method
      * can be used to compute a bounding box in graph coordinate space.
      * One can use that bounding box to determine if an attempt to
      * draw the image will be certain to fail. The bounding box is
      * not necessarily the smallest one possible.
      * <P>
      * When the argument imageInCGS is true, a pixel in the scaled
      * image will take up one unit in graph coordinate space. The
      * lower edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to the X axis in user space.
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpointName the name of the reference point of the image
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @return a bounding box in graph coordinate space
      * @exception IOException the image could not be loaded
      */
     public Rectangle2D imageBoundingBox(Image img, double x, double y,
					 RefPointName refpointName,
					 double angle, double scaleX,
					 double scaleY,
					 boolean imageInGCS)
	 throws IOException
     {
	 Point2D refpoint = getRefpoint(img, refpointName);
	 return imageBoundingBox(img, x, y, refpoint, angle, scaleX, scaleY,
				 imageInGCS);
     }
	
     /**
      * Draw an image given a reference point.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the image will take up one unit in graph coordinate
      * space. The upper edge of the image (from left to right in the
      * image's coordinate system) will be parallel to a line passing
      * through (x, y) in graph coordinate space and parallel to the
      * positive X axis.  The image will be placed so that its
      * reference point is at (x, y). Note that if the positive X axis
      * in graph coordinate space points right to left in user space,
      * the image will appear to have been rotated 180 degrees
      * relative to the positive X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, 0.0, 1.0, 1.0,
		   false, false, true);
     }

    /**
      * Draw an image given a reference point with each pixel
      * representing a unit in either user space or graph coordinate space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the image
      * will take up one unit in graph coordinate space. The upper
      * edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line passing through
      * (x, y) in graph coordinate space and parallel to the positive
      * X axis.  The image will be placed so that its reference point
      * is at (x, y). Note that if the positive X axis in graph
      * coordinate space points right to left in user space, the image
      * will appear to have been rotated 180 degrees relative to the
      * positive X axis in user space.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to the X axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param imageInGCS true if the image pixel size is a unit in
      *        graph coordinate space; false if the image pixel size
      *        is a unit in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, 0.0, 1.0, 1.0,
		   false, false, imageInGCS);
     }

     /**
      * Draw an image given a reference point and angle.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the image will take up one unit in graph coordinate
      * space. The lower edge of the image (from left to right in the
      * image's coordinate system) will be parallel to a line in user
      * space whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, angle, 1.0, 1.0,
		   false, false, true);
     }

     /**
      * Draw an image given a reference point and angle with each
      * pixel representing a unit in either user space or graph
      * coordinate space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the image
      * will take up one unit in graph coordinate space. The lower
      * edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to a line obtained by rotating
      * counter clockwise by the specified angle from the positive X
      * axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param imageInGCS true if the image pixel size is a unit in
      *        graph coordinate space; false if the image pixel size
      *        is a unit in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle,
			   boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, angle, 1.0, 1.0,
		   false, false, imageInGCS);
     }

     /**
      * Draw an image given a reference point, angle and scale factors.
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the scaled image will take up one unit in graph
      * coordinate space. The lower edge of the image (from left to
      * right in the image's coordinate system) will be parallel to a
      * line in user space whose direction is set by its angle from
      * the positive X direction when rotated towards the positive Y
      * direction, both in graph coordinate space. The image will be
      * placed so that the reference point is at the coordinates (x,
      * y) in graph coordinate space.  The mapping from
      * graph-coordinate-space angles to user-space angles is not
      * linear if the scale factors in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle,
			   double scaleX, double scaleY)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, angle, scaleX, scaleY,
		   false, false, true);
     }

     /**
      * Draw an image given a reference point, angle and scale
      * factors, with a pixel's height and width before scaling being
      * a unit length in either user space or graph coordinate space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the scaled
      * image will take up one unit in graph coordinate space. The
      * lower edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to a line obtained by rotating
      * counter clockwise by the specified angle from the positive X
      * axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle,
			   double scaleX, double scaleY, boolean imageInGCS)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, angle, scaleX, scaleY,
		   false, false, imageInGCS);
     }

     static final Point2D DEFAULT_REFPOINT = new Point2D.Double(0.0, 0.0);

     /**
      * Draw an image given a reference point, angle, scale factors,
      * reflection flags.
      * When an image is flipped in either the X or Y direction (or both),
      * it is flipped about the center of the image, and the reference point
      * remains at the same location relative to the image's upper left corner.
      * That is, the flipped image in effect overlays the original one and
      * replaces it.  The angle is the angle in user space, but reversed from
      * the usual clockwise direction used in most Java graphics operations
      * (to be consistent with the common case for graph coordinate space
      * where increasing X moves right and increasing Y moves up).
      * Each pixel's width and height are a single unit in graph coordinate
      * space.
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * A pixel in the scaled image will take up one unit in graph
      * coordinate space. The lower edge of the image (from left to
      * right in the image's coordinate system) will be parallel to a
      * line in user space whose direction is set by its angle from
      * the positive X direction when rotated towards the positive Y
      * direction, both in graph coordinate space. The image will be
      * placed so that the reference point is at the coordinates (x,
      * y) in graph coordinate space.  The mapping from
      * graph-coordinate-space angles to user-space angles is not
      * linear if the scale factors in the X and Y directions differ.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param flipX true of the image should be reflected about the Y axis
      *        in the image's coordinate system
      * @param  flipY true of the image should be reflected about the X axis
      *        in the image's coordinate system.
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle,
			   double scaleX, double scaleY,
			   boolean flipX, boolean flipY)
	 throws IOException
     {
	 drawImage(g2d, img, x, y, refpoint, angle, scaleX, scaleY,
		   flipX, flipY, true);
     }

     /**
      * Draw an image given a reference point, angle, scale factors, and
      * reflection flags, with a pixel's height and width before scaling being
      * a unit length in either user space or graph coordinate space.
      *
      * When an image is flipped in either the X or Y direction (or both),
      * it is flipped about the center of the image, and the reference point
      * remains at the same location relative to the image's upper left corner.
      * That is, the flipped image in effect overlays the original one and
      * replaces it. The angle is the angle in user space, but reversed from
      * the usual clockwise direction used in most Java graphics operations
      * (to be consistent with the common case for graph coordinate space
      * where increasing X moves right and increasing Y moves up).
      * <P>
      * There are of number of methods named <code>drawImage</code>. Details
      * for the arguments of these methods, include default values for
      * optional ones, can be found in the
      * <A href="#drawImg">overview for drawImage</A>.
      * <P>
      * When the argument imageInCGS is true, a pixel in the scaled
      * image will take up one unit in graph coordinate space. The
      * lower edge of the image (from left to right in the image's
      * coordinate system) will be parallel to a line in user space
      * whose direction is set by its angle from the positive X
      * direction when rotated towards the positive Y direction, both
      * in graph coordinate space. The image will be placed so that
      * the reference point is at the coordinates (x, y) in graph
      * coordinate space.  The mapping from graph-coordinate-space
      * angles to user-space angles is not linear if the scale factors
      * in the X and Y directions differ.
      * <P>
      * When the argument imageInCGS is false, a pixel in the scaled image
      * will take up one unit in user space. The lower and upper edges
      * of the image will be parallel to a line obtained by rotating
      * counter clockwise by the specified angle from the positive X
      * axis in user space.
      * @param g2d a graphics context on which to draw
      * @param img the image to draw
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param flipX true of the image should be reflected about the Y axis
      *        in the image's coordinate system
      * @param  flipY true of the image should be reflected about the X axis
      *        in the image's coordinate system.
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @exception IOException the image could not be loaded
      */
     public void drawImage(Graphics2D g2d, Image img, double x, double y,
			   Point2D refpoint, double angle,
			   double scaleX, double scaleY,
			   boolean flipX, boolean flipY,
			   boolean imageInGCS)
	 throws IOException
     {
	 if (imageInGCS) angle = getUserSpaceAngle(angle, true);
	 if (coordAF == null) createTransforms();
	 if (refpoint == null) refpoint = DEFAULT_REFPOINT;
	 if (imageInGCS) {
	     scaleX *= xscale;
	     scaleY *= yscale;
	 }

	 double iw = getImageWidth(img);
	 double ih = getImageHeight(img);


	 double iwr = refpoint.getX() * scaleX;
	 double ihr = refpoint.getY() * scaleY;


	 /*
	   double w = scaleX * iw;
	   double h = scaleY * ih;
	   double hw = w/2.0;
	   double hh = h/2.0;
	 */
	 Point2D point = coordTransform(x, y);
	 double xu = point.getX();
	 double yu = point.getY();

	 AffineTransform af = new AffineTransform();

	 af.translate(xu, yu);
	 // don't scale it at this point because we scaled it
	 // appropriately at the start (remember the transformations
	 // are added in reverse order)
	 af.rotate(-angle);
	 if (iwr != 0.0 || ihr != 0.0) {
	     af.translate(-iwr, -ihr);
	 }
	 af.scale(scaleX, scaleY);
	 if (flipX) {
	     AffineTransform flipit = new
		 AffineTransform(-1.0F, 0.0F, 0.0F, 1.0F, (float)(iw),
				 0.0);
	     af.concatenate(flipit);
	 }
	 if (flipY) {
	     AffineTransform flipit = new
		 AffineTransform(1.0F, 0.0F, 0.0F, -1.0F, 0.0, (float)ih);
	     af.concatenate(flipit);
	 }
	 Observer observer = new Observer(false, false, true);
	 if (g2d.drawImage(img, af, observer) == false) {
	     try {
		 synchronized(observer) {
		     while(!observer.done()) {
			 observer.wait();
		     }
		 }
		 g2d.drawImage(img, af, observer);
	     } catch (InterruptedException e) {
		 String msg = errorMsg("cannotLoadImage");
		 throw new IOException(msg, e);
	     }
	 }
     }


     /**
      * Get the bounding box in graph coordinate space for an image
      * given a reference point, angle, and scale factors, with a
      * pixel's height and width before scaling being a unit length in
      * either user space or graph coordinate space.
      * <P>
      * For images that are never translated or rotated, this method
      * can be used to compute a bounding box in graph coordinate space.
      * One can use that bounding box to determine if an attempt to
      * draw the image will be certain to fail. The bounding box is
      * not necessarily the smallest one possible.
      * <P>
      * The method {@link #getUserSpaceAngle(double,boolean)} can be used
      * to translate an angle in graph coordinate space into the appropriate
      * angle to use in user space, allowing for differences in scale factors
      * between the X and Y directions and any graph rotation that might have
      * been configured.
      * @param img the image
      * @param x the image's x coordinate in graph coordinate space
      * @param y the image's y coordinate in graph coordinate space
      * @param refpoint a reference point in the image's coordinate system
      *        that indicates the point on the image that will be placed at
      *        the point (x, y) in graph coordinate space
      * @param angle an angle, in radians and in user space, by which
      *        the image will be rotated in the counter clockwise
      *        direction, from its normal orientation, about the reference
      *        point
      * @param scaleX the scaling factor, in the X direction of the
      *        image, by which to change the size of the image
      * @param scaleY the scaling factor, in the Y direction of the
      *        image, by which to change the size of the image
      * @param imageInGCS true if a pixel's height and width before scaling
      *        represents a unit length in graph coordinate space; false
      *        if it represents a unit length in user space
      * @return a bounding box in graph coordinate space
      * @exception IOException the image could not be loaded
      */
     public Rectangle2D imageBoundingBox(Image img, double x, double y,
					 Point2D refpoint, double angle,
					 double scaleX, double scaleY,
					 boolean imageInGCS)
	 throws IOException
     {
	 // reproduce what we did to print in user space and
	 // then use an inverse coordinate transform to find the
	 // bounding box in graph coordinate space.

	 if (imageInGCS) angle = getUserSpaceAngle(angle, true);
	 if (coordAF == null) createTransforms();
	 if (refpoint == null) refpoint = DEFAULT_REFPOINT;
	 if (imageInGCS) {
	     scaleX *= xscale;
	     scaleY *= yscale;
	 }

	 double iw = getImageWidth(img);
	 double ih = getImageHeight(img);
	 double iwr = refpoint.getX() * scaleX;
	 double ihr = refpoint.getY() * scaleY;

	 Point2D point = coordTransform(x, y);
	 double xu = point.getX();
	 double yu = point.getY();

	 AffineTransform af = new AffineTransform();

	 af.translate(xu, yu);
	 // don't scale it at this point because we scaled it
	 // appropriately at the start (remember the transformations
	 // are added in reverse order)
	 // Because of the definition of the angle -- counter clockwise
	 // in user space from a horizontal line pointing right---the
	 // graphRotationAngle is included in this angle, so we
	 // don't have to adjust it as we did in getUserSpaceAngle.
	 af.rotate(-angle);
	 if (iwr != 0.0 || ihr != 0.0) {
	     af.translate(-iwr, -ihr);
	 }
	 af.scale(scaleX, scaleY);

	 Rectangle2D bbox = new Rectangle2D.Double(x, y, 0.0, 0.0);
	 Point2D dest = new Point2D.Double();
	 Point2D src = new Point2D.Double();

	 af.transform(DEFAULT_REFPOINT, dest);
	 invCoordAF.transform(dest, src);
	 bbox.add(src);
	 src.setLocation(0.0, ih);
	 af.transform(src, dest);
	 invCoordAF.transform(dest, src);
	 bbox.add(src);
	 src.setLocation(iw, 0.0);
	 af.transform(src, dest);
	 invCoordAF.transform(dest, src);
	 bbox.add(src);
	 src.setLocation(iw, ih);
	 af.transform(src, dest);
	 invCoordAF.transform(dest, src);
	 bbox.add(src);
	 return bbox;
     }

     /**
      * Class specifying a graph's font parameters.
      */
     public static class FontParms implements Cloneable  {
	 static Font defaultFont = new Font("Helvetica", Font.BOLD, 14);
	 static Color defaultColor = Color.BLACK;
	
	 Font font = defaultFont;
	 Color color = defaultColor;

	 Just justification = Just.LEFT;
	 BLineP baseLinePosition = BLineP.BASE;
	 double fontAngle = 0.0;

	 /**
	  * Constructor.
	  */
	 public FontParms() {}


	 /**
	  * Set the parameters to those provided by another instance.
	  * @param parms a FontParams instance whose parameters should be
	  * copied
	  */
	 public void set(FontParms parms) {
	     font = parms.font;
	     color = parms.color;
	     justification = parms.justification;
	     baseLinePosition = parms.baseLinePosition;
	     fontAngle = parms.fontAngle;
	 }

	 /**
	  * Set the font.
	  * @param f the font to use
	  */
	 public void setFont(Font f) {
	     font = (f == null)? defaultFont: f;
	 }
	 /**
	  * Get the font.
	  * @return the font
	  */
	 public Font getFont() {
	     return font;
	 }

	 /**
	  * Set the color of a font.
	  * @param c the color; null if the default color should be used
	  */
	 public void setColor(Color c) {
	     color = (c == null)? defaultColor: c;
	 }

	 /**
	  * Get the font color.
	  * @return the font color
	  */
	 public Color getColor() {
	     return color;
	 }

	 /**
	  * Set the font justification.
	  * @param j the font justification
	  */
	 public void setJustification(Just j) {
	     justification = j;
	 }

	 /**
	  * Get the font justification.
	  * @return the justification
	  */
	 public Just getJustification() {
	     return justification;
	 }

	 /**
	  * Set the font vertical alignment.
	  * @param blp the vertical alignment
	  */
	 public void setBaseline(BLineP blp) {
	     baseLinePosition = blp;
	 }

	 /**
	  * Get the font vertical alignment.
	  * @return the alignment
	  */
	 public BLineP getBaseline() {
	     return baseLinePosition;
	 }

	 /**
	  * Set the font angle.
	  * The font angle is measured counterclockwise.
	  * @param angle the angle in degrees
	  */
	 public void setAngle(double angle) {
	     fontAngle = angle;
	 }

	 /**
	  * Get the font angle
	  * @return the angle in degrees
	  */
	 public double getAngle() {
	     return fontAngle;
	 }

	 public Object clone() throws CloneNotSupportedException {
	     return super.clone();
	 }
     }

     private FontParms fontParms = new FontParms();


     /**
      * Set the font parameters for this graph.
      * @param parms a FontParams instance whose parameters should be
      *        copied; null to restore the default
      */
     public void setFontParms(FontParms parms) {
	 if (parms == null) {
	     fontParms = new FontParms();
	 } else {
	     fontParms.set(parms);
	 }
     }

     /**
      * Get the font parameters for this graph.
      * This method returns a copy of the current font parameters for
      * this graph.
      *@return the font parameters for this graph.
      */
     public FontParms getFontParms() {
	 try {
	     return (FontParms) fontParms.clone();
	 } catch (CloneNotSupportedException e) {
	     // cannot happen -- fontParms is not null and is defined
	     // to be cloneable.
	     return null;
	 }
     }

     Just justification = Just.LEFT;
     BLineP baseLinePosition = BLineP.BASE;
     double fontAngle = 0.0;	// in degrees

     /**
      * Set the font to use for this graph.
      * @param f the font to use
      */
     public void setFont(Font f) {
	 fontParms.setFont(f);
     }

     /**
      * Get the font for this graph.
      * @return the font
      */
     public Font getFont() {
	 return fontParms.getFont();
     }


     /**
      * Set the font color for this graph.
      * @param c the color; null if the default color should be used
      */
     public void setFontColor(Color c) {
	 fontParms.setColor(c);
     }

     /**
      * Get the font color for this graph.
      * @return the font color
      */
     public Color getFontColor() {
	 return fontParms.getColor();
     }

     /**
      * Set the font justification for this graph.
      * @param j the font justification
      */
     public void setFontJustification(Just j) {
	 fontParms.setJustification(j);
     }

     /**
      * Get the font justification for this graph.
      * @return the justification
      */
     public Just getFontJustification() {
	 return fontParms.getJustification();
     }

     /**
      * Set the font vertical alignment for this graph.
      * @param blp the vertical alignment
      */
     public void setFontBaseline(BLineP blp) {
	 fontParms.setBaseline(blp);
     }

     /**
      * Get the font vertical alignment for this graph.
      * @return the alignment
      */
     public BLineP getFontBaseline() {
	 return fontParms.getBaseline();
     }

     /**
      * Set the font angle for this graph.
      * @param angle the angle in degrees
      */
     public void setFontAngle(double angle) {
	 fontParms.setAngle(angle);
     }

     /**
      * Get the font angle for this graph.
      * @return the angle in degrees
      */
     public double getFontAngle() {
	 return fontParms.getAngle();
     }


     /**
      * Get a font with the same font family and style as the default font,
      * sized based on two points.
      * The font size will be as large as possible with the constraint that
      * the first argument, string, will have a length no larger than the
      * distance between p1 and p2 measured in user space.
      * @param string the string
      * @param p1 the first point in graph coordinate space
      * @param p2 the second point in graph coordinate space
      * @return the font
      */
     public Font getFontToFit(String string, Point2D p1, Point2D p2) {
	 Font font = fontParms.getFont();

	 return getFontToFit(string, p1, p2,
			     font.getFamily(),
			     font.getStyle());
     }


     Point2D p1Point = new Point2D.Double();
     Point2D p2Point = new Point2D.Double();


     /**
      * Get a font with a given font family and style,
      * sized based on two points.
      * The font size will be as large as possible with the constraint that
      * the first argument, string, will have a length no larger than the
      * distance between p1 and p2 in user space.
      * The size returned is based on the assumption that the string will be
      * drawn so that its baseline is parallel to a line from p1 to p2:
      * the length of a string for a given font varies slightly depending
      * on the string's orientation.
      * @param string the string
      * @param p1 the first point in graph coordinate space
      * @param p2 the second point in graph coordinate space
      * @param fontName the name of a font face or font family
      * @param fontStyle the style of the font (a logical 'or' of
      *        Font.PLAIN, Font.BOLD, and/or Font.ITALIC)
      * @return the font
      * @see java.awt.GraphicsEnvironment#getAllFonts()
      * @see java.awt.GraphicsEnvironment#getAvailableFontFamilyNames()
      */
     public synchronized Font getFontToFit(String string,
					   Point2D p1, Point2D p2,
					   String fontName, int fontStyle)
     {
	 int fontBaseSize = 0;
	 int fontSize = 1;
	 p1 = coordTransform(p1, p1Point);
	 p2 = coordTransform(p2, p2Point);
	 Font font = new Font(fontName, fontStyle, fontSize);
	 double dist = p1.distance(p2);
	 Graphics2D g2d = createGraphics();
	 try {
	     double x1 = p1.getX();
	     double y1 = p1.getY();
	     double x2 = p2.getX();
	     double y2 = p2.getY();
	     g2d.transform(AffineTransform.getRotateInstance(x2-x1, y2-y1,
							     x1, y1));
	     // g2d.setFont(font);
	     double textwidth =
		 g2d.getFontMetrics(font).getStringBounds(string, g2d)
		 .getWidth();
	     // int textwidth = g2d.getFontMetrics().stringWidth(string);
	     if (textwidth < dist) {
		 Font nextFont = font;
		 while ((double)textwidth < dist) {
		     font = nextFont;
		     int oldFontSize = fontSize;
		     int oldsize = fontBaseSize + fontSize;
		     fontSize *= 2;
		     nextFont = new Font(fontName, fontStyle,
					 fontBaseSize + fontSize);
		     // g2d.setFont(nextFont);
		     // textwidth = g2d.getFontMetrics().stringWidth(string);
		     textwidth =
			 g2d.getFontMetrics(nextFont)
			 .getStringBounds(string, g2d)
			 .getWidth();
		     /*
		       System.out.println("trying fontSize = "
		       + (fontBaseSize + fontSize)
		       + ", textwidth = " + textwidth
		       + "dist = " + dist);
		     */
		     if (textwidth == dist) return nextFont;
		     if (textwidth > dist && oldFontSize > 1) {
			 fontBaseSize = oldsize-1;
			 fontSize = 1;
			 nextFont = font;
			 g2d.setFont(nextFont);
			 textwidth =
			     g2d.getFontMetrics().getStringBounds(string, g2d)
			     .getWidth();
		     }
		 }
	     }
	 } finally {
	     g2d.dispose();
	 }
	 return font;
     }

     /**
      * Draw a string using the graph's font parameters.
      * @param string the string to draw
      * @param x the x position in graph coordinates
      * @param y the y position in graph coordinates
      */
     public void drawString(String string, double x, double y) {
	 drawString(string, x, y, fontParms);
     }

     /**
      * Draw a string.
      * @param string the string to draw
      * @param x the x position in graph coordinates
      * @param y the y position in graph coordinates
      * @param fp the font parameters to use
      */
     public void drawString(String string, double x, double y,
			    FontParms fp) {
	 if (fp == null) fp = fontParms;
	 Graphics2D g2d = (image != null)?image.createGraphics():
	     osg.createGraphics();
	 try {
	     g2d.setFont(fp.font);
	     g2d.setColor(fp.color);
	     // need a rotation to get the correct width, etc.
	     AffineTransform save = g2d.getTransform();
	     g2d.rotate(-Math.toRadians(fp.fontAngle), 0.0, 0.0);
	     FontRenderContext frc = g2d.getFontRenderContext();
	     LineMetrics metrics = fp.font.getLineMetrics(string, frc);
	     int width = g2d.getFontMetrics().stringWidth(string);
	     // now put it back so we can to the transform we need later
	     g2d.setTransform(save);

	     float joffset;
	     float boffset;

	     float xc, yc;
	     switch(fp.justification) {
	     default:
		 joffset = 0;
		 break;
	     case CENTER:
		 joffset = width/2.0F;
		 break;
	     case RIGHT:
		 joffset = (float) width;
		 break;
	     }
	     switch(fp.baseLinePosition) {
	     default:
		 boffset = 0;
		 break;
	     case CENTER:
		 boffset = (-metrics.getAscent() + metrics.getDescent())/2.0F;
		 break;
	     case TOP:
		 boffset = - metrics.getAscent();
		 break;
	     case BOTTOM:
		 boffset = metrics.getDescent();
		 break;
	     }
	     Point2D p = coordTransform((float)x, (float)y);
	     xc = (float) p.getX();
	     yc = (float) p.getY();
	     g2d.rotate(-Math.toRadians(fp.fontAngle), xc, yc);
	     g2d.drawString(string, xc - joffset, yc - boffset);
	     g2d.setTransform(save);
	 } finally {
	     g2d.dispose();
	 }
     }

     /*
     // Used internally so that a UserGraphic can be shifted in
     // graph coordinate space.
     private double xDrawingOffset = 0.0;
     private double yDrawingOffset = 0.0;
     */
     /**
      * Create a graphics context.
      * @return a new graphics context
      */
     public Graphics2D createGraphics() {
	 Graphics2D g2d = (image != null)? image.createGraphics():
	     osg.createGraphics();
	 /*
	 if (xDrawingOffset != 0.0 || yDrawingOffset != 0.0) {
	     g2d.translate(xDrawingOffset, yDrawingOffset);
	 }
	 */
	 return g2d;
     }

     /**
      * Create a Graphics context using graph coordinate space.
      * This is useful when one wishes to draw using a stroke whose
      * width is defined in graph coordinate space.  A graphics context
      * returned by this method should be used directly, with all operations
      * using units in graph coordinate space, including the width of strokes.
      * This is useful for instances of Graph used by the org.bzdev.anim2d
      * package.
      * @return a graphics context using graph coordinate space
      */
     public Graphics2D createGraphicsGCS() {
	 if (coordAF == null) createTransforms();
	 Graphics2D g2d = createGraphics();
	 g2d.transform(coordAF);
	 return g2d;
     }

     private AffineTransform coordAF = null;
     private AffineTransform invCoordAF = null;

     /**
      * Interface for objects that have a corresponding shape.
      * Objects implementing this interface are assumed to be
      * represented graphically by shapes, but are more conveniently
      * described in some other way.
      */
     public static interface Drawable {
	 /**
	  * Get the shape of the object.
	  * @return the shape of the object in graph coordinate space
	  */
	 public Shape toShape();

     }

     /**
      *  Interface for objects that can be drawn on a graph.
      *  These objects are drawn as if they have an origin at (0.0, 0.0) in
      *  graph coordinate space.
      */
     public static interface Graphic {
	 /**
	  * Add this object to a graph.
	  * The drawing operation is assumed to be a complex one that
	  * might involve fills, etc.
	  * <P>
	  * Any modifications to g2d or g2dGCS made by an implementation
	  * of this method should be undone before this method returns.
	  * For classes provided by the org.bzdev.anim2d package,
	  * such modifications must be undone before this method
	  * returns.
	  * <P>
	  * Two graphics contexts are provided as arguments. g2d will
	  * typically be used as the first argument to the graph's
	  * draw or fill methods.  In this case, the shape of an object
	  * is described in graph coordinate space but the widths of strokes
	  * drawn by fill operations, gradient paint, etc., have user-space
	  * units. The result is that line widths as they appear in an image
	  * are not sensitive to the mapping from graph coordinate space to
	  * user space.  One should use the draw and fill methods specified
	  * by {@link Graph}, using g2d as their first argument.
	  * By contrast, if a line width, etc., should be
	  * in graph-coordinate space units, one can use the g2dGCS argument
	  * directly. This will rarely be done when plotting a graph but
	  * is useful in animations.
	  * @param graph the graph on which this object should be drawn
	  * @param g2d the user-space Graphics2D to use to draw the Graphic
	  * @param g2dGCS the graph-coordinate space Graphics2D to use to
	  *        draw the Graphic
	  */
	 void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS);

	 /**
	  * Get the bounding box for this Graphic.
	  * Classes implementing this method should override it if
	  * a bounding box can be determined efficiently. It is intended
	  * for cases where a bounding-box comparison can allow drawing
	  * operations to be avoided.
	  * <P>
	  * The value returned must not be modified.
	  * @return the bounding box for this Graphic in graph
	  *         coordinate space; null if a bounding box has not been
	  *         computed
	  */
	 default Rectangle2D boundingBox() {
	     return null;
	 }
     }

     /**
      * Test if a Rectangle2D box1 touches  a Rectangle2D box2
      * This differs from the Rectangle2D method intersects in that
      * it works when box1 has an area of zero (a line, not a point).
      * @param box1 the first bounding box
      * @param box2 the second bounding box
      * @return true if a corner of box1 is contained by box2
      */
     private boolean touches(Rectangle2D box1, Rectangle2D box2) {
	 if (box1.intersects(box2)) return true;
	 if (box1.getWidth() == 0.0 || box1.getHeight() == 0.0) {
	     double x1 = box1.getMinX();
	     double y1 = box1.getMinY();
	     double x2 = box1.getMaxX();
	     double y2 = box1.getMaxY();
	     return box2.intersectsLine(x1, y1, x2, y2);
	 } else {
	     return false;
	 }
     }

     /**
      * Add a Graphic to a graph.
      * Adding refers to a sequence of graphic operations needed to
      * insert a complex object into a graph - something that might
      * require multiple calls to draw or fill methods.
      * The graphic will have its origin at (0.0, 0.0) in graph coordinate
      * space.
      * @param g the graphic to add to this graph
      */
     public void add(Graphic g) {
	 if (graphBoundingBoxGCS == null) {
	     boundingBox(true);
	 }
	 Rectangle2D bbox = g.boundingBox();
	 if (bbox == null || touches(bbox, graphBoundingBoxGCS)) {
	     Graphics2D g2d = createGraphics();
	     Graphics2D g2dGCS = createGraphicsGCS();
	     try {
		 g.addTo(this, g2d, g2dGCS);
	     } finally {
		 g2d.dispose();
		 g2dGCS.dispose();
	     }
	 }
     }




     /**
      * Add a graphic to a graph at a specified location.
      * Adding refers to a sequence of graphic operations needed to
      * insert a complex object into a graph - something that might
      * require multiple calls to draw or fill methods. Graphic g's
      * reference point at (0.0, 0.0) in graph coordinate space will
      * be moved to (x, y) in graph coordinate space and everything
      * drawn will be translated by the same amount as well.
      * @param g the graphic to add to this graph
      * @param x the x coordinate in graph-coordinate space at which
      *        the object should be placed
      * @param y the y coordinate in graph-coordinate space at which
      *        the object should be placed
      */
     public void add(Graphic g, double x, double y) {
	 Point2D location = coordTransform(x, y);
	 double xDrawingOffset  = location.getX();
	 double yDrawingOffset = location.getY();
	 Graphics2D g2d = createGraphics();
	 Graphics2D g2dGCS = createGraphicsGCS();
	 try {
	     g2d.translate(xDrawingOffset, yDrawingOffset);
	     g2dGCS.translate(x, y);
	     g.addTo(this, g2d, g2dGCS);
	 } finally {
	     g2d.dispose();
	     g2dGCS.dispose();
	 }
     }

     /**
      * Interface for user-space objects that can be drawn on a graph.
      * The object is drawn as if it was located at the
      * origin in user space and may be translated so that its reference
      * point will appear at a specified location in graph-coordinate space.
      * such a translation is provided by the Graphics2D passed as the
      * second argument to the {@link UserGraphic#addTo(Graph,Graphics2D)}
      * method.
      */
     public static interface UserGraphic {
	 /**
	  * Add this object to a graph.
	  * The drawing operation is assumed to be a complex one that
	  * might involve fills, etc. In a typical case, users will
	  * use the g2d argument directly, but may wish to look up
	  * various parameters associated with a graph (for example,
	  * the width or height).
	  * @param graph the graph on which this object should be drawn
	  * @param g2d the Graphics2D to use to draw the graph (which is
	  *        responsible for any necessary translations)
	  */
	 public void addTo(Graph graph, Graphics2D g2d);
     }

     /**
      * Add a graphic defined in user space to a graph.
      * Adding refers to a sequence of graphic operations needed to
      * insert a complex object into a graph - something that might
      * require multiple calls to draw or fill methods. The object is
      * assumed to have a reference point at location (0.0, 0.0) in
      * user space.
      * @param g the graphic to add to this graph
      * @param x the x coordinate in graph-coordinate space at which
      *        the object should be placed
      * @param y the y coordinate in graph-coordinate space at which
      *        the object should be placed
      */
     public void add(UserGraphic g, double x, double y) {
	 Point2D location = coordTransform(x, y);
	 double xDrawingOffset  = location.getX();
	 double yDrawingOffset = location.getY();
	 Graphics2D g2d = createGraphics();
	 try {
	     g2d.translate(xDrawingOffset, yDrawingOffset);
	     g.addTo(this, g2d);
	 } finally {
	     g2d.dispose();
	 }
     }

     /**
      * Add a UserGraphic at specified coordinates.
      * The UserGraphic is specified in user space with a reference point
      * at (0.0, 0.0) in user space. If the graphics context's affine
      * transformation differs from the affine transform of a graphics
      * context returned by createGraphics(), that additional transformation
      * will be applied before a translation to the user space coordinates
      * corresponding to (x, y) is applied.  This allows the graphics
      * context to provide its own transformation (e.g., for scaling or a
      * rotation).
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param graphic the UserGraphic to draw
      * @param x the x coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      * @param y the y coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      */
     public void add(Graphics2D g, UserGraphic graphic,
		      double x, double y) {
	 AffineTransform save = g.getTransform();
	 try {
	     AffineTransform af = g.getTransform();
	     Graphics2D g2d = createGraphics();
	     try {
		 AffineTransform g2daf = g2d.getTransform();
		 boolean isIdent = g2daf.isIdentity();
		 AffineTransform invaf;
		 if (isIdent) {
		     invaf = g2daf;
		 } else {
		     try {
			 invaf = g2daf.createInverse();
		     } catch (Exception e) {
			 // should never happen
			 return;
		     }
		 }
		 // remove user-space to device space transformations
		 af.preConcatenate(invaf);
		 // get the reference point at which to draw in user space
		 // and create an affine transformation to translate to the
		 // reference point
		 Point2D location = coordTransform(x, y);
		 AffineTransform paf = new AffineTransform();
		 paf.translate(location.getX(), location.getY());
		 // rebuild the affine transform and draw the shape
		 af.preConcatenate(paf);
		 if (!isIdent) af.preConcatenate(g2daf);
		 g.setTransform(af);
		 graphic.addTo(this, g);
	     } finally {
		 g2d.dispose();
	     }
	     // restore the original transformation
	 } finally {
	     g.setTransform(save);
	 }
     }


     /**
      * Draw a Drawable.
      * The drawable must be specified in graph coordinate space. That is,
      * its {@link Drawable#toShape()} method must return a shape whose
      * control points or dimensions are specified in graph coordinate
      * space units.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param drawable the Drawable to draw
      */
     public void draw(Graphics2D g, Drawable drawable) {
	 draw(g, drawable.toShape());
     }

     /**
      * Fill a Drawable.
      * The drawable must be specified in graph coordinate space.  That is,
      * its {@link Drawable#toShape()} method must return a shape whose
      * control points or dimensions are specified in graph coordinate
      * space units.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param drawable the Drawable to fill
      */
     public void fill(Graphics2D g, Drawable drawable) {
	 fill(g, drawable.toShape());
     }

     /**
      * Interface for user-space objects that have a corresponding shape.
      * Objects implementing this interface are assumed to be
      * represented graphically by shapes, but are more conveniently
      * described in some other way.
      * <P>
      * The value provided by {@link UserDrawable#toShape(boolean,boolean)}
      * may depend on the direction the positive X and/or Y axes in
      * graph coordinate space point in user space. Some objects drawn
      * may need to be drawn to match the direction of the coordinate
      * axes (for example, an arrow that should point in the direction of the
      * positive X axis in graph coordinate space, regardless of whether
      * the positive X axis in graph coordinate space points left or right
      * in user space).
      */
     public static interface UserDrawable {
	 /**
	  * Get the shape of the object.
	  * The shape is specified relative to the origin of user
	  * space [(0.0, 0.0) in user space].
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         in graph coordinate space points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        in graph coordinate space points down in user space
	  * @return the shape of the object specifying user-space control
	  *         points or dimensions
	  */
	 public Shape toShape(boolean xAxisPointsRight,
			      boolean yAxisPointsDown);
     }


     /**
      * Draw a UserDrawable.
      * The Drawable is specified in user space with a reference point
      * at (0.0, 0.0) in user space. Any modifications to the graphics
      * context's affine transform will be applied to the drawable,
      * possibly moving the drawable with respect to the reference point,
      * rotating the drawable, or shearing it. After any such transform
      * is applied, the point (0.0, 0.0) will be translated to the
      * location in user space corresponding to (x, y) in graph
      * coordinate space.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param drawable the UserDrawable to draw
      * @param x the x coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      * @param y the y coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      */
     public void draw(Graphics2D g, UserDrawable drawable,
		      double x, double y) {
	 AffineTransform save = g.getTransform();
	 try {
	     AffineTransform af = g.getTransform();
	     Graphics2D g2d = createGraphics();
	     try {
		 AffineTransform g2daf = g2d.getTransform();
		 boolean isIdent = g2daf.isIdentity();
		 AffineTransform invaf;
		 if (isIdent) {
		     invaf = g2daf;
		 } else {
		     try {
			 invaf = g2daf.createInverse();
		     } catch (Exception e) {
			 // should never happen
			 return;
		     }
		 }
		 // remove user-space to device space transformations
		 af.preConcatenate(invaf);
		 // get the reference point at which to draw in user space
		 // and create an affine transformation to translate to the
		 // reference point
		 Point2D location = coordTransform(x, y);
		 AffineTransform paf = new AffineTransform();
		 paf.translate(location.getX(), location.getY());
		 // rebuild the affine transform and draw the shape
		 af.preConcatenate(paf);
		 if (!isIdent) af.preConcatenate(g2daf);
		 g.setTransform(af);
		 g.draw(drawable.toShape((xscaleSigned >= 0.0),
					 (yscaleSigned >= 0.0)));
	     } finally {
		 g2d.dispose();
	     }
	 } finally {
	     // restore the original transformation
	     g.setTransform(save);
	 }
     }

     /**
      * Fill a UserDrawable.
      * The Drawable is specified in user space with a reference point
      * at (0.0, 0.0) in user space. Any modifications to the graphics
      * context's affine transform will be applied to the drawable,
      * possibly moving the drawable with respect to the reference point,
      * rotating the drawable, or shearing it.  After any such transform
      * is applied, the point (0.0, 0.0) will be translated to the
      * location in user space corresponding to (x, y) in graph
      * coordinate space.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param drawable the UserDrawable to fill
      * @param x the x coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      * @param y the y coordinate in graph-coordinate space at which
      *        the object's reference point should be placed
      */
     public void fill(Graphics2D g, UserDrawable drawable,
		      double x, double y) {
	 AffineTransform save = g.getTransform();
	 try {
	     AffineTransform af = g.getTransform();
	     Graphics2D g2d = createGraphics();
	     try {
		 AffineTransform g2daf = g2d.getTransform();
		 boolean isIdent = g2daf.isIdentity();
		 AffineTransform invaf;
		 if (isIdent) {
		     invaf = g2daf;
		 } else {
		     try {
			 invaf = g2daf.createInverse();
		     } catch (Exception e) {
			 // should never happen
			 return;
		     }
		 }
		 af.preConcatenate(invaf);
		 // Point2D location=g2daf.transform(coordTransform(x,y), null);
		 Point2D location = coordTransform(x, y);
		 AffineTransform paf = new AffineTransform();
		 paf.translate(location.getX(), location.getY());
		 af.preConcatenate(paf);
		 if (!isIdent) af.preConcatenate(g2daf);
		 g.setTransform(af);
		 g.fill(drawable.toShape((xscaleSigned >= 0.0),
					 (yscaleSigned >= 0.0)));
	     } finally {
		 g2d.dispose();
	     }
	 } finally {
	     g.setTransform(save);
	 }
     }

     /**
      * Returns a shape in an image's or graphic context's user space
      * given a shape in graph coordinate space.
      * @param s the original shape
      * @return the new shape
      */
     public Shape coordTransform(Shape s) {
	 if (coordAF == null) createTransforms();
	 if (s instanceof RectangularShape &&
	     (s instanceof Rectangle || s instanceof Rectangle2D)) {
	     RectangularShape rect = (RectangularShape)s;
	     Point2D minp = coordTransform(rect.getMinX(), rect.getMinY());
	     Point2D maxp = coordTransform(rect.getMaxX(), rect.getMaxY());
	     double xminp = minp.getX();
	     double yminp = minp.getY();
	     double xmaxp = maxp.getX();
	     double ymaxp = maxp.getY();
	     double x = (xminp < xmaxp)? xminp: xmaxp;
	     double y = (yminp < ymaxp)? yminp: ymaxp;
	     double w = Math.abs(xminp - xmaxp);
	     double h = Math.abs(yminp - ymaxp);
	     Rectangle2D r = new Rectangle2D.Double(x, y, w, h);
	     return r;
	 }
	 return new Path2D.Double(s, coordAF);
     }

     /**
      * Returns a shape in an image's or graphic context's user space
      * given a shape in graph coordinate space, preferentially making
      * the shape a {java.awt.Rectangle} when the shape is already an
      * instance of {java.awt.geom.RectangularShape}.
      * <P>
      * The method {@link java.awt.Graphics#setClip(Shape)} supports shapes
      * that are returned by {@link java.awt.Graphics#getClip()} or that
      * are instances of {@link java.awt.Rectangle}. This method will
      * convert a rectangular shape into a Rectangle, providing the
      * smallest {@link java.awt.Rectangle} that contains the shape.
      * @param s the original shape
      * @return the new shape
      */
     public Shape coordTransformForClip(Shape s) {
	 s = coordTransform(s);
	 if (s instanceof RectangularShape) {
	     if (s instanceof Rectangle) {
		 return s;
	     } else {
		 RectangularShape r = (RectangularShape) s;
		 double xmin = r.getMinX();
		 double ymin = r.getMinY();
		 double width = r.getWidth();
		 double height = r.getHeight();
		 int x = (int)Math.floor(xmin);
		 int y = (int)Math.floor(ymin);
		 int w = (int)Math.ceil(width);
		 int h = (int)Math.ceil(height);
		 if ((double)(x + w) < (xmin+width)) {
		     w++;
		 }
		 if ((double)(y+h) < (ymin+height)) {
		     h++;
		 }
		 return new Rectangle(x,y,w,h);
	     }
	 } else {
	     return s;
	 }
     }

     /**
      * Returns a shape in graph coordinate space given a shape in
      * user space.
      * @param s the original shape
      * @return the new shape
      */
     public Shape invCoordTransform(Shape s) {
	 if (coordAF == null) createTransforms();
	 return new Path2D.Double(s, invCoordAF);
     }

     /**
      * Draw a shape.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param s a shape in graph coordinate space
      */
     public void draw(Graphics2D g, Shape s) {
	 g.draw(coordTransform(s));
     }

     /**
      * Fill a shape.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param s a shape in graph coordinate space
      */
     public void fill(Graphics2D g, Shape s) {
	 g.fill(coordTransform(s));
     }

     /**
      * Clip a graphics context based on a shape
      * The shape will be converted to user space before the clip
      * method is called on the graphics context. When the argument
      * s is an instance of {@link java.awt.geom.RectangularShape},
      * the converted shape will be the smallest instance of
      * {@link java.awt.Rectangle} that fully contains s.
      * @param g a graphics context that was obtained by calling the
      *        graph's {@link #createGraphics()} method
      * @param s a shape in graph coordinate space
      * @see java.awt.Graphics2D#clip(Shape)
      */
     public void clip(Graphics2D g, Shape s) {
	 g.clip(coordTransformForClip(s));
     }

     /**
      * Convert x-y coordinates as floats from graph coordinate space
      * to user space.
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @return the corresponding point in user space
      */
     public Point2D coordTransform(float x, float y) {
	 Point2D p = new Point2D.Float(x,y);
	 return coordTransform(p);
     }

     /**
      * Convert x-y coordinates as doubles from graph coordinate space
      * to user space.
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @return the corresponding point in user space
      */
     public Point2D coordTransform(double x, double y) {
	 Point2D p = new Point2D.Double(x,y);
	 return coordTransform(p);
     }

     /**
      * Convert a point in graph coordinate space to user space.
      * @param point the point in graph coordinate space
      * @return the corresponding point in user space
      */
     public Point2D coordTransform(Point2D point) {
	 return coordTransform(point, null);
     }

     /**
      * Convert a point in graph coordinate space to user space and store it.
      * @param srcPoint the point in graph coordinate space
      * @param destPoint a point used to store the coordinates in user space;
      *        null if a new point should be created to store the results
      * @return the corresponding point in user space
      */
     public Point2D coordTransform(Point2D srcPoint, Point2D destPoint ) {
	 if (coordAF == null) createTransforms();
	 return coordAF.transform(srcPoint, destPoint);
     }

     /**
      * Convert the x-y coordinates, as doubles, in user space to a
      * point in graph coordinate space.
      * @param x the x coordinate
      * @param y the y coordinate
      * @return the corresponding point in graph-coordinate space
      */
     public Point2D invCoordTransform(double x, double y)
	 throws IllegalStateException
	
     {
	 return invCoordTransform(new Point2D.Double(x, y));
     }

     /**
      * Convert the x-y coordinates, as floats, in user space to a
      * point in graph coordinate space.
      * @param x the x coordinate
      * @param y the y coordinate
      * @return the corresponding point in graph-coordinate space
      */
     public Point2D invCoordTransform(float x, float y)
	 throws IllegalStateException
     {
	 return invCoordTransform(new Point2D.Float(x, y));
     }


     /**
      * Convert a point in user space to a point in graph coordinate
      * space.
      * @param point a point in user space
      * @return the corresponding point in graph-coordinate space
      */
     public Point2D invCoordTransform(Point2D point)
	 throws IllegalStateException
     {
	 return invCoordTransform(point, null);
     }

     /**
      * Convert a point in user space to a point in graph coordinate
      * space and store it.
      * @param srcPoint the point in graph coordinate space
      * @param destPoint a point used to store the coordinates in user space;
      *        null if a new point should be created to store the results
      * @return the corresponding point in graph coordinate space
      */
     public Point2D invCoordTransform(Point2D srcPoint, Point2D destPoint )
	 throws IllegalStateException
     {
	 if (coordAF == null) createTransforms();
	 if (invCoordAF == null)
	     throw new IllegalStateException(errorMsg("noInverseTransform"));
	 else
	     return invCoordAF.transform(srcPoint, destPoint);
     }

     /**
      * Class specifying graph tick marks along an axis.
      * Instances of this class are used by {@link Graph.Axis}
      * to determine whether a tick at an index (see {@link Graph.Axis})
      * is printed and if printed, how it is printed.
      * Ticks are characterized by several parameters:
      * <UL>
      *   <LI><code>length</code>. The length is a scaling factor
      *   that is multiplied by an axis width to obtain the length
      *   of a tick mark in user space.
      *   <LI><code>width</code>. The width is  a scaling factor
      *   that is multiplied by an axis width to obtain the width
      *   of a tick mark in user space.
      *   <LI><code>mod</code>. This is the modulus for a tick mark
      *       and is a positive integer. The value <code>index%mod</code>
      *       is used in  tests described below.
      *   <LI><code>modtest</code>. This value is a non-negative integer.
      *       For a given index supplied by a caller, a necessary
      *       condition for a tick mark to be shown is that
      *       <code>(index % mod) == modtest</code>.
      *   <LI><code>limit</code>. When 0, this field is ignored. It
      *        is also ignored if the limit modulus of the {@link Graph.Axis}
      *        is zero. Otherwise <code>limit</code> is a positive integer and
      *        a necessary condition for a tick mark to be shown
      *        is that <code>((index % mod) % limitModulus &le; limit</code>.
      *        The limit modulus is set to zero for instances of
      *        {@link Graph.Axis} and to a nonzero value for instances of
      *        {@link Graph.LogAxis}. For a logarithmic axis, a limit allows
      *        closely-spaced ticks to be used near the start of a decade
      *        but suppressed near its end where tick marks tend to
      *        "bunch up".
      *   <LI><code>format</code>. A format provides the format for a
      *       tick-specific label. This is used by the method
      *       {@link Graph.TickSpec#getTickLabel(double,double,Graph.Axis,long)}
      *       to format a double-precision value using an instance of
      *       {@link org.bzdev.util.SciFormatter} to format the value.
      *   <LI><code>stringOffset</code>. The offset in user-space units
      *       between a tick mark and a string labeling the tick mark.
      * </UL>
      * To label ticks {@link org.bzdev.util.SciFormatter SciFormatter}
      * is used (although the method that creates the label can be
      * overridden).  This formatter is similar to java.util.Formatter,
      * but can print numbers using scientific notation by using Unicode
      * to provide multiplication signs and superscripts.
      * <P>
      * When multiple TickSpec instances are added to an axis,
      * they are sorted on the basis of the modulus, length, width,
      * modtest, string offset, and existence of a format (in that
      * order with the first that does not produce a tie determining
      * the comparison. The order is determined by
      * <UL>
      *   <LI> the modulus.
      *   <LI> the length for tied values of the modulus.
      *   <LI> the width for tied values of the length and modulus.
      *   <LI> the modtest value for tied values of the width, etc.
      *   <LI> the string offset for tied values of the modtest, etc.
      *   <LI> the presence of a format for tied values of the string offset,
      *        etc.
      * </UL>
      * While the TickSpec class determines the ordering the class
      * {@link Graph.Axis} sorts its instances of TickSpec into the
      * appropriate order and selects the best one to use.
      * <P>
      * For a linear axis, one will typically use a construct whose
      * arguments provide a tick length, tick width, and a modulus,
      * with optionally a format and string offset. For an axis
      * going from 20 to 50, with a tick increment of 1, one might
      * use three instances of {@link Graph.TickSpec TickSpec}:
      * <UL>
      *   <LI> new Graph.TickSpec(3.0, 1.0, 1)
      *   <LI> new Graph.TickSpec(5.0, 2.0, 5)
      *   <LI> new Graph.TickSpec(5.0, 2.0, 10, "%2.0F", 5.0)
      * </UL>
      * This will produce ticks of various lengths, with the
      * most 3.0 units long. Tick marks at 25, 35, and 45 will be
      * 5 units long and twice as wide.  The tick marks at
      * 20, 30, 40, and 50 will additionally have a label providing
      * the coordinate.
      * <P>
      * For a logarithmic axis going from 1.0 to 100.0 (0 to 2 in graph
      * coordinate space)  with a tick increment of 1.0, one might use
      * the following tick specifications:
      * <UL>
      *    <LI> <code>new Graph.TickSpec(10.0, 1.0, 9, "%3.0f", 5.0)</code>.
      *         This will create ticks with labels at x values of
      *         1.0, 10.0, and 100.0 (user-space values are 0.0, 1.0, and 2.0
      *         respectively).
      *    <LI> <code>new Graph.TickSpec(5.0, 1.0, 9, 4)</code>.
      *         This will create ticks at 5.0 and 50.0 (user-space values
      *         are log<sub>10</sub>(5) and log<sub>20</sub>(50) respectively).
      *    <LI> <code> new Graph.TickSpec(3.0, 0.5, 1)</code>.
      *         This will create tick marks at 2, 3, 4, 5, 6, 7, 8, 9,
      *         20, 30, 40, 50, 60, 70, 80, and 90. It will also specify
      *         tick marks at 1, 10, and 100 but the first specification
      *         will be chosen due to the search order so the tick marks
      *         at 1, 10, and 100 will be ignored for this tick specification.
      * </UL>
      * <P>
      * Subclasses may be written to handle additional cases.
      * These can use the methods
      * <UL>
      *   <LI> {@link Graph.TickSpec#showTick(double,double,Graph.Axis,long)}.
      *        This method determines if a tick might be shown.
      *   <LI> {@link Graph.TickSpec#showTickLabel(double,double,Graph.Axis,long)}.
      *        This method determines if a tick-mark-specific label should
      *        be printed.
      *   <LI> {@link Graph.TickSpec#getTickLabel(double,double,Graph.Axis,long)}.
      *        This method computes the string for a tick-mark-specific label.
      * </UL>
      * All of these three methods take the same arguments:
      * <UL>
      *   <LI> the numerical value to be printed.
      *   <LI> the coordinate (X or Y depending on the direction of the axis)
      *        in graph coordinate space where the tick mark should be
      *        printed.
      *   <LI> the axis.
      *   <LI> the index.
      * </UL>
      * To implement a bar chart, for example, one could space the
      * ticks appropriately and override
      * {@link Graph.TickSpec#getTickLabel(double,double,Graph.Axis,long)}
      * so that it returns an appropriate string based on the index.
      * The default implementation for these methods is appropriate for
      * linear and logarithmic axes.
      */
     public static class TickSpec implements Comparable<TickSpec> {
	 double length;
	 double width;
	 long mod;
	 long modtest;
	 long limit;
	 String format;
	 double stringOffset;

	 public int compareTo(TickSpec other) {
	     long diffL = (mod - other.mod);
	     int diff = (diffL < 0)? -1:
		 ((diffL == 0)? 0: 1);
	     if (diff == 0) {
		 if (length > other.length) return -1;
		 if (length < other.length) return 1;
		 if (width > other.width) return -1;
		 if (width < other.width) return 1;
		 if (modtest > other.modtest) return -1;
		 if (other.modtest < modtest) return 1;
		 if (stringOffset > other.stringOffset) return -1;
		 if (stringOffset < other.stringOffset) return 1;
		 if (other.format == null) return -1;
		 if (format == null) return 1;
	     }
	     return -diff;
	 }

	 public boolean equals(Object obj) {
	     if (obj instanceof TickSpec
		 && obj.getClass().equals(TickSpec.class))
	     {
		 TickSpec other = (TickSpec) obj;
		 boolean result = (length == other.length)
		     && (width == other.width)
		     && (mod == other.mod)
		     && (modtest == other.modtest)
		     && (stringOffset == other.stringOffset)
		     && format.equals(other.format);
		 return result;
	     } else {
		 return false;
	     }
	 }

	 /**
	  * Get the format string.
	  * @return the format string provided by a constructor; null
	  *         if there is none
	  */
	 public String getFormat() {
	     return format;
	 }

	 /**
	  * Determine if a tick mark should be shown.
	  * The default implementation computes (ind % mod), where mod is
	  * a parameter given to the constructor,  and allows a
	  * tick mark when (ind % mod) is equal to the value of the modtest
	  * argument provided by a constructor (the default for modtest is
	  * zero).
	  * If a positive limit modulus (<code>limitModulus</code>) was
	  * provided with a non-negative limit,
	  * <code>((ind % mod) % limitModulus)</code> must be less than or
	  * equal to <code>limit</code>.  For example, for a logarithmic
	  * axis, a limit allows a higher density of ticks to be used
	  * at the start of a decade, but not near its end, where the
	  * tick marks tend to "bunch up".
	  * @param s the numerical value to be printed
	  * @param sc the location along the axis in graph coordinate space
	  * @param axis the axis
	  * @param ind the index (the number of tick locations before the
	  *        current one)
	  * @return true if a tick should be displayed; false otherwise
	  */
	 public boolean showTick(double s, double sc, Axis axis, long ind)
	     throws UnsupportedOperationException
	 {
	     long modind = ind % mod;
	     long limitModulus = axis.getLimitModulus();
	     long modind2 = (limitModulus == 0 || limit < 0)? -1:
		 (ind % limitModulus);
	     return ((modind == modtest) && (modind2 <= limit));
	 }

	 /**
	  * Determine if a label should be printed.
	  * This method is only called if showTick with the same
	  * arguments returns true.  The default implementation checks
	  * that the format field (set by a constructor) is not null
	  * and if that is the case, returns true; false otherwise.
	  * @param s the numerical value to be printed
	  * @param sc the location along the axis in graph coordinate space
	  * @param axis the axis
	  * @param ind the index (the number of tick locations before the
	  *        current one)
	  * @return true if a string should be displayed; false otherwise
	  */
	 public boolean showTickLabel(double s, double sc, Axis axis, long ind)
	 {
	     return (format != null);
	 }

	 /**
	  * Get the format string for a tick mark.
	  * This method is only called if showTickLabel, when called
	  * with the same arguments, returns true.  The default
	  * implementation simply returns a string representation of s
	  * using the format passed to some of the constructors. The
	  * formatting is done using
	  * {@link org.bzdev.util.SciFormatter SciFormatter}.
	  * This can be overridden if different tick marks require
	  * different formats.
	  * @param s the numerical value to be printed
	  * @param sc the location along the axis in graph coordinate space
	  * @param axis the axis for this label
	  * @param ind the index  (the number of tick locations before the
	  *        current one)
	  * @return the formatted string; null if there is none
	  */
	 public String getTickLabel(double s, double sc, Axis axis, long ind) {
	     SciFormatter sf = new SciFormatter();
	     sf.format(format, s);
	     return sf.toString();
	 }

	 /**
	  * Constructor.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  */
	 public TickSpec(double length, double width, int mod) {
	     this(length, width, mod, 0, -1, null, 0.0);
	 }


	 /**
	  * Constructor with  modulus test.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  * @param modtest the modulus test
	  */
	 public TickSpec(double length, double width, int mod,
			 int modtest) {
	     this(length, width, mod, modtest, -1, null, 0.0);
	 }

	 /**
	  * Constructor with limit and modulus test.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  * @param modtest the modulus test
	  * @param limit the maximum value of the index (mod limitModulus);
	  *        -1 if the limit should be ignored
	  */
	 public TickSpec(double length, double width, int mod,
			 int modtest, int limit) {
	     this(length, width, mod, modtest, limit, null, 0.0);
	 }


	 /**
	  * Constructor with format.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  * @param format the printf format used to determine the label
	  *        string for an argument of type double; null if there is none
	  * @param stringOffset the distance in user space between a tick mark
	  *        and a label
	  */
	 public TickSpec(double length, double width, int mod,
			 String format, double stringOffset) {
	     this(length, width, mod, 0, -1, format, stringOffset);
	 }

	 /**
	  * Constructor with format and modulus test.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  * @param modtest the modulus test
	  * @param format the printf format used to determine the label
	  *        string for an argument of type double; null if there is none
	  * @param stringOffset the distance in user space between a tick mark
	  *        and a label
	  */
	 public TickSpec(double length, double width,
			 int mod, int modtest,
			 String format, double stringOffset)
	     {
		 this(length, width, mod, modtest, -1, format, stringOffset);
	     }

	 /**
	  * Constructor with format, modulus test, and limit.
	  * @param length the length of a tick mark in multiples of the
	  *        axis width, which is in user-space units
	  * @param width the width of a tick mark  in multiples of the
	  *        axis width, which is in user-space units
	  * @param mod the modulus for determining when tick marks are shown
	  * @param modtest the modulus test
	  * @param limit the maximum value of the index (mod limitModulus);
	  *        -1 if the limit should be ignored
	  * @param format the printf format used to determine the label
	  *        string for an argument of type double; null if there is none
	  * @param stringOffset the distance in user space between a tick mark
	  *        and a tick label
	  */
	 public TickSpec(double length, double width,
			 int mod, int modtest, int limit,
			 String format, double stringOffset)
	     {
		 this.length = length;
		 this.width = width;
		 this.mod = mod;
		 this.modtest = modtest;
		 this.limit = limit;
		 this.format = format;
		 this.stringOffset = stringOffset;
	     }
     }

     /**
      * Class to specify a graph axis.
      * This class supports a linear axis. It's subclass, LogAxis, provides
      * a logarithmic axis. Users may wish to add additional subclasses.
      * <P>
      * A graph axis has a starting point, which is either an instance
      * of {@link java.awt.geom.Point2D} or two arguments giving the
      * starting points X and Y coordinates,. In both cases, the
      * coordinates are specified
      * in graph coordinate space. There is also a direction, a
      * {@link Graph.Axis.Dir Graph.Axis.Dir} constant VERTICAL_INCREASING,
      * VERTICAL_DECREASING, HORIZONTAL_DECREASING, and HORIZONTAL_INCREASING,
      * and a length, again a distance in graph coordinate space.
      * These are provided as arguments to a constructor.
      * Tick marks, if specified, will be on the clockwise or counterclockwise
      * side of the axis. The possible locations of tick marks are defined
      * by arguments given to the constructor:
      * <ul>
      *  <li> <code>tickBase</code> gives the x or y coordinate in graph
      *       coordinate space to begin counting possible tick
      *       locations. The choice between an x or y coordinate is determined
      *       by the direction of the axis.
      *  <li> <code>tickIncr</code> is the increment in graph coordinate space
      *        between possible tick locations. This is always a
      *        positive number: the direction of the axis determines
      *        the sign. The interpretation of this value is subclass
      *        specific. For this class, tick locations are linearly spaced
      *        and the tick increment is the spacing between potential
      *        tick-mark locations.
      *  <li> <code>counterclockwise</code> is a boolean field. When true,
      *       any tick marks are shown in the counterclockwise direction
      *       from a line pointing in the direction of the axis.
      * </ul>
      * Starting from tickBase, one increments a counter, moving in graph
      * coordinate space by tickIncr each time the counter is incremented by 1,
      * until one reaches the start of the axis. At this point, ticks may
      * be drawn as long as the tick will not be located past the end of
      * the axis. How ticks are drawn is specified by instances of the
      * class {@link Graph.TickSpec}. Multiple instances of
      * {@link Graph.TickSpec} can be added to an axis. Some rules provided
      * in the documentation for {@link Graph.TickSpec} indicate which
      * one is chosen at a given value of the counter.
      * Note that {@link Graph.LogAxis} handles ticks differently than this
      * class handles them.
      * <P>
      * The tick marks themselves are specified by creating instances
      * of the class {@link Graph.TickSpec} and using the method
      * {@link Graph.Axis#addTick(Graph.TickSpec)} to add a tick-mark
      * specification to the axis. At runtime, an appropriate tick-mark
      * specification will be chosen for each tick mark.
      * <P>
      * In addition, an axis may have a label. The method
      * {@link Graph.Axis#setLabel(String)} provides the text of the
      * label and the method {@link Graph.Axis#setLabelOffset(double)}
      * provides the separation between the label and an axis's tick
      * marks (including any text labeling a tick mark).
      * In addition, there are some methods for determining properties
      * of an axis: its width, color, and the font parameters used for
      * labels.
      * <P>
      * As an example, if an x axis goes from 20 to 50 in graph
      * coordinate space, the initial X coordinate will be 20 and the
      * length of the axis will be 30. Setting the tick increment to
      * 1.0 will allow ticks to be drawn at X coordinates 20, 21, 22,
      * ..., 49, 50. In this case the tick base should have the value
      * 20.  One can then arrange for a label to appear at every
      * 10<sup>th</sup> tick, which will place tick-mark labels at 20,
      * 30, 40, and 50.  By constraint, if the axis went from 23 to
      * 53, the initial X coordinate would be 23, and the length would
      * still be 30.  By putting the tick base value at 20, every
      * 10<sup>th</sup> tick will still be at 30, 40, and 50, where
      * tick-mark labels would still be printed. A label at 20 would
      * not be printed because 20 is outside the range of values that
      * can appear on the axis.
      * <P>
      * Subclasses may use the following methods:
      * <UL>
      *   <LI><code>initialIndex()</code>. This returns the initial value
      *       of the index - the minimum index such that a call
      *       to axisCoord(initialIndex()) will be the coordinate
      *       (x or y depending on the direction) of a point on
      *       the axis.
      *   <LI><code>axisValue(long index)</code>. This returns the
      *       position corresponding to an index. The value is the
      *       one used to print any label associated with a tick mark.
      *   <LI><code>axisCoord(long index)</code>. This returns
      *       the position along an axis as a point in graph coordinate
      *       space and is used to determine where a tick will be
      *       drawn.
      *   <LI><code>notDone(double coordinate)</code>. This returns
      *       true if is argument (a coordinate in graph coordinate space)
      *       is one that lies on the axis. It returns false if the
      *       point is off the axis.  The implementation increments
      *       the index until <code>notDone(axisCoord(index))</code>
      *       returns false at the beginning of a <code>while</code>
      *       loop.
      * </UL>
      * For this class, <code>axisCoord</code> and <code>axisValue</code>
      * return the same value. For the class <code>Graph.LogAxis</code>,
      * different values are returned.
      * @see Graph.TickSpec
      */
     public static class Axis {
	 /**
	  * The direction of an axis.
	  * The direction an axis points is defined as a direction
	  * in graph coordinate space.
	  */
	 public static enum Dir {
	     /**
	      * An axis pointing vertically in the direction of an increasing
	      * y coordinate.
	      */
	     VERTICAL_INCREASING,
	     /**
	      * An axis pointing vertically in the direction of a decreasing
	      * y coordinate.
	      */
	     VERTICAL_DECREASING,
	     /**
	      * An axis pointing horizontally in the direction of a decreasing
	      * x coordinate.
	      */
	     HORIZONTAL_DECREASING,
	     /**
	      * An axis pointing horizontally in the direction of an increasing
	      * x coordinate.
	      */
	     HORIZONTAL_INCREASING};

	 double startX;
	 double startY;
	 Dir dir;

	 private boolean tickLabelsHorizontal = true;

	 /**
	  * Set whether or not tick labels are constrained to be horizontal.
	  * This mode has no effect unless an axis is vertical.
	  * The default is <code>false</code>.
	  * @param mode true if tick labels must be horizontal;
	  *        false if they may be vertical
	  */
	 public void setTickLabelsHorizontal(boolean mode) {
	     tickLabelsHorizontal = mode;
	 }

	 /**
	  * Determine whether or not tick labels are constrained to be
	  * horizontal.
	  * This mode has no effect unless an axis is vertical.
	  * @return true if tick labels must be horizontal;
	  *        false if they may be vertical
	  */
	 public boolean tickLabelsAreHorizontal() {
	     return tickLabelsHorizontal;
	 }

	 /**
	  * Get the direction of an axis.
	  * The direction is defined in graph coordinate space.
	  * @return the direction
	  */
	 public Dir getDir() {return dir;}

	 double length;

	 double endX;
	 double endY;
	 double start;
	 double end;
	 boolean increasing;

	 double getStart() {return start;}

	 double getEnd() {return end;}

	 /**
	  * Get the X-coordinate start location of the axis.
	  * @return the start point in graph coordinate space
	  */
	 public double getStartX() {return startX;}

	 /**
	  * Get the Y-coordinate start location of the axis.
	  * @return the start point in graph coordinate space
	  */
	 public double getStartY() {return startY;}

	 /**
	  * Get the X-coordinate end location of the axis.
	  * @return the end point in graph coordinate space
	  */
	 public double getEndX() {return endX;}

	 /**
	  * Get the X-coordinate end location of the axis.
	  * @return the end point in graph coordinate space
	  */
	 public double getEndY() {return endY;}



	 static final double DEFAULT_WIDTH = 2;
	 double width = DEFAULT_WIDTH;
	 boolean counterclockwise;

	 /**
	  * Determine the direction of tick marks.
	  * @return true of tick marks should point in a counter-clockwise
	  *         direction; false otherwise
	  */
	 public boolean isCounterClockwise() {return counterclockwise;}

	 double tickBase;
	 double tickIncr;

	 /**
	  * Get the tick base.
	  * The tick base is the value corresponding to a tick index of zero.
	  * The index for the first tick on the axis may be offset from this
	  * value.  The value is intended to be used by the class defining it.
	  * @return the tick base
	  */
	 protected double getTickBase() {return tickBase;}

	 /**
	  * Get the tick increment.
	  * The tick increment determines the distance between ticks.
	  * For Graph.Axis, it is the distance in user-coordinate
	  * space between possible tick locations.  The value is
	  * intended to be used by the class defining it.
	  * @return the tick increment
	  */
	 protected double getTickIncr() {return tickIncr;}


	 FontParms fp = null;
	 static final Color DEFAULT_COLOR = Color.BLACK;
	 Color color = DEFAULT_COLOR;

	 String label = null;

	 static final double DEFAULT_LABEL_OFFSET = 3.0;

	 double labelOffset = DEFAULT_LABEL_OFFSET;

	 /**
	  * Set the offset for the label of an axis.
	  * This is the spacing from the tick marks, including
	  * any labels for the tick marks.
	  * @param offset the offset in user-space units
	  */
	 public void setLabelOffset(double offset) {
	     labelOffset = offset;
	 }

	 
	 /**
	  * Get the offset for the label of an axis.
	  * This is the spacing from the tick marks, including
	  * any labels for the tick marks.
	  * @return the offset in user-space units
	  */
	 public double getLabelOffset() {return labelOffset;}


	 /**
	  * Set the color of the axis.
	  * @param c the color; null for the default
	  */
	 public void setColor(Color c) {
	     if (c == null) {
		 color = DEFAULT_COLOR;
	     } else {
		 color = c;
	     }
	 }
	 /**
	  * Get the color of the axis.
	  * @return the color of the axis
	  */
	 public Color getColor() {return color;}

	 /**
	  * Set the width of the axis
	  * @param width the width in user-space units
	  */
	 public void setWidth(double width) {
	     this.width = width;
	 }

	 /**
	  * Get the width of the axis
	  * @return the width in user-space units
	  */
	 public double getWidth() {return width;}

	 /**
	  * Set the label of the axis.
	  * @param label the label
	  */
	 public void setLabel(String label) {
	     this.label = label;
	 }
	 /**
	  * Get the label of an axis
	  * @return the label; null if there is none
	  */
	 public String getLabel() {return label;}


	 private long limitModulus = 0;
	 /**
	  * Set the limit modulus.
	  * Some subclasses (e.g., LogAxis) have ticks that are not
	  * uniformly spaced, and the tick can be suppressed if the
	  * index modulo the limit modulus is larger than a tick-specified
	  * limit. A value of zero indicates that the limit modulus is
	  * not used. The limit modulus is not used by this class
	  * directly, but rather by the {@link Graph.TickSpec} class.
	  * @param value the limit modulus.
	  */
	 protected void setLimitModulus(long value) {
	     limitModulus = value;
	 }

	 /**
	  * Get the limit modulus.
	  * Some subclasses (e.g., LogAxis) have ticks that are not
	  * uniformly spaced, and the tick can be suppressed if the
	  * index modulo the limit modulus is larger than a tick-specified
	  * limit.  The usage for the limit modulus is determined by
	  * subclasses - this class merely keeps track of its value.
	  * @return the limit modulus; 0 if none is set or to be used
	  */
	 public long getLimitModulus() {return limitModulus;}

	 /**
	  * Constructor given a starting point.
	  * @param start the starting point in graph coordinate space
	  * @param dir the direction of the graph
	  * @param length the length of the axis in graph coordinate space
	  * @param tickBase the starting coordinate along the axis for
	  *        graph ticks, given in graph-coordinate space
	  * @param tickIncr the increment between possible tick locations
	  *    in graph coordinate space units
	  * @param counterclockwise the angular direction to follow to
	  *        reach a graph's labels and tick marks
	  */
	 public Axis(Point2D start, Dir dir, double length,
		     double tickBase, double tickIncr,
		     boolean counterclockwise)
	 {
	     this((double)start.getX(), (double)start.getY(), dir, length,
		  tickBase, tickIncr, counterclockwise);
	 }

	 /**
	  * Constructor given a starting point's x and y coordinates.
	  * @param startX the x coordinate of the axis' starting point in
	  *        graph coordinate space
	  * @param startY the y coordinate of the axis' starting point in
	  *        graph coordinate space
	  * @param dir the direction of the graph
	  * @param length the length of the axis in graph coordinate space
	  * @param tickBase the starting coordinate along the axis for
	  *        graph ticks, given in graph-coordinate space
	  * @param tickIncr the increment between possible tick locations
	  *    in graph coordinate space units
	  * @param counterclockwise the angular direction to follow to
	  *        reach a graph's labels and tick marks
	  */
	 public Axis(double startX, double startY, Dir dir, double length,
		     double tickBase, double tickIncr,
		     boolean counterclockwise)
	 {
	     this.startX = startX;
	     this.startY = startY;
	     this.dir = dir;
	     this.length = length;
	     this.tickBase = tickBase;
	     this.counterclockwise = counterclockwise;

	     switch(dir) {
	     case VERTICAL_INCREASING:
		 endX = startX;
		 endY = startY + length;
		 start = startY;
		 increasing = true;
		 break;
	     case VERTICAL_DECREASING:
		 endX = startX;
		 endY = startY - length;
		 start = startY;
		 increasing = false;
		 tickIncr = -tickIncr;
		 break;
	     case HORIZONTAL_DECREASING:
		 endX = startX - length;
		 endY = startY;
		 start = startX;
		 increasing = false;
		 tickIncr = -tickIncr;
		 break;
	     case HORIZONTAL_INCREASING:
	     default:
		 endX = startX + length;
		 endY = startY;
		 start = startX;
		 increasing = true;
		 break;
	     }
	     end = (increasing? (start + length): (start - length));
	     this.tickIncr = tickIncr;
	 }

	 TreeSet<TickSpec> tickList = new TreeSet<TickSpec>();

	 int getNumberOfLevels() {return tickList.size();}
	 void fillTspecArray(TickSpec[] tspecs) {
	     int i = 0;
	     for (TickSpec tspec: tickList) {
		 tspecs[i++] = tspec;
	     }
	 }

	 /**
	  * Add a tick specification to a list of tick specifications.
	  * Tick specifications are ordered based on the value of the mod
	  * argument passed to their constructors, with the highest mod values
	  * appearing first.  At a given tick position, the first one for
	  * which the method showTick returns true will be used.
	  * @param tickSpec the tick specification to add
	  */
	 public void addTick(TickSpec tickSpec) {
	     tickList.add(tickSpec);
	 }

	 /**
	  * Set the font parameters for the axis.
	  * @param parms the font parameters
	  */
	 public void setFontParms(FontParms parms) {
	     fp = parms;
	 }

	 /**
	  * Get the font parameters for an axis
	  * If there are none, the font parameters for the graph itself
	  * will be used.
	  * A non-null returned value will be a copy of the current
	  * font parameters for this axis.
	  * @return the font parameters; null if none are specified
	  */
	 public FontParms getFontParms() {
	     if (fp == null) {
		 return null;
	     }
	     try {
		 return (FontParms)fp.clone();
	     } catch (CloneNotSupportedException e) {
		 // cannot happen -- already handled a null
		 return null;
	     }
	 }

	 /**
	  * Get the initial index.
	  * This is the index for the first tick that can be displayed.
	  * @return the initial index
	  */
	 public long initialIndex()
	 {
	     long ind = Math.round((start - tickBase)/tickIncr);
	     if (Math.abs((start - tickBase) / (ind * tickIncr)) < 0.9999) {
		 ind++;
	     }
	     return ind;
	 }

	 /**
	  * Modify a graphics context.
	  * This method is called when the tick marks of an axis are
	  * about to be drawn. It enables the graphics context to
	  * be modified when the index reaches particular values.
	  * The index will increase sequentially from an initial value
	  * until the end of the axis is reached, but this method will
	  * be called only when there is a tick mark to draw.
	  * <P>
	  * The tick mark is a {@link java.awt.geom.Line2D Line2D} that
	  * is drawn as a line segment with an appropriately
	  * configured stroke.
	  * <P>
	  * A typical use might be to change the color of a tick mark
	  * or to place a small icon below a tick mark if the label
	  * was suppressed. The default implementation just returns.
	  * @param g2d the graphics context
	  * @param ind the tick index
	  * @param p the point on the axis corresponding to the tick
	  *        index and expressed in user-space units
	  * @param g the graph
	  * @param tickmark the tick mark
	  */
	 public void modifyGraphics(Graphics2D g2d, long ind, Point2D p,
				    Graph g, Line2D tickmark)
	 {
	     return;
	 }

	 /**
	  * Restore a graphics context.
	  * Subclasses that override
	  * {@link Graph.Axis#modifyGraphics(Graphics2D,long,Point2D,Graph,Line2D) modifyGraphics}
	  * must implement this method so that it restores
	  * the graphics context to the state it was in just before
	  * the previous call to
	  * {@link Graph.Axis#modifyGraphics(Graphics2D,long,Point2D,Graph,Line2D) modifyGraphics}.
	  * @param g2d the graphics context to restore.
	  */
	 public void restoreGraphics(Graphics2D g2d) {
	     return;
	 }

	 /**
	  * Modify font parameters.
	  * This method is called when the tick marks of an axis are
	  * about to be drawn. It enables the font parameters to
	  * be modified when the index reaches particular values.
	  * The index will increase sequentially from an initial value
	  * until the end of the axis is reached, but this method will
	  * be called only when there is a tick mark to draw. It will
	  * have an effect on any tick-mark label that is drawn.
	  * <P>
	  * When a tick mark's label is drawn, a different graphics
	  * context is used. The argument fp can, however, be modified
	  * in various ways: to change the font color or perhaps the
	  * label's orientation.
	  * @param fp the font parameters
	  * @param ind the tick index
	  * @param p the point on the axis corresponding to the tick
	  *        index and expressed in user-space units
	  * @param g the graph
	  */
	 public void modifyFontParms(FontParms fp, long ind,
				    Point2D p, Graph g)
	 {
	     return;
	 }

	 /**
	  * Restore font parameters to what they were
	  * before the previous call to
	  * {@link Graph.Axis#modifyFontParms(Graph.FontParms,long,Point2D,Graph) modifyFontParms}.
	  * Subclasses must implement this method if
	  * {@link Graph.Axis#modifyFontParms(Graph.FontParms,long,Point2D,Graph) modifyFontParms}
	  * is implemented.
	  * @param fp the font parameters to be restored
	  */
	 public void restoreFontParms(FontParms fp) {
	     return;
	 }

	 private double axisScale = 1.0;
	 /**
	  * Set the axis scale.
	  * The axis value that {@link Graph.Axis#axisValue(long)} returns
	  * will be divided by this factor. It does not affect the
	  * value returned by {@link Graph.Axis#axisCoord(long)}.
	  * The default value is 1.0.
	  * <P>
	  * The scale factor can be used to change units.  For example,
	  * if a graph was configured so that the X axis shows distances
	  * in meters, setting this factor to 10<sup>3</sup> will allow
	  * the axis to label tick marks in units of kilometers.
	  * @param scaleFactor the scaleFactor
	  * @exception IllegalArgumentException if the scale factor is
	  *            not a positive double-precision number
	  */
	 public void setAxisScale(double scaleFactor)
	     throws IllegalArgumentException
	 {
	     if (scaleFactor < Double.MIN_NORMAL) {
		 String msg = errorMsg("scaleFactor", scaleFactor);
		 throw new IllegalArgumentException(msg);
	     }
	     axisScale = scaleFactor;
	 }

	 /**
	  * Get the axis scale.
	  * @return the value for the axis scale
	  * @see Graph.Axis#setAxisScale(double)
	  */
	 public double getAxisScale() {
	     return axisScale;
	 }

	 /**
	  * Get the value for a point on an axis for which there may be a tick.
	  * The value is the one used to print a label for ticks that label
	  * their coordinates.
	  * @param ind the index
	  * @return the value corresponding to the index
	  */
	 public double axisValue(long ind) {
	     double prescaled = tickBase + ind * tickIncr;
	     return (axisScale == 1.0)? prescaled: prescaled/axisScale;
	 }

	 /**
	  * Get the coordinate value for a point on an axis for which there
	  * may be a tick.
	  * This will determine the x or y coordinate at which a tick mark
	  * may appear.
	  * @param ind the index
	  * @return the value in graph coordinate space corresponding
	  *         to the index
	  */
	 public  double axisCoord(long ind) {
	     return tickBase + ind * tickIncr;
	 }

	 /**
	  * Determine if iteration over ticks is not complete.
	  * This is called at the start the loop in a 'while' statement.
	  * @param sc the current location in graph coordinate space
	  * @return true if iteration should continue; false otherwise
	  */
	 public boolean notDone(double sc) {
	     return increasing? (sc <= end): (sc >= end);
	 }



     }
    
     /**
      * Class to provide a logarithmic axis.
      * To produce logarithmic plots, the logarithm to base 10 of
      * values must be used as the graph coordinate space coordinates.
      * This can be done for the x coordinate, the y coordinate, or
      * both.  Thus, the coordinates for the values 1, 10.0, and 100.0
      * are 0.0, 1.0, and 2.0 respectively.
      * <P>
      * Creating a logarithmic axis requires the user to provide the
      * following fields in a constructor:
      * <UL>
      *   <LI> the X coordinate in graph coordinate space for the start
      *        of an axis (which will be the logarithm to base 10 of a
      *        value when X uses a logarithmic scale).
      *   <LI> the Y coordinate in graph coordinate space for the start
      *        of an axis (which will be the logarithm to base 10 of a
      *        value when Y uses a logarithmic scale).
      *   <LI> The direction.
      *   <LI> The length of the axis in graph coordinate space units
      *        (this will be the absolute value of the difference
      *        of the logarithms to base 10 of the starting and ending value
      *        for the axis when a logarithmic scale is used for the
      *        corresponding coordinate).
      *   <LI> The tick increment. This determines the number of possible
      *        ticks per decade, represented by the decade [1.0, 10.0].
      *        For whole numbers in this decade, there are 9 possible
      *        tick locations past the initial point as the value of the
      *        initial point is 1.0. By contrast, for a linear axis,
      *        there are 10 locations. The difference in the count
      *        is due to the linear case starting at 0.0 while the
      *        logarithmic case starts at 1.0. Valid values for the
      *        tick increment are:
      *        <UL>
      *           <LI> 9.0.  In this case, ticks occur at at powers of
      *                10.
      *           <LI> 1.0 divided by a positive integer m. In this case,
      *                when n is an integer in the range [1, 10], for a
      *                power of 10 multiplied by n, there will be multiple
      *                ticks between successive values when m is larger
      *                than 1.0.  When m is 1.0, ticks occur at at powers of
      *                10 multiplied by 2, 3, 4, 5, 6, 7, 8, 9, 10.
      *        </UL>
      *   <LI> counterclockwise. This is a boolean value. When true,
      *        tick marks on the side of the axis corresponding to a
      *        counterclockwise rotation; when false, on the side
      *        corresponding to a clockwise rotation.
      * </UL>
      * Note that the constructor does not include a tick base: this
      * field is computed and passed to the constructor for the
      * superclass of this class. Setting up tick marks is discussed
      * in the documentation for {@link Graph.TickSpec}.
      * <P>
      * The methods {@link Graph.Axis#setWidth(double)},
      * {@link Graph.Axis#setColor(Color)}, {@link Graph.Axis#setLabel(String)},
      * {@link Graph.Axis#setLabelOffset(double)},
      * {@link Graph.Axis#setFontParms(Graph.FontParms)}, and
      * {@link Graph.Axis#addTick(Graph.TickSpec)}
      * can be used as described in the documentation for the class
      * {@link Graph.Axis}.
      * For details on how to add ticks.
      */
     public static class LogAxis extends Axis {
	 double ourStart = 1.0;

	 static String errorMsg(String key, Object... args) {
	     return Graph.errorMsg(key, args);
	 }

	 static double  getLogAxisTickBase(Dir dir,
					   double logStartX,
					   double logStartY,
					   double logLength)
	 {
	     switch(dir) {
	     case VERTICAL_INCREASING:
		 return Math.floor(logStartY);
	     case VERTICAL_DECREASING:
		 return Math.floor(logStartY - logLength);
	     case HORIZONTAL_DECREASING:
		 return Math.floor(logStartX - logLength);
	     case HORIZONTAL_INCREASING:
	     default:
		 return  Math.floor(logStartX);
	     }
	 }

	 static Dir getLogAxisDir(Dir dir) {
	     switch (dir) {
	     case VERTICAL_DECREASING: return Dir.VERTICAL_INCREASING;
	     case HORIZONTAL_DECREASING: return Dir.HORIZONTAL_INCREASING;
	     }
	     return dir;
	 }

	 static double getLogAxisStartX(Dir dir, double logStartX,
					double logLength)
	 {
	     if (dir == Dir.HORIZONTAL_DECREASING)
		 return (logStartX - logLength);
	     return logStartX;
	 }

	 static double getLogAxisStartY(Dir dir, double logStartY,
					double logLength)
	 {
	     if (dir == Dir.VERTICAL_DECREASING)
		 return (logStartY - logLength);
	     return logStartY;
	 }
	 static boolean getLogAxisCounterClockwise(Dir dir,
						   boolean counterclockwise)
	 {
	     if (dir == Dir.VERTICAL_DECREASING
		 || dir == Dir.HORIZONTAL_DECREASING)
		 return !counterclockwise;
	     return counterclockwise;
	 }

	 private double[] tickLocations;
	 private long limitModulus = 0;
	 private double base;
	 private double start;

	 /**
	  * Constructor.
	  * @param logStartX the starting X position in graph coordinate space
	  * @param logStartY the starting Y position in graph coordinate space
	  * @param dir the direction of the axis
	  * @param logLength the length of the axis in graph coordinate space
	  * @param tickIncr the tick increment before a logarithm to base 10
	  *        is computed, for the range [1.0, 10.0], with legal values
	  *        of either 9.0 or 1.0/n, where n is a positive integer
	  * @param counterclockwise the angular direction to follow to
	  *        reach a graph's labels and tick marks
	  * @exception IllegalArgumentException an input argument was not valid,
	  *            most likely the tickIncr
	  */
	 public LogAxis(double logStartX, double logStartY, Dir dir,
			double logLength, double tickIncr,
			boolean counterclockwise)
	     throws IllegalArgumentException
	 {
	     super(getLogAxisStartX(dir, logStartX, logLength),
		   getLogAxisStartY(dir, logStartY, logLength),
		   getLogAxisDir(dir),
		   logLength,
		   getLogAxisTickBase(dir, logStartX, logStartY, logLength),
		   tickIncr,
		   getLogAxisCounterClockwise(dir, counterclockwise));

	     base = Math.pow(10.0, getTickBase());
	     start = Math.pow(10.0, getStart());

	     if (tickIncr != 0.0) {
		 limitModulus = Math.round(9.0/tickIncr);
		 if (limitModulus != 1 && limitModulus % 9 != 0) {
		     throw new IllegalArgumentException
			 (errorMsg("logAxisTickIncr", tickIncr));
		 }
		 tickLocations = new double[(int)limitModulus];
		 setLimitModulus(limitModulus);
		 for (int i = 0; i < limitModulus; i++) {
		     tickLocations[i] = Math.log10(1.0 + i * tickIncr);
		 }
	     }
	 }

	 double graphStart = 0.0;
	 double currentTickIncr = 0.0;
	 double currentTickBase = 0.0;

	 @Override
	 public long initialIndex() {
	     // base + base * index*  tickIncr = start
	     long index = Math.round((start - base)/(base * tickIncr));
	     if (((start - base) / (index * base * tickIncr)) < 0.9999) {
		 index++;
	     }
	     return index;

	 }
	 
	 @Override
	 public double axisValue(long ind) {
	     double prescaled = Math.pow(10, axisCoord(ind));
	     double scale = getAxisScale();
	     return (scale == 1.0)? prescaled: prescaled/scale;

	 }

	 @Override
	 public double axisCoord(long ind) {
	     return getTickBase() + (double) (ind/limitModulus)
		 + tickLocations[(int)(ind % limitModulus)];
	 }
     }

     /**
      * Draw an axis.
      * @param axis the axis to draw
      */
     public void draw(Axis axis) {
	 Point2D startPoint =
	     coordTransform(axis.getStartX(), axis.getStartY());
	 Point2D endPoint = coordTransform(axis.getEndX(), axis.getEndY());
	 FontParms fp = axis.getFontParms();
	 if (fp == null) fp = getFontParms();
	 double xloffset = 0.0;
	 double yloffset = 0.0;
	 Graphics2D g2d = createGraphics();
	 try {
	     g2d.setColor(axis.getColor());
	     g2d.setStroke(new BasicStroke((float)axis.getWidth(),
					   BasicStroke.CAP_SQUARE,
					   BasicStroke.JOIN_MITER));
	     int levels = axis.getNumberOfLevels();
	     fp.setJustification(Just.CENTER);
	     if (levels > 0 && axis.tickIncr != 0.0) {
		 TickSpec[] tspecs = new TickSpec[levels];
		 // axis.tickList.toArray(tspecs);
		 axis.fillTspecArray(tspecs);
		 /*
		   for (TickSpec tspec: tspecs) {
		   System.out.println(tspec.mod +", " + tspec.length
		   + ", " + tspec.width
		   + ", " + tspec.modtest);
		   }
		 */
		 long ind = axis.initialIndex();
		 double s = axis.axisValue(ind);
		 double sc = axis.axisCoord(ind);
		 // double end = axis.getEnd();
		 while (axis.notDone(sc)) {
		     for (TickSpec tspec: tspecs) {
			 if (tspec.showTick(s, sc, axis, ind)) {
			     double tickLength = tspec.length * axis.getWidth();
			     double tickWidth = tspec.width * axis.getWidth();
			     double soffset = tspec.stringOffset;
			     String string = null;
			     if (tspec.showTickLabel(s, sc, axis, ind)) {
				 // Trim the string in case a format produces
				 // leading or trailing whitespace.
				 string = tspec.getTickLabel(s, sc, axis, ind)
				     .trim();
			     }
			     /*
			       System.out.println("printing tick at " + s
			       + ", width = " + tspec.width);
			     */
			     Axis.Dir direction = axis.getDir();
			     boolean vert;
			     Point2D p;
			     switch (direction) {
			     case VERTICAL_INCREASING:
			     case VERTICAL_DECREASING:
				 vert = true;
				 p = coordTransform(axis.getStartX(), sc);
				 break;
			     case HORIZONTAL_INCREASING:
			     case HORIZONTAL_DECREASING:
				 // need default to suppress a bogus compiler
				 // warning.
			     default:
				 p = coordTransform(sc, axis.getStartY());
				 vert = false;
				 break;
			     }
			     double x1, y1, x2, y2, xs, ys;
			     FontRenderContext frc = g2d.getFontRenderContext();
			     double strSpacing = 0.0;
			     boolean tickLabelsHorizontal =
				 axis.tickLabelsAreHorizontal();
			     if (string != null) {
				 AffineTransform savedt = vert? g2d.getTransform():
				     null;
				 if (vert && !tickLabelsHorizontal) {
				     g2d.rotate(-90.0);
				 }
				 if (vert && tickLabelsHorizontal) {
				     strSpacing =
					 g2d.getFontMetrics(fp.font)
					 .getStringBounds(string, g2d).getWidth();
				 } else {
				     LineMetrics metrics =
					 fp.font.getLineMetrics(string, frc);
				     strSpacing = metrics.getAscent()
					 + metrics.getDescent();
				 }
				 if (vert && !tickLabelsHorizontal) {
				     g2d.setTransform(savedt);
				 }
			     }
			     switch (direction) {
			     case VERTICAL_INCREASING:
				 if (axis.isCounterClockwise()) {
				     x1 = p.getX() -
					 Math.floor(axis.getWidth()/2.0);
				     y1 = p.getY();
				     x2 = x1 - tickLength;
				     y2 = y1;
				     xs = x2 - tspec.stringOffset;
				     ys = y2;
				     if (tickLabelsHorizontal) {
					 fp.setBaseline(BLineP.CENTER);
					 fp.setJustification(Just.RIGHT);
				     } else {
					 fp.setBaseline(BLineP.BOTTOM);
					 fp.setAngle(90.0);
				     }
				     double lo = xs - p.getX() - strSpacing;
				     if (lo < xloffset) xloffset = lo;
				 } else {
				     x1 = p.getX() + axis.getWidth()/2.0;
				     y1 = p.getY();
				     x2 = x1 + tickLength;
				     y2 = y1;
				     xs = x2 + tspec.stringOffset;
				     ys = y2;
				     if (tickLabelsHorizontal) {
					 fp.setBaseline(BLineP.CENTER);
					 fp.setJustification(Just.LEFT);
				     } else {
					 fp.setBaseline(BLineP.TOP);
					 fp.setAngle(90.0);
				     }
				     double lo = xs - p.getX() + strSpacing;
				     if (lo > xloffset) xloffset = lo;
				 }
				 break;
			     case VERTICAL_DECREASING:
				 if (axis.isCounterClockwise()) {
				     x1 = p.getX() + axis.getWidth()/2.0;
				     y1 = p.getY();
				     x2 = x1 + tickLength;
				     y2 = y1;
				     xs = x2 + tspec.stringOffset;
				     ys = y2;
				     if (tickLabelsHorizontal) {
					 fp.setBaseline(BLineP.CENTER);
					 fp.setJustification(Just.LEFT);
				     } else {
					 fp.setBaseline(BLineP.TOP);
					 fp.setAngle(90.0);
				     }
				     double lo = xs - p.getX() + strSpacing;
				     if (lo > xloffset) xloffset = lo;
				 } else {
				     x1 = p.getX() -
					 Math.floor(axis.getWidth()/2.0);
				     y1 = p.getY();
				     x2 = x1 - tickLength;
				     y2 = y1;
				     xs = x2 - tspec.stringOffset;
				     ys = y2;
				     if (tickLabelsHorizontal) {
					 fp.setBaseline(BLineP.CENTER);
					 fp.setJustification(Just.RIGHT);
				     } else {
					 fp.setBaseline(BLineP.BOTTOM);
					 fp.setAngle(90.0);
				     }
				     double lo = xs - p.getX() - strSpacing;
				     if (lo < xloffset) xloffset = lo;
				 }
				 break;
			     case HORIZONTAL_DECREASING:
				 fp.setJustification(Just.CENTER);
				 if (axis.isCounterClockwise()) {
				     x1 = p.getX();
				     y1 = p.getY() + axis.getWidth()/2.0;
				     x2 = x1;
				     y2 = y1 + tickLength;
				     xs = x2;
				     ys = y2 + tspec.stringOffset;
				     fp.setBaseline(BLineP.TOP);
				     fp.setAngle(0.0);
				     double lo = ys - p.getY() + strSpacing;
				     if (lo > yloffset) yloffset = lo;
				 } else {
				     x1 = p.getX();
				     y1 = p.getY()
					 - Math.floor(axis.getWidth()/2.0);
				     x2 = x1;
				     y2 = y1 - tickLength;
				     xs = x2;
				     ys = y2 - tspec.stringOffset;
				     fp.setBaseline(BLineP.BOTTOM);
				     fp.setAngle(0.0);
				     double lo = ys - p.getY() - strSpacing;
				     if (lo < yloffset) yloffset = lo;
				 }
				 break;
			     case HORIZONTAL_INCREASING:
				 // need default to suppress a bogus compiler
				 // warning.
			     default:
				 fp.setJustification(Just.CENTER);
				 if (axis.isCounterClockwise()) {
				     x1 = p.getX();
				     y1 = p.getY() -
					 Math.floor(axis.getWidth()/2.0);
				     x2 = x1;
				     y2 = y1 - tickLength;
				     xs = x2;
				     ys = y2 - tspec.stringOffset;
				     fp.setBaseline(BLineP.BOTTOM);
				     fp.setAngle(0.0);
				     double lo = ys - p.getY() - strSpacing;
				     if (lo < yloffset) yloffset = lo;
				 } else {
				     x1 = p.getX();
				     y1 = p.getY() + axis.getWidth()/2.0;
				     x2 = x1;
				     y2 = y1 + tickLength;
				     xs = x2;
				     ys = y2 + tspec.stringOffset;
				     fp.setBaseline(BLineP.TOP);
				     fp.setAngle(0.0);
				     double lo = ys - p.getY() + strSpacing;
				     if (lo > yloffset) yloffset = lo;
				 }
				 break;
			     }
			     g2d.setStroke(new BasicStroke
					   ((float)tickWidth,
					    BasicStroke.CAP_BUTT,
					    BasicStroke.JOIN_MITER));
			     Line2D tickmark = new Line2D.Double(x1, y1,
								 x2, y2);
			     // The defaults for these two methods just return.
			     // Some subclasses may override these.
			     try {
				 axis.modifyGraphics(g2d, ind, p,
						     this, tickmark);
				 g2d.draw(tickmark);
			     } finally {
				 axis.restoreGraphics(g2d);
			     }
			     if (string != null) {
				 try {
				     axis.modifyFontParms(fp, ind, p, this);
				     Point2D ip = invCoordTransform(xs, ys);
				     xs = ip.getX();
				     ys = ip.getY();
				     drawString(string, xs, ys, fp);
				 } finally {
				     axis.restoreFontParms(fp);
				 }
			     }
			     break;
			 }
		     }
		     // s = axis.tickBase + (++ind) * axis.tickIncr;
		     ind++;
		     s = axis.axisValue(ind);
		     sc = axis.axisCoord(ind);
		 }
	     }
	 } finally {
	     // reset g2d in case something changed
	     g2d.dispose();
	 }
	 g2d = createGraphics();
	 try {
	     g2d.setColor(axis.getColor());
	     g2d.setStroke(new BasicStroke((float)axis.getWidth(),
					   BasicStroke.CAP_SQUARE,
					   BasicStroke.JOIN_MITER));
	     g2d.draw(new Line2D.Double(startPoint, endPoint));
	 } finally {
	     g2d.dispose();
	 }

	 // similarly, reset fp in case something changed
	 fp = axis.getFontParms();
	 if (fp == null) fp = getFontParms();
	 fp.setJustification(Just.CENTER);
	 if (axis.getLabel() != null) {
	     switch(axis.getDir()) {
	     case VERTICAL_INCREASING:
		 if (axis.isCounterClockwise()) {
		     fp.setBaseline(BLineP.BOTTOM);
		     fp.setAngle(90.0);
		     xloffset -= axis.getLabelOffset();
		 } else {
		     fp.setBaseline(BLineP.TOP);
		     fp.setAngle(90.0);
		     xloffset += axis.getLabelOffset();
		 }
		 break;
	     case VERTICAL_DECREASING:
		 if (axis.isCounterClockwise()) {
		     fp.setBaseline(BLineP.TOP);
		     fp.setAngle(90.0);
		     xloffset += axis.getLabelOffset();
		 } else {
		     fp.setBaseline(BLineP.BOTTOM);
		     fp.setAngle(90.0);
		     xloffset -= axis.getLabelOffset();
		 }
		 break;
	     case HORIZONTAL_DECREASING:
		 if (axis.isCounterClockwise()) {
		     fp.setBaseline(BLineP.TOP);
		     fp.setAngle(0.0);
		     yloffset += axis.getLabelOffset();
		 } else {
		     fp.setBaseline(BLineP.BOTTOM);
		     fp.setAngle(0.0);
		     yloffset -= axis.getLabelOffset();
		 }
		 break;
	     case HORIZONTAL_INCREASING:
		 if (axis.isCounterClockwise()) {
		     fp.setBaseline(BLineP.BOTTOM);
		     fp.setAngle(0.0);
		     yloffset -= axis.getLabelOffset();
		 } else {
		     fp.setBaseline(BLineP.TOP);
		     fp.setAngle(0.0);
		     yloffset += axis.getLabelOffset();
		 }
		 break;
	     }
	     drawString(axis.label,
			((axis.getStartX() + axis.getEndX())/2.0 
			 + xloffset/xscaleSigned),
			((axis.getStartY() + axis.getEndY())/2.0
			 - yloffset/yscaleSigned),
			fp);
	 }
     }

     private int xLowerOffset = 0;
     private int yLowerOffset = 0;
     private int xUpperOffset = 0;
     private int yUpperOffset = 0;

     double xLower;
     double yLower;
     double xUpper;
     double yUpper;

     /**
      * Get the x coordinate for the lower end of the graph's range.
      * @return the x coordinate lower end of the graph's range in
      *         graph coordinate space
      */
     public double getXLower() {return xLower;}
     /**
      * Get the y coordinate for the lower end of the graph's range.
      * @return the y coordinate lower end of the graph's range in
      *         graph coordinate space
      */
     public double getYLower() {return yLower;}
     /**
      * Get the x coordinate for the upper end of the graph's range.
      * @return the x coordinate upper end of the graph's range in
      *         graph coordinate space
      */
     public double getXUpper() {return xUpper;}
     /**
      * Get the y coordinate for the upper end of the graph's range.
      * @return the y coordinate upper end of the graph's range in
      *         graph coordinate space
      */
     public double getYUpper() {return yUpper;}

     /**
      * Set the graph offsets symmetrically.
      * @param x the x offset in user space
      * @param y the y offset in user space
      */
     public void setOffsets(int x, int y) {
	 setOffsets(x, x, y, y);
     }

     /**
      * Set the graph offsets.
      * @param xL the lower x offset in user space
      * @param yL the lower y offset in user space
      * @param xU the upper x offset in user space
      * @param yU the upper y offset in user space
      */
     public void setOffsets(int xL, int xU, int yL, int yU) {
	 coordAF = null;
	 graphBoundingBoxGCS = null;
	 invCoordAF = null;
	 xLowerOffset = xL;
	 xUpperOffset = xU;
	 yLowerOffset = yL;
	 yUpperOffset = yU;
     }

     /**
      * Get the lower X offset for this graph.
      * Lower and upper refer to the left and right offsets respectively.
      * @return the lower X offset in user-space units
      */
     public int getXLowerOffset() {
	 return xLowerOffset;
     }

     /**
      * Get the upper X offset for this graph.
      * Lower and Upper refer to the left and right offsets respectively.
      * @return the upper X offset in user-space units
      */
     public int getXUpperOffset() {
	 return xUpperOffset;
     }

     /**
      * Get the lower Y offset for this graph.
      * Lower and upper refer to the top and bottom offsets respectively.
      * @return the lower X offset in user-space units
      */
     public int getYLowerOffset() {
	 return yLowerOffset;
     }

     /**
      * Get the upper Y offset for this graph.
      * Lower and upper refer to the top and bottom offsets respectively.
      * @return the upper Y offset in user-space units
      */
     public int getYUpperOffset() {
	 return yUpperOffset;
     }

     /**
      * Set the x and y ranges.
      * All values are in graph coordinate space.
      * @param xLower the lower value of the range in the x direction
      * @param xUpper the upper value of the range in the x direction
      * @param yLower the lower value of the range in the y direction
      * @param yUpper the upper value of the range in the y direction
      */
     public void setRanges(double xLower, double xUpper,
			   double yLower, double yUpper)
     {
	 coordAF = null;
	 graphBoundingBoxGCS = null;
	 invCoordAF = null;
	 this.xLower = xLower;
	 this.xUpper = xUpper;
	 this.yLower = yLower;
	 this.yUpper = yUpper;
	 parity =  (int)Math.signum((xUpper - xLower)*(yUpper-yLower));
     }

     /**
      * Set ranges based on scale factors.
      * A scale factor is the ratio of a distance in user space to a
      * distance in graph coordinate space. If angles are to be
      * preserved, the scale factors for the X and Y directions must be
      * equal. The scale factors are the numbers by which a coordinate
      * difference in graph coordinate space along the X
      * or Y directions respectively must be multiplied to obtain the
      * corresponding difference in user space.
      * <P>
      * Note: if {@link #setOffsets(int,int)} or
      * {@link #setOffsets(int,int,int,int)} will be called,
      * these methods should be called before this method is called.
      * @param xgcs the x coordinate of a point in graph coordinate
      *        space that will be positioned at a specified location on
      *        the graph
      * @param ygcs the y coordinate of a point in graph coordinate
      *        space  that will be positioned at a specified location on
      *        the graph
      * @param xf the fractional distance from the graph's left offset to its
      *        right offset at which the point xgcs in graph coordinate space
      *        appears
      * @param yf the fractional distance from the graph's lower offset to its
      *        upper offset at which the point ygcs in graph coordinate space
      *        appears
      * @param scaleFactorX the scale factor for the X direction
      * @param scaleFactorY the scale factor for the Y direction
      */
     public void setRanges(double xgcs, double ygcs, double xf, double yf,
			   double scaleFactorX, double scaleFactorY) {
	 double dwidthWithOffset = dwidth - (xLowerOffset + xUpperOffset);
	 double dheightWithOffset = dheight - (yLowerOffset + yUpperOffset);

	 double xus = xf * dwidthWithOffset;
	 double yus = yf * dheightWithOffset;

	 // double fx = xus/dwidthWithOffset;
	 // double fy = yus/dheightWithOffset;
	 double minx = xgcs - xus / scaleFactorX ;
	 double maxx = xgcs + (dwidthWithOffset-xus) / scaleFactorX;
	 double miny = ygcs - yus / scaleFactorY;
	 double maxy = ygcs + (dheightWithOffset-yus) / scaleFactorY;
	 setRanges(minx, maxx, miny, maxy);
     }

     /**
      * Get the x coordinate for the lower end of the graph's range given
      * specific parameters.
      * @param xgcs the x coordinate of a point in graph coordinate
      *        space that will be positioned at a specified location on
      *        the graph
      * @param xf the fractional distance from the graph's left offset to its
      *        right offset at which the point xgcs in graph coordinate space
      *        appears
      * @param scaleFactorX the scale factor for the X direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @return the lower value of the x coordinate in graph coordinate space
      *         of the graph's range
      */
     public double getXLower(double xgcs, double xf, double scaleFactorX)
     {
	 double dwidthWithOffset = dwidth - (xLowerOffset + xUpperOffset);
	 double xus = xf * dwidthWithOffset;
	 return  xgcs - xus / scaleFactorX ;
     }

     /**
      * Get the x coordinate for the upper end of the graph's range given
      * specific parameters.
      * @param xgcs the x coordinate of a point in graph coordinate
      *        space that will be positioned at a specified location on
      *        the graph
      * @param xf the fractional distance from the graph's left offset to its
      *        right offset at which the point xgcs in graph coordinate space
      *        appears
      * @param scaleFactorX the scale factor for the X direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @return the upper value of the x coordinate in graph coordinate space
      *         of the graph's range
      */
     public double getXUpper(double xgcs, double xf, double scaleFactorX) {
	 double dwidthWithOffset = dwidth - (xLowerOffset + xUpperOffset);
	 double xus = xf * dwidthWithOffset;
	 return xgcs + (dwidthWithOffset-xus) / scaleFactorX;
     }

     /**
      * Get the y coordinate for the lower end of the graph's range given
      * specific parameters.
      * @param ygcs the y coordinate of a point in graph coordinate
      *        space  that will be positioned at a specified location on
      *        the graph
      * @param yf the fractional distance from the graph's lower offset to its
      *        upper offset at which the point ygcs in graph coordinate space
      *        appears
      * @param scaleFactorY the scale factor for the Y direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @return the lower value of the y coordinate in graph coordinate space
      *         of the graph's range
      */
     public double getYLower(double ygcs, double yf, double scaleFactorY) {
	 double dheightWithOffset = dheight - (yLowerOffset + yUpperOffset);
	 double yus = yf * dheightWithOffset;
	 return ygcs - yus / scaleFactorY;
     }

     /**
      * Get the y coordinate for the upper end of the graph's range given
      * specific parameters.
      * @param ygcs the y coordinate of a point in graph coordinate
      *        space  that will be positioned at a specified location on
      *        the graph
      * @param yf the fractional distance from the graph's lower offset to its
      *        upper offset at which the point ygcs in graph coordinate space
      *        appears
      * @param scaleFactorY the scale factor for the Y direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @return the upper value of the y coordinate in graph coordinate space
      *         of the graph's range
      */
     public double getYUpper(double ygcs, double yf, double scaleFactorY) {
	 double dheightWithOffset = dheight - (yLowerOffset + yUpperOffset);
	 double yus = yf * dheightWithOffset;
	 return ygcs + (dheightWithOffset-yus) / scaleFactorY;
     }

     /**
      * Get the x coordinate for the lower end of the graph's range given
      * specific parameters and graph dimensions.
      * This method is useful for applications that have to determine
      * the lower X value before the graph is actually created.
      * @param width the width of the graph
      * @param xgcs the x coordinate of a point in graph coordinate
      *        space that will be positioned at a specified location on
      *        the graph
      * @param xf the fractional distance from the graph's left offset to its
      *        right offset at which the point xgcs in graph coordinate space
      *        appears
      * @param scaleFactorX the scale factor for the X direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @param xLowerOffset the X offset for the left edge of the graph
      * @param xUpperOffset the X offset for the right edge of the graph
      * @return the lower value of the x coordinate in graph coordinate space
      *         of the graph's range
      */
     public static double getXLower(double width,
				    double xgcs, double xf,
				    double scaleFactorX,
				    int xLowerOffset, int xUpperOffset)
     {
	 double widthWithOffset = width - (xLowerOffset + xUpperOffset);
	 double xus = xf * widthWithOffset;
	 return  xgcs - xus / scaleFactorX ;
     }

     /**
      * Get the x coordinate for the upper end of the graph's range given
      * specific parameters and graph dimensions.
      * This method is useful for applications that have to determine
      * the upper X value before the graph is actually created.
      * @param width the width of the graph
      * @param xgcs the x coordinate of a point in graph coordinate
      *        space that will be positioned at a specified location on
      *        the graph
      * @param xf the fractional distance from the graph's left offset to its
      *        right offset at which the point xgcs in graph coordinate space
      *        appears
      * @param scaleFactorX the scale factor for the X direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @param xLowerOffset the X offset for the left edge of the graph
      * @param xUpperOffset the X offset for the right edge of the graph
      * @return the upper value of the x coordinate in graph coordinate space
      *         of the graph's range
      */
     public static double getXUpper(double width,
				    double xgcs, double xf,
				    double scaleFactorX,
				    int xLowerOffset, int xUpperOffset)
     {
	 double widthWithOffset = width - (xLowerOffset + xUpperOffset);
	 double xus = xf * widthWithOffset;
	 return xgcs + (widthWithOffset-xus) / scaleFactorX;
     }

     /**
      * Get the y coordinate for the lower end of the graph's range given
      * specific parameters and graph dimensions.
      * This method is useful for applications that have to determine
      * the lower Y value before the graph is actually created.
      * @param height the height of the graph
      * @param ygcs the y coordinate of a point in graph coordinate
      *        space  that will be positioned at a specified location on
      *        the graph
      * @param yf the fractional distance from the graph's lower offset to its
      *        upper offset at which the point ygcs in graph coordinate space
      *        appears
      * @param scaleFactorY the scale factor for the Y direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @param yLowerOffset the Y offset for the lower edge of the graph
      * @param yUpperOffset the Y offset for the upper edge of the graph
      * @return the lower value of the y coordinate in graph coordinate space
      *         of the graph's range
      */
     public static double getYLower(double height,
				    double ygcs, double yf,
				    double scaleFactorY,
				    int yLowerOffset, int yUpperOffset)
     {
	 double heightWithOffset = height - (yLowerOffset + yUpperOffset);
	 double yus = yf * heightWithOffset;
	 return ygcs - yus / scaleFactorY;
     }

     /**
      * Get the y coordinate for the upper end of the graph's range given
      * specific parameters and graph dimensions.
      * This method is useful for applications that have to determine
      * the upper Y value before the graph is actually created.
      * @param height the height of the graph
      * @param ygcs the y coordinate of a point in graph coordinate
      *        space  that will be positioned at a specified location on
      *        the graph
      * @param yf the fractional distance from the graph's lower offset to its
      *        upper offset at which the point ygcs in graph coordinate space
      *        appears
      * @param scaleFactorY the scale factor for the Y direction (multiplying
      *        a distance in graph coordinate space by a scale factor yields
      *        the corresponding distance in user space)
      * @param yLowerOffset the Y offset for the lower edge of the graph
      * @param yUpperOffset the Y offset for the upper edge of the graph
      * @return the upper value of the y coordinate in graph coordinate space
      *         of the graph's range
      */
     public static double getYUpper(double height,
				    double ygcs, double yf,
				    double scaleFactorY,
				    int yLowerOffset, int yUpperOffset)
     {
	 double heightWithOffset = height - (yLowerOffset + yUpperOffset);
	 double yus = yf * heightWithOffset;
	 return ygcs + (heightWithOffset-yus) / scaleFactorY;
     }


     private double xGraphRotationAnchor = 0.0;
     private double yGraphRotationAnchor = 0.0;
     private double graphRotationAngle = 0.0;

     /**
      * Specify how to rotate a graph about an anchor point expressed
      * in graph coordinate space.
      * This method sets parameters for a rotation.
      * After scaling in the x and y directions, the rotation will keep
      * the anchor point at the same position and rotate the graph
      * about that point.  Typically the anchor point will be a point
      * that is visible on the graph.  A positive value of the angle
      * theta will rotate the graph frame counterclockwise, so objects
      * displayed will appear to have been rotated clockwise.
      * <P>
      * This method will rarely be used directly as it was added to
      * allow the {@link org.bzdev.anim2d.GraphView} class to support
      * rotations.
      * @param theta the rotation angle in radians
      * @param xAnchor the X coordinate in graph coordinate space of the
      *        anchor point
      * @param yAnchor the Y coordinate in graph coordinate space of the anchor
      *        anchor point
      */
     public void setRotation(double theta, double xAnchor, double yAnchor) {
	 coordAF = null;
	 graphBoundingBoxGCS = null;
	 invCoordAF = null;
	 graphRotationAngle = theta;
	 xGraphRotationAnchor = xAnchor;
	 yGraphRotationAnchor = yAnchor;
     }

     private double xscale, yscale;

     /**
      * Get the scale factor in the x direction.
      * The scale factor is multiplied by a difference of x coordinates
      * in graph coordinate space to obtain the difference in user space.
      * The value returned is unsigned (i.e., always non-negative).
      * To determine the sign, use the method {@link #xAxisPointsRight()},
      * which returns <CODE>true</CODE> when the positive X axis points
      * right in graph coordinate space (the normal orientation)..
      * @return the scale factor
      */
     public double getXScale() {
	 if (coordAF == null) createTransforms();
	 return xscale;
     }

     /**
      * Get the scale factor in the y direction.
      * The scale factor is multiplied by a difference of y coordinates
      * in graph coordinate space to obtain the difference in user space.
      * The value returned is unsigned (i.e., always non-negative).
      * To determine the sign, use the method {@link #yAxisPointsDown()},
      * which returns <CODE>false</CODE> when the positive Y axis points
      * up in graph coordinate space (the normal orientation).
      * @return the scale factor
      */
     public double getYScale() {
	 if (coordAF == null) createTransforms();
	 return yscale;
     }

     /*
      * These are used by draw string for the label for an axis. They are
      * positive when the starting X or Y coordinate is less than the
      * ending X or Y coordinate respectively; negative otherwise.
      */
     private double xscaleSigned;
     private double yscaleSigned;

     /**
      * Determine direction of positive X axis in graph coordinate space.
      * @return true if the positive X axis points right in user space;
      *         false otherwise
      */
     public boolean xAxisPointsRight() {
	 if (coordAF == null) createTransforms();
	 return xscaleSigned >= 0.0;
     }

     /**
      * Determine direction of positive Y axis in graph coordinate space.
      * @return true if the positive Y axis points down in user space;
      *         false otherwise
      */
     public boolean yAxisPointsDown() {
	 if (coordAF == null) createTransforms();
	 return yscaleSigned >= 0.0;
     }

     int parity = 0;
     /**
      * Get the parity for this graph.
      * A parity of zero indicates that the parity cannot yet be determined
      * because setRanges was not yet called or was configured so upper and
      * lower values in either the X or Y direction are identical.  Otherwise
      * a parity of 1.0 depends on the direction in which the positive X and
      * positive Y axes in graph coordinate space point in user space:
      * <UL>
      *    <LI> If the X axis points right and the Y axis points up,
      *        the parity is 1.
      *    <LI> If the X axis points left and the Y axis points down,
      *        the parity is 1.
      *    <LI> If the X axis points right and the Y axis points down,
      *        the parity is -1.
      *    <LI> If the X axis points left and the Y axis points up,
      *        the parity is -1.
      * </UL>
      * A parity of 1 indicates that images rotated counterclockwise in
      * user space are also rotated counterclockwise in graph coordinate
      * space. A parity of -1 indicates that these rotations are in
      * opposite directions.
      * @return the current parity
      */
     public int getParity() {
	 return parity;
     }

     /**
      * Get the angle in user space corresponding to an angle in
      * graph coordinate space.
      * "Counterclockwise in graph coordinate space" refers to the
      * angular direction a vector parallel to the positive X axis in
      * graph coordinate space would turn to point parallel to the
      * positive Y axis in graph coordinate space.
      * <P>
      * The angle returned when ccw is <code>true</code> is the angle
      * that this class's drawImage methods would use to make an image's
      * horizontal axis parallel to a line with a given angle from the
      * X axis in graph coordinate space.
      * @param gcsAngle the angle in radians in graph coordinate space,
      *        measured counterclockwise
      * @param ccw true if the angle in user space should be measured
      *        counterclockwise; false if the angle in user space should
      *        be measured clockwise.
      * @return the angle in user space.
      */
     public double getUserSpaceAngle(double gcsAngle, boolean ccw) {
	 if (coordAF == null) createTransforms();
	 double angle;
	 if (xscale == yscale) {
	     if (xscaleSigned > 0) {
		 if (yscaleSigned > 0) {
		     angle = gcsAngle;
		 } else {
		     angle = -gcsAngle;
		 }
	     } else {
		 if (yscaleSigned > 0) {
		     angle = Math.PI - gcsAngle;
		 } else {
		     angle = gcsAngle + Math.PI;
		 }
	     }
	 } else {
	     double x = Math.cos(gcsAngle) * xscaleSigned;
	     double y = Math.sin(gcsAngle) * yscaleSigned;
	     angle = Math.atan2(y, x);
	 }
	 if (graphRotationAngle != 0.0) {
	     angle -= graphRotationAngle;
	 }
	 return (ccw)? angle: -angle;
     }



     private void createTransforms() {
	 double xxscale = ((double)iwidth
			   - xLowerOffset - xUpperOffset)
	     / (xUpper - xLower);
	 double yyscale = - ((double)iheight
			     - yLowerOffset - yUpperOffset)
	     / (yUpper - yLower);

	 xscale = Math.abs(xxscale);
	 yscale = Math.abs(yyscale);

	 xscaleSigned = xxscale;
	 yscaleSigned = -yyscale;

	 coordAF = new
	     AffineTransform(xxscale, 0.0, 0.0, yyscale,
			     xLowerOffset - xxscale * xLower,
			     (double)iheight - yLowerOffset - yyscale * yLower);

	 if (graphRotationAngle != 0.0) {
	     double tmp[] = {xGraphRotationAnchor, yGraphRotationAnchor};
	     coordAF.transform(tmp, 0, tmp, 0, 1);
	     coordAF.preConcatenate
		 (AffineTransform.getRotateInstance(graphRotationAngle,
						    tmp[0], tmp[1]));
	 }
	 try {
	     invCoordAF = (AffineTransform) coordAF.clone();
	     invCoordAF.invert();
	 } catch (NoninvertibleTransformException e) {
	     invCoordAF = null;
	     graphBoundingBoxGCS = null;
	 }
     }

     /**
      * Get a coordinate transformation to go from graph coordinate space to
      * user space.
      * The value returned will be a copy of the transform for this graph.
      * @return the transformation
      */
     public AffineTransform getCoordTransform() {
	 if (coordAF == null) {
	     createTransforms();
	 }
	 return (AffineTransform) coordAF.clone();
     }

     Rectangle2D.Double graphBoundingBoxUS = null;
     Rectangle2D graphBoundingBoxGCS = null;

     /**
      * Get a bounding box for the visible area of a graph.
      * This method is intended for cases were a graph contains
      * a large number of objects with easily computed bounding
      * boxes or bounding boxes that have been cached. If a large
      * fraction of these objects are outside the visible area of
      * the graph, one use this method to avoid an attempt to
      * draw them.
      * <P>
      * The box is such that areas outside the bounding box will
      * be outside the region that is visible. For the graph
      * coordinate space case when rotations are used, a significant
      * fraction of the bounding box will not be visible. Rotations
      * are primarily used for animations that use an instance of
      * {@link org.bzdev.anim2d.GraphView} where the view changes
      * angle.
      * <P>
      * When gcs is true, the bounding box will be larger than the
      * bounding box implied by the range when offsets are used.
      * @param gcs true if the bounding box's coordinates are in
      *        graph coordinate space; false if the bounding box's
      *        units are in user space
      * @return the bounding box
      */
     public Rectangle2D boundingBox(boolean gcs) {
	 if (gcs) {
	     if (coordAF == null) {
		 createTransforms();
	     }
	     if (graphBoundingBoxGCS == null) {
		 Shape s = invCoordTransform(graphBoundingBoxUS);
		 graphBoundingBoxGCS = s.getBounds2D();
	     }
	     return (Rectangle2D)(graphBoundingBoxGCS.clone());
	 } else {
	     return (Rectangle2D)
		 (Rectangle2D.Double)(graphBoundingBoxUS.clone());
	 }
     }

     // cached array used by maybeVisible(Shape,boolean)
     private double[] osfcoords = new double[6];

     /**
      * Determine if a shape might be inside the portion of graph
      * coordinate space that can be displayed.
      * When this method returns true, a shape may be, but not
      * necessarily is, within the visible area for a graph.
      * <P>
      * The main use for this class occurs when a large number of
      * objects may be drawn, with many of them outside the visible
      * portion of graph coordinate space or user space. This method
      * can be used to determine which objects are worth printing.
      * It will run the fastest if the shape given to it is a
      * rectangle representing a bounding box (ideally precomputed).
      * @param s the shape to test
      * @param gcs true if the control points provided by a shape's
      *        path iterator are points in graph coordinate space;
      *        false if they are points in user space
      * @return true if a shape might be visible; false if it is
      *         definitely not visible
      */
     public boolean maybeVisible(Shape s, boolean gcs) {
	 if (gcs) {
	     if (coordAF == null) {
		 createTransforms();
	     }
	     if (graphBoundingBoxGCS == null) {
		 Shape bbs = invCoordTransform(graphBoundingBoxUS);
		 graphBoundingBoxGCS = bbs.getBounds2D();
	     }
	 }
	 Rectangle2D frameBB = gcs? graphBoundingBoxGCS: graphBoundingBoxUS;
	 PathIterator pit = s.getPathIterator(null);
	 while (!pit.isDone()) {
	     switch(pit.currentSegment(osfcoords)) {
	     case PathIterator.SEG_MOVETO:
	     case PathIterator.SEG_LINETO:
		 if (frameBB.outcode(osfcoords[0], osfcoords[1]) == 0) {
		     return true;
		 }
		 break;
	     case PathIterator.SEG_QUADTO:
		 if (frameBB.outcode(osfcoords[0], osfcoords[1]) == 0) {
		     return true;
		 }
		 if (frameBB.outcode(osfcoords[2], osfcoords[3]) == 0) {
		     return true;
		 }
		 break;
	     case PathIterator.SEG_CUBICTO:
		 if (frameBB.outcode(osfcoords[0], osfcoords[1]) == 0) {
		     return true;
		 }
		 if (frameBB.outcode(osfcoords[2], osfcoords[3]) == 0) {
		     return true;
		 }
		 if (frameBB.outcode(osfcoords[4], osfcoords[5]) == 0) {
		     return true;
		 }
		 break;
	     default:
		 break;
	     }
	     pit.next();
	 }
	 return false;
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

     /**
      * Write the graph to a file given a file name.
      * The type parameters are strings naming the file format as used
      * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
      * @param type the file type
      * @param name the file name
      * @exception IOException an error occurred during writing
      */
     public void write(final String type, final String name)
	 throws IOException
     {
	 /*
	 SecurityManager sm = System.getSecurityManager();
	 if (sm != null && name != null) {
	     sm.checkWrite(name);
	 }
	 */
	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(image, type, new File(name))) {
			     throw new IOException
				 (errorMsg("cannotWriteImg", type, name));
				 /*("cannot write this image type to a file");*/
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
     }

     /**
      * Write the graph to a file.
      * The type parameters are strings naming the file format as used
      * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
      * @param type the file type
      * @param file the file to write
      * @exception IOException an error occurred during writing
      */
     public void write(final String type, final File file) throws IOException {
	 final String name = (file != null? file.getPath(): null);
	 /*
	 SecurityManager sm = System.getSecurityManager();
	 if (sm != null && name != null) {
	     sm.checkWrite(name);
	 }
	 */
	 // since the security manager (if any) allowed the file to be
	 // written, we can use a doPrivileged block, which is needed because
	 // ImageIO.write creates a temporary file and there is no default
	 // permission allowing that.
	 try {
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(image, type, file)) {
			     throw new IOException
				 (errorMsg("cannotWriteImg", type, name));
				 /*("cannot write this image type to a file");*/
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
     }

     /**
      * Write the graph to a file provided by a file accessor.
      * The type parameters are strings naming the file format as used
      * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
      * @param type the file type
      * @param fa the file accessor to use
      * @exception IOException an error occurred during writing
      */
     public void write(final String type, FileAccessor fa) throws IOException {
	 final OutputStream os = fa.getOutputStream();
	 try {
	     // A test showed that ImageIO.write, when given an OutputStream,
	     // creates a temporary file, and we need permission to do that.
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(image, type, os)) {
			     throw new IOException
				 (errorMsg("cannotWriteImgFA", type));
				 /*("cannot write this image type to a file");*/
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
     }

     /**
      * Write the graph to an outputStream.
      * The type parameters are strings naming the file format as used
      * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
      * @param type the file type
      * @param os the output stream to which to write the image
      * @exception IOException an error occurred during writing
      */
     public void write(final String type, final OutputStream os)
	 throws IOException
     {
	 try {
	     // A test showed that ImageIO.write, when given an OutputStream,
	     // creates a temporary file, and we need permission to do that.
	     initACC();
	     AccessController.doPrivileged
		 (new PrivilegedExceptionAction<Void>() {
		     public Void run() throws IOException {
			 if (!ImageIO.write(image, type, os)) {
			     throw new IOException
				 (errorMsg("cannotWriteImgOS", type));
				 /*("cannot write this image type to "
				   + "an output stream");*/
			 }
			 return null;
		     }
		 }, context);
	 } catch (PrivilegedActionException e) {
	     throw (IOException) e.getException();
	 }
     }

     /**
      * Flush the graphics to an instance of OSGraphicsOps, if configured
      * by a constructor.
      * The behavior depends on the OSGraphicsOps subclass: some will
      * update and object where others may do nothing.  The documentation
      * for classes implementing OSGraphicsOps should specify the
      * behavior for that class.
      * @exception IOException an IO error occurred.
      */
     public void flush() throws IOException {
	 if (osg != null) {
	     osg.flush();
	 }
     }

     /**
      * Write graphics to an instance of  OSGraphicsOps, if configured
      * by a constructor.
      * @exception IOException an IO error occurred.
      */
     public void write() throws IOException {
	 if (osg != null) {
	     osg.imageComplete();
	 }
     }

     /**
      * Class for symbols used in plotting.
      * A symbol is used to label a data point on a graph
      * and error bars may be added to the symbol if needed.
      * The symbols can be scaled. Subclasses should create
      * symbols with a default size (corresponding to a scale
      * factor of 1.0) that fits in a bounding box 10.0
      * user-space units on a side and centered on (0.0, 0.0).
      */
     public static abstract class Symbol {
	 static final Color DEFAULT_COLOR = Color.BLACK;
	 Color color = DEFAULT_COLOR;

	 /**
	  * Constructor.
	  */
	 protected Symbol() {}


	 /**
	  * Set the symbol color
	  * @param color the color for the symbol
	  */
	 public void setColor(Color color) {
	     if (color == null) {
		 this.color = Color.BLACK;
	     } else {
		 this.color = color;
	     }
	 }

	 /**
	  * Get the symbol color
	  * @return the color for the symbol
	  */
	 public Color getColor() {
	     return color;
	 }

	 static final double DEFAULT_LINETHICKNESS = 1.0;
	 double lineThickness = DEFAULT_LINETHICKNESS;
	 /**
	  * Set the line thickness for drawing the symbol
	  * @param thickness the line thickness, a positive number, in
	  *        user-space units
	  * @exception IllegalArgumentException the argument is out of range
	  */
	 public void setLineThickness(double thickness)
	     throws IllegalArgumentException
	 {
	     if (thickness <= 0.0)
		 throw new IllegalArgumentException
		     (errorMsg("mustBePositive", thickness));
	     lineThickness = thickness;
	 }

	 /**
	  * Get the line thickness for drawing the symbol
	  * @return the line thickness  in user-space units
	  */
	 public double getLineThickness() {
	     return lineThickness;
	 }

	 static final double DEFAULT_SCALE = 1.0;

	 double scale = DEFAULT_SCALE;
	 /**
	  * Set the scale factor for the symbol.
	  * Symbols have a natural size for error bars.
	  * This size will be multiplied by the scale factor when drawing
	  * error bars.
	  * @param scaleFactor the scale factor, a positive number
	  * @exception IllegalArgumentException the argument is out of range
	  */
	 public void setScaleFactor(double scaleFactor)
	     throws IllegalArgumentException
	 {
	     if (scaleFactor <= 0.0)
		 throw new IllegalArgumentException
		     (errorMsg("mustBePositive", scaleFactor ));
	     this.scale = scale;
	 }

	 /**
	  * Get the scale factor for the symbol.
	  * Symbols have a natural size for error bars.
	  * This size will be multiplied by the scale factor when drawing
	  * error bars.
	  * @return the scale factor
	  */
	 public double getScaleFactor() {
	     return scale;
	 }

	 static final double DEFAULT_ERRORBARTABWIDTH = 10.0;
	 double errorBarTabWidth = DEFAULT_ERRORBARTABWIDTH;

	 /**
	  * Set the total tab width for an error bar.
	  * The tab width is the length of the lines at the ends of an error
	  * bar (the horizontal lines in the following figure):
	  * <blockquote><pre>
	  * .      _______
	  * .         |
	  * .         |
	  * .         |
	  * .         o
	  * .         |
	  * .         |
	  * .      ___|___
	  * .
	  * </pre></blockquote>
	  * Note that error bars can be vertical or horizontal, and that some
	  * symbols may require both.
	  * @param width the tab width, a positive number, in user-space units
	  * @exception IllegalArgumentException the argument is out of range
	  */
	 public void setErrorBarTab(double width)
	     throws IllegalArgumentException
	 {
	     if (width <= 0.0)
		 throw new IllegalArgumentException
		     (errorMsg("mustBePositive", width ));
	     errorBarTabWidth = width;
	 }

	 /**
	  * Get the total tab width for an error bar.
	  * The tab width is the length of the lines at the ends of an error
	  * bar (the horizontal lines in the following figure):
	  * <blockquote><pre>
	  * .      _______
	  * .         |
	  * .         |
	  * .         |
	  * .         o
	  * .         |
	  * .         |
	  * .      ___|___
	  * .
	  * </pre></blockquote>
	  * Note that error bars can be vertical or horizontal, and that some
	  * symbols may require both.
	  * @return the tab width in user-space units
	  */
	 public double getErrorBarTab() {
	     return errorBarTabWidth;
	 }

	 /**
	  * Get a UserGraphic representing a shape to draw.
	  * The UserGrapic should fit in a box 10 user-space units
	  * on a side and centered at (0.0, 0.0). The scale factor will
	  * be applied when the symbol is drawn.
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         in graph coordinate space points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        in graph coordinate space points down in user space
	  * @return the UserDrawable representing the portion of the
	  *         symbol to be drawn but not necessarily filled; null
	  *         if there is none
	  */
	 protected abstract UserGraphic getUserGraphic(boolean xAxisPointsRight,
						       boolean yAxisPointsDown);


	 /**
	  * Get the left X coordinate for a horizontal error bar.
	  * This is the location of the left edge of the symbol when
	  * the Y coordinate is zero.
	  * Normally this value will be a negative number.
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        points down in user space
	  * @return the coordinate in user space when the symbol
	  *         is drawn at (0.0, 0.0) with a scale factor of 1.0
	  */
	 protected abstract double getEBarStartLeft(boolean xAxisPointsRight,
						    boolean yAxisPointsDown);

	 /**
	  * Get the right X coordinate for a horizontal error bar.
	  * This is the location of the right edge of the symbol when
	  * the Y coordinate is zero.
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        points down in user space
	  * @return the coordinate in user space when the symbol
	  *         is drawn at (0.0, 0.0) with a scale factor of 1.0
	  */
	 protected abstract double getEBarStartRight(boolean xAxisPointsRight,
						     boolean yAxisPointsDown);

	 /**
	  * Get the upper Y coordinate for a vertical error bar.
	  * This is the location of the upper edge of the symbol when
	  * the X coordinate is zero.
	  * Because of user-space conventions, the upper Y coordinate
	  * will have a lower value than the lower y coordinate.
	  * Normally this value will be a negative number.
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        points down in user space
	  * @return the coordinate in user space when the symbol
	  *         is drawn at (0.0, 0.0) with a scale factor of 1.0
	  */
	 protected abstract double getEBarStartTop(boolean xAxisPointsRight,
						   boolean yAxisPointsDown);

	 /**
	  * Get the lower Y coordinate for a vertical error bar.
	  * This is the location of the lower edge of the symbol when
	  * the X coordinate is zero.
	  * @param xAxisPointsRight the direction for increasing X coordinates
	  *         points right in user space
	  * @param yAxisPointsDown the direction for increasing Y coordinates
	  *        points down in user space
	  * @return the coordinate in user space when the symbol
	  *         is drawn at (0.0, 0.0) with a scale factor of 1.0
	  */
	 protected abstract double getEBarStartBottom(boolean xAxisPointsRight,
						      boolean yAxisPointsDown);

     }

     /**
      * Draw a symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      */
     public void draw(Symbol symbol, double x, double y) {
	 drawEXY(symbol, x, y, 0.0, 0.0, 0.0, 0.0);
     }

     /**
      * Draw a symbol with symmetric error bars in the X direction.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param error distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      */
     public void drawEX(Symbol symbol, double x, double y,
				 double error)
     {
	 drawEXY(symbol, x, y, error, error, 0.0, 0.0);
     }

     /**
      * Draw a symbol with asymmetric error bars in the X direction.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param elow distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the lowest value of x
      * @param ehigh distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the highest value of x
      */
     public void drawEX(Symbol symbol, double x, double y,
				 double elow, double ehigh)
     {
	 drawEXY(symbol, x, y, elow, ehigh, 0.0, 0.0);
     }

     /**
      * Draw a symbol with symmetric error bars in the Y direction.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param error distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      */
     public void drawEY(Symbol symbol, double x, double y,
				 double error)
     {
	 drawEXY(symbol, x, y, 0.0, 0.0, error, error);
     }

     /**
      * Draw a symbol with asymmetric error bars in the Y direction.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param elow distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the lowest value of y
      * @param ehigh distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the highest value of y
      */
     public void drawEY(Symbol symbol, double x, double y,
				 double elow, double ehigh)
     {
	 drawEXY(symbol, x, y, 0.0, 0.0, elow, ehigh);
     }

     /**
      * Draw a symbol with symmetric error bars in the X and Y direction.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param errorX distance in the X direction in graph coordinate
      *        space from the point (x, y) to the edge of an error bar
      * @param errorY distance in the Y direction in graph coordinate
      *        space from the point (x, y) to the edge of an error bar
      */
     public void drawEXY(Symbol symbol, double x, double y,
			 double errorX, double errorY)
     {
	 drawEXY(symbol, x, y, errorX, errorX, errorY, errorY);
     }

     /**
      * Draw a symbol with asymmetric error bars in the X and Y directions.
      * An error bar will not be displayed if it does not extend
      * past the symbol.
      * @param symbol the symbol
      * @param x the x coordinate in graph coordinate space
      * @param y the y coordinate in graph coordinate space
      * @param elowX distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the lowest value of x
      * @param ehighX distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the highest value of x
      * @param elowY distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the lowest value of y
      * @param ehighY distance in graph coordinate space from
      *        the point (x, y) to the edge of an error bar
      *        with the highest value of y
      */
     public void drawEXY(final Symbol symbol, double x, double y,
				  double elowX, double ehighX,
				  double elowY, double ehighY)
     {
	 Graphics2D g2d = createGraphics();
	 try {
	     g2d.setColor(symbol.getColor());
	     g2d.setStroke(new BasicStroke((float)symbol.getLineThickness()));
	     boolean xAxisRight = xAxisPointsRight();
	     boolean yAxisDown = yAxisPointsDown();
	     final UserGraphic userGraphic =
		 symbol.getUserGraphic(xAxisRight,yAxisDown);
	     double scale = symbol.getScaleFactor();
	     g2d.scale(scale, scale);
	     add(g2d, userGraphic, x, y);

	     double etabHalfWidth = symbol.getErrorBarTab() / 2.0;

	     final Path2D ebar1 = new Path2D.Double();
	     if (elowX != 0.0) {
		 double ebarlen = elowX  * getXScale();
		 boolean canDraw = true;
		 if (xAxisPointsRight()) {
		     double start =
			 symbol.getEBarStartLeft(xAxisRight, yAxisDown) * scale;
		     if (ebarlen >= -start) {
			 ebar1.moveTo(start, 0.0);
			 ebar1.lineTo(-ebarlen, 0.0);
			 ebar1.moveTo(-ebarlen, -etabHalfWidth);
			 ebar1.lineTo(-ebarlen, etabHalfWidth);
		     } else {
			 canDraw = false;
		     }
		 } else {
		     double start = symbol.getEBarStartRight(xAxisRight,
							     yAxisDown)
			 * scale;
		     if (ebarlen > start) {
			 ebar1.moveTo(start, 0.0);
			 ebar1.lineTo(ebarlen, 0.0);
			 ebar1.moveTo(ebarlen, -etabHalfWidth);
			 ebar1.lineTo( ebarlen, etabHalfWidth);
		     } else {
			 canDraw = false;
		     }
		 }
		 if (canDraw) {
		     draw(g2d, new UserDrawable() {
			     public Shape toShape(boolean xAxisPointsRight,
						  boolean yAxisPointsDown)
			     {
				 return ebar1;
			     }
			 }, x, y);
		 }
	     }

	     final Path2D ebar2 = new Path2D.Double();
	     if (ehighX != 0.0) {
		 double ebarlen = ehighX * getXScale();
		 boolean canDraw = true;
		 if (xAxisPointsRight()) {
		     double start = symbol.getEBarStartRight(xAxisRight,
							     yAxisDown)
			 * scale;
		     if (ebarlen > start) {
			 ebar2.moveTo(start, 0.0);
			 ebar2.lineTo(ebarlen, 0.0);
			 ebar2.moveTo(ebarlen, -etabHalfWidth);
			 ebar2.lineTo(ebarlen, etabHalfWidth);
		     } else {
			 canDraw = false;
		     }
		 } else {
		     double start =
			 symbol.getEBarStartLeft(xAxisRight, yAxisDown) * scale;
		     if (ebarlen > -start) {
			 ebar2.moveTo(start, 0.0);
			 ebar2.lineTo(-ebarlen, 0.0);
			 ebar2.moveTo(-ebarlen, -etabHalfWidth);
			 ebar2.lineTo(-ebarlen, etabHalfWidth);
		     } else {
			 canDraw = false;
		     }
		 }
		 if (canDraw) {
		     draw(g2d, new UserDrawable() {
			     public Shape toShape(boolean xAxisPointsRight,
						  boolean yAxisPointsDown)
			     {
				 return ebar2;
			     }
			 }, x, y);
		 }
	     }

	     final Path2D ebar3 = new Path2D.Double();
	     if (elowY != 0.0) {
		 double ebarlen = elowY * getYScale();
		 boolean canDraw = true;
		 if (yAxisPointsDown()) {
		     double start = symbol.getEBarStartBottom(xAxisRight,
							      yAxisDown)
			 * scale;
		     if (ebarlen > start) {
			 ebar3.moveTo(0.0, start);
			 ebar3.lineTo(0.0, ebarlen);
			 ebar3.moveTo(-etabHalfWidth, ebarlen);
			 ebar3.lineTo(etabHalfWidth, ebarlen);
		     } else {
			 canDraw = false;
		     }
		 } else {
		     double start =
			 symbol.getEBarStartTop(xAxisRight, yAxisDown) * scale;
		     if (ebarlen > -start) {
			 ebar3.moveTo(0.0, start);
			 ebar3.lineTo(0.0, -ebarlen);
			 ebar3.moveTo( -etabHalfWidth, -ebarlen);
			 ebar3.lineTo(etabHalfWidth, -ebarlen);
		     } else {
			 canDraw = false;
		     }
		 }
		 if (canDraw) {
		     draw(g2d, new UserDrawable() {
			     public Shape toShape(boolean xAxisPointsRight,
						  boolean yAxisPointsDown)
			     {
				 return ebar3;
			     }
			 }, x, y);
		 }
	     }

	     final Path2D ebar4 = new Path2D.Double();
	     if (ehighY != 0.0) {
		 double ebarlen= ehighY * getYScale();
		 boolean canDraw = true;
		 if (yAxisPointsDown()) {
		     double start =
			 symbol.getEBarStartTop(xAxisRight, yAxisDown) * scale;
		     if (ebarlen > -start) {
			 ebar4.moveTo(0.0, start);
			 ebar4.lineTo(0.0, -ebarlen);
			 ebar4.moveTo(-etabHalfWidth, -ebarlen);
			 ebar4.lineTo(etabHalfWidth, -ebarlen);
		     } else {
			 canDraw = false;
		     }
		 } else {
		     double start = symbol.getEBarStartBottom(xAxisRight,
							      yAxisDown)
			 * scale;
		     if (ebarlen > start) {
			 ebar4.moveTo(0.0, start);
			 ebar4.lineTo(0.0, ebarlen);
			 ebar4.moveTo(-etabHalfWidth, ebarlen);
			 ebar4.lineTo(etabHalfWidth, ebarlen);
		     } else {
			 canDraw = false;
		     }
		 }
		 if (canDraw) {
		     draw(g2d, new UserDrawable() {
			     public Shape toShape(boolean xAxisPointsRight,
						  boolean yAxisPointsDown)
			     {
				 return ebar4;
			     }
			 }, x, y);
		 }
	     }
	 } finally {
	     g2d.dispose();
	 }
     }

     /**
      * Factory class for creating graph symbols.
      * A service-provider interface
      * {@link org.bzdev.graphs.spi.SymbolProvider} can be implemented
      * to add new symbols.  See
      * {@link org.bzdev.graphs.spi.SymbolProvider} for details.
      * <P>
      * The BZDev class library contains the following symbols as a
      * minimal set of symbols:
      * EmptyBowtie, EmptyCircle, EmptyHourglass, EmptySquare, SolidBowtie
      * SolidCircle, SolidHourglass, and SolidSquare. These are located in the
      * package org.bzdev.graphs.symbols.  There are no API pages
      * for these classes as users will not call a symbol's methods
      * directly and all of them merely implement
      * {@link Graph.Symbol}.
      */
     public static class SymbolFactory {

	 /**
	  * Constructor.
	  */
	 public SymbolFactory(){}

	 Color color = Symbol.DEFAULT_COLOR;
	 /**
	  * Set the symbol color
	  * @param color the color for the symbol
	  */
	 public void setColor(Color color) {
	     this.color = color;
	 }

	 /**
	  * Get the symbol color
	  * @return the color for the symbol
	  */
	 public Color getColor() {
	     return color;
	 }

	 double lineThickness = Symbol.DEFAULT_LINETHICKNESS;
	 /**
	  * Set the line thickness for drawing the symbol
	  * @param thickness the line thickness in user-space units
	  */
	 public void setLineThickness(double thickness) {
	     lineThickness = thickness;
	 }

	 /**
	  * Get the line thickness for drawing the symbol
	  * @return the line thickness  in user-space units
	  */
	 public double getLineThickness() {
	     return lineThickness;
	 }

	 double scale = Symbol.DEFAULT_SCALE;
	 /**
	  * Set the scale factor for the symbol.
	  * Symbols are generally defined so that, for a scale factor of
	  * 1.0, they will be sized appropriately for a bounding box
	  * 10 units on a side in user-space units. The scale factor
	  * can be used to change this default size (the line thickness
	  * is scaled along with the shape that is drawn).
	  * The default size will be multiplied by the scale factor.
	  * @param scaleFactor the scale factor
	  */
	 public void setScaleFactor(double scaleFactor) {
	     this.scale = scale;
	 }

	 /**
	  * Get the scale factor for the symbol.
	  * Symbols are generally defined so that, for a scale factor of
	  * 1.0, they will be sized appropriately for a bounding box
	  * 10 units on a side in user-space units.  The scale factor
	  * can be used to change this default size (the line thickness
	  * is scaled along with the shape that is drawn).
	  * The default size will be multiplied by the scale factor.
	  * @return the scale factor
	  */
	 public double getScaleFactor() {
	     return scale;
	 }

	 double errorBarTabWidth = Symbol.DEFAULT_ERRORBARTABWIDTH;

	 /**
	  * Set the total tab width for an error bar.
	  * The tab width is the length of the lines at the ends of an error
	  * bar (the horizontal lines in the following figure):
	  * <blockquote><pre>
	  * .      _______
	  * .         |
	  * .         |
	  * .         |
	  * .         o
	  * .         |
	  * .         |
	  * .      ___|___
	  * .
	  * </pre></blockquote>
	  * Note that error bars can be vertical or horizontal, and that some
	  * symbols may require both.
	  * @param width the tab width in user-space units
	  */
	 public void setErrorBarTab(double width) {
	     errorBarTabWidth = width;
	 }

	 /**
	  * Get the total tab width for an error bar.
	  * The tab width is the length of the lines at the ends of an error
	  * bar (the horizontal lines in the following figure):
	  * <blockquote><pre>
	  * .      _______
	  * .         |
	  * .         |
	  * .         |
	  * .         o
	  * .         |
	  * .         |
	  * .      ___|___
	  * .
	  * </pre></blockquote>
	  * Note that error bars can be vertical or horizontal, and that some
	  * symbols may require both.
	  * @return the tab width in user-space units
	  */
	 public double getErrorBarTab() {
	     return errorBarTabWidth;
	 }

	 private static Map<String, Class<? extends Graph.Symbol>> map
	    = new LinkedHashMap<String, Class<? extends Graph.Symbol>>();

	 static {
	     ServiceLoader<SymbolProvider> loader
		 = ServiceLoader.load(SymbolProvider.class);
	     for (SymbolProvider provider: loader) {
		 String name = provider.getSymbolName();
		 if (!map.containsKey(name)) {
		     map.put(name, provider.getSymbolClass());
		 }
	     }
	 }

	 /**
	  * Get the names of known symbols.
	  * A service provider interface
	  * {@link org.bzdev.graphs.spi.SymbolProvider} can be used
	  * to add additional symbols.
	  * @return the names of the symbols that have been defined.
	  */
	 public String[] getSymbolNames() {
	     String[] result = new String[map.size()];
	     return map.keySet().toArray(result);
	 }

	 /**
	  * Create a new symbol by name.
	  * @param name the symbol's name
	  * @return the symbol as configured by this factory
	  */
	 public Symbol newSymbol(String name)
	     throws IllegalArgumentException
	 {
	     Class<? extends Graph.Symbol> clazz = map.get(name);
	     if (clazz == null) {
		 throw new IllegalArgumentException(errorMsg("noSymbol", name));
	     }
	     return newSymbol(clazz);
	 }

	 /**
	  * Create a new symbol by class.
	  * @param clazz the symbol's class.
	  * @return the symbol as configured by this factory
	  */
	 public Symbol newSymbol(Class<? extends Graph.Symbol> clazz)
	     throws IllegalArgumentException
	 {
	     try {
		 // Symbol symbol = clazz.newInstance();
		 Symbol symbol = clazz.getDeclaredConstructor().newInstance();
		 symbol.setColor(color);
		 symbol.setLineThickness(lineThickness);
		 symbol.setScaleFactor(scale);
		 symbol.setErrorBarTab(errorBarTabWidth);
		 return symbol;
	     } catch(ReflectiveOperationException e) {
		 String msg = errorMsg("reflectiveOp", clazz.getName());
		 throw new IllegalArgumentException(msg, e);
	     }
	     /*
	     } catch (InstantiationException e1) {
		 String msg = errorMsg("notInstantiated", clazz.getName());
		 throw new IllegalArgumentException(msg, e1);
	     } catch(ExceptionInInitializerError e2) {
		 String msg = errorMsg("notInitialized", clazz.getName());
		 throw new IllegalArgumentException(msg, e2);
	     } catch (IllegalAccessException e3) {
		 String msg = errorMsg("notAccessible", clazz.getName());
		 throw new IllegalArgumentException(msg, e3);
	     }
	     */
	 }
     }
 }

//  LocalWords:  exbundle ul li BufferedImage pre mdash setOffsets fp
//  LocalWords:  setRanges getWidth getHeight createGraphics refPoint
//  LocalWords:  createGraphicsGCS drawImage refPointName scaleX anim
//  LocalWords:  scaleY flipX flipY imageInGCS drawString FontParms
//  LocalWords:  toShape addTo UserDrawable UserGraphic TickSpec osg
//  LocalWords:  IllegalArgumentException osgNoCopy boolean setColor
//  LocalWords:  OutputStreamGraphics setClearByFillMode infoflags lt
//  LocalWords:  setBackgroundColor getClearByFillMode img Helvetica
//  LocalWords:  getBackgroundColor cannotLoadImage IOException parms
//  LocalWords:  FontParams blp cloneable fontName fontStyle setFont
//  LocalWords:  getAllFonts getAvailableFontFamilyNames textwidth sc
//  LocalWords:  nextFont fontSize fontBaseSize xDrawingOffset dGCS
//  LocalWords:  yDrawingOffset xAxisPointsRight yAxisPointsDown enum
//  LocalWords:  srcPoint destPoint noInverseTransform modtest printf
//  LocalWords:  limitModulus modlimit SciFormatter formatter LogAxis
//  LocalWords:  showTick showTickLabel stringOffset tickBase dir xL
//  LocalWords:  tickIncr startX startY tickSpec yL drawImg RGB ARGB
//  LocalWords:  logStartX logStartY logLength logAxisTickIncr tspecs
//  LocalWords:  xU yU xLower xUpper yLower yUpper xgcs ygcs xf yf os
//  LocalWords:  scaleFactorX scaleFactorY ImageIO cannotWriteImg BGR
//  LocalWords:  doPrivileged accessor OutputStream cannotWriteImgFA
//  LocalWords:  outputStream cannotWriteImgOS mustBePositive elow iw
//  LocalWords:  scaleFactor blockquote UserGrapic ehigh errorX elowX
//  LocalWords:  errorY ehighX elowY ehighY noSymbol notInstantiated
//  LocalWords:  notInitialized notAccessible ABGR USHORT href ih hw
//  LocalWords:  refpointName refpoint hh fontParms g's daf le th fx
//  LocalWords:  coordTransform getTickLabel runtime setLabel notDone
//  LocalWords:  setLabelOffset initialIndex axisCoord axisValue xus
//  LocalWords:  modifyGraphics modifyFontParms superclass setWidth
//  LocalWords:  tspec getEnd whitespace dwidthWithOffset fy yus API
//  LocalWords:  dheightWithOffset xAnchor yAnchor BZDev EmptyBowtie
//  LocalWords:  EmptyCircle EmptyHourglass EmptySquare SolidBowtie
//  LocalWords:  SolidCircle SolidHourglass SolidSquare clazz affine
//  LocalWords:  setAxisScale Drawable getUserSpaceAngle drawable ccw
//  LocalWords:  OSGraphicsOps Subclasses subclasses tickmark gcs isw
//  LocalWords:  gcsAngle maybeVisible precomputed xscale yscale png
//  LocalWords:  imageInCGS graphRotationAngle iswNoCopy requestAlpha
//  LocalWords:  ISWriterOps ISWSize nextOutputStreamGraphics OSGSize
//  LocalWords:  IllegalStateException setOSGraphics FileOutputStream
//  LocalWords:  newInstance setStroke BasicStroke pts SymbolFactory
//  LocalWords:  circ newSymbol drawEY AxisBuilder setMaximumExponent
//  LocalWords:  addTickSpec setOffests xLowerOffset xUpperOffset msg
//  LocalWords:  yLowerOffset yUpperOffset ImageType bitype setClip
//  LocalWords:  getImageType getClip reflectiveOp errorMsg getName
//  LocalWords:  InstantiationException ExceptionInInitializerError
//  LocalWords:  IllegalAccessException
