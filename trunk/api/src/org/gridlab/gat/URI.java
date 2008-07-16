/*
 */
package org.gridlab.gat;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class implements URIs. It is API compatible with the java.net.{@link java.net.URI}.
 * However, URIs have a slightly different meaning in JavaGAT. The java.net.{@link java.net.URI}
 * only accepts absolute path names for URIs with a scheme and a host. The
 * JavaGAT URI also accepts relative path names for URIs with a scheme and a
 * host. This may be useful for protocols that have an entry point (the working
 * directory after a connection has been established), which is not the same as
 * the root of the file system.
 * <p>
 * For instance, if you ssh to a machine, your entry point is typically your
 * $HOME directory, which might be the only place on that machine where you're
 * allowed to do something. It might also be the case that you don't know
 * beforehand the path of your $HOME (it might start with '<code>/home/user</code>'
 * or '<code>/home3/user</code>', etc.). Although you don't know the
 * absolute path name to a file '<code>$HOME/somedir/somefile</code>' in
 * your $HOME, you do know the path name which is relative to the ssh entry
 * point.
 * <p>
 * Now let's see what would happen if you do want to specify this URI using the
 * java.net.{@link java.net.URI}:
 * <p>
 * <code>any://myhost/somedir/somefile</code>
 * <p>
 * According to the java.net.{@link java.net.URI}, this URI can be split up in
 * the following parts:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>myhost</code><br>
 * path: <code>/somedir/somefile</code><br>
 * <p>
 * It's impossible to specify a path like this:
 * <p>
 * path: <code>somedir/somefile</code><br>
 * <p>
 * We can try by leaving the first '/' of the path out. The URI would then be:
 * <p>
 * <code>any://myhostsomedir/somefile</code>
 * <p>
 * Which would be split up in these parts:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>myhostsomedir</code><br>
 * path: <code>/somefile</code><br>
 * <p>
 * Which is also not what we want. Therefore the JavaGAT URI semantics aren't
 * exactly the same as the java.net.{@link java.net.URI}. The JavaGAT URI
 * treats the first '/' of the java.net.{@link java.net.URI} path not as part
 * of the path, but as a separator between the previous parts of the URI and the
 * path. The 'real' path starts after the first '/'. Let's show a few examples
 * of the JavaGAT URI. Suppose we want to specify the same path
 * <code>somedir/somefile</code> using the JavaGAT URI. This would be:
 * <p>
 * <code>any://myhost/somedir/somefile</code>
 * <p>
 * According to JavaGAT URI, this URI can be split up in the following parts:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>myhost</code><br>
 * path: <code>somedir/somefile</code><br>
 * <p>
 * Which is exactly what we want. If we did want to specify the <emph>absolute</emph>
 * path <code>/somedir/somefile</code>, then the URI would be:
 * <p>
 * <code>any://myhost//somedir/somefile</code>
 * <p>
 * According to JavaGAT URI, this URI can be split up in the following parts:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>myhost</code><br>
 * path: <code>/somedir/somefile</code><br>
 * <p>
 * A few more examples will show correct JavaGAT URIs and how they're split up:
 * <p>
 * This JavaGAT URI '<code>/somedir/somefile</code>' splits up into:
 * <p>
 * scheme: <code>null</code><br>
 * host: <code>null</code><br>
 * path: <code>/somedir/somefile</code><br>
 * <p>
 * This JavaGAT URI '<code>file:somedir/somefile</code>' splits up into:
 * <p>
 * scheme: <code>file</code><br>
 * host: <code>null</code><br>
 * path: <code>somedir/somefile</code><br>
 * <p>
 * This JavaGAT URI '<code>any:////somedir/somefile</code>' splits up into:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>null</code><br>
 * path: <code>/somedir/somefile</code><br>
 * <p>
 * This JavaGAT URI '<code>any:///somedir/somefile</code>' splits up into:
 * <p>
 * scheme: <code>any</code><br>
 * host: <code>null</code><br>
 * path: <code>somedir/somefile</code><br>
 * <p>
 * JavaGAT supports the "any" protocol, which means that any protocol may be
 * used to retrieve this URI.
 * <p>
 * <b>Please be careful with using the "any" protocol in combination with
 * relative path names!</b> Suppose protocol A has the entry point '<code>/home/user</code>'
 * and protocol B has the entry point '/tmp'. Then the URI '<code>any://myhost/somedir/somefile</code>'
 * would point to two different location depending on the protocol. So, for
 * protocol A it would be resolved to '<code>/home/user/somedir/somefile</code>'
 * as for B it would be '<code>/tmp/somedir/somefile</code>'.
 * <p>
 * As far as we know, there is no good general solution for this problem. So,
 * try to use URIs with an absolute path as much as possible, and be careful
 * when you use URIs with relative paths.
 * <p>
 * One further note: for local files, '<code>file:///bla</code>' means the
 * file '<code>bla</code>' in the current directory (which is the directory
 * where the jvm is started), while '<code>file://hostname/bla</code>' means
 * the file named '<code>bla</code>' relative to the entry point for the
 * host, just like with the "any" protocol. The entry point in this case is
 * assumed to be your home directory: the user.home system property. The
 * hostname can be either "localhost" or the real hostname for your local
 * machine.
 * 
 * 
 * @author rob
 */
