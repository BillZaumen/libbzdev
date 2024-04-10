package org.bzdev.ejws;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.net.ssl.TrustManager;

import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.ServletAdapter;
import org.bzdev.net.SSLUtilities;
import org.bzdev.util.JSArray;
import org.bzdev.util.JSObject;
import org.bzdev.util.JSUtilities;
import org.bzdev.util.JSUtilities.YAML;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.TemplateProcessor;
import org.bzdev.util.TemplateProcessor.KeyMap;

//@exbundle org.bzdev.ejws.lpack.ConfigurableWS


/**
 * Configurable web server.
 * This class supports web servers that can be configured, at least
 * partially  with a configuration file.  The server can be configured
 * to use HTTPS, either directly or with a {@link CertManager}, which is
 * obtained using a Java service-provider interface.  When HTTPS is used,
 * an additional server running HTTP will be provided. This server will
 * provide an HTTP redirect to the HTTPS server. An additional server
 * will not be provided when HTTPS is not used.
 * <P>
 * When the configuration file is a Java {@link Properties} file using
 * the format described by the API documentation for
 * {@link Properties#load(Reader)}. A number of properties, called
 * "standard properties" below, are always available, but can be
 * supplemented by a set of properties provided in a constructor.
 * In addition, the input file can be a
 * <A HREF="https://linuxhandbook.com/yaml-basics/">YAML</A>
 * file as described <A HREF="#YAML">below</A>.  When the configuration
 * file is a YAML file, its file name must end with the file-name
 * extension "YAML", "yaml", "YML", or "yml".
 * <P>
 * The standard properties can be grouped into three categories:
 * <UL>
 *   <LI><A HREF="#colors">Configuring CSS colors</A> describes
 *	  how to set colors used by various web pages.
 *   <LI><A HREF="#SSL">SSL properties</A> describes properties
 *	  specific to SSL
 *   <LI><A HREF="#PARMS">Server parameters</A> describes how to
 *	  configure general server parameters, including whether or
 *	  not SSL is used.
 * </UL>
 * For <A ID="colors"> colors</A>, the standards properties use
 * values that are
 * <A HREF="https://www.w3schools.com/css/css_colors.asp">CSS colors</A>.
 * These properties are
 * <UL>
 * <LI><B>color</B>. This is the color used for text. If missing,
 *    a default, <B>white</B>, will be used
 * <LI><B>bgcolor</B>. This is the color used for the background.  If
 *    missing,  a default, <B>rgb(10,10,25)</B>, will be used.
 * <LI><B>linkColor</B>. This is the color used for links.  If
 *    missing,  a default, <B>rgb(65,225,128</B>, will be used.
 * <LI><B>visitedColor</B>. This is the color used for visited links.  If
 *    missing,  a default, <B>rgb(65,165,128)</B>, will be used.
 * <UL>
 * <P>
 * For <A ID="SSL">SSL parameters</A>, the standard properties are
 * <UL>
 *   <LI><B>keyStoreFile</B>. The value is the file name of the
 *     key store, which is used to hold the server&apos;s certificate.
 *     Relative file names are resolved against the directory containing
 *     the configuration file.
 *   <LI><B>keyStorePassword</B>. The value is the password used by
 *       the keystore file.
 *   <LI><B>keyPassword</B>. The value is the password used for individual
 *     entries in the keystore file.  If not provided, the default is the
 *     value of <B>keyStorePassword</B>.
 *   <LI><B>trustStoreFile</B>. The value is the file name of the
 *     trust store, which provides certificates to trust in
 *     addition to those provided by Java for certificate authorities.
 *     This can be used when an organization creates an internal
 *     certificate authority for the organization&apos;s use.
 *     Relative file names are resolved against the directory containing
 *     the configuration file.
 *   <LI><B>trustStorePassword</B>. The value is the password used by
 *       the keystore file.
 *   <LI><B>allowLoopback</B>. When <B>true</B>, a loopback host name
 *     will be recognized as valid. The default is <B>false</B>.
 *   <LI><B>allowSelfSigned</B>. When <B>true</B>, self-signed
 *     certificates are accepted.  The default is <B>false</B>. This
 *     option is useful for testing, but should not be used otherwise.
 *   <LI><B>certificateManager</B>. When present, the value is either
 *     a simple name or a fully-qualified class name for a certificate
 *     manager. The value "default" will set up the server so that a
 *     self-signed certificate is automatically generated and the
 *     corresponding keystore file will be created if it is not already
 *     present.  Certificates are renewed automatically. The Docker
 *     container wtzbzdev/ejwsacme includes a certificate manger whose
 *     simple name is "AcmeClient" and which will get a certificate from the
 *     Let's Encrypt certificate authority. In this case, the
 *     properties <B>domain</B> and <B>email</B> are required, and
 *     the server's DNS server must be configured to map the domain name
 *     to the server's IP address (i.e., by setting an 'A' or 'AAAA"
 *     record).
 *   <LI><B>certMode</B>. When present, the value may be <B>NORMAL</B>,
 *     <B>STAGED</B>, or <B>TEST</B>. The default is <B>NORMAL</B>.
 *     The value <B>STAGED</B> is useful for initial testing when the
 *     certificate manager's provider name is <B>AcmeClient</B>: the
 *     Let's Encrypt server will then generate a non-functioning certificate
 *     but the ACME protocol will be used to download a certificate and
 *     receiving a certificate indicates that there is not a configuration
 *     error.  When <B>certMode</B> is <B>TEST</B>, programs that
 *     implement the ACME protocol will not actually be run. This
 *     value is intended for some tests of the AcmeClient provider.
 *   <LI><B>certName</B>. This is a name used to tag a certificate.
 *   <LI><B>alwaysCreate</B>. WHen present, the value may be
 *     <STRONG>true</STRONG> or <STRONG>false</STRONG> (the default).
 *     When true, a new certificate will be created every time a
 *     certificate manager checks if the current certificate has
 *     expired. This is useful for testing, but should not be used
 *     otherwise.
 *   <LI><B>domain</B>. This is the fully-qualified domain name for the
 *     server.  It is used to create the distinguished name in a
 *     certificate.
 *   <LI><B>email</B>. This is an email address used by some
 *     certificate authorities to send notifications about expiring
 *     certificates.
 *   <LI><B>timeOffset</B>. The time offset in seconds from
 *     midnight, local time, at which a server should determine if
 *     a certificate should be renewed.
 *   <LI><B>interval</B>.  The number of days between attempts to
 *     renew a certificate.
 *   <LI><B>stopDelay</B>. The time interval in seconds from a request
 *     to shutdown a server to when the server is actually shut down.
 *     This is used to give transactions being processed time to complete
 *     and will be used only when a new certificate is needed..
 * </UL>
 * <P>
 * For <A ID="PARMS">server parameters</A>, the standard properties are
 * <UL>
 *   <LI><B>sslType</B>. When present, the values should be either
 *     <B>SSL</B> or <B>TLS</B>. TLS is preferred. When absent or
 *     when the value is <B>null</B>, HTTP is used instead of HTTPS.
 *   <LI><B>ipaddr</B>. The value is the numeric IP address on which
 *     this server listens, with two special values:
 *     <P>
 *     <UL>
 *       <LI><B>wildcard</B>. The server will use the wildcard address
 *         (this is the default).
 *       <LI><B>loopback</B>. The server will use the loopback
 *         address. Note that within a Linux container (e.g., when using
 *         Docker), the loopback address may be interpreted as the
 *         loopback address inside the container, no the loopback address
 *         of the system.
 *     </UL>
 *   <LI><B>port</B>. The value is the server&apos;s TCP port.  If missing
 *     the port is by default set to 80 for HTTP and 443 for HTTPS.
 *   <LI><B>helperPort</B>. The value is an alternate server&apos;s
 *     TCP port for use with HTTP when the certificate manager (if
 *     provided) does not provide an HTTP port to use.  If the
 *     value is zero or the parameter is not defined, and the
 *     certificate manager does not provide a port, an HTTP server
 *     will not be started.  If an HTTP server is started, it will provide
 *     an HTTP redirect to the HTTPS server for web pages containing
 *     documentation and public keys, and for queries. For POST methods,
 *     the HTTPS server has to be used directly.  If the certificate
 *     manager does not provide a port, this value must be set when an
 *     HTTP server is desired.
 *   <LI><B>backlog</B>. When present, the value is an integer providing
 *     the <A HREF="https://veithen.io/2014/01/01/how-tcp-backlog-works-in-linu*x.html">TCP backlog</A>.
 *    The default value is 30.
 *   <LI><B>nthreads</B>.
 *     The number of threads the server can use. The default is 50.
 *   <LI><B>trace</B>. A value of <B>true</B> indicates that the
 *     execution of a request will be traced, printing out what
 *     occurred on standard output.  The default is <B>false</B>.
 *   <LI><B>stackTrace</B>. A value of <B>true</B> indicates that
 *     errors resulting from GET or POST methods will generate a
 *     stack trace.  The default is <B>false</B>.
 * </UL>
 * <P>
 * <A ID="YAML">YAML</A> can be used as a configuration-file format,
 * which provides some additional capabilities. A YAML configuration file must
 * have a file-name suffix, either ".yml", ".yaml", ".YML", or ".YAML".
 * The syntax is
 * <BLOCKQUOTE><PRE>
 * %YAML 1.2
 * ---
 * config:
 *    <STRONG>NAME</STRONG>: <STRONG>VALUE</STRONG>
 *    ... (additional name-value pairs)
 * contexts:
 *    - prefix:  <STRONG>PREFIX</STRONG>
 *      className: <STRONG>CLASSNAME</STRONG>
 *      arg: <STRONG>ARGUMENT</STRONG>
 *      useHTTP: <STRONG>BOOLEAN</STRONG>
 *      welcome: 
 *        - <STRONG>PATH</STRONG>
 *        ... (additional welcome path names)
 *      parameters:
 *        <STRONG>NAME</STRONG>: <STRONG>VALUE</STRONG>
 *        ... (additional parameters)
 *      propertyNames:
 *         - <STRONG>NAME</STRONG>
 *         ... (additional property names)
 *      methods:
 *         - <STRONG>METHOD</STRONG>
 *         ... (additional methods)
 *      nowebxml:  <STRONG>BOOLEAN</STRONG>
 *      displayDir: <STRONG>BOOLEAN</STRONG>
 *      hideWebInf: <STRONG>BOOLEAN</STRONG>
 *      color: <STRONG>CSS_COLOR</STRONG>
 *      bgcolor: <STRONG>CSS_COLOR</STRONG>
 *      linkColor: <STRONG>CSS_COLOR</STRONG>
 *      visitedColor: <STRONG>CSS_COLOR</STRONG>
 *    -... (additional contexts)
 * ... (additional keys other than "config:" and "context:")
 * ...
 * </PRE></BLOCKQUOTE>
 * where
 * <UL>
 *   <LI><STRONG>PREFIX</STRONG> is the start of the path component of
 *      a URL
 *   <LI><STRONG>CLASSNAME</STRONG> is the class name for a suitable
 *      subclass  of {@link WebMap}. The class names that are directly
 *      supported are
 *      <UL>
 *         <LI>{@link org.bzdev.ejws.maps.DirWebMap}
 *         <LI>{@link org.bzdev.ejws.maps.RedirectWebMap}
 *         <LI>{@link org.bzdev.ejws.maps.ResourceWebMap}
 *         <LI>{@link org.bzdev.ejws.maps.ZipWebMap}
 *         <LI>{@link org.bzdev.ejws.maps.ServletWebMap}
 *      </UL>
 *      although the method
 *      {@link ConfigurableWS#addContext(String,String,String,EmbeddedWebSever,Map,String[],HttpMethod[],String,String,String,String,boolean,boolean,boolean)},
 *       if implemented, may support additional web maps.
 *   <LI><STRONG>ARGUMENT</STRONG> is the string representation of the
 *      argument required to initialize a web map. For
 *      <UL>
 *         <LI>{@link DirWebMap}, the argument is a file name.
 *         <LI>{@link RedirectWebMap}, the argument is a URL.
 *         <LI>{@link ResourceWebMap}, the argument is the initial part
 *             of a Java resource name, and should end with a '/'.
 *         <LI>{@link ZipWebMap}, the argument is a string naming a
 *             ZIP file.
 *         <LI>{@link ServletWebMap}, the class name for a servlet adapter
 *             that has a zero-argument constructor.
 *      </UL>
 *  <LI><STRONG>BOOLEAN</STRONG> is <STRONG>true</STRONG> or
 *      <STRONG>false</STRONG>.  If absent, the default is
 *      <STRONG>false</STRONG>.
 *  <LI><STRONG>PATH</STRONG> is the path to a "welcome" file
 *      If the path is a relative path, it is relative to the prefix path.
 *   <LI><STRONG>NAME</STRONG> is the name of a configuration property
 *      (listed under "config:") or a parameter.
 *   <LI><STRONG>VALUE</STRONG> is a string representation of a value
 *      corresponding to the given name.
 *   <LI> <STRONG>METHOD</STRONG> is the string CONNECT, DELETE, GET,
 *      HEAD, OPTIONS, POST, PUT, TRACE and indicates which HTTP
 *      methods are supported for the specified prefix.
 *   <LI><STRONG>BOOLEAN</STRONG> is either "true" or "false" (quotation
 *      marks are not necessary).
 *   <LI><STRONG>CSS_COLOR</STRONG> is a CSS color specification, provided
 *      as a string.
 * </UL>
 * The "config:" section provides the same information as a corresponding
 * {link Properties} file. The "contexts" section allows one to
 * add specific prefixes to the web server without having to
 * use
 *  {@link EmbeddedWebServer#add(String,String,Object,com.sun.net.httpserver.Authenticator,boolean,boolean,boolean)}
 * explicitly. The value for "contexts" is a list of YAML objects. For each
 * object, the value of the key
 * <UL>
 *   <LI><STRONG>prefix</STRONG> is the path for an HTTP context corresponding
 *     to a {@link WebMap}.
 *   <LI><STRONG>className</STRONG> is the class name of the
 *      {@link WebMap}.
 *   <LI><STRONG>arg</STRONG> is the argument or the string representation
 *      of an argument used to initialize a web map. For the class
 *      {@link ServletWebMap}, the argument is the fully qualified class
 *      name of a {@link ServletAdapter}.
 *   <LI><STRONG>useHTTP</STRONG>, when true, preferentially assigns
 *      this context to the helper, which runs HTTP, rather than to
 *      the server, which runs HTTPS when there is a helper.  If
 *      false, or if there is no helper, or if this key is missing,
 *      the context is assigned to the server.
 *   <LI><STRONG>welcome</STRONG> is a list of strings, each providing the
 *       path to a 'welcome' page for a given prefix.  If there are no
 *       welcome pages, this key may be omitted.
 *   <LI><STRONG>parameters</STRONG> is an object show keys are
 *      parameter names and whose values are parameter values, as required
 *      by a {@link ServletAdapter}.  If there are no parameters, this key
 *      may be omitted.
 *   <LI><STRONG>propertyNames</STRONG> is a list of names provided by
 *      the "config" section whose names and corresponding values will be
 *      added to the parameters.  This minimizes configuration errors
 *      that could occur when the same name/value pair is used by
 *      multiple servlet adapters.  If the list is empty, this key can
 *      be omitted.
 *   <LI><STRONG>methods</STRONG> is a list of the HTTP methods that
 *      a servlet adapter supports.
 *   <LI><STRONG>nowebxml</STRONG> is true if a web.xml file should
 *      be ignored; false otherwise.  If not provided, the value
 *      is assumed to be true.
 *   <LI><STRONG>displayDir</STRONG> is true if directories should be
 *      displayed when possible; false otherwise. If not provided, the value
 *      is assumed to be true.
 *   <LI><STRONG>hideWebInf</STRONG> is true if any WEB-INF directory
 *      should be hidden; false otherwise. If not provided, the value
 *      is assumed to be true.
 *   <LI><STRONG>color</STRONG> is the CSS color for text.
 *      This will override a standard property for the current prefix.
 *   <LI><STRONG>bgcolor</STRONG> is the CSS color for the background.
 *      This will override a standard property for the current prefix.
 *   <LI><STRONG>linkColor</STRONG> is the CSS color for links; null if
 *      for a default. This will override a standard property for the
 *      current prefix.
 *   <LI><STRONG>visitedColor</STRONG> is the CSS color for visited links;
 *      null for a default. This will override a standard property for
 *      the current prefix.
 * </UL>
 * Additional keys can be added as specified in the constructor
 * {@link ConfigurableWS#ConfigurableWS(Set,Set,File,File)}, a capability
 * provided for extensibility. Any such keys will normally be processed
 * before the server is started.
 */
