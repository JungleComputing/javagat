package org.gridlab.gat.io;

import java.io.IOException;
import org.gridlab.gat.util.Buffer;

/**
 * Streamable is a interface which provides methods for connections to
 * external entities such as files or other processes.
 * <p>
 * To send data down a Streamable it is necessary to construct a Buffer and
 * pack it with data.  Similarly, to receive data a buffer must be created
 * in which the data will be stored.  Sends and receives may either be
 * blocking, or asynchronous.  Asynchronous sends or receives must be
 * completed by an appropriate call.
 * <p>
 * The method of setting up a connection is the responsibility of the
 * derived class (e.g. FileStream or Stream).
 */
public interface Streamable
{
   /**
    * Reads from this Streamable into the given buffer. 
    * <p>
    * This is a blocking call and only returns if the buffer is full
    * or there is no more data to read.
    *
    * @param buffer The Buffer into which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void read(Buffer buffer) throws IOException;
    
   /**
    * Writes data from the given Buffer to this Streamable.  The buffer is not
    * modified.
    * <p> 
    * This is a blocking call and only returns when all the data in the
    * buffer has been sent.
    *
    * @param buffer The Buffer from which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void write(Buffer buffer) throws IOException;
    
   /**
    * Reads from this Streamable into the given buffer, but returning
    * immediately. The buffer should not be used until a ReadWait has
    * been called on this Streamable.
    * <p>
    * Only one iRead may be active at once on a given Streamable.
    * <p>
    *This is a non-blocking call.
    *
    * @param buffer The Buffer into which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void iRead(Buffer buffer) throws IOException;
    
   /**
    * This finishes the current iRead call on this Streamable.  All data
    * received between posting the iRead and this call will be available
    * in the associated buffer on return.
    *
    * @return A boolean indicating if data was received
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public boolean readFinish() throws IOException;
    
    
   /**
    * This tests if data has been received in the current iRead.
    *
    * @return A boolean indicating if data was received
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public boolean readTest() throws IOException; 
    
   /**
    * Writes data from the given Buffer to the Streamable, but returning
    * immediately.  The buffer should not be used until a WriteWait has been
    * called on this Streamable.  The buffer is not modified, but from
    * writeFinish() the position of the next item in the buffer which would 
    * be sent is returned, and this can be used to remove old data from 
    * the buffer.
    * <p>
    * This is a non-blocking call.
    *
    * @param buffer The Buffer from which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void iWrite(Buffer buffer) throws IOException;
    
   /**
    * This finishes the current iWrite call on this Streamable.  Not all data
    * in the buffer may have been sent.
    *
    * @return The position in the buffer right after the last data sent, a long
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public long writeFinish() throws IOException;
    
   /**
    * Tests if data has been sent from  the current iWrite call on this Streamable.
    *
    * @return The position in the buffer right after the last data sent, a long
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public long writeTest() throws IOException;
    
   /**
    * Closes this Streamable instance.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void close() throws IOException;
}