package org.bzdev.bin.lsnof;

import org.bzdev.obnaming.*;
import org.bzdev.util.*;
import org.bzdev.io.DirectoryAccessor;
import org.bzdev.io.AppendableWriter;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.net.URLClassLoaderOps;
import org.bzdev.net.URLPathParser;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.CopyUtilities;
import org.bzdev.scripting.Scripting;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.InvocationTargetException;
// import java.lang.reflect.Method;

//@exbundle org.bzdev.bin.lsnof.lpack.FactoryPrinter

/**
 * Program and static methods to print information about subclasses of
 * NamedObjectFactory.
 * When used as a program, the command-line arguments are the fully qualified
 * class names of the factories to be listed.
 */
public class FactoryPrinter {

    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.lsnof.lpack.FactoryPrinter");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    private static final String pathSeparator =
	System.getProperty("path.separator");

    static final String fileSep = System.getProperty("file.separator");


    // private static NamedObjFactoryLoader loader;

    static private boolean stackTrace = false;

    private static URL ourCodeBase;
    private static String ourCodebaseDir;
    static {
	try {
	    ourCodeBase = URLPathParser.class.getProtectionDomain()
		.getCodeSource().getLocation()
		.toURI().toURL();
	    ourCodebaseDir =
		(new File(URLPathParser.class.getProtectionDomain()
			  .getCodeSource().getLocation()
			  .toURI())).getCanonicalFile()
		.getParentFile().getCanonicalPath();
	} catch (Exception e) {
	    System.err.println(errorMsg("missingOwnCodebase"));
	    System.exit(1);
	}
    }

    private static void readConfigFiles(String languageName, String fileName) {
	File file = new File (fileName);
	if (!file.exists()) {
	    return;
	}
	if (!file.isFile()) {
	    System.err.println(errorMsg("configFileName", fileName));
	    /*
	    System.err.println("lsnof: \"" +fileName
			       + "\" is not a (configuration) file");
	    */
	    System.exit(1);
	}
	if (!file.canRead()) {
	    System.err.println(errorMsg("readable", fileName));
	    /*
	    System.err.println("lsnof: configuration file \"" +fileName
			       +"\" is not readable");
	    */
	    System.exit(1);
	}
	try {
	    LineNumberReader reader = 
		new LineNumberReader(new FileReader(file));
	    try {
		int mode = 0;
		boolean langsearch = true;
		boolean langsection = false;
		boolean error = false;
		for (String line = reader.readLine(); line != null;
		     line = reader.readLine()) {
		    line = line.trim();
		    if (line.length() == 0) continue;
		    if (line.startsWith("#")) continue;
		    if (line.equals("%end")) { 
			mode = 0;
			continue;
		    } else if (line.equals("%defs")) {
			mode = 1;
			langsearch = true;
			langsection = false;
			continue;
		    } else if (line.equals("%classpath.components")) {
			mode = 2;
			langsearch = true;
			langsection = false;
			continue;
		    } else if (line.startsWith("%java")) {
			mode = 0;
			continue;
		    }
		    switch (mode) {
		    case 1:
			continue;
		    case 2: // classpath components
			if (line.startsWith("%lang")) {
			    String language = line.substring(5).trim();
			    
			    if (languageName == null) {
				mode = 3;
				langsection = false;
			    } else if (languageName.equals(language)) {
				langsection = true;
			    } else  {
				if (langsection) mode = 3;
				langsection = false;
			    }
			    langsearch = false;
			    continue;
			} else if (line.startsWith("%")) {
			    String lineNo = "" + reader.getLineNumber();
			    System.err.println
				(errorMsg("syntaxEnd", fileName, lineNo));
			    /*
			    System.err.println("\"" + fileName +"\", line "
					       + reader.getLineNumber()
					       + ": syntax error - "
					       + "%end expected");
			    */
			    error = true;
			    continue;
			}
			if (langsearch || langsection) {
			    if (line.startsWith("~/")) {
				line = System.getProperty("user.home")
				    + line.substring(1);
			    } else if (line.equals("~")) {
				line = System.getProperty("user.home");
			    } else if (line.startsWith("~~")) {
				line = line.substring(1);
			    }
			    try {
				URL url;
				if (line.startsWith("file:")) {
				    url = new URL(line);
				} else {
				    url = (new File(line)).toURI().toURL();
				}
				URLClassLoaderOps.addURL(url);
			    } catch (Exception e) {
				String lineNo = "" + reader.getLineNumber();
				System.err.println
				    (errorMsg("syntaxURL", fileName, lineNo));
				System.exit(1);
			    }
			}
		    }
		}
		if (error)
		    throw new Exception(errorMsg("terminating"));
			/*("Terminating due to errors");*/
	    } catch (Exception e) {
		String lineNo = "" + reader.getLineNumber();
		String msg = e.getMessage();
		System.err.println
		    (errorMsg("exception3", fileName, lineNo, msg));
		/*
		System.err.println("\"" +fileName +"\", line " 
				   +reader.getLineNumber()
				   +": " +e.getMessage());
		*/
		System.exit(1);
	    }
	    reader.close();
	} catch (Exception er) {
	    System.err.println(er.getMessage());
	    System.exit(1);
	}
    }