public class ConfigurableWS {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.ConfigurableWS");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    private static final  String REDACTED = errorMsg("redacted");


    static final Charset UTF8 = Charset.forName("UTF-8");

    // Config names, used to check for misspellings in the
    // configuration file.
    static final Set<String> propertyNames =
	 Set.of("color", "bgcolor", "linkColor", "visitedColor",
		"ipaddr", "port", "helperPort", "backlog", "nthreads",
		"trace", "stackTrace",
		"keyStoreFile", "trustStoreFile", "sslType",
		"keyStorePassword", "keyPassword", "trustStorePassword",
		"allowLoopback", "allowSelfSigned",
		"certificateManager", "certMode", "alwaysCreate",
		"certName", "domain", "email", "timeOffset",
		"interval", "stopDelay");

    // required property names if a certificate manager is used.
    static final Set<String> cmPropertyNames =
	Set.of("sslType", "domain", "keyStoreFile");

    static final Set<String> standardKeys = Set.of("config", "contexts");

    boolean trace = false;
    boolean stacktrace = false;

    /**
     * Determine if tracing is configured.
     * @return true if tracing is configured; false otherwise
     */
    public boolean trace() {return trace;}

    /**
     * Determine if stack tracing is configured.
     * @return true if stack tracing is configured; false otherwise
     */
    public boolean stacktrace() {return stacktrace;}


