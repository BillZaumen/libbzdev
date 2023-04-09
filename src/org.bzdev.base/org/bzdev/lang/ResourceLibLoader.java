package org.bzdev.lang;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

//@exbundle org.bzdev.lang.lpack.Lang

/**
 * Resource-based library loader.
 * This class is used to load native-method libraries that are
 * bundled in a jar file, etc., containing an application.
 * This is convenient in cases where these libraries are small
 * and when it is convenient to package these with the
 * application's classes.  A naming convention is used to
 * allow libraries for multiple operating systems and machine
 * architectures to be bundled in the same jar file, as
 * described in the documentation for the
 * {@link ResourceLibLoader#load(String) load} method.
 * <p>
 * A ResourceLibLoader is initialized by providing it with an XML
 * specification file that determines which resource names will
 * be searched for library files.  The DTD for this specification
 * file is given below.  The top level element is defined by
 * <blockquote><pre>
 *    &lt;!ELEMENT libloaderspec (archdef | archfamily | osinfo)*&gt;
 * </pre></blockquote>
 * An <code>archdef</code> element provides a mapping from an OS
 * architecture name to the canonical name (the standard name to
 * use as part of a library resource name.)
 * <blockquote><pre>
 *    &lt;!ELEMENT archdef EMPTY&gt;
 *    &lt;!ATTLIST archdef
 *              name CDATA #REQUIRED
 *              cname CDATA #REQUIRED&gt;
 * </pre></blockquote>
 * If archdef elements are not provided, the name provided by the OS
 * is assumed to be the canonical name.
 * <P>
 * Architectures are sometimes grouped in families, where newer
 * versions of a microprocessor have instruction sets compatible
 * with older versions.  The <code>archfamily</code> element defines
 * such families, and consists a series of arch elements ordered from
 * the newest architecture to the oldest.
 *
 * <blockquote><pre>
 *    &lt;!ELEMENT archfamily (arch)*&gt;
 * </pre></blockquote>
 * The <code>arch</code> element defines an architecture than can be part
 * of a family.
 * <blockquote><pre>
 *    &lt;!ELEMENT arch  EMPTY&gt;
 *    &lt;!ATTLIST arch
 *              cname CDATA #REQUIRED&gt;
 * </pre></blockquote>
 * The <code>cname</code> attribute provides the canonical name of the
 * architecture.  A particular architecture
 * can have at most one older architecture associated with it. That is,
 * if one archfamily element contains one arch element followed by
 * another, a different archfamily element cannot contain the same
 * arch element followed by a different arch element. An arch element
 * must also appear only once in any given arch family. * <p>
 * The <code>osinfo</code> element can be used to indicate the suffix
 * used for the file and resource names of native-method libraries.
 * Typical suffixes are "<code>.so</code>" and "<code>.dll</code>".
 * A set of version elements contained in an osinfo element can be
 * used to define a single version name for a number of versions.
 * This is particularly useful for Linux systems, where the version
 * changes whenever new kernel is provided. These elements are defined
 * as follows:
 * <blockquote><pre>
 *    &lt;!ELEMENT osinfo (version)*&gt;
 *    &lt;!ATTLIST osinfo
 *              name  CDATA #REQUIRED
 *              prefix CDATA #IMPLIED
 *    	        suffix CDATA #IMPLIED&gt;
 * </pre></blockquote>
 * The <code>name</code> attribute is mandatory and specifies the
 * OS name for the element.
 * The <code>suffix</code> attribute provides a suffix string, and
 * must contain a leading period ("."). Any number of nested version
 * elements can be provided.
 * <blockquote><pre>
 *    &lt;!ELEMENT version EMPTY&gt;
 *    &lt;!ATTLIST version
 *              pattern CDATA #REQUIRED
 *              replacement CDATA #REQUIRED&gt;
 * </pre></blockquote>
 * Each version element contains a pattern (using the Java regular
 * expression syntax) to match against the os.version system property.
 * The replacement used is the one specified by the first version element
 * whose pattern matches the version provided by 
 * <code>System.getProperty("os.version")</code>.
 * <p>
 * The default specification file contains the following:
 * <blockquote><pre>
 * &lt;?xml version="1.0" ?&gt;
 * &lt;libloaderspec&gt;
 *   &lt;archfamily&gt;
 *     &lt;arch cname="i686"/&gt;
 *     &lt;arch cname="i386"/&gt;
 *   &lt;/archfamily&gt;
 *   &lt;osinfo name="Linux" suffix=".so"&gt;
 *     &lt;version pattern="2\.4\..*" replacement="2.4"/&gt;
 *     &lt;version pattern="2\.5\..*" replacement=2.5"/&gt;
 *     &lt;version pattern="2\.6\..*" replacement=2.6"/&gt;
 *     ...
 *     &lt;version pattern="4\.0\..*" replacement=4.0"/&gt;
 *     ...
 *     &lt;version pattern="4\.14\..*" replacement=4.14"/&gt;
 *   &lt;/osinfo&gt;
 * &lt;/libloaderspec&gt;
 * </pre></blockquote>
 * <P>
 * The following code prints the order in which the software will search for
 * a shared-library resource:
 * <blockquote><pre><code>
 *	org.bzdev.lang.ResourceLibLoader rl =
 *	      new org.bzdev.lang.ResourceLibLoader();
 *
 *	java.util.Iterator&lt;String&gt; it = rl.getResources("foo");
 *	while (it.hasNext()) {
 *	    System.out.println(it.next());
 *	}
 *  }
 * </CODE></PRE></blockquote>
 * Will create list the following resource names on a Linux system
 * running Linux version 3.2.0-126-generic on an Intel i3 processor
 * (Java's os.arch system property reports amd64 instead of
   the correct x86_64):
 * <blockquote><pre><code>
 * libfoo-Linux-3.2.0-126-generic-amd64.so
 * libfoo-Linux-3.2-amd64.so
 * libfoo-Linux-amd64.so
 * libfoo-amd64.so
 * libfoo-Linux-3.2.0-126-generic-x86_64.so
 * libfoo-Linux-3.2-x86_64.so
 * libfoo-Linux-x86_64.so
 * libfoo-x86_64.so
 * libfoo-Linux-3.2.0-126-generic-i386.so
 * libfoo-Linux-3.2-i386.so
 * libfoo-Linux-i386.so
 * libfoo-i386.so
 * </CODE></PRE></blockquote>
 * <P>
 * The object files are stored as resources. For the example above, a
 * resource with one or more of the names listed above should be used
 * as the resource name.  If the resource should start with a path,
 * the path must be prepended to the library name.  These paths
 * contain names that are separated by a '/' (but may not start with a
 * '/'). For example, when "com/foo/foo" is used as an argument to
 * {@link ResourceLibLoader#getResources(String)} or
 * {@link ResourceLibLoader#load(String)}, the corresponding resource
 * names are com/foo/libfoo-x86_64.so, com/foo/libfoo-Linux-x86_64.so,
 * etc., depending on how specific the code is to a particular OS
 * version.
 * <P>
 * Because the os.arch system property is not a reliable indication
 * of the CPU, one should make conservative assumptions about
 * which binaries to include (e.g., provide a binary for x86_64
 * rather than one for an amd64 system, unless you know that the
 * jar file will only be used on an AMD system.)
 * <P>
 * While there is a default specification file, it is intended
 * for cases where version-specific libraries are used primarily
 * for work-arounds for bugs in specific OS versions or libraries:
 * when using the default specification file, one will nearly always
 * provide a resource that does not contain an OS version in its name.
 * For more complex cases, a custom specification file should be used.
 */
