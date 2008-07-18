/*
 * MPA Source File: ParamWriter.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    08.11.2005 (14:41:28) by doerl $
 * Last Change: 1/14/08 (2:11:09 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParamWriter extends PrintWriter {
	private static final DateFormat sFormatter = new SimpleDateFormat( "yyyyMMddHHmm.ss");
	private String mSuffix = "#$";

	public ParamWriter( Writer out, String suffix) {
		super( out);
		mSuffix = suffix;
	}

	public ParamWriter( Writer out, String suffix, boolean autoFlush) {
		super( out, autoFlush);
		mSuffix = suffix;
	}

	public void addBoolean( String opt, Object param) {
		if (param != null) {
			try {
				Boolean b = (Boolean) param;
				addOption( opt, b.booleanValue() ? "y" : "n");
			}
			catch (ClassCastException ex) {
			}
		}
	}

	public void addDate( String opt, Object param) {
		if (param != null) {
			try {
				Date d = (Date) param;
				addOption( opt, sFormatter.format( d));
			}
			catch (ClassCastException ex) {
			}
		}
	}

	private void addOption( String opt, Object param) {
		print( mSuffix);
		print( " -");
		print( opt);
		if (param != null) {
			print( " ");
			println( param);
		}
		else {
			println();
		}
	}

	public void addParam( String opt, Object param) {
		if (param != null) {
			addOption( opt, null);
		}
	}

	public void addString( String opt, Object param) {
		if (param != null) {
			addOption( opt, param);
		}
	}
}
