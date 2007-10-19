/*
 * MPA Source File: SgeBrokerAdaptor.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	14.10.2005 (13:05:12) by doerl $ Last Change: 14.10.2005 (13:05:12) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 * @author  doerl
 */
public class PbsResourceBrokerAdaptor extends ResourceBrokerCpi implements IParameter {
    private static final String PREFIX = "#PBS";

    public PbsResourceBrokerAdaptor(GATContext context, Preferences pref)
            throws GATObjectCreationException {
        super(context, pref);
        //        System.out.println("constructor: PbsBrokerAdaptor");
    }

    public PbsMessage cancelJob(String id) throws GATInvocationException {
        try {
            return new PbsMessage(Executer.singleResult("qdel " + id));
        } catch (IOException e) {
            throw new GATInvocationException("pbs", e);
        }
    }

    /*************************************************************************************************/

    /** @fn public Job submitJob(JobDescription description) 
     * @brief submit a job to PBS.
     *
     * @param JobDescription description: The GAT JobDescription class. It contains the 
     *                                    HardwareResourceDescription and the SoftwareDescription
     *                                    class. Those are necessary, to create the PBS script
     *                                    for qsub.
     *
     * @retval A GatJob class.
     *
     * @author doerl (created).
     * @author A. Beck-Ratzka, AEI Potsdam, Germany (extended for usage of HardwareResourceDescription)
     * @version 1.0
     * @date 10-05-2006, extended for usage of HardwareResourceDescription.
     * @date 15-05-2006, extended for storing rc of the application to $HOME/.rc.JobID
     */

