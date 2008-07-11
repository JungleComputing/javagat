package org.gridlab.gat.resources;

import java.util.HashMap;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

/**
 * 
 * 
 * @author rkemp
 *
 */
public class WrapperSoftwareDescription extends JavaSoftwareDescription {

    /**
     * 
     */
    private static final long serialVersionUID = -8066795059038763966L;

    private URI gatLocation;

    // create the proper softwareDescription

    public WrapperSoftwareDescription() {
        super.setJavaMain("org.gridlab.gat.resources.cpi.Wrapper");
    }

    /**
     * Don't use this method. Use of this method will be ignored, since the java
     * main is always "org.gridlab.gat.resources.cpi.Wrapper".
     */
    public void setJavaMain(String main) {
        // ignore
    }

    public void setGATLocation(URI gatLocation) {
        this.gatLocation = gatLocation;
    }

    public URI getGATLocation() {
        return gatLocation;
    }

    /**
     * Don't use this method
     */
    public void setJavaClassPath(String classpath) {

    }

    /**
     * Doesn't support the attributes 'sandbox.enable', 'sandbox.use.root'
     */
    public String getJavaClassPath() {
        if (gatLocation == null) {
            // ok, we'll take the local $GAT_LOCATION/lib and stage it into the
            // sandbox in a
            // directory called lib, the classpath should contain everything
            // like lib/*
            return ".:lib/*";
        } else {
            if (gatLocation.hasAbsolutePath()) {
                return ".:" + gatLocation.getPath() + "/lib/*";
            } else {
                return ".:" + "../" + gatLocation.getPath() + "/lib/*";
            }
        }

    }

    public Map<File, File> getPreStaged() {
        if (gatLocation == null) {
            Map<File, File> result = new HashMap<File, File>();
            try {
                result.put(GAT.createFile(System.getenv("GAT_LOCATION")
                        + java.io.File.separator + "lib"), GAT.createFile("."));
                result.put(GAT.createFile(System.getenv("GAT_LOCATION")
                        + java.io.File.separator + "log4j.properties"), GAT
                        .createFile("log4j.properties"));
            } catch (GATObjectCreationException e) {
                return null;
            }
            return result;
        } else {
            return null;
        }
    }

    public Map<String, String> getJavaSystemProperties() {
        Map<String, String> result = new HashMap<String, String>();
        if (gatLocation == null) {
            result.put("log4j.configuration", "file:log4j.properties");
            result.put("gat.adaptor.path", "lib/adaptors");
        } else {
            if (gatLocation.hasAbsolutePath()) {
                result.put("log4j.configuration", "file:"
                        + gatLocation.getPath() + "/log4j.properties");
                result.put("gat.adaptor.path", gatLocation.getPath()
                        + "/lib/adaptors");

            } else {
                result.put("log4j.configuration", "file:../"
                        + gatLocation.getPath() + "/log4j.properties");
                result.put("gat.adaptor.path", "../" + gatLocation.getPath()
                        + "/lib/adaptors");
            }
        }
        return result;
    }

    public Map<String, Object> getEnvironment() {
        Map<String, Object> result = new HashMap<String, Object>();
        if (gatLocation == null) {
            result.put("GAT_LOCATION", ".");
        } else {
            if (gatLocation.hasAbsolutePath()) {
                result.put("GAT_LOCATION", gatLocation.getPath());
            } else {
                result.put("GAT_LOCATION", "../" + gatLocation.getPath());
            }
        }
        return result;
    }
}
