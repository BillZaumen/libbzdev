package org.bzdev.p3d;
import java.util.List;
import java.util.LinkedList;

/**
 * Manifold-component exception.
 * This exception may be thrown when it is necessary to compute
 * the components of a manifold.  If the method
 * {@link #hasFailedEdge()} returns true, there is an edge
 * associated with the exception
 */
public class ManifoldException extends IllegalStateException {
    boolean edgeException;

    ManifoldException(String msg, boolean edgeException) {
	super(msg);
	this.edgeException = edgeException;
    }

    /**
     * Determine if the exception is due to a duplicate edge.
     * @return true if the exception is due to a duplicate edge;
     *         false otherwise.
     */
    boolean hasFailedEdge() {
	return edgeException;
    }

    Model3D.Triangle eTriangle1 = null;
    Model3D.Triangle eTriangle2 = null;
    int failedEdge = -1;

    /**
     * Get a list of triangles associated with this exception.
     * The list can have one or two elements.
     * @return the list of triangles associated with this exception
     */
    public List<Model3D.Triangle> getErrorTriangles() {
	if (eTriangle1 == null && eTriangle2 == null) return null;
	LinkedList<Model3D.Triangle> result = new LinkedList<>();
	if (eTriangle1 != null)
	    result.add(eTriangle1);
	if (eTriangle2 != null)
	    result.add(eTriangle2);
	return result;
    }
 
    /**
     * Get the edge number of the directed edge for the second
     * triangle in the list returned by {@link #getErrorTriangles()}
     * that was shared by the first triangle in the list.
     * @return the edge number (0, 1, or 2); -1 if there is none
     */
    public int getFailedEdge() {return failedEdge;}
   

}
//  LocalWords:  hasFailedEdge getErrorTriangles
