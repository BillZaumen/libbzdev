/**
 * Module providing classes for the sbl application.
 */
module org.bzdev.sbl {
    exports org.bzdev.bin.sbl;
    requires java.base;
    requires java.desktop;
    requires org.bzdev.base;
    requires org.bzdev.desktop;
    opens org.bzdev.bin.sbl.lpack;
}
