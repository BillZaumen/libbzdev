import org.bzdev.swing.AuthenticationPane;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.File;
import java.nio.file.*;
import org.bzdev.net.*;

// For sslSetup()
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.*;
import javax.net.ssl.*;

import java.util.concurrent.*;

public class AuthPaneTest {

    // copied from AuthenticationPane
    private static class URLData {
	URL context;
	URL origin;
	String userName;
	char[] password;
	String realm;
	Certificate cert;
	SecureBasicUtilities ops;
    }
    private static ConcurrentHashMap<URL,URLData> urlmap =
	new ConcurrentHashMap<URL,URLData>();

    private static URLData getUData(URL url, String realm) {
	URL context = url;
	String path = url.getPath();
	if (path == null
	    || path.length() == 0
	    || !(path.charAt(0) == '/')) {
	    return null;
	} else if (path.length() != 1 && !path.endsWith("/")) {
	    path = path.substring(0, path.lastIndexOf('/')+1);
	    try {
		context = new URL(url, path);
	    } catch (MalformedURLException e) {}
	}
	URL ctxt = context;
	System.out.println(" ... starting ctxt = " + ctxt);
	while (!(urlmap.containsKey(ctxt)
		 && urlmap.get(ctxt).realm.equals(realm))) {
	    String p = ctxt.getPath();
	    int len = p.length();
	    if (len == 1) {
		URLData udata = new URLData();
		udata.context = context;
		udata.origin = url;
		udata.realm = realm;
		urlmap.put(context, udata);
		System.out.println("putting new udata for "
				   + context
				   + ", url = " + url);
		return udata;
	    }
	    p = p.substring(0, p.length()-1);
	    p = p.substring(0, p.lastIndexOf('/')+1);
	    try {
		ctxt = new URL(ctxt, p);
		System.out.println(" ... next ctxt = " + ctxt);
	    } catch (MalformedURLException e) {}
	}
	System.out.println("getting udata for " + ctxt
			   + ", url = " + url);
	return urlmap.get(ctxt);
    }


    static void testGetUData() throws Exception{
	URL url = new URL("http://foo.com/dir/x");
	URLData data = getUData(url, "realm");
	url = new URL("http://foo.com/dir/y");
	data = getUData(url, "realm");
	url = new URL("http://foo.com/dir/dir1/z");
	data = getUData(url, "realm");
	url = new URL("http://foo.com/dir1/dir1/z");
	data = getUData(url, "realm");
	url = new URL("http://foo.com/dir1/x");
	data = getUData(url, "realm");

    }


    public static void sslSetup() throws Exception {
	TrustManagerFactory tmf = TrustManagerFactory
	    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	tmf.init((KeyStore) null);
	X509TrustManager defaultTm = null;
	for (TrustManager tm: tmf.getTrustManagers()) {
	    if (tm instanceof X509TrustManager) {
		defaultTm = (X509TrustManager) tm;
		break;
	    }
	}
	// FileInputStream myKeys = new
	//    FileInputStream("../libbzdev/tests/ejws/thelio-ts.jks");
	FileInputStream myKeys = new
	    FileInputStream(System.getProperty("ssl.trustStore"));
	KeyStore myTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	myTrustStore.load(myKeys, System
			  .getProperty("ssl.trustStorePassword")
			  .toCharArray());
	tmf = TrustManagerFactory
	    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
	tmf.init(myTrustStore);

	X509TrustManager myTm = null;
	for (TrustManager tm : tmf.getTrustManagers()) {
	    if (tm instanceof X509TrustManager) {
		myTm = (X509TrustManager) tm;
		break;
	    }
	}

	X509TrustManager finalDefaultTm = defaultTm;
	X509TrustManager finalMyTm = myTm;

	X509TrustManager customTm = new X509TrustManager() {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
		    // If you're planning to use client-cert auth,
		    // merge results from "defaultTm" and "myTm".
		    return finalDefaultTm.getAcceptedIssuers();
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain,
					       String authType)
		    throws CertificateException
		{
		    try {
			finalDefaultTm.checkServerTrusted(chain, authType);
		    } catch (CertificateException e) {
			// This will throw another CertificateException if this fails too.
			finalMyTm.checkServerTrusted(chain, authType);
		    }
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain,
					       String authType)
		    throws CertificateException
		{
		    // If you're planning to use client-cert auth,
		    // do the same as checking the server.
		    finalDefaultTm.checkClientTrusted(chain, authType);
		}
	    };

