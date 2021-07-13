package org.bzdev.bin.lsnof;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bzdev.net.URLPathParser;
import org.bzdev.scripting.Scripting;
import org.bzdev.util.CopyUtilities;


//@exbundle org.bzdev.bin.lsnof.lpack.FactoryPrinter

/**
 * Launcher for FactoryPrinter.
 */
public class FactoryPrinterCmd {

    static String errorMsg(String key, Object... args) {
	return FactoryPrinter.errorMsg(key, args);
    }

    private static String javacmd = "java";

    private static final String pathSeparator =
	System.getProperty("path.separator");


    private static final String dotDotDot = "...";
    private static final String dotDotDotSep = dotDotDot + File.separator;
    private static final String tildeSep = "~" + File.separator;
    private static String ourCodebaseDir;
    static {
	try {
	    ourCodebaseDir =
		(new File(Scripting.class.getProtectionDomain()
			  .getCodeSource().getLocation().toURI()))
		.getCanonicalFile().getParentFile().getCanonicalPath();
	} catch (Exception e) {
	    System.err.println(errorMsg("missingOwnCodebase"));
	    System.exit(1);
	}
    }

    // module path
    private static StringBuilder sbmp = new StringBuilder();
    // module list
    private static StringBuilder sbmod = new StringBuilder();
    // class path
    private static StringBuilder sbcp = new StringBuilder();

    private static String MODULE_NAME_RE =
	"^([\\p{L}_$][\\p{L}\\p{N}_$]*)([.][\\p{L}_$][\\p{L}\\p{N}_$]*)*$";

    private static Set<String> modSet = new HashSet<>();

    private static boolean classpathExtended = false;

    static void extendCodebase(String codebase) {
	extendCodebase(codebase, true);
    }

    private static Map<String,Boolean> urlMap = new HashMap<>();