    PrintStream log = System.out;

    /**
     * Get the print stream used for logging.
     * @return the print stream; null if there is none.
     */
    public PrintStream getLog() {
	return log;
    }


    // CSS colors.
    String color = "white";
    String bgcolor = "rgb(10,10,25)";
    String linkColor = "rgb(65,225,128)";
    String visitedColor = "rgb(65,164,128)";

    /**
     * Get a CSS color for foreground text.
     * @return the color
     */
    public String color() {return color;}

    /**
     * Get a CSS color for background text.
     * @return the color
     */
    public String bgcolor() {return bgcolor;}

    /**
     * Get a CSS color for links.
     * @return the color
     */
    public String linkColor() {return linkColor;}

    /**
    /**
     * Get a CSS color for visited links.
     * @return the color
     */
    public String visitedColor() {return visitedColor;}

    static boolean defaultTrace = false;
    static boolean defaultStacktrace = false;

    /**
     * Set the defaults for tracing.
     * The initial value for both arguments is false.
     * @param defaultTrace true if tracing is turned on by default; false
     *        otherwise
     * @param defaultStacktrace true if a stack trace should be provided
     *        when an error occurs during tracing; false otherwise
     */
    public static void setTraceDefaults(boolean defaultTrace,
				 boolean defaultStacktrace)
    {
	ConfigurableWS.defaultTrace = defaultTrace;
	ConfigurableWS.defaultStacktrace = defaultStacktrace;
    }

