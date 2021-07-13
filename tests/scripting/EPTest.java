import java.io.*;
import javax.script.*;

public class EPTest {
    public static void main(String argv[]) throws Exception {
	ScriptEngineManager manager = new ScriptEngineManager();

	manager.put("org.bzdev.test", true);

	ScriptEngine engine = manager.getEngineByName("ESP");
	ScriptEngine engine1 = manager.getEngineByExtension("esp");
	ScriptEngine engine2 = manager.getEngineByMimeType("text/esp");

	if (engine == null) {
	    throw new NullPointerException("no engine");
	}
	
	ScriptEngineFactory factory = engine.getFactory();
	ScriptEngineFactory factory1 = engine1.getFactory();
	ScriptEngineFactory factory2 = engine2.getFactory();

	System.out.println("script engine name: " + factory.getEngineName());
	if (!factory.getEngineName().equals(factory1.getEngineName())) {
	    throw new Exception();
	}
	if (!factory.getEngineName().equals(factory2.getEngineName())) {
	    throw new Exception();
	}
	System.out.println("script engine version: "
			   + factory.getEngineVersion());
	if (!factory.getEngineVersion().equals(factory1.getEngineVersion())) {
	    throw new Exception();
	}
	if (!factory.getEngineVersion().equals(factory2.getEngineVersion())) {
	    throw new Exception();
	}
	System.out.println("script engine language name: "
			   + factory.getLanguageName());
	if (!factory.getLanguageName().equals(factory1.getLanguageName())) {
	    throw new Exception();
	}
	if (!factory.getLanguageName().equals(factory2.getLanguageName())) {
	    throw new Exception();
	}
	System.out.println("script engine language version: "
			   + factory.getLanguageVersion());
	if (!factory.getLanguageVersion()
	    .equals(factory1.getLanguageVersion())) {
	    throw new Exception();
	}
	if (!factory.getLanguageVersion()
	    .equals(factory2.getLanguageVersion())) {
	    throw new Exception();
	}
	System.out.println(factory.getMethodCallSyntax
			   ("foo", "bar", "1.0", "2.0"));

	System.out.println("media types:");
	for (String mt: factory.getMimeTypes()) {
	    System.out.println("    " + mt);
	}
	
	System.out.println("names:");
	for (String name: factory.getNames()) {
	    System.out.println("    " + name);
	}
	String parameters[] = {
	    ScriptEngine.ENGINE,
	    ScriptEngine.ENGINE_VERSION,
	    ScriptEngine.LANGUAGE,
	    ScriptEngine.LANGUAGE_VERSION,
	    ScriptEngine.NAME,
	    "THREADING"
	};

	System.out.println(factory.getOutputStatement("\"hello\""));
	
	for (String p: parameters) {
	    System.out.println(p + ": " + factory.getParameter(p));
	}
	System.out.println(factory.getProgram("var x = 10", "var y = 20",
					      "x*x + y*y"));

	System.out.println();
	System.out.println("------------------------");
	System.out.println();

	System.out.println("try running a script");
	ScriptContext context = engine.getContext();
	context.setReader(new InputStreamReader(System.in));
	context.setWriter(new PrintWriter(System.out, true));
	context.setErrorWriter(new PrintWriter(System.err, true));

	String filename = "eptest.esp";
	FileReader reader = new FileReader(filename);
	engine.put(ScriptEngine.FILENAME, filename);
	engine.eval(reader);
    }
}
