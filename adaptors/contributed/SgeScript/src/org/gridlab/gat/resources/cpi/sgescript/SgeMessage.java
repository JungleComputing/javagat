/*
 * MPA Source File: SgeMessage.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    26.10.2005 (15:13:21) by doerl $
 * Last Change: 1/14/08 (1:55:08 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.io.Serializable;

class SgeMessage implements Serializable {
	private static final long serialVersionUID = 5197498496054890712L;
	private String mMessage;

	public SgeMessage( String msg) {
		mMessage = msg;
	}

	public boolean isDeleted() {
		return (mMessage == null) || (mMessage.indexOf( "deleted") >= 0);
	}

	public String getMessage() {
		return mMessage;
	}

	public boolean isModified() {
		return (mMessage == null) || (mMessage.indexOf( "modified") >= 0);
	}
}
