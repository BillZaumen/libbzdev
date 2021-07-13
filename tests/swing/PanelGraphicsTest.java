import org.bzdev.swing.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PanelGraphicsTest {

    static JFrame frame;

    private static void init() {
	frame = new JFrame("Panel Graphics Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
 
        frame.setSize(800,800);
        fpane.setLayout(new BorderLayout());
	
    }

    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    init();
		}
	    });

	final PanelGraphics pg = new PanelGraphics(100, 100, true);
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    JPanel panel = pg.getPanel();
		    frame.add("Center", panel);
		    frame.setVisible(true);
		}
	    });

	Graphics2D g2d = pg.createGraphics();
	pg.setMode(PanelGraphics.Mode.FIT);
	g2d.setColor(Color.RED);
	g2d.fillRect(5, 5, 90, 90);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 90, 90);
	pg.imageComplete();
	Thread.sleep(10000);
	pg.reset();
	Thread.sleep(1000);
	g2d = pg.createGraphics();
	pg.setMode(PanelGraphics.Mode.FIT_HORIZONTAL);
	g2d.setColor(Color.YELLOW);
	g2d.fillRect(5, 5, 90, 90);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 90, 90);
	pg.imageComplete();
	Thread.sleep(10000);

	pg.reset();
	Thread.sleep(1000);
	g2d = pg.createGraphics();
	pg.setMode(PanelGraphics.Mode.FIT_VERTICAL);
	g2d.setColor(Color.ORANGE);
	g2d.fillRect(5, 5, 90, 90);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 90, 90);
	pg.imageComplete();
	Thread.sleep(10000);

	pg.reset();
	Thread.sleep(1000);
	g2d = pg.createGraphics();
	pg.setMode(PanelGraphics.Mode.AS_IS);
	g2d.setColor(Color.GREEN);
	g2d.fillRect(5, 5, 90, 90);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 90, 90);
	pg.imageComplete();
	Thread.sleep(10000);

	pg.reset();
	g2d = pg.createGraphics();
	pg.setMode(PanelGraphics.Mode.FILL);
	g2d.setColor(Color.BLUE);
	g2d.fillRect(5, 5, 90, 90);
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(5.0F));
	g2d.drawRect(5, 5, 90, 90);
	pg.imageComplete();
	Thread.sleep(10000);
	System.exit(0);
    }
}
