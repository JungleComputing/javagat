package org.gridlab.gat.resources.software;

import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.resources.hardware.HardwareResourceDescription;

/**
 * Capability provider interface to the CheckpointableSimpleJob class.
 * <p>
 * Capability provider wishing to provide the functionality 
 * of the CheckpointableSimpleJob class must extend this class and implement
 * all of the abstract methods in this class. Each abstract 
 * method in this class mirrors the corresponding method in 
 * this CheckpointableSimpleJob class and will be used to implement the 
 * corresponding method in the CheckpointableSimpleJob class at runtime.
*/
public abstract class CheckpointableSimpleJobCpi extends SimpleJobCpi
{           
   /**
    * Constructs a CheckpointableSimpleJobCpi instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the checkpointable simple job's executable
    */ 
    public CheckpointableSimpleJobCpi(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription)
    {            
        super(gatContext, softwareResourceDescription);
    }
    
   /**
    * Checkpoints the associated physical job. A physical job is said to be
    * checkpointed when it writes its current state information to a long
    * term storage medium. A physical job when checkpointing must write its
    * current state information to a long term storage medium in such a
    * manner so that it can, at a later date, by using the state information
    * stored in the long term storage medium continue running "from the
    * same point" it was at before checkpointing. This is useful for
    * physical jobs which involve significant data processing and can not,
    * for any number of reasons, process all of this data in a single run;
    * also, it is useful for physical jobs which involve significant data
    * processing and are, for any number of reasons unstable. In both of
    * these cases checkpointing allows the physical job to process some data
    * now, then, at a later date, continue running. This method can only be
    * called on a CheckpointableSimpleJobCpi in the Running state or an error
    * will occur.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void checkpoint() throws RemoteException, IOException; 
    
   /**
    * This method is equivalent to calling Checkpoint, Stop, then Submit on
    * this instance of CheckpointableSimpleJobCpi.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void migrate() throws RemoteException, IOException;
    
   /**
    * This method is equivalent to calling Checkpoint then Stop on this
    * instance of CheckpointableSimpleJobCpi, then calling Submit on an
    * instance of CheckpointableSimpleJobCpi identical to this instance except
    * that
    * <ul>
    *     <li> The CheckpointableSimpleJobCpi when it first enters the Running
    *          state will be in the state specified by the state information
    *          stored during the previous call to the Checkpoint method. </li>
    *     <li> The CheckpointableSimpleJobCpi when it first enters the Running 
    *          state will be running on a hardware resource described in the
    *          passed HardwareResourceDescription.</li>
    * </ul>
    * 
    * @param hardwareResourceDescription A description of the hardware resource
    * to which the physical job corresponding to this CheckpointableSimpleJobCpi
    * should be migrated, a HardwareResourceDescription
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void migrate(HardwareResourceDescription hardwareResourceDescription) throws RemoteException, IOException;
    
   /**
    * This method returns the state of the associated CheckpointableSimpleJobCpi. This is one
    * of the associated public member member variables Constructed,
    * Submitted, Running, Checkpointing, or Stopped.
    *
    * @return This method returns the state of the associated CheckpointableSimpleJobCpi, one
    * of the associated member variables of CheckpointableSimpleJob
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract int getState() throws RemoteException, IOException;                      
}