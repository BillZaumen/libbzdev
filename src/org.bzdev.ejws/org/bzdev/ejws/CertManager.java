package org.bzdev.ejws;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.security.KeyStore;
import java.security.cert.*;
import javax.net.ssl.TrustManager;

import org.bzdev.util.SafeFormatter;

//@exbundle org.bzdev.ejws.lpack.EmbeddedWebServer

/**
 * Certificate Manager.
 * <P>
 * A certificate manager is an alternative to 
 * {@link EmbeddedWebServer.SSLSetup} and, in addition to configuring
 * an {@link EmbeddedWebServer} for HTTPS, a certificate manager
 * automatically creates certificates and automatically renews them.
 * <P>
 * Callers should used the methods {@link CertManager#newInstance()}
 * or {@link CertManager#newInstance(String)} to create an instance of
 * this class. When an argument is provided, it is either the fully-qualified
 * class name of the desired certificate manager, the string "default" for
 * a certificate manager that will provide a self-signed certificate, or
 * the string "external" if another process manages the keystore.
 * When {@link CertManager#newInstance()}, the first instance found using
 * the Java service loader  is returned and if there are no such instances,
 * an instance of the default certificate manager is returned.
 * To configure the instance, use the methods
 * <UL>
 *   <LI><STRONG>{@link CertManager#setCertName(String)}</STRONG>.
 *       This provides an identifier to name a certificate. Some
 *       instances require this.
 *   <LI><STRONG>{@link CertManager#setDomain(String)}</STRONG>. 
 *       This provides the domain name used in a certificate.
 *   <LI><STRONG>{@link CertManager#setEmail(String)}</STRONG>.
 *       This provides an email address used to contact a user for
 *       administrative purposes (e.g., if a certificate is about to expire).
 *   <LI><STRONG>{@link CertManager#setTimeOffset(int)}</STRONG>.
 *       This provides a time offset in seconds from midnight and is used
 *       to schedule checks for expired certificates at a time at which the
 *       server is not likely to be busy: the server has to be stopped
 *       temporarily to install new certificates.
 *   <LI><STRONG>{@link CertManager#setInterval(int)}</STRONG>.
 *       This provides an interval in days between certificate renewal
 *       attempts. A certificate will not be renewed if it is not close
 *       to expiring. If set to zero, the value will be treated as one
 *       minute, which is useful for testing but not for actual use.
 *   <LI><STRONG>{@link CertManager#setStopDelay(int)}</STRONG>. This provides
 *       the time in seconds to wait before fully shutting down a server
 *       when necessary to renew certificates.
 *   <LI><STRONG>{@link CertManager#setProtocol(String)}</STRONG>.
 *       This provides the HTTPS protocol (e.g., SSL or TLS).
 *   <LI><STRONG>{@link CertManager#setKeystoreFile(File)}</STRONG>.
 *       This provides a keystore file that will be used
 *       to store certificates using the alias "servercert".  If the
 *       file does not exist, a new one will be created with a certificate.
 *   <LI><STRONG>{@link CertManager#setTruststoreFile(File)}</STRONG>.
 *       This provides a trust store file to allow the user to specify
 *       additional root certificates. It may be null if no trust store
 *       is desired.
 *   <LI><STRONG>{@link CertManager#setKeystorePW(char[])}</STRONG>.
 *       This provides the store password for a keystore as specified by
 *       keytool. The default password is "changeit".
 *   <LI><STRONG>{@link CertManager#setKeyPW(char[])}</STRONG>.
 *       This provides the key password for a keystore as specified by
 *       keytool. The default password is the 'store' password.
 *   <LI><STRONG>{@link CertManager#setTruststorePW(char[])}</STRONG>.
 *       This provides the store password for the trust store.
 *   <LI><STRONG>{@link CertManager#setConfigurator(EmbeddedWebServer.Configurator)}</STRONG>.
 *       This sets an SSL Configurator as specified by
 *       {@link EmbeddedWebServer.SSLSetup#configurator(EmbeddedWebServer.Configurator)}.
 *   <LI><STRONG>{@link CertManager#setTracer(Appendable)}</STRONG>.
 *       This provides an {@link Appendable} for tracing/logging.
 *   <LI><STRONG>{@link CertManager#setCertTrace(boolean)}</STRONG>.
 *       This indicates if new certificates should be be printed or not
 *       an {@link Appendable} for tracing has been configured.
 *   <LI><STRONG>{@link CertManager#setHelper(EmbeddedWebServer)}</STRONG>.
 *       This provides a 'helper' HTTP server that some certificate managers
 *       require. While these certificate managers will create such a server,
 *       one created with this method can run continuously.
 * </UL>
 * These methods return the {@link CertManager} on which they are called so
 * that one can write code such as
 * <BLOCKQUOTE><PRE>
 * CertManager cm = CertManager.newInstance()
 *   .setInterval(7)
 *   .setTimeOffset(4*3600)
 *   ...
 * </PRE></BLOCKQUOTE>
 * <P>
 * The <STRONG>newInstance</STRONG> methods use a {@link ServiceLoader}
 * to create instances of {@link CertManager}. Each must contain a zero
 * argument constructor and implement the following methods:
 * <UL>
 *   <LI><STRONG>{@link CertManager#requestCertificate()}</STRONG>.
 *   <LI><STRONG>{@link CertManager#certificateRequestStatus()}</STRONG>.
 *   <LI><STRONG>{@link CertManager#requestRenewal()}</STRONG>.
 *   <LI><STRONG>{@link CertManager#renewalRequestStatus()}</STRONG>.
 * </UL>
 * A provider may optionally implement the method
 * {@link CertManager#providerName()} to provide a name for a manager that is
 * more mnemonic than its class name, the method
 * {@link CertManager#configureHelper(EmbeddedWebServer)} to configure
 * an HTTP server (that is not running) for use by the provider, and the
 * method {@link CertManager#helperPort()} to indicate the port on which
 * an HTTP server should run, with 0 indicating that such a server is not
 * needed.
 * <P>
 * As usual, the module-info file for a subclass SUBCLASS should
 * contain the line
 * <BLOCKQUOTE><PRE>
 *     provides org.bzdev.ejws.CertManager with SUBCLASS;
 * </PRE></BLOCKQUOTE>
 * (using the fully-qualified name for SUBCLASS)
 * and the META-INF/services directory in the JAR file should contain
 * a file named org.bzdev.ejws.CertManager whose contents also include
 * <BLOCKQUOTE><PRE>
 * SUBCLASS
 * </PRE></BLOCKQUOTE>
 * (Tests indicate that some [all?] versions of Java will not recognize
 * service providers found on the class path if a META-INF/services entry
 * does not exist, so while redundant, including both is safer.)
 */
