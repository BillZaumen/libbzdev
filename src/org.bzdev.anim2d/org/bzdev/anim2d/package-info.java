/**
 * 2D animation package.
 * Please see <A HREF="{@docRoot}/factories-api/index.html" target="_top">
 * the BZDev library factory documentation</A> for configuring factories
 * and the <A HREF="doc-files/description.html"><B>org.bzdev.anim2d package
 * description</B></A> for additional details.
 * <p>
 * This package, which is based on the devqsim package, provides
 * support for two dimensional animation, typically "stick figures"
 * or simple graphics, although any level of complexity is possible.
 * <P>
 * In order for a simulation to run, one must first call the method
 * {@link org.bzdev.anim2d.Animation2D#initFrames(int,String,String)}
 * {@link org.bzdev.anim2d.Animation2D#initFrames(int,String,String,org.bzdev.io.DirectoryAccessor)},
 * {@link org.bzdev.anim2d.Animation2D#initFrames(int,String,org.bzdev.gio.ISWriterOps)},
 * or
 * {@link org.bzdev.anim2d.Animation2D#initFrames(int,org.bzdev.gio.ISWriterOps)}
 * in order to specify the maximum number of frames that could be generated
 * some data regarding the output file, and the type of the image produced
 * and to create the animation's graph.
 * Then calls to {@link org.bzdev.anim2d.Animation2D#scheduleFrames(long,int) scheduleFrames}
 * will schedule events to generate frames at the appropriate time.
 * Finally, a call to {@link org.bzdev.devqsim.Simulation#run() run},
 * possibly with arguments, will create the animation.
 * <P>
 * <B>Please see <A HREF="{@docRoot}/factories-api/index.html" target="_top">
 * the BZDev library factory documentation</A> for configuring factories
 * and the <A HREF="doc-files/description.html">org.bzdev.anim2d package
 * description</A> for additional details.</B>
 */
package org.bzdev.anim2d;

//  LocalWords:  HREF BZDev devqsim initFrames scheduleFrames
