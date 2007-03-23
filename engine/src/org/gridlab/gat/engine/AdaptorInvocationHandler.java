/*
 * Created on May 18, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.AdaptorNotSelectedException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;

import colobus.Colobus;

/**
 * @author rob
 */
public class AdaptorInvocationHandler implements InvocationHandler {

    static class AdaptorSorter {

        /**
         * list of adaptor class names (Strings) in order of successful execution
         */
        private LinkedList adaptorlist = new LinkedList();
        
        /**
         * list of adaptor class names (Strings) in order of successful execution per method
         * <methodName, LinkedList>
         */
        private HashMap adaptorMethodList = new HashMap();
        
        synchronized void add(String adaptorName) {
            if (!adaptorlist.contains(adaptorName)) {
                adaptorlist.add(adaptorName);
            }
        }

        synchronized String[] getOrdering(Method method) {
            ArrayList res = new ArrayList();
            ArrayList l = (ArrayList) adaptorMethodList.get(method);
            
            if(l==null) {
                return (String[]) adaptorlist.toArray(new String[adaptorlist.size()]);
            }
            
            // We have a list for this particular method. Use that order
            // first, and append the other adaptors at the end (in order).
            for(int i=0; i<l.size(); i++) {
                res.add(l.get(i)); // append
            }
            
            for(int i=0; i<adaptorlist.size(); i++) {
                String s = (String) adaptorlist.get(i);
                if(!res.contains(s)) {
                    res.add(s);
                }
            }

            return (String[]) res.toArray(new String[res.size()]);
        }
        
        synchronized void success(String adaptorName, Method method) {
            ArrayList l = (ArrayList) adaptorMethodList.get(method);
            
            if(l==null) {
                l = new ArrayList();
                adaptorMethodList.put(method, l);
            } else {
                l.remove(adaptorName);                
            }

            l.add(0, adaptorName);
        }
    }
    
    static final boolean OPTIMIZE_ADAPTOR_POLICY = true;

    private static final Colobus colobus =
            Colobus.getColobus(AdaptorInvocationHandler.class.getName());

    private static AdaptorSorter adaptorSorter = new AdaptorSorter();
    
    private GATContext context;

    private Preferences preferences;

    private Object[] constructorParameters;

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

        // @@@ this is not 100% correct! if the user changes the content
        // of the params after the create call, constructors that are called later might see it. 
        if (params != null) {
            this.constructorParameters = new Object[params.length];
            System.arraycopy(params, 0, this.constructorParameters, 0, params.length);
        }

