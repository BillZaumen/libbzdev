import org.bzdev.net.*;
import java.io.IOException;
import java.util.Map;

public class NullAdapter implements ServletAdapter {
    @Override
    public void doGet(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletAdapter.ServletException
    {
	res.sendError(404);
    }

    public void doPost(HttpServerRequest req, HttpServerResponse res)
	throws IOException, ServletAdapter.ServletException
    {
	res.sendError(404);
    }

}
