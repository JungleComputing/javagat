package org.gridlab.gat.resources;

import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.net.RemoteException;

/**
 * An instance of this class represents a monitorable simple job, a job which
 * requires the start of a single executable and is monitorable.
 * <p>
 * A simple job is what one normally considers when referring to a starting a
 * program. Thus, a simple job is primarily described by the software which is
 * to execute during this simple job. The description of the software which is
 * to execute during this simple job is given by an instance of the class
 * SoftwareResourceDescription. As detailed in the description section of the
 * documentation for the class SoftwareResourceDescription, an instance of the
 * class SoftwareResourceDescription describes a software component. uses this
 * description to describe the software which is to execute during this simple
 * job.
 * <p>
 * Upon creating an instance of the class the associated physical job is not
 * immediately running. An instance of the class has various states which it can
 * be in, only one of which maps to a running physical job. In particular the
 * various state are as follows
 * <ul>
 * <li><em>Constructed</em></li>
 * <li><em>Submitted</em></li>
 * <li><em>Running</em></li>
 * <li><em>Stopped</em></li>
 * </ul>
 * A description of the various states diagrammed in figure below is as follows:
 * <center><img src="doc-files/SimpleJob.jpg" height="600" width="150">
 * </center> <em>Constructed</em> An instance of the class has been
 * constructed, but the method Submit has not yet been successfully called on
 * this instance.
 * <p>
 * <em>Submitted</em> The method Submit has been successfully called on an
 * instance of while the instance was in the Constructed state.
 * <p>
 * <em>Running</em> The physical job corresponding to an instance of a is
 * running.
 * <p>
 * <em>Stopped</em> The physical job corresponding to an instance of a was
 * running but is not currently running due to a successful call to the method
 * Stop, or the physical job corresponding to an instance of a was running but
 * is not currently running due to the physical job completing.
 * <p>
 * In addition a allows one or more Metrics to be monitored.
 */
public abstract class Job implements Monitorable, Advertisable {

	/** Constructed state indicator */
	public static final int CONSTRUCTED = 1;

	/** Submitted state indicator */
	public static final int SUBMITTED = 2;

	/** Running state indicator */
	public static final int RUNNING = 4;

	/** Stopped state indicator */
	public static final int STOPPED = 8;

	/** Checkpointing state indicator */
	public static final int CHECKPOINTING = 16;

	private GATContext gatContext;

	private Preferences preferences;

	public abstract void unSchedule();

	/**
	 * Stops the associated physical job. Upon a successful call to this method
	 * the associated physical job is forcibly terminated. This method can only
	 * be called on a in the Running state.
	 * 
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract void stop() throws GATInvocationException, RemoteException,
			IOException;

	/**
	 * This method returns the state of the associated . This is one of the
	 * associated public member member variables CONSTRUCTED, SUBMITTED,
	 * RUNNING, or STOPPED.
	 * 
	 * @return This method returns the state of the associated , one of the
	 *         associated member variables
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract int getState() throws GATInvocationException,
			RemoteException, IOException;

	/**
	 * This method returns an instance of the class java.util.Map which contains
	 * information about the associated . This java.util.Map contains a set of
	 * key/value pairs the key, a java.lang.String, being the name of the
	 * information and the value being the value of the associated named
	 * information. The minimum set of keys which the returned java.util.Map
	 * contains is as follows:
	 * <ul>
	 * <li><em>hostname</em></li>
	 * <li><em>submissiontime</em></li>
	 * <li><em>starttime</em></li>
	 * <li><em>stoptime</em></li>
	 * </ul>
	 * <p>
	 * <em>hostname</em> The key hostname corresponds to a java.lang.String
	 * value which is the name of the host on which the physical job is running,
	 * if is in the Running state, or will be running on, if is in the Submitted
	 * state. If the associated is not in the Running or Submitted state, then
	 * the value is null.
	 * <p>
	 * <em>submissiontime</em> The key submissiontime corresponds to a long
	 * value which is the number of milliseconds after January 1, 1970, 00:00:00
	 * GMT when the associated physical job was submitted. This value is null
	 * for a in the Constructed state otherwise it is not null.
	 * <p>
	 * <em>starttime</em> The key starttime corresponds to a long value which
	 * is the number of milliseconds after January 1, 1970, 00:00:00 GMT when
	 * the associated physical job was started. This value is null for a in the
	 * Submitted or Constructed states otherwise it is not null.
	 * <p>
	 * <em>stoptime<em>
	 * The key stoptime corresponds to a long value which is the
	 * number of milliseconds after January 1, 1970, 00:00:00 GMT when the
	 * associated physical job stopped. This value is not null for a
	 *  in the Stopped state otherwise it is null.
	 * <p>
	 * Other key/value pairs will be in future added to the list of key/value
	 * pairs returned in this java.util.Map as the need develops.
	 *
	 * @return An instance of the class java.util.Map which presents 
	 * information about the associated .
	 * @throws java.rmi.RemoteException Thrown upon problems 
	 * accessing the remote instance
	 * @throws java.io.IOException Upon non-remote IO problem
	 */
	public abstract Map getInfo() throws GATInvocationException,
			RemoteException, IOException;

	/**
	 * This method returns the job id, a globally unique identifier for the
	 * physical job corresponding to this instance. This method should be called
	 * on an instance of this class only when the instance is in a Running or
	 * Submitted state or an error will be returned.
	 * 
	 * @return An instance of the class java.lang.String which represents the
	 *         job ID
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract String getJobID() throws GATInvocationException,
			RemoteException, IOException;

	/**
	 * Checkpoints the associated physical job. A physical job is said to be
	 * checkpointed when it writes its current state information to a long term
	 * storage medium. A physical job when checkpointing must write its current
	 * state information to a long term storage medium in such a manner so that
	 * it can, at a later date, by using the state information stored in the
	 * long term storage medium continue running "from the same point" it was at
	 * before checkpointing. This is useful for physical jobs which involve
	 * significant data processing and can not, for any number of reasons,
	 * process all of this data in a single run; also, it is useful for physical
	 * jobs which involve significant data processing and are, for any number of
	 * reasons unstable. In both of these cases checkpointing allows the
	 * physical job to process some data now, then, at a later date, continue
	 * running. This method can only be called on a Job in the Running state or
	 * an error will occur.
	 * 
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract void checkpoint() throws GATInvocationException,
			RemoteException, IOException;

	/**
	 * This method is equivalent to calling Checkpoint, Stop, then Submit on
	 * this instance of Job.
	 * 
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract void migrate() throws GATInvocationException,
			RemoteException, IOException;

	/**
	 * This method is equivalent to calling Checkpoint then Stop on this
	 * instance of Job, then calling Submit on an instance of Job identical to
	 * this instance except that
	 * <ul>
	 * <li>The Job when it first enters the Running state will be in the state
	 * specified by the state information stored during the previous call to the
	 * Checkpoint method.</li>
	 * <li>The Job when it first enters the Running state will be running on a
	 * hardware resource described in the passed HardwareResourceDescription.
	 * </li>
	 * </ul>
	 * 
	 * @param hardwareResourceDescription
	 *            A description of the hardware resource to which the physical
	 *            job corresponding to this Job should be migrated, a
	 *            HardwareResourceDescription
	 * @throws java.rmi.RemoteException
	 *             Thrown upon problems accessing the remote instance
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public abstract void migrate(
			HardwareResourceDescription hardwareResourceDescription)
			throws GATInvocationException, RemoteException, IOException;
}