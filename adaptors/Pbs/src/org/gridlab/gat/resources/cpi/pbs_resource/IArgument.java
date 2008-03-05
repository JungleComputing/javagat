/*
 * MPA Source File: IJob.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	29.04.2005 (14:23:59) by doerl $ Last Change: 4/29/05 (2:26:25 PM) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

/**
 * @author  doerl
 */
public interface IArgument {
    public static final Object ACCOUNT = "gat.account";
    public static final Object DATETIME = "pbs.datetime";
    public static final Object CHECKPOINT = "pbs.checkpoint";
    public static final Object HOLDING = "pbs.holding";
    public static final Object INTERACTIVE = "pbs.interactive";
    public static final Object JOIN = "gat.join";
    public static final Object KEEP = "pbs.keep";
    public static final String MAIL = "gat.mail";
    public static final String MAILCOND = "gat.mail.condition";
    public static final Object PRIORITY = "pbs.priority";
    public static final Object QUEUE = "gat.queue";
    public static final Object RERUN = "pbs.rerun";
    public static final Object SHELL = "gat.shell";
    public static final Object USER = "gat.username";
    public static final Object ENV = "gat.env";
    public static final Object VARIABLE = "pbs.variable";
    public static final Object SUPPRESS = "pbs.suppress";
    public static final Object CWD = "gat.cwd";
    public static final Object SWD = "gat.swd";
    public static final Object NOT_DELETE = "gat.not.delete";
    public static final Object SHDIR = "pbs.shared";

    //	public static final Object BINARY = "pbs.binary";
    //	public static final Object CHECKPOINTED = "pbs.checkpointed";
    //	public static final Object CLEAR = "pbs.clear";
    //	public static final Object CWD = "pbs.cwd";
    //	public static final Object PREFIX = "pbs.prefix";
    //	public static final Object REMOVE = "pbs.remove";
    //	public static final Object DISPLAY = "pbs.display";
    //	public static final Object DEADLINE = "pbs.deadline";
    //	public static final Object NOTIFY = "pbs.notify";
    //	public static final Object NOW = "pbs.now";
    //	public static final Object PROJECT = "pbs.project";
    //	public static final Object PARALLEL = "pbs.parallel";
    //	public static final Object SET = "pbs.set";
    //	public static final Object SOFT = "pbs.soft";
    //	public static final Object SYNC = "pbs.sync";
    //	public static final Object DEFINES = "pbs.defines";
    //	public static final Object VERIFY = "pbs.verify";
    //	public static final Object WARN = "pbs.warn";
}
