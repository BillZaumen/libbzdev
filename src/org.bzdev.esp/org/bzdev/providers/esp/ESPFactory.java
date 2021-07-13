package org.bzdev.providers.esp;
import java.util.Collections;
import java.util.List;
import javax.script.*;
import org.bzdev.util.ExpressionParser;

public class ESPFactory implements ScriptEngineFactory {

    @Override
    public String getEngineName() {return "BZDev ESP Engine";}

    @Override
    public String getEngineVersion() {return "1.0";}

    static final List<String> extensions = List.of("esp");

    @Override
    public List<String> getExtensions() {
	return Collections.unmodifiableList(extensions);
    }

    @Override
    public String getLanguageName() {return "ESP";}

    @Override
    public String getLanguageVersion() {return "1.0";}

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
	StringBuilder sb = new StringBuilder();
	sb.append(obj);
	sb.append(".");
	sb.append(m);
	sb.append("(");
	if (args.length > 0) {
	    sb.append(args[0]);
	    for (int i = 1; i < args.length; i++) {
		sb.append(",");
		sb.append(args[i]);
	    }
	}
	sb.append(")");
	return sb.toString();
    }

    private static final List<String> mimetypes =
	List.of("text/esp");
    @Override
    public List<String> getMimeTypes() {
	return Collections.unmodifiableList(mimetypes);
    }

    private static final List<String> names = List.of("ESP", "esp");

    @Override
    public List<String> getNames() {
	return Collections.unmodifiableList(names);
    }

    @Override
    public String getOutputStatement(String toDisplay) {
	StringBuilder sb = new StringBuilder();
	sb.append("global.getWriter().println(");
	sb.append(toDisplay);
	sb.append(")");
	return sb.toString();
    }

    @Override
    public Object getParameter(String key) {
	if (key.equals(ScriptEngine.ENGINE)) {
	    return getEngineName();
	} else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
	    return getEngineVersion();
	} else if (key.equals(ScriptEngine.LANGUAGE)) {
	    return getLanguageName();
	} else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
	    return getLanguageVersion();
	} else if (key.equals(ScriptEngine.NAME)) {
	    return getNames().get(0);
	} else if (key.equals("THREADING")) {
	    return "MULTITHREADED";
	} else {
	    return null;
	}
    }

    @Override
    public String getProgram(String... statements) {
	if (statements.length == 0) {
	    return "";
	} else {
	    StringBuilder sb = new StringBuilder();
	    sb.append(statements[0]);
	    for (int i = 1; i < statements.length; i++) {
		sb.append(";");
		sb.append(statements[i]);
	    }
	    return sb.toString();
	}
    }
    
    @Override
    public ScriptEngine getScriptEngine() {
	return new ESPEngine();
    }
}
