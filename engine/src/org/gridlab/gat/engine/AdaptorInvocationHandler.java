/*
 * Created on May 18, 2004
 */
package org.gridlab.gat.engine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;

/**
 * @author rob
 */
public class AdaptorInvocationHandler implements InvocationHandler {

    protected static Logger logger = Logger
            .getLogger(AdaptorInvocationHandler.class);

    // static class AdaptorSorter {
    //
    // /**
    // * list of adaptor class names (Strings) in order of successful
    // * execution
    // */
    // private LinkedList<String> adaptorlist = new LinkedList<String>();
    //
    // /**
    // * list of adaptor class names (Strings) in order of successful
    // * execution per method <methodName, LinkedList>
    // */
    // private HashMap<Method, ArrayList<String>> adaptorMethodList = new
    // HashMap<Method, ArrayList<String>>();
    //
    // synchronized void add(String adaptorName) {
    // if (!adaptorlist.contains(adaptorName)) {
    // adaptorlist.add(adaptorName);
    // }
    // }
    //
    // synchronized String[] getOrdering(Method method) {
    // ArrayList<String> res = new ArrayList<String>();
    // ArrayList<String> l = (ArrayList<String>) adaptorMethodList
    // .get(method);
    //
    // if (l == null) {
    // return (String[]) adaptorlist.toArray(new String[adaptorlist
    // .size()]);
    // }
    //
    // // We have a list for this particular method. Use that order
    // // first, and append the other adaptorInstantiations at the end (in
    // // order).
    // for (int i = 0; i < l.size(); i++) {
    // res.add(l.get(i)); // append
    // }
    //
    // for (int i = 0; i < adaptorlist.size(); i++) {
    // String s = (String) adaptorlist.get(i);
    // if (!res.contains(s)) {
    // res.add(s);
    // }
    // }
    //
    // return (String[]) res.toArray(new String[res.size()]);
    // }
    //
    // synchronized void success(String adaptorName, Method method) {
    // ArrayList<String> l = (ArrayList<String>) adaptorMethodList
    // .get(method);
    //
    // if (l == null) {
    // l = new ArrayList<String>();
    // adaptorMethodList.put(method, l);
    // } else {
    // l.remove(adaptorName);
    // }
    //
    // l.add(0, adaptorName);
    // }
    // }

    static final boolean OPTIMIZE_ADAPTOR_POLICY = true;

    // private static AdaptorSorter adaptorSorter = new AdaptorSorter();

    /**
     * the available adaptorInstantiations, keyed by class name the elements are
     * of type object (the real adaptor)
     */
    // private Hashtable<String, Object> adaptorInstantiations = new
    // Hashtable<String, Object>();
    /**
     * the available adaptors, keyed by class name the elements are of type
     * Adaptor
     */
    // private Hashtable<String, Adaptor> adaptors = new Hashtable<String,
    // Adaptor>();
    private List<Adaptor> adaptors = new ArrayList<Adaptor>();

    private Map<Adaptor, Object> instantiatedAdaptors = new HashMap<Adaptor, Object>();

    public AdaptorInvocationHandler(List<Adaptor> adaptors, GATContext context,
            Class<?>[] parameterTypes, Object[] params, boolean singleInstance)
            throws GATObjectCreationException {

        if (adaptors.size() == 0) {
            throw new GATObjectCreationException(
                    "no adaptors could be loaded for this object");
        }

        GATObjectCreationException e = new GATObjectCreationException(
                "No adaptors could be successfully instantiated");

        for (Adaptor adaptor : adaptors) {
            try {
                instantiatedAdaptors.put(adaptor, initAdaptor(adaptor, context,
                        parameterTypes, params));
                this.adaptors.add(adaptor);
                // this test keeps the number of instantiated adaptors for
                // objects that contain internal state, like the
                // FileInputStream, to a fixed size of 1, in order to prevent
                // multiple adaptors operating on one object, while all the
                // adaptors have a different internal state.
                if (singleInstance) {
                    break;
                }
            } catch (Throwable t) {
                e.add(adaptor.adaptorName, t);
            }
        }
        if (instantiatedAdaptors.size() == 0) {
            throw e;
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

        if (instantiatedAdaptors == null) {
            throw new GATInvocationException("no adaptor available for method "
                    + m);
        }

        for (Adaptor adaptor : adaptors) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                // Set context classloader before calling constructor.
                // Some adaptors may need this because some libraries
                // explicitly
                // use the context classloader. (jaxrpc).
                Thread.currentThread().setContextClassLoader(
                        adaptor.adaptorClass.getClassLoader());
                if (logger.isDebugEnabled()) {
                    logger.debug("invocation of method " + m.getName() + " on "
                            + adaptor.getShortAdaptorClassName() + " START");
                }

                // now invoke the method on the adaptor
                Object res = m
                        .invoke(instantiatedAdaptors.get(adaptor), params);

                if (logger.isDebugEnabled()) {
                    logger.debug("invocation of method " + m.getName() + " on "
                            + adaptor.getShortAdaptorClassName() + " DONE");
                }
                return res; // return on first successful adaptor
            } catch (Throwable t) {
                while (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException) t).getTargetException();
                }

