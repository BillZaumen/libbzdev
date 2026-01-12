package org.bzdev.util;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// See https://www.datacamp.com/tutorial/a-star-algorithm for pseudocode
// describing the algorithm (IMHO the pseudo code is a bit misleading.)

/**
 * A* search.
 * The A* search algorithm finds a path that minimizes a
 * quantity f(n) = g(n) + h(n) where
 * <UL>
 *   <LI> g(n) is the cost or length of a path traversing a graph from
 *        a starting node to node n.
 *   <LI> h(n) (a heuristic) is an estimate of the cost or length of
 *        the shortest path to a desired final node, and must be zero
 *        at the goal. If h(n) &le; d(n,m) + h(m) for each edge (n,
 *        m), where d(n,m) is the distance or cost of an edge
 *        connection node n to node m, then the A* algorithm will find
 *        an optimal path without removing a node from a priority
 *        queue more than once.
 * </UL>
 * <P>
 * For cases where g represent path lengths on a flat surface,
 * a suitable choice of h(n,m) is
 * ((x<SUB>n</SUB>-x<SUB>m</SUB>)<SUP>2</SUP>
 * + (x<SUB>n</SUB>-x<SUB>m</SUB>)<SUP>2</sup>)<SUP>1/2</SUP>
 * where the XY coordinates of n are (x<SUB>n</SUB>,y<SUB>n</SUB>)
 * and the XY coordinates of m are (x<SUB>m</SUB>,y<SUB>m</SUB>).
 * If paths more or less follow a rectilinear grid, then a suitable
 * choice is |x<SUB>n</SUB>-x<SUB>m</SUB>| + |y<SUB>n</SUB>-y<SUB>m</SUB>|.
 * <P>
 * For more details, see the Wikipedia article
 * <A HREF="https://en.wikipedia.org/wiki/A*_search_algorithm">
 * A* search algorithm</A>, which the preceding text paraphrases.
 * <P>

 * This implementation requires that the user provide a data structure
 * representing a node, a function to return a stream of the neighbors
 * of a node, a function g(n,m) that computes the cost of an edge
 * connecting node n to node m, and a function h(n,n<SUB>g</SUB>) that
 * computes an estimate of the cost from node n to the goal (node
 * n<SUB>g</SUB>).  A stream of neighboring nodes should not include
 * duplicates.
 * @param <Node> the type of a node
 */
public class AStarSearch<Node> {
    Function<Node,Stream<Node>> getNeighbors;
    ToDoubleBiFunction<Node,Node> g;
    ToDoubleBiFunction<Node,Node> h = (n1, n2) -> {
	return 0.0;
    };

    private class OurNode extends CachedSkewHeap.Entry<OurNode> {
	Node node;
	double gval;
	double hval;
	double f;
	boolean polled = false;
	OurNode parent;

	OurNode(Node node, double gval, double hval) {
	    this.node = node;
	    this.gval = gval;
	    this.hval = hval;
	    f = gval + hval;
	    parent = null;
	}
    }

    /**
     * Constructor.
     * The first argument is an implementation of a functional
     * interface that must return a new supplier each time it is
     * called.  The supplier must provide the neighboring nodes,
     * without duplicates, for the node passed to getNeighbors,
     * followed by null to indicate that all the neighboring nodes
     * have been provided.  The function g is used to compute the
     * "distance" between neighboring nodes, and the function h is a
     * heuristic estimating the distance from some node to a goal
     * node. When h is null, a default heuristic that produces the same
     * results as Dijkstra's shortest path algorithm is used.
     * <P>
     * Each node n<sub>i</sub> keeps track of two distances:
     * g<sub>i</sub> and h<sub>i</sub>. For the initial node n<sub>s</sub>,
     * g<sub>s</sub> = 0 and h<sub>s</sub> = h(n<sub>s</sub>, n<sub>f</sub>)
     * where n<sub>f</sub> is the final node (or "goal"). A priority queue
     * that initial contains just n<sub>s</sub> determines the order in which
     * nodes are processed. At each step, a node n<sub>i</sub>
     * with the minimum value of g<sub>i</sub> + h<sub>i</sub> is removed
     * from the priority queue. For neighbors of n<sub>i</sub> that have
     * not been previously removed from the priority queue, there are
     * two cases:
     * <UL>
     *   <LI> For a neighbor n<sub>j</sub> that has never been on the
     *        priority queue,
     *        g<sub>j</sub> = g<sub>i</sub> + g(n<sub>i</sub>,n<sub>j</sub>)
     *        and
     *        h<sub>j</sub> = h(n_sub>j</sub>, n<sub>f</sub>) 
     *   <LI> For a neighbor n<sub>j</sub> that is on the priority queue,
     *        g<sub>j</sub> is set to
     *         g<sub>i</sub> + g(n<sub>i</sub>,n<sub>j</sub>) if this would
     *        decrease the value of g<sub>j</sub>.
     * </UL>
     * When the final node is reached, a list of nodes  from the starting node
     * to the final node is returned.
     * @param getNeighbors a function that returns a {@link Stream stream}
     *         of neighboring nodes for node supplied as its argument
     * @param g a function that returns the distance from the node given
     *        by its first argument to the node given by its second
     *        argument.
     * @param h a heuristic function that returns an estimate of the distance
     *         from the node given by its first argument to the node given
     *         by its second argument; null for a default that always
     *         returns 0
     */
    public AStarSearch(Function<Node,Stream<Node>> getNeighbors,
		       ToDoubleBiFunction<Node,Node> g,
		       ToDoubleBiFunction<Node,Node> h) {
	this.getNeighbors = getNeighbors;
	this.g = g;
	if (h != null) {
	    this.h = h;
	}
    }