public abstract class CertManager {

    private static ResourceBundle exbundle = ResourceBundle.getBundle
	("org.bzdev.ejws.lpack.EmbeddedWebServer");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    /**
     * Mode determining how a {@link CertManager} behaves.
     */
    public static enum Mode {
	/**
	 * Normal operation.
	 * This is the default.
	 */
	NORMAL,
	/**
	 * Local mode.
	 * Creating a certificate involves both local operations
	 * and ones that require the use of a certificate authority.
	 * This mode performs the local operations, prints a description
	 * of any remote operations to a log file, and provides a
	 * self-signed certificate for further testing.
	 */
	LOCAL,
	/**
	 * Staging mode.
	 * Some providers can create non-functional test certificates
	 * because their certificate authority (for example, Lets Encrypt)
	 * places limits on the number of actual certificates that can be
	 * issued per unit time. If a certificate authority does not
	 * have such an option, this mode should behave the same as
	 * {@link Mode#NORMAL} mode.
	 */
	STAGED,
	/**
	 * Test mode.
	 * In this case, a certificate may not be created.  The mode
	 * TEST should be used only for initial testing where one
	 * might want to check for logic errors or a failure to catch
	 * various configuration errors. Providers may optionally
	 * handle this mode in the same way as {@link Mode#STAGED},
	 * {@link Mode#LOCAL} or {@link Mode#NORMAL}. A server may not
	 * work when this mode is used, but one can perform a
	 * standalone test (see the program CMTest.java in the
	 * tests/ejws directory of this library's source code and
	 * source code for the Docker image wtzbzdev/ejwsacme for
	 * examples).
	 * <P>
	 * Note: the default provider treats all modes the same as
	 * {@link Mode#NORMAL}.
	 */
	TEST
    }

    private Mode mode = Mode.NORMAL;

    /**
     * Set this certificate manager's mode.
     * This method should be used only when debugging or during
     * preliminary testing.
     * <P>
     * If not called, the default value is {@link Mode#NORMAL}.
     * @param mode the mode; the default if null
     * @return this certificate manage
     * @see CertManager.Mode
     */

    public CertManager setMode(Mode mode) {
	if (mode == null) mode = Mode.NORMAL;
	this.mode = mode;
	return this;
    }

    /**
     * Get the current mode.
     * @return the mode
     */
    public Mode getMode() {
	return mode;
    }

    private boolean alwaysCreate = false;

    /**
     * Determine if certificates should always be created instead
     * of being reused until they are close to expiring.
     * The default is false.
     * @return true if a certificate should always be created; false
     *         otherwise
     */
    public boolean alwaysCreate() {return alwaysCreate;}

    /**
     * Set if certificates should always be created instead
     * of being reused until they are close to expiring.
     * The default is false.
     * <P>
     * This method's primary use is for testing so that any code
     * written to renew a certificate is run as soon as a check for a
     * renewal is made.
     * @param mode true if a certificate should always be created; false
     *              otherwise
     * @return this certificate manage
     */
    public CertManager alwaysCreate(boolean mode) {
	alwaysCreate = mode;
	return this;
    }


    // By default, if a keystore does not exist, it will
    // be created.
    private static class DefaultCertManager extends CertManager {
	private static final int DAY = 3600*24;

	boolean status = false;
	String ks = null;
        static final int validity = 90; // Certificates valid for 90 days
	protected boolean externalKeystore = false;
	private X509Certificate ourcert = null; // used with externalKeystore

