/*
 * Created on Apr 19, 2004
 *
 *  An interface which is realized by any class which wishes to get
 *  advertized. At this moment, this interface does not have any
 * methods, but it is used as a "marker" interface.
 *
 */
package org.gridlab.gat.advert;

/**
 * @author rob
 *  
 */
public interface Advertisable extends java.io.Serializable {
	public String marshal();
}