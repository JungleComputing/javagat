package org.gridlab.gat.resources.cpi.gridsam;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.icenigrid.schema.jsdl.posix.y2005.m11.ArgumentType;
import org.icenigrid.schema.jsdl.posix.y2005.m11.EnvironmentType;
import org.icenigrid.schema.jsdl.posix.y2005.m11.FileNameType;
import org.icenigrid.schema.jsdl.posix.y2005.m11.LimitsType;
import org.icenigrid.schema.jsdl.posix.y2005.m11.POSIXApplicationDocument;
import org.icenigrid.schema.jsdl.posix.y2005.m11.POSIXApplicationType;
import org.icenigrid.schema.jsdl.y2005.m11.ApplicationType;
import org.icenigrid.schema.jsdl.y2005.m11.CandidateHostsType;
import org.icenigrid.schema.jsdl.y2005.m11.CreationFlagEnumeration;
import org.icenigrid.schema.jsdl.y2005.m11.DataStagingType;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionDocument;
import org.icenigrid.schema.jsdl.y2005.m11.JobDefinitionType;
import org.icenigrid.schema.jsdl.y2005.m11.JobDescriptionType;
import org.icenigrid.schema.jsdl.y2005.m11.JobIdentificationType;
import org.icenigrid.schema.jsdl.y2005.m11.ResourcesType;
import org.icenigrid.schema.jsdl.y2005.m11.SourceTargetType;

public class GridSAMJSDLGeneratorImpl implements GridSAMJSDLGenerator {

    private static final String MAX_MEMORY_ATTRIBUTE = "memory.max";

    private static final String MAX_CPU_TIME_ATTRIBUTE = "cputime.max";

    private static final String STDIN_ATTRIBUTE = "stdin";

    private static final String SLASH = "/";

    private static final String STDERR_ATTRIBUTE = "stderr";

    private static final String STDOUT_ATTRIBUTE = "stdout";

    private String javaGatStdin;

    private String javaGatStdout;

    private String javaGatStderr;

    private enum DataStageType {
        SOURCE("Source"), TARGET("Target");

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

    public JobDefinitionDocument generate(JobDescription description, Sandbox sandbox) {
        
        SoftwareDescription sd = description.getSoftwareDescription();
        
        JobDefinitionDocument jobDefinitionDocument = JobDefinitionDocument.Factory.newInstance();
        JobDefinitionType jobDef = jobDefinitionDocument.addNewJobDefinition();
        JobDescriptionType jobDescr = jobDef.addNewJobDescription();
        JobIdentificationType jobid = jobDescr.addNewJobIdentification();
        jobid.addJobProject("gridsam");

        addApplication(jobDescr, sd, sandbox);
        addResource(jobDescr, description, sandbox);
        addDataStaging(jobDescr, sd, sandbox);
        
        return jobDefinitionDocument;
    }

    private void addResource(JobDescriptionType jobDescr, JobDescription description, Sandbox sandbox) {

        ResourceDescription rd = description.getResourceDescription();
        if (rd == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("resourceDescription is null, not adding <Resources> tag");
            }
            return;
        }