    static private void readConfigFiles(String languageName) {
	String sysConfigFileName = System.getProperty("scrunner.sysconf");
	String usrConfigFileName = System.getProperty("scrunner.usrconf");
	// Properties properties = (defs == null)? null: new Properties();
	if (sysConfigFileName != null) {
	    readConfigFiles(languageName, sysConfigFileName);
	}
	if (usrConfigFileName != null) {
	    readConfigFiles(languageName, usrConfigFileName);
	}
    }

    static private void setupColors(TemplateProcessor.KeyMap keymap,
				    boolean darkmode)
    {
	if (darkmode) {
	    keymap.put("frameBackground", "#000000");
	    keymap.put("frameColor", "#2E1319");
	    keymap.put("menuBackground", "#101010");
	    keymap.put("menuColor", "#FFFFFF");
	    keymap.put("menuLinkColor", "#9EADC0");
	    keymap.put("contentBackground", "#101010");
	    keymap.put("contentColor", "#FFFFFF");
	    keymap.put("contentLinkColor", "#9EADC0");
	    keymap.put("contentVisitedColor", "#7E8DA0");
	    keymap.put("otherBackground", "#101010");
	    keymap.put("otherColor", "#FFFFFF");
	    keymap.put("otherLinkColor", "#9EADC0");
	    keymap.put("otherVisitedColor", "#7E8DA0");
	} else {
	    keymap.put("frameBackground", "#4D7A97");
	    keymap.put("frameColor", "#000077");
	    keymap.put("menuBackground", "#f8f8f8");
	    keymap.put("menuColor", "#000000");
	    keymap.put("menuLinkColor", "#4A6782");
	    keymap.put("contentBackground", "#ffffff");
	    keymap.put("contentColor", "#000000");
	    keymap.put("contentLinkColor", "#4A6782");
	    keymap.put("contentVistedColor", "#1F389C");
	    keymap.put("otherBackground", "#ffffff");
	    keymap.put("otherColor", "#000000");
	    keymap.put("otherLinkColor", "#4A6782");
	    keymap.put("otherVistedColor", "#1F389C");
	}
    }

    /**
     * Print a listing of factory information.
     * @param templateReader a Reader configured to read a template
     * @param darkmode true if dark mode is in effect; false otherwise
     * @param ap an Appendable used for output.
     * @param argv the class names of the factories that will be listed
     */
    static void printFactories(Reader templateReader, boolean darkmode,
				      Appendable ap, String[] argv) 
	throws Exception
    {
	printFactories(templateReader, darkmode, ap, argv, 0);
    }

