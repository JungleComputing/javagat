package org.gridlab.gat.resources.cpi;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class OutputForwarder extends Thread {

	InputStream out;

	StringBuffer sb;

	boolean finished = false; // reached eof or got exception.

	public OutputForwarder(InputStream out) {
		this(out, false);
	}

	/** All output is read from the stream, and appended to result. * */
	public OutputForwarder(InputStream out, boolean logOutput) {
		super("OutputForwarderThread");
		if(logOutput) {
			sb = new StringBuffer();
		}

		InputStream buf;
		try {
			buf = new BufferedInputStream(out);
		} catch (Throwable e) {
			sb.append("error piping output: ");
			sb.append(e);
			return;
		}
		this.out = buf;

		start();
	}

	private void readInputStream() {
		if (out == null) {
			synchronized (this) {
				finished = true;
				notifyAll();
				return;
			}
		}

		try {
			while (true) {
				int res = out.read();
				if (res < 0) {
					synchronized (this) {
						finished = true;
						notifyAll();
						return;
					}
				}
				if (sb != null) {
					sb.append((char) res);
				}
			}
		} catch (Throwable e) {
			synchronized (this) {
				finished = true;
				notifyAll();
				return;
			}
		} finally {
			try {
				out.close();
			} catch (Throwable e) {
				// ignore
			}
		}
	}

	public void run() {
		if (out != null) {
			readInputStream();
		}
	}

	public synchronized StringBuffer getResult() {
		while (!finished) {
			try {
				wait();
			} catch (Throwable t) {
			} // Ignore.
		}

		return sb;
	}

	protected void finalize() throws Throwable {

		try {
			if (out != null) {
				out.close();
			}
		} catch (Throwable t) {
			// ignore
		}
	}
}