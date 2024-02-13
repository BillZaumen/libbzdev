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
	if (req.getParameter("foo") != null) {
	    System.out.println("foo = " + req.getParameter("foo"));
	}
	res.sendError(404);
    }

}
