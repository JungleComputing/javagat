package org.gridlab.gat.advert.cpi.globus;

import stubs.GAT.MetaData;
import stubs.GAT.MetaDataEntry;

//import org.gridlab.gat.advert.MetaData;

class MetaDataUtils {
    public static MetaData toSoapMetaData(
            org.gridlab.gat.advert.MetaData metaData) {
        int size = metaData.size();
        MetaDataEntry[] resultEntries = new MetaDataEntry[size];
        for (int i = 0; i < size; i++) {
            String key = metaData.getKey(i);
            String value = metaData.getData(i);
            MetaDataEntry resultEntry = new MetaDataEntry();
            resultEntry.setKey(key);
            resultEntry.setValue(value);
            resultEntries[i] = resultEntry;
        }

        return new MetaData(resultEntries);
    }

    public static org.gridlab.gat.advert.MetaData toMetaData(MetaData metaData) {
        org.gridlab.gat.advert.MetaData result = new org.gridlab.gat.advert.MetaData();
        MetaDataEntry[] entries = metaData.getEntry();
        for (int i = 0; i < entries.length; i++) {
            MetaDataEntry entry = entries[i];
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
