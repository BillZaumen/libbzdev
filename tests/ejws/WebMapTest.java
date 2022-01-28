import org.bzdev.ejws.WebMap;
import org.bzdev.ejws.EjwsPrincipal;
import org.bzdev.net.HttpMethod;
import java.io.File;
import java.util.Set;

public class WebMapTest {
    // test program
    public static void main(String argv[]) {
	try {

	    WebMap map = new WebMap() {
		    protected WebMap.Info getInfoFromPath(String prepath,
							  String path,
							  String query,
							  String fragment,
							  WebMap.RequestInfo ri)
		    {
			return null;
		    }
		};

	    map.addPageEncoding("*.jsp", "UTF-8", false);
	    map.addPageEncoding("/foo/*", "US-ASCII", true);
	    map.addPageEncoding("/error.jsp", "UTF-16", true);

	    String[] strings = {
		"/foo.jsp",
		"foo.jsp",
		"/error.jsp",
		"/x/error.jsp",
		"/foo/error.jsp",
		"/bar.jsp",
		"bar.jsp"
	    };
	    for (String url: strings) {
		System.out.println(url +": " + map.getContentTypeFromURL(url)
				   +" " + map.getEncodingFromURL(url));
	    }
	    map = WebMap.newInstance(new File("example"),
				     "org.bzdev.ejws.maps.DirWebMap");
	    System.out.println(map.getClass().getName());

	    for (HttpMethod m: HttpMethod.values()) {
		System.out.println("method: " + m);
		HttpMethod method = HttpMethod.forName(m.toString());
		if (method != m) {
		    throw new Exception("method failure");
		}
	    }

	    EjwsPrincipal p1 = new EjwsPrincipal("user1", "realm");
	    EjwsPrincipal p2 = new EjwsPrincipal("user2", "realm",
						 Set.of("r1", "r2"));

	    System.out.println("p1 in role r1 = " + p1.getRoles()
			       .contains("r1"));
	    System.out.println("p1 in role r2 = " + p2.getRoles()
			       .contains("r2"));
	    

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }
}
