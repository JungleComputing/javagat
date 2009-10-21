/*
 * Created on Apr 19, 2004
 */
package org.gridlab.gat.advert.cpi;

import java.util.Map;
import java.util.NoSuchElementException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

/**
 * @author rob
 * 
 * The GATAdvertService allows GATAdvertisable instances to get published to and
 * queried in an advert directory. Such an advert directory is a meta data
 * directory with an hierarchical namespace attached.
 */
public class AdvertServiceCpi extends MonitorableCpi implements AdvertService {
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("add", false);
        capabilities.put("delete", false);
        capabilities.put("getMetaData", false);
        capabilities.put("getAdvertisable", false);
        capabilities.put("find", false);
        capabilities.put("setPWD", false);
        capabilities.put("getPWD", false);
        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = new Preferences();
        return preferences;
    }
    
    protected GATContext gatContext;

    /**
     * Create an instance of the AdvertService using the provided preference.
     * 
     * @param gatContext
     *                The context to use.
     * @param preferences
     *                The user preferences.
     */
    protected AdvertServiceCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }

    /**
     * Add an Advertizable instance and related meta data to the
     * GATAdvertService, at path (absolute or relative to PWD). If an
     * GATAdvertService entry exists at the specified path, that entry gets
     * overwritten, and a warning is issued.
     * 
     * @param advert
     *                instance to be entered into the GATAdvertService.
     * @param metaData
     *                Meta data to be associated with the passed
     *                GATAdvertiseable.
     * @param path
     *                Path (either absolute or relative to PWD) of the new
     *                entry.
     */
    public void add(Advertisable advert, MetaData metaData, String path)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Remove an Advertizable instance and related meta data from the
     * GATAdvertService, at path (absolute or relative to PWD). If
     * 
     * @param path
     *                Path (either absolute or relative to PWD) of the entry to
     *                be deleted.
     * @throws NoSuchElementException
     *                 The path is incorrect.
     */
    public void delete(String path) throws GATInvocationException,
            NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @param path
     *                Path (either absolute or relative to PWD) of the entry.
     * @return A MetaData containing the metadata.
     * @throws NoSuchElementException
     *                 The path is incorrect.
     */
    public MetaData getMetaData(String path) throws GATInvocationException,
            NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @param path
     *                Path (either absolute or relative to PWD) of the entry.
     * @return An Advertisable containing the advertisable.
     * @throws GATInvocationException 
     * @throws NoSuchElementException
     *                 The path is incorrect.
     */
    public Advertisable getAdvertisable(String path)
            throws GATInvocationException, NoSuchElementException {
        return getAdvertisable(gatContext, path);
    }
    

    /**
     * @param path
     *                Path (either absolute or relative to PWD) of the entry.
     * @param context The context with which to create the advertisable object.
     * @return An Advertisable containing the advertisable.
     * @throws NoSuchElementException
     *                 The path is incorrect.
     */
    public Advertisable getAdvertisable(GATContext context, String path)
            throws GATInvocationException, NoSuchElementException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Query the GATAdvertService for entries matching the specified set of meta
     * data. The returned paths can be destroyed at any time.
     * 
     * @param metaData
     *                Meta data describing the entries to be searched for.
     * @return Paths, each pointing to a matching entry. If no mathces are
     *         found, null is returned.
     */
    public String[] find(MetaData metaData) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Specify the element of the GATAdvertService namespace to be used as
     * reference for relative paths.
     * 
     * @param path
     *                New absolute or relative reference path.
     */
    public void setPWD(String path) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the current element of the GATAdvertService namespace used as
     * reference for relative paths.
     * 
     * @return Absolute reference path.
     */
    public String getPWD() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void exportDataBase(org.gridlab.gat.URI target)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void importDataBase(org.gridlab.gat.URI target)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
