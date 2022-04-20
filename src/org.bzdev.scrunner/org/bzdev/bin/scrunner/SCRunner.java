package org.bzdev.bin.scrunner;
import org.bzdev.scripting.*;
import org.bzdev.util.*;
import org.bzdev.io.*;
import org.bzdev.obnaming.NamedObjectFactory;
// import org.bzdev.net.URLClassLoaderOps;
import org.bzdev.net.URLPathParser;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.ObjectParser;

import javax.script.*;
import java.security.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
// import javax.security.auth.PrivateCredentialPermission;
// import javax.management.MBeanPermission;
// import javax.security.auth.kerberos.ServicePermission;
import java.util.regex.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

// used only to set the look and feel.
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

//@exbundle org.bzdev.bin.scrunner.lpack.SCRunner

public class SCRunner {

    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.scrunner.lpack.SCRunner");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    private static String ourCodebaseDir;
    static {
	try {
	    ourCodebaseDir =
		(new File(Scripting.class.getProtectionDomain()
			  .getCodeSource().getLocation()
			  .toURI())).getCanonicalFile()
		.getParentFile().getCanonicalPath();
	} catch (Exception e) {
	    System.err.println("Could not find our own codebase");
	    System.exit(1);
	}
    }

    static String fsepString = System.getProperty("file.separator");
    static char fsep = fsepString.charAt(0);

    static Set<String> extensions = null;
    static Map<String,OutputStream> ioOutputMap
	= new HashMap<String,OutputStream>();
    static Map<String,InputStream> ioInputMap
	= new HashMap<String,InputStream>();
    static Map<String,RandomAccessFile> ioRandomAccessMap
	= new HashMap<String,RandomAccessFile>();
    static Map<String,DirectoryAccessor> ioDirectoryMap
	= new HashMap<String,DirectoryAccessor>();
    static Map<String,Object> vmap = new HashMap<String,Object>();

    static Map<Character,Long> isuffixMap  = new HashMap<Character,Long>();
    static Map<Character,Double> dsuffixMap = new HashMap<Character,Double>();
    static {
	// use standard metric-unit prefixes
	isuffixMap.put('h', 100L);
	isuffixMap.put('k', 1000L);
	isuffixMap.put('M', 1000000L);
	isuffixMap.put('G', 1000000000L);
	isuffixMap.put('T', 1000000000000L);
	isuffixMap.put('P', 1000000000000000L);
	isuffixMap.put('E', 1000000000000000000L);

	dsuffixMap.put('h', 100.0);
	dsuffixMap.put('k', 1000.0);
	dsuffixMap.put('M', 1000000.0);
	dsuffixMap.put('G', 1000000000.0);
	dsuffixMap.put('T', 1.0e12);
	dsuffixMap.put('P', 1.0e15);
	dsuffixMap.put('E', 1.0e18);
	dsuffixMap.put('Z', 1.0e21);
	dsuffixMap.put('Y', 1.0e24);
	dsuffixMap.put('c', 1.0e-2);
	dsuffixMap.put('m', 1.0e-3);
	dsuffixMap.put('u', 1.0e-6);
	dsuffixMap.put('n', 1.0e-9);
	dsuffixMap.put('p', 1.0e-12);
	dsuffixMap.put('f', 1.0e-15);
	dsuffixMap.put('a', 1.0e-18);
	dsuffixMap.put('z', 1.0e-21);
	dsuffixMap.put('y', 1.0e-24);
    }


    static int parseInt(String string) throws NumberFormatException {
	
	if (string == null)
	    throw new NullPointerException(errorMsg("missingNumber"));
	int len = string.length();
	if (len == 0) throw new NumberFormatException(errorMsg("emptyString"));
	char last = string.charAt(len-1);
	if (isuffixMap.containsKey(last)) {
	    long mult = isuffixMap.get(last);
	    String istring = string.substring(0, len-1);
	    int value = Integer.parseInt(istring);
	    switch (last) {
	    case 'h':
	    case 'k':
	    case 'M':
	    case 'G':
		int result = value * (int) mult;
		if ((result < 0 && value > 0) || (result > 0 && value < 0)) {
		    throw new NumberFormatException
			(errorMsg("outOfRange", istring));
		}
		return result;
	    default:
		throw new NumberFormatException
		    (errorMsg("outOfRange", istring));
	    }
	} else {
	    return Integer.parseInt(string);
	}
    }

