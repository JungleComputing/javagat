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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class JDL {
	protected static final Logger logger = Logger.getLogger(JDL.class);
	
	private final long fileID;
	private String content;
	private File file;
	private String virtualOrganisation;
	private String executable;
	private List<String> inputFiles;
	private List<String> outputSrcFiles;
	private List<String> outputDestFiles;
	private List<String> requirements;
	private String stdInputFile;
	private String stdOutputFile;
	private String stdErrorFile;
	private List<String> environments;
	private List<String> arguments;

	private Map<String, Object> attributes;

	public JDL(String filename, long fileID) {
		virtualOrganisation = "";
		this.fileID = fileID;
		file = new File(filename);
		
		String deleteOnExitStr = System.getProperty("glite.deleteJDL");

		if ("true".equalsIgnoreCase(deleteOnExitStr)) {
			file.deleteOnExit();
		}

		
		inputFiles = new ArrayList<String>();
		outputSrcFiles = new ArrayList<String>();
		outputDestFiles = new ArrayList<String>();
		requirements = new ArrayList<String>();
		environments = new ArrayList<String>();
		arguments = new ArrayList<String>();
		requirements.add("other.GlueCEStateStatus == \"Production\"");
	}

	public void setVirtualOrganisation(String virtualOrganisation) {
		this.virtualOrganisation = virtualOrganisation;
	}

	public void setExecutable(String executable) {
		this.executable = executable;
	}

	public void addInputFiles(Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map) {
		
		for (org.gridlab.gat.io.File file : map.keySet()) {
			addInputFile(file.getAbsolutePath());
			
			if (map.get(file) != null) {
				logger.warn("gLite does not support renaming inputfiles in the sandbox.\n"
								+ "addPreStagedFile(src, dest), dest file will be ignored");
			}
		}
	}

	public void addRequirements(String requirement) {
		requirements.add(requirement);
	}

	public void setStdInputFile(File stdInputFile) {
		this.stdInputFile = stdInputFile.getAbsolutePath();
		addInputFile(this.stdInputFile);
	}

	public void addEnviroment(Map<String, Object> environment) {
		for (String varName : environment.keySet()) {
			String varValue = (String) environment.get(varName);
			this.environments.add(varName + "=" + varValue);
		}
	}

	public void setArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			arguments.add(args[i]);
		}
	}

	public void addInputFile(String filename) {
		this.inputFiles.add(filename);
	}

	public void addOutputFiles(Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> map) {
		
		for (org.gridlab.gat.io.File file : map.keySet()) {
			if (map.get(file) == null) {
				addOutputFile(file.getName());
			} else {
				org.gridlab.gat.io.File fileDest = map.get(file);
				outputSrcFiles.add(file.getName());
				outputDestFiles.add(fileDest.getName());
			}
		}
	}

	public void addOutputFile(String filename) {
		outputSrcFiles.add(filename);
		outputDestFiles.add(filename);
	}
	public void addOutputFile(String srcFilename, String destFilename ) {
		outputSrcFiles.add(srcFilename);
		outputDestFiles.add(destFilename);
	}
	
	public void setStdOutputFile(File stdout) {
		this.stdOutputFile = stdout.getName();
		this.addOutputFile(this.stdOutputFile, this.stdOutputFile);
	}
	
	public void setStdErrorFile(File stderr) {
		this.stdErrorFile = stderr.getName();
		this.addOutputFile(this.stdErrorFile, this.stdErrorFile);
	}
	

	public boolean create() {
		boolean success = false;
		
		try {
			FileWriter fileWriter = new FileWriter(file);
			createJDLFileContent();
			fileWriter.write(content);
			fileWriter.close();
			success = true;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return success;
	}
	
	

	private void createJDLFileContent() {
		content = "// Auto generated JDL File\n";
		content += "Rank = -other.GlueCEStateEstimatedResponseTime;\n";
		
		if (!virtualOrganisation.isEmpty()) {
			content += "VirtualOrganisation = \"" + virtualOrganisation
					+ "\";\n";
		}
		
		content += "Executable = \"" + executable + "\";\n";
		if (!arguments.isEmpty()) {
			content += "Arguments = \"";
			for (int i = 0; arguments.size() - 1 > i; i++) {
				content += (String) arguments.get(i) + " ";
			}
			content += (String) arguments.get(arguments.size() - 1) + "\";\n";
		}
		
		if (stdInputFile != null) {
			content += "StdInput =\"" + stdInputFile + "\";\n";
		}

		content += "StdOutput = \"" + this.stdOutputFile + "\";\n";
		content += "StdError = \"" + this.stdErrorFile + "\";\n";
		
		if (!inputFiles.isEmpty()) {
			content += "InputSandbox = {\n\t";
			for (int i = 0; inputFiles.size() - 1 > i; i++) {
				content += "\"file://" + (String) inputFiles.get(i) + "\",\n\t";
			}
			
			content += "\"file://"
					+ (String) inputFiles.get(inputFiles.size() - 1) + "\"\n";
			content += "};\n";
		}
		
		if (!outputSrcFiles.isEmpty()) {
			content += "OutputSandbox = {\n\t";
			
			for (int i = 0; outputSrcFiles.size() - 1 > i; i++) {
				content += "\"" + (String) outputSrcFiles.get(i) + "\",\n\t";
			}
			
			content += "\""
					+ (String) outputSrcFiles.get(outputSrcFiles.size() - 1)
					+ "\"";
			content += "\n};\n";
			
			content += "OutputSandboxDestURI = {\n\t";
			for (int i = 0; outputDestFiles.size() - 1 > i; i++) {
				content += "\"" + (String) outputDestFiles.get(i) + "\",\n\t";
			}
			
			content += "\""
					+ (String) outputDestFiles.get(outputDestFiles.size() - 1)
					+ "\"";
			content += "\n};\n";
		}
		
		if (!attributes.isEmpty()) {
			processAttributes();
		}

		if (!requirements.isEmpty()) {
			content += "Requirements =\n";
			for (int i = 0; requirements.size() - 1 > i; i++) {
				content += "\t" + (String) requirements.get(i) + " &&\n";
			}
			content += "\t"
					+ (String) requirements.get(requirements.size() - 1)
					+ ";\n";
		}
		if (!environments.isEmpty()) {
			content += "Environment = {\n";
			for (int i = 0; environments.size() - 1 > i; i++) {
				content += "\t\"" + (String) environments.get(i) + "\",\n";
			}
			content += "\t\""
					+ (String) environments.get(environments.size() - 1)
					+ "\"\n};\n";
		}
		
	}
	

	private void processAttributes() {
		for (String attKey : this.attributes.keySet()) {
			if ("time.max".equalsIgnoreCase(attKey)) {
				// object is supposed to be an instance of String or Long
				Object maxTime = attributes.get(attKey);
				this.addRequirements("other.GlueCEPolicyMaxWallClockTime ==  \"" + maxTime + "\"");
			} else if ("walltime.max".equalsIgnoreCase(attKey)) {
				Object maxTime = attributes.get(attKey);
				this.addRequirements("other.GlueCEPolicyMaxWallClockTime ==  \"" + maxTime + "\"");
			} else if ("cputime.max".equalsIgnoreCase(attKey)) {
				Object maxTime = attributes.get(attKey);
				this.addRequirements("other.GlueCEPolicyMaxCPUTime == \"" + maxTime + "\"");
			} else if ("project".equalsIgnoreCase(attKey)) {
				String project = (String) attributes.get(attKey);
				this.content += "HLRLocation = \"" + project + "\";\n";
			} else if ("memory.min".equalsIgnoreCase(attKey)) {
				Object minMemory = attributes.get(attKey);
				this.addRequirements("other.GlueHostMainMemoryRAMSize >= " + minMemory);
			} else if ("memory.max".equalsIgnoreCase(attKey)) {
				Object maxMemory = attributes.get(attKey);
				this.addRequirements("other.GlueHostMainMemoryRAMSize <= " + maxMemory);
			} else if ("glite.retrycount".equalsIgnoreCase(attKey)) {
				Object retryCount = attributes.get(attKey);
				this.content += "RetryCount = " + retryCount + ";\n";
			} else {
				throw new UnsupportedOperationException("Attribute " + attKey + " not supported in gLite-Adaptor");
			}
			
		}
		
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
		
	}
}