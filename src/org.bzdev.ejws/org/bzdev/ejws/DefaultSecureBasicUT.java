package org.bzdev.ejws;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class DefaultSecureBasicUT
    extends EjwsUserTable<EjwsSecureBasicAuth,EjwsSecureBasicAuth.Entry>
{

    /**
     * Constructor.
     */
    public DefaultSecureBasicUT() {
	super();
    }


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
    protected void makeActiveInDB(String user) throws Exception {
	EjwsSecureBasicAuth auth = getAuthenticator();
	SBLStore store = auth.getSBLStore();
	if (auth.hasGPGKey(user)) {
	    if (auth.validGPGUser(user) == false) {
		auth.signKey(user, true);
	    }
	} else if (store != null) {
	    if (store.containsUser(user)) {
		store.makeActive(user);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEntryFromDB(String email)
	throws Exception
    {
	if (getEntry(email) != null) {
	    throw new IllegalStateException("entry exists: " + email);
	}
	EjwsSecureBasicAuth auth = getAuthenticator();
	String alias = auth.getLoginAlias();
	String uriString = auth.generateRequestURI(null);
	
	SBLStore store = auth.getSBLStore();
	if (auth.gpghome() != null && auth.hasGPGKey(email)) {
	    boolean signed = auth.validGPGUser(email);
	    String recipients[] = {email};
	    if (signed) {
		auth.createUser(email, uriString, recipients, null)
		    .setURI(alias)
		    .setActive(true)
		    .addUser(true);
	    } else {
		auth.createUser(email, uriString, recipients, null)
		    .setURI(alias)
		    .setActive(false)
		    .addUser(true);
	    }
	} else if (store != null && store.containsUser(email)) {
	    String data = store.getSBLData(email);
	    boolean pwmode = (data != null);
	    if (pwmode == false) {
		data = store.getPWData(email);
	    }
	    boolean active = store.isActive(email);
	    if (pwmode) {
		auth.add(email, data);
		getEntry(email).setActive(active);
	    } else {
		auth.createUser(data, null)
		    .setActive(active)
		    .addUser();
	    }
	}
	boolean signed = auth.validGPGUser(email);
	if (signed) {
	    String recipients[] = {email};
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void
	copyEntryToDB(String User,
		      EjwsSecureBasicAuth.Entry entry,
		      boolean newEntry)
    {
	// Nothing to do 
	return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeEntryFromDB(String user) throws Exception {
	EjwsSecureBasicAuth auth = getAuthenticator();
	boolean status = false;
	SBLStore store = auth.getSBLStore();
	if (auth.gpghome() != null && auth.hasGPGKey(user)) {
	    String fpr = auth.getFingerprint(user);
	    if (fpr != null) {
		auth.deleteWithFingerprint(fpr);
	    }
	} else if (store != null) {
	    if (store.containsUser(user)) {
		store.removeUser(user);
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    public void storeGPGKey(String value, EjwsAuthenticator.GPGKeyIDs keyids)
	throws IllegalArgumentException, IllegalStateException, IOException
    {
	EjwsSecureBasicAuth auth = getAuthenticator();
	auth.storeGPGKey(value, keyids);
    }

    /**
     * {@inheritDoc}
     */
    public void storeSBLData(String s, EjwsAuthenticator.AddStatus status)
	throws Exception
    {
	EjwsSecureBasicAuth auth = getAuthenticator();
	auth.storeSBLData(s, status);
    }


    /**
     * {@inheritDoc}
     */
    public void storePW(String un, String pw, boolean isActive)
	throws Exception
    {
	EjwsSecureBasicAuth auth = getAuthenticator();
	SBLStore store = auth.getSBLStore();
	store.append(un, true, pw, isActive);
    }
}
