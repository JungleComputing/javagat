package hu.sztaki.lpds.monitor;

/**
 * Represents the definition of a control.
 *
 * Control definitions are just like metric definitions except that there
 * is no measurement type: a control is always "continuous", that is, a
 * consumer has to explicitely request its execution.
 *
 * Controls and metrics have different namespaces so the same name may be used
 * for a metric and a control at the same time.
 *
 * @author Gábor Gombás
 * @version $Id: ControlDefinition.java,v 1.1 2005/06/13 01:48:47 aagapi Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public class ControlDefinition extends MetricDefinition {

	/**
	 * Constructs a new control definition object.
	 *
	 * @param name		the name of the control.
	 * @param params	the parameters of the control.
	 * @param type		the type of the control's return value.
	 */
	public ControlDefinition(String name, MonitorParameter[] params,
			MonitorType type) {
		super(name, params, type, MeasurementType.CONTINUOUS);
	}

	/**
	 * Constructs a new control definition object that has no parameters.
	 *
	 * @param name		the name of the control.
	 * @param type		the type of the control's return value.
	 */
	public ControlDefinition(String name, MonitorType type) {
		super(name, type, MeasurementType.CONTINUOUS);
	}

	/**
	 * Tests the equality of two <code>ControlDefiniton</code> objects.
	 *
	 * Controls are identified by name. Therefore if the control names
	 * match, the definitions ought to match.
	 *
	 * @param obj		the object to compare to.
	 * @return		<code>true</code> if the passed object equals
	 *			to this <code>MetricDefinition</code>.
	 */
	public boolean equals(Object obj) {
		/* We have to make sure that a real MetricDefinition does
		 * not equal to a ControlDefinition */
		if (!(obj instanceof ControlDefinition))
			return false;
		return super.equals(obj);
	}

}
