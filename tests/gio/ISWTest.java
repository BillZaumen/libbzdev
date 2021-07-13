import org.bzdev.gio.OSGraphicsOps;
import org.bzdev.gio.ImageSequenceWriter;
import java.awt.*;


public class ISWTest {

    public static void main(String argv[]) throws Exception {
	ImageSequenceWriter isw = new ImageSequenceWriter("iswtest1.isq");

	isw.addMetadata(800, 600, "image/png");

	for (int i = 0; i < 10; i++) {
	    OSGraphicsOps osg = isw.nextOutputStreamGraphics();
	    Graphics2D g2d = osg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.drawString("example string\u00b9\u2074", 250, 400);
	    g2d.fillRect(100, 100, 200 + 10*i, 200);
	    g2d.dispose();
	    osg.imageComplete();
	    osg.close();
	}
	isw.close();

	isw = new ImageSequenceWriter("iswtest2.isq");
	isw.addMetadata(800, 600, "image/png");

	for (int i = 0; i < 10; i++) {
	    OSGraphicsOps osg = (i == 5)?
		isw.nextOutputStreamGraphics(5):
		isw.nextOutputStreamGraphics();
	    Graphics2D g2d = osg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.drawString("example string\u00b9\u2074", 250, 400);
	    g2d.fillRect(100, 100, 200 + 10*i, 200);
	    g2d.dispose();
	    osg.imageComplete();
	    osg.close();
	}
	isw.close();

	isw = new ImageSequenceWriter("iswtest3.isq");

	isw.addMetadata(800, 600, "image/png", "image%02d.png");

	for (int i = 0; i < 10; i++) {
	    OSGraphicsOps osg = isw.nextOutputStreamGraphics();
	    Graphics2D g2d = osg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.drawString("example string\u00b9\u2074", 250, 400);
	    g2d.fillRect(100, 100, 200 + 10*i, 200);
	    g2d.dispose();
	    osg.imageComplete();
	    osg.close();
	}
	isw.close();

	isw = new ImageSequenceWriter("iswtest4.isq");
	isw.addMetadata(800, 600, "image/png", "image%02d.png");

	for (int i = 0; i < 10; i++) {
	    OSGraphicsOps osg = (i == 5)?
		isw.nextOutputStreamGraphics(true, 9, 5):
		isw.nextOutputStreamGraphics(true, 9);
	    Graphics2D g2d = osg.createGraphics();
	    g2d.setColor(Color.red);
	    g2d.drawString("example string\u00b9\u2074", 250, 400);
	    g2d.fillRect(100, 100, 200 + 10*i, 200);
	    g2d.dispose();
	    osg.imageComplete();
	    osg.close();
	}
	isw.close();
    }
}


