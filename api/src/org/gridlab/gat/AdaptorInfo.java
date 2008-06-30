package org.gridlab.gat;

import java.util.Map;

public class AdaptorInfo {

    String fullname;

    String shortname;

    String type;

    String description;

    Preferences supportedPreferences;

    Map<String, Boolean> capabilities;

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

    public Preferences getSupportedPreferences() {
        return supportedPreferences;
    }

    public String getShortName() {
        return shortname;
    }

    public Map<String, Boolean> getSupportedCapabilities() {
        return capabilities;
    }

    public String getDescription() {
        return description;
    }

}
