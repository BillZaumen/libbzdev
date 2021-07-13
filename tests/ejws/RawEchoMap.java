import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

// Use with curl
public class RawEchoMap extends WebMap {
    public RawEchoMap(Object root) {
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
	Map<String,String> qmap = null;
	if (requestInfo.getMethod() != HttpMethod.POST) {
	    throw new EjwsException("POST was missing");
	}
	String type = requestInfo.getMediaType();
	
	System.out.println("type = " + type);
	InputStream ris = requestInfo.getDecodedInputStream();
	BufferedReader r =
		    new BufferedReader(new InputStreamReader(ris, UTF8));
	String line = null;
	while ((line = r.readLine()) != null) {
	    System.out.println(line);
	}
	WebMap.Info info = new WebMap.Info(requestInfo);
	info.sendResponseHeaders(200, -1);
	return info;
    }
}
