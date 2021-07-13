package org.bzdev.obnaming.processor;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.JavaIdents;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import java.lang.annotation.*;

import java.util.*;
import javax.tools.Diagnostic.Kind;
import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;

import org.bzdev.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ObjectNamerProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
	// return the the current version or RELEASE_7, whichever
	// is more recent.  This will prevent the annotation processor
	// from being used on releases earlier than RELEASE_7.
	SourceVersion cversion = super.getSupportedSourceVersion();
	SourceVersion version = SourceVersion.latest();
	return (cversion.ordinal() > version.ordinal())? cversion: version;
    }

    static final boolean DEBUG = false;

    private static final String PACKAGE = "org.bzdev.obnaming";
    private static final String APACKAGE = PACKAGE + ".annotations";
    private static final String PPACKAGE = PACKAGE + ".processor";

    private static final Set<String> supportedAnnotationTypes =
	new HashSet<String>();
    static {
	supportedAnnotationTypes.add(APACKAGE + ".ObjectNamer");
	supportedAnnotationTypes.add(APACKAGE + ".NamedObject");
    }

    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    private String getPackage(Element element) {
        while (!(element instanceof PackageElement)) {
            element = element.getEnclosingElement();
        }
        PackageElement p = (PackageElement) element;
        if (p.isUnnamed()) {
            return null;
        } else  {
            return p.getQualifiedName().toString();
        }
    }

    private static String printDateTime(Calendar cal) {
	return String.format("%tF %tT-00:00", cal, cal);
    }

    private static final String SCRIPTINGPKG = "org.bzdev.scripting";
    TypeMirror SCRIPTING_CONTEXT_MIRROR;

    boolean subclassOfScriptingContext(Types types, TypeMirror tm) {
	if (SCRIPTING_CONTEXT_MIRROR == null) return false;
	for (;;) {
	    if (types.isSameType(tm, SCRIPTING_CONTEXT_MIRROR)) return true;
	    List<? extends TypeMirror>list = types.directSupertypes(tm);
	    if (list.size() == 0) return false;
	    tm = list.get(0);
	}
    }
    static boolean isValidJavaIdentifier(String token, boolean fullyQualified) {
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
	return true;
    }

    static boolean isValidParmList(String string) {
	if (string == null || string.length() == 0) return true;
	string = string.trim();
	if (!string.startsWith("<")) return false;
	if (!string.endsWith(">")) return false;
	int len = string.length();
	int lenm1 = len-1;
	string = string.substring(1, len--);
	int[] cind = new int[len];
	int start = 0;
	int depth = 0;
	int maxdepth = 0;
	int tokend = start;
	for (int i = 0; i < len; i++) {
	    char ch = string.charAt(i);
	    if (ch == '<') {
		if (depth == 0) {
		    tokend = i;
		    if (!JavaIdents.isValidTypeParameter
			(string.substring(start, tokend))) {
			return false;
		    }
		}
		depth++;
		if (depth > maxdepth) maxdepth = depth;
	    } else if (ch == '>') {
		depth--;
		if (depth == 0) {
		    if (!JavaIdents.isValidTypeParameterList
			(string.substring(tokend, i+1))) {
			return false;
		    }
		}
	    } else if (depth == 0 && ch == ',') {
		if (maxdepth == 0) {
		    tokend = i;
		    if (!JavaIdents.isValidTypeParameter
			(string.substring(start, tokend))) {
			return false;
		    }
		}
		start = i+1;
		tokend = start;
		maxdepth = 0;
	    } else if (depth == 0 && i == lenm1) {
		if (maxdepth == 0) {
		    if (!JavaIdents.isValidTypeParameter
			(string.substring(start))) {
			return false;
		    }
		}
		start = i+1;
	    }
	}
	return true;
    }

    static boolean isValidJavaPType(String token) {
	token = token.trim();
	String[] subtokens = token.split("( extends )|( super )", 2);
	for (String subtoken: subtokens) {
	    if (!isValidJavaType(subtoken)) {
		return false;
	    }
	}
	return true;
    }


    static boolean isValidJavaType(String token) {
	String plist = null;
	token.replaceAll("[?]", " ? ");
	token.replaceAll("\\s+"," ");
	if (token.endsWith("...")) {
	    token = token.substring(0, token.length()-3);
	}
	token.trim();
	if (token.endsWith(">")) {
	    int bind = token.indexOf("<");
	    if (bind == -1) return false;
	    plist = token.substring(bind);
	    token = token.substring(0,bind);
	}
	int ind = token.indexOf('[');
	if (ind >= 0) {
	    int eind = token.lastIndexOf(']');
	    if (eind == -1) return false;
	    token = token.substring(0, ind);
	}
	return (token.equals("?") || JavaIdents.isValidIdentifier(token, true))
	    && JavaIdents.isValidTypeParameterList(plist);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
			   RoundEnvironment env)
    {
	Messager messager = processingEnv.getMessager();
	Filer filer = processingEnv.getFiler();
	ArrayList<TemplateProcessor.KeyMap> arrayList =
	    new ArrayList<TemplateProcessor.KeyMap>();
	Types types = processingEnv.getTypeUtils();
	Elements elements = processingEnv.getElementUtils();

	TypeElement scte =
	    elements.getTypeElement(SCRIPTINGPKG + ".ScriptingContext");
	SCRIPTING_CONTEXT_MIRROR = (scte == null)? null: scte.asType();

	if (DEBUG)
	    messager.printMessage(Kind.NOTE, "ObjectNamerProcessor called");

	boolean generate = true;
	for (Element e: env.getElementsAnnotatedWith(ObjectNamer.class)) {
	    try {
		if (DEBUG) {
		    messager.printMessage(Kind.NOTE, "found " + e.toString());
		    break;
		}

		generate = true;
		// each annotation contains all the information needed
		// to generate its two helper files.

		ObjectNamer ona = e.getAnnotation(ObjectNamer.class);
		if (ona == null) {
		    messager.printMessage(Kind.WARNING,
					  "ObjectNamer annotation missing", e);
		    continue;
		}
		String currentPackage = getPackage(e);
		TemplateProcessor.KeyMap keymap = new
		    TemplateProcessor.KeyMap();
		keymap.put("generator",
			   PACKAGE + ".processor.ObjectNamerProcessor");
		keymap.put("date",
			   /*javax.xml.bind.DatatypeConverter.*/printDateTime
			   (Calendar.getInstance(TimeZone.getTimeZone("UTC"))));

		keymap.put("package",
			   ((currentPackage == null)? "":
			    "package " + currentPackage + ";"));

		String helperClass = null;
		try {
		    helperClass = ona.helperClass().trim();
		    if (!JavaIdents.isValidIdentifier(helperClass, false)) {
			throw new Exception("illegal helperClass");
		    }
		    keymap.put("helperClass", helperClass);
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "helperClass illegal or missing from "
					  + "ObjectNamer annotation", e);

		}

		try {
		    String helperSuperclass = ona.helperSuperclass();
		    if (helperSuperclass == null) helperSuperclass = "";
		    helperSuperclass = helperSuperclass.trim();
		    if (helperSuperclass.length() > 0) {
			if (!JavaIdents.isValidIdentifier(helperSuperclass,
							  true)) {
			    throw new Exception("illegal helperSuperclass");
			}
			keymap.put("extendsHelperSuperclass",
				   "extends " +helperSuperclass);
			try {
			    String nscTypeParms =
				ona.helperSuperclassTypeParms();
			    nscTypeParms =  (nscTypeParms == null)? "":
				nscTypeParms.trim();
			    if (nscTypeParms.length() > 0) {
				if (!JavaIdents.isValidTypeParameterList
				    (nscTypeParms)) {
				    throw new Exception
					("illegal type parameter list");
				}
				keymap.put("helperSuperclassTypeParms",
					   nscTypeParms);
			    }
			    TypeMirror hsctm =
				elements.getTypeElement(helperSuperclass)
				.asType();
			    TemplateProcessor.KeyMap rnKeymap =
				new TemplateProcessor.KeyMap();
			    if (subclassOfScriptingContext(types, hsctm)) {
				String resourceName =
				    ona.factoryConfigScriptResource();
				resourceName = (resourceName == null)? "":
				    resourceName.trim();
				if (resourceName.length() > 0) {
				    rnKeymap.put("configScriptResource",
						 resourceName);
				    keymap.put("configFactory", rnKeymap);
				} else {
				    keymap.put("noConfigFactory", rnKeymap);
				}
				resourceName =
				    ona.factoryCreateScriptResource();
				resourceName = (resourceName == null)? "":
				    resourceName.trim();
				if (resourceName.length() > 0) {
				    rnKeymap.put("createScriptResource",
						 resourceName);
				    keymap.put("createFactory", rnKeymap);
				} else {
				    keymap.put("noCreateFactory", rnKeymap);
				}
				keymap.put("hasScripting", rnKeymap);
			    } else {
				    keymap.put("noConfigFactory", rnKeymap);
				    keymap.put("noCreateFactory", rnKeymap);
				    keymap.put("doesNotHaveScripting",
					       rnKeymap);
			    }
			} catch (Exception ex) {
			    generate = false;
			    messager.printMessage(Kind.ERROR,
						  "helperClass error in "
						  + "ObjectNamer annotation",
						  e);
			}
		    }
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "helperClass error in ObjectNamer "
					  + "annotation", e);
		}
		arrayList.clear();
		ObjectNamer.ConstrTypes[] hscArray = null;
		try {
		    hscArray = ona.helperSuperclassConstrTypes();
		} catch (NullPointerException enp) {
		    messager.printMessage(Kind.WARNING,
					  "no ObjectNamer annotation", e);
		}
		if (hscArray != null) {
		    for (ObjectNamer.ConstrTypes hscTypes: hscArray) {
			TemplateProcessor.KeyMap km =
			    new TemplateProcessor.KeyMap();
			StringBuilder constrArgs = new StringBuilder();
			StringBuilder superCall = new StringBuilder();
			int count = 0;
			for (String type: hscTypes.value()) {
			    if (!JavaIdents.isValidType(type)) {
				throw new Exception
				    ("Illegal helperSuperclassConstrTypes");
			    }
			    if (count > 0) {
				constrArgs.append(", ");
				superCall.append(", ");
			    }
			    count++;
			    constrArgs.append(type); constrArgs.append(" arg");
			    constrArgs.append(count);
			    superCall.append("arg"); superCall.append(count);
			}
			km.put("constrArgs", constrArgs.toString());
			km.put("superCall", superCall.toString());
			StringBuilder constrExceptions = new StringBuilder();
			count = 0;
			for (String exception: hscTypes.exceptions()) {
			    String except = exception.trim();
			    if (except.equals("")) continue;
			    if (!JavaIdents.isValidType(except)) {
				throw new Exception("illegal exception");
			    }
			    if (count == 0) {
				constrExceptions.append( "throws ");
			    } else {
				constrExceptions.append(", ");
			    }
			    count++;
			    constrExceptions.append(except);
			}
			km.put("constrExceptions", constrExceptions.toString());
			arrayList.add(km);
		    }
		} else {
		    messager.printMessage(Kind.WARNING,
					  "missing constructor annotation", e);
		}
		keymap.put("cloop", arrayList.toArray
			   (new TemplateProcessor.KeyMap[arrayList.size()]));

		try {
		    String objectClass = ona.objectClass();
		    objectClass = objectClass.trim();
		    if (objectClass.length() == 0) throw new Exception();
		    if (!JavaIdents.isValidIdentifier(objectClass, false)) {
			throw new Exception("objectClass not legal");
		    }
		    keymap.put("objectClass", objectClass);
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "objectClass illegal or missing from "
					  +"ObjectNamer annotation", e);
		}

		try {
		    String objectHelperClass = ona.objectHelperClass();
		    objectHelperClass = objectHelperClass.trim();
		    if (objectHelperClass.length() == 0) throw new Exception();
		    keymap.put("objectHelperClass", objectHelperClass);
		    if (!JavaIdents.isValidIdentifier(objectHelperClass,
						      false)) {
			throw new Exception("Illegal objectHelperClass");
		    }
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "objectHelperClass "
					  + "not valid or missing from "
					  +"ObjectNamer annotation", e);
		}
		if (generate) {
		    FileObject fo1 = null;
		    // Java-extension case
		    InputStream is1 = this.getClass().getClassLoader()
			.getResourceAsStream(PPACKAGE.replace(".", "/")
					     + "/"
					     + "ObjectNamer.tpl");
		    if (is1 == null) {
			try {
			    // Class-path case
			    fo1 = filer.getResource(StandardLocation.CLASS_PATH,
						    PPACKAGE,
						    "ObjectNamer.tpl");
			    is1 = fo1.openInputStream();
			} catch (Exception ex) {
			    messager.printMessage(Kind.ERROR,
						  "cannot find template: "
						  + ex.getMessage());
			    return true;
			}
		    }
		    try {
			InputStreamReader reader1 = new
			    InputStreamReader(is1, Charset.forName("UTF-8"));
			String target1 = (currentPackage == null)? helperClass:
			    currentPackage + "." + helperClass;
			JavaFileObject jfo1 = filer.createSourceFile(target1);
			Writer writer1 = jfo1.openWriter();
			TemplateProcessor tp = new TemplateProcessor(keymap);
			tp.processTemplate(reader1, writer1);
			reader1.close();
			writer1.flush();
			writer1.close();
		    } catch (Exception ex) {
			messager.printMessage(Kind.ERROR, "cannot generate file "
					      + helperClass + ".java");
		    }
		}
	    } catch (Exception exception) {
		exception.printStackTrace();
		messager.printMessage(Kind.ERROR,
				      "Annotation Processor Exception: "
				      + exception.getMessage(),
				      e);
	    }
	}
	for (Element e: env.getElementsAnnotatedWith(NamedObject.class)) {
	    try {
		generate = true;
		if (DEBUG) {
		    messager.printMessage(Kind.NOTE, "found " + e.toString());
		    break;
		}
		NamedObject ona = e.getAnnotation(NamedObject.class);
		if (ona == null) {
		    messager.printMessage(Kind.WARNING,
					  "ona == null for NamedObject", e);
		    continue;
		}
		String currentPackage = getPackage(e);
		TemplateProcessor.KeyMap keymap = new
		    TemplateProcessor.KeyMap();
		keymap.put("generator",
			   PACKAGE + ".processor.ObjectNamerProcessor");
		keymap.put("date",
			   /*javax.xml.bind.DatatypeConverter.*/printDateTime
			   (Calendar.getInstance(TimeZone.getTimeZone("UTC"))));
		keymap.put("package",
			   ((currentPackage == null)? "":
			    "package " + currentPackage + ";"));
		try {
		    String namerHelperClass = ona.namerHelperClass();
		    namerHelperClass = namerHelperClass.trim();
		    if (namerHelperClass.length() == 0) {
			throw new Exception();
		    }
		    if (!JavaIdents.isValidIdentifier(namerHelperClass,
						      false)) {
			throw new Exception
			    ("namerHelperClass is not a simple class name");
		    }
		    keymap.put("namerHelperClass", namerHelperClass);
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "namerHelperClass illegal or "
					  + "missing from "
					  +"NamedObject annotation", e);
		}

		try {
		    String namerClass = ona.namerClass();
		    if (namerClass.trim().length() == 0) {
			throw new Exception();
		    }
		    if (!JavaIdents.isValidIdentifier(namerClass, false)) {
			throw new Exception("namerClass is not a simple name");
		    }
		    keymap.put("namerClass", namerClass);
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "namerClass illegal or missing from "
					  +"NamedObject annotation", e);
		}

		String helperClass = null;
		try {
		    helperClass = ona.helperClass();
		    helperClass = helperClass.trim();
		    if (helperClass.length() == 0) throw new Exception();
		    if (!JavaIdents.isValidIdentifier(helperClass, false)) {
			throw new Exception("helperClass is not a simple name");
		    }
		    keymap.put("helperClass", helperClass);
		} catch (Exception ex) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "helperClass missing from "
					  +"NamedObject annotation", e);
		}

		String objectClass = e.getSimpleName().toString();
		keymap.put("objectClass", objectClass);
		String helperSuperclass = ona.helperSuperclass().trim();
		if (helperSuperclass.length() > 0) {
		    if (!JavaIdents.isValidIdentifier(helperSuperclass, true)) {
			throw new Exception("bad helperSuperclass");
		    }
		    keymap.put("extendsHelperSuperclass",
			       "extends " + helperSuperclass);
		    String hscTypeParms =
			ona.helperSuperclassTypeParms().trim();
		    if (hscTypeParms.length() > 0) {
			keymap.put("helperSuperclassTypeParms", hscTypeParms);
		    }
		}
		arrayList.clear();
		for (NamedObject.ConstrTypes hscTypes:
			 ona.helperSuperclassConstrTypes()) {
		    TemplateProcessor.KeyMap km =
			new TemplateProcessor.KeyMap();
		    StringBuilder constrArgs = new StringBuilder();
		    StringBuilder superCall = new StringBuilder();
		    int count = 0;
		    for (String type: hscTypes.value()) {
			if (!JavaIdents.isValidType(type)) {
			    throw new Exception("bad constructoro type");
			}
			constrArgs.append(", ");
			if (count > 0) {
			    superCall.append(", ");
			}
			count++;
			constrArgs.append(type); constrArgs.append(" arg");
			constrArgs.append(count);
			superCall.append("arg"); superCall.append(count);
		    }
		    km.put("constrArgs", constrArgs.toString());
		    km.put("superCall", superCall.toString());
		    StringBuilder constrExceptions = new StringBuilder();
		    for (String exception: hscTypes.exceptions()) {
			String except = exception.trim();
			if (except.equals("")) continue;
			if (except.equals("IllegalArgumentException")) continue;
			if (except.equals("java.lang.IllegalArgumentException"))
			    continue;
			constrExceptions.append(", ");
			constrExceptions.append(except);
		    }
		    km.put("constrExceptions", constrExceptions.toString());
		    arrayList.add(km);
		}
		keymap.put("ocloop", arrayList.toArray
			   (new TemplateProcessor.KeyMap[arrayList.size()]));
		if (generate) {
		    FileObject fo2 = null;
		    // Java-extension case
		    InputStream is2 = getClass().getClassLoader()
			.getResourceAsStream(PPACKAGE.replace(".", "/")
					     + "/" + "NamedObject.tpl");
		    if (is2 == null) {
			try {
			    // Class-file case
			    fo2 = filer.getResource(StandardLocation.CLASS_PATH,
						    PPACKAGE,
						    "NamedObject.tpl");
			    is2 = fo2.openInputStream();
			} catch (Exception ex) {
			    messager.printMessage(Kind.ERROR,
						  "cannot find template");
			    return true;
			}
		    }
		    try {
			InputStreamReader reader2 = new
			    InputStreamReader(is2, Charset.forName("UTF-8"));
			String target2 = (currentPackage == null)? helperClass:
			    currentPackage + "." + helperClass;
			JavaFileObject jfo2 = filer.createSourceFile(target2);
			Writer writer2 = jfo2.openWriter();
			TemplateProcessor tp = new TemplateProcessor(keymap);
			tp.processTemplate(reader2, writer2);
			reader2.close();
			writer2.flush();
			writer2.close();
		    } catch (Exception ex) {
			messager.printMessage(Kind.ERROR,
					      "cannot generate file "
					      + helperClass + ".java");
		    }
		}
	    } catch (Exception exception) {
		exception.printStackTrace();
		messager.printMessage(Kind.ERROR,
				      "Annotation Processor Exception: "
				      + exception.getMessage(),
				      e);
	    }
	}
	return true;
    }
}
