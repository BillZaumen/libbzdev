/**
 * Module providing discrete-event simulations.
 * <P>
 * This packages provides a discrete-event simulation with support for
 * queuing simulations.  The basic mechanism involves scheduling
 * lambda expressions or tasks on an event queue.  There are also
 * various queues that allow a task or piece of code to wait until
 * other events have been processed.  The module also provides
 * debugging support and instrumentation.
 * <P>
 * <B>Please see
 * <A HREF="org/bzdev/devqsim/doc-files/description.html">the org.bzdev.devqsim package description</A>
 * for an extended description of this module's use.</A></B>
 */
module org.bzdev.devqsim {
    exports org.bzdev.devqsim;
    // exports org.bzdev.providers.devqsim to org.bzdev.obnaming;
    exports org.bzdev.devqsim.rv;
    opens org.bzdev.devqsim.lpack;
    opens org.bzdev.devqsim.rv.lpack;
    opens org.bzdev.providers.devqsim.lpack;
    requires java.base;
    requires java.scripting;
    requires transitive org.bzdev.base;
    requires transitive org.bzdev.math;
    requires org.bzdev.obnaming;
    provides org.bzdev.obnaming.spi.ONLauncherProvider with
	org.bzdev.providers.devqsim.SimulationLauncherProvider;
    provides org.bzdev.lang.spi.ONLauncherData with
	org.bzdev.providers.devqsim.SimulationRVLauncherData;
    provides org.bzdev.obnaming.NamedObjectFactory with
	org.bzdev.devqsim.FifoTaskQueueFactory,
	org.bzdev.devqsim.LifoTaskQueueFactory,
	org.bzdev.devqsim.WaitTaskQueueFactory,
	org.bzdev.devqsim.PriorityTQFactory,
	org.bzdev.devqsim.ProcessClockFactory,
	org.bzdev.devqsim.SimFunctionFactory,
	org.bzdev.devqsim.SimFunctionTwoFactory,
	org.bzdev.devqsim.TraceSetFactory,
	org.bzdev.devqsim.rv.SimBinomialBoolRVFactory,
	org.bzdev.devqsim.rv.SimBinomialBoolRVRVFactory,
	org.bzdev.devqsim.rv.SimBinomialDblRVFactory,
	org.bzdev.devqsim.rv.SimBinomialDblRVRVFactory,
	org.bzdev.devqsim.rv.SimBinomialIATimeRVFactory,
	org.bzdev.devqsim.rv.SimBinomialIATimeRVRVFactory,
	org.bzdev.devqsim.rv.SimBinomialIntRVFactory,
	org.bzdev.devqsim.rv.SimBinomialIntRVRVFactory,
	org.bzdev.devqsim.rv.SimBinomialLongRVFactory,
	org.bzdev.devqsim.rv.SimBinomialLongRVRVFactory,
	org.bzdev.devqsim.rv.SimDetermBoolRVFactory,
	org.bzdev.devqsim.rv.SimDetermDblRVFactory,
	org.bzdev.devqsim.rv.SimDetermIATimeRVFactory,
	org.bzdev.devqsim.rv.SimDetermIntRVFactory,
	org.bzdev.devqsim.rv.SimDetermLongRVFactory,
	org.bzdev.devqsim.rv.SimFixedBoolRVFactory,
	org.bzdev.devqsim.rv.SimFixedBoolRVRVFactory,
	org.bzdev.devqsim.rv.SimFixedDblRVFactory,
	org.bzdev.devqsim.rv.SimFixedDblRVRVFactory,
	org.bzdev.devqsim.rv.SimFixedIATimeRVFactory,
	org.bzdev.devqsim.rv.SimFixedIATimeRVRVFactory,
	org.bzdev.devqsim.rv.SimFixedIntRVFactory,
	org.bzdev.devqsim.rv.SimFixedIntRVRVFactory,
	org.bzdev.devqsim.rv.SimFixedLongRVFactory,
	org.bzdev.devqsim.rv.SimFixedLongRVRVFactory,
	org.bzdev.devqsim.rv.SimGaussianIATimeRVFactory,
	org.bzdev.devqsim.rv.SimGaussianIATimeRVRVFactory,
	org.bzdev.devqsim.rv.SimGaussianRVFactory,
	org.bzdev.devqsim.rv.SimGaussianRVRVFactory,
	org.bzdev.devqsim.rv.SimLogNormalRVFactory,
	org.bzdev.devqsim.rv.SimLogNormalRVRVFactory,
	org.bzdev.devqsim.rv.SimPoissonDblRVFactory,
	org.bzdev.devqsim.rv.SimPoissonDblRVRVFactory,
	org.bzdev.devqsim.rv.SimPoissonIATimeRVFactory,
	org.bzdev.devqsim.rv.SimPoissonIATimeRVRVFactory,
	org.bzdev.devqsim.rv.SimPoissonIntRVFactory,
	org.bzdev.devqsim.rv.SimPoissonIntRVRVFactory,
	org.bzdev.devqsim.rv.SimPoissonLongRVFactory,
	org.bzdev.devqsim.rv.SimPoissonLongRVRVFactory,
	org.bzdev.devqsim.rv.SimUniformBoolRVFactory,
	org.bzdev.devqsim.rv.SimUniformDblRVFactory,
	org.bzdev.devqsim.rv.SimUniformDblRVRVFactory,
	org.bzdev.devqsim.rv.SimUniformIATimeRVFactory,
	org.bzdev.devqsim.rv.SimUniformIATimeRVRVFactory,
	org.bzdev.devqsim.rv.SimUniformIntRVFactory,
	org.bzdev.devqsim.rv.SimUniformIntRVRVFactory,
	org.bzdev.devqsim.rv.SimUniformLongRVFactory,
	org.bzdev.devqsim.rv.SimUniformLongRVRVFactory;
}

//  LocalWords:  HREF
