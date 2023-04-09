package org.bzdev.p3d;
import org.bzdev.graphs.Graph;
import org.bzdev.math.RealValuedFunction;
import org.bzdev.math.RealValuedFunctOps;
import org.bzdev.devqsim.SimFunction;
import org.bzdev.anim2d.*;
import java.awt.Graphics2D;
import java.awt.Color;

//@exbundle org.bzdev.p3d.lpack.P3d

/**
 * Class for creating an animated view of a 3D model.
 * The model's default orientation has the x axis pointing left, the
 * y axis pointing up, and z axis pointing towards the viewer.  It is
 * convenient to be able to create a video showing the model in various
 * orientations.
 * <P>
 * To create a video of a 3D model m3d, one will first create an
 * animation a2d of type Animation2D.
 * <pre><code>
 *      Model3D m3d = new Model3D();
 *      // add elements to the model
 *      ...
 *      Animation2D a2d = new Animation2D();
 *      Model3DView view = new Model3DView(a2d, "model", true);
 *      view.setModel(m3d);
 *      // Orient the model and set how the view will change over
 *      // time.
 *      ...
 *
 *      // The view must be made visible
 *      view.setVisible(true);
 *
 *      // Finally set up the animation and run it.
 *      int maxFrameCount = a2d.estimateFrameCount(...);
 *      a2d.initFrames(maxFrameCount, ".../img-", "png");
 *      a2d.scheduleFrames(0, maxFrameCount);
 *      a2d.run();
 * </CODE></PRE>
 *
 * There should usually be only one Model3DView that is visible
 * at a time because a Model3DView will manipulate the light source,
 * viewing angle, magnification, etc. Two that are competing with each
 * other will cause unpredictable results.
 */
public class Model3DView extends AnimationObject2D {

    private Model3D.Image image;
    private double lastTime;

