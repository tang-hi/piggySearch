package index;

import document.FieldType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndexingChain {

    private long nextFieldGen = 0;

    private Map<String, PerField> fieldHash = new HashMap<>();

    private Map<String, Integer> fieldNumber = new ConcurrentHashMap<>();

    AtomicInteger nextFieldNum = new AtomicInteger(0);

    private List<PerField> fields = new ArrayList<>();

    private StoredFieldConsumer storedFieldConsumer;

    public void processDocument(Iterable<? extends IndexableField> document, int docID) throws IOException{

        try {
            List<PerField> docFields = new ArrayList<>();

            for(IndexableField field : document) {
                PerField pf = getOrAddPerField(field.name());
                if(pf.fieldInfo == null) {
                    initializeFieldInfo(pf);
                }
                docFields.add(pf);
            }

            int docFieldIdx = 0;
            for(IndexableField field : document) {
                if(processField(docID, field, docFields.get(docFieldIdx))) {
                    // ignore
                }
                docFieldIdx++;
            }
        } finally {
            finishStoredFields();
        }

    }

    private void finishStoredFields() throws IOException{
        try {
            storedFieldConsumer.finishDocument();
        }catch (Exception e) {
            throw e;
        }

    }

    private void initializeFieldInfo(PerField pf) {
        fieldNumber.computeIfAbsent(pf.fieldName, k -> nextFieldNum.incrementAndGet());
        pf.fieldInfo = new FieldInfo(pf.fieldName, fieldNumber.get(pf.fieldName));
    }

    private boolean processField(int docID, IndexableField field, PerField pf) throws IOException {
        IndexableFieldType type = field.fieldType();

        if(type.stored()) {
            String value  =field.stringValue();
            if(value != null && value.length() > 20000) {
                throw new IllegalArgumentException("too large field string value");
            }

            try {
                storedFieldConsumer.writeField(pf.fieldInfo, field);
            } catch (Exception ignored) {

            }
        }
        return true;
    }

    private PerField getOrAddPerField(String fieldName) {
        PerField pf = fieldHash.getOrDefault(fieldName, null);

        if (pf == null) {
            pf = new PerField(fieldName);
        }

        fieldHash.put(fieldName, pf);
        return pf;
    }

    private final class PerField implements Comparable<PerField> {

        final String fieldName;

        long fieldGen = -1;

        private boolean first;

        FieldInfo fieldInfo;

        int docID;

        PerField(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public int compareTo(PerField other) {
            return this.fieldName.compareTo(other.fieldName);
        }

        void reset() {
            first = true;
        }
    }
}
