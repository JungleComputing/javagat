package org.gridlab.gat.net;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * An instance of this class represents the location of an abstract or physical 
 * resource. The location of an abstract or physical resource is represented by 
 * a URI as defined by the standards
 * <ul>
 *   <li> RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax </li>
 *   <li> RFC 2732: Format for Literal IPv6 Addresses in URLs. </li>
 * </ul>
 * One should refer to these standards to determine the allowed forms for URIs. 
 * This class provides a means to create a Location instance from a ``URI in 
 * String form,'' methods for accessing the various components of the contained 
 * URI, and various other utility methods.
 */
public class Location
{
    /**
     * This member variable represents the contained URI
     */
     private URI uri = null;
     
 
    /**
     * Constructs a Location  instance by parsing the given string as a URI.
     * <p>
     * This constructor parses the given string exactly as specified by the
     * grammar in RFC 2396, Appendix A, except IPv6 addresses are permitted
     * for the host component. An IPv6 address must be enclosed in square
     * brackets (`[' and `]') as specified by RFC 2732. The IPv6 address
     * itself must parse according to RFC 2373. IPv6 addresses are further
     * constrained to describe no more than sixteen bytes of address
     * information, a constraint implicit in RFC 2373 but not expressible in
     * the grammar.
     *
     * @param URI The java.lang.String to be parsed into a URI
     * @throws java.net.URISyntaxException Thrown upon problems parsing the 
     *         passed String 
     */
     public Location(String URI) throws URISyntaxException
     {
         uri = new URI( URI );
     }
     
    /**
     * Tests this Location for equality with the passed Object.
     * <p>
     * If the given object is not a Location, then this method immediately
     * returns false.
     * <p>
     * For two Locations to be considered equal requires that either both
     * are opaque or both are hierarchical. Their schemes must either both
     * be undefined or else be equal without regard to case, and similarly
     * for their fragments. 
     * <p>
     * For two opaque Locations to be considered equal, their
     * scheme-specific parts must be equal.
     * <p>
     * For two hierarchical Locations to be considered equal, their paths
     * must be equal and their queries must either both be undefined or else
     * be equal.  Their authorities must either both be undefined, or both
     * be registry-based, or both be server-based.  If their authorities are
     * defined and are registry-based, then they must be equal.  If their
     * authorities are defined and are server-based, then their hosts must
     * be equal without regard to case, their port numbers must be equal,
     * and their user-information components must be equal.
     * <p>
     * When testing the user-information, path, query, fragment, authority,
     * or scheme-specific parts of two Locations for equality, the raw forms
     * rather than the encoded forms of these components are compared and
     * the hexadecimal digits of escaped octets are compared without regard
     * to case.
     *
     * @param object The java.lang.Object to test for equality
     * @return A boolean indicating equality
     */
     public boolean equals(Object object)
     {
         Location location = null;
         
         if(false == (object instanceof Location))
           return false;
           
         location = (Location) object;
         
         return uri.equals( location.uri );
     }
     
     /**
      * Returns the decoded authority component of this Location.
      * <p>
      * A sequence of escaped octets is decoded by replacing it with the
      * sequence of characters that it represents in the UTF-8 character set.
      * UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      * any quoted US-ASCII characters as well as that of decoding any encoded
      * non-US-ASCII characters.  If a decoding error occurs when decoding the
      * escaped octets then the erroneous octets are replaced by
      * "\uFFFD", the Unicode replacement character.
      * <p>
      * The string returned by this method is equal to that returned by the
      * GetRawAuthority method except that all sequences of escaped octets are
      * decoded.
      *
      * @return The decoded authority component of this Location, 
      *          a java.lang.String, or null if authority is undefined.
      */
      public String getAuthority()
      {
          return uri.getAuthority();
      }
      
