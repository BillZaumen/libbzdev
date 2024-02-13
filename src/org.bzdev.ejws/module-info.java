/**
 * Module providing an embedded Java-based web server.
 * <P>
 * This module simplifies the use of the module jdk.httpserver by
 * providing handlers for specific purposes and by creating a
 * simplified API for setting up a web server.  It allows web pages to
 * be stored in JAR-file resources in order to simplify the
 * deployment of applications that can accessed via a web browser.
 * <P><B>
 * Please see <A HREF="org/bzdev/ejws/doc-files/description.html">the EJWS package description</A>
 * for a detailed description of this package.</B>
 */
module org.bzdev.ejws {
    exports org.bzdev.ejws;
    exports org.bzdev.ejws.maps;
    requires java.base;
    requires java.desktop;
    requires java.xml;
    requires jdk.httpserver;
    requires org.bzdev.base;
    uses org.bzdev.ejws.CertManager;
}

//  LocalWords:  jdk httpserver HREF EJWS
