////////////////////////////////////////////////////////////////////
//
// JDL.java
// 
// Contributor(s):
// Dec/2007 - Andreas Havenstein 
//     for Max Planck Institute for Gravitational Physics
//     (Albert Einstein Institute) 
//     Astrophysical Relativity / eScience
// 
// Jul/2008 - Thomas Zangerl (code cleanup, added attributes processing)
//		for Distributed and Parallel Systems Research Group (DPS)
//		University of Innsbruck
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.SoftwareResourceDescription;

public class JDL_Basic extends AbstractJDL{

    private String virtualOrganisation;
    private String executable;

    /**
     * Tree Sets have the advantage of containing an entry at most once. Hence,
     * adding input or output files multiple times will not lead to exceptions
     * at jobRegister.
     */
    private SortedSet<String> inputFiles;
    private LinkedHashMap<String,String> outputFiles;
    private List<String> requirements;
    private String stdInputFile;
    private String stdOutputFile;
    private String stdErrorFile;
    private List<String> environments;
    private List<String> arguments;
    private Map<String, Object> attributes;

    public JDL_Basic(final SoftwareDescription swDescription,
            final String voName, final ResourceDescription rd)
            throws GATObjectCreationException {

        inputFiles = new TreeSet<String>();
        outputFiles = new LinkedHashMap<String,String>();
        requirements = new ArrayList<String>();
        environments = new ArrayList<String>();
        arguments = new ArrayList<String>();
        requirements.add("other.GlueCEStateStatus == \"Production\"");

        // ... add content
        this.executable = (swDescription.getExecutable().toString());

        if (voName != null) {
            this.virtualOrganisation = voName;
        }

        if (swDescription.getStdin() != null) {
            this.stdInputFile = swDescription.getStdin().getAbsolutePath();
        }
        this.addInputFiles(swDescription.getPreStaged());
        this.addOutputFiles(swDescription.getPostStaged());

        if (swDescription.getStdout() != null) {
            this.stdOutputFile = swDescription.getStdout().getName();
            outputFiles.put(this.stdOutputFile, null);
        }

        if (swDescription.getStderr() != null) {
            this.stdErrorFile = swDescription.getStderr().getName();
            outputFiles.put(this.stdErrorFile, null);
        }

        if (swDescription.getEnvironment() == null) {
            if (swDescription.getArguments() != null) {
                this.setArguments(swDescription.getArguments());
            }
        } else {
            this.addEnviroment(swDescription.getEnvironment());
        }

        if (swDescription.getAttributes() != null) {
            this.attributes = swDescription.getAttributes();
        }

        // map GAT resource description to gLite glue schema and add it to
        // gLiteJobDescription
        // the resource description can also be null
        if (rd != null) {
            processResourceDescription(rd.getDescription());
        }

        this.jdlString = createJDLFileContent();
    }

    private void addInputFiles(
            Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map) {

        for (java.io.File file : map.keySet()) {
        	//Test if it is a file that comes from another job.
        	if(file.getName().toLowerCase().startsWith("root.nodes.") 
        			|| file.getName().toLowerCase().startsWith("root.inputsandbox")){
        		addInputFile(file.getName());
        	}else{
        		if (map.get(file) != null) {
        			addInputFile("\""+map.get(file).getPath()+"\"");
        		}else{
        			addInputFile("\""+file.getAbsolutePath()+"\"");
        		}
        	}
// But JavaGAT does ;-)
//            if (map.get(file) != null) {
//                LOGGER
//                        .warn("gLite does not support renaming inputfiles in the sandbox.\n"
//                                + "addPreStagedFile(src, dest), dest file will be ignored");
//            }
        }
    }

    // private void setStdInputFile(File stdInputFile) 

    private void addEnviroment(Map<String, Object> environment) {
        for (String varName : environment.keySet()) {
            String varValue = (String) environment.get(varName);
            this.environments.add(varName + "=" + varValue);
        }
    }

    private void setArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            arguments.add(args[i]);
        }
    }

    private void addInputFile(String filename) {
        this.inputFiles.add(filename);
    }

    private void addOutputFiles(
            Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map)
            throws GATObjectCreationException {

        for (java.io.File file : map.keySet()) {
            if (map.get(file) == null) {
                outputFiles.put(file.getPath(),null);
            } else { // copy poststaged file somewhere after staging out
                org.gridlab.gat.io.File fileDest = map.get(file);
                java.io.File parentFile = new java.io.File(fileDest.getParent());
                if (!parentFile.exists()) {
                    throw new GATObjectCreationException(
                            "The folder for the poststaged file does not exist!");
                }
                outputFiles.put(file.getPath(), fileDest.getName());
            }
        }
    }

