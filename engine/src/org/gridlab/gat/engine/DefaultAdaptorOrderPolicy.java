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
        placeAdaptor(pos++, "local", "file", l);
        placeAdaptor(pos++, "gridftp", "file", l);
        placeAdaptor(pos++, "sftpganymed", "file", l);
        placeAdaptor(pos++, "commandlinessh", "file", l);
        placeAdaptor(pos++, "sftpnew", "file", l);
        placeAdaptor(pos++, "ssh", "file", l);
        placeAdaptor(pos++, "sftp", "file", l);
        // rest in random order
    }

    private void orderFileInputStreamList(List<Adaptor> l) {
        int pos = 0;
        placeAdaptor(pos++, "local", "fileinputstream", l);
        placeAdaptor(pos++, "gridftp", "fileinputstream", l);
        placeAdaptor(pos++, "sftpnew", "fileinputstream", l);
        placeAdaptor(pos++, "ssh", "fileinputstream", l);
        placeAdaptor(pos++, "sftp", "fileinputstream", l);
        placeAdaptor(pos++, "copying", "fileinputstream", l);
        // rest in random order
    }

    private void orderFileOutputStreamList(List<Adaptor> l) {
        int pos = 0;
        placeAdaptor(pos++, "local", "fileoutputstream", l);
        placeAdaptor(pos++, "gridftp", "fileoutputstream", l);
        placeAdaptor(pos++, "ssh", "fileoutputstream", l);
        placeAdaptor(pos++, "sftp", "fileoutputstream", l);
        // rest in random order
    }

    private void orderResourceList(List<Adaptor> l) {
        int pos = 0;
        placeAdaptor(pos++, "local", "resourcebroker", l);
        placeAdaptor(pos++, "globus", "resourcebroker", l);
        placeAdaptor(pos++, "wsgt4new", "resourcebroker", l);
        placeAdaptor(pos++, "commandlinessh", "resourcebroker", l);
        placeAdaptor(pos++, "ssh", "resourcebroker", l);
        // rest in random order
    }
    
    private void placeAdaptor(int position, String adaptorName, String cpiName, List<Adaptor> l) {
        int currentPosition = l.indexOf(getAdaptor(adaptorName, cpiName, l));
        l.add(position, l.remove(currentPosition));
        return;
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
