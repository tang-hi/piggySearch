package index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentsWriter {
    private DocumentsWriterPerThreadPool dwptPool;

    final DocumentsWriterFlushControl flushControl;

    private AtomicInteger numDocs = new AtomicInteger(0);

    public DocumentsWriter() {
        dwptPool = new DocumentsWriterPerThreadPool();
        flushControl = new DocumentsWriterFlushControl(this);
    }

    public void addDoc(Iterable<? extends IndexableField> doc) throws IOException {
        DocumentsWriterPerThread dwpt = dwptPool.getAndLock();
        dwpt.addDoc(doc, numDocs.incrementAndGet());
        dwptPool.free(dwpt);
    }

    long updateDocuments(
            final Iterable<? extends Iterable<? extends IndexableField>> docs,
            final DocumentsWriterDeleteQueue.Node<?> delNode)
            throws IOException {
//        boolean hasEvents = preUpdate();

        final DocumentsWriterPerThread dwpt = flushControl.obtainAndLock();
        final DocumentsWriterPerThread flushingDWPT;
        long seqNo;

        try {
            // This must happen after we've pulled the DWPT because IW.close
            // waits for all DWPT to be released:
//            ensureOpen();
            try {
                seqNo =
                        dwpt.updateDocuments(docs, delNode, flushNotifications, numDocsInRAM::incrementAndGet);
            } finally {
                if (dwpt.isAborted()) {
                    flushControl.doOnAbort(dwpt);
                }
            }
            final boolean isUpdate = delNode != null && delNode.isDelete();
            flushingDWPT = flushControl.doAfterDocument(dwpt, isUpdate);
        } finally {
            if (dwpt.isFlushPending() || dwpt.isAborted()) {
                dwpt.unlock();
            } else {
                perThreadPool.marksAsFreeAndUnlock(dwpt);
            }
            assert dwpt.isHeldByCurrentThread() == false : "we didn't release the dwpt even on abort";
        }

//        if (postUpdate(flushingDWPT, hasEvents)) {
//            seqNo = -seqNo;
//        }
        return seqNo;
    }
}
