import org.bzdev.p3d.*;
import org.bzdev.gio.*;
import org.bzdev.graphs.Graph;
import org.bzdev.anim2d.Animation2D;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;

public class SGBTest12 {
    public static void main(String argv[]) throws Exception {
	// create a model for a 3D object

	Model3D m3d = new Model3D();

	// useful for debugging.
	m3d.setStackTraceMode(true);

	SteppedGrid.Builder sgb = new SteppedGrid.Builder(m3d, 10.0, 0.0);

	sgb.addRectangles(0.0, 0.0, 100.0, 100.0, 0.0, 0.0);
	sgb.addRectangles(10.0, 10.0, 80.0, 80.0, 10.0, 0.0);
	sgb.removeRectangles(20.0, 20.0, 60.0, 60.0);
	sgb.create();

	System.out.println("m3d.size() = " + m3d.size());
	if (m3d.notPrintable(System.out)) {
	    System.exit(1);
	}

	m3d.createImageSequence(new FileOutputStream("sgbtest12.isq"),
				"png", 8, 4, 0.0, 0.3, 0.01, true);
    }
}
