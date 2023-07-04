package org.bzdev.bin.scrunner;
import org.bzdev.scripting.*;
import org.bzdev.util.SafeFormatter;
import java.util.Set;
import java.util.ResourceBundle;

//@exbundle org.bzdev.bin.scrunner.lpack.SCRunner

public class ListScriptingLangs {

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.scrunner.lpack.SCRunner");

    static String localeString(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    public static void main(String argv[]) {
	// forbid recursive calls (e.g. when SCRunner loads classes from a
	// third party)
	CheckOncePerJVM.check();

	Set<String> languageNames = Scripting.getLanguageNameSet();
	if (argv.length == 0) {
	    for (String ln: languageNames) {
		System.out.println(ln);
	    }
	} else {
	    for (String ln: argv) {
		if (!languageNames.contains(ln)) {
		    /*
		    System.out.println("[Scripting language " + ln
				       + " not supported]");
		    */
		    System.out.println(localeString("notSupported", ln));
		    /*
		    if (argv.length == 1) {
			System.exit(1);
		    }
		    */
		    continue;
		}
		// System.out.println("Scripting language " + ln + ":");
		System.out.println(localeString("scriptingLang", ln));
		// System.out.print("   extensions = ");
		System.out.print("    " + localeString("extensions") + " = ");
		String delim = "";
		for (String ext: Scripting.getExtensionsByLanguageName(ln)){
		    System.out.print(delim + ext);
		    delim = ", ";
		}
		System.out.println();
		// System.out.print("   aliases = ");
		System.out.print("    " + localeString("aliases") + " = ");
		delim = "";
		int cnt = 0;
		for (String nm: Scripting.getAliasesByLanguageName(ln)) {
		    System.out.print(delim + nm);
		    cnt++;
		    if ((cnt % 5) == 0) {
			delim = ",\n              ";
		    } else {
			delim = ", ";
		    }
		}
		System.out.println();
	    }
	}
	System.exit(0);
    }
}
