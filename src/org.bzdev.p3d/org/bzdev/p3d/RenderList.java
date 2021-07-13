package org.bzdev.p3d;
//@exbundle org.bzdev.p3d.lpack.P3d

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Line2D;

class RenderListElement {
    PolyLine p;
    boolean edgeMask[];		// Only some edges shown in edge color
    double z;
    double zmin;
    double nz;
    Color c;
    Object tag;

    float x1;
    float y1;
    float x2;
    float y2;

    RenderListElement(PolyLine p, Color c, double z, double zmin, double nz,
		      float x1, float y1, float x2, float y2) {
	this(p, c, z, zmin, nz, x1, y1, x2, y2, null, null);
    }

    RenderListElement(PolyLine p, Color c, double z, double zmin,
		      double nz, float x1, float y1, float x2, float y2,
		      Object tag)
    {
	this(p, c, z, zmin, nz, x1, y1, x2, y2, tag, null);
    }
    RenderListElement(PolyLine p, Color c, double z, double zmin,
		      double nz, float x1, float y1, float x2, float y2,
		      Object tag, boolean edgeMask[])
    {
	this.p = p;
	this.c = c;
	this.z = z;
	this.zmin = zmin;
	this.nz = nz;
	this.x1 = x1;
	this.y1 = y1;
	this.x2 = x2;
	this.y2 = y2;
	this.tag = tag;		// for instrumentation
	this.edgeMask = edgeMask;
    }
}

