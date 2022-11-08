package org.bzdev.swing.proxyconf;

/**
 * Proxy-configuration data.
 * This class is provided to encode the configurations
 * of proxies for HTTP, HTTPS, FTP, and SOCKS - the
 * protocols which java.net recognizes as ones where
 * proxies may be used.
 * <P>
 * The methods test if a an instance of this class
 * is meant to configure a proxy for a protocol and
 * if so, will information needed to use the proxy
 * (typically its host name or IP address and its
 * TCP or UDP port number).  If not configured, the
 * application is expected to use the preference
 * database to look up the values.
 * <P>
 * There are two constructors.  One takes a single argument,
 * a preference-node name. The use of this constructor
 * implies that a preference database should be used to
 * configure proxies.  The second takes 5 arguments: a
 * preference-node name and 4 boolean values indicating
 * which proxies are being configured.
 */
public class ProxyInfo {
    // private String prefName;
    boolean usePreferences;
    boolean configHttp;
    boolean configHttps;
    boolean configFtp;
    boolean configSocks;

    /**
     * Default preference node name.
     * This constant is provided as a convenience for use by
     * {@link ProxyComponent#setPrefNodeName(String)}.
     *  Its value is "org/bzdev/swing/proxyconf".
     */
    public static final String DEFAULT_PREF_NODE =
	"org/bzdev/swing/proxyconf";

    /**
     * Constructor specifying use of preferences.
     * Any values for proxies set by this class should be ignored as
     * the values configured are expected to be those provided by
     * preferences.
     */
    public ProxyInfo() {
	// this.prefName = prefName;
	this.usePreferences = true;
	this.configHttp = false;
	this.configHttps = false;
	this.configFtp = false;
	this.configSocks = false;
    }

    /**
     * Constructor.
     * Sets which proxies are of interest to the application and should
     * be configured for it.
     * When a boolean argument is false, this class does not configure
     * the corresponding proxy and instead the preference database is
     * used.  To disable a proxy reliably, set the flag to true and
     * set the values for the proxy to null.
     * @param configHttp  true if an HTTP proxy is to be configured by
     *                    this object; false otherwise
     * @param configHttps true if an HTTPS proxy is to be configured by
     *                    this object; false otherwise
     * @param configFtp   true if an FTP proxy is to be configured by
     *                    this object; false otherwise
     * @param configSocks true if a SOCKS proxy is to be configured by
     *                    this object; false otherwise
     */

    public ProxyInfo(boolean configHttp, boolean configHttps,
		     boolean configFtp, boolean configSocks) {

	// this.prefName = prefName;
	this.usePreferences = false;
	this.configHttp = configHttp;
	this.configHttps = configHttps;
	this.configFtp = configFtp;
	this.configSocks = configSocks;
    }

    /*
     * Get the preference Node name
     * @return the preference name (null if not specified by this
     *         object)
     */
    // public String getPrefNodeName() {return prefName;}

    /**
     * Determine if configuration data should be taken from
     * the Java preferences database.
     * @return true if Java preferences should be used; false if
     *         the configurations in this object should be used.
     */
    public boolean usePreferences() {return usePreferences;}


    /**
     * Determine if an HTTP proxy is configured by this object
     * @return true if an HTTP proxy  is configured, false otherwise
     */
    public boolean getConfigHttp() {return configHttp;}
    /**
     * Determine if an HTTPS proxy is configured by this object.
     * @return true if an HTTPS proxy  is configured, false otherwise
     */
    public boolean getConfigHttps() {return configHttps;}
    /**
     * Determine if an FTP proxy is configured by this object.
     * @return true if an FTP proxy  is configured, false otherwise
     */
    public boolean getConfigFtp() {return configFtp;}
    /**
     * Determine if a SOCKS proxy is configured by this object.
     * @return true if a SOCKS proxy  is configured, false otherwise
     */
    public boolean getConfigSocks() {return configSocks;}

    boolean useSystemProxies = false;

    /**
     * Set system-proxy mode.
     * @param value true if system proxies should be used; false otherwise
     */
    public void setUseSystemProxies(boolean value) {
	useSystemProxies = value;
    }

    /**
     * Get system-proxy mode.
     * @return true if system proxies should be used; false otherwise
     */
    public boolean getUseSystemProxies() {
	return useSystemProxies;
    }

    String httpProxyHost = null;

    /**
     * Set the host name or IP address of an HTTP proxy.
     * @param value the IP address or host name of the HTTP proxy
     */
    public void setHttpProxyHost(String value) {
	httpProxyHost = value;
    }

    /**
     * Get the host name or IP address of an HTTP proxy.
     * @return the host name or IP address of the HTTP proxy
     */
    public String getHttpProxyHost() {
	return httpProxyHost;
    }

