/*
 * Created 02 Feb. 2009 by Bas Boterman.
 */

package ibis.advert;

/**
 * This class contains the main functionality for using the service which runs
 * at the Google App Engine.
 * 
 * @author bbn230
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Advert {
	
	final static Logger logger = LoggerFactory.getLogger(Advert.class);
	
	private static final String DEFAULT_SERVER     = "bbn230.appspot.com";
	private static final String DEFAULT_PUB_SERVER = "";
	
	private Communications comm = null;
	
	public Advert(String server, String user, String passwd) 
	  throws AuthenticationException, IOException {
		if (server == null) {
			if (user == null || passwd == null) {
				server = DEFAULT_PUB_SERVER;
			}
			else {
				server = DEFAULT_SERVER;
			}
		}

		comm = new Communications(server, user, passwd);
		logger.info("Communications class created.");
	}
	
	/**
	 * Add a {@link byte}[] to the App Engine, at an absolute path, with
	 * {@link MetaData} included, to the datastore. If an entry exists at the
	 * specified path, that entry gets overwritten, and a warning is issued.
	 * 
	 * @param bytes
	 *            {@link byte}[] to be stored.
	 * @param metaData
	 *            {@link MetaData} to be associated with the passed bytes.
	 * @param path
	 *            Absolute path of the new entry.
	 * @throws AppEngineResourcesException
	 *             This exception is thrown when the App Engine runs out of
	 *             resources.
	 */
	public void add(byte[] object, MetaData metaData, String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null) {
			throw new NullPointerException("Path can't be null.");
		}
		
		JSONArray  jsonarr = new JSONArray();
		JSONObject jsonobj = new JSONObject();
		
		Iterator<String> itr  = metaData.getAllKeys().iterator();
	
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metaData.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			jsonobj.put(key, value);
		}
		
		String base64 = new sun.misc.BASE64Encoder().encode(object);
		
		jsonarr.add(path);
		jsonarr.add(jsonobj);
		jsonarr.add(base64);
		
		logger.info("Calling httpSend() /add...");
		comm.httpSend("/add", jsonarr.toString());
		
        /*
         * TODO: return TTL of data stored at server (optional)?
         */
	}
	

	/**
	 * Remove an instance and related {@link MetaData} from the datastore at an
	 * absolute path.
	 * 
	 * @param path
	 *            Path is an absolute entry to be deleted.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public void delete(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null) {
			throw new NullPointerException("Path can't be null.");
		}
		
		logger.info("Calling httpSend() /del...");
		comm.httpSend("/del", path);
	}

	/**
	 * Gets an instance from the datastore at a given (absolute) path.
	 * 
	 * @param path
	 *            Absolute path of the entry.
	 * @return The instance at the given path.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public byte[] get(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException,Exception {
		if (path == null) {
			throw new NullPointerException("Path can't be null.");
		}
		
		logger.info("Calling httpSend() /get...");
		String base64 = comm.httpSend("/get", path);
		
		return new sun.misc.BASE64Decoder().decodeBuffer(base64);
	}

	/**
	 * Gets the {@link MetaData} of an instance from the given (absolute) path.
	 * 
	 * @param path
	 *            Absolute path of the entry.
	 * @return A {@link MetaData} object containing the meta data.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public MetaData getMetaData(String path) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		if (path == null) {
			throw new NullPointerException("Path can't be null.");
		}
		
		MetaData   metadata = new MetaData();
		
		logger.info("Calling httpSend() /getmd...");
		String result = comm.httpSend("/getmd", path);
		
		JSONObject jsonobj = JSONObject.fromObject(result);
		Iterator<?> itr    = jsonobj.keys();
		
		while (itr.hasNext()) {
			String key   = (String)itr.next();
			String value = (String)jsonobj.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			metadata.put(key,value);
		}

		return metadata;
	}

	/**
	 * Query the App Engine for entries matching the specified set of
	 * {@link MetaData}.
	 * 
	 * @param metaData
	 *            {@link MetaData} describing the entries to be searched for.
	 *            No wildcards allowed.
	 * @return a {@link String}[] of absolute paths, each pointing to a
	 *         matching entry. If no matches are found, null is returned.
	 */
	public String[] find(MetaData metaData)
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException, Exception {
		JSONObject metadata = new JSONObject();
		JSONArray  jsonarr  = new JSONArray();
		
		Iterator<String> itr  = metaData.getAllKeys().iterator();
		
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metaData.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			metadata.put(key, value);
		}
		
		logger.info("Calling httpSend() /find...");
		String result = comm.httpSend("/find", metadata.toString());
		
		jsonarr = JSONArray.fromObject(result);
		
		return (String[]) jsonarr.toArray(new String[0]);
	}
}
