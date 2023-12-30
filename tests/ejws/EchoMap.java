import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.FormDataIterator;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

// Use with curl
public class EchoMap extends WebMap {
    public EchoMap(Object root) {
	setMethods(HttpMethod.HEAD, HttpMethod.POST, HttpMethod.GET);
	setAllowsQuery(true);
    }

    private static Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String epath,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
	throws IOException, EjwsException
    {
	Map<String,String[]> qmap = null;
	System.out.println("contentLength = " + requestInfo.getContentLength());
	qmap  = requestInfo.getParameterMap();
	System.out.println("query = " + query);
	if (qmap != null && qmap.size() > 0) {
	    System.out.println("parameters:");
	    for (Map.Entry<String,String[]>entry: qmap.entrySet()) {
		System.out.println("    " + entry.getKey() + ": "
				   + entry.getValue()[0]);
	    }
	}
	if (requestInfo.getMethod() != HttpMethod.POST) {
	    throw new EjwsException("POST was missing");
	}
	String type = requestInfo.getMediaType();
	

	System.out.println("type = " + type);
	System.out.println("remoteAddr = " + requestInfo.getRemoteAddr());

	InputStream ris = requestInfo.getDecodedInputStream();
	if (false) {
	    ris.transferTo(System.out);
	} else if (type.equalsIgnoreCase("multipart/form-data")) {
	    String boundary = requestInfo.getFromHeader("content-type",
							"boundary");
	    System.out.println("boundary = \"" + boundary + "\"");
	    FormDataIterator fdi = new FormDataIterator(ris, boundary);
	    while (fdi.hasNext()) {
		InputStream is = fdi.next();
		System.out.println("name = " + fdi.getName());
		String filename = fdi.getFileName();
		if (filename != null) {
		    System.out.println("file name = " + filename);
		}
		BufferedReader r =
		    new BufferedReader(new InputStreamReader(is, UTF8));
		String line = null;
		System.out.println(">>>>>>>>>>>");
		while ((line = r.readLine()) != null) {
		    System.out.println(line);
		}
		System.out.println("<<<<<<<<<<<");
		is.close();
	    }
	}
	BufferedReader r = new BufferedReader(new InputStreamReader(ris, UTF8));
	String line = null;
	while ((line = r.readLine()) != null) {
	    System.out.println(line);
	}
	WebMap.Info info = new WebMap.Info(requestInfo);
	info.sendResponseHeaders(200, -1);
	return info;
    }
}
