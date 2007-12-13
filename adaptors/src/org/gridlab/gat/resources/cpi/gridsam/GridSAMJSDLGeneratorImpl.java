package org.gridlab.gat.resources.cpi.gridsam;

import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cas.types.AddTrustAnchor;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;

import ch.ethz.ssh2.crypto.cipher.DES;

public class GridSAMJSDLGeneratorImpl implements GridSAMJSDLGenerator {

    private static final String MAX_MEMORY_ATTRIBUTE = "maxMemory";
    private static final String MAX_CPU_TIME_ATTRIBUTE = "maxCPUTime";
    private static final String STDIN_ATTRIBUTE = "stdin";
    private static final String SLASH = "/";
    private static final String STDERR_ATTRIBUTE = "stderr";
    private static final String STDOUT_ATTRIBUTE = "stdout";
    
    private String javaGatStdin;
    private String javaGatStdout;
    private String javaGatStderr;

    private enum DataStageType {
        SOURCE("Source"),
        TARGET("Target");
        
        private String name;
        
        DataStageType(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
        
    }
    
    public GridSAMJSDLGeneratorImpl(GridSAMConf conf) {
        javaGatStdin = conf.getJavaGATStdin();
        javaGatStdout = conf.getJavaGATStdout();
        javaGatStderr = conf.getJavaGATStderr();
    }
    
    private Logger logger = Logger.getLogger(GridSAMJSDLGeneratorImpl.class);

    public String generate(JobDescription description, Sandbox sandbox) {
        SoftwareDescription sd = description.getSoftwareDescription();
        StringBuilder builder = new StringBuilder();
        addBegin(builder);
        addApplication(builder, sd, sandbox);
        addResource(builder, description, sandbox);
        addDataStaging(builder, sd, sandbox);
        addEnd(builder);
        
        return builder.toString();
    }

    private StringBuilder addResource(StringBuilder builder, JobDescription description, Sandbox sandbox) {

        ResourceDescription rd = description.getResourceDescription();
        if (rd == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("resourceDescription is null, not adding <Resources> tag");
            }
            return builder;
        }
        StringBuilder tmp = new StringBuilder();
        
        Object res = rd.getResourceAttribute("machine.node"); 
        if (res != null) {
            String hosts[] = null;
            if (res instanceof String) {
                hosts = new String[] {(String) res};
            } else if (res instanceof String[]) {
                hosts = (String[]) res;
            } else {
                logger.warn("unknown machine.node type...");
                return builder;
            }
            
            for (String host: hosts) {
                addSimpleTag(tmp, "HostName", host);
            }
        }
        
        // we have added something, so we have to add Resource tag
        if (tmp.length() > 0) {
            builder.append("<Resources><CandidateHosts>");
            builder.append(tmp);
            builder.append("</CandidateHosts></Resources>");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("did not found any properties to use, not adding <Resources> tag");
            }
        }
        
        return builder;
        
    }

    private StringBuilder addBegin(StringBuilder builder) {
        builder.append("<JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">" + "<JobDescription>" + "<JobIdentification>"
                + "<JobProject>gridsam</JobProject>" + "</JobIdentification>");
        return builder;
    }

    private StringBuilder addEnd(StringBuilder builder) {
        builder.append("</JobDescription>" + "</JobDefinition>");
        return builder;

    }

    @SuppressWarnings("unchecked")
    private StringBuilder addApplication(StringBuilder builder, SoftwareDescription sd, Sandbox sandbox) {
        builder.append("<Application>").append("<POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">");

        // add executable
        builder.append("<Executable>").append(sd.getLocation().getPath()).append("</Executable>");
        if (logger.isDebugEnabled()) {
            logger.debug("executable location=" + sd.getLocation() + ", path=" + sd.getLocation().getPath());
            logger.debug("arguments count=" + sd.getArguments().length);
        }

        // add arguments
        if (sd.getArguments().length > 0) {
            for (String argument : sd.getArguments()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("argument=" + argument);
                }
                builder.append("<Argument>").append(argument).append("</Argument>");
            }
        }
        Map<String, Object> attrs = sd.getAttributes();
        
        // add error output
        String tmp = (String) attrs.get(STDERR_ATTRIBUTE);
        if (tmp != null) {
            addSimpleTag(builder, "Error", javaGatStderr);
        }
        
        // add input
        tmp = (String) attrs.get(STDIN_ATTRIBUTE);
        if (tmp != null) {
            addSimpleTag(builder, "Input", javaGatStdin);
            
        }

        // add output
        tmp = (String) attrs.get(STDOUT_ATTRIBUTE);
        if (tmp != null) {
            addSimpleTag(builder, "Output", javaGatStdout);
        }

        Map env = (Map) attrs.get("environment");
        if (env != null) {
            for (Object eo : env.keySet()) {
                String key = (String) eo;
                builder.append("<Environment>").append(env.get(key)).append("</Environment>");
            }
        }
        
        addLimitsInfo(builder, sd);

        builder.append("</POSIXApplication></Application>");
        
        return builder;
    }
    
    private StringBuilder addDataStage(StringBuilder builder, String fileName, DataStageType type, String uri, boolean deleteOnTermination) {
        builder.append("<DataStaging>");
        builder.append("<FileName>").append(fileName).append("</FileName>");
        builder.append("<CreationFlag>overwrite</CreationFlag><DeleteOnTermination>").append(deleteOnTermination ? "true" : "false").append("</DeleteOnTermination>");
        builder.append("<").append(type).append("><URI>").append(uri).append("</URI></").append(type).append(">");
        builder.append("</DataStaging>");
        return builder;
    }

    private StringBuilder addDataStaging(StringBuilder builder, SoftwareDescription sd, Sandbox sandbox) {
        
        // copy whole sandbox
        addDataStage(builder, ".", DataStageType.SOURCE, sandbox.getSandbox(), true);
        
        // we have to copy all the files back by ourselves
        Map<File, File> postStaged = sd.getPostStaged();
        for (File file: postStaged.keySet()) {
            if (file.isAbsolute()) {
                // we don't have to move absolute files
                continue;
            }
            addDataStage(builder, file.getPath(), DataStageType.TARGET, sandbox.getSandbox() + SLASH + file.getPath(), true);
        }
        return builder;
    }

    /**
     * Appends XML info about maximum CPU time, memory limits and so on.
     * 
     * @param builder
     * @param sd
     */
    private void addLimitsInfo(StringBuilder builder, SoftwareDescription sd) {
        Map<String, Object> attrs = sd.getAttributes();
        
        if (attrs.get(MAX_MEMORY_ATTRIBUTE) != null) {
            addSimpleTag(builder, "MemoryLimit", attrs.get(MAX_MEMORY_ATTRIBUTE));
        }
        if (attrs.get(MAX_CPU_TIME_ATTRIBUTE) != null) {
            addSimpleTag(builder,"CPUTimeLimit", attrs.get(MAX_CPU_TIME_ATTRIBUTE));
        }
    }
    
    private StringBuilder addSimpleTag(StringBuilder builder, String tagName, Object tagValue) {
        builder.append("<").append(tagName).append(">").append(tagValue.toString()).append("</").append(tagName).append(">");
        return builder;
    }

    /*
     * <JobDefinition xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl">
     * <JobDescription> <JobIdentification> <JobProject>gridsam</JobProject>
     * </JobIdentification> <Application> <POSIXApplication
     * xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix">
     * <Executable>/bin/sleep</Executable> <!--<Argument>5</Argument>-->
     * <Argument>10</Argument> </POSIXApplication> </Application>
     * </JobDescription> </JobDefinition>
     */

}