public class ResourceLibLoader {

    static String errorMsg(String key, Object... args) {
	return LangErrorMsg.errorMsg(key, args);
    }

    static final String osName = 
	System.getProperty("os.name").replaceAll("\\s", "");
    static final String systemArch = 
	System.getProperty("os.arch").replaceAll("\\s", "");
    static final String vers =
	System.getProperty("os.version").replaceAll("\\s", "");

    HashSet<String> nset = new HashSet<String>();
    HashMap<String, String[]> vtable = new HashMap<String,String[]>();
    HashMap<String,String> stable = new HashMap<String,String>();
    HashMap<String,String> ptable = new HashMap<String,String>();
    HashMap<String,String> cntable = new HashMap<String,String>();
    HashMap<String,String> atree = new HashMap<String,String>();

    /**
     * Set the version string data for an OS name.
     *
     * @param osname the name of the operating system from the
     *               system property <code>os.name</code> (do
     *               not include whitespace in the name).
     * @param array an array in which elements occur in pairs,
     *              with the first providing a regular expression
     *              that will match against the <code>os.version</code>
     *              system property and the second contains the
     *              version string to use when searching for a resource.
     *
     * @throws IllegalArgumentException the array argument had an odd length.
     */
    private void putVersionInfo(String osname, String array[])
    {
	if (array.length % 2 != 0)
	    throw new IllegalArgumentException("array length must be even");
	vtable.put(osname, array);
    }
    

