/**
 * The org.bzdev.devqsim.rv package provides named objects that
 * contain random variables.  These objects implement interfaces used
 * by both named objects and random variables, and provide the basic
 * operations shared by all named objects and random variables.  The
 * primary rationale for this package is to simply the use of
 * distributed applications by allowing random variables to be
 * referenced by name. The class hierarchy is organized in the same
 * way as in the package {@link org.bzdev.math.rv}.
 * <P>
 * Classes in this package and in {@link org.bzdev.math.rv} implement
 * the interface {@link org.bzdev.math.rv.RandomVariableOps}, which
 * provides methods common to all random variables.  There are two
 * additional interfaces:
 * <UL>
 *   <LI> {@link org.bzdev.math.rv.RandomVariableRVOps} is implemented
 *     by random numbers that generate other random numbers.
 *   <LI> {@link org.bzdev.math.rv.RandomVariableRVNOps}  is implemented
 *     by random numbers that generate other random numbers that in
 *     turn generate numbers.
 * </UL>
 * The method {@link org.bzdev.devqsim.rv.SimRandomVariable#getRandomVariable()}
 * can be used to recover the random variable used internally&mdash;those
 * random variables sometimes have additional methods for performance reasons.
 * <P>
 * In addition, classes in this package implement
 * {@link org.bzdev.obnaming.NamedObjectOps}, which provides all of the
 * operations expected for named objects. As named objects, they have a
 * common base class: {@link org.bzdev.devqsim.DefaultSimObject}.
 * Please see
 * <A HREF= "{@docRoot}/org.bzdev.math/org/bzdev/math/rv/doc-files/description.html">
 * the description</A> for the {@link org.bzdev.math.rv} package for
 * further details regarding the random-variable properties.
 */
package org.bzdev.devqsim.rv;

//  LocalWords:  getRandomVariable HREF mdash
