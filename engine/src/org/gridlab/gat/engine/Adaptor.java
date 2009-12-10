/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;

/**
 * @author rob
 */
public class Adaptor {
    /** The fully qualified class name of the api we implement. */
    String cpi;

    /** The fully qualified class name of this adaptor. */
    String adaptorName;

    /** The actual class of this adaptor, must be a subclass of cpiClass. */
    Class<?> adaptorClass;
    
    /** The schemes recognized by this adaptor. */
    private String[] schemes;

    /**
     * @param cpiClass
     *                The class of the api this adaptor implements.
     * @param adaptorClass
     *                The actual class of this adaptor, must be a subclass of
     *                cpiClass.
     * @param schemes
     *                The schemes recognized by this adaptor.
     */
    public Adaptor(String cpiName, Class<?> adaptorClass, String[] schemes) {
        this.cpi = cpiName;
        this.adaptorName = adaptorClass.getName();
        this.adaptorClass = adaptorClass;
    }

    Object newInstance(Class<?>[] parameterTypes, Object[] parameters)
            throws Throwable {
        Throwable t = null;

        // Set context classloader before calling constructor.
        // Some adaptors may need this because some libraries explicitly
        // use the context classloader. (jaxrpc).
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                adaptorClass.getClassLoader());
        try {
            Constructor<?> ctor = adaptorClass.getConstructor(parameterTypes);

            if (ctor == null) {
                throw new GATObjectCreationException(
                        "No correct contructor exists in adaptor");
            }

            if (parameters == null) {
                throw new GATObjectCreationException(
                        "Parameters array is null (internal error)");
            }

            return ctor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            // rethrow original exception
            t = e.getTargetException();
        } catch (Throwable e) {
            t = e;
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }

        throw t;
    }

    String getCpi() {
        return cpi;
    }

    String getName() {
        return adaptorName;
    }

    Class<?> getAdaptorClass() {
        return adaptorClass;
    }

    public String toString() {
        return getName();
    }

    public String getShortAdaptorClassName() {
        String shortAdaptorClassName = adaptorClass.getName();
        int index = shortAdaptorClassName.lastIndexOf(".");
        if (index > 0) {
            shortAdaptorClassName = shortAdaptorClassName.substring(index + 1);
        }

        return shortAdaptorClassName;
    }

    /**
     * Match the specified URI with the recognized schemes of this adaptor.
     * Quite liberal: if the specified URI is null, or the adaptor does not
     * explicitly specify its recognized schemes, return <code>true</code>.
     * 
     * @param param the URI to match against the recognized schemes.
     * @return whether there is a match.
     */
    public boolean matchScheme(URI param) {
        if (param == null) {
            return true;
        }
        if (schemes == null) {
            return true;
        }
        String scheme = param.getScheme();
        if (scheme == null) {
            scheme = "";
        }
        for (String s : schemes) {
            if (s.equalsIgnoreCase(scheme)) {
                return true;
            }
        }
        return false;
    }
}
