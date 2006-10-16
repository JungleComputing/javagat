/*
 * Copyright 2000-2001,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridlab.gat.util;

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
     * @param uri The URI to encode.
     * @return The encoded URI.
     **/
    public static String encodeUri(String uri) {
        StringBuffer result = new StringBuffer(2 * uri.length());
        encodeUri(uri, result);

        return result.toString();
    }

    /**
     * Encodes a string according to RFC 2396.
     *
     * @param uri The URI to encode.
     * @param buf The StringBuffer that the encoded URI will be appended to.
     * @see #encodeUri(java.lang.String)
     **/
    public static void encodeUri(String uri, StringBuffer buf) {
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
     * are not printable characters, except for any characters in the fragment
     * identifier. This method will translate any escaped characters back to
     * the original.
     *
     * @param uri The URI to decode.
     * @return The decoded URI.
     **/
    public static String decodeUri(String uri) {
        StringBuffer result = new StringBuffer(uri.length());
        decodeUri(uri, result);

        return result.toString();
    }

    /**
     * Decodes a string according to RFC 2396.
     *
     * @param uri The URI to decode.
     * @param buf The StringBuffer that the decoded URI will be appended to.
     * @see #decodeUri(java.lang.String)
     **/
    public static void decodeUri(String uri, StringBuffer buf) {
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

    private static void _decodeUri(String uri, StringBuffer buf) {
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
