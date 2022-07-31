package index;

import document.FieldType;
import util.BytesRef;

public interface IndexableField {

    String name();

    String stringValue();

    FieldType fieldType();

    BytesRef byteValue();

    Number numericValue();
}