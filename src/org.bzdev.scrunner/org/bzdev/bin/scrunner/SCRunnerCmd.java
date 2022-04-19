package org.bzdev.bin.scrunner;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.scripting.Scripting;
import org.bzdev.net.URLClassLoaderOps;
import org.bzdev.net.URLPathParser;
import org.bzdev.util.CopyUtilities;

//@exbundle org.bzdev.bin.scrunner.lpack.SCRunner

/**
 * Program to generate a command to run.
 */
public class SCRunnerCmd {

    static String errorMsg(String key, Object... args) {
	return SCRunner.errorMsg(key, args);
    }

    /*
    static enum EscapeMode {
	NONE,
	SH,
	DOS
    }

    private static EscapeMode escapeMode = EscapeMode.NONE; 
    */
    private static boolean listScriptingLanguages = false;

    private static final String pathSeparator =
	System.getProperty("path.separator");

    private static String javacmd = "java";

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

    private static Properties defs = new Properties();

    private static StringBuilder sbmp = new StringBuilder();
    private static StringBuilder sbcp = new StringBuilder();
    private static StringBuilder sbmod = new StringBuilder();
    private static List<String> sbjops = new LinkedList<String>();
    private static List<String> sbcmd = new LinkedList<String>();
    private static StringBuilder sbrp = new StringBuilder();


    private static String blockName(int mode) {
	switch(mode) {
	case 0: return "top-level";
	case 1: return "definition";
	case 2: return "class-path component";
	default: return "<unknown>";
	}
    }

    private static String getExtension(String pathname) throws Exception {
	File file = new File(pathname);
	if (!file.isFile()) {
	    throw new Exception("\"" + pathname  + "\" - not a normal file");
	}
	if (!file.canRead()) {
	    throw new Exception("\"" + pathname + "\" - not readable");
	}

	String name = file.getName();
	int index = name.lastIndexOf('.');
	if (index++ == 0 || index == name.length()) {
	    return null;
	}
	return name.substring(index);
    }

    private static boolean propertyNotAllowed(String name) {
	if (System.getProperty(name) != null) return true;
	if (name.equals("java.system.class.loader")
	    || name.equals("java.security.manager")
	    || name.equals("scrunner.started")
	    || name.equals("scrunner.sysconf")
	    || name.equals("scrunner.usrconf")) {
	    return true;
	}
	return false;
    }

    private static boolean classpathExtended = false;

    /*
    private static String MODULE_NAME_RE =
	"^[a-zA-Z_$][a-zA-Z0-9_$]*([.][a-zA-Z_$][a-zA-Z0-9_$]*)*$";
    */

    private static String MODULE_NAME_RE =
	"^([\\p{L}_$][\\p{L}\\p{N}_$]*)([.][\\p{L}_$][\\p{L}\\p{N}_$]*)*$";

    private static Set<String> modSet = new HashSet<>();
    private static Map<String,Boolean> urlMap = new HashMap<>();
    private static Set<String>resourcepathSet = new HashSet<>();

