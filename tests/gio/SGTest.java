import org.bzdev.gio.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import javax.swing.*;


public class SGTest {

    static JPanel panel;

    public static void main(String argv[]) throws Exception {

	SurrogateGraphics2D sg2d = new SurrogateGraphics2D(false);
	final Graphics2DRecorder g2dr = new Graphics2DRecorder(sg2d);

	Graphics2D g2d = g2dr.createGraphics();

	g2d.setClip(30, 30, 700, 500);
	System.out.println(g2d.getClipBounds());

	g2d.setColor(Color.RED);
	g2d.fillRect(5, 5, 700, 500);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 700, 500);

	Graphics2D g2d2 = (Graphics2D)g2d.create();
	g2d2.setColor(Color.WHITE);
	Font f = new Font(Font.SANS_SERIF, Font.BOLD, 14);
	g2d2.setFont(f);
	g2d2.drawString("Hello", 100, 100);
	g2d2.dispose();
	g2d.setFont(f);
	g2d.drawString("Hello", 100, 130);

	g2d.dispose();


	BufferedImage bi = new BufferedImage(800, 600,
					     BufferedImage.TYPE_INT_ARGB_PRE);
    
	g2d = bi.createGraphics();
	g2dr.playback(g2d);
	g2d.dispose();

	ImageIO.write(bi, "png", new FileOutputStream("sgtest.png"));

	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    JFrame frame = new JFrame("panel test");
		    Container fpane = frame.getContentPane();
		    fpane.setLayout(new BorderLayout());
		    frame.addWindowListener(new WindowAdapter () {
			    public void windowClosing(WindowEvent e) {
				System.exit(0);
			    }
			});
		    panel = new JPanel() {
			    public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g;
				g2dr.playback(g2d);
			    }
			};
		    panel.setSize(800, 600);
		    frame.setSize(820, 620);
		    fpane.add(panel);
		    frame.setVisible(true);
		}
	    });

	Thread.sleep(7500L);

	g2dr.reset();
	g2d = g2dr.createGraphics();
	g2d.setClip(30, 50, 700, 500);
	g2d.setColor(Color.YELLOW);
	g2d.fillRect(5, 5, 700, 500);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 700, 500);
	g2d.dispose();
	panel.repaint();
	Thread.sleep(7500L);
	System.exit(0);
    }
}
