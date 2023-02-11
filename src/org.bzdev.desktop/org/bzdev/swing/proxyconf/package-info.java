/**
 * Proxy configuration classes.
 * <P>
 * Java provides several methods for configuring proxies, with
 * a full discussion available in a document entitled 
 * <A href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html"> Java Networking and Proxies</A>.
 * The default procedure for configuring proxies to set system
 * properties, either on the command line via the java command's -D
 * option, or by calling <code>System.setProperty</code> as described
 * in the documentation cited above. The classes in this package provide
 * a GUI component that configures these properties for the user, with
 * the capability of storing the values persistently as Java preferences.
 * <P>
 * The resources org/bzdev/swing/proxyconf/lpack/ProxyComponent.html and
 * the 'dark mode' equivalent
 * org/bzdev/swing/proxyconf/lpack/ProxyComponent.dm.html
 * contain HTML pages that can be used for documentation (these pages use
 * HTML 3.2 because that is the version supported by a JEditorPane).
 * Both pages depend on the sresource URL scheme, which indicates that
 * the resource is located on the application's class path.  If used,
 * <code>org.bzdev.protocols.Handlers.enable()</code> must be called.

 * @see org.bzdev.protocols.Handlers
 * @see org.bzdev.protocols.Handlers#enable()
 */
package org.bzdev.swing.proxyconf;

//  LocalWords:  href JEditorPane sresource
