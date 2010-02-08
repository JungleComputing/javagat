package org.gridlab.gat.resources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
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
     * Attribute name of common sandbox directory for wrapped jobs.
     * When set, it specifies a directory in which a copy is made of
     * the wrapper sandbox. This copy should be on the machine on which
     * the wrapper runs. This may be useful for wrapped jobs which may all
     * need common files. Using sandbox.common, these common files can be
     * copied to the wrapped-job node once instead of for each wrapped job.
     * The actual location of the sandbox (which is a directory inside
     * the sandbox.common directory) is made available to the wrapped jobs
     * by means of an environment variable <code>WRAPPER_COMMON_SANDBOX</code>.
     */
    
    public static final String SANDBOX_COMMON = "sandbox.common";

    /** The copying of the wrapper sandbox can be coordinated, in which
     * case the attribute "sandbox.common.trigger" should be set to
     * "true". In that case, the trigger directory is used for trigger files
     * with the name "SandboxCopy.WRAPPERNO", where WRAPPERNO stands for the
     * wrapper number. The existence of this file will enable wrapper WRAPPERNO
     * to copy its sandbox and continue the run. It is up to the JavaGAT application
     * to produce the trigger files.
     */
    public static final String SANDBOX_COMMON_TRIGGER = "sandbox.common.trigger";
    
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

        private String jobStateFileName;
        
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
         * 
         * @return the filename of the file that will be used for forwarding the
         *         {@link JobState} of the wrapped {@link Job}.
         */
        public String getJobStateFileName() {
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

        public void generateJobStateFileName() {
            try {
                java.io.File file = java.io.File.createTempFile(".JavaGAT",
                        "jobstate");
                this.jobStateFileName = file.getPath();
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final long serialVersionUID = -3241293801064308501L;

    private static int wrapperJobCount = 0;

    private static String triggerDirectory;

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
            ObjectOutputStream out = new ObjectOutputStream(tmp);
            out.writeObject(new URI("any://"
                    + InetAddress.getLocalHost().getCanonicalHostName() + "/"
                    + System.getProperty("user.dir")));
            out.writeInt(level);
            out.writeInt(wrapperJobIndex);
            String sandboxCopy = (String) softwareDescription.getAttributes().get(SANDBOX_COMMON);
            out.writeObject(sandboxCopy);
            String sandboxTrigger = (String) softwareDescription.getAttributes().get(SANDBOX_COMMON_TRIGGER);
            out.writeObject(sandboxTrigger);
            synchronized (WrapperJobDescription.class) {
                if (triggerDirectory == null) {
                    triggerDirectory = System.getProperty("user.dir");
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
     * 
     * @param description
     *                the description of the wrapped {@link Job}.
     * @return the filename of the file that's used to forward the
     *         {@link JobState} of the wrapped {@link Job} belonging to this
     *         {@link JobDescription}.
     */
    public String getJobStateFileName(JobDescription description) {
        for (WrappedJobInfo info : jobInfos) {
            if (info.getJobDescription() == description) {
                return info.jobStateFileName;
            }
        }
        return null;
    }
    
    /**
     * Sets the (local) directory where the trigger files will be written
     * to. If the directory doesn't exists, it will be created. If it does
     * exist, but it isn't a directory an Exception will be thrown. This method
     * can only be invoked once. The user has to create the trigger files
     * itself.
     * 
     * @param location
     * @throws Exception
     */
    public static void setTriggerDirectory(String location)
            throws Exception {
        synchronized (WrapperJobDescription.class) {
            if (triggerDirectory != null) {
                throw new Exception("triggerdirectory already set!");
            }
            java.io.File triggerDirectoryFile = new java.io.File(location);
            if (!triggerDirectoryFile.exists()) {
                triggerDirectoryFile.mkdirs();
            } else if (!triggerDirectoryFile.isDirectory()) {
                throw new Exception(
                        "triggerdirectory exists, but isn't a directory");
            }
            triggerDirectory = triggerDirectoryFile.getPath();
        }
    }
}
