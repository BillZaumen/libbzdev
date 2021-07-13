import org.bzdev.graphs.*;

/**
 * Custom axis for a time axis in units of seconds, where the
 * values are printed in minutes.
 */
public class TimeAxis extends Graph.Axis {
    /**
     * Constructor.
     * @param startX the x coordinate of the axis' starting point in
     *        graph coordinate space
     * @param startY the y coordinate of the axis' starting point in
     *        graph coordinate space
     * @param dir the direction of the graph
     * @param length the length of the axis in graph coordinate space
     * @param tickBase the starting coordinate along the axis for
     *        graph ticks, given in graph-coordinate space
     * @param tickIncr the increment between possible tick locations
     *    in graph coordinate space units
     * @param counterClockwise the angular direction to follow to
     *        reach a graph's labels and tick marks
     */
    public TimeAxis(double startX, double startY, Graph.Axis.Dir dir,
		    double length, double tickBase, double tickIncr,
		    boolean counterClockwise) {
	super(startX, startY, dir, length, tickBase, tickIncr,
	      counterClockwise);
    }
    @Override
    public double axisValue(long ind) {
	return (double)(ind/60);
    }
}