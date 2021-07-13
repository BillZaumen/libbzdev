import org.bzdev.p3d.*;
import java.util.List;

public class HollowCylinderTest2 {
    public static void main(String[] argv) throws Exception {

	Model3D m3d = new Model3D();
	double r = 10.0;
	for (int i = 0; i < 360; i++) {
	    double theta1 = Math.toRadians(i/1.0);
	    int j = (i+1) % 360;
	    double theta2 = Math.toRadians((j)/1.0);
	    double x1 = r * Math.cos(theta1);
	    double y1 = r * Math.sin(theta1);
	    double z1 = 0.0;
	    double x2 = r * Math.cos(theta2);
	    double y2 = r * Math.sin(theta2);
	    double z2 = 10.0;
	    P3d.Rectangle.addV(m3d, x1, y1, z1, x2, y2, z2);
	    m3d.addTriangle(0.0, 0.0, z2, x1, y1, z2, x2, y2, z2);
	    m3d.addFlippedTriangle(0.0, 0.0, z1, x1, y1, z1, x2, y2, z1);
	}
	double area1 = 2.0*Math.PI*(r* 10.0 + r*r);
	double volume1 = 10.0 * Math.PI * r * r;

	r = 5.0;
	for (int i = 0; i < 360; i++) {
	    double theta1 = Math.toRadians(i/1.0);
	    int j = (i+1) % 360;
	    double theta2 = Math.toRadians((j)/1.0);
	    double x1 = r * Math.cos(theta1);
	    double y1 = r * Math.sin(theta1);
	    double z1 = 1.0;
	    double x2 = r * Math.cos(theta2);
	    double y2 = r * Math.sin(theta2);
	    double z2 = 9.0;
	    // make this one point outwards to generate an error
	    P3d.Rectangle.addV(m3d, x1, y1, z1, x2, y2, z2);
	    m3d.addTriangle(0.0, 0.0, z2, x1, y1, z2, x2, y2, z2);
	    m3d.addFlippedTriangle(0.0, 0.0, z1, x1, y1, z1, x2, y2, z1);
	}
	double area2 = 2.0 * Math.PI*(r* 8.0 + r*r);
	double volume2 = -8.0 * Math.PI * r * r;

	List<Model3D.Edge> list1 = m3d.verifyClosed2DManifold();
	if (list1 != null) {
	    P3d.printEdgeErrors(System.out, list1);
	    System.exit(1);
	}
	List<Model3D.Triangle> list2 = m3d.verifyEmbedded2DManifold();
	if (list2 != null) {
	    P3d.printTriangleErrors(System.out, list2);
	    System.exit(1);
	}

	if (m3d.verifyNesting(System.out) == false) {
	    System.out.println("[error messages expected]");
	    System.exit(0);
	} else {
	    System.out.println("test should have failed");
	}

    }
}
