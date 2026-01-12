import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.security.SecureRandom;
import org.bzdev.graphs.Graph;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.*;
import org.bzdev.util.*;

public class AStar2 {

    private static class Node {
	double x;
	double y;

	public Node(double x, double y) {
	    this.x = x;
	    this.y = y;
	}
    }

    private static class Pair {
	Node n1;
	Node n2;
	public Pair(Node n1, Node n2) {
	    if (n1.x < n2.x) {
		this.n1 = n1;
		this.n2 = n2;
	    } else if  (n2.x < n1.x) {
		this.n1 = n2;
		this.n2 = n1;
	    } else {
		if (n1.y < n2.y) {
		    this.n1 = n1;
		    this.n2 = n2;
		} else if (n2.y < n1.y) {
		    this.n1 = n2;
		    this.n2 = n1;
		} else {
		    throw new IllegalArgumentException ("n1 equals n2");
		}
	    }
	}

	public boolean equals(Object o) {
	    if (o instanceof Pair) {
		Pair opair = (Pair) o;
		return ((n1 ==  opair.n1) && (n2 == opair.n2))
		    || ((n2 == opair.n1) && (n1 == opair.n2));
	    }
	    return false;
	}
    }

    static HashMap<Node,ArrayList<Node>> map = new HashMap<>(2048);

    public static Stream<Node> getNeighbors(Node node) {
	return map.get(node).stream();
    }

    public static  double dist(Node n1, Node n2) {
	double dx = n1.x - n2.x;
	double dy = n1.y - n2.y;
	return Math.sqrt(dx*dx + dy*dy);
    }

    public static void main(String argv[]) throws Exception {

	DoubleRandomVariable rv = new UniformDoubleRV(0.0, true, 100.0, true);

	int ind = 0;
	ArrayList<Node> nodeList = new ArrayList<>(1<<10);

	StaticRandom.maximizeQuality();
						
	for (int i = 0; i < 1000; i++) {
	    double x = rv.next();
	    double y = rv.next();
	    Node node = new Node(x, y);
	    nodeList.add(node);
	}
	Node last = null;
	ArrayList<Node>lastList = new ArrayList<>();
	HashSet<Pair> pairs = new HashSet<>(1<<14);
			     
	for (Node node: nodeList) {
	    ArrayList<Node> nbrList = new ArrayList<>();
	    if (last == null) {
		last = node;
		map.put(last, lastList);
	    } else {
		map.put(node, nbrList);
		lastList.add(node);
		nbrList.add(last);
		lastList = nbrList;
		pairs.add(new Pair(node,last));
		last = node;
	    }
	}
	SecureRandom sr = new SecureRandom();
	for (int i = 0; i < 4; i++) {
	    Collections.shuffle(nodeList, sr);
	    last = null;
	    for (Node node: nodeList) {
		if (last == null) {
		    last = node;
		    lastList = map.get(last);
		} else {
		    Pair pair = new Pair(node, last);
		    if (pairs.contains(pair)) continue;
		    pairs.add(pair);
		    ArrayList<Node> nbrList = map.get(node);
		    lastList.add(node);
		    nbrList.add(last);
		    lastList = nbrList;
		    last = node;
		}
	    }
	}
	Collections.shuffle(nodeList, sr);
	Node start = nodeList.get(0);
	Node end = nodeList.get(nodeList.size() - 1);
	for (Node n: nodeList) {
	    if (n == start) continue;
	    Pair p = new Pair(start, n);
	    if (!pairs.contains(p)) {
		end = n;
		break;
	    }
	}
	Graph graph = new Graph(1024, 1024);
	graph.setRanges(0.0, 100.0, 0.0, 100.0);
	graph.setBackgroundColor(Color.WHITE);
	graph.clear();
	Graphics2D  g2d = graph.createGraphics();
	g2d.setColor(Color.BLACK);
	g2d.setStroke(new BasicStroke(1.5F));
	for (Pair pair: pairs) {
	    Line2D line = new Line2D.Double(pair.n1.x, pair.n1.y,
					    pair.n2.x, pair.n2.y);
	    graph.draw(g2d, line);
	}
	graph.write("png", "astar2.png");

	AStarSearch<Node> search1 = new AStarSearch<>(AStar2::getNeighbors,
						      AStar2::dist,
						      null);

	AStarSearch<Node> search2 = new AStarSearch<>(AStar2::getNeighbors,
						      AStar2::dist,
						      AStar2::dist);

	System.out.println("--------------");
	

	java.util.List<Node> result = search1.search(start,end);
	for (Node n: result) {
	    System.out.format("(%g, %g)\n", n.x, n.y);
	}
	System.out.println("--------");
	result = search2.search(start,end);
	for (Node n: result) {
	    System.out.format("(%g, %g)\n", n.x, n.y);
	}

	System.out.println("--------");
	result = search2.search(start,end,true);
	for (Node n: result) {
	    System.out.format("(%g, %g)\n", n.x, n.y);
	}

	for (int i = 0; i < 10000; i++) {
	    result = search1.search(start,end);
	    result = search2.search(start,end);
	}
	long time1 = System.nanoTime();
	for (int i = 0; i < 10000; i++) {
	    result = search1.search(start,end);
	}
	long time2 = System.nanoTime();
	for (int i = 0; i < 10000; i++) {
	    result = search2.search(start,end);
	}
	long time3 = System.nanoTime();
	for (int i = 0; i < 10000; i++) {
	    result = search2.search(start,end, true);
	}
	long time4 = System.nanoTime();
	
	System.out.println("time2 - time1: " + (time2 - time1));
	System.out.println("time3 - time2: " + (time3 - time2));
	System.out.println("time4 - time3: " + (time4 - time3));
	System.out.println("ratio: " +
			   ((double)(time2 - time1) / (double)(time3 - time2)));
	System.out.println("ratio43: " +
			   ((double)(time4 - time3) / (double)(time3 - time2)));

    }
}
