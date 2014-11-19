package org.gridlab.gat.security.glite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.gridlab.gat.GATInvocationException;

/**
 * Manages the response received from the VOMS server.
 *
 */
public class VomsServerResponse {
	/** Collect everything marked with the bitstr tag here */
	private byte bitstr[];
	/** Collect the ac content, which is marked with an ac tag here */
	private byte acBytes[];
	/** Collect all errors that occur in this list */
	private List<String> errors;
	/** AttributeCertificate representation of the ac data */
	private AttributeCertificate atCert = null;
	
	/**
	 * Create a server response collection class
	 */
	public VomsServerResponse() {
		errors = new ArrayList<String>();
	}

	/**
	 * 
	 * @return The bitstring
	 */
	public byte[] getBitstrRaw() {
		return bitstr;
	}

	/**
	 * 
	 * @return String representation of the bitstr.
	 */
	public String getBitstr() {
		return new String(bitstr);
	}

	/**
	 * Get the attribute certificate from the bytes received from the server
	 * @return The attribute certificate
	 * @throws IOException 
	 */
	public AttributeCertificate getAc() throws GATInvocationException, IOException {

		if (acBytes != null && atCert == null) {
			ASN1InputStream asn1Stream = new ASN1InputStream(acBytes);
			ASN1Primitive dObj = asn1Stream.readObject();
			asn1Stream.close();

			if (!(dObj instanceof ASN1Sequence)) {
				throw new GATInvocationException("Invalid DER object found in AC");
			}
			
			atCert = new AttributeCertificate((ASN1Sequence) dObj);

		}
		return atCert;
	}

	public byte[] getAcRaw() {
		return acBytes;
	}

	public List<String> getErrors() {
		return this.errors;
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	
	void setBitstr(final byte [] data) { 
		this.bitstr = data;
	}
	
	void setAc(final byte [] data) {
		this.acBytes = data;
	}
	
	/**
	 * Return a String representation of the server response
	 * 
	 * @return a String containing information about the model class
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer("");
		buffer.append("VomsServerResponse - container for response from the VOMS server \n");
	
		try {
			final AttributeCertificate atCert = getAc();
			buffer.append("AC-information: \n");
			buffer.append("Valid not before: ").append(atCert.getAcinfo().getAttrCertValidityPeriod().getNotBeforeTime()).append("\n");
			buffer.append("Valid not after: ").append(atCert.getAcinfo().getAttrCertValidityPeriod().getNotAfterTime()).append("\n");
		} catch (Exception e) {
			buffer.append("Could not determine properties of the attribute certificate!\n");
		}
		
		buffer.append("**********************************************\n");
		buffer.append("Stored error messages\n");
		
		for (String error : errors) {
			buffer.append(error).append("\n");
		}
		
		
		return buffer.toString();
	}
}
