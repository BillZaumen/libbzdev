package org.bzdev.p3d;
// import org.bzdev.io.FISOutputStream;
import org.bzdev.util.*;
import org.bzdev.lang.StackTraceModePermission;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.WebEncoder;
import org.bzdev.geom.PathIterator3D;
import org.bzdev.geom.PathSplitter;
import org.bzdev.geom.Path3D;
import org.bzdev.geom.Point3D;
import org.bzdev.geom.Rectangle3D;
import org.bzdev.geom.Shape3D;
import org.bzdev.geom.SubdivisionIterator;
import org.bzdev.geom.SurfaceOps;
import org.bzdev.geom.Surface3D;
import org.bzdev.geom.SurfaceIterator;
import org.bzdev.geom.Transform3D;
import org.bzdev.gio.GraphicsCreator;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.gio.OSGraphicsOps;
import org.bzdev.gio.OutputStreamGraphics;
import org.bzdev.io.FileAccessor;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.GraphCreator;
import org.bzdev.lang.MathOps;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.Adder;
import org.bzdev.math.Adder.Kahan;
import org.bzdev.math.VectorOps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.io.*;
import javax.imageio.*;
import java.awt.geom.AffineTransform;

import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channels;
import java.security.*;

//@exbundle org.bzdev.p3d.lpack.P3d

/**
 * Three-dimensional model class.
 * This class represents a 3D model of an object as a set of
 * triangles.  It can produce an STL file, which is commonly supported
 * by 3D printers.
 * <P>
 * As an example of its use, the statements
 * <blockquote><code><pre>
 * Model3D m3d = new Model3D();
 * m3d.setStackTraceMode(true);
 * m3d.addTriangle(...);
 * ...
 * m3d.addTriangle(...);
 * if (m3d.notPrintable(System.out) System.exit(1);
 * m3d.writeSTL("model test", "model.stl");
 * System.exit(0);
 * </pre></code></blockquote>
 * will create a model, test it to verify that it is printable on a 3D
 * printer, and if it is printable, create an STL file.  The call to
 * setStackTraceMode will configure the model so that each triangle
 * the model creates with an explicitly specified tag is tagged with a
 * stack trace created when the triangle was created. Please see
 * {@link Model3D#setStackTraceMode(boolean)} for the procedure for
 * setting this permission when the scrunner command is used to run a
 * script.
 * <P>
 * The triangles in a model are expected to follow the same rules used
 * by STL files: the right-hand rule determines the triangle's orientation
 * and an edge of a triangle must intersect another triangle at a vertex.
 * While one can create triangles explicitly, one can also add the objects
 * in another instance of Model3D.
 * <P>
 * The simplest way to create an image showing the model is to
 * use the method
 * {@link Model3D#createImageSequence(OutputStream,String,int,int)}:
 * <blockquote><code><pre>
 *  Model3D m3d = new Model3D();
 *  ...
 *  m3d.addTriangle(...);
 *  ...
 *  m3d.createImageSequence(new FileOutputStream("model.isq"), "png", 8, 4);
 * </pre></code></blockquote>
 * <P>
 * The file model.isq is a ZIP file with a manifest describing various
 * image parameters, and a with a sequence of image files showing the model
 * in various orientations.
 * <P>
 * The following code creates an image, using the* low-level API:
 * <blockquote><code><pre>
 * int WIDTH = 800;
 * int HEIGHT = 800;
 * ...
 * Model3D m3d = new Model3D();
 * ...
 * m3d.addTriangle(...);
 * ...
 * Model3D.Image image =
 *    new Model3D.Image(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
 * image.setCoordRotation(...);
 * image.setBacksideColor(Color.RED);
 * image.setEdgeColor(Color.GREEN);
 * Graphics2D g2d = image.createGraphics();
 * g2d.setColor(Color.BLUE);
 * g2d.fillRect(0, 0, WIDTH, HEIGHT);
 * g2d.dispose();
 * m3d.setImageParameters(image);
 * m3d.render(image);
 * image.write("png", "modeltest.png");
 * </pre></code></blockquote>
 * <P>
 * To create an animation showing the model from different orientations
 * use the following code:
 * <blockquote><code><pre>
 * Model3D m3d = new Model3D();
 * ...
 * m3d.addTriangle(...);
 * ...
 * Animation2D a2d = new Animation2D(...);
 * a2d.setBackgroundColor(Color.BLUE.dargker().darker());
 * Model3DViewFactory = new Model3DViewFactory(a2d);
 * factory.setModel(m3d);
 * // configure factory
 * ...
 * factory.createObject(view);
 * // clear directory tmp
 * ...
 * int maxframes = a2d.estimateFrameCount(ANIMATION_LENGTH);
 * a2d.initFrames(maxFrames, "tmp/img-", "png")
 * a2d.scheduleFrames(0, maxframes);
 * a2d.run();
 * </pre></code></blockquote>
 * The documentation for {@link Model3DViewFactory} describes how to
 * configure this factory.
 * <P>

 * When a triangle is created, the coordinates of each vertex are
 * rounded to the nearest single-precision floating-point number, but
 * stored as a double-precision floating-point number.  In addition,
 * any value that differs from 0 by less than one part in 10<sup>10</sup>
 * will be treated as 0.0.
 * This is done for two reasons:
 * <OL>
 *  <LI> to prevent round-off errors from turning what should be a
 *       common vertex for multiple triangles into multiple vertices.
 *  <LI> So that area and volume computations will use the coordinates
 *       of vertices as those would be stored in an STL file.
 * </OL>
 * Although an STL file can contain a normal vector for a triangle,
 * the models create the normal vector from the vertices directly:
 * the computed vector is more accurate.
 * <P>
 * Two types of transformations can be applied when a model is being
 * created:
 * <UL>
 *     <LI> One may call {@link #pushParms()}, followed by
 *          {@link #setObjectTranslation(double,double,double) setObjectTranslation},
 *          {@link #setObjectTranslation(double,double,double,double,double,double) setObjectTranslation},
 *          and {@link #setObjectRotation(double,double,double) setObjectRotation}
 *          in order to change the position and orientation the triangles
 *          that will be created.  This is intended for the case where
 *          an object needs to have a particular orientation to be
 *          printed or when multiple copies of the object are needed.
 *     <LI> One may call {@link #pushTransform(Transform3D)} to modify
 *          an object.  The transform can be an arbitrary one. For
 *          example, one could create a set of triangles that represents
 *          a disk, increase its height with increasing radius, and
 *          then stretch that object in the Y direction in order to
 *          create an elliptical bowl.
 * </UL>
 * Calls to {@link #popParms()} and {@link #popTransform()} will remove
 * the transforms that were created, leaving the ones previously in place.
 * <P>
 * Because {@link Model3D} was designed for 3D printing, coordinate
 * values are rounded to single-precision floating point numbers and
 * very small values (those with absolute values less than the value
 * produced by the Java expression Math.ulp(1.0F)) are rounded to
 * 0.0. This rounding is not performed by
 * {@link org.bzdev.geom.Surface3D} and its subclasses.
 * <P>
 * {@link Model3D} provides several methods to aid in debugging. In
 * particular,
 * <UL>
 *      <LI> {@link Model3D#setStackTraceMode(boolean)}. This method can
 *           configure Model3D so that each primitive object added to the
 *           model is tagged with a stack trace showing where that object
 *           was created.
 *      <LI> {@link Model3D#notPrintable(Appendable)}. This method indicates
 *           if a model is not printable and provides output indicating problem
 *           areas such as edges that do not match as required or triangles
 *           that intersect each other except along edges.
 *      <LI> {@link Model3D#verifyClosed2DManifold()}. The method is used by
 *           {@link Model3D#notPrintable(Appendable)} but can be called directly
 *           to find edges that are not paired as required.
 *      <LI> {@link Model3D#verifyEmbedded2DManifold()}. The method is used by
 *           {@link Model3D#notPrintable(Appendable)} but can be called directly
 *           to find triangles that intersect other than along a shared edge.
 *      <LI> {@link Model3D#createCrossSection(Graph,double,double,double,double[])}.
 *           This method displays a graph showing a cross section of the model
 *           by projecting edges that pass through a plane onto that plane
 *           and ignoring other parts of the model.  Edges or triangles
 *           that meet this criteria and that are reported by
 *           {@link Model3D#verifyClosed2DManifold()} or
 *           {@link Model3D#verifyEmbedded2DManifold()} are shown in red.
 *           All other edges are shown in black.
 * </UL>
 */
