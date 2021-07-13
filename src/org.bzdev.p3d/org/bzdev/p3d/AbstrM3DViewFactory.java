package org.bzdev.p3d;

import org.bzdev.anim2d.Animation2D;
import org.bzdev.anim2d.AnimationObject2DFactory;
import org.bzdev.devqsim.SimFunction;

import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.anim2d.AnimationObject2DFactory;

import org.bzdev.obnaming.misc.ColorParm;
import org.bzdev.lang.Callable;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Abstract factory for Mode3DView.
 * <P>
 * The parameters this factory provides are the same as the parameters
 * provided by its subclass {@link Model3DViewFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/p3d/Model3DViewFactory.html" style="width:95%;height:500px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/p3d/Model3DViewFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "Model3DViewParmManager",
		    tipResourceBundle = "*.lpack.Model3DViewTips",
		    labelResourceBundle = "*.lpack.Model3DViewLabels",
		    docResourceBundle = "*.lpack.Model3DViewDocs")
abstract public class AbstrM3DViewFactory<Obj extends Model3DView>
    extends AnimationObject2DFactory<Obj>
{
    @CompoundParmType(tipResourceBundle = "*.lpack.Model3DViewTimeLineTips",
		      labelResourceBundle = "*.lpack.Model3DViewTimeLineLabels",
		      docResourceBundle = "*.lpack.Model3DViewTimeLineDocs")
    static class Timeline {
	// @PrimitiveParm("time") Double time = null;
	@PrimitiveParm("phi") Double phi = null;
	@PrimitiveParm("theta") Double theta = null;
	@PrimitiveParm("psi") Double psi = null;
	@PrimitiveParm("phiRate") Double phiRate = null;
	@PrimitiveParm("thetaRate") Double thetaRate = null;
	@PrimitiveParm("psiRate") Double psiRate = null;
	@PrimitiveParm("phiFunction") SimFunction phiFunction = null;
	@PrimitiveParm("thetaFunction") SimFunction thetaFunction = null;
	@PrimitiveParm("psiFunction") SimFunction psiFunction = null;
	@PrimitiveParm("lightsourcePhi") Double lsPhi = null;
	@PrimitiveParm("lightsourceTheta") Double lsTheta = null;
	@PrimitiveParm("lightsourcePhiRate") Double lsPhiRate = null;
	@PrimitiveParm("lightsourceThetaRate") Double lsThetaRate = null;
	@PrimitiveParm("lightsourcePhiFunction")
	    SimFunction lsPhiFunction = null;
	@PrimitiveParm("lightsourceThetaFunction")
	    SimFunction lsThetaFunction = null;
	@PrimitiveParm("xfract") Double xfract = null;
	@PrimitiveParm("yfract") Double yfract = null;
	@PrimitiveParm("xfractRate") Double xfractRate = null;
	@PrimitiveParm("yfractRate") Double yfractRate = null;
	@PrimitiveParm("xfractFunction") SimFunction xfractFunction = null;
	@PrimitiveParm("yfractFunction") SimFunction yfractFunction = null;
	@PrimitiveParm("magnification") Double mag = null;
	@PrimitiveParm("logmagnificationRate") Double logmagRate = null;
	@PrimitiveParm("magnificationFunction") SimFunction magFunction = null;
	// @PrimitiveParm("visible") Boolean visible = null;
	// @PrimitiveParm("zorder") Long zorder = null;
	@PrimitiveParm("colorFactor") Double colorFactor = null;
	@PrimitiveParm("changeScale") Boolean changeScale = null;
	@PrimitiveParm("forceScaleChange") boolean forceScaleChange = false;
    }

    @PrimitiveParm("time0") double time0 = 0.0;

    @KeyedCompoundParm("timeline")
	Map<Integer,Timeline> timelineMap =
	new TreeMap<Integer,Timeline>();

    @PrimitiveParm("colorFactor") double colorFactor = 0.0;

    @PrimitiveParm("border") double border = 0.0;

    @CompoundParm("backsideColor")
	ColorParm backsideColor = new ColorParm();

    @CompoundParm("edgeColor")
	ColorParm edgeColor =  new ColorParm();

    @CompoundParm("segmentColor")
	ColorParm segmentColor =  new ColorParm();

    @CompoundParm("backsideSegmentColor")
	ColorParm backsideSegmentColor =  new ColorParm();

    @PrimitiveParm("changeScale") boolean changeScale = true;

    @PrimitiveParm("phi") double phi = 0.0;

    @PrimitiveParm("theta") double theta = 0.0;
    @PrimitiveParm("psi") double psi = 0.0;

    @PrimitiveParm("phiRate") double phiRate = 0.0;

    @PrimitiveParm("thetaRate") double thetaRate = 0.0;

    @PrimitiveParm("psiRate") double psiRate = 0.0;

    @PrimitiveParm("phiFunction") SimFunction phiFunction = null;

    @PrimitiveParm("thetaFunction") SimFunction thetaFunction = null;

    @PrimitiveParm("psiFunction") SimFunction psiFunction = null;

    @PrimitiveParm("lightsourcePhi") double lsPhi = 0.0;

    @PrimitiveParm("lightsourceTheta") double lsTheta = 0.0;

    @PrimitiveParm("lightsourcePhiRate") double lsPhiRate = 0.0;

    @PrimitiveParm("lightsourceThetaRate") double lsThetaRate = 0.0;

    @PrimitiveParm("lightsourcePhiFunction") SimFunction lsPhiFunction = null;

    @PrimitiveParm("lightsourceThetaFunction")
	SimFunction lsThetaFunction = null;

    @PrimitiveParm("xfract") double xfract = 0.0;

    @PrimitiveParm("yfract") double yfract = 0.0;

    @PrimitiveParm("xfractRate") double xfractRate = 0.0;

    @PrimitiveParm("yfractRate") double yfractRate = 0.0;

    @PrimitiveParm("xfractFunction") SimFunction xfractFunction = null;

    @PrimitiveParm("yfractFunction") SimFunction yfractFunction = null;

    @PrimitiveParm("magnification") double mag = 1.0;

    @PrimitiveParm("logmagnificationRate") double logmagRate = 0.0;

    @PrimitiveParm("magnificationFunction") SimFunction magFunction = null;


    Model3D model;
    /**
     * Set the model that the factory will use to configure the objects it
     * creates.
     * @param model the model
     */
    public void setModel(Model3D model) {
	this.model = model;
    }


    Animation2D animation;

    Model3DViewParmManager<Obj> pm;

    /**
     * Constructor.
     * @param animation the animation
     */
    protected AbstrM3DViewFactory(Animation2D animation) {
	super(animation);
	this.animation = animation;
	pm = new Model3DViewParmManager<Obj>(this);
	initParms(pm, AbstrM3DViewFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }


    /**
     * Request subclasses to add entries to the timeline.
     *
     * Each subclass adding entries should create a Callable that
     * performs any necessary operations at the specified time, and
     * that Callable should be returned via a call to
     * addToTimelineResponse. Each subclass that implements
     * addToTimelineRequest must call
     * super.addToTimelineRequest(object, key, time)

     * as the first statement in addToTimelineRequest.
     * @param object the object being configured.
     * @param key the timeline key
     * @param time the time for the timeline entry
     */
    protected void addToTimelineRequest(final Obj object,
					int key,
					double time)
    {
	super.addToTimelineRequest(object, key, time);
	Timeline entry = timelineMap.get(key);
	// to be filled in
	final boolean setChangeScale = entry.changeScale != null;
	final boolean xChangeScale = entry.changeScale == null? false:
	    entry.changeScale;
	final boolean xforceScaleChange = entry.forceScaleChange;
	final boolean setCoords =
	    (entry.phi != null && entry.theta != null && entry.psi != null);
	final double xphi = (entry.phi == null)? 0.0: Math.toRadians(entry.phi);
	final double xtheta = (entry.theta == null)? 0.0:
	    Math.toRadians(entry.theta);
	final double xpsi = (entry.psi == null)? 0.0: Math.toRadians(entry.psi);
	final boolean setCoordsF =
	    entry.phiFunction != null
	    && entry.thetaFunction != null
	    && entry.psiFunction != null;
	final SimFunction phiF = (entry.phiFunction == null)? null:
	    entry.phiFunction;
	final SimFunction thetaF = (entry.thetaFunction == null)? null:
	    entry.thetaFunction;
	final SimFunction psiF = (entry.psiFunction == null)? null:
	    entry.psiFunction;

	final boolean usePhiRate = (entry.phiRate != null && phiF == null);
	final boolean useThetaRate = (entry.thetaRate != null);
	final boolean usePsiRate = (entry.psiRate != null);
	final double xphiRate = (entry.phiRate == null)? 0.0:
	    Math.toRadians(entry.phiRate);
	final double xthetaRate =
	    (entry.thetaRate == null)? 0.0: Math.toRadians(entry.thetaRate);
	final double xpsiRate =
	    (entry.psiRate == null)? 0.0: Math.toRadians(entry.psiRate);

	final boolean setlsCoords =
	    (entry.lsPhi != null && entry.lsTheta != null);
	final boolean setlsCoordsF =
	    entry.lsPhiFunction != null && entry.lsThetaFunction != null;
	final double lsPhi = (entry.lsPhi == null)? 0.0:
	    Math.toRadians(entry.lsPhi);
	final double lsTheta = (entry.lsTheta == null)? 0.0:
	    Math.toRadians(entry.lsTheta);
	final SimFunction lsPhiF =
	    (entry.lsPhiFunction == null)? null: entry.lsPhiFunction;
	final SimFunction lsThetaF =
	    (entry.lsThetaFunction == null)? null: entry.lsThetaFunction;
	final boolean useLsPhiRate =
	    (entry.lsPhiRate != null && lsPhiF == null);
	final boolean useLsThetaRate =
	    (entry.lsThetaRate != null && lsThetaF == null);
	final double lsPhiRate =
	    (entry.lsPhiRate == null)? 0.0: Math.toRadians(entry.lsPhiRate);
	final double lsThetaRate =
	    (entry.lsThetaRate == null)? 0.0: Math.toRadians(entry.lsThetaRate);

	final SimFunction xfractF =
	    (entry.xfractFunction == null)? null: entry.xfractFunction;
	final SimFunction yfractF =
	    (entry.yfractFunction == null)? null: entry.yfractFunction;
	final boolean useXfract = entry.xfract != null;
	final boolean useYfract = entry.yfract != null;
	final double xfract = entry.xfract == null? 0.0: entry.xfract;
	final double yfract = entry.yfract == null? 0.0: entry.yfract;
	final boolean useXfractRate =
	    entry.xfractRate != null && xfractF == null;
	final boolean useYfractRate =
	    entry.yfractRate != null && yfractF == null;
	final double xfractRate =
	    (entry.xfractRate == null)? 0.0: entry.xfractRate;
	final double yfractRate =
	    (entry.yfractRate == null)? 0.0: entry.yfractRate;

	final SimFunction magF =
	    entry.magFunction == null? null: entry.magFunction;
	final boolean useMag = entry.mag != null && magF == null;
	final double mag = entry.mag == null? 1.0: entry.mag;
	final boolean useLogmagRate = entry.logmagRate != null && magF == null;
	final double logmagRate =
	    entry.logmagRate == null? 0.0: entry.logmagRate;
	final boolean useColorFactor = entry.colorFactor != null;
	final double colorFactor =
	    entry.colorFactor == null? 0.0: entry.colorFactor;

	addToTimelineResponse(new Callable() {
		public void call() {
		    if (setChangeScale) {
			object.setChangeScale(xChangeScale);
		    }
		    if (xforceScaleChange) {
			object.forceScaleChange();
		    }
		    if (setCoords) {
			object.setCoordRotationBySF(null, null, null);
			object.setCoordRotation(xphi, xtheta, xpsi);
		    }
		    if (setCoordsF) {
			object.setCoordRotationBySF(phiF, thetaF, psiF);
		    }
		    if (usePhiRate) object.setPhiRate(xphiRate);
		    if (useThetaRate) object.setThetaRate(xthetaRate);
		    if (usePsiRate) object.setPsiRate(xpsiRate);
		    if (setlsCoords){
			object.setLightSourceBySF(null, null);
			object.setLightSource(lsPhi, lsTheta);
		    }
		    if (setlsCoordsF) {
			object.setLightSourceBySF(lsPhiF, lsThetaF);
		    }
		    if (useLsPhiRate) object.setLightSourcePhiRate(lsPhiRate);
		    if (useLsThetaRate) {
			object.setLightSourceThetaRate(lsThetaRate);
		    }
		    if (useXfract) {
			object.setXFractBySF(null);
			object.setXFract(xfract);
		    }
		    if (xfractF != null) object.setXFractBySF(xfractF);
		    if (useYfract) {
			object.setYFractBySF(null);
			object.setYFract(xfract);
		    }
		    if (yfractF != null) object.setYFractBySF(yfractF);
		    if (useXfractRate) object.setXFractRate(xfractRate);
		    if (useYfractRate) object.setYFractRate(yfractRate);

		    if (useMag) {
			object.setMagnificationBySF(null);
			object.setMagnification(mag);
		    }
		    if (magF != null) {
			object.setMagnificationBySF(magF);
		    }
		    if (useLogmagRate) {
			object.setLogMagnificationRate(logmagRate);
		    }
		    /*
		    if (useZorder) {
			object.setZorder(zorder);
		    }
		    if (usevis) {
			object.setVisible(visible);
		    }
		    */
		    if (useColorFactor)object.setColorFactor(colorFactor);
		}
	    });
    }

    /*
     * Allows a subclass to provide a Callable in response to a superclass
     * calling addToTimelineRequest.  The callable will be executed at the
     * time associated with the timeline entry indexed by the key passed to
     * {@link #addToTimelineRequest}.
     * @param callable the Callable provided by the subclass.
    protected final void addToTimelineResponse(Callable callable) {
	if (subclassTimeline != null && callable != null) {
	    subclassTimeline.add(callable);
	}
    }
     */

    private ArrayList<Callable>subclassTimeline = new ArrayList<>();

    static class TimedCallListEntry {
	Callable c;
	long time;

	public TimedCallListEntry(Callable c, long time) {
	    this.c = c;
	    this.time = time;
	}
    }

    LinkedList<TimedCallListEntry> ctlist = new LinkedList<>();

    @Override
    protected void initObject(final Obj object) {
	super.initObject(object);

	object.setModel(model);
	object.setInitialViewingTime(time0);

	object.setChangeScale(changeScale);
	object.setColorFactor(colorFactor);
	object.setBorder(border);
	object.setBacksideColor((backsideColor == null)? null:
				backsideColor.createColor());
	object.setEdgeColor((edgeColor == null)? null:
				edgeColor.createColor());
	object.setDefaultSegmentColor((segmentColor == null)? null:
				      segmentColor.createColor());
	object.setDefaultBacksideSegmentColor
	    ((backsideSegmentColor == null)? null:
	     backsideSegmentColor.createColor());

	object.setCoordRotation(Math.toRadians(phi),
				Math.toRadians(theta),
				Math.toRadians(psi));
	SimFunction phiF = (phiFunction == null)? null: phiFunction;
	SimFunction thetaF = (thetaFunction == null)? null: thetaFunction;
	SimFunction psiF = (psiFunction == null)? null: psiFunction;
	object.setCoordRotationBySF(phiF, thetaF, psiF);
	object.setPhiRate(Math.toRadians(phiRate));
	object.setThetaRate(Math.toRadians(thetaRate));
	object.setPsiRate(Math.toRadians(psiRate));
	object.setLightSource(Math.toRadians(lsPhi), Math.toRadians(lsTheta));
	object.setLightSourcePhiRate(Math.toRadians(lsPhiRate));
	object.setLightSourceThetaRate(Math.toRadians(lsThetaRate));
	SimFunction lsPhiF = (lsPhiFunction == null)? null: lsPhiFunction;
	SimFunction lsThetaF = (lsThetaFunction == null)? null: lsThetaFunction;
	object.setLightSourceBySF(lsPhiF, lsThetaF);
	object.setXFract(xfract);
	object.setYFract(yfract);
	object.setXFractRate(xfractRate);
	object.setYFractRate(yfractRate);
	SimFunction xfractF = (xfractFunction == null)? null: xfractFunction;
	SimFunction yfractF = (yfractFunction == null)? null: yfractFunction;
	object.setXFractBySF(xfractF);
	object.setYFractBySF(yfractF);
	object.setMagnification(mag);
	object.setLogMagnificationRate(logmagRate);
	SimFunction magF = (magFunction == null)? null: magFunction;
	object.setMagnificationBySF(magF);
    }
}

//  LocalWords:  DView DViewFactory IFRAME SRC px steelblue HREF
//  LocalWords:  DViewParmManager PrimitiveParm phiRate thetaRate
//  LocalWords:  psiRate phiFunction thetaFunction psiFunction xfract
//  LocalWords:  lightsourcePhi lightsourceTheta lightsourcePhiRate
//  LocalWords:  lightsourceThetaRate lightsourcePhiFunction yfract
//  LocalWords:  lightsourceThetaFunction xfractRate yfractRate
//  LocalWords:  xfractFunction yfractFunction logmagnificationRate
//  LocalWords:  magnificationFunction zorder colorFactor changeScale
//  LocalWords:  forceScaleChange backsideColor edgeColor subclasses
//  LocalWords:  segmentColor backsideSegmentColor useZorder usevis
//  LocalWords:  addToTimelineResponse addToTimelineRequest setZorder
//  LocalWords:  setVisible superclass subclassTimeline
