package org.bzdev.ejws;
import org.bzdev.lang.UnexpectedExceptionError;
import org.bzdev.lang.CallableArgsReturns;
import org.bzdev.net.HttpSessionOps;
import org.bzdev.util.SafeFormatter;

import com.sun.net.httpserver.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.SSLSessionContext;
import org.xml.sax.SAXException;

//@exbundle org.bzdev.ejws.lpack.EmbeddedWebServer

/**
 * Webserver for embedding in applications and stand-alone use.
 * Java provides a package com.sun.net.httpserver that provides
 * an API for building embedded HTTP and HTTPS servers.
 * The class EmbeddedWebServer configures an HttpServer or HttpsServer
 * (from the com.sun.net.httpserver package) and allows this server to
 * be started, stopped, or shut down.  The EmbeddedWebServer
 * constructors provide
 * <UL>
 *  <LI> an optional IP address (the default is the wildcard address)
 *  <LI> a TCP port number.
 *  <LI> a TCP backlog.
 *  <LI> the number of threads the server may use to handle requests. The
 *       server uses a fixed thread pool because embedded web servers are
 *       typically lightly loaded, so it is better to keep the configuration
 *       simple.
 *  <LI> An instance of {@link EmbeddedWebServer.SSLSetup}. When null, the
 *       server will use HTTP and when non-null, the server will run
 *       HTTPS. While there are defaults (suitable for testing), one
 *       can provide a number of SSL options explicitly. Typically, one
 *       will need a keystore containing a certificate. To create one
 *       using <STRONG>keytool</STRONG> run it with options similar to
 *       the following:
 *       <BLOCKQUOTE><PRE><CODE>
 * keytool -genkey -keyalg EC -groupname secp256r1 \
 *         -sigalg SHA256withECDSA -keystore ks.jks \
 *         -keypass changeit -storepass changeit \
 *         -dname CN=HOST -alias NAME -validity 365
 *       </CODE></PRE></BLOCKQUOTE>
 *       to import this certificate into a trust store, run a command
 *       similar to the following:
 *       <BLOCKQUOTE><PRE><CODE>
 * keytool -keystore ks.jks -alias thelio -exportcert \
 *         -storepass changeit -rfc \
 *   | keytool -importcert -alias NAME  -keystore ts.jks \
 *             -keypass changeit -storepass changeit -noprompt
 *       </CODE></PRE></BLOCKQUOTE>
 *       Generally for testing, one will need to provide both a
 *       keystore and a trust store (which is not needed if the
 *       certificate was signed by a certificate authority).
 * </UL>
 * <P>
 * After an EmbeddedWebServer is constructed, handlers for HTTP
 * requests have to be added to the server.  This is done by creating
 * a "context" that that contains an optional authenticator and an
 * {@link com.sun.net.httpserver.HttpHandler} that is responsible for processing
 * requests. This context is in turn associated with a
 * prefix&mdash;the initial portion of the path component of a URL or
 * URI. A specific HTTP handler, {@link FileHandler} is part of the
 * current package and takes care of header processing associated with
 * retrieving static resources (ones that do not change with time and
 * do not require a URL query component).  {@link FileHandler} is
 * controlled by an instance of the class {@link WebMap}, and All
 * subclasses of {@link WebMap} are expected to support a
 * single-argument constructor that will be used for initialization,
 * as described in the documentation for {@link WebMap}.
 * <P>
 * Creating contexts and adding them to an EmbeddedWebServer is handled by
 * the following methods:
 * <ul>
 *   <li>{@link EmbeddedWebServer#add(String,Class,Object,com.sun.net.httpserver.Authenticator,boolean,boolean,boolean)},
 *       which creates an instance of {@link org.bzdev.ejws.FileHandler},
 *       specifying a subclass of {@link WebMap} by class.
 *   <li>{@link EmbeddedWebServer#add(String,String,Object,com.sun.net.httpserver.Authenticator,boolean,boolean,boolean)},
 *       which creates an instance of {@link FileHandler}, specifying
 *       a subclass of {@link WebMap} by class name.
 *   <li>{@link EmbeddedWebServer#add(String,com.sun.net.httpserver.HttpHandler,com.sun.net.httpserver.Authenticator)},
 *       whose arguments contain an instance of {@link com.sun.net.httpserver.HttpHandler}.
 * </ul>
 * <P>
 * When a URL is processed, the handler with the longest matching prefix is
 * used.
 * <P>
 * For the simplest cases&mdash;just displaying static web pages&mdash;one can
 * configure a server by just using the "add" methods that take a WebMap's
 * class name or class as an argument. The argument passed to WebMap's
 * constructor (the Object following the class name or class) depends on the
 * type of WebMap.  For those in the BZDev package, these arguments are as
 * follows:
 * <UL>

 *   <LI>for the class <code>org.bzdev.ejws.maps.DirWebMap</code>, the
 *       argument used to create the web map is a {@link java.io.File}
 *       representing a directory.  The file handler will look for a
 *       file in that directory or a subdirectory with a name
 *       consisting of a requested URL's path with the prefix removed.
 *   <LI>for the class
 *       <code>org.bzdev.ejws.maps.RedirectWebMap</code>, the argument
 *       used to create the web map can be either a URL or a String.
 *       The file handler will provide an HTTP redirect to the URL
 *       obtained by resolving the argument against a requested URL's
 *       path with the prefix removed.
 *   <LI>for the class
 *       <code>org.bzdev.ejws.maps.ResourceWebMap</code>, the argument
 *       is a String representing the initial portion of a a resource
 *       name. The file handler will read a system resource whose path
 *       is the argument, followed by a requested URL's path with the
 *       prefix removed.
 *   <LI>for the class <code>org.bzdev.ejws.maps.URLWebMap</code>, the
 *       argument is either a URL, a String, or an instance of
 *       URLWebMap.Params.The file handler will resolve the requested
 *       path, excluding the prefix, against this URL and use the
 *       resulting URL to find the object to return.  This is similar
 *       to a redirect, except that the EmbeddedWebServer will return
 *       the object to the entity requesting it.
 *   <LI>for the class <code>org.bzdev.ejws.maps.ZipWebMap</code>, the
 *      argument is either a String giving a file name or a File.  The
 *      file must be either a ZIP file, WAR file, or JAR file.  The
 *      file handler will provide an entry whose name is the path of the
 *      requested URL with the prefix removed from that path.
 * </UL>
 * <P>
 * Finally, the method {@link EmbeddedWebServer#start()} will start
 * the web server and the methods {@link EmbeddedWebServer#stop(int)}
 * or {@link EmbeddedWebServer#shutdown(int)} will stop the server
 * calling shutdown will prevent the server from being
 * restarted&mdash;instead a new server will have to be created.
 * <P>
 * If the port number provided to a constructor is zero, the system will
 * allocate a port number, choosing one that is not currently in use.
 * This is useful for applications that will automatically open a browser
 * but where the web pages displayed are not intended for public use
 * (i.e., others would generally not be interested in seeing those pages).
 * Setting the port to zero does not provide privacy - it just avoids
 * conflicts between applications that might inadvertently choose the same
 * port number. For example, an application with an embedded manual might
 * bring up a browser to display the manual, using all the
 * features of a web browser, including javascript.
 * <P>
 * Warning: For HTTPS one needs a large pool size because of what
 * appears to be a bug in the openjdk-11 SSL implementation that leads
 * to an ever increasing number of connections in the TCP CLOSE-WAIT
 * state. This happens sporadically, and when it does, the number of
 * available threads drops.  Until this bug is fixed, the class
 * {org.bzdev.net.CloseWaitService} can be used to remove dormant
 * connections in the CLOSE-WAIT state. This command, however, uses
 * the program <CODE>ss</CODE>, which appears to be available only on
 * Linux systems. For non-Linux systems, EmbeddedWebServer using HTTPS
 * should be used only for testing.  The bug does not seem to occur
 * with HTTP, which seems to rule out a bug in the ejws package.
 */

