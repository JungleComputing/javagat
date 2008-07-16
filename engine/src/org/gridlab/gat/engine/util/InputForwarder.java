package org.gridlab.gat.engine.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class InputForwarder extends Thread {
    OutputStream in;

    InputStream source;

    String s;

    public InputForwarder(OutputStream in, InputStream source) {
        super("InputForwarderThread");

        this.source = source;
        this.in = in;

        InputStream buf;

        try {
            buf = new BufferedInputStream(source);
        } catch (Throwable e) {
            return;
        }

        this.source = buf;
        setDaemon(true);

        start();
    }

    public void run() {
        try {
            while (true) {
                int res = source.read();

                if (res < 0) {
                    return;
                }

                if (in != null) {
                    in.write(res);
                }
            }
        } catch (Throwable e) {
            return;
        } finally {
            try {
                in.close();
            } catch (Throwable e) {
                // ignore
            }
        }
    }
}
