/*
 * MPA Source File: SgeScriptBrokerAdaptor.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:05:12) by doerl $
 * Last Change: 1/15/08 (12:12:16 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

public class SgeScriptResourceBrokerAdaptor extends ABrokerAdaptor {
	public SgeScriptResourceBrokerAdaptor( GATContext context, URI uri) throws GATObjectCreationException {
		super( context, uri, "[SGE Script Broker]");
	}
}
