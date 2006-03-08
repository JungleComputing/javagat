package org.gridlab.gat.io.cpi.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.cpi.FileCpi;

public class LocalFileAdaptor extends FileCpi {

	File f;

	/**
	 * @param gatContext
	 * @param preferences
	 * @param location
	 */
	public LocalFileAdaptor(GATContext gatContext, Preferences preferences,
			URI location) throws GATObjectCreationException {
		super(gatContext, preferences, location);

		location = correctURI(location);

		if (GATEngine.DEBUG) {
			System.err.println("LocalFileAdaptor: LOCATION = " + location);
		}

		if (!location.getScheme().equals("file") || location.getHost() != null) {
			throw new GATObjectCreationException(
					"The LocalFileAdaptor can only handle local files.");
		}

		f = new File(IPUtils.getPath(location));
	}

	// Make life a bit easier for the programmer:
	// If the URI does not have a scheme part, just consider it a local
	// file.
	// The ctor of java.io.file does not accept this.
	protected URI correctURI(URI in) {
		if (in.getScheme() == null) {
			try {
				return new URI("file:///" + IPUtils.getPath(in));
			} catch (URISyntaxException e) {
				throw new Error("internal error in LocalFile: " + e);
			}
		}
		
		return in;
		/*
		 * URI tmpLocation = in;
		 * 
		 * if (in.getScheme() == null) { java.io.File tmp = new
		 * java.io.File(in.toString()); tmpLocation = tmp.toURI(); }
		 * 
		 * return tmpLocation;
		 */
	}

