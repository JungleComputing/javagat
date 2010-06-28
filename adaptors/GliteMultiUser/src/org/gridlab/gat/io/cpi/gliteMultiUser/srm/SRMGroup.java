package org.gridlab.gat.io.cpi.gliteMultiUser.srm;

import org.gridlab.gat.io.attributes.GroupPrincipal;
import org.gridlab.gat.io.cpi.gliteMultiUser.srm.SRMGroup;

/**
 * @author Jerome Revillard
 */
public class SRMGroup implements GroupPrincipal {
	protected final int gid; // gid
	protected final String name;

	protected SRMGroup(int gid, String name) {
		this.gid = gid;
		this.name = name;
	}

	/**
	 * @param name The VO group (i.e: hec/pdi)
	 */
	public SRMGroup(String name) {
		this(-1, name);
	}

	public int gid() {
		return gid;
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
		if (!(obj instanceof SRMGroup)){
			return false;
		}
		SRMGroup other = (SRMGroup) obj;
		if ((this.gid != other.gid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
