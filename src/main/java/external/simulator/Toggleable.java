package external.simulator;

public class Toggleable {
	private final String description;
	private final boolean state;

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
