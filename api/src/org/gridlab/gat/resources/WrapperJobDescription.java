package org.gridlab.gat.resources;

import ibis.util.IPUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job.JobState;

public class WrapperJobDescription extends JobDescription {

    /**
     * An instance of this enumeration indicates the scheduled type of the
     * {@link WrapperJobDescription}.
     * 
     * @author rkemp
     */
    public enum ScheduledType {
        /**
         * Wrapped jobs are coordinated by the application, by creating files that
         * trigger the next phase, be it pre-staging, running, or post-staging.
         */
        COORDINATED,
        /**
         * Parallel run. No special scheduling.
         */
        PARALLEL
    }
    
    /**
     * Attribute name of the source directory for common files for
     * wrapped jobs. When set, it specifies a directory that is copied
     * to the destination directory specified with the WRAPPER_COMMON_DEST
     * attribute. If either WRAPPER_COMMON_SRC or WRAPPER_COMMON_DEST is
     * not specified, the wrapper DOES NOT COPY.
     * This mechanism may be used to copy files to the remote machine once
     * (by the wrapper), instead of multiple times (for the wrapped jobs).
     */   
    public static final String WRAPPER_COMMON_SRC = "wrapper.common.src";
    
    /**
     * Attribute name of the destination directory for common files for
     * wrapped jobs. See {@link #WRAPPER_COMMON_SRC}.
     * If either WRAPPER_COMMON_SRC or WRAPPER_COMMON_DEST is
     * not specified, the wrapper DOES NOT COPY.
     * This mechanism may be used to copy files to the remote machine once
     * (by the wrapper), instead of multiple times (for the wrapped jobs).
     */
    
    public static final String WRAPPER_COMMON_DEST = "wrapper.common.dest";

    /**
     * The copying of the wrapper common directory can be coordinated, in which
     * case the attribute "wrapper.common.trigger" should be set to
     * "true". In that case, the trigger directory is used for trigger files
     * with the name "WrapperCommonTrigger.WRAPPERNO", where WRAPPERNO stands for the
     * wrapper number. The existence of this file will enable wrapper WRAPPERNO
     * to copy its {@link #WRAPPER_COMMON_SRC} directory and continue the run.
     * It is up to the JavaGAT application to produce the trigger files.
     */
    public static final String WRAPPER_COMMON_TRIGGER = "wrapper.common.trigger";
    
    /**
     * This object contains all the information necessary to describe a wrapped
     * {@link Job}.
     * 
     * @author rkemp
     */
    public class WrappedJobInfo implements Serializable {

        private static final long serialVersionUID = 4870069252793683143L;

        private JobDescription jobDescription;

        private URI brokerURI;

        private Preferences preferences;

        private URI jobStateFileName;
        
        private int wrappedJobIndex;
        
        private int wrapperJobIndex;

        /**
         * Creates a {@link WrappedJobInfo}.
         * 
         * @param jobDescription
         *                the {@link JobDescription} belonging to this wrapped
         *                {@link Job}.
         * @param brokerURI
         *                the {@link URI} of the broker where this {@link Job}
         *                should be submitted to.
         * @param preferences
         *                the {@link Preferences} that should be used to submit
         *                this {@link Job}.
         */
        public WrappedJobInfo(JobDescription jobDescription, URI brokerURI,
                Preferences preferences, int wrappedJobIndex, int wrapperJobIndex) {
            this.jobDescription = jobDescription;
            this.brokerURI = brokerURI;
            this.preferences = preferences;
            this.wrappedJobIndex = wrappedJobIndex;
            this.wrapperJobIndex = wrapperJobIndex;
        }

        /**
         * Returns the {@link JobDescription} belonging to this wrapped
         * {@link Job}.
         * 
         * @return the {@link JobDescription} belonging to this wrapped
         *         {@link Job}.
         */
        public JobDescription getJobDescription() {
            return jobDescription;
        }

        /**
         * Returns the {@link URI} of the broker where this wrapped {@link Job}
         * should be submitted to.
         * 
         * @return the {@link URI} of the broker where this wrapped {@link Job}
         *         should be submitted to.
         */
        public URI getBrokerURI() {
            return brokerURI;
        }

        /**
         * Returns the {@link Preferences} that should be used to submit the
         * wrapped {@link Job}.
         * 
         * @return the {@link Preferences} that should be used to submit the
         *         wrapped {@link Job}.
         */
        public Preferences getPreferences() {
            return preferences;
        }

