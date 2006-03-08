/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources;

/**
 * @author rob
 * 
 * An instance of this class describes a job to be run. It consists of a
 * description of the "executable" (a SoftwareDescription), and of a description
 * of the resource requirements of the job. The latter can be given as either a
 * GATResourceDescription, or as a specific GATResource; only one of these may
 * be specified.
 */
public interface JobDescription extends java.io.Serializable {
}