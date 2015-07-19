package external.simulator.Simulator;

import me.querol.andrew.ic.Gui.CircuitGUI;

import java.util.StringTokenizer;

public class LogicOutputElm extends CircuitElm {
	private final int FLAG_TERNARY = 1;
	private final int FLAG_NUMERIC = 2;
	private final int FLAG_PULLDOWN = 4;
	private double threshold;
	private String value;

	public LogicOutputElm(int xx, int yy) {
		super(xx, yy);
		threshold = 2.5;
	}

	public LogicOutputElm(int xa, int ya, int xb, int yb, int f,
	                      StringTokenizer st) {
		super(xa, ya, xb, yb, f);
		try {
			threshold = new Double(st.nextToken());
		} catch (Exception e) {
			threshold = 2.5;
		}
	}

	String dump() {
		return super.dump() + " " + threshold;
	}

	int getDumpType() {
		return 'M';
	}

	int getPostCount() {
		return 1;
	}

	boolean isTernary() {
		return (flags & FLAG_TERNARY) != 0;
	}

	boolean isNumeric() {
		return (flags & (FLAG_TERNARY | FLAG_NUMERIC)) != 0;
	}

	boolean needsPullDown() {
		return (flags & FLAG_PULLDOWN) != 0;
	}

	void setPoints() {
		super.setPoints();
		lead1 = interpPoint(point1, point2, 1 - 12 / dn);
	}

	public void draw(CircuitGUI.ClientCircuitGui g, int mouseX, int mouseY, float partialTicks) {
		//g.setColor(needsHighlight() ? selectColor : lightGrayColor);
		String s = (volts[0] < threshold) ? "L" : "H";
		if (isTernary()) {
			if (volts[0] > 3.75)
				s = "2";
			else if (volts[0] > 1.25)
				s = "1";
			else
				s = "0";
		} else if (isNumeric())
			s = (volts[0] < threshold) ? "0" : "1";
		value = s;
		setBbox(point1, lead1, 0);
		drawCenteredText(g, s, x2, y2, true, lightGrayColor);
		drawThickLine(g, point1, lead1, getVoltageColor(volts[0]));
		drawPosts(g, lightGrayColor);
	}

	void stamp() {
		if (needsPullDown())
			sim.stampResistor(nodes[0], 0, 1e6);
	}

	double getVoltageDiff() {
		return volts[0];
	}

	public void getInfo(String arr[]) {
		arr[0] = "logic output";
		arr[1] = (volts[0] < threshold) ? "low" : "high";
		if (isNumeric())
			arr[1] = value;
		arr[2] = "V = " + getVoltageText(volts[0]);
	}

	public EditInfo getEditInfo(int n) {
		if (n == 0)
			return new EditInfo("Threshold", threshold, 10, -10);
		if (n == 1) {
			EditInfo ei = new EditInfo("", 0, -1, -1);
			ei.checkbox = new Toggleable("Current Required", needsPullDown());
			return ei;
		}
		return null;
	}

	public void setEditValue(int n, EditInfo ei) {
		if (n == 0)
			threshold = ei.value;
		if (n == 1) {
			if (ei.checkbox.getState())
				flags = FLAG_PULLDOWN;
			else
				flags &= ~FLAG_PULLDOWN;
		}
	}

	@Override
	boolean isWire() {
		return false;
	}

	int getShortcut() {
		return 'o';
	}
}
