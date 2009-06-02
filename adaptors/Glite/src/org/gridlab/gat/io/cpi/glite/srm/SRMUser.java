package org.gridlab.gat.io.cpi.glite.srm;

import org.gridlab.gat.io.attributes.UserPrincipal;

/**
 * @author Jerome Revillard
 */
public class SRMUser implements UserPrincipal {
	protected final int uid; // uid
	protected final String name;

	protected SRMUser(int uid, String name) {
		this.uid = uid;
		this.name = name;
	}

	/**
	 * @param name The certificate header of the user (i.e: /C=ES/O=Maat_GKnowledge/CN=Jerome Revillard)
	 */
	public SRMUser(String name) {
		this(-1, name);
	}

	public int uid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this){
			return true;
		}
		if (!(obj instanceof SRMUser)){
			return false;
		}
		SRMUser other = (SRMUser) obj;
		if ((this.uid != other.uid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
