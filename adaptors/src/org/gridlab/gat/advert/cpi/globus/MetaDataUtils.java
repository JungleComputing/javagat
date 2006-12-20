package org.gridlab.gat.advert.cpi.globus;

import java.util.Map;
import java.util.HashMap;
import stubs.AdvertServiceEntryService.MetaDataType;
import stubs.AdvertServiceEntryService.MetaDataTypeMetaDataEntry;
import org.gridlab.gat.advert.MetaData;

class MetaDataUtils
{
    public static MetaDataType toAdvertServiceEntryServiceMetaDataType(MetaData metaData)
    {
	int size = metaData.size();
	MetaDataTypeMetaDataEntry[] resultEntries = new MetaDataTypeMetaDataEntry[size];
	for(int i=0;i<size;i++)
	    {
		String key = metaData.getKey(i);
		String value = metaData.getData(i);
		MetaDataTypeMetaDataEntry resultEntry = new MetaDataTypeMetaDataEntry();
		resultEntry.setKey(key);
		resultEntry.setValue(value);
		resultEntries[i] = resultEntry;
	    }

	return new MetaDataType(resultEntries);
    }

    public static stubs.AdvertServiceEntryFactoryService.MetaDataType toAdvertServiceEntryFactoryServiceMetaDataType(MetaData metaData)
    {
	int size = metaData.size();
	stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry[] resultEntries = new stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry[size];
	for(int i=0;i<size;i++)
	    {
		String key = metaData.getKey(i);
		String value = metaData.getData(i);
		stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry resultEntry = new stubs.AdvertServiceEntryFactoryService.MetaDataTypeMetaDataEntry();
		resultEntry.setKey(key);
		resultEntry.setValue(value);
		resultEntries[i] = resultEntry;
	    }

	return new stubs.AdvertServiceEntryFactoryService.MetaDataType(resultEntries);
    }
}
