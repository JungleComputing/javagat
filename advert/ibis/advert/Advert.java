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

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;

public class Advert {
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
			throws AppEngineResourcesException {
		
		byte   pathSize  = new Integer(path.length()).byteValue(); 
		byte[] pathBytes = path.getBytes();

		Iterator<String> itr  = metaData.getAllKeys().iterator();
		StringBuilder content = new StringBuilder();
		StringCharacterIterator scitr = null;
		char c = ' ';
	
		while (itr.hasNext()) {
			String key = itr.next();
			String value = metaData.get(key);

			if (value == null) {
				continue;
			}

			scitr = new StringCharacterIterator(key);
			c =  scitr.current();
			
			while (c != CharacterIterator.DONE ) {
				if (c == ':') {
					content.append("\\:");
				}
				else if (c == '=') {
					content.append("\\=");
				}
				else {
					content.append(c);
				}
				c = scitr.next();
			}
			
			content.append('=');
			
			scitr = new StringCharacterIterator(value);
			c =  scitr.current();
			
			while (c != CharacterIterator.DONE ) {
				if (c == ':') {
					content.append("\\:");
				}
				else if (c == '=') {
					content.append("\\=");
				}
				else {
					content.append(c);
				}
				c = scitr.next();
			}
			
			content.append(':');
		}
					
		byte   metaDataSize  = new Integer(content.length()).byteValue();
		byte[] metaDataBytes = content.toString().getBytes();
		
		byte   objectSize    = new Integer(object.length).byteValue();
		
        /* TODO: Send in following order:
         * pathSize pathBytes metaDataSize metaDataBytes objectSize object
         * 
         * TODO: Return TTL of data stored at server (optional)?
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
	public void delete(String path) throws NoSuchElementException {

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
	public byte[] get(String path) throws NoSuchElementException {

		return null;
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
	public MetaData getMetaData(String path) throws NoSuchElementException {

		return null;
	}

	/**
	 * Query the App Engine for entries matching the specified set of
	 * {@link MetaData}.
	 * 
	 * @param metaData
	 *            {@link MetaData} describing the entries to be searched for.
	 * @param pwd
	 *            Current working path.
	 * @return a {@link String}[] of absolute paths, each pointing to a
	 *         matching entry. If no matches are found, null is returned.
	 */
	public String[] find(MetaData metaData, String pwd) {

		return null;
	}
}
