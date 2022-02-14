import javax.swing.*;
import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.text.*;

public class TextFieldMenuTest {

    static String value = "";
    static int port = 0;
    static String area = "";

    static boolean urlInUse = false;
    static String url = "";
    static String uarea = "";

    static void init() {
	JFrame frame = new JFrame("Menu Test");
        frame.setLayout(new FlowLayout());
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

	JMenuBar menubar = new JMenuBar();
	JMenu menu = new JMenu("menu");

	TextFieldMenuItem item1 = new TextFieldMenuItem("item1", 20,
							frame,
							"text field test",
							TextFieldButton.Mode.
							USE_OUTPUT_NO_STATE) {
		public String inputValue() {
		    // System.out.println("inputValue = " + value);
		    return value;
		}

		public void outputValue(String v) {
		    // System.out.println("outputValue = " + value);
		    value = v;
		}
	    };
	menu.add(item1);

	PortTextField vtf2 = new PortTextField() {
		public void onAccepted() throws Exception {
		    super.onAccepted();
		    port = getValue();
		    System.out.println("port = " + port);
		}
	    };
	vtf2.setAllowEmptyTextField(true);
	vtf2.setDefaultValue(0);
	// vtf.setErrorPolicy(VTextField.ErrorPolicy.USE_OLD_VALUE);

	JMenuItem item2 = new
	    VTextFieldMenuItem (vtf2, "item2", frame, "Port",
				"enter a port number", true);
	menu.add(item2);

	JMenuItem item3 = new
	    TextAreaMenuItem("item3", 10, 32, frame, "Text Area") {
		protected String inputValue() {
		    return area;
		}
		protected void outputValue(String s) {
		    area = s;
		    System.out.println("area = " + area);
		}
	    };

	menu.add(item3);

	JMenuItem item4 = new
	    URLTextAreaMenuItem("item4", 10, 32, frame, "URL Text Area",
				"Error") {
		protected void outputURL(String u) {
		    url = u;
		    System.out.println("url = " + url);
		}
		protected void outputURLInUse(boolean inUse) {
		    urlInUse = inUse;
		    System.out.println("urlInUse = " + urlInUse);
		}
		protected  void outputText(String v) {
		    uarea = v;
		    System.out.println("uarea = " + uarea);
		}

		protected String inputText() {return uarea;}

		protected String inputURL() {return url;}

		protected boolean inputURLInUse() {return urlInUse;}
	    };
	menu.add(item4);
	    
	
	menubar.add(menu);
	frame.setJMenuBar(menubar);
	frame.pack();
	frame.setVisible(true);
    }

    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    try {
			init();
		    } catch(Exception e) {
			e.printStackTrace();
		    }
		}
	    });
	// Thread.sleep(30000L);
	// System.exit(0);
    }
}
