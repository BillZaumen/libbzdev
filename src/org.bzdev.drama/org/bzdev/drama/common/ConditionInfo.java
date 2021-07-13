package org.bzdev.drama.common;
import org.bzdev.devqsim.SimObject;

/**
 * Auxiliary data about a condition change.
 * Several classes (actors in particular) contain two methods
 * named onConditionChange(Condition, ConditionMode, SimObject) and
 * onConditionChange(Map&lt;Condition,ConditionInfo&gt;).  The default
 * implementation of the latter calls the former method, obtaining the
 * former method's second and third arguments from an instance of
 * ConditionInfo.
 */
public class ConditionInfo {
    ConditionMode mode;
    SimObject source;

    /**
     * Get the condition mode for the change.
     * @return the condition mode
     */
    public ConditionMode getMode() {return mode;}

    /**
     * Get the source of a change.
     * @return the source
     */
    public SimObject getSource() {return source;}

    public ConditionInfo(ConditionMode mode, SimObject source) {
	this.mode = mode;
	this.source = source;
    }
}

//  LocalWords:  onConditionChange ConditionMode SimObject lt
//  LocalWords:  ConditionInfo
