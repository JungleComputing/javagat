package org.gridlab.gat.resources.cpi.zorilla;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.resources.SoftwareDescription;

public final class ConnectionHandler implements Runnable {

    Logger logger = Logger.getLogger(ConnectionHandler.class);

    private final ZorillaJob job;

    private final Socket socket;

    private final DataInputStream in;

    private final DataOutputStream out;

    public ConnectionHandler(ZorillaJob job, Socket socket) throws IOException {
        this.job = job;
        this.socket = socket;

        logger.debug("new connection handler from: " + socket.getInetAddress());

        in = new DataInputStream(new BufferedInputStream(socket
            .getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket
            .getOutputStream()));

        new Thread(this).start();
    }

    private File findFile(Map map, String path) {
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            File key = (File) entry.getKey();
            File value = (File) entry.getValue();

            // value is "virtual" path in zorilla

            URI virtualURI = value.toURI();

            if (path.equalsIgnoreCase(virtualURI.getPath())) {
                return key;
            }
        }
        return null;
    }

    private File findFile(String path) throws IOException {

        SoftwareDescription sd = job.description.getSoftwareDescription();

        if (path.equalsIgnoreCase("<stdin>")) {
            return sd.getStdin();
        }

        if (path.equalsIgnoreCase("<stdout>")) {
            return sd.getStdout();
        }

        if (path.equalsIgnoreCase("<stderr>")) {
            return sd.getStderr();
        }

        File result = findFile(sd.getPreStaged(), path);

        if (result == null) {
            result = findFile(sd.getPostStaged(), path);
        }

        if (result == null) {
            throw new IOException("cannot find file with path: " + path);
        }
        return result;
    }

    private void readFile() throws IOException, GATInvocationException,
        GATObjectCreationException {
        String jobID = in.readUTF();
        if (job.getJobID() != null && !jobID.equalsIgnoreCase(job.getJobID())) {
            throw new IOException("unknown job: " + jobID);
        }

        String path = in.readUTF();
        long offset = in.readLong();
        long length = in.readLong();

        logger.debug("recieved read file request. job = " + jobID + ", path = "
            + path + ", offset = " + offset + ", length = " + length);

        File file = findFile(path);

        logger.debug("reading from file: " + file);

        if (offset >= file.length()) {
            out.writeInt(ClientProtocol.STATUS_OK);
            out.writeUTF("OK");
            out.writeLong(-1); // End of file
            out.flush();
            return;
        }
        
        if (length == -1) {
            length = file.length() - offset;
        }

        if (file.length() < (offset + length)) {
            length = file.length() - offset;
        }

        out.writeInt(ClientProtocol.STATUS_OK);
        out.writeUTF("OK");
        out.writeLong(length);
        out.flush();

        logger.debug("sending " + length + " bytes");

        // FIXME: what context?
        FileInputStream fileIn = GAT.createFileInputStream(new GATContext(),
            file.toURI());
        fileIn.skip(offset);

        byte[] buffer = new byte[32 * 1024];

        while (length > 0) {
            int maxRead = (int) Math.min(buffer.length, length);

            int read = fileIn.read(buffer, 0, maxRead);

            out.write(buffer, 0, read);

            length -= read;
        }
        fileIn.close();
        out.flush();
        
        logger.debug("done handling read request");

    }

    private void writeFile() throws IOException, GATObjectCreationException {
        String jobID = in.readUTF();
        if (!jobID.equalsIgnoreCase(job.getJobID())) {
            throw new IOException("unknown job: " + jobID);
        }

        String path = in.readUTF();
        long offset = in.readLong();
        long length = in.readLong();

        File file = findFile(path);

        // FIXME: what context?
        RandomAccessFile fileOut = GAT.createRandomAccessFile(new GATContext(),
            file, "w");
        fileOut.seek(offset);

        byte[] buffer = new byte[32 * 1024];

        while (length > 0) {
            int read = (int) Math.min(buffer.length, length);

            read = in.read(buffer, 0, read);

            fileOut.write(buffer, 0, read);

            length -= read;
        }
        fileOut.close();

        out.writeInt(ClientProtocol.STATUS_OK);
        out.writeUTF("OK");
        out.flush();
    }

    private void updateJobInfo() throws IOException {
        String jobID = in.readUTF();
        if (!jobID.equalsIgnoreCase(job.getJobID())) {
            throw new IOException("unknown job: " + jobID);
        }

        String executable = in.readUTF();
        Map attributes = ClientProtocol.readStringMap(in);
        Map status = ClientProtocol.readStringMap(in);
        int phase = in.readInt();

        out.writeInt(ClientProtocol.STATUS_OK);
        out.writeUTF("OK");
        out.flush();

        job.setState(executable, attributes, status, phase);
    }

    public void run() {
        logger.debug("received new callback connection");

        try {
            int clientProtoVersion = in.readInt();
            int authentication = in.readInt();

            if (clientProtoVersion != 1) {
                out.writeInt(ClientProtocol.STATUS_ERROR);
                out.writeUTF("only protocol version 1 is supported");
                close();
                throw new IOException("illegal protocol version");
            }

            if (authentication != ClientProtocol.AUTHENTICATION_NONE) {
                out.writeInt(ClientProtocol.STATUS_ERROR);
                out.writeUTF("authentication not supported");
                close();
                throw new IOException("illegal authentication");
            }

            logger.debug("send back connection init ack");

            out.writeInt(ClientProtocol.STATUS_OK);
            out.writeUTF("OK");
            out.flush();

            logger.debug("waiting for request");

            while (!socket.isClosed()) {
                logger.debug("receiving node request message");

                int opcode = in.readInt();

                logger.debug("received opcode: " + opcode);

                switch (opcode) {
                case ClientProtocol.CLOSE_CONNECTION:
                    //close connection received
                    socket.close();
                    break;
                case ClientProtocol.READ_FILE:
                    readFile();
                    break;
                case ClientProtocol.WRITE_FILE:
                    writeFile();
                    break;
                case ClientProtocol.JOB_INFO_UPDATE:
                    updateJobInfo();
                    break;
                default:
                    throw new IOException("unknown opcode: " + opcode);
                }
            }

        } catch (EOFException e) {
            logger.debug("socket closed on receiving/handling request", e);
        } catch (Exception e) {
            try {
                out.writeInt(ClientProtocol.STATUS_ERROR);
                out.writeUTF("error on creating job: " + e);
                out.flush();
            } catch (Exception e2) {
                // IGNORE
            }
            logger.debug("error on handling request", e);
        }
        close();
        logger.debug("user connection handler " + this + " exits");
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("could not close socket", e);
        }
    }

}
