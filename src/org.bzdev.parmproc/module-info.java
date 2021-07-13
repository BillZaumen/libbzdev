/**
 * Module providing an annotation processor for NamedObjectFactory.
 * <P>
 * This module provides an annotation processor used by
 * the Java compiler.  It is not needed at runtime.
 */
module org.bzdev.parmproc {
    opens org.bzdev.obnaming.processor;
    requires org.bzdev.base;
    requires org.bzdev.math;
    requires org.bzdev.obnaming;
    requires java.base;
    requires java.compiler;
    provides javax.annotation.processing.Processor
	with org.bzdev.obnaming.processor.ObjectNamerProcessor,
	org.bzdev.obnaming.processor.ParmManagerProcessor;
}
