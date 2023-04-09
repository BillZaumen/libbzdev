package org.bzdev.lang.processor;
// import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import org.bzdev.util.JavaIdents;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.annotation.*;
import java.util.*;
import javax.tools.Diagnostic.Kind;
import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;

// @SupportedAnnotationTypes({"org.bzdev.lang.annotations.DynamicMethod",
//                            "org.bzdev.lang.annotations.DMethodImpl"})

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({"org.bzdev.lang.annotations.DMethodOptions.lockingMode"})
public class DMethodProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
	// return the the current version or RELEASE_7, whichever
	// is more recent.  This will prevent the annotation processor
	// from being used on releases earlier than RELEASE_7.
	SourceVersion cversion = super.getSupportedSourceVersion();
	SourceVersion version = SourceVersion.latest();
	return (cversion.ordinal() > version.ordinal())? cversion: version;
    }

    // turn on to print a trace to stdout.
    private static final boolean debug = false;

    // Must match the current package name.
    private static final String MODULE = "org.bzdev.dmethods";
    private static final String PACKAGE = "org.bzdev.lang";
    private static final String MPACKAGE = MODULE +"/" + PACKAGE;
    private static final String APACKAGE = PACKAGE + ".annotations";

    private static final Set<String> supportedAnnotationTypes
	= new HashSet<String>();
    static {
	supportedAnnotationTypes.add(APACKAGE +".DynamicMethod");
	supportedAnnotationTypes.add(APACKAGE +".DMethodImpl");
    }

    public Set<String> getSupportedAnnotationTypes() {
	return supportedAnnotationTypes;
    }

    static DMethodOptions.Locking defaultLockingMode
	= DMethodOptions.Locking.MUTEX;

    class DispatcherData {
	ExecutableElement execElement = null;
	String methodName = null;
	String[] methodArgumentType = null;
	int[] order = null;
	boolean isVarArgs = false;
	String methodThrowables[] = null;
	String methodReturnType = null;
    }

    class HelperData {
	String helper = null;
	String baseType = null;
	String[] baseArgType = null;
	String[] baseThrowables = null;
	String baseReturnType = null;
	int[] baseOrder = null;
	boolean baseIsVarArgs = false;
	DMethodOptions.Locking baseLockingMode = defaultLockingMode;
	boolean tracing = false;
	int limitFactor = 1;
	TypeElement execElement = null;
	// used to signal that we read an entry from a class file so we
	// should not generate it.
	boolean skip = false;
    }

    Map<String,DispatcherData>dmap = new HashMap<String,DispatcherData>();

    Map<String,HelperData> hmap = new HashMap<String,HelperData>();

    Map<String,List<DispatcherData>> mmap =
	new HashMap<String,List<DispatcherData>>();

    private boolean addHdata(String helper,
			     String localHelper,
			     DispatcherData data,
			     String baseType,
			     String[] baseArgType,
			     String[] baseThrowables,
			     String baseReturnType,
			     int[] baseOrder,
			     boolean baseIsVarArgs,
			     DMethodOptions.Locking lockingMode,
			     boolean tracing,
			     int limitFactor)
    {
	HelperData hdata = hmap.get(localHelper);
	TypeElement enclosing =  (TypeElement)
	    data.execElement.getEnclosingElement();
	if (hdata == null) {
	    if (debug) {
		messager.printMessage(Kind.NOTE,
				       "addHdata: creating new data (key = "
				       +localHelper +")");
	    }
	    hdata = new HelperData();
	    hdata.helper = helper;
	    hdata.baseType = baseType;
	    hdata.baseArgType = baseArgType;
	    hdata.baseThrowables = baseThrowables;
	    hdata.baseReturnType = baseReturnType;
	    hdata.execElement = enclosing;
	    hdata.baseOrder = baseOrder;
	    hdata.baseIsVarArgs = baseIsVarArgs;
	    hmap.put(localHelper, hdata);
	}
	hdata.baseLockingMode = lockingMode;
	hdata.tracing = tracing;
	hdata.limitFactor = limitFactor;

	if (helper == null || hdata.helper == null ||
	    enclosing == null || hdata.execElement == null ||
	    baseType == null || hdata.baseType == null) {
	    if (debug) {
		messager.printMessage(Kind.NOTE,
				      "addHdata: data missing");
		messager.printMessage(Kind.NOTE,
				      "    ... helper = " +helper);
		messager.printMessage(Kind.NOTE,
				      "    ... hdata.helper = " +hdata.helper);
		messager.printMessage(Kind.NOTE,
				      "    ... enclosing = "  +enclosing);
		messager.printMessage(Kind.NOTE,
				      "    ... hdata.execElement = "
				      +hdata.execElement);
		messager.printMessage(Kind.NOTE,
				      "    ... baseType = " +baseType);
		messager.printMessage(Kind.NOTE,
				      "    ... hdata.baseType = "
				      + hdata.baseType);
	    }
	    return false;
	}
	if (!hdata.helper.equals(helper) ||
	    !hdata.execElement.equals(enclosing) ||
	    !hdata.baseType.equals(baseType)) {
	    if (debug)  messager.printMessage(Kind.NOTE,"addHdata: mismatch");
	    return false;
	}
	return true;

    }

    private boolean add(String helper, DispatcherData data,
			String baseType, String[] baseArgType,
			String[] baseThrowables,
			String baseReturnType,
			int[] baseOrder, boolean baseIsVarArgs,
			DMethodOptions.Locking lockingMode,
			boolean tracing, int limitFactor)
	throws IllegalArgumentException
    {
	if (addHdata(helper, helper, data, baseType,
		     baseArgType, baseThrowables, baseReturnType,
		     baseOrder, baseIsVarArgs,
		     lockingMode, tracing, limitFactor) == false) {
	    return false;
	}

	if (dmap.get(helper) != null) {
	    return false;
	}
	if (debug) messager.printMessage(Kind.NOTE,
					 "added dmap entry for " +helper);
	dmap.put(helper, data);
	return true;
    }

    private boolean add(String helper,
			String localHelper,
			DispatcherData data,
			String baseType,
			String[] baseArgType, String[] baseThrowables,
			String  baseReturnType,
			int[] baseOrder, boolean baseIsVarArgs,
			DMethodOptions.Locking lockingMode,
			boolean tracing, int limitFactor)
    {
	if (addHdata(helper, localHelper, data, baseType,
		     baseArgType, baseThrowables, baseReturnType,
		     baseOrder, baseIsVarArgs,
		     lockingMode, tracing, limitFactor) == false) {
	    return false;
	}

	List<DispatcherData> list = mmap.get(localHelper);
	if (list == null) {
	    list = new LinkedList<DispatcherData>();
	    mmap.put(localHelper, list);
	}
	list.add(data);
	return true;
    }

    private boolean checkOrder(int[] order) {
	// sanity check only
	int i;
	int count = 0;
	for (i = 0; i < order.length; i++) {
	    if(order[i] <= 0) count++;
	    if (order[i] > order.length) return false;
	}
	if (count == order.length) return false;
	int[] sorder = new int[order.length - count];

	for(i = 0, count = 0; i < order.length; i++) {
	    if (order[i] <= 0) {
		count++;
		continue;
	    }
	    sorder[i-count] = order[i];
	}
	Arrays.sort(sorder);
	for (i = 1; i < sorder.length; i++) {
	    if (sorder[i-1] + 1 != sorder[i]) return false;
	}
	return true;
    }

    boolean isAssignable(javax.lang.model.util.Types types,
			 TypeMirror type, String argType) {
	if (type == null) {
	    return false;
	}
	switch(type.getKind()) {
	case NONE:
	case NULL:
	case ERROR:
	    return false;
	}
	if (debug) messager.printMessage(Kind.NOTE,
					 " ... checking " + type.toString()
					 + " for assignability with "
					 + argType);
	if (type.toString().equals(argType)) return true;
	if (type.toString().equals("java.lang.Object")) {
	    return false;
	}
	Element e = types.asElement(type);
	TypeElement te = (TypeElement) e;
	List<? extends TypeMirror> mlist = te.getInterfaces();
	for (TypeMirror tm: mlist) {
	    if (isAssignable(types, tm, argType)) return true;
	}
	return isAssignable(types, te.getSuperclass(), argType);
    }

    private boolean lmodeNotSeen = true;
    private void processOptions(Map<String,String> options) {
	if (options == null) return;
	String lmode = options.get(APACKAGE + ".DMethodOptions.lockingMode");
	if (lmode != null) {
	    if (lmode.equals("NONE")) {
		defaultLockingMode = DMethodOptions.Locking.NONE;
	    } else if (lmode.equals("MUTEX")) {
		defaultLockingMode = DMethodOptions.Locking.MUTEX;
	    } else if (lmode.equals("RWLOCK")) {
		defaultLockingMode = DMethodOptions.Locking.RWLOCK;
	    }
	    if (lmodeNotSeen) {
		messager.printMessage(Kind.NOTE, 
				      "Annotation processor set default "
				      + "dynamic-method locking-mode to "
				      +lmode);
		lmodeNotSeen = false;
	    }
	}
    }

    Messager messager = null;


    public boolean process(Set<? extends TypeElement> elements,
                           RoundEnvironment env)
    {
	javax.lang.model.util.Types types =
	    processingEnv.getTypeUtils();
	javax.lang.model.util.Elements elementUtils =
	    processingEnv.getElementUtils();
	Messager messager = processingEnv.getMessager();
	this.messager = messager;
	Filer filer = processingEnv.getFiler();

	processOptions(processingEnv.getOptions());

	if (elements.size() == 0) return true;
	LinkedList<TypeElement> teList = new LinkedList<TypeElement>();
	for (TypeElement te: elements) {
	    try {
		if (te.getQualifiedName()
		    .toString().equals(APACKAGE + ".DynamicMethod")) {
		    teList.addFirst(te);
		} else {
		    teList.add(te);
		}
	    } catch (Exception e) {
		messager.printMessage(Kind.ERROR,
				      "DMethodProcessor: "
				      + e.getMessage());

	    }
	}
	for (TypeElement te: teList) {
	    if (debug) messager.printMessage(Kind.NOTE,
					     "te = " +te.asType().toString());
            for (Element e: env.getElementsAnnotatedWith(te)) {
		try {
		    String key;
		    String currentPackageName = getPackage(e);
		    DispatcherData data = new DispatcherData();
		    data.execElement = (ExecutableElement)e;
		    data.methodName = e.getSimpleName().toString();
		    DynamicMethod ea1 = e.getAnnotation(DynamicMethod.class);
		    DMethodImpl ea2 = e.getAnnotation(DMethodImpl.class);

		    List<? extends VariableElement> list = null;
		    List<? extends TypeMirror> throwableList = null;
		    TypeMirror returnType = null;
		    if (e instanceof ExecutableElement) {
			int[] order = null;
			ExecutableElement eee = (ExecutableElement) e;
			data.isVarArgs = eee.isVarArgs();
			list = eee.getParameters();
			throwableList = eee.getThrownTypes();
			returnType = eee.getReturnType();
			if (returnType != null) {
			    data.methodReturnType = returnType.toString();
			}
			if (list.size() == 0) {
			    messager.printMessage(Kind.ERROR,
						  "Method contains no"
						  +" arguments",
						  e);
			    continue;
			}

			DMethodOrder orderAnnotation
			    = e.getAnnotation(DMethodOrder.class);
			if (orderAnnotation != null) {
			    if (ea1 == null) {
				messager.printMessage(Kind.ERROR,
						      "DMethodOrder annotation "
						      +"but no DynamicMethod "
						      +"annotation",
						      e);
				continue;
			    }
			    if (debug)
				messager.printMessage(Kind.NOTE,
						      "DMethodOrder seen");
			    order = orderAnnotation.value();
			    //check format
			    if (checkOrder(order) == false) {
				messager.printMessage(Kind.ERROR,
						      "bad order annotation",
						      e);
				continue;
			    }
			    data.order = order;
			} else if (!(ea1 == null)) {
			    int nextArg = 0;
			    int nextVal = 1;
			    //
			    // Set up a default - standard order and include
			    // all arguments whose types are classes or
			    // interfaces but not arrays.
			    //
			    order = new int[list.size()];
			    for (VariableElement ve: list) {
				TypeMirror type = types.erasure(ve.asType());
				if (type.getKind() == TypeKind.DECLARED) {
				    order[nextArg] = nextVal++;
				} else {
				    order[nextArg] = 0;
				}
				nextArg++;
			    }
			    data.order = order;
			}

			data.methodArgumentType = new String[list.size()];
			int cnt = 0;
			int len = list.size();
			for (int i = 0; i < len; i++) {
			    VariableElement ve = list.get(i);
			    TypeMirror type = types.erasure(ve.asType());
			    if (type.getKind() != TypeKind.DECLARED) {
				cnt++;
			    }
			    if (debug) {
				messager.printMessage(Kind.NOTE,
						      "... " +type.toString()
						      + ", name = "
						      + ve.getSimpleName());
			    }
			    data.methodArgumentType[i] = type.toString();
			}
			len = throwableList.size();
			data.methodThrowables = new String[len];
			for (int i = 0; i < len; i++) {
			    TypeMirror type = throwableList.get(i);
			    if (debug) {
				messager.printMessage(Kind.NOTE,
						      "... " +", throwable = "
						      + type.toString());
			    }
			    data.methodThrowables[i] = type.toString();
			}
			if (cnt == list.size()) {
			    messager.printMessage(Kind.ERROR,
						  "All argument type must "
						  + "not be "
						  +" arrays, enums or "
						  +"primitive types",
						  e);
			    continue;
			}
		    }

		    String localHelper = null;
		    String baseType = null;
		    boolean baseIsVarArgs = false;
		    DMethodOptions.Locking baseLockingMode
			= defaultLockingMode;
		    boolean tracing = false;
		    int limitFactor = 1;
		    int[] baseOrder = null;
		    String[] baseArgType = null;
		    String[] baseThrowables = null;
		    String baseReturnType = null;
		    Element ee = e.getEnclosingElement();
		    DMethodContext eea =
			ee.getAnnotation(DMethodContext.class);
		    DMethodContexts eeas =
			ee.getAnnotation(DMethodContexts.class);
		    DMethodContext[] dca = null;
		    if (ea2 != null) {
			if (eea == null && eeas == null) {
			    messager.printMessage(Kind.ERROR,
						  "DispatcherContext(s) "
						  + "annotation missing",
						  ee);
			    continue;
			} else if (ea2 != null && eea == null) {
			    dca = eeas.value();
			} else if (ea2 != null && eeas == null) {
			    dca = new DMethodContext[1];
			    dca[0] = eea;
			} else {
			    DMethodContext[] dca1 = eeas.value();
			    dca = new DMethodContext[1 + dca1.length];
			    System.arraycopy(dca1, 0, dca, 0, dca1.length);
			    dca[dca1.length] = eea;
			}
		    }
		    try {
			if (ea1 != null) {
			    DMethodOptions dmoa =
				e.getAnnotation(DMethodOptions.class);
			    if (dmoa != null) {
				baseLockingMode = dmoa.lockingMode();
				if (baseLockingMode
				    == DMethodOptions.Locking.DEFAULT) {
				    baseLockingMode = defaultLockingMode;
				}
				tracing = dmoa.traceMode();
				limitFactor = dmoa.limitFactor();
				if (limitFactor <= 0) {
				    messager.printMessage(Kind.ERROR,
							  "limitFactor "
							  + " for DMethodOption"
							  + " must be "
							  + "positive",
							  e);
				    limitFactor = 1;
				}
			    }
			    key = ea1.value();
			    if (!JavaIdents.isValidIdentifier(key, true)) {
				messager.printMessage(Kind.ERROR,
						      "Annotation value is not"
						      + "a valid class name",
						      e);
				continue;
			    }
			    String keyPackage = getPackage(key);
			    // String currentPackageName = getPackage(e);
			    if (!key.contains(".")
				&& currentPackageName != null)
			    {
				key = currentPackageName +"." + key;
			    } else if ((currentPackageName == null
					&& key.contains("."))
				       || (currentPackageName != null
					   && !currentPackageName
					   .equals(keyPackage))) {
				messager.printMessage(Kind.ERROR,
						      "argument to "
						      +"@DynamicMethod "
						      +"must name a class in "
						      +"the current "
						      +"package",
						      e);
				continue;
			    }

			    baseType =
				((TypeElement)ee).getQualifiedName().toString();
			    baseArgType = data.methodArgumentType;
			    baseThrowables = data.methodThrowables;
			    baseReturnType = data.methodReturnType;
			    baseOrder = data.order;
			    baseIsVarArgs = data.isVarArgs;
			} else if (ea2 != null) {
			    key = ea2.value();
			    if (!JavaIdents.isValidIdentifier(key, true)) {
				messager.printMessage(Kind.ERROR,
						      "argument to "
						      + "@DMethodImpl must be "
						      + "a valid class name",
						      e);
				continue;
			    }
			    // String currentPackageName = getPackage(e);
			    if (!key.contains(".")
				&& currentPackageName != null)
				{
				    key = currentPackageName +"." + key;
				}

			    if (hmap.get(key) == null) {
				int lastInd = key.lastIndexOf('.');
				String simpleName = key;
				String packageName = "";
				if (lastInd >= 0) {
				    packageName = key.substring(0, lastInd);
				    simpleName = key.substring(lastInd+1);
				}
				TypeElement helperClass =
				    elementUtils.getTypeElement(key);
				if (helperClass == null) {
				    messager.printMessage
					(Kind.ERROR, "helper class "
					 + key + " missing");
				    continue;
				}
				if (debug)
				    messager.printMessage(Kind.NOTE,
							  "helperClass = " +
							  helperClass
							  .toString());
				DMethodHelperInfo dmhia =
				    helperClass.getAnnotation
				    (DMethodHelperInfo.class);
				if (dmhia != null) {
				    if (!key.equals(dmhia.helper())) {
					throw new Exception
					    ("bad helper field in annotation: "
					     + "found " + dmhia.helper()
					     + ", expected " + key);
				    }
				    baseType = dmhia.baseType();
				    baseArgType = dmhia.baseArgType();
				    if (debug) {
					messager.printMessage
					    (Kind.NOTE,
					     "baseArgType.length = "
					     + baseArgType.length);
					for (int i = 0; i < baseArgType.length;
					     i++)
					    {
						messager.printMessage
						    (Kind.NOTE,
						     "baseArgType[" + i + "] = "
						     + baseArgType[i]);
					    }
				    }
				    if (baseArgType.length == 0)
					baseArgType = null;
				    baseThrowables = dmhia.baseThrowables();
				    if (baseThrowables.length == 0)
					baseThrowables = null;
				    baseReturnType = dmhia.baseReturnType();
				    baseOrder = dmhia.baseOrder();
				    if (baseOrder.length == 0) {
					messager.printMessage
					    (Kind.ERROR,
					     "base order missing in annotation "
					     + "for " + key);
					continue;
				    } else {
					if (checkOrder(baseOrder) == false) {
					    messager.printMessage
						(Kind.ERROR,
						 "bad base order in annotation "
						 + "for " + key);
					    continue;
					}
				    }
				    baseIsVarArgs = dmhia.baseIsVarArgs();
				    baseLockingMode = dmhia.baseLockingMode();
				    tracing = dmhia.traceMode();
				    limitFactor = dmhia.limitFactor();
				    if (limitFactor <= 0) {
					messager.printMessage
					    (Kind.ERROR,
					     "limitFactor "
					     + " for DMethodHelperInfo"
					     + " must be positive");
					limitFactor = 1;
				    }
				} else {
				    messager.printMessage(Kind.ERROR,
							  "helper annotation "
							  + "for " + key
							  + " missing");
				    throw new Exception("missing anotation");
				}
				DispatcherData ddata = new DispatcherData();
				ddata.execElement = data.execElement;
				if (debug)
				    messager.printMessage(Kind.NOTE,
							  "processing cfg: "
							  + "calling add for "
							  + key);
				if (!add(key, ddata,
					 baseType, baseArgType,
					 baseThrowables, baseReturnType,
					 baseOrder, baseIsVarArgs,
					 baseLockingMode,
					 tracing, limitFactor)) {
				    messager.printMessage
					(Kind.ERROR,
					 "dublicate helpers among"
					 +" @Dispatch annotations", e);
				} else {
				    hmap.get(key).skip = true;
				    if (debug)
					messager.printMessage(Kind.NOTE,
							      "set skip to "
							      + "true for "
							      + key);
				}

			    } else {
				baseType = hmap.get(key).baseType;
				baseArgType = hmap.get(key).baseArgType;
				baseThrowables = hmap.get(key).baseThrowables;
				baseReturnType = hmap.get(key).baseReturnType;
				baseOrder = hmap.get(key).baseOrder;
				baseIsVarArgs = hmap.get(key).baseIsVarArgs;
				baseLockingMode =hmap.get(key).baseLockingMode;
				tracing = hmap.get(key).tracing;
				limitFactor = hmap.get(key).limitFactor;
			    }
			} else {
			    key = "";
			}
			if (baseArgType != null) {
			    if (list.size() != baseArgType.length) {
				messager.printMessage
				    (Kind.ERROR,
				     "wrong number of parameters for method",
				     e);
			    } else {
				int k = 0;
				int lst = list.size() - 1;
				for (VariableElement ve: list) {
				    TypeMirror type =
					types.erasure(ve.asType());
				    if (baseOrder[k] > 0 &&
					!(k == lst && baseIsVarArgs)) {
					if (debug) {
					    messager.printMessage
						(Kind.NOTE,
						 "calling is Assignable for "
						 + type.toString() + " and "
						 + baseArgType[k]);
					}
					if (!isAssignable(types,
							  type,
							  baseArgType[k])) {
					    messager.printMessage
						(Kind.ERROR,
						 type.toString()
						 +" is not a subclass "
						 +"of " +baseArgType[k],
						 e);
					}
				    } else {
					if (!type.toString()
					    .equals(baseArgType[k])) {
					    messager.printMessage
						(Kind.ERROR,
						 type.toString()
						 +" must match type "
						 +baseArgType[k],
						 e);
					}
				    }
				    k++;
				}
			    }
			}
			if (throwableList != null
			    && throwableList.size() > 0) {
			    if (baseThrowables == null) {
				messager.printMessage
				    (Kind.ERROR,
				     "throws clause found but no throws "
				     +"clause in a matching dynamic method",
				     e);
			    } else {
				for (TypeMirror type: throwableList) {
				    type = types.erasure(type);
				    boolean ok = false;
				    for (String extype: baseThrowables) {
					if (isAssignable(types, type, extype)) {
					    ok = true;
					    break;
					}
				    }
				    if (!ok) {
					messager.printMessage
					    (Kind.ERROR,
					     type.toString()
					     +" not a subclass"
					     +" of an exception declared"
					     +" for a matching dynamic method",
					     e);
				    }
				}
			    }
			}
			if (returnType != null) {
			    if (!isAssignable(types, types.erasure(returnType),
					      baseReturnType)) {
				messager.printMessage
				    (Kind.ERROR,
				     data.methodReturnType
				     + " not compatible with "
				     + baseReturnType + " for assignment",
				     e);
			    }
			}
		    } catch (Exception ex) {
			String msg = ex.getMessage();
			if (msg == null) {
			    msg = ex.toString();
			} else {
			    msg = ex.toString() + " - " +msg;
			}
			messager.printMessage(Kind.ERROR, msg);
			if (debug) ex.printStackTrace();
			// continue;
			return false;
		    }
		    if (dca != null) {
			int i;
			try {
			    for (i = 0; i < dca.length; i++) {
				String theHelper = dca[i].helper();
				if (!JavaIdents.isValidIdentifier(theHelper,
								  true)) {
				    messager.printMessage
					(Kind.ERROR,
					 "helper in @DMethodContext "
					 + "must name a fully qualified "
					 + "or simple class name",
					 ee);
				    continue;
				}
				if (!theHelper.contains(".") &&
				    currentPackageName != null) {
				    theHelper = currentPackageName
					+ "." + theHelper;
				}

				if (key.equals(theHelper)) {
				    localHelper = dca[i].localHelper();
				    if (!JavaIdents.isValidIdentifier
					(localHelper, false)) {
					messager.printMessage
					    (Kind.ERROR,
					     "helper in @DMethodContext "
					     + "must name a fully qualified "
					     + "or simple class name",
					     ee);
					continue;
				    }
				    String lhPackage = getPackage(localHelper);
				    // String currentPackageName=getPackage(e);
				    if (!localHelper.contains(".")
					&& currentPackageName != null) {
					localHelper =
					    currentPackageName +"."
					    + localHelper;
				    } else if ((currentPackageName == null &&
						localHelper.contains("."))
					       || (currentPackageName != null &&
						   !currentPackageName
						   .equals(lhPackage))) {
					messager.printMessage
					    (Kind.ERROR,
					     "localHelper in @DMethodContext "
					     +"must name a class in the "
					     +"current package",
					     ee);
					continue;
				    }
				    break;
				}
			    }
			    if (i == dca.length) {
				// nothing found.
				messager.printMessage(Kind.ERROR,
						      "missing @DMethodContext "
						      +"annotation",
						      ee);
				continue;
			    }
			    if (key.equals(localHelper)) {
				messager.printMessage(Kind.ERROR,
						      "@DMethodContext "
						      + "must not "
						      + "contain the same "
						      + "value for"
						      + "helper and "
						      + "localHelper",
						      ee);
				continue;
			    }
			} catch (Exception exdca) {
			    String msg = exdca.getMessage();
			    if (msg == null) msg = exdca.toString();
			    if (debug) exdca.printStackTrace();
			    messager.printMessage(Kind.ERROR, msg, ee);
			}
			if (debug) messager.printMessage(Kind.NOTE,
							 key +", "
							 + localHelper);
		    }

		    if (ea1 != null) {
			if (debug) messager.printMessage(Kind.NOTE,
							 "@DynamicMethod("
							 + key + ")");
			if (!add(key, data, baseType,
				 baseArgType, baseThrowables, baseReturnType,
				 baseOrder, baseIsVarArgs,
				 baseLockingMode, tracing, limitFactor)) {
			    messager.printMessage(Kind.ERROR,
						  "dublicate helpers among"
						  +" @Dispatch annotations",
						  e);
			    continue;
			}
		    }
		    if (ea2 != null) {
			if (debug) messager.printMessage(Kind.NOTE,
							 "@DMethodImpl("
							 + key + ")");
			if (!add(key, localHelper, data, baseType,
				 baseArgType, baseThrowables, baseReturnType,
				 baseOrder, baseIsVarArgs,
				 baseLockingMode, tracing, limitFactor)) {
			    messager.printMessage(Kind.ERROR,
						  "localHelper \""
						  +localHelper
						  +"\" multiply defined"
						  +" in DispatcherContext "
						  +"annotation(s)",
						  ee);
			    continue;
			}
		    }
		    if (ea1 == null && ea2 == null) {
			// error condition - should not occur
			continue;
		    }
		    if (debug) {
			messager.printMessage(Kind.NOTE,e +" in "
					      + e.getEnclosingElement());
		    }
		} catch (Exception exception) {
		    // catch-all.
		    messager.printMessage(Kind.ERROR, "DMethodProcessor: "
					  + exception.getMessage());
		}
	    }
        }
	for (String helper: dmap.keySet()) {
	    try {
		if (debug) messager.printMessage(Kind.NOTE,
						 "generating " + helper);
		if (hmap.get(helper) != null &&
		    hmap.get(helper).skip == false) {
		    generateHelper(filer, helper);
		}
	    } catch (Exception e) {
		String msg = e.getMessage();
		if (msg == null) {
		    msg = e.toString();
		} else {
		    msg = e.toString() + " in generateHelper - " +msg;
		}
		messager.printMessage(Kind.ERROR, msg);
	    }
	}
	for (String localhelper:  mmap.keySet()) {
	    try {
		if (debug) messager.printMessage(Kind.NOTE,
						 "generating " + localhelper);
		generateLocalHelper(filer, localhelper);
	    } catch (Exception e) {
		String msg = e.getMessage();
		if (msg == null) {
		    msg = e.toString();
		} else {
		    msg = e.toString() + " in generateLocalHelper - " +msg;
		}
		messager.printMessage(Kind.ERROR, msg);
	    }
	}
        return true;
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

    private String getPackage(String className) {
	int ind = className.lastIndexOf('.');
	if (ind == -1) return null;
	return className.substring(0, ind);
    }


    private int[] helperOrder(int[] order) {
	int i;
	int count = 0;
	for (i = 0; i < order.length; i++) {
	    if (debug) messager.printMessage(Kind.NOTE,
					     "orig order[" +i +"] = "
					     +order[i]);
	    if(order[i] <= 0) count++;
	    if (order[i] > order.length) return null;
	}
	if (debug) {
	    messager.printMessage(Kind.NOTE,
				  "order.length = " + order.length
				  +", count = " + count);
	}
	int[] result = new int[order.length - count];
	for (i = 0, count = 0; i < order.length; i++) {
	    if (order[i] > 0) {
		// result[order[i]-1] = i - count;
		result[order[i]-1] = i;
	    } else {
		count++;
	    }
	}
	return result;
    }

    private void generateDMethodHelperInfo(Writer hwriter,
					   String helper,
					   String baseType,
					   String[] baseArgType,
					   String[] baseThrowables,
					   String baseReturnType,
					   int[] baseOrder,
					   boolean baseIsVarArgs,
					   DMethodOptions.Locking
					   baseLockingMode,
					   boolean tracing, int limitFactor)
	throws IOException
    {
	hwriter.write("@org.bzdev.lang.annotations.DMethodHelperInfo(\n");
	hwriter.write("    helper = \"" +helper +"\",\n");
	hwriter.write("    baseType = \"" + baseType + "\",\n");
	if (baseArgType != null && baseArgType.length > 0) {
	    hwriter.write("    baseArgType = {");
	    if (baseArgType.length == 1) {
		hwriter.write("\"" + baseArgType[0] + "\"},\n");
	    } else {
		hwriter.write("\n");
		hwriter.write("\t\"" + baseArgType[0] + "\"");
		for (int i = 1; i < baseArgType.length; i++) {
		    hwriter.write(",\n\t\"" + baseArgType[i] + "\"");
		}
		hwriter.write("    },\n");
	    }
	}
	if (baseThrowables != null && baseThrowables.length > 0) {
	    hwriter.write("    baseThrowables = {");
	    if (baseThrowables.length == 1) {
		hwriter.write("\"" + baseThrowables[0] + "\"},\n");
	    } else {
		hwriter.write("\n");
		hwriter.write("\t\"" + baseThrowables[0] + "\"");
		for (int i = 1; i < baseThrowables.length; i++) {
		    hwriter.write(",\n\t\"" + baseThrowables[i] + "\"");
		}
		hwriter.write("    },\n");
	    }
	}
	hwriter.write("    baseReturnType = \"" + baseReturnType + "\",\n");
	if (baseOrder != null && baseOrder.length > 0) {
	    hwriter.write("    baseOrder = {");
	    if (baseOrder.length == 1) {
		hwriter.write(baseOrder[0] + "},\n");
	    } else {
		hwriter.write("\n");
		hwriter.write("\t" + baseOrder[0] + "");
		for (int i = 1; i < baseOrder.length; i++) {
		    hwriter.write(",\n\t" + baseOrder[i]);
		}
		hwriter.write("    },\n");
	    }
	}
	hwriter.write("    baseIsVarArgs = " + baseIsVarArgs + ",\n");
	hwriter.write("    baseLockingMode = "
		      + "org.bzdev.lang.DMethodOptions.Locking."
		      + baseLockingMode.toString() + ",\n");
	hwriter.write("    traceMode = " + tracing  +",\n");
	hwriter.write("    limitFactor = " + limitFactor);
	hwriter.write(")\n");
	return;
    }

    private void generateDMethodHelperInfo(StringBuilder sb,
					   String helper,
					   String baseType,
					   String[] baseArgType,
					   String[] baseThrowables,
					   String baseReturnType,
					   int[] baseOrder,
					   boolean baseIsVarArgs,
					   DMethodOptions.Locking
					   baseLockingMode,
					   boolean tracing, int limitFactor)
	throws IOException
    {
	sb.append("@org.bzdev.lang.annotations.DMethodHelperInfo(\n");
	sb.append("    helper = \"" +helper +"\",\n");
	sb.append("    baseType = \"" + baseType + "\",\n");
	if (baseArgType != null && baseArgType.length > 0) {
	    sb.append("    baseArgType = {");
	    if (baseArgType.length == 1) {
		sb.append("\"" + baseArgType[0] + "\"},\n");
	    } else {
		sb.append("\n");
		sb.append("\t\"" + baseArgType[0] + "\"");
		for (int i = 1; i < baseArgType.length; i++) {
		    sb.append(",\n\t\"" + baseArgType[i] + "\"");
		}
		sb.append("    },\n");
	    }
	}
	if (baseThrowables != null && baseThrowables.length > 0) {
	    sb.append("    baseThrowables = {");
	    if (baseThrowables.length == 1) {
		sb.append("\"" + baseThrowables[0] + "\"},\n");
	    } else {
		sb.append("\n");
		sb.append("\t\"" + baseThrowables[0] + "\"");
		for (int i = 1; i < baseThrowables.length; i++) {
		    sb.append(",\n\t\"" + baseThrowables[i] + "\"");
		}
		sb.append("    },\n");
	    }
	}
	sb.append("    baseReturnType = \"" + baseReturnType + "\",\n");
	if (baseOrder != null && baseOrder.length > 0) {
	    sb.append("    baseOrder = {");
	    if (baseOrder.length == 1) {
		sb.append(baseOrder[0] + "},\n");
	    } else {
		sb.append("\n");
		sb.append("\t" + baseOrder[0] + "");
		for (int i = 1; i < baseOrder.length; i++) {
		    sb.append(",\n\t" + baseOrder[i]);
		}
		sb.append("    },\n");
	    }
	}
	sb.append("    baseIsVarArgs = " + baseIsVarArgs + ",\n");
	sb.append("    baseLockingMode = "
		      + APACKAGE + ".DMethodOptions.Locking."
		      + baseLockingMode.toString() + ",\n");
	sb.append("    traceMode = " + tracing + ",\n");
	sb.append("    limitFactor = " + limitFactor);
	sb.append(")\n");
	return;
    }

    private static String printDateTime(Calendar cal) {
	return String.format("%tF %tT-00:00", cal, cal);
    }

    private void generateHelper(Filer filer, String helper) throws Exception {
	if (debug) messager.printMessage(Kind.NOTE,"in generateHelper");
	String[] baseArgType = hmap.get(helper).baseArgType;
	if (baseArgType.length > 1) {
	    generateMultiMethodHelper(filer, helper);
	    return;
	}
	KeyMap keyMap = new KeyMap();
	if (debug) messager.printMessage(Kind.NOTE, "using generateHelper");
	String baseType = hmap.get(helper).baseType;
	keyMap.put("baseArgType", baseArgType[0]);
	keyMap.put("baseType", baseType);
	int[] baseOrder = hmap.get(helper).baseOrder;
	String[] baseThrowables = hmap.get(helper).baseThrowables;
	if (baseThrowables != null && baseThrowables.length > 0) {
	    KeyMap[] map = new KeyMap[baseThrowables.length];
	    for (int k = 0; k < map.length; k++) {
		map[k] = new KeyMap();
		map[k].put("throwable", baseThrowables[k]);
		map[k].put("delim", ", ");
	    }
	    map[0].remove("delim");
	    KeyMap emap = new KeyMap();
	    emap.put("callThrowablesList", map);
	    keyMap.put("callThrowables", emap);
	}
	String baseReturnType = hmap.get(helper).baseReturnType;
	keyMap.put("baseReturnType", baseReturnType);
	if (baseReturnType.equals("void")) {
	    keyMap.put("returnExpr", "return");
	} else {
	    keyMap.put("returnsNonVoid", new KeyMap());
	    keyMap.put("resultEqual", baseReturnType + " result = ");
	    keyMap.put("returnExpr", "return result");
	}
	DMethodOptions.Locking baseLockingMode
	    = hmap.get(helper).baseLockingMode;
	boolean tracing = hmap.get(helper).tracing;
	int limitFactor = hmap.get(helper).limitFactor;
	String lockTypeString = "";
	String rLockString = "";
	String wLockString = "";
	boolean usesLock = true;
	switch(baseLockingMode) {
	case RWLOCK:
	    lockTypeString = "ReentrantReadWriteLock";
	    rLockString = "lock.readLock()";
	    wLockString = "lock.writeLock()";
	    break;
	case MUTEX:
	    lockTypeString = "ReentrantLock";
	    rLockString = "lock";
	    wLockString = "lock";
	    break;
	case NONE:
	    lockTypeString = "";
	    usesLock = false;
	    break;
	}
	if (usesLock) {
	    KeyMap map = new KeyMap();
	    map.put("lockTypeString", lockTypeString);
	    map.put("rLockString", rLockString);
	    map.put("wLockString", wLockString);
	    keyMap.put("usesLock", map);
	}
	if (tracing) {
	    keyMap.put("trace", new KeyMap());
	}
	keyMap.put("limitFactor", "" + limitFactor);
	Element helperElement = hmap.get(helper).execElement;
	String packageName = getPackage(helperElement);
	String helperName = helper;
	keyMap.put("helperName", helperName);
	boolean baseIsVarArgs = hmap.get(helper).baseIsVarArgs;
	if (packageName != null && helper.startsWith(packageName + ".")) {
		int start = packageName.length()+1;
		helperName = helper.substring(start);
	    }
	int lastInd = helperName.lastIndexOf(".");
	String simpleHelperName = helperName;
	if (lastInd >= 0) {
	    simpleHelperName = helperName.substring(lastInd);
	}
	keyMap.put("simpleHelperName", simpleHelperName);
	if (packageName != null && packageName.length() != 0) {
	    keyMap.put("package", "package " + packageName +";");
	}
	StringBuilder sb = new StringBuilder(1024);
	generateDMethodHelperInfo(sb, helper, baseType,
				  baseArgType, baseThrowables,
				  baseReturnType, baseOrder, baseIsVarArgs,
				  baseLockingMode, tracing, limitFactor);
	keyMap.put("annotation", sb.toString());
	sb = null;
	keyMap.put("generator",
		   PACKAGE + ".processor.DMethodProcessor");
	keyMap.put("date",
		   /*javax.xml.bind.DatatypeConverter.*/printDateTime
		   (Calendar.getInstance(TimeZone.getTimeZone("UTC"))));
	try {
	    if (debug) messager.printMessage(Kind.NOTE, "starting to write "
					     + helper);
	    // case where the annotation processor's code base can
	    // be used directly
	    InputStream is = this.getClass().getClassLoader()
		.getResourceAsStream(PACKAGE.replace(".", "/")
				     + "/processor/Helper.tpl");
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor module path
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
			 MPACKAGE + ".processor", "Helper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor module path; exclude module
		// name from second argument
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
			 PACKAGE + ".processor", "Helper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor path
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_PATH,
			 PACKAGE + ".processor", "Helper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the class path
		FileObject tfo = filer.getResource(StandardLocation.CLASS_PATH,
						   PACKAGE + ".processor",
						   "Helper.tpl");
		is = tfo.openInputStream();
	    }
	    InputStreamReader reader = new
		InputStreamReader(is, Charset.forName("UTF-8"));
	    JavaFileObject fo = filer.createSourceFile(helper);
	    Writer writer = fo.openWriter()
;
	    TemplateProcessor tp = new TemplateProcessor(keyMap);
	    tp.processTemplate(reader, writer);
	    reader.close();
	    writer.flush();
	    writer.close();
	} catch (Exception e) {
	    messager.printMessage(Kind.ERROR, "cannot generate file for "
				  + helperName);
	    messager.printMessage(Kind.ERROR, e.getMessage());
	}
    }


    private void generateMultiMethodHelper(Filer filer, String helper)
	throws Exception
    {
	KeyMap keyMap = new KeyMap();
	int i;
	if (debug) messager.printMessage(Kind.NOTE,
					 "Using generateMultiMethodHelper");
	String baseType = hmap.get(helper).baseType;
	keyMap.put("baseType", baseType);
	String[] baseArgType = hmap.get(helper).baseArgType;
	if (baseArgType != null && baseArgType.length > 0) {
	    KeyMap[] map = new KeyMap[baseArgType.length];
	    for (int k = 0; k < map.length; k++) {
		map[k] = new KeyMap();
		map[k].put("baseArgType", baseArgType[k]);
		map[k].put("delim", ", ");
		map[k].put("ind", "" + k);
	    }
	    map[0].remove("delim");
	    keyMap.put("baseArgTypeList", map);
	}
	String[] baseThrowables = hmap.get(helper).baseThrowables;
	if (baseThrowables != null && baseThrowables.length > 0) {
	    KeyMap[] map = new KeyMap[baseThrowables.length];
	    for (int k = 0; k < map.length; k++) {
		map[k] = new KeyMap();
		map[k].put("throwable", baseThrowables[k]);
		map[k].put("delim", ", ");
	    }
	    map[0].remove("delim");
	    KeyMap emap = new KeyMap();
	    emap.put("callThrowablesList", map);
	    keyMap.put("callThrowables", emap);
	}
	String baseReturnType = hmap.get(helper).baseReturnType;
	keyMap.put("baseReturnType", baseReturnType);
	if (!baseReturnType.equals("void")) {
	    keyMap.put("returnsNonVoid", new KeyMap());
	}

	int[] bOrder = hmap.get(helper).baseOrder;
	int[] order = helperOrder(bOrder);
	if (order != null && order.length > 0) {
	    KeyMap[] map = new KeyMap[order.length];
	    for (int k = 0; k < order.length; k++) {
		map[k] = new  KeyMap();
		map[k].put("ind", "" + order[k]);
		map[k].put("baseArgType", baseArgType[order[k]]);
		map[k].put("tdelim", ", ");
	    }
	    map[order.length-1].remove("tdelim");
	    keyMap.put("orderList", map);
	}

	DMethodOptions.Locking baseLockingMode
	    = hmap.get(helper).baseLockingMode;
	String lockTypeString = "";
	String rLockString = "";
	String wLockString = "";
	boolean usesLock = true;
	switch(baseLockingMode) {
	case RWLOCK:
	    lockTypeString = "ReentrantReadWriteLock";
	    rLockString = "lock.readLock()";
	    wLockString = "lock.writeLock()";
	    break;
	case MUTEX:
	    lockTypeString = "ReentrantLock";
	    rLockString = "lock";
	    wLockString = "lock";
	    break;
	case NONE:
	    lockTypeString = "";
	    usesLock = false;
	    break;
	}
	if (usesLock) {
	    KeyMap map = new KeyMap();
	    map.put("lockTypeString", lockTypeString);
	    map.put("rLockString", rLockString);
	    map.put("wLockString", wLockString);
	    keyMap.put("usesLock", map);
	}
	boolean tracing = hmap.get(helper).tracing;
	if (tracing) {
	    keyMap.put("trace", new KeyMap());
	}
	int limitFactor = hmap.get(helper).limitFactor;
	keyMap.put("limitFactor", "" + limitFactor);
	if (debug) {
	    for (i = 0; i < order.length; i++) {
		messager.printMessage(Kind.NOTE,
				      "order[" +i +"] = " +order[i]);
	    }
	}
	Element helperElement = hmap.get(helper).execElement;
	boolean baseIsVarArgs = hmap.get(helper).baseIsVarArgs;
	Writer hwriter = null;
	String packageName = getPackage(helperElement);
	String helperName = helper;
	keyMap.put("helperName", helperName);
	if (packageName != null && helper.startsWith(packageName + ".")) {
		int start = packageName.length()+1;
		helperName = helper.substring(start);
	    }
	int lastInd = helperName.lastIndexOf(".");
	String simpleHelperName = helperName;
	if (lastInd >= 0) {
	    simpleHelperName = helperName.substring(lastInd);
	}
	keyMap.put("simpleHelperName", simpleHelperName);
	if (packageName != null && packageName.length() != 0) {
	    keyMap.put("package", "package " + packageName + ";");
	}

	StringBuilder sb = new StringBuilder(1024);
	generateDMethodHelperInfo(sb, helper, baseType,
				  baseArgType, baseThrowables,
				  baseReturnType, bOrder, baseIsVarArgs,
				  baseLockingMode, tracing, limitFactor);
	keyMap.put("annotation", sb.toString());
	sb = null;

	if (baseArgType != null && baseArgType.length > 0) {
	    KeyMap[] maps = new KeyMap[baseArgType.length];
	    for (int k = 0; k < baseArgType.length; k++) {
		maps[k] = new KeyMap();
		maps[k].put("baseArgType", baseArgType[k]);
		maps[k].put("ind", "" + k);
	    }
	    keyMap.put("callArgs", maps);
	}
	if (baseReturnType.equals("void")) {
	    keyMap.put("returnExpr", "return");
	} else {
	    keyMap.put("resultEqual", baseReturnType + " result = ");
	    keyMap.put("returnExpr", "return result");
	}
	try {
	    keyMap.put("generator",
		       PACKAGE + ".processor.DMethodProcessor");
	    keyMap.put("date",
		       /*javax.xml.bind.DatatypeConverter.*/printDateTime
		       (Calendar.getInstance(TimeZone.getTimeZone("UTC"))));

	    // class-loader-for-this-processor case
	    InputStream is = this.getClass().getClassLoader()
		.getResourceAsStream(PACKAGE.replace(".", "/")
				     + "/processor/MultiMethodHelper.tpl");
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor module path
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
			 MPACKAGE + ".processor", "MultiMethodHelper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor module path; exclude module
		// name from second argument
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH,
			 PACKAGE + ".processor", "MultiMethodHelper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// case where the annotation processor's code base is on
		// the annotation processor path
		try {
		    FileObject tfo = filer.getResource
			(StandardLocation.ANNOTATION_PROCESSOR_PATH,
			 PACKAGE + ".processor", "MultiMethodHelper.tpl");
		    is = tfo.openInputStream();
		} catch (Exception tfoe) {
		}
	    }
	    if (is == null) {
		// Class-path case
		FileObject tfo = filer.getResource(StandardLocation.CLASS_PATH,
						   PACKAGE + ".processor",
						   "MultiMethodHelper.tpl");
		is = tfo.openInputStream();
	    }

	    InputStreamReader reader = new
		InputStreamReader(is, Charset.forName("UTF-8"));
	    JavaFileObject fo = filer.createSourceFile(helper);
	    Writer writer = fo.openWriter();
	    TemplateProcessor tp = new TemplateProcessor(keyMap);
	    tp.processTemplate(reader, writer);
	    reader.close();
	    writer.flush();
	    writer.close();
	} catch (Exception e) {
	    messager.printMessage(Kind.ERROR, "cannot generate file for "
				  + helperName);
	    messager.printMessage(Kind.ERROR, e.getMessage());
	}
    }

    private void generateLocalHelper(Filer filer, String localhelper)
	throws Exception
    {
	if (localhelper == null) return;
	if (hmap.get(localhelper) == null) return;
	String[] baseArgType =
	    hmap.get(localhelper).baseArgType;
	if (baseArgType == null) return;
	if (baseArgType.length > 1) {
	    generateMMLocalHelper(filer, localhelper);
	    return;
	}
	String[] baseThrowables = hmap.get(localhelper).baseThrowables;
	String baseReturnType = hmap.get(localhelper).baseReturnType;
	String helper = hmap.get(localhelper).helper;
	String baseType = hmap.get(localhelper).baseType;
	Element localElement = hmap.get(localhelper).execElement;
	String packageName = getPackage(localElement);
	String localhelperName = localhelper;
	if (packageName != null &&
	    localhelperName.startsWith(packageName + ".")) {
	    int start = packageName.length()+1;
	    localhelperName = localhelper.substring(start);
	}
	Element lhe = hmap.get(localhelper).execElement;
	TypeElement lhte =
	    (lhe instanceof TypeElement)? (TypeElement) lhe: null;
	// String localType =
	//    hmap.get(localhelper).execElement.getSimpleName().toString();
	String localType = (lhe == null)? lhe.getSimpleName().toString():
	    lhte.getQualifiedName().toString();

	// String methodArgumentType = dmap.get(helper).methodArgumentType;
	List<DispatcherData> list = mmap.get(localhelper);
	int i = 0;
	JavaFileObject fo = filer.createSourceFile(localhelper);
	Writer dwriter = fo.openWriter();

	if (packageName != null && packageName.length() != 0)
	    dwriter.write("package " +packageName +";\n");

	dwriter.write
	    ("class " +localhelperName +" extends " +helper +" {\n"
	     +"    static " +localhelperName +" helper = new "
	     +localhelperName +"();\n"
	     +"    static void register() {}\n");
	for (DispatcherData data: list) {
	    i++;
	    dwriter.write
		("    " +helper +".Caller caller" +i +" =\n"
		 +"         new " +helper +".Caller() {\n"
		 +"            public " +baseReturnType
		 +" call(" +baseType +" obj, "
		 +baseArgType[0] +" msg)");
	    if (baseThrowables != null && baseThrowables.length > 0) {
		dwriter.write
		    ("            throws " +baseThrowables[0]);
		for (int j = 1; j < baseThrowables.length; j++) {
		    dwriter.write(", " +baseThrowables[j]);
		}
 	    }
	    dwriter.write
		("            {\n"
		 +"             " +(baseReturnType.equals("void")?"":"return ")
		 +"((" +localType +")obj)."
		 +data.methodName +"((" +data.methodArgumentType[0] +")msg);\n"
		 +"            }\n"
		 +"         };\n");
	}
	dwriter.write
	    ("    " +localhelperName +"() {\n"
	     +"        register(" +localType +".class);\n");
	i = 0;
	for (DispatcherData data: list) {
	    i++;
	    dwriter.write
		("            addDispatch(" +data.methodArgumentType[0]
		 +".class, caller" +i +");\n");
	}
	dwriter.write("            addDispatchComplete();\n");
	dwriter.write
	    ("    }\n"
	     +"}\n");
	dwriter.flush();
	dwriter.close();
    }

    private void generateMMLocalHelper(Filer filer, String localhelper)
	throws Exception
    {
	if (localhelper == null) return;
	String helper = hmap.get(localhelper).helper;
	if (debug) messager.printMessage(Kind.NOTE, helper +":" +localhelper);
	String baseType = hmap.get(localhelper).baseType;
	Element localElement = hmap.get(localhelper).execElement;
	String packageName = getPackage(localElement);
	String localhelperName = localhelper;
	if (packageName != null &&
	    localhelperName.startsWith(packageName + ".")) {
	    int start = packageName.length()+1;
	    localhelperName = localhelper.substring(start);
	}
	int[] order = helperOrder(hmap.get(localhelper).baseOrder);
	if (debug) {
	    for (int i = 0; i < order.length; i++) {
		messager.printMessage(Kind.NOTE,"[MM " + localhelper
				      +"] order[" + i + "] = "
				      + order[i]);
	    }
	}

	if (hmap.get(localhelper) == null) return;
	if (hmap.get(localhelper).execElement == null) return;
	if (hmap.get(localhelper).baseArgType == null) return;

	String localType =
	    hmap.get(localhelper).execElement.getSimpleName().toString();
	String[] baseArgType =  hmap.get(localhelper).baseArgType;
	if (baseArgType == null) return;
	String[] baseThrowables = hmap.get(localhelper).baseThrowables;
	if (hmap.get(helper) == null) {
	    messager.printMessage(Kind.ERROR, "Dynamic method info for "
				  + helper + " is missing");
	    return;
	}
	String baseReturnType = hmap.get(helper).baseReturnType;
	List<DispatcherData> list = mmap.get(localhelper);
	int i = 0;
	int j;
	JavaFileObject fo = filer.createSourceFile(localhelper);
	Writer dwriter = fo.openWriter();

	if (packageName != null && packageName.length() != 0)
	    dwriter.write("package " +packageName +";\n");

	dwriter.write
	    ("class " +localhelperName +" extends " +helper +" {\n"
	     +"    static " +localhelperName +" helper = new "
	     +localhelperName +"();\n"
	     +"    static void register() {}\n");
	for (DispatcherData data: list) {
	    i++;
	    dwriter.write
		("    " +helper +".Caller caller" +i +" =\n"
		 +"         new " +helper +".Caller() {\n"
		 +"                public " +baseReturnType
		 +" call(" +baseType +" obj");
	    for (j = 0; j < baseArgType.length; j++) {
		dwriter.write(", " +baseArgType[j] +" arg" +j);
	    }
	    dwriter.write(")\n");
	    if (baseThrowables != null && baseThrowables.length > 0) {
		dwriter.write
		    ("            throws " +baseThrowables[0]);
		for (j = 1; j < baseThrowables.length; j++) {
		    dwriter.write(", " +baseThrowables[j]);
		}
	    }
	    dwriter.write
		("                {\n"
		 +"                   "
		 +(baseReturnType.equals("void")? "": "return ")
		 +"((" +localType +")obj)."
			  +data.methodName +"(");
	    for (j = 0; j <data.methodArgumentType.length; j++) {
		if (j > 0) dwriter.write
			       (",\n"
				+"                              ");
		dwriter.write
		    ("(" +data.methodArgumentType[j] +")arg" +j);
	    }

	    dwriter.write
		(");\n"
		 +"            }\n"
		 +"         };\n");
	}
	dwriter.write
	    ("    " +localhelperName +"() {\n"
	     +"        register(" +localType +".class);\n");
	i = 0;
	for (DispatcherData data: list) {
	    i++;
	    dwriter.write("        Class[] types" +i +" = new Class["
			  +order.length +"];\n");
	    for (j = 0; j < order.length; j++) {
	       dwriter.write("        types" +i +"[" +j +"] = "
			     +data.methodArgumentType[order[j]]
			     +".class;\n");
	    }

	    dwriter.write
		("        addDispatch(types" +i +", caller" +i +");\n");
	}
	dwriter.write("        addDispatchComplete();\n");

	dwriter.write
	    ("    }\n"
	     +"}\n");
	dwriter.flush();
	dwriter.close();
    }
}

