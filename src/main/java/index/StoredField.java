package index;

import document.FieldType;
import util.BytesRef;

public class StoredField extends Field{

    public static final FieldType TYPE;

    static {
        TYPE = new FieldType();
        TYPE.setStored(true);
    }

    public StoredField(String name, FieldType type, Object fieldData) {
        super(name, type, fieldData);
    }

    public StoredField(String name, String value) {
        super(name, TYPE, value);
    }

    public StoredField(String name, BytesRef bytesRef) {
        super(name, TYPE, bytesRef);
    }

    public StoredField(String name, int numeric) {
        super(name, TYPE);
        fieldData = numeric;
    }

    public StoredField(String name, long numeric) {
        super(name, TYPE);
        fieldData = numeric;
    }

    public StoredField(String name, float numeric) {
        super(name, TYPE);
        fieldData = numeric;
    }
    public StoredField(String name, double numeric) {
        super(name, TYPE);
        fieldData = numeric;
    }
}