public class EmbeddedWebServer {

    static class TraceFilter extends Filter {
	public TraceFilter() {}
	// implements the HTTP TRACE method.
	public String description() {return "provide HTTP TRACE";}

	public void doFilter(HttpExchange exch, Filter.Chain chain)
	    throws IOException
	{
	    String method = exch.getRequestMethod().trim();
	    if (method.equalsIgnoreCase("trace")) {
		StringBuilder sb = new StringBuilder();
		sb.append(exch.getProtocol() + " " + method
			  + " " + exch.getRequestURI().toString() + "\r\n");
		Headers headers = exch.getRequestHeaders();
		for (String name: headers.keySet()) {
		    for (String line: headers.get(name)) {
			sb.append(name + ": " + line + "\r\n");
		    }
		}
		byte[] response = null;
		try {
		    response = sb.toString().getBytes("US-ASCII");
		} catch(UnsupportedEncodingException e) {
		    throw new UnexpectedExceptionError(e);
		}
		InputStream is = exch.getRequestBody();
		try {
		    while(is.read() != -1);
		} catch (Exception e) {}
		headers = exch.getResponseHeaders();
		headers.set("Content-Type", "message/http");
		exch.sendResponseHeaders(200, response.length);
		OutputStream os = exch.getResponseBody();
		os.write(response);
		os.close();
	    } else {
		chain.doFilter(exch);
	    }
	}
    }

    HttpServer server;
    boolean useHTTPS = false;
    // int corePoolSize;
    // int maxPoolSize;
    // int maxQueueSize;
    int backlog;
    int nthreads;
    // int idleTimeout;
    InetAddress addr;
    ExecutorService executorService = null;
    int port;
    boolean serverRunning = false;
    boolean serverStopping = false;
    boolean serverShutdown = false;

    boolean warnIfInUse = false;


    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.EmbeddedWebServer");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    /**
     * Return the port for this web server.
     * This is the port number passed to a constructor unless
     * that number is zero (which indicates that the system will
     * pick a port number to use).
     * @return the port
     */
    public int getPort() {
	return port;
    }

    /**
     * Return the address for this web server.
     * The value returned is a socket address: an internet address
     * combined with a port number. This method is provided because
     * some  constructors do not explicitly provide the address.
     * @return the address
     */
    public InetSocketAddress getAddress() {
	return server.getAddress();
    }

    /**
     * Set warning mode.
     * @param value true if warnings are turned on; false otherwise
     */
    public void setWarningMode(boolean value) {
	warnIfInUse = value;
    }

    String serverName = "EmbeddedWebServer";
    Appendable errout = System.err;

    /**
     * Set warning mode specifying an output.
     * @param value true if warnings are turned on; false otherwise
     * @param serverName a name for this server, used in warnings
     * @param errout an Appendable used to log warnings; null for the default
     */
    public void setWarningMode(boolean value, String serverName,
			       Appendable errout) {
	warnIfInUse = value;
	this.serverName = (serverName == null)? "ejws": serverName;
	this.errout = (errout == null)? System.err: errout;
    }

    /**
     * Configure an HTTPS server.
     * This interface is provided because {@link EmbeddedWebServer},
     * when configured for HTTPS, does not provide direct access to
     * its {@link HttpsServer}, but will create an
     * {@link HttpsConfigurator} and this interface can be used to
     * implement its {@link HttpsConfigurator#configure(HttpsParameters)}
     * method.
     * @see EmbeddedWebServer.SSLSetup
     */
    @FunctionalInterface
    public static interface Configurator {
	/**
	 * Configure an https server.
	 * @param context the {@link SSLContext} used to construct
	 *        an {@link HttpsConfigurator}
	 * @param params the {@link HttpsParameters HttpsParameters}
	 *        used provided as an argument to
	 *        {@link HttpsConfigurator#configure(HttpsParameters)}.
	 */
	void configure(SSLContext context, HttpsParameters params);
    }

    /**
     * Setup parameters for an secure-socket server.
     * The methods return the object created by the constructor so
     * that one can chain the methods together for initialization.
     * The default keystore and password are stored as a resource in
     * the EJWS jar file.  When this is used, there is a corresponding
     * truststore in a file named ejwsCerts.jks. On Linux systems, this
     * should be in the directory /usr/share/bzdev although its actual
     * location may be system dependent.  This truststore's password is
     * "changeit". As it is in a directory owned by root, and is intended
     * only for testing, this password should not be changed.
     * ejwsCerts.jks is needed because without explicitly providing a
     * keystore containing the server's certificate, a self-signed
     * certificate will be used (a common practice during testing).
     */
    public static class SSLSetup {
	private static final char[] defaultpw = "changeit".toCharArray();
	String protocol = "SSL";
	InputStream ksis = null;
	char[] kspw = defaultpw;
	char[] kepw = defaultpw;
	InputStream tsis = null;
	char[] tspw = defaultpw;
	Configurator configurator = (sslContext, HttpsParams) ->{};

