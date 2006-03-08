package org.gridlab.gat.resources.software;

import java.util.Map;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the SimpleJob class.
 * <p>
 * Capability provider wishing to provide the functionality 
 * of the SimpleJob class must extend this class and implement
 * all of the abstract methods in this class. Each abstract 
 * method in this class mirrors the corresponding method in 
 * this SimpleJob class and will be used to implement the 
 * corresponding method in the SimpleJob class at runtime.
*/
public abstract class SimpleJobCpi implements Monitorable
{       
    protected GATContext gatContext = null;
    protected SoftwareResourceDescription softwareResourceDescription = null;
    
   /**
    * Constructs a SimpleJobCpi instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the simple job's executable
    */ 
    public SimpleJobCpi(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription)
    {            
        this.gatContext = gatContext;
        this.softwareResourceDescription = softwareResourceDescription;
    }
        
   /**
    * Submits the associated physical job to a job queue, an ordered list of
    * jobs which will eventually be run. Eventually the associated physical
    * job will be taken off of the job queue and begin running. This method
    * can only be called on a SimpleJobCpi in the Constructed or Stopped state or 
    * an error will occur.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void submit() throws RemoteException, IOException;
    
   /**
    * Stops the associated physical job. Upon a successful call to this
    * method the associated physical job is forcibly terminated. This method
    * can only be called on a SimpleJobCpi in the Running state.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void stop() throws RemoteException, IOException;
    
   /**
    * This method returns the state of the associated SimpleJobCpi. This is one
    * of the associated public member member variables Constructed,
    * Submitted, Running, or Stopped.
    *
    * @return This method returns the state of the associated SimpleJobCpi, one
    * of the associated member variables
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract int getState() throws RemoteException, IOException;
    
   /**
    * This method returns an instance of the class java.util.Map which contains
    * information about the associated SimpleJobCpi. This java.util.Map contains a
    * set of key/value pairs the key, a java.lang.String, being the name of the
    * information and the value being the value of the associated named
    * information.  The minimum set of keys which the returned java.util.Map
    * contains is as follows:
    * <ul>
    * <li> <em>hostname</em> </li>
    * <li> <em>submissiontime</em> </li>
    * <li> <em>starttime</em> </li>
    * <li> <em>stoptime</em> </li>
    * </ul>
    * <p>
    * <em>hostname</em> 
    * The key hostname corresponds to a java.lang.String value which is the
    * name of the host on which the physical job is running, if SimpleJobCpi is
    * in the Running state, or will be running on, if SimpleJobCpi is in the
    * Submitted state. If the associated SimpleJobCpi is not in the Running or
    * Submitted state, then the value is null.
    * <p>
    * <em>submissiontime</em>
    * The key submissiontime corresponds to a long value which is
    * the number of milliseconds after January 1, 1970, 00:00:00 GMT when
    * the associated physical job was submitted. This value is null for a
    * SimpleJobCpi in the Constructed state otherwise it is not null.
    * <p>
    * <em>starttime</em>
    * The key starttime corresponds to a long value which is the
    * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
    * associated physical job was started. This value is null for a
    * SimpleJobCpi in the Submitted or Constructed states otherwise it is not
    * null.
    * <p>
    * <em>stoptime<em>
    * The key stoptime corresponds to a long value which is the
    * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
    * associated physical job stopped. This value is not null for a
    * SimpleJobCpi in the Stopped state otherwise it is null.
    * <p>
    * Other key/value pairs will be in future added to the list of key/value
    * pairs returned in this java.util.Map as the need develops.
    *
    * @return An instance of the class java.util.Map which presents 
    * information about the associated SimpleJobCpi.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract Map getInfo() throws RemoteException, IOException;
    
   /**
    * This method returns the job id, a globally unique identifier for the physical
    * job corresponding to this instance. This method should be called on an instance
    * of this class only when the instance is in a Running or Submitted state or an
    * error will be returned.
    *
    * @return An instance of the class java.lang.String which represents the job ID
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract String getJobID() throws RemoteException, IOException;
}