import java.awt.Color;
import javax.swing.*;
import org.bzdev.swing.*;


public class ColorChooserTest {

    public static void doit() {

	JColorChooser cc = new JColorChooser();
	CSSColorChooserPanel csscc = new CSSColorChooserPanel();

	cc.addChooserPanel(csscc);

	JDialog ccd = JColorChooser.createDialog
	    (null, "Color Chooser", true, cc,
	     (e) -> {
		Color c = cc.getColor();
		System.out.println("color = " + c
				   + ", alpha = " + c.getAlpha());
		System.exit(0);
	     }, (e) -> {
		System.out.println("dialog canceled");
		System.exit(0);
	    });
	ccd.setVisible(true);
    }


    public static void main(String argv[]) throws Exception {
	SwingUtilities.invokeAndWait(()->{doit();});
    }

    

}
