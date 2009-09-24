/*
 * MPA Source File: Utils.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    2/13/07 (4:39:03 PM) by doerl $
 * Last Change: 1/14/08 (1:43:14 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class Utils {
	static boolean different( Object obj1, Object obj2) {
		if ((obj1 == null) && (obj2 == null)) {
			return false;
		}
		if ((obj1 != null) && obj1.equals( obj2)) {
			return false;
		}
		return true;
	}

	static String getEnvironment( Map env) {
		if (env == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (Iterator i = env.entrySet().iterator(); i.hasNext();) {
			Object key = i.next();
			Object val = env.get( key);
			if (sb.length() > 0) {
				sb.append( ",");
			}
			sb.append( key);
			if (val != null) {
				sb.append( "=");
				sb.append( val);
			}
		}
		return sb.toString();
	}

	static void log( Logger l, Level lvl, String msg) {
		if (l.isLoggable( lvl)) {
			l.log( lvl, msg);
		}
	}

	static void log( Logger l, Level lvl, String msg, Throwable ex) {
		if (l.isLoggable( lvl)) {
			l.log( lvl, msg, new Object[] { ex.getMessage() });
		}
	}

	static void log( Logger l, Level lvl, String msg, Object o) {
		if (l.isLoggable( lvl)) {
			l.log( lvl, msg, new Object[] { o });
		}
	}

	static void log( Logger l, Level lvl, String msg, Object o1, Object o2) {
		if (l.isLoggable( lvl)) {
			l.log( lvl, msg, new Object[] { o1, o2 });
		}
	}
}