	private boolean handleCertificateRequest(boolean renew) {
	    boolean status = false;
	    String pathsep = System.getProperty("file.separator");
	    String keytool = System.getProperty("java.home")
		+ pathsep + "bin" + pathsep + "keytool";
	    char[] carray = getKeystorePW();
	    String spw = (carray == null)? null: new String(carray);
	    char[] carray2 = getKeyPW();
	    String kpw = (carray2 == null)? spw: new String(carray);
	    if (spw == null) {
		Appendable tracer = getTracer();
		if (tracer != null) {
		    try {
			String msg = errorMsg("keytoolPW");
			tracer.append(msg + "\n");
		    } catch (IOException eio) {}
		}
		return false;
	    }
	    if (getDomain() == null) {
		if (tracer != null) {
		    try {
			String msg = errorMsg("Domain");
			tracer.append(msg + "\n");
		    } catch (IOException eio) {}
		}
		return false;
	    }
	    try {
		File ks = getKeystoreFile();
		if (ks == null) {
		    if (tracer != null) {
			try {
			    String msg = errorMsg("Keytool");
			    tracer.append(msg + "\n");
			} catch (IOException eio) {}
		    }
		    return false;
		}
		this.ks = ks.getCanonicalPath();
		if (ks.exists()) {
		    try {
			KeyStore keystore = KeyStore.getInstance(ks, carray);
			Certificate cert = keystore
			    .getCertificate("servercert");
			if (cert != null && cert instanceof X509Certificate) {
			    X509Certificate xcert = (X509Certificate) cert;
			    if (externalKeystore) {
				if (ourcert == null) ourcert = xcert;
				if (!alwaysCreate() && xcert == ourcert) {
				    return !renew;
				} else {
				    return true;
				}
			    }
			    long tdiff = xcert.getNotAfter().getTime()
				- Instant.now().toEpochMilli();
			    tdiff /= (DAY*1000);
			    Certificate[] chain =
				keystore.getCertificateChain("servercert");
			    boolean notSelfSigned = (chain != null)
				&& chain.length > 1;
			    boolean ok = notSelfSigned
				|| alwaysCreate();
			    if (3*tdiff <= validity || ok) {
				try {
				    ProcessBuilder pb1 = new
					ProcessBuilder(keytool,
						       "-delete",
						       "-keystore", this.ks,
						       "-storepass", spw,
						       "-alias", "servercert");
				    pb1.redirectOutput
					(ProcessBuilder.Redirect.DISCARD);
				    pb1.redirectError
					(ProcessBuilder.Redirect.DISCARD);
				    Process p1 = pb1.start();
				    p1.waitFor();
				} catch (Exception ee){}
			    } else {
				// We already have a valid certificate
				status = !renew;
				return status;
			    }
			} else {
			    if (cert != null) {
				// This is a corner case that should never
				// happen in practice: a cert manager should
				// always create an X509 certificate and
				// users are expected to create one as well.
				if (tracer != null) {
				    String msg = errorMsg("notX509");
				    tracer.append(msg + "\n");
				}
				return false;
			    }
			}
		    } catch (Exception ke) {
			status = false;
			Appendable tracer = getTracer();
			if (tracer != null) {
			    String msg = errorMsg("oldCert", ke.getMessage());
			    tracer.append(msg + "\n");
			}
			return false;
		    }
		}
		if (externalKeystore) return false;
		ProcessBuilder pb2 = new
		    ProcessBuilder(keytool,
				   "-genkeypair",
				   "-keyalg", "EC",
				   "-groupname", "secp256r1",
				   "-sigalg", "SHA256withECDSA",
				   "-keystore", this.ks,
				   "-keypass", kpw,
				   "-storepass", spw,
				   "-alias", "servercert",
				   "-dname", "CN=" + getDomain(),
				   "-validity", "" + validity);
		pb2.redirectOutput(ProcessBuilder.Redirect.DISCARD);
		pb2.redirectError(ProcessBuilder.Redirect.DISCARD);
		Process p2 = pb2.start();
		status =  p2.waitFor() == 0;
	    } catch (Exception e) {
		status = false;
		Appendable tracer = getTracer();
		if (tracer != null) {
		    try {
			String msg = errorMsg("newCert", e.getMessage());
			tracer.append(msg + "\n");
		    } catch (IOException eio) {}
		}
	    }
	    return status;
	}

	@Override
	protected void requestCertificate() {
	    status = handleCertificateRequest(false);
	}

	@Override
	protected boolean certificateRequestStatus() {return status;}

	private Object monitor = new Object();

	private boolean rstatus = false;

	@Override
	protected void requestRenewal() {
	    boolean rs = handleCertificateRequest(true);
	    synchronized (monitor) {
		rstatus = rs;
		monitor.notifyAll();
	    }
	}
	@Override
	protected boolean renewalRequestStatus() throws InterruptedException {
	    synchronized(monitor) {
		try {
		    while (rstatus == false) {
			monitor.wait();
			Appendable tracer = getTracer();
			if (tracer != null) {
			    try {
				String msg = errorMsg("renewStatus", rstatus);
				tracer.append(msg + "\n");
			    } catch (IOException eio) {}
			}
		    }
		    return true;
		} catch (InterruptedException e) {
		    rstatus = false;
		    throw e;
		} finally {
		    rstatus = false;
		}
	    }
	}
    }

    private static class ExternalCertManager extends DefaultCertManager {
	ExternalCertManager() {
	    super();
	    externalKeystore =  true;
	}
    }

    /**
     * Return a new instance of {@link CertManager}.
     * The instance returned will preferentially be the first one
     * provided by a service loader.  If the server loader cannot find
     * an instance, an instance of the default certificate manager
     * will be returned.
     * @return a new instance of {@link CertManager}.
     */
    public static CertManager newInstance() {
	ServiceLoader<CertManager>loader = ServiceLoader
	    .load(CertManager.class);
	for (CertManager manager: loader) {
	    return manager;
	}
	return new DefaultCertManager();
    }

