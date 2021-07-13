package org.bzdev.geom;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graph.Symbol;
import org.bzdev.graphs.Graph.SymbolFactory;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.math.VectorOps;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//@exbundle org.bzdev.geom.lpack.Geom

/**
 * Connect two convex paths with differing numbers of control
 * points.  Paths are considered to be convex when their projection
 * onto a plane is a convex two-dimensional path.  The orientation of
 * the surface is determined by traversing the outer of the two paths
 * and using the right-hand rule. When created using two-dimensional
 * paths, the outer path is converted into a counterclockwise path if
 * necessary and similarly the inner path is converted into a clockwise
 * path.
 * <P>
 * A two paths will typically lie in the same plane or be fairly close
 * to such a plane.  A typical use for this class is to act as a bridge
 * between a B&eacute;zier grid and a rectilinear object whose control
 * points may be separated by distances much larger than those for the
 * grid.
 * <P>
 * Occasionally it is easier to start with a 2D path in order
 * to take advantage of methods in classes such as {@link Paths2D},
 * and convert the 2D path to a 3D path.  Subclasses of {@link Path3D}
 * have constructors that simplify this process: for example,
 * {@link Path3D.Double#Double(Path2D,Transform3D)}.
 */
public class ConvexPathConnector implements Shape3D {

    static String errorMsg(String key, Object... args) {
	return GeomErrorMsg.errorMsg(key, args);
    }

    private static double[]  getControlPoints(Path2D path) {
	PathIterator pi = path.getPathIterator(null);
	return Path2DInfo.getControlPoints(pi, true);
    }

    private static Set<Point2D> getKnots(Path2D path,
					 final double xc,
					 final double yc) {
	PathIterator pi = path.getPathIterator(null);
	double[] coords = Path2DInfo.getControlPoints(pi, false);
	Set<Point2D> knots = new TreeSet<Point2D>((e1, e2) -> {
		double x1 = e1.getX() - xc;
		double y1 = e1.getY() - yc;
		double x2 = e2.getX() - xc;
		double y2 = e2.getY() - yc;
		double angle1 = Math.atan2(y1, x1);
		double angle2 = Math.atan2(y2, x2);
		if (angle1 < angle2) return -1;
		if (angle1 > angle2) return 1;
		// In case the angle test fails (which should not happen
		// for the either the inner or outer paths)...
		if (x1 < x2) return -1;
		if (x1 > x2) return 1;
		if (y1 < y2) return -1;
		if (y1 > y2) return 1;
		return 0;
	});
	for (int i = 0; i < coords.length; i += 2) {
	    Point2D p = new Point2D.Double(coords[i], coords[i+1]);
	    knots.add(p);
	}
	return knots;
    }

    private static double getAngle(Point2D p, double xc, double yc) {
		double x = p.getX() - xc;
		double y = p.getY() - yc;
		double angle = Math.atan2(y, x);
		// for round-off errors
		if (Math.abs(angle) < 1.e-10) {
		    angle = 0.0;
		}
		return angle;
    }

    private class AdjacentCPs {
	Point2D prev;
	Point2D next;
	double[] scoords;
    }

    private Map<Point2D,AdjacentCPs> createAdjacents(Path2D path) {
	Map<Point2D,AdjacentCPs> map = new HashMap<Point2D,AdjacentCPs>();
	Point2D start = null;
	Point2D p = null;
	Point2D prev = null;
	AdjacentCPs data = null;
	AdjacentCPs sdata = null;
	PathIterator pi = path.getPathIterator(null);
	double[] coords = new double[6];
	double[] tmp;
	while (!pi.isDone()) {
	    switch(pi.currentSegment(coords)) {
	    case PathIterator.SEG_MOVETO:
		p = new Point2D.Double(coords[0], coords[1]);
		data = new AdjacentCPs();
		map.put(p, data);
		start = p;
		sdata = data;
		break;
	    case PathIterator.SEG_LINETO:
		data.prev = prev;
		data.next = new Point2D.Double(coords[0], coords[1]);
		tmp = new double[2];
		System.arraycopy(coords, 0, tmp, 0, 2);
		data.scoords = tmp;
		prev = p;
		p = data.next;
		if (p.getX() == start.getX() && p.getY() == start.getY()) {
		    p = start;
		    data = sdata;
		} else {
		    data = new AdjacentCPs();
		}
		map.put(p, data);
		break;
	    case PathIterator.SEG_QUADTO:	
		data.prev = prev;
		data.next = new Point2D.Double(coords[2], coords[3]);
		tmp = new double[4];
		System.arraycopy(coords, 0, tmp, 0, 4);
		data.scoords = tmp;
		prev = p;
		p = data.next;
		if (p.getX() == start.getX() && p.getY() == start.getY()) {
		    p = start;
		    data = sdata;
		} else {
		    data = new AdjacentCPs();
		}
		map.put(p, data);
		break;
	    case PathIterator.SEG_CUBICTO:
		data.prev = prev;
		data.next = new Point2D.Double(coords[4], coords[5]);
		tmp = new double[6];
		System.arraycopy(coords, 0, tmp, 0, 6);
		data.scoords = tmp;
		prev = p;
		p = data.next;
		if (p.getX() == start.getX() && p.getY() == start.getY()) {
		    p = start;
		    data = sdata;
		} else {
		    data = new AdjacentCPs();
		}
		map.put(p, data);
		break;
	    case PathIterator.SEG_CLOSE:
		if (sdata != data) {
		    // need a final line to go back to the start
		    data.prev = prev;
		    data.next = start;
		    tmp = new double[2];
		    tmp[0] = start.getX();
		    tmp[1] = start.getY();
		    data.scoords = tmp;
		    sdata.prev = p;
		} else {
		   sdata.prev = prev;
		}
		break;
	    }
	    pi.next();
	}
	return map;
    }

    private boolean isVisible(AdjacentCPs data, Point2D p, Point2D op)
    {
	return Path2DInfo.lineSegmentsIntersect(p.getX(), p.getY(),
						op.getX(), op.getY(),
						data.prev.getX(),
						data.prev.getY(),
						data.next.getX(),
						data.next.getY());
    }

