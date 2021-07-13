package org.bzdev.devqsim;

/**
 * Simulation Event for Tasks.
 */
abstract public class TaskSimulationEvent extends SimulationEvent {
    TaskThread thread;
    String tag;

     TaskSimulationEvent() {
	 thread = null;
	 tag = null;
    }

     TaskSimulationEvent(String tag) {
	 thread = null;
	 this.tag = tag;
    }

     TaskSimulationEvent(TaskThread thread) {
	 this.thread = thread;
	 tag = null;
    }

    TaskSimulationEvent(TaskThread thread, String tag) {
	 this.thread = thread;
	 this.tag = tag;
    }

}