    /**
     * Set the suffix to use for a library for a given OS.
     * @param osname the name of the operating system from the
     *               system property <code>os.name</code> (do
     *               not include whitespace in the name).
     * @param suffix the suffix to use, including the leading '.'
     */
    private void putSuffixInfo(String osname, String suffix)
    {
	stable.put(osname, suffix);
    }

    /**
     * Set the prefix to use for a library for a given OS.
     * @param osname the name of the operating system from the
     *               system property <code>os.name</code> (do
     *               not include whitespace in the name).
     * @param suffix the suffix to use, including the leading '.'
     */
    private void putPrefixInfo(String osname, String prefix)
    {
	ptable.put(osname, prefix);
    }

    /**
     * Define an canonical Name for an architecture.
     * Different operating systems may use differing names for an
     * architecture, such as i386 and x86. It is desirable to map
     * these into a single name.  The replacements are done once,
     * not recursively.
     * @param arch the  architecture provided by the os.arch system property
     * @param canonicalName the canonical value to use
     */
    private void putArchCanonicalName(String arch, String canonicalName)
    {
	cntable.put(arch, canonicalName);
    }

    /**
     * Define the next architecture in a family.
     * Architectures sometimes form families in which newer ones provide
     * instruction sets that are supersets of older ones.  This allows
     * the predecessor of an architecture to be defined.  For example,
     * <code>putNextArch("i486", "i386")</code> would allow i386
     * libraries to be used on i486 systems.
     *
     * @param arch the canonical name of an architecture
     * @apram nextArch the next architecture in a family
     */
    private void putNextArch(String arch, String nextArch)
    {
	atree.put(arch, nextArch);
    }

    private String getSuffix()
    {
	String suffix = stable.get(osName);
	if (suffix == null) {
	    if (osName.startsWith("Windows")) suffix = ".dll";
	    else {
		suffix = ".so";
	    }
	}
	return suffix;
    }

    private String getPrefix() {
	String prefix = ptable.get(osName);
	if (prefix == null) {
	    if (osName.startsWith("Windows")) prefix = "";
	    else prefix = "lib";
	}
	return prefix;
    }


    private String getVersion()
    {
	String array[] = (String []) vtable.get(osName);
	if (array == null) return null;
	for (int i = 0; i < array.length; i += 2) {
	    if (vers.matches(array[i])) {
		return array[i+1];
	    }
	}
	return vers;
    }

    private String getArchCanonicalName() {
	return getArchCanonicalName(systemArch);
    }

    private String getArchCanonicalName(String arch) {
	String name = (String)cntable.get(arch);
	return (name == null)? arch: name;
    }

    private String getNextArch(String arch) {
	return (String)atree.get(arch);
    }


    private static String defaultSpecResource = "org/bzdev/lang/rlibloader.xml";