    /**
     * Get the names of each available certificate manager.
     * A name is either "default" or the fully qualified class name
     * of a certificate manager.  A manager may be listed twice
     * if it explicitly specifies a name.  Explicitly specified
     * names may be shared by more than one certificate manager.
     * @return the names of the certificate managers.
     */
    public static Set<String> providerNames() {
	final TreeSet<String> result = new TreeSet<>();
	ServiceLoader.load(CertManager.class).stream()
	    .map(ServiceLoader.Provider::get)
	    .forEach((m) -> {
		    String name = m.getClass().getCanonicalName();
		    result.add(name);
		    name = m.providerName();
		    if (name != null) {
			result.add(name);
		    }
		});
	/*
	Set<String> result =  ServiceLoader.load(CertManager.class)
	    .stream()
	    .map((obj) -> {return obj.getClass().getCanonicalName();})
	    .collect(Collectors.toSet());
	*/
	result.add("default");
	result.add("external");
	return result;
    }


    /**
     * Return a new instance of a certificate manager given its name.
     * @param providerName "default" for the default provider, "external"
     *        if another process manages certificats, or the
     *        fully qualified class name for the desired instance, a
     *        a provider name; or null for the first one available,
     *        excluding "default" and "external"
     */
    public static CertManager newInstance(String providerName) {
	if (providerName.equals("default")) {
	    return new DefaultCertManager();
	}
	if (providerName.equals("external")) {
	    return new ExternalCertManager();
	}
	ServiceLoader<CertManager>loader = ServiceLoader
	    .load(CertManager.class);
	for (CertManager manager: loader) {
	    if (providerName == null) {
		return manager;
	    } else  if (manager.getClass().getCanonicalName()
			.equals(providerName)) {
		return manager;
	    } else if (manager.providerName().equals(providerName)) {
		return manager;
	    }
	}
	return null;
    }

    private static final int DAY = 3600*24;

    String dommain = null;
    String email = null;

    String name = null;

    String protocol = null;
    File keystoreFile = null;
    File truststoreFile = null;
    TrustManager[] trustManagers = null;
    char[] keystorePW = "changeit".toCharArray();
    char[] keyPW = null;
    char[] truststorePW = null;
    String domain = null;
    EmbeddedWebServer.Configurator configurator = null;

    EmbeddedWebServer helper = null;

    Appendable tracer = null;

    /**
     * Set an {@link Appendable} to log various events.
     * @param tracer an {@link Appendable} used to log events; null
     *        to disable logging
     * @return this {@link CertManager}
     */
    public CertManager setTracer(Appendable tracer) {
	this.tracer = tracer;
	return this;
    }

    /**
     * Get the current {@link Appendable} used for tracing.
     * @return the {@link Appendable}; null if there is none
     */
    protected Appendable getTracer() {return tracer;}


    /**
     * Set the certificate name.
     * A certificate name is an identifier used to provide a readable
     * name for a certificate.  This may be used as part of a file name
     * in some implementations.
     * @param name the name.
     * @return this {@link CertManager}
     */
    public CertManager setCertName(String name) {
	this.name = name;
	return this;
    }

    /**
     * Get the certificate name.
     * @return the certificate name; null if there is none
     */
    public String getCertName() {return name;}

    
    /**
     * Set the domain name for a server.
     * This will typically be the CN (Common Name) portion of a certificate's
     * distinguished name.
     * @param domain the domain name (typically fully qualified or
     *        "localhost" for testing)
     * @return this {@link CertManager}
     */
    public CertManager setDomain(String domain) {
	this.domain = domain;
	return this;
    }
    
    /**
     * Get the domain name for a server.
     * @return the domain name
     */
    public String getDomain() {return domain;}

    /**
     * Set an email address for administrative emails.
     * This may be used for purposes such as notifications that a
     * certificate is about to expire.
     * @param email the email address
     * @return this {@link CertManager}
     */
    public CertManager setEmail(String email) {
	this.email = email;
	return this;
    }

    /**
     * Get the email address.
     * @return the email address
     */
    public String getEmail() {return email;}


    /**
     * Optional print name for a provider.
     * The default name for a provider is its fully qualified class
     * name.  This method provides an alternate name.  It may not be
     * unique and may represent groups of providers.  Providers that
     * use certbot should override this method to return the string
     * "certbot".  The names "default" and "external" are  reserved
     * and must not be used as new provider names.
     * @return the provider name; null if one is not explicitly provided
     */
    public String providerName() {
	return null;
    }

    /**
     * Determine if a certificate manager needs an HTTP server to
     * create a certificate, providing that server's TCP port.

     * For example, some implementations use the
     * <A HREF="https://datatracker.ietf.org/doc/html/rfc8555">ACME</A>
     *  protocol to obtain a certificate and use
     * a server running HTTP on port 80 for that purpose.
     * The default implementation returns 0, indicating that a server
     * is not needed.  If this method returns a non-zero value and
     * {@link #setHelper(EmbeddedWebServer)} was not called, a provider's
     * implementation is expected to create a server running HTTP using
     * the port returned by this method.
     * @return the TCP port if a web server is needed; 0 otherwise
     */
    public  int helperPort() {return 0;}

    /**
     * Configure a helper HTTP server for use with this certificate
     * manager.
     * The default implementation of this method does nothing and should
     * be overridden by providers such as ones that implement the
     * <A HREF="https://datatracker.ietf.org/doc/html/rfc8555">ACME</A>
     *  protocol, which requires a specific configuration for a server.
     * <P>
     * This method will be called by {@link #setHelper(EmbeddedWebServer)}
     * when all of the following are true:
     * <UL>
     *   <LI> {@link #helperPort()} returns a non-zero value.
     *   <LI> the argument to {@link #setHelper(EmbeddedWebServer)} is
     *        not null and is an {@link EmbeddedWebServer} that is not
     *        running.
     *   <LI> the server provided by the argument is an HTTP server, not
     *        an HTTPS server.
     *</UL>
     * @param helper the helper HTTP server
     */
    protected void configureHelper(EmbeddedWebServer helper) {
    }

