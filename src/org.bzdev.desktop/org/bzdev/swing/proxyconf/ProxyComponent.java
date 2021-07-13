package org.bzdev.swing.proxyconf;

import javax.swing.*;
import org.bzdev.swing.*;
import java.util.*;
import java.util.prefs.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.Color;
import java.awt.event.*;
import java.text.MessageFormat;

/**
 * Component for configuring network proxies.
 * There are some static methods for the case where a GUI will not be
 * used, and for configuring the location of entries in the preference database.
 * The classes {@link org.bzdev.swing.proxyconf.ProxyDialog} and
 * {@link org.bzdev.swing.ProxyMenuItem} are the ones a GUI will typically
 * use (usually just the menu item as it will handle everything).
 * <P>
 * The resource org/bzdev/swing/proxyconf/lpack/ProxyComponent.html contains
 * an HTML page that can be used for documentation (this page uses
 * HTML 3.2 because that is the version supported by a JEditorPane).
 * This page depends on the sresource URL scheme, which indicates that
 * the resource is located on the application's class path.  If used,
 * <code>org.bzdev.protocols.Handlers.enable()</code> must be called.
 * @see org.bzdev.swing.proxyconf.ProxyDialog
 * @see org.bzdev.swing.proxyconf.ProxyInfo
 * @see org.bzdev.swing.ProxyMenuItem
 */
public class ProxyComponent extends JComponent {

    private static Properties origProperties = System.getProperties();

    static private final String resourceBundleName = 
	"org.bzdev.swing.proxyconf.lpack.ProxyComponent";
    static ResourceBundle bundle = 
	ResourceBundle.getBundle(resourceBundleName);
    static String localeString(String name) {
	return bundle.getString(name);
    }

    static String prefname = null;
    static Preferences userPrefs = null;

    /**
     * Set the name of the preference node for proxies.
     * @param prefname the name of the preference node; null for the
     *        default (ProxyInfo.DEFAULT_PREF_NODE).
     */
    public static void setPrefNodeName(String prefname) {
	if (prefname == null) prefname =  ProxyInfo.DEFAULT_PREF_NODE;
	ProxyComponent.prefname = prefname;
	userPrefs = Preferences.userRoot().node(prefname);
    }

    static {
	setPrefNodeName(null);
    }

    JCheckBox useSystemProxies;
    String useSystemProxiesPref = "java.net.useSystemProxies";
    boolean useSystemProxiesFlag;

    JCheckBox useHttpProxy;
    String useHttpProxyPref = "useHttpProxy";
    boolean useHttpProxyFlag; 

    JCheckBox useHttpsProxy;
    String useHttpsProxyPref = "useHttpsProxy";
    boolean useHttpsProxyFlag; 

    JCheckBox useFtpProxy;
    String useFtpProxyPref = "useFtpProxy";
    boolean useFtpProxyFlag; 

    JCheckBox useSocksProxy;
    String useSocksProxyPref = "useSocksProxy";
    boolean useSocksProxyFlag; 

    static final String emptyString = "";

    JLabel httpProxyHostLabel;
    JTextField httpProxyHostField;
    String httpProxyHostPref = "http.proxyHost";
    String httpProxyHost; 

    JLabel httpProxyPortLabel;
    PortTextField httpProxyPortField;
    String httpProxyPortPref = "http.proxyPort";
    String httpProxyPort; 

    JLabel httpNonProxyHostsLabel;
    JTextField httpNonProxyHostsField;
    String httpNonProxyHostsPref = "http.nonProxyHosts";
    String httpNonProxyHosts; 

    JLabel httpsProxyHostLabel;
    JTextField httpsProxyHostField;
    String httpsProxyHostPref = "https.proxyHost";
    String httpsProxyHost; 

    JLabel httpsProxyPortLabel;
    PortTextField httpsProxyPortField;
    String httpsProxyPortPref = "https.proxyPort";
    String httpsProxyPort; 

    JLabel ftpProxyHostLabel;
    JTextField ftpProxyHostField;
    String ftpProxyHostPref = "ftp.proxyHost";
    String ftpProxyHost; 


    JLabel ftpProxyPortLabel;
    PortTextField ftpProxyPortField;
    String ftpProxyPortPref = "ftp.proxyPort";
    String ftpProxyPort; 


