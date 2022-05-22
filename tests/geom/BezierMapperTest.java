import java.awt.geom.Path2D;
import java.io.FileOutputStream;
import org.bzdev.p3d.*;
import org.bzdev.geom.*;

public class BezierMapperTest {
    public static void main(String argv[]) throws Exception {
	Path2D tpath = Paths2D.createArc(0.0, 0.0, 0.0, -1.0, 2*Math.PI);
	tpath.closePath();
	Path3D line = new Path3D.Double();

	double baseThickness = 4.5;
	double height0 = 44.45;
	double height2 = 10.0;
	double height1 = height0 - height2;
	double width = 6.0;
	double r = 1.0;

	line.moveTo(width/2.0, height1/2, baseThickness);
	line.lineTo(width/2.0, height1/2, 0.0);
	double inormal[] = {0.0, -1.0, 0.0};
	BezierGrid bg = new BezierGrid(tpath,
				       BezierGrid.getMapper(line, inormal));

	Model3D m3d = new Model3D();
	m3d.append(bg);
	m3d.setTessellationLevel(4);
	m3d.createImageSequence(new FileOutputStream("BezierMapperTest.isq"),
						     "png", 8,8);
	m3d.append(bg);
    }

}
