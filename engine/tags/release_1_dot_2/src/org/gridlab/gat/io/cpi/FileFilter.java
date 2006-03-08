/*
 * Created on Jan 7, 2005
 */
package org.gridlab.gat.io.cpi;

import org.gridlab.gat.io.File;

/**
 * @author rob
 */
public interface FileFilter {
    /**
     * Tests whether or not the specified abstract pathname should be included
     * in a pathname list.
     * 
     * @param pathname
     *            The abstract pathname to be tested
     * @return true if and only if pathname should be included
     */
    public boolean accept(File pathname);
}
