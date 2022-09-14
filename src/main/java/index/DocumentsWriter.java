package index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DocumentsWriter {
    public DocumentsWriterDeleteQueue deleteQueue;
    DocumentsWriterPerThreadPool perThreadPool;

    final DocumentsWriterFlushControl flushControl;

    private final FlushNotifications flushNotifications;

    private final AtomicInteger numDocsInRAM = new AtomicInteger(0);

    private AtomicInteger numDocs = new AtomicInteger(0);

    public DocumentsWriter() {
        perThreadPool =new DocumentsWriterPerThreadPool(
                () -> {
//                    final FieldInfos.Builder infos = new FieldInfos.Builder(globalFieldNumberMap);
                    return new DocumentsWriterPerThread(
//                            indexCreatedVersionMajor,
//                            segmentNameSupplier.get(),
//                            directoryOrig,
//                            directory,
//                            config,
                            deleteQueue
//                            infos,
//                            pendingNumDocs,
//                            enableTestPoints
                    );
                });
        flushControl = new DocumentsWriterFlushControl(this);
        flushNotifications = null;
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

            }
            final boolean isUpdate = delNode != null && delNode.isDelete();
            flushingDWPT = flushControl.doAfterDocument(dwpt, isUpdate);
        } finally {
            if (dwpt.isFlushPending()) {
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

    interface FlushNotifications {
    }
}
