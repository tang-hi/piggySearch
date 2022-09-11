package index;

import document.FieldType;
import util.BytesRef;

public class Field implements IndexableField{

    protected String name;

    protected Object fieldData;

    protected FieldType type;

    public Field(String name, FieldType type, Object fieldData) {
        this.name = name;
        this.type = type;
        this.fieldData = fieldData;
    }

    public Field(String name, FieldType type) {
        this.name = name;
        this.type = type;
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public String stringValue() {
        if (fieldData instanceof String) {
            return (String) fieldData;
        }
        return null;
    }

    @Override
    public FieldType fieldType() {
        return type;
    }

    @Override
    public BytesRef binaryValue() {
        if (fieldData instanceof BytesRef) {
            return (BytesRef) fieldData;
        }
        return null;
    }

    @Override
    public Number numericValue() {
        if(fieldData instanceof Number) {
            return (Number) fieldData;
        }
        return null;
    }
}
