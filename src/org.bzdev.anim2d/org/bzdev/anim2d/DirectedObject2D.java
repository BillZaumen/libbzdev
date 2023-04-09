package org.bzdev.anim2d;
import org.bzdev.lang.Callable;
import org.bzdev.devqsim.SimFunction;
import org.bzdev.graphs.Graph;
import org.bzdev.geom.BasicSplinePath2D;
import org.bzdev.math.CubicSpline;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Animation object that follows a path.
 * This class is provided primarily so that factories can be used
 * to configure paths.
 */
public abstract class DirectedObject2D extends PlacedAnimationObject2D {

    Animation2D animation;

    AnimationPath2D apath = null; // used only for printing the state & config
    BasicSplinePath2D path = null;

    boolean angleRelative = false;
    boolean angleRelative0 = false; // used only for printing config
    double pathAngle = 0.0;
    double pathAngle0 = 0.0;	// used only for printing config
    double pathAngularVelocity = 0.0;
    double pathAngularAccel = 0.0;
    double time;
    long stime = -1;		// just used for a test in update
    double time0 = Double.NaN;
    double s0 = Double.NaN;
    double u0 = Double.NaN;
    double s;

    RealValuedFunction distFunction = null;
    SimFunction distSimFunction = null;
    RealValuedFunction angleFunction = null;
    SimFunction angleSimFunction = null;
    RealValuedFunction pathAngleFunction = null;
    SimFunction pathAngleSimFunction = null;
    double angularVelocity = 0.0;
    double angularAcceleration = 0.0;

    double pathVelocity = 0.0;
    double pathAccel = 0.0;

