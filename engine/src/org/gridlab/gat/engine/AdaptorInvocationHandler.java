/*
 * Created on May 18, 2004
 */
package org.gridlab.gat.engine;

import colobus.Colobus;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * @author rob
 */
public class AdaptorInvocationHandler implements InvocationHandler {
    private static final Colobus colobus = Colobus
        .getColobus(AdaptorInvocationHandler.class.getName());

    /**
     * static list of adaptor class names in order of successful execution
     */
    private static LinkedList adaptorlist = new LinkedList();

    private GATContext context;

    private Preferences preferences;

    private Object[] parameters;

    /**
     * the available adaptors, keyed by class name
     */
    private Hashtable adaptors = new Hashtable();

    public AdaptorInvocationHandler(AdaptorList adaptors, GATContext context,
            Preferences preferences, Object[] params) {
        this.context = context;

        if (preferences != null) {
            this.preferences = (Preferences) preferences.clone();
        }

        if (params != null) {
            this.parameters = new Object[params.length];
            System.arraycopy(params, 0, this.parameters, 0, params.length);
        }

        Adaptor adaptor;
        String adaptorname;

        for (int count = 0; count < adaptors.size(); count++) {
            adaptor = adaptors.get(count);
            adaptorname = adaptor.getName();

            this.adaptors.put(adaptorname, adaptor);

            synchronized (adaptorlist) {
                if (!adaptorlist.contains(adaptorname)) {
                    adaptorlist.add(adaptorname);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method m, Object[] params)
            throws Throwable {
        GATInvocationException e = new GATInvocationException();

        if (adaptors == null) {
            throw new GATInvocationException("no adaptor available for method "
                + m);
        }

        String[] adaptornames;
        Object adaptor;

        synchronized (adaptorlist) {
            adaptornames = (String[]) adaptorlist
                .toArray(new String[adaptorlist.size()]);
        }

        // try adaptors in order of success
        for (int i = 0; i < adaptornames.length; i++) {
            // only try adaptors available for this handler
            if (adaptors.containsKey(adaptornames[i])) {
                adaptor = adaptors.get(adaptornames[i]);

                try {
                    // initialize the adaptor if not already initialized
                    if (adaptor instanceof Adaptor) {
                        adaptor = initAdaptor((Adaptor) adaptor, context,
                            preferences, parameters);
                        adaptors.put(adaptornames[i], adaptor);
                    }

                    String paramString = "";

                    if (params != null) {
                        for (int p = 0; p < params.length; p++) {
                            paramString += ("[ "
                                + ((params[p] == null) ? "null" : params[p]) + "]");
                        }
                    }

                    if (GATEngine.DEBUG) {
                        System.err.println("invocation of method " + m
                            + " on adaptor " + adaptornames[i] + " START");
                    }

                    long startHandle = colobus
                        .fireStartEvent("invocation of method " + m
                            + " on adaptor " + adaptornames[i] + " params: "
                            + paramString);

                    // now invoke the method on the adaptor
                    Object res = m.invoke(adaptor, params);

                    colobus.fireStopEvent(startHandle, "invocation of method "
                        + m + " on adaptor " + adaptornames[i] + "result: "
                        + res);

                    if (GATEngine.DEBUG) {
                        System.err.println("invocation of method " + m
                            + " on adaptor " + adaptornames[i] + " DONE");
                    }

                    if (i != 0) {
                        // move successful adaptor to start of list
                        synchronized (adaptorlist) {
                            adaptorlist.remove(adaptornames[i]);
                            adaptorlist.add(0, adaptornames[i]);
                        }
                    }

                    return res; // return on first successful adaptor
                } catch (GATObjectCreationException except) {
                    e.add(((Adaptor) adaptor).getName(), except);
                } catch (Throwable t) {
                    if (GATEngine.VERBOSE) {
                        System.err.print("Method " + m.getName()
                            + " on adaptor " + adaptornames[i] + " failed: ");

                        if (t instanceof InvocationTargetException) {
                            t = ((InvocationTargetException) t)
                                .getTargetException();
                        }

                        System.err.println(t);

                        if (GATEngine.DEBUG) {
                            t.printStackTrace();
                        }
                    }

                    e.add(adaptornames[i], t);
                }
            }
        }

        if (GATEngine.VERBOSE) {
            System.err.println("invoke: No adaptor could be invoked.");
        }

        throw e;
    }

    /**
     * Returns an instance of the specified XXXCpi class consistent with the
     * passed XXXCpi class name, preferences, and parameters
     *
     * @param adaptor     The adaptor to initialize
     * @param preferences The Preferences used to construct the Cpi class.
     * @param parameters  The Parameters for the Cpi Constructor null means no
     *                    parameters.
     * @param gatContext  the context
     * @return The specified Cpi class or null if no such adaptor exists
     * @throws org.gridlab.gat.GATObjectCreationException
     *          creation of the adaptor failed
     */
    public Object initAdaptor(Adaptor adaptor, GATContext gatContext,
            Preferences preferences, Object[] parameters)
            throws GATObjectCreationException {
        if (preferences == null) { // No preferences.
            preferences = new Preferences(new Hashtable());
        }

        if (parameters == null) {
            parameters = new Object[0];
        }

        // Add the context and the preferences as parameters
        Object[] newParameters = new Object[parameters.length + 2];
        newParameters[0] = gatContext;
        newParameters[1] = preferences;

        for (int i = 0; i < parameters.length; i++) {
            newParameters[i + 2] = parameters[i];
        }

        // Create an array with the parameter types
        Class[] parameterTypes = new Class[newParameters.length];

        for (int count = 0; count < parameterTypes.length; count++) {
            parameterTypes[count] = newParameters[count].getClass();
        }

        /*
         * for (int i = 0; i &lt; l.size(); i++) { Adaptor a = l.get(i); if
         * (a.satisfies(preferences)) { Object o = null; try { o =
         * a.newInstance(parameterTypes, newParameters); if (VERBOSE) {
         * System.err .println(&quot;getAdaptorList: Created adaptor instance of
         * type &quot; + a.getName()); } result.add(o); } catch (Throwable t) {
         * if (exc == null) { exc = new GATObjectCreationException(); }
         * exc.add(a.toString(), t); if (VERBOSE) { System.err
         * .println(&quot;getAdaptorList: Could not create an instance of
         * Adaptor &quot; + a.getName()); } } } else { // it does not satisfy
         * prefs. if (exc == null) { exc = new GATObjectCreationException(); }
         * exc.add(a.toString(), new GATInvocationException( &quot;adaptor does
         * not satisfy preferences&quot;)); } }
         */
        Object o;

        if (!adaptor.satisfies(preferences)) {
            // it does not satisfy prefs.
            GATObjectCreationException exc = new GATObjectCreationException();
            exc.add(adaptor.toString(), new GATInvocationException(
                "adaptor does not satisfy preferences"));
            throw (exc);
        }

        long startHandle = colobus.fireStartEvent("creating adaptor "
            + adaptor.getName());

        try {
            o = adaptor.newInstance(parameterTypes, newParameters);
            colobus.fireStopEvent(startHandle, "creating adaptor "
                + adaptor.getName() + " (success)");
        } catch (Throwable t) {
            colobus.fireStopEvent(startHandle, "creating adaptor "
                + adaptor.getName() + " (failed)");

            GATObjectCreationException exc = new GATObjectCreationException();
            exc.add(adaptor.toString(), t);

            if (GATEngine.VERBOSE) {
                System.err.println("    Couldn't create " + adaptor.getName()
                    + ": " + t.getMessage());
            }

            throw exc;
        }

        if (GATEngine.VERBOSE) {
            System.err.println("initAdaptor: instantiated " + adaptor.getName()
                + " adaptor for type " + adaptor.getCpi());
        }

        return o;
    }
}
