/*
 * Created on Apr 20, 2004
 */
package org.gridlab.gat.io;

import java.io.Serializable;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * An Endpoint represents an endpoint of a Pipe. An Endpoint can be created, and
 * listened to, and connected to.
 * 
 * An Endpoint represents an end of a byte stream. Depending on how a Endpoint
 * gets created, it can be listened to or connected to. In both cases, the
 * endpoint returns a Pipe. Hence, a Endpoint acts in fact as a Pipe factory:
 * multiple Pipes can be created from it by repeatedly listening for incoming
 * connections. The behaviour is similar to listening on a BSD socket.
 * 
 * Endpoints obtained from the AdvertService cannot be listened to.
 * 
 * Pipes created from endpoints continue to live after the Endpoint instance is
 * destroyed.
 * 
 * @author rob
 */
public interface Endpoint extends Monitorable, Advertisable, Serializable {
    /**
     * Connect to the Endpoint.
     * 
     * When a GATEndpoint is obtained from an Advert Directory, it can be used
     * to create a Pipe connected to the advertising application, by calling
     * connect on the Endpoint instance.
     * 
     * @return a new Pipe connected to the Endpoint.
     * @throws GATInvocationException
     *                 if no connection could be made.
     */
    public Pipe connect() throws GATInvocationException;

    /**
     * Listen for a new connection to the Endpoint.
     * 
     * The creator of an Endpoint can use the Endpoint to create Pipes, which
     * represent incoming connections. This is done by calling listen on the
     * Endpoint instance. This call is synchronous, and blocks until a
     * connection has been made.
     * 
     * @return a new Pipe connected to the Endpoint.
     * @throws GATInvocationException
     *                 if no connection could be made.
     */
    public Pipe listen() throws GATInvocationException;

    /**
     * Listen for a new connection to the Endpoint.
     * 
     * The creator of an Endpoint can use the Endpoint to create Pipes, which
     * represent incoming connections. This is done by calling listen on the
     * Endpoint instance. This call is synchronous, and blocks until a
     * connection has been made or until the specified timeout expires.
     * 
     * @param timeout
     *                the specified timeout in milliseconds
     * @return a new Pipe connected to the Endpoint.
     * @throws GATInvocationException
     *                 if no connection could be made.
     */
    public Pipe listen(int timeout) throws GATInvocationException;

    /**
     * Listen for a new connection to the Endpoint.
     * 
     * The creator of an Endpoint can use the Endpoint to create Pipes, which
     * represent incoming connections. This is done by calling listen on the
     * Endpoint instance. This call is asynchronous, and returns immediately.
     * When a new connection has been made, the pipeListener will be informed. *
     * 
     * @param pipeListener
     *                the listener that will be informed
     * @throws GATInvocationException
     *                 if no connection could be made.
     */
    public void listen(PipeListener pipeListener) throws GATInvocationException;

    /**
     * Listen for a new connection to the Endpoint.
     * 
     * The creator of an Endpoint can use the Endpoint to create Pipes, which
     * represent incoming connections. This is done by calling listen on the
     * Endpoint instance. This call is asynchronous, and returns immediately.
     * When a new connection has been made, the pipeListener will be informed.
     * 
     * @param pipeListener
     *                the listener that will be informed
     * @param timeout
     *                the specified timeout in milliseconds
     * @throws GATInvocationException
     *                 if no connection could be made.
     */
    public void listen(PipeListener pipeListener, int timeout)
            throws GATInvocationException;
}