    /**
     * Request a certificate.
     * When this method is called, the certificate should be
     * placed in a keystore specified by {@link #setKeystoreFile(File)}
     * with an alias "servercert". An existing certificate should be
     * returned unless it is about to expire, and if there is none, a new
     * certificate should be created.  This method may return before
     * the operation is complete.
     * <P>
     * Some providers of this class (e.g., ones using the
     * <A HREF="https://datatracker.ietf.org/doc/html/rfc8555">ACME</A>
     * protocol) require a server running HTTP on a specified port.
     * In this case, requestCertificate() should call
     * {@link #getHelper()} and if the result is not null, use that
     * server. If that server is not running, requestCertificate() should
     * ensure that any necessary prefixes have been added.
     * <P>
     * If this certificate manager needs a helper and
     * {@link #getHelper()} returns null, this method must create the
     * helper, configure it, start it, and shut it down before
     * exiting.
     */
    protected abstract void requestCertificate();

    /**
     * Determine the status of a previously issued call to
     * {@link #requestCertificate}.
     * If the status is unknown, this method must block until the
     * status can be determined.
     * @return true if a certificate is available; false otherwise
     */
    protected abstract boolean certificateRequestStatus();

    /**
     * Request that a certificate be renewed. If a new certificate
     * is available, this method must arrange for
     * {@link #renewalRequestStatus()} to return <CODE>true</CODE>.
     * This method and {@link #renewalRequestStatus()} are called
     * from different threads.
     * <P>
     * If this certificate manager needs a helper and
     * {@link #getHelper()} returns null, this method must create the
     * helper, configure it, start it, and shut it down before
     * exiting.
     */
    protected  abstract void requestRenewal();

    /**
     * Determine the status of a previously issued call to
     * {@link requestRenewal} when that method generates a new
     * certificate.
     * This method should normally block until it will return
     * <CODE>true</CODE>. It,
     * and {@link #requestRenewal()}, are called from different threads.
     * @return true if a new certificate is available; false if there was
     *         an error.
     */
    protected  abstract boolean renewalRequestStatus()
	throws InterruptedException;
    
    int timeOffset = 0;
    int interval = 5;
    int stopDelay = 3;

    /**
     * Set the time offset.
     * This is the time of the day in seconds at which attempts to
     * renew certificates will be made.  Time is measured from
     * midnight using the server's time zone.  The number of days
     * between renewal attempts are set by {@link #setInterval(int)}.
     * <P>
     * If not called, the default value is 0.
     * @param offset the time offset in seconds
     * @return this {@link CertManager}
     */
    public CertManager setTimeOffset(int offset) {
	timeOffset = offset;
	return this;
    }
    
    /**
     * Get the time offset.
     * This is the time of the day in seconds at which
     * attempts to renew certificates will be made.  Time is measured
     * from midnight using the server's time zone.  The number of days between
     * renewal attempts are set by {@link #setInterval(int)}.
     * @return the time offset in seconds.
     */
    public int getTimeOffset() {return timeOffset;}


    /**
     * Set the number of days between attempts to renew certificates.
     * <P>
     * If not called, the default value is 7.
     * @param interval the interval in days.
     * @return this {@link CertManager}
     */
    public CertManager setInterval(int interval) {
	this.interval = interval;
	return this;
    }

    /**
     * Get the number of days between attempts to renew certificates.
     * @return the interval in days
     */
    public int getInterval() {return interval;}

    /**
     * Set the delay for stopping a server.
     * When {@link EmbeddedWebServer#stop(int)} is called, new
     * requests are rejected immediately, and the delay indicates how
     * long to wait for existing requests to be processed.
     * <P>
     * If not called, the default value is 5.
     * @param stopDelay the delay in seconds
     * @return this {@link CertManager}
     */
    public CertManager setStopDelay(int stopDelay) {
	this.stopDelay = stopDelay;
	return this;
    }

    /**
     * Get the delay for stopping a server.
     * When {@link EmbeddedWebServer#stop(int)} is called, new
     * requests are rejected immediately, and the delay indicates how
     * long to wait for existing requests to be processed.
     * @return the delay in seconds
     */
    public int getStopDelay() {return stopDelay;}

    private TimeZone timezone = TimeZone.getDefault();

    /**
     * Set the time zone given a time-zone ID.
     * The Java class {@link TimeZone} provides a method to list the
     * available time zone IDs.
     * If the time zone ID is not recognized, the time zone will be
     * set to GMT.
     * <P>
     * If not called, the default value is the system default time zone.
     * @param timezone the time zone ID (e.g, UTC or America/Los_Angeles);
     *       null or an empty string for the system default
     * @return this {@link CertManager}
     * @see TimeZone#getAvailableIDs()
     */
    public CertManager setTimeZone(String timezone) {
	this.timezone = (timezone == null || timezone.trim().length() == 0)?
	    TimeZone.getDefault(): TimeZone.getTimeZone(timezone);
	return this;
    }

    /**
     * Set the time zone.
     * @param timezone the time zone ID (e.g, UTC or America/Los_Angeles);
     *       null for the system default
     * @return this {@link CertManager}
     */
    public CertManager setTimeZone(TimeZone timezone) {
	this.timezone = (timezone == null)? TimeZone.getDefault(): timezone;
	return this;
    }

