/*
 * Created on May 18, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.util.NoInfoLogging;

/**
 * @author rob
 */
public class AdaptorInvocationHandler implements InvocationHandler {

    protected static Logger logger = LoggerFactory
            .getLogger(AdaptorInvocationHandler.class);

    static final boolean OPTIMIZE_ADAPTOR_POLICY = true;

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
        
        URI param = null;
        for (Object p : params) {
            if (p instanceof URI) {
                param = (URI) p;
                break;
            }
        }

        for (Adaptor adaptor : adaptors) {
            try {
                if (adaptor.matchScheme(param)) {
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
                } else {
                    e.add(adaptor.adaptorName, new AdaptorNotApplicableException("cannot handle this URI: " + param));
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
        String loggerString = "";
        for (Adaptor adaptor : adaptors) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                // Set context classloader before calling constructor.
                // Some adaptors may need this because some libraries
                // explicitly
                // use the context classloader. (jaxrpc).
                Thread.currentThread().setContextClassLoader(
                        adaptor.adaptorClass.getClassLoader());
                String paramString = "";
                if (params != null) {
                    for (Object param : params) {
                        paramString += param + ", ";
                    }
                    if (paramString.endsWith(", ")) {
                        paramString = paramString.substring(0, paramString
                                .length() - 2);
                    }
                }
                loggerString += adaptor.getCpi() + " ("
                        + instantiatedAdaptors.get(adaptor) + ")."
                        + m.getName() + "(" + paramString + ") -> "
                        + adaptor.getShortAdaptorClassName();

                // now invoke the method on the adaptor
                Object res = m
                        .invoke(instantiatedAdaptors.get(adaptor), params);

                if (logger.isInfoEnabled()) {
                    if (adaptor.getAdaptorClass().getMethod(m.getName(),
                            m.getParameterTypes()).isAnnotationPresent(
                            NoInfoLogging.class)) {
                        logger.debug(loggerString + " SUCCESS");

                    } else {
                        logger.info(loggerString + " SUCCESS");
                    }
                }
                return res; // return on first successful adaptor
            } catch (Throwable t) {
                while (t instanceof InvocationTargetException) {
                    t = ((InvocationTargetException) t).getTargetException();
                }
                if (t instanceof java.util.NoSuchElementException) {
                    System.out.println("" + instantiatedAdaptors.get(adaptor));
                    if (instantiatedAdaptors.get(adaptor) instanceof org.gridlab.gat.advert.AdvertService) {
                        throw t;
                    }
                }
                e.add(adaptor.getShortAdaptorClassName(), loggerString, t);
                if (logger.isInfoEnabled()) {
                    if (adaptor.getAdaptorClass().getMethod(m.getName(),
                            m.getParameterTypes()).isAnnotationPresent(
                            NoInfoLogging.class)) {
                        logger.debug(loggerString + " FAILED (" + t + ")");
                    } else {
                        logger.info(loggerString + " FAILED (" + t + ")");
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

        if (logger.isTraceEnabled()) {
            logger.trace("adaptor instantiation: "
                    + adaptor.getShortAdaptorClassName() + " for type "
                    + adaptor.getCpi() + " START");
        }
        Object result;
        try {
            result = adaptor.newInstance(newParameterTypes, newParameters);
        } catch (Throwable t) {
            if (logger.isTraceEnabled()) {
                logger.trace("adaptor instantiation: "
                        + adaptor.getShortAdaptorClassName() + " for type "
                        + adaptor.getCpi() + " FAILED");
            }
            GATObjectCreationException exc;
            if (t instanceof GATObjectCreationException) {
                exc = (GATObjectCreationException) t;
            } else {
                exc = new GATObjectCreationException();
                exc.add(adaptor.toString(), t);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("reason: " + t);
            }
            throw exc;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("adaptor instantiation: "
                    + adaptor.getShortAdaptorClassName() + " for type "
                    + adaptor.getCpi() + " SUCCESS");
        }

        return result;
    }
}
