import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Buttons {

    static String taValue = "";
    static String tfValue = "";

    static String utaText = "";
    static String utaURL = "";
    static boolean utaInUse = false;

    static int port = 0;

    static void init() {
	JFrame frame = new JFrame("Button Test");
        frame.setLayout(new FlowLayout());
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

	JButton button1 = new JButton("button1");
	JButton button2 = new JButton("button2");

	button1.addActionListener((ae) -> {
		System.out.println("button1 pushed");
	    });
	button2.addActionListener((ae) -> {
		System.out.println("button2 pushed");
	    });

	frame.add(button1);
	frame.add(button2);

	frame.pack();

	frame.setVisible(true);
    }

    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(() -> {
		try {
		    UIManager.setLookAndFeel(UIManager
					     .getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(1);
		}
	    });
	SwingUtilities.invokeLater(() -> {
		init();
	    });
    }
}
