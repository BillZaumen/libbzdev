package org.bzdev.devqsim;

/**
 * Mode for how tracesets are managed by a SimObjectFactory's timeline.
 * Each entry in the timeline has a traceset mode indicating how the
 * trace set is handled.
 */
public enum TraceSetMode {
    /**
     * Keep the existing set, possibly adding to it.
     * (This is the default.)
     */
    KEEP,

    /**
     * Each TraceSet that is provided in the current timeline entry
     * will be removed from an object's TraceSet list.
     */
    REMOVE,

    /**
     * The previous TraceSet list will be replaced.  If no trace sets
     * are provided as a replacement, the TraceSet list will be cleared.
     */
    REPLACE
}
//  LocalWords:  tracesets SimObjectFactory's traceset TraceSet
