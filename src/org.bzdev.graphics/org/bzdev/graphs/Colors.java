package org.bzdev.graphs;
import org.bzdev.math.*;
import org.bzdev.util.DisjointSetsUnion;

import java.awt.*;
import java.awt.color.*;
import java.awt.image.ColorModel;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Locale;

//@exbundle org.bzdev.graphs.lpack.Graphs

/**
 * Class to provide various colors.
 * For colors related to blackbody radiation, this class is based on
 * the algorithm used in the article
 * <a href="http://www.physics.sfasu.edu/astro/color/spectra.html">
 * Approximate RGB values for Visible Wavelengths</a>,
 * <a href="http://www.fourmilab.ch/documents/specrend/">
 * Colour Rendering of Spectra</a>, and
 * <a href="http://www.fourmilab.ch/documents/specrend/specrend.c">
 * a public-domain C program</a>.
 * <P>
 * This implementation uses Java's ColorModel and ColorSpace classes
 * to transform colors from CIEXYZ color coordinates to those of the current
 * color model.  The color models can be obtained by calling the
 * getColorModel() method that are provided by the classes such as
 * {@link java.awt.image.BufferedImage}, {@link org.bzdev.graphs.Graph},
 * and {@link org.bzdev.gio.OutputStreamGraphics}. To get a color
 * model from a {@link java.awt.Graphics2D}, use the method
 * {@link java.awt.Graphics2D#getDeviceConfiguration()} and call its
 * {@link java.awt.GraphicsConfiguration#getColorModel()} method.
 * <P>
 * The methods for this class are static methods. These create a 
 * color that matches
 * <ul>
 *   <li> a specific wavelength.
 *   <li> a spectrum (the spectral radiance in terms of wavelength).
 *   <li> a CSS specification.
 * </ul>
 * In addition to creating a color from a CSS specification, there is
 * also a method that returns an integer array containing the RGB values
 * or the RGBA values for a color corresponding to a CSS specification.
 */
public class Colors {

    // Only static methods, so no need for a public constructor.
    private Colors() {}

    static String errorMsg(String key, Object... args) {
	return Graph.errorMsg(key, args);
    }

    static TreeMap<Integer,String> invMap = null;

    private synchronized static void invMapInit() {
	if (invMap != null) return;
	invMap = new TreeMap<Integer,String>();
	for (Map.Entry<String,Integer> entry:map.entrySet()) {
	    String key = entry.getKey();
	    Integer val = entry.getValue();
	    invMap.put(val, key);
	}
    }


