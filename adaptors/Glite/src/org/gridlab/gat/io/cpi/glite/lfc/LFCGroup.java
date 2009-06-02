package org.gridlab.gat.io.cpi.glite.lfc;

import org.gridlab.gat.io.attributes.GroupPrincipal;

/**
 * @author Jerome Revillard
 */
public class LFCGroup implements GroupPrincipal {
	protected final int gid; // gid
	protected final String name;

	protected LFCGroup(int gid, String name) {
		this.gid = gid;
		this.name = name;
	}

	/**
	 * @param name The VO group (i.e: hec/pdi)
	 */
	public LFCGroup(String name) {
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
		if (!(obj instanceof LFCGroup)){
			return false;
		}
		LFCGroup other = (LFCGroup) obj;
		if ((this.gid != other.gid)) {
			return false;
		}
		if ((this.name != other.name)) {
			return false;
		}
		return true;
	}
}