    boolean needHelperStart = false;
    EmbeddedWebServer helper = null;
    EmbeddedWebServer ews = null;
    
    Properties props = new Properties();
    JSArray  contextArray = null;
    JSObject remainder = null;

    /**
     * Get the properties defined in a configuration file.
     * When the configuration file uses YAML syntax, these
     * properties are the keys for the object that is the value
     * for the top-level key "config".
     * @return the properties
     */
    public Properties getProperties() {return props;}

    /**
     * Get the contexts specified in the configuration file.
     * This method returns a non-null value only for YAML configuration
     * files. The values listed are the values for the top-level key
     * "contexts".
     * @return the contexts listed in the configuration file; null
     *         if no contexts are specified.
     */
    public JSArray getContexts() {
	return contextArray;
    }

    /**
     * Get the keyword/value pairs specified at the top level of
     * the configuration file.
     * This method returns a non-null value only for YAML configuration
     * files.
     * <P>
     * This method can be used if a YAML configuration file contains
     * extra keys at its top level and is provided to make configuring
     * a server extensible.
     * @return an object containing the keyword/value pairs; null if
     *         there are none or if the only keywords are "config"
     *         or "contexts"
     */
    public JSObject getRemainder() {
	return remainder;
    }


    /**
     * Get the {@link EmbeddedWebServer} that will handle HTTP
     * or HTTPS requests.
     * @return the server
     */
    public EmbeddedWebServer  getServer() {return ews;}

    /**
     * Get the {@link EmbeddedWebServer} that will handle HTTP
     * as a 'helper' for the server returned by {@link #getServer()}.
     * When a helper exists, its default behavior is to provided an
     * HTTP redirect to the the server returned by {@link #getServer()}.
     * Additional contexts can be added to the helper server, either
     * explicitly or implicitly (by setting useHTTP to true for a
     * context).
     * @return the helper server; null if there is none
     */
    public EmbeddedWebServer getHelper() {return helper;}


    /**
     * Set a system property indicating that the JVM is
     * headless.
     */
    public static void headless() {
	System.setProperty("java.awt.headless", "true");
    }

    /**
     * Add a context to the web server.
     * This method is called by the constructor, and can override
     * the default behavior or handle additional {@link WebMap} subclasses.
     * When this is done, the method must return true.
     * <P>
     * The web-map classes automatically handled are {@link DirWebMap},
     * {@link RedirectWebMap}, {@link ResourceWebMap}, {@link ZipWebMap},
     * and {@link ServletWebMap}.
     *
     * @param prefix the path for a context
     * @param className the class name for a {@link WebMap}
     * @param arg the argument used to configure the {@link WebMap}
     * @param svr the server being modified
     * @param map a map assigning parameter names to parameter values
     * @param welcome the welcome paths; an empty array if there are none
     * @param methods the HTTP methods a servlet can use
     * @param color the CSS color for text
     * @param bgcolor the CSS color for the background
     * @param linkColor the CSS color for links
     * @param visitedColor the CSS color for visited links
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true if the caller wants directories displayed when
     *                possible; false otherwise
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @return true if the context was added; false if the request
     *         was ignored.
     * @throws Exception if an error occurs
     */
    protected boolean addContext(String prefix, String className,
				 String arg,
				 EmbeddedWebServer svr,
				 Map<String,String> map,
				 String[] welcome,
				 HttpMethod[] methods,
				 String color, String bgcolor,
				 String linkColor, String visitedColor,
				 boolean nowebxml,
				 boolean displayDir,
				 boolean hideWebInf)
	throws Exception
    {
	return false;
    }

