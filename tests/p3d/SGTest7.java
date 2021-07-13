import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;


public class SGTest7 {

    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);


	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);

	for (int i = 0; i < 8; i++) {
	    for (int j = 1; j < 8; j++) {
		if (i > 1 && i < 5  && j > 1 && j < 5) {
		    sg.addComponent(i, j, 0.0, 0.0, true, true);
		} else {
		    sg.addComponent(i,j, 0.0, 0.0);
		}
	    }
	}
	sg.addsCompleted();
	Path3DInfo.printSegments(System.out, sg.getBoundary());

	/*
 	System.out.println("upper:");
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		int flag = sg.externalEdges(i, j, true);
		if (flag != 0) {
		    String edges = "";
		    if ((flag & SteppedGrid.RIGHT) != 0) {
			edges = edges + "R";
		    }
		    if ((flag & SteppedGrid.LEFT) != 0) {
			edges = edges + "L";
		    }
		    if ((flag & SteppedGrid.UP) != 0) {
			edges = edges + "U";
		    }
		    if ((flag & SteppedGrid.DOWN) != 0) {
			edges = edges + "D";
		    }
		    System.out.format("(%d,%d): edges = %s\n",
				      i, j, edges);
		}
	    }
	}

	System.out.println("lower:");
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		int flag = sg.externalEdges(i, j, false);
		if (flag != 0) {
		    String edges = "";
		    if ((flag & SteppedGrid.RIGHT) != 0) {
			edges = edges + "R";
		    }
		    if ((flag & SteppedGrid.LEFT) != 0) {
			edges = edges + "L";
		    }
		    if ((flag & SteppedGrid.UP) != 0) {
			edges = edges + "U";
		    }
		    if ((flag & SteppedGrid.DOWN) != 0) {
			edges = edges + "D";
		    }
		    System.out.format("(%d,%d): edges = %s\n",
				      i, j, edges);
		}
	    }
	}
	*/
    }
}
