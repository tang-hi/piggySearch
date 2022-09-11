package index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentsWriter {
    private DocumentsWriterPerThreadPool dwptPool;

    private AtomicInteger numDocs = new AtomicInteger(0);

    public DocumentsWriter() {
        dwptPool = new DocumentsWriterPerThreadPool();
    }

    public void addDoc(Iterable<? extends IndexableField> doc) throws IOException {
        DocumentsWriterPerThread dwpt = dwptPool.getAndLock();
        dwpt.addDoc(doc, numDocs.incrementAndGet());
        dwptPool.free(dwpt);
    }
}
