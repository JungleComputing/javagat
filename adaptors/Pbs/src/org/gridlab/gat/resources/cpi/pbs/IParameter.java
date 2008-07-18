/*
 * MPA Source File: IParameter.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (15:28:04) by doerl $
 * Last Change: 1/14/08 (2:09:58 PM) by doerl
 * $Packaged to JavaGat: 16/07/08 by Alexander Beck-Ratzka, AEI.
 */

package org.gridlab.gat.resources.cpi.pbs;

public interface IParameter {
	public static final String PBS_EXEC = "PBS_EXEC";
	public static final String PBS_HOME = "PBS_HOME";
	public static final String PBS_O_HOME = "PBS_O_HOME";
	public static final String PBS_O_LANG = "PBS_O_LANG";
	public static final String PBS_O_LOGNAME = "PBS_O_LOGNAME";
	public static final String PBS_O_PATH = "PBS_O_PATH";
	public static final String PBS_O_MAIL = "PBS_O_MAIL";
	public static final String PBS_O_SHELL = "PBS_O_SHELL";
	public static final String PBS_O_TZ = "PBS_O_TZ";
	public static final String PBS_O_HOST = "PBS_O_HOST";
	public static final String PBS_O_QUEUE = "PBS_O_QUEUE";
	public static final String PBS_O_SYSTEM = "PBS_O_SYSTEM";
	public static final String PBS_O_WORKDIR = "PBS_O_WORKDIR";
	public static final String PBS_ENVIRONMENT = "PBS_ENVIRONMENT";
	public static final String PBS_JOBID = "PBS_JOBID";
	public static final String PBS_JOBNAME = "PBS_JOBNAME";
	public static final String PBS_NODEFILE = "PBS_NODEFILE";
	public static final String PBS_QUEUE = "PBS_QUEUE";
	public static final String BEOWULF_JOB_MAP = "BEOWULF_JOB_MAP";
	public static final String ENVIRONMENT = "ENVIRONMENT";
	public static final String PATH = "PATH";
//	public static final String JOB_ID = "JOB_ID";
//	public static final String JOB_NAME = "JOB_NAME";
//	public static final String QUEUE = "QUEUE";
//	public static final String ARC = "ARC";
//	public static final String PBS_CELL = "PBS_CELL";
//	public static final String PBS_BINARY_PATH = "PBS_BINARY_PATH";
//	public static final String PBS_JOB_SPOOL_DIR = "PBS_JOB_SPOOL_DIR";
//	public static final String PBS_CKPT_ENV = "PBS_CKPT_ENV";
//	public static final String PBS_CKPT_DIR = "PBS_CKPT_DIR";
//	public static final String PBS_STDERR_PATH = "PBS_STDERR_PATH";
//	public static final String PBS_STDOUT_PATH = "PBS_STDOUT_PATH";
//	public static final String PBS_TASK_ID = "PBS_TASK_ID";
//	public static final String HOME = "HOME";
//	public static final String HOSTNAME = "HOSTNAME";
//	public static final String LOGNAME = "LOGNAME";
//	public static final String NHOSTS = "NHOSTS";
//	public static final String NQUEUES = "NQUEUES";
//	public static final String NSLOTS = "NSLOTS";
//	public static final String PE = "PE";
//	public static final String PE_HOSTFILE = "PE_HOSTFILE";
//	public static final String REQUEST = "REQUEST";
//	public static final String SHELL = "SHELL";
//	public static final String TMPDIR = "TMPDIR";
//	public static final String TMP = "TMP";
//	public static final String TZ = "TZ";
//	public static final String USER = "USER";
}
