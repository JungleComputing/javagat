package org.gridlab.gat.resources.software;

import java.util.Map;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.hardware.HardwareResourceDescription;

/**
 * An instance of this class represents a checkpointable, monitorable simple
 * job, a job which requires the start of a single executable and is 
 * checkpointable and monitorable.
 * <p>
 * A checkpointable simple job is what one normally considers when referring to a
 * starting a program that is checkpointable. Thus, a simple job is primarily described by the
 * checkpointable software which is to execute during this checkpointable simple job. The description
 * of the checkpointable software which is to execute during this simple job is given by
 * an instance of the class SoftwareResourceDescription. As detailed in
 * the description section of the documentation for the class
 * SoftwareResourceDescription, an instance of the class
 * SoftwareResourceDescription describes a software component. CheckpointableSimpleJob
 * uses this description to describe the checkpointable software which is to execute
 * during this checkpointable simple job.
 * <p>
 * Upon creating an instance of the class CheckpointableSimpleJob the associated
 * physical job is not immediately running.  An instance of the class
 * CheckpointableSimpleJob has various states which it can be in, only one of which
 * maps to a running physical job. In particular the various state are as
 * follows
 * <ul>
 *   <li> <em>Constructed</em> </li>
 *   <li> <em>Submitted</em> </li>
 *   <li> <em>Running</em> </li>
 *   <li> <em>Checkpointing</em> </li>
 *   <li> <em>Stopped</em> </li>
 * </ul>
 * A description of the various states diagrammed in figure below is as follows:
 * <p>
 * <center>
 * <img src="doc-files/CheckpointableSimpleJob.jpg" height="600" width="300">
 * </center>
 * <p>
 * <em>Constructed</em> An instance of the class CheckpointableSimpleJob has been constructed, 
 * but the method Submit has not yet been successfully called on this instance.
 * <p>
 * <em>Submitted</em> The method Submit has been successfully called on an instance
 * of CheckpointableSimpleJob while the instance was in the Constructed or Stopped state.
 * <p>
 * <em>Running</em> The physical job corresponding to an instance of a CheckpointableSimpleJob
 * is running.
 * <p>
 * <em>Checkpointing</em> The physical job corresponding to an instance of a
 * CheckpointableSimpleJob is running and checkpointing due to a
 * successful call to the method Checkpoint.
 * <p>
 * <em>Stopped</em> The physical job corresponding to an instance of a CheckpointableSimpleJob was 
 * running but is not currently running due to a successful call to the method Stop, 
 * or the physical job corresponding to an instance of a CheckpointableSimpleJob was running but 
 * is not currently running due to the physical job completing.
 * <p>
 * In addition a CheckpointableSimpleJob allows one or more Metrics to be monitored.
 */
public class CheckpointableSimpleJob extends SimpleJob
{
    /** Checkpointing state indicator */
    public static final int CHECKPOINTING     = 16;
    
   /**
    * Constructs a CheckpointableSimpleJob instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the checkpointable simple job's executable
    * @throws  java.lang.Exception Thrown upon creation problems
    */ 
    public CheckpointableSimpleJob(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription) throws Exception
    {            
        super(gatContext, softwareResourceDescription);
        
        GATEngine gatEngine = GATEngine.getGATEngine();
                
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = softwareResourceDescription;
        
        simpleJobCpi = (SimpleJobCpi) gatEngine.constructCpiClass(CheckpointableSimpleJobCpi.class, array);
    }
    
   /**
    * Constructs a CheckpointableSimpleJob instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the checkpointable simple job's executable
    * @param preferences The Preferences for this instance
    * @throws  java.lang.Exception Thrown upon creation problems
    */ 
    public CheckpointableSimpleJob(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription, Preferences preferences) throws Exception
    {   
        super(gatContext, softwareResourceDescription, preferences);
        
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = softwareResourceDescription;
                
        simpleJobCpi = (SimpleJobCpi) gatEngine.constructCpiClass(CheckpointableSimpleJobCpi.class, preferences, array);
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
    * called on a CheckpointableSimpleJob in the Running state or an error
    * will occur.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void checkpoint() throws RemoteException, IOException
    {
        ( (CheckpointableSimpleJobCpi) simpleJobCpi ).checkpoint();
    }
    
   /**
    * This method is equivalent to calling Checkpoint, Stop, then Submit on
    * this instance of CheckpointableSimpleJob.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void migrate() throws RemoteException, IOException
    {
        ( (CheckpointableSimpleJobCpi) simpleJobCpi ).migrate();
    }
    
   /**
    * This method is equivalent to calling Checkpoint then Stop on this
    * instance of CheckpointableSimpleJob, then calling Submit on an
    * instance of CheckpointableSimpleJob identical to this instance except
    * that
    * <ul>
    *     <li> The CheckpointableSimpleJob when it first enters the Running
    *          state will be in the state specified by the state information
    *          stored during the previous call to the Checkpoint method. </li>
    *     <li> The CheckpointableSimpleJob when it first enters the Running 
    *          state will be running on a hardware resource described in the
    *          passed HardwareResourceDescription.</li>
    * </ul>
    * 
    * @param hardwareResourceDescription A description of the hardware resource
    * to which the physical job corresponding to this CheckpointableSimpleJob
    * should be migrated, a HardwareResourceDescription
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void migrate(HardwareResourceDescription hardwareResourceDescription) throws RemoteException, IOException
    {
        ( (CheckpointableSimpleJobCpi) simpleJobCpi ).migrate(hardwareResourceDescription);
    }
    
   /**
    * This method returns the state of the associated CheckpointableSimpleJob. This is one
    * of the associated public member member variables Constructed,
    * Submitted, Running, Checkpointing, or Stopped.
    *
    * @return This method returns the state of the associated CheckpointableSimpleJob, one
    * of the associated member variables
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public int getState() throws RemoteException, IOException
    {
        return simpleJobCpi.getState();
    }    
}