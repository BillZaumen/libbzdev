package org.bzdev.graphs;

/**
 * Interface for objects that create a graph.
 * The graph may not be initially available, but
 * the graph's width and height will be available.
 * <P>
 * The class {@link org.bzdev.anim2d.Animation2D}
 * implements this interface. It is used by a constructor
 * for the class {@link org.bzdev.p3d.Model3D.Image} so
 * that objects that initially store the dimensions of a
 * graph, but that do not initially have the graph available,
 * can be used in one an {@link org.bzdev.p3d.Model3D.Image}
 * constructor.  Other classes not defined by this class
 * library may have similar requirements.
 */
public interface GraphCreator {

    /**
     * Get the graph that this object creates.
     * <P>
     * Note: For some implementations of GraphCreator
     * the graph may not be initially available, but will
     * be created eventually.  The width and height, however,
     * will be available immediately.
     * @return the graph; null if one is not available
     * @see #getWidth()
     * @see #getHeight()
     * @see #getWidthAsInt()
     * @see #getHeightAsInt()
     */
    Graph getGraph();

    /**
     * Get the width of the graph as an integer.
     * The width is in user-space units. The corresponding
     * graph may or may not have been created when this method
     * is called.
     * @return the width
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    int getWidthAsInt();
    /**
     * Get the height of the graph as an integer.
     * The height is in user-space units. The corresponding
     * graph may or may not have been created when this method
     * is called.

     * @return the width
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    int getHeightAsInt();

    /**
     * Get the width of the graph.
     * The corresponding graph may or may not have been be created
     * when this method is called.
     * @return the width
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    default double getWidth() {
	return (double) getWidthAsInt();
    }

    /**
     * Get the height of the graph.
     * The corresponding graph may or may not have been created when
     * this method is called.
     * @return the width in user-space units
     * @see #getGraph()
     * @see org.bzdev.graphs.Graph
     */
    default double getHeight() {
	return (double) getHeightAsInt();
    }

}

//  LocalWords:  GraphCreator getWidth getHeight getWidthAsInt
//  LocalWords:  getHeightAsInt getGraph
