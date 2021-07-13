import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import org.bzdev.net.HttpMethod;
import org.bzdev.net.WebDecoder;
import org.bzdev.util.ErrorMessage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;

public class PostMap extends WebMap {
    public PostMap(Object root) {
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
	if (requestInfo.getMethod() == HttpMethod.GET) {
	    if (query == null) {
		throw new EjwsException("Query was missing");
	    }
	    qmap = WebDecoder.formDecode(query);
	} else if (requestInfo.getMethod() == HttpMethod.POST) {
	    if (!requestInfo.getContentType()
		.equals("application/x-www-form-urlencoded")) {
		return null;
	    }
	    InputStream ris = requestInfo.getDecodedInputStream();
	    qmap = WebDecoder.formDecode(ris);
	}
	StringBuilder sb = new StringBuilder();
	for (Map.Entry<String,String> entry: qmap.entrySet()) {
	    sb.append(entry.getKey());
	    sb.append(" = ");
	    sb.append(entry.getValue());
	    sb.append("\n");
	}
	byte[] array = sb.toString().getBytes(UTF8);
	WebMap.Info info = new WebMap.Info(requestInfo);
	info.setHeader("content-type", "text.plain;charset=UTF-8");
	info.sendResponseHeaders(200, array.length);
	OutputStream os = info.getOutputStream();
	os.write(array);
	os.close();
	return info;
    }
}
