package org.gridlab.gat.resources.cpi.glite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.OrderedCoScheduleJobDescription;
import org.gridlab.gat.resources.OrderedCoScheduleJobDescription.JobLink;

/**
 * Creation of a DAG JDL representation from an {@link OrderedCoScheduleJobDescription}.
 * Currently, the Global attributes InputSandbox, Rank and Requirements are not taken into account.
 * 
 * @author Jerome Revillard
 *
 */
public class JDL_DAG extends AbstractJDL{
    private List<JobDescription> jobDescriptions;
	private List<JDL_Basic> jdls;
    private HashSet<JobLink> jobLinks;

    public JDL_DAG(final OrderedCoScheduleJobDescription orderedCoScheduleJobDescription, final String voName)throws GATObjectCreationException {
        if (voName != null) {
            this.virtualOrganisation = voName;
        }

        this.jobDescriptions = orderedCoScheduleJobDescription.getJobDescriptions();
        this.jobLinks = orderedCoScheduleJobDescription.getLinks();
        
        this.jdlString = createJDLFileContent();
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

        builder.append("nodes = [\n");
        jdls = new ArrayList<JDL_Basic>();
        for (int i = 0; i < jobDescriptions.size(); i++) {
			JobDescription description = jobDescriptions.get(i);
			builder.append("\tNODE_"+description.hashCode()+" = [\n");
			builder.append("\t\tdescription = \n");
			jdls.add(new JDL_Basic(description.getSoftwareDescription(), virtualOrganisation, description.getResourceDescription()));
			builder.append(jdls.get(i).getJdlString().replaceAll("^", "\t\t\t").replaceAll("\n", "\n\t\t\t")+"\n");
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

	public List<JobDescription> getJobDescriptions() {
		return jobDescriptions;
	}
	
	public JobDescription getJobDescription(String nodeName) {
		for (int i = 0; i < jobDescriptions.size(); i++) {
			if(("NODE_"+jobDescriptions.get(i).hashCode()).equals(nodeName)){
				return jobDescriptions.get(i);
			}
		}
		return null;
	}
}