    JLabel ftpNonProxyHostsLabel;
    JTextField ftpNonProxyHostsField;
    String ftpNonProxyHostsPref = "ftp.nonProxyHosts";
    String ftpNonProxyHosts; 

    JLabel socksProxyHostLabel;
    JTextField socksProxyHostField;
    String socksProxyHostPref = "socksProxyHost";
    String socksProxyHost; 

    JLabel socksProxyPortLabel;
    PortTextField socksProxyPortField;
    String socksProxyPortPref = "socksProxyPort";
    String socksProxyPort; 

    void setPreferences() {
	// if (userPrefs == null) setPrefNodeName(ProxyInfo.DEFAULT_PREF_NODE);
	useSystemProxiesFlag = useSystemProxies.isSelected();
	userPrefs.putBoolean(useSystemProxiesPref, useSystemProxiesFlag);

	useHttpProxyFlag = useHttpProxy.isSelected();
	userPrefs.putBoolean(useHttpProxyPref, useHttpProxyFlag);

	useHttpsProxyFlag = useHttpsProxy.isSelected();
	userPrefs.putBoolean(useHttpsProxyPref, useHttpsProxyFlag);

	useFtpProxyFlag = useFtpProxy.isSelected();
	userPrefs.putBoolean(useFtpProxyPref, useFtpProxyFlag);

	httpProxyHost = httpProxyHostField.getText().trim();
	if (httpProxyHost == null || httpProxyHost.equals(emptyString)) {
	    userPrefs.remove(httpProxyHostPref);
	} else {
	    userPrefs.put(httpProxyHostPref, httpProxyHost);
	}

	httpProxyPort = httpProxyPortField.getText().trim();
	if (httpProxyPort == null || httpProxyPort.equals(emptyString)) {
	    userPrefs.remove(httpProxyPortPref);
	} else {
	    userPrefs.put(httpProxyPortPref, httpProxyPort);
	}

	httpNonProxyHosts = httpNonProxyHostsField.getText().trim();
	if (httpNonProxyHosts == null||httpNonProxyHosts.equals(emptyString)) {
	    userPrefs.remove(httpNonProxyHostsPref);
	} else {
	    userPrefs.put(httpNonProxyHostsPref, httpNonProxyHosts);
	}

	httpsProxyHost = httpsProxyHostField.getText().trim();
	if (httpsProxyHost == null || httpsProxyHost.equals(emptyString)) {
	    userPrefs.remove(httpsProxyHostPref);
	} else {
	    userPrefs.put(httpsProxyHostPref, httpsProxyHost);
	}

	httpsProxyPort = httpsProxyPortField.getText().trim();
	if (httpsProxyPort == null || httpsProxyPort.equals(emptyString)) {
	    userPrefs.remove(httpsProxyPortPref);
	} else {
	    userPrefs.put(httpsProxyPortPref, httpsProxyPort);
	}


	ftpProxyHost = ftpProxyHostField.getText().trim();
	if (ftpProxyHost == null || ftpProxyHost.equals(emptyString)) {
	    userPrefs.remove(ftpProxyHostPref);
	} else {
	    userPrefs.put(ftpProxyHostPref, ftpProxyHost);
	}

	ftpProxyPort = ftpProxyPortField.getText().trim();
	if (ftpProxyPort == null || ftpProxyPort.equals(emptyString)) {
	    userPrefs.remove(ftpProxyPortPref);
	} else {
	    userPrefs.put(ftpProxyPortPref, ftpProxyPort);
	}

	ftpNonProxyHosts = ftpNonProxyHostsField.getText().trim();
	if (ftpNonProxyHosts == null || ftpNonProxyHosts.equals(emptyString)) {
	    userPrefs.remove(ftpNonProxyHostsPref);
	} else {
	    userPrefs.put(ftpNonProxyHostsPref, ftpNonProxyHosts);
	}

	socksProxyHost = socksProxyHostField.getText().trim();
	if (socksProxyHost == null || socksProxyHost.equals(emptyString)) {
	    userPrefs.remove(socksProxyHostPref);
	} else {
	    userPrefs.put(socksProxyHostPref, socksProxyHost);
	}

	socksProxyPort = socksProxyPortField.getText().trim();
	if (socksProxyPort == null || socksProxyPort.equals(emptyString)) {
	    userPrefs.remove(socksProxyPortPref);
	} else {
	    userPrefs.put(socksProxyPortPref, socksProxyPort);
	}

    }


