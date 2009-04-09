package ibis.advert;

/**
 * This exception is thrown when the HTTP Send request is too large for the
 * App Engine to process.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class RequestTooLargeException extends Exception {
	public RequestTooLargeException() {
		super();
	}
	
	public RequestTooLargeException(String message) {
		super(message);
	}
}
