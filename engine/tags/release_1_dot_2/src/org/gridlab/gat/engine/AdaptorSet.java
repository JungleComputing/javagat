/*
 * Created on Aug 1, 2005 by rob
 */
package org.gridlab.gat.engine;

import java.util.ArrayList;

/** Represents the set of all loaded adaptors for all types.
 * 
 * @author rob
 */
public class AdaptorSet {
    /** Keys are cpiClass names, elements are AdaptorLists. */
    ArrayList adaptorLists = new ArrayList();

    ArrayList adaptorTypes = new ArrayList();

    public int size() {
        return adaptorLists.size();
    }

    AdaptorList getAdaptorList(int pos) {
        return (AdaptorList) adaptorLists.get(pos);
    }

    String getAdaptorTypeName(int pos) {
        return (String) adaptorTypes.get(pos);
    }

    /** add to position pos, shifting all elements >= pos to the right */
    void add(int pos, String adaptorName, AdaptorList adaptorList) {
        adaptorLists.add(pos, adaptorList);
        adaptorTypes.add(pos, adaptorName);
    }

    /** add to the end of the list */
    void add(String adaptorName, AdaptorList adaptorList) {
        adaptorLists.add(adaptorList);
        adaptorTypes.add(adaptorName);
    }

    AdaptorList getAdaptorList(String typeName) {
        for (int i = 0; i < adaptorTypes.size(); i++) {
            String type = (String) adaptorTypes.get(i);
            if (type.equals(typeName)) {
                return (AdaptorList) adaptorLists.get(i);
            }
        }

        return null;
    }

    protected void order() {
        AdaptorOrderPolicy adaptorOrderPolicy;

        String policy = System.getProperty("adaptor.order.policy");
        if (policy != null) {
            Class c;
            try {
                c = Class.forName(policy);
            } catch (ClassNotFoundException e) {
                throw new Error("adaptor policy " + policy + " not found: " + e);
            }
            try {
                adaptorOrderPolicy = (AdaptorOrderPolicy) c.newInstance();
            } catch (Exception e) {
                throw new Error("adaptor policy " + policy
                    + " could not be instantiated: " + e);
            }

            if (GATEngine.VERBOSE) {
                System.err.println("using adaptor ordering policy: " + policy);
            }
        } else {
            adaptorOrderPolicy = new DefaultAdaptorOrderPolicy();

            if (GATEngine.VERBOSE) {
                System.err.println("using default adaptor ordering policy");
            }
        }

        adaptorOrderPolicy.order(this);
    }

    protected void printAdaptorList() {
        System.err.println("------------LOADED ADAPTORS------------");
        for (int i = 0; i < size(); i++) {
            System.err.println("Adaptor type: " + getAdaptorTypeName(i) + ":");
            AdaptorList l = getAdaptorList(i);
            for (int j = 0; j < l.size(); j++) {
                System.err.println("    " + l.get(j));
            }
            System.err.println();
        }

        System.err.println("---------------------------------------");
    }
}
