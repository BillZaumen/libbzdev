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

@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParmManagerProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
	// return the the current version or RELEASE_7, whichever
	// is more recent.  This will prevent the annotation processor
	// from being used on releases earlier than RELEASE_7.
	SourceVersion cversion = super.getSupportedSourceVersion();
	SourceVersion version = SourceVersion.latest();
	return (cversion.ordinal() > version.ordinal())? cversion: version;
    }

    private static final boolean DEBUG = false;

    private static final String STRING = "java.lang.String";

    private static final String PACKAGE = "org.bzdev.obnaming";
    private static final String APACKAGE = PACKAGE + ".annotations";
    private static final String PPACKAGE = PACKAGE + ".processor";

    private static final Set<String> supportedAnnotationTypes =
	new HashSet<String>();
    static {
	supportedAnnotationTypes.add(APACKAGE + ".FactoryParmManager");
	supportedAnnotationTypes.add(APACKAGE + ".CompoundParmType");
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

    private static final String RVPKG = "org.bzdev.math.rv";
    private static final int PL = RVPKG.length();

    TypeMirror getRVBaseType(Types types, Element e) {
	TypeMirror tm = e.asType();
	return getRVBaseType(types, tm);
    }
    TypeMirror getRVBaseType(Types types, TypeMirror tm) {
	for(;;) {
	    String fqn = types.erasure(tm).toString();
	    if (fqn.equals(RVPKG + ".IntegerRandomVariable")
		|| fqn.equals(RVPKG + ".LongRandomVariable")
		|| fqn.equals(RVPKG + ".DoubleRandomVariable")
		|| fqn.equals(RVPKG + ".BooleanRandomVariable")
		|| fqn.equals(RVPKG + ".IntegerRandomVariableRV")
		|| fqn.equals(RVPKG + ".LongRandomVariableRV")
		|| fqn.equals(RVPKG + ".DoubleRandomVariableRV")
		|| fqn.equals(RVPKG + ".BooleanRandomVariableRV"))
		return tm;
	    List<? extends TypeMirror>list = types.directSupertypes(tm);
	    if (list.size() == 0) return null;
	    tm = list.get(0);
	}
    }

    boolean implementsNamedObjectOps(Types types, TypeMirror tm) {
	return implementsAux(types, tm, NAMEDOBJECTOPS_MIRROR);
    }

    boolean isEnum(Types types, TypeMirror tm) {
	List<? extends TypeMirror>list = types.directSupertypes(tm);
	if (list.size() == 0) {
	    return false;
	}
	TypeMirror sctm = list.get(0);
	sctm = types.erasure(sctm);
	return types.isSameType(sctm, ENUM_MIRROR);
    }


    boolean isSet(Types types, TypeMirror tm) {
	TypeMirror esctm = types.erasure(tm);
	if (types.isSameType(esctm, ERASED_SET_MIRROR)) return true;
	return implementsAuxErased(types, tm, ERASED_SET_MIRROR);
    }

    String getBaseParmTest(Types types, TypeMirror tm) {
	if(implementsNamedObjectOps(types, tm)) {
	    return "value instanceof " + types.erasure(tm).toString();
	} else if (isEnum(types, tm)) {
	    return "value instanceof " + tm.toString();
	} else if (types.isSameType(tm, INTEGER_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.INT))) {
	    return "true";
	} else if (types.isSameType(tm, DOUBLE_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.DOUBLE))) {
	    return "true";
	} else if (types.isSameType(tm, LONG_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.LONG))) {
	    return "true";
	} else if (types.isSameType(tm, BOOLEAN_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.BOOLEAN))) {
	    return "true";
	}
	return "true";
    }

    String getNumberClass(Types types, TypeMirror tm) {
	if (types.isSameType(tm, INTEGER_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.INT))) {
	    return "java.lang.Integer";
	} else if (types.isSameType(tm, DOUBLE_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.DOUBLE))) {
	    return "java.lang.Double";
	} else if (types.isSameType(tm, LONG_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.LONG))) {
	    return "java.lang.Long";
	} else {
	    return null;
	}
    }

    String getBaseParmType(Types types, TypeMirror tm) {
	if(implementsNamedObjectOps(types, tm)) {
	    return "org.bzdev.obnaming.NamedObjectOps";
	} else if (isEnum(types, tm)) {
	    return "java.lang.Enum<?>";
	} else if (types.isSameType(tm, INTEGER_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.INT))) {
	    return "int";
	} else if (types.isSameType(tm, DOUBLE_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.DOUBLE))) {
	    return "double";
	} else if (types.isSameType(tm, LONG_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.LONG))) {
	    return "long";
	} else if (types.isSameType(tm, BOOLEAN_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.BOOLEAN))) {
	    return "boolean";
	} else if (types.isSameType(tm, STRING_MIRROR)) {
	    return "String";
	}
	return null;
    }

    String getBaseKeyParmType(Types types, TypeMirror tm) {
	if(implementsNamedObjectOps(types, tm)) {
	    return "org.bzdev.obnaming.NamedObjectOps";
	} else if (isEnum(types, tm)) {
	    return "java.lang.Enum<?>";
	} else if (types.isSameType(tm, INTEGER_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.INT))) {
	    return "int";
	} else {
	    return null;
	}
    }

    String getBaseParmConv(Types types, TypeMirror tm) {
	if(implementsNamedObjectOps(types, tm)) {
	    return "(" + tm.toString() + ")";
	} else if (isEnum(types, tm)) {
	    return "(" + tm.toString() + ")";
	} else if (types.isSameType(tm, INTEGER_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.INT))) {
	    return "Integer.valueOf";
	} else if (types.isSameType(tm, DOUBLE_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.DOUBLE))) {
	    return "Double.valueOf";
	} else if (types.isSameType(tm, LONG_MIRROR)
		   || types.isSameType(tm,
				       types.getPrimitiveType(TypeKind.LONG))) {
	    return "Long.valueOf";
	} else if (types.isSameType(tm, BOOLEAN_MIRROR)
		   || types.isSameType(tm,
				       types
				       .getPrimitiveType(TypeKind.BOOLEAN))) {
	    return "Boolean.valueOf";
	}
	return "";
    }


    // Using this because types.isAssignable doesn't seem to work,
    // possibly because some of the helper classes that are part of
    // the class heirarchy may not yet have been created.
    boolean implementsAux(Types types, TypeMirror tm, TypeMirror target) {
	List<? extends TypeMirror>list = types.directSupertypes(tm);
	int n = list.size();
	if (n == 0) return false;
	TypeMirror next = null;
	for (TypeMirror tm2: list) {
	    if (next == null) {
		next = tm2;
		continue;
	    }
	    if (types.isSameType(tm2, target)) return true;
	    // if (tm2.toString().equals(target.toString())) return true;
	}
	return implementsAux(types, next, target);
    }

    boolean implementsAuxErased(Types types, TypeMirror tm, TypeMirror target) {
	List<? extends TypeMirror>list = types.directSupertypes(tm);
	int n = list.size();
	if (n == 0) return false;
	TypeMirror next = null;
	for (TypeMirror tm2: list) {
	    if (next == null) {
		next = tm2;
		continue;
	    }
	    if (types.isSameType(types.erasure(tm2), target)) return true;
	}
	return implementsAuxErased(types, next, target);
    }


    boolean implementsCloneable(Types types, TypeMirror tm) {
	return implementsAux(types, tm, CLONEABLE_MIRROR);
    }

    TypeMirror typeArgumentForSet(Types types, TypeMirror type) {
	if (type == null) return null;
	TypeMirror typeErasure = types.erasure(type);
	// if (!types.isSameType(typeErasure, ERASED_SET_MIRROR)) return null;
	if (!isSet(types, type)) return null;
	if (type.getKind() == TypeKind.DECLARED &&
	    type instanceof DeclaredType) {
	    List<? extends TypeMirror> list =
		((DeclaredType)type).getTypeArguments();
	    if (list.size() == 1) {
		return list.get(0);
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }

    TypeMirror keyTypeForMap(Types types, TypeMirror type) {
	if (type == null) return null;
	TypeMirror typeErasure = types.erasure(type);
	// if (!types.isSameType(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (!types.isAssignable(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (type.getKind() == TypeKind.DECLARED &&
	    type instanceof DeclaredType) {
	    List<? extends TypeMirror> list =
		((DeclaredType)type).getTypeArguments();
	    if (list.size() == 2) {
		TypeMirror result = list.get(0);
		if (types.isSameType(result, INTEGER_MIRROR)) {
		    result = types.unboxedType(INTEGER_MIRROR);
		}
		return result;
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }

    TypeMirror rawKeyTypeForMap(Types types, TypeMirror type) {
	if (type == null) return null;
	TypeMirror typeErasure = types.erasure(type);
	// if (!types.isSameType(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (!types.isAssignable(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (type.getKind() == TypeKind.DECLARED &&
	    type instanceof DeclaredType) {
	    List<? extends TypeMirror> list =
		((DeclaredType)type).getTypeArguments();
	    if (list.size() == 2) {
		TypeMirror result = list.get(0);
		return result;
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }

    TypeMirror valueTypeForMap(Types types, TypeMirror type) {
	if (type == null) return null;
	TypeMirror typeErasure = types.erasure(type);
	// if (!types.isSameType(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (!types.isAssignable(typeErasure, ERASED_MAP_MIRROR)) return null;
	if (type.getKind() == TypeKind.DECLARED &&
	    type instanceof DeclaredType) {
	    List<? extends TypeMirror> list =
		((DeclaredType)type).getTypeArguments();
	    if (list.size() == 2) {
		return list.get(1);
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }


    boolean getCloneFlag( Types types, TypeMirror tm) {
	TypeKind tk = tm.getKind();
	switch (tk) {
	case BOOLEAN:
	case INT:
	case LONG:
	case DOUBLE:
	case BYTE:
	case CHAR:
	case SHORT:
	case FLOAT:
	case NULL:
	    return false;
	default:
	    if (isEnum(types, tm)) return false;
	    if (isSet(types, tm)) return false;
	    if (implementsNamedObjectOps(types, tm)) return false;
	    return implementsCloneable(types, tm);
	}
    }

    // used for primitive parms
    TypeMirror getParmType(Types types, Element e, boolean rvmode) {
	return getParmType(types, e.asType(), rvmode);
    }
    TypeMirror getParmType(Types types, TypeMirror etm, boolean rvmode) {
	TypeMirror tm = etm;
	return getParmType(types, etm, tm, rvmode);
    }

    // used by keyed parms
    TypeMirror getParmType(Types types, TypeMirror tm) {
	if (types.isSameType(tm, INTEGER_MIRROR)) {
		return types.unboxedType(INTEGER_MIRROR);
	}
	Element e = (tm.getKind() == TypeKind.DECLARED)?
	    (((DeclaredType)tm).asElement()): null;
	return getParmType(types, e, tm, false);
    }

    TypeMirror getParmType(Types types, Element e, TypeMirror tm,
			   boolean rvmode)
    {
	return getParmType(types, ((e == null)? null: e.asType()), tm, rvmode);
    }

    TypeMirror getParmType(Types types, TypeMirror etm, TypeMirror tm,
			   boolean rvmode)
    {
	TypeKind tk = tm.getKind();
	switch (tk) {
	case BOOLEAN:
	case INT:
	case LONG:
	case DOUBLE:
	    if (rvmode) return null;
	    return tm;
	case DECLARED:
	    if (isEnum(types, tm)) {
		return STRING_MIRROR;
	    }
	    TypeMirror tm1 = (etm == null)? null: getRVBaseType(types, etm);
	    if (tm1 != null) {
		return types.erasure(tm1);
	    } else {
		if (implementsNamedObjectOps(types, tm)) {
		    return STRING_MIRROR;
		} else if (types.isSameType(tm, STRING_MIRROR)) {
		    return tm;
		}
		return null;
	    }
	default:
	    return null;
	}
    }

    TypeMirror getRvType(Types types, Element e, boolean rvmode) {
	return getRvType(types, e.asType(), rvmode);
    }

    TypeMirror getRvType(Types types, TypeMirror etm, boolean rvmode) {
	if (rvmode) {
	    return etm;
	} else {
	    return null;
	}
    }


    TypeMirror INTEGER_RV_MIRROR;
    TypeMirror LONG_RV_MIRROR;
    TypeMirror DOUBLE_RV_MIRROR;
    TypeMirror BOOLEAN_RV_MIRROR;
    TypeMirror INTEGER_MIRROR;
    TypeMirror LONG_MIRROR;
    TypeMirror BOOLEAN_MIRROR;
    TypeMirror DOUBLE_MIRROR;
    TypeMirror STRING_MIRROR;
    TypeMirror NAMEDOBJECTOPS_MIRROR;
    TypeMirror CLONEABLE_MIRROR;
    TypeMirror ERASED_SET_MIRROR;
    TypeMirror ERASED_MAP_MIRROR;
    TypeMirror ENUM_MIRROR;
    TypeMirror ENUM_SET_MIRROR;


    TypeMirror getType(Types types, Element e, boolean rvmode) {
	return getType(types, e.asType(), rvmode);
    }

    TypeMirror getType(Types types, TypeMirror etm, boolean rvmode) {
	if (rvmode) {
	    TypeMirror tm = getRVBaseType(types, etm);
	    if (tm == null) {
		return etm;
	    } else if (tm instanceof DeclaredType) {
		List<? extends TypeMirror> list =
		    ((DeclaredType)tm).getTypeArguments();
		if (list.size() == 0) {
		    if (types.isSameType(tm, INTEGER_RV_MIRROR))
			return types.getPrimitiveType(TypeKind.INT);
		    else if (types.isSameType(tm, LONG_RV_MIRROR))
			return types.getPrimitiveType(TypeKind.LONG);
		    else if (types.isSameType(tm, DOUBLE_RV_MIRROR))
			return types.getPrimitiveType(TypeKind.DOUBLE);
		    else if (types.isSameType(tm, BOOLEAN_RV_MIRROR))
			return types.getPrimitiveType(TypeKind.BOOLEAN);
		    else return null;
		}
		if (list.size() == 1) {
			return list.get(0);
		} else {
		    return null;
		}
	    }
	}
	return etm;
    }


    String getRVParmType(Types types, Element e, boolean rvmode) {
	TypeMirror etm = e.asType();
	return getRVParmType(types, etm, rvmode);
    }

    String getRVParmType(Types types, TypeMirror etm, boolean rvmode) {
	TypeMirror tm = getRVBaseType(types, etm);
	if (tm == null) return null;
	String tname = etm.toString();

	if (tname.equals(RVPKG + ".IntegerRandomVariable")) {
	    return (rvmode? "int": tname);
	} else if (tname.equals(RVPKG + ".LongRandomVariable")) {
	    return (rvmode? "long": tname);
	} else if (tname.equals(RVPKG + ".DoubleRandomVariable")) {
	    return (rvmode? "double": tname);
	} else if (tname.equals(RVPKG + ".BooleanRandomVariable")) {
	    return (rvmode? "boolean": tname);
	} else if (tname.equals(RVPKG + ".IntegerRandomVariableRV")) {
	    return (rvmode? RVPKG + ".IntegerRandomVariable": tname);
	} else if(tname.equals(RVPKG + ".LongRandomVariableRV")) {
	    return (rvmode? RVPKG + ".LongRandomVariable": tname);
	} else if (tname.equals(RVPKG + ".DoubleRandomVariableRV")) {
	    return (rvmode? RVPKG + ".DoubleRandomVariable": tname);
	} else if (tname.equals(RVPKG + ".BooleanRandomVariableRV")) {
	    return (rvmode? RVPKG + ".BooleanRandomVariable": tname);
	} else if (tname.startsWith(RVPKG + ".IntegerRandomVariableRV<")) {
	    return (rvmode? tname.substring(PL+25,tname.length()-1): tname);
	} else if (tname.startsWith(RVPKG + ".LongRandomVariableRV<")) {
	    return (rvmode? tname.substring(PL+22,tname.length()-1): tname);
	} else if (tname.startsWith(RVPKG + ".DoubleRandomVariableRV<")) {
	    return (rvmode? tname.substring(PL+24,tname.length()-1): tname);
	} else if (tname.startsWith(RVPKG + ".BooleanRandomVariableRV<")) {
	    return (rvmode? tname.substring(PL+25,tname.length()-1): tname);
	} else {
	    return null;
	}
    }

    String getRVBoundType(Types types, Element e) {
	return getRVBoundType(types, e.asType());
    }

    String getRVBoundType(Types types, TypeMirror etm) {
	TypeMirror tm = getRVBaseType(types, etm);
	if (tm == null) return null;
	String tname = etm.toString();
	if (tname.equals(RVPKG + ".IntegerRandomVariable")) {
	    return "java.lang.Integer";
	} else if (tname.equals(RVPKG + ".LongRandomVariable")) {
	    return "java.lang.Long";
	} else if (tname.equals(RVPKG + ".DoubleRandomVariable")) {
	    return "java.lang.Double";
	} else if (tname.equals(RVPKG + ".IntegerRandomVariableRV")) {
	    return "java.lang.Integer";
	} else if(tname.equals(RVPKG + ".LongRandomVariableRV")) {
	    return "java.lang.Long";
	} else if (tname.equals(RVPKG + ".DoubleRandomVariableRV")) {
	    return "java.lang.Double";
	} else if (tname.startsWith(RVPKG + ".IntegerRandomVariableRV<")) {
	    return "java.lang.Integer";
	} else if (tname.startsWith(RVPKG + ".LongRandomVariableRV<")) {
	    return "java.lang.Long";
	} else if (tname.startsWith(RVPKG + ".DoubleRandomVariableRV<")) {
	    return "java.lang.Double";
	} else {
	    return null;
	}
    }

    HashMap<String,TypeElement> makeTmap(Types types, TypeElement e) {
	HashMap<String,TypeElement> tmap = new HashMap<>();
	for (TypeParameterElement ne:
		 ((TypeElement)e).getTypeParameters()) {
	    List<? extends TypeMirror> bounds = ne.getBounds();
	    if (bounds.size() == 1) {
		TypeMirror tm = bounds.get(0);
		if (tm.getKind() == TypeKind.DECLARED) {
		    Element ee = types.asElement(tm);
		    if (ee instanceof TypeElement) {
			tmap.put(ne.toString(), (TypeElement)ee);
		    }
		}
	    }
	}
	return tmap;
    }

    void substTypeParms(StringBuilder sb, Types types,
			HashMap<String,TypeElement> tmap,
			TypeMirror tm)
    {
	// Replace a type parameter for the actual type where
	// possible.
	Element ee = types.asElement(tm);
	if (ee instanceof TypeElement) {
	    List<? extends TypeParameterElement> tpelist
		= ((TypeElement) ee).getTypeParameters();
	    if (tpelist.size() > 0) {
		sb.append(types.erasure(tm).toString());
		sb.append("<");
		boolean needComma = false;
		for (TypeParameterElement tpe: tpelist) {
		    String tmapkey = tpe.toString();
		    if (tmap.containsKey(tmapkey)) {
			if (needComma) sb.append(",");
			sb.append(tmap.get(tmapkey).toString());
			needComma = true;
		    }
		}
		sb.append(">");
	    } else {
		sb.append(tm.toString());
	    }
	}
    }

    String getTypeParmClasses(Types types, Element e) {
	if (e instanceof TypeElement) {
	    // TypeParameterElement tpe = null;
	    boolean notFirst = false;
	    StringBuilder sb = new StringBuilder();
	    HashMap<String,TypeElement> tmap = makeTmap(types, (TypeElement)e);
	    for (TypeParameterElement ne:
		     ((TypeElement)e).getTypeParameters()) {
		// tpe = ne;
		List<? extends TypeMirror> bounds = ne.getBounds();
		if (bounds.size() == 1) {
		    TypeMirror tm = bounds.get(0);
		    if (tm.getKind() == TypeKind.DECLARED) {
			if (notFirst) {
			    sb.append(",");
			}
			substTypeParms(sb, types, tmap, tm);
		    } else {
			return null;
		    }
		} else {
		    return null;
		}
		notFirst = true;
	    }
	    if (notFirst) {
		return sb.toString();
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }


    String getNamedObjectClass(Types types, Element e) {
	if (e instanceof TypeElement) {
	    HashMap<String,TypeElement> tmap = makeTmap(types, (TypeElement)e);
	    TypeParameterElement tpe = null;
	    for (TypeParameterElement ne:
		     ((TypeElement)e).getTypeParameters()) {
		tpe = ne;
	    }
	    // want the last one.
	    if (tpe == null) {
		return null;
	    }
	    List<? extends TypeMirror> bounds = tpe.getBounds();
	    if (bounds.size() == 1) {
		TypeMirror tm = bounds.get(0);
		if (tm.getKind() == TypeKind.DECLARED) {
		    StringBuilder sb = new StringBuilder();
		    substTypeParms(sb, types, tmap, tm);
		    return sb.toString();
		    // return tm.toString();
		} else {
		    return null;
		}
	    } else {
		return null;
	    }
	} else {
	    return null;
	}
    }

    private static final String CONSTRUCTOR_NAME = "<init>";

    String getObjectNamer(Elements elements, Element e) {
	Name constructor = elements.getName("<init>");
	if (e instanceof TypeElement) {
	    TypeElement te = (TypeElement) e;
	    Name simpleName = te.getSimpleName();
	    for (Element ee: te.getEnclosedElements()) {
		if (ee instanceof ExecutableElement) {
		    ExecutableElement ex = (ExecutableElement) ee;
		    if (ex.getSimpleName().equals(constructor)) {
			TypeMirror extm = ee.asType();
			if (extm instanceof ExecutableType) {
			    ExecutableType ext = (ExecutableType) extm;
			    List<? extends TypeMirror> types =
				ext.getParameterTypes();
			    if (types.size() == 1) {
				TypeMirror tm = types.get(0);
				if (tm.getKind() == TypeKind.DECLARED) {
				    DeclaredType dt = (DeclaredType) tm;
				    Element eee = dt.asElement();
				    if (eee instanceof TypeElement) {
					TypeElement te2 = (TypeElement) eee;
					return
					    te2.getQualifiedName().toString();
				    }
				}
			    }
			}
		    }
		}
	    }
	}
	return null;
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

    String getPackagePath(String pkg) {
	if (pkg != null) pkg = pkg.trim();
	if (pkg == null || pkg.length() == 0) {
	    return "";
	} else {
	    return pkg.replaceAll("[.]", "/");
	}
    }

    String getFormalParms(Element e, boolean showBounds) {
	String formalParameters = null;
	if (e instanceof TypeElement) {
	    for (TypeParameterElement tpe:
		     ((TypeElement) e).getTypeParameters())
		{
		    if (formalParameters == null) {
			formalParameters = "<" + tpe.toString();
		    } else {
			formalParameters = formalParameters
			    + "," + tpe.toString();
		    }
		    if (showBounds) {
			String bounds = null;
			for (TypeMirror tm: tpe.getBounds()) {
			    if (bounds == null) {
				bounds = " extends ";
			    } else {
				bounds = bounds + " & ";
			    }
			    bounds = bounds + tm.toString();
			}
			if (bounds != null) {
			    formalParameters = formalParameters + bounds;
			}
		    }
		}
	    if (formalParameters != null)
		formalParameters = formalParameters + ">";
	}
	return formalParameters;
    }
    Messager messager;

    private void processPrimitive(Types types, Elements elements,
				  ArrayList<TemplateProcessor.KeyMap> alist,
				  PrimitiveParm ppa,
				  Element ee)
    {
	processPrimitive(types, elements, alist, ppa, ee, false);
    }

    private void processPrimitive(Types types, Elements elements,
				  TemplateProcessor.KeyMap eetmKeymap,
				  KeyedPrimitiveParm ppa,
				  TypeMirror eetm,
				  boolean diffKeyTypes)
    {
	if (DEBUG) {
	    messager.printMessage(Kind.NOTE, "ppa value is " + ppa.value());
	}
	TemplateProcessor.KeyMap[] eeKeymap = {
	    new TemplateProcessor.KeyMap()
	};

	TypeMirror setofType = typeArgumentForSet(types, eetm);
	eetmKeymap.put("parmName", ppa.value());
	eetmKeymap.put("varType", eetm.toString());
	eetmKeymap.put("erasedVarType",
			types.erasure(eetm).toString());
	TypeMirror baseType = getRVBaseType(types, eetm);
	String rvParmType =
	    getRVParmType(types, eetm, ppa.rvmode());

	TypeMirror rvType = getRvType(types, eetm, ppa.rvmode());
	TypeMirror type = getType(types, eetm, ppa.rvmode());
	boolean rvmode = ppa.rvmode() && (baseType != null);
	boolean cloneFlag = getCloneFlag(types, type);

	String rvClass = (rvType == null)? "null":
	    types.erasure(rvType).toString() + ".class";
	String typeString = (type == null)? "null":
	    types.erasure(type).toString();
	eetmKeymap.put("rvClass", rvClass);
	eetmKeymap.put("type", ((setofType != null)? "null":
				 typeString));
	TypeMirror parmType =
	    getParmType(types, eetm, ppa.rvmode());
	eetmKeymap.put("parmType",
			((parmType == null)? STRING: parmType.toString()));
	eetmKeymap.put("noSuppress", new TemplateProcessor.KeyMap());
	if (parmType == null) parmType = STRING_MIRROR;
	if (setofType != null) {
	    type = setofType;
	    eetmKeymap.put("setofType", setofType.toString());
	    String bptype = getBaseParmType(types, setofType);
	    String bptest = getBaseParmTest(types, setofType);
	    String bpconv = getBaseParmConv(types, setofType);
	    if (bptype != null && bptest != null && bpconv != null
		&& (diffKeyTypes || !bptype.equals(parmType.toString()))) {
		eetmKeymap.remove("noSuppress");
		TemplateProcessor.KeyMap setofKeymap =
		    new TemplateProcessor.KeyMap();
		setofKeymap.put("baseParmType", bptype);
		setofKeymap.put("baseParmTest", bptest);
		setofKeymap.put("baseParmConv", bpconv);
		eetmKeymap.put("parse", setofKeymap);
	    }
	} else {
	    String bptype = getBaseParmType(types, type);
	    String bptest = getBaseParmTest(types, type);
	    String bpconv = getBaseParmConv(types, type);
	    if (diffKeyTypes && rvmode) {
		bptype = (String)eetmKeymap.get("parmType");
	    }
	    if (bptype != null && bptest != null && bpconv != null
		&& (diffKeyTypes
		    ||
		    (!bptype.equals(parmType.toString()) && rvmode == false))) {
		TemplateProcessor.KeyMap setofKeymap =
		    new TemplateProcessor.KeyMap();
		setofKeymap.put("baseParmType", bptype);
		setofKeymap.put("baseParmTest", bptest);
		setofKeymap.put("baseParmConv", bpconv);
		eetmKeymap.put("parse", setofKeymap);
		eetmKeymap.remove("noSuppress");
	    }
	}
	if (types.isSameType(parmType, STRING_MIRROR)) {
	    if (implementsNamedObjectOps(types, type)) {
		eetmKeymap.remove("noSuppress");
		eetmKeymap.put("startCall",
				"factory.getObjectNamer().getObject(");
		eetmKeymap.put("endCall",
				", " + type.toString() + ".class)");
	    } else if (isEnum(types, type)) {
		eetmKeymap.remove("noSuppress");
		eetmKeymap.put("startCall", type.toString() + ".valueOf(");
		eetmKeymap.put("endCall", ")");
		eetmKeymap.put("enumStringParse",
				new TemplateProcessor.KeyMap());
	    }
	}

	if (DEBUG) {
	    messager.printMessage(Kind.NOTE,
				  "  parmName = " + ppa.value()
				  +", varType = " + eetm.toString());
	    messager.printMessage(Kind.NOTE, "    rvClass = " + rvClass);
	    messager.printMessage(Kind.NOTE, "    type = " +
				  ((setofType != null)? "null": typeString));
	    messager.printMessage(Kind.NOTE, "    parmType = "
				  +((parmType == null)? STRING:
				    parmType.toString()));
	    messager.printMessage(Kind.NOTE, "    cloneFlag = " + cloneFlag);
	}

	//TemplateProcessor.KeyMap alistKeyMap = new TemplateProcessor.KeyMap();

	String rvbClass = getRVBoundType(types, eetm);

	eetmKeymap.put("rvbClass", ((rvbClass == null)? "java.lang.Number":
				     rvbClass));
	if (rvbClass == null) {
	    String value = ppa.lowerBound();
	    if (value.equals("null")) {
		eetmKeymap.put("glbTerm", "null");
	    } else {
		String numberClass = getNumberClass(types, type);
		if (numberClass == null) {
		    eetmKeymap.put("glbTerm", "null");
		} else {
		    eetmKeymap.put("glbTerm", "(" + numberClass + ".valueOf(\""
				   + value + "\"))");
		}
	    }
	} else {
	    String value = ppa.lowerBound();
	    if (value.equals("null")) {
		eetmKeymap.put("glbTerm", "null");
	    } else {
		eetmKeymap.put("glbTerm", "(" + rvbClass + ".valueOf(\""
				+ value + "\"))");
		eetmKeymap.put("keyedTightenMinStatement",
				"field.tightenMinimumS(\""
				+ ppa.lowerBound() +"\", "
				+ (ppa.lowerBoundClosed()? "true": "false")
				+");");
	    }
	}
	eetmKeymap.put("glbClosed", (ppa.lowerBoundClosed()? "true": "false"));
	if (rvbClass == null) {
	    // eetmKeymap.put("lubTerm", "null");
	    String value = ppa.upperBound();
	    if (value.equals("null")) {
		eetmKeymap.put("lubTerm", "null");
	    } else {
		String numberClass = getNumberClass(types, type);
		if (numberClass == null) {
		    eetmKeymap.put("lubTerm", "null");
		} else {
		    eetmKeymap.put("lubTerm", "(" + numberClass + ".valueOf(\""
				   + value + "\"))");
		}
	    }
	} else {
	    String value = ppa.upperBound();
	    if (value.equals("null")) {
		eetmKeymap.put("lubTerm", "null");
	    } else {
		eetmKeymap.put("lubTerm", "(" + rvbClass + ".valueOf(\""
				+ ppa.upperBound() + "\"))");
		eetmKeymap.put("keyedTightenMaxStatement",
				"field.tightenMaximumS(\""
				+ ppa.upperBound() +"\", "
				+ (ppa.upperBoundClosed()? "true": "false")
				+");");
	    }
	}
	eetmKeymap.put("lubClosed", (ppa.upperBoundClosed()? "true": "false"));

	if (setofType != null) {
	    eetmKeymap.put("primCaseSet", eeKeymap);
	} else {
	  eetmKeymap.put((cloneFlag? "primCaseClone": "primCase"), eeKeymap);
	}
    }

    private void processPrimitive(Types types, Elements elements,
				  ArrayList<TemplateProcessor.KeyMap> alist,
				  PrimitiveParm ppa,
				  Element ee,
				  boolean diffKeyTypes)
    {
	if (DEBUG) {
	    messager.printMessage(Kind.NOTE, "ppa value is " + ppa.value());
	    messager.printMessage(Kind.NOTE,
				  "ee.getSimpleName().toString() is "
				  + ee.getSimpleName().toString());
	}
	TemplateProcessor.KeyMap[] eeKeymap = {
	    new TemplateProcessor.KeyMap()
	};

	TypeMirror setofType = typeArgumentForSet(types, ee.asType());
	eeKeymap[0].put("parmName", ppa.value());
	eeKeymap[0].put("varName",
			ee.getSimpleName().toString());
	eeKeymap[0].put("varType", ee.asType().toString());
	eeKeymap[0].put("erasedVarType",
			types.erasure(ee.asType()).toString());
	String eeName = ee.getSimpleName().toString();
	TypeMirror baseType = getRVBaseType(types, ee);
	String rvParmType =
	    getRVParmType(types, ee, ppa.rvmode());

	TypeMirror rvType = getRvType(types, ee, ppa.rvmode());
	TypeMirror type = getType(types, ee, ppa.rvmode());
	boolean rvmode = ppa.rvmode() && (baseType != null);

	boolean cloneFlag = getCloneFlag(types, type);

	String rvClass = (rvType == null)? "null":
	    types.erasure(rvType).toString() + ".class";
	String typeString = (type == null)? "null":
	    types.erasure(type).toString();
	eeKeymap[0].put("rvClass", rvClass);
	eeKeymap[0].put("type", ((setofType != null)? "null":
				 typeString));
	TypeMirror parmType =
	    getParmType(types, ee, ppa.rvmode());
	eeKeymap[0].put("parmType",
			((parmType == null)? STRING: parmType.toString()));
	eeKeymap[0].put("noSuppress", new TemplateProcessor.KeyMap());
	if (parmType == null) parmType = STRING_MIRROR;
	if (setofType != null) {
	    type = setofType;
	    eeKeymap[0].put("setofType", setofType.toString());
	    String bptype = getBaseParmType(types, setofType);
	    String bptest = getBaseParmTest(types, setofType);
	    String bpconv = getBaseParmConv(types, setofType);
	    if (bptype != null && bptest != null && bpconv != null
		&& (diffKeyTypes || !bptype.equals(parmType.toString()))) {
		eeKeymap[0].remove("noSuppress");
		TemplateProcessor.KeyMap setofKeymap =
		    new TemplateProcessor.KeyMap();
		setofKeymap.put("baseParmType", bptype);
		setofKeymap.put("baseParmTest", bptest);
		setofKeymap.put("baseParmConv", bpconv);
		eeKeymap[0].put("parse", setofKeymap);
	    }
	} else {
	    String bptype = getBaseParmType(types, type);
	    String bptest = getBaseParmTest(types, type);
	    String bpconv = getBaseParmConv(types, type);
	    if (diffKeyTypes && rvmode) {
		bptype = (String)eeKeymap[0].get("parmType");
	    }
	    if (bptype != null && bptest != null && bpconv != null
		&& (diffKeyTypes
		    ||
		    (!bptype.equals(parmType.toString()) && rvmode == false))) {
		TemplateProcessor.KeyMap setofKeymap =
		    new TemplateProcessor.KeyMap();
		setofKeymap.put("baseParmType", bptype);
		setofKeymap.put("baseParmTest", bptest);
		setofKeymap.put("baseParmConv", bpconv);
		eeKeymap[0].put("parse", setofKeymap);
		eeKeymap[0].remove("noSuppress");
	    }
	}
	if (types.isSameType(parmType, STRING_MIRROR)) {
	    if (implementsNamedObjectOps(types, type)) {
		eeKeymap[0].remove("noSuppress");
		eeKeymap[0].put("startCall",
				"factory.getObjectNamer().getObject(");
		eeKeymap[0].put("endCall",
				", " + type.toString() + ".class)");
	    } else if (isEnum(types, type)) {
		eeKeymap[0].remove("noSuppress");
		eeKeymap[0].put("startCall", type.toString() + ".valueOf(");
		eeKeymap[0].put("endCall", ")");
		eeKeymap[0].put("enumStringParse",
				new TemplateProcessor.KeyMap());
	    }
	}

	if (DEBUG) {
	    messager.printMessage(Kind.NOTE,
				  "  parmName = " + ppa.value()
				  +", varType = " + ee.asType().toString());
	    messager.printMessage(Kind.NOTE, "    varName ="
				  + ee.getSimpleName().toString());
	    messager.printMessage(Kind.NOTE, "    rvClass = " + rvClass);
	    messager.printMessage(Kind.NOTE, "    type = " +
				  ((setofType != null)? "null": typeString));
	    messager.printMessage(Kind.NOTE, "    parmType = "
				  +((parmType == null)? STRING:
				    parmType.toString()));
	    messager.printMessage(Kind.NOTE, "    cloneFlag = " + cloneFlag);
	}

	TemplateProcessor.KeyMap alistKeyMap = new TemplateProcessor.KeyMap();

	String rvbClass = getRVBoundType(types, ee);
	alistKeyMap.put("rvbClass", ((rvbClass == null)? "java.lang.Number":
				     rvbClass));
	if (rvbClass == null) {
	    // alistKeyMap.put("glbTerm", "null");
	    String value = ppa.lowerBound();
	    if (value.equals("null")) {
		alistKeyMap.put("glbTerm", "null");
	    } else {
		String numberClass = getNumberClass(types, type);
		if (numberClass == null) {
		    alistKeyMap.put("glbTerm", "null");
		} else {
		    alistKeyMap.put("glbTerm", "(" + numberClass + ".valueOf(\""
				    + value + "\"))");
		}
	    }
	} else {
	    String value = ppa.lowerBound();
	    if (value.equals("null")) {
		alistKeyMap.put("glbTerm", "null");
	    } else {
		alistKeyMap.put("glbTerm", "(" + rvbClass + ".valueOf(\""
				+ value + "\"))");
		alistKeyMap.put("tightenMinStatement",
				"this.defaults." + ee.getSimpleName().toString()
				+ ".tightenMinimumS(\""
				+ ppa.lowerBound() +"\", "
				+ (ppa.lowerBoundClosed()? "true": "false")
				+");");
		alistKeyMap.put("keyedTightenMinStatement",
				"field." + ee.getSimpleName().toString()
				+ ".tightenMinimumS(\""
				+ ppa.lowerBound() +"\", "
				+ (ppa.lowerBoundClosed()? "true": "false")
				+");");
	    }
	}
	alistKeyMap.put("glbClosed", (ppa.lowerBoundClosed()? "true": "false"));
	if (rvbClass == null) {
	    // alistKeyMap.put("lubTerm", "null");
	    String value = ppa.upperBound();
	    if (value.equals("null")) {
		alistKeyMap.put("lubTerm", "null");
	    } else {
		String numberClass = getNumberClass(types, type);
		if (numberClass == null) {
		    alistKeyMap.put("lubTerm", "null");
		} else {
		    alistKeyMap.put("lubTerm", "(" + numberClass + ".valueOf(\""
				    + value + "\"))");
		}
	    }
	} else {
	    String value = ppa.upperBound();
	    if (value.equals("null")) {
		alistKeyMap.put("lubTerm", "null");
	    } else {
		alistKeyMap.put("lubTerm", "(" + rvbClass + ".valueOf(\""
				+ ppa.upperBound() + "\"))");
		alistKeyMap.put("tightenMaxStatement",
				"this.defaults." + ee.getSimpleName().toString()
				+ ".tightenMaximumS(\""
				+ ppa.upperBound() +"\", "
				+ (ppa.upperBoundClosed()? "true": "false")
				+");");
		alistKeyMap.put("keyedTightenMaxStatement",
				"field." + ee.getSimpleName().toString()
				+ ".tightenMaximumS(\""
				+ ppa.upperBound() +"\", "
				+ (ppa.upperBoundClosed()? "true": "false")
				+");");
	    }
	}
	alistKeyMap.put("lubClosed", (ppa.upperBoundClosed()? "true": "false"));

	if (setofType != null) {
	    alistKeyMap.put("primCaseSet", eeKeymap);
	} else {
	    alistKeyMap.put((cloneFlag? "primCaseClone": "primCase"), eeKeymap);
	}
	alist.add(alistKeyMap);
    }

    private boolean checkTipsAndLabels(String currentPackage, Element e,
				       String tipBaseName, String labelBaseName,
				       String docBaseName)
    {
	boolean result = true;
	if (tipBaseName != null && tipBaseName.contains(".")) {
	    // error checking
	    if (tipBaseName.startsWith("*.")) {
		String substring = tipBaseName.substring(2);
		if (!JavaIdents.isValidIdentifier(substring, true)) {
		    messager.printMessage (Kind.ERROR,
					   "\"" + substring + "\" is not"
					   + " a Java identifier", e);
		    result = false;
		}
		if (substring.contains(".") &&
		    !substring.startsWith("lpack.")) {
		    messager.printMessage (Kind.WARNING,
					   "tipResourceBundle did not start"
					   + " with \"*.lpack.\"", e);
		}
	    } else {
		if (currentPackage == null) {
		    messager.printMessage (Kind.WARNING,
					   "tipResourceBundle must be a"
					   + " simple name when annotating "
					   + " a class in an unnamed package",
					   e);
		} else {
		    int len = currentPackage.length() + 1;
		    if (tipBaseName.length() <= len ||
			!tipBaseName.startsWith(currentPackage + ".")) {
			messager.printMessage(Kind.WARNING,
					      "tipResourceBundle did not"
					      + " start with the package name"
					      + " of the element it annotates",
					      e);
		    } else {
			if (!JavaIdents.isValidIdentifier(tipBaseName, true)) {
			    messager.printMessage (Kind.ERROR,
						   "\"" + tipBaseName
						   + "\" is not"
						   + " a Java identifier", e);
			    result = false;
			}
			String substring = tipBaseName.substring(len);
			if (substring.contains(".") &&
			    !substring.startsWith(".lpack.")) {
			    messager.printMessage(Kind.WARNING,
						  "tipResourceBundle did not"
						  + " use lpack as the first"
						  + " subpackage of the"
						  + " package of the class that"
						  + " it annotates.", e);
			}
		    }
		}
	    }
	}
	if (labelBaseName != null && labelBaseName.contains(".")) {
	    // error checking
	    if (labelBaseName.startsWith("*.")) {
		String substring = labelBaseName.substring(2);
		if (!JavaIdents.isValidIdentifier(substring, true)) {
		    messager.printMessage (Kind.ERROR,
					   "\"" + substring + "\" is not"
					   + " a Java identifier", e);
		    result = false;
		}
		if (substring.contains(".") &&
		    !substring.startsWith("lpack.")) {
		    messager.printMessage(Kind.WARNING,
					  "labelResourceBundle did not start"
					  + " with \"*.lpack.\"", e);
		}
	    } else {
		if (currentPackage == null) {
		    messager.printMessage(Kind.WARNING,
					  "labelResourceBundle must be a"
					  + " simple name when annotating"
					  + " a class in an unnamed package",
					  e);
		} else {
		    int len = currentPackage.length() + 1;
		    if (labelBaseName.length() <= len ||
			!labelBaseName
			.startsWith(currentPackage + ".")) {
			messager.printMessage (Kind.ERROR,
					       "labelResourceBundle did not"
					       + " start with the package name"
					       + " of the element it "
					       + " annotates", e);
			result = false;
		    } else {
			if (!JavaIdents.isValidIdentifier(labelBaseName, true)){
			    messager.printMessage (Kind.ERROR,
						   "\"" + labelBaseName
						   + "\" is not"
						   + " a Java identifier", e);
			    result = false;
			}
			String substring = labelBaseName.substring(len);
			if (substring.contains(".") &&
			    !substring.startsWith(".lpack.")) {
			    messager.printMessage(Kind.WARNING,
						  "labelResourceBundle did not"
						  + " use lpack as the first"
						  + " subpackage of the"
						  + " package of the class that"
						  + " it annotates.", e);
			}
		    }
		}
	    }
	}
	if (docBaseName != null && docBaseName.contains(".")) {
	    // error checking
	    if (docBaseName.startsWith("*.")) {
		String substring = docBaseName.substring(2);
		if (!JavaIdents.isValidIdentifier(substring, true)) {
		    messager.printMessage (Kind.ERROR,
					   "\"" + substring + "\" is not"
					   + " a Java identifier", e);
		    result = false;
		}
		if (substring.contains(".") &&
		    !substring.startsWith("lpack.")) {
		    messager.printMessage (Kind.WARNING,
					   "docResourceBundle did not start"
					   + " with \"*.lpack.\"", e);
		}
	    } else {
		if (currentPackage == null) {
		    messager.printMessage (Kind.ERROR,
					   "docResourceBundle must be a"
					   + " simple name when annotating "
					   + " a class in an unnamed package",
					   e);
		    result = false;
		} else {
		    int len = currentPackage.length() + 1;
		    if (docBaseName.length() <= len ||
			!docBaseName.startsWith(currentPackage + ".")) {
			messager.printMessage(Kind.ERROR,
					      "docResourceBundle did not"
					      + " start with the package name"
					      + " of the element it annotates",
					      e);
			result = false;
		    } else {
			if (!JavaIdents.isValidIdentifier(docBaseName, true)) {
			    messager.printMessage (Kind.ERROR,
						   "\"" + docBaseName
						   + "\" is not"
						   + " a Java identifier", e);
			    result = false;
			}
			String substring = docBaseName.substring(len);
			if (substring.contains(".") &&
			    !substring.startsWith(".lpack.")) {
			    messager.printMessage(Kind.WARNING,
						  "docResourceBundle did not"
						  + " use lpack as the first"
						  + " subpackage of the"
						  + " package of the class that"
						  + " it annotates.", e);
			}
		    }
		}
	    }
	}
	return result;
    }

    private static String printDateTime(Calendar cal) {
	return String.format("%tF %tT-00:00", cal, cal);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
			   RoundEnvironment env)
    {
	Messager messager = processingEnv.getMessager();
	Filer filer = processingEnv.getFiler();
	Types types = processingEnv.getTypeUtils();
	Elements elements = processingEnv.getElementUtils();
	this.messager = messager;

	INTEGER_RV_MIRROR =
	    elements.getTypeElement(RVPKG + ".IntegerRandomVariable").asType();
	LONG_RV_MIRROR =
	    elements.getTypeElement(RVPKG + ".LongRandomVariable").asType();
	DOUBLE_RV_MIRROR =
	    elements.getTypeElement(RVPKG + ".DoubleRandomVariable").asType();
	BOOLEAN_RV_MIRROR =
	    elements.getTypeElement(RVPKG + ".BooleanRandomVariable").asType();
	INTEGER_MIRROR =
	    elements.getTypeElement("java.lang.Integer").asType();
	LONG_MIRROR =
	    elements.getTypeElement("java.lang.Long").asType();
	DOUBLE_MIRROR =
	    elements.getTypeElement("java.lang.Double").asType();
	BOOLEAN_MIRROR =
	    elements.getTypeElement("java.lang.Boolean").asType();
	STRING_MIRROR =
	    elements.getTypeElement("java.lang.String").asType();
	CLONEABLE_MIRROR =
	    elements.getTypeElement("java.lang.Cloneable").asType();
	NAMEDOBJECTOPS_MIRROR =
	    elements.getTypeElement(PACKAGE + ".NamedObjectOps").asType();
	ERASED_SET_MIRROR =
	    types.erasure(elements.getTypeElement("java.util.Set").asType());

	ERASED_MAP_MIRROR =
	    types.erasure(elements.getTypeElement("java.util.Map").asType());

	ENUM_MIRROR =
	    types.erasure(elements.getTypeElement("java.lang.Enum").asType());

	ENUM_SET_MIRROR =
	    types.erasure(elements.getTypeElement("java.util.EnumSet")
			  .asType());

	boolean generate = true;
	for (Element e: env.getElementsAnnotatedWith(CompoundParmType.class)){
	    // This is a trivial case.  We don't generate anything but
	    // rather simply do some checking of the annotation.
	    try {
		if (e == null) {
		    messager.printMessage(Kind.ERROR,
					  "no element for FactoryParmManager "
					  + "annotation");
		    continue;
		}
		if (e.getKind() != ElementKind.CLASS) {
		    messager.printMessage(Kind.ERROR,
					  "element annotated by "
					  + "FactoryParmManager is not a class",
					  e);
		    continue;
		}
		CompoundParmType cpt = e.getAnnotation(CompoundParmType.class);
		if (cpt == null) {
		    if (DEBUG)
			messager.printMessage(Kind.WARNING,
					      "cpt == null for "
					      + "CompoundParmType",
					      e);
		    continue;
		}
		String currentPackage = getPackage(e);
		String tipBaseName = cpt.tipResourceBundle();
		String labelBaseName = cpt.labelResourceBundle();
		String docBaseName = cpt.docResourceBundle();
		if (tipBaseName != null) tipBaseName = tipBaseName.trim();
		if (labelBaseName != null) labelBaseName = labelBaseName.trim();
		if (docBaseName != null) docBaseName = docBaseName.trim();
		if (!checkTipsAndLabels(currentPackage, e,
					tipBaseName, labelBaseName,
					docBaseName)) {
		    generate = false;
		}
	    } catch (Exception exception) {
		exception.printStackTrace();
		messager.printMessage(Kind.ERROR,
				      "Annotation Processor Exception: "
				      + exception.getMessage(),
				      e);
	    }
	}
	for (Element e: env.getElementsAnnotatedWith(FactoryParmManager.class)){
	    try {
		if (e == null) {
		    messager.printMessage(Kind.ERROR,
					  "no element for FactoryParmManager "
					  + "annotation");
		    continue;
		}
		if (e.getKind() != ElementKind.CLASS) {
		    messager.printMessage(Kind.ERROR,
					  "element annotated by "
					  + "FactoryParmManager is not a class",
					  e);
		    continue;
		}
		String factory = e.getSimpleName().toString();

		FactoryParmManager fpm =
		    e.getAnnotation(FactoryParmManager.class);
		if (fpm == null) {
		    if (DEBUG)
			messager.printMessage(Kind.WARNING,
					      "fpm == null for "
					      + "FactoryParmManager",
					      e);
		    continue;
		}
		generate = true;
		String currentPackage = getPackage(e);
		if (currentPackage != null) {
		    currentPackage = currentPackage.trim();
		}
		String manager = fpm.value();
		if (manager  != null) {
		    manager = manager.trim();
		    if (manager.length() == 0) manager = null;
		}
		String stdFactory = fpm.stdFactory();
		if (stdFactory != null) {
		    stdFactory = stdFactory.trim();
		}
		String packagePath = getPackagePath(currentPackage);
		String namerVar = null;
		String namedObjectClass = null;
		String typeParmClasses = null;
		String namerDoc = null;
		String style = null;
		boolean showParmDoc = false;
		String namerClass = null;

		if (stdFactory != null && stdFactory.length() != 0) {
		    if (!JavaIdents.isValidIdentifier(stdFactory, false)) {
			generate = false;
			messager.printMessage(Kind.ERROR,
					      "\"" + stdFactory
					      + "\" is not a Java "
					      + "identifier", e);
		    }
		    namerVar = fpm.namerVariable();
		    if (namerVar != null) namerVar = namerVar.trim();
		    if (!JavaIdents.isValidIdentifier(namerVar, false)) {
			generate = false;
			messager.printMessage(Kind.ERROR,
					      "\"" + namerVar
					      + "\" is not a Java "
					      + "identifier", e);
		    }
		    namedObjectClass = getNamedObjectClass(types, e);
		    typeParmClasses = getTypeParmClasses(types, e);
		    namerDoc = fpm.namerDocumentation();
		    if (namerDoc != null) namerDoc = namerDoc.trim();
		    style = fpm.iframeStyle();
		    if (style != null) style = style.trim();
		    showParmDoc = fpm.showParameterDocumentation();
		    namerClass = getObjectNamer(elements, e);
		    /*
		    messager.printMessage(Kind.NOTE,
					  "stdFactory = " + stdFactory);
		    messager.printMessage(Kind.NOTE,
					  "namedObjectClass = "
					  + namedObjectClass);
		    messager.printMessage(Kind.NOTE,
					  "typeParmClasses = "
					  + typeParmClasses);

		    messager.printMessage(Kind.NOTE,
					  "namerVar = " + namerVar);
		    messager.printMessage(Kind.NOTE,
					  "namerDoc = " + namerDoc);
		    messager.printMessage(Kind.NOTE,
					  "style = " + style);
		    messager.printMessage(Kind.NOTE,
					  "showParmDoc = " + showParmDoc);
		    messager.printMessage(Kind.NOTE,
					  "namerClass = " + namerClass);
		    messager.printMessage(Kind.NOTE,
					  "packagePath = " + packagePath);
		    */
		    if (namerVar == null || namerVar.length() == 0) {
			messager.printMessage(Kind.ERROR,
					     "Illegal namerVariable "
					     + "annotation element");
		    }
		}

		if (manager != null &&
		    !JavaIdents.isValidIdentifier(manager, false)) {
		    generate = false;
		    messager.printMessage(Kind.ERROR,
					  "\"" + manager + "\" is not a Java "
					  + "identifier", e);
		}

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
		if (manager != null) {
		    keymap.put("manager", manager);
		}
		keymap.put("factory", e.getSimpleName().toString());
		if (DEBUG) {
		    messager.printMessage(Kind.NOTE,
					  "factory = " + factory
					  + ", ParmManagerClass = " + manager);
		    messager.printMessage(Kind.NOTE,
					  "  package = " +currentPackage);
		}
		if (stdFactory != null && stdFactory.length() > 0
		    && typeParmClasses != null) {
		    keymap.put("stdFactory", stdFactory);
		    keymap.put("typeParmClasses", typeParmClasses);
		    keymap.put("namerVar", namerVar);
		    keymap.put("namerDoc", namerDoc);
		    keymap.put("style", style);
		    if (showParmDoc) {
			keymap.put("showParmDoc",
				   new TemplateProcessor.KeyMap());
		    }
		    keymap.put("namedObjectClass", namedObjectClass);
		    keymap.put("namerClass", namerClass);
		    keymap.put("packagePath", packagePath);
		    if (packagePath != null && packagePath.length() > 0) {
			keymap.put("packagePathSep", "/");
		    }
		}
		String tipBaseName = fpm.tipResourceBundle();
		String labelBaseName = fpm.labelResourceBundle();
		String docBaseName = fpm.docResourceBundle();
		if (tipBaseName != null) tipBaseName = tipBaseName.trim();
		if (labelBaseName != null) labelBaseName = labelBaseName.trim();
		if (docBaseName != null) docBaseName = docBaseName.trim();
		if (!checkTipsAndLabels(currentPackage, e,
					tipBaseName, labelBaseName,
					docBaseName)) {
		    generate = false;
		}
		if (tipBaseName != null && !tipBaseName.equals("")) {
		    TemplateProcessor.KeyMap tipKeymap = new
		    TemplateProcessor.KeyMap();
		    if (manager == null) {
			messager.printMessage(Kind.WARNING,
					      "tipResourceBundle without a "
					      + "ParmManager", e);
		    }
		    tipKeymap.put("tipBaseName", tipBaseName);
		    keymap.put("hasTip", tipKeymap);
		}
		if (labelBaseName != null && !labelBaseName.equals("")) {
		    TemplateProcessor.KeyMap labelKeymap = new
		    TemplateProcessor.KeyMap();
		    if (manager == null) {
			messager.printMessage(Kind.WARNING,
					      "labelResourceBundle without a "
					      + "ParmManager", e);
		    }
		    labelKeymap.put("labelBaseName", labelBaseName);
		    keymap.put("hasLabel", labelKeymap);
		}
		if (docBaseName != null && !docBaseName.equals("")) {
		    TemplateProcessor.KeyMap docKeymap = new
		    TemplateProcessor.KeyMap();
		    if (manager == null) {
			messager.printMessage(Kind.WARNING,
					      "docResourceBundle "
					      + "without a ParmManager", e);
		    }
		    docKeymap.put("docBaseName", docBaseName);
		    keymap.put("hasDoc", docKeymap);
		}

		String formalParms = getFormalParms(e, true);
		String formalParmArgs = getFormalParms(e, false);
		if (DEBUG && formalParms != null) {
		    messager.printMessage(Kind.NOTE,
					  "  formalParms = " + formalParms);
		}
		if (formalParms != null) {
		    keymap.put("formalParms", formalParms);
		    keymap.put("formalParmArgs", formalParmArgs);
		}

		ArrayList<TemplateProcessor.KeyMap> alist =
		    new ArrayList<TemplateProcessor.KeyMap>();

		for (Element ee: e.getEnclosedElements()) {
		    if (ee == null) {
			messager.printMessage(Kind.ERROR, "missing element", e);
			continue;
		    }
		    PrimitiveParm ppa = ee.getAnnotation(PrimitiveParm.class);
		    // element contains a primitive parm.
		    if (ppa != null) {
			processPrimitive(types, elements, alist, ppa, ee);
		    }
		    KeyedCompoundParm kpa =
			ee.getAnnotation(KeyedCompoundParm.class);
		    if (kpa != null) {
			String keyPrefix = kpa.value();
			String keyDelimiter = kpa.delimiter();
			TypeMirror mapType = ee.asType();
			TypeMirror mapKeyType = keyTypeForMap(types, mapType);
			TypeMirror rawMapKeyType =
			    rawKeyTypeForMap(types, mapType);
			TypeMirror mapValueType =
			    valueTypeForMap(types, mapType);
			ArrayList<TemplateProcessor.KeyMap> aalist =
			    new ArrayList<TemplateProcessor.KeyMap>();
			if (mapValueType != null
			    && mapValueType.getKind() == TypeKind.DECLARED) {
			    DeclaredType dt = (DeclaredType) mapValueType;
			    TemplateProcessor.KeyMap eeKeymap[] = {
				new TemplateProcessor.KeyMap()
			    };
			    eeKeymap[0].put("mapVar",
					    ee.getSimpleName().toString());
			    eeKeymap[0].put("keyPrefix", keyPrefix);
			    eeKeymap[0].put("keyDelimiter", keyDelimiter);
			    CompoundParmType cpa = dt.asElement()
				.getAnnotation(CompoundParmType.class);
			    if (cpa != null) {
				tipBaseName = cpa.tipResourceBundle();
				labelBaseName = cpa.labelResourceBundle();
				docBaseName = cpa.docResourceBundle();
				if (tipBaseName != null)
				    tipBaseName = tipBaseName.trim();
				if (labelBaseName != null)
				    labelBaseName = labelBaseName.trim();
				if (docBaseName != null)
				    docBaseName = docBaseName.trim();
				if (tipBaseName != null
				    && !tipBaseName.equals("")) {
				    TemplateProcessor.KeyMap tipKeymap = new
					TemplateProcessor.KeyMap();
				    tipKeymap.put("tipBaseName", tipBaseName);
				    eeKeymap[0].put("hasTipCPT", tipKeymap);
				}
				if (labelBaseName != null &&
				    !labelBaseName.equals("")) {
				    TemplateProcessor.KeyMap labelKeymap = new
					TemplateProcessor.KeyMap();
				    labelKeymap.put("labelBaseName",
						    labelBaseName);
				    eeKeymap[0].put("hasLabelCPT", labelKeymap);
				}
				if (docBaseName != null
				    && !docBaseName.equals("")) {
				    TemplateProcessor.KeyMap docKeymap = new
					TemplateProcessor.KeyMap();
				    docKeymap.put("docBaseName", docBaseName);
				    eeKeymap[0].put("hasDocCPT", docKeymap);
				}
			    }
			    eeKeymap[0].put("mapKeyType",
					    mapKeyType.toString());
			    eeKeymap[0].put("rawMapKeyType",
					    rawMapKeyType.toString());
			    if (mapKeyType.getKind() == TypeKind.DECLARED) {
				eeKeymap[0].put("erasedMapKeyType",
						types.erasure(mapKeyType)
						.toString());
			    } else {
				eeKeymap[0].put("erasedMapKeyType",
						mapKeyType.toString());
			    }
			    TypeMirror parmType = getParmType(types,
							      mapKeyType);
			    String baseParmType =
				getBaseKeyParmType(types, mapKeyType);
			    eeKeymap[0].put("mapKeyBaseParmType",
					    (baseParmType == null)? STRING:
					    baseParmType);
			    if (baseParmType != null) {
				eeKeymap[0].put("keyedParse",
						new TemplateProcessor.KeyMap());
			    }
			    if (!parmType.toString().equals("int")) {
				eeKeymap[0].put("keyedNoSuppress",
						new TemplateProcessor.KeyMap());
				eeKeymap[0].put("mapKeyTest",
						"key instanceof "
						+ (String)
						eeKeymap[0]
						.get("erasedMapKeyType"));
				eeKeymap[0].put("qvKeyTest",
						"qvalue[0] instanceof "
						+ (String)
						eeKeymap[0]
						.get("erasedMapKeyType"));
			    } else {
				eeKeymap[0].put("mapKeyTest", "true");
				eeKeymap[0].put("qvKeyTest",
						"qvalue[0] instanceof "
						+ types.erasure(rawMapKeyType)
						.toString());
			    }
			    eeKeymap[0].put("mapKeyParmType",
					    (parmType == null)? STRING:
					    parmType.toString());
			    boolean keytest =
				!(eeKeymap[0].get("mapKeyBaseParmType")
				 .equals(eeKeymap[0].get("mapKeyParmType")));

			    if (types.isSameType(mapKeyType, STRING_MIRROR)) {
				if (types.isSameType(rawMapKeyType,
						     INTEGER_MIRROR)) {
				    eeKeymap[0].put("mapKey",
						    "Integer.valueOf(key)");
				} else {
				    eeKeymap[0].put("mapKey", "key");
				}
			    } else if (implementsNamedObjectOps(types,
								mapKeyType)) {
				String expr = "factory.getObjectNamer()"
				    + ".getObject(key, "
				    + mapKeyType.toString() + ".class)";
				eeKeymap[0].put("mapKey", expr);
			    } else if (isEnum(types, mapKeyType)) {
				String expr = mapKeyType.toString() +
				    ".valueOf(key)";
				eeKeymap[0].put("mapKey", expr);
			    } else {
				eeKeymap[0].put("mapKey", "key");
			    }

			    eeKeymap[0].put("mapValueType",
					    mapValueType.toString());
			    TypeMirror emvtm =
				types.erasure(mapValueType);
			    if (types.isSameType(emvtm, ENUM_SET_MIRROR)) {
				eeKeymap[0].put("isEnumSet",
						new TemplateProcessor.KeyMap());
			    } else {
				eeKeymap[0].put("isNotEnumSet",
						new TemplateProcessor.KeyMap());
			    }
			    eeKeymap[0].put("erasedMapValueType",
					    emvtm.toString());
			    for (Element eee: dt.asElement()
				     .getEnclosedElements()) {
				if (eee == null) continue;
				PrimitiveParm ppaa =
				    eee.getAnnotation(PrimitiveParm.class);
				if (ppaa != null) {
				    processPrimitive(types, elements, aalist,
						     ppaa, eee, keytest);
				    // need to set up the tables differently
				    // when we are processing a set.
				    TypeMirror eeeType = eee.asType();
				    if (isSet(types, eeeType)) {
					TypeMirror setofType =
					    typeArgumentForSet(types, eeeType);
					aalist.get(aalist.size()-1)
					    .put("erasedMapValueVarType",
						 types.erasure(setofType)
						 .toString());
					if (types.isSameType(mapKeyType,
							     STRING_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("mapKey", "parms[0]");
					} else if (implementsNamedObjectOps
						   (types, mapKeyType)) {
					    String expr =
						"factory.getObjectNamer()"
						+ ".getObject(parms[0], "
						+ mapKeyType.toString()
						+ ".class)";
					    aalist.get(aalist.size()-1)
						.put("mapKey", expr);
					} else if (isEnum(types, mapKeyType)) {
					    String expr =
						mapKeyType.toString() +
						".valueOf(parms[0])";
					    aalist.get(aalist.size()-1)
						.put("mapKey", expr);
					} else if (parmType.toString()
						   .equals("int")) {
					    eeKeymap[0].put("mapKey", "key");
					    eeKeymap[0].put("qmapKey",
							    "Integer"
							    + ".valueOf"
							    + "(parms[0])");
					} else {
					    eeKeymap[0].put("mapKey",
							    "parms[0]");
					}
					if (types.isSameType(mapKeyType,
							     INTEGER_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("mapKey",
						     "Integer.valueOf(key)");
					    aalist.get(aalist.size()-1)
						.put("qvalueKeyParm",
						     "Integer.valueOf"
						     + "(parms[0])");
					}
					if (types.isSameType(setofType,
							     INTEGER_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("qvalueValueParm",
						     "Integer.valueOf("
						     +"parms[1])");
					} else if (types.isSameType
						   (setofType,LONG_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("qvalueValueParm",
						     "Long.valueOf("
						     +"parms[1])");
					} else if (types.isSameType
						   (setofType,DOUBLE_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("qvalueValueParm",
						     "Double.valueOf("
						     +"parms[1])");
					} else if (types.isSameType
						   (setofType,BOOLEAN_MIRROR)) {
					    aalist.get(aalist.size()-1)
						.put("qvalueValueParm",
						     "Boolean.valueOf("
						     +"parms[1])");
					} else {
					    aalist.get(aalist.size()-1)
						.put("qvalueValueParm",
						     "parms[1]");
					}
				    } else {
					// not a set.
					if (implementsNamedObjectOps
					    (types, mapKeyType)) {
					    aalist.get(aalist.size()-1)
						.put("mapKeyParmType",
						     "org.bzdev.obnaming"
						     + ".NamedObjectOps");
					    String expr = "("
						+ mapKeyType.toString() 
						+ ")key";
					    aalist.get(aalist.size()-1)
						.put("mapKey", expr);
					}
				    }
				    if (aalist.get(aalist.size()-1)
					.get("qmapKey") == null
					&& aalist.get(aalist.size()-1)
						 .get("mapKey") != null) {
					aalist.get(aalist.size()-1)
					    .put("qmapKey",
						 aalist.get(aalist.size()-1)
						 .get("mapKey"));
				    }
				}
			    }
			    eeKeymap[0].put
				("nestedParmLoop",
				 aalist.toArray
				 (new TemplateProcessor.KeyMap[aalist.size()]));
			    TemplateProcessor.KeyMap alistKeyMap =
				new TemplateProcessor.KeyMap();
			    alistKeyMap.put("keyedCase", eeKeymap);
			    alist.add(alistKeyMap);
			}
		    }
		    CompoundParm cppa = ee.getAnnotation(CompoundParm.class);
		    if(cppa != null) {
			String keyPrefix = cppa.value();
			String keyDelimiter = cppa.delimiter();
			TypeMirror valueType = ee.asType();
			ArrayList<TemplateProcessor.KeyMap> aalist =
			    new ArrayList<TemplateProcessor.KeyMap>();
			if (valueType != null &&
			    valueType.getKind() == TypeKind.DECLARED) {
			    DeclaredType dt = (DeclaredType) valueType;
			    TemplateProcessor.KeyMap eeKeymap[] = {
				new TemplateProcessor.KeyMap()
			    };
			    eeKeymap[0].put("valueVar",
					    ee.getSimpleName().toString());
			    eeKeymap[0].put("keyPrefix", keyPrefix);
			    eeKeymap[0].put("keyDelimiter", keyDelimiter);
			    CompoundParmType cpa = dt.asElement()
				.getAnnotation(CompoundParmType.class);
			    if (cpa != null) {
				tipBaseName = cpa.tipResourceBundle();
				labelBaseName = cpa.labelResourceBundle();
				docBaseName = cpa.docResourceBundle();
				if (tipBaseName != null)
				    tipBaseName = tipBaseName.trim();
				if (labelBaseName != null)
				    labelBaseName = labelBaseName.trim();
				if (docBaseName != null)
				    docBaseName = docBaseName.trim();
				if (tipBaseName != null
				    && !tipBaseName.equals("")) {
				    TemplateProcessor.KeyMap tipKeymap = new
					TemplateProcessor.KeyMap();
				    tipKeymap.put("tipBaseName", tipBaseName);
				    eeKeymap[0].put("hasTipCPT", tipKeymap);
				}
				if (labelBaseName != null &&
				    !labelBaseName.equals("")) {
				    TemplateProcessor.KeyMap labelKeymap = new
					TemplateProcessor.KeyMap();
				    labelKeymap.put("labelBaseName",
						    labelBaseName);
				    eeKeymap[0].put("hasLabelCPT", labelKeymap);
				}
				if (docBaseName != null
				    && !docBaseName.equals("")) {
				    TemplateProcessor.KeyMap docKeymap = new
					TemplateProcessor.KeyMap();
				    docKeymap.put("docBaseName", docBaseName);
				    eeKeymap[0].put("hasDocCPT", docKeymap);
				}
			    }
			    eeKeymap[0].put("valueType",
					    valueType.toString());
			    for (Element eee: dt.asElement()
				     .getEnclosedElements()) {
				if (eee == null) continue;
				PrimitiveParm ppaa =
				    eee.getAnnotation(PrimitiveParm.class);
				if (ppaa != null) {
				    processPrimitive(types, elements, aalist,
						     ppaa, eee);
				}
			    }
			    eeKeymap[0].put
				("nestedParmLoop",
				 aalist.toArray
				 (new TemplateProcessor.KeyMap[aalist.size()]));
			    TemplateProcessor.KeyMap alistKeyMap =
				new TemplateProcessor.KeyMap();
			    alistKeyMap.put("compoundCase", eeKeymap);
			    alist.add(alistKeyMap);
			}
		    }
		    KeyedPrimitiveParm kppa =
			ee.getAnnotation(KeyedPrimitiveParm.class);
		    if (kppa != null) {
			/*
			messager.printMessage(Kind.NOTE,
					      "starting a keyedPrimitiveParm");
			*/
			String keyPrefix = kppa.value();
			TypeMirror mapType = ee.asType();
			TypeMirror mapKeyType = keyTypeForMap(types, mapType);
			TypeMirror rawMapKeyType =
			    rawKeyTypeForMap(types, mapType);
			TypeMirror mapValueType =
			    valueTypeForMap(types, mapType);
			ArrayList<TemplateProcessor.KeyMap> aalist =
			    new ArrayList<TemplateProcessor.KeyMap>();
			if (mapValueType != null
			    && mapValueType.getKind() == TypeKind.DECLARED) {
			    DeclaredType dt = (DeclaredType) mapValueType;
			    TemplateProcessor.KeyMap eeKeymap[] = {
				new TemplateProcessor.KeyMap()
			    };
			    eeKeymap[0].put("keyPrefix", keyPrefix);
			    eeKeymap[0].put("mapVar",
					    ee.getSimpleName().toString());
			    eeKeymap[0].put("mapKeyType",
					    mapKeyType.toString());
			    eeKeymap[0].put("rawMapKeyType",
					    rawMapKeyType.toString());
			    if (mapKeyType.getKind() == TypeKind.DECLARED) {
				eeKeymap[0].put("erasedMapKeyType",
						types.erasure(mapKeyType)
						.toString());
			    } else {
				eeKeymap[0].put("erasedMapKeyType",
						mapKeyType.toString());
			    }
			    TypeMirror parmType = getParmType(types,
							      mapKeyType);
			    String baseParmType =
				getBaseKeyParmType(types, mapKeyType);
			    eeKeymap[0].put("mapKeyBaseParmType",
					    (baseParmType == null)? STRING:
					    baseParmType);
			    if (baseParmType != null) {
				eeKeymap[0].put("keyedParse",
						new TemplateProcessor.KeyMap());
			    }
			    if (!parmType.toString().equals("int")) {
				eeKeymap[0].put("keyedNoSuppress",
						new TemplateProcessor.KeyMap());
				eeKeymap[0].put("mapKeyTest",
						"key instanceof "
						+ (String)
						eeKeymap[0]
						.get("erasedMapKeyType"));
				eeKeymap[0].put("qvKeyTest",
						"qvalue[0] instanceof "
						+ (String)
						eeKeymap[0]
						.get("erasedMapKeyType"));
			    } else {
				eeKeymap[0].put("mapKeyTest", "true");
				eeKeymap[0].put("qvKeyTest",
						"qvalue[0] instanceof "
						+ types.erasure(rawMapKeyType)
						.toString());
			    }
			    eeKeymap[0].put("mapKeyParmType",
					    (parmType == null)? STRING:
					    parmType.toString());
			    boolean keytest =
				!(eeKeymap[0].get("mapKeyBaseParmType")
				 .equals(eeKeymap[0].get("mapKeyParmType")));

			    if (types.isSameType(mapKeyType, STRING_MIRROR)) {
				if (types.isSameType(rawMapKeyType,
						     INTEGER_MIRROR)) {
				    eeKeymap[0].put("mapKey",
						    "Integer.valueOf(key)");
				} else {
				    eeKeymap[0].put("mapKey", "key");
				}
			    } else if (implementsNamedObjectOps(types,
								mapKeyType)) {
				String expr = "factory.getObjectNamer()"
				    + ".getObject(key, "
				    + mapKeyType.toString() + ".class)";
				eeKeymap[0].put("mapKey", expr);
			    } else if (isEnum(types, mapKeyType)) {
				String expr = mapKeyType.toString() +
				    ".valueOf(key)";
				eeKeymap[0].put("mapKey", expr);
			    } else {
				eeKeymap[0].put("mapKey", "key");
			    }

			    eeKeymap[0].put("mapValueType",
					    mapValueType.toString());
			    TypeMirror emvtm =
				types.erasure(mapValueType);
			    if (types.isSameType(emvtm, ENUM_SET_MIRROR)) {
				eeKeymap[0].put("isEnumSet",
						new TemplateProcessor.KeyMap());
			    } else {
				eeKeymap[0].put("isNotEnumSet",
						new TemplateProcessor.KeyMap());
			    }
			    eeKeymap[0].put("erasedMapValueType",
					    emvtm.toString());
			    processPrimitive(types, elements, eeKeymap[0],
					     kppa, mapValueType, keytest);
			    if (isSet(types, mapValueType)) {
				TypeMirror setofType =
				    typeArgumentForSet(types, mapValueType);
				eeKeymap[0].put("erasedMapValueVarType",
					     types.erasure(setofType)
					     .toString());
				if (types.isSameType(mapKeyType,
						     STRING_MIRROR)) {
				    eeKeymap[0].put("mapKey", "parms[0]");
				} else if (implementsNamedObjectOps
					   (types, mapKeyType)) {
				    String expr =
					"factory.getObjectNamer()"
					+ ".getObject(parms[0], "
					+ mapKeyType.toString()
					+ ".class)";
				    eeKeymap[0].put("mapKey", expr);
				} else if (isEnum(types, mapKeyType)) {
				    String expr =
					mapKeyType.toString() +
					".valueOf(parms[0])";
				    eeKeymap[0].put("mapKey", expr);
				} else if (parmType.toString()
					   .equals("int")) {
				    eeKeymap[0].put("mapKey", "key");
				    eeKeymap[0].put("qmapKey",
						    "Integer"
						    + ".valueOf"
						    + "(parms[0])");
				} else {
				    eeKeymap[0].put("mapKey",
						    "parms[0]");
				}
				if (types.isSameType(mapKeyType,
						     INTEGER_MIRROR)) {
				    eeKeymap[0].put("mapKey",
						 "Integer.valueOf(key)");
				    eeKeymap[0].put("qvalueKeyParm",
						 "Integer.valueOf"
						 + "(parms[0])");
				}
				if (types.isSameType(setofType,
						     INTEGER_MIRROR)) {
				    eeKeymap[0].put("qvalueValueParm",
						 "Integer.valueOf("
						 +"parms[1])");
				} else if (types.isSameType
					   (setofType,LONG_MIRROR)) {
				    eeKeymap[0].put("qvalueValueParm",
						 "Long.valueOf("
						 +"parms[1])");
				} else if (types.isSameType
					   (setofType,DOUBLE_MIRROR)) {
				    eeKeymap[0].put("qvalueValueParm",
						 "Double.valueOf("
						 +"parms[1])");
				} else if (types.isSameType
					   (setofType,BOOLEAN_MIRROR)) {
				    eeKeymap[0].put("qvalueValueParm",
						 "Boolean.valueOf("
						 +"parms[1])");
				} else {
				    eeKeymap[0].put("qvalueValueParm",
						 "parms[1]");
				}
				if (eeKeymap[0].get("qmapKey") == null
				    && eeKeymap[0].get("mapKey") != null) {
				    eeKeymap[0].put("qmapKey",
						    eeKeymap[0].get("mapKey"));
				}
			    } else {
				// not a set
				if (implementsNamedObjectOps(types, mapKeyType)){
				    eeKeymap[0].put("mapKeyParmType",
						    "org.bzdev.obnaming"
						    + ".NamedObjectOps");
				    String expr = "("
					+ mapKeyType.toString() 
					+ ")key";
				    eeKeymap[0].put("mapKey", expr);
				}  else if (isEnum(types, mapKeyType)) {
				    eeKeymap[0].put("mapKeyParmType",
						    "java.lang.Enum<?>");
				    String expr = "("  +mapKeyType.toString()
					+ ")key";
				    eeKeymap[0].put("mapKey", expr);
				}
			    }
			    /*
			    messager.printMessage(Kind.NOTE,
						  "finished a "
						  + "keyedPrimitiveParm");
			    */
			    TemplateProcessor.KeyMap alistKeyMap =
				new TemplateProcessor.KeyMap();
			    alistKeyMap.put("keyedPrimitiveCase", eeKeymap);
			    alist.add(alistKeyMap);
			}
		    }
		}

		keymap.put("parmLoop",
			   alist.toArray
			   (new TemplateProcessor.KeyMap[alist.size()]));
		if (generate) {
		    FileObject fo = null;
		    InputStreamReader reader = null;
		    Writer writer = null;
		    if (manager != null) {
			if (DEBUG) {
			    messager.printMessage(Kind.NOTE, "generating "
						  + ((currentPackage == null)?
						     "": currentPackage + ".")
						  +  manager);
			}
			// Java-extension case
			InputStream is1 = this.getClass().getClassLoader()
			    .getResourceAsStream(PPACKAGE.replace(".", "/")
						 +"/" + "ParmManager.tpl");
			if (is1 == null) {
			    try {
				// Class-path case
				fo = filer.getResource
				    (StandardLocation.CLASS_PATH,
				     PPACKAGE, "ParmManager.tpl");
				is1 = fo.openInputStream();
			    } catch (Exception ex) {
				messager.printMessage(Kind.ERROR,
						      "cannot find template");
			    }
			}
			try {
			    reader = new
				InputStreamReader(is1,
						  Charset.forName("UTF-8"));
			    String target = (currentPackage == null)? manager:
				currentPackage + "." + manager;
			    JavaFileObject jfo = filer.createSourceFile(target);
			    writer = jfo.openWriter();
			    TemplateProcessor tp =
				new TemplateProcessor(keymap);
			    tp.processTemplate(reader, writer);
			    reader.close();
			    writer.flush();
			    writer.close();
			} catch (Exception ex) {
			    try {
				if (reader != null) {
				    reader.close();
				}
				if (writer != null) {
				    writer.flush();
				    writer.close();
				}
			    } catch (Exception ex2) {}
			    messager.printMessage(Kind.ERROR,
						  "cannot generate file "
						  + manager + ".java"
						  + "\n ... "
						  +ex.getMessage());
			}
		    }
		    if (stdFactory != null && stdFactory.length() > 0) {
			fo = null;
			InputStream is2 = this.getClass().getClassLoader()
			    .getResourceAsStream(PPACKAGE.replace(".", "/")
						 +"/" + "StdFactory.tpl");
			if (is2 == null) {
			    try {
				// Class-path case
				fo = filer.getResource
				    (StandardLocation.CLASS_PATH,
				     PPACKAGE, "StdFactory.tpl");
				is2 = fo.openInputStream();
			    } catch (Exception ex) {
				messager.printMessage(Kind.ERROR,
						      "cannot find std factory "
						      + "template");
			    }
			}
			reader = null;
			writer = null;
			try {
			    reader = new
				InputStreamReader(is2,
						  Charset.forName("UTF-8"));
			    String target = (currentPackage == null)?
				stdFactory:
				currentPackage + "." + stdFactory;
			    JavaFileObject jfo = filer.createSourceFile(target);
			    writer = jfo.openWriter();
			    TemplateProcessor tp =
				new TemplateProcessor(keymap);
			    tp.processTemplate(reader, writer);
			    reader.close();
			    writer.flush();
			    writer.close();
			} catch (Exception ex) {
			    try {
				if (reader != null) {
				    reader.close();
				}
				if (writer != null) {
				    writer.flush();
				    writer.close();
				}
			    } catch (Exception ex2) {}
			    messager.printMessage(Kind.ERROR,
						  "cannot generate file "
						  + stdFactory + ".java"
						  + "\n ... "
						  +ex.getMessage());
			}
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
