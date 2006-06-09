package org.gridlab.gat.resources.cpi.zorilla;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ClientProtocol {

    /**
     * client protocol version
     */
    public static final int VERSION = 1;

    // only authentication so far...
    public static final int AUTHENTICATION_NONE = 0;

    public static final int DEFAULT_PORT = 9843;

    // opcodes for client-to-node commands

    public static final int SUBMIT_JOB = 1;

    public static final int GET_JOB_INFO = 2;

    public static final int SET_JOB_ATTRIBUTES = 3;

    public static final int CANCEL_JOB = 4;

    public static final int GET_JOB_LIST = 5;

    public static final int KILL_NODE = 6;

    public static final int GET_NODE_INFO = 7;

    public static final int SET_NODE_ATTRIBUTES = 8;

    // opcodes for node-to-client commands

    public static final int READ_FILE = 1;

    public static final int WRITE_FILE = 2;

    public static final int JOB_INFO_UPDATE = 3;

    // status codes

    public static final int STATUS_OK = 1;

    public static final int STATUS_DENIED = 2;

    public static final int STATUS_ERROR = 3;

    // phases of a job

    public static final int PHASE_UNKNOWN = 0;

    public static final int PHASE_INITIAL = 1;

    public static final int PHASE_SCHEDULING = 2;

    public static final int PHASE_RUNNING = 3;

    public static final int PHASE_CLOSED = 4;

    public static final int PHASE_COMPLETED = 5;

    public static final int PHASE_CANCELLED = 6;

    public static final int PHASE_ERROR = 7;

    public static void writeStringMap(Map map, DataOutputStream out)
        throws IOException {
        out.writeInt(map.size());

        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            out.writeUTF((String) entry.getKey());
            out.writeUTF((String) entry.getValue());
        }
    }

    public static Map readStringMap(DataInputStream in) throws IOException {
        Map result = new HashMap();

        int nrOfEntries = in.readInt();

        for (int i = 0; i < nrOfEntries; i++) {
            result.put(in.readUTF(), in.readUTF());
        }

        return result;
    }

}
