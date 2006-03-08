package org.gridlab.gat.util;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;
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
 * One can construct a DefaultCollectionAdaptor in one of two ways. One can use the no
 * arguments constructor, which creates an empty collection. One can also
 * use the constructor which takes a single argument of type Collection,
 * used creates a new DefaultCollectionAdaptor with the same elements as its
 * argument. In effect, the latter constructor allows the user to copy
 * any Collection.
 * <p>
 * A DefaultCollectionAdaptor instance provides various methods which allow one to add
 * elements, remove elements, and query the Collection instance. All
 * elements which are added to a DefaultCollectionAdaptor instance must implement the
 * method Equals(). This allows for one to query the DefaultCollectionAdaptor instance
 * with methods such as Contains() and to remove specific elements with
 * methods such as Remove().
 */
public class DefaultCollectionAdaptor extends CollectionCpi
{
   protected Hashtable hashtable = null;
   
   /**
    * Constructs an empty DefaultCollectionAdaptor
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this DefaultCollectionAdaptor.
    */
    public DefaultCollectionAdaptor(GATContext gatContext)
    {            
        super(gatContext);
        
        hashtable = new Hashtable();
    }
        
   /**
    * Constructs a DefaultCollectionAdaptor containing the same elements as the passed
    * Collection.
    *
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this Collection.
    * @param coll The Collection whose elements are to be placed into 
    * this Collection.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public DefaultCollectionAdaptor(GATContext gatContext, Collection coll) throws Exception
    {            
        super(gatContext,coll);
        
        hashtable = new Hashtable();
        
        Iterator iterator = coll.iterator();
        while(iterator.hasNext())
        {
            Object object = iterator.next();
            Map map = (Map) coll.getPropertiesByElement(object);
            
            add(object,map);
        }
    }
          
   /**
    * Ensures that this DefaultCollectionAdaptor contains the specified element.
    *
    * @param object The Object to add
    */
    public void add(Object object)
    {
        hashtable.put(object, new Hashtable());
    }
    
   /**
    * Ensures that this DefaultCollectionAdaptor contains the specified element with the
    * associated properties. The properties must consist of name/value pairs
    * in which the name is a java.lang.String.
    *
    * @param object The Object to add
    * @param properties The properties, a java.util.Map, to associate with
    * the passed Object
    */
    public void add(Object object, Map properties)
    {
        hashtable.put(object, properties);
    }
    
   /**
    * Ensures that this DefaultCollectionAdaptor contains the all elements in the
    * specified Collection.
    *
    * @param coll The Collection whose elements to add
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance    
    */
    public void addAll(Collection coll) throws RemoteException
    {
        Iterator iterator = coll.iterator();
        while(iterator.hasNext())
        {
            Object object = iterator.next();
            Map map = coll.getPropertiesByElement(object);
            
            add(object, map);
        }
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
    */
    public List getElementsByProperties(Map properties)
    {
        Vector elements = new Vector();
        
        Set setA = properties.entrySet();
        Set entrySet = hashtable.entrySet();
        Iterator iterator = entrySet.iterator();
        while(iterator.hasNext())
        {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            Map map = (Map) mapEntry.getValue();
            Set setB = map.entrySet();
            
            if( setB.containsAll(setA) )
                elements.add(mapEntry.getKey());
        }
        
        return elements;
    }
    
   /**
    * Returns an java.util.Map which is the properties of the passed element. 
    * An element which is added to the Collection with no properties has a
    * java.util.Map of properties containing no name/value pairs.
    *
    * @param element The element, a Object, with which to query
    * @return A java.util.Map of the element's properties
    */
    public Map getPropertiesByElement(Object element)
    {
        return (Map) hashtable.get(element);
    }
        
   /**
    * Removes all elements from this Collection.
    */
    public void clear()
    {
        hashtable.clear();
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
    */
    public boolean contains(Object element)
    {
        return hashtable.containsKey(element);
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
        Iterator iterator = elements.iterator();
        while(iterator.hasNext())
        {
            Object object = iterator.next();
            
            if(false == contains(object))
              return false;
        }
        
        return true;
    }
    
   /**
    * Returns a boolean indicating if this Collection is empty.
    *
    * @return A boolean indicating if this Collection is empty
    */
    public boolean isEmpty()
    {
        return hashtable.isEmpty();
    }
    
   /**
    * Returns an java.util.Iterator over the elements in this
    * Collection. There are no guarantees concerning the order
    * in which the elements are returned.  
    *
    * @return A java.util.Iterator over the elements in this Collection
    */
    public Iterator iterator()
    {
        Vector elements = new Vector();
        
        Enumeration enumeration = hashtable.keys();
        while(enumeration.hasMoreElements())
        {
            Object object = enumeration.nextElement();
            elements.add(object);
        }
        
        return elements.iterator();
    }
    
   /**
    * Removes a single instance of the specified element from this
    * Collection, if it is present. The passed element must implement
    * the method Equals() as this method is used to determine which
    * element to remove.
    *
    * @param element The element, an Object, to remove from this Collection
    */
    public void remove(Object element)
    {
        hashtable.remove(element);
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
        Iterator iterator = elements.iterator();
        while(iterator.hasNext())
        {
            remove(iterator.next());
        }
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
        Iterator iterator = iterator();
        while(iterator.hasNext())
        {
            Object object = iterator.next();
            
            if( false == elements.contains(object) )
                remove(object);
        }
    }
    
   /**
    * Returns the number of elements in this Collection.
    *
    * @return The number of elements, an int, in this Collection
    */
    public int size()
    {
        return hashtable.size();
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
    */
    public void addMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * Removes the passed MetricListener from the java.util.List of MetricListeners
    * which are notified of MetricEvents corresponding to the passed
    * Metric instance.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    */
    public void removeMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * This method returns a java.util.List of Metric instances. Each Metric 
    * instance in this java.util.List is a Metric which can be monitored on 
    * this instance.
    *
    * @return An java.util.List of Metric instances. Each Metric instance
    * in this java.util.List is a Metric which can be monitored on this 
    * instance.
    */
    public List getMetrics()
    {
        return new Vector();
    }
}