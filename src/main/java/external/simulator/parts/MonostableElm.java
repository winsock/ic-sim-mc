package external.simulator.parts;

import external.simulator.ChipElm;
import external.simulator.EditInfo;
import external.simulator.Toggleable;

import java.util.StringTokenizer;

public class MonostableElm extends ChipElm {

	//Used to detect rising edge
	private boolean prevInputValue = false;
	private boolean retriggerable = false;
	private boolean triggered = false;
	private double lastRisingEdge = 0;
	private double delay = 0.01;

	public MonostableElm(int xx, int yy) {
		super(xx, yy);
	}

	public MonostableElm(int xa, int ya, int xb, int yb, int f,
	                     StringTokenizer st) {
		super(xa, ya, xb, yb, f, st);
		retriggerable = Boolean.valueOf(st.nextToken());
		delay = new Double(st.nextToken());
	}

	public String getChipName() {
		return "Monostable";
	}

	protected void setupPins() {
		sizeX = 2;
		sizeY = 2;
		pins = new Pin[getPostCount()];
		pins[0] = new Pin(0, SIDE_W, "");
		pins[0].clock = true;
		pins[1] = new Pin(0, SIDE_E, "Q");
		pins[1].output = true;
		pins[2] = new Pin(1, SIDE_E, "Q");
		pins[2].output = true;
		pins[2].lineOver = true;
	}

	protected int getPostCount() {
		return 3;
	}

	protected int getVoltageSourceCount() {
		return 2;
	}

	protected void execute() {

		if (prevInputValue != pins[0].value && (retriggerable || !triggered)) {
			lastRisingEdge = sim.getTime();
			pins[1].value = true;
			pins[2].value = false;
			triggered = true;
		}

		if (triggered && sim.getTime() > lastRisingEdge + delay) {
			pins[1].value = false;
			pins[2].value = true;
			triggered = false;
		}
		prevInputValue = pins[0].value;
	}

	protected String dump() {
		return super.dump() + " " + retriggerable + " " + delay;
	}

	public int getDumpType() {
		return 194;
	}

	public EditInfo getEditInfo(int n) {
		if (n == 2) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.setCheckbox(new Toggleable("Retriggerable", retriggerable));
			return ei;
		}
		if (n == 3) {
			return new EditInfo("Period (s)", delay, 0.001, 0.1);
		}
		return super.getEditInfo(n);
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 2) {
			retriggerable = ei.getCheckbox().getState();
		}
		if (n == 3) {
			delay = ei.getValue();
		}
		super.setEditValue(n, ei);
	}
}