class RenderList {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
    }

    java.util.List<RenderListElement> list =
	new java.util.LinkedList<RenderListElement>();
    void add(PolyLine p, Color c, double z, double zmin, double nz,
	     float x1, float y1, float x2, float y2) {
	list.add(new RenderListElement(p, c, z, zmin, nz, x1, y1, x2, y2));
	if (nz > 0.0) {
	    if (z > max) max = z;
	    if (zmin < min) min = zmin;
	}
    }

    void add(PolyLine p, Color c, double z, double zmin, double nz,
	     float x1, float y1, float x2, float y2,
	     boolean[] edgeMask) {
	list.add(new RenderListElement(p, c, z, zmin, nz, x1, y1, x2, y2,
				       null, edgeMask));
	if (z > max) max = z;
	if (zmin < min) min = zmin;
    }

    void add(PolyLine p, Color c, double z, double zmin, double nz,
	     float x1, float y1, float x2, float y2,
	     Object tag ) {
	list.add(new RenderListElement(p, c, z, zmin, nz, x1, y1, x2, y2, tag));
	if (z > max) max = z;
	if (zmin < min) min = zmin;
    }

    void add(PolyLine p, Color c, double z, double zmin, double nz,
	     float x1, float y1, float x2, float y2,
	     Object tag, boolean[] edgeMask ) {
	list.add(new RenderListElement(p, c, z, zmin, nz, x1, y1, x2, y2,
				       tag, edgeMask));
	if (z > max) max = z;
	if (zmin < min) min = zmin;
    }

    void addAll(RenderList newList) {
	list.addAll(newList.list);
	for(RenderListElement e: newList.list) {
	    if (e.z > max) max = e.z;
	    if (e.zmin < min) min = e.zmin;
	}
    }


    void reset() {
	list.clear();
	min = Double.POSITIVE_INFINITY;
	max = Double.NEGATIVE_INFINITY;
    }

    static java.util.Comparator<RenderListElement> comparator =
	new java.util.Comparator<RenderListElement>() {
	float[] coords = new float[6];
	public int compare(RenderListElement e1, RenderListElement e2) {
	    if ((e1.p.npoints == 2) || (e2.p.npoints == 2)) {
		if (e1.zmin < e2.zmin) return -1;
		if (e1.zmin > e2.zmin) return 1;
		if (e1.p.npoints == 2) {
		    if (e1.p.npoints < e2.p.npoints) return 1;
		    else if (e1.p.npoints > e2.p.npoints) return -1;
		}
		if (e2.p.npoints == 2) {
		    if (e1.p.npoints < e2.p.npoints) return 1;
		    else if (e1.p.npoints > e2.p.npoints) return -1;
		}
		if (e1.z < e2.z) return -1;
		if (e1.z > e2.z) return 1;
		return 0;
	    } else {
		if (e1.z < e2.z) return -1;
		if (e1.z > e2.z) return 1;
	    }
	    if (e1.nz < e2.nz) return -1;
	    if (e1.nz > e2.nz) return 1;
	    return 0;
	}
    };
    // reverse direction
    static java.util.Comparator<RenderListElement> rcomparator =
	new java.util.Comparator<RenderListElement>() {
	float[] coords = new float[6];
	public int compare(RenderListElement e1, RenderListElement e2) {
	    if ((e1.p.npoints == 2) || (e2.p.npoints == 2)) {
		if (e1.zmin < e2.zmin) return 1;
		if (e1.zmin > e2.zmin) return -1;
		if (e1.p.npoints == 2) {
			if (e1.p.npoints < e2.p.npoints) return -1;
			else if (e1.p.npoints > e2.p.npoints) return 1;
		}
		if (e2.p.npoints == 2) {
		    if (e1.p.npoints < e2.p.npoints) return -1;
		    else if (e1.p.npoints > e2.p.npoints) return 1;
		}
		if (e1.z < e2.z) return 1;
		if (e1.z > e2.z) return -1;
		return 0;
	    } else {
		if (e1.z < e2.z) return 1;
		if (e1.z > e2.z) return -1;
	    }
	    if (e1.nz < e2.nz) return 1;
	    if (e1.nz > e2.nz) return -1;
	    return 0;
	}
    };

    static float components[] = new float[3];
    static Color scaleColor(Color c, double z, double max, double min,
			    double colorFactor,
			    double nz, double normalFactor)
    {
	if (max == min) return c;
	c.getRGBColorComponents(components);
	if (colorFactor < 0.0)
	    throw new IllegalArgumentException
		(errorMsg("illegalColorFactor", colorFactor));
	if (normalFactor > 0.0) {
	    if (nz > 1.0) nz = 1.0;
	    colorFactor *= Math.exp(-(1.0-nz)/normalFactor);
	}
	double fraction = 1.0 - colorFactor * (max - z) / (max - min);
	if (fraction < 0.0) fraction = 0.0;
	if (fraction == 1.0) {
	    return c;
	} else {
	    for (int i = 0; i < 3; i++) {
		components[i] = (float)(fraction * components[i]);
	    }
	    return new Color(components[0], components[1], components[2]);
	}
    }

    void render(Graphics2D g) {
	render(g, null, 0.0, -1.0);
    }

    void render(Graphics2D g, Color edgeColor, double colorFactor,
		double normalFactor)
    {
	java.util.Collections.sort(list, comparator);
	Paint cc = g.getPaint();
	try {
	    for (RenderListElement e: list) {
		if (e.p.npoints == 2) {
		    // line segment;
		    /*
		    System.out.println ("drawing line segment ("
					+ e.p.xpoints[0] + ", " + e.p.ypoints[0]
					+ ")---("
					+ e.p.xpoints[1] + ", " + e.p.ypoints[1]
					+ "), " + e.tag);
		    */
		    g.setPaint(e.c);
		    g.draw(e.p);
		} else {
		    /*
		    System.out.println ("drawing triangle ("
					+ e.p.xpoints[0] + ", " + e.p.ypoints[0]
					+ ")---("
					+ e.p.xpoints[1] + ", " + e.p.ypoints[1]
					+ ")---("
					+ e.p.xpoints[2] + ", " + e.p.ypoints[2]
					+ "), " + e.tag);
		    */
		    if (colorFactor > 0.0) {
			Color c1 = scaleColor(e.c, e.z, max, min, colorFactor,
					      e.nz, normalFactor);
			Color c2 = scaleColor(e.c, e.zmin, max, min,
					      colorFactor,
					      e.nz, normalFactor);
			Paint p = new 
			    GradientPaint (e.x1, e.y1, c1, e.x2, e.y2, c2);
			g.setPaint(p);
		    } else {
			g.setPaint(e.c);
		    }
		    //g.draw(e.p);
		    g.fill(e.p);
		    if (edgeColor != null) {
			g.setPaint(edgeColor);
			if (e.edgeMask == null) {
			    g.draw(e.p);
			} else {
			    PathIterator pit = e.p.getPathIterator(null);
			    float coords[] = new float[6];
			    int segtype = pit.currentSegment(coords);
			    if (segtype != PathIterator.SEG_MOVETO) {
				throw new IllegalStateException
				    (errorMsg("badPath"));
			    }
			    float x0 = coords[0]; float y0 = coords[1];
			    float x1 = x0; float y1 = y0;
			    pit.next();
			    int i = 1;
			    while (!pit.isDone()) {
				segtype = pit.currentSegment(coords);
				if (segtype == PathIterator.SEG_CLOSE) break;
				float x2 = coords[0]; float y2 = coords[1];
				if (segtype == PathIterator.SEG_LINETO
				    && e.edgeMask[i]) {
				    Line2D.Float line = new
					Line2D.Float(x1, y1, x2, y2);
				    g.draw(line);
				}
				x1 = x2; y1 = y2;
				pit.next(); i++;
			    }
			    if (segtype == PathIterator.SEG_CLOSE) {
				if (e.edgeMask[0]) {
				    Line2D.Float line = new
					Line2D.Float(x1, y1, x0, y0);
				    g.draw(line);
				}
			    }
			}
		    }
		}
	    }
	} finally {
	    g.setPaint(cc);
	}
    }
}
