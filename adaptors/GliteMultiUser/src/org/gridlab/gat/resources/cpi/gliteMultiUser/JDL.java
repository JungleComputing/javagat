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

package org.gridlab.gat.resources.cpi.gliteMultiUser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a JDL (Job Description Language) based on a {@link SoftwareDescription} and a {@link ResourceDescription}. 
 * 
 * @author Stefan Bozic
 */
public class JDL {
	
	/** The logger instance */
	private static final Logger LOGGER = LoggerFactory.getLogger(JDL.class);

	/** The JDL string */
	private String jdlString;
	
	/** The JDL id*/
	private long jdlID;
	
	/** The virtual organization*/
	private String virtualOrganisation;
	
	/** The executable */
	private String executable;

	/**
	 * Tree Sets have the advantage of containing an entry at most once. Hence, adding input or output files multiple
	 * times will not lead to exceptions at jobRegister.
	 */
	private SortedSet<String> inputFiles;
	
	/**
	 * Tree Sets have the advantage of containing an entry at most once. Hence, adding input or output files multiple
	 * times will not lead to exceptions at jobRegister.
	 */	
	private SortedSet<String> outputSrcFiles;
	
	/**
	 * Tree Sets have the advantage of containing an entry at most once. Hence, adding input or output files multiple
	 * times will not lead to exceptions at jobRegister.
	 */	
	private SortedSet<String> outputDestFiles;
	
	/** The requirements for this JDL. */
	private List<String> requirements;
	
	/** The stdInputFile for this JDL. */
	private String stdInputFile;
	
	/** The stdOutputFile for this JDL. */
	private String stdOutputFile;
	
	/** The stdErrorFile for this JDL. */
	private String stdErrorFile;
	
	/** The environments for this JDL. */
	private List<String> environments;
	
	/** The arguments for this JDL. */
	private List<String> arguments;
	
	/** The attributes for this JDL. */
	private Map<String, Object> attributes;

	/**
	 * Constructor
	 * 
	 * @param jdlID the id for the jdl
	 * @param swDescription the GAT softwareDesription of the JOB
	 * @param voName the name of the virtual machine
	 * @param rd the resource description
	 * @throws GATObjectCreationException an exception that might occurs
	 */
	public JDL(final long jdlID, final SoftwareDescription swDescription, final String voName,
			final ResourceDescription rd) throws GATObjectCreationException {

		this.jdlID = jdlID;

		inputFiles = new TreeSet<String>();
		outputSrcFiles = new TreeSet<String>();
		outputDestFiles = new TreeSet<String>();
		requirements = new ArrayList<String>();
		environments = new ArrayList<String>();
		arguments = new ArrayList<String>();
		requirements.add("other.GlueCEStateStatus == \"Production\"");

		// ... add content
		this.executable = (swDescription.getExecutable().toString());

		if (voName != null) {
			this.virtualOrganisation = voName;
		}

		if (swDescription.getStdin() == null) {
			this.addInputFiles(swDescription.getPreStaged());
		} else {
			this.stdInputFile = swDescription.getStdin().getAbsolutePath();
		}

		this.addOutputFiles(swDescription.getPostStaged());

		if (swDescription.getStdout() != null) {
			this.stdOutputFile = swDescription.getStdout().getName();
			addOutputFile(this.stdOutputFile);
		}

		if (swDescription.getStderr() != null) {
			this.stdErrorFile = swDescription.getStderr().getName();
			addOutputFile(this.stdErrorFile);
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

	/**
	 * Sets a {@link Map} of input file definitions to the JDL
	 * @param map the {@link Map} to set
	 */
	private void addInputFiles(Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map) {

		for (java.io.File file : map.keySet()) {
			addInputFile(file.getAbsolutePath());

			if (map.get(file) != null) {
				LOGGER.warn("gLite does not support renaming inputfiles in the sandbox.\n"
						+ "addPreStagedFile(src, dest), dest file will be ignored");
			}
		}
	}

	// private void setStdInputFile(File stdInputFile) {
	// this.stdInputFile = stdInputFile.getAbsolutePath();
	// addInputFile(this.stdInputFile);
	// }


	/**
	 * Adds a {@link Map} of environment definitions to the JDL 
	 * 
	 * @param environment the values to set
	 */
	private void addEnviroment(Map<String, Object> environment) {
		for (String varName : environment.keySet()) {
			String varValue = (String) environment.get(varName);
			this.environments.add(varName + "=" + varValue);
		}
	}

	/**
	 * Sets an array of arguments to the JDL.
	 * @param args the array to set
	 */
	private void setArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			arguments.add(args[i]);
		}
	}

	/**
	 * Adds an filename to the input file list of the JDL.
	 * 
	 * @param filename the filename to add
	 */	
	private void addInputFile(String filename) {
		this.inputFiles.add(filename);
	}

