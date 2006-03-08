/*
 * Created on Apr 19, 2004
 */
package org.gridlab.gat.advert;

/**
 * @author rob
 */

/** An interface which is realized by any class which wishes to get advertized in the advert service.
 * At this moment, this interface does not have any user methods, but it is used as a "marker" interface.
 * The marshal method should not be called by users of the GAT.
 */
public interface Advertisable extends java.io.Serializable {
    /** Create a string representation of this object. Used internally in the GAT.
     * 
     * @return a string representation of this object
     */
    public String marshal();
}
