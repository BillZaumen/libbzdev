package org.bzdev.ejws;
import com.sun.net.httpserver.*;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * Class representing an embedded web-server principal.
 * This class extends {@link HttpPrincipal} by adding a set of roles.
 * For {@link HttpPrincipal}, an identity is a user name in a name space
 * called a "realm". The method {@link HttpPrincipal#getName()} will
 * return the realm and the user name separated by a colon. The method
 * {@link HttpPrincipal#getUsername()} returns the user name alone.
 * A common convention is for the user name to be an email address as this
 * typically identifies a user uniquely.
 * <P>
 * Web servers use realms so that multiple independent web sites or
 * web applications running on the same server can have their own
 * name space and passwords for identifying users. Frequently, in an
 * embedded web server, there will be only a single realm.
 * <P>
 * The EJWS package adds roles, which typically would be used to denote
 * various capabilities. It is useful when EJWS is used with a database
 * that supports SQL roles. Also, the
 * <A HREF="https://jakarta.ee/specifications/servlet/4.0/apidocs/">
 * Jakarta Servlet API Documentation</A> contains a method (isUserInRole)
 * that can determine if a user has been assigned a specified role, so
 * supporting roles is useful if code has to migrate from an EJWS server
 * to a full-featured web server that supports Servlets. Keep in mind that
 * the Servlet specification places some restrictions on role names. These
 * are not checked by this class or other EJWS classes.
 */
public class EjwsPrincipal extends HttpPrincipal {

    Set<String> roles;

    /**
     * Constructor.
     * @param username the user name
     * @param realm the HTTP realm
     */
    public EjwsPrincipal(String username, String realm) {
	this(username, realm, null);
    }

    /**
     * Constructor based on another principal.
     * @param p the original principal
     * @param roles the roles for this principal
     */
    public EjwsPrincipal(HttpPrincipal p, Set<String> roles) {
	this(p.getUsername(), p.getRealm(), roles);
    }


    /**
     * Constructor specifying roles.
     * @param username the user name
     * @param realm the HTTP realm
     * @param roles the roles for this user
     */
    public EjwsPrincipal(String username, String realm, Set<String>roles) {
	super(username, realm);
	roles = (roles == null)? Collections.emptySet():
	    Collections.unmodifiableSet(new HashSet<String>(roles));
    }

    @Override
    public String getName() {
	// There is an error in openjdk 11 in which getName()
	// returns the same string as getUsername() while the
	// javadoc API documentation claims it should return the
	// realm and the user name separated by a colon.
	// This method  was implemented to fix the problem.
	String realm = getRealm();
	if (realm == null) return super.getName();
	return realm + ":" + getUsername();
    }

    /**
     * Get the roles for this principal.
     * @return the roles (set at the time this object was created)
     */
    public Set<String> getRoles() {return roles;}

}

//  LocalWords:  HttpPrincipal getName getUsername EJWS HREF Servlet
//  LocalWords:  isUserInRole Servlets openjdk javadoc
