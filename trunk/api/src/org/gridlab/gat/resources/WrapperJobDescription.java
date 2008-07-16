package org.gridlab.gat.resources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.Job.JobState;

public class WrapperJobDescription extends JobDescription {

    /**
     * An instance of this enumeration indicates the staging type of the
     * {@link WrapperJobDescription}.
     * 
     * @author rkemp
     */
    public enum StagingType {
        /**
         * Sequential staging. At any given moment there will be max one of the
         * wrapped jobs be busy staging. Post staging and pre staging can happen
         * together.
         */
        SEQUENTIAL,
        /**
         * Parallel staging. All wrapped jobs can stage in parallel. This might
         * overload the host where the data is located, if many wrapped jobs
         * stage in parallel.
         */
        PARALLEL
    }

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
                Preferences preferences) {
            this.jobDescription = jobDescription;
            this.brokerURI = brokerURI;
            this.preferences = preferences;
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

    private static int preStageIdentifier = 0;

    private static String preStageDoneDirectory;

    private List<WrappedJobInfo> jobInfos = new ArrayList<WrappedJobInfo>();

    private StagingType prestagingType;

    private int level;

    private int maxConcurrentJobs;

    private int jobsUntilPreStageDone;

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
     * Sets the {@link StagingType} for the pre staging phase.
     * 
     * @param stagingType
     *                a {@link StagingType}
     */
    public void setPreStagingType(StagingType stagingType) {
        this.prestagingType = stagingType;
    }

    /**
     * Sets the number of jobs that should be finished pre staging before the
     * wrapper will write it's prestage done file.
     * 
     * @param jobsUntilPreStageDone
     *                the number of jobs that should be finished pre staging
     */
    public void setNumberOfJobsUntilPreStageDone(int jobsUntilPreStageDone) {
        this.jobsUntilPreStageDone = jobsUntilPreStageDone;
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
        jobInfos
                .add(new WrappedJobInfo(jobDescription, brokerURI, preferences));
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
     * Returns the {@link StagingType} for the {@link WrapperJob}.
     * 
     * @return the {@link StagingType} for the {@link WrapperJob}.
     */
    public StagingType getPrestagingType() {
        return prestagingType;
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
     */
    public File getInfoFile() {
        java.io.File f = null;
        try {
            f = File.createTempFile("GAT", "jobDescription");
            FileOutputStream tmp = new FileOutputStream(f);
            ObjectOutputStream out = new ObjectOutputStream(tmp);
            out.writeObject(new URI("any://"
                    + InetAddress.getLocalHost().getCanonicalHostName() + "/"
                    + System.getProperty("user.dir")));
            out.writeInt(level);
            synchronized (WrapperJobDescription.class) {
                out.writeInt(preStageIdentifier++);
            }
            synchronized (WrapperJobDescription.class) {
                if (preStageDoneDirectory == null) {
                    preStageDoneDirectory = System.getProperty("user.dir");
                }
            }
            out.writeObject(preStageDoneDirectory);
            out.writeInt(jobsUntilPreStageDone <= 0 ? jobInfos.size()
                    : jobsUntilPreStageDone);
            out.writeInt(maxConcurrentJobs <= 0 ? jobInfos.size()
                    : maxConcurrentJobs);
            out.writeObject(prestagingType);
            for (WrappedJobInfo jobInfo : jobInfos) {
                jobInfo.generateJobStateFileName();
            }
            out.writeObject(jobInfos);
            out.close();
        } catch (Exception e) {
            // TODO ignore, but should log or throw an exception
        }
        try {
            return GAT.createFile(f.getPath());
        } catch (GATObjectCreationException e) {
            // TODO ignore, but should log or throw an exception
        }
        return null;
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
     * Returns the maximum number of concurrent {@link Job}s that runs at a
     * given moment in the Wrapper.
     * 
     * @return the maximum number of concurrent {@link Job}s that runs at a
     *         given moment in the Wrapper.
     */
    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }

    /**
     * Sets the maximum number of concurrent {@link Job}s that runs at a given
     * moment in the Wrapper.
     * 
     * @param maxConcurrentJobs
     *                the maximum number of concurrent {@link Job}s that runs
     *                at a given moment in the Wrapper.
     */
    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

    /**
     * Sets the (local) directory where the pre stage done files will be written
     * to. If the directory doesn't exists, it will be created. If it does
     * exists, but it isn't a directory an Exception will be thrown. This method
     * can only be invoked once. The user has to delete the pre stage done files
     * itself.
     * 
     * @param location
     * @throws Exception
     */
    public static void setPreStageDoneDirectory(String location)
            throws Exception {
        synchronized (WrapperJobDescription.class) {
            if (preStageDoneDirectory != null) {
                throw new Exception("pre stage done directory already set!");
            }
            java.io.File preStageDoneDirectoryFile = new java.io.File(location);
            if (!preStageDoneDirectoryFile.exists()) {
                preStageDoneDirectoryFile.mkdirs();
            } else if (!preStageDoneDirectoryFile.isDirectory()) {
                throw new Exception(
                        "pre stage done directory exists, but isn't a directory");
            }
            preStageDoneDirectory = preStageDoneDirectoryFile.getPath();
        }
    }

}
