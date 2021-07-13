package org.bzdev.net;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Service to remove sockets in a TCP CLOSE_WAIT state.
 * Tests with Java 11 (this may change eventually) show that
 * {@link com.sun.net.httpserver.HttpsServer} (or some class that
 * it uses) does not always shut down TCP connections properly,
 * leaving connections in the TCP CLOSE_WAIT state indefinitely.
 * This also leads to an increasing number of
 * {@link com.sun.net.httpserver.HttpsServer} threads that are
 * deadlocked.  This class provides a work-around for this issue
 * by fordable turning connections that have been sitting in
 * a CLOSED_WAIT state. This will cause a thread to fail, but
 * in the {@link com.sun.net.httpserver.HttpsServer} case, among
 * others, and {@link java.util.concurrent.ExecutorService} is used to manage
 * threads and can create a new thread if an existing one fails.
 * <P>
 * This class is trivial to use: one first calls the contracts
 * to set it up, providing the socket used by a server as an
 * argument, plus a timeout and a polling interval.  Then the
 * service can be started using the {@link CloseWaitService#start()}
 * method and stopped using the {@link CloseWaitService#stop()}
 * method.
 * <P>
 * The method {@link CloseWaitService#start()} must be
 * called with root privileges, and the program <CODE>ss</CODE>
 * must be installed (making this class Linux-specific). To use
 * this class on systems other than Linux one should use a container
 * or virtual machine to provide a Linux environment, which can
 * be easily done with various "platform as a service" tools such
 * as Docker.
 */
public class CloseWaitService {

    long timeout;		// timeout in milliseconds
    long interval;		// time between updates in milliseconds
    int port;
    boolean wildcard;
    InetAddress addr;

    private class Pair {
	InetAddress addr;
	int port;
	public Pair(InetAddress addr, int port) {
	    this.addr = addr;
	    this.port = port;
	}
	@Override
	public int hashCode() {
	    return addr.hashCode() ^ port;
	}
	@Override
	public boolean equals(Object o) {
	    if (o instanceof Pair) {
		Pair opair = (Pair) o;
		return addr.equals(opair.addr) && (port == opair.port);
	    } else {
		return false;
	    }
	}
    }

    HashSet<Pair> localAddresses = null;

    private void listAddresses(int port)  {
	localAddresses = new HashSet<Pair>();
	try {
	    NetworkInterface.networkInterfaces()
		.forEach((interFace) -> {
			listAddresses(interFace, port);});
	} catch (Exception e) {
	}
    }

    private void listAddresses(NetworkInterface interFace, int port) {
	try {
	    interFace.subInterfaces().forEach((iface) -> {
		    listAddresses(iface, port);
		});
	    interFace.inetAddresses().forEach((addr) -> {
		    localAddresses.add(new Pair(addr, port));
		});
	} catch (Exception e) {
	}
    }

    private class SocketKey {
	String srcaddr;
	String dstaddr;
	String tag;
	long timestamp;
	public SocketKey(String s, String d, String tag, long t) {
	    srcaddr = s;
	    dstaddr = d;
	    this.tag = tag;
	    timestamp = t;
	}
	@Override
	public int hashCode() {
	    return tag.hashCode();
	}
	// do not test the timestamp - we used a set instead of
	// a map for efficiency reasons.
	@Override
	public boolean equals(Object other) {
	    if (other instanceof SocketKey) {
		SocketKey peerkey = (SocketKey) other;
		return srcaddr.equals(peerkey.srcaddr)
		    && dstaddr.equals(peerkey.dstaddr)
		    && tag.equals(peerkey.tag);
	    } else {
		return false;
	    }
	}
    }

    LinkedHashSet<SocketKey> addrSet = new LinkedHashSet<>(64);
    LinkedHashSet<SocketKey> newSet = new LinkedHashSet<>(64);

    private void updateSets() {
	Iterator<SocketKey> it = addrSet.iterator();
	while (it.hasNext()) {
	    SocketKey key = it.next();
	    if (newSet.contains(key)) {
		newSet.remove(key);
	    } else {
		it.remove();
	    }
	}
	for (SocketKey key: newSet) {
	    addrSet.add(key);
	}
	newSet.clear();
    }

    private void prune() {
	long t = System.currentTimeMillis();
	Iterator<SocketKey> it = addrSet.iterator();
	while (it.hasNext()) {
	    SocketKey key = it.next();
	    long diff = (t - key.timestamp) - timeout;
	    if (diff > 0) {
		it.remove();
		prune(key);
	    } else if (diff > 1000) {
		// leave some room for timing errors.
		break;
	    }
	}
    }

    private void scan() {
	ProcessBuilder pb =
	    new ProcessBuilder("ss", "-n", "-t", "-e", "-H",
			       "state", "close-wait");
	try {
	    Process p = pb.start();
	    BufferedReader r = new
		BufferedReader(new InputStreamReader(p.getInputStream()));
	    r.lines().forEach((line) -> {
		    String[] fields = line.split("[ \t]+");
		    String src = fields[2];
		    String dst = fields[3];
		    String tags = fields[4] +" " + fields[5] + " " + fields[6];
		    addEntry(src, dst, tags);
		});
	    r.close();
	    p.waitFor();
	} catch (IOException e) {
	} catch (InterruptedException e) {
	}
    }


    private void prune(SocketKey key) {
	ProcessBuilder pb = new
	    ProcessBuilder("ss", "-n", "-t", "-K",
			   "src", key.srcaddr,
			   "dst", key.dstaddr);
	pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
	try {
	    Process p = pb.start();
	    p.waitFor();
	} catch (IOException e) {
	} catch (InterruptedException e) {
	}
    }

    private void addEntry(String src, String dst, String tag) {
	int ind = src.lastIndexOf(":");
	if (ind == -1) return;
	try {
	    InetAddress addr1 = InetAddress.getByName(src.substring(0, ind));
	    int port1 = Integer.parseInt(src.substring(ind+1));
	    ind = dst.lastIndexOf(":");
	    if (ind == -1) return;
	    InetAddress addr2 = InetAddress.getByName(dst.substring(0, ind));
	    int port2 = Integer.parseInt(dst.substring(ind+1));
	    if (wildcard) {
		for (Pair iaddrPair: localAddresses) {
		    int iport = iaddrPair.port;
		    InetAddress iaddr = iaddrPair.addr;
		    if ((port1 == iport && iaddr.equals(addr1))
			|| (port2 == iport && addr.equals(addr2))) {
			SocketKey key = new
			    SocketKey(src, dst, tag,
				      System.currentTimeMillis());
			newSet.add(key);
			break;
		    }
		}
	    } else {
		if ((port1 == port && addr.equals(addr1))
		    || (port2 == port && addr.equals(addr2))) {
		    SocketKey key = new SocketKey(src, dst, tag,
						  System.currentTimeMillis());
		    newSet.add(key);
		}
	    }
	} catch (Exception e) {
	}
    }
	

    /**
     * Constructor.
     * The socket address must specify a TCP port number larger than
     * 0, but the IP address may be a wildcard address. Multiple wildcard
     * addresses can be provided, each for a different port (duplicates
     * will be ignored).
     * @param timeout the timeout in seconds
     * @param interval the interval at which the network state is
     *        polled, expressed in units of seconds
     * @param saddr the socket address to monitor
     */
    public CloseWaitService(int timeout, int interval,
			    InetSocketAddress... saddr) {
	this.timeout = timeout*1000L;
	this.interval = interval*1000L;
	if (saddr.length == 1) {
	    port =  saddr[0].getPort();
	    addr = saddr[0].getAddress();
	    wildcard = addr.isAnyLocalAddress();
	    if (wildcard) listAddresses(port);
	} else {
	    for (int i = 0; i < saddr.length; i++) {
		port =  saddr[i].getPort();
		addr = saddr[i].getAddress();
		wildcard = addr.isAnyLocalAddress();
		if (wildcard) {
		    listAddresses(port);
		} else {
		    localAddresses.add(new Pair(addr, port));
		}
	    }
	    // the service actually uses the wildcard value to determine
	    // if there is a single address or multiple addresses that
	    // have to be matched.
	    wildcard = true;
	}
    }

    boolean stopRequest = false;

    Thread thread = null;

    /**
     * Start the service.
     */
    public synchronized void start() {
	if (thread != null) return;
	try {
	    thread = new Thread(() -> {
		    for (;;) {
			if (stopRequest) {
			    addrSet.clear();
			    newSet.clear();
			    break;
			}
			scan();
			updateSets();
			prune();
			try {
			    Thread.currentThread().sleep(interval);
			} catch (InterruptedException e) {
			}
		    }
	    });
	    thread.start();
	} catch (Exception e) {
	}
    }

    /**
     * Stop the service.
     */
    public synchronized void stop() {
	if (thread == null) return;
	stopRequest = true;
	try {
	    thread.interrupt();
	    thread.join();
	    addrSet.clear();
	    newSet.clear();
	    stopRequest = false;
	    thread = null;
	} catch (InterruptedException e) {
	}
    }
}

//  LocalWords:  TCP CloseWaitService ss src dst saddr
