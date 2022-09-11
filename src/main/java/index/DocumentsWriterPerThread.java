package index;

import document.Document;

import java.io.IOException;

public class DocumentsWriterPerThread {

    private IndexingChain indexingChain;
    public DocumentsWriterPerThread() {
        indexingChain = new IndexingChain();
    }

    public void addDoc(Iterable<? extends IndexableField> doc, int docID) throws IOException {
        indexingChain.processDocument(doc, docID);
    }
}
