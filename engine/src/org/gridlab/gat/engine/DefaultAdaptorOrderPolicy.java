/*
 * Created on Aug 1, 2005 by rob
 */
package org.gridlab.gat.engine;

import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

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
    public void order(AdaptorSet adaptors) {
        for (int i = 0; i < adaptors.size(); i++) {
            String type = adaptors.getAdaptorTypeName(i);

            if (type.equals(FileCpi.class.getName())) {
                orderFileList(adaptors.getAdaptorList(i));
            } else if (type.equals(FileInputStreamCpi.class.getName())) {
                orderFileInputStreamList(adaptors.getAdaptorList(i));
            } else if (type.equals(FileOutputStreamCpi.class.getName())) {
                orderFileOutputStreamList(adaptors.getAdaptorList(i));
            } else if (type.equals(RandomAccessFileCpi.class.getName())) {
            } else if (type.equals(LogicalFileCpi.class.getName())) {
            } else if (type.equals(EndpointCpi.class.getName())) {
            } else if (type.equals(ResourceBrokerCpi.class.getName())) {
                orderResourceList(adaptors.getAdaptorList(i));
            } else if (type.equals(AdvertServiceCpi.class.getName())) {
            } else if (type.equals(MonitorableCpi.class.getName())) {
            } else {
                // unknown type
                System.err
                    .println("WARNING, unknown GAT type in DefaultAdaptorOrderPolicy");
            }
        }
    }

    protected void orderFileList(AdaptorList l) {
        int insertPos = 0;
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.local.LocalFileAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.globus.GridFTPFileAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.commandlineSsh.CommandlineSshFileAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.sftpnew.SftpNewFileAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.ssh.SshFileAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.sftp.SftpFileAdaptor");

        // rest in random order
    }

    protected void orderFileInputStreamList(AdaptorList l) {
        int insertPos = 0;
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.local.LocalFileInputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.globus.GridFTPFileInputStreamAdaptor");
        insertPos = l
            .placeAdaptor(insertPos,
                "org.gridlab.gat.io.cpi.copyingFileInputStream.CopyingFileInputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.sftpnew.SftpNewFileInputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.ssh.SshFileInputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.sftp.SftpFileInputStreamAdaptor");

        // rest in random order
    }

    protected void orderFileOutputStreamList(AdaptorList l) {
        int insertPos = 0;
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.local.LocalFileOutputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.globus.GridFTPFileOutputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.ssh.SshFileOutputStreamAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.io.cpi.sftp.SftpFileOutputStreamAdaptor");

        // rest in random order
    }

    protected void orderResourceList(AdaptorList l) {
        int insertPos = 0;
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.resources.cpi.local.LocalResourceBrokerAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.resources.cpi.globus.GlobusResourceBrokerAdaptor");
        insertPos = l.placeAdaptor(insertPos,
            "org.gridlab.gat.resources.cpi.ssh.SshResourceBrokerAdaptor");

        // rest in random order
    }
}
