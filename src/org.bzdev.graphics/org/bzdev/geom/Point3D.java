package org.bzdev.geom;
import org.bzdev.lang.UnexpectedExceptionError;


/**
 * Class representing a point in three dimensions.
 */
public abstract class Point3D implements Cloneable {

    /**
     * Constructor.
     */
    protected  Point3D() {
    }

    /**
     * Class representing points in three dimensions using
     * double-precision coordinates
     */
    public static class Double extends Point3D {
	public double x;
	public double y;
	public double z;

	/**
	 * Constructor.
	 */
	public Double() {super();}

	/**
	 * Constructor with coordinates.
	 * @param x the X coordinate of the point
	 * @param y the Y coordinate of the point
	 * @param z the Z coordinate of the point
	 */
	public Double(double x, double y, double z) {
	    super();
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}

	

	@Override
	public double getX() {return x;}
	@Override
	public double getY() {return y;}
	@Override
	public double getZ() {return z;}

	@Override
	public void setLocation(double x, double y, double z) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	}

	@Override
	public String toString() {
	    return "{ " + x + ", " + y + ", " + z + "}";
	}
    }

    /**
     * Class representing points in three dimensions using
     * double-precision coordinates
     */
    public static class Float extends Point3D {
	public float x;
	public float y;
	public float z;

	/**
	 * Constructor.
	 */
	public Float() {}

	/**
	 * Constructor with coordinates.
	 * @param x the X coordinate of the point
	 * @param y the Y coordinate of the point
	 * @param z the Z coordinate of the point
	 */
	public Float(double x, double y, double z) {
	    this.x = (float)x;
	    this.y = (float)y;
	    this.z = (float)z;
	}

	@Override
	public double getX() {return (double)x;}
	@Override
	public double getY() {return (double)y;}
	@Override
	public double getZ() {return (double)z;}

	@Override
	public void setLocation(double x, double y, double z) {
	    this.x = (float)x;
	    this.y = (float)y;
	    this.z = (float)z;
	}

	@Override
	public String toString() {
	    return "{ " + x + ", " + y + ", " + z + "}";
	}
    }

    /**
     * Get the distance from this point to another point given coordinates.
     * @param px the X coordinate of the other point
     * @param py the Y coordinate of the other point
     * @param pz the Z coordinate of the other point
     * @return the distance to the point (px, py, pz)
     */
    public double distance (double px, double py, double pz) {
	double dx = getX() - px;
	double dy = getY() - py;
	double dz = getZ() - pz;
	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Get the distance between two points given their coordinates.
     * @param x1 the X coordinate of the first point
     * @param y1 the Y coordinate of the first point
     * @param z1 the Z coordinate of the first point
     * @param x2 the X coordinate of the second point
     * @param y2 the Y coordinate of the second point
     * @param z2 the Z coordinate of the second point
     * @return the distance between two specified points
     */
    public static double distance(double x1, double y1, double z1,
				  double x2, double y2, double z2)
    {
	double dx = x1 - x2;
	double dy = y1 - y2;
	double dz = z1 - z2;
	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Get the distance from this point to another point.
     * @param pt a point
     * @return the distance from this point to point pt
     */
    public double distance(Point3D pt) {
	double dx = getX() - pt.getX();
	double dy = getY() - pt.getY();
	double dz = getZ() - pt.getZ();
	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }


    /**
     * Get the square of the distance from this point to another point
     * given coordinates.
     * @param x the X coordinate of the other point
     * @param y the Y coordinate of the other point
     * @param z the Z coordinate of the other point
     * @return the square of the distance to the point (px, py, pz)
     */
    public double distanceSq(double x, double y, double z) {
	double dx = getX() - x;
	double dy = getY() - y;
	double dz = getZ() - z;
	return dx*dx + dy*dy + dz*dz;
    }

    /**
     * Get the square of the distance between two points given their
     * coordinates.
     * @param x1 the X coordinate of the first point
     * @param y1 the Y coordinate of the first point
     * @param z1 the Z coordinate of the first point
     * @param x2 the X coordinate of the second point
     * @param y2 the Y coordinate of the second point
     * @param z2 the Z coordinate of the second point
     * @return the square of the distance between two specified points
     */
    public static double distanceSq(double x1, double y1, double z1,
				    double x2, double y2, double z2)
    {
	double dx = x1 - x2;
	double dy = y1 - y2;
	double dz = z1 - z2;
	return dx*dx + dy*dy + dz*dz;
    }

    /**
     * Get the square of the distance from this point to another point.
     * @param pt a point
     * @return the square of the distance from this point to point pt
     */
    public double distanceSq(Point3D pt) {
	double dx = getX() - pt.getX();
	double dy = getY() - pt.getY();
	double dz = getZ() - pt.getZ();
	return dx*dx + dy*dy + dz*dz;
    }

    /**
     * Get the X coordinate of this point.
     * @return the X coordinate
     */
    public abstract double getX();

    /**
     * Get the Y coordinate of this point.
     * @return the Y coordinate
     */
    public abstract double getY();

    /**
     * Get the Z coordinate of this point.
     * @return the Z coordinate
     */
    public abstract double getZ();

    @Override
    public int hashCode() {
	long vx = java.lang.Double.doubleToLongBits(getX());
	long vy = java.lang.Double.doubleToLongBits(getY()) * 31;
	long vz = java.lang.Double.doubleToLongBits(getZ()) * 1023;
	
	long v = (vx ^ vy) ^ vz;
	return ((int)v) ^((int)(v>>>32));
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) return false;
	if (obj instanceof Point3D) {
	    Point3D other = (Point3D)obj;
	    return (getX() == other.getX()
		    && getY() == other.getY()
		    && getZ() == other.getZ());

	} else {
	    return false;
	}
    }

    /**
     * Set the location of this point given the location's coordinates.
     * @param x the X coordinate for this point's location
     * @param y the Y coordinate for this point's location
     * @param z the Z coordinate for this point's location
     */
    public abstract void setLocation(double x, double y, double z);
    
    /**
     * Set the location of this point to the location of another point
     * @param p the point whose location will determine the location of
     *        this point
     */
    public void setLocation(Point3D p) {
	setLocation(p.getX(), p.getY(), p.getZ());
    }

    /**
     * Clone this object.
     * The new object will have the same X, Y, and Z coordinates
     * @return a clone of this object
     */
    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new UnexpectedExceptionError();
	}
    }
}

//  LocalWords:  px py pz
