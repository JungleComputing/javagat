package services.AdvertServiceEntry.impl;

import java.util.Map;
import java.util.HashMap;
import stubs.AdvertServiceEntryService.MetaDataType;
import stubs.AdvertServiceEntryService.MetaDataTypeMetaDataEntry;

public class MetaDataTypeUtils
{
    public static MetaDataType toAdvertServiceEntryServiceMetaDataType(stubs.AdvertServiceEntryFactoryService.MetaDataType metaData)
    {
	stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry[] entries = metaData.getMetaDataEntry();
	MetaDataTypeMetaDataEntry[] resultEntries = new MetaDataTypeMetaDataEntry[entries.length];
	for(int i=0;i<entries.length;i++)
	    {
		stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry entry = entries[i];
		String key = entry.getKey();
		String value = entry.getValue();
		MetaDataTypeMetaDataEntry resultEntry = new MetaDataTypeMetaDataEntry();
		resultEntry.setKey(key);
		resultEntry.setValue(value);
		resultEntries[i] = resultEntry;
	    }

	return new MetaDataType(resultEntries);
    }

    public static boolean isMap(MetaDataType metaData)
    {
	return (toMap(metaData) != null);
    }

    private static Map toMap(MetaDataType metaData)
    {
	Map map = new HashMap();
	MetaDataTypeMetaDataEntry[] entries = metaData.getMetaDataEntry();
	for(int i=0;i<entries.length;i++)
	    {
		MetaDataTypeMetaDataEntry entry = entries[i];
		String key = entry.getKey();
		String value = entry.getValue();
		if(map.containsKey(key))
		    return null;
		else
		    map.put(key,value);
	    }

	return map;
    }
}
