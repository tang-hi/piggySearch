package index;

import document.FieldType;
import util.BytesRef;

import java.io.Reader;

/**
 * Represents a single field for indexing. IndexWriter consumes Iterable&lt;IndexableField&gt; as a
 * document.
 *
 * @lucene.experimental
 */
public interface IndexableField {

    /** Field name */
    public String name();

    /** {@link IndexableFieldType} describing the properties of this field. */
    public IndexableFieldType fieldType();

    /** Non-null if this field has a binary value */
    public BytesRef binaryValue();

    /** Non-null if this field has a string value */
    public String stringValue();

    /** Non-null if this field has a string value */
    default CharSequence getCharSequenceValue() {
        return stringValue();
    }

    /** Non-null if this field has a Reader value */
    public Reader readerValue();

    /** Non-null if this field has a numeric value */
    public Number numericValue();
}