    /**
     * Print a listing of factory information given an offset.
     * @param templateReader a Reader configured to read a template
     * @param darkmode true if dark mode is in effect; false otherwise
     * @param ap an Appendable used for output.
     * @param argv the class names of the factories that will be listed
     * @param index the starting index to use for the array passed as the
     *        second argument
     */
    static void printFactories(Reader templateReader, boolean darkmode,
				      Appendable ap, String[] argv, int index) 
	throws Exception
    {

	int ind = 0;
	TemplateProcessor.KeyMap keymap =
	    new TemplateProcessor.KeyMap(argv.length - index);
	TemplateProcessor.KeyMapList keymaplist =
	    new TemplateProcessor.KeyMapList();
	LinkedList<NamedObjectFactory> list =
	    new LinkedList<NamedObjectFactory>();
	for (String className: argv) {
	    if (ind < index) {
		ind++;
		continue;
	    }
	    if (className.contains("*") || className.contains("|")
		|| className.contains("(")) {
		Set<NamedObjectFactory> set =
		    NamedObjectFactory.getListedFactories(className);
		list.addAll(set);
	    } else {
		try {
		    Object object = null;
		    Class factoryClass = Class.forName(className);
		    Constructor<?>[] constructors =
			factoryClass.getConstructors();
		    for (Constructor<?> constructor: constructors) {
			Class<?>[] fp = constructor.getParameterTypes();
			if (fp.length != 1) continue;
			if (ObjectNamerOps.class
			    .isAssignableFrom(fp[0])) {
			    Object[] args = new Object[1];
			    object = constructor.newInstance(args);
			    break;
			}
		    }
		    if (object == null) {
			throw new Exception(errorMsg("noConstr"));
			    /*("could not find appropriate constructor");*/
		    }
		    if (object instanceof NamedObjectFactory) {
			NamedObjectFactory factory =
			    (NamedObjectFactory) object;
			list.add(factory);
		    }
		} catch (Throwable e) {
		    String msg = e.getMessage();
		    System.err.println(errorMsg("ignored", className, msg));
		    Throwable ee = e.getCause();
		    if (ee != null) {
			msg = ee.getMessage();
			System.err.println(errorMsg("moreIgnored", msg));
			if (stackTrace) {
			    ee.printStackTrace(System.err);
			}
		    }
		}
	    }
	}
	for (NamedObjectFactory factory: list) {
		keymaplist.add(factory.getTemplateKeyMap());
	}
	keymap.put("factories", keymaplist);
	setupColors(keymap, darkmode);
	printFactories(templateReader, ap, keymap);
    }

    /**
     * Get the charset parameter of a MIME content type.
     * @param contentType the content type string
     * @return the charset; "UTF-8" as a default
     */
    private static String getCharset(String contentType) {
	for (String component: contentType.split(";")) {
	    String comp = component.trim();
	    String[] pair = comp.split("=");
	    if (pair.length != 2) continue;
	    if (pair[0].trim().equalsIgnoreCase("charset")) {
		return pair[1].trim();
	    }
	}
	return "UTF-8";
    }

    private  static final String UNP = "[unnamed package]";
    private static void fixKeymap(TemplateProcessor.KeyMap keymap) {
	Object kml  = keymap.get("factories");
	if (kml instanceof TemplateProcessor.KeyMapList) {
	    TemplateProcessor.KeyMapList keymaplist
		= (TemplateProcessor.KeyMapList) kml;
	    String lastpkg = "";
	    for (TemplateProcessor.KeyMap km: keymaplist) {
		Object obj = km.get("factoryPackage");
		    
		String pkg;
		if (obj == null) {
		    pkg = UNP;
		} else if (obj instanceof String) {
		    pkg = (String) obj;
		} else {
		    pkg = null;
		}
		if (pkg != null && !pkg.equals(lastpkg)) {
		    km.put("nextPackageEntry",
			   "<li>&nbsp;\n<li><B>" + pkg + "</B>\n");
		    lastpkg = pkg;
		}
	    }
	}
    }
    
    /**
     * Print factory information.
     * @param templateReader a Reader configured to read a template
     * @param ap an Appendable used for output.
     * @param keymap the KeyMap used by the template processor
     */
    static void printFactories(Reader templateReader,
				      Appendable ap,
				      TemplateProcessor.KeyMap keymap)
	throws Exception
    {
	Writer writer = new AppendableWriter(ap);

	fixKeymap(keymap);

	// URL tpurl = ClassLoader.getSystemResource(templateResource);
	TemplateProcessor tp = new TemplateProcessor(keymap);
	tp.processTemplate(templateReader, writer);
	writer.flush();
    }