    static TreeMap<String,Integer> map = new TreeMap<>();
    static {
	map.put("aliceblue", 0xf0f8ff);
	map.put("antiquewhite", 0xfaebd7);
	map.put("aqua", 0x00ffff);
	map.put("aquamarine", 0x7fffd4);
	map.put("azure", 0xf0ffff);
	map.put("beige", 0xf5f5dc);
	map.put("bisque", 0xffe4c4);
	map.put("black", 0x000000);
	map.put("blanchedalmond", 0xffebcd);
	map.put("blue", 0x0000ff);
	map.put("blueviolet", 0x8a2be2);
	map.put("brown", 0xa52a2a);
	map.put("burlywood", 0xdeb887);
	map.put("cadetblue", 0x5f9ea0);
	map.put("chartreuse", 0x7fff00);
	map.put("chocolate", 0xd2691e);
	map.put("coral", 0xff7f50);
	map.put("cornflowerblue", 0x6495ed);
	map.put("cornsilk", 0xfff8dc);
	map.put("crimson", 0xdc143c);
	map.put("cyan", 0x00ffff);
	map.put("darkblue", 0x00008b);
	map.put("darkcyan", 0x008b8b);
	map.put("darkgoldenrod", 0xb8860b);
	map.put("darkgray", 0xa9a9a9);
	map.put("darkgreen", 0x006400);
	map.put("darkgrey", 0xa9a9a9);
	map.put("darkkhaki", 0xbdb76b);
	map.put("darkmagenta", 0x8b008b);
	map.put("darkolivegreen", 0x556b2f);
	map.put("darkorange", 0xff8c00);
	map.put("darkorchid", 0x9932cc);
	map.put("darkred", 0x8b0000);
	map.put("darksalmon", 0xe9967a);
	map.put("darkseagreen", 0x8fbc8f);
	map.put("darkslateblue", 0x483d8b);
	map.put("darkslategray", 0x2f4f4f);
	map.put("darkslategrey", 0x2f4f4f);
	map.put("darkturquoise", 0x00ced1);
	map.put("darkviolet", 0x9400d3);
	map.put("deeppink", 0xff1493);
	map.put("deepskyblue", 0x00bfff);
	map.put("dimgray", 0x696969);
	map.put("dimgrey", 0x696969);
	map.put("dodgerblue", 0x1e90ff);
	map.put("firebrick", 0xb22222);
	map.put("floralwhite", 0xfffaf0);
	map.put("forestgreen", 0x228b22);
	map.put("fuchsia", 0xff00ff);
	map.put("gainsboro", 0xdcdcdc);
	map.put("ghostwhite", 0xf8f8ff);
	map.put("gold", 0xffd700);
	map.put("goldenrod", 0xdaa520);
	map.put("gray", 0x808080);
	map.put("green", 0x008000);
	map.put("greenyellow", 0xadff2f);
	map.put("grey", 0x808080);
	map.put("honeydew", 0xf0fff0);
	map.put("hotpink", 0xff69b4);
	map.put("indianred", 0xcd5c5c);
	map.put("indigo", 0x4b0082);
	map.put("ivory", 0xfffff0);
	map.put("khaki", 0xf0e68c);
	map.put("lavender", 0xe6e6fa);
	map.put("lavenderblush", 0xfff0f5);
	map.put("lawngreen", 0x7cfc00);
	map.put("lemonchiffon", 0xfffacd);
	map.put("lightblue", 0xadd8e6);
	map.put("lightcoral", 0xf08080);
	map.put("lightcyan", 0xe0ffff);
	map.put("lightgoldenrodyellow", 0xfafad2);
	map.put("lightgray", 0xd3d3d3);
	map.put("lightgreen", 0x90ee90);
	map.put("lightgrey", 0xd3d3d3);
	map.put("lightpink", 0xffb6c1);
	map.put("lightsalmon", 0xffa07a);
	map.put("lightseagreen", 0x20b2aa);
	map.put("lightskyblue", 0x87cefa);
	map.put("lightslategray", 0x778899);
	map.put("lightslategrey", 0x778899);
	map.put("lightsteelblue", 0xb0c4de);
	map.put("lightyellow", 0xffffe0);
	map.put("lime", 0x00ff00);
	map.put("limegreen", 0x32cd32);
	map.put("linen", 0xfaf0e6);
	map.put("magenta", 0xff00ff);
	map.put("maroon", 0x800000);
	map.put("mediumaquamarine", 0x66cdaa);
	map.put("mediumblue", 0x0000cd);
	map.put("mediumorchid", 0xba55d3);
	map.put("mediumpurple", 0x9370db);
	map.put("mediumseagreen", 0x3cb371);
	map.put("mediumslateblue", 0x7b68ee);
	map.put("mediumspringgreen", 0x00fa9a);
	map.put("mediumturquoise", 0x48d1cc);
	map.put("mediumvioletred", 0xc71585);
	map.put("midnightblue", 0x191970);
	map.put("mintcream", 0xf5fffa);
	map.put("mistyrose", 0xffe4e1);
	map.put("moccasin", 0xffe4b5);
	map.put("navajowhite", 0xffdead);
	map.put("navy", 0x000080);
	map.put("oldlace", 0xfdf5e6);
	map.put("olive", 0x808000);
	map.put("olivedrab", 0x6b8e23);
	map.put("orange", 0xffa500);
	map.put("orangered", 0xff4500);
	map.put("orchid", 0xda70d6);
	map.put("palegoldenrod", 0xeee8aa);
	map.put("palegreen", 0x98fb98);
	map.put("paleturquoise", 0xafeeee);
	map.put("palevioletred", 0xdb7093);
	map.put("papayawhip", 0xffefd5);
	map.put("peachpuff", 0xffdab9);
	map.put("peru", 0xcd853f);
	map.put("pink", 0xffc0cb);
	map.put("plum", 0xdda0dd);
	map.put("powderblue", 0xb0e0e6);
	map.put("purple", 0x800080);
	map.put("red", 0xff0000);
	map.put("rosybrown", 0xbc8f8f);
	map.put("royalblue", 0x4169e1);
	map.put("saddlebrown", 0x8b4513);
	map.put("salmon", 0xfa8072);
	map.put("sandybrown", 0xf4a460);
	map.put("seagreen", 0x2e8b57);
	map.put("seashell", 0xfff5ee);
	map.put("sienna", 0xa0522d);
	map.put("silver", 0xc0c0c0);
	map.put("skyblue", 0x87ceeb);
	map.put("slateblue", 0x6a5acd);
	map.put("slategray", 0x708090);
	map.put("slategrey", 0x708090);
	map.put("snow", 0xfffafa);
	map.put("springgreen", 0x00ff7f);
	map.put("steelblue", 0x4682b4);
	map.put("tan", 0xd2b48c);
	map.put("teal", 0x008080);
	map.put("thistle", 0xd8bfd8);
	map.put("tomato", 0xff6347);
	map.put("turquoise", 0x40e0d0);
	map.put("violet", 0xee82ee);
	map.put("wheat", 0xf5deb3);
	map.put("white", 0xffffff);
	map.put("whitesmoke", 0xf5f5f5);
	map.put("yellow", 0xffff00);
	map.put("yellowgreen", 0x9acd32);
    }


    /**
     * Get a set containing the names of CSS colors.
     * The name "transparent" is not included because there is no
     * corresponsding color that can be shown.
     * @return a set containing those CSS colors that have names
     */
    public static Set<String> namedCSSColors() {
	return map.keySet();
    }

    /**
     * Get a Collection containing the names of CSS colors,
     * excluding "transparent".
     * @param nameOrder true if the set is ordered by name, false
     *         if it is ordered by value
     * @return a set containing those CSS colors that have names
     */
    public static Collection<String> namedCSSColors(boolean nameOrder) {
	invMapInit();
	return nameOrder? map.keySet(): invMap.values();
    }

    /**
     * Get the index of a color in a table of named CSS colors,
     * sorted by either name or RGB value.
     * A RGB value can be represented as 0xRRGGBB where R, G, and B
     * are hexidecimal digits representing the red, green, and blue
     * components of a color respectively, with the value sorted in
     * numerical order.
     * <P>
     * The CSS color "transparent" is not included.
     * @param c the color
     * @param nameOrder true if the colors are sorted by name; false
     *        if the colors are sorted by RGB values
     * @return the index into the table; -1 if the color is null or
     *         is not a named color
     */
    public static int namedCSSColorIndex(Color c, boolean nameOrder) {
	if (c == null) return -1;
	if (nameOrder) {
	    int sz = map.size();
	    String name = getCSSName(c);
	    if (name == null) {
		return -1;
	    } else {
		return sz - map.tailMap(name).size();
	    }
	} else {
	    invMapInit();
	    int sz = invMap.size();
	    int code = c.getRGB() & 0xffffff;
	    if (invMap.containsKey(code)) {
		return sz - invMap.tailMap(code).size();
	    } else {
		return -1;
	    }
	}
    }


