package org.bzdev.anim2d;

import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.obnaming.misc.*;
import org.bzdev.graphs.*;
import org.bzdev.geom.SplinePath2D;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.WindingRule;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.CPoint;
import org.bzdev.devqsim.SimFunction;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Abstract factory for animation layers.
 * <P>
 * In addition to controlling the stacking order (z-order) and visibility,
 * this factory can configure an animation layer by providing the ability
 * to add standard graphic objects to it, including the following:
 * <UL>
 *   <LI> Arcs.
 *   <LI> Cubic curves.
 *   <LI> Ellipses.
 *   <LI> Images. (The actual image is specified via a URL, and may be
 *        scaled, translated, and rotated as desired).
 *   <LI> Lines.
 *   <LI> Quadratic paths.
 *   <LI> Rectangles.
 *   <LI> Round Rectangles.
 *   <LI> Spline paths. (A subclass of {@link java.awt.geom.Path2D.Double}
 *        that can use splines to provide smooth curves through a specified
 *        set of points.)
 *   <LI> Text.
 * </UL>
 * Most of the above are based on classes in the java.awt.geom
 * package.
 * <P>
 * While there are a few factory parameters that apply to an animation
 * layer as a whole, most of the parameters are subparameters of the
 * <CODE>object</CODE> parameter. These parameters have integer keys,
 * with the parameter <CODE>object.type</CODE> indicating the type of
 * object. For each type, a specific set of subparameters is used.
 * With the exception of spline paths, each object is
 * represented by a set of parameters with a common integer-valued
 * key. For spline paths, the keys are also integer-valued, but multiple
 * keys are needed to describe a path. Each path is
 * delimited by entries with a <CODE>PATH_START</CODE> and
 * <CODE>PATH_END</CODE> object type. In between are a series of path
 * segments separated by entries with a <CODE>SEG_END</CODE>,
 * <CODE>SEG_END_PREV</CODE>, <CODE>SEG_END_NEXT</CODE> or
 * <CODE>SEG_CLOSE</CODE> object type.
 * Each segment can optionally contain one or two control points (the
 * object type is <CODE>CONTROL_POINT</CODE>, corresponding to
 * quadratic or cubic B&eacute;zier curves.  Alternatively, a path
 * segment can contain a series of entries whose object type is
 * <CODE>SPLINE_POINT</CODE> or <CODE>SPLINE_FUNCTION</CODE>.
 * The allowed sequences of object.type values for these paths are shown
 * in the following diagram:
 * <DIV style="text-align: center">
 * <img src="doc-files/layerpath.png" class="imgBackground">
 * </DIV>
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link AnimationLayer2DFactory}
 * (the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html#object.type-org.bzdev.anim2d.AnimationLayer2DFactory" target="ftable">object.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationLayer2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "AnimationLayer2DParmManager",
		    tipResourceBundle = "*.lpack.AnimLayerTips",
		    labelResourceBundle = "*.lpack.AnimLayerLabels",
		    docResourceBundle = "*.lpack.AnimLayerDocs")
