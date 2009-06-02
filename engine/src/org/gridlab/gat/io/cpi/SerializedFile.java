/*
 * Created on Mar 10, 2005
 */
package org.gridlab.gat.io.cpi;

import java.io.Serializable;

import org.gridlab.gat.advert.Advertisable;

/**
 * @author rob
 * 
 */
@SuppressWarnings("serial")
public class SerializedFile implements Serializable, Advertisable {
    String location;

    // we need this constructor for castor
    public SerializedFile() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String marshal() {
        throw new Error("Should not be called");
    }

    public String toString() {
        return location;
    }
}
