/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @author rob
 */
/**
 * This class defines an exception that can have multiple causes. The causes can
 * again be nested exceptions. This exception is used by the GAT engine. If a
 * file.copy method is invoked, for instance, the gat engine will try all loaded
 * file adaptors until one succeeds. If none of the adaptors can copy the file,
 * a NestedException is thrown, containing the exceptions thrown by each
 * seperate adaptor. The methods getMessage and printStrackTrace will reflect
 * this hierarchy.
 */
@SuppressWarnings("serial")
class NestedException extends Exception {
    ArrayList<Throwable> throwables = new ArrayList<Throwable>();

    ArrayList<String> adaptorNames = new ArrayList<String>();

    public NestedException(String s) {
        super(s);
    }

    public NestedException() {
        super();
    }

    public NestedException(String adaptor, Throwable t) {
        super();
        add(adaptor, t);
    }

    public void add(String adaptor, Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
        }

        if (t instanceof NestedException) {
            NestedException ge = (NestedException) t;

            if (ge.throwables.size() == 1) {
                t = (Throwable) ge.throwables.get(0);
                adaptor = (String) ge.adaptorNames.get(0);
            }
        }

        String shortName = null;
        int pos = adaptor.lastIndexOf(".");
        if (pos < 0) {
            shortName = adaptor;
        } else {
            shortName = adaptor.substring(pos + 1);
        }

        throwables.add(t);
        adaptorNames.add(shortName);
    }

    public String getMessage() {
        return getMessage("");
    }
    
    public String getMessage(String indent) {
        String res = "";

        if (throwables.size() == 0) {
            return super.getMessage();
        }

        if (throwables.size() > 1) {
            res = "\n" + indent +"--- START OF NESTED EXCEPTION ---\n";
        }

        for (int i = 0; i < throwables.size(); i++) {
            String msg = ((Throwable) throwables.get(i)).getMessage();

            if (msg == null) {
                if (throwables.get(i) instanceof NestedException) {
                    msg = ((NestedException) throwables.get(i)).toString(indent + "    ");
                } else {
                    msg = ((Throwable) throwables.get(i)).toString();
                }
            }
            res += indent + "*** ";
            res += msg;

            if (throwables.size() > 1) {
                res += "\n";
            }
        }
        res.replaceAll("\n", "\n" + indent);
        if (throwables.size() > 1) {
            res += indent + "--- END OF NESTED EXCEPTION ---";
        }

        return res;
    }

    public String toString() {
        return toString("");
    }
    
    public String getSuperMessage() {
        return super.getMessage();
    }
    
    public String toString(String indent) {
        String res = "";
        if (throwables.size() == 0) {
            return super.getMessage();
        }
        res = "\n" + indent + "--- START OF NESTED EXCEPTION ---\n";

        for (int i = 0; i < throwables.size(); i++) {
            if (adaptorNames.get(i) != null) {
                if (adaptorNames.get(i).equals("")) {
                    res += (indent + "*** ("
                            + throwables.get(i).getClass().getSimpleName() + "): " + ((NestedException) throwables.get(i)).getSuperMessage());
                } else {
                    res += (indent + "*** " + adaptorNames.get(i) + " failed ("
                        + throwables.get(i).getClass().getSimpleName() + "): ");
                }
            }

            String msg; 
            if (throwables.get(i) instanceof NestedException) {
                msg = ((NestedException) throwables.get(i)).toString(indent + "    ");
            } else {
                msg = throwables.get(i).getMessage();
            }
            res += msg;
            res += "\n";
        }
        res.replaceAll("\n", "\n" + indent);
        res += indent + "--- END OF NESTED EXCEPTION ---";

        return res;
    }
        

    public void printStackTrace() {
        if (throwables.size() == 0) {
            super.printStackTrace();

            return;
        }

        System.err.println("--- START OF NESTED EXCEPTION STACK TRACE ---");

        for (int i = 0; i < throwables.size(); i++) {
            if (adaptorNames.get(i) != null) {
                System.err.println("*** stack trace of " + adaptorNames.get(i));
            }

            ((Throwable) throwables.get(i)).printStackTrace();
        }

        System.err.println("--- END OF NESTED EXCEPTION STACK TRACE ---");
    }

    public Throwable[] getExceptions() {
        return (Throwable[]) throwables.toArray();
    }

    public String[] getAdaptors() {
        return (String[]) adaptorNames.toArray();
    }

    public int getNrChildren() {
        return throwables.size();
    }
}
