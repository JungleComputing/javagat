package org.gridlab.gat.resources.cpi.glite;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;

/**
 * Describes a Hardware Resource (a CE queue) in gLite.
 * 
 * @author Max Berger
 */
public class GliteHardwareResource extends HardwareResourceCpi {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String queId;

    protected GliteHardwareResource(GATContext gatContext, String queName) {
        super(gatContext);
        this.queId = queName;
    }

    /** {@inheritDoc} */
    public Reservation getReservation() {
        // Reservations are not supported, so null.
        return null;
    }

    /** {@inheritDoc} */
    public ResourceDescription getResourceDescription() {
        HardwareResourceDescription hrd = new HardwareResourceDescription();
        hrd.addResourceAttribute(GliteConstants.RESOURCE_MACHINE_NODE, queId);
        return hrd;
    }

    /** {@inheritDoc} */
    public String marshal() {
        return queId;
    }

    @Override
    public String toString() {
        return "gLite CE: "+this.queId;
    }

}
