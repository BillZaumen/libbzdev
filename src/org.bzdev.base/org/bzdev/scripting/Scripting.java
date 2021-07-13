package org.bzdev.scripting;
import java.util.*;
import javax.script.*;
import org.bzdev.util.SafeFormatter;

/**
 * Provide information about the scripting environment.
 * This class provides methods to conveniently look up
 * the official scripting language name, various aliases
 * for those names, and the corresponding file-name
 * extensions (without the '.'). Sets of names and extensions
 * are also provided.
 * <P>
 * The names are determined when the class is loaded and initialized.
 */
public class Scripting {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.scripting.lpack.Scripting");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    static class Info {
	String lname;
	List<String> extensions = new LinkedList<String>();
	List<String> aliases = new LinkedList<String>();
    }

    // by short name (alias)
    static Map<String,Info> amap = new HashMap<String,Info>();
    // by extension
    static Map<String,Info> emap = new HashMap<String,Info>();
    // by language name (lname)
    static Map<String,Info> lmap = new HashMap<String,Info>();

    static {
	ScriptEngineManager manager = new ScriptEngineManager();
	ScriptEngineFactory factory = null;

	for (ScriptEngineFactory f: manager.getEngineFactories()) {
	    Info info = new Info();
	    info.lname = f.getLanguageName();
	    lmap.put(info.lname, info);
	    List<String> extensions = f.getExtensions();
	    if (extensions != null && !extensions.isEmpty()) {
		for (String ext: extensions) {
		    info.extensions.add(ext);
		    emap.put(ext, info);
		}
		// Fix For RFC 4329, which suggests "es" for
		// ECMAScript (media type application/ecmascript).
		// Included because Ubuntu recognizes ".es" files
		// as having the application/ecmascript media type.
		if (info.lname.equals("ECMAScript")
		    && emap.get("es") == null) {
		    info.extensions.add("es");
		    emap.put("es", info);
		}
	    }
	    for (String shortname: f.getNames()) {
		info.aliases.add(shortname);
		amap.put(shortname, info);
	    }
	}
	manager = null;
	factory = null;
    }


    /**
     * Get a set of the language names for the scripting languages
     * supported on the system.  The set returned cannot be modified.
     * @return the set of language names
     */
    public static Set<String> getLanguageNameSet() {
	return Collections.unmodifiableSet(lmap.keySet());
    }

    /**
     * Get a set of all the file-name extensions used by scripting
     * languages.   The set returned cannot be modified.
     * @return the set of file-name extensions
     */
    public static Set<String> getExtensionSet() {
	return Collections.unmodifiableSet(emap.keySet());
    }

    
    /**
     * Get a set of the aliases for the language names for each scripting
     * language supported on the system.
     * The set returned cannot be modified.
     * @return the set of aliases
     */
    public static Set<String> getAliasSet() {
	return Collections.unmodifiableSet(amap.keySet());
    }


    /**
     * Get a set of file-name extensions for a given language name.
     * The set returned cannot be modified.
     * @param name the language name
     * @return a set of file-name extensions
     */
    public static List<String> getExtensionsByLanguageName(String name) {
	Info info  = lmap.get(name);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.extensions);
    }

    /**
     * Get a set of file-name extensions given one extension.
     * The set returned cannot be modified.
     * @param extension a file name extension for a scripting language
     * @return a set of file-name extensions
     */
    public static List<String> getExtensionsByExtension(String extension) {
	Info info  = emap.get(extension);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.extensions);

    }

    /**
     * Get a set of file-name extensions for a given language-name alias.
     * The set returned cannot be modified.
     * @param alias the language-name alias
     * @return a set of file-name extensions
     */
    public static List<String> getExtensionsByAlias(String alias) {
	Info info  = amap.get(alias);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.extensions);
    }

    /**
     * Get a set of aliases for a given language name.
     * The set returned cannot be modified.
     * @param name the language name
     * @return a set of aliases for the specified language
     */
    public static List<String> getAliasesByLanguageName(String name) {
	Info info  = lmap.get(name);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.aliases);
    }

    /**
     * Get a set of aliases for a scripting language with a specified file-name
     * extension.
     * The set returned cannot be modified.
     * @param extension the file name extension
     * @return a set of aliases for the specified language
     */
    public static List<String> getAliasesByExtension(String extension) {
	Info info  = emap.get(extension);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.aliases);

    }

    /**
     * Get a set of aliases for a scripting language given one of its aliases.
     * The set returned cannot be modified.
     * @param alias an alias
     * @return a set of aliases for the language matching the specified alias
     */
    public static List<String> getAliasesByAlias(String alias) {
	Info info  = amap.get(alias);
	return (info == null)? Collections.emptyList():
	    Collections.unmodifiableList(info.aliases);
    }

    /**
     * Get the scripting language name given a file-name extension
     * @param extension the file-name extension
     * @return the scripting-language name
     */
    public static String getLanguageNameByExtension(String extension) {
	Info info  = emap.get(extension);
	return (info == null)? null: info.lname;
    }

    /**
     * Get the scripting language name given an alias
     * @param alias the alias for the scripting language
     * @return the scripting-language name matching the alias
     */
    public static String getLanguageNameByAlias(String alias) {
	Info info  = amap.get(alias);
	return (info == null)? null: info.lname;
    }

    /**
     * Determine if a language is supported given the language name.
     * @param languageName the official name for a scripting language
     * @return true if the language is supported ; false otherwise
     */
    public static boolean supportsLanguage(String languageName) {
	return lmap.containsKey(languageName);
    }
}

//  LocalWords:  lname ecmascript languageName
