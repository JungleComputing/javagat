package org.gridlab.gat.advert.cpi.globus;

import org.gridlab.gat.advert.MetaData;

class Entry
{
    private String path;
    private MetaData metaData;
    private String serializedAdvertisable;
    private static final String ENCODING = "UTF8";

    public Entry(String path, MetaData metaData, String serializedAdvertisable)
    {
	this.path = path;
	this.metaData = metaData;
	this.serializedAdvertisable = serializedAdvertisable;
    }

    public Entry(String path, MetaData metaData, byte[] serializedAdvertisable) throws java.io.UnsupportedEncodingException
    {
	this.path = path;
	this.metaData = metaData;
	this.serializedAdvertisable = new String(serializedAdvertisable, ENCODING);
    }

    public String getPath()
    {
	return this.path;
    }

    public MetaData getMetaData()
    {
	return this.metaData;
    }

    public String getSerializedAdvertisable()
    {
	return this.serializedAdvertisable;
    }

    public byte[] getBytesOfSerializedAdvertisable() throws java.io.UnsupportedEncodingException
    {
	if(this.serializedAdvertisable != null)
	    return this.serializedAdvertisable.getBytes(ENCODING);
	else
	    return new byte[0];
    }
}
