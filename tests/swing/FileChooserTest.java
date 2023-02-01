import java.io.File;

import javax.swing.*;
import org.bzdev.swing.*;


public class FileChooserTest {

    public static void doit() {

	ClearableFileChooser fc = new ClearableFileChooser(null, false);
	fc.setDialogTitle("ClearableFileChooser");
	fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	System.out.println("status = " + fc.showDialog(null));
	System.out.println("f = " + fc.getSelectedFile());;

    }


    public static void main(String argv[]) throws Exception {

	boolean systemUI = argv.length > 0 && argv[0].equals("--systemUI");
	if (systemUI) {
	    /*
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    */
	SwingUtilities.invokeAndWait(()->{
		DarkmodeMonitor.setSystemPLAF();
		DarkmodeMonitor.init();
	    });
	}


	SwingUtilities.invokeAndWait(()->{doit();});
	Thread.sleep(30000L);
	System.exit(0);
    }

    

}
