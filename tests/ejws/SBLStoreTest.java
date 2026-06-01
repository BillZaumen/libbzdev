import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bzdev.ejws.*;


public class SBLStoreTest {

    public static void main(String argv[]) throws Exception {
	SBLStore store = new SBLStore(new File("sblstoretest"));

	Map<String,EjwsAuthenticator.Entry> map = new HashMap<>();

	store.append("nm1", true, "foo1", true);
	store.append("nm2", true, "foo2", false);
	store.append("nm3", false, "foo3", true);
	store.append("nm4", false, "foo4", false);

	EjwsAuthenticator.Entry aentry = new EjwsAuthenticator.Entry();
	aentry.makeActive();

	map.put("nm1", aentry);
	map.put("nm2", new EjwsAuthenticator.Entry());
	map.put("nm3", aentry);
	map.put("nm4", new EjwsAuthenticator.Entry());

	store.close();
	
	store = new SBLStore(new File("sblstoretest"));
	store.mapStream().forEach((event) -> {
		System.out.format("%s, %b, %s, %b\n",
				  event.getKey(),
				  event.getValue().pwmode,
				  event.getValue().data,
				  event.getValue().isActive);
	    });
	store.clearMap();

	store.append("nm5", true, "foo5");
	store.append("nm6", true, "foo6");
	store.append("nm7", false, "foo7");
	store.append("nm8", false, "foo8");

	store.makeActive("nm5");
	store.makeActive("nm7");

	map.put("nm5", aentry);
	map.put("nm6", new EjwsAuthenticator.Entry());
	map.put("nm7", aentry);
	map.put("nm8", new EjwsAuthenticator.Entry());
	    
	System.out.println("(new entries only)");
	store.mapStream().forEach((event) -> {
		System.out.format("%s, %b, %s, %b\n",
				  event.getKey(),
				  event.getValue().pwmode,
				  event.getValue().data,
				  event.getValue().isActive);
	    });

	store.close();
	
	System.out.println("--------------------");

	store = new SBLStore(new File("sblstoretest"));
	store.mapStream().forEach((event) -> {
		System.out.format("%s, %b, %s, %b\n",
				  event.getKey(),
				  event.getValue().pwmode,
				  event.getValue().data,
				  event.getValue().isActive);
	    });

	store.clearMap(); // cleanup

	Set<String> users = store.getUsers(map, true);
	System.out.println("active users:");
	for (String name: users) {
	    System.out.println("    " + name);
	}

	users = store.getUsers(map, false);
	System.out.println("inactive users:");
	for (String name: users) {
	    System.out.println("    " + name);
	}
	store.close();
    }
}
