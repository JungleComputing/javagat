/*
 * MPA Source File: ABrokerAdaptor.java Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:	6/8/07 (2:36:31 PM) by doerl $ Last Change: 2/13/08 (4:12:29 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */
package org.gridlab.gat.resources.cpi.pbs;

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
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

abstract class ABrokerAdaptor extends ResourceBrokerCpi implements IParameter {
	private static final Logger LOGGER = Logger.getLogger( ABrokerAdaptor.class.getName());
	private static final String DEBUG_LEVEL = System.getProperty( "planck.pbs.adaptor.debug", "OFF");
	private static final String PREFIX = "#PBS";
	private String mMarker;

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
	}

	public ABrokerAdaptor( GATContext context, URI uri, String marker) throws GATObjectCreationException {
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

	PbsMessage cancelJob( String id) throws GATInvocationException, IOException {
		return new PbsMessage( Executer.singleResultCmd( "qdel " + id));
	}

	PbsMessage holdJob( String id) throws GATInvocationException, IOException {
		return new PbsMessage( Executer.singleResultCmd( "qhold -h u " + id));
	}

	Map getInfo( String id) throws GATInvocationException {
		Utils.log( LOGGER, Level.FINE, "{0} getInfo: {1}", mMarker, id);
		PbsResponse job = JobGuard.GUARD.getJob( id);
		if (job == null)
			return new HashMap();
		return job.getInfo();
	}

//	private String getInURI(File file) {
//		URI uri = file.toURI();
//		String host = uri.getHost();
//		if (host == null) {
//			host = "localhost";
//		}
//		String path = uri.getPath();
//		if (path.startsWith("//")) {
//			path = path.substring(1);
//		}
//		return host + ":" + path;
//	}
//
	private String getOutURI( File file) {
		URI uri = file.toGATURI();
		String path = uri.getPath();
		if (path.startsWith( "//"))
			path = path.substring( 1);
		String host = uri.getHost();
		if (host == null)
//			host = "localhost";
//			return host + ":" + path;
			return path;
		else
			return host + ":" + path;
	}

	PbsMessage releaseJob( String id) throws GATInvocationException, IOException {
		return new PbsMessage( Executer.singleResultCmd( "qrerun " + id));
	}

	private static String getResource( ResourceDescription rd) {
		Map res = rd.getDescription();
		StringBuffer sb = new StringBuffer();
		addResource( sb, IResources.ARCH, Executer.getArch( res)); // os.name, os.type
//		addResource( sb, IResources.NCPUS, res.get("machine.type"));
		addResource( sb, IResources.WALLTIME, res.get( "max.cputime"));
		addResource( sb, IResources.MEM, res.get( "memory.size"));
		addResource( sb, IResources.CPUT, res.get( "cpu.count"));
		Object nodes = res.get( "node.count");
		if (nodes != null) {
			Object ppn = res.get( "cpu.per.node");
			if (ppn != null)
				addResource( sb, IResources.NODES, nodes + ":ppn=" + ppn);
			else
				addResource( sb, IResources.NODES, nodes);
		}
		addResource( sb, IResources.CPUT, res.get( "cpu.type"));
		addResource( sb, IResources.FILE, res.get( "disk.size"));
		return (sb.length() > 0) ? sb.toString() : null;
	}

	int getState( String id) {
		Utils.log( LOGGER, Level.FINE, "{0} getState: {1}", mMarker, id);
		PbsResponse job = JobGuard.GUARD.getJob( id);
		if (job == null)
			return Job.STOPPED;
		return job.getState();
	}

	public Job submitJob( JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {
		Utils.log( LOGGER, Level.FINE, "{0} create Job", mMarker);
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
			temp = java.io.File.createTempFile( "pbs", null);
			String tmpname = temp.getName();
			int pos = tmpname.lastIndexOf( '.');
			if (pos > 0)
				tmpname = tmpname.substring( 0, pos);
//			try {
//				File retFile = GAT.createFile(gatContext, retName);
//				sd.addPostStagedFile(retFile, resolvePostStagedFile(retFile, host));
//			}
//			catch (Exception ex) {
//				throw new GATInvocationException("PbsBrokerAdaptor generic postStage", ex);
//			}
			ParamWriter job = null;
			try {
				job = new ParamWriter( new BufferedWriter( new FileWriter( temp)), PREFIX);
				job.println( "#!/bin/sh");
				job.println( "# qsub script automatically generated by scheduler");
				job.addString( "A", attr.get( IArgument.ACCOUNT));
				job.addDate( "a", attr.get( IArgument.DATETIME));
				job.addBoolean( "j", attr.get( IArgument.JOIN));
				job.addString( "M", attr.get( IArgument.MAIL));
				job.addString( "m", attr.get( IArgument.MAILCOND));
				job.addString( "S", attr.get( IArgument.SHELL));
				job.addString( "p", attr.get( IArgument.PRIORITY));
				job.addString( "q", attr.get( IArgument.QUEUE));
				job.addString( "u", attr.get( IArgument.USER));
				job.addBoolean( "r", attr.get( IArgument.RERUN));
				job.addParam( "V", attr.get( IArgument.ENV));
				job.addParam( "h", attr.get( IArgument.HOLD));
				job.addString( "N", tmpname);
//				if (host != null) {
//					job.addString("W", "stageout=" + retName + "@" + host + ":" + retName);
//				}
//				if (sd.getStdin() != null) {
//					job.addString("i", getInURI(sd.getStdin()));
//				}
				if (sd.getStdoutFile() != null)
					job.addString( "o", getOutURI( sd.getStdoutFile()));
				if (sd.getStderrFile() != null)
					job.addString( "e", getOutURI( sd.getStderrFile()));
				job.addString( "v", Utils.getEnvironment( sd.getEnvironment()));
				job.addString( "l", getResource( rd));
				if (attr.get( IArgument.SWD) != null)
					job.println( "cd " + attr.get( IArgument.SWD));
				if (sd.getAttributes().get( IArgument.CHECKSUM) != null)
					job.println( sd.getAttributes().get( IArgument.CHECKSUM));
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
				String retName = (String) attr.get( IArgument.SHDIR);
				if (retName != null) {
					job.println( "RETVALUE=$?");
					int last = retName.lastIndexOf( java.io.File.separatorChar);
					if (last > 0) {
						String path = retName.substring( 0, last);
						job.println( "if test -d \"" + path + "\"; then");
					}
					job.println( "  echo \"retvalue = ${RETVALUE}\" >" + retName);
					job.println( "  echo \"queue = ${PBS_QUEUE}\" >>" + retName);
					job.println( "  echo \"jobid = ${PBS_JOBID}\" >>" + retName);
					job.println( "  echo \"jobname = ${PBS_JOBNAME}\" >>" + retName);
					if (last > 0)
						job.println( "fi");
				}
			}
			catch (ClassCastException ex) {
				System.err.println( ex.getMessage());
			}
			finally {
				if (job != null)
					job.close();
			}
			id = Executer.singleResultCmd( "qsub " + temp);
			if (id == null)
				throw new GATInvocationException( "The job can not submit to the PBS");
		}
		catch (IOException ex) {
			throw new GATInvocationException( "The job can not submit to the PBS", ex);
		}
		finally {
			if ((temp != null) && !Boolean.TRUE.equals( attr.get( IArgument.NOT_DELETE)))
				temp.delete();
		}
		Utils.log( LOGGER, Level.FINE, "{0} submitted Job: {1}", mMarker, id);
		JobGuard.GUARD.addJob( id);
		return new PbsJob( gatContext, this, description, id, sandbox);
	}

	public PbsMessage unScheduleJob( String id) throws GATInvocationException, IOException {
		return new PbsMessage( Executer.singleResultCmd( "qdel " + id));
	}
}