    private boolean isVisibleFromOuter(Map<Point2D,AdjacentCPs> innerMap,
				       Point2D ip,
				       Map<Point2D,AdjacentCPs> outerMap,
				       Point2D op)
    {
	AdjacentCPs data1 = innerMap.get(ip);
        double tangent1[] = {data1.scoords[0] - ip.getX(),
			     data1.scoords[1] - ip.getY()};
	AdjacentCPs data2 = innerMap.get(data1.prev);
	int len = data2.scoords.length;
	// System.out.println("ip = " + ip);
	// System.out.println("data1.prev = " + data1.prev);
	// System.out.println("op = " + op);
	double tangent2[] = {data2.scoords[0] - data1.prev.getX(),
			     data2.scoords[1] - data1.prev.getY()};
	// System.out.format("tangent2 = (%g, %g)\n", tangent2[0], tangent2[1]);

	double vector1[] = {ip.getX() - op.getX(),
			    ip.getY() - op.getY()};

	double vector2[] = {data1.prev.getX() - op.getX(),
			    data1.prev.getY() - op.getY()};

	double xprod1 = vector1[0]*tangent1[1] - vector1[1]*tangent1[0];
	double xprod2 =  vector2[0]*tangent2[1] - vector2[1]*tangent2[0];

	// System.out.format("xprod1 = %g, xprod2 = %g\n", xprod1, xprod2);

	return xprod1 > 0 && xprod2 > 0;

    }

    private boolean isVisibleFromInner(Map<Point2D,AdjacentCPs> innerMap,
				       Point2D ip,
				       Map<Point2D,AdjacentCPs> outerMap,
				       Point2D op)
    {
	AdjacentCPs data1 = innerMap.get(ip);
        double tangent1[] = {data1.scoords[0] - ip.getX(),
			     data1.scoords[1] - ip.getY()};
	// System.out.format("tangent1 = (%g, %g)\n", tangent1[0], tangent1[1]);

	AdjacentCPs data2 = innerMap.get(data1.prev);
	// System.out.println("data1.prev = " + data1.prev);
	// System.out.println("data2 = " + data2);
	int len = data2.scoords.length;
	double[] tangent2 = new double[2];
	if (len == 2) {
	    tangent2[0] = data1.prev.getX() - ip.getX();
	    tangent2[1] = data1.prev.getY() - ip.getY();
	} else {
	    tangent2[0] = data2.scoords[/*len-4*/ 0] - ip.getX();
	    tangent2[1] = data2.scoords[/*len-3*/ 1] - ip.getY();
	}
	// System.out.format("tangent2 = (%g, %g)\n", tangent2[0], tangent2[1]);

	double vector1[] = {op.getX() - ip.getX(),
			    op.getY() - ip.getY()};
	
	// System.out.format("vector1 = (%g, %g)\n", vector1[0], vector1[1]);

	AdjacentCPs data3 = outerMap.get(op);

	double vector2[] = {data3.next.getX() - ip.getX(),
			    data3.next.getY() - ip.getY()};


	double xprod1 = tangent1[0]*vector1[1] - tangent1[1]*vector1[0];
	double xprod2 = vector2[0]*tangent2[1] - vector2[1]*tangent2[0];
	// System.out.format("xprod1 = %g, xprod2 = %g\n", xprod1, xprod2);

	return xprod1 > 0 && xprod2 > 0;
    }


    private double[] getInnerSegment(Map<Point2D,AdjacentCPs> map,
				     Point2D start, Point2D vertex)
    {
	AdjacentCPs data = map.get(start);
	double[] tmp = map.get(data.prev).scoords;
	if (tmp.length == 2) {
	    return getTriangle(data.prev, start, vertex);
	} else {
	    return getCubicVertex(data.prev, tmp, vertex);
	}
    }

    private double[] getOuterSegment(Map<Point2D,AdjacentCPs> map,
				     Point2D start, Point2D vertex)
    {
	AdjacentCPs data = map.get(start);
	double[] tmp = data.scoords;
	if (tmp.length == 2) {
	    return getTriangle(start, data.next, vertex);
	} else {
	    return getCubicVertex(start, tmp, vertex);
	}
    }


    /*
    double scale = 0.0;
    private void setScale(double[] icoords, double[] ocoords) {
	for (double x: icoords) {
	    double av = Math.abs(x);
	    if (av > scale) scale = av;
	}
	for (double x: ocoords) {
	    double av = Math.abs(x);
	    if (av > scale) scale = av;
	}
	if (scale == 0.0) scale = 1.0;
    }
    */


