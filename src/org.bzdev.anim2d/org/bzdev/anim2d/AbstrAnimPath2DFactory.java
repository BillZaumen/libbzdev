package org.bzdev.anim2d;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.ParmManager;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.misc.ColorParm;
import org.bzdev.obnaming.misc.BasicStrokeParm;

import org.bzdev.geom.BasicSplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.geom.SplinePathBuilder.WindingRule;
import org.bzdev.geom.SplinePathBuilder.CPoint;


// import org.bzdev.devqsim.SimObjectFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract factory for creating AnimationPath2D objects.
 * <P>
 * Paths are described by a sequence of control points, represented by a
 * <code>cpoint</code> table with an integer key.
 * The parameter <CODE>cpoint.type</CODE> has  a value that
 * is an enumeration constant whose type is
 * {@link org.bzdev.geom.SplinePathBuilder.CPointType} As one increments the
 * key, the following transition diagram shows the sequence of allowed values
 * for this type:
 * <DIV style="text-align:center">
 *     <IMG SRC="doc-files/basicpath.png">
 *</DIV>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link AnimationPath2DFactory}
 * (the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html#cpoint.type-org.bzdev.anim2d.AnimationPath2DFactory" target="ftable">cpoint.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationPath2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>

 * @see org.bzdev.geom.SplinePathBuilder.CPointType
 * @see org.bzdev.geom.SplinePathBuilder.WindingRule
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Cap
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Join
 * @see java.awt.BasicStroke
 */

@FactoryParmManager(value = "AnimPath2DParmManager",
		    tipResourceBundle = "*.lpack.AnimPath2DTips",
		    labelResourceBundle = "*.lpack.AnimPath2DLabels",
		    docResourceBundle = "*.lpack.AnimPath2DDocs")
public abstract class AbstrAnimPath2DFactory<Obj extends AnimationPath2D>
    extends AnimationObject2DFactory<Obj>
{
    AnimPath2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation associated with this factory
     */
    protected AbstrAnimPath2DFactory(Animation2D a2d) {
	super(a2d);
	pm =  new AnimPath2DParmManager<Obj>(this);
	initParms(pm, AbstrAnimPath2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @KeyedCompoundParm("cpoint")
	Map<Integer,SplinePathBuilder.CPoint> cpointMap
	= new TreeMap<Integer,SplinePathBuilder.CPoint>();

    @PrimitiveParm("windingRule")
	SplinePathBuilder.WindingRule windingRule =
	SplinePathBuilder.WindingRule.WIND_NON_ZERO;

    @CompoundParm("color")
    ColorParm colorParm = new ColorParm.BLACK();

    @CompoundParm("stroke")
	BasicStrokeParm strokeParm = new BasicStrokeParm.ONE();

    @PrimitiveParm("showSegments")
    boolean showSegments = false;

    @PrimitiveParm("radius")
    double radius = 5.0;

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	SplinePathBuilder.CPoint[] cpoints =
	    new SplinePathBuilder.CPoint[cpointMap.size()];
	cpointMap.values().toArray(cpoints);
	BasicSplinePathBuilder builder = new BasicSplinePathBuilder();
	builder.initPath(windingRule, cpoints);
	object.setPath(builder.getPath());
	//object.initPath(windingRule, cpoints);
	object.setStroke(strokeParm.createBasicStroke());
	object.setGcsMode(strokeParm.getGcsMode());
	object.setColor(colorParm.createColor());
	object.showSegments(showSegments, radius);
    }
}

//  LocalWords:  AnimationPath cpoint IMG SRC DFactory HREF ftable px
//  LocalWords:  IFRAME steelblue AnimPath DParmManager windingRule
//  LocalWords:  showSegments initPath cpoints
