package elevator;

public class Door {
    //True or false depending on if door is closed or not respectively.
    boolean isClosed;

    public Door(){
        this.isClosed = false;
    }

    /**
     * Represents closed elevator door by setting boolean isClosed value to true
     */
    public void closeDoor(){
        isClosed = true;
    }

    /**
     * Represents closed elevator door by setting boolean isClosed value to false
     */
    public void openDoor(){
        isClosed = false;
    }

    /**
     *
     * @return boolean value of isClosed. Will be true or false depending on state.
     */
    public boolean isClosed(){
        return isClosed;
    }
}