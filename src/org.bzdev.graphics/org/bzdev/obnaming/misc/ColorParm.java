package org.bzdev.obnaming.misc;
import org.bzdev.graphs.Colors;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import java.awt.Color;

/**
 * Factory support class for defining colors.
 * If no color is provided in a constructor and no color component is set,
 * the createColor method will return null.  If a default color is desired,
 * use the constructor that takes an instance of Color as its argument.
 * The factory 'set' methods will not allow a color component to be set
 * to null - instead one must clear the field to restore the default
 * (whether null or a specific color).
 * <P>
 * Parameter names will be compound names, typically using a period as a
 * separator. When this class is used, multiple parameters will be defined,
 * and the last component of the parameter name will be the following:
 * <ul>
 *   <li><code>css</code> - a string giving a color in a format acceptable
 *       to CSS as specified in a
 *       <A href="https://www.w3.org/TR/css-color-3/">W3C recommendation</A>
 *       for CSS level 3; null if CSS is not used.
 *   <li><code>red</code> - an integer in the range [0,255] giving the red
 *       component of the color (overrides the red component of the css
 *        option when the css parametercss is not null).
 *   <li><code>green</code> - an integer in the range [0,255] giving the green
 *       component of the color (overrides the green component of the css
 *        option when the css parametercss is not null).
 *   <li><code>blue</code> - an integer in the range [0,255] giving the blue
 *       component of the color (overrides the blue component of the
 *       css option when the css parametercss is not null).
 *   <li><code>alpha</code> - an integer in the range [0,255] giving the alpha
 *       component of the color. If null, the value will be 255 unless
 *       the css parameter specifies a value of alpha, in which case that
 *       value will be used.
 * </ul>
 * If all of these parameters are null, a default color will be used
 * (which may be null).
 */
@CompoundParmType(tipResourceBundle = "*.lpack.ColorParmTips",
		  labelResourceBundle = "*.lpack.ColorParmLabels",
		  docResourceBundle = "*.lpack.ColorParmDocs")
public class ColorParm {
    /**
     * A CSS specification for a color.
     */
    @PrimitiveParm(value="css")
    public String css = null;

