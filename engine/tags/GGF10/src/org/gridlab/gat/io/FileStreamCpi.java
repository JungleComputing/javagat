package org.gridlab.gat.io;

import java.io.IOException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the FileStream class.
 * <p>
 * Capability provider wishing to provide the functionality 
 * of the FileStream class must extend this class and 
 * implement all of the abstract methods in this class. Each 
 * abstract method in this class mirrors the corresponding 
 * method in this FileStream class and will be used to 
 * implement the corresponding method in the FileStream class 
 * at runtime.
 */
public abstract class FileStreamCpi implements Monitorable, Streamable
{
  protected int mode = 0;
  protected Location location = null;
  protected GATContext gatContext = null;
  
  /**
   * This creates a FileStreamCpi attached to the physical file at the
   * specified Location. The file may be opened in several modes:
   * <ul>
   *     <li>FileStream.read --- Open file for reading.  The stream 
   *         is positioned at the beginning of the file.</li>
   *     <li>FileStream.write --- Truncate file to zero length or 
   *         create file for writing.  The stream is positioned at 
   *         the beginning of the file.</li>
   *     <li>FileStream.readwrite --- Open for reading and writing.
   *         The stream is positioned at the beginning of the file.</li>
   *     <li>FileStream.append --- Open for appending (writing at end of
   *         file).  The file is created if it does not exist.  The 
   *         stream is positioned at the end of the file.</li>
   * </ul>
   *
   * @param gatContext The GATContext used to broker resources
   * @param location The Location of the file to open.
   * @param mode The mode to open it --- read, write, readwrite, or append,
   * member variables of the FileStream class
   */
   public FileStreamCpi(GATContext gatContext, Location location, Integer mode)
   {
       this.location = location;
       this.gatContext = gatContext;
       this.mode = mode.intValue();
   }
   
  /**
   * This returns the current position in the file.
   *
   * @return The current position in the file, a long
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public abstract long position() throws IOException;
   
  /**
   * This changes the current position in the file.  The position can
   * either be calculated from the current position in the file or from the
   * beginning or end. 
   *
   * @param offset A long,  from <em>whence</em>
   * @param whence One of the member variables used for this purpose, 
   * beginning, current or end of file.
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public abstract void seek(long offset, int whence) throws IOException;
   
  /**
   * Make sure that any underlying file-writing mechanisms have flushed
   * data to disk. 
   *
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public abstract void flush() throws IOException;   
}