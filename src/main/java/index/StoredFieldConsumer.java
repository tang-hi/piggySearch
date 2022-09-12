package index;

import disk.DiskWriter;
import disk.RamReader;
import disk.RamWriter;
import disk.Writer;
import store.Directory;
import util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class StoredFieldConsumer {



    public static final String FIELDS_EXTENSION = "fdt";

    public static final String INDEX_EXTENSION = "fdx";

    public static final String META_EXTENSION = "fdm";

    static final int STRING = 0x00;

    static final int BYTE_ARR = 0x01;

    static final int NUMERIC_INT = 0x02;

    static final int NUMERIC_FLOAT = 0x03;

    static final int NUMERIC_LONG = 0x04;

    static final int NUMERIC_DOUBLE = 0x05;


    private static final String HEADER = "piggySearch";

    private static final String FOOTER = "hcraeSyggip";

    final int maxDocsPerChunk;

    private Writer bufferedDocs;

    private Writer fieldsStream, metaStream;

    private int numBufferedDocs;
    private int numStoredFieldsInDoc = 0;

    private final String segment;

    private int numChunks;

    private int docBase;

    private List<Integer> numStoredFields;

    private List<Integer> endOffsets;

    StoredFieldConsumer(
        Directory directory,
        SegmentInfo si
    ) throws IOException {
        this.bufferedDocs = new RamWriter();
        numBufferedDocs = 0;
        docBase = 0;
        numStoredFields = new ArrayList<>();
        endOffsets = new ArrayList<>();
        maxDocsPerChunk = 128;
        numChunks = 0;

        boolean success = false;
        try {
            segment = si.name;
            metaStream = directory.createOutput(IndexFileNames.segmentFileName(segment, "", META_EXTENSION));
            metaStream.writeString(HEADER);
            fieldsStream = directory.createOutput(IndexFileNames.segmentFileName(segment, "", FIELDS_EXTENSION));
            fieldsStream.writeString(HEADER);

            success = true;
        } finally {

        }


    }



    public void finishDocument() throws IOException {
        numStoredFields.add(numStoredFieldsInDoc);
        numStoredFieldsInDoc = 0;

        endOffsets.add((int) bufferedDocs.size());
        numBufferedDocs++;

        assert numBufferedDocs == endOffsets.size();
        assert numBufferedDocs == numStoredFields.size();


        if(triggerFlush()) {
            flush();
        }
    }

    private boolean triggerFlush() {
        return numBufferedDocs >= maxDocsPerChunk;
    }

    private void flush() {
        numChunks++;

        final int[] lengths = new int[endOffsets.size()];
        for(int i = numBufferedDocs - 1; i > 0; --i) {
            lengths[i] = endOffsets.get(i) - endOffsets.get(i-1);
            assert lengths[i] >= 0;
        }

//        writeFdtHeader(docBase, numBufferedDocs, numStoredFields, lengths, sliced, dirtyChunk);

        RamReader byteBuffers = new RamReader(bufferedDocs.toByteBuffer());

    }


    void writeField(FieldInfo info, IndexableField field) throws IOException {
        ++numStoredFieldsInDoc;
        int bits = 0;

        Number number = field.numericValue();
        BytesRef bytes = null;
        String string = null;

        if(number != null) {
            if (number instanceof Byte || number instanceof Short || number instanceof Integer) {
                bits = NUMERIC_INT;
            } else if(number instanceof Long) {
                bits = NUMERIC_LONG;
            } else if(number instanceof Float) {
                bits = NUMERIC_FLOAT;
            } else if(number instanceof Double) {
                bits = NUMERIC_DOUBLE;
            } else {
                throw new IllegalArgumentException("can't store numeric type " + number.getClass());
            }
        } else {
            bytes = field.binaryValue();
            if(bytes == null) {
                bits = BYTE_ARR;
                string = null;
            } else {
                bits = STRING;
                string = field.stringValue();
                if(string == null) {
                    throw  new IllegalArgumentException(
                            "field "  + field.name()
                            +" is stored but does not have binaryValue, stringValue not numericValue"
                    );
                }
            }
        }

        final long infoAndBits = (((long) info.number) << 3) | bits;

        bufferedDocs.writeVLong(infoAndBits);

        if(bytes != null) {
            bufferedDocs.writeVInt(bytes.length);
            bufferedDocs.writeBytes(bytes.bytes, bytes.offset, bytes.length);
        } else if(string != null) {
            bufferedDocs.writeString(string);
        } else {
            if(number instanceof Byte || number instanceof Short || number instanceof  Integer) {
                bufferedDocs.writeZInt(number.intValue());
            } else if(number instanceof Long) {
                bufferedDocs.writeTlong(number.longValue());
            } else if(number instanceof Float) {
                bufferedDocs.writeZFloat(number.floatValue());
            } else if(number instanceof Double) {
                bufferedDocs.writeZDouble(number.doubleValue());
            } else {
                throw  new AssertionError("panic: shouldn't be here");
            }
        }
    }
}
