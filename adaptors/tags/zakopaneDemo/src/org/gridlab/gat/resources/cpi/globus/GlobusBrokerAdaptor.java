/*
 * Created on Oct 14, 2004
 */
package org.gridlab.gat.resources.cpi.globus;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author rob
 */
public class GlobusBrokerAdaptor extends ResourceBrokerCpi {
	public GlobusBrokerAdaptor(GATContext gatContext, Preferences preferences)
			throws AdaptorCreationException {
		super(gatContext, preferences);
		checkName("globus");
	}

	protected String createRSL(JobDescription description)
			throws GATInvocationException {

		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null) {
			throw new GATInvocationException(
					"The job description does not contain a software description");
		}

		String exe = IPUtils.getPath(getLocationURI(description));

		// parse the arguments
		String args = "";
		String[] argsA = getArgumentsArray(description);
		if (argsA != null) {
			for (int i = 0; i < argsA.length; i++) {
				args += "\"" + argsA[i] + "\" ";
			}
		}

		String rsl = "& (executable = " + exe + ")";

		if (args.length() != 0) {
			rsl += " (arguments = " + args + ")";
		}
		rsl += " (count = 1)";

		org.gridlab.gat.io.File stdout = sd.getStdout();
		if (stdout != null) {
			rsl += " (stdout = " + stdout.getPath() + ")";
		}

		org.gridlab.gat.io.File stderr = sd.getStderr();
		if (stdout != null) {
			rsl += " (stderr = " + stderr.getPath() + ")";
		}

		if (GATEngine.VERBOSE) {
			System.err.println("RSL: " + rsl);
		}
		return rsl;
	}

	String getResourceManagerContact(JobDescription description)
			throws GATInvocationException {
		String res = getHostname(description);
		if (res != null) {
			if (GATEngine.VERBOSE) {
				System.err.println("Resource manager contact = " + res);
			}
			return res;
		}

		throw new GATInvocationException(
				"The Globus resource broker needs a hostname");
	}

	String getHostname(JobDescription description)
			throws GATInvocationException {
		ResourceDescription d = description.getResourceDescription();
		if (d == null) {
			return null;
		}

		if (!(d instanceof HardwareResourceDescription)) {
			throw new GATInvocationException(
					"The Globus adaptor currently only supports harware resource descriptions");
		}

		HardwareResourceDescription hd = (HardwareResourceDescription) d;
		Map m = d.getDescription();
		Set keys = m.keySet();
		Iterator i = keys.iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String val = (String) m.get(key);

			if (key.equals("machine.node")) {
				return val;
			}
			System.err.println("GRMS: warning, ignoring key: " + key);
		}

		return null;
	}

	public Job submitJob(JobDescription description)
			throws GATInvocationException, IOException {

		String host = getHostname(description);
		if (host != null) {
			preStageFiles(description, host);
		}

		String rsl = createRSL(description);
		String contact = getResourceManagerContact(description);
		GramJob j = new GramJob(rsl); // @@@ uses default credential
		GlobusJob res = new GlobusJob(this, description, j);
		j.addListener(res);

		try {
			Gram.request(contact, j);
		} catch (Throwable t) {
			throw new GATInvocationException("globus", t);
		}

		return res;
	}
}

