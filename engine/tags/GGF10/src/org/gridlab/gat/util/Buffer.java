package org.gridlab.gat.util;

import java.util.Vector;
import java.util.Iterator;
import java.lang.reflect.Array;

/*
 * Notes:
 * 1. The method canUnPack() is not in the origianl documentation !!!
 * 2. The method canPack() is not in the origianl documentation !!!
 * 3. the method getNextPackableType() is not in the original documentation !!!
 * 4. the method getNextUnPackableType() is not in the original documentation !!!
 */
 
/**
 * A Buffer is a container for arbitrary data.  It has a finite capacity,
 * which can be increased or decreased, a current position, which may be
 * reset or moved, and an end position, which is the last position in the
 * buffer with valid data.
 * <p>
 * Data objects of any primitive type may be packed or unpacked from the
 * buffer.
 * <p>
 * <em>Transferring data</em>
 * There are two categories of get and  put operations: 
 * <ul>
 * <li> <em>Relative operations</em> pack or unpack one or more elements
 * starting at the current position and then increment the position to
 * after these elements. If the requested transfer exceeds
 * the capacity then an error is returned and no data is transferred.
 * <li> <em>Absolute operations</em> take an explicit position and do
 * not affect the position. Absolute pack and unpack operations return an
 * error if the initial position exceeds the capacity, or the final
 * position would exceed the capacity of the buffer.
 * </ul>
 * <p> 
 * A newly-created buffer always has a position at its beginning.  The 
 * initial capacity may be set in the constructor.
 * <p>
 * <em>Clearing, etc</em>
 * In addition to methods for accessing the position, limit, and capacity
 * values, this class also defines the following operations upon buffers:
 * <ul>
 * <li> clear() empties the buffer.
 * <li> truncate() clears all data from the buffer beyond the specified
 *      position and resets the current-position and last position to the
 *     specified position.
 * </ul>
 * <p>
 * <em>Thread safety</em>
 * Buffers are not safe for use by multiple concurrent threads. If a
 * buffer is to be used by more than one thread then access to the buffer
 * should be controlled by appropriate synchronisation.
 * <p>
 * <em>Buffer Operations</em>
 * This class defines various categories of operations upon Buffers:   
 * <ul>
 * <li> Relative bulk pack() methods that transfer sequences of objects
 * from an array or list into this Buffer;
 * <li> Relative bulk unpack() methods that transfer sequences of
 * objects from a Buffer into an array or List.
 * </ul>
 */
public class Buffer
{
    protected int pbuffer = 0;
    protected int position = 0;
    protected int epbuffer = 0;
    protected int endPosition = 0;
    protected Vector buffers = null;
    
   /**
    * Creates a new Buffer. The new buffer will be empty, with its position
    * set to the beginning.  The capacity will be calculated from the number
    * of objects specified. 
    *
    * @param capacity  The initial capacity, an int
    * @param clazz The class of elements this Buffer should contain.
    */
    public Buffer(int capacity, Class clazz)
    {
        buffers = new Vector();
        
        buffers.add( Array.newInstance(clazz, capacity) );
    }
 
   /**
    * This method returns a boolean indicating if a call to pack
    * will be able to pack data.
    *
    * @return A boolean indicating if there is space in this buffer
    */
    public boolean canPack()
    {
       if( (pbuffer < 0) || (buffers.size() < (pbuffer + 1)) )
       {
           return false;
       }
       
       return true;
    }
              
   /**
    * This method returns a boolean indicating if a call to unPack
    * will be able to unpack data.
    *
    * @return A boolean indicating if there is at least one element in this buffer
    */
    public boolean canUnPack()
    {
       if( (pbuffer < 0) || (buffers.size() < (pbuffer + 1)) )
         return false;
         
       Object[] array = (Object[]) buffers.elementAt(pbuffer);
       Object object = array[position];
       
       if( null == object )
         return false;
            
       return true;    
    }
    
   /**
    * Returns this buffer's position.
    *
    * @return This buffer's position.
    */
    public int getPosition()
    {
        int absolutePosition = 0;
        
        for(int count = 0; count < pbuffer; count++)
        {
            Object[] array = (Object[]) buffers.elementAt(count);
            absolutePosition = absolutePosition + array.length;
        }
        
        absolutePosition = absolutePosition + position;
        
        return absolutePosition;
    }
    
   /**
    *  Sets this buffer's position.
    *
    * @param newPosition The new position value, a int, must be less than or equal to
    * the last valid position.
    */
    public void setPosition(int newPosition)
    {
        int bufferCounter = 0;
        boolean foundBuffer = false;
        
        Iterator iterator = buffers.iterator();
        while( (false == foundBuffer) && (iterator.hasNext()) )
        {
            Object[] array = (Object[]) iterator.next();
            
            if( array.length < (newPosition + 1) )
            {
                bufferCounter = bufferCounter + 1;
                newPosition = newPosition - array.length;
            }
            else
            {
                foundBuffer = true;
                pbuffer = bufferCounter;
                position = newPosition;
            }
        }
    }
    
   /**
    *  Returns this buffer's end position.
    *
    * @return The current last position of the buffer, a int
    */
    public int getEndPosition()
    {
        int absoluteEndPosition = 0;
        
        for(int count = 0; count < epbuffer; count++)
        {
            Object[] array = (Object[]) buffers.elementAt(count);
            absoluteEndPosition = absoluteEndPosition + array.length;
        }
        
        absoluteEndPosition = absoluteEndPosition + endPosition;
        
        return absoluteEndPosition;        
    }
    