    private double[] getCubicVertex(Point2D start, double[] pcoords,
				    Point2D vertex)
    {
	double[] coords = new double[15];
	switch(pcoords.length) {
	case 2:
	    {
		double[] tmp1 = new double[4];
		Path2DInfo.elevateDegree(1, tmp1, 0, pcoords, 0);
		Path2DInfo.elevateDegree(2, coords, 0, tmp1, 0);
	    }
	    break;
	case 4:
	    {
		Path2DInfo.elevateDegree(2, coords, 0, pcoords, 0);
	    }
	    break;
	case 6:
	    System.arraycopy(pcoords, 0, coords, 0, 6);
	default:
	    break;
	}
	
	System.arraycopy(coords, 4, coords, 9, 2);
	System.arraycopy(coords, 2, coords, 6, 2);
	System.arraycopy(coords, 0, coords, 3, 2);
	coords[2] = 0.0;
	coords[5] = 0.0;
	coords[0] = start.getX();
	coords[1] = start.getY();
	coords[12] = vertex.getX();
	coords[13] = vertex.getY();
	/*
	for (int i = 0; i < coords.length; i++) {
	    if (Math.abs(coords[i])/scale < 1.e-10) coords[i] = 0.0;
	    coords[i] = (double)(float)coords[i];
	}
	*/
	if (graph != null) {
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(1.0F));
	    Line2D line = new Line2D.Double(coords[0], coords[1],
					    coords[9], coords[10]);
	    graph.draw(g2d, line);
	    line = new Line2D.Double(coords[9], coords[10],
				     coords[12], coords[13]);
	    graph.draw(g2d, line);
	    line = new Line2D.Double(coords[12], coords[13],
				     coords[0], coords[1]);
	    graph.draw(g2d, line);
	    g2d.dispose();
	}
	return coords;
    }

    private double[] getTriangle(Point2D pstart, Point2D pend, Point2D ip) {
	double[] coords = new double[9];
	coords[0] = pstart.getX();
	coords[1] = pstart.getY();
	coords[3] = pend.getX();
	coords[4] = pend.getY();
	coords[6] = ip.getX();
	coords[7] = ip.getY();
	if (graph != null) {
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(Color.BLACK);
	    g2d.setStroke(new BasicStroke(1.0F));
	    Line2D line = new Line2D.Double(coords[0], coords[1],
					    coords[3], coords[4]);
	    graph.draw(g2d, line);
	    line = new Line2D.Double(coords[3], coords[4],
				     coords[6], coords[7]);
	    graph.draw(g2d, line);
	    line = new Line2D.Double(coords[6], coords[7],
				     coords[0], coords[1]);
	    graph.draw(g2d, line);
	    g2d.dispose();
	}

	/*
	for (int i = 0; i < coords.length; i++) {
	    if (Math.abs(coords[i])/scale < 1.e-10) coords[i] = 0.0;
	    coords[i] = (double)(float)coords[i];
	}
	*/
	return coords;
    }


    // precondition: isVisibleFromOUter and isVisibleFromInnter
    // both returned true.
    private boolean shouldAdvanceInner(Map<Point2D,AdjacentCPs> outerMap,
				       Point2D op,
				       Map<Point2D,AdjacentCPs> innerMap,
					Point2D ip)
    {
	AdjacentCPs odata = outerMap.get(op);
	AdjacentCPs idata = innerMap.get(ip);

	Point2D nop = odata.next;
	Point2D nip = idata.prev;

	double vector1[] = {ip.getX() - op.getX(), ip.getY() - op.getY()};
	double vector2[] = {nip.getX() -op.getX(), nip.getY() - op.getY()};
	double vector3[] = {ip.getX() - nop.getX(), ip.getY() - nop.getY()};
	double vector4[] = {nip.getX() - nop.getX(), nip.getY() - nop.getY()};

	VectorOps.normalize(vector1);
	VectorOps.normalize(vector2);
	VectorOps.normalize(vector3);
	VectorOps.normalize(vector4);

	double xprod1 = vector2[0]*vector1[1] - vector2[1]*vector1[0];
	double xprod2 = vector4[0]*vector3[1] - vector4[1]*vector3[0];

	xprod1 = Math.abs(xprod1);
	xprod2 = Math.abs(xprod2);
	return xprod2 <= xprod1;

    }


    private int types[] = null;
    private double[] coords = null;
    private Color color = null;
    private Object tag = null;

    /**
     * Set this object's tag.
     */
    public void setTag(Object tag) {
	this.tag = tag;
    }

    /**
     * Get this object's tag.
     */
    public Object getTag() {
	return tag;
    }

    private static Graph graph = null;
    private static Graph.Symbol circ = null;
    private static Graph.Symbol square =  null;
    private static File graphFile = null;
    /**
     * Set up a graph for debugging.
     * The graph should be square in shape, and will be written
     * when the next constructor is called, after which the class
     * will reset itself so that no additional graphs will be outputted until
     * this method is called again.
     * <P>
     * The graph shows the following:
     * <UL>
     *  <LI> the 'knots' for the outer path are shown as a set of
     *       square symbols.
     *  <LI> the 'knots' for the inner path are shown  as a set of
     *       round symbols.
     *  <LI> a series of triangles are drawn showing the surface segments
     *       created. This is a simplified representation: B&eacute;zier
     *       path segments are  represented as straight lines connecting the
     *       end points.
     *  <LI> if an exception is thrown while the segments are being generated,
     *       red lines are drawn connecting the center point to the
     *       current inner and outer points, indicating where the
     *       constructor threw an exception.
     *  <LI> the region in which angles from the center point are negative
     *       are shown in a pale yellow.  Angles increase in the
     *       counterclockwise direction.  Note that the graph is sometimes
     *       rotated to make the displayed image as large as feasible.
     * </UL>
     * The control points shown are those in the 2D space on which the
     * actual paths were projected using an implementation-specific
     * algorithm.  If the graph's height and width are not identical,
     * the graph may be rotated by 90 degrees if that would result in a
     * larger image.
     * <P>
     * This method may be useful when trying to identify problems with
     * the paths such as an insufficient number of path segments.
     * @param graph the graph
     * @param file the output file for the graph
     */
    public static void setupDebuggingGraph(Graph graph, File file) {
	ConvexPathConnector.graph = graph;
	graphFile = file;
    }

    private void init(Path2D inner, Path2D outer,
		      HashMap<Point3D,double[]> cpmap)
	throws IllegalArgumentException
    {
	if (inner == null) {
	    throw new NullPointerException(errorMsg("innerPathNull"));
	}
	if (outer == null) {
	    throw new NullPointerException(errorMsg("outerPathNull"));
	}
	// we need a clockwise inner path and a counterclockwise outer path
	inner = Path2DInfo.isCounterclockwise(inner)?
	    Paths2D.reverse(inner): inner;
	outer = Path2DInfo.isClockwise(outer)? Paths2D.reverse(outer): outer;

	Point2D center = Path2DInfo.centerOfMassOf(inner);
	double cx = center.getX();
	double cy = center.getY();

	if (graph != null) {
	    double[] icpts =
		Path2DInfo.getControlPoints(inner.getPathIterator(null), false);
	    double[] ocpts =
		Path2DInfo.getControlPoints(outer.getPathIterator(null), false);
	    double max = 0.0;
	    double maxx = 0.0;
	    double maxy = 0.0;
	    for (int i = 0; i < icpts.length; i++) {
		if ((i%2) == 0) {
		    double tst = Math.abs(icpts[i] - cx);
		    if (tst > maxx) maxx = tst;
		} else {
		    double tst = Math.abs(icpts[i] - cy);
		    if (tst > maxy) maxy = tst;
		}
	    }
	    for (int i = 0; i < ocpts.length; i++) {
		if ((i%2) == 0) {
		    double tst = Math.abs(ocpts[i] - cx);
		    if (tst > maxx) maxx = tst;
		} else {
		    double tst = Math.abs(ocpts[i] - cy);
		    if (tst > maxy) maxy = tst;
		}
	    }
	    graph.setOffsets(50, 50);
	    double gw = graph.getWidth() - 100;
	    double gh = graph.getHeight() - 100;
	    double scalefx = gw / maxx;
	    double scalefy = gh /maxy;
	    double scalef = Math.min(scalefx, scalefy);

	    double scalefx2 = gh / maxx;
	    double scalefy2 = gw /maxy;
	    double scalef2 = Math.min(scalefx2, scalefy2);
	    boolean rot = (scalef < scalef2);
	    if (rot) {
		scalef = scalef2;
	    }
	    scalef /= 2;	// because we are going from the center
	    graph.setRanges(cx, cy, 0.5, 0.5, scalef, scalef);
	    if (rot) {
		graph.setRotation(-Math.PI/2, cx, cy);
	    }
	    graph.setBackgroundColor(Color.WHITE);
	    graph.clear();
	    Graphics2D g2d = graph.createGraphics();
	    g2d.setColor(new Color(255, 255, 225));
	    Rectangle2D rectangle = new Rectangle2D.Double(cx-maxx, cy-maxy,
							   2*maxx, maxy);
	    graph.fill(g2d, rectangle);
	    g2d.dispose();
	    if (circ == null || square == null) {
		Graph.SymbolFactory sf = new Graph.SymbolFactory();
		circ = sf.newSymbol("SolidCircle");
		square =  sf.newSymbol("SolidSquare");
	    }
	    for (int i = 0; i < icpts.length; i += 2) {
		graph.draw(circ, icpts[i], icpts[i+1]);
	    }
	    for (int i = 0; i < ocpts.length; i += 2) {
		graph.draw(square, ocpts[i], ocpts[i+1]);
	    }
	}

	double[] icoords = getControlPoints(inner);
	Path2D ihull = Path2DInfo.convexHull(icoords, 0, icoords.length/2);
	Set<Point2D>iknots = getKnots(inner, cx, cy);

	if (Path2DInfo.isClosed(inner) == false) {
	    throw new IllegalArgumentException(errorMsg("innerNotClosed"));
	}
	if (Path2DInfo.isClosed(outer) == false) {
	    throw new IllegalArgumentException(errorMsg("outerNotClosed"));
	}

	if (PathSplitter.split(inner).length > 1) {
	    throw new IllegalArgumentException(errorMsg("innerNotContinuous"));
	}
	if (PathSplitter.split(outer).length > 1) {
	    throw new IllegalArgumentException(errorMsg("outerNotContinuous"));
	}

	// Since the inner curve is convex, the convex hull is equal to
	// a path consisting of a polygon connecting the inner curve's
	// control points. We compute it to make sure it starts with
	// a knot and contains all control points. We also check path lengths:
	// the path length a path connecting control points with straight
	// lines should equal the path length of a convex hull.
	// Note: if multiple points on a convex hull lie along a straight
	// line, some of those points may not be included. as a results,
	// connecting the control points with straight lines to form a path
	// can produce a path with more control points than the computed
	// convex hull.
	Path2D itpath = Path2DInfo.controlPointPolygon(inner, true, null);
	double pathlength1 = Path2DInfo.pathLength(ihull);
	double pathlength2 = Path2DInfo.pathLength(itpath);
	if (Math.abs((pathlength1 - pathlength2)
		     /Math.max(pathlength1, pathlength2)) > 1.e-10) {
	    // If pathlength1 != pathlength 2, tpath must have backtracked
	    // and crossed itself at some point.
	    throw new IllegalArgumentException(errorMsg("innerNotConvex"));
	}

	Map<Point2D,AdjacentCPs> innerMap = createAdjacents(inner);
	Map<Point2D,AdjacentCPs> outerMap = createAdjacents(outer);

	double[] ocoords = getControlPoints(outer);
	Path2D ohull = Path2DInfo.convexHull(ocoords, 0, ocoords.length/2);
	Set<Point2D>oknots = getKnots(outer, cx, cy);
	Path2D otpath = Path2DInfo.controlPointPolygon(outer, true, null);
	double[] test = getControlPoints(otpath);
	// System.out.println("otpath control points:");

	// setScale(icoords, ocoords);
	
	pathlength1 = Path2DInfo.pathLength(ohull);
	pathlength2 = Path2DInfo.pathLength(otpath);
	if (Math.abs((pathlength1 - pathlength2)
		     /Math.max(pathlength1, pathlength2)) > 1.e-10) {
	    // If pathlength1 != pathlength 2, outer must have backtracked
	    // and crossed itself at some point.
	    throw new IllegalArgumentException(errorMsg("outerNotConvex"));
	}

	Map<Point2D,AdjacentCPs> map = createAdjacents(inner);


	Iterator<Point2D> iit = iknots.iterator();
	Iterator<Point2D> oit = oknots.iterator();

	if (!iit.hasNext() ) {
	    throw new IllegalArgumentException(errorMsg("innerPathEmpty"));
	}
	if (!oit.hasNext()) {
	    throw new IllegalArgumentException(errorMsg("outerPathEmpty"));
	}
	boolean inext = true;
	boolean onext = true;

	Point2D ip = iit.next();
	Point2D op = oit.next();

	Point2D istart = ip;
	Point2D ostart = op;
	Point2D lastv = null;
	Point2D startv = null;
	double iangle = getAngle(ip, cx, cy);
	double oangle = getAngle(op, cx, cy);
	/*
	System.out.format("iangle = %g, oangle = %g\n",
			  Math.toDegrees(iangle),
			  Math.toDegrees(oangle));
	*/
	LinkedList<double[]> segments = new LinkedList<>();
	boolean first = true;
	while (inext && onext) {
	    if (ip == istart && op == ostart && !first) break;
	    first = false;
	    boolean isvInner = isVisibleFromInner(innerMap, ip,
						   outerMap, op);
	    boolean isvOuter = isVisibleFromOuter(innerMap, ip,
						   outerMap, op);
	    /*
	    System.out.format("iangle = %g, oangle = %g"
			      + ", isvInner = %b, isvOuter=%b\n",
			      Math.toDegrees(iangle),
			      Math.toDegrees(oangle),
			      isvInner, isvOuter);
	    */
	    if (isvInner && isvOuter) {
		boolean tst = shouldAdvanceInner(outerMap, op, innerMap, ip);
		if (/*iangle <= oangle*/ tst) {
		    segments.add(getInnerSegment(innerMap, ip, op));
		    lastv = op;
		    if (startv == null) startv = lastv;
		    boolean ihn = iit.hasNext();
		    inext = ihn || (ip != istart);
		    if (inext) {
			ip = ihn? iit.next(): istart;
			iangle = getAngle(ip, cx, cy);
		    }
		} else {
		    segments.add(getOuterSegment(outerMap, op, ip));
		    lastv = ip;
		    if (startv == null) startv = lastv;
		    boolean ohn = oit.hasNext();
		    onext = ohn || (op != ostart);;
		    if (onext) {
			op = ohn? oit.next(): ostart;
			oangle = getAngle(op, cx, cy);
		    }
		}
	    } else if (isvOuter) {
		segments.add(getInnerSegment(innerMap, ip, op));
		lastv = op;
		if (startv == null) startv = lastv;
		boolean ihn = iit.hasNext();
		inext = ihn || (ip != istart);;
		if (inext) {
		    ip = ihn? iit.next(): istart;
		    iangle = getAngle(ip, cx, cy);
		}
	    } else if (isvInner) {
		segments.add(getOuterSegment(outerMap, op, ip));
		lastv = ip;
		if (startv == null) startv = lastv;
		boolean ohn =oit.hasNext();
		onext = ohn || (op != ostart);
		if (onext) {
		    op = ohn? oit.next(): ostart;
		    oangle = getAngle(op, cx, cy);
		}
	    } else {
		double xi, yi, zi, xo, yo, zo;
		if (cpmap == null) {
		    xi = ip.getX();
		    yi = ip.getY();
		    xo = op.getX();
		    yo = op.getY();
		    zi = zo = 0.0;
		} else {
		    Point3D ip3 = new Point3D.Double(ip.getX(), ip.getY(), 0.0);
		    Point3D op3 = new Point3D.Double(op.getX(), op.getY(), 0.0);
		    double[] isegment = cpmap.get(ip3);
		    double[] osegment = cpmap.get(op3);
		    xi = isegment[0];
		    yi = isegment[1];
		    zi = isegment[2];
		    xo = osegment[0];
		    yo = osegment[1];
		    zo = osegment[2];
		}
		if (graph != null) {
		    Graphics2D g2d = graph.createGraphics();
		    g2d.setColor(Color.RED);
		    g2d.setStroke(new BasicStroke(1.5F));
		    Line2D line = new Line2D
			.Double(cx, cy, ip.getX(), ip.getY());
		    graph.draw(g2d, line);
		    line = new Line2D
			.Double(cx, cy, op.getX(), op.getY());
		    graph.draw(g2d, line);
		    g2d.dispose();
		    double[] icpts =
			Path2DInfo.getControlPoints(inner.getPathIterator(null),
						    false);
		    double[] ocpts =
			Path2DInfo.getControlPoints(outer.getPathIterator(null),
						    false);
		    for (int i = 0; i < icpts.length; i += 2) {
			graph.draw(circ, icpts[i], icpts[i+1]);
		    }
		    for (int i = 0; i < ocpts.length; i += 2) {
			graph.draw(square, ocpts[i], ocpts[i+1]);
		    }
		    try {
			graph.write("png", graphFile);
		    } catch (IOException e) {
			String fname = graphFile.getName();
			System.err.println(errorMsg("graphWrite", fname));
		    } finally {
			graphFile = null;
			graph = null;
		    }
		}
		String msg = errorMsg("vertexVisibility", xi,yi,zi, xo,yo,zo);
		throw new IllegalArgumentException(msg);
	    }
	}
	/*
	System.out.format("exited 1st loop: iangle=%g, oangle=%g\n",
			  Math.toDegrees(iangle), Math.toDegrees(oangle));
	System.out.format("inext = %b, onext = %b\n", inext, onext);

	for (double[] array: segments) {
	    switch(array.length) {
	    case 9:
		System.out.format("(%g,%g)->(%g,%g)->%g,%g)\n",
				  array[0], array[1],
				  array[3], array[4],
				  array[6], array[7]);
		break;
	    case 15:
		System.out.format("(%g,%g)->...->(%g,%g)->%g,%g)\n",
				  array[0], array[1],
				  array[9], array[10],
				  array[12], array[13]);
		break;
	    }
	}
	*/
	types = new int[segments.size()];
	int i = 0;
	int len = 0;
	for (double[] segment: segments) {
	    len += segment.length;
	    types[i++] = (segment.length == 9)?
		SurfaceIterator.PLANAR_TRIANGLE:
		SurfaceIterator.CUBIC_VERTEX;
	}
	coords = new double[len];
	i = 0;
	for (double[] segment: segments) {
	    if (cpmap != null) {
		for (int j = 0; j < segment.length; j += 3) {
		    Point3D pt = new Point3D
			.Double(segment[j], segment[j+1], segment[j+2]);
		    double[] subsegment = cpmap.get(pt);
		    segment[j] = subsegment[0];
		    segment[j+1] = subsegment[1];
		    segment[j+2] = subsegment[2];
		}
	    }
	    System.arraycopy(segment, 0, coords, i, segment.length);
	    i += segment.length;
	}
	if (graph != null) {
	    try {
		graph.write("png", graphFile);
	    } catch (IOException e) {
		String fname = graphFile.getName();
		System.err.println(errorMsg("graphWrite", fname));
	    } finally {
		graphFile = null;
		graph = null;
	    }
	}
    }

    /**
     * Constructor using 2D paths.
     * The paths must be convex and closed. The surface produced will
     * be oriented so that its normal vector points in the Z direction
     * regardless of whether the paths are clockwise or counterclockwise.
     * @param inner the inner of the two paths
     * @param outer the outer of the two paths
     * @exception IllegalArgumentException if the planar projections of
     *            the paths are not convex paths, if the paths were empty,
     *            if the paths are not continuous, if the paths are not
     *            closed, or if a vertex does not have a peer vertex that
     *            is visible to it (e.g., a path does not have enough
     *            control points)
     * @exception NullPointerException if the inner or outer paths were null
     */
    public ConvexPathConnector(Path2D inner, Path2D outer)
	throws IllegalArgumentException, NullPointerException
    {
	init(inner, outer, null);
    }


    /**
     * Constructor using 3D paths.

     * The paths must be convex and closed.  The orientation is that
     * obtained by using the right-hand rule when traversing the outer
     * path from its end to its start (the reverse of the expected
     * case where the path is traversed from its start to its end, but
     * appropriate when the path is the boundary of a hole in a
     * surface). The inner path will be reversed if necessary
     * automatically. To determine if a curve is convex, it is first
     * projected onto a plane and the test is performed on the
     * projection.
     * @param inner the inner of the two paths
     * @param outer the outer of the two paths
     * @exception IllegalArgumentException if the planar projections of
     *            the paths are not convex paths, if the paths were empty,
     *            if the paths are not continuous, if the paths are not
     *            closed, or if a vertex does not have a peer vertex that
     *            is visible to it (e.g., a path does not have enough
     *            control points)
     * @exception NullPointerException if the inner or outer paths were null
     */
    public ConvexPathConnector(Path3D inner, Path3D outer)
	throws IllegalArgumentException, NullPointerException
    {
	this(inner, outer, false);
    }

    /**
     * Constructor using 3D paths.

     * The paths must be convex and closed.  The orientation is that
     * obtained by using the right-hand rule when traversing the outer
     * path from its end to its start (the reverse of the expected
     * case where the path is traversed from its start to its end, but
     * appropriate when the path is the boundary of a hole in a
     * surface). The inner path will be reversed if necessary
     * automatically. To determine if a curve is convex, it is first
     * projected onto a plane and the test is performed on the
     * projection.
     * <P>
     * The last argument should be false if the path is a boundary
     * associated with a hole in a surface.
     * @param inner the inner of the two paths
     * @param outer the outer of the two paths
     * @param counterclockwise true if the orientation is determined by
     *        using the right hand rule when traversing the outer path;
     *        false otherwise
     * @exception IllegalArgumentException if the planar projections of
     *            the paths are not convex paths, if the paths were empty,
     *            if the paths are not continuous, if the paths are not
     *            closed, or if a vertex does not have a peer vertex that
     *            is visible to it (e.g., a path does not have enough
     *            control points)
     * @exception NullPointerException if the inner or outer paths were null
     */
    public ConvexPathConnector(Path3D inner, Path3D outer,
			       boolean counterclockwise)
	throws IllegalArgumentException, NullPointerException
    {
	if (inner == null) {
	    throw new NullPointerException(errorMsg("innerPathNull"));
	}
	if (outer == null) {
	    throw new NullPointerException(errorMsg("outerPathNull"));
	}
	// make sure the paths are closed
	/*
	  // moved to init(...) so tests not done twice.
	if (Path3DInfo.isClosed(inner) == false) {
	    throw new IllegalArgumentException(errorMsg("innerNotClosed"));
	}
	if (Path3DInfo.isClosed(outer) == false) {
	    throw new IllegalArgumentException(errorMsg("outerNotClosed"));
	}
	// make sure we have continuous paths
	if (PathSplitter.split(inner).length > 1) {
	    throw new IllegalArgumentException(errorMsg("innerNotContinuous"));
	}
	if (PathSplitter.split(outer).length > 1) {
	    System.out.println("PathSplitter.split(outer).length = "
`			       + PathSplitter.split(outer).length);
	    throw new IllegalArgumentException(errorMsg("outerNotContinuous"));
	}
	*/
	Point3D icenter = BezierCap.findCenter(inner);
	double[] ivector = BezierCap.findVector(inner, icenter);

	Point3D ocenter = BezierCap.findCenter(outer);
	double[] ovector = BezierCap.findVector(outer, ocenter);

	if (VectorOps.dotProduct(ivector, ovector) < 0.0) {
	    // case where orientations are opposite.
	    VectorOps.multiply(ivector, -1.0, ivector);
	}
	
	double xc = (icenter.getX() + ocenter.getX())/2;
	double yc = (icenter.getY() + ocenter.getY())/2;
	double zc = (icenter.getZ() + ocenter.getZ())/2;
	double[] vector =
	    VectorOps.multiply(0.5, VectorOps.add(ivector, ovector));
	// Bezier caps assume the outer curve is clockwise because it is
	// a boundary of a hole.
	if (counterclockwise) {
	    VectorOps.multiply(vector, -1.0, vector);
	}
	VectorOps.normalize(vector);
	for (int i = 0; i < 3; i++) {
	    // in case of round off errors or negative zeros
	    if (Math.abs(vector[i]) < 1.0e-13) vector[i] = 0.0;
	    if (Math.abs(vector[i]) > 1.0) vector[i] = 1.0;
	    if (Math.abs(vector[i]) < -1.0) vector[i] = -1.0;
	}
	int i = 0;
	double max = 0.0;
	for (int j = 0; j < 3; j++) {
	    double a = Math.abs(vector[j]);
	    if (a > max) {
		i = j;
		max = a;
	    }
	}
	// double[] xdir = null;
	double[] ydir = null;
	switch (i) {
	case 0:
	    // xdir = new double[] {0.0, 1.0, 0.0};
	    ydir = new double[] {0.0, 0.0, 1.0};
	    break;
	case 1:
	    // xdir = new double[] {0.0, 0.0, 1.0};
	    ydir = new double[] {1.0, 0.0, 0.0};
	    break;
	case 2:
	    // xdir = new double[] {1.0, 0.0, 0.0};
	    ydir = new double[] {0.0, 1.0, 0.0};
	    break;
	}
	double[] xaxis = VectorOps.crossProduct(ydir, vector);
	VectorOps.normalize(xaxis);
	double[] yaxis = VectorOps.crossProduct(vector, xaxis);
	VectorOps.normalize(yaxis);
	/*
	System.out.format("xaxis = (%g, %g, %g)\n",
			  xaxis[0], xaxis[1], xaxis[2]);
	System.out.format("yaxis = (%g, %g, %g)\n",
			  yaxis[0], yaxis[1], yaxis[2]);
	System.out.format("zaxis = (%g, %g, %g)\n",
			  vector[0], vector[1], vector[2]);
	*/
	double[] zaxis = vector;
	Path2D inner2d = new Path2D.Double();
	PathIterator3D pit = inner.getPathIterator(null);
	double[] coords = new double[9];
	double[] value;
	double x1, y1, x2, y2, x3, y3;
	Point3D pt;
	HashMap<Point3D,double[]> cpmap = new HashMap<>(256);
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		inner2d.moveTo(x1, y1);
		break;
	    case PathIterator3D.SEG_LINETO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		inner2d.lineTo(x1, y1);
		break;
	    case PathIterator3D.SEG_QUADTO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x2 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y2 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x2, y2, 0.0);
		cpmap.put(pt, value);
		inner2d.quadTo(x1, y1, x2, y2);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x2 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y2 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x2, y2, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x3 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y3 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x3, y3, 0.0);
		cpmap.put(pt, value);
		inner2d.curveTo(x1, y1, x2, y2, x3, y3);
		break;
	    case PathIterator3D.SEG_CLOSE:
		inner2d.closePath();
		break;
	    }
	    pit.next();
	}

	Path2D outer2d = new Path2D.Double();
	pit = outer.getPathIterator(null);
	while (!pit.isDone()) {
	    switch(pit.currentSegment(coords)) {
	    case PathIterator3D.SEG_MOVETO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		outer2d.moveTo(x1, y1);
		break;
	    case PathIterator3D.SEG_LINETO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		outer2d.lineTo(x1, y1);
		break;
	    case PathIterator3D.SEG_QUADTO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x2 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y2 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x2, y2, 0.0);
		cpmap.put(pt, value);
		outer2d.quadTo(x1, y1, x2, y2);
		break;
	    case PathIterator3D.SEG_CUBICTO:
		i = 0;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x1 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y1 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x1, y1, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x2 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y2 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x2, y2, 0.0);
		cpmap.put(pt, value);
		i += 3;
		value = new double[3];
		System.arraycopy(coords, i, value, 0, 3);
		x3 = VectorOps.dotProduct(xaxis, 0, coords, i, 3);
		y3 = VectorOps.dotProduct(yaxis, 0, coords, i, 3);
		pt = new Point3D.Double(x3, y3, 0.0);
		cpmap.put(pt, value);
		outer2d.curveTo(x1, y1, x2, y2, x3, y3);
		break;
	    case PathIterator3D.SEG_CLOSE:
		outer2d.closePath();
		break;
	    }
	    pit.next();
	}
	init(inner2d, outer2d, cpmap);
    }

    private boolean flipped = false;

    /**
     * Change the orientation of the B&eacute;zier triangles associated
     * with this object.
     * This method affects the orientation of triangles provided by iterators.
     * It does not change the values returned by calling methods
     * such as {@link #print()}.
     * @param reverse true if the orientation is the reverse of
     *        the one that was initially defined; false if the orientation is
     *        the same as the one that was initially defined.
     */
    public void reverseOrientation(boolean reverse) {
	flipped = reverse;
    }

    /**
     * Invert the orientation from its current value.
     */
    public void flip() {
	flipped = !flipped;
    }

    /**
     * Determine if the orientation for this grid is reversed.
     * @return true if the orientation is reversed; false if not
     */
    public boolean isReversed() {return flipped;}

    /**
     * Set the  color for this shape.
     */
    public void setColor(Color c) {
	color = c;
    }


    /**
     * Get the color color for this shape.
     * This is the color for the shape, excluding B&eacute;zier triangles
     * for which an explicit color has been specified.
     * @return the color; null if it is not defined
     */
    public Color getColor() {
	return color;
    }


    /**
     * Print this object's control points.
     * Planar triangles are printed with the vertices in the order used
     * in barycentric coordinates: v1 followed by v3 followed by v2, where
     * the sequence v1 followed by v2 followed by v3 gives the orientation
     * using the right-and rule.
     */
    public void print() throws IOException {
	print(System.out);
    }

    /**
     * Print this object's control points, specifying an output.
     * Planar triangles are printed with the vertices in the order used
     * in barycentric coordinates: v1 followed by v3 followed by v2, where
     * the sequence v1 followed by v2 followed by v3 gives the orientation
     * using the right-and rule.
     * @param out the output
     */
    public void print(Appendable out) throws IOException {
	print("", out);
    }

    /**
     * Print this object's control points, specifying a prefix.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * <P>
     * Planar triangles are printed with the vertices in the order used
     * in barycentric coordinates: v1 followed by v3 followed by v2, where
     * the sequence v1 followed by v2 followed by v3 gives the orientation
     * using the right-and rule.
     * @param prefix the prefix
     */
    public void print(String prefix) throws IOException {
	print(prefix, System.out);
    }

    /**
     * Print this object's control points, specifying a prefix and output.
     * Each line will start with the prefix (typically some number
     * of spaces).
     * <P>
     * Planar triangles are printed with the vertices in the order used
     * in barycentric coordinates: v1 followed by v3 followed by v2, where
     * the sequence v1 followed by v2 followed by v3 gives the orientation
     * using the right-and rule.
     * @param prefix the prefix
     * @param out the output
     */
    public void print(String prefix, Appendable out) throws IOException {
	int len = types.length;
	out.append(String.format("%snumber of segments: %d\n", prefix, len));
	int offset = 0;
	for (int i = 0; i < len; i++) {
	    int type = types[i];
	    int slen = (type == SurfaceIterator.PLANAR_TRIANGLE)? 9: 15;
	    
	    if (slen == 9) {
		out.append(String.format("%ssegment %d (planar triangle):\n",
					 prefix, i));
		out.append(String.format("%s    vertices (barycentric order):"
					 + "\n",
					 prefix));
		for(int k = 0; k < 9; k += 3) {
		    out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					     coords[offset+k],
					     coords[offset+k+1],
					     coords[offset+k+2]));
		}
	    } else {
		out.append(String.format("%ssegment %d (cubic vertex):\n",
					 prefix, i));
		out.append(String.format("%s    cubic-curve control points:\n",
					 prefix));
		for(int k = 0; k < 12; k += 3) {
		    out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					     coords[offset+k],
					     coords[offset+k+1],
					     coords[offset+k+2]));
		}
		out.append(String.format("%s    vertex:\n", prefix));
		out.append(String.format("%s        (%g, %g, %g)\n", prefix,
					 coords[offset+12],
					 coords[offset+13],
					 coords[offset+14]));
	    }
	    offset += slen;
	}
    }

    // Shape3D methods

    private class Iterator1 implements SurfaceIterator {
	boolean flip = flipped;
	int index = 0;
	int offset = 0;
	boolean done = false;
	double[] cpoints = ConvexPathConnector.this.coords;
	Iterator1() {
	}

	@Override
	public Color currentColor() {
	    return color;
	}

	@Override
	public int currentSegment(double[] coords) {
	    if (index < types.length) {
		switch (types[index]) {
		case SurfaceIterator.CUBIC_VERTEX:
		    if (flip) {
			System.arraycopy(cpoints, offset, coords, 9, 3);
			System.arraycopy(cpoints, offset+3, coords, 6, 3);
			System.arraycopy(cpoints, offset+6, coords, 3, 3);
			System.arraycopy(cpoints, offset+9, coords, 0, 3);
			System.arraycopy(cpoints, offset+12, coords, 12, 3);
		    } else {
			System.arraycopy(cpoints, offset, coords, 0, 15);
		    }
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    if (flip) {
			System.arraycopy(cpoints, offset, coords, 0, 9);
		    } else {
			// because of barycentric convention in Surface3D
			System.arraycopy(cpoints, offset, coords, 0, 3);
			System.arraycopy(cpoints, offset+3, coords, 6, 3);
			System.arraycopy(cpoints, offset+6, coords, 3, 3);
		    }
		}
		return types[index];
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}
	
	double[] tcoords = null;

	@Override
	public int currentSegment(float[] coords) {
	    if (index < types.length) {
		if (tcoords == null) tcoords = new double[15];
		currentSegment(tcoords);
		for (int k = 0; k < 15; k++) {
		    coords[k] = (float)tcoords[k];
		}
		return types[index];
	    } else {
		return types[types.length-1];
	    }
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return index >= types.length;
	}

	@Override
	public void next() {
	    if (index < types.length) {
		offset += (types[index] == SurfaceIterator.CUBIC_VERTEX)? 15: 9;
		index++;
	    }
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }

    private class Iterator2 implements SurfaceIterator {
	boolean flip = flipped;
	int index = 0;
	int offset = 0;
	boolean done = false;
	double[] cpoints = ConvexPathConnector.this.coords;
	/*
	 * tform will never be null because getSurfaceIterator
	 * will create an instance of Iterator1 if the transform is null.
	 */
	Transform3D tform;

	Iterator2(Transform3D tform) {
	    this.tform = tform;
	}

	@Override
	public Color currentColor() {
	    return color;
	}

	double[] tcoords = null;

	@Override
	public int currentSegment(double[] coords) {
	    if (flip && tcoords == null) tcoords = new double[15];
	    int offset = index*15;
	    if (offset < cpoints.length) {
		switch(types[index]) {
		case SurfaceIterator.CUBIC_VERTEX:
		    if (flip) {
			System.arraycopy(cpoints, offset, tcoords, 9, 3);
			System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
			System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
			System.arraycopy(cpoints, offset+9, tcoords, 0, 3);
			System.arraycopy(cpoints, offset+12, tcoords, 12, 3);
			tform.transform(tcoords, 0, coords, 0, 5);
		    } else {
			tform.transform(cpoints, offset, coords, 0, 5);
		    }
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    if (flip) {
			System.arraycopy(cpoints, offset, tcoords, 0, 3);
			System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
			System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
			tform.transform(tcoords, 0, coords, 0, 3);
		    } else {
			tform.transform(cpoints, offset, coords, 0, 3);
		    }
		    break;
		}
	    }
	    return SurfaceIterator.CUBIC_VERTEX;
	}
	
	@Override
	public int currentSegment(float[] coords) {
	    if (flip && tcoords == null) tcoords = new double[15];
	    if (offset < cpoints.length) {
		switch (types[index]) {
		case SurfaceIterator.CUBIC_VERTEX:
		    if (flip) {
			System.arraycopy(cpoints, offset, tcoords, 9, 3);
			System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
			System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
			System.arraycopy(cpoints, offset+9, tcoords, 0, 3);
			System.arraycopy(cpoints, offset+12, tcoords, 12, 3);
			tform.transform(tcoords, 0, coords, 0, 5);
		    } else {
			tform.transform(cpoints, offset, coords, 0, 5);
		    }
		    break;
		case SurfaceIterator.PLANAR_TRIANGLE:
		    if (flip) {
			System.arraycopy(cpoints, offset, tcoords, 0, 3);
			System.arraycopy(cpoints, offset+3, tcoords, 6, 3);
			System.arraycopy(cpoints, offset+6, tcoords, 3, 3);
			tform.transform(tcoords, 0, coords, 0, 3);
		    } else {
			tform.transform(cpoints, offset, coords, 0, 3);
		    }
		}
	    }
	    return types[index];
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public boolean isDone() {
	    return index >= types.length;
	}

	@Override
	public void next() {
	    if (index < types.length) {
		offset += (types[index] == SurfaceIterator.CUBIC_VERTEX)? 15: 9;
		index++;
	    }
	}

	@Override
	public boolean isOriented() {
	    return true;
	}
    }
    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	if (tform == null) {
	    return new Iterator1();
	} else {
	    return new Iterator2(tform);
	}
    }

    @Override
    public final synchronized
    SurfaceIterator getSurfaceIterator(Transform3D tform, int level) {
	if (tform == null) {
	    if (level == 0) {
		return new Iterator1();
	    } else {
		return new SubdivisionIterator(new Iterator1(), level);
	    }
	} else {
	    if (level == 0) {
		return new Iterator2(tform);
	    } else {
		return new SubdivisionIterator(new Iterator1(), tform, level);
	    }
	}
    }

    Rectangle3D brect = null;

    boolean bpathSet = false;
    Path3D bpath = null;

    @Override
    public Path3D getBoundary() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return bpath;
    }

    /**
     * Determine if this BezierVertex is well formed.
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed() {
	if (!bpathSet) {
	    Surface3D.Boundary boundary
		= new Surface3D.Boundary(getSurfaceIterator(null));
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (bpath != null);
    }

    /**
     * Determine if this {@link ConvexPathConnector} is well formed,
     * logging error messages to an Appendable.
     * @param out an Appendable for logging error messages
     * @return true if the grid is well formed; false otherwise
     */
    public boolean isWellFormed(Appendable out) {
	Surface3D.Boundary boundary
	    = new Surface3D.Boundary(getSurfaceIterator(null), out);
	if (!bpathSet) { 
	    bpath = boundary.getPath();
	    bpathSet = true;
	}
	return (boundary.getPath() != null);
    }


    @Override
    public  Shape3D getComponent(int i)
	throws IllegalArgumentException
    {
	if (i < 0 || i >= 1) {
	    String msg = errorMsg("illegalComponentIndex", i, 1);
	    throw new IllegalArgumentException(msg);
	}
	return this;
    }

    @Override
    public Rectangle3D getBounds() {
	if (brect == null) {
	    double[] coords = new double[15];
	    SurfaceIterator sit = getSurfaceIterator(null);
	    while (!sit.isDone()) {
		int len = (sit.currentSegment(coords) ==
			   SurfaceIterator.PLANAR_TRIANGLE)? 9: 15;
		if (brect == null) {
		    brect = new
			Rectangle3D.Double(coords[0], coords[1], coords[2],
					   0.0, 0.0, 0.0);
		    for (int i = 3; i < len; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		} else {
		    for (int i = 0; i < len; i += 3) {
			brect.add(coords[i+0], coords[i+1], coords[i+2]);
		    }
		}
		sit.next();
	    }
	}
	return brect;
    }


    @Override
    public boolean isClosedManifold() {
	return false;
    }
    
    @Override
    public boolean isOriented() {return true;}

    @Override    
    public int numberOfComponents() {
	return types.length;
    }
}

//  LocalWords:  eacute zier Subclasses ip prev xprod setScale coords
//  LocalWords:  icoords ocoords pathlength tpath otpath iangle inext
//  LocalWords:  oangle toDegrees isvInner isvOuter onext Bezier len
//  LocalWords:  xaxis yaxis zaxis barycentric snumber ssegment tform
//  LocalWords:  getSurfaceIterator BezierVertex ConvexPathConnector
//  LocalWords:  Appendable exbundle isVisibleFromOUter innerPathNull
//  LocalWords:  isVisibleFromInnter outerPathNull SolidCircle png
//  LocalWords:  SolidSquare innerNotClosed outerNotClosed graphWrite
//  LocalWords:  innerNotContinuous outerNotContinuous innerNotConvex
//  LocalWords:  outerNotConvex innerPathEmpty outerPathEmpty init
//  LocalWords:  vertexVisibility IllegalArgumentException DInfo
//  LocalWords:  NullPointerException isClosed errorMsg
//  LocalWords:  illegalComponentIndex
