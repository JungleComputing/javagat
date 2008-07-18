/*
 * MPA Source File: IArgument.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    29.04.2005 (14:23:59) by doerl $
 * Last Change: 1/14/08 (2:09:37 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

public interface IArgument {
	public static final String ACCOUNT = "gat.account";
	public static final String DATETIME = "pbs.datetime";
	public static final String CHECKPOINT = "pbs.checkpoint";
	public static final String INTERACTIVE = "pbs.interactive";
	public static final String JOIN = "gat.join";
	public static final String KEEP = "pbs.keep";
	public static final String MAIL = "gat.mail";
	public static final String MAILCOND = "gat.mail.condition";
	public static final String PRIORITY = "pbs.priority";
	public static final String QUEUE = "gat.queue";
	public static final String RERUN = "pbs.rerun";
	public static final String SHELL = "gat.shell";
	public static final String USER = "gat.username";
	public static final String ENV = "gat.env";
	public static final String SUPPRESS = "pbs.suppress";
	public static final String HOLD = "gat.hold";
	public static final String CWD = "gat.cwd";
	public static final String SWD = "gat.swd";
	public static final String USER_LINE = "gat.userline";
	public static final String CHECKSUM = "gat.checksum";
	public static final String NOT_DELETE = "gat.not.delete";
	public static final String SHDIR = "pbs.shared";
//	public static final String BINARY = "pbs.binary";
//	public static final String CHECKPOINTED = "pbs.checkpointed";
//	public static final String CLEAR = "pbs.clear";
//	public static final String CWD = "pbs.cwd";
//	public static final String PREFIX = "pbs.prefix";
//	public static final String REMOVE = "pbs.remove";
//	public static final String DISPLAY = "pbs.display";
//	public static final String DEADLINE = "pbs.deadline";
//	public static final String NOTIFY = "pbs.notify";
//	public static final String NOW = "pbs.now";
//	public static final String PROJECT = "pbs.project";
//	public static final String PARALLEL = "pbs.parallel";
//	public static final String SET = "pbs.set";
//	public static final String SOFT = "pbs.soft";
//	public static final String SYNC = "pbs.sync";
//	public static final String DEFINES = "pbs.defines";
//	public static final String VERIFY = "pbs.verify";
//	public static final String WARN = "pbs.warn";
}
