/**
 * Module providing actor-based simulations.
 * <P>
 * This module extends the org.bzdev.devqsim module by providing
 * actor-based simulations where actors send messages to each other,
 * typically with some delay.  There are subsidiary classes to group
 * actors into domains, distribute messages to multiple actors, and
 * to respond to global conditions.
 * <P>
 * The package org.bzdev.drama.generic provides abstract
 * classes with type parameters so that various "flavors" of
 * simulations can be constructed.  The use of type parameters allows
 * names to be changed as well (e.g., in some fields, the term "agent"
 * is used rather than "actor"). The package org.bzdev.drama.common
 * contains some classes that all simulation flavors are likely to use.
 * <P><B>
 * Please see <A HREF="org/bzdev/drama/doc-files/description.html">the Drama package description<A>
 * for the description of a basic actor-based simulation package.</B>
 * @moduleGraph
 */
module org.bzdev.drama {
    exports org.bzdev.drama;
    // exports org.bzdev.providers.drama to org.bzdev.obnaming;
    exports org.bzdev.drama.common;
    exports org.bzdev.drama.generic;
    opens org.bzdev.drama.common.lpack;
    opens org.bzdev.drama.generic.lpack;
    opens org.bzdev.drama.lpack;
    opens org.bzdev.providers.drama.lpack;
    requires java.base;
    requires java.scripting;
    requires transitive org.bzdev.base;
    requires transitive org.bzdev.devqsim;
    requires org.bzdev.obnaming;
    provides org.bzdev.obnaming.spi.ONLauncherProvider with
	org.bzdev.providers.drama.DramaSimulationLauncherProvider;
    provides org.bzdev.obnaming.NamedObjectFactory with
	org.bzdev.drama.BooleanConditionFactory,
	org.bzdev.drama.DoubleConditionFactory,
	org.bzdev.drama.IntegerConditionFactory,
	org.bzdev.drama.LongConditionFactory,
	org.bzdev.drama.DomainFactory,
	org.bzdev.drama.DomainMemberFactory;
}

//  LocalWords:  HREF
