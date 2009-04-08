package org.gridlab.gat.io.cpi.attributes;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gridlab.gat.URI;
import org.gridlab.gat.io.permissions.attribute.*;

/**
 * @author Jerome Revillard
 */
public abstract class AbstractPosixFileAttributeView extends
        AbstractBasicFileAttributeView implements PosixFileAttributeView {
    private static final String PERMISSIONS_NAME = "permissions"; // Set<PosixFilePermission>
    private static final String OWNER_NAME = "owner"; // UserPrincipal
    private static final String GROUP_NAME = "group"; // GroupPrincipal

    public AbstractPosixFileAttributeView(URI location,
            boolean followSymbolicLinks) {
        super(location, followSymbolicLinks);
    }

    public String name() {
        return "posix";
    }

    @Override
    public Object getAttribute(String attribute) throws IOException {
        Object attributeValue = super.getAttribute(attribute);
        if (attributeValue == null) {
            if (attribute.equals(PERMISSIONS_NAME))
                attributeValue = readAttributes().permissions();
            if (attribute.equals(OWNER_NAME))
                attributeValue = readAttributes().owner();
            if (attribute.equals(GROUP_NAME))
                attributeValue = readAttributes().group();
        }
        return attributeValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAttribute(String attribute, Object value) throws IOException {
        if (attribute.equals(PERMISSIONS_NAME)) {
            setPermissions((Set<PosixFilePermission>) value);
            return;
        }
        if (attribute.equals(OWNER_NAME)) {
            setOwner((UserPrincipal) value);
            return;
        }
        if (attribute.equals(GROUP_NAME)) {
            setGroup((GroupPrincipal) value);
            return;
        }
        super.setAttribute(attribute, value);
    }

    public UserPrincipal getOwner() throws IOException {
        return (UserPrincipal) getAttribute(OWNER_NAME);
    }

    public Map<String, ?> readAttributes(String first, String... rest)
            throws IOException {
        Set<String> requestedAttributesNames = new HashSet<String>();
        boolean copyAll = false;
        if (first.equals("*")) {
            copyAll = true;
        } else {
            requestedAttributesNames.add(first);
            for (String attribute : rest) {
                if (attribute.equals("*")) {
                    copyAll = true;
                    break;
                }
                requestedAttributesNames.add(attribute);
            }
        }

        PosixFileAttributes posixFileAttributes = readAttributes();
        Map<String, Object> attributes = getAttributesMap(posixFileAttributes);
        if (copyAll) {
            return attributes;
        }

        for (Iterator<String> iterator = attributes.keySet().iterator(); iterator
                .hasNext();) {
            String attributeName = iterator.next();
            if (!requestedAttributesNames.contains(attributeName)) {
                iterator.remove();
            }
        }
        return attributes;
    }

    protected Map<String, Object> getAttributesMap(
            PosixFileAttributes posixFileAttributes) {
        Map<String, Object> attributes = super
                .getAttributesMap(posixFileAttributes);
        attributes.put(PERMISSIONS_NAME, posixFileAttributes.permissions());
        attributes.put(OWNER_NAME, posixFileAttributes.owner());
        attributes.put(GROUP_NAME, posixFileAttributes.group());
        return attributes;
    }
}
