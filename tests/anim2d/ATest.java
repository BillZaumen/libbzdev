import org.bzdev.anim2d.Animation2D;
import org.bzdev.graphs.Graph;

public class ATest {
    public static void main(String argv[]) throws Exception {
	Animation2D a2d = new Animation2D(1000, 800);
	a2d.setOffsets(10, 50, 5, 55);
	a2d.setRanges(100.0, 100.0, 0.5, 0.5, 10.0, 20.0);
	int offLX = a2d.getXLowerOffset();
	int offLY = a2d.getYLowerOffset();
	int offUX = a2d.getXUpperOffset();
	int offUY = a2d.getYUpperOffset();

	double lowerX = a2d.getXLower();
	double lowerY = a2d.getYLower();
	double upperX = a2d.getXUpper();
	double upperY = a2d.getYUpper();

	System.out.format("offsets: %d, %d, %d, %d\n",
			  offLX, offUX, offLY, offUY);
	System.out.format("ranges: %g, %g, %g, %g\n",
			  lowerX, upperX, lowerY, upperY);

	System.out.println("scaleX: " +((1000-60)/(upperX-lowerX)));
	System.out.println("scaleY: " +((800-60)/(upperY - lowerY)));

	// used only to create a graph; will not run the animation
	a2d.initFrames(25, "tmp/col-", "png");

	Graph g = a2d.getGraph();

	int offLXActual = g.getXLowerOffset();
	int offLYActual = g.getYLowerOffset();
	int offUXActual = g.getXUpperOffset();
	int offUYActual = g.getYUpperOffset();

	double lowerXActual = g.getXLower();
	double lowerYActual = g.getYLower();
	double upperXActual = g.getXUpper();
	double upperYActual = g.getYUpper();

	if (offLX != offLXActual) {
	    throw new Exception("offset error");
	}
	if (offLY != offLYActual) {
	    throw new Exception("offset error");
	}
	if (offUX != offUXActual) {
	    throw new Exception("offset error");
	}
	if (offUY != offUYActual) {
	    throw new Exception("offset error");
	}

	if (Math.abs(lowerX - lowerXActual) > 1.e-15) {
	    throw new Exception("range error");
	}
	if (Math.abs(lowerY - lowerYActual) > 1.e-15) {
	    throw new Exception("range error");
	}
	if (Math.abs(upperX - upperXActual) > 1.e-15) {
	    throw new Exception("range error");
	}
	if (Math.abs(upperY - upperYActual) > 1.e-15) {
	    throw new Exception("range error");
	}

	Graph.ImageType type1 = g.getImageType();
	Graph.ImageType type2 = a2d.getImageType();
	System.out.println("type2 = " +type2);
	if (type1 != type2) {
	    throw new Exception("type1 != type2");
	}
	a2d = new Animation2D(1000, 800);
	a2d.requestAlpha(true);
	type2 = a2d.getImageType();
	System.out.println("type2 = " +type2);
	a2d.requestAlpha(false);
	type2 = a2d.getImageType();
	System.out.println("type2 = " +type2);

	System.exit(0);
    }
}