public abstract class AbstrAnimLayer2DFactory<Obj extends AnimationLayer2D>
    extends AnimationObject2DFactory<Obj>
{

    static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }

    @CompoundParmType(tipResourceBundle = "*.lpack.AnimLayerSpecTips",
		      labelResourceBundle = "*.lpack.AnimLayerSpecLabels",
		      docResourceBundle = "*.lpack.AnimLayerSpecDocs")
    static class Spec {
	@PrimitiveParm("type")
	    AnimationLayer2D.Type type = AnimationLayer2D.Type.NULL;
	@PrimitiveParm("x") double x = 0.0;
	@PrimitiveParm("y") double y = 0.0;
	@PrimitiveParm("height") double height = 0.0;
	@PrimitiveParm("width") double width = 0.0;
	@PrimitiveParm("extent") double extent = 0.0;
	@PrimitiveParm("start") double start = 0.0;
	@PrimitiveParm("xend") double xend = 0.0;
	@PrimitiveParm("yend") double yend = 0.0;
	@PrimitiveParm("xcontrol") double xcontrol = 0.0;
	@PrimitiveParm("ycontrol") double ycontrol = 0.0;
	@PrimitiveParm("xcontrol1") double xcontrol1 = 0.0;
	@PrimitiveParm("ycontrol1") double ycontrol1 = 0.0;
	@PrimitiveParm("xcontrol2") double xcontrol2 = 0.0;
	@PrimitiveParm("ycontrol2") double ycontrol2 = 0.0;
	@PrimitiveParm("arcwidth") double arcwidth = 0.0;
	@PrimitiveParm("archeight") double archeight = 0.0;
	@PrimitiveParm("text") String text = "";
	@PrimitiveParm("imageURL") String imageURL = "";
	@PrimitiveParm("imageAngle") double imageAngle = 0.0;
	@PrimitiveParm("imageScaleX") double imageScaleX = 1.0;
	@PrimitiveParm("imageScaleY") double imageScaleY = 1.0;
	@PrimitiveParm("imageFlipX") boolean imageFlipX = false;
	@PrimitiveParm("imageFlipY") boolean imageFlipY = false;
	@PrimitiveParm("imageInGCS") boolean imageInGCS = true;
	@PrimitiveParm("refPoint") RefPointName refPointName =
	    RefPointName.LOWER_LEFT;
	@PrimitiveParm("windingRule")
	SplinePathBuilder.WindingRule windingRule =
	    SplinePathBuilder.WindingRule.WIND_NON_ZERO;
	@PrimitiveParm("draw") boolean draw = false;
	@PrimitiveParm("fill") boolean fill = false;
	@PrimitiveParm("xf") SimFunction xf = null;
	@PrimitiveParm("yf") SimFunction yf = null;
	@PrimitiveParm("t1") double t1 = 0.0;
	@PrimitiveParm("t2") double t2 = 0.0;
	@PrimitiveParm("n") int n = 0;
	@PrimitiveParm("shape") AnimationShape2D shape = null;
	Spec() {}
    }

    @KeyedCompoundParm("object")
	Map<Integer,Spec> specMap = new TreeMap<Integer,Spec>();

    @KeyedCompoundParm("object.drawColor") Map<Integer,ColorParm>
	drawColorMap = new TreeMap<Integer,ColorParm>();

    @KeyedCompoundParm("object.fillColor") Map<Integer,ColorParm>
	fillColorMap =	new TreeMap<Integer,ColorParm>();

    @KeyedCompoundParm("object.fontParms") Map<Integer,GraphFontParm>
	fontParmsMap = new TreeMap<Integer,GraphFontParm>();

    @KeyedCompoundParm("object.stroke")
    Map<Integer,BasicStrokeParm>strokeMap =
	new TreeMap<Integer,BasicStrokeParm>();

    static class OurColorParm extends ColorParm {
	public OurColorParm() {
	    super("black");
	}
    }

    @CompoundParm("drawColor")
	ColorParm drawColorParm = new OurColorParm();
    @CompoundParm("fillColor")
	ColorParm fillColorParm = new OurColorParm();

    @CompoundParm("stroke")
	BasicStrokeParm strokeParm = new BasicStrokeParm(1.0);

    @CompoundParm("fontParms")
	GraphFontParm fontParms = new GraphFontParm();

    AnimationLayer2DParmManager<Obj> pm;

    SplinePathBuilder spb = new SplinePathBuilder();

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected AbstrAnimLayer2DFactory(Animation2D a2d) {
	super(a2d);

	pm = new AnimationLayer2DParmManager<Obj>(this);
	initParms(pm, AbstrAnimLayer2DFactory.class);

	// The "object" parameter is used to clear or set default
	// values for a group of primitive parameters. We have these
	// split into multiple tables, so we have to remove it and then
	// replace it, keeping references to the original Parm instances.
	final ParmParser objectParmParser = pm.getParm("object").getParser();
	final ParmParser objectDrawColorParmParser =
	    pm.getParm("object.drawColor").getParser();
	final ParmParser objectFillColorParmParser =
	    pm.getParm("object.fillColor").getParser();
	final ParmParser objectStrokeParmParser =
	    pm.getParm("object.stroke").getParser();
	final ParmParser objectGraphFontParmParser =
	    pm.getParm("object.fontParms").getParser();
	removeParm("object");
	initParm(new Parm("object",
			  int.class, null,
			  new ParmParser() {
			      public void parse(int key) {
				  // we want the color and stroke tables
				  // to not have an entry unless explicitly
				  // requested.
				  objectParmParser.parse(key);
			      }
			      public void clear() {
				  objectParmParser.clear();
				  objectDrawColorParmParser.clear();
				  objectFillColorParmParser.clear();
				  objectStrokeParmParser.clear();
				  objectGraphFontParmParser.clear();
			      }
			      public void clear(int key) {
				  objectParmParser.clear(key);
				  objectDrawColorParmParser.clear(key);
				  objectFillColorParmParser.clear(key);
				  objectStrokeParmParser.clear(key);
				  objectGraphFontParmParser.clear(key);
			      }
			  },
			  null), AnimationLayer2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);

    }

    // using graph coordinate space units.
    static double getX(double x, double w, RefPointName rpn) {
	switch (rpn) {
	case LOWER_CENTER:
	case CENTER:
	case UPPER_CENTER:
	    return x - w/2.0;
	case LOWER_LEFT:
	case CENTER_LEFT:
	case UPPER_LEFT:
	    return x;
	case LOWER_RIGHT:
	case CENTER_RIGHT:
	case UPPER_RIGHT:
	    return x - w;
	default:
	    return 0.0;
	}
    }
    // using graph coordinate space units.
    static double getY(double y, double h, RefPointName rpn) {
	switch (rpn) {
	case CENTER:
	case CENTER_LEFT:
	case CENTER_RIGHT:
	    return y - h/2.0;
	case LOWER_CENTER:
	case LOWER_LEFT:
	case LOWER_RIGHT:
	    return y;
	case UPPER_CENTER:
	case UPPER_LEFT:
	case UPPER_RIGHT:
	    return y - h;
	default:
	    return 0.0;
	}
    }

    static Image getImage(String url) {
	try {
	    return ImageIO.read(new URL(url));
	} catch (MalformedURLException ue) {
	    String msg = errorMsg("malformedURL", url);
	    throw new IllegalArgumentException(msg, ue);
	} catch(Exception e) {
	    String msg = errorMsg("imageNotFound", url);
	    throw new IllegalArgumentException(msg, e);
	}
    }

    Integer key;
    Spec value;
    BasicStrokeParm bspValue;
    ColorParm dcpValue;
    ColorParm fcpValue;
    GraphFontParm gfpValue;

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	ArrayList<Graph.Graphic> list = new ArrayList<>();
	for (Map.Entry<Integer,Spec> entry: specMap.entrySet()) {
	    key = entry.getKey();
	    value = entry.getValue();
	    bspValue = strokeMap.get(key);
	    if (bspValue == null) bspValue = strokeParm;
	    dcpValue = drawColorMap.get(key);
	    if (dcpValue == null) dcpValue = drawColorParm;
	    fcpValue = fillColorMap.get(key);
	    if (fcpValue == null) fcpValue = fillColorParm;
	    gfpValue = fontParmsMap.get(key);
	    if (gfpValue == null) gfpValue = fontParms;
	    switch (value.type) {
	    case NULL:
		throw new IllegalStateException(errorMsg("typeNotProvided"));
	    case ARC_CHORD:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			double start = -value.start;
			double extent = -value.extent;
			boolean gcsMode = bspValue.getGcsMode();
			Arc2D arc = new Arc2D.Double(x, y, w, h,
							 start, extent,
							 Arc2D.CHORD);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();

			public Rectangle2D boundingBox() {
			    return arc.getBounds2D();
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(arc);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(arc);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, arc);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, arc);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case ARC_OPEN:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			double start = -value.start;
			double extent = -value.extent;
			boolean gcsMode = bspValue.getGcsMode();
			Arc2D arc = new Arc2D.Double(x, y, w, h,
							 start, extent,
							 Arc2D.OPEN);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = arc.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(arc);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(arc);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, arc);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, arc);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case ARC_PIE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			double start = -value.start;
			double extent = -value.extent;
			boolean gcsMode = bspValue.getGcsMode();
			Arc2D arc = new Arc2D.Double(x, y, w, h,
							 start, extent,
							 Arc2D.PIE);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = arc.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(arc);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(arc);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, arc);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, arc);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case CONTROL_POINT:
		spb.append(new CPoint(CPointType.CONTROL, value.x, value.y));
		break;
	    case CUBIC_CURVE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double x = value.x;
			double y = value.y;
			double xcontrol1 = value.xcontrol1;
			double ycontrol1 = value.ycontrol1;
			double xcontrol2 = value.xcontrol2;
			double ycontrol2 = value.ycontrol2;
			double xend = value.xend;
			double yend = value.yend;
			boolean gcsMode = bspValue.getGcsMode();

			CubicCurve2D curve =
			    new CubicCurve2D.Double(x, y, xcontrol1, ycontrol1,
						    xcontrol2, ycontrol2,
						    xend, yend);
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = curve.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(curve);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, curve);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case ELLIPSE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			boolean gcsMode = bspValue.getGcsMode();
			Ellipse2D ellipse = new Ellipse2D.Double(x, y, w, h);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Rectangle2D bbox = ellipse.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			Stroke stroke = bspValue.createBasicStroke();
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(ellipse);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(ellipse);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, ellipse);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, ellipse);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case IMAGE:
		list.add(new Graph.Graphic() {
			Image img = getImage(value.imageURL);
			double x = value.x;
			double y = value.y;
			double angle = Math.toRadians(value.imageAngle);
			double scaleX = value.imageScaleX;
			double scaleY = value.imageScaleY;
			boolean flipX = value.imageFlipX;
			boolean flipY = value.imageFlipY;
			boolean imageInGCS = value.imageInGCS;
			RefPointName rpn = value.refPointName;
			Rectangle2D bbox = null;
			public Rectangle2D boundingBox() {
			    if (bbox == null && imageInGCS) {
				Graph g = getAnimation().getGraph();
				try {
				    bbox = g.imageBoundingBox(img, x, y,
							      rpn, angle,
							      scaleX, scaleY,
							      imageInGCS);
				} catch (IOException e) {
				    bbox = null;
				}
			    }
			    return bbox;
			}

			public void addTo(Graph g,
					  Graphics2D g2d,
					  Graphics2D g2dGCS)
			{
			    try {
				g.drawImage(g2d, img, x, y, rpn, angle,
					    scaleX, scaleY, flipX, flipY,
					    imageInGCS);
			    } catch (IOException e) {
				String msg = errorMsg("imageNotReadable");
				throw new IllegalStateException(msg, e);
			    }
			}
		    });
		break;
	    case LINE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double x = value.x;
			double y = value.y;
			double xend = value.xend;
			double yend = value.yend;
			boolean gcsMode = bspValue.getGcsMode();
			Line2D line =
			    new Line2D.Double(x, y, xend, yend);
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = line.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(line);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, line);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case MOVE_TO:
		spb.append(new CPoint(CPointType.MOVE_TO, value.x, value.y));
		break;
	    case MOVE_TO_NEXT:
		spb.append(new CPoint(CPointType.MOVE_TO_NEXT));
	    case PATH_START:
		spb.initPath(value.windingRule);
		// spb.append(new CPoint(CPointType.MOVE_TO, value.x, value.y));
		break;
	    case PATH_END:
		list.add(new Graph.Graphic() {
			boolean draw = value.draw;
			boolean fill = value.fill;
			SplinePath2D spath = spb.getPath();
			boolean gcsMode = bspValue.getGcsMode();
			Stroke stroke = bspValue.createBasicStroke();
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Rectangle2D bbox = spath.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(spath);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(spath);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, spath);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, spath);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case QUAD_CURVE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double x = value.x;
			double y = value.y;
			double xcontrol = value.xcontrol;
			double ycontrol = value.ycontrol;
			double xend = value.xend;
			double yend = value.yend;
			boolean gcsMode = bspValue.getGcsMode();

			QuadCurve2D curve =
			    new QuadCurve2D.Double(x, y, xcontrol, ycontrol,
						   xend, yend);
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = curve.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(curve);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, curve);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case RECTANGLE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			boolean gcsMode = bspValue.getGcsMode();
			Rectangle2D rectangle =
			    new Rectangle2D.Double(x, y, w, h);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = rectangle.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(rectangle);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(rectangle);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, rectangle);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, rectangle);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case ROUND_RECTANGLE:
		list.add(new Graph.Graphic() {
			boolean fill = value.fill;
			boolean draw = value.draw;
			double w = value.width;
			double h = value.height;
			double x = getX(value.x, w, value.refPointName);
			double y = getY(value.y, h, value.refPointName);
			double arcwidth = value.arcwidth;
			double archeight = value.archeight;
			boolean gcsMode = bspValue.getGcsMode();
			RoundRectangle2D rectangle =
			    new RoundRectangle2D.Double(x, y, w, h,
							arcwidth, archeight);
			Color fc = fcpValue.createColor();
			Color dc = dcpValue.createColor();
			Stroke stroke = bspValue.createBasicStroke();
			Rectangle2D bbox = rectangle.getBounds2D();
			public Rectangle2D boundingBox() {
			    return bbox;
			}
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    if (gcsMode) {
				Color csave = g2dGCS.getColor();
				Stroke ssave = g2dGCS.getStroke();
				g2dGCS.setStroke(stroke);
				if (fill) {
				    g2dGCS.setColor(fc);
				    g2dGCS.fill(rectangle);
				}
				if (draw) {
				    g2dGCS.setColor(dc);
				    g2dGCS.draw(rectangle);
				}
				g2dGCS.setStroke(ssave);
				g2dGCS.setColor(csave);
			    } else {
				Color csave = g2d.getColor();
				Stroke ssave = g2d.getStroke();
				g2d.setStroke(stroke);
				if (fill) {
				    g2d.setColor(fc);
				    g.fill(g2d, rectangle);
				}
				if (draw) {
				    g2d.setColor(dc);
				    g.draw(g2d, rectangle);
				}
				g2d.setStroke(ssave);
				g2d.setColor(csave);
			    }
			}
		    });
		break;
	    case SEG_CLOSE:
		spb.append(new CPoint(CPointType.CLOSE));
		break;
	    case SEG_END:
		spb.append(new CPoint(CPointType.SEG_END, value.x, value.y));
		break;
	    case SEG_END_PREV:
		spb.append(new CPoint(CPointType.SEG_END_PREV));
		break;
	    case SEG_END_NEXT:
		spb.append(new CPoint(CPointType.SEG_END_NEXT));
	    case SPLINE_FUNCTION:
		spb.append(new CPoint(value.xf, value.yf, value.t1, value.t2,
				      value.n));
	    case SPLINE_POINT:
		spb.append(new CPoint(CPointType.SPLINE, value.x, value.y));
		break;
	    case SHAPE:
		list.add(value.shape);
		break;
	    case TEXT:
		list.add(new Graph.Graphic() {
			double x = value.x;
			double y = value.y;
			Graph.FontParms gfp = gfpValue.createFontParms();
			String text = value.text;
			public void addTo(Graph g,
					  Graphics2D g2d, Graphics2D g2dGCS)
			{
			    g.drawString(text, x, y, gfp);
			}
		    });
		break;
	    }
	}
	object.initGraphicArray(list);
    }
}

//  LocalWords:  exbundle subparameters SEG PREV eacute zier img src
//  LocalWords:  AnimationLayer DFactory HREF ftable IFRAME xend yend
//  LocalWords:  DParmManager xcontrol ycontrol arcwidth archeight xf
//  LocalWords:  imageURL imageAngle imageScaleX imageScaleY refPoint
//  LocalWords:  imageFlipX imageFlipY imageInGCS windingRule yf Parm
//  LocalWords:  drawColor fillColor fontParms malformedURL spb
//  LocalWords:  imageNotFound typeNotProvided imageNotReadable
//  LocalWords:  CPoint CPointType
