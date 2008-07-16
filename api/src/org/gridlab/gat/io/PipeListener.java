/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io;

/**
 * This interface allows instances of classes which implement this interface to
 * process GATPipes produced by listening on GATEndpoints.
 * 
 * @author rob
 */
public interface PipeListener {
    /**
     * An instance of a class implementing this interface receives GATPipes
     * produced by listening on Endpoints through calls to this callback method.
     * 
     * @param pipe
     *                the new Pipe.
     */
    public void processPipe(Pipe pipe);
}
