package index;

public class DocumentsWriterDeleteQueue {

    static Node<Term> newNode(Term term) {
        return new Node(term);
    }


    static class Node<T> {
        volatile Node<?> next;
        final T item;

        Node(T item) {
            this.item = item;
        }

        void apply(BufferedUpdates bufferedDeletes, int docIDUpto) {
            throw new IllegalStateException("sentinel item must never be applied");
        }

        boolean isDelete() {
            return true;
        }
    }
}
