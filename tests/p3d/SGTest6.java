import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;


public class SGTest6 {

    public static void main(String argv[]) throws Exception {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);


	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};

	SteppedGrid sg1 = new SteppedGrid(m3d, xs, ys, 1.0, true);
	SteppedGrid sg2 = new SteppedGrid(m3d, xs, ys, -1.0, false);


	for (int i = 1; i < 6; i++) {
	    for (int j = 1; j < 6; j++) {
		if (i == 3 && j == 3) {
		    sg1.addComponent(i, j, 1.0, true);
		    sg2.addComponent(i, j, -1.0, true);
		} else {
		    sg1.addComponent(i,j, 0.0);
		    sg2.addComponent(i,j, 0.0);
		}
	    }
	}
	sg1.addComponent(4, 7, 1.0);
	sg1.addComponent(7, 4, 1.0);
	sg2.addComponent(4, 7, -1.0);
	sg2.addComponent(7, 4, -1.0);

	sg1.addsCompleted();
	sg2.addsCompleted();

	/*
 	System.out.println("upper:");
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		if (i < 8 && j < 8) {
		    boolean b1 = sg1.isComponentFilled(i, j, true);
		    boolean b2 =  sg1.isComponentPlaceholder(i, j, true);
		    boolean b3 = sg1.isExternalVertex(i, j, true);
		    if (b1 || b2 || b3) {
			System.out.format("(%d, %d): filled = %b, "
					  + "placeholder = %b, "
					   + "external-vertex = %b\n",
					  i, j, b1, b2, b3);
		    }
		} else {
		    boolean b3 = sg1.isExternalVertex(i, j, true);
		    if (b3) {
			System.out.format("(%d, %d): filled = %b, "
					  + "placeholder = %b, "
					  + "external-vertex = %b\n",
					  i, j, false, false, b3);
		    }
		}
	    }
	}
	*/
	
	/*
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		int flag = sg1.externalEdges(i, j, true);
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
		if (i < 8 && j < 8) {
		    boolean b1 = sg2.isComponentFilled(i, j, false);
		    boolean b2 =  sg2.isComponentPlaceholder(i, j, false);
		    boolean b3 = sg2.isExternalVertex(i, j, false);
		    if (b1 || b2 || b3) {
			System.out.format("(%d, %d): filled = %b, "
					  + "placeholder = %b, "
					  + "external-vertex = %b\n",
					  i, j, b1, b2, b3);
		    }
		} else {
		    boolean b3 = sg2.isExternalVertex(i, j, false);
		    if (b3) {
			System.out.format("(%d, %d): filled = %b, "
					  + "placeholder = %b, "
					  + "external-vertex = %b\n",
					  i, j, false, false, b3);
		    }
		}
	    }
	}
	for (int i = 0; i < 9; i++) {
	    for (int j = 0; j < 9; j++) {
		int flag = sg2.externalEdges(i, j, false);
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
