/*
 * Created on Jan 7, 2005
 */
package org.gridlab.gat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author rob
 */
/**
 * This class implements URIs. It is API compatible with java.net.URI. However,
 * the standard Java class has a bug. The Java URI class does not work correctly
 * if you omit the hostname in a URI. For example: <BR>
 * file://&lt;hostname&gt;/<path><BR>
 * and <BR>
 * &lt;hostname&gt;== not set (empty string) <BR>
 * and <BR>
 * &lt;path&gt;== /bin/date <BR>
 * then the correct URI is <BR>
 * file:////bin/date <BR>
 * So four slashes in total after the "file:" <BR>
 * if the path would be a relative path such as foo/bar, the URI would be: <BR>
 * file:///foo/bar (three slashes because of the empty hostname). <BR>
 * However, the Java URI class getPath() method will return "/foo/bar" as the
 * path instead of "foo/bar"... <BR>
 * <BR>
 * <P>
 * Also note that relative paths in URIs are a bit ambiguous, especially with
 * the "any" scheme. It depends on the protocol (scheme) used which root is used
 * to resolve the path. So a URI with the "file" scheme will be relative to the
 * current working directory, while "ssh" might be relative to $HOME (it depends
 * on the settings of the ssh daemon of the remote machine). A "ftp" URI might
 * end up somewhere else, etc.
 * 
 * <p>
 * As far as we know, there is no good general solution for this problem. So,
 * try to use URIs with an absolute path as much as possible, and be careful
 * when you use URIs with relative paths.
 * 
 * <P>
 * One further note: for local files, file:///bla means the file bla in the
 * current directory, while file://hostname/bla means the file named bla
 * relative to the entry point for the host, just like with the any protocol.
 * The entry point in this case is assumed to be your home directory: the
 * user.home system property. The hostname can be either "localhost" or the real
 * hostname for your local machine.
 * 
 */
@SuppressWarnings("serial")
public class URI implements Serializable, Comparable<Object> {
    java.net.URI u;