    private void addContexts() throws Exception {
	if (contextArray != null) {
	    int sz = contextArray.size();
	    for (int i = 0; i < sz; i++) {
		JSObject obj = contextArray.get(i, JSObject.class);
		String prefix = obj.get("prefix", String.class);
		if (prefix == null) {
		    throw new Exception(errorMsg("context1"));
		}
		String className = obj.get("className", String.class);
		if (className == null) {
		    String msg = errorMsg("context2", "className", prefix);
		    throw new Exception(msg);
		}
		String arg = obj.get("arg", String.class);
		if (arg == null) {
		    throw new Exception(errorMsg("context2", "arg", prefix));
		}
		Boolean useHTTPB = obj.get("useHTTP", Boolean.class);
		boolean useHTTP = (useHTTPB == null)? false: useHTTPB;
		JSObject parameters = obj.get("parameters", JSObject.class);
		Boolean nowebxmlB = obj.get("nowebxml", Boolean.class);
		Boolean displayDirB = obj.get("displayDir", Boolean.class);
		Boolean hideWebInfB = obj.get("hideWebInf", Boolean.class);

		boolean nowebxml = (nowebxmlB == null)? true: nowebxmlB;
		boolean displayDir = (displayDirB == null)? true: displayDirB;
		boolean hideWebInf = (hideWebInfB == null)? true: hideWebInfB;
		    
		String color = obj.get("color", String.class);
		String bgcolor = obj.get("bgcolor", String.class);
		String linkColor = obj.get("linkColor", String.class);
		String visitedColor = obj.get("visitedColor", String.class);
		if (color == null) color = color();
		if (bgcolor == null) bgcolor = bgcolor();
		if (linkColor == null) linkColor = linkColor();
		if (visitedColor == null) visitedColor = visitedColor();

		JSArray propNames = obj.get("propertyNames", JSArray.class);
		Map<String,String> map =
		    (parameters == null && propNames == null)? null:
		    new HashMap<String,String>();
		if (parameters != null) {
		    for (String key: parameters.keySet()) {
			map.put(key, parameters.get(key,String.class));
		    }
		}
		if (propNames != null) {
		    propNames.forEach((o) -> {
			    if (o instanceof String) {
				String key = (String)o;
				String value = props.getProperty(key);
				if (value != null) {
				    map.put(key, value);
				}
			    }
			});
		}
		JSArray welcomeA = obj.get("welcome", JSArray.class);
		String[] welcome = null;
		if (welcomeA == null) {
		    welcome = new String[0];
		} else {
		    int wsz = welcomeA.size();
		    welcome = new String[wsz];
		    for (int j = 0; j < wsz; j++) {
			welcome[j] = welcomeA.get(j, String.class);
		    }
		}
		
		JSArray methodsA = obj.get("methods", JSArray.class);
		HttpMethod[] methods = null;
		if (methodsA != null) {
		    methods = new HttpMethod[methodsA.size()];
		    int msz = methodsA.size();
		    for (int j = 0; j < msz; j++) {
			String nm = methodsA.get(j, String.class);
			if (nm.equals("CONNECT")) {
			    methods[j] = HttpMethod.CONNECT;
			} else if (nm.equals("DELETE")) {
			    methods[j] =  HttpMethod.DELETE;
			} else if (nm.equals("GET")) {
			    methods[j] =  HttpMethod.GET;
			} else if (nm.equals("HEAD")) {
			    methods[j] =  HttpMethod.HEAD;
			} else if (nm.equals("OPTIONS")) {
			    methods[j] =  HttpMethod.OPTIONS;
			} else if (nm.equals("POST")) {
			    methods[j] =  HttpMethod.POST;
			} else if (nm.equals("PUT")) {
			    methods[j] =  HttpMethod.PUT;
			} else if (nm.equals("TRACE")) {
			    methods[j] =  HttpMethod.TRACE;
			} else {
			    throw new Exception(errorMsg("badMethod", nm));
			}
		    }
		}
		if (className.indexOf('.') == -1) {
		    className = "org.bzdev.ejws.maps." + className;
		} else if (className.charAt(0) == '.') {
		    className = className.substring(1);
		}
		EmbeddedWebServer svr = (useHTTP && helper != null)?
		    helper: ews;
		if (!addContext(prefix, className, arg, svr,
				map, welcome, methods,
				color, bgcolor, linkColor, visitedColor,
				nowebxml, displayDir, hideWebInf)) {
		    if (className.equals("org.bzdev.ejws.maps.DirWebMap")) {
			System.out.println("arg = " + arg);
			svr.add(prefix, DirWebMap.class,
				new DirWebMap.Config(new File(arg),
						     color, bgcolor,
						     linkColor, visitedColor),
				null, nowebxml, displayDir, hideWebInf);
		    } else if (className
			       .equals("org.bzdev.ejws.maps.RedirectWebMap")) {
			svr.add(prefix, RedirectWebMap.class, arg,
				null, nowebxml, displayDir, hideWebInf);
		    } else if (className
			       .equals("org.bzdev.ejws.maps.ResourceWebMap")) {
			svr.add(prefix, ResourceWebMap.class,
				new ResourceWebMap.Config(arg,
							  color, bgcolor,
							  linkColor,
							  visitedColor),
				null, nowebxml, displayDir, hideWebInf);
		    } else if (className
			       .equals("org.bzdev.ejws.maps.ZipWebMap")) {
			svr.add(prefix, ZipWebMap.class,
				new ZipWebMap.Config(new File(arg),
						     color, bgcolor,
						     linkColor, visitedColor),
				null, nowebxml, displayDir, hideWebInf);
		    } else if (className
			       .equals("org.bzdev.ejws.maps.ServletWebMap")) {
			Class<? extends ServletAdapter> adapterClass =
			    Class.forName(arg)
			    .asSubclass(ServletAdapter.class);
			Constructor<? extends ServletAdapter> constructor
			    = adapterClass.getConstructor();
			ServletAdapter adapter = constructor.newInstance();
			String allowsQueryS = obj.get("allowsQuery",
						      String.class);
			boolean allowsQuery = (allowsQueryS == null)? true:
			    allowsQueryS.trim().equalsIgnoreCase("true");
			System.out.println("adapter = " + adapter);
			System.out.println("allowsQuery = " + allowsQuery);
			System.out.println("methods = " + methods);
			for (HttpMethod m: methods) {
			    System.out.println("   ... " + m);
			}
			svr.add(prefix, ServletWebMap.class,
				new ServletWebMap.Config(adapter, map,
							 allowsQuery,
							 methods),
				null, nowebxml, displayDir, hideWebInf);
		    } else {
			String msg =
			    errorMsg("cannotConfig", prefix, className);
			throw new Exception(msg);
		    }
		    if (welcome.length > 0) {
			for (String path: welcome) {
			    svr.getWebMap(prefix).addWelcome(path);
			}
		    }
		}
	    }
	}
    }

   /**
     * Constructor.
     * The configuration file is described {@link ConfigurableWS above}.
     * When the configuration file is a YAML file, its file name must have
     * the file-name extension "YAML", "yaml", "YML", or "yml".
     * @param configFile the configuration file
     * @param logFile the log file ; null for standard output
     * @throws Exception if an error occurs
     */
    public ConfigurableWS(File configFile, File logFile)
	throws Exception
    {
	this(null, null, configFile, logFile);
    }

    /**
     * Constructor specifying additional property names.
     * The configuration file is described {@link ConfigurableWS above}.
     * When the configuration file is a YAML file, its file name must have
     * the file-name extension "YAML", "yaml", "YML", or "yml".
     * <P>
     * When the first argument is not null, the user must handle
     * any configuration required by those properties explicitly.
     * The method {@link #getProperties()} can be used to obtain the
     * a {@link Properties} object providing the values for any property.
     * @param additionalPropertyNames additional property names; null if
     *        there are none.
     * @param configFile the configuration file
     * @param logFile the log file ; null for standard output
     * @throws Exception if an error occurs
     */
    public ConfigurableWS(Set<String> additionalPropertyNames,
			  File configFile,
			  File logFile)
	throws Exception
    {
	this(additionalPropertyNames, null, configFile, logFile);
    }


