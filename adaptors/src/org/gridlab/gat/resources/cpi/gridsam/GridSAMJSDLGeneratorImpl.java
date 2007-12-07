package org.gridlab.gat.resources.cpi.gridsam;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;

public class GridSAMJSDLGeneratorImpl implements GridSAMJSDLGenerator {

    private Logger logger = Logger.getLogger(GridSAMJSDLGeneratorImpl.class);

    public String generate(SoftwareDescription sd, Sandbox sandbox) {
        StringBuilder builder = new StringBuilder();
        addBegin(builder);
        addApplication(builder, sd, sandbox);
        addEnd(builder);
        return builder.toString();
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
        // add output
        String tmp = (String) attrs.get("stdout");
        if (tmp != null) {
            builder.append("<Output>").append(tmp).append("</Output>");
        }
        
        builder.append("<WorkingDirectory>").append("/home0/mwi300").append("</WorkingDirectory>");

        // add input
        tmp = (String) attrs.get("stdin");
        if (tmp != null) {
            builder.append("<Input>").append(tmp).append("</Input>");
        }

        // add error output
        tmp = (String) attrs.get("stderr");
        if (tmp != null) {
            builder.append("<Error>").append(tmp).append("</Error>");
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
        
        
        // this is really wired!!!
        Map<File, File> preStaged = sd.getPreStaged();
        if (preStaged != null && preStaged.size() > 0) {
                builder.append("<DataStaging>");
                builder.append("<FileName>").append(".").append("</FileName>");
                builder.append("<CreationFlag>overwrite</CreationFlag><DeleteOnTermination>true</DeleteOnTermination>");
                builder.append("<Source><URI>").append(sandbox.getSandbox()).append("</URI></Source>");
                builder.append("</DataStaging>");
        }

        // we have to copy all the files back by ourselves
        Map<File, File> postStaged = sd.getPostStaged();
        for (File file: postStaged.keySet()) {
            if (file.isAbsolute()) {
                // we don't have to move absolute files
                continue;
            }
            builder.append("<DataStaging>");
            addTag(builder, "FileName", file.getPath());
//            builder.append("<FileName>").append(file.getPath()).append("</FileName>");
            builder.append("<CreationFlag>overwrite</CreationFlag><DeleteOnTermination>false</DeleteOnTermination>");
            builder.append("<Target><URI>").append(sandbox.getSandbox() + "/" + file.getPath()).append("</URI></Target>");
            builder.append("</DataStaging>");
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
        
        if (attrs.get("maxCPUTime") != null) {
            addTag(builder,"CPUTimeLimit", attrs.get("maxCPUTime"));
        }
        if (attrs.get("maxMemory") != null) {
            addTag(builder, "MemoryLimit", attrs.get("maxMemory"));
        }
    }
    
    private StringBuilder addTag(StringBuilder builder, String tagName, Object tagValue) {
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
