import org.bzdev.ejws.*;
import org.bzdev.net.HttpMethod;
import java.io.*;
import java.util.Map;

// Use with curl
public class ErrorMap extends WebMap {
    public ErrorMap(Object root) {
	setMethods(HttpMethod.HEAD, HttpMethod.POST, HttpMethod.GET);
	setAllowsQuery(true);
    }
    @Override
    protected WebMap.Info getInfoFromPath(String prepath,
					  String epath,
					  String query,
					  String fragment,
					  WebMap.RequestInfo requestInfo)
	throws IOException, EjwsException
    {
	Map<String,String[]> qmap = null;
	qmap  = requestInfo.getParameterMap();
	System.out.println("query = " + query);
	if (qmap != null && qmap.size() > 0) {
	    if (qmap.get("io") != null) {
		System.out.println("IO exception case");
		throw new IOException("testing IO exception");
	    } else {
		System.out.println("runtime exception case");
		throw new RuntimeException("testing runtime exception");
	    }
	}
	return null;
    }
}
