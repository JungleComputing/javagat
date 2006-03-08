package org.gridlab.gat.util;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An instance of this class represents a collection, a group of objects
 * which are known as elements.
 * <p>
 * One can construct a Collection in one of two ways. One can use the no
 * arguments constructor, which creates an empty collection. One can also
 * use the constructor which takes a single argument of type Collection,
 * used creates a new Collection with the same elements as its
 * argument. In effect, the latter constructor allows the user to copy
 * any Collection.
 * <p>
 * A Collection instance provides various methods which allow one to add
 * elements, remove elements, and query the Collection instance. All
 * elements which are added to a Collection instance must implement the
 * method Equals(). This allows for one to query the Collection instance
 * with methods such as Contains() and to remove specific elements with
 * methods such as Remove().
 */
public class Collection implements Monitorable
{
   private CollectionCpi collectionCpi = null;
   
  /**
   * The root of the Collection hierarchy
   */
   public static Collection RootCollection = null;
   
   static
   {
       try
       {
           RootCollection = new Collection( new GATContext() );
       }
       catch(Exception exception)
       {
           /*
            * From the Java Language Specification v2.0
            *
            * 8.7 Static Initializers
            *
            * ...It is a compile-time error for a static initializer to be
            * able to complete abruptly (¤14.1, ¤15.6) with a checked ex-
            * ception (¤11.2). It is a compile-time error if a static ini-
            * tializer cannot complete normally (¤14.20)...
            *
            */
       }
   }
   
