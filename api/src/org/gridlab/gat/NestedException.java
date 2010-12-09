/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
 * separate adaptor. The methods getMessage and printStrackTrace will reflect
 * this hierarchy.
 */
@SuppressWarnings("serial")
class NestedException extends Exception {
    ArrayList<Throwable> throwables = new ArrayList<Throwable>();

    ArrayList<String> adaptorNames = new ArrayList<String>();

    private String description;

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

    /**
     * Adds a throwable to this NestedException, which is caused by the given
     * adaptor
     * 
     * @param adaptor
     *            the adaptor that caused the throwable
     * @param t
     *            the throwable that is caused by the adaptor
     */
    public void add(String adaptor, Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
        }

        String shortName = null;
        if (adaptor == null) {
        	adaptor = "MISSING ADAPTOR NAME";
        }
        int pos = adaptor.lastIndexOf(".");
        if (pos < 0) {
            shortName = adaptor;
        } else {
            shortName = adaptor.substring(pos + 1);
        }

        throwables.add(t);
        adaptorNames.add(shortName);
    }

    /**
     * Adds a throwable to this NestedException, which is caused by the given
     * adaptor
     * 
     * @param adaptor
     *            the adaptor that caused the throwable
     * @param description
     *            the description of the activity that caused this exception
     * @param t
     *            the throwable that is caused by the adaptor
     */
    public void add(String adaptor, String description, Throwable t) {
        if (t instanceof InvocationTargetException) {
            t = t.getCause();
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
        this.description = description;
    }

    public String getMessage() {
        return getMessage("");
    }

    /**
     * gets the message of the NestedException, where each line of the exception
     * is indented by the supplied indentation String.
     * 
     * @param indent
     * @return the indented message of the exception
     */
    public String getMessage(String indent) {
        String res = "";

        if (throwables.size() == 0) {
            return super.getMessage();
        }

        if (throwables.size() > 1) {
            res = "\n" + indent + "--- START OF NESTED EXCEPTION ---\n";
        }

        for (int i = 0; i < throwables.size(); i++) {

            throwables.get(i).printStackTrace();

            String msg = throwables.get(i).getMessage();

            if (msg == null) {
                if (throwables.get(i) instanceof NestedException) {
                    msg = ((NestedException) throwables.get(i)).toString(indent
                            + "    ");
                } else {
                    msg = throwables.get(i).toString();
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
        String result = toString("");
        if (description != null) {
            result += "\n--- START OF DESCRIPTION ---\n" + description
                    + "\n---- END OF DESCRIPTION ----";
        }
        return result;
    }

    /**
     * this method provides a way to access the getMessage of the Exception
     * class
     * 
     * @return the getMessage of the Exception class
     */
    public String getSuperMessage() {
        return super.getMessage();
    }

    /**
     * gets the String representation of the NestedException, where each line of
     * the exception is indented by the supplied indentation String.
     * 
     * @param indent
     * @return the indented String representation of the exception
     */

    public String toString(String indent) {
        String res = "";
        if (throwables.size() == 0) {
            return super.getMessage();
        }

        res = "\n" + indent + "--- START OF NESTED EXCEPTION ---\n";

        for (int i = 0; i < throwables.size(); i++) {
            if (adaptorNames.get(i) != null) {
                if (adaptorNames.get(i).equals("")) {
                    res += indent + "*** ("
                            + throwables.get(i).getClass().getSimpleName()
                            + "): ";
                    if (throwables.get(i) instanceof NestedException) {
                        res += ((NestedException) throwables.get(i))
                                .getSuperMessage();
                    } else {
                        res += throwables.get(i).getMessage();
                    }

                } else {
                    res += (indent + "*** " + adaptorNames.get(i) + " failed ("
                            + throwables.get(i).getClass().getSimpleName() + "): ");
                }
            }

            String msg;
            if (throwables.get(i) instanceof NestedException) {
                msg = ((NestedException) throwables.get(i)).toString(indent
                        + "    ");
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

    /**
     * prints the stack trace of the NestedException.
     */
    public void printStackTrace() {
        printStackTrace(System.err, "");
    }

    /**
     * prints the stack trace of the NestedException, where each line of the
     * exception is indented by the supplied indentation String. Mostly provided
     * for backwards compatibility.
     * 
     * @param indent
     *            indentation of written text
     */

    public void printStackTrace(String indent) {
        printStackTrace(System.err, indent);
    }

    /**
     * prints the stack trace of the NestedException to the given stream
     * 
     * @param stream
     *            destination of the stacktrace
     */
    public void printStackTrace(PrintStream stream) {
        printStackTrace(stream, "");
    }

    /**
     * prints the stacktrace of the NestedException to the given writer.
     * 
     * @param writer
     *            destination of the stacktrace
     */
    public void printStackTrace(PrintWriter writer) {
        printStackTrace(writer, "");
    }

    /**
     * prints the stack trace of the NestedException to the given stream, where
     * each line of the exception is indented by the supplied indentation
     * String.
     * 
     * @param stream
     *            destination of the stacktrace
     * @param indent
     *            indentation of written text
     */
    public void printStackTrace(PrintStream stream, String indent) {
        if (throwables.size() == 0) {
            super.printStackTrace(stream);
            return;
        }

        stream
                .println(indent
                        + "--- START OF NESTED EXCEPTION STACK TRACE ---");

        for (int i = 0; i < throwables.size(); i++) {
            if (adaptorNames.get(i) != null) {
                stream.println(indent + "*** stack trace of "
                        + throwables.get(i).getClass().getSimpleName());
            }

            if (throwables.get(i) instanceof NestedException) {
                ((NestedException) throwables.get(i)).printStackTrace(stream,
                        indent + "    ");
            } else {
                StringWriter sWriter = new StringWriter();
                throwables.get(i).printStackTrace(new PrintWriter(sWriter));
                stream.println(indent + sWriter.toString());
            }
        }

        stream.println(indent + "--- END OF NESTED EXCEPTION STACK TRACE ---");
    }

    /**
     * prints the stacktrace of the NestedException to the given writer, where
     * each line of the exception is indented by the supplied indentation
     * String.
     * 
     * @param writer
     *            destination of the stacktrace
     * @param indent
     *            indentation of written text
     */
    public void printStackTrace(PrintWriter writer, String indent) {
        if (throwables.size() == 0) {
            super.printStackTrace(writer);

            return;
        }

        writer
                .println(indent
                        + "--- START OF NESTED EXCEPTION STACK TRACE ---");

        for (int i = 0; i < throwables.size(); i++) {
            if (adaptorNames.get(i) != null) {
                writer.println(indent + "*** stack trace of "
                        + throwables.get(i).getClass().getSimpleName());
            }

            if (throwables.get(i) instanceof NestedException) {
                ((NestedException) throwables.get(i)).printStackTrace(writer,
                        indent + "    ");
            } else {
                StringWriter sWriter = new StringWriter();
                throwables.get(i).printStackTrace(new PrintWriter(sWriter));
                writer.println(indent + sWriter.toString());
            }
        }

        writer.println(indent + "--- END OF NESTED EXCEPTION STACK TRACE ---");
    }

    /**
     * returns an array of all the throwables that are inside the nested
     * exception
     * 
     * @return an array of throwables containing the exceptions
     */
    public Throwable[] getExceptions() {
        return throwables.toArray(new Throwable[throwables.size()]);
    }

    /**
     * returns an array of all the adaptors that caused an exception on this
     * NestedException
     * 
     * @return an array of adaptors that caused the exceptions
     */
    public String[] getAdaptors() {
        return adaptorNames.toArray(new String[adaptorNames.size()]);
    }

    /**
     * returns the number of children of the nested exception If children of
     * this NestedException turn out to be NestedExceptions, they are counted as
     * just one child, so only first line children are counted. Grand children
     * are not counted!
     * 
     * @return the number of children
     */
    public int getNrChildren() {
        return throwables.size();
    }
}
