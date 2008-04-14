/*
 * MPA Source File: ParamWriter.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	08.11.2005 (14:41:28) by doerl $ Last Change: 08.11.2005 (14:41:28) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author  doerl
 */
public class ParamWriter extends PrintWriter {
    private static final DateFormat sFormatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
    private String mSuffix = "#$";

    public ParamWriter(Writer out, String suffix) {
        super(out);
        mSuffix = suffix;
    }

    public ParamWriter(Writer out, String suffix, boolean autoFlush) {
        super(out, autoFlush);
        mSuffix = suffix;
    }

    public void addBoolean(String opt, Object param) {
        if (param != null) {
            try {
                Boolean b = (Boolean) param;
                addOption(opt, b.booleanValue() ? "y" : "n");
            }
            catch (ClassCastException ex) {
            }
        }
    }

    public void addDate(String opt, Object param) {
        if (param != null) {
            try {
                Date d = (Date) param;
                addOption(opt, sFormatter.format(d));
            }
            catch (ClassCastException ex) {
            }
        }
    }

    public void addParam(String opt, Object param) {
        if (param != null) {
            addOption(opt, null);
        }
    }

    public void addString(String opt, Object param) {
        if (param != null) {
            addOption(opt, param);
        }
    }

    private void addOption(String opt, Object param) {
        print(mSuffix);
        print(" -");
        print(opt);
        if (param != null) {
            print(" ");
            println(param);
        }
        else {
            println();
        }
    }
}