    String httpProxyPort = null;

    /**
     * Set the TCP port of an HTTP proxy.
     * @param value the TCP port of the HTTP proxy
     */
    public void setHttpProxyPort(String value) {
	httpProxyPort = value;
    }

    /**
     * Get the TCP port of an HTTP proxy.
     * @return the TCP port the HTTP proxy
     */
    public String getHttpProxyPort() {
	return httpProxyPort;
    }

    String httpNonProxyHosts = null;

    /**
     * Set the host name or IP addresses that should not use an HTTP proxy.
     * @param value the IP addresses or host names, separated by a '|';
     *        null to indicate none.
     */
    public void setHttpNonProxyHosts(String value) {
	httpNonProxyHosts = value;
    }

    /**
     * Get the host name or IP addresses that should not use an HTTP proxy.
     * @return the host names and IP addresses, separated by a '|'; null if
     *         none have been set.
     */
    public String getHttpNonProxyHosts() {
	return httpNonProxyHosts;
    }

    String httpsProxyHost = null;

    /**
     * Set the host name or IP address of an HTTP proxy.
     * @param value the IP address or host name of the HTTP proxy; null if none
     */
    public void setHttpsProxyHost(String value) {
	httpsProxyHost = value;
    }

    /**
     * Get the host name or IP address of an HTTPS proxy.
     * @return the host name or IP address of the HTTPS proxy
     */
    public String getHttpsProxyHost() {
	return httpsProxyHost;
    }

    String httpsProxyPort = null;

    /**
     * Set the TCP port of an HTTPS proxy.
     * @param value the TCP port of the HTTPS proxy; null for no proxy
     */
    public void setHttpsProxyPort(String value) {
	httpsProxyPort = value;
    }

    /**
     * Get the TCP port of an HTTPS proxy.
     * @return the TCP port the HTTPS proxy; null if one is has not been set
     */
    public String getHttpsProxyPort() {
	return httpsProxyPort;
    }

    String ftpProxyHost = null;

    /**
     * Set the host name or IP address of an FTP proxy.
     * @param value the IP address or host name of the FTP proxy; null indicates
     *        no proxy
     */
    public void setFtpProxyHost(String value) {
	ftpProxyHost = value;
    }

    /**
     * Get the host name or IP address of an FTP proxy.
     * @return the host name or IP address of the FTP proxy; null if there is
     *         none
     */
    public String getFtpProxyHost() {
	return ftpProxyHost;
    }

    String ftpProxyPort = null;

    /**
     * Set the TCP port of an FTP proxy.
     * @param value the TCP port of the FTP proxy; null for no proxy
     */
    public void setFtpProxyPort(String value) {
	ftpProxyPort = value;
    }

    /**
     * Get the TCP port of an FTP proxy.
     * @return the TCP port the FTP proxy; null if no proxy was specified
     */
    public String getFtpProxyPort() {
	return ftpProxyPort;
    }

    String ftpNonProxyHosts = null;

    /**
     * Set the host name or IP addresses that should not use an FTP proxy.
     * @param value the IP addresses or host names, separated by a '|';
     *        null to indicate none.
     *
     */
    public void setFtpNonProxyHosts(String value) {
	ftpNonProxyHosts = value;
    }

    /**
     * Get the host name or IP addresses that should not use an FTP proxy.
     * @return the host names and IP addresses, separated by a '|'; null if
     *         none have been set.
     */
    public String getFtpNonProxyHosts() {
	return ftpNonProxyHosts;
    }

    String socksProxyHost = null;

    /**
     * Set the host name or IP address of a SOCKS proxy.
     * @param value the IP address or host name of the SOCKS proxy; null if no
     *        SOCKS proxy is specified
     */
    public void setSocksProxyHost(String value) {
	socksProxyHost = value;
    }

    /**
     * Get the host name or IP address of a SOCKS proxy.
     * @return the host name or IP address of the SOCKS proxy; null if
     *         there is none
     */
    public String getSocksProxyHost() {
	return socksProxyHost;
    }

    String socksProxyPort = null;

    /**
     * Set the TCP port of a SOCKS proxy.
     * @param value the TCP port of the SOCKS proxy; null for no proxy
     */
    public void setSocksProxyPort(String value) {
	socksProxyPort = value;
    }

    /**
     * Get the TCP port of a SOCKS proxy.
     * @return the TCP port the SOCKS proxy; null for no proxy specified
     */
    public String getSocksProxyPort() {
	return socksProxyPort;
    }
}

//  LocalWords:  HTTPS TCP UDP boolean prefName configHttp configFtp
//  LocalWords:  configHttps configSocks getPrefNodeName
