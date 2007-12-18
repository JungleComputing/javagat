package services.AdvertServiceEntry.impl;

import javax.xml.namespace.QName;

public interface AdvertServiceEntryConstants
{
    public static final String NS = "http://www.JavaGAT.org/namespaces/AdvertServiceEntry/AdvertServiceEntryService";
    public static final QName RP_PATH = new QName(NS, "Path");
    public static final QName RP_METADATA = new QName(NS, "MetaData");
    public static final QName RP_SERIALIZEDADVERTISABLE = new QName(NS, "SerializedAdvertisable");
    public static final QName RESOURCE_PROPERTIES = new QName(NS, "AdvertServiceEntryResourceProperties");
    public static final QName RESOURCE_REFERENCE = new QName(NS, "AdvertServiceEntryResourceReference");
}
