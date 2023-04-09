package org.bzdev.anim2d;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Map;
import java.util.TreeMap;

import org.bzdev.geom.SplinePathBuilder;
import org.bzdev.obnaming.annotations.CompoundParm;
import org.bzdev.obnaming.annotations.FactoryParmManager;
import org.bzdev.obnaming.NamedObjectFactory.ConfigException;
import org.bzdev.obnaming.NamedObjectOps;
import org.bzdev.obnaming.Parm;
import org.bzdev.obnaming.ParmParser;
import org.bzdev.obnaming.annotations.KeyedCompoundParm;
import org.bzdev.obnaming.annotations.KeyedPrimitiveParm;
import org.bzdev.obnaming.annotations.PrimitiveParm;
import org.bzdev.obnaming.misc.*;
import org.bzdev.math.rv.*;

//@exbundle org.bzdev.anim2d.lpack.Animation2D

/**
 * Abstract factory for animation shapes.
 * In addition to controlling the stacking order (z-order) and visibility,
 * this factory can configure an animation shape by adding animation paths
 * to. Each path provides part of a shape's outline.  A winding rule is used
 * to determine which paths represent an interior or exterior boundary for
 * the shape.  The Z-order and visibility will be ignored by animation layers.
 * When an animation shape is used as part of an animation layer, its
 * visibility should be set to <CODE>false</CODE>, which is the default for
 * this factory.
 * <P>
 * This factory provides a method
 * {@link AbstrAnimShape2DFactory#set(String,int,Object)} that will accept
 * instances of {@link java.awt.Shape}, {@link java.awt.geom.PathIterator},
 * {@link String}, and {@link AnimationPath2D} as its third argument. If a
 * string is provided, the string must be the name of an instance of
 * {@link AnimationPath2D} associated with the factory's animation.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link AnimationShape2DFactory}
 * (the documentation for
 * <CODE><A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html#object.type-org.bzdev.anim2d.AnimationShape2DFactory" target="ftable">object.type</A></CODE>
 * describes which "object" parameters are used for a given type):
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html" style="width:95%;height:500px;border: 3px solid steelblue" name="ftable">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/anim2d/AnimationShape2DFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * @see AnimationShape2D
 */


@FactoryParmManager(value = "AnimationShape2DParmManager",
		    tipResourceBundle = "*.lpack.AnimShapeTips",
		    labelResourceBundle = "*.lpack.AnimShapeLabels",
		    docResourceBundle = "*.lpack.AnimShapeDocs")
