/*
 * Created 24 Mar. 2009 by Bas Boterman.
 */

package ibis.advert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.Security;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * This class contains the main functionality for communication from and to the
 * Google App Engine.
 * 
 * @author bbn230
 */

class Communications {
	
	private static final int MAX_REQ_SIZE = 10000000;
	private static final int MAX_DB_SIZE  = 1000000;
	
	private String cookie;
	private String server;
	
	Communications(String server, String user, String passwd) 
	  throws MalformedURLException, ProtocolException, IOException, 
	  AuthenticationException {
		this.server = server;
		authenticate(user, passwd);
	}
	
	private static final String CLIENTLOGIN = 
		"https://www.google.com/accounts/ClientLogin";
	
	/**
	 * Function to set up SSL in the system properties. 
	 */
	private static void setupSsl() {
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
	}
	
	/**
	 * Function to authenticate to the Google App Engine
	 * @param server
	 * 				Server to connect to.
	 * @param user
	 * 				User's email address for identification.
	 * @param passwd
	 * 				User's password for identification.
	 * @return a {@link String} which contains a cookie with a session ID.
	 * @throws Exception
	 * 				Failed to authenticate to the App Engine.
	 */
	void authenticate(String user, String passwd) 
	  throws MalformedURLException, ProtocolException, IOException, 
	  AuthenticationException {
		URL url                 = null;
	    HttpURLConnection httpc = null;
		
		/* Setting up SSL. */
		setupSsl();
		
		/* Create URL object and open connection to ClientLogin. */
		url   = new URL(CLIENTLOGIN);
		httpc = (HttpURLConnection) url.openConnection();
	  
	    /* Set properties of the connection. */
		httpc.setRequestMethod("POST");
	    httpc.setDoInput(true);
	    httpc.setDoOutput(true);
	    httpc.setUseCaches(false);
	    httpc.setRequestProperty("Content-Type", 
	                             "application/x-www-form-urlencoded");
	  
	    /* Form the POST parameters. */
	    StringBuilder content = new StringBuilder();
		content.append("Email=").append(URLEncoder.encode(user, "UTF-8"));
	    content.append("&Passwd=").append(URLEncoder.encode(passwd, "UTF-8"));
	    content.append("&service=").append(URLEncoder.encode("ah", "UTF-8"));
	    content.append("&source=").append(URLEncoder.encode("IBIS Advert", 
	    		                                            "UTF-8"));
	
	    /* Write output. */
	    OutputStream outputStream = httpc.getOutputStream();
	    outputStream.write(content.toString().getBytes("UTF-8"));
	    outputStream.close();
	  
	    /* Retrieve the output. */
	    InputStream inputStream;
	    if (httpc.getResponseCode() == HttpURLConnection.HTTP_OK) {
	    	inputStream = httpc.getInputStream();
	    } 
	    else {
	    	throw new AuthenticationException();
	    }
	    
	    /* Retrieve body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(inputStream));

		String inputLine = null;
		String authid    = null;

		/* Extract auth token. */
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.startsWith("Auth")) {
				authid = inputLine.split("=")[1];
				break;
			}
		}
		
		in.close();
		
		/* Setting up a new connection to App Engine. */
		url = new URL("http://" + server + "/_ah/login?continue=https://" + server + "/&auth=" + authid);
		httpc = (HttpURLConnection) url.openConnection();
		
	    httpc.setRequestMethod("GET");
	    httpc.setUseCaches(false);
	    
	    if (!(httpc.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP || 
	    	  httpc.getResponseCode() == HttpURLConnection.HTTP_OK)) {
	    	throw new AuthenticationException();
	    }
		
		/* Retrieving cookie. */
		for (int i = 0; httpc.getHeaderField(i) != null; i++) {
			if (httpc.getHeaderFieldKey(i) != null && 
				httpc.getHeaderFieldKey(i).equals("Set-Cookie")) {
				cookie = httpc.getHeaderField(i);
			}
		}
		
		if (cookie == null) {
			throw new AuthenticationException();
		}
	}
	
	/**
	 * Function to send an object over HTTP.
	 * @param server
	 * 				Server to send the object to.
	 * @param cookie
	 * 				Cookie for identification.
	 * @param object
	 * 				Object to be send to the server.
	 * @return
	 * 				Returns the response body in {@link String} format.
	 * @throws Exception
	 * 				Failed to send object to server.
	 */
	String httpSend(String ext, String payload) 
	  throws MalformedURLException, IOException, AuthenticationException,
	  AppEngineResourcesException, NoSuchElementException, 
	  RequestTooLargeException {
		if (payload.length() > MAX_REQ_SIZE) {
			throw new RequestTooLargeException();
		}
		
	    URL url = new URL("http://" + server + ext);
		HttpURLConnection httpc = (HttpURLConnection) url.openConnection();
		
		/* Setting headers. */
		httpc.setRequestMethod("POST");
	    httpc.setRequestProperty("Cookie", cookie);
		httpc.setDoInput(true);
	    httpc.setDoOutput(true);
	    httpc.setUseCaches(false);
	    
	    /* Connecting and POSTing. */
		httpc.connect();
		OutputStreamWriter osw = new OutputStreamWriter(httpc.getOutputStream());
		
		/* Writing JSON data. */
		osw.write(payload);
		osw.flush();
		osw.close();
		
		/* Retrieving response headers. */
		if (httpc.getResponseCode() != HttpURLConnection.HTTP_OK) {
			if (httpc.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
				throw new AuthenticationException();
			}
			if (httpc.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				throw new NoSuchElementException();
			}
			if (httpc.getResponseCode() == HttpURLConnection.HTTP_REQ_TOO_LONG) {
				throw new RequestTooLargeException();
			}
			if (httpc.getResponseCode() == HttpURLConnection.HTTP_UNAVAILABLE) {
				throw new AppEngineResourcesException();
			}
			//TODO: throw generic Exception?
		}
		
		/* Retrieving body. */
		BufferedReader in = 
			new BufferedReader(new InputStreamReader(httpc.getInputStream()));

		return in.readLine();
	}
}