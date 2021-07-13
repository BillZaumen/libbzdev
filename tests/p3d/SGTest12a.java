import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import org.bzdev.math.rv.*;
import java.awt.*;
import java.awt.image.*;

public class SGTest12a {

    public static void main(String argv[]) throws Exception
    {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);

	double z1 = 0.0;
	double z2 = 0.0;


	double xs[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 10.0, -10.0);

	for (int i = 0; i < xs.length-1; i++) {
	    for (int j = 0; j < ys.length-1; j++) {
		if ((i < 3 || i > 7 || j < 3 || j > 7)) {
		    sg.addComponent(i, j, z1, z2);
		}
		if (i > 3 && i < 7 && j > 3 && j < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
		if ((i == 3 || i == 7) && j > 3 && j < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
		if ((j == 3 || j == 7) && i > 3 && i < 7) {
		    sg.addComponent(i, j, z1, z2, true, false);
		}
	    }
	}
	sg.addHalfComponent(3,3, z1, -z2, true, false);
	sg.addHalfComponent(3,7, z1, -z2, true, false);
	sg.addHalfComponent(7,3, z1, -z2, true, false);
	sg.addHalfComponent(7,7, z1, -z2, true, false);
	sg.addsCompleted();
	/*
 	System.out.println("upper:");
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
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
		    if ((flag & SteppedGrid.VERTICAL) != 0) {
			edges = edges + "V";
		    }
		    if ((flag & SteppedGrid.UPRIGHT) != 0) {
			edges = edges + "ur";
		    }
		    if ((flag & SteppedGrid.UPLEFT) != 0) {
			edges = edges + "ul";
		    }
		    if ((flag & SteppedGrid.DOWNRIGHT) != 0) {
			edges = edges + "dr";
		    }
		    if ((flag & SteppedGrid.DOWNLEFT) != 0) {
			edges = edges + "dl";
		    }
		    System.out.format("(%d,%d): edges = %s\n",
				      i, j, edges);
		}
	    }
	}
 	System.out.println("lower:");
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
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
		    if ((flag & SteppedGrid.VERTICAL) != 0) {
			edges = edges + "V";
		    }
		    if ((flag & SteppedGrid.UPRIGHT) != 0) {
			edges = edges + "ur";
		    }
		    if ((flag & SteppedGrid.UPLEFT) != 0) {
			edges = edges + "ul";
		    }
		    if ((flag & SteppedGrid.DOWNRIGHT) != 0) {
			edges = edges + "dr";
		    }
		    if ((flag & SteppedGrid.DOWNLEFT) != 0) {
			edges = edges + "dl";
		    }
		    System.out.format("(%d,%d): edges = %s\n",
				      i, j, edges);
		}
	    }
	}
	*/
	sg.addsCompleted();

	boolean error = false;

	Path3D boundary = sg.getBoundary();
	if (boundary != null) {
	    Path3DInfo.printSegments(System.out, boundary);
	} else {
	    System.out.println("null boundary");
	}

	if (sg.isClosedManifold()) {
	    System.out.println("closed manifold not expected");
	}

	int WIDTH=800;
	int HEIGHT = 600;
	Model3D.Image image =
	    new Model3D.Image(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
	image.setBacksideColor(Color.RED);
	image.setEdgeColor(Color.GREEN);
	Graphics2D g2d = image.createGraphics();
	g2d.setColor(Color.BLUE.darker());
	g2d.fillRect(0, 0, WIDTH, HEIGHT);
	g2d.dispose();
	image.setCoordRotation(Math.PI/4, Math.PI/4-Math.PI/12, 0.0);

	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest12a.png");
	System.exit(error? 1: 0);
    }
}
