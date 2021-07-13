import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;

import java.io.File;
import java.awt.Color;

public class PolarTest {

    public static void main(String argv[]) {
	try {
	    Animation2D animation = new Animation2D(1.0, 1);

	    PolarGridFactory gridf =
		new PolarGridFactory(animation);

	    double udist = (animation.getWidth() > animation.getHeight())?
		animation.getHeight(): animation.getWidth();
	    double scalef = udist/100.0;

	    animation.setRanges(0.0, 0.0, 0.5, 0.5, scalef, scalef);
	    animation.setBackgroundColor(Color.WHITE);

	    gridf.set("radialSpacing", 10);

	    gridf.createObject("grid");

	    int maxFrames = 5;
	    animation.initFrames(maxFrames, "ptmp/col-", "png");
	    animation.scheduleFrames(0, maxFrames);
	    File dir = new File("ptmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    animation.run(10);

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