    public Job submitJob(JobDescription description, MetricListener listener, Metric metric)
            throws GATInvocationException {
        try {
            /**
             declarations
             */

            String id = null;

            String Queue = null;
            String Time = null;
            String Filesize = null;
            String Memsize = null;
            String Nodes = null;
            String LString = null;

            String HwArg = null;

            ResourceDescription rdJob = null;
            HashMap<String, Object> rdJob_attr = null;

            Sandbox sandbox = null;

            /**
             get the descriptions
             */

            SoftwareDescription sd = description.getSoftwareDescription();
            if (sd == null) {
                throw new GATInvocationException(
                        "The job description does not contain a software description");
            }

            rdJob = new HardwareResourceDescription();
            rdJob = description.getResourceDescription();
            if (rdJob == null) {
                throw new GATInvocationException(
                        "The job description does not contain a hardware resource description");
            }

            /**
             load the resource attributes into a new hashtable.
             */

            rdJob_attr = (HashMap<String, Object>) rdJob.getDescription();

            String host = getHostname(description);
            if (host != null) {
                //			removePostStagedFiles(description, host);
                sandbox =
                        new Sandbox(gatContext, preferences, description, host,
                                null, false, true, true, true);
            }
            java.io.File temp = java.io.File.createTempFile("pbs", null);
            try {
                String tmpname = temp.getName();
                int pos = tmpname.lastIndexOf('.');
                if (pos > 0) {
                    tmpname = tmpname.substring(0, pos);
                }
                /* this peace of code is unused, and gives warnings --Rob
                 String retName = null;
                 try {
                 retName = (String) sd.getAttributes().get(IArgument.SHDIR);
                 }
                 catch (ClassCastException ex1) {
                 }
                 */
                //			try {
                //				File retFile = GAT.createFile(gatContext, retName);
                //				sd.addPostStagedFile(retFile, resolvePostStagedFile(retFile, host));
                //			}
                //			catch (Exception ex) {
                //				throw new GATInvocationException("PbsBrokerAdaptor generic postStage", ex);
                //			}
                ParamWriter job = null;
                try {
                    job =
                            new ParamWriter(new BufferedWriter(new FileWriter(
                                    temp)), PREFIX);
                    job.println("#!/bin/sh");
                    job
                            .println("# qsub script automatically generated by scheduler");

                    /**
                     The hardware resources. In the first realization same
                     args as in C-GAT PBS adaptor.
                     */

                    Queue = (String) rdJob_attr.get("machine.queue");
                    if (Queue == null) {
                        Queue = new String("");
                    } else {
                        job.addString("q", Queue);
                    }

                    Time = (String) rdJob_attr.get("cpu.walltime");
                    if (Time == null) {
                        Time = new String("00:01:00");
                    }

                    Filesize = (String) rdJob_attr.get("file.size");
                    if (Filesize == null) {
                        Filesize = new String("");
                    }

                    Memsize = (String) rdJob_attr.get("memory.size");
                    if (Memsize == null) {
                        Memsize = new String("");
                    }

                    Nodes = (String) rdJob_attr.get("cpu.nodes");
                    if (Nodes == null) {
                        Nodes = new String("1");
                    }
                    if (Queue.length() == 0) {
                        LString =
                                new String("walltime=" + Time + ",file="
                                        + Filesize + ",mem=" + Memsize
                                        + ",nodes=" + Nodes);
                    } else {
                        LString =
                                new String("walltime=" + Time + ",file="
                                        + Filesize + ",mem=" + Memsize
                                        + ",nodes=" + Nodes + ":" + Queue);
                    }
                    if (LString == null) {
                        throw new GATInvocationException(
                                "Cannot construct -l option; job submit to PBS failed.");
                    } else {
                        job.addString("l", LString);
                    }

                    HwArg = (String) rdJob_attr.get("Jobname");
                    if (HwArg == null) {
                        HwArg = System.getProperty("user.name");
                    }
                    if (HwArg != null)
                        job.addString("N", HwArg);
                    /** System.getProperty can deliver a null... */
                    HwArg = null;

                    HwArg = (String) rdJob_attr.get("YankEO");
                    if (HwArg != null) {
                        job.addString("j", HwArg);
                        HwArg = null;
                    }

                    //				if (host != null) {
                    //					job.addString("W", "stageout=" + retName + "@" + host + ":" + retName);
                    //				}
                    //				if (sd.getStdin() != null) {
                    //					job.addString("i", getInURI(sd.getStdin()));
                    //				}
                    if (sd.getStdout() != null) {
                        job.addString("o", getOutURI(sd.getStdout()));
                    }
                    if (sd.getStderr() != null) {
                        job.addString("e", getOutURI(sd.getStderr()));
                    }

                    if (sd.getAttributes().get(IArgument.SWD) != null) {
                        job.println("cd "
                                + sd.getAttributes().get(IArgument.SWD));
                    }
                    StringBuffer cmd = new StringBuffer();
                    cmd.append(getLocationURI(description).getPath());
                    if (sd.getArguments() != null) {
                        String[] args = sd.getArguments();
                        for (int i = 0; i < args.length; ++i) {
                            cmd.append(" ");
                            cmd.append(args[i]);
                        }
                    }
                    job.println(cmd.toString());
                    job.println("  echo \"retvalue = ${RETVALUE}\" >"
                            + " ${HOME}" + ".rc." + "${PBS_JOBID}");

                    //                 if (retName != null) {
                    //                     int last = retName.lastIndexOf(java.io.File.separatorChar);
                    //                     job.println("RETVALUE=$?");
                    //                     if (last > 0) {
                    //                         String path = retName.substring(0, last);
                    //                         job.println("if test -d \"" + path + "\"; then");
                    //                     }
                    //                     job.println("  echo \"retvalue = ${RETVALUE}\" >" + retName);
                    //                     job.println("  echo \"queue = ${PBS_QUEUE}\" >>" + retName);
                    //                     job.println("  echo \"jobid = ${PBS_JOBID}\" >>" + retName);
                    //                     job.println("  echo \"jobname = ${PBS_JOBNAME}\" >>" + retName);
                    //                     if (last > 0) {
                    //                         job.println("fi");
                    //                     }
                    //                 }
                } finally {
                    if (job != null) {
                        job.close();
                    }
                }
                id = Executer.singleResult("qsub " + temp);
                if (id == null) {
                    throw new GATInvocationException(
                            "The job can not submit to the PBS");
                }
            } finally {
                Object param = sd.getAttributes().get(IArgument.NOT_DELETE);
                if ((param == null) || !((Boolean) param).booleanValue()) {
                    temp.delete();
                }
            }
            return new PbsJob(gatContext, preferences, this, description, id,
                    sandbox, listener, metric);
        } catch (IOException e) {
            throw new GATInvocationException("pbs", e);
        }
    }