    static long parseLong(String string) throws NumberFormatException {
	
	if (string == null)
	    throw new NullPointerException(errorMsg("missingNumber"));
	int len = string.length();
	if (len == 0) throw new
			  NumberFormatException(errorMsg("emptyString"));
	char last = string.charAt(len-1);
	if (isuffixMap.containsKey(last)) {
	    long mult = isuffixMap.get(last);
	    String istring = string.substring(0, len-1);
	    long value = Long.parseLong(istring);
	    long result = value * mult;
	    if ((result < 0 && value > 0) || (result > 0 && value < 0)) {
		throw new NumberFormatException
		    (errorMsg("outOfRange", istring));
		/* throw new NumberFormatException("out of range");*/
	    }
	    return value *  mult;
	} else {
	    return Long.parseLong(string);
	}
    }

    static double parseDouble(String string) throws NumberFormatException {
	
	if (string == null)
	    throw new NullPointerException(errorMsg("missingNumber"));
	int len = string.length();
	if (len == 0)
	    throw new NumberFormatException(errorMsg("emptyString"));
	char last = string.charAt(len-1);
	if (isuffixMap.containsKey(last)) {
	    double mult = dsuffixMap.get(last);
	    String dstring = string.substring(0, len-1);
	    double value = Double.parseDouble(dstring);
	    return value *  mult;
	} else {
	    return Double.parseDouble(string);
	}
    }


    static String getExtension(String pathname) throws Exception {
	File file = new File(pathname);
	if (!file.isFile()) {
	    throw new Exception(errorMsg("notNormalFile", pathname));
	    /*("\"" + pathname  + "\" - not a normal file");*/
	}
	if (!file.canRead()) {
	    throw new Exception(errorMsg("notReadable", pathname));
		/*("\"" + pathname + "\" - not readable");*/
	}

	String name = file.getName();
	int index = name.lastIndexOf('.');
	if (++index == 0 || index == name.length())
	    return null;
	return name.substring(index);
    }

    static class Handler {
	static class Info {
	    ScriptingContext context;
	    String filename;
	    DelayedFileInputStream dfis;
	    boolean useStdin = false;
	    Info(ScriptingContext context, String filename)
		throws FileNotFoundException
	    {
		this.context = context;
		this.filename = filename;
		if (filename.equals("-")) {
		    useStdin = true;
		} else {
		    dfis = new DelayedFileInputStream(filename);
		}
	    }
	}
	ScriptEngine engine = null;
	Queue<Info> queue = new LinkedList<Info>();

	public void add(ScriptingContext context, String filename)
	    throws FileNotFoundException
	{
	    Info info = new Info(context, filename);
	    queue.add(info);
	}

