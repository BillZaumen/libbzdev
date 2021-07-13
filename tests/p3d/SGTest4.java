import org.bzdev.p3d.*;
import java.awt.*;
import java.awt.image.*;


public class SGTest4 {

    public static void main(String argv[]) throws Exception {

	Model3D m3d = new Model3D();
	m3d.setStackTraceMode(true);

	double xs[] = {0.0, 1.0, 3.0, 5.0};
	double ys[] = {0.0, 2.0, 4.0, 6.0};

	int[][] indices = {{0,0}, {0, 1}, {0, 2},
			   {1,0}, {1, 2},
			   {2,0}, {2, 1}, {2, 2}};


	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);

	for (int[] pair: indices) {
	    int i = pair[0];
	    int j = pair[1];
	    sg.addComponent(i, j, 0.0, 0.0, true, true);
	}

	sg.addComponent(1, 1, 1.0, -1.0, true, true);

	sg.addsCompleted();

	System.out.format("(%g, %g, %g)\n",
			  sg.getX(1), sg.getY(1),
			  sg.getZ(1,1,true));
	System.out.format("(%g, %g, %g)\n",
			  sg.getX(1), sg.getY(1),
			  sg.getZ(1,1,false));

	System.out.println("upper:");
	for (double z: sg.getZs(1, 1, true)) {
	    System.out.println("    " + z);
	}
	System.out.println("lower:");
	for (double z: sg.getZs(1, 1, false)) {
	    System.out.println("    " + z);
	}
	
	System.out.println("both:");
	for (double z: sg.getZs(1, 1)) {
	    System.out.println("    " + z);
	}
    }
}
