/**
 * Module providing classes for the lnsof command-line program.
 */
module org.bzdev.lsnof {
    exports org.bzdev.bin.lsnof;
    requires java.base;
    requires org.bzdev.base;
    requires org.bzdev.obnaming;
    requires org.bzdev.math;
    opens org.bzdev.bin.lsnof to java.base, org.bzdev.base,
	org.bzdev.obnaming, org.bzdev.math;
    opens org.bzdev.bin.lsnof.lpack;
}
