/* The only change made to this file is a small fix in the open method.
 * The original version did not use a timeout, causing JavaGAT to hang in some cases.
 * --Rob
 * 
 */

/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.ftp.vanilla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;

/**
 * <p>
 * Represents FTP Protocol Interpreter. Encapsulates
 * control channel communication.
 *
 * </p>
 */
public class FTPControlChannel extends BasicClientControlChannel {

	private static Logger logger =
		Logger.getLogger(FTPControlChannel.class.getName());

	public static final String CRLF = "\r\n";

	// used in blocking waitForReply()
	private static final int WAIT_FOREVER = -1;

	protected Socket socket;
	//input stream
	protected BufferedReader ftpIn;
	//raw stream underlying ftpIn
	protected InputStream rawFtpIn;
	//output stream
	protected OutputStream ftpOut;
	protected String host;
	protected int port;
	//true if connection has already been opened.
	protected boolean hasBeenOpened = false;

	public FTPControlChannel(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/** 
	Using this constructor, you can initialize an instance that does not 
	talk directly to the socket. If you use this constructor using streams
	that belong to an active connection, there's no need to call open() afterwards.
	**/
	public FTPControlChannel(InputStream in, OutputStream out) {
		setInputStream(in);
		setOutputStream(out);
	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}
    /**
     * This abstract method was not implemented, and with my
     * cog-jglobus-1.2-060418.jar it could not be compiled.
     */
    public void abortTransfer() {
    }

	protected BufferedReader getBufferedReader() {
		return ftpIn;
	}

	protected OutputStream getOutputStream() {
		return ftpOut;
	}

	// not intended to be public. you can set streams in the constructor.
	protected void setInputStream(InputStream in) {
		rawFtpIn = in;
		ftpIn = new BufferedReader(new InputStreamReader(rawFtpIn));
	}
	protected void setOutputStream(OutputStream out) {
		ftpOut = out;
	}

	/**
	 * opens the connection and returns after it is ready for communication.
	 * Before returning, it intercepts the initial server reply(-ies),
	 * and not positive, throws UnexpectedReplyCodeException.
	 * After returning, there should be no more queued replies on the line.
	 *
	 * Here's the sequence for connection establishment (rfc959):
	 * <PRE>
	 *	   120
	 *         220
	 *	   220
	 *     421
	 *</PRE>
	 * @throws IOException on I/O error
	 * @throws ServerException on negative or faulty server reply 
	 **/
	public void open() throws IOException, ServerException {

		if (hasBeenOpened()) {
			throw new IOException("Attempt to open an already opened connection");
		}

		//depending on constructor used, we may already have streams
		if (!haveStreams()) {
			logger.debug("opening control channel to " + host + " : " + port);
//			socket = new Socket(host, port);

                       // Use a timeout here. --Rob
                       socket = new Socket();
                       socket.connect(new InetSocketAddress(host, port), 5 * 1000); // 5 seconds timeout
            
			setInputStream(socket.getInputStream());
			setOutputStream(socket.getOutputStream());
		}

		readInitialReplies();

		hasBeenOpened = true;
	}

	//intercepts the initial replies 
	//(that the server sends after opening control ch.)
	protected void readInitialReplies() throws IOException, ServerException {
		Reply reply = null;

		try {

			reply = read();

		} catch (FTPReplyParseException rpe) {
			throw ServerException.embedFTPReplyParseException(
				rpe,
				"Received faulty initial reply");
		}

		if (Reply.isPositivePreliminary(reply)) {
			try {
				reply = read();
			} catch (FTPReplyParseException rpe) {
				throw ServerException.embedFTPReplyParseException(
					rpe,
					"Received faulty second reply");
			}
		}

		if (!Reply.isPositiveCompletion(reply)) {
			close();
			throw ServerException.embedUnexpectedReplyCodeException(
				new UnexpectedReplyCodeException(reply),
				"Server refused connection.");
		}
	}

	/**
	 * Closes the control channel
	 */
	public void close() throws IOException {
		logger.debug("ftp socket closed");
		if (ftpIn != null)
			ftpIn.close();
		if (ftpOut != null)
			ftpOut.close();
		if (socket != null)
			socket.close();

		hasBeenOpened = false;
	}

	/**
	   Block until one of the conditions are true:
	   <ol>
	   <li> a reply is available in the control channel,
	   <li> timeout (maxWait) expired
	   <li> aborted flag changes to true.
	   </ol>
	   If maxWait == WAIT_FOREVER, never timeout
	   and only check conditions (1) and (3).
	   @param maxWait timeout in miliseconds
	   @param ioDelay frequency of polling the control channel
	   and checking the conditions
	   @param aborted flag indicating wait aborted.
	 **/
	public void waitFor(Flag aborted, int ioDelay, int maxWait)
		throws ServerException, IOException, InterruptedException {
		int i = 0;
		logger.debug("checking input stream");
		while (!ftpIn.ready()) {
			if (aborted.flag)
				throw new InterruptedException();
			logger.debug("slept " + i);
			Thread.sleep(ioDelay);
			i += ioDelay;
			if (maxWait != WAIT_FOREVER && i >= maxWait) {
				logger.debug("timeout");
				throw new ServerException(ServerException.REPLY_TIMEOUT);
			}
		}

		/*
		  A bug in the server causes it to append \0 to each reply.
		  As the result, we receive this \0 before the next reply.
		  The code below handles this case.
		
		 */
		ftpIn.mark(2);
		int c = ftpIn.read();
		if (c != 0) {
			ftpIn.reset();
			// if we're here, the server is healthy
			// and the reply is waiting in the buffer
			return;
		}

		// if we're here, we deal with the buggy server.
		// we discarded the \0 and now resume wait.

		logger.debug("Server sent \\0; resume wait");
		while (!ftpIn.ready()) {
			if (aborted.flag)
				throw new InterruptedException();
			logger.debug("sleep " + i);
			Thread.sleep(ioDelay);
			i += ioDelay;
			if (maxWait != WAIT_FOREVER && i >= maxWait) {
				logger.debug("timeout");
				throw new ServerException(ServerException.REPLY_TIMEOUT);
			}
		}
	}

	/**
	 * Block until a reply is available in the control channel.
	 * @return the first unread reply from the control channel.
	 * @throws IOException on I/O error
	 * @throws FTPReplyParseException on malformatted server reply
	 **/
	public Reply read()
		throws ServerException, IOException, FTPReplyParseException {
		Reply reply = new Reply(ftpIn);
		if (logger.isInfoEnabled()) {
			logger.info("Control channel received: " + reply);
		}
		return reply;
	}

	/**
	 * Sends the command over the control channel.
	 * Do not wait for reply.
	 * @throws java.io.IOException on I/O error
	 * @param cmd FTP command
	 */
	public void write(Command cmd)
		throws IOException, IllegalArgumentException {

		if (cmd == null) {
			throw new IllegalArgumentException("null argument: cmd");
		}
		if (logger.isInfoEnabled()) {
			logger.info("Control channel sending: " + cmd);
		}
		writeStr(cmd.toString());
	}

	/**
	   Write the command to the control channel,
	   block until reply arrives and return the reply. 
	   Before calling this method make sure that no old replies are
	   waiting on the control channel. Otherwise the reply returned
	   may not be the reply to this command.
	 * @throws java.io.IOException on I/O error
	 * @throws FTPReplyParseException on bad reply format
	 * @param cmd FTP command
	 * @return the first reply that waits in the control channel
	 **/
	public Reply exchange(Command cmd)
		throws ServerException, IOException, FTPReplyParseException {
		// send the command
		write(cmd);
		// get the reply
		return read();
	}

	/**
	   Write the command to the control channel,
	   block until reply arrives and check if the command
	   completed successfully (reply code 200). 
	   If so, return the reply, otherwise throw exception.
	   Before calling this method make sure that no old replies are
	   waiting on the control channel. Otherwise the reply returned
	   may not be the reply to this command.
	 * @throws java.io.IOException on I/O error
	 * @throws FTPReplyParseException on bad reply format
	 * @throws UnexpectedReplyCodeException if reply is not a positive
	 completion reply (code 200)
	 * @param cmd FTP command
	 * @return the first reply that waits in the control channel
	 **/
	public Reply execute(Command cmd)
		throws
			ServerException,
			IOException,
			FTPReplyParseException,
			UnexpectedReplyCodeException {

		Reply reply = exchange(cmd);
		// check for positive reply
		if (!Reply.isPositiveCompletion(reply)) {
			throw new UnexpectedReplyCodeException(reply);
		}
		return reply;
	}

	// local utils
	protected void finalize() {
		try {
			close();
		} catch (IOException e) {
		}
	}

	protected void writeln(String msg) throws IOException {
		writeStr(msg + CRLF);
	}

	protected void writeStr(String msg) throws IOException {
		ftpOut.write(msg.getBytes());
		ftpOut.flush();
	}

	protected boolean hasBeenOpened() {
		return hasBeenOpened;
	}

	protected boolean haveStreams() {
		return (ftpIn != null && ftpOut != null);
	}

} // end StandardPI
