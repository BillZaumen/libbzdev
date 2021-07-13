import org.bzdev.p3d.*;
import org.bzdev.geom.*;
import java.awt.*;
import java.awt.image.*;

public class SGTest9 {

    public static void main(String argv[]) throws Exception
    {
	Model3D m3d = new Model3D(false);
	m3d.setStackTraceMode(true);


	double xs[] = {0.0, 1.0, 2.0, 3.0};
	double ys[] = {0.0, 1.0, 2.0, 3.0};

	SteppedGrid sg = new SteppedGrid(m3d, xs, ys, 1.0, -1.0);

	sg.addComponent(0, 1, 1.0, -1.0);
	sg.addComponent(1, 0, 1.0, -1.0);
	sg.addComponent(1, 1, 1.0, -1.0);
	sg.addComponent(1, 2, 1.0, -1.0);
	sg.addComponent(2, 1, 1.0, -1.0);

	sg.addHalfComponent(0, 0, 1.0, -1.0, false, false);
	sg.addHalfComponent(2, 0, 1.0, -1.0, false, false);
	sg.addHalfComponent(0, 2, 1.0, -1.0, false, false);
	sg.addHalfComponent(2, 2, 1.0, -1.0, false, false);

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
			edges = edges + "Dur";
		    }
		    if ((flag & SteppedGrid.UPLEFT) != 0) {
			edges = edges + "Dul";
		    }
		    if ((flag & SteppedGrid.DOWNRIGHT) != 0) {
			edges = edges + "Ddr";
		    }
		    if ((flag & SteppedGrid.DOWNLEFT) != 0) {
			edges = edges + "Ddl";
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
			edges = edges + "Dur";
		    }
		    if ((flag & SteppedGrid.UPLEFT) != 0) {
			edges = edges + "Dul";
		    }
		    if ((flag & SteppedGrid.DOWNRIGHT) != 0) {
			edges = edges + "Ddr";
		    }
		    if ((flag & SteppedGrid.DOWNLEFT) != 0) {
			edges = edges + "Ddl";
		    }
		    System.out.format("(%d,%d): edges = %s\n",
				      i, j, edges);
		}
	    }
	}
	*/

	sg.addsCompleted();


	boolean error = false;
	if (m3d.notPrintable(System.out)) {
	    System.out.println("not printable");
	    error = true;
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
	image.setCoordRotation(0.0, Math.PI/2 - Math.PI/12, 0.0);
	m3d.setImageParameters(image);
	m3d.render(image);
	image.write("png", "sgtest9.png");
	System.exit(error? 1: 0);
    }

}