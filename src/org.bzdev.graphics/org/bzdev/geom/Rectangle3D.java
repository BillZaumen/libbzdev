package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;
import java.util.Locale;

import org.bzdev.lang.StackTraceModePermission;
import java.awt.Color;
import java.security.*;

/**
 * Class representing a rectangular cuboid aligned with respect to 
 * the X, Y, and Z axes.
 * The API is modeled after the API for {@link java.awt.geom.Rectangle2D}
 * in order to reduce the learning curve.
 * <P>
 * A point is considered to lie <a name="inside">inside</a>
 * the rectangular cuboid, and thus to be contained by it, if all of
 * the following are true:
 * <UL>
 *  <li> the point is completely inside the cuboid.
 *  <li> the point lies on the boundary and the space adjacent to
 *  <li> the point is on the boundary of the cuboid and there exists
 *       a point with a larger X value that is inside the cuboid
 *  <li> the point is on the boundary of the cuboid and there exists
 *       a point with a larger Y value that is inside the cuboid
 *  <li> the point is on the boundary of the cuboid and there exists
 *       a point with a larger Z value that is inside the cuboid
 * </UL>
 * This convention is meant to mimic the convention used by
 * the {@link java.awt.Shape} interface and specifically the class
 * {@link java.awt.geom.Rectangle2D}.
 * <P>
 * Note: this documentation was in part based nearly literally on
 * the documentation for the class {@link java.awt.geom.Rectangle2D}.
 */
public abstract class Rectangle3D implements Shape3D, Cloneable {

    Color color;
    Object tag;

    /**
     * Psuedo tag.
     * Using this object as a tag will cause the tag to be a stack trace.
     */
    public static final Object STACKTRACE = new Object();

    @Override
    public boolean isOriented() {return true;}

    @Override
    public Path3D getBoundary() {
	return new Path3D.Double();
    }

    @Override
    public boolean isClosedManifold() {
	return true;
    }

    @Override
    public int numberOfComponents() {return 1;}

    public Shape3D getComponent(int i) throws IllegalArgumentException {
	if (i != 0) throw new IllegalArgumentException();
	return this;
    }

    /**
     * Set a rectangle's color.
     * @param c the color for this rectangle; null if none is specified
     */
    public void setColor(Color c) {
	color = c;
    }

    /**
     * Subclass of {@link Rectangle3D} that stores its values as
     * double-precision numbers.
     *
     */
    public static class Double extends Rectangle3D {
	private double xmin;
	private double ymin;
	private double zmin;
	private double width;
	private double height;
	private double depth;

	/**
	 * Constructor.
	 * The rectangle will have zero size and will be located
	 * at (0, 0, 0).
	 */
	public Double() {
	}

