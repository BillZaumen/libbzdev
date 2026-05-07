package org.bzdev.ejws;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class GPGSecureBasicUT
    extends EjwsUserTable<EjwsSecureBasicAuth,EjwsSecureBasicAuth.Entry>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void
	loadEntriesFromDB(Map<String,EjwsSecureBasicAuth.Entry> map)
    {
	EjwsSecureBasicAuth auth = getAuthenticator();
	auth.loadFromDirs();
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEntryFromDB(String email) throws Exception
    {
	if (true) {System.out.println("createEntryFromDB: " + email); return;}
	EjwsSecureBasicAuth auth = getAuthenticator();
	boolean signed = auth.validGPGUser(email);
	if (signed) {
	    String recipients[] = {email};
		String uriString = auth.generateRequestURI(null);
		String alias = auth.getLoginAlias();
		auth.createUser(email, uriString, recipients, null)
		    .setURI(alias)
		    .setActive(true)
		    .addUser(true);
	} else {
	    throw new Exception("cannot create user:" + email);
	}
	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EjwsSecureBasicAuth.Entry
	getEntryFromDB(EjwsSecureBasicAuth.Entry currentEntry, String user)
	throws Exception
    {
	throw new UnsupportedOperationException();
    }

    @Override
    protected void makeActiveInDB(String user) throws Exception {
	if (true) {System.out.println("... makeActiveInDB: " + user); return;}
	EjwsSecureBasicAuth auth = getAuthenticator();
	if (!auth.signKey(user, auth.hasGPGKey(user))) {
	    throw new Exception("cannot sign key for " + user);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeEntryFromDB(String user) {
	if (true){System.out.println("... removeEntryFromDB: " + user); return;}
	EjwsSecureBasicAuth auth = getAuthenticator();
	String fpr = auth.getFingerprint(user);
	if (fpr != null) {
	    auth.deleteWithFingerprint(fpr);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean hasDB() {
	return (getAuthenticator().gpghome() != null);
    }


}