     /**
      * Returns the decoded fragment component of this Location.
      * <p>
      * A sequence of escaped octets is decoded by replacing it with the
      * sequence of characters that it represents in the UTF-8 character set.
      * UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      * any quoted US-ASCII characters as well as that of decoding any encoded
      * non-US-ASCII characters.  If a decoding error occurs when decoding the
      * escaped octets then the erroneous octets are replaced by
      * "\uFFFD", the Unicode replacement character.
      * <p>
      * The string returned by this method is equal to that returned by the
      * GetRawFragment method except that all sequences of escaped octets are
      * decoded.
      *
      * @return The decoded fragment component of this Location, a 
      * java.lang.String, or null if fragment is undefined.
      */
      public String getFragment()
      {
          return uri.getFragment();
      }
      
     /**
      *  Returns the raw fragment component of this Location.
      *  <p>
      *  The fragment component of a Location, if defined, only contains legal
      *  URI characters.
      *
      * @return The raw fragment component of this Location, a java.lang.String, 
      * or null if fragment is undefined.
      */
      public String getRawFragment()
      {
          return uri.getRawFragment();
      }
      
     /**
      * Returns the host component of this Location.
      * <p>
      * The host component of a Location, if defined, will have one of the  following 
      * forms: 
      * <ul>
      * <li> A domain name consisting of one or more labels separated by period 
      * characters (`.'), optionally followed by a period character. Each label 
      * consists of alphanum characters as well as hyphen characters (`-'), though 
      * hyphens never occur as the first or last characters in a label. The last, 
      * or only, label in a domain name begins with an alpha character. </li>
      * <li> A dotted-quad IPv4 address of the form digit+.digit+.digit+.digit+,
      * where no digit sequence is longer than three characters and no sequence has 
      * a value larger than 255. </li>
      * <li> An IPv6 address enclosed in square brackets (`[' and `]') and consisting 
      * of hexadecimal digits, colon characters (`:'), and possibly an embedded IPv4 
      * address. The full syntax of IPv6 addresses is specified in RFC 2373: IPv6
      * Addressing Architecture. </li>
      * </ul>
      * The host component of a Location cannot contain escaped octets, hence this
      * method does not perform any decoding.
      *
      * @return The host component of this Location, a java.lang.String, or null if 
      * host is undefined.
      */
      public String getHost()
      {
          return uri.getHost();
      }
      
     /**
      *  Returns the decoded path component of this Location.
      *  <p> 
      *  A sequence of escaped octets is decoded by replacing it with the
      *  sequence of characters that it represents in the UTF-8 character set.
      *  UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      *  any quoted US-ASCII characters as well as that of decoding any
      *  encoded non-US-ASCII characters.  If a decoding error occurs when
      *  decoding the escaped octets then the erroneous octets are replaced by
      *  "\uFFFD", the Unicode replacement character.
      *  <p> 
      *  The string returned by this method is equal to that returned by the
      *  GetRawPath method except that all sequences of escaped octets are
      *  decoded.
      *
      * @return The decoded path component of this Location, a java.lang.String, 
      * or null if path is undefined.
      */
      public String getPath()
      {
          return uri.getPath();
      }
      
     /**
      *  Returns the raw path component of this Location.
      *  <p>
      *  The path component of a URI, if defined, only contains the slash
      *  character (`/'), the commercial-at character (`@'), and characters in
      *  the unreserved, punct, escaped, and other categories.
      *
      * @return The raw path component of this Location, a java.lang.String, 
      * or null if path is undefined. 
      */
      public String getRawPath()
      {
          return uri.getRawPath();
      }
      
     /**
      * Returns the port number of this Location.
      * <p> 
      * The port component of a URI, if defined, is a non-negative  integer.
      *
      * @return The port component of this URI, an int, or -1 if the port is 
      * undefined.
      */
      public int getPort()
      {
          return uri.getPort();
      }
      
     /**
      * Returns the decoded query component of this Location.
      * <p>
      * A sequence of escaped octets is decoded by replacing it with the
      * sequence of characters that it represents in the UTF-8 character set.
      * UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      * any quoted US-ASCII characters as well as that of decoding any
      * encoded non-US-ASCII characters.  If a decoding error occurs when
      * decoding the escaped octets then the erroneous octets are replaced by
      * "\uFFFD", the Unicode replacement character.
      * <p>
      * The string returned by this method is equal to that returned by the
      * GetRawQuery method except that all sequences of escaped octets are
      * decoded.
      *
      * @return The decoded query component of this Location, a java.lang.String, 
      * or null if query is undefined.
      */
      public String getQuery()
      {
          return uri.getQuery();
      }
      
