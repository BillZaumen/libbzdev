import org.bzdev.net.*;
import java.io.IOException;
import java.util.Map;

public class NullAdapter implements ServletAdapter {
    @Override
    public void doGet(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletAdapter.ServletException
    {
	if (req.getParameter("foo") != null) {
	     doPost(req,res);
	     return;
	}
	res.sendError(404);
    }

    public void doPost(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletAdapter.ServletException
    {
	System.out.println("media type = " + req.getMediaType());
	System.out.println("request-parameter names:");
	if (req.getParameterNameSet() == null) {
	    System.out.println("  ... [null set]");
	} else {
	    for (String name: req.getParameterNameSet()) {
		System.out.println("    ... " + name);
	    }
	}

	if (req.getParameter("foo") != null) {
	    System.out.println("foo = " + req.getParameter("foo"));
	}
	res.sendError(404);
    }

    public void init(Map<String,String> map)
	throws ServletAdapter.ServletException
    {
	if (map != null) {
	    System.out.println("init parameters:");
	    for (Map.Entry entry: map.entrySet()) {
		System.out.format("    %s: %s\n",
				  entry.getKey(), entry.getValue());
	    }
	    System.out.println("(not used)");
	}
    }
}
