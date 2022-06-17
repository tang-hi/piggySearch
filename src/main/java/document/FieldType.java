package document;

public class FieldType {

    private boolean stored;

    private boolean indexed;

    private boolean docValue;

    public FieldType() {}

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isDocValue() {
        return docValue;
    }

    public void setDocValue(boolean docValue) {
        this.docValue = docValue;
    }
}
