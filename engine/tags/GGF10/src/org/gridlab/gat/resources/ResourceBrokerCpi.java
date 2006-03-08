package org.gridlab.gat.resources;

import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.util.TimePeriod;
import org.gridlab.gat.resources.hardware.HardwareResourceDescription;

/**
 * Capability provider interface to the ResourceBroker class.
 * <p>
 * Capability provider wishing to provide the functionality of 
 * the ResourceBroker class must extend this class and implement 
 * all of the abstract methods in this class. Each abstract method 
 * in this class mirrors the corresponding method in this 
 * ResourceBroker class and will be used to implement the 
 * corresponding method in the ResourceBroker class at runtime. 
 */
public abstract class ResourceBrokerCpi
{
    protected GATContext gatContext = null;
    
   /**
    * This method constructs a ResourceBrokerCpi instance corresponding to the
    * passed GATContext. 
    *
    * @param gatContext A GATContext which will be used to broker resources
    */
    public ResourceBrokerCpi(GATContext gatContext)
    {            
        this.gatContext = gatContext;
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
    public abstract Reservation reserveHardwareResource(HardwareResourceDescription hardwareResourceDescription, TimePeriod timePeriod) throws RemoteException, IOException;
    
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
    public abstract List findHardwareResources(HardwareResourceDescription hardwareResourceDescription) throws RemoteException, IOException;
}