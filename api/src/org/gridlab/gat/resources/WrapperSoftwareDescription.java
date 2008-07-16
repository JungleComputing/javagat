package org.gridlab.gat.resources;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

/**
 * An instance of this class is a description of a piece of java software
 * (component) which is to be submitted as a wrapper job.
 * <p>
 * A wrapper job is a job will be submitted to a certain location and when it
 * runs it will submit other jobs (again using JavaGAT, which if needed is
 * copied to the remote location).
 * <p>
 * Wrapper jobs are useful in certain circumstances. First, some adaptors (like
 * the SGE adaptor) can only be used on a machine with specific software
 * installed. If you want to submit a job using SGE from a machine that doesn't
 * have a SGE installation but you are able to SSH to that machine, you can
 * submit an SSH wrapper job, which will be submitted using the SSH adaptor and
 * when the job runs, it will invoke the SGE adaptor to run the 'real' jobs.
 * <p>
 * The second situation where wrapper jobs are useful, is when you've single
 * threaded jobs that will be scheduled to multicore nodes. Without the wrapper
 * job it will only use one of the available cores and waste the other
 * resources. But using the wrapper job, one can start the wrapper on such a
 * node and then using the Local adaptor, submit as many jobs as wanted to make
 * full use of the available cores.
 * <p>
 * Third, wrapper jobs can prevent the overloading of a head node due to
 * staging. If jobs need a lot of staging, all the data will be staged to the
 * headnode of a cluster. After that, the jobs will be submitted and when the
 * jobs run, they access there data through a shared file system. This works
 * perfectly fine as long as you've enough quota on the head node. But sometimes
 * you don't, but you do have enough quota on the local disks of the nodes. So
 * it might be useful to stage the data to the local node disks. This can be
 * done using the wrapper job. One starts a wrapper job, which will run on a
 * node, then on the node itself it starts the pre staging process for the
 * 'real' jobs, making it possible to pre stage directly to the nodes.
 * <p>
 * The wrapper job always executes the java application
 * "org.gridlab.gat.resources.cpi.Wrapper". Because many things of the software
 * description are fixed for this application, certain inherited methods are
 * overwritten and will do nothing. It's still needed to set the executable (it
 * should point to a java (version 6 or higher) program, for instance
 * /usr/bin/java). Furthermore, you can specify a GAT location to indicate that
 * there's already a GAT installation on the other side and JavaGAT doesn't need
 * to be pre staged. Please be careful that your JavaGAT versions match.
 * 
 * @author rkemp
 * 
 */
public class WrapperSoftwareDescription extends JavaSoftwareDescription {

    /**
     * 
     */
    private static final long serialVersionUID = -8066795059038763966L;

    private URI gatLocation;

    /**
     * Creates a new WrapperSoftwareDescription.
     */
    public WrapperSoftwareDescription() {
        super.setJavaMain("org.gridlab.gat.resources.cpi.Wrapper");
    }

    /**
     * Don't use this method. Calls of this method will be ignored, since the
     * java main is fixed: "org.gridlab.gat.resources.cpi.Wrapper".
     */
    public void setJavaMain(String main) {
        // ignore
    }

    /**
     * Sets the GAT location that can be used to run the wrapper application.
     * 
     * @param gatLocation
     *                the GAT location that can be used to run the wrapper
     *                application.
     */
    public void setGATLocation(URI gatLocation) {
        this.gatLocation = gatLocation;
    }

    /**
     * Gets the GAT location that can be used to run the wrapper application.
     * 
     * @return the GAT location that can be used to run the wrapper application.
     */
    public URI getGATLocation() {
        return gatLocation;
    }

    /**
     * Don't use this method. The java class path is fixed for this object and
     * cannot be set. Calls to this method are ignored.
     */
    public void setJavaClassPath(String classpath) {
        // ignore
    }

    /**
     * Gets the java class path for the wrapper job. Doesn't support the
     * attributes 'sandbox.enable', 'sandbox.use.root'.
     * 
     * @return the java class path for the wrapper job.
     */
    public String getJavaClassPath() {
        if (gatLocation == null) {
            // ok, we'll take the local $GAT_LOCATION/lib and stage it into the
            // sandbox in a
            // directory called lib, the classpath should contain everything
            // like lib/*
            return ".:lib/*";
        } else {
            if (gatLocation.hasAbsolutePath()) {
                return ".:" + gatLocation.getUnresolvedPath() + "/lib/*";
            } else {
                return ".:" + "../" + gatLocation.getUnresolvedPath()
                        + "/lib/*";
            }
        }

    }

    public Map<File, File> getPreStaged() {
        if (gatLocation == null) {
            Map<File, File> result = new HashMap<File, File>();
            try {
                result.put(GAT.createFile(System.getenv("GAT_LOCATION")
                        + java.io.File.separator + "lib"), GAT.createFile("."));
                result.put(GAT.createFile(System.getenv("GAT_LOCATION")
                        + java.io.File.separator + "log4j.properties"), GAT
                        .createFile("log4j.properties"));
            } catch (GATObjectCreationException e) {
                return null;
            }
            return result;
        } else {
            return null;
        }
    }

    public Map<String, String> getJavaSystemProperties() {
        Map<String, String> result = new HashMap<String, String>();
        if (gatLocation == null) {
            result.put("log4j.configuration", "file:log4j.properties");
            result.put("gat.adaptor.path", "lib/adaptors");
        } else {
            if (gatLocation.hasAbsolutePath()) {
                result
                        .put("log4j.configuration", "file:"
                                + gatLocation.getUnresolvedPath()
                                + "/log4j.properties");
                result.put("gat.adaptor.path", gatLocation.getUnresolvedPath()
                        + "/lib/adaptors");

            } else {
                result
                        .put("log4j.configuration", "file:../"
                                + gatLocation.getUnresolvedPath()
                                + "/log4j.properties");
                result.put("gat.adaptor.path", "../"
                        + gatLocation.getUnresolvedPath() + "/lib/adaptors");
            }
        }
        return result;
    }

    public Map<String, Object> getEnvironment() {
        Map<String, Object> result = new HashMap<String, Object>();
        if (gatLocation == null) {
            result.put("GAT_LOCATION", ".");
        } else {
            if (gatLocation.hasAbsolutePath()) {
                result.put("GAT_LOCATION", gatLocation.getUnresolvedPath());
            } else {
                result.put("GAT_LOCATION", "../"
                        + gatLocation.getUnresolvedPath());
            }
        }
        return result;
    }
}
