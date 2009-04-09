package ibis.advert;

/**
 * This exception is thrown when the App Engine runs out of resources.
 * 
 * @author bbn230
 * 
 */

@SuppressWarnings("serial")
public class AppEngineResourcesException extends Exception {
	public AppEngineResourcesException() {
		super();
	}
	
	public AppEngineResourcesException(String message) {
		super(message);
	}
}
