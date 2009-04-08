package org.gridlab.gat.io.cpi.attributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.gridlab.gat.URI;
import org.gridlab.gat.io.attributes.BasicFileAttributeView;
import org.gridlab.gat.io.attributes.BasicFileAttributes;

/**
 * @author Jerome Revillar
 */
public abstract class AbstractBasicFileAttributeView implements
        BasicFileAttributeView {
    protected URI location;
    protected boolean followSymbolicLinks;

    private static final String SIZE_NAME = "size";
    private static final String CREATION_TIME_NAME = "creationTime";
    private static final String LAST_ACCESS_TIME_NAME = "lastAccessTime";
    private static final String LAST_MODIFIED_TIME_NAME = "lastModifiedTime";
    private static final String RESOLUTION_NAME = "resolution";
    private static final String FILE_KEY_NAME = "fileKey";
    private static final String LINK_COUNT_NAME = "linkCount";
    private static final String IS_DIRECTORY_NAME = "isDirectory";
    private static final String IS_REGULAR_FILE_NAME = "isRegularFile";
    private static final String IS_SYMBOLIC_LINK_NAME = "isSymbolicLink";
    private static final String IS_OTHER_NAME = "isOther";

    public AbstractBasicFileAttributeView(URI location,
            boolean followSymbolicLinks) {
        this.location = location;
        this.followSymbolicLinks = followSymbolicLinks;
    }

    public String name() {
        return "basic";
    }

    public Object getAttribute(String attribute) throws IOException {
        if (attribute.equals(SIZE_NAME))
            return readAttributes().size();
        if (attribute.equals(CREATION_TIME_NAME))
            return readAttributes().creationTime();
        if (attribute.equals(LAST_ACCESS_TIME_NAME))
            return readAttributes().lastAccessTime();
        if (attribute.equals(LAST_MODIFIED_TIME_NAME))
            return readAttributes().lastModifiedTime();
        if (attribute.equals(RESOLUTION_NAME))
            return readAttributes().resolution();
        if (attribute.equals(FILE_KEY_NAME))
            return readAttributes().fileKey();
        if (attribute.equals(LINK_COUNT_NAME))
            return readAttributes().linkCount();
        if (attribute.equals(IS_DIRECTORY_NAME))
            return readAttributes().isDirectory();
        if (attribute.equals(IS_REGULAR_FILE_NAME))
            return readAttributes().isRegularFile();
        if (attribute.equals(IS_SYMBOLIC_LINK_NAME))
            return readAttributes().isSymbolicLink();
        if (attribute.equals(IS_OTHER_NAME))
            return readAttributes().isOther();
        return null;
    }

    private Long checkTimeValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        Long time = (Long) value;
        if (time < 0L && time != -1L)
            throw new IllegalArgumentException("time value cannot be negative");
        return time;
    }

    public void setAttribute(String attribute, Object value) throws IOException {
        if (attribute.equals(LAST_MODIFIED_TIME_NAME)) {
            setTimes(checkTimeValue(value), null, null, TimeUnit.MILLISECONDS);
            return;
        }
        if (attribute.equals(LAST_ACCESS_TIME_NAME)) {
            setTimes(null, checkTimeValue(value), null, TimeUnit.MILLISECONDS);
            return;
        }
        if (attribute.equals(CREATION_TIME_NAME)) {
            setTimes(null, null, checkTimeValue(value), TimeUnit.MILLISECONDS);
            return;
        }
        throw new UnsupportedOperationException("'" + attribute
                + "' is unknown or read-only attribute");
    }

    protected Map<String, Object> getAttributesMap(
            BasicFileAttributes basicAttributes) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put(SIZE_NAME, basicAttributes.size());
        attributes.put(CREATION_TIME_NAME, basicAttributes.creationTime());
        attributes.put(LAST_ACCESS_TIME_NAME, basicAttributes.lastAccessTime());
        attributes.put(LAST_MODIFIED_TIME_NAME, basicAttributes
                .lastModifiedTime());
        attributes.put(RESOLUTION_NAME, basicAttributes.resolution());
        attributes.put(FILE_KEY_NAME, basicAttributes.fileKey());
        attributes.put(LINK_COUNT_NAME, basicAttributes.linkCount());
        attributes.put(IS_DIRECTORY_NAME, basicAttributes.isDirectory());
        attributes.put(IS_REGULAR_FILE_NAME, basicAttributes.isRegularFile());
        attributes.put(IS_SYMBOLIC_LINK_NAME, basicAttributes.isSymbolicLink());
        attributes.put(IS_OTHER_NAME, basicAttributes.isOther());
        return attributes;
    }
}