@SuppressWarnings("serial")
public class URI implements Serializable, Comparable<Object> {
    java.net.URI u;

    /**
     * @see java.net.URI#URI(String)
     * @param s
     *                The string to be parsed into a URI
     * @throws URISyntaxException
     */
    public URI(String s) throws URISyntaxException {
        u = new java.net.URI(new URIEncoder().encodeUri(s));
    }

    /**
     * Constructs a JavaGAT URI out of a {@link java.net.URI}.
     * 
     * @param u
     *                the {@link java.net.URI}.
     */
    public URI(java.net.URI u) {
        this.u = u;
    }

    public URI(String scheme, String userInfo, String host, int port,
            String path, String query, String fragment)
            throws URISyntaxException {
        // add an extra slash if the scheme is not null!
        // this.u = new java.net.URI(scheme, userInfo, host, port,
        // (scheme == null) ? path : "/" + path, query, fragment);
        this.u = new java.net.URI(scheme, userInfo, host, port, path, query,
                fragment);
    }

    /**
     * Creates a URI by parsing the given string.
     * 
     * @see java.net.URI#create(String)
     * 
     * @param s
     *                The string to be parsed into a URI
     * @return The new URI
     * @throws URISyntaxException
     */
    public static URI create(String s) throws URISyntaxException {
        return new URI(s);
    }

    /**
     * Small, but not complete check whether this URI refers to the local
     * machine. If the URI contains a hostname it is assumed to be remote. The
     * {@link #refersToLocalHost()} provides a more extensive check.
     * 
     * @return true if the URI refers to the localhost, false otherwise.
     */
    public boolean isLocal() {
        return u.getHost() == null;
    }

    /**
     * Extensive check whether URI refers to the local machine. This method
     * checks for the hostname "localhost", the short hostname, the full
     * hostname and the ip address. When the URI specifies a port number, it is
     * assumed to be a tunnel and this call will return FALSE.
     * 
     * @return true if the URI refers to the localhost, false otherwise
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
        if (localhostIPs != null) {
            for (String localhostIP : localhostIPs) {
                if (localhostIP.equals(u.getHost())) {
                    return true;
                }
            }
        }

        String[] localhostIPsFromHostName = getLocalHostIPsFromHostName();
        if (localhostIPsFromHostName != null) {
            for (String localhostIP : localhostIPsFromHostName) {
                if (localhostIP.equals(u.getHost())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the decoded path component of this URI.
     * 
     * @see java.net.URI#getPath()
     * 
     * @return The decoded path component of this URI, or <code>null</code> if
     *         the path is undefined
     */
    public String getPath() {
        /* this is where the magic happens to fix SUNs bug.. */
        String path = getUnresolvedPath();

        if (u.getHost() != null
                && (u.getHost().equals("localhost") || getLocalHostName()
                        .equals(u.getHost()))) {
            if (!path.startsWith("/")) {
                // a relative path for a URI that has a hostname that is the
                // local host
                // this means relative to the entry point for this machine,
                // $HOME.

                String home = new java.io.File(System.getProperty("user.home"))
                        .toURI().getPath();

                if (home == null) {
                    home = "";
                }

                path = home + path;
            }
        }

        return path;
    }

