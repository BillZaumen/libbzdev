package org.bzdev.util;
import java.util.*;

/**
 * Utility class for checking whether or not strings representing
 * Java types, identifiers, or type parameters are valid.
 * The tests are lexical and partially syntactic: a type parameter
 * such as "? extends Foo" will not check that Foo is a class that has
 * been defined. Similarly a type such as "Foo[EXPRESSION][]" will not
 * check that the EXPRESSION uses the correct Java syntax for an
 * expression.  Errors such as a type defined as "1Foo" or an
 * identifier whose name is "const" will be caught (while not
 * currently used by Java, "const" is a Java reserved word).
 */
public class JavaIdents {

    private static Set<String> reservedWords = new HashSet<String>(32);
    private static Set<String> primitiveTypes = new HashSet<String>();
    static {
	String list[] = {
	    "abstract", "do", "if", "private", "this",
	    "assert", "double", "implements", "protected", "throw",
	    "boolean", "else", "import", "public", "throws",
	    "break", "enum", "instanceof", "return", "transient",
	    "byte", "extends", "int", "short", "true",
	    "case", "false", "interface", "static", "try",
	    "catch", "final", "long", "strictfp", "void",
	    "char", "finally", "native", "super", "volatile",
	    "class", "float", "new", "switch", "while",
	    "const", "for", "null", "synchronized", "continue",
	    "default", "goto", "package"
	};
	for (String word: list) {
	    reservedWords.add(word);
	}

	primitiveTypes.add("boolean");
	primitiveTypes.add("char");
	primitiveTypes.add("byte");
	primitiveTypes.add("int");
	primitiveTypes.add("short");
	primitiveTypes.add("long");
	primitiveTypes.add("float");
	primitiveTypes.add("double");

    }

    private static boolean isReserved(String token) {
	return reservedWords.contains(token);
    }

    private static boolean isPrimitive(String token) {
	return primitiveTypes.contains(token);
    }

    /**
     * Determine if a string represents a valid Java identifier.
     * The test is a lexical test so that a return value of true does
     * not indicate that a compile-time error will not occur.
     * The test is a lexical test so that a return value of true does
     * not indicate that a compile-time error will not occur.
     * @param token the string to check
     * @param fullyQualified true if fully-qualified names are allowed;
     *        false if only simple names are allowed.
     * @return true if the string 'token' is valid; false otherwise
     */
    public static boolean isValidIdentifier(String token,
					    boolean fullyQualified)
    {
	if (token == null) return false;
	int len = token.length();
	boolean start = true;
	for (int i = 0; i < len; i++) {
	    char ch = token.charAt(i);
	    if (start) {
		if (!Character.isJavaIdentifierStart(ch)) {
		    return false;
		}
		start = false;
	    } else {
		if (fullyQualified && ch == '.') {
		    start = true;
		} else if (!Character.isJavaIdentifierPart(ch)) {
		    return false;
		}
	    }
	}
	if (start) return false;
	for (String component: token.split("[.]")) {
	    if (isReserved(component)) return false;
	}
	return true;
    }

    /**
     * Determine if a string is a valid Java type-parameter list.
     * The string must contain the list's opening and closing
     * '&lt;' and '&gt;' delimiters.
     * The test is a lexical test so that a return value of true does
     * not indicate that a compile-time error will not occur.
     * @param string the string to test
     * @return true if 'string' is valid; false otherwise
     */
    public static boolean isValidTypeParameterList(String string) {
	// System.out.println("... isValidTypeParameter: " + string);
	if (string == null || string.length() == 0) return true;
	string = string.trim();
	if (!string.startsWith("<")) return false;
	if (!string.endsWith(">")) return false;
	int len = string.length();
	string = string.substring(1, --len);
	len--;
	int lenm1 = len - 1;
	// System.out.println("... isValidTypeParameter without delims: " + string);
	int start = 0;
	int depth = 0;
	int maxdepth = 0;
	int tokend = start;
	for (int i = 0; i < len; i++) {
	    char ch = string.charAt(i);
	    if (ch == '<') {
		if (depth == 0) {
		    tokend = i;
		    if (start != tokend &&
			!isValidTypeParameter(string.substring(start, tokend))){
			return false;
		    }
		}
		depth++;
		if (depth > maxdepth) maxdepth = depth;
	    } else if (ch == '>') {
		depth--;
		if (depth == 0) {
		    if (!isValidTypeParameterList(string.substring(tokend,
								   i+1))) {
			return false;
		    }
		}
	    } else if (depth == 0 && ch == ',') {
		if (maxdepth == 0) {
		    tokend = i;
		    if (!isValidTypeParameter(string.substring(start,
							       tokend))) {
			return false;
		    }
		}
		start = i+1;
		tokend = start;
		maxdepth = 0;
	    } else if (depth == 0 && i == lenm1) {
		if (maxdepth == 0) {
		    if (!isValidTypeParameter(string.substring(start))) {
			return false;
		    }
		}
		start = i+1;
	    }
	}
	return true;
    }

    /**
     * Determine if a string is a valid Java type parameter.
     * The test accepts any type specification that can occur in a
     * type-parameter list (a comma-separated list delimited by an
     * opening '&lt;'and a closing '&gt;'). Wildcard types are
     * supported.  The type must not include the opening '&lt;'and a
     * closing '&gt;' delimiters, and the test is a lexical test so
     * that a return value of true does not indicate that a
     * compile-time error will not occur.
     * @param token the string to test
     * @return true if the string 'token' is valid; false otherwise
     */
    public static boolean isValidTypeParameter(String token) {
	if (token == null) return false;
	token = token.trim();
	if (token.length() == 0) return false;
	String[] subtokens = token.split("( extends )|( super )", 2);
	for (String subtoken: subtokens) {
	    if (!isValidType(subtoken, true)) {
		return false;
	    }
	}
	if (subtokens.length > 1 && subtokens[1].equals("?")) {
	    return false;
	}
	return true;
    }

    /**
     * Determine if a string is a valid Java type, including ones with a type
     * parameter.
     * The test is a lexical test so that a return value of true does
     * not indicate that a compile-time error will not occur.
     * @param type the string to test
     * @return true if the string 'type' is valid; false otherwise
     */
    public static boolean isValidType(String type) {
	return isValidType(type, false);
    }

    static boolean isValidType(String token, boolean allowWildcard) {
	if (token == null) return false;
	String plist = null;
	token.replaceAll("[?]", " ? ");
	token.replaceAll("\\s+"," ");
	if (token.endsWith("...")) {
	    token = token.substring(0, token.length()-3);
	}
	token.trim();
	if (token.length() == 0) return false;
	if (token.endsWith(">")) {
	    int bind = token.indexOf("<");
	    if (bind == -1) return false;
	    plist = token.substring(bind);
	    token = token.substring(0,bind).trim();
	}
	int ind = token.indexOf('[');
	if (ind >= 0) {
	    int eind = token.lastIndexOf(']');
	    if (eind == -1) return false;
	    token = token.substring(0, ind).trim();
	}

	/*
	System.out.println("isValidType: token =  " + token
			   + "; plist = " + plist
			   + ", allowWildcard = " + allowWildcard);
	*/

	return ((allowWildcard && token.equals("?"))
		|| (isValidIdentifier(token, true) || isPrimitive(token)))
	    && (plist == null || isValidTypeParameterList(plist));
    }
}

//  LocalWords:  const boolean enum instanceof strictfp goto lt plist
//  LocalWords:  fullyQualified isValidTypeParameter delims
//  LocalWords:  isValidType allowWildcard
