package org.gridlab.gat.resources.cpi.gridsam;

import org.apache.log4j.Logger;
import org.gridlab.gat.resources.SoftwareDescription;

public class GridSAMJSDLGeneratorImpl implements GridSAMJSDLGenerator {

    private Logger logger = Logger.getLogger(GridSAMJSDLGeneratorImpl.class);
    
    public String generate(SoftwareDescription sd) {
        StringBuilder builder = new StringBuilder();
        addBegin(builder);
        addApplication(builder, sd);
        addEnd(builder);
        return null;
    }
    
    private StringBuilder addBegin(StringBuilder builder) {
        builder.append("<JobDefinition xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">" +
        		"<JobDescription>" +
        		"<JobIdentification>" +
        		"<JobProject>gridsam</JobProject>" +
        		"</JobIdentification>");
        return builder;
    }
    
    private StringBuilder addEnd(StringBuilder builder) {
        builder.append("</JobDescription>" +
        		"</JobDefinition>");
        return builder;
                
    }
    
    private StringBuilder addApplication(StringBuilder builder, SoftwareDescription sd) {
        builder.append("<Application>").append("<POSIXApplication xmlns=\"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix\">");
        
        builder.append("<Executable>").append(sd.getLocation().getPath()).append("</Executable>");
        if (logger.isDebugEnabled()) {
            logger.debug("executable location=" + sd.getLocation());
            logger.debug("arguments count=" + sd.getArguments().length);
        }
        if (sd.getArguments().length > 0) {
            for (String argument : sd.getArguments()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("argument=" + argument);
                }
                builder.append("<Argument>").append(argument).append("</Argument>");
            }
        }
        builder.append("</POSIXApplication></Application>");
        return builder;
    }
    
    /*
     * <JobDefinition xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl">
    <JobDescription>
        <JobIdentification>
            <JobProject>gridsam</JobProject>
        </JobIdentification>
        <Application>
            <POSIXApplication xmlns="http://schemas.ggf.org/jsdl/2005/11/jsdl-posix">
                <Executable>/bin/sleep</Executable>
                <!--<Argument>5</Argument>-->
                <Argument>10</Argument>
            </POSIXApplication>
        </Application>
    </JobDescription>
</JobDefinition>
     */

}