     /**
      *  Returns the raw query component of this Location.
      *  <p>
      *  The query component of a URI, if defined, only contains legal URI
      *  characters. 
      *
      * @return The raw query component of this Location, a java.lang.String, or 
      * null if query is undefined.
      */
      public String getRawQuery()
      {
          return uri.getRawQuery();
      }
      
     /**
      * Returns the scheme component of this Location.
      * <p>
      * The scheme component of a URI, if defined, only contains characters
      * in the alphanum category and in the string "-.+".  A scheme always
      * starts with an alpha character.
      * <p>  
      * The scheme component of a URI cannot contain escaped octets, hence
      * this method does not perform any decoding.
      *
      * @return The scheme component of this Location, a java.lang.String, 
      * or null if scheme is undefined.
      */
      public String getScheme()
      {
          return uri.getScheme();
      }
      
     /**
      * Returns the decoded scheme-specific part of this Location.
      * <p>
      * A sequence of escaped octets is decoded by replacing it with the
      * sequence of characters that it represents in the UTF-8 character set.
      * UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      * any quoted US-ASCII characters as well as that of decoding any
      * encoded non-US-ASCII characters.  If a decoding error occurs when
      * decoding the escaped octets then the erroneous octets are replaced by
      * "\uFFFD", the Unicode replacement character. 
      * <p>
      * The string returned by this method is equal to that returned by the
      * GetRawSchemeSpecificPart method except that all sequences of escaped
      * octets are decoded.
      *
      * @return The decoded scheme-specific component of this Location, a 
      * java.lang.String (never null)
      */
      public String getSchemeSpecificPart()
      {
          return uri.getSchemeSpecificPart();
      }
      
     /**
      * Returns the raw scheme-specific part of this Location. The
      * scheme-specific part is never undefined, though it may be empty.
      * <p>
      *  The scheme-specific part of a URI only contains legal URI characters.
      *
      * @return The raw scheme-specific component of this Location, a 
      * java.lang.String (never null) 
      */
      public String getRawSchemeSpecificPart()
      {
          return uri.getRawSchemeSpecificPart();
      }
      
     /**
      *  Returns the decoded user-information component of this Location.
      *  <p>
      *  A sequence of escaped octets is decoded by replacing it with the
      *  sequence of characters that it represents in the UTF-8 character set.
      *  UTF-8 contains US-ASCII, hence decoding has the effect of de-quoting
      *  any quoted US-ASCII characters as well as that of decoding any
      *  encoded non-US-ASCII characters.  If a decoding error occurs when
      *  decoding the escaped octets then the erroneous octets are replaced by
      *  "\uFFFD", the Unicode replacement character.
      *  <p>
      *  The string returned by this method is equal to that returned by the
      *  GetRawUserInfo method except that all sequences of escaped octets are
      *  decoded.
      *
      * @return The decoded user-information component of this Location, a 
      * java.lang.String, or null if it is undefined
      */
      public String getUserInfo()
      {
          return uri.getUserInfo();
      }
      
     /**
      * Returns the raw user-information component of this Location.
      * <p>
      * The user-information component of a URI, if defined, only contains
      * characters in the unreserved, punct, escaped, and other categories. 
      *
      * @return The raw user-information component of this Location, a 
      * java.lang.String, or null if it is undefined
      */
      public String getRawUserInfo()
      {
          return uri.getRawUserInfo();
      }
      
     /**
      *  Returns the content of this Location as a java.lang.String.
      *  <p>
      *  A string equivalent to the input java.lang.String given to the Location
      *  constructor, or to the string computed from the originally-given
      *  components, as appropriate, is returned.
      *
      * @return The java.lang.String form of this Location
      */
      public String toString()
      {
          return uri.toString();
      }
}