        for (int count = 0; count < adaptors.size(); count++) {
            Adaptor adaptor = adaptors.get(count);
            String adaptorname = adaptor.getName();

            this.adaptors.put(adaptorname, adaptor);

            adaptorSorter.add(adaptorname);
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

        adaptornames = adaptorSorter.getOrdering(m);

        // try adaptors in order of success
        for (int i = 0; i < adaptornames.length; i++) {
            // only try adaptors available for this handler
            if (adaptors.containsKey(adaptornames[i])) {
                adaptor = adaptors.get(adaptornames[i]);

                try {
                    // initialize the adaptor if not already initialized
                    if (adaptor instanceof Adaptor) {
                        adaptor =
                                initAdaptor((Adaptor) adaptor, context,
                                        preferences, constructorParameters);
                        adaptors.put(adaptornames[i], adaptor);
                    }

                    String paramString = "";

                    if (params != null) {
                        for (int p = 0; p < params.length; p++) {
                            paramString +=
                                    ("[ "
                                            + ((params[p] == null) ? "null"
                                                    : params[p]) + "]");
                        }
                    }

                    if (GATEngine.DEBUG) {
                        System.err.println("invocation of method " + m
                                + " on adaptor " + adaptornames[i] + " START");
                    }

                    long startHandle =
                            colobus.fireStartEvent("invocation of method " + m
                                    + " on adaptor " + adaptornames[i]
                                    + " params: " + paramString);

                    // now invoke the method on the adaptor
                    Object res = m.invoke(adaptor, params);

                    colobus.fireStopEvent(startHandle, "invocation of method "
                            + m + " on adaptor " + adaptornames[i] + "result: "
                            + res);

                    if (GATEngine.DEBUG) {
                        System.err.println("invocation of method " + m
                                + " on adaptor " + adaptornames[i] + " DONE");
                    }

                    if (OPTIMIZE_ADAPTOR_POLICY && i != 0) {
                        // move successful adaptor to start of list
                        adaptorSorter.success(adaptornames[i], m);
                    }

                    return res; // return on first successful adaptor
                } catch (Throwable t) {
                    while (t instanceof InvocationTargetException) {
                        t =
                                ((InvocationTargetException) t)
                                        .getTargetException();
                    }

                    if (t instanceof GATObjectCreationException) {
                        e.add(((Adaptor) adaptor).getName(), t);
                    } else if (t instanceof MethodNotApplicableException) {
                        e.add(adaptornames[i], t);
                        if (GATEngine.DEBUG) {
                            System.err.print("Method " + m.getName()
                                    + " on adaptor " + adaptornames[i]
                                    + " is not applicable: ");
                            System.err.println(t);
                        }
                    } else {
                        e.add(adaptornames[i], t);
                        if (GATEngine.VERBOSE) {
                            System.err.print("Method " + m.getName()
                                    + " on adaptor " + adaptornames[i]
                                    + " failed: ");
                            System.err.println(t);
                            if (GATEngine.DEBUG) {
                                t.printStackTrace();
                            }
                        }
                    }
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
     * passed XXXCpi class name, preferences, and constructorParameters
     *
     * @param adaptor     The adaptor to initialize
     * @param preferences The Preferences used to construct the Cpi class.
     * @param constructorParameters  The Parameters for the Cpi Constructor null means no
     *                    constructorParameters.
     * @param gatContext  the context
     * @return The specified Cpi class or null if no such adaptor exists
     * @throws org.gridlab.gat.GATObjectCreationException
     *          creation of the adaptor failed
     */
    private Object initAdaptor(Adaptor adaptor, GATContext gatContext,
            Preferences preferences, Object[] parameters)
            throws GATObjectCreationException {
        if (preferences == null) { // No preferences.
            preferences = new Preferences(new Hashtable());
        }

        if (parameters == null) {
            parameters = new Object[0];
        }

        // Add the context and the preferences as constructorParameters
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

        if (!adaptor.satisfies(preferences)) {
            // it does not satisfy prefs.
            GATObjectCreationException exc = new GATObjectCreationException();
            exc.add(adaptor.toString(), new GATInvocationException(
                    "adaptor does not satisfy preferences"));
            throw (exc);
        }

        // check if the adaptor has been excluded in the preferences
        if (!GATEngine.isAdaptorSelected(preferences, adaptor)) {
            throw new AdaptorNotSelectedException(
                    "adaptor has not been selected by the user");
        }

        long startHandle =
                colobus.fireStartEvent("creating adaptor " + adaptor.getName());

        Object result;
        if (GATEngine.DEBUG) {
            System.err.println("initAdaptor: trying to instantiate "
                    + adaptor.getName() + " adaptor for type "
                    + adaptor.getCpi());
        }
        try {
            result = adaptor.newInstance(parameterTypes, newParameters);
            colobus.fireStopEvent(startHandle, "creating adaptor "
                    + adaptor.getName() + " (success)");
        } catch (Throwable t) {
            colobus.fireStopEvent(startHandle, "creating adaptor "
                    + adaptor.getName() + " (failed)");

            GATObjectCreationException exc = new GATObjectCreationException();
            exc.add(adaptor.toString(), t);

            if (t instanceof AdaptorNotApplicableException) {
                if (GATEngine.DEBUG) {
                    System.err.println("Adaptor " + adaptor.getName()
                            + " is not applicable: " + t.getMessage());
                }
            } else {
                if (GATEngine.VERBOSE) {
                    System.err.println("Couldn't create " + adaptor.getName()
                            + ": " + t.getMessage());
                }
            }
            
            throw exc;
        }

        if (GATEngine.VERBOSE) {
            System.err.println("initAdaptor: instantiated " + adaptor.getName()
                    + " adaptor for type " + adaptor.getCpi());
        }

        return result;
    }
}
