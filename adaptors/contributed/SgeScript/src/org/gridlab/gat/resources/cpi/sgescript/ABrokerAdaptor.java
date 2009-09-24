/*
 * MPA Source File: ABrokerAdaptor.java Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:	6/8/07 (1:40:34 PM) by doerl $ Last Change: 2/13/08 (4:12:25 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.

 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

abstract class ABrokerAdaptor extends ResourceBrokerCpi implements IParameter {
	private static final Logger LOGGER = Logger.getLogger( ABrokerAdaptor.class.getName());
	private static final String DEBUG_LEVEL = System.getProperty( "planck.sge.adaptor.debug", "OFF");
	private static final String PREFIX = "#$";
	private String mMarker;

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
	}

	protected ABrokerAdaptor( GATContext context, URI uri, String marker) throws GATObjectCreationException {
		super( context, uri);
		mMarker = marker;
		Utils.log( LOGGER, Level.FINE, "{0} <constructor>", mMarker);
	}

	private static void addResource( StringBuffer sb, String key, Object val) {
		if (val != null) {
			if (sb.length() > 0)
				sb.append( ",");
			sb.append( key);
			sb.append( "=");
			sb.append( val);
		}
	}

	SgeMessage cancelJob( String id) throws IOException {
		return new SgeMessage( Executer.singleResult( "/qdel " + id));
	}

	SgeMessage holdJob( String id) throws IOException {
		return new SgeMessage( Executer.singleResult( "/qhold -h u " + id));
	}

	Map getInfo( String id) throws GATInvocationException {
		Utils.log( LOGGER, Level.FINE, "{0} getInfo: {1}", mMarker, id);
		SgeResponse job = JobGuard.GUARD.getJob( id);
		if (job == null)
			return new HashMap();
		return job.getInfo();
	}

	private String getInURI( File file) {
		URI uri = file.toGATURI();
		return ":" + uri.getPath();
	}

	private String getOutURI( File file) {
		URI uri = file.toGATURI();
//		String host = uri.getHost();
//		if (host == null) {
//			host = IPUtils.getLocalHostName();
//		}
//		String protocol = uri.getScheme();
//		if ((protocol == null) || protocol.equals("file") || protocol.equals("any")) {
//			protocol = "ftp";
//		}
//		return protocol + "://" + host + "/" + uri.getPath();
		return ":" + uri.getPath();
	}

	SgeMessage releaseJob( String id) throws IOException {
		return new SgeMessage( Executer.singleResult( "/qalter -h U " + id));
	}

	private static String getResource( ResourceDescription rd) {
		Map res = rd.getDescription();
		StringBuffer sb = new StringBuffer();
		addResource( sb, IResources.ARCH, Executer.getArch( res)); // os.name, os.type
//		addResource( sb, IResources.NCPUS, res.get("machine.type"));
		addResource( sb, IResources.S_CPU, res.get( "max.cputime"));
		addResource( sb, IResources.MEM_FREE, res.get( "memory.size"));
		addResource( sb, IResources.NUM_PROC, res.get( "node.count"));
		addResource( sb, IResources.NUM_PROC, res.get( "cpu.per.node"));
//		addResource( sb, IResources.CPUT, res.get("cpu.type"));
		addResource( sb, IResources.H_VMEM, res.get( "disk.size"));
		return (sb.length() > 0) ? sb.toString() : null;
	}

	JobState getState( String id) {
		Utils.log( LOGGER, Level.FINE, "{0} getState: {1}", mMarker, id);
		SgeResponse job = JobGuard.GUARD.getJob( id);
		if (job == null)
			return JobState.SCHEDULED;
		return job.getState();
	}

    public Job submitJob( JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
		Utils.log( LOGGER, Level.FINE, "{0} submitJob", mMarker);
		String id = null;
		SoftwareDescription sd = description.getSoftwareDescription();
		if (sd == null)
			throw new GATInvocationException( "The job description does not contain a software description");
		Map attr = sd.getAttributes();
		Utils.log( LOGGER, Level.FINE, "{0} all software attributes: {1}", mMarker, attr);
		ResourceDescription rd = description.getResourceDescription();
		if (rd == null)
			throw new GATInvocationException( "The job description does not contain a hardware resource description");
		Utils.log( LOGGER, Level.FINE, "{0} all hardware attributes: {1}", mMarker, rd.getDescription());
		Sandbox sandbox = null;
		String host = getHostname();
		if (host != null)
			sandbox = new Sandbox( gatContext, description, host, null, false, true, true, true);
		java.io.File temp = null;
		try {
			temp = java.io.File.createTempFile( "sge", null);
			String tmpname = temp.getName();
			int pos = tmpname.lastIndexOf( '.');
			if (pos > 0)
				tmpname = tmpname.substring( 0, pos);
			ParamWriter job = null;
			try {
				job = new ParamWriter( new BufferedWriter( new FileWriter( temp)), PREFIX);
				job.println( "#!/bin/sh");
				job.println( "# qsub script automatically generated by scheduler");
				job.addDate( "a", attr.get( IArgument.DATETIME));
				job.addDate( "dl", attr.get( IArgument.DEADLINE));
				job.addString( "S", attr.get( IArgument.SHELL));
				job.addString( "A", attr.get( IArgument.ACCOUNT));
				job.addString( "M", attr.get( IArgument.MAIL));
				job.addString( "m", attr.get( IArgument.MAILCOND));
				job.addString( "P", attr.get( IArgument.PROJECT));
				job.addString( "p", attr.get( IArgument.PRIORITY));
				job.addString( "q", attr.get( IArgument.QUEUE));
				job.addString( "sc", attr.get( IArgument.SET));
				job.addString( "u", attr.get( IArgument.USER));
				job.addString( "ac", attr.get( IArgument.DEFINES));
				job.addString( "w", attr.get( IArgument.WARN));
				job.addBoolean( "b", attr.get( IArgument.BINARY));
				job.addBoolean( "j", attr.get( IArgument.JOIN));
				job.addBoolean( "r", attr.get( IArgument.RERUN));
				job.addBoolean( "sync", attr.get( IArgument.SYNC));
				job.addBoolean( "now", attr.get( IArgument.NOW));
				job.addParam( "cwd", attr.get( IArgument.CWD));
				job.addParam( "notify", attr.get( IArgument.NOTIFY));
				job.addParam( "soft", attr.get( IArgument.SOFT));
				job.addParam( "verify", attr.get( IArgument.VERIFY));
				job.addParam( "h", attr.get( IArgument.HOLD));
				job.addString( "N", tmpname);

				if (sd.getStdin() != null)
					job.addString( "i", getInURI( sd.getStdin()));
				if (sd.getStdout() != null)
					job.addString( "o", getOutURI( sd.getStdout()));
				if (sd.getStderr() != null)
					job.addString( "e", getOutURI( sd.getStderr()));
				job.addString( "l", getResource( rd));
				// @FixMe
				job.addParam( "V", attr.get( IArgument.ENV));
//				job.println("PATH=$SGE_O_PATH:$HOME/bin:$PATH");
				// @FixMe
				job.addString( "v", Utils.getEnvironment( sd.getEnvironment()));
				if (attr.get( IArgument.SWD) != null)
					job.println( "cd " + attr.get( IArgument.SWD));
				if (attr.get( IArgument.CHECKSUM) != null)
					job.println( attr.get( IArgument.CHECKSUM));
				if (attr.get( IArgument.USER_LINE) != null)
					job.println( attr.get( IArgument.USER_LINE));
				StringBuffer cmd = new StringBuffer();
				cmd.append( getExecutable( description));
				if (sd.getArguments() != null) {
					String[] args = sd.getArguments();
					for (int i = 0; i < args.length; ++i) {
						cmd.append( " ");
						cmd.append( args[i]);
					}
				}
				job.println( cmd.toString());
			}
			finally {
				if (job != null)
					job.close();
			}
			String output = Executer.singleResult( "/qsub " + temp);
			if ((output != null) && output.startsWith( "Your job "))
				id = output.substring( 9, output.indexOf( ' ', 9));
			else
				throw new GATInvocationException( "The job can not submit to the SGE");
		}
		catch (IOException ex) {
			throw new GATInvocationException( "The job can not submit to the SGE", ex);
		}
		finally {
			if ((temp != null) && !Boolean.TRUE.equals( attr.get( IArgument.NOT_DELETE)))
				temp.delete();
		}
		JobGuard.GUARD.addJob( id);
		return new SgeJob( gatContext, this, description, id, sandbox);
	}
}
