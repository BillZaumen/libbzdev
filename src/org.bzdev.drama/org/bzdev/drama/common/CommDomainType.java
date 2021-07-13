package org.bzdev.drama.common;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Class representing a communication-domain type.
 * Each communication domain has a communication-domain type that is
 * set when the domain is constructed or initialized as a communication
 * domain.  The communication-domain type of a communication domain
 * cannot be changed afterwards.  When searching for domains that allow
 * two message recipients (e.g., actors and groups) to communicate,
 * one can provide a set of the communication domains that may be used
 * and only communication domains of those types will be considered in
 * the search.
 */
public class CommDomainType {
    String name;

    public String getName() {return name;}

    CommDomainType(String name) {
	this.name = name;
    }

    static Map<String,CommDomainType>map = new HashMap<String,CommDomainType>();

    /**
     * Find an instance of CommDomainType given its name.
     * @param name the type's name
     * @return the CommDomainType corresponding to a name
     */
    public static synchronized CommDomainType findType(String name) {
	if (name == null) return null;
	CommDomainType result = map.get(name);
	if (result == null) {
	    result = new CommDomainType(name);
	    map.put(name, result);
	}
	return result;
    }

    /**
     * Get a set of the communication-domain types.
     * @param names the names of the communication-domain types that will
     *        be included in the set returned
     *@return a set of communication domain types
     */
    public static synchronized Set<CommDomainType> typeSet(String... names) {
	if (names.length == 0) return null;
	HashSet<CommDomainType> result = new HashSet<>(2 * names.length);
	for (String name: names) {
	    result.add(findType(name));
	}
	return Collections.unmodifiableSet(result);
    }
}

//  LocalWords:  CommDomainType
