package org.bzdev.obnaming.misc;

import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.graphs.Colors;
import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graph.FontParms;
import org.bzdev.graphs.Graph.Just;
import org.bzdev.graphs.Graph.BLineP;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

/**
 * Class to provide named-object-factory parameters specifying
 * instances of Graph.FontParms.
 * <P>
 * Parameter names will be compound names, typically using a period as a
 * separator. When this class is used, multiple parameters will be defined,
 * and the last component of the parameter name will be the following:
 * <ul>
 *    <li> "name" - the name of a font or font family.
 *    <li> "justification" - the font justification
 *          (<CODE>LEFT</CODE>, <CODE>RIGHT</CODE>, or
 *          <CODE>CENTER</CODE>).
 *    <li> "baselinePosition" - the position of the line used to
 *          position a font up or down. Values may be
 *          <CODE>TOP</CODE>, <CODE>CENTER</CODE>, <CODE>BASE</CODE>,
 *          or <CODE>BOTTOM</CODE>.
 *    <li> "angle" - the angle in degrees of the
 *         font from horizontal, measured counterclockwise in user space.
 *    <li> "size" - the size of the font (a positive integer)
 *    <li> "style" - the style of the font (<CODE>PLAIN</CODE>,
 *         <CODE>ITALIC</CODE>, <CODE>BOLD</CODE>, or
 *         <CODE>BOLD_ITALIC</CODE>).
 *    <li> "color.red" - the red intensity of the font color,
 *          an integer in the range [0, 255].
 *    <li> "color.green" - the green intensity of the font
 *          color, an integer in the range [0, 255].
 *    <li> "color.blue" - the blue intensity of the font color,
 *          an integer in the range [0, 255].
 *    <li> "color.alpha" - the alpha intensity of the font
 *          color,an integer in the range [0, 255] with 0 indicating
 *          transparent and 255 indicating opaque.
 * </ul>
 * The constants shown in upper case are enumeration constants for
 * the following enumeration classes:
 * <ul>
 *   <it> {@link org.bzdev.graphs.Graph.Just}
 *   <it> {@link org.bzdev.graphs.Graph.BLineP}
 *   <it> {@link org.bzdev.obnaming.misc.GraphFontParm.FontStyle}
 * </ul>
 * @see org.bzdev.graphs.Graph.Just
 * @see org.bzdev.graphs.Graph.BLineP
 * @see org.bzdev.obnaming.misc.GraphFontParm.FontStyle
 */
@CompoundParmType(tipResourceBundle ="*.lpack.GraphFontParmTips",
		  labelResourceBundle = "*.lpack.GraphFontParmLabels",
		  docResourceBundle = "*.lpack.GraphFontParmDocs")
public class GraphFontParm {
    /**
     * Font style specification.
     */
    public static enum FontStyle {
	/**
	 * Plain font style.
	 */
	PLAIN(Font.PLAIN),
	/**
	 * Italic font style.
	 */
	ITALIC(Font.ITALIC),
	/**
	 * Bold font style.
	 */
	BOLD(Font.BOLD),
	/**
	 * Bold, Italic font style.
	 */
	BOLD_ITALIC(Font.BOLD | Font.ITALIC);

	private int value;
	private FontStyle(int value) {this.value = value;}
	/**
	 * Get the integer constant matching a Cap enum constant.
	 * @return the integer constant defined by
	 * {@link java.awt.BasicStroke BasicStroke}
	 */
	public int getValue() {
	    return value;
	}
    }
    
    /**
     * Font justification.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm("justification")
	public Graph.Just justification = Graph.Just.LEFT;

    /**
     * Font baseline position.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm("baselinePosition")
	public Graph.BLineP baselinePosition = Graph.BLineP.BASE;

    /**
     * Font angle.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm("angle") public double fontAngle = 0.0;

    /**
     * Font name.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm("name") public String name = "sansserif";

    /**
     * Font style.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm("style") public FontStyle fstyle = FontStyle.BOLD;

    /**
     * Font size.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value = "size", lowerBound = "1") public int size = 14;

    /**
     * The CSS specification of a color.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value="color.css")
    public String css = null;

    /**
     * The red component of a color.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value="color.red",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer red = null;

    /**
     * The green component of a color.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value="color.green",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer green = null;

    /**
     * The blue component of a color.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value="color.blue",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer blue = 0;

    /**
     * The alpha component of a color.
     * This field is public because the parameter managers that
     * use it are defined outside of this package.  It should
     * not be used for other purposes.
     */
    @PrimitiveParm(value="color.alpha",
		   lowerBound = "0",
		   upperBound = "255")
    public Integer alpha = null;


    /**
     * Constructor.
     */
    public GraphFontParm() {}

    /**
     * Constructor with optional null value for the font name.
     * @param nullValue true if the font name is null; false to use the
     *        default font name.
     */
   public GraphFontParm(boolean nullValue) {
	this();
	if (nullValue) name = null;
    }

    /**
     * Constructor given a complete description of parameters
     * The parameters 'justification' and 'baselinePosition' determine
     * the text justification and baseline Position relative to the x-y
     * coordinate used to specify a string's location.
     * @param name the name of the font
     * @param style the style of the font
     * @param size the size of the font
     * @param color the color of the font
     * @param justification LEFT for left-justified strings; CENTER for
     *        strings that are centered, and RIGHT for strings that are
     *        right justified
     * @param baselinePosition TOP for text aligned at its top; CENTER
     *        for text aligned at its center; BASE for text aligned at
     *        its baseline, and BOTTOM for text aligned at its bottom.
     */
    public GraphFontParm(String name,
			  FontStyle style,
			  int size,
			  Color color,
			  Graph.Just justification,
			  Graph.BLineP baselinePosition,
			  double angle) {
	this.name = name;
	this.fstyle = style;
	this.size = size;
	this.justification = justification;
	this.baselinePosition = baselinePosition;
	this.fontAngle = angle;
	css = null;
	red = color.getRed();
	green = color.getGreen();
	blue = color.getBlue();
	alpha = color.getAlpha();
    }


    /**
     * Create font parameters
     * @return the font parameters
     */
    public Graph.FontParms createFontParms() {
	if (name == null) return null;
	Graph.FontParms fp = new Graph.FontParms();
	int r = 0;
	int g = 0;
	int b = 0;
	int a = 255;
	if (css != null) {
	    int[] components = Colors.getComponentsByCSS(css);
	    r = components[0];
	    g = components[1];
	    b = components[2];
	    if (components.length == 4) {
		a = components[3];
	    }
	}
	if (red != null) r = red;
	if (green != null) g = green;
	if (blue != null) b = blue;
	if (alpha != null) a = alpha;
	Color color = new Color(r, g, b, a);
	fp.setBaseline(baselinePosition);
	fp.setJustification(justification);
	fp.setAngle(fontAngle);
	fp.setColor(color);
	fp.setFont(new Font(name, fstyle.getValue(), size));
	return fp;
    }
}

//  LocalWords:  FontParms ul li baselinePosition enum BasicStroke
//  LocalWords:  sansserif css nullValue