    void getPreferences() {
	if (useInfo) {
	    useSystemProxiesFlag = pinfo.useSystemProxies;

	    if (pinfo.configHttp) {
		useHttpProxyFlag = (pinfo.httpProxyHost != null
				    || pinfo.httpProxyPort != null);
	    } else {
		useHttpProxyFlag =
		    userPrefs.getBoolean(useHttpProxyPref, false);
	    }

	    if (pinfo.configHttps) {
		useHttpsProxyFlag = (pinfo.httpsProxyHost != null
				     || pinfo.httpsProxyPort != null);
	    } else {
		useHttpsProxyFlag =
		    userPrefs.getBoolean(useHttpsProxyPref, false);
	    }

	    if (pinfo.configFtp) {
		useFtpProxyFlag = (pinfo.ftpProxyHost != null
				   || pinfo.ftpProxyPort != null);
	    } else {
		useFtpProxyFlag = userPrefs.getBoolean(useFtpProxyPref, false);
	    }

	    if (pinfo.configSocks) {
		useSocksProxyFlag = (pinfo.socksProxyHost != null
				     || pinfo.socksProxyPort != null);
	    } else {
		useSocksProxyFlag =
		    userPrefs.getBoolean(useSocksProxyPref, false);
	    }

	    if (pinfo.configHttp) {
		httpProxyHost = (pinfo.httpProxyHost == null)? emptyString:
		    pinfo.httpProxyHost;
		httpProxyPort = (pinfo.httpProxyPort == null)? emptyString:
		    pinfo.httpProxyPort;
		httpNonProxyHosts = (pinfo.httpNonProxyHosts == null)?
		    emptyString: pinfo.httpNonProxyHosts;
	    } else {
		httpProxyHost = userPrefs.get(httpProxyHostPref, emptyString);
		httpProxyPort = userPrefs.get(httpProxyPortPref, emptyString);
		httpNonProxyHosts =
		    userPrefs.get(httpNonProxyHostsPref, emptyString);
	    }

	    if (pinfo.configHttps) {
		httpsProxyHost = (pinfo.httpsProxyHost == null)? emptyString:
		    pinfo.httpsProxyHost;
		httpsProxyPort = (pinfo.httpsProxyPort == null)? emptyString:
		    pinfo.httpsProxyPort;
	    } else {
		httpsProxyHost = userPrefs.get(httpsProxyHostPref, emptyString);
		httpsProxyPort = userPrefs.get(httpsProxyPortPref, emptyString);
	    }

	    if (pinfo.configFtp) {
		ftpProxyHost = (pinfo.ftpProxyHost == null)? emptyString:
		    pinfo.ftpProxyHost;
		ftpProxyPort = (pinfo.ftpProxyPort == null)? emptyString:
		    pinfo.ftpProxyPort;
		ftpNonProxyHosts = (pinfo.ftpNonProxyHosts == null)?
		    emptyString: pinfo.ftpNonProxyHosts;
	    } else {
		ftpProxyHost = userPrefs.get(ftpProxyHostPref, emptyString);
		ftpProxyPort = userPrefs.get(ftpProxyPortPref, emptyString);
		ftpNonProxyHosts =
		    userPrefs.get(ftpNonProxyHostsPref, emptyString);
	    }

	    if (pinfo.configSocks) {
		socksProxyHost = (pinfo.socksProxyHost == null)? emptyString:
		    pinfo.socksProxyHost;
		socksProxyPort = (pinfo.socksProxyPort == null)? emptyString:
		    pinfo.socksProxyPort;
	    } else {
		socksProxyHost = userPrefs.get(socksProxyHostPref, emptyString);
		socksProxyPort = userPrefs.get(socksProxyPortPref, emptyString);
	    }

	} else {
	    // if(userPrefs==null)setPrefNodeName(ProxyInfo.DEFAULT_PREF_NODE);
	    useSystemProxiesFlag =
		userPrefs.getBoolean(useSystemProxiesPref, false);
	    useHttpProxyFlag = userPrefs.getBoolean(useHttpProxyPref, false);
	    useHttpsProxyFlag = userPrefs.getBoolean(useHttpsProxyPref, false);
	    useFtpProxyFlag = userPrefs.getBoolean(useFtpProxyPref, false);
	    useSocksProxyFlag = userPrefs.getBoolean(useSocksProxyPref, false);

	    httpProxyHost = userPrefs.get(httpProxyHostPref, emptyString);
	    httpProxyPort = userPrefs.get(httpProxyPortPref, emptyString);
	    httpNonProxyHosts = 
		userPrefs.get(httpNonProxyHostsPref, emptyString);

	    httpsProxyHost = userPrefs.get(httpsProxyHostPref, emptyString);
	    httpsProxyPort = userPrefs.get(httpsProxyPortPref, emptyString);

	    ftpProxyHost = userPrefs.get(ftpProxyHostPref, emptyString);
	    ftpProxyPort = userPrefs.get(ftpProxyPortPref, emptyString);
	    ftpNonProxyHosts = 
		userPrefs.get(ftpNonProxyHostsPref, emptyString);

	    socksProxyHost = userPrefs.get(socksProxyHostPref, emptyString);
	    socksProxyPort = userPrefs.get(socksProxyPortPref, emptyString);
	}

	if (usingGUI) {
	    useSystemProxies.setSelected(useSystemProxiesFlag);
	    useHttpProxy.setSelected(useHttpProxyFlag);
	    useHttpsProxy.setSelected(useHttpsProxyFlag);
	    useFtpProxy.setSelected(useFtpProxyFlag);
	    useSocksProxy.setSelected(useSocksProxyFlag);

	    httpProxyHostField.setText(httpProxyHost);
	    httpProxyPortField.setText(httpProxyPort);
	    httpNonProxyHostsField.setText(httpNonProxyHosts);

	    httpsProxyHostField.setText(httpsProxyHost);
	    httpsProxyPortField.setText(httpsProxyPort);

	    ftpProxyHostField.setText(ftpProxyHost);
	    ftpProxyPortField.setText(ftpProxyPort);
	    ftpNonProxyHostsField.setText(ftpNonProxyHosts);

	    socksProxyHostField.setText(socksProxyHost);
	    socksProxyPortField.setText(socksProxyPort);
	}
    }

