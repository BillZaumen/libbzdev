package org.bzdev.bin.yrunner;
import org.bzdev.obnaming.ObjectNamerLauncher;
import org.bzdev.util.*;
import org.bzdev.io.*;
import org.bzdev.obnaming.NamedObjectFactory;
// import org.bzdev.net.URLClassLoaderOps;
import org.bzdev.net.URLPathParser;
// import org.bzdev.util.SafeFormatter;
// import org.bzdev.util.TemplateProcessor;
// import org.bzdev.util.TemplateProcessor.KeyMap;
// import org.bzdev.util.TemplateProcessorKeyMapList;

import java.security.*;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;
// import javax.security.auth.PrivateCredentialPermission;
// import javax.management.MBeanPermission;
// import javax.security.auth.kerberos.ServicePermission;
import java.util.regex.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

//@exbundle org.bzdev.bin.yrunner.lpack.YRunner

public class YRunner {

    // as a convenience, convert tabs to spaces in YAML files assuming
    // a tab spacing of 8.
    static final int TAB_SPACING = 8;


    // resource bundle for messages used by exceptions and errors
    static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.bin.yrunner.lpack.YRunner");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    private static String ourCodebaseDir;
    static {
	try {
	    ourCodebaseDir =
		(new File(ObjectNamerLauncher.class.getProtectionDomain()
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

    static Set<String> extensions = Set.of("yaml", "YAML", "yml", "YML");
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
	if (index++ == 0 || index == name.length())
	    return null;
	return name.substring(index);
    }
    
    static final Charset UTF8 = Charset.forName("UTF-8");

    static class Handler {
	static class Info {
	    String filename;
	    DelayedFileInputStream dfis;
	    boolean useStdin = false;
	    Info(String filename)
		throws FileNotFoundException
	    {
		this.filename = filename;
		if (filename.equals("-")) {
		    useStdin = true;
		} else {
		    dfis = new DelayedFileInputStream(filename);
		}
	    }
	}
	Queue<Info> queue = new LinkedList<Info>();

	public void add(String filename)
	    throws FileNotFoundException
	{
	    Info info = new Info(filename);
	    queue.add(info);
	}

	public void checkFiles() {
	    boolean stdinSeen = false;
	    for (Info info: queue) {
		try {
		    if (info.useStdin) {
			if (stdinSeen) {
			    String msg = errorMsg("multipleStdin");
			    System.err.println(msg);
			    /*
			    System.err.println("yrunner: "
					       + "stdin used multiple times");
			    */
			    System.exit(1);
			}
			stdinSeen = true;
			continue;
		    }
		    if (!extensions.contains(getExtension(info.filename))) {
			String msg = errorMsg("illegalExt", info.filename);
			System.err.println(msg);
			/*
			System.err.println("yrunner: "
					   + "illegal file extension for "
					   + info.filename);
			*/
			System.exit(1);
		    }

		} catch (Exception e) {
		    String emsg = e.getMessage();
		    String msg = errorMsg("exception", emsg);
		    System.err.println(msg);
		    //System.err.println("yrunner: " + e.getMessage());
		    System.exit(1);
		}
	    }
	}

	public void run(ObjectNamerLauncher launcher) throws Exception {
	    for (Info info: queue) {
		try {
		    Reader reader = new
			DetabReader(new InputStreamReader
				    ((info.useStdin? System.in:
				      info.dfis.open()),UTF8), TAB_SPACING);
		    // Object object = JSUtilities.YAML.parse(reader);
		    launcher.process((info.useStdin? null: info.filename),
				     reader, true);
		} catch (FileNotFoundException efnf) {
		    String msg = errorMsg("exception", efnf.getMessage());
		    System.err.println(msg);
		    System.exit(1);
		} /* catch (Exception e) {
		    String fn = info.useStdin? "<stdin>": info.filename;
		    String msg = errorMsg("yErr", e.getMessage());
		    Exception ex = new IOException(msg, e.getCause());
		    ex.setStackTrace(e.getStackTrace());
		    throw ex;
		    } */
	    }
	}
    }

    static void printStackTrace(Throwable e, PrintStream out) {
	StackTraceElement[] elements = e.getStackTrace();
	for (StackTraceElement element: elements) {
	    out.println("    " + element);
	}
    }

    private static TemplateProcessor.KeyMapList
	getClassList(TemplateProcessor.KeyMapList list)
    {
	TemplateProcessor.KeyMapList clist = new
	    TemplateProcessor.KeyMapList();
	String cname = null;
	for (TemplateProcessor.KeyMap kmap: list) {
	    if (!kmap.get("class").equals(cname)) {
		cname = (String)kmap.get("class");
		kmap.put("title", cname);
		TemplateProcessor.KeyMap kmap2 =
		    new TemplateProcessor.KeyMap();
		kmap2.put("class", cname);
		clist.add(kmap2);
	    }
	}
	return clist;
    }


    private static final String LAUNCHERS_TPL = "launchers.tpl";
    private static final URL[] noURLs = new URL[0];

    public static void main(String argv[]) {
	// forbid recursive calls (e.g. when YRunner loads classes from a
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
	boolean listLaunchers = false;
	boolean verbose = false;
	boolean listCodebase = false;
	boolean generateDocs = false;

	// String filename = "test.js";
	int index = 0;
	Writer writer = null;
	Writer ewriter = null;
	// Queue<String> trustedFileNames = new LinkedList<String>();
	// Queue<String> nontrusedFileNames = new LinkedList<String>();

	int stdoutCount = 1;

	List<URL> apis = new LinkedList<URL>();

	while (index < argv.length && argv[index].startsWith("-")) {
	    if (argv[index].equals("--")) {
		index++;
		break;
	    } else if (argv[index].equals("--stackTrace")) {
		stackTrace = true;
	    } else if (argv[index].equals("--link")) {
		index++;
		try {
		    URL[] apiURLs  = URLPathParser.getURLs(null, argv[index],
						       null, null);
		    if (apiURLs == null) apiURLs = noURLs;
		    for (URL url: apiURLs) {
			apis.add(url);
		    }
		} catch (MalformedURLException mue) {
		    System.err.println(errorMsg("badURL", argv[index]));
		    System.exit(1);
		}
	    } else if (argv[index].equals("-o")) {
		index++;
		try {
		    if (!argv[index].equals("-")) {
			if (writer == null) {
			   stdoutCount--;
			}
			writer = new FileWriter(argv[index], UTF8);
			writer = new PrintWriter(writer, true);
		    } else {
			if (writer != null) {
			    stdoutCount++;
			}
			writer = null;
		    }
		} catch (Exception e) {
		    System.err.println(errorMsg("exception", e.getMessage()));
		    System.err.println("yrunner: " + e.getMessage());
		    System.exit(1);
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
		    if (varName.equals("out")
			|| (varName.equals("err") && ewriter != null)
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
		    } else if (varName.equals("err")) {
			if (!string.equals("-")) {
			    ewriter = new FileWriter(string, UTF8);
			    ewriter = new PrintWriter(ewriter, true);
			} else {
			    ewriter = new OutputStreamWriter(System.out, UTF8);
			    ewriter = new PrintWriter(ewriter, true);
			}
		    } else  if (string.equals("-")) {
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
		    // System.err.println("yrunner: " + e.getMessage());
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
		    // -i:foo:FILENAME  as experience shows this is
		    // a common error.  If a user does that and the next
		    // argument is a file containing a script, the script
		    // file could be overridden unless we handle that
		    // form of the argument.
		    string = varName.substring(ind+1);
		    varName = varName.substring(0, ind);
		}
		try {
		    if (varName.equals("out") || varName .equals("err")
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
		    // System.err.println("yrunner: " + e.getMessage());
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
		    if (varName.equals("out") || varName .equals("err")
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
		    // System.err.println("yrunner: " + e.getMessage());
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
			if (varName.equals("out") || varName .equals("err")
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
			System.err.println("yrunner -vS:" +varName
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
			if (varName.equals("out") || varName .equals("err")
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
			System.err.println("yrunner -vB:" +varName
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
			if (varName.equals("out") || varName .equals("err")
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
			System.err.println("yrunner -vL:" +varName
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
			if (varName.equals("out") || varName .equals("err")
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
			System.err.println("yrunner -vI:" + varName
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
			if (varName.equals("out") || varName .equals("err")
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
			System.err.println("yrunner -vD:" +varName
					   + ": " + msg);
			*/
			System.exit(1);
		    }
		} else {
		    System.err.println
			(errorMsg("unknownTypeCode", argv[index]));
		    /*
		    System.err.println("yrunner: "
				       + argv[index] +"- unknown type code");
		    */
		    System.exit(1);
		}
	    } else if (argv[index].equals("--trustLevel=0")) {
		trustLevel = 0;
	    } else if (argv[index].equals("--trustLevel=2")) {
		trustLevel = 2;
	    } else if (argv[index].equals("-r")) {
		org.bzdev.math.StaticRandom.maximizeQuality();
	    } else if (argv[index].startsWith("-r")) {
		int ind = argv[index].indexOf(':');
		if (ind < 0) {
		    System.err.println(errorMsg("unknownOption", argv[ind]));
		    /*
		    System.err.println("yrunner: unknown option " + argv[ind]);
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
		    if (varName.equals("out") || varName .equals("err")
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
		    // System.err.println("yrunner: " + e.getMessage());
		    System.exit(1);
		}
	    } else if (argv[index].equals("--listLaunchers")) {
		listLaunchers = true;
	    } else if (argv[index].equals("--verbose")) {
		verbose = true;
	    } else if (argv[index].equals("--generateDocs")) {
		generateDocs = true;
	    } else if (argv[index].equals("--listCodebase")) {
		// YRunner ensures that this will always be before
		// the --codebase options
		listCodebase = true;
	    } else if (argv[index].equals("--codebase")
		       || argv[index].equals("--classpathCodebase")) {
		index++;
		if (listCodebase) {
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
	    }  else {
		System.err.println(errorMsg("unknownOption", argv[index]));
		/*
		System.err.println("yrunner: unknown option \"" + argv[index] 
				   + "\"");
		*/
		System.exit(1);
	    }
	    index++;
	}

	if (listLaunchers) {
	    if (verbose) {
		TemplateProcessor.KeyMap keymap = ObjectNamerLauncher
		    .getProviderKeyMap();
		keymap.put("launchers", errorMsg("launchers"));
		keymap.put("launcherData", errorMsg("launcherData"));
		keymap.put("bullet", "\u2022");
		keymap.put("emdash", "\u2014");
		TemplateProcessor tp = new TemplateProcessor(keymap);
		try {
		    InputStream is =
			YRunner.class.getResourceAsStream(LAUNCHERS_TPL);
		    Reader r = new InputStreamReader(is, UTF8);
		    tp.processTemplate(r, "UTF-8", System.out);
		} catch (IOException eio) {
		    eio.printStackTrace();
		    System.exit(1);
		}
		System.exit(0);
	    } else {
		System.out.println(errorMsg("launchers"));
		for (String name: ObjectNamerLauncher.getLauncherNames()) {
		    System.out.println("    " + name);
		}
		System.out.println(errorMsg("launcherData"));
		for (String name: ObjectNamerLauncher.getLauncherDataNames()) {
		    System.out.println("    " + name);
		}
		System.exit(0);
	    }
	}
	if (listCodebase) System.exit(0);
	if (stdoutCount > 1) {
	    System.err.println(errorMsg("multipleOut"));
	    /*
	    System.err.println("yrunner: Multiple output streams sent "
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

	if (generateDocs) {
	    if (index >= argv.length) {
		System.err.println(errorMsg("missingArgs"));
		System.exit(1);
	    }
	} else if (index >= argv.length - 1) {
	    System.out.println("index = " + index + ", argv.length = "
			       + argv.length);
	    System.err.println(errorMsg("missingArgs"));
	    System.exit(1);
	}
	String lnames = argv[index].trim();
	int lindex = lnames.indexOf(',');

	String lname = (lindex == -1)? lnames: lnames.substring(0, lindex);
	String rest = (lindex == -1)? null: lnames.substring(lindex+1);
	String[] dlaunchers = (rest == null)? new String[0]: rest.split(",");
	ObjectNamerLauncher launcher = null;
	try {
	    launcher = ObjectNamerLauncher.newInstance(lname, dlaunchers);
	    index++;
	} catch (Exception eie) {
	    String ecn = eie.getClass().getName();
	    String emsg = eie.getMessage();
	    if (rest == null) {
		System.err.println(errorMsg("eieErr1", lname, ecn, emsg));
		if (stackTrace) {
		    eie.printStackTrace();	
		}
	    } else {
		System.err.println(errorMsg("eieErr2", lname, rest, ecn, emsg));
		if (stackTrace) {
		    eie.printStackTrace();
		}
	    }
	    System.exit(1);
	}

	if (generateDocs) {
	    try {
		ObjectNamerLauncher.createAPIMap(apis);
		TemplateProcessor.KeyMap kmap = new TemplateProcessor.KeyMap();
		kmap.put("lnames", lnames);
		kmap.put("retList",
			 ObjectNamerLauncher.keylistForReturnClasses());
		kmap.put("argList",
			 ObjectNamerLauncher.keylistForArgumentClasses());
		TemplateProcessor.KeyMapList
		    list = ObjectNamerLauncher.keylistForConstructors();
		kmap.put("constrClasses", getClassList(list));
		kmap.put("constrList", list);
		list = ObjectNamerLauncher.keylistForFunctions();
		kmap.put("functClasses", getClassList(list));
		kmap.put("functList", list);
		list = ObjectNamerLauncher.keylistForMethods();
		kmap.put("methodClasses", getClassList(list));
		kmap.put("methodList", list);
		kmap.put("constList",
			 ObjectNamerLauncher.keylistForConstants());
		String resource = errorMsg("docResource");
		TemplateProcessor tp = new TemplateProcessor(kmap);
		tp.processSystemResource(resource, "UTF-8", System.out);
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
		System.exit(1);
	    }
	    System.exit(0);
	}
	
	Handler handler = new Handler();

	while (index < argv.length) {
	    try {
		handler.add(argv[index]);
	    } catch (Exception e) {
		System.err.println
		    (errorMsg("noOpen2", argv[index], e.getMessage()));
		/*
		System.err.println("yrunner: could not open " + argv[index]
				   + " - " + e.getMessage());
		*/
	    }
	    index++;
	}
	for (Map.Entry<String,OutputStream>entry: ioOutputMap.entrySet()) {
	    launcher.set(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,InputStream>entry: ioInputMap.entrySet()) {
	    launcher.set(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,DirectoryAccessor>entry:
		 ioDirectoryMap.entrySet()) {
	    launcher.set(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,RandomAccessFile>entry:
		 ioRandomAccessMap.entrySet()) {
	    launcher.set(entry.getKey(), entry.getValue());
	}
	for (Map.Entry<String,Object>entry: vmap.entrySet()) {
	    launcher.set(entry.getKey(), entry.getValue());
	}

	if (writer != null) {
	    launcher.set("out", writer);
	} else {
	    Writer osw = new OutputStreamWriter(System.out, UTF8);
	    launcher.set("out", new PrintWriter(osw, true));
	}

	if (ewriter != null) {
	    launcher.set("err", ewriter);
	} else {
	    Writer osw = new OutputStreamWriter(System.err, UTF8);
	    launcher.set("err", new PrintWriter(osw, true));
	}

	handler.checkFiles();

	if (trustLevel == 0) {
	    System.setSecurityManager(new SecurityManager());
	} else if (trustLevel == 1) {
	    System.err.println(errorMsg("noSSM"));
	    System.exit(1);
	}
	try {
	    handler.run(launcher);
	} catch (Exception e) {
	    System.err.println(errorMsg("yrunner", e.getMessage()));
	    if (stackTrace) {
		printStackTrace(e, System.err);
		Throwable cause = e.getCause();
		while (cause != null) {
		    System.err.println("---------");
		    System.err.println(cause.getClass().getName() + ": "
				       + cause.getMessage());
		    printStackTrace(cause, System.err);
		    cause = cause.getCause();
		}
	    } else {
		Throwable cause = e.getCause();
		while (cause != null) {
		    System.err.println("---------");
		    System.err.println(cause.getClass().getName() + ": "
				       + cause.getMessage());
		    cause = cause.getCause();
		}
	    }
	    System.exit(1);
	}
	if (exit) System.exit(0);
    }
}