    private static void readConfigFiles(String languageName, String fileName) {
	File file = new File (fileName);
	if (!file.exists()) {
	    return;
	}
	if (!file.isFile()) {
	    System.err.println(errorMsg("notConfigFile", fileName));
	    System.exit(1);
	}

	if (!file.canRead()) {
	    System.err.println(errorMsg("configFileNotReadable", fileName));
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
			}
			if (langsearch || langsection) {
			    String[] parts = line.split("\\s*=\\s*", 2);
			    if (parts.length != 2) {
				int lno = reader.getLineNumber();
				String msg =
				    errorMsg("syntax", fileName, lno);
				System.err.println(msg);
				error = true;
			    } else {
				String name = parts[0];
				if (propertyNotAllowed(name) &&
				    !name.equals("java.ext.dirs")) {
				    // do not override standard Java properties
				    int lno = reader.getLineNumber();
				    String msg =
					errorMsg("propname",fileName,lno,name);
				    System.err.println(msg);
				    error = true;
				    continue;
				}
				String value = parts[1];
				if (name.equals("java.ext.dirs")) {
				    StringBuilder nv = new StringBuilder();
				    int cnt = 0;
				    for (String val:
					     value.split(Pattern.quote
							 (pathSeparator))) {
					if (val.startsWith("~/")) {
					    val =
						System.getProperty("user.home")
						+ val.substring(1);
					} else if (val.equals("~")) {
					    val =
						System.getProperty("user.home");
					} else if (val.startsWith("~~")) {
					    val = val.substring(1);
					}
					nv.append(val);
					if ((cnt++) > 0) {
					    nv.append(pathSeparator);
					}
				    }
				    value = nv.toString();
				} else {
				    if (value.startsWith("~/")) {
					value = System.getProperty("user.home")
					    + value.substring(1);
				    } else if (value.equals("~")) {
					value = System.getProperty("user.home");
				    } else if (value.startsWith("~~")) {
					value = value.substring(1);
				    }
				}
				defs.setProperty(name, value);
			    }
			}
			break;
		    case 2: // classpath components
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
			}
			if (langsearch || langsection) {
			    if (line.startsWith(tildeSep)) {
				line = System.getProperty("user.home")
				    + line.substring(1);
			    } else if (line.equals("~")) {
				line = System.getProperty("user.home");
			    } else if (line.startsWith("~~")) {
				line = line.substring(1);
			    } else if (line.equals(dotDotDot)) {
				line = ourCodebaseDir;
			    } else if (line.startsWith(dotDotDotSep)) {
				// replace the "..." with a directory name
				line = ourCodebaseDir
				    + File.separator + line.substring(4);
			    }
			    if (languageName == null || langsection) {
				// occurs during the first pass where
				// we have to make these files and directories
				// available so that we can get data about
				// scripting languages.
				try {
				    URL url;
				    if (line.startsWith("file:")) {
					url = new URL(line);
				    } else {
					url = (new File(line)).toURI().toURL();
				    }
				    extendCodebase(url.toString(), modulePath);
				    classpathExtended = true;
				} catch (Exception e) {
				    int lno = reader.getLineNumber();
				    String msg =
					errorMsg("urlParse", fileName, lno);
				    System.err.println(msg);
				    System.exit(1);
				}
			    } /* else {
				if (sbcp.length() > 0) {
				    sbcp.append(pathSeparator);
				}
				sbcp.append((line));
			    } */
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
	    } catch (Exception e) {
		int lno = reader.getLineNumber();
		String msg =
		    errorMsg("exceptionMsg", fileName, lno, e.getMessage());
		System.err.println(msg);
		System.exit(1);
	    }
	    reader.close();
	} catch (Exception er) {
	    System.err.println(errorMsg("scException", er.getMessage()));
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
	/*
	if (defs != null) {
	    for (String name: defs.stringPropertyNames()) {
		properties.setProperty(name, defs.getProperty(name));
	    }
	    for (String name: properties.stringPropertyNames()) {
		System.out.println("-D" + name + "=" 
				   + properties.getProperty(name));
	    }
	}
	*/
    }

    /*
    private static String escapeForShell(String string) {
	String result;
	switch(escapeMode) {
	case SH:
	    return string.replaceAll
		("([$<>|&;#{}()'`\"*?]|\\p{Space}|\\\\|\\[|\\])", "\\\\$1");
	case DOS:
	    string = string.replaceAll("(\\p{Space})", "\"$1\"");
	    string = string.replaceAll("%","%%");
	    string = string.replaceAll("([\"|><*?&^()'`,;])", "^$1");
	    return string.replaceAll("(\\\\)", "\\$1");
	default:
	    return string;
	}
    }
    */

    static String defaultSysConfigFile() {
	String fsep = System.getProperty("file.separator");
	if (fsep.equals("/")) {
	    // Unix file separator. Try to find it in /etc
	    // or /etc/opt ; return null otherwise as the
	    // location is unknown.
	    Path p;
	    try {
		p = Paths.get(SCRunnerCmd.class.getProtectionDomain()
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
		    (SCRunnerCmd.class.getProtectionDomain()
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
	    // Unix file separator. The file is in the user's
	    // home directory in the  .config/bzdev subdirectory.
	    // (This defines the location of the file - it is
	    // not required to actually exist).
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

    static void extendResourcePath(String entry, Appendable err)
	throws IOException
    {
	String url;
	try {
	    if (entry.startsWith("file:")
		|| entry.startsWith("jar:")
		|| entry.startsWith("http:")
		|| entry.startsWith("https:")
		|| entry.startsWith("ftp:")) {
		url = entry;
	    } else {
		url = new File(entry).getCanonicalFile().toURI().toURL()
		    .toString();
	    }

	    if (sbrp.length() > 0) {
		sbrp.append("|");
	    }
	    sbrp.append(url);
	} catch (IOException e) {
	    err.append
		(errorMsg("resourcePathError", e.getMessage()) + "\n");
	    // System.exit(1);
	    throw e;
	}
    }



    static void extendCodebase(String codebase) {
	extendCodebase(codebase, true);
    }

    static void extendCodebase(String codebase, boolean modulePath) {
	try {
	    URL[] urls = URLPathParser.getURLs(null, codebase,
					       ourCodebaseDir,
					       System.err);
	    for (URL url: urls) {
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
		    // CopyUtilities.copyStream(is, os);
		    try {
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
	    String msg = errorMsg("codebaseError", codebase, e.getMessage());
	    System.err.append(msg + "\n");
	    System.exit(1);
	}
    }


    public static void main(String argv[]) {
	// forbid recursive calls (e.g. when SCRunner loads classes from a
	// third party)
	CheckOncePerJVM.check();

	String initialClassPath = System.getProperty("java.class.path");
	String mainModule = System.getProperty("jdk.module.main");

	if (initialClassPath != null) {
	    String[] ccomps = initialClassPath.split(pathSeparator);
	    for (String comp: ccomps) {
		extendCodebase(comp, false);
	    }
	    // sbcp.append(initialClassPath);
	}
	String initialModulePath = System.getProperty("jdk.module.path");
	if (initialModulePath != null) {
	    String[] mcomps = initialModulePath.split(pathSeparator);
	    for (String comp: mcomps) {
		extendCodebase(comp, true);
	    }
	}

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

	// first arg indicates the shell for which a command is to
	// be generated

	if (argv.length > 0 && argv[0].equals("--listScriptingLanguages")) {
	    listScriptingLanguages = true;  
	}
	int index = 0;
	// sbcp.append((System.getProperty("java.class.path")));

	// These two are the defaults set by the shell script that
	// starts the program.  Of the two, only the policyFile can be
	// changed.
	String policyFile = System.getProperty("java.security.policy");
	if (policyFile != null) {
	    defs.setProperty("java.security.policy", policyFile);
	}

	String classLoader = System.getProperty("java.system.class.loader");
	if (classLoader != null  && !classLoader.equals("-none-")) {
	    defs.setProperty("java.system.class.loader", classLoader);
	} else {
	    /*
	    defs.setProperty("java.system.class.loader",
			     "org.bzdev.lang.DMClassLoader");
	    */
	}
	// need to extend class path because scripting-language-independent
	// class path entries in the configuration files may be needed for
	// scripting languages to be recognized.
	readConfigFiles(null);

	if (System.getProperty("scrunner.started") == null) {
	    // If we extended the class path  in the call to readConfigFiles,
	    // we have to restart, which will read the config file again,
	    // so we also set the property scrunner.started, which of course
	    // cannot appear in the config files.
	    if (classpathExtended) {
		List<String> arglist = new LinkedList<String>();
		arglist.add(javacmd);
		arglist.add("-Dscrunner.started=true");
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
				System.getProperty("java.security.manager"));
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
		    arglist.add("org.bzdev.scrunner/"
				+ "org.bzdev.bin.scrunner.SCRunnerCmd");
		} else {
		    arglist.add("org.bzdev.bin.scrunner.SCRunnerCmd");
		}
		for (String arg: argv) {
		    arglist.add(arg);
		}
		ProcessBuilder cppb = new ProcessBuilder(arglist);
		cppb.inheritIO();
		try {
		    Process proc = cppb.start();
		    System.exit(proc.waitFor());
		} catch (Exception e) {
		    System.err.println(errorMsg("scException", e.getMessage()));
		    System.exit(1);
		}
	    }
	}

	if (listScriptingLanguages) {
	    List<String> slArgList = new LinkedList<String>();
	    // slArgList.add("echo");
	    slArgList.add(javacmd);
	    for (String name: defs.stringPropertyNames()) {
		slArgList.add("-D" + name + "=" + defs.getProperty(name));
	    }
	    if (sbmp.length() > 0) {
		slArgList.add("-p");
		slArgList.add(sbmp.toString());
	    }

	    if (sbmod.length() > 0) {
		slArgList.add("--add-modules");
		slArgList.add(sbmod.toString());
	    }

	    if (sbcp.length() > 0) {
		slArgList.add("-classpath");
		slArgList.add(sbcp.toString());
	    }
	    if (sbmp.length() > 0) {
		slArgList.add("-m");
		slArgList.add("org.bzdev.scrunner/org.bzdev.bin.scrunner"
			      + ".ListScriptingLangs");
	    } else {
		slArgList.add("org.bzdev.bin.scrunner.ListScriptingLangs");
	    }
	    while ((++index) < argv.length) {
		slArgList.add(argv[index]);
	    }
	    ProcessBuilder lslpb = new ProcessBuilder(slArgList);
	    lslpb.inheritIO();
	    try {
		Process lslp = lslpb.start();
		System.exit(lslp.waitFor());
	    } catch (Exception e) {
		System.err.println(errorMsg("scException", e.getMessage()));
		System.exit(1);
	    }
	}

	String languageName = null;
	index = 0;
	String script = (argv[index].startsWith("-")
			 && !argv[index].equals("-"))?
	    null: argv[index++];
	// scan ahead to find the language
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
	    } else if (argv[index].equals("-t")) {
		index++; break;
	    } else if (argv[index].equals("-o")) {
		index++;
	    } else if (argv[index].equals("--plaf")) {
		index++;
	    } else if (argv[index].startsWith("-o:")
		       || argv[index].startsWith("-i:")
		       || argv[index].startsWith("-d:")) {
		String varName = argv[index].substring(3);
		int ind = varName.indexOf(':');
		if (ind < 0) index++;
	    } else if (argv[index].startsWith("-v")) {
		if (argv[index].length() >= 3 && argv[index].charAt(3) == ':') {
		    String rest = argv[index].substring(4);
		    if (rest.indexOf(":") < 0) {
			index++;
		    }
		}
	    } else if (argv[index].startsWith("-r")) {
		int ind = argv[index].indexOf(':');
		if (ind > 0) {
		    String string = argv[index].substring(0, ind);
		    ind = string.indexOf(':');
		    if (ind < 0) {
			index++;
		    }
		}
	    } else if (argv[index].equals("-L")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		languageName = argv[index];
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
	    } else if (argv[index].equals("-p")
		       || argv[index].equals("--module-path")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		String[] mcomps = argv[index].trim().split(pathSeparator);
		for (String mcomp: mcomps) {
		    extendCodebase(mcomp, true);
		}
	    } else if (argv[index].equals("--add-modules")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
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
	    } else if (argv[index].equals("--codebase")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		extendCodebase(argv[index], true);
	    } else if (argv[index].equals("--classpathCodebase")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		extendCodebase(argv[index], false);
	    } else if (argv[index].equals("--resourcePath")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
	    } else  if (argv[index].equals("--supportsLanguage")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
	    }
	    index++;
	}

	if (languageName == null) {
	    String extension = null;
	    while (index < argv.length && extension == null) {
		if (!argv[index].equals("-t")) {
		    if (extension == null) {
			try {
			    extension = getExtension(argv[index]);
			} catch (Exception e) {
			    String msg =
				errorMsg("scException", e.getMessage());
			    System.err.println(msg);
			    System.exit(1);
			}
		    }
		}
		index++;
	    }
	    if (extension == null) {
		languageName = "ECMAScript";
	    } else {
		languageName = Scripting.getLanguageNameByExtension(extension);
	    }
	}

	if (defs.getProperty("java.security.policy") == null) {
	    try {
		File pf = new File(SCRunnerCmd.class.getProtectionDomain()
				   .getCodeSource().getLocation().toURI());
		pf = pf.getParentFile();
		defs.setProperty("java.security.policy",
				 new File(pf, "libbzdev.policy")
				 .getCanonicalPath());
	    } catch (Exception eio) {
		System.err.println(errorMsg("policyFile"));
		System.exit(1);
	    }
	}

	List<String> argList = new LinkedList<String>();
	readConfigFiles(languageName);
	// argList.add("echo");
	argList.add(javacmd);
	/*
	argList.add("-classpath");
	argList.add(sbcp.toString());
	*/
	index = (script == null)? 0: 1;
	boolean dryrun = false;
	boolean listCodeBase = false;
	while (index < argv.length && argv[index].startsWith("-")
	       && !argv[index].equals("-t")) {
	    if (argv[index].equals("-")) {
		// This is a special case.  The argument "-" indicates
		// standard input and should be treated as a file-name
		// argument.  In this case, a preceding "--" argument
		// is not needed for the option to be unambiguous. The
		// index is not incremented.
		break;
	    } else if (argv[index].equals("--stackTrace")) {
		sbcmd.add("--stackTrace");
	    } else if (argv[index].equals("--")) {
		sbcmd.add("--");
		index++;
		break;
	    } else if (argv[index].equals("-o")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		sbcmd.add("-o");
		sbcmd.add((argv[index]));
	    } else if (argv[index].equals("--plaf")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		sbcmd.add("--plaf");
		sbcmd.add(argv[index]);
	    } else if (argv[index].startsWith("-o:")
		       || argv[index].startsWith("-i:")
		       || argv[index].startsWith("-d:")) {
		String varName = argv[index].substring(3);
		int ind = varName.indexOf(':');
		if (ind < 0) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[--index]));
			System.exit(1);
		    }
		    sbcmd.add((argv[index-1]));
		    sbcmd.add((argv[index]));
		} else {
		    sbcmd.add((argv[index]));
		}
	    } else if (argv[index].startsWith("-v")) {
		if (argv[index].startsWith("-vS:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    if (ind < 0) {
			index++;
			if (index == argv.length) {
			    System.err.println
				(errorMsg("missingArg", argv[--index]));
			    System.exit(1);
			}
			sbcmd.add((argv[index-1]));
			sbcmd.add((argv[index]));
		    } else {
			sbcmd.add((argv[index]));
		    }
		} else if (argv[index].startsWith("-vB:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    if (ind < 0) {
			index++;
			if (index == argv.length) {
			    System.err.println
				(errorMsg("missingArg", argv[--index]));
			    System.exit(1);
			}
			sbcmd.add((argv[index-1]));
			sbcmd.add((argv[index]));
		    } else {
			sbcmd.add((argv[index]));
		    }
		} else if (argv[index].startsWith("-vL:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    if (ind < 0) {
			index++;
			if (index == argv.length) {
			    System.err.println
				(errorMsg("missingArg", argv[--index]));
			    System.exit(1);
			}
			sbcmd.add((argv[index-1]));
			sbcmd.add((argv[index]));
		    } else {
			sbcmd.add((argv[index]));
		    }
		} else 	if (argv[index].startsWith("-vI:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    if (ind < 0) {
			index++;
			if (index == argv.length) {
			    System.err.println
				(errorMsg("missingArg", argv[--index]));
			    System.exit(1);
			}
			sbcmd.add((argv[index-1]));
			sbcmd.add((argv[index]));
		    } else {
			sbcmd.add((argv[index]));
		    }
		} else 	if (argv[index].startsWith("-vD:")) {
		    String varName = argv[index].substring(4);
		    int ind = varName.indexOf(':');
		    if (ind < 0) {
			index++;
			if (index == argv.length) {
			    System.err.println
				(errorMsg("missingArg", argv[--index]));
			    System.exit(1);
			}
			sbcmd.add((argv[index-1]));
			sbcmd.add((argv[index]));
		    } else {
			sbcmd.add((argv[index]));
		    }
		} else {
		    String msg = errorMsg("unknownTypeCode", argv[index]);
		    System.err.println(msg);
		    System.exit(1);
		}
	    } else if (argv[index].equals("--trustLevel=0")) {
		sbcmd.add("--trustLevel=0");
	    } else if (argv[index].equals("--trustLevel=1")) {
		sbcmd.add("--trustLevel=1");
	    } else if (argv[index].equals("--trustLevel=2")) {
		sbcmd.add("--trustLevel=2");
	    } else if (argv[index].equals("-r")) {
		sbcmd.add("-r");
	    } else if (argv[index].startsWith("-r")) {
		int ind = argv[index].indexOf(':');
		if (ind < -1) {
		    System.err.println("scrunner: bad argument \""
				       + argv[index] +"\"");
		    System.exit(1);
		}
		String string = argv[index].substring(ind);
		ind = string.indexOf(':');
		if (ind < 0) {
		    index++;
		    if (index == argv.length) {
			System.err.println
			    (errorMsg("missingArg", argv[--index]));
			System.exit(1);
		    }
		    sbcmd.add((argv[index-1]));
		    sbcmd.add((argv[index]));
		} else {
		    sbcmd.add((argv[index]));
		}
	    } else if (argv[index].equals("-L")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		languageName = argv[index];
		sbcmd.add("-L");
		sbcmd.add(languageName);
	    } else if (argv[index].startsWith("-D") ||
		       argv[index].startsWith("-J-D")) {
		// These -D arguments are provided *after*
		// org.bzdev.bin.scrunner.SCRunnerCmd appears on the
		// java command line, but will be put *before*
		// org.bzdev.bin.scrunner.SCRunner appears on the java
		// command line that this program generates
		String arg = argv[index];
		if (arg.startsWith("-J")) arg = arg.substring(2);
		int ind = arg.indexOf('=');
		if (ind < 0) {
		    System.err.println("scrunner: bad argument \""
				       + arg + "\"");
		    System.exit(1);
		}
		String[] pair = new String[2];
		pair[0] = arg.substring(2, ind);
		pair[1] = arg.substring(ind+1);
		String name = pair[0];
		String value = pair[1];
		if (propertyNotAllowed(name)) {
		    System.err.println("scrunner: bad argument "
				       +"(cannot set property \"" 
				       + name + "\")");
		    System.exit(1);
		}
		defs.setProperty(name, value);
	    } else if (argv[index].equals("--codebase")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		// extendCodebase(argv[index]);
		sbcmd.add("--codebase");
		sbcmd.add((argv[index]));
	    } else if (argv[index].equals("--classpathCodebase")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		sbcmd.add("--classpathCodebase");
		sbcmd.add(argv[index]);
	    } else if (argv[index].equals("--resourcePath")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		try {
		    for (URL rp: URLPathParser.getURLs(argv[index]) ){
			String rpname = rp.toString();
			if (!resourcepathSet.contains(rpname)) {
			    extendResourcePath(rpname, System.err);
			    resourcepathSet.add(rpname);
			}
		    }
		} catch (IOException urle) {
		    System.err.println(errorMsg("badPath", argv[index]));
		    System.exit(1);
		}
	    } else if (argv[index].startsWith("-J")) {
		String substring = argv[index].substring(2);
		sbjops.add(substring);
	    } else if (argv[index].equals("--dryrun")) {
		dryrun = true;
	    } else if (argv[index].equals("--versions")) {
		sbcmd.add("--versions");
	    } else if (argv[index].equals("--exit")) {
		sbcmd.add("--exit");
	    } else if (argv[index].equals("--listCodeBase")) {
		listCodeBase = true;
	    } else if (argv[index].equals("-p")
		       || argv[index].equals("--module-path")
		       || argv[index].equals("--add-modules")) {
		// we processed these cases before
		index++;
	    } else if (argv[index].equals("--supportsLanguage")) {
		index++;
		if (index == argv.length) {
		    System.err.println
			(errorMsg("missingArg", argv[--index]));
		    System.exit(1);
		}
		sbcmd.add("--supportsLanguage");
		sbcmd.add(argv[index]);
	    } else {
		String msg = errorMsg("unknownCmdOption", argv[index]);
		System.err.println(msg);
		System.exit(1);
	    }
	    index++;
	}
	argList.addAll(sbjops);
		    
	if (sbrp.length() > 0) {
	    String resources = defs
		.getProperty("org.bzdev.protocols.resource.path");
	    resources = (resources == null)? sbrp.toString():
		resources + "|" + sbrp.toString();
	    defs.setProperty("org.bzdev.protocols.resource.path", resources);
	}
	for (String name: defs.stringPropertyNames()) {
	    argList.add("-D" + (name) + "="
			   + (defs.getProperty(name)));
	}

	if (sbmp.length() > 0) {
	    argList.add("-p");
	    argList.add(sbmp.toString());
	}
	
	if (sbmod.length() > 0) {
	    argList.add("--add-modules");
	    argList.add(sbmod.toString());
	}

	if (sbcp.length() > 0) {
	    argList.add("-classpath");
	    argList.add(sbcp.toString());
	}

	if (sbmp.length() > 0) {
	    argList.add("-m");
	    argList.add("org.bzdev.scrunner/org.bzdev.bin.scrunner.SCRunner");
	} else {
	    argList.add("org.bzdev.bin.scrunner.SCRunner");
	}
	if (listCodeBase) {
	    argList.add("--listCodeBase");
	}
	argList.addAll(sbcmd);

	if (script != null) argList.add(script);
	while (index < argv.length) {
	    argList.add((argv[index]));
	    index++;
	}

	if (dryrun) {
	    boolean notFirst = false;
	    for (String arg: argList) {
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

	ProcessBuilder pb = new ProcessBuilder(argList);
	pb.inheritIO();
	try {
	    Process process = pb.start();
	    System.exit(process.waitFor());
	} catch (Exception ee) {
	    System.err.println(errorMsg("scException", ee.getMessage()));
	    System.exit(1);
	}
    }
}