    private static void ourSetProperty(Properties props,
				       String key,
				       String value)
    {
	if (value == null || value.equals(emptyString)) {
	    props.remove(key);
	} else {
	    props.setProperty(key, value);
	}
    }

    Properties setupEnv(Properties props) {
	if (useSystemProxiesFlag) {
	    ourSetProperty(props, useSystemProxiesPref,"true" );
	} else {
	    ourSetProperty(props, useSystemProxiesPref, "false");
	}


	if (useHttpProxyFlag && !useSystemProxiesFlag) {
	    ourSetProperty(props, httpProxyHostPref, httpProxyHost);
	    ourSetProperty(props, httpProxyPortPref, httpProxyPort);
	} else {
	    ourSetProperty(props, httpProxyHostPref, null);
	    ourSetProperty(props, httpProxyPortPref, null);
	}

	if (useHttpsProxyFlag && !useSystemProxiesFlag) {
	    ourSetProperty(props, httpsProxyHostPref, httpsProxyHost);
	    ourSetProperty(props, httpsProxyPortPref, httpsProxyPort);
	} else {
	    ourSetProperty(props, httpsProxyHostPref, null);
	    ourSetProperty(props, httpsProxyPortPref, null);
	}

	if ((useHttpsProxyFlag || useHttpProxyFlag) && !useSystemProxiesFlag) {
	    ourSetProperty(props, httpNonProxyHostsPref, httpNonProxyHosts);
	} else {
	    ourSetProperty(props, httpNonProxyHostsPref, null);
	}

	if (useFtpProxyFlag && !useSystemProxiesFlag) {
	    ourSetProperty(props, ftpProxyHostPref, ftpProxyHost);
	    ourSetProperty(props, ftpProxyPortPref, ftpProxyPort);
	    ourSetProperty(props, ftpNonProxyHostsPref, ftpNonProxyHosts);
	} else {
	    ourSetProperty(props, ftpProxyHostPref, null);
	    ourSetProperty(props, ftpProxyPortPref, null);
	    ourSetProperty(props, ftpNonProxyHostsPref, null);
	}

	if (useSocksProxyFlag && !useSystemProxiesFlag) {
	    ourSetProperty(props, socksProxyHostPref, socksProxyHost);
	    ourSetProperty(props, socksProxyPortPref, socksProxyPort);
	} else {
	    ourSetProperty(props, socksProxyHostPref, null);
	    ourSetProperty(props, socksProxyPortPref, null);
	}


	return props;
    }