    /**
     * Constructor specifying additional property names and additional keys.
     * The configuration file is described {@link ConfigurableWS above}.
     * When the configuration file is a YAML file, its file name must have
     * the file-name extension "YAML", "yaml", "YML", or "yml".
     * <P>
     * When the first argument is not null, the user must handle
     * any configuration required by those properties explicitly.
     * The method {@link #getProperties()} can be used to obtain the
     * a {@link Properties} object providing the values for any property.
     * When the second argument is not null, the user  must handle
     * any keys required by those properties explicitly.
     * The method {@link #getRemainder()} can be used to obtain a
     * {@link JSObject} with values accessed by any key other than
     * "config" or "context".
     * @param additionalPropertyNames additional property names; null if
     *        there are none.
     * @param additionalKeys additional keys for the top-level object in
     *        a YAML-based configuration file; null if there are none
     * @param configFile the configuration file
     * @param logFile the log file ; null for standard output
     * @throws Exception if an error occurs
     */
    public ConfigurableWS(Set<String> additionalPropertyNames,
			  Set<String> additionalKeys,
			  File configFile,
			  File logFile)
	throws Exception
    {
	int offset = 0;
	/*
	String buttonFGColor = "white";
	String buttonBGColor = "rgb(10,10,64)";
	String bquoteBGColor = "rgb(32,32,32)";
	*/
	int port = 80;
	int helperPort = 0;
	boolean setHelperPort = false;
	int backlog = 30;
	int nthreads = 50;
	trace = defaultTrace;
	stacktrace = defaultStacktrace;
	InetAddress addr = null;

	TreeSet<String> allowedNames = new TreeSet<>(propertyNames);
	if (additionalPropertyNames != null) {
	    allowedNames.addAll(additionalPropertyNames);
	}

	TreeSet allowedKeys =new TreeSet<>(standardKeys);
	if (additionalKeys != null) {
	    allowedKeys.addAll(additionalKeys);
	}


	EmbeddedWebServer.SSLSetup sslSetup = null;
	CertManager cm = null;
	CertManager.Mode cmMode = CertManager.Mode.NORMAL;
	boolean alwaysCreate = false;
	String sslType = null;
	File keyStoreFile = null;
	char[] keyStorePW = null;
	char[] keyPW = null;
	File trustStoreFile = null;
	char[] trustStorePW = null;
	boolean loopback = false;
	boolean selfsigned = false;
	String certName = "docsig";
	String domain = null;
	String email = null;
	int timeOffset = 0;
	int interval = 90;
	int stopDelay = 5;

	// File cdir = new File(System.getProperty("user.dir"));
	// File uadir = new File("/usr/app");

	log = (logFile != null)?
	    new PrintStream(new FileOutputStream(logFile), true, UTF8):
	    System.out;


	// System.out.println("argv.length = " + argv.length);
	// System.out.println("offset = " + offset);
	// System.out.flush();
	if (configFile != null) {
	    if (!configFile.exists()) {
		throw new FileNotFoundException
		    (errorMsg("noConfigFile", configFile));
	    }
	    File dir = configFile.getParentFile();

	    if (dir == null) dir = new File(System.getProperty("user.dir"));

	    log.println(errorMsg("confFile", configFile));
	    if (configFile.canRead()) {
		// System.out.println("propFile is readable");
		Reader r = new FileReader(configFile, UTF8);
		String fname = configFile.getName();
		if (fname.endsWith(".yml") || fname.endsWith(".yaml")
		    || fname.endsWith(".YML") || fname.endsWith(".YAML")) {
		    Object obj = JSUtilities.YAML.parse(r);
		    if (obj != null && obj instanceof JSObject) {
			JSObject object = (JSObject) obj;
			JSObject config = object.get("config", JSObject.class);
			for (String key: config.keySet()) {
			    props.put(key, "" + config.get(key));
			}
			contextArray = object.get("contexts", JSArray.class);
			for (String key: object.keySet()) {
			    if (!allowedKeys.contains(key)) {
				log.println(errorMsg("ignoringKey", key));
				continue;
			    }
			    if (key.equals("config")) continue;
			    if (key.equals("contexts")) continue;
			    if (remainder == null) {
				remainder = new JSObject();
			    }
			    remainder.put(key, object.get(key));
			}
		    }
		} else {
		    props.load(r);
		}
		r.close();
	        color = props.getProperty("color", color);
		bgcolor = props.getProperty("bgcolor", bgcolor);
		linkColor = props.getProperty("linkColor", linkColor);
		visitedColor = props.getProperty("visitedColor", visitedColor);
		/*
		buttonFGColor = props.getProperty("buttonFGColor",
						  buttonFGColor);
		buttonBGColor = props.getProperty("buttonBGColor",
						  buttonBGColor);
		bquoteBGColor = props.getProperty("bquoteBGColor",
						  bquoteBGColor);
		*/

		log.println("color = " + color);
		log.println("bgcolor = " + bgcolor);
		log.println("linkColor = " + linkColor);
		log.println("visitedColor = " + visitedColor);
		/*
		log.println("buttonFGColor = " + buttonFGColor);
		log.println("buttonBGColor = " + buttonBGColor);
		log.println("bquoteBGColor = " + bquoteBGColor);
		*/
		String s = props.getProperty("ipaddr");
		log.println("ipaddr = " + s);
		if (s != null) s = s.trim();
		if (s == null || s.equalsIgnoreCase("wildcard")) {
		    addr = null;
		} else if (s.equalsIgnoreCase("loopback")) {
		    addr = InetAddress.getLoopbackAddress();
		} else {
		    addr = InetAddress.getByName(s);
		}
		s = props.getProperty("backlog");
		backlog = (s == null)? backlog: Integer.parseInt(s);
		s = props.getProperty("nthreads");
		nthreads = (s == null)? nthreads: Integer.parseInt(s);
		s = props.getProperty("trace");
		trace = (s == null)? trace: Boolean.parseBoolean(s);
		s = props.getProperty("stackTrace");
		stacktrace = (s == null)? stacktrace: Boolean.parseBoolean(s);

		log.println("backlog = " + backlog);
		log.println("nthreads = " + nthreads);
		log.println("trace = " + trace);
		log.println("stackTrace = " + stacktrace);

		// System.out.println("trace = " + trace);
		// System.out.println("stacktrace = " + stacktrace);
		// System.out.flush();

		s = props.getProperty("certificateManager");
		if (s != null) {
		    log.println("certificateManager = " + s);
		    cm = CertManager.newInstance(s.strip());
		    if (cm == null) {
			log.println("... certificatManager not recognized");
		    }
		}

		s = props.getProperty("certMode");
		if (s != null) {
		    s = s.trim();
		    if (s.equals("NORMAL")) {
			CertManager.Mode certMode = CertManager.Mode.NORMAL;
			cm.setMode(certMode);
			log.println("certMode = NORMAL");
		    } else if (s.equals("LOCAL")) {
			CertManager.Mode certMode = CertManager.Mode.LOCAL;
			cm.setMode(certMode);
			log.println("certMode = LOCAL");
		    } else if (s.equals("STAGED")) {
			CertManager.Mode certMode = CertManager.Mode.STAGED;
			cm.setMode(certMode);
			log.println("certMode = STAGED");
		    } else if (s.equals("TEST")) {
			CertManager.Mode certMode = CertManager.Mode.TEST;
			log.println("certMode = TEST");
			cm.setMode(certMode);
		    } else {
			log.println("certMode \"" + s + "\" not recognized"
				    + " - ignored");
		    }
		} else {
		    log.println("certMode = NORMAL");
		}

		s = props.getProperty("alwaysCreate");
		alwaysCreate = (s == null)?
		    alwaysCreate: Boolean.parseBoolean(s);
		log.println("alwaysCreate = " + alwaysCreate);

		s = props.getProperty("keyStoreFile");
		log.println("keyStoreFile = " + s);
		if (s != null) {
		    Path path = Path.of(s);
		    if (path.isAbsolute()) {
			keyStoreFile = path.toFile();
		    } else {
			keyStoreFile = dir.toPath().resolve(path).toFile();
		    }
		    if (cm == null) {
			if (keyStoreFile.isDirectory()) {
			    keyStoreFile = null;
			    log.println(errorMsg("keystoreIsDir"));
			} else if (!keyStoreFile.canRead()) {
			    keyStoreFile = null;
			    log.println(errorMsg("keystoreNotReadable"));
			}
		    }
		}
		s = props.getProperty("trustStoreFile");
		log.println("trustStoreFile = " + s);
		if (s != null) {
		    Path path = Path.of(s);
		    if (path.isAbsolute()) {
			trustStoreFile = path.toFile();
		    } else {
			trustStoreFile = dir.toPath().resolve(path).toFile();
		    }
		    if (trustStoreFile.isDirectory()) {
			log.println(errorMsg("truststoreIsDir"));
			log.println(errorMsg("truststoreIgnored"));
			trustStoreFile = null;
		    } else if (!trustStoreFile.canRead()) {
			log.println(errorMsg("truststoreNotReadable"));
			log.println(errorMsg("truststoreIgnored"));
			trustStoreFile = null;
		    }
		}
		
		s = props.getProperty("sslType");
		if (s == null || (s.trim().length() == 0)
		    || s.trim().equals("null")) {
		    keyStoreFile = null;
		} else {
		    sslType = s.trim().toUpperCase();
		}
		log.println("sslType = " + sslType);

		s = props.getProperty("port");
		if (s == null) {
		    port = (sslType == null)? 80: 443;
		} else {
		    port = Integer.parseInt(s);
		}
		log.println("port = " + port);

		s = props.getProperty("helperPort");
		if (s == null) {
		    helperPort = (sslType == null)? 0: 80;
		} else {
		    helperPort = Integer.parseInt(s);
		    setHelperPort = true;
		}
		log.println("helperPort = " + helperPort);

		s = props.getProperty("allowLoopback");
		if (s != null && s.trim().equalsIgnoreCase("true")) {
		    loopback = true;
		}
		log.println("allowLoopback = " + loopback);
		s = props.getProperty("allowSelfSigned");
		if (s != null && s.trim().equalsIgnoreCase("true")) {
		    selfsigned = true;
		}
		log.println("allowSelfSigned = " + selfsigned);

		s = props.getProperty("keyStorePassword");
		keyStorePW = (s== null || keyStoreFile == null)? null:
		    s.toCharArray();
		log.println("keyStorePassword = "
			    + ((keyStorePW == null)? "null": REDACTED));

		s = props.getProperty("keyPassword");
		keyPW = (s == null)? keyStorePW: s.toCharArray();
		log.println("keyPassword = "
			    + ((keyPW == null)? "null": REDACTED));

		s = props.getProperty("trustStorePassword");
		trustStorePW = (s== null || trustStoreFile == null)? null:
		    s.toCharArray();
		log.println("trustStorePassword = "
			    + ((trustStorePW == null)? "null": REDACTED));

		certName = props.getProperty("certName", "docsig");
		domain = props.getProperty("domain");
		email = props.getProperty("email");
		s = props.getProperty("timeOffset");
		timeOffset = (s == null)? 0: Integer.parseInt(s);
		s = props.getProperty("interval");
		interval = (s == null)? 90: Integer.parseInt(s);
		s = props.getProperty("stopDelay");
		stopDelay = (s == null)? 5: Integer.parseInt(s);


		for (String key: props.stringPropertyNames()) {
		    if (!allowedNames.contains(key)) {
			log.println(errorMsg("badConfigProp", key));
		    }
		}
		if (cm != null && domain != null) {
		    for (String nm: cmPropertyNames) {
			String value = props.getProperty(nm);
			if (value == null) {
			    log.println(errorMsg("missingPropName", nm));
			}
		    }
		}
	    } else {
		log.println(errorMsg("canNotReadConfig", configFile));
		// System.out.println("propFile is not readable");
		// System.out.flush();
	    }
	} else {
	    throw new NullPointerException(errorMsg("nullConfigFile"));
	}
	try {
	    if (sslType != null) {
		TrustManager[] tms = null;
		if (loopback) {
		    SSLUtilities.allowLoopbackHostname();
		}
		if (trustStoreFile != null && trustStorePW == null) {
		    log.println(errorMsg("noTrustStorePW"));
		    // We can't use a trust store without a password,
		    // so set the trust store file to null
		    trustStoreFile = null;
		}
		if (selfsigned) {
		    tms = SSLUtilities
			.installTrustManager(sslType,
					     trustStoreFile, trustStorePW,
					     (cert) -> {return true;});
		} else if (trustStoreFile != null) {
		    tms = SSLUtilities
			.installTrustManager(sslType,
					     trustStoreFile, trustStorePW,
					     (cert) -> {return false;});
		}
		if (cm != null && domain != null) {
		    cm.setProtocol(sslType)
			.setInterval(interval)
			.setStopDelay(stopDelay)
			.setTimeOffset(timeOffset)
			.setCertName(certName)
			.setDomain(domain)
			.setKeystoreFile(keyStoreFile)
		        .setKeystorePW(keyStorePW)
			.setKeyPW(keyPW);

		    if (tms != null) {
			cm.setTrustManagers(tms);
		    } else if (trustStoreFile != null) {
			if (trustStorePW != null) {
			cm.setTruststoreFile(trustStoreFile)
			    .setTruststorePW(trustStorePW);
			} else {
			    log.println(errorMsg("noTrustStorePW"));
			}
		    }

		    if (email != null) cm.setEmail(email);
		    if (alwaysCreate) cm.alwaysCreate(alwaysCreate);
		    int hport = cm.helperPort();
		    int hhport = (hport == 0)? helperPort: hport;
		    if (helperPort != 0 && setHelperPort
			&& hhport != helperPort) {
			String msg =
			    errorMsg("helperConflict", helperPort, hport);
			log.println(msg);
		    }
		    helper = (hhport == 0)? null: new EmbeddedWebServer(hhport);
		    if (helper != null) {
			helper.add("/", RedirectWebMap.class,
				   "https://" + domain + ":" + port +"/",
				   null, true, false, true);
			if (hport != 0) {
			    cm.setHelper(helper);
			} else {
			    needHelperStart = (hhport != 0);
			}
		    }
		    // Don't need a trust store because no client
		    // authentication.
		} else {
		    if (domain == null) {
			log.println(errorMsg("noDomainForCM"));
			log.println(errorMsg("ignoringCM"));
		    }
		    cm = null;
		}
		sslSetup = (cm == null)?
		    new EmbeddedWebServer.SSLSetup(sslType): null;
		if (sslSetup != null && helperPort != 0) {
		    if (helperPort != port) {
			helper = new EmbeddedWebServer(helperPort);
			if (domain == null) domain = "localhost";
			helper.add("/", RedirectWebMap.class,
				   "https://" + domain + ":" + port +"/",
				   null, true, false, true);
			needHelperStart = true;
		    } else {
			log.println(errorMsg("httpPorts"));
		    }
		}
		if (cm == null && keyStoreFile != null && keyStorePW != null) {
		    // https://blog.syone.com/how-to-build-a-java-keystore-alias-with-a-complete-certificate-chain
		    // indicates how to set up a keystore so it contains
		    // the full certificate chain.
		    // Also see
		    // https://serverfault.com/questions/483465/import-of-pem-certificate-chain-and-key-to-java-keystore

		    sslSetup.keystore(new FileInputStream(keyStoreFile))
			.keystorePassword(keyStorePW);
		}
		if (cm == null && trustStoreFile != null
		    && trustStorePW != null) {
		    sslSetup.truststore(new FileInputStream(trustStoreFile))
			.truststorePassword(trustStorePW);
		    
		}
		if (cm == null && tms != null) {
		    sslSetup.trustManagers(tms);
		}
	    } else {
		// if sslType is null, we are using HTTP so any
		// certificate manager should be ignored.
		cm = null;
	    }
	    keyStorePW = null;
	    trustStorePW = null;

	    ews = (cm == null)?
		new EmbeddedWebServer(addr, port, backlog, nthreads, sslSetup):
		new EmbeddedWebServer(addr, port, backlog, nthreads, cm);

	    ews.setRootColors(color, bgcolor, linkColor, visitedColor);

	    log.println("trace = " + trace);
	    log.println("stacktrace = " + stacktrace);
	    if (trace) {
		ews.setTracer("/", log, false);
		/*
		ews.setTracer("/docsig/", log, stacktrace);
		ews.setTracer("/bzdev-api/", log,  false);
		ews.setTracer("/api/", log, false);
		ews.setTracer("/jars/", log, false);
		*/
	    }
	    addContexts();
	} catch (Exception ex) {
		log.println(errorMsg("serverException"));
	    ex.printStackTrace(log);
	    if (ex.getCause() != null) {
		Throwable t = ex;
		while ((t = t.getCause()) != null) {
		    log.println("----");
		    t.printStackTrace(log);
		}
		log.println("-------------");
		log.flush();
		// System.exit(1);
		throw ex;
	    }
	    log.flush();
	}
    }

