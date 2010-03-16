package org.gridlab.gat.resources.cpi.gliteMultiUser;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;
import org.gridlab.gat.resources.cpi.gliteMultiUser.GliteConstants;

/**
 * Describes a Hardware Resource (a CE queue) in gLite.
 * 
 * @author Max Berger
 */
@SuppressWarnings("serial")
public class GliteHardwareResource extends HardwareResourceCpi {

	/** The queue id */
    private final String queId;

    /**
     * Constructor
     * @param gatContext the gat context
     * @param queName the queue name
     */
    public GliteHardwareResource(GATContext gatContext, String queName) {
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

