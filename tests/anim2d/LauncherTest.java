import java.io.*;
import java.nio.charset.Charset;
import org.bzdev.anim2d.*;
import org.bzdev.io.DetabReader;
import org.bzdev.util.*;
import org.bzdev.anim2d.Animation2D;

public class LauncherTest {

    public static void main(String argv[]) throws Exception {
	Animation2DLauncher launcher = new Animation2DLauncher();
	Reader r = new FileReader("LauncherTest.yaml",
				  Charset.forName("UTF-8"));
	r = new DetabReader(r);
	try {
	    launcher.process("LauncherTest.yaml", r, true);
	    Animation2D a2d = (Animation2D) launcher.get("a2d");
	    System.out.println("in launcher, animation = " + a2d);
	    for (AnimationObject2D obj: a2d.getObjectsByZorder()) {
		System.out.println(obj.getName());
		obj.printConfiguration("    ", System.out);
	    }

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
