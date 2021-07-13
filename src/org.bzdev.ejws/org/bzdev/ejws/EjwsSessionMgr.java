package org.bzdev.ejws;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bzdev.net.ServerCookie;
import org.bzdev.net.HeaderOps;
import org.bzdev.net.HttpSessionOps;


class EjwsSessionMgr extends Filter {
 
   HttpSessionOps<?> sessionOps = null;

    static final String JSESSIONID = "Jsessionid";

    Map<String,EjwsSession> map = new
	HashMap<String,EjwsSession>(64);
    Set<EjwsSession> set = new TreeSet<EjwsSession>((s1, s2) -> {
	    long t1 = s1.lastAccessedTime + s2.maxInactiveInterval*1000L;
	    long t2 = s2.lastAccessedTime + s2.maxInactiveInterval*1000L;
	    if (t1 < t2) return -1;
	    else if (t1 > t2) return 1;
	    else return 0;
    });
    
    public EjwsSessionMgr(HttpSessionOps sessionOps) {
	super();
	this.sessionOps = sessionOps;
    }
    
    @Override
    public String description() {return "http session manager";}

    @Override
    public void doFilter(HttpExchange exchange, Filter.Chain chain)
	throws IOException
    {
	long currentTime = System.currentTimeMillis();
	synchronized (this) {
	    Iterator<EjwsSession> it = set.iterator();
	    while (it.hasNext()) {
		EjwsSession session = it.next();
		if ((session.lastAccessedTime
		     + session.maxInactiveInterval*1000L)
		    < currentTime) {
		    String sid = session.getID();
		    it.remove();
		    map.remove(sid);
		    if (sessionOps != null) {
			sessionOps.remove(sid);
		    }
		} else {
		    break;
		}
	    }
	}
	Headers headers = exchange.getRequestHeaders();
	ServerCookie[] cookies = ServerCookie.fetchCookies
	    (WebMap.asHeaderOps(exchange.getRequestHeaders()));
	for (int i = 0; i < cookies.length; i++) {
	    ServerCookie cookie = cookies[i];
	    String name = cookie.getName();
	    if (name.equalsIgnoreCase(JSESSIONID)) {
		String sid = cookie.getValue();
		EjwsSession session;
		synchronized(this) {
		    session = map.get(sid);
		    if (session != null) {
			set.remove(session);
			session.lastAccessedTime = currentTime;
			session.isNewID = false;
			set.add(session);
		    } else {
			session = new EjwsSession(this);
		    }
		}
		exchange.setAttribute("org.bzdev.ejws.session", session);
		chain.doFilter(exchange);
		return;
	    }
	}
	EjwsSession s;
	synchronized(this) {
	    s = new EjwsSession(this);
	}
        exchange.setAttribute("org.bzdev.ejws.session", s);
	chain.doFilter(exchange);
	return;
    }
}


//  LocalWords:  Jsessionid http