	/**
	 * Constructor using a default protocol.
	 */
	public SSLSetup() {}

	/**
	 * Constructor
	 * @param protocol the secure-socket protocol (for example,
	 *        "SSL" or "TSL")
	 */
	public SSLSetup(String protocol) {
	    this.protocol = protocol;
	}
	/**
	 * Provide an input stream for a keystore
	 * @param is an input stream used to load the keystore
	 * @return this object
	 */
	public SSLSetup keystore(InputStream is) {
	    this.ksis = is;
	    return this;
	}

	/**
	 * Provide an input stream for a truststore
	 * @param is an input stream used to load the truststore
	 * @return this object
	 */
	public SSLSetup truststore(InputStream is) {
	    this.tsis = is;
	    return this;
	}

	/**
	 * Provide a password for modifying/reading a keystore
	 * @param pw the password (as a char array)
	 * @return this object
	 */
	public SSLSetup keystorePassword(char[] pw) {
	    this.kspw = pw;
	    return this;
	}

	/**
	 * Provide a password for using a key in the keystore
	 * @param pw the password (as a char array)
	 * @return this object
	 */
	public SSLSetup keyPassword(char[] pw) {
	    this.kepw  = pw;
	    return this;
	}

	/**
	 * Provide a password for modifying/reading a truststore
	 * @param pw the password (as a char array)
	 * @return this object
	 */
	public SSLSetup truststorePassword(char[] pw) {
	    this.tspw = pw;
	    return this;
	}

	/**
	 * Provide a configurator.
	 * Configurators are instance of the functional interface
	 * {@link EmbeddedWebServer.Configurator}, and will typically be
	 * provided by a lambda expression that takes two arguments:
	 * an instance of {@link javax.net.ssl.SSLContext} and an instance
	 * of {@link com.sun.net.httpserver.HttpsParameters}. These two
	 * arguments are provided by the
	 * {@link com.sun.net.httpserver.HttpsServer} implementation.
	 * @param configurator the configurator
	 * @return an instance of {@link EmbeddedWebServer.SSLSetup},
	 *         encapsulating SSL parameters
	 * @see com.sun.net.httpserver.HttpsConfigurator
	 */
	public SSLSetup configurator(Configurator configurator) {
	    this.configurator = configurator;
	    return this;
	}
    }

    HttpsConfigurator httpsConfigurator = null;

    KeyStore ksForCerts = null;

    private Certificate[] getCertificatesAux()
	throws KeyStoreException
    {
	Iterator<String> aliases = ksForCerts.aliases().asIterator();
	ArrayList<Certificate> alist = new ArrayList<>();
	while (aliases.hasNext()) {
	    String alias = aliases.next();
	    try {
		if (ksForCerts.isKeyEntry(alias)) {
		    Certificate cert = ksForCerts.getCertificate(alias);
		    if (cert != null) {
			alist.add(cert);
		    }
		}
	    } catch (Exception e) {}
	}
	Certificate[] certificates = new Certificate[alist.size()];
	alist.toArray(certificates);
	return certificates;
    }