    /**
     * The red component of a color.
     */
    @PrimitiveParm(value="red",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer red = null;

    /**
     * The green component of a color.
     */
    @PrimitiveParm(value="green",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer green = null;

    /**
     * The blue component of a color.
     */
    @PrimitiveParm(value="blue",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer blue = null;


    /**
     * The alpha component of a color.
     */
    @PrimitiveParm(value="alpha",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer alpha = null;

    // will be ignored if any of the above is set.
    private String defaultCSS = null;
    private Integer defaultRed = null;
    private Integer defaultGreen = null;
    private Integer defaultBlue = null;
    private Integer defaultAlpha = null;

    /**
     * An instance of ColorParm whose default constructor
     * returns an object initialized to the CSS color specification
     * "black". An object of this type can be used in an initializer
     * but not as the type of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     */
    public static final class BLACK extends ColorParm {
	/**
	 * Constructor.
	 */
	public BLACK() {
	    super("black");
	}
    }

    /**
     * An instance of ColorParm whose default constructor
     * returns an object initialized to the CSS color specification
     * "red". An object of this type can be used in an initializer
     * but not as the type of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     */
    public static final class RED extends ColorParm {
	/**
	 * Constructor.
	 */
	public RED() {
	    super("red");
	}
    }

    /**
     * An instance of ColorParm whose default constructor
     * returns an object initialized to the CSS color specification
     * "green". An object of this type can be used in an initializer
     * but not as the type of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     */
    public static final class GREEN extends ColorParm {
	/**
	 * Constructor.
	 */
	public GREEN() {
	    super("green");
	}
    }

    /**
     * An instance of ColorParm whose default constructor
     * returns an object initialized to the CSS color specification
     * "blue". An object of this type can be used in an initializer
     * but not as the type of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     */
    public static final class BLUE extends ColorParm {
	/**
	 * Constructor.
	 */
	public BLUE() {
	    super("blue");
	}
    }

    /**
     * An instance of ColorParm whose default constructor
     * returns an object initialized to the CSS color specification
     * "WHITE". An object of this type can be used in an initializer
     * but not as the type of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     */
    public static final class WHITE extends ColorParm {
	/**
	 * Constructor.
	 */
	public WHITE() {
	    super("white");
	}
    }


    /**
     * Constructor.
     * Note: unless the fields are modified, a call to
     * {@link #createColor()} will return null.
     * To initialize a variable annotated with a {@literal @}CompoundParm
     * annotation, use one the inner classes or create a subclass with
     * a zero-argument constructor that will create the desired color.
     */
    public ColorParm() {}

    /**
     * Constructor given a color.
     * The color provided can be changed by setting  the red, green,
     * blue, or css fields. This constructor should not be
     * used to initialize the value of a variable annotated with a
     * {@literal @}CompoundParm annotation.
     * To initialize a variable annotated with a {@literal @}CompoundParm
     * annotation, use one the inner classes or create a subclass with
     * a zero-argument constructor that will create the desired color.
     * @param c the color used to configure this object
     */
    public ColorParm(Color c) {
	defaultRed = c.getRed();
	defaultGreen = c.getGreen();
	defaultBlue = c.getBlue();
	defaultAlpha = c.getAlpha();
    }

    /**
     * Constructor given a CSS color specification
     * This constructor should not be used to initialize the value of a
     * variable annotated with a {@literal @}CompoundParm annotation.
     * To initialize a variable annotated with a {@literal @}CompoundParm
     * annotation, use one the inner classes or create a subclass with
     * a zero-argument constructor that will create the desired color.
     * @param spec the CSS color specification
     * @exception IllegalArgumentException if the specification was ill formed
     */
    public ColorParm(String spec) throws IllegalArgumentException {
	// called to syntax check the argument
	Colors.getComponentsByCSS(spec);
	defaultCSS = spec;
	css = spec;
    }

    /**
     * Get the red component for a colorParm.
     * If other color components are non-null and the red component is null,
     * a default value of 0 is returned. Otherwise if the CSS component
     * is non-null, the red component of that color is returned. Otherwise
     * if a default was provided in a constructor, the red component of that
     * color is returned.
     * @return the red component of the color; null if no color
     *         components were provided either explicitly or by specifying
     *         a default color in a constructor.
     */
    public Integer getRed() {
	Color c = createColor();
	return (c == null)? null: c.getRed();
    }

    /**
     * Get the green component for a colorParm.
     * If other color components are non-null and the green component is null,
     * a default value of 0 is returned. Otherwise if the CSS component
     * is non-null, the red component of that color is returned. Otherwise
     * if a default was provided in a constructor, the green component of that
     * color is returned.
     * @return the green component of the color; null if no color
     *         components were provided either explicitly or by specifying
     *         a default color in a constructor.
     */
    public Integer getGreen() {
	Color c = createColor();
	return (c == null)? null: c.getGreen();
    }

    /**
     * Get the blue component for a colorParm.
     * If other color components are non-null and the blue component is null,
     * a default value of 0 is returned. Otherwise if the CSS component
     * is non-null, the blue component of that color is returned. Otherwise
     * if a default was provided in a constructor, the red component of that
     * color is returned.
     * @return the blue component of the color; null if no color
     *          components were provided either explicitly or by specifying
     *         a default color in a constructor.
     */
    public Integer getBlue() {
	Color c = createColor();
	return (c == null)? null: c.getBlue();
    }

    /**
     * Get the alpha component for a colorParm.
     * If other color components are non-null and the alpha component is null,
     * a default value of 255 is returned. Otherwise if a default alpha
     * component was provided, that is returned, but if a default alpha
     * component was not provided (i.e., was null), but other defaults
     * were provided, 255 is returned.
     * @return the alpha component of the color; null if no color
     *         components were provided either explicitly or by specifying
     *         a default value in a constructor.
     */
    public Integer getAlpha() {
	if (red == null && green == null && blue == null && alpha == null) {
	    if (defaultRed == null && defaultGreen == null
		&& defaultBlue == null && defaultAlpha == null) {
		if (defaultCSS == null) {
		    return null;
		} else {
		    return 255;
		}
	    }
	    return (defaultAlpha == null)? 255: defaultAlpha;
	}
	return (alpha == null)? 255: alpha;
    }

    /**
     * Create the color specified by this object.
     * @return the color; null if no color was specified in a constructor
     *         and no parameter was set to a non-null value
     */
    public Color createColor() {
	int r = 0;
	int g = 0;
	int b = 0;
	int a = 255;
	if (css == null && red == null && green == null
	    && blue == null  && alpha == null) {
	    // use default.
	    if (defaultCSS != null) {
		int[] components = Colors.getComponentsByCSS(defaultCSS);
		r = components[0];
		g = components[1];
		b = components[2];
		if (components.length  == 4) {
		    a = components[3];
		}
	    } else if (defaultRed == null && defaultGreen == null
		       && defaultBlue == null && defaultAlpha == null) {
		return null;
	    } else {
		// default Red, Green, Blue are set in a constructor
		// that was given a Color as its argument
		r = defaultRed;
		g = defaultGreen;
		b = defaultBlue;
		a = defaultAlpha;
	    }
	    return new Color(r, g, b, a);
	}
	if (css != null) {
	    int[] components = Colors.getComponentsByCSS(css);
	    r = components[0];
	    g = components[1];
	    b = components[2];
	    if (components.length  == 4) {
		a = components[3];
	    }
	}

	r = (red == null)? r: red;
	g = (green == null)? g: green;
	b = (blue == null)? b: blue;
	a = (alpha == null)? a: alpha;
	return new Color(r, g, b, a);
    }
}

//  LocalWords:  createColor ul li css href ColorParm initializer
//  LocalWords:  CompoundParm colorParm
