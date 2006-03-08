/*
 * Created on Jan 7, 2005
 */
package org.gridlab.gat.io.cpi;

import org.gridlab.gat.io.File;

/**
 * @author rob
 */
public interface FilenameFilter {
    /**
     * Tests if a specified file should be included in a file list.
     * 
     * @param dir
     *            the directory in which the file was found.
     * @param name
     *            the name of the file.
     * @return true if and only if the name should be included in the file list;
     *         false otherwise.
     */
    public boolean accept(File dir, String name);
}
