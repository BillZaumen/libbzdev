package org.bzdev.io;

import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.Method;
import java.security.*;

/**
 * Output stream for an XML fast info set.
 * This class is deprecated as of Java 11 because Java 11 does not
 * allow access to the class
 * com.xml.internal.fastinfoset.sax.SAXDocumentSerializer.
 * For Java-8 or earlier, use the following instructions.
 * <P>
 * It is the caller's responsibility to use this class's "write"
 * methods to pass a valid XML document to an instance of this
 * class, including anything loaded externally (e.g., an external
 * DTD). When an XML document is fully read, this class will
 * automatically flush and close the underlying output stream.
 * <P>
 * Warning: this class instantiates the class
 * com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer,
 * which is provided in the JDK (at least, at the time this
 * documentation was written), but for some unknown reason,
 * the OpenJDK compiler will not recognize the package
 * "com.sun.xml.internal.fastinfoset.sax" when used in an import
 * statement in spite of it being
 * present in the system JAR file. Oddly, one can create an instance
 * of the SAXDocumentSrializer class by using the reflection API.
 * <P>
 * Because of this odd behavior, one should refrain from writing
 * applications that are dependent on this class working as expected
 * as it may disappear or have to be modified in subsequent JDK releases.
 * <P>
 * The static method {@link FISOutputStream#isSupported()} can be used
 * to determine if this class is supported on a particular JDK or JRE.
 */
@Deprecated
public class FISOutputStream extends FilterOutputStream {

    static String
	fqn = "com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer";

    static final String LEX_PROP =
	"http://xml.org/sax/properties/lexical-handler";
    static final int BUFSIZE = 4096;

    static SAXParserFactory saxParserFactory = null;

    static Class<?> clasz;
    static {
	try {
	    saxParserFactory = SAXParserFactory.newInstance();
	} catch (Exception e) {}
	AccessController.doPrivileged
	    (new PrivilegedAction<Void>() {
		public Void run() {
		    try {
			clasz =
			    ClassLoader.getSystemClassLoader().loadClass(fqn);
		    } catch (Exception e) {
			clasz = null;
		    }
		    return null;
		}
	    });
	saxParserFactory.setNamespaceAware(true);
    }

    /**
     * Test if the fast infoset format is supported by the JRE.
     * The fast infoset format is assumed to be supported if the class file
     * for com.sun.xml.internal.fastinfoset.sax.SAXDocumentSerializer
     * can be loaded.
     * @return true if it is supported; false otherwise
     */
    public static boolean isSupported() {
	return (clasz != null);
    }

    InputStream input;
    SAXParser parser;
    DefaultHandler sds = null;
    boolean done = false;

    static PipedOutputStream createPOS() {
	try {
	    return new PipedOutputStream();
	} catch (Exception e) {
	    return null;
	}
    }


    /**
     * Constructor.
     * @param os the underlying output stream
     * @exception UnsupportedOperationException this Java implementation
     *            does not support this class.
     */
    public FISOutputStream(final OutputStream os)
	throws UnsupportedOperationException
    {
	super(createPOS());

	try {
	    input = (out instanceof PipedOutputStream)?
		new PipedInputStream((PipedOutputStream)out): null;

	    AccessController.doPrivileged
		(new PrivilegedAction<Void>() {
		    public Void run() {
			try {
			    // Object obj = clasz.newInstance();
			    Object obj = clasz.getDeclaredConstructor()
				.newInstance();
			    if (obj instanceof DefaultHandler) {
				sds = (DefaultHandler) obj;
				Method m =  clasz.getMethod("setOutputStream",
							    OutputStream.class);
				m.invoke(sds, os);
			    }
			} catch (Exception e) {
			    sds = null;
			}
			return null;
		    }
		});
	    if (sds == null) {
		throw new UnsupportedOperationException();
	    }

	    parser = saxParserFactory.newSAXParser();
	    parser.setProperty(LEX_PROP, sds);

	    AccessController.doPrivileged
		(new PrivilegedAction<Void>() {
		    public Void run() {
			Thread thread = new Thread(new Runnable() {
				public void run() {
				    try {
					out.flush();
					parser.parse(input, sds);
					os.flush();
					os.close();
				    } catch (Exception e) {
					System.out.println(e.getMessage());
				    } finally {
					done = true;
					synchronized (parser) {
					    parser.notifyAll();
					}
				    }
				}
			    });
			thread.start();
			return null;
		    }
		});
	} catch (Exception e) {
	}
    }

    @Override
    public void close() throws IOException {
	out.flush();
	out.close();
	synchronized(parser) {
	    while (!done) {
		try {
		    parser.wait(1000L);
		} catch (InterruptedException e) {
		    return;
		}
	    }
	}
    }
}

//  LocalWords:  DTD JDK OpenJDK SAXDocumentSrializer FISOutputStream
//  LocalWords:  isSupported JRE infoset os clasz newInstance
//  LocalWords:  UnsupportedOperationException setOutputStream