    public String getUnresolvedPath() {
        String path = u.getPath();
        if (u.getScheme() != null && u.getHost() == null
                && u.getSchemeSpecificPart() != null) {
            path = u.getSchemeSpecificPart();
            if (path.startsWith("///")) {
                path = path.substring(3);
            }
            // silly fix, the '/' gets chopped of anyway.
            path = "/" + path;
        }
        if (path == null || path.equals("")) {
            return null;
        }

        if ((u.getScheme() == null) && (u.getHost() == null)) {
            return path;
        }

        return path.substring(1);
    }

    /**
     * Compares this URI to another object, which must be a URI.
     * 
     * @see java.net.URI#compareTo(java.net.URI)
     * 
     * @param other
     *                The object to which this URI is to be compared
     * @return A negative integer, zero, or a positive integer as this URI is
     *         less than, equal to, or greater than the given URI
     */
    public int compareTo(Object other) {
        if (other instanceof java.net.URI) {
            return u.compareTo((java.net.URI) other);
        }

        if (other instanceof URI) {
            return u.compareTo(((URI) other).u);
        }

        return -1;
    }

    /**
     * Tests this URI for equality with another object.
     * 
     * @see java.net.URI#equals(Object)
     * @param o
     *                The object to which this URI is to be compared
     * @return true if, and only if, the given object is a URI that is identical
     *         to this URI
     */
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

    /**
     * Returns the decoded authority component of this URI.
     * 
     * @see java.net.URI#getAuthority()
     * @return The decoded authority component of this URI, or <code>null</code>
     *         if the authority is undefined
     */
    public String getAuthority() {
        return u.getAuthority();
    }

    /**
     * Returns the decoded fragment component of this URI.
     * 
     * @see java.net.URI#getFragment()
     * @return The decoded fragment component of this URI, or <code>null</code>
     *         if the fragment is undefined
     */
    public String getFragment() {
        return u.getFragment();
    }

    /**
     * Returns the host component of this URI.
     * 
     * @see java.net.URI#getHost()
     * @return The host component of this URI, or <code>null</code> if the
     *         host is undefined
     */
    public String getHost() {
        return u.getHost();
    }

    /**
     * Returns the host component of the URI with a resolved host. If the URI
     * refers to the local host, this will try to get the local host name and
     * return that. If that fails, "localhost" is returned.
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

    /**
     * Returns the port number of this URI.
     * 
     * @see java.net.URI#getPort()
     * @return The port component of this URI, or -1 if the port is undefined
     */
    public int getPort() {
        return u.getPort();
    }

    /**
     * Returns the port number of this URI or the default port if the port is
     * undefined.
     * 
     * @param defaultPort
     *                the default port
     * @return the port number of this URI or the default port if the port is
     *         undefined
     */
    public int getPort(int defaultPort) {
        int port = getPort();

        if (port < 0) {
            return defaultPort;
        }

        return port;
    }

    /**
     * Returns the decoded query component of this URI.
     * 
     * 
     * @see java.net.URI#getQuery()
     * @return The decoded query component of this URI, or <code>null</code>
     *         if the query is undefined
     */
    public String getQuery() {
        return u.getQuery();
    }

    /**
     * Returns the raw authority component of this URI.
     * 
     * @see java.net.URI#getRawAuthority()
     * @return The raw authority component of this URI, or <code>null</code>
     *         if the authority is undefined
     */
    public String getRawAuthority() {
        return u.getRawAuthority();
    }