	/**
	 * This method copies the physical file represented by this File instance to
	 * a physical file identified by the passed URI.
	 * 
	 * @param loc
	 *            The new location
	 * @throws java.io.IOException
	 *             Upon non-remote IO problem
	 */
	public void copy(URI loc) throws GATInvocationException, IOException {
		if (!IPUtils.isLocal(loc)) {
			throw new GATInvocationException(
					"default file: cannot copy to remote destination");
		}

		if (GATEngine.VERBOSE) {
			System.err.println("local copy of " + getPath() + " to "
					+ IPUtils.getPath(loc));
		}

		if (loc.equals(toURI())) {
			if (GATEngine.DEBUG) {
				System.err
						.println("local copy, source is the same file as dest.");
			}
			return;
		}

		if (!exists()) {
			throw new GATInvocationException(
					"the local source file does not exist, path = " + getPath());
		}

		if (isDirectory()) {
			if (GATEngine.DEBUG) {
				System.err.println("local copy, it is a dir");
			}
			copyDirectory(gatContext, preferences, toURI(), loc);
			return;
		}

		if (GATEngine.DEBUG) {
			System.err.println("local copy, it is a file");
		}

		File tmp = new File(IPUtils.getPath(correctURI(loc)));

		// if the destination URI is a dir, append the file name.
		if (tmp.isDirectory()) {
			String u = loc.toString() + "/" + getName();
			try {
				loc = new URI(u);
			} catch (URISyntaxException e) {
				throw new GATInvocationException("default file", e);
			}
		}

		// Create destination file
		File destinationFile = new File(IPUtils.getPath(correctURI(loc)));

		if (GATEngine.DEBUG) {
			System.err.println("creating local file " + destinationFile);
		}
		destinationFile.createNewFile();

		// Copy source to destination
		FileInputStream in = new FileInputStream(f);
		BufferedInputStream inBuf = new BufferedInputStream(in);

		FileOutputStream out = new FileOutputStream(destinationFile);
		BufferedOutputStream outBuf = new BufferedOutputStream(out);

		try {
			long bytesWritten = 0;
			byte[] buf = new byte[1024];
			while(bytesWritten != f.length()) {
				int len = inBuf.read(buf);
				outBuf.write(buf, 0, len);
				bytesWritten += len;
			}
		} catch (IOException e) {
			throw new GATInvocationException("default file", e);
		} finally {
			try {
				outBuf.close();
			} catch (IOException e) {
				throw new GATInvocationException("default file", e);
			}
			try {
				inBuf.close();
			} catch (IOException e) {
				throw new GATInvocationException("default file", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canRead()
	 */
	public boolean canRead() {
		return f.canRead();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canWrite()
	 */
	public boolean canWrite() {
		return f.canWrite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#compareTo(gat.io.File)
	 */
	public int compareTo(org.gridlab.gat.io.File other) {
		return f.compareTo(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#compareTo(java.lang.Object)
	 */
	public int compareTo(Object other) {
		return f.compareTo(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#createNewFile()
	 */
	public boolean createNewFile() throws IOException {
		return f.createNewFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#delete()
	 */
	public boolean delete() {
		return f.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#exists()
	 */
	public boolean exists() {
		return f.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getAbsoluteFile()
	 */
	public java.io.File getAbsoluteFile() throws GATInvocationException {
		try {
			return GAT.createFile(gatContext, preferences, new URI(f.getAbsoluteFile().getPath()));
		} catch (Exception e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getAbsolutePath()
	 */
	public String getAbsolutePath() {
		return f.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalFile()
	 */
	public java.io.File getCanonicalFile() throws GATInvocationException,
			IOException {
		try {
			return GAT.createFile(gatContext, preferences, new URI(f.getCanonicalFile().getPath()));
		} catch (Exception e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws IOException {
		return f.getCanonicalPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getParent()
	 */
	public String getParent() {
		return f.getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getParentFile()
	 */
	public java.io.File getParentFile() throws GATInvocationException {
		try {
			return GAT.createFile(gatContext, preferences, new URI(f.getParentFile().getPath()));
		} catch (Exception e) {
			throw new GATInvocationException("default file", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isAbsolute()
	 */
	public boolean isAbsolute() {
		return f.isAbsolute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isDirectory()
	 */
	public boolean isDirectory() {
		return f.isDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isFile()
	 */
	public boolean isFile() {
		return f.isFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isHidden()
	 */
	public boolean isHidden() {
		return f.isHidden();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException, IOException {
		return f.lastModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#length()
	 */
	public long length() throws GATInvocationException, IOException {
		return f.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#list()
	 */
	public String[] list() throws IOException, GATInvocationException {
		return f.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#list(java.io.FilenameFilter)
	 */
	public String[] list(FilenameFilter arg0) {
		return f.list(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles()
	 */
	public java.io.File[] listFiles() throws GATInvocationException {
		File[] r = f.listFiles();
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, new URI(r[i].getPath()));
			} catch (Exception e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles(java.io.FileFilter)
	 */
	public java.io.File[] listFiles(FileFilter arg0)
			throws GATInvocationException {
		File[] r = f.listFiles(arg0);
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, new URI(r[i].getPath()));
			} catch (Exception e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#listFiles(java.io.FilenameFilter)
	 */
	public java.io.File[] listFiles(FilenameFilter arg0)
			throws GATInvocationException {
		File[] r = f.listFiles(arg0);
		org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

		for (int i = 0; i < r.length; i++) {
			try {
				res[i] = GAT.createFile(gatContext, preferences, new URI(r[i].getPath()));
			} catch (Exception e) {
				throw new GATInvocationException("default file", e);
			}
		}
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException, IOException {
		return f.mkdir();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdirs()
	 */
	public boolean mkdirs() {
		return f.mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#move(java.net.URI)
	 */
	public void move(URI destination) throws IOException,
			GATInvocationException {
		File tmp = new File(IPUtils.getPath(destination));
		boolean res = f.renameTo(tmp);
		if (!res)
			throw new IOException("Could not move file");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#renameTo(java.io.File)
	 */
	public boolean renameTo(java.io.File arg0) throws GATInvocationException {
		File tmp = new File(arg0.toURI());
		return f.renameTo(tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#setLastModified(long)
	 */
	public boolean setLastModified(long arg0) {
		return f.setLastModified(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#setReadOnly()
	 */
	public boolean setReadOnly() {
		return f.setReadOnly();
	}
}