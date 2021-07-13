import org.bzdev.imageio.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;

public class ObserverTest {
    static Image image;

    public static void main(String argv[]) throws Exception {
	final BufferedImage bi = new BufferedImage(700, 700,
						   BufferedImage.TYPE_INT_ARGB);

	File f = new File("../graphs/testimg.png");
	final URL url = f.getAbsoluteFile().toURI().toURL();

	final BlockingImageObserver bio =
	    new BlockingImageObserver(false, false,
				      false, true);
	(new Thread(new Runnable() {
		public void run() {
		    try {
			Thread.sleep(1000L);
			image = ImageIO.read(url);
			int infoflags = ImageObserver.WIDTH
			    | ImageObserver.HEIGHT
			    | ImageObserver.PROPERTIES
			    | ImageObserver.ALLBITS;
			bi.createGraphics().drawImage(image,
						      new AffineTransform(),
						      null);
			bio.imageUpdate(image, infoflags, 0, 0, 0, 0);
		    } catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		    }
		}
	    })).run();
	System.out.println("waiting for image to load ...");
	bio.waitUntilDone();
	System.out.println("...done");
	ImageIO.write(bi, "png", new File("obtest.png"));

	final BufferedImage bi2 = new
	    BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);

	f = new File("../graphs/testimg-none.png");
	final URL url2 = f.getAbsoluteFile().toURI().toURL();

	final BlockingImageObserver bio2 =
	    new BlockingImageObserver(false, false,
				      false, true);
	(new Thread(new Runnable() {
		public void run() {
		    try {
			Thread.sleep(1000L);
			image = ImageIO.read(url2);
			int infoflags = ImageObserver.WIDTH
			    | ImageObserver.HEIGHT
			    | ImageObserver.PROPERTIES
			    | ImageObserver.ALLBITS;
			bi2.createGraphics().drawImage(image,
						       new AffineTransform(),
						       null);
			bio2.imageUpdate(image, infoflags, 0, 0, 0, 0);
		    } catch (Exception e) {
			System.out.println("exception expected: no image");
			System.exit(0);
		    }
		}
	    })).run();
	System.out.println("waiting for image to load ...");
	bio.waitUntilDone();
	System.out.println("... exception should have been thrown");
	System.exit(1);
  }
}