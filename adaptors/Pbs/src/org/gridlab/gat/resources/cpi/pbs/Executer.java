/*
 * MPA Source File: Executer.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    08.11.2005 (15:18:45) by doerl $
 * Last Change: 6/23/08 (11:37:02 AM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gridlab.gat.engine.util.Environment;

public class Executer implements IParameter {
	private static final Logger LOGGER = Logger.getLogger( Executer.class.getName());
	private static final String DEBUG_LEVEL = System.getProperty( "planck.pbs.adaptor.execute.debug", "OFF");
	private static final long TIMEOUT = Integer.getInteger( "planck.pbs.adaptor.execute.timeout", 0).intValue();
	private static Map sArchTypes = new HashMap();
	private static File sTemp = new File( "/tmp");
	private static String sPathBin;
	private static String[] sExport;

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
		Environment env = new Environment();
		String appPath = (env.getVar( PATH) != null) ? env.getVar( PATH) : "/bin:/usr/bin:/usr/X11R6/bin";
		String pbsExec = env.getVar( PBS_EXEC); // "/usr/local/pbs";
		String pbsHome = (env.getVar( PBS_HOME) != null) ? env.getVar( PBS_HOME) : "/var/spool/pbs";
		Vector exp = new Vector();
		if (pbsExec != null) {
			exp.add( PBS_EXEC + "=" + pbsExec);
		}
		exp.add( PBS_HOME + "=" + pbsHome);
		exp.add( PATH + "=" + appPath);
		exp.add( "LD_LIBRARY_PATH=" + pbsExec + "/lib/");
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] PATH = {0}", appPath);
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] PBS_EXEC = {0}", pbsExec);
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] PBS_HOME = {0}", pbsHome);
		setExport( exp);
		sPathBin = (pbsExec != null) ? (pbsExec + "/bin/") : null;
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] SGE_BIN = {0}", sPathBin);
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] TEMP = {0}", sTemp);
		sArchTypes.put( IUname.OS_LINUX, IResources.PBS_LINUX);
		sArchTypes.put( IUname.OS_SOLARIS + "." + IUname.TYPE_SPARC, IResources.PBS_SOLARIS5);
		sArchTypes.put( IUname.OS_SOLARIS + "." + IUname.TYPE_AMD64, IResources.PBS_SOLARIS7);
		sArchTypes.put( IUname.OS_AIX, IResources.PBS_AIX);
		sArchTypes.put( IUname.OS_HP_UX, IResources.PBS_HP);
	}

	private Executer() {
	}

	public static Vector allResults( String command) throws IOException {
		Vector result = new Vector();
		StringReader inRdr = null;
		BufferedReader buffRdr = null;
		try {
			String in = execute( command);
			inRdr = new StringReader( in);
			buffRdr = new BufferedReader( inRdr);
			String text;
			while ((text = buffRdr.readLine()) != null) {
				Utils.log( LOGGER, Level.FINE, "[PBS Executer] result: {0}", text);
				result.addElement( text);
			}
		}
		finally {
			if (buffRdr != null) {
				buffRdr.close();
				buffRdr = null;
			}
			if (inRdr != null) {
				inRdr.close();
				inRdr = null;
			}
		}
		return result;
	}

	public static Object getArch( Map res) {
		Object arch = null;
		Object osName = res.get( "os.name");
		Object osType = res.get( "os.type");
		if (osType != null) {
			arch = Executer.sArchTypes.get( osName + "." + osType);
		}
		if (arch == null) {
			arch = Executer.sArchTypes.get( osName);
		}
		return arch;
	}

	private static String execute( String command) throws IOException {
		String result = "";
		Pipe buffIn = null;
		Pipe buffErr = null;
		OutputStream out = null;
		InputStream in = null;
		InputStream err = null;
		try {
			if (sPathBin != null) {
				command = sPathBin + command;
			}
			Utils.log( LOGGER, Level.FINE, "[PBS Executer] execute command: {0}", command);
			Process proc = Runtime.getRuntime().exec( command, sExport, sTemp);
			out = proc.getOutputStream();
			in = proc.getInputStream();
			err = proc.getErrorStream();
			buffIn = new Pipe( "PBS execute output", in);
			buffErr = new Pipe( "PBS execute error", err);
			buffIn.start();
			buffErr.start();
			Utils.log( LOGGER, Level.FINE, "[PBS Executer] execute waitFor");
			int rVal = proc.waitFor();
			buffIn.join( TIMEOUT);
			buffErr.join( TIMEOUT);
			if (rVal != 0) {
				StringBuffer sb = new StringBuffer( "return value: ");
				sb.append( rVal);
				sb.append( ": ");
				sb.append( buffErr.getBuffer());
				sb.append( "\n");
				sb.append( buffIn.getBuffer());
				Utils.log( LOGGER, Level.FINE, "[PBS Executer] {0}", sb);
				throw new IOException( "failure " + sb);
			}
			result = buffIn.getBuffer().toString();
		}
		catch (InterruptedException ex) {
			StringBuffer sb = new StringBuffer( "InterruptedException: ");
			sb.append( ex.getMessage());
			Utils.log( LOGGER, Level.FINE, "[PBS Executer] {0}", sb);
			throw new IOException( sb.toString());
		}
		finally {
			if (out != null) {
				out.close();
				out = null;
			}
			if (in != null) {
				in.close();
				in = null;
			}
			if (err != null) {
				err.close();
				err = null;
			}
			if ((buffIn != null) && buffIn.isAlive()) {
				buffIn.interrupt();
			}
			if ((buffErr != null) && buffErr.isAlive()) {
				buffErr.interrupt();
			}
			buffIn = null;
			buffErr = null;
		}
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] execute got all");
		return result;
	}

	public static void setExport( Vector export) {
		if (export.size() > 0) {
			sExport = (String[]) export.toArray( new String[export.size()]);
		}
		else {
			sExport = null;
		}
	}

	public static String singleResultCmd( String command) throws IOException {
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] singleResultCmd for: {0}", command);
		String result = null;
		StringReader inRdr = null;
		BufferedReader buffRdr = null;
		try {
			String in = execute( command);
			inRdr = new StringReader( in);
			buffRdr = new BufferedReader( inRdr);
			result = buffRdr.readLine();
		}
		finally {
			if (buffRdr != null) {
				buffRdr.close();
				buffRdr = null;
			}
			if (inRdr != null) {
				inRdr.close();
				inRdr = null;
			}
		}
		Utils.log( LOGGER, Level.FINE, "[PBS Executer] singleResult: {0}", result);
		return result;
	}

	private static class Pipe extends Thread {
		private static final int BUFF_LEN = 2048;
		private Reader mIn;
		private StringWriter mOut = new StringWriter();

		public Pipe( String name, InputStream in) {
			super( name);
			setPriority( Thread.NORM_PRIORITY - 1);
			mIn = new InputStreamReader( in);
		}

		public StringBuffer getBuffer() {
			return mOut.getBuffer();
		}

		public void run() {
			try {
				char[] buff = new char[BUFF_LEN];
				int len;
				while ((len = mIn.read( buff, 0, BUFF_LEN)) >= 0) {
					mOut.write( buff, 0, len);
				}
				mOut.flush();
			}
			catch (IOException ex) {
				Utils.log( LOGGER, Level.WARNING, "io error: {0}", ex);
			}
			finally {
				try {
					mIn.close();
					mIn = null;
				}
				catch (IOException ex) {
					Utils.log( LOGGER, Level.WARNING, "io error: {0}", ex);
				}
				try {
					mOut.close();
				}
				catch (IOException ex) {
					Utils.log( LOGGER, Level.WARNING, "io error: {0}", ex);
				}
			}
		}
	}
}