    /**
     * Get Certificates.
     * This will return null for an HTTP server and an array of
     * certificates for HTTPS.  The certificates returned will be
     * those with private keys and any such key is assumed to be
     * possibly used as a server certificate.
     * @param aliases the names of the aliases; no arguments for all
     *        certificates
     * @return an array containing certificates; null if not applicable

     */
    public Certificate[] getCertificates(String... aliases) {
	if (ksForCerts == null) return null;
	if (aliases.length == 0) {
	    try {
		return getCertificatesAux();
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    Certificate[] certificates = new Certificate[aliases.length];
	    for (int i = 0; i < aliases.length; i++) {
		try {
		    certificates[i] = ksForCerts.getCertificate(aliases[i]);
		} catch (Exception e) {
		    certificates[i] = null;
		}
	    }
	    return certificates;
	}
    }

    private void setupServer(SSLSetup s /*
                             InputStream ksis, char[] ksp,w char[] kepw,
			     InputStream tsis, char[] tspw,
			     InputStream crlis,
			     final Configurator configurator*/)
	throws IOException, KeyStoreException, CRLException,
	       NoSuchAlgorithmException, CertificateException,
	       KeyManagementException, UnrecoverableKeyException,
	       InvalidAlgorithmParameterException, BindException
    {
	server = useHTTPS?
	    HttpsServer.create(new InetSocketAddress(addr, port), backlog):
	    HttpServer.create(new InetSocketAddress(addr, port), backlog);
	if (port == 0) {
	    // we've asked the server to allocate the port, so we'll set
	    // the port to the one actually used.
	    port = server.getAddress().getPort();
	}
	if (server instanceof HttpsServer) {
	    KeyStore ks = KeyStore.getInstance("JKS");
	    // char[] passphrase = "changeit".toCharArray();
	    if (s.ksis == null) {
		s.ksis = ClassLoader.getSystemResourceAsStream
		    ("org/bzdev/ejws/defaultCerts");
	    }
	    ks.load(s.ksis, s.kspw);
	    ksForCerts = ks;
	    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	    kmf.init(ks, s.kepw);
	    TrustManagerFactory tmf = null;
	    final SSLContext sslContext = SSLContext.getInstance(s.protocol);
	    if (s.tsis != null) {
		tmf = TrustManagerFactory.getInstance("SUNX509");
		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(s.tsis, s.tspw);
		tmf = TrustManagerFactory.getInstance("SUNX509");
		tmf.init(ts);
	    }
	    sslContext.init(kmf.getKeyManagers(),
			    ((tmf == null)? null: tmf.getTrustManagers()),
			    null);
	    HttpsServer sserver = (HttpsServer) server;
	    final Configurator conf = s.configurator;
	    httpsConfigurator = new HttpsConfigurator(sslContext) {
			public void configure(HttpsParameters params) {
			    if (conf != null) {
				conf.configure(sslContext, params);
			    } else {
				super.configure(params);
			    }
			}
		};
	    sserver.setHttpsConfigurator(httpsConfigurator);
	    // System.out.println("HTTPS server configured");
	}
    }

    // used when stop(int) is called. We create a new server.
    private void setupServer() {
	try {
	    server = useHTTPS?
		HttpsServer.create(new InetSocketAddress(addr, port), backlog):
		HttpServer.create(new InetSocketAddress(addr, port), backlog);
	    if (server instanceof HttpsServer) {
		HttpsServer sserver = (HttpsServer) server;
		sserver.setHttpsConfigurator(httpsConfigurator);
	    }
	    if (serverStopping) {
		for (Map.Entry<String,PrefixData> entry: prefixMap.entrySet()) {
		    String p = entry.getKey();
		    PrefixData data = entry.getValue();
		    doAdd(p, data);
		}
	    }
	} catch (IOException eio) {
	    try {
		errout.append(errorMsg("restartFailed", eio.getMessage()));
	    } catch (Exception e) {}
	}
    }

    static final private int DEFAULT_BACKLOG = 32;

    /*
     * Estimate the number of threads to support a given backlog
     */
    static int getNumberOfPoolThreads(int backlog) {
	int max = Runtime.getRuntime().availableProcessors();
	int result = 0;
	while (backlog > 0 && result < max) {
	    backlog /= 8;
	    result++;
	}
	if (result == 0) result = 1;
	return result;
    }

    /**
     * Constructor for a wildcard IP address with a default backlog and
     * a default number of threads.
     * @param port the TCP port number for a server; 0 for a system-allocated
     *             port
     * @param sslSetup the configuration for an HTTPS server; null for HTTP
     * @exception Exception an error occurred.
     */
    public EmbeddedWebServer(int port, SSLSetup sslSetup)
	throws Exception
    {
	this(port, DEFAULT_BACKLOG, getNumberOfPoolThreads(DEFAULT_BACKLOG),
	     sslSetup);
    }

    /**
     * Constructor for a wildcard IP address with a default number of threads.
     * @param port the TCP port number for a server; 0 for a system-allocated
     *             port
     * @param backlog the TCP backlog (maximum number of pending connections)
     * @param sslSetup the configuration for an HTTPS server; null for HTTP
     * @exception Exception an error occurred.
     */
    public EmbeddedWebServer(int port, int backlog, SSLSetup sslSetup)
	throws Exception
    {
	this(port, backlog, getNumberOfPoolThreads(backlog), sslSetup);
    }


    /**
     * Constructor for a wildcard IP address.
     * @param port the TCP port number for a server; 0 for a system-allocated
     *             port
     * @param backlog the TCP backlog (maximum number of pending connections)
     * @param nthreads the number of threads the server will use
     * @param sslSetup the configuration for an HTTPS server; null for HTTP
     * @exception Exception an error occurred
     */
    public EmbeddedWebServer(int port, int backlog, int nthreads,
			     SSLSetup sslSetup)
	throws Exception
    {
	this(null, port, backlog, nthreads, sslSetup);
    }


    /**
     * Constructor.
     * @param addr the Internet address for this server; null for the
     *        wildcard address
     * @param port the TCP port number for a server; 0 for a system-allocated
     *             port
     * @param backlog the TCP backlog (maximum number of pending connections)
     * @param nthreads the number of threads the server will use
     * @param sslSetup the configuration for an HTTPS server; null for HTTP
     */
    public EmbeddedWebServer(InetAddress addr,
			     int port, int backlog, int nthreads,
			     SSLSetup sslSetup)
    {
	this.addr = addr;
	this.port = port;
	this.backlog = backlog;
	this.nthreads = nthreads;
	this.useHTTPS = (sslSetup != null);
	createAddressList();
	try {
	    setupServer(sslSetup);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new UnexpectedExceptionError(e);
	}
    }

    private List<InetAddress> addressList = null;

    private void createAddressList() {
	if (addr == null || addr.isAnyLocalAddress()) {
	    LinkedList<InetAddress> list = new LinkedList<>();
	    try {
		Iterator<NetworkInterface> it1 =
		    NetworkInterface.getNetworkInterfaces().asIterator();
		while (it1.hasNext()) {
		    Iterator<InetAddress> it2 =
			it1.next().getInetAddresses().asIterator();
		    while (it2.hasNext()) {
			list.add(it2.next());
		    }
		}
	    } catch (Exception e) {
		try {
		    err.append("cannot create network address list\n");
		} catch (Exception ee) {}
	    }
	    addressList = list;
	} else {
	    addressList = List.of(addr);
	}
    }

    /**
     * Get a list of the Internet addresses that this embedded web server
     * uses.
     * <P>
     * The list can have more than one entry when the server is
     * configured using a wildcard address.
     * @return a list of addresses
     */
    public List<InetAddress> getAddresses() {
	return Collections.unmodifiableList(addressList);
    }

    /**
     * Determine if an address and port is one associated with this
     * server.
     * @param addr the internet address
     * @param port the port
     * @return true if the arguments provide an address and port that this
     *         server uses; false otherwise
     */
    public boolean isServerAddressAndPort(InetAddress addr, int port) {
	if (port == this.port) {
	    for (InetAddress a: getAddresses()) {
		if (a.equals(addr)) {
		    return true;
		}
	    }
	}
	return false;
    }


    // HashSet<String> prefixSet = new HashSet<String>();

    static class PrefixData {
	HttpHandler handler;
	com.sun.net.httpserver.Authenticator authenticator;
	HttpContext context = null;
	EjwsSessionMgr sessionMgr = null;
	PrefixData(HttpHandler h, com.sun.net.httpserver.Authenticator a) {
	    handler = h;
	    authenticator = a;
	}
    }

    HashMap<String,PrefixData> prefixMap = new HashMap<String,PrefixData>();

    /**
     * Add a session manager for a path.
     * A session implementation is an application-specific object
     * associated with a session, typically used to maintain the state
     * of a session.
     * <P>
     * The method {@link WebMap.RequestInfo#setSessionState(Object)}, which
     * implements
     * {@link org.bzdev.net.HttpServerRequest#setSessionState(Object)}, can
     * be used to set the session state. Similarly, the method
     * {@link WebMap.RequestInfo#getSessionState()}, which implements
     * {@link org.bzdev.net.HttpServerRequest#getSessionState()}, can be
     * used to fetch the sessions state.
     * <P>
     * {@link EmbeddedWebServer} does not provide any way of
     * deallocating resources once a state is no longer in use. The
     * class {@link java.lang.ref.Cleaner} (the API documentation for
     * this class includes an example) can be used to deallocate
     * resources or to perform some other action when a session is
     * removed.
     * @param path a path that has been added to this server
     * @param withState true if a session has a state; false otherwise
     * @return true on success; false otherwise (for example, the path
     *         had not been added to this server or the path does not
     *         have an {@link com.sun.net.httpserver.HttpContext HttpContext})
     */
    public boolean addSessionFilter(String path, boolean withState)  {
	return addSessionFilter(path, withState? new EjwsStateTable(): null);
    }


    /**
     * Add a session-manager filter for a path, specifying an implementation
     * that maps sessions to states.
     * A session implementation is an application-specific object
     * associated with a session, typically used to maintain the state
     * of a session.
     * <P>
     * The method {@link WebMap.RequestInfo#setSessionState(Object)}, which
     * implements
     * {@link org.bzdev.net.HttpServerRequest#setSessionState(Object)}, can
     * be used to set the session state. Similarly, the method
     * {@link WebMap.RequestInfo#getSessionState()}, which implements
     * {@link org.bzdev.net.HttpServerRequest#getSessionState()}, can be
     * used to fetch the sessions state.
     * <P>
     * {@link EjwsStateTable} provides the default implementation. This
     * implementation is suitable for most purposes. Subclassing it might
     * be useful for debugging (e.g., to insert print statements).
     * <P>
     * {@link EmbeddedWebServer} does not provide any way of
     * deallocating resources once a state is no longer in use. The
     * class {@link java.lang.ref.Cleaner} (the API documentation for
     * this class includes an example) can be used to deallocate
     * resources or to perform some other action when a session is
     * removed.
     * @param path a path that has been added to this server
     * @param sessionOps the object that maps a session ID to
     *        session implementations; null if no mapping is desired
     * @return true on success; false otherwise (for example, the path
     *         had not been added to this server or the path does not
     *         have an {@link com.sun.net.httpserver.HttpContext HttpContext})
     * @see org.bzdev.net.HttpSessionOps
     * @see EjwsStateTable
     * @see EmbeddedWebServer#addSessionFilter(String,boolean)
     */
    public boolean addSessionFilter(String path, HttpSessionOps sessionOps)  {
	PrefixData data = prefixMap.get(path);
	if (data == null) return false;
	HttpContext context = data.context;
	if (context == null) return false;
	if (data.sessionMgr == null) {
	    EjwsSessionMgr sm = new EjwsSessionMgr(sessionOps);
	    context.getFilters().add(sm);
	    data.sessionMgr = sm;
	}
	return true;
    }

    /**
     * Add a filter to the HTTP context associated with a path.
     * @param path the path
     * @param filter to the filter
     * @return true if the context exists; false otherwise
     */
    public boolean addFilter(String path, Filter filter) {
	PrefixData data = prefixMap.get(path);
	if (data == null) return false;
	HttpContext context = data.context;
	if (context == null) return false;
	context.getFilters().add(filter);
	return true;
    }


    boolean addedRootImplicitly = false;
    boolean executorSet = false;

    /**
     * Add a context to the server given the name of a WebMap subclass.
     * An instance of the subclass of WebMap will be passed to a new
     * instance of FileHandler and will be created using the
     * argument <code>arg</code> as the sole argument for the
     * WebMap's constructor.  This WebMap instance will
     * determine how paths are mapped into the resource that is to be
     * returned, and will determine properties such as the resource's
     * MIME type.  The context a server uses while handling a request
     * is that starting with the longest matching path, and determines
     * the handler to use.  If HTTP authentication is required, a
     * non-null authenticator must be provided.
     * <P>
     * The use of Web-Inf/web.xml files is described in the documentation
     * for {@link WebMap}.
     * @param p the path for the context; a null path will be treated as "/"
     * @param className the name of a subclass of WebMap, an instance
     *                  of which will be used to create a new FileHandler
     * @param arg the argument used in creating the WebMap instance
     * @param authenticator the authenticator for the context; null if there
     *        is none
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true if the caller wants directories displayed when
     *                possible; false otherwise
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @exception IllegalArgumentException the path is invalid or the path
     *            was already added.
     * @exception IllegalStateException the server has been shut down
     * @exception IOException an IO exception occurred (e.g., while
     *            printing a warning messages)
     * @exception SAXException an error occurred while parsing a web.xml file
     * @see WebMap
     */

    public void add(String p, String className, Object arg,
		    com.sun.net.httpserver.Authenticator authenticator,
		    boolean nowebxml, boolean displayDir, boolean hideWebInf)
	throws IllegalArgumentException, IllegalStateException, IOException,
	       SAXException
    {
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }

	} else {
	    p = "/";
	}
	FileHandler fh = new FileHandler((useHTTPS?"https":"http"),
					 arg, className, nowebxml, displayDir,
					 hideWebInf);
	add(p, fh, authenticator);

    }

    /**
     * Add a context to the server given a subclass of WebMap.
     * An instance of the subclass of WebMap will be passed to a
     * new instance of FileHandler and will be created using the
     * argument <code>arg</code> as the sole argument for the
     * WebMap's constructor.  This WebMap instance will
     * determine how paths are mapped into the resource that is to be
     * returned, and will determine properties such as the resource's
     * MIME type.  The context a server uses while handling a request
     * is that starting with the longest matching path, and determines
     * the handler to use.  If HTTP authentication is required, a
     * non-null authenticator must be provided.
     * <P>
     * The use of Web-Inf/web.xml files is described in the documentation
     * for {@link WebMap}.
     * @param p the path for the context
     * @param clazz a subclass of WebMap, an instance of which will be
     *              used to create a new FileHandler
     * @param arg the argument used in creating the WebMap instance
     * @param authenticator the authenticator for the context; null if there
     *                      is none
     * @param nowebxml true if a web.xml file should be ignored;
     *                 false otherwise
     * @param displayDir true if the caller wants directories displayed when
     *                possible; false otherwise
     * @param hideWebInf true if the WEB-INF directory should be hidden;
     *                   false if it should be visible
     * @exception IllegalArgumentException the path is invalid or the path
     *            was already added.
     * @exception IllegalStateException the server has been shut down
     * @exception IOException an IO exception occurred (e.g., while
     *            printing a warning messages)
     * @exception SAXException an error occurred while parsing a web.xml file
     * @see WebMap
     */
    public void add(String p, Class<? extends WebMap> clazz, Object arg,
		    com.sun.net.httpserver.Authenticator authenticator,
		    boolean nowebxml, boolean displayDir, boolean hideWebInf)
	throws IllegalArgumentException, IllegalStateException, IOException,
	       SAXException
    {
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	FileHandler fh = new
	    FileHandler((useHTTPS?"https":"http"),
			arg, clazz, nowebxml, displayDir, hideWebInf);
	add(p, fh, authenticator);
    }

    /**
     * Add a context to the server.
     * The context a server uses while handling a request is that starting
     * with the longest matching path, and determines the handler to use.
     * If HTTP authentication is required, a non-null authenticator must be
     * provided.  If p does not start with "/", the character "/" will be
     * prepended to it. A "/" will not be appended to the end of the path.
     * @param p the path for the context
     * @param handler the handler for the context
     * @param authenticator the authenticator for the context; null if there
     *        is none
     * @exception IllegalStateException the server has been shut down
     * @exception IllegalArgumentException the path was not valid or
     *            already added
     * @exception IOException an IO exception occurred (e.g., while
     *            printing a warning messages)
     */
    public void add(String p, HttpHandler handler,
		    com.sun.net.httpserver.Authenticator authenticator)
	throws IllegalStateException, IllegalArgumentException, IOException
    {
	if (server == null) {
	    throw new IllegalStateException(errorMsg("serverShutdown"));
	}
	// System.out.println("trying to add " + p);
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	if (p.equals("/") && addedRootImplicitly) {
	    server.removeContext(p);
	    addedRootImplicitly = false;
	}
	String pp = p;
	if (prefixMap.containsKey(pp)) {
	    throw new IllegalArgumentException(errorMsg("pathAlreadyAdded", p));
	}
	if (warnIfInUse) {
	    pp = pp.substring(0, pp.length() - 1);
	    pp = pp.substring(0, pp.lastIndexOf("/") + 1);
	    while(pp.length() > 0) {
		if (prefixMap.containsKey(pp)) {
		    errout.append(errorMsg("containedBy", serverName, p, pp));
		    break;
		}
		pp = pp.substring(0, pp.length() - 1);
		pp = pp.substring(0, pp.lastIndexOf("/") + 1);
	    }
	}
	PrefixData pd = new PrefixData(handler, authenticator);
	prefixMap.put(p, pd);
	if (!serverStopping) {
	    doAdd(p, pd);
	}
    }

    private void doAdd(String p, PrefixData pd)
    {
	HttpContext context = server.createContext(p, pd.handler);
	if (pd.handler instanceof FileHandler) {
	    ((FileHandler)(pd.handler)).setEWS(this);
	}
	context.getFilters().add(new TraceFilter());
	pd.context = context;
	if (pd.authenticator != null) {
	    context.setAuthenticator(pd.authenticator);
	}
    }


    /**
     * Remove a context.
     * @param p the path used when a context was added.
     * @return true if a context exists for path p; false otherwise
     */
    public boolean remove(String p) {
	if (server == null) {
	    throw new IllegalStateException(errorMsg("serverShutdown"));
	}
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	if (prefixMap.containsKey(p)) {
	    HttpHandler handler = getHttpHandler(p);
	    if (handler instanceof FileHandler) {
		((FileHandler) handler).setEWS(null);
	    }
	    WebMap wm = getWebMap(p);
	    server.removeContext(p);
	    // prefixSet.remove(p);
	    prefixMap.remove(p);
	    if (wm != null && wm.isConfigured()) {
		wm.deconfigure();
		wm.setConfigured(false);
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Determine if this web server has added a prefix.
     * A leading and trailing '/' will be added to the prefix if
     * missing.
     * @param p the prefix (null implies the root prefix)
     * @return true if the prefix was added; false otherwise
     */
    public boolean containsPrefix(String p) {
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	return prefixMap.containsKey(p);
    }

    /**
     * Get an HtppHandler if it is an instance of {@link FileHandler}.
     * A leading and/or trailing '/' will be added to the prefix if
     * missing.
     * <P>
     * This method is provided for convenience.
     * @param p the prefix (null implies the root prefix)
     * @return the handler
     */
    public FileHandler getFileHandler(String p) {
	HttpHandler handler = getHttpHandler(p);
	if (handler != null && handler instanceof FileHandler) {
	    return (FileHandler) handler;
	} else {
	    return null;
	}
    }

    /**
     * Get the HttpHandler associated with a prefix.
     * A leading and/or trailing '/' will be added to the prefix if
     * missing.
     * @param p the prefix (null implies the root prefix)
     * @return the handler
     */
    public HttpHandler getHttpHandler(String p) {
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	PrefixData pdata = prefixMap.get(p);
	if (pdata == null) {
	    return null;
	} else {
	    return pdata.handler;
	}
    }

    /**
     * Get the authenticator associated with a prefix.
     * A leading and/or trailing '/' will be added to the prefix if
     * missing.
     * @param p the prefix (null implies the root prefix)
     * @return the authenticator; null if there is none.
     */
    public com.sun.net.httpserver.Authenticator getAuthenticator(String p) {
	if (p != null) {
	    if (!p.startsWith("/")) {
		p = "/" + p;
	    }
	    if (!p.endsWith("/")) {
		p = p + "/";
	    }
	} else {
	    p = "/";
	}
	PrefixData pdata = prefixMap.get(p);
	if (pdata == null) {
	    return null;
	} else {
	    return pdata.authenticator;
	}
    }

    /**
     * Get the WebMap associated with a prefix.
     * A leading and/or trailing '/' will be added to the prefix if
     * missing.
     * @param p the prefix (null implies the root prefix)
     * @return the web map (null if there is none).
     */
    public WebMap getWebMap(String p) {
	HttpHandler handler = getHttpHandler(p);
	if (handler == null) {
	    return null;
	} else if (handler instanceof FileHandler) {
	    return ((FileHandler)handler).map;
	} else {
	    return null;
	}
    }

    /**
     * Set an Appendable for tracing.
     * This method should be used only for debugging.
     * Currently, the only handler that supports tracing is
     * {@link org.bzdev.ejws.FileHandler}.
     * @param p the prefix whose handler should be traced
     * @param tracer the Appendable for tracing requests and responses
     * @return true if the handler for the prefix supports tracing; false
     *         otherwise
     */
    public boolean setTracer(String p, Appendable tracer) {
	HttpHandler handler = getHttpHandler(p);
	if (handler == null) {
	    return false;
	} else if (handler instanceof FileHandler) {
	    ((FileHandler)handler).setTracer(tracer);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Set an Appendable for tracing, optionally with a stack trace for
     * exceptions.
     * This method should be used only for debugging.
     * Currently, the only handler that supports tracing is
     * {@link org.bzdev.ejws.FileHandler}.
     * @param p the prefix whose handler should be traced
     * @param tracer the Appendable for tracing requests and responses
     * @param stacktrace true for a stack trace; false otherwise
     * @return true if the handler for the prefix supports tracing; false
     *         otherwise
     */
    public boolean setTracer(String p, Appendable tracer, boolean stacktrace) {
	HttpHandler handler = getHttpHandler(p);
	if (handler == null) {
	    return false;
	} else if (handler instanceof FileHandler) {
	    ((FileHandler)handler).setTracer(tracer, stacktrace);
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Determine which prefixes have been configured for this web server.
     * @return a set of prefixes
     */
    public Set<String> getPrefixes() {
	return Collections.unmodifiableSet(prefixMap.keySet());
    }

    Appendable err = System.err;

    /**
     * Set the error output for errors that terminate an executor thread.
     * The default is System.err.
     * @param err the error output; null to suppress these messages
     */
    public void setErrorOutput(Appendable err) {
	this.err = err;
    }

    CallableArgsReturns<ExecutorService,Integer> executorServiceFactory
	= null;

    /**
     * Set up an ExecutorService factory.
     * This method is provided so that the user of this class can
     * install a specific {@link ExecutorService}.
     * The argument uses a functional interface, so the factory can be
     * provided as a lambda expression.
     * The argument is an {@link Integer} whose value is the number of
     * threads passed to a constructor of this class.
     * @param callable an object whose 'call' method (with an Integer argument)
     *        creates a new ExecutorService; null for the default
     */
    public void
	setExecutorServiceFactory(CallableArgsReturns<ExecutorService,Integer>
				  callable)
    {
	executorServiceFactory = callable;
    }

    WebMap.ColorSpec colorSpec = new WebMap.ColorSpec() {
	    private final String color = "black";
	    private final String bgcolor = "lightgray";
	    private final String linkColor = null;
	    private final String visitedColor = null;
	    @Override
	    public String getColor() {return color;}

	    @Override
	    public String getBackgroundColor() {return bgcolor;}

	    @Override
	    public String getLinkColor() {return linkColor;}

	    @Override
	    public String getVisitedColor() {return visitedColor;}
	};

    /**
     * Set the colors used by a RootHandler.
     * @param color the CSS color for text
     * @param bgcolor the CSS color for the background
     * @param linkColor the CSS color for links; null to ignore
     * @param visitedColor the CSS color for visited links; null to ignore
     * @throws IllegalArgumentException if color or bgcolor are missing
     *         or if only one of linkColor or visitedColor is null.
     */
    public void setRootColors(final String color, final String bgcolor,
			      final String linkColor, final String visitedColor)
    {
	if (color == null || bgcolor == null) {
	    throw new IllegalArgumentException(errorMsg("nullArgs1or2"));
	}
	if ((linkColor == null || visitedColor == null)
	    && (linkColor != visitedColor)) {
	    throw new IllegalArgumentException(errorMsg("nullArgs3or4"));
	}
	colorSpec = new WebMap.ColorSpec() {
		@Override
		public String getColor() {return color;}

		@Override
		public String getBackgroundColor() {return bgcolor;}

		@Override
		public String getLinkColor() {return linkColor;}

		@Override
		public String getVisitedColor() {return visitedColor;}
	    };
    }

    /**
     * Start the web server.
     * Starting a server that is already running has no effect.
     * @exception IllegalStateException the server is stopping or has been
     *            shut down
     */
    public void start() {

	if (serverRunning) {
	    return;
	}
	if (serverShutdown) {
	    throw new
		IllegalStateException(errorMsg("serverShutdown"));
	}
	if (serverStopping) {
	    throw new
		IllegalStateException(errorMsg("serverStopping"));
	}
	if (!prefixMap.containsKey("/") && !addedRootImplicitly) {
	    RootHandler rhandler = new RootHandler(prefixMap);
	    rhandler.setRootColors(colorSpec);
	    server.createContext("/", rhandler)
		.getFilters().add(new TraceFilter());
	    addedRootImplicitly = true;
	}
	for (String prefix: prefixMap.keySet()) {
	    WebMap wm = getWebMap(prefix);
	    if (!wm.isConfigured()) {
		try {
		    wm.configure();
		    wm.setConfigured(true);
		} catch (Exception e) {
		    System.err.println("could not configure web map");
		    wm.deconfigure();
		    wm.setConfigured(false);
		}
	    }
	}
	// server.setExecutor(null);
	if (!executorSet) {
	    if (executorServiceFactory != null) {
		executorService = executorServiceFactory.call(nthreads);
	    } else {
		executorService = Executors.newFixedThreadPool
		    (nthreads, new ThreadFactory() {
			    public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setUncaughtExceptionHandler((t,e) -> {
					String ct = "" + t.getId();
					try {
					    err.append("(" + ct + ") executor "
						       + "terminating: "
						       + e.getMessage() +"\n");
					    if (err instanceof Flushable) {
						Flushable f = (Flushable)err;
						f.flush();
					    }
					} catch (Exception eio) {}
				    });
				return thread;
			    }
			});
	    }
	    server.setExecutor(executorService);
	    /*
	    LinkedBlockingQueue<Runnable> queue =
		new LinkedBlockingQueue<Runnable>(maxQueueSize);
	    server.setExecutor(new ThreadPoolExecutor(corePoolSize,
						      maxPoolSize,
						      idleTimeout,
						      TimeUnit.SECONDS,
						      queue));
	    */
	    executorSet = true;
	}
	serverRunning = true;
	server.start();
    }


    /**
     * Stop the web server.
     * A call to this method will block until the server stops.  As soon
     * as this method is called, the server will stop accepting new
     * requests.  Processing of existing requests will continue for the
     * time interval specified by the argument.  Once the server stops,
     * The server's thread pool will be shut down in case the server
     * fails to do that.
     * @param delay the delay in seconds before the server is stopped.
     * @exception Exception an error occurred
     */
    public void stop(int delay) throws Exception {
	if (!serverRunning) {
	    throw new
		IllegalStateException(errorMsg("serverNotRunning"));
	}
	if (serverStopping) {
	    throw new
		IllegalStateException(errorMsg("serverAlreadyStopping"));
	}
	boolean needInterrupt = false;
	try {
	    serverStopping = true;
	    server.stop(delay);
	    executorService.shutdownNow();
	    /*
	     * In case shutdownNow fails.
	     */
	    if (!executorService.awaitTermination(1 + (delay/10),
						  TimeUnit.SECONDS)) {
		errout.append(errorMsg("executor", serverName));
	    }
	} catch (InterruptedException ie) {
	    executorService.shutdownNow();
	    needInterrupt = true;
	}
	executorSet = false;
	if (addedRootImplicitly) {
	    server.removeContext("/");
	    addedRootImplicitly = false;
	}
	for (String p: prefixMap.keySet()) {
	    WebMap wm = getWebMap(p);
	    if (wm.isConfigured()) {
		wm.deconfigure();
		wm.setConfigured(false);
	    }
	    server.removeContext(p);
	}

	if (serverShutdown == false) {
	    setupServer();
	}
	serverStopping = false;
	serverRunning = false;
	if (needInterrupt) Thread.currentThread().interrupt();
    }

    /**
     * Shutdown the web server.
     * A call to this method will block until the server stops and shuts down.
     * This differs from the method <code>stop</code> in that the server
     * cannot be restarted.
     * @param delay the delay in seconds before the server is stopped.
     */
    public void shutdown(int delay) {
	if (serverStopping) {
	    throw new
		IllegalStateException(errorMsg("serverAlreadyStopping"));
	}
	// serverStopping = true;
	serverShutdown = true;
	// serverRunning = false;
	server.stop(delay);
	for (String p: prefixMap.keySet()) {
	    WebMap wm = getWebMap(p);
	    if (wm.isConfigured()) {
		wm.deconfigure();
		wm.setConfigured(false);
	    }
	    server.removeContext(p);
	}
	prefixMap.clear();
	prefixMap = null;
	server = null;
    }


    /**
     * Create a new HttpHandler.
     * This method is provided so that new HttpHandler instances can
     * be created from a GUI or a command-line interface.
     * @param className the name of the class of the instance to create
     * @param arg an argument passed to the new instance's constructor
     *        (one argument, of type Object).
     * @return the new HttpHandler
     * @exception IOException an IO exception occurred during initialization
     * @exception IllegalArgumentException one of the arguments was
     *            illegal.
     */
    public static HttpHandler newHttpHandler(String className, Object arg)
	throws IOException, IllegalArgumentException
    {
	try {
	    Class<? extends HttpHandler> clasz =
		Class.forName(className).asSubclass(HttpHandler.class);
	    Constructor<? extends HttpHandler> constructor =
		clasz.getConstructor(Object.class);
	    return constructor.newInstance(arg);
	} catch (InstantiationException e1) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e1);
	} catch (IllegalAccessException e2) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e2);
	} catch (IllegalArgumentException e3) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e3);
	} catch (InvocationTargetException e4) {
	    Throwable e = e4.getTargetException();
	    if (e instanceof IOException) {
		throw (IOException) e;
	    } else if (e instanceof IllegalArgumentException) {
		throw (IllegalArgumentException) e;
	    } else {
		String msg = errorMsg("illegalArgument");
		throw new IllegalArgumentException(msg, e);
	    }
	} catch (NoSuchMethodException e5) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException("illegal argument - className?",
					       e5);
	} catch (SecurityException e6) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException("illegal argument - className?",
					       e6);
	} catch (ClassNotFoundException e7) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException("illegal argument - className?",
					       e7);
	}
    }


    /**
     * Create a new WebMap.
     * This method is provided so that new WebMap instances can
     * be created from a GUI or a command-line interface.
     * @param className the name of the class of the instance to create
     * @param arg an argument passed to the new instance's constructor
     *        (one argument, of type Object).
     * @return the new web map
     * @exception IOException an IO exception occurred during initialization
     * @exception IllegalArgumentException one of the arguments was
     *            illegal.
     */
    public static WebMap newWebMap(String className, Object arg)
	throws IOException, IllegalArgumentException
    {
	try {
	    Class<? extends WebMap> clasz =
		Class.forName(className).asSubclass(WebMap.class);
	    Constructor<? extends WebMap> constructor =
		clasz.getConstructor(Object.class);
	    return constructor.newInstance(arg);
	} catch (InstantiationException e1) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e1);
	} catch (IllegalAccessException e2) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e2);
	} catch (IllegalArgumentException e3) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e3);
	} catch (InvocationTargetException e4) {
	    Throwable e = e4.getTargetException();
	    if (e instanceof IOException) {
		throw (IOException) e;
	    } else if (e instanceof IllegalArgumentException) {
		throw (IllegalArgumentException) e;
	    } else {
		String msg = errorMsg("illegalArgument");
		throw new IllegalArgumentException(msg, e);
	    }
	} catch (NoSuchMethodException e5) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e5);
	} catch (SecurityException e6) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e6);
	} catch (ClassNotFoundException e7) {
	    String msg = errorMsg("illegalArgCN", className);
	    throw new IllegalArgumentException(msg, e7);
	}
    }
}

