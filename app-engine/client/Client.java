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
	private static void makeHttpsClientLogin(String uri) throws Exception {
	    // Create a login request. A login request is a POST request that looks like
	    // POST /accounts/ClientLogin HTTP/1.0
	    // Content-type: application/x-www-form-urlencoded
	    // Email=johndoe@gmail.com&Passwd=north23AZ&service=gbase&source=Insert Example
		
		//Setting up SSL
		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

		Properties properties = System.getProperties();

		String handlers = System.getProperty("java.protocol.handler.pkgs");
		if (handlers == null) {
			/* nothing specified yet (expected case) */
			properties.put("java.protocol.handler.pkgs",
					"com.sun.net.ssl.internal.www.protocol");
		} else {
			/* something already there, put ourselves out front */
			properties.put("java.protocol.handler.pkgs",
					"com.sun.net.ssl.internal.www.protocol|".concat(handlers));
		}
		System.setProperties(properties); 	
		
		// Create URL object
		URL url = new URL(uri);
		
	    // Open connection
	    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	  
	    // Set properties of the connection
	    urlConnection.setRequestMethod("POST");
	    urlConnection.setDoInput(true);
	    urlConnection.setDoOutput(true);
	    urlConnection.setUseCaches(false);
	    urlConnection.setRequestProperty("Content-Type",
	                                     "application/x-www-form-urlencoded");
	  
	    // Form the POST parameters
	    StringBuilder content = new StringBuilder();
	    content.append("Email=").append(URLEncoder.encode("johndoe@gmail.com", "UTF-8"));
	    content.append("&Passwd=").append(URLEncoder.encode("north23AZ", "UTF-8"));
	    content.append("&service=").append(URLEncoder.encode("xapi", "UTF-8"));
	    content.append("&source=").append(URLEncoder.encode("Google Base data API example", "UTF-8"));
	
	    OutputStream outputStream = urlConnection.getOutputStream();
	    outputStream.write(content.toString().getBytes("UTF-8"));
	    outputStream.close();
	  
	    // Retrieve the output
	    int responseCode = urlConnection.getResponseCode();
	    InputStream inputStream;
	    if (responseCode == HttpURLConnection.HTTP_OK) {
	      inputStream = urlConnection.getInputStream();
	    } else {
	      inputStream = urlConnection.getErrorStream();
	    }
	    
		for (int i = 0; urlConnection.getHeaderField(i) != null; i++) {
			System.out.println((urlConnection.getHeaderFieldKey(i)==null?"":urlConnection.getHeaderFieldKey(i) + ": ") + urlConnection.getHeaderField(i));
		}
	    
		System.out.println("----");
		
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
	    while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}
	}

	private static void makeHttpsLogin(String uri) throws Exception {
		String protocol = "http://";
//		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//
//		Properties properties = System.getProperties();
//
//		String handlers = System.getProperty("java.protocol.handler.pkgs");
//		if (handlers == null) {
//			/* nothing specified yet (expected case) */
//			properties.put("java.protocol.handler.pkgs",
//					"com.sun.net.ssl.internal.www.protocol");
//		} 
//		else {
//			/* something already there, put ourselves out front */
//			properties.put("java.protocol.handler.pkgs",
//					"com.sun.net.ssl.internal.www.protocol|".concat(handlers));
//		}
//		System.setProperties(properties); 

		try {
			URL page = new URL(protocol.concat(uri)); 
			URLConnection urlc = page.openConnection();

			System.out.println("*** Logging into https://" + uri + " ***");
			
//			urlc.setUseCaches(false);
//			urlc.setDoOutput(true);
//			OutputStreamWriter out = 
//				new OutputStreamWriter(urlc.getOutputStream());
//			
//			/* Writing POST data. */
//			out.write("");
//			out.close();
			
			/* Retrieving body. */	
			BufferedReader in = 
				new BufferedReader(new InputStreamReader(urlc.getInputStream()));
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
	 * This function makes an HTTP connection, and sends a cookie once the
	 * connection is established.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */	
	private static void makeHttpCookies(String uri) throws Exception {
		String protocol = "http://";
		URL url = new URL(protocol.concat(uri));
		URLConnection urlc = url.openConnection();
		//HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		System.out.println("*** Making an HTTP connection with cookies to http://" + uri + " ***");
		
		/* Setting cookies. */
		urlc.setRequestProperty("Cookie", "dev_appserver_login=test@example.com:False");
		
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
	 * This function makes an HTTP connection using the POST method, and
	 * sends a binary file as the message body.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
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
//		String server = "bbn230.appspot.com/";
		String server = "localhost:8081/";
		String uri = null;
		
		/* Making a standard connection in HTTP(S). */
		uri = server.concat("helloworld/");
		//makeHttpConnection(uri);
		//makeHttpsConnection(uri);
		
		/* Making a connection using POST forms in HTTP(S). */
		uri = server.concat("forms/sign");
		//makeHttpPost(uri);
		uri = server.concat("binary/get");
		//makeHttpBinaryPost(uri);
		
		/* Making a connection using cookes. */
		uri = server.concat("cookies/");
		//makeHttpCookies(uri);
		
		/* Logging in on a Google login page (using HTTPS). */
		uri = server.concat("_ah/login?email=test?example.com&action=Login&continue=http://localhost:8081/cookies/");
		//makeHttpsLogin(uri);
		
		/* Logging in to Google's ClientLogin. */
		uri = "https://www.google.com/accounts/ClientLogin";
		makeHttpsClientLogin(uri);
	}
}