package org.gridlab.gat.resources.cpi.glite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.OrderedCoScheduleJobDescription;
import org.gridlab.gat.resources.OrderedCoScheduleJobDescription.JobLink;

public class DAG_JDL implements JDLInterface{
    private static final Logger LOGGER = LoggerFactory.getLogger(DAG_JDL.class);

    private String jdlString;
    private String virtualOrganisation;

    /**
     * Tree Sets have the advantage of containing an entry at most once. Hence,
     * adding input or output files multiple times will not lead to exceptions
     * at jobRegister.
     */
    private SortedSet<String> inputFiles;
    private List<JobDescription> jobDescriptions;
    private HashSet<JobLink> jobLinks;

    public DAG_JDL(final OrderedCoScheduleJobDescription orderedCoScheduleJobDescription, final String voName)throws GATObjectCreationException {
        inputFiles = new TreeSet<String>();

        if (voName != null) {
            this.virtualOrganisation = voName;
        }

        this.jobDescriptions = orderedCoScheduleJobDescription.getJobDescriptions();
        this.jobLinks = orderedCoScheduleJobDescription.getLinks();
        
        this.jdlString = createJDLFileContent();
    }
    
    /**
     * Write the JDL content to a file on the harddisk. This can be useful for
     * debugging purposes
     * 
     * @return Boolean that indicates success.
     */
    public boolean saveToDisk() {
        boolean success = false;

        try {
            String jdlFileName = "gatjob_" + System.currentTimeMillis() + ".jdl";
            File file = new File(jdlFileName);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(this.jdlString);
            fileWriter.close();
            success = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return success;
    }

    private String createJDLFileContent() throws GATObjectCreationException {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        builder.append("// Auto generated DAG JDL File\n");
        builder.append("Type = \"dag\";\n");
        
        if (!(virtualOrganisation == null)) {
            builder.append("VirtualOrganisation = \"").append(
                    virtualOrganisation).append("\";\n");
        }

        if (!inputFiles.isEmpty()) {
            builder.append("InputSandbox = {\n\t");
            String lastInputFile = inputFiles.last();

            for (String inputFile : inputFiles.headSet(lastInputFile)) {
                builder.append("\"file://").append(inputFile).append("\",\n\t");
            }

            builder.append("\"file://").append(lastInputFile).append("\"\n")
                    .append("};\n");
        }
        
        builder.append("nodes = [\n");
        for (int i = 0; i < jobDescriptions.size(); i++) {
			JobDescription description = jobDescriptions.get(i);
			builder.append("\tNODE_"+description.hashCode()+" = [\n");
			builder.append("\t\tdescription = \n");
			builder.append(new JDL(0, description.getSoftwareDescription(), virtualOrganisation, description.getResourceDescription()).getJdlString().replaceAll("^", "\t\t\t").replaceAll("\n", "\n\t\t\t")+"\n");
			builder.append("\t];\n");
		}
        builder.append("];\n");
        builder.append("dependencies = {\n");
        for (Iterator<JobLink> iterator = jobLinks.iterator(); iterator.hasNext();) {
        	JobLink jobLink = iterator.next();
        	builder.append("\t{ NODE_" + jobLink.getFirstJob().hashCode() + ", NODE_" + jobLink.getSecondJob().hashCode() + "}");
        	if(iterator.hasNext()){
        		builder.append(",\n");
        	}else{
        		builder.append("\n");
        	}
		}
        builder.append("};\n");
        builder.append("]");
        return builder.toString();

    }

    public String getJdlString() {
        return jdlString;
    }
}