//  LocalWords:  exbundle Webserver API HTTPS EmbeddedWebServer IP ul
//  LocalWords:  HttpServer HttpsServer wildcard TCP mdash WebMap JKS
//  LocalWords:  EmbeddedWebServer's FileHandler boolean WebMap's TLS
//  LocalWords:  BZDev subdirectory URL's serverName errout ejws SunX
//  LocalWords:  Appendable passphrase useHTTPS SSL TSL maxThreads ss
//  LocalWords:  maxQueueLength corePoolSize maxPoolSize maxQueueSize
//  LocalWords:  addr idleTimeout ThreadPoolExecutor HashSet arg xml
//  LocalWords:  prefixSet className nowebxml displayDir hideWebInf
//  LocalWords:  IllegalArgumentException IllegalStateException https
//  LocalWords:  IOException SAXException http clazz prepended URI pw
//  LocalWords:  serverShutdown pathAlreadyAdded containedBy nthreads
//  LocalWords:  serverStopping setExecutor serverNotRunning Runnable
//  LocalWords:  serverAlreadyStopping HttpHandler illegalArgCN jks
//  LocalWords:  illegalArgument authenticator subclasses javascript
//  LocalWords:  InetAddress setupServer LinkedBlockingQueue TimeUnit
//  LocalWords:  shutdownNow serverRunning SSLSetup openjdk params
//  LocalWords:  HttpsConfigurator HttpsParameters SSLContext ksis
//  LocalWords:  keystore truststore ejwsCerts truststore's changeit
//  LocalWords:  configurator Configurators InputStream ksp kepw tsis
//  LocalWords:  tspw crlis toCharArray restartFailed sslSetup
//  LocalWords:  RequestInfo setSessionState getSessionState
//  LocalWords:  deallocating deallocate withState HttpContext
//  LocalWords:  EjwsStateTable Subclassing sessionOps stacktrace
//  LocalWords:  addSessionFilter ExecutorService
