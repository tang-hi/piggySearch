package index;

import java.io.IOException;
import java.util.List;

public class IndexWriter {

    private DocumentsWriter docWriter;

    public IndexWriter() {
        docWriter = new DocumentsWriter();
    }


    /**
     * Adds a document to this index.
     *
     * <p>Note that if an Exception is hit (for example disk full) then the index will be consistent,
     * but this document may not have been added. Furthermore, it's possible the index will have one
     * segment in non-compound format even when using compound files (when a merge has partially
     * succeeded).
     *
     * <p>This method periodically flushes pending documents to the Directory (see <a
     * href="#flush">above</a>), and also periodically triggers segment merges in the index according
     * to the {@link MergePolicy} in use.
     *
     * <p>Merges temporarily consume space in the directory. The amount of space required is up to 1X
     * the size of all segments being merged, when no readers/searchers are open against the index,
     * and up to 2X the size of all segments being merged when readers/searchers are open against the
     * index (see {@link #forceMerge(int)} for details). The sequence of primitive merge operations
     * performed is governed by the merge policy.
     *
     * <p>Note that each term in the document can be no longer than {@link #MAX_TERM_LENGTH} in bytes,
     * otherwise an IllegalArgumentException will be thrown.
     *
     * <p>Note that it's possible to create an invalid Unicode string in java if a UTF16 surrogate
     * pair is malformed. In this case, the invalid characters are silently replaced with the Unicode
     * replacement character U+FFFD.
     *
     * @return The <a href="#sequence_number">sequence number</a> for this operation
     * @throws CorruptIndexException if the index is corrupt
     * @throws IOException if there is a low-level IO error
     */
    public long addDocument(Iterable<? extends IndexableField> doc) throws IOException {
        return updateDocument(null, doc);
    }

    /**
     * Atomically adds a block of documents with sequentially assigned document IDs, such that an
     * external reader will see all or none of the documents.
     *
     * <p><b>WARNING</b>: the index does not currently record which documents were added as a block.
     * Today this is fine, because merging will preserve a block. The order of documents within a
     * segment will be preserved, even when child documents within a block are deleted. Most search
     * features (like result grouping and block joining) require you to mark documents; when these
     * documents are deleted these search features will not work as expected. Obviously adding
     * documents to an existing block will require you the reindex the entire block.
     *
     * <p>However it's possible that in the future Lucene may merge more aggressively re-order
     * documents (for example, perhaps to obtain better index compression), in which case you may need
     * to fully re-index your documents at that time.
     *
     * <p>See {@link #addDocument(Iterable)} for details on index and IndexWriter state after an
     * Exception, and flushing/merging temporary free space requirements.
     *
     * <p><b>NOTE</b>: tools that do offline splitting of an index (for example, IndexSplitter in
     * contrib) or re-sorting of documents (for example, IndexSorter in contrib) are not aware of
     * these atomically added documents and will likely break them up. Use such tools at your own
     * risk!
     *
     * @return The <a href="#sequence_number">sequence number</a> for this operation
     * @throws CorruptIndexException if the index is corrupt
     * @throws IOException if there is a low-level IO error
     * @lucene.experimental
     */
    public long addDocuments(Iterable<? extends Iterable<? extends IndexableField>> docs)
            throws IOException {
        return updateDocuments((DocumentsWriterDeleteQueue.Node<?>) null, docs);
    }

    /**
     * Updates a document by first deleting the document(s) containing <code>term</code> and then
     * adding the new document. The delete and then add are atomic as seen by a reader on the same
     * index (flush may happen only after the add).
     *
     * @return The <a href="#sequence_number">sequence number</a> for this operation
     * @param term the term to identify the document(s) to be deleted
     * @param doc the document to be added
     * @throws CorruptIndexException if the index is corrupt
     * @throws IOException if there is a low-level IO error
     */
    public long updateDocument(Term term, Iterable<? extends IndexableField> doc) throws IOException {
        return updateDocuments(
                term == null ? null : DocumentsWriterDeleteQueue.newNode(term), List.of(doc));
    }

    /**
     * Atomically deletes documents matching the provided delTerm and adds a block of documents with
     * sequentially assigned document IDs, such that an external reader will see all or none of the
     * documents.
     *
     * <p>See {@link #addDocuments(Iterable)}.
     *
     * @return The <a href="#sequence_number">sequence number</a> for this operation
     * @throws CorruptIndexException if the index is corrupt
     * @throws IOException if there is a low-level IO error
     * @lucene.experimental
     */
    public long updateDocuments(
            Term delTerm, Iterable<? extends Iterable<? extends IndexableField>> docs)
            throws IOException {
        return updateDocuments(
                delTerm == null ? null : DocumentsWriterDeleteQueue.newNode(delTerm), docs);
    }

    private long updateDocuments(
            final DocumentsWriterDeleteQueue.Node<?> delNode,
            Iterable<? extends Iterable<? extends IndexableField>> docs)
            throws IOException {
        boolean success = false;
        try {
            final long seqNo = maybeProcessEvents(docWriter.updateDocuments(docs, delNode));
            success = true;
            return seqNo;
        } catch (VirtualMachineError tragedy) {
            throw tragedy;
        } finally {

        }
    }

    private long maybeProcessEvents()


}