    /**
     * Get a set containing the names of CSS colors in a specified range
     * The color "transparent" is not included.
     * @param lower the starting name for the range (inclusive)
     * @param upper the ending name for the range (exclusive); null for
     *        all keys starting from lower
     * @return a set containing those CSS colors that have names in the
     *         specified range
     */
    public static Set<String> namedCSSColors(String lower, String upper) {
	if (upper == null) {
	    return map.tailMap(lower, true).keySet();
	} else {
	    return map.subMap(lower, true, upper, false).keySet();
	}
    }

    /**
     * Return the  CSS name of a color
     * @param c a color
     * @return the CSS name for the color; null if the color is
     *         not one with a name recognized by CSS.
     */
    public static String getCSSName(Color c) {
	if (c == null) return null;
	if (c.getAlpha() == 0 && c.getRed() == 0 && c.getGreen() == 0
	    && c.getBlue() == 0) return "transparent";
	invMapInit();
	int code = c.getRGB() & 0xffffff;
	return invMap.get(code);
    }

    /**
     * Return the CSS specification for a color.
     * If the color matches a named color, that name is returned.
     * Otherwise a rgb or rgba specification is returned.
     * <P>
     * Some CSS named colors are mapped to the same color, and the
     * value returned simply picks one. Also, the CSS rgb and rgba
     * specifications use floating point numbers for the alpha value
     * whereas Java's Color class uses an integer in the range [0, 255].
     * If a specification is used to create a color, this method may
     * return a slightly different specification due to rounding.
     * @param c the color
     * @return the CSS specification for the color.
     */
    public static String getCSS(Color c) {
	int alpha = c.getAlpha();
	int red = c.getRed();
	int green = c.getGreen();
	int blue = c.getBlue();
	if (alpha == 255) {
	    String name = getCSSName(c);
	    if (name != null) {
		return name;
	    } else {
		return String.format((Locale)null, "rgb(%d,%d,%d)",
				     red, green, blue);
	    }
	} else if (alpha == 0 && red == 0 && green == 0 && blue == 0) {
	    return "transparent";
	} else {
	    return String.format((Locale)null, "rgba(%d,%d,%d,%g)",
				 red, green, blue,
				 alpha/255.0);
	}
    }

    static enum Mode {
	RGB, RGBA, HSL, HSLA, NAMED
    }

    static class Parser {
	boolean parsed;
	int rgba;
	boolean hasalpha;

	void createRGBFromHSL(float[] fresult, int alpha) {
	    // convert to HSB
	    float b = (2*fresult[2]
		       + fresult[1]*(1 - Math.abs(2*fresult[2] - 1)))/ 2;
	    if (Math.abs(b) < 1.e-10F) {
		fresult[1] = 0.0F;
	    } else {
		fresult[1] = 2*(b - fresult[2]) / b;
	    }
	    fresult[2] = b;
	    rgba = Color.HSBtoRGB(fresult[0], fresult[1], fresult[2]);
	    // the Java conversion in Java 1.8 adds 1s to the bits
	    // corresponding to the alpha channel, so we clear those.
	    rgba &= 0xFFFFFF;
	    if (hasalpha) {
		rgba |= (alpha << 24);
	    }
	}

	void createRGB(Mode mode, String[] array)
	    throws NumberFormatException
	{
	    int[] result = new int[array.length];
	    switch (mode) {
	    case HSL:
	    case HSLA:
		// https://codeitdown.com/hsl-hsb-hsv-color/ for equation
		// to compute HSL to HSB so we can use a static Color
		// method to convert to RGB.
		float[] fresult = new float[3];
		int alpha = 255;
		for (int i = 0; i < array.length; i++) {
		    if (i == 3) {
			float opacity = Float.parseFloat(array[i]);
			if (opacity > 1.0) opacity = 1.0F;
			if (opacity < 0.0) opacity = 0.0F;
			alpha = Math.round(255*opacity);
		    } else {
			if (array[i].endsWith("%")) {
			    array[i] = array[i].substring(0,
							  array[i].length()-1);
			    fresult[i] = Float.parseFloat(array[i])/100.0F;
			} else {
			    fresult[i] = Float.parseFloat(array[i]);
			}
		    }
		}
		fresult[0] /= 360.0F;
		createRGBFromHSL(fresult, alpha);
		break;
	    case RGB:
	    case RGBA:
		for (int i = 0; i < array.length; i++) {
		    if (i == 3) {
			    float opacity = Float.parseFloat(array[i]);
			    if (opacity > 1.0F) opacity = 1.0F;
			    result[i] = (int) Math.round(255*opacity);
		    } else {
			if (array[i].endsWith("%")) {
			    array[i] =
				array[i].substring(0, array[i].length() - 1);
			    result[i] = (int)
				Math.round(2.55F * Float.parseFloat(array[i]));
			} else {
			    result[i] = Integer.parseInt(array[i]);
			}
		    }
		}
		if (hasalpha) {
		    rgba = result[3];
		}
		for (int i = 0; i < 3; i++) {
		    rgba = rgba << 8;
		    rgba |= result[i];
		}
		break;
	    }
	}

