/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @author rob
 */
public class GATInvocationException extends Exception {

	ArrayList throwables = new ArrayList();

	ArrayList adaptorNames = new ArrayList();

	public GATInvocationException(String s) {
		super(s);
	}

	public GATInvocationException() {
		super();
	}

	public GATInvocationException(String adaptor, Throwable t) {
		super();
		add(adaptor, t);
	}

	public void add(String adaptor, Throwable t) {
		if (t instanceof InvocationTargetException) {
			t = t.getCause();
		}
		throwables.add(t);
		adaptorNames.add(adaptor);
	}

	public String toString() {
		String res = "";

		if (throwables.size() == 0) {
			return super.toString();
		}

		for (int i = 0; i < throwables.size(); i++) {
			if (adaptorNames.get(i) != null) {
				res += adaptorNames.get(i) + "failed because of: ";
			}
			res += throwables.get(i);
			res += "\n";
		}
		return res;
	}
}