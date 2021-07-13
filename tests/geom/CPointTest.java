import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import org.bzdev.geom.*;
import org.bzdev.geom.SplinePathBuilder.CPoint;
import org.bzdev.geom.SplinePathBuilder.CPointType;
import org.bzdev.graphs.Graph;

public class CPointTest {
    public static void main(String argv[]) throws Exception {
	AffineTransform af = AffineTransform.getTranslateInstance(50.0, 10.0);
	af.rotate(Math.PI/4.0);
	    
	CPoint[] cpoints = {
	    new CPoint(CPointType.MOVE_TO, 10.0, 10.0),
	    new CPoint(CPointType.SEG_END, 40.0, 40.0),
	    new CPoint(CPointType.MOVE_TO, 10.0, 10.0, af),
	    new CPoint(CPointType.SEG_END, 40.0, 40.0, af)
	};
	SplinePathBuilder spb = new SplinePathBuilder();
	spb.append(cpoints);
	SplinePath2D path = spb.getPath();
	Graph graph = new Graph(700,700);

	graph.setOffsets(25,25);
	graph.setRanges(-100.0, 100.0 , -100.0, 100.0);
	
	Graphics2D g2d = graph.createGraphics();
	g2d.setStroke(new BasicStroke(4.0F));
	g2d.setColor(Color.BLACK);
	graph.draw(g2d, path);
	graph.write("png", new File("cptest.png"));
    }
}