//    private void addOutputFile(String filename) {
//        outputSrcFiles.add(filename);
//        outputDestFiles.add(filename);
//    }

    private String createJDLFileContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("[\n");
        builder.append("// Auto generated JDL File\n");
        builder.append("Rank = -other.GlueCEStateEstimatedResponseTime;\n");

        if (!(virtualOrganisation == null)) {
            builder.append("VirtualOrganisation = \"").append(
                    virtualOrganisation).append("\";\n");
        }

        builder.append("Executable = \"").append(executable).append("\";\n");
        if (!arguments.isEmpty()) {
            builder.append("Arguments = \"");
            for (int i = 0; arguments.size() - 1 > i; i++) {
                builder.append(arguments.get(i)).append(" ");
            }
            builder.append(arguments.get(arguments.size() - 1)).append("\";\n");
        }

        if (stdInputFile != null) {
            builder.append("StdInput =\"").append(stdInputFile).append("\";\n");
        }

        builder.append("StdOutput = \"").append(this.stdOutputFile).append(
                "\";\n");
        builder.append("StdError = \"").append(this.stdErrorFile).append(
                "\";\n");

        if (!inputFiles.isEmpty()) {
            builder.append("InputSandbox = {\n\t");
            String lastInputFile = inputFiles.last();

            for (String inputFile : inputFiles.headSet(lastInputFile)) {
                builder.append(inputFile).append(",\n\t");
            }
            builder.append(lastInputFile).append("\n");
            builder.append("};\n");
        }

        if (!outputFiles.isEmpty()) {
        	boolean outputSandboxDestURINeeded = false;
        	StringBuilder outputSandboxBuilder = new StringBuilder("OutputSandbox = {\n\t");
        	StringBuilder outputSandboxDestURIBuilder = new StringBuilder("OutputSandboxDestURI = {\n\t");
        	for (Iterator<Entry<String, String>> iterator = outputFiles.entrySet().iterator(); iterator.hasNext();) {
        		Entry<String, String> entry = iterator.next();
        		outputSandboxBuilder.append("\"");
        		outputSandboxDestURIBuilder.append("\"");
        		outputSandboxBuilder.append(entry.getKey());
        		if(entry.getValue() != null){
        			outputSandboxDestURINeeded = true;
        			outputSandboxDestURIBuilder.append(entry.getValue());
        		}else{
        			outputSandboxDestURIBuilder.append(entry.getKey());
        		}
        		outputSandboxBuilder.append("\"");
        		outputSandboxDestURIBuilder.append("\"");
        		if(iterator.hasNext()){
        			outputSandboxBuilder.append(",\n\t");
        			outputSandboxDestURIBuilder.append(",\n\t");
        		}else{
        			outputSandboxBuilder.append("\n};\n");
        			outputSandboxDestURIBuilder.append("\n};\n");
        		}
			}
        	
            builder.append(outputSandboxBuilder.toString());
            if(outputSandboxDestURINeeded){
            	builder.append(outputSandboxDestURIBuilder.toString());
            }
        }

        if (!attributes.isEmpty()) {
            processAttributes(builder);
        }

        if (!requirements.isEmpty()) {
            builder.append("Requirements =\n");
            for (int i = 0; requirements.size() - 1 > i; i++) {
                builder.append("\t").append(requirements.get(i))
                        .append(" &&\n");
            }
            builder.append("\t").append(
                    requirements.get(requirements.size() - 1)).append(";\n");
        }
        if (!environments.isEmpty()) {
            builder.append("Environment = {\n");
            for (int i = 0; environments.size() - 1 > i; i++) {
                builder.append("\t\"").append(environments.get(i)).append(
                        "\",\n");
            }
            builder.append("\t\"").append(
                    environments.get(environments.size() - 1)).append(
                    "\"\n};\n");
        }
        builder.append("];");
        return builder.toString();

    }

    // Map the "GAT requirements" to the glue schema and add the requirements to
    // the gLiteJobDescription
    @SuppressWarnings("unchecked")
    private void processResourceDescription(Map<String, Object> map) {

        for (String resDesc : map.keySet()) {

            if (resDesc.equals(SoftwareResourceDescription.OS_NAME)) {
                requirements.add("other.GlueHostOperatingSystemName == \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals(SoftwareResourceDescription.OS_RELEASE)) {
                requirements.add("other.GlueHostOperatingSystemRelease ==  \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals(SoftwareResourceDescription.OS_VERSION)) {
                requirements.add("other.GlueHostOperatingSystemVersion ==  \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals(SoftwareResourceDescription.OS_TYPE)) {
                requirements.add("other.GlueHostProcessorModel ==  \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals(HardwareResourceDescription.CPU_TYPE)) {
                requirements.add("other.GlueHostProcessorModel == \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals("machine.type")) {
                requirements.add("other.GlueHostProcessorModel ==  \""
                        + map.get(resDesc) + "\"");
            } else if (resDesc.equals(HardwareResourceDescription.MACHINE_NODE)) {
                // add requirements for multiple sites
                if (map.get(resDesc) instanceof List) {
                    addCEListToRequirements((List<Object>) map.get(resDesc));
                } else {
                    requirements.add("other.GlueCEUniqueID == \""
                            + map.get(resDesc) + "\"");
                }
            } else if (resDesc.equals(HardwareResourceDescription.CPU_SPEED)) {
                // gat: float & GHz
                // gLite: int & Mhz
                float gatspeed = new Float((String) map.get(resDesc));
                int gLiteSpeed = (int) (gatspeed * 1000);
                requirements.add("other.GlueHostProcessorClockSpeed >= "
                        + gLiteSpeed);
            } else if (resDesc.equals(HardwareResourceDescription.MEMORY_SIZE)) {
                // gat: float & GB
                // gLite: int & MB
                float gatRAM = (Float) map.get(resDesc);
                int gLiteRAM = (int) (gatRAM * 1024);
                requirements.add("other.GlueHostMainMemoryRAMSize >= "
                        + gLiteRAM);
            } else if (resDesc.equals(HardwareResourceDescription.DISK_SIZE)) {
                float gatDS = new Float((String) map.get(resDesc));
                int gLiteDS = (int) gatDS;
                requirements.add("other.GlueSESizeFree >= " + gLiteDS); // or
                                                                        // other.GlueSEUsedOnlineSize
                // the user may specify custom resource requirements which will
                // be considered in the GLUE matching
            } else if (resDesc.equals("glite.other")) {
                String extraReq = (String) map.get(resDesc);
                requirements.add(extraReq);
            }
        }

    }

    private void addCEListToRequirements(List<Object> ceList) {
        StringBuilder ceReqsBuilder = new StringBuilder();

        for (Object ceElem : ceList.subList(0, ceList.size() - 2)) {
            ceReqsBuilder.append("(other.GlueCEUniqueID == \"").append(ceElem)
                    .append("\") || ");
        }

        Object lastElem = ceList.get(ceList.size() - 1);
        ceReqsBuilder.append("(other.GlueCEUniqueID == \"").append(lastElem)
                .append("\")");

        this.requirements.add(ceReqsBuilder.toString());

    }

    /**
     * Some attributes that are recommended for processing in javagat plus some
     * additional specifically for glite will become interpreted here
     * 
     * @param builder
     *            The StringBuilder to which the generated JDL String fragment
     *            will be appended
     */
    @SuppressWarnings("unchecked")
    private void processAttributes(StringBuilder builder) {
        for (String attKey : this.attributes.keySet()) {
            if (SoftwareDescription.TIME_MAX.equalsIgnoreCase(attKey)) {
                // object is supposed to be an instance of String or Long
                Object maxTime = attributes.get(attKey);
                requirements.add("other.GlueCEPolicyMaxWallClockTime ==  \""
                        + maxTime + "\"");
            } else if (SoftwareDescription.WALLTIME_MAX.equalsIgnoreCase(attKey)) {
                Object maxTime = attributes.get(attKey);
                requirements.add("other.GlueCEPolicyMaxWallClockTime ==  \""
                        + maxTime + "\"");
            } else if (SoftwareDescription.CPUTIME_MAX.equalsIgnoreCase(attKey)) {
                Object maxTime = attributes.get(attKey);
                requirements.add("other.GlueCEPolicyMaxCPUTime == \"" + maxTime
                        + "\"");
            } else if (SoftwareDescription.PROJECT.equalsIgnoreCase(attKey)) {
                String project = (String) attributes.get(attKey);
                builder.append("HLRLocation = \"").append(project).append(
                        "\";\n");
            } else if (SoftwareDescription.MEMORY_MIN.equalsIgnoreCase(attKey)) {
                Object minMemory = attributes.get(attKey);
                requirements.add("other.GlueHostMainMemoryRAMSize >= "
                        + minMemory);
            } else if (SoftwareDescription.MEMORY_MAX.equalsIgnoreCase(attKey)) {
                Object maxMemory = attributes.get(attKey);
                requirements.add("other.GlueHostMainMemoryRAMSize <= "
                        + maxMemory);
            } else if ("glite.retrycount".equalsIgnoreCase(attKey)) {
                Object retryCount = attributes.get(attKey);
                builder.append("RetryCount = ").append(retryCount)
                        .append(";\n");
            } else if ("glite.DataRequirements.InputData"
                    .equalsIgnoreCase(attKey)) {
                List<String> inputElements = (ArrayList<String>) attributes
                        .get(attKey);

                builder.append("DataRequirements = {\n\t[\n").append(
                        "\t\tDataCatalogType = \"RLS\";\n").append(
                        "\t\tInputData = {");

                Iterator<String> it = inputElements.listIterator();

                while (it.hasNext()) {
                    builder.append('\"').append(it.next()).append('\"');

                    if (it.hasNext()) {
                        builder.append(",\n");
                    }
                }

                builder.append("};\n\t]\n};\n");

                builder
                        .append("DataAccessProtocol = {\"https\", \"gsiftp\"};\n");
            } else {
                throw new UnsupportedOperationException("Attribute " + attKey
                        + " not supported in gLite-Adaptor");
            }

        }

    }

}