    GridBagConstraints c0 = new GridBagConstraints();
    GridBagConstraints c0a = new GridBagConstraints();
    GridBagConstraints c1 = new GridBagConstraints();
    GridBagConstraints c2 = new GridBagConstraints();
    GridBagConstraints c2c = new GridBagConstraints();
    GridBagConstraints c3 = new GridBagConstraints();

    GridBagConstraints c4a = new GridBagConstraints();
    GridBagConstraints c4b = new GridBagConstraints();
    GridBagConstraints c4c = new GridBagConstraints();
    
    private void initConstraints() {
	c0.weightx = 1.0;
	c0a.weightx = 1.0;
	c0a.anchor = GridBagConstraints.WEST;
	c1.fill = GridBagConstraints.BOTH;
	c2.fill = GridBagConstraints.BOTH;
	c1.weightx = 1.0;
	c2.gridwidth = GridBagConstraints.REMAINDER;
	c2c.gridwidth = GridBagConstraints.RELATIVE;
	c3.weightx = 0.0;
	c3.gridwidth = GridBagConstraints.REMAINDER;

	c4b.gridwidth = 2;



    }

    JPanel prefs = new JPanel();
    // JPanel prefs1 = new JPanel();
    // JScrollPane prefsScrollpane = new JScrollPane(prefs1);
    // JScrollPane prefsScrollpane;

    private void addComponent(JPanel panel, Component comp, 
			      GridBagLayout bag, 
			      GridBagConstraints c) {
	bag.setConstraints(comp, c);
	panel.add(comp);
    }


    ProxyInfo pinfo;
    boolean useInfo;
    Component toplevel;
    boolean usingGUI = true;

    ProxyComponent(ProxyInfo info) {
	// No-GUI initialization
	this.pinfo = info;
	useInfo = (pinfo != null) && !pinfo.usePreferences();
	usingGUI = false;
    }

    /**
     * Set proxies.
     * This is intended for cases in which a GUI will not be used.
     * @param info proxy-configuration data; null implies that no data is
     *        provided
     */
    public static void setProxies(ProxyInfo info) {
	// bare-bones operation - don't use the GUI.
	// setPrefNodeName((info == null)? null: info.getPrefNodeName());
	ProxyComponent c = new ProxyComponent(info);
	c.getPreferences();
	c.accept();
    }


    ProxyComponent(ProxyInfo info, Component toplevel) {
	this.pinfo = info;
	// useInfo = (pinfo != null) && !pinfo.usePreferences();
	// setPrefNodeName(pinfo == null? null: pinfo.getPrefNodeName());

	this.toplevel = toplevel;

	initConstraints();
	// createPrefPanel calls getPreferences and accept.
	createPrefPanel();
	setLayout(new BorderLayout());
	prefs.validate();
	add(prefs, "Center");
    }

