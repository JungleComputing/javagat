package org.gridlab.gat.engine;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.StringTokenizer;
import org.gridlab.gat.io.FileCpi;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.StreamCpi;
import org.gridlab.gat.io.FileStreamCpi;
import org.gridlab.gat.io.LogicalFileCpi;
import org.gridlab.gat.util.CollectionCpi;
import org.gridlab.gat.resources.ResourceBrokerCpi;
import org.gridlab.gat.resources.software.SimpleJobCpi;
import org.gridlab.gat.resources.software.CheckpointableSimpleJobCpi;

/**
 * An instance of this class is a Thread which periodically checks the 
 * optional directory for GAT jars and makes them available to GAT
 */
public class GATEngineThread extends Thread
{
   /**
    * This holds information on the cpiClasses
    */
    private Hashtable cpiClasses = null;
    
   /**
    * Constructing GATEngine
    */
    private GATEngine gatEngine = null;
    
   /**
    * Constructs a default instance
    */
    public GATEngineThread(GATEngine gatEngine)
    {
        setDaemon(true);
        this.gatEngine = gatEngine;
        cpiClasses = new Hashtable();
    }
    
   /**
    * This method periodically populates the Map returned from a call to
    * the method getCpiClasses().
    */
    public void run()
    {
        while(true)
        {
            synchronized(gatEngine)
            {
                // Obtain directory for optional packages
                File optionalPkgDirectory = getOptionalPkgDirectory();
                
                // Obtain File's in the optional directory that are jar's
                List filesJars = getFileJars(optionalPkgDirectory);
                
                // Obtain JarFile's in the optional directory that are GAT jar's
                List jarFiles = getJarFiles(filesJars);
                
                // Populate cpiClasses
                populateCpiClasses(jarFiles);
                
                // Notify
                gatEngine.notify();
                
                // Wait
                pause();
            }
        }
    }
    
   /**
    * Gets the optional packages directories
    */
    protected File getOptionalPkgDirectory()
    {
        String standardExtensionsDirectory = System.getProperty("java.home") + File.separator + "lib" + File.separator + "ext";
        return new File( standardExtensionsDirectory );
    }
    
   /**
    *  Obtains File's in the optional directory that are jar's
    */
    protected List getFileJars(File optionalPkgDirectory)
    {
        Vector vector = new Vector();
        File[] files = optionalPkgDirectory.listFiles();
        
        for(int count = 0; count < files.length; count++)
          vector.add(files[count]);
          
        return vector;
    }
    
   /**
    *  Obtains JarFile's in the optional directory that are GAT jar's
    */
    protected List getJarFiles(List files)
    {
        File nextFile = null;
        JarFile jarFile = null;
        Manifest manifest = null;
        Attributes attributes = null; 
           
        Iterator iterator = files.iterator();
        
        Vector jarFiles = new Vector();
        
        while(iterator.hasNext())
        {
            nextFile = (File) iterator.next();

            if( nextFile.isFile() )
            {
                try
                {
                    jarFile = new JarFile(nextFile, true);
                    manifest = jarFile.getManifest();
                    if(null != manifest)
                    {
                        attributes = manifest.getMainAttributes();

                        // If a GATAdaptor add to jarFiles
                        if( "true".equals(attributes.getValue("GATAdaptor")) )
                        {
                             jarFiles.add(jarFile);
                        }
                    }
                }
                catch(IOException ioException)
                {
                    ioException.printStackTrace();  // Ignore IOException !!!
                }
            }
        }
        
        return jarFiles;
    }
    