	Parser(float h, float s, float l) {
	    float array[] = {h, s, l};
	    hasalpha = false;
	    createRGBFromHSL(array, 255);
	}

	Parser(float h, float s, float l, float a) {
	    float array[] = {h, s, l, a};
	    hasalpha = true;
	    int alpha = Math.round(a*255);
	    createRGBFromHSL(array, alpha);
	}

	Parser(String spec) throws
	    IllegalArgumentException
	{
	    String original = spec;
	    try {
		int[] values;
		spec = spec.replaceAll("\\p{Space}","").toLowerCase();
		if (spec.startsWith("#")) {
		    rgba = Integer.decode(spec);
		    hasalpha = false;
		} else if (spec.startsWith("rgba")) {
		    spec = spec.replace("rgba(", "").replace(")", "");
		    String[] array = spec.split(",");
		    hasalpha = true;
		    if (array.length != 4) {
			String msg = errorMsg("badSpecCSS", original);
			throw new IllegalArgumentException(msg);
		    }
		    createRGB(Mode.RGBA, array);
		} else if (spec.startsWith("rgb")) {
		    spec = spec.replace("rgb(", "").replace(")", "");
		    String[] array = spec.split(",");
		    if (array.length != 3) {
			String msg = errorMsg("badSpecCSS", original);
			throw new IllegalArgumentException(msg);
		    }
		    hasalpha = false;
		    createRGB(Mode.RGB, array);
		} else if (spec.startsWith("hsla")) {
		    spec = spec.replace("hsla(", "").replace(")", "");
		    String[] array = spec.split(",");
		    if (array.length != 4) {
			String msg = errorMsg("badSpecCSS", original);
			throw new IllegalArgumentException(msg);
		    }
		    hasalpha = true;
		    createRGB(Mode.HSLA, array);
		} else if (spec.startsWith("hsl")) {
		    spec = spec.replace("hsl(", "").replace(")", "");
		    String[] array = spec.split(",");
		    if (array.length != 3) {
			String msg = errorMsg("badSpecCSS", original);
			throw new IllegalArgumentException(msg);
		    }
		    createRGB(Mode.HSL, array);
		    hasalpha = false;
		} else {
		    // named colors
		    if (spec.equals("transparent")) {
			hasalpha = true;
			rgba = 0;
		    } else {
			hasalpha = false;
			Integer rgb = map.get(spec);
			if (rgb == null) {
			    throw new IllegalArgumentException
				(errorMsg("badSpecCSS", spec));
			}
			rgba = rgb;
		    }
		}
	    } catch (NumberFormatException e) {
		String msg = errorMsg("badSpecCSS", original);
		throw new IllegalArgumentException(msg, e);
	    }
	}
    }

    /**
     * Get a color given a CSS specification.
     * The specification for the color is a string containing one of
     * the following:
     * <UL>
     *   <LI>6 hexadecimal digits following an '#' and providing an opaque
     *       color (i.e., the alpha value is 255 in a RGBA color space).
     *   <LI><CODE>rgb(RED,BLUE,GREEN)</CODE>, where <CODE>RED</CODE>,
     *       <CODE>BLUE</CODE>, and <CODE>GREEN</CODE> are either numbers
     *       in the range [0, 255] or a number (typically floating point)
     *       followed by a '%'.
     *   <LI><CODE>rgba(RED,BLUE,GREEN,ALPHA)</CODE>, where <CODE>RED</CODE>,
     *       <CODE>BLUE</CODE>, and <CODE>GREEN</CODE> are either numbers
     *       in the range [0, 255] or a number (typically floating point)
     *       followed by a '%', and <CODE>ALPHA</CODE> varies from 0.0 (fully
     *       transparent to 1.0 (opaque).
     *   <LI><CODE>hsl(HUE,SATURATION,LIGHTNESS)</CODE>, where
     *       <CODE>HUE</CODE> is in the range [0,360],
     *       <CODE>SATURATION</CODE> is a number in the range [0,100]
     *       followed immediately by a '%', and where <CODE>LIGHTNESS</CODE>
     *       is a number in the range [0,100] followed immediately by a '%'.
     *   <LI><CODE>hsla(HUE,SATURATION,LIGHTNESS, ALPHA)</CODE>, where
     *       <CODE>HUE</CODE> is in the range [0,360],
     *       <CODE>SATURATION</CODE> is a number in the range [0,100]
     *       followed immediately by a '%', where <CODE>LIGHTNESS</CODE>
     *       is a number in the range [0,100] followed immediately by a '%',
     *       and where <CODE>ALPHA</CODE> is a floating point number in the
     *       range [0, 1].
     *   <LI>a keyword denoting an X11 color with the addition of "gray"
     *       and its synonym "grey" from SVG 1.0. X11 colors are described
     *       in the document
     *       <A href="https://en.wikipedia.org/wiki/X11_color_names">X11
     *       color names</A>.  The variant of these names that this class
     *       uses are the ones given in the
     *       <A href="https://www.w3.org/TR/css-color-3/">W3C recommendation</A>
     *       cited above. These are all lower case with no whitespace.
     * </UL>
     * As a convenience, whitespace will be trimmed from the specification
     * before processing it and the specification will be converted to
     * lower case.
     * @param spec the CSS specification
     * @return the color corresponding to the specification
     * @exception IllegalArgumentException the CSS specification was
     *            ill-formed.
     */
    public static Color getColorByCSS(String spec)
	throws IllegalArgumentException
    {
	Parser parser = new Parser(spec);
	return new Color(parser.rgba, parser.hasalpha);
    }


