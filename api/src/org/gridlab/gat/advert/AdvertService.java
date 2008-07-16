/*
 * Created on Apr 19, 2004
 */
package org.gridlab.gat.advert;

import java.util.NoSuchElementException;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * The AdvertService allows {@link Advertisable} instances to get published to
 * and queried in an advert directory. Such an advert directory is a meta data
 * directory with an hierarchical namespace attached, it exists in memory, but
 * can be exported to, or imported from persistent memory (hard disk).
 * 
 * @author rob
 */
public interface AdvertService extends Monitorable {
    /**
     * Add an {@link Advertisable} instance and related meta data to the
     * {@link AdvertService}, at path (absolute or relative to PWD). If an
     * {@link AdvertService} entry exists at the specified path, that entry gets
     * overwritten, and a warning is issued.
     * 
     * @param advert
     *                instance to be entered into the {@link AdvertService}.
     * @param metaData
     *                Meta data to be associated with the passed
     *                {@link Advertisable}.
     * @param path
     *                Path (either absolute or relative to PWD) of the new
     *                entry.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public void add(Advertisable advert, MetaData metaData, String path)
            throws GATInvocationException;

    /**
     * Remove an {@link Advertisable} instance and related meta data from the
     * {@link AdvertService}, at path (absolute or relative to PWD).
     * 
     * @param path
     *                Path (either absolute or relative to PWD) of the entry to
     *                be deleted.
     * @throws NoSuchElementException
     *                 The path is incorrect.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public void delete(String path) throws NoSuchElementException,
            GATInvocationException;

    /**
     * Gets an {@link Advertisable} instance from the given path (absolute or
     * relative to PWD).
     * 
     * @param path
     *                Path (either absolute or relative to PWD) of the entry.
     * @return The {@link Advertisable} instance at the given path
     * @throws NoSuchElementException
     *                 The path is incorrect.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public Advertisable getAdvertisable(String path)
            throws GATInvocationException, NoSuchElementException;

    /**
     * Gets the {@link MetaData} of an {@link Advertisable} instance from the
     * given path (absolute or relative to the PWD).
     * 
     * @param path
     *                Path (either absolute or relative to PWD) of the entry.
     * @return A {@link MetaData} containing the meta data.
     * @throws NoSuchElementException
     *                 The path is incorrect.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public MetaData getMetaData(String path) throws NoSuchElementException,
            GATInvocationException;

    /**
     * Query the {@link AdvertService} for entries matching the specified set of
     * meta data in the {@link MetaData}.
     * 
     * @param metaData
     *                {@link MetaData} describing the entries to be searched
     *                for.
     * @return a {@link String}[] of absolute paths, each pointing to a
     *         matching entry. If no matches are found, <code>null</code> is
     *         returned.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public String[] find(MetaData metaData) throws GATInvocationException;

    /**
     * Specify the element of the {@link AdvertService} name space to be used as
     * reference for relative paths. This method is equivalent to perform a 'cd'
     * in the name space. A relative path will be appended to the current path.
     * 
     * @param path
     *                New absolute or relative reference path.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public void setPWD(String path) throws GATInvocationException;

    /**
     * Returns the current element of the {@link AdvertService} name space used
     * as reference for relative paths.
     * 
     * @return absolute reference path.
     * @throws GATInvocationException
     *                 this exception is thrown when all adaptors fail on this
     *                 method it contains a tree of exceptions that were the
     *                 causes.
     */
    public String getPWD() throws GATInvocationException;

    /**
     * Exports the advert database to persistent memory located at the given
     * {@link URI}.
     * 
     * @param target
     *                the location where the advert database should be exported
     *                to.
     * @throws GATInvocationException
     *                 if something fails during the export
     */
    public void exportDataBase(org.gridlab.gat.URI target)
            throws GATInvocationException;

    /**
     * Imports the advert database from persistent memory located at the given
     * {@link URI}.
     * 
     * @param source
     *                the location where the advert database should be imported
     *                from.
     * @throws GATInvocationException
     *                 if something fails during the import
     */
    public void importDataBase(org.gridlab.gat.URI source)
            throws GATInvocationException;
}
