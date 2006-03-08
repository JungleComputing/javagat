package hu.sztaki.lpds.monitor;

/**
 * Defines how the value of a metric is generated.
 *
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: MeasurementType.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public final class MeasurementType {

	/* The codes must match the C definitions */
	private static final int MON_CONTINUOUS = 0;
	private static final int MON_EVENTLIKE = 1;

	/**
	 * Means that a metric can be measured at any time.
	 */
	public static final MeasurementType CONTINUOUS = new MeasurementType(MON_CONTINUOUS);

	/**
	 * Means that a measurement is triggered by some external event.
	 */
	public static final MeasurementType EVENT = new MeasurementType(MON_EVENTLIKE);

	/**
	 * Internal code of the data type. It is used by the native code.
	 */
	private int code;

	/**
	 * Creates a new <code>MeasurementType</code> object.
	 *
	 * @param code		the internal code of the measurement type. Must
	 *			match the C definitions.
	 */
	private MeasurementType(int code) {
		this.code = code;
	}

	/**
	 * Returns the internal code of the measurement type.
	 *
	 * @return		the internal code.
	 */
	int getCode() {
		return code;
	}

	/**
	 * Converts the measurement type to a textual form.
	 *
	 * @return		the measurement type as a string.
	 */
	public String toString() {
		if (code == MON_CONTINUOUS)
			return "continuous";
		else
			return "event";
	}
}
