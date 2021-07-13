package org.bzdev.ejws;

import com.sun.net.httpserver.*;
import java.util.*;

/**
 * Implementation of BasicAuthenticator using an in-memory table
 * of user names and passwords.
 */
public class EjwsBasicAuthenticator extends BasicAuthenticator {
    static class Entry {
	String pw;
	Set<String> roles;
	Entry(String pw, Set<String>roles) {
	    this.pw = pw; this.roles = roles;
	}
    }

    Map<String,Entry>map = new HashMap<String,Entry>();

    /**
     * Constructor.
     * @param realm the HTTP realm
     */
    public EjwsBasicAuthenticator(String realm) {
	super(realm);
    }

    /**
     * Add a user name and password for this authenticator's HTTP realm.
     * @param username the user name
     * @param password the password
     */
    public void add(String username, String password) {
	map.put(username, new Entry(password, null));
    }

    /**
     * Add a user name, the user's password and the user's roles for
     * this authenticator's HTTP realm.
     * @param username the user name
     * @param password the user's password
     * @param roles the user's roles
     */
    public void add(String username, String password, Set<String> roles ) {
	map.put(username, new Entry(password, roles));
    }

    @Override
    public Authenticator.Result authenticate(HttpExchange t) {
	Authenticator.Result result = super.authenticate(t);
	if (result instanceof Authenticator.Success) {
	    HttpPrincipal p = ((Authenticator.Success)result).getPrincipal();
	    String un = p.getUsername();
	    return new
		Authenticator.Success(new EjwsPrincipal(p, map.get(un).roles));
	} else {
	    return result;
	}
    }

    /**
     * Check credentials.
     * This method is called for each incoming request to verify
     * the given name and password in the context of this Authenticator's realm.
     * @param username the user name
     * @param password the password
     */
    @Override
    public boolean checkCredentials(String username, String password) {
	return password.equals(map.get(username).pw);
    }
}

//  LocalWords:  BasicAuthenticator username