   /**
    * Clears this buffer. The current and end positions are set to the
    * beginning of the buffer.
    */
    public void clear()
    {
        pbuffer = 0;
        position = 0;
        
        epbuffer = 0;
        endPosition = 0;
        
        buffers = new Vector();
    }
    
   /**
    * Truncates the buffer to the indicated position.  The current
    * position and end-position will be set to this point.
    *
    * @param truncation The position to truncate from, a int
    */
    public void truncate(int truncation)
    {
        if( 0 > truncation)
          throw new IllegalArgumentException("truncation to small");
          
        int bufferCounter = 0;
        boolean foundBuffer = false;
        
        Iterator iterator = buffers.iterator();
        while( (false == foundBuffer) && (iterator.hasNext()) )
        {
            Object[] array = (Object[]) iterator.next();
            
            if( array.length < (truncation + 1) )
            {
                bufferCounter = bufferCounter + 1;
                truncation = truncation - array.length;
            }
            else
            {
                foundBuffer = true;
                
                pbuffer = bufferCounter;
                position = truncation;
                
                epbuffer = bufferCounter;
                endPosition = truncation;
            }
        }    
    }
    
   /**
    * This method returns an instance of a Class object which
    * is the next Class type that can be packed into this Buffer
    *
    * @return A Class object which is the next Class type that
    * can be packed into this Buffer, null if no Class can be
    * packed into this Buffer.
    */
    public Class getNextPackableType()
    {
        if(false == canPack())
          throw new IndexOutOfBoundsException("Index out of bounds");
          
        Object[] array = (Object[]) buffers.elementAt(pbuffer);
        return array.getClass().getComponentType();
    }
    
   /**
    * Packs an object into the buffer at the current position.
    * The current position is placed after this item and the end
    * position is incremented accordingly.
    *
    * @param object The instance to pack, an Object
    */
    public void pack(Object object)
    { 
        if(false == canPack())
          throw new IndexOutOfBoundsException("Index out of bounds");
            
        Object[] array = (Object[]) buffers.elementAt(pbuffer);
        array[position] = object;
        
        if( epbuffer < pbuffer )
        {
            epbuffer = pbuffer;
            endPosition = position;
        } else if( (epbuffer == pbuffer) && (endPosition <= position) )
        {
            epbuffer = pbuffer;
            endPosition = position;
        }
        
        if( (position + 1) < array.length )
        {
            position = position + 1;
        }
        else
        {
            position = 0;
            pbuffer = pbuffer + 1;
        }
    }
    
   /**
    * This method returns an instance of a Class object which
    * is the next Class type that can be un-packed from this Buffer
    *
    * @return A Class object which is the next Class type that
    * can be un-packed from this Buffer, null if no Class can
    * be unpacked.
    */
    public Class getNextUnPackableType()
    {
        if(false == canUnPack())
          throw new IndexOutOfBoundsException("Index out of bounds");
          
        Object[] array = (Object[]) buffers.elementAt(pbuffer);
        return array.getClass().getComponentType();
    }
        
   /**
    * Unpacks an object from the current position in the buffer.
    * The current position is placed after this item.
    *
    * @param clazzz A Class instance specifying the type of the 
    * object to unpack.
    * @return An instance of the specified object, a Object
    */
    public Object unPack(Class clazzz)
    {
        if(false == canUnPack())
          throw new IndexOutOfBoundsException("Index out of bounds");
            
        Object[] array = (Object[]) buffers.elementAt(pbuffer);
        Object returnValue = array[position];
        
        if( false == clazzz.isInstance(array[position]) )
          throw new IllegalArgumentException("Unpacking incorrect Class type");
        
        if( (position + 1) < array.length )
        {
            position = position + 1;
        }
        else
        {
            position = 0;
            pbuffer = pbuffer + 1;
        }
        
        return returnValue;
    }
    
   /**
    * Packs an array of objects into the buffer at the current 
    * position with a stride of zero. The current position is 
    * placed after the last item and the end position is 
    * incremented accordingly.
    * 
    * @param array An array of objects to pack
    * @param number The number of items to pack, an int
    */
    public void packArray(Object[] array, int number)
    {
        for(int count = 0; count < number; count++)
          pack(array[count]);
    }
    
   /**
    * Packs an array of objects into the buffer at the current 
    * position. The current position is placed after the last 
    * item and the end position is incremented accordingly.
    *
    * @param array An array of objects to pack
    * @param number The number of items to pack, a int
    * @param stride The stride of the items to pack, a int
    */
    public void packArray(Object[] array, int number, int stride)
    {
        for(int count = 0; count < (number * stride) ; count =+ stride)
          pack(array[count]);
    }
    
   /**
    * Unpacks an array of objects from the current position in the 
    * buffer. The current position is placed after these items.
    *
    * @param clazzz A Class instance corresponding to a primitive type.
    * @return An array of elements
    */
    public Object[] unPackArray(Class clazzz)
    {
        Vector vector = new Vector();
        
        while( canUnPack() )
          vector.add( unPack(clazzz) );
        
        return (Object[]) vector.toArray();
    }
    
   /**
    *  Expands this buffer increasing the capacity by the specified 
    * number of primitive types.
    *
    * @param number The number of specified objects by which to expand
    * the current capacity, a int
    * @param clazz A Class instance corresponding to a primitive type.
    */
    public void expand(int number, Class clazz)
    {
        buffers.add( Array.newInstance(clazz, number) );
    }
}