    /**
     * Search for a path between two nodes.
     * This is equivalent to calling
     * {@link #search(Node,Node,boolean) search(start,goal,false)}.
     * @param start the initial node in a path
     * @param goal the final node in a path
     * @return a list of the nodes from the initial node to the final node,
     *         inclusive
     */
    public List<Node> search(Node start, Node goal) {
	return search(start, goal, false);
    }

    /**
     * Search for a path between two nodes, specifying if a node can not be
     * added to the priority queue after being polled or have its values
     * updated by a neighbor.
     * <P>
     * If the heuristic h(n) satisfies the condition h(n) &le; g(n,m) + h(m),
     * then pollOnce should be set to false as each node will be polled at
     * most once.  Setting pollOnce to true otherwise may reduce the amount
     * of searching performed, but possibly at the cost of finding a worse
     * path.  A timing test suggested a slight increase in running time
     * if pollOnce is set to true when the condition h(n) &le; g(n,m) + h(m)
     * holds.
     * @param start the initial node in a path
     * @param goal the final node in a path
     * @param pollOnce true if a node provided by the priority queue via
     *        a call to poll() will not be added to the priority queue again
     *        or have its values updated by a neighbor
     * @return a list of the nodes from the initial node to the final node,
     *         inclusive
     */
    public List<Node> search(Node start, Node goal, boolean pollOnce) {
	HashMap<Node,OurNode> map = new HashMap();
	OurNode ostart = new OurNode(start, 0.0, h.applyAsDouble(start, goal));
	map.put(start, ostart);
	// The Java class PriorityQueue is not a good choice here because
	// removing a specific object from that priority queue is O(n).
	CachedSkewHeap<OurNode> pq = new CachedSkewHeap<>() {
		protected int compareEntries(CachedSkewHeap.Entry<OurNode> e1,
					     CachedSkewHeap.Entry<OurNode> e2) {
		    OurNode n1 = (OurNode)e1;
		    OurNode n2 = (OurNode)e2;
		    if (n1.f < n2.f) return -1;
		    else if (n1.f == n2.f) return 0;
		    else return 1;
		}
	    };
	pq.add(ostart);
	ArrayList<OurNode> addList = new ArrayList<>();
	while (!pq.isEmpty()) {
	    OurNode ocurrent = pq.poll();
	    if (ocurrent.node.equals(goal)) {
		LinkedList<Node> result = new LinkedList<>();
		result.add(goal);
		OurNode ond = map.get(goal);
		while (ond.parent != null) {
		    ond = ond.parent;
		    result.addFirst(ond.node);
		}
		return result;
	    }
	    ocurrent.polled = true;
	    Stream<Node> neighbors = getNeighbors.apply(ocurrent.node);
	    if (neighbors == null) continue;
	    addList.clear();
	    neighbors.forEach((neighbor) -> {
		    if (neighbor == ocurrent.node) return;
		    OurNode oneighbor = map.get(neighbor);
		    if (pollOnce) {
			if ((oneighbor != null) && oneighbor.polled) {
			    return;
			}
		    }
		    double gvalue = ocurrent.gval
			+ g.applyAsDouble(ocurrent.node, neighbor);

		    if (oneighbor == null) {
			oneighbor = new OurNode(neighbor, gvalue,
						h.applyAsDouble(neighbor,
								goal));
			oneighbor.parent = ocurrent;
			map.put(neighbor, oneighbor);
			addList.add(oneighbor);
		    } else if (gvalue < oneighbor.gval) {
			pq.remove(oneighbor);
			oneighbor.parent = ocurrent;
			oneighbor.gval = gvalue;
			double hval = hval = h.applyAsDouble(neighbor, goal);
			oneighbor.f = oneighbor.gval + oneighbor.hval;
			addList.add(oneighbor);
		    }
		});
	    // delayed so the nodes tend to go onto the CachedSkewHeap's
	    // subsidiary skew heap, amortizing the merge operation.
	    for (OurNode onode: addList) {
		pq.add(onode);
	    }
	}
	return null;
    }
}

//  LocalWords:  pseudocode le XY HREF getNeighbors boolean pollOnce
//  LocalWords:  PriorityQueue CachedSkewHeap's