    public PbsMessage unScheduleJob(String id) throws GATInvocationException {
        try {
            return new PbsMessage(Executer.singleResult("qdel " + id));
        } catch (IOException e) {
            throw new GATInvocationException("pbs", e);
        }
    }

    Map<String, Object> getInfo(String id) throws GATInvocationException {
        try {
            Vector<String> params = Executer.allResults("qstat -f " + id);
            if (params.isEmpty()) {
                return new HashMap<String, Object>();
            }
            params.remove(0);
            return Executer.getPropertiesForm(params, '=');
        } catch (IOException e) {
            throw new GATInvocationException("pbs", e);
        }
    }

    int getState(String id) throws IOException {
        PbsResponse job = getJob(id);
        if (job == null) {
            return Job.STOPPED;
        }
        return job.getState();
    }

    /* unused, gives warnings --Rob
     private static void addResource(StringBuffer sb, String key, Object val) {
     if (val != null) {
     if (sb.length() > 0) {
     sb.append(",");
     }
     sb.append(key);
     sb.append("=");
     sb.append(val);
     }
     }

     private static String getResource(ResourceDescription rd) {
     StringBuffer sb = new StringBuffer();
     Map res = rd.getDescription();
     addResource(sb, IResources.ARCH, Executer.getArch(res));
     addResource(sb, IResources.MEM, res.get("memory.size"));
     addResource(sb, IResources.CPUT, res.get("cpu.maxtime"));
     addResource(sb, IResources.NCPUS, res.get("cpu.count"));
     addResource(sb, IResources.FILE, res.get("disk.size"));
     return sb.toString();
     }

     private String getEnvironment(Map env) {
     if (env == null) {
     return null;
     }
     StringBuffer sb = new StringBuffer();
     for (Iterator i = env.entrySet().iterator(); i.hasNext();) {
     Object key = i.next();
     Object val = env.get(key);
     if (sb.length() > 0) {
     sb.append(",");
     }
     sb.append(key);
     if (val != null) {
     sb.append("=");
     sb.append(val);
     }
     }
     return sb.toString();
     }
     */
    //	private String getInURI(File file) {
    //		URI uri = file.toURI();
    //		String host = uri.getHost();
    //		if (host == null) {
    //			host = "localhost";
    //		}
    //		String path = uri.getPath();
    //		if (path.startsWith("//")) {
    //			path = path.substring(1);
    //		}
    //		return host + ":" + path;
    //	}
    //
    private PbsResponse getJob(String id) throws IOException {
        Vector<String> jobs = Executer.allResults("qstat");
        PbsResponse result = null;
        for (int i = 2; i < jobs.size(); ++i) {
            String elem = jobs.get(i);
            PbsResponse temp = PbsResponse.parseJobLine(elem);
            if (id.startsWith(temp.getId())) {
                result = temp;
                break;
            }
        }
        return result;
    }

    private String getOutURI(File file) {
        URI uri = file.toGATURI();
        String host = uri.resolveHost();
        if (host == null) {
            host = "localhost";
        }
        String path = uri.getPath();
        if (path.startsWith("//")) {
            path = path.substring(1);
        }
        return host + ":" + path;
    }
}
