package external.simulator.Simulator;

/**
 * Created by winsock on 3/21/15.
 */
public class Toggleable {
    private String description;
    private boolean state;

    public Toggleable(String description, boolean state) {
        this.description = description;
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public boolean getState() {
        return state;
    }
}