                if (t instanceof GATObjectCreationException) {
                    e.add(adaptor.getShortAdaptorClassName(), t);
                } else if (t instanceof MethodNotApplicableException) {
                    e.add(adaptor.getShortAdaptorClassName(), t);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Method " + m.getName() + " on "
                                + adaptor.getShortAdaptorClassName()
                                + " is not applicable: " + t);
                    }
                } else {
                    e.add(adaptor.getShortAdaptorClassName(), t);
                    if (logger.isInfoEnabled()) {
                        logger.info("Method " + m.getName() + " on "
                                + adaptor.getShortAdaptorClassName()
                                + " failed: " + t);
                    }
                    if (logger.isDebugEnabled()) {
                        StringWriter writer = new StringWriter();
                        t.printStackTrace(new PrintWriter(writer));
                        logger.debug(writer.toString());
                    }
                }
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("invoke: No adaptor could be invoked.");
        }

        throw e;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
    // * java.lang.reflect.Method, java.lang.Object[])
    // */
    // public Object invoke(Object proxy, Method m, Object[] params)
    // throws Throwable {
    // GATInvocationException e = new GATInvocationException();
    //
    // if (adaptorInstantiations == null) {
    // throw new GATInvocationException("no adaptor available for method "
    // + m);
    // }
    //
    // String[] adaptornames;
    // Object adaptorInstantiation;
    // Adaptor adaptor;
    //
    // adaptornames = adaptorSorter.getOrdering(m);
    //
    // // try adaptorInstantiations in order of success
    // for (int i = 0; i < adaptornames.length; i++) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("trying adaptor " + i + " of " + adaptornames.length + ": "
    // + adaptornames[i]);
    // }
    // // only try adaptorInstantiations available for this handler
    // if (adaptorInstantiations.containsKey(adaptornames[i])) {
    // adaptorInstantiation = adaptorInstantiations
    // .get(adaptornames[i]);
    // adaptor = (Adaptor) adaptors.get(adaptornames[i]);
    // if (logger.isDebugEnabled()) {
    // logger.debug("selected adaptor is: " +
    // adaptor.getShortAdaptorClassName());
    // }
    //
    // ClassLoader loader = Thread.currentThread()
    // .getContextClassLoader();
    //
    // try {
    // // Set context classloader before calling constructor.
    // // Some adaptors may need this because some libraries
    // // explicitly
    // // use the context classloader. (jaxrpc).
    // Thread.currentThread().setContextClassLoader(
    // adaptor.adaptorClass.getClassLoader());
    // if (logger.isDebugEnabled()) {
    // logger.debug("invocation of method " + m.getName()
    // + " on " + adaptor.getShortAdaptorClassName()
    // + " START");
    // }
    //
    // // now invoke the method on the adaptor
    // Object res = m.invoke(adaptorInstantiation, params);
    //
    // if (logger.isDebugEnabled()) {
    // logger.debug("invocation of method " + m.getName()
    // + " on " + adaptor.getShortAdaptorClassName()
    // + " DONE");
    // }
    //
    // if (OPTIMIZE_ADAPTOR_POLICY && i != 0) {
    // // move successful adaptor to start of list
    // adaptorSorter.success(adaptornames[i], m);
    // }
    //
    // return res; // return on first successful adaptor
    // } catch (Throwable t) {
    // while (t instanceof InvocationTargetException) {
    // t = ((InvocationTargetException) t)
    // .getTargetException();
    // }
    //
    // if (t instanceof GATObjectCreationException) {
    // e.add(((Adaptor) adaptorInstantiation).getName(), t);
    // } else if (t instanceof MethodNotApplicableException) {
    // e.add(adaptornames[i], t);
    // if (logger.isDebugEnabled()) {
    // logger.debug("Method " + m.getName() + " on "
    // + adaptor.getShortAdaptorClassName()
    // + " is not applicable: " + t);
    // }
    // } else {
    // e.add(adaptornames[i], t);
    // if (logger.isInfoEnabled()) {
    // logger.info("Method " + m.getName() + " on "
    // + adaptor.getShortAdaptorClassName()
    // + " failed: " + t);
    // }
    // if (logger.isDebugEnabled()) {
    // StringWriter writer = new StringWriter();
    // t.printStackTrace(new PrintWriter(writer));
    // logger.debug(writer.toString());
    // }
    // }
    // } finally {
    // Thread.currentThread().setContextClassLoader(loader);
    // }
    // }
    // }
    //
    // if (logger.isInfoEnabled()) {
    // logger.info("invoke: No adaptor could be invoked.");
    // }
    //
    // throw e;
    // }

    /**
     * Returns an instance of the specified XXXCpi class consistent with the
     * passed XXXCpi class name, preferences, and constructorParameters
     * 
     * @param adaptor
     *                The adaptor to initialize
     * @param constructorParameters
     *                The Parameters for the Cpi Constructor null means no
     *                constructorParameters.
     * @param gatContext
     *                the context
     * @return The specified Cpi class or null if no such adaptor exists
     * @throws org.gridlab.gat.GATObjectCreationException
     *                 creation of the adaptor failed
     */
    private Object initAdaptor(Adaptor adaptor, GATContext gatContext,
            Class<?>[] parameterTypes, Object[] parameters)
            throws GATObjectCreationException {
        if (parameters == null) {
            parameters = new Object[0];
        }

        // Add the context and the preferences as constructorParameters
        Object[] newParameters = new Object[parameters.length + 1];
        Class<?>[] newParameterTypes = new Class[newParameters.length];

        newParameters[0] = gatContext;
        newParameterTypes[0] = GATContext.class;

        for (int i = 0; i < parameters.length; i++) {
            newParameters[i + 1] = parameters[i];
            newParameterTypes[i + 1] = parameterTypes[i];
        }

        // if (!adaptor.satisfies(gatContext.getPreferences())) {
        // // it does not satisfy prefs.
        // GATObjectCreationException exc = new GATObjectCreationException();
        // exc.add(adaptor.toString(), new GATInvocationException(
        // "adaptor does not satisfy preferences"));
        // throw (exc);
        // }

        Object result;
        if (logger.isDebugEnabled()) {
            logger.debug("initAdaptor: trying to instantiate "
                    + adaptor.getShortAdaptorClassName() + " for type "
                    + adaptor.getCpi());
        }
        try {
            result = adaptor.newInstance(newParameterTypes, newParameters);
        } catch (Throwable t) {
            GATObjectCreationException exc;
            if (t instanceof GATObjectCreationException) {
                exc = (GATObjectCreationException) t;
            } else {
                exc = new GATObjectCreationException();
                exc.add(adaptor.toString(), t);
            }
            if (t instanceof AdaptorNotApplicableException) {
                if (logger.isDebugEnabled()) {
                    logger.debug("initAdaptor: " + adaptor.getCpi()
                            + " is not applicable: " + t.getMessage());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("initAdaptor: Couldn't create "
                            + adaptor.getShortAdaptorClassName() + ": "
                            + t.getMessage());
                }
                if (logger.isDebugEnabled()) {
                    StringWriter writer = new StringWriter();
                    t.printStackTrace(new PrintWriter(writer));
                    logger.debug(writer.toString());
                }
            }
            throw exc;
        }

        if (logger.isInfoEnabled()) {
            logger.info("initAdaptor: instantiated "
                    + adaptor.getShortAdaptorClassName() + " for type "
                    + adaptor.getCpi());
        }

        return result;
    }
}