   /**
    *  Populate cpiClasses
    */
    protected void populateCpiClasses(List jarFiles)
    {
        JarFile jarFile = null;
        Manifest manifest = null;
        Attributes attributes = null;
        
        Iterator iterator = jarFiles.iterator();
        
        // Iterate over JarFiles
        while(iterator.hasNext())
        {
            Class clazz = null;             // Class of class extending XXXCpi class
            Class cpiClazz = null;          // Class of XXXCpi class   
            String clazzString = null;      // Name of class extending XXXCpi class
            Preferences properties = null;  // Preferences of adaptor
            
            jarFile = (JarFile) iterator.next();
            
            // Get info for all adaptors
            try
            {
                manifest = jarFile.getManifest();
                attributes = manifest.getMainAttributes();

                // Get info for FileCpi adaptor
                if( null != (clazzString = attributes.getValue("FileCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = FileCpi.class;
                    properties = new Preferences( attributes );
                }
                
                // Get info for LogicalFileCpi adaptor
                if( null != (clazzString = attributes.getValue("LogicalFileCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = LogicalFileCpi.class;
                    properties = new Preferences( attributes );
                }              
                
                // Get info for SimpleJobCpi adaptor
                if( null != (clazzString = attributes.getValue("SimpleJobCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = SimpleJobCpi.class;
                    properties = new Preferences( attributes );
                }        
                
                // Get info for CheckpointableSimpleJobCpi adaptor
                if( null != (clazzString = attributes.getValue("CheckpointableSimpleJobCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = CheckpointableSimpleJobCpi.class;
                    properties = new Preferences( attributes );
                }             
                
                // Get info for StreamCpi adaptor
                if( null != (clazzString = attributes.getValue("StreamCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = StreamCpi.class;
                    properties = new Preferences( attributes );
                }  
                
                // Get info for FileStreamCpi adaptor
                if( null != (clazzString = attributes.getValue("FileStreamCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = FileStreamCpi.class;
                    properties = new Preferences( attributes );
                }          
                
                // Get info for ResourceBrokerCpi adaptor
                if( null != (clazzString = attributes.getValue("ResourceBrokerCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = ResourceBrokerCpi.class;
                    properties = new Preferences( attributes );
                }     
                
                // Get info for CollectionCpi adaptor
                if( null != (clazzString = attributes.getValue("CollectionCpi-class")) )
                {
                    clazz = Class.forName(clazzString);
                    cpiClazz = CollectionCpi.class;
                    properties = new Preferences( attributes );
                } 
            }
            catch(Exception exception)
            {
                exception.printStackTrace();  // Simply try next JarFile !!!
            }
            
            // Upon successfully finding clazz, add the clazz and properties to cpiClasses
            if(null != clazz)
            {
                Map map = null;
                
                // Get/Create Map mapping XXXCpi.class to a Map
                if( null == (map = (Map) cpiClasses.get(cpiClazz)) )
                {
                    map = new Hashtable();
                    cpiClasses.put(cpiClazz, map);
                }
                
                // Map/Re-Map properties to Class of class extending XXXCpi class
                if( (null == map.get(properties)) || (false == clazz.equals(map.get(properties))) )
                  map.put(properties, clazz);
            }
        }
    }
    
   /**
    * Waits for a maximum of one second
    */
    protected void pause()
    {
            try
            {
                sleep(1000);
            }
            catch(InterruptedException interruptedException)
            {
                interruptedException.printStackTrace();  // Ignore InterruptedException !!!
            }    
    } 
    
   /**
    * This method returns a Map the keys of which are of type java.lang.String 
    * and contain the fully qualified path name of the various Cpi classes.
    * <p>
    * So, for example, a key would be the String
    * <p>
    * "org.gridlab.gat.io.FileCpi"
    * <p>
    * The values of the returned Map are Map's. Such a Map will contain as its 
    * keys instances of the class Preferences and as values instances of the 
    * class Class.
    * <p>
    * So, for example, a Map which is mapped to the key
    * <p>
    * "org.gridlab.gat.io.FileCpi"
    * <p>
    * is a second Map. This Map contains keys which are instances of the class
    * Preferences and values which are instances of the class Class. Each
    * key, a Preferences instance, will be constructed by passing an instance
    * of the Attributes class to the Preferences constructor. This Attributes
    * instance will correspond to a jar file in the optional packages
    * directory which is used to construct a Cpi clas of the type
    * <p>
    * "org.gridlab.gat.io.FileCpi"
    * <p>
    * The value, a instance of type Class, corresponding to the key, an
    * instance of type Preferences, is a Class instance corresponding to
    * the Cpi implemented in the corresponding optional jar.
    *
    * @return A Map of CpiClasses
    */
    public Map getCpiClasses()
    {
        return cpiClasses;
    }
}