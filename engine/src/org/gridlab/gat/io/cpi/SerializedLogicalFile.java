/*
 * Created on Mar 10, 2005
 */
package org.gridlab.gat.io.cpi;

import java.io.Serializable;
import java.util.Vector;

import org.gridlab.gat.advert.Advertisable;

/**
 * @author rob
 * 
 */
@SuppressWarnings("serial")
public class SerializedLogicalFile implements Serializable, Advertisable {
    protected String name;

    protected int mode;

    /**
     * Files in the LogicalFile. elements are URI Strings.
     */
    protected Vector<String> files;

    public SerializedLogicalFile() {
    }

    public Vector<String> getFiles() {
        return files;
    }

    public void setFiles(Vector<String> files) {
        this.files = files;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String marshal() {
        throw new Error("Should not be called");
    }

    public String toString() {
        return name;
    }
}
