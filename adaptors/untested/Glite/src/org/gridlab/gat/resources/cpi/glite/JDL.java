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
//
////////////////////////////////////////////////////////////////////

package org.gridlab.gat.resources.cpi.glite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class JDL {
	private String filename;
	private long fileID;
	private String content;
	private File file;
	private FileWriter fileWriter;
	private String VirtualOrganisation;
	private String Executable;
	private ArrayList InputFiles;
	private ArrayList OutputSrcFiles;
	private ArrayList OutputDestFiles;
	private ArrayList Requirements;
	private String StdInputFile;
	private ArrayList Environments;
	private ArrayList Arguments;

	public JDL(String filename, long fileID) {
		VirtualOrganisation = "";
		this.filename = filename;
		this.fileID = fileID;
		file = new File(this.filename);
		InputFiles = new ArrayList();
		OutputSrcFiles = new ArrayList();
		OutputDestFiles = new ArrayList();
		Requirements = new ArrayList();
		StdInputFile = null;
		Environments = new ArrayList();
		Arguments = new ArrayList();
		this.addRequirements("other.GlueCEStateStatus == \"Production\"");
	}

	public void setVirtualOrganisation(String VirtualOrganisation) {
		this.VirtualOrganisation = VirtualOrganisation;
	}

	public void setExecutable(String Executable) {
		this.Executable = Executable;
	}

	public void addInputFiles(Map map) throws Exception {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			org.gridlab.gat.io.File file = (org.gridlab.gat.io.File) entry
					.getKey(); // key = src, value = dest
			if (entry.getValue() != null)
				throw new Exception(
						"gLite does not support renaming inputfiles in the sandbox.\n"
								+ "addPreStagedFile(src, dest), dest file will be ignored");
			addInputFile(file.getAbsolutePath());
		}
	}

	public void addRequirements(String requirement) {
		Requirements.add(requirement);
	}

	public void setStdInputFile(String stdInputFile) {
		this.StdInputFile = stdInputFile;
		addInputFile(this.StdInputFile);
	}

	public void addEnviroment(Map environment) {
		Iterator it = environment.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String var_name = (String) entry.getKey();
			String var_value = (String) entry.getValue();
			Environments.add(var_name + "=" + var_value);
		}
	}

	public void setArguments(String[] args) {
		for (int i = 0; i < args.length; i++)
			Arguments.add(args[i]);
	}

	public void addInputFile(String Filename) {
		this.InputFiles.add(Filename);
	}

	public void addOutputFiles(Map map) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (entry.getValue() == null) {
				org.gridlab.gat.io.File file = (org.gridlab.gat.io.File) entry
						.getKey(); // key = src, value = dest
				addOutputFile(file.getName());

			} else { // entry.getValue() != null
				org.gridlab.gat.io.File fileSrc = (org.gridlab.gat.io.File) entry
						.getKey(); // key = src, value = dest
				OutputSrcFiles.add(fileSrc.getName());
				org.gridlab.gat.io.File fileDest = (org.gridlab.gat.io.File) entry
						.getValue();
				OutputDestFiles.add(fileDest.getName());
			}
		}
	}

	public void addOutputFile(String Filename) {
		OutputSrcFiles.add(Filename);
		OutputDestFiles.add(Filename);
	}
	public void addOutputFile(String SrcFilename, String DestFilename ) {
		OutputSrcFiles.add(SrcFilename);
		OutputDestFiles.add(DestFilename);
	}

	public boolean create() {
		try {
			fileWriter = new FileWriter(file);
			createJDLFileContent();
			fileWriter.write(content);
			fileWriter.close();
			return true;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	private void createJDLFileContent() {
		content = new String("// Auto generate JDL File\n");
		content += "Rank = -other.GlueCEStateEstimatedResponseTime;\n";
		if (VirtualOrganisation != "")
			content += "VirtualOrganisation = \"" + VirtualOrganisation
					+ "\";\n";
		content += "Executable = \"" + Executable + "\";\n";
		if (!Arguments.isEmpty()) {
			content += "Arguments = \"";
			for (int i = 0; Arguments.size() - 1 > i; i++) {
				content += (String) Arguments.get(i) + " ";
			}
			content += (String) Arguments.get(Arguments.size() - 1) + "\";\n";
		}
		if (StdInputFile != null)
			content += "StdInput =\"" + StdInputFile + "\";\n";

		content += "StdOutput = \"" + "std_" + fileID + ".out\";\n";
		content += "StdError = \"" + "std_" + fileID + ".err\";\n";
		if (!InputFiles.isEmpty()) {
			content += "InputSandbox = {\n\t";
			for (int i = 0; InputFiles.size() - 1 > i; i++)
				content += "\"file://" + (String) InputFiles.get(i) + "\",\n\t";
			content += "\"file://"
					+ (String) InputFiles.get(InputFiles.size() - 1) + "\"\n";
			content += "};\n";
		}
		if (!OutputSrcFiles.isEmpty()) {
			content += "OutputSandbox = {\n\t";
			for (int i = 0; OutputSrcFiles.size() - 1 > i; i++)
				content += "\"" + (String) OutputSrcFiles.get(i) + "\",\n\t";
			content += "\""
					+ (String) OutputSrcFiles.get(OutputSrcFiles.size() - 1)
					+ "\"";
			content += "\n};\n";
			
			content += "OutputSandboxDestURI = {\n\t";
			for (int i = 0; OutputDestFiles.size() - 1 > i; i++)
				content += "\"" + (String) OutputDestFiles.get(i) + "\",\n\t";
			content += "\""
					+ (String) OutputDestFiles.get(OutputDestFiles.size() - 1)
					+ "\"";
			content += "\n};\n";
		}

		if (!Requirements.isEmpty()) {
			content += "Requirements =\n";
			for (int i = 0; Requirements.size() - 1 > i; i++) {
				content += "\t" + (String) Requirements.get(i) + " &&\n";
			}
			content += "\t"
					+ (String) Requirements.get(Requirements.size() - 1)
					+ ";\n";
		}
		if (!Environments.isEmpty()) {
			content += "Environment = {\n";
			for (int i = 0; Environments.size() - 1 > i; i++) {
				content += "\t\"" + (String) Environments.get(i) + "\",\n";
			}
			content += "\t\""
					+ (String) Environments.get(Environments.size() - 1)
					+ "\"\n};\n";
		}
	}
}