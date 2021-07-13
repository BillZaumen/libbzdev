/**
 * Module providing classes for the scrunner application.
 */
module org.bzdev.scrunner {
    exports org.bzdev.bin.scrunner;
    requires java.base;
    requires java.scripting;
    requires org.bzdev.base;
    requires org.bzdev.math;
    requires org.bzdev.obnaming;
    opens org.bzdev.bin.scrunner.lpack;
}
