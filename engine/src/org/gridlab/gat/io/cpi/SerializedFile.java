/*
 * Created on Mar 10, 2005
 */
package org.gridlab.gat.io.cpi;

import org.gridlab.gat.advert.Advertisable;

import java.io.Serializable;

/**
 * @author rob
 *
 */
public class SerializedFile implements Serializable, Advertisable {
    String location;

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
}