        /**
         * Returns the filename of the file that will be used for forwarding the
         * {@link JobState} of the wrapped {@link Job}.
         * If relative, it is relative to where the wrapper job runs.
         * 
         * @return the filename of the file that will be used for forwarding the
         *         {@link JobState} of the wrapped {@link Job}.
         */
        public URI getJobStateFileName() {
            return jobStateFileName;
        }
        
        /**
         * Returns the index of this wrapped job.
         * @return the index.
         */
        public int getWrappedJobIndex() {
            return wrappedJobIndex;
        }
        
        /**
         * Returns the index of the wrapper job encapsulating this wrapped job.
         * @return the index.
         */
        public int getWrapperJobIndex() {
            return wrapperJobIndex;
        }

        void generateJobStateFileName() {
            String path = triggerDirectory.getPath();
            path += "/jobstate_" + wrapperJobIndex + "_" + wrappedJobIndex;
            try {
		this.jobStateFileName = triggerDirectory.setPath(path);
	    } catch (URISyntaxException e) {
		// Should not happen.
		e.printStackTrace();
	    }
        }
    }

    private static final long serialVersionUID = -3241293801064308501L;

    private static int wrapperJobCount = 0;

    private static URI triggerDirectory;

    private List<WrappedJobInfo> jobInfos = new ArrayList<WrappedJobInfo>();

    private ScheduledType scheduledType;

    private int level;
    
    private int wrapperJobIndex;

    /**
     * Creates a {@link WrapperJobDescription} based on the given
     * {@link WrapperSoftwareDescription}.
     * 
     * @param softwareDescription
     *                the {@link WrapperSoftwareDescription} containing the
     *                description of the software to be run
     */
    public WrapperJobDescription(WrapperSoftwareDescription softwareDescription) {
        super(softwareDescription);
        synchronized(WrapperJobDescription.class) {
            wrapperJobIndex = wrapperJobCount++;
        }
    }
    
    /**
     * Gets the wrapper job index.
     * @return the wrapper job index.
     */
    public int getWrapperJobIndex() {
        return wrapperJobIndex;
    }

    /**
     * Creates a {@link WrapperJobDescription} based on the given
     * {@link WrapperSoftwareDescription} and the {@link ResourceDescription}.
     * 
     * @param softwareDescription
     *                the {@link WrapperSoftwareDescription} containing the
     *                description of the software to be run
     * @param resourceDescription
     *                the {@link ResourceDescription} containing the description
     *                of on which resource the software should be run.
     */
    public WrapperJobDescription(
            WrapperSoftwareDescription softwareDescription,
            ResourceDescription resourceDescription) {
        super(softwareDescription, resourceDescription);
    }

    /**
     * Creates a {@link WrapperJobDescription} based on the given
     * {@link WrapperSoftwareDescription} and the {@link Resource}.
     * 
     * @param softwareDescription
     *                the {@link WrapperSoftwareDescription} containing the
     *                description of the software to be run
     * @param resource
     *                the {@link Resource} containing the description of on
     *                which resource the software should be run.
     */
    public WrapperJobDescription(
            WrapperSoftwareDescription softwareDescription, Resource resource) {
        super(softwareDescription, resource);
    }

    /**
     * Sets the {@link ScheduledType}.
     * 
     * @param scheduledType
     *                a {@link ScheduledType}
     */
    public void setScheduledType(ScheduledType scheduledType) {
        this.scheduledType = scheduledType;
    }

    /**
     * Sets the logging level for the wrapper job
     * 
     * @param level
     *                the logging level for the wrapper job
     */
    public void setLoggingLevel(int level) {
        this.level = level;
    }

    /**
     * Add a {@link JobDescription} for a wrapped {@link Job} that should be
     * submitted to a resource broker located at the given {@link URI} with the
     * set of {@link Preferences}.
     * 
     * @param jobDescription
     *                the {@link JobDescription} of the wrapped {@link Job}
     * @param brokerURI
     *                the resource broker {@link URI} for the wrapped
     *                {@link Job}
     * @param preferences
     *                the {@link Preferences} for the wrapped {@link Job}
     */
    public void add(JobDescription jobDescription, URI brokerURI,
            Preferences preferences) {
        jobInfos.add(new WrappedJobInfo(jobDescription, brokerURI,
                preferences, jobInfos.size(), wrapperJobIndex));
    }

    /**
     * Add {@link JobDescription}s for a wrapped {@link Job} that should be
     * submitted to a resource broker located at the given {@link URI} with the
     * set of {@link Preferences}.
     * 
     * @param jobDescriptions
     *                the {@link JobDescription}s of the wrapped {@link Job}s
     * @param brokerURI
     *                the resource broker {@link URI} for the wrapped
     *                {@link Job}s
     * @param preferences
     *                the {@link Preferences} for the wrapped {@link Job}s
     */
    public void add(JobDescription[] jobDescriptions, URI brokerURI,
            Preferences preferences) {
        for (JobDescription jobDescription : jobDescriptions) {
            add(jobDescription, brokerURI, preferences);
        }
    }

