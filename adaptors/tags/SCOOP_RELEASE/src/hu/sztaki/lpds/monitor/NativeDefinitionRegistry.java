package hu.sztaki.lpds.monitor;

/**
 * Implements a {@link DefinitionRegistry} using the native library.
 *
 * @author Gábor Gombás
 * @version $Id: NativeDefinitionRegistry.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public final class NativeDefinitionRegistry implements DefinitionRegistry {

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

	/**
	 * Constructs a new <code>NativeDefinitionRegistry</code>.
	 */

	public NativeDefinitionRegistry() {
		super();
	}

	/**
	 * Returns the definition of a control.
	 *
	 * @param name		the name of a control.
	 * @return		the definition of the named control.
	 *
	 * @throws UnknownMetricException if the metric name is not known.
	 */
	public native ControlDefinition getControlDefinition(String name)
			throws UnknownMetricException;

	/**
	 * Returns the definition of a metric.
	 *
	 * @param name		the name of a metric.
	 * @return		the definition of the named metric.
	 *
	 * @throws UnknownMetricException if the metric name is not known.
	 */
	public native MetricDefinition getMetricDefinition(String name)
			throws UnknownMetricException;
}