    /**
     * Get the time-zone ID.
     * @return the time-zone ID for this object
     */
    public String getTimeZone() {
	return timezone.getID();
    }

    /*
    private static final int tzoffset =
	(int)(TimeZone.getDefault().getRawOffset()/1000);
    */

    /**
     * Get the waiting time in milliseconds from when a server
     * starts to the first time at which an attempt to renew a
     * certificate is made.
     * @return the time interval in milliseconds
     */
    public long getInitialWaitMillis() {
	int tzoffset = (int) ((timezone.getRawOffset() +
			       timezone.getDSTSavings())
			      / 1000);
	if (interval == 0) {
	    // An interval of 0 does not make sense so set
	    // it to one minute: useful only for testing
	    return 60000L;
	}
	long now = Instant.now().getEpochSecond() + tzoffset;
	long tau = (now / DAY) * DAY;
	long diff = now - tau;
	if (diff < 0) diff += DAY;
	if (diff > DAY) diff -= DAY;
	return (DAY - diff + timeOffset + DAY*interval)*1000;

    }

    boolean certTrace = false;

    /**
     * Set whether or not certificates should be printed in traces
     * @param certTrace true if certificates should be printed; false otherwise
     * @return this {@link CertManager}
     */
    public CertManager setCertTrace(boolean certTrace) {
	this.certTrace = certTrace;
	return this;
    }

    /**
     * Get the time interval between attempts to renew certificates.
     * @return the time interval in milliseconds
     */
    public long getIntervalMillis() {
	if (interval == 0) {
	    // An interval of 0 does not make sense so set
	    // it to one minute: useful only for testing
	    return 60000;
	}
	return interval*DAY*1000;
    }

    /**
     * Set the  protocol for the server to use
     * @param protocol the protocol (e.g., SSL or TLS) for HTTPS; null for HTTP
     * @return this {@link CertManager}
     */
    public CertManager setProtocol(String protocol) {
	this.protocol = protocol;
	return this;
    }

    /**
     * Set the protocol for the server to use.
     * @return the protocol (e.g., SSL or TLS) for HTTPS; null for HTTP
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Set the file for  a keystore.
     * If the file does not exist, requests to get a certificate will
     * create the file.
     * @param file the keystore file; null if there is no keystore
     * @return this {@link CertManager}
     */
    public CertManager setKeystoreFile(File file) {
	keystoreFile = file;
	return this;
    }

    /**
     * Get the file for a keystore.
     * If the file does not exist, requests to get a certificate will
     * create the file.
     * @return the file; null if there is no keystore
     */
    public File getKeystoreFile() {
	return keystoreFile;
    }

    /**
     * Provide trust managers.
     * @param tms the trust managers; null too cancel
     * @return this object
     * @throws IllegalStateException if a {#link #t\setTrusstoreFile} was
     *         called with a non-null value
     * @see #setTruststoreFile(File)
     */
    public CertManager setTrustManagers(TrustManager[] tms)
	throws IllegalStateException
    {
	if (truststoreFile != null) {
	    throw new IllegalStateException("tsset1");
	}
	this.trustManagers = tms;
	return this;
    }

    /**
     * Provide a file containing a trust store.
     * @param file a file containing a trust store; null to
     *        cancel
     * @return this object
     * @throws IllegalStateException if {@link #trustManagers} has
     *         been called with a non-null value
     * @see #setTrustManagers(TrustManager[])
     */
    public CertManager setTruststoreFile(File file)
	throws IllegalStateException
    {
	if (trustManagers != null) {
	    throw new IllegalStateException("tmset1");
	}
	truststoreFile = file;
	return this;
    }

    /**
     * Set the "store" password for a keystore.
     * The default password is "changeit".
     * @param pw the password; null for the default
     * @return this {@link CertManager}
     */
    public CertManager setKeystorePW(char[] pw) {
	keystorePW = (pw == null)? "changeit".toCharArray(): pw.clone();
	return this;
    }

    /**
     * Get the "store" password for a keystore.
     * @return the password; null if there is none
     */
    protected char[] getKeystorePW() {
	return (keystorePW == null)? null: keystorePW.clone();
    };

    /**
     * Set the key password for a keystore.
     * @param pw the password; null to use the store password
     * @return this {@link CertManager}
     * @see CertManager#setKeystorePW(char[])
     */
    public CertManager setKeyPW(char[] pw) {
	keyPW = (pw == null)? null: pw.clone();
	return this;
    }

    /**
     * Get the key password for a keystore.
     * @return the password; the keystore password if no key password has
     *         been explicitly provided
     */
    protected char[] getKeyPW() {
	return (keyPW == null)? getKeystorePW(): keyPW.clone();
    }

    /**
     * Set the "store" password for a truststore.
     * @param pw the password; null if there is none
     * @return this {@link CertManager}
     */
    public CertManager setTruststorePW(char[] pw) {
	truststorePW = pw.clone();
	return this;
    }
    
    /**
     * Set the "store" password for a truststore.
     * @return pw the password; null if there is none
     */
    protected char[] getTruststorePW() {return truststorePW.clone();}