    void doSetEnabled() {
	if (useSystemProxiesFlag) {
	    useHttpProxy.setEnabled(false);
	    useHttpsProxy.setEnabled(false);
	    useFtpProxy.setEnabled(false);
	    useSocksProxy.setEnabled(false);
	    httpProxyHostLabel.setEnabled(false);
	    httpProxyPortLabel.setEnabled(false);
	    httpsProxyHostLabel.setEnabled(false);
	    httpsProxyPortLabel.setEnabled(false);
	    httpNonProxyHostsLabel.setEnabled(false);
	    ftpProxyHostLabel.setEnabled(false);
	    ftpProxyPortLabel.setEnabled(false);
	    ftpNonProxyHostsLabel.setEnabled(false);
	    socksProxyHostLabel.setEnabled(false);
	    socksProxyPortLabel.setEnabled(false);
	    httpProxyHostField.setEnabled(false);
	    httpProxyPortField.setEnabled(false);
	    httpsProxyHostField.setEnabled(false);
	    httpsProxyPortField.setEnabled(false);
	    ftpProxyHostField.setEnabled(false);
	    ftpProxyPortField.setEnabled(false);
	    ftpNonProxyHostsField.setEnabled(false);
	    socksProxyHostField.setEnabled(false);
	    socksProxyPortField.setEnabled(false);
	} else {
	    useHttpProxy.setEnabled(true);
	    useHttpsProxy.setEnabled(true);
	    useFtpProxy.setEnabled(true);
	    useSocksProxy.setEnabled(true);
	    if (useHttpProxyFlag) {
		httpProxyHostLabel.setEnabled(true);
		httpProxyPortLabel.setEnabled(true);
		httpProxyHostField.setEnabled(true);
		httpProxyPortField.setEnabled(true);
	    } else {
		httpProxyHostLabel.setEnabled(false);
		httpProxyPortLabel.setEnabled(false);
		httpProxyHostField.setEnabled(false);
		httpProxyPortField.setEnabled(false);
	    }
	    if (useHttpsProxyFlag) {
		httpsProxyHostLabel.setEnabled(true);
		httpsProxyPortLabel.setEnabled(true);
		httpsProxyHostField.setEnabled(true);
		httpsProxyPortField.setEnabled(true);
	    } else {
		httpsProxyHostLabel.setEnabled(false);
		httpsProxyPortLabel.setEnabled(false);
		httpsProxyHostField.setEnabled(false);
		httpsProxyPortField.setEnabled(false);
	    }
	    if (useHttpProxyFlag || useHttpsProxyFlag) {
		httpNonProxyHostsLabel.setEnabled(true);
		httpNonProxyHostsField.setEnabled(true);
	    } else {
		httpNonProxyHostsLabel.setEnabled(false);
		httpNonProxyHostsField.setEnabled(false);
	    }
	    if (useFtpProxyFlag) {
		ftpProxyHostLabel.setEnabled(true);
		ftpProxyPortLabel.setEnabled(true);
		ftpNonProxyHostsLabel.setEnabled(true);
		ftpProxyHostField.setEnabled(true);
		ftpProxyPortField.setEnabled(true);
		ftpNonProxyHostsField.setEnabled(true);
	    } else {
		ftpProxyHostLabel.setEnabled(false);
		ftpProxyPortLabel.setEnabled(false);
		ftpNonProxyHostsLabel.setEnabled(false);
		ftpProxyHostField.setEnabled(false);
		ftpProxyPortField.setEnabled(false);
		ftpNonProxyHostsField.setEnabled(false);
	    }
	    if (useSocksProxyFlag) {
		socksProxyHostLabel.setEnabled(true);
		socksProxyPortLabel.setEnabled(true);
		socksProxyHostField.setEnabled(true);
		socksProxyPortField.setEnabled(true);
	    } else {
		socksProxyHostLabel.setEnabled(false);
		socksProxyPortLabel.setEnabled(false);
		socksProxyHostField.setEnabled(false);
		socksProxyPortField.setEnabled(false);
	    }
	}
    }


