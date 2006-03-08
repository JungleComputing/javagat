package hu.sztaki.lpds.monitor.producer;

import hu.sztaki.lpds.monitor.Buffer;
import hu.sztaki.lpds.monitor.MetricDefinition;

/**
 * Used for sending back application-generated metric and control values.
 *
 * @author G??bor Gomb??s
 * @version $Id: MeasurementNotifier.java,v 1.3 2005/10/07 11:06:10 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */

public final class MeasurementNotifier {

    /** Used by the native code. */
    private int nativeId;

    /** Definition of the metric/control this notifier belongs to. */
    private MetricDefinition definition;

    /**
     * Constructs a new <code>MeasurementNotifier</code>.
     *
     * @param nativeId	the internal ID of the notifier.
     * @param definition	the metric/control definition the notifier
     *			belongs to.
     */
    MeasurementNotifier(int nativeId, MetricDefinition definition) {
        this.nativeId = nativeId;
        this.definition = definition;
    }

    /**
     * Sends a response to a metric measurement or control execution
     * request.
     *
     * @param value		the value to send to the consumer.
     *
     * @throws ClassCastException if the value does not match the data 
     *	type expected by the metric/control.
     */
    public void sendResponse(Object value) {
        if (value == null) {
            nativeSendResponse(nativeId, null);
        } else {
            Buffer buf = new Buffer();
            buf.encode(value, definition.getType());
            nativeSendResponse(nativeId, buf.getData());
        }
    }

    /**
     * Sends an empty response to a control execution request.
     *
     * The control must have {@link hu.sztaki.lpds.monitor.BasicType#VOID} data type.
     */
    public void sendResponse() {
        nativeSendResponse(nativeId, null);
    }

    /**
     * Sends a response to a metric measurement or control execution
     * request.
     *
     * @param id		the internal ID of the notifier.
     * @param value		the encoded return value.
     */
    private native void nativeSendResponse(int id, byte[] value);
}
