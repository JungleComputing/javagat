/*
 * Created on Apr 19, 2004
 */
package org.gridlab.gat.advert;

/**
 * An interface which is realized by any class which wishes to get advertised in
 * the advert service. At this moment, this interface does not have any user
 * methods, but it is used as a "marker" interface. The marshal method should
 * not be called by users of the GAT.
 * 
 * @author rob
 */
public interface Advertisable extends java.io.Serializable {
    /**
     * Create a {@link String} representation of this object. <b>This method
     * should not be used, it is used internally in the GAT.</b>
     * 
     * @return a {@link String} representation of this object
     */
    public String marshal();
}
