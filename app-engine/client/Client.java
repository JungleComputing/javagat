/*
 * Created 10 Feb. 2009 by Bas Boterman
 */

package client;

/**
 * Test classes to make HTTP(S) connections to the App Engine.
 * 
 * @author bbn230
 */

import java.net.*;
import java.io.*;
import java.security.Security;
import java.util.Properties;

public class Client {
	
	private static void makeHttpCookies(String uri) throws Exception {
		/* TODO: finish */
	}

	private static void makeHttpBinaryPost(String uri) throws Exception {
		/* Setting up a new connection. */
		String protocol = "http://";
		URL url = new URL(protocol.concat(uri));
		//URLConnection urlc = url.openConnection();
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
//	    DataInputStream is = new DataInputStream(new FileInputStream("E:/Documents/Documents/Eclipse/workspace/app-engine/app-engine/client/appengine.gif")); /* Windows location */
	    DataInputStream is = new DataInputStream(new FileInputStream("/Volumes/Users/bbn230/Documents/workspace/app-engine/app-engine/client/appengine.gif")); /* OSX location */
	    
	    byte[] b = new byte[7000];
	    int size = 0;
	    
	    try {
	    	size = is.read(b);
	        System.out.println("Bytes read: " + size);
		} 
	    catch (EOFException eof) {
			System.out.println("EOF reached."); 
		}
		catch (IOException ioe) {
			System.out.println("IO error: " + ioe);
		}
		
		httpc.setRequestMethod("POST");
		httpc.setDoInput(true);
		httpc.setDoOutput(true);
		httpc.connect();
		DataOutputStream out = new DataOutputStream(httpc.getOutputStream());
		
		try {
			out.write(b, 0, size);
			out.flush();
			out.close();
		}
		catch (IOException ioe) {
			System.out.println("IO error: " + ioe);
		}
		
		System.out.println("done. HTTP Response: " + httpc.getResponseCode());
	}
	
	/**
	 * This function makes an HTTP connection using the POST method.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
	private static void makeHttpPost(String uri) throws Exception {
		/* Setting up a new connection. */
		String protocol = "http://";
		URL url = new URL(protocol.concat(uri));
		URLConnection urlc = url.openConnection();
		
		System.out.println("*** Making an HTTP connection (POST) to http://" + uri + " ***");

		/* Setting up POST environment. */
		urlc.setDoOutput(true);
		OutputStreamWriter out = 
			new OutputStreamWriter(urlc.getOutputStream());
		
		/* Writing POST data. */
		out.write("author=bbn230&content=test");
		out.close();
		
		/* Retrieving body. */	
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}
		in.close();
	}
	
	/**
	 * This function makes an HTTPS connection to an URL defined by uri.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
	private static void makeHttpsConnection(String uri) throws Exception {
		String protocol = "https://";
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		Properties properties = System.getProperties();

		String handlers = System.getProperty("java.protocol.handler.pkgs");
		if (handlers == null) {
			/* nothing specified yet (expected case) */
			properties.put("java.protocol.handler.pkgs",
					"com.sun.net.ssl.internal.www.protocol");
		} 
		else {
			/* something already there, put ourselves out front */
			properties.put("java.protocol.handler.pkgs",
					"com.sun.net.ssl.internal.www.protocol|".concat(handlers));
		}
		System.setProperties(properties); 

		try {
			URL page = new URL(protocol.concat(uri)); 
			URLConnection urlc = page.openConnection();

			System.out.println("*** Making an HTTPS connection to https://" + uri + " ***");
			
			urlc.setUseCaches(false);
			
			/* Retrieving headers. */
			System.out.println("Content-type = " + urlc.getContentType());
			System.out.println("Content-length = " + urlc.getContentLength());
			
			/* Retrieving body. */
			BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
			}
			in.close();
		} 
		catch (MalformedURLException mue) {
			System.out.println("URL cannot be resolved");
		}
	}

	/**
	 * This function makes an HTTP connection to an URL defined by uri.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
	private static void makeHttpConnection(String uri) throws Exception {
		/* Setting up a new connection. */
		String protocol = "http://";
		URL url = new URL(protocol.concat(uri));
		URLConnection urlc = url.openConnection();
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		System.out.println("*** Making an HTTP connection to http://" + uri + " ***");

		/* Retrieving headers. */
		System.out.println(httpc.getResponseCode());
		System.out.println(httpc.getRequestMethod());
		for (int i = 0; httpc.getHeaderField(i) != null; i++) {
			System.out.println(httpc.getHeaderField(i));
		}

		/* Retrieving body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}
		in.close();
	}

	/**
	 * Main function; calls various local functions.
	 * 
	 * @param argv
	 *     Not used.
	 * @throws Exception
	 *     The connection can't be established. 
	 */
	public static void main(String argv[]) throws Exception {
		String server = "bbn230.appspot.com/";
//		String server = "localhost:8081/";
		String uri = null;
		
		/* Making a standard connection in HTTP(S). */
		uri = server.concat("helloworld/");
		//makeHttpConnection(uri);
		//makeHttpsConnection(uri);
		
		/* Making a connection using POST forms in HTTP(S). */
		uri = server.concat("forms/sign");
		//makeHttpPost(uri);
		uri = server.concat("binary/get");
		makeHttpBinaryPost(uri);
		
		/* Making a connection using cookes. */
		uri = server.concat("cookies/");
		//makeHttpCookies(uri);
	}
}