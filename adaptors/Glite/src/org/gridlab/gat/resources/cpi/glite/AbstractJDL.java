package org.gridlab.gat.resources.cpi.glite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJDL {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJDL.class);
	
	protected String virtualOrganisation;
    protected String jdlString = null;
	
	public final String getJdlString(){
    	return this.jdlString;
    }
    
    /**
     * Write the JDL content to a file on the harddisk. This can be useful for
     * debugging purposes
     * 
     * @return Boolean that indicates success.
     */
    public final boolean saveToDisk() {
        boolean success = false;
        String jdlFileName = "gatjob_" + System.currentTimeMillis() + ".jdl";
        try {
            File file = new File(jdlFileName);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(this.jdlString);
            fileWriter.close();
            success = true;
        } catch (IOException e) {
            LOGGER.info("Unable to save the JDL file in "+jdlFileName,e);
        }

        return success;
    }

}
