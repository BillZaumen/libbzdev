import java.io.*;
import org.bzdev.p3d.*;


public class Cover {

    public static void main(String argv[]) throws Exception {
	var m3d = new Model3D();

	double thickness = 3.0;

	double depth = -7.0;

	double height = 25.4 * 4.5; // 4.5 inches, in mm
	double width = 25.4 * 4.5; // 4.5 inches, in mm

	double fwidth = 3.0;

	var sgb = new SteppedGrid.Builder(m3d, thickness, 0.0);

	sgb.addRectangles(-fwidth, -fwidth,
			  width + 2*fwidth, height + 2*fwidth,
			  0.0, depth);
	sgb.addRectangles(0.0, 0.0, width, height, 0.0, 10.0);

	try {
	    System.out.println("Generate an error due to the bottom of");
	    System.out.println("the model being above the top");
	    sgb.create();
	} catch (Exception e) {
	    System.out.println(e.getMessage());
	    P3d.printSteppedGridBuilderCalls(System.out, null, sgb);
	    System.out.println("EXCEPTION EXPECTED");
	    System.exit(0);
	}

	System.out.println("expected exception not thrown");
	System.exit(1);
    }
}
