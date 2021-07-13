package org.bzdev.ejws.maps;
import org.bzdev.ejws.*;
import org.bzdev.util.ErrorMessage;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import java.net.*;
import com.sun.net.httpserver.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import javax.net.ssl.*;
import java.security.cert.*;

//@exbundle org.bzdev.ejws.maps.lpack.WebMap

/**
 * WebMap for resources whose location is determined by a URL.
 * The sole argument to the constructor is a URL or URI to which
 * a request URI's path, excluding the prefix, will be appended.
 * The URL's protocol must be one that can determine a resources
 * content encoding and media type (true for HTTP and HTTPS).
 */
public class URLWebMap extends WebMap {
    private URI base;
    private URI base1;
    /*
    private String sbase;
    private boolean jarProtocol = false;
    private String encapsulatedURI = null;
    private String encapsulatedBase = null;
    */
    static String errorMsg(String key, Object... args) {
	return WebMapErrorMsg.errorMsg(key, args);
    }

    public boolean allowsQuery() {return true;}

    /**
     * Type of the argument for the URLWebMap constructor.
     */
    public static class Params {
	URL b = null;
	// String sb = null;
	boolean nv = false;
	/**
	 * Constructor given a URL.
	 * @param b the base URL for the resource
	 * @param nv true if HTTPS requests should not be verified;
	 *        false otherwise
	 */
	public Params(URL b, boolean nv) {
	    this.b = b;
	    this.nv = nv;
	}

	/*
	 * Constructor given a String representation of a URL.
	 * @param sb the base URL for the resource
	 * @param nv true if HTTPS requests should not be verified;
	 *        false otherwise
	public Params(String sb, boolean nv) {
	    this.sb = sb;
	    this.nv = nv;
	}
	*/
    }

    boolean noVerify = false;


    private void setNoVerify(boolean value) {
	noVerify = value;
	// System.out.println("noVerify = " + noVerify);
    }

    /**
     * Check if certificates are verified.
     * @return true if verified; false otherwise
     */
    public boolean getNoVerify() {return noVerify;}


    /**
     * Constructor.
     * the argument can be a URL or an instance of URLWebMap.Parms.
     * When the argument is not an  instance of URLWebMap.Parms,
     * HTTPS connections will be verified: URLWebMap.Parms allows one
     * turn verification off while the constructor is running.
     * @param arg the argument
     */
    public URLWebMap(Object arg) {
	if (arg instanceof Params) {
	    Params params = (Params) arg;
	    arg = params.b;
	    setNoVerify(params.nv);
	}
	if (arg instanceof URL) {
	    try {
		base = ((URL) arg).toURI();
		if (base.getPath().endsWith("/")) {
		    base1 = base;
		} else {
		    base1 = new URI(base.getScheme(),
				    base.getAuthority(),
				    base.getPath() + "/",
				    base.getQuery(),
				    base.getFragment());
		}
	    } catch (URISyntaxException e) {
		String msg = errorMsg("needURI", ((URL)arg).toString());
		throw new IllegalArgumentException(msg, e);
	    }
	    /*
	} else if (arg instanceof String) {
	    // need to fix this to allow a query
	    sbase = (String) arg;
	    if (!sbase.endsWith("/")) {
		if (sbase.startsWith("jar:")) {
		    if (sbase.contains("!/")) {
			sbase = sbase + "/";
		    } else {
			sbase = sbase + "!/";
		    }
		    jarProtocol = true;
		} else {
		    sbase = sbase + "/" ;
		}
	    } else if (sbase.startsWith("jar:")) {
		jarProtocol = true;
	    }
	    if (jarProtocol) {
		int ind1 = 4; // "jar:".length()
		int ind2 = sbase.indexOf("!/");
		int ind3 = (ind2 == -1)? -1: (ind2 + 2);
		encapsulatedURI = sbase.substring(ind1, ind2);
		encapsulatedBase = (ind3 == -1)? "/": sbase.substring(ind3);
	    }
	    */
	} else {
	    throw new IllegalArgumentException
		(errorMsg("expectedURL", arg.getClass().getName()));
	}
    }

    /*
      public static X500Name getSubjectX500Name(X509Certificate cert)
      throws CertificateParsingException {
      try {
      Principal subjectDN = cert.getSubjectDN();
      if (subjectDN instanceof X500Name) {
      return (X500Name)subjectDN;
      } else {
      X500Principal subjectX500 = cert.getSubjectX500Principal();
      return new X500Name(subjectX500.getEncoded());
      }
      } catch (IOException e) {
      throw(CertificateParsingException)
      new CertificateParsingException().initCause(e);
      }
      }
    */

