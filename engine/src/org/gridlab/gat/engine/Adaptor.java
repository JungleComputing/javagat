/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;

/**
 * @author rob
 */
class Adaptor {
    /** The fully qualified class name of the api we implement. */
    String cpi;

    /** The class of the api this adaptor implements. */
    Class cpiClass;

    /** The fully qualified class name of this adaptor. */
    String adaptorName;

    /** The actual class of this adaptor, must be a subclass of cpiClass. */
    Class adaptorClass;

    /** Preferences associated with this adaptor. */
    Preferences preferences;

    /**
     * @param cpiClass
     *            The class of the api this adaptor implements.
     * @param adaptorClass
     *            The actual class of this adaptor, must be a subclass of
     *            cpiClass.
     * @param preferences
     *            Preferences associated with this adaptor.
     */
    public Adaptor(Class cpiClass, Class adaptorClass, Preferences preferences) {
        this.cpi = cpiClass.getName();
        this.cpiClass = cpiClass;
        this.adaptorName = adaptorClass.getName();
        this.adaptorClass = adaptorClass;
        this.preferences = preferences;
    }

    boolean satisfies(Preferences p) {
        boolean retVal = true;

        Iterator i = p.keySet().iterator();

        while (i.hasNext()) {
            Object key = i.next();
            Object requestedValue = p.get(key);

            if (this.preferences.containsKey(key)) {
                Object adaptorValue = this.preferences.get(key);
                boolean comparison = requestedValue.equals(adaptorValue);

                if (comparison == false) {
                    retVal = false;

                    break;
                }
            } else {
                //              todo - namespace for preferences
                //				retVal = false;
                //				break;
            }
        }

        return retVal;
    }

    Object newInstance(Class[] parameterTypes, Object[] parameters)
            throws Throwable {
        Throwable t = null;

        try {
            Constructor ctor = adaptorClass.getConstructor(parameterTypes);

            if (ctor == null) {
                throw new GATObjectCreationException(
                    "No correct contructor extists in adaptor");
            }

            if (parameters == null) {
                throw new GATObjectCreationException(
                    "Parameters array is null (internal error)");
            }

            return ctor.newInstance(parameters);
        } catch (InvocationTargetException e) {
            if (GATEngine.DEBUG) {
                System.err.println("Adaptor constructor threw exception: "
                    + e.getTargetException());
                e.getTargetException().printStackTrace();
            }

            // rethrow original exception
            t = e.getTargetException();
        } catch (Throwable e) {
            if (GATEngine.DEBUG) {
                System.err
                    .println("Could not construct adaptor object instance: "
                        + e + ": " + e.getMessage());
                e.printStackTrace();
            }

            t = e;
        }

        throw t;
    }

    String getCpi() {
        return cpi;
    }

    Class getCpiClass() {
        return cpiClass;
    }

    String getName() {
        return adaptorName;
    }

    Class getAdaptorClass() {
        return adaptorClass;
    }
    
    public String toString() {
        return getName();
    }

    public String getShortCpiName() {
        String shortCpiName = cpiClass.getName();
        int index = shortCpiName.lastIndexOf(".");
        if(index > 0) {
            shortCpiName = shortCpiName.substring(index+1);
        }
        
        // clip of the "Cpi"
        return shortCpiName.substring(0, shortCpiName.length()-3);
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
