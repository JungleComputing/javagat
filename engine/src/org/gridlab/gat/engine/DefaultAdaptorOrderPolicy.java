/*
 * Created on Aug 1, 2005 by rob
 */
package org.gridlab.gat.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author rob
 */
public class DefaultAdaptorOrderPolicy implements AdaptorOrderPolicy {
    public DefaultAdaptorOrderPolicy() {
        // do nothing
    }

    /**
     * 
     */
    public void order(Map<String, List<Adaptor>> adaptors) {
        Set<String> adaptorTypes = adaptors.keySet();
        for (String adaptorType : adaptorTypes) {
            if (adaptorType.equalsIgnoreCase("file")) {
                orderFileList(adaptors.get(adaptorType));
            } else if (adaptorType.equalsIgnoreCase("fileinputstream")) {
                orderFileInputStreamList(adaptors.get(adaptorType));
            } else if (adaptorType.equalsIgnoreCase("fileoutputstream")) {
                orderFileOutputStreamList(adaptors.get(adaptorType));
            } else if (adaptorType.equalsIgnoreCase("randomaccessfile")) {
            } else if (adaptorType.equalsIgnoreCase("logicalfile")) {
            } else if (adaptorType.equalsIgnoreCase("endpoint")) {
            } else if (adaptorType.equalsIgnoreCase("resourcebroker")) {
                orderResourceList(adaptors.get(adaptorType));
            } else if (adaptorType.equalsIgnoreCase("advertservice")) {
            } else if (adaptorType.equalsIgnoreCase("monitorable")) {
            } else {
                System.err
                        .println("WARNING, unknown GAT type in DefaultAdaptorOrderPolicy");
            }
        }
    }

    private void orderFileList(List<Adaptor> l) {
        int pos = 0;
        pos = placeAdaptor(pos, "local", "file", l);
        pos = placeAdaptor(pos, "gridftp", "file", l);
        pos = placeAdaptor(pos, "sftpganymed", "file", l);
        pos = placeAdaptor(pos, "commandlinessh", "file", l);
        pos = placeAdaptor(pos, "sftpnew", "file", l);
        pos = placeAdaptor(pos, "sshtrilead", "file", l);
        pos = placeAdaptor(pos, "sftp", "file", l);
        // rest in random order
    }

    private void orderFileInputStreamList(List<Adaptor> l) {
        int pos = 0;
        pos = placeAdaptor(pos, "local", "fileinputstream", l);
        pos = placeAdaptor(pos, "gridftp", "fileinputstream", l);
        pos = placeAdaptor(pos, "sftpnew", "fileinputstream", l);
        pos = placeAdaptor(pos, "sftp", "fileinputstream", l);
        pos = placeAdaptor(pos, "copying", "fileinputstream", l);
        // rest in random order
    }

    private void orderFileOutputStreamList(List<Adaptor> l) {
        int pos = 0;
        pos = placeAdaptor(pos, "local", "fileoutputstream", l);
        pos = placeAdaptor(pos, "gridftp", "fileoutputstream", l);
        pos = placeAdaptor(pos, "sftp", "fileoutputstream", l);
        // rest in random order
    }

    private void orderResourceList(List<Adaptor> l) {
        int pos = 0;
        pos = placeAdaptor(pos, "local", "resourcebroker", l);
        pos = placeAdaptor(pos, "globus", "resourcebroker", l);
        pos = placeAdaptor(pos, "wsgt4new", "resourcebroker", l);
        pos = placeAdaptor(pos, "sshtrilead", "resourcebroker", l);
        // rest in random order
    }

    private int placeAdaptor(int position, String adaptorName, String cpiName,
            List<Adaptor> l) {
        int currentPosition = l.indexOf(getAdaptor(adaptorName, cpiName, l));
        if (currentPosition < 0) {
            return position;
        }
        l.add(position, l.remove(currentPosition));
        return ++position;
    }

    private Adaptor getAdaptor(String shortName, String cpiName,
            List<Adaptor> adaptors) {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getShortAdaptorClassName().equalsIgnoreCase(
                    shortName + cpiName + "Adaptor")) {
                return adaptor;
            }
        }
        return null;
    }
}