    void createPrefPanel() {
	prefs.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	GridBagLayout layout = new GridBagLayout();
	prefs.setLayout(layout);


	useSystemProxies = new JCheckBox(localeString("useSystemProxies"));
	addComponent(prefs, useSystemProxies, layout, c2);
	useSystemProxies.addActionListener(new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
		    useSystemProxiesFlag = useSystemProxies.isSelected();
		    doSetEnabled();
		}
	    });

	addComponent(prefs, new JLabel(" "), layout, c3);

	useHttpProxy = new JCheckBox(localeString("useHttpProxy"));
	addComponent(prefs, useHttpProxy, layout, c2);
	useHttpProxy.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    useHttpProxyFlag = useHttpProxy.isSelected();
		    doSetEnabled();
		}
	    });

	httpProxyHostLabel = new JLabel(localeString("httpProxyHostLabel"));
	addComponent(prefs, httpProxyHostLabel, layout, c1);
	httpProxyHostField = new VTextField(40) {
		protected void onAccepted() {
		    httpProxyHost = getText();
		}
	    };
	addComponent(prefs, httpProxyHostField, layout, c2);
	

	httpProxyPortLabel = new JLabel(localeString("httpProxyPortLabel"));
	addComponent(prefs, httpProxyPortLabel, layout, c1);
	httpProxyPortField = new PortTextField(40) {
		protected void onAccepted() {
		    httpProxyPort = getText();
		}
	    };
	addComponent(prefs, httpProxyPortField, layout, c2);

	addComponent(prefs, new JLabel(" "), layout, c3);

	useHttpsProxy = new JCheckBox(localeString("useHttpsProxy"));
	addComponent(prefs, useHttpsProxy, layout, c2);
	useHttpsProxy.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    useHttpsProxyFlag = useHttpsProxy.isSelected();
		    doSetEnabled();
		}
	    });

	httpsProxyHostLabel = new JLabel(localeString("httpsProxyHostLabel"));
	addComponent(prefs, httpsProxyHostLabel, layout, c1);
	httpsProxyHostField = new VTextField(40)  {
		protected void onAccepted() {
		    httpsProxyHost = getText();
		}
	    };
	addComponent(prefs, httpsProxyHostField, layout, c2);

	httpsProxyPortLabel = new JLabel(localeString("httpsProxyPortLabel"));
	addComponent(prefs, httpsProxyPortLabel, layout, c1);
	httpsProxyPortField = new PortTextField(40) {
		protected void onAccepted() {
		    httpsProxyPort = getText();
		}
	    };
	addComponent(prefs, httpsProxyPortField, layout, c2);

	addComponent(prefs, new JLabel(" "), layout, c3);

	httpNonProxyHostsLabel = 
	    new JLabel(localeString("httpNonProxyHostsLabel"));
	addComponent(prefs, httpNonProxyHostsLabel, layout, c1);
	httpNonProxyHostsField = 
	    new VTextField(40) {
		protected void onAccepted() {
		    httpNonProxyHosts = getText();
		}
	    };
	addComponent(prefs, httpNonProxyHostsField, layout, c2);

	addComponent(prefs, new JLabel(" "), layout, c3);

	useFtpProxy = new JCheckBox(localeString("useFtpProxy"));
	addComponent(prefs, useFtpProxy, layout, c2);
	useFtpProxy.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    useFtpProxyFlag = useFtpProxy.isSelected();
		    doSetEnabled();
		}
	});

	ftpProxyHostLabel = new JLabel(localeString("ftpProxyHostLabel"));
	addComponent(prefs, ftpProxyHostLabel, layout, c1);
	ftpProxyHostField = new VTextField(40) {
		protected void onAccepted() {
		    ftpProxyHost = getText();
		}
	    };
	addComponent(prefs, ftpProxyHostField, layout, c2);

	ftpProxyPortLabel = new JLabel(localeString("ftpProxyPortLabel"));
	addComponent(prefs, ftpProxyPortLabel, layout, c1);
	ftpProxyPortField = new PortTextField(40) {
		protected void onAccepted() {
		    ftpProxyPort = getText();
		}
	    };
	addComponent(prefs, ftpProxyPortField, layout, c2);

	ftpNonProxyHostsLabel = 
	    new JLabel(localeString("ftpNonProxyHostsLabel"));
	addComponent(prefs, ftpNonProxyHostsLabel, layout, c1);
	ftpNonProxyHostsField = new VTextField(40) {
		protected void onAccepted() {
		    ftpNonProxyHosts = getText();
		}
	    };
	addComponent(prefs, ftpNonProxyHostsField, layout, c2);

	addComponent(prefs, new JLabel(" "), layout, c3);

	useSocksProxy = new JCheckBox(localeString("useSocksProxy"));
	addComponent(prefs, useSocksProxy, layout, c2);
	useSocksProxy.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    useSocksProxyFlag = useSocksProxy.isSelected();
		    doSetEnabled();
		}
	});

	socksProxyHostLabel = new JLabel(localeString("socksProxyHostLabel"));
	addComponent(prefs, socksProxyHostLabel, layout, c1);
	socksProxyHostField = new VTextField(40) {
		protected void onAccepted() {
		    socksProxyHost = getText();
		}
	    };
	addComponent(prefs, socksProxyHostField, layout, c2);

	socksProxyPortLabel = new JLabel(localeString("socksProxyPortLabel"));
	addComponent(prefs, socksProxyPortLabel, layout, c1);
	socksProxyPortField = new PortTextField(40) {
		protected void onAccepted() {
		    socksProxyPort = getText();
		}
	    };
	addComponent(prefs, socksProxyPortField, layout, c2);

	addComponent(prefs, new JLabel(" "), layout, c3);
	

	JPanel bpanel = new JPanel();
	GridBagLayout blayout = new GridBagLayout();
	JButton saveButton = new JButton(localeString("saveButton"));
	addComponent(bpanel, saveButton, blayout, c4a);
	saveButton.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    setPreferences();
		    ProxyComponent.this.accept();
		    useInfo = false;
		    if (toplevel != null) toplevel.setVisible(false);
		}
	    });

	JButton acceptButton = new JButton(localeString("acceptButton"));
	addComponent(bpanel, acceptButton, blayout, c4a);
	acceptButton.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    ProxyComponent.this.accept();
		    if (toplevel != null) toplevel.setVisible(false);
		    
		}
	    });

	JButton cancelChanges = new JButton(localeString("cancelChanges"));
	addComponent(bpanel, cancelChanges, blayout, c4a);
	cancelChanges.addActionListener(new AbstractAction () {
		public void actionPerformed(ActionEvent e) {
		    getPreferences();
		    if (toplevel != null) toplevel.setVisible(false);
		}
	    });

	addComponent(prefs, bpanel, layout, c4b);

	getPreferences();
	accept();
	doSetEnabled();
    }

    /**
     * Create a dialog box containing a proxy component.
     * @param immediate a descendant (or child) of the Frame or Dialog
     *        that owns the dialog to be created
     * @param title the string to display in the dialog's title bar
     * @param modal specifies whether dialog blocks user input to other 
     *        top-level windows when shown. If true, the modality type
     *        property is set to Dialog.DEFAULT_MODALITY_TYPE, otherwise 
     *        the dialog is modeless 
     * @param object either the previous ProxyDialog that was created or
     *        a ProxyInfo object defining how the proxies should be configured
     *        with null indicating the values stored as preferences under the
     *        node whose name is ProxyInfo.DEFAULT_PREF_NODE
     */
    static public ProxyDialog createDialog(Container immediate,
					   String title,
					   boolean modal,
					   Object object) {
	ProxyDialog oldDialog = null;
	ProxyInfo info = null;
	if (object instanceof ProxyDialog) {
	    oldDialog = (ProxyDialog) object;
	} else {
	    info = (ProxyInfo) object;
	}
	Container child = immediate;
	Container parent = child.getParent();
	while (parent != null) {
	    child = parent;
	    parent = child.getParent();
	}
	parent = child;
	ProxyDialog dialog;
	if (parent instanceof java.awt.Frame) {
	    dialog = new ProxyDialog((java.awt.Frame)parent, title, modal);
	} else if (parent instanceof java.awt.Dialog) {
	    dialog = new ProxyDialog((java.awt.Dialog)parent, title, modal);
	} else {
	    throw new IllegalArgumentException
		(MessageFormat.format(bundle.getString("illegalParent"),
				      parent.toString()));
	}
	final ProxyComponent c = (oldDialog != null)?
	    oldDialog.getProxyComponent(): new ProxyComponent(info, dialog);
	if (oldDialog != null) oldDialog.remove(c);
	dialog.addProxyComponent(c);
	dialog.setResizable(false);
	dialog.pack();
	dialog.setVisible(false);
	dialog.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    // treat as if we canceled.
		    c.getPreferences();
		    if (c.toplevel != null) c.toplevel.setVisible(false);
		}
	    });
	return dialog;
    }

    void accept() {
	System.setProperties(setupEnv((Properties)origProperties.clone()));
    }
}

//  LocalWords:  JEditorPane sresource prefname ProxyInfo useFtpProxy
//  LocalWords:  useHttpProxy useHttpsProxy useSocksProxy http https
//  LocalWords:  proxyHost proxyPort nonProxyHosts socksProxyHost
//  LocalWords:  socksProxyPort userPrefs setPrefNodeName JPanel
//  LocalWords:  prefs JScrollPane prefsScrollpane getPrefNodeName
//  LocalWords:  useInfo pinfo usePreferences createPrefPanel
//  LocalWords:  getPreferences useSystemProxies httpProxyHostLabel
//  LocalWords:  httpProxyPortLabel httpsProxyHostLabel saveButton
//  LocalWords:  httpsProxyPortLabel httpNonProxyHostsLabel dialog's
//  LocalWords:  ftpProxyHostLabel ftpProxyPortLabel acceptButton
//  LocalWords:  ftpNonProxyHostsLabel socksProxyHostLabel modeless
//  LocalWords:  socksProxyPortLabel cancelChanges ProxyDialog
//  LocalWords:  illegalParent
