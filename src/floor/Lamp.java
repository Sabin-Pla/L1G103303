package floor;

public class Lamp {
    boolean on;

    public Lamp(boolean on) {
        this.on = on;
    }

    public void turnOn() {
        on = true;
    }

    public void turnOff() {
        on = false;
    }
}
