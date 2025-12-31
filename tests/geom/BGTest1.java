import org.bzdev.geom.*;
import org.bzdev.p3d.*;
import java.io.*;

public class BGTest1 {
    public static void main(String argv[]) throws Exception {
	double tangent[] = {1.0, 0.0, 0.0};
	double normal[] = {0.0, 0.0, 1.0};

	Path3D arc1 = Paths3D.createArc(0.0, 0.0, 0.0,
					tangent, normal, 100.0,
					2*Math.PI);
	arc1.closePath();
	Path3D arc2 = Paths3D.createArc(0.0, 0.0, 0.0,
					tangent, normal, 100.0,
					2*Math.PI);
	arc1.closePath();
	arc2.closePath();

	BezierGrid cylinder = new BezierGrid(arc1, arc2);
	/*
	BezierCap cap1 = new BezierCap(arc1, 0.0, false);
	BezierCap cap2 = new BezierCap(arc2, 0.0, false);
	cap2.reverseOrientation(true);
	*/
	Model3D m3d = new Model3D();
	m3d.append(cylinder);
	/*
	m3d.append(cap1);
	m3d.append(cap2);
	*/

	m3d.setTessellationLevel(2);

	m3d.checkTessellation(System.out, true);
	System.out.println("---------------------------");
	m3d.checkTessellation(System.out, false);

    }
}
