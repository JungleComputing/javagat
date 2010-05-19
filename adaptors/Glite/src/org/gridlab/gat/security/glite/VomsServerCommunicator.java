package org.gridlab.gat.security.glite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gridlab.gat.GATInvocationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** This class was inspired by geclipse (http://www.geclipse.eu) 
 * Communicate with a VOMS server i.e. build and send the VOMS request with
 * FQANs and receive and parse the VOMS response
 * @author thomas
 * */
public class VomsServerCommunicator {
	/** Constant values of some XML elements that will be stumbled upon when parsing */
	private final static String ROOT_EL_NAME = "voms";
	private final static String COMMAND_EL_NAME = "command";
	private final static String LIFETIME_EL_NAME = "lifetime";
	private final static String GROUP_SEPARATOR = "/";
	private final static String QUAL_GROUP_COMMAND = "B";
	private final static String GROUP_COMMAND = "G";
	private final static String ROLE_IDENTIFIER = "Role=";
	private final static String VOMS_ANSWER_EL = "vomsans";
	private final static String VOMS_VERSION = "version";
	private final static String BITSTRING_ANSWER_EL = "bitstr";
	private final static String AC_ANSWER_EL = "ac";
	private final static String ERROR_ANSWER_EL = "error";
	private final static String ERROR_MSG_EL = "message";

	/** The document builder used to create the document representation fo the server request */
	private DocumentBuilder docBuilder;
	/** The transformer used. Created without DOM source, hence only used for copying XML data to server without transformation */
	private Transformer transformer;
	/** Request string for VO membership, groups and roles */
	private String requestString;
	/** Integer carrying the desired lifetime */
	private int lifetime;

	/**
	 * Decode the base64-encoded bitstream from the server using this look-up
	 * table
	 */
	public static final byte vomsDecodeMap[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
												0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
												0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 52, 53, 54, 55, 56, 57, 58, 59,
												60, 61, 0, 0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34,
												35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
												62, 0, 63, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
												14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 0 };


	/**
	 * Construct a new VOMS server communicator
	 * This class is used for sending a VOMS request to a server, including FQANs for group memberships
	 * and roles.
	 * 
	 * @param requestString Request string for VO, groups and roles separated by the / character
	 * @param lifetime The desired lifetime of the extended proxy part
	 * @throws GATInvocationException If trouble with parser or transformer creation occurs
	 */
	public VomsServerCommunicator(String requestString, int lifetime) throws GATInvocationException {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			TransformerFactory transFactory = TransformerFactory.newInstance();

			// needed for DOM transformation of the server request
			factory.setNamespaceAware(true);
			this.docBuilder = factory.newDocumentBuilder();
			this.transformer = transFactory.newTransformer();
		} catch (ParserConfigurationException e) {
			throw new GATInvocationException("Could not create a DOM document builder for VOMS server request", e);
		} catch (TransformerConfigurationException e) {
			throw new GATInvocationException("Could not create a copy-only transformer for VOMS server request", e);
		}

		this.requestString = requestString;
		this.lifetime = lifetime;
		
	}

	/**
	 * Write a request to the server
	 * @param oStream Stream obtained through a secure socket on the server
	 * @throws GATInvocationException 
	 */
	public void writeServerRequest(OutputStream oStream) throws GATInvocationException {
		Document requestDocument = buildRequestDocument();
		DOMSource transSource = new DOMSource(requestDocument);
		StreamResult result = new StreamResult(oStream);

		try {
			// this transformer will just copy the original to the result,
			// because the transformer factory used the empty transformer source
			transformer.transform(transSource, result);
			oStream.flush();
		} catch (TransformerException e) {
			throw new GATInvocationException("Could not copy to remote server using transformer ", e);
		} catch (IOException e) {
			throw new GATInvocationException("Could not communicate with remote server", e);
		}
	}
	
	/**
	 * Read the response that is given back after a request from the server.
	 * @param iStream The input stream which was obtained through a secure socket to the server
	 * @return VomsServerResponse data structure with received ACs and/or error messages
	 * @throws GATInvocationException If there are problems during parsing and I/O
	 */
	public VomsServerResponse readServerResponse(InputStream iStream) throws GATInvocationException {
		
		VomsServerResponse response = new VomsServerResponse();
		
		try {
			Document responseDocument = docBuilder.parse(iStream);

			NodeList list = responseDocument.getElementsByTagName(VOMS_ANSWER_EL);

			for (int i = 0; i < list.getLength(); i++) {
				Node current = list.item(i);
				parseResponseACs(current, response);
			}

		} catch (SAXException e) {
			throw new GATInvocationException("Encountered error upon parsing the server response!", e);
		} catch (IOException e) {
			throw new GATInvocationException("Error upon reading from the server response socket!", e);
		}

		return response;
	}

	/**
	 * The content of the Voms answer will be parsed for bistrings, ACs or errors
	 * ACs and bitstrings have to be base64-decoded and are then stored in the given
	 * VomsServerResponse class
	 * 
	 * @param node The root node from which to search for ACs and bitstrings
	 * @param response The server response in which the decoded ACs and errors get stored
	 * @throws VomsProxyException If an unexpected node name is encountered
	 */
	private void parseResponseACs(Node node, VomsServerResponse response)
			throws GATInvocationException {
		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node current =  list.item(i);
			String nodeName = current.getNodeName();

			if (VOMS_VERSION.equals(nodeName)) {
				//The version is now provided
			}
			
			else if (BITSTRING_ANSWER_EL.equals(nodeName)) {
				response.setBitstr(base64Decode(current.getFirstChild().getNodeValue(), vomsDecodeMap));
			}

			else if (AC_ANSWER_EL.equals(nodeName))  {
				response.setAc(base64Decode(current.getFirstChild().getNodeValue(), vomsDecodeMap));
			}
				//response.setAc(base64Decode(current.getTextContent(), vomsDecodeMap));

			else if (ERROR_ANSWER_EL.equals(nodeName)) {
				parseErrors((Element) current, response);
			}
			
			else {
				throw new GATInvocationException("Invalid node name: " + nodeName);
			}
		}
	}
	
	/**
	 * Store all error messages in the VomsServerResponse
	 * @param errorRoot The root from which to search for error messages
	 * @param response The response in which to store error messages
	 */
	private void parseErrors(Element errorRoot, VomsServerResponse response) {
		NodeList errMsgList = errorRoot.getElementsByTagName(ERROR_MSG_EL);
		
		for (int i = 0; i < errMsgList.getLength(); i++) {
			Node currentNode = errMsgList.item(i);
			String messageText = currentNode.getFirstChild().getNodeValue();
			//String messageText = currentNode.getTextContent();
			response.getErrors().add(messageText);
		}
	}

	/**
	 * Build a XML request document which can be sent to the server
	 * This contains a <voms> root and a FQAN request part and a lifetime part
	 * @return The constructed XML document
	 */
	private Document buildRequestDocument() {
		Document domDocument = docBuilder.newDocument();
		Node rootNode = domDocument.createElement(ROOT_EL_NAME);

		// now compute the FQANs that are going to be part of the server request
		DocumentFragment requestPart = domDocument.createDocumentFragment();

		// now build the command part
		String cmd = getFQANCommandString();
		Element commandElement = domDocument.createElement(COMMAND_EL_NAME);

		commandElement.appendChild(domDocument.createTextNode(cmd));
		requestPart.appendChild(commandElement);

		// the lifetime part
		Element lifetimeElement = domDocument.createElement(LIFETIME_EL_NAME);
		lifetimeElement.appendChild(domDocument.createTextNode(String
				.valueOf(lifetime)));
		requestPart.appendChild(lifetimeElement);

		rootNode.appendChild(requestPart);
		domDocument.appendChild(rootNode);

		return domDocument;
	}

	/**
	 * Get a legal command string from the stored fqan information
	 * @return String representing the FQAN information
	 */
	private String getFQANCommandString() {
		String commandString = "";

		if (this.requestString.indexOf(ROLE_IDENTIFIER) > (-1)) {
			commandString = QUAL_GROUP_COMMAND + GROUP_SEPARATOR + this.requestString;
		} else {
			commandString = GROUP_COMMAND + GROUP_SEPARATOR + this.requestString;
		}

		return commandString;
	}

