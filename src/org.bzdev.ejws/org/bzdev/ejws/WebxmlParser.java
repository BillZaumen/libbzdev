package org.bzdev.ejws;
import org.bzdev.util.ErrorMessage;
import org.bzdev.util.SafeFormatter;

import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.text.MessageFormat;
import java.nio.charset.Charset;

//@exbundle org.bzdev.ejws.lpack.EmbeddedWebServer

/**
 * Parser for web.xml files.
 * This parser handles only a subset of the elements that can appear
 * in these files.  Other elements are ignored.
 * <p>
 * The web.xml file should start with the following directives (this may
 * be updated as newer versions of the servlet specification are released):
 * <pre><code>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;web-app xmlns="http://java.sun.com/xml/ns/javaee"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"&gt;
 *   &lt;version&gt;2.5&lt;/version&gt;
 * </code></pre>
 * While ignored by the parser, it is a good idea to include a display-name
 * element and a description element, as these are used by various tools used
 * to configure web servers that support servlets.  The display-name element
 * provides a title for a web application.
 * <pre><code>
 *    &lt;dislay-name&gt;NAME&lt;/display-name&gt;
 * </code></pre>
 * the description element provides a description of a web application - a
 * short paragraph indicating what the application does.
 * <pre><code>
 *    &lt;description&gt;....&lt;/description&gt;
 * </code></pre>
 * A jsp-config directive should be used if JSP pages are provided for
 * error messages (support for JSP pages by the FileHandler class is
 * limited). Multiple property-group elements are allowed, associating a
 * character encoding with a url pattern.  A literal URL pattern must start
 * with a '/' and match the path for some resource.  A wildcard pattern can
 * start with a '*', followed non-wildcard symbols making up a path. It can
 * also end with a '*' but then must start with a '/'.  The '*' matches any
 * number of characters in a URL's path.
 * <pre><code>
 *    &lt;jsp-config&gt;
 *       &lt;jsp-property-group&gt;
 *          &lt;url-pattern&gt;/controls/error.jsp&lt;/url-pattern&gt;
 *          &lt;page-encoding&gt;UTF-8&lt;/page-encoding&gt;
 *       &lt;/jsp-property-group&gt;
 *    &lt;/jsp-config&gt;
 * </code></pre>
 * The mime-mapping element associates MIME types with file extensions.
 * There should be one element per extension.
 * <pre><code>
 *    &lt;mime-mapping&gt;
 *       &lt;extension&gt;js&lt;/extension&gt;
 *       &lt;mime-type&gt;text/javascript&lt;/mime-type&gt;
 *    &lt;/mime-mapping&gt;
 * </code></pre>
 * The welcome-file-list element contains a sequence of welcome-file
 * elements, each providing a path to a file to load when a URI's path
 * matches the context path.  It replaces the convention that some
 * web servers use of loading an index.html file in this case. The
 * first matching one is used.
 * <pre><code>
 *    &lt;welcome-file-list&gt;
 *       &lt;welcome-file&gt;/index.html&lt;/welcome-file&gt;
 *    &lt;/welcome-file-list&gt;
 * </code></pre>
 * Error page elements indicate where to find error pages. The page
 * lookup can be based on the HTTP response code (given in the exception-code
 * element) or the class name of a Java exception (given in the exception-type
 * element). In either case, the path to the resource is given in
 * a location element, and that path must start with a "/".  For example,
 * <pre><code>
 *    &lt;error-page&gt;
 *       &lt;exception-code&gt;100&lt;/exception-code&gt;
 *       &lt;location&gt;/.../error.jsp&lt;/location&gt;
 *    &lt;/error-page&gt;
 * </code></pre>
 * <pre><code>
 *    &lt;error-page&gt;
 *       &lt;exception-type&gt;java.lang.Exception&lt;/exception-code&gt;
 *       &lt;location&gt;/.../eerror.jsp&lt;/location&gt;
 *    &lt;/error-page&gt;
 * </code></pre>
 * Finally, the web-app element has to be terminated:
 * <pre><code>
 * &lt;/web-app&gt;
 * </code></pre>
 */

public class WebxmlParser {
    /*
    static final String PUBLICID =
	"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN";
    static final String SYSTEMID = "http://java.sun.com/dtd/web-app_2_3.dtd";
    static final String OUR_SYSTEMID = "resource:webxml.dtd";

    static private final String resourceBundleName =
    "org/bzdev/ejws/WebxmlParser";
    */

