package org.bzdev.anim2d;
import org.bzdev.graphs.Graphs;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.CompoundParmType;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.obnaming.misc.*;

/**
 * Abstract Factory for PolarGrid objects.
 * Polar grids can be added to an animation's frame for a variety
 * of reasons, including their use as an aid while an animation is
 * being developed.
 * <P>
 * Polar grids are drawn about an origin specified in GCS (Graph
 * Coordinate Space).  The origin can be specified as absolute
 * coordinates or as fractional coordinates. Absolute coordinates
 * give the actual location in GCS units, where as fractional
 * coordinates are in the range [0.0, 1.0] and give the fractional
 * distance between the lower and upper X and Y coordinates for
 * an animation's graph. Absolute and fractional coordinates are
 * mutually exclusive. the default is to use the fractional coordinates
 * (0.5, 0.5), which centers a polar grid on an animation's frame
 * (excluding any offsets).  The radial spacing for concentric circles
 * and the angle between adjacent radial lines can also be specified.
 * When the radial spacing is zero, the radial spacing will be computed
 * to fit in a reasonable number of circles. Other parameters that may
 * be set provide the color of grid lines and the width of the strokes
 * used to draw them.
 * <P>
 * This factory inherits the ability to set the  visibility and
 * stacking order (z-order) for the objects it will create.
 * The factory parameters are the same as those provided by
 * its subclass {@link PolarGridFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/PolarGridFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/PolarGridFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "PolarGridParmManager",
		    tipResourceBundle = "*.lpack.PolarGridTips",
		    labelResourceBundle = "*.lpack.PolarGridLabels")
public abstract class AbstrPolarGridFactory<Obj extends PolarGrid>
    extends AnimationObject2DFactory<Obj>
{
    @PrimitiveParm(value="radialSpacing")
    double radialSpacing = 0.0;

    @PrimitiveParm(value="angularSpacing", lowerBound="1",
		   lowerBoundClosed=true)
    int angularSpacing = 10;

    @CompoundParm("color")
    ColorParm colorParm = new ColorParm(Graphs.PolarGrid.LINE_COLOR_CSS);

    @PrimitiveParm(value = "strokeWidth",
		   lowerBound="0.0", lowerBoundClosed=false)
    double strokeWidth = Graphs.PolarGrid.STROKE_WIDTH;

    @PrimitiveParm("fractional")
    boolean fractional  = true;

    @PrimitiveParm("xo")
    double xo = 0.5;

    @PrimitiveParm("yo")
    double yo = 0.5;

    PolarGridParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected AbstrPolarGridFactory(Animation2D a2d) {
	super(a2d);
	pm = new PolarGridParmManager<Obj>(this);
	initParms(pm, AbstrPolarGridFactory.class);
    }

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	object.setOrigin(xo, yo, fractional);
	object.setColor(colorParm.createColor());
	object.setRadialSpacing(radialSpacing);
	object.setAngularSpacing(angularSpacing);
    }
}

//  LocalWords:  PolarGrid GCS PolarGridFactory IFRAME SRC px ftable
//  LocalWords:  steelblue HREF PolarGridParmManager radialSpacing xo
//  LocalWords:  angularSpacing strokeWidth
