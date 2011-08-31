/**
 * This package contains classes and interfaces which are used to provide input
 * and output.
 * Files and streams can be either local or remote. The most
 * important classes and interfaces are {@link org.gridlab.gat.io.File File},
 * {@link org.gridlab.gat.io.LogicalFile LogicalFile},
 * the stream classes, and the corresponding capability provider interfaces.
 * An instance of the class {@link org.gridlab.gat.io.File File} is an abstract
 * representation of a physical file and is used to perform operations on an
 * entire file.
 * An instance of the class {@link org.gridlab.gat.io.LogicalFile LogicalFile}
 * is an abstract representation of a set of identical physical files and is
 * used to facilitate data transfer.
 * {@link org.gridlab.gat.io.FileInputStream FileInputStream} and
 * {@link org.gridlab.gat.io.FileOutputStream FileOutputStream}
 * are used to perform IO.
 * The various capability provider interfaces are used by an
 * adaptor writer to implement the functionality contained in this package.
 */

package org.gridlab.gat.io;
