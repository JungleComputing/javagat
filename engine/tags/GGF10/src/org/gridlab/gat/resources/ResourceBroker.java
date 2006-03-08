package org.gridlab.gat.resources;

import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.util.TimePeriod;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.hardware.HardwareResourceDescription;

/**
 * An instance of this class is used to reserve resources.
 * <p>
 * A resource can either be a hardware resource or a software resource. A
 * software resource is simply an executable it makes little sense to
 * reserve such. Thus an instance of this class can currently only reserve a
 * hardware resource.
 * <p>
 * If one wishes to reserve a hardware resource, one must first describe
 * the hardware resource that one wishes to reserve. This is accomplished
 * by creating an instance of the class HardwareResourceDescription which
 * describes the hardware resource that one wishes to reserve. After
 * creating such an instance of the class HardwareResourceDescription
 * that describes the hardware resource one wishes to reserve, one must
 * specify the time period for which one wishes to reserve the hardware
 * resource. This is accomplished by creating an instance of the class
 * TimePeriod which specifies the time period for which one wishes to
 * reserve the hardware resource. Finally, one must obtain a reservation
 * for the desired hardware resource for the desired time period. This is
 * accomplished by calling the method ReserveHardwareResource() on an
 * instance of the class ResourceBroker with the appropriate instance of
 * HardwareResourceDescription and the appropriate instance of
 * TimePeriod.
 * <p>
 * In addition an instance of this class can be used to find hardware resources.
 * This is accomplished using the method FindHardwareResources(). This is 
 * accomplished by creating an instance of the class HardwareResourceDescription 
 * which describes the hardware resource that one wishes to find. After
 * creating such an instance of the class HardwareResourceDescription
 * that describes the hardware resource one wishes to find, one must 
 * find the corresponding hardware resource. This is accomplished by calling the 
 * method FindHardwareResources() on an instance of the class ResourceBroker 
 * with the appropriate instance of HardwareResourceDescription.
 */
public class ResourceBroker
{
    protected GATContext gatContext = null;
    private ResourceBrokerCpi resourceBrokerCpi = null;
    
   /**
    * This method constructs a ResourceBroker instance corresponding to the
    * passed GATContext. 
    *
    * @param gatContext A GATContext which will be used to broker resources
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public ResourceBroker(GATContext gatContext) throws Exception
    {            
        this.gatContext = gatContext;
        
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[1];
        array[0] = gatContext;
                
        resourceBrokerCpi = (ResourceBrokerCpi) gatEngine.constructCpiClass(ResourceBrokerCpi.class, array);
    }  
    
   /**
    * This method constructs a ResourceBroker instance corresponding to the
    * passed GATContext. 
    *
    * @param gatContext A GATContext which will be used to broker resources
    * @param preferences The Preferences for this instance
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public ResourceBroker(GATContext gatContext, Preferences preferences) throws Exception
    {            
        this.gatContext = gatContext;
        
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[1];
        array[0] = gatContext;
                
        resourceBrokerCpi = (ResourceBrokerCpi) gatEngine.constructCpiClass(ResourceBrokerCpi.class, preferences, array);
    }
    
   /**
    * Tests this ResourceBroker for equality with the passed Object.
    * <p>
    * If the given object is not a ResourceBroker, then this method
    * immediately returns false.
    * <p> 
    * If the passed object is a ResourceBroker, then it is deemed 
    * equal if it has an equivalent GATContext, as determined by 
    * the Equals method on GATContext.
    *
    * @param object The Object to test for equality
    * @return A boolean indicating equality
    */
    public boolean equals(Object object)
    {
        if( false == (object instanceof ResourceBroker) )
          return false;
          
        return gatContext.equals( ((ResourceBroker) object).gatContext );
    }
    
   /**
    * This method attempts to reserve the specified hardware resource 
    * for the specified time period. Upon reserving the specified 
    * hardware resource this method returns a Reservation. Upon failing
    * to reserve the specified hardware resource this method returns 
    * an error.
    *
    * @param hardwareResourceDescription A description, a 
    * HardwareResourceDescription, of the hardware resource to reserve
    * @param timePeriod The time period, a TimePeriod , for which to 
    * reserve the hardware resource
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public Reservation reserveHardwareResource(HardwareResourceDescription hardwareResourceDescription, TimePeriod timePeriod) throws RemoteException, IOException
    {
        return resourceBrokerCpi.reserveHardwareResource(hardwareResourceDescription, timePeriod);
    }
    
   /**
    * This method attempts to find one or more matching hardware
    * resources. Upon finding the specified hardware resource(s) this method
    * returns a java.util.List of HardwareResource instances. Upon failing to find the
    * specified hardware resource this method returns an error.
    *
    * @param hardwareResourceDescription A description, a  HardwareResoucreDescription, 
    * of the hardware resource(s) to find
    * @return java.util.List of HardwareResources upon success
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public List findHardwareResources(HardwareResourceDescription hardwareResourceDescription) throws RemoteException, IOException
    {
        return resourceBrokerCpi.findHardwareResources(hardwareResourceDescription);
    }
}