import com.sun.net.httpserver.*;
import java.awt.*;
import java.net.*;
import org.bzdev.ejws.*;
import org.bzdev.ejws.maps.*;
import java.io.File;

public class Test0a {
    public static void main(String argv[]) throws Exception {

	InetAddress addr = null;
	int backlog = 5;
	int port = 0;
	HttpServer server =
	    HttpServer.create(new InetSocketAddress(addr, port), backlog);
	FileHandler fh = new FileHandler("http", new File("/"), DirWebMap.class,
					 true, true, true);
	server.createContext("http:", fh);
    }
}
