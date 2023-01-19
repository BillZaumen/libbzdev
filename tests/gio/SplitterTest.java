import org.bzdev.gio.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class SplitterTest {
    public static void main(String argv[]) throws Exception {

	SwingUtilities.invokeLater(() -> {
		BufferedImage image1 = new
		    BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		BufferedImage image2 = new
		    BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		SplitterGraphics2D g2d = new
		    SplitterGraphics2D(image1.createGraphics(),
				       image2.createGraphics());

		g2d.setBackground(Color.DARK_GRAY);
		g2d.clearRect(0, 0, 300, 300);
		g2d.setColor(Color.WHITE);
		g2d.drawString("Hello", 10, 100);
		Shape shape = new
		    Rectangle2D.Double(100.0, 100.0, 100.0, 100.0);
		g2d.setColor(Color.RED);
		g2d.fill(shape);
		g2d.setColor(Color.WHITE);
		g2d.draw(shape);

		JFrame frame = new JFrame();
		Container panel = frame.getContentPane();
		panel.setLayout(new FlowLayout());
		panel.add(new JLabel(new ImageIcon(image1)));
		panel.add(new JLabel(" = "));
		panel.add(new JLabel(new ImageIcon(image2)));
		frame.pack();
		frame.setVisible(true);
	    });
	new Thread(()-> {
		try {
		    Thread.currentThread().sleep(20000L);
		    System.exit(0);
		} catch (Exception ei){}
	}).run();
    }
}