    /**
     * Class constructor.
     * Uses the default configuration file in resource
     * org/bzdev/lang/rlibloader.xml
     * @throws FactoryConfigurationError if ther was an XML factory
     *         configuration error
     * @throws SAXException if there was an SAX parser exception
     * @throws IOException if there was an IO exception
     * @throws ParserConfigurationException if a parser
     *          configuration exception
     */
    public ResourceLibLoader() 
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(defaultSpecResource);
    }

    /**
     * Class constructor configured via a resource
     * @param specResource the string naming a resource.
     * @throws FactoryConfigurationError if ther was an XML factory
     *         configuration error
     * @throws SAXException if there was an SAX parser exception
     * @throws IOException if there was an IO exception
     * @throws ParserConfigurationException if a parser
     *          configuration exception
     * @see ClassLoader#getResource(String)
     */
    public ResourceLibLoader(String specResource) 
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	this(specResource, false);
    }

   /**
     * Class constructor configured via a resource and option.
     * @param specResource the string naming a resource.
     * @param validating true to use a validating parser; false otherwise
     * @throws FactoryConfigurationError if ther was an XML factory
     *         configuration error
     * @throws SAXException if there was an SAX parser exception
     * @throws IOException if there was an IO exception
     * @throws ParserConfigurationException if a parser
     *          configuration exception
     * @see ClassLoader#getResource(String)
     */
     public ResourceLibLoader(String specResource, boolean validating)
	throws FactoryConfigurationError, SAXException, IOException,
	       ParserConfigurationException
    {
	InputStream is = 
	    ClassLoader.getSystemClassLoader().
	    getResourceAsStream(specResource);
	if (is == null) {
	    throw new IOException(errorMsg("openResource", specResource));
	}
	SAXParserFactory factory = SAXParserFactory.newInstance();
	factory.setNamespaceAware(false);
	factory.setValidating(validating);
	SAXParser parser = factory.newSAXParser();
	parser.parse(is, new DefaultHandler() {
		String cname = null;
		String name = null;
		Stack<String> last = new Stack<String>();
		Vector<String> vinfo = new Vector<String>();

		private String getLast() {
		    if (last.empty()) return null;
		    return (String)last.peek();
		}

		public void startElement(String uri,
					 String localname,
					 String qname,
					 Attributes attributes)
		    throws SAXException
		{
		    int i; 
		    int len = attributes.getLength();
		    if (qname.equals("libloaderspec")) {
			if (!last.empty()) 
			    throw new 
				SAXException(errorMsg("wrongFirst"));
		    }
		    if (qname.equals("archdef")) {
			if (!getLast().equals("libloaderspec")) {
			    throw new SAXException
				(errorMsg("notExpected", qname));
			}
			String name = attributes.getValue("name").trim();
			String cname = attributes.getValue("cname").trim();
			if (name == null || cname == null) 
			    throw new SAXException
				(errorMsg("missingAttr"));
			putArchCanonicalName(name, cname);
		    } else if (qname.equals("archfamily")) {
			if (!getLast().equals("libloaderspec")) {
			    throw new SAXException
				(errorMsg("notExpected", qname));
			}
		    } else if (qname.equals("arch")) {
			if (!getLast().equals("archfamily")) {
			    throw new SAXException
				(errorMsg("notExpected", qname));
			}
			String cn = attributes.getValue("cname").trim();
			if (cn != null) {
			    String nextName = getNextArch(cname);
			    if (nextName != null && nextName != cn) {
				throw new SAXException
				    (errorMsg("multipleSuccessors", cname));
			    }
			    if (cname != null) putNextArch(cname, cn);
			    cname = cn;
			}
		    } else if (qname.equals("osinfo")) {
			if (!getLast().equals("libloaderspec")) {
			    throw new SAXException
				(errorMsg("notExpected", qname));
			}
			name = attributes.getValue("name").trim();
			String suffix = attributes.getValue("suffix").trim();
			if (suffix != null) putSuffixInfo(name, suffix);
			String prefix = attributes.getValue("prefix").trim();
			if (prefix != null) putPrefixInfo(name, prefix);
		    } else if (qname.equals("version")) {
			if (!getLast().equals("osinfo")) {
			    throw new SAXException
				(errorMsg("notExpected", qname));
			}
			if (name == null) {
			    throw new SAXException(errorMsg("missingName"));
			}
			String pattern = attributes.getValue("pattern");
			String replacement =
			    attributes.getValue("replacement");
			if (pattern == null || replacement == null)
			    throw new 
				SAXException(errorMsg("veMissing"));
			vinfo.add(pattern);
			vinfo.add(replacement);
		    }
		    last.push(qname);
		}
		public void endElement(String uri, String localname,
				       String qname)
		    throws SAXException
		{
		    last.pop();
		    if (qname.equals("archfamily")) {
			cname = null;
		    } else if (qname.equals("osinfo")) {
			int ind = 0;
			String array[] = new String[vinfo.size()];
			Enumeration velem = vinfo.elements();
			while (velem.hasMoreElements()) {
			    String pattern = (String)velem.nextElement();
			    String replacement = (String)velem.nextElement();
			    array[ind++] = pattern;
			    array[ind++] = replacement;
			}
			putVersionInfo(name, array);
			name = null; vinfo.clear();
		    }
		}
	    });
    }

    private String getSuffix(String resource) {
	int index = resource.lastIndexOf('.');
	String suffix = getSuffix();
	if (suffix == null) {
	    if (index != -1 && index > resource.lastIndexOf('/')) {
		suffix = resource.substring(index);
	    } else {
		suffix = ".so";
	    }
	}
	return suffix;
    }


    private String getResourceParent(String resource) {
	int index = resource.lastIndexOf("/");
	return resource.substring(0, index+1);
    }

    private String getResourceName(String resource) {
	int index = resource.lastIndexOf("/");
	return resource.substring(index + 1);
    }


    /**
     * Get the resource names that will be tried to find libraries.
     * This method is used internally to generate candidate resource
     * names. If the values produced by the iterator are strings so
     * they may be printed. This is useful for debugging new
     * spec files and to debug problems regarding library resource
     * names. For systems in which shared library names always
     * start with a prefix (e.g., "lib" on Unix and Linux systems),
     * the prefix must be omitted.
     * @param resource a resource name for a library without the os,
     *                version, and architecture substrings (which are
     *                described in the
     *                {@link ResourceLibLoader#load(String) load}
     *                method)
     * @return an iterator that will produce the resources that will
     *         be tried in an appropriate order.
     */
    public Iterator<String> getResources(String resource) {
	Stack<String> stack = new Stack<String>();
	String version = getVersion();
	String prefix = getPrefix();
	String parent = getResourceParent(resource);
	String name = getResourceName(resource);
	String arch = getArchCanonicalName();
	String suffix = getSuffix();
	do {
	    if (!vers.equals(version)) {
		stack.push(parent + prefix + name + "-" +osName+ "-"
			   + vers + "-" + arch + suffix);
	    }
	    stack.push(parent + prefix + name 
		       + "-"+osName+ "-"+ version + "-"+arch +suffix);
	    stack.push(parent + prefix + name + "-" + osName + "-" 
		       + arch + suffix);
	    stack.push(parent + prefix + name + "-" + arch + suffix);
	} while (((arch = getNextArch(arch)) != null));
	return stack.iterator();
    }

    /**
     * Load a class from a resource.
     *
     * Several resource names will be tried and the first one that
     * matches a resource will be used to load a library. The resource
     * names are constructed from the argument and from substrings
     * based on the Java system properties os.name, os.version, and
     * os.arch.  The first resource name tried is composed of the
     * prefix (which may be an empty string), followed by the base
     * resource name provided in the argument, followed by a '-'
     * character, followed by the OS name and then another '-'
     * character, followed by the version and then another '-'
     * character, and finally followed by the OS architecture,in turn
     * followed by the suffix.  If a replacement for the version is
     * provided in the configuration file, both the version and the
     * replacement are tried, the actual version first.
     * <P>
     * The next resource name is composed of the the prefix, followed by
     * the base resource name provided in the argument, followed by a
     * '-' character, followed by the OS name, again followed by a '-'
     * character, and then followed by the OS architecture, which is
     * in turn followed by the suffix.
     * The last resource names starts with the prefix, followed by
     * the base resource name, followed by a '-' character, and finally
     * followed by the OS architecture, in turn followed by the suffix.
     * <P>
     * In all cases, the suffix is determined by the
     * name of the operating system.  If a suffix for the OS is not
     * specified in the xml configuration file, the suffix defaults to
     * ".dll" for any OS whose name starts with "Windows" and ".so"
     * otherwise. Similarly, if a prefix is not defined, it defaults to
     * the empty string for any OS whose name starts with Windows and to
     * "lib" for all other OS names.  Both defaults can be overridden in
     * the configuration specification.
     * <P>
     * The sequence described above will be repeated for each architecture
     * that is an ancestor of the architecture corresponding to the
     * system property "os.arch": the <code>archfamily</code> directives
     * describe a collection of trees with each directive describing a
     * sequence of nodes in a tree leading towards the root of the tree.
     * <p>
     * The OS name, OS version and OS architecture are by default the 
     * values of the os.name, os.version, and os.arch system properties. 
     * If a mapping is defined by the <code>archdef</code> directive,
     * however, the architecture name will be replace in all cases with
     * a canonical version of this name.
     * <p>
     * For example, if the specification maps the version
     * string to 2.4 for all 2.4 version, then with Linux 2.4.X running on
     * an i386 system, the resource names tried for <code>foo</code>
     * will be  <code>libfoo-Linux-2.4.20-6-i386.so</code>,
     * <code>libfoo-Linux-2.4-i386.so, libfoo-Linux-i386.so</code> and
     * <code>libfoo-i386.so</code>, followed by <code>libfoo.so</code> 
     * itself. On a windows system, the ".so" suffix will be replaced 
     * with a ".dll" suffix.  On a Linux system where the system property
     * <code>os.arch</code> is <code>i686</code>, using the default
     * specification file, the resource names that will be tried are
     * <code>libfoo-Linux-2.4.20-6-i686.so</code>,
     * <code>libfoo-Linux-2.4-i686.so, libfoo-Linux-i686.so</code>,
     *  <code>libfoo-Linux-2.4.20-6-i386.so</code>,
     * <code>libfoo-Linux-2.4-i386.so</code>,
     * <code>libfoo-Linux-i386.so</code> and <code>libfoo-i386.so</code>.
     * Two architectures appear because the specification defines a
     * family of architectures, so all are tried.
     * <p>
     * The resource as specified should not end in a suffix
     * (e.g., ".so" on Linux) and should not contain a prefix as part of
     * the  resource name (e.g., "lib"on Linux): those are provided by
     * the {@link #load(String)} method. If detailed information is not
     * available, the default suffixes are ".dll" for Windows systems and
     * ".so" for Linux/Unix systems.
     * 
     * @param resource a string naming the resource (the name for a
     *        shared library) excluding the OS, version, and architecture
     *        components, but excluding the suffix and prefix.
     * @throws UnsatisfiedLinkError the library could not be loaded.
     * @throws SecurityException the library could not be loaded.
     */
    public void load(String resource)
        throws  UnsatisfiedLinkError, SecurityException
    {
	int index = resource.lastIndexOf('.');
	String suffix = getSuffix(resource);

	Iterator<String> it = getResources(resource);
	InputStream is = null;

	while (is == null && it.hasNext()) {
	    String res = it.next();
	    is = ClassLoader.getSystemResourceAsStream(res);
	}

        if (is == null) {
            throw new
                UnsatisfiedLinkError(errorMsg("noResource", resource));
        }

        try {
            File lib = File.createTempFile("lib", suffix);
            lib.deleteOnExit();
            FileOutputStream os = new FileOutputStream(lib);

            byte buffer[] = new byte[1<<13];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.close();
            System.load(lib.getCanonicalPath());
        } catch (IOException e) {
            throw new UnsatisfiedLinkError(errorMsg("tmpIO"));
        }
    }
}

//  LocalWords:  exbundle ResourceLibLoader DTD blockquote pre lt os
//  LocalWords:  libloaderspec archdef archfamily osinfo ATTLIST xml
//  LocalWords:  CDATA cname getProperty rl getResources hasNext amd
//  LocalWords:  libfoo prepended  arounds osname dll
//  LocalWords:  whitespace IllegalArgumentException canonicalName
//  LocalWords:  supersets putNextArch nextArch specResource tmpIO
//  LocalWords:  ClassLoader getResource openResource wrongFirst
//  LocalWords:  notExpected missingAttr multipleSuccessors veMissing
//  LocalWords:  missingName substrings UnsatisfiedLinkError
//  LocalWords:  SecurityException noResource
