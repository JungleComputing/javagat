/*
 * Created on Jan 7, 2005
 */
package org.gridlab.gat;

import ibis.util.IPUtils;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.gridlab.gat.util.URIEncoder;

/**
 * @author rob
 */
/** This class implements URIs. It is API compatible with java.net.URI.
 * However,
 * the standard Java class has a bug. The Java URI class does not work correctly
 * if you omit the hostname in a URI. For example: <BR>
 * file:// <hostname>/ <path><BR>
 * and <BR>
 * <hostname>== not set (empty string) <BR>
 * and <BR>
 * <path>== /bin/date <BR>
 * then the correct URI is <BR>
 * file:////bin/date <BR>
 * So four slashes in total after the "file:" <BR>
 * if the path would be a relative path such as foo/bar, the URI would be: <BR>
 * file:///foo/bar (three slashes because of the empty hostname). <BR>
 * However, the Java URI class getPath() method will return "/foo/bar" as the
 * path instead of "foo/bar"... <BR><BR>
 * <P>Also note that relative paths in URIs are a bit ambiguous, especially with the "any" scheme. 
 * It depends on the protocol (scheme) used which root is used to resolve the path.
 * So a URI with the "file" scheme will be relative to the current working directory, while "ssh" might be
 * relative to $HOME (it depends on the settings of the ssh daemon of the remote machine).
 * A "ftp" URI might end up somewhere else, etc.

 * <p>As far as we know, there is no good general solution for this problem. So, try to use URIs with an absolute path
 * as much as possible, and be careful when you use URIs with relative paths. 
 * 
 * <P>One further note: for local files, file:///bla means the file bla in the current directory,
 * while file://hostname/bla means the file named bla relative to the entry point for the host, just like with the any protocol.
 * The entry point in this case is assumed to be your home directory: the user.home system property.
 * The hostname can be either "localhost" or the real hostname for your local machine. 
 *  
 */
public class URI implements Serializable, Comparable {
    java.net.URI u;

    public URI(String s) throws URISyntaxException {
        u = new java.net.URI(URIEncoder.encodeUri(s));
    }

    public URI(java.net.URI u) {
        this.u = u;
    }

    public static URI create(String s) throws URISyntaxException {
        return new URI(s);
    }

    /** Check whether URI refers to the local machine */
    public boolean isLocal() {
        return u.getHost() == null;
    }

    /** Check whether URI refers to the local machine. The difference between this 
     * call and isLocal is that this call also checks if the host in the URI is 
     * equal to the hostname of the local mahince or "localhost" */
    public boolean refersToLocalHost() {
        if (u.getHost() == null) {
            return true;
        }

        if (u.getHost().equals("localhost")) {
            return true;
        }

        if (IPUtils.getLocalHostName().equals(u.getHost())) {
            return true;
        }

        return false;
    }

    /* this is where the magic happens to fix SUNs bug.. */
    public String getPath() {
        String path = URIEncoder.decodeUri(u.getPath());

        if ((u.getScheme() == null) && (u.getHost() == null)) {
            return path;
        }

        path = path.substring(1);

        if (u.getHost() != null && (u.getHost().equals("localhost") || IPUtils.getLocalHostName().equals(u.getHost()))) {
            if(!path.startsWith(File.separator)) {
                // a relative path for a URI that has a hostname that is the local host 
                // this means relative to the entry point for this machine, $HOME.

                String home = System.getProperty("user.home");

                if (home == null) {
                    home = "";
                } else {
                    home += File.separator;
                }

                path = home + path;
            }
        }
        
        return path;
    }

    public int compareTo(Object other) {
        if (other instanceof java.net.URI) {
            return u.compareTo((java.net.URI) other);
        }

        if (other instanceof URI) {
            return u.compareTo(((URI) other).u);
        }

        return -1;
    }

