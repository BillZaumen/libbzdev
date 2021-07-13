package org.bzdev.obnaming.spi;
import org.bzdev.obnaming.ObjectNamerLauncher;
import org.bzdev.lang.spi.ONLauncherData;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
/**
 * SPI (Service Provider Interface) for {@link ObjectNamerLauncher}.
 * The naming convention used by the BZDev class library and some
 * related libraries is to use the last component of a module name
 * as the value returned by {@link #getName()} unless there is a
 * conflict. Service providers in the BZDev library will create
 * object-namer launchers with the following names:
 * <UL>
 *   <LI> <B>anim2d</B> to create an instance of
 *        {@link org.bzdev.anim2d.Animation2DLauncher}.
 *   <LI> <B>devqsim</B> -  to create an instance of
 *        {@link org.bzdev.devqsim.SimulationLauncher}.
 *   <LI> <B>drama</B> -  to create an instance of
 *        {@link org.bzdev.drama.DramaSimulationLauncher}.
 * </UL>
 * The class {@link org.bzdev.lang.spi.ONLauncherData},
 * which this class extends, provides methods for getting the name
 * of a launcher and an input stream for YAML-formatted data describing
 * the expressions the launcher can handle. The input stream must
 * use a UTF-8 character set. If the input stream contains tabs, the
 * method {@link ONLauncherData#getTabSpacing()} must be defined if
 * the spacing is not 8.
 * The stream must be a YAML 1.2 document whose top-level object contains
 * the following properties, the values of which are lists:
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
 * @see org.bzdev.obnaming.ObjectNamerLauncher#newInstance(String)
 * @see org.bzdev.obnaming.ObjectNamerLauncher#newInstance(String,String...)
 */
public interface ONLauncherProvider extends ONLauncherData
 {
    /**
     * Get the class for the {@link ObjectNamerLauncher} that this
     * this provider will help create.
     */
    Class<? extends ObjectNamerLauncher> onlClass();
}

//  LocalWords:  SPI ObjectNamerLauncher BZDev getName namer anim UTF
//  LocalWords:  devqsim SimulationLauncher DramaSimulationLauncher
//  LocalWords:  YAML ONLauncherData getTabSpacing argumentTypes
//  LocalWords:  JSArray qualitifed fieldClasses boolean returnTypes
//  LocalWords:  functionClasses methodClasses newInstance
