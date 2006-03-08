package org.gridlab.gat.resources.software;

import java.util.Map;
import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An instance of this class represents a monitorable simple job, a job
 * which requires the start of a single executable and is monitorable.
 * <p>
 * A simple job is what one normally considers when referring to a
 * starting a program. Thus, a simple job is primarily described by the
 * software which is to execute during this simple job. The description
 * of the software which is to execute during this simple job is given by
 * an instance of the class SoftwareResourceDescription. As detailed in
 * the description section of the documentation for the class
 * SoftwareResourceDescription, an instance of the class
 * SoftwareResourceDescription describes a software component. SimpleJob
 * uses this description to describe the software which is to execute
 * during this simple job.
 * <p>
 * Upon creating an instance of the class SimpleJob the associated
 * physical job is not immediately running.  An instance of the class
 * SimpleJob has various states which it can be in, only one of which
 * maps to a running physical job. In particular the various state are as
 * follows
 * <ul>
 *   <li> <em>Constructed</em> </li>
 *   <li> <em>Submitted</em> </li>
 *   <li> <em>Running</em> </li>
 *   <li> <em>Stopped</em> </li>
 * </ul>
 * A description of the various states diagrammed in figure below is as follows:
 * <center>
 * <img src="doc-files/SimpleJob.jpg" height="600" width="150">
 * </center>
 * <em>Constructed</em> An instance of the class SimpleJob has been constructed, 
 * but the method Submit has not yet been successfully called on this instance.
 * <p>
 * <em>Submitted</em> The method Submit has been successfully called on an instance
 * of SimpleJob while the instance was in the Constructed state.
 * <p>
 * <em>Running</em> The physical job corresponding to an instance of a SimpleJob
 * is running.
 * <p>
 * <em>Stopped</em> The physical job corresponding to an instance of a SimpleJob was 
 * running but is not currently running due to a successful call to the method Stop, 
 * or the physical job corresponding to an instance of a SimpleJob was running but 
 * is not currently running due to the physical job completing.
 * <p>
 * In addition a SimpleJob allows one or more Metrics to be monitored.
 */
public class SimpleJob implements Monitorable
{    
    /** Constructed state indicator */
    public static final int CONSTRUCTED = 1;
    
    /** Submitted state indicator */
    public static final int SUBMITTED   = 2;
    
    /** Running state indicator */
    public static final int RUNNING     = 4;
    
    /** Stopped state indicator */
    public static final int STOPPED     = 8;
    
    protected SimpleJobCpi simpleJobCpi = null;
    
   /**
    * Constructs a SimpleJob instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the simple job's executable
    * @throws  java.lang.Exception Thrown upon creation problems
    */ 
    public SimpleJob(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = softwareResourceDescription;
                
        simpleJobCpi = (SimpleJobCpi) gatEngine.constructCpiClass(SimpleJobCpi.class, array);
    }
    
   /**
    * Constructs a SimpleJob instance corresponding to the passed
    * SoftwareResourceDescription and GATContext
    * 
    * @param gatContext A GATContext used to broker resources
    * @param softwareResourceDescription A SoftwareResourceDescription
    * describing the simple job's executable
    * @param preferences The Preferences for this instance
    * @throws  java.lang.Exception Thrown upon creation problems
    */ 
    public SimpleJob(GATContext gatContext, SoftwareResourceDescription softwareResourceDescription, Preferences preferences) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = gatContext;
        array[1] = softwareResourceDescription;
                
        simpleJobCpi = (SimpleJobCpi) gatEngine.constructCpiClass(SimpleJobCpi.class, preferences, array);
    }
    
   /**
    * Submits the associated physical job to a job queue, an ordered list of
    * jobs which will eventually be run. Eventually the associated physical
    * job will be taken off of the job queue and begin running. This method
    * can only be called on a SimpleJob in the Constructed or Stopped state or 
    * an error will occur.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void submit() throws RemoteException, IOException
    {
        simpleJobCpi.submit();
    }
    
   /**
    * Stops the associated physical job. Upon a successful call to this
    * method the associated physical job is forcibly terminated. This method
    * can only be called on a SimpleJob in the Running state.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void stop() throws RemoteException, IOException
    {
        simpleJobCpi.stop();
    }
    
   /**
    * This method returns the state of the associated SimpleJob. This is one
    * of the associated public member member variables CONSTRUCTED,
    * SUBMITTED, RUNNING, or STOPPED.
    *
    * @return This method returns the state of the associated SimpleJob, one
    * of the associated member variables
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public int getState() throws RemoteException, IOException
    {
        return simpleJobCpi.getState();
    }
    
   /**
    * This method returns an instance of the class java.util.Map which contains
    * information about the associated SimpleJob. This java.util.Map contains a
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
    * name of the host on which the physical job is running, if SimpleJob is
    * in the Running state, or will be running on, if SimpleJob is in the
    * Submitted state. If the associated SimpleJob is not in the Running or
    * Submitted state, then the value is null.
    * <p>
    * <em>submissiontime</em>
    * The key submissiontime corresponds to a long value which is
    * the number of milliseconds after January 1, 1970, 00:00:00 GMT when
    * the associated physical job was submitted. This value is null for a
    * SimpleJob in the Constructed state otherwise it is not null.
    * <p>
    * <em>starttime</em>
    * The key starttime corresponds to a long value which is the
    * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
    * associated physical job was started. This value is null for a
    * SimpleJob in the Submitted or Constructed states otherwise it is not
    * null.
    * <p>
    * <em>stoptime<em>
    * The key stoptime corresponds to a long value which is the
    * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
    * associated physical job stopped. This value is not null for a
    * SimpleJob in the Stopped state otherwise it is null.
    * <p>
    * Other key/value pairs will be in future added to the list of key/value
    * pairs returned in this java.util.Map as the need develops.
    *
    * @return An instance of the class java.util.Map which presents 
    * information about the associated SimpleJob.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public Map getInfo() throws RemoteException, IOException
    {
        return simpleJobCpi.getInfo();
    }
    
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
    public String getJobID() throws RemoteException, IOException
    {
        return simpleJobCpi.getJobID();
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
        simpleJobCpi.addMetricListener(metricListener, metric);
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
        simpleJobCpi.removeMetricListener(metricListener, metric);
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
        return simpleJobCpi.getMetrics();
    }
}