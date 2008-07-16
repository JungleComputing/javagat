package hu.sztaki.lpds.monitor;

/**
 * Represents a record in a Mercury type definition tree.
 *
 * Values of Mercury records are represented as <code>Object[]</code> arrays
 * in Java. The elements of the array correspond to the fields of the record
 * in the order they appear in the record definition.
 *
 * @author G??bor Gomb??s
 * @version $Id: RecordType.java 857 2006-04-19 09:43:22Z rob $
 *
 * Use, modification and distribution is subject to the GridLal Software
 * License. See the "COPYING" file in the root directory of the source
 * tree or obtain a copy at http://www.gridlab.org/GLlicense.txt
 */
public class RecordType extends MonitorType {
    /** Descriptors of the fields of the record. */
    private MonitorType[] fields;

    /**
     * Constructs a new record type.
     *
     * The fields may be {@link SimpleType simple}, {@link ArrayType array}
     * or {@link RecordType record} types.
     *
     * @param name                the name of the type.
     * @param fields        field definitions.
     */
    public RecordType(String name, MonitorType[] fields) {
        super(name);
        this.fields = fields;
    }

    /**
     * Returns the fields of the record.
     *
     * @return                the field descriptors.
     */
    public MonitorType[] getFields() {
        return fields;
    }

    protected void internalToString(StringBuffer str) {
        str.append("rec { ");

        for (int i = 0; i < fields.length; i++) {
            fields[i].internalToString(str);
            str.append("; ");
        }

        str.append("}");

        if (getName() != null) {
            str.append(" ");
            str.append(getName());
        }
    }

    protected void internalToString(StringBuffer str, Object value) {
        str.append("{ ");

        for (int i = 0; i < fields.length; i++) {
            fields[i].internalToString(str, ((Object[]) value)[i]);

            if (i < (fields.length - 1)) {
                str.append(", ");
            }
        }

        str.append(" }");
    }
}
