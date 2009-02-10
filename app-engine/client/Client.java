/*
 * Created 10 Feb. 2009 by Bas Boterman
 */

package client;

/**
 * Test class to make HTTP connections to the App Engine.
 * 
 * @author bbn230
 */

import java.net.*;
import java.io.*;

public class Client {
	private static void makeHttpsConnection() throws Exception {
		/* TODO: make HTTPS connection here. */
	}
	
	private static void makeHttpConnection() throws Exception {
		/* Setting up a new connection. */		
		URL url = new URL("http://daraja.appspot.com/");
        URLConnection urlc = url.openConnection();
        HttpURLConnection httpc = (HttpURLConnection) url.openConnection();  
        BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        String inputLine;
        
        /* Retrieving headers. */
        System.out.println(httpc.getResponseCode());
        System.out.println(httpc.getRequestMethod());
        for(int i=0; httpc.getHeaderField(i) != null; i++) {
        	System.out.println(httpc.getHeaderField(i));
        }
       
        /* Retrieving body. */
        while ((inputLine = in.readLine()) != null) { 
            System.out.println(inputLine);
        }
        in.close();
	}
	
	public static void main(String argv[]) throws Exception {
		makeHttpConnection();
		makeHttpsConnection();
	}
}