/**
 * Package that augments the java.io package.
 * <P>
 * This package contains Writer and Reader classes - a reader for
 * instances of Document and a writer for an Appendable - and a class
 * to create zip documents with an embedded MIME type.
 * <P>
 * In addition, there are a series of classes that provide access to
 * specified files when a security manager is used. Typically, instances
 * of these classes will be created when an application starts and before
 * the security manager is enabled. Code run under the security manager
 * can then obtain input and output streams or random-access files (instances
 * of RandomAccessFile). To use these classes, either of the following
 * permissions is necessary:
 * <blockquote><code><pre>grant codeBase "CODEBASE" {
 *   permission java.io.FilePermission "&lt;&lt;ALL FILES &gt;&gt;" "read,write";
 * };</pre></code></blockquote>
 * or
 * <blockquote><code><pre>grant codeBase "CODEBASE" {
 *  permission java.security.AllPermission;
 * };</pre></code></blockquote>
 * where CODEBASE is the URL for the jar file containing this package.
 * Permissions should be automatically granted if the class library is
 * installed as a Java extension.
 */
package org.bzdev.io;

//  LocalWords:  io Appendable RandomAccessFile blockquote pre lt
//  LocalWords:  codeBase CODEBASE
