module ourmod {
    exports ourpkg;
    requires java.base;
    requires org.bzdev.base;
    requires org.bzdev.drama;
    requires org.bzdev.obnaming;
    provides org.bzdev.obnaming.NamedObjectFactory with
	ourpkg.Factory;
}
