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
 * Abstract Factory for CartesianGrid2D objects.
 * <P>
 * In addition to controlling the stacking order (z-order) and visibility,
 * this factory will configure a grid's parameters: the grid spacing,
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link CartesianGrid2DFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/CartesianGrid2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/CartesianGrid2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "Grid2DParmManager",
		    tipResourceBundle = "*.lpack.GridTips",
		    labelResourceBundle = "*.lpack.GridLabels",
		    docResourceBundle = "*.lpack.GridDocs")
public abstract class AbstrCartGrid2DFactory<Obj extends CartesianGrid2D>
    extends AnimationObject2DFactory<Obj>
{
    @PrimitiveParm(value="spacing", lowerBound="0.0", lowerBoundClosed=true)
    double spacing = 0.0;

    @PrimitiveParm(value="subspacing", lowerBound="1", lowerBoundClosed=true)
    int subspacing = 1;

    @CompoundParm("spacingColor")
    ColorParm spacingColorParm =
	new ColorParm(Graphs.CartesianGrid.SPACING_COLOR);

    @CompoundParm("subspacingColor")
    ColorParm subspacingColorParm =
	new ColorParm(Graphs.CartesianGrid.SUBSPACING_COLOR);

    @CompoundParm("axisColor")
    ColorParm axisColorParm = new ColorParm(Graphs.CartesianGrid.AXIS_COLOR);

    @PrimitiveParm(value = "strokeWidth",
		   lowerBound="0.0", lowerBoundClosed=false)
    double strokeWidth = Graphs.CartesianGrid.STROKE_WIDTH;

    Grid2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected AbstrCartGrid2DFactory(Animation2D a2d) {
	super(a2d);

	pm = new Grid2DParmManager<Obj>(this);
	initParms(pm, AbstrCartGrid2DFactory.class);
    }

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	object.setSpacing(spacing);
	object.setSubspacing(subspacing);
	object.setColors(axisColorParm.createColor(),
			 spacingColorParm.createColor(),
			 subspacingColorParm.createColor());
	object.setStrokeWidth(strokeWidth);
    }
}

//  LocalWords:  CartesianGrid DFactory IFRAME SRC px steelblue HREF
//  LocalWords:  ftable DParmManager subspacing spacingColor
//  LocalWords:  subspacingColor axisColor strokeWidth
