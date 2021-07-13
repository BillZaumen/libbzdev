package org.bzdev.lang.spi;
import java.io.InputStream;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;

/**
 * SPI (Service Provider Interface) for object-namer launcher
 * initialization data.
 * Some packages can provide additional data for an object namer
 * but do not contain an object namer itself. This interface
 * provides the SPI for this additional data, which must be a
 * YAML file describing the configuration. This SPI provides
 * a name used to look up the service provider, an input stream
 * to read the corresponding YAML file (which must be UTF-8 encoded),
 * and a tab spacing to use if tabs appear in the input file.
 * <P>
 * The BZDev class library names each service provider by using
 * the last component of the service provider's module name.
 * These names are 
 * <UL>
 *   <LI><B>desktop</B> - to allow the use of the class
 *       {@link org.bzdev.swing.AnimatedPanelGraphics}.
 *   <LI><B>math</B> - to provide additional constants and functions
 *   <LI><B>p3d</B> - to provide classes that allow animations of
 *       3D models to be shown.
 * </UL>
 * The additional data is obtained by calling
 * {@link ONLauncherData#getInputStream()}, which returns an input
 * stream containing UTF-8 encoded character data providing a YAML 1.2
 * document whose top-level object contains the following properties,
 * the values of which are lists:
 * <UL>
 *   <LI><B>argumentTypes</B> - a {@link JSArray} providing a list of
 *       strings giving the fully qualified class names for arguments
 *       used by constructors, functions, and methods. The types
 *       String, int, double, Integer, or Double should not be used,
 *       as these are allowed by default.
 *   <LI><B>fieldClasses</B> - a {@link JSArray} providing a list of
 *       strings giving the fully qualified class names for classes
 *       containing fields that can be used. The types of the fields
 *       that will be included are boolean, int, long, double,
 *       {@link String}, or an enumeration.
 *   <LI><B>functionClasses</B> - a {@link JSArray} providing a list of
 *       strings the fully qualified class names for classes whose public,
 *       static methods returning an allowable type have a fixed number
 *       of arguments whose types are boolean, int, long, double, or
 *       a type provided by the <B>argumentTypes</B> property.
 *   <LI><B>methodClasses</B> - a {@link JSArray} providing a list of
 *       strings giving the fully qualified class names classes whose
 *       instance methods returning an allowable type have a fixed number
 *       of arguments with types int, double, long, boolean, {@link String},
 *       or a type provided by the <B>argumentTypes</B> property.
 *   <LI><B>returnTypes</B> - a {@link JSArray} providing a list of
 *       strings giving the fully qualified class names for objects that
 *       the parser can return or can construct.  The constructors that
 *       will be provided are those with a fixed number of arguments
 *       whose types are int, long, double, boolean, {@link String}, or
 *       a type provided by the <B>argumentTypes</B> property.
 * </UL>
 * @see org.bzdev.obnaming.ObjectNamerLauncher#newInstance(String,String...)
 */
public interface ONLauncherData {
    /**
     * Get the name for this provider.
     * @return the name
     */
    String getName();

    /**
     * Get the module name for the module containing this launcher.a
     * @return the module name
     */
    default String getModuleName() {
	return getClass().getModule().getName();
    }

    /**
     * Get an input stream containing YAML-formatted initialization data
     * for an object-namer launcher.
     * @return the input stream;
     */
    InputStream getInputStream();

    /**
     * Get the tab spacing for the data stream.
     * @return the tab spacing (0 if there are no tabs)
     */
    default int getTabSpacing() {
	return 8;
    }

    /**
     * Return a description of this service provider.
     * The description should be short enough that it , the value of
     * {@link getName()}, and a few additional characters (e.g., " - ")
     * would fit on a single line.
     * @return the description
     */
    String description();
}

//  LocalWords:  SPI namer YAML UTF BZDev ONLauncherData JSArray
//  LocalWords:  getInputStream argumentTypes  fieldClasses
//  LocalWords:  boolean functionClasses methodClasses returnTypes
//  LocalWords:  newInstance
