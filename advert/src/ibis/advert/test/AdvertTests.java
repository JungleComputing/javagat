/*
 * Created 02 Apr. 2009 by Bas Boterman.
 */

package ibis.advert.test;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvertTests {

	private static final String SERVER = "bbn230.appspot.com";
	private static final String USER   = "ibisadvert@gmail.com";

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);
	
	private static void testDelete(Advert advert, String path) throws Exception {
		logger.info("Calling advert.delete()");
		advert.delete(path);
	}
	
	private static void testFind(Advert advert) throws Exception {
		MetaData metaData = new MetaData();
		String[] result   = null;
		
		metaData.put("key1", "value1");
		metaData.put("key3", "value3");
		
		logger.info("Calling advert.find()");
		result = advert.find(metaData);
		
		for (int i = 0; i < result.length; i++) {
			logger.debug("Find result: {}", result[i]);
		}
	}
	
	private static void testGetMD(Advert advert, String path) throws Exception {
		MetaData metaData = null;
		
		logger.info("Calling advert.getMetaData()");
		metaData = advert.getMetaData(path);
		
		Iterator<String> itr  = metaData.getAllKeys().iterator();
		
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metaData.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			logger.debug("GetMD result: {} - {}", key, value);
		}
	}
	
	private static void testGet(Advert advert, String path) throws Exception {
		byte[] b = null;
		
		logger.info("Calling advert.get()");
		b = advert.get(path);
		
		logger.debug("Get result: {}", b.toString());
	}
	
	private static void testAdd(Advert advert, String path, String filename) throws Exception {
		MetaData metaData = new MetaData();
		File file = new File(filename);;

		byte[] b = new byte[(int) file.length()];
	    int size = 0;
	    DataInputStream is = null;
	    
	    try {
	    	 is = new DataInputStream(new FileInputStream(file));
	    }
	    catch (FileNotFoundException nfe) {
	    	logger.error("File not found: {}", nfe);
	    }
	    
	    try {
	    	size = is.read(b);
	        logger.debug("Bytes read: {}", size);
		} 
	    catch (EOFException eof) {
			logger.error("EOF reached."); 
		}
		catch (IOException ioe) {
			logger.error("IO error: {}", ioe);
		}
		
		metaData.put("key1", "value1");
		metaData.put("key2", "value2");
		metaData.put("key3", "value3");
		
		logger.info("Calling advert.add()");
		advert.add(b, metaData, path);
	}
	
	public static void main(String argv[]) throws Exception {
		logger.info("Starting test class...");
		Advert advert   = null;
		String path     = null;
		String filename = null;
		
		if (argv.length != 3) {
			logger.error("***Usage: AdvertTests <passwd> <pathname> <filename>");
		}
		else {
			/* Create a new Advert object. */
			advert = new Advert(SERVER, USER, argv[0]);
			logger.info("Advert object created.");
			
			path     = argv[1];
			filename = argv[2];
			logger.debug("Path: {}", path);
			logger.debug("File: {}", filename);
		}
		
		/* add() */
//		logger.info("Testing add()...");
//		testAdd(advert, path, filename);
		
		/* get() */
//		logger.info("Testing get()...");
//		testGet(advert, path);
		
//		/* getMetaData() */
//		logger.info("Testing getMetaData()...");
//		testGetMD(advert, path);
//		
//		/* find() */
//		logger.info("Testing find()...");
//		testFind(advert);
//		
//		/* delete() */
		logger.info("Testing delete()...");
		testDelete(advert, path);
	}
}
