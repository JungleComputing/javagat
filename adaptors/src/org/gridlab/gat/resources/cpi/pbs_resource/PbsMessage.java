/*
 * MPA Source File: SgeMessage.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	26.10.2005 (15:13:21) by doerl $ Last Change: 26.10.2005 (15:13:21) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.Serializable;

/**
 * @author  doerl
 */
class PbsMessage implements Serializable {
    private static final long serialVersionUID = 6066230625214998210L;
    private String mMessage;

    public PbsMessage(String msg) {
        mMessage = msg;
    }

    public String getMessage() {
        return mMessage;
    }

    public boolean isDeleted() {
        return (mMessage == null) || (mMessage.length() == 0);
    }
}
