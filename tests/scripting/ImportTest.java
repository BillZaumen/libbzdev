import java.io.*;
import org.bzdev.scripting.*;

public class ImportTest {

    public static int getOne() {return 1;}

    public static void main(String argv[]) throws Exception {

	FileReader fr = new FileReader(argv[0]);

	String languageName = null;
	if (argv[0].endsWith(".js") || argv[0].endsWith(".es")) {
	    languageName = "ECMAScript";
	} else if (argv[0].endsWith(".esp")) {
	    languageName = "ESP";
	}

	ScriptingContext sc = new DefaultScriptingContext(languageName);
	ExtendedScriptingContext esc = new ExtendedScriptingContext(sc);
	esc.putScriptObject("scripting", esc);

	Object o = esc.evalScript(argv[0], fr);
	System.out.println("script returns " + o);
    }

}
