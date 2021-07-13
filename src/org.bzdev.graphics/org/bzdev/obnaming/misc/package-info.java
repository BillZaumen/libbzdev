/**
 * Common compound-parameter-type classes.
 * <P>
 * This package contains classes annotated by the {@literal @}CompoundParmType
 * annotation and represents collections of parameters likely to be generally
 * useful for writing obnaming factory classes.
 * <ul>
 *    <li> {@link org.bzdev.obnaming.misc.BasicStrokeParm} has a method
 *         {@link org.bzdev.obnaming.misc.BasicStrokeParm#createBasicStroke()}
 *         that will create an instance of BasicStroke. This method
 *         will return a configured value or a default value.  The default
 *         value will be null if the constructor is called with a single
 *         boolean argument whose value is <code>true</code>. In this case
 *         null is also returned if the width is set to zero.
 *    <li> {@link org.bzdev.obnaming.misc.ColorParm} has a method
 *         {@link org.bzdev.obnaming.misc.ColorParm#createColor()} that
 *         will create an instance of Color. This method will return a
 *         configured value or a default value.  The default value will be
 *         null if ColorParm's constructor is called with no arguments. A
 *         configured value will never be null.
 *    <li> {@link org.bzdev.obnaming.misc.GraphFontParm} has a method
 *         {@link org.bzdev.obnaming.misc.GraphFontParm#createFontParms()}
 *         that will create an instance of Graph.FontParms.  The default value
 *         will be null if the name of the font is null (not the
 *         empty string or the string "null") and can be configured by
 *         calling GraphFontParm's constructor with a single boolean argument
 *         equal to <code>true</code>.
 * </ul>
 * While primitive parameters can be set to null, compound parameters may not
 * be.  Allowing the 'create' methods to return null is useful in cases where
 * one wants to distinguish unconfigured values from configured values.
 * For example, some of the factories in the org.bzdev.anim2d package use
 * a compound parameter to represent an entry in a 'timeline' table.
 * and a null value in this case indicates that the existing values for
 * that parameter should not be changed.
 */
package org.bzdev.obnaming.misc;

//  LocalWords:  CompoundParmType obnaming ul li createBasicStroke
//  LocalWords:  BasicStroke boolean createColor ColorParm's
//  LocalWords:  createFontParms FontParms GraphFontParm's
//  LocalWords:  unconfigured
