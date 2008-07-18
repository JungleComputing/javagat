/*
 * MPA Source File: PbsScriptBrokerAdaptor.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (13:05:12) by doerl $
 * Last Change: 1/14/08 (2:28:41 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

public class PbsScriptResourceBrokerAdaptor extends ABrokerAdaptor {
	public PbsScriptResourceBrokerAdaptor( GATContext context, URI uri) throws GATObjectCreationException {
		super( context, uri, "[PBS Script Broker]");
	}
}