    /**
     * Set the SSL configurator.
     * Configurators are instance of the functional interface
     * {@link EmbeddedWebServer.Configurator}, and will typically be
     * provided by a lambda expression that takes two arguments:
     * an instance of {@link javax.net.ssl.SSLContext} and an instance
     * of {@link com.sun.net.httpserver.HttpsParameters}. These two
     * arguments are provided by the
     * {@link com.sun.net.httpserver.HttpsServer} implementation.
     * @param c the configurator
     * @return this {@link CertManager}
     */
    public CertManager setConfigurator(EmbeddedWebServer.Configurator c) {
	this.configurator = c;
	return this;
    }

    /**
     * Get a helper web server.
     * Certificate authorities such as Lets-Encrypt use the ACME protocol for
     * managing certificates
     * (<A HREF="https://datatracker.ietf.org/doc/html/rfc8555"> RFC 8555</A>),

     * and these may make use of a separate web server using HTTP
     * instead of HTTPS and typically running on port 80. This method
     * returns an {@link EmbeddedWebServer} that can be used for such
     * purposes. When provided, the method {@link #requestCertificate()}
     * is expected to  start the helper if it is not already
     * running and will start the helper after adding any prefixes
     * needed as part of the process of obtaining a certificate.
     * <P>
     * When this method returns null, {@link #requestCertificate}
     * and {@link #requestRenewal()} are expected to create a helper
     * if needed, but will usually shut this helper down before
     * returning.
     * @return the web server; null if none is provided
     */
    protected EmbeddedWebServer getHelper() {
	return helper;
    }

    /**
     * Set a helper web server.
     * Certificate authorities such as Lets-Encrypt use the ACME protocol for
     * managing certificates
     * (<A HREF="https://datatracker.ietf.org/doc/html/rfc8555"> RFC 8555</A>),
     * and these may make use of a separate web server using HTTP
     * instead of HTTPS and running on port 80.  This method allows
     * the caller to provide an {@link EmbeddedWebServer} that can be
     * used for such purposes, with the added benefit of configuring
     * additional prefixes to handle the case where a client is given
     * a URL whose scheme is HTTP instead of HTTPS.
     * <P>
     * If all of the following are true
     * <OL>
     *   <LI> the argument to this method is not null,
     *   <LI> {@link #helperPort()} returns a non-zero value,
     *   <LI> the helper server is not an HTTPS server,
     *   <LI> the helper server is not currently running,
     * </OL>
     * then the helper will be configured, typically adding prefixes
     * needed for it to operate as a helper, and the helper will be
     * started.
     * <P>
     * If a server is configured with this {@link CertManager} and that
     * server is shut down, the helper will also be shut down and cannot
     * be reused.  As a result, a {@link CertManager} that provides a
     * helper should be used with only a single {@link EmbeddedWebServer}.
     * Certificate manager providers should not try to start a helper
     * that is already running.  It is a good practice to let the
     * provider start the helper.
     * @param helper the embedded web server to use; null (the default)
     *        if none is provided
     * @return this {@link CertManager}
     * @see #configureHelper(EmbeddedWebServer)
     */
    public CertManager setHelper(EmbeddedWebServer helper) {
	this.helper = helper;
	int hport = helperPort();
	if (helper != null && hport != 0 && !helper.usesHTTPS()
	    && !helper.serverRunning()) {
	    configureHelper(helper);
	    helper.start();
	}

	return this;
    }

    private void traceCertificate() {
	if (certTrace && tracer != null) {
	    String pathsep = System.getProperty("file.separator");
	    String keytool = System.getProperty("java.home")
		+ pathsep + "bin" + pathsep + "keytool";
	    try {
		String keystore = keystoreFile.getCanonicalPath();
		ProcessBuilder pb = new
		    ProcessBuilder(keytool,
				   "-exportcert",
				   "-keystore", keystore,
				   "-storepass", new String(keystorePW),
				   "-alias", "servercert",
				   "-rfc");
		Process p = pb.start();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
		p.getInputStream().transferTo(bos);
		tracer.append(bos.toString());
	    } catch (IOException eio) {}
	}
    }

    private boolean createCertificateIfNeeded() {
	requestCertificate();
	try {
	    return certificateRequestStatus();
	} finally {
	    if (tracer != null) {
		try {
		    String msg = errorMsg("hasCert");
		    tracer.append(msg + "\n");
		} catch (IOException eio) {}
	    }
	    traceCertificate();
	}
    }

    /**
     * Get a configured instance of {@link EmbeddedWebServer.SSLSetup}.
     * Various methods provided by this class duplicate those provided
     * by {@link EmbeddedWebServer.SSLSetup}.
     * <P>
     * Calling this method will also create keystore file and
     * a certificate if one is not already available.
     * @return a configured instance {@link EmbeddedWebServer.SSLSetup}
     */
    public EmbeddedWebServer.SSLSetup getSetup() throws IOException {
	if (protocol != null) {
	    createCertificateIfNeeded();
	}
	EmbeddedWebServer.SSLSetup sslSetup =
	    new EmbeddedWebServer.SSLSetup(protocol);
	if (keystoreFile != null) {
	    sslSetup.keystore(new FileInputStream(keystoreFile));
	}

	if (trustManagers != null) {
	    sslSetup.trustManagers(trustManagers);
	}

	if (truststoreFile != null) {
	    sslSetup.truststore(new FileInputStream(truststoreFile));
	}

	if(keystorePW != null) {
	    sslSetup.keystorePassword(getKeystorePW());
	}
	if (keyPW != null || keystorePW != null) {
	    sslSetup.keyPassword(getKeyPW());
	}

	if (truststorePW != null) {
	    sslSetup.truststorePassword(truststorePW);
	}

	if (configurator != null) {
	    sslSetup.configurator(configurator);
	}
	return sslSetup;
    }