	public void checkFiles() {
	    boolean stdinSeen = false;
	    int scriptIndex = 0;
	    for (Info info: queue) {
		scriptIndex++;
		try {
		    if (info.useStdin) {
			if (stdinSeen) {
			    System.err.println(errorMsg("multipleStdin"));
			    /*
			    System.err.println("scrunner: "
					       + "stdin used multiple times");
			    */
			    System.exit(1);
			}
			stdinSeen = true;
			continue;
		    }
		    String ext = getExtension(info.filename);
		    if (ext == null && scriptIndex == 1) {
			// If the file for the first script does not have
			// an extension, assume the extension is "esp",
			// because ESP is the default scripting language
			ext = "esp";
		    }
		    if (!extensions.contains(ext)) {
			System.err.println
			    (errorMsg("illegalExt", info.filename, ext));
			/*
			System.err.println("scrunner: "
					   + "illegal file extension for "
					   + info.filename);
			*/
			System.exit(1);
		    }

		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    //System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    }
	}

	public void run() throws ScriptException {
	    for (Info info: queue) {
		try {
		    Reader reader = new
			InputStreamReader(info.useStdin? System.in:
					  info.dfis.open());
		    info.context.evalScript(info.filename, reader);
		} catch (FileNotFoundException e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    // System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    }
	}
    }

    static void printStackTrace(Throwable e, PrintStream out) {
	StackTraceElement[] elements = e.getStackTrace();
	for (StackTraceElement element: elements) {
	    out.println("    " + element);
	}
    }

    public static void main(String argv[]) {
	// forbid recursive calls (e.g. when SCRunner loads classes from a
	// third party)
	CheckOncePerJVM.check();

	org.bzdev.protocols.Handlers.enable();

	String extension = null;
	String languageName = null;
	boolean languageNameSet = false; // Engine name set explicitly.


	boolean stackTrace = false;
	int trustLevel = 0;
	boolean showFactories = false;
	boolean listVersions = false;
	boolean exit = false;
	boolean listCodeBase = false;

	// String filename = "test.js";
	int index = 0;
	Writer writer = null;
	// Queue<String> trustedFileNames = new LinkedList<String>();
	// Queue<String> nontrusedFileNames = new LinkedList<String>();

	int stdoutCount = 1;
	String plaf = UIManager.getSystemLookAndFeelClassName();

	while (index < argv.length && argv[index].startsWith("-")
	       && !argv[index].equals("-t")) {
	    if (argv[index].equals("-")) {
		// This is a special case.  The argument "-" indicates
		// standard input and should be treated as a file-name
		// argument.  In this case, a preceding "--" argument
		// is not needed for the option to be unambiguous. The
		// index is not incremented.
		break;
	    } else if (argv[index].equals("--")) {
		index++;
		break;
	    } else if (argv[index].equals("--stackTrace")) {
		stackTrace = true;
	    } else if (argv[index].equals("-o")) {
		index++;
		try {
		    if (!argv[index].equals("-")) {
			if (writer == null) {
			   stdoutCount--;
			}
			writer = new PrintWriter(argv[index], "UTF-8");
		    } else {
			if (writer != null) {
			    stdoutCount++;
			}
			writer = null;
		    }
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].equals("--plaf")) {
		index++;
		String ui = argv[index];
		if (ui.equals("java")) {
		    plaf = null;
		} else if (!ui.equals("system")) {
		    plaf = argv[index];
		}
	    } else if (argv[index].startsWith("-o:")) {
		String varName = argv[index].substring(3);
		int ind = varName.indexOf(':');
		String string;
		if (ind < 0) {
		    index++;
		    string = argv[index];
		} else {
		    // handle case where the user types
		    // -o:foo:FILENAME  as experience shows this is
		    // a common error.  If a user does that and the next
		    // argument is a file containing a script, the script
		    // file could be overridden unless we handle that
		    // form of the argument.
		    string = varName.substring(ind+1);
		    varName = varName.substring(0, ind);
		}
		try {
		    if (varName.equals("scripting")
			|| ioOutputMap.containsKey(varName)
			|| ioInputMap.containsKey(varName)
			|| ioDirectoryMap.containsKey(varName)
			|| ioRandomAccessMap.containsKey(varName)
			|| vmap.containsKey(varName)) {
			throw new Exception
			    (errorMsg("variableInUse", varName));
			/*
			throw new Exception("variable name \"" + varName
					    +"\" in use");
			*/
		    }
		    if (string.equals("-")) {
			if (ioOutputMap.get(varName) != System.out) {
			    stdoutCount++;
			}
			ioOutputMap.put(varName, System.out);
		    } else {
			if (ioOutputMap.get(varName) == System.out) {
			    stdoutCount--;
			}
			FileOutputStream fos = new FileOutputStream(string);
			ioOutputMap.put(varName, fos);
		    }
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    // System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].startsWith("-i:")) {
		String varName = argv[index].substring(3);
		int ind = varName.indexOf(':');
		String string;
		if (ind < 0) {
		    index++;
		    string = argv[index];
		} else {
		    // handle case where the user types
		    // -o:foo:FILENAME  as experience shows this is
		    // a common error.  If a user does that and the next
		    // argument is a file containing a script, the script
		    // file could be overridden unless we handle that
		    // form of the argument.
		    string = varName.substring(ind+1);
		    varName = varName.substring(0, ind);
		}
		try {
		    if (varName.equals("scripting")
			|| ioOutputMap.containsKey(varName)
			|| ioInputMap.containsKey(varName)
			|| ioDirectoryMap.containsKey(varName)
			|| ioRandomAccessMap.containsKey(varName)
			|| vmap.containsKey(varName)) {
			throw new Exception
			    (errorMsg("variableInUse", varName));
			/*
			throw new Exception("variable name \"" + varName
					    +"\" in use");
			*/
		    }
		    FileInputStream fis = new FileInputStream(string);
		    ioInputMap.put(varName, fis);
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    // System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].startsWith("-d:")) {
		String varName = argv[index].substring(3);
		int ind = varName.indexOf(':');
		String string;
		if (ind < 0) {
		    index++;
		    string = argv[index];
		} else {
		    // handle case where the user types
		    // -o:foo:FILENAME  as experience shows this is
		    // a common error.  If a user does that and the next
		    // argument is a file containing a script, the script
		    // file could be overridden unless we handle that
		    // form of the argument.
		    string = varName.substring(ind+1);
		    varName = varName.substring(0, ind);
		}
		try {
		    if (varName.equals("scripting")
			|| ioOutputMap.containsKey(varName)
			|| ioInputMap.containsKey(varName)
			|| ioDirectoryMap.containsKey(varName)
			|| ioRandomAccessMap.containsKey(varName)
			|| vmap.containsKey(varName)) {
			throw new Exception
			    (errorMsg("variableInUse", varName));
			/*
			  throw new Exception("variable name \"" + varName
			  +"\" in use");
			*/
		    }
		    DirectoryAccessor da = new DirectoryAccessor(string);
		    ioDirectoryMap.put(varName, da);
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    // System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].startsWith("-v")) {
		if (argv[index].startsWith("-vS:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    String string;
		    if (ind < 0) {
			index++;
			string = argv[index];
		    } else {
			string = varName.substring(ind+1);
			varName = varName.substring(0, ind);
		    }
		    try {
			if (varName.equals("scripting")
			    || ioOutputMap.containsKey(varName)
			    || ioInputMap.containsKey(varName)
			    || ioDirectoryMap.containsKey(varName)
			    || ioRandomAccessMap.containsKey(varName)
			    || vmap.containsKey(varName)) 
			    {
				throw new Exception
				    (errorMsg("variableInUse", varName));
				//throw new Exception("variable name in use");*/
			    }
			vmap.put(varName, string);
		    } catch (Exception e) {
			String msg = e.getMessage();
			System.err.println
			    (errorMsg("exception3", "-vS", varName, msg));
			/*
			System.err.println("scrunner -vS:" +varName
					   +": " +e.getMessage());
			*/
			System.exit(1);
		    }
		} else if (argv[index].startsWith("-vB:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    String string;
		    if (ind < 0) {
			index++;
			string = argv[index];
		    } else {
			string = varName.substring(ind+1);
			varName = varName.substring(0, ind);
		    }
		    try {
			if (varName.equals("scripting")
			    || ioOutputMap.containsKey(varName)
			    || ioInputMap.containsKey(varName)
			    || ioDirectoryMap.containsKey(varName)
			    || ioRandomAccessMap.containsKey(varName)
			    || vmap.containsKey(varName))
			    {
				throw new Exception
				    (errorMsg("variableInUse", varName));
				/*throw new Exception("variable name in use");*/
			    }
			boolean value = Boolean.parseBoolean(string);
			vmap.put(varName, value);
		    } catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null) {
			    msg = "";
			}
			msg = e.getClass().getSimpleName() +" - " + msg;
			System.err.println
			    (errorMsg("exception3", "-vB", varName, msg));
			/*
			System.err.println("scrunner -vB:" +varName
					   + ": " + msg);
			*/
			System.exit(1);
		    }
		} else if (argv[index].startsWith("-vL:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    String string;
		    if (ind < 0) {
			index++;
			string = argv[index];
		    } else {
			string = varName.substring(ind+1);
			varName = varName.substring(0, ind);
		    }
		    try {
			if (varName.equals("scripting")
			    || ioOutputMap.containsKey(varName)
			    || ioInputMap.containsKey(varName)
			    || ioDirectoryMap.containsKey(varName)
			    || ioRandomAccessMap.containsKey(varName)
			    || vmap.containsKey(varName))
			    {
				throw new Exception
				    (errorMsg("variableInUse", varName));
				/*throw new Exception("variable name in use");*/
			    }
			long value = parseLong(string);
			vmap.put(varName, value);
		    } catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null) {
			    msg = "";
			}
			msg = e.getClass().getSimpleName() +" - " + msg;
			System.err.println
			    (errorMsg("exception3", "-vL", varName, msg));
			/*
			System.err.println("scrunner -vL:" +varName
					   + ": " + msg);
			*/
			System.exit(1);
		    }
		} else 	if (argv[index].startsWith("-vI:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    String string;
		    if (ind < 0) {
			index++;
			string = argv[index];
		    } else {
			string = varName.substring(ind+1);
			varName = varName.substring(0, ind);
		    }
		    try {
			if (varName.equals("scripting")
			    || ioOutputMap.containsKey(varName)
			    || ioInputMap.containsKey(varName)
			    || ioDirectoryMap.containsKey(varName)
			    || ioRandomAccessMap.containsKey(varName)
			    || vmap.containsKey(varName)) 
			    {
				throw new Exception
				    (errorMsg("variableInUse", varName));
				/*throw new Exception("variable name in use");*/
			    }
			int value = parseInt(string);
			vmap.put(varName, value);
		    } catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null) {
			    msg = "";
			}
			msg = e.getClass().getSimpleName() +" - " + msg;
			System.err.println
			    (errorMsg("exception3", "-vI", varName, msg));
			System.err.println("scrunner -vI:" + varName
					   + ": " + msg);
			System.exit(1);
		    }
		} else 	if (argv[index].startsWith("-vD:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    String string;
		    if (ind < 0) {
			index++;
			string = argv[index];
		    } else {
			string = varName.substring(ind+1);
			varName = varName.substring(0, ind);
		    }
		    try {
			if (varName.equals("scripting")
			    || ioOutputMap.containsKey(varName)
			    || ioInputMap.containsKey(varName)
			    || ioDirectoryMap.containsKey(varName)
			    || ioRandomAccessMap.containsKey(varName)
			    || vmap.containsKey(varName)) 
			    {
				throw new Exception
				    (errorMsg("variableInUse", varName));
				/*throw new Exception("variable name in use");*/
			    }
			double value = parseDouble(string);
			vmap.put(varName, value);
		    } catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null) {
			    msg = "";
			}
			msg = e.getClass().getSimpleName() +" - " + msg;
			System.err.println
			    (errorMsg("exception3", "-vD", varName, msg));
			/*
			System.err.println("scrunner -vD:" +varName
					   + ": " + msg);
			*/
			System.exit(1);
		    }
		} else {
		    System.err.println
			(errorMsg("unknownTypeCode", argv[index]));
		    /*
		    System.err.println("scrunner: "
				       + argv[index] +"- unknown type code");
		    */
		    System.exit(1);
		}
	    } else if (argv[index].equals("--trustLevel=0")) {
		trustLevel = 0;
	    } else if (argv[index].equals("--trustLevel=1")) {
		trustLevel = 1;
	    } else if (argv[index].equals("--trustLevel=2")) {
		trustLevel = 2;
	    } else if (argv[index].equals("-r")) {
		org.bzdev.math.StaticRandom.maximizeQuality();
	    } else if (argv[index].startsWith("-r")) {
		int ind = argv[index].indexOf(':');
		if (ind < 0) {
		    System.err.println(errorMsg("unknownOption", argv[ind]));
		    /*
		    System.err.println("scrunner: unknown option " + argv[ind]);
		    */
		    System.exit(1);
		}
		String mode = argv[index].substring(1, ind);
		String varName = argv[index].substring(ind+1);
		ind = varName.indexOf(':');
		String string;
		if (ind < 0) {
		    index++;
		    string = argv[index];
		} else {
		    // handle case where the user types
		    // -o:foo:FILENAME  as experience shows this is
		    // a common error.  If a user does that and the next
		    // argument is a file containing a script, the script
		    // file could be overridden unless we handle that
		    // form of the argument.
		    string = varName.substring(ind+1);
		    varName = varName.substring(0, ind);
		}
		try {
		    if (varName.equals("scripting")
			|| ioOutputMap.containsKey(varName)
			|| ioInputMap.containsKey(varName)
			|| ioDirectoryMap.containsKey(varName)
			|| ioRandomAccessMap.containsKey(varName)
			|| vmap.containsKey(varName)) {
			throw new Exception
			    (errorMsg("variableInUse", varName));
			/*throw new Exception("variable name \"" + varName
			  +"\" in use");*/
		    }
		    RandomAccessFile raf = new RandomAccessFile(string, mode);
		    ioRandomAccessMap.put(varName, raf);
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    // System.err.println("scrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].equals("-L")) {
		index++;
		languageName = argv[index];
		if (!Scripting.supportsLanguage(languageName)) {
		    String ln = Scripting.getLanguageNameByAlias(languageName);
		    if (ln == null) {
			String msg =
			    errorMsg("badScriptingLanguageName", languageName);
			System.err.println(msg);
			System.exit(1);
		    } else {
			languageName = ln;
		    }
		}
	    } else if (argv[index].equals("--listCodeBase")) {
		// SCRunner ensures that this will always be before
		// the --codebase options
		listCodeBase = true;
	    } else if (argv[index].equals("--codebase")
		       || argv[index].equals("--classpathCodebase")) {
		index++;
		if (listCodeBase) {
		    String cp = argv[index];
		    try {
			URL[] urls = URLPathParser.getURLs(null, cp,
							   ourCodebaseDir,
							   System.err);
			// URLClassLoaderOps.addURLs(urls);
			for (URL url: urls) {
			    System.out.println(url.toString());
			}
		    } catch (MalformedURLException e) {
			String msg =
			    errorMsg("codebaseError", cp,e.getMessage());
			System.err.append(msg +"\n");
			System.exit(1);
		    }
		}
	    } else if (argv[index].equals("--versions")) {
		listVersions = true;
	    } else if (argv[index].equals("--exit")) {
		// force a call to System.exit(0) at the end.
		exit = true;
	    } else if (argv[index].equals("--supportsLanguage")) {
		index++;
		if (Scripting.supportsLanguage(argv[index])) {
		    System.exit(0);
		} else {
		    System.exit(1);
		}
	    }  else {
		System.err.println(errorMsg("unknownOption", argv[index]));
		/*
		System.err.println("scrunner: unknown option \"" + argv[index] 
				   + "\"");
		*/
		System.exit(1);
	    }
	    index++;
	}

	if (listCodeBase) System.exit(0);
	if (stdoutCount > 1) {
	    System.err.println(errorMsg("multipleOut"));
	    /*
	    System.err.println("scrunner: Multiple output streams sent "
			       + "to stdout");
	    */
	    System.exit(1);
	}

	if (listVersions) {
	    // List version info for each JAR file
	    // (e.g., each META-INF/MANIFEST.MF JAR-file entry)
	    Enumeration<URL> manifestURLs = null;
	    try {
		manifestURLs =
		    ClassLoader.getSystemResources("META-INF/MANIFEST.MF");
	    } catch (IOException e) {
		System.err.println(errorMsg("noManifest", e.getMessage()));
		System.exit(1);
	    }
	    while (manifestURLs.hasMoreElements()) {
		URL url = manifestURLs.nextElement();
		String urlString = url.toString();
		if (urlString.startsWith("jar:")) {
		    int lst = urlString.indexOf("!/META-INF/MANIFEST.MF");
		    if (lst < 0) {
			urlString = urlString.substring(4);
		    } else {
			urlString = urlString.substring(4, lst);
		    }
		}
		try {
		    InputStream is = url.openStream();
		    Manifest mf = new Manifest(is);
		    Attributes attributes = mf.getMainAttributes();
		    String version =attributes.getValue
			(Attributes.Name.IMPLEMENTATION_VERSION);
		    if (version == null) version = "-";
		    String specVersion = attributes.getValue
			(Attributes.Name.SPECIFICATION_VERSION);
		    if (specVersion == null) specVersion = "-";
		    System.out.println(urlString);
		    System.out.println("    " + specVersion + " " + version);
		} catch (IOException e) {
		    continue;
		}
	    }
	}

	int ind = index;
	while (ind < argv.length) {
	    if (argv[ind].equals("-t")) {
		ind++;
		if (ind < argv.length) {
		    if (extension == null) {
			try {
			    if (!argv[ind].equals("-")) {
				extension = getExtension(argv[ind]);
			    }
			} catch (Exception ee) {
			    System.err.println
				(errorMsg("exception", ee.getMessage()));
			    /*
			    System.err.println("scrunner: "
					       + ee.getMessage());
			    */
			    System.exit(1);
			}
		    }
		}
	    } else {
		if (extension == null) {
		    try {
			if (!argv[ind].equals("-")) {
			    extension = getExtension(argv[ind]);
			}
		    } catch (Exception ee) {
			System.err.println
			    (errorMsg("exception", ee.getMessage()));
			/*
			System.err.println("scrunner: "
					   + ee.getMessage());
			*/
			System.exit(1);
		    }
		}
	    }
	    ind++;
	}

	if (plaf != null) {
	    final String currentPLAF = plaf;
	    SwingUtilities.invokeLater(() -> {
		    try {
			UIManager.setLookAndFeel(currentPLAF);
		    } catch (Exception uie) {
			System.err.println(errorMsg("plaf", currentPLAF));
			System.exit(1);
		    }
		});
	}

	if (languageName == null) {
	    if (extension == null) {
		languageName = "ESP";
	    } else {
		languageName = Scripting.getLanguageNameByExtension(extension);
	    }
	}

	final Writer theWriter = writer;
	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		    if (theWriter != null) {
			try {
			    theWriter.flush();
			    theWriter.close();
			} catch (Exception eio){}
		    }
		    for (OutputStream os: ioOutputMap.values()) {
			try {
			    os.flush();
			    os.close();
			} catch (Exception eio) {}
		    }
	}));

	extensions = new HashSet<String>
	    (Scripting.getExtensionsByLanguageName(languageName));
	ScriptingContext tcontext =
	    new DefaultScriptingContext(languageName, true);
	ScriptingContext context = new ScriptingContext(tcontext, false);
	tcontext = new ExtendedScriptingContext(tcontext, true);
	context = new ExtendedScriptingContext(context, false);
	Handler handler = new Handler();

	while (index < argv.length) {
	    try {
		if (argv[index].equals("-t")) {
		    index++;
		    if (index < argv.length) {
			handler.add(tcontext, argv[index]);
		    }
		} else {
		    handler.add(context, argv[index]);
		}
	    } catch (Exception e) {
		System.err.println
		    (errorMsg("noOpen2", argv[index], e.getMessage()));
		/*
		System.err.println("scrunner: could not open " + argv[index]
				   + " - " + e.getMessage());
		*/
	    }
	    index++;
	}

	context.putScriptObject("scripting", context);
	for (Map.Entry<String,OutputStream>entry: ioOutputMap.entrySet()) {
	    context.putScriptObject(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,InputStream>entry: ioInputMap.entrySet()) {
	    context.putScriptObject(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,DirectoryAccessor>entry:
		 ioDirectoryMap.entrySet()) {
	    context.putScriptObject(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,RandomAccessFile>entry:
		 ioRandomAccessMap.entrySet()) {
	    context.putScriptObject(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,Object>entry: vmap.entrySet()) {
	    context.putScriptObject(entry.getKey(), entry.getValue());
	}

	if (writer != null) context.setWriter(writer);

	handler.checkFiles();

	if (trustLevel == 0) {
	    System.setSecurityManager(new SecurityManager());
	} else if (trustLevel == 1) {
	    System.setSecurityManager(new ScriptingSecurityManager());
	}
	try {
	    handler.run();
	} catch (Exception e) {
	    if (stackTrace) {
		if (e instanceof ScriptException) {
		    ScriptException se = (ScriptException) e;
		    String fn = se.getFileName();
		    int ln = se.getLineNumber();
		    String msg = e.getMessage();
		    if (ln != -1) {
			String tail = String.format("(%s#%d)", fn, ln);
			if (msg.endsWith(tail)) {
			    msg = msg.substring(0, msg.lastIndexOf(tail));
			    msg = msg.stripTrailing();
			}
		    }
		    if (ln == -1) {
			msg = errorMsg("unnumberedException", fn, msg);
		    } else {
			msg = errorMsg("numberedException", fn, ln, msg);
		    }
		    System.err.println(msg);
		} else {
		    System.err.println(e.getClass().getName() + ": "
				       + e.getMessage());
		}
		printStackTrace(e, System.err);
		Throwable cause = e.getCause();
		while (cause != null) {
		    System.err.println("---------");
		    if (cause instanceof ObjectParser.Exception) {
			System.err.println(cause.getMessage());
		    } else {
			System.err.println(cause.getClass().getName() + ": "
					   + cause.getMessage());
		    }
		    printStackTrace(cause, System.err);
		    cause = cause.getCause();
		}
	    } else {
		Throwable cause = e.getCause();
		Class<?> ec = e.getClass();
		String msg;
		if (e instanceof ScriptException) {
		    ScriptException se = (ScriptException)e;
		    String fn = se.getFileName();
		    int ln = se.getLineNumber();
			String m = se.getMessage();
			if (ln != -1) {
			    // Some scripting-related exceptions tag
			    // a string of the form (FILENAME#LINENO)
			    // to the end of a message.  This is redundant
			    // so we will eliminate it when it matches the
			    // file name and line number we are printing.
			    // The following lines contain all the 'cause'
			    // messages anyway, so all the information is
			    // available.  The lack of redundancy makes the
			    // first message easier to read.
			    String tail = String.format("(%s#%d)", fn, ln);
			    if (m.endsWith(tail)) {
				m = m.substring(0, m.lastIndexOf(tail));
				m = m.stripTrailing();
			    }
			}
			if (ln == -1) {
			    msg = errorMsg("unnumberedException", fn, m);
			} else {
			    msg = errorMsg("numberedException", fn, ln, m);
			}
		} else {
		    String cn = e.getClass().getName();
		    msg = errorMsg("exception2", cn, e.getMessage());
		}
		System.err.println(msg);
		// System.err.println("scrunner: " + e.getMessage());
		cause = e.getCause();
		while (cause != null) {
		    Class<?> clasz = cause.getClass();
		    Class<?> target =
			org.bzdev.obnaming.NamedObjectFactory
			.ConfigException.class;
		    String tn = errorMsg("configException");
		    String cn =(clasz.equals(target))? tn: clasz.getName();
		    if (clasz.equals(ObjectParser.Exception.class)) {
			System.err.println(cause.getMessage());
		    } else {
			msg = errorMsg("continued", cn, cause.getMessage());
			System.err.println("  ... " + msg);
		    }
		    cause = cause.getCause();
		}
	    }
	    System.exit(1);
	}
	if (exit) System.exit(0);
    }
}
