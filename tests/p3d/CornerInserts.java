import java.io.FileOutputStream;
import org.bzdev.p3d.*;
import org.bzdev.geom.*;

public class CornerInserts {
    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D();
	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 30.0, 0.0);

	// metal is 3.048 mm (0.12 inches) thick: 304 stainless steel
	// listed for  ponoko.com
	// Allow a 3.5 mm gap - the extra 0.5mm is for tolerances.
	double halfGap = 1.75;
	double halfThickness = 2.5;
	double off = 1;
	double tail = 46 + 5 + off;
	double delta = 6.0;


	sgb.addRectangles(0.0, 0.0, 2.0*(halfGap + halfThickness), tail,
			  0.0, 0.0);
	sgb.addRectangles(0.0, 0.0,
			  2.0*(halfGap + halfThickness) + 10.0, 5.0,
			  0.0, 0.0);
	
	sgb.addRectangles(0, tail,
			  2*(halfGap + halfThickness), 30.0, 
			  0.0, 0.0);
	sgb.addRectangles(0, tail+10,
			  2*(halfGap + halfThickness), 20.0, 
			  -20, 0.0);
	sgb.addRectangles(halfThickness, tail+10,
			  2*halfGap, 20.0,
			  -20, delta);
	sgb.addRectangles(halfThickness, tail,
			  2*halfGap, 10.0,
			  0.0, delta);
	sgb.removeRectangles(halfThickness, tail,
			     2*halfGap, delta);
	SteppedGrid sg = sgb.create();

	System.out.println("bounding box (front tab) = " + sg.getBounds());

	double offset = 10.0;

	sgb = new SteppedGrid.Builder(m3d, 30.0, 0.0);
	sgb.addRectangles(offset, offset,
			  2*(halfGap + halfThickness), 30.0, 
			  0.0, 0.0);
	sgb.addRectangles(offset, offset+10,
			  2*(halfGap + halfThickness), 20.0, 
			  -20, 0.0);
	sgb.addRectangles(offset + halfThickness, offset+10,
			  2*halfGap, 20.0,
			  -20, delta);
	sgb.addRectangles(offset + halfThickness, offset,
			  2*halfGap, 10.0,
			  0.0, delta);
	sgb.removeRectangles(offset + halfThickness, offset,
			     2*halfGap, delta);
	sg = sgb.create();
	System.out.println("bounding box (rear tab) = " + sg.getBounds());

	if (m3d.notPrintable(System.out)) System.exit(1);
	System.exit(0);
    }
}
