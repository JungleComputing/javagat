/*
 * MPA Source File: IParameter.java
 * Copyright (c) 2003-2008 by MPA Garching
 *
 * $Created:    14.10.2005 (15:28:04) by doerl $
 * Last Change: 1/14/08 (1:41:57 PM) by doerl
 * $Packaged to JavaGat: 18/07/08 by Alexander Beck-Ratzka, AEI.
 *
 */

package org.gridlab.gat.resources.cpi.sgescript;

public interface IParameter {
	static final String ARC = "ARC";
	static final String SGE_ROOT = "SGE_ROOT";
	static final String SGE_CELL = "SGE_CELL";
	static final String SGE_QMASTER_PORT = "SGE_QMASTER_PORT";
	static final String SGE_EXECD_PORT = "SGE_EXECD_PORT";
	static final String SGE_BINARY_PATH = "SGE_BINARY_PATH";
	static final String SGE_JOB_SPOOL_DIR = "SGE_JOB_SPOOL_DIR";
	static final String SGE_O_HOME = "SGE_O_HOME";
	static final Object SGE_O_HOST = "SGE_O_HOST";
	static final String SGE_O_LOGNAME = "SGE_O_LOGNAME";
	static final String SGE_O_MAIL = "SGE_O_MAIL";
	static final String SGE_O_PATH = "SGE_O_PATH";
	static final String SGE_O_SHELL = "SGE_O_SHELL";
	static final String SGE_O_TZ = "SGE_O_TZ";
	static final String SGE_O_WORKDIR = "SGE_O_WORKDIR";
	static final String SGE_CKPT_ENV = "SGE_CKPT_ENV";
	static final String SGE_CKPT_DIR = "SGE_CKPT_DIR";
	static final String SGE_STDERR_PATH = "SGE_STDERR_PATH";
	static final String SGE_STDOUT_PATH = "SGE_STDOUT_PATH";
	static final String SGE_TASK_ID = "SGE_TASK_ID";
	static final String ENVIRONMENT = "ENVIRONMENT";
	static final String HOME = "HOME";
	static final String HOSTNAME = "HOSTNAME";
	static final String JOB_ID = "JOB_ID";
	static final String JOB_NAME = "JOB_NAME";
	static final String LOGNAME = "LOGNAME";
	static final String NHOSTS = "NHOSTS";
	static final String NQUEUES = "NQUEUES";
	static final String NSLOTS = "NSLOTS";
	static final String PATH = "PATH";
	static final String PE = "PE";
	static final String PE_HOSTFILE = "PE_HOSTFILE";
	static final String QUEUE = "QUEUE";
	static final String REQUEST = "REQUEST";
	static final String SHELL = "SHELL";
	static final String TMPDIR = "TMPDIR";
	static final String TMP = "TMP";
	static final String TZ = "TZ";
	static final String USER = "USER";
}
