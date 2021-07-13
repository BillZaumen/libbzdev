import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.FormDataIterator;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.HttpSessionOps;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Enumeration;

// Use with curl
public class HeaderMap extends WebMap {
    HttpSessionOps sessionOps;
    public HeaderMap(Object root) {
	setMethods(HttpMethod.HEAD, HttpMethod.POST, HttpMethod.GET);
	setAllowsQuery(true);
	sessionOps = (HttpSessionOps) root;
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
	String sid = requestInfo.getRequestedSessionID();
	if (!sessionOps.contains(sid)) {
	    sessionOps.add(sid);
	}
	Enumeration<String> hn = requestInfo.getHeaderNames();
	while (hn.hasMoreElements()) {
	    String name = hn.nextElement();
	    System.out.println(name + ": " + requestInfo.getHeader(name));
	}
	
	InputStream ris = requestInfo.getEncodedInputStream();
	while (ris.read() != -1);
	WebMap.Info info = new WebMap.Info(requestInfo);
	info.sendResponseHeaders(200, -1);
	return info;
    }
}