    /**
     * Returns the raw fragment component of this URI.
     * 
     * @see java.net.URI#getRawFragment()
     * @return The raw fragment component of this URI, or <code>null</code> if
     *         the fragment is undefined
     */
    public String getRawFragment() {
        return u.getRawFragment();
    }

    /**
     * Returns the raw path component of this URI.
     * 
     * @see java.net.URI#getRawPath()
     * @return The path component of this URI, or <code>null</code> if the
     *         path is undefined
     */
    public String getRawPath() {
        return u.getRawPath();
    }

    /**
     * Returns the raw query component of this URI.
     * 
     * @see java.net.URI#getRawQuery()
     * @return The raw query component of this URI, or <code>null</code> if
     *         the query is undefined
     */
    public String getRawQuery() {
        return u.getRawQuery();
    }

    /**
     * Returns the raw scheme-specific part of this URI.
     * 
     * @see java.net.URI#getRawSchemeSpecificPart()
     * @return The raw scheme-specific part of this URI (never <code>null</code>)
     */
    public String getRawSchemeSpecificPart() {
        return u.getRawSchemeSpecificPart();
    }

    /**
     * Returns the raw user-information component of this URI.
     * 
     * @see java.net.URI#getRawUserInfo()
     * @return The raw user-information component of this URI, or
     *         <code>null</code> if the user information is undefined
     */
    public String getRawUserInfo() {
        return u.getRawUserInfo();
    }

    /**
     * Returns the scheme component of this URI.
     * 
     * @see java.net.URI#getScheme()
     * @return The scheme component of this URI, or <code>null</code> if the
     *         scheme is undefined
     */
    public String getScheme() {
        return u.getScheme();
    }

    /**
     * Returns the decoded scheme-specific part of this URI.
     * 
     * @see java.net.URI#getSchemeSpecificPart()
     * @return The decoded scheme-specific part of this URI (never
     *         <code>null</code>)
     */
    public String getSchemeSpecificPart() {
        return u.getSchemeSpecificPart();
    }

    /**
     * Returns the decoded user-information component of this URI.
     * 
     * @see java.net.URI#getUserInfo()
     * @return The decoded user-information component of this URI, or
     *         <code>null</code> if the user information is undefined
     */
    public String getUserInfo() {
        return u.getUserInfo();
    }

    /**
     * Returns a hash-code value for this URI.
     * 
     * @see java.net.URI#hashCode()
     * @return A hash-code value for this URI
     */
    public int hashCode() {
        return u.hashCode();
    }

    /**
     * Tells whether or not this URI has an absolute path.
     * 
     * @return true if this URI has an absolute path, false otherwise.
     */
    public boolean hasAbsolutePath() {
        if (u.toString().startsWith("/")) {
            return true;
        }
        if (u.getPath() == null) {
            return false;
        } else {
            return u.getPath().startsWith("//");
        }
    }

    /**
     * Tells whether or not this URI is absolute.
     * 
     * @see java.net.URI#isAbsolute()
     * @return true if, and only if, this URI is absolute
     */
    public boolean isAbsolute() {
        return u.isAbsolute();
    }

    /**
     * Tells whether or not this URI is opaque.
     * 
     * @see java.net.URI#isOpaque()
     * @return true if, and only if, this URI is opaque
     */
    public boolean isOpaque() {
        return u.isOpaque();
    }

    /**
     * Normalizes this URI's path.
     * 
     * @see java.net.URI#normalize()
     * @return A URI equivalent to this URI, but whose path is in normal form
     */
    public URI normalize() {
        return new URI(u.normalize());
    }

