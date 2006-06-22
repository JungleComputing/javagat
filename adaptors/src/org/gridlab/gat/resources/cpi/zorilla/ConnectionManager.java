package org.gridlab.gat.resources.cpi.zorilla;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public final class ConnectionManager implements Runnable {

    private final Logger logger = Logger.getLogger(ConnectionManager.class);

    private final ZorillaJob job;

    private final ServerSocket socket;

    private ArrayList connections = new ArrayList();

    public ConnectionManager(ZorillaJob job, ServerSocket socket) {
        this.job = job;
        this.socket = socket;

        new Thread(this).start();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("error on closing", e);
        }

        synchronized (this) {
            for (int i = 0; i < connections.size(); i++) {
                ((ConnectionHandler) connections.get(i)).close();
            }
            connections.clear();
        }

    }

    public void run() {
        try {

            while (!socket.isClosed()) {

                Socket sock = socket.accept();

                ConnectionHandler handler = new ConnectionHandler(job, sock);

                synchronized (this) {
                    connections.add(handler);
                }

            }

        } catch (IOException e) {
            logger.debug("could not accept new connection", e);
        }
    }
}