    /**
     * Get color components given a CSS specification.
     * The specification for the color is a string containing one of
     * the following:
     * <UL>
     *   <LI>6 hexadecimal digits following an '#' and providing an opaque
     *       color (i.e., the alpha value is 255 in a RGBA color space).
     *   <LI><CODE>rgb(RED,BLUE,GREEN)</CODE>, where <CODE>RED</CODE>,
     *       <CODE>BLUE</CODE>, and <CODE>GREEN</CODE> are either numbers
     *       in the range [0, 255] or a number (typically floating point)
     *       followed by a '%'.
     *   <LI><CODE>rgba(RED,BLUE,GREEN,ALPHA)</CODE>, where <CODE>RED</CODE>,
     *       <CODE>BLUE</CODE>, and <CODE>GREEN</CODE> are either numbers
     *       in the range [0, 255] or a number (typically floating point)
     *       followed by a '%', and <CODE>ALPHA</CODE> varies from 0.0 (fully
     *       transparent to 1.0 (opaque).
     *   <LI><CODE>hsl(HUE,SATURATION,LIGHTNESS)</CODE>, where
     *       <CODE>HUE</CODE> is in the range [0,360],
     *       <CODE>SATURATION</CODE> is a number in the range [0,100]
     *       followed immediately by a '%', and where <CODE>LIGHTNESS</CODE>
     *       is a number in the range [0,100] followed immediately by a '%'.
     *   <LI><CODE>hsl(HUE,SATURATION,LIGHTNESS, ALPHA)</CODE>, where
     *       <CODE>HUE</CODE> is in the range [0,360],
     *       <CODE>SATURATION</CODE> is a number in the range [0,100]
     *       followed immediately by a '%', where <CODE>LIGHTNESS</CODE>
     *       is a number in the range [0,100] followed immediately by a '%',
     *       and where <CODE>ALPHA</CODE> is a floating point number in the
     *       range [0, 1].
     *   <LI>a keyword denoting an X11 color with the addition of "gray"
     *       and its synonym "grey" from SVG 1.0. X11 colors are described
     *       in the document
     *       <A href="https://en.wikipedia.org/wiki/X11_color_names">X11
     *       color names</A>.  The variant of these names that this class
     *       uses are the ones given in the
     *       <A href="https://www.w3.org/TR/css-color-3/">W3C recommendation</A>
     *       cited above. These are all lower case with no whitespace.
     * </UL>
     * As a convenience, whitespace will be trimmed from the specification
     * before processing it and the specification will be converted to
     * lower case.
     * @param spec the CSS specification
     * @return an array whose first three elements are the red, green, and
     *         blue components of a color and whose optional fourth
     *         component in the alpha value, each in the range [0,255]
     * @exception IllegalArgumentException the CSS specification was
     *            ill-formed.
     */
    public static int[] getComponentsByCSS(String spec)
	throws IllegalArgumentException
    {
	Parser parser = new Parser(spec);
	int rgb = parser.rgba & 0xffffff;
	int result[] = new int[parser.hasalpha?4:3];
	result[0] = rgb >> 16;
	result[1] = (rgb & 0xffff) >> 8;
	result[2] = rgb & 0xff;
	if (parser.hasalpha) {
	    result[3] = parser.rgba >>> 24;
	}
	return result;
    }

    /**
     * Get a color given HSL values.
     * In CSS, h varies from 0 to 360, but Java's HSB code assumes that
     * h varies from 0 to 1.  This method uses the Java convention.
     * @param h the hue specified as a number in the range [0.0, 1.0)
     * @param s the saturation specified as a number in the range [0, 1]
     * @param l the lightness specified as a number in the range [0, 1]
     * @return the color corresponding the parameters h, s, and l
     */
    public static Color getColorByHSL(double h, double s, double l) {
	Parser parser = new Parser((float)h, (float)s, (float)l);
	return new Color(parser.rgba, parser.hasalpha);
    }

    /**
     * Get a color given HSLA values.
     * In CSS, h varies from 0 to 360, but Java's HSB code assumes that
     * h varies from 0 to 1.  This method uses the Java convention.
     * @param h the hue specified as a number in the range [0.0, 1.0)
     * @param s the saturation specified as a number in the range [0.0, 1.0]
     * @param l the lightness specified as a number in the range [0.0, 1.0]
     * @param a the value of alpha as a number in the range [0.0, 1.0]
     * @return the color corresponding the parameters h, s, l, and a
     */
    public static Color getColorByHSL(double h, double s, double l, double a) {
	Parser parser = new Parser((float)h, (float)s, (float)l, (float)a);
	return new Color(parser.rgba, parser.hasalpha);
    }

    /**
     * Get an opaque sRGB color with the specified red, green, and blue values
     * in the range [0.0, 1.0].
     * <P>
     * This method simply calls a {@link java.awt.Color} constructor
     * and is provided for scripting languages such as ESP that do not
     * support single-precision real numbers.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the color for the given values
     */
    public static Color getColorBysRGB(double r, double g, double b) {
	return new Color((float)r, (float)g, (float)b);
    }

