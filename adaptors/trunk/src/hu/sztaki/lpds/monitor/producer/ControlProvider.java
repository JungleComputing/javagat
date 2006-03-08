package hu.sztaki.lpds.monitor.producer;

import hu.sztaki.lpds.monitor.ControlDefinition;
import hu.sztaki.lpds.monitor.MonitorArg;

/**
 * A <code>ControlProvider</code> defines application-private controls.
 *
 * @author G??bor Gomb??s
 * @version $Id: ControlProvider.java,v 1.4 2006/01/23 11:05:54 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public interface ControlProvider {
    /**
     * Checks if the given argument list is acceptable for the control.
     *
     * @param def                the metric definition.
     * @param args                the metric arguments.
     * @return                <code>true</code> if the arguments are good,
     *                        <code>false</code> otherwise.
     */
    public boolean checkControlArguments(ControlDefinition def,
            MonitorArg[] args);

    /**
     * Executes a control.
     *
     * @param def                the metric definition.
     * @param args                the metric arguments.
     * @param notifier        a {@link MeasurementNotifier} object that is to be
     *                        used to send back the result.
     */
    public void executeControl(ControlDefinition def, MonitorArg[] args,
            MeasurementNotifier notifier);
}
