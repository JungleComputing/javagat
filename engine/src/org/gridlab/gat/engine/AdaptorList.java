/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.util.ArrayList;

/** Contains a list of adaptors that are loaded for a single specific gat object type (e.g. File)
 * @author rob
 */
public class AdaptorList {
    /** The fully qualified name of the class the adaptors in this set. */
    String cpi;

    /** The api class all adaptors in this set implement. */
    Class cpiClass;

    /** A list of the adaptors. The type of the elements is "Adaptor" */
    ArrayList adaptors;

    /**
     * @param cpiClass
     *            The api class all adaptors in this set implement.
     */
    AdaptorList(Class cpiClass) {
        this.cpi = cpiClass.getName();
        this.cpiClass = cpiClass;
        adaptors = new ArrayList();
    }

    void addAdaptor(Adaptor a) {
        adaptors.add(a);
    }

    void addAdaptor(int pos, Adaptor a) {
        adaptors.add(pos, a);
    }

    int size() {
        return adaptors.size();
    }

    Adaptor get(int pos) {
        return (Adaptor) adaptors.get(pos);
    }

    Adaptor remove(int pos) {
        return (Adaptor) adaptors.remove(pos);
    }

    int getPos(String adaptorName) {
        for (int i = 0; i < size(); i++) {
            Adaptor a = get(i);

            if (a.adaptorName.equals(adaptorName)) {
                return i;
            }
        }

        return -1;
    }

    int placeAdaptor(int destPos, String name) {
        int pos = getPos(name);

        if (pos != -1) {
            addAdaptor(destPos, remove(pos));
            destPos++;
        }

        return destPos;
    }
}
