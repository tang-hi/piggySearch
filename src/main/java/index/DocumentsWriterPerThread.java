package index;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

final class DocumentsWriterPerThread {

    final DocumentsWriterDeleteQueue deleteQueue;

    private final ReentrantLock lock = new ReentrantLock();

    private int numDocsInRAM;

    private AtomicBoolean flushPending = new AtomicBoolean(false);

    private IndexingChain indexingChain;
    public DocumentsWriterPerThread(DocumentsWriterDeleteQueue deleteQueue) {
        this.deleteQueue = deleteQueue;
        indexingChain = new IndexingChain();
        numDocsInRAM = 0;
    }

    long updateDocuments(
            Iterable<? extends Iterable<? extends IndexableField>> docs,
            DocumentsWriterDeleteQueue.Node<?> deleteNode,
            DocumentsWriter.FlushNotifications flushNotifications,
            Runnable onNewDocOnRAM)
            throws IOException {
        try {
            final int docsInRamBefore = numDocsInRAM;
            boolean allDocsIndexed = false;
            try {
                for (Iterable<? extends IndexableField> doc : docs) {
                    // Even on exception, the document is still added (but marked
                    // deleted), so we don't need to un-reserve at that point.
                    // Aborting exceptions will actually "lose" more than one
                    // document, so the counter will be "wrong" in that case, but
                    // it's very hard to fix (we can't easily distinguish aborting
                    // vs non-aborting exceptions):
                    try {
                        indexingChain.processDocument(doc,numDocsInRAM++);
                    } finally {
                        onNewDocOnRAM.run();
                    }
                }
                allDocsIndexed = true;
                return finishDocuments(deleteNode, docsInRamBefore);
            } finally {
                if (!allDocsIndexed ) {
                    // the iterator threw an exception that is not aborting
                    // go and mark all docs from this block as deleted
                    throw new RuntimeException("update Documents occur exception");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("update Documents occur exception");
        }
    }

    private long finishDocuments(DocumentsWriterDeleteQueue.Node<?> deleteNode, int docsInRamBefore) {
        return 0;
    }

    private void reserveOneDoc() {
    }

    void lock() {
        lock.lock();
    }

    boolean tryLock() {
        return lock.tryLock();
    }

    void unlock() {
        lock.unlock();
    }

    // TODO: wait to finish
    public long ramBytesUsed() {
        return 0;
    }

    boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    public boolean isFlushPending() {
        return flushPending.get() == Boolean.TRUE;
    }
}
