package org.gridlab.gat;

import java.util.Map;

/**
 * An object that can hold information about a specific adaptor.
 * 
 * This object contains the full and short name of the adaptor, the type (for
 * instance File, ResourceBroker, etc.), a description in text, the preferences
 * that are supported by this adaptor and which methods it implements (the
 * capabilities of the adaptor).
 * 
 * @author rkemp
 */
public class AdaptorInfo {

    private String fullname;

    private String shortname;

    private String type;

    private String description;

    private Preferences supportedPreferences;

    private Map<String, Boolean> capabilities;

    /**
     * Constructs an AdaptorInfo object.
     * 
     * @param fullname
     *                the full name of this adaptor (for instance
     *                org.gridlab.gat.resources.cpi.globus.GlobusResourceBrokerAdaptor)
     * @param shortname
     *                the short name of this adaptor (for instance
     *                GlobusResourceBrokerAdaptor)
     * @param type
     *                the type of this adaptor (for instance ResourceBroker)
     * @param supportedPreferences
     *                a {@link Preferences} object that contains all the keys
     *                that are supported by this adaptor, the values are the
     *                default values
     * @param capabilities
     *                A {@link Map} where the keys are method names and the
     *                values a boolean indicating whether this method is
     *                implemented.
     * @param description
     *                A text description of this adaptor, with additional
     *                information about this adaptor
     */
    public AdaptorInfo(String fullname, String shortname, String type,
            Preferences supportedPreferences,
            Map<String, Boolean> capabilities, String description) {
        this.fullname = fullname;
        this.shortname = shortname;
        this.type = type;
        this.supportedPreferences = supportedPreferences;
        this.capabilities = capabilities;
        this.description = description;
    }

    /**
     * Returns a String representation of this object.
     * 
     * @return The String representation of this object.
     */
    public String toString() {
        String result = "";
        result += "full name: \t" + fullname + "\n";
        result += "short name: \t" + shortname + "\n";
        result += "type name: \t" + type + "\n";
        result += "description: \t" + description + "\n";
        result += "supported preferences (key=default value):\n";
        if (supportedPreferences == null) {
            result += "\t\tnone\n";
        } else {
            for (String key : supportedPreferences.keySet()) {
                result += "\t\t" + key + "=" + supportedPreferences.get(key)
                        + "\n";
            }
        }
        result += "supported capabilities (method=supported):\n";
        if (capabilities == null) {
            result += "\t\tnone\n";
        } else {
            for (String key : capabilities.keySet()) {
                result += "\t\t" + key + "=" + capabilities.get(key) + "\n";
            }
        }
        return result;
    }

    /**
     * Returns a {@link Preferences} object where the keys are supported by this
     * adaptor, and the values are the default values for the corresponding
     * keys.
     * 
     * @return a {@link Preferences} object where the keys are supported by this
     *         adaptor, and the values are the default values for the
     *         corresponding keys.
     * 
     */
    public Preferences getSupportedPreferences() {
        return supportedPreferences;
    }

    /**
     * Returns the short name of this adaptor.
     * 
     * @return the short name of this adaptor.
     */
    public String getShortName() {
        return shortname;
    }

    /**
     * Returns a {@link Map}<{@link String}, {@link Boolean}> where the keys
     * are the method names and the values boolean indicating whether the method
     * is implemented.
     * 
     * @return a {@link Map}<{@link String}, {@link Boolean}> where the keys
     *         are the method names and the values boolean indicating whether
     *         the method is implemented.
     */
    public Map<String, Boolean> getSupportedCapabilities() {
        return capabilities;
    }

    /**
     * Returns the text description of this adaptor, with additional
     * information.
     * 
     * @return the text description of this adaptor, with additional
     *         information.
     */
    public String getDescription() {
        return description;
    }

}
