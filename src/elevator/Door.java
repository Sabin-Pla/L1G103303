package elevator;

public class Door {
    boolean isOpen;

    public Door() {
        this.isOpen = false;
    }

    public void open() {
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return this.isOpen;
    }
}