    /**
     * Get an sRGB color with the specified red, green, blue  and alpha values
     * in the range [0.0, 1.0].
     * <P>
     * This method simply calls a {@link java.awt.Color} constructor
     * and is provided for scripting languages such as ESP that do not
     * support single-precision real numbers.
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @param a the alpha component
     * @return the color for the given values
     */
    public static Color getColorBysRGB(double r, double g, double b, double a) {
	return new Color((float)r, (float)g, (float)b, (float) a);
    }


    /**
     * Get a color given the wavelength of light.
     * @param cm the color model
     * @param wavelength the wavelength in meters
     * @return the color for the given values
     */
    public static Color getColorByWavelength(ColorModel cm,
					     double wavelength) {
	return getColorByWavelength(cm, wavelength, 1.0, 1.0);
    }

    /**
     * Get a color given the wavelength and gamma-correction.
     * When the intensity is 1.0, the color is as bright as possible given
     * that the maximum RGB value for either of these three components has
     * an upper bound (255 for integer values and 1.0F for floating-point
     * values).  An intensity of 0.0 will result in a color equal to
     * {@link Color#BLACK}.
     * @param cm the color model
     * @param wavelength the wavelength in meters
     * @param intensity an intensity scaling factor in the range [0.0, 1.0]
     * @return the color for the given values
     */
    public static Color getColorByWavelength(ColorModel cm,
					     double wavelength,
					     double intensity)
    {
	return getColorByWavelength(cm, wavelength, intensity, 1.0);
    }


    /**
     * Get a color given a wavelength, gamma correction, intensity,
     * and alpha value.
     * The visible portion of the spectrum is between 380 nm and 780 nm.
     * Anything outside that range will appear black.
     * When the intensity is 1.0, the color is as bright as possible given
     * that the maximum RGB value for either of these three components has
     * an upper bound (255 for integer values and 1.0F for floating-point
     * values).  An intensity of 0.0 will result in a color equal to
     * {@link Color#BLACK}.
     * @param cm the color model
     * @param wavelength the wavelength in meters
     * @param intensity an intensity scaling factor in the range [0.0, 1.0]
     * @param alpha the alpha component of the color (range: [0.0, 1.0])
     * @return the color for the given values
     */
    public static Color getColorByWavelength(ColorModel cm,
					     double wavelength,
					     double intensity,
					     double alpha)
    {

	float cie[] = new float[3];
	if (wavelength >= 380e-9 && wavelength <= 780e-9) {
	    cie[0] = (float)xBar.valueAt(wavelength);
	    cie[1] = (float)yBar.valueAt(wavelength);
	    cie[2] = (float)zBar.valueAt(wavelength);
	}
	/*
	if (intensity != 1.0) {
	    cie[0] = (float)(cie[0]*intensity);
	    cie[1] = (float)(cie[1]*intensity);
	    cie[2] = (float)(cie[2]*intensity);
	}
	*/
	ColorSpace cs = cm.getColorSpace();
	float[] rgb = cs.fromCIEXYZ(cie);
	// fix roundoff errors and find max
	float max = 0.0F;
	for (int i = 0; i < rgb.length; i++) {
	    if (rgb[i] < 0.0) rgb[i] = 0.0F;
	    if (rgb[i] > 1.0) rgb[i] = 1.0F;
	    if (rgb[i] > max) max = rgb[i];
	}
	// normalize & scale
	if (max > 0.0F) {
	    if (intensity == 1.0) {
		for (int i = 0; i < 3; i++) {
		    rgb[i] /= max;
		}
	    } else {
		for (int i = 0; i < 3; i++) {
		    rgb[i] = (float)((rgb[i]*intensity)/max);
		}
	    }
	}
	if (alpha == 1.0) {
	    return new Color(rgb[0], rgb[1], rgb[2]);
	} else {
	    return new Color(rgb[0], rgb[1], rgb[2], (float) alpha);
	}
    }

    static RealValuedFunction xBar;
    static RealValuedFunction yBar;
    static RealValuedFunction zBar;

