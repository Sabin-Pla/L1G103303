package floor;

public class Lamp {
    boolean on;

    public Lamp(boolean on) {
        this.on = on;
    }

    public void turnOn() {
        if (on == false) {
            System.out.println("\nLamp: On");
            on = true;
        }
    }

    public void turnOff() {
        if (on == true){
            System.out.println("\nLamp: Off");
            on = false;
        }
    }
}