/**
  * The Base 64 encoder/decoder uses a custom matrix, hence standard classes 
  * for base-64 encoding decoding cannot be used.
  * A decoding that works has been developed in the Acacia project.
  * 
  * 
  * Copyright (c) 2007 , Acacia project authors (see AUTHORS for list)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Acacia project nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE ACACIA PROJECT AUTHORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE ACACIA PROJECT AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Base64 decode a given string using the lookup-table of VomsServerResponse
  * 
  * @param s The base64-encoded String
  * @return decoded byte sequence
  */
	public static byte[] base64Decode(String s , byte[] decoding_map ) {

	    char[] in = s.toCharArray() ;
	 
	    int iLen = in.length ;

	    int oLen = (iLen*3) / 4 ;
	    byte[] out = new byte[oLen] ;
	    int ip = 0 ;
	    int op = 0 ;
	    while( ip < iLen )
	    {
	      int i0 = in[ip++] ;
	      int i1 = in[ip++] ;
	      int i2 = ip < iLen ? in[ip++] : 'A' ;
	      int i3 = ip < iLen ? in[ip++] : 'A' ;

	      if( i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127 )
	         throw new IllegalArgumentException( "Illegal character in Base64 encoded data." ) ;

	      int b0 = decoding_map[i0] ;
	      int b1 = decoding_map[i1] ;
	      int b2 = decoding_map[i2] ;
	      int b3 = decoding_map[i3] ;
	      if( b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0 )
	         throw new IllegalArgumentException( "Illegal character in Base64 encoded data." ) ;

	      int o0 = (  b0         << 2 ) | ( b1 >>> 4 ) ;
	      int o1 = ( (b1 & 0xf ) << 4 ) | ( b2 >>> 2 ) ;
	      int o2 = ( (b2 &   3 ) << 6 ) |   b3 ;

	      out[op++] = (byte)o0;
	      if (op<oLen) out[op++] = (byte)o1 ;
	      if (op<oLen) out[op++] = (byte)o2 ;

	    }

	    return out ;

	  }
}