public abstract class AbstrAnimShape2DFactory<Obj extends AnimationShape2D>
    extends AnimationObject2DFactory<Obj>
{
    static String errorMsg(String key, Object... args) {
	return Animation2D.errorMsg(key, args);
    }

    Animation2D a2d;
    @PrimitiveParm("windingRule")
	SplinePathBuilder.WindingRule windingRule =
	SplinePathBuilder.WindingRule.WIND_NON_ZERO;

    // @PrimitiveParm("draw") boolean draw = false;
    // @PrimitiveParm("fill") boolean fill = false;

    @CompoundParm("drawColor") ColorParm drawColorParm =
	new ColorParm();

    @CompoundParm("fillColor") ColorParm fillColorParm =
	new ColorParm();

    @CompoundParm("stroke")
    BasicStrokeParm strokeParm = new BasicStrokeParm(1.0);

    @KeyedPrimitiveParm("shape") Map<Integer,AnimationPath2D> apathMap
	= new TreeMap<Integer,AnimationPath2D>();

    Map<Integer,Path2D> pathMap = new TreeMap<Integer,Path2D>();

    AnimationShape2DParmManager<Obj> pm;

    /**
     * Constructor.
     * @param a2d the animation
     */
    protected AbstrAnimShape2DFactory(Animation2D a2d) {
	super(a2d);
	this.a2d = a2d;
	setAnimationObjectDefaults(false, getZorder());
	pm = new AnimationShape2DParmManager<Obj>(this);
	initParms(pm, AbstrAnimShape2DFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
	pathMap.clear();
    }


    @Override
    public void clear(String name) {
	super.clear(name);
	if (name.equals("shape")) {
	    pathMap.clear();
	}
    }

    @Override
    public void remove(String name, int key) {
	if (name.equals("shape")) {
	    pathMap.remove(key);
	} else {
	    super.remove(name, key);
	}
    }

    @Override
    public void unset(String name, int index) {
	if (name.equals("shape")) {
	    pathMap.remove(index);
	} else {
	    super.unset(name, index);
	}
    }
    
    /**
     * Set a value provided as a Shape, PathIterator, AnimationPath2D, or any
     * of the other objects supported by factories, given an index.
     * When the name is "shape", the value can be a Shape, PathIterator,
     * or AnimationPath2D. Otherwise it can be any type acceptable for
     * the entry name.
     * @param name the name of the entry
     * @param key the index
     * @param value the object providing or specifying a value
     * @exception ConfigException an exception encapsulating an
     *            IllegalArgumentException if an argument is out of bounds
     *            or the name does not match a parameter;
     *            an UnsupportedOperationException if the factory
     *            does not allow this method to be used;
     *            an IllegalStateException if the factory is not in
     *            a state for which this value may be parsed and entered;
     *            IndexOutOfBoundsException if the index is not in a legal
     *            range
     * @exception IllegalArgumentException the value had the wrong type
     */
    public void set(String name, int key, Object value)
	throws ConfigException
    {
	if (name.equals("shape")) {
	    if (value instanceof AnimationPath2D) {
		pathMap.put(key, ((AnimationPath2D)value).getPath());
	    } else if (value instanceof Shape) {
		Path2D path = new Path2D.Double((Shape)value);
		pathMap.put(key, path);
	    } else if (value instanceof PathIterator) {
		Path2D path = new Path2D.Double();
		path.append((PathIterator)value, false);
		pathMap.put(key, path);
	    } else if (value instanceof String) {
		Object obj = a2d.getObject((String)value);
		if (obj != null && obj instanceof AnimationPath2D) {
		    AnimationPath2D apath = (AnimationPath2D) obj;
		    pathMap.put(key, apath.getPath());
		}
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("keyedValType", name, key));
	    }
	} else {
	    if (value instanceof BooleanRandomVariable) {
		set(name, key, (BooleanRandomVariable)value);
	    } else if (value instanceof BooleanRandomVariableRV) {
		set(name, key, (BooleanRandomVariableRV)value);
	    } else if (value instanceof DoubleRandomVariable) {
		set(name, key, (DoubleRandomVariable)value);
	    } else if (value instanceof DoubleRandomVariableRV) {
		set(name, key, (DoubleRandomVariableRV)value);
	    } else if (value instanceof IntegerRandomVariable) {
		set(name, key, (IntegerRandomVariable)value);
	    } else if (value instanceof IntegerRandomVariableRV) {
		set(name, key, (IntegerRandomVariableRV)value);
	    } else if (value instanceof LongRandomVariable) {
		set(name, key, (LongRandomVariable)value);
	    } else if (value instanceof LongRandomVariableRV) {
		set(name, key, (LongRandomVariableRV)value);
	    } else if (value instanceof NamedObjectOps) {
		set(name, key, (NamedObjectOps)value);
	    } else if (value instanceof Enum<?>) {
		set(name, key, (Enum<?>)value);
	    } else if (value instanceof String) {
		set(name, key, (String)value);
	    } else {
		throw new IllegalArgumentException
		    (errorMsg("keyedValType", name, key));
	    }
	}
    }

    @Override
    protected void initObject(Obj obj) {
	super.initObject(obj);
	obj.setDrawColor(drawColorParm.createColor());
	obj.setFillColor(fillColorParm.createColor());
	obj.setStroke(strokeParm.createBasicStroke());
	obj.setGCSMode(strokeParm.getGcsMode());
	obj.setShape(windingRule);
	for (AnimationPath2D p: apathMap.values()) {
	    obj.appendShape(p.getPath());
	}
	for (Path2D p: pathMap.values()) {
	    obj.appendShape(p);
	}
    }
}

//  LocalWords:  exbundle AnimationShape DFactory HREF ftable IFRAME
//  LocalWords:  SRC DParmManager windingRule PrimitiveParm boolean
//  LocalWords:  drawColor fillColor PathIterator AnimationPath
//  LocalWords:  ConfigException IllegalArgumentException
//  LocalWords:  UnsupportedOperationException IllegalStateException
//  LocalWords:  IndexOutOfBoundsException keyedValType
//  LocalWords:  AbstrAnimShape
