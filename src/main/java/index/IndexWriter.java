package index;

import java.io.IOException;

public class IndexWriter {

    private DocumentsWriter docWriter;

    public IndexWriter() {
        docWriter = new DocumentsWriter();
    }


    public void addDoc(Iterable<? extends IndexableField> doc) throws IOException {
        docWriter.addDoc(doc);

    }
}
