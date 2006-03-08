package org.gridlab.gat.engine;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Collection;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class make the various GAT adaptors available to GAT
 */
public class GATEngine
{
   /**
    * This member variable holds reference to the single GATEngine
    */
    private static GATEngine gatEngine = null;
    
   /**
    * This member variable holds reference to the GATEngineThread
    */ 
    private GATEngineThread gatEngineThread = null;
    
   /**
    * Constructs a default GATEngine instance
    */  
    protected GATEngine()
    {
        gatEngineThread = new GATEngineThread(this);
        gatEngineThread.start();
        
        try
        {
             synchronized(this)
             {
                 wait();
             }
        }
        catch(InterruptedException interruptedException)
        {
            interruptedException.printStackTrace();  // Ignore exception !!!
        }
    }
    
   /**
    * Singleton method to construct a GATEngine
    *
    * @return A GATEngine instance
    */ 
    public static synchronized GATEngine getGATEngine()
    {
        if(null == gatEngine)
          gatEngine = new GATEngine();
          
        return gatEngine;
    }
        
   /**
    * Returns an instance of the specified XXXCpi class consistent
    * with the passed XXXCpi class name and parameters
    *
    * @param cpiClass The Cpi Class for which to look
    * @param parameters The Parameters for the Cpi Constructor
    * @return The specified Cpi class or null if no such adaptor exists
    * @throws java.lang.Exception Upon one of many problems occuring
    */
    public Object constructCpiClass(Class cpiClass, Object[] parameters)
           throws Exception
    {
        return constructCpiClass(cpiClass, null, parameters);
    }
        
   /**
    * Returns an instance of the specified XXXCpi class consistent
    * with the passed XXXCpi class name, preferences, and parameters
    *
    * @param cpiClass The Cpi Class for which to look
    * @param preferences The Preferences used to construct the Cpi class
    * @param parameters The Parameters for the Cpi Constructor
    * @return The specified Cpi class or null if no such adaptor exists
    * @throws java.lang.Exception Upon one of many problems occuring
    */
    public Object constructCpiClass(Class cpiClass, Preferences preferences, Object[] parameters) throws Exception
    {
        Object object = null;
        Collection collection = null;
        
        Class[] clazzes = new Class[parameters.length];
        
        for(int count = 0; count < clazzes.length; count++)
          clazzes[count] = parameters[count].getClass();
        
        Map mapOne = gatEngineThread.getCpiClasses();
        Map mapTwo = (Map) mapOne.get(cpiClass);
        
        if(null == mapTwo)
          throw new NoClassDefFoundError("No class found");
          
        if(null == preferences)
          preferences = new Preferences( new Hashtable() );
        Set preferencesSet = preferences.entrySet();
        
        Set keys = mapTwo.keySet();
        Iterator iterator = keys.iterator();
        while( (null == object) && (iterator.hasNext()) )
        {
            Preferences nextPreferences = (Preferences) iterator.next();
            Set nextPreferencesSet = nextPreferences.entrySet();
            if( nextPreferencesSet.containsAll(preferencesSet) )
            {
                Class clazz = (Class) mapTwo.get(nextPreferences);
                Constructor constructor = clazz.getConstructor( clazzes );
                object = constructor.newInstance( parameters );
            }
        }

        if(null == object)
          throw new NoClassDefFoundError("No class found");
        
        return object;
    }
}