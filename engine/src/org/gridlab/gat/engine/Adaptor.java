/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gridlab.gat.GATObjectCreationException;

/**
 * @author rob
 */
class Adaptor {
    /** The fully qualified class name of the api we implement. */
    String cpi;

    /** The fully qualified class name of this adaptor. */
    String adaptorName;

    /** The actual class of this adaptor, must be a subclass of cpiClass. */
    Class<?> adaptorClass;

    /**
     * @param cpiClass
     *            The class of the api this adaptor implements.
     * @param adaptorClass
     *            The actual class of this adaptor, must be a subclass of
     *            cpiClass.
     */
    public Adaptor(String cpiName, Class<?> adaptorClass) {
        this.cpi = cpiName;
        this.adaptorName = adaptorClass.getName();
        this.adaptorClass = adaptorClass;
    }

//    boolean satisfies(Preferences p) {
//        boolean retVal = true;
//
//        Iterator<String> i = p.keySet().iterator();
//
//        while (i.hasNext()) {
//            String key = (String) i.next();
//            Object requestedValue = p.get(key);
//
//            if (this.preferences.containsKey(key)) {
//                Object adaptorValue = this.preferences.get(key);
//                boolean comparison = requestedValue.equals(adaptorValue);
//
//                if (comparison == false) {
//                    retVal = false;
//
//                    break;
//                }
//            } else {
//                //              todo - namespace for preferences
//                //                              retVal = false;
//                //                              break;
//            }
//        }
//
//        return retVal;
//    }

    Object newInstance(Class<?>[] parameterTypes, Object[] parameters)
            throws Throwable {
        Throwable t = null;

        // Set context classloader before calling constructor.
        // Some adaptors may need this because some libraries explicitly
        // use the context classloader. (jaxrpc).
        Thread.currentThread().setContextClassLoader(adaptorClass.getClassLoader());
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
        if(index > 0) {
            shortAdaptorClassName = shortAdaptorClassName.substring(index+1);
        }
        
        return shortAdaptorClassName;
    }
}