	SSLContext sslContext = SSLContext.getInstance("TLS");
	sslContext.init(null, new TrustManager[] { customTm }, null);

	// You don't have to set this as the default context,
	// it depends on the library you're using.
	SSLContext.setDefault(sslContext);

	HostnameVerifier defaultHNV =
	    HttpsURLConnection.getDefaultHostnameVerifier();

	HostnameVerifier ourHNV = new HostnameVerifier() {
		String loopback = InetAddress.getLoopbackAddress()
		    .getHostName();
		public boolean verify(String hostname, SSLSession session) {
		    System.out.println("hostname = " + hostname);
		    boolean result = defaultHNV.verify(hostname, session);
		    if (result == false) {
			if (hostname.equals(loopback)) return true;
		    }
		    return result;
		}
	    };
	HttpsURLConnection.setDefaultHostnameVerifier(ourHNV);
    }


    public static void main(String argv[]) throws Exception {

	testGetUData();

	String HOST = System.getProperty("HOST", "localhost");
	String pem1 = Files.readString(Paths.get("privateKey.pem"));
	System.out.println(pem1);
	String pem2 = Files.readString(Paths.get("../ejws/publicKey.pem"));
	System.out.println(pem2);
	SecureBasicUtilities ops1 = new SecureBasicUtilities(pem1);
	SecureBasicUtilities ops2 = new SecureBasicUtilities(pem2);
	char[] passArray = ops1.createPassword(null, "foo".toCharArray());
	String password = new String(passArray);
	byte[] sigarray = ops2.decodePassword(password);
	if (ops2.checkPassword(sigarray, null, "foo") == false) {
	    throw new Exception("ops");
	}

	String fn = System.getProperty("sblFile");
	if (fn != null || fn.length() > 0) {
	    SSLUtilities.configureUsingSBL("TLS", new File(fn), null);
	} else {
	    // sslSetup();
	    SSLUtilities
		.installTrustManager("TLS",
				     new File(System.getProperty
					      ("ssl.trustStore")),
				     System
				     .getProperty
				     ("ssl.trustStorePassword")
				     .toCharArray(),
				     (cert) -> {return true;});

	    SSLUtilities.allowLoopbackHostname();
	}

	String urlString1 = argv.length > 0 ? argv[0]: null;
	String urlString2 = argv.length > 1? argv[1]: null;
	final URL url1 = (urlString1 != null)? new URL (urlString1): null;
	final URL url2 = (urlString2 != null)? new URL (urlString2): null;

	// Se we'll handle authentication before the frame is constructed.

	File pkfile = new File(System.getProperty("sblFile",
						  "privateKey.pem.gpg"));
	if (!pkfile.exists()) pkfile = new File("privateKey.pem");

	System.out.println("pkfile = " + pkfile);

	try {
	    AuthenticationPane.setPrivateKey(new File("junk.pem"));
	    System.out.println("exception missing");
	    System.exit(1);
	} catch (Exception e) {}

	try {
	    AuthenticationPane.setPrivateKey(new File("AuthPaneTest.java"));
	    System.out.println("exception missing");
	    System.exit(1);
	} catch (Exception e) {}


	AuthenticationPane.setPrivateKey(pkfile);
	Authenticator auth =
	    AuthenticationPane.getAuthenticator(null, true);
	// AuthenticationPane.setPrivateKey(auth,pkfile);

	try {
	    AuthenticationPane.setPrivateKey(auth, new File("junk.pem"));
	    System.out.println("exception missing");
	    System.exit(1);
	} catch (Exception e) {}

	try {
	    AuthenticationPane.setPrivateKey(auth,
					     new File("AuthPaneTest.java"));
	    System.out.println("exception missing");
	    System.exit(1);
	} catch (Exception e) {}


	Authenticator.setDefault(auth);

	if (url1 != null) {
	    System.out.println("connect to login alias");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/login.html":
				   "http://" + HOST + ":8080/login.html");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("status = " + status);

		if (status == 200 || status == -1) {
		    System.out.println(urlc.getContentType());
		    System.out.println("content-length = "
				       + urlc.getContentLength());
		    System.out.println("... reading");
		    InputStream is = urlc.getInputStream();
		    is.transferTo(OutputStream.nullOutputStream());
		    is.close();
		    System.out.println("... reading complete");
		} else {
		}
	    }
	}
	System.out.println("load images, if any");

	ImageIcon icon1 = (url1 != null)? new ImageIcon(url1): null;
	ImageIcon icon2 = (url2 != null)? new ImageIcon(url2): null;


	SwingUtilities.invokeLater(() -> {
		JFrame frame = new JFrame("AuthenticationPane Test");
		Container fpane = frame.getContentPane();
		frame.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
			    System.exit(0);
			}
		    });
 
		fpane.setLayout(new FlowLayout());

		try {
		    if (url1 != null) {
			fpane.add(new JLabel(icon1));
		    }
		    if (url2 != null) {
			fpane.add(new JLabel(icon2));
		    }
		    if (url1 == null && url2 == null) {
			fpane.add(new JLabel("no images"));
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(1);
		}
		frame.pack();
		frame.setVisible(true);
	    });
	if (url1 != null) {
	    System.out.println("connect to logout");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/logout.html":
				   "http://" + HOST + ":8080/logout.html");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		System.out.println("... reading (content length = "
				   + urlc.getContentLength() + ")");
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... get input stream");
		InputStream is = (status == 200)? urlc.getInputStream():
		    ((HttpURLConnection) urlc).getErrorStream();
		int cnt  = 0;
		while (is.read() != -1) {
		    cnt++;
		}
		is.close();
		System.out.println("... read " + cnt + " bytes");
		System.out.println("... reading complete");
	    }
	}
	if (url1 != null) {
	    System.out.println("connect to login");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/login.html":
				   "http://" + HOST + ":8080/login.html");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		System.out.println("... reading (content length = "
				   + urlc.getContentLength() + ")");
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... get input stream");
		InputStream is = (status == 200)? urlc.getInputStream():
		    ((HttpURLConnection) urlc).getErrorStream();
		int cnt  = 0;
		while (is.read() != -1) {
		    cnt++;
		}
		is.close();
		System.out.println("... read " + cnt + " bytes");
		System.out.println("... reading complete");
	    }
	}

	if (url1 != null) {
	    System.out.println("*** sleeping 25 seconds");
	    Thread.sleep(25000L);
	    System.out.println("connect to root");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/":
				   "http://" + HOST + ":8080/");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... reading");
		InputStream is = urlc.getInputStream();
		is.transferTo(OutputStream.nullOutputStream());
		is.close();
		System.out.println("... reading complete");
	    }
	}
	if (url1 != null) {
	    System.out.println("*** sleeping 50 seconds");
	    Thread.sleep(50000L);
	    System.out.println("connect to root");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/":
				   "http://" + HOST + ":8080/");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... reading");
		InputStream is = urlc.getInputStream();
		is.transferTo(OutputStream.nullOutputStream());
		is.close();
		System.out.println("... reading complete");
	    }
	}
	if (url1 != null) {
	    System.out.println("*** sleeping 50 seconds");
	    Thread.sleep(50000L);
	    System.out.println("connect to logout");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/logout.html":
				   "http://" + HOST + ":8080/logout.html");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		System.out.println("... reading (content length = "
				   + urlc.getContentLength() + ")");
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... get input stream");
		InputStream is = (status == 200)? urlc.getInputStream():
		    ((HttpURLConnection) urlc).getErrorStream();
		int cnt  = 0;
		while (is.read() != -1) {
		    cnt++;
		}
		is.close();
		System.out.println("... read " + cnt + " bytes");
		System.out.println("... reading complete");
	    }
	}
	if (url1 != null) {
	    System.out.println("*** sleeping 1 seconds");
	    Thread.sleep(1000L);
	    System.out.println("connect to root again");
	    if (urlString1.startsWith("http://" + HOST + ":8080/")
		|| urlString1.startsWith("https://" + HOST + ":8080/")) {
		int last = urlString1.lastIndexOf('/');
		URL url0 = new URL(urlString1.startsWith("https")?
				   "https://" + HOST + ":8080/":
				   "http://" + HOST + ":8080/");
		URLConnection urlc = url0.openConnection();
		urlc.connect();
		System.out.println("... reading (content length = "
				   + urlc.getContentLength() + ")");
		int status = (urlc instanceof HttpURLConnection)?
		    ((HttpURLConnection) urlc).getResponseCode(): -1;
		System.out.println("... status = " + status);
		System.out.println("... get input stream");
		InputStream is = (status == 200)? urlc.getInputStream():
		    ((HttpURLConnection) urlc).getErrorStream();
		if (is != null) {
		    int cnt  = 0;
		    while (is.read() != -1) {
			cnt++;
		    }
		    is.close();
		    System.out.println("... read " + cnt + " bytes");
		    System.out.println("... reading complete");
		} else {
		    System.out.println("... no input stream");
		}
	    }
	}

	System.exit(0);
    }
}