	/**
	 * Adds a {@link List} of output file definitions.
	 * The maps contain the source as key and the destination as value.
	 *  
	 * @param map the {@link Map} to add
	 * @throws GATObjectCreationException an exception that might occurs
	 */
	private void addOutputFiles(Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map)
			throws GATObjectCreationException {

		for (java.io.File file : map.keySet()) {
			if (map.get(file) == null) {
				addOutputFile(file.getName());
			} else { // copy poststaged file somewhere after staging out
				org.gridlab.gat.io.File fileDest = map.get(file);
				outputSrcFiles.add(file.getName());

				java.io.File parentFile = new java.io.File(fileDest.getParent());
				if (!parentFile.exists()) {
					throw new GATObjectCreationException("The folder for the poststaged file does not exist!");
				}

				outputDestFiles.add(fileDest.getName());
			}
		}
	}

	/**
	 * Adds an filename to the output file list of the JDL. The source and destination directory are the same.
	 * 
	 * @param filename the filename to add
	 */
	private void addOutputFile(String filename) {
		outputSrcFiles.add(filename);
		outputDestFiles.add(filename);
	}

	// private void addOutputFile(String srcFilename, String destFilename ) {
	// outputSrcFiles.add(srcFilename);
	// outputDestFiles.add(destFilename);
	// }

