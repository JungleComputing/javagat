/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.util.ArrayList;

/**
 * @author rob
 */
class AdaptorList {

	String cpi;

	/** The fully qualified name of the class the adaptors in this set. */
	Class cpiClass;

	/** The api class all adaptors in this set implement. */
	ArrayList adaptors;

	/** A list of the adaptors. The type of the elements is "Adaptor" */

	/**
	 * @param cpi
	 *            The fully qualified name of the class the adaptors in this
	 *            set.
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