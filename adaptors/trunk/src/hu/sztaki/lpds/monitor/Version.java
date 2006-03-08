package hu.sztaki.lpds.monitor;

/**
 * The <code>Version</code> class contains the version of the Mercury package
 * the Java interface was built for.
 *
 * This class is used by both the Java and the JNI component to test each
 * other's version number.
 *
 * @author G??bor Gomb??s
 * @version $Id: Version.java,v 1.4 2006/01/23 11:05:53 rob Exp $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public final class Version {
    /**
     * Returns the version of the Java interface package.
     *
     * @return                the version string.
     */
    public static final String getVersion() {
        return "2.3.1";
    }

    /**
     * Returns the version of the underlying JNI libraries.
     *
     * @return                the version string.
     */
    public static final native String getNativeVersion();
}