    static void extendCodebase(String codebase, boolean modulePath) {
	try {
	    URL[] urls = URLPathParser.getURLs(null, codebase,
					       ourCodebaseDir,
					       System.err);
	    for (URL url: urls) {
		if (urlMap.containsKey(url.toString())) {
		    if (urlMap.get(url.toString()) != modulePath) {
			String msg = errorMsg("doubleUse", url.toString());
			System.err.println(msg);
			System.exit(1);
		    }
		    continue;
		}
		String fname;
		if (url.getProtocol().equals("file")) {
		    File f = new File(url.toURI());
		    if (!f.canRead()) {
			throw new IOException
			    (errorMsg("noFile", url.toString()));
		    } else if (!f.isDirectory() && !f.isFile()) {
			throw new IOException
			    (errorMsg("noFile", url.toString()));
		    }
		    fname = f.getCanonicalPath();
		    url = f.getCanonicalFile().toURI().toURL();
		    if (urlMap.containsKey(url.toString())) {
			if (urlMap.get(url.toString()) != modulePath) {
			    String msg = errorMsg("doubleUse", url.toString());
			    System.err.println(msg);
			    System.exit(1);
			}
			continue;
		    }
		} else {
		    if (urlMap.containsKey(url.toString())) {
			if (urlMap.get(url.toString()) != modulePath) {
			    String msg = errorMsg("doubleUse", url.toString());
			    System.err.println(msg);
			    System.exit(1);
			}
			continue;
		    }
		    File tmp = File.createTempFile("scrunner", "jar");
		    tmp.deleteOnExit();
		    OutputStream os = new FileOutputStream(tmp);
		    InputStream is = url.openConnection().getInputStream();
		    try {
			// CopyUtilities.copyStream(is, os);
			is.transferTo(os);
		    } finally {
			os.close();
			is.close();
		    }
		    fname = tmp.getCanonicalPath();
		}
		urlMap.put(url.toString(), modulePath);
		if (modulePath) {
		    if (sbmp.length() > 0) {
			sbmp.append(pathSeparator);
		    }
		    sbmp.append(fname);
		} else {
		    if (sbcp.length() > 0) {
			sbcp.append(pathSeparator);
		    }
		    sbcp.append(fname);
		}
	    }
	    // URLClassLoaderOps.addURLs(urls);
	} catch (Exception e) {
	    System.err.append
		(errorMsg("codebaseError", codebase, e.getMessage()) + "\n");
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
		boolean modulePath = false;
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
			modulePath = false;
			continue;
		    } else if (line.equals("%modulepath.components")) {
			mode = 2;
			langsearch = true;
			langsection = false;
			modulePath = true;
			continue;
		    } else if (line.equals("%modules")) {
			mode = 3;
			langsearch = true;
			langsection = false;
			continue;
		    } else if (line.startsWith("%java")) {
			if (line.length() < 6) {
			    int lno = reader.getLineNumber();
			    String msg = errorMsg("directive", fileName, lno);
			    System.err.println(msg);
			    System.exit(1);
			}
			char ch = line.charAt(5);
			if (!Character.isSpaceChar(ch)) {
			    int lno = reader.getLineNumber();
			    String msg = errorMsg("directive", fileName, lno);
			    System.err.println(msg);
			    System.exit(1);
			}
			String cmd = line.substring(6).trim();
			if (cmd.length() > 0) {
			    javacmd = cmd;
			}
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
				mode = 0;
				langsection = false;
			    } else if (languageName.equals(language)) {
				langsection = true;
			    } else  {
				if (langsection) mode = 0;
				langsection = false;
			    }
			    langsearch = false;
			    continue;
			} else if (line.startsWith("%")) {
			    String lineNo = "" + reader.getLineNumber();
			    System.err.println
				(errorMsg("syntaxEnd", fileName, lineNo));
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
				extendCodebase(url.toString(), modulePath);
				classpathExtended = true;
				// URLClassLoaderOps.addURL(url);
			    } catch (Exception e) {
				String lineNo = "" + reader.getLineNumber();
				System.err.println
				    (errorMsg("syntaxURL", fileName, lineNo));
				System.exit(1);
			    }
			}
			break;
		    case 3: // modules (for use with Java's --add-modules)
			if (line.startsWith("%lang")) {
			    String language = line.substring(5).trim();
			    if (languageName == null) {
				mode = 0 /*3*/;
				langsection = false;
			    } else if (languageName.equals(language)) {
				langsection = true;
			    } else  {
				if (langsection) mode = 0 /*3*/;
				langsection = false;
			    }
			    langsearch = false;
			    continue;
			} else if (line.startsWith("%")) {
			    int lno = reader.getLineNumber();
			    String msg =
				errorMsg("endExpected", fileName, lno);
			    System.err.println(msg);
			    error = true;
			    continue;
			} else if (!line.matches(MODULE_NAME_RE)) {
			    int lno = reader.getLineNumber();
			    String msg =
				errorMsg("badModuleName", fileName, lno, line);
			    System.err.println("msg");
			    error = true;
			    continue;
			}
			if (modSet.contains(line) == false) {
			    if (sbmod.length() > 0) {
				sbmod.append(",");
			    }
			    sbmod.append(line);
			    modSet.add(line);
			}
			break;
		    }
		}
		if (error)
		    throw new Exception(errorMsg("terminating"));
	    } catch (Exception e) {
		String lineNo = "" + reader.getLineNumber();
		String msg = e.getMessage();
		System.err.println
		    (errorMsg("exception3", fileName, lineNo, msg));
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

    static String defaultSysConfigFile() {
	String fsep = System.getProperty("file.separator");
	if (fsep.equals("/")) {
	    // Unix file separator. Try to find it in /etc
	    // or /etc/opt ; return null otherwise as the
	    // location is unknown.
	    Path p;
	    try {
		p = Paths.get(FactoryPrinterCmd.class.getProtectionDomain()
			      .getCodeSource().getLocation().toURI());
	    } catch(Exception e) {
	       return null;
	    }
	    if (p.startsWith("/opt")) {
		File cf = new File("/etc/opt/bzdev/scrunner.conf");
		if (cf.canRead()) {
		    return "/etc/opt/bzdev/scrunner.conf";
		}
	    } else if (p.startsWith("/usr/local")) {
		File cf = new File("/usr/local/etc/bzdev/scrunner.conf");
		if (cf.canRead()) {
		    return "/usr/local/etc/bzdev/scrunner.conf";
		}
	    } else {
		File cf = new File("/etc/bzdev/scrunner.conf");
		if (cf.canRead()) {
		    return "/etc/bzdev/scrunner.conf";
		}
	    }
	    return null;
	} else {
	    // for Windows or other operating systems that do
	    // not look like Unix, assume the configuration file
	    // is in the same directory as the jar files.
	    try {
		File fp = new File
		    (FactoryPrinterCmd.class.getProtectionDomain()
		     .getCodeSource().getLocation().toURI());
		fp = fp.getParentFile();
		return new File(fp, "scrunner.conf").getCanonicalPath();
		
	    } catch (Exception e) {
		return null;
	    }
	}
    }

    static String defaultUsrConfigFile() {
	String fsep = System.getProperty("file.separator");
	if (fsep.equals("/")) {
	    // Unix file separator. Try to find it in /etc
	    // or /etc/opt ; return null otherwise as the
	    // location is unknown.
	    return System.getProperty("user.home")
		+ "/.config/bzdev/scrunner.conf";
	} else {
	    // For any other OS (primarily Windows in practice)
	    // assume there is only a system config file. A startup
	    // script can always define scrunner.usrconf to set the
	    // file explicitly.
	    return null;
	}
    }

    public static void main(String argv[]) throws Exception {
	int index = 0;
	String initialClassPath = System.getProperty("java.class.path");
	String mainModule = System.getProperty("jdk.module.main");
	String languageName = null;

	if (initialClassPath != null) {
	    for (String comp: initialClassPath.split(pathSeparator)) {
		extendCodebase(comp, false);
	    }
	    // sbcp.append(initialClassPath);
	}
	String initialModulePath = System.getProperty("jdk.module.path");
	if (initialModulePath != null) {
	    for (String comp: initialModulePath.split(pathSeparator)) {
		extendCodebase(comp, true);
	    }
	    // sbmp.append(initialModulePath);
	}

	String policyFile = System.getProperty("java.security.policy");

	String sysConfigFileName = System.getProperty("scrunner.sysconf");
	if (sysConfigFileName == null) {
	    sysConfigFileName = defaultSysConfigFile();
	    if (sysConfigFileName != null) {
		System.setProperty("scrunner.sysconf", sysConfigFileName);
	    }
	} else if (sysConfigFileName.equals("-none-")) {
	    sysConfigFileName = null;
	    System.clearProperty("scrunner.sysconf");
	}

	String usrConfigFileName = System.getProperty("scrunner.usrconf");
	if (usrConfigFileName == null) {
	    usrConfigFileName = defaultUsrConfigFile();
	    if (usrConfigFileName != null) {
		System.setProperty("scrunner.usrconf", usrConfigFileName);
	    }
	} else if (usrConfigFileName.equals("-none-")) {
	    usrConfigFileName = null;
	    System.clearProperty("scrunner.usrconf");
	}


	File f = new File(FactoryPrinterCmd.class.getProtectionDomain()
			  .getCodeSource().getLocation().toURI());
	String jarfile = f.getCanonicalPath();

	readConfigFiles(null);
	LinkedList<String> iargs = new LinkedList<String>();

	iargs.addFirst(javacmd);
	if (sysConfigFileName != null) {
	    iargs.addFirst("-Dscrunner.sysconf=" + sysConfigFileName);
	}
	if (usrConfigFileName != null) {
	    iargs.addFirst("-Dscrunner.usrconf=" + usrConfigFileName);
	}
	if (System.getProperty("java.ext.dirs") != null) {
	    iargs.add("-Djava.ext.dirs=" +
		      System.getProperty("java.ext.dirs"));
	}

	LinkedList<String> args = new LinkedList<String>();
	boolean dryrun = false;
	boolean listCodeBase = false;
	while(index < argv.length && argv[index].startsWith("-")) {
	    String arg = argv[index];
	    if (arg.equals("--")) {
		args.add(arg);
		index++;
		break;
	    } else if (arg.equals("--add-modules")) {
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		String[] modules = argv[index].trim().split(",");
		for (String mod: modules) {
		    if (modSet.contains(argv[index]) == false) {
			if (sbmod.length() > 0) {
			    sbmod.append(",");
			}
			sbmod.append(mod);
			modSet.add(mod);
		    }
		}
		// if (sbmod.length() > 0) sbmod.append(",");
		// sbmod.append(argv[index]);
	    } else if (arg.equals("--codebase")) {
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		// We keep this in the arg list only for the
		// --listCodebase option
		arg = argv[index];
		args.add(arg);
		extendCodebase(arg, true);
	    } else if (arg.equals("--classpathCodebase")) {
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		// We keep this in the arg list only for the
		// --listCodebase option
		arg = argv[index];
		args.add(arg);
		extendCodebase(arg, false);
	    } else if (arg.equals("--module-path") || arg.equals("-p")) {
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		arg = argv[index].trim();
		args.add(arg);
		String[] mcomps = arg.split(pathSeparator);
		for (String mcomp: mcomps) {
		    extendCodebase(mcomp, true);
		}
		// if (sbmp.length() > 0) sbmp.append(":");
		// sbmp.append(argv[index]);
	    } else if (arg.equals("-L")) {
		if (classpathExtended
		    && System.getProperty("lsnof.started") == null) {
		    // restart
		    List<String> arglist = new LinkedList<String>();
		    arglist.add(javacmd);
		    arglist.add("-Dlsnof.started=true");
		    if (policyFile != null) {
			arglist.add("-Djava.security.policy=" + policyFile);
		    }
		    if (System.getProperty("scrunner.sysconf") != null) {
			arglist.add("-Dscrunner.sysconf=" +
				    System.getProperty("scrunner.sysconf"));
		    }
		    if (System.getProperty("scrunner.usrconf") != null) {
			arglist.add("-Dscrunner.usrconf=" +
				    System.getProperty("scrunner.usrconf"));
		    }
		    if (System.getProperty("java.security.manager") != null) {
			arglist.add("-Djava.security.manager=" +
				    System.getProperty
				    ("java.security.manager"));
		    }
		    if (System.getProperty("java.ext.dirs") != null) {
			arglist.add("-Djava.ext.dirs=" +
				    System.getProperty("java.ext.dirs"));
		    }
		    if (sbmp.length() > 0) {
			arglist.add("-p");
			arglist.add(sbmp.toString());
		    }
		    if (sbcp.length() > 0) {
			arglist.add("-classpath");
			arglist.add(sbcp.toString());
		    }
		    if (sbmp.length() > 0) {
			arglist.add("-m");
			arglist.add("org.bzdev.lsnof/"
				+ "org.bzdev.bin.lsnof.FactoryPrinterCmd");
		    } else {
			arglist.add("org.bzdev.bin.lsnof.FactoryPrinterCmd");
		    }
		    for (String s: argv) {
			arglist.add(s);
		    }
		    ProcessBuilder cppb = new ProcessBuilder(arglist);
		    cppb.inheritIO();
		    try {
			Process proc = cppb.start();
			System.exit(proc.waitFor());
		    } catch (Exception e) {
			System.err.println
			    (errorMsg("scException", e.getMessage()));
			System.exit(1);
		    }
		}
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		    /*
		    System.err.println("missing argument for "
				       + argv[index-1]);
		    */
		}
		arg = argv[index];
		args.add(arg);
		languageName = arg;
		if (!Scripting.supportsLanguage(languageName)) {
		    String ln = Scripting.getLanguageNameByAlias(languageName);
		    if (ln == null) {
			String msg =
			    (errorMsg("badScriptingLanguageName",languageName));
			System.err.println(msg);
			System.exit(1);
		    } else {
			languageName = ln;
		    }
		}
	    } else if (arg.equals("-L") || arg.equals("-d")
		       || arg.equals("--baseURL")
		       || arg.equals("--charset")
		       ||arg.equals("--tempateResource")
		       || arg.equals("--templateFile")
		       || arg.equals("--templateURL")
		       || arg.equals("--link")
		       || arg.equals("--menuFile")
		       || arg.equals("--overview")
		       || arg.equals("--target")) {
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		    /*
		    System.err.println("missing argument for "
				       + argv[index-1]);
		    */
		}
		arg = argv[index];
		args.add(arg);
	    } else if (arg.equals("--link-offline")) {
		// case where an option requires two arguments.
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		arg = argv[index];
		args.add(arg);
		index++;
		if (index == argv.length) {
		    System.err.println(errorMsg("missingArg", argv[index-1]));
		}
		arg = argv[index];
		args.add(arg);
	    } else if (arg.startsWith("-J")) {
		arg = arg.substring(2);
		if (arg.length() != 0) {
		    iargs.addFirst(arg);
		}
	    } else if (argv[index].equals("--dryrun")) {
	    	dryrun = true;
	    } else if (argv[index].equals("--listCodeBase")) {
		listCodeBase = true;
	    } else if (arg.startsWith("-")) {
		args.add(arg);
	    }
	    index++;
	}
	if (languageName != null) {
	    readConfigFiles(languageName);
	}

	if (policyFile != null) {
	    iargs.addFirst("-Djava.security.policy="+policyFile);
	}

	if (sbmp.length() > 0) {
	    iargs.addFirst("-p");
	    iargs.addFirst(sbmp.toString());
	}
	if (sbmod.length() > 0) {
	    iargs.addFirst("--add-modules");
	    iargs.addFirst(sbmod.toString());
	}
	if (sbcp.length() > 0) {
	    iargs.addFirst("-classpath");
	    iargs.addFirst(sbcp.toString());
	}
	if (sbmp.length() > 0) {
	    iargs.addFirst("-m");
	    iargs.addFirst
		("org.bzdev.lsnof/org.bzdev.bin.lsnof.FactoryPrinter");
	} else {
	    iargs.addFirst("org.bzdev.bin.lsnof.FactoryPrinter");
	}
	while (index < argv.length) {
	    args.add(argv[index]);
	    index++;
	}
	if (listCodeBase) {
	    // we want this before the other args, particularly
	    // the codebase args.
	    args.addFirst("--listCodeBase");
	}
	for (String arg: iargs) {
	    args.addFirst(arg);
	}

	if (dryrun) {
	    boolean notFirst = false;
	    for (String arg: args) {
		if (notFirst) {
		    System.out.print(" ");
		} else {
		    notFirst = true;
		}
		String escape = "\\\\";
		if (System.getProperty("os.name").startsWith("Windows")) {
		    escape = "^";
		    arg = arg.replaceAll("\\^", "^^");
		}
		arg = arg.replaceAll("(\\s|['\"(){};&$]|\\[|\\]|\\\\|\\|)",
				     escape + "$1");
		System.out.print(arg);
	    }
	    System.out.println();
	    System.exit(0);
	}

	ProcessBuilder pb = new ProcessBuilder(args);
	pb.inheritIO();
	try {
	    Process process = pb.start();
	    System.exit(process.waitFor());
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }
}
