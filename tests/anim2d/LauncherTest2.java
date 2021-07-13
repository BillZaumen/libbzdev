import java.io.*;
import java.nio.charset.Charset;
import org.bzdev.anim2d.*;
import org.bzdev.io.DetabReader;
import org.bzdev.util.*;
import org.bzdev.anim2d.Animation2D;
import org.bzdev.gio.ImageSequenceWriter;

public class LauncherTest2 {

    public static void main(String argv[]) throws Exception {
	// Animation2DLauncher launcher = new DesktopA2DLauncher();
	Animation2DLauncher launcher = (Animation2DLauncher)
	    Animation2DLauncher.newInstance("anim2d", "desktop");

	Reader r = new FileReader("LauncherTest2.yaml",
				  Charset.forName("UTF-8"));
	r = new DetabReader(r);
	try {
	    launcher.process("LauncherTest2", r, true);
	} catch (ExpressionParser.Exception e) {
	    System.out.println(e.getMessage() + ":");
	    System.out.println(e.getInput());
	    int offset = e.getOffset();
	    for (int i = 0; i < offset; i++) System.out.print(" ");
	    System.out.println("^");
	    Throwable cause = e.getCause();
	    if (cause != null) {
		System.out.println("... " + cause.getMessage());
		cause.printStackTrace();
	    }
	}
    }
}
