import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

// Just use Java built-in classes for this test.
public class TableTest {

    public static void setup() {

	String[] colNames = {"Test Data 1", "Test Data 2"};
	String data[][] = {
	    {"Line 1", ""},
	    {"Line 2", ""},
	    {"", ""},
	    {"", ""},
	    {"", ""},
	    {"", ""}
	};


	JTable table = new JTable(data, colNames);

	final JTextField tf = new JTextField();

	// Uncomment to instrument part of what the text field is doing.
	tf.addComponentListener(new ComponentListener() {
		public void componentShown(ComponentEvent e) {
		    System.out.println("shown");
		}
		public void componentMoved(ComponentEvent e) {
		    System.out.format("moved to (%d, %d)\n",
				      tf.getX(), tf.getY());
		}
		public void componentResized(ComponentEvent e) {
		    System.out.format("resized to w = %d, h= %d\n",
				      tf.getWidth(), tf.getHeight());;
		}
		public void componentHidden(ComponentEvent e) {
		    System.out.println("hidden");
		}
	    });
	tf.addCaretListener((ce) -> {
		System.out.println("caret position = " + ce.getDot()
				   + ", text = " +  tf.getText());
		Object src = ce.getSource();
		System.out.println("... tf bg = " + tf.getBackground());
		System.out.println("... tf fg = " + tf.getForeground());
		System.out.println("... tf caret = " + tf.getCaretColor());
		System.out.println("... tf font = " +tf.getFont());
		System.out.println("... tf showing = " + tf.isShowing());
	    });

	table.getColumnModel()
	    .getColumn(0)
	    .setCellEditor(new DefaultCellEditor(tf));

	System.out.println("our cell editor: " + table.getCellEditor(0, 0));
	System.out.println("tbl cell editor: " + table.getCellEditor(0, 1));


	// JScrollPane scrollPane = new JScrollPane(table);
	// table.setFillsViewportHeight(true);

	JFrame frame = new JFrame ("Table Test");
        Container fpane = frame.getContentPane();
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        fpane.setLayout(new BorderLayout());

        frame.add("Center", table);
        frame.pack();
	frame.setSize(300,500);
        frame.setVisible(true);
    }


    public static void main(String argv[]) throws Exception {

	if (argv.length > 0 && argv[0].equals("--systemUI")) {
	    SwingUtilities.invokeAndWait(() -> {
		    try {
			UIManager.setLookAndFeel
			    (UIManager.getSystemLookAndFeelClassName());
			// without the following, on a Pop!_OS system, the
			// caret is very hard to see. It's probably any
			// Ubuntu system but only tested on Pop!_OS.
			UIManager.put("TextField.caretForeground",
				      UIManager.get("TextField.foreground"));
			UIManager.put("TextArea.caretForeground",
				      UIManager.get("TextArea.foreground"));
			UIManager.put("EditorPane.caretForeground",
				      UIManager.get("EditorPane.foreground"));
			UIManager.put("TextPane.caretForeground",
				      UIManager.get("TextPane.foreground"));
		    } catch(Exception e) {
			System.err.println("system look and feel failed");
			System.exit(1);
		    }
		});
	}
	

	SwingUtilities.invokeLater(() -> {setup();});

    }
}
