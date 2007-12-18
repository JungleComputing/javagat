package services.AdvertServiceEntry.impl;

import java.util.*;
import stubs.GAT.*;

public class MetaDataUtils
{
    public static boolean isMap(MetaData metaData)
    {
	return (toMap(metaData) != null);
    }

    private static Map toMap(MetaData metaData)
    {
	Map map = new HashMap();
	MetaDataEntry[] entries = metaData.getEntry();
	for(int i=0;i<entries.length;i++)
	    {
		MetaDataEntry entry = entries[i];
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