    /**
     * Start the web server.
     * @throws Exception if an error occurs
     */
    public void start() throws Exception {
	try {
	    ews.start();
	    String scheme = ews.usesHTTPS()? "HTTPS": "HTTP";
	    log.println(errorMsg("ewsStart", ews.getPort(), scheme));
	    if (needHelperStart) {
		// In this case, the certificate manager does not
		// use the helper and start it, so we'll start the
		// helper manually as it will map HTTP requests
		// to HTTPS requests.
		helper.start();
		log.println(errorMsg("helperStart", helper.getPort()));
		needHelperStart = false;
	    }
	} catch (Exception ex) {
				 
	    log.println(errorMsg("serverException"));
	    ex.printStackTrace(log);
	    if (ex.getCause() != null) {
		Throwable t = ex;
		while ((t = t.getCause()) != null) {
		    log.println("----");
		    t.printStackTrace(log);
		}
		log.println("-------------");
		log.flush();
		// System.exit(1);
		throw ex;
	    }
	    log.flush();
	}
    }

    /**
     * Stop the web server.
     * A call to this method will block until the server stops.  As soon
     * as this method is called, the server will stop accepting new
     * requests.  Processing of existing requests will continue for the
     * time interval specified by the argument.  Once the server stops,
     * The server's thread pool will be shut down in case the server
     * fails to do that.
     * <P>
     * IF there is a 'helper' server, the helper will not be stopped.
     * @param delay the delay in seconds before the server is stopped.
     * @exception Exception if an error occurred
     */
    public void stop(int delay) throws Exception {
	ews.stop(delay);
    }