        Object res = rd.getResourceAttribute("machine.node");
        String hosts[] = null;
        ResourcesType resources = null;
        if (res != null) {
            if (res instanceof String) {
                hosts = new String[] { (String) res };
            } else if (res instanceof String[]) {
                hosts = (String[]) res;
            } else {
                logger.warn("unknown machine.node type...");
                return;
            }
            if (hosts != null && hosts.length != 0) {
                resources = jobDescr.addNewResources();
                CandidateHostsType candidates = resources.addNewCandidateHosts();
                
                for (String host : hosts) {
                    candidates.addHostName(host);
                }
            } else {  

                if (logger.isDebugEnabled()) {
                    logger
                    .debug("did not found any properties to use, not adding <Resources> tag");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addApplication(JobDescriptionType jobDescr,
            SoftwareDescription sd, Sandbox sandbox) {
        if (sd.getExecutable() != null) {
            ApplicationType appl = jobDescr.addNewApplication();
            XmlCursor cursor = appl.newCursor();
            cursor.toEndToken();
            
            POSIXApplicationDocument posixDoc = POSIXApplicationDocument.Factory
                    .newInstance();
            POSIXApplicationType posixAppl = posixDoc.addNewPOSIXApplication();
            FileNameType f = posixAppl.addNewExecutable();
            f.setStringValue(sd.getExecutable());
            
            if (sd.getArguments() != null) {
                logger.debug("arguments count=" + sd.getArguments().length);
            } else {
                logger.debug("null arguments!");
            }

            // add arguments
            if (sd.getArguments()!= null && sd.getArguments().length > 0) {
                for (String argument : sd.getArguments()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("argument=" + argument);
                    }
                    ArgumentType arg = posixAppl.addNewArgument();
                    arg.setStringValue(argument);
                }
            }
            
            Map<String, Object> attrs = sd.getAttributes();

            // add error output
            String tmp = (String) attrs.get(STDERR_ATTRIBUTE);
            if (tmp != null) {
                f = posixAppl.addNewError();
                f.setStringValue(javaGatStderr);
            }

            // add input
            tmp = (String) attrs.get(STDIN_ATTRIBUTE);
            if (tmp != null) {
                f = posixAppl.addNewInput();
                f.setStringValue(javaGatStdin);
            }

            // add output
            tmp = (String) attrs.get(STDOUT_ATTRIBUTE);
            if (tmp != null) {
                f = posixAppl.addNewOutput();               
                f.setStringValue(javaGatStdout);
            }

            Map<String, Object> env = sd.getEnvironment();
            if (env != null) {
                for (Object eo : env.keySet()) {
                    EnvironmentType ev = posixAppl.addNewEnvironment();                   
                    ev.setName(eo.toString());
                    ev.setStringValue(env.get(eo).toString());
                }
            }

            addLimitsInfo(posixAppl, sd);
            
            XmlCursor c = posixDoc.newCursor();
            c.toStartDoc();
            c.toNextToken();
            c.moveXml(cursor);
            
        } else {
            logger.debug("No executable in job description!");
        }
    }

    private void addDataStage(JobDescriptionType jobDescr, String fileName,
            DataStageType type, String uri, boolean deleteOnTermination) {
        DataStagingType ds = jobDescr.addNewDataStaging();
        SourceTargetType st = type == DataStageType.SOURCE ? ds.addNewSource() : ds.addNewTarget();
        st.setURI(uri);
        ds.setCreationFlag(CreationFlagEnumeration.OVERWRITE);
        ds.setDeleteOnTermination(deleteOnTermination);
        ds.setFileName(fileName);
    }

    private void addDataStaging(JobDescriptionType jobDescr,
            SoftwareDescription sd, Sandbox sandbox) {

        // copy whole sandbox
        addDataStage(jobDescr, ".", DataStageType.SOURCE, sandbox.getSandboxPath(),
                true);

        // we have to copy all the files back by ourselves
        Map<File, File> postStaged = sd.getPostStaged();
        for (File file : postStaged.keySet()) {
            if (file.isAbsolute()) {
                // we don't have to move absolute files
                continue;
            }
            addDataStage(jobDescr, file.getPath(), DataStageType.TARGET, sandbox
                    .getSandboxPath()
                    + SLASH + file.getPath(), true);
        }
    }

    /**
     * Appends XML info about maximum CPU time, memory limits and so on.
     * 
     * @param posixAppl
     * @param sd
     */
    private void addLimitsInfo(POSIXApplicationType posixAppl, SoftwareDescription sd) {
        Map<String, Object> attrs = sd.getAttributes();

        if (attrs.get(MAX_MEMORY_ATTRIBUTE) != null) {
            BigDecimal v = new BigDecimal(attrs.get(MAX_MEMORY_ATTRIBUTE).toString());
            LimitsType memLimit = posixAppl.addNewMemoryLimit();
            memLimit.setBigDecimalValue(v);
        }
        if (attrs.get(MAX_CPU_TIME_ATTRIBUTE) != null) {
            BigDecimal v = new BigDecimal(attrs.get(MAX_CPU_TIME_ATTRIBUTE).toString());
            LimitsType timeLimit = posixAppl.addNewCPUTimeLimit();
            timeLimit.setBigDecimalValue(v);
        }
    }
}
