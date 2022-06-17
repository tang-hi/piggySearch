package index;

import document.Document;

public class DocumentsWriterPerThread {

    private IndexingChain indexingChain;
    public DocumentsWriterPerThread() {
        indexingChain = new IndexingChain();
    }

    public void addDoc(Iterable<? extends IndexableField> doc) {
        indexingChain.processDocument(doc);
    }
}
