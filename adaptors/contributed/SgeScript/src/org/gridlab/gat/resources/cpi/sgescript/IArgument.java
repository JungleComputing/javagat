/*
 * MPA Source File: IArgument.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    29.04.2005 (14:23:59) by doerl $
 * Last Change: 1/14/08 (1:41:34 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

public interface IArgument {
	public static final String DATETIME = "sge.datetime";
//	public static final String VARIABLE = "sge.variable";
	public static final String ACCOUNT = "gat.account";
	public static final String BINARY = "sge.binary";
	public static final String CHECKPOINTED = "sge.checkpointed";
	public static final String CHECKPOINT = "sge.checkpoint";
	public static final String CLEAR = "sge.clear";
	public static final String CWD = "gat.cwd";
	public static final String PREFIX = "sge.prefix";
	public static final String REMOVE = "sge.remove";
	public static final String DISPLAY = "sge.display";
	public static final String DEADLINE = "sge.deadline";
	public static final String JOIN = "gat.join";
	public static final String MAIL = "gat.mail";
	public static final String MAILCOND = "gat.mail.condition";
	public static final String NOTIFY = "sge.notify";
	public static final String NOW = "sge.now";
	public static final String PROJECT = "sge.project";
	public static final String PRIORITY = "sge.priority";
	public static final String PARALLEL = "sge.parallel";
	public static final String QUEUE = "gat.queue";
	public static final String RERUN = "sge.rerun";
	public static final String SET = "sge.set";
	public static final String SHELL = "gat.shell";
	public static final String SOFT = "sge.soft";
	public static final String SYNC = "sge.sync";
	public static final String USER = "gat.username";
	public static final String DEFINES = "sge.defines";
	public static final String VERIFY = "sge.verify";
	public static final String HOLD = "gat.hold";
	public static final String ENV = "gat.env";
	public static final String WARN = "sge.warn";
	public static final String SWD = "gat.swd";
	public static final String CHECKSUM = "gat.checksum";
	public static final String USER_LINE = "gat.userline";
	public static final String NOT_DELETE = "gat.not.delete";
}
