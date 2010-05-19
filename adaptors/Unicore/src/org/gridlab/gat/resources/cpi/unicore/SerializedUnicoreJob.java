package org.gridlab.gat.resources.cpi.unicore;

import java.util.Map;

import org.gridlab.gat.io.File;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.gridlab.gat.resources.cpi.SerializedJob;

public class SerializedUnicoreJob extends SerializedJob {

    private static final long serialVersionUID = 1L;
    
    private String stdout;
    
    private String stderr;
    
    private String[] toStageOut;
    
    private String[] stagedOut;

    public SerializedUnicoreJob() {
    }

    public SerializedUnicoreJob(String classname, JobDescription jobDescription, Sandbox sandbox,
            String jobId, long submissiontime, long starttime, long stoptime,
            SoftwareDescription sd) {
        super(classname, jobDescription, sandbox, jobId, submissiontime, starttime,
                stoptime);
        
        // Get what is needed from the software description to deal with
        // poststaging.
        File s = sd.getStdout();
        if (s != null) {
            stdout = s.getAbsolutePath();
        }
        s = sd.getStderr();
        if (s != null) {
            stderr = s.getAbsolutePath();
        }
        Map<File, File> postStaged = sd.getPostStaged();
        
        if (postStaged!=null) {
            int sz = postStaged.keySet().size();
            toStageOut = new String[sz];
            stagedOut = new String[sz];
            int index = 0;
            for (java.io.File srcFile : postStaged.keySet()) {
                File destFile = postStaged.get(srcFile);
                toStageOut[index] = srcFile.getName();
                
                /**
                 * destFile = srcFile if no destFile has been given.
                 */
                
                if (destFile == null ) {
                	stagedOut[index] = srcFile.getName();
                }
                else {
                	stagedOut[index] = realPath(destFile.toGATURI());
                }
                
                /**
                 * some logs here...
                 */
                
                logger.debug("srcFile.getName " + srcFile.getName().toString());
                if (destFile!=null) {
                	logger.debug("destFile.toString() " + destFile.toString());
                    logger.debug("realpath(destFile.toURI() " + realPath(destFile.toGATURI()));
                }

                index++;                  
            }
        }
    }

	/**
	 * The method String windowsPath extracts the full windows path of an URI. If it is no
	 * windows path, the path name retrieved by the getPath method of the class URI is returned.
	 *  
	 * @param uri
	 * @return path name as a string
	 */

	public synchronized static String realPath(org.gridlab.gat.URI uri) {
		
		String path=null;
		String stringURI = null;
			
		/**
		 * assume that the URI has the protocol definition ended with '://'
		 */
		
		stringURI = uri.toString();
		int pathBegin = stringURI.indexOf("://");
		
		if (pathBegin != -1 ) {
			path = stringURI.substring(pathBegin+3);
		}
		else {
			path=stringURI;
		}
		
		if (isWindowsPath(path)) {
			return path;
		}
		else {
			return uri.getPath();
		}
		
		
	}
	
	/**
	 * The method String windowsPath extracts the full windows path of an URI, which
	 * is represented as a String.
	 *  
	 * @param uriPath
	 * @return path name as a string
	 */

	public synchronized static String realPath(String uriPath) {
		
		String path=null;
			
		/**
		 * assume that the URI has the protocol definition ended with '://'
		 */
		
		int pathBegin = uriPath.indexOf("://");
		
		if (pathBegin != -1 ) {
			path = uriPath.substring(pathBegin+3);
		}
		else {
			path=uriPath;
		}
		
		if (isWindowsPath(path)) {
			return path;
		}
		else {
			return uriPath;
		}
		
		
	}
	
    private static boolean isWindowsPath(String s) {
    	String subStr = s.substring(0, 2);
    	
    	if (subStr.matches("[a-zA-Z][:]")) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public String[] getToStageOut() {
        return toStageOut;
    }

    public void setToStageOut(String[] toStageOut) {
        this.toStageOut = toStageOut;
    }

    public String[] getStagedOut() {
        return stagedOut;
    }

    public void setStagedOut(String[] stagedOut) {
        this.stagedOut = stagedOut;
    }
}
