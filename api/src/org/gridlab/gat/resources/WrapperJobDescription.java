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

public class WrapperJobDescription extends JobDescription {

    public enum StagingType {
        SEQUENTIAL, PARALLEL
    }

    public class WrappedJobInfo implements Serializable {

        private static final long serialVersionUID = 4870069252793683143L;

        private JobDescription jobDescription;

        private URI brokerURI;

        private Preferences preferences;

        private String jobStateFileName;

        public WrappedJobInfo(JobDescription jobDescription, URI brokerURI,
                Preferences preferences) {
            this.jobDescription = jobDescription;
            this.brokerURI = brokerURI;
            this.preferences = preferences;
            try {
                java.io.File file = java.io.File.createTempFile(".JavaGAT",
                        "jobstate");
                this.jobStateFileName = file.getPath();
                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public JobDescription getJobDescription() {
            return jobDescription;
        }

        public URI getBrokerURI() {
            return brokerURI;
        }

        public Preferences getPreferences() {
            return preferences;
        }

        public String getJobStateFileName() {
            return jobStateFileName;
        }
    }

    private static final long serialVersionUID = -3241293801064308501L;

    private List<WrappedJobInfo> jobInfos = new ArrayList<WrappedJobInfo>();

    private StagingType prestagingType;

    private int level;

    private int maxConcurrentJobs;

    public WrapperJobDescription(WrapperSoftwareDescription softwareDescription) {
        super(softwareDescription);
    }

    public WrapperJobDescription(
            WrapperSoftwareDescription softwareDescription,
            ResourceDescription resourceDescription) {
        super(softwareDescription, resourceDescription);
    }

    public WrapperJobDescription(
            WrapperSoftwareDescription softwareDescription, Resource resource) {
        super(softwareDescription, resource);
    }

    public void setPreStagingType(StagingType stagingType) {
        this.prestagingType = stagingType;
    }

    public void setLoggingLevel(int level) {
        this.level = level;
    }

    public void add(JobDescription jobDescription, URI brokerURI,
            Preferences preferences) {
        jobInfos
                .add(new WrappedJobInfo(jobDescription, brokerURI, preferences));
    }

    public void add(JobDescription[] jobDescriptions, URI brokerURI,
            Preferences preferences) {
        for (JobDescription jobDescription : jobDescriptions) {
            add(jobDescription, brokerURI, preferences);
        }
    }

    public List<WrappedJobInfo> getJobInfos() {
        return jobInfos;
    }

    public StagingType getPrestagingType() {
        return prestagingType;
    }

    public int getLevel() {
        return level;
    }

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
            out.writeInt(maxConcurrentJobs <= 0 ? jobInfos.size()
                    : maxConcurrentJobs);
            out.writeObject(prestagingType);
            out.writeObject(jobInfos);
            out.close();
        } catch (Exception e) {
            // throw new GATInvocationException("WrapperJobDescription", e);
        }
        try {
            return GAT.createFile(f.getPath());
        } catch (GATObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String getJobInfo(JobDescription description) {
        for (WrappedJobInfo info : jobInfos) {
            if (info.getJobDescription() == description) {
                return info.jobStateFileName;
            }
        }
        return null;
    }

    public int getMaxConcurrentJobs() {
        return maxConcurrentJobs;
    }

    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

}
