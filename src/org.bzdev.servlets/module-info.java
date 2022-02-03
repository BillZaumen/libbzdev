/**
 * Module for encapsulating an instance of
 * {@link org.bzdev.net.ServletAdapter} in a servlet.
 * This module has a dependency  on the servlet API and
 * as a result the org.bzdev module does require this
 * module.
 */
module org.bzdev.servlets {
    exports org.bzdev.net.servlets;
    requires org.bzdev.base;
    requires servlet.api;
}