    /*
    static private final String resourceBundleName =
	"org/bzdev/ejws/WebxmlParser";
    static ResourceBundle bundle =
	ResourceBundle.getBundle(resourceBundleName);
    */

    // This bundle contains both format strings and MessageFormat strings

    static private ResourceBundle
	exbundle=ResourceBundle.getBundle("org.bzdev.ejws.lpack/WebxmlParser");

    static String localeString(String name) {
	return exbundle.getString(name);
    }

    static String errorMsg(String key, Object ... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }

    SAXParser parser;
    OurDefaultHandler handler = new OurDefaultHandler();

    WebMap webxmlMap = null;

    /**
     * Constructor.
     * @exception ParserConfigurationException an error occurred configuring
     *            the parser
     * @exception SAXException an error occurred during parsing
     */
    public WebxmlParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
	factory.setNamespaceAware(true);
	// webxmlMap =new WebMap();
        parser = factory.newSAXParser();
    }
    private static String HTTP_ACCEPT_VALUE = "text/xml"
	+ ", " + "application/xml";

    /**
     * Parse the input given a file
     * @param file the file to parse
     * @param map a WebMap used to store the configuration specified by
     *        the web.xml file being parsed
     * @exception SAXException a parser error occurred
     * @exception IOException an IO error occurred
     */
    public void parse (File file, WebMap map)
	throws SAXException, IOException
    {
	parse(new FileInputStream(file), file.toURI().toString(), map);
    }

    /**
     * Parse the input
     * @param urlAsString the URL for the file to parse
     * @param map a WebMap used to store the configuration specified by
     *        the web.xml file being parsed
     * @exception MalformedURLException the URL was malformed
     * @exception SAXException a parser error occurred
     * @exception IOException an IO error occurred
     */
    public void parse(String urlAsString, WebMap map)
	throws MalformedURLException, SAXException, IOException
    {
	setXMLFilename(urlAsString);
	URL url = new URL(urlAsString);
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    c.setRequestProperty("accept", HTTP_ACCEPT_VALUE);
	    ((HttpURLConnection) c).setRequestMethod("GET");
	    c.connect();
	    if (((HttpURLConnection) c).getResponseCode() !=
		HttpURLConnection.HTTP_OK) {
		throw new IOException(errorMsg("couldNotConnect", urlAsString));
		/*
		throw new IOException(String.format
				      (localeString("couldNotConnect"),
				       urlAsString));
		*/
	    }
	}
	parse(c.getInputStream(), urlAsString, map);
    }

    /**
     * Parse the input
     * @param url the URL for the file to parse
     * @param map a WebMap used to store the configuration specified by
     *        the web.xml file being parsed
     * @exception SAXException a parser error occurred
     * @exception IOException an IO error occurred
     */
    public void parse(URL url, WebMap map)
	throws SAXException, IOException
    {
	String urlAsString = url.toString();
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    c.setRequestProperty("accept", HTTP_ACCEPT_VALUE);
	    ((HttpURLConnection) c).setRequestMethod("GET");
	    c.connect();
	    if (((HttpURLConnection) c).getResponseCode() !=
		HttpURLConnection.HTTP_OK) {
		throw new IOException(errorMsg("couldNotConnect", urlAsString));
		/*
		throw new IOException(String.format
				      (localeString("couldNotConnect"),
				       urlAsString));
		*/
	    }
	}
	parse(c.getInputStream(), urlAsString, map);
    }

    /**
     * Parse the input given an input stream
     * @param is the input stream to parse
     * @param url a string representation of the URL for the input; null if
     *            no URL is available
     * @param map a WebMap used to store the configuration specified by
     *        the web.xml file being parsed
     * @exception SAXException a parser error occurred
     * @exception IOException an IO error occurred
     */
    public void parse(InputStream is, String url, WebMap map)
	throws SAXException, IOException
    {
	setXMLFilename(url);
        OurDefaultHandler handler = new OurDefaultHandler();
	webxmlMap = map;
	handler.errorSeen = false;
	handler.locator = null;
        parser.parse(is, handler);
        if (handler.errorSeen) {
            throw new SAXException(errorMsg("badDocument", url.toString()));
	}
	// write(System.out);
	webxmlMap = null;
	return;
    }

    void displayMessage(String msg, String title) {
	ErrorMessage.display(msg/*, title*/);
        // simplify for now
        // System.err.println(msg);
    }

    String xmlFilename = null;

    private void setXMLFilename(String name) {
	if (name == null) {
	    xmlFilename = null;
	    return;
	}
	if (name.startsWith("file:")) {
	    try {
		File f = new File(new URI(name));
		name = f.getCanonicalPath();
	    } catch (URISyntaxException e) {
		// no need to handle this - we would fail anyway
	    } catch (IOException eio) {
	    }
	}
	xmlFilename = name;
    }

    void displayMessage(Locator locator,
			String msg, String title) {
	ErrorMessage.display(xmlFilename, locator.getLineNumber(), msg);
    }

    class OurDefaultHandler extends DefaultHandler {
	boolean errorSeen = false;
        Locator locator = null;

	StringBuilder text = new StringBuilder();
	int matchlen = 0;
	boolean record = false;
	boolean done = false;

	Object ekey = null;
	String eloc = null;
	LinkedList<String> urlPatterns = new LinkedList<String>();
	String pageEncoding = null;
	boolean mtProcessing = false;
        boolean welcomeProcessing = false;
        boolean jspProcessing1 = false;
        boolean jspProcessing2 = false;
        boolean urlPatternProcessing = false;
        boolean pageEncodingProcessing = false;
        boolean errorPageProcessing = false;
	boolean isxmlProcessing = false;
	boolean isxml = false;
        int level = 0;

	@Override
	public void startDocument() {
	    // System.out.println("parsing started");
	    errorSeen = false;
	    text.setLength(0);
	    matchlen = 0;
	    record = false;
	    done = false;
	    mtProcessing = false;
	    welcomeProcessing = false;
	    jspProcessing1 = false;
	    jspProcessing2 = false;
	    urlPatternProcessing = false;
	    pageEncodingProcessing = false;
	    errorPageProcessing = false;
	    level = 0;
	    ekey = null;
	    eloc = null;
	    // webxmlMap.addEncoding(urlPatterns, pageEncoding);
	    urlPatterns.clear();
	    pageEncoding = null;
	    isxmlProcessing = false;
	    isxml = false;
	}

	@Override
	public void startElement(String uri, String localName,
                                 String qName, Attributes attr)
            throws SAXException
        {
            if (localName.equals("mime-mapping") && level == 1) {
                mtProcessing = true;
            } else if (localName.equals("welcome-file-list") && level == 1) {
                welcomeProcessing = true;
            } else if (localName.equals("extension") && mtProcessing
                       && level == 2) {
                record = true;
            } else if (localName.equals("mime-type") && mtProcessing
                        && level == 2) {
                record = true;
            } else if (localName.equals("welcome-file") && welcomeProcessing
                        && level == 2) {
                record = true;
            } else if (localName.equals("error-page") && level == 1) {
                errorPageProcessing = true;
            } else if (localName.equals("exception-code")
		       && errorPageProcessing && level == 2) {
                record = true;
            } else if (localName.equals("exception-type") && errorPageProcessing
                       && level == 2) {
                record = true;
	    } else if (localName.equals("location") && errorPageProcessing
                       && level == 2) {
                record = true;
            } else if (localName.equals("jsp-config")) {
                jspProcessing1 = true;
            } else if (localName.equals("jsp-property-group") && jspProcessing1
                       && level == 2) {
                jspProcessing2 = true;
		urlPatterns.clear();
		pageEncoding = Charset.defaultCharset().name();
		isxml = false;
	    } else if (localName.equals("url-pattern") && jspProcessing2
                       && level == 3) {
                urlPatternProcessing = true;
		record = true;
            } else if (localName.equals("page-encoding") && jspProcessing2
                       && level == 3) {
                pageEncodingProcessing = true;
		record = true;
	    } else if (localName.equals("is-xml") && jspProcessing2
		       && level == 3) {
		isxmlProcessing = true;
		record = true;
            } else if (record || mtProcessing || welcomeProcessing
                       || urlPatternProcessing || pageEncodingProcessing
		       || isxmlProcessing) {
            }
            level++;
        }


        String extension = null;
        String mimetype = null;

	@Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            if (localName.equals("mime-mapping")) {
                mtProcessing = false;
                webxmlMap.addMapping(extension, mimetype);
            } else if (localName.equals("welcome-file-list")) {
               welcomeProcessing = false;
            } else if (localName.equals("extension")) {
                extension = text.toString().trim();
                text.setLength(0);
                record = false;
            } else if (localName.equals("mime-type")) {
                mimetype = text.toString().trim();
                text.setLength(0);
                record = false;
            } else if (localName.equals("welcome-file")) {
                webxmlMap.addWelcome(text.toString().trim());
                text.setLength(0);
                record = false;
            } else if (localName.equals("error-page")) {
                errorPageProcessing = false;
                if (ekey != null && eloc != null) {
                    webxmlMap.addErrorEntry(ekey, eloc);
                }
		/*
                System.out.println("ekey = " + ekey
                                   +", eloc = " + eloc);
		*/
                ekey = null;
                eloc = null;
            } else if (localName.equals("exception-code")
		       && errorPageProcessing) {
                //ekey = new Integer(Integer.parseInt(text.toString().trim()));
		ekey = Integer.valueOf(text.toString().trim());
                text.setLength(0);
                record = false;
	    } else if (localName.equals("exception-type") &&
		       errorPageProcessing) {
		ekey = text.toString();
		text.setLength(0);
		record = false;
	    } else if (localName.equals("location") && errorPageProcessing) {
                eloc = text.toString().trim();
                text.setLength(0);
                record = false;
            } else if (localName.equals("jsp-config") && jspProcessing1) {
                jspProcessing1 = false;
            } else if (localName.equals("jsp-property-group")
                       && jspProcessing2) {
                jspProcessing2 = false;
		webxmlMap.addPageEncoding(urlPatterns, pageEncoding, isxml);

	    } else if (localName.equals("url-pattern")
		       && urlPatternProcessing) {
		urlPatterns.add(text.toString().trim());
		text.setLength(0);
		record = false;
	    } else if (localName.equals("page-encoding")
		       && pageEncodingProcessing) {
		pageEncoding = text.toString().trim();
		text.setLength(0);
		record = false;
	    } else if (localName.equals("is-xml") && isxmlProcessing) {
		isxml = text.toString().trim().equals("true");
		text.setLength(0);
		record = false;
	    }
	    level--;
	}

	@Override
        public void characters(char [] ch, int start, int length)
            throws SAXException
        {
	    if (record) text.append(ch, start, length);
        }

	@Override
	public void endDocument() {
	}

	@Override
        public void warning(SAXParseException e) {

            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("warningAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("warningAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("warningAtLineTitle"));
        }
	private void error(String msg) {
	    displayMessage(locator, msg, localeString("errorAtLineTitle"));
	    errorSeen = true;
	}

	@Override
        public void error(SAXParseException e) {
            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("errorAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("errorAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("errorAtLineTitle"));
            // System.err.println(msg);
            errorSeen = true;
        }

        public void fatalError(SAXParseException e) {
            String msg = (xmlFilename == null)?
		MessageFormat.format(localeString("fatalErrorAtLine"),
				     e.getLineNumber(),
				     e.getMessage()):
		MessageFormat.format(localeString("fatalErrorAtLineFN"),
				     xmlFilename,
				     e.getLineNumber(),
				     e.getMessage());
            displayMessage(msg, localeString("fatalErrorAtLineTitle"));
            // System.err.println(msg);
            errorSeen = true;
        }
    }

    static void copyStream(InputStream is, OutputStream os)
	throws IOException
    {
	byte[] buffer = new byte[4096];
	int len = 0;
	while ((len = is.read(buffer)) != -1) {
	    os.write(buffer, 0, len);
	}
	os.flush();
    }

}

//  LocalWords:  exbundle xml servlet pre lt UTF xmlns xsi servlets
//  LocalWords:  schemaLocation dislay jsp config JSP FileHandler url
//  LocalWords:  wildcard URL's js javascript URI's html
//  LocalWords:  lookup PUBLICID Microsystems SYSTEMID ResourceBundle
//  LocalWords:  resourceBundleName getBundle MessageFormat webxmlMap
//  LocalWords:  ParserConfigurationException SAXException WebMap msg
//  LocalWords:  IOException urlAsString couldNotConnect localeString
//  LocalWords:  badDocument addEncoding urlPatterns pageEncoding
//  LocalWords:  ekey eloc warningAtLine warningAtLineFN errorAtLine
//  LocalWords:  warningAtLineTitle errorAtLineTitle errorAtLineFN
//  LocalWords:  fatalErrorAtLine fatalErrorAtLineFN
//  LocalWords:  fatalErrorAtLineTitle
