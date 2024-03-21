import org.bzdev.geom.*;
import java.awt.*;
import java.awt.geom.*;
import org.bzdev.math.*;


public class Path2DHexTest {
    public static void main(String argv[]) throws Exception {

	Path2D path = new Path2D.Double();
	path.moveTo(1.0444816024023955, 0.6549075927859999);
	path.lineTo(0.9240998746568618, 1.4882247504128627);
	path.lineTo(1.0953335453526905, 1.7719619197070824);
	path.lineTo(1.3412507630091703, 1.7684861777084644);
	path.lineTo(1.4599813898287497, 1.5531018770331972);
	path.lineTo(1.2890227904570755, 0.6575574478311291);
	path.closePath();

	System.out.format("center = (%g, %g)  (approximately)\n",
			  (0.9240998746568618 + 1.4599813898287497) / 2.0,
			  (0.6549075927859999 + 1.7719619197070824) /2.0);

	Point2D cm = Path2DInfo.centerOfMassOf(path);
	System.out.println("cm = " + cm);

	double[][] moments = Path2DInfo.momentsOf(cm, path);

	double[] pmoments = Path2DInfo.principalMoments(moments);
	System.out.println("Principal moments: " + pmoments[0]
			   + ", " + pmoments[1]);

	double[][] paxes  = Path2DInfo.principalAxes(moments);
	for (int i = 0; i < 2; i++) {
	    System.out.println("Principal axes " + i
			       + ": ("+ paxes[i][0]
			       + ", " + paxes[i][1] + ")");
	}
	System.out.println("... dot product = "
			   + VectorOps.dotProduct(paxes[0], paxes[1]));

    }
}
