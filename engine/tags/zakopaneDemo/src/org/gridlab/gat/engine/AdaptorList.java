/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.util.ArrayList;

/**
 * @author rob
 */
class AdaptorList {

	/** The fully qualified name of the class the adaptors in this set. */
	String cpi;

	/** The api class all adaptors in this set implement. */
	Class cpiClass;

	/** A list of the adaptors. The type of the elements is "Adaptor" */
	ArrayList adaptors;

	/**
	 * @param cpiClass
	 *            The api class all adaptors in this set implement.
	 */
	AdaptorList(Class cpiClass) {
		this.cpi = cpiClass.getName();
		this.cpiClass = cpiClass;
		adaptors = new ArrayList();
	}

	void addAdaptor(Adaptor a) {
		adaptors.add(a);
	}

	int size() {
		return adaptors.size();
	}

	Adaptor get(int pos) {
		return (Adaptor) adaptors.get(pos);
	}
}