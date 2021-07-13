package testpkg;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import org.bzdev.scripting.*;
import javax.script.*;

@ObjectNamer(helperClass = "NHelper",
	     helperSuperclass = "org.bzdev.scripting.ScriptingContext",
	     objectClass = "ATestObject",
	     objectHelperClass = "OHelper")
public class ATestNamer extends NHelper implements ObjectNamerOps<ATestObject> {

    final ScriptEngineManager manager = new ScriptEngineManager();
    final ScriptEngine engine = manager.getEngineByName("javascript");
    final ScriptEngineFactory factory = engine.getFactory();
    final String languageName = factory.getLanguageName();
    final Bindings defaultBindings =
	engine.getBindings(ScriptContext.ENGINE_SCOPE);

    protected ScriptEngine doGetScriptEngine() {
	return engine;
    }

    protected Bindings doGetDefaultBindings() {
	return defaultBindings;
    }

    protected  String doGetScriptLanguage() {
	return languageName;
    }
}