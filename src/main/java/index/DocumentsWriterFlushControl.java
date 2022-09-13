package index;

public class DocumentsWriterFlushControl {

   private final DocumentsWriter documentsWriter;

    private final DocumentsWriterPerThreadPool perThreadPool;


    private boolean closed = false;

//    DocumentsWriterFlushControl(DocumentsWriter documentsWriter, LiveIndexWriterConfig config) {
//        this.infoStream = config.getInfoStream();
//        this.perThreadPool = documentsWriter.perThreadPool;
//        this.flushPolicy = config.getFlushPolicy();
//        this.config = config;
//        this.hardMaxBytesPerDWPT = config.getRAMPerThreadHardLimitMB() * 1024 * 1024;
//        this.documentsWriter = documentsWriter;
//    }

    DocumentsWriterFlushControl(DocumentsWriter documentsWriter) {
        this.perThreadPool = documentsWriter.perThreadPool;
        this.documentsWriter = documentsWriter;
    }

    DocumentsWriterPerThread obtainAndLock() {
        while (closed == false) {
            final DocumentsWriterPerThread perThread = perThreadPool.getAndLock();
            if (perThread.deleteQueue == documentsWriter.deleteQueue) {
                // simply return the DWPT even in a flush all case since we already hold the lock and the
                // DWPT is not stale
                // since it has the current delete queue associated with it. This means we have established
                // a happens-before
                // relationship and all docs indexed into this DWPT are guaranteed to not be flushed with
                // the currently
                // progress full flush.
                return perThread;
            } else {
//                try {
//                    // we must first assert otherwise the full flush might make progress once we unlock the
//                    // dwpt
//                    assert fullFlush && fullFlushMarkDone == false
//                            : "found a stale DWPT but full flush mark phase is already done fullFlush: "
//                            + fullFlush
//                            + " markDone: "
//                            + fullFlushMarkDone;
//                } finally {
//                    perThread.unlock();
//                    // There is a flush-all in process and this DWPT is
//                    // now stale - try another one
//                }
            }
        }
//        throw new AlreadyClosedException("flush control is closed");
    }
}
