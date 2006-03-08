/*
 * Created on Apr 19, 2004
 */
package org.gridlab.gat.advert;

import java.util.NoSuchElementException;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 * 
 * The GATAdvertService allows GATAdvertisable instances to get published to and
 * queried in an advert directory. Such an advert directory is a meta data
 * directory with an hierarchical namespace attached.
 */
public interface AdvertService extends Monitorable {

	/**
	 * Add an Advertizable instance and related meta data to the
	 * GATAdvertService, at path (absolute or relative to PWD). If an
	 * GATAdvertService entry exists at the specified path, that entry gets
	 * overwritten, and a warning is issued.
	 * 
	 * @param advert
	 *            instance to be entered into the GATAdvertService.
	 * @param metaData
	 *            Meta data to be associated with the passed GATAdvertiseable.
	 * @param path
	 *            Path (either absolute or relative to PWD) of the new entry.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
 	 */
	public void add(Advertisable advert, MetaData metaData, String path)
			throws GATInvocationException;

	/**
	 * Remove an Advertizable instance and related meta data from the
	 * GATAdvertService, at path (absolute or relative to PWD). If
	 * 
	 * @param path
	 *            Path (either absolute or relative to PWD) of the entry to be
	 *            deleted.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
	 */
	public void delete(String path) throws NoSuchElementException,
			GATInvocationException;


	/**
	 * @param path
	 *            Path (either absolute or relative to PWD) of the entry.
	 * @return A MetaData containing the metadata.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 */
	public Advertisable getAdvertisable(String path) throws GATInvocationException, NoSuchElementException;

	/**
	 * @param path
	 *            Path (either absolute or relative to PWD) of the entry.
	 * @return A MetaData containing the metadata.
	 * @throws NoSuchElementException
	 *             The path is incorrect.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
	 */
	public MetaData getMetaData(String path) throws NoSuchElementException,
			GATInvocationException;

	/**
	 * Query the GATAdvertService for entries matching the specified set of meta
	 * data. The returned paths can be destroyed at any time.
	 * 
	 * @param metaData
	 *            Meta data describing the entries to be searched for.
	 * @return Paths, each pointing to a matching entry. If no mathces are
	 *         found, null is returned.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
	 */
	public String[] find(MetaData metaData) throws GATInvocationException;

	/**
	 * Specify the element of the GATAdvertService namespace to be used as
	 * reference for relative paths.
	 * 
	 * @param path
	 *            New absolute or relative reference path.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
	 */
	public void setPWD(String path) throws GATInvocationException;

	/**
	 * Returns the current element of the GATAdvertService namespace used as
	 * reference for relative paths.
	 * 
	 * @return Absolute reference path.
	 * @throws GATInvocationException this exception is thrown when all adaptors fail on this method
	 *  it contains a tree of exceptions that were the causes.
	 */
	public String getPWD() throws GATInvocationException;
}