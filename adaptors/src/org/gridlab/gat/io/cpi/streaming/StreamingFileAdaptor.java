/*
 * Created on Sep 12, 2007 by roelof
 */
package org.gridlab.gat.io.cpi.streaming;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.FileCpi;

public class StreamingFileAdaptor extends FileCpi {
    
	public StreamingFileAdaptor(GATContext gatContext,
        Preferences preferences, URI location) {
        super(gatContext, preferences, location);
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#copy(org.gridlab.gat.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        try {
        	FileInputStream in = GAT.createFileInputStream(gatContext, location);
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader inBuffer = new BufferedReader(reader);
        	File dstFile = GAT.createFile(gatContext, dest);
            dstFile.setWritable(true);
            FileOutputStream out = GAT.createFileOutputStream(gatContext, dstFile);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            BufferedWriter outBuffer = new BufferedWriter(writer);
            while(true) {
                String line = inBuffer.readLine();
                if (line == null) {
                	break;
                }
            	outBuffer.write(line);
            	outBuffer.newLine();
            }
            in.close();
            outBuffer.flush();
            out.close();
        } catch (Exception e) {
            throw new GATInvocationException("streaming copy", e);
        } 
    }
}
