import java.util.*;
import java.util.regex.*;
import org.bzdev.util.SafeFormatter;

public class BundleTest {

    static int errcount = 0;
    static boolean terse = false;
    static boolean namesPrinted = false;

    public static void checkForErrors() throws Exception {
	if (errcount != 0) throw new Exception("Errors detected");
    }

    static Locale clocale = new Locale("");
    static Locale locale = clocale;

    public static void setLocale(String lang, String country, String variant) {
	if (variant == null) {
	    if (country == null) {
		if (lang != null) {
		    locale = new Locale(lang);
		} else {
		    locale = clocale;
		}
	    } else if (lang != null) {
		locale = new Locale(lang, country);
	    } else {
		locale = clocale;
	    }
	} else if (lang != null && country != null) {
	    locale = new Locale(lang, country, variant);
	} else {
	    locale = clocale;
	}
	if (locale == clocale)
	    System.out.println("No alternative locale configured for test");
	else System.out.println("Alternative locale = " + locale.toString());
    }

    static String bundleName = null;
    static String fileName = null;
    static ResourceBundle bundle1 = null;
    static ResourceBundle bundle2 = null;

    public static void setBundle(String name, String file) {
	namesPrinted = false;
	if (!terse || printExample) {
	    System.out.println("BUNDLE = " + name);
	    System.out.println("FILE = " + file);
	    namesPrinted = true;
	}
	bundle1 = null;
	bundle2 = null;
	bundleName = name;
	fileName = file;
	bundle1 = ResourceBundle.getBundle(name, clocale);
	if(clocale.equals(locale)) {
	    bundle2 = null;
	} else {
	    bundle2 = ResourceBundle.getBundle(name, locale);
	}
    }

    static String format1 = null;
    static String format2 = null;

    static boolean printExample = false;

    static public void setTerse(boolean mode) {
	terse = mode;
    }

    static public void setVerbose(boolean mode) {
	printExample = mode;
    }

    // change all formats to %s or %N$s because the tests do not
    // allow us to determine the %g cases

    static String modify(String format) {
	return SafeFormatter.modify(format);
    }

    static int getDirectiveCount(String format) {
	return SafeFormatter.getDirectiveCount(format);
    }


   static void getFormats(String key) {
	try {
	    format1 = null;
	    format2 = null;
	    format1 = bundle1.getString(key);
	    format2 = (bundle2 == null)? null: bundle2.getString(key);
	} catch (MissingResourceException mre) {
	    if (terse && !namesPrinted) {
		System.out.println("BUNDLE = " + bundleName);
		System.out.println("FILE = " + fileName);
		namesPrinted = true;
	    }
	    System.out.println("*** No format for key "  + key);
	    errcount++;
	} catch (ClassCastException cce) {
	    if (terse && !namesPrinted) {
		System.out.println("BUNDLE = " + bundleName);
		System.out.println("FILE = " + fileName);
		namesPrinted = true;
	    }
	    System.out.println("*** No format for key "  + key
			       + " (key's value was not a string)");
	    errcount++;
	}
    }

    static void handleErrorMsg(String key, Object... args) {
	getFormats(key);
	if (format2 != null && printExample) System.out.println("-----------");
	if (format1 != null) {
	    if (args.length != getDirectiveCount(format1)) {
		if (terse && !namesPrinted) {
		    System.out.println("BUNDLE = " + bundleName);
		    System.out.println("FILE = " + fileName);
		    namesPrinted = true;
		}
		System.out.println("*** for key \"" + key + "\" "
				   + "number of arguments not consistent "
				   + "with the number of "
				   + "formatting directives");
		errcount++;
	    } else {
		if (printExample) {
		    System.out.println
			("[key = " + key + "]: "
			 + String.format(clocale,
					 SafeFormatter.modify(format1),
					 args));
		}
	    }
	}
	if (format2 != null && printExample) System.out.println("--");
	if (format2 != null) {
	    if (args.length != getDirectiveCount(format2)) {
		if (terse && !namesPrinted) {
		    System.out.println("BUNDLE = " + bundleName);
		    System.out.println("FILE = " + fileName);
		    namesPrinted = true;
		}
		System.out.println("*** for key \"" + key + "\" "
				   + "number of arguments not consistent "
				   + "with the number of "
				   + "formatting directives (alt locale)");
		errcount++;
	    } else {
		if (printExample) {
		    System.out.println
			("[key = " + key + "]: "
			 + String.format(locale,
					 SafeFormatter.modify(format2),
					 args));
		}
	    }
	}
	if (format2 != null && printExample) System.out.println("-----------");
    }

    public static void errorMsg(String key) {
	handleErrorMsg(key);
    }

    public static void errorMsg1(String key, String arg1) {
	handleErrorMsg(key, arg1);
    }
    public static void errorMsg2(String key, String arg1, String arg2) {
	handleErrorMsg(key, arg1, arg2);
    }
    public static void errorMsg3(String key, String arg1, String arg2,
				 String arg3)
    {
	handleErrorMsg(key, arg1, arg2, arg3);
    }
    public static void errorMsg4(String key, String arg1, String arg2,
				 String arg3, String arg4)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4);
    }
    public static void errorMsg5(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5);
    }
    public static void errorMsg6(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5,
				 String arg6)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5, arg6);
    }
    public static void errorMsg7(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5,
				 String arg6, String arg7)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }
    public static void errorMsg8(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5,
				 String arg6, String arg7, String arg8)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }
    public static void errorMsg9(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5,
				 String arg6, String arg7, String arg8,
				 String arg9)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8,
		       arg9);
    }

    public static void errorMsg12(String key, String arg1, String arg2,
				 String arg3, String arg4, String arg5,
				 String arg6, String arg7, String arg8,
				  String arg9, String arg10,
				  String arg11, String arg12)
    {
	handleErrorMsg(key, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8,
		       arg9, arg10, arg11, arg12);
    }


}