    /**
     * Shutdown the web server.
     * A call to this method will block until the server stops and shuts down.
     * This differs from the method <code>stop</code> in that the server
     * cannot be restarted.
     * <P>
     * A 'helper' server will also be shut down.
     * @param delay the delay in seconds before the server is stopped.
     */
    public void shutdown(int delay) {
	ews.shutdown(delay);
    }
}

//  LocalWords:  exbundle HTTPS CertManager HREF YAML SSL PARMS rgb
//  LocalWords:  bgcolor linkColor visitedColor keyStoreFile apos DNS
//  LocalWords:  keyStorePassword keystore keyPassword trustStoreFile
//  LocalWords:  trustStorePassword allowLoopback loopback DOCSIG TLS
//  LocalWords:  allowSelfSigned certificateManager wtzbzdev docsig
//  LocalWords:  AcmeClient AAAA certMode certName timeOffset sslType
//  LocalWords:  stopDelay ipaddr TCP helperPort html nthreads yml
//  LocalWords:  stackTrace yaml YML BLOCKQUOTE PRE config className
//  LocalWords:  CLASSNAME arg useHTTP propertyNames nowebxml WebMap
//  LocalWords:  displayDir hideWebInf ConfigurableWS addContext xml
//  LocalWords:  EmbeddedWebSever HttpMethod boolean DirWebMap UTF
//  LocalWords:  RedirectWebMap ResourceWebMap ZipWebMap servlet JVM
//  LocalWords:  ServletWebMap EmbeddedWebServer ServletAdapter svr
//  LocalWords:  extensibility defaultTrace defaultStacktrace logFile
//  LocalWords:  getServer subclasses badMethod allowsQuery JSObject
//  LocalWords:  cannotConfig configFile getProperties getRemainder
//  LocalWords:  additionalPropertyNames additionalKeys buttonFGColor
//  LocalWords:  buttonBGColor bquoteBGColor cdir getProperty dir ews
//  LocalWords:  uadir argv noConfigFile confFile propFile println
//  LocalWords:  ignoringKey stacktrace certificatManager ignoringCM
//  LocalWords:  keystoreIsDir keystoreNotReadable truststoreIsDir
//  LocalWords:  truststoreIgnored truststoreNotReadable localhost
//  LocalWords:  badConfigProp missingPropName canNotReadConfig bzdev
//  LocalWords:  nullConfigFile noTrustStorePW helperConflict api
//  LocalWords:  noDomainForCM httpPorts setTracer serverException
//  LocalWords:  ewsStart helperStart
