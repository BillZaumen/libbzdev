package org.bzdev.gio;

import org.bzdev.gio.spi.OSGProvider;
import org.bzdev.imageio.ImageMimeInfo;

import java.util.Set;

/**
 * OSG Provider for image formats.
 * This class is needed by a service loader (and hence has to be
 * declared to be public) and should not be used for other purposes:
 * the method {@link #getOsgClass()}, for example, returns a class
 * that is a member of the current package but that is not a public
 * class.
 * The image formats supported are those supported by the
 * org.bzdev.imageio package, which provides some convenience
 * methods for using the javax.imageio package.
 */
public class OSGImageProvider implements OSGProvider {
    @Override
    public String[] getTypes() {
	Set<String> names = ImageMimeInfo.getFormatNames();
	return names.toArray(new String[names.size()]);
    }

    @Override
    public String[] getSuffixes(String type) {
	String mediaType = ImageMimeInfo.getMimeType(type);
	if (mediaType == null) return null;
	String prefSuffix = ImageMimeInfo.getExtensionForMimeType(mediaType);
	String[] suffixes = ImageMimeInfo.getSuffixes(mediaType);
	if (!suffixes[0].equals(prefSuffix)) {
	    for (int i = 0; i < suffixes.length; i++) {
		if (suffixes[i].equals(prefSuffix)) {
		    suffixes[i] = suffixes[0];
		    suffixes[0] = prefSuffix;
		    break;
		}
	    }
	}
	return suffixes;
    }
    @Override
    public String getMediaType(String type) {
	return ImageMimeInfo.getMimeType(type);
    }

    @Override
    public Class<ImageGraphics> getOsgClass() {
	return ImageGraphics.class;
    }
}