public class Model3D implements Shape3D, Model3DOps<Model3D.Triangle>
{

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
    }

    /**
     * Image for a 3D model.
     * While the p3d package can create images of 3D models, these
     * images are intended primarily for debugging.  For example, it
     * can show the interior of a surface in a different color than the
     * exterior, which is useful in finding errors in a model.  For
     * high quality images, one should consider creating an STL or
     * X3D file and importing it into programs designed to display
     * 3D models&mdash;these can take advantage of graphics processors.
     * and will produce more realistic images.
     * <P>
     * The constructors are the same as those for the BufferedImage class,
     * with there additional constructors that can use a
     * {@link org.bzdev.graphs.GraphCreator},
     * {@link org.bzdev.graphs.Graph} or
     * {@link org.bzdev.gio.OSGraphicsOps} instead of an internal
     * buffered image. The class {@link org.bzdev.anim2d.Animation2D}
     * implements {@link org.bzdev.graphs.GraphCreator}, whereas the
     * classes {@link org.bzdev.gio.OutputStreamGraphics} and
     * {@link org.bzdev.swing.PanelGraphics} implement
     * {@link org.bzdev.gio.OSGraphicsOps}.
     * <P>
     * After creating an instance of this class, one will configure
     * parameters for rendering:
     * please see {@link ImageData} for details.  For the simplest
     * case, one will use
     * {@link ImageData#setCoordRotation(double,double,double)}
     * and one of the {@link Model3D} methods
     * {@link Model3D#setImageParameters(Model3D.ImageData)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double,double)}, or
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double,double,boolean)}
     * to complete the configuration.
     * <P>
     * To render the image, one will use the {@link Model3D} method
     * {@link Model3D#render(Model3D.Image)} (several variants of this
     * method are available for more complex cases such as rendering
     * images from multiple models).
     * <P>
     * Finally, to write the image to a file or stream, one will
     * use the method {@link Image#write(String,String)},
     * {@link Image#write(String, File)},
     * {@link Image#write(String, FileAccessor)},
     * {@link Image#write(String, OutputStream)}, or
     * {@link Image#write()}.
     * @see java.awt.image.BufferedImage
     * @see org.bzdev.graphs.Graph
     * @see org.bzdev.gio.OSGraphicsOps
     */
    public static class Image implements GraphicsCreator, ImageData {
	ImageDataImpl idata;
	BufferedImage image = null;
	OSGraphicsOps osg = null;
	/**
	 * Constructor given a color model.
	 * @param cm the color model for the image
	 * @param raster the Raster for the data representing the image
	 * @param isRasterPremultiplied if true, the data in the raster has
	 *        been premultiplied with alpha
	 * @param properties a Hashtable of String/Object pairs
	 * @see java.awt.image.BufferedImage
	 */
	public Image(ColorModel cm, WritableRaster raster, 
		     boolean isRasterPremultiplied, 
		     Hashtable<?,?> properties)
	{
	    image = new BufferedImage(cm, raster, isRasterPremultiplied,
				      properties);
	    idata = new ImageDataImpl(image.getWidth(), image.getHeight());
	}

	/**
	 * Constructor.
	 * The predefined image types are the same as for BufferedImage
	 * @param width the width of the image
	 * @param height the height of the image
	 * @param imageType the image type
	 * @see java.awt.image.BufferedImage
	 */
	public Image(int width, int height, int imageType) {
	    image = new BufferedImage(width, height, imageType);
	    idata = new ImageDataImpl(image.getWidth(), image.getHeight());
	}

	/**
	 * Constructor given an index color model.
	 * @param width the width of the image
	 * @param height the height of the image
	 * @param imageType the image type, restricted to either
	 *        TYPE_BYTE_BINARY or TYPE_BYTE_INDEXED
	 * @param cm the IndexColorModel to use
	 * @see java.awt.image.BufferedImage

	 */
	public Image(int width, int height, int imageType, IndexColorModel cm) {
	    image = new BufferedImage(width, height, imageType, cm);
	    idata = new ImageDataImpl((int)image.getWidth(),
				      (int)image.getHeight());
	}

	/**`
	 * Constructor specifying an OSGraphicsOps.
	 * @param osg the OSGraphicsOps to be used to create an image
	 */
	public Image (OSGraphicsOps osg) {
	    this.osg = osg;
	    idata = new ImageDataImpl(osg.getWidth(), osg.getHeight());
	}

	// when  null, we don't need it. This may be set to a non-null
	// value in a constructor.
	private GraphCreator gc2d = null;

	private void fetchImage() {
	    Graph graph = gc2d.getGraph();
	    if (graph != null) {
		image = graph.getImage();
		if (image == null) {
		    osg = graph.getOutputStreamGraphics();
		}
		gc2d = null;
	    }
	}

	/**
	 * Constructor given a graph creator.
	 * The interface {@link GraphCreator} is implemented
	 * by {@link org.bzdev.anim2d.Animation2D}, which may construct
	 * its graph after this Constructor is called.
	 * @param gc2d the object that will create a graph used by
	 *        this object to provide a buffered image or graphics
	 *        output stream
	 */
	public Image(GraphCreator gc2d) {
	    Graph graph = gc2d.getGraph();
	    if (graph != null) {
		image = graph.getImage();
		if (image == null) {
		    osg = graph.getOutputStreamGraphics();
		}
	    } else {
		this.gc2d = gc2d;
	    }
	    idata = new ImageDataImpl(gc2d.getWidthAsInt(),
				      gc2d.getHeightAsInt());
	}

	/**
	 * Constructor given a Graph.
	 * The image created will share the graph's buffered image or
	 * output-stream graphics.
	 * @param graph an instance of Graph
	 */
	public Image (Graph graph) {
	    image = graph.getImage();
	    if (image == null) {
		osg = graph.getOutputStreamGraphics();
	    }
	    idata = new ImageDataImpl(graph.getWidthAsInt(),
				      graph.getHeightAsInt());
	}

	@Override
	public Graphics2D createGraphics() {
	    // gc2d is set to null when the graph is available
	    if (gc2d != null) fetchImage();
	    if (image != null) return image.createGraphics();
	    else if (osg != null) return osg.createGraphics();
	    else return null;
	}

	@Override
	public ImageDataImpl getImageData() {
	    return idata;
	}
	
	@Override
	public int getWidth() {
	    return idata.getWidth();
	}

	@Override
	public int getHeight() {
	    return idata.getHeight();
	}

	@Override
	public float getFloatWidth() {
	    return idata.getFloatWidth();
	}

	@Override
	public float getFloatHeight() {
	    return idata.getFloatHeight();
	}

	@Override
	public double getScaleFactor() {
	    return idata.getScaleFactor();
	}

	@Override
	public void setScaleFactor(double scaleFactor) {
	    idata.setScaleFactor(scaleFactor);
	}

	@Override
	public float getXOrigin() {
	    return idata.getXOrigin();
	}

	@Override
	public float getYOrigin() {
	    return idata.getYOrigin();
	}

	@Override
	public void setOrigin(int x, int y) {
	    idata.setOrigin(x, y);
	}

	@Override
	public void setOrigin(double x, double y) {
	    idata.setOrigin(x, y);
	}

	@Override
	public void setOrigin(float x, float y) {
	    idata.setOrigin(x, y);
	}

	@Override
	public void setOrigin (int offset) {
	    idata.setOrigin(offset);
	}

	@Override
	public void setOrigin (double offset) {
	    idata.setOrigin(offset);
	}

	@Override
	public void setOrigin (float offset) {
	    idata.setOrigin(offset);
	}

	@Override
	public void setOriginByFraction(double offset) {
	    idata.setOrigin(offset);
	}

	@Override
	public void setOriginByFraction (float offset) {
	    idata.setOrigin(offset);
	}

	@Override
	public void setOrigin() {
	    idata.setOrigin();
	}

	@Override
	public double getPhi() {
	    return idata.getPhi();
	}

	@Override
	public double getTheta() {
	    return idata.getTheta();
	}

	@Override
	public double getPsi() {
	    return idata.getPsi();
	}

	@Override
	public void setCoordRotation(double phi, double theta, double psi) {
	    idata.setCoordRotation(phi, theta, psi);
	}

	@Override
	public double getRotationXOrigin() {
	    return idata.getRotationXOrigin();
	}

	@Override
	public double getRotationYOrigin() {
	    return idata.getRotationYOrigin();
	}

	@Override
	public double getRotationZOrigin() {
	    return idata.getRotationZOrigin();
	}

	@Override
	public boolean moveOriginAfterRotation() {
	    return idata.moveOriginAfterRotation();
	}

	@Override
	public void setRotationOrigin(double x, double y, double z) {
	    idata.setRotationOrigin(x, y, z);
	}

	@Override
	public void setRotationOrigin(double x, double y, double z,
				      boolean move)
	{
	    idata.setRotationOrigin(x, y, z, move);
	}

	@Override
	public double getXTranslation() {
	    return idata.getXTranslation();
	}

	@Override
	public double getYTranslation() {
	    return idata.getYTranslation();
	}

	@Override
	public void setTranslation(double x, double y) {
	    idata.setTranslation(x, y);
	}

	@Override
	public double getDelta() {
	    return idata.getDelta();
	}

	@Override
	public void setDelta(double delta) {
	    idata.setDelta(delta);
	}

	@Override
	public double getLightSourcePhi() {
	    return idata.getLightSourcePhi();
	}

	@Override
	public double getLightSourceTheta() {
	    return idata.getLightSourceTheta();
	}

	@Override
	public void setLightSource(double phi, double theta) {
	    idata.setLightSource(phi, theta);
	}

	@Override
	public double getColorFactor() {
	    return idata.getColorFactor();

	}

	@Override
	public void setColorFactor(double factor) {
	    idata.setColorFactor(factor);
	}

	@Override
	public double getNormalFactor() {
	    return idata.getNormalFactor();

	}

	@Override
	public void setNormalFactor(double factor) {
	    idata.setNormalFactor(factor);
	}

	@Override
	public Color getBacksideColor() {
	    return idata.getBacksideColor();
	}

	@Override
	public void setBacksideColor(Color c) {
	    idata.setBacksideColor(c);
	}

	@Override
	public Color getEdgeColor() {
	    return idata.getEdgeColor();
	}

	@Override
	public void setEdgeColor(Color c) {
	    idata.setEdgeColor(c);
	}

	@Override
	public Color getDefaultSegmentColor() {
	    return idata.getDefaultSegmentColor();
	}

	@Override
	public void setDefaultSegmentColor(Color c) {
	    idata.setDefaultSegmentColor(c);
	}

	@Override
	public Color getDefaultBacksideSegmentColor() {
	    return idata.getDefaultBacksideSegmentColor();
	}

	@Override
	public void setDefaultBacksideSegmentColor(Color c) {
	    idata.setDefaultBacksideSegmentColor(c);
	}

	@Override
	public void reset() {
	    idata.reset();
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
		ProtectionDomain domain = new ProtectionDomain(null,
							       permissions);
		context =
		    new AccessControlContext(new ProtectionDomain[]{domain});
	    }
	}

	/**
	 * Write the image to a file given a file name.
	 * The type parameters are strings naming the file format as used
	 * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
	 * @param type the file type
	 * @param name the file name
	 * @exception IOException an error occurred during writing
	 */
	public void write(final String type, final String name)
	    throws IOException
	{
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null && name != null) {
		sm.checkWrite(name);
	    }
	    try {
		initACC();
		AccessController.doPrivileged
		    (new PrivilegedExceptionAction<Void>() {
			public Void run() throws IOException {
			    if (!ImageIO.write(image, type, new File(name))) {
				throw new IOException
				    (errorMsg("cannotWriteImageType", type));
			    }
			    return null;
			}
		    }, context);
	    } catch (PrivilegedActionException e) {
		throw (IOException) e.getException();
	    }
	}

	/**
	 * Write the image to a file.
	 * The type parameters are strings naming the file format as used
	 * by the class {@link javax.imageio.ImageIO javax.imageio.ImageIO}
	 * @param type the file type
	 * @param file the file to write
	 * @exception IOException an error occurred during writing
	 */
	public void write(final String type, final File file) throws IOException {
	    String name = (file != null? file.getPath(): null);
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null && name != null) {
		sm.checkWrite(name);
	    }
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
				    (errorMsg("cannotWriteImageType", type));
			    }
			    return null;
			}
		    }, context);
	    } catch (PrivilegedActionException e) {
		throw (IOException) e.getException();
	    }
	}

	/**
	 * Write the image to a file provided by a file accessor.
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
				    (errorMsg("cannotWriteImageType", type));
			    }
			    return null;
			}
		    }, context);
	    } catch (PrivilegedActionException e) {
		throw (IOException) e.getException();
	    }
	}

	/**
	 * Write the image to an output stream.
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
				    (errorMsg("cannotWriteImageTypeOS", type));
			    }
			    return null;
			}
		    }, context);
	    } catch (PrivilegedActionException e) {
		throw (IOException) e.getException();
	    }
	}

	/**
	 * Write the image to its OutputStreamGraphics.
	 * @exception IOException an IO error occurred.
	 */
	public void write() throws IOException {
	    if (osg != null) {
		osg.imageComplete();
	    }
	}
    }

    /**
     * Data used by a Model3D.Image instance to render an instance of Model3D.
     * The image described by this interface will have a height and width
     * given by the methods {@link ImageData#getHeight()} and
     * {@link ImageData#getWidth()}. One can specify an origin (e.g., by
     * calling (@link ImageData#setOrigin(double,double)} or related
     * methods) to indicate where a 3-D model's point (0.0, 0.0, 0.0)
     * (after rotations and translations) may appear. This origin is specified
     * using the Java convention where increasing X values move right and
     * increasing Y values move down. The model can also be scaled by
     * calling {ImageData#setScaleFactor(double)}. The scaling will be done
     * so that the model's point (0.0, 0.0, 0.0) will appear at the origin
     * for all scale factors.
     * <P>
     * To provide different views of a model, the model's coordinate system
     * can be transformed - the behavior specified above applies to the
     * the transformed coordinate system.  This transformation is specified
     * by the methods
     * {@link ImageData#setRotationOrigin(double,double,double)},
     * {@link ImageData#setRotationOrigin(double,double,double,boolean)},
     * {@link ImageData#setCoordRotation(double,double,double)};
     * {@link ImageData#setTranslation(double, double)}
     * <P>
     * To make the model appear to be three dimensional, the brightness
     * of the surface depends on the angle the surface makes with the
     * direction of a light source.  The light source can be positioned
     * by calling {@link ImageData#setLightSource(double,double)}.
     * To help distinguish parallel faces, the method
     * {@link ImageData#setColorFactor(double)} can be used. This
     * will configure the image rendering so that more distant areas
     * are dimmer.  Z-order issues may impact rendering. The method
     * {@link ImageData#setDelta(double)} can be used to partition
     * large triangles into a number of smaller one.
     * <P>
     * One can specify colors various features such as
     * lines showing the triangles that make up model, the interior (or 'back')
     * side of a triangles (which should never by visible), etc.  The
     * methods used for this purpose are
     * {@link ImageData#setBacksideColor(Color)},
     * {@link ImageData#setEdgeColor(Color)},
     * {@link ImageData#setDefaultSegmentColor(Color)}, and
     * {@link ImageData#setDefaultBacksideSegmentColor(Color)}.
     * <P>
     * Finally, while flexible, adjusting all the coordinate transformations
     * can be tedious.  For the common case, one
     * would use the {@link ImageData#setCoordRotation(double,double,double)}
     * to orient the view of a model, and a {@link Model3D} method named
     * setImageParameters to scale and translate a model to match the
     * available space that an image has.  These methods are
     * {@link Model3D#setImageParameters(Model3D.ImageData)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double,double)}, and
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double,double,boolean)}.
     */
    public static interface ImageData {

	/**
	 * Get the width of an image as an integer.
	 * @return the width of the image
	 */
	int getWidth();

	/**
	 * Get the height of an image as an integer.
	 * @return the height of the image
	 */
	int getHeight();

	/**
	 * Get the width of an image .
	 * @return the width in image user-space coordinates
	 */
	float getFloatWidth();
	/**
	 * Get the height of an image.
	 * @return the height in  user-space coordinates
	 */
	float getFloatHeight();
	/**
	 * Set the scale factor.
	 * A rendered model will be scaled by this amount. When the scale
	 * factor is 1.0, a unit distance in the model is a unit distance
	 * in the image's user space (e.g. 1/72 of an inch on a display).
	 * The scale factor is applied after any rotation or translation.
	 * The coordinate whose post-rotation and post-translation value
	 * is (0.0, 0.0, 0.0) will appear at the image's origin after
	 * scaling.
	 * @return the scale factor
	 */
	double getScaleFactor();

	/**
	 * Set the scale factor.
	 * A rendered model will be scaled by this amount. When the scale
	 * factor is 1.0, a unit distance in the model is a unit distance
	 * in the image's user space (e.g. 1/72 of an inch on a display).
	 * The scale factor is applied after any rotation or translation.
	 * The coordinate whose post-rotation and post-translation value
	 * is (0.0, 0.0, 0.0) will appear at the image's origin after
	 * scaling.
	 * @param scaleFactor the scale factor
	 */
	void setScaleFactor(double scaleFactor);

	/**
	 * Get the X coordinate of the origin.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * Note that user space follows the standard Java convention in
	 * which increasing X values go 'right' on an image and
	 * increasing Y values do 'down'.
	 * @return the X coordinate of the origin in the image's user space
	 */
	float getXOrigin();

	/**
	 * Get the Y coordinate of the origin
	 * Note that user space follows the standard Java convention in
	 * which increasing X values go 'right' on an image and
	 * increasing Y values do 'down'.
	 * @return the Y coordinate of the origin in the image's user space
	 */
	float getYOrigin();

	/**
	 * Set the origin given integer coordinates.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * Note that user space follows the standard Java convention in
	 * which increasing X values go 'right' on an image and
	 * increasing Y values do 'down'.
	 * @param x the X coordinate in the image's user space
	 * @param y the Y coordinate in the image's user space
	 */
	void setOrigin(int x, int y);
	/**
	 * Set the origin give double-precision coordinates
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * Note that user space follows the standard Java convention in
	 * which increasing X values go 'right' on an image and
	 * increasing Y values do 'down'.
	 * @param x the X coordinate in the image's user space
	 * @param y the Y coordinate in the image's user space
	 */
	void setOrigin(double x, double y);
	/**
	 * Set the origin given floating-point coordinates.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * Note that user space follows the standard Java convention in
	 * which increasing X values go 'right' on an image and
	 * increasing Y values do 'down'.
	 * @param x the user-space x-coordinate of the origin
	 * @param y the user-space y-coordinate of the origin
	 */
	void setOrigin(float x, float y);
	/**
	 * Set the origin given an integer offset.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * When the offset is non-negative, the offset gives the
	 * distance in user-space coordinates, for both the x coordinate
	 * and the y coordinate, from the lower left corner of the
	 * image to the position of the origin.  When negative, the
	 * absolute value gives the distances from the upper right corner
	 * of the image.
	 * @param offset the offset
	 */
	void setOrigin (int offset);
	/**
	 * Set the origin given a double-precision offset.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * When the offset is non-negative, the offset gives the
	 * distance in user-space coordinates, for both the x coordinate
	 * and the y coordinate, from the lower left corner of the
	 * image to the position of the origin.  When negative, the
	 * absolute value gives the distances from the upper right corner
	 * of the image.
	 * @param offset the offset
	 */
	void setOrigin (double offset);
	/**
	 * Set the origin given an offset.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * When the offset is non-negative, the offset gives the
	 * distance in user-space coordinates, for both the x coordinate
	 * and the y coordinate, from the lower left corner of the
	 * image to the position of the origin.  When negative, the
	 * absolute value gives the distances from the upper right corner
	 * of the image.
	 * @param offset the offset
	 */
	void setOrigin (float offset);

	/**
	 * Set the origin given a double-precision fractional offset.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * The fractional offset sets the offset to the value of offset
	 * multiplied by the width and height for the x coordinate and
	 * y coordinate respectively.  If the offset is positive, the
	 * offset is from the lower-left corner of an image. If negative,
	 * the offset is from the upper right corner.
	 * @param offset the fractional offset in the range (-1.0, 1.0)
	 */
	void setOriginByFraction(double offset);

	/**
	 * Set the origin given a single-precision fractional offset.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * The fractional offset sets the offset to the value of offset
	 * multiplied by the width and height for the x coordinate and
	 * y coordinate respectively.  If the offset is positive, the
	 * offset is from the lower-left corner of an image. If negative,
	 * the offset is from the upper right corner.
	 * @param offset the fractional offset in the range (-1.0, 1.0)
	 */
	void setOriginByFraction (float offset);

	/**
	 * Set the origin to a default value.
	 * The origin is the point on the image that will correspond to
	 * points in the model for which x = 0.0 and y = 0.0.
	 * The default will place the origin in the center of an image.
	 */
	void setOrigin();

	/**
	 * Get the Eulerian angle phi for a coordinate rotation.
	 * A coordinate rotation and translation refers to a new
	 * position and orientation of the coordinate system assuming
	 * the model is held stationary. The model will appear to
	 * rotate and translate in the opposite direction from that
	 * specified by these Eulerian angles.
	 * @return the value of phi in radians
	 * @see #setCoordRotation(double,double,double)
	 */
	double getPhi();

	/**
	 * Get the Eulerian angle theta for a coordinate rotation.
	 * A coordinate rotation and translation refers to a new
	 * position and orientation of the coordinate system assuming
	 * the model is held stationary. The model will appear to
	 * rotate and translate in the opposite direction from that
	 * specified by these Eulerian angles.
	 * @return the value of theta in radians
	 * @see #setCoordRotation(double,double,double)
	 */
	double getTheta();

	/**
	 * Get the Eulerian angle psi for a coordinate rotation.
	 * A coordinate rotation and translation refers to a new
	 * position and orientation of the coordinate system assuming
	 * the model is held stationary. The model will appear to
	 * rotate and translate in the opposite direction from that
	 * specified by these Eulerian angles.
	 * @return the value of psi in radians
	 * @see #setCoordRotation(double,double,double)
	 */
	double getPsi();

	/**
	 * Rotate the Model3D coordinates.
	 * When all values are zero, the original z axis is perpendicular to
	 * an image, the original x axis is horizontal, and the original
	 * y axis is vertical, all following the standard mathematical
	 * convention that the x axis goes from left to right, the y axis
	 * goes from bottom to top. The z axis points towards the viewer.
	 * <P>
	 * The Eulerian angles follow the convention used in Goldstein,
	 * "Classical Mechanics": phi is the angle the x-axis rotates
	 * with positive values of phi indicating a counter-clockwise
	 * rotation when viewed from positive values of z towards the
	 * plane in which z=0.  Theta then indicates a rotation about
	 * the new x axis, and psi indicates a rotation about the final
	 * z axis.
	 * <P>
	 * Note that the coordinate system is being rotated, not the
	 * object being displayed, and that the rotation applies from
	 * the original axes, as opposed to being applied incrementally.
	 * @param phi the Eulerian angle phi in radians
	 * @param theta the Eulerian angle theta in radians
	 * @param psi the Eulerian angle psi in radians
	 */
	void setCoordRotation(double phi, double theta, double psi);


	/**
	 * Get the X coordinate of the origin/target point for a coordinate
	 * rotation.
	 * This is the first argument to the most recent call to
	 * {@link #setRotationOrigin(double,double,double)} or
	 * {@link #setRotationOrigin(double,double,double,boolean)}.
	 *
	 * @return the X coordinate in the model's coordinate system
	 */
	double getRotationXOrigin();

	/**
	 * Get the Y coordinate of the origin/target point for a coordinate
	 * rotation.
	 * This is the first argument to the most recent call to
	 * {@link #setRotationOrigin(double,double,double)} or
	 * {@link #setRotationOrigin(double,double,double,boolean)}.
	 * @return the Y coordinate in the model's coordinate system
	 */
	double getRotationYOrigin();

	/**
	 * Get the Z coordinate of the origin/target point for a coordinate
	 * rotation.
	 * This is the second argument to the most recent call to
	 * {@link #setRotationOrigin(double,double,double)} or
	 * {@link #setRotationOrigin(double,double,double,boolean)}.
	 * @return the Z coordinate in the model's coordinate system
	 */
	double getRotationZOrigin();

	/**
	 * Determine if the transformed origin for (0,0,0) will be moved to
	 * the coordinate origin after the rotation is completed. This is
	 * the value of the last argument to the most recent call to
	 * {@link #setRotationOrigin(double,double,double,boolean)}, which
	 * is also called by the three-argument method
	 * {@link #setRotationOrigin(double,double,double)} with a final
	 * argument of <code>false</code>.
	 * When false, the rotation occurs about a specified point.
	 * When true, the rotation in effect occurs around (0, 0, 0) and
	 * then (0, 0, 0) is moved to the rotation origin.
	 * @return true if it will be moved; false if not
	 * @see #setRotationOrigin(double,double,double,boolean)
	 */
	boolean moveOriginAfterRotation();

	/**
	 * Move the rotation origin for a coordinate rotation.
	 * The default performs a coordinate rotation about the point
	 * (0,0,0).  For another value, the point (x, y, z) before the
	 * rotation will have the same value in the new coordinate system:
	 * (x,y,z) = (x',y',z').
	 * The x, y, and z arguments refer to the model's coordinate
	 * system.
	 * @param x the x coordinate about which to perform the rotation
	 * @param y the y coordinate about which to perform the rotation
	 * @param z the z coordinate about which to perform the rotation
	 */
	void setRotationOrigin(double x, double y, double z);
	/**
	 * Move the rotation origin for a coordinate rotation with options.
	 * Setting the 'move' argument to true is useful when you want to
	 * rotate an object and place the previous origin at a specific
	 * location.
	 * The x, y, and z arguments refer to the model's coordinate
	 * system.
	 * @param x the x coordinate about which to perform the rotation
	 * @param y the y coordinate about which to perform the rotation
	 * @param z the z coordinate about which to perform the rotation
	 * @param move false if (x,y,z) will have the same value after the
	 *        rotation; true if the point (0,0,0) in the old coordinate
	 *        system will be mapped to the point (x,y,z) in the new
	 *        coordinate system after the rotation is complete.
	 * @see #moveOriginAfterRotation()
	 */
	void setRotationOrigin(double x, double y, double z,
				      boolean move);

	/**
	 * Get the coordinate translation in the X direction that will be
	 * applied after other coordinate transformations are completed.
	 * (Note: because the coordinate system is being translated, objects
	 * will appear to move in the opposite direction).
	 * @return the translation in the X direction
	 */
	double getXTranslation();

	/**
	 * Get the coordinate translation in the Y direction that will be
	 * applied after other coordinate transformations are completed.
	 * (Note: because the coordinate system is being translated, objects
	 * will appear to move in the opposite direction).
	 * @return the translation in the Y direction
	 */
	double getYTranslation();

	/**
	 * Specify translation after coordinate changes.
	 * After other coordinate transformations are complete,
	 * the coordinate system in which a Model3D object is shown
	 * may be translated by an amount x in the x direction and an
	 * amount y in the y direction. The object displayed will appear
	 * to move in the opposite direction.
	 * @param x the x-axis translation
	 * @param y the y-axis translation
	 */
	void setTranslation(double x, double y);

	/**
	 * Get the minimum triangle size for rendering.
	 * The size is measured in the x, y, and z directions. Larger
	 * triangles will be split into some number of smaller ones.
	 * Triangles will not be split if the value is zero.
	 * @return the minimum size
	 * @see #setDelta(double)
	 */
	double getDelta();

	/**
	 * Set the minimum triangle size for rendering.
	 * The value of this parameter, if nonzero, specifies the
	 * maximum dimensions of a triangle that is to be rendered
	 * in the x, y, and z directions.  If a triangle is larger, it
	 * is partitioned into a smaller set of triangles. This
	 * partitioning applies only during rendering. Setting the
	 * minimum size to a small, nonzero value can improve rendering
	 * by making the calculation of which triangle is in front of
	 * others more reliable.  Too small a value, however, may slow
	 * rendering considerably.  Units are those used for a
	 * triangle's X, Y, and Z coordinates.
	 * @param delta the triangle dimension limit; zero to indicate
	 *        that triangles should not be split
	 */
	void setDelta(double delta);

	/**
	 * Get the light-source parameter phi.
	 * The parameter partially determines the direction of the light
	 * source as described in the documentation for
	 * {@link #setLightSource(double,double) setLightSource}.
	 * @return the light-source parameter phi in radians
	 * @see #setLightSource(double,double)
	 */
	double getLightSourcePhi();

	/**
	 * Get the light-source parameter theta.
	 * The parameter partially determines the direction of the light
	 * source as described in the documentation for
	 * {@link #setLightSource(double,double) setLightSource}.
	 * @return the light-source parameter theta in radians
	 * @see #setLightSource(double,double)
	 */
	double getLightSourceTheta();

	/**
	 * Sets the direction to a light source. The default direction,
	 * in which theta=0, corresponds to a light source at infinity aimed
	 * vertically downwards towards the x-y plane.
	 * The brightness used for a triangle varies as the cosine of the
	 * angle between a vector pointing in the direction of the light
	 * source and a vector perpendicular to the plane of the triangle.
	 * @param phi the angle in radians about the z axis
	 *        of the plane containing  the z axis and a unit vector
	 *        pointing towards the light source.  If phi is zero, this
	 *        plane includes the x axis and for non-zero theta, the unit
	 *        vector will have a positive x component.
	 * @param theta the angle in radians between a unit vector
	 *        pointing towards the light source makes and with a
	 *        vector parallel to the z axis.
	 */
	void setLightSource(double phi, double theta);

	/**
	 * Get the color factor.
	 * After coordinate transformations, the red, blue, and green
	 * components of the color of an object will be scaled depending
	 * on its height so that, for the same orientation, triangles at
	 * with the lowest values of 'z' will be dimmer than those with
	 * the maximum value of 'z' by this factor.  A value of zero
	 * indicates that the color factor will be ignored. A value
	 * larger than 1.0 will result in the multiple Z values being
	 * rendered in black, but with more color separation for higher
	 * Z values.
	 * @return the color factor
	 * @see #setColorFactor(double)
	 */
	double getColorFactor();

	/**
	 * Set the color factor.
	 * After coordinate transformations, the red, blue, and green
	 * components of the color of an object will be scaled depending
	 * on its height so that, for the same orientation, triangles at
	 * with the lowest values of 'z' will be dimmer than those with
	 * the maximum value of 'z' by this factor.  If the value is 1.0,
	 * the smallest z value of the object will be rendered as black.
	 * A value of 0.0 indicates that the color factor will be ignored.
	 * A value larger than 1.0 will result in the multiple Z
	 * values being rendered in black, but with more color
	 * separation for higher Z values.
	 * <P>
	 * This is intended to help emphasize differences along the 'z'
	 * axis that would normally be hard to discern visually.
	 * @param factor the color factor given as a non-negative number
	 */
	void setColorFactor(double factor);

	/**
	 * Get the normal factor
	 * After a coordinate transformation, the normal factor is
	 * used to reduce the color factor towards 0.0 when a surface
	 * segment' is not aligned with the Z axis.  If the Z
	 * component of the segment's normal vector is nz and the
	 * normal factor is larger than 0.0, the color factor is
	 * multiplied by exp(-(1.0-nz)/normalFactor). Since the normal
	 * vector has a length of 1.0, when the normal vector does not
	 * point in the positive Z direction, nz will be less than 1.0
	 * and the color factor will be reduced.
	 * @return the normal factor; 0.0 if there is none
	 */
	double getNormalFactor();


	/**
	 * Set the normal factor.
	 * After a coordinate transformation, the normal factor is
	 * used to reduce the color factor towards 0.0 when a surface
	 * segment' is not aligned with the Z axis.  If the Z
	 * component of the segment's normal vector is nz and the
	 * normal factor is larger than 0.0, the color factor is
	 * multiplied by exp(-(1.0-nz)/normalFactor). Since the normal
	 * vector has a length of 1.0, when the normal vector does not
	 * point in the positive Z direction, nz will be less than 1.0
	 * and the color factor will be reduced.
	 * @param factor the normal factor; 0.0 or negative if there is none
	 */
	void setNormalFactor(double factor);

	/**
	 * Get the color for the the side of a triangle that should not
	 * be visible due to being on the inside of a closed manifold.
	 * @return the color
	 */
	Color getBacksideColor();

	/**
	 * Set the color for the side of a triangle that should not
	 * be visible due to being on the inside of a closed manifold.
	 * This is useful for debugging a model: one class of errors
	 * results in a triangle having the wrong orientation, and setting
	 * this parameter can help indicate where the problem is in the
	 * model.
	 * @param c the color; null if a color is not set
	 */
	void setBacksideColor(Color c);

	/**
	 * Get the color used to indicate the edges of a triangle
	 * @return the color or null to indicate that edges will not
	 *         be shown
	 */
	Color getEdgeColor();

	/**
	 * Set the color to use to use to draw the edges of triangles.
	 * @param c the color; null if edges should not be displayed
	 */
	void setEdgeColor(Color c);

	/**
	 * Get the color to use for line segments explicitly added
	 * or associated with triangles added for rendering.
	 * The phrase "[line segments] associated with triangles"
	 * refers to the additional edges of triangles that were added
	 * during the rendering process to minimize z-order issues
	 * that can occur when at least one edge of a triangle is
	 * abnormally large. Such triangles may be added when
	 * {@link Model3D.ImageData#setDelta(double)} is called with
	 * a non-zero argument.
	 * @return the color; null if the line segments should not be shown
	 */
	Color getDefaultSegmentColor();

	/**
	 * Set the color to use for line segments explicitly added or
	 * associated with triangles added for rendering.
	 * The phrase "[line segments] associated with triangles"
	 * refers to the additional edges of triangles that were added
	 * during the rendering process to minimize z-order issues
	 * that can occur when at least one edge of a triangle is
	 * abnormally large. Such triangles may be added when
	 * {@link Model3D.ImageData#setDelta(double)} is called with
	 * a non-zero argument.
	 * @param c the color; null if line segments should not be displayed
	 */
	void setDefaultSegmentColor(Color c);

	/**
	 * Get the default backside color to use for line segments associated
	 * with triangles added for rendering.
	 * The phrase "[line segments] associated with triangles"
	 * refers to the additional edges of triangles that were added
	 * during the rendering process to minimize z-order issues
	 * that can occur when at least one edge of a triangle is
	 * abnormally large. Such triangles may be added when
	 * {@link Model3D.ImageData#setDelta(double)} is called with
	 * a non-zero argument.
	 * @return the color; null if the line segments should not be shown
	 */
	Color getDefaultBacksideSegmentColor();

	/**
	 * Set the default backside color to use for line segments associated
	 * with triangles added for rendering.
	 * The phrase "[line segments] associated with triangles"
	 * refers to the additional edges of triangles that were added
	 * during the rendering process to minimize z-order issues
	 * that can occur when at least one edge of a triangle is
	 * abnormally large. Such triangles may be added when
	 * {@link Model3D.ImageData#setDelta(double)} is called with
	 * a non-zero argument.
	 * @param c the color; null if these line segments should not
	 *       be displayed
	 */
	void setDefaultBacksideSegmentColor(Color c);

	/**
	 * Get the corresponding ImageDataImpl.
	 * @return the corresponding ImageDataImpl
	 */
	ImageDataImpl getImageData();
	/**
	 *  Reset to default values.
	 */
	void reset();
    }

    /**
     * The implementation of ImageData used by Model3D.
     * There are some fields and/or methods that are not visible
     * outside the org.bzdev.p3d package.
     */
    public static class ImageDataImpl  implements ImageData {
	// nothing to do, really, but the interface requires this method.
	public ImageDataImpl getImageData() {return this;}

	RenderList renderList = new RenderList();
	boolean rlistInvalid = true;

	boolean lastScaleXYSet = false;
	double lastScaleX = 1.0;
	double lastScaleY = 1.0;

	/**
	 * Force the scale factor to be recomputed the
	 * next time the Model3D method setImageParameters is called.
	 */
	public void forceScaleChange() {
	    lastScaleXYSet = false;
	}


	public void reset() {
	    renderList.reset();
	    rlistInvalid = true;
	}

	int widthInt;
	int heightInt;

	public int getWidth() {return widthInt;}
	public int getHeight() {return heightInt;}

	float width;
	float height;
	public float getFloatWidth() {return width;}
	public float getFloatHeight() {return height;}

	float xorigin;
	float yorigin;
	double scaleFactor = 1.0;

	public double getScaleFactor() {
	    return scaleFactor;
	}

	public float getXOrigin() {
	    return xorigin;
	}

	public float getYOrigin() {
	    return yorigin;
	}

	public void setScaleFactor(double scaleFactor) {
	    rlistInvalid = true;
	    this.scaleFactor = scaleFactor;
	}

	public void setOrigin(int x, int y) {
	    setOrigin((float)x, (float)y);
	}
	public void setOrigin(double x, double y) {
	    setOrigin((float)x, (float)y);
	}

	public void setOrigin(float x, float y) {
	    rlistInvalid = true;
	    xorigin = x;
	    yorigin = y;
	}

	public void setOrigin(int offset) {
	    setOrigin((float)offset);
	}

	public void setOrigin(double offset) {
	    setOrigin((float)offset);
	}

	public void setOrigin(float offset) {
	    rlistInvalid = true;
	    if (offset >= 0.0) {
		xorigin = offset;
		yorigin = getFloatHeight() - offset;
	    } else {
		xorigin = getFloatWidth() + offset;
		yorigin = -offset;
	    }
	}

	public void setOriginByFraction(double offset) {
	    setOriginByFraction((float)offset);
	}

	public void setOriginByFraction (float offset) {
	    rlistInvalid = true;
	    if (offset >= 1.0 || offset <= -1.0) {
		throw new IllegalArgumentException
		    (errorMsg("argOutOfRange1", offset));
	    }
	    if (offset == 0.0F) {
		xorigin = 0;
		yorigin = getFloatHeight();
	    } else if (offset > 0) {
		xorigin = (offset * getFloatWidth());
		yorigin = ((1.0F - offset) * getFloatHeight());

	    } else {
		xorigin = ((1.0F + offset) * getFloatWidth());
		yorigin = -(offset * getFloatHeight());
	    }
	}

	public void setOrigin() {
	    rlistInvalid = true;
	    float width = getFloatWidth();
	    float height = getFloatHeight();
	    xorigin = width/2.0F;
	    yorigin = height/2.0F;
	}


	// Model Transformations for images.
	double eulerPhi = 0.0;
	double eulerTheta = 0.0;
	double eulerPsi = 0.0;
	double[][] matrix = new double[3][3]; // coord rotation for rendering

	public double getPhi() {
	    return eulerPhi;
	}

	public double getTheta() {
	    return eulerTheta;
	}

	public double getPsi() {
	    return eulerPsi;
	}

	public void setCoordRotation(double phi, double theta, double psi) {
	    rlistInvalid = true;

	    eulerPhi = phi;
	    eulerTheta = theta;
	    eulerPsi = psi;

	    double sin_psi;
	    double cos_psi;
	    double sin_phi;
	    double cos_phi;
	    double sin_theta;
	    double cos_theta;

	    if (psi == 0.0) {
		sin_psi = 0.0;
		cos_psi = 1.0;
	    } else if (psi == Math.PI/2.0 || psi == -(Math.PI * 1.5)) {
		sin_psi = 1.0;
		cos_psi = 0.0;
	    } else if (psi == Math.PI || psi == -Math.PI) {
		sin_psi = 0.0;
		cos_psi = -1.0;
	    } else if (psi == Math.PI*1.5 || psi == -(Math.PI/2.0)) {
		sin_psi = -1.0;
		cos_psi = 0.0;
	    } else {
		sin_psi = Math.sin(psi);
		cos_psi = Math.cos(psi);
	    }
	    if (phi == 0.0) {
		sin_phi = 0.0;
		cos_phi = 1.0;
	    } else if (phi == Math.PI/2.0 || phi == -(Math.PI * 1.5)) {
		sin_phi = 1.0;
		cos_phi = 0.0;
	    } else if (phi == Math.PI || phi == -Math.PI) {
		sin_phi = 0.0;
		cos_phi = -1.0;
	    } else if (phi == Math.PI*1.5 || phi == -(Math.PI/2.0)) {
		sin_phi = -1.0;
		cos_phi = 0.0;
	    } else {
		sin_phi = Math.sin(phi);
		cos_phi = Math.cos(phi);
	    }
	    if (theta == 0.0) {
		sin_theta = 0.0;
		cos_theta = 1.0;
	    } else if (theta == Math.PI/2.0 || theta == -(Math.PI * 1.5)) {
		sin_theta = 1.0;
		cos_theta = 0.0;
	    } else if (theta == Math.PI || theta == -Math.PI) {
		sin_theta = 0.0;
		cos_theta = -1.0;
	    } else if (theta == Math.PI*1.5 || theta == -(Math.PI/2.0)) {
		sin_theta = -1.0;
		cos_theta = 0.0;
	    } else {
		sin_theta = Math.sin(theta);
		cos_theta = Math.cos(theta);
	    }
	    matrix[0][0] = cos_psi * cos_phi - cos_theta * sin_phi* sin_psi;
	    matrix[0][1] = cos_psi * sin_phi + cos_theta * cos_phi * sin_psi;
	    matrix[0][2] = sin_theta * sin_psi;
	    matrix[1][0] = -sin_psi * cos_phi - cos_theta * sin_phi * cos_psi;
	    matrix[1][1] = -sin_psi * sin_phi + cos_theta * cos_phi * cos_psi;
	    matrix[1][2] = sin_theta * cos_psi;
	    matrix[2][0] = sin_theta * sin_phi;
	    matrix[2][1] = - sin_theta*cos_phi;
	    matrix[2][2] = cos_theta;
	    adjustOriginAR();
	}

	double xoriginBR;
	double yoriginBR;
	double zoriginBR;
	boolean rotMoveOrigin = false;
	double xoriginAR;
	double yoriginAR;
	double zoriginAR;

	public double getRotationXOrigin() {
	    return xoriginBR;
	}

	public double getRotationYOrigin() {
	    return yoriginBR;
	}

	public double getRotationZOrigin() {
	    return zoriginBR;
	}

	public boolean moveOriginAfterRotation() {
	    return rotMoveOrigin;
	}

	public void setRotationOrigin(double x, double y, double z) {
	    setRotationOrigin(x, y, z, false);
	}

	public void setRotationOrigin(double x, double y, double z,
				      boolean move)
	{
	    rlistInvalid = true;
	    xoriginBR = x;
	    yoriginBR = y;
	    zoriginBR = z;
	    rotMoveOrigin = move;
	    adjustOriginAR();
	}

	private void adjustOriginAR() {
	    if (rotMoveOrigin) {
		xoriginAR = 0.0;
		yoriginAR = 0.0;
		zoriginAR = 0.0;
	    } else {
		xoriginAR = matrix[0][0]*xoriginBR
		    + matrix[0][1]*yoriginBR + matrix[0][2]*zoriginBR;
		yoriginAR = matrix[1][0]*xoriginBR
		    + matrix[1][1]*yoriginBR + matrix[1][2]*zoriginBR;
		zoriginAR = matrix[2][0]*xoriginBR
		    + matrix[2][1]*yoriginBR + matrix[2][2]*zoriginBR;
	    }
	    /*
	      System.out.println("xoriginAR = " + xoriginAR
	      + ", yoriginAR = " + yoriginAR
	      + ", zoriginAR = " + zoriginAR);
	    */
	}

	double xtranslation;
	double ytranslation;

	public double getXTranslation() {
	    return xtranslation;
	}

	public double getYTranslation() {
	    return ytranslation;
	}
    
	public void setTranslation(double x, double y) {
	    rlistInvalid = true;
	    xtranslation = x;
	    ytranslation = y;
	}


	double lightsourceNx = 0.0;
	double lightsourceNy = 0.0;
	double lightsourceNz = 1.0;

	// if nonzero, max 'z' difference before splitting a triangle into
	// multiple polygons.
	double delta = 0.0;

	public double getDelta() {
	    return delta;
	}


	public void setDelta(double delta) {
	    rlistInvalid = true;
	    this.delta = delta;
	}

	// only used for access methods.
	double lsphi;
	double lstheta;

	public double getLightSourcePhi() {
	    return lsphi;
	}
	public double getLightSourceTheta() {
	    return lstheta;
	}

	public void setLightSource(double phi, double theta) {
	    rlistInvalid = true;
	    double sin_psi;
	    double cos_psi;
	    double sin_phi;
	    double cos_phi;
	    double sin_theta;
	    double cos_theta;

	    lsphi = phi;
	    lstheta = theta;
	    if (phi == 0.0) {
		sin_phi = 0.0;
		cos_phi = 1.0;
	    } else if (phi == Math.PI/2.0) {
		sin_phi = 1.0;
		cos_phi = 0.0;
	    } else if (phi == Math.PI) {
		sin_phi = 0.0;
		cos_phi = -1.0;
	    } else if (phi == Math.PI*1.5) {
		sin_phi = -1.0;
		cos_phi = 0.0;
	    } else {
		sin_phi = Math.sin(phi);
		cos_phi = Math.cos(phi);
	    }
	    if (theta == 0.0) {
		sin_theta = 0.0;
		cos_theta = 1.0;
	    } else if (theta == Math.PI/2.0) {
		sin_theta = 1.0;
		cos_theta = 0.0;
	    } else if (theta == Math.PI) {
		sin_theta = 0.0;
		cos_theta = -1.0;
	    } else if (theta == Math.PI*1.5) {
		sin_theta = -1.0;
		cos_theta = 0.0;
	    } else {
		sin_theta = Math.sin(theta);
		cos_theta = Math.cos(theta);
	    }

	    lightsourceNz = cos_theta;
	    lightsourceNx = sin_theta * cos_phi;
	    lightsourceNy = sin_theta * sin_phi;
	}

	Color backsideColor = null;
	Color triangleColor = new Color(200, 200, 200);
	Color edgeColor = null;

	double  colorFactor = 0.0;
	double normalFactor = 0.0;

	public Color getBacksideColor() {
	    return backsideColor;
	}
	public Color getTriangleColor() {
	    return triangleColor;
	}
	public Color getEdgeColor() {
	    return edgeColor;
	}
	public double getColorFactor() {
	    return colorFactor;
	}

	public void setColorFactor(double factor)
	{
	    rlistInvalid = true;
	    colorFactor = factor;
	}

	public double getNormalFactor() {
	    return normalFactor;
	}

	public void setNormalFactor(double factor) {
	    rlistInvalid = true;
	    if (factor < 0.0) factor = 0.0;
	    normalFactor = factor;
	}

	public void setBacksideColor(Color c) {
	    rlistInvalid = true;
	    backsideColor = c;
	}

	public void setEdgeColor(Color c) {
	    rlistInvalid = true;
	    edgeColor = c;
	}

	Color segmentColor = Color.blue;

	public Color getDefaultSegmentColor() {
	    return segmentColor;
	}



	public void setDefaultSegmentColor(Color c) {
	    rlistInvalid = true;
	    segmentColor = c;
	}

	Color backsideSegmentColor = null;
    
	public Color getDefaultBacksideSegmentColor() {
	    return backsideSegmentColor;
	}


	public void setDefaultBacksideSegmentColor(Color c) {
	    rlistInvalid = true;
	    backsideSegmentColor = c;
	}

	private void initialize() {
	    setOrigin();
	    matrix[0][0] = 1.0;
	    matrix[1][1] = 1.0;
	    matrix[2][2] = 1.0;
	}

	public ImageDataImpl(int width, int height) {
	    this.width = (float) width; this.height = (float)height;
	    this.widthInt = width;
	    this.heightInt = height;
	    initialize();
	}
    }

    /**
     * Image Parameters
     * Instances of Image.ImageParams are created by the methods
     * {@link Model3D#setImageParameters(Model3D.ImageData) setImageParameters(m3d)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double) setImageParameters(m3d, border)},
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double) setImageParameters(m3d, border magnification)},
     * and
     * {@link Model3D#setImageParameters(Model3D.ImageData,double,double,double,double)	 *  setImageParameters}.
     *
     * The methods {@link Model3D.Image#getHeight() getHeight} and
     * {@link Model3D.Image#getWidth() getWidth} return the height and
     * width of the image passed as the first argument to setImageParameters.
     * The methods
     * {@link Model3D.ImageParams#getXOrigin() getXOrigin},
     * {@link Model3D.ImageParams#getYOrigin() getYOrigin},
     * {@link Model3D.ImageParams#getScaleFactor() getScaleFactor},
     * {@link Model3D.ImageParams#getPhi() getPhi},
     * {@link Model3D.ImageParams#getTheta() getTheta},
     * {@link Model3D.ImageParams#getPsi() getPsi},
     * {@link Model3D.ImageParams#getXTranslation() getXTranslation}, and
     * {@link Model3D.ImageParams#getYTranslation() getYTranslation}
     * return the parameters used to configure the image.
     * The methods
     * {@link Model3D.ImageParams#getMinXTranslation() getMinXTranslation},
     * {@link Model3D.ImageParams#getMinYTranslation() getMinYTranslation},
     * {@link Model3D.ImageParams#getMaxXTranslation() getMaxXTranslation}, and
     * {@link Model3D.ImageParams#getMaxYTranslation() getMaxYTranslation}
     * return the minimum and maximum values that
     * {@link Model3D.ImageParams#getXTranslation() getXTranslation} and
     * {@link Model3D.ImageParams#getYTranslation() getYTranslation}
     * can return, with minimum values occurring when
     * {@link Model3D.ImageParams#getViewFractionX() getViewFractionX} or
     * {@link Model3D.ImageParams#getViewFractionY() getViewFractionY} return values of 0.0
     * and with the maximum occurring when
     * {@link Model3D.ImageParams#getViewFractionX() getViewFractionX} or
     * {@link Model3D.ImageParams#getViewFractionY() getViewFractionY} return values of 1.0.
     * The methods
     * {@link Model3D.ImageParams#getBorder() getBorder},
     * {@link Model3D.ImageParams#getMagnification() getMagnification}
     * {@link Model3D.ImageParams#getScrollFractionX getScrollFractionX}, and
     * {@link Model3D.ImageParams#getScrollFractionY getScrollFractionY}
     * return the explicit and default arguments in the call to
     * setImageParameters.
     * The methods
     * {@link Model3D.ImageParams#getLowerBoundX() getLowerBoundX},
     * {@link Model3D.ImageParams#getLowerBoundY() getLowerBoundY},
     * {@link Model3D.ImageParams#getUpperBoundX() getUpperBoundX}, and
     * {@link Model3D.ImageParams#getUpperBoundY() getUpperBoundY} return
     * the lower and upper X and Y values for a bounding box containing
     * the 3D model after rotation and translation.
     * The methods
     * {@link Model3D.ImageParams#getViewFractionX() getViewFractionX} and
     * {@link Model3D.ImageParams#getViewFractionY() getViewFractionY} give
     * the fraction of the 3D model's bounding box (after rotation and
     * translation) that will appear in the image window.
     * Finally the methods
     * {@link Model3D.ImageParams#getShowsAllX() getShowsAllX} and
     * {@link Model3D.ImageParams#getShowsAllY() getShowsAllY} indicate if
     * all the model in each direction is viewable in the image at one time.
     */
    public static class ImageParams {
	float imageHeight, imageWidth;
	float xorigin, yorigin;
	double scaleFactor;
	double phi;
	double theta;
	double psi;
	double xtranslation, ytranslation;
	double minXtranslation, minYtranslation;
	double maxXtranslation, maxYtranslation;
	double border;
	double magnification;
	double minX, maxX;
	double minY, maxY;
	double xfract;
	double yfract;
	double xfractmin2, yfractmin2;
	boolean showsAllX, showsAllY;

	double rotXOrigin;
	double rotYOrigin;
	double rotZOrigin;
	boolean move;
	double delta;
	double lsPhi;
	double lsTheta;

	double colorFactor;
	double normalFactor;

	/**
	 * Get the height of the image associated with these parameters.
	 * @return the image height
	 */
	public float getFloatHeight() {return imageHeight;}

	/**
	 * Get the width of the image associated with these parameters.
	 * @return the image width
	 */
	public float getFloatWidth() {return imageWidth;}

	/**
	 * Get the x origin for the image.
	 * @return the x origin in pixels.
	 */
	public float getXOrigin() { return xorigin;}

	/**
	 * Get the y origin for the image.
	 * @return the y origin in pixels.
	 */
	public float getYOrigin() {return yorigin;}

	/**
	 * Get the scale factor for the image associated with these
	 * parameters.
	 * @return the scale factor
	 */
	public double getScaleFactor() {return scaleFactor;}

	/**
	 * Get the Eulerian angle &phi; for a coordinate rotation.
	 * @return the angle in radians
	 */
	public double getPhi() {return phi;}

	/**
	 * Get the Eulerian angle &theta; for a coordinate rotation.
	 * @return the angle in radians
	 */
	public double getTheta() {return theta;}

	/**
	 * Get the Eulerian angle &psi; for a coordinate rotation.
	 * @return the angle in radians
	 */
	public double getPsi() {return psi;}

	/**
	 * Get the x-axis coordinate translation to apply after
	 * a rotation.
	 * @return the coordinate translation
	 */
	public double getXTranslation() {return xtranslation;}

	/**
	 * Get the y-axis coordinate translation to apply after
	 * a rotation.
	 * @return the coordinate translation
	 */
	public double getYTranslation() {return ytranslation;}

	/**
	 * Get the minimum possible value that getXTranslation can return.
	 * This corresponds to a X scroll fraction of 0.0.
	 * @return the minimum X translation in Model units
	 */
	public double getMinXTranslation() {return minXtranslation;}

	/**
	 * Get the minimum possible value that getYTranslation can return.
	 * This corresponds to a Y scroll fraction of 0.0.
	 * @return the minimum Y translation in Model units
	 */
	public double getMinYTranslation() {return minYtranslation;}

	/**
	 * Get the maximum possible value that getXTranslation can return.
	 * This corresponds to a X scroll fraction of 1.0.
	 * @return the maximum X translation in Model units
	 */
	public double getMaxXTranslation() {return maxXtranslation;}

	/**
	 * Get the maximum possible value that getYTranslation can return.
	 * This corresponds to a Y scroll fraction of 1.0.
	 * @return the maximum Y translation in Model units
	 */
	public double getMaxYTranslation() {return maxYtranslation;}

	/**
	 * Get the size of the desired border for the 3D model used
	 * to create this parameter set.
	 * @return the border length in AWT user-space units
	 */
	public double getBorder() {return border;}

	/**
	 * Get the magnification for the 3D model used
	 * to create this parameter set.
	 * @return the magnification
	 */
	public double getMagnification() {return magnification;}

	/**
	 * Get the smallest value along the X access for the 3D model used
	 * to create this parameter set.
	 * @return the smallest S coordinate in AWT user-space units
	 */
	public double getLowerBoundX() {return minX;}

	/**
	 * Get the smallest value along the Y access for the 3D model used
	 * to create this parameter set.
	 * @return the smallest Y coordinate in AWT user-space units
	 */
	public double getLowerBoundY() {return minY;}

	/**
	 * Get the largest value along the X access for the 3D model used
	 * to create this parameter set.
	 * @return the largest x coordinate in AWT user-space units
	 */
	public double getUpperBoundX() {return maxX;}

	/**
	 * Get the largest value along the Y access for the 3D model used
	 * to create this parameter set.
	 * @return the largest y coordinate in AWT user-space units
	 */
	public double getUpperBoundY() {return maxY;}

	/**
	 * Get the horizontal fraction by which a 3D model was scrolled
	 * after being scaled and magnified.
	 * @return the fraction
	 */
	public double getScrollFractionX() {return xfract;}

	/**
	 * Get the vertical fraction by which a 3D model was scrolled
	 * after being scaled and magnified.
	 * @return the fraction
	 */
	public double getScrollFractionY() {return yfract;}

	/**
	 * Get the horizontal fraction of a 3D model's bounding box 
	 * after rotation for the viewable portion of its image.
	 * This value ignores any portion of an image displayed in the
	 * border area.  If the image is smaller horizontally than the
	 * image (excluding its borders) the value 1.0 is returned.
	 * @return the fraction
	 */
	public double getViewFractionX() {return xfractmin2;}

	/**
	 * Get the vertical fraction of a 3D model's bounding box 
	 * after rotation for the viewable portion of its image.
	 * This value ignores any portion of an image displayed in the
	 * border area.  If the image is smaller vertically than the
	 * image (excluding its borders) the value 1.0 is returned.
	 * @return the fraction
	 */
	public double getViewFractionY() {return yfractmin2;}

	/**
	 * Determine if the image used to create this parameter set
	 * contains the 3D model in the X direction after rotation.
	 * @return true if it does; false otherwise
	 */
	public boolean getShowsAllX() {return showsAllX;}

	/**
	 * Determine if the image used to create this parameter set
	 * contains the 3D model in the Y direction after rotation.
	 * @return true if it does; false otherwise
	 */
	public boolean getShowsAllY() {return showsAllY;}

	/**
	 * Get the X coordinate of the origin about which the coordinate
	 * rotation will be performed.
	 * @return the X coordinate
	 */
	public double getRotationXOrigin() {return rotXOrigin;}

	/**
	 * Get the Y coordinate of the origin about which the coordinate
	 * rotation will be performed.
	 * @return the Y coordinate
	 */
	public double getRotationYOrigin() {return rotYOrigin;}

	/**
	 * Get the Z coordinate of the origin about which the coordinate
	 * rotation will be performed.
	 * @return the Z coordinate
	 */
	public double getRotationZOrigin() {return rotZOrigin;}

	/**
	 * Determine if the transformed origin for (0,0,0) will be moved to
	 * the coordinate origin after the rotation is completed.
	 * @return true if it will be moved; false if not
	 */
	public boolean moveOriginAfterRotation() {return move;}

	/**
	 * Get the minimum triangle size for rendering.
	 * The size is measured in the x, y, and z directions. Larger
	 * triangles will be split into some number of smaller ones.
	 * Triangles will not be split if the value is zero.
	 * @return the minimum size
	 * @see Model3D.ImageData#setDelta(double)
	 */
	public double getDelta() {return delta;}

	/**
	 * Get the light-source parameter phi.
	 * The parameter partially determines the direction of the light
	 * source as described in the documentation for
	 * {@link Model3D.ImageData#setLightSource(double,double) setLightSource}.
	 * @return the light-source parameter phi
	 * @see Model3D.ImageData#setLightSource(double,double)
	 */
	public double getLightSourcePhi() {return lsPhi;}

	/**
	 * Get the light-source parameter theta.
	 * The parameter partially determines the direction of the light
	 * source as described in the documentation for
	 * {@link Model3D.ImageData#setLightSource(double,double) setLightSource}.
	 * @return the light-source parameter theta
	 * @see Model3D.ImageData#setLightSource(double,double)
	 */
	public double getLightSourceTheta() {
	    return lsTheta;
	}

	public double getColorFactor() {
	    return colorFactor;
	}

	public double getNormalFactor() {
	    return normalFactor;
	}

    }

    boolean stackTraceMode = false;
    boolean strict = true;

    // Used for tessellation - we need consistent values at
    // the corners.
    private TreeSet<Double> xCornerCoords = new TreeSet<>();
    private TreeSet<Double> yCornerCoords = new TreeSet<>();
    private TreeSet<Double> zCornerCoords = new TreeSet<>();


    /**
     * Find the values allowed for tessellation that bracket a
     * given value for an X coordinate.
     * If {@link #setULPFactor(double)} was not called or was called
     * with a value of 0.0, the value returned will contain the constants
     * -&infty; and &infty; ({@link Double#NEGATIVE_INFINITY} and
     * {@link Double#POSITIVE_INFINITY} respectively) and will contain one
     * of these if the value of x is above or below the values previously
     * provided when segments were added to the model.
     * <P>
     * This method is provided for debugging.
     * @param x the value of an X coordinate
     * @return an array consisting of the X coordinate below or equal to  x and
     *         the X coordinate above or equal to x.
     */
    public double[] xbracket(double x) {
	Double floor = xCornerCoords.floor(x);
	Double ceiling = xCornerCoords.ceiling(x);
	return new double[] {
	    (floor == null)? Double.NEGATIVE_INFINITY: floor.doubleValue(),
	    (ceiling == null)? Double.POSITIVE_INFINITY: ceiling.doubleValue()
	};
    }

    /**
     * Find the values allowed for tessellation that bracket a
     * given value for a Y coordinate.
     * If {@link #setULPFactor(double)} was not called or was called
     * with a value of 0.0, the value returned will contain the constants
     * -&infty; and &infty; ({@link Double#NEGATIVE_INFINITY} and
     * {@link Double#POSITIVE_INFINITY} respectively) and will contain one
     * of these if the value of y is above or below the values previously
     * provided when segments were added to the model.
     * <P>
     * This method is provided for debugging.
     * @param y the value of a Y coordinate
     * @return an array consisting of the Y coordinate below or equal to  x and
     *         the Y coordinate above or equal to x.
     */
    public double[] ybracket(double y) {
	Double floor = yCornerCoords.floor(y);
	Double ceiling = yCornerCoords.ceiling(y);
	return new double[] {
	    (floor == null)? Double.NEGATIVE_INFINITY: floor.doubleValue(),
	    (ceiling == null)? Double.POSITIVE_INFINITY: ceiling.doubleValue()
	};
    }

    /**
     * Find the values allowed for tessellation that bracket a
     * given value for a Z coordinate.
     * If {@link #setULPFactor(double)} was not called or was called
     * with a value of 0.0, the value returned will contain the constants
     * -&infty; and &infty; ({@link Double#NEGATIVE_INFINITY} and
     * {@link Double#POSITIVE_INFINITY} respectively) and will contain one
     * of these if the value of z is above or below the values previously
     * provided when segments were added to the model.
     * <P>
     * This method is provided for debugging.
     * @param z the value of a Z coordinate
     * @return an array consisting of the Z coordinate below or equal to  x and
     *         the Z coordinate above or equal to x.
     */
    public double[] zbracket(double z) {
	Double floor = zCornerCoords.floor(z);
	Double ceiling = zCornerCoords.ceiling(z);
	return new double[] {
	    (floor == null)? Double.NEGATIVE_INFINITY: floor.doubleValue(),
	    (ceiling == null)? Double.POSITIVE_INFINITY: ceiling.doubleValue()
	};
    }

    /**
     * Find the minimum ratio of the separation between adjacent coordinate
     * values to the ULP value for those coordinates, treated as
     * single-precision values.
     * The coordinates are obtained from tables maintained when
     * {@link #setULPFactor(double)} was called with a positive argument
     * (such an argument's value must be at least 2.0). The value returned
     * can be used as guide for adjusting the value
     * returned by {@link #setULPFactor(double)} by providing an estimate
     * of the closest distance between vertices before tessellation. Values
     * much larger than 1 are an indication that tessellation in not likely
     * to be affected by roundoff errors (assuming {@link #setULPFactor(double)}
     * has been called with a non-zero argument).
     */
    public double findMinULPRatio() {
	double min = Double.POSITIVE_INFINITY;
	List<TreeSet<Double>> list = Arrays.asList(xCornerCoords,
						   yCornerCoords,
						   zCornerCoords);
	for (TreeSet<Double> cornerCoords: list) {
	    double last = Double.NEGATIVE_INFINITY;
	    boolean first = true;
	    for (double value: cornerCoords) {
		if (first) {
		    last = value;
		    first = false;
		} else {
		    double ulp = Math.abs(value) < 1.0? Math.ulp(1.0F):
			Math.ulp((float)value);
		    double delta = value - last;
		    double ratio = delta / ulp;
		    if (ratio < min) min = ratio;
		    last = value;
		}
	    }
	}
	return min;
    }

    private static final int[] patchCornerIndices = {
	0, 9, 12, 36, 45
    };
    private static final int[] ctCornerIndices = {
	0, 9, 27
    };

    private static final int[] cvCornerIndices = {
	0, 9, 12
    };

    private static final int[] triangleCornerIndices = {
	0, 3, 6
    };

    /**
     * Set stack-trace mode.
     * When stack-trace mode is on (true), triangles without an
     * explicit tag are tagged with a stack trace taken at the point
     * when the triangle was created.  The default value is false.
     * <P>
     * When the scrunner command is used, one turns on the necessary
     * permissions by setting the trust level to 1 or 2. For a trust
     * level of 1, one should split a script into pieces and use the
     * -t option for the piece that sets this mode. For a trust level
     * of 0, one will need to set a custom security policy to grant
     * the permission
     * {@link org.bzdev.lang.StackTraceModePermission} for
     * "{@link org.bzdev.p3d.Model3D}".
     * and use the '-t' option. Unfortunately, there is currently no way
     * of obtaining the line number and file name for the position in a
     * script when a statement in a scripting language was used to create
     * one or more triangles.
     * @param mode true to turn on stack-trace mode; false to turn it off
     * @exception SecurityException a security manager was installed
     *            and the permission
     *            org.bzdev.lang.StackTraceModePermission was not
     *            granted for the class org.bzdev.p3d.Model3D
     */
    public void setStackTraceMode(boolean mode) {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new StackTraceModePermission
			       ("org.bzdev.p3d.Model3D"));
	}
	stackTraceMode = mode;
    }

    /**
     * Get the current stack-trace mode.
     * @return the stack-trace mode
     */
    public boolean getStackTraceMode() {return stackTraceMode;}

    private static final double limit = (double)Math.ulp(1F);

    private static final double fix(double val)
	throws IllegalArgumentException
    {
	if (Double.isNaN(val)) {
	    throw new IllegalArgumentException(errorMsg("NaN"));
	}
	if (Math.abs(val) < limit) {
	    val = 0.0;
	}
	return (float) val;
    }


    /**
     * Class representing a triangle embedded in a 3 dimensional space.
     */
    public static class Triangle
	implements Comparable<Triangle>, Model3DOps.Triangle
    {
	double x1; double y1; double z1;
	double x2; double y2; double z2;
	double x3; double y3; double z3;
	Color color;
	Object tag;
	LinkedList<Object> tagHistory = null;

	/**
	 * Return this object's hash code,
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
	    return Objects.hash(x1, y1, z1, x2, y2, z2, x3, y3, z3);
	}

	boolean ispatch = false;
	// Used if the triangle is actually a cubic patch.
	double x4 = Double.NaN; double y4 = Double.NaN; double z4 = Double.NaN;

	/**
	 * Determine if this triangle represents a cubic B&eacute;zier patch.
	 * @return true if this triangle represents a patch; false otherwise
	 */
	public boolean isPatch() {
	    return ispatch;
	}

	// compute twice the area of a triangle. This avoids a division
	// by 2 that can be done at the very end after the area of multiple
	// triangles are summed.
	double area2() {
	    double ux = x2 - x1;
	    double uy = y2 - y1;
	    double uz = z2 - z1;
	    double vx = x3 - x1;
	    double vy = y3 - y1;
	    double vz = z3 - z1;

	    double cpx = uy*vz - vy*uz;
	    double cpy = (vx*uz - ux*vz);
	    double cpz = (ux*vy - vx*uy);

	    return Math.sqrt(cpx*cpx + cpy*cpy + cpz*cpz);
	}

	SurfaceOps surface = null;
	int entryNumber = -1;
	int edgeNumber = -1;

	// used for computing manifolds when 'strict' is false
	Triangle prev = null;
	// used to compute manifold components.
	Edge[] getEdges(boolean counterclockwise, double[] coords)
	{
	    if (counterclockwise) {
		if (entryNumber == -1) {
		    Edge result[] = {
			new Edge(x1, y1, z1, x2, y2, z2, null),
			new Edge(x2, y2, z2, x3, y3, z3, null),
			new Edge(x3, y3, z3, x1, y1, z1, null)};
		    return result;
		} else {
		    int type = surface.getSegment(entryNumber, coords);
		    switch(type) {
		    case SurfaceIterator.CUBIC_PATCH:
			{
			    Edge result[] = {
				new Edge(coords[0], coords[1], coords[2],
					 coords[9], coords[10], coords[11],
					 null),
				new Edge(coords[9], coords[10], coords[11],
					 coords[45], coords[46], coords[47],
					 null),
				new Edge(coords[45], coords[46], coords[47],
					 coords[36], coords[37], coords[38],
					 null),
				new Edge(coords[36], coords[37], coords[38],
					 coords[0], coords[1], coords[2], null)
			    };
			    return result;
			}
		    case SurfaceIterator.CUBIC_TRIANGLE:
			{
			   Edge result[] = {
			       new Edge(coords[0], coords[1], coords[2],
					coords[27], coords[28], coords[29],
					null),
			       new Edge(coords[27], coords[28], coords[29],
					coords[9], coords[10], coords[11],
					null),
			       new Edge(coords[9], coords[10], coords[11],
					coords[0], coords[1], coords[2],
					null)
			   };
			   return result;
			}
		    case SurfaceIterator.CUBIC_VERTEX:
			{
			    Edge result[] = {
				new Edge(coords[0], coords[1], coords[2],
					 coords[9], coords[10], coords[11],
					 null),
				new Edge(coords[9], coords[10], coords[11],
					 coords[12], coords[13], coords[14],
					 null),
				new Edge(coords[12], coords[13], coords[14],
					 coords[0], coords[1], coords[2],
					 null)
			    };
			    return result;
			}
		    default:
			throw new IllegalStateException("bad case");
		    }
		}
	    } else {
		if (entryNumber == -1) {
		    Edge[] result = {
			new Edge(x2, y2, z2, x1, y1, z1, null),
			new Edge(x3, y3, z3, x2, y2, z2, null),
			new Edge(x1, y1, z1, x3, y3, z3, null)};
		    return result;
		} else {
		    int type = surface.getSegment(entryNumber, coords);
		    switch(type) {
		    case SurfaceIterator.CUBIC_PATCH:
			{
			    Edge result[] = {
				new Edge(coords[9], coords[10], coords[11],
					 coords[0], coords[1], coords[2], null),
				new Edge(coords[45], coords[46], coords[47],
					 coords[9], coords[10], coords[11],
					 null),
				new Edge(coords[36], coords[37], coords[38],
					 coords[45], coords[46], coords[47],
					 null),
				new Edge(coords[0], coords[1], coords[2],
					 coords[36], coords[37], coords[38],
					 null)
			    };
			    return result;
			}
		    case SurfaceIterator.CUBIC_TRIANGLE:
			{
			    Edge result[] = {
				new Edge(coords[27], coords[28], coords[29],
					 coords[0], coords[1], coords[2], null),
				new Edge(coords[9], coords[10], coords[11],
					 coords[27], coords[28], coords[29],
					 null),
				new Edge(coords[0], coords[1], coords[2],
					 coords[9], coords[10], coords[11],
					 null)
			    };
			    return result;
			}
		    case SurfaceIterator.CUBIC_VERTEX:
			{
			    Edge result[] = {
				new Edge(coords[9], coords[10], coords[11],
					 coords[0], coords[1], coords[2],
					 null),
				new Edge(coords[0], coords[1], coords[2],
					 coords[12], coords[13], coords[14],
					 null),
				new Edge(coords[12], coords[13], coords[14],
					 coords[9], coords[10], coords[11],
					 null)
			    };
			    return result;
			}
		    default:
			throw new IllegalStateException("bad case");
		    }
		}
	    }
	}


	/**
	 * Compute the area of a triangle.
	 * The units are those in which a unit length is the
	 * distance between 0 and 1 along the x, y, and z axis
	 * all are assumed to use the same units.  The algorithm
	 * computes the cross product of the vectors whose length
	 * and direction match the edges leading from the first
	 * vertex to the second and third respectively. The
	 * norm of this cross product is equal to twice the
	 * area of the triangle.
	 * @return the area of this triangle
	 */
	public double area() {
	    double ux = x2 - x1;
	    double uy = y2 - y1;
	    double uz = z2 - z1;
	    double vx = x3 - x1;
	    double vy = y3 - y1;
	    double vz = z3 - z1;

	    double cpx = uy*vz - vy*uz;
	    double cpy = (vx*uz - ux*vz);
	    double cpz = (ux*vy - vx*uy);

	    return Math.sqrt(cpx*cpx + cpy*cpy + cpz*cpz)/2.0;
	}

	/**
	 * Get the x coordinate of a triangle's or patch's first vertex.
	 * @return the x coordinate
	 */
	public double getX1() {return x1;}

	/**
	 * Get the y coordinate of a triangle's  or patch's first vertex.
	 * @return the y coordinate
	 */
	public double getY1() {return y1;}


	/**
	 * Get the z coordinate of a triangle's  or patch's first vertex.
	 * @return the z coordinate
	 */
	public double getZ1() {return z1;}


	/**
	 * Get the x coordinate of a triangle's  or patch's second vertex.
	 * @return the x coordinate
	 */
	public double getX2() {return x2;}

	/**
	 * Get the y coordinate of a triangle's  or patch's second vertex.
	 * @return the y coordinate
	 */
	public double getY2() {return y2;}


	/**
	 * Get the z coordinate of a triangle's  or patch's  second vertex.
	 * @return the z coordinate
	 */
	public double getZ2() {return z2;}


	/**
	 * Get the x coordinate of a triangle's  or patch's third vertex.
	 * @return the x coordinate
	 */
	public double getX3() {return x3;}

	/**
	 * Get the y coordinate of a triangle's  or patch's third vertex.
	 * @return the y coordinate
	 */
	public double getY3() {return y3;}


	/**
	 * Get the z coordinate of a triangle's  or patch's third vertex.
	 * @return the z coordinate
	 */
	public double getZ3() {return z3;}


	/**
	 * Get the x coordinate of a patch's fourth vertex.
	 * The return value is valid only if {@link #isPatch()} returns
	 * a value of true.
	 * @return the x coordinate
	 */
	public double getX4() {return x4;}

	/**
	 * Get the y coordinate of a patch's fourth vertex.
	 * The return value is valid only if {@link #isPatch()} returns
	 * a value of true.
	 * @return the y coordinate
	 */
	public double getY4() {return y4;}


	/**
	 * Get the z coordinate of a patch's fourth vertex.
	 * The return value is valid only if {@link #isPatch()} returns
	 * a value of true.
	 * @return the z coordinate
	 */
	public double getZ4() {return z4;}


	/**
	 * Get the minimum value along the x axis for a triangle.
	 * @return the minimum x value
	 */
	public double getMinX() {
	    double xmin = (x1 < x2)? x1: x2;
	    return (xmin < x3)? xmin: x3;
	}

	/**
	 * Get the minimum value along the y axis for a triangle.
	 * @return the minimum y value
	 */
	public double getMinY() {
	    double ymin = (y1 < y2)? y1: y2;
	    return (ymin < y3)? ymin: y3;
	}

	/**
	 * Get the minimum value along the z axis for a triangle.
	 * @return the minimum z value
	 */
	public double getMinZ() {
	    double zmin = (z1 < z2)? z1: z2;
	    return (zmin < z3)? zmin: z3;
	}

	/**
	 * Get the maximum value along the x axis for a triangle.
	 * @return the maximum x value
	 */
 	public double getMaxX() {
	    double xmax = (x1 > x2)? x1: x2;
	    return (xmax > x3)? xmax: x3;
	}

	/**
	 * Get the maximum value along the y axis for a triangle.
	 * @return the maximum y value
	 */
	public double getMaxY() {
	    double ymax = (y1 > y2)? y1: y2;
	    return (ymax > y3)? ymax: y3;
	}

	/**
	 * Get the maximum value along the z axis for a triangle.
	 * @return the maximum z value
	 */
	public double getMaxZ() {
	    double zmax = (z1 > z2)? z1: z2;
	    return (zmax > z3)? zmax: z3;
	}

	/**
	 * Get the length parallel to the x axis of a triangle's bounding box.
	 * Each edge of the bounding box is parallel to either the x, y or
	 * z axis in the Model3D's coordinate system.
	 * @return the length of an edge of the bounding box parallel to the
	 *         x axis
	 */
	public double getBoundingBoxX() {
	    return getMaxX() - getMinX();
	}

	/**
	 * Get the length parallel to the y axis of a triangle's bounding box.
	 * Each edge of the bounding box is parallel to either the x, y or
	 * z axis in the Model3D's coordinate system.
	 * @return the length of an edge of the bounding box parallel to the
	 *         y axis
	 */
	public double getBoundingBoxY() {
	    return getMaxY() - getMinY();
	}

	/**
	 * Get the length parallel to the z axis of a triangle's bounding box.
	 * Each edge of the bounding box is parallel to either the x, y or
	 * z axis in the Model3D's coordinate system.
	 * @return the length of an edge of the bounding box parallel to the
	 *         z axis
	 */
	public double getBoundingBoxZ() {
	    return getMaxZ() - getMinZ();
	}

	/**
	 * Comparison method.
	 * We compare on the basis of minimal z-axis values. This choice is
	 * convenient for rendering.
	 * @param other the triangle to compare to.
	 * @return -1, 0, or 1 if this triangle is respectively lower than,
	 *          equal to, or greater than the other triangle
	 */
	public int compareTo(Triangle other) {
	    // so we sort in a convenient order for slicing to get cross
	    // sections along the z axis as an object is built up, lowest
	    // values of z first.
	    double zmin1 = z1 < z2? z1: z2;
	    zmin1 = zmin1 < z3? zmin1: z3;

	    double zmin2 = other.z1 < other.z2? other.z1: other.z2;
	    zmin2 = zmin2 < other.z3? zmin2: other.z3;

	    if (zmin1 < zmin2) return -1;
	    else if (zmin1 > zmin2) return 1;
	    else return 0;
	}

	double nx; double ny; double nz;

	/**
	 * Get the X component of the normal vector.
	 * @return the X component of the normal vector
	 */
	public double getNormX() {return nx;}

	/**
	 * Get the Y component of the normal vector.
	 * @return the Y component of the normal vector
	 */
	public double getNormY() {return ny;}

	/**
	 * Get the Z component of the normal vector.
	 * @return the Z component of the normal vector
	 */
	public double getNormZ() {return nz;}

	/*
	// used internally for computing volumes of 2D manifolds.
	double rdot(double xref, double yref, double zref) {
	    return nx*(x1 - xref) + ny*(y1-yref) + nz*(z1 - zref);
	}
	*/

	static double limit = (double)Math.ulp(1F);

	private static final double fix(double val)
	    throws IllegalArgumentException
	{
	    if (Double.isNaN(val)) {
		throw new IllegalArgumentException(errorMsg("NaN"));
	    }
	    if (Math.abs(val) < limit) {
		val = 0.0;
	    }
	    return (float) val;
	}

	Triangle(Triangle triangle, Object tag) {
	    this(triangle.x1, triangle.y1, triangle.z1,
		 triangle.x2, triangle.y2, triangle.z2,
		 triangle.x3, triangle.y3, triangle.z3,
		 triangle.color, tag);
	    if (tag == null) {
		if (triangle.tag != null) {
		    this.tag = triangle.tag;
		    if (triangle.tagHistory != null) {
			tagHistory
			    = new LinkedList<Object>(triangle.tagHistory);
		    }
		}
	    } else {
		if (triangle.tag != null) {
		    tagHistory = (triangle.tagHistory == null)?
		    new LinkedList<Object>():
		    new LinkedList<Object>(triangle.tagHistory);
		    tagHistory.addFirst(triangle.tag);
		}
	    }
	}

	/** Constructor.
	 * The orientation of the triangle is determined by the
	 * right-hand rule when going from vertex 1 to vertex 2 to
	 * vertex 3.
	 * @param x1 the x coordinate of vertex 1
	 * @param y1 the y coordinate of vertex 1
	 * @param z1 the z coordinate of vertex 1
	 * @param x2 the x coordinate of vertex 2
	 * @param y2 the y coordinate of vertex 2
	 * @param z2 the z coordinate of vertex 2
	 * @param x3 the x coordinate of vertex 3
	 * @param y3 the y coordinate of vertex 3
	 * @param z3 the z coordinate of vertex 3
	 * @exception IllegalArgumentException
	 *            one of the first 9 arguments had the value
	 *            Double.NaN
	 */
	public Triangle(double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3) {
	    this(x1, y1, z1, x2, y2, z2, x3, y3, z3, null, null);
	}

	/** Constructor specifying a color.
	 * The orientation of the triangle is determined by the
	 * right-hand rule when going from vertex 1 to vertex 2 to
	 * vertex 3.
	 * @param x1 the x coordinate of vertex 1
	 * @param y1 the y coordinate of vertex 1
	 * @param z1 the z coordinate of vertex 1
	 * @param x2 the x coordinate of vertex 2
	 * @param y2 the y coordinate of vertex 2
	 * @param z2 the z coordinate of vertex 2
	 * @param x3 the x coordinate of vertex 3
	 * @param y3 the y coordinate of vertex 3
	 * @param z3 the z coordinate of vertex 3
	 * @param color the color of the triangle's front side; null for
	 *        the default
	 * @exception IllegalArgumentException
	 *            one of the first 9 arguments had the value
	 *            Double.NaN
	 */
	public Triangle(double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3,
			Color color) {
	    this(x1, y1, z1, x2, y2, z2, x3, y3, z3, color, null);
	}

	Triangle(SurfaceOps surface, int entryNumber) {
	    this.surface = surface;
	    this.entryNumber = entryNumber;
	    this.color = surface.getSegmentColor(entryNumber);
	}

	Triangle(double x1, double y1, double z1,
		 double x2, double y2, double z2,
		 double nx, double ny, double nz,
		 SurfaceOps surface,
		 int entryNumber, int edgeNumber,
		 Color color, Object tag)
	{
	    this.x1 = fix(x1);
	    this.y1 = fix(y1);
	    this.z1 = fix(z1);
	    this.x2 = fix(x2);
	    this.y2 = fix(y2);
	    this.z2 = fix(z2);
	    this.surface = surface;
	    this.entryNumber = entryNumber;
	    this.edgeNumber = edgeNumber;
	    this.color = color;
	    this.tag = tag;
	    double norm = Math.sqrt(nx*nx + ny*ny + nz*nz);
	    nx /= norm;
	    ny /= norm;
	    nz /= norm;
	    double[] coords = new double[48];
	    double x, y, z;
	    double xc, yc, zc;
	    switch(surface.getSegment(entryNumber, coords)) {
	    case SurfaceIterator.CUBIC_PATCH:
		int[] cpindices;
		switch (edgeNumber) {
		case 0:
		    xc = fix(coords[0]);
		    yc = fix(coords[1]);
		    zc = fix(coords[2]);
		    if (x1 == xc && y1 == yc && z1 == zc) {
			x3 = fix(coords[45]);
			y3 = fix(coords[46]);
			z3 = fix(coords[47]);
			x4 = fix(coords[36]);
			y4 = fix(coords[38]);
			z4 = fix(coords[39]);
		    } else {
			x4 = fix(coords[45]);
			y4 = fix(coords[46]);
			z4 = fix(coords[47]);
			x3 = fix(coords[36]);
			y3 = fix(coords[38]);
			z3 = fix(coords[39]);
		    }
		    break;
		case 1:
		    xc = fix(coords[9]);
		    yc = fix(coords[10]);
		    zc = fix(coords[11]);
		    if (x1 == xc && y1 == yc && z1 == zc) {
			x3 = fix(coords[36]);
			y3 = fix(coords[37]);
			z3 = fix(coords[38]);
			x4 = fix(coords[0]);
			y4 = fix(coords[1]);
			z4 = fix(coords[2]);
		    } else {
			x4 = fix(coords[36]);
			y4 = fix(coords[37]);
			z4 = fix(coords[38]);
			x3 = fix(coords[0]);
			y3 = fix(coords[1]);
			z3 = fix(coords[2]);
		    }
		    break;
		case 2:
		    int cpind2[] = {45, 36, 0};
		    xc = fix(coords[45]);
		    yc = fix(coords[46]);
		    zc = fix(coords[47]);
		    if (x1 == xc && y1 == yc && z1 == zc) {
			x3 = fix(coords[0]);
			y3 = fix(coords[1]);
			z3 = fix(coords[2]);
			x4 = fix(coords[9]);
			y4 = fix(coords[10]);
			z4 = fix(coords[11]);
		    } else {
			x4 = fix(coords[0]);
			y4 = fix(coords[1]);
			z4 = fix(coords[2]);
			x3 = fix(coords[9]);
			y3 = fix(coords[10]);
			z3 = fix(coords[11]);
		    }
		    break;
		case 3:
		    int cpind3[] = {36, 0, 9};
		    cpindices = cpind3;
		    xc = fix(coords[36]);
		    yc = fix(coords[37]);
		    zc = fix(coords[38]);
		    if (x1 == xc && y1 == yc && z1 == zc) {
			x3 = fix(coords[9]);
			y3 = fix(coords[10]);
			z3 = fix(coords[11]);
			x4 = fix(coords[45]);
			y4 = fix(coords[46]);
			z4 = fix(coords[47]);
		    } else {
			x4 = fix(coords[9]);
			y4 = fix(coords[10]);
			z4= fix(coords[11]);
			x3 = fix(coords[45]);
			y3 = fix(coords[46]);
			z3 = fix(coords[47]);
		    }
		    break;
		}
		ispatch = true;
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		int ctindices[] = {0, 9, 27};
		for (int i: ctindices) {
		    x = fix(coords[i]);
		    y = fix(coords[i+1]);
		    z = fix(coords[i+2]);
		    if ((x == x1 && y == y1 && z == z1)
			|| (x == x2 && y == y2 && z == z2)) {
			continue;
		    } else {
			this.x3 = x;
			this.y3 = y;
			this.z3 = z;
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		int cvindices[] = {0, 9, 12};
		for (int i: cvindices) {
		    x = fix(coords[i]);
		    y = fix(coords[i+1]);
		    z = fix(coords[i+2]);
		    if ((x == x1 && y == y1 && z == z1)
			|| (x == x2 && y == y2 && z == z2)) {
			continue;
		    } else {
			this.x3 = x;
			this.y3 = y;
			this.z3 = z;
		    }
		}
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		for (int i = 0; i < 9; i += 3) {
		    x = fix(coords[i]);
		    y = fix(coords[i+1]);
		    z = fix(coords[i+2]);
		    if ((x == x1 && y == y1 && z == z1)
			|| (x == x2 && y == y2 && z == z2)) {
			continue;
		    } else {
			this.x3 = x;
			this.y3 = y;
			this.z3 = z;
		    }
		}
		break;
	    }

	    if (Double.isNaN(nz) || Double.isNaN(ny) || Double.isNaN(nz)) {
		String msg =
		    errorMsg("linearVertices",x1,y1,z1,x2,y2,z2,x3,y3,z3);
		throw new IllegalArgumentException(msg);
	    }
	}

	/** Constructor specifying a color and tag.
	 * The orientation of the triangle is determined by the
	 * right-hand rule when going from vertex 1 to vertex 2 to
	 * vertex 3.  The tag must not be an instance of Triangle.
	 * @param x1 the x coordinate of vertex 1
	 * @param y1 the y coordinate of vertex 1
	 * @param z1 the z coordinate of vertex 1
	 * @param x2 the x coordinate of vertex 2
	 * @param y2 the y coordinate of vertex 2
	 * @param z2 the z coordinate of vertex 2
	 * @param x3 the x coordinate of vertex 3
	 * @param y3 the y coordinate of vertex 3
	 * @param z3 the z coordinate of vertex 3
	 * @param color the color of the triangle's front side; null for
	 *        the default
	 * @param tag an object tagging the triangle; null if there is none
	 * @exception IllegalArgumentException a tag was a triangle or
	 *            one of the first 9 arguments had the value
	 *            Double.NaN
	 */
	public Triangle(double x1, double y1, double z1,
			double x2, double y2, double z2,
			double x3, double y3, double z3,
			Color color, Object tag) 
	    {
		if (tag instanceof Triangle) {
		    throw new IllegalArgumentException
			(errorMsg("tagWasTriangle"));
		}
		this.x1 = fix(x1);
		this.y1 = fix(y1);
		this.z1 = fix(z1);
		this.x2 = fix(x2);
		this.y2 = fix(y2);
		this.z2 = fix(z2);
		this.x3 = fix(x3);
		this.y3 = fix(y3);
		this.z3 = fix(z3);
		this.color = color;
		this.tag = tag;

		double vx1 = this.x2 - this.x1;
		double vy1 = this.y2 - this.y1;
		double vz1 = this.z2 - this.z1;
		double vx2 = this.x3 - this.x1;
		double vy2 = this.y3 - this.y1;
		double vz2 = this.z3 - this.z1;

		nx = (vy1 * vz2 - vy2 * vz1);
		ny = (vx2 * vz1 - vx1 * vz2);
		nz = (vx1 * vy2 - vx2 * vy1);
		double norm = Math.sqrt(nx*nx + ny*ny + nz*nz);
		nx /= norm;
		ny /= norm;
		nz /= norm;
		if (Double.isNaN(nz) || Double.isNaN(ny) || Double.isNaN(nz)) {
		    String msg =
			errorMsg("linearVertices",x1,y1,z1,x2,y2,z2,x3,y3,z3);
		    throw new IllegalArgumentException(msg);
		}
		/*
		  System.out.println("triangle: norm = ("
		  +nx +", " +ny +", " + nz + ")");
		*/
	    }

	/**
	 * Get the tag associated with a triangle.
	 * @return the triangle's tag; null if there is none
	 */
	public Object getTag() {return tag;}

	/**
	 * Get the tag history list for a triangle.
	 * A history list can be created when a model adds triangles
	 * with tags from another model.
	 * @return the tag history list
	 */
	public List<Object> getTagHistory() {
	    return Collections.unmodifiableList(tagHistory);
	}

	// void setTag(Object tag) {this.tag = tag;}

	/**
	 * Get a triangle's color
	 * @return the triangle's color; null if no color is specified.
	 */
	public Color getColor() {
	    return color;
	}
    }

    /**
     * Class representing a straight line segment in 3 dimensions.
     */
    public class LineSegment {
	double x1; double y1; double z1;
	double x2; double y2; double z2;
	double nx; double ny; double nz; // normal to tangent plane; 0 if none
	Color color;
	Object tag;
	    
	/**
	 * Constructor.
	 * The line segment is a straight line going from one point to
	 * another.
	 * @param x1 the x coordinate of point 1
	 * @param y1 the y coordinate of point 1
	 * @param z1 the z coordinate of point 1
	 * @param x2 the x coordinate of point 2
	 * @param y2 the y coordinate of point 2
	 * @param z2 the z coordinate of point 2
	 */
	public LineSegment(double x1, double y1, double z1,
			   double x2, double y2, double z2)
	{
	    this(x1, y1, z1, x2, y2, z2,
		 0.0, 0.0, 0.0, (Color) null, new Object());
	}

	/**
	 * Constructor specifying a color.
	 * The line segment is a straight line going from one point to
	 * another.
	 * @param x1 the x coordinate of point 1
	 * @param y1 the y coordinate of point 1
	 * @param z1 the z coordinate of point 1
	 * @param x2 the x coordinate of point 2
	 * @param y2 the y coordinate of point 2
	 * @param z2 the z coordinate of point 2
	 * @param c the line segment's color
	 */
	public LineSegment(double x1, double y1, double z1,
			   double x2, double y2, double z2,
			   Color c)
	{
	    this(x1, y1, z1, x2, y2, z2,
		 0.0, 0.0, 0.0, c, new Object());
	}

	/**
	 * Constructor specifying a color and tag.
	 * The line segment is a straight line going from one point to
	 * another.
	 * @param x1 the x coordinate of point 1
	 * @param y1 the y coordinate of point 1
	 * @param z1 the z coordinate of point 1
	 * @param x2 the x coordinate of point 2
	 * @param y2 the y coordinate of point 2
	 * @param z2 the z coordinate of point 2
	 * @param c the line segment's color
	 * @param tag a label for the line segment indicating where it
	 *        was created
	 */
	public LineSegment(double x1, double y1, double z1,
			   double x2, double y2, double z2,
			   Color c, Object tag)
	{
	    this(x1, y1, z1, x2, y2, z2,
		 0.0, 0.0, 0.0, c, tag);
	}


	LineSegment(double xx1, double yy1, double zz1,
		    double xx2, double yy2, double zz2,
		    double nx, double ny, double nz)
	{
	    this(xx1, yy1, zz1, xx2, yy2, zz2, 
		 nx, ny, nz, (Color)null, new Object());
	    
	}

	LineSegment(double xx1, double yy1, double zz1,
		    double xx2, double yy2, double zz2,
		    double nx, double ny, double nz,
		    Color color)
	{
	    this(xx1, yy1, zz1, xx2, yy2, zz2,
		 nx, ny, nz, color, new Object());
	}


	LineSegment(double xx1, double yy1, double zz1,
		    double xx2, double yy2, double zz2,
		    double nx, double ny, double nz,
		    Color color, Object tag)
	{
	    this.tag = tag;
	    this.color = color;
	    x1 = xx1;
	    y1 = yy1;
	    z1 = zz1;
	    x2 = xx2;
	    y2 = yy2;
	    z2 = zz2;
	    if (!((x1 < x2) || (x1 == x2 && y1 < y2) 
		  || (x1 == x2 && y1 == y2 && z1 < z2))) {
		x1 = xx2;
		y1 = yy2;
		z1 = zz2;
		x2 = xx1;
		y2 = yy1;
		z2 = zz1;
	    }
	    double norm = Math.sqrt(nx*nx + ny*ny + nz*nz);
	    if (norm > 0.0) {
		this.nx = nx/norm;
		this.ny = ny/norm;
		this.nz = nz/norm;
	    } else {
		this.nx = 0.0;
		this.ny = 0.0;
		this.nz = 0.0;
	    }

	}

	/**
	 * Get the tag for a line segment.
	 * @return the tag; null if there is none
	 */
	Object getTag() {return tag;}
    }


    /**
     * Constructor.
     */
    public Model3D() {
	tmatrix[0][0] = 1.0;
	tmatrix[1][1] = 1.0;
	tmatrix[2][2] = 1.0;
	
    }

    /**
     * Constructor specifying strict mode.
     * Strict mode (the default) requires that tests for printability
     * require that all surfaces in a model are closed two-dimensional
     * manifolds. When strict mode is not active, a even number of triangles
     * can share two common vertices, provided that the orientations alternate.
     * An example is two "towers" with a triangular cross section that
     * meet at a common edge and that share a common base:
     * <BLOCKQUOTE><PRE>
     *          *-----*
     *           \   /
     *            \ /
     *             *
     *            / \
     *           /   \
     *          *-----*
     * </PRE><BLOCKQUOTE>
     * If the common vertices were split into pairs, and offset by a small
     * distance &delta;, the surface becomes a closed two-dimensional manifold.
     * When &delta; = 0, the surface is not a closed two-dimensional manifold,
     * but is still printable.
     * @param strict true if strict mode is used; false otherwise
     * @see #printable()
     * @see #notPrintable()
     * @see #verifyClosed2DManifold()
     * @see #notHollow()
     * @see #verifyNesting()
     * @see #numberOfComponents()
     * @see #getComponent(int)
     */
    public Model3D(boolean strict) {
	this.strict = strict;
	tmatrix[0][0] = 1.0;
	tmatrix[1][1] = 1.0;
	tmatrix[2][2] = 1.0;
    }

    int tlevel = 0;

    /**
     * Set the tessellation level of this model.
     * A value of 0 indicates no tessellation. Adding 1 to the level
     * quadruples the number of triangles and patches.
     * <P>
     * It is possible to make the value of level too high for particular
     * models as double-precision values are converted to floats (the
     * STL format uses floats). Some corner cases involving small
     * floating-point errors can be handled by using the method
     * {@link #setULPFactor(double)} to coalesce nearby floating-point
     * values.
     * <P>
     * The initial tessellation level is 0. Checking if a model is
     * printable, well-formed, etc., can take significantly longer
     * if the level is set to values larger than 0.  In cases where
     * the time is excessive, one may want to check the model before
     * calling this method.  Similar considerations apply to generating
     * images of a model using methods provided by this class. An
     * alternative for high resolution images is to create an STL or X3D
     * file and then use applications that make use of a computer's
     * GPU to improve the performance.
     * @param level the tessellation level
     */
    public void setTessellationLevel(int level) {
	if (level < 0) {
	    String msg  = errorMsg("negativeTessellation");
	    throw new IllegalArgumentException(msg);
	}
	tlevel = level;
    }

    /**
     * Get the current tessellation level.
     * @return the current tessellation level
     */
    public int getTessellationLevel() {
	return tlevel;
    }

    /**
     * Class representing the edge of a triangle in a 3 dimensional space.
     */
    public static class Edge {
	float x1;
	float y1;
	float z1;
	float x2;
	float y2;
	float z2;
	double nx;
	double ny;
	double nz;

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof Edge) {
		Edge e = (Edge)obj;
		if (x1 == e.x1 && y1 == e.y1 && z1 == e.z1
		    && x2 == e.x2 && y2 == e.y2 && z2 == e.z2
		    && reversed == e.reversed) {
		    return true;
		} else {
		    return false;
		}
	    } else {
		return false;
	    }
	}

	private int hashcode;

	@Override
	public int hashCode() {
	    return hashcode;
	}

	/**
	 * Get the x coordinate of the starting point of the edge.
	 * @return the x coordinate
	 */
	public float getX1() {return reversed? x2: x1;}
	/**
	 * Get the y coordinate of the starting point of the edge.
	 * @return the y coordinate
	 */
	public float getY1() {return reversed? y2: y1;}
	/**
	 * Get the z coordinate of the starting point of the edge.
	 * @return the z coordinate
	 */
	public float getZ1() {return reversed? z2: z1;}

	/**
	 * Get the x coordinate of the ending point of the edge.
	 * @return the x coordinate
	 */
	public float getX2() {return reversed? x1: x2;}
	/**
	 * Get the y coordinate of the ending point of the edge.
	 * @return the y coordinate
	 */
	public float getY2() {return reversed? y1: y2;}
	/**
	 * Get the z coordinate of the ending point of the edge.
	 * @return the z coordinate
	 */
	public float getZ2() {return reversed? z1: z2;}

	/**
	 * Get the edge's tag.
	 * @return the tag; null if there is none
	 */
	public Object getTag() {return tag;}

	boolean reversed = false;

	private static double limit = (double)Math.ulp(1F);

	private static final double fix(double val) {
	    if (Math.abs(val) < limit) {
		val = 0.0;
	    }
	    return (float)val;
	}

	Object tag;
	Triangle triangle = null;

	Edge(double x1, double y1, double z1, double x2, double y2, double z2,
	     Object tag) throws IllegalArgumentException {
	    this(x1, y1, z1, x2, y2, z2, tag, null);
	}

	Edge(double x1, double y1, double z1, double x2, double y2, double z2,
	     Object tag, Triangle triangle) throws IllegalArgumentException {
	    this((float)fix(x1), (float)fix(y1), (float)fix(z1),
		 (float)fix(x2), (float)fix(y2), (float)fix(z2),
		 tag, triangle);
	}

	Edge(float xx1, float yy1, float zz1, float xx2, float yy2, float zz2,
	     Object tag) throws IllegalArgumentException {
	    this(xx1, yy1, zz1, xx2, yy2, zz2, tag, null);
	}

	Edge(float xx1, float yy1, float zz1, float xx2, float yy2, float zz2,
	     Object tag, Triangle triangle) throws IllegalArgumentException {
	    this.tag = tag;
	    this.triangle = triangle;
	    if (!((xx1 < xx2) || (xx1 == xx2 && yy1 < yy2)
		  || (xx1 == xx2 && yy1 == yy2 && zz1 < zz2))) {
		x1 = xx2;
		y1 = yy2;
		z1 = zz2;
		x2 = xx1;
		y2 = yy1;
		z2 = zz1;
		reversed = true;
	    } else {
		x1 = xx1;
		y1 = yy1;
		z1 = zz1;
		x2 = xx2;
		y2 = yy2;
		z2 = zz2;
	    }

	    // compute a custom hashcode because the equals
	    // method ignores the tag.
	    hashcode = (reversed)? -1: 0;
	    hashcode ^= Float.floatToRawIntBits(x1);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(x1);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(y1);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(z1);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(x2);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(y2);
	    hashcode = Integer.rotateLeft(hashcode, 11);
	    hashcode ^= Float.floatToRawIntBits(z2);

	    nx = x2 - x1;
	    ny = y2 - y1;
	    nz = z2 - z1;
	    double norm = Math.sqrt(nx*nx + ny*ny + nz*nz);
	    if ((float) norm == (float) 0.0) {
		if (tag == null) {
		    tag = String.format("(%s,%s,%s)", x1, y1, z1);
		}
		if (tag instanceof StackTraceElement[]) {
		    Throwable t = new Throwable(errorMsg("zeroWidthEdge"));
		    t.setStackTrace((StackTraceElement[])tag);
		    String msg = errorMsg("identicalEndPoints");
		    throw new
			IllegalArgumentException(msg, t);
		} else {
		    throw new IllegalArgumentException
			(errorMsg("identicalEndPointsTag", tag.toString()));
		}
	    }
	    nx = nx / norm;
	    ny = ny / norm;
	    nz = nz / norm;
	}
    }

    double[][] tmatrix = new double[3][3]; // object rotation for adding.
    // initial point for triangle translation
    double txpos1 = 0.0;
    double typos1 = 0.0;
    double tzpos1 = 0.0;
    // final point for triangle translation
    double txpos2 = 0.0;
    double typos2 = 0.0;
    double tzpos2 = 0.0;

    static class TransformParms {
	double[][] tmatrix;
	double txoffset;
	double tyoffset;
	double tzoffset;
	double tEulerPhi;
	double tEulerTheta;
	double tEulerPsi;
	double txpos1;
	double typos1;
	double tzpos1;
	double txpos2;
	double typos2;
	double tzpos2;
    }

    private LinkedList<TransformParms> tstack = new
	LinkedList<TransformParms>();

    /**
     * Push object-transformation parameters.
     * These parameters are used to change the position or orientation
     * of an object without modifying the code used to generate the
     * object. The parameters set by the methods
     * {@link #setObjectTranslation(double,double,double) setObjectTranslation},
     * {@link #setObjectTranslation(double,double,double,double,double,double) setObjectTranslation},
     * and {@link #setObjectRotation(double,double,double) setObjectRotation}
     * can be pushed onto a stack and later restored.  The
     * transformations are applied in order, with the most recently defined
     * transformation applied first.  This ordering supports the
     * following use case:
     * <UL>
     *   <LI> A method m1() calls pushParms() adds an object to the
     *        model, and then sets the object translation and rotation
     *        multiple times to add additional objects to the model so
     *        that they are placed at specific locations relative to
     *        each other, creating a composite object.
     *   <LI> A method m2() also calls PushParms() and sets the
     *        object rotation and translation to place a series of
     *        objects at specific locations relative to each other.
     *        These objects are copies of the composite object obtained
     *        by calling m1().
     * </UL>
     * Note that the relative positions of the objects created by a call
     * to m1() is independent of whether m1() is called directly or if
     * it is called by m2(). If the transformations were applied in the
     * opposite order, this would in general not be the case.
     * <P>
     * Object-transformations parameters are typically used when
     * it is desirable to include multiple independent objects in
     * a model and position them in a way that will take advantage
     * of the pricing rules used by various 3D printing services.
     * <P>
     * If {@link #pushTransform(Transform3D)} is also used, the
     * tranforms provided by that method are applied before any of
     * the transforms set using
     * {@link #setObjectTranslation(double,double,double) setObjectTranslation},
     * {@link #setObjectTranslation(double,double,double,double,double,double) setObjectTranslation},
     * and {@link #setObjectRotation(double,double,double) setObjectRotation}
     * @see #setObjectTranslation(double,double,double)
     * @see #setObjectTranslation(double,double,double,double,double,double)
     * @see #setObjectRotation(double,double,double)
     * @see #popParms()
     */
    public void pushParms() {
	TransformParms tp = new TransformParms();
	tp.tmatrix = tmatrix;
	tp.txoffset = txoffset;
	tp.tyoffset = tyoffset;
	tp.tzoffset = tzoffset;
	tp.tEulerPhi = tEulerPhi;
	tp.tEulerTheta = tEulerTheta;
	tp.tEulerPsi = tEulerPsi;
	tp.txpos1 = txpos1;
	tp.typos1 = typos1;
	tp.tzpos1 = tzpos1;
	tp.txpos2 = txpos2;
	tp.typos2 = typos2;
	tp.tzpos2 = tzpos2;
	tstack.addFirst(tp);
	tmatrix = new double[3][3];
	tmatrix[0][0] = 1.0;
	tmatrix[1][1] = 1.0;
	tmatrix[2][2] = 1.0;
	txoffset = 0.0;
	tyoffset = 0.0;
	tzoffset = 0.0;
	txpos1 = 0.0;
	typos1 = 0.0;
	tzpos1 = 0.0;
	txpos2 = 0.0;
	typos2 = 0.0;
	tzpos2 = 0.0;
    }

    /**
     * Pop object-transformation parameters.
     * This undoes the effect of the method {@link #pushParms()}.
     * @see #setObjectTranslation(double,double,double)
     * @see #setObjectTranslation(double,double,double,double,double,double)
     * @see #setObjectRotation(double,double,double)
     * @see #pushParms()
     */
    public void popParms() {
	int size = tstack.size();
	if (size > 0) {
	    TransformParms tp = tstack.removeFirst();
	    tmatrix = tp.tmatrix;
	    txoffset = tp.txoffset;
	    tyoffset = tp.tyoffset;
	    tzoffset = tp.tzoffset;
	    txpos1 = tp.txpos1;
	    typos1 = tp.typos1;
	    tzpos1 = tp.tzpos1;
	    txpos2 = tp.txpos2;
	    typos2 = tp.typos2;
	    tzpos2 = tp.tzpos2;
	    tEulerPhi = tp.tEulerPhi;
	    tEulerTheta = tp.tEulerTheta;
	    tEulerPsi = tp.tEulerPsi;
	} else {
	    throw new IllegalStateException(errorMsg("parmstackEmpty"));
	}
    }

    // translation vector for triangle translation
    double txoffset = 0.0;
    double tyoffset = 0.0;
    double tzoffset = 0.0;

    // rotation parameters
    double tEulerPhi = 0.0;
    double tEulerTheta = 0.0;
    double tEulerPsi = 0.0;

    private void adjustTriangleOffsets() {
	if (txpos1 == 0.0 && typos1 == 0.0 && tzpos1 == 0.0) {
	    txoffset = txpos2; tyoffset = typos2; tzoffset = tzpos2;
	} else {
	    txoffset = txpos2 - tmatrix[0][0] * txpos1
		- tmatrix[0][1] * typos1 - tmatrix[0][2] * tzpos1;
	    tyoffset = typos2 - tmatrix[1][0] * txpos1
		- tmatrix[1][1] * typos1 - tmatrix[1][2] * tzpos1;
	    tzoffset = tzpos2 - tmatrix[2][0] * txpos1
		- tmatrix[2][1] * typos1 - tmatrix[2][2] * tzpos1;
	}
    }

    /**
     * Specify how to Translate new objects after rotating them about (0,0,0).
     * After the rotation, the point at the origin will be moved to point
     * (x2, y2, z2).
     * <P>
     * There is a stack of transformations that includes rotations and
     * translations.  The transformations are applied in order, with
     * the most recently defined transformation applied first.
     * Pushing this stack saves the current translations and rotations.
     * <P>
     * There is also a list of transforms set by calling
     * {@link #pushTransform(Transform3D)}. Transforms on this list are
     * applied before the object translations and rotations.
     * @param x2 the x coordinate for the translation
     * @param y2 the y coordinate for the translation
     * @param z2 the z coordinate for the translation
     * @see #setObjectTranslation(double,double,double,double,double,double)
     * @see #setObjectRotation(double,double,double)
     * @see #pushParms()
     * @see #popParms()
     */
    public  void setObjectTranslation(double x2, double y2, double z2)
    {
	setObjectTranslation(0.0, 0.0, 0.0, x2, y2, z2);
    }


    /**
     * Specify how to Translate an object after rotating it about a point.
     * after a rotation about the point (x1, y1, z1),  the objects will be
     * translated so that (x1, y1, z1) moves to (x2, y2, z2).
     * There is a stack of transformations that includes rotations and
     * translations. The transformations are applied in order, with the
     * most recently defined transformation applied first.
     * Pushing this stack saves the current translations and rotations.
     * <P>
     * There is also a list of transforms set by calling
     * {@link #pushTransform(Transform3D)}. Transforms on this list are
     * applied before the object translations and rotations.
     * @param x1 the initial x coordinate
     * @param y1 the initial y coordinate
     * @param z1 the initial z coordinate
     * @param x2 the final x coordinate
     * @param y2 the final y coordinate
     * @param z2 the final z coordinate
     * @see #setObjectTranslation(double,double,double)
     * @see #setObjectRotation(double,double,double)
     * @see #pushParms()
     * @see #popParms()
     */
    public void setObjectTranslation(double x1, double y1, double z1,
				     double x2, double y2, double z2)
    {
	txpos1 = x1; typos1 = y1; tzpos1 = z1;
	txpos2 = x2; typos2 = y2; tzpos2 = z2;
	adjustTriangleOffsets();
    }

    /**
     * Rotate new objects.
     * <P>
     * The rotation is specified by Eulerian angles.
     * The Eulerian angles follow the convention used in Goldstein,
     * "Classical Mechanics" but the objects are rotated instead of
     * the coordinates. The angle phi specifies an initial rotation
     * of objects counterclockwise about the z-axis of an origin at
     * (x1, y1, z1) set in the last call to
     * {@link #setObjectTranslation(double,double,double,double,double,double) setObjectTranslation},
     * with a default of (0,0,0). The angle theta specifies a second
     * rotation about an x axis through (x1, y1, z1), again counter
     * clockwise.  The angle psi specifies a final counterclockwise
     * rotation about the z axis.
     * <P>
     * In effect, the angles have the opposite signs from the corresponding
     * angles used by
     * {@link Model3D.Image#setCoordRotation(double,double,double) setCoordRotation}.
     * <P>
     * There is a stack of transformations that includes rotations and
     * translations. The transformations are applied in order, with the
     * most recently defined transformation applied first.
     * Pushing this stack saves the current translations and rotations.
     * <P>
     * There is also a list of transforms set by calling
     * {@link #pushTransform(Transform3D)}. Transforms on this list are
     * applied before the object translations and rotations.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     */
    public void setObjectRotation(double phi, double theta, double psi) {
	// nearly same as for setCoordRotation, but with the signs
	// of the angles reversed, and tmatrix set instead of matrix.

	tEulerPhi = -phi;
	tEulerTheta = -theta;
	tEulerPsi = -psi;

	double sin_psi;
	double cos_psi;
	double sin_phi;
	double cos_phi;
	double sin_theta;
	double cos_theta;

	if (tEulerPsi == 0.0) {
	    sin_psi = 0.0;
	    cos_psi = 1.0;
	} else if (tEulerPsi == Math.PI/2.0 || tEulerPsi == -(Math.PI * 1.5)) {
	    sin_psi = 1.0;
	    cos_psi = 0.0;
	} else if (tEulerPsi == Math.PI || tEulerPsi == -Math.PI) {
	    sin_psi = 0.0;
	    cos_psi = -1.0;
	} else if (tEulerPsi == Math.PI*1.5 || tEulerPsi == -(Math.PI/2.0)) {
	    sin_psi = -1.0;
	    cos_psi = 0.0;
	} else {
	    sin_psi = Math.sin(tEulerPsi);
	    cos_psi = Math.cos(tEulerPsi);
	}
	if (tEulerPhi == 0.0) {
	    sin_phi = 0.0;
	    cos_phi = 1.0;
	} else if (tEulerPhi == Math.PI/2.0 || tEulerPhi == -(Math.PI * 1.5)) {
	    sin_phi = 1.0;
	    cos_phi = 0.0;
	} else if (tEulerPhi == Math.PI || tEulerPhi == -Math.PI) {
	    sin_phi = 0.0;
	    cos_phi = -1.0;
	} else if (tEulerPhi == Math.PI*1.5 || tEulerPhi == -(Math.PI/2.0)) {
	    sin_phi = -1.0;
	    cos_phi = 0.0;
	} else {
	    sin_phi = Math.sin(tEulerPhi);
	    cos_phi = Math.cos(tEulerPhi);
	}
	if (tEulerTheta == 0.0) {
	    sin_theta = 0.0;
	    cos_theta = 1.0;
	} else if (tEulerTheta == Math.PI/2.0 || tEulerTheta == -(Math.PI*1.5)) {
	    sin_theta = 1.0;
	    cos_theta = 0.0;
	} else if (tEulerTheta == Math.PI || tEulerTheta == -Math.PI) {
	    sin_theta = 0.0;
	    cos_theta = -1.0;
	} else if (tEulerTheta == Math.PI*1.5 || tEulerTheta == -(Math.PI/2.0)) {
	    sin_theta = -1.0;
	    cos_theta = 0.0;
	} else {
	    sin_theta = Math.sin(tEulerTheta);
	    cos_theta = Math.cos(tEulerTheta);
	}
	tmatrix[0][0] = cos_psi * cos_phi - cos_theta * sin_phi* sin_psi;
	tmatrix[0][1] = cos_psi * sin_phi + cos_theta * cos_phi * sin_psi;
	tmatrix[0][2] = sin_theta * sin_psi;
	tmatrix[1][0] = -sin_psi * cos_phi - cos_theta * sin_phi * cos_psi;
	tmatrix[1][1] = -sin_psi * sin_phi + cos_theta * cos_phi * cos_psi;
	tmatrix[1][2] = sin_theta * cos_psi;
	tmatrix[2][0] = sin_theta * sin_phi;
	tmatrix[2][1] = - sin_theta*cos_phi;
	tmatrix[2][2] = cos_theta;
	adjustTriangleOffsets();
    }

    private boolean bbvalid = true;
    private double maxx = Double.NEGATIVE_INFINITY;
    private double maxy = Double.NEGATIVE_INFINITY;
    private double maxz = Double.NEGATIVE_INFINITY;
    private double minx = Double.POSITIVE_INFINITY;
    private double miny = Double.POSITIVE_INFINITY;
    private double minz = Double.POSITIVE_INFINITY;

    private void updateBoundingBox(Triangle triangle) {
	if (!bbvalid) return;
	if (triangle.x1 > maxx) maxx = triangle.x1;
	if (triangle.y1 > maxy) maxy = triangle.y1;
	if (triangle.z1 > maxz) maxz = triangle.z1;
	if (triangle.x2 > maxx) maxx = triangle.x2;
	if (triangle.y2 > maxy) maxy = triangle.y2;
	if (triangle.z2 > maxz) maxz = triangle.z2;
	if (triangle.x3 > maxx) maxx = triangle.x3;
	if (triangle.y3 > maxy) maxy = triangle.y3;
	if (triangle.z3 > maxz) maxz = triangle.z3;
	if (triangle.x1 < minx) minx = triangle.x1;
	if (triangle.y1 < miny) miny = triangle.y1;
	if (triangle.z1 < minz) minz = triangle.z1;
	if (triangle.x2 < minx) minx = triangle.x2;
	if (triangle.y2 < miny) miny = triangle.y2;
	if (triangle.z2 < minz) minz = triangle.z2;
	if (triangle.x3 < minx) minx = triangle.x3;
	if (triangle.y3 < miny) miny = triangle.y3;
	if (triangle.z3 < minz) minz = triangle.z3;
    }

    private void computeBoundingBoxIfNeeded() {
	if (bbvalid) return;
	maxx = Double.NEGATIVE_INFINITY;
	maxy = Double.NEGATIVE_INFINITY;
	maxz = Double.NEGATIVE_INFINITY;
	minx = Double.POSITIVE_INFINITY;
	miny = Double.POSITIVE_INFINITY;
	minz = Double.POSITIVE_INFINITY;
	bbvalid = true;
	for (Triangle triangle: triangleMap.values()) {
	    updateBoundingBox(triangle);
	}
	for (Triangle triangle: triangleSet) {
	    updateBoundingBox(triangle);
	}
	if (cubics.size() > 0) {
	    Rectangle3D bb = cubics.getBounds();
	    if (bb.getMaxX() > maxx) maxx = bb.getMaxX();
	    if (bb.getMaxY() > maxy) maxy = bb.getMaxY();
	    if (bb.getMaxZ() > maxz) maxz = bb.getMaxZ();
	    if (bb.getMinX() < minx) minx = bb.getMinX();
	    if (bb.getMinY() < miny) miny = bb.getMinY();
	    if (bb.getMinZ() < minz) minz = bb.getMinZ();
	}
	if (cubicVertices.size() > 0) {
	    Rectangle3D bb = cubicVertices.getBounds();
	    if (bb.getMaxX() > maxx) maxx = bb.getMaxX();
	    if (bb.getMaxY() > maxy) maxy = bb.getMaxY();
	    if (bb.getMaxZ() > maxz) maxz = bb.getMaxZ();
	    if (bb.getMinX() < minx) minx = bb.getMinX();
	    if (bb.getMinY() < miny) miny = bb.getMinY();
	    if (bb.getMinZ() < minz) minz = bb.getMinZ();
	}
    }
    
    /**
     * Get the length parallel to the x axis for the bounding box of
     * containing all objects.
     * @return the length of the bounding box in the x direction
     */
    public double getBoundingBoxX() {
	computeBoundingBoxIfNeeded();
	return (maxx - minx);
    }

    /**
     * Get the length parallel to the y axis for the bounding box of
     * containing all objects.
     * @return the length of the bounding box in the y direction
     */
    public double getBoundingBoxY() {
	computeBoundingBoxIfNeeded();
	return (maxy - miny);
    }

    /**
     * Get the length parallel to the z axis for the bounding box of
     * containing all objects.
     * @return the length of the bounding box in the z direction
     */
    public double getBoundingBoxZ() {
	computeBoundingBoxIfNeeded();
	return (maxz - minz);
    }

    /**
     * Get the minimum x coordinate for all objects contained in a 3D model.
     * @return the minimum value of the x coordinate.
     */
    public double getMinX() {
	computeBoundingBoxIfNeeded();
	return minx;
    }
    /**
     * Get the minimum y coordinate for all objects contained in a 3D model.
     * @return the minimum value of the y coordinate.
     */
    public double getMaxX() {
	computeBoundingBoxIfNeeded();
	return maxx;
    }
    /**
     * Get the minimum z coordinate for all objects contained in a 3D model.
     * @return the minimum value of the z coordinate.
     */
    public double getMinY() {
	computeBoundingBoxIfNeeded();
	return miny;
    }
    /**
     * Get the maximum x coordinate for all objects contained in a 3D model.
     * @return the maximum value of the x coordinate.
     */
    public double getMaxY() {
	computeBoundingBoxIfNeeded();
	return maxy;
    }

    /**
     * Get the maximum y coordinate for all objects contained in a 3D model.
     * @return the maximum value of the y coordinate.
     */
    public double getMinZ() {
	computeBoundingBoxIfNeeded();
	return minz;
    }

    /**
     * Get the maximum z coordinate for all objects contained in a 3D model.
     * @return the maximum value of the z coordinate.
     */
    public double getMaxZ() {
	computeBoundingBoxIfNeeded();
	return maxz;
    }

    @Override
    public Rectangle3D getBounds() {
	return new Rectangle3D.Double(getMinX(), getMinY(), getMinZ(),
				      getBoundingBoxX(),
				      getBoundingBoxY(),
				      getBoundingBoxZ());
    }

    @Override
    public SurfaceIterator getSurfaceIterator(final Transform3D tform) {
	final SurfaceIterator sit = new SurfaceIterator() {
	    int index = (!triangleMap.isEmpty())? 0:
		((!triangleSet.isEmpty())? 1: 2);
	    Iterator<Triangle>it = (index == 0)?
		triangleMap.values().iterator():
		((index == 1)? triangleSet.iterator(): null);

	    Triangle component = (index == 2)? null:
		(it.hasNext()? it.next(): null);

	    double[] tmp = new double[9];
	    public int currentSegment(double[] coords) {
		if (component == null) return -1;
		if (tform == null) {
		    coords[0] = component.x1;
		    coords[1] = component.y1;
		    coords[2] = component.z1;
		    coords[3] = component.x3;
		    coords[4] = component.y3;
		    coords[5] = component.z3;
		    coords[6] = component.x2;
		    coords[7] = component.y2;
		    coords[8] = component.z2;
		} else {
		    tmp[0] = component.x1;
		    tmp[1] = component.y1;
		    tmp[2] = component.z1;
		    tmp[3] = component.x3;
		    tmp[4] = component.y3;
		    tmp[5] = component.z3;
		    tmp[6] = component.x2;
		    tmp[7] = component.y2;
		    tmp[8] = component.z2;
		    tform.transform(tmp, 0, coords, 0, 9);
		}
		return SurfaceIterator.PLANAR_TRIANGLE;
	    }

	    public int currentSegment(float[] coords) {
		if (component == null) return -1;
		if (tform == null) {
		    coords[0] = (float)component.x1;
		    coords[1] = (float)component.y1;
		    coords[2] = (float)component.z1;
		    coords[3] = (float)component.x3;
		    coords[4] = (float)component.y3;
		    coords[5] = (float)component.z3;
		    coords[6] = (float)component.x2;
		    coords[7] = (float)component.y2;
		    coords[8] = (float)component.z2;
		} else {
		    tmp[0] = component.x1;
		    tmp[1] = component.y1;
		    tmp[2] = component.z1;
		    tmp[3] = component.x3;
		    tmp[4] = component.y3;
		    tmp[5] = component.z3;
		    tmp[6] = component.x2;
		    tmp[7] = component.y2;
		    tmp[8] = component.z2;
		    tform.transform(tmp, 0, coords, 0, 9);
		    for (int i = 0; i < 9; i++) {
			coords[i] = (float)fix(coords[i]);
		    }
		}
		return SurfaceIterator.PLANAR_TRIANGLE;
	    }

	    @Override
	    public Color currentColor() {
		return component.getColor();
	    }

	    @Override
	    public Object currentTag() {
		if (component == null) return null;
		return component.tag;
	    }

	    @Override
	    public boolean isDone() {
		return index == 2;
	    }
	    @Override
	    public void next() {
		if (it.hasNext()) {
		    component = it.next();
		} else {
		    if (index == 0) {
			index = 1;
			it = triangleSet.iterator();
			if (it.hasNext()) {
			    component = it.next();
			} else {
			    index = 2;
			}
		    } else if (index == 1) {
			index = 2;
		    }
		}
	    }
	    @Override
	    public boolean isOriented() {return true;}
	};
	int cSize = cubics.size();
	int cvSize = cubicVertices.size();
	if (cSize > 0 || cvSize > 0) {
	    return new SurfaceIterator() {
		SurfaceIterator cit = (cSize > 0)?
		    cubics.getSurfaceIterator(tform): null;
		SurfaceIterator cvit = (cvSize > 0)?
		    cubicVertices.getSurfaceIterator(tform): null;
		public int currentSegment(double[] coords) {
		    if (cit != null) {
			return cit.currentSegment(coords);
		    } else if (cvit != null) {
			return cvit.currentSegment(coords);
		    } else {
			return sit.currentSegment(coords);
		    }
		}
		public int currentSegment(float[] coords) {
		    if (cit != null) {
			return cit.currentSegment(coords);
		    } else if (cvit != null) {
			return cvit.currentSegment(coords);
		    } else {
			return sit.currentSegment(coords);
		    }
		}

		public Color currentColor() {
		    if (cit != null) {
			return cit.currentColor();
		    } else if (cvit != null) {
			return cvit.currentColor();
		    } else {
			return sit.currentColor();
		    }
		}

		public Object currentTag() {
		    if (cit != null) {
			return cit.currentTag();
		    } else if (cvit != null) {
			return cvit.currentTag();
		    } else {
			return sit.currentTag();
		    }
		}

		public boolean isDone() {
		    if (cit != null) {
			if (cit.isDone()) {
			    cit = null;
			    if (cvit != null) {
				if (cvit.isDone()) {
				    cvit = null;
				    return sit.isDone();
				} else {
				    return false;
				}
			    } else {
				return sit.isDone();
			    }
			} else {
			    return false;
			}
		    } else if (cvit != null) {
			if (cvit.isDone()) {
			    cvit = null;
			    return sit.isDone();
			} else {
			    return false;
			}
		    } else {
			return sit.isDone();
		    }
		}
		public boolean isOriented() {return true;}
		public void next() {
		    if (cit != null) {
			cit.next();
			if (cit.isDone()) cit = null;
		    } else if (cvit != null) {
			cvit.next();
			if (cvit.isDone()) cvit = null;
		    } else {
			sit.next();
		    }
		}
	    };
	} else {
	    return sit;
	}
    }
    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	return new SubdivisionIterator(getSurfaceIterator(tform), level);
    }

    @Override
    public boolean isOriented() {return true;}

    @Override
    public Path3D getBoundary() {
	return (new Surface3D.Boundary(getSurfaceIterator(null))).getPath();
    }

    @Override
    public boolean isClosedManifold() {
	return (verifyClosed2DManifold(true) == null);
    }

    Surface3D cubics = new Surface3D.Double();
    Path3D cpaths = null;

    Surface3D cubicVertices = new Surface3D.Double();

    HashMap<Object, Triangle> triangleMap =
	new HashMap<Object, Triangle>();

    LinkedHashSet<Triangle> triangleSet = new LinkedHashSet<Triangle>();

    /**
     * Get a scanner that can iterate over the planar triangles provided by this
     * model.
     * @return a {@link CollectionScanner} that can be used to iterate over the
     *         triangles associated with this model
     */
    public CollectionScanner<Triangle> triangles() {
	CollectionScanner<Triangle> cs = new CollectionScanner<>();
	cs.add(triangleMap.values());
	cs.add(triangleSet);
	return cs;
    }

    /**
     * Get a collection scanner that can iterate over planar triangles,
     * B&eacute;zier triangles, and B&eacute;zier patches.
     * @return an iterator
     */
    public CollectionScanner<Triangle> trianglesAndPatches() {
	CollectionScanner<Triangle> cs = new CollectionScanner<>();
	if (cubics.size() > 0) {
	    cs.add(new AbstractCollection<Triangle>() {
		    int sz = cubics.size();
		    Iterator<Triangle> cit = new Iterator<Triangle>() {
			double[] coords = new double[48];
			int index = 0;
			int size = cubics.size();
			@Override
			public boolean hasNext() {return index < size;}
			@Override
			public Triangle next() {
			    return new Triangle(cubics, index++);
			}
			@Override
			public void remove() {
			    throw new UnsupportedOperationException();
			}
		    };
		    @Override
		    public int size() {return sz;}
		    @Override
		    public Iterator<Triangle> iterator() {return cit;}
		});
	}
	if (cubicVertices.size() > 0) {
	    cs.add(new AbstractCollection<Triangle>() {
		    int sz = cubicVertices.size();
		    Iterator<Triangle> cit = new Iterator<Triangle>() {
			double[] coords = new double[48];
			int index = 0;
			int size = cubicVertices.size();
			@Override
			public boolean hasNext() {return index < size;}
			@Override
			public Triangle next() {
			    return new Triangle(cubicVertices, index++);
			}
			@Override
			public void remove() {
			    throw new UnsupportedOperationException();
			}
		    };
		    @Override
		    public int size() {return sz;}
		    @Override
		    public Iterator<Triangle> iterator() {return cit;}
		});
	}
	cs.add(triangleMap.values());
	cs.add(triangleSet);
	return cs;
    }

    /**
     * Exception indicating that an error occurred while tessellating
     * a model.
     */
    public static class TessellationException extends RuntimeException {

	TessellationException() {
	    super();
	}

	TessellationException(String msg) {
	    super(msg);
	}
    }

    private double ulpFactor = 0.0;

    /**
     * Set the ULP factor for this model.
     * When the argument provided to this method is nonzero, Model3D
     * will attempt to coallesce nearby values to prevent
     * inconsistencies due to floating point errors. A value x is
     * considered close to a previously provided value if within one a
     * multiple, equal to the argument of this method, of the
     * single-precision ULP (Unit of Least Precision) value for
     * x. When within this range, the value x will be replaced with
     * a previously provided value.
     * <P>
     * Unless the value is 0, it must be positive and no less than 2.0.
     * The default value is 0.0.
     * @param ulpFactor a multiple used to scale the floating-point
     *         ULP value; 0.0 to disable its use
     * @exception IllegalArgumentException the argument is out of range
     * @exception IllegalStateException this method was called after
     *            objects were added to this model
     */
    public void setULPFactor(double ulpFactor) {

	if (size() > 0) {
	    throw new IllegalStateException(errorMsg("ulpState"));
	}

	if (ulpFactor < 2.0 && ulpFactor != 0.0) {
	    String msg = errorMsg("ulpFactor", ulpFactor);
	    throw new IllegalArgumentException(msg);
	}
	this.ulpFactor = ulpFactor;
    }


    // Used by tessellate to prevent floating-point errors from
    // providing different values for the same coordinate.
    private double bestCoord(TreeSet<Double>valueSet, double x) {
	if (ulpFactor == 0.0) return x;
	Double floor = valueSet.floor(x);
	Double ceiling = valueSet.ceiling(x);
	double ulp = (Math.abs(x) < 1.0)? Math.ulp(1.0F): Math.ulp((float) x);
	if (floor != null) {
	    if (ceiling != null) {
		double diff1 = x - floor;
		double diff2 = ceiling - x;
		if (diff1 < diff2) {
		    if (diff1 < ulpFactor*ulp) {
			return floor;
		    } else {
			valueSet.add((double)(float)x);
		    }
		} else if (diff2 <= diff1) {
		    if (diff2 < ulpFactor*ulp) {
			return ceiling;
		    } else {
			valueSet.add((double)(float)x);
		    }
		}
	    } else {
		if ((x - floor) < ulpFactor*ulp) {
		    return floor;
		} else {
		    valueSet.add((double)(float)x);
		}
	    }
	} else if (ceiling != null) {
	    if ((ceiling - x) < ulpFactor*ulp) {
		return ceiling;
	    } else {
		valueSet.add((double)(float)x);
	    }
	} else {
	    valueSet.add((double)(float)x);
	}
	return (float)x;
    }

    /**
     * Get an iterator that will return a sequence of triangles after
     * tessellation using the preset tessellation level
     * @return an iterator
     * @see #setTessellationLevel(int)
     */
    public Iterator<Triangle> tessellate() {
	return tessellate(tlevel);
    }

    /**
     * Get an iterator that will return a sequence of triangles after
     * tessellation.
     * @param level the tessellation level
     * @return an iterator
     * @exception IllegalArgumentException the tessellation level was
     *            negative
     */
    public Iterator<Triangle> tessellate(int level) {
	if (level < 0) {
	    String msg = errorMsg("negativeTessellation");
	    throw new IllegalArgumentException(msg);
	}

	TreeSet<Double> xvalueSet = (TreeSet<Double>)xCornerCoords.clone();
	TreeSet<Double> yvalueSet = (TreeSet<Double>)yCornerCoords.clone();
	TreeSet<Double> zvalueSet = (TreeSet<Double>)zCornerCoords.clone();


	cubics.computeBoundary(null, true);
	Path3D path = cubics.getBoundary();
	if (path == null) {
	    throw new TessellationException(errorMsg("notWellFormed"));
	}
	PathIterator3D pit = path.getPathIterator(null);
	double[] pcoords = new double[9];
	double[] pcoords2;
	double x = Double.NaN, y = Double.NaN, z = Double.NaN;
	double fx = Double.NaN, fy = Double.NaN, fz = Double.NaN;
	// find the boundary for the cubic part of the model.
	// This will contain cubic Bezier curve segments that are actually
	// straight lines, and that will join planar triangles.  For
	// each such segment, create a set of edges oriented in the
	// reverse direction. Planar triangles that share such an edge
	// have to be partitioned.
	final HashSet<Edge> tedges = new HashSet<>();
	while (!pit.isDone()) {
	    switch(pit.currentSegment(pcoords)) {
	    case PathIterator3D.SEG_MOVETO:
		x = pcoords[0]; y = pcoords[1]; z = pcoords[2];
		fx = x; fy = y; fz = z;
		break;
	    case PathIterator3D.SEG_CUBICTO:
		pcoords2 =
		    Path3D.setupCubic(x, y , z,
				      pcoords[6], pcoords[7], pcoords[8]);
		boolean canUse = true;
		for (int i = 0; i < 9; i++) {
		    if ((float)pcoords2[i+3] != pcoords[i]) {
			/*
			System.out.format("pcoords2[%d+3] = %s, "
					  + "pcoords[%d] = %s\n",
					  i, pcoords2[i+3], i, pcoords[i]);
			*/
			// We don't generate an exception because users may
			// want to display a model before it has been completed.
			// An incomplete model may not be a closed manifold.
			canUse = false;
		    }
		}
		if (canUse) {
		    tedges.add(new Edge(pcoords[6], pcoords[7], pcoords[8],
					x, y, z, null));
		}
		x = pcoords[6]; y = pcoords[7]; z = pcoords[8];
		break;
	    case PathIterator3D.SEG_CLOSE:
		x = fx; y = fy; z = fz;
		break;
	    default:
		String msg = errorMsg("badPathIteratorType");
		throw new IllegalStateException(msg);
	    }
	    pit.next();
	}
	// now create the iterator.
	return new Iterator<Triangle>() {
	    double[] scoords = new double[48];
	    long npartitions = MathOps.lPow(2,level);
	    SurfaceIterator sit = cubics.getSurfaceIterator(null, level);
	    // cvsit does not subdivide its elements because we will do
	    // that below so that straight edges will not be subdivided
	    // unless necessary.
	    SurfaceIterator cvsit =
		cubicVertices.getSurfaceIterator(null);
	    CollectionScanner<Triangle> planarTriangles = triangles();
	    Iterator<Triangle> it = planarTriangles.iterator();
	    int cubicsIndex = 0;
	    boolean cpmode = true;
	    boolean flipDiagonal = false;
	    LinkedList<Triangle> currentTriangleList =
		new LinkedList<Triangle>();
	    public boolean hasNext() {
		if (sit != null && !sit.isDone()) {
		    return true;
		} else if (cvsit != null && !cvsit.isDone()) {
		    return true;
		} else if (it.hasNext()) {
		    return true;
		} else {
		    return !currentTriangleList.isEmpty();
		}
	    }
	    int sitind = -1;
	    int cvind = -1;
	    public Triangle next() {
		if (sit != null) {
		    if (sit.isDone()) {
			sit = null;
		    } else {
			 sitind++;
			int type = sit.currentSegment(scoords);
			Color color = sit.currentColor();
			Object tag = sit.currentTag();
			switch (type) {
			case SurfaceIterator.CUBIC_TRIANGLE:
			    sit.next();
			    return new
				Triangle(bestCoord(xvalueSet, scoords[0]),
					 bestCoord(yvalueSet, scoords[1]),
					 bestCoord(zvalueSet, scoords[2]),
					 bestCoord(xvalueSet, scoords[27]),
					 bestCoord(yvalueSet, scoords[28]),
					 bestCoord(zvalueSet, scoords[29]),
					 bestCoord(xvalueSet, scoords[9]),
					 bestCoord(yvalueSet, scoords[10]),
					 bestCoord(zvalueSet, scoords[11]),
					 color, tag);
			case SurfaceIterator.CUBIC_PATCH:
			    int vertexpair = -1;
			    if (scoords[0] == scoords[9]
				&& scoords[1] == scoords[10]
				&& scoords[2] == scoords[11]) {
				vertexpair = 0;
			    }
			    if (scoords[9] == scoords[45]
				&& scoords[10] == scoords[46]
				&& scoords[11] == scoords[47]) {
				if (vertexpair != -1) {
				    String msg =
					errorMsg("ZLE", sitind, 1);
				    throw new TessellationException(msg);
				}
				    vertexpair = 9;
			    }
			    if (scoords[45] == scoords[36]
				&& scoords[46] == scoords[37]
				&& scoords[47] == scoords[38]) {
				if (vertexpair != -1) {
				    String msg =
					errorMsg("ZLE", sitind, 2);
				    throw new TessellationException(msg);
				}
				vertexpair = 45;
			    }
			    if (scoords[36] == scoords[0]
				&& scoords[37] == scoords[1]
				&& scoords[38] == scoords[2]) {
				if (vertexpair != -1) {
				    String msg =
					errorMsg("ZLE", sitind, 3);
				    throw new TessellationException(msg);
				}
				vertexpair = 36;
			    }
			    switch(vertexpair) {
			    case 0:
				sit.next();
				return new
				    Triangle(bestCoord(xvalueSet, scoords[0]),
					     bestCoord(yvalueSet, scoords[1]),
					     bestCoord(zvalueSet, scoords[2]),
					     bestCoord(xvalueSet, scoords[9]),
					     bestCoord(yvalueSet, scoords[10]),
					     bestCoord(zvalueSet, scoords[11]),
					     bestCoord(xvalueSet, scoords[45]),
					     bestCoord(yvalueSet, scoords[46]),
					     bestCoord(zvalueSet, scoords[47]),
					     color, tag);
			    case 9:
				sit.next();
				return new
				    Triangle(bestCoord(xvalueSet, scoords[9]),
					     bestCoord(yvalueSet, scoords[10]),
					     bestCoord(zvalueSet, scoords[11]),
					     bestCoord(xvalueSet, scoords[45]),
					     bestCoord(yvalueSet, scoords[46]),
					     bestCoord(zvalueSet, scoords[47]),
					     bestCoord(xvalueSet, scoords[36]),
					     bestCoord(yvalueSet, scoords[37]),
					     bestCoord(zvalueSet, scoords[38]),
					     color, tag);
			    case 45:
				sit.next();
				return new
				    Triangle(bestCoord(xvalueSet, scoords[45]),
					     bestCoord(yvalueSet, scoords[46]),
					     bestCoord(zvalueSet, scoords[47]),
					     bestCoord(xvalueSet, scoords[0]),
					     bestCoord(yvalueSet, scoords[1]),
					     bestCoord(zvalueSet, scoords[2]),
					     bestCoord(xvalueSet, scoords[9]),
					     bestCoord(yvalueSet, scoords[10]),
					     bestCoord(zvalueSet, scoords[11]),
					     color, tag);
				
			    case 36:
				sit.next();
				return new
				    Triangle(bestCoord(xvalueSet, scoords[36]),
					     bestCoord(yvalueSet, scoords[37]),
					     bestCoord(zvalueSet, scoords[38]),
					     bestCoord(xvalueSet, scoords[9]),
					     bestCoord(yvalueSet, scoords[10]),
					     bestCoord(zvalueSet, scoords[11]),
					     bestCoord(xvalueSet, scoords[45]),
					     bestCoord(yvalueSet, scoords[46]),
					     bestCoord(zvalueSet, scoords[47]),
					     color, tag);
			    default:
				cpmode = !cpmode;
				if (cpmode) {
				    sit.next();
				    if (flipDiagonal) {
					return new
					    Triangle(bestCoord(xvalueSet,
								scoords[0]),
						     bestCoord(yvalueSet,
								scoords[1]),
						     bestCoord(zvalueSet,
								scoords[2]),
						     bestCoord(xvalueSet,
								scoords[9]),
						     bestCoord(yvalueSet,
								scoords[10]),
						     bestCoord(zvalueSet,
								scoords[11]),
						     bestCoord(xvalueSet,
								scoords[45]),
						     bestCoord(yvalueSet,
								scoords[46]),
						     bestCoord(zvalueSet,
								scoords[47]),
						     color, tag);
				    } else {
					return new
					    Triangle(bestCoord(xvalueSet,
								scoords[9]),
						     bestCoord(yvalueSet,
								scoords[10]),
						     bestCoord(zvalueSet,
								scoords[11]),
						     bestCoord(xvalueSet,
								scoords[45]),
						     bestCoord(yvalueSet,
								scoords[46]),
						     bestCoord(zvalueSet,
								scoords[47]),
						     bestCoord(xvalueSet,
								scoords[36]),
						     bestCoord(yvalueSet,
								scoords[37]),
						     bestCoord(zvalueSet,
								scoords[38]),
						     color, tag);
				    }
				} else {
				    double[] v1 = new double[3];
				    double[] v2 = new double[3];
				    double[] v3 = new double[3];
				    double[] v4 = new double[3];
				    for (int i = 0; i < 3; i++) {
					v1[i] = scoords[45+i] - scoords[i];
					v2[i] = scoords[36+i] - scoords[45+i];
				    }
				    VectorOps.normalize(v1);
				    VectorOps.normalize(v2);
				    VectorOps.crossProduct(v3, v1, v2);
				    VectorOps.normalize(v3);
				    for (int i = 0; i < 3; i++) {
					v1[i] = scoords[9+i] - scoords[i];
					v2[i] = scoords[45+i] - scoords[9+i];
				    }
				    VectorOps.normalize(v1);
				    VectorOps.normalize(v2);
				    VectorOps.crossProduct(v4, v1, v2);
				    VectorOps.normalize(v4);
				    VectorOps.crossProduct(v1, v3, v4);
				    double test1 = VectorOps.norm(v1);
				    for (int i = 0; i < 3; i++) {
					v1[i] = scoords[9+i] - scoords[i];
					v2[i] = scoords[36+i] - scoords[9+i];
				    }
				    VectorOps.normalize(v1);
				    VectorOps.normalize(v2);
				    VectorOps.crossProduct(v3, v1, v2);
				    VectorOps.normalize(v3);
				    for (int i = 0; i < 3; i++) {
					v1[i] = scoords[45+i] - scoords[9+i];
					v2[i] = scoords[36+i] - scoords[45+i];
				    }
				    VectorOps.normalize(v1);
				    VectorOps.normalize(v2);
				    VectorOps.crossProduct(v4, v1, v2);
				    VectorOps.normalize(v4);
				    VectorOps.crossProduct(v1, v3, v4);
				    double test2 = VectorOps.norm(v1);
				    flipDiagonal = (test1 <= test2);
				    if (flipDiagonal) {
					return new
					    Triangle(bestCoord(xvalueSet,
								scoords[0]),
						     bestCoord(yvalueSet,
								scoords[1]),
						     bestCoord(zvalueSet,
								scoords[2]),
						     bestCoord(xvalueSet,
								scoords[45]),
						     bestCoord(yvalueSet,
								scoords[46]),
						     bestCoord(zvalueSet,
								scoords[47]),
						     bestCoord(xvalueSet,
								scoords[36]),
						     bestCoord(yvalueSet,
								scoords[37]),
						     bestCoord(zvalueSet,
								scoords[38]),
						     color, tag);
				    } else {
					return new
					    Triangle(bestCoord(xvalueSet,
								scoords[0]),
						     bestCoord(yvalueSet,
								scoords[1]),
						     bestCoord(zvalueSet,
								scoords[2]),
						     bestCoord(xvalueSet,
								scoords[9]),
						     bestCoord(yvalueSet,
								scoords[10]),
						     bestCoord(zvalueSet,
								scoords[11]),
						     bestCoord(xvalueSet,
								scoords[36]),
						     bestCoord(yvalueSet,
								scoords[37]),
						     bestCoord(zvalueSet,
								scoords[38]),
						     color, tag);
				    }
				}
			    }
			}
		    }
		}
		if  (cvsit != null) {
		    HashSet<Edge> cvedges = new HashSet<>();
		    // partitioned into cubic patches
		    double[] cvcoords = new double[15];
		    LinkedList<Triangle> cvtlist = new LinkedList<>();
		    while (!cvsit.isDone()) {
			cvsit.currentSegment(cvcoords);
			if (((cvcoords[0] == cvcoords[9])
			     && (cvcoords[1] == cvcoords[10])
			     && (cvcoords[2] == cvcoords[11]))
			    || ((cvcoords[0] == cvcoords[12])
				&& (cvcoords[1] == cvcoords[13])
				&& (cvcoords[2] == cvcoords[14]))
			    || ((cvcoords[9] == cvcoords[12])
				&& (cvcoords[10] == cvcoords[13])
				&& (cvcoords[11] == cvcoords[14]))) {
			    String msg = errorMsg("ZLECV", cvind);
			    throw new IllegalArgumentException(msg);
			}
			double[] lpath = Path3D.setupCubic(cvcoords[0],
							   cvcoords[1],
							   cvcoords[2],
							   cvcoords[9],
							   cvcoords[10],
							   cvcoords[11]);
			boolean linear = true;
			for (int i = 3; i < 9; i++) {
			    if (fix(cvcoords[i]) != fix(lpath[i])) {
				linear = false;
				break;
			    }
			}
			/*
			VectorOps.sub(cvcoords, 15, cvcoords, 3, cvcoords, 0,
				      3);
			VectorOps.sub(cvcoords, 18, cvcoords, 6, cvcoords, 0,
				      3);
			VectorOps.sub(cvcoords, 21, cvcoords, 9, cvcoords, 0,
				      3);
			double scale = VectorOps.normSquared(cvcoords, 15, 9);
			if (scale == 0.0) {
			    String msg = errorMsg("ZLECV", cvind);
			    throw new IllegalArgumentException(msg);
			}
			VectorOps.crossProduct(cvcoords, 24,
					       cvcoords, 15, cvcoords, 21);
			boolean linear = (VectorOps.normSquared(cvcoords, 24, 3)
					  / scale) < 1.e-8;
			VectorOps.crossProduct(cvcoords, 24,
					       cvcoords, 15, cvcoords, 21);
			linear = linear
			    && (VectorOps.normSquared(cvcoords, 24, 3)
				/ scale) < 1.e-8;
			*/
			if (!linear && level > 0) {
			    LinkedList<double[]> list = new LinkedList<>();
			    list.add(cvcoords);
			    for (int i = 0; i < level; i++) {
				LinkedList<double[]> nlist = new LinkedList<>();
				for (double[] carray: list) {
				    double[][] paths = SubdivisionIterator
					.splitCubicBezierCurve(carray);
				    for (int k = 0; k < 12; k++) {
					// to duplicate what SubdivisionIterator
					// does.
					paths[0][k] = /*(float)*/paths[0][k];
					paths[1][k] = /*(float)*/paths[1][k];
				    }
				    nlist.add(paths[0]);
				    nlist.add(paths[1]);
				}
				list = nlist;
			    }
			    /*
			    double[] splitcoords = new double[18];
			    PathSplitter.split(PathIterator3D.SEG_CUBICTO,
					       cvcoords[0], cvcoords[1],
					       cvcoords[2], cvcoords, 3,
					       splitcoords, 0, 0.5);
			    double[] tmp = new double[12];
			    LinkedList<double[]> list = new LinkedList<>();
			    System.arraycopy(cvcoords, 0, tmp, 0, 3);
			    System.arraycopy(splitcoords, 0, tmp, 3, 9);
			    list.add(tmp);
			    tmp = new double[12];
			    System.arraycopy(splitcoords, 6, tmp, 0, 12);
			    list.add(tmp);
			    for (int i = 1; i < level; i++) {
				LinkedList<double[]> nlist = new LinkedList<>();
				for (double[] carray: list) {
				    PathSplitter.split
					(PathIterator3D.SEG_CUBICTO,
					 carray[0], carray[1], carray[2],
					 carray, 3, splitcoords, 0, 0.5);
				    tmp = new double[12];
				    System.arraycopy(carray, 0, tmp, 0, 3);
				    System.arraycopy(splitcoords, 0, tmp, 3, 9);
				    nlist.add(tmp);
				    tmp = new double[12];
				    System.arraycopy(splitcoords, 6,
						     tmp, 0, 12);
				    nlist.add(tmp);
				}
				list = nlist;
			    }
			    */
			    for (double[] pcoords: list) {
				double x1 = pcoords[0];
				double y1 = pcoords[1];
				double z1 = pcoords[2];
				double x2 = pcoords[9];
				double y2 = pcoords[10];
				double z2 = pcoords[11];
				double x3 = cvcoords[12];
				double y3 = cvcoords[13];
				double z3 = cvcoords[14];

				/*
				if (z3 > 0.0) {
				    System.out.format("(%g, %g, %g)"
						      + "-(%g, %g, %g)"
						      + "-(%g, %g, %g)\n",
						      x1, y1, z1,
						      x2, y2, z2,
						      x3, y3, z3);
				}
				*/
				Triangle t = new
				    Triangle(bestCoord(xvalueSet, x1),
					     bestCoord(yvalueSet, y1),
					     bestCoord(zvalueSet, z1),
					     bestCoord(xvalueSet, x2),
					     bestCoord(yvalueSet, y2),
					     bestCoord(zvalueSet, z2),
					     bestCoord(xvalueSet, x3),
					     bestCoord(yvalueSet, y3),
					     bestCoord(zvalueSet, z3),
					     cvsit.currentColor(),
					     cvsit.currentTag());
				cvtlist.add(t);
			    }
			} else {
			    // If level is 0 or if the cubic-curve edge is
			    // actually a straight line, we can replace the
			    // cubic vertex object with a triangle as the
			    // other two edges are straight lines.
			    Triangle t = new
				Triangle(bestCoord(xvalueSet, cvcoords[0]),
					 bestCoord(yvalueSet, cvcoords[1]),
					 bestCoord(zvalueSet, cvcoords[2]),
					 bestCoord(xvalueSet, cvcoords[9]),
					 bestCoord(yvalueSet, cvcoords[10]),
					 bestCoord(zvalueSet, cvcoords[11]),
					 bestCoord(xvalueSet, cvcoords[12]),
					 bestCoord(yvalueSet, cvcoords[13]),
					 bestCoord(zvalueSet, cvcoords[14]),
					 cvsit.currentColor(),
					 cvsit.currentTag());
			    cvtlist.add(t);
			}
			cvsit.next();
		    }
		    if (cvtlist.size() > 0) {
			planarTriangles.add(cvtlist);
		    }
		    it = planarTriangles.iterator();
		    cvsit = null;
		}
		if (currentTriangleList.isEmpty()) {
		    if (it.hasNext()) {
			Triangle ct = it.next();
			// add code to split the triangle when there
			// is a cubic Bezier edge adjacent to it.
			boolean mustPartition = false;
			Edge[] edges = ct.getEdges(true, scoords);
			boolean[] edgeFlags = new boolean[edges.length];
			for (int i = 0; i < edges.length; i++) {
			    Edge e = edges[i];
			    if (tedges.contains(e) && level > 0) {
				mustPartition = true;
				edgeFlags[i] = true;
			    }
			}
			if (mustPartition) {
			    double  xc =
				(ct.getX1() + ct.getX2() + ct.getX3())/3;
			    double  yc =
				(ct.getY1() + ct.getY2() + ct.getY3())/3;
			    double  zc =
				(ct.getZ1() + ct.getZ2() + ct.getZ3())/3;
			    for(int i = 0; i < edges.length; i++) {
				if (edgeFlags[i]) {
				    // split edge just like SubdivisionIterator
				    double x1 = edges[i].getX1();
				    double y1 = edges[i].getY1();
				    double z1 = edges[i].getZ1();
				    double x2 = edges[i].getX2();
				    double y2 = edges[i].getY2();
				    double z2 = edges[i].getZ2();
				    double[] ourcoords =
					Path3D.setupCubic(x1, y1, z1,
							  x2, y2, z2);
				    LinkedList<double[]> list =
					new LinkedList<>();
				    list.add(ourcoords);
				    for (int j = 0; j < level; j++) {
					LinkedList<double[]> nlist =
					    new LinkedList<>();
					for (double[] carray: list) {
					    double[][] paths =
						SubdivisionIterator
						.splitCubicBezierCurve(carray);
					    nlist.add(paths[0]);
					    nlist.add(paths[1]);
					}
					list = nlist;
				    }
				    for (double[] ecoords: list) {
					double xx1 = ecoords[0];
					double yy1 = ecoords[1];
					double zz1 = ecoords[2];
					double xx2 = ecoords[9];
					double yy2 = ecoords[10];
					double zz2 = ecoords[11];
					Triangle nt = new
					    Triangle(bestCoord(xvalueSet, xc),
						     bestCoord(yvalueSet, yc),
						     bestCoord(zvalueSet, zc),
						     bestCoord(xvalueSet, xx1),
						     bestCoord(yvalueSet, yy1),
						     bestCoord(zvalueSet, zz1),
						     bestCoord(xvalueSet, xx2),
						     bestCoord(yvalueSet, yy2),
						     bestCoord(zvalueSet, zz2),
						     ct.color, ct.tag);
					currentTriangleList.push(nt);
				    }
				    /*
				    for (int j = 0; j < npartitions; j++) {
					double u = ((double)j)/npartitions;
					double nu =
					    ((double)(j+1))/npartitions;
					double xx1 = (x1*(1-u) + x2 * u);
					double yy1 = (y1*(1-u) + y2 * u);
					double zz1 = (z1*(1-u) + z2 * u);
					double xx2 = (x1*(1-nu) + x2 * nu);
					double yy2 = (y1*(1-nu) + y2 * nu);
					double zz2 = (z1*(1-nu) + z2 * nu);
					Triangle nt = new
					    Triangle(xc, yc, zc,
						     xx1, yy1, zz1,
						     xx2, yy2, zz2,
						     ct.color, ct.tag);
					currentTriangleList.push(nt);
				    }
				    */
				} else {
				    Edge e = edges[i];
				    Triangle nt = new
					Triangle(bestCoord(xvalueSet, xc),
						 bestCoord(yvalueSet, yc),
						 bestCoord(zvalueSet, zc),
						 bestCoord(xvalueSet,
							    e.getX1()),
						 bestCoord(yvalueSet,
							    e.getY1()),
						 bestCoord(zvalueSet,
							    e.getZ1()),
						 bestCoord(xvalueSet,
							    e.getX2()),
						 bestCoord(yvalueSet,
							    e.getY2()),
						 bestCoord(zvalueSet,
							    e.getZ2()),
						 ct.color, ct.tag);
				    currentTriangleList.push(nt);
				}
			    }
			    return currentTriangleList.poll();
			} else {
			    return ct;
			}
		    } else {
			return null;
		    }
		} else {
		    return currentTriangleList.poll();
		}
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    }
	};
    }

    

    /**
     * Return the number of triangles and cubic patches in a model.
     * The value returned is the value before any tessellation.
     * @return the number of triangles and cubic patches in this model.
     */
    public int size() {
	return cubics.size() + triangleMap.size() + triangleSet.size()
	    + cubicVertices.size();
    }

    /**
     * Class to represent multiple tags (a current tag and a history)
     * 
     */
    public static class Tags extends LinkedList<Object> {
	Tags() {
	    super();
	}
	/**
	 * Get the current tag.
	 * @return the tag
	 */
	public Object getTag() {
	    if (isEmpty()) {
		return null;
	    }
	    return getFirst();
	}

	/**
	 * Get the tag history.
	 * @return a list of previous tags most recent first.
	 */
	public List<Object> getTagHistory() {
	    if (size() < 2) return null;
	    return Collections.unmodifiableList(subList(1, size()));
	}
    }

    /**
     * Append the surface segments specified by a surface iterator.
     * @param si the surface iterator
     * @param tag a tag to label the segments added by this method
     * @exception IllegalArgumentException the surface iterator returned
                  segments from a non-oriented 3D shape
     */
    public final void append(SurfaceIterator si, Object tag)
	throws IllegalArgumentException
    {
	cachedArea = null;
	cachedVolume = null;
	manifoldComponents = null;
	double[] coords = new double[48];
	while (!si.isDone()) {
	    if (!si.isOriented()) {
		throw new IllegalArgumentException("notOriented");
	    }
	    int type = si.currentSegment(coords);
	    Color sicolor = si.currentColor();
	    Object sitag = si.currentTag();
	    switch (type) {
	    case SurfaceIterator.CUBIC_PATCH:
		cpaths = null;		// need to recompute boundary.
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntags = new Tags();
			ntags.addFirst(sitag);
			ntags.addFirst(tag);
			tag = ntags;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 16);
		}
		for (int i = 0; i < 48; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: patchCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubics.addCubicPatch(coords, sicolor, tag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 48; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		cpaths = null;		// need to recompute boundary.
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntags = new Tags();
			ntags.addFirst(sitag);
			ntags.addFirst(tag);
			tag = ntags;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 10);
		}
		for (int i = 0; i < 30; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: ctCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubics.addCubicTriangle(coords, sicolor, tag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 30; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		cpaths = null;		// need to recompute boundary.
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntags = new Tags();
			ntags.addFirst(sitag);
			ntags.addFirst(tag);
			tag = ntags;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 5);
		}
		for (int i = 0; i < 15; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: cvCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubicVertices.addCubicVertex(coords, sicolor, tag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 15; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		  // addTriangle does the transform!
		for (int i: triangleCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		Triangle t = new Triangle(coords[0], coords[1], coords[2],
					  coords[6], coords[7], coords[8],
					  coords[3], coords[4], coords[5],
					  sicolor, sitag);
		if (tag == null) {
		    addTriangle(t);
		} else {
		    t = new Triangle(t, tag);
		    addTriangle(t);
		}
		break;
	    default:
		throw new UnexpectedExceptionError();
	    }
	    si.next();
	}
    }

    /**
     * Append the surface segments specified by another surface.
     * @param surface the other surface
     * @exception IllegalArgumentException the surface was not an oriented
     *            surface
     */
    public final void append(Shape3D surface) throws IllegalArgumentException {
	SurfaceIterator si = surface.getSurfaceIterator(null);
	append(si, null);
    }


    /**
     * Append the surface segments specified by another surface after
     * applying a transformation.
     * @param surface the other surface
     * @param transform the transform to apply to a surface's control points
     * @exception IllegalArgumentException the surface was not an oriented
     *            surface
     */
    public final void append(Shape3D surface, Transform3D transform)
	throws IllegalArgumentException
    {
	SurfaceIterator si = surface.getSurfaceIterator(transform);
	append(si, null);
    }

    /**
     * Add a generalized model.
     * <P>
     * This class implements an interface named Model3DOps that provides
     * the operations needed to add components to a model and to
     * implement the {@link org.bzdev.geom.Shape3D} interface. This method
     * is provided so that other implementations of the
     * {@link Model3DOps} interface can be used to create models that will
     * be imported into the current model.
     * @param m3d the model.
     * @exception IllegalArgumentException if a model is being added to itself
     */
    public void addModel(Model3DOps<?> m3d)
    {
	addModel(m3d, null);
    }

    /**
     * Add a generalized model, including a tag.
     * <P>
     * This class implements an interface named Model3DOps that provides
     * the operations needed to add components to a model and to
     * implement the {@link org.bzdev.geom.Shape3D} interface. This method
     * is provided so that other implementations of the
     * {@link Model3DOps} interface can be used to create models that will
     * be imported into the current model.
     * @param m3d the model
     * @param tag a tag to name this call to addModel
     * @exception IllegalArgumentException if a model is being added to itself
     */
    public void addModel(Model3DOps<?> m3d, Object tag)
    {
	if (m3d instanceof Model3D) {
	    addModel((Model3D)m3d);
	    return;
	}
	double[] coords = new double[48];
	SurfaceIterator si = m3d.getSurfaceIterator(null);
	while (!si.isDone()) {
	    int type = si.currentSegment(coords);
	    Color sicolor = si.currentColor();
	    Object sitag = si.currentTag();
	    switch (type) {
	    case SurfaceIterator.CUBIC_PATCH:
		cpaths = null;
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntag = new Tags();
			ntag.addFirst(sitag);
			ntag.addFirst(tag);
			tag = ntag;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 16);
		}
		for (int i = 0; i < 48; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: patchCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubics.addCubicPatch(coords, sicolor, sitag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 48; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_TRIANGLE:
		cpaths = null;
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntag = new Tags();
			ntag.addFirst(sitag);
			ntag.addFirst(tag);
			tag = ntag;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 10);
		}
		for (int i = 0; i < 30; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: ctCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubics.addCubicTriangle(coords, sicolor, sitag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 30; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.CUBIC_VERTEX:
		cpaths = null;
		if (tag == null && sitag != null) {
		    tag = sitag;
		} else if (tag != null && sitag != null) {
		    if (sitag instanceof Tags) {
			((Tags)sitag).addFirst(tag);
			tag = sitag;
		    } else {
			Tags ntag = new Tags();
			ntag.addFirst(sitag);
			ntag.addFirst(tag);
			tag = ntag;
		    }
		}
		if (mustDoTransforms()) {
		    doTransforms(coords, 0, coords, 0, 5);
		}
		for (int i = 0; i < 15; i++) {
		    coords[i] = fix(coords[i]);
		}
		for (int i: cvCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		cubicVertices.addCubicVertex(coords, sicolor, sitag);
		cachedArea = null;
		cachedVolume = null;
		manifoldComponents = null;
		if (bbvalid) {
		    for (int i = 0; i < 15; i += 3) {
			double x = coords[i];
			double y = coords[i+1];
			double z = coords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		break;
	    case SurfaceIterator.PLANAR_TRIANGLE:
		// addTriangle does the transform!
		for (int i: triangleCornerIndices) {
		    bestCoord(xCornerCoords, coords[i]);
		    bestCoord(yCornerCoords, coords[i+1]);
		    bestCoord(zCornerCoords, coords[i+2]);
		}
		if (tag == null) {
		    Triangle t = new Triangle(coords[0], coords[1], coords[2],
					      coords[6], coords[7], coords[8],
					      coords[3], coords[4], coords[5],
					      sicolor, sitag);
		    addTriangle(t);
		} else {
		    Triangle t = new Triangle(coords[0], coords[1], coords[2],
					      coords[6], coords[7], coords[8],
					      coords[3], coords[4], coords[5],
					      sicolor, sitag);
		    t = new Triangle(t, tag);
		    addTriangle(t);
		}
		break;
	    default:
		throw new UnexpectedExceptionError();
	    }
	    si.next();
	}
    }

    /**
     * Add the objects in another Model3D to this model.
     * @param m3d a model containing objects to add
     */
    public void addModel(Model3D m3d) {
	addModel(m3d, null);
    }

    /**
     * Add the objects in another Model3D to this model and tag them.
     * @param m3d a model containing objects to add
     * @param tag a tag to label the objects added
     * @exception IllegalArgumentException if a model is being added to itself
     */
    public void addModel(Model3D m3d, Object tag) {
	if (m3d == this) {
	    throw new IllegalArgumentException(errorMsg("addToSelf"));
	}
	if (m3d.cubics.size() != 0) {
	    SurfaceIterator si = m3d.cubics.getSurfaceIterator(null);
	    append(si, tag);
	}
	if (m3d.cubicVertices.size() != 0) {
	    SurfaceIterator si = m3d.cubicVertices.getSurfaceIterator(null);
	    append(si, tag);
	}
	for (Triangle triangle: m3d.triangleMap.values()) {
	    Triangle t = new Triangle(triangle, tag);
	    addTriangle(t);

	}
	for (Triangle triangle: m3d.triangleSet) {
	    Triangle t = new Triangle(triangle, tag);
	    addTriangle(triangle);
	}
    }

    /**
     * Add a possibly tessellated 3D model to this model.
     * When the second argument is true, only triangles are added and
     * these are computed using argument model's tessellation level.
     * When the second argument is false, this method is equivalent
     * to {@link #addModel(Model3D)}.
     * <P>
     * Models with multiple components may require different levels
     * of tessellation for each. For example, some 3D printing services
     * charge a fee per part, but count interlocking parts as a single
     * part.  Components used to tie parts together to reduce the per-part
     * fee will be discarded after printing, and do not need as high a
     * level of tessellation as other parts.
     * <P>
     * Note: if an isolated component consists of planar triangles,
     * including ones created by using classes such as
     * {@link SteppedGrid}, the method {@link #tessellate()} will not
     * subdivide those triangles regardless of the tessellation level.
     * @param m3d the model to add
     * @param tessellate true of the model being added should be
     *        tessellated; false otherwise
     */
    public void addModel(Model3D m3d, boolean tessellate) {
	if (m3d == this) {
	    throw new IllegalArgumentException(errorMsg("addToSelf"));
	}
	if (tessellate) {
	    Iterator<Triangle> it = m3d.tessellate();
	    while (it.hasNext()) {
		Triangle triangle = it.next();
		addTriangle(triangle);
	    }
	} else {
	    addModel(m3d);
	}
    }

    LinkedList<Transform3D> transforms = new LinkedList<>();

    /**
     * Add a transform that will be applied to a triangle
     * when it is added to the model.  The transform is applied
     * to the triangle's vertices. The edges will still be straight.
     * For cubic  patches, triangles, and vertices, the transform is
     * applied to each control point.
     * <P>
     * This method can be used to create a variety of objects
     * from existing models by distorting them.  The transforms
     * are applied in the order in which they were added.  This
     * is the opposite of the order used by
     * {@link #pushParms()}.  The transforms are general transforms.
     * <P>
     * Transforms set using {@link #pushTransform(Transform3D)} are
     * applied before the transforms set using
     * {@link #setObjectTranslation(double,double,double) setObjectTranslation},
     * {@link #setObjectTranslation(double,double,double,double,double,double) setObjectTranslation},
     * {@link #setObjectRotation(double,double,double)} or {@link #pushParms()}.
     * @param transform the transform to apply.
     * @exception IllegalArgumentException the argument was null
     * @see #popTransform()
     */
    public void pushTransform(Transform3D transform) {
	if (transform == null) {
	    throw new IllegalArgumentException(errorMsg("nullArgument"));
	}
	transforms.addLast(transform);
    }

    /**
     * Remove the last transform that was added.
     * @exception IllegalStateException the transform stack was empty
     */
    public void popTransform() {
	if (transforms.size() == 0) {
	    throw new IllegalStateException(errorMsg("popError"));
	}
	transforms.removeLast();
    }

    private boolean mustDoTransforms() {
	int n = transforms.size();
	boolean hasParms = tEulerPhi != 0.0 || tEulerTheta != 0.0
	    || tEulerPsi != 0.0 || txoffset != 0.0 || tyoffset != 0.0
	    || tzoffset != 0.0;
	int ns = tstack.size();
	if (hasParms) ns++;
	return (n > 0 || ns > 0);
    }

    private void doTransforms(double[] scoords, int soffset,
			     double[] dcoords, int doffset,
			     int npoints)
    {
	int npoints3 = 3*npoints;
	boolean needCopy = (scoords == dcoords) &&
	    (Math.abs(soffset-doffset) < npoints3);
	int n = transforms.size();
	boolean hasParms = tEulerPhi != 0.0 || tEulerTheta != 0.0
	    || tEulerPsi != 0.0 || txoffset != 0.0 || tyoffset != 0.0
	    || tzoffset != 0.0;
	int ns = tstack.size();
	if (hasParms) ns++;
	if (ns == 0 && n == 0) {
	    if (scoords == dcoords && soffset == doffset) {
		return;
	    }
	    System.arraycopy(scoords, soffset, dcoords, doffset, npoints3);
	    return;
	}
	if (needCopy) {
	    double[] tmp = new double[npoints3];
	    System.arraycopy(scoords, soffset, tmp, 0, npoints3);
	    scoords = tmp;
	    soffset = 0;
	}
	boolean notmps = (n == 1 && ns == 0);
	double[] tmp1 = notmps? null: new double[npoints3];
	double[] tmp2 = notmps? null: new double[npoints3];
	if (n == 1 && ns == 0) {
	    for (Transform3D transform: transforms) {
		transform.transform(scoords, soffset,
				    dcoords, doffset,
				    npoints);
	    }
	    return;
	} else if (ns > 0 && n == 0) {
	    System.arraycopy(scoords, soffset, tmp1, 0, npoints3);
	} else if (n > 1) {
	    int nm1 = n - 1;
	    int i = 0;
	    for (Transform3D transform: transforms) {
		if (i == 0) {
		    transform.transform(scoords, soffset, tmp2, 0, npoints);
		} else if (i == nm1 && ns == 0) {
		    transform.transform(tmp1, 0, dcoords, doffset, npoints);
		} else {
		    transform.transform(tmp1, 0, tmp2, 0, npoints);
		}
		double[] tmp3 = tmp1;
		tmp1 = tmp2;
		tmp2 = tmp3;
		i++;
	    }
	}

	if (ns == 1) {
	    if (hasParms) {
		for (int i = 0; i < npoints3; i += 3) {
		    dcoords[doffset+i] = txoffset + tmatrix[0][0]*tmp1[i]
			+ tmatrix[0][1]*tmp1[i+1] + tmatrix[0][2]*tmp1[i+2];
		    dcoords[doffset+i+1] = tyoffset + tmatrix[1][0]*tmp1[i]
			+ tmatrix[1][1]*tmp1[i+1] + tmatrix[1][2]*tmp1[i+2];
		    dcoords[doffset+i+2] = tzoffset + tmatrix[2][0]*tmp1[i]
			+ tmatrix[2][1]*tmp1[i+1] + tmatrix[2][2]*tmp1[i+2];
		}
		return;
	    } else {
		for (TransformParms tp: tstack) {
		    if (tp.tEulerPhi != 0.0 || tp.tEulerTheta != 0.0
			|| tp.tEulerPsi != 0.0
			|| tp.txoffset != 0.0 || tp.tyoffset != 0.0
			|| tp.tzoffset != 0.0 ) {
			for (int i = 0; i < npoints3; i += 3) {
			    dcoords[doffset+i] = tp.txoffset
				+ tp.tmatrix[0][0]*tmp1[i]
				+ tp.tmatrix[0][1]*tmp1[i+1]
				+ tp.tmatrix[0][2]*tmp1[i+2];
			    dcoords[doffset+i+1] = tp.tyoffset
				+ tp.tmatrix[1][0]*tmp1[i]
				+ tp.tmatrix[1][1]*tmp1[i+1]
				+ tp.tmatrix[1][2]*tmp1[i+2];
			    dcoords[doffset+i+2] = tp.tzoffset
				+ tp.tmatrix[2][0]*tmp1[i]
				+ tp.tmatrix[2][1]*tmp1[i+1]
				+ tp.tmatrix[2][2]*tmp1[i+2];
			}
		    } else {
			System.arraycopy(tmp1, 0, dcoords, doffset, npoints3);
		    }
		    return;
		}
		return;
	    }
	} else if (ns > 1) {
	    if (hasParms) {
		for (int i = 0; i < npoints3; i += 3) {
		    tmp2[i] = txoffset + tmatrix[0][0]*tmp1[i]
			+ tmatrix[0][1]*tmp1[i+1] + tmatrix[0][2]*tmp1[i+2];
		    tmp2[i+1] = tyoffset + tmatrix[1][0]*tmp1[i]
			+ tmatrix[1][1]*tmp1[i+1] + tmatrix[1][2]*tmp1[i+2];
		    tmp2[i+2] = tzoffset + tmatrix[2][0]*tmp1[i]
			+ tmatrix[2][1]*tmp1[i+1] + tmatrix[2][2]*tmp1[i+2];
		}
	    }
	    for (TransformParms tp: tstack) {
		if (tp.tEulerPhi != 0.0 || tp.tEulerTheta != 0.0
		    || tp.tEulerPsi != 0.0
		    || tp.txoffset != 0.0 || tp.tyoffset != 0.0
		    || tp.tzoffset != 0.0 ) {
		    for (int i = 0; i < npoints3; i += 3) {
			tmp2[i] = tp.txoffset + tp.tmatrix[0][0]*tmp1[i]
			    + tp.tmatrix[0][1]*tmp1[i+1]
			    + tp.tmatrix[0][2]*tmp1[i+2];
			tmp2[i+1] = tp.tyoffset + tp.tmatrix[1][0]*tmp1[i]
			    + tp.tmatrix[1][1]*tmp1[i+1]
			    + tp.tmatrix[1][2]*tmp1[i+2];
			tmp2[i+2] = tp.tzoffset + tp.tmatrix[2][0]*tmp1[i]
			    + tp.tmatrix[2][1]*tmp1[i+1]
			    + tp.tmatrix[2][2]*tmp1[i+2];
		    }
		    double[] tmp3 = tmp1;
		    tmp1 = tmp2;
		    tmp2 = tmp3;
		}
	    }
	    System.arraycopy(tmp1, 0, dcoords, doffset, npoints3);
	}
    }


    /**
     * {@inheritDoc}
     * <P>
     * Transforms that cause a modified triangle to be stored can
     * be configured using {@link #pushTransform(Transform3D)} and
     * {@link #popTransform()}.
     * @param triangle {@inheritDoc}
     * @return {@inheritDoc}
     * @see #pushTransform(Transform3D)
     * @see #popTransform()
     */
    @Override
    public Triangle addTriangle(Model3DOps.Triangle triangle) {
	if (triangle instanceof Triangle) {
	    return addTriangle((Triangle) triangle);
	} else {
	    return addTriangle
		(triangle.getX1(), triangle.getY1(), triangle.getZ1(),
		 triangle.getX2(), triangle.getY2(), triangle.getZ2(),
		 triangle.getX3(), triangle.getY3(), triangle.getZ3(),
		 triangle.getColor(), triangle.getTag());
	}
    }

    /*
     * Add a triangle whose type is Model3D.Triangle to the model.
     * Transforms that cause a modified triangle to be stored can
     * be configured using {@link #pushTransform(Transform3D)} and
     * {@link #popTransform()}.
     * @param triangle the triangle to add
     * @return the triangle stored by this model
     * @see #pushTransform(Transform3D)
     * @see #popTransform()
     */
    /*public*/ private Triangle addTriangle(Triangle triangle) {
	cachedArea = null;
	cachedVolume = null;
	manifoldComponents = null;

	if (triangle.entryNumber != -1) {
	    if (cubics == triangle.surface) return triangle;
	    if (cubicVertices == triangle.surface) return triangle;
	    // indicates that this triangle is being copied from a
	    // different model and was not a planar triangle
	    double[] tcoords = new double[48];
	    int type = triangle.surface.getSegment(triangle.entryNumber,
						   tcoords);
	    switch (type) {
	    case SurfaceIterator.CUBIC_PATCH:
		if (mustDoTransforms()) {
		    doTransforms(tcoords, 0, tcoords, 0, 16);
		}
		for (int i = 0; i < 48; i++) {
		    tcoords[i] = fix(tcoords[i]);
		}
		for (int i: patchCornerIndices) {
		    bestCoord(xCornerCoords, tcoords[i]);
		    bestCoord(yCornerCoords, tcoords[i+1]);
		    bestCoord(zCornerCoords, tcoords[i+2]);
		}
		cubics.addCubicPatch(tcoords,
				     triangle.surface.getSegmentTag
				     (triangle.entryNumber));
		if (bbvalid) {
		    for (int i = 0; i < 48; i += 3) {
			double x = tcoords[i];
			double y = tcoords[i+1];
			double z = tcoords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		return new Triangle(cubics, cubics.size()-1);
	    case SurfaceIterator.CUBIC_VERTEX:
		if (mustDoTransforms()) {
		    doTransforms(tcoords, 0, tcoords, 0, 5);
		}
		for (int i = 0; i < 15; i++) {
		    tcoords[i] = fix(tcoords[i]);
		}
		for (int i: cvCornerIndices) {
		    bestCoord(xCornerCoords, tcoords[i]);
		    bestCoord(yCornerCoords, tcoords[i+1]);
		    bestCoord(zCornerCoords, tcoords[i+2]);
		}
		cubicVertices.addCubicVertex(tcoords,
					     triangle.surface.getSegmentTag
					     (triangle.entryNumber));
		if (bbvalid) {
		    for (int i = 0; i < 15; i += 3) {
			double x = tcoords[i];
			double y = tcoords[i+1];
			double z = tcoords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		return new Triangle(cubicVertices, cubicVertices.size()-1);
	    case SurfaceIterator.CUBIC_TRIANGLE:
		if (mustDoTransforms()) {
		    doTransforms(tcoords, 0, tcoords, 0, 10);
		}
		for (int i = 0; i < 30; i++) {
		    tcoords[i] = fix(tcoords[i]);
		}
		for (int i: ctCornerIndices) {
		    bestCoord(xCornerCoords, tcoords[i]);
		    bestCoord(yCornerCoords, tcoords[i+1]);
		    bestCoord(zCornerCoords, tcoords[i+2]);
		}
		cubics.addCubicTriangle(tcoords,
					triangle.surface.getSegmentTag
					(triangle.entryNumber));
		if (bbvalid) {
		    for (int i = 0; i < 30; i += 3) {
			double x = tcoords[i];
			double y = tcoords[i+1];
			double z = tcoords[i+2];
			if (x < minx) {minx = x; needSTLBase= true;}
			if (x > maxx) {maxx = x; needSTLBase= true;}
			if (y < miny) {miny = y; needSTLBase= true;}
			if (y > maxy) {maxy = y; needSTLBase= true;}
			if (z < minz) {minz = z; needSTLBase= true;}
			if (z > maxz) {maxz = z; needSTLBase= true;}
		    }
		}
		return new Triangle(cubics, cubics.size()-1);
	    default:
		throw new IllegalStateException
		    (errorMsg("wrongGetSegmentType", type));
	    }
	}

	Object tag = triangle.tag;
	double src[] = {triangle.x1, triangle.y1, triangle.z1,
			triangle.x2, triangle.y2, triangle.z2,
			triangle.x3, triangle.y3, triangle.z3};
	if (mustDoTransforms()) {
	    double[] dst = new double[9];
	    doTransforms(src, 0, dst, 0, 3);
	    for (int i: triangleCornerIndices) {
		bestCoord(xCornerCoords, dst[i]);
		bestCoord(yCornerCoords, dst[i+1]);
		bestCoord(zCornerCoords, dst[i+2]);
	    }
	    triangle = new Triangle(dst[0], dst[1], dst[2],
				    dst[3], dst[4], dst[5],
				    dst[6], dst[7], dst[8],
				    triangle.color, triangle.tag);
	} else {
	    for (int i: triangleCornerIndices) {
		bestCoord(xCornerCoords, src[i]);
		bestCoord(yCornerCoords, src[i+1]);
		bestCoord(zCornerCoords, src[i+2]);
	    }
	}
	/*
	if (transforms.size() != 0) {
	    for (Transform3D transform: transforms) {
		Point3D.Double src = new Point3D.Double
		    (triangle.x1, triangle.y1, triangle.z1);
		Point3D.Double dst = new Point3D.Double();
		transform.transform(src, dst);
		double tx1 = dst.x;
		double ty1 = dst.y;
		double tz1 = dst.z;
		src.setLocation(triangle.x2, triangle.y2, triangle.z2);
		transform.transform(src, dst);
		double tx2 = dst.x;
		double ty2 = dst.y;
		double tz2 = dst.z;
		src.setLocation(triangle.x3, triangle.y3, triangle.z3);
		transform.transform(src, dst);
		double tx3 = dst.x;
		double ty3 = dst.y;
		double tz3 = dst.z;
		triangle = new Triangle(tx1, ty1, tz1,
					tx2, ty2, tz2,
					tx3, ty3, tz3,
					triangle.color, triangle.tag);
	    }
	}
	if (tstack.size() > 0
	    || tEulerPhi != 0.0 || tEulerTheta != 0.0 || tEulerPsi != 0.0
	    || txoffset != 0.0 || tyoffset != 0.0 || tzoffset != 0.0 ) {
	    double tx1 = triangle.x1;
	    double ty1 = triangle.y1;
	    double tz1 = triangle.z1;
	    double tx2 = triangle.x2;
	    double ty2 = triangle.y2;
	    double tz2 = triangle.z2;
	    double tx3 = triangle.x3;
	    double ty3 = triangle.y3;
	    double tz3 = triangle.z3;
	    for (TransformParms tp: tstack) {
		if (tp.tEulerPhi != 0.0 || tp.tEulerTheta != 0.0
		    || tp.tEulerPsi != 0.0
		    || tp.txoffset != 0.0 || tp.tyoffset != 0.0 
		    || tp.tzoffset != 0.0 ) {
		    double xx1 = tp.txoffset + tp.tmatrix[0][0] * tx1 
			+ tp.tmatrix[0][1] * ty1 + tp.tmatrix[0][2] * tz1;
		    double yy1 = tp.tyoffset + tp.tmatrix[1][0] * tx1 
			+ tp.tmatrix[1][1] * ty1 + tp.tmatrix[1][2] * tz1;
		    double zz1 = tp.tzoffset + tp.tmatrix[2][0] * tx1 
			+ tp.tmatrix[2][1] * ty1 + tp.tmatrix[2][2] * tz1;
		    double xx2 = tp.txoffset + tp.tmatrix[0][0] * tx2 
			+ tp.tmatrix[0][1] * ty2 + tp.tmatrix[0][2] * tz2;
		    double yy2 = tp.tyoffset + tp.tmatrix[1][0] * tx2 
			+ tp.tmatrix[1][1] * ty2 + tp.tmatrix[1][2] * tz2;
		    double zz2 = tp.tzoffset + tp.tmatrix[2][0] * tx2 
			+ tp.tmatrix[2][1] * ty2 + tp.tmatrix[2][2] * tz2;
		    double xx3 = tp.txoffset + tp.tmatrix[0][0] * tx3 
			+ tp.tmatrix[0][1] * ty3 + tp.tmatrix[0][2] * tz3;
		    double yy3 = tp.tyoffset + tp.tmatrix[1][0] * tx3
			+ tp.tmatrix[1][1] * ty3 + tp.tmatrix[1][2] * tz3;
		    double zz3 = tp.tzoffset + tp.tmatrix[2][0] * tx3 
			+ tp.tmatrix[2][1] * ty3 + tp.tmatrix[2][2] * tz3;
		    tx1 = xx1; ty1 = yy1; tz1 = zz1;
		    tx2 = xx2; ty2 = yy2; tz1 = zz2;
		    tx3 = xx3; ty3 = yy3; tz1 = zz3;
		}
	    }
	    double x1 = txoffset + tmatrix[0][0] * tx1 
		+ tmatrix[0][1] * ty1 + tmatrix[0][2] * tz1;
	    double y1 = tyoffset + tmatrix[1][0] * tx1 
		+ tmatrix[1][1] * ty1 + tmatrix[1][2] * tz1;
	    double z1 = tzoffset + tmatrix[2][0] * tx1 
		+ tmatrix[2][1] * ty1 + tmatrix[2][2] * tz1;
	    double x2 = txoffset + tmatrix[0][0] * tx2 
		+ tmatrix[0][1] * ty2 + tmatrix[0][2] * tz2;
	    double y2 = tyoffset + tmatrix[1][0] * tx2 
		+ tmatrix[1][1] * ty2 + tmatrix[1][2] * tz2;
	    double z2 = tzoffset + tmatrix[2][0] * tx2 
		+ tmatrix[2][1] * ty2 + tmatrix[2][2] * tz2;
	    double x3 = txoffset + tmatrix[0][0] * tx3 
		+ tmatrix[0][1] * ty3 + tmatrix[0][2] * tz3;
	    double y3 = tyoffset + tmatrix[1][0] * tx3
		+ tmatrix[1][1] * ty3 + tmatrix[1][2] * tz3;
	    double z3 = tzoffset + tmatrix[2][0] * tx3 
		+ tmatrix[2][1] * ty3 + tmatrix[2][2] * tz3;
	    Triangle oldTriangle = triangle;
	    triangle = new Triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3,
				    oldTriangle.color, tag);
	}
	*/
	if (tag != null && !(tag instanceof StackTraceElement[])) {
	    Triangle old = triangleMap.put(triangle.getTag(), triangle);
	    if (old != null) {
		if (bbvalid) {
		    if (old.x1 == maxx || old.x2 == maxx || old.x3 == maxx
			|| old.y1 == maxy || old.y2 == maxy || old.y3 == maxy
			|| old.z1 == maxz || old.z2 == maxz || old.z3 == maxz
			|| old.x1 == minx || old.x2 == minx || old.x3 == minx
			|| old.y1 == miny || old.y2 == miny || old.y3 == miny
			|| old.z1 == minz || old.z2 == minz || old.z3 == minz) {
			bbvalid = false;
			needSTLBase = true;
		    }
		}
	    }
	    updateBoundingBox(triangle);
	} else {
	    if (tag == null && stackTraceMode) {
		tag = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
		triangle.tag = tag;
	    }
	    triangleSet.add(triangle);
	    updateBoundingBox(triangle);
	}
	return triangle;
    }

    // used only internally so '=' OK.
    boolean containsTriangle(Triangle triangle) {
	return triangleSet.contains(triangle)
	    || triangleMap.containsKey(triangle);
    }

    /*
     * Add a triangle to the model, specifying the triangle by its vertices.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    @Override
    public Triangle addTriangle(double x1, double y1, double z1,
				double x2, double y2, double z2,
				double x3, double y3, double z3)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3));
    }

    /*
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    @Override
    public Triangle addFlippedTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1, x3, y3, z3, x2,  y2, z2));
    }


    /*
     * Add a triangle to the model, specifying the triangle by its vertices
     * and its color.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    @Override
    public Triangle addTriangle(double x1, double y1, double z1,
				double x2, double y2, double z2,
				double x3, double y3, double z3,
				Color color)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1,
					x2, y2, z2, 
					x3, y3, z3,
					color));
    }

    /*
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices and color.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @return the triangle stored by this model
     * @exception IllegalArgumentException one of the first 9 arguments
     *            had the value Double.NaN
     */
    @Override
    public Triangle addFlippedTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3,
				       Color color)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1, 
					x3, y3, z3,
					x2, y2, z2,
					color));
    }


    /*
     * Add a triangle to the model, specifying the triangle by its vertices
     * and its color, also specifying a tag.
     * The orientation of the triangle is determined by the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @param tag the tag; null if there is none
     * @return the triangle stored by this model
     * @exception IllegalArgumentException a tag was a triangle or
     *            one of the first 9 arguments had the value
     *            Double.NaN
     */
    @Override
    public Triangle addTriangle(double x1, double y1, double z1,
				double x2, double y2, double z2,
				double x3, double y3, double z3,
				Color color, Object tag)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1,
					x2, y2, z2,
					x3, y3, z3,
					color, tag));
    }

    /*
     * Add a flipped triangle to the model, specifying the triangle by
     * its vertices and color, also specifying a tag.
     * The orientation of the triangle is the opposite to what the
     * right-hand rule when going from vertex 1 to vertex 2 to
     * vertex 3 would suggest.
     * @param x1 the x coordinate of vertex 1
     * @param y1 the y coordinate of vertex 1
     * @param z1 the z coordinate of vertex 1
     * @param x2 the x coordinate of vertex 2
     * @param y2 the y coordinate of vertex 2
     * @param z2 the z coordinate of vertex 2
     * @param x3 the x coordinate of vertex 3
     * @param y3 the y coordinate of vertex 3
     * @param z3 the z coordinate of vertex 3
     * @param color the color of the triangle; null if none is specified
     * @param tag the tag; null if there is none
     * @return the triangle stored by this model
     * @exception IllegalArgumentException a tag was a triangle or
     *            one of the first 9 arguments had the value
     *            Double.NaN
     */
    @Override
    public Triangle addFlippedTriangle(double x1, double y1, double z1,
				       double x2, double y2, double z2,
				       double x3, double y3, double z3,
				       Color color, Object tag)
	throws IllegalArgumentException
    {
	return addTriangle(new Triangle(x1, y1, z1,
					x3, y3, z3,
					x2, y2, z2,
					color, tag));
    }

    /**
     * Remove a triangle.
     * Only triangles with tags can be removed.
     * @param tag the triangle's tag.
     */
    public void removeTriangle(Object tag) {
	cachedArea = null;
	cachedVolume = null;
	manifoldComponents = null;
	if (tag instanceof Triangle) {
	    Object tagtag = ((Triangle) tag).getTag();
	    if (tagtag == null) {
		Triangle triangle = (Triangle) tag;
		if (triangleSet.remove(triangle)) {
		    if (bbvalid) {
			if (triangle.x1 == maxx
			    || triangle.x2 == maxx
			    || triangle.x3 == maxx
			    || triangle.y1 == maxy
			    || triangle.y2 == maxy
			    || triangle.y3 == maxy
			    || triangle.z1 == maxz
			    || triangle.z2 == maxz
			    || triangle.z3 == maxz
			    || triangle.x1 == minx
			    || triangle.x2 == minx
			    || triangle.x3 == minx
			    || triangle.y1 == miny
			    || triangle.y2 == miny
			    || triangle.y3 == miny
			    || triangle.z1 == minz
			    || triangle.z2 == minz
			    || triangle.z3 == minz) {
			    bbvalid = false;
			    needSTLBase = true;
			}
		    }
		}
		return;
	    } else {
		tag = tagtag;
	    }
	}
	Triangle old = triangleMap.remove(tag);
	if (old != null) {
	    if (bbvalid) {
		if (old.x1 == maxx || old.x2 == maxx || old.x3 == maxx
		    || old.y1 == maxy || old.y2 == maxy || old.y3 == maxy
		    || old.z1 == maxz || old.z2 == maxz || old.z3 == maxz
		    || old.x1 == minx || old.x2 == minx || old.x3 == minx
		    || old.y1 == miny || old.y2 == miny || old.y3 == miny
		    || old.z1 == minz || old.z2 == minz || old.z3 == minz) {
		    bbvalid = false;
		    needSTLBase = true;
		}
	    }
	}
    }

    private HashMap<Object, LineSegment> lineSegmentMap =
	new HashMap<Object, LineSegment>();

    private HashSet<LineSegment> lineSegmentSet =
	new HashSet<LineSegment>();

    /**
     * Add a line segment.
     * @param segment the line segment to add.
     */
    public LineSegment addLineSegment(LineSegment segment) {
	Object tag = segment.getTag();
	if (tag == null) {
	    lineSegmentSet.add(segment);
	    return segment;
	}
	return lineSegmentMap.put(segment.getTag(), segment);
    }
    /**
     * Add a line segment given the coordinates of its end points.
     * The line segment is a straight line going from point p1 to
     * another point p2.
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param z1 the z coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @param z2 the z coordinate of point 2
     */
    public LineSegment addLineSegment(double x1, double y1, double z1,
				      double x2, double y2, double z2)
    {
	return addLineSegment(new LineSegment(x1,y1,z1, x2,y2,z2));
    }

    /**
     * Add a line segment given the coordinates of its end points and color.
     * The line segment is a straight line going from point p1 to
     * another point p2.
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param z1 the z coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @param z2 the z coordinate of point 2
     * @param color the line segment's color; null if no color is specified
     */
    public LineSegment addLineSegment(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      Color color)
    {
	return addLineSegment(new LineSegment(x1,y1,z1, x2,y2,z2, color));
    }

    /**
     * Add a line segment given the coordinates of its end points, its color,
     * and a tag.
     * The line segment is a straight line going from point p1 to
     * another point p2.
     * @param x1 the x coordinate of point 1
     * @param y1 the y coordinate of point 1
     * @param z1 the z coordinate of point 1
     * @param x2 the x coordinate of point 2
     * @param y2 the y coordinate of point 2
     * @param z2 the z coordinate of point 2
     * @param color the line segment's color; null if no color is specified
     * @param tag a tag for the line segment; null if none is specified
     */
    public LineSegment addLineSegment(double x1, double y1, double z1,
				      double x2, double y2, double z2,
				      Color color, Object tag) {
	return addLineSegment(new LineSegment(x1,y1,z1, x2,y2,z2, color, tag));
    }

    /**
     * Remove a line segment.
     * @param tag a tag naming the line segment
     */
    public void removeLineSegment(Object tag) {
	if (tag instanceof LineSegment) {
	    Object tagtag = ((LineSegment) tag).getTag();
	    if (tagtag == null) {
		lineSegmentSet.remove(tag);
		return;
	    } else {
		tag = tagtag;
	    }
	}
	lineSegmentMap.remove(tag);
    }


    private void addTriangleToList(Model3D.ImageDataImpl idata,
				   Triangle triangle,
				   double xx1, double yy1, double zz1,
				   double xx2, double yy2, double zz2,
				   double xx3, double yy3, double zz3,
				   double nxx, double nyy, double nzz,
				   boolean[] edgeMask)
    {
	RenderList renderList = idata.renderList;
	double zz;
	int indmax;
	if (zz1 >= zz2) {
	    if (zz1 >= zz3) {
		zz = zz1;
		indmax = 1;
	    } else {
		zz = zz3;
		indmax = 3;
	    }
	} else if (zz2 >= zz3) {
	    zz = zz2;
	    indmax = 2;
	} else {
	    zz = zz3;
	    indmax = 3;
	}

	double zzmin;
	int indmin;
	if (zz1 <= zz2) {
	    if (zz1 <= zz3) {
		zzmin = zz1;
		indmin = 1;
	    } else {
		zzmin = zz3;
		indmin = 3;
	    }
	} else if (zz2 <= zz3) {
	    zzmin = zz2;
	    indmin = 2;
	} else {
	    zzmin = zz3;
	    indmin = 3;
	}

	double xx = (xx1 < xx2)? xx2: xx1;
	xx = (xx < xx3)? xx3: xx;
	double xxmin = (xx1 < xx2)? xx1: xx2;
	xxmin = (xxmin < xx3)? xxmin: xx3;

	double yy = (yy1 < yy2)? yy2: yy1;
	yy = (yy < yy3)? yy3: yy;
	double yymin = (yy1 < yy2)? yy1: yy2;
	yymin = (yymin < yy3)? yymin: yy3;


	if (idata.delta > 0.0 && ((zz - zzmin) > idata.delta
				  || xx - xxmin > idata.delta
				  || yy - yymin > idata.delta)) {
	    // partition triangle into 4 subtriangles for rendering
	    if (edgeMask == null) {
		edgeMask = new boolean[3];
		for (int i = 0; i < 3; i++) edgeMask[i] = true;
	    }
	    double dx = xx1 - xx2;
	    double dy = yy1 - yy2;
	    double dz = zz1 - zz1;
	    double asq = dx*dx + dy*dy + dz*dz;
	    dx = xx2 - xx3; dy = yy2 - yy3; dz = zz2 - zz3;
	    double bsq = dx*dx + dy*dy + dz*dz;
	    dx = xx3 - xx1; dy = yy3 - yy1; dz = zz3 - zz1;
	    double csq = dx*dx + dy*dy + dz*dz;
	    double dsq = idata.delta * idata.delta;
	    double xa = (xx1 + xx2) / 2.0;
	    double ya = (yy1 + yy2) / 2.0;
	    double za = (zz1 + zz2) / 2.0;
	    double xb = (xx2 + xx3) / 2.0;
	    double yb = (yy2 + yy3) / 2.0;
	    double zb = (zz2 + zz3) / 2.0;
	    double xc = (xx3 + xx1) / 2.0;
	    double yc = (yy3 + yy1) / 2.0;
	    double zc = (zz3 + zz1) / 2.0;

	    boolean[] edgeMask1 = new boolean[3];
	    boolean[] edgeMask2 = new boolean[3];
	    int ecase = 0;
	    if (asq <= dsq) {
		if (bsq > csq) {
		    ecase = 2;
		} else {
		    ecase = 3;
		}
	    } else if (bsq <= dsq) {
		if ( asq > csq) {
		    ecase = 1;
		} else {
		    ecase = 3;
		}
	    } else if (csq <= dsq) {
		if (asq > bsq) {
		    ecase = 1;
		} else {
		    ecase = 2;
		}
	    } else if (asq >= bsq + csq) {
		ecase = 1;
	    } else if (bsq >= asq + csq) {
		ecase = 2;
	    } else if (csq >= asq + bsq) {
		ecase = 3;
	    }
	    switch (ecase) {
	    case 1:
		{
		    edgeMask1[1] = edgeMask[0];
		    edgeMask1[2] = edgeMask[1];
		    // edgeMask1 = null;
		    addTriangleToList(idata, triangle,
				      xx3, yy3, zz3, xx1, yy1, zz1, xa, ya, za,
				      nxx, nyy, nzz, edgeMask1);
		    edgeMask2[2] = edgeMask[1];
		    edgeMask2[0] = edgeMask[2];
		    // edgeMask2 = null;
		    addTriangleToList(idata, triangle,
				      xx3, yy3, zz3, xa, ya, za, xx2, yy2, zz2,
				      nxx, nyy, nzz, edgeMask2);
		    break;
		}
	    case 2:
		{
		    edgeMask1[1] = edgeMask[1];
		    edgeMask1[2] = edgeMask[2];
		    //  edgeMask1 = null;
		    addTriangleToList(idata, triangle,
				      xx1, yy1, zz1, xx2, yy2, zz2, xb, yb, zb,
				      nxx, nyy, nzz, edgeMask1);
		    edgeMask2[2] = edgeMask[2];
		    edgeMask2[0] = edgeMask[0];
		    //  edgeMask2 = null;
		    addTriangleToList(idata, triangle,
				      xx1, yy1, zz1, xb, yb, zb, xx3, yy3, zz3,
				      nxx, nyy, nzz, edgeMask2);
		    break;
		}
	    case 3:
		{
		    edgeMask1[1] = edgeMask[2];
		    edgeMask1[2] = edgeMask[0];
		    // edgeMask1 = null;
		    addTriangleToList(idata, triangle,
				      xx2, yy2, zz2, xx3, yy3, zz3, xc, yc, zc,
				      nxx, nyy, nzz, edgeMask1);
		    edgeMask2[2] = edgeMask[0];
		    edgeMask2[0] = edgeMask[1];
		    // edgeMask2 = null;
		    addTriangleToList(idata, triangle,
				      xx2, yy2, zz2, xc, yc, zc, xx1, yy1, zz1,
				      nxx, nyy, nzz, edgeMask2);
		    break;
		}
	    default:
		{
		    boolean[] edgeMaskCenter = new boolean[3];
		    edgeMask1[0] = edgeMask[0];
		    edgeMask1[1] = edgeMask[1];
		    edgeMask2[0] = edgeMask[1];
		    edgeMask2[1] = edgeMask[2];
		    boolean[] edgeMask3 = new boolean[3];
		    edgeMask3[0] = edgeMask[2];
		    edgeMask3[1] = edgeMask[0];
		    if (false) {
			// SHOW EDGES FOR DEBUGGING.
			edgeMaskCenter = null;
			edgeMask1 = null;
			edgeMask2 = null;
			edgeMask3 = null;
		    }
		    addTriangleToList(idata, triangle,
				      xa, ya, za, xb, yb, zb, xc, yc, zc,
				      nxx, nyy, nzz, edgeMaskCenter);
		    addTriangleToList(idata, triangle,
				      xx1, yy1, zz1, xa, ya, za, xc, yc, zc,
				      nxx, nyy, nzz, edgeMask1);
		    addTriangleToList(idata, triangle,
				      xx2, yy2, zz2, xb, yb, zb, xa, ya, za,
				      nxx, nyy, nzz, edgeMask2);
		    addTriangleToList(idata, triangle,
				      xx3, yy3, zz3, xc, yc, zc, xb, yb, zb,
				      nxx, nyy, nzz, edgeMask3);
		}
	    }
	} else {
	    double xmin, ymin, zmin;
	    double xmax, ymax, zmax;
	    double xother, yother, zother;
	    int indother = 1;
	    while (indother != indmax && indother != indmin) {
		indother = (indother % 3) + 1;
	    }
	    switch (indother) {
	    case 1:
		xother = xx1; yother = yy1; zother = zz1;
		break;
	    case 2:
		xother = xx2; yother = yy3; zother = zz2;
		break;
	    case 3:
		xother = xx3; yother = yy3; zother = zz3;
		break;
	    default:
		throw new RuntimeException(errorMsg("badCase"));
	    }
	    switch (indmin) {
	    case 1:
		xmin = xx1; ymin = yy1; zmin = zz1;
		break;
	    case 2:
		xmin = xx2; ymin = yy3; zmin = zz2;
		break;
	    case 3:
		xmin = xx3; ymin = yy3; zmin = zz3;
		break;
	    default:
		throw new RuntimeException(errorMsg("badCase"));
	    }
	    switch (indmax) {
	    case 1:
		xmax = xx1; ymax = yy1; zmax = zz1;
		break;
	    case 2:
		xmax = xx2; ymax = yy3; zmax = zz2;
		break;
	    case 3:
		xmax = xx3; ymax = yy3; zmax = zz3;
		break;
	    default:
		throw new RuntimeException(errorMsg("badCase"));
	    }

	    float xcf1 = (float)(idata.xorigin + xmax * idata.scaleFactor);
	    float ycf1 = (float)(idata.yorigin + ymax * idata.scaleFactor);
	    float xcf2 = xcf1;
	    float ycf2 = ycf1;
	    if (idata.colorFactor > 0.0) {
		if (zmin != zmax) {
		    // using vector identity
		    // A X (B X C) = (A . C)B - (A . B)C
		    // on n X (n X z) 
		    double np = Math.sqrt(1.0 - (nzz * nzz));
		    double npx = (nzz * nxx) / np;
		    double npy = (nzz * nyy) / np;
		    double npz = ((nzz * nzz) - 1.0)/ np;
		    double s = (zmin - zmax) * npz;

		    double endx = xmax + npx * s;
		    double endy = ymax + npy * s;
		    double endz = ymax + npz * s;
		    // for a Buffered Image, the scale factor between user
		    // space and device space is 1.0.
		    xcf2 = (float)(idata.xorigin + endx * idata.scaleFactor);
		    ycf2 = (float)(idata.yorigin + endy * idata.scaleFactor);
		}
	    }
	   
	    PolyLine p = new PolyLine(idata.xorigin + xx1 * idata.scaleFactor,
				      idata.yorigin - yy1 * idata.scaleFactor,
				      idata.xorigin + xx2 * idata.scaleFactor,
				      idata.yorigin - yy2 * idata.scaleFactor,
				      idata.xorigin + xx3 * idata.scaleFactor,
				      idata.yorigin - yy3 * idata.scaleFactor);

	    if (nzz >= 0) {
		Color tc = (triangle.color == null)? idata.triangleColor:
		    triangle.color;
		double dotproduct = idata.lightsourceNx *nxx +
		    idata.lightsourceNy *nyy + idata.lightsourceNz * nzz;
		if (dotproduct < 0.0) {
		    dotproduct = 0.0;
		}
		int rlevel = (int)Math.round(tc.getRed() * dotproduct);
		int glevel = (int)Math.round(tc.getGreen() * dotproduct);
		int blevel = (int)Math.round(tc.getBlue() * dotproduct);
		int alevel = tc.getAlpha();
		Color c = new Color(rlevel, glevel, blevel, alevel);
		renderList.add(p, c, zz, zzmin, nzz, xcf1, ycf1, xcf2, ycf2,
			       triangle.tag, edgeMask);
	    } else if (idata.backsideColor != null) {
		renderList.add(p, idata.backsideColor, zz, zzmin, nzz,
			       xcf1, ycf1, xcf2, ycf2,
			       triangle.tag, edgeMask);
	    }
	}
    }

    private void doRenderIteration(Model3D.ImageDataImpl idata,
				   Triangle triangle)
    {
	double x1 = triangle.x1;
	double y1 = triangle.y1;
	double z1 = triangle.z1;

	double x2 = triangle.x2;
	double y2 = triangle.y2;
	double z2 = triangle.z2;

	double x3 = triangle.x3;
	double y3 = triangle.y3;
	double z3 = triangle.z3;

	double nx = triangle.nx;
	double ny = triangle.ny;
	double nz = triangle.nz;
	// System.out.println("render: nz = " + nz);

	if (idata.rotMoveOrigin) {
	    if (idata.xoriginBR != 0.0) x1 -= idata.xoriginBR;
	    if (idata.yoriginBR != 0.0) y1 -= idata.xoriginBR;
	    if (idata.zoriginBR != 0.0) z1 -= idata.xoriginBR;

	    if (idata.xoriginBR != 0.0) x2 -= idata.xoriginBR;
	    if (idata.yoriginBR != 0.0) y2 -= idata.xoriginBR;
	    if (idata.zoriginBR != 0.0) z2 -= idata.xoriginBR;

	    if (idata.xoriginBR != 0.0) x3 -= idata.xoriginBR;
	    if (idata.yoriginBR != 0.0) y3 -= idata.xoriginBR;
	    if (idata.zoriginBR != 0.0) z3 -= idata.xoriginBR;
	}

	double xx1; double yy1; double zz1;
	double xx2; double yy2; double zz2;
	double xx3; double yy3; double zz3;
	double nzz; double nxx; double nyy;

	if (idata.eulerPhi == 0.0 && idata.eulerTheta == 0.0 
	    && idata.eulerPsi == 0.0) {
	    xx1 = x1; yy1 = y1; zz1 = z1;
	    xx2 = x2; yy2 = y2; zz2 = z2;
	    xx3 = x3; yy3 = y3; zz3 = z3;
	    nzz = nz;
	    nxx = nx;
	    nyy = ny;
	} else {
	    double[][] matrix = idata.matrix;
	    xx1 = matrix[0][0]*x1 + matrix[0][1]*y1 + matrix[0][2]*z1;
	    yy1 = matrix[1][0]*x1 + matrix[1][1]*y1 + matrix[1][2]*z1;
	    zz1 = matrix[2][0]*x1 + matrix[2][1]*y1 + matrix[2][2]*z1;

	    xx2 = matrix[0][0]*x2 + matrix[0][1]*y2 + matrix[0][2]*z2;
	    yy2 = matrix[1][0]*x2 + matrix[1][1]*y2 + matrix[1][2]*z2;
	    zz2 = matrix[2][0]*x2 + matrix[2][1]*y2 + matrix[2][2]*z2;

	    xx3 = matrix[0][0]*x3 + matrix[0][1]*y3 + matrix[0][2]*z3;
	    yy3 = matrix[1][0]*x3 + matrix[1][1]*y3 + matrix[1][2]*z3;
	    zz3 = matrix[2][0]*x3 + matrix[2][1]*y3 + matrix[2][2]*z3;

	    nxx = matrix[0][0]*nx + matrix[0][1]*ny + matrix[0][2]*nz;
	    nyy = matrix[1][0]*nx + matrix[1][1]*ny + matrix[1][2]*nz;
	    nzz = matrix[2][0]*nx + matrix[2][1]*ny + matrix[2][2]*nz;
	}
	    
	if (idata.rotMoveOrigin) {
	    x1 = xx1; y1 = yy1; z1 = zz1;
	    x2 = xx2; y2 = yy2; z2 = zz2;
	    x3 = xx3; y3 = yy3; z3 = zz3;
	} else {
	    x1 = (idata.xoriginAR == 0.0 && idata.xoriginBR == 0.0)? xx1:
		(xx1 + idata.xoriginBR - idata.xoriginAR);
	    y1 = (idata.yoriginAR == 0.0 && idata.yoriginBR == 0.0)? yy1:
		(yy1 + idata.yoriginBR - idata.yoriginAR);
	    z1 = (idata.zoriginAR == 0.0 && idata.zoriginBR == 0.0)? zz1:
		(zz1 + idata.zoriginBR - idata.zoriginAR);

	    x2 = (idata.xoriginAR == 0.0 && idata.xoriginBR == 0.0)? xx2:
		(xx2 + idata.xoriginBR - idata.xoriginAR);
	    y2 = (idata.yoriginAR == 0.0 && idata.yoriginBR == 0.0)? yy2:
		(yy2 + idata.yoriginBR - idata.yoriginAR);
	    z2 = (idata.zoriginAR == 0.0 && idata.zoriginBR == 0.0)? zz2:
		(zz2 + idata.zoriginBR - idata.zoriginAR);

	    x3 = (idata.xoriginAR == 0.0 && idata.xoriginBR == 0.0)? xx3:
		(xx3 + idata.xoriginBR - idata.xoriginAR);
	    y3 = (idata.yoriginAR == 0.0 && idata.yoriginBR == 0.0)?
		yy3: (yy3 + idata.yoriginBR - idata.yoriginAR);
	    z3 = (idata.zoriginAR == 0.0 && idata.zoriginBR == 0.0)? zz3:
		(zz3 + idata.zoriginBR - idata.zoriginAR);
	}

	xx1 = (idata.xtranslation == 0.0)? x1: (x1 - idata.xtranslation);
	yy1 = (idata.ytranslation == 0.0)? y1: (y1 - idata.ytranslation);
	zz1 = z1;

	xx2 = (idata.xtranslation == 0.0)? x2: (x2 - idata.xtranslation);
	yy2 = (idata.ytranslation == 0.0)? y2: (y2 - idata.ytranslation);
	zz2 = z2;

	xx3 = (idata.xtranslation == 0.0)? x3: (x3 - idata.xtranslation);
	yy3 = (idata.ytranslation == 0.0)? y3: (y3 - idata.ytranslation);
	zz3 = z3;

	addTriangleToList(idata, triangle,
			  xx1, yy1, zz1, xx2, yy2, zz2, xx3, yy3, zz3,
			  nxx, nyy, nzz, null);
    }

    private void doLineSegmentIteration(Model3D.ImageDataImpl idata, 
					LineSegment segment)
    {
	RenderList renderList = idata.renderList;

	double x1 = segment.x1;
	double y1 = segment.y1;
	double z1 = segment.z1;

	double x2 = segment.x2;
	double y2 = segment.y2;
	double z2 = segment.z2;

	double nx = segment.nx;
	double ny = segment.ny;
	double nz = segment.nz;
	// System.out.println("render: nz = " + nz);

	if (idata.xoriginBR != 0.0) x1 -= idata.xoriginBR;
	if (idata.yoriginBR != 0.0) y1 -= idata.yoriginBR;
	if (idata.zoriginBR != 0.0) z1 -= idata.zoriginBR;

	if (idata.xoriginBR != 0.0) x2 -= idata.xoriginBR;
	if (idata.yoriginBR != 0.0) y2 -= idata.yoriginBR;
	if (idata.zoriginBR != 0.0) z2 -= idata.zoriginBR;


	double xx1; double yy1; double zz1;
	double xx2; double yy2; double zz2;
	double nzz; double nxx; double nyy;

	if (idata.eulerPhi == 0.0 && idata.eulerTheta == 0.0
	    && idata.eulerPsi == 0.0) {
	    xx1 = x1; yy1 = y1; zz1 = z1;
	    xx2 = x2; yy2 = y2; zz2 = z2;
	    nzz = nz;
	    nxx = nx;
	    nyy = ny;
	} else {
	    final double matrix[][] = idata.matrix;
	    xx1 = matrix[0][0]*x1 + matrix[0][1]*y1 + matrix[0][2]*z1;
	    yy1 = matrix[1][0]*x1 + matrix[1][1]*y1 + matrix[1][2]*z1;
	    zz1 = matrix[2][0]*x1 + matrix[2][1]*y1 + matrix[2][2]*z1;

	    xx2 = matrix[0][0]*x2 + matrix[0][1]*y2 + matrix[0][2]*z2;
	    yy2 = matrix[1][0]*x2 + matrix[1][1]*y2 + matrix[1][2]*z2;
	    zz2 = matrix[2][0]*x2 + matrix[2][1]*y2 + matrix[2][2]*z2;

	    nxx = matrix[0][0]*nx + matrix[0][1]*ny + matrix[0][2]*nz;
	    nyy = matrix[1][0]*nx + matrix[1][1]*ny + matrix[1][2]*nz;
	    nzz = matrix[2][0]*nx + matrix[2][1]*ny + matrix[2][2]*nz;
	}
	    
	if (idata.rotMoveOrigin) {
	    x1 = xx1; y1 = yy1; z1 = zz1;
	    x2 = xx2; y2 = yy2; z2 = zz2;
	} else {
	    x1 = (idata.xoriginAR == 0.0)? xx1: (xx1 + idata.xoriginBR);
	    y1 = (idata.yoriginAR == 0.0)? yy1: (yy1 + idata.yoriginBR);
	    z1 = (idata.zoriginAR == 0.0)? zz1: (zz1 + idata.zoriginBR);

	    x2 = (idata.xoriginAR == 0.0)? xx2: (xx2 + idata.xoriginBR);
	    y2 = (idata.yoriginAR == 0.0)? yy2: (yy2 + idata.yoriginBR);
	    z2 = (idata.zoriginAR == 0.0)? zz2: (zz2 + idata.zoriginBR);
	}

	xx1 = (idata.xtranslation == 0.0)? x1: (x1 - idata.xtranslation);
	yy1 = (idata.ytranslation == 0.0)? y1: (y1 - idata.ytranslation);
	zz1 = z1;

	xx2 = (idata.xtranslation == 0.0)? x2: (x2 - idata.xtranslation);
	yy2 = (idata.ytranslation == 0.0)? y2: (y2 - idata.ytranslation);
	zz2 = z2;

	PolyLine p = new PolyLine(idata.xorigin + xx1 * idata.scaleFactor,
				  idata.yorigin - yy1 * idata.scaleFactor,
				  idata.xorigin + xx2 * idata.scaleFactor,
				  idata.yorigin - yy2 * idata.scaleFactor);

	double zz = (zz1 < zz2)? zz2: zz1;
	double zzmin = (zz1 < zz2)? zz1: zz2;


	if (nx == 0.0 && ny == 0.0 && nz == 0.0) {
	    nzz = 1.0;
	}
	if (nzz >= 0) {
	    Color sc = (segment.color == null)? idata.segmentColor:
		segment.color;

	    renderList.add(p, sc, zz, zzmin, nzz, 0.0F, 0.0F, 0.0F, 0.0F,
			   segment.tag);
	} else if (idata.backsideSegmentColor != null) {
	    renderList.add(p, idata.backsideSegmentColor,
			   zz, zzmin, nzz, 0.0F, 0.0F, 0.0F, 0.0F, segment.tag);
	}
    }

    /**
     * Set image parameters.
     * The image data (idata) will be modified so that all the objects are
     * within the border of the image.
     * @param idata image data to read and modify
     */
    public ImageParams setImageParameters(Model3D.ImageData idata) {
	return setImageParameters(idata, -1.0, 1.0, 0.0, 0.0);
    }

    /**
     * Set image parameters given a border.
     * The image data (id) will be modified based on the other
     * arguments so that all objects fit within the image and its
     * border.
     * @param idata image data to read and modify
     * @param border the minimum distance in user space from the edges of
     *        an image to the object(s) being displayed; -1 for a default
     */
    public ImageParams setImageParameters(Model3D.ImageData idata,
						double border)
    {
	return setImageParameters(idata, border, 1.0, 0.0, 0.0);
	
    }


    /**
     * Set image parameters given a border and magnification.
     * The image data (id) will be modified based on the other
     * arguments.  The magnification field provides a factor by which
     * an object is scaled up, so that a value of 2.0 doubles the size
     * of the objects in an image. The fractional positions are 0.
     *
     * @param idata image data to read and modify
     * @param border the minimum distance in user space from the edges of
     *        an image to the object(s) being displayed; -1 for a default
     * @param magnification  The magnification of the image.
     */
    public ImageParams
	setImageParameters(Model3D.ImageData idata,
			   double border, double magnification)
    {
	return setImageParameters(idata, border, magnification, 0.0, 0.0);
    }


    /**
     * Set image parameters given a magnification and fractional positions.
     * The image data (id) will be modified based on the other
     * arguments.  The magnification field provides a factor by which
     * an object is scaled up, so that a value of 2.0 doubles the size
     * of the objects in an image. If xfract is 0.0, the minimum value
     * of x for any object will appear at the left border.  If xfract
     * is 1.0, the maximum value of x for any object will appear at the
     * right border. If yfract is 0, the minimum value of y for any object
     * will appear at the bottom border.  If yfract is 1.0, the maximum
     * value of y for any object will appear at the top border. A default
     * border will be used.
     *
     * @param idata image data to read and modify
     * @param magnification  The magnification of the image.
     * @param xfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the x axis, with values
     *        in the range [0.0, 1.0]
     * @param yfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the y axis, with values
     *        in the range [0.0, 1.0]
     */
    public ImageParams
	setImageParameters(Model3D.ImageData idata,
			   double magnification,
			   double xfract, double yfract)
    {
	return setImageParameters(idata, -1.0,
				  magnification, xfract, yfract);
    }


    /**
     * Set image parameters given a border, magnification, and fractional
     * positions.
     * The image data (id) will be modified based on the other
     * arguments.  The magnification field provides a factor by which
     * an object is scaled up, so that a value of 2.0 doubles the size
     * of the objects in an image. If xfract is 0.0, the minimum value
     * of x for any object will appear at the left border.  If xfract
     * is 1.0, the maximum value of x for any object will appear at the
     * right border. If yfract is 0, the minimum value of y for any object
     * will appear at the bottom border.  If yfract is 1.0, the maximum
     * value of y for any object will appear at the top border.
     *
     * @param id image data to read and modify
     * @param border the minimum distance in user space from the edges of
     *        an image to the object(s) being displayed; -1 for a default
     * @param magnification  The magnification of the image.
     * @param xfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the x axis, with values
     *        in the range [0.0, 1.0]
     * @param yfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the y axis, with values
     *        in the range [0.0, 1.0]
     */
    public ImageParams
	setImageParameters(Model3D.ImageData id, double border,
			   double magnification,
			   double xfract, double yfract)
    {
	return setImageParameters(id, border, magnification, xfract, yfract,
				  true);
    }

    /**
     * Set image parameters given a border, magnification, fractional
     * positions, and a scaling option
     * The image data (id) will be modified based on the other
     * arguments.  The magnification field provides a factor by which
     * an object is scaled up, so that a value of 2.0 doubles the size
     * of the objects in an image. If xfract is 0.0, the minimum value
     * of x for any object will appear at the left border.  If xfract
     * is 1.0, the maximum value of x for any object will appear at the
     * right border. If yfract is 0, the minimum value of y for any object
     * will appear at the bottom border.  If yfract is 1.0, the maximum
     * value of y for any object will appear at the top border.
     *
     * @param id image data to read and modify
     * @param border the minimum distance in user space from the edges of
     *        an image to the object(s) being displayed; -1 for a default
     * @param magnification  The magnification of the image.
     * @param xfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the x axis, with values
     *        in the range [0.0, 1.0]
     * @param yfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the y axis, with values
     *        in the range [0.0, 1.0]
     * @param changeScale true if the scale should be changed; false if not
     */
    public ImageParams
	setImageParameters(Model3D.ImageData id, double border,
			   double magnification,
			   double xfract, double yfract,
			   boolean changeScale)
    {
	ImageParams result = new ImageParams();
	ImageDataImpl idata = id.getImageData();
	if (border < 0.0) {
	    double bx = 0.01 * idata.getFloatWidth();
	    double by = 0.01 * idata.getFloatHeight();
	    border = (bx > by)? by: bx;
	}

	result.border = border;
	result.imageHeight = idata.getFloatHeight();
	result.imageWidth = idata.getFloatWidth();

	double height = result.imageHeight - 2.0 * border;
	double width = result.imageWidth - 2.0 * border;
	if (xfract < 0.0 || xfract > 1.0 || yfract < 0.0 || yfract > 1.0
	    || magnification <= 0.0 || height <= 0.0 || width <= 0.0) {
	    String arglist =
		((xfract < 0.0 || xfract > 1.0)? (" xfract=" + xfract): "")
		+ ((yfract < 0.0 || yfract > 1.0)? (" yfract=" + yfract): "")
		+ ((magnification <= 0.0)?
		   (" magnification=" + magnification): "")
		+ ((height <= 0.0 || width <= 0.0)? (" border=" + border): "");
	    throw new IllegalArgumentException
		(errorMsg("argsOutOfRange", arglist));
	}

	double defaultMinX = Double.POSITIVE_INFINITY;
	double defaultMaxX = Double.NEGATIVE_INFINITY;
	double defaultMinY = Double.POSITIVE_INFINITY;
	double defaultMaxY = Double.NEGATIVE_INFINITY;
	if (cubics.size() > 0 || cubicVertices.size() > 0) {
	    Iterator<Triangle> it = tessellate();
	    while (it.hasNext()) {
		Triangle triangle = it.next();
		double xx1, xx2, xx3, yy1, yy2, yy3;
		if (idata.eulerPhi == 0.0 && idata.eulerTheta == 0.0 
		    && idata.eulerPsi == 0.0) {
		    xx1 = triangle.x1; yy1 = triangle.y1;
		    xx2 = triangle.x2; yy2 = triangle.y2;
		    xx3 = triangle.x3; yy3 = triangle.y3;
		} else {
		    double[][] matrix = idata.matrix;
		    xx1 = matrix[0][0]*triangle.x1 + matrix[0][1]*triangle.y1 
			+ matrix[0][2]*triangle.z1;
		    yy1 = matrix[1][0]*triangle.x1 + matrix[1][1]*triangle.y1
			+ matrix[1][2]*triangle.z1;
		    xx2 = matrix[0][0]*triangle.x2 + matrix[0][1]*triangle.y2
			+ matrix[0][2]*triangle.z2;
		    yy2 = matrix[1][0]*triangle.x2 + matrix[1][1]*triangle.y2
			+ matrix[1][2]*triangle.z2;
		    xx3 = matrix[0][0]*triangle.x3 + matrix[0][1]*triangle.y3
			+ matrix[0][2]*triangle.z3;
		    yy3 = matrix[1][0]*triangle.x3 + matrix[1][1]*triangle.y3
			+ matrix[1][2]*triangle.z3;
		}
		if (xx1 < defaultMinX) defaultMinX = xx1;
		if (xx1 > defaultMaxX) defaultMaxX = xx1;
		if (xx2 < defaultMinX) defaultMinX = xx2;
		if (xx2 > defaultMaxX) defaultMaxX = xx2;
		if (xx3 < defaultMinX) defaultMinX = xx3;
		if (xx3 > defaultMaxX) defaultMaxX = xx3;
		if (yy1 < defaultMinY) defaultMinY = yy1;
		if (yy1 > defaultMaxY) defaultMaxY = yy1;
		if (yy2 < defaultMinY) defaultMinY = yy2;
		if (yy2 > defaultMaxY) defaultMaxY = yy2;
		if (yy3 < defaultMinY) defaultMinY = yy3;
		if (yy3 > defaultMaxY) defaultMaxY = yy3;
	    }
	} else {
	    for (Triangle triangle: triangleMap.values()) {
		double xx1, xx2, xx3, yy1, yy2, yy3;
		if (idata.eulerPhi == 0.0 && idata.eulerTheta == 0.0 
		    && idata.eulerPsi == 0.0) {
		    xx1 = triangle.x1; yy1 = triangle.y1;
		    xx2 = triangle.x2; yy2 = triangle.y2;
		    xx3 = triangle.x3; yy3 = triangle.y3;
		} else {
		    double[][] matrix = idata.matrix;
		    xx1 = matrix[0][0]*triangle.x1 + matrix[0][1]*triangle.y1 
			+ matrix[0][2]*triangle.z1;
		    yy1 = matrix[1][0]*triangle.x1 + matrix[1][1]*triangle.y1
			+ matrix[1][2]*triangle.z1;
		    xx2 = matrix[0][0]*triangle.x2 + matrix[0][1]*triangle.y2
			+ matrix[0][2]*triangle.z2;
		    yy2 = matrix[1][0]*triangle.x2 + matrix[1][1]*triangle.y2
			+ matrix[1][2]*triangle.z2;
		    xx3 = matrix[0][0]*triangle.x3 + matrix[0][1]*triangle.y3
			+ matrix[0][2]*triangle.z3;
		    yy3 = matrix[1][0]*triangle.x3 + matrix[1][1]*triangle.y3
			+ matrix[1][2]*triangle.z3;
		}
		if (xx1 < defaultMinX) defaultMinX = xx1;
		if (xx1 > defaultMaxX) defaultMaxX = xx1;
		if (xx2 < defaultMinX) defaultMinX = xx2;
		if (xx2 > defaultMaxX) defaultMaxX = xx2;
		if (xx3 < defaultMinX) defaultMinX = xx3;
		if (xx3 > defaultMaxX) defaultMaxX = xx3;
		if (yy1 < defaultMinY) defaultMinY = yy1;
		if (yy1 > defaultMaxY) defaultMaxY = yy1;
		if (yy2 < defaultMinY) defaultMinY = yy2;
		if (yy2 > defaultMaxY) defaultMaxY = yy2;
		if (yy3 < defaultMinY) defaultMinY = yy3;
		if (yy3 > defaultMaxY) defaultMaxY = yy3;
	    }

	    for (Triangle triangle: triangleSet) {
		double xx1, xx2, xx3, yy1, yy2, yy3;
		if (idata.eulerPhi == 0.0 && idata.eulerTheta == 0.0 
		    && idata.eulerPsi == 0.0) {
		    xx1 = triangle.x1; yy1 = triangle.y1;
		    xx2 = triangle.x2; yy2 = triangle.y2;
		    xx3 = triangle.x3; yy3 = triangle.y3;
		} else {
		    double[][] matrix = idata.matrix;
		    xx1 = matrix[0][0]*triangle.x1 + matrix[0][1]*triangle.y1 
			+ matrix[0][2]*triangle.z1;
		    yy1 = matrix[1][0]*triangle.x1 + matrix[1][1]*triangle.y1
			+ matrix[1][2]*triangle.z1;
		    xx2 = matrix[0][0]*triangle.x2 + matrix[0][1]*triangle.y2
			+ matrix[0][2]*triangle.z2;
		    yy2 = matrix[1][0]*triangle.x2 + matrix[1][1]*triangle.y2
			+ matrix[1][2]*triangle.z2;
		    xx3 = matrix[0][0]*triangle.x3 + matrix[0][1]*triangle.y3
			+ matrix[0][2]*triangle.z3;
		    yy3 = matrix[1][0]*triangle.x3 + matrix[1][1]*triangle.y3
			+ matrix[1][2]*triangle.z3;
		}
		if (xx1 < defaultMinX) defaultMinX = xx1;
		if (xx1 > defaultMaxX) defaultMaxX = xx1;
		if (xx2 < defaultMinX) defaultMinX = xx2;
		if (xx2 > defaultMaxX) defaultMaxX = xx2;
		if (xx3 < defaultMinX) defaultMinX = xx3;
		if (xx3 > defaultMaxX) defaultMaxX = xx3;
		if (yy1 < defaultMinY) defaultMinY = yy1;
		if (yy1 > defaultMaxY) defaultMaxY = yy1;
		if (yy2 < defaultMinY) defaultMinY = yy2;
		if (yy2 > defaultMaxY) defaultMaxY = yy2;
		if (yy3 < defaultMinY) defaultMinY = yy3;
		if (yy3 > defaultMaxY) defaultMaxY = yy3;
	    }
	}
	double scalex;
	double scaley;
	if (changeScale  || !idata.lastScaleXYSet) {
	    scalex = width / (defaultMaxX - defaultMinX);
	    scaley = height / (defaultMaxY - defaultMinY);
	    idata.lastScaleX = scalex;
	    idata.lastScaleY = scaley;
	    idata.lastScaleXYSet = true;
	} else {
	    scalex = idata.lastScaleX;
	    scaley = idata.lastScaleY;
	}
	double scale = (scalex > scaley)? scaley: scalex;

	scale *= magnification;
	idata.setScaleFactor(scale);

	result.scaleFactor = scale;
	result.magnification = magnification;

	idata.setOrigin(border, idata.getFloatHeight() - border);

	result.xorigin = idata.xorigin;
	result.yorigin = idata.yorigin;
	result.phi = idata.eulerPhi;
	result.theta = idata.eulerTheta;
	result.psi = idata.eulerPsi;
	result.xfract = xfract;
	result.yfract = yfract;

	result.rotXOrigin = idata.xoriginBR;
	result.rotYOrigin = idata.yoriginBR;
	result.rotZOrigin = idata.zoriginBR;
	result.move = idata.rotMoveOrigin;
	result.delta = idata.delta;
	result.lsPhi = idata.lsphi;
	result.lsTheta = idata.lstheta;
	result.colorFactor = idata.colorFactor;
	result.normalFactor = idata.normalFactor;

	double xt = defaultMinX;
	double yt = defaultMinY;
	double rw = width / scale;
	double rh = height / scale;
	double hrw = rw / 2.0;
	double hrh = rh / 2.0;
	double xfractmin = hrw / (defaultMaxX - defaultMinX);
	double yfractmin = hrh / (defaultMaxY - defaultMinY);
	boolean xfractminInRange = xfractmin > 0.0 && xfractmin < 1.0;
	boolean yfractminInRange = yfractmin > 0.0 && yfractmin < 1.0;
	result.xfractmin2 = rw / (defaultMaxX - defaultMinX);
	result.yfractmin2 = rh / (defaultMaxY - defaultMinY);

	if ((defaultMaxX - defaultMinX) < rw) {
	    xfract = 0.0;
	    // System.out.println("adjusting xt");
	    xt += ((defaultMaxX - defaultMinX) / 2.0) - hrw;
	    result.showsAllX = true;
	    result.xfractmin2 = 1.0;
	} else {
	    result.showsAllX = false;
	}
	result.minXtranslation = xt;
	/*
	System.out.println("xfractmin = " + xfractmin
			   +", yfractmin = " + yfractmin);
	*/
	if (xfract > 0.0 && xfract < 1.0) {
	    xfract = xfractmin + xfract * (1.0 - xfractmin - xfractmin);
	} else if (xfract == 1.0) {
	    xfract = 1.0 - xfractmin;
	}
	if (xfractminInRange) {
	    if (xfract > xfractmin && xfract < (1.0 - xfractmin)) {
		// System.out.println("old xt = " + xt);
		xt += xfract * (defaultMaxX - defaultMinX) - hrw;
		// System.out.println("new xt = " + xt);
	    } else if (xfract >= (1.0 - xfractmin)) {
		xfract = 1.0 - xfractmin;
		xt += xfract * (defaultMaxX - defaultMinX) - hrw;
	    }
	    result.maxXtranslation = result.minXtranslation
		+ (1.0 - xfractmin) * (defaultMaxX - defaultMinX) - hrw;
	} else {
	    result.maxXtranslation = result.minXtranslation;
	}

	if ((defaultMaxY - defaultMinY) < rh) {
	    yfract = 0.0;
	    // System.out.println("adjusting yt");
	    yt +=  ((defaultMaxY - defaultMinY) / 2.0) - hrh;
	    result.showsAllY = true;
	    result.yfractmin2 = 1.0;
	} else {
	    result.showsAllY = false;
	}
	result.minYtranslation = yt;
	if (yfract > 0.0 && yfract < 1.0) {
	    yfract = yfractmin + yfract * (1.0 - yfractmin - yfractmin);
	} else if (yfract == 1.0) {
	    yfract = 1.0 - yfractmin;
	}
	if (yfractminInRange) {
	    // System.out.println("adjusting for yfract");
	    if (yfract > yfractmin && yfract < (1.0 - yfractmin)) {
		yt += yfract * (defaultMaxY - defaultMinY) - hrh;
	    } else if (yfract >= (1.0 - yfractmin)) {
		yfract = 1.0 - yfractmin;
		yt += yfract * (defaultMaxY - defaultMinY) - hrh;
	    }
	    result.maxYtranslation = result.minYtranslation
		+(1.0 - yfractmin) * (defaultMaxY - defaultMinY) - hrh;
	} else {
	    result.maxYtranslation = result.minYtranslation;
	}
	idata.setTranslation(xt, yt);
	result.xtranslation = xt;
	result.ytranslation = yt;
	result.minX = defaultMinX - xt; result.maxX = defaultMaxX - xt;
	result.minY = defaultMinY - yt; result.maxY = defaultMaxY - yt;

	return result;
    }

    /**
     * Render a model.
     * The render list, a list of triangles sorted by stacking order,
     * will be cleared afterwards.
     * @param image the image which will contain the rendered model
     */
    public void render(Model3D.Image image) {
	render(image, false);
    }

    /**
     * Render a model, optionally preserving the render list so that
     * additional objects can be added.
     * @param image the image which will contain the rendered model
     * @param keep true if the render list should be preserved; false if not.
     */
    public void render(Model3D.Image image, boolean keep) {
	render(image, keep, 0.0, 0.0);
    }

    /**
     * Render a model, optionally preserving the render list so that
     * additional objects can be added and translating the coordinate
     * system for the model.
     * @param image the image which will contain the rendered model
     * @param keep true if the render list should be preserved; false if not.
     * @param tx the x coordinate of the new origin's location
     * @param ty the y coordinate of the new origin's location
     */
    public void render(Model3D.Image image, boolean keep,
		       double tx, double ty) {
	Graphics2D g2d = image.createGraphics();
	Model3D.ImageDataImpl idata = image.getImageData();
	try {
	    render(idata, g2d, keep, tx, ty);
	} finally {
	    g2d.dispose();
	}
    }

    /**
     * Render a model given a graphics context.
     * @param id the image data for rendered model
     * @param g2d the graphics context to use
     */
    public void render(Model3D.ImageData id, Graphics2D g2d)
    {
	render(id, g2d, false, 0.0, 0.0);
    }

    /**
     * Render a model given a graphics context and a render-list preservation
     * flag.
     * @param id the image data for rendered model
     * @param g2d the graphics context to use
     * @param keep true if the render list should be preserved; false if not.
     */
    public void render(Model3D.ImageData id, Graphics2D g2d, boolean keep)
    {
	render(id, g2d, keep, 0.0, 0.0);
    }

    /**
     * Render a model given a graphics context and translations.
     * @param id the image data for rendered model
     * @param g2d the graphics context to use
     * @param keep true if the render list should be preserved; false if not.
     * @param tx the x coordinate of the new origin's location
     * @param ty the y coordinate of the new origin's location
     */
    public void render(Model3D.ImageData id, Graphics2D g2d,
		       boolean keep, double tx, double ty)
    {
	ImageDataImpl idata = id.getImageData();
	AffineTransform at = g2d.getTransform();
	boolean reset = idata.rlistInvalid;
	try {
	    if (tx != 0.0 || ty != 0.0) {
		tx = tx * idata.scaleFactor;
		ty = ty * idata.scaleFactor;
		g2d.translate(-tx, -ty);
	    }
	    if (reset) {
		idata.renderList.reset();
		if (cubics.size() > 0 || cubicVertices.size() > 0) {
		    Iterator<Triangle> it = tessellate();
		    while (it.hasNext()) {
			Triangle triangle = it.next();
			doRenderIteration(idata, triangle);
		    }
		} else {
		    for (Triangle triangle: triangleMap.values()) {
			doRenderIteration(idata, triangle);
		    }
		    for (Triangle triangle: triangleSet) {
			doRenderIteration(idata, triangle);
		    }
		}
		for (LineSegment segment: lineSegmentMap.values()) {
		    doLineSegmentIteration(idata, segment);
		}
		for (LineSegment segment: lineSegmentSet) {
		    doLineSegmentIteration(idata, segment);
		}
		idata.rlistInvalid = false;
	    }
	    idata.renderList.render(g2d, idata.edgeColor, idata.colorFactor,
				    idata.normalFactor);
	} finally {
	    g2d.setTransform(at);
	    if (!keep) {
		idata.renderList.reset();
		idata.rlistInvalid = true;
	    }
	}
    }

    private Triangle createSurrogateTriangleC(Surface3D ourCubics,
					      int entryNumber, int edgeNumber,
					      double x1, double y1, double z1,
					      double x2, double y2, double z2,
					      double[] ccoords) {
	int type = ourCubics.getSegment(entryNumber, ccoords);
	Color color = ourCubics.getSegmentColor(entryNumber);
	Object tag = ourCubics.getSegmentTag(entryNumber);
	double u = 0.0, v = 0.0;
	switch (edgeNumber) {
	case 0:
	    u = 0.0; v = 0.0;
	    break;
	case 1:
	    u = 1.0; v = 0.0;
	    break;
	case 2:
	    u = 1.0; v = 1.0;
	    break;
	case 3:
	    u = 0.0; v = 1.0;
	    break;
	}
	double[] utangent = new double[3];
	double[] vtangent = new double[3];
	SurfaceOps.uTangent(utangent, type, ccoords, u, v);
	SurfaceOps.vTangent(vtangent, type, ccoords, u, v);
	double[] cprod = VectorOps.crossProduct(utangent, vtangent);
	return new Triangle(x1, y1, z1, x2, y2, z2,
			    cprod[0], cprod[1], cprod[2],
			    ourCubics,
			    entryNumber, edgeNumber, color, tag);
    }

    private Triangle createSurrogateTriangleV(Surface3D ourCubics,
					      int entryNumber, int edgeNumber,
					      double x1, double y1, double z1,
					      double x2, double y2, double z2,
					      double[] ccoords) {
	int type = ourCubics.getSegment(entryNumber, ccoords);
	Color color = ourCubics.getSegmentColor(entryNumber);
	Object tag = ourCubics.getSegmentTag(entryNumber);
	double u = 0.0, v = 0.0;
	switch (edgeNumber) {
	case 0:
	    u = 0.0; v = 0.0;
	    break;
	case 1:
	    u = 1.0; v = 0.0;
	    break;
	case 2:
	    u = 0.0; v = 1.0;
	    break;
	}
	double[] utangent = new double[3];
	double[] vtangent = new double[3];
	if (edgeNumber != 2) {
	    SurfaceOps.uTangent(utangent, type, ccoords, u, v);
	    SurfaceOps.vTangent(vtangent, type, ccoords, u, v);
	} else {
	    utangent[0] = ccoords[0] - ccoords[12];
	    utangent[1] = ccoords[1] - ccoords[13];
	    utangent[2] = ccoords[2] - ccoords[14];
	    vtangent[0] = ccoords[9] - ccoords[12];
	    vtangent[1] = ccoords[10] - ccoords[13];
	    vtangent[2] = ccoords[11] - ccoords[14];
	}
	double[] cprod = VectorOps.crossProduct(utangent, vtangent);
	return new Triangle(x1, y1, z1, x2, y2, z2,
			    cprod[0], cprod[1], cprod[2],
			    ourCubics,
			    entryNumber, edgeNumber, color, tag);
    }

    private void verifyManifoldIterationV(Surface3D ourCubics,
					  double x1, double y1, double z1,
					  double x2, double y2, double z2,
					  int entryNumber, int edgeNumber,
					  double[] ccoords, Edges edges)
    {
	Object tag = ourCubics.getSegmentTag(entryNumber);
	Triangle triangle = createSurrogateTriangleV(ourCubics,
						     entryNumber, edgeNumber,
						     x1, y1, z1,
						     x2, y2, z2,
						     ccoords);
	Edge edge = new Edge(x1,y1,z1, x2,y2,z2, tag, triangle);
	edges.add(edge);
    }


    private void verifyManifoldIterationC(Surface3D ourCubics,
					  double x1, double y1, double z1,
					  double x2, double y2, double z2,
					  int entryNumber, int edgeNumber,
					  double[] ccoords, Edges edges)
    {
	Object tag = ourCubics.getSegmentTag(entryNumber);
	Triangle triangle = createSurrogateTriangleC(ourCubics,
						     entryNumber, edgeNumber,
						     x1, y1, z1,
						     x2, y2, z2,
						     ccoords);
	Edge edge = new Edge(x1,y1,z1, x2,y2,z2, tag, triangle);
	edges.add(edge);
    }


    private void verifyManifoldIterationT(Triangle triangle, Edges edges) {
	double x1 = triangle.x1;
	double y1 = triangle.y1;
	double z1 = triangle.z1;

	double x2 = triangle.x2;
	double y2 = triangle.y2;
	double z2 = triangle.z2;

	double x3 = triangle.x3;
	double y3 = triangle.y3;
	double z3 = triangle.z3;
	Object tag = triangle.tag;

	edges.add(new Edge(x1,y1,z1, x2,y2,z2, tag, triangle));
	edges.add(new Edge(x2,y2,z2, x3,y3,z3, tag, triangle));
	edges.add(new Edge(x3,y3,z3, x1,y1,z1, tag, triangle));
    }

    /**
     * Test that the model consists of a closed 2D manifold.
     * The orientation of a triangle is determined by using a "right hand rule"
     * when traversing vertices. For a manifold to be two dimensional, each
     * edge must be traversed no more than twice and to be closed, each edge
     * must be traversed exactly twice, once in each direction. Thus, each
     * edge must be shared by exactly two triangles. A less stringent test
     * is useful in 3D printing: to cubes that touch on a edge but share
     * a common base are printable, but the surface is not a manifold
     * (although it would be if the cubes were offset from each other by
     * a tiny amount). The constructor {@link #Model3D(boolean)} allows
     * one to specify a "strict" mode, where true requires that the surface
     * is manifold and false allows multiple surfaces to touch at an edge.
     * <p>
     * In addition, the rules for a valid STL file preclude partially
     * overlapping edges.
     * @return a list of edges for which the test failed; null if it succeeded
     */
    public List<Edge> verifyClosed2DManifold() {
	return verifyClosed2DManifold(strict);
    }

    // internal function: isClosedManifold() ignores the strict setting.
    List<Edge> verifyClosed2DManifold(boolean strict)
    {
	Edges edges = new Edges();

	Surface3D ourCubics = (cubicVertices.size() > 0)?
	    new Surface3D.Double(cubics, cubicVertices): cubics;
	ourCubics.computeBoundary(null, true);
	Path3D boundary = ourCubics.getBoundary();
	if (boundary != null) {
	    int[] segmentIndices = ourCubics.getBoundarySegmentIndices();
	    int[] edgeNumbers = ourCubics.getBoundaryEdgeNumbers();
	    // Object[] tags = ourCubics.getBoundaryTags();
	    double[] coords = new double[12];
	    double[] pcoords = null;
	    double[] ccoords = new double[48];
	    if (!boundary.isEmpty()) {
		PathIterator3D pit = boundary.getPathIterator(null);
		int index = 0;
		double x = 0.0, y = 0.0, z = 0.0;
		double x0 = 0.0, y0 = 0.0, z0 = 0.0;
		while (!pit.isDone()) {
		    int type = pit.currentSegment(coords);
		    int entryNumber = -1;
		    int edgeNumber = -1;
		    Object tag = null;
		    switch(type) {
		    case PathIterator3D.SEG_MOVETO:
			x = coords[0];
			y = coords[1];
			z = coords[2];
			x0 = x;
			y0 = y;
			z0 = z;
			break;
		    case PathIterator3D.SEG_LINETO:
			entryNumber = segmentIndices[index];
			edgeNumber = edgeNumbers[index];
			index++;
			// tag = cubics.getSegmentTag(segmentIndices[index++]);
			// Need to create an edge because a cubic vertex
			// has two straight-line edges.
			verifyManifoldIterationV(ourCubics,
						 x, y, z,
						 coords[0],
						 coords[1],
						 coords[2],
						 entryNumber, edgeNumber,
						 ccoords, edges);
			x = coords[0];
			y = coords[1];
			z = coords[2];
			break;
		    case PathIterator3D.SEG_CUBICTO:
			entryNumber = segmentIndices[index];
			edgeNumber = edgeNumbers[index];
			index++;
			// tag = cubics.getSegmentTag(segmentIndices[index++]);;
			pcoords = Path3D.setupCubic(x, y, z,
					     coords[6], coords[7], coords[8]);
			if ((float)coords[0] == (float)pcoords[3]
			    && (float)coords[1] == (float)pcoords[4]
			    && (float)coords[2] == (float)pcoords[5]
			    && (float)coords[3] == (float)pcoords[6]
			    && (float)coords[4] == (float)pcoords[7]
			    && (float)coords[5] == (float)pcoords[8]) {
			    verifyManifoldIterationC(ourCubics,
						     x, y, z,
						     coords[6],
						     coords[7],
						     coords[8],
						     entryNumber, edgeNumber,
						     ccoords, edges);
			} else {
			    // error condition: not a straight edge so
			    // can't match any triangle, but also on a boundary
			    // so it doesn't match a cubic.
			    LinkedList<Model3D.Edge> result =
				new LinkedList<>();
			    Edge e =
				new Edge(x, y, z,
					 coords[6], coords[7], coords[8],
					 ourCubics.getSegmentTag(entryNumber));
			    result.add(e);
			    return result;
			}
			x = coords[6];
			y = coords[7];
			z = coords[8];
			break;
		    case PathIterator3D.SEG_CLOSE:
			x  = x0; y = y0; z = z0;
			break;
		    default:
			break;
		    }
		    pit.next();
		}
	    }
	}

	for (Triangle triangle: triangleMap.values()) {
	    verifyManifoldIterationT(triangle, edges);
	}
	for (Triangle triangle: triangleSet) {
	    verifyManifoldIterationT(triangle, edges);
	}
	return edges.verify(strict);
    }


    /**
     * Test that the model's 2D manifold is embedded in a Euclidean 3
     * dimensional space.
     * The model is assumed to consist only of triangles: cubic patches,
     * cubic vertices, and cubic triangles are ignored.  To test these,
     * first create a tessellated model.
     * Given that the manifold is represented by a set of triangles,
     * the requirement is that triangles do not intersect.
     * @return a list of triangles, each pair of which intersect each other.
     */
    public List<Triangle> verifyEmbedded2DManifold() {
	Triangles triangles = new Triangles();
	triangles.setLimit(limit);
	return triangles.verify(this);
    }

    /**
     * Test that the model's 2D manifold is embedded in a Euclidean 3
     * dimensional space, setting a test limit.
     * The model is assumed to consist only of triangles: cubic patches,
     * cubic vertices, and cubic triangles are ignored.  To test these,
     * first create a tessellated model.
     * Given that the manifold is represented by a set of triangles,
     * the requirement is that triangles do not intersect.
     * <P>
     * The limit (the default is Math.ulp(1F)) is a bound on how far
     * from zero values may be and still be considered to be zero. The
     * limit is intended to account for round-off errors.
     * The method {@link #verifyEmbedded2DManifold()} uses the default
     * limit.
     * @param limit the limit (a non-negative number)
     * @return a list of triangles, each pair of which intersect each other.
     */
    public List<Triangle> verifyEmbedded2DManifold(double limit) {
	Triangles triangles = new Triangles();
	triangles.setLimit(limit);
	return triangles.verify(this);
    }

    ManifoldComponents manifoldComponents = null;

    /**
     * Get the number of manifold components for a model.
     * @return the number of manifold components for the current model
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     */
    public int numberOfComponents() throws ManifoldException {
	if (manifoldComponents == null) {
	    manifoldComponents = new ManifoldComponents(this, strict);
	}
	return manifoldComponents.size();
    }

    /**
     * Get a manifold component.
     * The components are referenced by an index, specified as an
     * integer in the range [0,n),  where n is the number of manifold
     * components.  If n is zero, no index is valid.
     * @param index the component's index
     * @return a model containing the specified component
     * @see #numberOfComponents()
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     */
    public Model3D getComponent(int index) throws ManifoldException {
	if (manifoldComponents == null) {
	    manifoldComponents = new ManifoldComponents(this, strict);
	}
	return manifoldComponents.getModel(index);
    }

    /**
     * Determine if some manifold component represents a vacant space.
     * @return true if the model has no inward-facing surfaces; false if it
     *         does
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     */
    public boolean notHollow() throws ManifoldException {
	try {
	    return notHollow(null);
	} catch (IOException e) {throw new UnexpectedExceptionError(e);}
    }

    /**
     * Determine if some manifold component represents a vacant space and
     * optionally store a record of the results.
     * @param out an Appendable for recording details; null for no output
     * @return true if the model has no inward-facing surfaces; false if it
     *         does
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     * @exception IOException an error occurred during writing
     */
    public boolean notHollow(Appendable out)
	throws ManifoldException, IOException
    {
	try {
	    if (manifoldComponents == null) {
		manifoldComponents = new ManifoldComponents(this, strict);
	    }
	} catch (ManifoldException e) {
	    if (out != null) {
		out.append(e.getMessage() + "\n");
		if (stackTraceMode) {
		    P3d.printTriangleErrors(out, e.getErrorTriangles());
		}
	    }
	    throw e;
	}
	int cnt = 0;
	for (int i = 0; i < manifoldComponents.size(); i++) {
	    if (manifoldComponents.getModel(i).volume() < 0.0) {
		if (out != null) {
		    try {
			String msg = (errorMsg("hollowComponent", i));
			out.append(msg + "\n");
		    } catch (IOException e) {}
		    cnt++;
		} else {
		    return false;
		}
	    }
	}
	return (cnt == 0);
    }

    /**
     * Determine if a component of a 3D model is hollow.
     * The precondition for using this method is that the
     * model is a closed 2D manifold embedded in a Euclidean
     * three-dimensional space.
     * @param index the index of a component.
     * @return true if the component is hollow (i.e., the normal vectors
     *         of its triangles point inwards, not outwards); false
     *         otherwise
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     */
    public boolean isComponentHollow(int index) throws ManifoldException {
	return ((Model3D)getComponent(index)).volume() < 0.0;
    }


    /**
     * Test that the model compoents are properly nested.
     * @return true if the components are properly nested; false otherwise
     */
    public boolean verifyNesting() {
	return verifyNesting(null);
    }

    /**
     * Test that the model compoents are properly nested, and record errors.
     * @param out the output appendable
     * @return true if the components are properly nested; false otherwise
     * @exception ManifoldException the model is ill-formed and its
     *            components (the components of a manifold) cannot be
     *            computed.
     */
    public boolean verifyNesting(Appendable out) throws ManifoldException{
	boolean needTesselation =
	    (cubics.size() > 0) || (cubicVertices.size() > 0);
	ManifoldComponents mc;
	if (needTesselation) {
	    mc = new ManifoldComponents(this, strict, true);
	} else {
	    if (manifoldComponents == null) {
		manifoldComponents = new ManifoldComponents(this, strict);
	    }
	    mc = manifoldComponents;
	}
	return mc.verifyNesting(out);
    }


    /**
     * Test if a model is printable on a 3D printer..
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.  This method assumes that hollow objects
     * are not allowed for 3D printing.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @return true if the model can be printed; false otherwise
     */
    public boolean printable() {
	return !notPrintable();
    }

    /**
     * Test if a model is printable on a 3D printer, given an Appendable
     * for error-message output.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.  This method assumes that hollow objects
     * are not allowed for 3D printing.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @param out the output on which to print error messages; null if none
     *        is to be used
     * @return true if the model cannot be printed; false otherwise
     * @exception IOException an IO exception occurred when printing error
     *            messages
     */
    public boolean printable(Appendable out) throws IOException {
	return !notPrintable(out);
    }

    /**
     * Test if a model is printable on a 3D printer, specifying whether
     * hollow objects are allowed.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @param hollow true if the printer allows hollow objects; false if not.
     * @return true if the model cannot be printed; false otherwise
     */
    public boolean printable(boolean hollow)
    {
	return !notPrintable(hollow);
    }

    /**
     * Test if a model is printable on a 3D printer, given an Appendable for
     * error-message output and specifying whether hollow objects are allowed.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @param hollow true if the printer allows hollow objects; false if not.
     * @param out the output on which to print error messages; null if none
     *        is to be used
     * @return true if the model cannot be printed; false otherwise
     * @exception IOException an IO exception occurred when printing error
     *            messages
     */
    public boolean printable(boolean hollow, Appendable out)
	throws IOException
    {
	return !notPrintable(hollow, out);
    }


    /**
     * Test if a model is not printable on a 3D printer..
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.  This method assumes that hollow objects
     * are not allowed for 3D printing.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @return true if the model cannot be printed; false otherwise
     */
    public boolean notPrintable() {
	try {
	    return notPrintable(false, null);
	} catch (IOException e) {
	    // With no output stream, no IOException will be generated,
	    // but we have to catch the exception because the method we
	    // could throw one if its second argument was not null.
	    // The Java compiler complains if we don't return something,
	    // so we throw an error - if the error is generated, something
	    // is seriously wrong.
	    throw new UnexpectedExceptionError(e);
	}
    }

    /**
     * Test if a model is not printable on a 3D printer, given an Appendable
     * for error-message output.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.  This method assumes that hollow objects
     * are not allowed for 3D printing.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @param out the output on which to print error messages; null if none
     *        is to be used
     * @return true if the model cannot be printed; false otherwise
     * @exception IOException an IO exception occurred when printing error
     *            messages
     */
    public boolean notPrintable(Appendable out) throws IOException {
	return notPrintable(false, out);
    }

    /**
     * Test if a model is not printable on a 3D printer, specifying
     *  whether hollow objects are allowed.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * @param hollow true if the printer allows hollow objects; false if not.
     * @return true if the model cannot be printed; false otherwise
     */
    public boolean notPrintable(boolean hollow)
    {
	try {
	    return notPrintable(hollow, null);
	} catch (IOException eio) {
	    // the output stream is null, which means there is
	    // no IO operations to perform.
	    throw new UnexpectedExceptionError(eio);
	}
    }


    /**
     * Test if a model is not printable on a 3D printer, given an Appendable for
     * error-message output and specifying whether hollow objects are allowed.
     * 3D printing technologies such as laser sintering do not allow one
     * to print hollow objects because material would be trapped inside
     * the object being printed.
     * <P>
     * Note: this test does not include physical constraints on dimensions.
     * Rather it verifies that the object being printed has a surface that
     * is a closed manifold, the it does not intersect itself, and that
     * components are ordered correctly so that what should be the inside of
     * an object is not the outside of that object. Specific printers will
     * usually have constraints on a model such as minimal thicknesses for
     * "walls" and "wires", and minimum sizes for holes for letting material
     * be removed from cavities.
     * The test for self intersection apply only to a model's triangles.
     * If a model contains a cubic patch, cubic vertices, or cubic triangles,
     * a tessellated model should be created first.
     * @param hollow true if the printer allows hollow objects; false if not.
     * @param out the output on which to print error messages; null if none
     *        is to be used
     * @return true if the model cannot be printed; false otherwise
     * @exception IOException an IO exception occurred when printing error
     *            messages
     */
    public boolean notPrintable(boolean hollow, Appendable out)
	throws IOException
    {
	if (!cubics.isWellFormed(out, true)) {
	    if (out != null) {
		String msg = (errorMsg("notWellFormed"));
		out.append(msg + "\n");
	    }
	}
	List<Edge> edges = verifyClosed2DManifold();
	if (edges != null) {
	    if (out != null) {
		String msg = (errorMsg("notClosedManifold"));
		out.append(msg + "\n");
		P3d.printEdgeErrors(out, edges);
	    }
	    return true;
	}
	List<Triangle> triangles = verifyEmbedded2DManifold();
	if (triangles != null) {
	    if (out != null) {
		String msg = (errorMsg("notEmbedded"));
		out.append(msg + "\n");
		P3d.printTriangleErrors(out, triangles);
	    }
	    return true;
	}
	// A ManifoldException does not have to be handled because
	// the preceding tests ensure that the following statement
	// will not be executed if the model is ill-formed.
	boolean result = false;
	if (hollow == false) {
	    if (notHollow(out) == false) {
		return true;
	    }
	}
	return !verifyNesting(out);
    }

    Double cachedArea = null;

    /**
     * Compute the surface area of a 3D model
     * The model must be a closed manifold embedded in a two-dimensional
     * space. The units are those used by graph-coordinate space.
     * <P>
     * Implementation note: the result of this computation will be
     * cached so that subsequent calls to this method will simply use
     * the cached value (which is cleared if a triangle is added to the
     * model).
     * @return the surface area
     */
    public double area() {
	if (cachedArea != null) {
	    return cachedArea.doubleValue();
	}
	CollectionScanner<Triangle> cs = new CollectionScanner<>();
	cs.add(triangleMap.values());
	cs.add(triangleSet);
	Adder adder = new Adder.Kahan();
	// double xarea = 0.0;
	for (Triangle triangle: cs) {
	    adder.add(triangle.area2());
	    // xarea += triangle.area2();
	}
	double xarea = adder.getSum();
	xarea /= 2.0;
	adder.reset();
	Surface3D.addAreaToAdder(adder, cubics.getSurfaceIterator(null));
	Surface3D.addAreaToAdder(adder, cubicVertices.getSurfaceIterator(null));
	xarea += adder.getSum();
	cachedArea = xarea;
	return xarea;
    }

    Double cachedVolume = null;

    /**
     * Compute the volume of a 3D model
     * The model must be a closed manifold embedded in a two-dimensional
     * space. The units are those used by graph-coordinate space.  The
     * normal vector is assumed to be in the direction implied by the
     * right-hand rule as each triangle's vertices are traversed in the
     * order defined. If the normal vectors for the triangles in a manifold
     * point outwards, the volume returned will be positive; otherwise it may
     * be negative.  One can use inward pointing normal vectors to represent
     * a cavity, but such models typically cannot be 3D printed as material will
     * be trapped inside. A cube with x, y, and z varying from 0.0 to 1.0 will
     * have a volume of 1.0.
     * <P>
     * The algorithm used to compute the volume has a complexity linear in
     * the number of triangles: the algorithm uses Gauss' theorem to turn
     * the volume computation into a surface integral, and for each triangle,
     * the integral can be evaluated in closed form.
     * <P>
     * Implementation note: the result of this computation will be
     * cached so that subsequent calls to this method will simply use
     * the cached value (which is cleared if a triangle is added to the
     * model).
     * @return the volume
     */
    public double volume() {
	if (cachedVolume != null) return cachedVolume.doubleValue();
	int n = size();
	if (n == 0) return 0.0;
	CollectionScanner<Triangle> cs = new CollectionScanner<>();
	cs.add(triangleMap.values());
	cs.add(triangleSet);
	// double vol = 0.0;
	// double vol1 = 0.0;
	// double vol2 = 0.0;
	// heuristic choice - we want as many of the terms as
	// possible to have the same sign.
	computeBoundingBoxIfNeeded();
	double xref = (minx + maxx)/2.0;
	double yref = (miny + maxy)/2.0;
	double zref = (minz + maxz)/2.0;
	// double[] array = new double[n];
	// int index = 0;
	Adder adder = new Adder.Kahan();
	for (Triangle triangle: cs) {
	    double ux = triangle.x2 - triangle.x1;
	    double uy = triangle.y2 - triangle.y1;
	    double uz = triangle.z2 - triangle.z1;
	    double vx = triangle.x3 - triangle.x1;
	    double vy = triangle.y3 - triangle.y1;
	    double vz = triangle.z3 - triangle.z1;

	    double cpx = uy*vz - vy*uz;
	    double cpy = (vx*uz - ux*vz);
	    double cpz = (ux*vy - vx*uy);
	    // the length of the cross product is twice the area of the
	    // triangle, so the result is twice the area of the triangle
	    // multiplied by the dot product of a vector from the
	    // reference point to the (x1, y1, z1) vertex of the triangle.
	    // If we use the normal vector instead of the cross product
	    // and multiply by twice the area, we end up computing the
	    // cross product anyway but with an unnecessary square root
	    // and dot product to get its norm.
	    adder.add((triangle.x1 - xref)*cpx
		      + (triangle.y1 - yref)*cpy + (triangle.z1 - zref)*cpz);
	}
	double vol = adder.getSum();
	// Divide by 6 rather than 3 because the cross products were vectors
	// whose length is twice the area of their triangles.
	vol /= 6.0;
	adder.reset();
	SurfaceOps.addVolumeToAdder(adder,
				    cubics.getSurfaceIterator(null),
				    new Point3D.Double(xref, yref, zref));
	SurfaceOps.addVolumeToAdder(adder,
				    cubicVertices.getSurfaceIterator(null),
				    new Point3D.Double(xref, yref, zref));
	vol += adder.getSum()/3.0;
	cachedVolume = vol;
	return vol;
    }

    boolean needSTLBase = true;
    boolean useSTLBase = true;

    double xSTLBase = 0.0;
    double ySTLBase = 0.0;
    double zSTLBase = 0.0;

    /**
     * Set whether or not an STL base is used.
     * An STL base provides a translation that sets the minimum
     * x, y, and z coordinates to some value,  always a positive
     * one (STL files generally contain only positive vertex positions).
     * The default is <code>true</code>.  If set to <code>false</code>
     * the STL-base processing is ignored, and the coordinates provided
     * by the model (some of these coordinates may be negative or zero)
     * are used instead.
     * @value true if an STL base is used; false otherwise
     */
    public void setSTLBase(boolean value) {
	useSTLBase = value;
    }

    /**
     * Set the value of STL Base.
     * The STL Base parameters will be set so that the minimum values
     * of x, y and z for objects in a model are given by offset.
     * The default is 1.0.
     * @param offset an offset determining the STL base.
     */
    public void setSTLBase(double offset) {
	if (offset <= 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("setSTLBaseErr", offset));
	} else {
	    computeBoundingBoxIfNeeded();
	    useSTLBase = true;
	    needSTLBase = false;
	    xSTLBase = offset - minx;
	    ySTLBase = offset - miny;
	    zSTLBase = offset - minz;
	}
    }

    /**
     * Set the value of STL Base to a default.
     * Equivalent to setSTLBase(1.0).
     */
    public void setSTLBase() {
	setSTLBase(1.0);
    }

    void setSTLBaseIfNeeded() {
	if (useSTLBase && needSTLBase) {
	    setSTLBase();
	}
    }

    double unitScale = 0.001;
    /**
     * Set the unit scale factor for X3D files.
     * The default is 0.001 so that a length of 1.0 is 1 mm.
     * @param value the length of a unit length in meters
     */
    public void setUnitScaleX3D(double value) {
	if (value <= 0.0)
	    throw new IllegalArgumentException
		(errorMsg("setUnitScaleX3D", value));
	this.unitScale = unitScale;
    }

    /**
     * Get the unit scale factor for X3D files.
     * The value is the length in meters of a unit-length in the model.
     * @return the scale factor
     */
    public double getUnitScaleX3D() {return unitScale;}

    private void writeTriangleSTL(WritableByteChannel c, ByteBuffer buffer,
				  Triangle triangle)
	throws IOException
    {
	buffer.clear(); buffer.putFloat((float)(triangle.nx));
	buffer.putFloat((float)(triangle.ny));
	buffer.putFloat((float)(triangle.nz));
	if (useSTLBase) {
	    buffer.putFloat((float)(triangle.x1 + xSTLBase));
	    buffer.putFloat((float)(triangle.y1 + ySTLBase));
	    buffer.putFloat((float)(triangle.z1 + zSTLBase));
	    buffer.putFloat((float)(triangle.x2 + xSTLBase));
	    buffer.putFloat((float)(triangle.y2 + ySTLBase));
	    buffer.putFloat((float)(triangle.z2 + zSTLBase));
	    buffer.putFloat((float)(triangle.x3 + xSTLBase));
	    buffer.putFloat((float)(triangle.y3 + ySTLBase));
	    buffer.putFloat((float)(triangle.z3 + zSTLBase));
	} else {
	    buffer.putFloat((float)(triangle.x1));
	    buffer.putFloat((float)(triangle.y1));
	    buffer.putFloat((float)(triangle.z1));
	    buffer.putFloat((float)(triangle.x2));
	    buffer.putFloat((float)(triangle.y2));
	    buffer.putFloat((float)(triangle.z2));
	    buffer.putFloat((float)(triangle.x3));
	    buffer.putFloat((float)(triangle.y3));
	    buffer.putFloat((float)(triangle.z3));
	}
	buffer.putShort((short)0);
	buffer.flip();
	c.write(buffer);
    }

    /**
     * Create an X3D file from the model, given a file name.
     * The first three parameters are strings used in meta data.
     * The file created will use the X3D binary format if the JRE supports
     * the fast infoset format; otherwise the format will default to the
     * XML format.  To control the format explicitly,
     * use {@link #writeX3D(String,String,String,OutputStream,boolean)}.
     * <P>
     * The XML format before a infoset is created does not have a
     * DOCTYPE declaration: tests indicated that the corresponding DTD
     * was not being processed appropriately.
     * The X3D file will specify an "Interchange" profile.
     * If the file-name extension is "x3dz" or "X3DZ", the file will be
     * compressed.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param fname the name of the file
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 String fname)
	throws IOException
    {
	OutputStream os = new FileOutputStream(new File(fname));
	boolean compress = (fname.endsWith(".x3dz") || fname.endsWith(".X3DZ"));
	writeX3D(title, description, creator, os, compress);
	os.close();
    }

    /**
     * Create an X3D file from the model, given a file name and
     * specifying a profile.
     * The first three parameters are strings used in meta data.
     * use {@link #writeX3D(String,String,String,OutputStream,boolean)}.
     * If the file-name extension is "x3dz" or "X3DZ", the file will be
     * compressed.
     * <P>
     * The XML format before a infoset is created does not have a
     * DOCTYPE declaration: tests indicated that the corresponding DTD
     * was not being processed appropriately.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param full true if the Full X3D profile should be used; false for
     *        the Interchange profile
     * @param fname the name of the file
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 boolean full, String fname)
	throws IOException
    {
	OutputStream os = new FileOutputStream(new File(fname));
	boolean compress = (fname.endsWith(".x3dz") || fname.endsWith(".X3DZ"));
	writeX3D(title, description, creator, full, os, compress);
	os.close();
    }


    /**
     * Create an STL file from the model, given a file name.
    * The file created will use the X3D binary format if the JRE supports
     * the fast infoset format; otherwise the format will default to the
     * XML format.  To control the format explicitly,
     * use {@link #writeX3D(String,String,String,OutputStream,boolean)}.
     * @param id the STL file's ID string (limited to 80 7-bit ASCII characters)
     * @param fname the name of the file
     * @exception IOException an error occurred when opening or writing the file
     */
    public void writeSTL(String id, String fname) throws IOException {
	writeSTL(id, new File(fname));
    }

    /**
     * Create an STL file from the model, given a file.
     * The file created will use the X3D binary format if the JRE supports
     * the fast infoset format; otherwise the format will default to the
     * XML format.  To control the format explicitly,
     * use {@link #writeX3D(String,String,String,OutputStream,boolean)}.
     * @param id the STL file's ID string (limited to 80 7-bit ASCII characters)
     * @param f the output file
     * @exception IOException an error occurred when opening or writing the file
     */
    public void writeSTL(String id, File f) throws IOException {
	FileChannel fc = new FileOutputStream(f).getChannel();
	writeSTL(id, fc);
	fc.close();
    }

    /**
     * Create an X3D file from the model, given a File.
     * The first three parameters are strings used in meta data.
     * The X3D file will specify a "Interchange" profile.
     * If the file-name extension is "x3dz" or "X3DZ", the file will be
     * compressed.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param f the file
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 File f)
	throws IOException
    {
	OutputStream os = new FileOutputStream(f);
	String fname = f.getName();
	boolean compress = (fname.endsWith(".x3dz") || fname.endsWith(".X3DZ"));
	writeX3D(title, description, creator, os, compress);
	os.close();
    }

    /**
     * Create an X3D file from the model, given a File and specifying a
     * profile.
     * The first three parameters are strings used in meta data.
     * If the file-name extension is "x3dz" or "X3DZ", the file will be
     * compressed.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param full true if the Full X3D profile should be used; false for
     *        the Interchange profile
     * @param creator the file's author; null for a default
     * @param f the file
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 boolean full, File f)
	throws IOException
    {
	OutputStream os = new FileOutputStream(f);
	String fname = f.getName();
	boolean compress = (fname.endsWith(".x3dz") || fname.endsWith(".X3DZ"));
	writeX3D(title, description, creator, full, os, compress);
	os.close();
    }

    /**
     * Create an STL file from the model, given an output stream.
     * @param id the STL file's ID string (limited to 80 7-bit ASCII characters)
     * @param out the output stream
     * @exception IOException an error occurred when writing to the stream
     */
    public void writeSTL(String id, OutputStream out) throws IOException {
	WritableByteChannel c = Channels.newChannel(out);
	writeSTL(id, c);
	// We don't close the channel as that will close the output stream
	// as well: newChannel, at least in some implementations, returns
	// the output stream's channel and calling newChannel twice does not
	// result in two different channels.
    }

    private static final String PACKAGE = "org/bzdev/p3d";
    private static final String DEFAULT_TITLE = "3D Model";
    private static final String DEFAULT_DESCRIPTION = "3D Model";
    private static final String DEFAULT_CREATOR = "Java Application";

    private String x3dAppearance(Color c) {
	float transparency = (float)((255 - c.getAlpha())/255.0);
	return "<Appearance><Material diffuseColor=\""
	    + (float)(c.getRed()/255.0)
	    + " " + (float)(c.getGreen()/255.0)
	    + " " + (float)(c.getBlue()/255.0)
	    + "\" transparency = \"" + transparency
	    + "\"/></Appearance>";
    }

    private static String printDateTime(Calendar cal) {
	// return String.format("%tF %tT-00:00", cal, cal);
	return String.format("%te %tB %tY", cal, cal, cal);
    }

    /**
     * Create an X3D file from the model, given an output stream.
     * The first three parameters are strings used in meta data.
     * The X3D file will specify an "Interchange" profile.
     * The file will not be compressed.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param os the output stream
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 OutputStream os)
	throws IOException
    {
	writeX3D(title, description, creator, false, os, false);
    }

    /**
     * Create an X3D file from the model, specifying a profile and
     * given an output stream.
     * The first three parameters are strings used in meta data.
     * The file will not be compressed.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param full true if the Full X3D profile should be used; false for
     *        the Interchange profile
     * @param os the output stream
     * @exception IOException an IO error occurred
     */
    public void writeX3D(String title, String description, String creator,
			 boolean full,
			 OutputStream os)
	throws IOException
    {
	writeX3D(title, description, creator, full, os,
		 /*FISOutputStream.isSupported()*/ false);
    }

    /**
     * Create an X3D file in either binary or XML format from the model,
     * given an output stream.
     * The first three parameters are strings used in meta data.
     * The binary format is a fast infoset encoding of the XML file.
     * Currently it is not supported but may be in future releases.
     * The X3D file will specify an "Interchange" profile.
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param os the output stream
     * @param compress true if the file should be compressed; false otherwise
     * @exception IOException an IO error occurred
     * @exception UnsupportedOperationException binary encoding is not supported
     */
    public void writeX3D(String title, String description, String creator,
			 final OutputStream os, boolean compress)
	throws IOException
    {
	writeX3D(title, description, creator, false, os, compress);
    }

    /**
     * Create an X3D file in either binary or XML format from the model,
     * given a profile specification and an output stream.
     * The first three parameters are strings used in meta data.
     * <P>
     * If compressed, the output stream will be automatically closed
     * @param title the title of the file; null for a default
     * @param description a description of the file; null for a default
     * @param creator the file's author; null for a default
     * @param full true if the Full X3D profile should be used; false for
     *        the Interchange profile
     * @param os the output stream
     * @param compress true if the file should be compressed; false for
     *        XML alone
     * @exception IOException an IO error occurred
     * @exception UnsupportedOperationException binary encoding is not supported
     */
    public void writeX3D(String title, String description, String creator,
			 boolean full, OutputStream os, boolean compress)
	throws IOException
    {
	if (os == null) {
	    throw new NullPointerException(errorMsg("nullOutputStream"));
	}
	if (compress) {
	    os = new GZIPOutputStream(os);
	}

	final OutputStream out = os;

	TemplateProcessor.KeyMap map = new TemplateProcessor.KeyMap();
	map.put("PROFILE", (full? "Full": "Interchange"));
	map.put("TITLE", ((title == null)? DEFAULT_TITLE: title));
	map.put("DESCRIPTION", (description == null? DEFAULT_DESCRIPTION:
				WebEncoder.htmlEncode(description)));
	map.put("CREATOR", (creator == null? DEFAULT_CREATOR:
			    WebEncoder.htmlEncode(creator)));
	map.put("CREATION_DATE",
		/*javax.xml.bind.DatatypeConverter.*/printDateTime
		(Calendar.getInstance(TimeZone.getTimeZone("UTC"))));
	// System.out.println("CREATION_DATE = " + map.get("CREATION_DATE"));

	StackTraceElement[] starray = AccessController.doPrivileged
	    (new PrivilegedAction<StackTraceElement[]>() {
		public StackTraceElement[] run() {
		    return Thread.currentThread().getStackTrace();
		}
	    });
	map.put("APPNAME",starray[starray.length - 1].getClassName());

	long tnumber = MathOps.lPow(2, tlevel);
	map.put("tessellation", String.format(Locale.ROOT, "%d", tnumber));
	map.put("utessellation", String.format(Locale.ROOT, "%d", tnumber));
	map.put("vtessellation", String.format(Locale.ROOT, "%d", tnumber));

	TemplateProcessor.KeyMapIterable kmpi = !full? null:
	    new TemplateProcessor.KeyMapIterable() {
		SurfaceIterator sit = cubicVertices.getSurfaceIterator(null);
		SurfaceIterator sit2 = cubics.getSurfaceIterator(null);
		double[] coords1 = new double[48];
		double[] coords2 = new double[48];
		StringBuilder sb = new StringBuilder(1024);
		public Iterator<TemplateProcessor.KeyMap> iterator() {
		    return new Iterator<TemplateProcessor.KeyMap>() {
			public boolean hasNext() {
			    if (sit == sit2) {
				return !sit.isDone();
			    } else {
				if (sit.isDone()) {
				    sit = sit2;
				    return !sit.isDone();
				} else {
				    return false;
				}
			    }
			}
			public void remove() {
			    throw new UnsupportedOperationException();
			}
			public TemplateProcessor.KeyMap next() {
			    if (sit != sit2 && sit.isDone()) sit = sit2;
			    TemplateProcessor.KeyMap m =
				new TemplateProcessor.KeyMap();
			    int type = sit.currentSegment(coords1);
			    switch(type) {
			    case SurfaceIterator.CUBIC_VERTEX:
				// The V direction of a cubic vertex consists
				// of straight lines so we should not subdivide.
				m.put("vtessellation", "1");
			    case SurfaceIterator.CUBIC_TRIANGLE:
				if (type == SurfaceIterator.CUBIC_VERTEX) {
				    Surface3D
					.cubicVertexToPatch(coords1, 0, coords2,
							    0);
				} else {
				    Surface3D
					.triangleToPatch(coords1, 0, coords2,
							 0);
				}
				double[] tmp = coords1;
				coords1 = coords2;
				coords2 = tmp;
				// fall through - we now have a cubic patch
			    case SurfaceIterator.CUBIC_PATCH:
				for (int i = 0; i < 47; i++) {
				    sb.append(String.format(Locale.ROOT,
							    "%s", coords1[i]));
				    sb.append(" ");
				}
				sb.append(String.format(Locale.ROOT,
							"%s", coords1[47]));
				m.put("controlPoints", sb.toString());
				sb.delete(0, sb.length());
			    }
			    Color color = sit.currentColor();
			    if (color != null) {
				m.put("APPEARANCE",
				      x3dAppearance(color));
			    }
			    return m;
			}
		    };
		}
	    };

	if (kmpi != null) {
	    map.put("patches", kmpi);
	}
	    
	TemplateProcessor.KeyMapIterable kmi =
	    new TemplateProcessor.KeyMapIterable() {
		public Iterator<TemplateProcessor.KeyMap> iterator() {
		    CollectionScanner<Triangle> cs = new CollectionScanner<>();
		    if (full) {
			cs.add(triangleMap.values());
			cs.add(triangleSet);
		    }
		    return new
			EncapsulatingIterator<TemplateProcessor.KeyMap,Triangle>
			(full? cs.iterator(): tessellate()) {
			public TemplateProcessor.KeyMap next() {
			    Triangle triangle = encapsulatedNext();
			    TemplateProcessor.KeyMap m =
				new TemplateProcessor.KeyMap();
			    m.put("X1", "" + (float)(triangle.x1*unitScale));
			    m.put("Y1", "" + (float)(triangle.y1*unitScale));
			    m.put("Z1", "" + (float)(triangle.z1*unitScale));
			    m.put("X2", "" + (float)(triangle.x2*unitScale));
			    m.put("Y2", "" + (float)(triangle.y2*unitScale));
			    m.put("Z2", "" + (float)(triangle.z2*unitScale));
			    m.put("X3", "" + (float)(triangle.x3*unitScale));
			    m.put("Y3", "" + (float)(triangle.y3*unitScale));
			    m.put("Z3", "" + (float)(triangle.z3*unitScale));
			    if (triangle.color != null) {
				m.put("APPEARANCE",
				      x3dAppearance(triangle.color));
			    }
			    return m;
			}
		    };
		}
	    };
	map.put("triangles", kmi);

	TemplateProcessor tp = new TemplateProcessor(map);
	// String template = (binary)? "x3db.tpl": "x3d.tpl";
	final String template = "x3d.tpl";
	try {
	    AccessController.doPrivileged
		(new PrivilegedExceptionAction<Void>() {
		    public Void run() throws IOException {
			Reader rd = new InputStreamReader
			    (Model3D.class.getResourceAsStream(template),
			     "UTF-8");
			/*
			  tp.processSystemResource(PACKAGE + "/" + template,
			  "UTF-8",
			  out);
			*/
			tp.processTemplate(rd, "UTF-8", out);
			rd.close();
			if (compress) {
			    out.close();
			}
			return (Void)null;
		    }
		});
	} catch (PrivilegedActionException e) {
	    throw (IOException) e.getException();
	}
    }

    /**
     * Create an STL file from the model, given a writable byte channel.
     * @param id the STL file's ID string (limited to 80 7-bit ASCII characters)
     * @param c the output channel
     * @exception IOException an error occurred when writing to the channel
     */
    public void writeSTL(String id, WritableByteChannel c) throws IOException {
	int i;
	setSTLBaseIfNeeded();
	ByteBuffer buffer = ByteBuffer.allocate(126);
	buffer.order(ByteOrder.LITTLE_ENDIAN);
	buffer.clear();
	long triangleCount = triangleMap.size() + triangleSet.size();
	if (triangleCount >= Integer.MAX_VALUE) {
	    throw new IOException(errorMsg("tooManyTriangles"));
	}
	if (id.length() > 80) {
	    throw new IllegalArgumentException(errorMsg("idTooLong"));
	}

	LinkedList<Triangle>tlist = new LinkedList<Triangle>();
	if (cubics.size() > 0 || cubicVertices.size() > 0) {
	    triangleCount = 0;
	    Iterator<Triangle> it = tessellate();
	    while (it.hasNext()) {
		Triangle triangle = it.next();
		triangleCount++;
		if (triangleCount >= Integer.MAX_VALUE) {
		    throw new IOException(errorMsg("tooManyTriangles"));
		}
		tlist.add(triangle);
	    }
	} else {
	    tlist.addAll(triangleMap.values());
	    tlist.addAll(triangleSet);
	}
	Collections.sort(tlist);

	int tcount = (int)triangleCount;
	i = 0;
	while (i < id.length()) {
	    char ch = id.charAt(i);
	    if (ch == 0 || ch > 128)
		throw new IllegalArgumentException(errorMsg("illegalIdChars"));
	    buffer.put((byte)ch);
	    i++;
	}
	while (i++ < 80) buffer.put((byte)0);
	buffer.putInt((int)tcount);
	buffer.flip();
	c.write(buffer);

	for (Triangle triangle: tlist) {
	    writeTriangleSTL(c, buffer, triangle);
	}
    }

    /**
     * Create a sequence of images.
     * The images will be created in the format supported by
     * {@link org.bzdev.gio.ImageSequenceWriter}.
     * When the number of steps is 0, only a single image will appear
     * in the sequence.  When &theta; is 0 or 180 degrees, $phi; will
     * be set to 0.  if nphi is 0, &phi; will also be set to 0.
     * Otherwise for each value of &theta; &phi; will range from 0 to
     * 360 in steps of 360/nphi.
     * <P>
     * The images show the edges of triangles in green, and
     * shows the interior side of a triangle in red.  The background is
     * a dark blue and the model is various shades of gray depending on
     * a triangle's orientation.  This is intended for visual model
     * checking.
     * @param os the output stream
     * @param imageType the image type
     * @param nphi the number of steps for the Eulerian angle &phi;
     * @param ntheta the number of steps for the Eulerian angle $theta;
     */
    public void createImageSequence(OutputStream os, String imageType,
				    int nphi, int ntheta)
	throws IOException
    {
	createImageSequence(os, imageType, nphi, ntheta, 0.0, 0.0, 0.0, true);
    }


    /**
     * Create a sequence of images, specifying a color factor and
     * the maximum triangle size for rendering.
     * The images will be created in the format supported by
     * {@link org.bzdev.gio.ImageSequenceWriter}.
     * When the number of steps is 0, only a single image will appear
     * in the sequence.  When &theta; is 0 or 180 degrees, $phi; will
     * be set to 0.  if nphi is 0, &phi; will also be set to 0.
     * Otherwise for each value of &theta; &phi; will range from 0 to
     * 360 in steps of 360/nphi.
     * <P>
     * The parameters delta and colorFactor are used to fine-tune the
     * images. A non-zero value of delta will result in triangles above
     * a critical size being partitioned into smaller triangles for
     * rendering in order to reduce Z-ordering problems. A non-zero
     * color factor will cause triangles with lower Z values (after all
     * transformations and coordinate-system rotations) to appear
     * darker. This is useful when a model has multiple parallel surfaces
     * with different Z values. and the user otherwise cannot distinguish
     * them.
     * <P>
     * The images show the edges of triangles in green, and
     * shows the interior side of a triangle in red.  The background is
     * a dark blue and the model is various shades of gray depending on
     * a triangle's orientation.  This is intended for visual model
     * checking.
     * @param os the output stream
     * @param imageType the image type
     * @param nphi the number of steps for the Eulerian angle &phi;
     * @param ntheta the number of steps for the Eulerian angle $theta;
     * @param delta the maximum triangle size for rendering; 0.0 if this
     *        parameter should be ignored
     * @param colorFactor the color factor; 0.0 if this parameter should
     *        be ignored
     * @see Model3D.Image#setDelta(double)
     * @see Model3D.Image#setColorFactor(double)
     */
       public void createImageSequence(OutputStream os, String imageType,
				    int nphi, int ntheta,
				    double delta, double colorFactor)
	throws IOException
    {
	createImageSequence(os, imageType, nphi, ntheta,
			    delta, colorFactor, 0.0, true);
    }

    /**
     * Create a sequence of images, specifying a color factor, normal
     * factor and the maximum triangle size for rendering.
     * The images will be created in the format supported by
     * {@link org.bzdev.gio.ImageSequenceWriter}.
     * When the number of steps is 0, only a single image will appear
     * in the sequence.  When &theta; is 0 or 180 degrees, $phi; will
     * be set to 0.  if nphi is 0, &phi; will also be set to 0.
     * Otherwise for each value of &theta; &phi; will range from 0 to
     * 360 in steps of 360/nphi.
     * <P>
     * The parameters delta and colorFactor are used to fine-tune the
     * images. A non-zero value of delta will result in triangles above
     * a critical size being partitioned into smaller triangles for
     * rendering in order to reduce Z-ordering problems. A non-zero
     * color factor will cause triangles with lower Z values (after all
     * transformations and coordinate-system rotations) to appear
     * darker. This is useful when a model has multiple parallel surfaces
     * with different Z values. and the user otherwise cannot distinguish
     * them. The color factor is useful primarily for surfaces that
     * are in the X-Y plane after coordinate transformations. If the
     * the normal factor is set to a small positive value, the color
     * factor will be effectively reduced when triangles whose normal vectors
     * are not aligned with the Z axis are rendered. For a normal factor
     * f, the color factor will be reduced by a factor of
     * exp(-(1-n<sub>z</sub>)/f) where n<sub>z</sub> is the Z component
     * of the normal vector.
     * <P>
     * The images show the edges of triangles in green, and
     * shows the interior side of a triangle in red.  The background is
     * a dark blue and the model is various shades of gray depending on
     * a triangle's orientation.  This is intended for visual model
     * checking.
     * @param os the output stream
     * @param imageType the image type
     * @param nphi the number of steps for the Eulerian angle &phi;
     * @param ntheta the number of steps for the Eulerian angle $theta;
     * @param delta the maximum triangle size for rendering; 0.0 if this
     *        parameter should be ignored
     * @param colorFactor the color factor; 0.0 if this parameter should
     *        be ignored
     * @param normalFactor the normal factor; 0.0 if there is none
     * @param showEdges true if edges of triangles and patches should be shown;
     *        false otherwise
     * @see Model3D.Image#setDelta(double)
     * @see Model3D.Image#setColorFactor(double)
     * @see Model3D.Image#setNormalFactor(double)
     */
       public void createImageSequence(OutputStream os, String imageType,
				       int nphi, int ntheta,
				       double delta, double colorFactor,
				       double normalFactor, boolean showEdges)
	throws IOException
    {
	ImageSequenceWriter isw = new ImageSequenceWriter(os);

	String ext = OutputStreamGraphics.getSuffixForImageType(imageType);
	int total = nphi*(ntheta+1);
	int cnt = 0;
	while (total > 0) {
	    total /= 10;
	    cnt++;
	}

	final int WIDTH = 700;
	final int HEIGHT = 700;

	String format = "image%0" + cnt + "d." + ext;
	String mtype = OutputStreamGraphics.getMediaTypeForImageType(imageType);
	isw.addMetadata(WIDTH, HEIGHT, mtype, format);
	double deltaPhi = (nphi == 0.0)? 0.0: 360.0 / nphi;
	double deltaTheta = (ntheta == 0.0)? 0.0: 180.0/ntheta;
	boolean noPhis = true;

	for (int j = 0; j <= ntheta; j++) {
	    for (int i = 0; i < nphi; i++) {
		double phi = i * deltaPhi;
		double theta = j * deltaTheta;
		String name = String.format("phi=%1.1f,theta=%1.1f",
					    phi, theta);
		OSGraphicsOps osg =
		    isw.nextOutputStreamGraphics("images/"+name);
		Model3D.Image image = new Model3D.Image(osg);
		if (showEdges) {
		    image.setEdgeColor(Color.GREEN);
		}
		image.setBacksideColor(Color.RED);
		Graphics2D g2d = image.createGraphics();
		g2d.setBackground(Color.BLUE.darker().darker());
		g2d.clearRect(0, 0, WIDTH, HEIGHT);

		image.setCoordRotation(Math.toRadians(phi),
				       Math.toRadians(theta),
				       0.0);
		image.setDelta (delta);
		image.setColorFactor(colorFactor);
		image.setNormalFactor(normalFactor);
		setImageParameters(image, 50.0);
		render(image);

		g2d.setColor(Color.WHITE);
		Font font = new Font(Font.SANS_SERIF,Font.BOLD, 24);
		g2d.setFont(font);
		String title = String.format("\u03c6 = %1.1f\u00B0,"
					     + " \u03b8 = %1.1f\u00B0",
					    phi, theta);
		int textwidth = (int) Math.round
		    (g2d.getFontMetrics(font).getStringBounds(title, g2d)
		     .getWidth());
		g2d.drawString(title, (WIDTH - textwidth)/2, HEIGHT-25);
		g2d.dispose();
		image.write();
		osg.close();

		if (j == 0 || j == ntheta) {
		    break;
		}
	    }
	}
	isw.close();
    }

    /**
     * Generate a graph containing a cross section of this model, using
     * the current tessellation level, where triangle edges pass through
     * a plane that passes through the point (xp, yp, zp) and perpendicular
     * to a normal vector.
     * <P>
     * The orientation of the cross section is implementation dependent,
     * but may be rotated by 90 degrees if that will result in a larger
     * image.
     * The triangles and edges that go through the plane will be
     * drawn, projected onto the plane, and will be denoted by black
     * lines.  Any edges and triangles reported by the methods
     * {@link #verifyClosed2DManifold()} or
     * {@link #verifyEmbedded2DManifold()} will be denoted by red lines.
     * After this method is called, the graph should be written or otherwise
     * displayed. The graph will have its offsets and ranges
     * set automatically.
     * For example, in a test case that purposely generated an error and
     * where {@link #notPrintable(Appendable)} printed
     * <BLOCKQUOTE><CODE><PRE>
     * 3D model's surface is not embedded in a three-dimensional space:
     * Planar Triangle (-6.70000,2.30000,4.70000)-(-6.70000,6.70000,4.70000)-(-1.10000,1.90526,4.70000)
     * Planar Triangle (-3.00000,3.00000,8.00000)-(3.00000,3.00000,4.00000)-(-3.00000,3.00000,4.00000)

     * </PRE></CODE></BLOCKQUOTE>
     * The code
     * <BLOCKQUOTE><CODE><PRE>
     * Graph graph = new Graph(700, 700);
     * m3d.createCrossSection(graph, -3.0, 3.0, 4.7,
     *                        new double[] {0.0, 1.0, 0.0});
     * graph.write("png", "fakelock.png");
     * </PRE></CODE></BLOCKQUOTE>
     * produced the following image (truncated and rotated for display
     * purposes:
     * <P style="text-align: center">
     * <img src="doc-files/fakelock.png"  class="imgBackground">
     * <P>
     * thus providing a visual representation of the error.
     * @param graph the graph
     * @param xp the X coordinate of the designated point on the plane
     * @param yp the Y coordinate of the designated point on the plane
     * @param zp the Z coordinate of the designated point on the plane
     * @param normal the normal vector for the plane
     * @exception IllegalArgumentException if the graph or normal vector
     *            are null, if the graph's size is less than 200&times;200,
     *            of if the normal vector's norm is 0
     */
    public void createCrossSection(Graph graph,
				   double xp, double yp, double zp,
				   double[] normal)
    {
	if (graph == null) {
	    throw new NullPointerException(errorMsg("nullGraph"));
	}
	if (normal == null) {
	    throw new NullPointerException(errorMsg("nullNormal"));
	}
	double gwidth = graph.getWidth();
	double gheight = graph.getHeight();
	if (gwidth < 200 || gheight < 200)  {
	    long igwidth = Math.round(gwidth);
	    long igheight = Math.round(gheight);
	    String msg = errorMsg("graphDims", igwidth, igheight);
	    throw new IllegalArgumentException(msg);
	}
	gwidth -= 100;
	gheight -= 100;

	List<Edge> badEdges = verifyClosed2DManifold();
	List<Triangle> badTriangles = verifyEmbedded2DManifold();

	int i = 0;
	double max = 0.0;
	for (int j = 0; j < 3; j++) {
	    double a = Math.abs(normal[j]);
	    if (a > max) {
		i = j;
		max = a;
	    }
	}
	double[] xdir = null;
	double[] ydir = null;
	switch (i) {
	case 0:
	    xdir = new double[] {0.0, 1.0, 0.0};
	    ydir = new double[] {0.0, 0.0, 1.0};
	    break;
	case 1:
	    xdir = new double[] {0.0, 0.0, 1.0};
	    ydir = new double[] {1.0, 0.0, 0.0};
	    break;
	case 2:
	    xdir = new double[] {1.0, 0.0, 0.0};
	    ydir = new double[] {0.0, 1.0, 0.0};
	    break;
	}
	double[] xaxis = VectorOps.crossProduct(ydir, normal);
	VectorOps.normalize(xaxis);
	double[] yaxis = VectorOps.crossProduct(normal, xaxis);
	VectorOps.normalize(yaxis);
	double[] zaxis = VectorOps.unitVector(normal);
	double[] tmp = new double[3];
	Rectangle2D rectangle = new Rectangle2D.Double();
	rectangle.setRect(0.0, 0.0, 0.0, 0.0);
	ArrayList<Line2D> lines = new ArrayList<>();

	Iterator<Triangle> it = tessellate();
	while (it.hasNext()) {
	    Triangle triangle = it.next();
	    tmp[0] = triangle.x1 - xp;
	    tmp[1] = triangle.y1 - yp;
	    tmp[2] = triangle.z1 - zp;
	    double x1 = VectorOps.dotProduct(xaxis, tmp);
	    double y1 = VectorOps.dotProduct(yaxis, tmp);
	    double z1 = VectorOps.dotProduct(zaxis, tmp);
	    tmp[0] = triangle.x2 - xp;
	    tmp[1] = triangle.y2 - yp;
	    tmp[2] = triangle.z2 - zp;
	    double x2 = VectorOps.dotProduct(xaxis, tmp);
	    double y2 = VectorOps.dotProduct(yaxis, tmp);
	    double z2 = VectorOps.dotProduct(zaxis, tmp);
	    tmp[0] = triangle.x3 - xp;
	    tmp[1] = triangle.y3 - yp;
	    tmp[2] = triangle.z3 - zp;
	    double x3 = VectorOps.dotProduct(xaxis, tmp);
	    double y3 = VectorOps.dotProduct(yaxis, tmp);
	    double z3 = VectorOps.dotProduct(zaxis, tmp);
	    if (z1 > limit && z2 > limit && z3 > limit) continue;
	    if (z1 < -limit &&  z2 < -limit && z3 < -limit) continue;
	    Line2D line1 = new Line2D.Double(x1, y1, x2, y2);
	    Line2D line2 = new Line2D.Double(x2, y2, x3, y3);
	    Line2D line3 = new Line2D.Double(x3, y3, x1, y1);
	    lines.add(line1);
	    lines.add(line2);
	    lines.add(line3);
	    var rect = line1.getBounds2D();
	    Rectangle2D.union(rectangle, rect, rectangle);
	    rect = line2.getBounds2D();
	    Rectangle2D.union(rectangle, rect, rectangle);
	    rect = line3.getBounds2D();
	    Rectangle2D.union(rectangle, rect, rectangle);
	}
	graph.setOffsets(50, 50, 50, 50);
	double width = rectangle.getWidth();
	double height = rectangle.getHeight();
	double scalex = gwidth/width;
	double scaley = gheight/height;
	double scalef = Math.min(scalex, scaley);

	double scalex2 = gheight/width;
	double scaley2 = gwidth/height;
	double scalef2 = Math.min(scalex2, scaley2);

	boolean rot = (scalef < scalef2);

	if (rot) {
	    scalef = scalef2;
	}
	graph.setRanges(rectangle.getCenterX(), rectangle.getCenterY(),
			0.5, 0.5, scalef, scalef);
	if (rot) {
	    graph.setRotation(-Math.PI/2,
			      rectangle.getCenterX(),
			      rectangle.getCenterY());
	}

	Graphics2D g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(1.5F));
	g2d.setColor(Color.BLACK);
	for(Line2D line: lines) {
	    graph.draw(g2d, line);
	}
	g2d.setColor(Color.RED);
	if (badEdges != null) {
	    for(Edge edge: badEdges) {
		tmp[0] = edge.getX1() - xp;
		tmp[1] = edge.getY1() - yp;
		tmp[2] = edge.getZ1() - zp;
		double x1 = VectorOps.dotProduct(xaxis, tmp);
		double y1 = VectorOps.dotProduct(yaxis, tmp);
		double z1 = VectorOps.dotProduct(zaxis, tmp);
		tmp[0] = edge.getX2() - xp;
		tmp[1] = edge.getY2() - yp;
		tmp[2] = edge.getZ2() - zp;
		double x2 = VectorOps.dotProduct(xaxis, tmp);
		double y2 = VectorOps.dotProduct(yaxis, tmp);
		double z2 = VectorOps.dotProduct(zaxis, tmp);
		if (z1 > limit && z2 > limit) continue;
		if (z1 < -limit && z2 < -limit) continue;
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		graph.draw(g2d, line);
	    }
	}
	if (badTriangles != null) {
	    for (Triangle triangle: badTriangles) {
		tmp[0] = triangle.x1 - xp;
		tmp[1] = triangle.y1 - yp;
		tmp[2] = triangle.z1 - zp;
		double x1 = VectorOps.dotProduct(xaxis, tmp);
		double y1 = VectorOps.dotProduct(yaxis, tmp);
		double z1 = VectorOps.dotProduct(zaxis, tmp);
		tmp[0] = triangle.x2 - xp;
		tmp[1] = triangle.y2 - yp;
		tmp[2] = triangle.z2 - zp;
		double x2 = VectorOps.dotProduct(xaxis, tmp);
		double y2 = VectorOps.dotProduct(yaxis, tmp);
		double z2 = VectorOps.dotProduct(zaxis, tmp);
		tmp[0] = triangle.x3 - xp;
		tmp[1] = triangle.y3 - yp;
		tmp[2] = triangle.z3 - zp;
		double x3 = VectorOps.dotProduct(xaxis, tmp);
		double y3 = VectorOps.dotProduct(yaxis, tmp);
		double z3 = VectorOps.dotProduct(zaxis, tmp);
		if (z1 > limit && z2 > limit && z3 > limit) continue;
		if (z1 < -limit && z2 < -limit && z3 < -limit) continue;
		Line2D line1 = new Line2D.Double(x1, y1, x2, y2);
		Line2D line2 = new Line2D.Double(x2, y2, x3, y3);
		Line2D line3 = new Line2D.Double(x3, y3, x1, y1);
		graph.draw(g2d, line1);
		graph.draw(g2d, line2);
		graph.draw(g2d, line3);
	    }
	}
	g2d.dispose();
    }
}

