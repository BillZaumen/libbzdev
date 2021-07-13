import java.io.*;
import java.nio.charset.Charset;
import org.bzdev.anim2d.*;
import org.bzdev.io.DetabReader;
import org.bzdev.util.*;
import org.bzdev.anim2d.Animation2D;
import org.bzdev.gio.ImageSequenceWriter;

public class LauncherTest1 {

    public static void main(String argv[]) throws Exception {
	Animation2DLauncher launcher = new Animation2DLauncher();


	Reader r = new FileReader("LauncherTest1.yaml",
				  Charset.forName("UTF-8"));
	r = new DetabReader(r);
	try {
	    launcher.set("isw", new ImageSequenceWriter("launcher.isq"));
	    launcher.process("LauncherTest1.yaml", r, true);

	} catch (ExpressionParser.Exception e) {
	    System.out.println(e.getMessage() + ":");
	    System.out.println(e.getInput());
	    int offset = e.getOffset();
	    for (int i = 0; i < offset; i++) System.out.print(" ");
	    System.out.println("^");
	    Throwable cause = e.getCause();
	    if (cause != null) {
		System.out.println("... " + cause.getMessage());
		// cause.printStackTrace();
	    }
	}
    }
}