    /**
     * Returns a {@link List} of {@link WrappedJobInfo} containing the
     * information for the wrapped {@link Job}s.
     * 
     * @return a {@link List} of {@link WrappedJobInfo} containing the
     *         information for the wrapped {@link Job}s.
     */
    public List<WrappedJobInfo> getJobInfos() {
        return jobInfos;
    }

    /**
     * Returns the {@link ScheduledType} for the {@link WrapperJob}.
     * 
     * @return the {@link ScheduledType} for the {@link WrapperJob}.
     */
    public ScheduledType getScheduledType() {
        return scheduledType;
    }

    /**
     * Returns the logging level for the {@link WrapperJob}.
     * 
     * @return the logging level for the {@link WrapperJob}.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns a {@link File} containing all the information needed by the
     * Wrapper application.
     * 
     * @return a {@link File} containing all the information needed by the
     *         Wrapper application.
     * @throws GATObjectCreationException 
     */
    public File getInfoFile(GATContext context) throws GATObjectCreationException {
        java.io.File f = null;
        try {
            f = File.createTempFile("GAT", "jobDescription");
            f.deleteOnExit();
            FileOutputStream tmp = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(tmp));
            URI originator = new URI("any://"
                    + IPUtils.getLocalHostAddress().getCanonicalHostName() + "/"
                    + System.getProperty("user.dir"));
            out.writeObject(originator);
            out.writeInt(level);
            out.writeInt(wrapperJobIndex);
            String wrapperCommonSrc = (String) softwareDescription.getAttributes().get(WRAPPER_COMMON_SRC);
            out.writeObject(wrapperCommonSrc);
            String wrapperCommonDest = (String) softwareDescription.getAttributes().get(WRAPPER_COMMON_DEST);
            out.writeObject(wrapperCommonDest);
            String sandboxTrigger = (String) softwareDescription.getAttributes().get(WRAPPER_COMMON_TRIGGER);
            out.writeObject(sandboxTrigger);
            synchronized (WrapperJobDescription.class) {
                if (triggerDirectory == null) {
                    triggerDirectory = originator;
                }
                File triggerDir = GAT.createFile(context, triggerDirectory);
                if (triggerDir.exists()) {
                    if (! triggerDir.isDirectory()) {
                        throw new GATObjectCreationException("specified trigger directory " + triggerDirectory
                                + " exists and is not a directory");
                    }
                } else if (! triggerDir.mkdirs()) {
                    throw new GATObjectCreationException("could not create specified trigger directory " + triggerDirectory);
                }
            }
            out.writeObject(triggerDirectory);
            out.writeObject(scheduledType);
            for (WrappedJobInfo jobInfo : jobInfos) {
                jobInfo.generateJobStateFileName();
            }
            out.writeObject(jobInfos);
            out.close();
        } catch (Exception e) {
            throw new GATObjectCreationException("Failed to create wrapper info file", e);
        }
        return GAT.createFile(context, f.getPath());
    }

    /**
     * Returns the filename of the file that's used to forward the
     * {@link JobState} of the wrapped {@link Job} belonging to this
     * {@link JobDescription}.
     * Path is relative to the wrapper location, or absolute.
     * 
     * @param description
     *                the description of the wrapped {@link Job}.
     * @return the filename of the file that's used to forward the
     *         {@link JobState} of the wrapped {@link Job} belonging to this
     *         {@link JobDescription}.
     */
    public URI getJobStateFileName(JobDescription description) {
        for (WrappedJobInfo info : jobInfos) {
            if (info.getJobDescription() == description) {
                return info.jobStateFileName;
            }
        }
        return null;
    }
    
    /**
     * Sets the directory where the trigger files will be written
     * to. This method can only be invoked once. 
     * The user has to create the trigger files itself.
     * Note: the location specified, if not absolute, is to be regarded
     * as relative to where the wrapper job actually is running.
     * 
     * @param location the location of the trigger directory.
     * @throws Exception when the trigger directory is already set.
     */
    public static void setTriggerDirectory(URI location)
            throws Exception {
        synchronized (WrapperJobDescription.class) {
            if (triggerDirectory != null) {
                throw new Exception("triggerdirectory already set!");
            }
            triggerDirectory = location;
        }
    }
}