//  LocalWords:  exbundle STL blockquote pre setStackTraceMode stl os
//  LocalWords:  writeSTL BufferedImage ARGB createGraphics fillRect
//  LocalWords:  setImageParameters png modeltest setBackgroundColor
//  LocalWords:  DViewFactory setModel createObject tmp maxframes img
//  LocalWords:  estimateFrameCount initFrames maxFrames ImageData OL
//  LocalWords:  scheduleFrames setCoordRotation boolean FileAccessor
//  LocalWords:  OutputStream isRasterPremultiplied premultiplied osg
//  LocalWords:  Hashtable imageType IndexColorModel ImageIO accessor
//  LocalWords:  OutputStreamGraphics IOException doPrivileged param
//  LocalWords:  cannotWriteImageType  getHeight getWidth vertices xt
//  LocalWords:  cannotWriteImageTypeOS setOrigin setScaleFactor AWT
//  LocalWords:  setRotationOrigin setTranslation setLightSource yt
//  LocalWords:  setColorFactor setDelta setBacksideColor scaleFactor
//  LocalWords:  setEdgeColor setDefaultSegmentColor Eulerian coord
//  LocalWords:  setDefaultBacksideSegmentColor Goldstein xoriginAR
//  LocalWords:  moveOriginAfterRotation ImageDataImpl argOutOfRange
//  LocalWords:  yoriginAR zoriginAR ImageParams getXOrigin getPhi tx
//  LocalWords:  getYOrigin getScaleFactor getTheta getPsi getBorder
//  LocalWords:  getXTranslation getYTranslation getMinXTranslation
//  LocalWords:  getMinYTranslation getMaxXTranslation getLowerBoundX
//  LocalWords:  getMaxYTranslation getViewFractionX getViewFractionY
//  LocalWords:  getMagnification getScrollFractionX getLowerBoundY
//  LocalWords:  getScrollFractionY getUpperBoundX getUpperBoundY ty
//  LocalWords:  getShowsAllX getShowsAllY SecurityException hashcode
//  LocalWords:  tagWasTriangle identicalEndPoints setObjectRotation
//  LocalWords:  identicalEndPointsTag setObjectTranslation pushParms
//  LocalWords:  parmstackEmpty tmatrix CollectionScanner addToSelf
//  LocalWords:  subtriangles edgeMask badCase idata xfract yfract nx
//  LocalWords:  changeScale argsOutOfRange xfractmin yfractmin fname
//  LocalWords:  afterwards verifyEmbedded DManifold Appendable UTC
//  LocalWords:  ManifoldException numberOfComponents hollowComponent
//  LocalWords:  compoents appendable sintering notClosedManifold tpl
//  LocalWords:  notEmbedded setSTLBaseErr setSTLBase setUnitScaleX
//  LocalWords:  newChannel diffuseColor APPNAME UTF tooManyTriangles
//  LocalWords:  idTooLong illegalIdChars scrunner rdot yref zref ny
//  LocalWords:  nz setTag zeroWidthEdge NaN IllegalArgumentException
//  LocalWords:  printability notPrintable verifyClosed notHollow isq
//  LocalWords:  verifyNesting getComponent pushTransform popParms gc
//  LocalWords:  popTransform nullArgument IllegalStateException DOps
//  LocalWords:  popError linearVertices addModel isClosedManifold si
//  LocalWords:  nphi ntheta colorFactor setNormalFactor normalFactor
//  LocalWords:  listEdges triangleMap createImageSequence viewable
//  LocalWords:  FileOutputStream OSGraphicsOps GraphCreator isPatch
//  LocalWords:  eacute zier notWellFormed ZLE notOriented JRE writeX
//  LocalWords:  wrongGetSegmentType infoset isSupported DOCTYPE DTD
//  LocalWords:  controlPoints showEdges Bezier pcoords cubics xarea
//  LocalWords:  getBoundaryTags getSegmentTag segmentIndices tF tT
//  LocalWords:  te tB tY FISOutputStream nullOutputStream tp ZLECV
//  LocalWords:  UnsupportedOperationException processSystemResource
//  LocalWords:  tlevel SteppedGrid utessellation vtessellation ulp
//  LocalWords:  subclasses ourCubics xp yp zp createCrossSection src
//  LocalWords:  fakelock nullGraph nullNormal graphDims
