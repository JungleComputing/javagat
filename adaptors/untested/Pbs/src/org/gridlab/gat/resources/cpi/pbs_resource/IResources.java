/*
 * MPA Source File: IResources.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	20.10.2005 (15:59:04) by doerl $ Last Change: 20.10.2005 (15:59:04) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

/**
 * @author  doerl
 */
public interface IResources {
    public static final String ARCH = "arch";
    public static final String CPUT = "cput";
    public static final String FILE = "file";
    public static final String MEM = "mem";
    public static final String NCPUS = "ncpus";
    public static final String NICE = "nice";
    public static final String NODES = "nodes";
    public static final String PCPUT = "pcput";
    public static final String PMEM = "pmem";
    public static final String PVMEN = "pvmem";
    public static final String RESC = "resc";
    public static final String SOFTWARE = "software";
    public static final String VMEM = "vmem";
    public static final String WALLTIME = "walltime";
    // Cray
    public static final String MPPE = "mppe";
    public static final String MPPT = "mppt";
    public static final String PF = "pf";
    public static final String PMPPT = "pmppt";
    public static final String PNCPUS = "pncpus";
    public static final String PPF = "ppf";
    public static final String PROCS = "procs";
    public static final String PSDS = "psds";
    public static final String SDS = "sds";
	
    //	public static final String CALENDAR = "calendar";
    //	public static final String CPU = "cpu";
    //	public static final String H_CORE = "h_core";
    //	public static final String H_CPU = "h_cpu";
    //	public static final String H_DATA = "h_data";
    //	public static final String H_FSIZE = "h_fsize";
    //	public static final String HOSTNAME = "hostname";
    //	public static final String H_RSS = "h_rss";
    //	public static final String H_RT = "h_rt";
    //	public static final String H_STACK = "h_stack";
    //	public static final String H_VMEM = "h_vmem";
    //	public static final String LOAD_AVG = "load_avg";
    //	public static final String LOAD_LONG = "load_long";
    //	public static final String LOAD_MEDIUM = "load_medium";
    //	public static final String LOAD_SHORT = "load_short";
    //	public static final String MEM_FREE = "mem_free";
    //	public static final String MEM_TOTAL = "mem_total";
    //	public static final String MEM_USED = "mem_used";
    //	public static final String MIN_CPU_INTERVAL = "min_cpu_interval";
    //	public static final String NP_LOAD_AVG = "np_load_avg";
    //	public static final String NP_LOAD_LONG = "np_load_long";
    //	public static final String NP_LOAD_MEDIUM = "np_load_medium";
    //	public static final String NP_LOAD_SHORT = "np_load_short";
    //	public static final String NUM_PROC = "num_proc";
    //	public static final String QNAME = "qname";
    //	public static final String RERUN = "rerun";
    //	public static final String S_CORE = "s_core";
    //	public static final String S_CPU = "s_cpu";
    //	public static final String S_DATA = "s_data";
    //	public static final String SEQ_NO = "seq_no";
    //	public static final String S_FSIZE = "s_fsize";
    //	public static final String SLOTS = "slots";
    //	public static final String S_RSS = "s_rss";
    //	public static final String S_RT = "s_rt";
    //	public static final String S_STACK = "s_stack";
    //	public static final String S_VMEM = "s_vmem";
    //	public static final String SWAP_FREE = "swap_free";
    //	public static final String SWAP_RATE = "swap_rate";
    //	public static final String SWAP_RSVD = "swap_rsvd";
    //	public static final String SWAP_TOTAL = "swap_total";
    //	public static final String SWAP_USED = "swap_used";
    //	public static final String TMPDIR = "tmpdir";
    //	public static final String VIRTUAL_FREE = "virtual_free";
    //	public static final String VIRTUAL_TOTAL = "virtual_total";
    //	public static final String VIRTUAL_USED = "virtual_used";
    //
    //	public static final String PBS_MAC_OS_X = "darwin";
    //	public static final String PBS_TRU64 = "tru64";
    public static final String PBS_AIX = "aix4";
    //	public static final String PBS_AIX51 = "aix51";
    public static final String PBS_DIGITAL = "digitalunix";
    public static final String PBS_FREEBSD = "freebsd";
    public static final String PBS_HP = "hpux10";
    public static final String PBS_IRIX = "irix6";
    public static final String PBS_IRIXARRAY = "irix6array";
    public static final String PBS_LINUX = "linux";
    //	public static final String PBS_LINUX_AMD64 = "lx24-amd64";
    public static final String PBS_NETBSD = "netbsd";
    public static final String PBS_SOLARIS5 = "solaris5";
    public static final String PBS_SOLARIS7 = "solaris7";
    //	public static final String PBS_SOLARIS_SPARC32 = "sol-sparc";
    //	public static final String PBS_SOLARIS_SPARC64 = "sol-sparc64";
    //	public static final String PBS_SOLARIS_X86 = "sol-x86";
    //	public static final String PBS_SOLARIS_AMD64 = "sol-amd64";
}