    volatile int stopcount = 0;

    Thread[] monitorCertificate(final EmbeddedWebServer ews) {
	Thread certMonitor1 = new Thread(() -> {
		if (tracer != null) {
		    try {
			String msg = errorMsg("monitoringStarted");
			tracer.append(msg + "\n");
		    } catch (IOException eio) {}
		}
		long wait = getInitialWaitMillis();
		Instant time = Instant.now();
		try {
		    Thread.currentThread().sleep(wait);
		    time = time.plusMillis(wait);
		    for (;;) {
			Instant now = Instant.now();
			long diff = time.toEpochMilli() - now.toEpochMilli();
			wait = getIntervalMillis();
			time = time.plusMillis(wait);
			wait += diff;
			if (tracer != null) {
			    try {
				String msg = errorMsg("requestRenewal");
				tracer.append(msg +"\n");
			    } catch (IOException eio) {}
			}
			synchronized(ews) {
			    // synchronized on ews so that we can't
			    // start or stop ews while the keystore is being
			    // updated
			    requestRenewal();
			}
			Thread.currentThread().sleep(wait);
		    }
		} catch (InterruptedException e) {
		    stopcount++;
		    if (stopcount == 2) {
			if (tracer != null) {
			    try {
				String msg = errorMsg("monitoringStopped");
				tracer.append(msg + "\n");
			    } catch (IOException eio) {}
			}
			stopcount = 0;
		    }
		}
	});
	certMonitor1.start();

	Thread certMonitor2 = new Thread(() -> {
		try {
		    for (;;) {
			boolean status = renewalRequestStatus();
			if (status) {
			    synchronized(ews) {
				if (tracer != null) {
				    String msg = errorMsg("renewed");
				    tracer.append(msg + "\n");
				}
				traceCertificate();
				ews.modifyServerSetup();
				if (ews.serverRunning()) {
				    ews.stop(stopDelay);
				    ews.start();
				}
			    }
			}
		    }
		} catch (InterruptedException ie) {
		    stopcount++;
		    if (stopcount == 2) {
			if (tracer != null) {
			    try {
				String msg = errorMsg("monitoringStopped");
				tracer.append(msg + "\n");
			    } catch (IOException eio) {}
			}
			stopcount = 0;
		    }
		} catch (Exception e) {
		    if (tracer != null) {
			try {
			    String emsg = e.getMessage();
			    String msg = errorMsg("renewalFailed", emsg);
			    tracer.append(msg + "\n");
			} catch (IOException eio) {}
		    }
		}
	});
	certMonitor2.start();
	Thread[] result = {
	    certMonitor1,
	    certMonitor2
	};
	return result;
    }

    Thread[] certThreads = null;
    EmbeddedWebServer ews = null;

    /**
     * Test if certificates ares being monitored.
     * @return true if certificates are being monitored; false otherwise
     */
    public boolean isMonitoring() {
	return certThreads != null;
    }

    /**
     * Start monitoring certificates for a server
     * @param ews the server whose certificates are to be monitored
     */
    public synchronized void startMonitoring(EmbeddedWebServer ews) {
	if (certThreads != null) {
	    if (this.ews == ews) {
		return;
	    } else {
		String msg = errorMsg("multipleEWS");
		if (tracer != null) {
		    try {
			tracer.append(msg + "\n");
		    } catch (IOException eio) {}
		}
		throw new IllegalStateException(msg);
	    }
	}
	certThreads =  monitorCertificate(ews);
	stopcount = 0;
	this.ews = ews;
    }

    /**
     * Stop monitoring certificates for a server.
     */
    public synchronized void stopMonitoring() {
	if (stopcount > 0) return;
	if (certThreads != null) {
	    stopcount = 0;
	    try {
		certThreads[0].interrupt();
	    } catch (Exception e) {}
	    try {
		certThreads[1].interrupt();
	    } catch (Exception e) {}
	    certThreads = null;
	    ews = null;
	}
	EmbeddedWebServer helper = getHelper();
	if (helper != null && helper.serverRunning()) {
	    // Shut down the helper because stopMonitoring
	    // is called only by EmbeddedWebServer.shutdown.
	    helper.shutdown(0);
	}
    }
}




//  LocalWords:  exbundle EmbeddedWebServer SSLSetup HTTPS setDomain
//  LocalWords:  CertManager newInstance setCertName setEmail SSL TLS
//  LocalWords:  setTimeOffset setInterval setStopDelay setProtocol
//  LocalWords:  setKeystoreFile keystore servercert setKeystorePW CN
//  LocalWords:  setTruststoreFile keytool setKeyPW setTruststorePW
//  LocalWords:  Configurator setTracer Appendable setCertTrace PRE
//  LocalWords:  boolean BLOCKQUOTE ServiceLoader requestCertificate
//  LocalWords:  certificateRequestStatus requestRenewal storepass pw
//  LocalWords:  renewalRequestStatus genkeypair keyalg groupname SHA
//  LocalWords:  secp sigalg withECDSA keypass dname providerName rfc
//  LocalWords:  changeit localhost stopDelay certTrace truststore OL
//  LocalWords:  configurator Configurators exportcert ews toSet TCP
//  LocalWords:  multipleEWS certbot setHelper configureHelper HREF
//  LocalWords:  helperPort ResourceBundle getBundle getHelper CMTest
//  LocalWords:  stopMonitoring ejws wtzbzdev ejwsacme