//  LocalWords:  SupportedAnnotationTypes stdout DynamicMethod hdata
//  LocalWords:  DMethodImpl addHdata execElement baseType dmap MUTEX
//  LocalWords:  assignability DMethodOptions lockingMode RWLOCK te
//  LocalWords:  DMethodProcessor DMethodOrder throwable enums cfg tF
//  LocalWords:  DispatcherContext limitFactor DMethodOption tT delim
//  LocalWords:  currentPackageName getPackage helperClass anotation
//  LocalWords:  baseArgType DMethodHelperInfo dublicate localHelper
//  LocalWords:  DMethodContext generateHelper generateLocalHelper
//  LocalWords:  baseThrowables baseReturnType baseOrder traceMode
//  LocalWords:  baseIsVarArgs baseLockingMode callThrowablesList tpl
//  LocalWords:  callThrowables returnExpr resultEqual readLock UTF
//  LocalWords:  ReentrantReadWriteLock writeLock ReentrantLock msg
//  LocalWords:  lockTypeString rLockString wLockString usesLock arg
//  LocalWords:  helperName simpleHelperName baseArgTypeList tdelim
//  LocalWords:  generateMultiMethodHelper orderList callArgs
//  LocalWords:  MultiMethodHelper localType methodArgumentType
//  LocalWords:  addDispatch addDispatchComplete
