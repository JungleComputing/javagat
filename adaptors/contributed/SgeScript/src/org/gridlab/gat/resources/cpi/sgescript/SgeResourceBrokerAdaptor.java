/*
 * MPA Source File: SgeBrokerAdaptor.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    6/8/07 (1:39:28 PM) by doerl $
 * Last Change: 1/15/08 (12:12:23 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

public class SgeResourceBrokerAdaptor extends ABrokerAdaptor {
	public SgeResourceBrokerAdaptor( GATContext context, URI uri) throws GATObjectCreationException {
		super( context, uri, "[SGE Broker]");
	}
}