    private static boolean mainCalled = false;

    private static final URL[] noURLs = new URL[0];

    /**
     * Main program.
     * Print factory information to standard output.  The arguments
     * are the fully-qualified factory class names.  All factories must be
     * instances of org.bzdev.obnaming.NamedObjectFactory.
     * @param argv the command-line arguments
     */

    public static void main(String argv[]) {
	if (mainCalled) {
	    // make sure a factory can't try to take advantage of
	    // any permissions a user may have granted to our protection
	    // domain.
	    throw new RuntimeException(errorMsg("recursive"));
	} else {
	    mainCalled = true;
	}

	org.bzdev.protocols.Handlers.enable();

	try {
	    //	    AllowOnce.check();
	    int index = 0;
	    String templateName = "plainTemplate";
	    DirectoryAccessor da = null;
	    InputStream overviewStream = null;
	    Reader reader = null;
	    String charset = null;
	    String pattern = null;
	    boolean notTrusted = true;
	    String languageName = null;
	    boolean listCodeBase = false;
	    boolean darkmode = false;
	    URL[] urls = noURLs;
	    String target = null;
	    String menuFile = null;
	    int menuWidth = errorMsg("factories").length();

	    while (index < argv.length && argv[index].startsWith("-")) {
		if (argv[index].equals("--")) {
		    index++;
		    break;
		} else if (argv[index].equals("-d")) {
		    index++;
		    String fname = argv[index];
		    File outfile = new File(fname);
		    NamedObjectFactory.addJDoc(outfile.toURI().toURL());
		    if (!fname.endsWith(fileSep)) {
			fname = fname + fileSep;
		    }
		    fname = fname + "factories-api";
		    da = new DirectoryAccessor(fname);
		} else if (argv[index].equals("--baseURL")) {
		    index++;
		    URL[] baseArray = URLPathParser.getURLs(null, argv[index],
							    null,
							    null);
		    if (baseArray.length != 1) {
			System.err.println(errorMsg("badBase", argv[index]));
			System.exit(1);
		    }
		    NamedObjectFactory.setDocAPIBase(baseArray[0]);
		    /*
		} else if (argv[index].equals("--trusted")) {
		    notTrusted = false;
		    */
		} else if (argv[index].equals("--stackTrace")) {
		    stackTrace = true;
		} else if (argv[index].equals("-L")) {
		    index++;
		    languageName = argv[index];
		    if (!Scripting.supportsLanguage(languageName)) {
			String ln =
			    Scripting.getLanguageNameByAlias(languageName);
			if (ln == null) {
			    String ln1 = languageName;
			    String msg =
				errorMsg("badScriptingLanguageName", ln1);
			    System.err.println(msg);
			    System.exit(1);
			} else {
			    languageName = ln;
			}
		    }
		} else if (argv[index].equals("--charset")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    charset = argv[index];
		} else if (argv[index].equals("--darkmode")) {
		    darkmode = true;
		} else if (argv[index].equals("--html")) {
		    templateName = "htmlTemplate";
		} else if (argv[index].equals("--menu")) {
		    templateName = "menuFrameTemplate";
		} else if (argv[index].equals("--list")) {
		    templateName = "listTemplate";
		} else if (argv[index].equals("--definingClass")) {
		    templateName = "defTemplate";
		} else if (argv[index].equals("--definingClassHTML")) {
		    templateName = "defHTMLTemplate";
		} else if (argv[index].equals("--templateResource")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    InputStream is =
			ClassLoader.getSystemResourceAsStream(argv[index]);
		    if (is == null) {
			FactoryPrinter.class.getResourceAsStream
			    ("/" + argv[index]);
		    }
		    reader = new InputStreamReader(is, "UTF-8");
		} else if (argv[index].equals("--templateFile")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    InputStream is = new FileInputStream(argv[index]);
		    reader = new InputStreamReader(is, ((charset == null)?
							"UTF-8": charset));
		} else if (argv[index].equals("--templateURL")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    URL url = new URL(argv[index]);
		    URLConnection connection = url.openConnection();
		    String contentType = connection.getContentType();
		    if (contentType != null) {
			contentType.trim();
			if (!contentType.toLowerCase(Locale.ENGLISH)
			    .startsWith("text")) {
			    System.err.println
				(errorMsg("notText", argv[index-1]));
			    /*
			    System.err.println("URL does not point to a text "
					       + "object");
			    */
			    System.exit(1);
			}
		    }
		    if (charset == null) {
			charset = (contentType == null)? "UTF-8":
			    getCharset(contentType);
		    }
		    InputStream is = connection.getInputStream();
		    reader = new InputStreamReader(is, charset);
		} else if (argv[index].equals("--overview")) {
		    index++;
		    URL[] overviewURLs =
			URLPathParser.getURLs(null, argv[index], null, null);
		    if (overviewURLs == null) overviewURLs = noURLs;
		    switch(overviewURLs.length) {
		    case 0:
			break;
		    case 1:
			URL overviewURL = overviewURLs[0];
			try {
			    overviewStream = overviewURL.openStream();
			} catch (IOException eio) {
			    String msg =
			      errorMsg("readURLFailed", overviewURL.toString());
			    System.err.println(msg);
			    System.exit(1);
			}
			break;
		    default:
			System.err.println
			    (errorMsg("illegalArg", argv[index]));
			System.exit(1);
		    }
		} else if (argv[index].equals("--codebase") ||
			   argv[index].equals("--classpathCodebase")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    if (listCodeBase) {
			urls = URLPathParser.getURLs(null, argv[index],
						     ourCodebaseDir,
						     System.err);
			if (urls == null) urls = noURLs;
			for (URL url: urls) {
			    System.out.println(url.toString());
			}
		    }
		} else if (argv[index].equals("-p")
			   || argv[index].equals("--module-path")) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[index-1]));
			/*
			System.err.println("missing argument for "
					   + argv[index-1]);
			*/
			System.exit(1);
		    }
		    if (listCodeBase) {
			for (String comp:
				 argv[index].trim().split(pathSeparator)) {
			    urls = URLPathParser.getURLs(null, comp,
							 ourCodebaseDir,
							 System.err);
			    if (urls == null) urls = noURLs;
			    for (URL url: urls) {
				System.out.println(url.toString());
			    }
			}
		    }
		} else if (argv[index].equals("--link")) {
		    index++;
		    URL[] apiURLs = URLPathParser.getURLs(null, argv[index],
						      null,
						      null);
		    if (apiURLs == null) apiURLs = noURLs;
		    for  (URL url: apiURLs) {
			NamedObjectFactory.addJDoc(url);
		    }
		} else if (argv[index].equals("--link-offline")) {
		    index++;
		    URL[] apiURLs = URLPathParser.getURLs(null, argv[index],
						      null,
						      null);
		    if (apiURLs == null) apiURLs = noURLs;
		    index++;
		    URL[] offlineURLs = URLPathParser.getURLs(null, argv[index],
						      null,
						      null);
		    if (offlineURLs == null) offlineURLs = noURLs;
		    if (apiURLs.length != offlineURLs.length) {
			System.err.println(errorMsg("urlLengths"));
			System.exit(1);
		    }
		    for (int i = 0; i < apiURLs.length; i++) {
			NamedObjectFactory.addJDoc(apiURLs[i], offlineURLs[i]);
		    }
		} else if (argv[index].equals("--target")) {
		    index++;
		    target = argv[index];
		    NamedObjectFactory.setTarget(argv[index]);
		} else if (argv[index].equals("--menuFile")) {
		    index++;
		    menuFile = argv[index];
		} else if (argv[index].equals("--listCodeBase")) {
		    listCodeBase = true;
		    try {
			// Cannot get the protection domain for
			// java.lang.Object (the call returns null).
			// We can get it for the BZDev library.
			System.out
			    .println(org.bzdev.obnaming.NamedObjectFactory.class
				     .getProtectionDomain()
				     .getCodeSource()
				     .getLocation());
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		} else {
		    System.err.println
			(errorMsg("illegalArg", argv[index]));
		    /*
		    System.err.println("illegal command-line argument: "
				       + argv[index]);
		    */
		    System.exit(1);
		}
		index++;
	    }

	    if (listCodeBase) System.exit(0);
	    // null implies ignore language-specific parts of the
	    // configuration file.
	    // readConfigFiles(languageName);
	    // URLClassLoaderOps.close();
	    
	    String overviewResource = null;
	    CharArrayReader menuTemplateReader = null;
	    CharArrayReader docTemplateReader = null;
	    String framesetResource = null;

	    if (da != null) {
		String tResource =
		    "org/bzdev/bin/lsnof/FactoryPrinterTemplates";
		ResourceBundle templateBundle =
		    ResourceBundle.getBundle(tResource);
		overviewResource = templateBundle.getString("overview");
		framesetResource = templateBundle.getString("frameset");

		String menuResource =
		    templateBundle.getString("menuFrameTemplate");
		String htmlResource =
		    templateBundle.getString("htmlTemplate");
		InputStream is =
		    ClassLoader.getSystemResourceAsStream(menuResource);
		if (is == null) {
		    is = FactoryPrinter.class.getResourceAsStream
			("/" + menuResource);
		}
		StringBuilder sb = new StringBuilder(512);
		try {
		    CopyUtilities.copyStream(is, sb, Charset.forName("UTF-8"));
		} catch (Exception e) {
		    throw new UnexpectedExceptionError();
		}
		is.close();
		menuTemplateReader =
		    new CharArrayReader(sb.toString().toCharArray());
		is = ClassLoader.getSystemResourceAsStream(htmlResource);
		if (is == null) {
		    is = FactoryPrinter.class.getResourceAsStream
			("/" + htmlResource);
		}
		sb = new StringBuilder(1>>15);
		try {
		    CopyUtilities.copyStream(is, sb, Charset.forName("UTF-8"));
		} catch (Exception e) {
		    throw new UnexpectedExceptionError();
		}
		is.close();
		docTemplateReader =
		    new CharArrayReader(sb.toString().toCharArray());
	    } else if (reader == null) {
		String tResource =
		    "org/bzdev/bin/lsnof/FactoryPrinterTemplates";
		ResourceBundle templateBundle =
		    ResourceBundle.getBundle(tResource);
		String templateResource =
		    templateBundle.getString(templateName);
		InputStream is =
		    ClassLoader.getSystemResourceAsStream(templateResource);
		if (is == null) {
		    is = FactoryPrinter.class.getResourceAsStream
			("/" + templateResource);
		}
		if (charset == null) {
		    charset = "UTF-8";
		}
		reader = new InputStreamReader(is, charset);
	    }

	    if (System.getProperty("java.security.policy") == null) {
		try {
		    File pf = new File(FactoryPrinter.class
				       .getProtectionDomain()
				       .getCodeSource().getLocation().toURI());
		    pf = pf.getParentFile();
		    System.setProperty("java.security.policy",
				       new File(pf, "libbzdev.policy")
				       .getCanonicalPath());
		} catch (Exception eio) {
		    System.err.println(errorMsg("noPolicy"));
		    /*
		    System.err.println("could not find libbzdev policy file");
		    */
		    System.exit(1);
		}
	    }

	    /*
	    if (notTrusted) {
		try {
		    System.setSecurityManager(new SecurityManager());
		} catch (UnsupportedOperationException eu){}
	    }
	    */
	    if (da != null) {
		Set<NamedObjectFactory> fset = new TreeSet<>
		    (new Comparator<NamedObjectFactory>() {
			public int compare(NamedObjectFactory f1,
					   NamedObjectFactory f2) {
			    Class<?> c1 = f1.getClass();
			    Class<?> c2 = f2.getClass();
			    String n1 = c1.getName();
			    String n2 = c2.getName();
			    Package p1 = c1.getPackage();
			    Package p2 = c2.getPackage();
			    if (p1 == null && p2 != null) {
				return -1;
			    } else if (p1 != null && p2 == null) {
				return 1;
			    } else {
				return n1.compareTo(n2);
			    }
			}
		    });
		while (index < argv.length) {
		    fset.addAll(NamedObjectFactory
				.getListedFactories(argv[index++]));
		}
		for (NamedObjectFactory f: fset) {
		    Class<?> c = f.getClass();
		    String pname = c.getPackage().getName();
		    String cname = c.getSimpleName();
		    int clen = cname.length();
		    if (cname.endsWith("Factory")) {
			clen -= 8;
		    }
		    int len = Math.max(pname.length(), clen);
		    if (menuWidth < len) {
			menuWidth = len;
		    }
		}
		NamedObjectFactory.setTarget("factories");
		TemplateProcessor.KeyMap keymap =
		    NamedObjectFactory.getTemplateKeyMapForFactories(fset);
		keymap.put("menuWidth", "" + menuWidth);
		OutputStream os = da.getOutputStream("menu.html");
		Writer writer = new OutputStreamWriter(os, "UTF-8");
		setupColors(keymap, darkmode);
		printFactories(menuTemplateReader, writer, keymap);
		writer.flush();
		writer.close();
		NamedObjectFactory.setTarget(null);
		for (NamedObjectFactory factory: fset) {
		    Class fclass = factory.getClass();
		    Package pkg = fclass.getPackage();
		    String p = (pkg == null)? "": pkg.getName();
		    if (p.endsWith(".")) p = p.substring(0, p.length()-1);
		    String[] components = p.split("[.]");
		    if ((p.length() > 0) && !p.endsWith(".")) p = p + ".";
		    String cname = fclass.getName().substring(p.length());
		    cname.replaceAll("[$]", ".");
		    DirectoryAccessor da2 = da;
		    for (String component: components) {
			da2 = da2.addDirectory(component);
		    }
		    os = da2.getOutputStream(cname + ".html");
		    writer = new OutputStreamWriter(os, "UTF-8");
		    keymap = NamedObjectFactory.getTemplateKeyMapForFactories
			(p + cname);
		    setupColors(keymap, darkmode);
		    printFactories(docTemplateReader, writer, keymap);
		    docTemplateReader.reset();
		    writer.flush();
		    writer.close();
		}
		os = da.getOutputStream("overview.html");
		writer = new OutputStreamWriter(os, "UTF-8");
		if (overviewStream == null) {
		    overviewStream = FactoryPrinter.class.getResourceAsStream
			(overviewResource.startsWith("/")? overviewResource:
			 "/" + overviewResource);
		}
		TemplateProcessor.KeyMap keymap2 =
			new TemplateProcessor.KeyMap();
		keymap.put("menuWidth", "" + menuWidth);
		setupColors(keymap2, darkmode);
		TemplateProcessor tp2 = new TemplateProcessor(keymap2);
		tp2.processTemplate(new InputStreamReader(overviewStream,
							  "UTF-8"),
				    writer);
		writer.flush();
		writer.close();
		os = da.getOutputStream("index.html");
		writer = new OutputStreamWriter(os, "UTF-8");
		InputStream fsis = FactoryPrinter.class.getResourceAsStream
		    (framesetResource.startsWith("/")? framesetResource:
		     "/" + framesetResource);
		TemplateProcessor.KeyMap keymap1 =
		    new TemplateProcessor.KeyMap();
		keymap1.put("menuWidth", "" + menuWidth);
		setupColors(keymap1, darkmode);
		TemplateProcessor tp = new TemplateProcessor(keymap1);
		/*
		CopyUtilities.copyStream(fsis, writer,
					 Charset.forName("UTF-8"));
		*/
		tp.processTemplate(new InputStreamReader(fsis, "UTF-8"),
				   writer);
		fsis.close();
		writer.flush();
		writer.close();
	    } else if (index == argv.length) {
		TemplateProcessor.KeyMap keymap =
		    NamedObjectFactory.getTemplateKeyMapForFactories();
		keymap.put("menuWidth", "" + menuWidth);
		setupColors(keymap, darkmode);
		printFactories(reader, System.out, keymap);
	    } else {
		printFactories(reader, darkmode, System.out, argv, index);
	    }
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    if (stackTrace) {
		e.printStackTrace(System.err);
	    }
	    System.exit(1);
	}
	System.exit(0);
    }
}