	/**
	 * Write the JDL content to a file on the harddisk. This can be useful for debugging purposes
	 * 
	 * @return Boolean that indicates success.
	 */
	public boolean saveToDisk() {
		boolean success = false;

		try {
			String jdlFileName = "gatjob_" + this.jdlID + ".jdl";
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

	/**
	 * Creates the content of the JDL.
	 * 
	 * @return a {@link String} representation of the JDL
	 */
	private String createJDLFileContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("// Auto generated JDL File\n");
		builder.append("Rank = -other.GlueCEStateEstimatedResponseTime;\n");

		if (!(virtualOrganisation == null)) {
			builder.append("VirtualOrganisation = \"").append(virtualOrganisation).append("\";\n");
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

		builder.append("StdOutput = \"").append(this.stdOutputFile).append("\";\n");
		builder.append("StdError = \"").append(this.stdErrorFile).append("\";\n");

		if (!inputFiles.isEmpty()) {
			builder.append("InputSandbox = {\n\t");
			String lastInputFile = inputFiles.last();
			String filteredPath;
			
			for (String inputFile : inputFiles.headSet(lastInputFile)) {
				filteredPath = inputFile.replace('\\', '/');
				builder.append("\"file://").append(filteredPath).append("\",\n\t");
			}
		
			filteredPath = lastInputFile.replace('\\', '/');
			builder.append("\"file://").append(filteredPath).append("\"\n").append("};\n");
		}

		if (!outputSrcFiles.isEmpty()) {

			builder.append("OutputSandbox = {\n\t");
			String lastSrcFile = outputSrcFiles.last();

			for (String srcFile : outputSrcFiles.headSet(lastSrcFile)) {
				builder.append("\"").append(srcFile).append("\",\n\t");
			}

			builder.append("\"").append(lastSrcFile).append("\"").append("\n};\n");

			builder.append("OutputSandboxDestURI = {\n\t");
			String lastDestFile = outputDestFiles.last();
			String filteredPath;
			
			for (String destFile : outputDestFiles.headSet(lastDestFile)) {
				filteredPath = destFile.replace('\\', '/');
				builder.append("\"").append(filteredPath).append("\",\n\t");
			}

			filteredPath = lastDestFile.replace('\\', '/');
			builder.append("\"").append(lastDestFile).append("\"").append("\n};\n");
		}

		if (!attributes.isEmpty()) {
			processAttributes(builder);
		}

		if (!requirements.isEmpty()) {
			builder.append("Requirements =\n");
			for (int i = 0; requirements.size() - 1 > i; i++) {
				builder.append("\t").append(requirements.get(i)).append(" &&\n");
			}
			builder.append("\t").append(requirements.get(requirements.size() - 1)).append(";\n");
		}
		if (!environments.isEmpty()) {
			builder.append("Environment = {\n");
			for (int i = 0; environments.size() - 1 > i; i++) {
				builder.append("\t\"").append(environments.get(i)).append("\",\n");
			}
			builder.append("\t\"").append(environments.get(environments.size() - 1)).append("\"\n};\n");
		}

		return builder.toString();

	}

	/**
	 * Map the "GAT requirements" to the glue schema and add the requirements to the gLiteJobDescription
	 * 
	 * @param map the parameter {@link Map}
	 */
	@SuppressWarnings("unchecked")
	private void processResourceDescription(Map<String, Object> map) {

		for (String resDesc : map.keySet()) {

			if (resDesc.equals("os.name")) {
				requirements.add("other.GlueHostOperatingSystemName == \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals("os.release")) {
				requirements.add("other.GlueHostOperatingSystemRelease ==  \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals("os.version")) {
				requirements.add("other.GlueHostOperatingSystemVersion ==  \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals("os.type")) {
				requirements.add("other.GlueHostProcessorModel ==  \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals("cpu.type")) {
				requirements.add("other.GlueHostProcessorModel == \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals("machine.type")) {
				requirements.add("other.GlueHostProcessorModel ==  \"" + map.get(resDesc) + "\"");
			} else if (resDesc.equals(GliteConstants.RESOURCE_MACHINE_NODE)) {
				// add requirements for multiple sites
				if (map.get(resDesc) instanceof List) {
					addCEListToRequirements((List<Object>) map.get(resDesc));
				} else {
					requirements.add("other.GlueCEUniqueID == \"" + map.get(resDesc) + "\"");
				}
			} else if (resDesc.equals("cpu.speed")) {
				// gat: float & GHz
				// gLite: int & Mhz
				float gatspeed = new Float((String) map.get(resDesc));
				int gLiteSpeed = (int) (gatspeed * 1000);
				requirements.add("other.GlueHostProcessorClockSpeed >= " + gLiteSpeed);
			} else if (resDesc.equals("memory.size")) {
				// gat: float & GB
				// gLite: int & MB
				float gatRAM = (Float) map.get(resDesc);
				int gLiteRAM = (int) (gatRAM * 1024);
				requirements.add("other.GlueHostMainMemoryRAMSize >= " + gLiteRAM);
			} else if (resDesc.equals("disk.size")) {
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

	/**
	 * Adds a {@link List} of CE to the requirements.
	 * 
	 * @param ceList the {@link List} to add
	 */
	private void addCEListToRequirements(List<Object> ceList) {
		StringBuilder ceReqsBuilder = new StringBuilder();

		for (Object ceElem : ceList.subList(0, ceList.size() - 2)) {
			ceReqsBuilder.append("(other.GlueCEUniqueID == \"").append(ceElem).append("\") || ");
		}

		Object lastElem = ceList.get(ceList.size() - 1);
		ceReqsBuilder.append("(other.GlueCEUniqueID == \"").append(lastElem).append("\")");

		this.requirements.add(ceReqsBuilder.toString());

	}

	/**
	 * Some attributes that are recommended for processing in javagat plus some additional specifically for glite will
	 * become interpreted here
	 * 
	 * @param builder The StringBuilder to which the generated JDL String fragment will be appended
	 */
	@SuppressWarnings("unchecked")
	private void processAttributes(StringBuilder builder) {
		for (String attKey : this.attributes.keySet()) {
			if (SoftwareDescription.TIME_MAX.equalsIgnoreCase(attKey)) {
				// object is supposed to be an instance of String or Long
				Object maxTime = attributes.get(attKey);
				requirements.add("other.GlueCEPolicyMaxWallClockTime ==  \"" + maxTime + "\"");
			} else if (SoftwareDescription.WALLTIME_MAX.equalsIgnoreCase(attKey)) {
				Object maxTime = attributes.get(attKey);
				requirements.add("other.GlueCEPolicyMaxWallClockTime ==  \"" + maxTime + "\"");
			} else if (SoftwareDescription.CPUTIME_MAX.equalsIgnoreCase(attKey)) {
				Object maxTime = attributes.get(attKey);
				requirements.add("other.GlueCEPolicyMaxCPUTime == \"" + maxTime + "\"");
			} else if (SoftwareDescription.PROJECT.equalsIgnoreCase(attKey)) {
				String project = (String) attributes.get(attKey);
				builder.append("HLRLocation = \"").append(project).append("\";\n");
			} else if (SoftwareDescription.MEMORY_MIN.equalsIgnoreCase(attKey)) {
				Object minMemory = attributes.get(attKey);
				requirements.add("other.GlueHostMainMemoryRAMSize >= " + minMemory);
			} else if (SoftwareDescription.MEMORY_MAX.equalsIgnoreCase(attKey)) {
				Object maxMemory = attributes.get(attKey);
				requirements.add("other.GlueHostMainMemoryRAMSize <= " + maxMemory);
			} else if ("glite.retrycount".equalsIgnoreCase(attKey)) {
				Object retryCount = attributes.get(attKey);
				builder.append("RetryCount = ").append(retryCount).append(";\n");
			} else if ("glite.DataRequirements.InputData".equalsIgnoreCase(attKey)) {
				List<String> inputElements = (ArrayList<String>) attributes.get(attKey);

				builder.append("DataRequirements = {\n\t[\n").append("\t\tDataCatalogType = \"RLS\";\n").append(
						"\t\tInputData = {");

				Iterator<String> it = inputElements.listIterator();

				while (it.hasNext()) {
					builder.append('\"').append(it.next()).append('\"');

					if (it.hasNext()) {
						builder.append(",\n");
					}
				}

				builder.append("};\n\t]\n};\n");

				builder.append("DataAccessProtocol = {\"https\", \"gsiftp\"};\n");
			} else {
				throw new UnsupportedOperationException("Attribute " + attKey + " not supported in gLite-Adaptor");
			}

		}

	}

	/**
	 * Returns a String representation of this instance
	 * 
	 * @return a String representation of this instance
	 */
	public String getJdlString() {
		return jdlString;
	}

	/**
	 * Returns the id of this JDL
	 * 
	 * @return the id of this JDL
	 */
	public long getJdlID() {
		return jdlID;
	}
}
