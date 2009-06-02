/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Resource is a base interface which is realized by any class which wishes to
 * indicate it represents a node or component; currently both a
 * {@link HardwareResource} and a {@link SoftwareResource} realize this
 * interface.
 * 
 * A {@link Reservation} may be associated with this {@link Resource}, and can
 * be obtained by the operation <code>getReservation</code>.
 * 
 * @author rob
 */
public interface Resource extends Monitorable, Advertisable,
        java.io.Serializable {
    /**
     * Returns the {@link ResourceDescription} which describes this
     * {@link Resource} instance.
     * 
     * @return the {@link ResourceDescription} which describes this
     *         {@link Resource} instance.
     */
    public ResourceDescription getResourceDescription();

    /**
     * Returns a {@link Reservation} associated with this {@link Resource}, or
     * <code>null</code> if no reservation was associated.
     * 
     * @return a {@link Reservation} associated with this {@link Resource}, or
     *         <code>null</code> if no reservation was associated.
     */
    public Reservation getReservation();
}
