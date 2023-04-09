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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

/** Abstract Factory for creating instances of {@link ConnectingLine2D}.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link ConnectingLine2DFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/ConnectingLine2DFactory.html" style="width:95%;height:600px;border: 3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/ConnectingLine2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 *
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Cap
 * @see org.bzdev.obnaming.misc.BasicStrokeParm.Join
 * @see java.awt.BasicStroke
 * @see java.awt.Color
 */
@FactoryParmManager(value = "ConnLine2DParmManager",
		    tipResourceBundle = "*.lpack.ConnLine2DTips",
		    labelResourceBundle = "*.lpack.ConnLine2DLabels",
		    docResourceBundle = "*.lpack.ConnLine2DDocs",
		    stdFactory="ConnectingLine2DFactory",
		    namerVariable="a2d",
		    namerDocumentation="the animation")
public abstract class AbstrConnLine2DFactory<Obj extends ConnectingLine2D>
    extends AnimationObject2DFactory<Obj>
{
    ConnLine2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation associated with this factory
     */
    protected AbstrConnLine2DFactory(Animation2D a2d) {
	super(a2d);
	pm =  new ConnLine2DParmManager<Obj>(this);
	initParms(pm, AbstrConnLine2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @CompoundParm("color")
    ColorParm colorParm = new ColorParm.BLACK();

    @CompoundParm("stroke")
	BasicStrokeParm strokeParm = new BasicStrokeParm.ONE();
    
    @PrimitiveParm(value = "u1", lowerBound = "0.0", lowerBoundClosed = true,
		   upperBound = "1.0", upperBoundClosed = false)
	double u1 = 0.0;

    @PrimitiveParm(value = "u2", lowerBound = "0.0", lowerBoundClosed = false,
		   upperBound = "1.0", upperBoundClosed = true)
	double u2 = 1.0;

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	object.configure(u1, u2);
	object.setStroke(strokeParm.createBasicStroke());
	object.setGcsMode(strokeParm.getGcsMode());
	object.setColor(colorParm.createColor());
    }
}

//  LocalWords:  ConnectingLine DFactory IFRAME SRC px steelblue HREF
//  LocalWords:  ConnLine DParmManager
