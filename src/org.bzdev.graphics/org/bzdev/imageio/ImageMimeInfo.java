package org.bzdev.imageio;
import java.util.*;
import javax.imageio.*;
import java.io.File;

/**
 * Information about MIME types for writable images handled by
 * javax.imageio classes.
 * The methods are all static.
 * <P>
 * Note: The term "MIME type" is the historic one. Current IETF usage
 * has replaced this with "media type".
 */
public class ImageMimeInfo {

    static Map<String,String> fmtmap = new HashMap<String,String>();
    static Map<String,String> extmap = new HashMap<String,String>();
    static Map<String,String[]> suffixmap = new HashMap<String,String[]>();
    static Map<String,String> suffixToMType = new HashMap<String,String>();

    static Set<String> preferredNameSet = new HashSet<String>();
    static Map<String,String> mtypeFromName = new HashMap<String,String>();


    /**
     * Get the MIME type for a suffix.
     * The suffix does not include the period separating it from the
     * rest of a file name.  The mapping is not case sensitive. The
     * rationale for not distinguishing between upper and lower case
     * characters is that image file are sometimes imported from
     * external sources (e.g., a digital camera) that may use
     * a different convention regarding the case of file names.
     * @param suffix the suffix
     * @return the MIME type; null if the suffix is not recognized
     *         as one used for image files
     */
    public static String getMIMETypeForSuffix(String suffix) {
	if (suffix == null) return null;
	return suffixToMType.get(suffix.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the entry set mapping mime types to the corresponding suffixes.
     * @return the entry set
     */
    public static Set<Map.Entry<String,String[]>> getMimeToSuffixesEntrySet() {
	return suffixmap.entrySet();
    }

    /**
     * Get the number of suffixes
     * @return the number of suffixes
     */
    public static int numberOfSuffixes() {
	return suffixToMType.size();
    }

    /**
     * Get a set of Map.Entry&lt;String,String&gt; containing each
     * entry in the suffix to MIME type map.
     * @return a set of of Map.Entry&lt;String,String&gt; representing the
     *         entries in the suffix to MIME type map.
     */
    public static Set<Map.Entry<String,String>> getSuffixEntrySet() {
	return suffixToMType.entrySet();
    }

    /**
     * Get a set of all suffixes for writable formats.
     * @return the set of file suffixes
     */
    public static Set<String> getSuffixSet() {
	return suffixToMType.keySet();
    }

    /**
     * Get the file extension matching a MIME type.
     * @param mtype the MIME type
     * @return the file extension
     */
    static public String getExtensionForMimeType(String mtype) {
	return extmap.get(mtype);
    }

    /**
     * Get the extension for a file.
     * @param file the file
     * @return the file extension; null if there is no extension;
     *         an empty string if the file name ends in a period
     */
    public static String getFilenameExtension(File file) {
	String filename = file.getName();
	int index = filename.lastIndexOf('.');
	return (index == -1 || index+1 == filename.length())? null:
	    filename.substring(index+1);
    }

    /**
     * Get the extension for a file name.
     * @param filename the filename
     * @return the file extension; null if there is no extension;
     *         an empty string if the file name ends in a period
     */
    public static String getFilenameExtension(String filename) {
	return getFilenameExtension(new File(filename));
    }

    /**
     * Get the format name given a file.
     * The format name is determined by the file's extension.
     * @param file the file
     * @return the format name for the file; null if there is none
     *         or if the format is not recognized by Java
     */
    public static String getFormatNameForFile(File file) {
	String ext = getFilenameExtension(file);
	if (ext == null) return null;
	String mt = getMIMETypeForSuffix(ext);
	if (mt == null) return null;
	return getFormatNameForMimeType(mt);
    }


    /**
     * Get the format name given a file.
     * The format name is determined by the file's extension.
     * @param filename  the file name
     * @return the format name for the file; null if there is none
     *         or if the format is not recognized by Java
     */
    public static String getFormatNameForFile(String filename) {
	String ext = getFilenameExtension(filename);
	if (ext == null) return null;
	String mt = getMIMETypeForSuffix(ext);
	if (mt == null) return null;
	return getFormatNameForMimeType(mt);
    }

    /**
     * Get the format name given a file-name extension or suffix.
     * @param suffix the suffix or file-name extension
     * @return the format name; null if there is none
     *         or if the format is not recognized by Java
     */
    public static String getFormatNameForSuffix(String suffix) {
	String mt = getMIMETypeForSuffix(suffix);
	if (mt == null) return null;
	return getFormatNameForMimeType(mt);
    }

    static {
	boolean ok1 = false;
	Iterator<ImageWriter>it;
	Map<Class,String> cmap = new HashMap<Class,String>();
	Map<String,String> preferred = new HashMap<String,String>();
	preferred.put("image/bmp", "bmp");
	preferred.put("image/jpeg", "jpg");
	preferred.put("image/png", "png");
	preferred.put("image/gif", "gif");
	Map<String,String> fpreferred = new HashMap<String,String>();
	fpreferred.put("image/bmp", "bmp");
	fpreferred.put("image/jpeg", "jpeg");
	fpreferred.put("image/png", "png");
	fpreferred.put("image/gif", "gif");

	
	Map<String,LinkedList<String>> smap =
	    new HashMap<String, LinkedList<String>>();
	for(String mt: ImageIO.getWriterMIMETypes()) {
	    it = ImageIO.getImageWritersByMIMEType(mt);
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    if (preferred.containsKey(mt)) {
			cmap.put(c, mt);
		    } else if (!cmap.containsKey(c)) {
			cmap.put(c, mt);
		    }

		}
	    }
	}
	for (String name: ImageIO.getWriterFormatNames()) {
	    it = ImageIO.getImageWritersByFormatName(name);
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    String mt = cmap.get(c);
		    if (mt != null) {
			String s = fpreferred.get(mt);
			if (fmtmap.get(mt) == null || name.equals(s)) {
			    fmtmap.put(mt, name);
			    preferredNameSet.add(name);
			    mtypeFromName.put(name, mt);
			} 
			if (mtypeFromName.get(name) == null) {
			    mtypeFromName.put(name, mt);
			}
		    }
		}
	    }
	}
	for (String name: ImageIO.getWriterFormatNames()) {
	    if (!preferredNameSet.contains(name)) {
		preferredNameSet.add(name);
	    }
	}
	for (String ext: ImageIO.getWriterFileSuffixes()) {
	    it = ImageIO.getImageWritersBySuffix(ext);
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    String mt = cmap.get(c);
		    if (mt != null) {
			String s = preferred.get(mt);
			if (extmap.get(mt) == null ||
			    ext.equals(s)) {
			    extmap.put(mt, ext);
			}
		    }
		}
		it = ImageIO.getImageWritersBySuffix(ext);
	    }
	    if (it != null) {
		while (it.hasNext()) {
		    Class c = it.next().getClass();
		    String mtype = cmap.get(c);
		    if (mtype == null) continue;
		    LinkedList<String> list = smap.get(mtype);
		    if (list == null) {
			list = new LinkedList<String>();
			smap.put(mtype,list);
		    }
		    list.add(ext);
		    suffixToMType.put(ext.toLowerCase(Locale.ENGLISH), mtype);
		}
	    }
	}

	for (Map.Entry<String,LinkedList<String>> entry: smap.entrySet()) {
	    String mt = entry.getKey();
	    LinkedList<String> list = entry.getValue();
	    suffixmap.put(mt, list.toArray(new String[list.size()]));
	}
    }

    /**
     * Get all suffixes (file extensions) associated with Java-supported
     * images that can be used for writing files.
     * @return a set of file name suffixes
     */
    public static String[] getAllExt() {
	return ImageIO.getWriterFileSuffixes();
    }



    /**
     * Determine if a MIME type is supported.
     * @param mimeType a MIME type
     * @return true if the MIME type is supported by javax.imageio operations;
     *         false otherwise
     */
    public static boolean supportsMIMEType(String mimeType) {
	return (suffixmap.get(mimeType) != null);
    }

    /**
     * Get the javax.imageio format name matching a MIME type.
     * @param mimeType a MIME type
     * @return a standard javax.imageio format name matching the MIME type
     */
    public static String getFormatNameForMimeType(String mimeType) {
	return fmtmap.get(mimeType);
    }

    /**
     * Get the suffixes matching a MIME type.
     * @param mimeType a MIME type
     * @return the suffixes for the MIME type
     */
    public static String[] getSuffixes(String mimeType) {
	return suffixmap.get(mimeType);

    }

    /**
     * Get the MIME type for a format name
     * @param name the format name
     * @return the MIME type matching a format name.
     */
    public static String getMimeType(String name) {
	return mtypeFromName.get(name);
    }

    /**
     * Get the names of the formats supported by the javax.imageio package.
     * There are typically multiple names corresponding to the same MIME
     * type.
     * @return an unmodifiable set containing the names
      */
    public static Set<String> getFormatNames() {
	return Collections.unmodifiableSet(preferredNameSet);
    }

    /**
     * Get the MIME types of the image types supported by the javax.imageio 
     * package.
     * @return an unmodifiable set containing the MIME types
     */
    public static Set<String> getMimeTypes() {
	return Collections.unmodifiableSet(fmtmap.keySet());
    }
}

//  LocalWords:  javax imageio IETF lt mtype bmp jpeg jpg png gif
//  LocalWords:  mimeType unmodifiable
