import org.bzdev.swing.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleConsoleTest3 {
    static SimpleConsole tc;
    public static void main(String argv[]) throws Exception {
	try {
	    SimpleConsole.ExitMode exitMode = SimpleConsole.ExitMode.NEVER;
	    boolean smBefore = false;
	    boolean smAfter = false;

	    if (argv.length > 0) {
		if (argv[0].equals("--ask")) {
		    exitMode = SimpleConsole.ExitMode.ASK;
		} else if (argv[0].equals("--always")) {
		    exitMode = SimpleConsole.ExitMode.ALWAYS;
		} else if (argv[0].equals("--smBefore")) {
		    smBefore = true;
		} else if (argv[0].equals("--smAfter")) {
		    smAfter = true;
		}
	    }
	    SimpleConsole.ExitMode exit = exitMode;

	    if (argv.length > 1) {
		if (argv[1].equals("--smBefore")) {
		    smBefore = true;
		} else if (argv[1].equals("--smAfter")) {
		    smAfter = true;
		}
	    }

	    if (smBefore) {
		Toolkit.getDefaultToolkit();
		System.setSecurityManager(new SecurityManager());
	    }

	    SwingUtilities.invokeAndWait(new Runnable () {
		    public void run() {
			tc = SimpleConsole.newFramedInstance
			    (800, 600, "ConsoleTest3", true, exit);
		    }
		});

	    if (smAfter) {
		System.setSecurityManager(new SecurityManager());
	    }

	    Thread.currentThread().sleep(2000);
	    tc.setBold(false);
	    tc.setItalic(false);
	    if (tc.isBold()) {
		throw new Exception("bold not expected");
	    }
	    if (tc.isItalic()) {
		throw new Exception("Italic not expected");
	    }
	    for (int i = 0; i < 100; i++) {
		if ((i % 20) == 0) {
		    tc.addSeparatorIfNeeded();
		}
		if ((i == 50)) {
		    tc.setTextForeground(Color.RED);
		    if (!tc.getTextForeground().equals(Color.RED)) {
			throw new Exception("red expected");
		    }
		}
		if (i == 70) {
		    tc.setBold(true);
		    if (!tc.isBold()) {
			throw new Exception("bold expected");
		    }
		}
		if (i == 90) {
		    tc.setItalic(true);
		    if (!tc.isItalic()) {
			throw new Exception("Italic expected");
		    }
		}
		if (i == 95) {
		    tc.setBold(false);
		    if (tc.isBold()) {
			throw new Exception("bold not expected");
		    }
		}
		String str = "line" +i;
		tc.append(str);
		tc.append('\n');

	    }
	    tc.setSeparatorColor(Color.BLACK);
	    tc.addSeparator();
	    tc.perform(c -> {
		    c.setTextForeground(Color.BLACK);
		    c.setBold(true);
		    c.append("Hello\n");
		});
	    tc.append("Goodbye\n");

	    Thread.currentThread().sleep(30000);
	    System.exit(0);

	} catch (Exception e) {
            e.printStackTrace();
	    System.exit(1);
        }
    }
}
