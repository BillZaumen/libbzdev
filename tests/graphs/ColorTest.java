import org.bzdev.graphs.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import org.bzdev.util.units.MKS;

public class ColorTest {
    public static void main(String argv[]) throws Exception {

	double temps[] = {1000.0, 2000.0, 3000.0, 4000.0, 5000.0};

	BufferedImage image = new BufferedImage(600,600,
						BufferedImage.TYPE_INT_RGB);

	ColorModel cm = image.getColorModel();

	Color c;
	float[] ca;

	for (double T: temps) {
	    c = Colors.getColorBySpectrum(cm,
					  Colors.blackbodySpectrum(T),
					  1.0, 1.0);
	    ca = c.getRGBColorComponents(null);

	    System.out.format("T = %gK: r = %g, g = %g, b = %g\n",
			      T, ca[0], ca[1], ca[2]);
	}

	c = Colors.getColorByWavelength(cm, MKS.nm(300.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("300 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(380.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("380 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(400.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("400 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(500.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("500 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(600.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("600 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(700.0));
	    ca = c.getRGBColorComponents(null);
	    System.out.format("700 nm: r = %g, g = %g, b = %g\n",
			      ca[0], ca[1], ca[2]);

	c = Colors.getColorByWavelength(cm, MKS.nm(780.0));
	ca = c.getRGBColorComponents(null);
	System.out.format("780 nm: r = %g, g = %g, b = %g\n",
			  ca[0], ca[1], ca[2]);

	System.out.println("named CSS colors:");
	int testInd = 0;
	int i = 0;
	for (String name: Colors.namedCSSColors()) {
	    if (name.equals("tomato")) testInd = i;
	    System.out.println(name);
	    i++;
	}
	i = 0;
	for (String name: Colors.namedCSSColors(false)) {
	    if (i == testInd) {
		if (!name.equals("tomato")) {
		    throw new Exception("namedCSSColors(boolean) failed");
		}
	    }
	    i++;
	}

	int testInd2 = 0;
	i = 0;
	for (String name: Colors.namedCSSColors(false)) {
	    if (name.equals("tomato")) testInd2 = i;
	    i++;
	}

	System.out.println("named CSS colors starting with 'a' :");
	for (String name: Colors.namedCSSColors("a", "b")) {
	    System.out.println(name);
	}

	System.out.println("... test of getColorByCSS with no alpha channel");

	String name = "tomato";
	String hex =  "#ff6347";
	String hsl = "hsl(9, 100%, 64%)";
	String rgb = "rgb(255, 99, 71)";

	c = new Color(255, 99, 71);
	if (!name.equals(Colors.getCSSName(c))) {
	    System.out.format("c = %s, code = %x\n", c, c.getRGB());
	    System.out.println("Colors.getCSSName(c) = "
			       + Colors.getCSSName(c));
	    throw new Exception("named color lookup failed");
	}

	if (Colors.namedCSSColorIndex(c, true) != testInd) {
	    System.out.println("testInd = " + testInd + ", tried "
			       + Colors.namedCSSColorIndex(c, true));
	    throw new Exception("named color index wrong");
	}

	Color c1 = Colors.getColorByCSS(name);
	Color c2 = Colors.getColorByCSS(hex);
	Color c3 = Colors.getColorByCSS(hsl);
	Color c4 = Colors.getColorByCSS(rgb);
	Color chsl  = Colors.getColorByHSL(9.0/360, 1.0, 0.64);

	if (Colors.namedCSSColorIndex(c1, true) != testInd) {
	    System.out.println("testInd = " + testInd + ", tried "
			       + Colors.namedCSSColorIndex(c1, true));
	    throw new Exception("named color index wrong");
	}
	if (Colors.namedCSSColorIndex(c1, false) != testInd2) {
	    System.out.format("c1.getRGB() = %x\n", c1.getRGB());
	    System.out.println("testInd2 = " + testInd2 + ", tried "
			       + Colors.namedCSSColorIndex(c1, false));
	    throw new Exception("named color index wrong");
	}

	if (Colors.getCSSName(new Color(255, 99, 72))  != null) {
	    throw new Exception("named color lookup found non-existent name");
	}

	int[] comp = Colors.getComponentsByCSS(name);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71) {
	    throw new Exception("comp failed");
	}
	comp = Colors.getComponentsByCSS(hex);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71) {
	    throw new Exception("comp failed");
	}
	comp = Colors.getComponentsByCSS(hsl);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71) {
	    throw new Exception("comp failed");
	}
	comp = Colors.getComponentsByCSS(rgb);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71) {
	    throw new Exception("comp failed");
	}

	if (!c1.equals(c)) {
	    throw new Exception("c1 != c");
	}
	if (!c2.equals(c)) {
	    throw new Exception("c2 != c");
	}
	if (!c3.equals(c)) {
	    throw new Exception("c3 != c");
	}
	if (!c4.equals(c)) {
	    throw new Exception("c4 != c");
	}

	if (!chsl.equals(c)) {
	    throw new Exception("chsl != c");
	}

	System.out.println("... alpha test");
	c = new Color(255, 99, 71, 128);
	String hsla = "hsla(9, 100%, 64%, 0.5)";
	String rgba = "rgba(255, 99, 71, 0.5)";
	c3 = Colors.getColorByCSS(hsla);
	c4 = Colors.getColorByCSS(rgba);
	chsl  = Colors.getColorByHSL(9.0/360, 1.0, 0.64, 0.5);
	if (!c3.equals(c)) {
	    throw new Exception("c3 != c");
	}
	if (!c4.equals(c)) {
	    throw new Exception("c4 != c");
	}
	if (!chsl.equals(c)) {
	    throw new Exception("chsl != c");
	}

	comp = Colors.getComponentsByCSS(hsla);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71
	    || comp[3] != 128) {
	    throw new Exception("comp failed");
	}
	comp = Colors.getComponentsByCSS(rgba);
	if (comp[0] != 255 || comp[1] != 99 || comp[2] != 71
	    || comp[3] != 128) {
	    throw new Exception("comp failed");
	}


	String spec2v = "rgb(64, 128, 191)";
	String spec2p = "rgb(25%, 50%, 75%)";

	c1 = Colors.getColorByCSS(spec2v);

	c2 = Colors.getColorByCSS(spec2p);
	if (!c1.equals(c2)) {
	    throw new Exception("c1 != c2");
	}

	System.out.println("test CSS white");
	name = "white";
	c = new Color(255, 255, 255);
	if (!name.equals(Colors.getCSSName(c))) {
	    System.out.format("c = %s, code = %x\n", c, c.getRGB());
	    System.out.println("Colors.getCSSName(c) = "
			       + Colors.getCSSName(c));
	    throw new Exception("named color lookup failed");
	}

	c1 = Colors.getColorByCSS(name);
	if (!c1.equals(c)) {
	    System.out.format("%s not %s\n", c1, name);
	    throw new Exception("getColorByCSS failed");
	}
	System.out.println("checking 'transparent'");
	name = "transparent";
	c1 = Colors.getColorByCSS(name);
	if (c1.getAlpha() != 0 && c1.getRed() != 0 && c1.getGreen() != 0
	    && c1.getBlue() != 0) {
	    throw new Exception("getColorByCSS failed");
	}
	if (!Colors.getCSSName(c1).equals("transparent")) {
	    throw new Exception("getCSSName failed");
	}


	System.out.println("... done");


	System.exit(0);
    }
}