    static String errorMsg(String key, Object... args) {
	return P3dErrorMsg.errorMsg(key, args);
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
    public Model3DView(Animation2D animation, String name, boolean intern) {
	super(animation, name, intern);
	image = new Model3D.Image(animation);
	lastTime = animation.currentTime();
    }

    private Model3D model;
    private double time0 = 0.0;

    /**
     * Set the model.
     * This must be called with a non-null value before this object is used
     * in an animation.
     * @param model the model
     */
    public void setModel(Model3D model) {
	this.model = model;
    }

    /**
     * Set the initial viewing time.
     * When Real-valued functions are used, the argument passed to
     * the function will be the difference between the simulation time and
     * the initial viewing time.
     * @param time0 the initial viewing time
     */
    public void setInitialViewingTime( double time0) {
	this.time0 = time0;
    }

    private boolean changeScale = true;

    /**
     * Set whether or not the scale will be recomputed each time
     * {@link #addTo(Graph,Graphics2D,Graphics2D)} is called.
     * A value of false will not prevent the scale from being initialized.
     * @param changeScale true if the scale will always be recomputed; false
     *        otherwise
     */
    public void setChangeScale(boolean changeScale) {
	this.changeScale = changeScale;
    }

    boolean forceSC = false;
    /**
     * Force the scale to be recomputed the next time
     * {@link #addTo(Graph,Graphics2D,Graphics2D)} is called.
     */
    public void forceScaleChange() {
	forceSC = true;
    }


    /**
     * Get whether or not the scale will be recomputed each time
     * {@link #addTo(Graph,Graphics2D,Graphics2D)} is called.
     * A value of false will not prevent the scale from being initialized.
     * @return true if the scale will always be recomputed; false
     *        otherwise
     */
    public boolean getChangeScale() {
	return changeScale;
    }


    private double theta = 0.0;
    private double phi = 0.0;
    private double psi = 0.0;
    private double border = 0.0;
    private double magnification = 1.0;
    private double xfract = 0.0;
    private double yfract = 0.0;
    private double lsPhi = 0.0;
    private double lsTheta = 0.0;


    private RealValuedFunction thetaFunction = null;
    private RealValuedFunction phiFunction = null;
    private RealValuedFunction psiFunction = null;
    private RealValuedFunction magFunction = null;
    private RealValuedFunction xfractFunction = null;
    private RealValuedFunction yfractFunction = null;
    private RealValuedFunction lsPhiFunction = null;
    private RealValuedFunction lsThetaFunction = null;

    // Following SimFunction definitions used only by printConfiguration
    private SimFunction thetaSimFunction = null;
    private SimFunction phiSimFunction = null;
    private SimFunction psiSimFunction = null;
    private SimFunction magSimFunction = null;
    private SimFunction xfractSimFunction = null;
    private SimFunction yfractSimFunction = null;
    private SimFunction lsPhiSimFunction = null;
    private SimFunction lsThetaSimFunction = null;


    private double thetaRate = 0.0;
    private double phiRate = 0.0;
    private double psiRate = 0.0;
    private double xfractRate = 0.0;
    private double yfractRate = 0.0;
    private double magRate = 0.0;
    private double lsPhiRate = 0.0;
    private double lsThetaRate = 0.0;

    /**
     * Get the Eulerian angle &phi; for a coordinate rotation.
     * @return the angle in radians
     */
    public double getPhi() {return phi;}

    /**
     * Get the function to compute the Eulerian angle &phi; as a function of
     * time.
     * The time is in units of seconds. When not null, the value computed
     * by this function will override the values given explicitly or modified
     * by a rate of change.
     * @return the function; null if there is none
     */
    public RealValuedFunction getPhiFunction() {return phiFunction;}

    /**
     * Get the Eulerian angle &theta; for a coordinate rotation.
     * @return the angle in radians
     */
    public double getTheta() {return theta;}

    /**
     * Get the function to compute the Eulerian angle &theta; as a function of
     * time.
     * The time is in units of seconds. When not null, the value computed
     * by this function will override the values given explicitly or modified
     * by a rate of change.
     * @return the function; null if there is none
     */
    public RealValuedFunction getThetaFunction() {return thetaFunction;}

    /**
     * Get the Eulerian angle &psi; for a coordinate rotation.
     * @return the angle in radians
     */
    public double getPsi() {return psi;}

    /**
     * Get the function to compute the Eulerian angle &psi; as a function of
     * time.
     * The time is in units of seconds. When not null, the value computed
     * by this function will override the values given explicitly or modified
     * by a rate of change.
     * @return the function; null if there is none
     */
    public RealValuedFunction getPsiFunction() {return psiFunction;}


    /**
     * Rotate the Model3D coordinates.
     * When all values are zero, the z access is perpendicular to
     * an image, the x axis is horizontal, and the y axis is vertical,
     * all following the standard mathematical convention that the
     * x axis goes from left to right, the y axis goes from bottom to
     * top, and the z axis points towards the viewer.
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
     * object being displayed.
     * @param phi the Eulerian angle phi in radians
     * @param theta the Eulerian angle theta in radians
     * @param psi the Eulerian angle psi in radians
     */
    public void setCoordRotation(double phi, double theta, double psi) {
	this.theta = theta;
	this.phi = phi;
	this.psi = psi;
    }

    /**
     * Rotate the Model3D coordinates by providing functions giving
     * the rotation as a function of time.
     * When all returned values are zero, the z access is perpendicular to
     * an image, the x axis is horizontal, and the y axis is vertical,
     * all following the standard mathematical convention that the
     * x axis goes from left to right, the y axis goes from bottom to
     * top, and the z axis points towards the viewer.
     * <P>

     * The functions provide Eulerian angles, in units of radians, as
     * a function of the time returned by the animation method
     * {@link org.bzdev.devqsim.Simulation#currentTime()}. If a function is
     * null, the value of the Eulerian angle is based on the value
     * passed explicitly and any specified rate of change.

     * <P>
     * The Eulerian angles follow the convention used in Goldstein,
     * "Classical Mechanics": &phi; is the angle the x-axis rotates
     * with positive values of $phi; indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  The angle &theta; then indicates a rotation about
     * the new x axis, and &psi; indicates a rotation about the final
     * z axis.
     * <P>
     * Note that the coordinate system is being rotated, not the
     * object being displayed.
     * @param phiF the function that computes the Eulerian angle &phi; in
     *        radians; null if there is no function for this angle
     * @param thetaF the function that computes Eulerian angle &theta; in
     *        radians; null if there is no function for this angle
     * @param psiF the function that computes the Eulerian angle &psi; in
     *        radians; null if there is no function for this angle
     */
    public void setCoordRotationByF(RealValuedFunctOps phiF,
				    RealValuedFunctOps thetaF,
				    RealValuedFunctOps psiF)
    {
	if (thetaF == null) {
	    this.thetaFunction = null;
	} else if (thetaF instanceof RealValuedFunction) {
	    this.thetaFunction = (RealValuedFunction) thetaF;
	} else {
	    this.thetaFunction = new RealValuedFunction(thetaF);
	}
	if (phiF == null) {
	    this.phiFunction = null;
	} else if (phiF instanceof RealValuedFunction) {
	    this.phiFunction = (RealValuedFunction) phiF;
	} else {
	    this.phiFunction = new RealValuedFunction(phiF);
	}
	if (psiF == null) {
	    this.psiFunction = null;
	} else if (psiF instanceof RealValuedFunction) {
	    this.psiFunction = (RealValuedFunction) psiF;
	} else {
	    this.psiFunction = new RealValuedFunction(psiF);
	}
	this.thetaSimFunction = null;
	this.phiSimFunction = null;
	this.psiSimFunction = null;
    }

    /**
     * Rotate the Model3D coordinates by providing simulation functions giving
     * the rotation as a function of time.
     * When all returned values are zero, the z access is perpendicular to
     * an image, the x axis is horizontal, and the y axis is vertical,
     * all following the standard mathematical convention that the
     * x axis goes from left to right, the y axis goes from bottom to
     * top, and the z axis points towards the viewer.
     * <P>

     * The functions provide Eulerian angles, in units of radians, as
     * a function of the time returned by the animation method
     * {@link org.bzdev.devqsim.Simulation#currentTime()}. If a function is
     * null, the value of the Eulerian angle is based on the value
     * passed explicitly and any specified rate of change.

     * <P>
     * The Eulerian angles follow the convention used in Goldstein,
     * "Classical Mechanics": &phi; is the angle the x-axis rotates
     * with positive values of $phi; indicating a counter-clockwise
     * rotation when viewed from positive values of z towards the
     * plane in which z=0.  The angle &theta; then indicates a rotation about
     * the new x axis, and &psi; indicates a rotation about the final
     * z axis.
     * <P>
     * Note that the coordinate system is being rotated, not the
     * object being displayed.
     * @param phiF the function that computes the Eulerian angle &phi; in
     *        radians; null if there is no function for this angle
     * @param thetaF the function that computes Eulerian angle &theta; in
     *        radians; null if there is no function for this angle
     * @param psiF the function that computes the Eulerian angle &psi; in
     *        radians; null if there is no function for this angle
     */
    public void setCoordRotationBySF(SimFunction phiF,
				     SimFunction thetaF,
				     SimFunction psiF)
    {
	this.thetaFunction = (thetaF == null)? null: thetaF.getFunction();
	this.phiFunction = (phiF == null)? null: phiF.getFunction();
	this.psiFunction = (psiF == null)? null: psiF.getFunction();
	this.thetaSimFunction = thetaF;
	this.phiSimFunction = phiF;
	this.psiSimFunction = psiF;
    }

    /**
     * Get the border's size.
     * The border is the minimum distance in user space between a model
     * and the image's frame when the magnification is 1.0.
     * @return the size of the border
     */
    public double getBorder() {return border;}

    /**
     * Set the border.
     * The border is the minimum distance in user space between a model
     * and the image's frame when the magnification is 1.0.
     * @param border a non-negative number giving the border
     */
    public void setBorder(double border) {
	this.border = border;
    }
    
    /**
     * Get the fraction of the image width, excluding borders, and going
     * from left to right, by which the left side of the model is shifted.
     * @return the fraction in the range [0.0, 1.0]
     */
    public double getXFract() {return xfract;}

    /**
     * Get the function determining the fraction of the image width,
     * excluding borders, and going from left to right, by which the
     * left side of the model is shifted.  The function must return
     * values in the range [0.0, 1.0].
     * @return the function; null if there is none
     */
    public RealValuedFunction getXFractFunction() {return xfractFunction;}

    /**
     * Set the fraction of the image width, excluding borders, and going
     * from left to right, by which the left side of the model is shifted.
     * @param xfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the x axis, with values
     *        in the range [0.0, 1.0]
     */
    public void setXFract(double xfract) {
	this.xfract = xfract;
    }

    /**
     * Set the function determining the fraction of the image width,
     * excluding borders, and going from left to right, by which the
     * left side of the model is shifted.  The function must return
     * values in the range [0.0, 1.0].
     * If the function is null, the fraction is determined by the values
     * passed to {@link #setXFract(double)} and {@link #setXFractRate(double)}.
     * @param xfractFunction the function; null if no function is to be used
     */
    public  void setXFractByF(RealValuedFunctOps xfractFunction) {
	if (xfractFunction == null) {
	    this.xfractFunction = null;
	} else if (xfractFunction instanceof RealValuedFunction) {
	    this.xfractFunction = (RealValuedFunction) xfractFunction;
	} else {
	    this.xfractFunction = new RealValuedFunction(xfractFunction);
	}
	this.xfractSimFunction = null;
    }
    
    /**
     * Set the simulation function determining the fraction of the image width,
     * excluding borders, and going from left to right, by which the
     * left side of the model is shifted.  The function must return
     * values in the range [0.0, 1.0].
     * If the function is null, the fraction is determined by the values
     * passed to {@link #setXFract(double)} and {@link #setXFractRate(double)}.
     * @param xfractFunction the function; null if no function is to be used
     */
    public  void setXFractBySF(SimFunction xfractFunction) {
	this.xfractFunction = (xfractFunction == null)? null:
	    xfractFunction.getFunction();
	this.xfractSimFunction = xfractFunction;
    }

    /**
     * Get the fraction of the image width, excluding borders, and going
     * from bottom to top, by which the bottom side of the model is shifted.
     * @return the fraction in the range [0.0, 1.0]
     */
    public double getYFract() {return xfract;}

    /**
     * Get the function determining the fraction of the image width,
     * excluding borders, and going from bottom to top, by which the
     * bottom side of the model is shifted.
     * The function must return values in the range  [0.0, 1.0].
     * @return the fraction; null if none is to be used
     */
    public RealValuedFunction getYFractFunction() {return yfractFunction;}


    /**
     * Set the fraction of the image width, excluding borders, and going
     * from bottom to top, by which the bottom side of the model is shifted.
     * @param yfract the fraction of the image width, excluding the borders,
     *        by which an image was shifted along the y axis, with values
     *        in the range [0.0, 1.0]
     */
    public void setYFract(double yfract) {
	this.yfract = yfract;
    }

    /**
     * Set the function determining the fraction of the image width,
     * excluding borders, and going from bottom to top, by which the
     * bottom side of the model is shifted.
     * This function must return values in the range [0.0, 1.0].
     * If the function is null, the fraction is determined by the values
     * passed to {@link #setYFract(double)} and {@link #setXFractRate(double)}.
     * @param yfractFunction the function; null if none is to be used
     */
    public void setYFractByF(RealValuedFunctOps yfractFunction) {
	if (yfractFunction == null) {
	    this.yfractFunction = null;
	} else if (yfractFunction instanceof RealValuedFunction) {
	    this.yfractFunction = (RealValuedFunction)yfractFunction;
	} else {
	    this.yfractFunction = new RealValuedFunction(yfractFunction);
	}
	this.yfractSimFunction = null;
    }

    /**
     * Set the simulation function determining the fraction of the image width,
     * excluding borders, and going from bottom to top, by which the
     * bottom side of the model is shifted.
     * This function must return values in the range [0.0, 1.0].
     * If the function is null, the fraction is determined by the values
     * passed to {@link #setYFract(double)} and {@link #setXFractRate(double)}.
     * @param yfractFunction the function; null if none is to be used
     */
    public void setYFractBySF(SimFunction yfractFunction) {
	this.yfractFunction = (yfractFunction == null)? null:
	    yfractFunction.getFunction();
	this.yfractSimFunction = yfractFunction;
    }


    /**
     * Get the minimum triangle size for rendering.
     * @return the size of an edge
     * @see #setDelta(double)
     */
    public double getDelta() {return image.getDelta();}

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
     * rendering considerably.
     * @param delta the triangle dimension limit; ignored if zero
     */
    public void setDelta(double delta) {
	image.setDelta(delta);
    }

    /**
     * Get the light-source parameter phi.
     * The parameter partially determines the direction of the light
     * source as described in the documentation for
     * {@link #setLightSource(double,double) setLightSource}.
     * @return the light-source parameter phi
     * @see #setLightSource(double,double)
     */
    public double getLightSourcePhi() {
	return lsPhi;
    }

    /**
     * Get the function that determines light-source parameter &phi;.
     * The parameter partially determines the direction of the light
     * source as described in the documentation for
     * {@link #setLightSource(double,double) setLightSource}.
     * The function provides the value of the parameter as a function of
     * time in units of seconds.
     * @return the function; null if there is none
     * @see #setLightSource(double,double)
     */
    public RealValuedFunction getLightSourcePhiFunction() {
	return lsPhiFunction;
    }

    /**
     * Get the light-source parameter theta.
     * The parameter partially determines the direction of the light
     * source as described in the documentation for
     * {@link #setLightSource(double,double) setLightSource}.
     * @return the light-source parameter theta
     * @see #setLightSource(double,double)
     */
    public double getLightSourceTheta() {
	return lsTheta;
    }

    /**
     * Get the function determining the light-source parameter &theta;.
     * The parameter partially determines the direction of the light
     * source as described in the documentation for
     * {@link #setLightSource(double,double) setLightSource}.
     * The function provides the value of the parameter as a function of
     * time in units of seconds.
     * @return the function ; null if there is none
     * @see #setLightSource(double,double)
     */
    public RealValuedFunction getLightSourceThetaFunction() {
	return lsThetaFunction;
    }

    /**
     * Sets the direction to a light source. The default direction,
     * in which theta=0, corresponds to a light source at infinity aimed
     * vertically downwards towards the x-y plane.
     * The brightness used for a triangle varies as the cosine of the
     * angle between a vector pointing in the direction of the light
     * source and a vector perpendicular to the plane of the triangle.
     * @param phi the angle in radians about the z axis
     *        of the plane containing a unit vector pointing towards
     *        the light source will.  If phi is zero, this plane includes
     *        the x axis and for non-zero theta, the unit vector will have
     *        a positive x component.
     * @param theta the angle between a unit vector pointing towards
     *        the light source makes and with a vector parallel to the
     *        z axis.
     */
    public void setLightSource(double phi, double theta) {
	lsPhi = phi;
	lsTheta = theta;
	// image.setLightSource(phi, theta);
    }

    /**
     * Sets functions that determine the direction to a light source.
     * The default direction, in which &theta;=0, corresponds to a light
     * source at infinity aimed vertically downwards towards the x-y
     * plane.  The brightness used for a triangle varies as the cosine
     * of the angle between a vector pointing in the direction of the
     * light source and a vector perpendicular to the plane of the
     * triangle.  These parameters are:
     * <ul>
     *   <li> &phi; - the angle in radians about the z axis
     *        of the plane containing a unit vector pointing towards
     *        the light source will.  If phi is zero, this plane includes
     *        the x axis and for non-zero theta, the unit vector will have
     *        a positive x component.
     *   <li> &theta; - the angle between a unit vector pointing towards
     *        the light source makes and with a vector parallel to the
     *        z axis.
     * </ul>
     * The functions determine these values as a function of time, with
     * time provided in units of seconds.
     * @param phiF the function for &phi;; null if there is none
     * @param thetaF the function for &theta;; null if there is none
     */
    public void setLightSourceByF(RealValuedFunctOps phiF,
				  RealValuedFunctOps thetaF)
    {
	if (phiF == null) {
	    lsPhiFunction = null;
	} else if (phiF instanceof RealValuedFunction) {
	    lsPhiFunction = (RealValuedFunction) phiF;
	} else {
	    lsPhiFunction = new RealValuedFunction(phiF);
	}
	if (thetaF == null) {
	    lsThetaFunction = null;
	} else if (thetaF instanceof RealValuedFunction) {
	    lsThetaFunction = (RealValuedFunction)thetaF;
	} else {
	    lsThetaFunction = new RealValuedFunction(thetaF);
	}
	lsPhiSimFunction = null;
	lsThetaSimFunction = null;
    }

    /**
     * Sets simulation functions that determine the direction to a light source.
     * The default direction, in which &theta;=0, corresponds to a light
     * source at infinity aimed vertically downwards towards the x-y
     * plane.  The brightness used for a triangle varies as the cosine
     * of the angle between a vector pointing in the direction of the
     * light source and a vector perpendicular to the plane of the
     * triangle.  These parameters are:
     * <ul>
     *   <li> &phi; - the angle in radians about the z axis
     *        of the plane containing a unit vector pointing towards
     *        the light source will.  If phi is zero, this plane includes
     *        the x axis and for non-zero theta, the unit vector will have
     *        a positive x component.
     *   <li> &theta; - the angle between a unit vector pointing towards
     *        the light source makes and with a vector parallel to the
     *        z axis.
     * </ul>
     * The functions determine these values as a function of time, with
     * time provided in units of seconds.
     * @param phiF the function for &phi;; null if there is none
     * @param thetaF the function for &theta;; null if there is none
     */
    public void setLightSourceBySF(SimFunction phiF,
				   SimFunction thetaF)
    {
	lsPhiFunction = (phiF == null)? null: phiF.getFunction();
	lsThetaFunction = (thetaF == null)? null: thetaF.getFunction();
	lsPhiSimFunction = phiF;
	lsThetaSimFunction = thetaF;
    }

    /**
     * Get the rate at which the light source's angle phi changes.
     * @return the rate in radians per second
     */
    public double getLightSourcePhiRate() {return lsPhiRate;}

    /**
     * Set the rate at which the light source's angle phi changes.
     * @param phiRate the rate in radians per second
     */
    public void setLightSourcePhiRate(double phiRate) {
	lsPhiRate = phiRate;
    }

    /**
     * Get the rate at which the light source's angle theta changes.
     * @return the rate in radians per second
     */
    public double getLightSourceThetaRate() {return lsThetaRate;}

    /**
     * Set the rate at which the light source's angle theta changes.
     * @param thetaRate the rate in radians per second
     */
    public void setLightSourceThetaRate(double thetaRate) {
	lsThetaRate = thetaRate;
    }

    /**
     * Get the color factor.
     * After coordinate transformations, the red, blue, and green
     * components of the color of an object will be scaled depending
     * on its height so that, for the same orientation, triangles at
     * with the lowest values of 'z' will be dimmer than those with
     * the maximum value of 'z' by this factor.  A value of zero
     * indicates that the color factor will be ignored.
     *  @return the color factor
     * @see #setColorFactor(double)
     */
    public double getColorFactor() {return image.getColorFactor();}

    /**
     *  Set the color factor.
     *  After coordinate transformations, the red, blue, and green
     *  components of the color of an object will be scaled depending
     *  on its height so that, for the same orientation, triangles at
     *  with the lowest values of 'z' will be dimmer than those with
     *  the maximum value of 'z' by this factor.  If the value is 1.0,
     *  the smallest z value of the object will be rendered as black.
     *  <P>
     *  This is intended to help emphasize differences along the 'z'
     *  axis that would normally be hard to discern visually.
     *  @param factor the color factor given as a non-negative number
     */
    public void setColorFactor(double factor) {
	image.setColorFactor(factor);
    }

    /**
     * Get the color for the the side of a triangle that should not
     * be visible due to being on the inside of a closed manifold.
     * @return the color
     */
    public Color getBacksideColor() {return image.getBacksideColor();}

    /**
     * Set the color for the side of a triangle that should not
     * be visible due to being on the inside of a closed manifold.
     * This is useful for debugging a model: if the model does not
     * @param c the color
     */
    public void setBacksideColor(Color c) {
	image.setBacksideColor(c);
    }

    /**
     * Get the color used to indicate the edges of a triangle
     * @return the color or null to indicate that edges will not
     *         be shown
     */
    public Color getEdgeColor() {return image.getEdgeColor();}

    /**
     * Set the color to use to use to draw the edges of triangles.
     * @param c the color; null if edges should not be displayed
     */
    public void setEdgeColor(Color c) {
	image.setEdgeColor(c);
    }

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
    public Color getDefaultSegmentColor() {
	return image.getDefaultSegmentColor();
    }

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
    public void setDefaultSegmentColor(Color c) {
	image.setDefaultSegmentColor(c);
    }

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
     * @return the color; null if these line segments should not
     *       be displayed
     */
    public Color getDefaultBacksideSegmentColor() {
	return image.getDefaultBacksideSegmentColor();
    }

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
    public void setDefaultBacksideSegmentColor(Color c) {
	image.setDefaultBacksideSegmentColor(c);
    }

    /**
     *  Reset to default values.
     */
    public void reset() {
	image.reset();
    }

    /**
     * Get the rate at which the Eulerian angle theta changes.
     * @return the rate in radians per second
     */
    public double getThetaRate() {return thetaRate;}

    /**
     * Set the rate at which the Eulerian angle theta changes.
     * @param thetaRate the rate in radians per second
     */
    public void setThetaRate(double thetaRate) {
	this.thetaRate = thetaRate;
    }

    /**
     * Get the rate at which the Eulerian angle phi changes.
     * @return the rate in radians per second
     */
    public double getPhiRate() {return phiRate;}

    /**
     * Set the rate at which the Eulerian angle phi changes.
     * @param phiRate the rate in radians per second
     */
    public void setPhiRate(double phiRate) {
	this.phiRate = phiRate;
    }

    /**
     * Get the rate at which the Eulerian angle psi changes.
     * @return the rate in radians per second
     */
    public double getPsiRate() {return psiRate;}

    /**
     * Set the rate at which the Eulerian angle psi changes.
     * @param psiRate the rate in radians per second
     */
    public void setPsiRate(double psiRate) {
	this.psiRate = psiRate;
    }

    /**
     * Get the rate at which the fractional distance in the x direction
     * changes.  The fractional distance is defined so that 0.0 implies
     * that the left edge of the model is as far left as allowed by the
     * border, and 1.0 implies that the right edge of the model is as far
     * to the right as allowed by the border.
     * @return the rate in units of  seconds<sup>-1</sup>
     */
    public double getXFractRate() {return xfractRate;}

    /**
     * Set the rate at which the fractional distance in the x direction
     * changes.  The fractional distance is defined so that 0.0 implies
     * that the left edge of the model is as far left as allowed by the
     * border, and 1.0 implies that the right edge of the model is as far
     * to the right as allowed by the border.
     * @param xfractRate the rate in units of  seconds<sup>-1</sup>
     */
    public void setXFractRate(double xfractRate) {
	this.xfractRate = xfractRate;
    }

    /**
     * Get the rate at which the fractional distance in the y direction
     * changes.  The fractional distance is defined so that 0.0 implies
     * that the bottom edge of the model is as close to the bottom of
     * the image  as allowed by the
     * border, and 1.0 implies that the top edge of the model is as close
     * to the top of the image as allowed by the border.
     * @return the rate in units of seconds<sup>-1</sup>
     */
    public double getYFractRate() {return yfractRate;}

    /**
     * Set the rate at which the fractional distance in the y direction
     * changes.  The fractional distance is defined so that 0.0 implies
     * that the bottom edge of the model is as close to the bottom of
     * the image  as allowed by the
     * border, and 1.0 implies that the top edge of the model is as close
     * to the top of the image as allowed by the border.
     * @param yfractRate the rate in units of seconds<sup>-1</sup>
     */
    public void setYFractRate(double yfractRate) {
	this.yfractRate = yfractRate;
    }

    /**
     * Get the magnification factor.
     * @return the magnification factor.
     */
    public double getMagnification() {return magnification;}

    /**
     * Get the function determining the magnification factor.

     * The function, if not null, must return a positive real number
     * that gives the magnification as a function of the time returned
     * by the animation method
     * {@link org.bzdev.devqsim.Simulation#currentTime()}.
     * @return the function; null if there is none
     */
    public RealValuedFunction getMagnificationFunction() {return magFunction;}

    /**
     * Set the magnification factor.
     * @param magnification the magnification factor.
     */
    public void setMagnification(double magnification) {
	if (magnification <= 0.0)
	    throw new IllegalArgumentException
		(errorMsg("illegalMag", magnification));
	this.magnification = magnification;
    }

    /**
     * Set the function determining the magnification factor.
     * The function, if not null,  must return a positive real number that
     * gives the magnification as a function of time returned by the
     * animation method
     * {@link org.bzdev.devqsim.Simulation#currentTime()}.
     * If a function is not provided, the magnification factor provided
     * explicitly, perhaps modified by a magnification rate, will be used.
     * @param magF the function; null if there is none
     */
    public void setMagnificationByF(RealValuedFunctOps magF) {
	if (magF == null) {
	    magFunction = null;
	} else if (magF instanceof RealValuedFunction) {
	    magFunction = (RealValuedFunction) magF;
	} else {
	    magFunction = new RealValuedFunction(magF);
	}
	magSimFunction = null;
    }

    /**
     * Set the simulation function determining the magnification factor.
     * The function, if not null,  must return a positive real number that
     * gives the magnification as a function of time returned by the
     * animation method
     * {@link org.bzdev.devqsim.Simulation#currentTime()}.
     * If a function is not provided, the magnification factor provided
     * explicitly, perhaps modified by a magnification rate, will be used.
     * @param magF the function; null if there is none
     */
    public void setMagnificationBySF(SimFunction magF) {
	magFunction = (magF == null)? null: magF.getFunction();
	magSimFunction = magF;
    }

    /**
     * Get the logarithmic magnification rate.
     * The magnification factor will change with time as exp(magRate * t);
     * @return  the  magnification rate
     */
    public double getLogMagnificationRate() {return magRate;}

    /**
     * Set the magnification rate logarithmicly.
     * The magnification factor will change with time as exp(magRate * t);
     * @param magRate the logarithmic magnification rate
     */
    public void setLogMagnificationRate(double magRate) {
	this.magRate = magRate;
    }

    /**
     * Set the magnification rate logarithmicly to magnification to a
     * target value in a specified time.
     * @param magTarget the target magnification value after a time
     *        interval of deltaT elapses
     * @param deltaT the time interval for the magnification factor to change
     *        from its current value to the target value
     */
    public void setLogMagnificationRate(double magTarget, double deltaT) {
	magRate = Math.log(magTarget/magnification) / deltaT;
    }

    public void update(double t, long simtime) {
	super.update(t, simtime);
	double tf = (time0 == 0.0)? t: (t-time0);
	if (magFunction != null) {
	    magnification = magFunction.valueAt(tf);
	} else if (magRate != 0.0) {
	    magnification *= Math.exp(magRate * (t - lastTime));
	}
	if (thetaFunction != null) {
	    theta = thetaFunction.valueAt(tf);
	} else if (thetaRate != 0.0) {
	    theta += thetaRate *(t - lastTime);
	}

	if (phiFunction != null) {
	    phi = phiFunction.valueAt(tf);
	} else if (phiRate != 0.0) {
	    phi += phiRate *(t - lastTime);
	}

	if (psiFunction != null) {
	    psi = psiFunction.valueAt(tf);
	} else if (psiRate != 0.0) {
	    psi += psiRate *(t - lastTime);
	}

	if (xfractFunction != null) {
	    xfract = xfractFunction.valueAt(tf);
	    if (xfract > 1.0) xfract = 1.0;
	    if (xfract < 0.0) xfract = 0.0;
	} else if (xfractRate != 0.0) {
	    xfract += xfractRate *(t - lastTime);
	    if (xfract > 1.0) xfract = 1.0;
	    if (xfract < 0.0) xfract = 0.0;
	}

	if (yfractFunction != null) {
	    yfract = yfractFunction.valueAt(tf);
	    if (yfract > 1.0) yfract = 1.0;
	    if (yfract < 0.0) yfract = 0.0;
	} else if (yfractRate != 0.0) {
	    yfract += yfractRate *(t - lastTime);
	    if (yfract > 1.0) yfract = 1.0;
	    if (yfract < 0.0) yfract = 0.0;
	}

	if (lsThetaFunction != null) {
	    lsTheta = lsThetaFunction.valueAt(tf);
	} else if (lsThetaRate != 0.0) {
	    lsTheta += lsThetaRate * (t - lastTime);
	}

	if (lsPhiFunction != null) {
	    lsPhi = lsPhiFunction.valueAt(tf);
	} else if (lsPhiRate != 0.0) {
	    lsPhi += lsPhiRate * (t - lastTime);
	}

	lastTime = t;
    }

    /**
     * Add this object to an animation's graph.
     * This is a special case. There should be only on in use
     * per animation. It will set up the graph so that the graph displays
     * a particular range in graph coordinate space. Angles are ignored
     * so that Graph View will never be rotated. Graphic operations
     * at a particular time should be performed after a graph's
     * {@link Graph#add(Graphic) add} method is called for any GraphView
     * defined for the graph, and after 
     * {@link #update(double,long) update} is called with the
     * current time as its argument.
     * @param graph the graph on which this object should be drawn
     * @param g2d  (ignored but needed by Graph.Graphics interface)
     * @param g2dGCS  (ignored but needed by Graph.Graphics interface)
     * @exception IllegalStateException initialize was not called
     */
    public void addTo(Graph graph, Graphics2D g2d, Graphics2D g2dGCS)
	throws IllegalStateException
    {
	if (model == null) throw new IllegalStateException(errorMsg("noModel"));
	if (forceSC && changeScale == false) {
	    image.getImageData().forceScaleChange();
	}
	forceSC = false;
	image.setCoordRotation(phi, theta, psi);
	image.setLightSource(lsPhi, lsTheta);
	model.setImageParameters(image, border, magnification, xfract, yfract,
				 changeScale);
	// model.render(image);
	model.render(image, g2d);
    }

    /**
     * {@inheritDoc}
     * Defined in {@link GraphView}:
     * <UL>
     *   <LI> the initial viewing time.
     *   <LI> the change-scale-per-view flag.
     *   <LI> the backside color.
     *   <LI> the edge color.
     *   <LI> the default segment color.
     *   <LI> the default backside segment color.
     *   <LI> the triangle-rendering delta.
     *   <LI> the color factor.
     *   <LI> either the phi simulation function, a note that the phi
     *        function was set,  or the phi rate.

     *   <LI> either the theta simulation function, a note that the
     *        theta function was set, or the theta rate.
     *   <LI> either the psi simulation function, a note that the
     *        psi function was set, or the psi rate.
     *   <LI> either the magnification simulation function, a note that
     *        the magnification was set, or the magnification rate (which
     *        is logarithmic).
     *   <LI> either the X-fraction simulation function, a note that the
     *        X-fraction function was set, or the X-fraction rate
     *   <LI> either the Y-fraction simulation function, a note that the
     *        Y-fraction function was set, or the Y-fraction rate
     *   <LI> either the lightsource phi simulation function, a note
     *        that the lightsource phi function was set, or the
     *        lightsource phi rate.
     *   <LI> either the lightsource theta simulation function, a note
     *        that the lightsource theta function was set, or the
     *        lightsource theta rate.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   java.io.PrintWriter out) {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "initial viewing time: " + time0);
	out.println(prefix + "change scale per view: " + getChangeScale());
	Color c = getBacksideColor();
	out.println(prefix + "backside color: " + ((c == null)? "none": c));
	c = getEdgeColor();
	out.println(prefix + "edge color: " + ((c == null)? "none": c));
	c = getDefaultSegmentColor();
	out.println(prefix + "default segment color: "
		    + ((c == null)? "none": c));
	c = getDefaultBacksideSegmentColor();
	out.println(prefix + "default backside segment color: "
		    + ((c == null)? "none": c));
	out.println(prefix + "triangle-rendering delta: " + getDelta());
	out.println(prefix + "color factor: " + getColorFactor());
	if (phiSimFunction != null) {
	    out.println(prefix + "phi simulation function: "
			+ phiSimFunction.getName());
	} else if (phiFunction != null ) {
	    out.println(prefix + "phi function is set");
	} else  {
	    out.println(prefix + "phi rate: " + getPhiRate());
	}
	if (thetaSimFunction != null) {
	    out.println(prefix + "theta simulation function: "
			+ thetaSimFunction.getName());
	} else if (thetaFunction != null) {
	    out.println(prefix + "theta function is set");
	} else  {
	    out.println(prefix + "theta rate: " + getThetaRate());
	}
	if (psiSimFunction != null) {
	    out.println(prefix + "psi simulation function: "
			+ psiSimFunction.getName());
	} else if (psiFunction != null) {
	    out.println(prefix + "psi function is set");
	} else  {
	    out.println(prefix + "psi rate: " + getPsiRate());
	}
	if (magSimFunction != null) {
	    out.println(prefix + "magnification simulation function: "
			+ magSimFunction.getName());
	} else if (magFunction != null) {
	    out.println(prefix + "magnification function is set");
	} else  {
	    out.println(prefix + "magnification rate (logarithmic): "
			+ getLogMagnificationRate());
	}
	if (xfractSimFunction != null) {
	    out.println(prefix + "x-fraction simulation function: "
			+ xfractSimFunction.getName());
	} else if (xfractFunction != null) {
	    out.println(prefix + "X-fraction function is set");
	} else  {
	    out.println(prefix + "X-fraction rate: " + getXFractRate());
	}
	if (yfractSimFunction != null) {
	    out.println(prefix + "Y-fraction simulation function: "
			+ yfractSimFunction.getName());
	} else if (yfractFunction != null) {
	    out.println(prefix + "Y-fraction function is set");
	} else  {
	    out.println(prefix + "Y-fraction rate: " + getYFractRate());
	}
	if (lsPhiSimFunction != null) {
	    out.println(prefix + "lightsource phi simulation function: "
			+ lsPhiSimFunction.getName());
	} else if (lsPhiFunction != null) {
	    out.println(prefix + "lightsource phi function is set");
	} else  {
	    out.println(prefix + "lightsource phi rate: "
			+ getLightSourcePhiRate());
	}
	if (lsThetaSimFunction != null) {
	    out.println(prefix + "lightsource theta simulation function: "
			+ lsThetaSimFunction.getName());
	} else if (lsThetaFunction != null) {
	    out.println(prefix + "lightsource theta function is set");
	} else  {
	    out.println(prefix + "lightsource theta rate: "
			+ getLightSourceThetaRate());
	}
    }

    /**
     * {@inheritDoc}
     * Defined in {@link Model3DView}:
     * <UL>
     *   <LI> a note if a forced scale change is pending.
     *   <LI> the value of the Eulerian angle theta in radians.
     *   <LI> the value of the Eulerian angle phi in radians.
     *   <LI> the value of the Eulerian angle psi in radians.
     *   <LI> the X fractional distance.
     *   <LI> the Y fractional distance.
     *   <LI> the value of theta for the light sources.
     *   <LI> the value of phi for the light source.
     *   <LI> the magnification (1.0 indicates no magnification).
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix, boolean printName,
			   java.io.PrintWriter out) {
	super.printState(iPrefix, prefix, printName, out);
	boolean sc = getChangeScale();
	if (forceSC) out.println(prefix + "pending forced scale change");
	out.println(prefix + "theta: " + getTheta());
	out.println(prefix + "phi: " + getPhi());
	out.println(prefix + "psi" + getPsi());
	out.println(prefix + "X fractional distance: " + getXFract());
	out.println(prefix + "Y fractional distance: " + getYFract());
	out.println(prefix + "light-source theta: "
		    + getLightSourceTheta());
	out.println(prefix + "light-source phi: " + getLightSourcePhi());
	out.println(prefix + "magnification: " + getMagnification());
    }
}

//  LocalWords:  exbundle pre DView setModel setVisible maxFrameCount
//  LocalWords:  initFrames png scheduleFrames getObject addTo phiF
//  LocalWords:  IllegalArgumentException changeScale SimFunction ul
//  LocalWords:  printConfiguration Eulerian Goldstein li iPrefix
//  LocalWords:  currentTime thetaF psiF xfract setXFract yfract magF
//  LocalWords:  setXFractRate xfractFunction setYFract setDelta dGCS
//  LocalWords:  yfractFunction setLightSource phiRate thetaRate
//  LocalWords:  setColorFactor param ImageData psiRate xfractRate
//  LocalWords:  yfractRate illegalMag magRate magTarget deltaT
//  LocalWords:  GraphView IllegalStateException noModel lightsource
//  LocalWords:  printName logarithmicly
