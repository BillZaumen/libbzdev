package org.bzdev.anim2d;

/**
 * Factory for creating a graph view.
 * This factory provides the same parameters and object-initialization
 * methods as AbstrGraphViewFactory. The parameters are:
 * <ul>
 *   <li> "initialX" - the initial X coordinate of a GraphView in graph
 *         coordinate space.
 *   <li> "initialY" - the initial Y coordinate of a GraphView in graph
 *         coordinate space.
 *   <li> "xFrameFraction" - the fractional distance from the graph's
 *        left offset to its right offset at which the point specifying the
 *        X coordinate of a GraphView's location appears. This point's
 *        graph coordinate space coordinates are (initialX, initialY).
 *   <li> "yFrameFraction" - the fractional distance from the graph's
 *        lower offset to its upper offset at which the point specifying the
 *        Y coordinate of the graph's location appears. This point's
 *        graph coordinate space coordinates are (initialX, initialY).
 *   <li> "scaleX" - the scale factor for the X direction (the amount by
 *        which to multiple a distance in graph coordinate space along the
 *        X axis to get the corresponding distance in user space).
 *   <li> "scaleY" -  the scale factor for the X direction (the amount by
 *        which to multiple a distance in graph coordinate space along the
 *        Y axis to get the corresponding distance in user space).
 *   <li> "zoom" - the zoom level, which must be a positive real number.
 *   <li> "rotation"  - the angle in degrees by which the graph view is
 *        rotated about its location. Positive values indicate that the
 *        view rotates counterclockwise, corresponding to objects in the
 *        view appearing to rotate clockwise.
 *   <li> "timeline" - an integer-keyed set of values that define
 *        changes in an object as it traverses a path
 *        <ul>
 *          <li> "timeline.time" - the time at which timeline parameters
 *               are to change (typically measured in seconds, not
 *               simulation ticks).
 *          <li> "timeline.x" - the X coordinate of the reference point
 *               at the specified time, provided in graph coordinate
 *               space units. If this parameter is not defined for the key,
 *               the previous value is not changed.
 *          <li> "timeline.y" - the Y coordinate of the reference point
 *               at the specified time, provided in graph coordinate space
 *               units. If this parameter is not defined for the key,
 *               the previous value is not changed.
 *          <li> "timeline.path" - the path that the object should follow.
 *               If 'animation' is the current Animation2D,
 *               {@link Animation2D#nullPath() animation.nullPath()}
 *               will provide a constant path that indicates that the
 *               path should no longer be used. If no value is given,
 *               the value is not changed.
 *          <li> "timeline.t0" - the initial time interval from the time at
 *               which the path is set to the time at which the path traversal
 *               starts
 *          <li> "timeline.u0" - the initial value of the path's parameter
 *               (the   default is 0.0).
 *          <li> "timeline.velocity" - the velocity along the path.
 *          <li> "timeline.acceleration" - the acceleration along the path.
 *          <li> "timeline.distanceFunction" - a SimFunction object
 *                associated with this animation giving the
 *                distance along the path as a function of time. If
 *                'animation' is the current Animation2D,
 *                {@link Animation2D#nullFunction() animation.nullFunction()}
  *                will provide a function that indicates that the
 *                current function should no longer be used. If no
 *                value is given, the function is not changed.

 *          <li> "timeline.angleFunction" - a SimFunction object
 *                associated with this animation giving the angle
 *                of the object as it moves along the path as a
 *                function of time. If 'animation' is the current
 *                Animation2D,
 *                {@link Animation2D#nullFunction() animation.nullFunction()}
 *                will provide a function that indicates that the
 *                current function should no longer be used. If no
 *                value is given, the function is not changed.  The
 *                value returned is in radians, not degrees. If a
 *                path is currently defined, the time argument
 *                treats an argument of zero as the time indicated
 *                by the path's "timeline.t0" parameter. Otherwise
 *                the argument refers to simulation time.  The argument
 *                uses double-precision time.
 *          <li> "timeline.zoomMode" - a selector of type
 *                GraphView.ZoomMode indicating which of the following
 *                4 parameters are valid.  The values are
 *                <CODE>SET_VALUE</CODE> (indicating the
 *                "timeline.zoom" is valid), <CODE>SET_RATE</CODE>
 *                (indicating that "timeline.zoomRate is valid), or
 *                <CODE>SET_TARGET</CODE> (indicating that
 *                "timeline.zoomTarget" and "timeline.zoomInterval"
 *                are valid).  The default value, <CODE>KEEP</CODE>,
 *                indicates that nothing will be changed.
 *          <li> "timeline.zoom" - the zoom level, which must be a
 *                positive real number, to be set at a specified time (see
 *                {@link DirectedObject2DFactory DirectedObject2DFactory}).
 *                This parameter is valid if "timeline.zoomMode" is set
 *                to <CODE>SET_VALUE</CODE>;
 *          <li> "timeline.zoomRate" - the zoom rate, to be set at a
 *                specified time.  The value of zoom will vary as
 *                exp(timeline.zoomRate * (t - time) where t &gt; time and
 *                where time is value specified by "timeline.time" (see
 *                {@link DirectedObject2DFactory DirectedObject2DFactory}).
 *                This parameter is valid if "timeline.zoomMode" is set
 *                to <CODE>SET_RATE</CODE>;
 *          <li> "timeline.zoomTarget" - the desired zoom level, which
 *                must be a positive real number.  This parameter is
 *                valid if "timeline.zoomMode" is set to
 *                <CODE>SET_TARGET</CODE>;
 *          <li> "timeline.zoomInterval" - the interval, starting at a
 *                time specified by "timeline.time" (see
 *                {@link DirectedObject2DFactory DirectedObject2DFactory})
 *                over which the zoom level should change from its
 *                value at the the time specified by
 *                "timeline.time" to the value specified by
 *                "timeline.zoomTarget.  This parameter is valid
 *                if "timeline.zoomMode" is set to <CODE>SET_TARGET</CODE>;
 *           <li> "timeline.rotation" - the angle in degrees by which the graph
 *                 view is rotated about its location. Positive values
 *                 indicate that the view rotates counterclockwise,
 *                 corresponding to objects in the view appearing to
 *                 rotate clockwise. If missing, the current value is not
 *                 changed.
 *           <li> "timeline.rotationRate" - the rate in degrees per
 *                second by which a graph view's rotation angle
 *                changes.  If missing, the current value is not
 *                changed.
 *           <li> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpretted. the values are
 *                enumeration constants of type
 *                {@link org.bzdev.devqsim.TraceSetMode} and are used as
 *                follows:
 *                <ul>
 *                  <li> <code>KEEP</code> - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <li> <code>REMOVE</code> - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <li> <code>REPLACE</code> - remove all existing trace sets
 *                       and repalce those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </ul>
 *           <li> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
*        </ul>
 *   <li> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 * </ul>
 */
public class GraphViewFactory extends AbstrGraphViewFactory<GraphView> {

    /**
     * Constructor.
     * @param a2d the animation
     */
    public GraphViewFactory(Animation2D a2d) {
	super(a2d);
    }

    /**
     * Constructor for service provider.
     * This constructor should not be used directly. It is necessary
     * because of the introduction of modules in Java 9, and is
     * used by a service provider that allows factories to be listed,
     * possibly with documentation regarding their parameters. It
     * jst calls the default constructor with a null argument.
     */
    public GraphViewFactory() {
	this(null);
    }


    @Override
    protected GraphView newObject(String name) {
	return new GraphView(getAnimation(), name, willIntern());
    }
}
