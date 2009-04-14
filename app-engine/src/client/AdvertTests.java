package client;

import ibis.advert.Advert;
import ibis.advert.MetaData;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvertTests {

	private static final String server = "bbn230.appspot.com";
	private static final String user   = "ibisadvert@gmail.com";

	final static Logger logger = LoggerFactory.getLogger(AdvertTests.class);
	
	private static File readFile() {
//		String filename = "/Volumes/Users/bbn230/Documents/workspace/app-engine/app-engine/src/client/appengine.gif"; /* OSX location */
		String filename = "E:/Documents/Documents/Eclipse/workspace/app-engine/app-engine/src/client/appengine.gif";  /* Win location */
		
		File result = new File(filename);
		
		return result;
	}
	
	private static void testDelete(Advert advert) throws Exception {
		String path = "/home/bboterm/advert";
		
		logger.info("Calling advert.delete()");
		advert.delete(path);
	}
	
	private static void testAdd(Advert advert) throws Exception {
		MetaData metaData = new MetaData();
		
		File file = readFile();
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
		
		String path = "/home/bboterm/advert";
		
		logger.info("Calling advert.add()");
		advert.add(b, metaData, path);
	}
	
	public static void main(String argv[]) throws Exception {
		logger.info("Starting test class...");
		Advert advert = null;
		
		if (argv.length != 1) {
			logger.error("***Usage: provide password as first and only argument!");
		}
		else {
			/* Create a new Advert object. */
			advert = new Advert(server, user, argv[0]);
			logger.info("Advert object created.");
		}
		
		/* add() */
//		logger.info("Testing add()...");
//		testAdd(advert);
		
		/* delete() */
		logger.info("Testing delete()...");
		testDelete(advert);
		
		/* find() */
		
		/* get() */
		
		/* getMetaData() */
	}
}
