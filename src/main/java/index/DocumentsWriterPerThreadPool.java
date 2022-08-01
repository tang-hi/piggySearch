package index;

import java.util.LinkedList;

public class DocumentsWriterPerThreadPool {

    private LinkedList<DocumentsWriterPerThread> freeList;

    public DocumentsWriterPerThreadPool() {
        freeList = new LinkedList<>();
    }

    public DocumentsWriterPerThread getAndLock() {
        synchronized (this) {
            DocumentsWriterPerThread dwpt = freeList.poll();
            if (dwpt == null) {
                return new DocumentsWriterPerThread();
            }
            return dwpt;
        }
    }

    public void free(DocumentsWriterPerThread dwpt) {
        if(dwpt == null) {
            throw new IllegalArgumentException("dwpt is null");
        }
        synchronized (this) {
            freeList.push(dwpt);
        }
    }


}
