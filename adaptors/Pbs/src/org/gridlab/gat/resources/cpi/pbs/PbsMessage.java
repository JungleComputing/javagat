/*
 * MPA Source File: PbsMessage.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    26.10.2005 (15:13:21) by doerl $
 * Last Change: 1/14/08 (2:17:33 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.Serializable;

class PbsMessage implements Serializable {
	private static final long serialVersionUID = 6066230625214998210L;
	private String mMessage;

	public PbsMessage( String msg) {
		mMessage = msg;
	}

	public boolean isDeleted() {
		return (mMessage == null) || (mMessage.length() == 0);
	}

	public String getMessage() {
		return mMessage;
	}
}
