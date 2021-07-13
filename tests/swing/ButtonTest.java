import javax.swing.*;
import org.bzdev.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ButtonTest {

    static String taValue = "";
    static String tfValue = "";

    static String utaText = "";
    static String utaURL = "";
    static boolean utaInUse = false;

    static void init() {
	JFrame frame = new JFrame("Button Test");
        frame.setLayout(new FlowLayout());
        frame.addWindowListener(new WindowAdapter () {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

	TextAreaButton taButton = new 
	    TextAreaButton("text area", 10, 40, frame,
			   "text area") {
		protected String inputValue() {
		    return taValue;
		}
		protected void outputValue(String value) {
		    taValue = value;
		}
	    };

	TextFieldButton tfButton = new
	    TextFieldButton("text field", 40, frame, "text field") {
		protected String inputValue() {
		    return tfValue;
		}
		protected void outputValue(String value) {
		    tfValue = value;
		}
	    };

	URLTextAreaButton urlButton = new
	    URLTextAreaButton("URL Button", 10, 40, frame, 
			      "ULR Text Area", "URL Text Area Error") {
		protected void outputURLInUse(boolean inUse) {
		    utaInUse = inUse;
		}
		protected void outputURL(String url) {
		    utaURL = url;
		}
		protected void outputText(String text) {
		    utaText = text;
		}
		protected boolean inputURLInUse() {
		    return utaInUse;
		}
		protected String inputText() {
		    return utaText;
		}
		protected String inputURL() {
		    return utaURL;
		}
	    };

	frame.add(taButton);
	frame.add(tfButton);
	frame.add(urlButton);

	frame.pack();

	frame.setVisible(true);
    }

    public static void main(String argv[]) {
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    init();
		}
	    });
	
    }
}