package org.gridlab.gat.util;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * Capability provider interface to the Collection class.
 * <p>
 * Capability provider wishing to provide the functionality
 * of the Collection class must extend this class and implement 
 * all of the abstract methods in this class. Each abstract 
 * method in this class mirrors the corresponding method in this 
 * Collection class and will be used to implement the 
 * corresponding method in the Collection class at runtime.
 */
public abstract class CollectionCpi implements Monitorable
{   
   protected GATContext gatContext = null;
   protected Collection collection = null;
   
   /**
    * Constructs an empty CollectionCpi
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this CollectionCpi.
    */
    public CollectionCpi(GATContext gatContext)
    {
        this.gatContext = gatContext;
    }
        
   /**
    * Constructs a CollectionCpi containing the same elements as the passed
    * Collection.
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this CollectionCpi.
    * @param collection The Collection whose elements are to be placed into 
    * this CollectionCpi.
    */
    public CollectionCpi(GATContext gatContext, Collection collection)
    {            
        this.gatContext = gatContext;
        this.collection = collection;
    }
          
   /**
    * Ensures that this CollectionCpi contains the specified element.
    *
    * @param object The Object to add
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void add(Object object) throws RemoteException;
    
   /**
    * Ensures that this CollectionCpi contains the specified element with the
    * associated properties. The properties must consist of name/value pairs
    * in which the name is a java.lang.String.
    *
    * @param object The Object to add
    * @param properties The properties, a java.util.Map, to associate with
    * the passed Object
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void add(Object object, Map properties) throws RemoteException;
    
   /**
    * Ensures that this CollectionCpi contains the all elements in the
    * specified Collection.
    *
    * @param collection The Collection whose elements to add
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void addAll(Collection collection)  throws RemoteException;
    
   /**
    * Returns a java.util.List of elements which match the passed set of
    * properties. An element matches the passed properties if and only if
    * for each name in the passed java.util.Map there exists an equal name in
    * the element's properties as determined by the Equals() method and the
    * associated values are also equal as determined by the value's Equals()
    * method.
    *
    * @param properties The properties, a java.util.Map, with which to query
    * @return A java.util.List of the matching elements
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract List getElementsByProperties(Map properties) throws RemoteException;
    
   /**
    * Returns an java.util.Map which is the properties of the passed element. 
    * An element which is added to the Collection with no properties has a
    * java.util.Map of properties containing no name/value pairs.
    *
    * @param element The element, a Object, with which to query
    * @return A java.util.Map of the element's properties
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract Map getPropertiesByElement(Object element) throws RemoteException;
        
   /**
    * Removes all elements from this CollectionCpi.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void clear() throws RemoteException;
    
   /**
    * Returns true if this CollectionCpi contains the specified
    * element. Equality with the passed element and the contained element is
    * determined using the Equals method which both instances must
    * implement.
    *
    * @param element The element whose presence in this CollectionCpi is 
    * to be tested, an Object
    * @return A boolean indicating if this CollectionCpi contains the specified
    * element
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract boolean contains(Object element) throws RemoteException;
    
   /**
    * Returns true if this CollectionCpi contains all of the elements in the
    * specified Collection. Returns false otherwise.
    *
    * @param elements The Collection whose elements presence in this 
    * CollectionCpi is to be tested.
    * @return A boolean indicating if this CollectionCpi contains all the
    * specified elements
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract boolean containsAll(Collection elements) throws RemoteException;
    
   /**
    * Returns a boolean indicating if this CollectionCpi is empty.
    *
    * @return A boolean indicating if this CollectionCpi is empty
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract boolean isEmpty() throws RemoteException;
    
   /**
    * Returns an java.util.Iterator over the elements in this
    * CollectionCpi. There are no guarantees concerning the order
    * in which the elements are returned.  
    *
    * @return A java.util.Iterator over the elements in this CollectionCpi
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract Iterator iterator()  throws RemoteException;
    
   /**
    * Removes a single instance of the specified element from this
    * CollectionCpi, if it is present. The passed element must implement
    * the method Equals() as this method is used to determine which
    * element to remove.
    *
    * @param element The element, an Object, to remove from this CollectionCpi
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void remove(Object element) throws RemoteException;
    
   /**
    * Removes all this CollectionCpi's elements that are also contained in the
    * specified Collection.
    *
    * @param elements The Collection of elements to remove from this CollectionCpi
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void removeAll(Collection elements) throws RemoteException;
    
   /**
    * Retains only the elements in this CollectionCpi that are contained in the
    * specified Collection.
    *
    * @param elements The Collection of elements to retain in this CollectionCpi
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void retainAll(Collection elements) throws RemoteException;
    
   /**
    * Returns the number of elements in this CollectionCpi.
    *
    * @return The number of elements, an int, in this CollectionCpi
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract int size() throws RemoteException;
}