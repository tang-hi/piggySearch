package index;

import java.io.IOException;

public class DocumentsWriter {
    private DocumentsWriterPerThreadPool dwptPool;

    public DocumentsWriter() {
        dwptPool = new DocumentsWriterPerThreadPool();
    }

    public void addDoc(Iterable<? extends IndexableField> doc) throws IOException {
        DocumentsWriterPerThread dwpt = dwptPool.getAndLock();
        dwpt.addDoc(doc);
        dwptPool.free(dwpt);
    }
}
