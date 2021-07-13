package org.bzdev.p3d;

import java.awt.*;
import java.awt.geom.*;

class PolyLine implements Shape, Cloneable {
    Path2D.Float path;
    int npoints = 0;
    boolean closed = false;
    PolyLine() {
	path = new Path2D.Float();
    }

    PolyLine(double x1, double y1, double x2, double y2,
	     double x3, double y3)
    {
	path = new Path2D.Float(Path2D.WIND_NON_ZERO, 4);
	path.moveTo(x1, y1);
	path.lineTo(x2, y2);
	path.lineTo(x3, y3);
	path.closePath();
	npoints = 3;
	closed = true;
    }

    PolyLine(double x1, double y1, double x2, double y2) {
	path = new Path2D.Float(Path2D.WIND_NON_ZERO, 2);
	path.moveTo(x1, y1);
	path.lineTo(x2, y2);
	npoints = 2;
	closed = false;
    }

    @Override
    public boolean contains(double x, double y) {
	return path.contains(x, y);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
	return path.contains(x, y, w, h);
    }

    @Override
    public boolean contains(Point2D p) {
	return path.contains(p);
    }

    @Override
    public boolean contains(Rectangle2D r) {
	return path.contains(r);
    }

    @Override
    public Rectangle getBounds() {
	return path.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
	return path.getBounds2D();
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
	return path.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
	return path.getPathIterator(at, flatness);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
	return path.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
	return path.intersects(r);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
	Object object = super.clone();
	if (object instanceof PolyLine) {
	    PolyLine obj = (PolyLine)object;
	    obj.path = (Path2D.Float)path.clone();
	}
	return object;
    }
}
