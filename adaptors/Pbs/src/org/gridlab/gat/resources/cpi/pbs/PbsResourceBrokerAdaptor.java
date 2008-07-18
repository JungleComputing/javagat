/*
 * MPA Source File: PbsBrokerAdaptor.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    6/8/07 (2:36:09 PM) by doerl $
 * Last Change: 1/14/08 (2:28:47 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

public class PbsResourceBrokerAdaptor extends ABrokerAdaptor {
	public PbsResourceBrokerAdaptor( GATContext context, URI uri) throws GATObjectCreationException {
		super( context,  uri, "[PBS Broker]");
	}
}
