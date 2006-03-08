/*
 * Created on May 18, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.gridlab.gat.GATInvocationException;

/**
 * @author rob
 */
public class AdaptorInvocationHandler implements InvocationHandler {

	Object[] adaptors;

	public AdaptorInvocationHandler(Object[] adaptors) {
		this.adaptors = adaptors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 *      java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method m, Object[] params)
			throws Throwable {
		GATInvocationException e = null;

		if (adaptors == null) {
			throw new GATInvocationException("no adaptor available for method "
					+ m);
		}

		for (int i = 0; i < adaptors.length; i++) {
			try {
				Object res = m.invoke(adaptors[i], params);
				return res; // return on first successful adaptor
			} catch (Throwable t) {
				//                System.err.println("GOT EXC: " + t);
				if (e == null)
					e = new GATInvocationException();
				e.add(adaptors[i].getClass().toString(), t);
			}
		}

		throw e;
	}
}