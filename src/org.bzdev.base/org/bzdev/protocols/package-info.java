/**
 * Package providing additional protocols handlers.
 * <P>
 * This package contains a class,{@link org.bzdev.protocols.Handlers},
 *  that will load additional protocol handlers.
 * <P>
 * The protocol handlers are initialized by calling
 * {@link org.bzdev.protocols.Handlers#enable() Handlers.enable()},
 * which enables protocols defined in subpackages of
 * org.bzdev.protocols by a class named Handler in those subpackages,
 * the names of which must match the protocol portion of a URL.  These
 * Handler classes must extend java.net.URLStreamHandler.
 * Protocol-handler specific properties can be set by calling
 * {@link org.bzdev.protocols.Handlers#setProperty(String,String) Handlers.setProperty}.
 * Alternatively, before calling Handlers.enable(), a system property,
 * whose name is the concatenation "org.bzdev.protocols." and the
 * protocol-handler specific property (the first argument for the method
 * {@link org.bzdev.protocols.Handlers#setProperty(String,String)},
 *  can be set either by calling System.setProperty or by using the
 * "-D" option to the java command.
 * <p>
 * The protocols supported are the following:
 * <dl>
 * <dt><B>resource</B></dt><dd>A URL of the form resource:PATH[#FRAGMENT]
 * (where PATH starts with a "/") accesses a resource that can be
 * found either by using the system class loader or by searching a
 * a path of jar files and directories specified by the Handlers
 * property <code>resource.path</code>. (The corresponding system
 * property is <code>org.bzdev.protocols.resource.path</code>.)
 * The FRAGMENT component, if it exists, is useful when the resource
 * uses HTML format or some other data format for which fragment
 * identifiers are recognized.  The format of the Handlers property
 * <code>resource.path</code> is a list of jar files and directories
 * separated by the path-separator character. In this list, the
 * component "$classpath" indicates that the class path should be
 * searched, allowing the search order to be controlled precisely.
 * If this property is not defined, the class path will be searched
 * for the resource. Otherwise "$classpath" must appear explicitly
 * if the class path is to be searched.
 * </dd>
 * <dt><B>sresource</B></dt><dd>A URL of the form sresource:PATH[#FRAGMENT]
 * (where PATH starts with a "/") accesses a resource that can be found using
 * the system class loader.  If present, the FRAGMENT component is useful 
 * when the resource uses HTML format or some other data format for which
 * fragment identifiers are recognized.
 * </dd>
 * </dl>
 * The <code>resource</code> and <code>sresource</code> protocols are useful
 * when a URL references an object that is part of an application's code base
 * or in related files or directories.
 */
package org.bzdev.protocols;

//  LocalWords:  subpackages setProperty dl dt classpath sresource
