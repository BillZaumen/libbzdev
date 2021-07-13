import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import org.bzdev.scripting.*;
import javax.script.*;

@ObjectNamer(helperClass = "NHelper",
	     helperSuperclass = "org.bzdev.scripting.ScriptingContext",
	     objectClass = "ATestObject",
	     objectHelperClass = "OHelper")
public class ATestNamer extends NHelper implements ObjectNamerOps<ATestObject> {

    static String lang = "ECMAScript";
    public static void setLanguage(String l) {
	lang = l;
    }


    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByName(lang);
    final ScriptEngineFactory factory = engine.getFactory();
    final String languageName = factory.getLanguageName();
    final Bindings defaultBindings =
	engine.getBindings(ScriptContext.ENGINE_SCOPE);

    protected ScriptEngine doGetScriptEngine() {
	return engine;
    }

    protected javax.script.Bindings doGetDefaultBindings() {
	return defaultBindings;
    }

    protected  String doGetScriptLanguage() {
	return languageName;
    }

    public void quickTest() {
	if (doGetDefaultBindings() == null) {
	    System.out.println("no default bindings");
	}
    }
}
