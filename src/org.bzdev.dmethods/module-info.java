/**
 * Dynamic-method module for the BZDev class library.
 * <P>
 * This module provides an annotation processor used by
 * the Java compiler.  It is not needed at runtime.
 */
module org.bzdev.dmethods {
    opens org.bzdev.lang.processor;
    requires org.bzdev.base;
    requires java.base;
    requires java.compiler;
    provides javax.annotation.processing.Processor
	with org.bzdev.lang.processor.DMethodProcessor;
}

//  LocalWords:  BZDev runtime
