import org.bzdev.obnaming.misc.*;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;

import org.bzdev.graphs.Graph;
import org.bzdev.graphs.Graph.FontParms;

public class InitialTest {
    public static void main(String argv[]) {
	ColorParm cp = new ColorParm();

	if (cp.createColor() != null) {
	    System.out.println("createColor should have returned null");
	    System.exit(1);
	}


	cp.green = 255;

	System.out.format("red = %d, green = %d, blue = %d, alpha = %d\n",
			  cp.getRed(), cp.getGreen(), cp.getBlue(),
			  cp.getAlpha());

	Color color = cp.createColor();

	System.out.format("red = %d, green = %d, blue = %d, alpha = %d\n",
			  color.getRed(), color.getGreen(), color.getBlue(),
			  color.getAlpha());

	cp = new ColorParm();
	cp.css = "rgb(255,0,0)";
	color = cp.createColor();

	System.out.format("rgb(255,0.0): red = %d, green = %d,"
			  + " blue = %d, alpha = %d\n",
			  color.getRed(), color.getGreen(), color.getBlue(),
			  color.getAlpha());

	cp = new ColorParm();
	cp.css = "red";
	cp.alpha = 128;
	color = cp.createColor();
	System.out.format("red: red = %d, green = %d, blue = %d, alpha = %d\n",
			  color.getRed(), color.getGreen(), color.getBlue(),
			  color.getAlpha());

	cp = new ColorParm();
	cp.css = "white";
	cp.alpha = 255;
	color = cp.createColor();
	System.out.format("white: red = %d, green = %d,"
			  + " blue = %d, alpha = %d\n",
			  color.getRed(), color.getGreen(), color.getBlue(),
			  color.getAlpha());


	BasicStrokeParm bsp = new BasicStrokeParm();

	bsp.width = 2.0;
	bsp.join = BasicStrokeParm.Join.MITER;
	bsp.miterLimit = 11.0;
	
	bsp.dashPhase = 0.001;
	bsp.cap = BasicStrokeParm.Cap.BUTT;
	bsp.dashIncrement = 5.0;
	bsp.dashPattern = "--  - -  ";
	
	float[] dashArray = BasicStrokeParm.getDashArray(bsp.dashPattern,
							 bsp.dashIncrement);

	boolean notFirst = false;
	for (float f: dashArray) {
	    if (notFirst) {
		System.out.print(", ");
	    } else {
		notFirst = true;
		System.out.print("dashArray = [");
	    }
	    System.out.print(f);
	}
	System.out.println("]");

	BasicStroke bs = bsp.createBasicStroke();
	System.out.format("cap = %d, join = %d, width = %f, limit = %f\n",
			  bs.getEndCap(), bs.getLineJoin(),
			  bs.getLineWidth(), bs.getMiterLimit());
	float[] array = bs.getDashArray();
	if (array == null) array = new float[0];
	System.out.format("dashPhase = %f, dashArray length = %d\n array:\n",
			  bs.getDashPhase(), array.length);
	for (int i = 0; i < array.length; i +=2) {
	    System.out.format("%f (dash), %f (space)\n", array[i], array[i+1]);
	}
	bsp.dashPattern = "-";
	bs = bsp.createBasicStroke();
	System.out.format("cap = %d, join = %d, width = %f, limit = %f\n",
			  bs.getEndCap(), bs.getLineJoin(),
			  bs.getLineWidth(), bs.getMiterLimit());
	array = bs.getDashArray();
	if (array == null) array = new float[0];
	System.out.format("dashPhase = %f, dashArray length = %d\n array:\n",
			  bs.getDashPhase(), array.length);
	for (int i = 0; i < array.length; i +=2) {
	    System.out.format("%f (dash), %f (space)\n", array[i], array[i+1]);
	}	

	System.out.println("\nGraphFontParm:");
	GraphFontParm gfp = new GraphFontParm();
	gfp.css="rgb(100, 150, 200)";
	gfp.alpha = 128;
	Graph.FontParms fp = gfp.createFontParms();
	System.out.format("angle = %5.3f, baseline = %s, justification = %s",
			  fp.getAngle(),
			  fp.getBaseline(),
			  fp.getJustification());
	Color fc = fp.getColor();
	System.out.format(", color = (%d,%d,%d,%d)\n",
			  fc.getRed(), fc.getGreen(), fc.getBlue(),
			  fc.getAlpha());
	Font f = fp.getFont();
	System.out.format("Font name=\"%s\", size=%d, plain=%s, bold=%s"
			  + ", italic=%s\n",
			  f.getName(), f.getSize(),
			  f.isPlain(), f.isBold(), f.isItalic());

	cp = new ColorParm.RED();
	Color c1 = cp.createColor();
	cp.css = "white";
	Color c2 = cp.createColor();
	System.out.println(c1 +" " + c2);
	cp = new ColorParm(new Color(100, 150, 200));
	System.out.println(cp.createColor());
	cp.css = "red";
	System.out.println("rbga = " + cp.getRed()
			   + ", " + cp.getGreen()
			   + ", " + cp.getBlue()
			   + ", " + cp.getAlpha());
	System.out.println(cp.createColor());
	cp.alpha = 128;
	System.out.println("rbga = " + cp.getRed()
			   + ", " + cp.getGreen()
			   + ", " + cp.getBlue()
			   + ", " + cp.getAlpha());
	Color c3 = cp.createColor();
	System.out.println(c3 + ", alpha = " + c3.getAlpha());
	cp.css = null;
	cp.alpha = null;
	c3 = cp.createColor();
	System.out.println(c3 + ", alpha = " +c3.getAlpha());
	System.out.println("rbga = " + cp.getRed()
			   + ", " + cp.getGreen()
			   + ", " + cp.getBlue()
			   + ", " + cp.getAlpha());
    }
}
