/*
 * Created on May 14, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @author rob
 */
class NestedException extends Exception {

	ArrayList throwables = new ArrayList();

	ArrayList adaptorNames = new ArrayList();

	public NestedException(String s) {
		super(s);
	}

	public NestedException() {
		super();
	}

	public NestedException(String adaptor, Throwable t) {
		super();
		add(adaptor, t);
	}

	public void add(String adaptor, Throwable t) {
		if (t instanceof InvocationTargetException) {
			t = t.getCause();
		}

		if (t instanceof NestedException) {
			NestedException ge = (NestedException) t;
			if (ge.throwables.size() == 1) {
				t = (Throwable) ge.throwables.get(0);
				adaptor = (String) ge.adaptorNames.get(0);
			}
		}

		throwables.add(t);
		adaptorNames.add(adaptor);
	}

	public String toString() {
		String res = "";

		if (throwables.size() == 0) {
			return super.toString();
		}

		res = "\n--- START OF NESTED EXCEPTION ---\n";
		for (int i = 0; i < throwables.size(); i++) {
			if (adaptorNames.get(i) != null) {
				res += "*** " + adaptorNames.get(i)
						+ " adaptor failed because of: ";
			}
			String msg = ((Throwable) throwables.get(i)).getMessage();
			if (msg == null)
				msg = ((Throwable) throwables.get(i)).toString();
			res += msg;
			res += "\n";
		}
		res += "--- END OF NESTED EXCEPTION ---\n";
		return res;
	}

	public void printStackTrace() {
		if (throwables.size() == 0) {
			super.printStackTrace();
			return;
		}

		System.err.println("--- START OF NESTED EXCEPTION STACK TRACE ---");
		for (int i = 0; i < throwables.size(); i++) {
			if (adaptorNames.get(i) != null) {
				System.err.println("*** stack trace of " + adaptorNames.get(i));
			}
			((Throwable) throwables.get(i)).printStackTrace();
		}
		System.err.println("--- END OF NESTED EXCEPTION STACK TRACE ---");
	}
}