	/**
	 * Constructor with a tag.
	 * The rectangle will have zero size and will be located
	 * at (0, 0, 0).
	 * @param tag a tag naming this object; {@link Rectangle3D#STACKTRACE}
	 *        to tag this object with a stacktrace
	 * @exception SecurityException a security manager was installed
	 *            and the permission
	 *            org.bzdev.lang.StackTraceModePermission was not
	 *            granted for the class org.bzdev.geom.Rectangle3D
	 *            when the tag was the constant
	 *            {@link Rectangle3D#STACKTRACE}
	 */
	public Double(Object tag) {
	    if (tag == Rectangle3D.STACKTRACE) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new StackTraceModePermission
				       ("org.bzdev.geom.Rectangle3D"));
		}
		this.tag = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		this.tag = tag;
	    }

	}

	/**
	 * Constructor providing dimensions of a rectangular cuboid.
	 * @param x the X coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param y the Y coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param z the Z coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param w the width of the object (the length in the X direction)
	 * @param h the height of the object (the length in the Y direction)
	 * @param d the depth of the object (the length in the Z direction)
	 */
	public Double(double x, double y, double z,
		      double w, double h, double d)
	{
	    xmin = x;
	    ymin = y;
	    zmin = z;
	    width = w;
	    height = h;
	    depth = d;
	}

	/**
	 * Constructor providing dimensions of a rectangular cuboid and
	 * providing a tag.
	 * @param x the X coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param y the Y coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param z the Z coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param w the width of the object (the length in the X direction)
	 * @param h the height of the object (the length in the Y direction)
	 * @param d the depth of the object (the length in the Z direction)
	 * @param tag a tag naming this object; {@link Rectangle3D#STACKTRACE}
	 *        to tag this object with a stacktrace
	 * @exception SecurityException a security manager was installed
	 *            and the permission
	 *            org.bzdev.lang.StackTraceModePermission was not
	 *            granted for the class org.bzdev.geom.Rectangle3D
	 *            when the tag was the constant
	 *            {@link Rectangle3D#STACKTRACE}
	 */
	public Double(double x, double y, double z,
		      double w, double h, double d,
		      Object tag)
	{
	    this(x, y, z, w, h, d);
	    if (tag == Rectangle3D.STACKTRACE) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new StackTraceModePermission
				       ("org.bzdev.geom.Rectangle3D"));
		}
		this.tag = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		this.tag = tag;
	    }
	}

	@Override
	public double getMinX() {return xmin;}
	@Override
	public double getMinY() {return ymin;}
	@Override
	public double getMinZ() {return zmin;}

	@Override
	public double getWidth() {return width;}
	@Override
	public double getHeight() {return height;}
	@Override
	public double getDepth() {return depth;}

	@Override
	public void setRect(double x, double y, double z,
			    double w, double h, double d)
	{
	    xmin = x;
	    ymin = y;
	    zmin = z;
	    width = w;
	    height = h;
	    depth = d;
	}

	@Override
	public boolean isEmpty() {
	    return (width == 0.0) && (height == 0.0) && (depth == 0.0);
	}

	@Override
	public String toString() {
	    return String.format((Locale)null, "{xmin=%g, ymin=%g, zmin=%g, "
				 + "width=%g, height=%g, depth=%g}",
				 xmin, ymin, zmin, width, height, depth);
	}
	@Override
	public SurfaceIterator getSurfaceIterator(Transform3D tform,
						  int level)
	{
	    if (tform == null) {
		if (level == 0) {
		    return getSurfaceIterator(null);
		} else {
		    return new SubdivisionIterator(getSurfaceIterator(null),
						   level);
		}
	    } else if (tform instanceof AffineTransform3D) {
		if (level == 0) {
		    return getSurfaceIterator(tform);
		} else {
		    return new SubdivisionIterator(getSurfaceIterator(tform),
						   level);
		}
	    } else {
		if (level == 0) {
		    return getSurfaceIterator(tform);
		} else {
		    return new SubdivisionIterator(getSurfaceIterator(null),
						   tform, level);
		}
	    }
	}
    }

    /**
     * Subclass of {@link Rectangle3D} that stores its values as
     * single-precision numbers.
     *
     */
    public static class Float extends Rectangle3D {
	private float xmin;
	private float ymin;
	private float zmin;
	private float width;
	private float height;
	private float depth;

	/**
	 * Constructor.
	 * The rectangle will have zero size and will be located
	 * at (0, 0, 0).
	 */
	public Float() {
	}

	/**
	 * Constructor with a tag.
	 * The rectangle will have zero size and will be located
	 * at (0, 0, 0).
	 * @param tag a tag naming this object; {@link Rectangle3D#STACKTRACE}
	 *        to tag this object with a stacktrace
	 * @exception SecurityException a security manager was installed
	 *            and the permission
	 *            org.bzdev.lang.StackTraceModePermission was not
	 *            granted for the class org.bzdev.geom.Rectangle3D
	 *            when the tag was the constant
	 *            {@link Rectangle3D#STACKTRACE}
	 */
	public Float(Object tag) {
	    this();
	    if (tag == Rectangle3D.STACKTRACE) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new StackTraceModePermission
				       ("org.bzdev.geom.Rectangle3D"));
		}
		this.tag = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		this.tag = tag;
	    }
	}

	/**
	 * Constructor providing dimensions of a rectangular cuboid.
	 * @param x the X coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param y the Y coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param z the Z coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param w the width of the object (the length in the X direction)
	 * @param h the height of the object (the length in the Y direction)
	 * @param d the depth of the object (the length in the Z direction)
	 */
	public Float(float x, float y, float z,
		      float w, float h, float d)
	{
	    xmin = x;
	    ymin = y;
	    zmin = z;
	    width = w;
	    height = h;
	    depth = d;
	}

	/**
	 * Constructor providing dimensions of a rectangular cuboid and
	 * providing a tag.
	 * @param x the X coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param y the Y coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param z the Z coordinate of the vertex whose coordinates have a
	 *        minimal value.
	 * @param w the width of the object (the length in the X direction)
	 * @param h the height of the object (the length in the Y direction)
	 * @param d the depth of the object (the length in the Z direction)
	 * @param tag a tag naming this object; {@link Rectangle3D#STACKTRACE}
	 *        to tag this object with a stacktrace
	 * @exception SecurityException a security manager was installed
	 *            and the permission
	 *            org.bzdev.lang.StackTraceModePermission was not
	 *            granted for the class org.bzdev.geom.Rectangle3D
	 *            when the tag was the constant
	 *            {@link Rectangle3D#STACKTRACE}
	 */
	public Float(float x, float y, float z,
		     float w, float h, float d,
		     Object tag)
	{
	    this(x, y, z, w, h, d);
	    if (tag == Rectangle3D.STACKTRACE) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
		    sm.checkPermission(new StackTraceModePermission
				       ("org.bzdev.geom.Rectanlge3D"));
		}
		this.tag = AccessController.doPrivileged
		     (new PrivilegedAction<StackTraceElement[]>() {
			public StackTraceElement[] run() {
			    return Thread.currentThread().getStackTrace();
			}
		     });
	    } else {
		this.tag = tag;
	    }
	}

	@Override
	public double getMinX() {return xmin;}
	@Override
	public double getMinY() {return ymin;}
	@Override
	public double getMinZ() {return zmin;}

	@Override
	public double getWidth() {return width;}
	@Override
	public double getHeight() {return height;}
	@Override
	public double getDepth() {return depth;}

	@Override
	public void setRect(double x, double y, double z,
			    double w, double h, double d)
	{
	    xmin = (float)x;
	    ymin = (float)y;
	    zmin = (float)z;
	    width = (float)w;
	    height = (float)h;
	    depth = (float)d;
	}

	@Override
	public boolean isEmpty() {
	    return (width == 0.0F) && (height == 0.0F) && (depth == 0.0F);
	}

	@Override
	public String toString() {
	    return String.format((Locale)null, "{xmin=%g, ymin=%g, zmin=%g, "
				 + "width=%g, height=%g, depth=%g}",
				 xmin, ymin, zmin, width, height, depth);
	}
    }

    /**
     * Adds a point, specified by its coordinates, to this Rectangle3D.
     * The resulting Rectangle3D is the smallest Rectangle3D that
     * contains both the original Rectangle3D and the specified point
     * (newx, newy, newz) specified by the arguments.
     * <P>
     * After adding a point, a call to contains with the added point
     * as an argument does not necessarily return true. The contains
     * method does not return true for points on the right or bottom
     * edges of a rectangle. Therefore, if the added point falls on
     * the left or bottom edge of the enlarged rectangle, contains
     * returns false for that point.
     * @param newx the X coordinate of the new point
     * @param newy the Y coordinate of the new point
     * @param newz the Z coordinate of the new point
     */
    public void add(double newx, double newy, double newz) {
	Rectangle3D result = (Rectangle3D) clone();
	result.setRectFromDiagonal(getMinX(), getMinY(), getMinZ(),
				   newx, newy, newz);
	union(this, result, this);

    }

    /**
     * Adds a point to this Rectangle3D.
     * The resulting Rectangle3D is the smallest Rectangle3D that
     * contains both the original Rectangle3D and the specified point
     * (newx, newy, newz) specified by the arguments.
     * <P>
     * After adding a point, a call to contains with the added point
     * as an argument does not necessarily return true. The contains
     * method does not return true for points on the right or bottom
     * edges of a rectangle. Therefore, if the added point falls on
     * the left or bottom edge of the enlarged rectangle, contains
     * returns false for that point.
     * @param p the new point
     */
    public void add(Point3D p) {
	add(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Adds a Rectangle3D object to this Rectangle3D. 
     * The resulting Rectangle3D is the union of the two Rectangle2D objects.
     * @param r the rectangle to add
     */
    public void add(Rectangle3D r) {
	union(this, r, this);
    }

    /**
     * Returns a new Rectangle3D object representing the intersection
     * of this Rectangle3D with the specified Rectangle3D.
     * @param r the Rectangle2D to be intersected with this Rectangle3D
     * @return the largest Rectangle3D contained in both the specified
     *         Rectangle3D and in this Rectangle3D.
     */
    public Rectangle3D createIntersection(Rectangle3D r) {
	Rectangle3D result = (Rectangle3D) clone();
	intersect(this, r, result);
	return result;
    }

    /**
     * Returns a new Rectangle3D object representing the union of this
     * Rectangle3D with the specified Rectangle3D.
     * @param r the Rectangle3D to be intersected with this Rectangle3D
     * @return the smallest Rectangle3D containing both the specified
     *         Rectangle3D and this Rectangle2D
     */
    public Rectangle3D createUnion(Rectangle3D r) {
	Rectangle3D result = (Rectangle3D) clone();
	union(this, r, result);
	return result;

    }

    /**
     * Tests if the specified coordinates are inside this Rectangle3D.
     * A point is contained by this rectangle if it is
     * <a href= "#inside">inside</a> it.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return true if the point denoted by the specified coordinates
     *         are inside this cuboid; false otherwise
     */
    public boolean contains(double x, double y, double z) {
	double xdiff = x - getMinX();
	double ydiff = y - getMinY();
	double zdiff = z - getMinZ();
	return ((xdiff >= 0.0 && xdiff < getWidth())
		&& (ydiff >= 0.0 && ydiff < getHeight())
		&& (zdiff >= 0.0 && zdiff < getDepth()));
    }

    /**
     * Tests if all the points inside the specified rectangular cuboid,
     * specified by coordinates and dimensions, are inside this cuboid.
     * A point is contained by this rectangle if it is
     * <a href= "#inside">inside</a> it.
     * @param x the X coordinate of the specified cuboid's minimum X value
     * @param y the Y coordinate of the specified cuboid's minimum Y value
     * @param z the Z coordinate of the specified cuboid's minimum Z value
     * @param w the width of the specified cuboid (its length in the
     *          X direction)
     * @param h the height of the specified cuboid (its length in the
     *          Y direction)
     * @param d the depth of the specified cuboid (its length in the
     *          Z direction)
     * @return true if the specified rectangular cuboid are inside this cuboid;
     *         false otherwise
     */
    public boolean contains (double x, double y, double z,
			     double w, double h, double d)
    {
	double xmin = getMinX();
	double ymin = getMinY();
	double zmin = getMinZ();
	double x1 = xmin + getWidth();
	double y1 = ymin + getHeight();
	double z1 = zmin + getDepth();

	double x2 = x + w;
	double y2 = y + h;
	double z2 = z + d;

	return xmin <= x && ymin <= y && zmin <= z
	    && x2 <= x1 && y2 <= y1 && z2 <= z1;
    }

    /**
     * Test if a point is contained by this rectangular cuboid.
     * A point is contained by this rectangle if it is
     * <a href= "#inside">inside</a> it.
     * @param p a point
     * @return true if the specified point is within this cuboid; false
     *              otherwise
     */
    public boolean contains(Point3D p) {
	return contains(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Tests if all the points inside the specified rectangular cuboid
     * are inside this cuboid.
     * A point is contained by this rectangle if it is
     * <a href= "#inside">inside</a> it.
     * @param r the specified rectangular cuboid
     * @return true if the points inside r are within this cuboid; false
     *              otherwise
     */
    public boolean contains(Rectangle3D r) {
	double x1 = r.getMinX();
	double y1 = r.getMinY();
	double z1 = r.getMinZ();
	double x2 = x1 + r.getWidth();
	double y2 = y1 + r.getHeight();
	double z2 = z1 + r.getDepth();
	return contains(x1, y1, z1) && contains(x2, y2, z2);
    }


    /**
     * Get the height of this rectangular cuboid.
     * The height is defined as the length in the Y direction.
     * @return the height
     */
    public abstract double getHeight();

    /**
     * Get the width of this rectangular cuboid.
     * The width is defined as the length in the X direction.
     * @return the width
     */
    public abstract double getWidth();

    /**
     * Get the depth of this rectangular cuboid.
     * The depth is defined as the length in the Z direction.
     * @return the depth
     */
    public abstract double getDepth();

    /**
     * Get the minimum X coordinate for this rectangular cuboid
     * @return the minimum X coordinate
     */
    public abstract double getMinX();

    /**
     * Get the minimum Y coordinate for this rectangular cuboid
     * @return the minimum Y coordinate
     */
    public abstract double getMinY();

    /**
     * Get the minimum Z coordinate for this rectangular cuboid
     * @return the minimum Z coordinate
     */
    public abstract double getMinZ();

    /**
    /**
     * Get the maximum X coordinate for this rectangular cuboid
     * @return the maximum X coordinate
     */
    public double getMaxX() {
	return getMinX() + getWidth();
    }

    /**
     * Get the maximum Y coordinate for this rectangular cuboid
     * @return the maximum Y coordinate
     */
    public double getMaxY() {
	return getMinY() + getHeight();
    }

    /**
     * Get the maximum Z coordinate for this rectangular cuboid
     * @return the maximum Z coordinate
     */
    public double getMaxZ() {
	return getMinZ() + getDepth();
    }


    /**
     * Get the X coordinate for the center of this rectangular cuboid.
     * @return the X coordinate for the center of this rectangular cuboid
     */
    public double getCenterX() {
	return getMinX() + getWidth()/2.0;
    }

    /**
     * Get the Y coordinate for the center of this rectangular cuboid.
     * @return the Y coordinate for the center of this rectangular cuboid
     */
    public double getCenterY() {
	return getMinY() + getHeight()/2.0;
    }

    /**
     * Get the Z coordinate for the center of this rectangular cuboid.
     * @return the Z coordinate for the center of this rectangular cuboid
     */
    public double getCenterZ() {
	return getMinZ() + getDepth()/2.0;
    }

    /**
     * Determine if this rectangular cuboid intersects another rectangular
     * cuboid.
     * @param r the other rectangular cuboid
     * @return true if this cuboid and the other cuboid intersect; false
     *         otherwise
     */
    public boolean intersects(Rectangle3D r) {
	return intersects(r.getMinX(), r.getMinY(), r.getMinZ(),
			  r.getWidth(), r.getHeight(), r.getDepth());
    }


    /**
     * Determine if this rectangular cuboid intersects a
     * rectangular cuboid that is specified by its corner and the
     * length of its edges.
     * @param x the X coordinate of the specified cuboid's minimum X value
     * @param y the Y coordinate of the specified cuboid's minimum Y value
     * @param z the Z coordinate of the specified cuboid's minimum Z value
     * @param w the width of the specified cuboid (its length in the
     *          X direction)
     * @param h the height of the specified cuboid (its length in the
     *          Y direction)
     * @param d the depth of the specified cuboid (its length in the
     *          Z direction)
     * @return true if this cuboid and the specified cuboid intersect; false
     *         otherwise
     */
    public boolean intersects(double x, double y, double z,
			      double w, double h, double d)
    {
	double x1 = getMinX();
	double y1 = getMinY();
	double z1 = getMinZ();
	double x2 = x1 + getWidth();
	double y2 = y1 + getHeight();
	double z2 = z1 + getDepth();

	double xo = x+w;
	double yo = y+h;
	double zo = z+d;

	if (xo < x1 || x > x2) return false;
	if (yo < y1 || y > y2) return false;
	if (zo < z1 || z > z2) return false;

	return true;
    }

    /**
     * Intersects two source Rectangle3D objects and puts the result into
     * the specified destination Rectangle3D object.
     * The destination object can be one of the source objects, if desired,
     * in which case a source object will be overridden.
     * @param src1 the first source Rectangle3D object
     * @param src2 the second source Rectangle3D object
     * @param dest the destination Rectangle3D object
     */
    public static void intersect(Rectangle3D src1,
				 Rectangle3D src2,
				 Rectangle3D dest)
    {
	double x1 = src1.getMinX();
	double y1 = src1.getMinY();
	double z1 = src1.getMinZ();
	double x2 = x1 + src1.getWidth();
	double y2 = y1 + src1.getHeight();
	double z2 = z1 + src1.getDepth();

	double xr1 = src2.getMinX();
	double yr1 = src2.getMinY();
	double zr1 = src2.getMinZ();
	double wr = src2.getWidth();
	double hr = src2.getHeight();
	double dr = src2.getDepth();

	if(!src1.intersects(xr1, yr1, zr1, wr, hr, dr)) {
	    // do something sensible for the case where the
	    // intersection is the null set.
	    dest.setRect(x1, y1, z1, 0.0, 0.0, 0.0);
	    return;
	}

	double xr2 = xr1 + wr;
	double yr2 = yr1 + hr;
	double zr2 = zr1 + dr;

	double xx1 = (x1 > xr1)? x1: xr1;
	double yy1 = (y1 > yr1)? y1: yr1;
	double zz1 = (z1 > zr1)? z1: zr1;

	double xx2 = (x2 < xr2)? x2: xr2;
	double yy2 = (y2 < yr2)? y2: yr2;
	double zz2 = (z2 < zr2)? z2: zr2;

	dest.setRectFromDiagonal(xx1, yy1, zz1, xx2, yy2, zz2);
    }

    /**
     * Tests if a Rectangle3D object is empty.
     * A Rectangle3D is empty if its width, height, and depth are
     * all zero.
     * @return true if this object is empty; false otherwise
     */
    public boolean isEmpty() {
	return (getWidth() == 0.0) && (getHeight() == 0.0)
	    && (getDepth() == 0.0);
    }

    @Override
    public Object clone() {
	try {
	    Object object = super.clone();
	    return object;
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError(e);
	}
    }

    @Override
    public Rectangle3D getBounds() {
	return this;
    }

    private static class SI implements SurfaceIterator {
	int index = 0;
	double segments[][];
	Color color;
	Object tag;

	SI(double[][] segments, Color color, Object tag) {
	    this.segments = segments;
	    this.color = color;
	    this.tag = tag;
	}

	@Override
	public void next() {
	    if (index < 12) index++;
	}

	@Override
	public boolean isDone() {
	    return index > 11;
	}

	@Override
	public Color currentColor() {
	    return color;
	}

	@Override
	public Object currentTag() {
	    return tag;
	}

	@Override
	public int currentSegment(double[] coords) {
	    System.arraycopy(segments[index], 0, coords, 0, 9);
	    return SurfaceIterator.PLANAR_TRIANGLE;
	}

	@Override
	public int currentSegment(float[] coords) {
	    for (int i = 0; i < 9; i++) {
		coords[i] = (float)segments[index][i];
	    }
	    return SurfaceIterator.PLANAR_TRIANGLE;
	}

	@Override
	public boolean isOriented() {return true;}
    }

    /**
     * Get a surface iterator for this object.
     * <P>
     * The transform should either be an affine transform or
     * well-approximated by an affine transform over the surface
     * of this rectangle.
     * @param tform a transform to apply to the cuboid; null
     *        if the identity transform will be used
     */
    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform) {
	double xmin = getMinX();
	double xmax = getMaxX();
	double ymin = getMinY();
	double ymax = getMaxY();
	double zmin = getMinZ();
	double zmax = getMaxZ();

	double[][] segments = {
	    {xmax, ymin, zmin, xmax, ymax, zmin, xmin, ymin, zmin}, // bot
	    {xmin, ymin, zmin, xmax, ymax, zmin, xmin, ymax, zmin}, // bot
	    {xmin, ymin, zmax, xmax, ymax, zmax, xmax, ymin, zmax}, // top
	    {xmin, ymin, zmax, xmin, ymax, zmax, xmax, ymax, zmax}, // top
	    {xmin, ymin, zmin, xmax, ymin, zmax, xmax, ymin, zmin}, // front
	    {xmin, ymin, zmin, xmin, ymin, zmax, xmax, ymin, zmax}, // front
	    {xmin, ymax, zmin, xmax, ymax, zmin, xmax, ymax, zmax}, // back
	    {xmin, ymax, zmin, xmax, ymax, zmax, xmin, ymax, zmax}, // back
	    {xmin, ymin, zmin, xmin, ymax, zmin, xmin, ymax, zmax}, // left
	    {xmin, ymin, zmin, xmin, ymax, zmax, xmin, ymin, zmax}, // left
	    {xmax, ymin, zmin, xmax, ymax, zmax, xmax, ymax, zmin}, // right
	    {xmax, ymin, zmin, xmax, ymin, zmax, xmax, ymax, zmax}, // right
	    /*
	    {xmax, ymin, zmin, xmin, ymin, zmin, xmax, ymax, zmin}, // bot
	    {xmin, ymin, zmin, xmin, ymax, zmin, xmax, ymax, zmin}, // bot
	    {xmin, ymin, zmax, xmax, ymin, zmax, xmax, ymax, zmax}, // top
	    {xmin, ymin, zmax, xmax, ymax, zmax, xmin, ymax, zmax}, // top
	    {xmin, ymin, zmin, xmax, ymin, zmin, xmax, ymin, zmax}, // front
	    {xmin, ymin, zmin, xmax, ymin, zmax, xmin, ymin, zmax}, // front
	    {xmin, ymax, zmin, xmax, ymax, zmax, xmax, ymax, zmin}, // back
	    {xmin, ymax, zmin, xmin, ymax, zmax, xmax, ymax, zmax}, // back
	    {xmin, ymin, zmin, xmin, ymax, zmax, xmin, ymax, zmin }, // left
	    {xmin, ymin, zmin, xmin, ymin, zmax, xmin, ymax, zmax}, // left
	    {xmax, ymin, zmin, xmax, ymax, zmin, xmax, ymax, zmax }, // right
	    {xmax, ymin, zmin, xmax, ymax, zmax, xmax, ymin, zmax }, // right
	    */
	};

	if (tform != null) {
	    for (int i = 0; i < 12; i++) {
		double[] tmp = new double[9];
		tform.transform(segments[i], 0, tmp, 0, 3);
		segments[i] = tmp;
	    }
	}

	return new SI(segments, color, tag);
    }

    @Override
    public SurfaceIterator getSurfaceIterator(Transform3D tform, int level)
    {
	if (tform == null) {
	    if (level == 0) {
		return getSurfaceIterator(null);
	    } else {
		return new SubdivisionIterator(getSurfaceIterator(null),
					       level);
	    }
	} else if (tform instanceof AffineTransform3D) {
	    if (level == 0) {
		return getSurfaceIterator(tform);
	    } else {
		return new SubdivisionIterator(getSurfaceIterator(tform),
					       level);
	    }
	} else {
	    if (level == 0) {
		return getSurfaceIterator(tform);
	    } else {
		return new SubdivisionIterator(getSurfaceIterator(null),
					       tform, level);
	    }
	}
    }

    /**
     * Set the configuration of this object given explicit coordinates
     * and dimensions
     * @param x the X coordinate of the vertex whose coordinates have a
     *        minimal value.
     * @param y the Y coordinate of the vertex whose coordinates have a
     *        minimal value.
     * @param z the Z coordinate of the vertex whose coordinates have a
     *        minimal value.
     * @param w the width of the object (the length in the X direction)
     * @param h the height of the object (the length in the Y direction)
     * @param d the depth of the object (the length in the Z direction)
     */
    public abstract void setRect(double x, double y, double z,
			  double w, double h, double d);

    /**
     * Set the configuration of this object to that of a specified Rectangle3D.
     * @param r the object that will be copied.
     */
    public void setRect(Rectangle3D r) {
	setRect(r.getMinX(), r.getMinY(), r.getMinZ(),
		r.getWidth(), r.getHeight(), r.getDepth());
    }

    /**
     * Set the configuration of this object based on the coordinates of
     * vertices that lie along a diagonal.
     * @param x1 the X coordinate of the first vertex
     * @param y1 the Y coordinate of the first vertex
     * @param z1 the Z coordinate of the first vertex
     * @param x2 the X coordinate of the second vertex
     * @param y2 the Y coordinate of the second vertex
     * @param z2 the Z coordinate of the second vertex
     */
    public void setRectFromDiagonal(double x1, double y1, double z1,
				    double x2, double y2, double z2) {
	double x = (x1 < x2)? x1: x2;
	double y = (y1 < y2)? y1: y2;
	double z = (z1 < z2)? z1: z2;

	double w = Math.abs(x2-x1);
	double h = Math.abs(y2-y1);
	double d = Math.abs(z2-z1);

	setRect(x, y, z, w, h, d);
    }

    /**
     * Set the configuration of this object based on vertices that lie
     * along a diagonal.
     * @param p1 a point at the location of the first vertex
     * @param p2 a point at the location of the second vertex
     */
    public void setRectFromDiagonal(Point3D p1, Point3D p2) {
	setRectFromDiagonal(p1.getX(), p1.getY(), p1.getZ(),
			    p2.getX(), p2.getY(), p2.getZ());
    }


    /**
     * Set the configuration of this object based on the coordinates
     * of the center of this cuboid and the coordinates of a corner.
     * @param centerX the X coordinate of the center of the desired cuboid
     * @param centerY the Y coordinate of the center of the desired cuboid
     * @param centerZ the Y coordinate of the center of the desired cuboid
     * @param cornerX the X coordinate of a corner of the desired  cuboid
     * @param cornerY the Y coordinate of a corner of the desired cuboid
     * @param cornerZ the Y coordinate of a corner of the desired cuboid

     */
    public void setRectFromCenter(double centerX,
				  double centerY,
				  double centerZ,
				  double cornerX,
				  double cornerY,
				  double cornerZ)
    {
	// double x = centerX - (cornerX - centerX);
	double x = 2.0*centerX - cornerX;
	double y = 2.0*centerY - cornerY;
	double z = 2.0*centerZ - cornerZ;

	setRectFromDiagonal(x, y, z, cornerX, cornerY, cornerZ);
    }

    /**
     * Set the configuration of this object based on points at
     * the center of this cuboid and a corner of this cuboid.
     * @param center a point positioned at the center of the desired cuboid
     * @param corner a point positioned at a corner of the desired cuboid
     */
    public void setRectFromCenter(Point3D center, Point3D corner) {
	setRectFromCenter(center.getX(), center.getY(), center.getZ(),
			  corner.getX(), corner.getY(), corner.getZ());
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Rectangle3D) {
	    Rectangle3D object = (Rectangle3D) obj;
	    if ((getMinX() == object.getMinX())
		&& (getMinY() == object.getMinY())
		&& (getMinZ() == object.getMinZ())
		&& (getWidth() == object.getWidth())
		&& (getHeight() == object.getHeight())
		&& (getDepth() == object.getDepth())) {
		return true;
	    } 
	}
	return false;
    }


    @Override
    public int hashCode() {
	long vx = java.lang.Double.doubleToLongBits(getMinX());
	long vy = java.lang.Double.doubleToLongBits(getMinY()) * 31;
	long vz = java.lang.Double.doubleToLongBits(getMinZ()) * 1023;
	long w = java.lang.Double.doubleToLongBits(getWidth()) * ((1<<10)-1);
	long h = java.lang.Double.doubleToLongBits(getHeight()) * ((1<<15)-1);
	long d = java.lang.Double.doubleToLongBits(getDepth()) * ((1<<20)-1);
	
	long v = ((((vx ^ vy) ^ vz) ^ w) ^ h) ^ d;
	return ((int)v) ^((int)(v>>>32));
    }



    /**
     * Sets a destination Rectangle3D to the union of two source
     * Rectangle3D objects.
     * The destination object can be one of the source objects, if desired,
     * in which case a source object will be overridden.
     * @param src1 the first source Rectangle3D object
     * @param src2 the second source Rectangle3D object
     * @param dest the destination Rectangle3D object
     */
    public static void
	union(Rectangle3D src1, Rectangle3D src2, Rectangle3D dest)
    {
	double x1 = src1.getMinX();
	double y1 = src1.getMinY();
	double z1 = src1.getMinZ();
	double x2 = x1 + src1.getWidth();
	double y2 = y1 + src1.getHeight();
	double z2 = z1 + src1.getDepth();

	double xr1 = src2.getMinX();
	double yr1 = src2.getMinY();
	double zr1 = src2.getMinZ();
	double xr2 = xr1 + src2.getWidth();
	double yr2 = yr1 + src2.getHeight();
	double zr2 = zr1 + src2.getDepth();

	double xx1 = (x1 < xr1)? x1: xr1;
	double yy1 = (y1 < yr1)? y1: yr1;
	double zz1 = (z1 < zr1)? z1: zr1;
	double xx2 = (xr2 < x2)? x2: xr2;
	double yy2 = (yr2 < y2)? y2: yr2;
	double zz2 = (zr2 < z2)? z2: zr2;
	dest.setRectFromDiagonal(xx1, yy1, zz1, xx2, yy2, zz2);
    }
}

//  LocalWords:  API li Psuedo STACKTRACE stacktrace xmin ymin zmin
//  LocalWords:  SecurityException newx newy newz href cuboid's src
//  LocalWords:  dest tform vertices centerX centerY centerZ cornerX
//  LocalWords:  cornerY cornerZ