    static {
	final double[] xBarTable = {
	    0.00140000, 0.00220000, 0.00420000,
	    0.00760000, 0.0143000, 0.0232000,
	    0.0435000, 0.0776000, 0.134400,
	    0.214800, 0.283900, 0.328500,
	    0.348300, 0.348100, 0.336200,
	    0.318700, 0.290800, 0.251100,
	    0.195400, 0.142100, 0.0956000,
	    0.0580000, 0.0320000, 0.0147000,
	    0.00490000, 0.00240000, 0.00930000,
	    0.0291000, 0.0633000, 0.109600,
	    0.165500, 0.225700, 0.290400,
	    0.359700, 0.433400, 0.512100,
	    0.594500, 0.678400, 0.762100,
	    0.842500, 0.916300, 0.978600,
	    1.02630, 1.05670, 1.06220,
	    1.04560, 1.00260, 0.938400,
	    0.854400, 0.751400, 0.642400,
	    0.541900, 0.447900, 0.360800,
	    0.283500, 0.218700, 0.164900,
	    0.121200, 0.0874000, 0.0636000,
	    0.0468000, 0.0329000, 0.0227000,
	    0.0158000, 0.0114000, 0.00810000,
	    0.00580000, 0.00410000, 0.00290000,
	    0.00200000, 0.00140000, 0.00100000,
	    0.000700000, 0.000500000, 0.000300000,
	    0.000200000, 0.000200000, 0.000100000,
	    0.000100000, 0.000100000, 0.00000
	};

	final double[] yBarTable = {
	    0.00000, 0.000100000, 0.000100000,
	    0.000200000, 0.000400000, 0.000600000,
	    0.00120000, 0.00220000, 0.00400000,
	    0.00730000, 0.0116000, 0.0168000,
	    0.0230000, 0.0298000, 0.0380000,
	    0.0480000, 0.0600000, 0.0739000,
	    0.0910000, 0.112600, 0.139000,
	    0.169300, 0.208000, 0.258600,
	    0.323000, 0.407300, 0.503000,
	    0.608200, 0.710000, 0.793200,
	    0.862000, 0.914900, 0.954000,
	    0.980300, 0.995000, 1.00000,
	    0.995000, 0.978600, 0.952000,
	    0.915400, 0.870000, 0.816300,
	    0.757000, 0.694900, 0.631000,
	    0.566800, 0.503000, 0.441200,
	    0.381000, 0.321000, 0.265000,
	    0.217000, 0.175000, 0.138200,
	    0.107000, 0.0816000, 0.0610000,
	    0.0446000, 0.0320000, 0.0232000,
	    0.0170000, 0.0119000, 0.00820000,
	    0.00570000, 0.00410000, 0.00290000,
	    0.00210000, 0.00150000, 0.00100000,
	    0.000700000, 0.000500000, 0.000400000,
	    0.000200000, 0.000200000, 0.000100000,
	    0.000100000, 0.000100000, 0.00000,
	    0.00000, 0.00000, 0.00000
	};

	final double[] zBarTable = {
	    0.00650000, 0.0105000, 0.0201000,
	    0.0362000, 0.0679000, 0.110200,
	    0.207400, 0.371300, 0.645600,
	    1.03910, 1.38560, 1.62300,
	    1.74710, 1.78260, 1.77210,
	    1.74410, 1.66920, 1.52810,
	    1.28760, 1.04190, 0.813000,
	    0.616200, 0.465200, 0.353300,
	    0.272000, 0.212300, 0.158200,
	    0.111700, 0.0782000, 0.0573000,
	    0.0422000, 0.0298000, 0.0203000,
	    0.0134000, 0.00870000, 0.00570000,
	    0.00390000, 0.00270000, 0.00210000,
	    0.00180000, 0.00170000, 0.00140000,
	    0.00110000, 0.00100000, 0.000800000,
	    0.000600000, 0.000300000, 0.000200000,
	    0.000200000, 0.000100000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000,
	    0.00000, 0.00000, 0.00000
	};

	xBar = new RealValuedFunction() {
		public double getMaxDomain() {return 780.0e-9;}
		public double getMinDomain() {return 380.0e-9;}
		public double valueAt(double x) {
		    double xx = (x - 380.0e-9) /5.0e-9;
		    int i = (int)Math.rint(Math.floor(xx));
		    double z = xx - i;
		    if (i < 0 || i >= (xBarTable.length-1)) return 0.0;
		    else {
			return  xBarTable[i]*(1.0-z) + xBarTable[i+1]*z;
		    }
		}
	    };

	yBar = new RealValuedFunction() {
		public double getMaxDomain() {return 780.0e-9;}
		public double getMinDomain() {return 380.0e-9;}
		public double valueAt(double x) {
		    double xx = (x - 380.0e-9) /5.0e-9;
		    int i = (int)Math.rint(Math.floor(xx));
		    double z = xx - i;
		    if (i < 0 || i >= (yBarTable.length-1)) return 0.0;
		    else {
			return  yBarTable[i]*(1.0-z) + yBarTable[i+1]*z;
		    }
		}
	    };

	zBar = new RealValuedFunction() {
		public double getMaxDomain() {return 780.0e-9;}
		public double getMinDomain() {return 380.0e-9;}
		public double valueAt(double x) {
		    double xx = (x - 380.0e-9) /5.0e-9;
		    int i = (int)Math.rint(Math.floor(xx));
		    double z = xx - i;
		    if (i < 0 || i >= (zBarTable.length-1)) return 0.0;
		    else {
			return  zBarTable[i]*(1.0-z) + zBarTable[i+1]*z;
		    }
		}
	    };

	// xBar = new CubicSpline1(xBarTable, 380.0e-9, 5.0e-9);
	// yBar = new CubicSpline1(yBarTable, 380.0e-9, 5.0e-9);
	// zBar = new CubicSpline1(zBarTable, 380.0e-9, 5.0e-9);
	// System.out.println(xBar.getDomainMin());
	// System.out.println(xBar.getDomainMax());
	
	/*
	double max = 0.0;
	for (int j = 0; j < 10 * (xBarTable.length-1); j++) {
	    double x = 380.0e-9 + j * 0.5e-9;
	    int i = j/10;
	    int k = (j%10);
	    double y1 = xBar.valueAt(x);
	    if (y1 < 0.0) System.out.println("y1 negative");
	    double y2 = (xBarTable[i]* (10-k) + xBarTable[i+1]*k)/10.0;
	    if (y2 > 0.0) {
		double v = Math.abs((y1-y2)/y2);
		if (v > max) max = v;
	    }
	}
	System.out.println ("max diff = " + max);
	*/
    }

