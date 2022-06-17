package document;

import index.IndexableField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Document implements Iterable<IndexableField> {

    private final List<IndexableField> fields = new ArrayList<>();

    public Document() {}

    @Override
    public Iterator<IndexableField> iterator() {
        return fields.iterator();
    }

    public void add(IndexableField field) {
        fields.add(field);
    }

    public void removeField(String name) {
       Iterator<IndexableField>  it = fields.iterator();
       while (it.hasNext()) {
           IndexableField field = it.next();
           if(field.name().equals(name)) {
               it.remove();
               return;
           }
       }
    }

    public void removeFields(String name) {
        fields.removeIf(field -> field.name().equals(name));
    }
}
