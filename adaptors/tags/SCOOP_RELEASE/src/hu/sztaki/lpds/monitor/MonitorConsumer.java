package hu.sztaki.lpds.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Represents a connection (channel) to a producer.
 *
 * A <code>MonitorConsumer</code> can be used to send commands to the
 * producer and receive metric values.
 *
 * @author Gábor Gombás
 * @version $Id: MonitorConsumer.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class MonitorConsumer extends Thread {

	public static final int STATUS_OK = 0;
	public static final int STATUS_GENERIC_ERR = 1;
	public static final int STATUS_UNKNOWN_CMD = 2;
	public static final int STATUS_UNKNOWN_METRIC = 3;
	public static final int STATUS_UNKNOWN_CHANNEL = 4;
	public static final int STATUS_BADPARAM = 5;
	public static final int STATUS_PARAM_TYPE = 6;
	public static final int STATUS_PARAM_MULTIPLE = 7;
	public static final int STATUS_PARAM_UNKNOWN = 8;
	public static final int STATUS_PARAM_MISSING = 9;
	public static final int STATUS_AUTH_NEEDED = 10;
	public static final int STATUS_AUTH_ERR = 11;
	public static final int STATUS_ACCESS_DENIED = 12;
	public static final int STATUS_RES_LIMIT = 13;
	public static final int STATUS_METRIC_KILLED = 14;
	public static final int STATUS_UNKNOWN_CONTROL = 15;
	public static final int STATUS_ABORTED = 16;

	/** Used by the native code for mapping to handleinfo. */
	private int monHandle = -1;

	/** The producer-assigned identifier of this channel. It gets set
	 * after the auth() command completes. */
	private int channelId;

	/** List of registered metric listeners. */
	private ArrayList metricListeners;

	/** Collection of the pending commands. */
	private TreeMap pendingCommands;

	/** This is the buffer used for decoding the metric values. */
	private Buffer buf;

	/** Collection of cached metric definitions. */
	private TreeMap defCache;

	/** Holds the exception that caused the listener thread to terminate. */
	private Exception pendingException;

	/* ============================================================
	 * Class initialization
	 */

	static {
		/* Load the native code */
		System.loadLibrary("mercury_consumer_java-" +
			Version.getVersion());

		if (!Version.getVersion().equals(Version.getNativeVersion()))
			throw new RuntimeException("Mercury version " +
				"mismatch: Java side is " +
				Version.getVersion() + ", JNI side is " +
				Version.getNativeVersion());
	}

	/* ============================================================
	 * Subclass: MetricInstance
	 */

	/**
	 * A <code>MetricInstance</code> represents a metric instance.
	 *
	 * This class makes using the GET and STOP commands easier.
	 */
	public final class MetricInstance {

		/** The numeric ID of the metric instance. */
		private int metricId;

		/**
		 * Creates a new metric instance.
		 *
		 * @param metricId	the numeric metric ID.
		 */
		protected MetricInstance(int metricId) {
			this.metricId = metricId;
		}

		/**
		 * Returns the numeric metric ID.
		 *
		 * @return	the metric ID.
		 */
		public int getMetricId() {
			return metricId;
		}

		/**
		 * Performs a <tt>SUBSCRIBE</tt> command on the metric instance.
		 *
		 * @see MonitorConsumer#subscribe(int, int)
		 */
		public CommandResult subscribe() {
			return MonitorConsumer.this.subscribe(metricId);
		}

		/**
		 * Performs a <tt>BUFFER</tt> command on the metric instance.
		 *
		 * @see MonitorConsumer#buffer(int, int)
		 */
		public CommandResult buffer() {
			return MonitorConsumer.this.buffer(metricId);
		}

		/**
		 * Performs a <tt>GET</tt> command on the metric instance.
		 *
		 * @see MonitorConsumer#get(int, int)
		 */
		public CommandResult get() {
			return MonitorConsumer.this.get(metricId);
		}

		/**
		 * Performs a <tt>STOP</tt> command on the metric instance.
		 *
		 * @see MonitorConsumer#stop(int, int)
		 */
		public CommandResult stop() {
			return MonitorConsumer.this.stop(metricId);
		}
	}

	/* ============================================================
	 * Subclass: CommandResult
	 */

	/**
	 * Represents a response to a command sent to the producer.
	 */
	public class CommandResult {

		/** If <code>false</code>, the command has not completed yet. */
		boolean finished = false;
		/** The sequence number of the command. */
		private int seq;
		/** The status returned by the producer. */
		int status;
		/** Contains the optional ID returned by the producer in the
		 * command response. */
		int id;

		/**
		 * Constructs a new <code>CommandResult</code>.
		 *
		 * @param seq		the command's sequence number.
		 */
		CommandResult(int seq) {
			this.seq = seq;
		}

		/**
		 * Updates the result's status and wakes up any waiters.
		 *
		 * This method is invoked by the native code when the command
		 * completes.
		 *
		 * @param status	the command's result status.
		 * @param id		the optional ID returned by the command.
		 */
		synchronized void finish(int status, int id) {
			this.status = status;
			this.id = id;
			finished = true;
			notifyAll();
		}

		/**
		 * Tests whether the command's result status has already arrived.
		 *
		 * @return		<code>true</code> if the command has
		 *			finished.
		 */
		public synchronized boolean isFinished() {
			return finished;
		}

		/**
		 * Returns the sequence number of the command.
		 *
		 * @return		the sequence number of the command.
		 */
		public int getSeq() {
			return seq;
		}

		/**
		 * Returns the status of the command.
		 *
		 * The returned value is valid only if isFinished() returned
		 * <code>true</code> or {@link #waitResult} completed
		 * successfully.
		 *
		 * @return		the status of the command.
		 */
		public int getStatus() {
			return status;
		}

		/**
		 * Tests whether the command has failed.
		 *
		 * The result is valid only if isFinished() returned
		 * <code>true</code> or {@link #waitResult} completed
		 * successfully.
		 *
		 * @return		<code>true</code> if the command has
		 *			failed, <code>false</code> if it has
		 *			succeeded.
		 */
		public boolean hasFailed() {
			return status != 0;
		}

		/**
		 * Returns the status of the command in text form.
		 *
		 * The returned value is valid only if isFinished() returned
		 * <code>true</code> or {@link #waitResult} completed
		 * successfully.
		 *
		 * @return		the status as a string.
		 */
		public native String getStatusStr();

		/**
		 * Returns the id returned by the command.
		 *
		 * The returned value is valid only if isFinished() returned
		 * <code>true</code> or {@link #waitResult} completed
		 * successfully. If the command has not returned any ID, this
		 * method returns 0.
		 *
		 * @return		the ID returned by the command.
		 */
		public int getResultId() {
			return id;
		}

		/**
		 * Waits for the completion of the command.
		 *
		 * @param timeout	the maximum time to wait in
		 *			milliseconds.
		 *
		 * @throws InterruptedException if another thread has
		 *	interrupted the current thread.
		 */
		public synchronized void waitResult(long timeout)
				throws InterruptedException {
			if (finished)
				return;
			wait(timeout);
			MonitorConsumer.this.checkPendingException();
		}

		/**
		 * Waits for the completion of the command.
		 *
		 * This method may block infinitely.
		 *
		 * @throws InterruptedException if another thread has
		 *	interrupted the current thread.
		 */
		public void waitResult() throws InterruptedException {
			waitResult(0);
		}

	}

	/* ============================================================
	 * Subclass: CollectResult
	 */

	/**
	 * Represents a response to a <tt>COLLECT</tt> command sent to the
	 * producer.
	 */
	public class CollectResult extends CommandResult {

		/** Used for caching the result of getMetricInstance() */
		private MetricInstance instance;

		/**
		 * Constructs a new <code>CollectResult</code>.
		 *
		 * @param seq		the command's sequence number.
		 */
		CollectResult(int seq) {
			super(seq);
		}

		/**
		 * Returns a <code>MetricInstance</code> object for the
		 * identifier returned by the COLLECT command.
		 *
		 * @return		a metric instance object.
		 */
		public synchronized MetricInstance getMetricInstance() {
			if (instance != null)
				return instance;

			if (!finished)
				return null;
			if (status != 0)
				return null;

			instance = new MetricInstance(id);
			return instance;
		}
	}

	/* ============================================================
	 * Constructors
	 */

	/**
	 * Creates a new monitor channel to a producer.
	 *
	 * This method opens a network connection to the specified producer.
	 * A new thread is created automatically to process the messages
	 * (such as responses for commands or metric values) received from the
	 * producer.
	 *
	 * @param url		the URL of the producer.
	 *
	 * @throws MonitorConnectionException if the connection attempt failed.
	 */
	public MonitorConsumer(String url) {
		super("Monitor Consumer <uninitialized>");

		metricListeners = new ArrayList();
		pendingCommands = new TreeMap();
		defCache = new TreeMap();
		connect(url);
		setName("Monitor Consumer #" + Integer.toString(monHandle) +
			": " + url);
		setDaemon(true);
		start();
	}

	/**
	 * Creates a new monitor channel to a producer.
	 *
	 * @param group		the message parsing thread will be part of this
	 *			<code>ThreadGroup</code>.
	 * @param url		the URL of the producer.
	 *
	 * @see #MonitorConsumer(String)
	 */
	public MonitorConsumer(ThreadGroup group, String url) {
		super(group, "Monitor Consumer <uninitialized>");

		metricListeners = new ArrayList();
		pendingCommands = new TreeMap();
		defCache = new TreeMap();
		connect(url);
		setName("Monitor Consumer #" + Integer.toString(monHandle) +
			": " + url);
		setDaemon(true);
		start();
	}

	/* ============================================================
	 * Metric listener handling
	 */

	/**
	 * Registers a new metric listener.
	 *
	 * The {@link MetricListener#processMetric processMetric} method of the
	 * registered listener will be invoked whenever a new metric value is
	 * received. If there are multiple registered metric listeners, their
	 * {@link MetricListener#processMetric processMetric} methods will be
	 * invoked with the received value in the order of registration until
	 * one of them returns <code>true</code>.
	 *
	 * The registered listeners will also receive any errors that cause the
	 * message parsing thread to terminate (such as when the connection is
	 * broken or a {@link MetricListener#processMetric processMetric}
	 * method raises an exception).
	 *
	 * @param listener	the listener to be notified when a metric value
	 *			is received or the connection is broken.
	 */
	public void addMetricListener(MetricListener listener) {
		metricListeners.add(listener);
	}

	/**
	 * Processes a metric value.
	 *
	 * This method is called from the native code and is responsible for
	 * notifying the registered metric listener(s).
	 *
	 * @param mid		the ID of the received metic value.
	 * @param timestamp	the timestamp of the metric value.
	 * @param value		the encoded metric value.
	 */
	private void metricCallback(int mid, long timestamp, byte[] value) {
		MetricDefinition def;
		try {
			def = getMetricDefinition(mid);
		} catch (Exception e) {
			return;
		}

		/* Do not create a new buffer every time */
		if (buf == null)
			buf = new Buffer(value);
		else
			buf.setData(value);
		MetricValue mv = new MetricValue(this, mid,
			buf.decode(def.getType()), new Date(timestamp), def);

		Iterator i = metricListeners.iterator();
		while (i.hasNext()) {
			MetricListener l = (MetricListener)i.next();
			if (l.processMetric(mv))
				break;
		}
	}

	/* ============================================================
	 * Command listener handling
	 */

	/**
	 * Adds a new <code>CommandResult</code> to the list of pending commands.
	 *
	 * This method is invoked by the native code when it has sent a
	 * command successfully.
	 *
	 * @param result	the result object to notify when the command
	 *			completes.
	 */
	private void addCommandResult(CommandResult result) {
		pendingCommands.put(new Integer(result.getSeq()), result);
	}

	/**
	 * Processes a command status response.
	 *
	 * This method is called from the native code and is responsible
	 * for notifying the appropriate CommandResult object about the
	 * completion of the command.
	 *
	 * @param commandId	the command's sequence number.
	 * @param status	the command's result.
	 * @param dataId	the (optional) ID returned by the command.
	 */
	private void commandCallback(int commandId, int status, int dataId) {
		Integer id = new Integer(commandId);
		CommandResult res = (CommandResult)pendingCommands.get(id);
		if (res != null) {
			pendingCommands.remove(id);
			res.finish(status, dataId);
		}
	}

	/* ============================================================
	 * Connection management
	 */

	/**
	 * Wakes up all <code>CommandResult</code> objects currently being
	 * blocked in {@link CommandResult#waitResult waitResult}.
	 */
	private void wakeupAll() {
		Iterator i = pendingCommands.values().iterator();
		while (i.hasNext()) {
			Object cmd = i.next();
			synchronized (cmd) {
				cmd.notifyAll();
			}
		}
	}

	/**
	 * Re-throws an exception that caused the message parsing thread to die.
	 */
	private void checkPendingException() {
		if (pendingException == null)
			return;

		/* Do not create a new exception if the pending one is already
		 * a runtime exception */
		if (pendingException instanceof RuntimeException)
			throw (RuntimeException)pendingException;
		else
			throw new MonitorConnectionException(pendingException);
	}

	/**
	 * Starts the thread that listens for messages from the producer.
	 *
	 * The thread runs until the connection is closed or an exception
	 * occurs. Such an exception can be generated by the native code
	 * when the connection is broken or by one of the registered
	 * {@link MetricListener metric listeners}.
	 */
	public void run() {
		try {
			/* parseMessages() returns from time to time to
			 * allow the Java VM to clean up any temporal objects
			 * that might be left behind by the native code */
			while (parseMessages())
				/* Nothing */;
		}
		catch (Exception e) {
			synchronized (this) {
				pendingException = e;
				wakeupAll();
			}

			/* If there are no metric listeners, just let the
			 * thread die with the received exception */
			if (metricListeners.size() == 0)
				checkPendingException();

			/* Propagate the error to all registered listeners */
			Iterator i = metricListeners.iterator();
			while (i.hasNext()) {
				MetricListener l = (MetricListener)i.next();
				l.processError(this, e);
			}
		}
	}

	/**
	 * Closes the connection to the producer.
	 *
	 * This method closes the network connection and instructs the
	 * message parsing thread to terminate.
	 *
	 * @throws MonitorConnectionException if the connection is not open.
	 */
	public synchronized void close() {
		checkPendingException();
		nativeClose();
	}

	/**
	 * Shuts down the monitoring thread.
	 */
	protected void finalize() {
		nativeClose();
	}

	/**
	 * Returns the ID of this channel to the producer.
	 *
	 * The producer assigns an ID to every connection after a successful
	 * authentication. Therefore the result of this method is valid only
	 * after authentication has been completed successfully.
	 *
	 * @return		the channel ID.
	 */
	public int getChannelId() {
		return channelId;
	}

	/* ============================================================
	 * Metric definition management
	 */

	/**
	 * Retrieves the definition for a metric ID.
	 *
	 * Received metric values are identified by a numeric ID. This method
	 * retrieves the metric definition for such an ID.
	 *
	 * The return value of controls are normal metric values. Therefore
	 * this method can also be used for retrieving the definition of the
	 * control that was executed.
	 *
	 * @param metricId	the metric ID.
	 * @return		the definition of the metric.
	 *
	 * @throws MonitorConnectionException if the connection is not open.
	 * @throws UnknownMetricException if the metric ID is invalid.
	 */
	public MetricDefinition getMetricDefinition(int metricId)
			throws UnknownMetricException {
		/* First try to get the definition from our cache */
		Integer key = new Integer(metricId);
		MetricDefinition result = (MetricDefinition)defCache.get(key);
		if (result != null)
			return result;

		/* If not in the cache, go down to the native code */
		result = nativeGetMetricDefinition(metricId);

		/* Store the result in the cache */
		defCache.put(key, result);
		return result;
	}

	/* ============================================================
	 * Native methods
	 */

	/**
	 * Connects to the producer.
	 *
	 * @param url		the URL of the producer.
	 *
	 * @throws MonitorConnectionException if the connection attempt failed.
	 */
	private synchronized native void connect(String url);

	/**
	 * Closes the connection and tells the message parsing thread to
	 * terminate.
	 */
	private native void nativeClose();

	/**
	 * Reads and processes messages sent by the producer.
	 */
	private native boolean parseMessages();

	/**
	 * Retrieves the metric definition for a numeric metric ID.
	 *
	 * @param metricId	the metric ID.
	 * @return		the definition of the metric.
	 *
	 * @throws UnknownMetricException if the specified metric ID is invalid.
	 */
	private synchronized native MetricDefinition nativeGetMetricDefinition(int metricId)
			throws UnknownMetricException;

	/**
	 * Sends an AUTH command.
	 *
	 * @param method	the authentication method to use. If
	 *			<code>null</code> is passed, the native code
	 *			will select the strongest available method.
	 */
	private native CommandResult nativeAuth(String method);

	/**
	 * Sends a COLLECT command.
	 *
	 * @param name		then name of the metric to collect.
	 * @param args		the metric arguments.
	 */
	private native CollectResult nativeCollect(String name, MonitorArg[] args);

	/**
	 * Sends a STOP command.
	 */
	private native CommandResult nativeStop(int mid, int channel);

	/**
	 * Sends a GET command.
	 */
	private native CommandResult nativeGet(int mid, int channel);

	/**
	 * Sends a SUBSCRIBE command.
	 */
	private native CommandResult nativeSubscribe(int mid, int channel);

	/**
	 * Sends a BUFFER command.
	 */
	private native CommandResult nativeBuffer(int mid, int channel);

	/**
	 * Sends a QUERY command.
	 */
	private native CommandResult nativeQuery(String name, MonitorArg[] args);

	/**
	 * Sends an EXECUTE command.
	 */
	private native CommandResult nativeExecute(String name, MonitorArg[] args);

	/**
	 * Sends a WRAP command.
	 */
	private native CommandResult nativeWrap(String name);

	/* ============================================================
	 * Java interface for the native commands
	 */

	/**
	 * Performs authentication using an automatically selected mechanism.
	 *
	 * This method will select the strongest authentication mechanism that
	 * is supported by both the consumer and the producer.
	 *
	 * @return		a command status object.
	 *
	 * @see #auth(String)
	 */
	public CommandResult auth() {
		return auth(null);
	}

	/**
	 * Performs anonymous authentication.
	 *
	 * @return		a command status object.
	 * @deprecated		use the {@link #auth()} method instead.
	 */
	public CommandResult authAnonymous() {
		return auth("anonymous");
	}

	/**
	 * Performs authentication using the specified mechanism.
	 *
	 * @param mech		the authentication mechanism to use.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	the authentication fails.
	 */
	public synchronized CommandResult auth(String mech) {
		checkPendingException();
		return nativeAuth(mech);
	}

	/**
	 * Sends a <tt>COLLECT</tt> command to the producer.
	 *
	 * The <tt>COLLECT</tt> command creates a new metric instance in the
	 * producer. This command in itself does not cause any metric values to
	 * be sent however.
	 *
	 * @param name		the name of the metric to collect.
	 * @param args		the arguments of the metric.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	sending the command fails.
	 * @throws IllegalArgumentException if the arguments do not match the
	 *	parameters in the metric definition.
	 */
	public synchronized CollectResult collect(String name, MonitorArg[] args) {
		checkPendingException();
		return nativeCollect(name, args);
	}

	/**
	 * Sends a <tt>COLLECT</tt> command to the producer for a metric that
	 * takes no arguments.
	 *
	 * @param name		the name of the metric to collect.
	 * @return		a command status object.
	 *
	 * @see #collect(String, MonitorArg[])
	 */
	public synchronized CollectResult collect(String name) {
		checkPendingException();
		return nativeCollect(name, null);
	}

	/**
	 * Sends a <tt>STOP</tt> command to the producer.
	 *
	 * The <tt>STOP</tt> command instructs the producer to stop sending
	 * metric values for the specified metric ID. Any further references
	 * to the stopped ID will result in an error.
	 *
	 * @param mid		the metric ID to stop.
	 * @param channel	the channel ID where to stop the metric.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	sending the command fails.
	 */
	public synchronized CommandResult stop(int mid, int channel) {
		checkPendingException();
		return nativeStop(mid, channel);
	}

	/**
	 * Sends a <tt>STOP</tt> command to the producer for the current
	 * channel.
	 *
	 * @param mid		the metric ID to stop.
	 * @return		a command status object.
	 *
	 * @see #stop(int, int)
	 */
	public synchronized CommandResult stop(int mid) {
		return stop(mid, 0);
	}

	/**
	 * Sends a <tt>GET</tt> command to the producer.
	 *
	 * The effec of the <tt>GET</tt> command depends on the metric.
	 * <ul>
	 * <li>If the metric is {@link MeasurementType#CONTINUOUS continuous},
	 *	the producer will perform a new measurement and send back the
	 *	resulting metric value.
	 * <li>If the metric is {@link MeasurementType#EVENT event-like}, the
	 *	producer sends back any metric values it has already buffered.
	 *	If there is a <tt>SUBSCRIBE</tt> command in effect for this
	 *	metric ID, the <tt>GET</tt> command will not have any effect.
	 * </ul>
	 *
	 * @param mid		the metric ID to get.
	 * @param channel	the channel ID to perform the GET command on.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	sending the command fails.
	 */
	public synchronized CommandResult get(int mid, int channel) {
		checkPendingException();
		return nativeGet(mid, channel);
	}

	/**
	 * Sends a <tt>GET</tt> command to the producer for the current
	 * channel.
	 *
	 * @param mid		the metric ID to get.
	 * @return		a command status object.
	 *
	 * @see #get(int, int)
	 */
	public CommandResult get(int mid) {
		return get(mid, 0);
	}

	/**
	 * Sends a <tt>SUBSCRIBE</tt> command to the producer.
	 *
	 * The <tt>SUBSCRIBE</tt> command has no effect for
	 * {@link MeasurementType#CONTINUOUS continuous} metrics. In case of
	 * {@link MeasurementType#EVENT event-like} metrics, it instructs the
	 * producer to send any metric values immediately to the consumer when
	 * they are generated.
	 *
	 * @param mid		the metric ID to subscribe.
	 * @param channel	the channel ID where to subscribe the metric to.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	sending the command fails.
	 */
	public synchronized CommandResult subscribe(int mid, int channel) {
		checkPendingException();
		return nativeSubscribe(mid, channel);
	}

	/**
	 * Sends a <tt>SUBSCRIBE</tt> command to the producer for the
	 * current channel.
	 *
	 * @param mid		the metric ID to subscribe.
	 * @return		a command status object.
	 *
	 * @see #subscribe(int, int)
	 */
	public CommandResult subscribe(int mid) {
		return subscribe(mid, 0);
	}

	/**
	 * Sends a <tt>BUFFER</tt> command to the producer.
	 *
	 * The <tt>BUFFER</tt> command cancels the effect of a previous
	 * <tt>SUBSCRIBE</tt> command.
	 *
	 * @param mid		the metric ID to buffer.
	 * @param channel	the channel ID where to buffer the metric on.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open or
	 *	sending the command fails.
	 */
	public synchronized CommandResult buffer(int mid, int channel) {
		checkPendingException();
		return nativeBuffer(mid, channel);
	}

	/**
	 * Sends a <tt>BUFFER</tt> command to the producer for the current
	 * channel.
	 *
	 * @param mid		the metric ID to buffer.
	 * @return		a command status object.
	 *
	 * @see #buffer(int, int)
	 */
	public CommandResult buffer(int mid) {
		return buffer(mid, 0);
	}

	/**
	 * Sends a <tt>QUERY</tt> command to the producer.
	 *
	 * The <tt>QUERY</tt> is a shorthand for sending
	 * <tt>COLLECT</tt>/<tt>GET</tt>/<tt>STOP</tt> commands in
	 * sequence for the same metric ID. It can only be used for
	 * {@link MeasurementType#CONTINUOUS continuous} metrics.
	 *
	 * @param name		the name of the metric to query.
	 * @param args		the arguments of the metric instance.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open.
	 * @throws IllegalArgumentException if the arguments do not match the
	 *	parameters in the metric definition.
	 */
	public synchronized CommandResult query(String name, MonitorArg[] args) {
		checkPendingException();
		return nativeQuery(name, args);
	}

	/**
	 * Sends a <tt>QUERY</tt> command to the producer for a metric that
	 * takes no arguments.
	 *
	 * @param name		the name of the metric to query.
	 * @return		a command status object.
	 *
	 * @see #query(String, MonitorArg[])
	 */
	public synchronized CommandResult query(String name) {
		checkPendingException();
		return nativeQuery(name, null);
	}

	/**
	 * Sends an <tt>EXECUTE</tt> command to the producer.
	 *
	 * The <tt>EXECUTE</tt> command causes the named control to be executed.
	 *
	 * @param name		the name of the control to execute.
	 * @param args		the arguments of the control.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open.
	 * @throws IllegalArgumentException if the arguments do not match the
	 *	parameters in the metric definition.
	 */
	public synchronized CommandResult execute(String name, MonitorArg[] args) {
		checkPendingException();
		return nativeExecute(name, args);
	}

	/**
	 * Sends an <tt>EXECUTE</tt> command to the producer for a control
	 * that takes no arguments.
	 *
	 * @param name		the name of the control to execute.
	 * @return		a command status object.
	 *
	 * @see #execute(String, MonitorArg[])
	 */
	public synchronized CommandResult execute(String name) {
		checkPendingException();
		return nativeExecute(name, null);
	}

	/**
	 * Sends a <tt>WRAP</tt> command to the consumer.
	 *
	 * @param name		the name of the transport layer to negotiate.
	 * @return		a command status object.
	 *
	 * @throws MonitorConnectionException if the connection is not open.
	 * @throws IllegalArgumentException if the transport layer is unknown.
	 */
	public synchronized CommandResult wrap(String name) {
		checkPendingException();
		return nativeWrap(name);
	}
}
