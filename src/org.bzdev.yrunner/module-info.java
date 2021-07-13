/**
 * Module providing classes for the scrunner application.
 */
module org.bzdev.yrunner {
    exports org.bzdev.bin.yrunner;
    requires java.base;
    requires org.bzdev.base;
    requires org.bzdev.math;
    requires org.bzdev.obnaming;
    opens org.bzdev.bin.yrunner.lpack;
}
