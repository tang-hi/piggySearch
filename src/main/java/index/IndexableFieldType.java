package index;

import java.util.Map;

public interface IndexableFieldType {

    /** True if the field's value should be stored */
    boolean stored();

    /**
     * True if this field's value should be analyzed by the {@link Analyzer}.
     *
     * <p>This has no effect if {@link #indexOptions()} returns IndexOptions.NONE.
     */
    // TODO: shouldn't we remove this?  Whether/how a field is
    // tokenized is an impl detail under Field?
    boolean tokenized();

    /**
     * True if this field's indexed form should be also stored into term vectors.
     *
     * <p>This builds a miniature inverted-index for this field which can be accessed in a
     * document-oriented way from {@link IndexReader#getTermVector(int,String)}.
     *
     * <p>This option is illegal if {@link #indexOptions()} returns IndexOptions.NONE.
     */
    boolean storeTermVectors();

    /**
     * True if this field's token character offsets should also be stored into term vectors.
     *
     * <p>This option is illegal if term vectors are not enabled for the field ({@link
     * #storeTermVectors()} is false)
     */
    boolean storeTermVectorOffsets();

    /**
     * True if this field's token positions should also be stored into the term vectors.
     *
     * <p>This option is illegal if term vectors are not enabled for the field ({@link
     * #storeTermVectors()} is false).
     */
    boolean storeTermVectorPositions();

    /**
     * True if this field's token payloads should also be stored into the term vectors.
     *
     * <p>This option is illegal if term vector positions are not enabled for the field ({@link
     * #storeTermVectors()} is false).
     */
    boolean storeTermVectorPayloads();

    /**
     * True if normalization values should be omitted for the field.
     *
     * <p>This saves memory, but at the expense of scoring quality (length normalization will be
     * disabled), and if you omit norms, you cannot use index-time boosts.
     */
    boolean omitNorms();

    /** {@link IndexOptions}, describing what should be recorded into the inverted index */
    IndexOptions indexOptions();


    /**
     * If this is positive (representing the number of point dimensions), the field is indexed as a
     * point.
     */
    int pointDimensionCount();

    /** The number of dimensions used for the index key */
    int pointIndexDimensionCount();

    /** The number of bytes in each dimension's values. */
    int pointNumBytes();

    /** The number of dimensions of the field's vector value */
    int vectorDimension();


    /**
     * Attributes for the field type.
     *
     * <p>Attributes are not thread-safe, user must not add attributes while other threads are
     * indexing documents with this field type.
     *
     * @return Map
     */
    Map<String, String> getAttributes();
}