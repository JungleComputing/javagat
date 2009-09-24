/*
 * MPA Source File: Executer.java Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:	08.11.2005 (15:18:45) by doerl $ Last Change: 1/21/08 (3:28:26 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

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
	private static final String DEBUG_LEVEL = System.getProperty( "planck.sge.adaptor.execute.debug", "OFF");
	private static final long TIMEOUT = Integer.getInteger( "planck.sge.adaptor.execute.timeout", 0).intValue();
	private static Map sArchTypes = new HashMap();
	private static File sTemp = new File( "/tmp");
	private static String sPathBin;
	private static String[] sExport;

	static {
		LOGGER.setLevel( Level.parse( DEBUG_LEVEL));
		Environment env = new Environment();
		String appPath = (env.getVar( PATH) != null) ? env.getVar( PATH) : "/bin:/usr/bin";
		String sgeCell = (env.getVar( SGE_CELL) != null) ? env.getVar( SGE_CELL) : "default";
		String sgeRoot = (env.getVar( SGE_ROOT) != null) ? env.getVar( SGE_ROOT) : "/usr";
		String sgePort = env.getVar( SGE_QMASTER_PORT);
		String sgeExec = env.getVar( SGE_EXECD_PORT);
		Vector exp = new Vector();
		exp.add( SGE_ROOT + "=" + sgeRoot);
		exp.add( SGE_CELL + "=" + sgeCell);
		if (sgePort != null)
			exp.add( SGE_QMASTER_PORT + "=" + sgePort);
		if (sgeExec != null)
			exp.add( SGE_EXECD_PORT + "=" + sgeExec);
		exp.add( PATH + "=" + appPath);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] PATH = {0}", appPath);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] SGE_CELL = {0}", sgeCell);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] SGE_ROOT = {0}", sgeRoot);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] SGE_QMASTER_PORT = {0}", sgePort);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] SGE_EXECD_PORT = {0}", sgeExec);
		setExport( exp);
		String output = null;
		try {
			output = singleResultCmd( sgeRoot + "/util/arch");
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		String sgeArch = (output != null) ? output : "solaris";
		String sgeLibs = sgeRoot + "/lib/" + sgeArch;
		exp.add( "LD_LIBRARY_PATH=" + sgeLibs);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] Arch = {0}", sgeArch);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] LD_LIBRARY_PATH = {0}", sgeLibs);
		setExport( exp);
		sPathBin = sgeRoot + "/bin/" + sgeArch;
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] SGE_BIN = {0}", sPathBin);
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] TEMP = {0}", sTemp);
		sArchTypes.put( IUname.OS_LINUX, IResources.SGE_LINUX_X86);
		sArchTypes.put( IUname.OS_LINUX + "." + IUname.TYPE_AMD64, IResources.SGE_LINUX_AMD64);
		sArchTypes.put( IUname.OS_SOLARIS + "." + IUname.TYPE_SPARC, IResources.SGE_SOLARIS_SPARC32);
		sArchTypes.put( IUname.OS_SOLARIS + "." + IUname.TYPE_XX86, IResources.SGE_SOLARIS_X86);
		sArchTypes.put( IUname.OS_SOLARIS + "." + IUname.TYPE_AMD64, IResources.SGE_SOLARIS_AMD64);
		sArchTypes.put( IUname.OS_AIX, IResources.SGE_AIX43);
		sArchTypes.put( IUname.OS_HP_UX, IResources.SGE_HP11);
		sArchTypes.put( IUname.OS_MAC_OS_X, IResources.SGE_MAC_OS_X);
		sArchTypes.put( IUname.OS_TRU64, IResources.SGE_TRU64);
	}

	private Executer() {
	}

	public static Vector allResults( String command) throws IOException {
		Vector result = new Vector();
		StringReader inRdr = null;
		BufferedReader buffRdr = null;
		try {
			String in = execute( sPathBin + command);
			inRdr = new StringReader( in);
			buffRdr = new BufferedReader( inRdr);
			String text;
			while ((text = buffRdr.readLine()) != null) {
				Utils.log( LOGGER, Level.FINE, "[SGE Executer] result: {0}", text);
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
		if (osType != null)
			arch = sArchTypes.get( osName + "." + osType);
		if (arch == null)
			arch = sArchTypes.get( osName);
		if (arch == null) {
			if (osType != null)
				Utils.log( LOGGER, Level.WARNING, "unknown architecture: {0}.{1}", osName, osType);
			else
				Utils.log( LOGGER, Level.WARNING, "unknown architecture: {0}", osName);
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
			Utils.log( LOGGER, Level.FINE, "[SGE Executer] execute command: {0}", command);
			Process proc = Runtime.getRuntime().exec( command, sExport, sTemp);
			out = proc.getOutputStream();
			in = proc.getInputStream();
			err = proc.getErrorStream();
			buffIn = new Pipe( "SGE execute output", in);
			buffErr = new Pipe( "SGE execute error", err);
			buffIn.start();
			buffErr.start();
			Utils.log( LOGGER, Level.FINE, "[SGE Executer] execute waitFor");
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
				Utils.log( LOGGER, Level.FINE, "[SGE Executer] {0}", sb);
				throw new IOException( "failure " + sb);
			}
			result = buffIn.getBuffer().toString();
		}
		catch (InterruptedException ex) {
			StringBuffer sb = new StringBuffer( "InterruptedException: ");
			sb.append( ex.getMessage());
			Utils.log( LOGGER, Level.FINE, "[SGE Executer] {0}", sb);
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
			if ((buffIn != null) && buffIn.isAlive())
				buffIn.interrupt();
			if ((buffErr != null) && buffErr.isAlive())
				buffErr.interrupt();
			buffIn = null;
			buffErr = null;
		}
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] execute got all");
		return result;
	}

	private static void setExport( Vector export) {
		if (export.size() > 0)
			sExport = (String[]) export.toArray( new String[export.size()]);
		else
			sExport = null;
	}

	public static String singleResult( String command) throws IOException {
		return singleResultCmd( sPathBin + command);
	}

	private static String singleResultCmd( String command) throws IOException {
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] singleResultCmd for: {0}", command);
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
		Utils.log( LOGGER, Level.FINE, "[SGE Executer] singleResult: {0}", result);
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