    private void doNotVerify(HttpsURLConnection sc) {
	TrustManager[] trustAll = new TrustManager[] {
	    new X509TrustManager() {
		public X509Certificate[] getAcceptedIssuers() {
		    return null;
		}
		public void checkClientTrusted(X509Certificate[] certs,
					       String authType)
		{}
		public void checkServerTrusted(X509Certificate[] certs,
					       String authType)
		{}
	    }
	};
	try {
	    SSLContext context = SSLContext.getInstance("SSL");
	    context.init(null, trustAll, new java.security.SecureRandom());
	    sc.setSSLSocketFactory(context.getSocketFactory());
	} catch (Exception e) {
	    ErrorMessage.display("cannot disable certificate checking");
	}
	sc.setHostnameVerifier(new HostnameVerifier() {
		public boolean verify(String hn, SSLSession session) {
		    /*
		      try {
		      System.out.println("checking certificates");
		      Certificate certs[] = session.getPeerCertificates();
		      for (Certificate c: certs) {
		      if (c instanceof X509Certificate) {
		      X509Certificate cert = (X509Certificate) c;
		      Collection<List<?>> altNames =
		      cert.getSubjectAlternativeNames();
		      if (altNames == null) {
		      System.out.println("... no names");
		      } else {
		      for (List<?> list: altNames) {
		      if (((Integer)list.get(0) == 2)
		      ||((Integer)list.get(0)
		      == 7)) {
		      System.out.println
		      ("... " + list.get(1));
		      }
		      }
		      }
		      X500Name subjectName =
		      getSubjectX500Name(cert);
		      DerValue derValue =
		      subjectName
		      .findMostSpecificAttribute
		      (X500Name.commonName_oid);
		      System.out.println
		      (derValue.getAsString());
		      }
		      }
		      System.out.println("checking done");
		      System.out.println(hn +" "
		      + " "
		      + session.getPeerHost()
		      + ": "
		      + session.getPeerPrincipal()
		      .getName());
		      } catch (Exception e) {
		      System.out.println(hn
		      +" <error for peer info>");
		      }
		    */
		    // return hn.equals(session.getPeerHost());
		    // System.out.println(hn.equals(session.getPeerHost()));
		    return true;
		}
	    });
    }

    /**
     * Get an Info object for a resource.
     * Only the path component is used.
     * @param prepath the initial portion of the request URI - the part
     *        before the path portion of the URI
     * @param path the relative path to the resource
     * @param query the query portion of the request URI
     * @param fragment the fragment portion of the request URI
     * @param requestInfo an object encapsulating request data
     *        (headers, input streams, etc.)
     * @return an Info object describing properties of a resource and
     *         providing an input stream to the resource
     */
    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String path,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
    {
	// System.out.println("getInfoFromPath ...");
	// System.out.println("prepath = " + prepath);
	// System.out.println(" path = " + path);
	if (path != null && path.startsWith("/")) {
	    path = path.substring(1);
	}
	URL url;
	boolean dirlist = false;
	try {
	    URI uri = new URI(null, null, path, query, fragment);
	    if (base != null) {
		// url = base.resolve(uri).toURL();
		url = (new URI(base.getScheme(), base.getAuthority(),
			       ((path==null || path.length() == 0)?
				base1.getPath():
				base1.resolve(uri).getPath()),
			       query, fragment)).toURL();
		/*
	    } else if (sbase != null) {
		if (jarProtocol && getDisplayDir()
		    && (path.endsWith("/") || path.length() == 0)) {
		    System.out.println("jarProtocol = " + jarProtocol);
		    dirlist = true;
		    url = new URL(encapsulatedURI);
		} else {
		    url = new URL(sbase + path);
		}
		*/
	    } else {
		return null;
	    }
	} catch (MalformedURLException e) {
	    // System.out.println(e.getMessage());
	    String msg = errorMsg("badURLI");
	    throw new IllegalArgumentException(msg, e);
	} catch (URISyntaxException ee) {
	    String msg = errorMsg("badURLI");
	    throw new IllegalArgumentException(msg, ee);
	}
	try {
	    /*
	    if (jarProtocol) {
		JarURLConnection jc = (JarURLConnection)url.openConnection();
		if (dirlist) {
		    ZipFile zipfile = jc.getJarFile();
		    return EjwsUtilities.printHtmlDir(zipfile, prepath,
						      encapsulatedBase, path,
						      "UTF-8",
						      this);
		}
		if (jc == null) {
		    URL u;
		    boolean compress = false;
		    for (String p: gzipPaths(path)) {
			u  = new URL(base + p);
			jc = (JarURLConnection)u.openConnection();
			if (jc != null) {
			    compress = true;
			    break;
			}
		    }
		}
		if (jc == null) return null;
		String mt = getMimeType(path);
		long len = jc.getContentLength();
		InputStream is1 = jc.getInputStream();

	    }
	    */
	    URLConnection c = url.openConnection();
	    if (c == null) return null;

	    if (noVerify && c instanceof HttpsURLConnection) {
		// System.out.println("will not verify " +url.toString());
		HttpsURLConnection sc = (HttpsURLConnection) c;
		doNotVerify(sc);
	    }
	    long length = c.getContentLength();
	    String mimeType = c.getContentType();
	    if (mimeType == null) {
		mimeType = getMimeType(path);
	    }
	    String contentEncoding = c.getContentEncoding();
	    InputStream is = c.getInputStream();
	    if (is != null) {
		WebMap.Info result;
		if (length != -1) {
		    result = new WebMap.Info(is, length, mimeType,
					     url.toString());
		} else {
		    ByteArrayOutputStream os =
			new ByteArrayOutputStream(1024<<4);
		    // EjwsUtilities.copyStream(is, os);
		    is.transferTo(os);
		    length = os.size();
		    is = new ByteArrayInputStream(os.toByteArray());
		    result = new Info(is, length, mimeType,
				      url.toString());
		}
		if (contentEncoding != null) {
		    result.setEncoding(contentEncoding);
		}
		return result;
	    } else {
		return null;
	    }
	} catch (IOException e) {
	    return null;
	}
    }
}

//  LocalWords:  exbundle WebMap URI's URLWebMap nv HTTPS sb noVerify
//  LocalWords:  Parms URL's arg needURI query expectedURL subjectDN
//  LocalWords:  getSubjectX CertificateParsingException getSubjectDN
//  LocalWords:  instanceof subjectX getEncoded IOException initCause
//  LocalWords:  SSL getPeerCertificates altNames subjectName oid hn
//  LocalWords:  getSubjectAlternativeNames DerValue derValue getName
//  LocalWords:  findMostSpecificAttribute commonName getAsString uri
//  LocalWords:  getPeerHost getPeerPrincipal prepath toString url
//  LocalWords:  getMessage badURLI UTF