    /**
     * Attempts to parse this URI's authority component, if defined, into
     * user-information, host, and port components.
     * 
     * @see java.net.URI#parseServerAuthority()
     * @return A URI whose authority field has been parsed as a server-based
     *         authority
     * @throws URISyntaxException
     *                 If the authority component of this URI is defined but
     *                 cannot be parsed as a server-based authority according to
     *                 RFC 2396
     */
    public URI parseServerAuthority() throws URISyntaxException {
        return new URI(u.parseServerAuthority());
    }

    /**
     * Relativizes the given URI against this URI.
     * 
     * @see java.net.URI#relativize(java.net.URI)
     * @param arg0
     *                The URI to be relativized against this URI
     * @return The resulting URI
     */
    public URI relativize(java.net.URI arg0) {
        return new URI(u.relativize(arg0));
    }

    /**
     * Constructs a new URI by parsing the given string and then resolving it
     * against this URI.
     * 
     * @see java.net.URI#resolve(String)
     * @param arg0
     *                The string to be parsed into a URI
     * @return The resulting URI
     */
    public URI resolve(String arg0) {
        return new URI(u.resolve(arg0));
    }

    /**
     * Resolves the given URI against this URI.
     * 
     * @see java.net.URI#resolve(java.net.URI)
     * @param arg0
     *                The URI to be resolved against this URI
     * @return The resulting URI
     */
    public URI resolve(java.net.URI arg0) {
        return new URI(u.resolve(arg0));
    }

    /**
     * Returns the content of this URI as a US-ASCII string.
     * 
     * @see java.net.URI#toASCIIString()
     * @return The string form of this URI, encoded as needed so that it only
     *         contains characters in the US-ASCII charset
     */
    public String toASCIIString() {
        return u.toASCIIString();
    }

    /**
     * Returns the content of this URI as a string.
     * 
     * @see java.net.URI#toString()
     * @return The string form of this URI
     */
    public String toString() {
        return new URIEncoder().decodeUri(u.toString());
    }

    /**
     * Constructs a URL from this URI.
     * 
     * @see java.net.URI#toURL()
     * @return A URL constructed from this URI
     * @throws MalformedURLException
     *                 If a protocol handler for the URL could not be found, or
     *                 if some other error occurred while constructing the URL
     */
    public URL toURL() throws MalformedURLException {
        return u.toURL();
    }

    /**
     * Constructs a java.net.{@link java.net.URI} out of this URI.
     * 
     * @return the java.net.{@link java.net.URI} similar to this URI.
     */
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

    public URI setScheme(String scheme) throws URISyntaxException {
        return new URI(scheme, getUserInfo(), getHost(), getPort(-1), u
                .getPath(), getQuery(), getFragment());
    }

    public URI setUserInfo(String userInfo) throws URISyntaxException {
        return new URI(getScheme(), userInfo, getHost(), getPort(-1), u
                .getPath(), getQuery(), getFragment());
    }

    public URI setHost(String host) throws URISyntaxException {
        // host = "" like in any:///bla returns also null
        if (getHost() == null && !getSchemeSpecificPart().startsWith("///")) {
            return new URI(getScheme(), getUserInfo(), host, getPort(-1), "/"
                    + u.getPath(), getQuery(), getFragment());
        }
        return new URI(getScheme(), getUserInfo(), host, getPort(-1), u
                .getPath(), getQuery(), getFragment());
    }

    public URI setPort(int port) throws URISyntaxException {
        return new URI(getScheme(), getUserInfo(), getHost(), port,
                u.getPath(), getQuery(), getFragment());
    }

    public URI setPath(String path) throws URISyntaxException {
        return new URI(getScheme(), getUserInfo(), getHost(), getPort(-1), "/"
                + path, getQuery(), getFragment());
    }

    public URI setQuery(String query) throws URISyntaxException {
        return new URI(getScheme(), getUserInfo(), getHost(), getPort(-1), u
                .getPath(), query, getFragment());
    }

    public URI setFragment(String fragment) throws URISyntaxException {
        return new URI(getScheme(), getUserInfo(), getHost(), getPort(-1), u
                .getPath(), getQuery(), fragment);
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
