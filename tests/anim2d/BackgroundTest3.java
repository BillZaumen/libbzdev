import org.bzdev.anim2d.*;
import org.bzdev.gio.ImageSequenceWriter;
import org.bzdev.graphs.Graph;


import java.io.File;
import java.awt.*;
import java.util.*;

// We found a case in version 1.1.99 where white text
// no longer prints. This is an attempt to reproduce the
// problem

public class BackgroundTest3 {

    public static void main(String argv[]) {
	try {
	    Animation2D a2d = new Animation2D(1.0, 1);

	    AnimationLayer2DFactory textf =
		new AnimationLayer2DFactory(a2d);

	    a2d.setRanges(0.0, 0.0, 0.5, 0.5, 1.0, 1.0);
	    a2d.setBackgroundColor(Color.WHITE);
	    a2d.setFontColor(Color.RED);
	    a2d.setFontJustification(Graph.Just.LEFT);
	    a2d.setFont(new Font("Helvetica", Font.BOLD, 26));

	    AnimationLayer2D layer = new AnimationLayer2D(a2d,"text", true);
	    layer.initGraphicArray
		(Arrays.asList
		 (new Graph.Graphic() {
			 public void addTo(Graph graph,
					   Graphics2D g2d, Graphics2D g2dGCS)
			 {
			     System.out.println("... drawing hello");
			     graph.drawString("hello", 0.0, 0.0);
		     }
		 }));
	    layer.setZorder(1, true);

	    int maxFrames = 5;
	    a2d.initFrames(maxFrames, "b3tmp/col-", "png");
	    a2d.scheduleFrames(0, maxFrames);
	    File dir = new File("b3tmp");
	    dir.mkdirs();
	    for (File file: dir.listFiles()) {
		file.delete();
	    }
	    a2d.run();

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
