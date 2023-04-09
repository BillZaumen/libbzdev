package org.bzdev.ejws;
import java.util.Map;
import java.util.HashMap;

import org.bzdev.net.HttpSessionOps;


/**
 *  Ejws immplenation of the HttpSessionOps interface
 */
public class EjwsStateTable implements HttpSessionOps {

    Map<String,Object> map;

    /**
     * Constructor.
     */
    public EjwsStateTable() {
	map = new HashMap<String,Object>();
    }

    /**
     * Constructor specifying an internal table size.
     * @param size the table size
     */
    public EjwsStateTable(int size) {
	map = new HashMap<String,Object>(size);
    }

    @Override
    public void remove(String sid) {
	map.remove(sid);
    }

    @Override
    public void put(String sid, Object state) {
	map.put(sid, state);
    }

    @Override
    public void rename(String oldID, String newID)
	throws IllegalStateException
    {
	if (!map.containsKey(oldID) || map.containsKey(newID)) {
	    throw new IllegalStateException();
	}
	Object state = map.remove(oldID);
	map.put(newID, state);
    }


    @Override
    public boolean contains(String sid) {
	return map.containsKey(sid);
    }

    @Override
    public Object get(String sid) {
	return map.get(sid);
    }

}