    public URI(String s) throws URISyntaxException {
        u = new java.net.URI(new URIEncoder().encodeUri(s));
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

    /**
     * Check whether URI refers to the local machine. The difference between
     * this call and isLocal is that this call also checks if the host in the
     * URI is equal to the hostname of the local mahince or "localhost".
     * Another exception is when the URI specifies a port number. Then,
     * this is assumed to be a tunnel.
     */
    public boolean refersToLocalHost() {
        if (u.getHost() == null) {
            return true;
        }
        
        if (u.getPort() != -1) {
            return false;
        }

        if (u.getHost().equals("localhost")) {
            return true;
        }

        if (getLocalHostName().equals(u.getHost())) {
            return true;
        }

        if (getLocalCanonicalHostName().equals(u.getHost())) {
            return true;
        }

        String[] localhostIPs = getLocalHostIPs();
        for (String localhostIP : localhostIPs) {
            if (localhostIP.equals(u.getHost())) {
                return true;
            }
        }
        
        String[] localhostIPsFromHostName = getLocalHostIPsFromHostName();
        for (String localhostIP : localhostIPsFromHostName) {
            if (localhostIP.equals(u.getHost())) {
                return true;
            }
        }

        return false;
    }

    /* this is where the magic happens to fix SUNs bug.. */
    public String getPath() {
        String path = u.getPath();
        if (path == null) {
            return null;
        }

        path = new URIEncoder().decodeUri(u.getPath());

        if ((u.getScheme() == null) && (u.getHost() == null)) {
            return path;
        }

        path = path.substring(1);

        if (u.getHost() != null
                && (u.getHost().equals("localhost") || getLocalHostName()
                        .equals(u.getHost()))) {
            if (!path.startsWith(File.separator)) {
                // a relative path for a URI that has a hostname that is the
                // local host
                // this means relative to the entry point for this machine,
                // $HOME.

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

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if ((o instanceof URI)) {
            return u.equals(((URI) o).u);
        }

        if ((o instanceof java.net.URI)) {
            return u.equals(o);
        }

        return false;

        // I have no idea why this used to be here. It seems wrong. --Rob
        /*
         * URI other = (URI) arg0;
         * 
         * if ((other.getScheme() != null && other.getScheme().equals("any")) ||
         * (getScheme() != null && getScheme().equals("any"))) { String
         * tmpURIString = "file://"; if (getScheme() != null) { tmpURIString =
         * getScheme() + "://"; } tmpURIString += ((other.getUserInfo() == null) ? "" :
         * other.getUserInfo()); tmpURIString += ((other.getHost() == null) ? "" :
         * other.getHost()); tmpURIString += ((other.getPort() == -1) ? "" :
         * (":" + other.getPort())); tmpURIString += ("/" + other.getPath());
         * 
         * System.err.println("URI equals: created tmp URI: " + tmpURIString + ",
         * orig was: " + other + ", compare with: " + u + "."); boolean res =
         * u.toString().equals(tmpURIString); // System.err.println("result of
         * URI equals = " + res); return res; }
         */
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

    /**
     * gets the host componnent of the URI. If the URI refers to the local host,
     * this will try to get the local host name and return that. If that fails,
     * "localhost" is returned.
     * 
     * @return the host
     */
    public String resolveHost() {
        String host = getHost();

        if (host == null) {
            host = getLocalHostName();
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
     * @return returns the port specified in the URI, and default if none was
     *         specified.
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
        return u.getScheme();
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

    /**
     * Checks whether this URI is "compatible" with the given scheme. If this
     * URI has the same scheme, it is compatible. When this URI has "any" as
     * scheme, it is also compatible. If the scheme parameter is "any", this
     * method always returns true. No scheme is interpreted as a "file" scheme.
     * 
     * @param scheme
     *                the scheme to compare to
     * @return true: the URIs are compatible
     */
    public boolean isCompatible(String scheme) {
        if (scheme.equals("any")) {
            return true;
        }

        if (getScheme() == null) {
            return scheme.equals("file");
        }

        if (getScheme().equals("any")) {
            return true;
        }

        return getScheme().equals(scheme);
    }

    public void debugPrint() {
        System.err
                .println("URI: scheme = " + getScheme() + ", host = "
                        + getHost() + ", port = " + getPort() + ", path = "
                        + getPath());
        System.err.println("underlying: " + u);
    }

    private String getLocalHostName() {
        try {
            InetAddress a = InetAddress.getLocalHost();
            if (a != null) {
                return a.getHostName();
            }
        } catch (IOException e) {
            // ignore
        }
        return "localhost";
    }

    private String getLocalCanonicalHostName() {
        try {
            InetAddress a = InetAddress.getLocalHost();
            if (a != null) {
                return a.getCanonicalHostName();
            }
        } catch (IOException e) {
            // ignore
        }
        return "localhost";
    }

    private String[] getLocalHostIPs() {
        try {
            InetAddress[] all = InetAddress.getAllByName("localhost");
            String[] res = new String[all.length];
            for (int i = 0; i < all.length; i++) {
                res[i] = all[i].getHostAddress();
            }
            return res;
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    private String[] getLocalHostIPsFromHostName() {
        try {
            InetAddress a = InetAddress.getLocalHost();
            if (a != null) {
                InetAddress[] all = InetAddress.getAllByName(a.getHostName());
                String[] res = new String[all.length];
                for (int i = 0; i < all.length; i++) {
                    res[i] = all[i].getHostAddress();
                }
                return res;
            }
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    /*
     * Copyright 2000-2001,2004 The Apache Software Foundation.
     * 
     * Licensed under the Apache License, Version 2.0 (the "License"); you may
     * not use this file except in compliance with the License. You may obtain a
     * copy of the License at
     * 
     * http://www.apache.org/licenses/LICENSE-2.0
     * 
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
     * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
     * License for the specific language governing permissions and limitations
     * under the License.
     */

    /**
     * A utility class that encodes/decodes Strings into a valid URI format.
     */
    public class URIEncoder {
        private static final String ESCAPE_CHARS = "<>#%\"{}|\\^[]`";

        /**
         * Encodes a string according to RFC 2396. According to this spec, any
         * characters outside the range 0x20 - 0x7E must be escaped because they
         * are not printable characters, except for characters in the fragment
         * identifier. Even within this range a number of characters must be
         * escaped. This method will perform this escaping.
         * 
         * @param uri
         *                The URI to encode.
         * @return The encoded URI.
         */
        public String encodeUri(String uri) {
            StringBuffer result = new StringBuffer(2 * uri.length());
            encodeUri(uri, result);

            return result.toString();
        }

        /**
         * Encodes a string according to RFC 2396.
         * 
         * @param uri
         *                The URI to encode.
         * @param buf
         *                The StringBuffer that the encoded URI will be appended
         *                to.
         * @see #encodeUri(java.lang.String)
         */
        public void encodeUri(String uri, StringBuffer buf) {
            for (int i = 0; i < uri.length(); i++) {
                char c = uri.charAt(i);
                int cInt = c;

                if ((ESCAPE_CHARS.indexOf(c) >= 0) || (cInt <= 0x20)) {
                    // Escape character
                    buf.append('%');

                    String hexVal = Integer.toHexString(cInt);

                    // Ensure use of two characters
                    if (hexVal.length() == 1) {
                        buf.append('0');
                    }

                    buf.append(hexVal);
                } else {
                    buf.append(c);
                }
            }
        }

        /**
         * Decodes a string according to RFC 2396. According to this spec, any
         * characters outside the range 0x20 - 0x7E must be escaped because they
         * are not printable characters, except for any characters in the
         * fragment identifier. This method will translate any escaped
         * characters back to the original.
         * 
         * @param uri
         *                The URI to decode.
         * @return The decoded URI.
         */
        public String decodeUri(String uri) {
            StringBuffer result = new StringBuffer(uri.length());
            decodeUri(uri, result);

            return result.toString();
        }

        /**
         * Decodes a string according to RFC 2396.
         * 
         * @param uri
         *                The URI to decode.
         * @param buf
         *                The StringBuffer that the decoded URI will be appended
         *                to.
         * @see #decodeUri(java.lang.String)
         */
        public void decodeUri(String uri, StringBuffer buf) {
            // Search for a fragment identifier
            int indexOfHash = uri.indexOf('#');

            if (indexOfHash == -1) {
                // No fragment identifier
                _decodeUri(uri, buf);
            } else {
                // Fragment identifier found
                String baseUri = uri.substring(0, indexOfHash);
                String fragId = uri.substring(indexOfHash);

                _decodeUri(baseUri, buf);
                buf.append(fragId);
            }
        }

        private void _decodeUri(String uri, StringBuffer buf) {
            int percentIdx = uri.indexOf('%');
            int startIdx = 0;

            while (percentIdx != -1) {
                buf.append(uri.substring(startIdx, percentIdx));

                // The two character following the '%' contain a hexadecimal
                // code for the original character, i.e. '%20'
                String xx = uri.substring(percentIdx + 1, percentIdx + 3);
                int c = Integer.parseInt(xx, 16);
                buf.append((char) c);

                startIdx = percentIdx + 3;

                percentIdx = uri.indexOf('%', startIdx);
            }

            buf.append(uri.substring(startIdx));
        }
    }

}