    private static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }


    /**
     * Constructor.
     * @param animation the animation
     * @param name the name of the object; null for an automatically generated
     *        name
     * @param intern true if the object can be looked up by using the methods
     * in {@link org.bzdev.devqsim.Simulation Simulation}; false otherwise.
     * @exception IllegalArgumentException typically means a name is already
     *            in use
     * @see org.bzdev.devqsim.Simulation#getObject(String,Class)
     */
    public DirectedObject2D(Animation2D animation,
			     String name,
			     boolean intern)
    {
	super(animation, name, intern);
	this.animation = animation;
	time = animation.currentTime();
    }

    /**
     * Determine if this object has a path to follow.
     * @return true if this object has a path to follow; false otherwise
     */
    public boolean hasPath() {
	return (path != null);
    }

    /**
     * Get the time used by the last call to update.
     * @return the time;
     * @see #update(double,long)
     * @see AnimationObject2D#update()
     */
    protected double getUpdatedTime() {
	return time;
    }

    /**
     * Get the path parameter at the time specified in the last call to update.
     *@return the value of the path parameter u; Double.NaN if no path set
     * @see #update(double,long)
     */
    protected double getU() {
	return (path == null)? Double.NaN: path.u(s);
    }

    /**
     * Get the distance along the path at the start of a path
     * traversal at the time specified in the last call to update.
     * The value is initialized when a setPath method is called.
     * @return the initial distance along the path;
     *         Double.NaN if the path is null
     */
    protected double getInitialS() {
	return s0;
    }

    /**
     * Get the path parameter for the start of a path
     * traversal.
     * @return the initial value of the path parameter;
     (         Double.NaN if the path is null
     */
    protected double getInitialU() {
	return u0;
    }

    /**
     * Get the current distance along the path at the time specified
     * in the last call to update.
     * The distance will be the one corresponding to the time returned
     * by {@link #getUpdatedTime()}.
     * @return the distance along the path
     * @see #update(double,long)
     */
    protected double getS() {
	return (path == null)? Double.NaN: s;
    }

    /**
     * Get the path parameter for a specific distance along the path
     * @param s the distance along the path from its start (negative
     *        distances or distances larger than the length of the
     *        path are allowed only for cyclic paths)
     *@return the value of the path parameter u; Double.NaN if no path set
     */
    public double getU(double s) {
	return (path == null)? Double.NaN: path.u(s);
    }

    /**
     * Get the current distance along the path from its start.
     * @param u the path parameter
     * @return the distance; Double.NaN if there is no path
     */
    public double getS(double u) {
	return (path == null)? Double.NaN: path.s(u);
    }


    private double pathInversionLimit = -1.0;	// signal not in use.

    /**
     * Get the inversion limit that will be used for
     * splines that are created by paths in order to determine the
     * path distance given the path parameter and vice versa.
     * The limit is used when a spline is created.
     * @return the path inversion limit; -1.0 if the default will
     *         be used.
     */
    public double getPathInversionLimit() {
	return pathInversionLimit;
    }

    /**
     * Set the inversion limit for a path.
     * This method  calls the method
     * {@link org.bzdev.geom.BasicSplinePath2D#setInversionLimit(double)}
      * thus modifying the behavior of the path.
     * @param limit the inversion limit; negative values if no limit is to
     *        be set
     */
    public void setPathInversionLimit(double limit) {
	if (limit < 0.0) {
	    pathInversionLimit = -1.0;
	} else {
	    pathInversionLimit = limit;
	    if (path != null) {
		path.setInversionLimit(limit);
	    }
	}
    }

    /**
     * Return the default value of the angle-relative flag.
     * This method returns <code>true</code> unless
     * overridden.
     * @return the default value for setPath methods
     * @see #setPath(AnimationPath2D,double)
     * @see #setPath(BasicSplinePath2D,double)
     */
    protected boolean defaultAngleRelative() {
	return true;
    }

    /**
     * Set the path, specified by an instance of AnimationPath2D.
     * This method calls
     * {@link #setPath(AnimationPath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0, an angle of 0.0, and the angle-relative flag set to its
     * default value (provided by the method defaultAngleRelative()).
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one is desired, it should be reset.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     *{@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * @param objectPath the path to set
     * @param time0 the increment from the current time
     *        at which the path should be set.
     */
    public final void setPath(AnimationPath2D objectPath, double time0) {
	setPath(objectPath, 0.0, 0.0, defaultAngleRelative(), time0);
    }

    /**
     * Set the path.
     * This method calls
     * {@link #setPath(BasicSplinePath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0, an angle of 0.0, and the angle-relative flag set to  its
     * default value (provided by the method defaultAngleRelative()).
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one is desired, it should be reset.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * @param path the path the object will follow
     * @param time0 the increment from the current time
     *        at which the path should be set.
     */
    public final void setPath(BasicSplinePath2D path, double time0) {
	setPath(path, 0.0, 0.0, defaultAngleRelative(), time0);
    }


    /**
     * Set the path, using an AnimationPath2D object to specify the
     * path and specifying the angle-relative flag.
     * This method calls
     * {@link #setPath(BasicSplinePath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0, and an angle of 0.0.
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one is desired, it should be reset.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     *
     * @param objectPath the object providing the path this object will follow
     * @param angleRelative true if the angle (0.0) is measured relative to the
     *        path's tangent; false if the angle is measured from the
     *        positive X axis in graph coordinate space
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(AnimationPath2D objectPath, boolean angleRelative,
			      double time0)
    {
	setPath(objectPath, 0.0, 0.0, angleRelative, time0);
    }

    /**
     * Set the path, specifying the angle-relative flag.
     * This method calls
     * {@link #setPath(BasicSplinePath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0 and an angle of 0.0.
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one is desired, it should be reset.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * @param path the path the object will follow
     * @param angleRelative true if the angle (0.0)is measured relative to the
     *        path's tangent; false if the angle is measured from the
     *        positive X axis in graph coordinate space
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(BasicSplinePath2D path, boolean angleRelative,
			      double time0)
    {
	setPath(path, 0.0, 0.0, angleRelative, time0);
    }


    /**
     * Set the path, using an AnimationPath2D object to specify the
     * path and specifying both the angle and the angle-relative flag.
     * This method calls
     * {@link #setPath(BasicSplinePath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0, and an angle of 0.0.
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one is desired, it should be reset.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * <P>
     * As a use case for this method, suppose an animation contains an image
     * for a large map and a path that represents a route, and the map is too
     * large to fit in a window. One can use this method to make a GraphView
     * follow the route (one will set the path velocity as well). By setting
     * the angle-relative field to true and the angle to -&pi;/2, the Y axis
     * will always appear to be tangent to the path and the map will seem to
     * rotate as the animation runs, mimicking a GPS display in a vehicle.
     * @param objectPath the object providing the path this object will follow
     * @param angle the initial angle
     * @param angleRelative true if the angle (0.0) is measured relative to the
     *        path's tangent; false if the angle is measured from the
     *        positive X axis in graph coordinate space
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(AnimationPath2D objectPath,
			      double angle,
			      boolean angleRelative,
			      double time0)
    {
	setPath(objectPath, 0.0, angle, angleRelative, time0);
    }

    /**
     * Set the path, specifying both the initial angle and the
     * angle-relative flag.
     * This method calls
     * {@link #setPath(BasicSplinePath2D,double,double,boolean,double)}
     * with appropriate arguments: an initial value of the path parameter u
     * of 0.0 and an angle of 0.0.
     * The path angle function will be set to null, as will the corresponding
     * simulation function. If one of these functions is desired, it
     * should be set after this method is called..
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * <P>
     * As a use case for this method, suppose an animation contains an image
     * for a large map and a path that represents a route, and the map is too
     * large to fit in a window. One can use this method to make a GraphView
     * follow the route (one will set the path velocity as well). By setting
     * the angle-relative field to true and the angle to -&pi;/2, the Y axis
     * will always appear to be tangent to the path and the map will seem to
     * rotate as the animation runs, mimicking a GPS display in a vehicle.
     * @param path the path the object will follow
     * @param angle the initial angle
     * @param angleRelative true if the angle (0.0)is measured relative to the
     *        path's tangent; false if the angle is measured from the
     *        positive X axis in graph coordinate space
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(BasicSplinePath2D path,
			      double angle, boolean angleRelative,
			      double time0)
    {
	setPath(path, 0.0, angle, angleRelative, time0);
    }

    /**
     * Set a path, specified by an instance of AnimationPath2D and
     * specifying an initial path parameter and angle for the
     * object to follow.
     * The object's position will held at the start of the path until
     * the object's time passes time0. The object's initial angle
     * will match that of a tangent to the path at the object's
     * reference point's current location.
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one of these functions is desired,
     * it should be set after this method is called.
     * <P>
     * To override the behavior that this class provides, one should
     * override
     * {@link #setPathImplementation(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}.
     * <P>
     * As a use case for this method, suppose an animation contains an image
     * for a large map and a path that represents a route, and the map is too
     * large to fit in a window. One can use this method to make a GraphView
     * follow the route (one will set the path velocity as well). By setting
     * the angle-relative field to true and the angle to -&pi;/2, the Y axis
     * will always appear to be tangent to the path and the map will seem to
     * rotate as the animation runs, mimicking a GPS display in a vehicle.
     * @param objectPath the object providing the path
     * @param u0 the path's parameter at a time equal to time0
     * @param angle the initial angle
     * @param angleRelative true if the angle is measured relative to
     *        path's tangent; false if the angle is absolute
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(AnimationPath2D objectPath, double u0,
			      double angle, boolean angleRelative,
			      double time0)
    {
	if (objectPath == null) {
	    setPath((BasicSplinePath2D)null, u0, angle, angleRelative, time0);
	    return;
	}
	if (Animation2D.level1 > -1) {
	    trace(Animation2D.level1,
		  "setPath - pathName=\"%s\", u0=%g, angle=%g (%g degrees), "
		  + "angleRelative=%b, time0=%g",
		  objectPath.getName(), u0, angle, Math.toDegrees(angle),
		  angleRelative, time0);
	}
	setPathImplementation(objectPath.getPath(), u0, angle,
			      angleRelative, time0);
	apath = objectPath;
    }

    /**
     * Set a path for this object to follow, specifying an initial
     * path parameter and angle.
     * The object's position will held at the start of the path
     * until the object's time passes time0. The object's
     * angle will match that of a tangent to the path at the object's
     * reference point's current location.
     * The path angle function will be set to null unless this behavior
     * is overridden by a subclass, as will the corresponding
     * simulation function. If one of these functions is desired, it
     * should be set after this method is called.
     * @param path the path the object will follow
     * @param u0 the path's parameter at a time equal to time0
     * @param angle the initial angle
     * @param angleRelative true if the angle is measured relative to
     *        path's tangent; false if the angle is absolute
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    public final void setPath(BasicSplinePath2D path, double u0,
			      double angle, boolean angleRelative,
			      double time0)
    {
	if (Animation2D.level1 > -1) {
	    trace(Animation2D.level1,
		  "setPath - path hashcode=%h, u0=%g, angle=%g (%g degrees), "
		  + "angleRelative=%b, time0=%g",
		  path, u0, angle, Math.toDegrees(angle),
		  angleRelative, time0);
	}
	setPathImplementation(path, u0, angle, angleRelative, time0);
    }

    /**
     * Set a path for this object to follow, specifying an initial
     * path parameter and angle.
     * The object's position will held at the start of the path
     * until the object's time passes time0. The object's
     * angle will match that of a tangent to the path at the object's
     * reference point's current location.
     * The path angle function will be set to null, as will the corresponding
     * simulation function. If one is desired, it should be reset
     * after setPath is called.
     * Subclasses that modify how paths are processed should override
     * this method and in most cases call super.setPathImplementation and,
     * of course, document any changes in behavior.
     * @param path the path the object will follow
     * @param u0 the path's parameter at a time equal to time0
     * @param angle the initial angle
     * @param angleRelative true if the angle is measured relative to
     *        path's tangent; false if the angle is absolute
     * @param time0 the increment from the current time
     *        at which the object starts moving along the path
     */
    protected void setPathImplementation(BasicSplinePath2D path, double u0,
					 double angle, boolean angleRelative,
					 double time0)
    {
	update();
	apath = null;
	this.path = path;
	if (path != null) {
	    if (pathInversionLimit >= 0.0) {
		path.setInversionLimit(pathInversionLimit);
	    }
	    this.angleRelative = angleRelative;
	    this.angleRelative0 = angleRelative;
	    this.pathAngle = angle;
	    this.pathAngle0 = angle;
	    pathAngleFunction = null;
	    pathAngleSimFunction = null;
	    this.time0 = animation.currentTime() + time0;
	    this.u0 = u0;
	    s = path.s(u0);
	    s0 = s;
	    double currentAngle;
	    if (angleRelative) {
		double tangentAngle = Math.atan2(path.dyDu(u0), path.dxDu(u0));
		currentAngle = pathAngle + tangentAngle;
	    } else {
		currentAngle = angle;
	    }
	    double x = path.getX(u0);
	    double y = path.getY(u0);
	    super.setPosition(x, y, currentAngle);
	    if (Animation2D.level1 > -1) {
		trace(Animation2D.level1,
		      "    ...(setPath) new position - x=%g, y=%g, angle=%g "
		      + "(%g degrees)",
		      x, y, currentAngle, Math.toDegrees(currentAngle));
	    }
	}
    }

    /**
     * Test if the angleRelative flag was set when the current path was
     * set.
     * When angleRelative flag the angle function, angular velocity, and
     * angular acceleration are assumed to be computed relative to the
     * tangent of the path.  If the angular velocity is zero, the actual
     * angle (the one returned by getAngle()) will be equal to the angle
     * of the tangent to the path.
     * @return true if the angleRelative flag was set; false if it was
     *         not or if there is no path
     */
    public boolean angleIsRelative() {
	return (path != null) && angleRelative;
    }

    /**
     * Set whether or not the angle is relative to the current path.
     * The setPath methods will also set whether or not the angle is
     * relative to the current path. This method allows that to be
     * changed. When the angle-relative flag's value changes, the
     * path angle will be adjusted so that the object does not
     * immediately rotate.
     * @param relative true if the angle is relative to the current path;
     *        false otherwise
     * @exception IllegalStateException no path has been defined
     */
    public void setAngleRelative(boolean relative) {
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotDefined"));
	if (relative && !angleRelative) {
	    double u = (s == s0)? u0: getU();
	    double tangentAngle = Math.atan2(path.dyDu(u), path.dxDu(u));
	    pathAngle -= tangentAngle;
	    angleRelative = relative;
	} else if (!relative && angleRelative) {
	    double u = (s == s0)? u0: getU();
	    double tangentAngle = Math.atan2(path.dyDu(u), path.dxDu(u));
	    pathAngle += tangentAngle;
	    angleRelative = relative;
	}
	// for the other two cases, there is nothing to do.
    }

    @Override
    public void setPosition(double x, double y, double angle) {
	update();
	super.setPosition(x, y, angle);
	if (path == null) {
	    pathAngle = angle;
	} else {
	    if (angleRelative) {
		double u = (s == s0)? u0: getU();
		double tangentAngle = Math.atan2(path.dyDu(u), path.dxDu(u));
		pathAngle = angle - tangentAngle;
	    } else {
		pathAngle = angle;
	    }
	}
    }

    @Override
    public void setAngle(double angle) {
	update();
	super.setAngle(angle);
	if (path == null) {
	    pathAngle = angle;
	} else {
	    if (angleRelative) {
		double u = (s == s0)? u0: getU();
		double tangentAngle = Math.atan2(path.dyDu(u), path.dxDu(u));
		pathAngle = angle - tangentAngle;
	    } else {
		pathAngle = angle;
	    }
	}
    }

    /**
     * Set angle to use when traversing the current path.
     * The path angle is relative to the path if the angleRelative flag
     * was set to true when the path was constructed or during a
     * subsequent call to {@link #setAngleRelative(boolean)}; otherwise
     * the angle is an absolute angle measured counterclockwise from the
     * positive x axis.
     * @param angle the angle in radians
     * @exception IllegalStateException a path does not exist
     */
    public void setPathAngle(double angle) {
	update();
	if (path == null)
	    throw new IllegalStateException(errorMsg("pathNotDefined"));
	pathAngle = angle;
	if (angleRelative) {
	    double u = (s == s0)? u0: getU();
	    double tangentAngle = Math.atan2(path.dyDu(u), path.dxDu(u));
	    super.setAngle(pathAngle - tangentAngle);
	} else {
	    super.setAngle(pathAngle);
	}
    }

    /**
     * Set the angular velocity.
     * Traversing a path will set this value to the current path angular
     * velocity. The changes begin when path traversal starts.
     * @see #setPathAngularVelocity(double)
     * @param angularVelocity the angular velocity in radians per unit time
     *        (unit time is typically a second)
     */
    public void setAngularVelocity(double angularVelocity) {
	update();
	this.angularVelocity = angularVelocity;
    }

    /**
     * Set the angular acceleration.
     * Traversing a path will set this value to the current path angular
     * acceleration. The changes begin when path traversal starts.
     * @param angularAcceleration the angular acceleration in radians per
     * unit time per unit time (unit time is typically a second)
     * @see #setPathAngularAcceleration(double)
     */
    public void setAngularAcceleration(double angularAcceleration) {
	update();
	this.angularAcceleration = angularAcceleration;
    }

    /**
     * Get the current angular velocity.
     * This is the value provided in the last call to
     * {@link #setAngularVelocity(double)}, possibly updated to the current
     * path angular velocity if a path is being traversed.
     * @return the angular velocity in radians per unit time
     *         (unit time is typically one second)
     */
    public double getAngularVelocity() {
	update();
	return angularVelocity;
    }

    /**
     * Clear the path.
     * This will also clear the distance function (if any),
     * the path angle function (if any), and set both the path velocity
     * and path acceleration to zero.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     */
    public void clearPath() {
	update();
	path = null;
	distFunction = null;
	distSimFunction = null;
	pathAngleFunction = null;
	pathAngleSimFunction = null;
	pathVelocity = 0.0;
	pathAccel = 0.0;
	s0 = Double.NaN;
	u0 = Double.NaN;
	time0 = Double.NaN;
	angleRelative = false;
	pathAngle = 0.0;
    }

    /**
     * Set the function giving the distance along the path as a function
     * of time, setting the function at a specific simulation time.
     * The distance along the path will change when the object's time is
     * updated.
     * When a distance function is provided, the path velocity and
     * acceleration is ignored.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param f the function providing the distance along the path
     */
    public void setDistanceByF(final RealValuedFunctOps f) {
	update();
	if (f == null) {
	    distFunction = null;
	} else if (f instanceof RealValuedFunction) {
	    distFunction = (RealValuedFunction)f;
	} else {
	    distFunction = new RealValuedFunction(f);
	}
	distSimFunction = null;
    }

    /**
     * Set the simulation function giving the distance along the path as
     * a function of time, setting the function at a specific simulation time.
     * The distance along the path will change when the object's time is
     * updated.
     * When a distance function is provided, the path velocity and
     * acceleration is ignored.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param f the function providing the distance along the path
     */
    public void setDistanceBySF(final SimFunction f) {
	update();
	distFunction = (f== null)? null: f.getFunction();
	distSimFunction = f;
    }

    /*
     * Constant providing a spline that will cause an object's angle
     * to point along the current tangent to its path in the direction
     * of increasing distance.
     public static final CubicSpline TANGENT_TO_PATH
	= new org.bzdev.math.CubicSpline1(new double[4], 0.0, 1.0);

     * Constant providing a spline that will cause an object's angle
     * to point along the current tangent to its path in the direction
     * of decreasing distance.

    public static final CubicSpline REVERSED_TANGENT_TO_PATH
	= new org.bzdev.math.CubicSpline1(new double[4], 0.0, 1.0);
     */

    /**
     * Set the function giving the angle of an object as a function
     * of simulation time.
     * When a path is being traversed, this angle
     * function will be ignored if a function provided by
     * {@link #setPathAngleByF(RealValuedFunctOps)} or
     * {@link #setPathAngleBySF(SimFunction)} exists at the current time.
     * Similarly, when an angle function is provided, the path angular
     * velocity and angular acceleration are not used.  The angle will
     * change as the object's time is updated.
     * <P>
     * This method calls {@link AnimationObject2D#update()}. The function
     * provided must return an angle in units of radians.
     * @param f the function providing the angle as a function of time
     */
    public void setAngleByF(RealValuedFunctOps f) {
	update();
	if (f == null) {
	    angleFunction = null;
	} else if (f instanceof RealValuedFunction) {
	    angleFunction = (RealValuedFunction)f;
	} else {
	    angleFunction = new RealValuedFunction(f);
	}
	angleSimFunction = null;
    }

    /**
     * Set the simulation function giving the angle of an object as a
     * simulation function of simulation time.
     * When a path is being traversed, this angle
     * function will be ignored if a function provided by
     * {@link #setPathAngleByF(RealValuedFunctOps)} or
     * {@link #setPathAngleBySF(SimFunction)} exists at the current time.
     * Similarly, when an angle function is provided, the path angular
     * velocity and angular acceleration are not used.  The angle will
     * change as the object's time is updated.
     * <P>
     * This method calls {@link AnimationObject2D#update()}. The function
     * provided must return an angle in units of radians.
     * @param f the function providing the angle as a function of time
     */
    public void setAngleBySF(SimFunction f) {
	update();
	angleFunction = (f == null)? null: f.getFunction();
	angleSimFunction = f;
    }

    /**
     * Set the function giving the angle of an object as a function
     * of time measured from the point at which the traversal of a path
     * begins.
     * When {@link #angleIsRelative} returns true, the angle
     * function refers to the change in angle relative to the tangent
     * at the current location along a path.
     * When an angle function is provided, the path angular velocity
     * and angular acceleration is not used.
     * The angle will change as the object's time is updated. This function
     * has no effect before a path's traversal starts. If the path traversal
     * has started and a path is configured, this function will be used
     * instead of a function provided by
     * {@link #setAngleByF(RealValuedFunctOps)}.
     * <P>
     * This method calls {@link AnimationObject2D#update()}. The function
     * provided must return an angle in units of radians.
     * @param f the function providing the angle as a function of time
     */
    public void setPathAngleByF(RealValuedFunctOps f) {
	update();
	if (f == null) {
	    pathAngleFunction = null;
	} else if (f instanceof RealValuedFunction) {
	    pathAngleFunction = (RealValuedFunction)f;
	} else {
	    pathAngleFunction = new RealValuedFunction(f);
	}
	angleSimFunction = null;
    }

    /**
     * Set the simulation function giving the angle of an object as a function
     * of time measured from the point at which the traversal of a path
     * begins.
     * When {@link #angleIsRelative} returns true, the angle
     * function refers to the change in angle relative to the tangent
     * at the current location along a path.
     * When an angle function is provided, the path angular velocity
     * and angular acceleration is not used.
     * The angle will change as the object's time is updated. This function
     * has no effect before a path's traversal starts. If the path traversal
     * has started and a path is configured, this function will be used
     * instead of a function provided by
     * {@link #setAngleByF(RealValuedFunctOps)}.
     * <P>
     * This method calls {@link AnimationObject2D#update()}. The function
     * provided must return an angle in units of radians.
     * @param f the function providing the angle as a function of time
     */
    public void setPathAngleBySF(SimFunction f) {
	update();
	pathAngleFunction = (f == null)? null: f.getFunction();
	pathAngleSimFunction = f;
    }

    /**
     * Set the velocity along the path.
     * The value will change as the object's time is updated.
     * When a distance function is provided, the path velocity and
     * acceleration is ignored.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param pathVelocity the velocity of the object along the path.
     */
    public void setPathVelocity(double pathVelocity) {
	update();
	this.pathVelocity = pathVelocity;
    }

    /**
     * Set the acceleration along the path.
     * The value will change as the object's time is updated.
     * When a distance function is provided, the path velocity and
     * acceleration is ignored.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param pathAccel the object's acceleration along the path
     */
    public void setPathAcceleration(double pathAccel) {
	update();
	this.pathAccel = pathAccel;
    }

    /**
     * Set the angular velocity along the path.
     * When {@link #angleIsRelative} returns true, the angular
     * velocity refers to the change in angle relative to the tangent
     * at the current location along a path.
     * The angle will change as the object's time is updated.
     * When an angle function is provided, the path angular velocity
     * and angular acceleration is not used.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param pathAngularVelocity the angular velocity of the object while
     *        traversing the  path
     */
    public void setPathAngularVelocity(double pathAngularVelocity) {
	update();
	// this.pathAngularVelocity = pathAngularVelocity;
	angularVelocity = pathAngularVelocity;
    }

    /**
     * Set the angular acceleration along the path
     * When {@link #angleIsRelative} returns true, the angular
     * acceleration refers to the change in angle relative to the tangent
     * at the current location along a path.
     * The angle will change as the object's time is updated.
     * When an angle function is provided, the path angular velocity
     * and angular acceleration is not used.
     * <P>
     * This method calls {@link AnimationObject2D#update()}.
     * @param pathAngularAccel the object's angular acceleration while
     *        traversing the path
     */
    public void setPathAngularAcceleration(double pathAngularAccel) {
	update();
	// this.pathAngularAccel = pathAngularAccel;
	angularAcceleration = pathAngularAccel;
    }

    /**
     * Get the x coordinate for a point along a path.
     * @param u the path parameter
     * @return the x coordinate; 0.0 if there is no path
     */
    protected double getPathX(double u) {
	return (path == null)? 0.0: path.getX(u);
    }

    /**
     * Get the y coordinate for a point along a path.
     * @param u the path parameter
     * @return the y coordinate; 0.0 if there is no path
     */
    public double getPathY(double u) {
	return (path == null)? 0.0: path.getY(u);
    }

    /**
     * Get the derivative of the x coordinate for a point along a path
     * with respect to the path parameter.
     * @param u the path parameter
     * @return the derivative of the x coordinate; 0.0 if there is no path
     */
    public double getPathDxDu(double u) {
	return (path == null)? 0.0: path.dxDu(u);
    }

    /**
     * Get the derivative of the y coordinate for a point along a path
     * with respect to the path parameter.
     * @param u the path parameter
     * @return the derivative of the y coordinate; 0.0 if there is no path
     */
    public double getPathDyDu(double u) {
	return (path == null)? 0.0: path.dyDu(u);
    }

    /**
     * Get the maximum value of the path parameter.
     * For a closed path, this is the first positive value of the path
     * parameter at which the path repeats itself.
     * @return the maximum value of the path parameter; 0.0 if there is no path
     */
    public double getMaxPathParameter() {
	return (path == null)? 0.0: path.getMaxParameter();
    }

    /**
     * Get the length of the path.
     * @return the length of the path; 0.0 if there is no path
     */
    public double getPathLength() {
	return (path == null)? 0.0: path.getPathLength();
    }

    /**
     * Get the length of the path parameter between an initial and final
     * value of the path parameter.
     * The returned value is the same if u1 and u2 are exchanged:
     * lengths are non-negative.
     * @param u1 the initial path parameter
     * @param u2 the final path parameter
     * @return the length of the path; 0.0 if there is no path
     */
    public double getPathLength(double u1, double u2) {
	return (path == null)? 0.0: path.getPathLength(u1, u2);
    }


    /**
     * Get the distance traversed on a subpath.
     * If u2 &lt; u1, the value returned is negative.
     * <P>
     * This is intended for cases where direction is important.
     * @param u1 the parameter at the first end point of a subpath
     * @param u2 the parameter at the second end point of a subpath
     * @return the distance
     * @exception IllegalStateException the path is not a simple path
     * @exception IllegalArgumentException the parameter is out of bounds
     */
    public double getDistance(double u1, double u2)
	throws IllegalStateException, IllegalArgumentException
    {
	return (path == null)? 0.0: path.getDistance(u1, u2);
    }

    /**
     * Determine if the path is closed.
     * @return true if the path exists and is closed; false otherwise
     */
    public boolean isClosedPath() {
	return (path != null) && path.isClosed();
    }

    /**
     * Determine if the path exists.
     * @return true if the path exists; false otherwise.
     */
    public boolean pathExists() {
	return (path != null);
    }

    /**
     * Get the path angle.
     * When {@link #angleIsRelative()} returns true, the path angle is
     * measured relative to the path's tangent in the direction of
     * increasing distance.  Otherwise the path angle is measured from
     * the positive x axis.  The path angle is used by this class only
     * when a path is set, and the path angle is set when a path is
     * set.
     * @return the path angle
     * @see #setPath(AnimationPath2D, double)
     * @see #setPath(org.bzdev.geom.BasicSplinePath2D,double)
     * @see #setPath(AnimationPath2D, double,double,boolean,double)
     * @see #setPath(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)
     */
    public double getPathAngle() {
	return pathAngle;
    }

    /**
     * Get the path velocity.
     * @return the speed along the path at the current time
     */
    public double getPathVelocity() {return pathVelocity;}

    /**
     * Get the path acceleration.
     * @return the current value of the acceleration.
     */
    public double getPathAcceleration() {return pathAccel;}

    /**
     * Get the initial time at which a path traversal will start.
     * When finite, this is the argument named time0 that was used
     * when the last setPath method was called added to the simulation
     * time when that setPath method was called.
     * @return the time at which a path traversal will start;
     *         Double.POSITIVE_INFINITY if there is no path
     */
    public double getInitialPathTime() {
	return (path == null)? Double.POSITIVE_INFINITY: time0;
    }


    /**
     * Update the object's state to that at a specified time.
     * The values of t passed to this method should form an increasing
     * sequence.  The default implementation's behavior depends on
     * whether functions are provided. A distance function, path
     * velocity, path acceleration, or angle function is not used when
     * there is no path, although the angular acceleration and
     * angular velocity will be used if these are nonzero.
     * <OL>
     *  <li> If a distance function is provided, that function is
     *       used to determine the new value of s (the distance
     *       along the path from its start) at time &tau;, with
     *       the path velocity and acceleration determined by that
     *       function's first and second derivatives (if not implemented,
     *       the path velocity and/or acceleration is set to
     *       Double.NaN). The time &tau; is the difference between the
     *       current time t and the time time0 at which the path
     *       traversal starts.
     *  <li> If a distance function is not provided, the path
     *       velocity and path acceleration are used to determine
     *       the new value of s, and the path velocity is updated as
     *       well.
     *  <li> If an angle function is provided, that function is
     *       used to determine the new angle, and its derivatives, if
     *       implemented provide the angular velocity and
     *       angular acceleration. The time argument $tau; is the
     *       difference between the current time t and the time time0
     *       at which the path traversal starts. If the path was
     *       set with the flag angleRelative set to true, the angle is
     *       added to the tangent at the object's new position along the
     *       path; otherwise the angle is measured counterclockwise
     *       from the direction of the positive X axis.
     *  <li> If an angle function is not provided the path angular velocity
     *       and path angular acceleration are used to compute the
     *       change in angle from the last time update was called.
     *       If the path was set with the flag angleRelative set to true,
     *       the angle is added to the angle of the tangent to
     *       the path at its new location; otherwise the angle is measured
     *       counterclockwise from the direction of the positive X axis.
     * </OL>
     * The angleRelative flag is the boolean argument to
     * {@link #setPath(org.bzdev.geom.BasicSplinePath2D,double,double,boolean,double)}
     * or
     * {@link #setPath(AnimationPath2D,double,double,boolean,double)}, but
     * may be subsequently changed by calling
     * {@link #setAngleRelative(boolean)}.
     * <P>
     * If a velocity or acceleration has the value Double.NaN, it is treated
     * as if its value were 0.0.
     * In cases where one needs to obtain the previous value of s, one
     * can override this method and obtain the previous value by calling
     * {@link #getS()} before calling {@link #update(double,long) super.update}.
     * @param t the time for which the object should be calculated.
     * @param simtime the simulation time
     */
    protected void update(double t, long simtime) {
	// Our superclasses do not provide this method up to
	// SimObject, whose implementation does nothing, so we
	// don't have to call update on our superclass.
	//
	// If path is null, one will just call setPosition to put
	// the object in the right place.
	if (path != null && t >= time0) {
	    if (time < time0) {
		// time = time0;
		// update the time between paths (or until the
		// first path) by acting like no
		// path is configured. Motion is not possible without a path
		// except by explicitly setting the position,
		// but rotations may still occur.
		BasicSplinePath2D savedPath = path;
		path = null;
		update(time0, animation.getTicks(time0));
		path = savedPath;
	    }
	    if (t >= time0) {
		if (t <= time && simtime <= stime) {
		    // we've already updated this to the current time so
		    // there is nothing to do.
		    return;
		}
		double arg = t - time0;
		if (distFunction == null) {
		    double deltat = t - time;
		    double deltav =
			(Double.isNaN(pathAccel)? 0.0:pathAccel) * deltat;
		    double deltas =
			(Double.isNaN(pathVelocity)? 0.0:pathVelocity) * deltat
			+ 0.5 * deltav * deltat;
		    double newS = s + deltas;
		    double plen = path.getPathLength();
		    if (path.isClosed() || (newS >= 0.0  && newS < plen)) {
			s = newS;
			pathVelocity += deltav;
		    } else {
			if (newS > plen) s = plen;
			if (newS < 0.0) s = 0.0;
			pathVelocity = 0.0;
			pathAccel = 0.0;
		    }
		} else {
		    double dm = distFunction.getDomainMax();
		    if (arg > dm ) {
			arg = dm;
		    }
		    s = distFunction.valueAt(arg);
		    try {
			pathVelocity = distFunction.derivAt(arg);
		    } catch (UnsupportedOperationException e) {
			pathVelocity = Double.NaN;
		    }
		    try {
			pathAccel = distFunction.secondDerivAt(arg);
		    } catch (UnsupportedOperationException e) {
			pathAccel = Double.NaN;
		    }
		}
		double angle;
		if (angleFunction == null && pathAngleFunction == null) {
		    double deltat = t - time;
		    double deltaw =
			(Double.isNaN(angularAcceleration)? 0.0:
			 angularAcceleration) * deltat;
		    double deltaa =
			(Double.isNaN(angularVelocity)? 0.0:
			 angularVelocity)
			* deltat + 0.5 * deltaw * deltat;
		    pathAngle += deltaa;
		    angularVelocity += deltaw;
		    angle = pathAngle;
		} else if (pathAngleFunction != null) {
		    double dm = pathAngleFunction.getDomainMax();
		    if (arg > dm) {
			arg = dm;
		    }
		    pathAngle = pathAngleFunction.valueAt(arg);
		    angle = pathAngle;
		    try {
			angularVelocity = pathAngleFunction.derivAt(arg);
		    } catch (UnsupportedOperationException e) {
			angularVelocity = Double.NaN;
		    }
		    try {
			angularAcceleration =
			    pathAngleFunction.secondDerivAt(arg);
		    } catch (UnsupportedOperationException e) {
			angularAcceleration = Double.NaN;
		    }
		} else {
		    // angle function is not null but path angle function is null
		    arg = t;
		    double dm = angleFunction.getDomainMax();
		    if (arg > dm) {
			arg = dm;
		    }
		    pathAngle = angleFunction.valueAt(arg);
		    angle = pathAngle;
		    try {
			angularVelocity = angleFunction.derivAt(arg);
		    } catch (UnsupportedOperationException e) {
			angularVelocity = Double.NaN;
		    }
		    try {
			angularAcceleration = angleFunction.secondDerivAt(arg);
		    } catch (UnsupportedOperationException e) {
			angularAcceleration = Double.NaN;
		    }
		}
		double u = path.u(s);
		if (Animation2D.level3 > -1) {
		    trace(Animation2D.level3,
			  "update - u=%g, v=%g, a=%g, angularV=%g, angularA=%g",
			  u, pathVelocity, pathAccel,
			  angularVelocity, angularAcceleration);
		}
		if (angleRelative) {
		    double tangent = Math.atan2(path.dyDu(u),path.dxDu(u));
		    if (Double.isNaN(tangent)) {
			// something is wrong, so let's not change the
			// angle.
			double newxx = path.getX(u);
			double newyy = path.getY(u);
			double theAngle = getAngle();
			super.setPosition(newxx, newyy, theAngle);
			if (Animation2D.level2 > -1) {
			    trace(Animation2D.level2,
				  "update - x = %g, y=%g, angle = %g "
				  + "(%g degrees)",
				  newxx, newyy, theAngle,
				  Math.toDegrees(theAngle));
			}
			time = t;
			stime = simtime;
			return;
		    }
		    if (angleFunction == null || pathAngleFunction != null) {
			angle += tangent;
		    } else {
			pathAngle -= tangent;
		    }
		}
		double newx = path.getX(u);
		double newy = path.getY(u);
		super.setPosition(newx, newy, angle);
		// angularVelocity = pathAngularVelocity;
		// angularAcceleration = pathAngularAccel;
		if (Double.isNaN(angularVelocity)) angularVelocity = 0.0;
		if (Animation2D.level2 > -1) {
		    trace(Animation2D.level2,
			  "update - x=%g, y=%g, angle=%g (%g degrees)",
			  newx, newy, angle, Math.toDegrees(angle));
		}
	    }
	} else if (path == null) {
	    if (angularVelocity != 0.0 || angularAcceleration != 0.0) {
		double delta = t-time;
		double term1, term2;
		if (angularAcceleration == 0.0) {
		    term1 = 0.0;
		    term2 = (angularVelocity == 0.0)? 0.0:
			angularVelocity * delta;
		} else {
		    term1 = angularAcceleration * delta;
		    term2 = ((angularVelocity == 0.0)? 0.0:
			     angularVelocity*delta) + term1*delta/2.0;
		}
		angularVelocity += term1;
		super.setAngle(getAngle() + term2);
	    }
	}
	stime = simtime;
	time = t;
    }

    /**
     * {@inheritDoc}
     * Defined in {@link DirectedObject2D}:
     * <UL>
     *  <LI> the path this object will follow: either the name of an
     *       instance of AnimationPath2D or the configuration of a
     *       BasicSplinePath2D as provided by its method
     *       {@link BasicSplinePath2D#printTable(String,Appendable)}.
     *  <LI> the distance along a path for its starting point (the initial
     *       distance).
     *  <LI> the initial value of the path's parameter.
     *  <LI> the time at which a path traversal should start.
     *  <LI> the initial value for whether or not the angle for the object
     *       is relative to the path's tangent or absolute.
     *  <LI> the distance function.
     *  <LI> a flag indicating if the distance function is set.
     *  <LI> the path velocity.
     *  <LI> the path acceleration.
     *  <LI> the path inversion limit (described in the documentation for
     *       {@link #getPathInversionLimit()}.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   java.io.PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	if (path == null) {
	    out.println(prefix + "path: (none)");
	} else {
	    if (apath != null) {
		out.println(prefix + "path: " + apath.getName());
	    } else {
		out.println(prefix + "path:");
		path.printTable(prefix + "    ", out);
	    }
	    out.println(prefix + "initial path distance: " + getInitialS());
	    out.println(prefix + "initial path parameter: "
			+ getInitialU());
	    out.println(prefix + "initial path-traversal time: " + time0);
	    out.println(prefix + "initial angle-relative flag: "
			+ angleRelative0);
	    out.println(prefix + "initial path angle: " + pathAngle0);

	    if (distSimFunction != null) {
		out.println(prefix + "distance function: "
			    + distSimFunction.getName());
	    } else if (distFunction != null) {
		out.println(prefix + "distance function is set");
	    }
	    if (distFunction == null) {
		if (pathAccel == 0.0) {
		    out.println(prefix + "path velocity: "
				+ getPathVelocity());
		}
		out.println(prefix + "path acceleration: "
				+ getPathAcceleration());
	    }
	}
	out.println(prefix + "path inversion limit: "
		    + getPathInversionLimit());
    }

    /**
     * {@inheritDoc}
     * Defined in {@link DirectedObject2D}:
     * <UL>
     *   <LI> the name of the path (if any) this object can follow.
     *   <LI> the path parameter.
     *   <LI> the path distance.
     *   <LI> the angle-relative flag.
     *   <LI> the velocity along the path (NaN if undefined). When a
     *        distance function is used, its derivative, if implemented,
     *        determines this value.
     *   <LI> the acceleration along the path (NaN if undefined).  When a
     *        distance function is used, its second derivative, if implemented,
     *        determines this value.
     *   <LI> the path angle. The angle is measured relative to the path's
     *        tangent in the direction of an increasing path parameter when
     *        the angle-relative flag is true; otherwise it is measured
     *        relative to the X axis.  Angles are measured counterclockwise.
     *   <LI> the angular velocity.  When a path-angle function is used,
     *        its derivative, if implemented, determines this value.
     *   <LI> the angular acceleration. When a path-angle function is used,
     *        its derivative, if implemented, determines this value.
     *   <LI> the angular acceleration. When a path-angle function is used,
     *        its derivative, if implemented, determines this value.
     *   <LI> the time at which this data was last updated.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	if (path == null) {
	    out.println(prefix + "path: (none)");
	} else {
	    if (apath != null) {
		out.println(prefix + "path: " + apath.getName());
	    } else {
		out.println(prefix + "path:");
		path.printTable(prefix + "    ", out);
	    }
	    out.println(prefix + "path parameter (u): " + getU());
	    out.println(prefix + "path distance (s): " +  getS());
	    out.println(prefix + "angle-relative flag: "
			+ angleIsRelative());
	    out.println(prefix + "velocity along the path: "
			+ pathVelocity);
	    out.println(prefix + "acceleration along the path: "
			+ pathAccel);
	    out.println(prefix + "path angle: " + pathAngle + " radians ("
			+ Math.toDegrees(pathAngle) + "\u00B0)");
	    out.println(prefix + "angular velocity: "
			+ angularVelocity
			+ " radians/sec (" + Math.toDegrees(angularVelocity)
			+ "\u00B0 per second)");
	    out.println(prefix + "angular acceleration: "
			+ angularAcceleration + " radians/s\u00B2 ("
			+ Math.toDegrees(angularAcceleration) + "\u00B0)");
	}
	out.println(prefix + "time at last update = " + time);
    }
}

//  LocalWords:  exbundle config IllegalArgumentException getObject
//  LocalWords:  AnimationObject NaN setPath getUpdatedTime versa lt
//  LocalWords:  setInversionLimit AnimationPath boolean objectPath
//  LocalWords:  setPathImplementation BasicSplinePath angleRelative
//  LocalWords:  pathName hashcode getAngle IllegalStateException OL
//  LocalWords:  pathNotDefined setAngleRelative angularVelocity li
//  LocalWords:  setPathAngularVelocity angularAcceleration pathAccel
//  LocalWords:  setPathAngularAcceleration setAngularVelocity getS
//  LocalWords:  CubicSpline setPathAngleByF RealValuedFunction
//  LocalWords:  setPathAngleBySF SimFunction angleIsRelative subpath
//  LocalWords:  setAngleByF pathVelocity pathAngularVelocity simtime
//  LocalWords:  pathAngularAccel superclasses SimObject superclass
//  LocalWords:  setPosition angularV angularA DirectedObject iPrefix
//  LocalWords:  printTable Appendable getPathInversionLimit
//  LocalWords:  printName defaultAngleRelative GraphView Subclasses
