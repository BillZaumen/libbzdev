package org.bzdev.gio;

import org.bzdev.gio.spi.OSGProvider;
import java.util.HashSet;

/**
 * OSG Provider for Postscript.
 * This class is needed by a service loader (and hence has to be
 * declared to be public) and should not be used for other purposes:
 * the method {@link #getOsgClass()}, for example, returns a class
 * that is a member of the current package but that is not a public
 * class.
 */
public class OSGPostscriptProvider implements OSGProvider {

    static String types[] = {"ps", "PS"};
    static String suffixes[] = {"ps", "PS"};
    static String mediaType = "application/postscript";
    static Class<PostscriptGraphics> clazz = PostscriptGraphics.class;

    static HashSet<String> tset = new HashSet<>();

    static {
	for (String s: types) {
	    tset.add(s);
	}
    }

    @Override
    public String[] getTypes() {
	return types.clone();
    }

    @Override
    public String[] getSuffixes(String type) {
	return tset.contains(type)? suffixes.clone(): null;
    }

    @Override
    public String getMediaType(String type) {
	return tset.contains(type)? mediaType: null;
    }

    @Override
    public Class<PostscriptGraphics> getOsgClass() {
	return clazz;
    }
}