    /**
     * Get a color given a spectrum, gamma correction, intensity,
     * and alpha value.
     * The visible portion of the spectrum is between 380 nm and 780 nm.
     * Anything outside that range will appear black.
     * When the intensity is 1.0, the color is as bright as possible given
     * that the maximum RGB value for either of these three components has
     * an upper bound (255 for integer values and 1.0F for floating-point
     * values).  An intensity of 0.0 will result in a color equal to
     * {@link Color#BLACK}.
     * @param cm the color model
     * @param spectrum the spectral radiance (measured per unit wavelength)
     *        of electromagnetic radiation as a function of wavelength
     * @param intensity an intensity scaling factor in the range [0, 1.0]
     * @param alpha the alpha component of the color (range: [0.0, 1.0])
     * @return the color for the given values
     */
    public static Color getColorBySpectrum(ColorModel cm,
					   final RealValuedFunctOps spectrum,
					   double intensity,
					   double alpha)
    {
	GLQuadrature glqx = new GLQuadrature(9) {
		protected double function(double wl) {
		    return xBar.valueAt(wl)*spectrum.valueAt(wl);
		}
	    };
	GLQuadrature glqy = new GLQuadrature(9) {
		protected double function(double wl) {
		    return yBar.valueAt(wl)*spectrum.valueAt(wl);
		}
	    };
	GLQuadrature glqz = new GLQuadrature(9) {
		protected double function(double wl) {
		    return zBar.valueAt(wl)*spectrum.valueAt(wl);
		}
	    };

	double x = glqx.integrate(380.0e-9, 780.0e-9, 9);
	double y = glqy.integrate(380.0e-9, 780.0e-9, 9);
	double z = glqz.integrate(380.0e-9, 780.0e-9, 9);

	double xpypz = x + y + z;
	if (xpypz != 0.0) {
	    x /= xpypz;
	    y /= xpypz;
	    z /= xpypz;
	}
	/*
	System.out.format("x = %g, y = %g, z = %g\n",
			  x, y, z);
	*/
	float cie[] = new float[3];
	cie[0] = (float)x;
	cie[1] = (float)y;
	cie[2] = (float)z;
	// ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ColorSpace cs = cm.getColorSpace();
	float[] rgb = cs.fromCIEXYZ(cie);
	// fix roundoff errors and find max
	float max = 0.0F;
	for (int i = 0; i < rgb.length; i++) {
	    if (rgb[i] < 0.0) rgb[i] = 0.0F;
	    if (rgb[i] > 1.0) rgb[i] = 1.0F;
	    if (rgb[i] > max) max = rgb[i];
	}
	// normalize & scale
	if (max > 0.0F) {
	    if (intensity == 1.0) {
		for (int i = 0; i < 3; i++) {
		    rgb[i] /= max;
		}
	    } else {
		for (int i = 0; i < 3; i++) {
		    rgb[i] = (float)((rgb[i]*intensity)/max);
		}
	    }
	}
	if (alpha == 1.0) {
	    return new Color(rgb[0], rgb[1], rgb[2]);
	} else {
	    return new Color(rgb[0], rgb[1], rgb[2], (float) alpha);
	}
    }
    
    /**
     * Get the blackbody spectrum for a specified temperature.
     * The spectrum is defined as the spectral radiance (measured
     * per unit wavelength) of electromagnetic radiation as a function of
     * wavelength.
     * @param T the temperature in Kelvin
     * @return the spectrum as a real-valued function.
     */
    public static RealValuedFunction blackbodySpectrum(final double T) {
	return new RealValuedFunction() {
	    public double valueAt(double wl) {
		return (3.74183e-16 * Math.pow(wl, -5.0)) /
		    (Math.exp(1.4388e-2 / (wl * T)) - 1.0);
	    }
	};
    }
}

//  LocalWords:  href RGB Colour ColorModel ColorSpace CIEXYZ ul li
//  LocalWords:  getColorModel getDeviceConfiguration aliceblue grey
//  LocalWords:  antiquewhite blanchedalmond blueviolet burlywood HSL
//  LocalWords:  cadetblue cornflowerblue cornsilk darkblue darkcyan
//  LocalWords:  darkgoldenrod darkgray darkgreen darkgrey darkkhaki
//  LocalWords:  darkmagenta darkolivegreen darkorange darkorchid HSB
//  LocalWords:  darkred darksalmon darkseagreen darkslateblue peru
//  LocalWords:  darkslategray darkslategrey darkturquoise darkviolet
//  LocalWords:  deeppink deepskyblue dimgray dimgrey dodgerblue rgba
//  LocalWords:  floralwhite forestgreen gainsboro ghostwhite hotpink
//  LocalWords:  greenyellow indianred lavenderblush lawngreen rgb nm
//  LocalWords:  lemonchiffon lightblue lightcoral lightcyan oldlace
//  LocalWords:  lightgoldenrodyellow lightgray lightgreen lightgrey
//  LocalWords:  lightpink lightsalmon lightseagreen lightskyblue hsl
//  LocalWords:  lightslategray lightslategrey lightsteelblue skyblue
//  LocalWords:  lightyellow limegreen mediumaquamarine mediumblue
//  LocalWords:  mediumorchid mediumpurple mediumseagreen mintcream
//  LocalWords:  mediumslateblue mediumspringgreen mediumturquoise
//  LocalWords:  mediumvioletred midnightblue mistyrose navajowhite
//  LocalWords:  olivedrab orangered palegoldenrod palegreen seagreen
//  LocalWords:  paleturquoise palevioletred papayawhip peachpuff CSS
//  LocalWords:  powderblue rosybrown royalblue saddlebrown slateblue
//  LocalWords:  sandybrown slategray slategrey springgreen steelblue
//  LocalWords:  whitesmoke yellowgreen hsla badspec SVG whitespace
//  LocalWords:  IllegalArgumentException roundoff xBar CubicSpline
//  LocalWords:  xBarTable yBar yBarTable zBar zBarTable getDomainMin
//  LocalWords:  getDomainMax valueAt blackbody exbundle badSpecCSS