   /**
    * Constructs an empty Collection
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this Collection.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public Collection(GATContext gatContext) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
                
        Object[] array = new Object[1];
        array[0] = gatContext;
        
        collectionCpi = (CollectionCpi) gatEngine.constructCpiClass(CollectionCpi.class, array);
    }
    
   /**
    * Constructs an empty Collection
    *
    * @param preferences A Preferences instance which is used to specify user preferences
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this Collection.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public Collection(Preferences preferences, GATContext gatContext) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
                
        Object[] array = new Object[1];
        array[0] = gatContext;
        
        collectionCpi = (CollectionCpi) gatEngine.constructCpiClass(CollectionCpi.class, preferences, array);
    } 
    
   /**
    * Constructs a Collection containing the same elements as the passed
    * Collection.
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this Collection.
    * @param collection The Collection whose elements are to be placed into 
    * this Collection.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public Collection(GATContext gatContext, Collection collection) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = collection;
                
        collectionCpi = (CollectionCpi) gatEngine.constructCpiClass(CollectionCpi.class, array);
    }
    
   /**
    * Constructs a Collection containing the same elements as the passed
    * Collection.
    *
    * @param preferences A Preferences instance which is used to specify user preferences
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this Collection.
    * @param collection The Collection whose elements are to be placed into 
    * this Collection.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public Collection(Preferences preferences, GATContext gatContext, Collection collection) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = collection;
                
        collectionCpi = (CollectionCpi) gatEngine.constructCpiClass(CollectionCpi.class, preferences, array);
    }
      
   /**
    * Ensures that this Collection contains the specified element.
    *
    * @param object The Object to add
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void add(Object object) throws RemoteException
    {
        collectionCpi.add(object);
    }
    
   /**
    * Ensures that this Collection contains the specified element with the
    * associated properties. The properties must consist of name/value pairs
    * in which the name is a java.lang.String.
    *
    * @param object The Object to add
    * @param properties The properties, a java.util.Map, to associate with
    * the passed Object
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void add(Object object, Map properties) throws RemoteException
    {
        collectionCpi.add(object, properties);
    }
    
   /**
    * Ensures that this Collection contains the all elements in the
    * specified Collection.
    *
    * @param collection The Collection whose elements to add
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void addAll(Collection collection)  throws RemoteException
    {
        collectionCpi.addAll(collection);
    }
    
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
    public List getElementsByProperties(Map properties) throws RemoteException
    {
        return collectionCpi.getElementsByProperties(properties);
    }
    
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
    public Map getPropertiesByElement(Object element) throws RemoteException
    {
        return collectionCpi.getPropertiesByElement(element);
    }
    
   /**
    * Removes all elements from this Collection.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void clear() throws RemoteException
    {
        collectionCpi.clear();
    }
    
   /**
    * Returns true if this Collection contains the specified
    * element. Equality with the passed element and the contained element is
    * determined using the Equals method which both instances must
    * implement.
    *
    * @param element The element whose presence in this Collection is 
    * to be tested, an Object
    * @return A boolean indicating if this Collection contains the specified
    * element
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public boolean contains(Object element) throws RemoteException
    {
        return collectionCpi.contains(element);
    }
    
   /**
    * Returns true if this Collection contains all of the elements in the
    * specified Collection. Returns false otherwise.
    *
    * @param elements The Collection whose elements presence in this 
    * Collection is to be tested.
    * @return A boolean indicating if this Collection contains all the
    * specified elements
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public boolean containsAll(Collection elements) throws RemoteException
    {
        return collectionCpi.containsAll(elements);
    }
    
   /**
    * Returns a boolean indicating if this Collection is empty.
    *
    * @return A boolean indicating if this Collection is empty
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public boolean isEmpty() throws RemoteException
    {
        return collectionCpi.isEmpty();
    }
    
   /**
    * Returns an java.util.Iterator over the elements in this
    * Collection. There are no guarantees concerning the order
    * in which the elements are returned.  
    *
    * @return A java.util.Iterator over the elements in this Collection
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public Iterator iterator()  throws RemoteException
    {
        return collectionCpi.iterator();
    }
    
   /**
    * Removes a single instance of the specified element from this
    * Collection, if it is present. The passed element must implement
    * the method Equals() as this method is used to determine which
    * element to remove.
    *
    * @param element The element, an Object, to remove from this Collection
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void remove(Object element) throws RemoteException
    {
        collectionCpi.remove(element);
    }
    
   /**
    * Removes all this Collections elements that are also contained in the
    * specified Collection.
    *
    * @param elements The Collection of elements to remove from this Collection
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void removeAll(Collection elements) throws RemoteException
    {
        collectionCpi.removeAll(elements);
    }
    
   /**
    * Retains only the elements in this Collection that are contained in the
    * specified Collection.
    *
    * @param elements The Collection of elements to retain in this Collection
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void retainAll(Collection elements) throws RemoteException
    {
        collectionCpi.retainAll(elements);
    }
    
   /**
    * Returns the number of elements in this Collection.
    *
    * @return The number of elements, an int, in this Collection
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public int size() throws RemoteException
    {
        return collectionCpi.size();
    }

   /**
    * This method adds the passed instance of a MetricListener to the java.util.List
    * of MetricListeners which are notified of MetricEvents by an
    * instance of this class. The passed MetricListener is only notified of
    * MetricEvents which correspond to Metric instance passed to this
    * method.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void addMetricListener(MetricListener metricListener, Metric metric) throws RemoteException
    {
        collectionCpi.addMetricListener(metricListener, metric);
    }
    
   /**
    * Removes the passed MetricListener from the java.util.List of MetricListeners
    * which are notified of MetricEvents corresponding to the passed
    * Metric instance.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void removeMetricListener(MetricListener metricListener, Metric metric) throws RemoteException
    {
        collectionCpi.removeMetricListener(metricListener, metric);
    }
    
   /**
    * This method returns a java.util.List of Metric instances. Each Metric 
    * instance in this java.util.List is a Metric which can be monitored on 
    * this instance.
    *
    * @return An java.util.List of Metric instances. Each Metric instance
    * in this java.util.List is a Metric which can be monitored on this 
    * instance.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public List getMetrics() throws RemoteException
    {
        return collectionCpi.getMetrics();
    }
}