    public boolean equals(Object arg0) {
        if(arg0 == null) {
            return false;
        }
        
        if (!(arg0 instanceof URI)) {
            return false;
        }
        
        URI other = (URI) arg0;

        if (other.getScheme().equals("any") || getScheme().equals("any")) {
            String tmpURIString = getScheme() + "://";
            tmpURIString += ((other.getUserInfo() == null) ? "" : other
                .getUserInfo());
            tmpURIString += ((other.getHost() == null) ? "" : other.getHost());
            tmpURIString += ((other.getPort() == -1) ? "" : (":" + other
                .getPort()));
            tmpURIString += ("/" + other.getPath());

            //	        System.err.println("URI equals: created tmp URI: " + tmpURIString + ", orig was: " + other + ", compare with: " + u + ".");
            boolean res = u.toString().equals(tmpURIString);

            //	        System.err.println("result of URI equals = " + res);
            return res;
        }

        return u.equals(arg0);
    }

    public String getAuthority() {
        return u.getAuthority();
    }

    public String getFragment() {
        return u.getFragment();
    }

    public String getHost() {
        return u.getHost();
    }

    /** gets the host componnent of the URI.
     * If the URI refers to the local host, this will try to get the local host name and return that.
     * If that fails, "localhost" is returned.
     *
     * @return the host
     */
    public String resolveHost() {
        String host = getHost();

        if (host == null) {
            host = IPUtils.getLocalHostName();
        }

        if ((host == null) || (host.length() == 0)) {
            return "localhost";
        }

        return host;
    }

    public int getPort() {
        return u.getPort();
    }

    /**
     *
     * @return returns the port specified in the URI, and default if none was specified.
     */
    public int getPort(int defaultPort) {
        int port = getPort();

        if (port < 0) {
            return defaultPort;
        }

        return port;
    }

    public String getQuery() {
        return u.getQuery();
    }

    public String getRawAuthority() {
        return u.getRawAuthority();
    }

    public String getRawFragment() {
        return u.getRawFragment();
    }

    public String getRawPath() {
        return u.getRawPath();
    }

    public String getRawQuery() {
        return u.getRawQuery();
    }

    public String getRawSchemeSpecificPart() {
        return u.getRawSchemeSpecificPart();
    }

    public String getRawUserInfo() {
        return u.getRawUserInfo();
    }

    public String getScheme() {
        String scheme = u.getScheme();

        /*
         if (scheme == null) {
         if (getHost() == null) {
         return "file";
         } else {
         return "any";
         }
         }
         */
        return scheme;
    }

    public String getSchemeSpecificPart() {
        return u.getSchemeSpecificPart();
    }

    public String getUserInfo() {
        return u.getUserInfo();
    }

    public int hashCode() {
        return u.hashCode();
    }

    public boolean isAbsolute() {
        return u.isAbsolute();
    }

    public boolean isOpaque() {
        return u.isOpaque();
    }

    public URI normalize() {
        return new URI(u.normalize());
    }

    public URI parseServerAuthority() throws URISyntaxException {
        return new URI(u.parseServerAuthority());
    }

    public URI relativize(java.net.URI arg0) {
        return new URI(u.relativize(arg0));
    }

    public URI resolve(String arg0) {
        return new URI(u.resolve(arg0));
    }

    public URI resolve(java.net.URI arg0) {
        return new URI(u.resolve(arg0));
    }

    public String toASCIIString() {
        return u.toASCIIString();
    }

    public String toString() {
        return u.toString();
    }

    public URL toURL() throws MalformedURLException {
        return u.toURL();
    }

    public java.net.URI toJavaURI() {
        return u;
    }

    /** Checks whether this URI is "compatible" with the given scheme
     * If this URI has the same scheme, it is compatible.
     * When this URI has "any" as scheme, or no scheme at all, it is also
     * compatible.
     *
     * @param scheme the scheme to compare to
     * @return true: the URIs are compatible
     */
    public boolean isCompatible(String scheme) {
        if ((getScheme() == null) || getScheme().equals("any")) {
            return true;
        }

        if (getScheme().equals(scheme)) {
            return true;
        }

        return false;
    }

    public void debugPrint() {
        System.err.println("URI: scheme = " + getScheme() + ", host = "
            + getHost() + ", port = " + getPort() + ", path = " + getPath());
        System.err.println("underlying: " + u);
    }
}
