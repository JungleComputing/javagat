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
import java.util.Iterator;
import java.util.Properties;

public class Client {

	/**
	 * This function tests a response of any binary data.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
	private static void makeHttpBinaryResponse(String uri) throws Exception {
		/* Setting up a new connection. */
		String protocol = "http://";
		URL url = new URL(protocol.concat(uri));
//		URLConnection urlc = url.openConnection();
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		System.out.println("*** Making an HTTP connection to http://" + uri + " ***");

		/* Retrieving headers. */
		System.out.println(httpc.getResponseCode());
		System.out.println(httpc.getRequestMethod());
		for (int i = 0; httpc.getHeaderField(i) != null; i++) {
			System.out.println((httpc.getHeaderFieldKey(i)==null?"":httpc.getHeaderFieldKey(i) + ": ") + httpc.getHeaderField(i));
		}

		/* Retrieving body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(httpc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}
		in.close();		
	}
	
	/**
	 * This function makes a login attempt to Google's ClientLogin API.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */		
	private static void makeHttpsClientLogin(String uri, String password) throws Exception {
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
	    content.append("Email=").append(URLEncoder.encode("ibisadvert@gmail.com", "UTF-8"));
	    content.append("&Passwd=").append(URLEncoder.encode(password, "UTF-8"));
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

	/**
	 * This function emulates the Google login page over and HTTP 
	 * connection.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */	
	private static void makeHttpLogin(String uri) throws Exception {
		String protocol = "http://";

		try {
			URL url = new URL(protocol.concat(uri)); 
			HttpURLConnection httpc = (HttpURLConnection) url.openConnection();

			System.out.println("*** Logging into http://" + uri + " ***");
			
		    httpc.setRequestMethod("GET");
		    httpc.setDoInput(true);
		    httpc.setDoOutput(true);
		    httpc.setUseCaches(false);
			
			/* Retrieving headers. */
			System.out.println(httpc.getResponseCode());
			System.out.println(httpc.getRequestMethod());
			for (int i = 0; httpc.getHeaderField(i) != null; i++) {
				System.out.println((httpc.getHeaderFieldKey(i)==null?"":httpc.getHeaderFieldKey(i) + ": ") + httpc.getHeaderField(i));
			}

			/* Retrieving body. */	
			BufferedReader in = 
				new BufferedReader(new InputStreamReader(httpc.getInputStream()));
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
	 * This function does a binary post, with self-encoded data.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */	
	private static void makeOwnPost(String uri) throws Exception {
		String protocol = "http://";
		String filename = "/Volumes/Users/bbn230/Documents/workspace/app-engine/app-engine/client/appengine.gif"; /* OSX location */
		//String filename = "E:/Documents/Documents/Eclipse/workspace/app-engine/app-engine/client/appengine.gif";  /* Win location */
		String prefix = "0123456789";
		
		/* Setting up URL */
		URL url = new URL(protocol.concat(uri));
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		/* Reading file into buffer */
		File file = new File(filename);
		
	    DataInputStream is = new DataInputStream(new FileInputStream(file));

		Integer pathLength = new Integer(prefix.length());
		System.out.println();
	    
	    byte[] a = new byte[1];
	    a[0] = pathLength.byteValue();
	    byte[] b = new byte[(int) file.length()];
 	    byte[] c = new byte [a.length + b.length];
 	    
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

		System.arraycopy(a, 0, c, 0,        a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		
		/* Setting up request headers */
		httpc.setRequestMethod("POST");
		httpc.setDoInput(true);
		httpc.setDoOutput(true);
		httpc.connect();
		DataOutputStream out = new DataOutputStream(httpc.getOutputStream());
		
		try {
			out.write(c, 0, a.length + b.length);
			out.flush();
			out.close();
		}
		catch (IOException ioe) {
			System.out.println("IO error: " + ioe);
		}
		
		System.out.println("done. HTTP Response: " + httpc.getResponseCode());	
		
		/* Retrieving body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(httpc.getInputStream()));
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
		}
		in.close();		
	}
	
	/**
	 * This function does an HTTP POST request, sending multiple data items
	 * to a server. In this case it is one string of text, and one binary
	 * file.
	 * 
	 * @param uri
	 *     {@link String} to make the connection to.
	 * @throws Exception
	 *     The connection can't be established.
	 */
	private static void makeHttpMultipartPost(String uri) throws Exception {
		/* Setting up a new connection. */
		String protocol = "http://";
		String filename = "/Volumes/Users/bbn230/Documents/workspace/app-engine/app-engine/client/appengine.gif"; /* OSX location */
		//String filename = "E:/Documents/Documents/Eclipse/workspace/app-engine/app-engine/client/appengine.gif";  /* Win location */
		String boundary = "AaB03x"; /* Part boundaries should not occur in any of the data; how this is done lies outside the scope of this specification. */
		
		/* Setting up URL */
		URL url = new URL(protocol.concat(uri));
		//URLConnection urlc = url.openConnection();
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		/* Reading file into buffer */
		File file = new File(filename);
		
	    DataInputStream is = new DataInputStream(new FileInputStream(file));
	    
	    byte[] b = new byte[(int) file.length()];
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
		
		/* Setting up request headers */
		httpc.setRequestMethod("POST");
	    httpc.setDoInput(true);
	    httpc.setDoOutput(true);
	    httpc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			    
	    /* Connecting and POSTing */
		httpc.connect();
		DataOutputStream dos = new DataOutputStream(httpc.getOutputStream());
		OutputStreamWriter osw = new OutputStreamWriter(httpc.getOutputStream());
		
		/*
		 * Content-Type: multipart/form-data; boundary=AaB03x
		 * 
		 * --AaB03x
   		 * Content-Disposition: form-data; name="submit-name"
		 * 
		 * Larry
		 * --AaB03x
         * Content-Disposition: form-data; name="object"; filename="appengine.gif"
         * Content-Type: image/gif
         * 
         * ... contents of file1.txt ...
         * --AaB03x--
         *
		 */
		
		/* Building POST message */
		osw.write("--" + boundary + "\r\n");
		osw.write("Content-Disposition: form-data; name=\"path\"\r\n\r\n");
		osw.write("abcdefgh" + "\r\n");
		osw.write("--" + boundary + "\r\n");
		osw.write("Content-Disposition: form-data; name=\"object\"; filename=\"appengine.gif\"\r\n");
		osw.write("Content-Type: image/gif\r\n\r\n");
		osw.flush();
		
		try {
			dos.write(b, 0, size);
			dos.flush();
		}
		catch (IOException ioe) {
			System.out.println("IO error: " + ioe);
		}

		osw.write("\r\n");
		osw.write("--" + boundary + "--");
		osw.flush();
		
		dos.close();
		osw.close();
		
		System.out.println("done. HTTP Response: " + httpc.getResponseCode());	
		
		/* Retrieving body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(httpc.getInputStream()));
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
		uri = server.concat("binary/multipart");
		//makeHttpMultipartPost(uri);
		uri = server.concat("binary/get");
		//makeOwnPost(uri);
		
		/* Making a connection using cookes. */
		uri = server.concat("cookies/");
		//makeHttpCookies(uri);
		
		/* Logging in on a Google login page (using HTTP). */
		uri = server.concat("_ah/login?email=test@example.com&action=Login");
		//makeHttpLogin(uri);
		
		/* Logging in to Google's ClientLogin. */
		uri = "https://www.google.com/accounts/ClientLogin";
		if (argv.length != 1) {
			System.out.println("***Usage: provide password as first and only argument!");
		}
		//makeHttpsClientLogin(uri, argv[0]);
		
		/* Getting a binary response. */
		uri = server.concat("binary/display");
		//makeHttpBinaryResponse(uri);
	}
}